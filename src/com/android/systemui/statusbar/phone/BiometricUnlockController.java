package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.biometrics.BiometricSourceType;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardViewController;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.systemui.statusbar.phone.OpBiometricUnlockController;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
public class BiometricUnlockController extends OpBiometricUnlockController implements Dumpable {
    private static final UiEventLogger UI_EVENT_LOGGER = new UiEventLoggerImpl();
    private final Context mContext;
    private final DozeParameters mDozeParameters;
    private DozeScrimController mDozeScrimController;
    private boolean mFadedAwayAfterWakeAndUnlock;
    private final Handler mHandler;
    private boolean mHasScreenTurnedOnSinceAuthenticating;
    private final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardStateController mKeyguardStateController;
    private KeyguardViewController mKeyguardViewController;
    private KeyguardViewMediator mKeyguardViewMediator;
    private final NotificationMediaManager mMediaManager;
    private final MetricsLogger mMetricsLogger;
    private int mMode;
    private final NotificationShadeWindowController mNotificationShadeWindowController;
    private PendingAuthenticated mPendingAuthenticated = null;
    private boolean mPendingShowBouncer;
    private final PowerManager mPowerManager;
    private final Runnable mReleaseBiometricWakeLockRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.BiometricUnlockController.1
        @Override // java.lang.Runnable
        public void run() {
            Log.i("BiometricUnlockCtrl", "biometric wakelock: TIMEOUT!!");
            BiometricUnlockController.this.releaseBiometricWakeLock();
        }
    };
    private final ScreenLifecycle.Observer mScreenObserver = new ScreenLifecycle.Observer() { // from class: com.android.systemui.statusbar.phone.BiometricUnlockController.4
        @Override // com.android.systemui.keyguard.ScreenLifecycle.Observer
        public void onScreenTurnedOn() {
            BiometricUnlockController.this.mHasScreenTurnedOnSinceAuthenticating = true;
        }
    };
    private final ShadeController mShadeController;
    private StatusBar mStatusBar;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private PowerManager.WakeLock mWakeLock;
    private final int mWakeUpDelay;
    @VisibleForTesting
    final WakefulnessLifecycle.Observer mWakefulnessObserver = new WakefulnessLifecycle.Observer() { // from class: com.android.systemui.statusbar.phone.BiometricUnlockController.3
        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedWakingUp() {
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedWakingUp() {
            if (BiometricUnlockController.this.mPendingShowBouncer) {
                BiometricUnlockController.this.showBouncer();
            }
        }
    };

    /* access modifiers changed from: private */
    public static final class PendingAuthenticated {
        public final BiometricSourceType biometricSourceType;
        public final boolean isStrongBiometric;
        public final int userId;

        PendingAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            this.userId = i;
            this.biometricSourceType = biometricSourceType;
            this.isStrongBiometric = z;
        }
    }

    @VisibleForTesting
    public enum BiometricUiEvent implements UiEventLogger.UiEventEnum {
        BIOMETRIC_FINGERPRINT_SUCCESS(396),
        BIOMETRIC_FINGERPRINT_FAILURE(397),
        BIOMETRIC_FINGERPRINT_ERROR(398),
        BIOMETRIC_FACE_SUCCESS(399),
        BIOMETRIC_FACE_FAILURE(400),
        BIOMETRIC_FACE_ERROR(401),
        BIOMETRIC_IRIS_SUCCESS(402),
        BIOMETRIC_IRIS_FAILURE(403),
        BIOMETRIC_IRIS_ERROR(404);
        
        static final Map<BiometricSourceType, BiometricUiEvent> ERROR_EVENT_BY_SOURCE_TYPE = Map.of(BiometricSourceType.FINGERPRINT, BIOMETRIC_FINGERPRINT_ERROR, BiometricSourceType.FACE, BIOMETRIC_FACE_ERROR, BiometricSourceType.IRIS, BIOMETRIC_IRIS_ERROR);
        static final Map<BiometricSourceType, BiometricUiEvent> FAILURE_EVENT_BY_SOURCE_TYPE = Map.of(BiometricSourceType.FINGERPRINT, BIOMETRIC_FINGERPRINT_FAILURE, BiometricSourceType.FACE, BIOMETRIC_FACE_FAILURE, BiometricSourceType.IRIS, BIOMETRIC_IRIS_FAILURE);
        static final Map<BiometricSourceType, BiometricUiEvent> SUCCESS_EVENT_BY_SOURCE_TYPE = Map.of(BiometricSourceType.FINGERPRINT, BIOMETRIC_FINGERPRINT_SUCCESS, BiometricSourceType.FACE, BIOMETRIC_FACE_SUCCESS, BiometricSourceType.IRIS, BIOMETRIC_IRIS_SUCCESS);
        private final int mId;

        private BiometricUiEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public BiometricUnlockController(Context context, DozeScrimController dozeScrimController, KeyguardViewMediator keyguardViewMediator, ScrimController scrimController, StatusBar statusBar, ShadeController shadeController, NotificationShadeWindowController notificationShadeWindowController, KeyguardStateController keyguardStateController, Handler handler, KeyguardUpdateMonitor keyguardUpdateMonitor, Resources resources, KeyguardBypassController keyguardBypassController, DozeParameters dozeParameters, MetricsLogger metricsLogger, DumpManager dumpManager) {
        super(context, keyguardViewMediator, statusBar);
        this.mContext = context;
        this.mPowerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mShadeController = shadeController;
        this.mUpdateMonitor = keyguardUpdateMonitor;
        this.mDozeParameters = dozeParameters;
        keyguardUpdateMonitor.registerCallback(this);
        this.mMediaManager = (NotificationMediaManager) Dependency.get(NotificationMediaManager.class);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).addObserver(this.mWakefulnessObserver);
        ((ScreenLifecycle) Dependency.get(ScreenLifecycle.class)).addObserver(this.mScreenObserver);
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        this.mDozeScrimController = dozeScrimController;
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mStatusBar = statusBar;
        this.mKeyguardStateController = keyguardStateController;
        this.mHandler = handler;
        this.mWakeUpDelay = resources.getInteger(17694932);
        this.mKeyguardBypassController = keyguardBypassController;
        keyguardBypassController.setUnlockController(this);
        this.mMetricsLogger = metricsLogger;
        dumpManager.registerDumpable(BiometricUnlockController.class.getName(), this);
    }

    public void setKeyguardViewController(KeyguardViewController keyguardViewController) {
        this.mKeyguardViewController = keyguardViewController;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseBiometricWakeLock() {
        if (this.mWakeLock != null) {
            this.mHandler.removeCallbacks(this.mReleaseBiometricWakeLockRunnable);
            Log.i("BiometricUnlockCtrl", "releasing biometric wakelock");
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    public void acquireWakeLock() {
        PowerManager.WakeLock newWakeLock = this.mPowerManager.newWakeLock(1, "wake-and-unlock:wakelock");
        this.mWakeLock = newWakeLock;
        newWakeLock.acquire();
        this.mHandler.postDelayed(this.mReleaseBiometricWakeLockRunnable, 15000);
        Log.i("BiometricUnlockCtrl", "fingerprint acquired, grabbing biometric wakelock");
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricAcquired(BiometricSourceType biometricSourceType) {
        Trace.beginSection("BiometricUnlockController#onBiometricAcquired");
        releaseBiometricWakeLock();
        if (!this.mUpdateMonitor.isDeviceInteractive()) {
            if (LatencyTracker.isEnabled(this.mContext)) {
                int i = 2;
                if (biometricSourceType == BiometricSourceType.FACE) {
                    i = 6;
                }
                LatencyTracker.getInstance(this.mContext).onActionStart(i);
            }
            this.mWakeLock = this.mPowerManager.newWakeLock(1, "wake-and-unlock:wakelock");
            Trace.beginSection("acquiring wake-and-unlock");
            this.mWakeLock.acquire();
            Trace.endSection();
            Log.i("BiometricUnlockCtrl", "biometric acquired, grabbing biometric wakelock");
            this.mHandler.postDelayed(this.mReleaseBiometricWakeLockRunnable, 15000);
        }
        Trace.endSection();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
        Trace.beginSection("BiometricUnlockController#onBiometricAuthenticated");
        setFingerprintState(false, this.mStatusBar.isBouncerShowing() ? 2 : 3);
        if (this.mUpdateMonitor.isGoingToSleep()) {
            this.mPendingAuthenticated = new PendingAuthenticated(i, biometricSourceType, z);
            Trace.endSection();
            return;
        }
        this.mMetricsLogger.write(new LogMaker(1697).setType(10).setSubtype(toSubtype(biometricSourceType)));
        Optional ofNullable = Optional.ofNullable(BiometricUiEvent.SUCCESS_EVENT_BY_SOURCE_TYPE.get(biometricSourceType));
        UiEventLogger uiEventLogger = UI_EVENT_LOGGER;
        Objects.requireNonNull(uiEventLogger);
        ofNullable.ifPresent(new Consumer(uiEventLogger) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$DYeRSGfkACOmMKintAq15p0aYRA
            public final /* synthetic */ UiEventLogger f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.log((BiometricUnlockController.BiometricUiEvent) obj);
            }
        });
        if (this.mKeyguardBypassController.onBiometricAuthenticated(biometricSourceType, z)) {
            OpUtils.setRecentUnlockBiometricFinger(true);
            this.mKeyguardViewMediator.userActivity();
            this.mStatusBar.getPanelController().stopTrackingAfterUnlock();
            startWakeAndUnlock(biometricSourceType, z);
            return;
        }
        Log.d("BiometricUnlockCtrl", "onBiometricAuthenticated aborted by bypass controller");
    }

    public void startWakeAndUnlock(BiometricSourceType biometricSourceType, boolean z) {
        startWakeAndUnlock(calculateMode(biometricSourceType, z));
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00b7  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void startWakeAndUnlock(int r8) {
        /*
        // Method dump skipped, instructions count: 296
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.BiometricUnlockController.startWakeAndUnlock(int):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startWakeAndUnlock$0 */
    public /* synthetic */ void lambda$startWakeAndUnlock$0$BiometricUnlockController(boolean z, boolean z2) {
        if (!z) {
            Log.i("BiometricUnlockCtrl", "bio wakelock: Authenticated, waking up...");
            this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), 4, "android.policy:BIOMETRIC");
        }
        if (z2) {
            this.mKeyguardViewMediator.onWakeAndUnlocking();
        }
        Trace.beginSection("release wake-and-unlock");
        releaseBiometricWakeLock();
        this.mIsScreenOffUnlock = false;
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showBouncer() {
        super.opShowBouncer();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onStartedGoingToSleep(int i) {
        resetMode();
        this.mFadedAwayAfterWakeAndUnlock = false;
        this.mPendingAuthenticated = null;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onFinishedGoingToSleep(int i) {
        Trace.beginSection("BiometricUnlockController#onFinishedGoingToSleep");
        PendingAuthenticated pendingAuthenticated = this.mPendingAuthenticated;
        if (pendingAuthenticated != null) {
            this.mHandler.post(new Runnable(pendingAuthenticated) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$BiometricUnlockController$WXzEzz1fr3GrmjWXzyYSNPAnvmA
                public final /* synthetic */ BiometricUnlockController.PendingAuthenticated f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    BiometricUnlockController.this.lambda$onFinishedGoingToSleep$1$BiometricUnlockController(this.f$1);
                }
            });
            this.mPendingAuthenticated = null;
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishedGoingToSleep$1 */
    public /* synthetic */ void lambda$onFinishedGoingToSleep$1$BiometricUnlockController(PendingAuthenticated pendingAuthenticated) {
        onBiometricAuthenticated(pendingAuthenticated.userId, pendingAuthenticated.biometricSourceType, pendingAuthenticated.isStrongBiometric);
    }

    public boolean hasPendingAuthentication() {
        PendingAuthenticated pendingAuthenticated = this.mPendingAuthenticated;
        return pendingAuthenticated != null && this.mUpdateMonitor.isUnlockingWithBiometricAllowed(pendingAuthenticated.isStrongBiometric) && this.mPendingAuthenticated.userId == KeyguardUpdateMonitor.getCurrentUser();
    }

    public int getMode() {
        return this.mMode;
    }

    private int calculateMode(BiometricSourceType biometricSourceType, boolean z) {
        if (biometricSourceType == BiometricSourceType.FACE || biometricSourceType == BiometricSourceType.IRIS) {
            return calculateModeForPassiveAuth(z);
        }
        return calculateModeForFingerprint(z);
    }

    private int calculateModeForFingerprint(boolean z) {
        boolean isUnlockingWithBiometricAllowed = this.mUpdateMonitor.isUnlockingWithBiometricAllowed(z);
        boolean isDreaming = this.mUpdateMonitor.isDreaming();
        if (!this.mUpdateMonitor.isDeviceInteractive()) {
            if (!this.mKeyguardViewController.isShowing()) {
                return 4;
            }
            if (this.mDozeScrimController.isPulsing() && isUnlockingWithBiometricAllowed) {
                return 2;
            }
            if (isUnlockingWithBiometricAllowed || !this.mKeyguardStateController.isMethodSecure()) {
                return 1;
            }
            return 3;
        } else if (isUnlockingWithBiometricAllowed && isDreaming) {
            return 6;
        } else {
            if (!this.mKeyguardViewController.isShowing()) {
                return 0;
            }
            if (this.mKeyguardViewController.bouncerIsOrWillBeShowing() && isUnlockingWithBiometricAllowed) {
                return 8;
            }
            if (isUnlockingWithBiometricAllowed) {
                return 5;
            }
            if (!this.mKeyguardViewController.isBouncerShowing()) {
                return 3;
            }
            return 0;
        }
    }

    private int calculateModeForPassiveAuth(boolean z) {
        boolean isUnlockingWithBiometricAllowed = this.mUpdateMonitor.isUnlockingWithBiometricAllowed(z);
        boolean isDreaming = this.mUpdateMonitor.isDreaming();
        boolean bypassEnabled = this.mKeyguardBypassController.getBypassEnabled();
        if (!this.mUpdateMonitor.isDeviceInteractive()) {
            if (!this.mKeyguardViewController.isShowing()) {
                if (bypassEnabled) {
                    return 1;
                }
                return 4;
            } else if (!isUnlockingWithBiometricAllowed) {
                return bypassEnabled ? 3 : 0;
            } else {
                if (this.mDozeScrimController.isPulsing()) {
                    if (bypassEnabled) {
                        return 2;
                    }
                    return 4;
                } else if (bypassEnabled) {
                    return 2;
                } else {
                    return 4;
                }
            }
        } else if (!isUnlockingWithBiometricAllowed || !isDreaming) {
            if (!this.mKeyguardViewController.isShowing()) {
                return 0;
            }
            if (!this.mKeyguardViewController.bouncerIsOrWillBeShowing() || !isUnlockingWithBiometricAllowed) {
                if (!isUnlockingWithBiometricAllowed) {
                    return bypassEnabled ? 3 : 0;
                }
                if (bypassEnabled) {
                    return 7;
                }
                return 0;
            } else if (!bypassEnabled || !this.mKeyguardBypassController.canPlaySubtleWindowAnimations()) {
                return 8;
            } else {
                return 7;
            }
        } else if (bypassEnabled) {
            return 6;
        } else {
            return 4;
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
        this.mMetricsLogger.write(new LogMaker(1697).setType(11).setSubtype(toSubtype(biometricSourceType)));
        Optional ofNullable = Optional.ofNullable(BiometricUiEvent.FAILURE_EVENT_BY_SOURCE_TYPE.get(biometricSourceType));
        UiEventLogger uiEventLogger = UI_EVENT_LOGGER;
        Objects.requireNonNull(uiEventLogger);
        ofNullable.ifPresent(new Consumer(uiEventLogger) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$DYeRSGfkACOmMKintAq15p0aYRA
            public final /* synthetic */ UiEventLogger f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.log((BiometricUnlockController.BiometricUiEvent) obj);
            }
        });
        if (biometricSourceType == BiometricSourceType.FINGERPRINT) {
            onFingerprintAuthFailed();
        }
        cleanup();
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) {
        this.mMetricsLogger.write(new LogMaker(1697).setType(15).setSubtype(toSubtype(biometricSourceType)).addTaggedData(1741, Integer.valueOf(i)));
        Optional ofNullable = Optional.ofNullable(BiometricUiEvent.ERROR_EVENT_BY_SOURCE_TYPE.get(biometricSourceType));
        UiEventLogger uiEventLogger = UI_EVENT_LOGGER;
        Objects.requireNonNull(uiEventLogger);
        ofNullable.ifPresent(new Consumer(uiEventLogger) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$DYeRSGfkACOmMKintAq15p0aYRA
            public final /* synthetic */ UiEventLogger f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.log((BiometricUnlockController.BiometricUiEvent) obj);
            }
        });
        Log.d("BiometricUnlockCtrl", "onFingerprintError: " + str);
        onFingerprintUnlockCancel(2);
        cleanup();
    }

    private void cleanup() {
        releaseBiometricWakeLock();
        this.mIsScreenOffUnlock = false;
    }

    public void startKeyguardFadingAway() {
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.phone.BiometricUnlockController.2
            @Override // java.lang.Runnable
            public void run() {
                BiometricUnlockController.this.mNotificationShadeWindowController.setForceDozeBrightness(false);
            }
        }, 96);
    }

    public void finishKeyguardFadingAway() {
        if (isWakeAndUnlock()) {
            this.mFadedAwayAfterWakeAndUnlock = true;
        }
        resetMode();
    }

    private void resetMode() {
        Log.d("BiometricUnlockCtrl", "resetMode");
        this.mMode = 0;
        this.mNotificationShadeWindowController.setForceDozeBrightness(false);
        if (this.mStatusBar.getNavigationBarView() != null) {
            this.mStatusBar.getNavigationBarView().setWakeAndUnlocking(false);
        }
        this.mStatusBar.notifyBiometricAuthModeChanged();
        setFaceLockMode(0);
        changePanelVisibilityByAlpha(1, true);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(" BiometricUnlockController:");
        printWriter.print("   mMode=");
        printWriter.println(this.mMode);
        printWriter.print("   mWakeLock=");
        printWriter.println(this.mWakeLock);
    }

    public boolean isWakeAndUnlock() {
        int i = this.mMode;
        return i == 1 || i == 2 || i == 6;
    }

    public boolean unlockedByWakeAndUnlock() {
        return isWakeAndUnlock() || this.mFadedAwayAfterWakeAndUnlock;
    }

    public boolean isBiometricUnlock() {
        return opIsBiometricUnlock();
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.phone.BiometricUnlockController$5  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$android$hardware$biometrics$BiometricSourceType;

        static {
            int[] iArr = new int[BiometricSourceType.values().length];
            $SwitchMap$android$hardware$biometrics$BiometricSourceType = iArr;
            try {
                iArr[BiometricSourceType.FINGERPRINT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$android$hardware$biometrics$BiometricSourceType[BiometricSourceType.FACE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$android$hardware$biometrics$BiometricSourceType[BiometricSourceType.IRIS.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private int toSubtype(BiometricSourceType biometricSourceType) {
        int i = AnonymousClass5.$SwitchMap$android$hardware$biometrics$BiometricSourceType[biometricSourceType.ordinal()];
        if (i == 1) {
            return 0;
        }
        if (i != 2) {
            return i != 3 ? 3 : 2;
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.systemui.statusbar.phone.OpBiometricUnlockController
    public ShadeController getShadeController() {
        return this.mShadeController;
    }
}
