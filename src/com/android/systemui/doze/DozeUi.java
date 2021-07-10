package com.android.systemui.doze;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeMachine;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.util.AlarmTimeout;
import com.android.systemui.util.wakelock.WakeLock;
import java.util.Objects;
public class DozeUi implements DozeMachine.Part {
    private final boolean mCanAnimateTransition;
    private final Context mContext;
    private final DozeLog mDozeLog;
    private final DozeParameters mDozeParameters;
    private final Handler mHandler;
    private final DozeHost mHost;
    private boolean mKeyguardShowing;
    private final KeyguardUpdateMonitorCallback mKeyguardVisibilityCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.systemui.doze.DozeUi.1
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            DozeUi.this.mKeyguardShowing = z;
            DozeUi.this.updateAnimateScreenOff();
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onAlwaysOnEnableChanged(boolean z) {
            if (!z) {
                DozeUi.this.updateAnimateScreenOff(false);
            }
        }
    };
    private long mLastTimeTickElapsed = 0;
    private final DozeMachine mMachine;
    private final AlarmTimeout mTimeTicker;
    private final WakeLock mWakeLock;

    static /* synthetic */ void lambda$onTimeTick$0() {
    }

    private void scheduleTimeTick() {
    }

    public DozeUi(Context context, AlarmManager alarmManager, DozeMachine dozeMachine, WakeLock wakeLock, DozeHost dozeHost, Handler handler, DozeParameters dozeParameters, KeyguardUpdateMonitor keyguardUpdateMonitor, DozeLog dozeLog) {
        this.mContext = context;
        this.mMachine = dozeMachine;
        this.mWakeLock = wakeLock;
        this.mHost = dozeHost;
        this.mHandler = handler;
        this.mCanAnimateTransition = !dozeParameters.getDisplayNeedsBlanking();
        this.mDozeParameters = dozeParameters;
        this.mTimeTicker = new AlarmTimeout(alarmManager, new AlarmManager.OnAlarmListener() { // from class: com.android.systemui.doze.-$$Lambda$DozeUi$FO90hbI6xqXYUh2DtwuwM-uzJzs
            @Override // android.app.AlarmManager.OnAlarmListener
            public final void onAlarm() {
                DozeUi.m14lambda$FO90hbI6xqXYUh2DtwuwMuzJzs(DozeUi.this);
            }
        }, "doze_time_tick", handler);
        keyguardUpdateMonitor.registerCallback(this.mKeyguardVisibilityCallback);
        this.mDozeLog = dozeLog;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAnimateScreenOff() {
        if (this.mCanAnimateTransition) {
            updateAnimateScreenOff(this.mDozeParameters.getAlwaysOn() && this.mKeyguardShowing && !this.mHost.isPowerSaveActive() && ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isAlwaysOnEnabled());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAnimateScreenOff(boolean z) {
        if (this.mCanAnimateTransition) {
            this.mDozeParameters.setControlScreenOffAnimation(z);
            this.mHost.setAnimateScreenOff(z);
        }
    }

    private void pulseWhileDozing(final int i) {
        this.mHost.pulseWhileDozing(new DozeHost.PulseCallback() { // from class: com.android.systemui.doze.DozeUi.2
            @Override // com.android.systemui.doze.DozeHost.PulseCallback
            public void onPulseStarted() {
                DozeMachine.State state;
                try {
                    DozeMachine dozeMachine = DozeUi.this.mMachine;
                    if (i == 8) {
                        state = DozeMachine.State.DOZE_PULSING_BRIGHT;
                    } else {
                        state = DozeMachine.State.DOZE_PULSING;
                    }
                    dozeMachine.requestState(state);
                } catch (IllegalStateException unused) {
                }
            }

            @Override // com.android.systemui.doze.DozeHost.PulseCallback
            public void onPulseFinished() {
                DozeUi.this.mMachine.requestState(DozeMachine.State.DOZE_PULSE_DONE);
            }
        }, i);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.doze.DozeUi$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$doze$DozeMachine$State;

        static {
            int[] iArr = new int[DozeMachine.State.values().length];
            $SwitchMap$com$android$systemui$doze$DozeMachine$State = iArr;
            try {
                iArr[DozeMachine.State.DOZE_AOD.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_AOD_DOCKED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_AOD_PAUSING.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_AOD_PAUSED.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_REQUEST_PULSE.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.INITIALIZED.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.FINISH.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_PULSING.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_PULSING_BRIGHT.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.DOZE_PULSE_DONE.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
        }
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void transitionTo(DozeMachine.State state, DozeMachine.State state2) {
        switch (AnonymousClass3.$SwitchMap$com$android$systemui$doze$DozeMachine$State[state2.ordinal()]) {
            case 1:
            case 2:
                if (state == DozeMachine.State.DOZE_AOD_PAUSED || state == DozeMachine.State.DOZE) {
                    this.mHost.dozeTimeTick();
                    Handler handler = this.mHandler;
                    WakeLock wakeLock = this.mWakeLock;
                    DozeHost dozeHost = this.mHost;
                    Objects.requireNonNull(dozeHost);
                    handler.postDelayed(wakeLock.wrap(new Runnable() { // from class: com.android.systemui.doze.-$$Lambda$TvDuFxrq6WnRSNRP7k8oBY4uOBc
                        @Override // java.lang.Runnable
                        public final void run() {
                            DozeHost.this.dozeTimeTick();
                        }
                    }), 500);
                }
                scheduleTimeTick();
                break;
            case 3:
                scheduleTimeTick();
                break;
            case 4:
            case 5:
                unscheduleTimeTick();
                break;
            case 6:
                scheduleTimeTick();
                pulseWhileDozing(this.mMachine.getPulseReason());
                break;
            case 7:
                this.mHost.startDozing();
                break;
            case 8:
                this.mHost.stopDozing();
                unscheduleTimeTick();
                break;
        }
        updateAnimateWakeup(state2);
    }

    private void updateAnimateWakeup(DozeMachine.State state) {
        boolean z = true;
        switch (AnonymousClass3.$SwitchMap$com$android$systemui$doze$DozeMachine$State[state.ordinal()]) {
            case 6:
            case 9:
            case 10:
            case 11:
                this.mHost.setAnimateWakeup(true);
                return;
            case 7:
            default:
                DozeHost dozeHost = this.mHost;
                if (!this.mCanAnimateTransition || !this.mDozeParameters.getAlwaysOn()) {
                    z = false;
                }
                dozeHost.setAnimateWakeup(z);
                return;
            case 8:
                return;
        }
    }

    private void unscheduleTimeTick() {
        if (this.mTimeTicker.isScheduled()) {
            verifyLastTimeTick();
            this.mTimeTicker.cancel();
        }
    }

    private void verifyLastTimeTick() {
        long elapsedRealtime = SystemClock.elapsedRealtime() - this.mLastTimeTickElapsed;
        if (elapsedRealtime > 90000) {
            String formatShortElapsedTime = Formatter.formatShortElapsedTime(this.mContext, elapsedRealtime);
            this.mDozeLog.traceMissedTick(formatShortElapsedTime);
            Log.e("DozeMachine", "Missed AOD time tick by " + formatShortElapsedTime);
        }
    }

    /* access modifiers changed from: private */
    public void onTimeTick() {
        verifyLastTimeTick();
        this.mHost.dozeTimeTick();
        this.mHandler.post(this.mWakeLock.wrap($$Lambda$DozeUi$lHTcknku1GKi6pFF17CHlz1K3H8.INSTANCE));
        scheduleTimeTick();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public KeyguardUpdateMonitorCallback getKeyguardCallback() {
        return this.mKeyguardVisibilityCallback;
    }
}
