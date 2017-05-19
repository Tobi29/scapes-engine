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

import org.tobi29.scapes.engine.utils.readOnly

/**
 * Parses a stream of tokens into parameters, flags and arguments
 */
class TokenParser
/**
 * Constructs a new parses using the given options
 * @param options The sequence of options to use for parsing
 */
(private val options: Sequence<CommandOption>) {
    private var optionsTerminated = false
    private var currentOption: CommandOption? = null
    private val currentArgs = ArrayList<String>()
    private val tokensMut = ArrayList<Token>()

    /**
     * The current list of parsed tokens
     */
    val tokens = tokensMut.readOnly()

    /**
     * Finishes the parsing and returns a list of parsed tokens
     */
    fun finish(): List<Token> {
        currentOption?.let { option ->
            tokensMut.add(Token.Parameter(option, currentArgs))
            currentArgs.clear()
            currentOption = null
        }

        return tokens
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
                }
                when (token[1]) {
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
            if (currentArgs.size >= option.args - 1) {
                tokensMut.add(Token.Parameter(option, currentArgs + token))
                currentArgs.clear()
                currentOption = null
            } else {
                currentArgs.add(token)
            }
            return
        }

        tokensMut.add(Token.Argument(token))
    }

    private fun appendShort(token: String) {
        val equals = token.indexOf('=')

        if (equals >= 0) {
            for (i in 0..equals - 2) {
                val char = token[i]
                (options.firstOrNull { it.matches(char) }
                        ?: throw InvalidCommandLineException(
                        "Invalid option: $char")).let {
                    if (it.args > 0) {
                        currentOption = it
                    } else {
                        tokensMut.add(Token.Parameter(it, emptyList()))
                    }
                }
            }

            val name = token[equals - 1]
            val argument = token.substring(equals + 1)

            (options.firstOrNull { it.matches(name) }
                    ?: throw InvalidCommandLineException(
                    "Invalid option: $name")).let {
                tokensMut.add(Token.Parameter(it, listOf(argument)))
            }
            return
        }

        for (char in token) {
            (options.firstOrNull { it.matches(char) }
                    ?: throw InvalidCommandLineException(
                    "Invalid option: $char")).let {
                if (it.args > 0) {
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

            (options.firstOrNull { it.matches(name) }
                    ?: throw InvalidCommandLineException(
                    "Invalid option: $name")).let {
                tokensMut.add(Token.Parameter(it, listOf(argument)))
            }
            return
        }

        (options.firstOrNull { it.matches(token) }
                ?: throw InvalidCommandLineException(
                "Invalid option: $token")).let {
            if (it.args > 0) {
                currentOption = it
            } else {
                tokensMut.add(Token.Parameter(it, emptyList()))
            }
        }
    }

    private fun appendTerminateOptions() {
        optionsTerminated = true
    }

    /**
     * A parsed token
     */
    sealed class Token {
        /**
         * An argument not attached to any [CommandOption]
         */
        data class Argument
        /**
         * Creates a new argument instance with the given contents
         * @param argument The argument string
         */
        (
                /**
                 * The argument string
                 */
                val argument: String) : Token()

        /**
         * A parameter combining a [CommandOption] and its values
         */
        data class Parameter
        /**
         * Creates a new parameter instance with the given contents
         * @param option The [CommandOption] of the parameter
         * @param value The argument strings
         */
        (
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
