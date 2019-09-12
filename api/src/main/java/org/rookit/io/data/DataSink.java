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
import java.io.OutputStream;
import java.io.Writer;

public interface DataSink {

    void clear() throws IOException;

    boolean isEmpty();

    OutputStream writeTo() throws IOException;

    default Completable writeTo(final Consumer<OutputStream> consumer) {

        return Completable.fromAction(() -> {
            try (final OutputStream outputStream = writeTo()) {
                consumer.accept(outputStream);
            }
        });
    }

    Completable writeToWithWriter(Consumer<Writer> consumer);

    default <T> Single<T> writeTo(final Function<OutputStream, T> function) {
        return Single.fromCallable(() -> {
            try (final OutputStream outputStream = writeTo()) {
                return function.apply(outputStream);
            }
        });
    }

    <T> Single<T> writeToWithWriter(Function<Writer, T> function);

    Completable copyFrom(final DataSource dataBucket);

}
