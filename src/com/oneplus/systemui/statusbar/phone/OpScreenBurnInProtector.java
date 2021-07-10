package com.oneplus.systemui.statusbar.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.C0009R$integer;
import java.util.ArrayList;
import java.util.List;
public class OpScreenBurnInProtector {
    private static OpScreenBurnInProtector mInstance;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector.1
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002b  */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x0034  */
        @Override // android.content.BroadcastReceiver
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(android.content.Context r3, android.content.Intent r4) {
            /*
                r2 = this;
                java.lang.String r3 = r4.getAction()
                int r4 = r3.hashCode()
                r0 = -2128145023(0xffffffff81271581, float:-3.0688484E-38)
                r1 = 1
                if (r4 == r0) goto L_0x001e
                r0 = -1454123155(0xffffffffa953d76d, float:-4.7038264E-14)
                if (r4 == r0) goto L_0x0014
                goto L_0x0028
            L_0x0014:
                java.lang.String r4 = "android.intent.action.SCREEN_ON"
                boolean r3 = r3.equals(r4)
                if (r3 == 0) goto L_0x0028
                r3 = 0
                goto L_0x0029
            L_0x001e:
                java.lang.String r4 = "android.intent.action.SCREEN_OFF"
                boolean r3 = r3.equals(r4)
                if (r3 == 0) goto L_0x0028
                r3 = r1
                goto L_0x0029
            L_0x0028:
                r3 = -1
            L_0x0029:
                if (r3 == 0) goto L_0x0034
                if (r3 == r1) goto L_0x002e
                goto L_0x0039
            L_0x002e:
                com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector r2 = com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector.this
                r2.stop()
                goto L_0x0039
            L_0x0034:
                com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector r2 = com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector.this
                r2.start()
            L_0x0039:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector.AnonymousClass1.onReceive(android.content.Context, android.content.Intent):void");
        }
    };
    private Context mContext;
    private int mCurrentMove = 0;
    private int mDuration = 12000;
    private Handler mHandler = new BIHandler();
    private List<OnBurnInPreventListener> mListeners = new ArrayList();
    private int mMaxDistance = 10;
    private boolean mMovingLeft = true;
    private boolean mStart = false;

    public interface OnBurnInPreventListener {
        void onBurnInPreventTrigger(int i);
    }

    public static synchronized OpScreenBurnInProtector getInstance() {
        OpScreenBurnInProtector opScreenBurnInProtector;
        synchronized (OpScreenBurnInProtector.class) {
            if (mInstance == null) {
                mInstance = new OpScreenBurnInProtector();
            }
            opScreenBurnInProtector = mInstance;
        }
        return opScreenBurnInProtector;
    }

    public void registerListener(Context context, OnBurnInPreventListener onBurnInPreventListener) {
        if (context == null) {
            Log.d("OpScreenBurnInProtector", "context is null");
            return;
        }
        if (this.mContext == null) {
            this.mContext = context;
            int i = Settings.Global.getInt(context.getContentResolver(), "debug_op_burn_in_time", 0) * 1000;
            int i2 = Settings.Global.getInt(this.mContext.getContentResolver(), "debug_op_burn_in_distance", 0);
            if (i == 0 || i2 == 0) {
                this.mDuration = this.mContext.getResources().getInteger(C0009R$integer.op_burn_in_time) * 1000;
                this.mMaxDistance = this.mContext.getResources().getInteger(C0009R$integer.op_burn_in_distance);
            } else {
                this.mDuration = i;
                this.mMaxDistance = i2;
            }
            Log.d("OpScreenBurnInProtector", "init duration " + this.mDuration + " distance " + this.mMaxDistance);
        }
        if (!(this.mDuration == 0 && this.mMaxDistance == 0) && !this.mListeners.contains(onBurnInPreventListener)) {
            this.mListeners.add(onBurnInPreventListener);
            if (this.mListeners.size() == 1) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.SCREEN_OFF");
                intentFilter.addAction("android.intent.action.SCREEN_ON");
                this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
            }
            onBurnInPreventListener.onBurnInPreventTrigger(this.mCurrentMove);
            start();
        }
    }

    public void unregisterListener(OnBurnInPreventListener onBurnInPreventListener) {
        if (this.mListeners.contains(onBurnInPreventListener)) {
            this.mListeners.remove(onBurnInPreventListener);
            if (this.mListeners.size() == 0) {
                this.mContext.unregisterReceiver(this.mBroadcastReceiver);
            }
        }
    }

    private class BIHandler extends Handler {
        private BIHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message == null) {
                Log.w("OpScreenBurnInProtector", "msg null");
            } else if (message.what == 1) {
                OpScreenBurnInProtector.this.moveToNext();
            }
        }
    }

    public void start() {
        if (!this.mStart && !this.mHandler.hasMessages(1)) {
            this.mStart = true;
            this.mHandler.sendEmptyMessageDelayed(1, (long) this.mDuration);
        }
    }

    public void stop() {
        this.mStart = false;
        this.mHandler.removeMessages(1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void moveToNext() {
        if (Math.abs(this.mCurrentMove) >= this.mMaxDistance) {
            this.mMovingLeft = !this.mMovingLeft;
        }
        this.mCurrentMove += this.mMovingLeft ? -1 : 1;
        Log.d("OpScreenBurnInProtector", "moveToNext " + this.mCurrentMove);
        for (OnBurnInPreventListener onBurnInPreventListener : this.mListeners) {
            onBurnInPreventListener.onBurnInPreventTrigger(this.mCurrentMove);
        }
        this.mHandler.sendEmptyMessageDelayed(1, (long) this.mDuration);
    }
}
