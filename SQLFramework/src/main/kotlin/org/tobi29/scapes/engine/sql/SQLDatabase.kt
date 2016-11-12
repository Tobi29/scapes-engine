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
                columns: Array<String>,
                rows: List<Array<Any>>)

    fun replace(table: String,
                columns: Array<String>,
                vararg rows: Array<Any>) {
        replace(table, columns, listOf(*rows))
    }

    fun insert(table: String,
               columns: Array<String>,
               rows: List<Array<Any>>)

    fun insert(table: String,
               columns: Array<String>,
               vararg rows: Array<Any>) {
        insert(table, columns, listOf(*rows))
    }

    fun query(table: String,
              columns: Array<String>,
              matches: List<Pair<String, Any>>): List<Array<Any?>> {
        val matchesList = ArrayList<String>(matches.size)
        val values = ArrayList<Any>(matches.size)
        matches.forEach {
            matchesList.add(it.first)
            values.add(it.second)
        }
        return compileQuery(table, columns, matchesList).run(values)
    }

    fun query(table: String,
              columns: Array<String>,
              vararg matches: Pair<String, Any>): List<Array<Any?>> {
        return query(table, columns, listOf(*matches))
    }

    fun compileQuery(table: String,
                     columns: Array<String>,
                     matches: List<String>): SQLQuery

    fun compileQuery(table: String,
                     columns: Array<String>,
                     vararg matches: String): SQLQuery {
        return compileQuery(table, columns, listOf(*matches))
    }

    fun delete(table: String,
               matches: List<Pair<String, Any>>)

    fun delete(table: String,
               vararg matches: Pair<String, Any>) {
        delete(table, listOf(*matches))
    }

    fun createTable(name: String,
                    primaryKey: String?,
                    columns: List<SQLColumn>)

    fun createTable(name: String,
                    primaryKey: String? = null,
                    vararg columns: SQLColumn) {
        createTable(name, primaryKey, listOf(*columns))
    }

    fun createTable(name: String,
                    vararg columns: SQLColumn) {
        createTable(name, null, *columns)
    }

    fun dropTable(name: String)
}

class SQLColumn(val name: String, val type: SQLType, val extra: String? = null)

interface SQLQuery {
    fun run(values: List<Any>): List<Array<Any?>>

    fun run(vararg values: Any): List<Array<Any?>> {
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
