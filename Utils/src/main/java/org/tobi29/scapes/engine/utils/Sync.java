/*
 * Copyright 2012-2016 Tobi29
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

package org.tobi29.scapes.engine.utils;

import java8.util.function.LongConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tobi29.scapes.engine.utils.math.FastMath;
import org.tobi29.scapes.engine.utils.task.Joiner;

import java.util.concurrent.locks.LockSupport;

/**
 * Utility class for "mostly" accurate frame capping
 */
public class Sync {
    private static final Logger LOGGER = LoggerFactory.getLogger(Sync.class);
    private final long minSkipDelay, maxDiff;
    private final boolean logSkip;
    private final String name;
    private double currentTPS, delta;
    private long lastSync, sync, diff, tickDiff;

    /**
     * Constructs a new {@link Sync} instance
     *
     * @param tps          Target TPS
     * @param minSkipDelay Minimum delay to start skipping frames in
     *                     nanoseconds
     * @param logSkip      Whether or not to log skips
     * @param name         Name used by to log skips
     */
    public Sync(double tps, long minSkipDelay, boolean logSkip, String name) {
        if (minSkipDelay < 0) {
            throw new IllegalArgumentException(
                    "Minimum skip delay is negative");
        }
        maxDiff = (long) (1000000000.0 / tps);
        this.minSkipDelay = -minSkipDelay;
        this.logSkip = logSkip;
        this.name = name;
        currentTPS = tps;
        delta = 1.0 / tps;
    }

    /**
     * Initializes the sync to start capping.
     * <p>
     * This method should be called right before the first iteration starts
     */
    public void init() {
        sync = System.nanoTime();
        lastSync = sync;
    }

    /**
     * Get the TPS
     *
     * @return Calculated TPS (1 / delta)
     */
    public double tps() {
        return currentTPS;
    }

    /**
     * Get the delta time in seconds
     *
     * @return Current delta (1 / TPS), clamped between {@code 0.0001} and
     * {@code 1.0}
     */
    public double delta() {
        return delta(0.0001, 1.0);
    }

    /**
     * Get the delta time in seconds
     *
     * @param min Minimum value
     * @param max Maximum value
     * @return Current delta (1 / TPS), clamped between min and max
     */
    public double delta(double min, double max) {
        return FastMath.clamp(delta, min, max);
    }

    /**
     * Get the delta
     *
     * @return Nanoseconds between syncs (excluding sleep)
     */
    public int diff() {
        return (int) diff;
    }

    /**
     * Get the delta
     *
     * @return Nanoseconds between syncs (including sleep)
     */
    public int tickDiff() {
        return (int) tickDiff;
    }

    /**
     * Get the nano time of last sync
     *
     * @return Nanoseconds between syncs
     */
    public long lastSync() {
        return lastSync;
    }

    /**
     * Get the anticipated delta
     *
     * @return Nanoseconds between syncs including sleeping time
     */
    public int maxDiff() {
        return (int) maxDiff;
    }

    /**
     * Execute cap and calculate TPS
     * <p>
     * Non capping counterpart:
     *
     * @see #tick()
     */
    public void cap() {
        cap(LockSupport::parkNanos);
    }

    /**
     * Execute cap and calculate TPS
     * <p>
     * Non capping counterpart:
     *
     * @param joiner Joiner that the {@link Joiner.Joinable#sleep} method is
     *               called on
     * @see #tick()
     */
    public void cap(Joiner.Joinable joiner) {
        cap(sleep -> {
            // Using nanos is useless as the implementation on wait is bad
            if (sleep < 1000000) {
                return;
            }
            joiner.sleep(sleep / 1000000);
        });
    }

    /**
     * Execute cap and calculate TPS
     * <p>
     * Non capping counterpart: {@link #tick()}
     *
     * @param park Callback for executing the sleep
     * @see #tick()
     */
    public void cap(LongConsumer park) {
        long current = System.nanoTime();
        diff = current - lastSync;
        sync += maxDiff;
        long sleep = sync - current;
        if (sleep < minSkipDelay) {
            if (logSkip) {
                long oldSync = sync;
                sync = lastSync + maxDiff;
                LOGGER.warn("{}-Sync is skipping {} nanoseconds!", name,
                        sync - oldSync);
            } else {
                sync = lastSync + maxDiff;
            }
        } else if (sleep > maxDiff) {
            park.accept(maxDiff);
        } else {
            park.accept(sleep);
        }
        long newSync = System.nanoTime();
        tickDiff = newSync - lastSync;
        currentTPS = 1000000000.0 / tickDiff;
        double delta = tickDiff / 1000000000.0;
        if (Double.isNaN(delta)) {
            this.delta = 0.0;
        } else {
            this.delta = delta;
        }
        lastSync = newSync;
    }

    /**
     * Calculate TPS without capping
     * <p>
     * Capping counterpart: {@link #cap()}
     *
     * @see #cap()
     */
    public void tick() {
        long newSync = System.nanoTime();
        diff = newSync - lastSync;
        tickDiff = diff;
        currentTPS = 1000000000.0 / tickDiff;
        double delta = tickDiff / 1000000000.0;
        if (Double.isNaN(delta)) {
            this.delta = 0.0;
        } else {
            this.delta = delta;
        }
        lastSync = newSync;
    }
}
