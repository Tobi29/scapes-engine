package org.tobi29.scapes.engine.utils.io.filesystem

import org.tobi29.scapes.engine.utils.io.InterruptibleChannel
import org.tobi29.scapes.engine.utils.io.SeekableByteChannel

interface FileChannel : SeekableByteChannel, InterruptibleChannel
