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

package org.tobi29.scapes.engine.sql;

import java8.util.Optional;

public class SQLColumn {
    private final String name;
    private final SQLType type;
    private final Optional<String> extra;

    public SQLColumn(String name, SQLType type) {
        this(name, type, Optional.empty());
    }

    public SQLColumn(String name, SQLType type, String extra) {
        this(name, type, Optional.of(extra));
    }

    public SQLColumn(String name, SQLType type, Optional<String> extra) {
        this.name = name;
        this.type = type;
        this.extra = extra;
    }

    public String name() {
        return name;
    }

    public SQLType type() {
        return type;
    }

    public Optional<String> extra() {
        return extra;
    }
}
