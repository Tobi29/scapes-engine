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

package org.tobi29.scapes.engine.utils.tag

fun TagMap.write(writer: TagStructureWriter) {
    for ((key, value) in this) {
        if (value is TagMap) {
            if (value.isEmpty()) {
                writer.structureEmpty(key)
            } else {
                writer.beginStructure(key)
                value.write(writer)
                writer.endStructure()
            }
        } else if (value is TagList) {
            value.write(writer, key)
        } else if (value is TagPrimitive) {
            writer.writePrimitiveTag(key, value)
        } else {
            throw IllegalArgumentException("Invalid value: $value")
        }
    }
}

fun TagList.write(writer: TagStructureWriter,
                  key: String) {
    if (size > 0) {
        writer.beginList(key)
        for (i in 0 until size - 1) {
            val element = this[i]
            if (element is TagMap) {
                if (element.isEmpty()) {
                    writer.structureEmpty()
                } else {
                    writer.beginListStructure()
                    element.write(writer)
                    writer.endStructure()
                }
            } else if (element is TagList) {
                element.write(writer)
            } else if (element is TagPrimitive) {
                writer.writePrimitiveTag(element)
            } else {
                throw IllegalArgumentException("Invalid element: $element")
            }
        }
        val element = this[size - 1]
        if (element is TagMap) {
            if (element.isEmpty()) {
                writer.endListWithEmpty()
            } else {
                writer.beginListStructure()
                element.write(writer)
                writer.endListWithTerminate()
            }
        } else if (element is TagList) {
            element.write(writer)
            writer.endList()
        } else if (element is TagPrimitive) {
            writer.writePrimitiveTag(element)
            writer.endList()
        } else {
            throw IllegalArgumentException("Invalid element: $element")
        }
    } else {
        writer.listEmpty(key)
    }
}

fun TagList.write(writer: TagStructureWriter) {
    if (size > 0) {
        writer.beginList()
        for (i in 0 until size - 1) {
            val element = this[i]
            if (element is TagMap) {
                if (element.isEmpty()) {
                    writer.structureEmpty()
                } else {
                    writer.beginListStructure()
                    element.write(writer)
                    writer.endStructure()
                }
            } else if (element is TagList) {
                element.write(writer)
            } else if (element is TagPrimitive) {
                writer.writePrimitiveTag(element)
            } else {
                throw IllegalArgumentException("Invalid element: $element")
            }
        }
        val element = this[size - 1]
        if (element is TagMap) {
            if (element.isEmpty()) {
                writer.endListWithEmpty()
            } else {
                writer.beginListStructure()
                element.write(writer)
                writer.endListWithTerminate()
            }
        } else if (element is TagList) {
            element.write(writer)
            writer.endList()
        } else if (element is TagPrimitive) {
            writer.writePrimitiveTag(element)
            writer.endList()
        } else {
            throw IllegalArgumentException("Invalid element: $element")
        }
    } else {
        writer.listEmpty()
    }
}
