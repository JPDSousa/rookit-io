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
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import org.rookit.io.path.PathConfig;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

final class BaseTemporaryPathPoolFactory implements TemporaryPathPoolFactory, Closeable {

    private final Map<PathConfig, TemporaryPathPool> pools;

    @Inject
    private BaseTemporaryPathPoolFactory() {
        this.pools = Maps.newHashMap();
    }

    @Override
    public TemporaryPathPool create(final PathConfig config) throws IOException {
        final Path path = config.temporaryDirectory();
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }

        return this.pools.computeIfAbsent(config,
                key -> new BaseTemporaryPathPool(config, Queues.newLinkedBlockingDeque()));
    }

    @Override
    public String toString() {
        return "BaseTemporaryPathPoolFactory{" +
                "pools=" + this.pools +
                "}";
    }

    @Override
    public void close() throws IOException {
        // TODO this needs more care
        for (final TemporaryPathPool temporaryPathPool : this.pools.values()) {
            temporaryPathPool.close();
        }
    }
}
