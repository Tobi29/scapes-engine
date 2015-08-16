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

import org.tobi29.scapes.engine.utils.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for storing tree data structures in streams
 * <p>
 * Each {@code TagStructure} contains a {@code Map} which is indirectly accessed
 * with setters and getters to prevent unsupported types in the map
 * <p>
 * The read and write function allow all contents to be written into any kind of
 * {@code OutputStream} and recreated at a later point
 * <p>
 * The binary format used for storing data allows compression which does not close the
 * {@code OutputStream} to make it usable in networking code
 */
public class TagStructure {
    private static final byte[] EMPTY_BYTE = new byte[0];
    private Map<String, Object> tags = new ConcurrentHashMap<>();

    /**
     * Returns the {@code boolean} that is mapped to that key
     * <p>
     * If no value is found or the value has the wrong type, it returns
     * <tt>false</tt>
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key or <tt>false</tt> if no valid value
     * was found
     */
    public boolean getBoolean(String key) {
        Object tag = tags.get(key);
        return tag instanceof Boolean && (boolean) tag;
    }

    /**
     * Returns the {@code byte} that is mapped to that key
     * <p>
     * If no value is found or the value has the wrong type, it returns
     * <tt>0</tt>
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key or <tt>0</tt> if no valid value was
     * found
     */
    public byte getByte(String key) {
        Object tag = tags.get(key);
        return tag instanceof Number ? ((Number) tag).byteValue() : 0;
    }

    /**
     * Returns the {@code byte[]} that is mapped to that key
     * <p>
     * If no value is found or the value has the wrong type, it returns an empty
     * {@code byte[]}
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key or an empty {@code byte[]} if no
     * valid value was found
     */
    public byte[] getByteArray(String key) {
        Object tag = tags.get(key);
        return tag instanceof byte[] ? (byte[]) tag : EMPTY_BYTE;
    }

    /**
     * Returns the {@code short} that is mapped to that key
     * <p>
     * If no value is found or the value has the wrong type, it returns
     * <tt>0</tt>
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key or <tt>0</tt> if no valid value was
     * found
     */
    public short getShort(String key) {
        Object tag = tags.get(key);
        return tag instanceof Number ? ((Number) tag).shortValue() : 0;
    }

    /**
     * Returns the {@code int} that is mapped to that key
     * <p>
     * If no value is found or the value has the wrong type, it returns
     * <tt>0</tt>
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key or <tt>0</tt> if no valid value was
     * found
     */
    public int getInteger(String key) {
        Object tag = tags.get(key);
        return tag instanceof Number ? ((Number) tag).intValue() : 0;
    }

    /**
     * Returns the {@code float} that is mapped to that key
     * <p>
     * If no value is found or the value has the wrong type, it returns
     * <tt>0</tt>
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key or <tt>0</tt> if no valid value was
     * found
     */
    public float getFloat(String key) {
        Object tag = tags.get(key);
        return tag instanceof Number ? ((Number) tag).floatValue() : 0;
    }

    /**
     * Returns the {@code long} that is mapped to that key
     * <p>
     * If no value is found or the value has the wrong type, it returns
     * <tt>0</tt>
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key or <tt>0</tt> if no valid value was
     * found
     */
    public long getLong(String key) {
        Object tag = tags.get(key);
        return tag instanceof Number ? ((Number) tag).longValue() : 0;
    }

    /**
     * Returns the {@code double} that is mapped to that key
     * <p>
     * If no value is found or the value has the wrong type, it returns
     * <tt>0</tt>
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key or <tt>0</tt> if no valid value was
     * found
     */
    public double getDouble(String key) {
        Object tag = tags.get(key);
        return tag instanceof Number ? ((Number) tag).doubleValue() : 0;
    }

