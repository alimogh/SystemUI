package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Insets;
import android.graphics.Rect;
import android.metrics.LogMaker;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;
import android.view.WindowInsetsAnimationControlListener;
import android.view.WindowInsetsAnimationController;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityContainer;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.settingslib.utils.ThreadUtils;
import com.android.systemui.C0000R$anim;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.assist.ui.DisplayUtils;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.InjectionInflationController;
import com.android.systemui.util.ProductUtils;
import com.oneplus.keyguard.OpEmergencyPanel;
import com.oneplus.keyguard.OpKeyguardSecurityContainer;
import com.oneplus.util.OpUtils;
import java.util.List;
import java.util.function.Supplier;
public class KeyguardSecurityContainer extends OpKeyguardSecurityContainer implements KeyguardSecurityView {
    private static final UiEventLogger sUiEventLogger = new UiEventLoggerImpl();
    private AlertDialog mAlertDialog;
    private boolean mAppearAnimationStarted;
    private KeyguardSecurityCallback mCallback;
    private KeyguardSecurityModel.SecurityMode mCurrentSecuritySelection;
    private KeyguardSecurityView mCurrentSecurityView;
    private boolean mDisappearAnimRunning;
    private OpEmergencyPanel mEmergencyPanel;
    private Animation mFacelockAnimationSet;
    private int mImeInset;
    private InjectionInflationController mInjectionInflationController;
    private final KeyguardStateController mKeyguardStateController;
    private LockPatternUtils mLockPatternUtils;
    private final MetricsLogger mMetricsLogger;
    private KeyguardSecurityCallback mNullCallback;
    private View mOpKeyguardEmergencyPanelView;
    private AdminSecondaryLockScreenController mSecondaryLockScreenController;
    private SecurityCallback mSecurityCallback;
    private View mSecurityIcon;
    private View mSecurityIconSwap;
    private KeyguardSecurityModel mSecurityModel;
    KeyguardSecurityViewFlipper mSecurityViewFlipper;
    private final KeyguardUpdateMonitor mUpdateMonitor;

    public interface SecurityCallback {
        boolean dismiss(boolean z, int i, boolean z2);

        void finish(boolean z, int i);

        void onCancelClicked();

        void onSecurityModeChanged(KeyguardSecurityModel.SecurityMode securityMode, boolean z);

        void reportMDMEvent(String str, String str2, String str3);

        void reset();

        void tryToStartFaceLockFromBouncer();

