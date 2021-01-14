package io.github.iiiburnyiii.mvikore.core.base

import kotlinx.coroutines.flow.SharedFlow

public interface Feature<in Intent, out State, out Event> : Store<Intent, State> {

    val events: SharedFlow<Event>

    fun cancel()

}