    /**
     * Returns the {@code String} that is mapped to that key
     * <p>
     * If no value is found or the value has the wrong type, it returns an empty
     * {@code String}
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key or an empty {@code String} if no
     * valid value was found
     */
    public String getString(String key) {
        Object tag = tags.get(key);
        return tag instanceof String ? (String) tag : "";
    }

    /**
     * Returns the {@code TagStructure} that is mapped to that key
     * <p>
     * If no value is found or the value has the wrong type, it returns an empty
     * {@code TagStructure} and maps it to the specified key
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key or an empty {@code TagStructure} if
     * no valid value was found
     */
    public TagStructure getStructure(String key) {
        Object tag = tags.get(key);
        if (tag != null) {
            if (tag instanceof TagStructure) {
                return (TagStructure) tag;
            }
        }
        TagStructure newTag = new TagStructure();
        tags.put(key, newTag);
        return newTag;
    }

    /**
     * Returns the {@code List} that is mapped to that key
     * <p>
     * This {@code List} only contains {@code TagStructure} elements
     * <p>
     * If no value is found or the value has the wrong type, it returns an empty
     * {@code List}
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key or an empty {@code List} if no valid
     * value was found
     */
    public List<TagStructure> getList(String key) {
        Object tag = tags.get(key);
        return tag instanceof StructureList ? (StructureList) tag :
                Collections.emptyList();
    }

    /**
     * Reads the given {@code MultiTag} from the {@code TagStructure} mapped to
     * the given key
     *
     * @param key   The key whose value will be read
     * @param value The {@code MultiTag} object that reads from the mapped
     *              {@code TagStructure}
     * @return The given value
     */
    public <E extends MultiTag.Readable> E getMultiTag(String key, E value) {
        value.read(getStructure(key));
        return value;
    }

    /**
     * Returns the {@code UUID} mapped to the given key, internally using a
     * {@code MultiTag} to read the value
     *
     * @param key The key whose value will be returned
     * @return The value mapped to the key
     */
    public UUID getUUID(String key) {
        return MultiTag.readUUID(getStructure(key));
    }

    /**
     * Returns an {@code Set} of all {@code Object} entries in this {@code
     * TagStructure}
     * <p>
     * Note: It is not recommended to attempt to change the contents this {@code
     * Set}
     *
     * @return {@code Set} of all {@code Object} entries in this {@code
     * TagStructure}
     */
    public Set<Map.Entry<String, Object>> getTagEntrySet() {
        return Collections.unmodifiableSet(tags.entrySet());
    }

    /**
     * Maps the given {@code boolean} to the key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code boolean} that was mapped
     */
    public boolean setBoolean(String key, boolean value) {
        tags.put(key, value);
        return value;
    }

    /**
     * Maps the given {@code byte} to the key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code byte} that was mapped
     */
    public byte setByte(String key, byte value) {
        tags.put(key, value);
        return value;
    }

    /**
     * Maps the given {@code byte[]} to the key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code byte[]} that was mapped
     */
    public byte[] setByteArray(String key, byte... value) {
        Objects.requireNonNull(value);
        tags.put(key, value);
        return value;
    }

    /**
     * Maps the given {@code short} to the key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code short} that was mapped
     */
    public short setShort(String key, short value) {
        tags.put(key, value);
        return value;
    }

    /**
     * Maps the given {@code int} to the key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code int} that was mapped
     */
    public int setInteger(String key, int value) {
        tags.put(key, value);
        return value;
    }

    /**
     * Maps the given {@code float} to the key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code float} that was mapped
     */
    public float setFloat(String key, float value) {
        tags.put(key, value);
        return value;
    }

    /**
     * Maps the given {@code long} to the key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code long} that was mapped
     */
    public long setLong(String key, long value) {
        tags.put(key, value);
        return value;
    }

    /**
     * Maps the given {@code double} to the key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code double} that was mapped
     */
    public double setDouble(String key, double value) {
        tags.put(key, value);
        return value;
    }

