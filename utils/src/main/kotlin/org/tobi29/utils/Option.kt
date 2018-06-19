/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.utils

/**
 * Unbiased either type
 * @param L Left type
 * @param R Right Type
 */
sealed class Either<out L, out R>

/**
 * Left version of [Either]
 * @param T Value type
 */
data class EitherLeft<out T>(val value: T) : Either<T, Nothing>()

/**
 * Right version of [Either]
 * @param T Value type
 */
data class EitherRight<out T>(val value: T) : Either<Nothing, T>()

/**
 * Returns the value, using a common type between two sides
 * @param L Left type
 * @param R Right type
 * @param T Common type
 * @receiver The [Either] to resolve
 * @return Either value
 */
inline fun <L, R, T> Either<L, R>.get(): T where L : T, R : T = when (this) {
    is EitherLeft<L> -> this.get()
    is EitherRight<R> -> this.get()
}

/**
 * Returns the value
 * @param T Value type
 * @receiver The [Either] to resolve
 * @return The value
 */
inline fun <T> EitherLeft<T>.get(): T = value

/**
 * Returns the value
 * @param T Value type
 * @receiver The [Either] to resolve
 * @return The value
 */
inline fun <T> EitherRight<T>.get(): T = value

        /**
         * Option type, left for value, right for no value
         * @param T Optional value type
         */
typealias Option<T> = Either<T, Nothing?>

        /**
         * Option type with value
         * @param T Value type
         */
typealias OptionSome<T> = EitherLeft<T>

        /**
         * Option type without value
         */
typealias OptionNone = EitherRight<Nothing?>

/**
 * Shared instance for [OptionNone]
 */
val nil: OptionNone = EitherRight(null)

        /**
         * Result type, left for value, right for error
         *
         * **Note:** `E` should always be a subclass of [Throwable]
         * @param T Optional value type
         * @param E Error type
         */
typealias Result<T, E> = Either<T, E>

        /**
         * Result type with value
         * @param T Value type
         */
typealias ResultOk<T> = EitherLeft<T>

        /**
         * Result type with error
         *
         * **Note:** `E` should always be a subclass of [Throwable]
         * @param E Error type
         */
typealias ResultError<E> = EitherRight<E>

/**
 * Unwrap the result, throwing the error if applicable
 * @param T Value type
 * @param E Error type
 * @receiver Result to unwrap
 * @throws E If the result was an error
 * @return The value of the result
 */
inline fun <T, E : Throwable> Result<T, E>.unwrap(): T = when (this) {
    is ResultOk<T> -> this.unwrap()
    is ResultError<E> -> this.unwrap()
}

/**
 * Unwrap the result
 * @param T Value type
 * @receiver Result to unwrap
 * @return The value of the result
 */
inline fun <T> ResultOk<T>.unwrap(): T = get()

/**
 * Throw the error
 * @param E Error type
 * @receiver Result to unwrap
 * @return Nothing
 */
inline fun <E : Throwable> ResultError<E>.unwrap(): Nothing = throw get()

/**
 * Unwrap the result, returning `null` in case it was an error
 * @param T Value type
 * @param E Error type
 * @receiver Result to unwrap
 * @return The value of the result or `null` in case it was an error
 */
inline fun <T, E> Result<T, E>.tryUnwrap(): T? = when (this) {
    is ResultOk<T> -> this.tryUnwrap()
    is ResultError<E> -> this.tryUnwrap()
}

/**
 * Unwrap the result
 * @param T Value type
 * @receiver Result to unwrap
 * @return The value of the result
 */
inline fun <T> ResultOk<T>.tryUnwrap(): T = get()

/**
 * Returns `null`
 * @param E Error type
 * @receiver Result to unwrap
 * @return `null`
 */
@Suppress("unused")
inline fun <E> ResultError<E>.tryUnwrap(): Nothing? = null

inline fun <T : R, E, R> Result<T, E>.unwrapOr(error: (E) -> R): R =
    when (this) {
        is ResultOk<T> -> this.get()
        is ResultError<E> -> error(this.get())
    }

/**
 * Run the given [block] and return normally or catch an exception of type [E]
 * and wrap into a [Result]
 * @param block The code to run safely
 * @param R Return value type
 * @param E Error type
 * @return [ResultOk] with return value of [block] on success, [ResultError] with caught exception otherwise
 */
inline fun <R, reified E : Throwable> tryWrap(block: () -> R): Result<R, E> =
    try {
        EitherLeft(block()) // FIXME: Compiler bug
    } catch (e: Throwable) {
        if (e !is E) throw e
        EitherRight(e) // FIXME: Compiler bug
    }
