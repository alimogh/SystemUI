package com.oneplus.keyguard.clock;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.oneplus.plugin.OpLsState;
import java.util.Date;
public class OpClockCtrl implements AlarmManager.OnAlarmListener {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    public static final int TIME_CHANGED_INTERVAL = SystemProperties.getInt("sys.aod.time.interval", 60000);
    private static OpClockCtrl mInstance;
    private AlarmManager mAlarmManager;
    private BGHandler mBGHandler;
    private OnTimeUpdatedListener mListener;
    private final Object mLock = new Object();
    private Looper mNonUiLooper;
    private PowerManager mPM;
    private boolean mScreenON;

    public interface OnTimeUpdatedListener {
        void onTimeChanged();
    }

    public static synchronized OpClockCtrl getInstance() {
        OpClockCtrl opClockCtrl;
        synchronized (OpClockCtrl.class) {
            if (mInstance == null) {
                mInstance = new OpClockCtrl();
            }
            opClockCtrl = mInstance;
        }
        return opClockCtrl;
    }

    public void onStartCtrl(OnTimeUpdatedListener onTimeUpdatedListener, Context context) {
        this.mListener = onTimeUpdatedListener;
        if (this.mBGHandler == null) {
            this.mBGHandler = new BGHandler(getNonUILooper());
            if (context != null) {
                PowerManager powerManager = (PowerManager) context.getSystemService("power");
                this.mPM = powerManager;
                this.mScreenON = powerManager.isScreenOn();
                this.mAlarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
            }
        }
        startUpdate("startCtrl");
    }

    public void onScreenTurnedOn() {
        this.mScreenON = true;
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        if (keyguardUpdateMonitor == null || !keyguardUpdateMonitor.isDreaming() || !keyguardUpdateMonitor.isAlwaysOnEnabled() || this.mPM.isInteractive()) {
            startUpdate("ScreenON");
            return;
        }
        dispatchTimeChanged();
        startSchedule("screen turned on");
    }

    public void onScreenTurnedOff() {
        this.mScreenON = false;
        stopUpdate("ScreenOFF");
        cancelSchedule();
    }

    /* access modifiers changed from: private */
    public class BGHandler extends Handler {
        public BGHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message == null) {
                Log.w("ClockCtrl", "BGHandler: msg null");
            } else if (message.what == 131072) {
                OpClockCtrl opClockCtrl = OpClockCtrl.this;
                boolean z = true;
                if (message.arg1 != 1) {
                    z = false;
                }
                opClockCtrl.handleNotifySchedule(z);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifySchedule(boolean z) {
        long currentTimeMillis = System.currentTimeMillis();
        long j = (((currentTimeMillis / 60000) + 1) * 60000) - currentTimeMillis;
        Log.i("ClockCtrl", " schedule next: " + j);
        if (this.mBGHandler != null) {
            Message obtain = Message.obtain();
            obtain.what = 131072;
            obtain.arg1 = 1;
            this.mBGHandler.sendMessageDelayed(obtain, j);
        }
        if (z) {
            dispatchTimeChanged();
        }
    }

    private void startUpdate(String str) {
        Log.i("ClockCtrl", "startUpdate: " + str + ", " + this.mScreenON);
        if (this.mScreenON) {
            BGHandler bGHandler = this.mBGHandler;
            if (bGHandler != null) {
                bGHandler.removeMessages(131072);
                this.mBGHandler.sendEmptyMessage(131072);
            }
            dispatchTimeChanged();
        }
    }

    private void stopUpdate(String str) {
        Log.i("ClockCtrl", "stopUpdate: " + str);
        BGHandler bGHandler = this.mBGHandler;
        if (bGHandler != null) {
            bGHandler.removeMessages(131072);
        }
    }

    private void dispatchTimeChanged() {
        OnTimeUpdatedListener onTimeUpdatedListener = this.mListener;
        if (onTimeUpdatedListener != null) {
            onTimeUpdatedListener.onTimeChanged();
        }
    }

    public Looper getNonUILooper() {
        Looper looper;
        synchronized (this.mLock) {
            if (this.mNonUiLooper == null) {
                HandlerThread handlerThread = new HandlerThread("ClockCtrl thread");
                handlerThread.start();
                this.mNonUiLooper = handlerThread.getLooper();
            }
            looper = this.mNonUiLooper;
        }
        return looper;
    }

    public void onStartedWakingUp() {
        cancelSchedule();
    }

    private void startSchedule(String str) {
        long j;
        long currentTimeMillis = System.currentTimeMillis();
        if (TIME_CHANGED_INTERVAL < 60000) {
            int seconds = new Date().getSeconds();
            int i = TIME_CHANGED_INTERVAL;
            j = (long) (i - ((seconds * 1000) % i));
        } else {
            j = (((currentTimeMillis / 60000) + 1) * 60000) - currentTimeMillis;
        }
        long elapsedRealtime = SystemClock.elapsedRealtime() + j;
        if (DEBUG) {
            Log.d("ClockCtrl", "startSchedule: reason= " + str + ", delay= " + j + ", callers= " + Debug.getCallers(1));
        }
        this.mAlarmManager.setExact(2, elapsedRealtime, "OpClockCtrl.always_on", this, OpLsState.getInstance().getPhoneStatusBar().getAodWindowManager().getUIHandler());
    }

    private void cancelSchedule() {
        if (DEBUG) {
            Log.d("ClockCtrl", "cancelSchedule");
        }
        this.mAlarmManager.cancel(this);
    }

    @Override // android.app.AlarmManager.OnAlarmListener
    public void onAlarm() {
        dispatchTimeChanged();
        startSchedule("on alarm");
    }
}
