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
    fun createTable(name: String,
                    primaryKey: Array<out String>,
                    vararg columns: SQLColumn)

    fun dropTable(name: String)

    fun compileQuery(table: String,
                     columns: Array<out String>,
                     vararg matches: String): SQLQuery {
        return compileQuery(table, columns,
                *Array(matches.size) { SQLMatch(matches[it]) })
    }

    fun compileQuery(table: String,
                     columns: Array<out String>,
                     vararg matches: SQLMatch): SQLQuery

    fun compileInsert(table: String,
                      vararg columns: String): SQLInsert

    fun compileUpdate(table: String,
                      matches: Array<out String>,
                      vararg columns: String): SQLUpdate {
        return compileUpdate(table,
                Array(matches.size) { SQLMatch(matches[it]) }, *columns)
    }

    fun compileUpdate(table: String,
                      matches: Array<out SQLMatch>,
                      vararg columns: String): SQLUpdate

    fun compileReplace(table: String,
                       vararg columns: String): SQLReplace

    fun compileDelete(table: String,
                      vararg matches: String): SQLDelete {
        return compileDelete(table,
                *Array(matches.size) { SQLMatch(matches[it]) })
    }

    fun compileDelete(table: String,
                      vararg matches: SQLMatch): SQLDelete
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

class SQLMatch(val name: String,
               val operator: SQLMatchOperator = SQLMatchOperator.EQUALS)

interface SQLQuery {
    operator fun invoke(vararg values: Any?): List<Array<Any?>>
}

interface SQLInsert {
    operator fun invoke(vararg values: Array<out Any?>)
}

interface SQLUpdate {
    operator fun invoke(values: Array<out Any?>,
                        vararg updates: Any?)
}

interface SQLReplace {
    operator fun invoke(vararg values: Array<out Any?>)
}

interface SQLDelete {
    operator fun invoke(vararg values: Any?)
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

enum class SQLMatchOperator(val sql: String) {
    EQUALS("="),
    NOT("<>"),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_EQUAL("<="),
    BETWEEN(" BETWEEN "),
    LIKE(" LIKE "),
    IN(" IN ")
}
