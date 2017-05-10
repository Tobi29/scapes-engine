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
import org.tobi29.scapes.engine.args.CommandOption
import org.tobi29.scapes.engine.args.TokenParser
import org.tobi29.scapes.engine.args.assemble
import org.tobi29.scapes.engine.args.validate
import org.tobi29.scapes.engine.test.assertions.shouldEqual

object CommandLineTests : Spek({
    describe("parsing options from tokens") {
        given("a parser configuration") {
            val optionsList = ArrayList<CommandOption>()
            val helpOption = CommandOption(setOf('h'), setOf("help"),
                    "Print this text and exit").also { optionsList.add(it) }
            val versionOption = CommandOption(setOf('v'), setOf("version"),
                    "Print version and exit").also { optionsList.add(it) }
            val propertyOption = CommandOption(setOf('p'), setOf("property"), 1,
                    "Set a property").also { optionsList.add(it) }
            val options = optionsList.asSequence()
            on("parsing a list of arguments") {
                val args = listOf("--property", "first", "arg", "-v",
                        "-p=second")

                val parser = TokenParser(options)
                args.forEach { parser.append(it) }
                val tokens = parser.finish()
                val commandLine = tokens.assemble()
                commandLine.validate()

                it("should return the correct token sequence") {
                    tokens shouldEqual listOf(
                            TokenParser.Token.Parameter(propertyOption,
                                    listOf("first")),
                            TokenParser.Token.Argument("arg"),
                            TokenParser.Token.Parameter(versionOption,
                                    listOf()),
                            TokenParser.Token.Parameter(propertyOption,
                                    listOf("second")))
                }

                it("should contain the correct parameters and arguments in the command line") {
                    commandLine.arguments shouldEqual listOf("arg")
                    commandLine.parameters shouldEqual mapOf(
                            propertyOption to listOf("first", "second"),
                            versionOption to listOf())
                }
            }
        }
    }
})