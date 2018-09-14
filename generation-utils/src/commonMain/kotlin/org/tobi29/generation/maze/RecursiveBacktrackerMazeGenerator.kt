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

package org.tobi29.generation.maze

import org.tobi29.arrays.BitFieldGrid
import org.tobi29.arrays.getAt
import org.tobi29.arrays.setAt
import org.tobi29.math.Face
import org.tobi29.math.Random
import org.tobi29.math.vector.MutableVector2i
import org.tobi29.utils.Pool

/**
 * Maze generator using recursive backtracking
 */
object RecursiveBacktrackerMazeGenerator : MazeGenerator {
    override fun generate(width: Int,
                          height: Int,
                          startX: Int,
                          startY: Int,
                          random: Random): Maze {
        val maze = BitFieldGrid(width, height)
        val maxX = width - 1
        val maxY = height - 1
        val path = Pool { MutableVector2i() }
        var current: MutableVector2i? = path.push().setXY(startX, startY)
        val directions = Array(4) { Face.NONE }
        while (current != null) {
            val x = current.x
            val y = current.y
            maze.setAt(x, y, 2, true)
            var validDirections = 0
            if (x < maxX && !maze.getAt(x + 1, y, 2)) {
                directions[validDirections++] = Face.EAST
            }
            if (y < maxY && !maze.getAt(x, y + 1, 2)) {
                directions[validDirections++] = Face.SOUTH
            }
            if (x > 0 && !maze.getAt(x - 1, y, 2)) {
                directions[validDirections++] = Face.WEST
            }
            if (y > 0 && !maze.getAt(x, y - 1, 2)) {
                directions[validDirections++] = Face.NORTH
            }
            current = if (validDirections > 0) {
                val direction = directions[random.nextInt(validDirections)]
                if (direction == Face.NORTH) {
                    maze.setAt(x, y, 0, true)
                } else if (direction == Face.EAST) {
                    maze.setAt(x + 1, y, 1, true)
                } else if (direction == Face.SOUTH) {
                    maze.setAt(x, y + 1, 0, true)
                } else if (direction == Face.WEST) {
                    maze.setAt(x, y, 1, true)
                }
                path.push().setXY(x + direction.x, y + direction.y)
            } else {
                path.pop()
            }
        }
        return Maze(maze)
    }
}
