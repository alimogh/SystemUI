package com.oneplus.aod.views.parsons;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0015R$string;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.aod.utils.OpAodDimenHelper;
import com.oneplus.aod.views.parsons.OpUnlockDataHelper;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
public class OpUnlockDataHelper implements OpKeyguardUnlockCounter.OpKeyguardUnlockCounterListener {
    private OpKeyguardUnlockCounter mKeyguardUnlockCounter;
    private int mUnlockCount = -1;
    private UnlockDrawingHelper mUnlockDrawingHelper;
    private OpParsonsUnlockLabel mUnlocksMsg;

    public OpUnlockDataHelper(Context context, OpParsonsBar opParsonsBar) {
        this.mUnlockDrawingHelper = new UnlockDrawingHelper(context, opParsonsBar);
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        if (phoneStatusBar != null) {
            OpKeyguardUnlockCounter keyguardUnlockCounter = phoneStatusBar.getKeyguardUnlockCounter();
            this.mKeyguardUnlockCounter = keyguardUnlockCounter;
            this.mUnlockDrawingHelper.setKeyguardUnlockCounter(keyguardUnlockCounter);
        }
    }

    @Override // com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter.OpKeyguardUnlockCounterListener
    public void onUnlockDataChanged() {
        requestData();
    }

    public void setUnlocksMsg(OpParsonsUnlockLabel opParsonsUnlockLabel) {
        this.mUnlocksMsg = opParsonsUnlockLabel;
    }

    public void updateResources() {
        this.mUnlockDrawingHelper.updateResources();
    }

    public void onTimeChanged(long j, long j2) {
        if (this.mKeyguardUnlockCounter != null) {
            this.mUnlockDrawingHelper.onTimeChanged(j, j2);
        }
    }

    public void startListen() {
        if (Build.DEBUG_ONEPLUS) {
            StringBuilder sb = new StringBuilder();
            sb.append("startListen: ");
            sb.append(this.mKeyguardUnlockCounter != null);
            Log.d("OpUnlockDataHelper", sb.toString());
        }
        OpKeyguardUnlockCounter opKeyguardUnlockCounter = this.mKeyguardUnlockCounter;
        if (opKeyguardUnlockCounter != null) {
            opKeyguardUnlockCounter.addListener(this);
        }
    }

    public void stopListen() {
        if (Build.DEBUG_ONEPLUS) {
            StringBuilder sb = new StringBuilder();
            sb.append("stopListen: ");
            sb.append(this.mKeyguardUnlockCounter != null);
            Log.d("OpUnlockDataHelper", sb.toString());
        }
        OpKeyguardUnlockCounter opKeyguardUnlockCounter = this.mKeyguardUnlockCounter;
        if (opKeyguardUnlockCounter != null) {
            opKeyguardUnlockCounter.removeListener(this);
        }
    }

    public void clearUnlockRecord(Canvas canvas, int i, int i2, int i3, int i4, Paint paint) {
        this.mUnlockDrawingHelper.setRange(i, i2, i3, i4);
        this.mUnlockDrawingHelper.draw(canvas, paint);
        refreshUnlockCount(this.mUnlockDrawingHelper.getUnlockCount());
    }

    private void requestData() {
        Log.d("OpUnlockDataHelper", "requestData#start");
        Trace.beginSection("OpUnlockDataHelper#requestData");
        long elapsedRealtime = SystemClock.elapsedRealtime();
        this.mUnlockDrawingHelper.requestData();
        Log.d("OpUnlockDataHelper", String.format("requestData#end cost %d ms.", Long.valueOf(SystemClock.elapsedRealtime() - elapsedRealtime)));
        Trace.endSection();
    }

    private void refreshUnlockCount(int i) {
        int i2;
        if (this.mUnlockCount != i) {
            Log.d("OpUnlockDataHelper", "refreshUnlockCount: unlockCount= " + i);
            this.mUnlockCount = i;
            if (i == 1) {
                i2 = C0015R$string.op_aod_parsons_unlock_one;
            } else {
                i2 = C0015R$string.op_aod_parsons_unlock_other;
            }
            this.mUnlocksMsg.setUnlockMsg(i2, i);
        }
    }

    /* access modifiers changed from: private */
    public static class UnlockDrawingHelper {
        private OpParsonsBar mBar;
        private long mClockTime;
        private Context mContext;
        private long mDeltaTime;
        private float mHeightOfOneSecond;
        private int mMinGap;
        private Rect mRange = new Rect();
        private ArrayList<OpKeyguardUnlockCounter.UnlockRecord> mRecords = new ArrayList<>();
        private int mUnlockCount;
        private OpKeyguardUnlockCounter mUnlockCounter;

        public UnlockDrawingHelper(Context context, OpParsonsBar opParsonsBar) {
            this.mContext = context;
            this.mBar = opParsonsBar;
        }

        public void updateResources() {
            this.mMinGap = OpAodDimenHelper.convertDpToFixedPx2(this.mContext, C0005R$dimen.op_parsons_timeline_min_gap);
        }

        public void setKeyguardUnlockCounter(OpKeyguardUnlockCounter opKeyguardUnlockCounter) {
            this.mUnlockCounter = opKeyguardUnlockCounter;
        }

