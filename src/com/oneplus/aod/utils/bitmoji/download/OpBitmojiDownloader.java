package com.oneplus.aod.utils.bitmoji.download;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import com.oneplus.aod.utils.bitmoji.OpBitmojiHelper;
import com.oneplus.aod.utils.bitmoji.download.task.AvatarDownloadTask;
import com.oneplus.aod.utils.bitmoji.download.task.BaseDownloadTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
public class OpBitmojiDownloader {
    private HashMap<String, Future<?>> mDownloadMap = new HashMap<>();
    private HashMap<String, Boolean> mForceMap = new HashMap<>();
    private ExecutorService mPool = Executors.newSingleThreadExecutor();
    public BaseDownloadTask.OnTaskListener mTaskListener = new BaseDownloadTask.OnTaskListener() { // from class: com.oneplus.aod.utils.bitmoji.download.OpBitmojiDownloader.1
        @Override // com.oneplus.aod.utils.bitmoji.download.task.BaseDownloadTask.OnTaskListener
        public void onTaskFinished(String str) {
            Log.d("OpBitmojiDownloader", "onTaskFinished: key= " + str);
            synchronized (OpBitmojiDownloader.this.mDownloadMap) {
                OpBitmojiDownloader.this.mDownloadMap.remove(str);
                OpBitmojiDownloader.this.mForceMap.remove(str);
            }
            if (!OpBitmojiDownloader.this.hasUndoneTask()) {
                OpBitmojiDownloader.this.wakeLockRelease("all task finished");
                OpBitmojiHelper.getInstance().shouldNotifyDownloadStatusChange();
                OpBitmojiDownloader.this.clearCounter();
            }
        }
    };
    private int mTotalCount;
    private PowerManager.WakeLock mWakeLock;

    public OpBitmojiDownloader(Context context) {
        PowerManager.WakeLock newWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, OpBitmojiDownloader.class.getSimpleName() + ":run");
        this.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(false);
    }

    public void enqueue(BaseDownloadTask baseDownloadTask) {
        Log.d("OpBitmojiDownloader", "enqueue: key= " + baseDownloadTask.key());
        baseDownloadTask.setTaskListener(this.mTaskListener);
        synchronized (this.mDownloadMap) {
            wakeLockAcquire("enqueue");
            if (!(baseDownloadTask instanceof AvatarDownloadTask)) {
                this.mTotalCount++;
            }
            this.mDownloadMap.put(baseDownloadTask.key(), this.mPool.submit(baseDownloadTask));
            this.mForceMap.put(baseDownloadTask.key(), new Boolean(baseDownloadTask.isForce()));
        }
        OpBitmojiHelper.getInstance().shouldNotifyDownloadStatusChange();
    }

    public void stopAll() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiDownloader", "stopAll");
        }
        synchronized (this.mDownloadMap) {
            for (Future<?> future : this.mDownloadMap.values()) {
                if (future != null) {
                    future.cancel(true);
                }
            }
            this.mDownloadMap.clear();
            this.mForceMap.clear();
            clearCounter();
            wakeLockRelease("stopAll");
        }
        OpBitmojiHelper.getInstance().shouldNotifyDownloadStatusChange();
    }

    public int getTotalCount() {
        int i;
        synchronized (this.mDownloadMap) {
            i = this.mTotalCount;
        }
        return i;
    }

    public boolean hasForceData() {
        synchronized (this.mDownloadMap) {
            for (Map.Entry<String, Boolean> entry : this.mForceMap.entrySet()) {
                if (entry.getValue().booleanValue()) {
                    return true;
                }
            }
            return false;
        }
    }

    public int getRunningTaskCount() {
        int size;
        synchronized (this.mDownloadMap) {
            size = this.mDownloadMap.size();
            if (this.mDownloadMap.containsKey("avatar")) {
                size--;
            }
        }
        return size;
    }

    private int getRunningTaskCountInner() {
        int size;
        synchronized (this.mDownloadMap) {
            size = this.mDownloadMap.size();
        }
        return size;
    }

    public boolean hasUndoneTask() {
        return getRunningTaskCountInner() > 0;
    }

    public ArrayList<String> getDownloadingList() {
        ArrayList<String> arrayList = new ArrayList<>();
        synchronized (this.mDownloadMap) {
            for (Map.Entry<String, Future<?>> entry : this.mDownloadMap.entrySet()) {
                Future<?> value = entry.getValue();
                if (value != null && !value.isCancelled() && !value.isDone()) {
                    arrayList.add(entry.getKey());
                }
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearCounter() {
        this.mTotalCount = 0;
    }

    private void wakeLockAcquire(String str) {
        if (!this.mWakeLock.isHeld()) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiDownloader", "wakeLockAcquire: reason= " + str);
            }
            this.mWakeLock.acquire();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wakeLockRelease(String str) {
        if (this.mWakeLock.isHeld() && this.mDownloadMap.size() == 0) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiDownloader", "wakeLockRelease: reason= " + str);
            }
            this.mWakeLock.release();
        }
    }
}
