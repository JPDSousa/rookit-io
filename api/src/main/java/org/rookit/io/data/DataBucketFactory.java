package org.rookit.io.data;

import java.io.Closeable;

public interface DataBucketFactory<E> extends Closeable {

    DataBucket create(E source);

}
