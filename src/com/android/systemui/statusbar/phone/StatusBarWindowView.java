package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.ScreenDecorations;
import com.oneplus.battery.OpChargingAnimationController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintController;
public class StatusBarWindowView extends FrameLayout {
    private static Context mContext;
    private int mLeftInset = 0;
    protected NotificationPanelViewController mNotificationPanelViewController;
    private int mRightInset = 0;
    private int mTopInset = 0;

    static {
        boolean z = StatusBar.DEBUG;
    }

    public StatusBarWindowView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        Insets insetsIgnoringVisibility = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
        this.mLeftInset = insetsIgnoringVisibility.left;
        this.mRightInset = insetsIgnoringVisibility.right;
        this.mTopInset = 0;
        DisplayCutout displayCutout = getRootWindowInsets().getDisplayCutout();
        if (displayCutout != null) {
            this.mTopInset = displayCutout.getWaterfallInsets().top;
        }
        applyMargins();
        return windowInsets;
    }

    private void applyMargins() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) childAt.getLayoutParams();
                if (layoutParams.rightMargin != this.mRightInset || layoutParams.leftMargin != this.mLeftInset || layoutParams.topMargin != this.mTopInset) {
                    layoutParams.rightMargin = this.mRightInset;
                    layoutParams.leftMargin = this.mLeftInset;
                    layoutParams.topMargin = this.mTopInset;
                    childAt.requestLayout();
                }
            }
        }
    }

    public static Pair<Integer, Integer> paddingNeededForCutoutAndRoundedCorner(DisplayCutout displayCutout, Pair<Integer, Integer> pair, int i) {
        if (displayCutout == null) {
            return new Pair<>(Integer.valueOf(i), Integer.valueOf(i));
        }
        int safeInsetLeft = displayCutout.getSafeInsetLeft();
        int safeInsetRight = displayCutout.getSafeInsetRight();
        if (pair != null) {
            safeInsetLeft = Math.max(safeInsetLeft, ((Integer) pair.first).intValue());
            safeInsetRight = Math.max(safeInsetRight, ((Integer) pair.second).intValue());
        }
        return new Pair<>(Integer.valueOf(Math.max(safeInsetLeft, i)), Integer.valueOf(Math.max(safeInsetRight, i)));
    }

    public static Pair<Integer, Integer> cornerCutoutMargins(DisplayCutout displayCutout, Display display) {
        return statusBarCornerCutoutMargins(displayCutout, display, 0, 0);
    }

    public static Pair<Integer, Integer> statusBarCornerCutoutMargins(DisplayCutout displayCutout, Display display, int i, int i2) {
        if (displayCutout == null) {
            return null;
        }
        Point point = new Point();
        display.getRealSize(point);
        Rect rect = new Rect();
        if (i == 0) {
            ScreenDecorations.DisplayCutoutView.boundsFromDirection(mContext, displayCutout, 48, rect);
        } else if (i == 1) {
            ScreenDecorations.DisplayCutoutView.boundsFromDirection(mContext, displayCutout, 3, rect);
        } else if (i == 2) {
            ScreenDecorations.DisplayCutoutView.boundsFromDirection(mContext, displayCutout, 5, rect);
        } else if (i == 3) {
            return null;
        }
        if (i2 >= 0 && rect.top > i2) {
            return null;
        }
        if (rect.left <= 0) {
            return new Pair<>(Integer.valueOf(rect.right), 0);
        }
        if (rect.right >= point.x) {
            return new Pair<>(0, Integer.valueOf(point.x - rect.left));
        }
        return null;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (OpLsState.getInstance().getPreventModeCtrl().isPreventModeActive()) {
            OpLsState.getInstance().getPreventModeCtrl().disPatchTouchEvent(motionEvent);
            return true;
        } else if (OpLsState.getInstance().getBiometricUnlockController().getMode() == 5 || OpLsState.getInstance().getBiometricUnlockController().getMode() == 7 || ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFacelockUnlocking() || ((OpChargingAnimationController) Dependency.get(OpChargingAnimationController.class)).isAnimationStarted()) {
            return true;
        } else {
            if (OpLsState.getInstance().getFpAnimationCtrl() != null && OpLsState.getInstance().getFpAnimationCtrl().isPlayingAnimation()) {
                return true;
            }
            if (((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isHighLightHintShow() || ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isCarModeHighlightHintSHow()) {
                this.mNotificationPanelViewController.onHightlightHintIntercept(motionEvent);
            }
            return super.onInterceptTouchEvent(motionEvent);
        }
    }

    public void setNotificationPanelViewController(NotificationPanelViewController notificationPanelViewController) {
        this.mNotificationPanelViewController = notificationPanelViewController;
    }
}
