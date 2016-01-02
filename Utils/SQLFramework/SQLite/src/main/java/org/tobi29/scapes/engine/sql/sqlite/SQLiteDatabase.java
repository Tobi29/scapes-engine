package org.tobi29.scapes.engine.sql.sqlite;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteConstants;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import java8.util.Optional;
import org.tobi29.scapes.engine.sql.SQLColumn;
import org.tobi29.scapes.engine.sql.SQLDatabase;
import org.tobi29.scapes.engine.sql.SQLQuery;
import org.tobi29.scapes.engine.sql.SQLType;
import org.tobi29.scapes.engine.utils.MutableSingle;
import org.tobi29.scapes.engine.utils.Pair;
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath;
import org.tobi29.scapes.engine.utils.task.Joiner;
import org.tobi29.scapes.engine.utils.task.TaskExecutor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SQLiteDatabase implements SQLDatabase, AutoCloseable {
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final SQLiteConnection connection;
    private final Joiner joiner;
    private Optional<Exception> exception = Optional.empty();

    public SQLiteDatabase(FilePath path, TaskExecutor taskExecutor,
            SQLiteConfig config) {
        connection = new SQLiteConnection(new File(path.toUri()));
        joiner = taskExecutor.runTask(joiner -> {
            try {
                connection.open();
                statement("PRAGMA secure_delete = " + config.secureDelete);
                statement("PRAGMA journal_mode = " + config.journalMode);
                statement("PRAGMA synchronous = " + config.synchronous);
            } catch (SQLiteException e) {
                exception = Optional.of(e);
                return;
            }
            try {
                while (!joiner.marked() || !queue.isEmpty()) {
                    while (!queue.isEmpty()) {
                        queue.poll().run();
                    }
                    joiner.sleep();
                }
                assert queue.isEmpty();
            } finally {
                connection.dispose();
            }
        }, "SQLite");
    }

    private void statement(String sql) throws SQLiteException {
        SQLiteStatement statement = connection.prepare(sql);
        try {
            while (statement.step()) {
            }
        } finally {
            statement.dispose();
        }
    }

    private void access(SQLiteRunnable connection) throws IOException {
        queue.add(() -> {
            try {
                connection.run();
            } catch (IOException | SQLiteException e) {
                if (!exception.isPresent()) {
                    exception = Optional.of(e);
                }
            }
        });
        joiner.wake();
        // This throws earlier exceptions, not current ones
        if (exception.isPresent()) {
            throw new IOException(exception.get());
        }
    }

    private <R> R access(SQLiteSupplier<R> connection) throws IOException {
        Joiner.Joinable joinable = new Joiner.Joinable();
        MutableSingle<R> output = new MutableSingle<>();
        queue.add(() -> {
            try {
                output.a = connection.get();
            } catch (IOException | SQLiteException e) {
                if (!exception.isPresent()) {
                    exception = Optional.of(e);
                }
            } finally {
                joinable.join();
            }
        });
        joiner.wake();
        joinable.joiner().join();
        if (exception.isPresent()) {
            throw new IOException(exception.get());
        }
        return output.a;
    }

    @Override
    public void replace(String table, String[] columns, List<Object[]> rows)
            throws IOException {
        List<Object[]> rowsSafe = new ArrayList<>(rows.size());
        rowsSafe.addAll(rows);
        StringBuilder sql = new StringBuilder(columns.length << 5);
        sql.append("INSERT OR REPLACE INTO ").append(table).append(" (");
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
        for (Object[] row : rowsSafe) {
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
        access(() -> {
            SQLiteStatement statement = connection.prepare(compiled);
            try {
                int i = 1;
                for (Object[] row : rowsSafe) {
                    for (Object object : row) {
                        resolveObject(object, i++, statement);
                    }
                }
                while (statement.step()) {
                }
            } finally {
                statement.dispose();
            }
        });
    }

    @Override
    public void insert(String table, String[] columns, List<Object[]> rows)
            throws IOException {
        List<Object[]> rowsSafe = new ArrayList<>(rows.size());
        rowsSafe.addAll(rows);
        StringBuilder sql = new StringBuilder(columns.length << 5);
        sql.append("INSERT OR IGNORE INTO ").append(table).append(" (");
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
        for (Object[] row : rowsSafe) {
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
        access(() -> {
            SQLiteStatement statement = connection.prepare(compiled);
            try {
                int i = 1;
                for (Object[] row : rowsSafe) {
                    for (Object object : row) {
                        resolveObject(object, i++, statement);
                    }
                }
                while (statement.step()) {
                }
            } finally {
                statement.dispose();
            }
        });
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
        return values -> access(() -> {
            SQLiteStatement statement = connection.prepare(compiled);
            try {
                int i = 1;
                for (Object value : values) {
                    resolveObject(value, i++, statement);
                }
                List<Object[]> rows = new ArrayList<>();
                while (statement.step()) {
                    rows.add(resolveResult(statement, columns.length));
                }
                return rows;
            } finally {
                statement.dispose();
            }
        });
    }

    @Override
    public void delete(String table, List<Pair<String, Object>> matches)
            throws IOException {
        List<Pair<String, Object>> matchesSafe =
                new ArrayList<>(matches.size());
        matchesSafe.addAll(matches);
        StringBuilder sql = new StringBuilder(64);
        sql.append("DELETE FROM ").append(table).append(" WHERE ");
        boolean first = true;
        for (Pair<String, Object> match : matchesSafe) {
            if (first) {
                first = false;
            } else {
                sql.append(',');
            }
            sql.append(match.a).append("=?");
        }
        sql.append(';');
        String compiled = sql.toString();
        access(() -> {
            SQLiteStatement statement = connection.prepare(compiled);
            try {
                int i = 1;
                for (Pair<String, Object> match : matchesSafe) {
                    resolveObject(match.b, i++, statement);
                }
                while (statement.step()) {
                }
            } finally {
                statement.dispose();
            }
        });
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
        access(() -> {
            SQLiteStatement statement = connection.prepare(compiled);
            try {
                while (statement.step()) {
                }
            } finally {
                statement.dispose();
            }
        });
    }

    @Override
    public void dropTable(String name) throws IOException {
        String compiled = "DROP TABLE IF EXISTS " + name + ';';
        access(() -> {
            SQLiteStatement statement = connection.prepare(compiled);
            try {
                while (statement.step()) {
                }
            } finally {
                statement.dispose();
            }
        });
    }

    @Override
    public void close() {
        joiner.join();
    }

    private void resolveObject(Object object, int i, SQLiteStatement statement)
            throws SQLiteException {
        if (object instanceof Byte) {
            statement.bind(i, (Byte) object);
        } else if (object instanceof Short) {
            statement.bind(i, (Short) object);
        } else if (object instanceof Integer) {
            statement.bind(i, (Integer) object);
        } else if (object instanceof Long) {
            statement.bind(i, (Long) object);
        } else if (object instanceof Float) {
            statement.bind(i, (Float) object);
        } else if (object instanceof Double) {
            statement.bind(i, (Double) object);
        } else if (object instanceof byte[]) {
            statement.bind(i, (byte[]) object);
        } else if (object instanceof String) {
            statement.bind(i, (String) object);
        } else if (object == null) {
            statement.bindNull(i);
        }
    }

    private Object[] resolveResult(SQLiteStatement statement, int columns)
            throws SQLiteException {
        Object[] row = new Object[columns];
        for (int i = 0; i < columns; i++) {
            switch (statement.columnType(i)) {
                case SQLiteConstants.SQLITE_NULL:
                    row[i] = null;
                    break;
                case SQLiteConstants.SQLITE_INTEGER:
                    row[i] = statement.columnLong(i);
                    break;
                case SQLiteConstants.SQLITE_FLOAT:
                    row[i] = statement.columnDouble(i);
                    break;
                case SQLiteConstants.SQLITE_BLOB:
                    row[i] = statement.columnBlob(i);
                    break;
                case SQLiteConstants.SQLITE_TEXT:
                    row[i] = statement.columnString(i);
                    break;
            }
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

    private interface SQLiteRunnable {
        void run() throws IOException, SQLiteException;
    }

    private interface SQLiteSupplier<T> {
        T get() throws IOException, SQLiteException;
    }
}
