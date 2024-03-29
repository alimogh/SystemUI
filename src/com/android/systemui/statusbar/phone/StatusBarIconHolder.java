package com.android.systemui.statusbar.phone;

import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
public class StatusBarIconHolder {
    private StatusBarIcon mIcon;
    private StatusBarSignalPolicy.MobileIconState mMobileState;
    private int mResourceId;
    private int mTag = 0;
    private int mType = 0;
    private boolean mVisible = true;
    private StatusBarSignalPolicy.WifiIconState mWifiState;

    public static StatusBarIconHolder fromIcon(StatusBarIcon statusBarIcon) {
        StatusBarIconHolder statusBarIconHolder = new StatusBarIconHolder();
        statusBarIconHolder.mIcon = statusBarIcon;
        return statusBarIconHolder;
    }

    public static StatusBarIconHolder fromWifiIconState(StatusBarSignalPolicy.WifiIconState wifiIconState) {
        StatusBarIconHolder statusBarIconHolder = new StatusBarIconHolder();
        statusBarIconHolder.mWifiState = wifiIconState;
        statusBarIconHolder.mType = 1;
        return statusBarIconHolder;
    }

    public static StatusBarIconHolder fromMobileIconState(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        StatusBarIconHolder statusBarIconHolder = new StatusBarIconHolder();
        statusBarIconHolder.mMobileState = mobileIconState;
        statusBarIconHolder.mType = 2;
        statusBarIconHolder.mTag = mobileIconState.subId;
        return statusBarIconHolder;
    }

    public static StatusBarIconHolder fromOPCustView(int i, boolean z) {
        StatusBarIconHolder statusBarIconHolder = new StatusBarIconHolder();
        statusBarIconHolder.mType = 3;
        statusBarIconHolder.mResourceId = i;
        statusBarIconHolder.mVisible = z;
        return statusBarIconHolder;
    }

    public int getType() {
        return this.mType;
    }

    public StatusBarIcon getIcon() {
        return this.mIcon;
    }

    public StatusBarSignalPolicy.WifiIconState getWifiState() {
        return this.mWifiState;
    }

    public void setWifiState(StatusBarSignalPolicy.WifiIconState wifiIconState) {
        this.mWifiState = wifiIconState;
    }

    public StatusBarSignalPolicy.MobileIconState getMobileState() {
        return this.mMobileState;
    }

    public void setMobileState(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        this.mMobileState = mobileIconState;
    }

    public int getResourceId() {
        return this.mResourceId;
    }

    public boolean isVisible() {
        int i = this.mType;
        if (i == 0) {
            return this.mIcon.visible;
        }
        if (i == 1) {
            return this.mWifiState.visible;
        }
        if (i == 2) {
            return this.mMobileState.visible;
        }
        if (i != 3) {
            return true;
        }
        return this.mVisible;
    }

    public void setVisible(boolean z) {
        if (isVisible() != z) {
            int i = this.mType;
            if (i == 0) {
                this.mIcon.visible = z;
            } else if (i == 1) {
                this.mWifiState.visible = z;
            } else if (i == 2) {
                this.mMobileState.visible = z;
            } else if (i == 3) {
                this.mVisible = z;
            }
        }
    }

    public int getTag() {
        return this.mTag;
    }
}
