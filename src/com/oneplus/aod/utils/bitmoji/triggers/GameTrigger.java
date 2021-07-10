package com.oneplus.aod.utils.bitmoji.triggers;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.Trigger;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
public class GameTrigger extends CategoryTrigger {
    private GameObserver mObserver;

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String getCurrentCategory() {
        return "gaming";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public String getMdmLabel() {
        return "game";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public int getPriority() {
        return 3;
    }

    public GameTrigger(Context context, OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
        this.mObserver = new GameObserver(opBitmojiManager.getHandler());
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void init() {
        super.init();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("game_mode_status"), false, this.mObserver);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String[] getCategories() {
        return new String[]{"gaming"};
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public boolean isActive() {
        return this.mObserver.isGameModeOn();
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void dumpDetail(FileDescriptor fileDescriptor, IndentingPrintWriter indentingPrintWriter, String[] strArr) {
        indentingPrintWriter.println("isGameModeOn=" + this.mObserver.isGameModeOn());
    }

    private class GameObserver extends ContentObserver {
        private boolean mIsGameModeOn;

        public GameObserver(Handler handler) {
            super(handler);
            onChange(true);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            boolean isGameModeOn = OpUtils.isGameModeOn(((Trigger) GameTrigger.this).mContext);
            if (this.mIsGameModeOn != isGameModeOn) {
                if (Build.DEBUG_ONEPLUS) {
                    String str = ((Trigger) GameTrigger.this).mTag;
                    Log.d(str, "GameMode change: " + isGameModeOn);
                }
                this.mIsGameModeOn = isGameModeOn;
                ((Trigger) GameTrigger.this).mBitmojiManager.onTriggerChanged("gaming", this.mIsGameModeOn);
            }
        }

        public boolean isGameModeOn() {
            return this.mIsGameModeOn;
        }
    }
}
