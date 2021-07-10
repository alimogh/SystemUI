package com.oneplus.util;

import com.android.systemui.C0006R$drawable;
public class OpBatteryUtils {
    public static int getDashImageResId(int i) {
        if (i == 0) {
            return C0006R$drawable.op_dash_charging_state_0;
        }
        if (i > 0 && i <= 10) {
            return C0006R$drawable.op_dash_charging_state_10;
        }
        if (i > 10 && i <= 20) {
            return C0006R$drawable.op_dash_charging_state_20;
        }
        if (i > 20 && i <= 30) {
            return C0006R$drawable.op_dash_charging_state_30;
        }
        if (i > 30 && i <= 40) {
            return C0006R$drawable.op_dash_charging_state_40;
        }
        if (i > 40 && i <= 50) {
            return C0006R$drawable.op_dash_charging_state_50;
        }
        if (i > 50 && i <= 60) {
            return C0006R$drawable.op_dash_charging_state_60;
        }
        if (i > 60 && i <= 70) {
            return C0006R$drawable.op_dash_charging_state_70;
        }
        if (i > 70 && i <= 80) {
            return C0006R$drawable.op_dash_charging_state_80;
        }
        if (i > 80 && i <= 90) {
            return C0006R$drawable.op_dash_charging_state_90;
        }
        if (i <= 90 || i > 100) {
            return 0;
        }
        return C0006R$drawable.op_dash_charging_state_100;
    }

    public static int getBatteryMaskInsideResId(int i) {
        int i2 = C0006R$drawable.op_battery_mask_0;
        if (i == 0) {
            return i2;
        }
        if (i > 0 && i <= 5) {
            return C0006R$drawable.op_battery_mask_5;
        }
        if (i > 5 && i <= 10) {
            return C0006R$drawable.op_battery_mask_10;
        }
        if (i > 10 && i <= 15) {
            return C0006R$drawable.op_battery_mask_15;
        }
        if (i > 15 && i <= 20) {
            return C0006R$drawable.op_battery_mask_20;
        }
        if (i > 20 && i <= 25) {
            return C0006R$drawable.op_battery_mask_25;
        }
        if (i > 25 && i <= 30) {
            return C0006R$drawable.op_battery_mask_30;
        }
        if (i > 30 && i <= 35) {
            return C0006R$drawable.op_battery_mask_35;
        }
        if (i > 35 && i <= 40) {
            return C0006R$drawable.op_battery_mask_40;
        }
        if (i > 40 && i <= 45) {
            return C0006R$drawable.op_battery_mask_45;
        }
        if (i > 45 && i <= 50) {
            return C0006R$drawable.op_battery_mask_50;
        }
        if (i > 50 && i <= 55) {
            return C0006R$drawable.op_battery_mask_55;
        }
        if (i > 55 && i <= 60) {
            return C0006R$drawable.op_battery_mask_60;
        }
        if (i > 60 && i <= 65) {
            return C0006R$drawable.op_battery_mask_65;
        }
        if (i > 65 && i <= 70) {
            return C0006R$drawable.op_battery_mask_70;
        }
        if (i > 70 && i <= 75) {
            return C0006R$drawable.op_battery_mask_75;
        }
        if (i > 75 && i <= 80) {
            return C0006R$drawable.op_battery_mask_80;
        }
        if (i > 80 && i <= 85) {
            return C0006R$drawable.op_battery_mask_85;
        }
        if (i > 85 && i <= 90) {
            return C0006R$drawable.op_battery_mask_90;
        }
        if (i <= 90 || i > 95) {
            return i > 95 ? C0006R$drawable.op_battery_mask_100 : i2;
        }
        return C0006R$drawable.op_battery_mask_95;
    }
}
