package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.widget.Chronometer;
import com.android.systemui.statusbar.policy.CallbackController;
public interface OpHighlightHintController extends CallbackController<OnHighlightHintStateChangeListener> {

    public interface OnHighlightHintStateChangeListener {
        default void onCarModeHighlightHintInfoChange() {
        }

        default void onCarModeHighlightHintStateChange(boolean z) {
        }

        default void onHighlightHintInfoChange() {
        }

        default void onHighlightHintStateChange() {
        }
    }

    StatusBarNotification getCarModeHighlightHintNotification();

    int getHighlighColor();

    StatusBarNotification getHighlightHintNotification();

    Chronometer getKeyguardChronometer();

    Chronometer getStatusBarChronometer();

    boolean isCarModeHighlightHintSHow();

    boolean isHighLightHintShow();

    void launchCarModeAp(Context context);

    void onBarStatechange(int i);

    void onExpandedVisibleChange(boolean z);

    void onHeadUpPinnedModeChange(boolean z);

    void onNotificationUpdate(boolean z, StatusBarNotification statusBarNotification, boolean z2, StatusBarNotification statusBarNotification2);

    boolean showOvalLayout();
}