        void userActivity();
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public enum BouncerUiEvent implements UiEventLogger.UiEventEnum {
        UNKNOWN(0),
        BOUNCER_DISMISS_EXTENDED_ACCESS(413),
        BOUNCER_DISMISS_BIOMETRIC(414),
        BOUNCER_DISMISS_NONE_SECURITY(415),
        BOUNCER_DISMISS_PASSWORD(416),
        BOUNCER_DISMISS_SIM(417),
        BOUNCER_PASSWORD_SUCCESS(418),
        BOUNCER_PASSWORD_FAILURE(419);
        
        private final int mId;

        private BouncerUiEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardSecurityContainer(Context context) {
        this(context, null, 0);
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCurrentSecuritySelection = KeyguardSecurityModel.SecurityMode.Invalid;
        VelocityTracker.obtain();
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        new WindowInsetsAnimation.Callback(0) { // from class: com.android.keyguard.KeyguardSecurityContainer.1
            private final Rect mFinalBounds = new Rect();
            private final Rect mInitialBounds = new Rect();

            @Override // android.view.WindowInsetsAnimation.Callback
            public void onPrepare(WindowInsetsAnimation windowInsetsAnimation) {
                KeyguardSecurityContainer.this.mSecurityViewFlipper.getBoundsOnScreen(this.mInitialBounds);
            }

            @Override // android.view.WindowInsetsAnimation.Callback
            public WindowInsetsAnimation.Bounds onStart(WindowInsetsAnimation windowInsetsAnimation, WindowInsetsAnimation.Bounds bounds) {
                KeyguardSecurityContainer.this.mSecurityViewFlipper.getBoundsOnScreen(this.mFinalBounds);
                return bounds;
            }

            @Override // android.view.WindowInsetsAnimation.Callback
            public WindowInsets onProgress(WindowInsets windowInsets, List<WindowInsetsAnimation> list) {
                if (KeyguardSecurityContainer.this.mDisappearAnimRunning) {
                    KeyguardSecurityContainer.this.mSecurityViewFlipper.setTranslationY((float) (this.mInitialBounds.bottom - this.mFinalBounds.bottom));
                } else {
                    int i2 = 0;
                    for (WindowInsetsAnimation windowInsetsAnimation : list) {
                        if ((windowInsetsAnimation.getTypeMask() & WindowInsets.Type.ime()) != 0) {
                            i2 += (int) MathUtils.lerp((float) (this.mInitialBounds.bottom - this.mFinalBounds.bottom), 0.0f, windowInsetsAnimation.getInterpolatedFraction());
                        }
                    }
                    KeyguardSecurityContainer.this.mSecurityViewFlipper.setTranslationY((float) i2);
                }
                return windowInsets;
            }

            @Override // android.view.WindowInsetsAnimation.Callback
            public void onEnd(WindowInsetsAnimation windowInsetsAnimation) {
                if (!KeyguardSecurityContainer.this.mDisappearAnimRunning) {
                    KeyguardSecurityContainer.this.mSecurityViewFlipper.setTranslationY(0.0f);
                }
            }
        };
        this.mCallback = new KeyguardSecurityCallback() { // from class: com.android.keyguard.KeyguardSecurityContainer.3
            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void userActivity() {
                if (KeyguardSecurityContainer.this.mSecurityCallback != null) {
                    KeyguardSecurityContainer.this.mSecurityCallback.userActivity();
                }
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void onUserInput() {
                KeyguardSecurityContainer.this.mUpdateMonitor.cancelFaceAuth();
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z, int i2) {
                dismiss(z, i2, false);
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z, int i2, boolean z2) {
                KeyguardSecurityContainer.this.mSecurityCallback.dismiss(z, i2, z2);
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reportUnlockAttempt(int i2, boolean z, int i3) {
                BouncerUiEvent bouncerUiEvent;
                if (z) {
                    SysUiStatsLog.write(64, 2);
                    KeyguardSecurityContainer.this.mUpdateMonitor.clearFailedUnlockAttempts(true);
                    Settings.Secure.putIntForUser(((FrameLayout) KeyguardSecurityContainer.this).mContext.getContentResolver(), "confirm_lock_password_fragment.key_num_wrong_confirm_attempts", 0, i2);
                    KeyguardSecurityContainer.this.mLockPatternUtils.reportSuccessfulPasswordAttempt(i2);
                    ThreadUtils.postOnBackgroundThread($$Lambda$KeyguardSecurityContainer$3$OMSiLPoAPJCjVWlQcH6dkkVRIyE.INSTANCE);
                } else {
                    SysUiStatsLog.write(64, 1);
                    KeyguardSecurityContainer.this.reportFailedUnlockAttempt(i2, i3);
                }
                KeyguardSecurityContainer.this.mMetricsLogger.write(new LogMaker(197).setType(z ? 10 : 11));
                UiEventLogger uiEventLogger = KeyguardSecurityContainer.sUiEventLogger;
                if (z) {
                    bouncerUiEvent = BouncerUiEvent.BOUNCER_PASSWORD_SUCCESS;
                } else {
                    bouncerUiEvent = BouncerUiEvent.BOUNCER_PASSWORD_FAILURE;
                }
                uiEventLogger.log(bouncerUiEvent);
            }

            static /* synthetic */ void lambda$reportUnlockAttempt$0() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException unused) {
                }
                Runtime.getRuntime().gc();
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reset() {
                KeyguardSecurityContainer.this.mSecurityCallback.reset();
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void onCancelClicked() {
                KeyguardSecurityContainer.this.mSecurityCallback.onCancelClicked();
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reportMDMEvent(String str, String str2, String str3) {
                if ("pass".equals(str2)) {
                    if (KeyguardSecurityContainer.this.mCurrentSecuritySelection == KeyguardSecurityModel.SecurityMode.Password) {
                        str2 = "password";
                    } else if (KeyguardSecurityContainer.this.mCurrentSecuritySelection == KeyguardSecurityModel.SecurityMode.PIN) {
                        str2 = "pin";
                    }
                }
                KeyguardSecurityContainer.this.mSecurityCallback.reportMDMEvent(str, str2, str3);
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void tryToStartFaceLockFromBouncer() {
                KeyguardSecurityContainer.this.mSecurityCallback.tryToStartFaceLockFromBouncer();
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void hideSecurityIcon() {
                KeyguardSecurityContainer.this.hideSecurityIcon();
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public KeyguardSecurityModel.SecurityMode getCurrentSecurityMode() {
                return KeyguardSecurityContainer.this.getCurrentSecurityMode();
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public OpEmergencyPanel getEmergencyPanel() {
                return KeyguardSecurityContainer.this.mEmergencyPanel;
            }
        };
        this.mNullCallback = new KeyguardSecurityCallback(this) { // from class: com.android.keyguard.KeyguardSecurityContainer.4
            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z, int i2) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z, int i2, boolean z2) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public OpEmergencyPanel getEmergencyPanel() {
                return null;
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void hideSecurityIcon() {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void onUserInput() {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reportMDMEvent(String str, String str2, String str3) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reportUnlockAttempt(int i2, boolean z, int i3) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reset() {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void tryToStartFaceLockFromBouncer() {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void userActivity() {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public KeyguardSecurityModel.SecurityMode getCurrentSecurityMode() {
                return KeyguardSecurityModel.SecurityMode.None;
            }
        };
        this.mSecurityModel = (KeyguardSecurityModel) Dependency.get(KeyguardSecurityModel.class);
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        new SpringAnimation(this, DynamicAnimation.Y);
        this.mInjectionInflationController = new InjectionInflationController(SystemUIFactory.getInstance().getRootComponent());
        ViewConfiguration.get(context);
        this.mKeyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        this.mSecondaryLockScreenController = new AdminSecondaryLockScreenController(context, this, this.mUpdateMonitor, this.mCallback, new Handler(Looper.myLooper()));
        this.mFacelockAnimationSet = AnimationUtils.loadAnimation(((FrameLayout) this).mContext, C0000R$anim.facelock_lock_blink);
    }

    public void setSecurityCallback(SecurityCallback securityCallback) {
        this.mSecurityCallback = securityCallback;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).onResume(i);
        }
        updateBiometricRetry();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        AlertDialog alertDialog = this.mAlertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
            this.mAlertDialog = null;
        }
        this.mSecondaryLockScreenController.hide();
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).onPause();
        }
        this.mSecurityViewFlipper.setWindowInsetsAnimationCallback(null);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onStartingToHide() {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).onStartingToHide();
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).startAppearAnimation();
        }
        this.mAppearAnimationStarted = true;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        this.mAppearAnimationStarted = false;
        this.mDisappearAnimRunning = true;
        if (this.mCurrentSecuritySelection == KeyguardSecurityModel.SecurityMode.Password) {
            Log.i("KeyguardSecurityView", "startDisappearAnimation, Password, controlWindowInsetsAnimation, mImeInset:" + this.mImeInset);
            if (this.mImeInset > 0) {
                this.mSecurityViewFlipper.getWindowInsetsController().controlWindowInsetsAnimation(WindowInsets.Type.ime(), 125, Interpolators.LINEAR, null, new WindowInsetsAnimationControlListener() { // from class: com.android.keyguard.KeyguardSecurityContainer.2
                    @Override // android.view.WindowInsetsAnimationControlListener
                    public void onCancelled(WindowInsetsAnimationController windowInsetsAnimationController) {
                    }

                    @Override // android.view.WindowInsetsAnimationControlListener
                    public void onReady(final WindowInsetsAnimationController windowInsetsAnimationController, int i) {
                        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
                        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(windowInsetsAnimationController, ofFloat) { // from class: com.android.keyguard.-$$Lambda$KeyguardSecurityContainer$2$EBkY_BuI_octLTkwvLqPBEt-j3I
                            public final /* synthetic */ WindowInsetsAnimationController f$0;
                            public final /* synthetic */ ValueAnimator f$1;

                            {
                                this.f$0 = r1;
                                this.f$1 = r2;
                            }

                            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                                KeyguardSecurityContainer.AnonymousClass2.lambda$onReady$0(this.f$0, this.f$1, valueAnimator);
                            }
                        });
                        ofFloat.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.keyguard.KeyguardSecurityContainer.2.1
                            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                            public void onAnimationEnd(Animator animator) {
                                windowInsetsAnimationController.finish(false);
                            }
                        });
                        ofFloat.setDuration(125L);
                        ofFloat.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
                        ofFloat.start();
                    }

                    static /* synthetic */ void lambda$onReady$0(WindowInsetsAnimationController windowInsetsAnimationController, ValueAnimator valueAnimator, ValueAnimator valueAnimator2) {
                        if (!windowInsetsAnimationController.isCancelled()) {
                            Insets shownStateInsets = windowInsetsAnimationController.getShownStateInsets();
                            windowInsetsAnimationController.setInsetsAndAlpha(Insets.add(shownStateInsets, Insets.of(0, 0, 0, (int) (((float) ((-shownStateInsets.bottom) / 4)) * valueAnimator.getAnimatedFraction()))), ((Float) valueAnimator2.getAnimatedValue()).floatValue(), valueAnimator.getAnimatedFraction());
                        }
                    }

                    @Override // android.view.WindowInsetsAnimationControlListener
                    public void onFinished(WindowInsetsAnimationController windowInsetsAnimationController) {
                        KeyguardSecurityContainer.this.mDisappearAnimRunning = false;
                    }
                });
            }
        }
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            return getSecurityView(securityMode).startDisappearAnimation(runnable);
        }
        return false;
    }

