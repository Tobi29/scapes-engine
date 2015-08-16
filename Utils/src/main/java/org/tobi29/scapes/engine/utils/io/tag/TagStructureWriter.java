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

package org.tobi29.scapes.engine.utils.io.tag;

import java.io.IOException;

public interface TagStructureWriter {
    void begin(TagStructure root) throws IOException;

    void end() throws IOException;

    void beginStructure() throws IOException;

    void beginStructure(String key) throws IOException;

    void endStructure() throws IOException;

    void structureEmpty() throws IOException;

    void structureEmpty(String key) throws IOException;

    void beginList(String key) throws IOException;

    void endListWidthTerminate() throws IOException;

    void endListWithEmpty() throws IOException;

    void listEmpty(String key) throws IOException;

    void writeTag(String key, Object tag) throws IOException;
}
