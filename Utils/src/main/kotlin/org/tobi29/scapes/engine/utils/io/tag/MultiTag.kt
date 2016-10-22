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

package org.tobi29.scapes.engine.utils.io.tag

import java.util.*

object MultiTag {
    /**
     * Utility method to write a [UUID] into a [TagStructure]
     * @param uuid The [UUID] that will be written
     * @return A newly created [TagStructure] containing the data from the [UUID]
     */
    fun writeUUID(uuid: UUID): TagStructure {
        val tagStructure = TagStructure()
        tagStructure.setNumber("Most", uuid.mostSignificantBits)
        tagStructure.setNumber("Least", uuid.leastSignificantBits)
        return tagStructure
    }

    /**
     * Utility method to write a [TagStructure] into a [UUID]
     * @param tagStructure The [TagStructure] that will be written
     * @return A newly created [UUID] containing the data from [tagStructure]
     */
    fun readUUID(tagStructure: TagStructure): UUID? {
        val most = tagStructure.getNumber("Most")
        val least = tagStructure.getNumber("Least")
        if (most == null || least == null) {
            return null
        }
        return UUID(most.toLong(), least.toLong())
    }

    /**
     * Utility method to write [Properties] into a [TagStructure]
     * @param properties The [Properties] that will be written
     * @return A newly created [TagStructure] containing the data from the [Properties]
     */
    fun writeProperties(properties: Properties): TagStructure {
        val tagStructure = TagStructure()
        for ((key, value) in properties) {
            tagStructure.setString(key.toString(),
                    value.toString())
        }
        return tagStructure
    }

    /**
     * Utility method to write a [TagStructure] into [Properties]
     * @param tagStructure The [TagStructure] that will be written
     * @return Newly created [Properties] containing the data from the [TagStructure]
     */
    fun readProperties(tagStructure: TagStructure): Properties {
        val properties = Properties()
        readProperties(tagStructure, properties)
        return properties
    }

    /**
     * Utility method to write a [TagStructure] into [Properties]
     * @param tagStructure The [TagStructure] that will be written
     * @param properties   Existing [Properties] object to write to
     */
    fun readProperties(tagStructure: TagStructure,
                       properties: Properties) {
        for ((key, value) in tagStructure.tagEntrySet) {
            properties.setProperty(key.toString(),
                    value.toString())
        }
    }

    /**
     * Allows returning object state out of a [TagStructure]
     */
    interface Readable {
        fun read(tagStructure: TagStructure)
    }

    /**
     * Allows writing object state into a [TagStructure]
     */
    interface Writeable {
        fun write(): TagStructure
    }

    /**
     * Shorthand for read and write multi-tags
     */
    interface ReadAndWrite : Readable, Writeable
}
