package com.android.systemui.statusbar.policy;
public interface RotationLockController extends Object {

    public interface RotationLockControllerCallback {
        void onRotationLockStateChanged(boolean z, boolean z2);
    }

    int getRotationLockOrientation();

    boolean isRotationLocked();

    void setRotationLocked(boolean z);

    void setRotationLockedAtAngle(boolean z, int i);
}
