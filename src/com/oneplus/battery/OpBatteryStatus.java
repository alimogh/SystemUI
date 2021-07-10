package com.oneplus.battery;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.settingslib.fuelgauge.BatteryStatus;
import com.android.systemui.C0009R$integer;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.util.ProductUtils;
public class OpBatteryStatus extends BatteryStatus {
    public int fastCharge;
    public boolean mProtectCharging;
    public long mTimeToFull;
    public boolean pdcharge;
    public boolean wirelessCharging;
    public boolean wirelessChargingDeviated;
    public boolean wirelessWarpCharging;

    public OpBatteryStatus(int i, int i2, int i3, int i4, int i5, int i6, boolean z, boolean z2, boolean z3, boolean z4) {
        super(i, i2, i3, i4, i5);
        this.mTimeToFull = -1;
        this.mProtectCharging = false;
        this.fastCharge = i6;
        this.wirelessCharging = z;
        this.wirelessWarpCharging = z2;
        this.wirelessChargingDeviated = z3;
        this.pdcharge = z4;
        this.mProtectCharging = false;
    }

    public OpBatteryStatus(Intent intent, int i, boolean z, boolean z2, boolean z3, boolean z4) {
        this(intent);
        this.fastCharge = i;
        this.wirelessCharging = z;
        this.wirelessWarpCharging = z2;
        this.wirelessChargingDeviated = z3;
        this.pdcharge = z4;
        if (intent.getIntExtra("fastcharge_status", 0) != 0) {
            long longExtra = intent.getLongExtra("estimate_time_to_full", 0);
            Log.d("OpBatteryStatus", "OpBatteryStatus TimeToFull " + longExtra);
            if (longExtra != 0) {
                this.mTimeToFull = longExtra;
            }
            this.mProtectCharging = intent.getBooleanExtra("protect_charging", false);
            return;
        }
        this.mTimeToFull = -1;
    }

    private OpBatteryStatus(Intent intent) {
        super(intent);
        this.mTimeToFull = -1;
        this.mProtectCharging = false;
    }

    public int getChargingSpeed(Context context) {
        if (((BatteryController) Dependency.get(BatteryController.class)).isFastCharging(this.fastCharge)) {
            return 2;
        }
        if (((BatteryController) Dependency.get(BatteryController.class)).isWarpCharging(this.fastCharge)) {
            return 3;
        }
        if (this.wirelessCharging && this.wirelessWarpCharging) {
            return 4;
        }
        int integer = context.getResources().getInteger(C0009R$integer.config_chargingSlowlyThreshold);
        context.getResources().getInteger(C0009R$integer.config_chargingFastThreshold);
        if (!ProductUtils.isUsvMode()) {
            return this.maxChargingWattage <= 0 ? -1 : 1;
        }
        int i = this.maxChargingWattage;
        if (i <= 0) {
            return -1;
        }
        if (i < integer) {
            return 0;
        }
        return 1;
    }

    @Override // com.android.settingslib.fuelgauge.BatteryStatus
    public String toString() {
        return "OpBatteryStatus{status=" + this.status + ",level=" + this.level + ",plugged=" + this.plugged + ",health=" + this.health + ",maxChargingWattage=" + this.maxChargingWattage + "}, fastCharge:" + this.fastCharge + ", wirelessCharging:" + this.wirelessCharging + ", wirelessWarpCharging:" + this.wirelessWarpCharging + ", wirelessChargingDeviated:" + this.wirelessChargingDeviated + ", pdcharge:" + this.pdcharge;
    }

    public boolean isWirelessChargingDeviated() {
        return this.wirelessChargingDeviated;
    }

    public boolean isPdCharging() {
        return this.pdcharge;
    }

    public boolean isProtectCharging() {
        return this.mProtectCharging;
    }

    public long getSwarpRemainingTime() {
        return this.mTimeToFull;
    }
}
