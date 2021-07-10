package com.oneplus.opzenmode;

import com.android.systemui.statusbar.policy.CallbackController;
public interface OpZenModeController extends CallbackController<Callback> {

    public interface Callback {
        default void onDndChanged(boolean z) {
        }

        default void onThreeKeyStatus(int i) {
        }
    }

    boolean getDndEnable();

    int getThreeKeySatus();

    void setDndEnable(boolean z);
}
