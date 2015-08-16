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

package org.tobi29.scapes.engine.utils.tests.util;

import org.tobi29.scapes.engine.utils.io.tag.TagStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class TagStructureTemplate {
    private TagStructureTemplate() {
    }

    public static TagStructure createTagStructure() {
        Random random = new Random();
        TagStructure tagStructure = new TagStructure();
        // All primitive tags
        tagStructure.setBoolean("Boolean", random.nextBoolean());
        tagStructure.setByte("Byte", (byte) random.nextInt(0x100));
        byte[] array = new byte[1024];
        random.nextBytes(array);
        tagStructure.setByteArray("Byte[]", array);
        tagStructure.setShort("Short", (short) random.nextInt(0x10000));
        tagStructure.setInteger("Integer", random.nextInt());
        tagStructure.setLong("Long", random.nextLong());
        tagStructure.setFloat("Float", random.nextFloat());
        tagStructure.setDouble("Double", random.nextDouble());
        tagStructure.setString("String", "◊Blah blah blah◊");
        // Filled structure and list
        TagStructure childStructure = tagStructure.getStructure("Structure");
        for (int i = 0; i < 256; i++) {
            childStructure.setByte("Entry#" + i, (byte) i);
        }
        List<TagStructure> childList = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            TagStructure listEntry = new TagStructure();
            listEntry.setInteger("Entry#" + i, i);
            childList.add(listEntry);
        }
        tagStructure.setList("List", childList);
        // Empty structure and list
        tagStructure.getStructure("EmptyStructure");
        tagStructure.setList("EmptyList", new ArrayList<>());
        return tagStructure;
    }
}
