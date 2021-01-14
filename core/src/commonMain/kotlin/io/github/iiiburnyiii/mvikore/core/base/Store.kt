package io.github.iiiburnyiii.mvikore.core.base

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

public interface Store<in Intent, out State> : FlowCollector<Intent>, StateFlow<State>