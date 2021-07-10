package com.oneplus.faceunlock;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SystemSensorManager;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricSourceType;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.Vibrator;
import android.util.Log;
import android.util.OpFeatures;
import android.view.WindowManagerGlobal;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarWindowController;
import com.oneplus.anim.OpCameraAnimateController;
import com.oneplus.faceunlock.internal.IOPFaceSettingService;
import com.oneplus.faceunlock.internal.IOPFacelockCallback;
import com.oneplus.faceunlock.internal.IOPFacelockService;
import com.oneplus.keyguard.OpKeyguardUpdateMonitor;
import com.oneplus.systemui.keyguard.OpKeyguardViewMediator;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.OpUtils;
import com.oneplus.util.VibratorSceneUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
public class OpFacelockController extends KeyguardUpdateMonitorCallback {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private static final int FAILED_ATTEMPTS_TO_TIMEOUT = (OpUtils.isWeakFaceUnlockEnabled() ? 3 : 5);
    private static int mFaceUnlockNoticeDelay = 3000;
    private final boolean isLod;
    private boolean mBinding = false;
    private boolean mBindingSetting = false;
    private boolean mBouncer = false;
    private boolean mBoundToService = false;
    private boolean mBoundToSettingService = false;
    private final BroadcastReceiver mBroadcastReceiver;
    private OpCameraAnimateController mCameraAnim;
    private boolean mCameraLaunching = false;
    private final ServiceConnection mConnection;
    private Context mContext;
    private boolean mEnterBouncerAfterScreenOn;
    BiometricUnlockController mFPC;
    private boolean mFaceLockActive = false;
    private HandlerThread mFacelockThread;
    private int mFailAttempts;
    private boolean mFirst = true;
    private long mFpFailTimeStamp;
    private FingerprintManager mFpm;
    private Handler mHandler;
    private KeyguardIndicationController mIndicator;
    private boolean mIsGoingToSleep = false;
    private boolean mIsKeyguardShowing = false;
    private boolean mIsScreenOffUnlock = false;
    private boolean mIsScreenTurnedOn = false;
    private boolean mIsScreenTurningOn = false;
    private boolean mIsSleep = false;
    private KeyguardStateCallback mKeyguardStateCallback;
    private KeyguardViewMediator mKeyguardViewMediator;
    private final Sensor mLightSensor;
    private final SensorEventListener mLightSensorListener;
    private int mLightingModeBrightness;
    private boolean mLightingModeEnabled;
    private int mLightingModeSensorThreshold;
    private boolean mLockout = false;
    private LinkedList<Long> mMotorQueue;
    private boolean mNeedToPendingStopFacelock = false;
    private final IOPFacelockCallback mOPFacelockCallback;
    private final Runnable mOffAuthenticateRunnable;
    private boolean mPendingFacelockWhenBouncer;
    private String mPendingLaunchCameraSource = null;
    private boolean mPendingStopFacelock = false;
    private StatusBar mPhoneStatusBar;
    IPowerManager mPowerManager;
    private final Runnable mResetScreenOnRunnable;
    private SensorManager mSensorManager;
    private int mSensorRate;
    private IOPFacelockService mService;
    private final ServiceConnection mSettingConnection;
    private IOPFaceSettingService mSettingService;
    private boolean mSimSecure;
    long mSleepTime;
    private boolean mStartFacelockWhenScreenOn = false;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private Handler mUIHandler;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private Vibrator mVibrator;

    private int downMotorBySystemApp() {
        return -999;
    }

    private int upMotorBySystemApp() {
        return -999;
    }

