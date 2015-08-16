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

import org.tobi29.scapes.engine.utils.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class FileSystemContainer implements Path {
    private static final Pattern SPLIT = Pattern.compile(":");
    private final Map<String, Path> fileSystems = new ConcurrentHashMap<>();

    public void registerFileSystem(String id, Path path) {
        fileSystems.put(id, path);
    }

    public void removeFileSystem(String id) {
        fileSystems.remove(id);
    }

    private Path fileSystem(String id) {
        Path fileSystem = fileSystems.get(id);
        if (fileSystem == null) {
            throw new IllegalArgumentException("Unknown file system: " + id);
        }
        return fileSystem;
    }

    @Override
    public Resource get(String path) {
        Pair<String, String> location = splitPath(path);
        return fileSystem(location.a).get(location.b);
    }

    private Pair<String, String> splitPath(String path) {
        String[] array = SPLIT.split(path, 2);
        if (array.length != 2) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        return new Pair<>(array[0], array[1]);
    }
}
