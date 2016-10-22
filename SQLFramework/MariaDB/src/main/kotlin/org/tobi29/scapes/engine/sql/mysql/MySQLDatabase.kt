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

package org.tobi29.scapes.engine.sql.mysql

import org.tobi29.scapes.engine.sql.SQLColumn
import org.tobi29.scapes.engine.sql.SQLDatabase
import org.tobi29.scapes.engine.sql.SQLQuery
import org.tobi29.scapes.engine.sql.SQLType
import org.tobi29.scapes.engine.utils.io.use
import java.io.IOException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class MySQLDatabase(private val connection: Connection) : SQLDatabase {
    override fun replace(table: String,
                         columns: Array<String>,
                         rows: List<Array<Any>>) {
        val sql = StringBuilder(columns.size shl 5)
        sql.append("INSERT INTO ").append(table).append(" (")
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
        for (row in rows) {
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
        sql.append(" ON DUPLICATE KEY UPDATE ")
        first = true
        for (column in columns) {
            if (first) {
                first = false
            } else {
                sql.append(',')
            }
            sql.append(column).append("=VALUES(").append(column).append(')')
        }
        sql.append(';')
        val compiled = sql.toString()
        try {
            val oi: PreparedStatement = connection.prepareStatement(compiled)
            oi.use { statement: PreparedStatement ->
                var i = 1
                for (row in rows) {
                    for (`object` in row) {
                        resolveObject(`object`, i++, statement)
                    }
                }
                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            throw IOException(e)
        }

    }

    override fun insert(table: String,
                        columns: Array<String>,
                        rows: List<Array<Any>>) {
        val sql = StringBuilder(columns.size shl 5)
        sql.append("INSERT IGNORE INTO ").append(table).append(" (")
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
        for (row in rows) {
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
        try {
            connection.prepareStatement(compiled).use { statement ->
                var i = 1
                for (row in rows) {
                    for (`object` in row) {
                        resolveObject(`object`, i++, statement)
                    }
                }
                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            throw IOException(e)
        }

    }

    override fun compileQuery(table: String,
                              columns: Array<String>,
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
        sql.append(" FROM ").append(table).append(" WHERE ")

        first = true
        for (match in matches) {
            if (first) {
                first = false
            } else {
                sql.append(',')
            }
            sql.append(match).append("=?")
        }
        sql.append(';')
        val compiled = sql.toString()
        return object : SQLQuery {
            override fun run(values: List<Any>): List<Array<Any?>> {
                try {
                    return connection.prepareStatement(
                            compiled).use { statement ->
                        // MariaDB specific optimization
                        statement.fetchSize = Int.MIN_VALUE
                        var i = 1
                        for (value in values) {
                            resolveObject(value, i++, statement)
                        }
                        val result = statement.executeQuery()
                        val rows = ArrayList<Array<Any?>>()
                        while (result.next()) {
                            rows.add(resolveResult(result, columns.size))
                        }
                        return@use rows
                    }
                } catch (e: SQLException) {
                    throw IOException(e)
                }
            }
        }
    }

    override fun delete(table: String,
                        matches: List<Pair<String, Any>>) {
        val sql = StringBuilder(64)
        sql.append("DELETE FROM ").append(table).append(" WHERE ")
        var first = true
        for (match in matches) {
            if (first) {
                first = false
            } else {
                sql.append(',')
            }
            sql.append(match.first).append("=?")
        }
        sql.append(';')
        val compiled = sql.toString()
        try {
            connection.prepareStatement(compiled).use { statement ->
                var i = 1
                for (match in matches) {
                    resolveObject(match.second, i++, statement)
                }
                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            throw IOException(e)
        }

    }

    override fun createTable(name: String,
                             primaryKey: String?,
                             columns: List<SQLColumn>) {
        val sql = StringBuilder(64)
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
        }
        if (primaryKey != null) {
            sql.append(", PRIMARY KEY (").append(primaryKey).append(')')
        }
        sql.append(");")
        val compiled = sql.toString()
        try {
            connection.createStatement().use { statement ->
                statement.executeUpdate(compiled)
            }
        } catch (e: SQLException) {
            throw IOException(e)
        }

    }

    override fun dropTable(name: String) {
        val compiled = "DROP TABLE IF EXISTS $name;"
        try {
            connection.createStatement().use { statement ->
                statement.executeUpdate(compiled)
            }
        } catch (e: SQLException) {
            throw IOException(e)
        }

    }

    private fun resolveObject(`object`: Any,
                              i: Int,
                              statement: PreparedStatement) {
        statement.setObject(i, `object`)
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

    private fun resolveType(type: SQLType,
                            extra: String?): String {
        val typeStr = type.toString()
        if (extra != null) {
            return "$typeStr($extra)"
        }
        return typeStr
    }
}