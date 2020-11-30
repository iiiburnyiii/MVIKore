package io.github.iiiburnyiii.mvikore.core

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

interface Store<in Intent, out State> : FlowCollector<Intent>, StateFlow<State>