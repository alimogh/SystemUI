package com.android.systemui.statusbar.policy;

import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.systemui.Dumpable;
import java.util.Collection;
import java.util.List;
public interface BluetoothController extends CallbackController<Callback>, Dumpable {

    public interface Callback {
        default void onBluetoothActiveDeviceChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        }

        void onBluetoothDevicesChanged();

        void onBluetoothStateChange(boolean z);
    }

    boolean canConfigBluetooth();

    void connect(CachedBluetoothDevice cachedBluetoothDevice);

    void disconnect(CachedBluetoothDevice cachedBluetoothDevice);

    int getBatteryLevel();

    int getBluetoothState();

    int getBondState(CachedBluetoothDevice cachedBluetoothDevice);

    String getConnectedDeviceName();

    List<CachedBluetoothDevice> getConnectedDevices();

    Collection<CachedBluetoothDevice> getDevices();

    boolean isBluetoothConnected();

    boolean isBluetoothConnecting();

    boolean isBluetoothEnabled();

    boolean isBluetoothSupported();

    boolean isBluetoothTransientEnabling();

    void setBluetoothEnabled(boolean z);

    void setBluetoothTransientEnabling();
}
