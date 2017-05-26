/*
 * Copyright 2012-2017 Tobi29
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

package org.tobi29.scapes.engine.sql.mysql

import org.tobi29.scapes.engine.sql.*
import org.tobi29.scapes.engine.utils.io.IOException
import org.tobi29.scapes.engine.utils.use
import java.security.AccessController
import java.security.PrivilegedAction
import java.security.PrivilegedActionException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class MySQLDatabase(private val connection: Connection) : SQLDatabase {
    init {
        val security = System.getSecurityManager()
        security?.checkPermission(RuntimePermission("scapes.mysql"))
    }

    override fun createTable(name: String,
                             primaryKey: Array<out String>,
                             columns: Array<out SQLColumn>) {
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
            sql.append(sqlType(column.type, column.extra))
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
        access {
            createStatement().use { statement ->
                statement.executeUpdate(compiled)
            }
        }

    }

    override fun dropTable(name: String) {
        val compiled = "DROP TABLE IF EXISTS $name;"
        access {
            createStatement().use { statement ->
                statement.executeUpdate(compiled)
            }
        }
    }

    override fun compileQuery(table: String,
                              columns: Array<out String>,
                              matches: Array<out SQLMatch>): SQLQuery {
        val columnSize = columns.size
        val matchesSize = matches.size
        val sql = StringBuilder(columnSize shl 5)
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
        sql.append(" FROM ").append(table).append(" WHERE ")
        sqlWhere(matches, sql)
        sql.append(';')
        val compiled = sql.toString()
        return { values ->
            if (values.size != matchesSize) {
                throw IllegalArgumentException(
                        "Amount of query values (${values.size}) does not match amount of matches ($matchesSize)")
            }
            access {
                prepareStatement(compiled).use { statement ->
                    // MariaDB specific optimization
                    statement.fetchSize = Int.MIN_VALUE
                    var i = 1
                    i = resolveObjects(values, statement, i)
                    val result = statement.executeQuery()
                    val rows = ArrayList<Array<Any?>>()
                    while (result.next()) {
                        rows.add(resolveResult(result, columns.size))
                    }
                    return@use rows
                }
            }
        }
    }

    override fun compileInsert(table: String,
                               columns: Array<out String>): SQLInsert {
        val columnSize = columns.size
        val prefix = StringBuilder(columnSize shl 3)
        prefix.append("INSERT IGNORE INTO ").append(table).append(" (")
        var first = true
        for (column in columns) {
            if (first) {
                first = false
            } else {
                prefix.append(',')
            }
            prefix.append(column)
        }
        prefix.append(") VALUES ")
        val compiledPrefix = prefix.toString()
        val compiledSuffix = ";"
        return { values ->
            val sql = StringBuilder(columnSize shl 5)
            sql.append(compiledPrefix)
            first = true
            for (row in values) {
                if (row.size != columnSize) {
                    throw IllegalArgumentException(
                            "Amount of updated values (${row.size}) does not match amount of columns ($columnSize)")
                }
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
            sql.append(compiledSuffix)
            val compiled = sql.toString()
            access {
                prepareStatement(compiled).use { statement ->
                    var i = 1
                    for (row in values) {
                        i = resolveObjects(row, statement, i)
                    }
                    statement.executeUpdate()
                }
            }
        }
    }

    override fun compileUpdate(table: String,
                               matches: Array<out SQLMatch>,
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
            sql.append(column).append("'=?")
        }
        sql.append(" WHERE ")
        sqlWhere(matches, sql)
        sql.append(';')
        val compiled = sql.toString()
        return { values, updates ->
            if (updates.size != columnsSize) {
                throw IllegalArgumentException(
                        "Amount of updated values (${updates.size}) does not match amount of columns ($columnsSize)")
            }
            access {
                prepareStatement(compiled).use { statement ->
                    var i = 1
                    i = resolveObjects(updates, statement, i)
                    i = resolveObjects(values, statement, i)
                    statement.executeUpdate()
                }
            }
        }
    }

    override fun compileReplace(table: String,
                                columns: Array<out String>): SQLReplace {
        val columnsSize = columns.size
        val prefix = StringBuilder(columnsSize shl 4)
        prefix.append("INSERT INTO ").append(table).append(" (")
        var first = true
        for (column in columns) {
            if (first) {
                first = false
            } else {
                prefix.append(',')
            }
            prefix.append(column)
        }
        prefix.append(") VALUES ")
        val compiledPrefix = prefix.toString()
        val suffix = StringBuilder(columnsSize shl 5)
        suffix.append(" ON DUPLICATE KEY UPDATE ")
        first = true
        for (column in columns) {
            if (first) {
                first = false
            } else {
                suffix.append(',')
            }
            suffix.append(column).append("=VALUES(").append(column).append(")")
        }
        suffix.append(';')
        val compiledSuffix = suffix.toString()
        return { values ->
            val sql = StringBuilder(columnsSize shl 5)
            sql.append(compiledPrefix)
            var first = true
            for (row in values) {
                if (row.size != columnsSize) {
                    throw IllegalArgumentException(
                            "Amount of updated values (${row.size}) does not match amount of columns ($columnsSize)")
                }
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
            sql.append(compiledSuffix)
            val compiled = sql.toString()
            access {
                prepareStatement(compiled).use { statement ->
                    var i = 1
                    for (row in values) {
                        i = resolveObjects(row, statement, i)
                    }
                    statement.executeUpdate()
                }
            }
        }
    }

    override fun compileDelete(table: String,
                               matches: Array<out SQLMatch>): SQLDelete {
        val sql = StringBuilder(64)
        sql.append("DELETE FROM ").append(table).append(" WHERE ")
        sqlWhere(matches, sql)
        sql.append(';')
        val compiled = sql.toString()
        return { values ->
            access {
                prepareStatement(compiled).use { statement ->
                    var i = 1
                    i = resolveObjects(values, statement, i)
                    statement.executeUpdate()
                }
            }
        }
    }

    private inline fun <R> access(crossinline block: Connection.() -> R): R {
        try {
            try {
                return AccessController.doPrivileged(PrivilegedAction {
                    synchronized(connection) {
                        return@PrivilegedAction block(connection)
                    }
                })
            } catch (e: PrivilegedActionException) {
                e.cause?.let { throw it }
                throw e
            }
        } catch (e: SQLException) {
            throw IOException(e)
        }
    }

    private fun resolveObjects(matches: Array<out Any?>,
                               statement: PreparedStatement,
                               index: Int): Int {
        var i = index
        for (match in matches) {
            resolveObject(match, i++, statement)
        }
        return i
    }

    private fun resolveObject(value: Any?,
                              i: Int,
                              statement: PreparedStatement) {
        statement.setObject(i, value)
    }

    private fun resolveResult(result: ResultSet,
                              columns: Int): Array<Any?> {
        val row = arrayOfNulls<Any>(columns)
        for (i in 0..columns - 1) {
            val j = i + 1
            row[i] = result.getObject(j)
        }
        return row
    }
}
