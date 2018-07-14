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

package org.tobi29.tilemaps.tiled

import kotlinx.coroutines.experimental.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.io.IOException
import org.tobi29.io.classpath.ClasspathPath
import kotlin.test.assertFalse

object TMXParserTest : Spek({
    describe("parsing a tmx file") {
        // Just a basic smoke test
        val classLoader = this::class.java.classLoader
        val path = ClasspathPath(classLoader, "csvmap.tmx")
        val map = runBlocking {
            path.readAsync {
                it.readTMXMap(
                    path.parent ?: throw IOException("No parent")
                )
            }
        }
        it("should have read layers") {
            assertFalse(map.layers.isEmpty())
        }
    }
})
