
package com.github.iiiburnyiii.mvikore.core

import com.github.iiiburnyiii.mvikore.core.base.Actor
import com.github.iiiburnyiii.mvikore.core.base.EventPublisher
import com.github.iiiburnyiii.mvikore.core.base.Reducer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*

internal class ActorWrapper<Intent, State, Effect>(
    private val actor: Actor<Intent, Effect, State>,
    private val states: StateFlow<State>,
    private val reducerWrapper: FlowCollector<Triple<Intent, Effect, State>>
) : FlowCollector<Pair<Intent, State>> {

    override suspend fun emit(value: Pair<Intent, State>) {
        val (intent, state) = value

        coroutineScope {
            actor(intent, state).cancellable()
                .onEach { effect -> reducerWrapper.emit(Triple(intent, effect, states.value)) }
                .launchIn(this)
        }
    }

}

internal class ReducerWrapper<Intent, Effect, State>(
    private val reducer: Reducer<Effect, State>,
    private val states: MutableStateFlow<State>,
    private val eventsPublisherWrapper: FlowCollector<Triple<Intent, Effect, State>>?
) : FlowCollector<Triple<Intent, Effect, State>> {

    override suspend fun emit(value: Triple<Intent, Effect, State>) {
        val (intent, effect, state) = value

        val newState = reducer(effect, state)
        states.emit(newState)

        eventsPublisherWrapper?.emit(Triple(intent, effect, state))
    }

}

internal class EventPublisherWrapper<Intent, Effect, State, Event>(
    private val eventPublisher: EventPublisher<Intent, Effect, State, Event>,
    private val events: MutableSharedFlow<Event>
) : FlowCollector<Triple<Intent, Effect, State>> {

    override suspend fun emit(value: Triple<Intent, Effect, State>) {
        val (intent, effect, state) = value

        eventPublisher(intent, effect, state)?.let { event ->
            events.emit(event)
        }
    }

}
