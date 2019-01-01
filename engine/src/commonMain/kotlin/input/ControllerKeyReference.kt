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

package org.tobi29.scapes.engine.input

class ControllerKeyReference {
    private val key: ControllerKey?
    private val modifiers: List<ControllerKey>

    constructor() {
        key = null
        modifiers = emptyList()
    }

    constructor(key: ControllerKey,
                vararg modifiers: ControllerKey) {
        this.key = key
        this.modifiers = modifiers.toList()
    }

    constructor(key: ControllerKey,
                modifiers: List<ControllerKey>) {
        this.key = key
        this.modifiers = modifiers
    }

    constructor(keys: List<ControllerKey>) {
        if (keys.isEmpty()) {
            throw IllegalArgumentException(
                    "List requires at least one key")
        }
        key = keys[keys.size - 1]
        modifiers = ArrayList(keys.size - 1)
        for (i in 0 until keys.size - 1) {
            modifiers.add(keys[i])
        }
    }

    fun isDown(controller: ControllerButtons): Boolean {
        if (key == null) {
            return false
        }
        if (!controller.isDown(key)) {
            return false
        }
        for (key in modifiers) {
            if (!controller.isDown(key)) {
                return false
            }
        }
        return true
    }

    fun isPressed(event: ControllerKey,
                  controller: ControllerButtons): Boolean {
        return event == key && isDown(controller)
    }

    fun isReleased(event: ControllerKey): Boolean {
        return event == key
    }

    override fun toString(): String {
        val str = StringBuilder(20)
        str.append(key)
        for (key in modifiers) {
            str.append(',').append(key)
        }
        return str.toString()
    }

    fun humanName(): String {
        val str = StringBuilder(20)
        key?.let { str.append(it.humanName) }
        for (key in modifiers) {
            str.append(" + ").append(key.humanName)
        }
        return str.toString()
    }

    companion object {
        fun valueOf(str: String?): ControllerKeyReference? {
            if (str == null) {
                return ControllerKeyReference()
            }
            val split = str.split(',', limit = 2)
            val modifiers: List<ControllerKey>
            modifiers = if (split.size > 1) {
                split[1].split(',').map {
                    ControllerKey.valueOf(it) ?: return null
                }
            } else {
                emptyList()
            }
            return ControllerKeyReference(
                    ControllerKey.valueOf(split[0]) ?: return null, modifiers)
        }

        fun isPressed(event: ControllerKey,
                      controller: ControllerButtons,
                      vararg references: ControllerKeyReference?) =
                mostSpecific({ it.isPressed(event, controller) }, *references)

        private fun mostSpecific(
                check: (ControllerKeyReference) -> Boolean,
                vararg references: ControllerKeyReference?): ControllerKeyReference? {
            var length = -1
            var key: ControllerKeyReference? = null
            for (reference in references) {
                if (reference == null) {
                    continue
                }
                if (check(reference) && reference.modifiers.size > length) {
                    key = reference
                    length = reference.modifiers.size
                }
            }
            return key
        }
    }
}

fun ControllerKeyReference?.isDown(controller: ControllerButtons) =
        this?.isDown(controller) ?: false

fun ControllerKeyReference?.isPressed(event: ControllerKey,
                                      controller: ControllerButtons) =
        this?.isPressed(event, controller) ?: false
