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

package org.tobi29.scapes.engine.utils.io

/**
 * Detect the mime type of the given resource
 *
 * **Note:** The exact result may vary from platform to platform
 * @param name The name of the resource or `null`
 * @returns a mime-type string
 */
fun detectMime(name: String? = null) =
        detectMime(null, name)

/**
 * Detect the mime type of the given resource
 *
 * **Note:** The exact result may vary from platform to platform
 * @param stream The stream to read from
 * @param name The name of the resource or `null`
 * @returns a mime-type string
 */
fun detectMime(stream: ReadableByteStream? = null,
               name: String? = null) =
        detectMimeImpl(stream, name)
