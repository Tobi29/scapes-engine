/*
 * Copyright 2012-2019 Tobi29
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
import org.tobi29.utils.Identified
import org.tobi29.utils.Named
import org.tobi29.utils.Versioned

/**
 * Base class for application objects handling basic needs such as command line
 * parsing and status code on exit
 *
 * **Note:** Consider using [Application] if possible instead
 */
abstract class BareApplication : EntryPoint(), Identified, Named, Versioned {
    protected val cli = CommandConfigBuilder()
    val commandConfig: CommandConfig get() = CommandConfig(executableName, cli)

    /**
     * Should be overridden for application code
     * @throws InvalidTokensException causes help to be printed before exiting
     */
    abstract suspend fun execute(commandLine: CommandLine): StatusCode

    final override suspend fun execute(
        arguments: Array<String>
    ): StatusCode = try {
        val commandLine = commandConfig.parseCommandLine(arguments.asIterable())
        withTokens(arguments.asIterable()) {
            handleEarly(commandLine) ?: execute(commandLine)
        }
    } catch (e: InvalidTokensException) {
        handleCommandLineError(e)
    }

    /**
     * Called after catching an [InvalidTokensException]
     */
    protected open fun handleCommandLineError(e: InvalidTokensException): StatusCode {
        return 255
    }

    /**
     * Called right before calling [execute]
     *
     *@return `null` will cause normal execution of [execute] otherwise exits
     */
    protected open fun handleEarly(commandLine: CommandLine): StatusCode? {
        return null
    }
}

/**
 * Base class for application objects handling basic needs such as command line
 * parsing, status code on exit and help and version display
 *
 * Subclasses should implement the metadata properties as well as the [execute]
 * method to run the application code
 */
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

// TODO: Remove after 0.0.14

@Deprecated(
    "Use interface from utils",
    ReplaceWith("Named", "org.tobi29.utils.Named")
)
typealias Named = Named

@Deprecated(
    "Use interface from utils",
    ReplaceWith("Identified", "org.tobi29.utils.Identified")
)
typealias Identified = Identified

@Deprecated(
    "Use interface from utils",
    ReplaceWith("Versioned", "org.tobi29.utils.Versioned")
)
typealias Versioned = Versioned
