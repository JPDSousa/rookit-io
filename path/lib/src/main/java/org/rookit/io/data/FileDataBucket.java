
package org.rookit.io.data;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import org.rookit.failsafe.Failsafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

final class FileDataBucket implements DataBucket {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileDataBucket.class);

    private final Failsafe failsafe;
    private final Path path;
    private final Charset charset;

    FileDataBucket(final Failsafe failsafe, final Path path, final Charset charset) {
        this.failsafe = failsafe;
        this.charset = charset;
        logger.trace("New {} in path {}", FileDataBucket.class.getName(), path);
        this.path = path;
    }

    @Override
    public void clear() throws IOException {
        Files.deleteIfExists(this.path);
    }

    @Override
    public boolean isEmpty() {
        return Files.notExists(this.path)
                || !canRead();
    }
    
    @SuppressWarnings("boxing")
    private boolean canRead() {
        try (final InputStream input = Files.newInputStream(this.path, StandardOpenOption.CREATE,
                StandardOpenOption.READ)) {
            return input.read() != -1;
        } catch (final IOException e) {
            final String errMsg = String.format("Attempting to read path '%s' resulted in an exception.", this.path);
            logger.warn(errMsg, e);
            return false;
        }
    }

    @Override
    public InputStream readFrom() throws IOException {
        if (Files.notExists(this.path)) {
            throw new FileNotFoundException(this.path.toString());
        }
        return Files.newInputStream(this.path, StandardOpenOption.READ);
    }

    private Reader toReader(final InputStream stream) {
        return new InputStreamReader(stream, this.charset);
    }

    private Writer toWriter(final OutputStream stream) {
        return new OutputStreamWriter(stream, this.charset);
    }

    @Override
    public <T> Single<T> readFromWithReader(final Function<Reader, T> function) {
        return readFrom(inputStream -> {
            return function.apply(toReader(inputStream));
        });
    }

    @Override
    public Completable readFromWithReader(final Consumer<Reader> consumer) {
        return readFrom(stream -> {
            consumer.accept(toReader(stream));
        });
    }

    @Override
    public OutputStream writeTo() {
        try {
            return Files.newOutputStream(this.path);
        } catch (final IOException e) {
            return this.failsafe.handleException().inputOutputException(e);
        }
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
            logger.trace("Attempting to copy path {} to itself. Skipping.", this.path);
            return Completable.complete();
        } else {
            return dataBucket.readFrom(reader -> {
                Files.copy(reader, this.path);
            });
        }
    }

    @Override
    public String toString() {
        return "FileDataBucket{" +
                "failsafe=" + this.failsafe +
                ", path=" + this.path +
                ", charset=" + this.charset +
                "}";
    }
}
