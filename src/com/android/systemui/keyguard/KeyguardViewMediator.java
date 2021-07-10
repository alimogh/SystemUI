package com.android.systemui.keyguard;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.hardware.biometrics.BiometricSourceType;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.EventLog;
import android.util.Log;
import android.util.LogPrinter;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.constraintlayout.widget.R$styleable;
import com.android.internal.policy.IKeyguardDismissCallback;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.internal.util.LatencyTracker;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardDisplayManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.KeyguardViewController;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.C0003R$bool;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.InjectionInflationController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.OpSystemUIInjector;
import com.oneplus.systemui.keyguard.OpKeyguardViewMediator;
import com.oneplus.util.OpUtils;
import com.oneplus.worklife.OPWLBHelper;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
public class KeyguardViewMediator extends OpKeyguardViewMediator implements Dumpable {
    private static final Intent USER_PRESENT_INTENT = new Intent("android.intent.action.USER_PRESENT").addFlags(606076928);
    private static SparseIntArray mUnlockTrackSimStates = new SparseIntArray();
    private AlarmManager mAlarmManager;
    private boolean mAodShowing;
    private AudioManager mAudioManager;
    private boolean mBootCompleted;
    private boolean mBootSendUserPresent;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.keyguard.KeyguardViewMediator.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction())) {
                OPWLBHelper.getInstance(context).sendShutDownBroadcast();
                synchronized (KeyguardViewMediator.this) {
                    KeyguardViewMediator.this.mShuttingDown = true;
                }
            }
        }
    };
    private CharSequence mCustomMessage;
    private final BroadcastReceiver mDelayedLockBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.keyguard.KeyguardViewMediator.4
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD".equals(intent.getAction())) {
                int intExtra = intent.getIntExtra("seq", 0);
                Log.d("KeyguardViewMediator", "received DELAYED_KEYGUARD_ACTION with seq = " + intExtra + ", mDelayedShowingSequence = " + KeyguardViewMediator.this.mDelayedShowingSequence);
                synchronized (KeyguardViewMediator.this) {
                    if (KeyguardViewMediator.this.mDelayedShowingSequence == intExtra) {
                        KeyguardViewMediator.this.doKeyguardLocked(null);
                    }
                }
            } else if ("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK".equals(intent.getAction())) {
                int intExtra2 = intent.getIntExtra("seq", 0);
                int intExtra3 = intent.getIntExtra("android.intent.extra.USER_ID", 0);
                if (intExtra3 != 0) {
                    synchronized (KeyguardViewMediator.this) {
                        if (KeyguardViewMediator.this.mDelayedProfileShowingSequence == intExtra2) {
                            KeyguardViewMediator.this.lockProfile(intExtra3);
                        }
                    }
                }
            }
        }
    };
    private int mDelayedProfileShowingSequence;
    private int mDelayedShowingSequence;
    private DeviceConfigProxy mDeviceConfig;
    private boolean mDeviceInteractive;
    private final DismissCallbackRegistry mDismissCallbackRegistry;
    private boolean mDozing;
    private IKeyguardDrawnCallback mDrawnCallback;
    private IKeyguardExitCallback mExitSecureCallback;
    private boolean mExternallyEnabled = true;
    private final FalsingManager mFalsingManager;
    private boolean mGoingToSleep;
    private OpKeyguardViewMediator.OpHandler mHandler = new OpKeyguardViewMediator.OpHandler(Looper.myLooper(), null, true) { // from class: com.android.systemui.keyguard.KeyguardViewMediator.6
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    KeyguardViewMediator.this.handleShow((Bundle) message.obj);
                    return;
                case 2:
                    KeyguardViewMediator.this.handleHide();
                    return;
                case 3:
                    KeyguardViewMediator.this.handleReset();
                    return;
                case 4:
                    Trace.beginSection("KeyguardViewMediator#handleMessage VERIFY_UNLOCK");
                    KeyguardViewMediator.this.handleVerifyUnlock();
                    Trace.endSection();
                    return;
                case 5:
                    KeyguardViewMediator.this.handleNotifyFinishedGoingToSleep();
                    return;
                case 6:
                    Trace.beginSection("KeyguardViewMediator#handleMessage NOTIFY_SCREEN_TURNING_ON");
                    KeyguardViewMediator.this.handleNotifyScreenTurningOn((IKeyguardDrawnCallback) message.obj);
                    Trace.endSection();
                    return;
                case 7:
                    Trace.beginSection("KeyguardViewMediator#handleMessage KEYGUARD_DONE");
                    KeyguardViewMediator.this.handleKeyguardDone();
                    Trace.endSection();
                    return;
                case 8:
                    Trace.beginSection("KeyguardViewMediator#handleMessage KEYGUARD_DONE_DRAWING");
                    KeyguardViewMediator.this.handleKeyguardDoneDrawing();
                    Trace.endSection();
                    return;
                case 9:
                    Trace.beginSection("KeyguardViewMediator#handleMessage SET_OCCLUDED");
                    KeyguardViewMediator keyguardViewMediator = KeyguardViewMediator.this;
                    boolean z = true;
                    boolean z2 = message.arg1 != 0;
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    keyguardViewMediator.handleSetOccluded(z2, z);
                    Trace.endSection();
                    return;
                case 10:
                    synchronized (KeyguardViewMediator.this) {
                        KeyguardViewMediator.this.doKeyguardLocked((Bundle) message.obj);
                    }
                    return;
                case 11:
                    DismissMessage dismissMessage = (DismissMessage) message.obj;
                    KeyguardViewMediator.this.handleDismiss(dismissMessage.getCallback(), dismissMessage.getMessage());
                    return;
                case 12:
                    Trace.beginSection("KeyguardViewMediator#handleMessage START_KEYGUARD_EXIT_ANIM");
                    StartKeyguardExitAnimParams startKeyguardExitAnimParams = (StartKeyguardExitAnimParams) message.obj;
                    KeyguardViewMediator.this.handleStartKeyguardExitAnimation(startKeyguardExitAnimParams.startTime, startKeyguardExitAnimParams.fadeoutDuration);
                    KeyguardViewMediator.this.mFalsingManager.onSuccessfulUnlock();
                    Trace.endSection();
                    return;
                case 13:
                    Trace.beginSection("KeyguardViewMediator#handleMessage KEYGUARD_DONE_PENDING_TIMEOUT");
                    Log.w("KeyguardViewMediator", "Timeout while waiting for activity drawn!");
                    Trace.endSection();
                    return;
                case 14:
                    Trace.beginSection("KeyguardViewMediator#handleMessage NOTIFY_STARTED_WAKING_UP");
                    KeyguardViewMediator.this.handleNotifyStartedWakingUp();
                    Trace.endSection();
                    return;
                case 15:
                    Trace.beginSection("KeyguardViewMediator#handleMessage NOTIFY_SCREEN_TURNED_ON");
                    KeyguardViewMediator.this.handleNotifyScreenTurnedOn();
                    Trace.endSection();
                    return;
                case 16:
                    KeyguardViewMediator.this.handleNotifyScreenTurnedOff();
                    return;
                case 17:
                    KeyguardViewMediator.this.handleNotifyStartedGoingToSleep();
                    return;
                case 18:
                    KeyguardViewMediator.this.handleSystemReady();
                    return;
                default:
                    return;
            }
        }
    };
    private Animation mHideAnimation;
    private final Runnable mHideAnimationFinishedRunnable = new Runnable() { // from class: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$k6pAp5zgE8i-fpCJKwKX2Ojqibw
        @Override // java.lang.Runnable
        public final void run() {
            KeyguardViewMediator.this.lambda$new$7$KeyguardViewMediator();
        }
    };
    private boolean mHideAnimationRun = false;
    private boolean mHideAnimationRunning = false;
    private boolean mHiding;
    private boolean mInGestureNavigationMode;
    private boolean mInputRestricted;
    private KeyguardDisplayManager mKeyguardDisplayManager;
    private boolean mKeyguardDonePending = false;
    private final Runnable mKeyguardGoingAwayRunnable = new Runnable() { // from class: com.android.systemui.keyguard.KeyguardViewMediator.7
        @Override // java.lang.Runnable
        public void run() {
            Trace.beginSection("KeyguardViewMediator.mKeyGuardGoingAwayRunnable");
            Log.d("KeyguardViewMediator", "keyguardGoingAway");
            ((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).keyguardGoingAway();
            boolean isSupportAccelerate = KeyguardViewMediator.this.isSupportAccelerate();
            int i = (((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).shouldDisableWindowAnimationsForUnlock() || (((OpKeyguardViewMediator) KeyguardViewMediator.this).mStatusBar != null && ((OpKeyguardViewMediator) KeyguardViewMediator.this).mStatusBar.getFacelockController() != null && ((OpKeyguardViewMediator) KeyguardViewMediator.this).mStatusBar.getFacelockController().isScreenOffUnlock() && isSupportAccelerate) || (KeyguardViewMediator.this.mWakeAndUnlocking && !OpUtils.isHomeApp() && isSupportAccelerate)) ? 2 : 0;
            if (((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).isGoingToNotificationShade() || (KeyguardViewMediator.this.mWakeAndUnlocking && KeyguardViewMediator.this.mPulsing)) {
                i |= 1;
            }
            if (((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).isUnlockWithWallpaper()) {
                i |= 4;
            }
            if (((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).shouldSubtleWindowAnimationsForUnlock()) {
                i |= 8;
            }
            Log.d("KeyguardViewMediator", "keyguardGoingAway supportAccelerate:" + isSupportAccelerate + " flags:" + i);
            KeyguardViewMediator.this.mUpdateMonitor.setKeyguardGoingAway(true);
            ((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).setKeyguardGoingAwayState(true);
            KeyguardViewMediator.this.mUiBgExecutor.execute(new Runnable(i) { // from class: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$7$KooXr448I10DQHquWhJeRDn4i_w
                public final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    KeyguardViewMediator.AnonymousClass7.lambda$run$0(this.f$0);
                }
            });
            Trace.endSection();
        }

        static /* synthetic */ void lambda$run$0(int i) {
            try {
                ActivityTaskManager.getService().keyguardGoingAway(i);
            } catch (RemoteException e) {
                Log.e("KeyguardViewMediator", "Error while calling WindowManager", e);
            }
        }
    };
    private final ArrayList<IKeyguardStateCallback> mKeyguardStateCallbacks = new ArrayList<>();
    private final Lazy<KeyguardViewController> mKeyguardViewControllerLazy;
    private final SparseIntArray mLastSimStates = new SparseIntArray();
    private boolean mLockLater;
    private final LockPatternUtils mLockPatternUtils;
    private int mLockSoundId;
    private int mLockSoundStreamId;
    private float mLockSoundVolume;
    private SoundPool mLockSounds;
    private boolean mNeedToReshowWhenReenabled = false;
    private boolean mOccluded = false;
    private final DeviceConfig.OnPropertiesChangedListener mOnPropertiesChangedListener = new DeviceConfig.OnPropertiesChangedListener() { // from class: com.android.systemui.keyguard.KeyguardViewMediator.1
        public void onPropertiesChanged(DeviceConfig.Properties properties) {
            if (properties.getKeyset().contains("nav_bar_handle_show_over_lockscreen")) {
                KeyguardViewMediator.this.mShowHomeOverLockscreen = properties.getBoolean("nav_bar_handle_show_over_lockscreen", true);
            }
        }
    };
    private final PowerManager mPM;
    private boolean mPendingLock;
    private boolean mPendingReset;
    private boolean mPendingSetKeyguardDone = false;
    private String mPhoneState = TelephonyManager.EXTRA_STATE_IDLE;
    private boolean mPowerKeyCameraLaunching;
    private boolean mPulsing;
    private boolean mShowHomeOverLockscreen;
    private PowerManager.WakeLock mShowKeyguardWakeLock;
    private boolean mShowing;
    private boolean mShuttingDown;
    private StatusBarManager mStatusBarManager;
    private boolean mSystemReady;
    private final TrustManager mTrustManager;
    private int mTrustedSoundId;
    private final Executor mUiBgExecutor;
    private int mUiSoundsStreamType;
    private int mUnlockSoundId;
    KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.keyguard.KeyguardViewMediator.2
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserInfoChanged(int i) {
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitching(int i) {
            synchronized (KeyguardViewMediator.this) {
                KeyguardViewMediator.this.resetKeyguardDonePendingLocked();
                if (KeyguardViewMediator.this.mLockPatternUtils.isLockScreenDisabled(i)) {
                    KeyguardViewMediator.this.dismiss(null, null);
                } else {
                    KeyguardViewMediator.this.resetStateLocked();
                }
                KeyguardViewMediator.this.adjustStatusBarLocked();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(int i) {
            UserInfo userInfo;
            if (i != 0 && (userInfo = UserManager.get(((SystemUI) KeyguardViewMediator.this).mContext).getUserInfo(i)) != null && !KeyguardViewMediator.this.mLockPatternUtils.isSecure(i)) {
                if (userInfo.isGuest() || userInfo.isDemo()) {
                    KeyguardViewMediator.this.dismiss(null, null);
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onClockVisibilityChanged() {
            KeyguardViewMediator.this.adjustStatusBarLocked();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onDeviceProvisioned() {
            KeyguardViewMediator.this.sendUserPresentBroadcast();
            synchronized (KeyguardViewMediator.this) {
                if (KeyguardViewMediator.this.mustNotUnlockCurrentUser()) {
                    KeyguardViewMediator.this.doKeyguardLocked(null);
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:20:0x008e  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x009f  */
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onSimStateChanged(int r7, int r8, int r9) {
            /*
            // Method dump skipped, instructions count: 401
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.keyguard.KeyguardViewMediator.AnonymousClass2.onSimStateChanged(int, int, int):void");
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (KeyguardViewMediator.this.mLockPatternUtils.isSecure(currentUser)) {
                KeyguardViewMediator.this.mLockPatternUtils.getDevicePolicyManager().reportFailedBiometricAttempt(currentUser);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            if (KeyguardViewMediator.this.mLockPatternUtils.isSecure(i)) {
                KeyguardViewMediator.this.mLockPatternUtils.getDevicePolicyManager().reportSuccessfulBiometricAttempt(i);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTrustChanged(int i) {
            if (i == KeyguardUpdateMonitor.getCurrentUser()) {
                synchronized (KeyguardViewMediator.this) {
                    KeyguardViewMediator.this.notifyTrustedChangedLocked(KeyguardViewMediator.this.mUpdateMonitor.getUserHasTrust(i));
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onHasLockscreenWallpaperChanged(boolean z) {
            synchronized (KeyguardViewMediator.this) {
                KeyguardViewMediator.this.notifyHasLockscreenWallpaperChanged(z);
            }
        }
    };
    private final KeyguardUpdateMonitor mUpdateMonitor;
    ViewMediatorCallback mViewMediatorCallback = new ViewMediatorCallback() { // from class: com.android.systemui.keyguard.KeyguardViewMediator.3
        @Override // com.android.keyguard.ViewMediatorCallback
        public void reportMDMEvent(String str, String str2, String str3) {
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void userActivity() {
            KeyguardViewMediator.this.userActivity();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void keyguardDone(boolean z, int i) {
            if (i == ActivityManager.getCurrentUser()) {
                Log.d("KeyguardViewMediator", "keyguardDone");
                KeyguardViewMediator.this.tryKeyguardDone();
            }
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void keyguardDoneDrawing() {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardDoneDrawing");
            KeyguardViewMediator.this.mHandler.sendEmptyMessage(8);
            Trace.endSection();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void setNeedsInput(boolean z) {
            ((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).setNeedsInput(z);
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void keyguardDonePending(boolean z, int i) {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardDonePending");
            Log.d("KeyguardViewMediator", "keyguardDonePending");
            if (i != ActivityManager.getCurrentUser()) {
                Trace.endSection();
                return;
            }
            KeyguardViewMediator.this.mKeyguardDonePending = true;
            KeyguardViewMediator.this.mHideAnimationRun = true;
            KeyguardViewMediator.this.mHideAnimationRunning = true;
            ((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).startPreHideAnimation(KeyguardViewMediator.this.mHideAnimationFinishedRunnable);
            KeyguardViewMediator.this.mHandler.sendEmptyMessageDelayed(13, 3000);
            Trace.endSection();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void keyguardGone() {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#keyguardGone");
            Log.d("KeyguardViewMediator", "keyguardGone");
            ((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).setKeyguardGoingAwayState(false);
            KeyguardViewMediator.this.mKeyguardDisplayManager.hide();
            Trace.endSection();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void readyForKeyguardDone() {
            Trace.beginSection("KeyguardViewMediator.mViewMediatorCallback#readyForKeyguardDone");
            if (KeyguardViewMediator.this.mKeyguardDonePending) {
                KeyguardViewMediator.this.mKeyguardDonePending = false;
                KeyguardViewMediator.this.tryKeyguardDone();
            }
            Trace.endSection();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void resetKeyguard() {
            KeyguardViewMediator.this.resetStateLocked();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void onCancelClicked() {
            ((KeyguardViewController) KeyguardViewMediator.this.mKeyguardViewControllerLazy.get()).onCancelClicked();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void onBouncerVisiblityChanged(boolean z) {
            synchronized (KeyguardViewMediator.this) {
                KeyguardViewMediator.this.adjustStatusBarLocked(z, false);
            }
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void playTrustedSound() {
            KeyguardViewMediator.this.playTrustedSound();
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public boolean isScreenOn() {
            return KeyguardViewMediator.this.mDeviceInteractive;
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public int getBouncerPromptReason() {
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            boolean isTrustUsuallyManaged = KeyguardViewMediator.this.mUpdateMonitor.isTrustUsuallyManaged(currentUser);
            boolean z = isTrustUsuallyManaged || (KeyguardViewMediator.this.mUpdateMonitor.isUnlockingWithBiometricsPossible(currentUser) || KeyguardViewMediator.this.mUpdateMonitor.isUnlockWithFacelockPossible());
            KeyguardUpdateMonitor.StrongAuthTracker strongAuthTracker = KeyguardViewMediator.this.mUpdateMonitor.getStrongAuthTracker();
            int strongAuthForUser = strongAuthTracker.getStrongAuthForUser(currentUser);
            if (z && !strongAuthTracker.hasUserAuthenticatedSinceBoot()) {
                return 1;
            }
            if (z && (strongAuthForUser & 16) != 0) {
                return 2;
            }
            if (z && (strongAuthForUser & 2) != 0) {
                return 3;
            }
            if (isTrustUsuallyManaged && (strongAuthForUser & 4) != 0) {
                return 4;
            }
            if (z && (strongAuthForUser & 8) != 0) {
                return 5;
            }
            if (z && (strongAuthForUser & 64) != 0) {
                return 6;
            }
            if (!z || (strongAuthForUser & 128) == 0) {
                return 0;
            }
            return 7;
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public CharSequence consumeCustomMessage() {
            CharSequence charSequence = KeyguardViewMediator.this.mCustomMessage;
            KeyguardViewMediator.this.mCustomMessage = null;
            return charSequence;
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void tryToStartFaceLockFromBouncer() {
            if (((OpKeyguardViewMediator) KeyguardViewMediator.this).mStatusBar != null && ((OpKeyguardViewMediator) KeyguardViewMediator.this).mStatusBar.getFacelockController() != null) {
                ((OpKeyguardViewMediator) KeyguardViewMediator.this).mStatusBar.getFacelockController().tryToStartFaceLock(true);
            }
        }

        @Override // com.android.keyguard.ViewMediatorCallback
        public void startPowerKeyLaunchCamera() {
            Log.d("KeyguardViewMediator", "startPowerKeyLaunchCamera");
            KeyguardViewMediator.this.mPowerKeyCameraLaunching = true;
            KeyguardViewMediator.this.mHandler.sendMessageDelayed(KeyguardViewMediator.this.mHandler.obtainMessage(R$styleable.Constraint_layout_goneMarginRight, new StartKeyguardExitAnimParams(SystemClock.uptimeMillis() + KeyguardViewMediator.this.mHideAnimation.getStartOffset(), KeyguardViewMediator.this.mHideAnimation.getDuration())), 1000);
        }
    };
    private boolean mWaitingUntilKeyguardVisible = false;
    private boolean mWakeAndUnlocking;

    public void onShortPowerPressedGoHome() {
    }

    public KeyguardViewMediator(Context context, FalsingManager falsingManager, LockPatternUtils lockPatternUtils, BroadcastDispatcher broadcastDispatcher, Lazy<KeyguardViewController> lazy, DismissCallbackRegistry dismissCallbackRegistry, KeyguardUpdateMonitor keyguardUpdateMonitor, DumpManager dumpManager, Executor executor, PowerManager powerManager, TrustManager trustManager, DeviceConfigProxy deviceConfigProxy, NavigationModeController navigationModeController) {
        super(context);
        this.mFalsingManager = falsingManager;
        this.mLockPatternUtils = lockPatternUtils;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mKeyguardViewControllerLazy = lazy;
        this.mDismissCallbackRegistry = dismissCallbackRegistry;
        this.mUiBgExecutor = executor;
        this.mUpdateMonitor = keyguardUpdateMonitor;
        this.mPM = powerManager;
        this.mTrustManager = trustManager;
        dumpManager.registerDumpable(KeyguardViewMediator.class.getName(), this);
        this.mDeviceConfig = deviceConfigProxy;
        this.mShowHomeOverLockscreen = deviceConfigProxy.getBoolean("systemui", "nav_bar_handle_show_over_lockscreen", true);
        DeviceConfigProxy deviceConfigProxy2 = this.mDeviceConfig;
        OpKeyguardViewMediator.OpHandler opHandler = this.mHandler;
        Objects.requireNonNull(opHandler);
        deviceConfigProxy2.addOnPropertiesChangedListener("systemui", new Executor() { // from class: com.android.systemui.keyguard.-$$Lambda$eLWs6RgXUxZrGIBRjUxV6jB43VY
            @Override // java.util.concurrent.Executor
            public final void execute(Runnable runnable) {
                OpKeyguardViewMediator.OpHandler.this.post(runnable);
            }
        }, this.mOnPropertiesChangedListener);
        this.mInGestureNavigationMode = QuickStepContract.isGesturalMode(navigationModeController.addListener(new NavigationModeController.ModeChangedListener() { // from class: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$me7csJcL_HvRFR46jFg-Qy6MBGw
            @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
            public final void onNavigationModeChanged(int i) {
                KeyguardViewMediator.this.lambda$new$0$KeyguardViewMediator(i);
            }
        }));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$KeyguardViewMediator(int i) {
        this.mInGestureNavigationMode = QuickStepContract.isGesturalMode(i);
    }

    public void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
    }

    /* access modifiers changed from: package-private */
    public boolean mustNotUnlockCurrentUser() {
        return UserManager.isSplitSystemUser() && KeyguardUpdateMonitor.getCurrentUser() == 0;
    }

    private void setupLocked() {
        PowerManager.WakeLock newWakeLock = this.mPM.newWakeLock(1, "show keyguard");
        this.mShowKeyguardWakeLock = newWakeLock;
        boolean z = false;
        newWakeLock.setReferenceCounted(false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        this.mBroadcastDispatcher.registerReceiver(this.mBroadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD");
        intentFilter2.addAction("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK");
        this.mContext.registerReceiver(this.mDelayedLockBroadcastReceiver, intentFilter2, "com.android.systemui.permission.SELF", null);
        this.mKeyguardDisplayManager = new KeyguardDisplayManager(this.mContext, new InjectionInflationController(SystemUIFactory.getInstance().getRootComponent()));
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        KeyguardUpdateMonitor.setCurrentUser(ActivityManager.getCurrentUser());
        if (this.mContext.getResources().getBoolean(C0003R$bool.config_enableKeyguardService)) {
            if (!shouldWaitForProvisioning() && !this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())) {
                z = true;
            }
            setShowingLocked(z, true);
        } else {
            setShowingLocked(false, true);
        }
        ContentResolver contentResolver = this.mContext.getContentResolver();
        this.mDeviceInteractive = this.mPM.isInteractive();
        this.mLockSounds = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
        String string = Settings.Global.getString(contentResolver, "lock_sound");
        if (string != null) {
            this.mLockSoundId = this.mLockSounds.load(string, 1);
        }
        if (string == null || this.mLockSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load lock sound from " + string);
        }
        String string2 = Settings.Global.getString(contentResolver, "unlock_sound");
        if (string2 != null) {
            this.mUnlockSoundId = this.mLockSounds.load(string2, 1);
        }
        if (string2 == null || this.mUnlockSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load unlock sound from " + string2);
        }
        String string3 = Settings.Global.getString(contentResolver, "trusted_sound");
        if (string3 != null) {
            this.mTrustedSoundId = this.mLockSounds.load(string3, 1);
        }
        if (string3 == null || this.mTrustedSoundId == 0) {
            Log.w("KeyguardViewMediator", "failed to load trusted sound from " + string3);
        }
        this.mLockSoundVolume = (float) Math.pow(10.0d, (double) (((float) this.mContext.getResources().getInteger(17694833)) / 20.0f));
        this.mHideAnimation = AnimationUtils.loadAnimation(this.mContext, 17432683);
        new WorkLockActivityController(this.mContext);
    }

    @Override // com.oneplus.systemui.keyguard.OpKeyguardViewMediator, com.android.systemui.SystemUI
    public void start() {
        super.start();
        synchronized (this) {
            setupLocked();
        }
    }

    public void onSystemReady() {
        this.mHandler.obtainMessage(18).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSystemReady() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "onSystemReady");
            this.mSystemReady = true;
            doKeyguardLocked(null);
            this.mUpdateMonitor.registerCallback(this.mUpdateCallback);
            this.mUpdateMonitor.dispatchSystemReady();
        }
        maybeSendUserPresentBroadcast();
        this.mUpdateMonitor.setUserUnlocked(UserManager.get(this.mContext).isUserUnlocked(KeyguardUpdateMonitor.getCurrentUser()));
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x00a7  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00ff  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onStartedGoingToSleep(int r11) {
        /*
        // Method dump skipped, instructions count: 271
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.keyguard.KeyguardViewMediator.onStartedGoingToSleep(int):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onStartedGoingToSleep$1 */
    public /* synthetic */ void lambda$onStartedGoingToSleep$1$KeyguardViewMediator() {
        boolean z = OpKeyguardViewMediator.DEBUG_THREAD;
        enableMessageLogging(z, z);
        getMsgDump();
    }

    public void enableMessageLogging(boolean z, boolean z2) {
        OpKeyguardViewMediator.OpHandler opHandler = this.mHandler;
        if (opHandler != null && opHandler.getLooper() != null) {
            Log.d("KeyguardViewMediator", "enableMessageLogging: " + z + "," + z2);
            if (z) {
                this.mHandler.getLooper().setMessageLogging(new LogPrinter(3, "SystemUI"));
            } else {
                this.mHandler.getLooper().setMessageLogging(null);
            }
            if (z2) {
                this.mHandler.getLooper().dump(new LogPrinter(3, "SystemUI"), "queue_debug_enable");
            } else {
                this.mHandler.getLooper().dump(new LogPrinter(3, "SystemUI"), "queue_debug_disable");
            }
        }
    }

    public void getMsgDump() {
        OpKeyguardViewMediator.OpHandler opHandler = this.mHandler;
        if (opHandler != null && opHandler.getLooper() != null) {
            this.mHandler.getLooper().dump(new LogPrinter(3, "SystemUI"), "looper:");
        }
    }

    public void onFinishedGoingToSleep(int i, boolean z) {
        Log.d("KeyguardViewMediator", "onFinishedGoingToSleep(" + i + ")");
        synchronized (this) {
            this.mDeviceInteractive = false;
            this.mGoingToSleep = false;
            this.mWakeAndUnlocking = false;
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            notifyFinishedGoingToSleep();
            if (z) {
                Log.i("KeyguardViewMediator", "Camera gesture was triggered, preventing Keyguard locking.");
                ((PowerManager) this.mContext.getSystemService(PowerManager.class)).wakeUp(SystemClock.uptimeMillis(), 5, "com.android.systemui:CAMERA_GESTURE_PREVENT_LOCK");
                this.mPendingLock = false;
                this.mPendingReset = false;
            }
            if (this.mPendingReset) {
                resetStateLocked();
                this.mPendingReset = false;
            }
            if (this.mPendingLock) {
                doKeyguardLocked(null);
                this.mPendingLock = false;
            }
            if (!this.mLockLater && !z) {
                doKeyguardForChildProfilesLocked();
            }
        }
        this.mUpdateMonitor.dispatchFinishedGoingToSleep(i);
    }

    private long getLockTimeout(int i) {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        long j = (long) Settings.Secure.getInt(contentResolver, "lock_screen_lock_after_timeout", 5000);
        long maximumTimeToLock = this.mLockPatternUtils.getDevicePolicyManager().getMaximumTimeToLock(null, i);
        return maximumTimeToLock <= 0 ? j : Math.max(Math.min(maximumTimeToLock - Math.max((long) Settings.System.getInt(contentResolver, "screen_off_timeout", 30000), 0L), j), 0L);
    }

    private void doKeyguardLaterLocked() {
        long lockTimeout = getLockTimeout(KeyguardUpdateMonitor.getCurrentUser());
        if (lockTimeout == 0) {
            doKeyguardLocked(null);
        } else {
            doKeyguardLaterLocked(lockTimeout);
        }
    }

    private void doKeyguardLaterLocked(long j) {
        long elapsedRealtime = SystemClock.elapsedRealtime() + j;
        Intent intent = new Intent("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD");
        intent.putExtra("seq", this.mDelayedShowingSequence);
        intent.addFlags(268435456);
        this.mAlarmManager.setExactAndAllowWhileIdle(2, elapsedRealtime, PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456));
        Log.d("KeyguardViewMediator", "setting alarm to turn off keyguard, seq = " + this.mDelayedShowingSequence);
        doKeyguardLaterForChildProfilesLocked();
    }

    private void doKeyguardLaterForChildProfilesLocked() {
        int[] enabledProfileIds = UserManager.get(this.mContext).getEnabledProfileIds(UserHandle.myUserId());
        for (int i : enabledProfileIds) {
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(i)) {
                long lockTimeout = getLockTimeout(i);
                if (lockTimeout == 0) {
                    doKeyguardForChildProfilesLocked();
                } else {
                    long elapsedRealtime = SystemClock.elapsedRealtime() + lockTimeout;
                    Intent intent = new Intent("com.android.internal.policy.impl.PhoneWindowManager.DELAYED_LOCK");
                    intent.putExtra("seq", this.mDelayedProfileShowingSequence);
                    intent.putExtra("android.intent.extra.USER_ID", i);
                    intent.addFlags(268435456);
                    this.mAlarmManager.setExactAndAllowWhileIdle(2, elapsedRealtime, PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456));
                }
            }
        }
    }

    private void doKeyguardForChildProfilesLocked() {
        int[] enabledProfileIds = UserManager.get(this.mContext).getEnabledProfileIds(UserHandle.myUserId());
        for (int i : enabledProfileIds) {
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(i)) {
                lockProfile(i);
            }
        }
    }

    private void cancelDoKeyguardLaterLocked() {
        this.mDelayedShowingSequence++;
    }

    private void cancelDoKeyguardForChildProfilesLocked() {
        this.mDelayedProfileShowingSequence++;
    }

    public void onStartedWakingUp() {
        Trace.beginSection("KeyguardViewMediator#onStartedWakingUp");
        enableMessageLogging(OpKeyguardViewMediator.DEBUG_THREAD, true);
        getMsgDump();
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$0dGSJEb3YAsSlv-UzuxVEX8D0Cw
            @Override // java.lang.Runnable
            public final void run() {
                KeyguardViewMediator.this.lambda$onStartedWakingUp$2$KeyguardViewMediator();
            }
        }, 11000);
        if (this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())) {
            notifyUnlockCounter(false);
        }
        this.mUpdateMonitor.setGoingToSleepReason(-1);
        this.mUpdateMonitor.notifyFakeLocking(false);
        synchronized (this) {
            this.mDeviceInteractive = true;
            cancelDoKeyguardLaterLocked();
            cancelDoKeyguardForChildProfilesLocked();
            Log.d("KeyguardViewMediator", "onStartedWakingUp, seq = " + this.mDelayedShowingSequence);
            notifyStartedWakingUp();
        }
        this.mUpdateMonitor.dispatchStartedWakingUp();
        StatusBar statusBar = this.mStatusBar;
        if (!(statusBar == null || statusBar.getFacelockController() == null)) {
            this.mStatusBar.getFacelockController().onPreStartedWakingUp();
        }
        maybeSendUserPresentBroadcast();
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onStartedWakingUp$2 */
    public /* synthetic */ void lambda$onStartedWakingUp$2$KeyguardViewMediator() {
        boolean z = OpKeyguardViewMediator.DEBUG_THREAD;
        enableMessageLogging(z, z);
        getMsgDump();
    }

    public void onScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Trace.beginSection("KeyguardViewMediator#onScreenTurningOn");
        notifyScreenOn(iKeyguardDrawnCallback);
        Trace.endSection();
    }

    public void onScreenTurnedOn() {
        Trace.beginSection("KeyguardViewMediator#onScreenTurnedOn");
        notifyScreenTurnedOn();
        this.mUpdateMonitor.dispatchScreenTurnedOn();
        Trace.endSection();
    }

    public void onScreenTurnedOff() {
        notifyScreenTurnedOff();
        this.mUpdateMonitor.dispatchScreenTurnedOff();
    }

    private void maybeSendUserPresentBroadcast() {
        if (this.mSystemReady && this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser())) {
            sendUserPresentBroadcast();
        } else if (this.mSystemReady && shouldWaitForProvisioning()) {
            getLockPatternUtils().userPresent(KeyguardUpdateMonitor.getCurrentUser());
        }
    }

    public void onDreamingStarted() {
        this.mUpdateMonitor.dispatchDreamingStarted();
        synchronized (this) {
            if (this.mDeviceInteractive && this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser())) {
                doKeyguardLaterLocked();
            }
        }
    }

    public void onDreamingStopped() {
        this.mUpdateMonitor.dispatchDreamingStopped();
        synchronized (this) {
            if (this.mDeviceInteractive) {
                cancelDoKeyguardLaterLocked();
            }
        }
    }

    public void setKeyguardEnabled(boolean z) {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "setKeyguardEnabled(" + z + ")");
            this.mExternallyEnabled = z;
            if (z || !this.mShowing) {
                if (z && this.mNeedToReshowWhenReenabled) {
                    Log.d("KeyguardViewMediator", "previously hidden, reshowing, reenabling status bar expansion");
                    this.mNeedToReshowWhenReenabled = false;
                    updateKeyguardDisable(false);
                    updateInputRestrictedLocked();
                    if (this.mExitSecureCallback != null) {
                        Log.d("KeyguardViewMediator", "onKeyguardExitResult(false), resetting");
                        try {
                            this.mExitSecureCallback.onKeyguardExitResult(false);
                        } catch (RemoteException e) {
                            Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e);
                        }
                        this.mExitSecureCallback = null;
                        resetStateLocked();
                    } else {
                        this.mUpdateMonitor.notifyKeyguardDone(false);
                        this.mUpdateMonitor.notifyFakeLocking(false);
                        showLocked(null);
                        this.mWaitingUntilKeyguardVisible = true;
                        this.mHandler.sendEmptyMessageDelayed(8, 2000);
                        Log.d("KeyguardViewMediator", "waiting until mWaitingUntilKeyguardVisible is false");
                        while (this.mWaitingUntilKeyguardVisible) {
                            try {
                                wait();
                            } catch (InterruptedException unused) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        Log.d("KeyguardViewMediator", "done waiting for mWaitingUntilKeyguardVisible");
                    }
                }
            } else if (this.mExitSecureCallback != null) {
                Log.d("KeyguardViewMediator", "in process of verifyUnlock request, ignoring");
            } else {
                Log.d("KeyguardViewMediator", "remembering to reshow, hiding keyguard, disabling status bar expansion");
                this.mNeedToReshowWhenReenabled = true;
                updateKeyguardDisable(true);
                updateInputRestrictedLocked();
                hideLocked();
            }
        }
    }

    public void verifyUnlock(IKeyguardExitCallback iKeyguardExitCallback) {
        Trace.beginSection("KeyguardViewMediator#verifyUnlock");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "verifyUnlock");
            if (shouldWaitForProvisioning()) {
                Log.d("KeyguardViewMediator", "ignoring because device isn't provisioned");
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e);
                }
            } else if (this.mExternallyEnabled) {
                Log.w("KeyguardViewMediator", "verifyUnlock called when not externally disabled");
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e2) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e2);
                }
            } else if (this.mExitSecureCallback != null) {
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e3) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e3);
                }
            } else if (!isSecure()) {
                this.mExternallyEnabled = true;
                this.mNeedToReshowWhenReenabled = false;
                updateKeyguardDisable(false);
                updateInputRestricted();
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(true);
                } catch (RemoteException e4) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e4);
                }
            } else {
                try {
                    iKeyguardExitCallback.onKeyguardExitResult(false);
                } catch (RemoteException e5) {
                    Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult(false)", e5);
                }
            }
        }
        Trace.endSection();
    }

    public boolean isShowingAndNotOccluded() {
        return this.mShowing && !this.mOccluded;
    }

    public void setOccluded(boolean z, boolean z2) {
        Trace.beginSection("KeyguardViewMediator#setOccluded");
        Log.d("KeyguardViewMediator", "setOccluded " + z);
        this.mHandler.removeMessages(9);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(9, z ? 1 : 0, z2 ? 1 : 0));
        Trace.endSection();
    }

    public static int getUnlockTrackSimState(int i) {
        return mUnlockTrackSimStates.get(i);
    }

    public boolean isHiding() {
        return this.mHiding;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetOccluded(boolean z, boolean z2) {
        boolean z3;
        Trace.beginSection("KeyguardViewMediator#handleSetOccluded");
        synchronized (this) {
            if (this.mHiding && z) {
                startKeyguardExitAnimation(0, 0);
            }
            if (this.mOccluded != z) {
                this.mOccluded = z;
                this.mUpdateMonitor.setKeyguardOccluded(z);
                KeyguardViewController keyguardViewController = this.mKeyguardViewControllerLazy.get();
                if (!((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isSimPinSecure()) {
                    if (z2 && this.mDeviceInteractive) {
                        z3 = true;
                        keyguardViewController.setOccluded(z, z3);
                        adjustStatusBarLocked();
                    }
                }
                z3 = false;
                keyguardViewController.setOccluded(z, z3);
                adjustStatusBarLocked();
            }
        }
        Trace.endSection();
    }

    public void doKeyguardTimeout(Bundle bundle) {
        this.mHandler.removeMessages(10);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(10, bundle));
    }

    public boolean isInputRestricted() {
        return this.mShowing || this.mNeedToReshowWhenReenabled;
    }

    private void updateInputRestricted() {
        synchronized (this) {
            updateInputRestrictedLocked();
        }
    }

    private void updateInputRestrictedLocked() {
        boolean isInputRestricted = isInputRestricted();
        if (this.mInputRestricted != isInputRestricted) {
            this.mInputRestricted = isInputRestricted;
            for (int size = this.mKeyguardStateCallbacks.size() - 1; size >= 0; size--) {
                IKeyguardStateCallback iKeyguardStateCallback = this.mKeyguardStateCallbacks.get(size);
                try {
                    iKeyguardStateCallback.onInputRestrictedStateChanged(isInputRestricted);
                } catch (RemoteException e) {
                    Slog.w("KeyguardViewMediator", "Failed to call onDeviceProvisioned", e);
                    if (e instanceof DeadObjectException) {
                        this.mKeyguardStateCallbacks.remove(iKeyguardStateCallback);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doKeyguardLocked(Bundle bundle) {
        if (KeyguardUpdateMonitor.CORE_APPS_ONLY) {
            Log.d("KeyguardViewMediator", "doKeyguard: not showing because booting to cryptkeeper");
            return;
        }
        boolean z = true;
        if (!this.mExternallyEnabled) {
            Log.d("KeyguardViewMediator", "doKeyguard: not showing because externally disabled");
            this.mNeedToReshowWhenReenabled = true;
            updateKeyguardDisable(true);
            this.mUpdateMonitor.notifyFakeLocking(true);
            OpSystemUIInjector.setKeyguardDone(false);
        } else if (this.mKeyguardViewControllerLazy.get().isShowing()) {
            Log.d("KeyguardViewMediator", "doKeyguard: not showing because it is already showing");
            resetStateLocked();
            this.mPendingSetKeyguardDone = false;
            OpSystemUIInjector.setKeyguardDone(false);
            this.mUpdateMonitor.notifyKeyguardDone(false);
        } else {
            if (!mustNotUnlockCurrentUser() || !this.mUpdateMonitor.isDeviceProvisioned()) {
                boolean z2 = this.mUpdateMonitor.isSimPinSecure() || ((SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(1)) || SubscriptionManager.isValidSubscriptionId(this.mUpdateMonitor.getNextSubIdForState(7))) && (SystemProperties.getBoolean("keyguard.no_require_sim", false) ^ true));
                if (z2 || !shouldWaitForProvisioning()) {
                    if (bundle == null || !bundle.getBoolean("force_show", false)) {
                        z = false;
                    }
                    if (this.mLockPatternUtils.isLockScreenDisabled(KeyguardUpdateMonitor.getCurrentUser()) && !z2 && !z) {
                        Log.d("KeyguardViewMediator", "doKeyguard: not showing because lockscreen is off");
                        return;
                    } else if (this.mLockPatternUtils.checkVoldPassword(KeyguardUpdateMonitor.getCurrentUser())) {
                        Log.d("KeyguardViewMediator", "Not showing lock screen since just decrypted");
                        setShowingLocked(false);
                        hideLocked();
                        return;
                    }
                } else {
                    Log.d("KeyguardViewMediator", "doKeyguard: not showing because device isn't provisioned and the sim is not locked or missing");
                    return;
                }
            }
            this.mPendingSetKeyguardDone = false;
            OpSystemUIInjector.setKeyguardDone(false);
            this.mUpdateMonitor.notifyKeyguardDone(false);
            Log.d("KeyguardViewMediator", "doKeyguard: showing the lock screen");
            this.mUpdateMonitor.notifyFakeLocking(false);
            updateKeyguardDisable(false);
            showLocked(bundle);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void lockProfile(int i) {
        this.mTrustManager.setDeviceLockedForUser(i, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldWaitForProvisioning() {
        return !this.mUpdateMonitor.isDeviceProvisioned() && !isSecure();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDismiss(IKeyguardDismissCallback iKeyguardDismissCallback, CharSequence charSequence) {
        if (this.mShowing) {
            if (iKeyguardDismissCallback != null) {
                this.mDismissCallbackRegistry.addCallback(iKeyguardDismissCallback);
            }
            this.mCustomMessage = charSequence;
            this.mKeyguardViewControllerLazy.get().dismissAndCollapse();
        } else if (iKeyguardDismissCallback != null) {
            new DismissCallbackWrapper(iKeyguardDismissCallback).notifyDismissError();
        }
    }

    public void dismiss(IKeyguardDismissCallback iKeyguardDismissCallback, CharSequence charSequence) {
        this.mHandler.obtainMessage(11, new DismissMessage(iKeyguardDismissCallback, charSequence)).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetStateLocked() {
        Log.e("KeyguardViewMediator", "resetStateLocked");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3));
    }

    private void notifyStartedGoingToSleep() {
        Log.d("KeyguardViewMediator", "notifyStartedGoingToSleep");
        this.mHandler.sendEmptyMessage(17);
    }

    private void notifyFinishedGoingToSleep() {
        Log.d("KeyguardViewMediator", "notifyFinishedGoingToSleep");
        this.mHandler.sendEmptyMessage(5);
    }

    private void notifyStartedWakingUp() {
        Log.d("KeyguardViewMediator", "notifyStartedWakingUp");
        this.mHandler.sendEmptyMessage(14);
    }

    private void notifyScreenOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Log.d("KeyguardViewMediator", "notifyScreenOn");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(6, iKeyguardDrawnCallback));
    }

    private void notifyScreenTurnedOn() {
        Log.d("KeyguardViewMediator", "notifyScreenTurnedOn");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(15));
    }

    private void notifyScreenTurnedOff() {
        Log.d("KeyguardViewMediator", "notifyScreenTurnedOff");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(16));
    }

    private void showLocked(Bundle bundle) {
        Trace.beginSection("KeyguardViewMediator#showLocked aqcuiring mShowKeyguardWakeLock");
        Log.d("KeyguardViewMediator", "showLocked");
        this.mShowKeyguardWakeLock.acquire();
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, bundle));
        Trace.endSection();
    }

    private void hideLocked() {
        Trace.beginSection("KeyguardViewMediator#hideLocked");
        Log.d("KeyguardViewMediator", "hideLocked");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2));
        Trace.endSection();
    }

    public boolean isSecure() {
        return isSecure(KeyguardUpdateMonitor.getCurrentUser());
    }

    public boolean isSecure(int i) {
        return this.mLockPatternUtils.isSecure(i) || this.mUpdateMonitor.isSimPinSecure();
    }

    public void setSwitchingUser(boolean z) {
        this.mUpdateMonitor.setSwitchingUser(z);
    }

    public void setCurrentUser(int i) {
        KeyguardUpdateMonitor.setCurrentUser(i);
        synchronized (this) {
            notifyTrustedChangedLocked(this.mUpdateMonitor.getUserHasTrust(i));
        }
    }

    public void keyguardDone() {
        Trace.beginSection("KeyguardViewMediator#keyguardDone");
        Log.d("KeyguardViewMediator", "keyguardDone()");
        userActivity();
        EventLog.writeEvent(70000, 2);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(7));
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void tryKeyguardDone() {
        Log.d("KeyguardViewMediator", "tryKeyguardDone: pending - " + this.mKeyguardDonePending + ", animRan - " + this.mHideAnimationRun + " animRunning - " + this.mHideAnimationRunning);
        if (!this.mKeyguardDonePending && this.mHideAnimationRun && !this.mHideAnimationRunning) {
            handleKeyguardDone();
        } else if (!this.mHideAnimationRun) {
            Log.d("KeyguardViewMediator", "tryKeyguardDone: starting pre-hide animation");
            this.mHideAnimationRun = true;
            this.mHideAnimationRunning = true;
            this.mKeyguardViewControllerLazy.get().startPreHideAnimation(this.mHideAnimationFinishedRunnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKeyguardDone() {
        Trace.beginSection("KeyguardViewMediator#handleKeyguardDone");
        this.mUiBgExecutor.execute(new Runnable(KeyguardUpdateMonitor.getCurrentUser()) { // from class: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$mFsUTJO27ferqTk7kQw3sOLv8yU
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                KeyguardViewMediator.this.lambda$handleKeyguardDone$3$KeyguardViewMediator(this.f$1);
            }
        });
        StatusBar statusBar = this.mStatusBar;
        int i = 0;
        boolean isBouncerShowing = statusBar != null ? statusBar.isBouncerShowing() : false;
        Log.d("KeyguardViewMediator", "handleKeyguardDone" + this.mOccluded + ", " + isBouncerShowing);
        synchronized (this) {
            resetKeyguardDonePendingLocked();
        }
        this.mUpdateMonitor.notifyDisplayKeyguardUnlockSuccess();
        this.mUpdateMonitor.clearFailedUnlockAttempts(false);
        if (this.mGoingToSleep) {
            this.mUpdateMonitor.clearBiometricRecognized();
            Log.i("KeyguardViewMediator", "Device is going to sleep, aborting keyguardDone");
            BiometricUnlockController biometricUnlockController = OpLsState.getInstance().getBiometricUnlockController();
            if (biometricUnlockController != null) {
                i = biometricUnlockController.getMode();
            }
            if (biometricUnlockController != null && i != 1 && i != 8 && i != 7 && i != 5) {
                biometricUnlockController.opResetMode();
                return;
            }
            return;
        }
        IKeyguardExitCallback iKeyguardExitCallback = this.mExitSecureCallback;
        if (iKeyguardExitCallback != null) {
            try {
                iKeyguardExitCallback.onKeyguardExitResult(true);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onKeyguardExitResult()", e);
            }
            this.mExitSecureCallback = null;
            this.mExternallyEnabled = true;
            this.mNeedToReshowWhenReenabled = false;
            updateKeyguardDisable(false);
            updateInputRestricted();
        }
        try {
            if (!this.mShowing || !this.mOccluded || !isBouncerShowing) {
                this.mPendingSetKeyguardDone = false;
                OpSystemUIInjector.setKeyguardDone(true);
            } else {
                Slog.d("KeyguardViewMediator", "pending setKeyguardDone");
                this.mPendingSetKeyguardDone = true;
            }
        } catch (Exception e2) {
            Log.w("KeyguardViewMediator", "Exception e = " + e2.toString());
        }
        this.mUpdateMonitor.notifyKeyguardDone(true);
        this.mUpdateMonitor.setUserUnlocked(true);
        handleHide();
        this.mUpdateMonitor.clearBiometricRecognized();
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleKeyguardDone$3 */
    public /* synthetic */ void lambda$handleKeyguardDone$3$KeyguardViewMediator(int i) {
        if (this.mLockPatternUtils.isSecure(i)) {
            this.mLockPatternUtils.getDevicePolicyManager().reportKeyguardDismissed(i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUserPresentBroadcast() {
        synchronized (this) {
            if (this.mBootCompleted) {
                int currentUser = KeyguardUpdateMonitor.getCurrentUser();
                this.mUiBgExecutor.execute(new Runnable((UserManager) this.mContext.getSystemService("user"), new UserHandle(currentUser), currentUser) { // from class: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$46NCjKcw7zemUffIguiaIbd5NnM
                    public final /* synthetic */ UserManager f$1;
                    public final /* synthetic */ UserHandle f$2;
                    public final /* synthetic */ int f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        KeyguardViewMediator.this.lambda$sendUserPresentBroadcast$4$KeyguardViewMediator(this.f$1, this.f$2, this.f$3);
                    }
                });
            } else {
                this.mBootSendUserPresent = true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$sendUserPresentBroadcast$4 */
    public /* synthetic */ void lambda$sendUserPresentBroadcast$4$KeyguardViewMediator(UserManager userManager, UserHandle userHandle, int i) {
        for (int i2 : userManager.getProfileIdsWithDisabled(userHandle.getIdentifier())) {
            this.mContext.sendBroadcastAsUser(USER_PRESENT_INTENT, UserHandle.of(i2));
        }
        getLockPatternUtils().userPresent(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKeyguardDoneDrawing() {
        Trace.beginSection("KeyguardViewMediator#handleKeyguardDoneDrawing");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleKeyguardDoneDrawing");
            if (this.mWaitingUntilKeyguardVisible) {
                Log.d("KeyguardViewMediator", "handleKeyguardDoneDrawing: notifying mWaitingUntilKeyguardVisible");
                this.mWaitingUntilKeyguardVisible = false;
                notifyAll();
                this.mHandler.removeMessages(8);
            }
        }
        Trace.endSection();
    }

    private void playSounds(boolean z) {
        playSound(z ? this.mLockSoundId : this.mUnlockSoundId);
    }

    private void playSound(int i) {
        if (i != 0 && Settings.System.getIntForUser(this.mContext.getContentResolver(), "lockscreen_sounds_enabled", 1, KeyguardUpdateMonitor.getCurrentUser()) == 1) {
            this.mLockSounds.stop(this.mLockSoundStreamId);
            if (this.mAudioManager == null) {
                AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
                this.mAudioManager = audioManager;
                if (audioManager != null) {
                    this.mUiSoundsStreamType = audioManager.getUiSoundsStreamType();
                } else {
                    return;
                }
            }
            this.mUiBgExecutor.execute(new Runnable(i) { // from class: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$MwbBHbLGyKB-5Z0qAnOegZyyz6A
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    KeyguardViewMediator.this.lambda$playSound$5$KeyguardViewMediator(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$playSound$5 */
    public /* synthetic */ void lambda$playSound$5$KeyguardViewMediator(int i) {
        if (!this.mAudioManager.isStreamMute(this.mUiSoundsStreamType)) {
            SoundPool soundPool = this.mLockSounds;
            float f = this.mLockSoundVolume;
            int play = soundPool.play(i, f, f, 1, 0, 1.0f);
            Log.d("KeyguardViewMediator", "play lock soundId: " + i + ", volume:" + this.mLockSoundVolume + ", " + this.mLockSounds + ", type:" + this.mUiSoundsStreamType + ", " + play);
            synchronized (this) {
                this.mLockSoundStreamId = play;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void playTrustedSound() {
        playSound(this.mTrustedSoundId);
    }

    private void updateActivityLockScreenState(boolean z, boolean z2) {
        this.mUiBgExecutor.execute(new Runnable(z, z2) { // from class: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$EQ35u3mjdwkLrREGeB01rhox6_0
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                KeyguardViewMediator.this.lambda$updateActivityLockScreenState$6$KeyguardViewMediator(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateActivityLockScreenState$6 */
    public /* synthetic */ void lambda$updateActivityLockScreenState$6$KeyguardViewMediator(boolean z, boolean z2) {
        Log.d("KeyguardViewMediator", "updateActivityLockScreenState(" + z + ", " + z2 + ")");
        try {
            Log.d("KeyguardViewMediator", "setLockScreenShown, " + z + ", " + z2 + ", " + this.mPendingSetKeyguardDone);
            if (this.mPendingSetKeyguardDone && !z) {
                OpSystemUIInjector.setKeyguardDone(true);
            }
            this.mPendingSetKeyguardDone = false;
            notifyUnlockCounter(z);
            ActivityTaskManager.getService().setLockScreenShown(z, z2);
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleShow(Bundle bundle) {
        Trace.beginSection("KeyguardViewMediator#handleShow");
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (this.mLockPatternUtils.isSecure(currentUser)) {
            this.mLockPatternUtils.getDevicePolicyManager().reportKeyguardSecured(currentUser);
        }
        synchronized (this) {
            if (!this.mSystemReady) {
                Log.d("KeyguardViewMediator", "ignoring handleShow because system is not ready.");
                OpSystemUIInjector.setKeyguardDone(true);
                return;
            }
            Log.d("KeyguardViewMediator", "handleShow");
            this.mHiding = false;
            this.mWakeAndUnlocking = false;
            this.mUpdateMonitor.onFacelockUnlocking(false);
            setShowingLocked(true);
            this.mKeyguardViewControllerLazy.get().show(bundle);
            ((NotificationMediaManager) Dependency.get(NotificationMediaManager.class)).updateMediaMetaData(false, true);
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            adjustStatusBarLocked();
            userActivity();
            this.mUpdateMonitor.resetFingerprintAlreadyAuthenticated();
            this.mUpdateMonitor.setKeyguardGoingAway(false);
            this.mKeyguardViewControllerLazy.get().setKeyguardGoingAwayState(false);
            this.mShowKeyguardWakeLock.release();
            this.mKeyguardDisplayManager.show();
            this.mLockPatternUtils.scheduleNonStrongBiometricIdleTimeout(KeyguardUpdateMonitor.getCurrentUser());
            Trace.endSection();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$7 */
    public /* synthetic */ void lambda$new$7$KeyguardViewMediator() {
        Log.e("KeyguardViewMediator", "mHideAnimationFinishedRunnable#run");
        this.mHideAnimationRunning = false;
        tryKeyguardDone();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHide() {
        Trace.beginSection("KeyguardViewMediator#handleHide");
        if (this.mAodShowing) {
            ((PowerManager) this.mContext.getSystemService(PowerManager.class)).wakeUp(SystemClock.uptimeMillis(), 4, "com.android.systemui:BOUNCER_DOZING");
        }
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleHide, " + this.mOccluded);
            if (mustNotUnlockCurrentUser()) {
                Log.d("KeyguardViewMediator", "Split system user, quit unlocking.");
                return;
            }
            this.mHiding = true;
            if (!this.mShowing || this.mOccluded) {
                handleStartKeyguardExitAnimation(SystemClock.uptimeMillis() + this.mHideAnimation.getStartOffset(), this.mHideAnimation.getDuration());
            } else {
                this.mKeyguardGoingAwayRunnable.run();
            }
            Trace.endSection();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStartKeyguardExitAnimation(long j, long j2) {
        Trace.beginSection("KeyguardViewMediator#handleStartKeyguardExitAnimation");
        Log.d("KeyguardViewMediator", "handleStartKeyguardExitAnimation startTime=" + j + " fadeoutDuration=" + j2);
        EventLog.writeEvent(70000, 2);
        synchronized (this) {
            if (!this.mHiding) {
                setShowingLocked(this.mShowing, true);
                return;
            }
            this.mHiding = false;
            if (this.mWakeAndUnlocking && this.mDrawnCallback != null) {
                this.mKeyguardViewControllerLazy.get().getViewRootImpl().setReportNextDraw();
                notifyDrawn(this.mDrawnCallback);
                this.mDrawnCallback = null;
            }
            if (TelephonyManager.EXTRA_STATE_IDLE.equals(this.mPhoneState)) {
                playSounds(false);
            }
            setShowingLocked(false);
            this.mWakeAndUnlocking = false;
            this.mDismissCallbackRegistry.notifyDismissSucceeded();
            this.mKeyguardViewControllerLazy.get().hide(j, j2);
            this.mUpdateMonitor.onFacelockUnlocking(false);
            resetKeyguardDonePendingLocked();
            this.mHideAnimationRun = false;
            adjustStatusBarLocked();
            sendUserPresentBroadcast();
        }
        KeyguardUpdateMonitor keyguardUpdateMonitor = this.mUpdateMonitor;
        if (keyguardUpdateMonitor != null && keyguardUpdateMonitor.isSimPinSecure()) {
            Log.d("KeyguardViewMediator", "doKeyguard again when sim pin is still locked");
            doKeyguardLocked(null);
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void adjustStatusBarLocked() {
        adjustStatusBarLocked(false, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void adjustStatusBarLocked(boolean z, boolean z2) {
        if (this.mStatusBarManager == null) {
            this.mStatusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        }
        StatusBarManager statusBarManager = this.mStatusBarManager;
        if (statusBarManager == null) {
            Log.w("KeyguardViewMediator", "Could not get status bar manager");
            return;
        }
        int i = 0;
        if (z2) {
            statusBarManager.disable(0);
        }
        if (z || isShowingAndNotOccluded()) {
            if (!this.mShowHomeOverLockscreen || !this.mInGestureNavigationMode) {
                i = 2097152;
            }
            i |= 16777216;
        }
        Log.d("KeyguardViewMediator", "adjustStatusBarLocked: mShowing=" + this.mShowing + " mOccluded=" + this.mOccluded + " isSecure=" + isSecure() + " force=" + z + " --> flags=0x" + Integer.toHexString(i));
        this.mStatusBarManager.disable(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReset() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleReset");
            this.mKeyguardViewControllerLazy.get().reset(true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleVerifyUnlock() {
        Trace.beginSection("KeyguardViewMediator#handleVerifyUnlock");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleVerifyUnlock");
            setShowingLocked(true);
            this.mKeyguardViewControllerLazy.get().dismissAndCollapse();
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyStartedGoingToSleep() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyStartedGoingToSleep");
            this.mKeyguardViewControllerLazy.get().onStartedGoingToSleep();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyFinishedGoingToSleep() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyFinishedGoingToSleep");
            this.mKeyguardViewControllerLazy.get().onFinishedGoingToSleep();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyStartedWakingUp() {
        Trace.beginSection("KeyguardViewMediator#handleMotifyStartedWakingUp");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyWakingUp");
            this.mKeyguardViewControllerLazy.get().onStartedWakingUp();
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyScreenTurningOn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Trace.beginSection("KeyguardViewMediator#handleNotifyScreenTurningOn");
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurningOn");
            this.mKeyguardViewControllerLazy.get().onScreenTurningOn();
            this.mUpdateMonitor.notifyScreenTurningOn();
            if (iKeyguardDrawnCallback != null) {
                if (this.mWakeAndUnlocking) {
                    this.mDrawnCallback = iKeyguardDrawnCallback;
                } else {
                    notifyDrawn(iKeyguardDrawnCallback);
                }
            }
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyScreenTurnedOn() {
        Trace.beginSection("KeyguardViewMediator#handleNotifyScreenTurnedOn");
        if (LatencyTracker.isEnabled(this.mContext)) {
            LatencyTracker.getInstance(this.mContext).onActionEnd(5);
        }
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurnedOn");
            OpLsState.getInstance().onScreenTurnedOn();
            this.mKeyguardViewControllerLazy.get().onScreenTurnedOn();
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifyScreenTurnedOff() {
        synchronized (this) {
            Log.d("KeyguardViewMediator", "handleNotifyScreenTurnedOff");
            this.mDrawnCallback = null;
            this.mUpdateMonitor.onFacelockUnlocking(false);
        }
    }

    private void notifyDrawn(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Trace.beginSection("KeyguardViewMediator#notifyDrawn");
        try {
            iKeyguardDrawnCallback.onDrawn();
        } catch (RemoteException e) {
            Slog.w("KeyguardViewMediator", "Exception calling onDrawn():", e);
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetKeyguardDonePendingLocked() {
        this.mKeyguardDonePending = false;
        this.mHandler.removeMessages(13);
    }

    @Override // com.android.systemui.SystemUI
    public void onBootCompleted() {
        this.mUpdateMonitor.dispatchBootCompleted();
        synchronized (this) {
            this.mBootCompleted = true;
            adjustStatusBarLocked(false, true);
            if (this.mBootSendUserPresent) {
                sendUserPresentBroadcast();
            }
        }
        ((HotspotController) Dependency.get(HotspotController.class)).onBootCompleted();
    }

    public void onWakeAndUnlocking() {
        Trace.beginSection("KeyguardViewMediator#onWakeAndUnlocking");
        this.mWakeAndUnlocking = true;
        adjustStatusBarLocked();
        keyguardDone();
        Trace.endSection();
    }

    public void startKeyguardExitAnimation(long j, long j2) {
        Trace.beginSection("KeyguardViewMediator#startKeyguardExitAnimation");
        Log.d("KeyguardViewMediator", "startKeyguardExitAnimation");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(12, new StartKeyguardExitAnimParams(j, j2)));
        Trace.endSection();
    }

    public ViewMediatorCallback getViewMediatorCallback() {
        return this.mViewMediatorCallback;
    }

    public LockPatternUtils getLockPatternUtils() {
        return this.mLockPatternUtils;
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mSystemReady: ");
        printWriter.println(this.mSystemReady);
        printWriter.print("  mBootCompleted: ");
        printWriter.println(this.mBootCompleted);
        printWriter.print("  mBootSendUserPresent: ");
        printWriter.println(this.mBootSendUserPresent);
        printWriter.print("  mExternallyEnabled: ");
        printWriter.println(this.mExternallyEnabled);
        printWriter.print("  mShuttingDown: ");
        printWriter.println(this.mShuttingDown);
        printWriter.print("  mNeedToReshowWhenReenabled: ");
        printWriter.println(this.mNeedToReshowWhenReenabled);
        printWriter.print("  mShowing: ");
        printWriter.println(this.mShowing);
        printWriter.print("  mInputRestricted: ");
        printWriter.println(this.mInputRestricted);
        printWriter.print("  mOccluded: ");
        printWriter.println(this.mOccluded);
        printWriter.print("  mDelayedShowingSequence: ");
        printWriter.println(this.mDelayedShowingSequence);
        printWriter.print("  mExitSecureCallback: ");
        printWriter.println(this.mExitSecureCallback);
        printWriter.print("  mDeviceInteractive: ");
        printWriter.println(this.mDeviceInteractive);
        printWriter.print("  mGoingToSleep: ");
        printWriter.println(this.mGoingToSleep);
        printWriter.print("  mHiding: ");
        printWriter.println(this.mHiding);
        printWriter.print("  mDozing: ");
        printWriter.println(this.mDozing);
        printWriter.print("  mAodShowing: ");
        printWriter.println(this.mAodShowing);
        printWriter.print("  mWaitingUntilKeyguardVisible: ");
        printWriter.println(this.mWaitingUntilKeyguardVisible);
        printWriter.print("  mKeyguardDonePending: ");
        printWriter.println(this.mKeyguardDonePending);
        printWriter.print("  mHideAnimationRun: ");
        printWriter.println(this.mHideAnimationRun);
        printWriter.print("  mPendingReset: ");
        printWriter.println(this.mPendingReset);
        printWriter.print("  mPendingLock: ");
        printWriter.println(this.mPendingLock);
        printWriter.print("  mWakeAndUnlocking: ");
        printWriter.println(this.mWakeAndUnlocking);
        printWriter.print("  mDrawnCallback: ");
        printWriter.println(this.mDrawnCallback);
        opDump(printWriter, strArr);
        OpKeyguardViewMediator.OpHandler opHandler = this.mHandler;
        if (!(opHandler == null || opHandler.getLooper() == null)) {
            this.mHandler.getLooper().dump(new PrintWriterPrinter(printWriter), "KeyguardViewMediator ");
        }
        boolean z = this.mContext.getResources().getBoolean(17891529);
        printWriter.print("  config_showNavigationBar: ");
        printWriter.println(z);
    }

    public void setDozing(boolean z) {
        if (z != this.mDozing) {
            this.mDozing = z;
            setShowingLocked(this.mShowing);
        }
    }

    public void setPulsing(boolean z) {
        this.mPulsing = z;
    }

    /* access modifiers changed from: private */
    public static class StartKeyguardExitAnimParams {
        long fadeoutDuration;
        long startTime;

        private StartKeyguardExitAnimParams(long j, long j2) {
            this.startTime = j;
            this.fadeoutDuration = j2;
        }
    }

    private void setShowingLocked(boolean z) {
        setShowingLocked(z, false);
    }

    private void setShowingLocked(boolean z, boolean z2) {
        StatusBar statusBar = this.mStatusBar;
        boolean z3 = false;
        boolean z4 = (this.mDozing || (statusBar != null ? statusBar.isDozingCustom() : false)) && !this.mWakeAndUnlocking;
        if (!(z == this.mShowing && z4 == this.mAodShowing && !z2)) {
            z3 = true;
        }
        this.mShowing = z;
        this.mAodShowing = z4;
        if (OpLsState.getInstance().getPreventModeCtrl() != null) {
            OpLsState.getInstance().getPreventModeCtrl().setKeyguardShowing(this.mShowing);
        }
        if (z3) {
            notifyDefaultDisplayCallbacks(z);
            updateActivityLockScreenState(z, z4);
        }
    }

    private void notifyDefaultDisplayCallbacks(boolean z) {
        DejankUtils.whitelistIpcs(new Runnable(z) { // from class: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$yjVkN_s0X965Oz99BRDYm2QmmVk
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                KeyguardViewMediator.this.lambda$notifyDefaultDisplayCallbacks$8$KeyguardViewMediator(this.f$1);
            }
        });
        updateInputRestrictedLocked();
        this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.keyguard.-$$Lambda$KeyguardViewMediator$9w77yKhmKyk51EFfW0CD4wjMErQ
            @Override // java.lang.Runnable
            public final void run() {
                KeyguardViewMediator.this.lambda$notifyDefaultDisplayCallbacks$9$KeyguardViewMediator();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$notifyDefaultDisplayCallbacks$8 */
    public /* synthetic */ void lambda$notifyDefaultDisplayCallbacks$8$KeyguardViewMediator(boolean z) {
        for (int size = this.mKeyguardStateCallbacks.size() - 1; size >= 0; size--) {
            IKeyguardStateCallback iKeyguardStateCallback = this.mKeyguardStateCallbacks.get(size);
            try {
                iKeyguardStateCallback.onShowingStateChanged(z);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onShowingStateChanged", e);
                if (e instanceof DeadObjectException) {
                    this.mKeyguardStateCallbacks.remove(iKeyguardStateCallback);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$notifyDefaultDisplayCallbacks$9 */
    public /* synthetic */ void lambda$notifyDefaultDisplayCallbacks$9$KeyguardViewMediator() {
        this.mTrustManager.reportKeyguardShowingChanged();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyTrustedChangedLocked(boolean z) {
        for (int size = this.mKeyguardStateCallbacks.size() - 1; size >= 0; size--) {
            try {
                this.mKeyguardStateCallbacks.get(size).onTrustedChanged(z);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call notifyTrustedChangedLocked", e);
                if (e instanceof DeadObjectException) {
                    this.mKeyguardStateCallbacks.remove(size);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyHasLockscreenWallpaperChanged(boolean z) {
        for (int size = this.mKeyguardStateCallbacks.size() - 1; size >= 0; size--) {
            try {
                this.mKeyguardStateCallbacks.get(size).onHasLockscreenWallpaperChanged(z);
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call onHasLockscreenWallpaperChanged", e);
                if (e instanceof DeadObjectException) {
                    this.mKeyguardStateCallbacks.remove(size);
                }
            }
        }
    }

    public void addStateMonitorCallback(IKeyguardStateCallback iKeyguardStateCallback) {
        synchronized (this) {
            this.mKeyguardStateCallbacks.add(iKeyguardStateCallback);
            try {
                iKeyguardStateCallback.onSimSecureStateChanged(this.mUpdateMonitor.isSimPinSecure());
                iKeyguardStateCallback.onShowingStateChanged(this.mShowing);
                iKeyguardStateCallback.onInputRestrictedStateChanged(this.mInputRestricted);
                iKeyguardStateCallback.onTrustedChanged(this.mUpdateMonitor.getUserHasTrust(KeyguardUpdateMonitor.getCurrentUser()));
                iKeyguardStateCallback.onHasLockscreenWallpaperChanged(this.mUpdateMonitor.hasLockscreenWallpaper());
            } catch (RemoteException e) {
                Slog.w("KeyguardViewMediator", "Failed to call to IKeyguardStateCallback", e);
            }
        }
    }

    public void notifyUnlockCounter(boolean z) {
        this.mKeyguardUnlockCounter.reportEvent(z, SystemClock.elapsedRealtime());
    }

    /* access modifiers changed from: private */
    public static class DismissMessage {
        private final IKeyguardDismissCallback mCallback;
        private final CharSequence mMessage;

        DismissMessage(IKeyguardDismissCallback iKeyguardDismissCallback, CharSequence charSequence) {
            this.mCallback = iKeyguardDismissCallback;
            this.mMessage = charSequence;
        }

        public IKeyguardDismissCallback getCallback() {
            return this.mCallback;
        }

        public CharSequence getMessage() {
            return this.mMessage;
        }
    }

    public void setAodShowing(boolean z) {
        setShowingLocked(this.mShowing);
    }
}
