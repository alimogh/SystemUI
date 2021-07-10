package com.android.systemui.statusbar.phone;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.ColorUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.assist.AssistHandleViewController;
import com.android.systemui.model.SysUiState;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsOnboarding;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.RegionSamplingHelper;
import com.android.systemui.statusbar.policy.DeadZone;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.util.Utils;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.statusbar.phone.OpEdgeNavGestureHandler;
import com.oneplus.util.OpNavBarUtils;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;
public class NavigationBarView extends FrameLayout implements NavigationModeController.ModeChangedListener {
    static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private final int COLOR_BACKGROUND_DARK;
    private final int COLOR_BACKGROUND_LIGHT;
    private final int COLOR_KEY_DARK;
    private final int COLOR_KEY_LIGHT;
    private final int COLOR_KEY_TRANSPARENT;
    private final Region mActiveRegion = new Region();
    private Rect mBackButtonBounds = new Rect();
    private KeyButtonDrawable mBackIcon;
    private int mBackgroundColor = 0;
    private final NavigationBarTransitions mBarTransitions;
    private final SparseArray<ButtonDispatcher> mButtonDispatchers = new SparseArray<>();
    private Configuration mConfiguration;
    private final ContextualButtonGroup mContextualButtonGroup;
    private int mCurrentRotation = -1;
    View mCurrentView = null;
    private final DeadZone mDeadZone;
    private boolean mDeadZoneConsuming = false;
    int mDisabledFlags = 0;
    private KeyButtonDrawable mDockedIcon;
    private final Consumer<Boolean> mDockedListener = new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarView$3_rm_LYAhHXvCBhrsX10ry5w8OA
        @Override // java.util.function.Consumer
        public final void accept(Object obj) {
            NavigationBarView.this.lambda$new$2$NavigationBarView((Boolean) obj);
        }
    };
    private boolean mDockedStackExists;
    private EdgeBackGestureHandler mEdgeBackGestureHandler;
    private FloatingRotationButton mFloatingRotationButton;
    private HeadsUpManager mHeadsUpManager;
    private boolean mHideNavBar = false;
    private Rect mHomeButtonBounds = new Rect();
    private KeyButtonDrawable mHomeDefaultIcon;
    private View mHorizontal;
    private boolean mImeShow = false;
    private final View.OnClickListener mImeSwitcherClickListener = new View.OnClickListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarView.1
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            ((InputMethodManager) ((FrameLayout) NavigationBarView.this).mContext.getSystemService(InputMethodManager.class)).showInputMethodPickerFromSystem(true, NavigationBarView.this.getContext().getDisplayId());
        }
    };
    private boolean mImeVisible;
    private boolean mInCarMode = false;
    private boolean mIsCTSOn = false;
    public boolean mIsCustomNavBar = OpNavBarUtils.isSupportCustomNavBar();
    private boolean mIsHideNavBarOn = false;
    private boolean mIsImmersiveSticky = false;
    private boolean mIsInBrickMode = false;
    private boolean mIsLightBar = false;
    private boolean mIsPanelViewFullExpanded = false;
    private boolean mIsVertical = false;
    private boolean mKeyguardShow = false;
    private int mLastButtonColor = 0;
    private int mLastRippleColor = 0;
    private boolean mLayoutTransitionsEnabled = true;
    private int mNavBarMode;
    private final int mNavColorSampleMargin;
    int mNavigationIconHints = 0;
    private NavigationBarInflaterView mNavigationInflaterView;
    private final ViewTreeObserver.OnComputeInternalInsetsListener mOnComputeInternalInsetsListener = new ViewTreeObserver.OnComputeInternalInsetsListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarView$khIxhJwBd7pJnFFXnq8zupcHrv8
        public final void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
            NavigationBarView.this.lambda$new$0$NavigationBarView(internalInsetsInfo);
        }
    };
    private OnVerticalChangedListener mOnVerticalChangedListener;
    private final OpEdgeNavGestureHandler mOpEdgeNavGestureHandler;
    private Rect mOrientedHandleSamplingRegion;
    private final OverviewProxyService mOverviewProxyService;
    private NotificationPanelViewController mPanelView;
    private final PluginManager mPluginManager;
    private final View.AccessibilityDelegate mQuickStepAccessibilityDelegate = new View.AccessibilityDelegate() { // from class: com.android.systemui.statusbar.phone.NavigationBarView.2
        private AccessibilityNodeInfo.AccessibilityAction mToggleOverviewAction;

        @Override // android.view.View.AccessibilityDelegate
        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
            if (this.mToggleOverviewAction == null) {
                this.mToggleOverviewAction = new AccessibilityNodeInfo.AccessibilityAction(C0008R$id.action_toggle_overview, NavigationBarView.this.getContext().getString(C0015R$string.quick_step_accessibility_toggle_overview));
            }
            accessibilityNodeInfo.addAction(this.mToggleOverviewAction);
        }

        @Override // android.view.View.AccessibilityDelegate
        public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
            if (i != C0008R$id.action_toggle_overview) {
                return super.performAccessibilityAction(view, i, bundle);
            }
            ((Recents) Dependency.get(Recents.class)).toggleRecentApps();
            return true;
        }
    };
    private KeyButtonDrawable mRecentIcon;
    private Rect mRecentsButtonBounds = new Rect();
    private RecentsOnboarding mRecentsOnboarding;
    private RegionSamplingHelper mRegionSamplingHelper;
    private Rect mRotationButtonBounds = new Rect();
    private RotationButtonController mRotationButtonController;
    private Rect mSamplingBounds = new Rect();
    private boolean mScreenOn = true;
    private ScreenPinningNotify mScreenPinningNotify;
    private boolean mShowNavKey = false;
    private final SysUiState mSysUiFlagContainer;
    private Configuration mTmpLastConfiguration;
    private int[] mTmpPosition = new int[2];
    private final NavTransitionListener mTransitionListener = new NavTransitionListener();
    private boolean mUseCarModeUi = false;
    private View mVertical;
    private boolean mWakeAndUnlocking;

    public interface OnVerticalChangedListener {
        void onVerticalChanged(boolean z);
    }

    private static String visibilityToString(int i) {
        return i != 4 ? i != 8 ? "VISIBLE" : "GONE" : "INVISIBLE";
    }

    /* access modifiers changed from: private */
    public class NavTransitionListener implements LayoutTransition.TransitionListener {
        private boolean mBackTransitioning;
        private long mDuration;
        private boolean mHomeAppearing;
        private TimeInterpolator mInterpolator;
        private long mStartDelay;

        private NavTransitionListener() {
        }

        @Override // android.animation.LayoutTransition.TransitionListener
        public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            if (view.getId() == C0008R$id.back) {
                this.mBackTransitioning = true;
            } else if (view.getId() == C0008R$id.home && i == 2) {
                this.mHomeAppearing = true;
                this.mStartDelay = layoutTransition.getStartDelay(i);
                this.mDuration = layoutTransition.getDuration(i);
                this.mInterpolator = layoutTransition.getInterpolator(i);
            }
        }

        @Override // android.animation.LayoutTransition.TransitionListener
        public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            if (view.getId() == C0008R$id.back) {
                this.mBackTransitioning = false;
            } else if (view.getId() == C0008R$id.home && i == 2) {
                this.mHomeAppearing = false;
            }
        }

        public void onBackAltCleared() {
            ButtonDispatcher backButton = NavigationBarView.this.getBackButton();
            if (!this.mBackTransitioning && backButton.getVisibility() == 0 && this.mHomeAppearing && NavigationBarView.this.getHomeButton().getAlpha() == 0.0f) {
                NavigationBarView.this.getBackButton().setAlpha(0.0f);
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(backButton, "alpha", 0.0f, 1.0f);
                ofFloat.setStartDelay(this.mStartDelay);
                ofFloat.setDuration(this.mDuration);
                ofFloat.setInterpolator(this.mInterpolator);
                ofFloat.start();
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$NavigationBarView(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        if (!this.mEdgeBackGestureHandler.isHandlingGestures() || this.mImeVisible) {
            internalInsetsInfo.setTouchableInsets(0);
            return;
        }
        internalInsetsInfo.setTouchableInsets(3);
        ButtonDispatcher imeSwitchButton = getImeSwitchButton();
        if (imeSwitchButton.getVisibility() == 0) {
            int[] iArr = new int[2];
            View currentView = imeSwitchButton.getCurrentView();
            currentView.getLocationInWindow(iArr);
            internalInsetsInfo.touchableRegion.set(iArr[0], iArr[1], iArr[0] + currentView.getWidth(), iArr[1] + currentView.getHeight());
            return;
        }
        internalInsetsInfo.touchableRegion.setEmpty();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v8, resolved type: com.android.systemui.statusbar.phone.FloatingRotationButton */
    /* JADX WARN: Multi-variable type inference failed */
    public NavigationBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        int i;
        int addListener = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(this);
        this.mNavBarMode = addListener;
        boolean isGesturalMode = QuickStepContract.isGesturalMode(addListener);
        this.mSysUiFlagContainer = (SysUiState) Dependency.get(SysUiState.class);
        this.mPluginManager = (PluginManager) Dependency.get(PluginManager.class);
        this.mContextualButtonGroup = new ContextualButtonGroup(C0008R$id.menu_container);
        ContextualButton contextualButton = new ContextualButton(C0008R$id.ime_switcher, C0006R$drawable.ic_ime_switcher_default);
        int i2 = C0008R$id.rotate_suggestion;
        if (this.mIsCustomNavBar) {
            i = C0006R$drawable.ic_sysbar_rotate_button2;
        } else {
            i = C0006R$drawable.ic_sysbar_rotate_button;
        }
        RotationContextButton rotationContextButton = new RotationContextButton(i2, i);
        ContextualButton contextualButton2 = new ContextualButton(C0008R$id.accessibility_button, C0006R$drawable.ic_sysbar_accessibility_button);
        this.mContextualButtonGroup.addButton(contextualButton);
        if (!isGesturalMode) {
            this.mContextualButtonGroup.addButton(rotationContextButton);
        }
        this.mContextualButtonGroup.addButton(contextualButton2);
        OverviewProxyService overviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        this.mOverviewProxyService = overviewProxyService;
        this.mRecentsOnboarding = new RecentsOnboarding(context, overviewProxyService);
        FloatingRotationButton floatingRotationButton = new FloatingRotationButton(context);
        this.mFloatingRotationButton = floatingRotationButton;
        this.mRotationButtonController = new RotationButtonController(context, C0016R$style.RotateButtonCCWStart90, !isGesturalMode ? rotationContextButton : floatingRotationButton);
        this.mConfiguration = new Configuration();
        this.mTmpLastConfiguration = new Configuration();
        this.mConfiguration.updateFrom(context.getResources().getConfiguration());
        this.mScreenPinningNotify = new ScreenPinningNotify(((FrameLayout) this).mContext);
        this.mBarTransitions = new NavigationBarTransitions(this, (CommandQueue) Dependency.get(CommandQueue.class));
        SparseArray<ButtonDispatcher> sparseArray = this.mButtonDispatchers;
        int i3 = C0008R$id.back;
        sparseArray.put(i3, new ButtonDispatcher(i3));
        SparseArray<ButtonDispatcher> sparseArray2 = this.mButtonDispatchers;
        int i4 = C0008R$id.home;
        sparseArray2.put(i4, new ButtonDispatcher(i4));
        SparseArray<ButtonDispatcher> sparseArray3 = this.mButtonDispatchers;
        int i5 = C0008R$id.home_handle;
        sparseArray3.put(i5, new ButtonDispatcher(i5));
        SparseArray<ButtonDispatcher> sparseArray4 = this.mButtonDispatchers;
        int i6 = C0008R$id.recent_apps;
        sparseArray4.put(i6, new ButtonDispatcher(i6));
        this.mButtonDispatchers.put(C0008R$id.ime_switcher, contextualButton);
        this.mButtonDispatchers.put(C0008R$id.accessibility_button, contextualButton2);
        this.mButtonDispatchers.put(C0008R$id.rotate_suggestion, rotationContextButton);
        this.mButtonDispatchers.put(C0008R$id.menu_container, this.mContextualButtonGroup);
        this.mDeadZone = new DeadZone(this);
        this.mNavColorSampleMargin = getResources().getDimensionPixelSize(C0005R$dimen.navigation_handle_sample_horizontal_margin);
        this.mEdgeBackGestureHandler = new EdgeBackGestureHandler(context, this.mOverviewProxyService, this.mSysUiFlagContainer, this.mPluginManager, new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$WrUd8iBVzCnkNGlDjVh6Yvbf6CM
            @Override // java.lang.Runnable
            public final void run() {
                NavigationBarView.this.updateStates();
            }
        });
        this.mOpEdgeNavGestureHandler = new OpEdgeNavGestureHandler(context, this.mOverviewProxyService, this.mSysUiFlagContainer);
        if (OpNavBarUtils.isSupportHideNavBar()) {
            SparseArray<ButtonDispatcher> sparseArray5 = this.mButtonDispatchers;
            int i7 = C0008R$id.nav;
            sparseArray5.put(i7, new ButtonDispatcher(i7));
        }
        this.COLOR_BACKGROUND_LIGHT = context.getColor(C0004R$color.op_nav_bar_background_light);
        this.COLOR_BACKGROUND_DARK = context.getColor(C0004R$color.op_nav_bar_background_dark);
        context.getColor(C0004R$color.op_nav_bar_background_transparent);
        this.COLOR_KEY_LIGHT = context.getColor(C0004R$color.op_nav_bar_key_light);
        this.COLOR_KEY_DARK = context.getColor(C0004R$color.op_nav_bar_key_dark);
        this.COLOR_KEY_TRANSPARENT = context.getColor(C0004R$color.op_nav_bar_key_transparent);
    }

    public NavigationBarTransitions getBarTransitions() {
        return this.mBarTransitions;
    }

    public LightBarTransitionsController getLightTransitionsController() {
        return this.mBarTransitions.getLightTransitionsController();
    }

    public void setComponents(NotificationPanelViewController notificationPanelViewController) {
        this.mPanelView = notificationPanelViewController;
        updatePanelSystemUiStateFlags();
    }

    public void setOnVerticalChangedListener(OnVerticalChangedListener onVerticalChangedListener) {
        this.mOnVerticalChangedListener = onVerticalChangedListener;
        notifyVerticalChangedListener(this.mIsVertical);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (QuickStepContract.isGesturalMode(this.mNavBarMode) && this.mImeVisible && motionEvent.getAction() == 0) {
            SysUiStatsLog.write(304, (int) motionEvent.getX(), (int) motionEvent.getY());
        }
        return shouldDeadZoneConsumeTouchEvents(motionEvent) || super.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        shouldDeadZoneConsumeTouchEvents(motionEvent);
        return super.onTouchEvent(motionEvent);
    }

    /* access modifiers changed from: package-private */
    public void onTransientStateChanged(boolean z) {
        this.mEdgeBackGestureHandler.onNavBarTransientStateChanged(z);
        this.mOpEdgeNavGestureHandler.onNavBarTransientStateChanged(z);
    }

    /* access modifiers changed from: package-private */
    public void onBarTransition(int i) {
        if (i == 4) {
            this.mRegionSamplingHelper.stop();
            getLightTransitionsController().setIconsDark(false, true);
            return;
        }
        this.mRegionSamplingHelper.start(this.mSamplingBounds);
    }

    private boolean shouldDeadZoneConsumeTouchEvents(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mDeadZoneConsuming = false;
        }
        if (!this.mDeadZone.onTouchEvent(motionEvent) && !this.mDeadZoneConsuming) {
            return false;
        }
        if (actionMasked == 0) {
            setSlippery(true);
            this.mDeadZoneConsuming = true;
        } else if (actionMasked == 1 || actionMasked == 3) {
            updateSlippery();
            this.mDeadZoneConsuming = false;
        }
        return true;
    }

    public void abortCurrentGesture() {
        getHomeButton().abortCurrentGesture();
    }

    public View getCurrentView() {
        return this.mCurrentView;
    }

    public RotationButtonController getRotationButtonController() {
        return this.mRotationButtonController;
    }

    public ButtonDispatcher getNavButton() {
        return this.mButtonDispatchers.get(C0008R$id.nav);
    }

    public ButtonDispatcher getRecentsButton() {
        return this.mButtonDispatchers.get(C0008R$id.recent_apps);
    }

    public ButtonDispatcher getBackButton() {
        return this.mButtonDispatchers.get(C0008R$id.back);
    }

    public ButtonDispatcher getHomeButton() {
        return this.mButtonDispatchers.get(C0008R$id.home);
    }

    public ButtonDispatcher getImeSwitchButton() {
        return this.mButtonDispatchers.get(C0008R$id.ime_switcher);
    }

    public ButtonDispatcher getAccessibilityButton() {
        return this.mButtonDispatchers.get(C0008R$id.accessibility_button);
    }

    public RotationContextButton getRotateSuggestionButton() {
        return (RotationContextButton) this.mButtonDispatchers.get(C0008R$id.rotate_suggestion);
    }

    public ButtonDispatcher getHomeHandle() {
        return this.mButtonDispatchers.get(C0008R$id.home_handle);
    }

    public SparseArray<ButtonDispatcher> getButtonDispatchers() {
        return this.mButtonDispatchers;
    }

    public boolean isRecentsButtonVisible() {
        return getRecentsButton().getVisibility() == 0;
    }

    public boolean isOverviewEnabled() {
        return (this.mDisabledFlags & 16777216) == 0;
    }

    public boolean isQuickStepSwipeUpEnabled() {
        return this.mOverviewProxyService.shouldShowSwipeUpUI() && isOverviewEnabled();
    }

    private void reloadNavIcons() {
        updateIcons(Configuration.EMPTY);
    }

    public void setHideNavBarOn(boolean z) {
        this.mIsHideNavBarOn = z;
        if (z) {
            updateButtonColor(this.COLOR_KEY_TRANSPARENT, -1);
        } else {
            notifyNavBarColorChange(this.mBackgroundColor);
        }
    }

    public void updateNavButtonState(boolean z) {
        this.mShowNavKey = z;
        if (getNavButton() != null) {
            getNavButton().setVisibility(showNavKey() ? 0 : 4);
        }
    }

    private boolean isLightColor(int i) {
        return !isLegible(-1, i);
    }

    private boolean isDarkColor(int i) {
        return !isLegible(-16777216, i);
    }

    private boolean isLegible(int i, int i2) {
        return ColorUtils.calculateContrast(i, ColorUtils.setAlphaComponent(i2, 255)) >= 2.0d;
    }

    public void notifyNavBarColorChange(int i) {
        notifyNavBarColorChange(i, false);
    }

    private void notifyNavBarColorChange(int i, boolean z) {
        int mode = this.mBarTransitions.getMode();
        if (DEBUG) {
            Log.d("StatusBar/NavBarView", "notifyNavBarColorChange barMode: " + mode + ", bgcolor: 0x" + Integer.toHexString(i) + ", mImeShow: " + this.mImeShow + ", mKeyguardShow: " + this.mKeyguardShow + ", expanded: " + z + ", hasPinnedHeadsUp(): " + hasPinnedHeadsUp() + ", mDockedStackExists: " + this.mDockedStackExists + ", mIsLightBar: " + this.mIsLightBar + ", isScreenCompat: " + OpUtils.isScreenCompat() + ", isScreenSaverOn: " + isScreenSaverOn());
        }
        if (this.mIsCTSOn != OpUtils.isCTS()) {
            boolean isCTS = OpUtils.isCTS();
            this.mIsCTSOn = isCTS;
            updateMainIcons(isCTS);
        }
        if (OpUtils.isCTS()) {
            updateButtonColor(-1728053248, -16777216);
            return;
        }
        if (getNavButton() != null) {
            getNavButton().setVisibility(showNavKey() ? 0 : 4);
        }
        if (this.mImeShow) {
            if (mode == 2) {
                updateButtonColor(this.COLOR_KEY_TRANSPARENT, -1);
            } else {
                updateButtonColor(this.COLOR_KEY_LIGHT, -16777216);
            }
        } else if ((this.mKeyguardShow || (z && !hasPinnedHeadsUp() && !this.mDockedStackExists)) && !this.mIsLightBar && !isScreenSaverOn() && mode != 4) {
            updateButtonColor(this.COLOR_KEY_TRANSPARENT, -1);
        } else {
            this.mBackgroundColor = i;
            if (this.mDockedStackExists) {
                updateButtonColor(this.COLOR_KEY_LIGHT, -16777216);
            } else if (mode == 1 || mode == 2) {
                updateButtonColor(this.COLOR_KEY_TRANSPARENT, -1);
            } else if (mode == 4) {
                if (OpUtils.isScreenCompat()) {
                    updateButtonColor(this.COLOR_KEY_DARK, -1);
                } else {
                    updateButtonColor(this.COLOR_KEY_LIGHT, -16777216);
                }
            } else if (mode == 3 && isScreenSaverOn()) {
                updateButtonColor(this.COLOR_KEY_LIGHT, -16777216);
            } else if (this.mIsHideNavBarOn && !OpUtils.isHomeApp()) {
                updateButtonColor(this.COLOR_KEY_TRANSPARENT, -1);
            } else if (OpUtils.isScreenCompat()) {
                updateButtonColor(this.COLOR_KEY_DARK, -1);
            } else if (this.mIsLightBar) {
                updateButtonColor(this.COLOR_KEY_LIGHT, -16777216);
            } else {
                int i2 = this.mBackgroundColor;
                if (i2 == 0) {
                    updateButtonColor(this.COLOR_KEY_TRANSPARENT, -1);
                } else if (i2 == this.COLOR_BACKGROUND_LIGHT) {
                    updateButtonColor(this.COLOR_KEY_LIGHT, -16777216);
                } else if (i2 == this.COLOR_BACKGROUND_DARK || i2 == -16777216) {
                    updateButtonColor(this.COLOR_KEY_DARK, -1);
                } else {
                    applyAppCustomColor();
                }
            }
        }
    }

    private void applyAppCustomColor() {
        if (isLightColor(this.mBackgroundColor)) {
            updateButtonColor(this.COLOR_KEY_LIGHT, -16777216);
        } else if (isDarkColor(this.mBackgroundColor)) {
            updateButtonColor(this.COLOR_KEY_DARK, -1);
        } else {
            updateButtonColor(this.COLOR_KEY_TRANSPARENT, -1);
        }
    }

    private void updateButtonColor(int i, int i2) {
        updateButtonColor(i, i2, false);
    }

    private void updateButtonColor(int i, int i2, boolean z) {
        if (!(i == this.mLastButtonColor && i2 == this.mLastRippleColor && !z)) {
            if (DEBUG) {
                Log.d("StatusBar/NavBarView", "updateButtonColor buttonColor=0x" + Integer.toHexString(i) + ", rippleColor=0x" + Integer.toHexString(i2) + ", force=" + z + ", caller: " + Debug.getCallers(8));
            }
            this.mLastButtonColor = i;
            this.mLastRippleColor = i2;
            for (int i3 = 0; i3 < this.mButtonDispatchers.size(); i3++) {
                ArrayList<View> views = this.mButtonDispatchers.valueAt(i3).getViews();
                int size = views.size();
                for (int i4 = 0; i4 < size; i4++) {
                    if (views.get(i4) instanceof KeyButtonView) {
                        KeyButtonView keyButtonView = (KeyButtonView) views.get(i4);
                        keyButtonView.updateThemeColor(i);
                        keyButtonView.setRippleColor(i2);
                    }
                }
            }
            postInvalidate();
        }
    }

    private boolean showNavKey() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("StatusBar/NavBarView", "showNavKey mShowNavKey: " + this.mShowNavKey + " isHomeApp: " + OpUtils.isHomeApp() + " isScreenCompat: " + OpUtils.isScreenCompat() + " mImeShow: " + this.mImeShow + " mKeyguardShow: " + this.mKeyguardShow + " isMultiWindow: " + this.mDockedStackExists + " isScreenSaveron: " + isScreenSaverOn() + " isImmersiveSticky: " + this.mIsImmersiveSticky + " isSystemUI: " + OpUtils.isSystemUI() + " isInBrickMode: " + this.mIsInBrickMode);
        }
        return this.mShowNavKey && !OpUtils.isHomeApp() && !OpUtils.isScreenCompat() && !this.mImeShow && !this.mKeyguardShow && !isScreenSaverOn() && !this.mIsImmersiveSticky && !this.mDockedStackExists && !OpUtils.isSystemUI() && !this.mIsInBrickMode;
    }

    public void onShowKeyguard(boolean z) {
        if (this.mKeyguardShow != z) {
            this.mKeyguardShow = z;
            if (OpNavBarUtils.isSupportCustomNavBar() && !QuickStepContract.isGesturalMode(this.mNavBarMode)) {
                int i = 0;
                boolean z2 = this.mKeyguardShow && isScreenSaverOn();
                if (!z) {
                    i = this.mBackgroundColor;
                }
                if (!z2) {
                    notifyNavBarColorChange(i);
                }
            }
        }
    }

    public void onImmersiveSticky(boolean z) {
        if (DEBUG) {
            Log.d("StatusBar/NavBarView", "onImmersiveSticky " + z);
        }
        this.mIsImmersiveSticky = z;
        if (getNavButton() != null) {
            getNavButton().setVisibility(showNavKey() ? 0 : 4);
        }
    }

    public void setLightBar(boolean z) {
        if (this.mIsLightBar != z) {
            Log.d("StatusBar/NavBarView", "setLightBar to " + z);
        }
        this.mIsLightBar = z;
        if (this.mIsCustomNavBar) {
            notifyNavBarColorChange(this.mBackgroundColor);
        }
    }

    private boolean hasPinnedHeadsUp() {
        HeadsUpManager headsUpManager = this.mHeadsUpManager;
        return headsUpManager != null && headsUpManager.hasPinnedHeadsUp();
    }

    public void refreshButtonColor() {
        updateButtonColor(this.mLastButtonColor, this.mLastRippleColor, true);
    }

    public boolean isScreenSaverOn() {
        return ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDreaming() && ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isScreenOn();
    }

    private void updateIcons(Configuration configuration) {
        KeyButtonDrawable keyButtonDrawable;
        KeyButtonDrawable keyButtonDrawable2;
        boolean z = true;
        boolean z2 = configuration.orientation != this.mConfiguration.orientation;
        boolean z3 = configuration.densityDpi != this.mConfiguration.densityDpi;
        if (configuration.getLayoutDirection() == this.mConfiguration.getLayoutDirection()) {
            z = false;
        }
        if (z2 || z3) {
            if (this.mIsCustomNavBar) {
                keyButtonDrawable2 = getDrawable(C0006R$drawable.ic_sysbar_docked2);
            } else {
                keyButtonDrawable2 = getDrawable(C0006R$drawable.ic_sysbar_docked);
            }
            this.mDockedIcon = keyButtonDrawable2;
            this.mHomeDefaultIcon = getHomeDrawable();
        }
        if (z3 || z) {
            if (this.mIsCustomNavBar) {
                keyButtonDrawable = getDrawable(C0006R$drawable.ic_sysbar_recent2);
            } else {
                keyButtonDrawable = getDrawable(C0006R$drawable.ic_sysbar_recent);
            }
            this.mRecentIcon = keyButtonDrawable;
            this.mContextualButtonGroup.updateIcons();
        }
        if (z2 || z3 || z) {
            this.mBackIcon = getBackDrawable();
        }
    }

    private void updateMainIcons(boolean z) {
        KeyButtonDrawable keyButtonDrawable;
        this.mHomeDefaultIcon = getHomeDrawable();
        this.mBackIcon = getBackDrawable();
        if (z) {
            this.mRecentIcon = getDrawable(C0006R$drawable.ic_sysbar_recent);
        } else {
            if (this.mIsCustomNavBar) {
                keyButtonDrawable = getDrawable(C0006R$drawable.ic_sysbar_recent2);
            } else {
                keyButtonDrawable = getDrawable(C0006R$drawable.ic_sysbar_recent);
            }
            this.mRecentIcon = keyButtonDrawable;
        }
        this.mContextualButtonGroup.updateIcons();
        getHomeButton().setImageDrawable(this.mHomeDefaultIcon);
        getBackButton().setImageDrawable(this.mBackIcon);
        getRecentsButton().setImageDrawable(this.mRecentIcon);
    }

    public KeyButtonDrawable getBackDrawable() {
        KeyButtonDrawable drawable = getDrawable(getBackDrawableRes());
        orientBackButton(drawable);
        return drawable;
    }

    public int getBackDrawableRes() {
        if (!OpNavBarUtils.isSupportCustomNavBar() || OpUtils.isCTS()) {
            return chooseNavigationIconDrawableRes(C0006R$drawable.ic_sysbar_back, C0006R$drawable.ic_sysbar_back_quick_step);
        }
        return chooseNavigationIconDrawableRes(C0006R$drawable.ic_sysbar_back2, C0006R$drawable.ic_sysbar_back_quick_step2);
    }

    public KeyButtonDrawable getHomeDrawable() {
        KeyButtonDrawable keyButtonDrawable;
        KeyButtonDrawable keyButtonDrawable2;
        boolean z = true;
        if (!OpNavBarUtils.isSupportCustomNavBar() || OpUtils.isCTS()) {
            if (!this.mOverviewProxyService.shouldShowSwipeUpUI() || QuickStepContract.isLegacyMode(this.mNavBarMode)) {
                z = false;
            }
            if (z) {
                keyButtonDrawable = getDrawable(C0006R$drawable.ic_sysbar_home_quick_step);
            } else {
                keyButtonDrawable = getDrawable(C0006R$drawable.ic_sysbar_home);
            }
            orientHomeButton(keyButtonDrawable);
            return keyButtonDrawable;
        }
        if (!this.mOverviewProxyService.shouldShowSwipeUpUI() || QuickStepContract.isLegacyMode(this.mNavBarMode)) {
            z = false;
        }
        if (z) {
            keyButtonDrawable2 = getDrawable(C0006R$drawable.ic_sysbar_home_quick_step2);
        } else {
            keyButtonDrawable2 = getDrawable(C0006R$drawable.ic_sysbar_home2);
        }
        orientHomeButton(keyButtonDrawable2);
        return keyButtonDrawable2;
    }

    private void orientBackButton(KeyButtonDrawable keyButtonDrawable) {
        boolean z = (this.mNavigationIconHints & 1) != 0;
        boolean z2 = this.mConfiguration.getLayoutDirection() == 1;
        float f = 0.0f;
        float f2 = z ? -90.0f : 0.0f;
        if (DEBUG) {
            Log.d("StatusBar/NavBarView", "orientBackBtn: useAltBack=" + z + ", isRtl=" + z2 + ", degrees=" + keyButtonDrawable.getRotation() + "->" + f2);
        }
        if (keyButtonDrawable.getRotation() != f2) {
            if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
                keyButtonDrawable.setRotation(f2);
                return;
            }
            if (!this.mOverviewProxyService.shouldShowSwipeUpUI() && !this.mIsVertical && z) {
                f = -getResources().getDimension(C0005R$dimen.navbar_back_button_ime_offset);
            }
            ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(keyButtonDrawable, PropertyValuesHolder.ofFloat(KeyButtonDrawable.KEY_DRAWABLE_ROTATE, f2), PropertyValuesHolder.ofFloat(KeyButtonDrawable.KEY_DRAWABLE_TRANSLATE_Y, f));
            ofPropertyValuesHolder.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofPropertyValuesHolder.setDuration(0L);
            ofPropertyValuesHolder.start();
        }
    }

    private void orientHomeButton(KeyButtonDrawable keyButtonDrawable) {
        keyButtonDrawable.setRotation(this.mIsVertical ? 90.0f : 0.0f);
    }

    private int chooseNavigationIconDrawableRes(int i, int i2) {
        return this.mOverviewProxyService.shouldShowSwipeUpUI() && !QuickStepContract.isLegacyMode(this.mNavBarMode) ? i2 : i;
    }

    private KeyButtonDrawable getDrawable(int i) {
        return KeyButtonDrawable.create(((FrameLayout) this).mContext, i, true);
    }

    public void onScreenStateChanged(boolean z) {
        this.mScreenOn = z;
        if (!z) {
            this.mRegionSamplingHelper.stop();
        } else if (Utils.isGesturalModeOnDefaultDisplay(getContext(), this.mNavBarMode)) {
            this.mRegionSamplingHelper.start(this.mSamplingBounds);
        }
    }

    public void setWindowVisible(boolean z) {
        this.mRegionSamplingHelper.setWindowVisible(z);
        this.mRotationButtonController.onNavigationBarWindowVisibilityChange(z);
    }

    @Override // android.view.View
    public void setLayoutDirection(int i) {
        reloadNavIcons();
        this.mNavigationInflaterView.onLikelyDefaultLayoutChange();
    }

    public void setNavigationIconHints(int i) {
        if (i != this.mNavigationIconHints) {
            boolean z = false;
            boolean z2 = (i & 1) != 0;
            if ((this.mNavigationIconHints & 1) != 0) {
                z = true;
            }
            if (z2 != z) {
                onImeVisibilityChanged(z2);
            }
            this.mNavigationIconHints = i;
            updateNavButtonIcons();
            this.mImeShow = z2;
            if (OpNavBarUtils.isSupportCustomNavBar() && !QuickStepContract.isGesturalMode(this.mNavBarMode)) {
                if (z2) {
                    notifyNavBarColorChange(this.COLOR_KEY_LIGHT);
                } else {
                    notifyNavBarColorChange(this.mBackgroundColor);
                }
            }
        }
    }

    private void onImeVisibilityChanged(boolean z) {
        if (!z) {
            this.mTransitionListener.onBackAltCleared();
        }
        this.mImeVisible = z;
        this.mRotationButtonController.getRotationButton().setCanShowRotationButton(!this.mImeVisible);
        changeHomeHandleAlpha(false);
    }

    public void setDisabledFlags(int i) {
        if (this.mDisabledFlags != i) {
            boolean isOverviewEnabled = isOverviewEnabled();
            this.mDisabledFlags = i;
            if (!isOverviewEnabled && isOverviewEnabled()) {
                reloadNavIcons();
            }
            updateNavButtonIcons();
            updateSlippery();
            setUpSwipeUpOnboarding(isQuickStepSwipeUpEnabled());
            updateDisabledSystemUiStateFlags();
        }
    }

    public void updateNavButtonIcons() {
        LayoutTransition layoutTransition;
        int i;
        int i2;
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        int i3 = 0;
        boolean z = phoneStatusBar != null && phoneStatusBar.isHideImeBackAndSwitcher();
        boolean z2 = (this.mNavigationIconHints & 1) != 0;
        KeyButtonDrawable keyButtonDrawable = this.mBackIcon;
        orientBackButton(keyButtonDrawable);
        KeyButtonDrawable keyButtonDrawable2 = this.mHomeDefaultIcon;
        if (!this.mUseCarModeUi) {
            orientHomeButton(keyButtonDrawable2);
        }
        getHomeButton().setImageDrawable(keyButtonDrawable2);
        getBackButton().setImageDrawable(keyButtonDrawable);
        updateRecentsIcon();
        updateNavButtonIcon(this.mHideNavBar);
        boolean z3 = QuickStepContract.isGesturalMode(this.mNavBarMode) && z2 && ((i2 = this.mCurrentRotation) == 1 || i2 == 3 || z);
        StringBuilder sb = new StringBuilder();
        sb.append("showImeSwitcher:");
        sb.append((this.mNavigationIconHints & 2) != 0);
        sb.append(",disableImeSwitcher:");
        sb.append(z3);
        Log.d("StatusBar/NavBarView", sb.toString());
        this.mContextualButtonGroup.setButtonVisibility(C0008R$id.ime_switcher, (this.mNavigationIconHints & 2) != 0 && !z3);
        this.mBarTransitions.reapplyDarkIntensity();
        boolean z4 = QuickStepContract.isGesturalMode(this.mNavBarMode) || (this.mDisabledFlags & 2097152) != 0;
        boolean isRecentsButtonDisabled = isRecentsButtonDisabled();
        boolean z5 = isRecentsButtonDisabled && (2097152 & this.mDisabledFlags) != 0;
        boolean z6 = !z2 && (this.mEdgeBackGestureHandler.isHandlingGestures() || (this.mDisabledFlags & 4194304) != 0);
        if (QuickStepContract.isGesturalMode(this.mNavBarMode) && z2 && ((i = this.mCurrentRotation) == 1 || i == 3 || z)) {
            z6 = true;
        }
        boolean isScreenPinningActive = ActivityManagerWrapper.getInstance().isScreenPinningActive();
        if (this.mOverviewProxyService.isEnabled()) {
            isRecentsButtonDisabled |= true ^ QuickStepContract.isLegacyMode(this.mNavBarMode);
            if (isScreenPinningActive && !QuickStepContract.isGesturalMode(this.mNavBarMode)) {
                z4 = false;
                z6 = false;
            }
        } else if (isScreenPinningActive) {
            isRecentsButtonDisabled = false;
            z6 = false;
        }
        ViewGroup viewGroup = (ViewGroup) getCurrentView().findViewById(C0008R$id.nav_buttons);
        if (!(viewGroup == null || (layoutTransition = viewGroup.getLayoutTransition()) == null || layoutTransition.getTransitionListeners().contains(this.mTransitionListener))) {
            layoutTransition.addTransitionListener(this.mTransitionListener);
        }
        if (DEBUG) {
            Log.d("StatusBar/NavBarView", "updateNavButtonIcons: disableBack/Home/Recent: " + z6 + "/" + z4 + "/" + isRecentsButtonDisabled + ", mNavBarMode=" + this.mNavBarMode + ", disableFlags=" + Integer.toHexString(this.mDisabledFlags) + ", useAltBack=" + z2 + ", rotation=" + this.mCurrentRotation);
        }
        getBackButton().setVisibility(z6 ? 4 : 0);
        getHomeButton().setVisibility(z4 ? 4 : 0);
        getRecentsButton().setVisibility(isRecentsButtonDisabled ? 4 : 0);
        ButtonDispatcher homeHandle = getHomeHandle();
        if (z5) {
            i3 = 4;
        }
        homeHandle.setVisibility(i3);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isRecentsButtonDisabled() {
        return this.mUseCarModeUi || !isOverviewEnabled() || getContext().getDisplayId() != 0;
    }

    private Display getContextDisplay() {
        return getContext().getDisplay();
    }

    public void setLayoutTransitionsEnabled(boolean z) {
        this.mLayoutTransitionsEnabled = z;
        updateLayoutTransitionsEnabled();
    }

    public void setWakeAndUnlocking(boolean z) {
        setUseFadingAnimations(z);
        this.mWakeAndUnlocking = z;
        updateLayoutTransitionsEnabled();
    }

    private void updateLayoutTransitionsEnabled() {
        boolean z = !this.mWakeAndUnlocking && this.mLayoutTransitionsEnabled;
        LayoutTransition layoutTransition = ((ViewGroup) getCurrentView().findViewById(C0008R$id.nav_buttons)).getLayoutTransition();
        if (layoutTransition == null) {
            return;
        }
        if (z) {
            layoutTransition.enableTransitionType(2);
            layoutTransition.enableTransitionType(3);
            layoutTransition.enableTransitionType(0);
            layoutTransition.enableTransitionType(1);
            return;
        }
        layoutTransition.disableTransitionType(2);
        layoutTransition.disableTransitionType(3);
        layoutTransition.disableTransitionType(0);
        layoutTransition.disableTransitionType(1);
    }

    private void setUseFadingAnimations(boolean z) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) ((ViewGroup) getParent()).getLayoutParams();
        if (layoutParams != null) {
            boolean z2 = layoutParams.windowAnimations != 0;
            if (!z2 && z) {
                layoutParams.windowAnimations = C0016R$style.Animation_NavigationBarFadeIn;
            } else if (z2 && !z) {
                layoutParams.windowAnimations = 0;
            } else {
                return;
            }
            if (!isAttachedToWindow()) {
                Log.w("StatusBar/NavBarView", "view isn't attached");
            } else {
                ((WindowManager) getContext().getSystemService("window")).updateViewLayout((View) getParent(), layoutParams);
            }
        }
    }

    public void onStatusBarPanelStateChanged() {
        updateSlippery();
        updatePanelSystemUiStateFlags();
    }

    public void updateDisabledSystemUiStateFlags() {
        int displayId = ((FrameLayout) this).mContext.getDisplayId();
        SysUiState sysUiState = this.mSysUiFlagContainer;
        boolean z = true;
        sysUiState.setFlag(1, ActivityManagerWrapper.getInstance().isScreenPinningActive());
        sysUiState.setFlag(128, (this.mDisabledFlags & 16777216) != 0);
        sysUiState.setFlag(256, (this.mDisabledFlags & 2097152) != 0);
        if ((this.mDisabledFlags & 33554432) == 0) {
            z = false;
        }
        sysUiState.setFlag(1024, z);
        sysUiState.commitUpdate(displayId);
    }

    public void updatePanelSystemUiStateFlags() {
        int displayId = ((FrameLayout) this).mContext.getDisplayId();
        NotificationPanelViewController notificationPanelViewController = this.mPanelView;
        if (notificationPanelViewController != null) {
            SysUiState sysUiState = this.mSysUiFlagContainer;
            sysUiState.setFlag(4, notificationPanelViewController.isFullyExpanded());
            sysUiState.setFlag(2048, this.mPanelView.isInSettings());
            sysUiState.commitUpdate(displayId);
            if (this.mIsPanelViewFullExpanded != this.mPanelView.isFullyExpanded()) {
                this.mIsPanelViewFullExpanded = this.mPanelView.isFullyExpanded();
                changeHomeHandleAlpha(true);
            }
        }
    }

    public void updateStates() {
        boolean z = this.mOverviewProxyService.shouldShowSwipeUpUI() && !QuickStepContract.isLegacyMode(this.mNavBarMode);
        if (z) {
            updateNavButtonState(false);
        }
        NavigationBarInflaterView navigationBarInflaterView = this.mNavigationInflaterView;
        if (navigationBarInflaterView != null) {
            navigationBarInflaterView.onLikelyDefaultLayoutChange();
        }
        updateSlippery();
        reloadNavIcons();
        updateNavButtonIcons();
        setUpSwipeUpOnboarding(isQuickStepSwipeUpEnabled());
        StringBuilder sb = new StringBuilder();
        sb.append("setHaptic ");
        sb.append(!z);
        sb.append(", ");
        sb.append(this);
        Log.d("StatusBar/NavBarView", sb.toString());
        WindowManagerWrapper.getInstance().setNavBarVirtualKeyHapticFeedbackEnabled(!z);
        getHomeButton().setAccessibilityDelegate(z ? this.mQuickStepAccessibilityDelegate : null);
    }

    public void updateSlippery() {
        NotificationPanelViewController notificationPanelViewController;
        setSlippery(!isQuickStepSwipeUpEnabled() || ((notificationPanelViewController = this.mPanelView) != null && notificationPanelViewController.isFullyExpanded() && !this.mPanelView.isCollapsing()));
    }

    private void setSlippery(boolean z) {
        setWindowFlag(536870912, z);
    }

    private void setWindowFlag(int i, boolean z) {
        WindowManager.LayoutParams layoutParams;
        ViewGroup viewGroup = (ViewGroup) getParent();
        if (viewGroup != null && (layoutParams = (WindowManager.LayoutParams) viewGroup.getLayoutParams()) != null) {
            if (z != ((layoutParams.flags & i) != 0)) {
                if (z) {
                    layoutParams.flags = i | layoutParams.flags;
                } else {
                    layoutParams.flags = (~i) & layoutParams.flags;
                }
                if (!isAttachedToWindow()) {
                    Log.w("StatusBar/NavBarView", "isn't attached");
                } else {
                    ((WindowManager) getContext().getSystemService("window")).updateViewLayout(viewGroup, layoutParams);
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        Log.d("StatusBar/NavBarView", "onNavigationModeChanged: " + this.mNavBarMode + " to " + i);
        Context currentUserContext = ((NavigationModeController) Dependency.get(NavigationModeController.class)).getCurrentUserContext();
        this.mNavBarMode = i;
        this.mBarTransitions.onNavigationModeChanged(i);
        this.mEdgeBackGestureHandler.onNavigationModeChanged(this.mNavBarMode);
        this.mOpEdgeNavGestureHandler.onNavigationModeChanged(this.mNavBarMode, currentUserContext);
        this.mRecentsOnboarding.onNavigationModeChanged(this.mNavBarMode);
        getRotateSuggestionButton().onNavigationModeChanged(this.mNavBarMode);
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            this.mRegionSamplingHelper.start(this.mSamplingBounds);
        } else {
            this.mRegionSamplingHelper.stop();
        }
        updateStates();
        if (OpNavBarUtils.isSupportCustomNavBar() && !QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            setLightBar(getLightTransitionsController().getCurrentDarkIntensity() == 1.0f);
        }
    }

    public void setAccessibilityButtonState(boolean z, boolean z2) {
        getAccessibilityButton().setLongClickable(z2);
        this.mContextualButtonGroup.setButtonVisibility(C0008R$id.accessibility_button, z);
    }

    /* access modifiers changed from: package-private */
    public void hideRecentsOnboarding() {
        this.mRecentsOnboarding.hide(true);
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        NavigationBarInflaterView navigationBarInflaterView = (NavigationBarInflaterView) findViewById(C0008R$id.navigation_inflater);
        this.mNavigationInflaterView = navigationBarInflaterView;
        navigationBarInflaterView.setButtonDispatchers(this.mButtonDispatchers);
        getImeSwitchButton().setOnClickListener(this.mImeSwitcherClickListener);
        ((Divider) Dependency.get(Divider.class)).registerInSplitScreenListener(this.mDockedListener);
        updateOrientationViews();
        reloadNavIcons();
        boolean z = true;
        int i = 0;
        if (this.mOverviewProxyService.shouldShowSwipeUpUI() || !QuickStepContract.isLegacyMode(this.mNavBarMode) || Settings.System.getInt(((FrameLayout) this).mContext.getContentResolver(), "op_gesture_button_enabled", 0) != 1) {
            z = false;
        }
        this.mShowNavKey = z;
        if (getNavButton() != null) {
            ButtonDispatcher navButton = getNavButton();
            if (!showNavKey()) {
                i = 4;
            }
            navButton.setVisibility(i);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        this.mDeadZone.onDraw(canvas);
        super.onDraw(canvas);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSamplingRect() {
        this.mSamplingBounds.setEmpty();
        View currentView = getHomeHandle().getCurrentView();
        if (currentView != null) {
            int[] iArr = new int[2];
            currentView.getLocationOnScreen(iArr);
            Point point = new Point();
            currentView.getContext().getDisplay().getRealSize(point);
            this.mSamplingBounds.set(new Rect(iArr[0] - this.mNavColorSampleMargin, point.y - getNavBarHeight(), iArr[0] + currentView.getWidth() + this.mNavColorSampleMargin, point.y));
        }
    }

    /* access modifiers changed from: package-private */
    public void setOrientedHandleSamplingRegion(Rect rect) {
        this.mOrientedHandleSamplingRegion = rect;
        this.mRegionSamplingHelper.updateSamplingRect();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mActiveRegion.setEmpty();
        updateButtonLocation(getBackButton(), this.mBackButtonBounds, true);
        updateButtonLocation(getHomeButton(), this.mHomeButtonBounds, false);
        updateButtonLocation(getRecentsButton(), this.mRecentsButtonBounds, false);
        updateButtonLocation(getRotateSuggestionButton(), this.mRotationButtonBounds, true);
        this.mOverviewProxyService.onActiveNavBarRegionChanges(this.mActiveRegion);
        this.mRecentsOnboarding.setNavBarHeight(getMeasuredHeight());
    }

    private void updateButtonLocation(ButtonDispatcher buttonDispatcher, Rect rect, boolean z) {
        View currentView = buttonDispatcher.getCurrentView();
        if (currentView == null) {
            rect.setEmpty();
            return;
        }
        float translationX = currentView.getTranslationX();
        float translationY = currentView.getTranslationY();
        currentView.setTranslationX(0.0f);
        currentView.setTranslationY(0.0f);
        if (z) {
            currentView.getLocationOnScreen(this.mTmpPosition);
            int[] iArr = this.mTmpPosition;
            rect.set(iArr[0], iArr[1], iArr[0] + currentView.getMeasuredWidth(), this.mTmpPosition[1] + currentView.getMeasuredHeight());
            this.mActiveRegion.op(rect, Region.Op.UNION);
        }
        currentView.getLocationInWindow(this.mTmpPosition);
        int[] iArr2 = this.mTmpPosition;
        rect.set(iArr2[0], iArr2[1], iArr2[0] + currentView.getMeasuredWidth(), this.mTmpPosition[1] + currentView.getMeasuredHeight());
        currentView.setTranslationX(translationX);
        currentView.setTranslationY(translationY);
    }

    private void updateOrientationViews() {
        this.mHorizontal = findViewById(C0008R$id.horizontal);
        this.mVertical = findViewById(C0008R$id.vertical);
        updateCurrentView();
    }

    /* access modifiers changed from: package-private */
    public boolean needsReorient(int i) {
        return this.mCurrentRotation != i;
    }

    private void updateCurrentView() {
        resetViews();
        View view = this.mIsVertical ? this.mVertical : this.mHorizontal;
        this.mCurrentView = view;
        boolean z = false;
        view.setVisibility(0);
        this.mNavigationInflaterView.setVertical(this.mIsVertical);
        this.mNavigationInflaterView.updateCurrentView();
        int rotation = getContextDisplay().getRotation();
        this.mCurrentRotation = rotation;
        NavigationBarInflaterView navigationBarInflaterView = this.mNavigationInflaterView;
        if (rotation == 1) {
            z = true;
        }
        navigationBarInflaterView.setAlternativeOrder(z);
        this.mNavigationInflaterView.updateButtonDispatchersCurrentView();
        updateLayoutTransitionsEnabled();
    }

    private void resetViews() {
        this.mHorizontal.setVisibility(8);
        this.mVertical.setVisibility(8);
    }

    private void updateRecentsIcon() {
        this.mDockedIcon.setRotation((!this.mDockedStackExists || !this.mIsVertical) ? 0.0f : 90.0f);
        getRecentsButton().setImageDrawable(this.mDockedStackExists ? this.mDockedIcon : this.mRecentIcon);
        this.mBarTransitions.reapplyDarkIntensity();
    }

    public void updateNavButtonIcon(boolean z) {
        KeyButtonDrawable keyButtonDrawable;
        this.mHideNavBar = z;
        if (getNavButton() != null) {
            ButtonDispatcher navButton = getNavButton();
            if (z) {
                keyButtonDrawable = getDrawable(C0006R$drawable.ic_sysbar_pin_on);
            } else {
                keyButtonDrawable = getDrawable(C0006R$drawable.ic_sysbar_pin_off);
            }
            navButton.setImageDrawable(keyButtonDrawable);
        }
    }

    public void showPinningEnterExitToast(boolean z) {
        if (z) {
            this.mScreenPinningNotify.showPinningStartToast();
        } else {
            this.mScreenPinningNotify.showPinningExitToast();
        }
    }

    public void showPinningEscapeToast() {
        this.mScreenPinningNotify.showEscapeToast(this.mNavBarMode == 2, isRecentsButtonVisible());
    }

    public void reorient() {
        updateCurrentView();
        ((NavigationBarFrame) getRootView()).setDeadZone(this.mDeadZone);
        this.mDeadZone.onConfigurationChanged(this.mCurrentRotation);
        this.mBarTransitions.init();
        boolean z = false;
        if (getNavButton() != null) {
            getNavButton().setVisibility(showNavKey() ? 0 : 4);
        }
        if (DEBUG) {
            Log.d("StatusBar/NavBarView", "reorient(): rot=" + this.mCurrentRotation);
        }
        if (!isLayoutDirectionResolved()) {
            resolveLayoutDirection();
        }
        updateNavButtonIcons();
        ButtonDispatcher homeHandle = getHomeHandle();
        int i = this.mCurrentRotation;
        if (i == 0 || i == 2) {
            z = true;
        }
        homeHandle.setVertical(z);
        if (this.mEdgeBackGestureHandler != null) {
            if (DEBUG) {
                Log.d("StatusBar/NavBarView", "NavBarView onConfigurationChanged");
            }
            this.mEdgeBackGestureHandler.onConfigurationChanged(this.mCurrentRotation);
        }
        OpEdgeNavGestureHandler opEdgeNavGestureHandler = this.mOpEdgeNavGestureHandler;
        if (opEdgeNavGestureHandler != null) {
            opEdgeNavGestureHandler.onConfigurationChanged(this.mCurrentRotation);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x006f: APUT  (r3v6 java.lang.Object[]), (2 ??[int, float, short, byte, char]), (r6v2 java.lang.String) */
    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int i, int i2) {
        int i3;
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        if (DEBUG) {
            Log.d("StatusBar/NavBarView", String.format("onMeasure: (%dx%d) old: (%dx%d)", Integer.valueOf(size), Integer.valueOf(size2), Integer.valueOf(getMeasuredWidth()), Integer.valueOf(getMeasuredHeight())));
        }
        boolean z = size > 0 && size2 > size && !QuickStepContract.isGesturalMode(this.mNavBarMode);
        if (z != this.mIsVertical) {
            this.mIsVertical = z;
            if (DEBUG) {
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(size2);
                objArr[1] = Integer.valueOf(size);
                objArr[2] = this.mIsVertical ? "y" : "n";
                Log.d("StatusBar/NavBarView", String.format("onMeasure: h=%d, w=%d, vert=%s", objArr));
            }
            reorient();
            notifyVerticalChangedListener(z);
        }
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            if (this.mIsVertical) {
                i3 = getResources().getDimensionPixelSize(17105329);
            } else {
                i3 = getResources().getDimensionPixelSize(17105327);
            }
            this.mBarTransitions.setBackgroundFrame(new Rect(0, getResources().getDimensionPixelSize(17105324) - i3, size, size2));
        } else {
            this.mBarTransitions.setBackgroundFrame(null);
        }
        super.onMeasure(i, i2);
    }

    private int getNavBarHeight() {
        if (this.mIsVertical) {
            return getResources().getDimensionPixelSize(17105329);
        }
        return getResources().getDimensionPixelSize(17105327);
    }

    private void notifyVerticalChangedListener(boolean z) {
        OnVerticalChangedListener onVerticalChangedListener = this.mOnVerticalChangedListener;
        if (onVerticalChangedListener != null) {
            onVerticalChangedListener.onVerticalChanged(z);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mTmpLastConfiguration.updateFrom(this.mConfiguration);
        this.mConfiguration.updateFrom(configuration);
        boolean updateCarMode = updateCarMode();
        updateIcons(this.mTmpLastConfiguration);
        updateRecentsIcon();
        this.mRecentsOnboarding.onConfigurationChanged(this.mConfiguration);
        if (!updateCarMode) {
            Configuration configuration2 = this.mTmpLastConfiguration;
            if (configuration2.densityDpi == this.mConfiguration.densityDpi && configuration2.getLayoutDirection() == this.mConfiguration.getLayoutDirection()) {
                return;
            }
        }
        updateNavButtonIcons();
    }

    private boolean updateCarMode() {
        Configuration configuration = this.mConfiguration;
        if (configuration != null) {
            boolean z = (configuration.uiMode & 15) == 3;
            if (z != this.mInCarMode) {
                this.mInCarMode = z;
                this.mUseCarModeUi = false;
            }
        }
        return false;
    }

    private String getResourceName(int i) {
        if (i == 0) {
            return "(null)";
        }
        try {
            return getContext().getResources().getResourceName(i);
        } catch (Resources.NotFoundException unused) {
            return "(unknown)";
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestApplyInsets();
        reorient();
        this.mNavBarMode = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(this);
        this.mRegionSamplingHelper = new RegionSamplingHelper(this, new RegionSamplingHelper.SamplingCallback() { // from class: com.android.systemui.statusbar.phone.NavigationBarView.3
            @Override // com.android.systemui.statusbar.phone.RegionSamplingHelper.SamplingCallback
            public void onRegionDarknessChanged(boolean z) {
                NavigationBarView.this.getLightTransitionsController().setIconsDark(!z, true);
            }

            @Override // com.android.systemui.statusbar.phone.RegionSamplingHelper.SamplingCallback
            public Rect getSampledRegion(View view) {
                NavigationBarView.this.updateSamplingRect();
                return NavigationBarView.this.mSamplingBounds;
            }

            @Override // com.android.systemui.statusbar.phone.RegionSamplingHelper.SamplingCallback
            public boolean isSamplingEnabled() {
                return Utils.isGesturalModeOnDefaultDisplay(NavigationBarView.this.getContext(), NavigationBarView.this.mNavBarMode);
            }
        });
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            this.mRegionSamplingHelper.start(this.mSamplingBounds);
        } else {
            this.mRegionSamplingHelper.stop();
        }
        onNavigationModeChanged(this.mNavBarMode);
        setUpSwipeUpOnboarding(isQuickStepSwipeUpEnabled());
        RotationButtonController rotationButtonController = this.mRotationButtonController;
        if (rotationButtonController != null) {
            rotationButtonController.registerListeners();
        }
        this.mEdgeBackGestureHandler.onNavBarAttached();
        this.mOpEdgeNavGestureHandler.setHomeButton((QuickStepContract.isGesturalMode(this.mNavBarMode) ? getHomeHandle() : getHomeButton()).getCurrentView());
        this.mOpEdgeNavGestureHandler.onNavBarAttached();
        changeHomeHandleAlpha(false);
        getViewTreeObserver().addOnComputeInternalInsetsListener(this.mOnComputeInternalInsetsListener);
        getLightTransitionsController().bypassTransition(true);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((NavigationModeController) Dependency.get(NavigationModeController.class)).removeListener(this);
        setUpSwipeUpOnboarding(false);
        for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
            this.mButtonDispatchers.valueAt(i).onDestroy();
        }
        RotationButtonController rotationButtonController = this.mRotationButtonController;
        if (rotationButtonController != null) {
            rotationButtonController.unregisterListeners();
        }
        this.mEdgeBackGestureHandler.onNavBarDetached();
        this.mOpEdgeNavGestureHandler.onNavBarDetached();
        getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mOnComputeInternalInsetsListener);
    }

    private void setUpSwipeUpOnboarding(boolean z) {
        if (z) {
            this.mRecentsOnboarding.onConnectedToLauncher();
        } else {
            this.mRecentsOnboarding.onDisconnectedFromLauncher();
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00f2: APUT  (r6v11 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r8v20 java.lang.String) */
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NavigationBarView {");
        Rect rect = new Rect();
        Point point = new Point();
        getContextDisplay().getRealSize(point);
        printWriter.println(String.format("      this: " + StatusBar.viewInfo(this) + " " + visibilityToString(getVisibility()), new Object[0]));
        getWindowVisibleDisplayFrame(rect);
        boolean z = rect.right > point.x || rect.bottom > point.y;
        StringBuilder sb = new StringBuilder();
        sb.append("      window: ");
        sb.append(rect.toShortString());
        sb.append(" ");
        sb.append(visibilityToString(getWindowVisibility()));
        sb.append(z ? " OFFSCREEN!" : "");
        printWriter.println(sb.toString());
        printWriter.println(String.format("      mCurrentView: id=%s (%dx%d) %s %f", getResourceName(getCurrentView().getId()), Integer.valueOf(getCurrentView().getWidth()), Integer.valueOf(getCurrentView().getHeight()), visibilityToString(getCurrentView().getVisibility()), Float.valueOf(getCurrentView().getAlpha())));
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(this.mDisabledFlags);
        objArr[1] = this.mIsVertical ? "true" : "false";
        objArr[2] = Float.valueOf(getLightTransitionsController().getCurrentDarkIntensity());
        printWriter.println(String.format("      disabled=0x%08x vertical=%s darkIntensity=%.2f", objArr));
        printWriter.println("      mOrientedHandleSamplingRegion: " + this.mOrientedHandleSamplingRegion);
        dumpButton(printWriter, "back", getBackButton());
        dumpButton(printWriter, "home", getHomeButton());
        dumpButton(printWriter, "rcnt", getRecentsButton());
        dumpButton(printWriter, "rota", getRotateSuggestionButton());
        dumpButton(printWriter, "a11y", getAccessibilityButton());
        printWriter.println("    }");
        printWriter.println("    mScreenOn: " + this.mScreenOn);
        NavigationBarInflaterView navigationBarInflaterView = this.mNavigationInflaterView;
        if (navigationBarInflaterView != null) {
            navigationBarInflaterView.dump(printWriter);
        }
        this.mContextualButtonGroup.dump(printWriter);
        this.mRecentsOnboarding.dump(printWriter);
        this.mRegionSamplingHelper.dump(printWriter);
        this.mEdgeBackGestureHandler.dump(printWriter);
        if (DEBUG) {
            printWriter.println("barMode: " + this.mBarTransitions.getMode());
            printWriter.println("mIsLightBar: " + this.mIsLightBar);
            printWriter.println("mIsHideNavBarOn: " + this.mIsHideNavBarOn);
            printWriter.println("mBackgroundColor: " + Integer.toHexString(this.mBackgroundColor));
            printWriter.println("mLastButtonColor: " + Integer.toHexString(this.mLastButtonColor));
            printWriter.println("mLastRippleColor: " + Integer.toHexString(this.mLastRippleColor));
        }
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        AssistHandleViewController assistHandleViewController;
        int i;
        int systemWindowInsetLeft = windowInsets.getSystemWindowInsetLeft();
        int systemWindowInsetRight = windowInsets.getSystemWindowInsetRight();
        int i2 = this.mCurrentRotation;
        boolean z = false;
        Log.d("StatusBar/NavBarView", "onApplyWindowInsets: leftInset:" + systemWindowInsetLeft + ", rightInset:" + systemWindowInsetRight + ",isGesturalMode:" + QuickStepContract.isGesturalMode(this.mNavBarMode) + ",isLandScape:" + (i2 == 1 || i2 == 3));
        if (QuickStepContract.isGesturalMode(this.mNavBarMode) && ((i = this.mCurrentRotation) == 1 || i == 3)) {
            systemWindowInsetLeft = 0;
            systemWindowInsetRight = 0;
        }
        setPadding(systemWindowInsetLeft, windowInsets.getSystemWindowInsetTop(), systemWindowInsetRight, windowInsets.getSystemWindowInsetBottom());
        this.mEdgeBackGestureHandler.setInsets(systemWindowInsetLeft, systemWindowInsetRight);
        if (!QuickStepContract.isGesturalMode(this.mNavBarMode) || windowInsets.getSystemWindowInsetBottom() == 0) {
            z = true;
        }
        setClipChildren(z);
        setClipToPadding(z);
        NavigationBarController navigationBarController = (NavigationBarController) Dependency.get(NavigationBarController.class);
        if (navigationBarController == null) {
            assistHandleViewController = null;
        } else {
            assistHandleViewController = navigationBarController.getAssistHandlerViewController();
        }
        if (assistHandleViewController != null) {
            assistHandleViewController.setBottomOffset(windowInsets.getSystemWindowInsetBottom());
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    private static void dumpButton(PrintWriter printWriter, String str, ButtonDispatcher buttonDispatcher) {
        printWriter.print("      " + str + ": ");
        if (buttonDispatcher == null) {
            printWriter.print("null");
        } else {
            printWriter.print(visibilityToString(buttonDispatcher.getVisibility()) + " alpha=" + buttonDispatcher.getAlpha());
        }
        printWriter.println();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$NavigationBarView(Boolean bool) {
        post(new Runnable(bool) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarView$seIN-E1MF9Wb6jBs3U7jhkEzAV4
            public final /* synthetic */ Boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                NavigationBarView.this.lambda$new$1$NavigationBarView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$NavigationBarView(Boolean bool) {
        this.mDockedStackExists = bool.booleanValue();
        updateRecentsIcon();
    }

    public void onBrickModeChanged(boolean z) {
        this.mIsInBrickMode = z;
    }

    private void changeHomeHandleAlpha(boolean z) {
        boolean z2;
        NotificationPanelViewController notificationPanelViewController;
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        boolean z3 = true;
        ButtonDispatcher homeHandle = (!QuickStepContract.isGesturalMode(this.mNavBarMode) || phoneStatusBar.getNavigationBarHiddenMode() == 1 || ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isKeyguardVisible() || OpUtils.isHomeApp()) ? null : getHomeHandle();
        int i = 0;
        if (!QuickStepContract.isGesturalMode(this.mNavBarMode) || !this.mImeVisible || (phoneStatusBar.getNavigationBarHiddenMode() != 1 && (this.mEdgeBackGestureHandler == null || EdgeBackGestureHandler.sSideGestureEnabled))) {
            z2 = false;
        } else {
            homeHandle = getHomeHandle();
            Log.d("StatusBar/NavBarView", "hideHomeHandle when no bar and ime");
            z2 = true;
        }
        if (homeHandle != null) {
            int i2 = this.mCurrentRotation;
            if (!(i2 == 1 || i2 == 3) || (notificationPanelViewController = this.mPanelView) == null || !notificationPanelViewController.isFullyExpanded()) {
                z3 = false;
            }
            if (z3 || z2) {
                i = 4;
            }
            homeHandle.setVisibility(i);
            homeHandle.setAlpha((z3 || z2) ? 0.0f : 1.0f, z);
        }
    }

    public RegionSamplingHelper getRegionSamplingHelper() {
        return this.mRegionSamplingHelper;
    }
}
