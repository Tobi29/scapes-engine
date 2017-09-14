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

/**
 * A option for parsing command line arguments
 */
data class CommandOption(
        /**
         * Set of characters used as short names
         */
        val shortNames: Set<Char>,
        /**
         * The amount of arguments this option requires
         */
        val longNames: Set<String>,
        /**
         * The amount of arguments this option requires
         */
        val args: Int,
        /**
         * Description used for printing usage
         */
        val description: String) {
    /**
     * Creates a new option with the given names, no arguments and description
     * @param shortNames Set of characters used as short names
     * @param longNames Set of strings used as long names
     * @param description Description used for printing usage
     */
    constructor(shortNames: Set<Char>,
                longNames: Set<String>,
                description: String
    ) : this(shortNames, longNames, 0, description)

    /**
     * Name retrieved through first long name or if none found first short name
     * useful for displaying the option to the user
     */
    val simpleName = longNames.firstOrNull()
            ?: shortNames.firstOrNull()?.toString() ?: "???"
}

/**
 * Checks if the given name matches one of the short names
 * @receiver The [CommandOption] to check on
 * @param name The name to check
 * @return `true` if the name matches
 */
fun CommandOption.matches(name: Char) = shortNames.contains(name)

/**
 * Checks if the given name matches one of the long names
 * @receiver The [CommandOption] to check on
 * @param name The name to check
 * @return `true` if the name matches
 */
fun CommandOption.matches(name: String) = longNames.contains(name)

/**
 * Generate a help text for the given options with descriptions aligned
 * when using a monospace font
 * @receiver The sequence of [CommandOption]s to read
 * @return The help info in a string
 */
fun Iterable<CommandOption>.printHelp() =
        StringBuilder().also { printHelp(it) }.toString()

/**
 * Generate a help text for the given options with descriptions aligned
 * when using a monospace font
 * @receiver The sequence of [CommandOption]s to read
 * @param appendable The appendable to write to
 */
fun Iterable<CommandOption>.printHelp(appendable: Appendable) {
    val options = map { Pair(it, it.printUsage()) }.toList()
    val descriptionGap = (options.map { it.second.length }.max() ?: 0) + 4
    var first = false
    options.forEach { (option, usage) ->
        if (!first) {
            first = true
        } else {
            appendable.append("\n")
        }

        appendable.append(usage)
        repeat(descriptionGap - usage.length) { appendable.append(' ') }
        appendable.append(option.description)
    }
}

/**
 * Generate a usage text for the given option from its parameters
 * @receiver The [CommandOption] to read
 * @return The usage info in a string
 */
fun CommandOption.printUsage() =
        StringBuilder().also { printUsage(it) }.toString()

/**
 * Generate a usage text for the given option from its parameters
 * @receiver The [CommandOption] to read
 * @param appendable The appendable to write to
 */
fun CommandOption.printUsage(appendable: Appendable) {
    var first = false
    shortNames.forEach {
        if (!first) {
            first = true
        } else {
            appendable.append(", ")
        }

        appendable.append('-').append(it)
    }
    longNames.forEach {
        if (!first) {
            first = true
        } else {
            appendable.append(", ")
        }

        appendable.append("--").append(it)
    }
    if (args > 1) {
        appendable.append(" <arg 1")
        for (i in 2..args) {
            appendable.append(", arg ").append(i.toString())
        }
        appendable.append('>')
    } else if (args == 1) {
        appendable.append(" <arg")
        appendable.append('>')
    }
}
