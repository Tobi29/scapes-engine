/*
 * Copyright 2012-2015 Tobi29
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
package org.tobi29.scapes.engine.utils.io.tag;

import org.tobi29.scapes.engine.utils.Streams;

import java.util.Properties;
import java.util.UUID;

/**
 * Interface to make types usable as {@code MultiTag}s
 */
public class MultiTag {
    /**
     * Utility method to write a {@code UUID} into a {@code TagStructure}
     *
     * @param uuid The {@code UUID} that will be written
     * @return A newly created {@code TagStructure} containing the data from the
     * {@code UUID}
     */
    public static TagStructure writeUUID(UUID uuid) {
        TagStructure tagStructure = new TagStructure();
        tagStructure.setLong("Most", uuid.getMostSignificantBits());
        tagStructure.setLong("Least", uuid.getLeastSignificantBits());
        return tagStructure;
    }

    /**
     * Utility method to write a {@code TagStructure} into a {@code UUID}
     *
     * @param tagStructure The {@code TagStructure} that will be written
     * @return A newly created {@code UUID} containing the data from the {@code
     * TagStructure}
     */
    public static UUID readUUID(TagStructure tagStructure) {
        return new UUID(tagStructure.getLong("Most"),
                tagStructure.getLong("Least"));
    }

    /**
     * Utility method to write {@code Properties} into a {@code TagStructure}
     *
     * @param properties The {@code Properties} that will be written
     * @return A newly created {@code TagStructure} containing the data from the
     * {@code Properties}
     */
    public static TagStructure writeProperties(Properties properties) {
        TagStructure tagStructure = new TagStructure();
        Streams.forEach(properties.entrySet(), entry -> tagStructure
                .setString(String.valueOf(entry.getKey()),
                        String.valueOf(entry.getValue())));
        return tagStructure;
    }

    /**
     * Utility method to write a {@code TagStructure} into {@code Properties}
     *
     * @param tagStructure The {@code TagStructure} that will be written
     * @return Newly created {@code Properties} containing the data from the
     * {@code TagStructure}
     */
    public static Properties readProperties(TagStructure tagStructure) {
        Properties properties = new Properties();
        readProperties(tagStructure, properties);
        return properties;
    }

    /**
     * Utility method to write a {@code TagStructure} into {@code Properties}
     *
     * @param tagStructure The {@code TagStructure} that will be written
     * @param properties   Existing {@code Properties} object to write to
     */
    public static void readProperties(TagStructure tagStructure,
            Properties properties) {
        Streams.forEach(tagStructure.getTagEntrySet(), entry -> properties
                .setProperty(String.valueOf(entry.getKey()),
                        String.valueOf(entry.getValue())));
    }

    public interface Readable {
        void read(TagStructure tagStructure);
    }

    public interface Writeable {
        TagStructure write();
    }

    public interface ReadAndWrite extends Readable, Writeable {
    }
}
