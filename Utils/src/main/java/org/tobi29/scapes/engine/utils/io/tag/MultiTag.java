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

package org.tobi29.scapes.engine.utils.io.tag;

import org.tobi29.scapes.engine.utils.Streams;

import java.util.Properties;
import java.util.UUID;

/**
 * Utility class for easier usage of complex data structures in tags
 */
public class MultiTag {
    /**
     * Utility method to write a {@link UUID} into a {@link TagStructure}
     *
     * @param uuid The {@link UUID} that will be written
     * @return A newly created {@link TagStructure} containing the data from the
     * {@link UUID}
     */
    public static TagStructure writeUUID(UUID uuid) {
        TagStructure tagStructure = new TagStructure();
        tagStructure.setLong("Most", uuid.getMostSignificantBits());
        tagStructure.setLong("Least", uuid.getLeastSignificantBits());
        return tagStructure;
    }

    /**
     * Utility method to write a {@link TagStructure} into a {@link UUID}
     *
     * @param tagStructure The {@link TagStructure} that will be written
     * @return A newly created {@link UUID} containing the data from the {@link
     * TagStructure}
     */
    public static UUID readUUID(TagStructure tagStructure) {
        return new UUID(tagStructure.getLong("Most"),
                tagStructure.getLong("Least"));
    }

    /**
     * Utility method to write {@link Properties} into a {@link TagStructure}
     *
     * @param properties The {@link Properties} that will be written
     * @return A newly created {@link TagStructure} containing the data from the
     * {@link Properties}
     */
    public static TagStructure writeProperties(Properties properties) {
        TagStructure tagStructure = new TagStructure();
        Streams.forEach(properties.entrySet(), entry -> tagStructure
                .setString(String.valueOf(entry.getKey()),
                        String.valueOf(entry.getValue())));
        return tagStructure;
    }

    /**
     * Utility method to write a {@link TagStructure} into {@link Properties}
     *
     * @param tagStructure The {@link TagStructure} that will be written
     * @return Newly created {@link Properties} containing the data from the
     * {@link TagStructure}
     */
    public static Properties readProperties(TagStructure tagStructure) {
        Properties properties = new Properties();
        readProperties(tagStructure, properties);
        return properties;
    }

    /**
     * Utility method to write a {@link TagStructure} into {@link Properties}
     *
     * @param tagStructure The {@link TagStructure} that will be written
     * @param properties   Existing {@link Properties} object to write to
     */
    public static void readProperties(TagStructure tagStructure,
            Properties properties) {
        Streams.forEach(tagStructure.getTagEntrySet(), entry -> properties
                .setProperty(String.valueOf(entry.getKey()),
                        String.valueOf(entry.getValue())));
    }

    /**
     * Allows returning object state out of a {@link TagStructure}
     */
    public interface Readable {
        void read(TagStructure tagStructure);
    }

    /**
     * Allows writing object state into a {@link TagStructure}
     */
    public interface Writeable {
        TagStructure write();
    }

    /**
     * Shorthand for read and write multi-tags
     */
    public interface ReadAndWrite extends Readable, Writeable {
    }
}
