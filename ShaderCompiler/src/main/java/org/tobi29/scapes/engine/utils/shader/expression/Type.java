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

package org.tobi29.scapes.engine.utils.shader.expression;

import java8.util.Optional;

public class Type {
    public final Types type;
    public final Optional<Expression> array;
    public final boolean constant;
    public final Precision precision;

    public Type(Types type, boolean constant, Precision precision) {
        this(type, Optional.empty(), constant, precision);
    }

    public Type(Types type, Expression array, boolean constant,
            Precision precision) {
        this(type, Optional.of(array), constant, precision);
    }

    public Type(Types type, Optional<Expression> array, boolean constant,
            Precision precision) {
        this.type = type;
        this.array = array;
        this.constant = constant;
        this.precision = precision;
    }
}
