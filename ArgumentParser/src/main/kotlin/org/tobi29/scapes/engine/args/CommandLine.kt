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
        val subcommand: CommandSubcommand? = null,
        /**
         * The arguments in order they appeared in
         */
        val arguments: List<String>,
        /**
         * The string parameters from the options
         */
        val parameters: Map<CommandOption, List<String>>)

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
    val arguments = filterMap<TokenParser.Token.Argument>()
            .map { it.argument }.toList()
    val parameters = filterMap<TokenParser.Token.Parameter>()
            .groupBy { it.option }.asSequence()
            .map { Pair(it.key, it.value.flatMap { it.value }) }.toMap()
    return CommandLine(null, arguments, parameters)
}

/**
 * Validates the given command line and throws in case of errors
 *
 * Currently this just checks if all parameters have at least as many
 * arguments attached at they require
 * @receiver The [CommandLine] to check
 * @throws InvalidCommandLineException An offending entry was found
 */
fun CommandLine.validate() {
    parameters.forEach { (option, arguments) ->
        if (option.args.size > arguments.size) {
            throw InvalidCommandLineException(
                    "Not enough arguments supplied for ${option.simpleName}")
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
fun Iterable<CommandElement>.parseDirtyCommandLine(tokens: Iterable<String>): CommandLine =
        parseTokens(tokens).let { (subcommand, tokens) ->
            tokens.assemble().copy(subcommand = subcommand)
        }

/**
 * Parse the given tokens into a command line instance and validates it
 * @receiver The parser configuration
 * @throws InvalidCommandLineException When a token is invalid or it resulted in an invalid command line
 * @return An assembled valid command line instance
 */
fun Iterable<CommandElement>.parseCommandLine(tokens: Iterable<String>): CommandLine =
        parseDirtyCommandLine(tokens).apply { validate() }
