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

package org.jbox2d.particle

import org.jbox2d.pooling.pop
import org.tobi29.math.max
import org.tobi29.math.min
import org.tobi29.math.vector.MutableVector2d
import org.tobi29.stdex.ArrayDeque
import org.tobi29.stdex.assert
import org.tobi29.utils.Pool
import kotlin.math.max
import kotlin.math.min

class VoronoiDiagram(generatorCapacity: Int) {

    private val m_generatorBuffer: Array<Generator>
    private var m_generatorCount: Int = 0
    private var m_countX: Int = 0
    private var m_countY: Int = 0
    // The diagram is an array of "pointers".
    private var m_diagram: Array<Generator?>? = null

    private val lower = MutableVector2d()
    private val upper = MutableVector2d()
    private val taskPool = Pool { VoronoiDiagramTask() }
    private val queue = ArrayDeque<VoronoiDiagramTask>()

    class Generator {
        internal val center = MutableVector2d()
        internal var tag: Int = 0
    }

    class VoronoiDiagramTask {
        internal var m_x: Int = 0
        internal var m_y: Int = 0
        internal var m_i: Int = 0
        internal lateinit var m_generator: Generator

        constructor()

        constructor(
            x: Int,
            y: Int,
            i: Int,
            g: Generator
        ) {
            m_x = x
            m_y = y
            m_i = i
            m_generator = g
        }

        operator fun set(
            x: Int,
            y: Int,
            i: Int,
            g: Generator
        ): VoronoiDiagramTask {
            m_x = x
            m_y = y
            m_i = i
            m_generator = g
            return this
        }
    }

    interface VoronoiDiagramCallback {
        fun callback(
            aTag: Int,
            bTag: Int,
            cTag: Int
        )
    }

    init {
        m_generatorBuffer = Array(generatorCapacity) { Generator() }
        m_generatorCount = 0
        m_countX = 0
        m_countY = 0
        m_diagram = null
    }

    fun getNodes(callback: VoronoiDiagramCallback) {
        for (y in 0 until m_countY - 1) {
            for (x in 0 until m_countX - 1) {
                val i = x + y * m_countX
                val a = m_diagram!![i]
                val b = m_diagram!![i + 1]
                val c = m_diagram!![i + m_countX]
                val d = m_diagram!![i + 1 + m_countX]
                if (b !== c) {
                    if (a !== b && a !== c) {
                        callback.callback(a!!.tag, b!!.tag, c!!.tag)
                    }
                    if (d !== b && d !== c) {
                        callback.callback(b!!.tag, d!!.tag, c!!.tag)
                    }
                }
            }
        }
    }

    fun addGenerator(
        center: MutableVector2d,
        tag: Int
    ) {
        val g = m_generatorBuffer[m_generatorCount++]
        g.center.x = center.x
        g.center.y = center.y
        g.tag = tag
    }

    fun generate(radius: Double) {
        assert { m_diagram == null }
        val inverseRadius = 1 / radius
        lower.x = Double.MAX_VALUE
        lower.y = Double.MAX_VALUE
        upper.x = -Double.MAX_VALUE
        upper.y = -Double.MAX_VALUE
        for (k in 0 until m_generatorCount) {
            val g = m_generatorBuffer[k]
            lower.set(min(lower, g.center))
            upper.set(max(upper, g.center))
        }
        m_countX = 1 + (inverseRadius * (upper.x - lower.x)).toInt()
        m_countY = 1 + (inverseRadius * (upper.y - lower.y)).toInt()
        m_diagram = arrayOfNulls(m_countX * m_countY)
        //queue.reset(arrayOfNulls(4 * m_countX * m_countX))
        for (k in 0 until m_generatorCount) {
            val g = m_generatorBuffer[k]
            g.center.x = inverseRadius * (g.center.x - lower.x)
            g.center.y = inverseRadius * (g.center.y - lower.y)
            val x = max(
                0,
                min(g.center.x.toInt(), m_countX - 1)
            )
            val y = max(
                0,
                min(g.center.y.toInt(), m_countY - 1)
            )
            queue.push(taskPool.push().set(x, y, x + y * m_countX, g))
        }
        while (true) {
            val front = queue.poll() ?: break
            val x = front.m_x
            val y = front.m_y
            val i = front.m_i
            val g = front.m_generator
            if (m_diagram!![i] == null) {
                m_diagram!![i] = g
                if (x > 0) {
                    queue.add(taskPool.push().set(x - 1, y, i - 1, g))
                }
                if (y > 0) {
                    queue.add(taskPool.push().set(x, y - 1, i - m_countX, g))
                }
                if (x < m_countX - 1) {
                    queue.add(taskPool.push().set(x + 1, y, i + 1, g))
                }
                if (y < m_countY - 1) {
                    queue.add(taskPool.push().set(x, y + 1, i + m_countX, g))
                }
            }
            taskPool.pop(front)
        }
        val maxIteration = m_countX + m_countY
        for (iteration in 0 until maxIteration) {
            for (y in 0 until m_countY) {
                for (x in 0 until m_countX - 1) {
                    val i = x + y * m_countX
                    val a = m_diagram!![i]
                    val b = m_diagram!![i + 1]
                    if (a !== b) {
                        queue.add(taskPool.push().set(x, y, i, b!!))
                        queue.add(taskPool.push().set(x + 1, y, i + 1, a!!))
                    }
                }
            }
            for (y in 0 until m_countY - 1) {
                for (x in 0 until m_countX) {
                    val i = x + y * m_countX
                    val a = m_diagram!![i]
                    val b = m_diagram!![i + m_countX]
                    if (a !== b) {
                        queue.add(taskPool.push().set(x, y, i, b!!))
                        queue.add(
                            taskPool.push().set(
                                x, y + 1, i + m_countX,
                                a!!
                            )
                        )
                    }
                }
            }
            var updated = false
            while (true) {
                val front = queue.poll() ?: break
                val x = front.m_x
                val y = front.m_y
                val i = front.m_i
                val k = front.m_generator
                val a = m_diagram!![i]
                if (a !== k) {
                    val ax = a!!.center.x - x
                    val ay = a.center.y - y
                    val bx = k.center.x - x
                    val by = k.center.y - y
                    val a2 = ax * ax + ay * ay
                    val b2 = bx * bx + by * by
                    if (a2 > b2) {
                        m_diagram!![i] = k
                        if (x > 0) {
                            queue.push(taskPool.push().set(x - 1, y, i - 1, k))
                        }
                        if (y > 0) {
                            queue.push(
                                taskPool.push().set(
                                    x, y - 1, i - m_countX,
                                    k
                                )
                            )
                        }
                        if (x < m_countX - 1) {
                            queue.push(taskPool.push().set(x + 1, y, i + 1, k))
                        }
                        if (y < m_countY - 1) {
                            queue.push(
                                taskPool.push().set(
                                    x, y + 1, i + m_countX,
                                    k
                                )
                            )
                        }
                        updated = true
                    }
                }
                taskPool.pop(front)
            }
            if (!updated) {
                break
            }
        }
    }
}
