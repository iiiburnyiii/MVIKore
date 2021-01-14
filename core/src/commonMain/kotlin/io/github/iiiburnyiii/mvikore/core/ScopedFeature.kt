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
    eventPublisher: EventPublisher<Intent, Effect, State, Event>? = null,
    private val bootstrapper: Bootstrapper<Intent>? = null
) : Feature<Intent, State, Event>,
    CoroutineScope by CoroutineScope(SupervisorJob() + flowDispatcher) {

    private val stateMutex = Mutex()
    private val stateFlow = MutableStateFlow(initialState)
    private val eventFlow = MutableSharedFlow<Event>()
    private val intentFlow = MutableSharedFlow<Intent>()
    private val subscribedSignal = Channel<Unit>()

    override val events: SharedFlow<Event> get() = eventFlow

    override val value: State get() = stateFlow.value
    override val replayCache: List<State> get() = stateFlow.replayCache

    init {
        launch {
            bootstrapper?.invoke()
                ?.onStart { subscribedSignal.receive() } /** await subscription to [stateFlow] */
                ?.cancellable()
                ?.collect { intent -> emit(intent) }
        }

        launch {
            intentFlow.collect { intent ->
                launch {
                    actor(intent, stateFlow.value)
                        .cancellable()
                        .collect { effect ->
                            reducer(effect, stateFlow.value).also { newState ->
                                stateMutex.withLock {
                                    stateFlow.value = newState
                                }

                                eventPublisher
                                    ?.invoke(intent, effect, newState)
                                    ?.let { event -> eventFlow.emit(event) }
                            }
                        }
                }
            }
        }
    }

    override suspend fun emit(value: Intent) {
        intentFlow.emit(value)
    }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<State>) {
        stateFlow
            .onSubscription { subscribedSignal.send(Unit) }
            .collect(collector)
    }

    override fun cancel() {
        (this as CoroutineScope).cancel()
    }

}
