package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardStatusView;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import com.android.systemui.statusbar.phone.LockIcon;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.phone.PanelView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.plugin.OpLsState;
public class OpNotificationPanelView extends PanelView {
    protected static String TAG = "OpNotificationPanelView";
    protected LockIcon mLockIcon;
    protected boolean mNeedShowOTAWizard = false;
    private NotificationPanelViewController mNotificationPanelViewController;

    static {
        boolean z = Build.DEBUG_ONEPLUS;
    }

    public OpNotificationPanelView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Log.d(TAG, "OpNotificationPanelView init ");
        getNotificationPanelViewController();
        getStatusBar();
    }

    private StatusBar getStatusBar() {
        if (this.mStatusBar == null) {
            this.mStatusBar = OpLsState.getInstance().getPhoneStatusBar();
            String str = TAG;
            Log.d(str, "init mStatusBar:" + this.mStatusBar + ", stack:" + Debug.getCallers(5));
        }
        return this.mStatusBar;
    }

    public void setStatuBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
    }

    public void setNotificationPanelViewController(NotificationPanelViewController notificationPanelViewController) {
        this.mNotificationPanelViewController = notificationPanelViewController;
    }

    private NotificationPanelViewController getNotificationPanelViewController() {
        if (this.mNotificationPanelViewController == null) {
            this.mNotificationPanelViewController = ((StatusBar) Dependency.get(StatusBar.class)).getPanelController();
            String str = TAG;
            Log.d(str, "init mNotificationPanelViewController:" + this.mNotificationPanelViewController + ", stack:" + Debug.getCallers(5));
        }
        return this.mNotificationPanelViewController;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        new Handler();
    }

    public void initOpBottomArea() {
        setShowOTAWizard(this.mNeedShowOTAWizard);
    }

    public void setShowOTAWizard(boolean z) {
        this.mNeedShowOTAWizard = z;
        String str = TAG;
        Log.d(str, "setShowOTAWizard, " + z);
        KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomArea;
        if (keyguardBottomAreaView != null) {
            keyguardBottomAreaView.setShowOTAWizard(this.mNeedShowOTAWizard);
            return;
        }
        String str2 = TAG;
        Log.i(str2, "mKeyguardBottomArea == null" + Debug.getCallers(5));
    }

    public void updateOpLockIcon() {
        StatusBar statusBar;
        LockIcon lockIcon;
        KeyguardStatusView keyguardStatusView = getKeyguardStatusView();
        if (keyguardStatusView != null && (statusBar = this.mStatusBar) != null) {
            NotificationShadeWindowView notificationShadeWindowView = statusBar.getNotificationShadeWindowView();
            if (notificationShadeWindowView == null) {
                Log.i(TAG, " updateOpLockIcon notificationShadeWindowView is null");
                return;
            }
            ViewGroup lockIconContainer = keyguardStatusView.getLockIconContainer();
            if (this.mLockIcon == null) {
                this.mLockIcon = (LockIcon) notificationShadeWindowView.findViewById(C0008R$id.lock_icon);
            }
            if (lockIconContainer != null && (lockIcon = this.mLockIcon) != null) {
                ((ViewGroup) lockIcon.getParent()).removeView(this.mLockIcon);
                lockIconContainer.addView(this.mLockIcon);
                ViewGroup.LayoutParams layoutParams = this.mLockIcon.getLayoutParams();
                if (layoutParams instanceof FrameLayout.LayoutParams) {
                    ((FrameLayout.LayoutParams) layoutParams).setMargins(0, 0, 0, 0);
                    this.mLockIcon.setLayoutParams(layoutParams);
                }
                Log.i(TAG, " updateOpLockIcon complete");
            } else if (lockIconContainer == null) {
                Log.i(TAG, " updateOpLockIcon statusbarViewContainer is null");
            } else if (this.mLockIcon == null) {
                Log.i(TAG, " updateOpLockIcon lockIcon is null");
            }
        } else if (keyguardStatusView == null) {
            Log.i(TAG, " updateOpLockIcon keyguardStatusView is null");
        } else if (this.mStatusBar == null) {
            Log.i(TAG, " updateOpLockIcon mStatusBar is null");
        }
    }

    private KeyguardStatusView getKeyguardStatusView() {
        return getNotificationPanelViewController().getKeyguardStatusView();
    }
}
