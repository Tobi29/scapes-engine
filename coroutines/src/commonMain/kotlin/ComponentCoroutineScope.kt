/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.tobi29.stdex.Volatile
import org.tobi29.utils.ComponentHolder
import org.tobi29.utils.ComponentLifecycle
import org.tobi29.utils.ComponentRegisteredHolder
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class ComponentCoroutineScope<in H : ComponentHolder<out Any>> :
    ComponentRegisteredHolder<H>, CoroutineScope {
    @Volatile
    private var job: Job? = null
    override val coroutineContext: CoroutineContext
        // FIXME: How to handle null properly?
        get() = job ?: EmptyCoroutineContext

    override fun init(holder: H) {
        super.init(holder)
        job = Job()
    }

    override fun dispose(holder: H) {
        job?.cancel()
        super.dispose(holder)
    }
}

abstract class ComponentJobHandle<in H : ComponentHolder<out Any>> :
    ComponentCoroutineScope<H>(), ComponentLifecycle<H> {
    private val jobHandle = JobHandle(this)
    @Volatile
    private var holder: H? = null

    override val coroutineContext: CoroutineContext
        get() = super.coroutineContext.let { coroutineContext ->
            val holder = holder
            if (holder == null) coroutineContext
            else coroutineContextFor(holder) + coroutineContext
        }

    protected abstract suspend fun CoroutineScope.runJobTask(holder: H)

    protected open fun coroutineContextFor(holder: H): CoroutineContext =
        EmptyCoroutineContext

    override fun init(holder: H) {
        super<ComponentCoroutineScope>.init(holder)
        this.holder = holder
        start()
    }

    override fun dispose(holder: H) {
        halt()
        this.holder = null
        super<ComponentCoroutineScope>.dispose(holder)
    }

    override fun start() {
        val holder = holder ?: error("Started before initializing")
        jobHandle.launch { runJobTask(holder) }
    }

    override fun halt() {
        jobHandle.job?.cancel()
    }
}
