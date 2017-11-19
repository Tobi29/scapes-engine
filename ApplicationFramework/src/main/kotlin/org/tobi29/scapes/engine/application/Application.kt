package org.tobi29.scapes.engine.application

import org.tobi29.scapes.engine.args.*
import org.tobi29.scapes.engine.utils.*

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
    private val optionsMut = ArrayList<CommandOption>()
    val options = optionsMut.readOnly()

    abstract suspend fun execute(commandLine: CommandLine): StatusCode

    override suspend fun execute(args: Array<String>): StatusCode {
        val commandLine = tryWrap<CommandLine, InvalidCommandLineException> {
            optionsMut.parseCommandLine(args.asIterable())
        }.unwrapOr { e ->
            printerrln(e.message)
            return 255
        }
        handleEarly(commandLine)?.let { return it }
        return execute(commandLine)
    }

    open protected fun handleEarly(commandLine: CommandLine): StatusCode? {
        return null
    }

    protected fun commandOption(option: CommandOption) =
            option.also { optionsMut.add(it) }

    protected fun commandOption(
            shortNames: Set<Char> = emptySet(),
            longNames: Set<String> = emptySet(),
            args: List<String> = emptyList(),
            description: String
    ) = commandOption(CommandOption(shortNames, longNames, args, description))
}

abstract class Application : BareApplication() {
    private val helpOption = commandOption(
            shortNames = setOf('h'),
            longNames = setOf("help"),
            description = "Print this text and exit")
    private val versionOption = commandOption(
            shortNames = setOf('v'),
            longNames = setOf("version"),
            description = "Print version and exit")

    override fun handleEarly(commandLine: CommandLine): StatusCode? {
        super.handleEarly(commandLine)?.let { return it }

        if (commandLine.getBoolean(helpOption)) {
            println(options.printHelp(execName, commandLine.subcommand))
            return 0
        }

        if (commandLine.getBoolean(versionOption)) {
            println("$fullName $version")
            return 0
        }

        return null
    }
}
