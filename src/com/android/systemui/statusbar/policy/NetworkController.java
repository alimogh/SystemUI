package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionInfo;
import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.wifi.AccessPoint;
import com.android.systemui.DemoMode;
import com.oneplus.networkspeed.NetworkSpeedController;
import java.util.List;
public interface NetworkController extends CallbackController<SignalCallback>, DemoMode {

    public interface AccessPointController {

        public interface AccessPointCallback {
            void onAccessPointsChanged(List<AccessPoint> list);

            void onSettingsActivityTriggered(Intent intent);
        }

        void addAccessPointCallback(AccessPointCallback accessPointCallback);

        boolean canConfigWifi();

        boolean connect(AccessPoint accessPoint);

        int getIcon(AccessPoint accessPoint);

        void removeAccessPointCallback(AccessPointCallback accessPointCallback);

        void scanForAccessPoints();
    }

    public interface EmergencyListener {
        void setEmergencyCallsOnly(boolean z);
    }

    public interface SignalCallback {
        default void setEthernetIndicators(IconState iconState) {
        }

        default void setHasAnySimReady(boolean z) {
        }

        default void setIsAirplaneMode(IconState iconState) {
        }

        default void setLTEStatus(IconState[] iconStateArr) {
        }

        default void setMobileDataEnabled(boolean z) {
        }

        default void setMobileDataIndicators(IconState iconState, IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, int[] iArr, int[] iArr2, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4, boolean z5) {
        }

        default void setNoSims(boolean z, boolean z2) {
        }

        default void setProvision(int i, int i2) {
        }

        default void setSubs(List<SubscriptionInfo> list) {
        }

        default void setTetherError(Intent intent) {
        }

        default void setVirtualSimstate(int[] iArr) {
        }

        default void setWifiIndicators(boolean z, IconState iconState, IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
        }
    }

    void addCallback(SignalCallback signalCallback);

    AccessPointController getAccessPointController();

    DataSaverController getDataSaverController();

    DataUsageController getMobileDataController();

    boolean hasEmergencyCryptKeeperText();

    boolean hasMobileDataFeature();

    boolean isRadioOn();

    void removeCallback(SignalCallback signalCallback);

    void setNetworkSpeedController(NetworkSpeedController networkSpeedController);

    void setWifiEnabled(boolean z);

    public static class IconState {
        public final String contentDescription;
        public final int icon;
        public final boolean visible;

        public IconState(boolean z, int i, String str) {
            this.visible = z;
            this.icon = i;
            this.contentDescription = str;
        }

        public IconState(boolean z, int i, int i2, Context context) {
            this(z, i, context.getString(i2));
        }
    }
}
