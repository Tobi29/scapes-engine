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

package org.tobi29.args

import org.tobi29.stdex.InlineUtility
import org.tobi29.stdex.Throws

/**
 * Fetches the token from the given [CommandLine]
 * @return A token or `null` if the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CommandLine.getToken(argument: CommandArgument): TokenParser.Token.Argument? =
    argumentTokens[argument]?.firstOrNull()

/**
 * Fetches the token from the given [CommandLine]
 * @throws MissingArgumentException If the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingArgumentException::class)
inline fun CommandLine.requireToken(argument: CommandArgument): TokenParser.Token.Argument =
    getToken(argument)
            ?: throw MissingArgumentException(null, this, argument)

/**
 * Fetches the first argument for the argument from the given [CommandLine]
 * @return A string or `null` if the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CommandLine.get(argument: CommandArgument): String? =
    getToken(argument)?.value

/**
 * Fetches the first argument for the argument from the given [CommandLine]
 * @param block Called with argument and token if an argument was found
 * @throws MissingArgumentException If the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingArgumentException::class)
inline fun <R> CommandLine.getSafe(
    argument: CommandArgument,
    block: (String?) -> R?
): R? = getToken(argument)?.let { token ->
    try {
        block(token.value)
    } catch (e: IllegalArgumentException) {
        throw InvalidArgumentException(null, this, token, e.message)
    }
}

/**
 * Fetches the first argument for the argument from the given [CommandLine]
 * @throws MissingArgumentException If the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingArgumentException::class)
inline fun CommandLine.require(argument: CommandArgument): String =
    get(argument)
            ?: throw MissingArgumentException(null, this, argument)

/**
 * Fetches the first argument for the argument from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws MissingArgumentException If the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingArgumentException::class)
inline fun <R> CommandLine.require(
    argument: CommandArgument,
    block: (String) -> R
): R = block(
    get(argument)
            ?: throw MissingArgumentException(null, this, argument)
)

/**
 * Fetches the first argument for the argument from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws MissingArgumentException If the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingArgumentException::class)
inline fun <R : Any> CommandLine.requireOrNull(
    argument: CommandArgument,
    block: (String?) -> R?
): R = block(get(argument))
        ?: throw MissingArgumentException(null, this, argument)

/**
 * Fetches the first argument for the argument from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws InvalidArgumentException If [block] threw [IllegalArgumentException]
 * @throws MissingArgumentException If the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidArgumentException::class, MissingArgumentException::class)
inline fun <R> CommandLine.requireSafe(
    argument: CommandArgument,
    block: (String) -> R
): R = requireToken(argument).let { token ->
    try {
        block(token.value)
    } catch (e: IllegalArgumentException) {
        throw InvalidArgumentException(null, this, token, e.message)
    }
}

/**
 * Fetches the first argument for the argument from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws InvalidArgumentException If [block] threw [IllegalArgumentException]
 * @throws MissingArgumentException If the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidArgumentException::class, MissingArgumentException::class)
inline fun <R : Any> CommandLine.requireSafeOrNull(
    argument: CommandArgument,
    block: (String?) -> R?
): R = getToken(argument).let { token ->
    try {
        block(token?.value)
    } catch (e: IllegalArgumentException) {
        throw if (token == null) {
            MissingArgumentException(null, this, argument, reason = e.message)
        } else {
            InvalidArgumentException(null, this, token, e.message)
        }
    }
} ?: throw MissingArgumentException(null, this, argument)

// Conversions for common types

/**
 * Checks if the argument is set in the given [CommandLine]
 * @return `true` in case the argument was set and either to "true" or nothing
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CommandLine.getBoolean(argument: CommandArgument): Boolean {
    return (get(argument) ?: return false) == "true"
}

/**
 * Fetches the first argument for the argument from the given [CommandLine] and
 * converts it to an int
 * @return An int or `null` if the argument was not found
 * @throws InvalidArgumentException If a bad value was encountered
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidArgumentException::class)
inline fun CommandLine.getInt(argument: CommandArgument) =
    getSafe(argument) { it?.toInt() }

/**
 * Fetches the first argument for the argument from the given [CommandLine] and
 * converts it to a long
 * @return A long or `null` if the argument was not found
 * @throws InvalidArgumentException If a bad value was encountered
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidArgumentException::class)
inline fun CommandLine.getLong(argument: CommandArgument) =
    getSafe(argument) { it?.toLong() }

/**
 * Fetches the first argument for the argument from the given [CommandLine] and
 * converts it to a double
 * @return A double or `null` if the argument was not found
 * @throws InvalidArgumentException If a bad value was encountered
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidArgumentException::class)
inline fun CommandLine.getDouble(argument: CommandArgument) =
    getSafe(argument) { it?.toDouble() }

/**
 * Fetches the first argument for the argument from the given [CommandLine] and
 * converts it to an int
 * @throws InvalidArgumentException If a bad value was encountered
 * @throws MissingArgumentException If the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidArgumentException::class, MissingArgumentException::class)
inline fun CommandLine.requireInt(argument: CommandArgument): Int =
    requireSafe(argument) { it.toInt() }

/**
 * Fetches the first argument for the argument from the given [CommandLine] and
 * converts it to a long
 * @throws InvalidArgumentException If a bad value was encountered
 * @throws MissingArgumentException If the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidArgumentException::class, MissingArgumentException::class)
inline fun CommandLine.requireLong(argument: CommandArgument): Long =
    requireSafe(argument) { it.toLong() }

/**
 * Fetches the first argument for the argument from the given [CommandLine] and
 * converts it to a double
 * @throws InvalidArgumentException If a bad value was encountered
 * @throws MissingArgumentException If the argument was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidArgumentException::class, MissingArgumentException::class)
inline fun CommandLine.requireDouble(argument: CommandArgument): Double =
    requireSafe(argument) { it.toDouble() }
