package com.github.iiiburnyiii.mvikore.core.base

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow

interface Store<in Intent, out State> : FlowCollector<Intent>, SharedFlow<State> {

    val state: State

}