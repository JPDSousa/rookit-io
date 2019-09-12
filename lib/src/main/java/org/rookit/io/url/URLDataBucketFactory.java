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
package org.rookit.io.url;

import com.google.common.collect.Queues;
import com.google.inject.Inject;
import org.rookit.failsafe.Failsafe;
import org.rookit.io.data.DataBucket;
import org.rookit.io.data.DataBucketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Queue;

final class URLDataBucketFactory implements DataBucketFactory<URL> {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(URLDataBucketFactory.class);

    private final Failsafe failsafe;
    private final DataBucketFactory<URI> uriDataBucketFactory;
    private final Queue<Path> tempFiles;
    private final Charset charset;

    @Inject
    private URLDataBucketFactory(final Failsafe failsafe,
                                 final DataBucketFactory<URI> uriDataBucketFactory,
                                 final Charset charset) {
        this.failsafe = failsafe;
        this.uriDataBucketFactory = uriDataBucketFactory;
        this.charset = charset;
        this.tempFiles = Queues.newLinkedBlockingDeque();
    }

    @Override
    public DataBucket create(final URL url) {
        this.failsafe.checkArgument().isNotNull(logger, url, "url");
        try {
            return this.uriDataBucketFactory.create(url.toURI());
        } catch (final URISyntaxException e) {
            // since the URL protocol has some limitations in regards to our requirements,
            // we use this implementation only as fallback.
            return new URLDataBucket(url, this.charset);
        }
    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public String toString() {
        return "URLDataBucketFactory{" +
                "failsafe=" + this.failsafe +
                ", uriDataBucketFactory=" + this.uriDataBucketFactory +
                ", tempFiles=" + this.tempFiles +
                ", charset=" + this.charset +
                "}";
    }
}
