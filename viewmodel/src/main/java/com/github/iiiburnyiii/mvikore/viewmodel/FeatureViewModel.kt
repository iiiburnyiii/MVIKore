package com.github.iiiburnyiii.mvikore.viewmodel

import androidx.lifecycle.ViewModel
import com.github.iiiburnyiii.mvikore.core.BaseFeature
import com.github.iiiburnyiii.mvikore.core.base.*
import kotlinx.coroutines.*

open class FeatureViewModel<Intent, Effect, State, Event>(
    initialState: State,
    launchDispatcher: CoroutineDispatcher = Dispatchers.Default,
    actor: Actor<Intent, Effect, State>,
    reducer: Reducer<Effect, State>,
    bootstrapper: Bootstrapper<Intent>? = null,
    eventPublisher: EventPublisher<Intent, Effect, State, Event>? = null
) : ViewModel(), Feature<Intent, State, Event> by BaseFeature<Intent, Effect, State, Event>(
    initialState = initialState,
    launchDispatcher = launchDispatcher,
    actor = actor,
    reducer = reducer,
    bootstrapper = bootstrapper,
    eventPublisher = eventPublisher
) {

    override fun onCleared() {
        super.onCleared()
        coroutineContext.cancel()
    }

}