package com.android.systemui.doze;

import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Build;
import android.os.Trace;
import android.util.Log;
import com.android.internal.util.Preconditions;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.dock.DockManager;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.util.Assert;
import com.android.systemui.util.wakelock.WakeLock;
import com.oneplus.plugin.OpLsState;
import java.io.PrintWriter;
import java.util.ArrayList;
public class DozeMachine {
    static final boolean DEBUG = DozeService.DEBUG;
    private static final boolean ONEPLUS_DEBUG = Build.DEBUG_ONEPLUS;
    private final BatteryController mBatteryController;
    private DockManager mDockManager;
    private final DozeHost mDozeHost;
    private final DozeLog mDozeLog;
    private final Service mDozeService;
    private Part[] mParts;
    private int mPulseReason;
    private final ArrayList<State> mQueuedRequests = new ArrayList<>();
    private State mState = State.UNINITIALIZED;
    private final WakeLock mWakeLock;
    private boolean mWakeLockHeldForCurrentState = false;
    private final WakefulnessLifecycle mWakefulnessLifecycle;

    public interface Part {
        default void destroy() {
        }

        default void dump(PrintWriter printWriter) {
        }

        void transitionTo(State state, State state2);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.doze.DozeMachine$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$doze$DozeMachine$State;

        static {
            int[] iArr = new int[State.values().length];
            $SwitchMap$com$android$systemui$doze$DozeMachine$State = iArr;
            try {
                iArr[State.DOZE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[State.DOZE_AOD.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[State.DOZE_AOD_PAUSED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[State.DOZE_AOD_PAUSING.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[State.DOZE_AOD_DOCKED.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[State.DOZE_REQUEST_PULSE.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[State.DOZE_PULSING.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[State.DOZE_PULSING_BRIGHT.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[State.UNINITIALIZED.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[State.INITIALIZED.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[State.FINISH.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[State.DOZE_PULSE_DONE.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
        }
    }

    public enum State {
        UNINITIALIZED,
        INITIALIZED,
        DOZE,
        DOZE_AOD,
        DOZE_REQUEST_PULSE,
        DOZE_PULSING,
        DOZE_PULSING_BRIGHT,
        DOZE_PULSE_DONE,
        FINISH,
        DOZE_AOD_PAUSED,
        DOZE_AOD_PAUSING,
        DOZE_AOD_DOCKED;

        /* access modifiers changed from: package-private */
        public boolean canPulse() {
            int i = AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State[ordinal()];
            return i == 1 || i == 2 || i == 3 || i == 4 || i == 5;
        }

        /* access modifiers changed from: package-private */
        public boolean staysAwake() {
            int i = AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State[ordinal()];
            return i == 5 || i == 6 || i == 7 || i == 8;
        }

        /* access modifiers changed from: package-private */
        public boolean isAlwaysOn() {
            return this == DOZE_AOD || this == DOZE_AOD_DOCKED;
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Removed duplicated region for block: B:13:0x002f A[ORIG_RETURN, RETURN, SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:24:? A[RETURN, SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public int screenState(com.android.systemui.statusbar.phone.DozeParameters r4) {
            /*
                r3 = this;
                com.oneplus.plugin.OpLsState r0 = com.oneplus.plugin.OpLsState.getInstance()
                com.android.keyguard.KeyguardUpdateMonitor r0 = r0.getUpdateMonitor()
                int[] r1 = com.android.systemui.doze.DozeMachine.AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State
                int r3 = r3.ordinal()
                r3 = r1[r3]
                r1 = 3
                r2 = 1
                switch(r3) {
                    case 1: goto L_0x0046;
                    case 2: goto L_0x0032;
                    case 3: goto L_0x0046;
                    case 4: goto L_0x003b;
                    case 5: goto L_0x0031;
                    case 6: goto L_0x0020;
                    case 7: goto L_0x0031;
                    case 8: goto L_0x0031;
                    case 9: goto L_0x0017;
                    case 10: goto L_0x0017;
                    default: goto L_0x0015;
                }
            L_0x0015:
                r3 = 0
                return r3
            L_0x0017:
                if (r0 == 0) goto L_0x0020
                boolean r3 = r0.isAlwaysOnEnabled()
                if (r3 == 0) goto L_0x0020
                return r2
            L_0x0020:
                if (r0 == 0) goto L_0x0029
                boolean r3 = r0.isAlwaysOnEnabled()
                if (r3 == 0) goto L_0x0029
                return r1
            L_0x0029:
                boolean r3 = r4.shouldControlScreenOff()
                if (r3 == 0) goto L_0x0030
                r2 = 2
            L_0x0030:
                return r2
            L_0x0031:
                return r1
            L_0x0032:
                if (r0 == 0) goto L_0x003b
                boolean r3 = r0.isAlwaysOnEnabled()
                if (r3 == 0) goto L_0x003b
                return r1
            L_0x003b:
                if (r0 == 0) goto L_0x0044
                boolean r3 = r0.isAlwaysOnEnabled()
                if (r3 == 0) goto L_0x0044
                return r2
            L_0x0044:
                r3 = 4
                return r3
            L_0x0046:
                return r2
                switch-data {1->0x0046, 2->0x0032, 3->0x0046, 4->0x003b, 5->0x0031, 6->0x0020, 7->0x0031, 8->0x0031, 9->0x0017, 10->0x0017, }
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.doze.DozeMachine.State.screenState(com.android.systemui.statusbar.phone.DozeParameters):int");
        }
    }

    public DozeMachine(Service service, AmbientDisplayConfiguration ambientDisplayConfiguration, WakeLock wakeLock, WakefulnessLifecycle wakefulnessLifecycle, BatteryController batteryController, DozeLog dozeLog, DockManager dockManager, DozeHost dozeHost) {
        this.mDozeService = service;
        this.mWakefulnessLifecycle = wakefulnessLifecycle;
        this.mWakeLock = wakeLock;
        this.mBatteryController = batteryController;
        this.mDozeLog = dozeLog;
        this.mDockManager = dockManager;
        this.mDozeHost = dozeHost;
    }

    public void destroy() {
        for (Part part : this.mParts) {
            part.destroy();
        }
    }

    public void setParts(Part[] partArr) {
        Preconditions.checkState(this.mParts == null);
        this.mParts = partArr;
    }

    public void requestState(State state) {
        Preconditions.checkArgument(state != State.DOZE_REQUEST_PULSE);
        requestState(state, -1);
    }

    public void requestPulse(int i) {
        Preconditions.checkState(!isExecutingTransition());
        requestState(State.DOZE_REQUEST_PULSE, i);
    }

    private void requestState(State state, int i) {
        Assert.isMainThread();
        KeyguardUpdateMonitor updateMonitor = OpLsState.getInstance().getUpdateMonitor();
        if (updateMonitor != null && updateMonitor.isAlwaysOnEnabled() && ((i == -1 || i == 11) && state == State.DOZE_REQUEST_PULSE)) {
            state = State.DOZE_AOD;
        }
        if (DEBUG) {
            Log.i("DozeMachine", "request: current=" + this.mState + " req=" + state, new Throwable("here"));
        }
        boolean z = !isExecutingTransition();
        this.mQueuedRequests.add(state);
        if (z) {
            this.mWakeLock.acquire("DozeMachine#requestState");
            for (int i2 = 0; i2 < this.mQueuedRequests.size(); i2++) {
                transitionTo(this.mQueuedRequests.get(i2), i);
                Log.d("DozeMachine", "transitionTo: finish= " + i2 + ", size= " + this.mQueuedRequests.size());
            }
            this.mQueuedRequests.clear();
            this.mWakeLock.release("DozeMachine#requestState");
            return;
        }
        Log.d("DozeMachine", "queue event: " + state + ", inside queue: " + this.mQueuedRequests);
    }

    public State getState() {
        Assert.isMainThread();
        if (!isExecutingTransition()) {
            return this.mState;
        }
        throw new IllegalStateException("Cannot get state because there were pending transitions: " + this.mQueuedRequests.toString());
    }

    public int getPulseReason() {
        Assert.isMainThread();
        State state = this.mState;
        boolean z = state == State.DOZE_REQUEST_PULSE || state == State.DOZE_PULSING || state == State.DOZE_PULSING_BRIGHT || state == State.DOZE_PULSE_DONE;
        Preconditions.checkState(z, "must be in pulsing state, but is " + this.mState);
        return this.mPulseReason;
    }

    public void wakeUp() {
        this.mDozeService.requestWakeUp();
    }

    public boolean isExecutingTransition() {
        return !this.mQueuedRequests.isEmpty();
    }

    private void transitionTo(State state, int i) {
        State transitionPolicy = transitionPolicy(state);
        if (DEBUG) {
            Log.i("DozeMachine", "transition: old=" + this.mState + " req=" + state + " new=" + transitionPolicy);
        }
        if (transitionPolicy != this.mState) {
            validateTransition(transitionPolicy);
            State state2 = this.mState;
            this.mState = transitionPolicy;
            KeyguardUpdateMonitor updateMonitor = OpLsState.getInstance().getUpdateMonitor();
            if (updateMonitor != null && updateMonitor.isAlwaysOnEnabled() && this.mState == State.DOZE_AOD && i == -1) {
                OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager().resetStatus();
            }
            this.mDozeLog.traceState(transitionPolicy);
            Trace.traceCounter(4096, "doze_machine_state", transitionPolicy.ordinal());
            updatePulseReason(transitionPolicy, state2, i);
            performTransitionOnComponents(state2, transitionPolicy);
            updateWakeLockState(transitionPolicy);
            resolveIntermediateState(transitionPolicy);
        }
    }

    private void updatePulseReason(State state, State state2, int i) {
        if (state == State.DOZE_REQUEST_PULSE) {
            this.mPulseReason = i;
        } else if (state2 == State.DOZE_PULSE_DONE) {
            this.mPulseReason = -1;
        }
    }

    private void performTransitionOnComponents(State state, State state2) {
        Part[] partArr = this.mParts;
        for (Part part : partArr) {
            if (ONEPLUS_DEBUG) {
                Log.d("DozeMachine", "performTransitionOnComponents start. Part:" + part);
            }
            part.transitionTo(state, state2);
            if (ONEPLUS_DEBUG) {
                Log.d("DozeMachine", "performTransitionOnComponents end. Part:" + part);
            }
        }
        if (AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State[state2.ordinal()] == 11) {
            this.mDozeService.finish();
        }
    }

    private void validateTransition(State state) {
        try {
            int i = AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State[this.mState.ordinal()];
            boolean z = true;
            if (i == 9) {
                Preconditions.checkState(state == State.INITIALIZED);
            } else if (i == 11) {
                Preconditions.checkState(state == State.FINISH);
            }
            int i2 = AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State[state.ordinal()];
            if (i2 == 7) {
                if (this.mState != State.DOZE_REQUEST_PULSE) {
                    z = false;
                }
                Preconditions.checkState(z);
            } else if (i2 == 12) {
                if (!(this.mState == State.DOZE_REQUEST_PULSE || this.mState == State.DOZE_PULSING)) {
                    if (this.mState != State.DOZE_PULSING_BRIGHT) {
                        z = false;
                    }
                }
                Preconditions.checkState(z);
            } else if (i2 == 9) {
                throw new IllegalArgumentException("can't transition to UNINITIALIZED");
            } else if (i2 == 10) {
                if (this.mState != State.UNINITIALIZED) {
                    z = false;
                }
                Preconditions.checkState(z);
            }
        } catch (RuntimeException e) {
            throw new IllegalStateException("Illegal Transition: " + this.mState + " -> " + state, e);
        }
    }

    private State transitionPolicy(State state) {
        State state2 = this.mState;
        State state3 = State.FINISH;
        if (state2 == state3) {
            return state3;
        }
        if (!this.mDozeHost.isDozeSuppressed() || !state.isAlwaysOn()) {
            State state4 = this.mState;
            if ((state4 == State.DOZE_AOD_PAUSED || state4 == State.DOZE_AOD_PAUSING || state4 == State.DOZE_AOD || state4 == State.DOZE || state4 == State.DOZE_AOD_DOCKED) && state == State.DOZE_PULSE_DONE) {
                Log.i("DozeMachine", "Dropping pulse done because current state is already done: " + this.mState);
                return this.mState;
            } else if (state == State.DOZE_AOD && this.mBatteryController.isAodPowerSave()) {
                return State.DOZE;
            } else {
                if (state != State.DOZE_REQUEST_PULSE || this.mState.canPulse()) {
                    return state;
                }
                Log.i("DozeMachine", "Dropping pulse request because current state can't pulse: " + this.mState);
                return this.mState;
            }
        } else {
            Log.i("DozeMachine", "Doze is suppressed. Suppressing state: " + state);
            this.mDozeLog.traceDozeSuppressed(state);
            return State.DOZE;
        }
    }

    private void updateWakeLockState(State state) {
        boolean staysAwake = state.staysAwake();
        if (this.mWakeLockHeldForCurrentState && !staysAwake) {
            this.mWakeLock.release("DozeMachine#heldForState");
            this.mWakeLockHeldForCurrentState = false;
        } else if (!this.mWakeLockHeldForCurrentState && staysAwake) {
            this.mWakeLock.acquire("DozeMachine#heldForState");
            this.mWakeLockHeldForCurrentState = true;
        }
    }

    private void resolveIntermediateState(State state) {
        State state2;
        int i = AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State[state.ordinal()];
        if (i == 10 || i == 12) {
            int wakefulness = this.mWakefulnessLifecycle.getWakefulness();
            KeyguardUpdateMonitor updateMonitor = OpLsState.getInstance().getUpdateMonitor();
            if (state != State.INITIALIZED && (wakefulness == 2 || wakefulness == 1)) {
                state2 = State.FINISH;
            } else if (updateMonitor != null && updateMonitor.isAlwaysOnEnabled()) {
                state2 = State.DOZE_AOD;
                OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager().updateForPulseReason(-1);
            } else if (this.mDockManager.isDocked()) {
                state2 = this.mDockManager.isHidden() ? State.DOZE : State.DOZE_AOD_DOCKED;
            } else {
                state2 = State.DOZE;
            }
            transitionTo(state2, -1);
        }
    }

    public void dump(PrintWriter printWriter) {
        printWriter.print(" state=");
        printWriter.println(this.mState);
        printWriter.print(" wakeLockHeldForCurrentState=");
        printWriter.println(this.mWakeLockHeldForCurrentState);
        printWriter.print(" wakeLock=");
        printWriter.println(this.mWakeLock);
        printWriter.println("Parts:");
        for (Part part : this.mParts) {
            part.dump(printWriter);
        }
    }

    public interface Service {
        void finish();

        void requestWakeUp();

        void setDozeScreenBrightness(int i);

        void setDozeScreenState(int i);

        public static class Delegate implements Service {
            private final Service mDelegate;

            public Delegate(Service service) {
                this.mDelegate = service;
            }

            @Override // com.android.systemui.doze.DozeMachine.Service
            public void finish() {
                this.mDelegate.finish();
            }

            @Override // com.android.systemui.doze.DozeMachine.Service
            public void setDozeScreenState(int i) {
                this.mDelegate.setDozeScreenState(i);
            }

            @Override // com.android.systemui.doze.DozeMachine.Service
            public void requestWakeUp() {
                this.mDelegate.requestWakeUp();
            }

            @Override // com.android.systemui.doze.DozeMachine.Service
            public void setDozeScreenBrightness(int i) {
                this.mDelegate.setDozeScreenBrightness(i);
            }
        }
    }
}
