package com.oneplus.aod;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
public class OpDozeScrimController {
    protected KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private PowerManager.WakeLock mWakeLock;

    public void initWakeLock(Context context) {
        PowerManager.WakeLock newWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "OpDozeScrimController");
        this.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(true);
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
    }

    /* access modifiers changed from: protected */
    public void acquireWakeLock() {
        if (!this.mWakeLock.isHeld()) {
            Log.d("OpDozeScrimController", "hold pulse wake lock");
            this.mWakeLock.acquire();
        }
    }

    /* access modifiers changed from: protected */
    public void releaseWakeLock() {
        if (this.mWakeLock.isHeld()) {
            Log.d("OpDozeScrimController", "release pulse wake lock");
            this.mWakeLock.release();
        }
    }
}
