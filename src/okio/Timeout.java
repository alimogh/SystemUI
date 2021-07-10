package okio;

import java.io.IOException;
import java.io.InterruptedIOException;
public class Timeout {
    private long deadlineNanoTime;
    private boolean hasDeadline;

    public void throwIfReached() throws IOException {
        if (Thread.interrupted()) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("interrupted");
        } else if (this.hasDeadline && this.deadlineNanoTime - System.nanoTime() <= 0) {
            throw new InterruptedIOException("deadline reached");
        }
    }
}
