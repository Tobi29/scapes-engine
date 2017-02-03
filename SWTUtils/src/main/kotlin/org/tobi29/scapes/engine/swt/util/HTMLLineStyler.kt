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
import org.eclipse.swt.custom.LineStyleEvent
import org.eclipse.swt.custom.LineStyleListener
import org.eclipse.swt.custom.StyleRange
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.events.DisposeEvent
import org.eclipse.swt.events.DisposeListener
import org.eclipse.swt.events.VerifyEvent
import org.eclipse.swt.events.VerifyListener
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.widgets.Display
import org.tobi29.scapes.engine.utils.toArray
import java.io.StringReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader

class HTMLLineStyler(display: Display) : DisposeListener, LineStyleListener, VerifyListener {
    private val styles = Collections.newSetFromMap(
            ConcurrentHashMap<StyleRange, Boolean>())
    private val colorTable = ConcurrentHashMap<String, Color>()

    constructor(text: StyledText) : this(text.display) {
        text.addLineStyleListener(this)
        text.addVerifyListener(this)
        text.addDisposeListener(this)
    }

    init {
        colorTable.put("black", display.getSystemColor(SWT.COLOR_BLACK))
        colorTable.put("white", display.getSystemColor(SWT.COLOR_WHITE))
        colorTable.put("darkred", display.getSystemColor(SWT.COLOR_DARK_RED))
        colorTable.put("darkgreen",
                display.getSystemColor(SWT.COLOR_DARK_GREEN))
        colorTable.put("darkyellow",
                display.getSystemColor(SWT.COLOR_DARK_YELLOW))
        colorTable.put("darkblue", display.getSystemColor(SWT.COLOR_DARK_BLUE))
        colorTable.put("darkmagenta",
                display.getSystemColor(SWT.COLOR_DARK_MAGENTA))
        colorTable.put("darkcyan", display.getSystemColor(SWT.COLOR_DARK_CYAN))
        colorTable.put("darkgray", display.getSystemColor(SWT.COLOR_DARK_GRAY))
        colorTable.put("red", display.getSystemColor(SWT.COLOR_RED))
        colorTable.put("green", display.getSystemColor(SWT.COLOR_GREEN))
        colorTable.put("yellow", display.getSystemColor(SWT.COLOR_YELLOW))
        colorTable.put("blue", display.getSystemColor(SWT.COLOR_BLUE))
        colorTable.put("magenta", display.getSystemColor(SWT.COLOR_MAGENTA))
        colorTable.put("cyan", display.getSystemColor(SWT.COLOR_CYAN))
        colorTable.put("gray", display.getSystemColor(SWT.COLOR_GRAY))
    }

    override fun lineGetStyle(event: LineStyleEvent) {
        val start = event.lineOffset
        val end = event.lineOffset + event.lineText.length
        event.styles = styles.asSequence().filter { style ->
            val styleStart = style.start
            val styleEnd = styleStart + style.length
            !(end <= styleStart || start >= styleEnd)
        }.toArray()
    }

    override fun verifyText(e: VerifyEvent) {
        try {
            val reader = FACTORY.createXMLStreamReader(StringReader(e.text))
            val output = StringBuilder(e.text.length)
            val styles = ArrayDeque<Triple<Int, String, (StyleRange, StringBuilder) -> Unit>>()
            while (reader.hasNext()) {
                val event = reader.eventType
                when (event) {
                    XMLStreamConstants.CHARACTERS -> output.append(reader.text)
                    XMLStreamConstants.START_ELEMENT -> {
                        val type = reader.name.localPart
                        when (type) {
                            "p", "span", "b" -> {
                                val start = e.start + output.length
                                styles.add(
                                        Triple(start, type, addStyle(reader)))
                            }
                        }
                    }
                    XMLStreamConstants.END_ELEMENT -> {
                        val type = reader.name.localPart
                        when (type) {
                            "p", "span", "b" -> {
                                val end = e.start + output.length
                                val iterator = styles.descendingIterator()
                                var style: Triple<Int, String, (StyleRange, StringBuilder) -> Unit>? = null
                                while (iterator.hasNext()) {
                                    val next = iterator.next()
                                    if (next.second == type) {
                                        style = next
                                        break
                                    }
                                }
                                if (style != null) {
                                    val styleRange = StyleRange()
                                    styleRange.start = style.first
                                    styleRange.length = end - style.first
                                    style.third(styleRange, output)
                                    this.styles.add(styleRange)
                                }
                            }
                        }
                    }
                }
                reader.next()
            }
            e.text = output.toString()
        } catch (e1: XMLStreamException) {
            e.doit = false
        }

    }

    private fun addStyle(
            reader: XMLStreamReader): (StyleRange, StringBuilder) -> Unit {
        val type = reader.name.localPart
        val count = reader.attributeCount
        val style = Properties()
        for (i in 0..count - 1) {
            if ("style" == reader.getAttributeName(i).localPart) {
                val attribute = reader.getAttributeValue(i).split(
                        ";".toRegex()).dropLastWhile(
                        String::isEmpty).toTypedArray()
                for (property in attribute) {
                    val split = property.split(":".toRegex(), 2).toTypedArray()
                    if (split.size == 2) {
                        style.setProperty(split[0].trim { it <= ' ' },
                                split[1].trim { it <= ' ' })
                    }
                }
            }
        }
        val colorName = style.getProperty("color")
        val color = colorName?.let { colorTable[it] }
        when (type) {
            "p", "span" -> return { styleRange, text ->
                color?.let { styleRange.foreground = it }
                text.append('\n')
            }
            "b" -> return { styleRange, text ->
                color?.let { styleRange.foreground = it }
                styleRange.fontStyle = styleRange.fontStyle or SWT.BOLD
            }
        }
        return { styleRange, text -> }
    }

    override fun widgetDisposed(e: DisposeEvent) {
    }

    companion object {
        private val FACTORY = XMLInputFactory.newInstance()
    }
}
