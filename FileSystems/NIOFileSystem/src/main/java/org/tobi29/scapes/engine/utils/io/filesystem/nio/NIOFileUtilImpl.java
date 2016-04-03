package org.tobi29.scapes.engine.utils.io.filesystem.nio;

import java8.util.Spliterators;
import java8.util.function.Predicate;
import java8.util.stream.Stream;
import org.apache.tika.Tika;
import org.threeten.bp.Instant;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.io.*;
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath;
import org.tobi29.scapes.engine.utils.io.filesystem.FileUtilImpl;
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.zip.ZipFile;

public class NIOFileUtilImpl implements FileUtilImpl {
    public static Path toPath(FilePath path) {
        if (path instanceof FilePathImpl) {
            return ((FilePathImpl) path).path;
        }
        return Paths.get(path.toUri());
    }

    private static void read(Path path,
            IOConsumer<BufferedReadChannelStream> read) throws IOException {
        read(path, read, StandardOpenOption.READ);
    }

    private static void read(Path path,
            IOConsumer<BufferedReadChannelStream> read, OpenOption... options)
            throws IOException {
        try (FileChannel channel = FileChannel.open(path, options)) {
            read.accept(new BufferedReadChannelStream(channel));
        }
    }

    private static <R> R readReturn(Path path,
            IOFunction<BufferedReadChannelStream, R> read) throws IOException {
        return readReturn(path, read, StandardOpenOption.READ);
    }

    private static <R> R readReturn(Path path,
            IOFunction<BufferedReadChannelStream, R> read,
            OpenOption... options) throws IOException {
        try (FileChannel channel = FileChannel.open(path, options)) {
            return read.apply(new BufferedReadChannelStream(channel));
        }
    }

