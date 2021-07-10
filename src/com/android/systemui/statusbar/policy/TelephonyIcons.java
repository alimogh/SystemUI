package com.android.systemui.statusbar.policy;

import android.util.Log;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.statusbar.policy.MobileSignalController;
import com.oneplus.signal.OpSignalIcons;
import com.oneplus.util.OpUtils;
import java.util.HashMap;
import java.util.Map;
/* access modifiers changed from: package-private */
public class TelephonyIcons {
    static final MobileSignalController.MobileIconGroup CARRIER_NETWORK_CHANGE;
    static final MobileSignalController.MobileIconGroup DATA_DISABLED;
    static final MobileSignalController.MobileIconGroup E;
    static final MobileSignalController.MobileIconGroup FIVE_G_BASIC;
    static final MobileSignalController.MobileIconGroup FIVE_G_SA;
    static final MobileSignalController.MobileIconGroup FIVE_G_UWB;
    static final int FLIGHT_MODE_ICON = C0006R$drawable.stat_sys_airplane_mode;
    static final MobileSignalController.MobileIconGroup FOUR_FIVE_G;
    static final MobileSignalController.MobileIconGroup FOUR_G;
    static final MobileSignalController.MobileIconGroup FOUR_G_LTE;
    static final MobileSignalController.MobileIconGroup FOUR_G_PLUS;
    static final MobileSignalController.MobileIconGroup G;
    static final MobileSignalController.MobileIconGroup H;
    static final MobileSignalController.MobileIconGroup H_PLUS;
    static final int ICON_1X = C0006R$drawable.stat_sys_data_fully_connected_1x;
    static final int ICON_2G = C0006R$drawable.stat_sys_data_fully_connected_2g;
    static final int ICON_3G = C0006R$drawable.stat_sys_data_fully_connected_3g;
    static final int ICON_3G_PLUS = C0006R$drawable.stat_sys_data_fully_connected_3g_plus;
    static final int ICON_4G = C0006R$drawable.stat_sys_data_fully_connected_4g;
    static final int ICON_4G_PLUS = C0006R$drawable.stat_sys_data_fully_connected_4g_plus;
    static final int ICON_4_5G = C0006R$drawable.stat_sys_data_fully_connected_4_5g;
    static final int ICON_5G;
    static final int ICON_5G_BASIC;
    static final int ICON_5G_E = C0006R$drawable.ic_5g_e_mobiledata;
    static final int ICON_5G_PLUS = C0006R$drawable.ic_5g_plus_mobiledata;
    static final int ICON_5G_SA;
    static final int ICON_5G_UWB = C0006R$drawable.stat_sys_data_fully_connected_5g_uwb;
    static final int ICON_5G_X = C0006R$drawable.stat_sys_data_fully_connected_5gx_mobiledata;
    static final int ICON_DATA_DISABLED = C0006R$drawable.stat_sys_data_disabled;
    static final int ICON_E = C0006R$drawable.stat_sys_data_fully_connected_e;
    static final int ICON_G = C0006R$drawable.stat_sys_data_fully_connected_g;
    static final int ICON_H = C0006R$drawable.stat_sys_data_fully_connected_h;
    static final int ICON_H_PLUS = C0006R$drawable.stat_sys_data_fully_connected_h_plus;
    static final int ICON_LTE = C0006R$drawable.stat_sys_data_fully_connected_lte;
    static final int ICON_LTE_PLUS = C0006R$drawable.stat_sys_data_fully_connected_lte_plus;
    static final Map<String, MobileSignalController.MobileIconGroup> ICON_NAME_TO_ICON;
    static final int ICON_VOWIFI = C0006R$drawable.ic_vowifi;
    static final int ICON_VOWIFI_CALLING = C0006R$drawable.ic_vowifi_calling;
    static final MobileSignalController.MobileIconGroup LTE;
    static final MobileSignalController.MobileIconGroup LTE_CA_5G_E;
    static final MobileSignalController.MobileIconGroup LTE_PLUS;
    static final MobileSignalController.MobileIconGroup NOT_DEFAULT_DATA;
    static final MobileSignalController.MobileIconGroup NR_5G;
    static final MobileSignalController.MobileIconGroup NR_5G_PLUS;
    static final MobileSignalController.MobileIconGroup NR_5G_X;
    static final int[] ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_ROAMING = (OpUtils.isSupportFiveBar() ? ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_ROAMING_FIVE_BAR : ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_ROAMING_FOUR_BAR);
    static final int[] ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_ROAMING_FIVE_BAR = {C0006R$drawable.stat_sys_signal_0_op_5_bar_roam, C0006R$drawable.stat_sys_signal_1_op_5_bar_roam, C0006R$drawable.stat_sys_signal_2_op_5_bar_roam, C0006R$drawable.stat_sys_signal_3_op_5_bar_roam, C0006R$drawable.stat_sys_signal_4_op_5_bar_roam, C0006R$drawable.stat_sys_signal_5_op_5_bar_roam};
    static final int[] ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_ROAMING_FOUR_BAR = {C0006R$drawable.stat_sys_signal_oneplus_roam_0, C0006R$drawable.stat_sys_signal_oneplus_roam_1, C0006R$drawable.stat_sys_signal_oneplus_roam_2, C0006R$drawable.stat_sys_signal_oneplus_roam_3, C0006R$drawable.stat_sys_signal_oneplus_roam_4};
    static final int[] ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_VIRTUAL = (OpUtils.isSupportFiveBar() ? ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_VIRTUAL_FIVE_BAR : ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_VIRTUAL_FOUR_BAR);
    static final int[] ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_VIRTUAL_FIVE_BAR = {C0006R$drawable.stat_sys_signal_0_op_5_bar_virtual, C0006R$drawable.stat_sys_signal_1_op_5_bar_virtual, C0006R$drawable.stat_sys_signal_2_op_5_bar_virtual, C0006R$drawable.stat_sys_signal_3_op_5_bar_virtual, C0006R$drawable.stat_sys_signal_4_op_5_bar_virtual, C0006R$drawable.stat_sys_signal_5_op_5_bar_virtual};
    static final int[] ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_VIRTUAL_FOUR_BAR = {C0006R$drawable.stat_sys_signal_0_op_4_bar_virtual, C0006R$drawable.stat_sys_signal_1_op_4_bar_virtual, C0006R$drawable.stat_sys_signal_2_op_4_bar_virtual, C0006R$drawable.stat_sys_signal_3_op_4_bar_virtual, C0006R$drawable.stat_sys_signal_4_op_4_bar_virtual};
    static final MobileSignalController.MobileIconGroup ONE_X;
    static final int STACKED_ICON_1X = C0006R$drawable.stat_sys_data_op_stacked_1x;
    static final int STACKED_ICON_2G = C0006R$drawable.stat_sys_data_op_stacked_2g;
    static final int STACKED_ICON_3G = C0006R$drawable.stat_sys_data_op_stacked_3g;
    static final int STACKED_ICON_4G = C0006R$drawable.stat_sys_data_op_stacked_4g;
    static final int STACKED_ICON_4G_PLUS = C0006R$drawable.stat_sys_data_op_stacked_4g_plus;
    static final int STACKED_ICON_G = C0006R$drawable.stat_sys_data_op_stacked_g;
    static final int STACKED_ICON_LTE = C0006R$drawable.stat_sys_data_op_stacked_lte;
    static final int STACKED_ICON_LTE_PLUS = C0006R$drawable.stat_sys_data_op_stacked_lte_plus;
    static final int STACKED_ICON_ROAM = C0006R$drawable.stat_sys_data_op_stacked_roam;
    static final int[] STACKED_STRENGTH_ICONS = (OpUtils.isSupportFiveBar() ? STACKED_STRENGTH_ICONS_FIVE_BAR : STACKED_STRENGTH_ICONS_FOUR_BAR);
    static final int[] STACKED_STRENGTH_ICONS_FIVE_BAR = {C0006R$drawable.stat_sys_signal_0_op_5_bar_stacked, C0006R$drawable.stat_sys_signal_1_op_5_bar_stacked, C0006R$drawable.stat_sys_signal_2_op_5_bar_stacked, C0006R$drawable.stat_sys_signal_3_op_5_bar_stacked, C0006R$drawable.stat_sys_signal_4_op_5_bar_stacked, C0006R$drawable.stat_sys_signal_5_op_5_bar_stacked};
    static final int[] STACKED_STRENGTH_ICONS_FOUR_BAR = {C0006R$drawable.stat_sys_signal_0_op_4_bar_stacked, C0006R$drawable.stat_sys_signal_1_op_4_bar_stacked, C0006R$drawable.stat_sys_signal_2_op_4_bar_stacked, C0006R$drawable.stat_sys_signal_3_op_4_bar_stacked, C0006R$drawable.stat_sys_signal_4_op_4_bar_stacked};
    static final int TELEPHONY_NO_NETWORK = C0006R$drawable.stat_sys_signal_null;
    static final int[][] TELEPHONY_SIGNAL_STRENGTH = (OpUtils.isSupportFiveBar() ? TELEPHONY_SIGNAL_STRENGTH_FIVE_BAR : TELEPHONY_SIGNAL_STRENGTH_FOUR_BAR);
    static final int[][] TELEPHONY_SIGNAL_STRENGTH_FIVE_BAR = {new int[]{C0006R$drawable.stat_sys_signal_0_op_5_bar, C0006R$drawable.stat_sys_signal_1_op_5_bar, C0006R$drawable.stat_sys_signal_2_op_5_bar, C0006R$drawable.stat_sys_signal_3_op_5_bar, C0006R$drawable.stat_sys_signal_4_op_5_bar, C0006R$drawable.stat_sys_signal_5_op_5_bar}, new int[]{C0006R$drawable.stat_sys_signal_0_op_5_bar_fully, C0006R$drawable.stat_sys_signal_1_op_5_bar_fully, C0006R$drawable.stat_sys_signal_2_op_5_bar_fully, C0006R$drawable.stat_sys_signal_3_op_5_bar_fully, C0006R$drawable.stat_sys_signal_4_op_5_bar_fully, C0006R$drawable.stat_sys_signal_5_op_5_bar_fully}};
    static final int[][] TELEPHONY_SIGNAL_STRENGTH_FOUR_BAR = {new int[]{C0006R$drawable.stat_sys_signal_0, C0006R$drawable.stat_sys_signal_1, C0006R$drawable.stat_sys_signal_2, C0006R$drawable.stat_sys_signal_3, C0006R$drawable.stat_sys_signal_4}, new int[]{C0006R$drawable.stat_sys_signal_0_fully, C0006R$drawable.stat_sys_signal_1_fully, C0006R$drawable.stat_sys_signal_2_fully, C0006R$drawable.stat_sys_signal_3_fully, C0006R$drawable.stat_sys_signal_4_fully}};
    static final MobileSignalController.MobileIconGroup THREE_G;
    static final MobileSignalController.MobileIconGroup THREE_G_PLUS;
    static final MobileSignalController.MobileIconGroup TWO_G;
    static final MobileSignalController.MobileIconGroup UNKNOWN;
    static final MobileSignalController.MobileIconGroup VOWIFI;
    static final MobileSignalController.MobileIconGroup VOWIFI_CALLING;
    static final MobileSignalController.MobileIconGroup WFC;

