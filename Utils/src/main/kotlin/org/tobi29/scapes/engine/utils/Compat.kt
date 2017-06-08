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

/*
/**
 * Android API Level that the current application is running on or `null`
 * if not running on Android
 */
header val ANDROID_API_LEVEL: Int?

/**
 * `true` if the current application is running on Kotlin/JVM or Android
 */
header val IS_JVM: Boolean

/**
 * `true` if the current application is running on Android
 * @see [ANDROID_API_LEVEL]
 */
header val IS_ANDROID: Boolean

/**
 * `true` if the current application is running on Kotlin/JS
 */
header val IS_JS: Boolean

/**
 * `true` if the current application is running on Kotlin/Native
 */
header val IS_NATIVE: Boolean

/**
 * Checks if the current application is running on at least the specified
 * Android API level or on any other JVM
 *
 * Returns `false` on non Kotlin/JVM platforms
 * @param level The minimum API Level
 * @return `true` if specified API Level is available or running on other JVM
 */
header fun isAndroidAPI(level: Int): Boolean
*/
