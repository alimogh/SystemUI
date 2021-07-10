package com.android.systemui.statusbar.policy;

import com.android.systemui.C0015R$string;
import com.oneplus.util.OpUtils;
public class AccessibilityContentDescriptions {
    static final int[] ETHERNET_CONNECTION_VALUES = {C0015R$string.accessibility_ethernet_disconnected, C0015R$string.accessibility_ethernet_connected};
    static final int[] PHONE_SIGNAL_STRENGTH = (OpUtils.isSupportFiveBar() ? PHONE_SIGNAL_STRENGTH_FIVE_BAR : PHONE_SIGNAL_STRENGTH_FOUR_BAR);
    static final int[] PHONE_SIGNAL_STRENGTH_FIVE_BAR = {C0015R$string.accessibility_no_phone, C0015R$string.accessibility_phone_one_bar, C0015R$string.accessibility_phone_two_bars, C0015R$string.accessibility_phone_three_bars, C0015R$string.accessibility_phone_four_bars, C0015R$string.accessibility_phone_signal_full};
    static final int[] PHONE_SIGNAL_STRENGTH_FOUR_BAR = {C0015R$string.accessibility_no_phone, C0015R$string.accessibility_phone_one_bar, C0015R$string.accessibility_phone_two_bars, C0015R$string.accessibility_phone_three_bars, C0015R$string.accessibility_phone_signal_full};
    static final int[] WIFI_CONNECTION_STRENGTH = {C0015R$string.accessibility_no_wifi, C0015R$string.accessibility_wifi_one_bar, C0015R$string.accessibility_wifi_two_bars, C0015R$string.accessibility_wifi_three_bars, C0015R$string.accessibility_wifi_signal_full};
    static final int WIFI_NO_CONNECTION = C0015R$string.accessibility_no_wifi;
}
