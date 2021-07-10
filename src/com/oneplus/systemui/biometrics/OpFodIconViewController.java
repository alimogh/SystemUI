package com.oneplus.systemui.biometrics;

import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.oneplus.aod.OpAodDisplayViewManager;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.biometrics.OpFodHelper;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
public class OpFodIconViewController implements OpFodHelper.OnFingerprintStateChangeListener {
    private static final boolean FORCE_APPLIED_CUST_ICON = SystemProperties.getBoolean("debug.force_applied_cust_icon", false);
    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.systemui.biometrics.OpFodIconViewController.3
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            OpFodIconViewController.this.mIconNormal.setAlpha(Float.parseFloat(valueAnimator.getAnimatedValue().toString()));
        }
    };
    private OpAodDisplayViewManager mAodDisplayViewManager;
    private boolean mAppliedCustIcon = false;
    private Context mContext;
    private ContentResolver mContextResolver;
    private OpBiometricDialogImpl mDialogImpl;
    private boolean mGoingToSleep;
    private OpCircleImageView mIconDisable;
    private OpCircleImageView mIconFlash;
    private OpCircleImageView mIconNormal;
    private boolean mIsScreenTurningOn;
    private boolean mIsWakingUp;
    KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.systemui.biometrics.OpFodIconViewController.2
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedWakingUp() {
            super.onStartedWakingUp();
            OpFodIconViewController.this.mIsWakingUp = true;
            if (OpFodIconViewController.this.mIconDisable != null) {
                OpFodIconViewController.this.mIconDisable.setAlpha(0.2f);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedGoingToSleep(int i) {
            super.onStartedGoingToSleep(i);
            OpFodIconViewController.this.mGoingToSleep = true;
            OpFodIconViewController.this.mIsWakingUp = false;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFinishedGoingToSleep(int i) {
            super.onFinishedGoingToSleep(i);
            OpFodIconViewController.this.mGoingToSleep = false;
            if (OpFodIconViewController.this.mIconDisable != null) {
                OpFodIconViewController.this.mIconDisable.setAlpha(0.2f);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOn() {
            super.onScreenTurnedOn();
            OpFodIconViewController.this.mIsScreenTurningOn = false;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOff() {
            super.onScreenTurnedOff();
            OpFodIconViewController.this.mIsScreenTurningOn = false;
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onScreenTurningOn() {
            super.onScreenTurningOn();
            OpFodIconViewController.this.mIsScreenTurningOn = true;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            super.onKeyguardVisibilityChanged(z);
            OpFodIconViewController.this.mShowingKeyguard = z;
        }
    };
    private View mPanel;
    private PowerManager mPowerManager;
    private SettingsObserver mSettingsObserver;
    private boolean mShowingKeyguard;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private ValueAnimator mTimeoutAnimator;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private WakefulnessLifecycle mWakefulnessLifecycle;
    final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() { // from class: com.oneplus.systemui.biometrics.OpFodIconViewController.1
        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedWakingUp() {
            OpFodIconViewController.this.mIsWakingUp = false;
        }
    };

    public OpFodIconViewController(Context context, ViewGroup viewGroup, ViewGroup viewGroup2, OpBiometricDialogImpl opBiometricDialogImpl) {
        this.mContext = context;
        this.mIconFlash = (OpCircleImageView) viewGroup.findViewById(C0008R$id.op_fingerprint_icon_white);
        this.mIconNormal = (OpCircleImageView) viewGroup2.findViewById(C0008R$id.op_fingerprint_icon);
        this.mIconDisable = (OpCircleImageView) viewGroup2.findViewById(C0008R$id.op_fingerprint_icon_disable);
        View findViewById = viewGroup2.findViewById(C0008R$id.fp_panel);
        this.mPanel = findViewById;
        ViewGroup.LayoutParams layoutParams = findViewById.getLayoutParams();
        int convertDpToFixedPx = OpUtils.convertDpToFixedPx(this.mContext.getResources().getDimension(C0005R$dimen.op_fp_panel_size));
        layoutParams.height = convertDpToFixedPx;
        layoutParams.width = convertDpToFixedPx;
        updatePanelColor();
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        WakefulnessLifecycle wakefulnessLifecycle = (WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class);
        this.mWakefulnessLifecycle = wakefulnessLifecycle;
        wakefulnessLifecycle.addObserver(this.mWakefulnessObserver);
        this.mStatusBarKeyguardViewManager = OpLsState.getInstance().getStatusBarKeyguardViewManager();
        this.mAodDisplayViewManager = OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager();
        this.mDialogImpl = opBiometricDialogImpl;
        this.mContextResolver = this.mContext.getContentResolver();
        this.mSettingsObserver = new SettingsObserver(this.mContext.getMainThreadHandler());
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.9f, 1.0f);
        this.mTimeoutAnimator = ofFloat;
        ofFloat.setDuration(2000L);
        this.mTimeoutAnimator.addUpdateListener(this.mAnimatorUpdateListener);
        SettingsObserver settingsObserver = this.mSettingsObserver;
        if (settingsObserver != null) {
            settingsObserver.register();
        }
        OpFodHelper.getInstance().addFingerprintStateChangeListener(this);
        updateNormalIcon();
    }

    @Override // com.oneplus.systemui.biometrics.OpFodHelper.OnFingerprintStateChangeListener
    public void onFingerprintStateChanged() {
        handleUpdateIconVisibility(false);
    }

    public void onUiModeChanged() {
        updatePanelColor();
        updateNormalIcon();
    }

    private void updatePanelColor() {
        this.mPanel.setBackgroundTintList(ColorStateList.valueOf(this.mContext.getColor(ThemeColorUtils.getCurrentTheme() == 1 ? C0004R$color.op_fp_panel_color_dark : C0004R$color.op_fp_panel_color_light)));
    }

    private void updateNormalIcon() {
        ThemeColorUtils.init(this.mContext);
        boolean z = OpUtils.isREDVersion() || FORCE_APPLIED_CUST_ICON;
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d("OpFodIconViewController", "updateNormalIcon: cur=" + z + ", pre=" + this.mAppliedCustIcon);
        }
        if (this.mAppliedCustIcon != z) {
            if (z) {
                this.mIconNormal.setBackgroundDrawable(this.mContext.getResources().getDrawable(C0006R$drawable.fod_icon_custom));
            } else {
                this.mIconNormal.setBackgroundDrawable(this.mContext.getResources().getDrawable(C0006R$drawable.fod_icon_default));
            }
            this.mAppliedCustIcon = z;
        }
    }

    public void handleUpdateLayoutDimension() {
        int i;
        int i2;
        boolean is2KResolution = OpUtils.is2KResolution();
        Resources resources = this.mContext.getResources();
        if (is2KResolution) {
            i = C0005R$dimen.op_biometric_icon_normal_width_2k;
        } else {
            i = C0005R$dimen.op_biometric_icon_normal_width_1080p;
        }
        int dimension = (int) resources.getDimension(i);
        ViewGroup.LayoutParams layoutParams = this.mIconDisable.getLayoutParams();
        layoutParams.height = dimension;
        layoutParams.width = dimension;
        this.mIconDisable.setLayoutParams(layoutParams);
        this.mIconDisable.updateLayoutDimension(is2KResolution);
        ViewGroup.LayoutParams layoutParams2 = this.mIconNormal.getLayoutParams();
        layoutParams2.height = dimension;
        layoutParams2.width = dimension;
        this.mIconNormal.setLayoutParams(layoutParams2);
        this.mIconNormal.updateLayoutDimension(is2KResolution);
        Resources resources2 = this.mContext.getResources();
        if (is2KResolution) {
            i2 = C0005R$dimen.op_biometric_icon_flash_width_2k;
        } else {
            i2 = C0005R$dimen.op_biometric_icon_flash_width_1080p;
        }
        int dimension2 = (int) resources2.getDimension(i2);
        ViewGroup.LayoutParams layoutParams3 = this.mIconFlash.getLayoutParams();
        layoutParams3.height = dimension2;
        layoutParams3.width = dimension2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x03ce, code lost:
        r36.mIconNormal.setVisibility(4);
        r36.mIconDisable.setVisibility(0);
        r36.mDialogImpl.updateTransparentIconVisibility(8);
        r1 = "6";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x03e9, code lost:
        if (com.oneplus.systemui.biometrics.OpFodHelper.getInstance().isEmptyClient() == false) goto L_0x03f4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x03eb, code lost:
        r36.mIconNormal.setVisibility(4);
        r1 = "7";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x03fa, code lost:
        if (r36.mIconNormal.getVisibility() != 4) goto L_0x0436;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x0404, code lost:
        if (com.oneplus.systemui.biometrics.OpFodHelper.getInstance().isKeyguardClient() == false) goto L_0x0423;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x0406, code lost:
        r1 = r36.mShowingKeyguard;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x0408, code lost:
        if (r1 != false) goto L_0x0410;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x040a, code lost:
        if (r10 != false) goto L_0x0410;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x040c, code lost:
        if (r1 != false) goto L_0x0436;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x040e, code lost:
        if (r7 == false) goto L_0x0436;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x0410, code lost:
        r2 = false;
        r36.mIconNormal.setVisibility(0);
        r36.mIconDisable.setVisibility(4);
        r36.mDialogImpl.updateTransparentIconVisibility(0);
        r1 = "8-1";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0423, code lost:
        r2 = false;
        r36.mIconNormal.setVisibility(0);
        r36.mIconDisable.setVisibility(4);
        r36.mDialogImpl.updateTransparentIconVisibility(0);
        r1 = "8-2";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:115:0x0436, code lost:
        r2 = false;
        r1 = "0";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x038e, code lost:
        if (com.oneplus.systemui.biometrics.OpFodHelper.isAppLocker(com.oneplus.systemui.biometrics.OpFodHelper.getInstance().getCurrentOwner()) != false) goto L_0x0390;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0390, code lost:
        if (r5 == false) goto L_0x0392;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0392, code lost:
        if (r13 != false) goto L_0x043a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0394, code lost:
        if (r13 == false) goto L_0x0398;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0398, code lost:
        if (r3 == false) goto L_0x03a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x039a, code lost:
        r36.mIconNormal.setVisibility(4);
        r36.mIconDisable.setVisibility(4);
        r1 = "4";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x03ae, code lost:
        if (r36.mUpdateMonitor.isFingerprintLockout() == false) goto L_0x03bf;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x03b0, code lost:
        r36.mIconNormal.setVisibility(4);
        r2 = false;
        r36.mIconDisable.setVisibility(0);
        r1 = "5";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x03c0, code lost:
        if (r6 != false) goto L_0x03e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x03c2, code lost:
        if (r3 != false) goto L_0x03e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x03cc, code lost:
        if (com.oneplus.systemui.biometrics.OpFodHelper.getInstance().isKeyguardClient() == false) goto L_0x03e1;
     */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0477  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x0487  */
    /* JADX WARNING: Removed duplicated region for block: B:137:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleUpdateIconVisibility(boolean r37) {
        /*
        // Method dump skipped, instructions count: 1212
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.biometrics.OpFodIconViewController.handleUpdateIconVisibility(boolean):void");
    }

    public void updatePanelVisibility() {
        if (OpFodHelper.getInstance().isFromBiometricPrompt() || OpFodHelper.isSystemApp() || this.mIconNormal.getVisibility() != 0 || this.mDialogImpl.isFodHighlighted()) {
            this.mPanel.setVisibility(4);
        } else {
            this.mPanel.setVisibility(0);
        }
    }

    public void onBrightnessChange() {
        OpCircleImageView opCircleImageView = this.mIconNormal;
        if (opCircleImageView != null) {
            opCircleImageView.onBrightnessChange();
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public final Uri fpScreenTimeoutAnimation = Settings.System.getUriFor("fp_screen_time_out");

        SettingsObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            OpFodIconViewController.this.mContextResolver.registerContentObserver(this.fpScreenTimeoutAnimation, false, this);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            super.onChange(z);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            Settings.System.getInt(OpFodIconViewController.this.mContextResolver, "fp_screen_time_out", 0);
        }
    }

    public boolean isShowingFodIcon() {
        OpCircleImageView opCircleImageView = this.mIconNormal;
        if (opCircleImageView == null || opCircleImageView.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public boolean isShowingDisableIcon() {
        OpCircleImageView opCircleImageView = this.mIconDisable;
        if (opCircleImageView == null || opCircleImageView.getVisibility() != 0) {
            return false;
        }
        return true;
    }
}
