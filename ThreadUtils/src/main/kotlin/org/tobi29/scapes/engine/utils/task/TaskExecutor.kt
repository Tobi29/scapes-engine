/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.utils.task

import kotlinx.coroutines.experimental.Job
import org.tobi29.scapes.engine.utils.Crashable

header class TaskExecutor {
    constructor(parent: TaskExecutor,
                name: String,
                wakeup: Joiner? = null)

    constructor(crashHandler: Crashable,
                name: String,
                wakeup: Joiner? = null)

    fun runTask(task: suspend () -> Unit,
                name: String): Job

    fun runTask(task: () -> Unit,
                taskLock: TaskLock,
                name: String)

    fun shutdown()

    header enum class Priority {
        HIGH,
        MEDIUM,
        LOW
    }
}
