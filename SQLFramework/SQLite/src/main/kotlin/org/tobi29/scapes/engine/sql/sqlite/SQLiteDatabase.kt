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

package org.tobi29.scapes.engine.sql.sqlite

import com.almworks.sqlite4java.SQLiteConnection
import com.almworks.sqlite4java.SQLiteConstants
import com.almworks.sqlite4java.SQLiteException
import com.almworks.sqlite4java.SQLiteStatement
import org.tobi29.scapes.engine.sql.*
import org.tobi29.scapes.engine.utils.io.filesystem.FilePath
import org.tobi29.scapes.engine.utils.task.Joiner
import org.tobi29.scapes.engine.utils.task.TaskExecutor
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

class SQLiteDatabase(path: FilePath, taskExecutor: TaskExecutor,
                     config: SQLiteConfig) : SQLDatabase, AutoCloseable {
    private val queue = ConcurrentLinkedQueue<() -> Unit>()
    private val connection: SQLiteConnection
    private val joiner: Joiner
    private var exception: Exception? = null

    init {
        connection = SQLiteConnection(File(path.toUri()))
        joiner = taskExecutor.runThread({ joiner ->
            try {
                connection.open()
                statement("PRAGMA secure_delete = ${config.secureDelete}")
                statement("PRAGMA foreign_keys = ${config.foreignKeys}")
                statement("PRAGMA journal_mode = ${config.journalMode}")
                statement("PRAGMA synchronous = ${config.synchronous}")
            } catch (e: SQLiteException) {
                if (exception == null) {
                    exception = e
                }
                return@runThread
            }
            try {
                while (!joiner.marked || !queue.isEmpty()) {
                    while (!queue.isEmpty()) {
                        queue.poll()()
                    }
                    joiner.sleep()
                }
                assert(queue.isEmpty())
            } finally {
                connection.dispose()
            }
        }, "SQLite")
    }

    private fun statement(sql: String) {
        val statement = connection.prepare(sql)
        try {
            while (statement.step()) {
            }
        } finally {
            statement.dispose()
        }
    }

    private fun access(connection: () -> Unit) {
        queue.add({
            try {
                connection()
            } catch (e: IOException) {
                if (exception == null) {
                    exception = e
                }
            } catch (e: SQLiteException) {
                if (exception == null) {
                    exception = e
                }
            }
        })
        joiner.wake()
        // This throws earlier exceptions, not current ones
        if (exception != null) {
            throw IOException(exception)
        }
    }

    private fun <R> accessReturn(connection: () -> R): R {
        val joinable = Joiner.BasicJoinable()
        val output = AtomicReference<R>()
        queue.add({
            try {
                output.set(connection())
            } catch (e: IOException) {
                if (exception == null) {
                    exception = e
                }
            } catch (e: SQLiteException) {
                if (exception == null) {
                    exception = e
                }
            } finally {
                joinable.join()
            }
        })
        joiner.wake()
        joinable.joiner.join()
        if (exception != null) {
            throw IOException(exception)
        }
        return output.get()
    }

    override fun replace(table: String,
                         columns: Array<out String>,
                         rows: List<Array<out Any?>>) {
        val rowsSafe = ArrayList<Array<out Any?>>(rows.size)
        rowsSafe.addAll(rows)
        val sql = StringBuilder(columns.size shl 5)
        sql.append("INSERT OR REPLACE INTO ").append(table).append(" (")
        var first = true
        for (column in columns) {
            if (first) {
                first = false
            } else {
                sql.append(',')
            }
            sql.append(column)
        }
        sql.append(") VALUES ")
        first = true
        for (row in rowsSafe) {
            if (first) {
                first = false
                sql.append('(')
            } else {
                sql.append(",(")
            }
            var rowFirst = true
            for (ignored in row) {
                if (rowFirst) {
                    rowFirst = false
                } else {
                    sql.append(',')
                }
                sql.append('?')
            }
            sql.append(')')
        }
        sql.append(';')
        val compiled = sql.toString()
        access({
            val statement = connection.prepare(compiled)
            try {
                var i = 1
                for (row in rowsSafe) {
                    for (`object` in row) {
                        resolveObject(`object`, i++, statement)
                    }
                }
                while (statement.step()) {
                }
            } finally {
                statement.dispose()
            }
        })
    }

    override fun insert(table: String,
                        columns: Array<out String>,
                        rows: List<Array<out Any?>>) {
        val rowsSafe = ArrayList<Array<out Any?>>(rows.size)
        rowsSafe.addAll(rows)
        val sql = StringBuilder(columns.size shl 5)
        sql.append("INSERT OR IGNORE INTO '").append(table).append("' (")
        var first = true
        for (column in columns) {
            if (first) {
                first = false
            } else {
                sql.append(',')
            }
            sql.append(column)
        }
        sql.append(") VALUES ")
        first = true
        for (row in rowsSafe) {
            if (first) {
                first = false
            } else {
                sql.append(',')
            }
            sql.append('(')
            var rowFirst = true
            for (ignored in row) {
                if (rowFirst) {
                    rowFirst = false
                } else {
                    sql.append(',')
                }
                sql.append('?')
            }
            sql.append(')')
        }
        sql.append(';')
        val compiled = sql.toString()
        access({
            val statement = connection.prepare(compiled)
            try {
                var i = 1
                for (row in rowsSafe) {
                    for (`object` in row) {
                        resolveObject(`object`, i++, statement)
                    }
                }
                while (statement.step()) {
                }
            } finally {
                statement.dispose()
            }
        })
    }

    override fun createTable(name: String,
                             primaryKey: List<String>,
                             columns: List<SQLColumn>) {
        val sql = StringBuilder(512)
        sql.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (")
        var first = true
        for (column in columns) {
            if (first) {
                first = false
            } else {
                sql.append(',')
            }
            sql.append(column.name).append(' ')
            sql.append(resolveType(column.type, column.extra))
            if (column.notNull) {
                sql.append(" NOT NULL")
            }
            if (column.unique) {
                sql.append(" UNIQUE")
            }
        }
        sql.append(", PRIMARY KEY (")
        first = true
        for (column in primaryKey) {
            if (first) {
                first = false
            } else {
                sql.append(',')
            }
            sql.append(column)
        }
        sql.append(')')
        for (column in columns) {
            val foreignKey = column.foreignKey
            if (foreignKey != null) {
                sql.append(", FOREIGN KEY (").append(column.name).append(')')
                sql.append(" REFERENCES ").append(foreignKey.table)
                sql.append('(').append(foreignKey.column).append(')')
                sql.append(" ON UPDATE ").append(foreignKey.onUpdate.sql)
                sql.append(" ON DELETE ").append(foreignKey.onDelete.sql)
            }
        }
        sql.append(");")
        val compiled = sql.toString()
        access({
            val statement = connection.prepare(compiled)
            try {
                while (statement.step()) {
                }
            } finally {
                statement.dispose()
            }
        })
    }

    override fun dropTable(name: String) {
        val compiled = "DROP TABLE IF EXISTS $name;"
        access({
            val statement = connection.prepare(compiled)
            try {
                while (statement.step()) {
                }
            } finally {
                statement.dispose()
            }
        })
    }

    override fun compileQuery(table: String,
                              columns: Array<out String>,
                              matches: List<String>): SQLQuery {
        val sql = StringBuilder(columns.size shl 5)
        sql.append("SELECT ")
        var first = true
        for (column in columns) {
            if (first) {
                first = false
            } else {
                sql.append(',')
            }
            sql.append(column)
        }
        sql.append(" FROM ").append(table)
        whereSQL(matches, sql)
        sql.append(';')
        val compiled = sql.toString()
        return object : SQLQuery {
            override fun run(values: List<Any?>): List<Array<Any?>> {
                return accessReturn({
                    val statement = connection.prepare(compiled)
                    try {
                        var i = 1
                        i = whereParameters(values, statement, i)
                        val rows = ArrayList<Array<Any?>>()
                        while (statement.step()) {
                            rows.add(resolveResult(statement, columns.size))
                        }
                        return@accessReturn rows
                    } finally {
                        statement.dispose()
                    }
                })
            }
        }
    }

    override fun compileUpdate(table: String,
                               matches: List<String>,
                               columns: Array<out String>): SQLUpdate {
        val columnsSize = columns.size
        val sql = StringBuilder(columns.size shl 5)
        sql.append("UPDATE ").append(table).append(" SET ")
        var first = true
        for (column in columns) {
            if (first) {
                first = false
            } else {
                sql.append(',')
            }
            sql.append(column).append("=?")
        }
        whereSQL(matches, sql)
        sql.append(';')
        val compiled = sql.toString()
        return object : SQLUpdate {
            override fun run(values: List<Any?>,
                             updates: List<Any?>) {
                if (updates.size != columnsSize) {
                    throw IllegalArgumentException(
                            "Amount of updated values (${updates.size}) does not match amount of columns ($columnsSize)")
                }
                access({
                    val statement = connection.prepare(compiled)
                    try {
                        var i = 1
                        for (update in updates) {
                            resolveObject(update, i++, statement)
                        }
                        i = whereParameters(values, statement, i)
                        while (statement.step()) {
                        }
                    } finally {
                        statement.dispose()
                    }
                })
            }
        }
    }

    override fun compileDelete(table: String,
                               matches: List<String>): SQLDelete {
        val sql = StringBuilder(64)
        sql.append("DELETE FROM ").append(table)
        whereSQL(matches, sql)
        sql.append(';')
        val compiled = sql.toString()
        return object : SQLDelete {
            override fun run(values: List<Any?>) {
                access({
                    val statement = connection.prepare(compiled)
                    try {
                        var i = 1
                        i = whereParameters(values, statement, i)
                        while (statement.step()) {
                        }
                    } finally {
                        statement.dispose()
                    }
                })
            }
        }
    }

    override fun close() {
        joiner.join()
    }

    private fun whereSQL(matches: List<String>,
                         sql: StringBuilder) {
        sql.append(" WHERE ")
        var first = true
        for (match in matches) {
            if (first) {
                first = false
            } else {
                sql.append(" AND ")
            }
            sql.append(match).append("=?")
        }
    }

    private fun whereParameters(matches: List<Any?>,
                                statement: SQLiteStatement,
                                index: Int): Int {
        var i = index
        for (match in matches) {
            resolveObject(match, i++, statement)
        }
        return i
    }

    private fun resolveObject(value: Any?,
                              i: Int,
                              statement: SQLiteStatement) {
        if (value is Byte) {
            statement.bind(i, value.toInt())
        } else if (value is Short) {
            statement.bind(i, value.toInt())
        } else if (value is Int) {
            statement.bind(i, value)
        } else if (value is Long) {
            statement.bind(i, value)
        } else if (value is Float) {
            statement.bind(i, value.toDouble())
        } else if (value is Double) {
            statement.bind(i, value)
        } else if (value is ByteArray) {
            statement.bind(i, value)
        } else if (value is String) {
            statement.bind(i, value)
        } else if (value == null) {
            statement.bindNull(i)
        }
    }

    private fun resolveResult(statement: SQLiteStatement,
                              columns: Int): Array<Any?> {
        val row = arrayOfNulls<Any?>(columns)
        for (i in 0..columns - 1) {
            when (statement.columnType(i)) {
                SQLiteConstants.SQLITE_NULL -> row[i] = null
                SQLiteConstants.SQLITE_INTEGER -> row[i] = statement.columnLong(
                        i)
                SQLiteConstants.SQLITE_FLOAT -> row[i] = statement.columnDouble(
                        i)
                SQLiteConstants.SQLITE_BLOB -> row[i] = statement.columnBlob(i)
                SQLiteConstants.SQLITE_TEXT -> row[i] = statement.columnString(
                        i)
            }
        }
        return row
    }

    private fun resolveType(type: SQLType,
                            extra: String?): String {
        val typeStr = type.toString()
        if (extra != null) {
            return "$typeStr($extra)"
        }
        return typeStr
    }
}
