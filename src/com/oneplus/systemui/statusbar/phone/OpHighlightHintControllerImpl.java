package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Chronometer;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.scene.OpSceneModeObserver;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintController;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
import java.util.Iterator;
public class OpHighlightHintControllerImpl implements OpHighlightHintController, KeyguardStateController.Callback {
    private int mBarState;
    private int mBgColor = 0;
    private final ArrayList<OpHighlightHintController.OnHighlightHintStateChangeListener> mCallbacks = new ArrayList<>();
    private StatusBarNotification mCarModeHighlightHintNotification;
    private boolean mCarModeShow = false;
    private NotificationEntryManager mEntryManager;
    private boolean mExpandedVisible = false;
    private boolean mHeadUpShow = false;
    private StatusBarNotification mHighlightHintNotification;
    private boolean mHighlightHintShow = false;
    private KeyguardStateController mKeyguardMonitor;
    private boolean mKeyguardShow = false;
    private boolean mShowCarModeHighlightNotification;
    private boolean mShowHighlightNotification;

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public boolean showOvalLayout() {
        return true;
    }

    public OpHighlightHintControllerImpl() {
        KeyguardStateController keyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        this.mKeyguardMonitor = keyguardStateController;
        keyguardStateController.addCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onKeyguardShowingChanged() {
        this.mKeyguardShow = this.mKeyguardMonitor.isShowing();
        if (Build.DEBUG_ONEPLUS) {
            Log.d("HighlightHintCtrl", "onKeyguardShowingChanged mKeyguardShow:" + this.mKeyguardShow);
        }
        onCarModeHighlightHintStateChange();
    }

    public NotificationEntryManager getEntryManager() {
        if (this.mEntryManager == null) {
            this.mEntryManager = (NotificationEntryManager) Dependency.get(NotificationEntryManager.class);
        }
        return this.mEntryManager;
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public void onNotificationUpdate(boolean z, StatusBarNotification statusBarNotification, boolean z2, StatusBarNotification statusBarNotification2) {
        this.mShowHighlightNotification = z;
        this.mHighlightHintNotification = statusBarNotification;
        this.mShowCarModeHighlightNotification = z2;
        this.mCarModeHighlightHintNotification = statusBarNotification2;
        getEntryManager();
        StatusBarNotification statusBarNotification3 = this.mHighlightHintNotification;
        if (!(statusBarNotification3 == null || statusBarNotification3.getNotification() == null)) {
            this.mBgColor = this.mHighlightHintNotification.getNotification().getBackgroundColorOnStatusBar();
        }
        onHighlightHintStateChange();
        onHighlightHintInfoUpdate();
        StatusBarNotification statusBarNotification4 = this.mCarModeHighlightHintNotification;
        if (!(statusBarNotification4 == null || statusBarNotification4.getNotification() == null)) {
            this.mCarModeHighlightHintNotification.getNotification().getBackgroundColorOnStatusBar();
        }
        onCarModeHighlightHintInfoUpdate();
        onCarModeHighlightHintStateChange();
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public void onHeadUpPinnedModeChange(boolean z) {
        if (this.mHeadUpShow != z) {
            this.mHeadUpShow = z;
            onHighlightHintStateChange();
            onCarModeHighlightHintStateChange();
        }
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public void onExpandedVisibleChange(boolean z) {
        if (this.mExpandedVisible != z) {
            this.mExpandedVisible = z;
            onHighlightHintStateChange();
            onCarModeHighlightHintStateChange();
        }
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public void onBarStatechange(int i) {
        if (this.mBarState != i) {
            this.mBarState = i;
            Log.d("HighlightHintCtrl", "onBarStatechange barstate:" + i);
            onHighlightHintStateChange();
            onCarModeHighlightHintStateChange();
        }
    }

    private void onHighlightHintStateChange() {
        boolean shouldShowHighlightHint = shouldShowHighlightHint();
        if (shouldShowHighlightHint != this.mHighlightHintShow) {
            this.mHighlightHintShow = shouldShowHighlightHint;
            dumpInfo();
            Iterator<OpHighlightHintController.OnHighlightHintStateChangeListener> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onHighlightHintStateChange();
            }
        }
    }

    private void onHighlightHintInfoUpdate() {
        Iterator<OpHighlightHintController.OnHighlightHintStateChangeListener> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onHighlightHintInfoChange();
        }
    }

    private void onCarModeHighlightHintStateChange() {
        boolean shouldShowCarModeHighlightHint = shouldShowCarModeHighlightHint();
        if (shouldShowCarModeHighlightHint != this.mCarModeShow) {
            Log.d("HighlightHintCtrl", "onCarModeHighlightHintStateChange show:" + shouldShowCarModeHighlightHint);
            this.mCarModeShow = shouldShowCarModeHighlightHint;
            Iterator<OpHighlightHintController.OnHighlightHintStateChangeListener> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onCarModeHighlightHintStateChange(this.mCarModeShow);
            }
        }
    }

    private void onCarModeHighlightHintInfoUpdate() {
        Iterator<OpHighlightHintController.OnHighlightHintStateChangeListener> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onCarModeHighlightHintInfoChange();
        }
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public boolean isHighLightHintShow() {
        return this.mHighlightHintShow;
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public int getHighlighColor() {
        return this.mBgColor;
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public boolean isCarModeHighlightHintSHow() {
        return this.mCarModeShow;
    }

    public boolean shouldShowHighlightHint() {
        return this.mShowHighlightNotification && !this.mHeadUpShow && !(this.mExpandedVisible && this.mBarState == 0) && !((OpSceneModeObserver) Dependency.get(OpSceneModeObserver.class)).isInBrickMode();
    }

    private boolean shouldShowCarModeHighlightHint() {
        return this.mShowCarModeHighlightNotification && !this.mKeyguardShow;
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public void launchCarModeAp(Context context) {
        Log.d("HighlightHintCtrl", "launchCarModeAp");
        StatusBarNotification statusBarNotification = this.mCarModeHighlightHintNotification;
        Intent intentOnStatusBar = statusBarNotification != null ? statusBarNotification.getNotification().getIntentOnStatusBar() : null;
        if (intentOnStatusBar != null && context != null) {
            context.sendBroadcast(intentOnStatusBar);
        }
    }

    public void addCallback(OpHighlightHintController.OnHighlightHintStateChangeListener onHighlightHintStateChangeListener) {
        this.mCallbacks.add(onHighlightHintStateChangeListener);
        onHighlightHintStateChangeListener.onHighlightHintStateChange();
        onHighlightHintStateChangeListener.onHighlightHintInfoChange();
    }

    public void removeCallback(OpHighlightHintController.OnHighlightHintStateChangeListener onHighlightHintStateChangeListener) {
        this.mCallbacks.remove(onHighlightHintStateChangeListener);
    }

    private void dumpInfo() {
        if (OpUtils.DEBUG_ONEPLUS) {
            boolean z = this.mExpandedVisible && this.mBarState == 0;
            Log.i("HighlightHintCtrl", "mHighlightHintShow:" + this.mHighlightHintShow + " showNotification:" + this.mShowHighlightNotification + " HeadsUp:" + this.mHeadUpShow + " expanededAfterUnlock:" + z);
        }
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public StatusBarNotification getHighlightHintNotification() {
        return this.mHighlightHintNotification;
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public Chronometer getStatusBarChronometer() {
        if (this.mHighlightHintNotification == null) {
            return null;
        }
        return getEntryManager().getActiveNotificationUnfiltered(this.mHighlightHintNotification.getKey()).statusBarChronometer;
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public Chronometer getKeyguardChronometer() {
        if (this.mHighlightHintNotification == null) {
            return null;
        }
        return getEntryManager().getActiveNotificationUnfiltered(this.mHighlightHintNotification.getKey()).keyguardChronometer;
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController
    public StatusBarNotification getCarModeHighlightHintNotification() {
        return this.mCarModeHighlightHintNotification;
    }
}
