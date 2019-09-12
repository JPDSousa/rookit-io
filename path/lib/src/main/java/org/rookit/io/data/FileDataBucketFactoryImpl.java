
package org.rookit.io.data;

import com.google.common.collect.Queues;
import com.google.inject.Inject;
import org.rookit.failsafe.Failsafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Queue;

@SuppressWarnings("javadoc")
final class FileDataBucketFactoryImpl implements DataBucketFactory<Path> {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileDataBucketFactoryImpl.class);

    private final Failsafe failsafe;
    private final Queue<Path> tempFiles;
    private final Charset charset;

    @Inject
    private FileDataBucketFactoryImpl(final Failsafe failsafe,
                                      final Charset charset) {
        this.failsafe = failsafe;
        this.charset = charset;
        this.tempFiles = Queues.newArrayDeque();
    }

    @Override
    public DataBucket create(final Path path) {
        this.failsafe.checkArgument().isNotNull(logger, path, "path");
        return createWithAbsolutePath(path);
    }

    private DataBucket createWithAbsolutePath(final Path absolutePath) {
        return new FileDataBucket(this.failsafe, absolutePath, this.charset);
    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public String toString() {
        return "FileDataBucketFactoryImpl{" +
                "failsafe=" + this.failsafe +
                ", tempFiles=" + this.tempFiles +
                ", charset=" + this.charset +
                "}";
    }
}
