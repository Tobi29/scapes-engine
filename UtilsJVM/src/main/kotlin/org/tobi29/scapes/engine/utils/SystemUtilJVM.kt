package org.tobi29.scapes.engine.utils

import kotlin.system.exitProcess

/**
 * Runs [exitProcess] in a new thread
 *
 * **Note**: Unlike [exitProcess] this function is very likely to return
 * @param status The status code passed to [exitProcess]
 */
fun exitLater(status: Int) {
    val thread = Thread {
        exitProcess(status)
    }
    thread.name = "Exit-JVM"
    thread.start()
}