    private static void write(Path path,
            IOConsumer<BufferedWriteChannelStream> write) throws IOException {
        write(path, write, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void write(Path path,
            IOConsumer<BufferedWriteChannelStream> write, OpenOption... options)
            throws IOException {
        try (FileChannel channel = FileChannel.open(path, options)) {
            BufferedWriteChannelStream stream =
                    new BufferedWriteChannelStream(channel);
            write.accept(stream);
            stream.flush();
        }
    }

    private static <R> R writeReturn(Path path,
            IOFunction<BufferedWriteChannelStream, R> write)
            throws IOException {
        return writeReturn(path, write, StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static <R> R writeReturn(Path path,
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

    public static FilePath path(Path path) {
        return new FilePathImpl(path);
    }

    public static ReadSource read(Path path) {
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
                NIOFileUtilImpl.read(path, reader::accept);
            }

            @Override
            public ReadableByteChannel channel() throws IOException {
                return Files.newByteChannel(path, StandardOpenOption.READ);
            }

            @Override
            public <R> R readReturn(IOFunction<ReadableByteStream, R> reader)
                    throws IOException {
                return NIOFileUtilImpl.readReturn(path, reader::apply);
            }

            @Override
            public String mimeType() throws IOException {
                try (InputStream streamIn = readIO()) {
                    return new Tika().detect(streamIn, path.toString());
                }
            }
        };
    }

    public static void deleteDir(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
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

    @Override
    public FilePath path(String path) {
        return path(Paths.get(path));
    }

    @Override
    public ReadSource read(FilePath path) {
        return read(toPath(path));
    }

    @Override
    public void read(FilePath path, IOConsumer<BufferedReadChannelStream> read)
            throws IOException {
        read(toPath(path), read);
    }

    @Override
    public <R> R readReturn(FilePath path,
            IOFunction<BufferedReadChannelStream, R> read) throws IOException {
        return readReturn(toPath(path), read);
    }

    @Override
    public void write(FilePath path,
            IOConsumer<BufferedWriteChannelStream> write) throws IOException {
        write(toPath(path), write);
    }

    @Override
    public <R> R writeReturn(FilePath path,
            IOFunction<BufferedWriteChannelStream, R> write)
            throws IOException {
        return writeReturn(toPath(path), write);
    }

    @Override
    public FilePath createDirectories(FilePath path) throws IOException {
        return path(Files.createDirectories(toPath(path)));
    }

    @Override
    public void delete(FilePath path) throws IOException {
        Files.delete(toPath(path));
    }

    @Override
    public boolean deleteIfExists(FilePath path) throws IOException {
        return Files.deleteIfExists(toPath(path));
    }

    @Override
    public void deleteDir(FilePath path) throws IOException {
        deleteDir(toPath(path));
    }

    @Override
    public boolean exists(FilePath path) {
        return Files.exists(toPath(path));
    }

    @Override
    public boolean isRegularFile(FilePath path) {
        return Files.isRegularFile(toPath(path));
    }

    @Override
    public boolean isDirectory(FilePath path) {
        return Files.isDirectory(toPath(path));
    }

    @Override
    public boolean isHidden(FilePath path) {
        try {
            return Files.isHidden(toPath(path));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isNotHidden(FilePath path) {
        return !isHidden(path);
    }

    @Override
    public FilePath createTempFile(String prefix, String suffix)
            throws IOException {
        return path(Files.createTempFile(prefix, suffix));
    }

    @Override
    public FilePath copy(FilePath source, FilePath target) throws IOException {
        return path(Files.copy(toPath(source), toPath(target)));
    }

    @Override
    public void stream(FilePath path, IOConsumer<Stream<FilePath>> consumer)
            throws IOException {
        try (DirectoryStream<Path> stream = Files
                .newDirectoryStream(toPath(path))) {
            consumer.accept(Streams.of(
                    Spliterators.spliteratorUnknownSize(stream.iterator(), 0))
                    .map(NIOFileUtilImpl::path));
        }
    }

    @Override
    public List<FilePath> list(FilePath path) throws IOException {
        List<FilePath> files = new ArrayList<>();
        stream(path, stream -> stream.forEach(files::add));
        return files;
    }

    @Override
    public final List<FilePath> list(FilePath path,
            Predicate<FilePath>[] filters) throws IOException {
        List<FilePath> files = new ArrayList<>();
        stream(path, stream -> stream.filter(file -> {
            for (Predicate<FilePath> filter : filters) {
                if (!filter.test(file)) {
                    return false;
                }
            }
            return true;
        }).forEach(files::add));
        return files;
    }

    @Override
    public void streamRecursive(FilePath path,
            IOConsumer<Stream<FilePath>> consumer) throws IOException {
        consumer.accept(Streams.of(listRecursive(path)));
    }

    @Override
    public List<FilePath> listRecursive(FilePath path) throws IOException {
        List<FilePath> files = new ArrayList<>();
        Files.walkFileTree(toPath(path),
                EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir,
                            BasicFileAttributes attrs) {
                        files.add(path(dir));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file,
                            BasicFileAttributes attrs) {
                        files.add(path(file));
                        return FileVisitResult.CONTINUE;
                    }
                });
        return files;
    }

    @Override
    public final List<FilePath> listRecursive(FilePath path,
            Predicate<FilePath>[] filters) throws IOException {
        List<FilePath> files = new ArrayList<>();
        Files.walkFileTree(toPath(path),
                EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file,
                            BasicFileAttributes attrs) {
                        FilePath filePath = path(file);
                        boolean valid = true;
                        for (Predicate<FilePath> filter : filters) {
                            if (!filter.test(filePath)) {
                                valid = false;
                                break;
                            }
                        }
                        if (valid) {
                            files.add(filePath);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
        return files;
    }

    @Override
    public void setLastModifiedTime(FilePath path, Instant value)
            throws IOException {
        Files.setLastModifiedTime(toPath(path),
                FileTime.fromMillis(value.toEpochMilli()));
    }

    @Override
    public Instant getLastModifiedTime(FilePath path) throws IOException {
        return Instant.ofEpochMilli(
                Files.getLastModifiedTime(toPath(path)).toMillis());
    }

    @Override
    public ZipFile zipFile(FilePath path) throws IOException {
        return new ZipFile(toPath(path).toFile());
    }

    @Override
    public <R> R tempChannel(FilePath path, IOFunction<FileChannel, R> consumer)
            throws IOException {
        try (FileChannel channel = FileChannel
                .open(toPath(path), StandardOpenOption.READ,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.DELETE_ON_CLOSE)) {
            return consumer.apply(channel);
        }
    }

    private static final class FilePathImpl implements FilePath {
        private final Path path;

        private FilePathImpl(Path path) {
            this.path = path;
        }

        @Override
        public int compareTo(FilePath o) {
            return path.compareTo(toPath(o));
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FilePath &&
                    path.equals(toPath((FilePath) obj));
        }

        @Override
        public String toString() {
            return path.toString();
        }

        @Override
        public URI toUri() {
            return path.toUri();
        }

        @Override
        public FilePath resolve(String other) {
            return path(path.resolve(other));
        }

        @Override
        public FilePath resolve(FilePath other) {
            return path(path.resolve(toPath(other)));
        }

        @Override
        public FilePath getFileName() {
            return path(path.getFileName());
        }

        @Override
        public FilePath toAbsolutePath() {
            return path(path.toAbsolutePath());
        }

        @Override
        public ReadSource get(String path) {
            return read(toPath(resolve(path)));
        }
    }
}
