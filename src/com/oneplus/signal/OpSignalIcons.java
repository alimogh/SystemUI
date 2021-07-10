package com.oneplus.signal;

import com.android.systemui.C0006R$drawable;
public class OpSignalIcons {
    public static final int FOUR_G_LTE = C0006R$drawable.stat_sys_data_fully_connected_4g_lte;
    public static final int FOUR_G_PLUS_LTE = C0006R$drawable.stat_sys_data_fully_connected_4g_plus_lte;
    public static final int HD = C0006R$drawable.stat_sys_hd;
    public static final int[] HD_ICONS = {C0006R$drawable.stat_sys_hd_slot1, C0006R$drawable.stat_sys_hd_slot2, C0006R$drawable.stat_sys_hd_slot12};
    public static final int HD_UNAVAILABLE = C0006R$drawable.stat_sys_hd_unavailable;
    public static final int VOLTE = C0006R$drawable.stat_sys_volte;
    public static final int[] VOLTE_ICONS = {C0006R$drawable.stat_sys_volte_slot1, C0006R$drawable.stat_sys_volte_slot2, C0006R$drawable.stat_sys_volte_slot12};
    public static final int VOWIFI = C0006R$drawable.stat_sys_vowifi;
    public static final int[] VOWIFI_ICONS = {C0006R$drawable.stat_sys_vowifi_slot1, C0006R$drawable.stat_sys_vowifi_slot2, C0006R$drawable.stat_sys_vowifi_slot12};

    public static int getIdleDataIcon(long j) {
        if ((32843 & j) != 0 || (93108 & j) != 0) {
            return C0006R$drawable.op_stat_sys_data_idle_3g;
        }
        if ((397312 & j) != 0) {
            return C0006R$drawable.op_stat_sys_data_idle_4g_lte;
        }
        if ((j & 524288) != 0) {
            return C0006R$drawable.op_stat_sys_data_idle_5g_uwb;
        }
        return 0;
    }

    public static int getDisableDataIcon(long j) {
        if ((93108 & j) != 0) {
            return C0006R$drawable.stat_sys_data_disconnected_3g;
        }
        if ((397312 & j) == 0 && (j & 524288) == 0) {
            return C0006R$drawable.stat_sys_data_disabled;
        }
        return C0006R$drawable.stat_sys_data_disconnected_4g_lte;
    }
}
