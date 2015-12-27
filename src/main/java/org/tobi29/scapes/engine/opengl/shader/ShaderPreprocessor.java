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

public class ShaderPreprocessor {
    private final Map<String, String> vertexVariables =
            new ConcurrentHashMap<>(), fragmentVariables =
            new ConcurrentHashMap<>(), properties = new ConcurrentHashMap<>();

    public void supplyVariable(String key, String value) {
        vertexVariables.put(key, value);
        fragmentVariables.put(key, value);
    }

    public void supplyProperty(String key, Object value) {
        supplyProperty(key, String.valueOf(value));
    }

    public void supplyProperty(String key, String value) {
        properties.put(key, value);
        supplyVariable("\\$" + key, value);
    }

    public Map<String, String> properties() {
        return properties;
    }

    public String processVertexSource(String source) {
        for (Map.Entry<String, String> entry : vertexVariables.entrySet()) {
            source = source.replaceAll(entry.getKey(), entry.getValue());
        }
        return source;
    }

    public String processFragmentSource(String source) {
        for (Map.Entry<String, String> entry : fragmentVariables.entrySet()) {
            source = source.replaceAll(entry.getKey(), entry.getValue());
        }
        return source;
    }
}
