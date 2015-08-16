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

package org.tobi29.scapes.engine.utils.math;

import org.tobi29.scapes.engine.utils.math.vector.Vector3;
import org.tobi29.scapes.engine.utils.math.vector.Vector3d;

import java.util.Optional;

public class Intersection {
    private final Vector3 intersection;

    private Intersection(Vector3 intersection) {
        this.intersection = intersection;
    }

    public static Optional<Intersection> intersectPointerPane(Vector3 lp1,
            Vector3 lp2, PointerPane pane) {
        double minX, minY, minZ, maxX, maxY, maxZ;
        switch (pane.face) {
            case UP:
                minX = pane.x + pane.aabb.minX;
                minY = pane.y + pane.aabb.minY;
                minZ = pane.z + pane.aabb.maxZ;
                maxX = pane.x + pane.aabb.maxX;
                maxY = pane.y + pane.aabb.maxY;
                maxZ = pane.z + pane.aabb.maxZ;
                break;
            case DOWN:
                minX = pane.x + pane.aabb.minX;
                minY = pane.y + pane.aabb.minY;
                minZ = pane.z + pane.aabb.minZ;
                maxX = pane.x + pane.aabb.maxX;
                maxY = pane.y + pane.aabb.maxY;
                maxZ = pane.z + pane.aabb.minZ;
                break;
            case NORTH:
                minX = pane.x + pane.aabb.minX;
                minY = pane.y + pane.aabb.minY;
                minZ = pane.z + pane.aabb.minZ;
                maxX = pane.x + pane.aabb.maxX;
                maxY = pane.y + pane.aabb.minY;
                maxZ = pane.z + pane.aabb.maxZ;
                break;
            case EAST:
                minX = pane.x + pane.aabb.maxX;
                minY = pane.y + pane.aabb.minY;
                minZ = pane.z + pane.aabb.minZ;
                maxX = pane.x + pane.aabb.maxX;
                maxY = pane.y + pane.aabb.maxY;
                maxZ = pane.z + pane.aabb.maxZ;
                break;
            case SOUTH:
                minX = pane.x + pane.aabb.minX;
                minY = pane.y + pane.aabb.maxY;
                minZ = pane.z + pane.aabb.minZ;
                maxX = pane.x + pane.aabb.maxX;
                maxY = pane.y + pane.aabb.maxY;
                maxZ = pane.z + pane.aabb.maxZ;
                break;
            case WEST:
                minX = pane.x + pane.aabb.minX;
                minY = pane.y + pane.aabb.minY;
                minZ = pane.z + pane.aabb.minZ;
                maxX = pane.x + pane.aabb.minX;
                maxY = pane.y + pane.aabb.maxY;
                maxZ = pane.z + pane.aabb.maxZ;
                break;
            default:
                minX = 0.0;
                minY = 0.0;
                minZ = 0.0;
                maxX = 0.0;
                maxY = 0.0;
                maxZ = 0.0;
                break;
        }
        Optional<Intersection> inter =
                intersectPlane(lp1, lp2, new Vector3d(maxX, minY, minZ),
                        new Vector3d(minX, maxY, minZ),
                        new Vector3d(minX, minY, maxZ));
        if (!inter.isPresent()) {
            return Optional.empty();
        }
        Intersection intersection = inter.get();
        if (maxX < intersection.intersection.doubleX() ||
                minX > intersection.intersection.doubleX()) {
            return Optional.empty();
        }
        if (maxY < intersection.intersection.doubleY() ||
                minY > intersection.intersection.doubleY()) {
            return Optional.empty();
        }
        if (maxZ < intersection.intersection.doubleZ() ||
                minZ > intersection.intersection.doubleZ()) {
            return Optional.empty();
        }
        return Optional.of(intersection);
    }

    public static Optional<Intersection> intersectPlane(Vector3 lp1,
            Vector3 lp2, Vector3 p1, Vector3 p2, Vector3 p3) {
        Vector3 e1 = p2.minus(p1);
        Vector3 e2 = p3.minus(p1);
        Vector3 normal = FastMath.cross(e1, e2);
        return intersectPlane(lp1, lp2, p1, normal);
    }

    public static Optional<Intersection> intersectPlane(Vector3 lp1,
            Vector3 lp2, Vector3 p1, Vector3 normal) {
        Vector3 ldir = lp2.minus(lp1);
        double numerator = FastMath.dot(normal, ldir);
        if (FastMath.abs(numerator) > 0.0001) {
            Vector3 p1tolp1 = p1.minus(lp1);
            double t = FastMath.dot(normal, p1tolp1) / numerator;
            if (t <= 0 || t >= 1) {
                return Optional.empty();
            }
            Vector3 pos = lp1.plus(ldir.multiply(t));
            return Optional.of(new Intersection(pos));
        }
        return Optional.empty();
    }

    public Vector3 getPos() {
        return intersection;
    }
}
