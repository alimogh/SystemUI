package kotlinx.coroutines.scheduling;
/* compiled from: Tasks.kt */
public final class NanoTimeSource extends TimeSource {
    public static final NanoTimeSource INSTANCE = new NanoTimeSource();

    private NanoTimeSource() {
    }

    @Override // kotlinx.coroutines.scheduling.TimeSource
    public long nanoTime() {
        return System.nanoTime();
    }
}
