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

import org.tobi29.scapes.engine.sql.*
import org.tobi29.scapes.engine.utils.io.use
import java.io.IOException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class MySQLDatabase(private val connection: Connection) : SQLDatabase {
    override fun replace(table: String,
                         columns: Array<out String>,
                         rows: List<Array<out Any?>>) {
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
            sql.append(column).append("=VALUES(").append(column).append(")")
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

    override fun insert(table: String,
                        columns: Array<out String>,
                        rows: List<Array<out Any?>>) {
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
        try {
            connection.createStatement().use { statement ->
                statement.executeUpdate(compiled)
            }
        } catch (e: SQLException) {
            throw IOException(e)
        }

    }

    override fun dropTable(name: String) {
        val compiled = "DROP TABLE IF EXISTS '$name';"
        try {
            connection.createStatement().use { statement ->
                statement.executeUpdate(compiled)
            }
        } catch (e: SQLException) {
            throw IOException(e)
        }
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
                try {
                    return connection.prepareStatement(
                            compiled).use { statement ->
                        // MariaDB specific optimization
                        statement.fetchSize = Int.MIN_VALUE
                        var i = 1
                        i = whereParameters(matches, statement, i)
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
            sql.append(column).append("'=?")
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
                try {
                    return connection.prepareStatement(
                            compiled).use { statement ->
                        var i = 1
                        for (update in updates) {
                            resolveObject(update, i++, statement)
                        }
                        i = whereParameters(values, statement, i)
                        statement.executeUpdate()
                    }
                } catch (e: SQLException) {
                    throw IOException(e)
                }
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
                try {
                    return connection.prepareStatement(
                            compiled).use { statement ->
                        var i = 1
                        i = whereParameters(matches, statement, i)
                        statement.executeUpdate()
                    }
                } catch (e: SQLException) {
                    throw IOException(e)
                }
            }
        }
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
                                statement: PreparedStatement,
                                index: Int): Int {
        var i = index
        for (match in matches) {
            resolveObject(match, i++, statement)
        }
        return i
    }

    private fun resolveObject(`object`: Any?,
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
