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

package org.tobi29.scapes.engine.utils

actual internal fun ByteArray.utf8ToStringImpl(offset: Int,
                                             size: Int) =
        String(this, offset, size)

actual internal fun String.utf8ToArrayImpl(destination: ByteArray?,
                                         offset: Int,
                                         size: Int) =
        toByteArray().let {
            if (destination == null && offset == 0
                    && (size < 0 || size == it.size)) it
            else {
                val array = destination ?: ByteArray(offset +
                        if (size < 0) it.size else size)
                copy(it, array, size.coerceAtMost(it.size))
                array
            }
        }

actual internal fun CharArray.copyToStringImpl(offset: Int,
                                             size: Int) =
        String(this, offset, size)

actual internal fun String.copyToArrayImpl(destination: CharArray,
                                         offset: Int,
                                         startIndex: Int,
                                         endIndex: Int) =
        toCharArray(destination, offset, startIndex, endIndex)
