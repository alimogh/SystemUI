package com.oneplus.aod.utils.bitmoji.triggers;

import android.content.Context;
import android.media.AudioAttributes;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger;
public class MusicTrigger extends AudioTrigger {
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger
    public boolean enableDelay() {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String getCurrentCategory() {
        return "tunes";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public String getMdmLabel() {
        return "music";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger
    public String getTriggerId() {
        return "music";
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger
    public String tagStartCountToCheckPlay() {
        return "Bitmoji#MusicTrigger";
    }

    public MusicTrigger(Context context, OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String[] getCategories() {
        return new String[]{"tunes"};
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.AudioTrigger
    public boolean checkAudioAttributes(AudioAttributes audioAttributes) {
        if (audioAttributes.getContentType() == 2 && audioAttributes.getUsage() == 1) {
            return true;
        }
        return false;
    }
}
