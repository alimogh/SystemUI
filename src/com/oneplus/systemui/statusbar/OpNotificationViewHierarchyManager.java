package com.oneplus.systemui.statusbar;
public class OpNotificationViewHierarchyManager {
    private boolean mIsAnyNotificationLocked = false;

    public boolean isAnyNotificationLocked() {
        return this.mIsAnyNotificationLocked;
    }

    /* access modifiers changed from: protected */
    public void setAnyNotificationLocked(boolean z) {
        this.mIsAnyNotificationLocked = z;
    }
}
