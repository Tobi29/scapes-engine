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
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.Streams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface SQLDatabase {
    void replace(String table, String[] columns, List<Object[]> rows)
            throws IOException;

    default void replace(String table, String[] columns, Object[]... rows)
            throws IOException {
        replace(table, columns, Arrays.asList(rows));
    }

    void insert(String table, String[] columns, List<Object[]> rows)
            throws IOException;

    default void insert(String table, String[] columns, Object[]... rows)
            throws IOException {
        insert(table, columns, Arrays.asList(rows));
    }

    default List<Object[]> query(String table, String[] columns,
            List<Pair<String, Object>> matches) throws IOException {
        List<String> matchesList = new ArrayList<>(matches.size());
        List<Object> values = new ArrayList<>(matches.size());
        Streams.forEach(matches, pair -> {
            matchesList.add(pair.a);
            values.add(pair.b);
        });
        return compileQuery(table, columns, matchesList).run(values);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default List<Object[]> query(String table, String[] columns,
            Pair... matches) throws IOException {
        return query(table, columns, Arrays.asList(matches));
    }

    SQLQuery compileQuery(String table, String[] columns, List<String> matches);

    default SQLQuery compileQuery(String table, String[] columns,
            String... matches) {
        return compileQuery(table, columns, Arrays.asList(matches));
    }

    void delete(String table, List<Pair<String, Object>> matches)
            throws IOException;

    @SuppressWarnings({"unchecked", "rawtypes"})
    default void delete(String table, Pair... matches) throws IOException {
        delete(table, Arrays.asList(matches));
    }

    void createTable(String name, Optional<String> primaryKey,
            List<SQLColumn> columns) throws IOException;

    default void createTable(String name, Optional<String> primaryKey,
            SQLColumn... columns) throws IOException {
        createTable(name, primaryKey, Arrays.asList(columns));
    }

    default void createTable(String name, SQLColumn... columns)
            throws IOException {
        createTable(name, Optional.empty(), columns);
    }

    default void createTable(String name, String primaryKey,
            SQLColumn... columns) throws IOException {
        createTable(name, Optional.of(primaryKey), columns);
    }

    void dropTable(String name) throws IOException;
}
