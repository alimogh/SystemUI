package com.oneplus.aod.utils.bitmoji.triggers;

import com.android.keyguard.KeyguardUpdateMonitorCallback;
/* compiled from: TGIFTrigger.kt */
public final class TGIFTrigger$mCallback$1 extends KeyguardUpdateMonitorCallback {
    final /* synthetic */ TGIFTrigger this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    TGIFTrigger$mCallback$1(TGIFTrigger tGIFTrigger) {
        this.this$0 = tGIFTrigger;
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onTimeChanged() {
        boolean z = this.this$0.inTGIFTime();
        if (this.this$0.mInTGIF != z) {
            this.this$0.mInTGIF = z;
            this.this$0.getHandler().post(new Runnable(this, z) { // from class: com.oneplus.aod.utils.bitmoji.triggers.TGIFTrigger$mCallback$1$onTimeChanged$1
                final /* synthetic */ boolean $inTGIFTime;
                final /* synthetic */ TGIFTrigger$mCallback$1 this$0;

                {
                    this.this$0 = r1;
                    this.$inTGIFTime = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    this.this$0.this$0.mBitmojiManager.onTriggerChanged("tgif", this.$inTGIFTime);
                }
            });
        }
    }
}
