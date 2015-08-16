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

package org.tobi29.scapes.engine.opengl.texture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.utils.BufferCreatorNative;
import org.tobi29.scapes.engine.utils.io.filesystem.FileSystemContainer;
import org.tobi29.scapes.engine.utils.io.filesystem.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class TextureManager {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(TextureManager.class);
    private final ScapesEngine engine;
    private final Map<String, TextureAsset> cache = new ConcurrentHashMap<>();
    private final Texture empty;

    public TextureManager(ScapesEngine engine) {
        this.engine = engine;
        ByteBuffer buffer = BufferCreatorNative.bytes(4);
        buffer.put((byte) -1);
        buffer.put((byte) -1);
        buffer.put((byte) -1);
        buffer.put((byte) -1);
        buffer.rewind();
        empty = new TextureCustom(1, 1, buffer, 0);
    }

    public void bind(String asset, GL gl) {
        if (asset == null) {
            unbind(gl);
        } else if (asset.isEmpty()) {
            unbind(gl);
        } else {
            bind(get(asset), gl);
        }
    }

    public void bind(Texture texture, GL gl) {
        if (texture == null) {
            unbind(gl);
        } else {
            texture.bind(gl);
        }
    }

    public Texture get(String asset) {
        Texture texture = cache.get(asset);
        if (texture == null) {
            texture = load(asset);
        }
        return texture;
    }

    private Texture load(String asset) {
        try {
            Properties properties = new Properties();
            FileSystemContainer files = engine.files();
            Resource imageResource = files.get(asset + ".png");
            Resource propertiesResource = files.get(asset + ".properties");
            if (propertiesResource.exists()) {
                try (InputStream streamIn = propertiesResource.readIO()) {
                    properties.load(streamIn);
                }
            }
            TextureAsset texture = imageResource
                    .readReturn(stream -> new TextureAsset(stream, properties));
            cache.put(asset, texture);
            return texture;
        } catch (IOException e) {
            LOGGER.error("Failed to load texture from: {} ({})", asset,
                    e.getMessage());
        }
        return empty;
    }

    public void unbind(GL gl) {
        empty.bind(gl);
    }

    public void clearCache(GL gl) {
        for (Texture texture : cache.values()) {
            texture.dispose(gl);
        }
        cache.clear();
    }
}
