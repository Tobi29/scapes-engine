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

import org.tobi29.arrays.ByteArray2
import org.tobi29.arrays.change
import org.tobi29.math.Face
import org.tobi29.math.Random
import org.tobi29.math.vector.MutableVector2i
import org.tobi29.stdex.assert
import org.tobi29.stdex.maskAt
import org.tobi29.stdex.setAt
import org.tobi29.utils.Pool

/**
 * Maze generator using prim's algorithm
 */
object PrimsAlgorithmMazeGenerator : MazeGenerator {
    override fun generate(
        width: Int, height: Int,
        startX: Int, startY: Int,
        random: Random
    ): Maze {
        val maze = ByteArray2(width, height)
        val maxX = width - 1
        val maxY = height - 1
        val list = Pool { MutableVector2i() }
        maze.change(startX, startY) { it.setAt(2).setAt(3) }
        if (startY > 0) {
            list.push().setXY(startX, startY - 1)
            maze.change(startX, startY - 1) { it.setAt(3) }
        }
        if (startX < maxX) {
            list.push().setXY(startX + 1, startY)
            maze.change(startX + 1, startY) { it.setAt(3) }
        }
        if (startY < maxY) {
            list.push().setXY(startX, startY + 1)
            maze.change(startX, startY + 1) { it.setAt(3) }
        }
        if (startX > 0) {
            list.push().setXY(startX - 1, startY)
            maze.change(startX - 1, startY) { it.setAt(3) }
        }
        val directions = arrayOfNulls<Face>(4)
        while (true) {
            val current = list.removeAt(random.nextInt(list.size))
            val x = current.x
            val y = current.y
            list.give(current)
            maze.change(x, y) { it.setAt(2) }
            var validDirections = 0
            if (y > 0) {
                if (maze[x, y - 1].maskAt(2)) {
                    directions[validDirections++] = Face.NORTH
                }
                if (!maze[x, y - 1].maskAt(3)) {
                    list.push().setXY(x, y - 1)
                    maze.change(x, y - 1) { it.setAt(3) }
                }
            }
            if (x < maxX) {
                if (maze[x + 1, y].maskAt(2)) {
                    directions[validDirections++] = Face.EAST
                }
                if (!maze[x + 1, y].maskAt(3)) {
                    list.push().setXY(x + 1, y)
                    maze.change(x + 1, y) { it.setAt(3) }
                }
            }
            if (y < maxY) {
                if (maze[x, y + 1].maskAt(2)) {
                    directions[validDirections++] = Face.SOUTH
                }
                if (!maze[x, y + 1].maskAt(3)) {
                    list.push().setXY(x, y + 1)
                    maze.change(x, y + 1) { it.setAt(3) }
                }
            }
            if (x > 0) {
                if (maze[x - 1, y].maskAt(2)) {
                    directions[validDirections++] = Face.WEST
                }
                if (!maze[x - 1, y].maskAt(3)) {
                    list.push().setXY(x - 1, y)
                    maze.change(x - 1, y) { it.setAt(3) }
                }
            }
            assert { validDirections > 0 }
            val direction = directions[random.nextInt(validDirections)]
            if (direction == Face.NORTH) {
                maze.change(x, y) { it.setAt(0) }
            } else if (direction == Face.EAST) {
                maze.change(x + 1, y) { it.setAt(1) }
            } else if (direction == Face.SOUTH) {
                maze.change(x, y + 1) { it.setAt(0) }
            } else if (direction == Face.WEST) {
                maze.change(x, y) { it.setAt(1) }
            }
            if (list.isEmpty()) {
                break
            }
        }
        return Maze(maze)
    }
}
