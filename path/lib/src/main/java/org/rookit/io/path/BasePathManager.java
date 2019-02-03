/*******************************************************************************
 * Copyright (C) 2018 Joao Sousa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.rookit.io.path;

import com.google.common.collect.Queues;
import com.google.inject.Inject;
import org.rookit.io.file.TemporaryFilePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Queue;

public final class BasePathManager implements PathManager {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(BasePathManager.class);

    public static PathManager create(final PathConfig config, final TemporaryFilePool temporaryFilePool) {
        return new BasePathManager(config, temporaryFilePool);
    }

    private final PathConfig config;
    private final Queue<Path> tempFiles;
    private final TemporaryFilePool temporaryFilePool;

    @Inject
    private BasePathManager(final PathConfig config, final TemporaryFilePool temporaryFilePool) {
        this.config = config;
        this.temporaryFilePool = temporaryFilePool;
        this.tempFiles = Queues.newArrayDeque();
    }

    @Override
    public Path handleURI(final URI uri) throws IOException {
        try {
            final Path path = Paths.get(uri);
            if (this.config.failIfAbsent() && Files.notExists(path)) {
                throw new FileNotFoundException(path.toString());
            }
            return path;
        } catch (final FileSystemNotFoundException e) {
            logger.debug("Unsupported Filesystem for URI: {}.", uri);
            return createFromURI(uri);
        }
    }

    private Path createFromURI(final URI uri) throws IOException {
        final Path tempFile = this.temporaryFilePool.poll();
        Files.copy(uri.toURL().openStream(), tempFile);
        logger.debug("Dumped URI '{}' to path '{}'.", uri, tempFile);
        this.tempFiles.offer(tempFile);
        return tempFile;
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
        return "BasePathManager{" +
                "config=" + this.config +
                ", tempFiles=" + this.tempFiles +
                ", temporaryFilePool=" + this.temporaryFilePool +
                "}";
    }
}
