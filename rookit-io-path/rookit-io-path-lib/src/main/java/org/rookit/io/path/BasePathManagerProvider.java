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

import com.google.common.io.Closer;
import com.google.inject.Inject;
import com.google.inject.Provider;

public final class BasePathManagerProvider implements Provider<PathManager> {

    public static PathManager create(final Closer closer, final PathConfig pathConfig) {
        return new BasePathManagerProvider(closer, pathConfig).get();
    }

    @SuppressWarnings("FieldNotUsedInToString") // default toString
    private final Closer closer;
    private final PathConfig pathConfig;

    @Inject
    private BasePathManagerProvider(final Closer closer, final PathConfig pathConfig) {
        this.closer = closer;
        this.pathConfig = pathConfig;
    }

    @Override
    public PathManager get() {
        return this.closer.register(new PathManagerImpl(this.pathConfig));
    }

    @Override
    public String toString() {
        return "BasePathManagerProvider{" +
                ", pathConfig=" + this.pathConfig +
                "}";
    }
}
