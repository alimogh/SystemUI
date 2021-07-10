package com.android.settingslib.deviceinfo;
public abstract class AbstractBluetoothAddressPreferenceController extends AbstractConnectivityPreferenceController {
    private static final String[] CONNECTIVITY_INTENTS = {"android.bluetooth.adapter.action.STATE_CHANGED"};
    static final String KEY_BT_ADDRESS = "bt_address";

    /* access modifiers changed from: protected */
    @Override // com.android.settingslib.deviceinfo.AbstractConnectivityPreferenceController
    public String[] getConnectivityIntents() {
        return CONNECTIVITY_INTENTS;
    }
}
