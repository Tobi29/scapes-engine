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

package com.j256.simplemagik

import com.j256.simplemagik.entries.readMagicEntries
import com.j256.simplemagik.entries.write
import org.tobi29.io.OutputStreamByteStream
import org.tobi29.stdex.JvmStatic
import org.tobi29.stdex.printerrln
import kotlin.coroutines.experimental.buildIterator

object MagicCompiler {
    @JvmStatic
    fun main(args: Array<String>) {
        val entries = readMagicEntries(buildIterator {
            while (true) {
                yield(readLine() ?: break)
            }
        }) { error, description, e ->
            if (e == null)
                printerrln("Magic error: $error $description")
            else
                printerrln("Magic error: $error $description")
        }
        val output = OutputStreamByteStream(System.out)
        for (entry in entries) entry.write(output)
        System.out.flush()
    }
}
