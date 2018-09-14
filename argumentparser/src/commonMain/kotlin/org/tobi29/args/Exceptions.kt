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
import org.tobi29.utils.Either
import org.tobi29.utils.EitherLeft
import org.tobi29.utils.EitherRight
import org.tobi29.utils.get

/**
 * Exception when invalid input was given to command line parsing
 */
sealed class InvalidTokensException(
    val tokens: List<String>? = null,
    message: String
) : Exception(message)

class EmptyCommandException(
    message: String = "Empty command"
) : InvalidTokensException(emptyList(), message)

class UnknownCommandException(
    tokens: List<String>? = null,
    val command: List<String>,
    message: String = "Unknown command ${command.joinToString(" ")}"
) : InvalidTokensException(tokens, message)

class UnknownOptionException(
    tokens: List<String>? = null,
    val option: Either<Char, String>,
    message: String = "Invalid option ${option.get()}"
) : InvalidTokensException(tokens, message) {
    constructor(
        tokens: List<String>? = null,
        option: Char,
        message: String = "Invalid option $option"
    ) : this(tokens, EitherLeft(option), message)

    constructor(
        tokens: List<String>? = null,
        option: String,
        message: String = "Invalid option $option"
    ) : this(tokens, EitherRight(option), message)
}

class StrayArgumentException(
    tokens: List<String>? = null,
    val argument: String,
    message: String = "Stray argument $argument"
) : InvalidTokensException(tokens, message)

sealed class InvalidCommandLineException(
    tokens: List<String>? = null,
    val commandLine: CommandLine,
    message: String
) : InvalidTokensException(tokens, message)

sealed class InvalidTokenException(
    tokens: List<String>? = null,
    commandLine: CommandLine,
    open val token: TokenParser.Token,
    message: String
) : InvalidCommandLineException(tokens, commandLine, message)

sealed class InvalidTokenParameterException(
    tokens: List<String>? = null,
    commandLine: CommandLine,
    override val token: TokenParser.Token.Parameter,
    message: String
) : InvalidTokenException(tokens, commandLine, token, message) {
    val option get(): CommandOption = token.option
}

sealed class InvalidTokenArgumentException(
    tokens: List<String>? = null,
    commandLine: CommandLine,
    override val token: TokenParser.Token.Argument,
    message: String
) : InvalidTokenException(tokens, commandLine, token, message) {
    val argument get(): CommandArgument = token.argument
}

class MissingOptionException(
    tokens: List<String>? = null,
    commandLine: CommandLine,
    val option: CommandOption,
    message: String = "No value given for ${option.simpleName}"
) : InvalidCommandLineException(tokens, commandLine, message)

class MissingOptionArgumentException(
    tokens: List<String>? = null,
    commandLine: CommandLine,
    token: TokenParser.Token.Parameter,
    message: String = "Not enough values supplied for ${token.option.simpleName}"
) : InvalidTokenParameterException(tokens, commandLine, token, message) {
    val amount get(): Int = option.args.size - token.value.size
}

class ExtraOptionArgumentException(
    tokens: List<String>? = null,
    commandLine: CommandLine,
    token: TokenParser.Token.Parameter,
    message: String = "Too many values supplied for ${token.option.simpleName}"
) : InvalidTokenParameterException(tokens, commandLine, token, message) {
    val amount get(): Int = token.value.size - option.args.size
}

class InvalidOptionArgumentException(
    tokens: List<String>? = null,
    commandLine: CommandLine,
    token: TokenParser.Token.Parameter,
    reason: String? = null,
    message: String = "Invalid option for ${token.option.simpleName
    }: ${token.value.joinToString(" ")}${
    if (reason != null) " ($reason)" else ""}\""
) : InvalidTokenParameterException(tokens, commandLine, token, message)

class MissingArgumentException(
    tokens: List<String>? = null,
    commandLine: CommandLine,
    val argument: CommandArgument,
    val amount: Int = 1,
    message: String = "Not enough values supplied for ${argument.name}"
) : InvalidCommandLineException(tokens, commandLine, message)

class ExtraArgumentException(
    tokens: List<String>? = null,
    commandLine: CommandLine,
    token: TokenParser.Token.Argument,
    val amount: Int = 1,
    message: String = "Too many values supplied for ${token.argument.name}"
) : InvalidTokenArgumentException(tokens, commandLine, token, message)

class InvalidArgumentException(
    tokens: List<String>? = null,
    commandLine: CommandLine,
    token: TokenParser.Token.Argument,
    reason: String? = null,
    message: String = "Invalid argument for ${token.argument.name
    }: ${token.value}${
    if (reason != null) " ($reason)" else ""}"
) : InvalidTokenArgumentException(tokens, commandLine, token, message)

@Suppress("UNCHECKED_CAST")
fun <E : InvalidTokensException> E.attach(
    tokens: List<String>? = this.tokens
): E = when (this) {
    is EmptyCommandException ->
        EmptyCommandException(message!!) as E
    is UnknownCommandException ->
        UnknownCommandException(
            tokens, command, message!!
        ) as E
    is UnknownOptionException ->
        UnknownOptionException(
            tokens, option, message!!
        ) as E
    is StrayArgumentException ->
        StrayArgumentException(
            tokens, argument, message!!
        ) as E
    is MissingOptionException ->
        MissingOptionException(
            tokens, commandLine, option, message!!
        ) as E
    is MissingOptionArgumentException ->
        MissingOptionArgumentException(
            tokens, commandLine, token, message!!
        ) as E
    is ExtraOptionArgumentException ->
        ExtraOptionArgumentException(
            tokens, commandLine, token, message!!
        ) as E
    is InvalidOptionArgumentException ->
        InvalidOptionArgumentException(
            tokens, commandLine, token, null, message!!
        ) as E
    is MissingArgumentException ->
        MissingArgumentException(
            tokens, commandLine, argument, amount, message!!
        ) as E
    is ExtraArgumentException ->
        ExtraArgumentException(
            tokens, commandLine, token, amount, message!!
        ) as E
    is InvalidArgumentException ->
        InvalidArgumentException(
            tokens, commandLine, token, null, message!!
        ) as E
    else -> throw IllegalArgumentException("Invalid exception: $this")
}

@InlineUtility
@Suppress("NOTHING_TO_INLINE")
inline fun <R> withTokens(
    tokens: Iterable<String>? = null,
    block: () -> R
): R = try {
    block()
} catch (e: InvalidTokensException) {
    throw e.attach(tokens?.toList())
}
