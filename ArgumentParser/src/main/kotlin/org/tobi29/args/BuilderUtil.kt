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

package org.tobi29.args

typealias CommandConfigBuilder = MutableCollection<CommandElement>

inline fun CommandConfigBuilder(): CommandConfigBuilder = HashSet()

/**
 * Add a new element
 * @param element The element to add
 */
inline fun <E : CommandElement> CommandConfigBuilder.commandElement(
    element: E
): E = element.also { add(it) }

/**
 * Adds a new option
 * @param shortNames Set of characters used as short names
 * @param longNames Set of strings used as long names
 * @param args The names of arguments this option requires
 * @param description Description used for printing usage
 * @param abortParse Abort parsing of all further tokens when this matches
 */
inline fun CommandConfigBuilder.commandOption(
    shortNames: Set<Char>,
    longNames: Set<String>,
    args: List<String>,
    description: String,
    abortParse: Boolean = false
): CommandOption = commandElement(
    CommandOption(
        shortNames, longNames, args, description,
        abortParse
    )
)

/**
 * Adds a new flag
 * @param shortNames Set of characters used as short names
 * @param longNames Set of strings used as long names
 * @param description Description used for printing usage
 * @param abortParse Abort parsing of all further tokens when this matches
 */
inline fun CommandConfigBuilder.commandFlag(
    shortNames: Set<Char>,
    longNames: Set<String>,
    description: String,
    abortParse: Boolean = false
): CommandOption = commandElement(
    CommandFlag(
        shortNames, longNames, description,
        abortParse
    )
)

/**
 * Adds a new argument
 * @param name Name of the argument
 * @param count Range of valid number of arguments
 */
inline fun CommandConfigBuilder.commandArgument(
    name: String,
    count: IntRange = 0..1,
    abortParse: Boolean = false
): CommandArgument = commandElement(
    CommandArgument(
        name, count,
        abortParse
    )
)
