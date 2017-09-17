package org.tobi29.scapes.engine.utils

/**
 * Copy data from the [src] array to [dest]
 * @param src The array to copy from
 * @param dest The array to copy to
 * @param length The amount of elements to copy
 * @param offsetSrc Offset in the source array
 * @param offsetDest Offset in the destination array
 */
header fun <T> copyArray(src: Array<out T>,
                         dest: Array<in T>,
                         length: Int,
                         offsetSrc: Int,
                         offsetDest: Int)

/**
 * Copy data from the [src] array to [dest]
 * @param src The array to copy from
 * @param dest The array to copy to
 * @param length The amount of elements to copy
 * @param offsetSrc Offset in the source array
 * @param offsetDest Offset in the destination array
 */
header fun copyArray(src: BooleanArray,
                     dest: BooleanArray,
                     length: Int,
                     offsetSrc: Int,
                     offsetDest: Int)

/**
 * Copy data from the [src] array to [dest]
 * @param src The array to copy from
 * @param dest The array to copy to
 * @param length The amount of elements to copy
 * @param offsetSrc Offset in the source array
 * @param offsetDest Offset in the destination array
 */
header fun copyArray(src: ByteArray,
                     dest: ByteArray,
                     length: Int,
                     offsetSrc: Int,
                     offsetDest: Int)

/**
 * Copy data from the [src] array to [dest]
 * @param src The array to copy from
 * @param dest The array to copy to
 * @param length The amount of elements to copy
 * @param offsetSrc Offset in the source array
 * @param offsetDest Offset in the destination array
 */
header fun copyArray(src: ShortArray,
                     dest: ShortArray,
                     length: Int,
                     offsetSrc: Int,
                     offsetDest: Int)

/**
 * Copy data from the [src] array to [dest]
 * @param src The array to copy from
 * @param dest The array to copy to
 * @param length The amount of elements to copy
 * @param offsetSrc Offset in the source array
 * @param offsetDest Offset in the destination array
 */
header fun copyArray(src: IntArray,
                     dest: IntArray,
                     length: Int,
                     offsetSrc: Int,
                     offsetDest: Int)

/**
 * Copy data from the [src] array to [dest]
 * @param src The array to copy from
 * @param dest The array to copy to
 * @param length The amount of elements to copy
 * @param offsetSrc Offset in the source array
 * @param offsetDest Offset in the destination array
 */
header fun copyArray(src: LongArray,
                     dest: LongArray,
                     length: Int,
                     offsetSrc: Int,
                     offsetDest: Int)

/**
 * Copy data from the [src] array to [dest]
 * @param src The array to copy from
 * @param dest The array to copy to
 * @param length The amount of elements to copy
 * @param offsetSrc Offset in the source array
 * @param offsetDest Offset in the destination array
 */
header fun copyArray(src: FloatArray,
                     dest: FloatArray,
                     length: Int,
                     offsetSrc: Int,
                     offsetDest: Int)

/**
 * Copy data from the [src] array to [dest]
 * @param src The array to copy from
 * @param dest The array to copy to
 * @param length The amount of elements to copy
 * @param offsetSrc Offset in the source array
 * @param offsetDest Offset in the destination array
 */
header fun copyArray(src: DoubleArray,
                     dest: DoubleArray,
                     length: Int,
                     offsetSrc: Int,
                     offsetDest: Int)

/**
 * Copy data from the [src] array to [dest]
 * @param src The array to copy from
 * @param dest The array to copy to
 * @param length The amount of elements to copy
 * @param offsetSrc Offset in the source array
 * @param offsetDest Offset in the destination array
 */
header fun copyArray(src: CharArray,
                     dest: CharArray,
                     length: Int,
                     offsetSrc: Int,
                     offsetDest: Int)
