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
    actor: Actor<Intent, Effect, State>,
    reducer: Reducer<Effect, State>,
    eventPublisher: EventPublisher<Effect, State, Event>? = null,
    bootstrapper: Bootstrapper<Intent>? = null
) : Feature<Intent, State, Event>,
    CoroutineScope by CoroutineScope(SupervisorJob() + flowDispatcher){

    private val stateFlow = MutableStateFlow(initialState)
    private val eventFlow = MutableSharedFlow<Event>()
    private val intentFlow = MutableSharedFlow<Intent>()
    private val subscribedSignal = MutableStateFlow(false)

    init {
        launch {
            intentFlow.flatMapConcat { intent ->
                actor(intent, stateFlow.value)
                    .cancellable()
                    .map { effect ->
                        reducer(effect, stateFlow.value).let { newState ->
                            newState to eventPublisher?.invoke(effect, newState)
                        }
                    }
            }.collect { (newState: State, event: Event?) ->
                stateFlow.value = newState

                event?.let {
                    eventFlow.emit(it)
                }
            }
        }

        launch {
            bootstrapper?.invoke()
                ?.cancellable()
                ?.onStart { subscribedSignal.first { it } } /** await subscription to [stateFlow] */
                ?.let { bootstrapperFlow ->
                    intentFlow.emitAll(bootstrapperFlow)
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
        subscribedSignal.value = true
        stateFlow.collect(collector)
    }

    override fun cancel() {
        (this as CoroutineScope).cancel()
    }

}
