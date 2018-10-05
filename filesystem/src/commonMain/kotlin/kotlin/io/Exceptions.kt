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

package kotlin.io

import org.tobi29.io.IOException

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect open class FileSystemException : IOException {
    constructor(deny: Nothing)
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect class FileAlreadyExistsException : FileSystemException {
    constructor(deny: Nothing)
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect class AccessDeniedException : FileSystemException {
    constructor(deny: Nothing)
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect class NoSuchFileException : FileSystemException {
    constructor(deny: Nothing)
}