package com.oneplus.systemui.biometrics;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.oneplus.core.oimc.OIMCServiceManager;
import com.oneplus.keyguard.OpKeyguardUpdateMonitor;
import com.oneplus.util.OpUtils;
import vendor.oneplus.hardware.display.V1_0.IOneplusDisplay;
public class OpFodDisplayNotifier {
    private static final int REAL_ALWAYS_ON_DELAY_TIME = SystemProperties.getInt("debug.real_aod_delay_time", 50);
    private AodModeNotifyRunnable mAodModeNotifyRunnable;
    private Context mContext;
    private IOneplusDisplay mDaemon = null;
    private DimModeNotifyRunnable mDimModeNotifyRunnable;
    private DisableFpModeRunable mDisableFpModeRunnable;
    private Handler mHandler;
    private HideAodModeRunnable mHideAodModeRunnable;
    private final OIMCServiceManager mOIMCServiceManager;
    private PressModeRunnable mPressModeRunnable;
    private ContentResolver mResolver;
    private OpKeyguardUpdateMonitor mUpdateMonitor;

    public OpFodDisplayNotifier(Context context) {
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mUpdateMonitor = (OpKeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mOIMCServiceManager = new OIMCServiceManager();
        try {
            this.mDaemon = IOneplusDisplay.getService();
        } catch (Exception e) {
            Log.d("OpFodDisplayNotifier", "IOneplusDisplay getService Exception e = " + e.toString());
        }
        HandlerThread handlerThread = new HandlerThread("OpFodDisplayNotifier", -10);
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper());
        this.mDimModeNotifyRunnable = new DimModeNotifyRunnable();
        this.mAodModeNotifyRunnable = new AodModeNotifyRunnable();
        this.mPressModeRunnable = new PressModeRunnable();
        this.mHideAodModeRunnable = new HideAodModeRunnable();
        this.mDisableFpModeRunnable = new DisableFpModeRunable();
    }

    /* access modifiers changed from: protected */
    public void forceDisableFingerprintMode() {
        if (OpUtils.isCustomFingerprint()) {
            this.mHandler.removeCallbacks(this.mDisableFpModeRunnable);
            this.mHandler.post(this.mDisableFpModeRunnable);
        }
    }

    public void notifyAodMode(int i) {
        if (OpUtils.isCustomFingerprint()) {
            this.mHandler.removeCallbacks(this.mAodModeNotifyRunnable);
            this.mAodModeNotifyRunnable.setMode(i);
            if (i == 5) {
                this.mHandler.postDelayed(this.mAodModeNotifyRunnable, (long) REAL_ALWAYS_ON_DELAY_TIME);
            } else {
                this.mHandler.post(this.mAodModeNotifyRunnable);
            }
        }
    }

    public void notifyDisplayDimMode(int i, int i2) {
        this.mHandler.removeCallbacks(this.mDimModeNotifyRunnable);
        this.mDimModeNotifyRunnable.setMode(i, i2);
        this.mHandler.post(this.mDimModeNotifyRunnable);
    }

    public void notifyPressMode(int i) {
        this.mPressModeRunnable.setMode(i);
        this.mHandler.post(this.mPressModeRunnable);
    }

    public void notifyHideAodMode(int i) {
        this.mHideAodModeRunnable.setMode(i);
        this.mHandler.post(this.mHideAodModeRunnable);
    }

    private class AodModeNotifyRunnable implements Runnable {
        private int mAodMode;

        private AodModeNotifyRunnable() {
        }

        /* access modifiers changed from: protected */
        public void setMode(int i) {
            this.mAodMode = i;
        }

        @Override // java.lang.Runnable
        public void run() {
            int i = this.mAodMode;
            if (i == 5 || i == 4) {
                Settings.System.putIntForUser(OpFodDisplayNotifier.this.mResolver, "auto_brightness_in_aod", 0, -2);
            } else {
                Settings.System.putIntForUser(OpFodDisplayNotifier.this.mResolver, "auto_brightness_in_aod", 1, -2);
            }
            try {
                Log.i("OpFodDisplayNotifier", "set OP_DISPLAY_AOD_MODE: " + this.mAodMode);
                OpFodDisplayNotifier.this.mDaemon.setMode(8, this.mAodMode);
            } catch (Exception e) {
                Log.d("OpFodDisplayNotifier", "postOnBackgroundThread Exception e = " + e.toString());
            }
        }
    }

    private class DimModeNotifyRunnable implements Runnable {
        private int mAodMode;
        private int mDimMode;

        public DimModeNotifyRunnable() {
        }

        /* access modifiers changed from: protected */
        public void setMode(int i, int i2) {
            this.mDimMode = i;
            this.mAodMode = i2;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                boolean isKeyguardDone = OpFodDisplayNotifier.this.mUpdateMonitor.isKeyguardDone();
                Log.d("OpFodDisplayNotifier", "set dim mDimMode to " + this.mDimMode + ", aodMode= " + this.mAodMode + ", isKeyguardDone = " + isKeyguardDone);
                OpFodDisplayNotifier.this.mDaemon.setMode(10, this.mDimMode);
                if (this.mDimMode != 0) {
                    OpFodDisplayNotifier.this.mOIMCServiceManager.notifyModeChange("FingerPrintMode", 1, 0);
                } else if ((this.mAodMode != 2 && this.mAodMode != 5) || ((this.mAodMode == 2 || this.mAodMode == 5) && isKeyguardDone)) {
                    OpFodDisplayNotifier.this.mOIMCServiceManager.notifyModeChange("FingerPrintMode", 2, 0);
                }
            } catch (Exception e) {
                Log.d("OpFodDisplayNotifier", "notifyDisplayDimMode Exception e = " + e.toString());
            }
        }
    }

    private class PressModeRunnable implements Runnable {
        private int mPressMode;

        public PressModeRunnable() {
        }

        /* access modifiers changed from: protected */
        public void setMode(int i) {
            this.mPressMode = i;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                Log.i("OpFodDisplayNotifier", "notifyPressMode: " + this.mPressMode);
                OpFodDisplayNotifier.this.mDaemon.setMode(9, this.mPressMode);
            } catch (Exception e) {
                Log.d("OpFodDisplayNotifier", "postOnBackgroundThread Exception e = " + e.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public class HideAodModeRunnable implements Runnable {
        private int mHideAodMode;

        public HideAodModeRunnable() {
        }

        /* access modifiers changed from: protected */
        public void setMode(int i) {
            this.mHideAodMode = i;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                Log.i("OpFodDisplayNotifier", "OP_DISPLAY_APPLY_HIDE_AOD: " + this.mHideAodMode);
                OpFodDisplayNotifier.this.mDaemon.setMode(11, this.mHideAodMode);
            } catch (Exception e) {
                Log.d("OpFodDisplayNotifier", "postOnBackgroundThread Exception e = " + e.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public class DisableFpModeRunable implements Runnable {
        public DisableFpModeRunable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                Log.i("OpFodDisplayNotifier", "force disable fp mode");
                OpFodDisplayNotifier.this.mOIMCServiceManager.notifyModeChange("FingerPrintMode", 2, 0);
            } catch (Exception e) {
                Log.d("OpFodDisplayNotifier", "postOnBackgroundThread Exception e = " + e.toString());
            }
        }
    }
}