    /**
     * Maps the given {@code String} to the key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code String} that was mapped
     */
    public String setString(String key, String value) {
        Objects.requireNonNull(value);
        tags.put(key, value);
        return value;
    }

    /**
     * Maps the given {@code TagStructure} to the key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code TagStructure} that was mapped
     */
    public TagStructure setStructure(String key, TagStructure value) {
        tags.put(key, value);
        return value;
    }

    /**
     * Maps the given {@code List} to the key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code List} that was mapped
     */
    public List<TagStructure> setList(String key, List<TagStructure> value) {
        StructureList list = new StructureList(value.size());
        list.addAll(value);
        tags.put(key, list);
        return value;
    }

    /**
     * Writes the given {@code MultiTag} into a {@code TagStructure} and maps it
     * to the given key
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the {@code TagStructure} will be mapped to
     * @param value The {@code MultiTag} that will be written
     * @return The {@code MultiTag} that was written
     */
    public <E extends MultiTag.Writeable> E setMultiTag(String key, E value) {
        setStructure(key, value.write());
        return value;
    }

    /**
     * Maps the given {@code UUID} to the key, internally using a {@code
     * MultiTag} to write the value
     * <p>
     * If a value was already mapped to the key, it will be overridden
     *
     * @param key   The key that the value will be mapped to
     * @param value The value that will be mapped
     * @return The {@code UUID} that was mapped
     */
    public UUID setUUID(String key, UUID value) {
        setStructure(key, MultiTag.writeUUID(value));
        return value;
    }

    /**
     * Moves the value of the given key to another key
     * <p>
     * Note: If the no value was found this method does nothing
     *
     * @param from The key to take the value from
     * @param to   The key to map the value to
     */
    public void move(String from, String to) {
        Object tag = tags.remove(from);
        if (tag != null) {
            tags.put(to, tag);
        }
    }

    /**
     * Removes the value mapped to the given key
     *
     * @param key The key of the value that will be removed
     */
    public void remove(String key) {
        tags.remove(key);
    }

    /**
     * Returns whether or not the {@code TagStructure} contains this key
     *
     * @param key The key that will be checked
     * @return <tt>true</tt> if the key was found
     */
    public boolean has(String key) {
        return tags.containsKey(key);
    }

