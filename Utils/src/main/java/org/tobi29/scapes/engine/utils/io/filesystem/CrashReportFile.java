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

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.tobi29.scapes.engine.utils.io.WritableByteStream;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utility class for writing crash reports
 */
public final class CrashReportFile {
    private static final String[] SYSTEM_PROPERTIES =
            {"os.name", "os.version", "os.arch", "java.runtime.name",
                    "java.version", "java.class.version", "java.vendor"};
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter FILE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String LN = "\n";

    private CrashReportFile() {
    }

    /**
     * Suggests how to name the file
     *
     * @param path Parent directory to put the file into
     * @return A file inside of the given directory with appropriate name
     */
    public static FilePath file(FilePath path) {
        return file(path, OffsetDateTime.now());
    }

    /**
     * Suggests how to name the file
     *
     * @param path Parent directory to put the file into
     * @param time Time when crash occurred
     * @return A file inside of the given directory with appropriate name
     */
    public static FilePath file(FilePath path, OffsetDateTime time) {
        return path
                .resolve("CrashReport-" + time.format(FILE_FORMATTER) + ".txt");
    }

    /**
     * Writes a crash report
     *
     * @param e           The {@code Throwable} that supplies te stacktrace
     * @param path        The {@code File} that the report is written to
     * @param name        Name of the program used in the report
     * @param debugValues A {@code Map} that supplies extra information
     */
    public static void writeCrashReport(Throwable e, FilePath path, String name,
            Map<String, String> debugValues) throws IOException {
        writeCrashReport(e, path, name, debugValues, OffsetDateTime.now());
    }

    /**
     * Writes a crash report
     *
     * @param e           The {@code Throwable} that supplies te stacktrace
     * @param path        The {@code File} that the report is written to
     * @param name        Name of the program used in the report
     * @param debugValues A {@code Map} that supplies extra information
     * @param time        Time when crash occurred
     */
    public static void writeCrashReport(Throwable e, FilePath path, String name,
            Map<String, String> debugValues, OffsetDateTime time)
            throws IOException {
        FileUtil.write(path, stream -> {
            ln(stream, name + " has crashed: " + e);
            ln(stream);
            ln(stream,
                    "Please give this file to someone who has an idea of what to do with it (developer!)");
            ln(stream);
            ln(stream, "-----------Stacktrace:-----------");
            try (StringWriter writer = new StringWriter()) {
                e.printStackTrace(new PrintWriter(writer));
                ln(stream, writer.toString());
            }
            ln(stream);
            ln(stream, "---------Active Threads:---------");
            for (Map.Entry<?, ?> entry : Thread.getAllStackTraces()
                    .entrySet()) {
                ln(stream, "Thread:" + entry.getKey());
                for (StackTraceElement element : (StackTraceElement[]) entry
                        .getValue()) {
                    ln(stream, "\tat " + element);
                }
                ln(stream);
            }
            ln(stream, "--------System properties:-------");
            for (String property : SYSTEM_PROPERTIES) {
                ln(stream, property + " = " + System.getProperty(property));
            }
            ln(stream);
            ln(stream, "----------Debug values:----------");
            for (Map.Entry<String, String> entry : debugValues.entrySet()) {
                ln(stream, entry.getKey() + " = " + entry.getValue());
            }
            ln(stream);
            ln(stream, "--------------Time:--------------");
            ln(stream, LocalDateTime.from(time).format(FORMATTER));
        });
    }

    private static void ln(WritableByteStream stream) throws IOException {
        print(stream, LN);
    }

    private static void ln(WritableByteStream stream, String line)
            throws IOException {
        print(stream, line + LN);
    }

    private static void print(WritableByteStream stream, String str)
            throws IOException {
        byte[] array = str.getBytes(StandardCharsets.UTF_8);
        stream.put(array);
    }
}
