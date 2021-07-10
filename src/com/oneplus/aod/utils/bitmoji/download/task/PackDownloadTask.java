package com.oneplus.aod.utils.bitmoji.download.task;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.android.systemui.Dependency;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.download.OpBitmojiDownloadManager;
import com.oneplus.aod.utils.bitmoji.download.item.Pack;
import com.oneplus.aod.utils.bitmoji.download.item.Sticker;
import com.oneplus.aod.utils.bitmoji.download.task.BaseDownloadTask;
import java.util.Iterator;
public class PackDownloadTask extends BaseDownloadTask {
    private Pack mPack;

    public PackDownloadTask(Context context, Handler handler, BaseDownloadTask.OnDownloadDoneListener onDownloadDoneListener, Pack pack) {
        super(context, handler, onDownloadDoneListener);
        this.mPack = pack;
    }

    @Override // java.util.concurrent.Callable
    public Boolean call() {
        Boolean bool = Boolean.FALSE;
        boolean z = true;
        try {
            String packId = this.mPack.getPackId();
            Iterator<Sticker> it = this.mPack.values().iterator();
            while (true) {
                if (it.hasNext()) {
                    Sticker next = it.next();
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    if (!saveImageToFile(next.getUri(), getBitmojiHelper().getImagePath(packId, next.getUri()))) {
                        z = false;
                    }
                } else if (!Thread.currentThread().isInterrupted()) {
                    if (z) {
                        this.mPack.setNeedsUpdate(false);
                        this.mPack.notNew();
                        try {
                            this.mPack.writeToFile(this.mContext);
                        } catch (Exception e) {
                            Log.e(this.mTag, "pack write to file fail" + packId, e);
                        }
                    }
                    ((OpBitmojiManager) Dependency.get(OpBitmojiManager.class)).onImagePackUpdate(OpBitmojiDownloadManager.getTriggerIdByPackId(packId), packId);
                    bool = Boolean.valueOf(z);
                }
            }
            return bool;
        } finally {
            onTaskDone();
            onDownloadDone(z);
        }
    }

    @Override // com.oneplus.aod.utils.bitmoji.download.task.BaseDownloadTask
    public String key() {
        return this.mPack.getPackId();
    }
}