    private void updateBiometricRetry() {
        KeyguardSecurityModel.SecurityMode securityMode = getSecurityMode();
        if (this.mKeyguardStateController.isFaceAuthEnabled() && securityMode != KeyguardSecurityModel.SecurityMode.SimPin && securityMode != KeyguardSecurityModel.SecurityMode.SimPuk) {
            KeyguardSecurityModel.SecurityMode securityMode2 = KeyguardSecurityModel.SecurityMode.None;
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return this.mSecurityViewFlipper.getTitle();
    }

    /* access modifiers changed from: protected */
    public KeyguardSecurityView getSecurityView(KeyguardSecurityModel.SecurityMode securityMode) {
        KeyguardSecurityView keyguardSecurityView;
        int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        int childCount = this.mSecurityViewFlipper.getChildCount();
        int i = 0;
        while (true) {
            if (i >= childCount) {
                keyguardSecurityView = null;
                break;
            } else if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                keyguardSecurityView = (KeyguardSecurityView) this.mSecurityViewFlipper.getChildAt(i);
                break;
            } else {
                i++;
            }
        }
        int layoutIdFor = getLayoutIdFor(securityMode);
        if (keyguardSecurityView == null && layoutIdFor != 0) {
            LayoutInflater from = LayoutInflater.from(((FrameLayout) this).mContext);
            Log.v("KeyguardSecurityView", "inflating id = " + layoutIdFor);
            View inflate = this.mInjectionInflationController.injectable(from).inflate(layoutIdFor, (ViewGroup) this.mSecurityViewFlipper, false);
            updateEmergencyPanel(inflate);
            this.mSecurityViewFlipper.addView(inflate);
            updateSecurityView(inflate);
            keyguardSecurityView = (KeyguardSecurityView) inflate;
            keyguardSecurityView.reset();
        }
        updateSecurityIcon(keyguardSecurityView);
        return keyguardSecurityView;
    }

