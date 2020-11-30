package io.github.iiiburnyiii.mvikore.core

import kotlinx.coroutines.flow.SharedFlow

interface Feature<in Intent, out State, out Event> : Store<Intent, State> {

    val events: SharedFlow<Event>

    fun close()

}
