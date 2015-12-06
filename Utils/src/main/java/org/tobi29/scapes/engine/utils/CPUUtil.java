package org.tobi29.scapes.engine.utils;

import java8.util.Optional;
import org.tobi29.scapes.engine.utils.spi.CPUReaderProvider;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public final class CPUUtil {
    private static final Optional<CPUReaderProvider> PROVIDER = loadService();

    private CPUUtil() {
    }

    private static Optional<CPUReaderProvider> loadService() {
        for (CPUReaderProvider provider : ServiceLoader
                .load(CPUReaderProvider.class)) {
            try {
                if (provider.available()) {
                    return Optional.of(provider);
                }
            } catch (ServiceConfigurationError e) {
            }
        }
        return Optional.empty();
    }

    public static Optional<Reader> reader() {
        if (PROVIDER.isPresent()) {
            return Optional.of(PROVIDER.get().reader());
        } else {
            return Optional.empty();
        }
    }

    public interface Reader {
        double totalCPU();

        double totalCPU(long[] threads);
    }
}
