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
package org.tobi29.scapes.engine.utils.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Joiner {
    private final Joinable[] joinables;

    private Joiner(Joinable thread) {
        joinables = new Joinable[]{thread};
    }

    public Joiner(Collection<Joiner> joiners) {
        this(joiners.toArray(new Joiner[joiners.size()]));
    }

    public Joiner(Joiner... joiners) {
        List<Joinable> list = new ArrayList<>(joiners.length);
        for (Joiner joiner : joiners) {
            Collections.addAll(list, joiner.joinables);
        }
        joinables = list.toArray(new Joinable[list.size()]);
    }

    @SuppressWarnings("NakedNotify")
    public void wake() {
        for (Joinable thread : joinables) {
            synchronized (thread) {
                thread.woken.set(true);
                thread.notifyAll();
            }
        }
    }

    @SuppressWarnings("NakedNotify")
    public void join() {
        for (Joinable thread : joinables) {
            thread.marked = true;
        }
        for (Joinable thread : joinables) {
            while (!thread.joining) {
                synchronized (thread) {
                    thread.woken.set(true);
                    thread.notifyAll();
                    try {
                        thread.wait(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    @SuppressWarnings("NakedNotify")
    public void join(Runnable runnable) {
        for (Joinable thread : joinables) {
            thread.marked = true;
        }
        for (Joinable thread : joinables) {
            while (!thread.joining) {
                synchronized (thread) {
                    thread.woken.set(true);
                    thread.notifyAll();
                    runnable.run();
                    try {
                        thread.wait(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public static class Joinable {
        private final Joiner joiner;
        private final AtomicBoolean woken = new AtomicBoolean();
        private volatile boolean joining, marked;

        public Joinable() {
            joiner = new Joiner(this);
        }

        public synchronized void join() {
            joining = true;
            notifyAll();
        }

        public Joiner joiner() {
            return joiner;
        }

        public boolean marked() {
            return marked;
        }

        public void sleep() {
            sleep(0);
        }

        @SuppressWarnings("WaitNotInLoop")
        public synchronized void sleep(long time) {
            if (!woken.getAndSet(false)) {
                try {
                    wait(time);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
