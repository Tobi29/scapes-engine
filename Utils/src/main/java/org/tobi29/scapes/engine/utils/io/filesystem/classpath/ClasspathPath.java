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

import org.tobi29.scapes.engine.utils.io.filesystem.Path;
import org.tobi29.scapes.engine.utils.io.filesystem.Resource;

public class ClasspathPath implements Path {
    private final ClassLoader classLoader;
    private final String path;

    public ClasspathPath(ClassLoader classLoader, String path) {
        this.classLoader = classLoader;
        this.path = path;
    }

    @Override
    public Resource get(String path) {
        return new ClasspathResource(classLoader, this.path + path);
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
        if (!(obj instanceof ClasspathPath)) {
            return false;
        }
        ClasspathPath resource = (ClasspathPath) obj;
        return path.equals(resource.path) &&
                classLoader.equals(resource.classLoader);
    }
}
