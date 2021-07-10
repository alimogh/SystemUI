package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.C0002R$attr;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.KeyguardStatusBarView;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.oneplus.systemui.statusbar.phone.OpKeyguardStatusBarView;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
public class KeyguardStatusBarView extends OpKeyguardStatusBarView implements BatteryController.BatteryStateChangeCallback, UserInfoController.OnUserInfoChangedListener, ConfigurationController.ConfigurationListener {
    private boolean mBatteryCharging;
    private BatteryController mBatteryController;
    private boolean mBatteryListening;
    private BatteryMeterView mBatteryView;
    private TextView mCarrierLabel;
    private int mContentPaddintTop;
    private int mCutoutSideNudge = 0;
    private View mCutoutSpace;
    private int mDensityDpi;
    private DisplayCutout mDisplayCutout;
    private final Rect mEmptyRect = new Rect(0, 0, 0, 0);
    private float mFontScale;
    private StatusBarIconController.TintedIconManager mIconManager;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private boolean mKeyguardUserSwitcherShowing;
    private int mLastOrientation = 0;
    private int mLayoutState = 0;
    private ImageView mMultiUserAvatar;
    private MultiUserSwitch mMultiUserSwitch;
    private Pair<Integer, Integer> mPadding = new Pair<>(0, 0);
    private int mRoundedCornerPadding = 0;
    private ViewGroup mStatusIconArea;
    private View mSystemIconsContainer;
    private UserSwitcherController mUserSwitcherController;

    private int calculateMargin(int i, int i2) {
        if (i2 >= i) {
            return 0;
        }
        return i - i2;
    }

    private void updateSystemIconsLayoutParams() {
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
    }

