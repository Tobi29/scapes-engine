package org.tobi29.scapes.engine.application

import org.tobi29.scapes.engine.args.*
import org.tobi29.scapes.engine.utils.Version
import org.tobi29.scapes.engine.utils.printerrln

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
    private val optionsMut = ArrayList<CommandElement>()
    val commandConfig by lazy { CommandConfig(execName, optionsMut) }

    abstract suspend fun execute(commandLine: CommandLine): StatusCode

    override suspend fun execute(args: Array<String>): StatusCode {
        val commandLine = try {
            commandConfig.parseDirtyCommandLine(args.asIterable())
        } catch (e: InvalidCommandLineException) {
            printerrln(e.message)
            return 255
        }
        handleEarly(commandLine)?.let { return it }
        try {
            commandLine.validate()
        } catch (e: InvalidCommandLineException) {
            printerrln(e.message)
            return 255
        }
        return execute(commandLine)
    }

    open protected fun handleEarly(commandLine: CommandLine): StatusCode? {
        return null
    }

    protected fun <E : CommandElement> commandElement(option: E) =
            option.also { optionsMut.add(it) }

    protected fun commandOption(
            shortNames: Set<Char> = emptySet(),
            longNames: Set<String> = emptySet(),
            args: List<String> = emptyList(),
            description: String
    ) = commandElement(CommandOption(shortNames, longNames, args, description))

    protected fun commandArgument(
            name: String,
            count: IntRange = 0..1
    ) = commandElement(CommandArgument(name, count))
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
            println(commandLine.command.printHelp())
            return 0
        }

        if (commandLine.getBoolean(versionOption)) {
            println("$fullName $version")
            return 0
        }

        return null
    }
}
