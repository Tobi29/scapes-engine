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

package org.tobi29.scapes.engine.utils;

import java.util.regex.Pattern;

public final class VersionUtil {
    private static final Pattern DOT = Pattern.compile("\\."), UNDERSCORE =
            Pattern.compile("_");

    public static Comparison compare(Version current, Version check) {
        if (check.major > current.major) {
            return Comparison.HIGHER_MAJOR;
        } else if (check.major < current.major) {
            return Comparison.LOWER_MAJOR;
        } else if (check.minor > current.minor) {
            return Comparison.HIGHER_MINOR;
        } else if (check.minor < current.minor) {
            return Comparison.LOWER_MINOR;
        } else if (check.revision > current.revision) {
            return Comparison.HIGHER_REVISION;
        } else if (check.revision < current.revision) {
            return Comparison.LOWER_REVISION;
        } else if (check.build > current.build) {
            return Comparison.HIGHER_BUILD;
        } else if (check.build < current.build) {
            return Comparison.LOWER_BUILD;
        }
        return Comparison.EQUAL;
    }

    public static Version get(String str) throws VersionException {
        String[] split = DOT.split(str);
        if (split.length > 3) {
            throw new VersionException("Too many delimiters: " + str);
        }
        if (split.length <= 0) {
            throw new VersionException("Weird string: " + str);
        }
        int major, minor = 0, revision = 0, build = 1;
        try {
            major = Integer.parseInt(split[0]);
        } catch (NumberFormatException e) {
            throw new VersionException("Invalid major: " + split[0], e);
        }
        if (split.length >= 2) {
            try {
                minor = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                throw new VersionException("Invalid minor: " + split[1], e);
            }
            if (split.length == 3) {
                String[] split2 = UNDERSCORE.split(split[2]);
                if (split2.length > 2) {
                    throw new VersionException(
                            "Too many delimiters: " + split[2]);
                }
                if (split2.length <= 0) {
                    throw new VersionException("Weird string: " + split[2]);
                }
                try {
                    revision = Integer.parseInt(split2[0]);
                } catch (NumberFormatException e) {
                    throw new VersionException("Invalid revision: " + split2[0],
                            e);
                }
                if (split2.length == 2) {
                    try {
                        build = Integer.parseInt(split2[1]);
                    } catch (NumberFormatException e) {
                        throw new VersionException(
                                "Invalid build: " + split2[1], e);
                    }
                }
            }
        }
        return new Version(major, minor, revision, build);
    }

    public enum Comparison {
        LOWER_MAJOR(-4),
        LOWER_MINOR(-3),
        LOWER_REVISION(-2),
        LOWER_BUILD(-1),
        EQUAL(0),
        HIGHER_BUILD(1),
        HIGHER_REVISION(2),
        HIGHER_MINOR(3),
        HIGHER_MAJOR(4);
        private final int level;

        Comparison(int level) {
            this.level = level;
        }

        public boolean atLeast(Comparison other) {
            return level >= other.level;
        }

        public boolean atMost(Comparison other) {
            return level <= other.level;
        }
    }

    public static class Version {
        private final int major, minor, revision, build;

        public Version(int major, int minor, int revision, int build) {
            this.major = major;
            this.minor = minor;
            this.revision = revision;
            this.build = build;
        }

        public int major() {
            return major;
        }

        public int minor() {
            return minor;
        }

        public int revision() {
            return revision;
        }

        public int build() {
            return build;
        }

        @Override
        public String toString() {
            return major + "." + minor + '.' + revision + '_' + build;
        }
    }

    public static class VersionException extends Exception {
        private VersionException(String message) {
            super(message);
        }

        private VersionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
