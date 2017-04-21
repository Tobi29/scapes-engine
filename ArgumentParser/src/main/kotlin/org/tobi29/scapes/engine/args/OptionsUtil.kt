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

package org.tobi29.scapes.engine.args

import org.tobi29.scapes.engine.utils.math.vector.Vector2d
import org.tobi29.scapes.engine.utils.math.vector.Vector2i
import org.tobi29.scapes.engine.utils.math.vector.Vector3d
import org.tobi29.scapes.engine.utils.math.vector.Vector3i

/**
 * Fetches the list of arguments from the given [CommandLine]
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @returns A list of strings or `null` if the option was not found
 */
fun CommandLine.getList(option: CommandOption) =
        parameters[option]

/**
 * Fetches the first argument for the option from the given [CommandLine]
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @returns A string or `null` if the option was not found
 */
fun CommandLine.get(option: CommandOption) =
        getList(option)?.firstOrNull()

/**
 * Checks if the option is set in the given [CommandLine]
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @returns True in case the option was set and either to "true" or nothing
 */
fun CommandLine.getBoolean(option: CommandOption): Boolean {
    val value = get(option) ?: return false
    return (value.firstOrNull() ?: "true") == "true"
}

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to an int
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @returns An int or `null` if the option was not found
 */
fun CommandLine.getInt(option: CommandOption) =
        get(option)?.toIntOrNull()

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a long
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @returns A long or `null` if the option was not found
 */
fun CommandLine.getLong(option: CommandOption) =
        get(option)?.toLongOrNull()

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a double
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @returns A double or `null` if the option was not found
 */
fun CommandLine.getDouble(option: CommandOption) =
        get(option)?.toDoubleOrNull()

/**
 * Fetches the first argument for the option from the given [CommandLine]
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @returns A string
 * @throws InvalidCommandLineException If the option was not found
 */
fun CommandLine.require(option: CommandOption) =
        require(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * calls [block] with it before returning, allowing computing default values
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @param block Called right after retrieving the value
 * @returns A string
 * @throws InvalidCommandLineException If the option was not found
 */
inline fun CommandLine.require(option: CommandOption,
                               block: (String?) -> String?) =
        block(get(option)) ?: throw InvalidCommandLineException(
                "Missing argument: ${option.simpleName}")

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to an int
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @returns An int
 * @throws InvalidCommandLineException If the option was not found
 */
fun CommandLine.requireInt(option: CommandOption) =
        requireInt(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine],
 * converts it to an int and calls [block] with it before returning, allowing
 * computing default values
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @param block Called right after retrieving the value
 * @returns An int
 * @throws InvalidCommandLineException If the option was not found
 */
inline fun CommandLine.requireInt(option: CommandOption,
                                  block: (Int?) -> Int?) =
        block(getInt(option)) ?: throw InvalidCommandLineException(
                "Missing argument: ${option.simpleName}")

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a long
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @returns A long
 * @throws InvalidCommandLineException If the option was not found
 */
fun CommandLine.requireLong(option: CommandOption) =
        requireLong(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine],
 * converts it to a long and calls [block] with it before returning, allowing
 * computing default values
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @param block Called right after retrieving the value
 * @returns A long
 * @throws InvalidCommandLineException If the option was not found
 */
inline fun CommandLine.requireLong(option: CommandOption,
                                   block: (Long?) -> Long?) =
        block(getLong(option)) ?: throw InvalidCommandLineException(
                "Missing argument: ${option.simpleName}")

/**
 * Fetches the first argument for the option from the given [CommandLine] and
 * converts it to a double
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @returns A double
 * @throws InvalidCommandLineException If the option was not found
 */
fun CommandLine.requireDouble(option: CommandOption) =
        requireDouble(option) { it }

/**
 * Fetches the first argument for the option from the given [CommandLine],
 * converts it to a double and calls [block] with it before returning, allowing
 * computing default values
 * @receiver The [CommandLine] to read
 * @param option The [CommandOption] to look for
 * @param block Called right after retrieving the value
 * @returns A double
 * @throws InvalidCommandLineException If the option was not found
 */
inline fun CommandLine.requireDouble(option: CommandOption,
                                     block: (Double?) -> Double?) =
        block(getDouble(option)) ?: throw InvalidCommandLineException(
                "Missing argument: ${option.simpleName}")

/**
 * Reads a 2 dimensional vector from the given strings
 * @param values The string to read from
 * @returns A vector containing the values from the strings
 * @throws InvalidCommandLineException If there were not 2 strings or invalid numbers
 */
fun getVector2d(values: List<String>): Vector2d {
    if (values.size != 2) {
        throw InvalidCommandLineException(
                "Unable to parse vector2d: ${values.joinToString(
                        separator = " ")}")
    }
    try {
        return Vector2d(values[0].toDouble(), values[1].toDouble())
    } catch (e: NumberFormatException) {
        throw InvalidCommandLineException(
                "Unable to parse vector2d: ${values.joinToString(
                        separator = " ")}")
    }
}

/**
 * Reads a 3 dimensional vector from the given strings
 * @param values The string to read from
 * @returns A vector containing the values from the strings
 * @throws InvalidCommandLineException If there were not 3 strings or invalid numbers
 */
fun getVector3d(values: List<String>): Vector3d {
    if (values.size != 3) {
        throw InvalidCommandLineException(
                "Unable to parse vector3d: ${values.joinToString(
                        separator = " ")}")
    }
    try {
        return Vector3d(values[0].toDouble(), values[1].toDouble(),
                values[2].toDouble())
    } catch (e: NumberFormatException) {
        throw InvalidCommandLineException(
                "Unable to parse vector3d: ${values.joinToString(
                        separator = " ")}")
    }
}

/**
 * Reads a 2 dimensional vector from the given strings
 * @param values The string to read from
 * @returns A vector containing the values from the strings
 * @throws InvalidCommandLineException If there were not 2 strings or invalid numbers
 */
fun getVector2i(values: List<String>): Vector2i {
    if (values.size != 2) {
        throw InvalidCommandLineException(
                "Unable to parse vector2i: ${values.joinToString(
                        separator = " ")}")
    }
    try {
        return Vector2i(values[0].toInt(), values[1].toInt())
    } catch (e: NumberFormatException) {
        throw InvalidCommandLineException(
                "Unable to parse vector2i: ${values.joinToString(
                        separator = " ")}")
    }
}

/**
 * Reads a 3 dimensional vector from the given strings
 * @param values The string to read from
 * @returns A vector containing the values from the strings
 * @throws InvalidCommandLineException If there were not 3 strings or invalid numbers
 */
fun getVector3i(values: List<String>): Vector3i {
    if (values.size != 3) {
        throw InvalidCommandLineException(
                "Unable to parse vector3i: ${values.joinToString(
                        separator = " ")}")
    }
    try {
        return Vector3i(values[0].toInt(), values[1].toInt(),
                values[2].toInt())
    } catch (e: NumberFormatException) {
        throw InvalidCommandLineException(
                "Unable to parse vector3i: ${values.joinToString(
                        separator = " ")}")
    }
}
