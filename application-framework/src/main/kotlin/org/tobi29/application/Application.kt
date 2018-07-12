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

package org.tobi29.application

import org.tobi29.args.*
import org.tobi29.stdex.printerrln
import org.tobi29.utils.Version

interface Named {
    val execName: String
    val fullName: String get() = execName
    val name: String get() = fullName.replace(" ", "")
}

interface Identified {
    val id: String
}

interface Versioned {
    val version: Version
}

abstract class BareApplication : EntryPoint(), Identified, Named, Versioned {
    protected val cli = CommandConfigBuilder()
    val commandConfig by lazy { CommandConfig(execName, cli) }

    abstract suspend fun execute(commandLine: CommandLine): StatusCode

    override suspend fun execute(args: Array<String>): StatusCode {
        val commandLine = try {
            commandConfig.parseCommandLine(args.asIterable())
        } catch (e: InvalidTokensException) {
            return handleCommandLineError(e)
        }
        handleEarly(commandLine)?.let { return it }
        return execute(commandLine)
    }

    protected open fun handleCommandLineError(e: InvalidTokensException): StatusCode {
        return 255
    }

    protected open fun handleEarly(commandLine: CommandLine): StatusCode? {
        return null
    }
}

abstract class Application : BareApplication() {
    private val helpOption = cli.commandFlag(
        setOf('h'), setOf("help"),
        "Print this text and exit",
        abortParse = true
    )
    private val versionOption = cli.commandFlag(
        setOf('v'), setOf("version"),
        "Print version and exit",
        abortParse = true
    )

    override fun handleCommandLineError(e: InvalidTokensException): StatusCode {
        printerrln(e.message)
        when (e) {
            is MissingOptionException,
            is MissingArgumentException -> {
                e as InvalidCommandLineException
                printerrln("Usage: ${e.commandLine.command.printUsage()}")
            }
        }
        return super.handleCommandLineError(e)
    }

    override fun handleEarly(commandLine: CommandLine): StatusCode? {
        super.handleEarly(commandLine)?.let { return it }

        if (commandLine.getBoolean(helpOption)) {
            print(commandLine.command.printHelp())
            return 0
        }

        if (commandLine.getBoolean(versionOption)) {
            println("$fullName $version")
            return 0
        }

        return null
    }
}
