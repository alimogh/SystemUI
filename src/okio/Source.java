package okio;

import java.io.Closeable;
import java.io.IOException;
public interface Source extends Closeable {
    @Override // java.io.Closeable, java.lang.AutoCloseable, java.nio.channels.Channel
    void close() throws IOException;

    long read(Buffer buffer, long j) throws IOException;
}
