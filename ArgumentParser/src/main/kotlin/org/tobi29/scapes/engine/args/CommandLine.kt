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

import org.tobi29.scapes.engine.utils.filterMap

/**
 * Result from parsing and assembling command line options
 */
data class CommandLine(
        /**
         * The innermost subcommand matched
         */
        val command: List<CommandConfig>,
        /**
         * The string parameters from the options
         */
        val parameters: Map<CommandOption, List<String>>,
        /**
         * The arguments in order they appeared in
         */
        val arguments: Map<CommandArgument, List<String>>)

/**
 * Assembles the given parsed tokens for easy access
 * @receiver The parsed tokens to assemble
 * @return A [CommandLine] instance containing the data from the tokens
 */
fun Collection<TokenParser.Token>.assemble() = asSequence().assemble()

/**
 * Assembles the given parsed tokens for easy access
 * @receiver The parsed tokens to assemble
 * @return A [CommandLine] instance containing the data from the tokens
 */
fun Sequence<TokenParser.Token>.assemble(): CommandLine {
    val parameters = filterMap<TokenParser.Token.Parameter>()
            .groupBy { it.option }.asSequence()
            .map { Pair(it.key, it.value.flatMap { it.value }) }.toMap()
    val arguments = filterMap<TokenParser.Token.Argument>()
            .groupBy { it.argument }.asSequence()
            .map { Pair(it.key, it.value.map { it.value }) }.toMap()
    return CommandLine(emptyList(), parameters, arguments)
}

/**
 * Validates the given command line and throws in case of errors
 *
 * Currently this just checks if all parameters and arguments have at least as
 * many values attached as they require
 * @receiver The [CommandLine] to check
 * @throws InvalidCommandLineException An offending entry was found
 */
fun CommandLine.validate() {
    parameters.forEach { (option, values) ->
        if (option.args.size > values.size) {
            throw InvalidCommandLineException(
                    "Not enough values supplied for ${option.simpleName}")
        }
    }
    arguments.forEach { (argument, values) ->
        if (argument.count.endInclusive < values.size) {
            throw InvalidCommandLineException(
                    "Too many values supplied for ${argument.name}")
        } else if (argument.count.start > values.size) {
            throw InvalidCommandLineException(
                    "Not enough values supplied for ${argument.name}")
        }
    }
    command.forEach { (_, elements) ->
        elements.asSequence().mapNotNull { it as? CommandArgument }.forEach { argument ->
            val count = arguments[argument]?.size ?: 0
            if (argument.count.endInclusive < count) {
                throw InvalidCommandLineException(
                        "Too many values supplied for ${argument.name}")
            } else if (argument.count.start > count) {
                throw InvalidCommandLineException(
                        "Not enough values supplied for ${argument.name}")
            }
        }
    }
}

/**
 * Exception when invalid input was given to command line parsing
 */
class InvalidCommandLineException(message: String) : Exception(message)

/**
 * Parse the given tokens into a command line instance
 * @receiver The parser configuration
 * @throws InvalidCommandLineException When a token is invalid
 * @return An assembled command line instance
 */
fun CommandConfig.parseDirtyCommandLine(tokens: Iterable<String>): CommandLine =
        parseTokens(tokens).let { (subcommand, tokens) ->
            tokens.assemble().copy(command = subcommand)
        }

/**
 * Parse the given tokens into a command line instance and validates it
 * @receiver The parser configuration
 * @throws InvalidCommandLineException When a token is invalid or it resulted in an invalid command line
 * @return An assembled valid command line instance
 */
fun CommandConfig.parseCommandLine(tokens: Iterable<String>): CommandLine =
        parseDirtyCommandLine(tokens).apply { validate() }
