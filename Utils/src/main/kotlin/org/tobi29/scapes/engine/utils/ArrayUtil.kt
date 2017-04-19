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

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun BooleanArray.fill(supplier: (Int) -> Boolean) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun ByteArray.fill(supplier: (Int) -> Byte) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun ShortArray.fill(supplier: (Int) -> Short) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun IntArray.fill(supplier: (Int) -> Int) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun LongArray.fill(supplier: (Int) -> Long) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun FloatArray.fill(supplier: (Int) -> Float) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun DoubleArray.fill(supplier: (Int) -> Double) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 */
inline fun CharArray.fill(supplier: (Int) -> Char) {
    for (i in indices) {
        set(i, supplier(i))
    }
}

/**
 * Fills the given array with values
 * @receiver Array to fill
 * @param supplier Supplier called for each value written to the array
 * @param E type
 */
inline fun <E> Array<in E>.fill(supplier: (Int) -> E) {
    for (i in indices) {
        set(i, supplier(i))
    }
}
