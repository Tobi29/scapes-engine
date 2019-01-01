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

package org.tobi29.utils

import org.tobi29.stdex.InlineUtility

/**
 * Unbiased either type
 */
sealed class Either<out L, out R>

/**
 * Left version of [Either]
 */
data class EitherLeft<out T>(val value: T) : Either<T, Nothing>()

/**
 * Right version of [Either]
 */
data class EitherRight<out T>(val value: T) : Either<Nothing, T>()

/**
 * Returns the value, using a common type between two sides
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <L, R, T> Either<L, R>.get(): T where L : T, R : T = when (this) {
    is EitherLeft<L> -> this.get()
    is EitherRight<R> -> this.get()
}

/**
 * Returns the value
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <T> EitherLeft<T>.get(): T = value

/**
 * Returns the value
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <T> EitherRight<T>.get(): T = value

/**
 * Option type, left for value, right for no value
 */
typealias Option<T> = Either<T, Nothing?>

/**
 * Option type with value
 */
typealias OptionSome<T> = EitherLeft<T>

/**
 * Option type without value
 */
typealias OptionNone = EitherRight<Nothing?>

/**
 * Shared instance for [OptionNone]
 */
inline val nil: OptionNone get() = Nil.nil

@PublishedApi
internal object Nil {
    val nil = EitherRight(null)
}

/**
 * Result type, left for value, right for error
 *
 * **Note:** `E` should always be a subclass of [Throwable]
 */
typealias Result<T, E> = Either<T, E>

/**
 * Result type with value
 */
typealias ResultOk<T> = EitherLeft<T>

/**
 * Result type with error
 *
 * **Note:** `E` should always be a subclass of [Throwable]
 */
typealias ResultError<E> = EitherRight<E>

/**
 * Unwrap the result, throwing the error if applicable
 * @throws E If the result was an error
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <T, E : Throwable> Result<T, E>.unwrap(): T = when (this) {
    is ResultOk<T> -> this.unwrap()
    is ResultError<E> -> this.unwrap()
}

/**
 * Unwrap the result
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <T> ResultOk<T>.unwrap(): T = get()

/**
 * Throw the error
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <E : Throwable> ResultError<E>.unwrap(): Nothing = throw get()

/**
 * Unwrap the result, returning `null` in case it was an error
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <T, E> Result<T, E>.tryUnwrap(): T? = when (this) {
    is ResultOk<T> -> this.tryUnwrap()
    is ResultError<E> -> this.tryUnwrap()
}

/**
 * Unwrap the result
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <T> ResultOk<T>.tryUnwrap(): T = get()

/**
 * Returns `null`
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE", "unused")
inline fun <E> ResultError<E>.tryUnwrap(): Nothing? = null

/**
 * Either returns the value of the result or calls [error]
 * @param error Called with the error in case of an error result
 * @return The value of the result or the return value of [error]
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <T : R, E, R> Result<T, E>.unwrapOr(error: (E) -> R): R =
    when (this) {
        is ResultOk<T> -> this.get()
        is ResultError<E> -> error(this.get())
    }

/**
 * Run the given [block] and return normally or catch an exception of type [E]
 * and wrap into a [Result]
 * @param block The code to run safely
 * @return [ResultOk] with return value of [block] on success, [ResultError] with caught exception otherwise
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <R, reified E : Throwable> tryWrap(block: () -> R): Result<R, E> =
    try {
        EitherLeft(block()) // FIXME: Compiler bug
    } catch (e: Throwable) {
        if (e !is E) throw e
        EitherRight(e) // FIXME: Compiler bug
    }
