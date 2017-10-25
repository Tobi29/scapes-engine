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

package org.tobi29.scapes.engine.codec

import org.tobi29.scapes.engine.utils.io.Closeable
import org.tobi29.scapes.engine.utils.tag.TagMap

/**
 * Interface for reading audio data from an encoded source
 */
interface ReadableAudioStream : Closeable {
    /**
     * Meta data of the stream, may be `null` until data becomes available
     *
     * Calling [get] has to eventually reveal it
     *
     * **Implementation note:** This really ought to return something non-null
     * before ever requesting a buffer in [get] to allow scanning files without
     * fully decoding them
     */
    val metaData: AudioMetaData?

    /**
     * Try filling the [buffer] with data
     *
     * Once this returns [Result.EOS] no further calls should be necessary
     * anymore
     *
     * **Note:** Even when this returns [Result.EOS] the [buffer] might still
     * contain leftover audio data
     * @param buffer The audio buffer to fill, possibly partially
     * @return [Result], for info head there
     * @see [Result]
     */
    // TODO: @Throws(IOException::class)
    fun get(buffer: AudioBuffer?): Result

    /**
     * Disposes any resources held by this object, no further calls should be
     * done on it
     */
    // TODO: @Throws(IOException::class)
    override fun close() {}

    /**
     * Result type when calling [get] with a possibly `null` [AudioBuffer]
     */
    enum class Result {
        /**
         * The data source is starved of data and you should wait until the next
         * call
         */
        YIELD,
        /**
         * The stream cannot continue without being given an audio buffer with
         * space in it
         */
        BUFFER,
        /**
         * The stream cannot return any more data, no further calls to [get]
         * should be made
         */
        EOS
    }
}

data class AudioMetaData(val vendor: String?,
                         val comment: TagMap?)
