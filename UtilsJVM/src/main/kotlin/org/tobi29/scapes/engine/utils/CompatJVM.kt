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

@file:Suppress("NOTHING_TO_INLINE")

package org.tobi29.scapes.engine.utils

impl val ANDROID_API_LEVEL = run {
    try {
        Class.forName("android.os.Build.VERSION")
                .getField("SDK_INT").get(null) as? Int
    } catch (e: ReflectiveOperationException) {
        null
    }
}

impl inline val IS_JVM get() = true
impl inline val IS_ANDROID get() = ANDROID_API_LEVEL != null
impl inline val IS_JS get() = false
impl inline val IS_NATIVE get() = false

impl inline fun isAndroidAPI(level: Int) =
        ANDROID_API_LEVEL ?: Int.MAX_VALUE >= level