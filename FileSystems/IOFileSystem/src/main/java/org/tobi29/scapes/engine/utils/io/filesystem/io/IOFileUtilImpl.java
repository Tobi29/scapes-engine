package org.tobi29.scapes.engine.utils.io.filesystem.io;

import java8.util.function.Predicate;
import java8.util.stream.Stream;
import org.apache.tika.Tika;
import org.threeten.bp.Instant;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.io.*;
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath;
import org.tobi29.scapes.engine.utils.io.filesystem.FileUtilImpl;
import org.tobi29.scapes.engine.utils.io.filesystem.ReadSource;

import java.io.*;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class IOFileUtilImpl implements FileUtilImpl {
    public static File toFile(FilePath path) {
        if (path instanceof FilePathImpl) {
            return ((FilePathImpl) path).file;
        }
        return new File(path.toUri());
    }

    private static void read(File file,
            IOConsumer<BufferedReadChannelStream> read) throws IOException {
        try (FileChannel channel = channelR(file)) {
            read.accept(new BufferedReadChannelStream(channel));
        }
    }

    private static <R> R readReturn(File file,
            IOFunction<BufferedReadChannelStream, R> read) throws IOException {
        try (FileChannel channel = channelR(file)) {
            return read.apply(new BufferedReadChannelStream(channel));
        }
    }

    private static void write(File file,
            IOConsumer<BufferedWriteChannelStream> write) throws IOException {
        try (FileChannel channel = channelDRW(file)) {
            BufferedWriteChannelStream stream =
                    new BufferedWriteChannelStream(channel);
            write.accept(stream);
            stream.flush();
        }
    }

    private static <R> R writeReturn(File file,
            IOFunction<BufferedWriteChannelStream, R> write)
            throws IOException {
        try (FileChannel channel = channelDRW(file)) {
            BufferedWriteChannelStream stream =
                    new BufferedWriteChannelStream(channel);
            R r = write.apply(stream);
            stream.flush();
            return r;
        }
    }

    public static FilePath path(File file) {
        return new FilePathImpl(file);
    }

    public static ReadSource read(File file) {
        return new ReadSource() {
            @Override
            public boolean exists() {
                return file.exists();
            }

            @Override
            public InputStream readIO() throws IOException {
                return new FileInputStream(file);
            }

            @Override
            public void read(IOConsumer<ReadableByteStream> reader)
                    throws IOException {
                IOFileUtilImpl.read(file, reader::accept);
            }

            @Override
            public ReadableByteChannel channel() throws IOException {
                return channelR(file);
            }

            @Override
            public <R> R readReturn(IOFunction<ReadableByteStream, R> reader)
                    throws IOException {
                return IOFileUtilImpl.readReturn(file, reader::apply);
            }

            @Override
            public String mimeType() throws IOException {
                try (InputStream streamIn = readIO()) {
                    return new Tika().detect(streamIn, file.toString());
                }
            }
        };
    }

    public static void deleteDir(File file) {
        // TODO: Add loop check
        File[] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                if (child.isFile()) {
                    child.delete();
                } else if (child.isDirectory()) {
                    deleteDir(child);
                }
            }
        }
        file.delete();
    }

    private static FileChannel channelR(File file) throws IOException {
        return new RandomAccessFile(file, "r").getChannel();
    }

    private static FileChannel channelRW(File file) throws IOException {
        return new RandomAccessFile(file, "rw").getChannel();
    }

    private static FileChannel channelDRW(File file) throws IOException {file.delete();
        return new RandomAccessFile(file, "rw").getChannel();
    }

    private static void listRecursive(File file, List<FilePath> list) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                list.add(path(child));
                if (child.isDirectory()) {
                    listRecursive(child, list);
                }
            }
        }
    }

    @SafeVarargs
    private static void listRecursive(File file, List<FilePath> list,
            Predicate<FilePath>... filters) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                FilePath filePath = path(child);
                boolean valid = true;
                for (Predicate<FilePath> filter : filters) {
                    if (!filter.test(filePath)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    list.add(filePath);
                }
                if (child.isDirectory()) {
                    listRecursive(child, list, filters);
                }
            }
        }
    }

    @Override
    public FilePath path(String path) {
        return path(new File(path));
    }

    @Override
    public ReadSource read(FilePath path) {
        return read(toFile(path));
    }

    @Override
    public void read(FilePath path, IOConsumer<BufferedReadChannelStream> read)
            throws IOException {
        read(toFile(path), read);
    }

    @Override
    public <R> R readReturn(FilePath path,
            IOFunction<BufferedReadChannelStream, R> read) throws IOException {
        return readReturn(toFile(path), read);
    }

    @Override
    public void write(FilePath path,
            IOConsumer<BufferedWriteChannelStream> write) throws IOException {
        write(toFile(path), write);
    }

    @Override
    public <R> R writeReturn(FilePath path,
            IOFunction<BufferedWriteChannelStream, R> write)
            throws IOException {
        return writeReturn(toFile(path), write);
    }

    @Override
    public FilePath createDirectories(FilePath path) throws IOException {
        toFile(path).mkdirs();
        return path;
    }

    @Override
    public void delete(FilePath path) throws IOException {
        toFile(path).delete();
    }

    @Override
    public boolean deleteIfExists(FilePath path) throws IOException {
        File file = toFile(path);
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    @Override
    public void deleteDir(FilePath path) throws IOException {
        deleteDir(toFile(path));
    }

    @Override
    public boolean exists(FilePath path) {
        return toFile(path).exists();
    }

    @Override
    public boolean isRegularFile(FilePath path) {
        return toFile(path).isFile();
    }

    @Override
    public boolean isDirectory(FilePath path) {
        return toFile(path).isDirectory();
    }

    @Override
    public boolean isHidden(FilePath path) {
        return toFile(path).isHidden();
    }

    @Override
    public boolean isNotHidden(FilePath path) {
        return !isHidden(path);
    }

    @Override
    public FilePath createTempFile(String prefix, String suffix)
            throws IOException {
        return path(File.createTempFile(prefix, suffix));
    }

    @Override
    public FilePath copy(FilePath source, FilePath target) throws IOException {
        read(toFile(source), input -> {
            write(toFile(target),
                    output -> ProcessStream.process(input, output::put));
        });
        return target;
    }

    @Override
    public void stream(FilePath path, IOConsumer<Stream<FilePath>> consumer)
            throws IOException {
        consumer.accept(Streams.of(list(path)));
    }

    @Override
    public List<FilePath> list(FilePath path) throws IOException {
        List<FilePath> list = new ArrayList<>();
        File[] files = toFile(path).listFiles();
        if (files != null) {
            for (File child : files) {
                list.add(path(child));
            }
        }
        return list;
    }

    @Override
    public final List<FilePath> list(FilePath path,
            Predicate<FilePath>[] filters) {
        List<FilePath> list = new ArrayList<>();
        File[] files = toFile(path).listFiles();
        if (files != null) {
            for (File child : files) {
                FilePath filePath = path(child);
                boolean valid = true;
                for (Predicate<FilePath> filter : filters) {
                    if (!filter.test(filePath)) {
                        valid = false;
                        break;
                    }
                }
                if (valid) {
                    list.add(filePath);
                }
            }
        }
        return list;
    }

    @Override
    public void streamRecursive(FilePath path,
            IOConsumer<Stream<FilePath>> consumer) throws IOException {
        consumer.accept(Streams.of(listRecursive(path)));
    }

    @Override
    public List<FilePath> listRecursive(FilePath path) throws IOException {
        List<FilePath> files = new ArrayList<>();
        listRecursive(toFile(path), files);
        return files;
    }

    @Override
    public final List<FilePath> listRecursive(FilePath path,
            Predicate<FilePath>[] filters) {
        List<FilePath> files = new ArrayList<>();
        listRecursive(toFile(path), files, filters);
        return files;
    }

    @Override
    public void setLastModifiedTime(FilePath path, Instant value)
            throws IOException {
        toFile(path).setLastModified(value.toEpochMilli());
    }

    @Override
    public Instant getLastModifiedTime(FilePath path) throws IOException {
        return Instant.ofEpochMilli(toFile(path).lastModified());
    }

    @Override
    public ZipFile zipFile(FilePath path) throws IOException {
        return new ZipFile(toFile(path));
    }

    @Override
    public <R> R tempChannel(FilePath path, IOFunction<FileChannel, R> consumer)
            throws IOException {
        File file = toFile(path);
        R ret;
        try (FileChannel channel = channelRW(file)) {
            ret = consumer.apply(channel);
        }
        // If this delete fails you are probably using M$ Windows and this
        // implementation is only meant for Android
        file.delete();
        return ret;
    }

    private static final class FilePathImpl implements FilePath {
        private final File file;

        private FilePathImpl(File file) {
            this.file = file;
        }

        @Override
        public int compareTo(FilePath o) {
            return file.compareTo(toFile(o));
        }

        @Override
        public int hashCode() {
            return file.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FilePath &&
                    file.equals(toFile((FilePath) obj));
        }

        @Override
        public String toString() {
            return file.toString();
        }

        @Override
        public URI toUri() {
            return file.toURI();
        }

        @Override
        public FilePath resolve(String other) {
            return path(new File(file, other));
        }

        @Override
        public FilePath resolve(FilePath other) {
            return resolve(other.toString());
        }

        @Override
        public FilePath getFileName() {
            return path(new File(file.getName()));
        }

        @Override
        public FilePath toAbsolutePath() {
            return path(file.getAbsoluteFile());
        }

        @Override
        public ReadSource get(String path) {
            return read(toFile(resolve(path)));
        }
    }
}
