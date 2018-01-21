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

package org.tobi29.sql.sqljet

import org.tmatesoft.sqljet.core.SqlJetException
import org.tmatesoft.sqljet.core.SqlJetTransactionMode
import org.tmatesoft.sqljet.core.internal.ISqlJetMemoryPointer
import org.tmatesoft.sqljet.core.schema.SqlJetConflictAction
import org.tmatesoft.sqljet.core.table.ISqlJetCursor
import org.tmatesoft.sqljet.core.table.SqlJetDb
import org.tobi29.sql.*
import org.tobi29.io.IOException
import org.tobi29.stdex.checkPermission
import java.security.AccessController
import java.security.PrivilegedAction

class SQLJetDatabase(private val connection: SqlJetDb) : SQLDatabase {
    init {
        checkPermission("scapes.sqljet")
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
            transactionWrite {
                createTable(compiled)
            }
        }
    }

    override fun dropTable(name: String) {
        access {
            transactionWrite {
                dropTable(name)
            }
        }
    }

    override fun compileQuery(table: String,
                              columns: Array<out String>,
                              matches: Array<out SQLMatch>): SQLQuery {
        return { values ->
            if (values.size != matches.size) {
                throw IllegalArgumentException(
                        "Amount of query values (${values.size}) does not match amount of matches ($matches.size)")
            }
            val matching = matches.asSequence() zip values.asSequence()
            val rows = ArrayList<Array<Any?>>()
            access {
                transactionRead {
                    val cursor = getTable(table).open()
                    cursor.forEach {
                        if (!matching.filter { (match, value) ->
                            val entry = cursor.getValue(match.name)
                            !sqlMatch(value, entry, match.operator)
                        }.any()) {
                            rows.add(Array(columns.size) {
                                resolveObject(cursor.getValue(columns[it]))
                            })
                        }
                    }
                }
            }
            rows
        }
    }

    override fun compileInsert(table: String,
                               columns: Array<out String>): SQLInsert {
        return { values ->
            access {
                transactionWrite {
                    val sqlTable = getTable(table)
                    values.forEach { row ->
                        val inserts = (columns.asSequence() zip row.asSequence()).toMap()
                        sqlTable.insertByFieldNames(inserts)
                    }
                }
            }
        }
    }

    override fun compileUpdate(table: String,
                               matches: Array<out SQLMatch>,
                               columns: Array<out String>): SQLUpdate {
        return { values, updates ->
            if (updates.size != columns.size) {
                throw IllegalArgumentException(
                        "Amount of updated values (${updates.size}) does not match amount of columns ($columns.size)")
            }
            val matching = matches.asSequence() zip values.asSequence()
            access {
                transactionWrite {
                    val sqlTable = getTable(table)
                    val updating = (columns.asSequence() zip updates.asSequence()).toMap()
                    val cursor = sqlTable.open()
                    cursor.forEach {
                        if (!matching.filter { (match, value) ->
                            val entry = cursor.getValue(match.name)
                            !sqlMatch(value, entry, match.operator)
                        }.any()) {
                            cursor.updateByFieldNames(updating)
                        }
                    }
                }
            }
        }
    }

    override fun compileReplace(table: String,
                                columns: Array<out String>): SQLReplace {
        return { values ->
            access {
                transactionWrite {
                    val sqlTable = getTable(table)
                    values.asSequence().map { row ->
                        (columns.asSequence() zip row.asSequence()).toMap()
                    }.forEach { entry ->
                        sqlTable.insertByFieldNamesOr(
                                SqlJetConflictAction.REPLACE, entry)
                    }
                }
            }
        }
    }

    override fun compileDelete(table: String,
                               matches: Array<out SQLMatch>): SQLDelete {
        return { values ->
            val matching = matches.asSequence() zip values.asSequence()
            access {
                transactionWrite {
                    val sqlTable = getTable(table)
                    val cursor = sqlTable.open()
                    cursor.forEach {
                        if (!matching.filter { (match, value) ->
                            val entry = cursor.getValue(match.name)
                            !sqlMatch(value, entry, match.operator)
                        }.any()) {
                            cursor.delete()
                        }
                    }
                }
            }
        }
    }

    private inline fun <R> access(crossinline block: SqlJetDb.() -> R): R {
        return try {
            AccessController.doPrivileged(PrivilegedAction {
                synchronized(connection) {
                    return@PrivilegedAction block(connection)
                }
            })
        } catch (e: SqlJetException) {
            throw IOException(e)
        }
    }
}

fun sqlMatch(a: Any?,
             b: Any?,
             operator: SQLMatchOperator) = when (operator) {
    SQLMatchOperator.EQUALS -> a.toString() == b.toString()
    SQLMatchOperator.NOT -> a.toString() != b.toString()
    else -> throw UnsupportedOperationException(
            "Match operator not implemented: $operator")
}

fun resolveObject(obj: Any?) = when (obj) {
    is ISqlJetMemoryPointer -> resolveObject(obj)
    else -> obj
}

fun resolveObject(pointer: ISqlJetMemoryPointer) = ByteArray(
        pointer.remaining()).apply { pointer.getBytes(this) }

inline fun <R> SqlJetDb.transactionRead(block: () -> R) =
        transaction(SqlJetTransactionMode.READ_ONLY, block)

inline fun <R> SqlJetDb.transactionWrite(block: () -> R) =
        transaction(SqlJetTransactionMode.WRITE, block)

inline fun <R> SqlJetDb.transactionExclusive(block: () -> R) =
        transaction(SqlJetTransactionMode.EXCLUSIVE, block)

inline fun <R> SqlJetDb.transaction(mode: SqlJetTransactionMode,
                                    block: () -> R): R {
    beginTransaction(mode)
    return try {
        block()
    } finally {
        commit()
    }
}

inline fun ISqlJetCursor.forEach(block: () -> Unit) {
    if (!eof()) {
        do {
            block()
        } while (next())
    }
}
