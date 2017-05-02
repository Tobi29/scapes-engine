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

package org.tobi29.scapes.engine.utils.generation.maze.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.math.Random
import org.tobi29.scapes.engine.utils.generation.maze.PrimsAlgorithmMazeGenerator

object PrimsAlgorithmMazeTests : Spek({
    describe("generating a maze") {
        on("generating a maze") {
            val maze = PrimsAlgorithmMazeGenerator.generate(64, 32, Random(0))
            it("should have the correct width") {
                maze.width shouldEqual 64
            }
            it("should have the correct height") {
                maze.height shouldEqual 32
            }
        }
    }
})
