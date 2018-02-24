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

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.assertions.shouldEqual
import org.tobi29.assertions.shouldThrow

private val helpFlag = CommandFlag(
    setOf('h'), setOf("help"),
    "Print this text and exit"
)
private val versionFlag = CommandFlag(
    setOf('v'), setOf("version"),
    "Print version and exit"
)
private val propertyOption = CommandOption(
    setOf('p'), setOf("property"), listOf("arg"),
    "Set a property"
)
private val multiPropertyOption = CommandOption(
    setOf('m'), setOf("multi-property"), listOf("arg1", "arg2"),
    "Set two properties"
)
private val abortFlag = CommandFlag(
    setOf('c'), setOf("abort-flag"), "Aborts parse",
    abortParse = true
)
private val abortOption = CommandOption(
    setOf('a'), setOf("abort"), listOf("arg"), "Aborts parse",
    abortParse = true
)
private val multiAbortOption = CommandOption(
    setOf('b'), setOf("multi-abort"), listOf("arg1", "arg2"), "Aborts parse",
    abortParse = true
)
private val subPropertyOption = CommandOption(
    setOf('p'), setOf("property"), listOf("arg"),
    "Set a subcommand property"
)
private val subSettingOption = CommandOption(
    setOf('s'), setOf("setting"), listOf("arg"),
    "Set a subcommand setting"
)

private val argumentArgument = CommandArgument(
    "arg", 0..1
)
private val argument2Argument = CommandArgument(
    "arg2", 0..2
)
private val argumentMinArgument = CommandArgument(
    "arg2", 1..1
)
private val subArgumentArgument = CommandArgument(
    "arg", 1..Int.MAX_VALUE
)
private val abortArgument = CommandArgument(
    "arg", 1..1,
    abortParse = true
)

private val subCommand = CommandConfig(
    "sub", listOf(
        subPropertyOption,
        subSettingOption,
        subArgumentArgument
    )
)
private val command = CommandConfig(
    "test", listOf(
        helpFlag,
        versionFlag,
        propertyOption,
        multiPropertyOption,
        abortFlag,
        abortOption,
        multiAbortOption,
        argumentArgument,
        argument2Argument,
        subCommand
    )
)
private val command2 = CommandConfig(
    "test", listOf(
        abortFlag,
        argumentMinArgument
    )
)
private val command3 = CommandConfig(
    "test", listOf(
        abortArgument
    )
)

