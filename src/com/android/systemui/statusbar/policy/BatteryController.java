package com.android.systemui.statusbar.policy;

import com.android.systemui.DemoMode;
import com.android.systemui.Dumpable;
public interface BatteryController extends DemoMode, Dumpable, CallbackController<BatteryStateChangeCallback> {

    public interface BatteryStateChangeCallback {
        default void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        }

        default void onBatteryPercentShowChange(boolean z) {
        }

        default void onBatteryStyleChanged(int i) {
        }

        default void onFastChargeChanged(int i) {
        }

        default void onInvalidChargeChanged(int i) {
        }

        default void onOptimizatedStatusChange(boolean z) {
        }

        default void onPowerSaveChanged(boolean z) {
        }

        default void onSWarpBatteryLevelChanged(float f, float f2, long j) {
        }

        default void onWirelessWarpChargeChanged(boolean z) {
        }
    }

    public interface EstimateFetchCompletion {
        void onBatteryRemainingEstimateRetrieved(String str);
    }

    default void getEstimatedTimeRemainingString(EstimateFetchCompletion estimateFetchCompletion) {
    }

    default void init() {
    }

    boolean isAodPowerSave();

    default boolean isFastCharging(int i) {
        return false;
    }

    boolean isPowerSave();

    default boolean isWarpCharging(int i) {
        return false;
    }

    void setPowerSaveMode(boolean z);
}
