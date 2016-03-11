package org.tobi29.scapes.engine.utils.mbeans;

import java8.util.concurrent.ConcurrentMaps;
import org.tobi29.scapes.engine.utils.CPUUtil;
import org.tobi29.scapes.engine.utils.spi.CPUReaderProvider;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
            private final ConcurrentMap<Long, Long> lastThreadTimes =
                    new ConcurrentHashMap<>();
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
                    Map<Long, Long> threadTimes;
                    if (threads.length >= lastThreadTimes.size()) {
                        threadTimes = lastThreadTimes;
                    } else {
                        threadTimes = new ConcurrentHashMap<>();
                    }
                    for (long thread : threads) {
                        long threadTime = THREADS.getThreadCpuTime(thread);
                        double threadDelta = threadTime - ConcurrentMaps
                                .getOrDefault(lastThreadTimes, thread, 0L);
                        threadTimes.put(thread, threadTime);
                        threadDelta /= delta;
                        cpu += threadDelta;
                    }
                    lastTime = time;
                } else {
                    cpu = Double.NaN;
                }
                return cpu;
            }
        };
    }
}
