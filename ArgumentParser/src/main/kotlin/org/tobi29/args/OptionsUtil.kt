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

package org.tobi29.args

/**
 * Fetches the list of arguments from the given [CommandLine]
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @return A list of strings or `null` if the option was not found
 */
fun CommandLine.getList(option: CommandOption): List<String>? =
    parameters[option]?.firstOrNull()

/**
 * Fetches the first argument for the option from the given [CommandLine]
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @return A string or `null` if the option was not found
 */
fun CommandLine.get(option: CommandOption): String? =
    getList(option)?.firstOrNull()

/**
 * Checks if the option is set in the given [CommandLine]
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @return True in case the option was set and either to "true" or nothing
 */
fun CommandLine.getBoolean(option: CommandOption): Boolean {
    val value = getList(option) ?: return false
    return (value.firstOrNull() ?: "true") == "true"
}

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to an int
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @return An int or `null` if the option was not found
 */
fun CommandLine.getInt(option: CommandOption) =
    get(option)?.let {
        it.toIntOrNull() ?: throw InvalidOptionArgumentException(
            null, this, option, listOf(it)
        )
    }

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a long
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @return A long or `null` if the option was not found
 */
fun CommandLine.getLong(option: CommandOption) =
    get(option)?.let {
        it.toLongOrNull() ?: throw InvalidOptionArgumentException(
            null, this, option, listOf(it)
        )
    }

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a double
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @return A double or `null` if the option was not found
 */
fun CommandLine.getDouble(option: CommandOption) =
    get(option)?.let {
        it.toDoubleOrNull() ?: throw InvalidOptionArgumentException(
            null, this, option, listOf(it)
        )
    }

/**
 * Fetches the first argument for the option from the given [CommandLine]
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @return A string
 * @throws MissingOptionException If the option was not found
 */
fun CommandLine.require(option: CommandOption) =
    require(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * calls [block] with it before returning, allowing computing default values
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @param block Called right after retrieving the value
 * @return A string
 * @throws MissingOptionException If the option was not found
 */
inline fun <R> CommandLine.require(
    option: CommandOption,
    block: (String?) -> R?
): R =
    block(get(option))
            ?: throw MissingOptionException(null, this, option)

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to an int
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @return An int
 * @throws MissingOptionException If the option was not found
 */
fun CommandLine.requireInt(option: CommandOption): Int =
    requireInt(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine],
 * converts it to an int and calls [block] with it before returning, allowing
 * computing default values
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @param block Called right after retrieving the value
 * @return An int
 * @throws MissingOptionException If the option was not found
 */
inline fun <R> CommandLine.requireInt(
    option: CommandOption,
    block: (Int?) -> R?
): R =
    block(getInt(option))
            ?: throw MissingOptionException(null, this, option)

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a long
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @return A long
 * @throws MissingOptionException If the option was not found
 */
fun CommandLine.requireLong(option: CommandOption): Long =
    requireLong(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine],
 * converts it to a long and calls [block] with it before returning, allowing
 * computing default values
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @param block Called right after retrieving the value
 * @return A long
 * @throws MissingOptionException If the option was not found
 */
inline fun <R> CommandLine.requireLong(
    option: CommandOption,
    block: (Long?) -> R?
): R =
    block(getLong(option))
            ?: throw MissingOptionException(null, this, option)

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a double
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @return A double
 * @throws MissingOptionException If the option was not found
 */
fun CommandLine.requireDouble(option: CommandOption): Double =
    requireDouble(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine],
 * converts it to a double and calls [block] with it before returning, allowing
 * computing default values
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @param block Called right after retrieving the value
 * @return A double
 * @throws MissingOptionException If the option was not found
 */
inline fun <R> CommandLine.requireDouble(
    option: CommandOption,
    block: (Double?) -> R?
): R =
    block(getDouble(option))
            ?: throw MissingOptionException(null, this, option)
