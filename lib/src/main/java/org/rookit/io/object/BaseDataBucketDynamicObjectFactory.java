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
package org.rookit.io.object;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import io.reactivex.Single;
import org.rookit.io.data.DataSource;
import org.rookit.utils.object.DynamicObject;
import org.rookit.utils.object.DynamicObjectFactory;
import org.rookit.utils.object.MalformedObjectException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;

final class BaseDataBucketDynamicObjectFactory implements DataBucketDynamicObjectFactory {

    private final DynamicObjectFactory delegate;
    private final Charset charset;

    @Inject
    private BaseDataBucketDynamicObjectFactory(final DynamicObjectFactory delegate,
                                               final Charset charset) {
        this.delegate = delegate;
        this.charset = charset;
    }

    @Override
    public Single<DynamicObject> fromDataSource(final DataSource dataBucket) {
        return dataBucket.readFromWithReader(reader -> {
            return this.delegate.fromRawContent(CharStreams.toString(reader));
        });
    }

    @Override
    public Collection<String> supportedTypes() {
        return this.delegate.supportedTypes();
    }

    @Override
    public DynamicObject fromRawContent(final String rawContent) throws MalformedObjectException, IOException {
        return this.delegate.fromRawContent(rawContent);
    }

    @Override
    public String toString() {
        return "BaseDataBucketDynamicObjectFactory{" +
                "delegate=" + this.delegate +
                ", charset=" + this.charset +
                "}";
    }
}
