
package org.rookit.io.bistream;

import com.google.common.collect.Queues;
import com.google.inject.Inject;
import org.rookit.failsafe.Failsafe;
import org.rookit.io.file.TemporaryFilePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Queue;

@SuppressWarnings("javadoc")
final class FileBiStreamFactoryImpl implements FileBiStreamFactory {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileBiStreamFactoryImpl.class);

    private final Failsafe failsafe;
    private final TemporaryFilePool temporaryFilePool;
    private final Queue<Path> tempFiles;

    @Inject
    private FileBiStreamFactoryImpl(final TemporaryFilePool temporaryFilePool,
                                    final Failsafe failsafe) {
        this.temporaryFilePool = temporaryFilePool;
        this.failsafe = failsafe;
        this.tempFiles = Queues.newArrayDeque();
    }

    @Override
    public BiStream createTemporary() {
        try {
            return createWithAbsolutePath(this.temporaryFilePool.poll());
        } catch (final IOException e) {
            return this.failsafe.handleException().inputOutputException(e);
        }
    }

    @Override
    public BiStream create(final Path path) {
        this.failsafe.checkArgument().isNotNull(logger, path, "path");
        return createWithAbsolutePath(path);
    }

    private BiStream createWithAbsolutePath(final Path absolutePath) {
        return new FileBiStream(this.failsafe, absolutePath);
    }

    @Override
    public void close() throws IOException {
        // give back the files that are no longer needed.
        while (!this.tempFiles.isEmpty()) {
            this.temporaryFilePool.offer(this.tempFiles.poll());
        }
    }

    @Override
    public String toString() {
        return "FileBiStreamFactoryImpl{" +
                "failsafe=" + this.failsafe +
                ", temporaryFilePool=" + this.temporaryFilePool +
                ", tempFiles=" + this.tempFiles +
                "}";
    }
}
