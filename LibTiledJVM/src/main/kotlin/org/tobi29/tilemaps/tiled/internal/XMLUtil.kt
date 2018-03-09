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

package org.tobi29.tilemaps.tiled.internal

import org.tobi29.io.IOException
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.Vector2i
import org.w3c.dom.Node
import org.w3c.dom.NodeList

internal fun Node.getAttributeValue(
    name: String
): String? {
    val attributes = attributes
    var value: String? = null
    if (attributes != null) {
        val attribute = attributes.getNamedItem(name)
        if (attribute != null) {
            value = attribute.nodeValue
        }
    }
    return value
}

internal fun Node.getAttributeInt(
    name: String
): Int? = getAttributeValue(name)?.toIntOrNull()

internal fun Node.getAttributeDouble(
    name: String
): Double? = getAttributeValue(name)?.toDoubleOrNull()

internal fun Node.getAttributeVector2i(
    nameX: String,
    nameY: String
): Vector2i? = getAttributeInt(nameX)?.let { x ->
    getAttributeInt(nameY)?.let { y -> Vector2i(x, y) }
}

internal fun Node.getAttributeVector2d(
    nameX: String,
    nameY: String
): Vector2d? = getAttributeDouble(nameX)?.let { x ->
    getAttributeDouble(nameY)?.let { y -> Vector2d(x, y) }
}

internal fun Node.requireAttributeValue(
    name: String
): String {
    val attributes = attributes
    var value: String? = null
    if (attributes != null) {
        val attribute = attributes.getNamedItem(name)
        if (attribute != null) {
            value = attribute.nodeValue
        }
    }
    return value ?: throw IOException("Missing attribute: $name")
}

internal fun Node.requireAttributeInt(
    name: String
): Int = try {
    requireAttributeValue(name).toInt()
} catch (e: NumberFormatException) {
    throw IOException(e)
}

internal fun Node.requireAttributeDouble(
    name: String
): Double = try {
    requireAttributeValue(name).toDouble()
} catch (e: NumberFormatException) {
    throw IOException(e)
}

internal fun Node.requireAttributeVector2i(
    nameX: String,
    nameY: String
): Vector2i = requireAttributeInt(nameX).let { x ->
    requireAttributeInt(nameY).let { y -> Vector2i(x, y) }
}

internal fun Node.requireAttributeVector2d(
    nameX: String,
    nameY: String
): Vector2d = requireAttributeDouble(nameX).let { x ->
    requireAttributeDouble(nameY).let { y -> Vector2d(x, y) }
}

internal fun String.parseVector2iList(): List<Vector2i> =
    splitToSequence(' ').map { it.trim() }.map {
        val chunk = it.split(',')
        if (chunk.size != 2) throw IOException("Uneven number of values")
        try {
            Vector2i(chunk[0].toInt(), chunk[1].toInt())
        } catch (e: NumberFormatException) {
            throw IOException(e)
        }
    }.toList()

internal inline val Node.nodeNameL: String get() = nodeName.toLowerCase()

internal inline fun NodeList.forEach(block: (Node) -> Unit) {
    for (i in 0 until length) {
        block(item(i))
    }
}

internal inline fun NodeList.forEachElement(block: (Node) -> Unit) {
    forEach { child ->
        if (child.nodeType == Node.ELEMENT_NODE) block(child)
    }
}
