package com.oneplus.aod.utils.bitmoji.triggers.base;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.oneplus.aod.utils.bitmoji.OpBitmojiHelper;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.keyguard.clock.OpClockCtrl;
import java.io.FileDescriptor;
import java.io.PrintWriter;
public abstract class Trigger {
    public static final int IMAGES_PER_SET = SystemProperties.getInt("sys.aod.bitmoji.imageset", 5);
    public static final int IMAGE_PER_TIME = SystemProperties.getInt("sys.aod.bitmoji.refreshtime", OpClockCtrl.TIME_CHANGED_INTERVAL * 4);
    protected OpBitmojiManager mBitmojiManager;
    protected Context mContext;
    protected long mStartTime;
    protected final String mTag = getClass().getSimpleName();
    protected int mTimeIndex;

    public void dumpDetail(FileDescriptor fileDescriptor, IndentingPrintWriter indentingPrintWriter, String[] strArr) {
    }

    public void dynamicConfig(String[] strArr) {
    }

    public abstract String[] getCurrentImageArray();

    public abstract String getCurrentPackId();

    public abstract String getMdmLabel();

    public abstract int getPriority();

    /* access modifiers changed from: protected */
    public abstract int getUsedImageCount();

    public void init() {
    }

    public boolean isActive() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void onCompleted() {
    }

    public abstract void onImagePackUpdate(String str);

    /* access modifiers changed from: protected */
    public abstract void upateUsedImageCount(int i);

    public Trigger(Context context, OpBitmojiManager opBitmojiManager) {
        this.mContext = context;
        this.mBitmojiManager = opBitmojiManager;
    }

    public String toString() {
        return this.mTag + "/" + this.mTimeIndex;
    }

    public void prepare() {
        this.mStartTime = SystemClock.elapsedRealtime();
        int currentTimeMillis = ((int) (System.currentTimeMillis() % 60000)) % OpClockCtrl.TIME_CHANGED_INTERVAL;
        if (currentTimeMillis != 0) {
            this.mStartTime -= (long) currentTimeMillis;
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.d(this.mTag, "prepare: startTime= " + this.mStartTime + ", timeDiff= " + currentTimeMillis);
        }
        this.mTimeIndex = -1;
        updateImageList();
    }

    public boolean complete() {
        if (getCurrentIndex() >= IMAGES_PER_SET) {
            onCompleted();
            return true;
        }
        updateImageList();
        return false;
    }

    /* access modifiers changed from: protected */
    public int getCurrentIndex() {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long j = this.mStartTime;
        if (elapsedRealtime >= j) {
            int i = (int) ((elapsedRealtime - j) / ((long) IMAGE_PER_TIME));
            if (i < IMAGES_PER_SET) {
                if (Build.DEBUG_ONEPLUS) {
                    String str = this.mTag;
                    Log.d(str, "getCurrentIndex: currentTime= " + elapsedRealtime + ", index= " + i);
                }
                return i;
            }
            String str2 = this.mTag;
            Log.w(str2, "getCurrentIndex: mStartTime= " + this.mStartTime + ", currentTime= " + elapsedRealtime + ", index= " + i + " is bigger than IMAGES_PER_SET");
            return IMAGES_PER_SET;
        } else if (!Build.DEBUG_ONEPLUS) {
            return 0;
        } else {
            Log.d(this.mTag, "getCurrentIndex: currentTime is smaller then startTime.");
            return 0;
        }
    }

    public String getImagePath() {
        String[] currentImageArray = getCurrentImageArray();
        if (currentImageArray == null || currentImageArray.length <= 0) {
            return null;
        }
        return currentImageArray[currentImageArray.length - 1];
    }

    /* access modifiers changed from: protected */
    public KeyguardUpdateMonitor getKeyguardUpdateMonitor() {
        return this.mBitmojiManager.getKeyguardUpdateMonitor();
    }

    /* access modifiers changed from: protected */
    public Handler getHandler() {
        return this.mBitmojiManager.getHandler();
    }

    /* access modifiers changed from: protected */
    public void acquireWakeLock(long j) {
        this.mBitmojiManager.getWakeLock().acquire(j);
    }

    /* access modifiers changed from: protected */
    public OpBitmojiHelper getHelper() {
        return OpBitmojiHelper.getInstance();
    }

    private void updateImageList() {
        int currentIndex = getCurrentIndex();
        if (this.mTimeIndex != currentIndex) {
            this.mTimeIndex = currentIndex;
            String[] currentImageArray = getCurrentImageArray();
            if (currentImageArray != null && currentImageArray.length > 0) {
                int random = (int) (Math.random() * ((double) (currentImageArray.length - getUsedImageCount())));
                if (random < currentImageArray.length - 1) {
                    String str = currentImageArray[random];
                    System.arraycopy(currentImageArray, random + 1, currentImageArray, random, (currentImageArray.length - random) - 1);
                    currentImageArray[currentImageArray.length - 1] = str;
                    upateUsedImageCount(currentImageArray.length);
                }
                if (Build.DEBUG_ONEPLUS) {
                    String str2 = this.mTag;
                    Log.d(str2, "updateImageList: timeIndex= " + currentIndex + ", arrayIndex= " + random + ", usedImageCount= " + getUsedImageCount());
                }
            } else if (Build.DEBUG_ONEPLUS) {
                String str3 = this.mTag;
                Log.d(str3, "updateImageList: timeIndex= " + currentIndex + ", imageList is empty right now");
            }
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(this.mTag + ":");
        IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(printWriter, "  ");
        indentingPrintWriter.increaseIndent();
        indentingPrintWriter.println("startTime=" + this.mStartTime);
        indentingPrintWriter.println("timeIndex=" + this.mTimeIndex);
        indentingPrintWriter.println("usedImageCount= " + getUsedImageCount());
        dumpDetail(fileDescriptor, indentingPrintWriter, strArr);
        String[] currentImageArray = getCurrentImageArray();
        if (currentImageArray != null) {
            indentingPrintWriter.println("imageList size=" + currentImageArray.length);
        } else {
            indentingPrintWriter.println("imageList is empty");
        }
        indentingPrintWriter.println("isActive=" + isActive());
        indentingPrintWriter.println("priority=" + getPriority());
        indentingPrintWriter.decreaseIndent();
    }
}
