package com.android.systemui;

import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.BootCompleteCache;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: BootCompleteCacheImpl.kt */
public final class BootCompleteCacheImpl implements BootCompleteCache, Dumpable {
    private final AtomicBoolean bootComplete = new AtomicBoolean(false);
    @GuardedBy({"listeners"})
    private final List<WeakReference<BootCompleteCache.BootCompleteListener>> listeners = new ArrayList();

    public BootCompleteCacheImpl(@NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        dumpManager.registerDumpable("BootCompleteCacheImpl", this);
    }

    @Override // com.android.systemui.BootCompleteCache
    public boolean isBootComplete() {
        return this.bootComplete.get();
    }

    public final void setBootComplete() {
        if (this.bootComplete.compareAndSet(false, true)) {
            Log.d("BootCompleteCacheImpl", "Boot complete set");
            synchronized (this.listeners) {
                Iterator<T> it = this.listeners.iterator();
                while (it.hasNext()) {
                    BootCompleteCache.BootCompleteListener bootCompleteListener = (BootCompleteCache.BootCompleteListener) ((WeakReference) it.next()).get();
                    if (bootCompleteListener != null) {
                        bootCompleteListener.onBootComplete();
                    }
                }
                this.listeners.clear();
                Unit unit = Unit.INSTANCE;
            }
        }
    }

    @Override // com.android.systemui.BootCompleteCache
    public boolean addListener(@NotNull BootCompleteCache.BootCompleteListener bootCompleteListener) {
        Intrinsics.checkParameterIsNotNull(bootCompleteListener, "listener");
        if (this.bootComplete.get()) {
            return true;
        }
        synchronized (this.listeners) {
            if (this.bootComplete.get()) {
                return true;
            }
            this.listeners.add(new WeakReference<>(bootCompleteListener));
            return false;
        }
    }

    @Override // com.android.systemui.BootCompleteCache
    public void removeListener(@NotNull BootCompleteCache.BootCompleteListener bootCompleteListener) {
        Intrinsics.checkParameterIsNotNull(bootCompleteListener, "listener");
        if (!this.bootComplete.get()) {
            synchronized (this.listeners) {
                this.listeners.removeIf(new Predicate<WeakReference<BootCompleteCache.BootCompleteListener>>(this, bootCompleteListener) { // from class: com.android.systemui.BootCompleteCacheImpl$removeListener$$inlined$synchronized$lambda$1
                    final /* synthetic */ BootCompleteCache.BootCompleteListener $listener$inlined;

                    {
                        this.$listener$inlined = r2;
                    }

                    public final boolean test(@NotNull WeakReference<BootCompleteCache.BootCompleteListener> weakReference) {
                        Intrinsics.checkParameterIsNotNull(weakReference, "it");
                        return weakReference.get() == null || weakReference.get() == this.$listener$inlined;
                    }
                });
                Unit unit = Unit.INSTANCE;
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        printWriter.println("BootCompleteCache state:");
        printWriter.println("  boot complete: " + isBootComplete());
        if (!isBootComplete()) {
            printWriter.println("  listeners:");
            synchronized (this.listeners) {
                Iterator<T> it = this.listeners.iterator();
                while (it.hasNext()) {
                    printWriter.println("    " + ((WeakReference) it.next()));
                }
                Unit unit = Unit.INSTANCE;
            }
        }
    }
}
