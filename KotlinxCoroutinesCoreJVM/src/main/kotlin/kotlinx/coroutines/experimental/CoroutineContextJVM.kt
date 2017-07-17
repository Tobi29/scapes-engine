/*
 * Copyright 2016-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlinx.coroutines.experimental

import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.CoroutineContext

private const val DEBUG_PROPERTY_NAME = "kotlinx.coroutines.debug"

private val DEBUG = run {
    val value = System.getProperty(DEBUG_PROPERTY_NAME)
    when (value) {
        "auto", null -> CoroutineId::class.java.desiredAssertionStatus()
        "on", "" -> true
        "off" -> false
        else -> error("System property '$DEBUG_PROPERTY_NAME' has unrecognized value '$value'")
    }
}

private val COROUTINE_ID = AtomicLong()

// for tests only
impl internal fun resetCoroutineId() {
    COROUTINE_ID.set(0)
}

impl public fun newCoroutineContext(context: CoroutineContext): CoroutineContext =
        if (DEBUG) context + CoroutineId(
                COROUTINE_ID.incrementAndGet()) else context

@PublishedApi
impl internal fun updateContext(context: CoroutineContext): String? {
    if (!DEBUG) return null
    val newId = context[CoroutineId] ?: return null
    val currentThread = Thread.currentThread()
    val oldName = currentThread.name
    val coroutineName = context[CoroutineName]?.name ?: "coroutine"
    currentThread.name = buildString(
            oldName.length + coroutineName.length + 10) {
        append(oldName)
        append(" @")
        append(coroutineName)
        append('#')
        append(newId.id)
    }
    return oldName
}

@PublishedApi
impl internal fun restoreContext(oldName: String?) {
    if (oldName != null) Thread.currentThread().name = oldName
}

private class CoroutineId(val id: Long) : AbstractCoroutineContextElement(
        CoroutineId) {
    companion object Key : CoroutineContext.Key<CoroutineId>

    override fun toString(): String = "CoroutineId($id)"
}
