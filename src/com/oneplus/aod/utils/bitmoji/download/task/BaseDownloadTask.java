package com.oneplus.aod.utils.bitmoji.download.task;

import android.content.Context;
import android.os.Handler;
import com.oneplus.aod.utils.bitmoji.OpBitmojiHelper;
import java.util.concurrent.Callable;
public abstract class BaseDownloadTask implements Callable<Boolean> {
    protected Context mContext;
    private boolean mForce;
    private Handler mHandler;
    private OnDownloadDoneListener mListener;
    protected final String mTag = getClass().getSimpleName();
    private OnTaskListener mTaskListener;

    public interface OnDownloadDoneListener {
        void onDownloadFail(String str);

        void onDownloadSuccess(String str);
    }

    public interface OnTaskListener {
        void onTaskFinished(String str);
    }

    public abstract String key();

    public BaseDownloadTask(Context context, Handler handler, OnDownloadDoneListener onDownloadDoneListener) {
        this.mContext = context;
        this.mHandler = handler;
        this.mListener = onDownloadDoneListener;
    }

    public void setForce(boolean z) {
        this.mForce = z;
    }

    public boolean isForce() {
        return this.mForce;
    }

    public void setTaskListener(OnTaskListener onTaskListener) {
        this.mTaskListener = onTaskListener;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0034 A[SYNTHETIC, Splitter:B:19:0x0034] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x003b A[SYNTHETIC, Splitter:B:23:0x003b] */
    /* JADX WARNING: Removed duplicated region for block: B:29:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean saveImageToFile(android.net.Uri r3, java.io.File r4) {
        /*
            r2 = this;
            r0 = 0
            android.content.Context r1 = r2.mContext     // Catch:{ Exception -> 0x002a }
            android.content.ContentResolver r1 = r1.getContentResolver()     // Catch:{ Exception -> 0x002a }
            android.graphics.ImageDecoder$Source r3 = android.graphics.ImageDecoder.createSource(r1, r3)     // Catch:{ Exception -> 0x002a }
            android.graphics.Bitmap r3 = android.graphics.ImageDecoder.decodeBitmap(r3)     // Catch:{ Exception -> 0x002a }
            java.io.FileOutputStream r1 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x002a }
            r1.<init>(r4)     // Catch:{ Exception -> 0x002a }
            android.graphics.Bitmap$CompressFormat r4 = android.graphics.Bitmap.CompressFormat.WEBP     // Catch:{ Exception -> 0x0025, all -> 0x0022 }
            r0 = 100
            r3.compress(r4, r0, r1)     // Catch:{ Exception -> 0x0025, all -> 0x0022 }
            boolean r2 = android.os.Build.DEBUG_ONEPLUS     // Catch:{ Exception -> 0x0025, all -> 0x0022 }
            r2 = 1
            r1.close()     // Catch:{ Exception -> 0x0021 }
        L_0x0021:
            return r2
        L_0x0022:
            r2 = move-exception
            r0 = r1
            goto L_0x0039
        L_0x0025:
            r3 = move-exception
            r0 = r1
            goto L_0x002b
        L_0x0028:
            r2 = move-exception
            goto L_0x0039
        L_0x002a:
            r3 = move-exception
        L_0x002b:
            java.lang.String r2 = r2.mTag     // Catch:{ all -> 0x0028 }
            java.lang.String r4 = "saveImageToFile: occur error"
            android.util.Log.e(r2, r4, r3)     // Catch:{ all -> 0x0028 }
            if (r0 == 0) goto L_0x0037
            r0.close()     // Catch:{ Exception -> 0x0037 }
        L_0x0037:
            r2 = 0
            return r2
        L_0x0039:
            if (r0 == 0) goto L_0x003e
            r0.close()     // Catch:{ Exception -> 0x003e }
        L_0x003e:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.utils.bitmoji.download.task.BaseDownloadTask.saveImageToFile(android.net.Uri, java.io.File):boolean");
    }

    /* access modifiers changed from: protected */
    public OpBitmojiHelper getBitmojiHelper() {
        return OpBitmojiHelper.getInstance();
    }

    /* access modifiers changed from: protected */
    public void onDownloadDone(boolean z) {
        if (this.mListener != null) {
            this.mHandler.post(new Runnable(z) { // from class: com.oneplus.aod.utils.bitmoji.download.task.-$$Lambda$BaseDownloadTask$zWjBvqHpvbBSKUjlYDosKonwZdc
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    BaseDownloadTask.this.lambda$onDownloadDone$0$BaseDownloadTask(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onDownloadDone$0 */
    public /* synthetic */ void lambda$onDownloadDone$0$BaseDownloadTask(boolean z) {
        if (z) {
            this.mListener.onDownloadSuccess(key());
        } else {
            this.mListener.onDownloadFail(key());
        }
    }

    /* access modifiers changed from: protected */
    public void onTaskDone() {
        OnTaskListener onTaskListener = this.mTaskListener;
        if (onTaskListener != null) {
            onTaskListener.onTaskFinished(key());
            this.mTaskListener = null;
        }
    }
}
