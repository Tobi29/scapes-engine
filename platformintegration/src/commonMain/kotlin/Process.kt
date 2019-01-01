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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.platform

import org.tobi29.io.IOException
import org.tobi29.io.filesystem.FilePath
import org.tobi29.stdex.Throws

expect class Process {
    val exitCode: Int
    val isAlive: Boolean
}

expect fun Process.onStop(callback: (Int) -> Unit)

// TODO: Make suspending
expect fun Process.awaitStop()

sealed class RedirectOut {
    object Inherit : RedirectOut()

    class File(
        val path: FilePath,
        val append: Boolean = false
    ) : RedirectOut()

    // TODO: Support pipe redirect
}

sealed class RedirectIn {
    object Inherit : RedirectIn()

    class File(val path: FilePath) : RedirectIn()

    // TODO: Support pipe redirect
}

// TODO: Support changing environment variables

// FIXME @Throws(IOException::class)
fun launchProcess(
    executable: FilePath,
    vararg arguments: String,
    redirectStdout: RedirectOut = RedirectOut.Inherit,
    redirectStderr: RedirectOut = RedirectOut.Inherit,
    redirectStdin: RedirectIn = RedirectIn.Inherit
): Process = launchProcess(
    executable.toAbsolutePath().toString(), *arguments,
    redirectStdout = redirectStdout,
    redirectStderr = redirectStderr,
    redirectStdin = redirectStdin
)

// FIXME @Throws(IOException::class)
fun launchProcess(
    executable: String,
    vararg arguments: String,
    redirectStdout: RedirectOut = RedirectOut.Inherit,
    redirectStderr: RedirectOut = RedirectOut.Inherit,
    redirectStdin: RedirectIn = RedirectIn.Inherit
): Process = launchProcessImpl(
    executable, arguments,
    redirectStdout, redirectStderr, redirectStdin
)

@PublishedApi
internal expect fun launchProcessImpl(
    executable: String,
    arguments: Array<out String>,
    redirectStdout: RedirectOut,
    redirectStderr: RedirectOut,
    redirectStdin: RedirectIn
): Process

expect fun checkChildProcesses()
