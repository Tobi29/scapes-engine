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

package org.tobi29.scapes.engine.graphics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShaderPreprocessor {
    private final Map<String, String> properties = new ConcurrentHashMap<>();

    public void supplyProperty(String key, Object value) {
        supplyProperty(key, String.valueOf(value));
    }

    public void supplyProperty(String key, String value) {
        properties.put(key, value);
    }

    public Map<String, String> properties() {
        return properties;
    }
}