    public OpFacelockController(Context context, KeyguardViewMediator keyguardViewMediator, StatusBar statusBar, StatusBarKeyguardViewManager statusBarKeyguardViewManager, StatusBarWindowController statusBarWindowController, BiometricUnlockController biometricUnlockController) {
        boolean isSupport = OpFeatures.isSupport(new int[]{162});
        this.isLod = isSupport;
        this.mLightingModeSensorThreshold = isSupport ? 5 : 0;
        this.mLightingModeBrightness = 300;
        this.mLightingModeEnabled = false;
        this.mPendingFacelockWhenBouncer = false;
        this.mSleepTime = 0;
        this.mMotorQueue = new LinkedList<>();
        this.mEnterBouncerAfterScreenOn = false;
        this.mSimSecure = false;
        this.mFpFailTimeStamp = 0;
        this.mBroadcastReceiver = new BroadcastReceiver() { // from class: com.oneplus.faceunlock.OpFacelockController.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                if ("com.oneplus.faceunlock.action.FACE_SETTING_CHANGED".equals(intent.getAction())) {
                    OpFacelockController.this.mHandler.removeMessages(12);
                    OpFacelockController.this.mHandler.sendEmptyMessage(12);
                    if (OpFacelockController.DEBUG) {
                        Log.d("OpFacelockController", "intent to update face added");
                    }
                }
            }
        };
        this.mResetScreenOnRunnable = new Runnable() { // from class: com.oneplus.faceunlock.OpFacelockController.3
            @Override // java.lang.Runnable
            public void run() {
                Log.d("OpFacelockController", "reset screen on, offUnlock:" + OpFacelockController.this.mIsScreenOffUnlock);
                if (OpFacelockController.this.mIsScreenOffUnlock) {
                    OpFacelockController.this.updateKeyguardAlpha(1, true, true);
                }
            }
        };
        this.mOffAuthenticateRunnable = new Runnable() { // from class: com.oneplus.faceunlock.OpFacelockController.9
            @Override // java.lang.Runnable
            public void run() {
                OpFacelockController.this.mKeyguardViewMediator.notifyScreenOffAuthenticate(false, OpKeyguardViewMediator.AUTHENTICATE_FACEUNLOCK);
            }
        };
        this.mConnection = new ServiceConnection() { // from class: com.oneplus.faceunlock.OpFacelockController.12
            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d("OpFacelockController", "Connected to Facelock service");
                OpFacelockController.this.mService = IOPFacelockService.Stub.asInterface(iBinder);
                OpFacelockController.this.mBinding = false;
                OpFacelockController.this.mBoundToService = true;
                OpFacelockController.this.tryToStartFaceLock(false);
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName componentName) {
                Log.e("OpFacelockController", "disconnect from Facelock service");
                OpFacelockController.this.mService = null;
                OpFacelockController.this.mBinding = false;
                OpFacelockController.this.mBoundToService = false;
            }
        };
        this.mSettingConnection = new ServiceConnection() { // from class: com.oneplus.faceunlock.OpFacelockController.13
            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                OpFacelockController.this.mSettingService = IOPFaceSettingService.Stub.asInterface(iBinder);
                Log.d("OpFacelockController", "Connected to FaceSetting service, " + OpFacelockController.this.mSettingService);
                OpFacelockController.this.mBoundToSettingService = true;
                OpFacelockController.this.updateIsFaceAdded();
                OpFacelockController.this.mBindingSetting = false;
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName componentName) {
                Log.e("OpFacelockController", "disconnect from FaceSetting service");
                OpFacelockController.this.mSettingService = null;
                OpFacelockController.this.mBoundToSettingService = false;
                OpFacelockController.this.mUpdateMonitor.setIsFaceAdded(false);
                OpFacelockController.this.mBindingSetting = false;
            }
        };
        this.mOPFacelockCallback = new IOPFacelockCallback.Stub() { // from class: com.oneplus.faceunlock.OpFacelockController.14
            @Override // com.oneplus.faceunlock.internal.IOPFacelockCallback
            public void onBeginRecognize(int i) {
                if (OpFacelockController.this.mFaceLockActive && OpFacelockController.DEBUG) {
                    Log.d("OpFacelockController", "onBeginRecognize");
                }
            }

            @Override // com.oneplus.faceunlock.internal.IOPFacelockCallback
            public void onCompared(int i, int i2, int i3, int i4, int i5) {
                if (i3 == 2 && OpFacelockController.this.mIsScreenOffUnlock) {
                    if (OpFacelockController.DEBUG) {
                        Log.d("OpFacelockController", "onCompared 2 to remove timeout");
                    }
                    OpFacelockController.this.mUIHandler.removeCallbacks(OpFacelockController.this.mResetScreenOnRunnable);
                    OpFacelockController.this.updateKeyguardAlpha(1, true, true);
                }
            }

            @Override // com.oneplus.faceunlock.internal.IOPFacelockCallback
            public void onEndRecognize(int i, int i2, int i3) {
                if (OpFacelockController.this.mFaceLockActive) {
                    if (OpFacelockController.this.mIsScreenOffUnlock && i3 != 0) {
                        OpFacelockController.this.mHandler.removeCallbacks(OpFacelockController.this.mOffAuthenticateRunnable);
                        OpFacelockController.this.mKeyguardViewMediator.notifyScreenOffAuthenticate(false, OpKeyguardViewMediator.AUTHENTICATE_FACEUNLOCK);
                    }
                    OpFacelockController.this.mHandler.removeMessages(8);
                    OpFacelockController.this.mNeedToPendingStopFacelock = false;
                    boolean isUnlockingWithBiometricAllowed = OpFacelockController.this.mUpdateMonitor.isUnlockingWithBiometricAllowed();
                    StringBuilder sb = new StringBuilder();
                    sb.append("onEndRecognize, result:");
                    sb.append(i3);
                    sb.append(", keyguardShow:");
                    sb.append(OpFacelockController.this.mIsKeyguardShowing);
                    sb.append(", bouncer:");
                    sb.append(OpFacelockController.this.mBouncer);
                    sb.append(", allowed:");
                    sb.append(isUnlockingWithBiometricAllowed);
                    sb.append(", isSleep:");
                    sb.append(OpFacelockController.this.mIsSleep);
                    sb.append(", simpin:");
                    sb.append(OpFacelockController.this.mUpdateMonitor.isSimPinSecure());
                    sb.append(", pending:");
                    sb.append(OpFacelockController.this.mPendingLaunchCameraSource != null);
                    sb.append(", auto:");
                    sb.append(OpFacelockController.this.mUpdateMonitor.isAutoFacelockEnabled());
                    Log.d("OpFacelockController", sb.toString());
                    OpFacelockController.this.mKeyguardViewMediator.userActivity();
                    String str = "1";
                    if (i3 == 0) {
                        if (!OpFacelockController.this.mUpdateMonitor.allowShowingLock() || !isUnlockingWithBiometricAllowed || OpFacelockController.this.mIsSleep || OpFacelockController.this.mUpdateMonitor.isSimPinSecure()) {
                            Log.d("OpFacelockController", "not handle recognize");
                            OpFacelockController.this.mHandler.removeMessages(2);
                            OpFacelockController.this.mHandler.sendEmptyMessage(2);
                            if (OpFacelockController.this.mIsScreenOffUnlock) {
                                OpFacelockController.this.mHandler.removeCallbacks(OpFacelockController.this.mOffAuthenticateRunnable);
                                OpFacelockController.this.mKeyguardViewMediator.notifyScreenOffAuthenticate(false, OpKeyguardViewMediator.AUTHENTICATE_FACEUNLOCK);
                                return;
                            }
                            return;
                        }
                        try {
                            if (!(System.currentTimeMillis() - OpFacelockController.this.getFpFailTimeStamp() <= 2000)) {
                                str = "0";
                            }
                            if (OpFacelockController.this.mLightingModeEnabled) {
                                OpMdmLogger.log("lock_unlock_success", "face_bright", str);
                            } else {
                                OpMdmLogger.log("lock_unlock_success", "face", str);
                            }
                            KeyguardUpdateMonitor unused = OpFacelockController.this.mUpdateMonitor;
                            if (OpKeyguardUpdateMonitor.isMotorCameraSupported()) {
                                OpFacelockController.this.mUpdateMonitor.reportFaceUnlock();
                            }
                        } catch (Exception e) {
                            Log.w("OpFacelockController", "Exception e = " + e.toString());
                        }
                        if (!OpFacelockController.this.mUpdateMonitor.isAutoFacelockEnabled() && !OpFacelockController.this.mPhoneStatusBar.isBouncerShowing()) {
                            KeyguardUpdateMonitor unused2 = OpFacelockController.this.mUpdateMonitor;
                            if (!OpKeyguardUpdateMonitor.isMotorCameraSupported()) {
                                if (OpFacelockController.this.mPendingLaunchCameraSource == null || OpFacelockController.this.mUpdateMonitor.isAutoFacelockEnabled()) {
                                    Log.d("OpFacelockController", "onEndRecognize, result ok to skip bouncer");
                                    OpFacelockController.this.mHandler.sendEmptyMessage(7);
                                    if (OpFacelockController.this.mIsScreenOffUnlock) {
                                        OpFacelockController.this.mUIHandler.removeCallbacks(OpFacelockController.this.mResetScreenOnRunnable);
                                        OpFacelockController.this.updateKeyguardAlpha(1, false, false);
                                        OpFacelockController.this.mHandler.removeCallbacks(OpFacelockController.this.mOffAuthenticateRunnable);
                                        OpFacelockController.this.mKeyguardViewMediator.notifyScreenOffAuthenticate(false, OpKeyguardViewMediator.AUTHENTICATE_FACEUNLOCK);
                                        return;
                                    }
                                    return;
                                }
                                Log.d("OpFacelockController", "onEndRecognize, result ok to unlock and camera");
                                OpFacelockController.this.mUIHandler.removeCallbacks(OpFacelockController.this.mResetScreenOnRunnable);
                                OpFacelockController.this.mHandler.sendEmptyMessage(3);
                                return;
                            }
                        }
                        Log.d("OpFacelockController", "onEndRecognize, result ok to unlock");
                        OpFacelockController.this.mUIHandler.removeCallbacks(OpFacelockController.this.mResetScreenOnRunnable);
                        OpFacelockController.this.mHandler.sendEmptyMessage(3);
                    } else if (i3 == 2) {
                        OpMdmLogger.log("lock_unlock_failed", "face_timeout", str);
                        Log.d("OpFacelockController", "onEndRecognize: no face");
                        OpFacelockController.this.mHandler.sendEmptyMessage(5);
                    } else if (i3 == 3) {
                        Log.d("OpFacelockController", "onEndRecognize: camera error");
                        if (OpFacelockController.this.mIsScreenOffUnlock) {
                            OpFacelockController.this.mUIHandler.removeCallbacks(OpFacelockController.this.mResetScreenOnRunnable);
                            OpFacelockController.this.updateKeyguardAlpha(1, false, true);
                        }
                        OpFacelockController.this.mHandler.sendEmptyMessage(10);
                    } else if (i3 == 4) {
                        Log.d("OpFacelockController", "onEndRecognize: no permission");
                        if (OpFacelockController.this.mIsScreenOffUnlock) {
                            OpFacelockController.this.mUIHandler.removeCallbacks(OpFacelockController.this.mResetScreenOnRunnable);
                            OpFacelockController.this.updateKeyguardAlpha(1, false, true);
                        }
                        OpFacelockController.this.mHandler.sendEmptyMessage(11);
                    } else {
                        if (OpFacelockController.this.mLightingModeEnabled) {
                            OpMdmLogger.log("lock_unlock_failed", "face_bright", str);
                        } else {
                            OpMdmLogger.log("lock_unlock_failed", "face", str);
                        }
                        VibratorSceneUtils.doVibrateWithSceneMultipleTimes(OpFacelockController.this.mContext, OpFacelockController.this.mVibrator, 1019, 0, 50, 3);
                        Log.d("OpFacelockController", "onEndRecognize: fail " + (OpFacelockController.this.mFailAttempts + 1) + " times");
                        OpFacelockController.this.mHandler.sendEmptyMessage(4);
                    }
                }
            }
        };
        this.mLightSensorListener = new SensorEventListener() { // from class: com.oneplus.faceunlock.OpFacelockController.15
            @Override // android.hardware.SensorEventListener
            public void onAccuracyChanged(Sensor sensor, int i) {
            }

            @Override // android.hardware.SensorEventListener
            public void onSensorChanged(SensorEvent sensorEvent) {
                float f = sensorEvent.values[0];
                if (OpFacelockController.DEBUG) {
                    Log.d("OpFacelockController", "light sensor: lux:" + f + ", already lighting:" + OpFacelockController.this.mLightingModeEnabled + ", threshold:" + OpFacelockController.this.mLightingModeSensorThreshold);
                }
                if (f <= ((float) OpFacelockController.this.mLightingModeSensorThreshold) && !OpFacelockController.this.mLightingModeEnabled) {
                    OpFacelockController.this.updateFacelockLightMode(true);
                }
            }
        };
        Log.d("OpFacelockController", "new facelock");
        this.mContext = context;
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(context);
        this.mUpdateMonitor = instance;
        instance.registerCallback(this);
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mPhoneStatusBar = statusBar;
        keyguardViewMediator.getViewMediatorCallback();
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        HandlerThread handlerThread = new HandlerThread("FacelockThread");
        this.mFacelockThread = handlerThread;
        handlerThread.start();
        this.mHandler = new FacelockHandler(this.mFacelockThread.getLooper());
        this.mUIHandler = new Handler();
        WindowManagerGlobal.getWindowManagerService();
        SystemSensorManager systemSensorManager = new SystemSensorManager(this.mContext, this.mHandler.getLooper());
        this.mSensorManager = systemSensorManager;
        this.mLightSensor = systemSensorManager.getDefaultSensor(5);
        this.mSensorRate = this.mContext.getResources().getInteger(17694742);
        this.mPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
        this.mLightingModeBrightness = OpFeatures.isSupport(new int[]{235}) ? 200 : ((PowerManager) this.mContext.getSystemService("power")).getMaximumScreenBrightnessSetting();
        this.mFPC = biometricUnlockController;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.oneplus.faceunlock.action.FACE_SETTING_CHANGED");
        context.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        KeyguardStateCallback keyguardStateCallback = new KeyguardStateCallback();
        this.mKeyguardStateCallback = keyguardStateCallback;
        keyguardViewMediator.addStateMonitorCallback(keyguardStateCallback);
        this.mFpm = (FingerprintManager) this.mContext.getSystemService("fingerprint");
        OpCameraAnimateController opCameraAnimateController = new OpCameraAnimateController(this.mContext);
        this.mCameraAnim = opCameraAnimateController;
        opCameraAnimateController.init();
    }

    private class FacelockHandler extends Handler {
        FacelockHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (OpFacelockController.DEBUG) {
                Log.d("OpFacelockController", "handleMessage: what:" + message.what + ", bound:" + OpFacelockController.this.mBoundToService + ", active:" + OpFacelockController.this.mFaceLockActive);
            }
            switch (message.what) {
                case 1:
                    if (OpFacelockController.this.mBoundToService) {
                        OpFacelockController.this.handleStartFacelock();
                        break;
                    } else {
                        return;
                    }
                case 2:
                    if (OpFacelockController.this.mBoundToService) {
                        OpFacelockController.this.updateRecognizedState(0, false);
                        OpFacelockController.this.handleStopFacelock();
                        break;
                    } else {
                        return;
                    }
                case 3:
                    if (OpFacelockController.this.mFaceLockActive && OpFacelockController.this.mBoundToService) {
                        OpFacelockController.this.unlockKeyguard();
                        break;
                    } else {
                        return;
                    }
                    break;
                case 4:
                    if (OpFacelockController.this.mFaceLockActive) {
                        OpFacelockController.this.handleRecognizeFail();
                        break;
                    } else {
                        return;
                    }
                case 5:
                    if (OpFacelockController.this.mFaceLockActive) {
                        OpFacelockController.this.playFacelockIndicationTextAnim();
                        OpFacelockController.this.updateRecognizedState(6, true);
                        OpFacelockController.this.handleStopFacelock();
                        break;
                    } else {
                        return;
                    }
                case 6:
                    OpFacelockController.this.handleResetLockout();
                    break;
                case 7:
                    if (OpFacelockController.this.mFaceLockActive && OpFacelockController.this.mBoundToService) {
                        OpFacelockController.this.handleSkipBouncer();
                        break;
                    } else {
                        return;
                    }
                case 8:
                    OpFacelockController.this.handleResetFacelockPending();
                    break;
                case 9:
                default:
                    Log.e("OpFacelockController", "Unhandled message");
                    break;
                case 10:
                    if (OpFacelockController.this.mFaceLockActive) {
                        OpFacelockController.this.playFacelockIndicationTextAnim();
                        OpFacelockController.this.updateRecognizedState(8, true);
                        OpFacelockController.this.handleStopFacelock();
                        break;
                    } else {
                        return;
                    }
                case 11:
                    if (OpFacelockController.this.mFaceLockActive) {
                        OpFacelockController.this.playFacelockIndicationTextAnim();
                        OpFacelockController.this.updateRecognizedState(9, true);
                        OpFacelockController.this.handleStopFacelock();
                        break;
                    } else {
                        return;
                    }
                case 12:
                    OpFacelockController.this.updateIsFaceAdded();
                    break;
                case 13:
                    if (OpFacelockController.this.mFaceLockActive) {
                        OpFacelockController.this.enterBouncer();
                        OpFacelockController.this.playFacelockIndicationTextAnim();
                        OpFacelockController.this.updateRecognizedState(11, true);
                        OpFacelockController.this.handleStopFacelock();
                        break;
                    } else {
                        return;
                    }
                case 14:
                    OpFacelockController.this.handleFaceUnlockNotice();
                    break;
            }
            if (OpFacelockController.DEBUG) {
                Log.d("OpFacelockController", "handleMessage: done");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceUnlockNotice() {
        KeyguardIndicationController keyguardIndicationController;
        this.mHandler.removeMessages(14);
        if (OpKeyguardUpdateMonitor.isMotorCameraSupported() && this.mIsKeyguardShowing && !this.mBouncer && (keyguardIndicationController = this.mIndicator) != null) {
            boolean isShowingText = keyguardIndicationController.isShowingText();
            Log.d("OpFacelockController", "handleNotice, " + isShowingText);
            if (!isShowingText) {
                this.mUIHandler.post(new Runnable() { // from class: com.oneplus.faceunlock.OpFacelockController.2
                    @Override // java.lang.Runnable
                    public void run() {
                        if (OpFacelockController.this.mPhoneStatusBar != null) {
                            OpFacelockController.this.mPhoneStatusBar.onEmptySpaceClick();
                        }
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRecognizedState(int i, boolean z) {
        if (!this.mLockout) {
            this.mUpdateMonitor.notifyFacelockStateChanged(i);
            updateNotifyMessage(i, z);
            if (this.mUpdateMonitor.isFacelockDisabled()) {
                this.mLockout = true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleResetLockout() {
        this.mLockout = false;
        if (this.mBoundToService && canLaunchFacelock()) {
            updateRecognizedState(5, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSkipBouncer() {
        if (DEBUG) {
            Log.d("OpFacelockController", "handleSkipBouncer");
        }
        this.mFailAttempts = 0;
        this.mMotorQueue.clear();
        updateRecognizedState(2, false);
        handleStopFacelock();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRecognizeFail() {
        boolean z;
        int i = this.mFailAttempts + 1;
        this.mFailAttempts = i;
        int i2 = i % FAILED_ATTEMPTS_TO_TIMEOUT != 0 ? 7 : 1;
        if (this.mFailAttempts >= 3) {
            if (this.mPhoneStatusBar != null) {
                if (DEBUG) {
                    Log.d("OpFacelockController", "enter Bouncer");
                }
                enterBouncer();
            }
            z = false;
        } else {
            z = true;
        }
        if (z) {
            playFacelockIndicationTextAnim();
        }
        updateRecognizedState(i2, true);
        handleStopFacelock();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playFacelockIndicationTextAnim() {
        StatusBar statusBar = this.mPhoneStatusBar;
        if (statusBar != null && !statusBar.isBouncerShowing()) {
            this.mUIHandler.post(new Runnable() { // from class: com.oneplus.faceunlock.OpFacelockController.4
                @Override // java.lang.Runnable
                public void run() {
                    OpFacelockController.this.mPhoneStatusBar.startFacelockFailAnimation();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enterBouncer() {
        Log.d("OpFacelockController", "handle enter Bouncer");
        this.mEnterBouncerAfterScreenOn = false;
        this.mUIHandler.post(new Runnable() { // from class: com.oneplus.faceunlock.OpFacelockController.5
            @Override // java.lang.Runnable
            public void run() {
                OpFacelockController.this.mStatusBarKeyguardViewManager.showBouncer(false);
                if (!OpFacelockController.this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    OpFacelockController.this.mPhoneStatusBar.animateCollapsePanels(0, true);
                }
            }
        });
    }

    private boolean isWakingUpReasonSupported(String str) {
        if ("com.android.systemui:CAMERA_GESTURE_CIRCLE".equals(str)) {
            return false;
        }
        return OpUtils.isFaceUnlockSupportPassiveWakeup() || ("wakeUp".equals(str) && this.mUpdateMonitor.isFacelockWaitingTap() && this.mBouncer) || "android.policy:POWER".equals(str) || "android.policy:DOUBLE_TAP".equals(str);
    }

    public void onPreStartedWakingUp() {
        String str;
        try {
            str = this.mPowerManager.getWakingUpReason();
        } catch (RemoteException e) {
            Log.e("OpFacelockController", "getWakingUpReason," + e.getMessage());
            str = "android.policy:POWER";
        }
        Log.d("OpFacelockController", "onPreStartedWakingUp, bound:" + this.mBoundToService + ", pending:" + this.mPendingFacelockWhenBouncer + ", bouncerRec:" + this.mUpdateMonitor.isBouncerRecognizeEnabled() + ", fp:" + this.mFPC.isWakeAndUnlock() + ", reason:" + str);
        this.mIsSleep = false;
        if (this.mBoundToService && canLaunchFacelock()) {
            if (OpKeyguardUpdateMonitor.isMotorCameraSupported() && (this.mUpdateMonitor.isBouncerRecognizeEnabled() || this.mFPC.isWakeAndUnlock())) {
                return;
            }
            if (!isWakingUpReasonSupported(str)) {
                updateRecognizedState(12, false);
                return;
            }
            if (this.mPendingFacelockWhenBouncer) {
                updateRecognizedState(3, false);
            }
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessage(1);
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onStartedWakingUp() {
        String str;
        try {
            str = this.mPowerManager.getWakingUpReason();
        } catch (RemoteException e) {
            Log.e("OpFacelockController", "getWakingUpReason," + e.getMessage());
            str = "android.policy:POWER";
        }
        Log.d("OpFacelockController", "onStartedWakingUp, bound:" + this.mBoundToService + ", lockout:" + this.mLockout + ", bouncerRec:" + this.mUpdateMonitor.isBouncerRecognizeEnabled() + ", fp:" + this.mFPC.isWakeAndUnlock() + ", reason:" + str + ", type:" + this.mUpdateMonitor.getFacelockRunningType() + ", bouncer: " + this.mBouncer + ", notice:" + this.mUpdateMonitor.getFacelockNoticeEnabled());
        this.mIsSleep = false;
        if (this.mBoundToService && canLaunchFacelock()) {
            if (!OpKeyguardUpdateMonitor.isMotorCameraSupported() || (!this.mUpdateMonitor.isBouncerRecognizeEnabled() && !this.mFPC.isWakeAndUnlock())) {
                if (isWakingUpReasonSupported(str)) {
                    this.mHandler.removeMessages(2);
                    this.mHandler.removeMessages(1);
                    this.mHandler.sendEmptyMessage(1);
                }
            } else if (this.mUpdateMonitor.getFacelockNoticeEnabled()) {
                this.mHandler.removeMessages(14);
                this.mHandler.sendEmptyMessageDelayed(14, (long) mFaceUnlockNoticeDelay);
            }
        }
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onFacelockLightingChanged(boolean z) {
        if (DEBUG) {
            Log.d("OpFacelockController", "onLightChanged " + z);
        }
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onScreenTurningOn() {
        if (DEBUG) {
            Log.d("OpFacelockController", "onScreenTurningOn");
        }
        this.mIsScreenTurningOn = true;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onScreenTurnedOn() {
        if (DEBUG) {
            Log.d("OpFacelockController", "onScreenTurnedOn, " + this.mStartFacelockWhenScreenOn + ", " + this.mIsSleep + ", " + this.mEnterBouncerAfterScreenOn);
        }
        this.mIsScreenTurnedOn = true;
        if (this.mEnterBouncerAfterScreenOn) {
            enterBouncer();
        }
        if (this.mStartFacelockWhenScreenOn) {
            this.mStartFacelockWhenScreenOn = false;
            if (canLaunchFacelock()) {
                this.mIsSleep = false;
                if (this.mBoundToService) {
                    this.mHandler.removeMessages(2);
                    this.mHandler.removeMessages(1);
                    this.mHandler.sendEmptyMessage(1);
                }
            }
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onScreenTurnedOff() {
        if (DEBUG) {
            Log.d("OpFacelockController", "onScreenTurnedOff");
        }
        this.mIsScreenTurnedOn = false;
        this.mIsScreenTurningOn = false;
        this.mEnterBouncerAfterScreenOn = false;
    }

    public void onPreStartedGoingToSleep() {
        if (DEBUG) {
            Log.d("OpFacelockController", "onPreStartedGoingToSleep");
        }
        this.mIsSleep = true;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onStartedGoingToSleep(int i) {
        if (DEBUG) {
            Log.d("OpFacelockController", "onStartedGoingToSleep, " + i + ", bound:" + this.mBoundToService);
        }
        this.mIsGoingToSleep = true;
        this.mStartFacelockWhenScreenOn = false;
        this.mCameraLaunching = false;
        this.mIsSleep = true;
        this.mHandler.removeMessages(14);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(2);
        this.mPendingFacelockWhenBouncer = false;
        this.mSleepTime = SystemClock.uptimeMillis();
        this.mIsScreenOffUnlock = false;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onFinishedGoingToSleep(int i) {
        if (DEBUG) {
            Log.d("OpFacelockController", "onFinishedGoingToSleep, " + i);
        }
        this.mIsGoingToSleep = false;
        this.mLightingModeBrightness = SystemProperties.getInt("persist.sys.facelock.bright", this.mLightingModeBrightness);
        int i2 = SystemProperties.getInt("persist.sys.facelock.lsensor", 0);
        if (i2 > 0) {
            this.mLightingModeSensorThreshold = i2;
        }
        SystemProperties.getInt("persist.sys.facelock.uptimes", 6);
        SystemProperties.getInt("persist.sys.facelock.updura", 18000);
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onDreamingStateChanged(boolean z) {
        if (DEBUG) {
            Log.d("OpFacelockController", "onDreamingStateChanged, " + z);
        }
    }

    private class KeyguardStateCallback extends IKeyguardStateCallback.Stub {
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

        public void onShowingStateChanged(boolean z) {
        }

        public void onTrustedChanged(boolean z) {
        }

        private KeyguardStateCallback() {
        }

        public void onSimSecureStateChanged(boolean z) {
            if (OpFacelockController.this.mSimSecure != z) {
                if (!z && OpFacelockController.this.mBoundToService && OpFacelockController.this.mIsKeyguardShowing && OpFacelockController.this.mUpdateMonitor.isAutoFacelockEnabled()) {
                    Log.d("OpFacelockController", "onSimSecure to start");
                    OpFacelockController.this.tryToStartFaceLock(true);
                }
                OpFacelockController.this.mSimSecure = z;
            }
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onUserSwitchComplete(int i) {
        if (i != 0) {
            stopFacelock();
            return;
        }
        Log.d("OpFacelockController", "user switch to owner");
        tryToStartFaceLock(false);
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onDeviceProvisioned() {
        if (DEBUG) {
            Log.d("OpFacelockController", "onDeviceProvisioned, bound:" + this.mBoundToService);
        }
        if (!this.mBoundToService) {
            bindFacelock();
        }
        this.mHandler.removeMessages(12);
        this.mHandler.sendEmptyMessage(12);
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onStrongAuthStateChanged(int i) {
        if (OpUtils.isCustomFingerprint() && !canLaunchFacelock()) {
            if (this.mUpdateMonitor.isFacelockAvailable() || this.mUpdateMonitor.isFacelockRecognizing()) {
                Log.d("OpFacelockController", "onStrongAuthStateChanged to stop");
                stopFacelock();
            }
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onKeyguardVisibilityChanged(boolean z) {
        if (DEBUG) {
            Log.d("OpFacelockController", "onKeyguardVisibilityChanged, show:" + z + ", bound:" + this.mBoundToService + ", first:" + this.mFirst);
        }
        if (this.mIsKeyguardShowing != z) {
            if (!this.mBoundToService) {
                bindFacelock();
            }
            if (this.mFirst) {
                this.mHandler.removeMessages(12);
                this.mHandler.sendEmptyMessage(12);
            }
            if (!z) {
                this.mHandler.removeMessages(14);
                this.mStartFacelockWhenScreenOn = false;
                this.mCameraLaunching = false;
                this.mNeedToPendingStopFacelock = false;
                this.mHandler.removeMessages(1);
                this.mHandler.sendEmptyMessage(2);
            } else if (!this.mIsKeyguardShowing && this.mBoundToService && canLaunchFacelock()) {
                if (OpUtils.isFaceUnlockSupportPassiveWakeup()) {
                    this.mHandler.removeMessages(2);
                    this.mHandler.removeMessages(1);
                    this.mHandler.sendEmptyMessage(1);
                } else {
                    updateRecognizedState(12, false);
                }
            }
            this.mIsKeyguardShowing = z;
            if (!z) {
                this.mPendingFacelockWhenBouncer = false;
            }
        }
    }

    public boolean tryToStartFaceLockInBouncer() {
        boolean userCanSkipBouncer = this.mUpdateMonitor.getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser());
        Log.d("OpFacelockController", "startInBouncer, bound:" + this.mBoundToService + ", " + canLaunchFacelock() + ", skip:" + userCanSkipBouncer);
        if (!canLaunchFacelock() || userCanSkipBouncer) {
            return false;
        }
        if (this.mBoundToService) {
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessage(1);
        }
        return true;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onKeyguardBouncerChanged(boolean z) {
        if (DEBUG) {
            Log.d("OpFacelockController", "onKeyguardBouncerChanged , bouncer:" + z + ", show:" + this.mIsKeyguardShowing + ", skip:" + this.mUpdateMonitor.canSkipBouncerByFacelock() + ", unlocking:" + this.mUpdateMonitor.isFacelockUnlocking() + ", bouncerRec:" + this.mUpdateMonitor.isBouncerRecognizeEnabled() + ", active: " + this.mFaceLockActive + ", interactive: " + this.mUpdateMonitor.isDeviceInteractive() + ", type:" + this.mUpdateMonitor.getFacelockRunningType());
        }
        this.mBouncer = z;
        if (!OpKeyguardUpdateMonitor.isMotorCameraSupported() || this.mUpdateMonitor.isFacelockUnlocking()) {
            if (this.mIsKeyguardShowing || !z) {
                if (!OpKeyguardUpdateMonitor.isMotorCameraSupported() && !OpUtils.isFaceUnlockSupportPassiveWakeup() && this.mUpdateMonitor.isFacelockWaitingTap()) {
                    tryToStartFaceLock(false);
                } else if (!this.mUpdateMonitor.isAutoFacelockEnabled() && this.mFaceLockActive) {
                    updateRecognizedState(3, false);
                }
                if (this.mIsKeyguardShowing && z) {
                    if (this.mUpdateMonitor.canSkipBouncerByFacelock()) {
                        this.mFPC.startWakeAndUnlockForFace(8);
                    } else if (this.mUpdateMonitor.isFacelockUnlocking()) {
                        Log.d("OpFacelockController", "just keyguardDone");
                        this.mKeyguardViewMediator.keyguardDone();
                    }
                }
            } else {
                tryToStartFaceLock(false);
            }
        } else if (this.mIsKeyguardShowing && z) {
            if (this.mUpdateMonitor.isBouncerRecognizeEnabled()) {
                tryToStartFaceLockInBouncer();
            } else if (this.mUpdateMonitor.getFacelockRunningType() == 0) {
                tryToStartFaceLockInBouncer();
            }
        }
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onClearFailedFacelockAttempts() {
        if (DEBUG) {
            Log.d("OpFacelockController", "onClearFailedFacelockAttempts, failed:" + this.mFailAttempts + ", lockout:" + this.mLockout);
        }
        this.mFailAttempts = 0;
        this.mLockout = false;
        this.mMotorQueue.clear();
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onSystemReady() {
        if (DEBUG) {
            Log.d("OpFacelockController", "onSystemReady");
        }
        bindFacelock();
        bindFacelockSetting();
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
    public void onPasswordLockout() {
        if (DEBUG) {
            Log.d("OpFacelockController", "onPasswordLockout");
        }
        stopFacelock();
    }

    public boolean tryToStartFaceLock(boolean z) {
        Log.d("OpFacelockController", "tryToStartFaceLock, bound:" + this.mBoundToService + ", motor:" + z);
        if ((OpKeyguardUpdateMonitor.isMotorCameraSupported() && !z) || !canLaunchFacelock()) {
            return false;
        }
        if (this.mBoundToService) {
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessage(1);
        }
        return true;
    }

    public void tryToStartFaceLockAfterScreenOn() {
        if (DEBUG) {
            Log.d("OpFacelockController", "tryToStartFaceLockAfterScreenOn," + this.mUpdateMonitor.isBouncerRecognizeEnabled());
        }
        if (!OpUtils.isCustomFingerprint()) {
            this.mStartFacelockWhenScreenOn = true;
        } else if (this.mBoundToService && !OpKeyguardUpdateMonitor.isMotorCameraSupported()) {
            this.mPendingFacelockWhenBouncer = true;
            this.mHandler.postDelayed(new Runnable() { // from class: com.oneplus.faceunlock.OpFacelockController.6
                @Override // java.lang.Runnable
                public void run() {
                    OpFacelockController.this.mPendingFacelockWhenBouncer = false;
                    if (!OpFacelockController.this.tryToStartFaceLock(true)) {
                        OpFacelockController.this.stopFacelock();
                    }
                }
            }, 500);
        }
    }

    public boolean canLaunchFacelock() {
        if (this.mCameraLaunching) {
            Log.d("OpFacelockController", "not start when camera launching");
            return false;
        } else if (!this.mUpdateMonitor.isFacelockAllowed()) {
            if (DEBUG) {
                Log.d("OpFacelockController", "not allow to facelock");
            }
            return false;
        } else if (!isFacelockTimeout()) {
            return true;
        } else {
            Log.d("OpFacelockController", "timeout, not allow to facelock");
            return false;
        }
    }

    public boolean isFacelockRunning() {
        return this.mFaceLockActive;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateIsFaceAdded() {
        int i;
        Log.d("OpFacelockController", "updateIsFaceAdded, " + this.mSettingService);
        IOPFaceSettingService iOPFaceSettingService = this.mSettingService;
        if (iOPFaceSettingService == null) {
            this.mUpdateMonitor.setIsFaceAdded(false);
            bindFacelockSetting();
            return;
        }
        boolean z = true;
        try {
            i = iOPFaceSettingService.checkState(0);
        } catch (Exception e) {
            Log.d("OpFacelockController", "updateIsFaceAdded fail: " + e.getMessage());
            i = 1;
        }
        boolean isFaceAdded = this.mUpdateMonitor.isFaceAdded();
        if (i != 0) {
            z = false;
        }
        if (DEBUG) {
            Log.d("OpFacelockController", "isFaceAdded:" + z + ", pre:" + isFaceAdded);
        }
        if (!this.mUpdateMonitor.isFaceAdded() && z && !this.mUpdateMonitor.isUnlockingWithBiometricAllowed() && this.mStatusBarKeyguardViewManager.getBouncer() != null) {
            this.mUpdateMonitor.setIsFaceAdded(z);
            this.mStatusBarKeyguardViewManager.getBouncer().updateBouncerPromptReason();
            Log.d("OpFacelockController", "face is added and not allowed, update Prompt reason");
        }
        this.mUpdateMonitor.setIsFaceAdded(z);
        if (z != isFaceAdded) {
            if (z) {
                bindFacelock();
                if (this.mBoundToService) {
                    tryToStartFaceLock(false);
                }
            } else {
                stopFacelock();
            }
        }
        if (!z) {
            unbindFacelockSetting();
        }
        this.mFirst = false;
    }

    public boolean notifyCameraLaunching(boolean z, String str, boolean z2) {
        boolean z3 = false;
        if (!OpUtils.isSupportDoubleTapAlexa() || !z || !z2 || this.mPhoneStatusBar.isDoubleTapCamera()) {
            if (this.mIsKeyguardShowing) {
                this.mCameraLaunching = z;
            }
            Log.d("OpFacelockController", "notifyCameraLaunching, source:" + str + ", facelockActive:" + this.mFaceLockActive + ", keyguard:" + this.mIsKeyguardShowing + ", isDoubleTap:" + z2);
            if (this.mFaceLockActive) {
                if (str != null) {
                    this.mPendingLaunchCameraSource = str;
                    z3 = true;
                }
                stopFacelock();
            }
            return z3;
        }
        Log.d("OpFacelockController", "return notifyCameraLaunching");
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStartFacelock() {
        this.mHandler.removeMessages(14);
        boolean isCameraErrorState = this.mUpdateMonitor.isCameraErrorState();
        Log.d("OpFacelockController", "handle startFacelock, active:" + this.mFaceLockActive + ", pendingStop:" + this.mPendingStopFacelock + ", live wp:" + this.mStatusBarKeyguardViewManager.isShowingLiveWallpaper(false) + ", cameraError:" + isCameraErrorState + ", showing:" + this.mIsKeyguardShowing + ", pending:" + this.mPendingFacelockWhenBouncer + ", intOn:" + this.mIsScreenTurningOn + ", on:" + this.mIsScreenTurnedOn);
        if (this.mService == null) {
            Log.d("OpFacelockController", "not start Facelock");
        } else if (isCameraErrorState) {
            Log.d("OpFacelockController", "not start when camera error");
        } else if (this.mPendingFacelockWhenBouncer) {
            Log.d("OpFacelockController", "pending in bouncer");
        } else if (this.mFaceLockActive) {
            this.mPendingStopFacelock = false;
            updateRecognizedState(3, false);
        } else if (!this.mIsScreenTurnedOn && this.mKeyguardViewMediator.isScreenOffAuthenticating()) {
            this.mStartFacelockWhenScreenOn = true;
            Log.d("OpFacelockController", "pending start to screen on");
        } else if (!OpKeyguardUpdateMonitor.isMotorCameraSupported() || upMotorBySystemApp() != -999) {
            this.mStartFacelockWhenScreenOn = false;
            updateRecognizedState(3, false);
            this.mFaceLockActive = true;
            this.mNeedToPendingStopFacelock = true;
            if (!OpKeyguardUpdateMonitor.isMotorCameraSupported() && !this.mIsScreenTurningOn && !this.mIsScreenTurnedOn && !this.mKeyguardViewMediator.isScreenOffAuthenticating() && this.mIsKeyguardShowing && this.mUpdateMonitor.isAutoFacelockEnabled()) {
                this.mIsScreenOffUnlock = true;
                updateKeyguardAlpha(0, true, false);
                this.mUIHandler.removeCallbacks(this.mResetScreenOnRunnable);
                this.mUIHandler.postDelayed(this.mResetScreenOnRunnable, 600);
            }
            synchronized (this) {
                try {
                    this.mService.registerCallback(this.mOPFacelockCallback);
                    this.mService.prepare();
                    this.mService.startFaceUnlock(0);
                } catch (RemoteException e) {
                    Log.e("OpFacelockController", "startFacelock fail, " + e.getMessage());
                    this.mNeedToPendingStopFacelock = false;
                    this.mHandler.sendEmptyMessage(4);
                    return;
                } catch (NullPointerException e2) {
                    Log.e("OpFacelockController", "startFacelock mService null, " + e2.getMessage());
                    this.mNeedToPendingStopFacelock = false;
                    this.mHandler.sendEmptyMessage(4);
                    return;
                }
            }
            this.mHandler.removeMessages(8);
            this.mHandler.sendEmptyMessageDelayed(8, 500);
            registerLightSensor(true);
        } else {
            if (!this.mIsScreenTurnedOn) {
                this.mEnterBouncerAfterScreenOn = true;
            } else {
                enterBouncer();
            }
            updateRecognizedState(10, true);
            Log.d("OpFacelockController", "not start motor for up limited");
            this.mMotorQueue.clear();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleResetFacelockPending() {
        this.mNeedToPendingStopFacelock = false;
        if (DEBUG) {
            Log.d("OpFacelockController", "handleResetFacelockPending, " + this.mPendingStopFacelock);
        }
        if (this.mPendingStopFacelock) {
            handleStopFacelock();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopFacelock() {
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessage(2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStopFacelock() {
        if (!this.mFaceLockActive) {
            Log.d("OpFacelockController", "not stop facelock, active:" + this.mFaceLockActive);
        } else if (this.mNeedToPendingStopFacelock) {
            this.mPendingStopFacelock = true;
            if (DEBUG) {
                Log.d("OpFacelockController", "pending stop facelock");
            }
        } else {
            Log.d("OpFacelockController", "handle stopFacelock, pending camera:" + this.mPendingLaunchCameraSource);
            this.mHandler.removeMessages(8);
            this.mPendingStopFacelock = false;
            this.mFaceLockActive = false;
            stopFacelockLightMode();
            if (OpKeyguardUpdateMonitor.isMotorCameraSupported()) {
                downMotorBySystemApp();
            }
            synchronized (this) {
                try {
                    this.mService.unregisterCallback(this.mOPFacelockCallback);
                    this.mService.stopFaceUnlock(0);
                    this.mService.release();
                } catch (RemoteException e) {
                    Log.e("OpFacelockController", "stopFacelock fail, " + e.getMessage());
                } catch (NullPointerException e2) {
                    Log.e("OpFacelockController", "stopFacelock mService null, " + e2.getMessage());
                }
            }
            final String str = this.mPendingLaunchCameraSource;
            if (str != null) {
                this.mUIHandler.post(new Runnable() { // from class: com.oneplus.faceunlock.OpFacelockController.7
                    @Override // java.lang.Runnable
                    public void run() {
                        OpFacelockController.this.launchCamera(str);
                    }
                });
                this.mPendingLaunchCameraSource = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateKeyguardAlpha(final int i, final boolean z, final boolean z2) {
        final boolean isShowingLiveWallpaper = this.mStatusBarKeyguardViewManager.isShowingLiveWallpaper(false);
        Log.d("OpFacelockController", "update alpha:" + i + ", " + this.mIsScreenOffUnlock + ", live wp:" + isShowingLiveWallpaper + ", " + z2);
        if (i == 0 && z) {
            this.mHandler.removeCallbacks(this.mOffAuthenticateRunnable);
            this.mKeyguardViewMediator.notifyScreenOffAuthenticate(true, OpKeyguardViewMediator.AUTHENTICATE_FACEUNLOCK, 1);
        }
        this.mUIHandler.post(new Runnable() { // from class: com.oneplus.faceunlock.OpFacelockController.8
            @Override // java.lang.Runnable
            public void run() {
                if (!isShowingLiveWallpaper) {
                    OpFacelockController.this.mKeyguardViewMediator.changePanelAlpha(i, OpKeyguardViewMediator.AUTHENTICATE_FACEUNLOCK);
                    OpFacelockController.this.mStatusBarKeyguardViewManager.getViewRootImpl().setReportNextDraw();
                    if (i == 1 && z) {
                        OpFacelockController.this.mHandler.removeCallbacks(OpFacelockController.this.mOffAuthenticateRunnable);
                        if (OpUtils.isCustomFingerprint() || z2) {
                            OpFacelockController.this.mOffAuthenticateRunnable.run();
                        }
                    }
                }
            }
        });
        if (i == 1) {
            this.mIsScreenOffUnlock = false;
            if (z) {
                int i2 = SystemClock.uptimeMillis() - this.mSleepTime > 5000 ? 10 : 100;
                if (!isShowingLiveWallpaper) {
                    i2 = 300;
                }
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("OpFacelockController", "updateKeyguardAlpha: delay= " + i2);
                }
                this.mHandler.removeCallbacks(this.mOffAuthenticateRunnable);
                if (OpUtils.isCustomFingerprint() || z2) {
                    this.mHandler.postDelayed(this.mOffAuthenticateRunnable, (long) i2);
                } else {
                    this.mKeyguardViewMediator.notifyScreenOffAuthenticate(false, OpKeyguardViewMediator.AUTHENTICATE_FACEUNLOCK);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unlockKeyguard() {
        final boolean isShowingLiveWallpaper = this.mStatusBarKeyguardViewManager.isShowingLiveWallpaper(false);
        final boolean isBouncerShowing = this.mPhoneStatusBar.isBouncerShowing();
        boolean isDeviceInteractive = this.mUpdateMonitor.isDeviceInteractive();
        Log.d("OpFacelockController", "unlockKeyguard, bouncer:" + isBouncerShowing + ", live wp:" + isShowingLiveWallpaper + ", interactive = " + isDeviceInteractive + ", offUnlock:" + this.mIsScreenOffUnlock);
        this.mFailAttempts = 0;
        this.mMotorQueue.clear();
        OpUtils.setRecentUnlockBiometricFace(true);
        this.mUpdateMonitor.hideFODDim();
        this.mUpdateMonitor.onFacelockUnlocking(true);
        this.mUpdateMonitor.notifyFacelockStateChanged(4);
        this.mUIHandler.post(new Runnable() { // from class: com.oneplus.faceunlock.OpFacelockController.10
            @Override // java.lang.Runnable
            public void run() {
                int i = 5;
                if (OpFacelockController.this.mIsScreenOffUnlock && !isShowingLiveWallpaper) {
                    i = 1;
                } else if (isBouncerShowing) {
                    i = 8;
                } else {
                    KeyguardUpdateMonitor unused = OpFacelockController.this.mUpdateMonitor;
                    if (!OpKeyguardUpdateMonitor.isMotorCameraSupported() && !isShowingLiveWallpaper && OpFacelockController.this.mUpdateMonitor.isDeviceInteractive()) {
                        OpFacelockController.this.mKeyguardViewMediator.onWakeAndUnlocking();
                        i = 0;
                    }
                }
                OpFacelockController.this.resetFPTimeout();
                OpFacelockController.this.mFPC.startWakeAndUnlockForFace(i);
            }
        });
        this.mHandler.removeCallbacks(this.mOffAuthenticateRunnable);
        this.mKeyguardViewMediator.notifyScreenOffAuthenticate(false, OpKeyguardViewMediator.AUTHENTICATE_FACEUNLOCK, 2);
        this.mUpdateMonitor.notifyFacelockStateChanged(0);
        stopFacelock();
    }

    public boolean isFacelockTimeout() {
        return this.mLockout || !this.mUpdateMonitor.isUnlockingWithBiometricAllowed();
    }

    private void bindFacelock() {
        if (!this.mBinding) {
            if (!this.mUpdateMonitor.isFaceAdded()) {
                Log.d("OpFacelockController", "no face added");
                return;
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.oneplus.faceunlock", "com.oneplus.faceunlock.FaceUnlockService"));
            try {
                if (this.mContext.bindServiceAsUser(intent, this.mConnection, 1, UserHandle.OWNER)) {
                    Log.d("OpFacelockController", "Binding ok");
                    this.mBinding = true;
                    return;
                }
                Log.d("OpFacelockController", "Binding fail");
            } catch (Exception e) {
                Log.e("OpFacelockController", "bindFacelock fail, " + e.getMessage());
            }
        }
    }

    private void bindFacelockSetting() {
        if (this.mBindingSetting) {
            Log.d("OpFacelockController", "return Binding");
            return;
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.oneplus.faceunlock", "com.oneplus.faceunlock.FaceSettingService"));
        try {
            if (this.mContext.bindServiceAsUser(intent, this.mSettingConnection, 1, UserHandle.OWNER)) {
                Log.d("OpFacelockController", "Binding setting ok");
                this.mBindingSetting = true;
                return;
            }
            Log.d("OpFacelockController", "Binding setting fail");
        } catch (Exception e) {
            Log.e("OpFacelockController", "bind setting fail, " + e.getMessage());
        }
    }

    private void unbindFacelockSetting() {
        try {
            Log.d("OpFacelockController", "unbindFacelockSetting, " + this.mBoundToSettingService);
            if (this.mBoundToSettingService) {
                this.mContext.unbindService(this.mSettingConnection);
                this.mBindingSetting = false;
                this.mSettingService = null;
                this.mBoundToSettingService = false;
            }
        } catch (Exception e) {
            Log.e("OpFacelockController", "unbind face setting fail, " + e.getMessage());
        }
        try {
            Log.d("OpFacelockController", "unbind facelock, " + this.mBoundToService);
            if (this.mBoundToService) {
                this.mContext.unbindService(this.mConnection);
                this.mService = null;
                this.mBinding = false;
                this.mBoundToService = false;
            }
        } catch (Exception e2) {
            Log.e("OpFacelockController", "unbind facelock fail, " + e2.getMessage());
        }
    }

    private void updateNotifyMessage(final int i, final boolean z) {
        final int facelockNotifyMsgId = this.mUpdateMonitor.getFacelockNotifyMsgId(i);
        this.mUIHandler.post(new Runnable() { // from class: com.oneplus.faceunlock.OpFacelockController.11
            @Override // java.lang.Runnable
            public void run() {
                if (OpFacelockController.this.mIndicator != null) {
                    if (OpFacelockController.this.mIndicator.getLockscreenLockIconController() != null) {
                        if (!OpFacelockController.this.mIsGoingToSleep || i != 0) {
                            OpFacelockController.this.mIndicator.getLockscreenLockIconController().setFacelockRunning(i, true);
                        } else {
                            OpFacelockController.this.mIndicator.getLockscreenLockIconController().setFacelockRunning(i, false);
                        }
                    }
                    int i2 = i;
                    if (i2 == 3) {
                        OpFacelockController.this.mIndicator.showTransientIndication(" ", z, true);
                    } else if (i2 == 2) {
                        OpFacelockController.this.mIndicator.showTransientIndication((CharSequence) null);
                    } else if (facelockNotifyMsgId <= 0) {
                    } else {
                        if (!OpUtils.isCustomFingerprint()) {
                            OpFacelockController.this.mIndicator.showTransientIndication(OpFacelockController.this.mContext.getString(facelockNotifyMsgId), z, true);
                        } else if (OpFacelockController.this.mUpdateMonitor.isFacelockAvailable()) {
                            OpFacelockController.this.mIndicator.showTransientIndication((CharSequence) null);
                        } else {
                            OpFacelockController.this.mIndicator.showTransientIndication(OpFacelockController.this.mContext.getString(facelockNotifyMsgId), z, true);
                        }
                    }
                }
            }
        });
    }

    private void registerLightSensor(boolean z) {
        if (this.mUpdateMonitor.isFacelockLightingEnabled()) {
            if (z) {
                this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, this.mSensorRate * 1000, this.mHandler);
            } else {
                this.mSensorManager.unregisterListener(this.mLightSensorListener);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFacelockLightMode(boolean z) {
        if (z) {
            try {
                this.mPowerManager.overrideScreenBrightnessRangeMinimum(this.mLightingModeBrightness);
            } catch (RemoteException e) {
                Log.e("OpFacelockController", "updateFacelockLightMode, overrideScreenBrightness:" + e.getMessage());
            }
        } else {
            this.mPowerManager.overrideScreenBrightnessRangeMinimum(0);
        }
        this.mLightingModeEnabled = z;
        if (z && this.mIsKeyguardShowing) {
            registerLightSensor(false);
        }
    }

    public void stopFacelockLightMode() {
        registerLightSensor(false);
        updateFacelockLightMode(false);
    }

    public void setKeyguardIndicationController(KeyguardIndicationController keyguardIndicationController) {
        this.mIndicator = keyguardIndicationController;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void launchCamera(String str) {
        StatusBar statusBar = this.mPhoneStatusBar;
        if (statusBar != null) {
            statusBar.getKeyguardBottomAreaView().launchCamera(str);
        }
    }

    public boolean isScreenOffUnlock() {
        return this.mIsScreenOffUnlock;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("  OpFacelockController: \n");
        printWriter.print("  mFailAttempts: " + this.mFailAttempts);
        printWriter.print("  mLockout: " + this.mLockout);
        printWriter.print("  mBinding: " + this.mBinding);
        printWriter.print("  mCameraLaunching: " + this.mCameraLaunching);
        printWriter.print("  mBoundToService: " + this.mBoundToService);
        printWriter.print("  mBoundToSettingService: " + this.mBoundToSettingService);
        printWriter.print("  mFaceLockActive: " + this.mFaceLockActive);
        printWriter.print("  mService: " + this.mService);
        printWriter.print("  mFirst: " + this.mFirst);
        printWriter.print("  isFacelockEnabled: " + this.mUpdateMonitor.isFacelockEnabled());
        printWriter.print("  isAutoFacelockEnabled: " + this.mUpdateMonitor.isAutoFacelockEnabled());
        printWriter.print("  isFacelockLightingEnabled: " + this.mUpdateMonitor.isFacelockLightingEnabled());
        printWriter.print("  FacelockRunningType: " + this.mUpdateMonitor.getFacelockRunningType());
        printWriter.print("  isFacelockTimeout: " + isFacelockTimeout());
        printWriter.print("  isFacelockAllowed: " + this.mUpdateMonitor.isFacelockAllowed());
        printWriter.print("  mIsKeyguardShowing: " + this.mIsKeyguardShowing);
        printWriter.print("  mBouncer: " + this.mBouncer);
        printWriter.print("  mIsScreenTurnedOn: " + this.mIsScreenTurnedOn);
        printWriter.print("  mNeedToPendingStopFacelock: " + this.mNeedToPendingStopFacelock);
        printWriter.print("  mPendingStopFacelock: " + this.mPendingStopFacelock);
        printWriter.print("  mPendingLaunchCameraSource: " + this.mPendingLaunchCameraSource);
        printWriter.print("  mIsScreenOffUnlock: " + this.mIsScreenOffUnlock);
        printWriter.print("  mStartFacelockWhenScreenOn: " + this.mStartFacelockWhenScreenOn);
        printWriter.print("  mIsSleep: " + this.mIsSleep);
        printWriter.print("  mLightingModeEnabled: " + this.mLightingModeEnabled);
        printWriter.print("  mLightingModeSensorThreshold: " + this.mLightingModeSensorThreshold);
        printWriter.print("  mLightingModeBrightness: " + this.mLightingModeBrightness);
        printWriter.print("  FAILED_ATTEMPTS_TO_TIMEOUT: " + FAILED_ATTEMPTS_TO_TIMEOUT);
        printWriter.print("  mMotorQueue: " + Arrays.toString(this.mMotorQueue.toArray()));
        printWriter.print("  passive: " + OpUtils.isFaceUnlockSupportPassiveWakeup());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetFPTimeout() {
        BiometricManager biometricManager;
        if (this.mFpm != null && (biometricManager = (BiometricManager) this.mContext.getSystemService(BiometricManager.class)) != null) {
            biometricManager.resetLockout(null);
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
        super.onBiometricAuthFailed(biometricSourceType);
        this.mFpFailTimeStamp = System.currentTimeMillis();
    }

    public long getFpFailTimeStamp() {
        return this.mFpFailTimeStamp;
    }

    public void onFacelockEnableChanged(boolean z) {
        Log.i("OpFacelockController", " onFacelockEnableChanged:" + z);
        if (!z) {
            this.mIsScreenOffUnlock = false;
        }
    }
}
