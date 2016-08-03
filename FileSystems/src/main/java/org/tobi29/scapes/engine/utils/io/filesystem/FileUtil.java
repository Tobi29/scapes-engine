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

package org.tobi29.scapes.engine.utils.io.filesystem;

import java8.util.function.Predicate;
import java8.util.stream.Stream;
import org.threeten.bp.Instant;
import org.tobi29.scapes.engine.utils.UnsupportedJVMException;
import org.tobi29.scapes.engine.utils.io.IOConsumer;
import org.tobi29.scapes.engine.utils.io.IOFunction;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;
import org.tobi29.scapes.engine.utils.io.filesystem.spi.FileSystemProvider;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.zip.ZipFile;

public final class FileUtil {
    private static final FileUtilImpl IMPL = loadService();

    private FileUtil() {
    }

    private static FileUtilImpl loadService() {
        for (FileSystemProvider filesystem : ServiceLoader
                .load(FileSystemProvider.class)) {
            try {
                if (filesystem.available()) {
                    return filesystem.implementation();
                }
            } catch (ServiceConfigurationError e) {
            }
        }
        throw new UnsupportedJVMException(
                "No filesystem implementation available");
    }

    public static FilePath path(String path) {
        return IMPL.path(path);
    }

    public static ReadSource read(FilePath path) {
        return IMPL.read(path);
    }

    public static void read(FilePath path, IOConsumer<ReadableByteStream> read)
            throws IOException {
        IMPL.read(path, read);
    }

    public static <R> R readReturn(FilePath path,
            IOFunction<ReadableByteStream, R> read) throws IOException {
        return IMPL.readReturn(path, read);
    }

    public static void write(FilePath path,
            IOConsumer<WritableByteStream> write) throws IOException {
        IMPL.write(path, write);
    }

    public static <R> R writeReturn(FilePath path,
            IOFunction<WritableByteStream, R> write) throws IOException {
        return IMPL.writeReturn(path, write);
    }

    public static FilePath createDirectories(FilePath path) throws IOException {
        return IMPL.createDirectories(path);
    }

    public static void delete(FilePath path) throws IOException {
        IMPL.delete(path);
    }

    public static boolean deleteIfExists(FilePath path) throws IOException {
        return IMPL.deleteIfExists(path);
    }

    public static void deleteDir(FilePath path) throws IOException {
        IMPL.deleteDir(path);
    }

    public static boolean exists(FilePath path) {
        return IMPL.exists(path);
    }

    public static boolean isRegularFile(FilePath path) {
        return IMPL.isRegularFile(path);
    }

    public static boolean isDirectory(FilePath path) {
        return IMPL.isDirectory(path);
    }

    public static boolean isHidden(FilePath path) {
        return IMPL.isHidden(path);
    }

    public static boolean isNotHidden(FilePath path) {
        return !isHidden(path);
    }

    public static FilePath createTempFile(String prefix, String suffix)
            throws IOException {
        return IMPL.createTempFile(prefix, suffix);
    }

    public static FilePath copy(FilePath source, FilePath target)
            throws IOException {
        return IMPL.copy(source, target);
    }

    public static FilePath move(FilePath source, FilePath target)
            throws IOException {
        return IMPL.move(source, target);
    }

    public static void stream(FilePath path,
            IOConsumer<Stream<FilePath>> consumer) throws IOException {
        IMPL.stream(path, consumer);
    }

    public static void consume(FilePath path, IOConsumer<FilePath> consumer)
            throws IOException {
        IMPL.stream(path, paths -> {
            Iterator<FilePath> iterator = paths.iterator();
            while (iterator.hasNext()) {
                consumer.accept(iterator.next());
            }
        });
    }

    public static List<FilePath> list(FilePath path) throws IOException {
        return IMPL.list(path);
    }

    @SafeVarargs
    public static List<FilePath> list(FilePath path,
            Predicate<FilePath>... filters) throws IOException {
        return IMPL.list(path, filters);
    }

    public static void streamRecursive(FilePath path,
            IOConsumer<Stream<FilePath>> consumer) throws IOException {
        IMPL.streamRecursive(path, consumer);
    }

    public static void consumeRecursive(FilePath path,
            IOConsumer<FilePath> consumer) throws IOException {
        IMPL.streamRecursive(path, paths -> {
            Iterator<FilePath> iterator = paths.iterator();
            while (iterator.hasNext()) {
                consumer.accept(iterator.next());
            }
        });
    }

    public static List<FilePath> listRecursive(FilePath path)
            throws IOException {
        return IMPL.listRecursive(path);
    }

    @SafeVarargs
    public static List<FilePath> listRecursive(FilePath path,
            Predicate<FilePath>... filters) throws IOException {
        return IMPL.listRecursive(path, filters);
    }

    public static void setLastModifiedTime(FilePath path, Instant value)
            throws IOException {
        IMPL.setLastModifiedTime(path, value);
    }

    public static Instant getLastModifiedTime(FilePath path)
            throws IOException {
        return IMPL.getLastModifiedTime(path);
    }

    @SuppressWarnings("ReturnOfNull")
    public static void tempChannel(FilePath path,
            IOConsumer<FileChannel> consumer) throws IOException {
        tempChannelReturn(path, channel -> {
            consumer.accept(channel);
            return null;
        });
    }

    public static <R> R tempChannelReturn(FilePath path,
            IOFunction<FileChannel, R> consumer) throws IOException {
        return IMPL.tempChannel(path, consumer);
    }

    public static ZipFile zipFile(FilePath path) throws IOException {
        return IMPL.zipFile(path);
    }
}
