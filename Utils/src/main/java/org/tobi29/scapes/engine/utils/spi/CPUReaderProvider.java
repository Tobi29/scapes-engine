package org.tobi29.scapes.engine.utils.spi;

import org.tobi29.scapes.engine.utils.CPUUtil;

public interface CPUReaderProvider {
    boolean available();

    CPUUtil.Reader reader();
}