    public KeyguardStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.systemui.statusbar.phone.OpKeyguardStatusBarView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mSystemIconsContainer = findViewById(C0008R$id.system_icons_container);
        MultiUserSwitch multiUserSwitch = (MultiUserSwitch) findViewById(C0008R$id.multi_user_switch);
        this.mMultiUserSwitch = multiUserSwitch;
        multiUserSwitch.setVisibility(8);
        this.mMultiUserAvatar = (ImageView) findViewById(C0008R$id.multi_user_avatar);
        this.mCarrierLabel = (TextView) findViewById(C0008R$id.keyguard_carrier_text);
        this.mBatteryView = (BatteryMeterView) this.mSystemIconsContainer.findViewById(C0008R$id.battery);
        this.mCutoutSpace = findViewById(C0008R$id.cutout_space_view);
        this.mStatusIconArea = (ViewGroup) findViewById(C0008R$id.status_icon_area);
        StatusIconContainer statusIconContainer = (StatusIconContainer) findViewById(C0008R$id.statusIcons);
        loadDimens();
        updateUserSwitcher();
        this.mBatteryController = (BatteryController) Dependency.get(BatteryController.class);
        this.mContentPaddintTop = getResources().getDimensionPixelSize(C0005R$dimen.status_bar_padding_top);
        this.mDensityDpi = getResources().getConfiguration().densityDpi;
        this.mFontScale = getResources().getConfiguration().fontScale;
        this.mBatteryView.setPowerSaveEnabled(this.mBatteryController.isPowerSave());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (!(this.mDensityDpi == configuration.densityDpi && this.mFontScale == configuration.fontScale)) {
            this.mDensityDpi = configuration.densityDpi;
            this.mFontScale = configuration.fontScale;
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mIconManager);
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mIconManager);
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mMultiUserAvatar.getLayoutParams();
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0005R$dimen.multi_user_avatar_keyguard_size);
        marginLayoutParams.height = dimensionPixelSize;
        marginLayoutParams.width = dimensionPixelSize;
        this.mMultiUserAvatar.setLayoutParams(marginLayoutParams);
        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mMultiUserSwitch.getLayoutParams();
        marginLayoutParams2.width = getResources().getDimensionPixelSize(C0005R$dimen.multi_user_switch_width_keyguard);
        marginLayoutParams2.setMarginEnd(getResources().getDimensionPixelSize(C0005R$dimen.multi_user_switch_keyguard_margin));
        this.mMultiUserSwitch.setLayoutParams(marginLayoutParams2);
        ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) this.mSystemIconsContainer.getLayoutParams();
        marginLayoutParams3.setMarginStart(getResources().getDimensionPixelSize(C0005R$dimen.system_icons_super_container_margin_start));
        this.mSystemIconsContainer.setLayoutParams(marginLayoutParams3);
        View view = this.mSystemIconsContainer;
        view.setPaddingRelative(view.getPaddingStart(), this.mSystemIconsContainer.getPaddingTop(), getResources().getDimensionPixelSize(C0005R$dimen.system_icons_keyguard_padding_end), this.mSystemIconsContainer.getPaddingBottom());
        this.mCarrierLabel.setTextSize(0, (float) getResources().getDimensionPixelSize(17105509));
        ViewGroup.MarginLayoutParams marginLayoutParams4 = (ViewGroup.MarginLayoutParams) this.mCarrierLabel.getLayoutParams();
        marginLayoutParams4.setMarginStart(calculateMargin(getResources().getDimensionPixelSize(C0005R$dimen.keyguard_carrier_text_margin), ((Integer) this.mPadding.first).intValue()));
        this.mCarrierLabel.setLayoutParams(marginLayoutParams4);
        updateKeyguardStatusBarHeight();
        updateOrientationAndContentPosition(configuration.orientation);
        if (OpUtils.isSupportHolePunchFrontCam()) {
            updateStatusBarPadding();
        }
    }

    private void updateKeyguardStatusBarHeight() {
        DisplayCutout displayCutout = this.mDisplayCutout;
        int i = displayCutout == null ? 0 : displayCutout.getWaterfallInsets().top;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        marginLayoutParams.height = getResources().getDimensionPixelSize(C0005R$dimen.status_bar_header_height_keyguard) + i;
        setLayoutParams(marginLayoutParams);
    }

    private void loadDimens() {
        Resources resources = getResources();
        resources.getDimensionPixelSize(C0005R$dimen.system_icons_switcher_hidden_expanded_margin);
        resources.getDimensionPixelSize(C0005R$dimen.system_icons_super_container_avatarless_margin_end);
        this.mCutoutSideNudge = getResources().getDimensionPixelSize(C0005R$dimen.display_cutout_margin_consumption);
        getContext().getResources().getBoolean(17891375);
        this.mRoundedCornerPadding = resources.getDimensionPixelSize(C0005R$dimen.rounded_corner_content_padding);
    }

    private void updateVisibilities() {
        if (this.mMultiUserSwitch.getParent() == this.mStatusIconArea || this.mKeyguardUserSwitcherShowing) {
            ViewParent parent = this.mMultiUserSwitch.getParent();
            ViewGroup viewGroup = this.mStatusIconArea;
            if (parent == viewGroup && this.mKeyguardUserSwitcherShowing) {
                viewGroup.removeView(this.mMultiUserSwitch);
                return;
            }
            return;
        }
        if (this.mMultiUserSwitch.getParent() != null) {
            getOverlay().remove(this.mMultiUserSwitch);
        }
        this.mStatusIconArea.addView(this.mMultiUserSwitch, 0);
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mLayoutState = 0;
        if (updateLayoutConsideringCutout()) {
            requestLayout();
        }
        updateStatusBarPadding();
        return super.onApplyWindowInsets(windowInsets);
    }

    private boolean updateLayoutConsideringCutout() {
        this.mDisplayCutout = getRootWindowInsets().getDisplayCutout();
        updateKeyguardStatusBarHeight();
        Pair<Integer, Integer> cornerCutoutMargins = StatusBarWindowView.cornerCutoutMargins(this.mDisplayCutout, getDisplay());
        updatePadding(cornerCutoutMargins);
        if (this.mDisplayCutout == null || cornerCutoutMargins != null) {
            return updateLayoutParamsNoCutout();
        }
        return updateLayoutParamsForCutout();
    }

    private void updatePadding(Pair<Integer, Integer> pair) {
        DisplayCutout displayCutout = this.mDisplayCutout;
        int i = displayCutout == null ? 0 : displayCutout.getWaterfallInsets().top;
        this.mPadding = StatusBarWindowView.paddingNeededForCutoutAndRoundedCorner(this.mDisplayCutout, pair, this.mRoundedCornerPadding);
        setPadding(((Integer) this.mPadding.first).intValue(), i + OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.op_keyguard_status_bar_cust_padding_top, 1080), ((Integer) this.mPadding.second).intValue(), 0);
    }

    private boolean updateLayoutParamsNoCutout() {
        if (this.mLayoutState == 2) {
            return false;
        }
        this.mLayoutState = 2;
        View view = this.mCutoutSpace;
        if (view != null) {
            view.setVisibility(8);
        }
        ((RelativeLayout.LayoutParams) this.mCarrierLabel.getLayoutParams()).addRule(16, C0008R$id.status_icon_area);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mStatusIconArea.getLayoutParams();
        layoutParams.removeRule(17);
        layoutParams.width = -2;
        ((LinearLayout.LayoutParams) this.mSystemIconsContainer.getLayoutParams()).setMarginStart(getResources().getDimensionPixelSize(C0005R$dimen.system_icons_super_container_margin_start));
        return true;
    }

    private boolean updateLayoutParamsForCutout() {
        if (this.mLayoutState == 1) {
            return false;
        }
        this.mLayoutState = 1;
        if (this.mCutoutSpace == null) {
            updateLayoutParamsNoCutout();
        }
        Rect rect = new Rect();
        ScreenDecorations.DisplayCutoutView.boundsFromDirection(this.mDisplayCutout, 48, rect);
        this.mCutoutSpace.setVisibility(0);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mCutoutSpace.getLayoutParams();
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
                Log.i("KeyguardStatusBarView", "left:" + rect.left + ", right:" + rect.right + ", width:" + rect.width() + ", height:" + rect.height());
            }
        }
        layoutParams.width = rect.width();
        layoutParams.height = rect.height();
        layoutParams.addRule(13);
        ((RelativeLayout.LayoutParams) this.mCarrierLabel.getLayoutParams()).addRule(16, C0008R$id.cutout_space_view);
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mStatusIconArea.getLayoutParams();
        layoutParams2.addRule(17, C0008R$id.cutout_space_view);
        layoutParams2.width = -1;
        ((LinearLayout.LayoutParams) this.mSystemIconsContainer.getLayoutParams()).setMarginStart(0);
        return true;
    }

    public void setListening(boolean z) {
        if (z != this.mBatteryListening) {
            this.mBatteryListening = z;
            if (z) {
                this.mBatteryController.addCallback(this);
            } else {
                this.mBatteryController.removeCallback(this);
            }
        }
    }

    private void updateUserSwitcher() {
        boolean z = this.mKeyguardUserSwitcher != null;
        this.mMultiUserSwitch.setClickable(z);
        this.mMultiUserSwitch.setFocusable(z);
        this.mMultiUserSwitch.setKeyguardMode(z);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.systemui.statusbar.phone.OpKeyguardStatusBarView, android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        UserInfoController userInfoController = (UserInfoController) Dependency.get(UserInfoController.class);
        userInfoController.addCallback(this);
        UserSwitcherController userSwitcherController = (UserSwitcherController) Dependency.get(UserSwitcherController.class);
        this.mUserSwitcherController = userSwitcherController;
        this.mMultiUserSwitch.setUserSwitcherController(userSwitcherController);
        userInfoController.reloadUserInfo();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        this.mIconManager = new StatusBarIconController.TintedIconManager((ViewGroup) findViewById(C0008R$id.statusIcons), (CommandQueue) Dependency.get(CommandQueue.class), "keyguard");
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mIconManager);
        onThemeChanged();
        updateOrientationAndContentPosition(getResources().getConfiguration().orientation);
        updateStatusBarPadding();
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.systemui.statusbar.phone.OpKeyguardStatusBarView, android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((UserInfoController) Dependency.get(UserInfoController.class)).removeCallback(this);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mIconManager);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
    public void onUserInfoChanged(String str, Drawable drawable, String str2) {
        this.mMultiUserAvatar.setImageDrawable(drawable);
    }

    public void setQSPanel(QSPanel qSPanel) {
        this.mMultiUserSwitch.setQsPanel(qSPanel);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        if (this.mBatteryCharging != z2) {
            this.mBatteryCharging = z2;
            updateVisibilities();
        }
    }

    public void setKeyguardUserSwitcherShowing(boolean z, boolean z2) {
        this.mKeyguardUserSwitcherShowing = z;
        if (z2) {
            animateNextLayoutChange();
        }
        updateVisibilities();
        updateLayoutConsideringCutout();
        updateSystemIconsLayoutParams();
    }

    private void animateNextLayoutChange() {
        final int left = this.mSystemIconsContainer.getLeft();
        final boolean z = this.mMultiUserSwitch.getParent() == this.mStatusIconArea;
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() { // from class: com.android.systemui.statusbar.phone.KeyguardStatusBarView.1
            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                KeyguardStatusBarView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                boolean z2 = z && KeyguardStatusBarView.this.mMultiUserSwitch.getParent() != KeyguardStatusBarView.this.mStatusIconArea;
                KeyguardStatusBarView.this.mSystemIconsContainer.setX((float) left);
                KeyguardStatusBarView.this.mSystemIconsContainer.animate().translationX(0.0f).setDuration(400).setStartDelay(z2 ? 300 : 0).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).start();
                if (z2) {
                    KeyguardStatusBarView.this.getOverlay().add(KeyguardStatusBarView.this.mMultiUserSwitch);
                    KeyguardStatusBarView.this.mMultiUserSwitch.animate().alpha(0.0f).setDuration(300).setStartDelay(0).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$KeyguardStatusBarView$1$DyabYtIeJMptnepd5jqXSnZ7UZ0
                        @Override // java.lang.Runnable
                        public final void run() {
                            KeyguardStatusBarView.AnonymousClass1.this.lambda$onPreDraw$0$KeyguardStatusBarView$1();
                        }
                    }).start();
                } else {
                    KeyguardStatusBarView.this.mMultiUserSwitch.setAlpha(0.0f);
                    KeyguardStatusBarView.this.mMultiUserSwitch.animate().alpha(1.0f).setDuration(300).setStartDelay(200).setInterpolator(Interpolators.ALPHA_IN);
                }
                return true;
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onPreDraw$0 */
            public /* synthetic */ void lambda$onPreDraw$0$KeyguardStatusBarView$1() {
                KeyguardStatusBarView.this.mMultiUserSwitch.setAlpha(1.0f);
                KeyguardStatusBarView.this.getOverlay().remove(KeyguardStatusBarView.this.mMultiUserSwitch);
            }
        });
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        super.setVisibility(i);
        if (i != 0) {
            this.mSystemIconsContainer.animate().cancel();
            this.mSystemIconsContainer.setTranslationX(0.0f);
            this.mMultiUserSwitch.animate().cancel();
            this.mMultiUserSwitch.setAlpha(1.0f);
            return;
        }
        updateVisibilities();
        updateSystemIconsLayoutParams();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onThemeChanged() {
        this.mBatteryView.setColorsFromContext(((RelativeLayout) this).mContext);
        updateIconsAndTextColors();
        ((UserInfoControllerImpl) Dependency.get(UserInfoController.class)).onDensityOrFontScaleChanged();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        loadDimens();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() {
        this.mCarrierLabel.setTextAppearance(Utils.getThemeAttr(((RelativeLayout) this).mContext, 16842818));
        onThemeChanged();
        this.mBatteryView.updatePercentView();
    }

    private void updateIconsAndTextColors() {
        int i;
        int colorAttrDefaultColor = Utils.getColorAttrDefaultColor(((RelativeLayout) this).mContext, C0002R$attr.wallpaperTextColor);
        Context context = ((RelativeLayout) this).mContext;
        if (((double) Color.luminance(colorAttrDefaultColor)) < 0.5d) {
            i = C0004R$color.dark_mode_icon_color_single_tone;
        } else {
            i = C0004R$color.light_mode_icon_color_single_tone;
        }
        int colorStateListDefaultColor = Utils.getColorStateListDefaultColor(context, i);
        float f = colorAttrDefaultColor == -1 ? 0.0f : 1.0f;
        this.mCarrierLabel.setTextColor(colorStateListDefaultColor);
        StatusBarIconController.TintedIconManager tintedIconManager = this.mIconManager;
        if (tintedIconManager != null) {
            tintedIconManager.setTint(colorStateListDefaultColor);
        }
        applyDarkness(C0008R$id.battery, this.mEmptyRect, f, colorStateListDefaultColor);
        applyDarkness(C0008R$id.clock, this.mEmptyRect, f, colorStateListDefaultColor);
    }

    private void applyDarkness(int i, Rect rect, float f, int i2) {
        View findViewById = findViewById(i);
        if (findViewById instanceof DarkIconDispatcher.DarkReceiver) {
            ((DarkIconDispatcher.DarkReceiver) findViewById).onDarkChanged(rect, f, i2);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardStatusBarView:");
        printWriter.println("  mBatteryCharging: " + this.mBatteryCharging);
        printWriter.println("  mKeyguardUserSwitcherShowing: " + this.mKeyguardUserSwitcherShowing);
        printWriter.println("  mBatteryListening: " + this.mBatteryListening);
        printWriter.println("  mLayoutState: " + this.mLayoutState);
        BatteryMeterView batteryMeterView = this.mBatteryView;
        if (batteryMeterView != null) {
            batteryMeterView.dump(fileDescriptor, printWriter, strArr);
        }
    }

    private void updateOrientationAndContentPosition(int i) {
        if (this.mLastOrientation != i) {
            this.mLastOrientation = i;
            int i2 = i == 1 ? this.mContentPaddintTop : 0;
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
            marginLayoutParams.setMarginsRelative(marginLayoutParams.getMarginStart(), i2, marginLayoutParams.getMarginEnd(), marginLayoutParams.bottomMargin);
        }
    }

    /* access modifiers changed from: protected */
    public void updateStatusBarPadding() {
        int i;
        boolean isHolePunchCutoutHide = OpUtils.isHolePunchCutoutHide(getContext());
        boolean z = true;
        if (!OpUtils.isSupportHolePunchFrontCam() || getContext().getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        if (isHolePunchCutoutHide) {
            i = 0;
        } else {
            i = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.op_status_bar_cust_padding_top, 1080);
        }
        setPadding(getPaddingLeft(), i, getPaddingRight(), 0);
        int dimensionPixelSize = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.status_bar_padding_start, 1080);
        int dimensionPixelSize2 = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.status_bar_padding_end, 1080);
        if (isHolePunchCutoutHide) {
            dimensionPixelSize = dimensionPixelSize2;
        }
        int i2 = z ? dimensionPixelSize2 : dimensionPixelSize;
        if (z) {
            dimensionPixelSize2 = dimensionPixelSize;
        }
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mCarrierLabel.getLayoutParams();
        layoutParams.setMarginStart(i2);
        this.mCarrierLabel.setLayoutParams(layoutParams);
        View findViewById = findViewById(C0008R$id.status_bar_contents);
        if (findViewById != null) {
            findViewById.setPaddingRelative(i2, 0, 0, 0);
        }
        this.mSystemIconsContainer.setPaddingRelative(0, 0, dimensionPixelSize2, 0);
        requestLayout();
    }
}
