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

package org.tobi29.scapes.engine.opengl.shader;

import org.tobi29.scapes.engine.ScapesEngine;
import org.tobi29.scapes.engine.opengl.GL;
import org.tobi29.scapes.engine.utils.io.filesystem.FileSystemContainer;
import org.tobi29.scapes.engine.utils.io.filesystem.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ShaderManager {
    private final ScapesEngine engine;
    private final Map<String, Shader> cache = new ConcurrentHashMap<>();
    private final Map<String, ShaderCompileInformation> compileInformation =
            new ConcurrentHashMap<>();

    public ShaderManager(ScapesEngine engine) {
        this.engine = engine;
    }

    public Shader get(String asset, GL gl) {
        if (!cache.containsKey(asset)) {
            load(asset, gl);
        }
        return cache.get(asset);
    }

    private void load(String asset, GL gl) {
        try {
            Properties properties = new Properties();
            FileSystemContainer files = engine.files();
            Resource vertexResource = files.get(asset + ".vsh");
            Resource fragmentResource = files.get(asset + ".fsh");
            Resource propertiesResource = files.get(asset + ".properties");
            if (propertiesResource.exists()) {
                try (InputStream streamIn = propertiesResource.readIO()) {
                    properties.load(streamIn);
                }
            }
            Shader shader =
                    new Shader(vertexResource, fragmentResource, properties,
                            compileInformation(asset), gl);
            cache.put(asset, shader);
        } catch (IOException e) {
            engine.crash(e);
        }
    }

    public ShaderCompileInformation compileInformation(String asset) {
        ShaderCompileInformation information = compileInformation.get(asset);
        if (information == null) {
            information = new ShaderCompileInformation();
            compileInformation.put(asset, information);
        }
        return information;
    }

    public void clearCache(GL gl) {
        for (Shader shader : cache.values()) {
            shader.dispose(gl);
        }
        cache.clear();
    }
}
