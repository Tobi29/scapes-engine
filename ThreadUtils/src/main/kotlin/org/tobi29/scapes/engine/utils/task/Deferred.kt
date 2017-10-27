package org.tobi29.scapes.engine.utils.task

import kotlinx.coroutines.experimental.Deferred

inline fun <T> Deferred<T>.tryGet(): T? =
        if (isCompleted) getCompleted() else null
