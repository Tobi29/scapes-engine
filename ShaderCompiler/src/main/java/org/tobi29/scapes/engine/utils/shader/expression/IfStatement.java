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

public class IfStatement extends Statement {
    public final Expression condition;
    public final Statement statement;
    public final Optional<Statement> statementElse;

    public IfStatement(Expression condition, Statement statement) {
        this(condition, statement, Optional.empty());
    }

    public IfStatement(Expression condition, Statement statement,
            Statement statementElse) {
        this(condition, statement, Optional.of(statementElse));
    }

    public IfStatement(Expression condition, Statement statement,
            Optional<Statement> statementElse) {
        this.condition = condition;
        this.statement = statement;
        this.statementElse = statementElse;
    }
}
