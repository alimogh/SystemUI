package com.android.settingslib.fuelgauge;

import android.content.Intent;
public class BatteryStatus {
    public final int health;
    public final int level;
    public final int maxChargingWattage;
    public final int plugged;
    public final int status;

    public BatteryStatus(int i, int i2, int i3, int i4, int i5) {
        this.status = i;
        this.level = i2;
        this.plugged = i3;
        this.health = i4;
        this.maxChargingWattage = i5;
    }

    public BatteryStatus(Intent intent) {
        this.status = intent.getIntExtra("status", 1);
        this.plugged = intent.getIntExtra("plugged", 0);
        this.level = intent.getIntExtra("level", 0);
        this.health = intent.getIntExtra("health", 1);
        int intExtra = intent.getIntExtra("max_charging_current", -1);
        int intExtra2 = intent.getIntExtra("max_charging_voltage", -1);
        intExtra2 = intExtra2 <= 0 ? 5000000 : intExtra2;
        if (intExtra > 0) {
            this.maxChargingWattage = (intExtra / 1000) * (intExtra2 / 1000);
        } else {
            this.maxChargingWattage = -1;
        }
    }

    public boolean isPluggedIn() {
        int i = this.plugged;
        return i == 1 || i == 2 || i == 4;
    }

    public boolean isPluggedInWired() {
        int i = this.plugged;
        return i == 1 || i == 2;
    }

    public boolean isCharged() {
        return this.status == 5 || this.level >= 100;
    }

    public String toString() {
        return "BatteryStatus{status=" + this.status + ",level=" + this.level + ",plugged=" + this.plugged + ",health=" + this.health + ",maxChargingWattage=" + this.maxChargingWattage + "}";
    }
}
