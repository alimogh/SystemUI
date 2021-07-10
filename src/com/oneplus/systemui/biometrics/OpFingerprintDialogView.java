package com.oneplus.systemui.biometrics;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Trace;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import androidx.appcompat.R$styleable;
import com.android.internal.os.SomeArgs;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0009R$integer;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.biometrics.OpFodHelper;
import com.oneplus.systemui.biometrics.OpFrameAnimationHelper;
import com.oneplus.util.OpUtils;
public class OpFingerprintDialogView extends LinearLayout implements OpFodHelper.OnFingerprintStateChangeListener {
    private static final boolean DEBUG_ONEPLUS = Build.DEBUG_ONEPLUS;
    private boolean mAnimatingAway;
    OpFrameAnimationHelper.Callbacks mCallbacks = new OpFrameAnimationHelper.Callbacks() { // from class: com.oneplus.systemui.biometrics.OpFingerprintDialogView.3
        @Override // com.oneplus.systemui.biometrics.OpFrameAnimationHelper.Callbacks
        public void animationFinished() {
            Log.d("OpFingerprintDialogView", "animationFinished");
            OpFingerprintDialogView.this.stopAnimation();
            OpFingerprintDialogView.this.doFingerPressUpInternal();
            OpFingerprintDialogView.this.mDialogImpl.collapseTransparentLayout();
            if (OpFingerprintDialogView.this.mPendingHideDialog) {
                Log.d("OpFingerprintDialogView", "Pending hide fingerprint dialog until animation end");
                OpFingerprintDialogView opFingerprintDialogView = OpFingerprintDialogView.this;
                opFingerprintDialogView.mPendingHideDialog = false;
                opFingerprintDialogView.mAnimatingAway = false;
                OpFingerprintDialogView.this.mDialogImpl.hideFodDialogInner("animation finished");
                OpFingerprintDialogView.this.updateIconVisibility(false);
            }
        }
    };
    private Context mContext;
    private final LinearLayout mDialog;
    private OpBiometricDialogImpl mDialogImpl;
    private float mDisplayWidth;
    private FingerprintManager mFm;
    private OpFodWindowManager mFodWindowManager;
    private OpFingerprintAnimationCtrl mFpAnimationCtrl;
    private Handler mHandler;
    private OpFingerprintHighlightView mHighlightView;
    private ViewGroup mLayout;
    KeyguardUpdateMonitorCallback mMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.systemui.biometrics.OpFingerprintDialogView.4
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOff() {
            super.onScreenTurnedOff();
            Log.d("OpFingerprintDialogView", "onScreenTurnedOff : fp client = " + OpFingerprintDialogView.this.getOwnerString() + ", showOnWindow = " + OpFingerprintDialogView.this.mShowOnWindow);
            OpFingerprintDialogView.this.mHandler.removeMessages(114);
            OpFingerprintDialogView.this.mHandler.sendEmptyMessage(114);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedWakingUp() {
            super.onStartedWakingUp();
            boolean isKeyguardClient = OpFodHelper.getInstance().isKeyguardClient();
            boolean isShowing = OpFingerprintDialogView.this.mStatusBarKeyguardViewManager.isShowing();
            Log.d("OpFingerprintDialogView", "onStartedWakingUp: owner= " + OpFingerprintDialogView.this.getOwnerString() + ", isKeyguardShowing= " + isShowing + ", mPendingHideDialog= " + OpFingerprintDialogView.this.mPendingHideDialog);
            OpFingerprintDialogView.this.mFpAnimationCtrl.updateAnimationDelayTime(OpFingerprintDialogView.this.mPm.isInteractive());
            if (!isKeyguardClient || !isShowing || !OpFingerprintDialogView.this.mPendingHideDialog) {
                OpFingerprintDialogView.this.mHandler.removeMessages(115);
                OpFingerprintDialogView.this.mHandler.sendEmptyMessage(115);
                return;
            }
            Log.d("OpFingerprintDialogView", "unlock success skip onStartedWakingUp");
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFinishedGoingToSleep(int i) {
            super.onFinishedGoingToSleep(i);
            Log.d("OpFingerprintDialogView", "onFinishedGoingToSleep: why= " + i);
            OpFingerprintDialogView.this.mHandler.removeMessages(R$styleable.AppCompatTheme_windowActionBar);
            Message obtainMessage = OpFingerprintDialogView.this.mHandler.obtainMessage(R$styleable.AppCompatTheme_windowActionBar);
            obtainMessage.arg1 = i;
            OpFingerprintDialogView.this.mHandler.sendMessage(obtainMessage);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOn() {
            super.onScreenTurnedOn();
            Log.d("OpFingerprintDialogView", "onScreenTurnedOn: " + OpFingerprintDialogView.this.mLayout.getAlpha());
            OpFingerprintDialogView.this.mHandler.removeMessages(R$styleable.AppCompatTheme_viewInflaterClass);
            OpFingerprintDialogView.this.mHandler.sendEmptyMessage(R$styleable.AppCompatTheme_viewInflaterClass);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            super.onKeyguardVisibilityChanged(z);
            boolean isKeyguardClient = OpFodHelper.getInstance().isKeyguardClient();
            Log.d("OpFingerprintDialogView", "onKeyguardVisibilityChanged: showing= " + z + ", isKeyguardClient= " + isKeyguardClient + ", mPendingHideDialog= " + OpFingerprintDialogView.this.mPendingHideDialog);
            if (!isKeyguardClient || !OpFingerprintDialogView.this.mPendingHideDialog || z) {
                OpFingerprintDialogView.this.mHandler.removeMessages(R$styleable.AppCompatTheme_windowActionBarOverlay);
                Message obtainMessage = OpFingerprintDialogView.this.mHandler.obtainMessage(R$styleable.AppCompatTheme_windowActionBarOverlay);
                obtainMessage.obj = Boolean.valueOf(z);
                OpFingerprintDialogView.this.mHandler.sendMessage(obtainMessage);
                return;
            }
            Log.d("OpFingerprintDialogView", "unlock success skip onKeyguardVisibilityChanged");
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardBouncerChanged(boolean z) {
            OpFingerprintDialogView.this.mHandler.removeMessages(129);
            Message obtainMessage = OpFingerprintDialogView.this.mHandler.obtainMessage(129);
            obtainMessage.obj = Boolean.valueOf(z);
            OpFingerprintDialogView.this.mHandler.sendMessage(obtainMessage);
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onFacelockStateChanged(int i) {
            boolean isScreenOffUnlock = OpLsState.getInstance().getPhoneStatusBar().getFacelockController().isScreenOffUnlock();
            Log.d("OpFingerprintDialogView", "onFacelockStateChanged: type:" + i + ", isOffUnlock:" + isScreenOffUnlock);
            if (i == 4) {
                OpFingerprintDialogView.this.mHandler.removeMessages(R$styleable.AppCompatTheme_windowFixedHeightMajor);
                Message obtainMessage = OpFingerprintDialogView.this.mHandler.obtainMessage(R$styleable.AppCompatTheme_windowFixedHeightMajor);
                obtainMessage.arg1 = i;
                OpFingerprintDialogView.this.mHandler.sendMessage(obtainMessage);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStrongAuthStateChanged(int i) {
            super.onStrongAuthStateChanged(i);
            OpFingerprintDialogView.this.mHandler.removeMessages(R$styleable.AppCompatTheme_windowFixedHeightMinor);
            Message obtainMessage = OpFingerprintDialogView.this.mHandler.obtainMessage(R$styleable.AppCompatTheme_windowFixedHeightMinor);
            obtainMessage.arg1 = i;
            OpFingerprintDialogView.this.mHandler.sendMessage(obtainMessage);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onSimStateChanged(int i, int i2, int i3) {
            OpFingerprintDialogView.this.mHandler.removeMessages(R$styleable.AppCompatTheme_windowFixedWidthMajor);
            Message obtainMessage = OpFingerprintDialogView.this.mHandler.obtainMessage(R$styleable.AppCompatTheme_windowFixedWidthMajor);
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = Integer.valueOf(i);
            obtain.arg2 = Integer.valueOf(i2);
            obtain.arg3 = Integer.valueOf(i3);
            obtainMessage.obj = obtain;
            OpFingerprintDialogView.this.mHandler.sendMessage(obtainMessage);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitching(int i) {
            super.onUserSwitching(i);
            if (OpFingerprintDialogView.DEBUG_ONEPLUS) {
                Log.d("OpFingerprintDialogView", "onUserSwitching " + i);
            }
            OpFingerprintDialogView.this.mHandler.removeMessages(R$styleable.AppCompatTheme_windowFixedWidthMinor);
            Message obtainMessage = OpFingerprintDialogView.this.mHandler.obtainMessage(R$styleable.AppCompatTheme_windowFixedWidthMinor);
            obtainMessage.arg1 = i;
            OpFingerprintDialogView.this.mHandler.sendMessage(obtainMessage);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(int i) {
            if (OpFingerprintDialogView.DEBUG_ONEPLUS) {
                Log.d("OpFingerprintDialogView", "onUserSwitchComplete " + i);
            }
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onKeyguardDoneChanged(boolean z) {
            Log.d("OpFingerprintDialogView", "onKeyguardDoneChanged: isKeyguardDone = " + z + ", mPendingHideDialog= " + OpFingerprintDialogView.this.mPendingHideDialog);
            if (!z || !OpFingerprintDialogView.this.mPendingHideDialog) {
                OpFingerprintDialogView.this.mHandler.removeMessages(androidx.constraintlayout.widget.R$styleable.Constraint_visibilityMode);
                OpFingerprintDialogView.this.mHandler.obtainMessage(androidx.constraintlayout.widget.R$styleable.Constraint_visibilityMode, Boolean.valueOf(z)).sendToTarget();
                return;
            }
            Log.d("OpFingerprintDialogView", "unlock success skip onKeyguardDoneChanged");
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onQSExpendChanged(boolean z) {
            if (OpFingerprintDialogView.this.isDialogShowing()) {
                boolean isUnlockingWithBiometricAllowed = OpFingerprintDialogView.this.mUpdateMonitor.isUnlockingWithBiometricAllowed();
                KeyguardUpdateMonitor keyguardUpdateMonitor = OpFingerprintDialogView.this.mUpdateMonitor;
                KeyguardUpdateMonitor unused = OpFingerprintDialogView.this.mUpdateMonitor;
                boolean isUnlockWithFingerprintPossible = keyguardUpdateMonitor.isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser());
                Log.d("OpFingerprintDialogView", "onQSExpendChanged: expend= " + z + ", isUnlockingWithBiometricAllowed= " + isUnlockingWithBiometricAllowed + ", isUnlockWithFingerprintPossible= " + isUnlockWithFingerprintPossible);
                if (!OpFodHelper.getInstance().isEmptyClient() && isUnlockWithFingerprintPossible) {
                    OpFingerprintDialogView.this.updateIconVisibility(false);
                }
            }
            OpFodHelper.getInstance().handleQSExpandChanged(z);
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onShuttingDown() {
            if (OpFingerprintDialogView.this.isDialogShowing()) {
                Log.d("OpFingerprintDialogView", "onShuttingDown");
                if (!OpFodHelper.getInstance().isEmptyClient()) {
                    OpFingerprintDialogView.this.updateIconVisibility(true);
                }
            }
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onShutdownDialogVisibilityChanged(boolean z) {
            if (OpFingerprintDialogView.this.isDialogShowing()) {
                Log.d("OpFingerprintDialogView", "onShutdownDialogVisibilityChanged: show= " + z);
                if (!OpFodHelper.getInstance().isEmptyClient()) {
                    OpFingerprintDialogView.this.updateIconVisibility(false);
                }
            }
            OpFodHelper.getInstance().handleShutdownDialogVisibilityChanged(z);
        }
    };
    private OpFodDisplayController mOpFodDisplayController;
    private OpFodIconViewController mOpFodIconViewController;
    boolean mPendingHideDialog = false;
    private PowerManager mPm;
    private Runnable mPressTimeoutRunnable = new Runnable() { // from class: com.oneplus.systemui.biometrics.OpFingerprintDialogView.2
        @Override // java.lang.Runnable
        public void run() {
            boolean isFingerDown = OpFingerprintDialogView.this.mDialogImpl.isFingerDown();
            Log.d("OpFingerprintDialogView", "Press Timeout: pressed = " + isFingerDown);
            if (isFingerDown) {
                OpFingerprintDialogView.this.setDisplayPressModeFingerUp();
                OpFingerprintDialogView.this.playAnimation(2);
                OpLsState.getInstance().getPhoneStatusBar().onFpPressedTimeOut();
            }
        }
    };
    private boolean mShowOnWindow;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private final WindowManager mWindowManager;

    public void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) { // from class: com.oneplus.systemui.biometrics.OpFingerprintDialogView.1
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                if (i != 129) {
                    switch (i) {
                        case androidx.constraintlayout.widget.R$styleable.Constraint_visibilityMode /* 110 */:
                            OpFingerprintDialogView.this.handleNotifyKeyguardDone(((Boolean) message.obj).booleanValue());
                            return;
                        case 111:
                            OpFingerprintDialogView.this.handleNotifyBrightnessChange();
                            return;
                        case 112:
                            OpFingerprintDialogView.this.mOpFodIconViewController.handleUpdateIconVisibility(((Boolean) message.obj).booleanValue());
                            return;
                        case 113:
                            SomeArgs someArgs = (SomeArgs) message.obj;
                            OpFingerprintDialogView.this.handleUpdateLayoutDimension(((Boolean) someArgs.arg1).booleanValue(), ((Float) someArgs.arg2).floatValue());
                            return;
                        case 114:
                            OpFingerprintDialogView.this.handleOnScreenTurnedOff();
                            return;
                        case 115:
                            OpFingerprintDialogView.this.handleOnStartedWakingUp();
                            return;
                        case R$styleable.AppCompatTheme_viewInflaterClass /* 116 */:
                            OpFingerprintDialogView.this.handleOnScreenTurnedOn();
                            return;
                        case R$styleable.AppCompatTheme_windowActionBar /* 117 */:
                            OpFingerprintDialogView.this.handleOnFinishedGoingToSleep(message.arg1);
                            return;
                        case R$styleable.AppCompatTheme_windowActionBarOverlay /* 118 */:
                            OpFingerprintDialogView.this.handleOnKeyguardVisibilityChanged(((Boolean) message.obj).booleanValue());
                            return;
                        default:
                            switch (i) {
                                case R$styleable.AppCompatTheme_windowFixedHeightMajor /* 120 */:
                                    OpFingerprintDialogView.this.handleOnFacelockStateChanged(message.arg1);
                                    return;
                                case R$styleable.AppCompatTheme_windowFixedHeightMinor /* 121 */:
                                    OpFingerprintDialogView.this.handleOnStrongAuthStateChanged(message.arg1);
                                    return;
                                case R$styleable.AppCompatTheme_windowFixedWidthMajor /* 122 */:
                                    SomeArgs someArgs2 = (SomeArgs) message.obj;
                                    OpFingerprintDialogView.this.handleOnSimStateChanged(((Integer) someArgs2.arg1).intValue(), ((Integer) someArgs2.arg2).intValue(), ((Integer) someArgs2.arg3).intValue());
                                    return;
                                case R$styleable.AppCompatTheme_windowFixedWidthMinor /* 123 */:
                                    OpFingerprintDialogView.this.handleOnUserSwitching(message.arg1);
                                    return;
                                default:
                                    return;
                            }
                    }
                } else {
                    OpFingerprintDialogView.this.handleOnKeyguardBouncerChanged(((Boolean) message.obj).booleanValue());
                }
            }
        };
    }

    public OpFingerprintDialogView(Context context, OpBiometricDialogImpl opBiometricDialogImpl) {
        super(context);
        this.mDialogImpl = opBiometricDialogImpl;
        initHandler(opBiometricDialogImpl.getMainLooper());
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        this.mDisplayWidth = (float) displayMetrics.widthPixels;
        LayoutInflater from = LayoutInflater.from(getContext());
        this.mContext = context;
        this.mPm = (PowerManager) context.getSystemService("power");
        this.mFm = (FingerprintManager) this.mContext.getSystemService("fingerprint");
        this.mLayout = (ViewGroup) from.inflate(C0011R$layout.op_fingerprint_view, (ViewGroup) this, false);
        OpFingerprintHighlightView opFingerprintHighlightView = (OpFingerprintHighlightView) from.inflate(C0011R$layout.op_fingerprint_high_light_view, (ViewGroup) null, false);
        this.mHighlightView = opFingerprintHighlightView;
        opFingerprintHighlightView.setDialog(opBiometricDialogImpl);
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mOpFodDisplayController = new OpFodDisplayController(context);
        this.mFpAnimationCtrl = new OpFingerprintAnimationCtrl(this.mLayout, context);
        this.mOpFodIconViewController = new OpFodIconViewController(context, this.mHighlightView, this.mLayout, opBiometricDialogImpl);
        this.mUpdateMonitor.registerCallback(this.mMonitorCallback);
        OpFodHelper.getInstance().setFodIconViewController(this.mOpFodIconViewController);
        OpFodHelper.getInstance().addFingerprintStateChangeListener(this);
        OpLsState.getInstance().setFpAnimationCtrl(this.mFpAnimationCtrl);
        this.mStatusBarKeyguardViewManager = OpLsState.getInstance().getStatusBarKeyguardViewManager();
        addView(this.mLayout);
        this.mDialog = (LinearLayout) this.mLayout.findViewById(C0008R$id.dialog);
        this.mLayout.setFocusableInTouchMode(true);
        this.mLayout.requestFocus();
        KeyguardUpdateMonitor keyguardUpdateMonitor = this.mUpdateMonitor;
        if (keyguardUpdateMonitor != null) {
            keyguardUpdateMonitor.setFodDialogView(this);
        }
        initFodAnimationStyle();
    }

    public void addToWindow() {
        if (this.mShowOnWindow) {
            Log.w("OpFingerprintDialogView", "addToWindow: already added.");
        } else {
            Log.d("OpFingerprintDialogView", "addToWindow: addFpViewToWindow");
            this.mFodWindowManager.addFpViewToWindow();
        }
        this.mHighlightView.addToWindow();
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mDialog.getLayoutParams().width = (int) this.mDisplayWidth;
        this.mLayout.setAlpha(1.0f);
        this.mPendingHideDialog = false;
        boolean isDialogShowingRequest = this.mDialogImpl.isDialogShowingRequest();
        Log.d("OpFingerprintDialogView", "onAttachedToWindow: isKeyguardDone= " + this.mUpdateMonitor.isKeyguardDone() + ", isDialogShowingRequest= " + isDialogShowingRequest);
        this.mShowOnWindow = true;
        if (!isDialogShowingRequest) {
            Log.d("OpFingerprintDialogView", "onAttachedToWindow: remove window now.");
            this.mWindowManager.removeViewImmediate(this);
            return;
        }
        this.mOpFodIconViewController.handleUpdateIconVisibility(false);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        boolean isDialogShowingRequest = this.mDialogImpl.isDialogShowingRequest();
        boolean isFingerprintDetecting = OpFodHelper.getInstance().isFingerprintDetecting();
        Log.d("OpFingerprintDialogView", "onDetachedFromWindow: isDialogShowingRequest=" + isDialogShowingRequest + ", isFpDetecting= " + isFingerprintDetecting);
        resetState();
        this.mPendingHideDialog = false;
        super.onDetachedFromWindow();
        this.mShowOnWindow = false;
        if (isFingerprintDetecting || isDialogShowingRequest) {
            Log.d("OpFingerprintDialogView", "Because fingerprint is still on detecting state, add the window again");
            addToWindow();
        }
    }

    public void startDismiss(boolean z) {
        boolean isDialogShowingRequest = this.mDialogImpl.isDialogShowingRequest();
        Log.d("OpFingerprintDialogView", "startDismiss: starting..., authenticated: " + z + ", mShowOnWindow: " + this.mShowOnWindow + ", isDialogShowingRequest: " + isDialogShowingRequest + ", callers: " + Debug.getCallers(2));
        Trace.traceBegin(8, "OpFingerprintDialogView.hide");
        this.mHighlightView.removeFromWindow();
        this.mOpFodDisplayController.dismiss();
        if (this.mShowOnWindow) {
            this.mWindowManager.removeViewImmediate(this);
        } else {
            Log.d("OpFingerprintDialogView", "startDismiss: window is not added yet, remove it after attached");
        }
        this.mDialogImpl.onFingerprintDialogDismissDone();
        Trace.traceEnd(8);
    }

    public void forceRemove() {
        Log.d("OpFingerprintDialogView", "forceRemove");
        this.mLayout.animate().cancel();
        this.mDialog.animate().cancel();
        if (isAttachedToWindow()) {
            this.mWindowManager.removeViewImmediate(this);
        }
        this.mHighlightView.removeFromWindow();
        this.mAnimatingAway = false;
    }

    public boolean isAnimatingAway() {
        return this.mAnimatingAway;
    }

    private void updateAnimationResource() {
        if (!OpFodHelper.getInstance().isEmptyClient()) {
            this.mFpAnimationCtrl.updateAnimationRes(this.mPm.isInteractive());
        }
    }

    @Override // com.oneplus.systemui.biometrics.OpFodHelper.OnFingerprintStateChangeListener
    public void onFingerprintStateChanged() {
        if (OpFodHelper.getInstance().isFingerprintDetecting()) {
            updateAnimationResource();
        }
    }

    public void postPressTimeOutRunnable() {
        if (DEBUG_ONEPLUS) {
            Log.d("OpFingerprintDialogView", "post press timeout message");
        }
        removePressTimeOutMessage();
        this.mHandler.postDelayed(this.mPressTimeoutRunnable, 1000);
    }

    public void removePressTimeOutMessage() {
        if (this.mHandler.hasCallbacks(this.mPressTimeoutRunnable)) {
            if (DEBUG_ONEPLUS) {
                Log.d("OpFingerprintDialogView", "remove press timeout message");
            }
            this.mHandler.removeCallbacks(this.mPressTimeoutRunnable);
        }
    }

    public void doFingerprintPressDown(int i) {
        if (i == 0) {
            postPressTimeOutRunnable();
        }
        if (!this.mDialogImpl.isFingerDown()) {
            Log.d("OpFingerprintDialogView", "doFingerprintPressDown: press state the same");
        } else if (!this.mShowOnWindow) {
            Log.d("OpFingerprintDialogView", "doFingerprintPressDown: fp window not exist don't show pressed button");
        } else {
            Log.d("OpFingerprintDialogView", "doFingerprintPressDown: owner:" + getOwnerString() + ", done:" + this.mUpdateMonitor.isKeyguardDone() + ", callers: " + Debug.getCallers(1));
            OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager().onFingerPressed(true);
            this.mDialogImpl.expandTransparentLayout();
            playAnimation(1);
            setDisplayPressModeFingerDown();
        }
    }

    public void doFingerprintPressUp() {
        if (DEBUG_ONEPLUS) {
            Log.d("OpFingerprintDialogView", "doFingerprintPressUp owner:" + getOwnerString() + ", done:" + this.mUpdateMonitor.isKeyguardDone() + ", pending hide dialog: " + this.mPendingHideDialog + ", callers: " + Debug.getCallers(1));
        }
        if (!this.mPendingHideDialog) {
            stopAnimation();
        }
        doFingerPressUpInternal();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doFingerPressUpInternal() {
        removePressTimeOutMessage();
        setDisplayPressModeFingerUp();
        OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager().onFingerPressed(false);
    }

    public void onFpEventCancel() {
        boolean isFingerDown = this.mDialogImpl.isFingerDown();
        Log.d("OpFingerprintDialogView", "onFpEventCancel: pressed= " + isFingerDown + ", keyguardDone = " + this.mUpdateMonitor.isKeyguardDone() + ", callers= " + Debug.getCallers(1));
        if (!this.mShowOnWindow) {
            Log.d("OpFingerprintDialogView", "onFpEventCancel: fp window has been removed.");
            return;
        }
        removePressTimeOutMessage();
        if (!isFingerDown) {
            return;
        }
        if (this.mFpAnimationCtrl.isPlayingAnimation()) {
            this.mFpAnimationCtrl.waitAnimationFinished(this.mCallbacks);
        } else {
            this.mCallbacks.animationFinished();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playAnimation(int i) {
        if (this.mShowOnWindow) {
            Log.d("OpFingerprintDialogView", "playAnimation: dialog showing");
        }
        if (this.mFm.isEnrolling()) {
            Log.d("OpFingerprintDialogView", "playAnimation: in enrolling");
        } else if (this.mUpdateMonitor.isKeyguardDone() && this.mUpdateMonitor.isDeviceInteractive() && OpFodHelper.isSystemUI(getOwnerString())) {
            Log.d("OpFingerprintDialogView", "playAnimation: keyguard done");
        } else if (this.mUpdateMonitor.isGoingToSleep() || (!this.mUpdateMonitor.isScreenOn() && !this.mUpdateMonitor.isScreenTurningOn())) {
            Log.d("OpFingerprintDialogView", "playAnimation: don't play animation due to going to sleep or screen off");
        } else {
            OpFingerprintAnimationCtrl opFingerprintAnimationCtrl = this.mFpAnimationCtrl;
            if (opFingerprintAnimationCtrl != null) {
                opFingerprintAnimationCtrl.playAnimation(i);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopAnimation() {
        OpFingerprintAnimationCtrl opFingerprintAnimationCtrl = this.mFpAnimationCtrl;
        if (opFingerprintAnimationCtrl != null) {
            opFingerprintAnimationCtrl.stopAnimation(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnScreenTurnedOff() {
        if (DEBUG_ONEPLUS) {
            Log.d("OpFingerprintDialogView", "handleOnScreenTurnedOff");
        }
        resetState();
        shouldForceShowFod("screen turned off");
    }

    private boolean shouldForceShowFod(String str) {
        boolean z = this.mShowOnWindow;
        boolean isFingerprintEnrolled = this.mUpdateMonitor.isFingerprintEnrolled(KeyguardUpdateMonitor.getCurrentUser());
        boolean z2 = !this.mUpdateMonitor.isUnlockingWithBiometricAllowed() || OpFodHelper.getInstance().isFingerprintLockout();
        boolean isShowing = this.mStatusBarKeyguardViewManager.isShowing();
        boolean isKeyguardDone = this.mUpdateMonitor.isKeyguardDone();
        boolean isDeviceInteractive = this.mUpdateMonitor.isDeviceInteractive();
        boolean isDreaming = this.mUpdateMonitor.isDreaming();
        Log.d("OpFingerprintDialogView", "shouldForceShowFod: reason= " + str + ", isWindowAttached= " + z + ", isFpEnrolled= " + isFingerprintEnrolled + ", isKeyguardShowing= " + isShowing + ", isKeyguardDone= " + isKeyguardDone + ", isDeviceInteractive= " + isDeviceInteractive + ", isDreaming= " + isDreaming + ", isLockout= " + z2);
        if (!OpUtils.isCustomFingerprint() || !isFingerprintEnrolled || ((!isShowing && (isDeviceInteractive || !isDreaming)) || isKeyguardDone)) {
            return false;
        }
        if (OpFodHelper.getInstance().isEmptyClient()) {
            Log.d("OpFingerprintDialogView", "shouldForceShowFod: add to window.");
            Bundle bundle = new Bundle();
            bundle.putString("key_fingerprint_package_name", "forceShow-keyguard");
            this.mDialogImpl.forceShowFodDialog(bundle);
        } else {
            Log.d("OpFingerprintDialogView", "shouldForceShowFod: already added to window, update ui.");
            this.mOpFodIconViewController.handleUpdateIconVisibility(false);
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnStartedWakingUp() {
        boolean isKeyguardClient = OpFodHelper.getInstance().isKeyguardClient();
        boolean isShowing = this.mStatusBarKeyguardViewManager.isShowing();
        if (DEBUG_ONEPLUS) {
            Log.d("OpFingerprintDialogView", "handleOnStartedWakingUp : ownerString = " + getOwnerString() + ", isKeyguardShowing = " + isShowing);
        }
        if (!isKeyguardClient || isShowing) {
            this.mOpFodIconViewController.handleUpdateIconVisibility(false);
        } else if (this.mDialogImpl != null) {
            this.mOpFodIconViewController.handleUpdateIconVisibility(true);
            this.mDialogImpl.hideFodDialogInner("start waking up");
        }
        updateAnimationResource();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnScreenTurnedOn() {
        if (DEBUG_ONEPLUS) {
            Log.d("OpFingerprintDialogView", "handleOnScreenTurnedOn");
        }
        if (!shouldForceShowFod("screen turned on") && !OpFodHelper.getInstance().isEmptyClient() && !OpFodHelper.getInstance().isKeyguardClient()) {
            Log.d("OpFingerprintDialogView", "update icon visibility while turned on.");
            updateIconVisibility(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnFinishedGoingToSleep(int i) {
        if (DEBUG_ONEPLUS) {
            Log.d("OpFingerprintDialogView", "handleOnFinishedGoingToSleep: why= " + i);
        }
        shouldForceShowFod("finish going to sleep");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnKeyguardVisibilityChanged(boolean z) {
        if (DEBUG_ONEPLUS) {
            Log.d("OpFingerprintDialogView", "handleOnKeyguardVisibilityChanged");
        }
        if (z) {
            shouldForceShowFod("keyguard visibility changed: show");
        } else {
            this.mOpFodIconViewController.handleUpdateIconVisibility(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnKeyguardBouncerChanged(boolean z) {
        if (this.mStatusBarKeyguardViewManager.isShowing()) {
            this.mOpFodIconViewController.handleUpdateIconVisibility(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnFacelockStateChanged(int i) {
        if (DEBUG_ONEPLUS) {
            Log.d("OpFingerprintDialogView", "handleOnFacelockStateChanged : type = " + i);
        }
        this.mDialogImpl.onFaceUnlocked();
        this.mOpFodIconViewController.handleUpdateIconVisibility(true);
        onFpEventCancel();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnStrongAuthStateChanged(int i) {
        KeyguardUpdateMonitor keyguardUpdateMonitor = this.mUpdateMonitor;
        boolean isUnlockingWithBiometricAllowed = keyguardUpdateMonitor != null ? keyguardUpdateMonitor.isUnlockingWithBiometricAllowed() : true;
        Log.d("OpFingerprintDialogView", "onStrongAuthStateChanged, " + isUnlockingWithBiometricAllowed);
        if (!isUnlockingWithBiometricAllowed) {
            this.mOpFodIconViewController.handleUpdateIconVisibility(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnSimStateChanged(int i, int i2, int i3) {
        KeyguardUpdateMonitor keyguardUpdateMonitor = this.mUpdateMonitor;
        if (keyguardUpdateMonitor != null && keyguardUpdateMonitor.isSimPinSecure()) {
            this.mOpFodIconViewController.handleUpdateIconVisibility(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnUserSwitching(int i) {
        OpFingerprintAnimationCtrl opFingerprintAnimationCtrl = this.mFpAnimationCtrl;
        if (opFingerprintAnimationCtrl != null) {
            opFingerprintAnimationCtrl.resetState();
            this.mFpAnimationCtrl.checkAnimationValueValid();
            this.mFpAnimationCtrl.updateAnimationRes(this.mPm.isInteractive());
        }
        initFodAnimationStyle();
    }

    private void resetState() {
        this.mOpFodDisplayController.resetState();
        Log.i("OpFingerprintDialogView", "resetState: mPendingHideDialog " + this.mPendingHideDialog);
        this.mPendingHideDialog = false;
        if (OpFodHelper.getInstance().isEmptyClient()) {
            this.mFpAnimationCtrl.resetState();
        } else {
            Log.i("OpFingerprintDialogView", "resetState: fp client to " + getOwnerString() + ", reuse animation");
        }
        this.mOpFodIconViewController.handleUpdateIconVisibility(false);
        stopAnimation();
    }

    public void updateIconVisibility(boolean z) {
        if (z) {
            Log.d("OpFingerprintDialogView", "updateIconVisibility: forceHide trace = " + Debug.getCallers(5));
        }
        this.mHandler.removeMessages(112);
        Message obtainMessage = this.mHandler.obtainMessage(112);
        obtainMessage.obj = Boolean.valueOf(z);
        this.mHandler.sendMessage(obtainMessage);
    }

    public void hideFODDim() {
        this.mOpFodDisplayController.hideFODDim();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyKeyguardDone(boolean z) {
        Log.d("OpFingerprintDialogView", "handleNotifyKeyguardDone, " + z);
        if (z) {
            this.mOpFodIconViewController.handleUpdateIconVisibility(true);
            onFpEventCancel();
            this.mDialogImpl.hideFodDialogInner("keyguard done");
        }
    }

    private boolean needsToWaitUntilAnimationDone() {
        boolean z = !this.mUpdateMonitor.isKeyguardDone() && OpUtils.isHomeApp() && OpUtils.isSupportRefreshRateSwitch() && (this.mUpdateMonitor.isKeyguardVisible() || !this.mPm.isInteractive());
        this.mPendingHideDialog = z;
        this.mAnimatingAway = z;
        return z;
    }

    public void notifyFingerprintAuthenticated() {
        boolean isHomeApp = OpUtils.isHomeApp();
        boolean isKeyguardVisible = this.mUpdateMonitor.isKeyguardVisible();
        boolean needsToWaitUntilAnimationDone = needsToWaitUntilAnimationDone();
        Log.d("OpFingerprintDialogView", "notifyFingerprintAuthenticated isInteractive, " + this.mPm.isInteractive() + " isKeyguardVisible:" + isKeyguardVisible + " isFingerDown:" + this.mDialogImpl.isFingerDown() + " isHomeApp:" + isHomeApp + " needsToWaitUntilAnimationDone: " + needsToWaitUntilAnimationDone);
        doFingerprintPressUp();
        this.mOpFodDisplayController.notifyFingerprintAuthenticated();
        this.mOpFodIconViewController.handleUpdateIconVisibility(true);
        if (needsToWaitUntilAnimationDone) {
            OpFingerprintAnimationCtrl opFingerprintAnimationCtrl = this.mFpAnimationCtrl;
            if (opFingerprintAnimationCtrl == null) {
                return;
            }
            if (!opFingerprintAnimationCtrl.isPlayingAnimation()) {
                this.mCallbacks.animationFinished();
            } else {
                this.mFpAnimationCtrl.waitAnimationFinished(this.mCallbacks);
            }
        } else {
            this.mDialogImpl.collapseTransparentLayout();
            this.mDialogImpl.hideFodDialogInner("fp authenticated");
        }
    }

    public boolean isPendingHideDialog() {
        return this.mPendingHideDialog;
    }

    private void setDisplayPressModeFingerDown() {
        this.mOpFodDisplayController.onFingerPressDown();
        this.mOpFodIconViewController.updatePanelVisibility();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDisplayPressModeFingerUp() {
        this.mOpFodDisplayController.onFingerPressUp();
        this.mOpFodIconViewController.updatePanelVisibility();
    }

    public String getOwnerString() {
        return OpFodHelper.getInstance().getCurrentOwner();
    }

    public void notifyBrightnessChange() {
        this.mHandler.removeMessages(111);
        this.mHandler.sendEmptyMessage(111);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyBrightnessChange() {
        if (this.mShowOnWindow) {
            this.mOpFodIconViewController.onBrightnessChange();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (DEBUG_ONEPLUS) {
            Log.i("OpFingerprintDialogView", "onConfigurationChanged");
        }
        this.mFodWindowManager.handleConfigurationChange();
    }

    public void updateLayoutDimension(boolean z, float f) {
        this.mHandler.removeMessages(113);
        Message obtainMessage = this.mHandler.obtainMessage(113);
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = Boolean.valueOf(z);
        obtain.arg2 = Float.valueOf(f);
        obtainMessage.obj = obtain;
        this.mHandler.sendMessage(obtainMessage);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateLayoutDimension(boolean z, float f) {
        if (OpUtils.isSupportResolutionSwitch(this.mContext) || OpUtils.isSupportCutout()) {
            this.mDisplayWidth = f;
            this.mOpFodIconViewController.handleUpdateLayoutDimension();
            this.mFpAnimationCtrl.updateLayoutDimension(z);
            this.mFodWindowManager.handleConfigurationChange();
        }
    }

    public boolean isDialogShowing() {
        return this.mShowOnWindow;
    }

    public void setFodWindowManager(OpFodWindowManager opFodWindowManager) {
        this.mFodWindowManager = opFodWindowManager;
    }

    private void initFodAnimationStyle() {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        ContentResolver contentResolver = this.mContext.getContentResolver();
        int intForUser = Settings.System.getIntForUser(contentResolver, "op_custom_unlock_animation_style", -1, 0);
        Log.i("OpFingerprintDialogView", "animValue = " + intForUser + ", user = " + currentUser);
        if (intForUser == -1) {
            Settings.System.putIntForUser(contentResolver, "op_custom_unlock_animation_style", this.mContext.getResources().getInteger(C0009R$integer.fingerprint_default_animation_style), currentUser);
        } else if (currentUser != 0) {
            Settings.System.putIntForUser(contentResolver, "op_custom_unlock_animation_style", intForUser, currentUser);
        }
    }

    public void onUiModeChanged() {
        this.mOpFodIconViewController.onUiModeChanged();
    }

    public boolean isFodHighlighted() {
        return this.mOpFodDisplayController.isFodHighlighted();
    }

    public static class OpFingerprintHighlightView extends AddRemoveRequestingView {
        public OpFingerprintHighlightView(Context context) {
            this(context, null);
        }

        public OpFingerprintHighlightView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }
    }

    public void setDimForFingerprintAcquired() {
        this.mOpFodDisplayController.setDimForFingerprintAcquired();
    }
}
