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

import java.util.*

interface SQLDatabase {
    fun replace(table: String,
                columns: Array<out String>,
                rows: List<Array<out Any?>>)

    fun replace(table: String,
                columns: Array<out String>,
                vararg rows: Array<out Any?>) {
        replace(table, columns, listOf(*rows))
    }

    fun update(table: String,
               matches: List<Pair<String, Any?>>,
               updates: List<Pair<String, Any?>>) {
        val matchesList = ArrayList<String>(matches.size)
        val values = ArrayList<Any?>(matches.size)
        matches.forEach {
            matchesList.add(it.first)
            values.add(it.second)
        }
        val columns = ArrayList<String>(updates.size)
        val updateValues = ArrayList<Any?>(updates.size)
        updates.forEach {
            columns.add(it.first)
            updateValues.add(it.second)
        }
        compileQuery(table, columns.toTypedArray(), matchesList).run(values,
                updateValues)
    }

    fun update(table: String,
               matches: Array<Pair<String, Any?>>,
               vararg updates: Pair<String, Any?>) {
        update(table, listOf(*matches), listOf(*updates))
    }

    fun insert(table: String,
               columns: Array<out String>,
               rows: List<Array<out Any?>>)

    fun insert(table: String,
               columns: Array<out String>,
               vararg rows: Array<out Any?>) {
        insert(table, columns, listOf(*rows))
    }

    fun query(table: String,
              columns: Array<out String>,
              matches: List<Pair<String, Any?>>): List<Array<Any?>> {
        val matchesList = ArrayList<String>(matches.size)
        val values = ArrayList<Any?>(matches.size)
        matches.forEach {
            matchesList.add(it.first)
            values.add(it.second)
        }
        return compileQuery(table, columns, matchesList).run(values)
    }

    fun query(table: String,
              columns: Array<out String>,
              vararg matches: Pair<String, Any?>): List<Array<Any?>> {
        return query(table, columns, listOf(*matches))
    }

    fun delete(table: String,
               matches: List<Pair<String, Any?>>) {
        val matchesList = ArrayList<String>(matches.size)
        val values = ArrayList<Any?>(matches.size)
        matches.forEach {
            matchesList.add(it.first)
            values.add(it.second)
        }
        compileDelete(table, matchesList).run(values)
    }

    fun delete(table: String,
               vararg matches: Pair<String, Any?>) {
        delete(table, listOf(*matches))
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
        return compileQuery(table, columns, listOf(*matches))
    }

    fun compileUpdate(table: String,
                      matches: List<String>,
                      columns: Array<out String>): SQLUpdate

    fun compileUpdate(table: String,
                      matches: Array<out String>,
                      vararg columns: String): SQLUpdate {
        return compileUpdate(table, listOf(*matches), columns)
    }

    fun compileDelete(table: String,
                      matches: List<String>): SQLDelete

    fun compileDelete(table: String,
                      vararg matches: String): SQLDelete {
        return compileDelete(table, listOf(*matches))
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
        return run(listOf(*values))
    }
}

interface SQLUpdate {
    fun run(values: List<Any?>,
            updates: List<Any?>)

    fun run(values: Array<out Any?>,
            vararg updates: Any?) {
        return run(listOf(*values), listOf(*updates))
    }
}

interface SQLDelete {
    fun run(values: List<Any?>)

    fun run(vararg values: Any?) {
        return run(listOf(*values))
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
