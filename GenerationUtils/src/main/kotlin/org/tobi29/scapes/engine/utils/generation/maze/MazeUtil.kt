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

import org.tobi29.scapes.engine.utils.BitFieldGrid
import org.tobi29.scapes.engine.utils.Pool
import org.tobi29.scapes.engine.utils.getAt
import org.tobi29.scapes.engine.utils.math.Face
import org.tobi29.scapes.engine.utils.math.vector.MutableVector3i
import org.tobi29.scapes.engine.utils.math.vector.Vector2i
import org.tobi29.scapes.engine.utils.setAt
import kotlin.experimental.and

inline fun Maze.drawMazeWalls(roomSizeX: Int,
                              roomSizeY: Int,
                              lineH: (Int, Int, Int) -> Unit,
                              lineV: (Int, Int, Int) -> Unit) {
    val cellSizeX = roomSizeX + 1
    val cellSizeY = roomSizeY + 1
    val w = width * roomSizeX + 1
    val h = height * roomSizeY + 1
    for (y in 0..height - 1) {
        val yy = y * roomSizeY
        for (x in 0..width - 1) {
            val xx = x * roomSizeX
            if (isWall(x, y, Face.NORTH)) {
                lineH(xx, yy, cellSizeX)
            }
            if (isWall(x, y, Face.WEST)) {
                lineV(xx, yy, cellSizeY)
            }
        }
    }
    val lw = w - 1
    val lh = h - 1
    for (x in 0..lw - 1 step roomSizeX) {
        lineH(x, lh, cellSizeX)
    }
    for (y in 0..lh - 1 step roomSizeY) {
        lineV(lw, y, cellSizeY)
    }
}

inline fun Maze.drawMazeWalls(roomSizeX: Int,
                              roomSizeY: Int,
                              pixel: (Int, Int) -> Unit) {
    drawMazeWalls(roomSizeX, roomSizeY, { x, y, l ->
        for (xx in x..x + l) {
            pixel(xx, y)
        }
    }, { x, y, l ->
        for (yy in y..y + l) {
            pixel(x, yy)
        }
    })
}

fun Maze.findPath(from: Vector2i,
                  to: Vector2i): Array<Vector2i>? {
    return edit().findPath(from, to)
}

private fun BitFieldGrid.findPath(from: Vector2i,
                                  to: Vector2i): Array<Vector2i>? {
    val path = Pool { MutableVector3i() }
    var current: MutableVector3i? = path.push().set(from.x, from.y, 0)
    val w = width
    val h = height
    val lw = w - 1
    val lh = h - 1
    val tx = to.x
    val ty = to.y
    while (current != null) {
        val x = current.x
        val y = current.y
        if (x == tx && y == ty) {
            return Array(path.size) { path[it].now() }
        }
        val dir = current.z
        if (dir < 4) {
            current.z++
            when (dir) {
                0 -> {
                    setAt(x, y, 2, true)
                    if (y > 0 && getAt(x, y, 0) && getAt(x, y - 1, 2)) {
                        current = path.push().set(x, y - 1, 0)
                    }
                }
                1 -> if (x < lw && getAt(x + 1, y) and 0x6 == 0x6.toByte()) {
                    current = path.push().set(x + 1, y, 0)
                }
                2 -> if (y < lh && getAt(x, y + 1) and 0x5 == 0x5.toByte()) {
                    current = path.push().set(x, y + 1, 0)
                }
                3 -> if (x > 0 && getAt(x, y, 1) && getAt(x - 1, y, 2)) {
                    current = path.push().set(x - 1, y, 0)
                }
            }
        } else {
            current = path.pop()
        }
    }
    return null
}
