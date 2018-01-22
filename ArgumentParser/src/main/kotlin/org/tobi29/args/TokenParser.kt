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

import org.tobi29.stdex.readOnly

/**
 * Parses a stream of tokens into parameters, flags and arguments
 */
class TokenParser(command: CommandConfig) {
    private var optionsTerminated = false
    private var currentOption: CommandOption? = null
    private val currentArgs = ArrayList<String>()
    private val tokensMut = ArrayList<Token>()
    private var subcommands = emptyMap<String, CommandConfig>()
    private val shortOptions = HashMap<Char, CommandOption>()
    private val longOptions = HashMap<String, CommandOption>()
    private val commandMut = ArrayList<CommandConfig>()
    private val arguments = ArrayList<CommandArgument>()
    private var argumentsIndex = 0
    private var argumentCount = 0

    /**
     * The command path
     */
    val command = commandMut.readOnly()

    /**
     * The current list of parsed tokens
     */
    val tokens = tokensMut.readOnly()

    init {
        enterCommand(command)
    }

    /**
     * Finishes the parsing and returns a list of parsed tokens
     * @throws InvalidTokensException The parser finished on an invalid token
     */
    fun finish(): Pair<List<CommandConfig>, List<Token>> {
        currentOption?.let { option ->
            tokensMut.add(Token.Parameter(option, currentArgs.toList()))
            currentArgs.clear()
            currentOption = null
        }

        return command to tokens
    }

    /**
     * Parses the next token
     * @param token The token to parse
     * @throws InvalidTokensException The token is invalid
     */
    fun append(token: String) {
        val length = token.length

        if (length == 0 || optionsTerminated || currentOption != null) {
            // Token: ".*"
            appendArg(token)
            return
        }

        when (token[0]) {
            '-' -> {
                if (length == 1) {
                    // Token: "-"
                    appendArg(token)
                } else when (token[1]) {
                    '-' -> {
                        if (length == 2) {
                            // Token: "--"
                            appendTerminateOptions()
                        } else {
                            // Token: "--.+"
                            appendLong(token.substring(2))
                        }
                    }
                    else -> {
                        // Token: "-.+"
                        appendShort(token.substring(1))
                    }
                }
            }
            else -> {
                // Token: ".+"
                appendArg(token)
            }
        }
    }

    // Handle plain arguments
    private fun appendArg(token: String) {
        currentOption?.let { option ->
            if (currentArgs.size >= option.args.size - 1) {
                tokensMut.add(Token.Parameter(option, currentArgs + token))
                currentArgs.clear()
                currentOption = null
            } else {
                currentArgs.add(token)
            }
            return
        }

        subcommands[token]?.let { subcommand ->
            enterCommand(subcommand)
            return
        }

        if (argumentCount > 0) args@ {
            tokensMut.add(Token.Argument(arguments[argumentsIndex], token))

            argumentCount--
            if (argumentCount <= 0) {
                argumentsIndex++
                if (argumentsIndex >= arguments.size) {
                    return@args
                }
                argumentCount = arguments[argumentsIndex].count.last
            }
            return
        }

        throw StrayArgumentException(null, token)
    }

    // Handle options and flags starting with a short name
    private fun appendShort(token: String) {
        // Token: .=.*
        val equals = token.indexOf('=')
        if (equals >= 0) {
            for (i in 0..equals - 2) {
                val name = token[i]
                (shortOptions[name]
                        ?: throw UnknownOptionException(null, name)).let {
                    if (it.args.isNotEmpty()) {
                        currentOption = it
                    } else {
                        tokensMut.add(Token.Parameter(it, emptyList()))
                        Unit
                    }
                }
            }

            val name = token[equals - 1]
            val argument = token.substring(equals + 1)

            (shortOptions[name]
                    ?: throw UnknownOptionException(null, name)).let {
                tokensMut.add(Token.Parameter(it, listOf(argument)))
            }
            return
        }

        // Token: ..+
        if (token.length > 1) {
            shortOptions[token[0]]?.let { option ->
                if (option.args.size != 1) return@let

                tokensMut.add(
                        Token.Parameter(option, listOf(token.substring(1))))
                return
            }
        }

        // Token: .+
        for (name in token) {
            (shortOptions[name]
                    ?: throw UnknownOptionException(null, name)).let { flag ->
                if (flag.args.isNotEmpty()) {
                    currentOption = flag
                } else {
                    tokensMut.add(Token.Parameter(flag, emptyList()))
                }
            }
        }
    }

    // Handle options and flags starting with a long name
    private fun appendLong(token: String) {
        // Token: .*=.*
        val equals = token.indexOf('=')
        if (equals >= 0) {
            val name = token.substring(0, equals)
            val argument = token.substring(equals + 1)

            (longOptions[name]
                    ?: throw UnknownOptionException(null, name)).let {
                tokensMut.add(Token.Parameter(it, listOf(argument)))
            }
            return
        }

        // Token: .*
        (longOptions[token]
                ?: throw UnknownOptionException(null, token)).let { flag ->
            if (flag.args.isNotEmpty()) {
                currentOption = flag
            } else {
                tokensMut.add(Token.Parameter(flag, emptyList()))
            }
        }
    }

    private fun appendTerminateOptions() {
        optionsTerminated = true
    }

    private fun enterCommand(subcommand: CommandConfig) {
        commandMut.add(subcommand)
        val elements = subcommand.elements
        subcommands = elements.asSequence().mapNotNull {
            (it as? CommandConfig)?.let { it.name to it }
        }.toMap()
        arguments.clear()
        argumentsIndex = 0
        for (element in elements) {
            when (element) {
                is CommandOption -> {
                    element.shortNames.forEach { shortOptions[it] = element }
                    element.longNames.forEach { longOptions[it] = element }
                }
                is CommandArgument -> {
                    if (element.count.endInclusive > 0) arguments.add(element)
                }
            }
        }
        argumentCount = if (arguments.isNotEmpty())
            arguments.first().count.last else 0
    }

    /**
     * A parsed token
     */
    sealed class Token {
        /**
         * An argument not attached to any [CommandOption]
         */
        data class Argument(
                /**
                 * The [CommandArgument] of the argument
                 */
                val argument: CommandArgument,
                /**
                 * The argument strings
                 */
                val value: String) : Token()

        /**
         * A parameter combining a [CommandOption] and its values
         */
        data class Parameter(
                /**
                 * The [CommandOption] of the parameter
                 */
                val option: CommandOption,
                /**
                 * The argument strings
                 */
                val value: List<String>) : Token()
    }
}

/**
 * Parse the given tokens into parameters, flags and arguments
 * @receiver The parser configuration
 * @throws InvalidTokensException When a token is invalid
 * @return A list of parameters, flags and arguments
 */
fun CommandConfig.parseTokens(
        tokens: Iterable<String>
): Pair<List<CommandConfig>, List<TokenParser.Token>> =
        withArgs(tokens) {
            TokenParser(this).let { parser ->
                tokens.forEach { parser.append(it) }
                parser.finish()
            }
        }
