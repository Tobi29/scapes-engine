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

package org.tobi29.platform

import org.tobi29.io.ReadableByteChannel
import org.tobi29.io.WritableByteStream

/**
 * Standard output stream
 *
 * **Note:** For writing text data it is highly recommended
 * to use the [print] family of functions
 */
expect val stdout: WritableByteStream

/**
 * Standard output stream
 *
 * **Note:** For writing text data it is highly recommended
 * to use the [printerr] family of functions
 */
expect val stderr: WritableByteStream

/**
 * Standard input stream
 */
expect val stdin: ReadableByteChannel?

/**
 * System dependant newline string, usually will be `"\n"` or on some weird
 * systems `"\r\n"`
 */
expect val stdln: String
