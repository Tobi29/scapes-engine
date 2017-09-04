package org.tobi29.scapes.engine.utils.io

import org.tobi29.scapes.engine.utils.copyToString

fun crashReportName(time: String): String =
        "CrashReport-$time.txt"

fun Appendable.crashReportSection(name: String,
                                  columns: Int = 180) {
    val dashes = columns - name.length - 1
    val dashesLeft = dashes shr 1
    val dashesRight = dashes - dashesLeft
    val dashesLeftStr = CharArray(dashesLeft) { '-' }.copyToString()
    val dashesRightStr = CharArray(dashesRight) { '-' }.copyToString()
    println("$dashesLeftStr$name:$dashesRightStr")
}

fun Appendable.writeCrashReport(e: Throwable,
                                name: String,
                                vararg sections: Pair<String, Appendable.() -> Unit>) =
        writeCrashReport(e, name, sections.asSequence())

fun Appendable.writeCrashReport(e: Throwable,
                                name: String,
                                sections: Sequence<Pair<String, Appendable.() -> Unit>>) {
    println(name + " has crashed: " + e)
    println()
    println("Please give this file to someone who has an idea of what to do with it (developer!)")
    println()
    for ((sectionName, section) in sections) {
        crashReportSection(sectionName)
        section()
        println()
    }
}

fun crashReportSectionProperties(properties: Map<String, String>): Appendable.() -> Unit =
        crashReportSectionProperties(
                properties.asSequence()
                        .map { (key, value) -> key to value })

fun crashReportSectionProperties(properties: Sequence<Pair<String, String>>): Appendable.() -> Unit = {
    for ((key, value) in properties) {
        println("$key = $value")
    }
}

fun crashReportSectionString(time: String): Appendable.() -> Unit = {
    println(time)
}

fun crashReportSectionTime(time: String): Pair<String, Appendable.() -> Unit> =
        "Time" to crashReportSectionString(time)

fun Appendable.println() {
    print("\n")
}

fun Appendable.println(line: String) {
    print("$line\n")
}

fun Appendable.print(str: String) {
    append(str)
}