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

import java.util.*

/**
 * Keeps references to the listener objects, so they get garbage collected once
 * the owner goes unused
 * @param valid Implementation for [isValid]
 */
class ListenerOwnerHandle(private val valid: () -> Boolean = { true }) {
    /**
     * Returns whether or not listeners of this owner should receive events at a
     * given moment
     */
    val isValid: Boolean
        get() = valid()

    private val listeners = HashSet<Any>()

    internal fun add(listener: Any) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }
}
