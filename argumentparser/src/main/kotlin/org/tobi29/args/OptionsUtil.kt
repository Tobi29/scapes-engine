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

package org.tobi29.args

import org.tobi29.stdex.InlineUtility
import org.tobi29.stdex.Throws

/**
 * Fetches the list of arguments from the given [CommandLine]
 * @return A list of strings or `null` if the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CommandLine.getList(option: CommandOption): List<String>? =
    parameters[option]?.lastOrNull()

/**
 * Fetches the first argument for the option from the given [CommandLine]
 * @return A string or `null` if the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CommandLine.get(option: CommandOption): String? =
    getList(option)?.firstOrNull()

/**
 * Checks if the option is set in the given [CommandLine]
 * @return `true` in case the option was set and either to "true" or nothing
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CommandLine.getBoolean(option: CommandOption): Boolean {
    val value = getList(option) ?: return false
    return (value.firstOrNull() ?: "true") == "true"
}

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to an int
 * @return An int or `null` if the option was not found
 * @throws InvalidOptionArgumentException If a bad value was encountered
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
@Throws(InvalidOptionArgumentException::class)
inline fun CommandLine.getInt(option: CommandOption) =
    get(option)?.let {
        it.toIntOrNull() ?: throw InvalidOptionArgumentException(
            null, this, option, listOf(it)
        )
    }

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a long
 * @return A long or `null` if the option was not found
 * @throws InvalidOptionArgumentException If a bad value was encountered
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
@Throws(InvalidOptionArgumentException::class)
inline fun CommandLine.getLong(option: CommandOption) =
    get(option)?.let {
        it.toLongOrNull() ?: throw InvalidOptionArgumentException(
            null, this, option, listOf(it)
        )
    }

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a double
 * @return A double or `null` if the option was not found
 * @throws InvalidOptionArgumentException If a bad value was encountered
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
@Throws(InvalidOptionArgumentException::class)
inline fun CommandLine.getDouble(option: CommandOption) =
    get(option)?.let {
        it.toDoubleOrNull() ?: throw InvalidOptionArgumentException(
            null, this, option, listOf(it)
        )
    }

/**
 * Fetches the first argument for the option from the given [CommandLine]
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
@Throws(MissingOptionException::class)
inline fun CommandLine.require(option: CommandOption): String =
    require(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * calls [block] with it before returning, allowing computing default values
 * @param block Called right after retrieving the value
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
@Throws(MissingOptionException::class)
inline fun <R> CommandLine.require(
    option: CommandOption,
    block: (String?) -> R?
): R = block(get(option))
        ?: throw MissingOptionException(null, this, option)

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to an int
 * @throws InvalidOptionArgumentException If a bad value was encountered
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
@Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun CommandLine.requireInt(option: CommandOption): Int =
    requireInt(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine],
 * converts it to an int and calls [block] with it before returning, allowing
 * computing default values
 * @param block Called right after retrieving the value
 * @throws InvalidOptionArgumentException If a bad value was encountered
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
@Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun <R> CommandLine.requireInt(
    option: CommandOption,
    block: (Int?) -> R?
): R = block(getInt(option))
        ?: throw MissingOptionException(null, this, option)

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a long
 * @throws InvalidOptionArgumentException If a bad value was encountered
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
@Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun CommandLine.requireLong(option: CommandOption): Long =
    requireLong(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine],
 * converts it to a long and calls [block] with it before returning, allowing
 * computing default values
 * @param block Called right after retrieving the value
 * @throws InvalidOptionArgumentException If a bad value was encountered
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
@Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun <R> CommandLine.requireLong(
    option: CommandOption,
    block: (Long?) -> R?
): R = block(getLong(option))
        ?: throw MissingOptionException(null, this, option)

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a double
 * @throws InvalidOptionArgumentException If a bad value was encountered
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
@Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun CommandLine.requireDouble(option: CommandOption): Double =
    requireDouble(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine],
 * converts it to a double and calls [block] with it before returning, allowing
 * computing default values
 * @param block Called right after retrieving the value
 * @throws InvalidOptionArgumentException If a bad value was encountered
 * @throws MissingOptionException If the option was not found
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
@Throws(InvalidOptionArgumentException::class, MissingOptionException::class)
inline fun <R> CommandLine.requireDouble(
    option: CommandOption,
    block: (Double?) -> R?
): R = block(getDouble(option))
        ?: throw MissingOptionException(null, this, option)
