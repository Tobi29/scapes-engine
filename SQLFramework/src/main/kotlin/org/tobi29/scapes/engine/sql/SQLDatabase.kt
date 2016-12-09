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

package org.tobi29.scapes.engine.sql

interface SQLDatabase {
    fun replace(table: String,
                columns: Array<out String>,
                rows: List<Array<out Any?>>)

    fun replace(table: String,
                columns: Array<out String>,
                vararg rows: Array<out Any?>) {
        replace(table, columns, listOf(*rows))
    }

    fun createTable(name: String,
                    primaryKey: List<String>,
                    columns: List<SQLColumn>)

    fun createTable(name: String,
                    primaryKey: Array<String>,
                    vararg columns: SQLColumn) {
        createTable(name, listOf(*primaryKey), listOf(*columns))
    }

    fun dropTable(name: String)

    fun compileQuery(table: String,
                     columns: Array<out String>,
                     matches: List<String>): SQLQuery

    fun compileQuery(table: String,
                     columns: Array<out String>,
                     vararg matches: String): SQLQuery {
        return compileQuery(table, columns, matches.toList())
    }

    fun compileInsert(table: String,
                      vararg columns: String): SQLInsert

    fun compileUpdate(table: String,
                      matches: List<String>,
                      columns: Array<out String>): SQLUpdate

    fun compileUpdate(table: String,
                      matches: Array<out String>,
                      vararg columns: String): SQLUpdate {
        return compileUpdate(table, matches.toList(), columns)
    }

    fun compileDelete(table: String,
                      matches: List<String>): SQLDelete

    fun compileDelete(table: String,
                      vararg matches: String): SQLDelete {
        return compileDelete(table, matches.toList())
    }
}

class SQLColumn(val name: String,
                val type: SQLType,
                val extra: String? = null,
                val foreignKey: SQLForeignKey? = null,
                val notNull: Boolean = false,
                val unique: Boolean = false)

class SQLForeignKey(val table: String,
                    val column: String,
                    val onUpdate: SQLReferentialAction = SQLReferentialAction.RESTRICT,
                    val onDelete: SQLReferentialAction = onUpdate)

interface SQLQuery {
    fun run(values: List<Any?>): List<Array<Any?>>

    fun run(vararg values: Any?): List<Array<Any?>> {
        return run(values.toList())
    }
}

interface SQLInsert {
    fun run(values: List<Array<out Any?>>)

    fun run(vararg values: Array<out Any?>) {
        return run(listOf(*values))
    }
}

interface SQLUpdate {
    fun run(values: List<Any?>,
            updates: List<Any?>)

    fun run(values: Array<out Any?>,
            vararg updates: Any?) {
        return run(values.toList(), updates.toList())
    }
}

interface SQLDelete {
    fun run(values: List<Any?>)

    fun run(vararg values: Any?) {
        return run(values.toList())
    }
}

enum class SQLType {
    INT,
    BIGINT,
    LONGBLOB,
    CHAR,
    VARCHAR
}

enum class SQLReferentialAction(val sql: String) {
    RESTRICT("RESTRICT"),
    CASCADE("CASCADE"),
    SET_NULL("SET NULL")
}
