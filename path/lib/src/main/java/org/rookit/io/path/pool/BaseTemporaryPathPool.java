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
package org.rookit.io.path.pool;

import com.google.common.collect.Maps;
import org.rookit.io.path.PathConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Queue;

import static org.apache.commons.lang3.StringUtils.EMPTY;

final class BaseTemporaryPathPool implements TemporaryPathPool {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(BaseTemporaryPathPool.class);

    private final PathConfig config;
    private final Queue<Path> tempFiles;

    BaseTemporaryPathPool(final PathConfig config, final Queue<Path> tempFiles) {
        this.config = config;
        //noinspection AssignmentOrReturnOfFieldWithMutableType -> intentional
        this.tempFiles = tempFiles;
    }

    @Override
    public Path pollFile() throws IOException {
        return poll(Files.createTempFile(this.config.temporaryDirectory(), EMPTY, ".temp"));
    }

    @Override
    public Path pollDirectory() throws IOException {
        return poll(Files.createTempDirectory(this.config.temporaryDirectory(), EMPTY));
    }

    private Path poll(final Path path) {
        // TODO should take into consideration whether the file was added or not to the queue.
        this.tempFiles.offer(path);
        return path;
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

        // TODO make me parallel
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
        return "BaseTemporaryPathPool{" +
                "config=" + this.config +
                ", tempFiles=" + this.tempFiles +
                "}";
    }
}
