package com.android.systemui.statusbar.phone;

import android.os.Build;
import android.os.Handler;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.phone.ScrimController;
import com.oneplus.aod.OpDozeScrimController;
import com.oneplus.plugin.OpLsState;
public class DozeScrimController extends OpDozeScrimController implements StatusBarStateController.StateListener {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private final DozeLog mDozeLog;
    private final DozeParameters mDozeParameters;
    private boolean mDozing;
    private boolean mFullyPulsing;
    private final Handler mHandler = new Handler();
    private DozeHost.PulseCallback mPulseCallback;
    private final Runnable mPulseOut = new Runnable() { // from class: com.android.systemui.statusbar.phone.DozeScrimController.3
        @Override // java.lang.Runnable
        public void run() {
            DozeScrimController.this.mFullyPulsing = false;
            DozeScrimController.this.mHandler.removeCallbacks(DozeScrimController.this.mPulseOut);
            DozeScrimController.this.mHandler.removeCallbacks(DozeScrimController.this.mPulseOutExtended);
            if (DozeScrimController.DEBUG) {
                Log.d("DozeScrimController", "Pulse out, mDozing=" + DozeScrimController.this.mDozing);
            }
            if (DozeScrimController.this.mDozing) {
                DozeScrimController.this.pulseFinished();
            }
        }
    };
    private final Runnable mPulseOutExtended = new Runnable() { // from class: com.android.systemui.statusbar.phone.DozeScrimController.2
        @Override // java.lang.Runnable
        public void run() {
            DozeScrimController.this.mHandler.removeCallbacks(DozeScrimController.this.mPulseOut);
            DozeScrimController.this.mPulseOut.run();
        }
    };
    private int mPulseReason;
    private boolean mRequestPulsing;
    private final ScrimController.Callback mScrimCallback = new ScrimController.Callback() { // from class: com.android.systemui.statusbar.phone.DozeScrimController.1
        @Override // com.android.systemui.statusbar.phone.ScrimController.Callback
        public void onDisplayBlanked() {
            if (DozeScrimController.DEBUG) {
                Log.d("DozeScrimController", "Pulse in, mDozing=" + DozeScrimController.this.mDozing + " mPulseReason=" + DozeLog.reasonToString(DozeScrimController.this.mPulseReason) + ", mRequestPulsing=" + DozeScrimController.this.mRequestPulsing);
            }
            if (DozeScrimController.this.mDozing && DozeScrimController.this.mRequestPulsing) {
                DozeScrimController.this.pulseStarted();
            }
        }

        @Override // com.android.systemui.statusbar.phone.ScrimController.Callback
        public void onFinished() {
            if (DozeScrimController.DEBUG) {
                Log.d("DozeScrimController", "Pulse in finished, mDozing=" + DozeScrimController.this.mDozing);
            }
            if (DozeScrimController.this.mDozing) {
                DozeScrimController.this.acquireWakeLock();
                if (((OpDozeScrimController) DozeScrimController.this).mKeyguardUpdateMonitor == null || (((OpDozeScrimController) DozeScrimController.this).mKeyguardUpdateMonitor != null && !((OpDozeScrimController) DozeScrimController.this).mKeyguardUpdateMonitor.isAlwaysOnEnabled())) {
                    DozeScrimController.this.mHandler.postDelayed(DozeScrimController.this.mPulseOut, (long) DozeScrimController.this.mDozeParameters.getPulseVisibleDuration(DozeScrimController.this.mPulseReason));
                }
                DozeScrimController.this.mHandler.postDelayed(DozeScrimController.this.mPulseOut, (long) DozeScrimController.this.mDozeParameters.getPulseVisibleDuration(DozeScrimController.this.mPulseReason));
                DozeScrimController.this.mFullyPulsing = true;
            }
        }

        @Override // com.android.systemui.statusbar.phone.ScrimController.Callback
        public void onCancelled() {
            DozeScrimController.this.pulseFinished();
        }
    };

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
    }

    public DozeScrimController(DozeParameters dozeParameters, DozeLog dozeLog) {
        this.mDozeParameters = dozeParameters;
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback(this);
        this.mDozeLog = dozeLog;
    }

    @VisibleForTesting
    public void setDozing(boolean z) {
        if (this.mDozing != z) {
            this.mDozing = z;
            if (!z) {
                cancelPulsing();
            }
        }
    }

    public void pulse(DozeHost.PulseCallback pulseCallback, int i) {
        if (pulseCallback != null) {
            boolean z = true;
            if (!this.mDozing || this.mPulseCallback != null) {
                if (DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Pulse supressed. Dozing: ");
                    sb.append(this.mDozeParameters);
                    sb.append(" had callback? ");
                    if (this.mPulseCallback == null) {
                        z = false;
                    }
                    sb.append(z);
                    Log.d("DozeScrimController", sb.toString());
                }
                pulseCallback.onPulseFinished();
                return;
            }
            this.mRequestPulsing = true;
            this.mPulseCallback = pulseCallback;
            this.mPulseReason = i;
            this.mScrimCallback.onDisplayBlanked();
            return;
        }
        throw new IllegalArgumentException("callback must not be null");
    }

    public void pulseOutNow() {
        if (this.mPulseCallback != null && this.mFullyPulsing) {
            this.mPulseOut.run();
        }
    }

    public boolean isPulsing() {
        return this.mPulseCallback != null;
    }

    public void extendPulse() {
        this.mHandler.removeCallbacks(this.mPulseOut);
    }

    public void extendPulse(int i) {
        if (DEBUG) {
            Log.d("DozeScrimController", "extendPulse: " + i);
        }
        this.mHandler.removeCallbacks(this.mPulseOut);
        this.mPulseReason = i;
        this.mHandler.postDelayed(this.mPulseOut, (long) this.mDozeParameters.getPulseVisibleDuration(i));
    }

    private void cancelPulsing() {
        if (this.mPulseCallback != null) {
            if (DEBUG) {
                Log.d("DozeScrimController", "Cancel pulsing");
            }
            this.mFullyPulsing = false;
            this.mHandler.removeCallbacks(this.mPulseOut);
            this.mHandler.removeCallbacks(this.mPulseOutExtended);
            pulseFinished();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pulseStarted() {
        this.mDozeLog.tracePulseStart(this.mPulseReason);
        DozeHost.PulseCallback pulseCallback = this.mPulseCallback;
        if (pulseCallback != null) {
            pulseCallback.onPulseStarted();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pulseFinished() {
        this.mDozeLog.tracePulseFinish();
        this.mRequestPulsing = false;
        if (this.mPulseCallback != null) {
            if (this.mPulseReason != 1 || this.mKeyguardUpdateMonitor.isAlwaysOnEnabled()) {
                this.mPulseCallback.onPulseFinished();
            } else {
                Log.d("DozeScrimController", "pulseFinished: handle notification on aod first");
                OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager().quickHideNotificationBeforeScreenOff(this.mPulseCallback, this.mHandler);
            }
            this.mPulseCallback = null;
        }
        releaseWakeLock();
    }

    public ScrimController.Callback getScrimCallback() {
        return this.mScrimCallback;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        setDozing(z);
    }
}
