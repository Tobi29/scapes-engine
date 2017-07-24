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
 * Throw [AssertionError] in case [block] returns false
 *
 * **Note:** [block] is not guaranteed to run, so there should be no side
 * effects in it
 * @param message The message to display in the [AssertionError]
 * @param block The code to assert
 */
inline fun assert(message: String? = null,
                  block: () -> Boolean) =
        assert(Assertions.ENABLED, message, block)

/**
 * Throw [AssertionError] in case [block] returns false
 *
 * **Note:** [block] is not guaranteed to run, so there should be no side
 * effects in it
 * @param message The message to display in the [AssertionError]
 * @param block The code to assert
 */
inline fun assert(message: () -> String?,
                  block: () -> Boolean) =
        assert(Assertions.ENABLED, message, block)

/**
 * Throw [AssertionError] in case [block] returns false
 *
 * **Note:** [block] is not guaranteed to run, so there should be no side
 * effects in it
 * @param block The code to assert
 */
inline fun assert(block: () -> Boolean) = assert(Assertions.ENABLED, block)

/**
 * Throw [AssertionError] in case [block] returns false
 * @param enabled Whether or not to actually run the assertion
 * @param message The message to display in the [AssertionError]
 * @param block The code to assert
 */
inline fun assert(enabled: Boolean,
                  message: String? = null,
                  block: () -> Boolean) = assert(enabled, { message }, block)

/**
 * Throw [AssertionError] in case [block] returns false
 * @param enabled Whether or not to actually run the assertion
 * @param message The message to display in the [AssertionError]
 * @param block The code to assert
 */
inline fun assert(enabled: Boolean,
                  message: () -> String?,
                  block: () -> Boolean): Boolean {
    if (enabled) {
        if (!block()) {
            throw message()?.let { AssertionError(it) } ?: AssertionError()
        }
        return true
    }
    return false
}

/**
 * Throw [AssertionError] in case [block] returns false
 * @param enabled Whether or not to actually run the assertion
 * @param block The code to assert
 */
inline fun assert(enabled: Boolean,
                  block: () -> Boolean) = assert(enabled, null, block)
