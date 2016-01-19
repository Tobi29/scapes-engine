package org.tobi29.scapes.engine.sql;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public interface SQLQuery {
    List<Object[]> run(List<Object> values) throws IOException;

    default List<Object[]> run(Object... values) throws IOException {
        return run(Arrays.asList(values));
    }
}
