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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

/* impl */ inline fun Float.bits() = java.lang.Float.floatToIntBits(this)

/* impl */ inline fun Double.bits() = java.lang.Double.doubleToLongBits(this)

/* impl */ inline fun Int.bitsToFloat() = java.lang.Float.intBitsToFloat(this)

/* impl */ inline fun Long.bitsToDouble() = java.lang.Double.longBitsToDouble(
        this)
