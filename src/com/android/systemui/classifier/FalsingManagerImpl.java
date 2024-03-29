package com.android.systemui.classifier;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.biometrics.BiometricSourceType;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.analytics.DataCollector;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.StatusBarState;
import com.android.systemui.util.sensors.AsyncSensorManager;
import java.io.PrintWriter;
import java.util.concurrent.Executor;
public class FalsingManagerImpl implements FalsingManager {
    private static final int[] CLASSIFIER_SENSORS = {8};
    private static final int[] COLLECTOR_SENSORS = {1, 4, 8, 5, 11};
    private final AccessibilityManager mAccessibilityManager;
    private boolean mBouncerOffOnDown = false;
    private boolean mBouncerOn = false;
    private final Context mContext;
    private final DataCollector mDataCollector;
    private boolean mEnforceBouncer = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final HumanInteractionClassifier mHumanInteractionClassifier;
    private int mIsFalseTouchCalls;
    private boolean mIsTouchScreen = true;
    private boolean mJustUnlockedWithFace = false;
    private final KeyguardUpdateMonitorCallback mKeyguardUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.classifier.FalsingManagerImpl.4
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            if (i == KeyguardUpdateMonitor.getCurrentUser() && biometricSourceType == BiometricSourceType.FACE) {
                FalsingManagerImpl.this.mJustUnlockedWithFace = true;
            }
        }
    };
    private MetricsLogger mMetricsLogger;
    private Runnable mPendingWtf;
    private boolean mScreenOn;
    private SensorEventListener mSensorEventListener = new SensorEventListener() { // from class: com.android.systemui.classifier.FalsingManagerImpl.1
        @Override // android.hardware.SensorEventListener
        public synchronized void onSensorChanged(SensorEvent sensorEvent) {
            FalsingManagerImpl.this.mDataCollector.onSensorChanged(sensorEvent);
            FalsingManagerImpl.this.mHumanInteractionClassifier.onSensorChanged(sensorEvent);
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
            FalsingManagerImpl.this.mDataCollector.onAccuracyChanged(sensor, i);
        }
    };
    private final SensorManager mSensorManager;
    private boolean mSessionActive = false;
    protected final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) { // from class: com.android.systemui.classifier.FalsingManagerImpl.3
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            FalsingManagerImpl.this.updateConfiguration();
        }
    };
    private boolean mShowingAod;
    private int mState = 0;
    public StatusBarStateController.StateListener mStatusBarStateListener = new StatusBarStateController.StateListener() { // from class: com.android.systemui.classifier.FalsingManagerImpl.2
        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onStateChanged(int i) {
            if (FalsingLog.ENABLED) {
                FalsingLog.i("setStatusBarState", "from=" + StatusBarState.toShortString(FalsingManagerImpl.this.mState) + " to=" + StatusBarState.toShortString(i));
            }
            FalsingManagerImpl.this.mState = i;
            FalsingManagerImpl.this.updateSessionActive();
        }
    };
    private final Executor mUiBgExecutor;

    FalsingManagerImpl(Context context, Executor executor) {
        this.mContext = context;
        this.mSensorManager = (SensorManager) Dependency.get(AsyncSensorManager.class);
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mDataCollector = DataCollector.getInstance(this.mContext);
        this.mHumanInteractionClassifier = HumanInteractionClassifier.getInstance(this.mContext);
        this.mUiBgExecutor = executor;
        this.mScreenOn = ((PowerManager) context.getSystemService(PowerManager.class)).isInteractive();
        this.mMetricsLogger = new MetricsLogger();
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("falsing_manager_enforce_bouncer"), false, this.mSettingsObserver, -1);
        updateConfiguration();
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback(this.mStatusBarStateListener);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mKeyguardUpdateCallback);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConfiguration() {
        boolean z = false;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "falsing_manager_enforce_bouncer", 0) != 0) {
            z = true;
        }
        this.mEnforceBouncer = z;
    }

    private boolean shouldSessionBeActive() {
        boolean z = FalsingLog.ENABLED;
        return isEnabled() && this.mScreenOn && this.mState == 1 && !this.mShowingAod;
    }

    private boolean sessionEntrypoint() {
        if (this.mSessionActive || !shouldSessionBeActive()) {
            return false;
        }
        onSessionStart();
        return true;
    }

    private void sessionExitpoint(boolean z) {
        if (!this.mSessionActive) {
            return;
        }
        if (z || !shouldSessionBeActive()) {
            this.mSessionActive = false;
            if (this.mIsFalseTouchCalls != 0) {
                if (FalsingLog.ENABLED) {
                    FalsingLog.i("isFalseTouchCalls", "Calls before failure: " + this.mIsFalseTouchCalls);
                }
                this.mMetricsLogger.histogram("falsing_failure_after_attempts", this.mIsFalseTouchCalls);
                this.mIsFalseTouchCalls = 0;
            }
            this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.classifier.-$$Lambda$FalsingManagerImpl$8SXkW2Wsm8XWKvooYKTPgEEzXnU
                @Override // java.lang.Runnable
                public final void run() {
                    FalsingManagerImpl.this.lambda$sessionExitpoint$0$FalsingManagerImpl();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$sessionExitpoint$0 */
    public /* synthetic */ void lambda$sessionExitpoint$0$FalsingManagerImpl() {
        this.mSensorManager.unregisterListener(this.mSensorEventListener);
    }

    public void updateSessionActive() {
        if (shouldSessionBeActive()) {
            sessionEntrypoint();
        } else {
            sessionExitpoint(false);
        }
    }

    private void onSessionStart() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onSessionStart", "classifierEnabled=" + isClassifierEnabled());
            clearPendingWtf();
        }
        this.mBouncerOn = false;
        this.mSessionActive = true;
        this.mJustUnlockedWithFace = false;
        this.mIsFalseTouchCalls = 0;
        if (this.mHumanInteractionClassifier.isEnabled()) {
            registerSensors(CLASSIFIER_SENSORS);
        }
        if (this.mDataCollector.isEnabledFull()) {
            registerSensors(COLLECTOR_SENSORS);
        }
        if (this.mDataCollector.isEnabled()) {
            this.mDataCollector.onFalsingSessionStarted();
        }
    }

    private void registerSensors(int[] iArr) {
        for (int i : iArr) {
            Sensor defaultSensor = this.mSensorManager.getDefaultSensor(i);
            if (defaultSensor != null) {
                this.mUiBgExecutor.execute(new Runnable(defaultSensor) { // from class: com.android.systemui.classifier.-$$Lambda$FalsingManagerImpl$VJW_VOVtQGpUmd7AtKlCfAEhBZE
                    public final /* synthetic */ Sensor f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        FalsingManagerImpl.this.lambda$registerSensors$1$FalsingManagerImpl(this.f$1);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$registerSensors$1 */
    public /* synthetic */ void lambda$registerSensors$1$FalsingManagerImpl(Sensor sensor) {
        this.mSensorManager.registerListener(this.mSensorEventListener, sensor, 1);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isClassifierEnabled() {
        return this.mHumanInteractionClassifier.isEnabled();
    }

    private boolean isEnabled() {
        return this.mHumanInteractionClassifier.isEnabled() || this.mDataCollector.isEnabled();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isUnlockingDisabled() {
        return this.mDataCollector.isUnlockingDisabled();
    }

    /* JADX WARN: Type inference failed for: r3v4, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r4v1, types: [boolean, int] */
    /* JADX WARNING: Unknown variable types count: 2 */
    @Override // com.android.systemui.plugins.FalsingManager
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isFalseTouch() {
        /*
            r7 = this;
            boolean r0 = com.android.systemui.classifier.FalsingLog.ENABLED
            if (r0 == 0) goto L_0x006f
            boolean r0 = r7.mSessionActive
            if (r0 != 0) goto L_0x006f
            android.content.Context r0 = r7.mContext
            java.lang.Class<android.os.PowerManager> r1 = android.os.PowerManager.class
            java.lang.Object r0 = r0.getSystemService(r1)
            android.os.PowerManager r0 = (android.os.PowerManager) r0
            boolean r0 = r0.isInteractive()
            if (r0 == 0) goto L_0x006f
            java.lang.Runnable r0 = r7.mPendingWtf
            if (r0 != 0) goto L_0x006f
            boolean r3 = r7.isEnabled()
            boolean r4 = r7.mScreenOn
            int r0 = r7.mState
            java.lang.String r5 = com.android.systemui.statusbar.StatusBarState.toShortString(r0)
            java.lang.Throwable r6 = new java.lang.Throwable
            java.lang.String r0 = "here"
            r6.<init>(r0)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "Session is not active, yet there's a query for a false touch."
            r0.append(r1)
            java.lang.String r1 = " enabled="
            r0.append(r1)
            r0.append(r3)
            java.lang.String r1 = " mScreenOn="
            r0.append(r1)
            r0.append(r4)
            java.lang.String r1 = " mState="
            r0.append(r1)
            r0.append(r5)
            java.lang.String r1 = ". Escalating to WTF if screen does not turn on soon."
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "isFalseTouch"
            com.android.systemui.classifier.FalsingLog.wLogcat(r1, r0)
            com.android.systemui.classifier.-$$Lambda$FalsingManagerImpl$v5ZF-PRlWWHHEjWpilJxodWNKMI r0 = new com.android.systemui.classifier.-$$Lambda$FalsingManagerImpl$v5ZF-PRlWWHHEjWpilJxodWNKMI
            r1 = r0
            r2 = r7
            r1.<init>(r3, r4, r5, r6)
            r7.mPendingWtf = r0
            android.os.Handler r1 = r7.mHandler
            r2 = 1000(0x3e8, double:4.94E-321)
            r1.postDelayed(r0, r2)
        L_0x006f:
            boolean r0 = android.app.ActivityManager.isRunningInUserTestHarness()
            r1 = 0
            if (r0 == 0) goto L_0x0077
            return r1
        L_0x0077:
            android.view.accessibility.AccessibilityManager r0 = r7.mAccessibilityManager
            boolean r0 = r0.isTouchExplorationEnabled()
            if (r0 == 0) goto L_0x0080
            return r1
        L_0x0080:
            boolean r0 = r7.mIsTouchScreen
            if (r0 != 0) goto L_0x0085
            return r1
        L_0x0085:
            boolean r0 = r7.mJustUnlockedWithFace
            if (r0 == 0) goto L_0x008a
            return r1
        L_0x008a:
            int r0 = r7.mIsFalseTouchCalls
            int r0 = r0 + 1
            r7.mIsFalseTouchCalls = r0
            com.android.systemui.classifier.HumanInteractionClassifier r0 = r7.mHumanInteractionClassifier
            boolean r0 = r0.isFalseTouch()
            if (r0 != 0) goto L_0x00bf
            boolean r2 = com.android.systemui.classifier.FalsingLog.ENABLED
            if (r2 == 0) goto L_0x00b4
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Calls before success: "
            r2.append(r3)
            int r3 = r7.mIsFalseTouchCalls
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            java.lang.String r3 = "isFalseTouchCalls"
            com.android.systemui.classifier.FalsingLog.i(r3, r2)
        L_0x00b4:
            com.android.internal.logging.MetricsLogger r2 = r7.mMetricsLogger
            int r3 = r7.mIsFalseTouchCalls
            java.lang.String r4 = "falsing_success_after_attempts"
            r2.histogram(r4, r3)
            r7.mIsFalseTouchCalls = r1
        L_0x00bf:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.classifier.FalsingManagerImpl.isFalseTouch():boolean");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$isFalseTouch$2 */
    public /* synthetic */ void lambda$isFalseTouch$2$FalsingManagerImpl(int i, int i2, String str, Throwable th) {
        FalsingLog.wtf("isFalseTouch", "Session did not become active after query for a false touch. enabled=" + i + '/' + (isEnabled() ? 1 : 0) + " mScreenOn=" + i2 + '/' + (this.mScreenOn ? 1 : 0) + " mState=" + str + '/' + StatusBarState.toShortString(this.mState) + ". Look for warnings ~1000ms earlier to see root cause.", th);
    }

    private void clearPendingWtf() {
        Runnable runnable = this.mPendingWtf;
        if (runnable != null) {
            this.mHandler.removeCallbacks(runnable);
            this.mPendingWtf = null;
        }
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean shouldEnforceBouncer() {
        return this.mEnforceBouncer;
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setShowingAod(boolean z) {
        this.mShowingAod = z;
        updateSessionActive();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenTurningOn() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onScreenTurningOn", "from=" + (this.mScreenOn ? 1 : 0));
            clearPendingWtf();
        }
        this.mScreenOn = true;
        if (sessionEntrypoint()) {
            this.mDataCollector.onScreenTurningOn();
        }
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenOnFromTouch() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onScreenOnFromTouch", "from=" + (this.mScreenOn ? 1 : 0));
        }
        this.mScreenOn = true;
        if (sessionEntrypoint()) {
            this.mDataCollector.onScreenOnFromTouch();
        }
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenOff() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onScreenOff", "from=" + (this.mScreenOn ? 1 : 0));
        }
        this.mDataCollector.onScreenOff();
        this.mScreenOn = false;
        sessionExitpoint(false);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onSuccessfulUnlock() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onSucccessfulUnlock", "");
        }
        this.mDataCollector.onSucccessfulUnlock();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onBouncerShown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onBouncerShown", "from=" + (this.mBouncerOn ? 1 : 0));
        }
        if (!this.mBouncerOn) {
            this.mBouncerOn = true;
            this.mDataCollector.onBouncerShown();
        }
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onBouncerHidden() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onBouncerHidden", "from=" + (this.mBouncerOn ? 1 : 0));
        }
        if (this.mBouncerOn) {
            this.mBouncerOn = false;
            this.mDataCollector.onBouncerHidden();
        }
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onQsDown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onQsDown", "");
        }
        this.mHumanInteractionClassifier.setType(0);
        this.mDataCollector.onQsDown();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setQsExpanded(boolean z) {
        this.mDataCollector.setQsExpanded(z);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTrackingStarted(boolean z) {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onTrackingStarted", "");
        }
        this.mHumanInteractionClassifier.setType(z ? 8 : 4);
        this.mDataCollector.onTrackingStarted();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTrackingStopped() {
        this.mDataCollector.onTrackingStopped();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationActive() {
        this.mDataCollector.onNotificationActive();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationDoubleTap(boolean z, float f, float f2) {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onNotificationDoubleTap", "accepted=" + z + " dx=" + f + " dy=" + f2 + " (px)");
        }
        this.mDataCollector.onNotificationDoubleTap();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setNotificationExpanded() {
        this.mDataCollector.setNotificationExpanded();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificatonStartDraggingDown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onNotificatonStartDraggingDown", "");
        }
        this.mHumanInteractionClassifier.setType(2);
        this.mDataCollector.onNotificatonStartDraggingDown();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onStartExpandingFromPulse() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onStartExpandingFromPulse", "");
        }
        this.mHumanInteractionClassifier.setType(9);
        this.mDataCollector.onStartExpandingFromPulse();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificatonStopDraggingDown() {
        this.mDataCollector.onNotificatonStopDraggingDown();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onExpansionFromPulseStopped() {
        this.mDataCollector.onExpansionFromPulseStopped();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationDismissed() {
        this.mDataCollector.onNotificationDismissed();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationStartDismissing() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onNotificationStartDismissing", "");
        }
        this.mHumanInteractionClassifier.setType(1);
        this.mDataCollector.onNotificatonStartDismissing();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationStopDismissing() {
        this.mDataCollector.onNotificatonStopDismissing();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onCameraOn() {
        this.mDataCollector.onCameraOn();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onLeftAffordanceOn() {
        this.mDataCollector.onLeftAffordanceOn();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onAffordanceSwipingStarted(boolean z) {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onAffordanceSwipingStarted", "");
        }
        if (z) {
            this.mHumanInteractionClassifier.setType(6);
        } else {
            this.mHumanInteractionClassifier.setType(5);
        }
        this.mDataCollector.onAffordanceSwipingStarted(z);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onAffordanceSwipingAborted() {
        this.mDataCollector.onAffordanceSwipingAborted();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onUnlockHintStarted() {
        this.mDataCollector.onUnlockHintStarted();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onCameraHintStarted() {
        this.mDataCollector.onCameraHintStarted();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onLeftAffordanceHintStarted() {
        this.mDataCollector.onLeftAffordanceHintStarted();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTouchEvent(MotionEvent motionEvent, int i, int i2) {
        if (motionEvent.getAction() == 0) {
            this.mIsTouchScreen = motionEvent.isFromSource(4098);
            this.mBouncerOffOnDown = !this.mBouncerOn;
        }
        if (this.mSessionActive) {
            if (!this.mBouncerOn) {
                this.mDataCollector.onTouchEvent(motionEvent, i, i2);
            }
            if (this.mBouncerOffOnDown) {
                this.mHumanInteractionClassifier.onTouchEvent(motionEvent);
            }
        }
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void dump(PrintWriter printWriter) {
        printWriter.println("FALSING MANAGER");
        printWriter.print("classifierEnabled=");
        printWriter.println(isClassifierEnabled() ? 1 : 0);
        printWriter.print("mSessionActive=");
        printWriter.println(this.mSessionActive ? 1 : 0);
        printWriter.print("mBouncerOn=");
        printWriter.println(this.mSessionActive ? 1 : 0);
        printWriter.print("mState=");
        printWriter.println(StatusBarState.toShortString(this.mState));
        printWriter.print("mScreenOn=");
        printWriter.println(this.mScreenOn ? 1 : 0);
        printWriter.println();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void cleanup() {
        this.mSensorManager.unregisterListener(this.mSensorEventListener);
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).removeCallback(this.mStatusBarStateListener);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mKeyguardUpdateCallback);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public Uri reportRejectedTouch() {
        if (this.mDataCollector.isEnabled()) {
            return this.mDataCollector.reportRejectedTouch();
        }
        return null;
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isReportingEnabled() {
        return this.mDataCollector.isReportingEnabled();
    }
}
