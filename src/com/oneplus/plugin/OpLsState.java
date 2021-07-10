package com.oneplus.plugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.oneplus.plugin.OpBaseCtrl;
import com.oneplus.systemui.biometrics.OpFingerprintAnimationCtrl;
public class OpLsState implements OpBaseCtrl.ControlCallback {
    private static OpLsState sInstance;
    private BiometricUnlockController mBiometricUnlockController;
    private ViewGroup mContainer;
    private Context mContext;
    public final OpBaseCtrl[] mControls;
    private OpFingerprintAnimationCtrl mFingerprintAnimationCtrl;
    private boolean mInit = false;
    private boolean mIsFinishedScreenTuredOn;
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    private Looper mNonUiLooper;
    private StatusBar mPhonstatusBar;
    private final OpPreventModeCtrl mPreventModeCtrl;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private KeyguardUpdateMonitor mUpdateMonitor;

    public OpPreventModeCtrl getPreventModeCtrl() {
        return this.mPreventModeCtrl;
    }

    public static synchronized OpLsState getInstance() {
        OpLsState opLsState;
        synchronized (OpLsState.class) {
            if (sInstance == null) {
                sInstance = new OpLsState();
            }
            opLsState = sInstance;
        }
        return opLsState;
    }

    OpLsState() {
        new MyUIHandler();
        this.mIsFinishedScreenTuredOn = false;
        OpPreventModeCtrl opPreventModeCtrl = new OpPreventModeCtrl();
        this.mPreventModeCtrl = opPreventModeCtrl;
        this.mControls = new OpBaseCtrl[]{opPreventModeCtrl};
        this.mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.plugin.OpLsState.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedWakingUp() {
                OpLsState.this.mIsFinishedScreenTuredOn = true;
                OpBaseCtrl[] opBaseCtrlArr = OpLsState.this.mControls;
                for (OpBaseCtrl opBaseCtrl : opBaseCtrlArr) {
                    if (opBaseCtrl != null && opBaseCtrl.isEnable()) {
                        opBaseCtrl.onStartedWakingUp();
                    }
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onStartedGoingToSleep(int i) {
                OpLsState.this.mIsFinishedScreenTuredOn = false;
                OpBaseCtrl[] opBaseCtrlArr = OpLsState.this.mControls;
                for (OpBaseCtrl opBaseCtrl : opBaseCtrlArr) {
                    if (opBaseCtrl != null && opBaseCtrl.isEnable()) {
                        opBaseCtrl.onStartedGoingToSleep(i);
                    }
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onFinishedGoingToSleep(int i) {
                OpBaseCtrl[] opBaseCtrlArr = OpLsState.this.mControls;
                for (OpBaseCtrl opBaseCtrl : opBaseCtrlArr) {
                    if (opBaseCtrl != null && opBaseCtrl.isEnable()) {
                        opBaseCtrl.onFinishedGoingToSleep(i);
                    }
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onScreenTurnedOff() {
                OpLsState.this.mIsFinishedScreenTuredOn = false;
                OpBaseCtrl[] opBaseCtrlArr = OpLsState.this.mControls;
                for (OpBaseCtrl opBaseCtrl : opBaseCtrlArr) {
                    if (opBaseCtrl != null && opBaseCtrl.isEnable()) {
                        opBaseCtrl.onScreenTurnedOff();
                    }
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardBouncerChanged(boolean z) {
                OpBaseCtrl[] opBaseCtrlArr = OpLsState.this.mControls;
                for (OpBaseCtrl opBaseCtrl : opBaseCtrlArr) {
                    if (opBaseCtrl != null && opBaseCtrl.isEnable()) {
                        opBaseCtrl.onKeyguardBouncerChanged(z);
                    }
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardVisibilityChanged(boolean z) {
                OpBaseCtrl[] opBaseCtrlArr = OpLsState.this.mControls;
                for (OpBaseCtrl opBaseCtrl : opBaseCtrlArr) {
                    if (opBaseCtrl != null && opBaseCtrl.isEnable()) {
                        opBaseCtrl.onKeyguardVisibilityChanged(z);
                    }
                }
            }
        };
    }

    public void init(Context context, ViewGroup viewGroup, StatusBar statusBar, CommandQueue commandQueue) {
        synchronized (this) {
            if (!this.mInit) {
                Log.d("OpLsState", "init");
                this.mContainer = viewGroup;
                this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
                this.mPhonstatusBar = statusBar;
                this.mInit = true;
                this.mContext = context;
                getNonUILooper();
                OpBaseCtrl[] opBaseCtrlArr = this.mControls;
                for (OpBaseCtrl opBaseCtrl : opBaseCtrlArr) {
                    if (opBaseCtrl != null) {
                        opBaseCtrl.setCallback(this);
                        opBaseCtrl.init(context);
                        opBaseCtrl.startCtrl();
                    }
                }
                this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
            }
        }
    }

    public void onFingerprintStartedGoingToSleep() {
        this.mIsFinishedScreenTuredOn = false;
    }

    public void onWallpaperChange(Bitmap bitmap) {
        OpBaseCtrl[] opBaseCtrlArr = this.mControls;
        for (OpBaseCtrl opBaseCtrl : opBaseCtrlArr) {
            if (opBaseCtrl != null && opBaseCtrl.isEnable()) {
                opBaseCtrl.onWallpaperChange(bitmap);
            }
        }
        this.mPhonstatusBar.onWallpaperChange(bitmap);
    }

    public void onScreenTurnedOn() {
        this.mIsFinishedScreenTuredOn = true;
        OpBaseCtrl[] opBaseCtrlArr = this.mControls;
        for (OpBaseCtrl opBaseCtrl : opBaseCtrlArr) {
            if (opBaseCtrl != null && opBaseCtrl.isEnable()) {
                opBaseCtrl.onScreenTurnedOn();
            }
        }
    }

    public Looper getNonUILooper() {
        Looper looper;
        synchronized (this) {
            if (this.mNonUiLooper == null) {
                HandlerThread handlerThread = new HandlerThread("OpLsState thread");
                handlerThread.start();
                this.mNonUiLooper = handlerThread.getLooper();
            }
            looper = this.mNonUiLooper;
        }
        return looper;
    }

    private class MyUIHandler extends Handler {
        private MyUIHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                synchronized (OpLsState.this) {
                }
            }
        }
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    public StatusBarKeyguardViewManager getStatusBarKeyguardViewManager() {
        return this.mStatusBarKeyguardViewManager;
    }

    public StatusBar getPhoneStatusBar() {
        return this.mPhonstatusBar;
    }

    public KeyguardUpdateMonitor getUpdateMonitor() {
        KeyguardUpdateMonitor keyguardUpdateMonitor;
        synchronized (this) {
            keyguardUpdateMonitor = this.mUpdateMonitor;
        }
        return keyguardUpdateMonitor;
    }

    public ViewGroup getContainer() {
        return this.mContainer;
    }

    public boolean isFinishedScreenTuredOn() {
        return this.mIsFinishedScreenTuredOn;
    }

    public void setBiometricUnlockController(BiometricUnlockController biometricUnlockController) {
        this.mBiometricUnlockController = biometricUnlockController;
    }

    public BiometricUnlockController getBiometricUnlockController() {
        return this.mBiometricUnlockController;
    }

    public void setFpAnimationCtrl(OpFingerprintAnimationCtrl opFingerprintAnimationCtrl) {
        this.mFingerprintAnimationCtrl = opFingerprintAnimationCtrl;
    }

    public OpFingerprintAnimationCtrl getFpAnimationCtrl() {
        return this.mFingerprintAnimationCtrl;
    }

    public Context getSystemUIContext() {
        return this.mContext;
    }
}
