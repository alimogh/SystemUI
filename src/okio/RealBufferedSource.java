package okio;

import java.io.IOException;
import java.nio.ByteBuffer;
/* access modifiers changed from: package-private */
public final class RealBufferedSource implements BufferedSource {
    public final Buffer buffer = new Buffer();
    boolean closed;
    public final Source source;

    RealBufferedSource(Source source) {
        if (source != null) {
            this.source = source;
            return;
        }
        throw new NullPointerException("source == null");
    }

    @Override // okio.BufferedSource
    public Buffer buffer() {
        return this.buffer;
    }

    @Override // okio.Source
    public long read(Buffer buffer, long j) throws IOException {
        if (buffer == null) {
            throw new IllegalArgumentException("sink == null");
        } else if (j < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + j);
        } else if (!this.closed) {
            Buffer buffer2 = this.buffer;
            if (buffer2.size == 0 && this.source.read(buffer2, 8192) == -1) {
                return -1;
            }
            return this.buffer.read(buffer, Math.min(j, this.buffer.size));
        } else {
            throw new IllegalStateException("closed");
        }
    }

    @Override // okio.BufferedSource
    public boolean request(long j) throws IOException {
        Buffer buffer;
        if (j < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + j);
        } else if (!this.closed) {
            do {
                buffer = this.buffer;
                if (buffer.size >= j) {
                    return true;
                }
            } while (this.source.read(buffer, 8192) != -1);
            return false;
        } else {
            throw new IllegalStateException("closed");
        }
    }

    @Override // okio.BufferedSource
    public int select(Options options) throws IOException {
        if (!this.closed) {
            do {
                int selectPrefix = this.buffer.selectPrefix(options, true);
                if (selectPrefix == -1) {
                    return -1;
                }
                if (selectPrefix != -2) {
                    this.buffer.skip((long) options.byteStrings[selectPrefix].size());
                    return selectPrefix;
                }
            } while (this.source.read(this.buffer, 8192) != -1);
            return -1;
        }
        throw new IllegalStateException("closed");
    }

    @Override // java.nio.channels.ReadableByteChannel
    public int read(ByteBuffer byteBuffer) throws IOException {
        Buffer buffer = this.buffer;
        if (buffer.size == 0 && this.source.read(buffer, 8192) == -1) {
            return -1;
        }
        return this.buffer.read(byteBuffer);
    }

    @Override // okio.BufferedSource
    public long indexOf(ByteString byteString) throws IOException {
        return indexOf(byteString, 0);
    }

    public long indexOf(ByteString byteString, long j) throws IOException {
        if (!this.closed) {
            while (true) {
                long indexOf = this.buffer.indexOf(byteString, j);
                if (indexOf != -1) {
                    return indexOf;
                }
                Buffer buffer = this.buffer;
                long j2 = buffer.size;
                if (this.source.read(buffer, 8192) == -1) {
                    return -1;
                }
                j = Math.max(j, (j2 - ((long) byteString.size())) + 1);
            }
        } else {
            throw new IllegalStateException("closed");
        }
    }

    @Override // okio.BufferedSource
    public long indexOfElement(ByteString byteString) throws IOException {
        return indexOfElement(byteString, 0);
    }

    public long indexOfElement(ByteString byteString, long j) throws IOException {
        if (!this.closed) {
            while (true) {
                long indexOfElement = this.buffer.indexOfElement(byteString, j);
                if (indexOfElement != -1) {
                    return indexOfElement;
                }
                Buffer buffer = this.buffer;
                long j2 = buffer.size;
                if (this.source.read(buffer, 8192) == -1) {
                    return -1;
                }
                j = Math.max(j, j2);
            }
        } else {
            throw new IllegalStateException("closed");
        }
    }

    @Override // java.nio.channels.Channel
    public boolean isOpen() {
        return !this.closed;
    }

    @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable, java.nio.channels.Channel
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.source.close();
            this.buffer.clear();
        }
    }

    @Override // java.lang.Object
    public String toString() {
        return "buffer(" + this.source + ")";
    }
}
