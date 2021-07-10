package com.oneplus.systemui.statusbar;

import android.service.notification.StatusBarNotification;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
public class OpNotificationLockscreenUserManagerImpl {
    protected boolean mIsSecure;

    /* access modifiers changed from: protected */
    public boolean shouldShowOnKeyguardInternal(NotificationEntryManager notificationEntryManager, StatusBarNotification statusBarNotification, boolean z, boolean z2) {
        return z && z2 && notificationEntryManager.shouldShowOnKeyguard(statusBarNotification);
    }

    public boolean isSecure() {
        return this.mIsSecure;
    }

    /* access modifiers changed from: protected */
    public void setSecure(LockPatternUtils lockPatternUtils, int i) {
        this.mIsSecure = lockPatternUtils.getKeyguardStoredPasswordQuality(i) != 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("  mIsSecure=" + this.mIsSecure);
        printWriter.println();
    }
}
