/*
 * Copyright 2012-2018 Tobi29
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tobi29.profiler

import kotlinx.coroutines.experimental.ThreadContextElement
import kotlinx.coroutines.experimental.withContext
import org.tobi29.logging.KLogger
import org.tobi29.profiler.spi.ProfilerDispatcherProvider
import org.tobi29.stdex.ThreadLocal
import org.tobi29.stdex.readOnly
import org.tobi29.utils.spiLoad
import kotlin.collections.set
import kotlin.coroutines.experimental.CoroutineContext

actual class Profiler {
    private val instance by ThreadLocal {
        val thread = Thread.currentThread()
        val node = Node(lazy { "${thread.id}-${thread.name}" }, root)
        node to ProfilerHandle(node).also {
            root._children[node.name] = node
        }
    }

    actual val root = Node("Threads")
    actual val roots get() = root.children
    actual val threadRoot get() = instance.first

    @PublishedApi
    internal actual fun current() = instance.second
}

internal actual val dispatchers get() = Dispatchers.i

private object Dispatchers {
    private val logger = KLogger<Dispatchers>()

    @JvmStatic
    val i = spiLoad(
        spiLoad<ProfilerDispatcherProvider>(
            Dispatchers::class.java.classLoader
        ), { e ->
            logger.warn(e) { "Service configuration error" }
        }).mapNotNull { it.dispatcher() }.readOnly()
}

// TODO: Experimental
suspend fun <R> profilerSectionSuspend(
    name: String, receiver: suspend () -> R
): R = withContext(newProfilerContext(name)) {
    receiver()
}

@PublishedApi
internal fun newProfilerContext(name: String): CoroutineContext =
    ProfilerNodeContext(name)

private class ProfilerNodeContext(
    private val name: String
) : ThreadContextElement<ProfilerHandle> {
    companion object Key : CoroutineContext.Key<ProfilerNodeContext>

    override val key: CoroutineContext.Key<ProfilerNodeContext> get() = Key

    override fun updateThreadContext(
        context: CoroutineContext
    ): ProfilerHandle = profilerHandle!!.apply {
        println("${Thread.currentThread().name}: Entered $name")
        enterNode(name)
    }

    override fun restoreThreadContext(
        context: CoroutineContext, oldState: ProfilerHandle
    ) {
        println("${Thread.currentThread().name}: Exited $name")
        oldState.exitNode(name)
    }
}
