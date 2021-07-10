package com.android.systemui.doze;

import android.app.AlarmManager;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.AmbientDisplayConfiguration;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.format.Formatter;
import android.util.Log;
import android.util.OpFeatures;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dock.DockManager;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeMachine;
import com.android.systemui.doze.DozeSensors;
import com.android.systemui.doze.DozeTriggers;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.util.Assert;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.android.systemui.util.sensors.ProximitySensor;
import com.android.systemui.util.wakelock.WakeLock;
import com.oneplus.aod.OpAodDisplayViewManager;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpUtils;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import vendor.oneplus.hardware.display.V1_0.IOneplusDisplay;
public class DozeTriggers implements DozeMachine.Part {
    private static final boolean DEBUG = DozeService.DEBUG;
    private static final UiEventLogger UI_EVENT_LOGGER = new UiEventLoggerImpl();
    private static boolean sWakeDisplaySensorState = true;
    private final boolean mAllowPulseTriggers;
    private OpAodDisplayViewManager mAodDisplayViewManager;
    private Runnable mAodPausingRunnable = new Runnable() { // from class: com.android.systemui.doze.DozeTriggers.1
        @Override // java.lang.Runnable
        public void run() {
            if (DozeTriggers.DEBUG) {
                Log.i("DozeTriggers", "Prox NEAR, really pausing AOD.");
            }
            DozeTriggers.this.mMachine.requestState(DozeMachine.State.DOZE_AOD_PAUSING);
            if (DozeTriggers.this.mAodPausingWakeLock != null && DozeTriggers.this.mAodPausingWakeLock.isHeld()) {
                DozeTriggers.this.mAodPausingWakeLock.release();
                DozeTriggers.this.mAodPausingWakeLock = null;
            }
        }
    };
    private PowerManager.WakeLock mAodPausingWakeLock;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final TriggerReceiver mBroadcastReceiver = new TriggerReceiver();
    private final AmbientDisplayConfiguration mConfig;
    private final Context mContext;
    private final DockEventListener mDockEventListener = new DockEventListener();
    private final DockManager mDockManager;
    private final DozeHost mDozeHost;
    private final DozeLog mDozeLog;
    private final DozeParameters mDozeParameters;
    private final DozeSensors mDozeSensors;
    private Handler mHandler;
    private DozeHost.Callback mHostCallback = new DozeHost.Callback() { // from class: com.android.systemui.doze.DozeTriggers.2
        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onNotificationAlerted(Runnable runnable) {
            DozeTriggers.this.onNotification(runnable);
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onPowerSaveChanged(boolean z) {
            if (DozeTriggers.this.mDozeHost.isPowerSaveActive() && !OpUtils.isCustomFingerprint()) {
                DozeTriggers.this.mMachine.requestState(DozeMachine.State.DOZE);
            } else if (DozeTriggers.this.mMachine.getState() == DozeMachine.State.DOZE && DozeTriggers.this.mConfig.alwaysOnEnabled(-2)) {
                DozeTriggers.this.mMachine.requestState(DozeMachine.State.DOZE_AOD);
            }
            KeyguardUpdateMonitor updateMonitor = OpLsState.getInstance().getUpdateMonitor();
            if (updateMonitor != null && updateMonitor.isAlwaysOnEnabled() && z) {
                DozeTriggers.this.mMachine.requestState(DozeMachine.State.DOZE);
            }
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onDozeSuppressedChanged(boolean z) {
            DozeMachine.State state;
            if (!DozeTriggers.this.mConfig.alwaysOnEnabled(-2) || z) {
                state = DozeMachine.State.DOZE;
            } else {
                state = DozeMachine.State.DOZE_AOD;
            }
            DozeTriggers.this.mMachine.requestState(state);
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onThreeKeyChanged(int i) {
            if (DozeMachine.DEBUG) {
                Log.d("DozeTriggers", "requestThreeKeyStatusPulse");
            }
            DozeTriggers.this.requestPulse(10, false, null);
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onSingleTap() {
            if (DozeMachine.DEBUG) {
                Log.d("DozeTriggers", "requestSingleTapPulse");
            }
            DozeTriggers.this.requestPulse(12, false, null);
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onFingerprintPoke() {
            if (DozeMachine.DEBUG) {
                Log.d("DozeTriggers", "requestFingerprintPoke");
            }
            DozeTriggers.this.requestPulse(13, false, null);
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onAlwaysOnEnableChanged(boolean z) {
            if (DozeMachine.DEBUG) {
                Log.d("DozeTriggers", "onAlwaysOnEnableChanged, active:" + z);
            }
            DozeTriggers.this.mDozeSensors.setLightSensorListening(z);
            if (z) {
                DozeTriggers.this.mMachine.requestPulse(11);
                if (DozeTriggers.this.getAodDisplayViewManager() != null) {
                    DozeTriggers.this.mAodDisplayViewManager.updateForPulseReason(11);
                } else {
                    Log.e("DozeTriggers", "onAlwaysOnEnableChanged: mAodDisplayViewManager is null!!!");
                }
            } else {
                DozeTriggers.this.mMachine.requestState(DozeMachine.State.DOZE);
            }
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onDozeServiceTimeChanged() {
            DozeTriggers.this.mDozeSensors.onDozeTimeChanged();
        }
    };
    private final DozeMachine mMachine;
    private final MetricsLogger mMetricsLogger = ((MetricsLogger) Dependency.get(MetricsLogger.class));
    private long mNotificationPulseTime;
    private PowerManager mPM;
    private final ProximitySensor.ProximityCheck mProxCheck;
    private boolean mPulsePending;
    private final AsyncSensorManager mSensorManager;
    private final UiModeManager mUiModeManager;
    private final WakeLock mWakeLock;

    private boolean canPulse() {
        return true;
    }

    @VisibleForTesting
    public enum DozingUpdateUiEvent implements UiEventLogger.UiEventEnum {
        DOZING_UPDATE_NOTIFICATION(433),
        DOZING_UPDATE_SIGMOTION(434),
        DOZING_UPDATE_SENSOR_PICKUP(435),
        DOZING_UPDATE_SENSOR_DOUBLE_TAP(436),
        DOZING_UPDATE_SENSOR_LONG_SQUEEZE(437),
        DOZING_UPDATE_DOCKING(438),
        DOZING_UPDATE_SENSOR_WAKEUP(439),
        DOZING_UPDATE_SENSOR_WAKE_LOCKSCREEN(440),
        DOZING_UPDATE_SENSOR_TAP(441);
        
        private final int mId;

        private DozingUpdateUiEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }

        static DozingUpdateUiEvent fromReason(int i) {
            switch (i) {
                case 1:
                    return DOZING_UPDATE_NOTIFICATION;
                case 2:
                    return DOZING_UPDATE_SIGMOTION;
                case 3:
                    return DOZING_UPDATE_SENSOR_PICKUP;
                case 4:
                    return DOZING_UPDATE_SENSOR_DOUBLE_TAP;
                case 5:
                    return DOZING_UPDATE_SENSOR_LONG_SQUEEZE;
                case 6:
                    return DOZING_UPDATE_DOCKING;
                case 7:
                    return DOZING_UPDATE_SENSOR_WAKEUP;
                case 8:
                    return DOZING_UPDATE_SENSOR_WAKE_LOCKSCREEN;
                case 9:
                    return DOZING_UPDATE_SENSOR_TAP;
                default:
                    return null;
            }
        }
    }

    public DozeTriggers(Context context, DozeMachine dozeMachine, DozeHost dozeHost, AlarmManager alarmManager, AmbientDisplayConfiguration ambientDisplayConfiguration, DozeParameters dozeParameters, AsyncSensorManager asyncSensorManager, WakeLock wakeLock, boolean z, DockManager dockManager, ProximitySensor proximitySensor, ProximitySensor.ProximityCheck proximityCheck, DozeLog dozeLog, BroadcastDispatcher broadcastDispatcher) {
        this.mContext = context;
        this.mMachine = dozeMachine;
        this.mDozeHost = dozeHost;
        this.mConfig = ambientDisplayConfiguration;
        this.mDozeParameters = dozeParameters;
        this.mSensorManager = asyncSensorManager;
        this.mWakeLock = wakeLock;
        this.mAllowPulseTriggers = z;
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mDozeSensors = new DozeSensors(context, alarmManager, this.mSensorManager, dozeParameters, ambientDisplayConfiguration, wakeLock, new DozeSensors.Callback() { // from class: com.android.systemui.doze.-$$Lambda$XuSeOmLZ56lHJGoIP26_sIwbcBM
            @Override // com.android.systemui.doze.DozeSensors.Callback
            public final void onSensorPulse(int i, float f, float f2, float[] fArr) {
                DozeTriggers.this.onSensor(i, f, f2, fArr);
            }
        }, new Consumer() { // from class: com.android.systemui.doze.-$$Lambda$DozeTriggers$ulqUMEXi8OgK7771oZ9BOr21BBk
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DozeTriggers.lambda$ulqUMEXi8OgK7771oZ9BOr21BBk(DozeTriggers.this, ((Boolean) obj).booleanValue());
            }
        }, dozeLog, proximitySensor, dozeHost);
        this.mUiModeManager = (UiModeManager) this.mContext.getSystemService(UiModeManager.class);
        this.mDockManager = dockManager;
        this.mProxCheck = proximityCheck;
        this.mDozeLog = dozeLog;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mPM = (PowerManager) this.mContext.getSystemService("power");
        try {
            IOneplusDisplay.getService();
        } catch (Exception e) {
            Log.d("DozeTriggers", "IOneplusDisplay getService Exception e = " + e.toString());
        }
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void destroy() {
        this.mDozeSensors.destroy();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onNotification(Runnable runnable) {
        if (DozeMachine.DEBUG) {
            Log.d("DozeTriggers", "requestNotificationPulse");
        }
        if (!sWakeDisplaySensorState) {
            Log.d("DozeTriggers", "Wake display false. Pulse denied.");
            runIfNotNull(runnable);
            this.mDozeLog.tracePulseDropped("wakeDisplaySensor");
            return;
        }
        this.mNotificationPulseTime = SystemClock.elapsedRealtime();
        if (!OpAodUtils.isNotificationWakeUpEnabled()) {
            runIfNotNull(runnable);
            this.mDozeLog.tracePulseDropped("pulseOnNotificationsDisabled");
        } else if (this.mDozeHost.isDozeSuppressed()) {
            runIfNotNull(runnable);
            this.mDozeLog.tracePulseDropped("dozeSuppressed");
        } else {
            requestPulse(1, false, runnable);
            this.mDozeLog.traceNotificationPulse();
        }
    }

    private static void runIfNotNull(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    private void proximityCheckThenCall(Consumer<Boolean> consumer, boolean z, int i) {
        Boolean isProximityCurrentlyNear = this.mDozeSensors.isProximityCurrentlyNear();
        KeyguardUpdateMonitor updateMonitor = OpLsState.getInstance().getUpdateMonitor();
        boolean z2 = false;
        boolean isKeyguardDone = updateMonitor != null ? updateMonitor.isKeyguardDone() : false;
        int customProximityResult = this.mDozeSensors.getCustomProximityResult();
        StringBuilder sb = new StringBuilder();
        sb.append("proximityCheckThenCall: alreadyPerformedProxCheck=");
        sb.append(z);
        sb.append(", cachedProxNear=");
        sb.append(isProximityCurrentlyNear);
        sb.append(", customProximity=");
        sb.append(customProximityResult == 1 ? "near" : "far");
        sb.append(", isKeyguardDone=");
        sb.append(isKeyguardDone);
        sb.append(", reason=");
        sb.append(i);
        Log.d("DozeTriggers", sb.toString());
        if (OpFeatures.isSupport(new int[]{60})) {
            boolean z3 = customProximityResult == 1;
            this.mDozeLog.traceProximityResult(z3, 0, i);
            if (Boolean.valueOf(z3).booleanValue() && !isKeyguardDone) {
                z2 = true;
            }
            consumer.accept(Boolean.valueOf(z2));
        } else if (z) {
            consumer.accept(null);
        } else if (isProximityCurrentlyNear != null) {
            consumer.accept(isProximityCurrentlyNear);
        } else {
            this.mProxCheck.check(500, new Consumer(SystemClock.uptimeMillis(), i, consumer) { // from class: com.android.systemui.doze.-$$Lambda$DozeTriggers$7dHaL16-QO2EYQ_3R1TKZzEi3lA
                public final /* synthetic */ long f$1;
                public final /* synthetic */ int f$2;
                public final /* synthetic */ Consumer f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r4;
                    this.f$3 = r5;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DozeTriggers.this.lambda$proximityCheckThenCall$0$DozeTriggers(this.f$1, this.f$2, this.f$3, (Boolean) obj);
                }
            });
            this.mWakeLock.acquire("DozeTriggers");
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$proximityCheckThenCall$0 */
    public /* synthetic */ void lambda$proximityCheckThenCall$0$DozeTriggers(long j, int i, Consumer consumer, Boolean bool) {
        boolean z;
        long uptimeMillis = SystemClock.uptimeMillis();
        DozeLog dozeLog = this.mDozeLog;
        if (bool == null) {
            z = false;
        } else {
            z = bool.booleanValue();
        }
        dozeLog.traceProximityResult(z, uptimeMillis - j, i);
        consumer.accept(bool);
        this.mWakeLock.release("DozeTriggers");
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void onSensor(int i, float f, float f2, float[] fArr) {
        boolean z = false;
        boolean z2 = i == 4;
        boolean z3 = i == 9;
        boolean z4 = i == 3;
        boolean z5 = i == 5;
        boolean z6 = i == 7;
        boolean z7 = i == 8;
        boolean z8 = (fArr == null || fArr.length <= 0 || fArr[0] == 0.0f) ? false : true;
        if (i == -1) {
            this.mDozeHost.stopPulsing();
            return;
        }
        DozeMachine.State state = null;
        if (z6) {
            if (!this.mMachine.isExecutingTransition()) {
                state = this.mMachine.getState();
            }
            onWakeScreen(z8, state);
        } else if (z5) {
            requestPulse(i, true, null);
        } else if (!z7) {
            proximityCheckThenCall(new Consumer(z2, z3, f, f2, i, z4) { // from class: com.android.systemui.doze.-$$Lambda$DozeTriggers$_9uGVeOllRSk5IFkZMhDAbIz6Gw
                public final /* synthetic */ boolean f$1;
                public final /* synthetic */ boolean f$2;
                public final /* synthetic */ float f$3;
                public final /* synthetic */ float f$4;
                public final /* synthetic */ int f$5;
                public final /* synthetic */ boolean f$6;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                    this.f$5 = r6;
                    this.f$6 = r7;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DozeTriggers.this.lambda$onSensor$1$DozeTriggers(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, (Boolean) obj);
                }
            }, true, i);
        } else if (z8) {
            requestPulse(i, true, null);
        }
        if (z4) {
            if (SystemClock.elapsedRealtime() - this.mNotificationPulseTime < ((long) this.mDozeParameters.getPickupVibrationThreshold())) {
                z = true;
            }
            this.mDozeLog.tracePickupWakeUp(z);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onSensor$1 */
    public /* synthetic */ void lambda$onSensor$1$DozeTriggers(boolean z, boolean z2, float f, float f2, int i, boolean z3, Boolean bool) {
        if (bool != null && bool.booleanValue()) {
            Log.d("DozeTriggers", "In pocket, drop event");
        } else if (z || z2) {
            if (!(f == -1.0f || f2 == -1.0f)) {
                this.mDozeHost.onSlpiTap(f, f2);
            }
            gentleWakeUp(i);
        } else if (z3) {
            requestPulse(i, true, null);
        } else {
            this.mDozeHost.extendPulse(i);
        }
    }

    private void gentleWakeUp(int i) {
        this.mMetricsLogger.write(new LogMaker(223).setType(6).setSubtype(i));
        Optional ofNullable = Optional.ofNullable(DozingUpdateUiEvent.fromReason(i));
        UiEventLogger uiEventLogger = UI_EVENT_LOGGER;
        Objects.requireNonNull(uiEventLogger);
        ofNullable.ifPresent(new Consumer(uiEventLogger) { // from class: com.android.systemui.doze.-$$Lambda$vBVHjIDgps_phZpQ4QNJ6P1upak
            public final /* synthetic */ UiEventLogger f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.log((DozeTriggers.DozingUpdateUiEvent) obj);
            }
        });
        if (OpLsState.getInstance().getUpdateMonitor().isAlwaysOnEnabled() || this.mDozeParameters.getDisplayNeedsBlanking()) {
            this.mDozeHost.setAodDimmingScrim(255.0f);
        }
        this.mMachine.wakeUp();
    }

    /* access modifiers changed from: private */
    public void onProximityFar(boolean z) {
        if (this.mMachine.isExecutingTransition()) {
            Log.w("DozeTriggers", "onProximityFar called during transition. Ignoring sensor response.");
            return;
        }
        boolean z2 = !z;
        DozeMachine.State state = this.mMachine.getState();
        boolean z3 = false;
        boolean z4 = state == DozeMachine.State.DOZE_AOD_PAUSED;
        boolean z5 = state == DozeMachine.State.DOZE_AOD_PAUSING;
        if (state == DozeMachine.State.DOZE_AOD) {
            z3 = true;
        }
        if (state == DozeMachine.State.DOZE_PULSING || state == DozeMachine.State.DOZE_PULSING_BRIGHT) {
            if (DEBUG) {
                Log.i("DozeTriggers", "Prox changed, ignore touch = " + z2);
            }
            this.mDozeHost.onIgnoreTouchWhilePulsing(z2);
        }
        if (z) {
            removeAodPausingRunable();
            if (z4 || z5) {
                if (DEBUG) {
                    Log.i("DozeTriggers", "Prox FAR, unpausing AOD");
                }
                KeyguardUpdateMonitor updateMonitor = OpLsState.getInstance().getUpdateMonitor();
                if (updateMonitor == null || !updateMonitor.isAlwaysOnEnabled()) {
                    this.mMachine.requestState(DozeMachine.State.DOZE_AOD);
                } else {
                    continuePulseRequest(11);
                }
            }
        } else if (z2 && z3) {
            if (DEBUG) {
                Log.i("DozeTriggers", "Prox NEAR");
            }
            if (!this.mHandler.hasCallbacks(this.mAodPausingRunnable)) {
                if (DEBUG) {
                    Log.i("DozeTriggers", "Prox NEAR, pausing AOD");
                }
                PowerManager.WakeLock newWakeLock = this.mPM.newWakeLock(1, "aod_pausing_delay_wakelock");
                this.mAodPausingWakeLock = newWakeLock;
                newWakeLock.acquire();
                this.mHandler.postDelayed(this.mAodPausingRunnable, 3000);
            } else if (DEBUG) {
                Log.i("DozeTriggers", "Prox NEAR, Wait");
            }
        }
    }

    private void removeAodPausingRunable() {
        this.mHandler.removeCallbacks(this.mAodPausingRunnable);
        PowerManager.WakeLock wakeLock = this.mAodPausingWakeLock;
        if (wakeLock != null && wakeLock.isHeld()) {
            this.mAodPausingWakeLock.release();
            this.mAodPausingWakeLock = null;
        }
    }

    private void onWakeScreen(boolean z, DozeMachine.State state) {
        this.mDozeLog.traceWakeDisplay(z);
        sWakeDisplaySensorState = z;
        boolean z2 = true;
        if (z) {
            proximityCheckThenCall(new Consumer(state) { // from class: com.android.systemui.doze.-$$Lambda$DozeTriggers$HZx5UzHarvs5L6-DXQmh-vvZFRQ
                public final /* synthetic */ DozeMachine.State f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DozeTriggers.this.lambda$onWakeScreen$2$DozeTriggers(this.f$1, (Boolean) obj);
                }
            }, true, 7);
            return;
        }
        boolean z3 = state == DozeMachine.State.DOZE_AOD_PAUSED;
        if (state != DozeMachine.State.DOZE_AOD_PAUSING) {
            z2 = false;
        }
        if (!z2 && !z3) {
            this.mMachine.requestState(DozeMachine.State.DOZE);
            this.mMetricsLogger.write(new LogMaker(223).setType(2).setSubtype(7));
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onWakeScreen$2 */
    public /* synthetic */ void lambda$onWakeScreen$2$DozeTriggers(DozeMachine.State state, Boolean bool) {
        if ((bool == null || !bool.booleanValue()) && state == DozeMachine.State.DOZE) {
            this.mMachine.requestState(DozeMachine.State.DOZE_AOD);
            this.mMetricsLogger.write(new LogMaker(223).setType(1).setSubtype(7));
        }
    }

    /* renamed from: com.android.systemui.doze.DozeTriggers$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$doze$DozeMachine$State;

        static {
            int[] iArr = new int[DozeMachine.State.values().length];
            $SwitchMap$com$android$systemui$doze$DozeMachine$State = iArr;
            try {
                iArr[DozeMachine.State.INITIALIZED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_AOD.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_AOD_PAUSED.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_AOD_PAUSING.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_PULSING.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_PULSING_BRIGHT.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_AOD_DOCKED.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_PULSE_DONE.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.FINISH.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
        }
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void transitionTo(DozeMachine.State state, DozeMachine.State state2) {
        switch (AnonymousClass3.$SwitchMap$com$android$systemui$doze$DozeMachine$State[state2.ordinal()]) {
            case 1:
                this.mBroadcastReceiver.register(this.mBroadcastDispatcher);
                this.mDozeHost.addCallback(this.mHostCallback);
                this.mDockManager.addListener(this.mDockEventListener);
                this.mDozeSensors.requestTemporaryDisable();
                checkTriggersAtInit();
                return;
            case 2:
            case 3:
                this.mDozeSensors.setCustomProxListening(true);
                this.mDozeSensors.resetMotionValue();
                this.mDozeSensors.setProxListening(state2 != DozeMachine.State.DOZE);
                this.mDozeSensors.setListening(true);
                this.mDozeSensors.setPaused(false);
                if (state2 == DozeMachine.State.DOZE_AOD && !sWakeDisplaySensorState) {
                    onWakeScreen(false, state2);
                    return;
                }
                return;
            case 4:
            case 5:
                this.mDozeSensors.setProxListening(true);
                this.mDozeSensors.setPaused(true);
                return;
            case 6:
            case 7:
            case 8:
                removeAodPausingRunable();
                this.mDozeSensors.setCustomProxListening(false);
                this.mDozeSensors.setTouchscreenSensorsListening(false);
                return;
            case 9:
                this.mDozeSensors.requestTemporaryDisable();
                this.mDozeSensors.updateListening();
                return;
            case 10:
                removeAodPausingRunable();
                this.mBroadcastReceiver.unregister(this.mBroadcastDispatcher);
                this.mDozeHost.removeCallback(this.mHostCallback);
                this.mDozeSensors.setCustomProxListening(false);
                this.mDozeSensors.setLightSensorListening(false);
                this.mDockManager.removeListener(this.mDockEventListener);
                this.mDozeSensors.setListening(false);
                this.mDozeSensors.setProxListening(false);
                return;
            default:
                return;
        }
    }

    private void checkTriggersAtInit() {
        if (this.mUiModeManager.getCurrentModeType() == 3 || ((this.mDozeHost.isPowerSaveActive() && !OpUtils.isCustomFingerprint()) || this.mDozeHost.isBlockingDoze() || !this.mDozeHost.isProvisioned())) {
            this.mMachine.requestState(DozeMachine.State.FINISH);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestPulse(int i, boolean z, Runnable runnable) {
        Assert.isMainThread();
        if (this.mMachine.isExecutingTransition()) {
            Log.w("DozeTriggers", "requestPulse called during transition. ignore pulse");
            return;
        }
        this.mDozeHost.extendPulse(i);
        if (this.mMachine.getState() == DozeMachine.State.DOZE_PULSING && i == 8) {
            this.mMachine.requestState(DozeMachine.State.DOZE_PULSING_BRIGHT);
        } else if (this.mPulsePending || !this.mAllowPulseTriggers || !canPulse()) {
            if (this.mAllowPulseTriggers) {
                this.mDozeLog.tracePulseDropped(this.mPulsePending, this.mMachine.getState(), this.mDozeHost.isPulsingBlocked());
            }
            runIfNotNull(runnable);
        } else {
            boolean z2 = true;
            this.mPulsePending = true;
            $$Lambda$DozeTriggers$7efrn9gYOB_Pbk9skV2oR0AOE r1 = new Consumer(runnable, i) { // from class: com.android.systemui.doze.-$$Lambda$DozeTriggers$7efrn9gY-OB_Pbk9skV2oR0-AOE
                public final /* synthetic */ Runnable f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DozeTriggers.this.lambda$requestPulse$3$DozeTriggers(this.f$1, this.f$2, (Boolean) obj);
                }
            };
            if (this.mDozeParameters.getProxCheckBeforePulse() && !z) {
                z2 = false;
            }
            proximityCheckThenCall(r1, z2, i);
            this.mMetricsLogger.write(new LogMaker(223).setType(6).setSubtype(i));
            Optional ofNullable = Optional.ofNullable(DozingUpdateUiEvent.fromReason(i));
            UiEventLogger uiEventLogger = UI_EVENT_LOGGER;
            Objects.requireNonNull(uiEventLogger);
            ofNullable.ifPresent(new Consumer(uiEventLogger) { // from class: com.android.systemui.doze.-$$Lambda$vBVHjIDgps_phZpQ4QNJ6P1upak
                public final /* synthetic */ UiEventLogger f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.log((DozeTriggers.DozingUpdateUiEvent) obj);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$requestPulse$3 */
    public /* synthetic */ void lambda$requestPulse$3$DozeTriggers(Runnable runnable, int i, Boolean bool) {
        if (bool == null || !bool.booleanValue()) {
            continuePulseRequest(i);
            return;
        }
        this.mDozeLog.tracePulseDropped("inPocket");
        this.mPulsePending = false;
        runIfNotNull(runnable);
    }

    private void continuePulseRequest(int i) {
        this.mPulsePending = false;
        Log.d("DozeTriggers", "continuePulseRequest: state = " + this.mMachine.getState() + ", reason = " + i);
        if (i == 3 && SystemProperties.getBoolean("test.aod.liftup", false)) {
            this.mPM.wakeUp(SystemClock.uptimeMillis(), "android.policy:LIFT_UP");
        } else if (this.mMachine.getState() != DozeMachine.State.DOZE_PULSING || i != 3) {
            if (this.mDozeHost.isPulsingBlocked() || !canPulse()) {
                this.mDozeLog.tracePulseDropped(this.mPulsePending, this.mMachine.getState(), this.mDozeHost.isPulsingBlocked());
                return;
            }
            if (getAodDisplayViewManager() != null) {
                this.mAodDisplayViewManager.updateForPulseReason(i);
            } else {
                Log.e("DozeTriggers", "continuePulseRequest: mAodDisplayViewManager is null!!!");
            }
            this.mMachine.requestPulse(i);
        }
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void dump(PrintWriter printWriter) {
        printWriter.print(" notificationPulseTime=");
        printWriter.println(Formatter.formatShortElapsedTime(this.mContext, this.mNotificationPulseTime));
        printWriter.println(" pulsePending=" + this.mPulsePending);
        printWriter.println("DozeSensors:");
        this.mDozeSensors.dump(printWriter);
    }

    private class TriggerReceiver extends BroadcastReceiver {
        private boolean mRegistered;

        private TriggerReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.doze.pulse".equals(intent.getAction())) {
                if (DozeMachine.DEBUG) {
                    Log.d("DozeTriggers", "Received pulse intent");
                }
                DozeTriggers.this.requestPulse(0, false, null);
            }
            if (UiModeManager.ACTION_ENTER_CAR_MODE.equals(intent.getAction())) {
                DozeTriggers.this.mMachine.requestState(DozeMachine.State.FINISH);
            }
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                DozeTriggers.this.mDozeSensors.onUserSwitched();
            }
        }

        public void register(BroadcastDispatcher broadcastDispatcher) {
            if (!this.mRegistered) {
                IntentFilter intentFilter = new IntentFilter("com.android.systemui.doze.pulse");
                intentFilter.addAction(UiModeManager.ACTION_ENTER_CAR_MODE);
                intentFilter.addAction("android.intent.action.USER_SWITCHED");
                broadcastDispatcher.registerReceiver(this, intentFilter);
                this.mRegistered = true;
            }
        }

        public void unregister(BroadcastDispatcher broadcastDispatcher) {
            if (this.mRegistered) {
                broadcastDispatcher.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }
    }

    private class DockEventListener implements DockManager.DockEventListener {
        private DockEventListener(DozeTriggers dozeTriggers) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private OpAodDisplayViewManager getAodDisplayViewManager() {
        if (this.mAodDisplayViewManager == null && OpLsState.getInstance().getPhoneStatusBar() != null) {
            this.mAodDisplayViewManager = OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager();
        }
        return this.mAodDisplayViewManager;
    }
}
