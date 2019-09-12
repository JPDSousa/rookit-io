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
package org.rookit.io.uri;

import com.google.inject.Inject;
import org.rookit.failsafe.Failsafe;
import org.rookit.io.data.DataBucket;
import org.rookit.io.data.DataBucketFactory;
import org.rookit.utils.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

final class URIDataBucketFactory implements DataBucketFactory<URI> {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(URIDataBucketFactory.class);

    private final Failsafe failsafe;
    private final DataBucketFactory<Path> pathDataBucketFactory;
    private final Registry<URI, FileSystem> fileSystemRegistry;

    @Inject
    private URIDataBucketFactory(final Failsafe failsafe,
                                 final DataBucketFactory<Path> pathFactory,
                                 final Registry<URI, FileSystem> fileSystemRegistry) {
        this.failsafe = failsafe;
        this.pathDataBucketFactory = pathFactory;
        this.fileSystemRegistry = fileSystemRegistry;
    }

    @Override
    public DataBucket create(final URI uri) {
        this.failsafe.checkArgument().isNotNull(logger, uri, "uri");
        return this.pathDataBucketFactory.create(createPath(uri));
    }

    private Path createPath(final URI uri) {
        try {
            return Paths.get(uri);
        } catch (final FileSystemNotFoundException e) {
            logger.debug("Unsupported Filesystem for URI: {}.", uri);

            // Fetch the URI from our custom providers
            return this.fileSystemRegistry.fetch(uri)
                    .map(FileSystem::provider)
                    .map(provider -> provider.getPath(uri))
                    .blockingGet();
        }

    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public String toString() {
        return "URIDataBucketFactory{" +
                "failsafe=" + this.failsafe +
                ", pathDataBucketFactory=" + this.pathDataBucketFactory +
                ", fileSystemRegistry=" + this.fileSystemRegistry +
                "}";
    }
}
