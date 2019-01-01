/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.utils

/**
 * Interface for objects with an identifier (such as applications)
 */
interface Identified {
    /**
     * The identifier for this object
     * @note Avoid spaces or characters not valid in filesystem paths
     */
    val id: String
}

/**
 * Interface for objects with a name (such as applications)
 */
interface Named {
    /**
     * The name for this object
     * @note Avoid spaces or characters not valid in filesystem paths
     */
    val name: String

    /**
     * The executable name for this object
     * @note Avoid characters not valid in filesystem paths
     */
    val executableName: String get() = name

    /**
     * The full name (primarily for display to humans) for this object
     */
    val fullName: String get() = name
}

/**
 * Interface for objects with a version (such as applications)
 */
interface Versioned {
    /**
     * The version for this object
     */
    val version: Version
}
