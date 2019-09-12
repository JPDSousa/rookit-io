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

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import org.apache.commons.io.IOUtils;
import org.rookit.io.data.DataBucket;
import org.rookit.io.data.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

final class URLDataBucket implements DataBucket {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(URLDataBucket.class);

    private final URL url;
    private final Charset charset;

    URLDataBucket(final URL url, final Charset charset) {
        this.url = url;
        this.charset = charset;
    }

    @Override
    public void clear() {
        // TODO how to we tackle this??
        logger.warn("Cannot clear a URL referenced resource.");
    }

    @Override
    public boolean isEmpty() {
        try (final InputStream read = readFrom()) {
            return read.read() != -1;
        } catch (final IOException e) {
            final String errMsg = String.format("Error while trying to read from URL '%s'.", this.url);
            logger.warn(errMsg, e);
            return false;
        }
    }

    @Override
    public OutputStream writeTo() throws IOException {
        final URLConnection connection = this.url.openConnection();
        connection.setDoOutput(true);
        return connection.getOutputStream();
    }

    private Reader toReader(final InputStream stream) {
        return new InputStreamReader(stream, this.charset);
    }

    private Writer toWriter(final OutputStream stream) {
        return new OutputStreamWriter(stream, this.charset);
    }

    @Override
    public Completable writeToWithWriter(final Consumer<Writer> consumer) {
        return writeTo(stream -> {
            consumer.accept(toWriter(stream));
        });
    }

    @Override
    public <T> Single<T> writeToWithWriter(final Function<Writer, T> function) {
        return writeTo(stream -> {
            return function.apply(toWriter(stream));
        });
    }

    @Override
    public Completable copyFrom(final DataSource dataBucket) {
        if (equals(dataBucket)) {
            logger.trace("Attempting to copy write {} to itself. Skipping.", this.url);
            return Completable.complete();
        } else {
            return dataBucket.readFrom(reader -> {
                writeTo(writer -> {
                    IOUtils.copy(reader, writer);
                    // TODO this might be a problem
                }).blockingAwait();
            });
        }
    }

    @Override
    public InputStream readFrom() throws IOException {
        return this.url.openStream();
    }

    @Override
    public <T> Single<T> readFromWithReader(final Function<Reader, T> function) {
        return readFrom(stream -> {
            return function.apply(toReader(stream));
        });
    }

    @Override
    public Completable readFromWithReader(final Consumer<Reader> consumer) {
        return readFrom(stream -> {
            consumer.accept(toReader(stream));
        });
    }

    @Override
    public String toString() {
        return "URLDataBucket{" +
                "url=" + this.url +
                ", charset=" + this.charset +
                "}";
    }
}
