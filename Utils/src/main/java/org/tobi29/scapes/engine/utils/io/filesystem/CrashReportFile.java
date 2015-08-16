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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Utility class for writing crash reports
 */
public final class CrashReportFile {
    private static final String[] SYSTEM_PROPERTIES =
            {"os.name", "os.version", "os.arch", "java.runtime.name",
                    "java.version", "java.class.version", "java.vendor"};
    private static final Clock DEFAULT_CLOCK = Clock.systemUTC();
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ISO_DATE_TIME;
    private static final DateTimeFormatter FILE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private CrashReportFile() {
    }

    /**
     * Suggests how to name the file
     *
     * @param path Parent directory to put the file into
     * @return A file inside of the given directory with appropriate name
     */
    public static Path file(Path path) {
        return file(path, DEFAULT_CLOCK);
    }

    /**
     * Suggests how to name the file
     *
     * @param path  Parent directory to put the file into
     * @param clock The clock used to name the file
     * @return A file inside of the given directory with appropriate name
     */
    public static Path file(Path path, Clock clock) {
        return path.resolve("CrashReport-" +
                OffsetDateTime.now(clock).format(FILE_FORMATTER) +
                ".txt");
    }

    /**
     * Writes a crash report
     *
     * @param e           The {@code Throwable} that supplies te stacktrace
     * @param path        The {@code File} that the report is written to
     * @param name        Name of the program used in the report
     * @param debugValues A {@code Map} that supplies extra information
     */
    public static void writeCrashReport(Throwable e, Path path, String name,
            Map<String, String> debugValues) throws IOException {
        writeCrashReport(e, path, name, debugValues, DEFAULT_CLOCK);
    }

    /**
     * Writes a crash report
     *
     * @param e           The {@code Throwable} that supplies te stacktrace
     * @param path        The {@code File} that the report is written to
     * @param name        Name of the program used in the report
     * @param debugValues A {@code Map} that supplies extra information
     * @param clock       The clock used to determine time in the report
     */
    public static void writeCrashReport(Throwable e, Path path, String name,
            Map<String, String> debugValues, Clock clock) throws IOException {
        try (PrintWriter writer = new PrintWriter(
                Files.newBufferedWriter(path))) {
            writer.println(name + " has crashed: " + e);
            writer.println();
            writer.println(
                    "Please give this file to someone who has an idea of what to do with it (developer!)");
            writer.println();
            writer.println("-----------Stacktrace:-----------");
            e.printStackTrace(writer);
            writer.println();
            writer.println("---------Active Threads:---------");
            for (Map.Entry<?, ?> entry : Thread.getAllStackTraces()
                    .entrySet()) {
                writer.println("Thread:" + entry.getKey());
                for (StackTraceElement element : (StackTraceElement[]) entry
                        .getValue()) {
                    writer.println("\tat " + element);
                }
                writer.println();
            }
            writer.println("--------System properties:-------");
            for (String property : SYSTEM_PROPERTIES) {
                writer.println(property + " = " + System.getProperty(property));
            }
            writer.println();
            writer.println("----------Debug values:----------");
            for (Map.Entry<String, String> entry : debugValues.entrySet()) {
                writer.println(entry.getKey() + " = " + entry.getValue());
            }
            writer.println();
            writer.println("--------------Time:--------------");
            writer.println(OffsetDateTime.now(clock).format(FORMATTER));
        }
    }
}
