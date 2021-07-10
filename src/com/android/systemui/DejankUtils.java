package com.android.systemui;

import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.view.Choreographer;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.utils.ThreadUtils;
import com.android.systemui.util.Assert;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;
import java.util.function.Supplier;
public class DejankUtils {
    public static final boolean STRICT_MODE_ENABLED = (Build.IS_ENG || SystemProperties.getBoolean("persist.sysui.strictmode", false));
    private static final Runnable sAnimationCallbackRunnable = $$Lambda$DejankUtils$SyBRIrRRZtwJZ1Fy9Pe5WnzuioU.INSTANCE;
    private static Stack<String> sBlockingIpcs = new Stack<>();
    private static final Choreographer sChoreographer = Choreographer.getInstance();
    private static final Handler sHandler = new Handler();
    private static boolean sImmediate;
    private static final Object sLock = new Object();
    private static final ArrayList<Runnable> sPendingRunnables = new ArrayList<>();
    private static final Binder.ProxyTransactListener sProxy = new Binder.ProxyTransactListener() { // from class: com.android.systemui.DejankUtils.1
        public void onTransactEnded(Object obj) {
        }

        public Object onTransactStarted(IBinder iBinder, int i) {
            return null;
        }

        public Object onTransactStarted(IBinder iBinder, int i, int i2) {
            synchronized (DejankUtils.sLock) {
                if ((i2 & 1) != 1) {
                    if (!DejankUtils.sBlockingIpcs.empty() && ThreadUtils.isMainThread()) {
                        if (DejankUtils.sTemporarilyIgnoreStrictMode) {
                        }
                    }
                }
                return null;
            }
            try {
                String interfaceDescriptor = iBinder.getInterfaceDescriptor();
                synchronized (DejankUtils.sLock) {
                    if (DejankUtils.sWhitelistedFrameworkClasses.contains(interfaceDescriptor)) {
                        return null;
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            StrictMode.noteSlowCall("IPC detected on critical path: " + ((String) DejankUtils.sBlockingIpcs.peek()));
            return null;
        }
    };
    private static boolean sTemporarilyIgnoreStrictMode = false;
    private static final HashSet<String> sWhitelistedFrameworkClasses = new HashSet<>();

    static {
        if (STRICT_MODE_ENABLED) {
            sWhitelistedFrameworkClasses.add("android.view.IWindowSession");
            sWhitelistedFrameworkClasses.add("com.android.internal.policy.IKeyguardStateCallback");
            sWhitelistedFrameworkClasses.add("android.os.IPowerManager");
            sWhitelistedFrameworkClasses.add("com.android.internal.statusbar.IStatusBarService");
            Binder.setProxyTransactListener(sProxy);
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectCustomSlowCalls().penaltyFlashScreen().penaltyLog().build());
        }
    }

    static /* synthetic */ void lambda$static$0() {
        for (int i = 0; i < sPendingRunnables.size(); i++) {
            sHandler.post(sPendingRunnables.get(i));
        }
        sPendingRunnables.clear();
    }

    public static void startDetectingBlockingIpcs(String str) {
        if (STRICT_MODE_ENABLED) {
            synchronized (sLock) {
                sBlockingIpcs.push(str);
            }
        }
    }

    public static void stopDetectingBlockingIpcs(String str) {
        if (STRICT_MODE_ENABLED) {
            synchronized (sLock) {
                sBlockingIpcs.remove(str);
            }
        }
    }

    public static void whitelistIpcs(Runnable runnable) {
        if (!STRICT_MODE_ENABLED || sTemporarilyIgnoreStrictMode) {
            runnable.run();
            return;
        }
        synchronized (sLock) {
            sTemporarilyIgnoreStrictMode = true;
        }
        try {
            runnable.run();
            synchronized (sLock) {
                sTemporarilyIgnoreStrictMode = false;
            }
        } catch (Throwable th) {
            synchronized (sLock) {
                sTemporarilyIgnoreStrictMode = false;
                throw th;
            }
        }
    }

    public static <T> T whitelistIpcs(Supplier<T> supplier) {
        if (!STRICT_MODE_ENABLED || sTemporarilyIgnoreStrictMode) {
            return supplier.get();
        }
        synchronized (sLock) {
            sTemporarilyIgnoreStrictMode = true;
        }
        try {
            T t = supplier.get();
            synchronized (sLock) {
                sTemporarilyIgnoreStrictMode = false;
            }
            return t;
        } catch (Throwable th) {
            synchronized (sLock) {
                sTemporarilyIgnoreStrictMode = false;
                throw th;
            }
        }
    }

    public static void postAfterTraversal(Runnable runnable) {
        if (sImmediate) {
            runnable.run();
            return;
        }
        Assert.isMainThread();
        sPendingRunnables.add(runnable);
        postAnimationCallback();
    }

    public static void removeCallbacks(Runnable runnable) {
        Assert.isMainThread();
        sPendingRunnables.remove(runnable);
        sHandler.removeCallbacks(runnable);
    }

    private static void postAnimationCallback() {
        sChoreographer.postCallback(1, sAnimationCallbackRunnable, null);
    }

    @VisibleForTesting
    public static void setImmediate(boolean z) {
        sImmediate = z;
    }
}
