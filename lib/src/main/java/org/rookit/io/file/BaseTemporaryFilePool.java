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
package org.rookit.io.file;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import org.rookit.io.path.PathConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Queue;

import static org.apache.commons.lang3.StringUtils.EMPTY;

final class BaseTemporaryFilePool implements TemporaryFilePool {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(BaseTemporaryFilePool.class);

    private final PathConfig config;
    private final Queue<Path> tempFiles;

    @Inject
    BaseTemporaryFilePool(final PathConfig config) {
        this.config = config;
        this.tempFiles = Queues.newArrayDeque();
    }

    @Override
    public Path poll() throws IOException {
        final Path tempFile = Files.createTempFile(this.config.temporaryDirectory(), EMPTY, EMPTY);
        // TODO should take into consideration whether the file was added or not to the queue.
        this.tempFiles.offer(tempFile);
        return tempFile;
    }

    @Override
    public boolean offer(final Path path) throws IOException {
        if (this.tempFiles.remove(path)) {
            Files.deleteIfExists(path);
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        final Map<String, IOException> errors = Maps.newHashMap();
        logger.debug("Closing path manager");

        while (!this.tempFiles.isEmpty()) {
            final Path tempFile = this.tempFiles.remove();
            try {
                logger.debug("Attempting removal of: {}", tempFile);
                Files.deleteIfExists(tempFile);
            } catch (final IOException e) {
                logger.debug("Removal of temporary file '{}' failed due to: {}", tempFile, e);
                errors.put(tempFile.toString(), e);
            }
        }
        if (!errors.isEmpty()) {
            throw new IOException("The following files could not be deleted: " + errors);
        }
    }

    @Override
    public String toString() {
        return "BaseTemporaryFilePool{" +
                "config=" + this.config +
                ", tempFiles=" + this.tempFiles +
                "}";
    }
}
