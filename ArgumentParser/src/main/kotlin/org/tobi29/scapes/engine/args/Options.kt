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
 * Command element for parsing command line arguments
 */
sealed class CommandElement

/**
 * An option for parsing command line arguments
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
         * The names of arguments this option requires
         */
        val args: List<String>,
        /**
         * Description used for printing usage
         */
        val description: String) : CommandElement() {
    /**
     * Creates a new option with the given names, no arguments and description
     * @param shortNames Set of characters used as short names
     * @param longNames Set of strings used as long names
     * @param description Description used for printing usage
     */
    constructor(shortNames: Set<Char>,
                longNames: Set<String>,
                description: String
    ) : this(shortNames, longNames, emptyList(), description)

    /**
     * Name retrieved through first long name or if none found first short name
     * useful for displaying the option to the user
     */
    val simpleName = longNames.firstOrNull()
            ?: shortNames.firstOrNull()?.toString() ?: "???"
}

/**
 * A subcommand for separating command line arguments
 */
data class CommandSubcommand(
        /**
         * Name of the subcommand
         */
        val name: String,
        /**
         * Command elements available in the subcommand
         */
        val elements: Iterable<CommandElement>
) : CommandElement()

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
 * @param execName Name of executable
 * @param subcommand Subcommand options to show
 * @return The help info in a string
 */
fun Iterable<CommandElement>.printHelp(execName: String? = null,
                                       subcommand: List<CommandSubcommand> = emptyList()) =
        StringBuilder().also { printHelp(it, execName, subcommand) }.toString()

/**
 * Generate a help text for the given options with descriptions aligned
 * when using a monospace font
 * @receiver The sequence of [CommandOption]s to read
 * @param appendable The appendable to write to
 * @param execName Name of executable
 * @param subcommand Subcommand options to show
 */
fun Iterable<CommandElement>.printHelp(appendable: Appendable,
                                       execName: String? = null,
                                       subcommand: List<CommandSubcommand> = emptyList()) {
    val elements = this + subcommand.flatMap { it.elements }
    val options = elements.mapNotNull {
        (it as? CommandOption)?.let { it to it.printUsage() }
    }
    appendable.append("Usage: ")
    execName?.let { appendable.append(it) }
    appendable.append('\n')
    val descriptionGap = (options.map { it.second.length }.max() ?: 0) + 4
    var first = false
    options.forEach { (option, usage) ->
        if (!first) {
            first = true
        } else {
            appendable.append('\n')
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
    if (args.isNotEmpty()) args.joinTo(appendable, prefix = " <", postfix = ">")
}
