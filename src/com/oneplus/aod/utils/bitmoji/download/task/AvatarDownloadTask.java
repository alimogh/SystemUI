package com.oneplus.aod.utils.bitmoji.download.task;

import android.content.Context;
import android.os.Handler;
import com.oneplus.aod.utils.bitmoji.download.item.Avatar;
import com.oneplus.aod.utils.bitmoji.download.task.BaseDownloadTask;
import com.oneplus.systemui.OpSystemUIProvider;
public class AvatarDownloadTask extends BaseDownloadTask {
    private Avatar mAvatar;

    @Override // com.oneplus.aod.utils.bitmoji.download.task.BaseDownloadTask
    public String key() {
        return "avatar";
    }

    public AvatarDownloadTask(Context context, Handler handler, BaseDownloadTask.OnDownloadDoneListener onDownloadDoneListener, Avatar avatar) {
        super(context, handler, onDownloadDoneListener);
        this.mAvatar = avatar;
    }

    @Override // java.util.concurrent.Callable
    public Boolean call() {
        boolean z;
        Throwable th;
        Boolean valueOf;
        try {
            z = saveImageToFile(getBitmojiHelper().getSelfieUri(), getBitmojiHelper().getAvatarFile());
            try {
                if (Thread.currentThread().isInterrupted()) {
                    valueOf = Boolean.FALSE;
                } else {
                    if (z) {
                        this.mAvatar.setNeedsUpdate(false);
                        this.mAvatar.writeToFile(this.mContext);
                        OpSystemUIProvider.notifyAvatarUpdate(this.mContext);
                    }
                    valueOf = Boolean.valueOf(z);
                }
                onTaskDone();
                onDownloadDone(z);
                return valueOf;
            } catch (Throwable th2) {
                th = th2;
                onTaskDone();
                onDownloadDone(z);
                throw th;
            }
        } catch (Throwable th3) {
            z = false;
            th = th3;
            onTaskDone();
            onDownloadDone(z);
            throw th;
        }
    }
}
