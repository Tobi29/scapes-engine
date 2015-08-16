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

public class PrimsAlgorithmMazeGenerator implements MazeGenerator {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PrimsAlgorithmMazeGenerator.class);
    private static final byte MASK_NORTH = 0x1, MASK_WEST = 0x2, MASK_VISITED =
            0x4, MASK_LISTED = 0x8;
    private final int width, height, startX, startY;
    private byte[][] data;

    public PrimsAlgorithmMazeGenerator(int width, int height, Random random) {
        this(width, height, random.nextInt(width), random.nextInt(height));
    }

    public PrimsAlgorithmMazeGenerator(int width, int height, int startX,
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
        int x = startX;
        int y = startY;
        data[x][startY] |= MASK_VISITED | MASK_LISTED;
        Pool<MutableVector2> list = new Pool<>(MutableVector2i::new);
        list.push().set(x, y);
        Face[] directions = new Face[4];
        int validDirections = 0;
        if (y > 0) {
            data[x][y - 1] |= MASK_VISITED;
            directions[validDirections++] = Face.NORTH;
            list.push().set(x, y - 1);
            data[x][y - 1] |= MASK_LISTED;
        }
        if (x < maxX) {
            data[x + 1][y] |= MASK_LISTED;
            directions[validDirections++] = Face.EAST;
            list.push().set(x + 1, y);
            data[x + 1][y] |= MASK_LISTED;
        }
        if (y < maxY) {
            data[x][y + 1] |= MASK_LISTED;
            directions[validDirections++] = Face.SOUTH;
            list.push().set(x, y + 1);
            data[x][y + 1] |= MASK_LISTED;
        }
        if (x > 0) {
            data[x - 1][y] |= MASK_LISTED;
            directions[validDirections++] = Face.WEST;
            list.push().set(x - 1, y);
            data[x - 1][y] |= MASK_LISTED;
        }
        MutableVector2 current = new MutableVector2i(x, y);
        while (true) {
            assert validDirections > 0;
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
            list.remove(current);
            if (list.isEmpty()) {
                break;
            }
            MutableVector2 next = list.get(random.nextInt(list.size()));
            x = next.intX();
            y = next.intY();
            current.set(x, y);
            data[x][y] |= MASK_VISITED;
            validDirections = 0;
            if (y > 0) {
                if ((data[x][y - 1] & MASK_VISITED) != 0) {
                    directions[validDirections++] = Face.NORTH;
                }
                if ((data[x][y - 1] & MASK_LISTED) == 0) {
                    list.push().set(x, y - 1);
                    data[x][y - 1] |= MASK_LISTED;
                }
            }
            if (x < maxX) {
                if ((data[x + 1][y] & MASK_VISITED) != 0) {
                    directions[validDirections++] = Face.EAST;
                }
                if ((data[x + 1][y] & MASK_LISTED) == 0) {
                    list.push().set(x + 1, y);
                    data[x + 1][y] |= MASK_LISTED;
                }
            }
            if (y < maxY) {
                if ((data[x][y + 1] & MASK_VISITED) != 0) {
                    directions[validDirections++] = Face.SOUTH;
                }
                if ((data[x][y + 1] & MASK_LISTED) == 0) {
                    list.push().set(x, y + 1);
                    data[x][y + 1] |= MASK_LISTED;
                }
            }
            if (x > 0) {
                if ((data[x - 1][y] & MASK_VISITED) != 0) {
                    directions[validDirections++] = Face.WEST;
                }
                if ((data[x - 1][y] & MASK_LISTED) == 0) {
                    list.push().set(x - 1, y);
                    data[x - 1][y] |= MASK_LISTED;
                }
            }
        }
        LOGGER.debug("Generated prim-maze in {} ms.",
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
