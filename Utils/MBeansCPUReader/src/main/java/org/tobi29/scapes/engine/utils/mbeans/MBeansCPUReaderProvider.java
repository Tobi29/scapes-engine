package org.tobi29.scapes.engine.utils.mbeans;

import java8.util.Maps;
import org.tobi29.scapes.engine.utils.CPUUtil;
import org.tobi29.scapes.engine.utils.spi.CPUReaderProvider;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class MBeansCPUReaderProvider implements CPUReaderProvider {
    private static final ThreadMXBean THREADS =
            ManagementFactory.getThreadMXBean();

    @Override
    public boolean available() {
        return THREADS.isThreadCpuTimeSupported();
    }

    @Override
    public CPUUtil.Reader reader() {
        return new CPUUtil.Reader() {
            private final Map<Long, Long> threadTimes = new HashMap<>();
            private final Set<Long> threads = new HashSet<>();
            private long lastTime;

            @Override
            public double totalCPU() {
                return totalCPU(THREADS.getAllThreadIds());
            }

            @Override
            public double totalCPU(long[] threads) {
                double cpu = 0.0;
                if (THREADS.isThreadCpuTimeSupported()) {
                    if (!THREADS.isThreadCpuTimeEnabled()) {
                        THREADS.setThreadCpuTimeEnabled(true);
                    }
                    long time = System.nanoTime();
                    double delta = time - lastTime;
                    for (long thread : threads) {
                        long threadTime = THREADS.getThreadCpuTime(thread);
                        double threadDelta = threadTime -
                                Maps.getOrDefault(threadTimes, thread, 0L);
                        this.threads.add(thread);
                        threadTimes.put(thread, threadTime);
                        threadDelta /= delta;
                        cpu += threadDelta;
                    }
                    Iterator<Long> iterator = threadTimes.keySet().iterator();
                    while (iterator.hasNext()) {
                        if (!this.threads.contains(iterator.next())) {
                            iterator.remove();
                        }
                    }
                    this.threads.clear();
                    lastTime = time;
                } else {
                    cpu = Double.NaN;
                }
                return cpu;
            }
        };
    }
}
