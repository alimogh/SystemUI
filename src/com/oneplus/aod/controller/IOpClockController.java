package com.oneplus.aod.controller;

import android.graphics.Rect;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.oneplus.aod.OpAodBatteryStatusView;
import com.oneplus.aod.OpAodNotificationIconAreaController;
import com.oneplus.aod.views.OpTextDate;
import java.util.TimeZone;
public interface IOpClockController {
    default void applyBatteryInfoTextSettings(OpAodBatteryStatusView opAodBatteryStatusView) {
    }

    void applyBatteryInfoViewMargin(LinearLayout.LayoutParams layoutParams);

    default void applyDateInfoTextSettings(OpTextDate opTextDate) {
    }

    void applyDateInfoViewMargin(LinearLayout.LayoutParams layoutParams);

    default void applyNotificationInfoTextSettings(OpAodNotificationIconAreaController opAodNotificationIconAreaController) {
    }

    void applyNotificationInfoViewMargin(LinearLayout.LayoutParams layoutParams);

    default void applyOwnerInfoTextSettings(TextView textView) {
    }

    void applyOwnerInfoViewMargin(LinearLayout.LayoutParams layoutParams);

    void applySliceInfoViewMargin(LinearLayout.LayoutParams layoutParams);

    void applySystemInfoViewMargin(RelativeLayout.LayoutParams layoutParams);

    default String getBurnInHandleClassName() {
        return null;
    }

    View getClockView();

    View getCurrentView();

    default int getMovingDistance() {
        return 0;
    }

    void onDestroyView();

    void onFodIndicationVisibilityChanged(boolean z);

    void onFodShowOrHideOnAod(boolean z);

    default void onScreenTurnedOff() {
    }

    default void onScreenTurnedOn() {
    }

    default void onTimeTick() {
    }

    default void onTimeZoneChanged(TimeZone timeZone) {
    }

    void onUserTrigger(int i);

    void recoverFromBurnInScreen();

    default boolean shouldShowBattery() {
        return true;
    }

    default boolean shouldShowDate() {
        return true;
    }

    default boolean shouldShowNotification() {
        return true;
    }

    default boolean shouldShowOwnerInfo() {
        return true;
    }

    default boolean shouldShowSliceInfo() {
        return true;
    }

    default void updateSettings(int i) {
    }

    default Rect getBound() {
        return new Rect();
    }
}