        public void onTimeChanged(long j, long j2) {
            OpKeyguardUnlockCounter opKeyguardUnlockCounter = this.mUnlockCounter;
            if (opKeyguardUnlockCounter != null) {
                this.mClockTime = opKeyguardUnlockCounter.getRelativeTime(j);
                this.mDeltaTime = j2;
                Log.d("UnlockDrawingHelper", "onTimeChanged: clockTime= " + j + ", relativeTime= " + this.mClockTime + ", deltaTime= " + j2);
                this.mBar.invalidate();
            }
        }

        public void requestData() {
            OpKeyguardUnlockCounter opKeyguardUnlockCounter = this.mUnlockCounter;
            if (opKeyguardUnlockCounter != null) {
                ArrayList<OpKeyguardUnlockCounter.UnlockRecord> retrieveRecords = opKeyguardUnlockCounter.retrieveRecords(opKeyguardUnlockCounter.getRelativeTime(SystemClock.elapsedRealtime()), this.mDeltaTime);
                synchronized (this.mRecords) {
                    this.mRecords.clear();
                    if (retrieveRecords != null) {
                        Iterator<OpKeyguardUnlockCounter.UnlockRecord> it = retrieveRecords.iterator();
                        while (it.hasNext()) {
                            this.mRecords.add(it.next());
                        }
                    }
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("UnlockDrawingHelper", "requestData: size= " + this.mRecords.size());
                    }
                }
                this.mBar.post(new Runnable() { // from class: com.oneplus.aod.views.parsons.-$$Lambda$OpUnlockDataHelper$UnlockDrawingHelper$RfyRvgW1lWsiyW5UPUJhFwlOGgg
                    @Override // java.lang.Runnable
                    public final void run() {
                        OpUnlockDataHelper.UnlockDrawingHelper.this.lambda$requestData$0$OpUnlockDataHelper$UnlockDrawingHelper();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$requestData$0 */
        public /* synthetic */ void lambda$requestData$0$OpUnlockDataHelper$UnlockDrawingHelper() {
            this.mBar.invalidate();
        }

        public int getUnlockCount() {
            return this.mUnlockCount;
        }

        public void setRange(int i, int i2, int i3, int i4) {
            this.mRange.set(i, i2, i3, i4);
            this.mHeightOfOneSecond = ((float) (((this.mBar.getHeight() - i2) - i2) - this.mBar.getOverlayView().getHeight())) / 86400.0f;
            if (Build.DEBUG_ONEPLUS) {
                Log.d("UnlockDrawingHelper", "setRange: left= " + i + ", top= " + i2 + ", right= " + i3 + ", bottom= " + i4 + ", heightOfOneSecond= " + this.mHeightOfOneSecond);
            }
        }

        public void draw(Canvas canvas, Paint paint) {
            ArrayList arrayList = new ArrayList();
            synchronized (this.mRecords) {
                Iterator<OpKeyguardUnlockCounter.UnlockRecord> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    arrayList.add(it.next().clone());
                }
            }
            int size = arrayList.size();
            this.mUnlockCount = 0;
            long beginTime = getBeginTime();
            for (int i = 0; i < size; i++) {
                OpKeyguardUnlockCounter.UnlockRecord unlockRecord = (OpKeyguardUnlockCounter.UnlockRecord) arrayList.get(i);
                long beginTime2 = unlockRecord.getBeginTime();
                long endTime = unlockRecord.getEndTime();
                if (endTime > beginTime) {
                    float relativeYStart = getRelativeYStart(beginTime2);
                    float relativeYEnd = getRelativeYEnd(endTime);
                    if (relativeYEnd >= relativeYStart) {
                        int i2 = this.mMinGap;
                        if (i2 != 0) {
                            float f = relativeYEnd - relativeYStart;
                            if (f < ((float) i2) && f > 0.0f) {
                                relativeYStart = relativeYEnd - ((float) i2);
                            }
                        }
                        Rect rect = this.mRange;
                        canvas.drawRect((float) rect.left, relativeYStart, (float) rect.right, relativeYEnd, paint);
                        this.mUnlockCount++;
                        if (Build.DEBUG_ONEPLUS) {
                            Log.d("UnlockDrawingHelper", "draw: i= " + i + ", record= " + unlockRecord.toString() + ", top= " + relativeYStart + ", bottom= " + relativeYEnd);
                        }
                    }
                } else if (Build.DEBUG_ONEPLUS) {
                    Log.d("UnlockDrawingHelper", "draw: filter out too old record. minTime= " + beginTime + ", record= " + unlockRecord.toString());
                }
            }
        }

        private long getBeginTime() {
            long j = this.mClockTime;
            long j2 = this.mDeltaTime;
            if (j > j2) {
                return j - j2;
            }
            return 0;
        }

        private float getRelativeYStart(long j) {
            return getRelativeY(j, true);
        }

        private float getRelativeYEnd(long j) {
            return getRelativeY(j, false);
        }

        private float getRelativeY(long j, boolean z) {
            Rect rect = this.mRange;
            int i = rect.top;
            int i2 = rect.bottom;
            long j2 = 0;
            if (j == 0) {
                if (z) {
                    return 0.0f;
                }
                return (float) i2;
            } else if (this.mHeightOfOneSecond == 0.0f) {
                return 0.0f;
            } else {
                long j3 = this.mClockTime;
                if (j3 > j) {
                    j2 = j3 - j;
                }
                return ((float) i2) - (((float) TimeUnit.MILLISECONDS.toSeconds(j2)) * this.mHeightOfOneSecond);
            }
        }
    }
}
