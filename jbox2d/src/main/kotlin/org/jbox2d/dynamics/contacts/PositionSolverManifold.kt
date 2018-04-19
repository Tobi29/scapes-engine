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

package org.jbox2d.dynamics.contacts

import org.jbox2d.collision.Manifold
import org.jbox2d.common.Transform
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.math.vector.normalizeSafe
import org.tobi29.stdex.assert

internal class PositionSolverManifold {

    val normal = MutableVector2d()
    val point = MutableVector2d()
    var separation: Double = 0.0

    fun initialize(
        pc: ContactPositionConstraint,
        xfA: Transform,
        xfB: Transform,
        index: Int
    ) {
        assert { pc.pointCount > 0 }

        val xfAq = xfA.q
        val xfBq = xfB.q
        val pcLocalPointsI = pc.localPoints[index]
        when (pc.type) {
            Manifold.ManifoldType.CIRCLES -> {
                // Transform.mulToOutUnsafe(xfA, pc.localPoint, pointA);
                // Transform.mulToOutUnsafe(xfB, pc.localPoints[0], pointB);
                // normal.set(pointB).subLocal(pointA);
                // normal.normalize();
                //
                // point.set(pointA).addLocal(pointB).mulLocal(.5);
                // temp.set(pointB).subLocal(pointA);
                // separation = Vec2.dot(temp, normal) - pc.radiusA - pc.radiusB;
                val plocalPoint = pc.localPoint
                val pLocalPoints0 = pc.localPoints[0]
                val pointAx =
                    xfAq.cos * plocalPoint.x - xfAq.sin * plocalPoint.y + xfA.p.x
                val pointAy =
                    xfAq.sin * plocalPoint.x + xfAq.cos * plocalPoint.y + xfA.p.y
                val pointBx =
                    xfBq.cos * pLocalPoints0.x - xfBq.sin * pLocalPoints0.y + xfB.p.x
                val pointBy =
                    xfBq.sin * pLocalPoints0.x + xfBq.cos * pLocalPoints0.y + xfB.p.y
                normal.x = pointBx - pointAx
                normal.y = pointBy - pointAy
                normal.normalizeSafe()

                point.x = (pointAx + pointBx) * .5
                point.y = (pointAy + pointBy) * .5
                val tempx = pointBx - pointAx
                val tempy = pointBy - pointAy
                separation = tempx * normal.x + tempy * normal.y - pc.radiusA -
                        pc.radiusB
            }

            Manifold.ManifoldType.FACE_A -> {
                // Rot.mulToOutUnsafe(xfAq, pc.localNormal, normal);
                // Transform.mulToOutUnsafe(xfA, pc.localPoint, planePoint);
                //
                // Transform.mulToOutUnsafe(xfB, pc.localPoints[index], clipPoint);
                // temp.set(clipPoint).subLocal(planePoint);
                // separation = Vec2.dot(temp, normal) - pc.radiusA - pc.radiusB;
                // point.set(clipPoint);
                val pcLocalNormal = pc.localNormal
                val pcLocalPoint = pc.localPoint
                normal.x = xfAq.cos * pcLocalNormal.x - xfAq.sin *
                        pcLocalNormal.y
                normal.y = xfAq.sin * pcLocalNormal.x + xfAq.cos *
                        pcLocalNormal.y
                val planePointx =
                    xfAq.cos * pcLocalPoint.x - xfAq.sin * pcLocalPoint.y + xfA.p.x
                val planePointy =
                    xfAq.sin * pcLocalPoint.x + xfAq.cos * pcLocalPoint.y + xfA.p.y

                val clipPointx =
                    xfBq.cos * pcLocalPointsI.x - xfBq.sin * pcLocalPointsI.y + xfB.p.x
                val clipPointy =
                    xfBq.sin * pcLocalPointsI.x + xfBq.cos * pcLocalPointsI.y + xfB.p.y
                val tempx = clipPointx - planePointx
                val tempy = clipPointy - planePointy
                separation = tempx * normal.x + tempy * normal.y - pc.radiusA -
                        pc.radiusB
                point.x = clipPointx
                point.y = clipPointy
            }

            Manifold.ManifoldType.FACE_B -> {
                // Rot.mulToOutUnsafe(xfBq, pc.localNormal, normal);
                // Transform.mulToOutUnsafe(xfB, pc.localPoint, planePoint);
                //
                // Transform.mulToOutUnsafe(xfA, pcLocalPointsI, clipPoint);
                // temp.set(clipPoint).subLocal(planePoint);
                // separation = Vec2.dot(temp, normal) - pc.radiusA - pc.radiusB;
                // point.set(clipPoint);
                //
                // // Ensure normal points from A to B
                // normal.negateLocal();
                val pcLocalNormal = pc.localNormal
                val pcLocalPoint = pc.localPoint
                normal.x = xfBq.cos * pcLocalNormal.x - xfBq.sin *
                        pcLocalNormal.y
                normal.y = xfBq.sin * pcLocalNormal.x + xfBq.cos *
                        pcLocalNormal.y
                val planePointx =
                    xfBq.cos * pcLocalPoint.x - xfBq.sin * pcLocalPoint.y + xfB.p.x
                val planePointy =
                    xfBq.sin * pcLocalPoint.x + xfBq.cos * pcLocalPoint.y + xfB.p.y

                val clipPointx =
                    xfAq.cos * pcLocalPointsI.x - xfAq.sin * pcLocalPointsI.y + xfA.p.x
                val clipPointy =
                    xfAq.sin * pcLocalPointsI.x + xfAq.cos * pcLocalPointsI.y + xfA.p.y
                val tempx = clipPointx - planePointx
                val tempy = clipPointy - planePointy
                separation = tempx * normal.x + tempy * normal.y - pc.radiusA -
                        pc.radiusB
                point.x = clipPointx
                point.y = clipPointy
                normal.x = normal.x * -1
                normal.y = normal.y * -1
            }
        }
    }
}
