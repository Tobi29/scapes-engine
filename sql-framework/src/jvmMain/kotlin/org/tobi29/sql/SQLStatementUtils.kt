/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.sql

fun sqlWhere(matches: Array<out SQLMatch>,
             sql: StringBuilder) {
    var first = true
    for (match in matches) {
        if (first) {
            first = false
        } else {
            sql.append(" AND ")
        }
        sql.append(match.name).append(match.operator.sql).append('?')
    }
}

fun sqlType(type: SQLType,
            extra: String?): String {
    val typeStr = type.toString()
    if (extra != null) {
        return "$typeStr($extra)"
    }
    return typeStr
}
