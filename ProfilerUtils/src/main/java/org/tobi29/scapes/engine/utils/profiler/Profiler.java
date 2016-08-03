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

package org.tobi29.scapes.engine.utils.profiler;

import java8.util.Maps;
import java8.util.Optional;
import java8.util.function.Supplier;
import org.tobi29.scapes.engine.utils.Streams;
import org.tobi29.scapes.engine.utils.ThreadLocalUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public final class Profiler {
    private static final Map<Thread, Profiler> PROFILERS = new WeakHashMap<>();
    private static final ThreadLocal<Profiler> INSTANCE =
            ThreadLocalUtil.ofThread(Profiler::new);
    private static boolean enabled;
    private final Node rootNode;
    private Node node;

    private Profiler(Thread thread) {
        node = rootNode = new Node(thread::getName);
        PROFILERS.put(thread, this);
    }

    public static C section(String name) {
        if (!enabled) {
            return () -> {
            };
        }
        return INSTANCE.get().enterTryNode(name);
    }

    public static Optional<Node> node(Thread thread) {
        return Optional.ofNullable(PROFILERS.get(thread))
                .map(profiler -> profiler.rootNode);
    }

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void reset() {
        Streams.forEach(PROFILERS.values(), Profiler::resetNodes);
    }

    private C enterTryNode(String name) {
        enterNode(name);
        return () -> exitNode(name);
    }

    private void enterNode(String name) {
        node = Maps.computeIfAbsent(node.children, name,
                key -> new Node(() -> key, node));
        node.lastEnter = System.nanoTime();
    }

    private void exitNode(String name) {
        if (!node.parent.isPresent()) {
            throw new IllegalStateException(
                    "Profiler stack popped on root node");
        }
        assert Objects.equals(name, node.name.get());
        node.time += System.nanoTime() - node.lastEnter;
        node = node.parent.get();
    }

    private void resetNodes() {
        rootNode.children.clear();
    }

    @SuppressWarnings("InterfaceNamingConvention")
    public interface C extends AutoCloseable {
        @Override
        void close();
    }

    public static final class Node {
        public final Optional<Node> parent;
        public final Supplier<String> name;
        public final Map<String, Node> children = new HashMap<>();
        private long lastEnter, time;

        private Node(Supplier<String> name) {
            this(name, Optional.empty());
        }

        private Node(Supplier<String> name, Node parent) {
            this(name, Optional.of(parent));
        }

        private Node(Supplier<String> name, Optional<Node> parent) {
            this.name = name;
            this.parent = parent;
        }

        public double time() {
            return time / 1000000000.0;
        }
    }
}
