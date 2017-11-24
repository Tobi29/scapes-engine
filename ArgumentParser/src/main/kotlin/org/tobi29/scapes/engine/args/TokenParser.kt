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

import org.tobi29.scapes.engine.utils.ArrayDeque
import org.tobi29.scapes.engine.utils.readOnly

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
    private var currentArgument: ArgumentEntry? = null
    private val arguments = ArrayDeque<CommandArgument>()

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
     */
    fun finish(): Pair<List<CommandConfig>, List<Token>> {
        currentOption?.let { option ->
            tokensMut.add(Token.Parameter(option, currentArgs))
            currentArgs.clear()
            currentOption = null
        }

        return command to tokens
    }

    /**
     * Parses the next token
     * @param token The token to parse
     * @throws InvalidCommandLineException The token is invalid
     */
    fun append(token: String) {
        val length = token.length

        if (length == 0 || optionsTerminated || currentOption != null) {
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
                            // Token: "--..."
                            appendLong(token.substring(2))
                        }
                    }
                    else -> {
                        // Token: "-..."
                        appendShort(token.substring(1))
                    }
                }
            }
            else -> {
                // Token: "..."
                appendArg(token)
            }
        }
    }

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

        if ((currentArgument?.count ?: 0) <= 0) arguments.poll()?.let {
            currentArgument = ArgumentEntry(it)
        }
        currentArgument?.let { argument ->
            tokensMut.add(Token.Argument(argument.argument, token))
            argument.count--
            if (argument.count <= 0) currentArgument = null
            return
        }

        throw InvalidCommandLineException("Stray argument: $token")
    }

    private fun appendShort(token: String) {
        val equals = token.indexOf('=')

        if (equals >= 0) {
            for (i in 0..equals - 2) {
                val name = token[i]
                (shortOptions[name] ?: throw InvalidCommandLineException(
                        "Invalid option: $name")).let {
                    if (it.args.isNotEmpty()) {
                        currentOption = it
                    } else {
                        tokensMut.add(Token.Parameter(it, emptyList()))
                    }
                }
            }

            val name = token[equals - 1]
            val argument = token.substring(equals + 1)

            (shortOptions[name] ?: throw InvalidCommandLineException(
                    "Invalid option: $name")).let {
                tokensMut.add(Token.Parameter(it, listOf(argument)))
            }
            return
        }

        for (name in token) {
            (shortOptions[name] ?: throw InvalidCommandLineException(
                    "Invalid option: $name")).let {
                if (it.args.isNotEmpty()) {
                    currentOption = it
                } else {
                    tokensMut.add(Token.Parameter(it, emptyList()))
                }
            }
        }
    }

    private fun appendLong(token: String) {
        val equals = token.indexOf('=')

        if (equals >= 0) {
            val name = token.substring(0, equals)
            val argument = token.substring(equals + 1)

            (longOptions[name] ?: throw InvalidCommandLineException(
                    "Invalid option: $name")).let {
                tokensMut.add(Token.Parameter(it, listOf(argument)))
            }
            return
        }

        (longOptions[token] ?: throw InvalidCommandLineException(
                "Invalid option: $token")).let {
            if (it.args.isNotEmpty()) {
                currentOption = it
            } else {
                tokensMut.add(Token.Parameter(it, emptyList()))
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
        currentArgument = null
        arguments.clear()
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

    private class ArgumentEntry(val argument: CommandArgument) {
        var count = argument.count.endInclusive
    }
}

/**
 * Parse the given tokens into parameters, flags and arguments
 * @receiver The parser configuration
 * @throws InvalidCommandLineException When a token is invalid
 * @return A list of parameters, flags and arguments
 */
fun CommandConfig.parseTokens(
        tokens: Iterable<String>
): Pair<List<CommandConfig>, List<TokenParser.Token>> =
        TokenParser(this).let { parser ->
            tokens.forEach { parser.append(it) }
            parser.finish()
        }
