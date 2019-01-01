/*
 * Copyright 2012-2018 Tobi29
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

package org.tobi29.coroutines

import kotlinx.coroutines.channels.Channel

// TODO: Remove after 0.0.14

@Deprecated(
    "Use kotlinx.coroutines version",
    ReplaceWith(
        "Channel<E>",
        "kotlinx.coroutines.channels.Channel"
    )
)
typealias Channel<E> = kotlinx.coroutines.channels.Channel<E>

@Deprecated(
    "Use kotlinx.coroutines version",
    ReplaceWith(
        "Channel<E>",
        "kotlinx.coroutines.channels.Channel"
    )
)
typealias UnboundChannel<E> = kotlinx.coroutines.channels.Channel<E>

@Deprecated(
    "Use kotlinx.coroutines version",
    ReplaceWith(
        "Channel<E>(Channel.UNLIMITED)",
        "kotlinx.coroutines.channels.Channel"
    )
)
inline fun <E> LinkedListChannel() =
    kotlinx.coroutines.channels.Channel<E>(Channel.UNLIMITED)

@Deprecated(
    "Use kotlinx.coroutines version",
    ReplaceWith(
        "ClosedSendChannelException",
        "kotlinx.coroutines.channels.ClosedSendChannelException"
    )
)
typealias ClosedSendChannelException = kotlinx.coroutines.channels.ClosedSendChannelException

@Deprecated(
    "Use kotlinx.coroutines version",
    ReplaceWith(
        "ClosedReceiveChannelException",
        "kotlinx.coroutines.channels.ClosedReceiveChannelException"
    )
)
typealias ClosedReceiveChannelException = kotlinx.coroutines.channels.ClosedReceiveChannelException
