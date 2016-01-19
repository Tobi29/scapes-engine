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
