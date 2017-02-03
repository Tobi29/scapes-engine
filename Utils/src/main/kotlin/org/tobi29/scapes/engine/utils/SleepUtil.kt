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

package org.tobi29.scapes.engine.utils

/**
 * Calls [Thread.sleep] whilst catching [InterruptedException] and discarding it
 * @param time The time to sleep in milliseconds
 */
fun sleep(time: Long) {
    try {
        Thread.sleep(time)
    } catch (e: InterruptedException) {
    }
}

/**
 * Calls [Thread.sleep] whilst catching [InterruptedException] and discarding it
 *
 * In case the sleep ended short of the desired time, it repeats until
 * successful
 * @param time The time to sleep in milliseconds
 */
fun sleepAtLeast(time: Long) {
    val end = System.currentTimeMillis() + time
    var delta = time
    do {
        sleep(delta)
        delta = end - System.currentTimeMillis()
    } while (delta > 0)
}
