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

package org.tobi29.scapes.engine.sql.sqlite

import org.tobi29.scapes.engine.sql.*
import org.tobi29.scapes.engine.utils.io.use
import java.io.IOException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class SQLiteDatabase(private val connection: Connection) : SQLDatabase {
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
        return object : SQLQuery {
            override fun invoke(values: Array<out Any?>): List<Array<Any?>> {
                if (values.size != matchesSize) {
                    throw IllegalArgumentException(
                            "Amount of query values (${values.size}) does not match amount of matches ($matchesSize)")
                }
                try {
                    return connection.prepareStatement(
                            compiled).use { statement ->
                        var i = 1
                        i = resolveObjects(values, statement, i)
                        val result = statement.executeQuery()
                        val rows = ArrayList<Array<Any?>>()
                        while (result.next()) {
                            rows.add(resolveResult(result, columnSize))
                        }
                        return@use rows
                    }
                } catch (e: SQLException) {
                    throw IOException(e)
                }
            }
        }
    }

    override fun compileInsert(table: String,
                               columns: Array<out String>): SQLInsert {
        val columnSize = columns.size
        val prefix = StringBuilder(columnSize shl 3)
        prefix.append("INSERT OR IGNORE INTO '").append(table).append("' (")
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
        return object : SQLInsert {
            override fun invoke(values: Array<out Array<out Any?>>) {
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
                try {
                    connection.prepareStatement(compiled).use { statement ->
                        var i = 1
                        for (row in values) {
                            i = resolveObjects(row, statement, i)
                        }
                        statement.executeUpdate()
                    }
                } catch (e: SQLException) {
                    throw IOException(e)
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
            sql.append(column).append("=?")
        }
        sql.append(" WHERE ")
        sqlWhere(matches, sql)
        sql.append(';')
        val compiled = sql.toString()
        return object : SQLUpdate {
            override fun invoke(values: Array<out Any?>,
                                updates: Array<out Any?>) {
                if (updates.size != columnsSize) {
                    throw IllegalArgumentException(
                            "Amount of updated values (${updates.size}) does not match amount of columns ($columnsSize)")
                }
                try {
                    return connection.prepareStatement(
                            compiled).use { statement ->
                        var i = 1
                        i = resolveObjects(updates, statement, i)
                        i = resolveObjects(values, statement, i)
                        statement.executeUpdate()
                    }
                } catch (e: SQLException) {
                    throw IOException(e)
                }
            }
        }
    }

    override fun compileReplace(table: String,
                                columns: Array<out String>): SQLReplace {
        val columnsSize = columns.size
        val prefix = StringBuilder(columnsSize shl 4)
        prefix.append("INSERT OR REPLACE INTO ").append(table).append(" (")
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
        return object : SQLReplace {
            override fun invoke(values: Array<out Array<out Any?>>) {
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
                try {
                    connection.prepareStatement(compiled).use { statement ->
                        var i = 1
                        for (row in values) {
                            i = resolveObjects(row, statement, i)
                        }
                        statement.executeUpdate()
                    }
                } catch (e: SQLException) {
                    throw IOException(e)
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
        return object : SQLDelete {
            override fun invoke(values: Array<out Any?>) {
                try {
                    return connection.prepareStatement(
                            compiled).use { statement ->
                        var i = 1
                        i = resolveObjects(values, statement, i)
                        statement.executeUpdate()
                    }
                } catch (e: SQLException) {
                    throw IOException(e)
                }
            }
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
}
