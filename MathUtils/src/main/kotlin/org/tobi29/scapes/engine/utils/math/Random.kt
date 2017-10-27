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

package org.tobi29.scapes.engine.utils.math

/**
 * Source of (pseudo) random values
 *
 * **Note:** No guarantees about thread-safety or security of the implementation
 * are given
 */
interface Random {
    /**
     * Returns either `true` or `false`
     * @return `true` or `false`
     */
    fun nextBoolean(): Boolean

    /**
     * Returns a value in range `0..[bound] - 1`
     * @return a value in range `0..[bound] - 1`
     */
    fun nextInt(bound: Int) = nextInt(0, bound - 1)

    /**
     * Returns a value in range `[min]..[max]`
     * @return a value in range `[min]..[max]`
     */
    fun nextInt(min: Int = Int.MIN_VALUE,
                max: Int = Int.MAX_VALUE): Int {
        if (max < min) {
            throw IllegalArgumentException(
                    "Invalid random bounds: $min <= $max")
        }
        var r = nextInt()
        val m = max - min
        val n = m + 1
        if (n and m == 0) {
            r = (r and m) + min
        } else if (n > 0) {
            var u = r ushr 1
            r = u % n
            while (u + m - r < 0) {
                u = nextInt() ushr 1
                r = u % n
            }
            r += min
        } else {
            while (r < min || r > max) {
                r = nextInt()
            }
        }
        return r
    }

    /**
     * Returns a value in range `[Int.MIN_VALUE]..[Int.MAX_VALUE]`
     * @return a value in range `[Int.MIN_VALUE]..[Int.MAX_VALUE]`
     */
    fun nextInt(): Int

    /**
     * Returns a value in range `0L..[bound] - 1L`
     * @return a value in range `0L..[bound] - 1L`
     */
    fun nextLong(bound: Long) = nextLong(0L, bound - 1L)

    /**
     * Returns a value in range `[min]..[max]`
     * @return a value in range `[min]..[max]`
     */
    fun nextLong(min: Long = Long.MIN_VALUE,
                 max: Long = Long.MAX_VALUE): Long {
        if (max < min) {
            throw IllegalArgumentException(
                    "Invalid random bounds: $min <= $max")
        }
        var r = nextLong()
        val m = max - min
        val n = m + 1L
        if (n and m == 0L) {
            r = (r and m) + min
        } else if (n > 0L) {
            var u = r ushr 1
            r = u % n
            while (u + m - r < 0L) {
                u = nextLong() ushr 1
                r = u % n
            }
            r += min
        } else {
            while (r < min || r > max) {
                r = nextLong()
            }
        }
        return r
    }

    /**
     * Returns a value in range `[Long.MIN_VALUE]..[Long.MAX_VALUE]`
     * @return a value in range `[Long.MIN_VALUE]..[Long.MAX_VALUE]`
     */
    fun nextLong(): Long

    /**
     * Returns a value between [min] (inclusive) and [max] (exclusive)
     * @return a value between [min] (inclusive) and [max] (exclusive)
     */
    fun nextFloat(min: Float = 0.0f,
                  max: Float = 1.0f) = nextFloat() * (max - min) + min

    /**
     * Returns a value between `0.0f` (inclusive) and `1.0f` (exclusive)
     * @return a value between `0.0f` (inclusive) and `1.0f` (exclusive)
     */
    fun nextFloat(): Float

    /**
     * Returns a value between [min] (inclusive) and [max] (exclusive)
     * @return a value between [min] (inclusive) and [max] (exclusive)
     */
    fun nextDouble(min: Double = 0.0,
                   max: Double = 1.0) = nextFloat() * (max - min) + min

    /**
     * Returns a value between `0.0` (inclusive) and `1.0` (exclusive)
     * @return a value between `0.0` (inclusive) and `1.0` (exclusive)
     */
    fun nextDouble(): Double
}

/**
 * Create a new pseudo random number generator with a random seed
 *
 * **Note:** The implementation will probably be not be thread-safe nor fit for
 * security related tasks
 * @return a new instance of [Random]
 */
expect fun Random(): Random

/**
 * Create a new pseudo random number generator with the given random seed
 *
 * Calling the same sequence of operations on the same seed shall return
 * identical values (besides floating point accuracy) across platforms
 *
 * Reference implementation is based on the `java.lang.Random` class in the
 * Java Standard Library and describes how the values shall be generated
 *
 * **Note:** The implementation will probably be not be thread-safe nor fit for
 * security related tasks
 * @return a new instance of [Random]
 */
expect fun Random(seed: Long): Random

/**
 * Give a pseudo random number generator to use in the current thread
 *
 * As the implementation may not be thread-safe passing the returned object
 * to a different thread is forbidden
 *
 * **Note:** The implementation may not be thread-safe nor fit for security
 * related tasks
 * @return a new instance of [Random]
 */
expect fun threadLocalRandom(): Random

/**
 * Returns a value in range `[Byte.MIN_VALUE]..[Byte.MAX_VALUE]`
 * @return a value in range `[Byte.MIN_VALUE]..[Byte.MAX_VALUE]`
 */
fun Random.nextByte() = nextInt(256).toByte()

/**
 * Returns a value in range `[Short.MIN_VALUE]..[Short.MAX_VALUE]`
 * @return a value in range `[Short.MIN_VALUE]..[Short.MAX_VALUE]`
 */
fun Random.nextShort() = nextInt(65536).toShort()

/**
 * Fills the given array with random byte values
 * @param array the array to fill
 * @param offset first index to fill
 * @param length how many values to insert
 */
fun Random.nextBytes(array: ByteArray,
                     offset: Int = 0,
                     length: Int = array.size - offset): ByteArray {
    var i = 0
    while (i < array.size) {
        var r = nextInt()
        repeat(min(array.size - i, 4)) {
            array[i++] = r.toByte()
            r = r shr 4
        }
    }
    return array
}
