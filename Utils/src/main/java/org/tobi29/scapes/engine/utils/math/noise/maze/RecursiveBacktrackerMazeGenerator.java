/*
 * Copyright 2012-2015 Tobi29
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

package org.tobi29.scapes.engine.utils.math.noise.maze;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.utils.Pool;
import org.tobi29.scapes.engine.utils.math.Face;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2i;

import java.util.Random;

public class RecursiveBacktrackerMazeGenerator implements MazeGenerator {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(RecursiveBacktrackerMazeGenerator.class);
    private static final byte MASK_NORTH = 0x1, MASK_WEST = 0x2, MASK_VISITED =
            0x4;
    private final int width, height, startX, startY;
    private byte[][] data;

    public RecursiveBacktrackerMazeGenerator(int width, int height,
            Random random) {
        this(width, height, random.nextInt(width), random.nextInt(height));
    }

    public RecursiveBacktrackerMazeGenerator(int width, int height, int startX,
            int startY) {
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    @Override
    public void generate(Random random) {
        long time = System.currentTimeMillis();
        data = new byte[width][height];
        int maxX = width - 1;
        int maxY = height - 1;
        Pool<MutableVector2> path = new Pool<>(MutableVector2i::new);
        MutableVector2 current = path.push().set(startX, startY);
        Face[] directions = new Face[4];
        while (current != null) {
            int x = current.intX();
            int y = current.intY();
            data[x][y] |= MASK_VISITED;
            int validDirections = 0;
            if (x < maxX) {
                if ((data[x + 1][y] & MASK_VISITED) == 0) {
                    directions[validDirections++] = Face.EAST;
                }
            }
            if (y < maxY) {
                if ((data[x][y + 1] & MASK_VISITED) == 0) {
                    directions[validDirections++] = Face.SOUTH;
                }
            }
            if (x > 0) {
                if ((data[x - 1][y] & MASK_VISITED) == 0) {
                    directions[validDirections++] = Face.WEST;
                }
            }
            if (y > 0) {
                if ((data[x][y - 1] & MASK_VISITED) == 0) {
                    directions[validDirections++] = Face.NORTH;
                }
            }
            if (validDirections > 0) {
                Face direction = directions[random.nextInt(validDirections)];
                if (direction == Face.NORTH) {
                    data[x][y] |= MASK_NORTH;
                } else if (direction == Face.EAST) {
                    data[x + 1][y] |= MASK_WEST;
                } else if (direction == Face.SOUTH) {
                    data[x][y + 1] |= MASK_NORTH;
                } else if (direction == Face.WEST) {
                    data[x][y] |= MASK_WEST;
                }
                current = path.push()
                        .set(x + direction.getX(), y + direction.getY());
            } else {
                if (!path.isEmpty()) {
                    current = path.pop();
                } else {
                    current = null;
                }
            }
        }
        LOGGER.debug("Generated recursive-backtracker-maze in {} ms.",
                System.currentTimeMillis() - time);
    }

    @Override
    public boolean[][] createMap(int roomSizeX, int roomSizeY) {
        int cellSizeX = roomSizeX + 1;
        int cellSizeY = roomSizeY + 1;
        boolean[][] blocks =
                new boolean[width * cellSizeX + 1][height * cellSizeY + 1];
        for (int y = 0; y < height; y++) {
            int yy = y * cellSizeY;
            for (int x = 0; x < width; x++) {
                int xx = x * cellSizeX;
                if ((data[x][y] & MASK_NORTH) == 0) {
                    for (int wall = 0; wall <= cellSizeX; wall++) {
                        blocks[xx + wall][yy] = true;
                    }
                }
                if ((data[x][y] & MASK_WEST) == 0) {
                    for (int wall = 0; wall <= cellSizeY; wall++) {
                        blocks[xx][yy + wall] = true;
                    }
                }
            }
        }
        int i = blocks.length - 1;
        for (int y = 0; y < blocks[i].length; y++) {
            blocks[i][y] = true;
        }
        i = blocks[0].length - 1;
        for (int x = 0; x < blocks.length; x++) {
            blocks[x][i] = true;
        }
        return blocks;
    }
}
