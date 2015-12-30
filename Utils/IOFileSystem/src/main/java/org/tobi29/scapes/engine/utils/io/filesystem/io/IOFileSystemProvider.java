package org.tobi29.scapes.engine.utils.io.filesystem.io;

import org.tobi29.scapes.engine.utils.io.filesystem.FileUtilImpl;
import org.tobi29.scapes.engine.utils.io.filesystem.spi.FileSystemProvider;

public class IOFileSystemProvider implements FileSystemProvider {
    @Override
    public boolean available() {
        return true;
    }

    @Override
    public FileUtilImpl implementation() {
        return new IOFileUtilImpl();
    }
}
