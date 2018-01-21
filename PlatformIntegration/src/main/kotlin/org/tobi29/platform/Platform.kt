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

package org.tobi29.platform

/**
 * Enum containing all platforms known that may be relevant to a running
 * application using this api
 */
enum class Platform {
    /**
     * GNU/Linux system
     */
    LINUX,

    /**
     * macOS system
     */
    MACOS,

    /**
     * Non-Linux, non-macOS Unix system, e.g. BSDs, Solaris, etc.
     */
    UNIX,

    /**
     * Something that needs to go.
     */
    WINDOWS,

    /**
     * Android system
     */
    ANDROID,

    /**
     * iOS system (either through Kotlin/Native or Multi OS Engine)
     */
    IOS,

    /**
     * Something that luckily never made it anywhere so far. Also needs to go.
     */
    WINDOWS_PHONE,

    /**
     * An unsupported system, consider reporting info on how to detect it if any
     * device returns this
     */
    UNKNOWN
}
