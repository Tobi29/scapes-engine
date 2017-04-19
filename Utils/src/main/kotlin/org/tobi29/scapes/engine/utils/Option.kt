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

sealed class Option<T> {
    class Some<T>(val value: T) : Option<T>()
    object Nothing : Option<Nothing>()
}

sealed class Either<out L, out R> {
    class Left<out T>(val value: T) : Either<T, Nothing>()
    class Right<out T>(val value: T) : Either<Nothing, T>()
}

sealed class Result<out T, out E : Throwable> {
    class Ok<out T>(val value: T) : Result<T, Nothing>() {
        override fun get() = value
    }

    class Error<out T : Throwable>(val error: T) : Result<Nothing, T>() {
        override fun get() = throw error
    }

    abstract fun get(): T
}
