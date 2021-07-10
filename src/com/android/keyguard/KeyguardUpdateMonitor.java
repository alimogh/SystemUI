package com.android.keyguard;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.UserSwitchObserver;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricSourceType;
import android.hardware.biometrics.CryptoObject;
import android.hardware.biometrics.IBiometricEnabledOnKeyguardCallback;
import android.hardware.face.FaceManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.OpFeatures;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import androidx.lifecycle.Observer;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.settingslib.WirelessUtils;
import com.android.systemui.C0015R$string;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.util.Assert;
import com.android.systemui.util.RingerModeTracker;
import com.google.android.collect.Lists;
import com.oneplus.battery.OpBatteryStatus;
import com.oneplus.keyguard.OpKeyguardUpdateMonitor;
import com.oneplus.systemui.biometrics.OpFodBurnInProtectionHelper;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
public class KeyguardUpdateMonitor extends OpKeyguardUpdateMonitor implements TrustManager.TrustListener, Dumpable {
    public static final boolean CORE_APPS_ONLY;
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_FACE = Build.IS_DEBUGGABLE;
    private static final ComponentName FALLBACK_HOME_COMPONENT = new ComponentName("com.android.settings", "com.android.settings.FallbackHome");
    private static int sCurrentUser;
    private int mActiveMobileDataSubscription = -1;
    private boolean mAssistantVisible;
    private boolean mAuthInterruptActive;
    private final Executor mBackgroundExecutor;
    private OpBatteryStatus mBatteryStatus;
    private IBiometricEnabledOnKeyguardCallback mBiometricEnabledCallback = new IBiometricEnabledOnKeyguardCallback.Stub() { // from class: com.android.keyguard.KeyguardUpdateMonitor.3
        public void onChanged(BiometricSourceType biometricSourceType, boolean z, int i) throws RemoteException {
            if (biometricSourceType == BiometricSourceType.FACE) {
                KeyguardUpdateMonitor.this.mFaceSettingEnabledForUser.put(i, z);
                KeyguardUpdateMonitor.this.mHandler.post(KeyguardUpdateMonitor.this.mUpdateFaceListeningState);
            }
        }
    };
    private BiometricManager mBiometricManager;
    private boolean mBouncer;
    @VisibleForTesting
    protected final BroadcastReceiver mBroadcastAllReceiver = new BroadcastReceiver() { // from class: com.android.keyguard.KeyguardUpdateMonitor.10
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.app.action.NEXT_ALARM_CLOCK_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(301);
            } else if ("android.intent.action.USER_INFO_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(317, intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId()), 0));
            } else if ("com.android.facelock.FACE_UNLOCK_STARTED".equals(action)) {
                Trace.beginSection("KeyguardUpdateMonitor.mBroadcastAllReceiver#onReceive ACTION_FACE_UNLOCK_STARTED");
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(327, 1, getSendingUserId()));
                Trace.endSection();
            } else if ("com.android.facelock.FACE_UNLOCK_STOPPED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(327, 0, getSendingUserId()));
            } else if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(309, Integer.valueOf(getSendingUserId())));
            } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(334, getSendingUserId(), 0));
            } else if ("android.intent.action.USER_STOPPED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(340, intent.getIntExtra("android.intent.extra.user_handle", -1), 0));
            } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(341, intent.getIntExtra("android.intent.extra.user_handle", -1), 0));
            } else if ("android.intent.action.TIME_SET".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(301);
            }
        }
    };
    private final BroadcastDispatcher mBroadcastDispatcher;
    @VisibleForTesting
    protected final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.keyguard.KeyguardUpdateMonitor.9
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            boolean z;
            boolean z2;
            boolean z3;
            String action = intent.getAction();
            if (KeyguardUpdateMonitor.DEBUG) {
                Log.d("KeyguardUpdateMonitor", "received broadcast " + action);
            }
            if ("android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(339, intent.getStringExtra("time-zone")));
            } else if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                int intExtra = intent.getIntExtra("fastcharge_status", 0);
                boolean z4 = intent.getBooleanExtra("pd_charge", false) && intent.getIntExtra("plugged", 0) == 1;
                int i = OpUtils.SUPPORT_WARP_CHARGING ? intExtra : intExtra > 0 ? 1 : 0;
                if (KeyguardUpdateMonitor.DEBUG) {
                    Log.d("KeyguardUpdateMonitor", "pdCharge " + z4 + ", chargingstatus " + intExtra + ", fastcharge:" + i);
                }
                if (OpFeatures.isSupport(new int[]{237})) {
                    boolean booleanExtra = intent.getBooleanExtra("wireless_fastcharge_type", false);
                    boolean booleanExtra2 = intent.getBooleanExtra("wireless_status", false);
                    boolean booleanExtra3 = intent.getBooleanExtra("deviated_wireless_charge", false);
                    if (KeyguardUpdateMonitor.DEBUG) {
                        Log.d("KeyguardUpdateMonitor", "wirelessWarpCharging " + booleanExtra + ", wirelessCharging " + booleanExtra2 + ", wirelessChargingDeviated:" + booleanExtra3);
                    }
                    z2 = booleanExtra;
                    z3 = booleanExtra2;
                    z = booleanExtra3;
                } else {
                    z3 = false;
                    z2 = false;
                    z = false;
                }
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(302, new OpBatteryStatus(intent, i, z3, z2, z, z4)));
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action)) {
                SimData fromIntent = SimData.fromIntent(intent);
                if (!intent.getBooleanExtra("rebroadcastOnUnlock", false)) {
                    Log.v("KeyguardUpdateMonitor", "action " + action + " state: " + intent.getStringExtra("ss") + " slotId: " + fromIntent.slotId + " subid: " + fromIntent.subId);
                    KeyguardUpdateMonitor.this.mHandler.obtainMessage(304, fromIntent.subId, fromIntent.slotId, Integer.valueOf(fromIntent.simState)).sendToTarget();
                } else if (fromIntent.simState == 1) {
                    KeyguardUpdateMonitor.this.mHandler.obtainMessage(338, Boolean.TRUE).sendToTarget();
                }
            } else if ("android.intent.action.PHONE_STATE".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(306, intent.getStringExtra("state")));
            } else if ("android.intent.action.AIRPLANE_MODE".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(329);
            } else if ("android.intent.action.SERVICE_STATE".equals(action)) {
                ServiceState newFromBundle = ServiceState.newFromBundle(intent.getExtras());
                int intExtra2 = intent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1);
                Log.v("KeyguardUpdateMonitor", "action " + action + " serviceState=" + newFromBundle + " subId=" + intExtra2);
                KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(330, intExtra2, 0, newFromBundle));
            } else if ("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(328);
            } else if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action)) {
                KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(337);
            } else if ("android.intent.action.ONEPLUS_CHARGE_TIME_ENABLE".equals(action)) {
                Log.d("KeyguardUpdateMonitor", "Reflash battery info by op intent");
                KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.this;
                keyguardUpdateMonitor.mFocusUpdateBatteryInfo = KeyguardUpdateMonitor.DEBUG;
                KeyguardUpdateMonitor.this.mHandler.sendMessage(keyguardUpdateMonitor.mHandler.obtainMessage(302, KeyguardUpdateMonitor.this.mBatteryStatus));
            } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                KeyguardUpdateMonitor.this.dispatchBootCompleted();
            }
        }
    };
    private final ArrayList<WeakReference<KeyguardUpdateMonitorCallback>> mCallbacks = Lists.newArrayList();
    private final Runnable mCancelNotReceived = new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor.1
        @Override // java.lang.Runnable
        public void run() {
            Log.w("KeyguardUpdateMonitor", "Cancel not received, transitioning to STOPPED");
            KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.this;
            keyguardUpdateMonitor.mFaceRunningState = 0;
            keyguardUpdateMonitor.mFingerprintRunningState = 0;
            KeyguardUpdateMonitor.this.updateBiometricListeningState();
        }
    };
    private final Context mContext;
    private boolean mCredentialAttempted;
    private boolean mDeviceInteractive;
    private final DevicePolicyManager mDevicePolicyManager;
    private boolean mDeviceProvisioned;
    private ContentObserver mDeviceProvisionedObserver;
    private final IDreamManager mDreamManager;
    @VisibleForTesting
    FaceManager.AuthenticationCallback mFaceAuthenticationCallback = new FaceManager.AuthenticationCallback() { // from class: com.android.keyguard.KeyguardUpdateMonitor.14
        public void onAuthenticationFailed() {
            KeyguardUpdateMonitor.this.handleFaceAuthFailed();
        }

        public void onAuthenticationSucceeded(FaceManager.AuthenticationResult authenticationResult) {
            Trace.beginSection("KeyguardUpdateMonitor#onAuthenticationSucceeded");
            KeyguardUpdateMonitor.this.handleFaceAuthenticated(authenticationResult.getUserId(), authenticationResult.isStrongBiometric());
            Trace.endSection();
        }

        public void onAuthenticationHelp(int i, CharSequence charSequence) {
            KeyguardUpdateMonitor.this.handleFaceHelp(i, charSequence.toString());
        }

        public void onAuthenticationError(int i, CharSequence charSequence) {
            KeyguardUpdateMonitor.this.handleFaceError(i, charSequence.toString());
        }

        public void onAuthenticationAcquired(int i) {
            KeyguardUpdateMonitor.this.handleFaceAcquired(i);
        }
    };
    private CancellationSignal mFaceCancelSignal;
    private ArrayDeque<KeyguardFaceListenModel> mFaceListenModels;
    private final FaceManager.LockoutResetCallback mFaceLockoutResetCallback = new FaceManager.LockoutResetCallback() { // from class: com.android.keyguard.KeyguardUpdateMonitor.12
        public void onLockoutReset() {
            KeyguardUpdateMonitor.this.handleFaceLockoutReset();
        }
    };
    private FaceManager mFaceManager;
    private int mFaceRunningState = 0;
    private SparseBooleanArray mFaceSettingEnabledForUser = new SparseBooleanArray();
    private FingerprintManager.AuthenticationCallback mFingerprintAuthenticationCallback = new FingerprintManager.AuthenticationCallback() { // from class: com.android.keyguard.KeyguardUpdateMonitor.13
        @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
        public void onAuthenticationFailed() {
            KeyguardUpdateMonitor.this.mHandler.removeMessages(501);
            if (((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mDuringAcquired) {
                ((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mDuringAcquired = false;
                KeyguardUpdateMonitor.this.updateFingerprintListeningState();
            }
            KeyguardUpdateMonitor.this.handleFingerprintAuthFailed();
        }

        @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult authenticationResult) {
            Trace.beginSection("KeyguardUpdateMonitor#onAuthenticationSucceeded");
            Log.d("KeyguardUpdateMonitor", "received onAuthenticationSucceeded");
            KeyguardUpdateMonitor.this.mHandler.removeMessages(501);
            if (((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mDuringAcquired) {
                ((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mDuringAcquired = false;
            }
            if (!KeyguardUpdateMonitor.this.isDeviceInteractive()) {
                if (OpKeyguardUpdateMonitor.IS_SUPPORT_CUSTOM_FINGERPRINT) {
                    ((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mAodFpAuthenticatedTime = System.currentTimeMillis();
                }
                KeyguardUpdateMonitor.this.notifyWallpaperAodUnlock();
            }
            KeyguardUpdateMonitor.this.handleFingerprintAuthenticated(authenticationResult.getUserId(), authenticationResult.isStrongBiometric());
            Trace.endSection();
        }

        @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
        public void onAuthenticationHelp(int i, CharSequence charSequence) {
            KeyguardUpdateMonitor.this.mHandler.removeMessages(501);
            if (((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mDuringAcquired) {
                ((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mDuringAcquired = false;
                KeyguardUpdateMonitor.this.updateFingerprintListeningState();
            }
            KeyguardUpdateMonitor.this.handleFingerprintHelp(i, charSequence.toString());
        }

        @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
        public void onAuthenticationError(int i, CharSequence charSequence) {
            if (i != 101) {
                KeyguardUpdateMonitor.this.mHandler.removeMessages(501);
                if (((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mDuringAcquired) {
                    ((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mDuringAcquired = false;
                    KeyguardUpdateMonitor.this.updateFingerprintListeningState();
                }
                if (i == 9 && KeyguardUpdateMonitor.this.getFingerprintFailedUnlockAttempts() == 4) {
                    KeyguardUpdateMonitor.this.handleFingerprintAuthFailed();
                }
                KeyguardUpdateMonitor.this.handleFingerprintError(i, charSequence == null ? "" : charSequence.toString());
            } else if (KeyguardUpdateMonitor.this.mFingerprintRunningState == 1 && !((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mLockoutState) {
                Log.d("KeyguardUpdateMonitor", "state stopped when interrupted");
                KeyguardUpdateMonitor.this.setFingerprintRunningState(0);
            }
        }

        public void onAuthenticationAcquired(int i) {
            KeyguardUpdateMonitor.this.handleFingerprintAcquired(i);
        }
    };
    private CancellationSignal mFingerprintCancelSignal;
    private boolean mFingerprintLockedOut;
    private final FingerprintManager.LockoutResetCallback mFingerprintLockoutResetCallback = new FingerprintManager.LockoutResetCallback() { // from class: com.android.keyguard.KeyguardUpdateMonitor.11
        public void onLockoutReset() {
            KeyguardUpdateMonitor.this.handleFingerprintLockoutReset();
        }
    };
    private int mFingerprintRunningState = 0;
    public boolean mFocusUpdateBatteryInfo = false;
    private FingerprintManager mFpm;
    private boolean mGoingToSleep;
    private final OpKeyguardUpdateMonitor.OpHandler mHandler;
    private int mHardwareFaceUnavailableRetryCount = 0;
    private int mHardwareFingerprintUnavailableRetryCount = 0;
    private boolean mHasLockscreenWallpaper;
    private boolean mIsDreaming;
    private boolean mIsLaunchingEmergencyCall;
    private final boolean mIsPrimaryUser;
    private KeyguardBypassController mKeyguardBypassController;
    private boolean mKeyguardGoingAway;
    private boolean mKeyguardIsVisible;
    private boolean mKeyguardOccluded;
    private boolean mLockIconPressed;
    private LockPatternUtils mLockPatternUtils;
    private boolean mLogoutEnabled;
    private boolean mNeedsSlowUnlockTransition;
    private int mPhoneState;
    @VisibleForTesting
    public PhoneStateListener mPhoneStateListener = new PhoneStateListener() { // from class: com.android.keyguard.KeyguardUpdateMonitor.4
        @Override // android.telephony.PhoneStateListener
        public void onActiveDataSubscriptionIdChanged(int i) {
            KeyguardUpdateMonitor.this.mActiveMobileDataSubscription = i;
            KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(328);
        }
    };
    private Runnable mRetryFaceAuthentication = new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor.8
        @Override // java.lang.Runnable
        public void run() {
            Log.w("KeyguardUpdateMonitor", "Retrying face after HW unavailable, attempt " + KeyguardUpdateMonitor.this.mHardwareFaceUnavailableRetryCount);
            KeyguardUpdateMonitor.this.updateFaceListeningState();
        }
    };
    private Runnable mRetryFingerprintAuthentication = new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor.7
        @Override // java.lang.Runnable
        public void run() {
            Log.w("KeyguardUpdateMonitor", "Retrying fingerprint after HW unavailable, attempt " + KeyguardUpdateMonitor.this.mHardwareFingerprintUnavailableRetryCount);
            KeyguardUpdateMonitor.this.updateFingerprintListeningState();
        }
    };
    private int mRingMode;
    private final Observer<Integer> mRingerModeObserver = new Observer<Integer>() { // from class: com.android.keyguard.KeyguardUpdateMonitor.2
        public void onChanged(Integer num) {
            KeyguardUpdateMonitor.this.mHandler.obtainMessage(305, num.intValue(), 0).sendToTarget();
        }
    };
    private RingerModeTracker mRingerModeTracker;
    private boolean mScreenOn;
    private Map<Integer, Intent> mSecondaryLockscreenRequirement = new HashMap();
    private boolean mSecureCameraLaunched;
    private ServiceState mServiceStateWhenNoSim = null;
    HashMap<Integer, ServiceState> mServiceStates = new HashMap<>();
    HashMap<Integer, SimData> mSimDatas = new HashMap<>();
    private final StatusBarStateController mStatusBarStateController;
    private StrongAuthTracker mStrongAuthTracker;
    private List<SubscriptionInfo> mSubscriptionInfo;
    private SubscriptionManager.OnSubscriptionsChangedListener mSubscriptionListener = new SubscriptionManager.OnSubscriptionsChangedListener() { // from class: com.android.keyguard.KeyguardUpdateMonitor.5
        @Override // android.telephony.SubscriptionManager.OnSubscriptionsChangedListener
        public void onSubscriptionsChanged() {
            KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(328);
        }
    };
    private SubscriptionManager mSubscriptionManager;
    private boolean mSwitchingUser;
    private final TaskStackChangeListener mTaskStackListener = new TaskStackChangeListener() { // from class: com.android.keyguard.KeyguardUpdateMonitor.18
        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChangedBackground() {
            try {
                ActivityManager.StackInfo stackInfo = ActivityTaskManager.getService().getStackInfo(0, 4);
                if (stackInfo != null) {
                    KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(335, Boolean.valueOf(stackInfo.visible)));
                }
            } catch (RemoteException e) {
                Log.e("KeyguardUpdateMonitor", "unable to check task stack", e);
            }
        }
    };
    @VisibleForTesting
    protected boolean mTelephonyCapable;
    private TelephonyManager mTelephonyManager;
    private TrustManager mTrustManager;
    private Runnable mUpdateBiometricListeningState = new Runnable() { // from class: com.android.keyguard.-$$Lambda$1j3e9zQjoHLmTKUSOai-AGDPc7o
        @Override // java.lang.Runnable
        public final void run() {
            KeyguardUpdateMonitor.this.updateBiometricListeningState();
        }
    };
    private Runnable mUpdateFaceListeningState = new Runnable() { // from class: com.android.keyguard.-$$Lambda$KeyguardUpdateMonitor$-p3j9mgQKQBipOv8IK-aOQhECw0
        @Override // java.lang.Runnable
        public final void run() {
            KeyguardUpdateMonitor.this.updateFaceListeningState();
        }
    };
    @VisibleForTesting
    SparseArray<BiometricAuthenticated> mUserFaceAuthenticated = new SparseArray<>();
    private SparseBooleanArray mUserFaceUnlockRunning = new SparseBooleanArray();
    @VisibleForTesting
    SparseArray<BiometricAuthenticated> mUserFingerprintAuthenticated = new SparseArray<>();
    private SparseBooleanArray mUserHasTrust = new SparseBooleanArray();
    private SparseBooleanArray mUserIsUnlocked = new SparseBooleanArray();
    private UserManager mUserManager;
    private final UserSwitchObserver mUserSwitchObserver = new UserSwitchObserver() { // from class: com.android.keyguard.KeyguardUpdateMonitor.16
        public void onUserSwitching(int i, IRemoteCallback iRemoteCallback) {
            KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(310, i, 0, iRemoteCallback));
        }

        public void onUserSwitchComplete(int i) throws RemoteException {
            KeyguardUpdateMonitor.this.mHandler.sendMessage(KeyguardUpdateMonitor.this.mHandler.obtainMessage(314, i, 0));
        }
    };
    private SparseBooleanArray mUserTrustIsManaged = new SparseBooleanArray();
    private SparseBooleanArray mUserTrustIsUsuallyManaged = new SparseBooleanArray();

    private boolean containsFlag(int i, int i2) {
        if ((i & i2) != 0) {
            return DEBUG;
        }
        return false;
    }

    public static boolean isSimPinSecure(int i) {
        if (i == 2 || i == 3 || i == 7) {
            return DEBUG;
        }
        return false;
    }

    static {
        try {
            CORE_APPS_ONLY = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class BiometricAuthenticated {
        private final boolean mAuthenticated;
        private final boolean mIsStrongBiometric;

        BiometricAuthenticated(boolean z, boolean z2) {
            this.mAuthenticated = z;
            this.mIsStrongBiometric = z2;
        }
    }

    public static synchronized void setCurrentUser(int i) {
        synchronized (KeyguardUpdateMonitor.class) {
            sCurrentUser = i;
        }
    }

    public static synchronized int getCurrentUser() {
        int i;
        synchronized (KeyguardUpdateMonitor.class) {
            i = sCurrentUser;
        }
        return i;
    }

    public void onTrustChanged(boolean z, int i, int i2) {
        Assert.isMainThread();
        this.mUserHasTrust.put(i, z);
        for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustChanged(i);
                if (z && i2 != 0) {
                    keyguardUpdateMonitorCallback.onTrustGrantedWithFlags(i2, i);
                }
            }
        }
    }

    public void onTrustError(CharSequence charSequence) {
        dispatchErrorMessage(charSequence);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSimSubscriptionInfoChanged() {
        Assert.isMainThread();
        Log.v("KeyguardUpdateMonitor", "onSubscriptionInfoChanged()");
        List<SubscriptionInfo> completeActiveSubscriptionInfoList = this.mSubscriptionManager.getCompleteActiveSubscriptionInfoList();
        if (completeActiveSubscriptionInfoList != null) {
            Iterator<SubscriptionInfo> it = completeActiveSubscriptionInfoList.iterator();
            while (it.hasNext()) {
                Log.v("KeyguardUpdateMonitor", "SubInfo:" + it.next());
            }
        } else {
            Log.v("KeyguardUpdateMonitor", "onSubscriptionInfoChanged: list is null");
        }
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(DEBUG);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < subscriptionInfo.size(); i++) {
            SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(i);
            if (refreshSimState(subscriptionInfo2.getSubscriptionId(), subscriptionInfo2.getSimSlotIndex())) {
                arrayList.add(subscriptionInfo2);
            }
        }
        for (int i2 = 0; i2 < arrayList.size(); i2++) {
            SimData simData = this.mSimDatas.get(Integer.valueOf(((SubscriptionInfo) arrayList.get(i2)).getSubscriptionId()));
            for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onSimStateChanged(simData.subId, simData.slotId, simData.simState);
                }
            }
        }
        callbacksRefreshCarrierInfo();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAirplaneModeChanged() {
        callbacksRefreshCarrierInfo();
    }

    private void callbacksRefreshCarrierInfo() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
            }
        }
    }

    public List<SubscriptionInfo> getSubscriptionInfo(boolean z) {
        List<SubscriptionInfo> list = this.mSubscriptionInfo;
        if (list == null || z) {
            list = this.mSubscriptionManager.getCompleteActiveSubscriptionInfoList();
        }
        if (list == null) {
            this.mSubscriptionInfo = new ArrayList();
        } else {
            this.mSubscriptionInfo = list;
        }
        return new ArrayList(this.mSubscriptionInfo);
    }

    public List<SubscriptionInfo> getFilteredSubscriptionInfo(boolean z) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        if (subscriptionInfo.size() == 2) {
            SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(0);
            SubscriptionInfo subscriptionInfo3 = subscriptionInfo.get(1);
            if (subscriptionInfo2.getGroupUuid() == null || !subscriptionInfo2.getGroupUuid().equals(subscriptionInfo3.getGroupUuid()) || (!subscriptionInfo2.isOpportunistic() && !subscriptionInfo3.isOpportunistic())) {
                return subscriptionInfo;
            }
            if (CarrierConfigManager.getDefaultConfig().getBoolean("always_show_primary_signal_bar_in_opportunistic_network_boolean")) {
                if (!subscriptionInfo2.isOpportunistic()) {
                    subscriptionInfo2 = subscriptionInfo3;
                }
                subscriptionInfo.remove(subscriptionInfo2);
            } else {
                if (subscriptionInfo2.getSubscriptionId() == this.mActiveMobileDataSubscription) {
                    subscriptionInfo2 = subscriptionInfo3;
                }
                subscriptionInfo.remove(subscriptionInfo2);
            }
        }
        return subscriptionInfo;
    }

    public void onTrustManagedChanged(boolean z, int i) {
        Assert.isMainThread();
        this.mUserTrustIsManaged.put(i, z);
        this.mUserTrustIsUsuallyManaged.put(i, this.mTrustManager.isTrustUsuallyManaged(i));
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustManagedChanged(i);
            }
        }
    }

    public void setCredentialAttempted() {
        this.mCredentialAttempted = DEBUG;
        updateBiometricListeningState();
    }

    public void setKeyguardGoingAway(boolean z) {
        this.mKeyguardGoingAway = z;
    }

    public void setKeyguardOccluded(boolean z) {
        this.mKeyguardOccluded = z;
        updateBiometricListeningState();
    }

    public boolean isDreaming() {
        return this.mIsDreaming;
    }

    public void awakenFromDream() {
        IDreamManager iDreamManager;
        if (this.mIsDreaming && (iDreamManager = this.mDreamManager) != null) {
            try {
                iDreamManager.awaken();
            } catch (RemoteException unused) {
                Log.e("KeyguardUpdateMonitor", "Unable to awaken from dream");
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void onFingerprintAuthenticated(int i, boolean z) {
        Assert.isMainThread();
        Trace.beginSection("KeyGuardUpdateMonitor#onFingerPrintAuthenticated");
        this.mUserFingerprintAuthenticated.put(i, new BiometricAuthenticated(DEBUG, z));
        if (getUserCanSkipBouncer(i)) {
            this.mTrustManager.unlockedByBiometricForUser(i, BiometricSourceType.FINGERPRINT);
        }
        this.mFingerprintCancelSignal = null;
        opOnFingerprintAuthenticated(i);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthenticated(i, BiometricSourceType.FINGERPRINT, z);
            }
        }
        OpKeyguardUpdateMonitor.OpHandler opHandler = this.mHandler;
        opHandler.sendMessageDelayed(opHandler.obtainMessage(336), 500);
        this.mAssistantVisible = false;
        reportSuccessfulBiometricUnlock(z, i);
        Trace.endSection();
    }

    private void reportSuccessfulBiometricUnlock(final boolean z, final int i) {
        this.mBackgroundExecutor.execute(new Runnable() { // from class: com.android.keyguard.KeyguardUpdateMonitor.6
            @Override // java.lang.Runnable
            public void run() {
                KeyguardUpdateMonitor.this.mLockPatternUtils.reportSuccessfulBiometricUnlock(z, i);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintAuthFailed() {
        Assert.isMainThread();
        if (getFingerprintFailedUnlockAttempts() < 5) {
            this.mFingerprintFailedAttempts.put(sCurrentUser, getFingerprintFailedUnlockAttempts() + 1);
            if (OpKeyguardUpdateMonitor.DEBUG) {
                Log.d("KeyguardUpdateMonitor", "fp Auth Failed, failed attempts = " + getFingerprintFailedUnlockAttempts());
            }
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthFailed(BiometricSourceType.FINGERPRINT);
            }
        }
        handleFingerprintHelp(-1, this.mContext.getString(C0015R$string.kg_fingerprint_not_recognized));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintAcquired(int i) {
        Assert.isMainThread();
        opHandleFingerprintAcquired(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintAuthenticated(int i, boolean z) {
        Trace.beginSection("KeyGuardUpdateMonitor#handlerFingerPrintAuthenticated");
        int i2 = 0;
        try {
            int i3 = ActivityManager.getService().getCurrentUser().id;
            if (i3 != i) {
                try {
                    Log.d("KeyguardUpdateMonitor", "Fingerprint authenticated for wrong user: " + i);
                    handleFingerprintAuthFailed();
                } finally {
                    setFingerprintRunningState(i2);
                }
            } else if (isFingerprintDisabled(i3)) {
                Log.d("KeyguardUpdateMonitor", "Fingerprint disabled by DPM for userId: " + i3);
                handleFingerprintAuthFailed();
                setFingerprintRunningState(0);
            } else {
                onFingerprintAuthenticated(i3, z);
                setFingerprintRunningState(0);
                Trace.endSection();
            }
        } catch (RemoteException e) {
            Log.e("KeyguardUpdateMonitor", "Failed to get current user id: ", e);
            setFingerprintRunningState(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintHelp(int i, String str) {
        Assert.isMainThread();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricHelp(i, str, BiometricSourceType.FINGERPRINT);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintError(int i, String str) {
        int i2;
        Assert.isMainThread();
        opHandleFingerprintError1(i);
        if (i == 5 && this.mHandler.hasCallbacks(this.mCancelNotReceived)) {
            this.mHandler.removeCallbacks(this.mCancelNotReceived);
        }
        if (i == 5 && this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(0);
            updateFingerprintListeningState();
        } else {
            setFingerprintRunningState(0);
            this.mFingerprintCancelSignal = null;
            this.mFaceCancelSignal = null;
        }
        if (i == 1 && (i2 = this.mHardwareFingerprintUnavailableRetryCount) < 10) {
            this.mHardwareFingerprintUnavailableRetryCount = i2 + 1;
            this.mHandler.removeCallbacks(this.mRetryFingerprintAuthentication);
            this.mHandler.postDelayed(this.mRetryFingerprintAuthentication, 500);
        }
        if (i == 9) {
            this.mLockPatternUtils.requireStrongAuth(8, getCurrentUser());
        }
        if (i == 7 || i == 9) {
            this.mFingerprintLockedOut = DEBUG;
        }
        for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricError(i, str, BiometricSourceType.FINGERPRINT);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintLockoutReset() {
        this.mFingerprintLockedOut = false;
        OpKeyguardUpdateMonitor.OpHandler opHandler = this.mHandler;
        opHandler.sendMessageDelayed(opHandler.obtainMessage(336), !this.mLockoutState ? 100 : 500);
        opHandleFingerprintLockoutReset();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setFingerprintRunningState(int i) {
        boolean z = false;
        boolean z2 = this.mFingerprintRunningState == 1;
        if (i == 1) {
            z = true;
        }
        Log.d("KeyguardUpdateMonitor", "fingerprintRunningState: " + z2 + " to " + z + ", " + i);
        this.mFingerprintRunningState = i;
        if (z2 != z) {
            notifyFingerprintRunningStateChanged();
        }
    }

    private void notifyFingerprintRunningStateChanged() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricRunningStateChanged(isFingerprintDetectionRunning(), BiometricSourceType.FINGERPRINT);
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void onFaceAuthenticated(int i, boolean z) {
        Trace.beginSection("KeyGuardUpdateMonitor#onFaceAuthenticated");
        Assert.isMainThread();
        this.mUserFaceAuthenticated.put(i, new BiometricAuthenticated(DEBUG, z));
        if (getUserCanSkipBouncer(i)) {
            this.mTrustManager.unlockedByBiometricForUser(i, BiometricSourceType.FACE);
        }
        this.mFaceCancelSignal = null;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthenticated(i, BiometricSourceType.FACE, z);
            }
        }
        OpKeyguardUpdateMonitor.OpHandler opHandler = this.mHandler;
        opHandler.sendMessageDelayed(opHandler.obtainMessage(336), 500);
        this.mAssistantVisible = false;
        reportSuccessfulBiometricUnlock(z, i);
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceAuthFailed() {
        Assert.isMainThread();
        setFaceRunningState(0);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricAuthFailed(BiometricSourceType.FACE);
            }
        }
        handleFaceHelp(-2, this.mContext.getString(C0015R$string.kg_face_not_recognized));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceAcquired(int i) {
        Assert.isMainThread();
        if (i == 0) {
            if (DEBUG_FACE) {
                Log.d("KeyguardUpdateMonitor", "Face acquired");
            }
            for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onBiometricAcquired(BiometricSourceType.FACE);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceAuthenticated(int i, boolean z) {
        Trace.beginSection("KeyGuardUpdateMonitor#handlerFaceAuthenticated");
        int i2 = 0;
        try {
            if (this.mGoingToSleep) {
                Log.d("KeyguardUpdateMonitor", "Aborted successful auth because device is going to sleep.");
                return;
            }
            try {
                int i3 = ActivityManager.getService().getCurrentUser().id;
                if (i3 != i) {
                    Log.d("KeyguardUpdateMonitor", "Face authenticated for wrong user: " + i);
                    setFaceRunningState(0);
                } else if (isFaceDisabled(i3)) {
                    Log.d("KeyguardUpdateMonitor", "Face authentication disabled by DPM for userId: " + i3);
                    setFaceRunningState(0);
                } else {
                    if (DEBUG_FACE) {
                        Log.d("KeyguardUpdateMonitor", "Face auth succeeded for user " + i3);
                    }
                    onFaceAuthenticated(i3, z);
                    setFaceRunningState(0);
                    Trace.endSection();
                }
            } catch (RemoteException e) {
                Log.e("KeyguardUpdateMonitor", "Failed to get current user id: ", e);
                setFaceRunningState(0);
            }
        } finally {
            setFaceRunningState(i2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceHelp(int i, String str) {
        Assert.isMainThread();
        if (DEBUG_FACE) {
            Log.d("KeyguardUpdateMonitor", "Face help received: " + str);
        }
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricHelp(i, str, BiometricSourceType.FACE);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceError(int i, String str) {
        int i2;
        Assert.isMainThread();
        if (DEBUG_FACE) {
            Log.d("KeyguardUpdateMonitor", "Face error received: " + str);
        }
        if (i == 5 && this.mHandler.hasCallbacks(this.mCancelNotReceived)) {
            this.mHandler.removeCallbacks(this.mCancelNotReceived);
        }
        if (i == 5 && this.mFaceRunningState == 3) {
            setFaceRunningState(0);
            updateFaceListeningState();
        } else {
            setFaceRunningState(0);
        }
        if ((i == 1 || i == 2) && (i2 = this.mHardwareFaceUnavailableRetryCount) < 10) {
            this.mHardwareFaceUnavailableRetryCount = i2 + 1;
            this.mHandler.removeCallbacks(this.mRetryFaceAuthentication);
            this.mHandler.postDelayed(this.mRetryFaceAuthentication, 500);
        }
        if (i == 9) {
            this.mLockPatternUtils.requireStrongAuth(8, getCurrentUser());
        }
        for (int i3 = 0; i3 < this.mCallbacks.size(); i3++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i3).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricError(i, str, BiometricSourceType.FACE);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceLockoutReset() {
        updateFaceListeningState();
    }

    private void setFaceRunningState(int i) {
        boolean z = false;
        boolean z2 = this.mFaceRunningState == 1;
        if (i == 1) {
            z = true;
        }
        this.mFaceRunningState = i;
        Log.d("KeyguardUpdateMonitor", "faceRunningState: " + this.mFaceRunningState);
        if (z2 != z) {
            notifyFaceRunningStateChanged();
        }
    }

    private void notifyFaceRunningStateChanged() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricRunningStateChanged(isFaceDetectionRunning(), BiometricSourceType.FACE);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFaceUnlockStateChanged(boolean z, int i) {
        Assert.isMainThread();
        this.mUserFaceUnlockRunning.put(i, z);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFaceUnlockStateChanged(z, i);
            }
        }
    }

    public boolean isFingerprintDetectionRunning() {
        if (this.mFingerprintRunningState == 1) {
            return DEBUG;
        }
        return false;
    }

    public boolean isFaceDetectionRunning() {
        if (this.mFaceRunningState == 1) {
            return DEBUG;
        }
        return false;
    }

    private boolean isTrustDisabled(int i) {
        return isSimPinSecure();
    }

    private boolean isFingerprintDisabled(int i) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        if ((devicePolicyManager == null || (devicePolicyManager.getKeyguardDisabledFeatures(null, i) & 32) == 0) && !isSimPinSecure()) {
            return false;
        }
        return DEBUG;
    }

    private boolean isFaceDisabled(int i) {
        return ((Boolean) DejankUtils.whitelistIpcs(new Supplier((DevicePolicyManager) this.mContext.getSystemService("device_policy"), i) { // from class: com.android.keyguard.-$$Lambda$KeyguardUpdateMonitor$N2Cyv6mYvgookTnpPTeaGdzNtxk
            public final /* synthetic */ DevicePolicyManager f$1;
            public final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return KeyguardUpdateMonitor.this.lambda$isFaceDisabled$0$KeyguardUpdateMonitor(this.f$1, this.f$2);
            }
        })).booleanValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$isFaceDisabled$0 */
    public /* synthetic */ Boolean lambda$isFaceDisabled$0$KeyguardUpdateMonitor(DevicePolicyManager devicePolicyManager, int i) {
        return Boolean.valueOf(((devicePolicyManager == null || (devicePolicyManager.getKeyguardDisabledFeatures(null, i) & 128) == 0) && !isSimPinSecure()) ? false : DEBUG);
    }

    public boolean getUserCanSkipBouncer(int i) {
        if (getUserHasTrust(i) || getUserUnlockedWithBiometric(i)) {
            return DEBUG;
        }
        return false;
    }

    public boolean getUserHasTrust(int i) {
        if ((isTrustDisabled(i) || !this.mUserHasTrust.get(i)) && !canSkipBouncerByFacelock()) {
            return false;
        }
        return DEBUG;
    }

    public boolean getUserUnlockedWithBiometric(int i) {
        BiometricAuthenticated biometricAuthenticated = this.mUserFingerprintAuthenticated.get(i);
        BiometricAuthenticated biometricAuthenticated2 = this.mUserFaceAuthenticated.get(i);
        boolean z = biometricAuthenticated != null && biometricAuthenticated.mAuthenticated && isUnlockingWithBiometricAllowed(biometricAuthenticated.mIsStrongBiometric);
        boolean z2 = biometricAuthenticated2 != null && biometricAuthenticated2.mAuthenticated && isUnlockingWithBiometricAllowed(biometricAuthenticated2.mIsStrongBiometric);
        Log.i("KeyguardUpdateMonitor", "getUserUnlockedWithBiometric, fingerprintAllowed:" + z + ", faceAllowed:" + z2 + ", canSkipBouncerByFacelock():" + canSkipBouncerByFacelock() + ", isFacelockUnlocking():" + isFacelockUnlocking());
        if (z || z2 || canSkipBouncerByFacelock() || isFacelockUnlocking()) {
            return DEBUG;
        }
        return false;
    }

    public boolean getUserTrustIsManaged(int i) {
        if ((!this.mUserTrustIsManaged.get(i) || isTrustDisabled(i)) && !canSkipBouncerByFacelock()) {
            return false;
        }
        return DEBUG;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0089 A[LOOP:0: B:16:0x0089->B:21:0x00a4, LOOP_START, PHI: r3 
      PHI: (r3v1 int) = (r3v0 int), (r3v2 int) binds: [B:15:0x0087, B:21:0x00a4] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARNING: Removed duplicated region for block: B:25:? A[ORIG_RETURN, RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateSecondaryLockscreenRequirement(int r6) {
        /*
            r5 = this;
            java.util.Map<java.lang.Integer, android.content.Intent> r0 = r5.mSecondaryLockscreenRequirement
            java.lang.Integer r1 = java.lang.Integer.valueOf(r6)
            java.lang.Object r0 = r0.get(r1)
            android.content.Intent r0 = (android.content.Intent) r0
            android.app.admin.DevicePolicyManager r1 = r5.mDevicePolicyManager
            android.os.UserHandle r2 = android.os.UserHandle.of(r6)
            boolean r1 = r1.isSecondaryLockscreenEnabled(r2)
            r2 = 1
            r3 = 0
            if (r1 == 0) goto L_0x0077
            if (r0 != 0) goto L_0x0077
            android.app.admin.DevicePolicyManager r0 = r5.mDevicePolicyManager
            android.os.UserHandle r1 = android.os.UserHandle.of(r6)
            android.content.ComponentName r0 = r0.getProfileOwnerOrDeviceOwnerSupervisionComponent(r1)
            if (r0 != 0) goto L_0x003f
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "No Profile Owner or Device Owner supervision app found for User "
            r0.append(r1)
            r0.append(r6)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "KeyguardUpdateMonitor"
            android.util.Log.e(r1, r0)
            goto L_0x0086
        L_0x003f:
            android.content.Intent r1 = new android.content.Intent
            java.lang.String r4 = "android.app.action.BIND_SECONDARY_LOCKSCREEN_SERVICE"
            r1.<init>(r4)
            java.lang.String r0 = r0.getPackageName()
            android.content.Intent r0 = r1.setPackage(r0)
            android.content.Context r1 = r5.mContext
            android.content.pm.PackageManager r1 = r1.getPackageManager()
            android.content.pm.ResolveInfo r0 = r1.resolveService(r0, r3)
            if (r0 == 0) goto L_0x0086
            android.content.pm.ServiceInfo r1 = r0.serviceInfo
            if (r1 == 0) goto L_0x0086
            android.content.Intent r1 = new android.content.Intent
            r1.<init>()
            android.content.pm.ServiceInfo r0 = r0.serviceInfo
            android.content.ComponentName r0 = r0.getComponentName()
            android.content.Intent r0 = r1.setComponent(r0)
            java.util.Map<java.lang.Integer, android.content.Intent> r1 = r5.mSecondaryLockscreenRequirement
            java.lang.Integer r4 = java.lang.Integer.valueOf(r6)
            r1.put(r4, r0)
            goto L_0x0087
        L_0x0077:
            if (r1 != 0) goto L_0x0086
            if (r0 == 0) goto L_0x0086
            java.util.Map<java.lang.Integer, android.content.Intent> r0 = r5.mSecondaryLockscreenRequirement
            java.lang.Integer r1 = java.lang.Integer.valueOf(r6)
            r4 = 0
            r0.put(r1, r4)
            goto L_0x0087
        L_0x0086:
            r2 = r3
        L_0x0087:
            if (r2 == 0) goto L_0x00a7
        L_0x0089:
            java.util.ArrayList<java.lang.ref.WeakReference<com.android.keyguard.KeyguardUpdateMonitorCallback>> r0 = r5.mCallbacks
            int r0 = r0.size()
            if (r3 >= r0) goto L_0x00a7
            java.util.ArrayList<java.lang.ref.WeakReference<com.android.keyguard.KeyguardUpdateMonitorCallback>> r0 = r5.mCallbacks
            java.lang.Object r0 = r0.get(r3)
            java.lang.ref.WeakReference r0 = (java.lang.ref.WeakReference) r0
            java.lang.Object r0 = r0.get()
            com.android.keyguard.KeyguardUpdateMonitorCallback r0 = (com.android.keyguard.KeyguardUpdateMonitorCallback) r0
            if (r0 == 0) goto L_0x00a4
            r0.onSecondaryLockscreenRequirementChanged(r6)
        L_0x00a4:
            int r3 = r3 + 1
            goto L_0x0089
        L_0x00a7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardUpdateMonitor.updateSecondaryLockscreenRequirement(int):void");
    }

    public Intent getSecondaryLockscreenRequirement(int i) {
        return this.mSecondaryLockscreenRequirement.get(Integer.valueOf(i));
    }

    public boolean isTrustUsuallyManaged(int i) {
        Assert.isMainThread();
        return this.mUserTrustIsUsuallyManaged.get(i);
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitor
    public boolean isWeakFaceTimeout() {
        if (!OpUtils.isWeakFaceUnlockEnabled() || !isUnlockWithFacelockPossible()) {
            return false;
        }
        if (this.mStrongAuthTracker.isWeakFaceTimeout() || isFacelockDisabled()) {
            return DEBUG;
        }
        return false;
    }

    public int getBiometricTimeoutStringWhenPrompt(BiometricSourceType biometricSourceType, KeyguardSecurityModel.SecurityMode securityMode) {
        if (!OpUtils.isWeakFaceUnlockEnabled() || !isWeakFaceTimeout()) {
            return 0;
        }
        if (!isUnlockingWithBiometricAllowed() || !isUnlockWithFingerprintPossible(getCurrentUser())) {
            int i = AnonymousClass19.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[securityMode.ordinal()];
            if (i == 1) {
                return C0015R$string.op_kg_prompt_reason_face_timeout_pattern;
            }
            if (i == 2) {
                return C0015R$string.op_kg_prompt_reason_face_timeout_pin;
            }
            if (i != 3) {
                return 0;
            }
            return C0015R$string.op_kg_prompt_reason_face_timeout_password;
        }
        int i2 = AnonymousClass19.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[securityMode.ordinal()];
        if (i2 == 1) {
            return C0015R$string.op_kg_prompt_reason_face_timeout_bouncer_pattern;
        }
        if (i2 == 2) {
            return C0015R$string.op_kg_prompt_reason_face_timeout_bouncer_pin;
        }
        if (i2 != 3) {
            return 0;
        }
        return C0015R$string.op_kg_prompt_reason_face_timeout_bouncer_password;
    }

    /* renamed from: com.android.keyguard.KeyguardUpdateMonitor$19  reason: invalid class name */
    static /* synthetic */ class AnonymousClass19 {
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
        }
    }

    public boolean isUnlockingWithBiometricAllowed(boolean z) {
        return this.mStrongAuthTracker.isUnlockingWithBiometricAllowed(z);
    }

    public boolean isUserInLockdown(int i) {
        return containsFlag(this.mStrongAuthTracker.getStrongAuthForUser(i), 32);
    }

    public boolean userNeedsStrongAuth() {
        if (this.mStrongAuthTracker.getStrongAuthForUser(getCurrentUser()) != 0) {
            return DEBUG;
        }
        return false;
    }

    public boolean needsSlowUnlockTransition() {
        return this.mNeedsSlowUnlockTransition;
    }

    public StrongAuthTracker getStrongAuthTracker() {
        return this.mStrongAuthTracker;
    }

    /* access modifiers changed from: private */
    public void notifyStrongAuthStateChanged(int i) {
        Assert.isMainThread();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStrongAuthStateChanged(i);
            }
        }
    }

    public boolean isScreenOn() {
        return this.mScreenOn;
    }

    private void dispatchErrorMessage(CharSequence charSequence) {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTrustAgentErrorMessage(charSequence);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setAssistantVisible(boolean z) {
        this.mAssistantVisible = z;
        updateBiometricListeningState();
    }

    /* access modifiers changed from: private */
    public static class SimData {
        public int simState;
        public int slotId;
        public int subId;

        SimData(int i, int i2, int i3) {
            this.simState = i;
            this.slotId = i2;
            this.subId = i3;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:29:0x008a, code lost:
            if ("IMSI".equals(r0) == false) goto L_0x008d;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        static com.android.keyguard.KeyguardUpdateMonitor.SimData fromIntent(android.content.Intent r7) {
            /*
                java.lang.String r0 = r7.getAction()
                java.lang.String r1 = "android.intent.action.SIM_STATE_CHANGED"
                boolean r0 = r1.equals(r0)
                if (r0 == 0) goto L_0x0093
                java.lang.String r0 = "ss"
                java.lang.String r0 = r7.getStringExtra(r0)
                java.lang.String r1 = "android.telephony.extra.SLOT_INDEX"
                r2 = 0
                int r1 = r7.getIntExtra(r1, r2)
                r3 = -1
                java.lang.String r4 = "android.telephony.extra.SUBSCRIPTION_INDEX"
                int r3 = r7.getIntExtra(r4, r3)
                java.lang.String r4 = "ABSENT"
                boolean r4 = r4.equals(r0)
                r5 = 5
                java.lang.String r6 = "reason"
                if (r4 == 0) goto L_0x003d
                java.lang.String r7 = r7.getStringExtra(r6)
                java.lang.String r0 = "PERM_DISABLED"
                boolean r7 = r0.equals(r7)
                if (r7 == 0) goto L_0x003a
                r7 = 7
                goto L_0x003b
            L_0x003a:
                r7 = 1
            L_0x003b:
                r2 = r7
                goto L_0x008d
            L_0x003d:
                java.lang.String r4 = "READY"
                boolean r4 = r4.equals(r0)
                if (r4 == 0) goto L_0x0047
            L_0x0045:
                r2 = r5
                goto L_0x008d
            L_0x0047:
                java.lang.String r4 = "LOCKED"
                boolean r4 = r4.equals(r0)
                if (r4 == 0) goto L_0x0067
                java.lang.String r7 = r7.getStringExtra(r6)
                java.lang.String r0 = "PIN"
                boolean r0 = r0.equals(r7)
                if (r0 == 0) goto L_0x005d
                r2 = 2
                goto L_0x008d
            L_0x005d:
                java.lang.String r0 = "PUK"
                boolean r7 = r0.equals(r7)
                if (r7 == 0) goto L_0x008d
                r2 = 3
                goto L_0x008d
            L_0x0067:
                java.lang.String r7 = "NETWORK"
                boolean r7 = r7.equals(r0)
                if (r7 == 0) goto L_0x0071
                r2 = 4
                goto L_0x008d
            L_0x0071:
                java.lang.String r7 = "CARD_IO_ERROR"
                boolean r7 = r7.equals(r0)
                if (r7 == 0) goto L_0x007c
                r2 = 8
                goto L_0x008d
            L_0x007c:
                java.lang.String r7 = "LOADED"
                boolean r7 = r7.equals(r0)
                if (r7 != 0) goto L_0x0045
                java.lang.String r7 = "IMSI"
                boolean r7 = r7.equals(r0)
                if (r7 == 0) goto L_0x008d
                goto L_0x0045
            L_0x008d:
                com.android.keyguard.KeyguardUpdateMonitor$SimData r7 = new com.android.keyguard.KeyguardUpdateMonitor$SimData
                r7.<init>(r2, r1, r3)
                return r7
            L_0x0093:
                java.lang.IllegalArgumentException r7 = new java.lang.IllegalArgumentException
                java.lang.String r0 = "only handles intent ACTION_SIM_STATE_CHANGED"
                r7.<init>(r0)
                throw r7
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardUpdateMonitor.SimData.fromIntent(android.content.Intent):com.android.keyguard.KeyguardUpdateMonitor$SimData");
        }

        public String toString() {
            return "SimData{state=" + this.simState + ",slotId=" + this.slotId + ",subId=" + this.subId + "}";
        }
    }

    public static class StrongAuthTracker extends LockPatternUtils.StrongAuthTracker {
        private boolean mIsFaceTimeout = false;
        private final Consumer<Integer> mStrongAuthRequiredChangedCallback;

        public StrongAuthTracker(Context context, Consumer<Integer> consumer) {
            super(context);
            this.mStrongAuthRequiredChangedCallback = consumer;
        }

        public boolean isUnlockingWithBiometricAllowed(boolean z) {
            return isBiometricAllowedForUser(z, KeyguardUpdateMonitor.getCurrentUser());
        }

        public boolean hasUserAuthenticatedSinceBoot() {
            if ((getStrongAuthForUser(KeyguardUpdateMonitor.getCurrentUser()) & 1) == 0) {
                return KeyguardUpdateMonitor.DEBUG;
            }
            return false;
        }

        public void onStrongAuthRequiredChanged(int i) {
            this.mStrongAuthRequiredChangedCallback.accept(Integer.valueOf(i));
        }

        public boolean isWeakFaceTimeout() {
            return this.mIsFaceTimeout;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitor
    public void handleStartedWakingUp() {
        Trace.beginSection("KeyguardUpdateMonitor#handleStartedWakingUp");
        Assert.isMainThread();
        updateBiometricListeningState();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStartedWakingUp();
            }
        }
        super.handleStartedWakingUp();
        Trace.endSection();
    }

    /* access modifiers changed from: protected */
    public void handleStartedGoingToSleep(int i) {
        Assert.isMainThread();
        this.mLockIconPressed = false;
        clearBiometricRecognized();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onStartedGoingToSleep(i);
            }
        }
        this.mGoingToSleep = DEBUG;
        opHandleStartedGoingToSleep();
        if (OpKeyguardUpdateMonitor.IS_SUPPORT_CUSTOM_FINGERPRINT) {
            this.mAodFpAuthenticatedTime = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void handleFinishedGoingToSleep(int i) {
        Assert.isMainThread();
        this.mGoingToSleep = false;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onFinishedGoingToSleep(i);
            }
        }
        updateBiometricListeningState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenTurnedOn() {
        Assert.isMainThread();
        if (OpKeyguardUpdateMonitor.IS_SUPPORT_CUSTOM_FINGERPRINT) {
            updateFingerprintListeningState();
        }
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            try {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onScreenTurnedOn();
                }
            } catch (Exception e) {
                Log.e("KeyguardUpdateMonitor", "handleScreenTurnedOn error : " + e);
            }
        }
        opHandleScreenTurnedOn();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenTurnedOff() {
        DejankUtils.startDetectingBlockingIpcs("KeyguardUpdateMonitor#handleScreenTurnedOff");
        Assert.isMainThread();
        this.mHardwareFingerprintUnavailableRetryCount = 0;
        this.mHardwareFaceUnavailableRetryCount = 0;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onScreenTurnedOff();
            }
        }
        opHandleScreenTurnedOff();
        DejankUtils.stopDetectingBlockingIpcs("KeyguardUpdateMonitor#handleScreenTurnedOff");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDreamingStateChanged(int i) {
        Assert.isMainThread();
        boolean z = DEBUG;
        if (i != 1) {
            z = false;
        }
        this.mIsDreaming = z;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDreamingStateChanged(this.mIsDreaming);
            }
        }
        updateBiometricListeningState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserInfoChanged(int i) {
        Assert.isMainThread();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserInfoChanged(i);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserUnlocked(int i) {
        Assert.isMainThread();
        this.mUserIsUnlocked.put(i, DEBUG);
        this.mNeedsSlowUnlockTransition = resolveNeedsSlowUnlockTransition();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserUnlocked();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserStopped(int i) {
        Assert.isMainThread();
        this.mUserIsUnlocked.put(i, this.mUserManager.isUserUnlocked(i));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleUserRemoved(int i) {
        Assert.isMainThread();
        this.mUserIsUnlocked.delete(i);
        this.mUserTrustIsUsuallyManaged.delete(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKeyguardGoingAway(boolean z) {
        Assert.isMainThread();
        setKeyguardGoingAway(z);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setStrongAuthTracker(StrongAuthTracker strongAuthTracker) {
        StrongAuthTracker strongAuthTracker2 = this.mStrongAuthTracker;
        if (strongAuthTracker2 != null) {
            this.mLockPatternUtils.unregisterStrongAuthTracker(strongAuthTracker2);
        }
        this.mStrongAuthTracker = strongAuthTracker;
        this.mLockPatternUtils.registerStrongAuthTracker(strongAuthTracker);
    }

    /* access modifiers changed from: private */
    public void registerRingerTracker() {
        this.mRingerModeTracker.getRingerMode().observeForever(this.mRingerModeObserver);
    }

    @VisibleForTesting
    protected KeyguardUpdateMonitor(Context context, Looper looper, BroadcastDispatcher broadcastDispatcher, DumpManager dumpManager, RingerModeTracker ringerModeTracker, Executor executor, StatusBarStateController statusBarStateController, LockPatternUtils lockPatternUtils) {
        super(context);
        this.mContext = context;
        this.mFodBurnInProtectionHelper = new OpFodBurnInProtectionHelper(context, this);
        this.mSubscriptionManager = SubscriptionManager.from(context);
        this.mDeviceProvisioned = isDeviceProvisionedInSettingsDb();
        this.mStrongAuthTracker = new StrongAuthTracker(context, new Consumer() { // from class: com.android.keyguard.-$$Lambda$KeyguardUpdateMonitor$-GZaxeQabrHzh5b8rORPTQGQVD8
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                KeyguardUpdateMonitor.this.notifyStrongAuthStateChanged(((Integer) obj).intValue());
            }
        });
        this.mBackgroundExecutor = executor;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mRingerModeTracker = ringerModeTracker;
        this.mStatusBarStateController = statusBarStateController;
        this.mLockPatternUtils = lockPatternUtils;
        dumpManager.registerDumpable(KeyguardUpdateMonitor.class.getName(), this);
        this.mHandler = new OpKeyguardUpdateMonitor.OpHandler(looper) { // from class: com.android.keyguard.KeyguardUpdateMonitor.15
            @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitor.OpHandler, android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                boolean z = KeyguardUpdateMonitor.DEBUG;
                switch (i) {
                    case 301:
                        KeyguardUpdateMonitor.this.handleTimeUpdate();
                        return;
                    case 302:
                        KeyguardUpdateMonitor.this.handleBatteryUpdate((OpBatteryStatus) message.obj);
                        return;
                    case 303:
                    case 307:
                    case 311:
                    case 313:
                    case 315:
                    case 316:
                    case 323:
                    case 324:
                    case 325:
                    case 326:
                    default:
                        super.handleMessage(message);
                        return;
                    case 304:
                        KeyguardUpdateMonitor.this.opHandlePendingSubInfoChange(message.arg2);
                        KeyguardUpdateMonitor.this.handleSimStateChange(message.arg1, message.arg2, ((Integer) message.obj).intValue());
                        return;
                    case 305:
                        KeyguardUpdateMonitor.this.handleRingerModeChange(message.arg1);
                        return;
                    case 306:
                        KeyguardUpdateMonitor.this.handlePhoneStateChanged((String) message.obj);
                        return;
                    case 308:
                        KeyguardUpdateMonitor.this.handleDeviceProvisioned();
                        return;
                    case 309:
                        KeyguardUpdateMonitor.this.handleDevicePolicyManagerStateChanged(message.arg1);
                        return;
                    case 310:
                        KeyguardUpdateMonitor.this.handleUserSwitching(message.arg1, (IRemoteCallback) message.obj);
                        return;
                    case 312:
                        KeyguardUpdateMonitor.this.handleKeyguardReset();
                        return;
                    case 314:
                        KeyguardUpdateMonitor.this.handleUserSwitchComplete(message.arg1);
                        return;
                    case 317:
                        KeyguardUpdateMonitor.this.handleUserInfoChanged(message.arg1);
                        return;
                    case 318:
                        KeyguardUpdateMonitor.this.handleReportEmergencyCallAction();
                        return;
                    case 319:
                        Trace.beginSection("KeyguardUpdateMonitor#handler MSG_STARTED_WAKING_UP");
                        KeyguardUpdateMonitor.this.handleStartedWakingUp();
                        Trace.endSection();
                        return;
                    case 320:
                        KeyguardUpdateMonitor.this.handleFinishedGoingToSleep(message.arg1);
                        return;
                    case 321:
                        KeyguardUpdateMonitor.this.handleStartedGoingToSleep(message.arg1);
                        return;
                    case 322:
                        KeyguardUpdateMonitor.this.handleKeyguardBouncerChanged(message.arg1);
                        return;
                    case 327:
                        Trace.beginSection("KeyguardUpdateMonitor#handler MSG_FACE_UNLOCK_STATE_CHANGED");
                        KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.this;
                        if (message.arg1 == 0) {
                            z = false;
                        }
                        keyguardUpdateMonitor.handleFaceUnlockStateChanged(z, message.arg2);
                        Trace.endSection();
                        return;
                    case 328:
                        if (((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mSimUnlockSlot0 || ((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mSimUnlockSlot1) {
                            ((OpKeyguardUpdateMonitor) KeyguardUpdateMonitor.this).mPendingSubInfoChange = KeyguardUpdateMonitor.DEBUG;
                            Log.d("KeyguardUpdateMonitor", "delay handle subinfo change");
                            return;
                        }
                        KeyguardUpdateMonitor.this.handleSimSubscriptionInfoChanged();
                        return;
                    case 329:
                        KeyguardUpdateMonitor.this.handleAirplaneModeChanged();
                        return;
                    case 330:
                        KeyguardUpdateMonitor.this.handleServiceStateChange(message.arg1, (ServiceState) message.obj);
                        return;
                    case 331:
                        KeyguardUpdateMonitor.this.handleScreenTurnedOn();
                        return;
                    case 332:
                        Trace.beginSection("KeyguardUpdateMonitor#handler MSG_SCREEN_TURNED_ON");
                        KeyguardUpdateMonitor.this.handleScreenTurnedOff();
                        Trace.endSection();
                        return;
                    case 333:
                        KeyguardUpdateMonitor.this.handleDreamingStateChanged(message.arg1);
                        return;
                    case 334:
                        KeyguardUpdateMonitor.this.handleUserUnlocked(message.arg1);
                        return;
                    case 335:
                        KeyguardUpdateMonitor.this.setAssistantVisible(((Boolean) message.obj).booleanValue());
                        return;
                    case 336:
                        KeyguardUpdateMonitor.this.updateBiometricListeningState();
                        return;
                    case 337:
                        KeyguardUpdateMonitor.this.updateLogoutEnabled();
                        return;
                    case 338:
                        KeyguardUpdateMonitor.this.updateTelephonyCapable(((Boolean) message.obj).booleanValue());
                        return;
                    case 339:
                        KeyguardUpdateMonitor.this.handleTimeZoneUpdate((String) message.obj);
                        return;
                    case 340:
                        KeyguardUpdateMonitor.this.handleUserStopped(message.arg1);
                        return;
                    case 341:
                        KeyguardUpdateMonitor.this.handleUserRemoved(message.arg1);
                        return;
                    case 342:
                        KeyguardUpdateMonitor.this.handleKeyguardGoingAway(((Boolean) message.obj).booleanValue());
                        return;
                }
            }
        };
        init();
        if (!this.mDeviceProvisioned) {
            watchForDeviceProvisioning();
        }
        if (OpKeyguardUpdateMonitor.IS_SUPPORT_FACE_UNLOCK) {
            watchForFacelockSettings();
        }
        this.mBatteryStatus = new OpBatteryStatus(1, 100, 0, 0, 0, 0, false, false, false, false);
        this.mAodAlwaysOnController.init(this, this.mHandler);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentFilter.addAction("android.intent.action.AIRPLANE_MODE");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.SERVICE_STATE");
        intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.ONEPLUS_CHARGE_TIME_ENABLE");
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mBroadcastReceiver, intentFilter, this.mHandler);
        this.mBackgroundExecutor.execute(new Runnable() { // from class: com.android.keyguard.-$$Lambda$KeyguardUpdateMonitor$Nf7eL_mU0R406vfJ5bZtFnV99-Q
            @Override // java.lang.Runnable
            public final void run() {
                KeyguardUpdateMonitor.this.lambda$new$1$KeyguardUpdateMonitor();
            }
        });
        this.mHandler.post(new Runnable() { // from class: com.android.keyguard.-$$Lambda$KeyguardUpdateMonitor$hhywwBjwxz_vxvVm2yL-5SB8SzM
            @Override // java.lang.Runnable
            public final void run() {
                KeyguardUpdateMonitor.this.registerRingerTracker();
            }
        });
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.USER_INFO_CHANGED");
        intentFilter2.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        intentFilter2.addAction("com.android.facelock.FACE_UNLOCK_STARTED");
        intentFilter2.addAction("com.android.facelock.FACE_UNLOCK_STOPPED");
        intentFilter2.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        intentFilter2.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter2.addAction("android.intent.action.USER_STOPPED");
        intentFilter2.addAction("android.intent.action.USER_REMOVED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mBroadcastAllReceiver, intentFilter2, this.mHandler, UserHandle.ALL);
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mSubscriptionListener);
        try {
            ActivityManager.getService().registerUserSwitchObserver(this.mUserSwitchObserver, "KeyguardUpdateMonitor");
        } catch (RemoteException e) {
            e.rethrowAsRuntimeException();
        }
        TrustManager trustManager = (TrustManager) context.getSystemService(TrustManager.class);
        this.mTrustManager = trustManager;
        trustManager.registerTrustListener(this);
        setStrongAuthTracker(this.mStrongAuthTracker);
        this.mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.getService("dreams"));
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.fingerprint")) {
            this.mFpm = (FingerprintManager) context.getSystemService("fingerprint");
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.biometrics.face")) {
            this.mFaceManager = (FaceManager) context.getSystemService("face");
        }
        if (!(this.mFpm == null && this.mFaceManager == null)) {
            BiometricManager biometricManager = (BiometricManager) context.getSystemService(BiometricManager.class);
            this.mBiometricManager = biometricManager;
            biometricManager.registerEnabledOnKeyguardCallback(this.mBiometricEnabledCallback);
        }
        updateBiometricListeningState();
        FingerprintManager fingerprintManager = this.mFpm;
        if (fingerprintManager != null) {
            fingerprintManager.addLockoutResetCallback(this.mFingerprintLockoutResetCallback);
        }
        FaceManager faceManager = this.mFaceManager;
        if (faceManager != null) {
            faceManager.addLockoutResetCallback(this.mFaceLockoutResetCallback);
        }
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
        UserManager userManager = (UserManager) context.getSystemService(UserManager.class);
        this.mUserManager = userManager;
        this.mIsPrimaryUser = userManager.isPrimaryUser();
        int currentUser = ActivityManager.getCurrentUser();
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(DevicePolicyManager.class);
        this.mDevicePolicyManager = devicePolicyManager;
        this.mLogoutEnabled = devicePolicyManager.isLogoutEnabled();
        updateSecondaryLockscreenRequirement(currentUser);
        for (UserInfo userInfo : this.mUserManager.getUsers()) {
            SparseBooleanArray sparseBooleanArray = this.mUserTrustIsUsuallyManaged;
            int i = userInfo.id;
            sparseBooleanArray.put(i, this.mTrustManager.isTrustUsuallyManaged(i));
            SparseBooleanArray sparseBooleanArray2 = this.mUserIsUnlocked;
            int i2 = userInfo.id;
            sparseBooleanArray2.put(i2, this.mUserManager.isUserUnlocked(i2));
        }
        updateAirplaneModeState();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mTelephonyManager = telephonyManager;
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 4194304);
            for (int i3 = 0; i3 < this.mTelephonyManager.getActiveModemCount(); i3++) {
                int simState = this.mTelephonyManager.getSimState(i3);
                int[] subscriptionIds = this.mSubscriptionManager.getSubscriptionIds(i3);
                if (subscriptionIds != null) {
                    for (int i4 : subscriptionIds) {
                        this.mHandler.obtainMessage(304, i4, i3, Integer.valueOf(simState)).sendToTarget();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$KeyguardUpdateMonitor() {
        Intent registerReceiver;
        int defaultSubscriptionId = SubscriptionManager.getDefaultSubscriptionId();
        ServiceState serviceStateForSubscriber = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getServiceStateForSubscriber(defaultSubscriptionId);
        OpKeyguardUpdateMonitor.OpHandler opHandler = this.mHandler;
        opHandler.sendMessage(opHandler.obtainMessage(330, defaultSubscriptionId, 0, serviceStateForSubscriber));
        if (this.mBatteryStatus == null && (registerReceiver = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) != null && this.mBatteryStatus == null) {
            this.mBroadcastReceiver.onReceive(this.mContext, registerReceiver);
        }
    }

    private void updateAirplaneModeState() {
        if (WirelessUtils.isAirplaneModeOn(this.mContext) && !this.mHandler.hasMessages(329)) {
            this.mHandler.sendEmptyMessage(329);
        }
    }

    public void updateBiometricListeningState() {
        updateFingerprintListeningState();
        updateFaceListeningState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFingerprintListeningState() {
        if (!this.mHandler.hasMessages(336)) {
            if (this.mDuringAcquired) {
                Log.d("KeyguardUpdateMonitor", "not update fp listen state during acquired");
                return;
            }
            this.mHandler.removeCallbacks(this.mRetryFingerprintAuthentication);
            boolean shouldListenForFingerprint = shouldListenForFingerprint();
            int i = this.mFingerprintRunningState;
            boolean z = DEBUG;
            if (!(i == 1 || i == 3)) {
                z = false;
            }
            Log.d("KeyguardUpdateMonitor", "updateFP: " + shouldListenForFingerprint + ", " + z + ", " + this.mFingerprintRunningState);
            if (z && !shouldListenForFingerprint) {
                stopListeningForFingerprint();
            } else if (!z && shouldListenForFingerprint) {
                startListeningForFingerprint();
            }
        }
    }

    public boolean isUserUnlocked(int i) {
        return this.mUserIsUnlocked.get(i);
    }

    public void onAuthInterruptDetected(boolean z) {
        Log.d("KeyguardUpdateMonitor", "onAuthInterruptDetected(" + z + ")");
        if (this.mAuthInterruptActive != z) {
            this.mAuthInterruptActive = z;
            updateFaceListeningState();
        }
    }

    public void requestFaceAuth() {
        Log.d("KeyguardUpdateMonitor", "requestFaceAuth()");
        updateFaceListeningState();
    }

    public void cancelFaceAuth() {
        stopListeningForFace();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void updateFaceListeningState() {
        if (!this.mHandler.hasMessages(336)) {
            this.mHandler.removeCallbacks(this.mRetryFaceAuthentication);
            boolean shouldListenForFace = shouldListenForFace();
            if (this.mFaceRunningState == 1 && !shouldListenForFace) {
                stopListeningForFace();
            } else if (this.mFaceRunningState != 1 && shouldListenForFace) {
                startListeningForFace();
            }
        }
    }

    private boolean shouldListenForFaceAssistant() {
        BiometricAuthenticated biometricAuthenticated = this.mUserFaceAuthenticated.get(getCurrentUser());
        if (!this.mAssistantVisible || !this.mKeyguardOccluded) {
            return false;
        }
        if ((biometricAuthenticated == null || !biometricAuthenticated.mAuthenticated) && !this.mUserHasTrust.get(getCurrentUser(), false)) {
            return DEBUG;
        }
        return false;
    }

    private boolean shouldListenForFingerprint() {
        if (this.mFingerprintLockedOut && this.mBouncer) {
            boolean z = this.mCredentialAttempted;
        }
        boolean z2 = (this.mKeyguardIsVisible || !this.mDeviceInteractive || ((this.mBouncer && !this.mKeyguardGoingAway) || this.mGoingToSleep || (this.mKeyguardOccluded && this.mIsDreaming))) && !this.mSwitchingUser && !isFingerprintDisabled(getCurrentUser()) && (!this.mKeyguardGoingAway || !this.mDeviceInteractive) && this.mIsPrimaryUser;
        Log.d("KeyguardUpdateMonitor", "shouldListen: " + z2 + ", vis:" + this.mKeyguardIsVisible + ", inter:" + this.mDeviceInteractive + ", bouncer:" + this.mBouncer + ", going:" + this.mKeyguardGoingAway + ", sleep:" + this.mGoingToSleep + ", occlude:" + this.mKeyguardOccluded + ", dream:" + this.mIsDreaming + ", switch:" + this.mSwitchingUser + ", disabled:" + isFingerprintDisabled(getCurrentUser()) + ", primary:" + this.mIsPrimaryUser);
        if (!opShouldListenForFingerprint() || !z2) {
            return false;
        }
        return DEBUG;
    }

    public boolean shouldListenForFace() {
        boolean z = false;
        boolean z2 = this.mKeyguardIsVisible && this.mDeviceInteractive && !this.mGoingToSleep && !(this.mStatusBarStateController.getState() == 2);
        int currentUser = getCurrentUser();
        int strongAuthForUser = this.mStrongAuthTracker.getStrongAuthForUser(currentUser);
        boolean z3 = containsFlag(strongAuthForUser, 2) || containsFlag(strongAuthForUser, 32);
        boolean z4 = containsFlag(strongAuthForUser, 1) || containsFlag(strongAuthForUser, 16);
        KeyguardBypassController keyguardBypassController = this.mKeyguardBypassController;
        boolean z5 = keyguardBypassController != null && keyguardBypassController.canBypass();
        boolean z6 = !getUserCanSkipBouncer(currentUser) || z5;
        boolean z7 = (!z4 || (z5 && !this.mBouncer)) && !z3;
        if ((this.mBouncer || this.mAuthInterruptActive || z2 || shouldListenForFaceAssistant()) && !this.mSwitchingUser && !isFaceDisabled(currentUser) && z6 && !this.mKeyguardGoingAway && this.mFaceSettingEnabledForUser.get(currentUser) && !this.mLockIconPressed && z7 && this.mIsPrimaryUser && !this.mSecureCameraLaunched) {
            z = true;
        }
        if (DEBUG_FACE) {
            maybeLogFaceListenerModelData(new KeyguardFaceListenModel(System.currentTimeMillis(), currentUser, z, this.mBouncer, this.mAuthInterruptActive, z2, shouldListenForFaceAssistant(), this.mSwitchingUser, isFaceDisabled(currentUser), z6, this.mKeyguardGoingAway, this.mFaceSettingEnabledForUser.get(currentUser), this.mLockIconPressed, z7, this.mIsPrimaryUser, this.mSecureCameraLaunched));
        }
        return z;
    }

    private void maybeLogFaceListenerModelData(KeyguardFaceListenModel keyguardFaceListenModel) {
        if (DEBUG_FACE && this.mFaceRunningState != 1 && keyguardFaceListenModel.isListeningForFace()) {
            if (this.mFaceListenModels == null) {
                this.mFaceListenModels = new ArrayDeque<>(20);
            }
            if (this.mFaceListenModels.size() >= 20) {
                this.mFaceListenModels.remove();
            }
            this.mFaceListenModels.add(keyguardFaceListenModel);
        }
    }

    public void onLockIconPressed() {
        this.mLockIconPressed = DEBUG;
        int currentUser = getCurrentUser();
        this.mUserFaceAuthenticated.put(currentUser, null);
        notifyFacelockStateChanged(0);
        updateFaceListeningState();
        this.mStrongAuthTracker.onStrongAuthRequiredChanged(currentUser);
    }

    private void startListeningForFingerprint() {
        Log.d("KeyguardUpdateMonitor", "startListeningForFingerprint(), " + this.mFingerprintRunningState);
        int i = this.mFingerprintRunningState;
        if (i == 2) {
            setFingerprintRunningState(3);
        } else if (i != 3) {
            int currentUser = getCurrentUser();
            if (isUnlockWithFingerprintPossible(currentUser)) {
                CancellationSignal cancellationSignal = this.mFingerprintCancelSignal;
                if (cancellationSignal != null) {
                    cancellationSignal.cancel();
                }
                CancellationSignal cancellationSignal2 = new CancellationSignal();
                this.mFingerprintCancelSignal = cancellationSignal2;
                this.mFpm.authenticate(null, cancellationSignal2, 0, this.mFingerprintAuthenticationCallback, null, currentUser);
                setFingerprintRunningState(1);
            }
        }
    }

    private void startListeningForFace() {
        Log.d("KeyguardUpdateMonitor", "startListeningForFace, " + this.mFaceRunningState);
        if (this.mFaceRunningState == 2) {
            setFaceRunningState(3);
            return;
        }
        int currentUser = getCurrentUser();
        if (isUnlockWithFacePossible(currentUser)) {
            CancellationSignal cancellationSignal = this.mFaceCancelSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
            CancellationSignal cancellationSignal2 = new CancellationSignal();
            this.mFaceCancelSignal = cancellationSignal2;
            this.mFaceManager.authenticate((CryptoObject) null, cancellationSignal2, 0, this.mFaceAuthenticationCallback, (Handler) null, currentUser);
            setFaceRunningState(1);
        }
    }

    public boolean isUnlockingWithBiometricsPossible(int i) {
        if (isUnlockWithFacePossible(i) || isUnlockWithFingerprintPossible(i)) {
            return DEBUG;
        }
        return false;
    }

    public boolean isUnlockWithFingerprintPossible(int i) {
        FingerprintManager fingerprintManager = this.mFpm;
        if (fingerprintManager == null || !fingerprintManager.isHardwareDetected() || isFingerprintDisabled(i) || this.mFpm.getEnrolledFingerprints(i).size() <= 0) {
            return false;
        }
        return DEBUG;
    }

    private boolean isUnlockWithFacePossible(int i) {
        if (!isFaceAuthEnabledForUser(i) || isFaceDisabled(i)) {
            return false;
        }
        return DEBUG;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$isFaceAuthEnabledForUser$2 */
    public /* synthetic */ Boolean lambda$isFaceAuthEnabledForUser$2$KeyguardUpdateMonitor(int i) {
        FaceManager faceManager = this.mFaceManager;
        return Boolean.valueOf((faceManager == null || !faceManager.isHardwareDetected() || !this.mFaceManager.hasEnrolledTemplates(i) || !this.mFaceSettingEnabledForUser.get(i)) ? false : DEBUG);
    }

    public boolean isFaceAuthEnabledForUser(int i) {
        return ((Boolean) DejankUtils.whitelistIpcs(new Supplier(i) { // from class: com.android.keyguard.-$$Lambda$KeyguardUpdateMonitor$L_ayq_delmLzhYZy-R4IglXGLtE
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return KeyguardUpdateMonitor.this.lambda$isFaceAuthEnabledForUser$2$KeyguardUpdateMonitor(this.f$1);
            }
        })).booleanValue();
    }

    private void stopListeningForFingerprint() {
        Log.v("KeyguardUpdateMonitor", "stopListeningForFingerprint()");
        if (this.mFingerprintRunningState == 1) {
            CancellationSignal cancellationSignal = this.mFingerprintCancelSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
                this.mFingerprintCancelSignal = null;
                if (!this.mHandler.hasCallbacks(this.mCancelNotReceived)) {
                    this.mHandler.postDelayed(this.mCancelNotReceived, 3000);
                }
            }
            setFingerprintRunningState(2);
        }
        if (this.mFingerprintRunningState == 3) {
            setFingerprintRunningState(2);
        }
    }

    private void stopListeningForFace() {
        Log.v("KeyguardUpdateMonitor", "stopListeningForFace()");
        if (this.mFaceRunningState == 1) {
            CancellationSignal cancellationSignal = this.mFaceCancelSignal;
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
                this.mFaceCancelSignal = null;
                if (!this.mHandler.hasCallbacks(this.mCancelNotReceived)) {
                    this.mHandler.postDelayed(this.mCancelNotReceived, 3000);
                }
            }
            setFaceRunningState(2);
        }
        if (this.mFaceRunningState == 3) {
            setFaceRunningState(2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDeviceProvisionedInSettingsDb() {
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            return DEBUG;
        }
        return false;
    }

    private void watchForDeviceProvisioning() {
        this.mDeviceProvisionedObserver = new ContentObserver(this.mHandler) { // from class: com.android.keyguard.KeyguardUpdateMonitor.17
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                super.onChange(z);
                KeyguardUpdateMonitor keyguardUpdateMonitor = KeyguardUpdateMonitor.this;
                keyguardUpdateMonitor.mDeviceProvisioned = keyguardUpdateMonitor.isDeviceProvisionedInSettingsDb();
                if (KeyguardUpdateMonitor.this.mDeviceProvisioned) {
                    KeyguardUpdateMonitor.this.mHandler.sendEmptyMessage(308);
                }
                Log.d("KeyguardUpdateMonitor", "DEVICE_PROVISIONED state = " + KeyguardUpdateMonitor.this.mDeviceProvisioned);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this.mDeviceProvisionedObserver);
        boolean isDeviceProvisionedInSettingsDb = isDeviceProvisionedInSettingsDb();
        if (isDeviceProvisionedInSettingsDb != this.mDeviceProvisioned) {
            this.mDeviceProvisioned = isDeviceProvisionedInSettingsDb;
            if (isDeviceProvisionedInSettingsDb) {
                this.mHandler.sendEmptyMessage(308);
            }
        }
    }

    public void setHasLockscreenWallpaper(boolean z) {
        Assert.isMainThread();
        if (z != this.mHasLockscreenWallpaper) {
            this.mHasLockscreenWallpaper = z;
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onHasLockscreenWallpaperChanged(z);
                }
            }
        }
    }

    public boolean hasLockscreenWallpaper() {
        return this.mHasLockscreenWallpaper;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDevicePolicyManagerStateChanged(int i) {
        Assert.isMainThread();
        updateFingerprintListeningState();
        updateSecondaryLockscreenRequirement(i);
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDevicePolicyManagerStateChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleUserSwitching(int i, IRemoteCallback iRemoteCallback) {
        Assert.isMainThread();
        clearBiometricRecognized();
        this.mUserTrustIsUsuallyManaged.put(i, this.mTrustManager.isTrustUsuallyManaged(i));
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserSwitching(i);
            }
        }
        try {
            iRemoteCallback.sendResult((Bundle) null);
        } catch (RemoteException unused) {
        }
        opHandleUserSwitch(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUserSwitchComplete(int i) {
        Assert.isMainThread();
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onUserSwitchComplete(i);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDeviceProvisioned() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onDeviceProvisioned();
            }
        }
        if (this.mDeviceProvisionedObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDeviceProvisionedObserver);
            this.mDeviceProvisionedObserver = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePhoneStateChanged(String str) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handlePhoneStateChanged(" + str + ")");
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(str)) {
            this.mPhoneState = 0;
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(str)) {
            this.mPhoneState = 2;
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(str)) {
            this.mPhoneState = 1;
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onPhoneStateChanged(this.mPhoneState);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRingerModeChange(int i) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handleRingerModeChange(" + i + ")");
        this.mRingMode = i;
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onRingerModeChanged(i);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTimeUpdate() {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handleTimeUpdate");
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTimeChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTimeZoneUpdate(String str) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handleTimeZoneUpdate");
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onTimeZoneChanged(TimeZone.getTimeZone(str));
                keyguardUpdateMonitorCallback.onTimeChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBatteryUpdate(OpBatteryStatus opBatteryStatus) {
        Assert.isMainThread();
        boolean isBatteryUpdateInteresting = isBatteryUpdateInteresting(this.mBatteryStatus, opBatteryStatus);
        if (OpKeyguardUpdateMonitor.DEBUG) {
            Log.d("KeyguardUpdateMonitor", "handleBatteryUpdate, batteryUpdateInteresting:" + isBatteryUpdateInteresting);
        }
        this.mBatteryStatus = opBatteryStatus;
        if (isBatteryUpdateInteresting) {
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onRefreshBatteryInfo(opBatteryStatus);
                }
            }
        }
        this.mFocusUpdateBatteryInfo = false;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateTelephonyCapable(boolean z) {
        Assert.isMainThread();
        if (z != this.mTelephonyCapable) {
            this.mTelephonyCapable = z;
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onTelephonyCapable(this.mTelephonyCapable);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0061  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00d4  */
    @com.android.internal.annotations.VisibleForTesting
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleSimStateChange(int r9, int r10, int r11) {
        /*
            r8 = this;
            com.android.systemui.util.Assert.isMainThread()
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "handleSimStateChange(subId="
            r0.append(r1)
            r0.append(r9)
            java.lang.String r1 = ", slotId="
            r0.append(r1)
            r0.append(r10)
            java.lang.String r1 = ", state="
            r0.append(r1)
            r0.append(r11)
            java.lang.String r1 = ")"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "KeyguardUpdateMonitor"
            android.util.Log.d(r1, r0)
            boolean r0 = android.telephony.SubscriptionManager.isValidSubscriptionId(r9)
            r2 = 0
            r3 = 1
            if (r0 != 0) goto L_0x0050
            java.lang.String r0 = "invalid subId in handleSimStateChange()"
            android.util.Log.w(r1, r0)
            if (r11 != r3) goto L_0x0047
            r8.updateTelephonyCapable(r3)
            java.util.HashMap<java.lang.Integer, com.android.keyguard.KeyguardUpdateMonitor$SimData> r0 = r8.mSimDatas
            r0.clear()
            r0 = r3
            goto L_0x0051
        L_0x0047:
            r0 = 8
            if (r11 != r0) goto L_0x004f
            r8.updateTelephonyCapable(r3)
            goto L_0x0050
        L_0x004f:
            return
        L_0x0050:
            r0 = r2
        L_0x0051:
            java.util.HashMap<java.lang.Integer, com.android.keyguard.KeyguardUpdateMonitor$SimData> r4 = r8.mSimDatas
            java.lang.Integer r5 = java.lang.Integer.valueOf(r9)
            java.lang.Object r4 = r4.get(r5)
            com.android.keyguard.KeyguardUpdateMonitor$SimData r4 = (com.android.keyguard.KeyguardUpdateMonitor.SimData) r4
            java.lang.String r5 = ", "
            if (r4 != 0) goto L_0x0090
            com.android.keyguard.KeyguardUpdateMonitor$SimData r4 = new com.android.keyguard.KeyguardUpdateMonitor$SimData
            r4.<init>(r11, r10, r9)
            java.util.HashMap<java.lang.Integer, com.android.keyguard.KeyguardUpdateMonitor$SimData> r6 = r8.mSimDatas
            java.lang.Integer r7 = java.lang.Integer.valueOf(r9)
            r6.put(r7, r4)
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r6 = "init SimData: "
            r4.append(r6)
            r4.append(r9)
            r4.append(r5)
            r4.append(r10)
            r4.append(r5)
            r4.append(r11)
            java.lang.String r4 = r4.toString()
            android.util.Log.d(r1, r4)
            goto L_0x00c6
        L_0x0090:
            int r6 = r4.simState
            if (r6 != r11) goto L_0x009e
            int r6 = r4.subId
            if (r6 != r9) goto L_0x009e
            int r6 = r4.slotId
            if (r6 == r10) goto L_0x009d
            goto L_0x009e
        L_0x009d:
            r3 = r2
        L_0x009e:
            r4.simState = r11
            r4.subId = r9
            r4.slotId = r10
            if (r3 == 0) goto L_0x00c6
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r6 = "change SimData: "
            r4.append(r6)
            r4.append(r9)
            r4.append(r5)
            r4.append(r10)
            r4.append(r5)
            r4.append(r11)
            java.lang.String r4 = r4.toString()
            android.util.Log.d(r1, r4)
        L_0x00c6:
            if (r3 != 0) goto L_0x00ca
            if (r0 == 0) goto L_0x00ea
        L_0x00ca:
            if (r11 == 0) goto L_0x00ea
        L_0x00cc:
            java.util.ArrayList<java.lang.ref.WeakReference<com.android.keyguard.KeyguardUpdateMonitorCallback>> r0 = r8.mCallbacks
            int r0 = r0.size()
            if (r2 >= r0) goto L_0x00ea
            java.util.ArrayList<java.lang.ref.WeakReference<com.android.keyguard.KeyguardUpdateMonitorCallback>> r0 = r8.mCallbacks
            java.lang.Object r0 = r0.get(r2)
            java.lang.ref.WeakReference r0 = (java.lang.ref.WeakReference) r0
            java.lang.Object r0 = r0.get()
            com.android.keyguard.KeyguardUpdateMonitorCallback r0 = (com.android.keyguard.KeyguardUpdateMonitorCallback) r0
            if (r0 == 0) goto L_0x00e7
            r0.onSimStateChanged(r9, r10, r11)
        L_0x00e7:
            int r2 = r2 + 1
            goto L_0x00cc
        L_0x00ea:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardUpdateMonitor.handleSimStateChange(int, int, int):void");
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void handleServiceStateChange(int i, ServiceState serviceState) {
        Log.d("KeyguardUpdateMonitor", "handleServiceStateChange(subId=" + i + ", serviceState=" + serviceState);
        int i2 = 0;
        if (!SubscriptionManager.isValidSubscriptionId(i)) {
            Log.w("KeyguardUpdateMonitor", "invalid subId in handleServiceStateChange()");
            this.mServiceStateWhenNoSim = serviceState;
            while (i2 < this.mCallbacks.size()) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onServiceStateChanged(i, serviceState);
                }
                i2++;
            }
            return;
        }
        updateTelephonyCapable(DEBUG);
        this.mServiceStates.put(Integer.valueOf(i), serviceState);
        while (i2 < this.mCallbacks.size()) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback2 = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback2 != null) {
                keyguardUpdateMonitorCallback2.onRefreshCarrierInfo();
                keyguardUpdateMonitorCallback2.onServiceStateChanged(i, serviceState);
            }
            i2++;
        }
    }

    public boolean isKeyguardVisible() {
        return this.mKeyguardIsVisible;
    }

    public void onKeyguardVisibilityChanged(boolean z) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "onKeyguardVisibilityChanged(" + z + ")");
        this.mKeyguardIsVisible = z;
        if (z) {
            this.mSecureCameraLaunched = false;
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardVisibilityChangedRaw(z);
            }
        }
        opOnKeyguardVisibilityChanged(z);
        updateBiometricListeningState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKeyguardReset() {
        Log.d("KeyguardUpdateMonitor", "handleKeyguardReset");
        updateBiometricListeningState();
        this.mNeedsSlowUnlockTransition = resolveNeedsSlowUnlockTransition();
        this.mIsLaunchingEmergencyCall = false;
    }

    private boolean resolveNeedsSlowUnlockTransition() {
        if (isUserUnlocked(getCurrentUser())) {
            return false;
        }
        return FALLBACK_HOME_COMPONENT.equals(this.mContext.getPackageManager().resolveActivity(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME"), 0).getComponentInfo().getComponentName());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKeyguardBouncerChanged(int i) {
        Assert.isMainThread();
        Log.d("KeyguardUpdateMonitor", "handleKeyguardBouncerChanged(" + i + ")");
        boolean z = DEBUG;
        if (i != 1) {
            z = false;
        }
        this.mBouncer = z;
        if (z) {
            this.mSecureCameraLaunched = false;
        } else {
            this.mCredentialAttempted = false;
        }
        for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i2).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onKeyguardBouncerChanged(z);
            }
        }
        if (z) {
            updateLaunchingLeftAffordance(false);
        }
        updateBiometricListeningState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReportEmergencyCallAction() {
        Assert.isMainThread();
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onEmergencyCallAction();
            }
        }
        this.mIsLaunchingEmergencyCall = DEBUG;
    }

    public boolean isLaunchingEmergencyCall() {
        return this.mIsLaunchingEmergencyCall;
    }

    private boolean isBatteryUpdateInteresting(OpBatteryStatus opBatteryStatus, OpBatteryStatus opBatteryStatus2) {
        if (this.mFocusUpdateBatteryInfo) {
            Log.d("KeyguardUpdateMonitor", " mFocusUpdateBatteryInfo");
            return DEBUG;
        }
        boolean isPluggedIn = opBatteryStatus2.isPluggedIn();
        boolean isPluggedIn2 = opBatteryStatus.isPluggedIn();
        boolean z = isPluggedIn2 && isPluggedIn && opBatteryStatus.status != opBatteryStatus2.status;
        if (isPluggedIn2 != isPluggedIn || z || opBatteryStatus.level != opBatteryStatus2.level) {
            return DEBUG;
        }
        if (isPluggedIn && opBatteryStatus2.maxChargingWattage != opBatteryStatus.maxChargingWattage) {
            return DEBUG;
        }
        if ((!isPluggedIn || opBatteryStatus2.fastCharge == opBatteryStatus.fastCharge) && opBatteryStatus.wirelessChargingDeviated == opBatteryStatus2.wirelessChargingDeviated && opBatteryStatus.wirelessCharging == opBatteryStatus2.wirelessCharging && opBatteryStatus.wirelessWarpCharging == opBatteryStatus2.wirelessWarpCharging && opBatteryStatus.getSwarpRemainingTime() == opBatteryStatus2.getSwarpRemainingTime()) {
            return false;
        }
        return DEBUG;
    }

    public void removeCallback(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        Assert.isMainThread();
        Log.v("KeyguardUpdateMonitor", "*** unregister callback for " + keyguardUpdateMonitorCallback);
        this.mCallbacks.removeIf(new Predicate() { // from class: com.android.keyguard.-$$Lambda$KeyguardUpdateMonitor$BtuCi3IGxUtdSY1jcMIdW6mFmhs
            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return KeyguardUpdateMonitor.lambda$removeCallback$3(KeyguardUpdateMonitorCallback.this, (WeakReference) obj);
            }
        });
    }

    static /* synthetic */ boolean lambda$removeCallback$3(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback, WeakReference weakReference) {
        if (weakReference.get() == keyguardUpdateMonitorCallback) {
            return DEBUG;
        }
        return false;
    }

    public void registerCallback(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        Assert.isMainThread();
        Log.v("KeyguardUpdateMonitor", "*** register callback for " + keyguardUpdateMonitorCallback);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            if (this.mCallbacks.get(i).get() == keyguardUpdateMonitorCallback) {
                Log.e("KeyguardUpdateMonitor", "Object tried to add another callback", new Exception("Called by"));
                return;
            }
        }
        this.mCallbacks.add(new WeakReference<>(keyguardUpdateMonitorCallback));
        removeCallback(null);
        sendUpdates(keyguardUpdateMonitorCallback);
    }

    public void setKeyguardBypassController(KeyguardBypassController keyguardBypassController) {
        this.mKeyguardBypassController = keyguardBypassController;
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitor
    public boolean isSwitchingUser() {
        return this.mSwitchingUser;
    }

    public void setSwitchingUser(boolean z) {
        this.mSwitchingUser = z;
        this.mHandler.post(this.mUpdateBiometricListeningState);
    }

    private void sendUpdates(KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback) {
        keyguardUpdateMonitorCallback.onRefreshBatteryInfo(this.mBatteryStatus);
        keyguardUpdateMonitorCallback.onTimeChanged();
        keyguardUpdateMonitorCallback.onRingerModeChanged(this.mRingMode);
        keyguardUpdateMonitorCallback.onPhoneStateChanged(this.mPhoneState);
        keyguardUpdateMonitorCallback.onRefreshCarrierInfo();
        keyguardUpdateMonitorCallback.onClockVisibilityChanged();
        keyguardUpdateMonitorCallback.onKeyguardVisibilityChangedRaw(this.mKeyguardIsVisible);
        keyguardUpdateMonitorCallback.onTelephonyCapable(this.mTelephonyCapable);
        for (Map.Entry<Integer, SimData> entry : this.mSimDatas.entrySet()) {
            SimData value = entry.getValue();
            keyguardUpdateMonitorCallback.onSimStateChanged(value.subId, value.slotId, value.simState);
        }
    }

    public void sendKeyguardReset() {
        this.mHandler.obtainMessage(312).sendToTarget();
    }

    public void sendKeyguardBouncerChanged(boolean z) {
        Log.d("KeyguardUpdateMonitor", "sendKeyguardBouncerChanged(" + z + ")");
        Message obtainMessage = this.mHandler.obtainMessage(322);
        obtainMessage.arg1 = z ? 1 : 0;
        obtainMessage.sendToTarget();
    }

    public void reportSimUnlocked(int i) {
        opReportSimUnlocked(i);
    }

    public void reportEmergencyCallAction(boolean z) {
        if (!z) {
            this.mHandler.obtainMessage(318).sendToTarget();
            return;
        }
        Assert.isMainThread();
        handleReportEmergencyCallAction();
    }

    public boolean isDeviceProvisioned() {
        return this.mDeviceProvisioned;
    }

    public ServiceState getServiceState(int i) {
        return this.mServiceStates.get(Integer.valueOf(i));
    }

    public void clearBiometricRecognized() {
        Assert.isMainThread();
        this.mUserFingerprintAuthenticated.clear();
        this.mUserFaceAuthenticated.clear();
        this.mTrustManager.clearAllBiometricRecognized(BiometricSourceType.FINGERPRINT);
        this.mTrustManager.clearAllBiometricRecognized(BiometricSourceType.FACE);
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
            if (keyguardUpdateMonitorCallback != null) {
                keyguardUpdateMonitorCallback.onBiometricsCleared();
            }
        }
    }

    public boolean isSimPinVoiceSecure() {
        return isSimPinSecure();
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitor
    public boolean isSimPinSecure() {
        for (SubscriptionInfo subscriptionInfo : getSubscriptionInfo(false)) {
            if (isSimPinSecure(getSimState(subscriptionInfo.getSubscriptionId()))) {
                return DEBUG;
            }
        }
        return false;
    }

    public int getSimState(int i) {
        if (this.mSimDatas.containsKey(Integer.valueOf(i))) {
            return this.mSimDatas.get(Integer.valueOf(i)).simState;
        }
        return 0;
    }

    private int getSlotId(int i) {
        if (!this.mSimDatas.containsKey(Integer.valueOf(i))) {
            refreshSimState(i, SubscriptionManager.getSlotIndex(i));
        }
        return this.mSimDatas.get(Integer.valueOf(i)).slotId;
    }

    public int getSimSlotId(int i) {
        if (this.mSimDatas.containsKey(Integer.valueOf(i))) {
            return this.mSimDatas.get(Integer.valueOf(i)).slotId;
        }
        Log.w("KeyguardUpdateMonitor", "invalid subid not in keyguard");
        return -1;
    }

    public boolean isOOS() {
        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
        boolean z = true;
        for (int i = 0; i < phoneCount; i++) {
            int[] subId = SubscriptionManager.getSubId(i);
            if (subId == null || subId.length < 1) {
                Log.d("KeyguardUpdateMonitor", "no valid SIM");
                ServiceState serviceState = this.mServiceStateWhenNoSim;
                if (serviceState == null) {
                    serviceState = this.mContext.getSystemService(TelephonyManager.class) != null ? ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getServiceStateForSubscriber(-1) : null;
                }
                if (serviceState != null) {
                    if (serviceState.isEmergencyOnly()) {
                        z = false;
                    }
                    if (!(serviceState.getVoiceRegState() == 1 || serviceState.getVoiceRegState() == 3)) {
                        z = false;
                    }
                    Log.d("KeyguardUpdateMonitor", "mServiceStateWhenNoSim is emergency: " + serviceState.isEmergencyOnly());
                    Log.d("KeyguardUpdateMonitor", "mServiceStateWhenNoSim voice state: " + serviceState.getVoiceRegState());
                } else {
                    Log.d("KeyguardUpdateMonitor", "mServiceStateWhenNoSim is NULL");
                }
            } else {
                Log.d("KeyguardUpdateMonitor", "slot id:" + i + " subId:" + subId[0]);
                ServiceState serviceState2 = this.mServiceStates.get(Integer.valueOf(subId[0]));
                if (serviceState2 != null) {
                    if (serviceState2.isEmergencyOnly()) {
                        z = false;
                    }
                    if (!(serviceState2.getVoiceRegState() == 1 || serviceState2.getVoiceRegState() == 3)) {
                        z = false;
                    }
                    Log.d("KeyguardUpdateMonitor", "is emergency: " + serviceState2.isEmergencyOnly());
                    Log.d("KeyguardUpdateMonitor", "voice state: " + serviceState2.getVoiceRegState());
                } else {
                    Log.d("KeyguardUpdateMonitor", "state is NULL");
                }
            }
        }
        Log.d("KeyguardUpdateMonitor", "is Emergency supported: " + z);
        return z;
    }

    private boolean refreshSimState(int i, int i2) {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        boolean z = false;
        int simState = telephonyManager != null ? telephonyManager.getSimState(i2) : 0;
        SimData simData = this.mSimDatas.get(Integer.valueOf(i));
        if (simData == null) {
            this.mSimDatas.put(Integer.valueOf(i), new SimData(simState, i2, i));
            Log.d("KeyguardUpdateMonitor", "init refreshSimState: " + i + ", " + i2 + ", " + simState);
            return DEBUG;
        }
        if (!(simData.simState == simState && simData.slotId == i2)) {
            z = true;
        }
        if (z) {
            Log.d("KeyguardUpdateMonitor", "refresh SimData: " + i + ", old: " + simData.slotId + ", slotId:" + i2 + ", old:" + simData.simState + ", state:" + simState);
        }
        simData.simState = simState;
        simData.slotId = i2;
        return z;
    }

    public void dispatchStartedWakingUp() {
        synchronized (this) {
            this.mDeviceInteractive = DEBUG;
        }
        this.mHandler.sendEmptyMessage(319);
    }

    public void dispatchStartedGoingToSleep(int i) {
        OpKeyguardUpdateMonitor.OpHandler opHandler = this.mHandler;
        opHandler.sendMessage(opHandler.obtainMessage(321, i, 0));
    }

    public void dispatchFinishedGoingToSleep(int i) {
        synchronized (this) {
            this.mDeviceInteractive = false;
        }
        OpKeyguardUpdateMonitor.OpHandler opHandler = this.mHandler;
        opHandler.sendMessage(opHandler.obtainMessage(320, i, 0));
    }

    public void dispatchScreenTurnedOn() {
        synchronized (this) {
            this.mScreenOn = DEBUG;
        }
        this.mHandler.sendEmptyMessage(331);
    }

    public void dispatchScreenTurnedOff() {
        synchronized (this) {
            this.mScreenOn = false;
        }
        this.mHandler.sendEmptyMessage(332);
    }

    public void dispatchDreamingStarted() {
        OpKeyguardUpdateMonitor.OpHandler opHandler = this.mHandler;
        opHandler.sendMessage(opHandler.obtainMessage(333, 1, 0));
    }

    public void dispatchDreamingStopped() {
        OpKeyguardUpdateMonitor.OpHandler opHandler = this.mHandler;
        opHandler.sendMessage(opHandler.obtainMessage(333, 0, 0));
    }

    public void dispatchKeyguardGoingAway(boolean z) {
        OpKeyguardUpdateMonitor.OpHandler opHandler = this.mHandler;
        opHandler.sendMessage(opHandler.obtainMessage(342, Boolean.valueOf(z)));
    }

    public boolean isDeviceInteractive() {
        return this.mDeviceInteractive;
    }

    @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitor
    public boolean isGoingToSleep() {
        return this.mGoingToSleep;
    }

    public int getNextSubIdForState(int i) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        int i2 = -1;
        int i3 = Integer.MAX_VALUE;
        for (int i4 = 0; i4 < subscriptionInfo.size(); i4++) {
            int subscriptionId = subscriptionInfo.get(i4).getSubscriptionId();
            int slotId = getSlotId(subscriptionId);
            if (i == getSimState(subscriptionId) && i3 > slotId) {
                i2 = subscriptionId;
                i3 = slotId;
            }
        }
        return i2;
    }

    public int getUnlockedSubIdForState(int i) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        for (int i2 = 0; i2 < subscriptionInfo.size(); i2++) {
            int subscriptionId = subscriptionInfo.get(i2).getSubscriptionId();
            int slotIndex = SubscriptionManager.getSlotIndex(subscriptionId);
            if (i == 2 || i == 3) {
                Log.i("KeyguardUpdateMonitor", "getUnlockedSubIdForState, id:" + subscriptionId + ", slotId:" + slotIndex + ", state:" + i + ", getSimState(id):" + getSimState(subscriptionId) + ", KeyguardViewMediator.getUnlockTrackSimState(slotId):" + KeyguardViewMediator.getUnlockTrackSimState(slotIndex));
            }
            if (i == getSimState(subscriptionId) && KeyguardViewMediator.getUnlockTrackSimState(slotIndex) != 5) {
                return subscriptionId;
            }
        }
        return -1;
    }

    public SubscriptionInfo getSubscriptionInfoForSubId(int i) {
        List<SubscriptionInfo> subscriptionInfo = getSubscriptionInfo(false);
        for (int i2 = 0; i2 < subscriptionInfo.size(); i2++) {
            SubscriptionInfo subscriptionInfo2 = subscriptionInfo.get(i2);
            if (i == subscriptionInfo2.getSubscriptionId()) {
                return subscriptionInfo2;
            }
        }
        return null;
    }

    public boolean isLogoutEnabled() {
        return this.mLogoutEnabled;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateLogoutEnabled() {
        Assert.isMainThread();
        boolean isLogoutEnabled = this.mDevicePolicyManager.isLogoutEnabled();
        if (this.mLogoutEnabled != isLogoutEnabled) {
            this.mLogoutEnabled = isLogoutEnabled;
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = this.mCallbacks.get(i).get();
                if (keyguardUpdateMonitorCallback != null) {
                    keyguardUpdateMonitorCallback.onLogoutEnabledChanged();
                }
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardUpdateMonitor state:");
        printWriter.println("  SIM States:");
        Iterator<SimData> it = this.mSimDatas.values().iterator();
        while (it.hasNext()) {
            printWriter.println("    " + it.next().toString());
        }
        printWriter.println("  Subs:");
        if (this.mSubscriptionInfo != null) {
            for (int i = 0; i < this.mSubscriptionInfo.size(); i++) {
                printWriter.println("    " + this.mSubscriptionInfo.get(i));
            }
        }
        printWriter.println("  Current active data subId=" + this.mActiveMobileDataSubscription);
        printWriter.println("  Service states:");
        for (Integer num : this.mServiceStates.keySet()) {
            int intValue = num.intValue();
            printWriter.println("    " + intValue + "=" + this.mServiceStates.get(Integer.valueOf(intValue)));
        }
        FingerprintManager fingerprintManager = this.mFpm;
        if (fingerprintManager != null && fingerprintManager.isHardwareDetected()) {
            int currentUser = ActivityManager.getCurrentUser();
            int strongAuthForUser = this.mStrongAuthTracker.getStrongAuthForUser(currentUser);
            BiometricAuthenticated biometricAuthenticated = this.mUserFingerprintAuthenticated.get(currentUser);
            printWriter.println("  Fingerprint state (user=" + currentUser + ")");
            StringBuilder sb = new StringBuilder();
            sb.append("    allowed=");
            sb.append((biometricAuthenticated == null || !isUnlockingWithBiometricAllowed(biometricAuthenticated.mIsStrongBiometric)) ? false : DEBUG);
            printWriter.println(sb.toString());
            StringBuilder sb2 = new StringBuilder();
            sb2.append("    auth'd=");
            sb2.append((biometricAuthenticated == null || !biometricAuthenticated.mAuthenticated) ? false : DEBUG);
            printWriter.println(sb2.toString());
            printWriter.println("    authSinceBoot=" + getStrongAuthTracker().hasUserAuthenticatedSinceBoot());
            printWriter.println("    disabled(DPM)=" + isFingerprintDisabled(currentUser));
            printWriter.println("    possible=" + isUnlockWithFingerprintPossible(currentUser));
            printWriter.println("    listening: actual=" + this.mFingerprintRunningState + " expected=" + (shouldListenForFingerprint() ? 1 : 0));
            StringBuilder sb3 = new StringBuilder();
            sb3.append("    strongAuthFlags=");
            sb3.append(Integer.toHexString(strongAuthForUser));
            printWriter.println(sb3.toString());
            printWriter.println("    trustManaged=" + getUserTrustIsManaged(currentUser));
        }
        FaceManager faceManager = this.mFaceManager;
        if (faceManager != null && faceManager.isHardwareDetected()) {
            int currentUser2 = ActivityManager.getCurrentUser();
            int strongAuthForUser2 = this.mStrongAuthTracker.getStrongAuthForUser(currentUser2);
            BiometricAuthenticated biometricAuthenticated2 = this.mUserFaceAuthenticated.get(currentUser2);
            printWriter.println("  Face authentication state (user=" + currentUser2 + ")");
            StringBuilder sb4 = new StringBuilder();
            sb4.append("    allowed=");
            sb4.append((biometricAuthenticated2 == null || !isUnlockingWithBiometricAllowed(biometricAuthenticated2.mIsStrongBiometric)) ? false : DEBUG);
            printWriter.println(sb4.toString());
            StringBuilder sb5 = new StringBuilder();
            sb5.append("    auth'd=");
            sb5.append((biometricAuthenticated2 == null || !biometricAuthenticated2.mAuthenticated) ? false : DEBUG);
            printWriter.println(sb5.toString());
            printWriter.println("    authSinceBoot=" + getStrongAuthTracker().hasUserAuthenticatedSinceBoot());
            printWriter.println("    disabled(DPM)=" + isFaceDisabled(currentUser2));
            printWriter.println("    possible=" + isUnlockWithFacePossible(currentUser2));
            printWriter.println("    strongAuthFlags=" + Integer.toHexString(strongAuthForUser2));
            printWriter.println("    trustManaged=" + getUserTrustIsManaged(currentUser2));
            printWriter.println("    enabledByUser=" + this.mFaceSettingEnabledForUser.get(currentUser2));
            printWriter.println("    mSecureCameraLaunched=" + this.mSecureCameraLaunched);
        }
        opDump(fileDescriptor, printWriter, strArr);
        ArrayDeque<KeyguardFaceListenModel> arrayDeque = this.mFaceListenModels;
        if (!(arrayDeque == null || arrayDeque.isEmpty())) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
            printWriter.println("  Face listen results (last 20 calls):");
            Iterator<KeyguardFaceListenModel> it2 = this.mFaceListenModels.iterator();
            while (it2.hasNext()) {
                KeyguardFaceListenModel next = it2.next();
                String format = simpleDateFormat.format(new Date(next.getTimeMillis()));
                printWriter.println("    " + format + " " + next.toString());
            }
        }
    }

    public static KeyguardUpdateMonitor getInstance(Context context) {
        return (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
    }

    public int getPhoneState() {
        return this.mPhoneState;
    }
}
