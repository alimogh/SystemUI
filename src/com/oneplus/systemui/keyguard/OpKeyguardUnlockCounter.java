package com.oneplus.systemui.keyguard;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
public class OpKeyguardUnlockCounter {
    private AlarmManager.OnAlarmListener mAlarmListener = new AlarmManager.OnAlarmListener() { // from class: com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter.2
        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpKeyguardUnlockCounter", "onAlarm");
            }
            OpKeyguardUnlockCounter.this.doSync();
        }
    };
    private AlarmManager mAlarmManager;
    private Context mContext;
    private H mHandler;
    private UnlockEvent mLastEvent;
    private ArrayList<OpKeyguardUnlockCounterListener> mListeners = new ArrayList<>();
    private Object mLock = new Object();
    private KeyguardUpdateMonitorCallback mMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter.1
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(int i) {
            OpKeyguardUnlockCounter.this.stopSchedule();
            if (i == 0) {
                OpKeyguardUnlockCounter.this.doSync();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTimeChanged() {
            OpKeyguardUnlockCounter.this.timestamp();
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onShuttingDown() {
            OpKeyguardUnlockCounter.this.reportEvent(true, SystemClock.elapsedRealtime());
            OpKeyguardUnlockCounter.this.timestamp();
        }
    };
    private OpRelativeTimeHelper mRelativeTimeHelper;

    public interface OpKeyguardUnlockCounterListener {
        void onUnlockDataChanged();
    }

    private long getBeginTime(long j) {
        if (j > 86400000) {
            return j - 86400000;
        }
        return 0;
    }

    public OpKeyguardUnlockCounter(Context context) {
        this.mContext = context;
        this.mHandler = new H((Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mAlarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
        this.mRelativeTimeHelper = new OpRelativeTimeHelper(context);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mMonitorCallback);
        doSync();
    }

    public void reportEvent(boolean z, long j) {
        if (!validUser()) {
            Log.i("OpKeyguardUnlockCounter", "not user owner, do not record!!!");
            return;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, new UnlockEvent(!z ? 1 : 0, this.mRelativeTimeHelper.getBeginTime() + j)));
    }

    public void addListener(OpKeyguardUnlockCounterListener opKeyguardUnlockCounterListener) {
        if (opKeyguardUnlockCounterListener != null && validUser()) {
            synchronized (this.mListeners) {
                if (!this.mListeners.contains(opKeyguardUnlockCounterListener)) {
                    this.mListeners.add(opKeyguardUnlockCounterListener);
                }
            }
            dispatchUnlockDataChanged();
        }
    }

    public void removeListener(OpKeyguardUnlockCounterListener opKeyguardUnlockCounterListener) {
        if (opKeyguardUnlockCounterListener != null) {
            synchronized (this.mListeners) {
                this.mListeners.remove(opKeyguardUnlockCounterListener);
            }
        }
    }

    public void dispatchUnlockDataChanged() {
        if (validUser()) {
            synchronized (this.mListeners) {
                if (this.mListeners.size() != 0) {
                    Iterator<OpKeyguardUnlockCounterListener> it = this.mListeners.iterator();
                    while (it.hasNext()) {
                        OpKeyguardUnlockCounterListener next = it.next();
                        if (next != null) {
                            next.onUnlockDataChanged();
                        }
                    }
                }
            }
        }
    }

    public ArrayList<UnlockRecord> retrieveRecords(long j, long j2) {
        long j3 = j > j2 ? j - j2 : 0;
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpKeyguardUnlockCounter", "retrieveRecords: beginTime= " + j3 + ", currentElapsedTime= " + j);
        }
        ArrayList<UnlockRecord> arrayList = new ArrayList<>();
        synchronized (this.mLock) {
            Stack<UnlockEvent> stack = new Stack<>();
            retrieveInStack(stack, j3, j);
            UnlockRecord unlockRecord = new UnlockRecord();
            while (true) {
                if (stack.empty()) {
                    break;
                }
                UnlockEvent pop = stack.pop();
                if (pop.mType == 0) {
                    if (pop.mTriggerTime <= j3) {
                        break;
                    }
                    unlockRecord.mEndTime = pop.mTriggerTime;
                } else if (pop.mType == 1) {
                    if (pop.mTriggerTime > j3) {
                        unlockRecord.mBeginTime = pop.mTriggerTime;
                    }
                    arrayList.add(unlockRecord.clone());
                    unlockRecord.mBeginTime = 0;
                    unlockRecord.mEndTime = 0;
                }
            }
        }
        return arrayList;
    }

    public long getRelativeTime(long j) {
        return this.mRelativeTimeHelper.getBeginTime() + j;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00f4, code lost:
        if (r3 != null) goto L_0x00e2;
     */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00fb A[SYNTHETIC, Splitter:B:50:0x00fb] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void syncRecordFromDisk() {
        /*
        // Method dump skipped, instructions count: 263
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter.syncRecordFromDisk():void");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0082  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x008c A[SYNTHETIC, Splitter:B:44:0x008c] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeToDisk(com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter.UnlockEvent r7) {
        /*
            r6 = this;
            boolean r0 = android.os.Build.DEBUG_ONEPLUS
            if (r0 == 0) goto L_0x001e
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "writeToDisk: event= "
            r0.append(r1)
            java.lang.String r1 = r7.logString()
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "OpKeyguardUnlockCounter"
            android.util.Log.d(r1, r0)
        L_0x001e:
            java.lang.Object r0 = r6.mLock
            monitor-enter(r0)
            com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter$UnlockEvent r1 = r6.mLastEvent     // Catch:{ all -> 0x0090 }
            if (r1 == 0) goto L_0x0036
            com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter$UnlockEvent r1 = r6.mLastEvent     // Catch:{ all -> 0x0090 }
            int r1 = r1.mType     // Catch:{ all -> 0x0090 }
            int r2 = r7.mType     // Catch:{ all -> 0x0090 }
            if (r1 != r2) goto L_0x0036
            java.lang.String r6 = "OpKeyguardUnlockCounter"
            java.lang.String r7 = "writeToDisk: same event type, abandon."
            android.util.Log.d(r6, r7)     // Catch:{ all -> 0x0090 }
            monitor-exit(r0)     // Catch:{ all -> 0x0090 }
            return
        L_0x0036:
            r6.mLastEvent = r7     // Catch:{ all -> 0x0090 }
            java.io.File r1 = new java.io.File     // Catch:{ all -> 0x0090 }
            android.content.Context r2 = r6.mContext     // Catch:{ all -> 0x0090 }
            java.io.File r2 = r2.getFilesDir()     // Catch:{ all -> 0x0090 }
            java.lang.String r3 = "unlockData"
            r1.<init>(r2, r3)     // Catch:{ all -> 0x0090 }
            boolean r2 = r1.exists()     // Catch:{ all -> 0x0090 }
            if (r2 != 0) goto L_0x0059
            r1.createNewFile()     // Catch:{ Exception -> 0x004f }
            goto L_0x0059
        L_0x004f:
            r6 = move-exception
            java.lang.String r7 = "OpKeyguardUnlockCounter"
            java.lang.String r1 = "writeToDisk: create file failed!"
            android.util.Log.e(r7, r1, r6)
            monitor-exit(r0)
            return
        L_0x0059:
            r2 = 0
            java.io.PrintWriter r3 = new java.io.PrintWriter     // Catch:{ Exception -> 0x0078 }
            java.io.FileWriter r4 = new java.io.FileWriter     // Catch:{ Exception -> 0x0078 }
            r5 = 1
            r4.<init>(r1, r5)     // Catch:{ Exception -> 0x0078 }
            r3.<init>(r4)     // Catch:{ Exception -> 0x0078 }
            java.lang.String r7 = r7.toString()     // Catch:{ Exception -> 0x0073, all -> 0x0070 }
            r3.println(r7)     // Catch:{ Exception -> 0x0073, all -> 0x0070 }
            r3.close()     // Catch:{ Exception -> 0x0085 }
            goto L_0x0085
        L_0x0070:
            r6 = move-exception
            r2 = r3
            goto L_0x008a
        L_0x0073:
            r7 = move-exception
            r2 = r3
            goto L_0x0079
        L_0x0076:
            r6 = move-exception
            goto L_0x008a
        L_0x0078:
            r7 = move-exception
        L_0x0079:
            java.lang.String r1 = "OpKeyguardUnlockCounter"
            java.lang.String r3 = "writeToDisk: occur error"
            android.util.Log.e(r1, r3, r7)     // Catch:{ all -> 0x0076 }
            if (r2 == 0) goto L_0x0085
            r2.close()
        L_0x0085:
            monitor-exit(r0)
            r6.dispatchUnlockDataChanged()
            return
        L_0x008a:
            if (r2 == 0) goto L_0x008f
            r2.close()     // Catch:{ Exception -> 0x008f }
        L_0x008f:
            throw r6
        L_0x0090:
            r6 = move-exception
            monitor-exit(r0)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter.writeToDisk(com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter$UnlockEvent):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x0072 A[SYNTHETIC, Splitter:B:35:0x0072] */
    /* JADX WARNING: Removed duplicated region for block: B:47:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean retrieveInStack(java.util.Stack<com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter.UnlockEvent> r4, long r5, long r7) {
        /*
            r3 = this;
            java.lang.String r5 = "OpKeyguardUnlockCounter"
            java.io.File r6 = new java.io.File
            android.content.Context r3 = r3.mContext
            java.io.File r3 = r3.getFilesDir()
            java.lang.String r0 = "unlockData"
            r6.<init>(r3, r0)
            boolean r3 = r6.exists()
            if (r3 == 0) goto L_0x0076
            r3 = 0
            java.io.BufferedReader r0 = new java.io.BufferedReader     // Catch:{ Exception -> 0x0064, all -> 0x0060 }
            java.io.FileReader r1 = new java.io.FileReader     // Catch:{ Exception -> 0x0064, all -> 0x0060 }
            r1.<init>(r6)     // Catch:{ Exception -> 0x0064, all -> 0x0060 }
            r0.<init>(r1)     // Catch:{ Exception -> 0x0064, all -> 0x0060 }
        L_0x0020:
            java.lang.String r3 = r0.readLine()     // Catch:{ Exception -> 0x005e }
            if (r3 == 0) goto L_0x005a
            com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter$UnlockEvent r3 = com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter.UnlockEvent.parse(r3)     // Catch:{ Exception -> 0x005e }
            if (r3 == 0) goto L_0x0020
            long r1 = r3.mTriggerTime     // Catch:{ Exception -> 0x005e }
            int r6 = (r1 > r7 ? 1 : (r1 == r7 ? 0 : -1))
            if (r6 <= 0) goto L_0x0056
            boolean r6 = android.os.Build.DEBUG_ONEPLUS     // Catch:{ Exception -> 0x005e }
            if (r6 == 0) goto L_0x004e
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x005e }
            r6.<init>()     // Catch:{ Exception -> 0x005e }
            java.lang.String r7 = "retrieveInStack: event invalid: "
            r6.append(r7)     // Catch:{ Exception -> 0x005e }
            java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x005e }
            r6.append(r3)     // Catch:{ Exception -> 0x005e }
            java.lang.String r3 = r6.toString()     // Catch:{ Exception -> 0x005e }
            android.util.Log.w(r5, r3)     // Catch:{ Exception -> 0x005e }
        L_0x004e:
            r4.clear()     // Catch:{ Exception -> 0x005e }
            r3 = 0
            r0.close()     // Catch:{ Exception -> 0x0055 }
        L_0x0055:
            return r3
        L_0x0056:
            r4.push(r3)
            goto L_0x0020
        L_0x005a:
            r0.close()     // Catch:{ Exception -> 0x0076 }
            goto L_0x0076
        L_0x005e:
            r3 = move-exception
            goto L_0x0067
        L_0x0060:
            r4 = move-exception
            r0 = r3
            r3 = r4
            goto L_0x0070
        L_0x0064:
            r4 = move-exception
            r0 = r3
            r3 = r4
        L_0x0067:
            java.lang.String r4 = "syncRecordFromDisk: read file occur error"
            android.util.Log.e(r5, r4, r3)     // Catch:{ all -> 0x006f }
            if (r0 == 0) goto L_0x0076
            goto L_0x005a
        L_0x006f:
            r3 = move-exception
        L_0x0070:
            if (r0 == 0) goto L_0x0075
            r0.close()     // Catch:{ Exception -> 0x0075 }
        L_0x0075:
            throw r3
        L_0x0076:
            r3 = 1
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter.retrieveInStack(java.util.Stack, long, long):boolean");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doSync() {
        this.mHandler.sendEmptyMessage(0);
        startSchedule();
    }

    public static String logFormatTime(long j) {
        return DateUtils.formatElapsedTime(j / 1000);
    }

    private boolean validUser() {
        return KeyguardUpdateMonitor.getCurrentUser() == 0;
    }

    private void startSchedule() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpKeyguardUnlockCounter", "startSchedule");
        }
        this.mAlarmManager.setExact(3, SystemClock.elapsedRealtime() + 1800000, "OpKeyguardUnlockCounter-SYNC", this.mAlarmListener, this.mHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopSchedule() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpKeyguardUnlockCounter", "stopSchedule");
        }
        this.mAlarmManager.cancel(this.mAlarmListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void timestamp() {
        this.mRelativeTimeHelper.recordLastElapsedRealtime();
        this.mRelativeTimeHelper.recordLastActiveTime();
    }

    public class H extends Handler {
        public H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 0) {
                OpKeyguardUnlockCounter.this.syncRecordFromDisk();
            } else if (i == 1) {
                OpKeyguardUnlockCounter.this.writeToDisk((UnlockEvent) message.obj);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class UnlockEvent {
        long mTriggerTime;
        int mType;

        public UnlockEvent(int i, long j) {
            this.mType = i;
            this.mTriggerTime = j;
        }

        public static UnlockEvent parse(String str) {
            String[] split;
            if (TextUtils.isEmpty(str) || (split = str.split("\\|")) == null || split.length != 2) {
                return null;
            }
            try {
                return new UnlockEvent(Integer.parseInt(split[0]), Long.parseLong(split[1]));
            } catch (NumberFormatException unused) {
                Log.e("OpKeyguardUnlockCounter", "parse error: " + str);
                return null;
            }
        }

        public String toString() {
            return String.format("%d|%d", Integer.valueOf(this.mType), Long.valueOf(this.mTriggerTime)).toString();
        }

        public String logString() {
            return String.format("%d|%s", Integer.valueOf(this.mType), OpKeyguardUnlockCounter.logFormatTime(this.mTriggerTime));
        }
    }

    public static class UnlockRecord implements Cloneable {
        long mBeginTime;
        long mEndTime;

        UnlockRecord() {
        }

        @Override // java.lang.Object
        public UnlockRecord clone() {
            try {
                return (UnlockRecord) super.clone();
            } catch (CloneNotSupportedException e) {
                Log.e("UnlockRecord", "clone failed!", e);
                return null;
            }
        }

        public long getBeginTime() {
            return this.mBeginTime;
        }

        public long getEndTime() {
            return this.mEndTime;
        }

        public String getBeginTimeInFormat() {
            return OpKeyguardUnlockCounter.logFormatTime(this.mBeginTime);
        }

        public String getEndTimeInFormat() {
            return OpKeyguardUnlockCounter.logFormatTime(this.mEndTime);
        }

        @Override // java.lang.Object
        public String toString() {
            return String.format("UnlockRecord: mBeginTime= " + this.mBeginTime + ", mEndTime= " + this.mEndTime + ", begin= " + getBeginTimeInFormat() + ", end= " + getEndTimeInFormat(), new Object[0]);
        }
    }
}
