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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShaderCompileInformation {
    private final Map<String, String> vertexVariables =
            new ConcurrentHashMap<>(), fragmentVariables =
            new ConcurrentHashMap<>();
    private final Map<String, PostCompileListener> postCompileListeners =
            new ConcurrentHashMap<>();

    public void supplyExternal(String key, int value) {
        supplyExternal(key, String.valueOf(value));
    }

    public void supplyExternal(String key, float value) {
        supplyExternal(key, String.valueOf(value));
    }

    public void supplyExternal(String key, String value) {
        String define = "#define " + key;
        supplyVariable(define + " _SCAPES_ENGINE_EXTERNAL",
                define + ' ' + value);
    }

    public void supplyDefine(String key, boolean value) {
        String define = "#define " + key;
        String replace = define + " _SCAPES_ENGINE_DEFINE";
        if (value) {
            supplyVariable(replace, define);
        } else {
            supplyVariable(replace, "");
        }
    }

    public void supplyVariable(String key, String value) {
        vertexVariables.put(key, value);
        fragmentVariables.put(key, value);
    }

    public void supplyVertexVariable(String key, String value) {
        vertexVariables.put(key, value);
    }

    public void supplyFragmentVariable(String key, String value) {
        fragmentVariables.put(key, value);
    }

    public void supplyPostCompile(String id, PostCompileListener listener) {
        postCompileListeners.put(id, listener);
    }

    protected String processVertexSource(String source) {
        for (Map.Entry<String, String> entry : vertexVariables.entrySet()) {
            source = source.replaceAll(entry.getKey(), entry.getValue());
        }
        return source;
    }

    protected String processFragmentSource(String source) {
        for (Map.Entry<String, String> entry : fragmentVariables.entrySet()) {
            source = source.replaceAll(entry.getKey(), entry.getValue());
        }
        return source;
    }

    protected void postCompile(Shader shader) {
        postCompileListeners.values()
                .forEach(listener -> listener.postCompile(shader));
    }

    @FunctionalInterface
    public interface PostCompileListener {
        void postCompile(Shader shader);
    }
}
