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

package org.tobi29.scapes.engine.sql.mysql;

import java8.util.Optional;
import org.tobi29.scapes.engine.sql.SQLColumn;
import org.tobi29.scapes.engine.sql.SQLDatabase;
import org.tobi29.scapes.engine.sql.SQLQuery;
import org.tobi29.scapes.engine.sql.SQLType;
import org.tobi29.scapes.engine.utils.Pair;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDatabase implements SQLDatabase {
    private final Connection connection;

    public MySQLDatabase(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void replace(String table, String[] columns, List<Object[]> rows)
            throws IOException {
        StringBuilder sql = new StringBuilder(columns.length << 5);
        sql.append("INSERT INTO ").append(table).append(" (");
        boolean first = true;
        for (String column : columns) {
            if (first) {
                first = false;
            } else {
                sql.append(',');
            }
            sql.append(column);
        }
        sql.append(") VALUES ");
        first = true;
        for (Object[] row : rows) {
            if (first) {
                first = false;
                sql.append('(');
            } else {
                sql.append(",(");
            }
            boolean rowFirst = true;
            for (Object ignored : row) {
                if (rowFirst) {
                    rowFirst = false;
                } else {
                    sql.append(',');
                }
                sql.append('?');
            }
            sql.append(')');
        }
        sql.append(" ON DUPLICATE KEY UPDATE ");
        first = true;
        for (String column : columns) {
            if (first) {
                first = false;
            } else {
                sql.append(',');
            }
            sql.append(column).append("=VALUES(").append(column).append(')');
        }
        sql.append(';');
        String compiled = sql.toString();
        try (PreparedStatement statement = connection
                .prepareStatement(compiled)) {
            int i = 1;
            for (Object[] row : rows) {
                for (Object object : row) {
                    resolveObject(object, i++, statement);
                }
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void insert(String table, String[] columns, List<Object[]> rows)
            throws IOException {
        StringBuilder sql = new StringBuilder(columns.length << 5);
        sql.append("INSERT IGNORE INTO ").append(table).append(" (");
        boolean first = true;
        for (String column : columns) {
            if (first) {
                first = false;
            } else {
                sql.append(',');
            }
            sql.append(column);
        }
        sql.append(") VALUES ");
        first = true;
        for (Object[] row : rows) {
            if (first) {
                first = false;
                sql.append('(');
            } else {
                sql.append(",(");
            }
            boolean rowFirst = true;
            for (Object ignored : row) {
                if (rowFirst) {
                    rowFirst = false;
                } else {
                    sql.append(',');
                }
                sql.append('?');
            }
            sql.append(')');
        }
        sql.append(';');
        String compiled = sql.toString();
        try (PreparedStatement statement = connection
                .prepareStatement(compiled)) {
            int i = 1;
            for (Object[] row : rows) {
                for (Object object : row) {
                    resolveObject(object, i++, statement);
                }
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public SQLQuery compileQuery(String table, String[] columns,
            List<String> matches) {
        StringBuilder sql = new StringBuilder(columns.length << 5);
        sql.append("SELECT ");
        boolean first = true;
        for (String column : columns) {
            if (first) {
                first = false;
            } else {
                sql.append(',');
            }
            sql.append(column);
        }
        sql.append(" FROM ").append(table).append(" WHERE ");

        first = true;
        for (String match : matches) {
            if (first) {
                first = false;
            } else {
                sql.append(',');
            }
            sql.append(match).append("=?");
        }
        sql.append(';');
        String compiled = sql.toString();
        return values -> {
            try (PreparedStatement statement = connection
                    .prepareStatement(compiled)) {
                // MariaDB specific optimization
                statement.setFetchSize(Integer.MIN_VALUE);
                int i = 1;
                for (Object value : values) {
                    resolveObject(value, i++, statement);
                }
                ResultSet result = statement.executeQuery();
                List<Object[]> rows = new ArrayList<>();
                while (result.next()) {
                    rows.add(resolveResult(result, columns.length));
                }
                return rows;
            } catch (SQLException e) {
                throw new IOException(e);
            }
        };
    }

    @Override
    public void delete(String table, List<Pair<String, Object>> matches)
            throws IOException {
        StringBuilder sql = new StringBuilder(64);
        sql.append("DELETE FROM ").append(table).append(" WHERE ");
        boolean first = true;
        for (Pair<String, Object> match : matches) {
            if (first) {
                first = false;
            } else {
                sql.append(',');
            }
            sql.append(match.a).append("=?");
        }
        sql.append(';');
        String compiled = sql.toString();
        try (PreparedStatement statement = connection
                .prepareStatement(compiled)) {
            int i = 1;
            for (Pair<String, Object> match : matches) {
                resolveObject(match.b, i++, statement);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void createTable(String name, Optional<String> primaryKey,
            List<SQLColumn> columns) throws IOException {
        StringBuilder sql = new StringBuilder(64);
        sql.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (");
        boolean first = true;
        for (SQLColumn column : columns) {
            if (first) {
                first = false;
            } else {
                sql.append(',');
            }
            sql.append(column.name()).append(' ');
            sql.append(resolveType(column.type(), column.extra()));
        }
        if (primaryKey.isPresent()) {
            sql.append(", PRIMARY KEY (").append(primaryKey.get()).append(')');
        }
        sql.append(");");
        String compiled = sql.toString();
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(compiled);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void dropTable(String name) throws IOException {
        String compiled = "DROP TABLE IF EXISTS " + name + ';';
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(compiled);
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    private void resolveObject(Object object, int i,
            PreparedStatement statement) throws SQLException {
        statement.setObject(i, object);
    }

    private Object[] resolveResult(ResultSet result, int columns)
            throws SQLException {
        Object[] row = new Object[columns];
        for (int i = 0; i < columns; i++) {
            int j = i + 1;
            row[i] = result.getObject(j);
        }
        return row;
    }

    private String resolveType(SQLType type, Optional<String> extra) {
        String typeStr = type.toString();
        if (extra.isPresent()) {
            return typeStr + '(' + extra.get() + ')';
        }
        return typeStr;
    }
}
