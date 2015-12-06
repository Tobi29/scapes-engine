package org.tobi29.scapes.engine.utils.io.filesystem.nio;

import org.tobi29.scapes.engine.utils.io.filesystem.FileUtilImpl;
import org.tobi29.scapes.engine.utils.io.filesystem.spi.FileSystemProvider;

public class NIOFileSystemProvider implements FileSystemProvider {
    @Override
    public boolean available() {
        return true;
    }

    @Override
    public FileUtilImpl implementation() {
        return new NIOFileUtilImpl();
    }
}
