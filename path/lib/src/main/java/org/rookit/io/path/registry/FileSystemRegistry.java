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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import org.rookit.utils.registry.Registry;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.util.List;

final class FileSystemRegistry implements Registry<URI, FileSystem> {

    private final List<FileSystem> fileSystems;

    FileSystemRegistry() {
        // TODO this is not thread safe
        this.fileSystems = Lists.newArrayList();
    }

    @Override
    public Maybe<FileSystem> get(final URI key) {
        return fetch(key).toMaybe();
    }

    @Override
    public Single<FileSystem> fetch(final URI key) {
        return Single.just(key)
                .map(this::createFileSystem)
                .onErrorResumeNext(throwable -> handleCreationError(key, throwable));
    }

    private SingleSource<FileSystem> handleCreationError(final URI key, final Throwable throwable) {
        if (throwable instanceof FileSystemAlreadyExistsException) {
            return Single.fromCallable(() -> FileSystems.getFileSystem(key));
        }
        return Single.error(throwable);
    }

    private FileSystem createFileSystem(final URI key) throws IOException {
        final FileSystem fileSystem = FileSystems.newFileSystem(key, ImmutableMap.of());
        this.fileSystems.add(fileSystem);

        return fileSystem;
    }

    @Override
    public void close() throws IOException {
        // TODO this needs some care
        for (final FileSystem fileSystem : this.fileSystems) {
            fileSystem.close();
        }
    }

    @Override
    public String toString() {
        return "FileSystemRegistry{" +
                "fileSystems=" + this.fileSystems +
                "}";
    }
}
