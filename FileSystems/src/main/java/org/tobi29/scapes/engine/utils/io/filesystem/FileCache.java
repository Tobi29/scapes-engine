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
package org.tobi29.scapes.engine.utils.io.filesystem;

import java8.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.tobi29.scapes.engine.utils.ArrayUtil;
import org.tobi29.scapes.engine.utils.io.*;

import java.io.IOException;
import java.security.MessageDigest;

/**
 * Utility class for managing data in a file cache
 */
public class FileCache {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(FileCache.class);
    private final FilePath root;
    private final Duration time;

    /**
     * Creates a new {@link FileCache}
     *
     * @param root The root directory that the cache will be saved into, will be
     *             created if it doesn't exist
     */
    public FileCache(FilePath root) throws IOException {
        this(root, Duration.ofDays(16));
    }

    /**
     * Creates a new {@link FileCache}
     *
     * @param root The root directory that the cache will be saved into, will be
     *             created if it doesn't exist
     * @param time Time in milliseconds until a file will be treated as old and
     *             is deleted on {@linkplain #check()}
     */
    public FileCache(FilePath root, Duration time) throws IOException {
        FileUtil.createDirectories(root);
        this.root = root;
        this.time = time;
    }

    /**
     * Take a {@link ReadableByteStream} from the resource, reads it and write
     * its data into a file in the cache
     *
     * @param resource {@link ReadSource} that will be read until it ends and
     *                 will be closed
     * @param type     The type of data that will be stored, to organize the
     *                 cache
     * @return A {@link Location} to later access the stored data,
     * containing the checksum of the written file
     * @throws IOException If an I/O error occurred
     */
    public synchronized Location store(ReadSource resource, String type)
            throws IOException {
        return resource.readReturn(stream -> store(stream, type));
    }

    /**
     * Reads the given {@link ReadableByteStream} and write its data into a file
     * in the cache
     *
     * @param stream {@link ReadableByteStream} that will be read until it ends
     *               and will be closed
     * @param type   The type of data that will be stored, to organize the cache
     * @return A {@link Location} to later access the stored data, containing
     * the checksum of the written file
     * @throws IOException If an I/O error occurred
     */
    public synchronized Location store(ReadableByteStream stream, String type)
            throws IOException {
        FilePath write = FileUtil.createTempFile("CacheWrite", ".jar");
        return FileUtil.tempChannelReturn(write, channel -> {
            MessageDigest digest = ChecksumUtil.Algorithm.SHA256.digest();
            BufferedWriteChannelStream streamOut =
                    new BufferedWriteChannelStream(channel);
            ProcessStream.process(stream, buffer -> {
                digest.update(buffer);
                buffer.rewind();
                streamOut.put(buffer);
            });
            streamOut.flush();
            channel.position(0);
            byte[] checksum = digest.digest();
            FilePath parent = root.resolve(type);
            FileUtil.createDirectories(parent);
            String name = ArrayUtil.toHexadecimal(checksum);
            BufferedReadChannelStream streamIn =
                    new BufferedReadChannelStream(channel);
            FileUtil.write(parent.resolve(name),
                    output -> ProcessStream.process(streamIn, output::put));
            return new Location(type, checksum);
        });
    }

    /**
     * Gives the {@link FilePath} from the {@link Location} in this cache
     *
     * @param location The location that will be looked up
     * @return A {@link FilePath} pointing at the file in cache or empty if the
     * cache does not contain a matching file
     */
    public synchronized Optional<FilePath> retrieve(Location location)
            throws IOException {
        String name = ArrayUtil.toHexadecimal(location.array);
        FilePath file = file(location.type, name);
        if (FileUtil.exists(file)) {
            FileUtil.setLastModifiedTime(file, Instant.now());
            return Optional.of(file);
        }
        return Optional.empty();
    }

    /**
     * Deletes the give location from the cache, does nothing if no matching file is found
     *
     * @param location The location that will be deleted
     */
    public synchronized void delete(Location location) throws IOException {
        String name = ArrayUtil.toHexadecimal(location.array);
        FilePath file = file(location.type, name);
        FileUtil.deleteIfExists(file);
    }

    /**
     * Deletes all files of the given type from the cache
     *
     * @param type The name of the type that will be removed
     */
    public synchronized void delete(String type) throws IOException {
        FileUtil.deleteDir(root.resolve(type));
    }

    /**
     * Checks the entire cache and deletes files that don't match their checksum name or are old
     */
    public synchronized void check() throws IOException {
        Instant currentTime = Instant.now().minus(time);
        for (FilePath invalid : FileUtil
                .listRecursive(root, FileUtil::isRegularFile,
                        FileUtil::isNotHidden, file -> {
                            try {
                                return FileUtil.getLastModifiedTime(file)
                                        .isBefore(currentTime);
                            } catch (IOException e) {
                                return false;
                            }
                        })) {
            FileUtil.deleteIfExists(invalid);
            LOGGER.debug("Deleted old cache entry: {}", invalid);
        }
    }

    private FilePath file(String type, String name) {
        return root.resolve(type + '/' + name);
    }

    /**
     * A location in the cache
     */
    public static class Location {
        private final String type;
        private final byte[] array;

        /**
         * Constructs a new location of the given type and the checksum
         *
         * @param type     Type of the location
         * @param checksum Array containing the checksum name
         */
        public Location(String type, byte[] checksum) {
            this.type = type;
            array = checksum;
        }
    }
}
