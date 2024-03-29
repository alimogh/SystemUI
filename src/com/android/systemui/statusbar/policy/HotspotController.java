package com.android.systemui.statusbar.policy;

import com.android.systemui.Dumpable;
public interface HotspotController extends CallbackController<Callback>, Dumpable {
    int getNumConnectedDevices();

    boolean isHotspotEnabled();

    boolean isHotspotSupported();

    boolean isHotspotTransient();

    default boolean isOperatorHotspotDisable() {
        return false;
    }

    default void onBootCompleted() {
    }

    void setHotspotEnabled(boolean z);

    public interface Callback {
        default void onHotspotAvailabilityChanged(boolean z) {
        }

        void onHotspotChanged(boolean z, int i);

        default void onOperatorHotspotChanged(boolean z) {
        }

        default void onHotspotChanged(boolean z, int i, int i2) {
            onHotspotChanged(z, i);
        }
    }
}
