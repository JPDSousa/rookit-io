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

import com.google.inject.Inject;
import org.rookit.io.data.DataBucketFactory;
import org.rookit.io.data.DataSource;
import org.rookit.io.object.DataBucketDynamicObjectFactory;
import org.rookit.utils.object.DynamicObject;
import org.rookit.utils.registry.BaseRegistries;
import org.rookit.utils.registry.Registry;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;

final class PathRegistriesImpl implements PathRegistries {

    private final DataBucketFactory<Path> bucketFactory;
    private final DataBucketDynamicObjectFactory dynamicObjectFactory;
    private final BaseRegistries registries;

    @Inject
    private PathRegistriesImpl(final DataBucketFactory<Path> bucketFactory,
                               final DataBucketDynamicObjectFactory dynamicObjectFactory,
                               final BaseRegistries registries) {
        this.bucketFactory = bucketFactory;
        this.dynamicObjectFactory = dynamicObjectFactory;
        this.registries = registries;
    }

    @Override
    public Registry<String, DataSource> directoryRegistry(final Path directory) {
        return new PathRegistry(directory, this.bucketFactory);
    }

    @Override
    public Registry<String, DynamicObject> serializedDirectoryRegistry(final Path directory) {
        return this.registries.mapValueRegistry(directoryRegistry(directory),
                this.dynamicObjectFactory::fromDataSource);
    }

    @Override
    public Registry<URI, FileSystem> uriFileSystemRegistry() {
        return new FileSystemRegistry();
    }

    @Override
    public String toString() {
        return "PathRegistriesImpl{" +
                "bucketFactory=" + this.bucketFactory +
                ", dynamicObjectFactory=" + this.dynamicObjectFactory +
                ", registries=" + this.registries +
                "}";
    }
}
