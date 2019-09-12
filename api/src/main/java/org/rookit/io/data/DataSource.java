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
package org.rookit.io.data;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public interface DataSource {

    @Deprecated
    InputStream readFrom() throws IOException;

    default <T> Single<T> readFrom(final Function<InputStream, T> function) {
        return Single.fromCallable(() -> {
            try (final InputStream inputStream = readFrom()) {
                return function.apply(inputStream);
            }
        });
    }

    <T> Single<T> readFromWithReader(final Function<Reader, T> function);

    default Completable readFrom(final Consumer<InputStream> consumer) {
        return Completable.fromAction(() -> {
            try (final InputStream inputStream = readFrom()) {
                consumer.accept(inputStream);
            }
        });
    }

    Completable readFromWithReader(final Consumer<Reader> consumer);

}
