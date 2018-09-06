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
import org.tobi29.stdex.assert

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
    val parameters: Map<CommandOption, List<List<String>>>,
    /**
     * The arguments in order they appeared in
     */
    val arguments: Map<CommandArgument, List<String>>,
    /**
     * Trail tokens after parsing aborted, `null` if no abort occurred
     */
    val trail: List<String>?
)

/**
 * Returns `true` if the command line resulted from an aborted parse
 * @return `true` or `false`
 */
@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline val CommandLine.isAborted
    get() = trail != null

/**
 * Assembles the given parsed tokens for easy access
 * @receiver The parsed tokens to assemble
 * @return A [CommandLine] instance containing the data from the tokens
 */
fun Iterable<TokenParser.Token>.assemble(command: List<CommandConfig> = emptyList()): CommandLine {
    val parameters = filterIsInstance<TokenParser.Token.Parameter>()
        .groupBy { it.option }.asSequence()
        .map { Pair(it.key, it.value.map { it.value }) }.toMap()
    val arguments = filterIsInstance<TokenParser.Token.Argument>()
        .groupBy { it.argument }.asSequence()
        .map { Pair(it.key, it.value.map { it.value }) }.toMap()
    val trail = if (none {
            when (it) {
                is TokenParser.Token.Parameter -> it.option.abortParse
                is TokenParser.Token.Argument -> it.argument.abortParse
                else -> false
            }
        }) null
    else filterIsInstance<TokenParser.Token.Trail>()
        .map { it.value }.toList()
    return CommandLine(command, parameters, arguments, trail)
}

/**
 * Validates the given tokens and command line and throws in case of errors
 *
 * **Note:** The [CommandLine] must be equal to the result of
 * `tokens.assemble()` or otherwise any [Throwable] might get thrown or
 * an inconsistency might not get found
 * @receiver The [CommandLine] to check
 * @param tokens The sequence of tokens from parsing
 * @throws InvalidCommandLineException An offending entry was found
 */
fun CommandLine.validate(tokens: Iterable<TokenParser.Token>) {
    assert { this == tokens.assemble(command) }

    val parameterTokens = tokens.asSequence()
        .filterIsInstance<TokenParser.Token.Parameter>()
    val argumentTokens = tokens.asSequence()
        .filterIsInstance<TokenParser.Token.Argument>()

    parameters.forEach { (parameter, instances) ->
        instances.forEach { values ->
            if (parameter.args.size < values.size)
                throw ExtraOptionArgumentException(null, this,
                    parameterTokens.first {
                        println("${it.option} $parameter ${it.value} $values")
                        it.option == parameter && it.value == values
                    })
            if (parameter.args.size > values.size)
                throw MissingOptionArgumentException(null, this,
                    parameterTokens.first {
                        it.option == parameter && it.value == values
                    })
        }
    }

    arguments.forEach { (argument, values) ->
        if (argument.count.last < values.size)
            throw ExtraArgumentException(
                null, this,
                argumentTokens.filter {
                    it.argument == argument
                }.drop(argument.count.last).first()
            )
    }

    if (!isAborted) {
        command.forEach { (_, elements) ->
            elements.asSequence().mapNotNull { it as? CommandArgument }
                .forEach { argument ->
                    val count = arguments[argument]?.size ?: 0
                    if (argument.count.start > count) {
                        throw MissingArgumentException(
                            null, this,
                            argument, argument.count.start - count
                        )
                    }
                }
        }
    }
}

/**
 * Parse the given tokens into a command line instance and validates it
 * @receiver The parser configuration
 * @throws InvalidTokensException When a token is invalid or it resulted in an invalid command line
 * @return An assembled valid command line instance
 */
fun CommandConfig.parseCommandLine(args: Iterable<String>): CommandLine =
    parseTokens(args).let { (subcommand, tokens) ->
        tokens.assemble(subcommand).also { commandLine ->
            withTokens(args) { commandLine.validate(tokens) }
        }
    }
