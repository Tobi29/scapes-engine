/*
 * Copyright 2012-2019 Tobi29
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

package org.tobi29.utils

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.tobi29.assertions.shouldEqual

object ListenerTests : Spek({
    describe("listener owners") {
        val graphPriority = {
            var counter = 0
            val graph = HashMap<String, EventDispatcher>()
            graph.put("Root", EventDispatcher().apply {
                graph.put("1", EventDispatcher(this) {
                    listen<Unit>(10) {
                        counter++
                        counter shouldEqual 3
                    }
                    events.enable()
                    graph.put("11", EventDispatcher(events) {
                        listen<Unit>(0) {
                            counter++
                            counter shouldEqual 4
                        }
                        listen<Unit>(30) {
                            counter++
                            counter shouldEqual 1
                        }
                        events.enable()
                    })
                })
                graph.put("2", EventDispatcher(this) {
                    listen<Unit>(20) {
                        counter++
                        counter shouldEqual 2
                    }
                    events.enable()
                })
            })
            Pair(graph, { counter })
        }
        val graphListeners = {
            var counter = 0
            val graph = HashMap<String, EventDispatcher>()
            graph.put("Root", EventDispatcher().apply {
                graph.put("1", EventDispatcher(this) {
                    listen<Unit>(10) { counter++ }
                    events.enable()
                    graph.put("11", EventDispatcher(events) {
                        listen<Unit>(0) { counter++ }
                        listen<Unit>(30) { counter++ }
                        events.enable()
                    })
                })
                graph.put("2", EventDispatcher(this) {
                    listen<Unit>(20) { counter++ }
                    events.enable()
                })
            })
            Pair(graph, { counter })
        }
        describe("firing an event on root") {
            val (graph, counter) = graphPriority()
            graph["Root"]?.fire(Unit)
            it("should fire all listeners in the correct order") {
                counter() shouldEqual 4
            }
        }
        describe("firing an event on root") {
            val (graph, counter) = graphListeners()
            graph["Root"]?.fire(Unit)
            it("should fire all listeners") {
                counter() shouldEqual 4
            }
        }
        describe("firing an event on a child") {
            val (graph, counter) = graphListeners()
            graph["1"]?.fire(Unit)
            it("should fire all listeners") {
                counter() shouldEqual 4
            }
        }
        describe("disabling a child and firing an event on root") {
            val (graph, counter) = graphListeners()
            graph["1"]?.disable()
            graph["Root"]?.fire(Unit)
            it("should fire all enabled listeners") {
                counter() shouldEqual 1
            }
        }
    }
})