    static {
        int i = C0006R$drawable.stat_sys_data_fully_connected_5g;
        ICON_5G = i;
        ICON_5G_SA = i;
        ICON_5G_BASIC = i;
        int[] iArr = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        CARRIER_NETWORK_CHANGE = new MobileSignalController.MobileIconGroup("CARRIER_NETWORK_CHANGE", null, null, iArr, 0, 0, 0, 0, iArr[0], C0015R$string.carrier_network_change_mode, 0, false);
        int[] iArr2 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        THREE_G = new MobileSignalController.MobileIconGroup("3G", null, null, iArr2, 0, 0, 0, 0, iArr2[0], C0015R$string.data_connection_3g, ICON_3G, true);
        int[] iArr3 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        WFC = new MobileSignalController.MobileIconGroup("WFC", null, null, iArr3, 0, 0, 0, 0, iArr3[0], 0, 0, false);
        int[] iArr4 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        UNKNOWN = new MobileSignalController.MobileIconGroup("Unknown", null, null, iArr4, 0, 0, 0, 0, iArr4[0], 0, 0, false);
        int[] iArr5 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        E = new MobileSignalController.MobileIconGroup("E", null, null, iArr5, 0, 0, 0, 0, iArr5[0], C0015R$string.data_connection_edge, ICON_E, false);
        int[] iArr6 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        ONE_X = new MobileSignalController.MobileIconGroup("1X", null, null, iArr6, 0, 0, 0, 0, iArr6[0], C0015R$string.data_connection_cdma, ICON_1X, true);
        int[] iArr7 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        G = new MobileSignalController.MobileIconGroup("G", null, null, iArr7, 0, 0, 0, 0, iArr7[0], C0015R$string.data_connection_gprs, ICON_G, false);
        int[] iArr8 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        H = new MobileSignalController.MobileIconGroup("H", null, null, iArr8, 0, 0, 0, 0, iArr8[0], C0015R$string.data_connection_3_5g, ICON_H, false);
        int[] iArr9 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        H_PLUS = new MobileSignalController.MobileIconGroup("H+", null, null, iArr9, 0, 0, 0, 0, iArr9[0], C0015R$string.data_connection_3_5g_plus, ICON_H_PLUS, false);
        int[] iArr10 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FOUR_G = new MobileSignalController.MobileIconGroup("4G", null, null, iArr10, 0, 0, 0, 0, iArr10[0], C0015R$string.data_connection_4g, ICON_4G, true);
        int[] iArr11 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FOUR_G_PLUS = new MobileSignalController.MobileIconGroup("4G+", null, null, iArr11, 0, 0, 0, 0, iArr11[0], C0015R$string.data_connection_4g_plus, ICON_4G_PLUS, true);
        int[] iArr12 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FOUR_FIVE_G = new MobileSignalController.MobileIconGroup("4.5G", null, null, iArr12, 0, 0, 0, 0, iArr12[0], C0015R$string.data_connection_4_5g, ICON_4_5G, true);
        int[] iArr13 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        LTE = new MobileSignalController.MobileIconGroup("LTE", null, null, iArr13, 0, 0, 0, 0, iArr13[0], C0015R$string.data_connection_lte, ICON_LTE, true);
        int[] iArr14 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        LTE_PLUS = new MobileSignalController.MobileIconGroup("LTE+", null, null, iArr14, 0, 0, 0, 0, iArr14[0], C0015R$string.data_connection_lte_plus, ICON_LTE_PLUS, true);
        int[] iArr15 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        LTE_CA_5G_E = new MobileSignalController.MobileIconGroup("5Ge", null, null, iArr15, 0, 0, 0, 0, iArr15[0], C0015R$string.data_connection_5ge_html, ICON_5G_E, true);
        int[] iArr16 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        NR_5G = new MobileSignalController.MobileIconGroup("5G", null, null, iArr16, 0, 0, 0, 0, iArr16[0], C0015R$string.data_connection_5g, ICON_5G, true);
        int[] iArr17 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        NR_5G_PLUS = new MobileSignalController.MobileIconGroup("5G_PLUS", null, null, iArr17, 0, 0, 0, 0, iArr17[0], C0015R$string.data_connection_5g_plus, ICON_5G_PLUS, true);
        int[] iArr18 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        NR_5G_X = new MobileSignalController.MobileIconGroup("5G_X", null, null, iArr18, 0, 0, 0, 0, iArr18[0], C0015R$string.data_connection_5g_x, ICON_5G_X, true);
        int[] iArr19 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        DATA_DISABLED = new MobileSignalController.MobileIconGroup("DataDisabled", null, null, iArr19, 0, 0, 0, 0, iArr19[0], C0015R$string.cell_data_off_content_description, ICON_DATA_DISABLED, false);
        int[] iArr20 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        NOT_DEFAULT_DATA = new MobileSignalController.MobileIconGroup("NotDefaultData", null, null, iArr20, 0, 0, 0, 0, iArr20[0], C0015R$string.not_default_data_content_description, 0, false);
        int i2 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0];
        int[] iArr21 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FIVE_G_BASIC = new MobileSignalController.MobileIconGroup("5GBasic", null, null, iArr21, 0, 0, 0, 0, iArr21[0], C0015R$string.data_connection_5g_basic, ICON_5G_BASIC, false);
        int[] iArr22 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FIVE_G_UWB = new MobileSignalController.MobileIconGroup("5GUWB", null, null, iArr22, 0, 0, 0, 0, iArr22[0], C0015R$string.data_connection_5g_uwb, ICON_5G_UWB, false);
        int[] iArr23 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FIVE_G_SA = new MobileSignalController.MobileIconGroup("5GSA", null, null, iArr23, 0, 0, 0, 0, iArr23[0], C0015R$string.data_connection_5g_sa, ICON_5G_SA, false);
        int[] iArr24 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        VOWIFI = new MobileSignalController.MobileIconGroup("VoWIFI", null, null, iArr24, 0, 0, 0, 0, iArr24[0], 0, ICON_VOWIFI, false);
        int[] iArr25 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        VOWIFI_CALLING = new MobileSignalController.MobileIconGroup("VoWIFICall", null, null, iArr25, 0, 0, 0, 0, iArr25[0], 0, ICON_VOWIFI_CALLING, false);
        HashMap hashMap = new HashMap();
        ICON_NAME_TO_ICON = hashMap;
        hashMap.put("carrier_network_change", CARRIER_NETWORK_CHANGE);
        ICON_NAME_TO_ICON.put("3g", THREE_G);
        ICON_NAME_TO_ICON.put("wfc", WFC);
        ICON_NAME_TO_ICON.put("unknown", UNKNOWN);
        ICON_NAME_TO_ICON.put("e", E);
        ICON_NAME_TO_ICON.put("1x", ONE_X);
        ICON_NAME_TO_ICON.put("g", G);
        ICON_NAME_TO_ICON.put("h", H);
        ICON_NAME_TO_ICON.put("h+", H_PLUS);
        ICON_NAME_TO_ICON.put("4g", FOUR_G);
        ICON_NAME_TO_ICON.put("4g+", FOUR_G_PLUS);
        ICON_NAME_TO_ICON.put("5ge", LTE_CA_5G_E);
        ICON_NAME_TO_ICON.put("lte", LTE);
        ICON_NAME_TO_ICON.put("lte+", LTE_PLUS);
        ICON_NAME_TO_ICON.put("5g", NR_5G);
        ICON_NAME_TO_ICON.put("5g_plus", NR_5G_PLUS);
        ICON_NAME_TO_ICON.put("5guwb", FIVE_G_UWB);
        ICON_NAME_TO_ICON.put("datadisable", DATA_DISABLED);
        ICON_NAME_TO_ICON.put("notdefaultdata", NOT_DEFAULT_DATA);
        int[] iArr26 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        TWO_G = new MobileSignalController.MobileIconGroup("2G", null, null, iArr26, 0, 0, 0, 0, iArr26[0], C0015R$string.data_connection_2g, ICON_2G, true);
        int[] iArr27 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        THREE_G_PLUS = new MobileSignalController.MobileIconGroup("3G+", null, null, iArr27, 0, 0, 0, 0, iArr27[0], C0015R$string.data_connection_3_5g, ICON_3G_PLUS, true);
        int[] iArr28 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FOUR_G_LTE = new MobileSignalController.MobileIconGroup("LTE", null, null, iArr28, 0, 0, 0, 0, iArr28[0], C0015R$string.data_connection_lte, OpSignalIcons.FOUR_G_LTE, true);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0035: APUT  (r2v1 int[]), (0 ??[int, short, byte, char]), (r4v1 int) */
    static int[] getStackedVoiceIcon(int i, int i2, boolean z, boolean z2) {
        int i3 = STACKED_ICON_1X;
        int[] iArr = new int[2];
        int i4 = STACKED_STRENGTH_ICONS[i2];
        if (z) {
            i3 = STACKED_ICON_ROAM;
        } else if (i != 7) {
            if (i != 13) {
                Log.w("TelephonyIcons", "Unknow network type:" + i);
            } else {
                i3 = z2 ? STACKED_ICON_LTE : STACKED_ICON_4G;
            }
        }
        iArr[0] = i4;
        iArr[1] = i3;
        return iArr;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0051: APUT  (r1v1 int[]), (0 ??[int, short, byte, char]), (r4v1 int) */
    static int[] getStackedDataIcon(int i, int i2, boolean z) {
        int i3;
        int i4;
        int[] iArr = new int[2];
        int i5 = STACKED_STRENGTH_ICONS[i2];
        if (i != 3) {
            if (i != 4) {
                if (!(i == 5 || i == 6)) {
                    if (i != 7) {
                        if (i != 17) {
                            if (i != 19) {
                                switch (i) {
                                    case 12:
                                    case 14:
                                        break;
                                    case 13:
                                        if (!z) {
                                            i4 = STACKED_ICON_4G;
                                            break;
                                        } else {
                                            i4 = STACKED_ICON_LTE;
                                            break;
                                        }
                                    default:
                                        i3 = STACKED_ICON_G;
                                        Log.w("TelephonyIcons", "Unknow network type:" + i);
                                        break;
                                }
                                iArr[0] = i5;
                                iArr[1] = i3;
                                return iArr;
                            }
                            i4 = z ? STACKED_ICON_LTE_PLUS : STACKED_ICON_4G_PLUS;
                            i3 = i4;
                            iArr[0] = i5;
                            iArr[1] = i3;
                            return iArr;
                        }
                    }
                }
            }
            i3 = STACKED_ICON_2G;
            iArr[0] = i5;
            iArr[1] = i3;
            return iArr;
        }
        i3 = STACKED_ICON_3G;
        iArr[0] = i5;
        iArr[1] = i3;
        return iArr;
    }

    static int getOneplusVirtualSimSignalIconId(int i) {
        return ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_VIRTUAL[i];
    }

    static int getOneplusRoamingSignalIconId(int i) {
        return ONEPLUS_TELEPHONY_SIGNAL_STRENGTH_ROAMING[i];
    }
}
