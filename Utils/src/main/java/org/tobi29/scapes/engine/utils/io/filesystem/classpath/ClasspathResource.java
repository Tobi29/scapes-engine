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

package org.tobi29.scapes.engine.utils.io.filesystem.classpath;

import org.apache.tika.Tika;
import org.tobi29.scapes.engine.utils.io.BufferedReadChannelStream;
import org.tobi29.scapes.engine.utils.io.IOConsumer;
import org.tobi29.scapes.engine.utils.io.IOFunction;
import org.tobi29.scapes.engine.utils.io.ReadableByteStream;
import org.tobi29.scapes.engine.utils.io.filesystem.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class ClasspathResource implements Resource {
    private final ClassLoader classLoader;
    private final String path;

    public ClasspathResource(ClassLoader classLoader, String path) {
        this.classLoader = classLoader;
        this.path = path;
    }

    @Override
    public boolean exists() {
        return AccessController.doPrivileged(
                (PrivilegedAction<URL>) () -> classLoader.getResource(path)) !=
                null;
    }

    @Override
    public InputStream readIO() throws IOException {
        return AccessController.doPrivileged(
                (PrivilegedAction<InputStream>) () -> classLoader
                        .getResourceAsStream(path));
    }

    @Override
    public void read(IOConsumer<ReadableByteStream> reader) throws IOException {
        try (InputStream streamIn = readIO()) {
            reader.accept(new BufferedReadChannelStream(
                    Channels.newChannel(streamIn)));
        }
    }

    @Override
    public ReadableByteChannel channel() throws IOException {
        return Channels.newChannel(readIO());
    }

    @Override
    public <R> R readReturn(IOFunction<ReadableByteStream, R> reader)
            throws IOException {
        try (InputStream streamIn = readIO()) {
            return reader.apply(new BufferedReadChannelStream(
                    Channels.newChannel(streamIn)));
        }
    }

    @Override
    public String mimeType() throws IOException {
        try (InputStream streamIn = readIO()) {
            return new Tika().detect(streamIn, path);
        }
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + path.hashCode();
        result = prime * result + classLoader.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClasspathResource)) {
            return false;
        }
        ClasspathResource resource = (ClasspathResource) obj;
        return path.equals(resource.path) &&
                classLoader.equals(resource.classLoader);
    }
}
