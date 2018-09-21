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
 * @return A token or `null` if the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CommandLine.getToken(option: CommandOption): TokenParser.Token.Parameter? =
    parameterTokens[option]?.lastOrNull()

/**
 * Fetches the token from the given [CommandLine]
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingOptionException::class)
inline fun CommandLine.requireToken(option: CommandOption): TokenParser.Token.Parameter =
    getToken(option)
            ?: throw MissingOptionException(null, this, option)

// Full list

/**
 * Fetches the first option for the option from the given [CommandLine]
 * @return A string or `null` if the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CommandLine.getList(option: CommandOption): List<String>? =
    getToken(option)?.value

/**
 * Fetches the first option for the option from the given [CommandLine]
 * @param block Called with option and token if an option was found
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingOptionException::class)
inline fun <R> CommandLine.getListSafe(
    option: CommandOption,
    block: (List<String>?) -> R?
): R? = getToken(option)?.let { token ->
    try {
        block(token.value)
    } catch (e: IllegalArgumentException) {
        throw InvalidOptionArgumentException(null, this, token, e.message)
    }
}

/**
 * Fetches the first option for the option from the given [CommandLine]
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingOptionException::class)
inline fun CommandLine.requireList(option: CommandOption): List<String> =
    requireList(option) { it }

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingOptionException::class)
inline fun <R> CommandLine.requireList(
    option: CommandOption,
    block: (List<String>) -> R
): R = block(
    getList(option)
            ?: throw MissingOptionException(null, this, option)
)

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingOptionException::class)
inline fun <R : Any> CommandLine.requireListOrNull(
    option: CommandOption,
    block: (List<String>?) -> R?
): R = block(getList(option))
        ?: throw MissingOptionException(null, this, option)

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws InvalidOptionArgumentException If [block] threw [IllegalArgumentException]
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun <R> CommandLine.requireListSafe(
    option: CommandOption,
    block: (List<String>) -> R
): R = requireToken(option).let { token ->
    try {
        block(token.value)
    } catch (e: IllegalArgumentException) {
        throw InvalidOptionArgumentException(null, this, token, e.message)
    }
}

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws InvalidOptionArgumentException If [block] threw [IllegalArgumentException]
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun <R : Any> CommandLine.requireListSafeOrNull(
    option: CommandOption,
    block: (List<String>?) -> R?
): R = getToken(option).let { token ->
    try {
        block(token?.value)
    } catch (e: IllegalArgumentException) {
        throw if (token == null) {
            MissingOptionException(null, this, option, e.message)
        } else {
            InvalidOptionArgumentException(null, this, token, e.message)
        }
    }
} ?: throw MissingOptionException(null, this, option)

// First element only

/**
 * Fetches the first option for the option from the given [CommandLine]
 * @return A string or `null` if the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CommandLine.get(option: CommandOption): String? =
    getList(option)?.firstOrNull()

/**
 * Fetches the first option for the option from the given [CommandLine]
 * @param block Called with option and token if an option was found
 * @return Return value of [block] or `null` if the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <R> CommandLine.getWithToken(
    option: CommandOption,
    block: (String, TokenParser.Token.Parameter) -> R
): R? = getToken(option)?.let { token ->
    token.value.firstOrNull()?.let { block(it, token) }
}

/**
 * Fetches the first option for the option from the given [CommandLine]
 * @param block Called with option and token if an option was found
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingOptionException::class)
inline fun <R> CommandLine.getSafe(
    option: CommandOption,
    block: (String?) -> R?
): R? = getWithToken(option) { value, token ->
    try {
        block(value)
    } catch (e: IllegalArgumentException) {
        throw InvalidOptionArgumentException(null, this, token, e.message)
    }
}

/**
 * Fetches the first option for the option from the given [CommandLine]
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingOptionException::class)
inline fun CommandLine.require(option: CommandOption): String =
    require(option) { it }

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingOptionException::class)
inline fun <R> CommandLine.require(
    option: CommandOption,
    block: (String) -> R
): R = requireList(option) { value ->
    block(
        value.firstOrNull() ?: throw MissingOptionException(null, this, option)
    )
}

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(MissingOptionException::class)
inline fun <R : Any> CommandLine.requireOrNull(
    option: CommandOption,
    block: (String?) -> R?
): R = block(get(option))
        ?: throw MissingOptionException(null, this, option)

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws InvalidOptionArgumentException If [block] threw [IllegalArgumentException]
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun <R> CommandLine.requireSafe(
    option: CommandOption,
    block: (String) -> R
): R = requireListSafe(option) { value ->
    block(
        value.firstOrNull() ?: throw MissingOptionException(null, this, option)
    )
}

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * calls [block] with it before returning
 * @param block Called right after retrieving the value
 * @throws InvalidOptionArgumentException If [block] threw [IllegalArgumentException]
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun <R : Any> CommandLine.requireSafeOrNull(
    option: CommandOption,
    block: (String?) -> R?
): R = getToken(option).let { token ->
    try {
        block(token?.value?.firstOrNull())
    } catch (e: IllegalArgumentException) {
        throw if (token == null) {
            MissingOptionException(null, this, option, e.message)
        } else {
            InvalidOptionArgumentException(null, this, token, e.message)
        }
    }
} ?: throw MissingOptionException(null, this, option)

// Conversions for common types

/**
 * Checks if the option is set in the given [CommandLine]
 * @return `true` in case the option was set and either to "true" or nothing
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CommandLine.getBoolean(option: CommandOption): Boolean {
    return (getList(option) ?: return false).firstOrNull() ?: "true" == "true"
}

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * converts it to an int
 * @return An int or `null` if the option was not found
 * @throws InvalidOptionArgumentException If a bad value was encountered
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidOptionArgumentException::class)
inline fun CommandLine.getInt(option: CommandOption) =
    getSafe(option) { it?.toInt() }

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * converts it to a long
 * @return A long or `null` if the option was not found
 * @throws InvalidOptionArgumentException If a bad value was encountered
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidOptionArgumentException::class)
inline fun CommandLine.getLong(option: CommandOption) =
    getSafe(option) { it?.toLong() }

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * converts it to a double
 * @return A double or `null` if the option was not found
 * @throws InvalidOptionArgumentException If a bad value was encountered
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidOptionArgumentException::class)
inline fun CommandLine.getDouble(option: CommandOption) =
    getSafe(option) { it?.toDouble() }

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * converts it to an int
 * @throws InvalidOptionArgumentException If a bad value was encountered
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun CommandLine.requireInt(option: CommandOption): Int =
    requireSafe(option) { it.toInt() }

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * converts it to a long
 * @throws InvalidOptionArgumentException If a bad value was encountered
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun CommandLine.requireLong(option: CommandOption): Long =
    requireSafe(option) { it.toLong() }

/**
 * Fetches the first option for the option from the given [CommandLine] and
 * converts it to a double
 * @throws InvalidOptionArgumentException If a bad value was encountered
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
// FIXME @Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun CommandLine.requireDouble(option: CommandOption): Double =
    requireSafe(option) { it.toDouble() }
