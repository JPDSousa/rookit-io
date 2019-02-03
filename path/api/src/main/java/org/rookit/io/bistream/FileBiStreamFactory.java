package org.rookit.io.bistream;

import java.io.Closeable;
import java.nio.file.Path;

public interface FileBiStreamFactory extends Closeable {

    BiStream create(Path relativePath);

    BiStream createTemporary();

}
