/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.swt.util

import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.FontData
import org.tobi29.scapes.engine.utils.assert
import org.tobi29.scapes.engine.utils.logging.KLogging
import org.tobi29.scapes.engine.utils.toArray

object Fonts : KLogging() {
    private val MONOSPACED = fontDatas(monospaceFonts)

    val monospace: Array<FontData>
        get() = MONOSPACED.copyOf()

    private fun fontDatas(map: Map<String, String>): Array<FontData> {
        val os = identifier(System.getProperty("os.name"))
        val ws = identifier(SWT.getPlatform())
        var fonts: String? = map["${os}_$ws"]
        if (fonts == null) {
            fonts = map[os]
        }
        if (fonts == null) {
            logger.warn { "Unable to identify OS, using fallback." }
            fonts = map["unknown"].orEmpty()
        }
        return parseFontString(fonts)
    }

    private fun parseFontString(fonts: String): Array<FontData> {
        val fontDataTexts = fonts.split(';')
        return fontDataTexts.asSequence().map { text ->
            val split = text.split('|', limit = 3)
            assert { split.size == 3 }
            val name = split[0]
            val styles = split[1].split(',')
            var style = 0
            for (s in styles) {
                when (s) {
                    "normal" -> style = style or SWT.NORMAL
                    "bold" -> style = style or SWT.BOLD
                    "italic" -> style = style or SWT.ITALIC
                }
            }
            val height = split[2].toInt()
            FontData(name, height, style)
        }.toArray()
    }

    private fun identifier(str: String): String {
        val characters = CharArray(str.length)
        var i = 0
        for (j in 0 until str.length) {
            val character = str[j]
            if (!Character.isWhitespace(character)) {
                characters[i++] = Character.toLowerCase(character)
            }
        }
        return String(characters, 0, i)
    }
}

private val monospaceFonts = HashMap<String, String>().apply {
    this["aix"] = "adobe-courier|normal|12"
    this["hp-ux"] = "adobe-courier|normal|14"
    this["linux_gtk"] = "Monospace|normal|10"
    this["linux"] = "adobe-courier|normal|12"
    this["macosx"] = "Monaco|normal|11;Courier|normal|12;Courier New|normal|12"
    this["sunos"] = "adobe-courier|normal|12"
    this["solaris"] = "adobe-courier|normal|12"
    this["windows98"] = "Courier New|normal|10;Courier|normal|10;Lucida Console|normal|9"
    this["windowsnt"] = "Courier New|normal|10;Courier|normal|10;Lucida Console|normal|9"
    this["windows2000"] = "Courier New|normal|10;Courier|normal|10;Lucida Console|normal|9"
    this["windowsxp"] = "Courier New|normal|10;Courier|normal|10;Lucida Console|normal|9"
    this["windowsvista"] = "Consolas|normal|10;Courier New|normal|10"
    this["windows7"] = "Consolas|normal|10;Courier New|normal|10"
    this["windows8"] = "Consolas|normal|10;Courier New|normal|10"
    this["unknown"] = "Courier New|normal|10;Courier|normal|10;b&h-lucidabright|normal|9"
}
