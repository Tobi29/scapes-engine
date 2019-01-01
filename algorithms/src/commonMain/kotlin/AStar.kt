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

package org.tobi29.algorithms

/**
 * Executes the Dijkstra algorithm on a generic interface for graphs
 *
 * [cost] should normally add the previous cost to the edge cost
 * @param neighbours Returns nodes that can be directly reached from the given one
 * @param cost Returns the cost for going from one node to another
 * @param isFinished Returns `true` if [setFinished] was called for the given node
 * @param setFinished Called when the shortest path to a node was found
 * @param starts Starting nodes with initial cost
 * @return `true` if the algorithm was terminated by [setFinished] returning `true`
 */
inline fun <N, S : Comparable<S>> pathindDijkstra(
    neighbours: (N) -> Sequence<N>,
    cost: (N, N, S) -> S,
    isFinished: (N) -> Boolean,
    setFinished: (N, N, S) -> Boolean,
    starts: Iterable<Pair<N, S>>
): Boolean = pathfindAStar(
    neighbours,
    cost, { _, pathCost -> pathCost },
    isFinished, setFinished,
    starts
)

/**
 * Executes the A* algorithm on a generic interface for graphs
 *
 * [cost] should normally add the previous cost to the edge cost
 *
 * [score] should normally add the given cost to the estimated value
 * @param neighbours Returns nodes that can be directly reached from the given one
 * @param cost Returns the cost for going from one node to another
 * @param score Returns the score for a given node and the cost
 * @param isFinished Returns `true` if [setFinished] was called for the given node
 * @param setFinished Called when the shortest path to a node was found
 * @param starts Starting nodes with initial cost
 * @return `true` if the algorithm was terminated by [setFinished] returning `true`
 */
inline fun <N, S : Comparable<S>> pathfindAStar(
    neighbours: (N) -> Sequence<N>,
    cost: (N, N, S) -> S,
    score: (N, S) -> S,
    isFinished: (N) -> Boolean,
    setFinished: (N, N, S) -> Boolean,
    starts: Iterable<Pair<N, S>>
): Boolean {
    val queue = PriorityQueue<Entry<N, S>>()
    val entryMap = HashMap<N, Entry<N, S>>()
    for ((start, startCost) in starts) {
        Entry(start, start, startCost, score(start, startCost)).also { entry ->
            queue.add(entry)
            entryMap[start] = entry
        }
    }
    while (true) {
        val (current, previous, previousCost, _) = queue.poll() ?: break
        entryMap.remove(current)
        if (setFinished(current, previous, previousCost)) return true
        for (neighbour in neighbours(current)) {
            if (isFinished(neighbour)) continue
            val newCost = cost(current, neighbour, previousCost)
            val newScore = score(neighbour, newCost)
            val existing = entryMap[neighbour]
            if (existing != null) {
                if (existing.score >= newScore) {
                    queue.remove(existing)
                    // We overwrite the entry in entryMap down below
                } else {
                    continue
                }
            }
            Entry(neighbour, current, newCost, newScore).also { entry ->
                queue.add(entry)
                entryMap[neighbour] = entry
            }
        }
    }
    return false
}

@PublishedApi
internal data class Entry<N, S : Comparable<S>>(
    val node: N,
    val previous: N,
    val cost: S,
    val score: S
) : Comparable<Entry<N, S>> {
    override fun compareTo(other: Entry<N, S>) = score.compareTo(other.score)
}