    private void updateSecurityView(View view) {
        if (view instanceof KeyguardSecurityView) {
            KeyguardSecurityView keyguardSecurityView = (KeyguardSecurityView) view;
            keyguardSecurityView.setKeyguardCallback(this.mCallback);
            keyguardSecurityView.setLockPatternUtils(this.mLockPatternUtils);
            return;
        }
        Log.w("KeyguardSecurityView", "View " + view + " is not a KeyguardSecurityView");
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        KeyguardSecurityViewFlipper keyguardSecurityViewFlipper = (KeyguardSecurityViewFlipper) findViewById(C0008R$id.view_flipper);
        this.mSecurityViewFlipper = keyguardSecurityViewFlipper;
        keyguardSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    private void updateEmergencyPanel(View view) {
        OpEmergencyPanel opEmergencyPanel;
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) view).findViewById(C0008R$id.keyguard_eca_emergency_container);
        if (viewGroup == null) {
            Log.i("KeyguardSecurityView", "parentViewGroup == null");
        } else if (OpUtils.isSupportEmergencyPanel()) {
            View inflate = LayoutInflater.from(((FrameLayout) this).mContext).inflate(C0011R$layout.op_keyguard_emergency_panel, viewGroup, false);
            this.mOpKeyguardEmergencyPanelView = inflate;
            if (inflate != null) {
                viewGroup.addView(inflate);
            } else {
                Log.w("KeyguardSecurityView", "mOpKeyguardEmergencyPanelView == null");
            }
            this.mEmergencyPanel = (OpEmergencyPanel) viewGroup.findViewById(C0008R$id.emergency_panel);
            Log.i("KeyguardSecurityView", "parentViewGroup mEmergencyPanel:" + this.mEmergencyPanel);
            if (DisplayUtils.getWidth(((FrameLayout) this).mContext) > 1080 && (opEmergencyPanel = this.mEmergencyPanel) != null) {
                ViewGroup.LayoutParams layoutParams = opEmergencyPanel.findViewById(C0008R$id.bubble_panel).getLayoutParams();
                layoutParams.height = OpUtils.convertPxByResolutionProportion((float) ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_bubble_area_height), 1080);
                layoutParams.width = OpUtils.convertPxByResolutionProportion((float) ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_bubble_area_width), 1080);
                FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mEmergencyPanel.findViewById(C0008R$id.bubble).getLayoutParams();
                layoutParams2.topMargin = OpUtils.convertPxByResolutionProportion((float) ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_bubble_margin_top), 1080);
                layoutParams2.height = OpUtils.convertPxByResolutionProportion((float) ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_bubble_diameter), 1080);
                layoutParams2.width = OpUtils.convertPxByResolutionProportion((float) ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_bubble_diameter), 1080);
                ((TextView) this.mEmergencyPanel.findViewById(C0008R$id.sos)).setTextSize(0, (float) OpUtils.convertPxByResolutionProportion((float) ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_bubble_text_size), 1080));
                TextView textView = (TextView) this.mEmergencyPanel.findViewById(C0008R$id.hint);
                int convertPxByResolutionProportion = OpUtils.convertPxByResolutionProportion((float) ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_panel_indicator_padding), 1080);
                textView.setPadding(convertPxByResolutionProportion, 0, convertPxByResolutionProportion, 0);
                textView.setTextSize(0, (float) OpUtils.convertPxByResolutionProportion((float) ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_panel_indicator_hint_text_size), 1080));
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();
                marginLayoutParams.bottomMargin = convertPxByResolutionProportion;
                marginLayoutParams.topMargin = OpUtils.convertPxByResolutionProportion((float) ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_panel_indicator_hint_margin_top), 1080);
                textView.setLayoutParams(marginLayoutParams);
                ((ViewGroup.MarginLayoutParams) this.mEmergencyPanel.findViewById(C0008R$id.indator_layout).getLayoutParams()).topMargin = OpUtils.convertPxByResolutionProportion((float) ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_panel_indicator_margin_top), 1080);
            }
            OpEmergencyPanel opEmergencyPanel2 = this.mEmergencyPanel;
            if (opEmergencyPanel2 != null) {
                opEmergencyPanel2.setVisibility(4);
            }
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityModel.setLockPatternUtils(lockPatternUtils);
        this.mSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int i;
        int paddingTop = getPaddingTop();
        if (ViewRootImpl.sNewInsetsMode == 2) {
            int i2 = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()).bottom;
            int i3 = windowInsets.getInsets(WindowInsets.Type.ime()).bottom;
            this.mImeInset = i3;
            i = Integer.max(i2, i3);
            if (this.mImeInset > 0) {
                paddingTop = getResources().getDimensionPixelSize(17105481);
            }
            if (Build.DEBUG_ONEPLUS) {
                Log.i("KeyguardSecurityView", "onApplyWindowInsets,, paddingTopWhenIme:" + paddingTop + ", mImeInset:" + this.mImeInset + ", bottomInset:" + i2 + ", inset:" + i);
            }
        } else {
            i = windowInsets.getSystemWindowInsetBottom();
        }
        setPadding(getPaddingLeft(), paddingTop, getPaddingRight(), i);
        return windowInsets.inset(0, 0, 0, i);
    }

    private void showDialog(String str, String str2) {
        AlertDialog alertDialog = this.mAlertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        int themeColor = OpUtils.getThemeColor(((FrameLayout) this).mContext);
        AlertDialog create = new AlertDialog.Builder(((FrameLayout) this).mContext, themeColor == 1 ? 16974374 : 16974394).setTitle(str).setMessage(str2).setCancelable(false).setNeutralButton(C0015R$string.notification_appops_ok, (DialogInterface.OnClickListener) null).create();
        this.mAlertDialog = create;
        if (!(((FrameLayout) this).mContext instanceof Activity)) {
            create.getWindow().setType(2009);
        }
        this.mAlertDialog.show();
        if (themeColor != 2) {
            Button button = this.mAlertDialog.getButton(-3);
            int themeAccentColor = OpUtils.getThemeAccentColor(((FrameLayout) this).mContext, C0004R$color.qs_detail_button_white);
            if (button != null && themeAccentColor > 0) {
                button.setTextColor(themeAccentColor);
            }
        }
    }

    private void showTimeoutDialog(int i, int i2) {
        int i3;
        int i4 = i2 / 1000;
        int i5 = AnonymousClass7.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[this.mSecurityModel.getSecurityMode(i).ordinal()];
        if (i5 == 1) {
            i3 = C0015R$string.kg_too_many_failed_pattern_attempts_dialog_message;
        } else if (i5 == 2) {
            i3 = C0015R$string.kg_too_many_failed_pin_attempts_dialog_message;
        } else if (i5 != 3) {
            i3 = 0;
        } else {
            i3 = C0015R$string.kg_too_many_failed_password_attempts_dialog_message;
        }
        if (i3 != 0) {
            showDialog(null, ((FrameLayout) this).mContext.getString(i3, Integer.valueOf(this.mLockPatternUtils.getCurrentFailedPasswordAttempts(i)), Integer.valueOf(i4)));
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.keyguard.KeyguardSecurityContainer$7  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass7 {
        static final /* synthetic */ int[] $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode;

        static {
            int[] iArr = new int[KeyguardSecurityModel.SecurityMode.values().length];
            $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode = iArr;
            try {
                iArr[KeyguardSecurityModel.SecurityMode.Pattern.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.PIN.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.Password.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.Invalid.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.None.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.SimPin.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[KeyguardSecurityModel.SecurityMode.SimPuk.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
        }
    }

    private void showAlmostAtWipeDialog(int i, int i2, int i3) {
        String str;
        if (i3 != 1) {
            if (i3 != 2) {
                str = i3 != 3 ? null : ((FrameLayout) this).mContext.getString(C0015R$string.kg_failed_attempts_almost_at_erase_user, Integer.valueOf(i), Integer.valueOf(i2));
            } else {
                str = ((FrameLayout) this).mContext.getString(C0015R$string.kg_failed_attempts_almost_at_erase_profile, Integer.valueOf(i), Integer.valueOf(i2));
            }
        } else if (ProductUtils.isUsvMode() && i2 == 5) {
            str = ((FrameLayout) this).mContext.getString(C0015R$string.kg_failed_attempts_almost_at_wipe_vzw_warning_first);
            ((FrameLayout) this).mContext.getString(C0015R$string.kg_failed_attempts_almost_at_wipe_vzw_title);
        } else if (!ProductUtils.isUsvMode() || i2 != 1) {
            str = ((FrameLayout) this).mContext.getString(C0015R$string.kg_failed_attempts_almost_at_wipe, Integer.valueOf(i), Integer.valueOf(i2));
        } else {
            str = ((FrameLayout) this).mContext.getString(C0015R$string.kg_failed_attempts_almost_at_wipe_vzw_warning_last);
            ((FrameLayout) this).mContext.getString(C0015R$string.kg_failed_attempts_almost_at_wipe_vzw_title);
        }
        showDialog(null, str);
    }

    private void showWipeDialog(int i, int i2) {
        String str;
        if (i2 == 1) {
            str = ((FrameLayout) this).mContext.getString(C0015R$string.kg_failed_attempts_now_wiping, Integer.valueOf(i));
        } else if (i2 != 2) {
            str = i2 != 3 ? null : ((FrameLayout) this).mContext.getString(C0015R$string.kg_failed_attempts_now_erasing_user, Integer.valueOf(i));
        } else {
            str = ((FrameLayout) this).mContext.getString(C0015R$string.kg_failed_attempts_now_erasing_profile, Integer.valueOf(i));
        }
        showDialog(null, str);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportFailedUnlockAttempt(int i, int i2) {
        int i3;
        int i4;
        int i5 = 1;
        int currentFailedPasswordAttempts = this.mLockPatternUtils.getCurrentFailedPasswordAttempts(i) + 1;
        Log.d("KeyguardSecurityView", "reportFailedPatternAttempt: #" + currentFailedPasswordAttempts);
        DevicePolicyManager devicePolicyManager = this.mLockPatternUtils.getDevicePolicyManager();
        int maximumFailedPasswordsForWipe = devicePolicyManager.getMaximumFailedPasswordsForWipe(null, i);
        int i6 = maximumFailedPasswordsForWipe > 0 ? maximumFailedPasswordsForWipe - currentFailedPasswordAttempts : Integer.MAX_VALUE;
        Log.d("KeyguardSecurityView", "reportFailedPatternAttempt: failedAttemptsBeforeWipe = " + maximumFailedPasswordsForWipe + " remainingBeforeWipe = " + i6);
        if (ProductUtils.isUsvMode()) {
            i3 = 10;
            i4 = 0;
        } else {
            i4 = i2;
            i3 = 5;
        }
        if (i6 < 5 || (ProductUtils.isUsvMode() && i6 <= i3)) {
            int profileWithMinimumFailedPasswordsForWipe = devicePolicyManager.getProfileWithMinimumFailedPasswordsForWipe(i);
            if (profileWithMinimumFailedPasswordsForWipe == i) {
                if (profileWithMinimumFailedPasswordsForWipe != 0) {
                    i5 = 3;
                }
            } else if (profileWithMinimumFailedPasswordsForWipe != -10000) {
                i5 = 2;
            }
            if (i6 > 0) {
                showAlmostAtWipeDialog(currentFailedPasswordAttempts, i6, i5);
            } else {
                Slog.i("KeyguardSecurityView", "Too many unlock attempts; user " + profileWithMinimumFailedPasswordsForWipe + " will be wiped!");
                showWipeDialog(currentFailedPasswordAttempts, i5);
            }
        }
        this.mUpdateMonitor.reportFailedStrongAuthUnlockAttempt(i);
        this.mLockPatternUtils.reportFailedPasswordAttempt(i);
        if (i4 > 0) {
            this.mLockPatternUtils.reportPasswordLockout(i4, i);
            showTimeoutDialog(i, i4);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showPrimarySecurityScreen$0 */
    public /* synthetic */ KeyguardSecurityModel.SecurityMode lambda$showPrimarySecurityScreen$0$KeyguardSecurityContainer() {
        return this.mSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
    }

    /* access modifiers changed from: package-private */
    public void showPrimarySecurityScreen(boolean z) {
        Log.v("KeyguardSecurityView", "showPrimarySecurityScreen(turningOff=" + z + ")");
        showSecurityScreen((KeyguardSecurityModel.SecurityMode) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.keyguard.-$$Lambda$KeyguardSecurityContainer$2pPkYsoLI01tKHny_UaXkNxV-qo
            @Override // java.util.function.Supplier
            public final Object get() {
                return KeyguardSecurityContainer.this.lambda$showPrimarySecurityScreen$0$KeyguardSecurityContainer();
            }
        }));
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x010a A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x011c  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0135  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x013c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean showNextSecurityScreenOrFinish(boolean r11, int r12, boolean r13) {
        /*
        // Method dump skipped, instructions count: 322
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardSecurityContainer.showNextSecurityScreenOrFinish(boolean, int, boolean):boolean");
    }

    private void showSecurityScreen(KeyguardSecurityModel.SecurityMode securityMode) {
        Log.d("KeyguardSecurityView", "showSecurityScreen(" + securityMode + ")");
        KeyguardSecurityModel.SecurityMode securityMode2 = this.mCurrentSecuritySelection;
        if (securityMode != securityMode2) {
            KeyguardSecurityView securityView = getSecurityView(securityMode2);
            KeyguardSecurityView securityView2 = getSecurityView(securityMode);
            if (securityView != null) {
                securityView.onPause();
                securityView.setKeyguardCallback(this.mNullCallback);
            }
            if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
                securityView2.onResume(2);
                securityView2.setKeyguardCallback(this.mCallback);
            }
            int childCount = this.mSecurityViewFlipper.getChildCount();
            int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
            boolean z = false;
            int i = 0;
            while (true) {
                if (i >= childCount) {
                    break;
                } else if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                    this.mSecurityViewFlipper.setDisplayedChild(i);
                    break;
                } else {
                    i++;
                }
            }
            this.mCurrentSecuritySelection = securityMode;
            this.mCurrentSecurityView = securityView2;
            SecurityCallback securityCallback = this.mSecurityCallback;
            if (securityMode != KeyguardSecurityModel.SecurityMode.None && securityView2.needsInput()) {
                z = true;
            }
            securityCallback.onSecurityModeChanged(securityMode, z);
        }
    }

    private int getSecurityViewIdForMode(KeyguardSecurityModel.SecurityMode securityMode) {
        int i = AnonymousClass7.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[securityMode.ordinal()];
        if (i == 1) {
            return C0008R$id.keyguard_pattern_view;
        }
        if (i != 2) {
            if (i == 3) {
                return C0008R$id.keyguard_password_view;
            }
            if (i == 6) {
                return C0008R$id.keyguard_sim_pin_view;
            }
            if (i != 7) {
                return 0;
            }
            return C0008R$id.keyguard_sim_puk_view;
        } else if (this.mUpdateMonitor.isAutoCheckPinEnabled()) {
            return C0008R$id.keyguard_pin_view_auto;
        } else {
            return C0008R$id.keyguard_pin_view;
        }
    }

    public int getLayoutIdFor(KeyguardSecurityModel.SecurityMode securityMode) {
        int i = AnonymousClass7.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[securityMode.ordinal()];
        if (i == 1) {
            return C0011R$layout.keyguard_pattern_view;
        }
        if (i != 2) {
            if (i == 3) {
                return C0011R$layout.keyguard_password_view;
            }
            if (i == 6) {
                return C0011R$layout.keyguard_sim_pin_view;
            }
            if (i != 7) {
                return 0;
            }
            return C0011R$layout.keyguard_sim_puk_view;
        } else if (this.mUpdateMonitor.isAutoCheckPinEnabled()) {
            return C0011R$layout.keyguard_pin_view_auto;
        } else {
            return C0011R$layout.keyguard_pin_view;
        }
    }

    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return this.mSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
    }

    public KeyguardSecurityModel.SecurityMode getCurrentSecurityMode() {
        return this.mCurrentSecuritySelection;
    }

    public KeyguardSecurityView getCurrentSecurityView() {
        return this.mCurrentSecurityView;
    }

    public KeyguardSecurityModel.SecurityMode getCurrentSecuritySelection() {
        return this.mCurrentSecuritySelection;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return this.mSecurityViewFlipper.needsInput();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mSecurityViewFlipper.setKeyguardCallback(keyguardSecurityCallback);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void reset() {
        this.mSecurityViewFlipper.reset();
        this.mDisappearAnimRunning = false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
        if (this.mCurrentSecuritySelection != KeyguardSecurityModel.SecurityMode.None) {
            if (i != 0) {
                Log.i("KeyguardSecurityView", "Strong auth required, reason: " + i);
            }
            getSecurityView(this.mCurrentSecuritySelection).showPromptReason(i);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(CharSequence charSequence, ColorStateList colorStateList) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).showMessage(charSequence, colorStateList);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean isCheckingPassword() {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            return getSecurityView(securityMode).isCheckingPassword();
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideSecurityIcon() {
        Log.d("KeyguardSecurityView", "hideSecurityIcon");
        View view = this.mSecurityIcon;
        if (view != null) {
            ((ImageView) view.findViewById(C0008R$id.security_image)).setClickable(false);
            this.mSecurityIcon.setVisibility(4);
            this.mSecurityIcon.clearAnimation();
        }
        View view2 = this.mSecurityIconSwap;
        if (view2 != null) {
            view2.setVisibility(0);
        }
        Animation animation = this.mFacelockAnimationSet;
        if (animation != null) {
            animation.setAnimationListener(null);
        }
    }

    private void updateSecurityIcon(KeyguardSecurityView keyguardSecurityView) {
        if (keyguardSecurityView != null) {
            ViewGroup viewGroup = (ViewGroup) keyguardSecurityView;
            this.mSecurityIcon = viewGroup.findViewById(C0008R$id.fingerprint_icon);
            this.mSecurityIconSwap = viewGroup.findViewById(C0008R$id.fingerprint_icon_swap);
        }
        updateSecurityIcon();
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0091  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0093  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00b7  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateSecurityIcon() {
        /*
            r7 = this;
            android.view.View r0 = r7.mSecurityIcon
            if (r0 == 0) goto L_0x00c2
            int r1 = com.android.systemui.C0008R$id.security_image
            android.view.View r0 = r0.findViewById(r1)
            android.widget.ImageView r0 = (android.widget.ImageView) r0
            com.android.keyguard.KeyguardUpdateMonitor r1 = r7.mUpdateMonitor
            boolean r1 = r1.isFacelockAvailable()
            r2 = 1
            r3 = 0
            if (r1 != 0) goto L_0x0021
            com.android.keyguard.KeyguardUpdateMonitor r1 = r7.mUpdateMonitor
            boolean r1 = r1.isFacelockRecognizing()
            if (r1 == 0) goto L_0x001f
            goto L_0x0021
        L_0x001f:
            r1 = r3
            goto L_0x0022
        L_0x0021:
            r1 = r2
        L_0x0022:
            com.android.keyguard.KeyguardUpdateMonitor r4 = r7.mUpdateMonitor
            boolean r4 = r4.isFingerprintDetectionRunning()
            com.android.keyguard.KeyguardUpdateMonitor r5 = r7.mUpdateMonitor
            boolean r5 = r5.isUnlockingWithBiometricAllowed()
            if (r5 == 0) goto L_0x003a
            com.android.keyguard.KeyguardUpdateMonitor r5 = r7.mUpdateMonitor
            boolean r5 = r5.isFingerprintLockout()
            if (r5 != 0) goto L_0x003a
            r5 = r2
            goto L_0x003b
        L_0x003a:
            r5 = r3
        L_0x003b:
            com.android.keyguard.KeyguardUpdateMonitor r6 = r7.mUpdateMonitor
            boolean r6 = r6.isKeyguardDone()
            if (r6 == 0) goto L_0x0045
        L_0x0043:
            r4 = r3
            goto L_0x0080
        L_0x0045:
            if (r1 == 0) goto L_0x004e
            int r4 = com.android.systemui.C0006R$drawable.facelock_bouncer_icon
            r0.setImageResource(r4)
            r4 = r2
            goto L_0x0080
        L_0x004e:
            boolean r6 = com.oneplus.util.OpUtils.isCustomFingerprint()
            if (r6 != 0) goto L_0x0043
            if (r4 == 0) goto L_0x0043
            if (r5 == 0) goto L_0x0043
            android.content.Context r4 = r7.getContext()
            java.lang.String r5 = "fingerprint"
            java.lang.Object r4 = r4.getSystemService(r5)
            android.hardware.fingerprint.FingerprintManager r4 = (android.hardware.fingerprint.FingerprintManager) r4
            java.util.List r5 = r4.getEnrolledFingerprints()
            if (r5 == 0) goto L_0x006f
            int r5 = r5.size()
            goto L_0x0070
        L_0x006f:
            r5 = r3
        L_0x0070:
            boolean r4 = r4.isHardwareDetected()
            if (r4 == 0) goto L_0x007a
            if (r5 <= 0) goto L_0x007a
            r4 = r2
            goto L_0x007b
        L_0x007a:
            r4 = r3
        L_0x007b:
            int r5 = com.android.systemui.C0006R$drawable.ic_fingerprint_lockscreen_blow
            r0.setImageResource(r5)
        L_0x0080:
            android.view.View r5 = r7.mSecurityIconSwap
            if (r5 == 0) goto L_0x008d
            if (r4 != 0) goto L_0x0088
            r6 = r3
            goto L_0x008a
        L_0x0088:
            r6 = 8
        L_0x008a:
            r5.setVisibility(r6)
        L_0x008d:
            android.view.View r5 = r7.mSecurityIcon
            if (r4 == 0) goto L_0x0093
            r6 = r3
            goto L_0x0094
        L_0x0093:
            r6 = 4
        L_0x0094:
            r5.setVisibility(r6)
            java.lang.String r5 = "KeyguardSecurityView"
            if (r4 == 0) goto L_0x00b3
            if (r1 == 0) goto L_0x00b3
            r0.setClickable(r2)
            com.android.keyguard.KeyguardSecurityContainer$5 r1 = new com.android.keyguard.KeyguardSecurityContainer$5
            r1.<init>()
            r0.setOnClickListener(r1)
            boolean r0 = android.os.Build.DEBUG_ONEPLUS
            if (r0 == 0) goto L_0x00bf
            java.lang.String r0 = "show bouncer face icon"
            android.util.Log.d(r5, r0)
            goto L_0x00bf
        L_0x00b3:
            boolean r1 = android.os.Build.DEBUG_ONEPLUS
            if (r1 == 0) goto L_0x00bc
            java.lang.String r1 = "hide bouncer face icon"
            android.util.Log.d(r5, r1)
        L_0x00bc:
            r0.setClickable(r3)
        L_0x00bf:
            r7.updateIconAnimation()
        L_0x00c2:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardSecurityContainer.updateSecurityIcon():void");
    }

    private void updateIconAnimation() {
        if (this.mSecurityIcon != null && this.mFacelockAnimationSet != null) {
            if (!this.mUpdateMonitor.isFacelockRecognizing() || !this.mAppearAnimationStarted) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("KeyguardSecurityView", "stop anim");
                }
                this.mSecurityIcon.clearAnimation();
                this.mFacelockAnimationSet.setAnimationListener(null);
                return;
            }
            this.mFacelockAnimationSet.setAnimationListener(new Animation.AnimationListener() { // from class: com.android.keyguard.KeyguardSecurityContainer.6
                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationRepeat(Animation animation) {
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationStart(Animation animation) {
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationEnd(Animation animation) {
                    if (KeyguardSecurityContainer.this.mUpdateMonitor.isFacelockRecognizing() && KeyguardSecurityContainer.this.mFacelockAnimationSet != null && KeyguardSecurityContainer.this.mSecurityIcon != null) {
                        if (Build.DEBUG_ONEPLUS) {
                            Log.d("KeyguardSecurityView", "start again");
                        }
                        KeyguardSecurityContainer.this.mFacelockAnimationSet.setAnimationListener(this);
                        KeyguardSecurityContainer.this.mSecurityIcon.startAnimation(KeyguardSecurityContainer.this.mFacelockAnimationSet);
                    }
                }
            });
            if (Build.DEBUG_ONEPLUS) {
                Log.d("KeyguardSecurityView", "start anim");
            }
            this.mSecurityIcon.startAnimation(this.mFacelockAnimationSet);
        }
    }
}
