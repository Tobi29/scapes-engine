/*
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.jbox2d.collision

import org.jbox2d.common.Rot
import org.jbox2d.common.Settings
import org.jbox2d.common.Transform
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.Vector2d
import org.tobi29.math.vector.distanceSqr
import org.tobi29.math.vector.normalizeSafe
import org.tobi29.stdex.math.sqr

/**
 * This is used to compute the current state of a contact manifold.
 *
 * @author daniel
 */
class WorldManifold {
    /**
     * World vector pointing from A to B
     */
    val normal = MutableVector2d()

    /**
     * World contact point (point of intersection)
     */
    val points: Array<MutableVector2d> =
        Array(Settings.maxManifoldPoints) { MutableVector2d() }

    /**
     * A negative value indicates overlap, in meters.
     */
    val separations: DoubleArray = DoubleArray(Settings.maxManifoldPoints)

    fun initialize(
        manifold: Manifold,
        xfA: Transform,
        radiusA: Double,
        xfB: Transform,
        radiusB: Double
    ) {
        if (manifold.pointCount == 0) {
            return
        }

        when (manifold.type) {
            Manifold.ManifoldType.CIRCLES -> {
                normal.x = 1.0
                normal.y = 0.0
                val v = manifold.localPoint
                // Transform.mulToOutUnsafe(xfA, manifold.localPoint, pointA);
                // Transform.mulToOutUnsafe(xfB, manifold.points[0].localPoint, pointB);
                val pointA = Vector2d(
                    xfA.q.cos * v.x - xfA.q.sin * v.y + xfA.p.x,
                    xfA.q.sin * v.x + xfA.q.cos * v.y + xfA.p.y
                )
                val mp0p = manifold.points[0].localPoint
                val pointB = Vector2d(
                    xfB.q.cos * mp0p.x - xfB.q.sin * mp0p.y + xfB.p.x,
                    xfB.q.sin * mp0p.x + xfB.q.cos * mp0p.y + xfB.p.y
                )

                if (pointA distanceSqr pointB > sqr(Settings.EPSILON)) {
                    normal.x = pointB.x - pointA.x
                    normal.y = pointB.y - pointA.y
                    normal.normalizeSafe()
                }

                val cAx = normal.x * radiusA + pointA.x
                val cAy = normal.y * radiusA + pointA.y

                val cBx = -normal.x * radiusB + pointB.x
                val cBy = -normal.y * radiusB + pointB.y

                points[0].x = (cAx + cBx) * .5
                points[0].y = (cAy + cBy) * .5
                separations[0] = (cBx - cAx) * normal.x + (cBy - cAy) * normal.y
            }
            Manifold.ManifoldType.FACE_A -> {
                Rot.mulToOut(xfA.q, manifold.localNormal, normal)
                val planePoint = Transform.mul(xfA, manifold.localPoint.now())

                for (i in 0 until manifold.pointCount) {
                    // b2Vec2 clipPoint = b2Mul(xfB, manifold->points[i].localPoint);
                    // b2Vec2 cA = clipPoint + (radiusA - b2Dot(clipPoint - planePoint,
                    // normal)) * normal;
                    // b2Vec2 cB = clipPoint - radiusB * normal;
                    // points[i] = 0.5 * (cA + cB);
                    val clipPoint =
                        Transform.mul(xfB, manifold.points[i].localPoint.now())
                    // use cA as temporary for now
                    // cA.set(clipPoint).subLocal(planePoint);
                    // float scalar = radiusA - Vec2.dot(cA, normal);
                    // cA.set(normal).mulLocal(scalar).addLocal(clipPoint);
                    // cB.set(normal).mulLocal(radiusB).subLocal(clipPoint).negateLocal();
                    // points[i].set(cA).addLocal(cB).mulLocal(0.5);

                    val scalar =
                        radiusA - ((clipPoint.x - planePoint.x) * normal.x + (clipPoint.y - planePoint.y) * normal.y)

                    val cAx = normal.x * scalar + clipPoint.x
                    val cAy = normal.y * scalar + clipPoint.y

                    val cBx = -normal.x * radiusB + clipPoint.x
                    val cBy = -normal.y * radiusB + clipPoint.y

                    points[i].x = (cAx + cBx) * .5
                    points[i].y = (cAy + cBy) * .5
                    separations[i] = (cBx - cAx) * normal.x + (cBy - cAy) *
                            normal.y
                }
            }
            Manifold.ManifoldType.FACE_B -> {
                Rot.mulToOut(xfB.q, manifold.localNormal, normal)
                val planePoint = Transform.mul(xfB, manifold.localPoint.now())

                // final Mat22 R = xfB.q;
                // normal.x = R.ex.x * manifold.localNormal.x + R.ey.x * manifold.localNormal.y;
                // normal.y = R.ex.y * manifold.localNormal.x + R.ey.y * manifold.localNormal.y;
                // final Vec2 v = manifold.localPoint;
                // planePoint.x = xfB.p.x + xfB.q.ex.x * v.x + xfB.q.ey.x * v.y;
                // planePoint.y = xfB.p.y + xfB.q.ex.y * v.x + xfB.q.ey.y * v.y;

                for (i in 0 until manifold.pointCount) {
                    // b2Vec2 clipPoint = b2Mul(xfA, manifold->points[i].localPoint);
                    // b2Vec2 cB = clipPoint + (radiusB - b2Dot(clipPoint - planePoint,
                    // normal)) * normal;
                    // b2Vec2 cA = clipPoint - radiusA * normal;
                    // points[i] = 0.5 * (cA + cB);

                    val clipPoint =
                        Transform.mul(xfA, manifold.points[i].localPoint.now())
                    // cB.set(clipPoint).subLocal(planePoint);
                    // float scalar = radiusB - Vec2.dot(cB, normal);
                    // cB.set(normal).mulLocal(scalar).addLocal(clipPoint);
                    // cA.set(normal).mulLocal(radiusA).subLocal(clipPoint).negateLocal();
                    // points[i].set(cA).addLocal(cB).mulLocal(0.5);

                    // points[i] = 0.5 * (cA + cB);

                    //
                    // clipPoint.x = xfA.p.x + xfA.q.ex.x * manifold.points[i].localPoint.x + xfA.q.ey.x *
                    // manifold.points[i].localPoint.y;
                    // clipPoint.y = xfA.p.y + xfA.q.ex.y * manifold.points[i].localPoint.x + xfA.q.ey.y *
                    // manifold.points[i].localPoint.y;

                    val scalar =
                        radiusB - ((clipPoint.x - planePoint.x) * normal.x + (clipPoint.y - planePoint.y) * normal.y)

                    val cBx = normal.x * scalar + clipPoint.x
                    val cBy = normal.y * scalar + clipPoint.y

                    val cAx = -normal.x * radiusA + clipPoint.x
                    val cAy = -normal.y * radiusA + clipPoint.y

                    points[i].x = (cAx + cBx) * .5
                    points[i].y = (cAy + cBy) * .5
                    separations[i] = (cAx - cBx) * normal.x + (cAy - cBy) *
                            normal.y
                }
                // Ensure normal points from A to B.
                normal.x = -normal.x
                normal.y = -normal.y
            }
        }
    }
}
