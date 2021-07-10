package com.android.systemui.appops;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dumpable;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
public class AppOpsControllerImpl implements AppOpsController, AppOpsManager.OnOpActiveChangedInternalListener, AppOpsManager.OnOpNotedListener, Dumpable {
    protected static final int[] OPS = {26, 24, 27, 0, 1};
    @GuardedBy({"mActiveItems"})
    private final List<AppOpItem> mActiveItems = new ArrayList();
    private final AppOpsManager mAppOps;
    private H mBGHandler;
    private final ArrayMap<Integer, Set<AppOpsController.Callback>> mCallbacksByCode = new ArrayMap<>();
    private boolean mListening;
    @GuardedBy({"mNotedItems"})
    private final List<AppOpItem> mNotedItems = new ArrayList();

    public AppOpsControllerImpl(Context context, Looper looper, DumpManager dumpManager) {
        int[] iArr = OPS;
        new ArrayList();
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mBGHandler = new H(looper);
        for (int i : iArr) {
            this.mCallbacksByCode.put(Integer.valueOf(i), new ArraySet());
        }
        dumpManager.registerDumpable("AppOpsControllerImpl", this);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setBGHandler(H h) {
        this.mBGHandler = h;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: com.android.systemui.appops.AppOpsControllerImpl */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setListening(boolean z) {
        int[] iArr = OPS;
        this.mListening = z;
        if (z) {
            this.mAppOps.startWatchingActive(iArr, this);
            this.mAppOps.startWatchingNoted(iArr, this);
            return;
        }
        this.mAppOps.stopWatchingActive(this);
        this.mAppOps.stopWatchingNoted(this);
        this.mBGHandler.removeCallbacksAndMessages(null);
        synchronized (this.mActiveItems) {
            this.mActiveItems.clear();
        }
        synchronized (this.mNotedItems) {
            this.mNotedItems.clear();
        }
    }

    private AppOpItem getAppOpItemLocked(List<AppOpItem> list, int i, int i2, String str) {
        int size = list.size();
        for (int i3 = 0; i3 < size; i3++) {
            AppOpItem appOpItem = list.get(i3);
            if (appOpItem.getCode() == i && appOpItem.getUid() == i2 && appOpItem.getPackageName().equals(str)) {
                return appOpItem;
            }
        }
        return null;
    }

    private boolean updateActives(int i, int i2, String str, boolean z) {
        synchronized (this.mActiveItems) {
            AppOpItem appOpItemLocked = getAppOpItemLocked(this.mActiveItems, i, i2, str);
            if (appOpItemLocked == null && z) {
                this.mActiveItems.add(new AppOpItem(i, i2, str, System.currentTimeMillis()));
                return true;
            } else if (appOpItemLocked == null || z) {
                return false;
            } else {
                this.mActiveItems.remove(appOpItemLocked);
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeNoted(int i, int i2, String str) {
        boolean z;
        synchronized (this.mNotedItems) {
            AppOpItem appOpItemLocked = getAppOpItemLocked(this.mNotedItems, i, i2, str);
            if (appOpItemLocked != null) {
                this.mNotedItems.remove(appOpItemLocked);
            } else {
                return;
            }
        }
        synchronized (this.mActiveItems) {
            z = getAppOpItemLocked(this.mActiveItems, i, i2, str) != null;
        }
        if (!z) {
            lambda$onOpActiveChanged$0(i, i2, str, false);
        }
    }

    private boolean addNoted(int i, int i2, String str) {
        AppOpItem appOpItemLocked;
        boolean z;
        synchronized (this.mNotedItems) {
            appOpItemLocked = getAppOpItemLocked(this.mNotedItems, i, i2, str);
            if (appOpItemLocked == null) {
                appOpItemLocked = new AppOpItem(i, i2, str, System.currentTimeMillis());
                this.mNotedItems.add(appOpItemLocked);
                z = true;
            } else {
                z = false;
            }
        }
        this.mBGHandler.removeCallbacksAndMessages(appOpItemLocked);
        this.mBGHandler.scheduleRemoval(appOpItemLocked, 5000);
        return z;
    }

    public void onOpActiveChanged(int i, int i2, String str, boolean z) {
        boolean z2;
        if (updateActives(i, i2, str, z)) {
            synchronized (this.mNotedItems) {
                z2 = getAppOpItemLocked(this.mNotedItems, i, i2, str) != null;
            }
            if (!z2) {
                this.mBGHandler.post(new Runnable(i, i2, str, z) { // from class: com.android.systemui.appops.-$$Lambda$AppOpsControllerImpl$ytWudla0eUXQNol33KSx7VyQvYM
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ int f$2;
                    public final /* synthetic */ String f$3;
                    public final /* synthetic */ boolean f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        AppOpsControllerImpl.this.lambda$onOpActiveChanged$0$AppOpsControllerImpl(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                });
            }
        }
    }

    public void onOpNoted(int i, int i2, String str, int i3) {
        boolean z;
        if (i3 == 0 && addNoted(i, i2, str)) {
            synchronized (this.mActiveItems) {
                z = getAppOpItemLocked(this.mActiveItems, i, i2, str) != null;
            }
            if (!z) {
                this.mBGHandler.post(new Runnable(i, i2, str) { // from class: com.android.systemui.appops.-$$Lambda$AppOpsControllerImpl$Ik-chvj1nqb8W_dVPetwy70ZXqg
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ int f$2;
                    public final /* synthetic */ String f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        AppOpsControllerImpl.this.lambda$onOpNoted$1$AppOpsControllerImpl(this.f$1, this.f$2, this.f$3);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onOpNoted$1 */
    public /* synthetic */ void lambda$onOpNoted$1$AppOpsControllerImpl(int i, int i2, String str) {
        lambda$onOpActiveChanged$0(i, i2, str, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: notifySuscribers */
    public void lambda$onOpActiveChanged$0(int i, int i2, String str, boolean z) {
        if (this.mCallbacksByCode.containsKey(Integer.valueOf(i))) {
            for (AppOpsController.Callback callback : this.mCallbacksByCode.get(Integer.valueOf(i))) {
                callback.onActiveStateChanged(i, i2, str, z);
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("AppOpsController state:");
        printWriter.println("  Listening: " + this.mListening);
        printWriter.println("  Active Items:");
        for (int i = 0; i < this.mActiveItems.size(); i++) {
            printWriter.print("    ");
            printWriter.println(this.mActiveItems.get(i).toString());
        }
        printWriter.println("  Noted Items:");
        for (int i2 = 0; i2 < this.mNotedItems.size(); i2++) {
            printWriter.print("    ");
            printWriter.println(this.mNotedItems.get(i2).toString());
        }
    }

    /* access modifiers changed from: protected */
    public class H extends Handler {
        H(Looper looper) {
            super(looper);
        }

        public void scheduleRemoval(final AppOpItem appOpItem, long j) {
            removeCallbacksAndMessages(appOpItem);
            postDelayed(new Runnable() { // from class: com.android.systemui.appops.AppOpsControllerImpl.H.1
                @Override // java.lang.Runnable
                public void run() {
                    AppOpsControllerImpl.this.removeNoted(appOpItem.getCode(), appOpItem.getUid(), appOpItem.getPackageName());
                }
            }, appOpItem, j);
        }
    }
}
