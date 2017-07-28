package org.tobi29.scapes.engine.utils.io.filesystem

import org.tobi29.scapes.engine.utils.findMap
import org.tobi29.scapes.engine.utils.io.AutoCloseable
import org.tobi29.scapes.engine.utils.io.IOException
import kotlin.coroutines.experimental.SequenceBuilder
import kotlin.coroutines.experimental.buildIterator

fun walk(
        path: FilePath,
        onEnter: suspend SequenceBuilder<FilePath>.(FilePath) -> Boolean =
        { true },
        onEntry: suspend SequenceBuilder<FilePath>.(FilePath) -> Unit =
        { yield(it) },
        onLeave: suspend SequenceBuilder<FilePath>.(FilePath) -> Unit =
        {},
        options: Array<out LinkOption> = emptyArray()
): FileTreeIterator =
        FileWalker(onEnter, onEntry, onLeave, options).iterator(path)

fun walk(
        path: FilePath,
        order: FileTreeOrder = FileTreeOrder.PRE_ORDER,
        options: Array<out LinkOption> = emptyArray()
): FileTreeIterator =
        FileWalker(order, options = options).iterator(path)

fun FileWalker.iterator(path: FilePath): FileTreeIterator =
        buildFileTreeIterator(streams) { walk(this@iterator, path) }

class FileWalker(
        internal val onEnter: suspend SequenceBuilder<FilePath>.(FilePath) -> Boolean =
        { true },
        internal val onEntry: suspend SequenceBuilder<FilePath>.(FilePath) -> Unit =
        { yield(it) },
        internal val onLeave: suspend SequenceBuilder<FilePath>.(FilePath) -> Unit =
        {},
        internal val options: Array<out LinkOption> = emptyArray()
) {
    constructor(
            order: FileTreeOrder = FileTreeOrder.PRE_ORDER,
            options: Array<out LinkOption> = emptyArray()
    ) : this(
            onEnter = {
                if (order == FileTreeOrder.PRE_ORDER) yield(it)
                true
            },
            onEntry = { yield(it) },
            onLeave = {
                if (order == FileTreeOrder.POST_ORDER) yield(it)
            },
            options = options
    )

    val streams = ArrayList<DirectoryStream>()
    internal val parents = HashSet<Any>()
}

suspend fun SequenceBuilder<FilePath>.walk(
        walker: FileWalker,
        path: FilePath
) {
    val onEntry = walker.onEntry
    val onEnter = walker.onEnter
    val onLeave = walker.onLeave
    val metadata = metadata(path, *walker.options)
    val basic = metadata.findMap<FileBasicMetadata>()
    val type = basic?.type
    if (type != FileType.TYPE_DIRECTORY && type != FileType.TYPE_SYMLINK) {
        onEntry(path)
        return
    }
    val stream = try {
        directoryStream(path)
    } catch (e: IOException) {
        onEntry(path)
        return
    }
    walker.streams.add(stream)
    // FIXME: This causes memory errors on Kotlin/Native and is not required as long as close() is called
    // try {
    if (onEnter(path)) {
        val key = metadata.findMap<FileBasicMetadata>()?.uid
        key?.let { if (!walker.parents.add(it)) return }
        try {
            for (child in stream) {
                walk(walker, child)
            }
        } finally {
            key?.let { walker.parents.remove(it) }
        }
    }
    // } finally {
    walker.streams.remove(stream)
    stream.close()
    // }
    onLeave(path)
}

interface FileTreeIterator : Iterator<FilePath>, AutoCloseable

enum class FileTreeOrder {
    PRE_ORDER,
    POST_ORDER
}

private fun buildFileTreeIterator(streams: MutableList<out AutoCloseable>,
                                  block: suspend SequenceBuilder<FilePath>.() -> Unit): FileTreeIterator {
    val iterator = buildIterator { block() }
    return object : FileTreeIterator, Iterator<FilePath> by iterator {
        override fun close() {
            // Cleanup in reverse order to avoid copying in array
            val cleanup = streams.listIterator(streams.size)
            while (cleanup.hasPrevious()) {
                cleanup.previous().close()
                cleanup.remove()
            }
        }
    }
}

private fun buildFileTreeIterator(close: () -> Unit,
                                  block: suspend SequenceBuilder<FilePath>.() -> Unit): FileTreeIterator {
    val iterator = buildIterator { block() }
    return object : FileTreeIterator, Iterator<FilePath> by iterator {
        override fun close() {
            close()
        }
    }
}