object CommandLineTests : Spek({
    describe("parsing options from tokens") {
        given("a parser configuration") {
            for (test in listOf(
                CommandParseTest(
                    command = command,
                    args = listOf(
                        "--property", "first", "arg", "-hv",
                        "arg2", "-p=second", "-m", "1", "2", "-pv"
                    ),
                    tokens = listOf(
                        TokenParser.Token.Parameter(
                            propertyOption,
                            listOf("first")
                        ),
                        TokenParser.Token.Argument(
                            argumentArgument,
                            "arg"
                        ),
                        TokenParser.Token.Parameter(
                            helpFlag,
                            listOf()
                        ),
                        TokenParser.Token.Parameter(
                            versionFlag,
                            listOf()
                        ),
                        TokenParser.Token.Argument(
                            argument2Argument,
                            "arg2"
                        ),
                        TokenParser.Token.Parameter(
                            propertyOption,
                            listOf("second")
                        ),
                        TokenParser.Token.Parameter(
                            multiPropertyOption,
                            listOf("1", "2")
                        ),
                        TokenParser.Token.Parameter(
                            propertyOption,
                            listOf("v")
                        )
                    ),
                    commandLine = CommandLine(
                        listOf(command),
                        mapOf(
                            propertyOption to listOf(
                                listOf("first"),
                                listOf("second"),
                                listOf("v")
                            ),
                            multiPropertyOption to listOf(listOf("1", "2")),
                            helpFlag to listOf(listOf()),
                            versionFlag to listOf(listOf())
                        ),
                        mapOf(
                            argumentArgument to listOf("arg"),
                            argument2Argument to listOf("arg2")
                        ),
                        null
                    )
                ),
                CommandParseTest(
                    command = command,
                    args = listOf(
                        "-p", "a", "sub", "arg", "-p", "b",
                        "-s", "c"
                    ),
                    tokens = listOf(
                        TokenParser.Token.Parameter(
                            propertyOption,
                            listOf("a")
                        ),
                        TokenParser.Token.Argument(
                            subArgumentArgument,
                            "arg"
                        ),
                        TokenParser.Token.Parameter(
                            subPropertyOption,
                            listOf("b")
                        ),
                        TokenParser.Token.Parameter(
                            subSettingOption,
                            listOf("c")
                        )
                    ),
                    commandLine = CommandLine(
                        listOf(command, subCommand),
                        mapOf(
                            propertyOption to listOf(listOf("a")),
                            subPropertyOption to listOf(listOf("b")),
                            subSettingOption to listOf(listOf("c"))
                        ),
                        mapOf(
                            subArgumentArgument to listOf(
                                "arg"
                            )
                        ),
                        null
                    )
                ),
                CommandParseTest(
                    command = command,
                    args = listOf(
                        "-c", "-a", "-b", "-c"
                    ),
                    tokens = listOf(
                        TokenParser.Token.Parameter(
                            abortFlag,
                            listOf()
                        ),
                        TokenParser.Token.Trail("-a"),
                        TokenParser.Token.Trail("-b"),
                        TokenParser.Token.Trail("-c")
                    ),
                    commandLine = CommandLine(
                        listOf(command),
                        mapOf(
                            abortFlag to listOf(listOf())
                        ),
                        mapOf(),
                        listOf("-a", "-b", "-c")
                    )
                ),
                CommandParseTest(
                    command = command,
                    args = listOf(
                        "--abort-flag", "-a", "-b", "-c"
                    ),
                    tokens = listOf(
                        TokenParser.Token.Parameter(
                            abortFlag,
                            listOf()
                        ),
                        TokenParser.Token.Trail("-a"),
                        TokenParser.Token.Trail("-b"),
                        TokenParser.Token.Trail("-c")
                    ),
                    commandLine = CommandLine(
                        listOf(command),
                        mapOf(
                            abortFlag to listOf(listOf())
                        ),
                        mapOf(),
                        listOf("-a", "-b", "-c")
                    )
                ),
                CommandParseTest(
                    command = command,
                    args = listOf(
                        "-a", "-a", "-b", "-c"
                    ),
                    tokens = listOf(
                        TokenParser.Token.Parameter(
                            abortOption,
                            listOf("-a")
                        ),
                        TokenParser.Token.Trail("-b"),
                        TokenParser.Token.Trail("-c")
                    ),
                    commandLine = CommandLine(
                        listOf(command),
                        mapOf(
                            abortOption to listOf(listOf("-a"))
                        ),
                        mapOf(),
                        listOf("-b", "-c")
                    )
                ),
                CommandParseTest(
                    command = command,
                    args = listOf(
                        "--abort", "-a", "-b", "-c"
                    ),
                    tokens = listOf(
                        TokenParser.Token.Parameter(
                            abortOption,
                            listOf("-a")
                        ),
                        TokenParser.Token.Trail("-b"),
                        TokenParser.Token.Trail("-c")
                    ),
                    commandLine = CommandLine(
                        listOf(command),
                        mapOf(
                            abortOption to listOf(listOf("-a"))
                        ),
                        mapOf(),
                        listOf("-b", "-c")
                    )
                ),
                CommandParseTest(
                    command = command,
                    args = listOf(
                        "--abort=-a", "-b", "-c"
                    ),
                    tokens = listOf(
                        TokenParser.Token.Parameter(
                            abortOption,
                            listOf("-a")
                        ),
                        TokenParser.Token.Trail("-b"),
                        TokenParser.Token.Trail("-c")
                    ),
                    commandLine = CommandLine(
                        listOf(command),
                        mapOf(
                            abortOption to listOf(listOf("-a"))
                        ),
                        mapOf(),
                        listOf("-b", "-c")
                    )
                ),
                CommandParseTest(
                    command = command,
                    args = listOf(
                        "-b", "-a", "-b", "-c"
                    ),
                    tokens = listOf(
                        TokenParser.Token.Parameter(
                            multiAbortOption,
                            listOf("-a", "-b")
                        ),
                        TokenParser.Token.Trail("-c")
                    ),
                    commandLine = CommandLine(
                        listOf(command),
                        mapOf(
                            multiAbortOption to listOf(listOf("-a", "-b"))
                        ),
                        mapOf(),
                        listOf("-c")
                    )
                ),
                CommandParseTest(
                    command = command,
                    args = listOf(
                        "--multi-abort", "-a", "-b", "-c"
                    ),
                    tokens = listOf(
                        TokenParser.Token.Parameter(
                            multiAbortOption,
                            listOf("-a", "-b")
                        ),
                        TokenParser.Token.Trail("-c")
                    ),
                    commandLine = CommandLine(
                        listOf(command),
                        mapOf(
                            multiAbortOption to listOf(listOf("-a", "-b"))
                        ),
                        mapOf(),
                        listOf("-c")
                    )
                ),
                CommandParseTest(
                    command = command3,
                    args = listOf(
                        "a", "b", "c"
                    ),
                    tokens = listOf(
                        TokenParser.Token.Argument(
                            abortArgument,
                            "a"
                        ),
                        TokenParser.Token.Trail("b"),
                        TokenParser.Token.Trail("c")
                    ),
                    commandLine = CommandLine(
                        listOf(command3),
                        mapOf(),
                        mapOf(
                            abortArgument to listOf("a")
                        ),
                        listOf("b", "c")
                    )
                ),
                CommandParseTest(
                    command = command2,
                    args = listOf(
                        "-c"
                    ),
                    tokens = listOf(
                        TokenParser.Token.Parameter(
                            abortFlag,
                            listOf()
                        )
                    ),
                    commandLine = CommandLine(
                        listOf(command2),
                        mapOf(
                            abortFlag to listOf(listOf())
                        ),
                        mapOf(),
                        listOf()
                    )
                )
            )) {
                on("parsing the command ${test.args}") {
                    val (subcommand, tokens) =
                            test.command.parseTokens(test.args)
                    val commandLine = tokens.assemble()
                        .copy(command = subcommand)

                    it("should return the correct token sequence") {
                        tokens shouldEqual test.tokens
                    }

                    it("should return a valid command line") {
                        commandLine.validate(tokens)
                    }

                    it("should contain the correct parameters and arguments in the command line") {
                        commandLine shouldEqual test.commandLine
                    }
                }
            }
        }
        given("a parser configuration with missing arguments") {
            for (test in listOf(
                CommandParseTest(
                    command = command,
                    args = listOf("--multi-property=1", "-m", "2"),
                    tokens = listOf(
                        TokenParser.Token.Parameter(
                            multiPropertyOption,
                            listOf("1")
                        ),
                        TokenParser.Token.Parameter(
                            multiPropertyOption,
                            listOf("2")
                        )
                    ),
                    commandLine = CommandLine(
                        listOf(command),
                        mapOf(
                            multiPropertyOption to listOf(
                                listOf("1"),
                                listOf("2")
                            )
                        ),
                        mapOf(),
                        null
                    )
                ),
                CommandParseTest(
                    command = command2,
                    args = listOf(),
                    tokens = listOf(),
                    commandLine = CommandLine(
                        listOf(command2),
                        mapOf(),
                        mapOf(),
                        null
                    )
                )
            )) {
                on("parsing the command ${test.args}") {
                    val (subcommand, tokens) =
                            test.command.parseTokens(test.args)
                    val commandLine = tokens.assemble()
                        .copy(command = subcommand)

                    it("should return the correct token sequence") {
                        tokens shouldEqual test.tokens
                    }

                    it("should return a invalid command line") {
                        shouldThrow<InvalidTokensException> {
                            commandLine.validate(tokens)
                        }
                    }

                    it("should contain the correct parameters and arguments in the command line") {
                        commandLine shouldEqual test.commandLine
                    }
                }
            }
        }
    }
    given("a parser configuration with stray arguments") {
        for (test in listOf(
            CommandParseTest(
                command = command,
                args = listOf("1", "2", "3", "4")
            )
        )) {
            on("parsing the command ${test.args}") {
                it("should fails to parse") {
                    shouldThrow<InvalidTokensException> {
                        test.command.parseTokens(test.args)
                    }
                }
            }
        }
    }
})

private data class CommandParseTest(
    val command: CommandConfig,
    val args: List<String>,
    val tokens: List<TokenParser.Token> = emptyList(),
    val commandLine: CommandLine = CommandLine(
        emptyList(), emptyMap(), emptyMap(), emptyList()
    )
)
