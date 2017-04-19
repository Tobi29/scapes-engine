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

package org.tobi29.scapes.engine.utils.tests

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.tobi29.scapes.engine.test.assertions.shouldEqual
import org.tobi29.scapes.engine.utils.EventDispatcher

object ListenerTests : Spek({
    describe("listener owners") {
        given("a graph of with listeners with priority") {
            val graphMem by memoized {
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
            on("firing an event on root") {
                it("should fire all listeners in the correct order") {
                    val (graph, counter) = graphMem
                    graph["Root"]?.fire(Unit)
                    counter() shouldEqual 4
                }
            }
        }
        given("a graph of listeners") {
            val graphMem by memoized {
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
            on("firing an event on root") {
                it("should fire all listeners") {
                    val (graph, counter) = graphMem
                    graph["Root"]?.fire(Unit)
                    counter() shouldEqual 4
                }
            }
            on("firing an event on a child") {
                it("should fire all listeners") {
                    val (graph, counter) = graphMem
                    graph["1"]?.fire(Unit)
                    counter() shouldEqual 4
                }
            }
            on("disabling a child and firing an event on root") {
                it("should fire all enabled listeners") {
                    val (graph, counter) = graphMem
                    graph["1"]?.disable()
                    graph["Root"]?.fire(Unit)
                    counter() shouldEqual 1
                }
            }
        }
    }
})
