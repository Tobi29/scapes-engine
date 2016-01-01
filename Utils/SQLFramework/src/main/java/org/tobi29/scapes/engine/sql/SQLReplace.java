package org.tobi29.scapes.engine.sql;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public interface SQLReplace {
    void run(List<Object[]> rows) throws IOException;

    default void run(Object[]... rows) throws IOException {
        run(Arrays.asList(rows));
    }
}
