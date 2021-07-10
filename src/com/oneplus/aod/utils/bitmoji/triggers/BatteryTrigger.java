package com.oneplus.aod.utils.bitmoji.triggers;

import android.content.Context;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.BatteryController;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: BatteryTrigger.kt */
public final class BatteryTrigger extends CategoryTrigger {
    private static final int BATTERY_LEVEL_MAX = 15;
    private static final int BATTERY_LEVEL_MIN = 0;
    private final BatteryTrigger$mBatteryCallback$1 mBatteryCallback = new BatteryTrigger$mBatteryCallback$1(this);
    private BatteryController mBatteryController = ((BatteryController) Dependency.get(BatteryController.class));
    private int mBatteryLevel = -1;
    private boolean mCharging;

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger, com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public int getPriority() {
        return 4;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public BatteryTrigger(@NotNull Context context, @NotNull OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(opBitmojiManager, "bitmojiManager");
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void init() {
        super.init();
        BatteryController batteryController = this.mBatteryController;
        if (batteryController != null) {
            batteryController.addCallback(this.mBatteryCallback);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    @Nullable
    public String[] getCategories() {
        return new String[]{"battery_low", "charging"};
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    @Nullable
    public String getCurrentCategory() {
        if (this.mCharging) {
            return "charging";
        }
        if (batteryLevelInRange(this.mBatteryLevel)) {
            return "battery_low";
        }
        return null;
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public boolean isActive() {
        return this.mCharging || batteryLevelInRange(this.mBatteryLevel);
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    @Nullable
    public String getMdmLabel() {
        String currentCategory = getCurrentCategory();
        if (currentCategory == null) {
            return null;
        }
        int hashCode = currentCategory.hashCode();
        if (hashCode != 1436115569) {
            if (hashCode == 2023666210 && currentCategory.equals("battery_low")) {
                return "battery_low";
            }
            return null;
        } else if (currentCategory.equals("charging")) {
            return "battery_charging";
        } else {
            return null;
        }
    }

    /* access modifiers changed from: private */
    public final boolean batteryLevelInRange(int i) {
        return BATTERY_LEVEL_MIN <= i && BATTERY_LEVEL_MAX >= i;
    }
}
