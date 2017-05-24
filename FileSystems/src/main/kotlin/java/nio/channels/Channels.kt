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

package java.nio.channels

/*
@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header abstract class AbstractInterruptibleChannel : Channel, InterruptibleChannel {
    /**
     * @throws IOException
     */
    protected abstract fun implCloseChannel()

    protected fun begin()

    /**
     * @throws IOException
     */
    protected fun end(completed: Boolean)
}

@Suppress("HEADER_WITHOUT_IMPLEMENTATION")
header abstract class FileChannel : AbstractInterruptibleChannel, SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel {

    /**
     * @throws IOException
     */
    abstract override fun read(dst: ByteBuffer): Int

    /**
     * @throws IOException
     */
    abstract override fun read(dsts: Array<ByteBuffer>,
                               offset: Int,
                               length: Int): Long

    /**
     * @throws IOException
     */
    abstract override fun write(src: ByteBuffer): Int

    /**
     * @throws IOException
     */
    abstract override fun write(srcs: Array<ByteBuffer>,
                                offset: Int,
                                length: Int): Long

    /**
     * @throws IOException
     */
    abstract override fun position(): Long

    /**
     * @throws IOException
     */
    abstract override fun position(newPosition: Long): FileChannel

    /**
     * @throws IOException
     */
    abstract override fun size(): Long

    /**
     * @throws IOException
     */
    abstract override fun truncate(size: Long): FileChannel

    /**
     * @throws IOException
     */
    abstract fun force(metaData: Boolean)

    /**
     * @throws IOException
     */
    abstract fun transferTo(position: Long,
                            count: Long,
                            target: WritableByteChannel): Long

    /**
     * @throws IOException
     */
    abstract fun transferFrom(src: ReadableByteChannel,
                              position: Long,
                              count: Long): Long

    /**
     * @throws IOException
     */
    abstract fun read(dst: ByteBuffer,
                      position: Long): Int

    /**
     * @throws IOException
     */
    abstract fun write(src: ByteBuffer,
                       position: Long): Int
}
*/
