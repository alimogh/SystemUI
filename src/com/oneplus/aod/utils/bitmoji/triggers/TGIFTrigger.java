package com.oneplus.aod.utils.bitmoji.triggers;

import android.content.Context;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger;
import java.util.Calendar;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: TGIFTrigger.kt */
public final class TGIFTrigger extends CategoryTrigger {
    private final TGIFTrigger$mCallback$1 mCallback = new TGIFTrigger$mCallback$1(this);
    private boolean mInTGIF;

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    @NotNull
    public String getCurrentCategory() {
        return "tgif";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    @NotNull
    public String getMdmLabel() {
        return "tgif";
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public int getPriority() {
        return 2;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public TGIFTrigger(@NotNull Context context, @NotNull OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(opBitmojiManager, "bitmojiManager");
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void init() {
        getKeyguardUpdateMonitor().registerCallback(this.mCallback);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    @Nullable
    public String[] getCategories() {
        return new String[]{"tgif"};
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public boolean isActive() {
        return this.mInTGIF;
    }

    /* access modifiers changed from: private */
    public final boolean inTGIFTime() {
        int i;
        Calendar instance = Calendar.getInstance();
        if (instance.get(7) != 6 || (i = instance.get(11)) < 18 || i >= 24) {
            return false;
        }
        return true;
    }
}
