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
package org.rookit.io.path.registry;

import io.reactivex.Maybe;
import io.reactivex.Single;
import org.rookit.io.data.DataBucketFactory;
import org.rookit.io.data.DataSource;
import org.rookit.utils.registry.Registry;

import java.nio.file.Files;
import java.nio.file.Path;

final class PathRegistry implements Registry<String, DataSource> {

    private final Path directory;
    private final DataBucketFactory<Path> bucketFactory;

    PathRegistry(final Path directory, final DataBucketFactory<Path> bucketFactory) {
        this.directory = directory;
        this.bucketFactory = bucketFactory;
    }

    @Override
    public Maybe<DataSource> get(final String key) {
        return Maybe.just(this.directory.resolve(key))
                .filter(Files::exists)
                .map(this.bucketFactory::create);
    }

    @Override
    public Single<DataSource> fetch(final String key) {
        return Single.just(this.directory.resolve(key))
                .map(this.bucketFactory::create);
    }

    @Override
    public void close() {
        // nothing to be closed
    }

    @Override
    public String toString() {
        return "PathRegistry{" +
                "directory=" + this.directory +
                ", bucketFactory=" + this.bucketFactory +
                "}";
    }
}
