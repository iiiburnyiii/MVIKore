package io.github.iiiburnyiii.mvikore.core

import io.github.iiiburnyiii.mvikore.core.base.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public open class ScopedFeature<in Intent, in Effect, out State, out Event>(
    initialState: State,
    flowDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val actor: Actor<Intent, Effect, State>,
    private val reducer: Reducer<Effect, State>,
    private val eventPublisher: EventPublisher<Effect, State, Event>? = null,
    bootstrapper: Bootstrapper<Intent>? = null
) : Feature<Intent, State, Event>,
    CoroutineScope by CoroutineScope(SupervisorJob() + flowDispatcher){

    private val stateFlow = MutableStateFlow(initialState)
    private val eventFlow = MutableSharedFlow<Event>()
    private val intentFlow = MutableSharedFlow<Intent>()

    private val subscribedSignal = MutableStateFlow(false)
    private val stateMutex = Mutex()

    init {
        launch {
            intentFlow.flatMapMerge { intent ->
                actor(intent, stateFlow.value).cancellable()
            }.collect { effect ->
                handleEffect(effect)
            }
        }

        launch {
            bootstrapper?.invoke()
                ?.cancellable()
                ?.onStart {
                    /** await subscription to [stateFlow] */
                    subscribedSignal.first { it }
                }
                ?.collect { bootstrapperIntent ->
                    intentFlow.emit(bootstrapperIntent)
                }
        }

    }

    override val events: SharedFlow<Event> get() = eventFlow

    override val value: State get() = stateFlow.value
    override val replayCache: List<State> get() = stateFlow.replayCache

    override suspend fun emit(value: Intent) {
        intentFlow.emit(value)
    }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<State>) {
        if (!subscribedSignal.value) subscribedSignal.value = true
        stateFlow.collect(collector)
    }

    private suspend fun handleEffect(effect: Effect) {
        reducer(effect, stateFlow.value).let { newState ->
            stateMutex.withLock {
                stateFlow.value = newState
            }
            eventPublisher?.invoke(effect, newState)
                ?.let { eventFlow.emit(it) }
        }
    }

    override fun cancel() {
        (this as CoroutineScope).cancel()
    }

}
