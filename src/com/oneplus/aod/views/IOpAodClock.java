package com.oneplus.aod.views;

import android.view.ViewGroup;
import com.oneplus.aod.utils.OpAodSettings;
import java.util.Calendar;
public interface IOpAodClock {
    default void alignforBurnIn(int i) {
    }

    default void alignforBurnIn2(ViewGroup viewGroup, int i, int i2) {
    }

    void applyLayoutParams(OpAodSettings.OpViewInfo opViewInfo);

    default void onFodIndicationVisibilityChanged(boolean z) {
    }

    default void onFodShowOrHideOnAod(boolean z) {
    }

    default void onScreenTurnedOff() {
    }

    default void onScreenTurnedOn() {
    }

    void onTimeChanged(Calendar calendar);

    default void onUserTrigger(int i) {
    }

    default void recoverFromBurnInScreen() {
    }

    default void release() {
    }

    default void supportSeconds(boolean z) {
    }
}
