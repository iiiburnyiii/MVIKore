package com.github.iiiburnyiii.mvikore.core

import com.github.iiiburnyiii.mvikore.core.base.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

open class BaseFeature<in Intent, in Effect, out State, out Event>(
    initialState: State,
    launchDispatcher: CoroutineDispatcher,
    actor: Actor<Intent, Effect, State>,
    reducer: Reducer<Effect, State>,
    eventPublisher: EventPublisher<Intent, Effect, State, Event>?,
    private val bootstrapper: Bootstrapper<Intent>?
) : Feature<Intent, State, Event> {

    private val stateFlow = MutableStateFlow(initialState)
    private val eventFlow = MutableSharedFlow<Event>()
    private val intentFlow = MutableSharedFlow<Intent>()

    override val coroutineContext: CoroutineContext = launchDispatcher
    override val state: State get() = stateFlow.value
    override val events: SharedFlow<Event> get() = eventFlow

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
        launch {
            intentFlow
                .onEach { intent -> actorWrapper.emit(intent to state) }
                .launchIn(this)

            bootstrapper?.invoke()
                ?.onEach { intent -> intentFlow.emit(intent) }
                ?.launchIn(this)
        }
    }

    override suspend fun emit(value: Intent) {
        intentFlow.emit(value)
    }

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<State>) {
        stateFlow.collect(collector)
    }

}
