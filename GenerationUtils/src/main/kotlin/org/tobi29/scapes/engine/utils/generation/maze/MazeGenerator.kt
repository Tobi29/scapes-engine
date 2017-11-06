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

package org.tobi29.scapes.engine.utils.generation.maze

import org.tobi29.scapes.engine.math.Random

/**
 * Generates random mazes
 */
interface MazeGenerator {
    /**
     * Generates a new maze
     * @param width width of the maze
     * @param height height of the maze
     * @param random rng instance
     */
    fun generate(width: Int,
                 height: Int,
                 random: Random): Maze {
        return generate(width, height, random.nextInt(width),
                random.nextInt(height), random)
    }

    /**
     * Generates a new maze
     * @param width width of the maze
     * @param height height of the maze
     * @param startX starting x-position in the maze algorithm
     * @param startY starting y-position in the maze algorithm
     * @param random rng instance
     */
    fun generate(width: Int,
                 height: Int,
                 startX: Int,
                 startY: Int,
                 random: Random): Maze
}
