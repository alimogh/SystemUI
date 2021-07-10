package com.android.systemui;

import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
public class ForegroundServiceController {
    private final Object mMutex = new Object();
    private final SparseArray<ForegroundServicesUserState> mUserServices = new SparseArray<>();

    interface UserStateUpdateCallback {
        boolean updateUserState(ForegroundServicesUserState foregroundServicesUserState);
    }

    public ForegroundServiceController(NotificationEntryManager notificationEntryManager, AppOpsController appOpsController, Handler handler) {
    }

    public boolean isDisclosureNeededForUser(int i) {
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(i);
            if (foregroundServicesUserState == null) {
                return false;
            }
            return foregroundServicesUserState.isDisclosureNeeded();
        }
    }

    public boolean isSystemAlertWarningNeeded(int i, String str) {
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(i);
            boolean z = false;
            if (foregroundServicesUserState == null) {
                return false;
            }
            if (foregroundServicesUserState.getStandardLayoutKeys(str) == null) {
                z = true;
            }
            return z;
        }
    }

    public ArraySet<Integer> getAppOps(int i, String str) {
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(i);
            if (foregroundServicesUserState == null) {
                return null;
            }
            return foregroundServicesUserState.getFeatures(str);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateUserState(int i, UserStateUpdateCallback userStateUpdateCallback, boolean z) {
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(i);
            if (foregroundServicesUserState == null) {
                if (!z) {
                    return false;
                }
                foregroundServicesUserState = new ForegroundServicesUserState();
                this.mUserServices.put(i, foregroundServicesUserState);
            }
            return userStateUpdateCallback.updateUserState(foregroundServicesUserState);
        }
    }

    public boolean isDisclosureNotification(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getId() == 40 && statusBarNotification.getTag() == null && statusBarNotification.getPackageName().equals("android");
    }

    public boolean isSystemAlertNotification(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getPackageName().equals("android") && statusBarNotification.getTag() != null && statusBarNotification.getTag().contains("AlertWindowNotification");
    }
}
