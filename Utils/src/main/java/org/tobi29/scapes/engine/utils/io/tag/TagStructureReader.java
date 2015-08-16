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

import org.tobi29.scapes.engine.utils.Pair;

import java.io.IOException;

public interface TagStructureReader {
    void begin() throws IOException;

    void end() throws IOException;

    Pair<String, Object> next() throws IOException;

    void beginStructure() throws IOException;

    void endStructure() throws IOException;

    void beginList() throws IOException;

    void endList() throws IOException;

    enum SpecialNext {
        STRUCTURE,
        STRUCTURE_TERMINATE,
        STRUCTURE_EMPTY,
        LIST,
        LIST_TERMINATE,
        LIST_EMPTY
    }
}
