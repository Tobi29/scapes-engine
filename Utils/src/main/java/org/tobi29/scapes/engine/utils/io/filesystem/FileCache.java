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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.utils.ArrayUtil;
import org.tobi29.scapes.engine.utils.io.ChecksumUtil;
import org.tobi29.scapes.engine.utils.io.ProcessStream;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for managing data in a file cache
 */
public class FileCache {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(FileCache.class);
    private final Path root, temp;
    private final Duration time;

    /**
     * Creates a new {@code FileCache}
     *
     * @param root The root directory that the cache will be saved into, will be created if it doesn't exist
     */
    public FileCache(Path root, Path temp) throws IOException {
        this(root, temp, Duration.ofDays(16));
    }

    /**
     * Creates a new {@code FileCache}
     *
     * @param root The root directory that the cache will be saved into, will be created if it doesn't exist
     * @param time Time until a file will be treated as old and is deleted on {@linkplain #check()}
     */
    public FileCache(Path root, Path temp, Duration time) throws IOException {
        Files.createDirectories(root);
        Files.createDirectories(temp);
        this.root = root;
        this.temp = temp;
        this.time = time;
    }

    /**
     * Reads the given {@code InputStream} and write its data into a file in the cache
     *
     * @param input {@code InputStream} that will be read until it ends and will be closed
     * @param type  The type of data that will be stored, to organize the cache
     * @return A {@code FileCacheLocation} to later access the stored data, containing the checksum of the written file
     * @throws IOException If an I/O error occurred
     */
    public synchronized Location store(ReadableByteStream input, String type)
            throws IOException {
        Path parent = root.resolve(type);
        Files.createDirectories(parent);
        Path write = temp.resolve(UUID.randomUUID().toString());
        byte[] checksum = FileUtil.writeReturn(write, output -> {
            MessageDigest digest = ChecksumUtil.Algorithm.SHA256.digest();
            ProcessStream.process(input, buffer -> {
                digest.update(buffer);
                buffer.rewind();
                output.put(buffer);
            });
            return digest.digest();
        });
        String name = ArrayUtil.toHexadecimal(checksum);
        try {
            Files.move(write, parent.resolve(name));
        } catch (IOException e) {
            LOGGER.warn("Failed to move output file into cache directory");
        }
        return new Location(type, checksum);
    }

    /**
     * Gives the {@code File} from the {@code FileCacheLocation} in this cache
     *
     * @param location The location that will be looked up
     * @return A {@code File} pointing at the file in cache or null if the cache doesn't contain a matching file
     */
    public synchronized Optional<Path> retrieve(Location location)
            throws IOException {
        String name = ArrayUtil.toHexadecimal(location.array);
        Path file = file(location.type, name);
        if (Files.exists(file)) {
            Files.setLastModifiedTime(file, FileTime.from(Instant.now()));
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
        Path file = file(location.type, name);
        Files.deleteIfExists(file);
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
        for (Path directory : Files.newDirectoryStream(root)) {
            if (Files.isDirectory(directory) && !Files.isHidden(directory)) {
                for (Path file : Files.newDirectoryStream(directory)) {
                    if (Files.isRegularFile(file) && !Files.isHidden(file) &&
                            Files.getLastModifiedTime(file).toInstant()
                                    .isBefore(currentTime)) {
                        Files.delete(file);
                        LOGGER.debug("Deleted old cache entry: {}", file);
                    }
                }
            }
        }
    }

    private Path file(String type, String name) {
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
