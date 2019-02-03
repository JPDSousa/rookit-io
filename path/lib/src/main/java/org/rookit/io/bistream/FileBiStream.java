
package org.rookit.io.bistream;

import org.rookit.failsafe.Failsafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class FileBiStream implements BiStream {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileBiStream.class);

    private final Failsafe failsafe;
    private final Path path;

    FileBiStream(final Failsafe failsafe, final Path path) {
        this.failsafe = failsafe;
        logger.trace("New {} in path {}", FileBiStream.class.getName(), path);
        this.path = path;
    }

    @Override
    public void clear() {
        try {
            Files.deleteIfExists(this.path);
        } catch (final IOException e) {
            this.failsafe.handleException().inputOutputException(e);
        }
    }

    @Override
    public boolean isEmpty() {
        return Files.notExists(this.path)
                || !canRead();
    }
    
    @SuppressWarnings("boxing")
    private boolean canRead() {
        try (final InputStream input = Files.newInputStream(this.path, StandardOpenOption.CREATE,
                StandardOpenOption.READ)) {
            return input.read() != -1;
        } catch (final IOException e) {
            //noinspection AutoUnboxing
            return this.failsafe.handleException().inputOutputException(e);
        }
    }

    @Override
    public InputStream readFrom() {
        try {
            return Files.newInputStream(this.path, StandardOpenOption.CREATE, StandardOpenOption.READ);
        } catch (final IOException e) {
            return this.failsafe.handleException().inputOutputException(e);
        }
    }

    @Override
    public OutputStream writeTo() {
        try {
            return Files.newOutputStream(this.path);
        } catch (final IOException e) {
            return this.failsafe.handleException().inputOutputException(e);
        }
    }

    @Override
    public void copyFrom(final BiStream biStream) {
        if (!equals(biStream)) {
            try {
                Files.copy(biStream.readFrom(), this.path);
            } catch (final IOException e) {
                this.failsafe.handleException().inputOutputException(e);
            }
        }
        else {
            logger.trace("Attempting to copy path {} to itself. Ignoring.", this.path);
        }
    }

    @Override
    public String toString() {
        return "FileBiStream{" +
                "injector=" + this.failsafe +
                ", path='" + this.path + '\'' +
                "}";
    }
}
