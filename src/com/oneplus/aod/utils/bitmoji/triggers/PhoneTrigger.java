package com.oneplus.aod.utils.bitmoji.triggers;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioPlaybackConfiguration;
import android.os.Build;
import android.telecom.TelecomManager;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.triggers.PhoneTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.Trigger;
public class PhoneTrigger extends AudioTrigger {
    private int mPhoneState;

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String getCurrentCategory() {
        return "messaging";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public String getMdmLabel() {
        return "phone";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public int getPriority() {
        return 3;
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger
    public String getTriggerId() {
        return "messaging";
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger
    public String tagStartCountToCheckPlay() {
        return "Bitmoji#PhoneTrigger";
    }

    public PhoneTrigger(Context context, OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String[] getCategories() {
        return new String[]{"messaging"};
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger
    public boolean isAudioActive() {
        return this.mPlaybackStateActive.get();
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger
    public boolean checkAudioAttributes(AudioAttributes audioAttributes) {
        if (audioAttributes.getContentType() == 1 && audioAttributes.getUsage() == 2) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger
    public boolean inCondition() {
        TelecomManager from = TelecomManager.from(this.mContext);
        int mode = this.mAudioManager.getMode();
        return from.isInCall() || mode == 3 || mode == 2;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger
    public boolean checkConfigIsActive(AudioPlaybackConfiguration audioPlaybackConfiguration) {
        int mode = this.mAudioManager.getMode();
        boolean isActive = audioPlaybackConfiguration.isActive();
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "checkConfigIsActive: configActive=" + isActive + ", mode= " + mode);
        }
        return isActive || mode == 3 || mode == 2;
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
    public class UpdateMonitorCallback extends DelayTrigger.TriggerUpdateMonitorCallback {
        private UpdateMonitorCallback() {
            super();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onPhoneStateChanged(int i) {
            if (PhoneTrigger.this.mPhoneState != i) {
                PhoneTrigger.this.mPhoneState = i;
                if (PhoneTrigger.this.getHandler() != null) {
                    if (Build.DEBUG_ONEPLUS) {
                        String str = ((Trigger) PhoneTrigger.this).mTag;
                        Log.d(str, "onPhoneStateChanged: phoneState= " + i);
                    }
                    PhoneTrigger.this.getHandler().post(new Runnable(i == 2) { // from class: com.oneplus.aod.utils.bitmoji.triggers.-$$Lambda$PhoneTrigger$UpdateMonitorCallback$qXnYSJ-UVsbnxgzzDFfqUantRsE
                        public final /* synthetic */ boolean f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            PhoneTrigger.UpdateMonitorCallback.this.lambda$onPhoneStateChanged$0$PhoneTrigger$UpdateMonitorCallback(this.f$1);
                        }
                    });
                    return;
                }
                Log.e(((Trigger) PhoneTrigger.this).mTag, "onPhoneStateChanged: handler is null");
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onPhoneStateChanged$0 */
        public /* synthetic */ void lambda$onPhoneStateChanged$0$PhoneTrigger$UpdateMonitorCallback(boolean z) {
            PhoneTrigger phoneTrigger = PhoneTrigger.this;
            phoneTrigger.onTriggerChanged(phoneTrigger.getTriggerId(), z);
        }
    }
}
