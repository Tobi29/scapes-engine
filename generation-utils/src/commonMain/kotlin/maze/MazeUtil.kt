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

import org.tobi29.math.Face

inline fun Maze.drawMazeWalls(
    roomSizeX: Int,
    roomSizeY: Int,
    lineH: (Int, Int, Int) -> Unit,
    lineV: (Int, Int, Int) -> Unit
) {
    val cellSizeX = roomSizeX + 1
    val cellSizeY = roomSizeY + 1
    val w = width * roomSizeX + 1
    val h = height * roomSizeY + 1
    for (y in 0 until height) {
        val yy = y * roomSizeY
        for (x in 0 until width) {
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
    for (x in 0 until lw step roomSizeX) {
        lineH(x, lh, cellSizeX)
    }
    for (y in 0 until lh step roomSizeY) {
        lineV(lw, y, cellSizeY)
    }
}

inline fun Maze.drawMazeWalls(
    roomSizeX: Int,
    roomSizeY: Int,
    pixel: (Int, Int) -> Unit
) {
    drawMazeWalls(roomSizeX, roomSizeY, { x, y, l ->
        for (xx in x until x + l) {
            pixel(xx, y)
        }
    }, { x, y, l ->
        for (yy in y until y + l) {
            pixel(x, yy)
        }
    })
}
