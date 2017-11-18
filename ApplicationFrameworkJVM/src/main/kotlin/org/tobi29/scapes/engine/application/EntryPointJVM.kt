package org.tobi29.scapes.engine.application

import kotlinx.coroutines.experimental.runBlocking
import kotlin.system.exitProcess

actual abstract class EntryPoint actual constructor() : Program {
}

fun EntryPoint.executeMain(args: Array<String>) {
    exitProcess(runBlocking { execute(args) })
}
