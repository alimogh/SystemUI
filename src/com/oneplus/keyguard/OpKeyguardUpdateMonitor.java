package com.oneplus.keyguard;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SystemSensorManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.util.OpFeatures;
import android.util.SparseIntArray;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.settingslib.fuelgauge.BatteryStatus;
import com.android.settingslib.utils.ThreadUtils;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.phone.KeyguardBouncer;
import com.oneplus.android.context.IOneplusContext;
import com.oneplus.android.context.OneplusContext;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.aod.alwayson.OpAodAlwaysOnController;
import com.oneplus.display.IOneplusColorDisplayManager;
import com.oneplus.keyguard.clock.OpClockCtrl;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.biometrics.OpFingerprintDialogView;
import com.oneplus.systemui.biometrics.OpFodBurnInProtectionHelper;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.OpReflectionUtils;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
public class OpKeyguardUpdateMonitor {
    protected static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    public static final boolean IS_SUPPORT_BOOT_TO_ENTER_BOUNCER = OpFeatures.isSupport(new int[]{35});
    protected static final boolean IS_SUPPORT_CUSTOM_FINGERPRINT = OpUtils.isCustomFingerprint();
    public static final boolean IS_SUPPORT_FACE_UNLOCK = OpFeatures.isSupport(new int[]{34});
    private static final boolean IS_SUPPORT_FINGERPRINT_POCKET = (OpFeatures.isSupport(new int[]{61}) && OpFeatures.isSupport(new int[]{116}));
    public static final boolean IS_SUPPORT_MOTOR_CAMERA = OpFeatures.isSupport(new int[]{91});
    private String FOD_UI_DEBUG = "sys.prop.fod_ui_test";
    protected OpAodAlwaysOnController mAodAlwaysOnController = null;
    private HashMap<String, String> mAodFpAuthTimeMap = null;
    protected long mAodFpAuthenticatedTime = 0;
    private boolean mAutoFacelockEnabled = false;
    private long mBSPTotalTime = 0;
    private boolean mBootCompleted = false;
    private boolean mBouncerRecognizeEnabled = false;
    protected boolean mCameraLaunched;
    private OpClockCtrl mClockCtrl = OpClockCtrl.getInstance();
    private final Context mContext;
    private int mCurDisplayPoweStatus = 1;
    protected boolean mDuringAcquired = false;
    private boolean mFacelockEnabled = false;
    private boolean mFacelockLightingEnabled = false;
    private int mFacelockRunningType = 0;
    private ContentObserver mFacelockSettingsObserver;
    private int mFacelockSuccessTimes = 0;
    private boolean mFacelockUnlocking;
    private SparseIntArray mFailedAttempts = new SparseIntArray();
    private boolean mFakeLocking;
    private boolean mFingerprintAlreadyAuthenticated;
    protected SparseIntArray mFingerprintFailedAttempts = new SparseIntArray();
    protected OpFodBurnInProtectionHelper mFodBurnInProtectionHelper;
    private OpFingerprintDialogView mFodDialogView;
    private ContentObserver mFodOnAodStateObserver;
    private FingerprintManager mFpm;
    private int mGoingToSleepReason = -1;
    private boolean mImeShow = false;
    protected boolean mIsEmergencyPanelExpand = false;
    private boolean mIsFaceAdded = false;
    protected boolean mIsInBrickMode = false;
    private boolean mIsKeyguardDone = true;
    private boolean mIsLowLightEnv;
    private boolean mIsUserUnlocked = true;
    private boolean mKeyguardShowing = false;
    private boolean mLaunchingCamera;
    private boolean mLaunchingLeftAffordance;
    private boolean mLidOpen = true;
    protected boolean mLockoutState = false;
    private IOneplusColorDisplayManager mOneplusColorDisplayManager;
    protected boolean mPendingSubInfoChange = false;
    SensorEventListener mPocketListener = new SensorEventListener() { // from class: com.oneplus.keyguard.OpKeyguardUpdateMonitor.3
        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            int i;
            if (sensorEvent.values.length == 0) {
                Log.d("OpKeyguardUpdateMonitor", "Event has no values!");
                i = 0;
            } else {
                Log.d("OpKeyguardUpdateMonitor", "Event: value=" + sensorEvent.values[0] + " max=" + OpKeyguardUpdateMonitor.this.mPocketSensor.getMaximumRange());
                i = 1;
                if (!(sensorEvent.values[0] == 1.0f)) {
                    i = 2;
                }
            }
            if (OpKeyguardUpdateMonitor.this.mPocketState != i) {
                OpKeyguardUpdateMonitor opKeyguardUpdateMonitor = OpKeyguardUpdateMonitor.this;
                opKeyguardUpdateMonitor.updateFPStateBySensor(i, opKeyguardUpdateMonitor.mLidOpen);
            }
            if (OpKeyguardUpdateMonitor.this.mFodDialogView != null) {
                OpKeyguardUpdateMonitor.this.mFodDialogView.updateIconVisibility(false);
            }
        }
    };
    private Sensor mPocketSensor;
    private boolean mPocketSensorEnabled;
    private int mPocketState = 0;
    protected PowerManager mPowerManager;
    private boolean mPreventModeActive = false;
    private boolean mQSExpanded = false;
    private long mResetAttempsTimeInMillis = 0;
    private boolean mScreenTurningOn = false;
    private SensorManager mSensorManager;
    private boolean mShowFodOnAodEnabled = false;
    private boolean mShutdownDialogShow = false;
    private boolean mShutingDown = false;
    protected boolean mSimUnlockSlot0 = false;
    protected boolean mSimUnlockSlot1 = false;
    private boolean mSkipBouncerByFacelock = false;
    private OpClockCtrl.OnTimeUpdatedListener mTimeTickListener = new OpClockCtrl.OnTimeUpdatedListener() { // from class: com.oneplus.keyguard.OpKeyguardUpdateMonitor.2
        @Override // com.oneplus.keyguard.clock.OpClockCtrl.OnTimeUpdatedListener
        public void onTimeChanged() {
            Log.i("OpKeyguardUpdateMonitor", "onTimeChanged");
            if (OpKeyguardUpdateMonitor.this.getHandler() != null) {
                OpKeyguardUpdateMonitor.this.getHandler().removeMessages(301);
                OpKeyguardUpdateMonitor.this.getHandler().sendEmptyMessage(301);
            }
        }
    };
    private String mWakingUpReason = null;
    private Calendar mWakingUpTime = null;

    private boolean isSensorNear(int i, boolean z) {
        return i == 1 || !z;
    }

    /* access modifiers changed from: protected */
    public class OpHandler extends Handler {
        public OpHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 701) {
                boolean z = false;
                switch (i) {
                    case 500:
                        OpKeyguardUpdateMonitor.this.handleScreenTurningOn();
                        return;
                    case 501:
                        OpKeyguardUpdateMonitor opKeyguardUpdateMonitor = OpKeyguardUpdateMonitor.this;
                        if (opKeyguardUpdateMonitor.mDuringAcquired) {
                            opKeyguardUpdateMonitor.mDuringAcquired = false;
                            opKeyguardUpdateMonitor.updateFingerprintListeningState();
                            OpKeyguardUpdateMonitor.this.handleFingerprintTimeout();
                            return;
                        }
                        return;
                    case 502:
                        OpKeyguardUpdateMonitor opKeyguardUpdateMonitor2 = OpKeyguardUpdateMonitor.this;
                        if (message.arg1 != 1) {
                            z = true;
                        }
                        opKeyguardUpdateMonitor2.handleLidSwitchChanged(z);
                        return;
                    case 503:
                        OpKeyguardUpdateMonitor.this.handleSystemReady();
                        return;
                    case 504:
                        OpKeyguardUpdateMonitor opKeyguardUpdateMonitor3 = OpKeyguardUpdateMonitor.this;
                        if (message.arg1 != 1) {
                            z = true;
                        }
                        opKeyguardUpdateMonitor3.handlePreventModeChanged(z);
                        return;
                    case 505:
                        OpKeyguardUpdateMonitor opKeyguardUpdateMonitor4 = OpKeyguardUpdateMonitor.this;
                        if (message.arg1 != 1) {
                            z = true;
                        }
                        opKeyguardUpdateMonitor4.handleAlwaysOnChanged(z);
                        return;
                    case 506:
                        OpKeyguardUpdateMonitor opKeyguardUpdateMonitor5 = OpKeyguardUpdateMonitor.this;
                        if (message.arg1 != 1) {
                            z = true;
                        }
                        opKeyguardUpdateMonitor5.handleEnvironmentLightChanged(z);
                        return;
                    case 507:
                        OpKeyguardUpdateMonitor.this.handleDisplayPowerStatusChanged(message.arg1);
                        return;
                    case 508:
                        OpKeyguardUpdateMonitor.this.handleFinishedWakingUp();
                        return;
                    case 509:
                        OpKeyguardUpdateMonitor.this.handleBootCompleted();
                        return;
                    case 510:
                        OpKeyguardUpdateMonitor opKeyguardUpdateMonitor6 = OpKeyguardUpdateMonitor.this;
                        String str = (String) message.obj;
                        if (message.arg1 == 1) {
                            z = true;
                        }
                        opKeyguardUpdateMonitor6.handleOnVideoChanged(str, z);
                        return;
                    default:
                        return;
                }
            } else {
                OpKeyguardUpdateMonitor opKeyguardUpdateMonitor7 = OpKeyguardUpdateMonitor.this;
                if (opKeyguardUpdateMonitor7.mSimUnlockSlot0 || opKeyguardUpdateMonitor7.mSimUnlockSlot1) {
                    Log.d("OpKeyguardUpdateMonitor", "timeout delay of slot: " + message.arg1 + ", " + OpKeyguardUpdateMonitor.this.mSimUnlockSlot0 + ", " + OpKeyguardUpdateMonitor.this.mSimUnlockSlot1);
                    OpKeyguardUpdateMonitor.this.opHandlePendingSubInfoChange(message.arg1);
                }
            }
        }
    }

    protected OpKeyguardUpdateMonitor(Context context) {
        this.mContext = context;
        if (context.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
            this.mFpm = (FingerprintManager) context.getSystemService("fingerprint");
        }
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        if (IS_SUPPORT_CUSTOM_FINGERPRINT) {
            this.mOneplusColorDisplayManager = (IOneplusColorDisplayManager) OneplusContext.queryInterface(IOneplusContext.EType.ONEPLUS_COLORDISPLAY_MANAGER);
        }
        startClockCtrl();
        this.mAodAlwaysOnController = new OpAodAlwaysOnController(this.mContext);
        if (IS_SUPPORT_CUSTOM_FINGERPRINT) {
            watchForFodOnAodSettings();
            this.mAodFpAuthTimeMap = new HashMap<>();
        }
    }

    private void watchForFodOnAodSettings() {
        this.mFodOnAodStateObserver = new ContentObserver(getHandler()) { // from class: com.oneplus.keyguard.OpKeyguardUpdateMonitor.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                super.onChange(z);
                OpKeyguardUpdateMonitor opKeyguardUpdateMonitor = OpKeyguardUpdateMonitor.this;
                boolean z2 = true;
                if (Settings.System.getIntForUser(opKeyguardUpdateMonitor.mContext.getContentResolver(), "show_fod_on_aod_enabled", 1, -2) != 1) {
                    z2 = false;
                }
                opKeyguardUpdateMonitor.mShowFodOnAodEnabled = z2;
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("show_fod_on_aod_enabled"), false, this.mFodOnAodStateObserver, -1);
        this.mFodOnAodStateObserver.onChange(true);
    }

    private void startClockCtrl() {
        OpClockCtrl opClockCtrl = this.mClockCtrl;
        if (opClockCtrl != null) {
            opClockCtrl.onStartCtrl(this.mTimeTickListener, this.mContext);
        }
    }

    private void onScreenStatusChanged(boolean z) {
        OpClockCtrl opClockCtrl = this.mClockCtrl;
        if (opClockCtrl == null) {
            return;
        }
        if (z) {
            opClockCtrl.onScreenTurnedOn();
        } else {
            opClockCtrl.onScreenTurnedOff();
        }
    }

    /* access modifiers changed from: protected */
    public void handleStartedWakingUp() {
        this.mIsLowLightEnv = false;
        if (OpUtils.isCustomFingerprint()) {
            setPocketSensorEnabled(false);
        }
        onScreenStatusChanged(true);
        if (!this.mIsKeyguardDone) {
            this.mWakingUpTime = Calendar.getInstance();
        }
        this.mClockCtrl.onStartedWakingUp();
    }

    /* access modifiers changed from: protected */
    public void init() {
        SystemSensorManager systemSensorManager = new SystemSensorManager(this.mContext, getHandler().getLooper());
        this.mSensorManager = systemSensorManager;
        this.mPocketSensor = systemSensorManager.getDefaultSensor(33171025, true);
        checkDozeSettings();
    }

    /* access modifiers changed from: protected */
    public void opHandleFingerprintAcquired(int i) {
        if (i == 0 || i == 6) {
            this.mDuringAcquired = true;
            getHandler().removeMessages(501);
            getHandler().sendEmptyMessageDelayed(501, IS_SUPPORT_CUSTOM_FINGERPRINT ? 3000 : 1500);
            for (int i2 = 0; i2 < getCallbacks().size(); i2++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i2).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onFingerprintAcquired(i);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void opHandlePendingSubInfoChange(int i) {
        if (i == 0) {
            this.mSimUnlockSlot0 = false;
        } else if (i == 1) {
            this.mSimUnlockSlot1 = false;
        }
        if (!this.mSimUnlockSlot0 && !this.mSimUnlockSlot1) {
            if (this.mPendingSubInfoChange) {
                Log.d("OpKeyguardUpdateMonitor", "handle pending subinfo change");
                handleSimSubscriptionInfoChanged();
            }
            this.mPendingSubInfoChange = false;
        }
    }

    public void opReportSimUnlocked(int i) {
        int slotIndex = SubscriptionManager.getSlotIndex(i);
        Log.v("OpKeyguardUpdateMonitor", "reportSimUnlocked(subId=" + i + ", slotId=" + slotIndex + ")");
        if (slotIndex == 0) {
            this.mSimUnlockSlot0 = true;
        } else if (slotIndex == 1) {
            this.mSimUnlockSlot1 = true;
        }
        getHandler().sendMessageDelayed(getHandler().obtainMessage(701, slotIndex, 0), 2000);
        handleSimStateChange(i, slotIndex, 5);
    }

    public boolean isUserUnlocked() {
        if (!IS_SUPPORT_BOOT_TO_ENTER_BOUNCER || !getLockPatternUtils().isSecure(getCurrentUser())) {
            return true;
        }
        return this.mIsUserUnlocked;
    }

    public void setUserUnlocked(boolean z) {
        if (this.mIsUserUnlocked != z && z) {
            OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager().handleUserUnlocked();
            OpAodUtils.forceTurnOnFpBlackGesture(this.mContext, getCurrentUser());
        }
        this.mIsUserUnlocked = z;
    }

    public void setFodDialogView(OpFingerprintDialogView opFingerprintDialogView) {
        this.mFodDialogView = opFingerprintDialogView;
        this.mFodBurnInProtectionHelper.setFodDialogView(opFingerprintDialogView);
    }

    public void onImeShow(boolean z) {
        if (IS_SUPPORT_CUSTOM_FINGERPRINT) {
            boolean isUnlockingWithBiometricAllowed = isUnlockingWithBiometricAllowed();
            Log.d("OpKeyguardUpdateMonitor", "onImeShow: show:( " + this.mImeShow + " -> " + z + " ), mLockoutState= " + this.mLockoutState + ", isUnlockingWithBiometricAllowed= " + isUnlockingWithBiometricAllowed);
            if (this.mImeShow != z) {
                this.mImeShow = z;
                if (this.mLockoutState || !isUnlockingWithBiometricAllowed) {
                    Log.d("OpKeyguardUpdateMonitor", "onImeShow: in lockout state, just update ui.");
                } else {
                    Log.d("OpKeyguardUpdateMonitor", "onImeShow: update fingerprint listening state");
                    getHandler().postDelayed(getUpdateBiometricListeningStateRunnable(), isFingerprintDetectionRunning() ? 0 : 250);
                }
                OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
                if (opFingerprintDialogView != null && opFingerprintDialogView.isAttachedToWindow()) {
                    this.mFodDialogView.updateIconVisibility(false);
                }
            }
        }
    }

    public boolean isImeShow() {
        return this.mImeShow;
    }

    public void setQSExpanded(boolean z) {
        if (IS_SUPPORT_CUSTOM_FINGERPRINT && z != this.mQSExpanded) {
            Log.d("OpKeyguardUpdateMonitor", "setQSExpanded: " + z + ", callers= " + Debug.getCallers(1));
            this.mQSExpanded = z;
            if (isKeyguardVisible()) {
                updateFingerprintListeningState();
            }
            for (int i = 0; i < getCallbacks().size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onQSExpendChanged(z);
                }
            }
        }
    }

    public boolean isQSExpanded() {
        return this.mQSExpanded;
    }

    public void setShutdownDialogVisible(boolean z) {
        if (IS_SUPPORT_CUSTOM_FINGERPRINT && z != this.mShutdownDialogShow) {
            Log.d("OpKeyguardUpdateMonitor", "setShutdownDialogVisibility: " + z + ", callers= " + Debug.getCallers(1));
            this.mShutdownDialogShow = z;
            if (isKeyguardVisible()) {
                updateFingerprintListeningState();
            }
            for (int i = 0; i < getCallbacks().size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onShutdownDialogVisibilityChanged(z);
                }
            }
        }
    }

    public boolean isShutdownDialogVisible() {
        return this.mShutdownDialogShow;
    }

    public void notifyShutDownOrReboot() {
        this.mShutingDown = true;
        updateFingerprintListeningState();
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onShuttingDown();
            }
        }
    }

    public boolean isShuttingDown() {
        return this.mShutingDown;
    }

    public void notifyScreenTurningOn() {
        Log.d("OpKeyguardUpdateMonitor", "notifyScreenTurningOn");
        synchronized (this) {
            this.mScreenTurningOn = true;
        }
        getHandler().sendEmptyMessage(500);
    }

    public void handleScreenTurningOn() {
        int size = getCallbacks().size();
        for (int i = 0; i < size; i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onScreenTurningOn();
            }
        }
        if (IS_SUPPORT_CUSTOM_FINGERPRINT && !isDeviceInteractive() && isDreaming() && !isUnlockingWithBiometricAllowed()) {
            isFingerprintEnrolled(getCurrentUser());
        }
    }

    public boolean isScreenTurningOn() {
        return this.mScreenTurningOn;
    }

    public void earlyNotifySwitchingUser() {
        Log.d("OpKeyguardUpdateMonitor", "earlyNotifySwitchingUser");
        hideFODDim();
        setSwitchingUser(true);
    }

    public boolean isPreventModeActivte() {
        return this.mPreventModeActive;
    }

    public void dispatchAuthenticateChanged(boolean z, int i, int i2, int i3) {
        int size = getCallbacks().size();
        for (int i4 = 0; i4 < size; i4++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i4).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onAuthenticateChanged(z, i, i2, i3);
            }
        }
    }

    public void hideFODDim() {
        OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
        if (opFingerprintDialogView != null) {
            opFingerprintDialogView.hideFODDim();
        }
    }

    public void notifyFakeLocking(boolean z) {
        this.mFakeLocking = z;
    }

    public void setGoingToSleepReason(int i) {
        this.mGoingToSleepReason = i;
    }

    public int getGoingToSleepReason() {
        return this.mGoingToSleepReason;
    }

    public void notifyBrightnessChange() {
        OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
        if (opFingerprintDialogView != null) {
            opFingerprintDialogView.notifyBrightnessChange();
        }
    }

    public boolean isFingerprintEnrolled(int i) {
        FingerprintManager fingerprintManager = this.mFpm;
        return fingerprintManager != null && fingerprintManager.isHardwareDetected() && this.mFpm.getEnrolledFingerprints(i).size() > 0;
    }

    public void updateLaunchingCameraState(boolean z) {
        if (this.mLaunchingCamera != z) {
            this.mLaunchingCamera = z;
            updateFingerprintListeningState();
            OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
            if (opFingerprintDialogView != null) {
                opFingerprintDialogView.updateIconVisibility(false);
            }
        }
        this.mCameraLaunched = z;
        Log.i("OpKeyguardUpdateMonitor", "updateLaunchingCameraState:" + this.mCameraLaunched);
    }

    public boolean isCameraLaunched() {
        return this.mCameraLaunched;
    }

    public boolean isLaunchingCamera() {
        return this.mLaunchingCamera;
    }

    public void notifyKeyguardDone(boolean z) {
        Log.d("OpKeyguardUpdateMonitor", "notifyKeyguardDone isKeyguardDone " + z);
        if (this.mDuringAcquired) {
            getHandler().removeMessages(501);
            this.mDuringAcquired = false;
        }
        this.mIsKeyguardDone = z;
        if (this.mWakingUpTime != null && z) {
            reportMDMEvent();
            this.mWakingUpTime = null;
        }
        int size = getCallbacks().size();
        for (int i = 0; i < size; i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardDoneChanged(z);
            }
        }
        if (this.mIsKeyguardDone) {
            ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).notifyKeyguardDone();
        }
    }

    public boolean isKeyguardDone() {
        return this.mIsKeyguardDone;
    }

    /* access modifiers changed from: protected */
    public void opHandleStartedGoingToSleep() {
        this.mFingerprintAlreadyAuthenticated = false;
        if (IS_SUPPORT_CUSTOM_FINGERPRINT) {
            isFingerprintEnrolled(getCurrentUser());
        }
        onScreenStatusChanged(false);
        if (this.mWakingUpTime != null && !this.mIsKeyguardDone) {
            reportMDMEvent();
            this.mWakingUpTime = null;
        }
        this.mBSPTotalTime = 0;
    }

    public void opOnKeyguardVisibilityChanged(boolean z) {
        this.mKeyguardShowing = z;
        if (!z) {
            this.mFingerprintAlreadyAuthenticated = false;
        } else {
            SystemProperties.getInt(this.FOD_UI_DEBUG, 0);
        }
        Log.d("OpKeyguardUpdateMonitor", "keyguard showing " + z + " isOnePlusHomeApp " + OpUtils.isOnePlusHomeApp() + " isBouncer=" + isBouncer() + " mLaunchingCamera=" + this.mLaunchingCamera + " mLaunchingLeftAffordance=" + this.mLaunchingLeftAffordance);
        if (z) {
            ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).notifyNavBarButtonAlphaChanged(0.0f, true);
            OpUtils.setRecentUnlockBiometricFace(false);
            OpUtils.setRecentUnlockBiometricFinger(false);
        } else if (!OpUtils.isOnePlusHomeApp() || isBouncer() || this.mLaunchingCamera || this.mLaunchingLeftAffordance) {
            ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).notifyNavBarButtonAlphaChanged(1.0f, true);
        }
    }

    public void dispatchSystemReady() {
        getHandler().sendEmptyMessage(503);
    }

    public void handleSystemReady() {
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onSystemReady();
            }
        }
    }

    public int keyguardPinPasswordLength() {
        int i;
        try {
            i = (int) getLockPatternUtils().getLockSettings().getLong("lockscreen.pin_password_length", 0, getCurrentUser());
        } catch (Exception e) {
            Log.d("OpKeyguardUpdateMonitor", "getLong error: " + e.getMessage());
            i = 0;
        }
        if (i >= 4) {
            return i;
        }
        return 0;
    }

    public boolean isAutoCheckPinEnabled() {
        return keyguardPinPasswordLength() != 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFPStateBySensor(int i, boolean z) {
        int i2 = this.mPocketState;
        boolean z2 = this.mLidOpen;
        this.mPocketState = i;
        this.mLidOpen = z;
        int i3 = !z ? 1 : i;
        FingerprintManager fingerprintManager = this.mFpm;
        if (fingerprintManager != null) {
            fingerprintManager.updateStatus(i3);
        }
        if (isFingerprintDetectionRunning() && isSensorNear(i, z)) {
            updateFingerprintListeningState();
        } else if (isSensorNear(i2, z2) && !isSensorNear(i, z)) {
            updateFingerprintListeningState();
        }
    }

    private void setPocketSensorEnabled(boolean z) {
        if (isPreventModeEnabled(this.mContext)) {
            if (this.mLidOpen || !z) {
                int currentUser = getCurrentUser();
                FingerprintManager fingerprintManager = this.mFpm;
                boolean z2 = fingerprintManager != null && fingerprintManager.isHardwareDetected() && this.mFpm.getEnrolledFingerprints(currentUser).size() > 0;
                if (DEBUG) {
                    Log.d("OpKeyguardUpdateMonitor", "listen pocket-sensor: " + z + ", current=" + this.mPocketSensorEnabled + ", FP enabled=" + z2);
                }
                if (!z2 || !z) {
                    if (this.mPocketSensorEnabled) {
                        this.mPocketSensorEnabled = false;
                        if (isSensorNear(this.mPocketState, this.mLidOpen)) {
                            this.mPocketState = 0;
                            this.mLidOpen = true;
                            updateFingerprintListeningState();
                        }
                        this.mPocketState = 0;
                        this.mLidOpen = true;
                        FingerprintManager fingerprintManager2 = this.mFpm;
                        if (fingerprintManager2 != null) {
                            fingerprintManager2.updateStatus(0);
                        }
                        this.mSensorManager.unregisterListener(this.mPocketListener);
                        return;
                    }
                    handleLidSwitchChanged(true);
                } else if (!this.mPocketSensorEnabled) {
                    this.mPocketSensorEnabled = true;
                    this.mSensorManager.registerListener(this.mPocketListener, this.mPocketSensor, 3);
                }
            } else {
                Log.d("OpKeyguardUpdateMonitor", "not register when Lid closed");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLidSwitchChanged(boolean z) {
        if (z != this.mLidOpen) {
            updateFPStateBySensor(this.mPocketState, z);
        }
    }

    /* access modifiers changed from: protected */
    public void opHandleFingerprintError1(int i) {
        if (i == 7 || (IS_SUPPORT_CUSTOM_FINGERPRINT && i == 9)) {
            long timeInMillis = Calendar.getInstance().getTimeInMillis() - this.mResetAttempsTimeInMillis;
            Log.i("OpKeyguardUpdateMonitor", "diff = " + timeInMillis);
            if (timeInMillis >= 2000) {
                this.mLockoutState = true;
            }
            if (IS_SUPPORT_CUSTOM_FINGERPRINT) {
                showFodAndCountdownToHide("fingerprint on error");
            }
        }
    }

    public boolean isPreventModeEnabled(Context context) {
        if (!IS_SUPPORT_FINGERPRINT_POCKET) {
            return false;
        }
        try {
            if (Settings.System.getInt(context.getContentResolver(), "oem_acc_anti_misoperation_screen") != 0) {
                return true;
            }
            return false;
        } catch (Settings.SettingNotFoundException unused) {
            return false;
        }
    }

    public boolean isFirstUnlock() {
        return !getStrongAuthTracker().hasUserAuthenticatedSinceBoot();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePreventModeChanged(boolean z) {
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onPreventModeChanged(z);
            }
        }
        if (IS_SUPPORT_CUSTOM_FINGERPRINT && this.mQSExpanded) {
            updateFingerprintListeningState();
        }
    }

    public void notifyPreventModeChange(boolean z) {
        int i = !z ? 1 : 0;
        this.mPreventModeActive = z;
        getHandler().removeMessages(504);
        getHandler().sendMessage(getHandler().obtainMessage(504, i, 0));
    }

    /* access modifiers changed from: protected */
    public void opOnFingerprintAuthenticated(int i) {
        this.mFingerprintAlreadyAuthenticated = isUnlockingWithBiometricAllowed();
        if (OpLsState.getInstance().getPhoneStatusBar() != null) {
            OpLsState.getInstance().getPhoneStatusBar().onFingerprintAuthenticated();
        }
    }

    /* access modifiers changed from: protected */
    public void opHandleFingerprintLockoutReset() {
        this.mLockoutState = false;
    }

    /* access modifiers changed from: protected */
    public void opHandleScreenTurnedOn() {
        this.mScreenTurningOn = false;
        if (!OpUtils.isCustomFingerprint()) {
            setPocketSensorEnabled(false);
        } else if (!isDeviceInteractive()) {
            setPocketSensorEnabled(true);
        }
        if (OpUtils.isCustomFingerprint() && !isDeviceInteractive()) {
            updateFingerprintListeningState();
        }
        onScreenStatusChanged(true);
    }

    /* access modifiers changed from: protected */
    public void opHandleScreenTurnedOff() {
        this.mScreenTurningOn = false;
        if (!OpUtils.isCustomFingerprint()) {
            setPocketSensorEnabled(true);
        } else if (this.mAodAlwaysOnController.isAlwaysOnEnabled()) {
            setPocketSensorEnabled(true);
        } else {
            setPocketSensorEnabled(false);
        }
        if (OpUtils.isCustomFingerprint()) {
            updateFingerprintListeningState();
        }
        onScreenStatusChanged(false);
    }

    public void clearFailedUnlockAttempts(boolean z) {
        if (z) {
            Log.i("OpKeyguardUpdateMonitor", "ResetAttempsTimeInMillis = " + this.mResetAttempsTimeInMillis);
            this.mResetAttempsTimeInMillis = Calendar.getInstance().getTimeInMillis();
        }
        this.mFailedAttempts.delete(getCurrentUser());
        clearFingerprintFailedUnlockAttempts();
        Log.d("OpKeyguardUpdateMonitor", "clear " + z + ", " + isFacelockDisabled());
        if (!OpUtils.isWeakFaceUnlockEnabled()) {
            clearFailedFacelockAttempts();
        } else if (z || !isFacelockDisabled()) {
            clearFailedFacelockAttempts();
        }
    }

    public int getFailedUnlockAttempts(int i) {
        return this.mFailedAttempts.get(i, 0);
    }

    public void reportFailedStrongAuthUnlockAttempt(int i) {
        this.mFailedAttempts.put(i, getFailedUnlockAttempts(i) + 1);
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "confirm_lock_password_fragment.key_num_wrong_confirm_attempts", Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "confirm_lock_password_fragment.key_num_wrong_confirm_attempts", 0, getCurrentUser()) + 1, getCurrentUser());
    }

    private void clearFingerprintFailedUnlockAttempts() {
        this.mFingerprintFailedAttempts.delete(getCurrentUser());
    }

    public int getFingerprintFailedUnlockAttempts() {
        return this.mFingerprintFailedAttempts.get(getCurrentUser(), 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintTimeout() {
        if (DEBUG) {
            Log.d("OpKeyguardUpdateMonitor", "handleFingerprintTimeout");
        }
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFingerprintTimeout();
            }
        }
    }

    public void updateLaunchingLeftAffordance(boolean z) {
        this.mLaunchingLeftAffordance = z;
        updateFingerprintListeningState();
        OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
        if (opFingerprintDialogView != null) {
            opFingerprintDialogView.updateIconVisibility(false);
        }
    }

    public boolean isLaunchingLeftAffordance() {
        return this.mLaunchingLeftAffordance;
    }

    public boolean isFingerprintAlreadyAuthenticated() {
        return this.mFingerprintAlreadyAuthenticated;
    }

    public void resetFingerprintAlreadyAuthenticated() {
        this.mFingerprintAlreadyAuthenticated = false;
    }

    public boolean isFingerprintLockout() {
        return this.mLockoutState;
    }

    /* access modifiers changed from: protected */
    public boolean opShouldListenForFingerprint() {
        if (IS_SUPPORT_CUSTOM_FINGERPRINT) {
            if (!(!isDeviceInteractive() && !isScreenOn()) || this.mScreenTurningOn) {
                if ((!isKeyguardVisible() || isBouncer() || this.mPreventModeActive) ? false : this.mQSExpanded) {
                    Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: disableByQSExpanded");
                    return false;
                } else if (OpLsState.getInstance().getPhoneStatusBar().getPanelController().isFullyExpanded() && !this.mKeyguardShowing && !isBouncer()) {
                    Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: disableByPanelExpanded");
                    return false;
                } else if (!isUnlockingWithBiometricAllowed() && !isFingerprintLockout()) {
                    Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: biometric not allowed");
                    return false;
                } else if (this.mShutingDown) {
                    Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: Shuting Down");
                    return false;
                } else if (this.mShutdownDialogShow) {
                    Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: Shutdown Dialog showing");
                    return false;
                } else if (this.mFacelockRunningType == 4) {
                    Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: Facelock RECOGNIZED_OK");
                    return false;
                } else if (this.mIsKeyguardDone && !this.mFakeLocking) {
                    Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: Keyguard Done and not fake locking");
                    return false;
                } else if (this.mFacelockUnlocking) {
                    Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: FacelockUnlocking");
                    return false;
                } else if (!isDreaming() || !isKeyguardOccluded() || !isDeviceInteractive() || (!isScreenSaverEnabled() && !isScreenSaverActivatedOnDock())) {
                    if (this.mPowerManager.isInteractive()) {
                        if (isKeyguardOccluded() && !isBouncer()) {
                            Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: start to wake up and keyguard is occluded.");
                            return false;
                        } else if (this.mIsKeyguardDone) {
                            Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: start to wake up and keyguard is done.");
                            return false;
                        }
                    }
                    KeyguardBouncer bouncer = OpLsState.getInstance().getStatusBarKeyguardViewManager().getBouncer();
                    if (isBouncer() && bouncer.isSecurityModePassword() && bouncer.isKeyguardPasswordInputMethodPickerShow()) {
                        Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: bouncer password input method picker is showing");
                        return false;
                    }
                } else {
                    Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: screen saver is enabled");
                    return false;
                }
            } else {
                Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: screen off");
                return false;
            }
        }
        if (isBouncer() && this.mImeShow) {
            Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: IME show");
            return false;
        } else if (this.mFingerprintAlreadyAuthenticated) {
            Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: FingerprintAlreadyAuthenticated");
            return false;
        } else if (isSensorNear(this.mPocketState, this.mLidOpen) && isPreventModeEnabled(this.mContext)) {
            Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: prevent mode");
            return false;
        } else if (isGoingToSleep()) {
            Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: going to sleep");
            return false;
        } else {
            if (!this.mPowerManager.isInteractive() || isGoingToSleep()) {
                this.mLaunchingCamera = false;
            }
            if (this.mLaunchingCamera) {
                Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: Launching Camera");
                return false;
            } else if (this.mLaunchingLeftAffordance) {
                Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: LaunchingLeftAffordance");
                return false;
            } else if (this.mIsInBrickMode) {
                Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: Brick Mode");
                return false;
            } else if (!this.mIsEmergencyPanelExpand) {
                return true;
            } else {
                Log.d("OpKeyguardUpdateMonitor", "opShouldListenForFingerprint false: EmergencyPanelExpand");
                return false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void watchForFacelockSettings() {
        this.mFacelockSettingsObserver = new ContentObserver(getHandler()) { // from class: com.oneplus.keyguard.OpKeyguardUpdateMonitor.4
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                super.onChange(z);
                OpKeyguardUpdateMonitor.this.updateFacelockSettings();
                Log.d("OpKeyguardUpdateMonitor", "facelock state = " + OpKeyguardUpdateMonitor.this.mFacelockEnabled + ", " + OpKeyguardUpdateMonitor.this.mAutoFacelockEnabled + ", " + OpKeyguardUpdateMonitor.this.mFacelockLightingEnabled + ", " + OpKeyguardUpdateMonitor.this.mBouncerRecognizeEnabled + ", " + OpKeyguardUpdateMonitor.this.mFacelockSuccessTimes);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("oneplus_face_unlock_enable"), false, this.mFacelockSettingsObserver, 0);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("oneplus_auto_face_unlock_enable"), false, this.mFacelockSettingsObserver, 0);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("oneplus_face_unlock_assistive_lighting_enable"), false, this.mFacelockSettingsObserver, 0);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("oneplus_face_unlock_powerkey_recognize_enable"), false, this.mFacelockSettingsObserver, 0);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("oneplus_face_unlock_success_times"), false, this.mFacelockSettingsObserver, 0);
        updateFacelockSettings();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFacelockSettings() {
        boolean z = true;
        this.mFacelockEnabled = Settings.System.getIntForUser(this.mContext.getContentResolver(), "oneplus_face_unlock_enable", 0, 0) == 1;
        if (!(OpLsState.getInstance().getPhoneStatusBar() == null || OpLsState.getInstance().getPhoneStatusBar().getFacelockController() == null)) {
            OpLsState.getInstance().getPhoneStatusBar().getFacelockController().onFacelockEnableChanged(this.mFacelockEnabled);
        }
        this.mAutoFacelockEnabled = Settings.System.getIntForUser(this.mContext.getContentResolver(), "oneplus_auto_face_unlock_enable", 0, 0) == 1;
        boolean z2 = Settings.System.getIntForUser(this.mContext.getContentResolver(), "oneplus_face_unlock_assistive_lighting_enable", 0, 0) == 1;
        if (z2 != this.mFacelockLightingEnabled) {
            this.mFacelockLightingEnabled = z2;
            int size = getCallbacks().size();
            for (int i = 0; i < size; i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onFacelockLightingChanged(z2);
                }
            }
        }
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "oneplus_face_unlock_powerkey_recognize_enable", 0, 0) != 0) {
            z = false;
        }
        this.mBouncerRecognizeEnabled = z;
        this.mFacelockSuccessTimes = Settings.System.getIntForUser(this.mContext.getContentResolver(), "oneplus_face_unlock_success_times", 0, 0);
    }

    public void notifyFacelockStateChanged(final int i) {
        final int i2 = this.mFacelockRunningType;
        this.mFacelockRunningType = i;
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpKeyguardUpdateMonitor", "notifyFacelockStateChanged, type:" + i + ", isWeak:" + isWeakFaceTimeout());
        }
        if (isFacelockWaitingTap() && isWeakFaceTimeout()) {
            this.mFacelockRunningType = 0;
            Log.d("OpKeyguardUpdateMonitor", "[WeakFace] change to not running");
            i = 0;
        }
        if (i == 4 && OpUtils.isCustomFingerprint()) {
            getHandler().sendEmptyMessage(336);
        }
        getHandler().post(new Runnable() { // from class: com.oneplus.keyguard.OpKeyguardUpdateMonitor.5
            @Override // java.lang.Runnable
            public void run() {
                for (int i3 = 0; i3 < OpKeyguardUpdateMonitor.this.getCallbacks().size(); i3++) {
                    KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) ((WeakReference) OpKeyguardUpdateMonitor.this.getCallbacks().get(i3)).get();
                    if (keyguardUpdateMonitorCallback != null) {
                        keyguardUpdateMonitorCallback.onFacelockStateChanged(i);
                    }
                }
                int i4 = i2;
                int i5 = i;
                if (i4 != i5) {
                    if (i5 == 2) {
                        OpKeyguardUpdateMonitor.this.updateFacelockTrustState(true);
                    } else {
                        OpKeyguardUpdateMonitor.this.updateFacelockTrustState(false);
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFacelockTrustState(boolean z) {
        this.mSkipBouncerByFacelock = z;
        Log.d("OpKeyguardUpdateMonitor", "FacelockTrust," + z);
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustChanged(getCurrentUser());
            }
        }
    }

    public int getFacelockRunningType() {
        return this.mFacelockRunningType;
    }

    public boolean isFacelockWaitingTap() {
        return this.mFacelockRunningType == 12;
    }

    public boolean isFacelockAvailable() {
        int i = this.mFacelockRunningType;
        return i == 5 || i == 6 || i == 7 || i == 12;
    }

    public boolean isFacelockDisabled() {
        return this.mFacelockRunningType == 1;
    }

    public boolean isFacelockRecognizing() {
        return this.mFacelockRunningType == 3;
    }

    public boolean shouldShowFacelockIcon() {
        int i = this.mFacelockRunningType;
        return i == 3 || i == 4 || i == 5 || i == 6 || i == 7 || i == 12;
    }

    public boolean isCameraErrorState() {
        int i = this.mFacelockRunningType;
        return i == 8 || i == 9 || i == 10 || i == 11;
    }

    private void clearFailedFacelockAttempts() {
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onClearFailedFacelockAttempts();
            }
        }
        notifyFacelockStateChanged(0);
    }

    public boolean isFacelockEnabled() {
        return this.mFacelockEnabled;
    }

    public boolean isAutoFacelockEnabled() {
        if (IS_SUPPORT_MOTOR_CAMERA) {
            return !this.mBouncerRecognizeEnabled;
        }
        return this.mAutoFacelockEnabled;
    }

    public boolean isBouncerRecognizeEnabled() {
        return this.mBouncerRecognizeEnabled;
    }

    public static boolean isMotorCameraSupported() {
        return IS_SUPPORT_MOTOR_CAMERA;
    }

    public boolean isFacelockLightingEnabled() {
        return this.mFacelockLightingEnabled;
    }

    public boolean isFacelockAllowed() {
        Log.d("OpKeyguardUpdateMonitor", "isFacelockAllowed, visible:" + isKeyguardVisible() + ", inter:" + isDeviceInteractive() + ", bouncer:" + isBouncer() + ", done:" + this.mIsKeyguardDone + ", switching:" + isSwitchingUser() + ", enabled:" + isFacelockEnabled() + ", added:" + this.mIsFaceAdded + ", simpin:" + isSimPinSecure() + ", user:" + getCurrentUser() + ", fp authenticated:" + this.mFingerprintAlreadyAuthenticated + ", on:" + isScreenOn() + ", " + isWeakFaceTimeout());
        if (!allowShowingLock() || !isDeviceInteractive() || isSwitchingUser() || ((this.mFingerprintAlreadyAuthenticated && !isScreenOn()) || isWeakFaceTimeout() || !isUnlockWithFacelockPossible())) {
            return false;
        }
        return true;
    }

    public void setIsFaceAdded(boolean z) {
        this.mIsFaceAdded = z;
    }

    public int getFacelockNotifyMsgId(int i) {
        if (i == 1) {
            return C0015R$string.face_unlock_timeout;
        }
        switch (i) {
            case 5:
            case 12:
                return C0015R$string.face_unlock_tap_to_retry;
            case 6:
                return C0015R$string.face_unlock_no_face;
            case 7:
                return C0015R$string.face_unlock_fail;
            case 8:
                return C0015R$string.face_unlock_camera_error;
            case 9:
                return C0015R$string.face_unlock_no_permission;
            case 10:
                return C0015R$string.face_unlock_retry_other;
            case 11:
                return C0015R$string.face_unlock_retry_other;
            default:
                return 0;
        }
    }

    public boolean shouldPlayFacelockFailAnim() {
        int i = this.mFacelockRunningType;
        return i == 1 || i == 6 || i == 7 || i == 8 || i == 9 || i == 10 || i == 11;
    }

    public boolean canSkipBouncerByFacelock() {
        return this.mSkipBouncerByFacelock;
    }

    public void onFacelockUnlocking(boolean z) {
        this.mFacelockUnlocking = z;
        OpLsState.getInstance().getPhoneStatusBar().onFacelockUnlocking(this.mFacelockUnlocking);
    }

    public boolean isFacelockUnlocking() {
        return this.mFacelockUnlocking;
    }

    public boolean isUnlockWithFacelockPossible() {
        return isFacelockEnabled() && this.mIsFaceAdded && getLockPatternUtils().isSecure(getCurrentUser()) && !isSimPinSecure() && getCurrentUser() == 0;
    }

    public boolean isFaceAdded() {
        return this.mIsFaceAdded;
    }

    public void notifyPasswordLockout() {
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onPasswordLockout();
            }
        }
    }

    public boolean allowShowingLock() {
        if (isKeyguardVisible()) {
            return true;
        }
        if (!isBouncer() || isKeyguardGoingAway() || isForegroundApp("com.oneplus.camera")) {
            return false;
        }
        return true;
    }

    private boolean isForegroundApp(String str) {
        if (str == null) {
            return false;
        }
        List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) this.mContext.getSystemService(ActivityManager.class)).getRunningTasks(1);
        return !runningTasks.isEmpty() && str.equals(runningTasks.get(0).topActivity.getPackageName());
    }

    public void reportFaceUnlock() {
        if (this.mFacelockSuccessTimes < 3) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "oneplus_face_unlock_success_times", this.mFacelockSuccessTimes + 1, getCurrentUser());
            this.mFacelockSuccessTimes++;
        }
    }

    public boolean getFacelockNoticeEnabled() {
        if (isBouncerRecognizeEnabled() && this.mFacelockSuccessTimes < 3) {
            return true;
        }
        return false;
    }

    public void setWakingUpReason(String str) {
        this.mWakingUpReason = str;
    }

    public String getWakingUpReason() {
        return this.mWakingUpReason;
    }

    public void onBrickModeChanged(boolean z) {
        this.mIsInBrickMode = z;
    }

    public boolean isSensorDetectedNear() {
        return isSensorNear(this.mPocketState, this.mLidOpen) && isPreventModeEnabled(this.mContext);
    }

    public void notifyDisplayKeyguardUnlockSuccess() {
        if (IS_SUPPORT_CUSTOM_FINGERPRINT && this.mOneplusColorDisplayManager != null) {
            if (DEBUG) {
                Log.i("OpKeyguardUpdateMonitor", "unlock keyguard and notify display");
            }
            this.mOneplusColorDisplayManager.setExitFingerPrintModeWay(true);
        }
    }

    /* access modifiers changed from: protected */
    public void opDump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        int currentUser = getCurrentUser();
        FingerprintManager fingerprintManager = this.mFpm;
        if (fingerprintManager != null && fingerprintManager.isHardwareDetected()) {
            getStrongAuthTracker().getStrongAuthForUser(currentUser);
            printWriter.println("    FingerprintFailedAttempts=" + getFingerprintFailedUnlockAttempts());
            printWriter.println("    mPocketSensorEnabled=" + this.mPocketSensorEnabled);
            printWriter.println("    mPocketState=" + this.mPocketState);
            printWriter.println("    mLaunchingCamera=" + this.mLaunchingCamera);
            printWriter.println("    mDuringAcquired=" + this.mDuringAcquired);
            printWriter.println("    mLockoutState=" + this.mLockoutState);
            printWriter.println("    mFingerprintAlreadyAuthenticated=" + this.mFingerprintAlreadyAuthenticated);
            printWriter.println("    EnrollSize=" + this.mFpm.getEnrolledFingerprints(currentUser).size());
        }
        printWriter.println("    mBatteryStatus=" + getBatteryStatus().status + ", level=" + getBatteryStatus().level + ", health=" + getBatteryStatus().health + ", maxChargingWattage=" + getBatteryStatus().maxChargingWattage);
        StringBuilder sb = new StringBuilder();
        sb.append("    mKeyguardIsVisible=");
        sb.append(isKeyguardIsVisible());
        printWriter.println(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("    mGoingToSleep=");
        sb2.append(isGoingToSleep());
        printWriter.println(sb2.toString());
        printWriter.println("    isPreventModeEnabled=" + isPreventModeEnabled(this.mContext));
        printWriter.println("    mPreventModeActive=" + this.mPreventModeActive);
        printWriter.println("    mDeviceProvisioned=" + isDeviceProvisioned());
        printWriter.println("    getFailedUnlockAttempts=" + getFailedUnlockAttempts(currentUser));
        printWriter.println("    getUserCanSkipBouncer=" + getUserCanSkipBouncer(currentUser));
        printWriter.println("    mDeviceInteractive=" + isDeviceInteractive());
        printWriter.println("    mScreenOn=" + isScreenOn());
        printWriter.println("    mIsKeyguardDone=" + this.mIsKeyguardDone);
        printWriter.println("    IS_SUPPORT_BOOT_TO_ENTER_BOUNCER=" + IS_SUPPORT_BOOT_TO_ENTER_BOUNCER);
        printWriter.println("    mIsUserUnlocked=" + isUserUnlocked());
        printWriter.println("    mSimUnlockSlot0=" + this.mSimUnlockSlot0);
        printWriter.println("    mSimUnlockSlot1=" + this.mSimUnlockSlot1);
        printWriter.println("    mPendingSubInfoChange=" + this.mPendingSubInfoChange);
        printWriter.println("    IS_SUPPORT_FACE_UNLOCK=" + IS_SUPPORT_FACE_UNLOCK);
        printWriter.println("    mIsFaceAdded=" + this.mIsFaceAdded);
        printWriter.println("    mIsWeakFaceTimeout=" + isWeakFaceTimeout());
        printWriter.println("    mFacelockRunningType=" + this.mFacelockRunningType);
        printWriter.println("    isSecure=" + getLockPatternUtils().isSecure(getCurrentUser()));
        printWriter.println("    getCurrentUser=" + getCurrentUser());
        printWriter.println("    mSkipBouncerByFacelock=" + this.mSkipBouncerByFacelock);
        printWriter.println("    mFacelockUnlocking=" + this.mFacelockUnlocking);
        printWriter.println("    mBouncerRecognizeEnabled=" + this.mBouncerRecognizeEnabled);
        printWriter.println("    mFacelockTimes=" + this.mFacelockSuccessTimes);
        printWriter.println("    IS_SUPPORT_FINGERPRINT_POCKET=" + IS_SUPPORT_FINGERPRINT_POCKET);
        printWriter.println("    IS_SUPPORT_MOTOR_CAMERA=" + IS_SUPPORT_MOTOR_CAMERA);
        printWriter.println("    isFacelockDisabled=" + isFacelockDisabled());
        printWriter.println("    isUnlockWithFacelockPossible=" + isUnlockWithFacelockPossible());
        printWriter.println("    isWeakFaceUnlockEnabled=" + OpUtils.isWeakFaceUnlockEnabled());
        printWriter.println("    isFodShouldHiddenOnAod= " + isFodShouldHiddenOnAod());
        OpAodAlwaysOnController opAodAlwaysOnController = this.mAodAlwaysOnController;
        printWriter.println(opAodAlwaysOnController != null ? opAodAlwaysOnController.toString() : "");
        printWriter.println("    mBootCompleted=" + this.mBootCompleted);
        if (SystemProperties.getInt("sys.debug.systemui.pin", 0) == 56) {
            printWriter.println("    length=" + keyguardPinPasswordLength());
        }
    }

    private boolean isScreenSaverEnabled() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "screensaver_enabled", this.mContext.getResources().getBoolean(17891429) ? 1 : 0, -2) != 0;
    }

    private boolean isScreenSaverActivatedOnDock() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "screensaver_activate_on_dock", this.mContext.getResources().getBoolean(17891427) ? 1 : 0, -2) != 0;
    }

    public HashMap<Integer, ServiceState> opGetServiceStates() {
        return getServiceStates();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFingerprintListeningState() {
        OpReflectionUtils.methodInvokeVoid(KeyguardUpdateMonitor.class, this, "updateFingerprintListeningState", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private OpHandler getHandler() {
        return (OpHandler) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mHandler");
    }

    private Runnable getUpdateBiometricListeningStateRunnable() {
        return (Runnable) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mUpdateBiometricListeningState");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ArrayList<WeakReference<KeyguardUpdateMonitorCallback>> getCallbacks() {
        return (ArrayList) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mCallbacks");
    }

    private boolean isKeyguardVisible() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(KeyguardUpdateMonitor.class, this, "isKeyguardVisible", new Object[0])).booleanValue();
    }

    private boolean isDeviceInteractive() {
        return ((Boolean) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mDeviceInteractive")).booleanValue();
    }

    private boolean isDreaming() {
        return ((Boolean) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mIsDreaming")).booleanValue();
    }

    private boolean isKeyguardOccluded() {
        return ((Boolean) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mKeyguardOccluded")).booleanValue();
    }

    public boolean isUnlockingWithBiometricAllowed() {
        try {
            return ((Boolean) OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(KeyguardUpdateMonitor.class, "isUnlockingWithBiometricAllowed", Boolean.TYPE), Boolean.TRUE)).booleanValue();
        } catch (Exception e) {
            Log.e("OpKeyguardUpdateMonitor", "isUnlockingWithBiometricAllowed occur error", e);
            return false;
        }
    }

    public boolean isWeakFaceTimeout() {
        Object methodInvokeVoid = OpReflectionUtils.methodInvokeVoid(KeyguardUpdateMonitor.class, this, "isWeakFaceTimeout", new Object[0]);
        if (methodInvokeVoid != null) {
            return ((Boolean) methodInvokeVoid).booleanValue();
        }
        return false;
    }

    public boolean isGoingToSleep() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(KeyguardUpdateMonitor.class, this, "isGoingToSleep", new Object[0])).booleanValue();
    }

    public boolean isSwitchingUser() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(KeyguardUpdateMonitor.class, this, "isSwitchingUser", new Object[0])).booleanValue();
    }

    public boolean isSimPinSecure() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(KeyguardUpdateMonitor.class, this, "isSimPinSecure", new Object[0])).booleanValue();
    }

    private boolean isKeyguardGoingAway() {
        return ((Boolean) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mKeyguardGoingAway")).booleanValue();
    }

    private int getCurrentUser() {
        return ((Integer) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "sCurrentUser")).intValue();
    }

    private LockPatternUtils getLockPatternUtils() {
        return (LockPatternUtils) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mLockPatternUtils");
    }

    private KeyguardUpdateMonitor.StrongAuthTracker getStrongAuthTracker() {
        return (KeyguardUpdateMonitor.StrongAuthTracker) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mStrongAuthTracker");
    }

    private boolean isFingerprintDetectionRunning() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(KeyguardUpdateMonitor.class, this, "isFingerprintDetectionRunning", new Object[0])).booleanValue();
    }

    private boolean isScreenOn() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(KeyguardUpdateMonitor.class, this, "isScreenOn", new Object[0])).booleanValue();
    }

    private boolean isBouncer() {
        return ((Boolean) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mBouncer")).booleanValue();
    }

    private void setSwitchingUser(boolean z) {
        OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(KeyguardUpdateMonitor.class, "setSwitchingUser", Boolean.TYPE), Boolean.valueOf(z));
    }

    private BatteryStatus getBatteryStatus() {
        return (BatteryStatus) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mBatteryStatus");
    }

    private boolean isKeyguardIsVisible() {
        return ((Boolean) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mKeyguardIsVisible")).booleanValue();
    }

    private boolean isDeviceProvisioned() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(KeyguardUpdateMonitor.class, this, "isDeviceProvisioned", new Object[0])).booleanValue();
    }

    private boolean getUserCanSkipBouncer(int i) {
        return ((Boolean) OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(KeyguardUpdateMonitor.class, "getUserCanSkipBouncer", Integer.TYPE), Integer.valueOf(i))).booleanValue();
    }

    private void handleSimSubscriptionInfoChanged() {
        OpReflectionUtils.methodInvokeVoid(KeyguardUpdateMonitor.class, this, "handleSimSubscriptionInfoChanged", new Object[0]);
    }

    private void handleSimStateChange(int i, int i2, int i3) {
        Class cls = Integer.TYPE;
        OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(KeyguardUpdateMonitor.class, "handleSimStateChange", cls, cls, cls), Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3));
    }

    private HashMap<Integer, ServiceState> getServiceStates() {
        return (HashMap) OpReflectionUtils.getValue(KeyguardUpdateMonitor.class, this, "mServiceStates");
    }

    private void reportMDMEvent() {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("reportMDMEvent: mWakingUpTime is null = ");
            sb.append(this.mWakingUpTime == null ? "true" : "false");
            Log.i("OpKeyguardUpdateMonitor", sb.toString());
        }
        if (this.mWakingUpTime != null) {
            Calendar instance = Calendar.getInstance();
            KeyguardUpdateMonitor.getCurrentUser();
            OpMdmLogger.log("keyguard_temp", "keyguard_temp", String.valueOf(TimeUnit.MILLISECONDS.toSeconds(instance.getTimeInMillis() - this.mWakingUpTime.getTimeInMillis())), "X9HFK50WT7");
        }
    }

    public void notifyKeyguardFadedAway() {
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardFadedAway();
            }
        }
    }

    public boolean isAlwaysOnEnabled() {
        return this.mAodAlwaysOnController.isAlwaysOnEnabled();
    }

    public OpAodAlwaysOnController getAodAlwaysOnController() {
        return this.mAodAlwaysOnController;
    }

    public void showFodAndCountdownToHide(String str) {
        this.mFodBurnInProtectionHelper.startSchedule(str);
    }

    public boolean isFodShouldHiddenOnAod() {
        return this.mFodBurnInProtectionHelper.isFodHiddenOnAod() || !this.mShowFodOnAodEnabled;
    }

    private void checkDozeSettings() {
        if (!OpAodUtils.isSupportAlwaysOn()) {
            boolean z = true;
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "doze_always_on", 0, 0) != 1) {
                z = false;
            }
            if (z) {
                Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "doze_always_on", 0, 0);
            }
        }
    }

    public boolean isFodSupportOnAod() {
        return this.mShowFodOnAodEnabled;
    }

    public boolean isOpFingerprintDisabled(int i) {
        return ((Boolean) OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(KeyguardUpdateMonitor.class, "isFingerprintDisabled", Integer.TYPE), Integer.valueOf(i))).booleanValue();
    }

    public boolean isFodHintShowing() {
        if (OpLsState.getInstance().getPhoneStatusBar() != null) {
            return OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager().hasHintText();
        }
        return false;
    }

    public void updateFodIconVisibility() {
        OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
        if (opFingerprintDialogView != null) {
            opFingerprintDialogView.updateIconVisibility(false);
        }
    }

    /* access modifiers changed from: protected */
    public void opHandleUserSwitch(int i) {
        Log.i("OpKeyguardUpdateMonitor", "opHandleUserSwitch");
        boolean z = true;
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "show_fod_on_aod_enabled", 1, i) != 1) {
            z = false;
        }
        this.mShowFodOnAodEnabled = z;
    }

    public void dispatchAlwaysOnEnableChanged(boolean z) {
        int i = !z ? 1 : 0;
        getHandler().removeMessages(505);
        getHandler().sendMessage(getHandler().obtainMessage(505, i, 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAlwaysOnChanged(boolean z) {
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onAlwaysOnEnableChanged(z);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyWallpaperAodUnlock() {
        Log.i("OpKeyguardUpdateMonitor", "notifyWallpaperAodUnlock: " + Debug.getCallers(3));
        Intent intent = new Intent("com.oneplus.systemui.aod_unlock");
        intent.setPackage("com.oneplus.wallpaper");
        this.mContext.sendBroadcast(intent);
    }

    public boolean isLowLightEnv() {
        return this.mIsLowLightEnv;
    }

    public void notifyEnvironmentLightChanged(boolean z) {
        int i = !z ? 1 : 0;
        this.mIsLowLightEnv = z;
        getHandler().removeMessages(506);
        getHandler().sendMessage(getHandler().obtainMessage(506, i, 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEnvironmentLightChanged(boolean z) {
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onEnvironmentLightChanged(z);
            }
        }
    }

    public void notifyFpAcquiredInfo(int i) {
        HashMap<String, String> hashMap;
        if (i != 0) {
            int i2 = i & 1023;
            int i3 = (i >> 10) & 255;
            int i4 = (i >> 22) & 3;
            if (((i >> 24) & 255) == 0 && (hashMap = this.mAodFpAuthTimeMap) != null) {
                hashMap.clear();
                this.mBSPTotalTime = Long.valueOf((long) (i2 + i3)).longValue();
                this.mAodFpAuthTimeMap.put("finger_time_1", String.valueOf(i2));
                this.mAodFpAuthTimeMap.put("finger_time_2", String.valueOf(i4));
                this.mAodFpAuthTimeMap.put("finger_time_3", String.valueOf(i3));
            }
        }
    }

    public void onWakingUpScrimAnimationStart(long j) {
        HashMap<String, String> hashMap;
        long j2 = this.mAodFpAuthenticatedTime;
        if (j2 != 0 && j != 0 && this.mBSPTotalTime != 0 && (hashMap = this.mAodFpAuthTimeMap) != null) {
            long j3 = j - j2;
            hashMap.put("finger_time_4", String.valueOf(j3));
            this.mAodFpAuthTimeMap.put("finger_time_0", String.valueOf(j3 + this.mBSPTotalTime));
            if (this.mAodFpAuthTimeMap.size() != 5) {
                Log.w("OpKeyguardUpdateMonitor", "aod unlock mdm recorders incorrect: " + this.mAodFpAuthTimeMap.size());
            } else {
                ThreadUtils.postOnBackgroundThread(new Runnable() { // from class: com.oneplus.keyguard.-$$Lambda$OpKeyguardUpdateMonitor$_S0XabNqBJYebkaW2v7ppWxNC80
                    @Override // java.lang.Runnable
                    public final void run() {
                        OpKeyguardUpdateMonitor.this.lambda$onWakingUpScrimAnimationStart$0$OpKeyguardUpdateMonitor();
                    }
                });
            }
            this.mBSPTotalTime = 0;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onWakingUpScrimAnimationStart$0 */
    public /* synthetic */ void lambda$onWakingUpScrimAnimationStart$0$OpKeyguardUpdateMonitor() {
        OpMdmLogger.log("lock_unlock_success", this.mAodFpAuthTimeMap, "X9HFK50WT7");
    }

    public void notifyDisplayPowerStatusChanged(int i) {
        if (DEBUG) {
            Log.i("OpKeyguardUpdateMonitor", "notifyDisplayPowerStatusChanged= " + i + ", callstack= " + Debug.getCallers(2));
        }
        if (this.mCurDisplayPoweStatus != i) {
            this.mCurDisplayPoweStatus = i;
            getHandler().sendMessage(getHandler().obtainMessage(507, i, 0));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDisplayPowerStatusChanged(int i) {
        for (int i2 = 0; i2 < getCallbacks().size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDisplayPowerStatusChanged(i);
            }
        }
    }

    public int getDisplayPowerStatus() {
        return this.mCurDisplayPoweStatus;
    }

    public void dispatchFinishedWakingUp() {
        getHandler().sendEmptyMessage(508);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFinishedWakingUp() {
        int size = getCallbacks().size();
        for (int i = 0; i < size; i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFinishedWakingUp();
            }
        }
    }

    public void onEmergencyPanelExpandChanged(boolean z) {
        boolean isUnlockingWithBiometricAllowed = isUnlockingWithBiometricAllowed();
        Log.d("OpKeyguardUpdateMonitor", "onEmergencyPanelExpandChanged: show:( " + this.mIsEmergencyPanelExpand + " -> " + z + " ), mLockoutState= " + this.mLockoutState + ", isUnlockingWithBiometricAllowed= " + isUnlockingWithBiometricAllowed);
        if (this.mIsEmergencyPanelExpand != z) {
            this.mIsEmergencyPanelExpand = z;
            if (this.mLockoutState || !isUnlockingWithBiometricAllowed) {
                Log.d("OpKeyguardUpdateMonitor", "onEmergencyPanelExpandChanged: in lockout state, just update ui.");
            } else {
                Log.d("OpKeyguardUpdateMonitor", "onEmergencyPanelExpandChanged: update fingerprint listening state");
                getHandler().postDelayed(getUpdateBiometricListeningStateRunnable(), isFingerprintDetectionRunning() ? 0 : 250);
            }
            OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
            if (opFingerprintDialogView != null && opFingerprintDialogView.isAttachedToWindow()) {
                this.mFodDialogView.updateIconVisibility(false);
            }
        }
    }

    public boolean isEmergencyPanelExpand() {
        return this.mIsEmergencyPanelExpand;
    }

    public void notifyInsetsChanged() {
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onInsetsChanged();
            }
        }
    }

    public void dispatchBootCompleted() {
        getHandler().sendEmptyMessage(509);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBootCompleted() {
        if (!this.mBootCompleted) {
            this.mBootCompleted = true;
            for (int i = 0; i < getCallbacks().size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onBootCompleted();
                }
            }
        }
    }

    public void notifyVideoChanged(String str, boolean z) {
        getHandler().sendMessage(getHandler().obtainMessage(510, z ? 1 : 0, 0, str));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOnVideoChanged(String str, boolean z) {
        for (int i = 0; i < getCallbacks().size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = getCallbacks().get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onVideoChanged(str, z);
            }
        }
    }

    public boolean hasBootCompleted() {
        return this.mBootCompleted;
    }
}
