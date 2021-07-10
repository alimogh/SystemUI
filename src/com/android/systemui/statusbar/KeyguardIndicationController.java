package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.biometrics.BiometricSourceType;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.R$styleable;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.internal.widget.ViewClippingUtil;
import com.android.keyguard.KeyguardStatusView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.settingslib.Utils;
import com.android.systemui.C0000R$anim;
import com.android.systemui.C0002R$attr;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0014R$raw;
import com.android.systemui.C0015R$string;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dock.DockManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import com.android.systemui.statusbar.phone.KeyguardIndicationTextView;
import com.android.systemui.statusbar.phone.LockscreenLockIconController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.wakelock.SettableWakeLock;
import com.android.systemui.util.wakelock.WakeLock;
import com.oneplus.battery.OpBatteryStatus;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.IllegalFormatConversionException;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class KeyguardIndicationController implements StatusBarStateController.StateListener, ConfigurationController.ConfigurationListener, KeyguardStateController.Callback {
    private static int CHARGING_INFO_ANITMAION_DURATION = 100;
    private String mAlignmentIndication;
    private AudioManager mAudioManager;
    private final IBatteryStats mBatteryInfo;
    private int mBatteryLevel;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private BroadcastReceiver mBroadcastReceiver;
    private TextView mChargingInfo;
    private LinearLayout mChargingInfoContainer;
    private TextView mChargingInfoLevel;
    private ValueAnimator mChargingInfofadeInAnimation;
    private ValueAnimator mChargingInfofadeOutAnimation;
    private SoundPool mChargingSound;
    private int mChargingSoundId;
    private int mChargingSpeed;
    private long mChargingTimeRemaining;
    private int mChargingWattage;
    private final Context mContext;
    private AnimationDrawable mDashAnimation;
    private FrameLayout mDashContainer;
    private ImageView mDashView;
    private final DevicePolicyManager mDevicePolicyManager;
    private KeyguardIndicationTextView mDisclosure;
    private float mDisclosureMaxAlpha;
    private final DockManager mDockManager;
    private boolean mDozing;
    AnimatorSet mFadeOutAnimatorSet;
    private final Handler mHandler = new Handler() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.12
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                KeyguardIndicationController.this.hideTransientIndication();
            } else if (i == 2) {
                if (KeyguardIndicationController.this.mLockIconController != null) {
                    KeyguardIndicationController.this.mLockIconController.setTransientBiometricsError(false);
                }
            } else if (i == 3) {
                KeyguardIndicationController.this.showSwipeUpToUnlock();
            } else if (i == 102) {
                Log.d("KeyguardIndication", "MSG_PLAY_FAST_CHARGE_ANIMATION");
                KeyguardIndicationController.this.mChargingInfofadeInAnimation.cancel();
                KeyguardIndicationController.this.mChargingInfoContainer.setVisibility(8);
                KeyguardIndicationController.this.mDashView.setVisibility(0);
                if (KeyguardIndicationController.this.mDashAnimation != null) {
                    KeyguardIndicationController.this.mDashAnimation.start();
                }
                KeyguardIndicationController.this.playFastWarpChargingSound();
                KeyguardIndicationController.this.mHandler.sendEmptyMessageDelayed(R$styleable.Constraint_layout_goneMarginTop, 900);
            } else if (i == 103) {
                Log.d("KeyguardIndication", "MSG_PLAY_FADE_OUT_ANIMATION");
                KeyguardIndicationController.this.mFadeOutAnimatorSet.start();
            }
        }
    };
    private boolean mHideTransientMessageOnScreenOff;
    private ViewGroup mIndicationArea;
    private ViewGroup mInfoView;
    private ColorStateList mInitialTextColorState;
    private KeyguardBottomAreaView mKeyguardBottomArea;
    private KeyguardStateCallback mKeyguardStateCallback;
    private final KeyguardStateController mKeyguardStateController;
    private KeyguardStatusView mKeyguardStatusView;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private KeyguardViewMediator mKeyguardViewMediator;
    private int mLastChargingSpeed;
    private LockscreenLockIconController mLockIconController;
    private String mMessageToShowOnScreenOn;
    private View mOwnerInfo;
    private boolean mPdCharging;
    private boolean mPowerCharged;
    private boolean mPowerPluggedIn;
    private boolean mPowerPluggedInWired;
    private boolean mProtectCharging;
    private String mRestingIndication;
    private boolean mShowingError = false;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final StatusBarStateController mStatusBarStateController;
    private KeyguardIndicationTextView mTextView;
    private final KeyguardUpdateMonitorCallback mTickReceiver = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.11
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTimeChanged() {
            if (KeyguardIndicationController.this.mVisible) {
                KeyguardIndicationController.this.updateIndication(false);
            }
        }
    };
    private CharSequence mTransientIndication;
    private boolean mTransientTextIsError;
    private KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private final UserManager mUserManager;
    private boolean mVisible;
    private final SettableWakeLock mWakeLock;
    private boolean mWirelessChargingDeviated;

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.KeyguardIndicationController$1  reason: invalid class name */
    public class AnonymousClass1 implements ViewClippingUtil.ClippingParameters {
    }

    private String getTrustManagedIndication() {
        return null;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
    }

    private ValueAnimator chargingInfoFadeInAnimation() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        final ViewGroup.LayoutParams layoutParams = this.mChargingInfoContainer.getLayoutParams();
        final int chargingInfoHeight = getChargingInfoHeight();
        ofFloat.setDuration((long) CHARGING_INFO_ANITMAION_DURATION);
        ofFloat.setInterpolator(Interpolators.LINEAR);
        ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                layoutParams.height = 0;
                KeyguardIndicationController.this.mChargingInfoContainer.setLayoutParams(layoutParams);
                KeyguardIndicationController.this.mChargingInfoContainer.requestLayout();
                KeyguardIndicationController.this.mChargingInfoContainer.setVisibility(4);
                Log.d("KeyguardIndication", "chargingInfoFadeInAnimation onAnimationStart, mChargingInfoContainer:" + KeyguardIndicationController.this.mChargingInfoContainer + ", mChargingInfoContainer.id:" + KeyguardIndicationController.this.mChargingInfoContainer.getId());
                if (KeyguardIndicationController.this.mInfoView != null) {
                    Log.d("KeyguardIndication", "onAnimationStart, mInfoView.getVisibility():" + KeyguardIndicationController.this.mInfoView.getVisibility() + ", mInfoViewID:" + KeyguardIndicationController.this.mInfoView.getId());
                    return;
                }
                Log.d("KeyguardIndication", "onAnimationStart, mInfoView: null");
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                layoutParams.height = chargingInfoHeight;
                KeyguardIndicationController.this.mChargingInfoContainer.setLayoutParams(layoutParams);
                KeyguardIndicationController.this.mChargingInfoContainer.requestLayout();
                Log.d("KeyguardIndication", "chargingInfoFadeInAnimation onAnimationEnd / height:" + chargingInfoHeight + ", mChargingInfoContainer.visib:" + KeyguardIndicationController.this.mChargingInfoContainer.getVisibility());
                if (KeyguardIndicationController.this.mInfoView != null) {
                    Log.d("KeyguardIndication", "onAnimationEnd, mInfoView.getVisibility():" + KeyguardIndicationController.this.mInfoView.getVisibility() + ", mInfoViewID:" + KeyguardIndicationController.this.mInfoView.getId());
                    return;
                }
                Log.d("KeyguardIndication", "onAnimationEnd, mInfoView: null");
            }
        });
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardIndicationController.this.mChargingInfoContainer.setVisibility(0);
                layoutParams.height = (int) (((Float) valueAnimator.getAnimatedValue()).floatValue() * ((float) chargingInfoHeight));
                KeyguardIndicationController.this.mChargingInfoContainer.setLayoutParams(layoutParams);
                KeyguardIndicationController.this.mChargingInfoContainer.requestLayout();
            }
        });
        return ofFloat;
    }

    private ValueAnimator chargingInfoFadeOutAnimation() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        final ViewGroup.LayoutParams layoutParams = this.mChargingInfoContainer.getLayoutParams();
        final int chargingInfoHeight = getChargingInfoHeight();
        ofFloat.setDuration((long) CHARGING_INFO_ANITMAION_DURATION);
        ofFloat.setInterpolator(Interpolators.LINEAR);
        ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.4
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                Log.d("KeyguardIndication", "chargingInfoFadeOutAnimation onAnimationStart");
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                KeyguardIndicationController.this.mKeyguardStatusView.setCharging(false);
                KeyguardIndicationController.this.mChargingInfoContainer.setVisibility(8);
                layoutParams.height = chargingInfoHeight;
                KeyguardIndicationController.this.mChargingInfoContainer.setLayoutParams(layoutParams);
                KeyguardIndicationController.this.mChargingInfoContainer.requestLayout();
                Log.d("KeyguardIndication", "chargingInfoFadeOutAnimation onAnimationEnd");
            }
        });
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                layoutParams.height = (int) ((1.0f - ((Float) valueAnimator.getAnimatedValue()).floatValue()) * ((float) chargingInfoHeight));
                KeyguardIndicationController.this.mChargingInfoContainer.setLayoutParams(layoutParams);
                KeyguardIndicationController.this.mChargingInfoContainer.requestLayout();
            }
        });
        return ofFloat;
    }

    private AnimatorSet getFadeOutAnimation() {
        new ValueAnimator();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(800L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.6
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardIndicationController.this.mChargingInfo.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(1.0f, 0.0f);
        ofFloat2.setDuration(800L);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.7
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardIndicationController.this.mDashContainer.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(800L);
        animatorSet.addListener(new Animator.AnimatorListener() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.8
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                String computePowerIndication = KeyguardIndicationController.this.computePowerIndication();
                String substring = computePowerIndication.substring(0, computePowerIndication.indexOf("•") + 1);
                String substring2 = computePowerIndication.substring(computePowerIndication.indexOf("•") + 1);
                KeyguardIndicationController.this.mChargingInfoLevel.setText(substring);
                KeyguardIndicationController.this.updateChargingInfo(substring2);
                KeyguardIndicationController.this.mChargingInfoContainer.setVisibility(0);
                KeyguardIndicationController.this.mChargingInfoContainer.setAlpha(0.0f);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                KeyguardIndicationController.this.mDashContainer.setVisibility(8);
                KeyguardIndicationController.this.mDashContainer.setAlpha(1.0f);
                KeyguardIndicationController.this.mChargingInfoContainer.setAlpha(1.0f);
            }
        });
        animatorSet.playTogether(ofFloat2, ofFloat);
        return animatorSet;
    }

    KeyguardIndicationController(Context context, WakeLock.Builder builder, KeyguardStateController keyguardStateController, StatusBarStateController statusBarStateController, KeyguardUpdateMonitor keyguardUpdateMonitor, DockManager dockManager, BroadcastDispatcher broadcastDispatcher, DevicePolicyManager devicePolicyManager, IBatteryStats iBatteryStats, UserManager userManager) {
        this.mContext = context;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mDevicePolicyManager = devicePolicyManager;
        this.mKeyguardStateController = keyguardStateController;
        this.mStatusBarStateController = statusBarStateController;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mDockManager = dockManager;
        dockManager.addAlignmentStateListener(new DockManager.AlignmentStateListener() { // from class: com.android.systemui.statusbar.-$$Lambda$KeyguardIndicationController$MNRKvB1L0H3Iaik26PzOwQaf05I
        });
        builder.setTag("Doze:KeyguardIndication");
        this.mWakeLock = new SettableWakeLock(builder.build(), "KeyguardIndication");
        this.mBatteryInfo = iBatteryStats;
        this.mUserManager = userManager;
        this.mKeyguardUpdateMonitor.registerCallback(getKeyguardCallback());
        this.mKeyguardUpdateMonitor.registerCallback(this.mTickReceiver);
        this.mStatusBarStateController.addCallback(this);
        this.mKeyguardStateController.addCallback(this);
        if (OpUtils.isCustomFingerprint()) {
            this.mInitialTextColorState = Utils.getColorAttr(this.mContext, C0002R$attr.wallpaperTextColor);
            KeyguardIndicationTextView keyguardIndicationTextView = this.mTextView;
            if (keyguardIndicationTextView != null) {
                keyguardIndicationTextView.setTypeface(Typeface.DEFAULT);
            }
        }
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        SoundPool soundPool = new SoundPool(1, 1, 0);
        this.mChargingSound = soundPool;
        this.mChargingSoundId = soundPool.load(this.mContext, C0014R$raw.charging, 1);
    }

    public void setIndicationArea(ViewGroup viewGroup) {
        this.mIndicationArea = viewGroup;
        KeyguardIndicationTextView keyguardIndicationTextView = (KeyguardIndicationTextView) viewGroup.findViewById(C0008R$id.keyguard_indication_text);
        this.mTextView = keyguardIndicationTextView;
        this.mInitialTextColorState = keyguardIndicationTextView != null ? keyguardIndicationTextView.getTextColors() : ColorStateList.valueOf(-1);
        KeyguardIndicationTextView keyguardIndicationTextView2 = (KeyguardIndicationTextView) viewGroup.findViewById(C0008R$id.keyguard_indication_enterprise_disclosure);
        this.mDisclosure = keyguardIndicationTextView2;
        this.mDisclosureMaxAlpha = keyguardIndicationTextView2.getAlpha();
        updateIndication(false);
        updateDisclosure();
        if (this.mBroadcastReceiver == null) {
            this.mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.KeyguardIndicationController.9
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    KeyguardIndicationController.this.updateDisclosure();
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
            intentFilter.addAction("android.intent.action.USER_REMOVED");
            this.mBroadcastDispatcher.registerReceiver(this.mBroadcastReceiver, intentFilter);
        }
    }

    private int getChargingInfoHeight() {
        return OpUtils.SUPPORT_WARP_CHARGING ? this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_owner_info_font_size) + this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.charging_dash_margin_top) : this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_keyguard_indication_height);
    }

    private void updateChargingInfoAndOwnerInfo() {
        updateChargingInfoAndOwnerInfo(false);
    }

    private void updateChargingInfoAndOwnerInfo(boolean z) {
        if (this.mChargingInfo != null && this.mOwnerInfo != null && this.mChargingInfoLevel != null) {
            int chargingInfoHeight = getChargingInfoHeight();
            Log.d("KeyguardIndication", "updateChargingInfoAndOwnerInfo, height:" + chargingInfoHeight);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mChargingInfoContainer.getLayoutParams();
            layoutParams.height = chargingInfoHeight;
            this.mChargingInfoContainer.setLayoutParams(layoutParams);
            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mOwnerInfo.getLayoutParams();
            layoutParams2.height = chargingInfoHeight;
            this.mOwnerInfo.setLayoutParams(layoutParams2);
            if (z) {
                if (this.mPowerPluggedIn && this.mChargingSpeed >= 0) {
                    String computePowerIndication = computePowerIndication();
                    String substring = computePowerIndication.substring(0, computePowerIndication.indexOf("•") + 1);
                    String substring2 = computePowerIndication.substring(computePowerIndication.indexOf("•") + 1);
                    this.mChargingInfoLevel.setText(substring);
                    updateChargingInfo(substring2);
                    this.mChargingInfoContainer.setVisibility(0);
                    this.mKeyguardStatusView.setCharging(true);
                }
                this.mChargingInfoContainer.requestLayout();
                this.mOwnerInfo.requestLayout();
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        ValueAnimator valueAnimator = this.mChargingInfofadeInAnimation;
        if (valueAnimator != null && valueAnimator.isStarted()) {
            this.mChargingInfofadeInAnimation.cancel();
        }
        this.mChargingInfofadeInAnimation = chargingInfoFadeInAnimation();
        ValueAnimator valueAnimator2 = this.mChargingInfofadeOutAnimation;
        if (valueAnimator2 != null && valueAnimator2.isStarted()) {
            this.mChargingInfofadeOutAnimation.cancel();
        }
        this.mChargingInfofadeOutAnimation = chargingInfoFadeOutAnimation();
        updateChargingInfoAndOwnerInfo();
    }

    private void registerKeyguardStateCallbackCallbacks() {
        getKeyguardStateCallback();
        this.mKeyguardViewMediator.addStateMonitorCallback(this.mKeyguardStateCallback);
    }

    public void setLockIconController(LockscreenLockIconController lockscreenLockIconController) {
        this.mLockIconController = lockscreenLockIconController;
    }

    /* access modifiers changed from: protected */
    public KeyguardUpdateMonitorCallback getKeyguardCallback() {
        if (this.mUpdateMonitorCallback == null) {
            this.mUpdateMonitorCallback = new BaseKeyguardCallback();
        }
        return this.mUpdateMonitorCallback;
    }

    private void getKeyguardStateCallback() {
        if (this.mKeyguardStateCallback == null) {
            this.mKeyguardStateCallback = new KeyguardStateCallback(this, null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDisclosure() {
        if (((Boolean) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.statusbar.-$$Lambda$KeyguardIndicationController$z0kELVO5O0J_Wr2PuJE1CflZShk
            @Override // java.util.function.Supplier
            public final Object get() {
                return Boolean.valueOf(KeyguardIndicationController.this.isOrganizationOwnedDevice());
            }
        })).booleanValue()) {
            CharSequence organizationOwnedDeviceOrganizationName = getOrganizationOwnedDeviceOrganizationName();
            if (organizationOwnedDeviceOrganizationName != null) {
                this.mDisclosure.switchIndication(this.mContext.getResources().getString(C0015R$string.do_disclosure_with_name, organizationOwnedDeviceOrganizationName));
            } else {
                this.mDisclosure.switchIndication(C0015R$string.do_disclosure_generic);
            }
            this.mDisclosure.setVisibility(0);
            return;
        }
        this.mDisclosure.setVisibility(8);
    }

    /* access modifiers changed from: private */
    public boolean isOrganizationOwnedDevice() {
        return this.mDevicePolicyManager.isDeviceManaged() || this.mDevicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile();
    }

    private CharSequence getOrganizationOwnedDeviceOrganizationName() {
        if (this.mDevicePolicyManager.isDeviceManaged()) {
            return this.mDevicePolicyManager.getDeviceOwnerOrganizationName();
        }
        if (this.mDevicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile()) {
            return getWorkProfileOrganizationName();
        }
        return null;
    }

    private CharSequence getWorkProfileOrganizationName() {
        int workProfileUserId = getWorkProfileUserId(UserHandle.myUserId());
        if (workProfileUserId == -10000) {
            return null;
        }
        return this.mDevicePolicyManager.getOrganizationNameForUser(workProfileUserId);
    }

    private int getWorkProfileUserId(int i) {
        for (UserInfo userInfo : this.mUserManager.getProfiles(i)) {
            if (userInfo.isManagedProfile()) {
                return userInfo.id;
            }
        }
        return -10000;
    }

    public void setVisible(boolean z) {
        this.mVisible = z;
        this.mIndicationArea.setVisibility(z ? 0 : 8);
        if (z) {
            if (!this.mHandler.hasMessages(1)) {
                hideTransientIndication();
            }
            updateIndication(false);
        } else if (!z) {
            hideTransientIndication();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String getTrustGrantedIndication() {
        if (this.mKeyguardUpdateMonitor.canSkipBouncerByFacelock()) {
            return this.mContext.getString(C0015R$string.op_keyguard_indication_face_unlocked);
        }
        return this.mContext.getString(C0015R$string.op_keyguard_indication_trust_unlocked);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setPowerPluggedIn(boolean z) {
        this.mPowerPluggedIn = z;
    }

    public void hideTransientIndicationDelayed(long j) {
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(1), j);
    }

    public void showTransientIndication(int i) {
        showTransientIndication(this.mContext.getResources().getString(i));
    }

    public void showTransientIndication(CharSequence charSequence) {
        showTransientIndication(charSequence, false, false);
    }

    public void showTransientIndication(CharSequence charSequence, boolean z, boolean z2) {
        this.mTransientIndication = charSequence;
        this.mHideTransientMessageOnScreenOff = z2 && charSequence != null;
        this.mTransientTextIsError = z;
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(3);
        if (this.mDozing && !TextUtils.isEmpty(this.mTransientIndication)) {
            this.mWakeLock.setAcquired(true);
            hideTransientIndicationDelayed(5000);
        }
        updateIndication(false);
        if (this.mShowingError) {
            this.mShowingError = false;
            animateErrorText(this.mTextView);
        }
    }

    private void animateErrorText(KeyguardIndicationTextView keyguardIndicationTextView) {
        Animator loadAnimator = AnimatorInflater.loadAnimator(this.mContext, C0000R$anim.oneplus_control_text_error_message_anim);
        if (loadAnimator != null) {
            loadAnimator.cancel();
            loadAnimator.setTarget(keyguardIndicationTextView);
            loadAnimator.start();
        }
    }

    public void hideTransientIndication() {
        if (this.mTransientIndication != null) {
            this.mTransientIndication = null;
            this.mHideTransientMessageOnScreenOff = false;
            this.mHandler.removeMessages(1);
            updateIndication(false);
        }
    }

    /* access modifiers changed from: protected */
    public final void updateIndication(boolean z) {
        if (TextUtils.isEmpty(this.mTransientIndication)) {
            this.mWakeLock.setAcquired(false);
        }
        if (!this.mVisible) {
            return;
        }
        if (this.mDozing) {
            this.mTextView.setTextColor(-1);
            if (!TextUtils.isEmpty(this.mTransientIndication)) {
                this.mTextView.switchIndication(this.mTransientIndication);
            } else if (!TextUtils.isEmpty(this.mAlignmentIndication)) {
                this.mTextView.switchIndication(this.mAlignmentIndication);
                this.mTextView.setTextColor(this.mContext.getColor(C0004R$color.misalignment_text_color));
            } else {
                this.mTextView.switchIndication(NumberFormat.getPercentInstance().format((double) (((float) this.mBatteryLevel) / 100.0f)));
            }
        } else {
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            String trustGrantedIndication = getTrustGrantedIndication();
            String trustManagedIndication = getTrustManagedIndication();
            String str = null;
            if (this.mPowerPluggedIn) {
                str = computePowerIndication();
            }
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.i("KeyguardIndication", "updateIndication 2 , trustGrantedIndication:" + trustGrantedIndication + ", trustManagedIndication:" + trustManagedIndication + ", mPowerPluggedIn:" + this.mPowerPluggedIn + ", powerIndication:" + str + ", mKeyguardUpdateMonitor.isUserUnlocked(userId):" + this.mKeyguardUpdateMonitor.isUserUnlocked(currentUser) + ", mTransientIndication:" + ((Object) this.mTransientIndication) + ", mAlignmentIndication:" + this.mAlignmentIndication + ", mRestingIndication:" + this.mRestingIndication);
            }
            if (!this.mKeyguardUpdateMonitor.isUserUnlocked(currentUser)) {
                this.mTextView.switchIndication(17040493);
            } else if (!TextUtils.isEmpty(this.mTransientIndication)) {
                this.mTextView.switchIndication(this.mTransientIndication);
            } else if (!TextUtils.isEmpty(trustGrantedIndication) && this.mKeyguardUpdateMonitor.getUserHasTrust(currentUser)) {
                this.mTextView.switchIndication(trustGrantedIndication);
            } else if (!TextUtils.isEmpty(this.mAlignmentIndication)) {
                this.mTextView.switchIndication(this.mAlignmentIndication);
            } else if (TextUtils.isEmpty(trustManagedIndication) || !this.mKeyguardUpdateMonitor.getUserTrustIsManaged(currentUser) || this.mKeyguardUpdateMonitor.getUserHasTrust(currentUser)) {
                this.mTextView.switchIndication(this.mRestingIndication);
            } else {
                this.mTextView.switchIndication(trustManagedIndication);
            }
            this.mTextView.setTextColor(this.mInitialTextColorState);
            if (this.mPowerPluggedIn && this.mChargingInfoLevel != null && this.mChargingInfo != null) {
                String computePowerIndication = computePowerIndication();
                String substring = computePowerIndication.substring(0, computePowerIndication.indexOf("•") + 1);
                String substring2 = computePowerIndication.substring(computePowerIndication.indexOf("•") + 1);
                this.mChargingInfoLevel.setText(substring);
                updateChargingInfo(substring2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public String computePowerIndication() {
        int i;
        if (this.mBatteryLevel >= 100) {
            return this.mContext.getResources().getString(C0015R$string.keyguard_charged);
        }
        boolean z = this.mChargingTimeRemaining > 0;
        String str = "";
        if (this.mPowerPluggedInWired) {
            int i2 = this.mChargingSpeed;
            if (i2 != 0) {
                if (i2 != 2) {
                    if (i2 != 3) {
                        if (z) {
                            i = C0015R$string.op_keyguard_indication_charging_time;
                        } else {
                            i = C0015R$string.keyguard_plugged_in;
                        }
                    } else if (z) {
                        i = C0015R$string.op_keyguard_plugged_in_charging_time_warp;
                    } else {
                        i = C0015R$string.keyguard_plugged_in_charging_warp;
                    }
                } else if (z) {
                    i = C0015R$string.op_keyguard_indication_charging_time_fast;
                } else {
                    i = C0015R$string.keyguard_plugged_in_charging_fast;
                }
            } else if (z) {
                i = C0015R$string.op_keyguard_indication_charging_time_slowly;
            } else {
                i = C0015R$string.keyguard_plugged_in_charging_slowly;
            }
            if (this.mPdCharging) {
                if (z) {
                    i = C0015R$string.op_keyguard_indication_charging_time_fast;
                } else {
                    i = C0015R$string.keyguard_plugged_in_charging_fast;
                }
            }
        } else {
            if (this.mChargingSpeed == 4) {
                if (z) {
                    i = C0015R$string.op_keyguard_plugged_in_charging_time_wireless_warp;
                } else {
                    i = C0015R$string.keyguard_plugged_in_wireless_warp;
                }
            } else if (z) {
                i = C0015R$string.op_keyguard_indication_charging_time_wireless;
            } else {
                i = C0015R$string.keyguard_plugged_in_wireless;
            }
            if (this.mWirelessChargingDeviated) {
                str = this.mContext.getResources().getString(C0015R$string.op_wireless_charge_deviated_label);
            }
        }
        String format = NumberFormat.getPercentInstance().format((double) (((float) this.mBatteryLevel) / 100.0f));
        if (!this.mPowerPluggedInWired && this.mWirelessChargingDeviated && SystemProperties.getBoolean("debug.wireless_charging_deviated", false)) {
            return format + " " + str;
        } else if (z) {
            String formatShortElapsedTimeRoundingUpToMinutes = Formatter.formatShortElapsedTimeRoundingUpToMinutes(this.mContext, this.mChargingTimeRemaining);
            Locale locale = this.mContext.getResources().getConfiguration().locale;
            String language = locale.getLanguage();
            String country = locale.getCountry();
            if ("zh".equals(language) && "CN".equals(country)) {
                formatShortElapsedTimeRoundingUpToMinutes = addChargingTimeTextSpace(formatShortElapsedTimeRoundingUpToMinutes);
            }
            try {
                return this.mContext.getResources().getString(i, formatShortElapsedTimeRoundingUpToMinutes, format);
            } catch (IllegalFormatConversionException unused) {
                return this.mContext.getResources().getString(i, formatShortElapsedTimeRoundingUpToMinutes);
            }
        } else {
            try {
                return this.mContext.getResources().getString(i, format);
            } catch (IllegalFormatConversionException unused2) {
                return this.mContext.getResources().getString(i);
            }
        }
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showSwipeUpToUnlock() {
        if (!this.mDozing) {
            if (this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                this.mStatusBarKeyguardViewManager.showBouncerMessage(this.mContext.getString(C0015R$string.keyguard_retry), this.mInitialTextColorState);
            } else if (this.mKeyguardUpdateMonitor.isScreenOn()) {
                showTransientIndication(this.mContext.getString(C0015R$string.keyguard_unlock), false, true);
                hideTransientIndicationDelayed(5000);
            }
        }
    }

    public void setDozing(boolean z) {
        if (this.mDozing != z) {
            this.mDozing = z;
            if (!this.mHideTransientMessageOnScreenOff || !z) {
                updateIndication(false);
            } else {
                hideTransientIndication();
            }
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardIndicationController:");
        printWriter.println("  mTransientTextIsError: " + this.mTransientTextIsError);
        printWriter.println("  mInitialTextColorState: " + this.mInitialTextColorState);
        printWriter.println("  mPowerPluggedInWired: " + this.mPowerPluggedInWired);
        printWriter.println("  mPowerPluggedIn: " + this.mPowerPluggedIn);
        printWriter.println("  mPowerCharged: " + this.mPowerCharged);
        printWriter.println("  mChargingSpeed: " + this.mChargingSpeed);
        printWriter.println("  mChargingWattage: " + this.mChargingWattage);
        printWriter.println("  mMessageToShowOnScreenOn: " + this.mMessageToShowOnScreenOn);
        printWriter.println("  mDozing: " + this.mDozing);
        printWriter.println("  mBatteryLevel: " + this.mBatteryLevel);
        StringBuilder sb = new StringBuilder();
        sb.append("  mTextView.getText(): ");
        KeyguardIndicationTextView keyguardIndicationTextView = this.mTextView;
        sb.append((Object) (keyguardIndicationTextView == null ? null : keyguardIndicationTextView.getText()));
        printWriter.println(sb.toString());
        printWriter.println("  computePowerIndication(): " + computePowerIndication());
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        setDozing(z);
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozeAmountChanged(float f, float f2) {
        this.mDisclosure.setAlpha((1.0f - f) * this.mDisclosureMaxAlpha);
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onUnlockedChanged() {
        updateIndication(!this.mDozing);
    }

    /* access modifiers changed from: protected */
    public class BaseKeyguardCallback extends KeyguardUpdateMonitorCallback {
        protected BaseKeyguardCallback() {
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshBatteryInfo(OpBatteryStatus opBatteryStatus) {
            int i = opBatteryStatus.status;
            boolean z = i == 2 || i == 5;
            boolean z2 = KeyguardIndicationController.this.mPowerPluggedIn;
            KeyguardIndicationController.this.mPowerPluggedInWired = opBatteryStatus.isPluggedInWired() && (KeyguardIndicationController.this.mPowerPluggedInWired || z);
            KeyguardIndicationController.this.mPowerPluggedIn = opBatteryStatus.isPluggedIn() && (KeyguardIndicationController.this.mPowerPluggedIn || z);
            KeyguardIndicationController.this.mPowerCharged = opBatteryStatus.isCharged();
            KeyguardIndicationController.this.mChargingWattage = opBatteryStatus.maxChargingWattage;
            KeyguardIndicationController keyguardIndicationController = KeyguardIndicationController.this;
            keyguardIndicationController.mLastChargingSpeed = keyguardIndicationController.mChargingSpeed;
            KeyguardIndicationController keyguardIndicationController2 = KeyguardIndicationController.this;
            keyguardIndicationController2.mChargingSpeed = opBatteryStatus.getChargingSpeed(keyguardIndicationController2.mContext);
            KeyguardIndicationController.this.mBatteryLevel = opBatteryStatus.level;
            KeyguardIndicationController.this.mWirelessChargingDeviated = opBatteryStatus.isWirelessChargingDeviated();
            KeyguardIndicationController.this.mPdCharging = opBatteryStatus.isPdCharging();
            KeyguardIndicationController.this.mProtectCharging = opBatteryStatus.isProtectCharging();
            Log.d("KeyguardIndication", "onRefreshBatteryInfo: plugged:" + KeyguardIndicationController.this.mPowerPluggedIn + ", wasPluggedIn: " + z2 + ", charged:" + KeyguardIndicationController.this.mPowerCharged + ", level:" + KeyguardIndicationController.this.mBatteryLevel + ", speed:" + KeyguardIndicationController.this.mChargingSpeed + ", last speed:" + KeyguardIndicationController.this.mLastChargingSpeed + ", visible:" + KeyguardIndicationController.this.mVisible + ", mWirelessChargingDeviated:" + KeyguardIndicationController.this.mWirelessChargingDeviated + ", isPdCharging:" + KeyguardIndicationController.this.mPdCharging + ", isProtectCharging:" + KeyguardIndicationController.this.mProtectCharging + ", timeToFull:" + opBatteryStatus.getSwarpRemainingTime() + ", mChargingInfo:" + KeyguardIndicationController.this.mChargingInfo);
            try {
                KeyguardIndicationController.this.mChargingTimeRemaining = KeyguardIndicationController.this.mPowerPluggedIn ? KeyguardIndicationController.this.mBatteryInfo.computeChargeTimeRemaining() : -1;
            } catch (RemoteException e) {
                Log.e("KeyguardIndication", "Error calling IBatteryStats: ", e);
                KeyguardIndicationController.this.mChargingTimeRemaining = -1;
            }
            Log.d("KeyguardIndication", "onRefreshBatteryInfo: aosp ChargingTimeRemaining " + KeyguardIndicationController.this.mChargingTimeRemaining);
            if (opBatteryStatus.getSwarpRemainingTime() > 0) {
                KeyguardIndicationController.this.mChargingTimeRemaining = opBatteryStatus.getSwarpRemainingTime() * 1000;
                Log.d("KeyguardIndication", "onRefreshBatteryInfo: swarp ChargingTimeRemaining " + KeyguardIndicationController.this.mChargingTimeRemaining);
            }
            if (KeyguardIndicationController.this.mProtectCharging) {
                KeyguardIndicationController.this.mChargingTimeRemaining = 0;
            }
            if (KeyguardIndicationController.this.mInfoView != null) {
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.d("KeyguardIndication", "onRefreshBatteryInfo, mInfoView.getVisibility():" + KeyguardIndicationController.this.mInfoView.getVisibility());
                }
            } else if (OpUtils.DEBUG_ONEPLUS) {
                Log.d("KeyguardIndication", "onRefreshBatteryInfo, mInfoView: null");
            }
            boolean z3 = KeyguardIndicationController.this.isFastCharge() && !KeyguardIndicationController.this.mPdCharging;
            String computePowerIndication = KeyguardIndicationController.this.computePowerIndication();
            if (KeyguardIndicationController.this.mChargingInfoLevel != null && KeyguardIndicationController.this.mChargingInfo != null) {
                if (KeyguardIndicationController.this.mPowerPluggedIn && (!z3 || KeyguardIndicationController.this.mLastChargingSpeed == KeyguardIndicationController.this.mChargingSpeed)) {
                    String substring = computePowerIndication.substring(0, computePowerIndication.indexOf("•") + 1);
                    String substring2 = computePowerIndication.substring(computePowerIndication.indexOf("•") + 1);
                    KeyguardIndicationController.this.mChargingInfoLevel.setText(substring);
                    KeyguardIndicationController.this.updateChargingInfo(substring2);
                }
                if (!z2 && KeyguardIndicationController.this.mPowerPluggedIn) {
                    if (!z3) {
                        KeyguardIndicationController.this.mChargingInfofadeOutAnimation.cancel();
                        if (KeyguardIndicationController.this.mInfoView != null) {
                            Log.d("KeyguardIndication", "!playFastChargeAnimation, mInfoView.getVisibility():" + KeyguardIndicationController.this.mInfoView.getVisibility() + ", mInfoViewID:" + KeyguardIndicationController.this.mInfoView.getId());
                        } else {
                            Log.d("KeyguardIndication", "!playFastChargeAnimation, mInfoView: null");
                        }
                        if (KeyguardIndicationController.this.mChargingInfoContainer.getVisibility() == 0 || KeyguardIndicationController.this.mInfoView.getVisibility() == 0) {
                            KeyguardIndicationController.this.mChargingInfoContainer.setVisibility(0);
                        } else {
                            Log.i("KeyguardIndication", "before mChargingInfofadeInAnimation.start");
                            KeyguardIndicationController.this.mChargingInfofadeInAnimation.start();
                        }
                    }
                    KeyguardIndicationController.this.mKeyguardStatusView.setCharging(true);
                } else if (z2 && !KeyguardIndicationController.this.mPowerPluggedIn) {
                    if (KeyguardIndicationController.this.mKeyguardStatusView.hasOwnerInfo()) {
                        KeyguardIndicationController.this.mKeyguardStatusView.setCharging(false);
                        KeyguardIndicationController.this.mChargingInfoContainer.setVisibility(8);
                    } else {
                        KeyguardIndicationController.this.mChargingInfofadeInAnimation.cancel();
                        KeyguardIndicationController.this.mChargingInfofadeOutAnimation.start();
                    }
                }
                if (KeyguardIndicationController.this.mDozing) {
                    if (!z2 && KeyguardIndicationController.this.mPowerPluggedIn) {
                        KeyguardIndicationController keyguardIndicationController3 = KeyguardIndicationController.this;
                        keyguardIndicationController3.showTransientIndication(keyguardIndicationController3.computePowerIndication());
                        KeyguardIndicationController.this.hideTransientIndicationDelayed(5000);
                    } else if (z2 && !KeyguardIndicationController.this.mPowerPluggedIn) {
                        KeyguardIndicationController.this.hideTransientIndication();
                    }
                }
                if (KeyguardIndicationController.this.mDashView == null || KeyguardIndicationController.this.mDashAnimation == null) {
                    Log.w("KeyguardIndication", "no dash view");
                } else if (!z3) {
                    KeyguardIndicationController.this.mHandler.removeMessages(R$styleable.Constraint_layout_goneMarginStart);
                    KeyguardIndicationController.this.mHandler.removeMessages(R$styleable.Constraint_layout_goneMarginTop);
                    if (KeyguardIndicationController.this.mChargingInfo.getVisibility() != 0 && !z2 && KeyguardIndicationController.this.mPowerPluggedIn) {
                        String substring3 = computePowerIndication.substring(0, computePowerIndication.indexOf("•") + 1);
                        String substring4 = computePowerIndication.substring(computePowerIndication.indexOf("•") + 1);
                        KeyguardIndicationController.this.mChargingInfoLevel.setText(substring3);
                        KeyguardIndicationController.this.updateChargingInfo(substring4);
                        KeyguardIndicationController.this.mChargingInfoContainer.setVisibility(0);
                    }
                    if (KeyguardIndicationController.this.mDashAnimation != null) {
                        KeyguardIndicationController.this.mDashAnimation.stop();
                    }
                    KeyguardIndicationController.this.mFadeOutAnimatorSet.cancel();
                    KeyguardIndicationController.this.mDashView.setVisibility(4);
                    KeyguardIndicationController.this.mDashContainer.setVisibility(8);
                } else if (KeyguardIndicationController.this.mLastChargingSpeed != KeyguardIndicationController.this.mChargingSpeed) {
                    if (OpUtils.SUPPORT_WARP_CHARGING) {
                        String substring5 = computePowerIndication.substring(0, computePowerIndication.indexOf("•") + 1);
                        String substring6 = computePowerIndication.substring(computePowerIndication.indexOf("•") + 1);
                        KeyguardIndicationController.this.mChargingInfoLevel.setText(substring5);
                        KeyguardIndicationController.this.updateChargingInfo(substring6);
                        KeyguardIndicationController.this.mChargingInfoContainer.setVisibility(0);
                        return;
                    }
                    KeyguardIndicationController.this.mDashView.setImageResource(0);
                    KeyguardIndicationController.this.mDashView.setVisibility(4);
                    KeyguardIndicationController.this.mDashContainer.setVisibility(0);
                    Message message = new Message();
                    message.what = R$styleable.Constraint_layout_goneMarginStart;
                    KeyguardIndicationController.this.mHandler.sendEmptyMessage(message.what);
                } else if (!z2 && KeyguardIndicationController.this.mPowerPluggedIn) {
                    String substring7 = computePowerIndication.substring(0, computePowerIndication.indexOf("•") + 1);
                    String substring8 = computePowerIndication.substring(computePowerIndication.indexOf("•") + 1);
                    KeyguardIndicationController.this.mChargingInfoLevel.setText(substring7);
                    KeyguardIndicationController.this.updateChargingInfo(substring8);
                    KeyguardIndicationController.this.mChargingInfoContainer.setVisibility(0);
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) {
            if (KeyguardIndicationController.this.mKeyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true)) {
                boolean z = i == -2;
                if (KeyguardIndicationController.this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    KeyguardIndicationController.this.mStatusBarKeyguardViewManager.showBouncerMessage(str, KeyguardIndicationController.this.mInitialTextColorState, 1);
                } else if (KeyguardIndicationController.this.mKeyguardUpdateMonitor.isScreenOn()) {
                    KeyguardIndicationController.this.mShowingError = true;
                    KeyguardIndicationController.this.showTransientIndication(str, false, z);
                    if (!z) {
                        KeyguardIndicationController.this.hideTransientIndicationDelayed(1300);
                    }
                }
                if (z) {
                    KeyguardIndicationController.this.mHandler.sendMessageDelayed(KeyguardIndicationController.this.mHandler.obtainMessage(3), 1300);
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) {
            if (!shouldSuppressBiometricError(i, biometricSourceType, KeyguardIndicationController.this.mKeyguardUpdateMonitor)) {
                animatePadlockError();
                if (i == 3) {
                    KeyguardIndicationController.this.showSwipeUpToUnlock();
                } else if (KeyguardIndicationController.this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    KeyguardIndicationController.this.mStatusBarKeyguardViewManager.showBouncerMessage(str, KeyguardIndicationController.this.mInitialTextColorState, 1);
                } else if (KeyguardIndicationController.this.mKeyguardUpdateMonitor.isScreenOn()) {
                    KeyguardIndicationController.this.mShowingError = true;
                    KeyguardIndicationController.this.showTransientIndication(str);
                    KeyguardIndicationController.this.hideTransientIndicationDelayed(5000);
                } else {
                    KeyguardIndicationController.this.mMessageToShowOnScreenOn = str;
                }
            }
        }

        private void animatePadlockError() {
            if (KeyguardIndicationController.this.mLockIconController != null) {
                KeyguardIndicationController.this.mLockIconController.setTransientBiometricsError(true);
            }
            KeyguardIndicationController.this.mHandler.removeMessages(2);
            KeyguardIndicationController.this.mHandler.sendMessageDelayed(KeyguardIndicationController.this.mHandler.obtainMessage(2), 1300);
        }

        private boolean shouldSuppressBiometricError(int i, BiometricSourceType biometricSourceType, KeyguardUpdateMonitor keyguardUpdateMonitor) {
            if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
                return shouldSuppressFingerprintError(i, keyguardUpdateMonitor);
            }
            if (biometricSourceType == BiometricSourceType.FACE) {
                return shouldSuppressFaceError(i, keyguardUpdateMonitor);
            }
            return false;
        }

        private boolean shouldSuppressFingerprintError(int i, KeyguardUpdateMonitor keyguardUpdateMonitor) {
            return (!keyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true) && i != 9) || i == 5;
        }

        private boolean shouldSuppressFaceError(int i, KeyguardUpdateMonitor keyguardUpdateMonitor) {
            return (!keyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true) && i != 9) || i == 5;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTrustAgentErrorMessage(CharSequence charSequence) {
            KeyguardIndicationController.this.showTransientIndication(charSequence, true, false);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOn() {
            if (KeyguardIndicationController.this.mMessageToShowOnScreenOn != null) {
                KeyguardIndicationController keyguardIndicationController = KeyguardIndicationController.this;
                keyguardIndicationController.showTransientIndication(keyguardIndicationController.mMessageToShowOnScreenOn, true, false);
                KeyguardIndicationController.this.hideTransientIndicationDelayed(5000);
                KeyguardIndicationController.this.mMessageToShowOnScreenOn = null;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) {
            if (z) {
                KeyguardIndicationController.this.hideTransientIndication();
                KeyguardIndicationController.this.mMessageToShowOnScreenOn = null;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            super.onBiometricAuthenticated(i, biometricSourceType, z);
            KeyguardIndicationController.this.mHandler.sendEmptyMessage(1);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(int i) {
            if (KeyguardIndicationController.this.mVisible) {
                KeyguardIndicationController.this.updateIndication(false);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserUnlocked() {
            if (KeyguardIndicationController.this.mVisible) {
                KeyguardIndicationController.this.updateIndication(false);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            if (OpUtils.isCustomFingerprint()) {
                boolean isUnlockWithFingerprintPossible = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser());
                KeyguardIndicationController.this.updateBottomMargins(z, isUnlockWithFingerprintPossible);
                KeyguardIndicationController.this.updatePadding(z, isUnlockWithFingerprintPossible);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean playFastWarpChargingSound() {
        boolean isStreamMute = this.mAudioManager.isStreamMute(2);
        Log.d("KeyguardIndication", "play dash anim, " + isStreamMute + ", " + this.mVisible);
        if (isStreamMute || !this.mVisible) {
            return false;
        }
        this.mChargingSound.play(this.mChargingSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        return true;
    }

    /* access modifiers changed from: private */
    public class KeyguardStateCallback extends IKeyguardStateCallback.Stub {
        private boolean mSimSecure;

        public void onDisabledStateChanged(boolean z) {
        }

        public void onFingerprintStateChange(boolean z, int i, int i2, int i3) {
        }

        public void onHasLockscreenWallpaperChanged(boolean z) {
        }

        public void onInputRestrictedStateChanged(boolean z) {
        }

        public void onPocketModeActiveChanged(boolean z) {
        }

        public void onTrustedChanged(boolean z) {
        }

        private KeyguardStateCallback() {
        }

        /* synthetic */ KeyguardStateCallback(KeyguardIndicationController keyguardIndicationController, AnonymousClass1 r2) {
            this();
        }

        public void onShowingStateChanged(boolean z) {
            Log.d("KeyguardIndication", "onShowingStateChanged " + z);
            if (z) {
                KeyguardIndicationController.this.updateDashViews();
            } else {
                KeyguardIndicationController.this.releaseDashViews();
            }
        }

        public void onSimSecureStateChanged(boolean z) {
            if (this.mSimSecure != z) {
                Log.d("KeyguardIndication", "onSimSecureStateChanged simSecure " + z);
                if (KeyguardIndicationController.this.mKeyguardBottomArea != null) {
                    KeyguardIndicationController.this.mKeyguardBottomArea.updateIndicationArea();
                }
                this.mSimSecure = z;
            }
        }
    }

    public void updateDashViews() {
        ImageView imageView = this.mDashView;
        if (imageView != null) {
            imageView.setBackground(this.mContext.getDrawable(C0006R$drawable.charging_dash_animation));
            this.mDashAnimation = (AnimationDrawable) this.mDashView.getBackground();
        }
    }

    public void releaseDashViews() {
        ImageView imageView = this.mDashView;
        if (imageView != null) {
            imageView.setBackground(null);
        }
        this.mDashAnimation = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isFastCharge() {
        int i;
        return this.mVisible && this.mPowerPluggedIn && ((i = this.mChargingSpeed) == 2 || i == 3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBottomMargins(boolean z, boolean z2) {
        if (z && this.mIndicationArea != null) {
            this.mKeyguardBottomArea.updateIndicationArea();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePadding(boolean z, boolean z2) {
        int i;
        if (z) {
            if (z2) {
                i = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.oneplus_contorl_layout_margin_left5);
            } else {
                i = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_affordance_height);
            }
            this.mTextView.setPadding(i, 0, i, 0);
        }
    }

    public boolean isShowingText() {
        KeyguardIndicationTextView keyguardIndicationTextView = this.mTextView;
        if (keyguardIndicationTextView != null && !TextUtils.isEmpty(keyguardIndicationTextView.getText()) && this.mTextView.getVisibility() == 0) {
            return true;
        }
        return false;
    }

    public void init(KeyguardViewMediator keyguardViewMediator, KeyguardStatusView keyguardStatusView, KeyguardBottomAreaView keyguardBottomAreaView) {
        Log.i("KeyguardIndication", "KeyguardIndicationController, init, keyguardViewMediator:" + keyguardViewMediator + ", keyguardStatusView:" + keyguardStatusView + ", keyguardBottomAreaView:" + keyguardBottomAreaView + ", stack:" + Debug.getCallers(5));
        setKeyguardStatusView(keyguardStatusView);
        setKeyguardBottomArea(keyguardBottomAreaView);
        this.mChargingInfofadeInAnimation = chargingInfoFadeInAnimation();
        this.mChargingInfofadeOutAnimation = chargingInfoFadeOutAnimation();
        this.mFadeOutAnimatorSet = getFadeOutAnimation();
        updateChargingInfoAndOwnerInfo();
        this.mKeyguardViewMediator = keyguardViewMediator;
        registerKeyguardStateCallbackCallbacks();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    public void setKeyguardStatusView(KeyguardStatusView keyguardStatusView) {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d("KeyguardIndication", "setKeyguardStatusView, keyguardStatusView:" + keyguardStatusView + ", stack:" + Debug.getCallers(3));
        }
        this.mKeyguardStatusView = keyguardStatusView;
        this.mDashContainer = (FrameLayout) keyguardStatusView.findViewById(C0008R$id.charging_dash_container);
        this.mDashView = (ImageView) keyguardStatusView.findViewById(C0008R$id.charging_dash);
        this.mChargingInfoContainer = (LinearLayout) keyguardStatusView.findViewById(C0008R$id.charging_info_contain);
        this.mChargingInfoLevel = (TextView) keyguardStatusView.findViewById(C0008R$id.charging_info_level);
        TextView textView = (TextView) keyguardStatusView.findViewById(C0008R$id.charging_info);
        this.mChargingInfo = textView;
        if (textView != null) {
            textView.setSelected(true);
        }
        ViewGroup viewGroup = (ViewGroup) keyguardStatusView.findViewById(C0008R$id.charging_and_owner_info_view);
        this.mInfoView = viewGroup;
        if (viewGroup != null) {
            Log.d("KeyguardIndication", "new, mInfoView.getVisibility():" + this.mInfoView.getVisibility() + ", mInfoViewID:" + this.mInfoView.getId() + ", resId:" + C0008R$id.charging_and_owner_info_view + ", mInfoView:" + this.mInfoView);
        }
        this.mOwnerInfo = keyguardStatusView.findViewById(C0008R$id.owner_info);
        updateChargingInfoAndOwnerInfo(true);
    }

    public void setKeyguardBottomArea(KeyguardBottomAreaView keyguardBottomAreaView) {
        this.mKeyguardBottomArea = keyguardBottomAreaView;
    }

    public LockscreenLockIconController getLockscreenLockIconController() {
        return this.mLockIconController;
    }

    public class CenterAlignImageSpan extends ImageSpan {
        public CenterAlignImageSpan(KeyguardIndicationController keyguardIndicationController, Drawable drawable) {
            super(drawable);
        }

        @Override // android.text.style.DynamicDrawableSpan, android.text.style.ReplacementSpan
        public void draw(Canvas canvas, CharSequence charSequence, int i, int i2, float f, int i3, int i4, int i5, Paint paint) {
            Drawable drawable = getDrawable();
            if (drawable != null) {
                Paint.FontMetricsInt fontMetricsInt = paint.getFontMetricsInt();
                canvas.save();
                canvas.translate(f, (float) (((((fontMetricsInt.descent + i4) + i4) + fontMetricsInt.ascent) / 2) - (drawable.getBounds().bottom / 2)));
                drawable.draw(canvas);
                canvas.restore();
            }
        }
    }

    private SpannableString computeProtectCharging(String str) {
        Drawable drawable = this.mContext.getResources().getDrawable(C0006R$drawable.op_protect_charging_temperature);
        drawable.setTintMode(PorterDuff.Mode.SRC_IN);
        drawable.setTint(this.mContext.getResources().getColor(C0004R$color.op_control_icon_color_inactive_dark));
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_protect_charging_temperautre_icon_height);
        drawable.setBounds(0, 0, dimensionPixelSize, dimensionPixelSize);
        String string = this.mContext.getResources().getString(C0015R$string.op_protect_charging_dot);
        String string2 = this.mContext.getResources().getString(C0015R$string.op_protect_charging);
        SpannableString spannableString = new SpannableString(str + string + string2);
        spannableString.setSpan(new CenterAlignImageSpan(this, drawable), str.length() + string.length() + -6, str.length() + string.length(), 1);
        return spannableString;
    }

    private String addChargingTimeTextSpace(String str) {
        Matcher matcher = Pattern.compile("(\\d)+").matcher(str);
        ArrayList arrayList = new ArrayList();
        StringBuffer stringBuffer = new StringBuffer(str);
        while (matcher.find()) {
            arrayList.add(new int[]{matcher.start(), matcher.end()});
        }
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            int[] iArr = (int[]) arrayList.get(size);
            if (size == 0) {
                stringBuffer.insert(iArr[1], String.format(" ", new Object[0]));
            } else {
                stringBuffer.insert(iArr[1], String.format(" ", new Object[0]));
                stringBuffer.insert(iArr[0], String.format(" ", new Object[0]));
            }
        }
        return stringBuffer.toString();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateChargingInfo(String str) {
        if (str != null && this.mChargingInfo != null) {
            SpannableString computeProtectCharging = computeProtectCharging(str);
            if (!(this.mProtectCharging ? computeProtectCharging.toString() : str).equals(this.mChargingInfo.getText().toString())) {
                TextView textView = this.mChargingInfo;
                if (this.mProtectCharging) {
                    str = computeProtectCharging;
                }
                textView.setText(str);
            } else if (OpUtils.DEBUG_ONEPLUS) {
                Log.d("KeyguardIndication", "charging info is same as last time");
            }
        }
    }
}
