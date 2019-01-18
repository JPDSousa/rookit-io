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

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Queue;

import static org.apache.commons.lang3.StringUtils.EMPTY;

final class PathManagerImpl implements PathManager, Closeable {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(PathManagerImpl.class);

    private final PathConfig config;
    private final Queue<Path> tempFiles;

    PathManagerImpl(final PathConfig config) {
        this.config = config;
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
            return createFromURI(uri);
        }
    }

    private Path createFromURI(final URI uri) throws IOException {
        final Path tempFile = Files.createTempFile(this.config.temporaryDirectory(), EMPTY, EMPTY);
        Files.copy(uri.toURL().openStream(), tempFile);
        logger.debug("Dumped URI '{}' to path '{}'.", uri, tempFile);
        this.tempFiles.offer(tempFile);
        return tempFile;
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
        return "PathManagerImpl{" +
                "config=" + this.config +
                ", tempFiles=" + this.tempFiles +
                "}";
    }
}
