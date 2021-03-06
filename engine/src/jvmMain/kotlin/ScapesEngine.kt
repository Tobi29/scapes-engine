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

@file:JvmName("ScapesEngineJVMKt")
package org.tobi29.scapes.engine

internal actual fun ScapesEngine.initEngineEarly() {
    val runtime = Runtime.getRuntime()
    ScapesEngine.logger.info {
        "Operating system: ${System.getProperty(
            "os.name"
        )} ${System.getProperty(
            "os.version"
        )} ${System.getProperty("os.arch")}"
    }
    ScapesEngine.logger.info {
        "Java: ${System.getProperty(
            "java.version"
        )} (MaxMemory: ${runtime.maxMemory() / 1048576}, Processors: ${runtime.availableProcessors()})"
    }
}

internal actual fun ScapesEngine.initEngineLate() {
    registerComponent(
        MemoryProfilerComponent.COMPONENT,
        MemoryProfilerComponent(debugValues)
    )
}
