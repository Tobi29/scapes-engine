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

import org.tobi29.stdex.concurrent.read
import org.tobi29.stdex.concurrent.withLock

@Deprecated(
    "Use version from stdex",
    ReplaceWith("StampLock", "org.tobi29.stdex.concurrent.StampLock")
)
typealias StampLock = org.tobi29.stdex.concurrent.StampLock

@Deprecated(
    "Use version from stdex",
    ReplaceWith("read(block)", "org.tobi29.stdex.concurrent.read")
)
inline fun <R> StampLock.read(crossinline block: () -> R): R = read(block)

@Deprecated(
    "Use version from stdex",
    ReplaceWith("withLock(block)", "org.tobi29.stdex.concurrent.withLock")
)
inline fun <R> StampLock.write(block: () -> R): R = withLock(block)