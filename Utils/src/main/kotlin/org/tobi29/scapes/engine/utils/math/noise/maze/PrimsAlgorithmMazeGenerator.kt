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

import mu.KLogging
import org.tobi29.scapes.engine.utils.Pool
import org.tobi29.scapes.engine.utils.math.Face
import org.tobi29.scapes.engine.utils.math.and
import org.tobi29.scapes.engine.utils.math.or
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2i
import java.util.*

class PrimsAlgorithmMazeGenerator(private val width: Int, private val height: Int, private val startX: Int,
                                  private val startY: Int) : MazeGenerator {
    private var data: Array<ByteArray>? = null

    constructor(width: Int, height: Int, random: Random) : this(width, height,
            random.nextInt(width), random.nextInt(height)) {
    }

    override fun generate(random: Random) {
        val time = System.currentTimeMillis()
        val data = Array(width) { ByteArray(height) }
        val maxX = width - 1
        val maxY = height - 1
        var x = startX
        var y = startY
        data[x][startY] = data[x][startY] or (MASK_VISITED or MASK_LISTED).toByte()
        val list = Pool { MutableVector2i() }
        list.push().set(x, y)
        val directions = arrayOfNulls<Face>(4)
        var validDirections = 0
        if (y > 0) {
            data[x][y - 1] = data[x][y - 1] or MASK_VISITED
            directions[validDirections++] = Face.NORTH
            list.push().set(x, y - 1)
            data[x][y - 1] = data[x][y - 1] or MASK_LISTED
        }
        if (x < maxX) {
            data[x + 1][y] = data[x + 1][y] or MASK_LISTED
            directions[validDirections++] = Face.EAST
            list.push().set(x + 1, y)
            data[x + 1][y] = data[x + 1][y] or MASK_LISTED
        }
        if (y < maxY) {
            data[x][y + 1] = data[x][y + 1] or MASK_LISTED
            directions[validDirections++] = Face.SOUTH
            list.push().set(x, y + 1)
            data[x][y + 1] = data[x][y + 1] or MASK_LISTED
        }
        if (x > 0) {
            data[x - 1][y] = data[x - 1][y] or MASK_LISTED
            directions[validDirections++] = Face.WEST
            list.push().set(x - 1, y)
            data[x - 1][y] = data[x - 1][y] or MASK_LISTED
        }
        val current = MutableVector2i(x, y)
        while (true) {
            assert(validDirections > 0)
            val direction = directions[random.nextInt(validDirections)]
            if (direction === Face.NORTH) {
                data[x][y] = data[x][y] or MASK_NORTH
            } else if (direction === Face.EAST) {
                data[x + 1][y] = data[x + 1][y] or MASK_WEST
            } else if (direction === Face.SOUTH) {
                data[x][y + 1] = data[x][y + 1] or MASK_NORTH
            } else if (direction === Face.WEST) {
                data[x][y] = data[x][y] or MASK_WEST
            }
            list.remove(current)
            if (list.isEmpty) {
                break
            }
            val next = list[random.nextInt(list.size())]
            x = next.x
            y = next.y
            current.set(x, y)
            data[x][y] = data[x][y] or MASK_VISITED
            validDirections = 0
            if (y > 0) {
                if (data[x][y - 1] and MASK_VISITED != 0.toByte()) {
                    directions[validDirections++] = Face.NORTH
                }
                if (data[x][y - 1] and MASK_LISTED == 0.toByte()) {
                    list.push().set(x, y - 1)
                    data[x][y - 1] = data[x][y - 1] or MASK_LISTED
                }
            }
            if (x < maxX) {
                if (data[x + 1][y] and MASK_VISITED != 0.toByte()) {
                    directions[validDirections++] = Face.EAST
                }
                if (data[x + 1][y] and MASK_LISTED == 0.toByte()) {
                    list.push().set(x + 1, y)
                    data[x + 1][y] = data[x + 1][y] or MASK_LISTED
                }
            }
            if (y < maxY) {
                if (data[x][y + 1] and MASK_VISITED != 0.toByte()) {
                    directions[validDirections++] = Face.SOUTH
                }
                if (data[x][y + 1] and MASK_LISTED == 0.toByte()) {
                    list.push().set(x, y + 1)
                    data[x][y + 1] = data[x][y + 1] or MASK_LISTED
                }
            }
            if (x > 0) {
                if (data[x - 1][y] and MASK_VISITED != 0.toByte()) {
                    directions[validDirections++] = Face.WEST
                }
                if (data[x - 1][y] and MASK_LISTED == 0.toByte()) {
                    list.push().set(x - 1, y)
                    data[x - 1][y] = data[x - 1][y] or MASK_LISTED
                }
            }
        }
        logger.debug { "Generated prim-maze in ${System.currentTimeMillis() - time}ms." }
        this.data = data
    }

    override fun createMap(roomSizeX: Int,
                           roomSizeY: Int): Array<BooleanArray> {
        val data = data ?: throw IllegalStateException("Maze not generated")
        val cellSizeX = roomSizeX + 1
        val cellSizeY = roomSizeY + 1
        val blocks = Array(width * cellSizeX + 1) {
            BooleanArray(height * cellSizeY + 1)
        }
        for (y in 0..height - 1) {
            val yy = y * cellSizeY
            for (x in 0..width - 1) {
                val xx = x * cellSizeX
                if (data[x][y] and MASK_NORTH == 0.toByte()) {
                    for (wall in 0..cellSizeX) {
                        blocks[xx + wall][yy] = true
                    }
                }
                if (data[x][y] and MASK_WEST == 0.toByte()) {
                    for (wall in 0..cellSizeY) {
                        blocks[xx][yy + wall] = true
                    }
                }
            }
        }
        var i = blocks.size - 1
        for (y in 0..blocks[i].size - 1) {
            blocks[i][y] = true
        }
        i = blocks[0].size - 1
        for (x in blocks.indices) {
            blocks[x][i] = true
        }
        return blocks
    }

    companion object : KLogging() {
        private val MASK_NORTH: Byte = 0x1
        private val MASK_WEST: Byte = 0x2
        private val MASK_VISITED: Byte = 0x4
        private val MASK_LISTED: Byte = 0x8
    }
}