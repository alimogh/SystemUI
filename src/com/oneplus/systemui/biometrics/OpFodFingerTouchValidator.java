package com.oneplus.systemui.biometrics;

import android.content.Context;
import android.os.Debug;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
public class OpFodFingerTouchValidator {
    private boolean mAodMode = false;
    private boolean mFingerOnSensor;
    private boolean mFingerOnView;
    KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.systemui.biometrics.OpFodFingerTouchValidator.1
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedWakingUp() {
            OpFodFingerTouchValidator.this.mAodMode = false;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOff() {
            OpFodFingerTouchValidator.this.mAodMode = true;
        }
    };
    private KeyguardUpdateMonitor mUpdateMonitor;

    public OpFodFingerTouchValidator(Context context) {
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
    }

    public boolean validateFingerAction(int i) {
        Log.d("OpFodFingerTouchValidator", "validateFingerAction: getAction = " + i);
        if (i != 0 || this.mFingerOnView) {
            if ((i != 1 && i != 3) || !this.mFingerOnView) {
                return false;
            }
            this.mFingerOnView = false;
        } else if (this.mUpdateMonitor.isUnlockingWithBiometricAllowed() || OpFodHelper.getInstance().isDoingEnroll()) {
            this.mFingerOnView = true;
            this.mFingerOnSensor = false;
        } else {
            Log.d("OpFodFingerTouchValidator", "validateFingerAction: onView, not allow to unlock by fingerprint. return.");
            return false;
        }
        return true;
    }

    public boolean validateFingerAction(int i, int i2) {
        if (i != 6) {
            Log.d("OpFodFingerTouchValidator", "validateFingerAction: onSensor, return, acquiredInfo: " + i);
            return false;
        } else if (this.mFingerOnView) {
            Log.d("OpFodFingerTouchValidator", "validateFingerAction: onSensor, finger on view return.");
            return false;
        } else if (i2 != 0 && i2 != 1) {
            Log.d("OpFodFingerTouchValidator", "validateFingerAction: onSensor, return, vendorCode: " + i2);
            return false;
        } else if (i2 == 0) {
            this.mFingerOnSensor = true;
            this.mFingerOnView = false;
            return true;
        } else if (this.mFingerOnSensor) {
            this.mFingerOnSensor = false;
            return true;
        } else {
            Log.d("OpFodFingerTouchValidator", "validateFingerAction: onSensor, not receive touch down before.");
            return false;
        }
    }

    public boolean isFingerDown() {
        return this.mFingerOnView || this.mFingerOnSensor;
    }

    public String toString() {
        return String.format("([%s]: aodMode: %b, fingerOnView: %b, fingerOnSensor: %b)", "OpFodFingerTouchValidator", Boolean.valueOf(this.mAodMode), Boolean.valueOf(this.mFingerOnView), Boolean.valueOf(this.mFingerOnSensor)).toString();
    }

    public boolean isFingerDownOnView() {
        return this.mFingerOnView;
    }

    public boolean isFingerDownOnSensor() {
        return this.mFingerOnSensor;
    }

    public void resetTouchFromSensor() {
        Log.d("OpFodFingerTouchValidator", "resetTouchFromSensor");
        this.mFingerOnSensor = false;
    }

    public void reset() {
        Log.d("OpFodFingerTouchValidator", "reset: callers= " + Debug.getCallers(1));
        this.mFingerOnView = false;
        this.mFingerOnSensor = false;
    }
}
