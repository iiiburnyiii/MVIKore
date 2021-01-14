package io.github.iiiburnyiii.mvikore.core

import kotlinx.coroutines.flow.Flow

typealias Bootstrapper<Intent> = () -> Flow<Intent>

typealias Actor<Intent, Effect, State> = (intent: Intent, state: State) -> Flow<Effect>

typealias Reducer<Effect, State> = (effect: Effect, state: State) -> State

typealias EventPublisher<Effect, State, Event> = (effect: Effect, state: State) -> Event?
