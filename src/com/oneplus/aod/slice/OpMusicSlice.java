package com.oneplus.aod.slice;

import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaSessionManager;
import android.util.Log;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.oneplus.aod.slice.OpSliceManager;
public class OpMusicSlice extends OpSlice implements NotificationMediaManager.MediaListener {
    private boolean mIsPlaying = false;
    private MediaMetadata mMediaMetadata = null;
    private final NotificationMediaManager mNotificationMediaManager;

    public OpMusicSlice(Context context, OpSliceManager.Callback callback) {
        super(callback);
        this.mIcon = C0006R$drawable.op_aod_slice_music;
        MediaSessionManager mediaSessionManager = (MediaSessionManager) context.getSystemService("media_session");
        this.mNotificationMediaManager = (NotificationMediaManager) Dependency.get(NotificationMediaManager.class);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.slice.OpSlice
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (OpSlice.DEBUG) {
            String str = this.mTag;
            Log.i(str, "handleSetListening = " + z + ", mNotificationMediaManager = " + this.mNotificationMediaManager);
        }
        if (z) {
            NotificationMediaManager notificationMediaManager = this.mNotificationMediaManager;
            if (notificationMediaManager != null) {
                this.mMediaMetadata = notificationMediaManager.getMediaMetadata();
                this.mNotificationMediaManager.addCallback(this);
                return;
            }
            return;
        }
        NotificationMediaManager notificationMediaManager2 = this.mNotificationMediaManager;
        if (notificationMediaManager2 != null) {
            notificationMediaManager2.removeCallback(this);
        }
    }

    private void updateInfo() {
        MediaMetadata mediaMetadata = this.mMediaMetadata;
        if (mediaMetadata != null) {
            this.mPrimary = mediaMetadata.getString("android.media.metadata.TITLE");
            this.mSecondary = this.mMediaMetadata.getString("android.media.metadata.ARTIST");
            if (OpSlice.DEBUG) {
                String str = this.mTag;
                Log.i(str, "updateInfo: primary = " + this.mPrimary + ", secondary = " + this.mSecondary + ", playing = " + this.mIsPlaying);
            }
            if (this.mPrimary == null) {
                this.mPrimary = "Unknow";
            }
            String str2 = this.mSecondary;
            if (str2 == null || str2.trim().length() == 0) {
                this.mSecondary = "Unknown artist";
            }
            setActive(this.mIsPlaying);
            updateUI();
            return;
        }
        setActive(false);
    }

    @Override // com.android.systemui.statusbar.NotificationMediaManager.MediaListener
    public void onPrimaryMetadataOrStateChanged(MediaMetadata mediaMetadata, int i) {
        this.mMediaMetadata = mediaMetadata;
        this.mIsPlaying = i == 3 || i == 6;
        updateInfo();
    }
}
