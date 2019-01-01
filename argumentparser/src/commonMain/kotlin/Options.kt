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

import org.tobi29.arrays.Array2
import org.tobi29.stdex.InlineUtility
import org.tobi29.utils.formatTable

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
     * Set of strings used as long names
     */
    val longNames: Set<String>,
    /**
     * The names of arguments this option requires
     */
    val args: List<String>,
    /**
     * Description used for printing usage
     */
    val description: String,
    /**
     * Abort parsing of all further tokens when this matches
     */
    val abortParse: Boolean = false
) : CommandElement() {
    /**
     * Name retrieved through first long name or if none found first short name
     * useful for displaying the option to the user
     */
    val simpleName = longNames.firstOrNull()
            ?: shortNames.firstOrNull()?.toString() ?: "???"
}

/**
 * Creates a new flag
 * @param shortNames Set of characters used as short names
 * @param longNames Set of strings used as long names
 * @param description Description used for printing usage
 * @param abortParse Abort parsing of all further tokens when this matches
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun CommandFlag(
    shortNames: Set<Char>,
    longNames: Set<String>,
    description: String,
    abortParse: Boolean = false
): CommandOption = CommandOption(
    shortNames, longNames, emptyList(), description, abortParse
)

/**
 * A subcommand for separating command line arguments
 */
data class CommandConfig(
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
 * An argument for otherwise unmatched tokens
 */
data class CommandArgument(
    /**
     * Name of the argument
     */
    val name: String,
    /**
     * Range of valid number of arguments
     */
    val count: IntRange = 0..1,
    /**
     * Abort parsing of all further tokens when this matches
     */
    val abortParse: Boolean = false
) : CommandElement()

/**
 * Checks if the given name matches one of the short names
 * @param name The name to check
 * @return `true` if the name matches
 */
fun CommandOption.matches(name: Char) = name in shortNames

/**
 * Checks if the given name matches one of the long names
 * @param name The name to check
 * @return `true` if the name matches
 */
fun CommandOption.matches(name: String) = name in longNames

/**
 * Checks if the given option is a flag
 */
inline val CommandOption.isFlag: Boolean get() = args.isEmpty()

/**
 * Generate a usage text for the given options
 */
fun Iterable<CommandConfig>.printUsage() =
    StringBuilder().also { printUsage(it) }.toString()

/**
 * Generate a usage text for the given options
 * @param appendable The appendable to write to
 */
fun Iterable<CommandConfig>.printUsage(appendable: Appendable) {
    val elements = flatMap { it.elements }

    var first = true
    for (config in this) {
        if (first) {
            first = false
        } else {
            appendable.append(' ')
        }
        appendable.append(config.name)
    }
    elements.forEach { element ->
        if (element !is CommandArgument) return@forEach
        if (element.count.start == 0) {
            appendable.append(" [").append(element.name).append(']')
        } else repeat(element.count.start) {
            appendable.append(' ').append(element.name)
        }
        if (element.count.endInclusive > element.count.start)
            appendable.append("...")
    }
}

/**
 * Generate a help text for the given options with descriptions aligned
 * when using a monospace font
 */
fun Iterable<CommandConfig>.printHelp() =
    StringBuilder().also { printHelp(it) }.toString()

/**
 * Generate a help text for the given options with descriptions aligned
 * when using a monospace font
 * @param appendable The appendable to write to
 */
fun Iterable<CommandConfig>.printHelp(appendable: Appendable) {
    val elements = flatMap { it.elements }

    appendable.append("Usage:\n    ")
    printUsage(appendable)
    appendable.append('\n')

    elements.optionsTable().takeIf { it.height > 0 }?.let {
        appendable.append("\nOptions:\n")
        it.formatTable(appendable, delimiter = "    ", prefix = "    ")
    }

    elements.flagsTable().takeIf { it.height > 0 }?.let {
        appendable.append("\nFlags:\n")
        it.formatTable(appendable, delimiter = "    ", prefix = "    ")
    }
}

/**
 * Generate a usage text for the given option from its parameters
 * @return The usage info in a string
 */
fun CommandOption.printUsage() =
    StringBuilder().also { printUsage(it) }.toString()

/**
 * Generate a usage text for the given option from its parameters
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

private fun Iterable<CommandElement>.optionsTable() =
    mapNotNull { (it as? CommandOption)?.takeIf { !it.isFlag } }.table()

private fun Iterable<CommandElement>.flagsTable() =
    mapNotNull { (it as? CommandOption)?.takeIf { it.isFlag } }.table()

private fun List<CommandOption>.table(): Array2<String> {
    return Array2(2, size) { x, y ->
        val option = this[y]
        when (x) {
            0 -> option.printUsage()
            1 -> option.description
            else -> error("Impossible")
        }
    }
}
