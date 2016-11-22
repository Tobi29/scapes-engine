/*
 * Copyright 2012-2016 Tobi29
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
package org.tobi29.scapes.engine.utils.math.noise.maze

import org.tobi29.scapes.engine.utils.Pool
import org.tobi29.scapes.engine.utils.math.Face
import org.tobi29.scapes.engine.utils.math.and
import org.tobi29.scapes.engine.utils.math.or
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2i
import java.util.*

/**
 * Maze generator using prim's algorithm
 */
object PrimsAlgorithmMazeGenerator : MazeGenerator {
    override fun generate(width: Int,
                          height: Int,
                          startX: Int,
                          startY: Int,
                          random: Random): Maze {
        val maze = MutableMaze(width, height)
        val maxX = width - 1
        val maxY = height - 1
        val list = Pool { MutableVector2i() }
        maze.changeAt(startX, startY) { it or MASK_LISTED or MASK_VISITED }
        if (startY > 0) {
            list.push().set(startX, startY - 1)
            maze.changeAt(startX, startY - 1) { it or MASK_LISTED }
        }
        if (startX < maxX) {
            list.push().set(startX + 1, startY)
            maze.changeAt(startX + 1, startY) { it or MASK_LISTED }
        }
        if (startY < maxY) {
            list.push().set(startX, startY + 1)
            maze.changeAt(startX, startY + 1) { it or MASK_LISTED }
        }
        if (startX > 0) {
            list.push().set(startX - 1, startY)
            maze.changeAt(startX - 1, startY) { it or MASK_LISTED }
        }
        val directions = arrayOfNulls<Face>(4)
        while (true) {
            val current = list.removeAt(random.nextInt(list.size()))
            val x = current.x
            val y = current.y
            list.give(current)
            maze.changeAt(x, y) { it or MASK_VISITED }
            var validDirections = 0
            if (y > 0) {
                if (maze.getAt(x, y - 1) and MASK_VISITED != 0.toByte()) {
                    directions[validDirections++] = Face.NORTH
                }
                if (maze.getAt(x, y - 1) and MASK_LISTED == 0.toByte()) {
                    list.push().set(x, y - 1)
                    maze.changeAt(x, y - 1) { it or MASK_LISTED }
                }
            }
            if (x < maxX) {
                if (maze.getAt(x + 1, y) and MASK_VISITED != 0.toByte()) {
                    directions[validDirections++] = Face.EAST
                }
                if (maze.getAt(x + 1, y) and MASK_LISTED == 0.toByte()) {
                    list.push().set(x + 1, y)
                    maze.changeAt(x + 1, y) { it or MASK_LISTED }
                }
            }
            if (y < maxY) {
                if (maze.getAt(x, y + 1) and MASK_VISITED != 0.toByte()) {
                    directions[validDirections++] = Face.SOUTH
                }
                if (maze.getAt(x, y + 1) and MASK_LISTED == 0.toByte()) {
                    list.push().set(x, y + 1)
                    maze.changeAt(x, y + 1) { it or MASK_LISTED }
                }
            }
            if (x > 0) {
                if (maze.getAt(x - 1, y) and MASK_VISITED != 0.toByte()) {
                    directions[validDirections++] = Face.WEST
                }
                if (maze.getAt(x - 1, y) and MASK_LISTED == 0.toByte()) {
                    list.push().set(x - 1, y)
                    maze.changeAt(x - 1, y) { it or MASK_LISTED }
                }
            }
            assert(validDirections > 0)
            val direction = directions[random.nextInt(validDirections)]
            if (direction === Face.NORTH) {
                maze.changeAt(x, y) { it or Maze.MASK_NORTH }
            } else if (direction === Face.EAST) {
                maze.changeAt(x + 1, y) { it or Maze.MASK_WEST }
            } else if (direction === Face.SOUTH) {
                maze.changeAt(x, y + 1) { it or Maze.MASK_NORTH }
            } else if (direction === Face.WEST) {
                maze.changeAt(x, y) { it or Maze.MASK_WEST }
            }
            if (list.isEmpty) {
                break
            }
        }
        return maze.toMaze()
    }

    private val MASK_VISITED: Byte = 0x4
    private val MASK_LISTED: Byte = 0x8
}
