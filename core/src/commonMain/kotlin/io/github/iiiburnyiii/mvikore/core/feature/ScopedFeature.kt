package io.github.iiiburnyiii.mvikore.core.feature

import io.github.iiiburnyiii.mvikore.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

open class ScopedFeature<in Intent, in Effect, out State, out Event>(
    initialState: State,
    autoInit: Boolean = true,
    flowDispatcher: CoroutineDispatcher = Dispatchers.Default,
    actor: Actor<Intent, Effect, State>,
    reducer: Reducer<Effect, State>,
    eventPublisher: EventPublisher<Intent, Effect, State, Event>? = null,
    private val bootstrapper: Bootstrapper<Intent>? = null
) : Feature<Intent, State, Event> {

    private val stateFlow = MutableStateFlow(initialState)
    private val eventFlow = MutableSharedFlow<Event>()
    private val intentFlow = MutableSharedFlow<Intent>()

    private val featureScope = CoroutineScope(SupervisorJob() + flowDispatcher)
    override val events: SharedFlow<Event> get() = eventFlow

    override val value: State get() = stateFlow.value
    override val replayCache: List<State> get() = stateFlow.replayCache

    private val eventPublisherWrapper = eventPublisher?.let {
        EventPublisherWrapper(it, eventFlow)
    }

    private val reducerWrapper = ReducerWrapper(
        reducer = reducer,
        states = stateFlow,
        eventsPublisherWrapper = eventPublisherWrapper
    )

    private val actorWrapper = ActorWrapper(
        actor = actor,
        states = stateFlow,
        reducerWrapper = reducerWrapper
    )

    init {
        if (autoInit) initFeature()
    }

    protected fun initFeature() = featureScope.launch {
        intentFlow
            .onEach { intent -> actorWrapper.emit(intent to value) }
            .launchIn(this)

        bootstrapper?.invoke()
            ?.cancellable()
            ?.onEach { intent -> intentFlow.emit(intent) }
            ?.launchIn(this)
    }

    override suspend fun emit(value: Intent) {
        intentFlow.emit(value)
    }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<State>) {
        stateFlow.collect(collector)
    }

    override fun close() {
        featureScope.cancel()
    }

}
