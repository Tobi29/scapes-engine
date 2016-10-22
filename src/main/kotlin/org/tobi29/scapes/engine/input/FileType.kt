/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.input

import java.util.*

class FileType(val extensions: Array<Pair<String, String>>) {
    constructor(vararg extensions: String) : this(parseExtensions(extensions)) {
    }

    companion object {
        val IMAGE = FileType("*.png", "PNG File")
        val MUSIC = FileType("*.ogg", "ogg-Vorbis File", "*.mp3", "MP3 File",
                "*.wav", "Wave File")

        private fun parseExtensions(array: Array<out String>): Array<Pair<String, String>> {
            if (array.size % 2 != 0) {
                throw IllegalArgumentException("Array has to have even length")
            }
            val extensions = ArrayList<Pair<String, String>>(array.size shr 1)
            for (i in 0..(array.size shr 1) - 1) {
                val j = i shl 1
                extensions += Pair(array[j], array[j + 1])
            }
            return extensions.toTypedArray()
        }
    }
}
