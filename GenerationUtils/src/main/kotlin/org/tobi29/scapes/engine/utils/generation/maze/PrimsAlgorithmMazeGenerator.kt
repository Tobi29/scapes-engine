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

import org.tobi29.scapes.engine.utils.*
import org.tobi29.scapes.engine.utils.math.Face
import org.tobi29.scapes.engine.utils.math.Random
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2i

/**
 * Maze generator using prim's algorithm
 */
object PrimsAlgorithmMazeGenerator : MazeGenerator {
    override fun generate(width: Int,
                          height: Int,
                          startX: Int,
                          startY: Int,
                          random: Random): Maze {
        val maze = BitFieldGrid(width, height)
        val maxX = width - 1
        val maxY = height - 1
        val list = Pool { MutableVector2i() }
        maze.setAt(startX, startY, 2, true)
        maze.setAt(startX, startY, 3, true)
        if (startY > 0) {
            list.push().set(startX, startY - 1)
            maze.setAt(startX, startY - 1, 3, true)
        }
        if (startX < maxX) {
            list.push().set(startX + 1, startY)
            maze.setAt(startX + 1, startY, 3, true)
        }
        if (startY < maxY) {
            list.push().set(startX, startY + 1)
            maze.setAt(startX, startY + 1, 3, true)
        }
        if (startX > 0) {
            list.push().set(startX - 1, startY)
            maze.setAt(startX - 1, startY, 3, true)
        }
        val directions = arrayOfNulls<Face>(4)
        while (true) {
            val current = list.removeAt(random.nextInt(list.size))
            val x = current.x
            val y = current.y
            list.give(current)
            maze.setAt(x, y, 2, true)
            var validDirections = 0
            if (y > 0) {
                if (maze.getAt(x, y - 1, 2)) {
                    directions[validDirections++] = Face.NORTH
                }
                if (!maze.getAt(x, y - 1, 3)) {
                    list.push().set(x, y - 1)
                    maze.setAt(x, y - 1, 3, true)
                }
            }
            if (x < maxX) {
                if (maze.getAt(x + 1, y, 2)) {
                    directions[validDirections++] = Face.EAST
                }
                if (!maze.getAt(x + 1, y, 3)) {
                    list.push().set(x + 1, y)
                    maze.setAt(x + 1, y, 3, true)
                }
            }
            if (y < maxY) {
                if (maze.getAt(x, y + 1, 2)) {
                    directions[validDirections++] = Face.SOUTH
                }
                if (!maze.getAt(x, y + 1, 3)) {
                    list.push().set(x, y + 1)
                    maze.setAt(x, y + 1, 3, true)
                }
            }
            if (x > 0) {
                if (maze.getAt(x - 1, y, 2)) {
                    directions[validDirections++] = Face.WEST
                }
                if (!maze.getAt(x - 1, y, 3)) {
                    list.push().set(x - 1, y)
                    maze.setAt(x - 1, y, 3, true)
                }
            }
            assert { validDirections > 0 }
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
            if (list.isEmpty()) {
                break
            }
        }
        return Maze(maze)
    }
}
