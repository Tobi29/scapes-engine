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

package org.tobi29.scapes.engine.utils.mbeans

import org.tobi29.scapes.engine.utils.CPUUtil
import org.tobi29.scapes.engine.utils.spi.CPUReaderProvider
import java.lang.management.ManagementFactory

class MBeansCPUReaderProvider : CPUReaderProvider {

    override fun available(): Boolean {
        return THREADS.isThreadCpuTimeSupported
    }

    override fun reader(): CPUUtil.Reader {
        return object : CPUUtil.Reader {
            private val threadTimes = HashMap<Long, Long>()
            private val threads = HashSet<Long>()
            private var lastTime: Long = 0

            override fun totalCPU(): Double {
                return totalCPU(THREADS.allThreadIds)
            }

            override fun totalCPU(threads: LongArray): Double {
                var cpu = 0.0
                if (THREADS.isThreadCpuTimeSupported) {
                    if (!THREADS.isThreadCpuTimeEnabled) {
                        THREADS.isThreadCpuTimeEnabled = true
                    }
                    val time = System.nanoTime()
                    val delta = (time - lastTime).toDouble()
                    for (thread in threads) {
                        val threadTime = THREADS.getThreadCpuTime(thread)
                        var threadDelta = (threadTime
                                - (threadTimes[thread] ?: 0L)).toDouble()
                        this.threads.add(thread)
                        threadTimes.put(thread, threadTime)
                        threadDelta /= delta
                        cpu += threadDelta
                    }
                    val iterator = threadTimes.keys.iterator()
                    while (iterator.hasNext()) {
                        if (!this.threads.contains(iterator.next())) {
                            iterator.remove()
                        }
                    }
                    this.threads.clear()
                    lastTime = time
                } else {
                    cpu = Double.NaN
                }
                return cpu
            }
        }
    }

    companion object {
        private val THREADS = ManagementFactory.getThreadMXBean()
    }
}