    /**
     * Copies all contents of the {@code TagStructure}
     * <p>
     * Note: {@code TagStructure}, {@code List} and {@code byte[]} objects will
     * be copied as well, so modifying them does not affect the original
     *
     * @return A copy of this {@code TagStructure}
     */
    public TagStructure copy() {
        TagStructure tag = new TagStructure();
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof TagStructure) {
                tag.tags.put(entry.getKey(), ((TagStructure) value).copy());
            } else if (value instanceof StructureList) {
                StructureList original = (StructureList) value;
                StructureList list = new StructureList(original.size());
                list.addAll(original);
                tags.put(entry.getKey(), list);
            } else if (value instanceof byte[]) {
                byte[] array = (byte[]) value;
                byte[] copy = new byte[array.length];
                System.arraycopy(array, 0, copy, 0, array.length);
                tag.tags.put(entry.getKey(), copy);
            } else {
                tag.tags.put(entry.getKey(), value);
            }
        }
        return tag;
    }

    /**
     * Writes this {@code TagStructure}
     *
     * @param writer The {@code TagStructureWriter} used to write the data
     * @throws IOException Thrown when an I/O exception occurred
     */
    public void write(TagStructureWriter writer) throws IOException {
        writer.begin(this);
        writeData(writer);
        writer.end();
    }

    /**
     * Reads this {@code TagStructure}
     *
     * @param reader The {@code TagStructureReader} used to read the data
     * @throws IOException Thrown when an I/O exception occurred
     */
    public void read(TagStructureReader reader) throws IOException {
        reader.begin();
        readData(reader);
        reader.end();
    }

    @Override
    public int hashCode() {
        int hash = 31;
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof byte[]) {
                hash += 31 * hash + Arrays.hashCode((byte[]) value);
            } else {
                hash += 31 * hash + entry.getKey().hashCode();
                hash += 31 * hash + entry.getValue().hashCode();
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TagStructure) {
            TagStructure tagStructure = (TagStructure) obj;
            for (Map.Entry<String, Object> entry : tags.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof byte[]) {
                    Object other = tagStructure.tags.get(entry.getKey());
                    if (other instanceof byte[]) {
                        if (!Arrays.equals((byte[]) value, (byte[]) other)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    if (!entry.getValue()
                            .equals(tagStructure.tags.get(entry.getKey()))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void writeData(TagStructureWriter writer) throws IOException {
        for (Map.Entry<String, Object> entry : tags.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof TagStructure) {
                TagStructure structure = (TagStructure) value;
                if (structure.tags.isEmpty()) {
                    writer.structureEmpty(entry.getKey());
                } else {
                    writer.beginStructure(entry.getKey());
                    structure.writeData(writer);
                    writer.endStructure();
                }
            } else if (value instanceof StructureList) {
                StructureList list = (StructureList) value;
                int size = list.size();
                if (size > 0) {
                    size--;
                    writer.beginList(entry.getKey());
                    for (int i = 0; i < size; i++) {
                        TagStructure structure = list.get(i);
                        if (structure.tags.isEmpty()) {
                            writer.structureEmpty();
                        } else {
                            writer.beginStructure();
                            structure.writeData(writer);
                            writer.endStructure();
                        }
                    }
                    TagStructure structure = list.get(size);
                    if (structure.tags.isEmpty()) {
                        writer.endListWithEmpty();
                    } else {
                        writer.beginStructure();
                        structure.writeData(writer);
                        writer.endListWidthTerminate();
                    }
                } else {
                    writer.listEmpty(entry.getKey());
                }
            } else {
                writer.writeTag(entry.getKey(), value);
            }
        }
    }

    private TagStructureReader.SpecialNext readData(TagStructureReader reader)
            throws IOException {
        Map<String, Object> newTags = new ConcurrentHashMap<>();
        while (true) {
            Pair<String, Object> next = reader.next();
            if (next == null) {
                continue;
            }
            Object tag = next.b;
            if (tag == TagStructureReader.SpecialNext.STRUCTURE) {
                reader.beginStructure();
                TagStructure structure = new TagStructure();
                structure.readData(reader);
                reader.endStructure();
                tag = structure;
            } else if (tag ==
                    TagStructureReader.SpecialNext.STRUCTURE_TERMINATE) {
                tags = newTags;
                return TagStructureReader.SpecialNext.STRUCTURE_TERMINATE;
            } else if (tag == TagStructureReader.SpecialNext.STRUCTURE_EMPTY) {
                tag = new TagStructure();
            } else if (tag == TagStructureReader.SpecialNext.LIST) {
                StructureList list = new StructureList();
                reader.beginList();
                while (true) {
                    TagStructure structure = new TagStructure();
                    list.add(structure);
                    if (structure.readData(reader) ==
                            TagStructureReader.SpecialNext.LIST_TERMINATE) {
                        break;
                    }
                    reader.endStructure();
                    reader.beginStructure();
                }
                reader.endList();
                tag = list;
            } else if (tag == TagStructureReader.SpecialNext.LIST_TERMINATE) {
                tags = newTags;
                return TagStructureReader.SpecialNext.LIST_TERMINATE;
            } else if (tag == TagStructureReader.SpecialNext.LIST_EMPTY) {
                tag = new StructureList();
            }
            newTags.put(next.a, tag);
        }
    }

    @SuppressWarnings("ClassExtendsConcreteCollection")
    public static class StructureList extends ArrayList<TagStructure> {
        private StructureList() {
        }

        private StructureList(int size) {
            super(size);
        }

        @Override
        public Object clone() {
            return super.clone();
        }
    }
}
