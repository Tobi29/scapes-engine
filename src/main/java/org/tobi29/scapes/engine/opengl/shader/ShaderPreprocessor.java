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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderPreprocessor {
    private static final Pattern REPEAT =
            Pattern.compile("#repeat ([0-9]+) (.*)");
    private static final Pattern INDEX = Pattern.compile("\\$i");
    private final Map<String, String> vertexVariables =
            new ConcurrentHashMap<>(), fragmentVariables =
            new ConcurrentHashMap<>();

    public void supplyExternal(String key, Object value) {
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

    public void supplyVariable(String key, Object value) {
        supplyVariable(key, String.valueOf(value));
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

    protected String processSource(String source) {
        Matcher matcher = REPEAT.matcher(source);
        StringBuffer buffer = new StringBuffer(source.length());
        while (matcher.find()) {
            int amount = 1;
            try {
                amount = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
            }
            String replacement = matcher.group(2) + '\n';
            StringBuilder loop =
                    new StringBuilder(amount * replacement.length());
            for (int i = 0; i < amount; i++) {
                loop.append(INDEX.matcher(replacement)
                        .replaceAll(String.valueOf(i)));
            }
            matcher.appendReplacement(buffer,
                    Matcher.quoteReplacement(loop.toString()));
        }
        matcher.appendTail(buffer);
        source = buffer.toString();
        return source;
    }

    protected String processVertexSource(String source) {
        for (Map.Entry<String, String> entry : vertexVariables.entrySet()) {
            source = source.replaceAll(entry.getKey(), entry.getValue());
        }
        return processSource(source);
    }

    protected String processFragmentSource(String source) {
        for (Map.Entry<String, String> entry : fragmentVariables.entrySet()) {
            source = source.replaceAll(entry.getKey(), entry.getValue());
        }
        return processSource(source);
    }
}
