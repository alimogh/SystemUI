package com.android.systemui.statusbar.phone;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Debug;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.OpFeatures;
import android.util.Pair;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.constraintlayout.widget.R$styleable;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.util.leak.RotationUtils;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarView;
import com.oneplus.util.OpUtils;
import java.util.Objects;
public class PhoneStatusBarView extends OpPhoneStatusBarView {
    public static int MODE_COMPAT = 100;
    public static int MODE_FULL = R$styleable.Constraint_layout_goneMarginStart;
    private static boolean mIsFullScreenMode = false;
    private AppOpsManager mAppOps;
    StatusBar mBar;
    private DarkIconDispatcher.DarkReceiver mBattery;
    private View mCenterIconSpace;
    private final CommandQueue mCommandQueue = ((CommandQueue) Dependency.get(CommandQueue.class));
    private int mCutoutSideNudge = 0;
    private View mCutoutSpace;
    private DisplayCutout mDisplayCutout;
    private boolean mHeadsUpVisible;
    private Runnable mHideExpandedRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarView.1
        @Override // java.lang.Runnable
        public void run() {
            PhoneStatusBarView phoneStatusBarView = PhoneStatusBarView.this;
            if (phoneStatusBarView.mPanelFraction == 0.0f) {
                phoneStatusBarView.mBar.makeExpandedInvisible();
            }
        }
    };
    boolean mIsFullyOpenedPanel = false;
    private float mMinFraction;
    private OverviewProxyService mOverviewProxyService = ((OverviewProxyService) Dependency.get(OverviewProxyService.class));
    private int mRotationOrientation = -1;
    private int mRoundedCornerPadding = 0;
    private ScrimController mScrimController;
    private int mStatusBarHeight;
    private View mSystemIconArea;
    private String mTopActivityPackage = "";

    static {
        boolean z = StatusBar.DEBUG;
    }

    public PhoneStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setBar(StatusBar statusBar) {
        this.mBar = statusBar;
    }

    public void setScrimController(ScrimController scrimController) {
        this.mScrimController = scrimController;
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarView, com.android.systemui.statusbar.phone.PanelBar, android.view.View
    public void onFinishInflate() {
        this.mBattery = (DarkIconDispatcher.DarkReceiver) findViewById(C0008R$id.battery);
        this.mCutoutSpace = findViewById(C0008R$id.cutout_space_view);
        this.mCenterIconSpace = findViewById(C0008R$id.centered_icon_area);
        this.mSystemIconArea = findViewById(C0008R$id.system_icon_area);
        super.onFinishInflate();
        updateResources();
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarView, android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this.mBattery);
        if (updateOrientationAndCutout()) {
            updateLayoutForCutout();
        }
        updateStatusBarPadding();
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarView, android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this.mBattery);
        this.mDisplayCutout = null;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarView, android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateResources();
        if (updateOrientationAndCutout()) {
            updateLayoutForCutout();
            requestLayout();
        }
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        if (updateOrientationAndCutout()) {
            updateLayoutForCutout();
            requestLayout();
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    private boolean updateOrientationAndCutout() {
        boolean z;
        int exactRotation = RotationUtils.getExactRotation(((FrameLayout) this).mContext);
        if (exactRotation != this.mRotationOrientation) {
            this.mRotationOrientation = exactRotation;
            z = true;
        } else {
            z = false;
        }
        if (Objects.equals(getRootWindowInsets().getDisplayCutout(), this.mDisplayCutout)) {
            return z;
        }
        this.mDisplayCutout = getRootWindowInsets().getDisplayCutout();
        return true;
    }

    private boolean checkFullScreenMode(String str) {
        boolean isFullAndNotchEnabled = isFullAndNotchEnabled(str);
        if (isFullAndNotchEnabled == mIsFullScreenMode) {
            return false;
        }
        mIsFullScreenMode = isFullAndNotchEnabled;
        return true;
    }

    private boolean isFullAndNotchEnabled(String str) {
        boolean z = true;
        if (OpFeatures.isSupport(new int[]{49}) && !TextUtils.isEmpty(str)) {
            StatusBar statusBar = this.mBar;
            boolean isCameraNotchIgnoreSetting = statusBar != null ? statusBar.isCameraNotchIgnoreSetting() : false;
            try {
                ApplicationInfo applicationInfo = getContext().getPackageManager().getApplicationInfo(str, 1);
                if (applicationInfo == null) {
                    return false;
                }
                int i = applicationInfo.uid;
                if (isCameraNotchIgnoreSetting || getCompatMode(this.mAppOps, str, i) != 1) {
                    z = false;
                }
                Log.i("PhoneStatusBarView", "isFullAndNotchEnabled() isFullMode=" + z + " pkg=" + str + " isCameraNotchIgnoreSetting=" + isCameraNotchIgnoreSetting + ", uid=" + i);
                return z;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private int getCompatMode(AppOpsManager appOpsManager, String str, int i) {
        StatusBar statusBar = this.mBar;
        int i2 = 0;
        boolean isCameraNotchIgnoreSetting = statusBar != null ? statusBar.isCameraNotchIgnoreSetting() : false;
        if (appOpsManager != null) {
            try {
                int checkOpNoThrow = appOpsManager.checkOpNoThrow(1006, i, str);
                if (checkOpNoThrow != 2) {
                    if (checkOpNoThrow != MODE_FULL) {
                        if (checkOpNoThrow == 0 || checkOpNoThrow == MODE_COMPAT) {
                            i2 = 2;
                        }
                        Log.d("PhoneStatusBarView", "getCompatMode: mode:" + i2 + " ops:" + checkOpNoThrow);
                    }
                }
                if (!isCameraNotchIgnoreSetting) {
                    i2 = 1;
                }
                Log.d("PhoneStatusBarView", "getCompatMode: mode:" + i2 + " ops:" + checkOpNoThrow);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("PhoneStatusBarView", "AppOps is null");
        }
        return i2;
    }

    public void updateTopPackage(String str) {
        if (TextUtils.isEmpty(this.mTopActivityPackage) || !TextUtils.equals(this.mTopActivityPackage, str)) {
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.i("PhoneStatusBarView", "updateTopPackage pkg:" + str);
            }
            this.mTopActivityPackage = str;
            if (checkFullScreenMode(str)) {
                updateLayoutForCutout();
                requestLayout();
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public boolean panelEnabled() {
        return this.mCommandQueue.panelsEnabled();
    }

    public boolean onRequestSendAccessibilityEventInternal(View view, AccessibilityEvent accessibilityEvent) {
        if (!super.onRequestSendAccessibilityEventInternal(view, accessibilityEvent)) {
            return false;
        }
        AccessibilityEvent obtain = AccessibilityEvent.obtain();
        onInitializeAccessibilityEvent(obtain);
        dispatchPopulateAccessibilityEvent(obtain);
        accessibilityEvent.appendRecord(obtain);
        return true;
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onPanelPeeked() {
        super.onPanelPeeked();
        this.mBar.makeExpandedVisible(false);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onPanelCollapsed() {
        super.onPanelCollapsed();
        post(this.mHideExpandedRunnable);
        this.mIsFullyOpenedPanel = false;
    }

    public void removePendingHideExpandedRunnables() {
        Log.i("PhoneStatusBarView", "removePendingHideExpandedRunnables: " + Debug.getCallers(5));
        removeCallbacks(this.mHideExpandedRunnable);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onPanelFullyOpened() {
        super.onPanelFullyOpened();
        if (!this.mIsFullyOpenedPanel) {
            this.mPanel.getView().sendAccessibilityEvent(32);
        }
        this.mIsFullyOpenedPanel = true;
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.mBar.interceptTouchEvent(motionEvent) || super.onTouchEvent(motionEvent);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onTrackingStarted() {
        super.onTrackingStarted();
        this.mBar.onTrackingStarted();
        this.mScrimController.onTrackingStarted();
        removePendingHideExpandedRunnables();
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onClosingFinished() {
        super.onClosingFinished();
        this.mBar.onClosingFinished();
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onTrackingStopped(boolean z) {
        super.onTrackingStopped(z);
        this.mBar.onTrackingStopped(z);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void onExpandingFinished() {
        super.onExpandingFinished();
        this.mScrimController.onExpandingFinished();
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.mBar.interceptTouchEvent(motionEvent) || super.onInterceptTouchEvent(motionEvent);
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarView, com.android.systemui.statusbar.phone.PanelBar
    public void panelScrimMinFractionChanged(float f) {
        if (Float.isNaN(f)) {
            throw new IllegalArgumentException("minFraction cannot be NaN");
        } else if (this.mMinFraction != f) {
            this.mMinFraction = f;
            updateScrimFraction();
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void panelExpansionChanged(float f, boolean z) {
        super.panelExpansionChanged(f, z);
        updateScrimFraction();
        int i = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
        if ((i == 0 || f == 1.0f) && this.mBar.getNavigationBarView() != null) {
            this.mBar.getNavigationBarView().onStatusBarPanelStateChanged();
        }
        if (i == 0 || f == 1.0f) {
            OpLsState.getInstance().getPreventModeCtrl().onPanelExpandedChange(z);
        }
        if ((i == 0 || f == 1.0f) && this.mBar.getNavigationBarView() == null) {
            this.mOverviewProxyService.updateSystemUIStateFlagsInternal();
        }
    }

    private void updateScrimFraction() {
        float f = this.mPanelFraction;
        float f2 = this.mMinFraction;
        if (f2 < 1.0f) {
            f = Math.max((f - f2) / (1.0f - f2), 0.0f);
        }
        this.mScrimController.setPanelExpansion(f);
    }

    public void updateResources() {
        this.mCutoutSideNudge = getResources().getDimensionPixelSize(C0005R$dimen.display_cutout_margin_consumption);
        this.mRoundedCornerPadding = getResources().getDimensionPixelSize(C0005R$dimen.rounded_corner_content_padding);
        updateStatusBarHeight();
    }

    private void updateStatusBarHeight() {
        int i;
        DisplayCutout displayCutout = this.mDisplayCutout;
        int i2 = displayCutout == null ? 0 : displayCutout.getWaterfallInsets().top;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0005R$dimen.status_bar_height);
        this.mStatusBarHeight = dimensionPixelSize;
        layoutParams.height = dimensionPixelSize - i2;
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(C0005R$dimen.status_bar_padding_top);
        int dimensionPixelSize3 = getResources().getDimensionPixelSize(C0005R$dimen.status_bar_padding_start);
        int dimensionPixelSize4 = getResources().getDimensionPixelSize(C0005R$dimen.status_bar_padding_end);
        if (OpUtils.isSupportResolutionSwitch(getContext())) {
            dimensionPixelSize3 = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.status_bar_padding_start, 1080);
            dimensionPixelSize4 = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.status_bar_padding_end, 1080);
        }
        findViewById(C0008R$id.status_bar_contents).setPaddingRelative(dimensionPixelSize3, dimensionPixelSize2, dimensionPixelSize4, 0);
        findViewById(C0008R$id.notification_lights_out).setPaddingRelative(0, dimensionPixelSize3, 0, 0);
        setLayoutParams(layoutParams);
        if ((!OpUtils.isSupportCutout() || OpUtils.isSupportHolePunchFrontCam() || OpUtils.isCutoutHide(((FrameLayout) this).mContext)) && !OpUtils.isCutoutEmulationEnabled()) {
            i = getResources().getDimensionPixelSize(C0005R$dimen.op_status_icon_left_padding);
        } else {
            i = 0;
        }
        this.mSystemIconArea.setPaddingRelative(i, 0, 0, 0);
    }

    private void updateLayoutForCutout() {
        updateStatusBarHeight();
        updateCutoutLocation(StatusBarWindowView.cornerCutoutMargins(this.mDisplayCutout, getDisplay()));
        updateSafeInsets(StatusBarWindowView.statusBarCornerCutoutMargins(this.mDisplayCutout, getDisplay(), this.mRotationOrientation, this.mStatusBarHeight));
    }

    private void updateCutoutLocation(Pair<Integer, Integer> pair) {
        if (this.mCutoutSpace != null) {
            DisplayCutout displayCutout = this.mDisplayCutout;
            if (displayCutout == null || displayCutout.isEmpty() || pair != null) {
                this.mCenterIconSpace.setVisibility(0);
                this.mCutoutSpace.setVisibility(8);
                return;
            }
            this.mCenterIconSpace.setVisibility(8);
            this.mCutoutSpace.setVisibility(0);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mCutoutSpace.getLayoutParams();
            Rect rect = new Rect();
            ScreenDecorations.DisplayCutoutView.boundsFromDirection(((FrameLayout) this).mContext, this.mDisplayCutout, 48, rect);
            int i = rect.left;
            int i2 = this.mCutoutSideNudge;
            rect.left = i + i2;
            rect.right -= i2;
            int dimensionPixelSize = getResources().getDimensionPixelSize(C0005R$dimen.op_cust_statusbar_cutout_show_region_left);
            int dimensionPixelSize2 = getResources().getDimensionPixelSize(C0005R$dimen.op_cust_statusbar_cutout_show_region_right);
            if (!(dimensionPixelSize == 0 && dimensionPixelSize2 == 0) && !OpUtils.isCutoutEmulationEnabled()) {
                rect.left = dimensionPixelSize;
                rect.right = dimensionPixelSize2;
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.i("PhoneStatusBarView", "left:" + rect.left + ", right:" + rect.right + ", width():" + rect.width() + ", height():" + rect.height());
                }
            }
            layoutParams.width = rect.width();
            layoutParams.height = rect.height();
        }
    }

    private void updateSafeInsets(Pair<Integer, Integer> pair) {
        Pair<Integer, Integer> paddingNeededForCutoutAndRoundedCorner = StatusBarWindowView.paddingNeededForCutoutAndRoundedCorner(this.mDisplayCutout, pair, this.mRoundedCornerPadding);
        setPadding(((Integer) paddingNeededForCutoutAndRoundedCorner.first).intValue(), getPaddingTop(), ((Integer) paddingNeededForCutoutAndRoundedCorner.second).intValue(), getPaddingBottom());
    }

    public void setHeadsUpVisible(boolean z) {
        this.mHeadsUpVisible = z;
        updateVisibility();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelBar
    public boolean shouldPanelBeVisible() {
        return this.mHeadsUpVisible || super.shouldPanelBeVisible();
    }
}
