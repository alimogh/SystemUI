package com.android.systemui.statusbar.policy;

import com.android.systemui.C0006R$drawable;
import com.oneplus.systemui.statusbar.policy.OpWifiIcons;
public class WifiIcons extends OpWifiIcons {
    public static final int[][] QS_WIFI_4_SIGNAL_STRENGTH;
    public static final int[][] QS_WIFI_5_SIGNAL_STRENGTH;
    public static final int[][] QS_WIFI_6_SIGNAL_STRENGTH;
    public static final int QS_WIFI_DISABLED = C0006R$drawable.op_q_ic_qs_wifi_disabled;
    public static final int QS_WIFI_NO_NETWORK;
    public static final int[][] QS_WIFI_SIGNAL_STRENGTH;
    static final int[] WIFI_4_FULL_ICONS = {17302870, 17302871, 17302872, 17302873, 17302874};
    private static final int[] WIFI_4_NO_INTERNET_ICONS = {C0006R$drawable.ic_qs_wifi_4_0, C0006R$drawable.ic_qs_wifi_4_1, C0006R$drawable.ic_qs_wifi_4_2, C0006R$drawable.ic_qs_wifi_4_3, C0006R$drawable.ic_qs_wifi_4_4};
    static final int[][] WIFI_4_SIGNAL_STRENGTH;
    static final int[] WIFI_5_FULL_ICONS = {17302875, 17302876, 17302877, 17302878, 17302879};
    private static final int[] WIFI_5_NO_INTERNET_ICONS = {C0006R$drawable.ic_qs_wifi_5_0, C0006R$drawable.ic_qs_wifi_5_1, C0006R$drawable.ic_qs_wifi_5_2, C0006R$drawable.ic_qs_wifi_5_3, C0006R$drawable.ic_qs_wifi_5_4};
    static final int[][] WIFI_5_SIGNAL_STRENGTH;
    static final int[] WIFI_6_FULL_ICONS = {17302880, 17302881, 17302882, 17302883, 17302884};
    private static final int[] WIFI_6_NO_INTERNET_ICONS;
    static final int[][] WIFI_6_SIGNAL_STRENGTH;
    static final int[] WIFI_FULL_ICONS = {C0006R$drawable.op_q_stat_sys_wifi_signal_0_fully, C0006R$drawable.op_q_stat_sys_wifi_signal_1_fully, C0006R$drawable.op_q_stat_sys_wifi_signal_2_fully, C0006R$drawable.op_q_stat_sys_wifi_signal_3_fully, C0006R$drawable.op_q_stat_sys_wifi_signal_4_fully};
    static final int WIFI_LEVEL_COUNT = WIFI_SIGNAL_STRENGTH[0].length;
    private static final int[] WIFI_NO_INTERNET_ICONS = {C0006R$drawable.op_q_stat_sys_wifi_signal_0, C0006R$drawable.op_q_stat_sys_wifi_signal_1, C0006R$drawable.op_q_stat_sys_wifi_signal_2, C0006R$drawable.op_q_stat_sys_wifi_signal_3, C0006R$drawable.op_q_stat_sys_wifi_signal_4};
    static final int WIFI_NO_NETWORK;
    static final int[][] WIFI_SIGNAL_STRENGTH;

    static {
        int[] iArr = {C0006R$drawable.ic_qs_wifi_6_0, C0006R$drawable.ic_qs_wifi_6_1, C0006R$drawable.ic_qs_wifi_6_2, C0006R$drawable.ic_qs_wifi_6_3, C0006R$drawable.ic_qs_wifi_6_4};
        WIFI_6_NO_INTERNET_ICONS = iArr;
        int[][] iArr2 = {WIFI_NO_INTERNET_ICONS, WIFI_FULL_ICONS};
        QS_WIFI_SIGNAL_STRENGTH = iArr2;
        WIFI_SIGNAL_STRENGTH = iArr2;
        int[][] iArr3 = {WIFI_4_NO_INTERNET_ICONS, WIFI_4_FULL_ICONS};
        QS_WIFI_4_SIGNAL_STRENGTH = iArr3;
        WIFI_4_SIGNAL_STRENGTH = iArr3;
        int[][] iArr4 = {WIFI_5_NO_INTERNET_ICONS, WIFI_5_FULL_ICONS};
        QS_WIFI_5_SIGNAL_STRENGTH = iArr4;
        WIFI_5_SIGNAL_STRENGTH = iArr4;
        int[][] iArr5 = {iArr, WIFI_6_FULL_ICONS};
        QS_WIFI_6_SIGNAL_STRENGTH = iArr5;
        WIFI_6_SIGNAL_STRENGTH = iArr5;
        int i = C0006R$drawable.op_q_stat_sys_wifi_signal_0_fully;
        QS_WIFI_NO_NETWORK = i;
        WIFI_NO_NETWORK = i;
    }
}
