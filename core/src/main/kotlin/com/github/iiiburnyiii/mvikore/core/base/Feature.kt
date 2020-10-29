package com.github.iiiburnyiii.mvikore.core.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow

interface Feature<in Intent, out State, out Event> : Store<Intent, State>, CoroutineScope {

    val events: SharedFlow<Event>

}
