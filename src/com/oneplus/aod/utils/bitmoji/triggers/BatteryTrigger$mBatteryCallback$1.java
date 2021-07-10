package com.oneplus.aod.utils.bitmoji.triggers;

import com.android.systemui.statusbar.policy.BatteryController;
/* compiled from: BatteryTrigger.kt */
public final class BatteryTrigger$mBatteryCallback$1 implements BatteryController.BatteryStateChangeCallback {
    final /* synthetic */ BatteryTrigger this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    BatteryTrigger$mBatteryCallback$1(BatteryTrigger batteryTrigger) {
        this.this$0 = batteryTrigger;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        boolean z3;
        int i2 = this.this$0.mBatteryLevel;
        this.this$0.mBatteryLevel = i;
        if (!(this.this$0.mCharging) || !z2) {
            boolean z4 = true;
            boolean z5 = false;
            if (this.this$0.mCharging != z2) {
                this.this$0.mCharging = z2;
                z3 = true;
            } else {
                z3 = false;
            }
            if (!z2 && i2 != i) {
                if (this.this$0.batteryLevelInRange(i2) == this.this$0.batteryLevelInRange(i)) {
                    z4 = false;
                }
                z5 = z4;
            }
            if (z3 || z5) {
                this.this$0.getHandler().post(new Runnable(this, i) { // from class: com.oneplus.aod.utils.bitmoji.triggers.BatteryTrigger$mBatteryCallback$1$onBatteryLevelChanged$1
                    final /* synthetic */ int $level;
                    final /* synthetic */ BatteryTrigger$mBatteryCallback$1 this$0;

                    {
                        this.this$0 = r1;
                        this.$level = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        this.this$0.this$0.mBitmojiManager.onTriggerChanged("battery", false);
                        if ((this.this$0.this$0.mCharging) || (this.this$0.this$0.batteryLevelInRange(this.$level))) {
                            this.this$0.this$0.mBitmojiManager.onTriggerChanged("battery", true);
                        }
                    }
                });
            }
        }
    }
}
