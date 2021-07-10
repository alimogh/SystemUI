package com.android.settingslib.deviceinfo;
public abstract class AbstractIpAddressPreferenceController extends AbstractConnectivityPreferenceController {
    private static final String[] CONNECTIVITY_INTENTS = {"android.net.conn.CONNECTIVITY_CHANGE", "android.net.wifi.LINK_CONFIGURATION_CHANGED", "android.net.wifi.STATE_CHANGE"};
    static final String KEY_IP_ADDRESS = "wifi_ip_address";

    /* access modifiers changed from: protected */
    @Override // com.android.settingslib.deviceinfo.AbstractConnectivityPreferenceController
    public String[] getConnectivityIntents() {
        return CONNECTIVITY_INTENTS;
    }
}
