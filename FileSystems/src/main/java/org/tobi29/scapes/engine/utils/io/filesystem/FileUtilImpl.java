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
import org.tobi29.scapes.engine.utils.io.IOConsumer;
import org.tobi29.scapes.engine.utils.io.IOFunction;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.zip.ZipFile;

public interface FileUtilImpl {
    FilePath path(String path);

    ReadSource read(FilePath path);

    void read(FilePath path, IOConsumer<ReadableByteStream> read)
            throws IOException;

    <R> R readReturn(FilePath path, IOFunction<ReadableByteStream, R> read)
            throws IOException;

    void write(FilePath path, IOConsumer<WritableByteStream> write)
            throws IOException;

    <R> R writeReturn(FilePath path, IOFunction<WritableByteStream, R> write)
            throws IOException;

    FilePath createDirectories(FilePath path) throws IOException;

    void delete(FilePath path) throws IOException;

    boolean deleteIfExists(FilePath path) throws IOException;

    void deleteDir(FilePath path) throws IOException;

    boolean exists(FilePath path);

    boolean isRegularFile(FilePath path);

    boolean isDirectory(FilePath path);

    boolean isHidden(FilePath path);

    boolean isNotHidden(FilePath path);

    FilePath createTempFile(String prefix, String suffix) throws IOException;

    FilePath copy(FilePath source, FilePath target) throws IOException;

    FilePath move(FilePath source, FilePath target) throws IOException;

    void stream(FilePath path, IOConsumer<Stream<FilePath>> consumer)
            throws IOException;

    List<FilePath> list(FilePath path) throws IOException;

    List<FilePath> list(FilePath path, Predicate<FilePath>[] filters)
            throws IOException;

    void streamRecursive(FilePath path, IOConsumer<Stream<FilePath>> consumer)
            throws IOException;

    List<FilePath> listRecursive(FilePath path) throws IOException;

    List<FilePath> listRecursive(FilePath path, Predicate<FilePath>[] filters)
            throws IOException;

    void setLastModifiedTime(FilePath path, Instant value) throws IOException;

    Instant getLastModifiedTime(FilePath path) throws IOException;

    ZipFile zipFile(FilePath path) throws IOException;

    <R> R tempChannel(FilePath path, IOFunction<FileChannel, R> consumer)
            throws IOException;
}
