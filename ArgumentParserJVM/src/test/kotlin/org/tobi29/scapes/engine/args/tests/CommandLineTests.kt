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

package org.tobi29.scapes.engine.args.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.args.*
import org.tobi29.scapes.engine.test.assertions.shouldEqual

private val helpOption = CommandOption(
        setOf('h'), setOf("help"), "Print this text and exit")
private val versionOption = CommandOption(
        setOf('v'), setOf("version"), "Print version and exit")
private val propertyOption = CommandOption(
        setOf('p'), setOf("property"), listOf("arg"), "Set a property")
private val argumentOption = CommandArgument(
        "arg", 0..1)
private val argument2Option = CommandArgument(
        "arg2", 0..1)
private val subPropertyOption = CommandOption(
        setOf('p'), setOf("property"), listOf("arg"),
        "Set a subcommand property")
private val subSettingOption = CommandOption(
        setOf('s'), setOf("setting"), listOf("arg"), "Set a subcommand setting")
private val subArgumentOption = CommandArgument(
        "arg", 1..Integer.MAX_VALUE)
private val subCommand = CommandConfig("sub", listOf(
        subPropertyOption, subSettingOption, subArgumentOption))
private val command = CommandConfig("test", listOf(
        helpOption, versionOption, propertyOption, argumentOption,
        argument2Option, subCommand))

object CommandLineTests : Spek({
    describe("parsing options from tokens") {
        given("a parser configuration") {
            for (test in listOf(
                    CommandParseTest(
                            args = listOf("--property", "first", "arg", "-v",
                                    "arg2", "-p=second"),
                            tokens = listOf(
                                    TokenParser.Token.Parameter(
                                            propertyOption,
                                            listOf("first")),
                                    TokenParser.Token.Argument(
                                            argumentOption, "arg"),
                                    TokenParser.Token.Parameter(
                                            versionOption,
                                            listOf()),
                                    TokenParser.Token.Argument(
                                            argument2Option, "arg2"),
                                    TokenParser.Token.Parameter(
                                            propertyOption,
                                            listOf("second"))),
                            commandLine = CommandLine(
                                    listOf(command),
                                    mapOf(propertyOption to listOf("first",
                                            "second"),
                                            versionOption to listOf()),
                                    mapOf(argumentOption to listOf("arg"),
                                            argument2Option to listOf(
                                                    "arg2")))),
                    CommandParseTest(
                            args = listOf("-p", "a", "sub", "arg", "-p", "b",
                                    "-s", "c"),
                            tokens = listOf(
                                    TokenParser.Token.Parameter(
                                            propertyOption,
                                            listOf("a")),
                                    TokenParser.Token.Argument(
                                            subArgumentOption, "arg"),
                                    TokenParser.Token.Parameter(
                                            subPropertyOption,
                                            listOf("b")),
                                    TokenParser.Token.Parameter(
                                            subSettingOption,
                                            listOf("c"))),
                            commandLine = CommandLine(
                                    listOf(command, subCommand),
                                    mapOf(propertyOption to listOf("a"),
                                            subPropertyOption to listOf("b"),
                                            subSettingOption to listOf("c")),
                                    mapOf(subArgumentOption to listOf("arg")))
                    ))) {
                on("parsing the command ${test.args}") {
                    val (subcommand, tokens) = command.parseTokens(test.args)
                    val commandLine = tokens.assemble()
                            .copy(command = subcommand)
                    commandLine.validate()

                    it("should return the correct token sequence") {
                        tokens shouldEqual test.tokens
                    }

                    it("should contain the correct parameters and arguments in the command line") {
                        commandLine shouldEqual test.commandLine
                    }
                }
            }
        }
    }
})

private data class CommandParseTest(
        val args: List<String>,
        val tokens: List<TokenParser.Token>,
        val commandLine: CommandLine
)
