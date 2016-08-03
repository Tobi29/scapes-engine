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

package org.tobi29.scapes.engine.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.utils.BufferCreator;
import org.tobi29.scapes.engine.utils.UnsupportedJVMException;
import org.tobi29.scapes.engine.utils.graphics.Image;
import org.tobi29.scapes.engine.utils.graphics.PNG;
import org.tobi29.scapes.engine.utils.io.ByteBufferStream;
import org.tobi29.scapes.engine.utils.io.CompressionUtil;
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath;
import org.tobi29.scapes.engine.utils.io.filesystem.FileUtil;
import org.tobi29.scapes.engine.utils.math.FastMath;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ServerInfo {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ServerInfo.class);
    private final String name;
    private final Image image;
    private final ByteBuffer buffer;

    public ServerInfo(String name) {
        this(name, new Image());
    }

    public ServerInfo(String name, FilePath iconPath) {
        this(name, image(iconPath));
    }

    public ServerInfo(String name, Image image) {
        this.name = name;
        this.image = image;
        ByteBuffer buffer;
        try {
            buffer = CompressionUtil
                    .compress(new ByteBufferStream(image.buffer()));
        } catch (IOException e) {
            throw new UnsupportedJVMException(e);
        }
        buffer.flip();
        byte[] array = name.getBytes(StandardCharsets.UTF_8);
        this.buffer =
                BufferCreator.bytes(1 + array.length + buffer.remaining());
        this.buffer.put((byte) array.length);
        this.buffer.put(array);
        this.buffer.put(buffer);
        this.buffer.flip();
    }

    public ServerInfo(ByteBuffer buffer) {
        Image image = new Image();
        byte[] array = new byte[buffer.get()];
        buffer.get(array);
        name = new String(array, StandardCharsets.UTF_8);
        try {
            ByteBuffer imageBuffer =
                    CompressionUtil.decompress(new ByteBufferStream(buffer));
            imageBuffer.flip();
            int size = (int) FastMath.sqrt(imageBuffer.remaining() >> 2);
            image = new Image(size, size, imageBuffer);
        } catch (IOException e) {
            LOGGER.warn("Failed to decompress server icon: {}", e.toString());
        }
        this.buffer = buffer;
        this.image = image;
    }

    private static Image image(FilePath path) {
        if (FileUtil.exists(path)) {
            try {
                Image image = FileUtil.readReturn(path,
                        stream -> PNG.decode(stream, BufferCreator::bytes));
                int width = image.width();
                if (width != image.height()) {
                    LOGGER.warn("The icon has to be square sized.");
                } else if (width > 256) {
                    LOGGER.warn("The icon may not be larger than 256x256.");
                } else {
                    return image;
                }
            } catch (IOException e) {
                LOGGER.warn("Unable to load icon: {}", e.getMessage());
            }
        }
        return new Image();
    }

    public String getName() {
        return name;
    }

    public Image getImage() {
        return image;
    }

    public ByteBuffer getBuffer() {
        return buffer.asReadOnlyBuffer();
    }
}
