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

import org.apache.tika.Tika;
import org.tobi29.scapes.engine.utils.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class FileUtil {
    private FileUtil() {
    }

    public static ReadSource read(java.nio.file.Path path) {
        return new ReadSource() {
            @Override
            public boolean exists() {
                return Files.exists(path);
            }

            @Override
            public InputStream readIO() throws IOException {
                return Files.newInputStream(path);
            }

            @Override
            public void read(IOConsumer<ReadableByteStream> reader)
                    throws IOException {
                FileUtil.read(path, reader::accept);
            }

            @Override
            public ReadableByteChannel channel() throws IOException {
                return Files.newByteChannel(path, StandardOpenOption.READ);
            }

            @Override
            public <R> R readReturn(IOFunction<ReadableByteStream, R> reader)
                    throws IOException {
                return FileUtil.readReturn(path, reader::apply);
            }

            @Override
            public String mimeType() throws IOException {
                return new Tika().detect(readIO(), path.toString());
            }
        };
    }

    public static void read(java.nio.file.Path path,
            IOConsumer<BufferedReadChannelStream> read) throws IOException {
        read(path, read, StandardOpenOption.READ);
    }

    public static void read(java.nio.file.Path path,
            IOConsumer<BufferedReadChannelStream> read, OpenOption... options)
            throws IOException {
        try (FileChannel channel = FileChannel.open(path, options)) {
            read.accept(new BufferedReadChannelStream(channel));
        }
    }

    public static <R> R readReturn(java.nio.file.Path path,
            IOFunction<BufferedReadChannelStream, R> read) throws IOException {
        return readReturn(path, read, StandardOpenOption.READ);
    }

    public static <R> R readReturn(java.nio.file.Path path,
            IOFunction<BufferedReadChannelStream, R> read,
            OpenOption... options) throws IOException {
        try (FileChannel channel = FileChannel.open(path, options)) {
            return read.apply(new BufferedReadChannelStream(channel));
        }
    }

    public static void write(java.nio.file.Path path,
            IOConsumer<BufferedWriteChannelStream> write) throws IOException {
        write(path, write, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void write(java.nio.file.Path path,
            IOConsumer<BufferedWriteChannelStream> write, OpenOption... options)
            throws IOException {
        try (FileChannel channel = FileChannel.open(path, options)) {
            BufferedWriteChannelStream stream =
                    new BufferedWriteChannelStream(channel);
            write.accept(stream);
            stream.flush();
        }
    }

    public static <R> R writeReturn(java.nio.file.Path path,
            IOFunction<BufferedWriteChannelStream, R> write)
            throws IOException {
        return writeReturn(path, write, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static <R> R writeReturn(java.nio.file.Path path,
            IOFunction<BufferedWriteChannelStream, R> write,
            OpenOption... options) throws IOException {
        try (FileChannel channel = FileChannel.open(path, options)) {
            BufferedWriteChannelStream stream =
                    new BufferedWriteChannelStream(channel);
            R r = write.apply(stream);
            stream.flush();
            return r;
        }
    }

    public static void deleteDir(java.nio.file.Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<java.nio.file.Path>() {
            @Override
            public FileVisitResult visitFile(java.nio.file.Path file,
                    BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
