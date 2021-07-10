package com.oneplus.aod.utils.bitmoji.triggers;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioPlaybackConfiguration;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0001R$array;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.triggers.VideoTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.Trigger;
import java.util.ArrayList;
import java.util.List;
public class VideoTrigger extends AudioTrigger {
    private long mPlayStart2 = -1;
    private boolean mUsing;
    private ArrayList<String> mWhitelist = new ArrayList<>();

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String getCurrentCategory() {
        return "watching";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public String getMdmLabel() {
        return "video";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger
    public String getTriggerId() {
        return "video";
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger
    public String tagStartCountToCheckPlay() {
        return "Bitmoji#VideoTrigger";
    }

    public VideoTrigger(Context context, OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
        String[] stringArray = context.getResources().getStringArray(C0001R$array.op_bitmoji_video_whitelist);
        if (stringArray != null && stringArray.length > 0) {
            for (String str : stringArray) {
                this.mWhitelist.add(str);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String[] getCategories() {
        return new String[]{"watching"};
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger
    public boolean checkAudioAttributes(AudioAttributes audioAttributes) {
        if (audioAttributes.getContentType() == 3 && audioAttributes.getUsage() == 1) {
            return true;
        }
        return false;
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger
    public boolean isActiveInner() {
        return this.mPlayStart != -1 ? SystemClock.elapsedRealtime() - this.mPlayStart >= ((long) this.mDuration) : this.mPlayStart2 != -1;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger
    public boolean inCondition() {
        List<AudioPlaybackConfiguration> activePlaybackConfigurations = this.mAudioManager.getActivePlaybackConfigurations();
        if (activePlaybackConfigurations == null) {
            return false;
        }
        for (AudioPlaybackConfiguration audioPlaybackConfiguration : activePlaybackConfigurations) {
            if (checkAudioAttributes(audioPlaybackConfiguration.getAudioAttributes()) && audioPlaybackConfiguration.isActive()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger
    public KeyguardUpdateMonitorCallback getUpdateMonitorCallback() {
        if (this.mCallback == null) {
            this.mCallback = new UpdateMonitorCallback();
        }
        return this.mCallback;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean whitelist(String str) {
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        return this.mWhitelist.contains(str);
    }

    /* access modifiers changed from: private */
    public class UpdateMonitorCallback extends DelayTrigger.TriggerUpdateMonitorCallback {
        private UpdateMonitorCallback() {
            super();
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onVideoChanged(String str, boolean z) {
            if (VideoTrigger.this.mUsing != z) {
                if (Build.DEBUG_ONEPLUS) {
                    String str2 = ((Trigger) VideoTrigger.this).mTag;
                    Log.d(str2, "onVideoChanged: packageName= " + str + ", using= " + z);
                }
                if (!z || VideoTrigger.this.whitelist(str)) {
                    VideoTrigger.this.mUsing = z;
                    VideoTrigger.this.mPlayStart2 = z ? SystemClock.elapsedRealtime() : -1;
                    VideoTrigger.this.getHandler().post(new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.triggers.-$$Lambda$VideoTrigger$UpdateMonitorCallback$71usmY14uCDs89PhnTDc1xhKUX4
                        @Override // java.lang.Runnable
                        public final void run() {
                            VideoTrigger.UpdateMonitorCallback.this.lambda$onVideoChanged$0$VideoTrigger$UpdateMonitorCallback();
                        }
                    });
                } else if (Build.DEBUG_ONEPLUS) {
                    Log.d(((Trigger) VideoTrigger.this).mTag, "onVideoChanged: not in whitelist");
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onVideoChanged$0 */
        public /* synthetic */ void lambda$onVideoChanged$0$VideoTrigger$UpdateMonitorCallback() {
            VideoTrigger videoTrigger = VideoTrigger.this;
            videoTrigger.onTriggerChanged(videoTrigger.getTriggerId(), VideoTrigger.this.mUsing);
        }
    }
}
