package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.systemui.statusbar.policy.BluetoothController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
public class BluetoothControllerImpl implements BluetoothController, BluetoothCallback, CachedBluetoothDevice.Callback, LocalBluetoothProfileManager.ServiceListener {
    private boolean mAudioProfileOnly;
    private final Handler mBgHandler;
    private final WeakHashMap<CachedBluetoothDevice, ActuallyCachedState> mCachedState = new WeakHashMap<>();
    private final List<CachedBluetoothDevice> mConnectedDevices = new ArrayList();
    private int mConnectionState = 0;
    private final int mCurrentUser;
    private boolean mEnabled;
    private final H mHandler;
    private boolean mIsActive;
    private int mLastChangedActiveBluetoothProfile;
    private CachedBluetoothDevice mLastChangedActiveDevice;
    private long mLastStateChangeUpdateTime;
    private long mLastUpdateTime;
    private final LocalBluetoothManager mLocalBluetoothManager;
    private int mState;
    private boolean mTransientEnabling = false;
    private final UserManager mUserManager;

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfileManager.ServiceListener
    public void onServiceDisconnected() {
    }

    public BluetoothControllerImpl(Context context, Looper looper, Looper looper2, LocalBluetoothManager localBluetoothManager) {
        this.mLocalBluetoothManager = localBluetoothManager;
        this.mBgHandler = new Handler(looper);
        this.mHandler = new H(looper2);
        LocalBluetoothManager localBluetoothManager2 = this.mLocalBluetoothManager;
        if (localBluetoothManager2 != null) {
            localBluetoothManager2.getEventManager().registerCallback(this);
            this.mLocalBluetoothManager.getProfileManager().addServiceListener(this);
            onBluetoothStateChanged(this.mLocalBluetoothManager.getBluetoothAdapter().getBluetoothState());
        }
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mCurrentUser = ActivityManager.getCurrentUser();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public boolean canConfigBluetooth() {
        return !this.mUserManager.hasUserRestriction("no_config_bluetooth", UserHandle.of(this.mCurrentUser)) && !this.mUserManager.hasUserRestriction("no_bluetooth", UserHandle.of(this.mCurrentUser));
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("BluetoothController state:");
        printWriter.print("  mLocalBluetoothManager=");
        printWriter.println(this.mLocalBluetoothManager);
        if (this.mLocalBluetoothManager != null) {
            printWriter.print("  mEnabled=");
            printWriter.println(this.mEnabled);
            printWriter.print("  mConnectionState=");
            printWriter.println(stateToString(this.mConnectionState));
            printWriter.print("  mAudioProfileOnly=");
            printWriter.println(this.mAudioProfileOnly);
            printWriter.print("  mIsActive=");
            printWriter.println(this.mIsActive);
            printWriter.print("  mConnectedDevices=");
            printWriter.println(this.mConnectedDevices);
            printWriter.print("  mCallbacks.size=");
            printWriter.println(this.mHandler.mCallbacks.size());
            printWriter.println("  Bluetooth Devices:");
            Iterator<CachedBluetoothDevice> it = getDevices().iterator();
            while (it.hasNext()) {
                printWriter.println("    " + getDeviceString(it.next()));
            }
            if (this.mLocalBluetoothManager.getBluetoothAdapter() != null) {
                printWriter.println("  getConnectionState=");
                printWriter.println(this.mLocalBluetoothManager.getBluetoothAdapter().getConnectionState());
            }
        }
    }

    private static String stateToString(int i) {
        if (i == 0) {
            return "DISCONNECTED";
        }
        if (i == 1) {
            return "CONNECTING";
        }
        if (i == 2) {
            return "CONNECTED";
        }
        if (i == 3) {
            return "DISCONNECTING";
        }
        return "UNKNOWN(" + i + ")";
    }

    private String getDeviceString(CachedBluetoothDevice cachedBluetoothDevice) {
        return cachedBluetoothDevice.getName() + " " + cachedBluetoothDevice.getBondState() + " " + cachedBluetoothDevice.isConnected();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public int getBondState(CachedBluetoothDevice cachedBluetoothDevice) {
        return getCachedState(cachedBluetoothDevice).mBondState;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public List<CachedBluetoothDevice> getConnectedDevices() {
        return this.mConnectedDevices;
    }

    public void addCallback(BluetoothController.Callback callback) {
        this.mHandler.obtainMessage(3, callback).sendToTarget();
        this.mHandler.sendEmptyMessage(2);
    }

    public void removeCallback(BluetoothController.Callback callback) {
        this.mHandler.obtainMessage(4, callback).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public boolean isBluetoothEnabled() {
        return this.mEnabled;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public int getBluetoothState() {
        return this.mState;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public boolean isBluetoothConnected() {
        return this.mConnectionState == 2;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public boolean isBluetoothConnecting() {
        return this.mConnectionState == 1;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public void setBluetoothTransientEnabling() {
        this.mTransientEnabling = true;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public boolean isBluetoothTransientEnabling() {
        return this.mTransientEnabling;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public void setBluetoothEnabled(boolean z) {
        LocalBluetoothManager localBluetoothManager = this.mLocalBluetoothManager;
        if (localBluetoothManager != null) {
            localBluetoothManager.getBluetoothAdapter().setBluetoothEnabled(z);
        }
        this.mLastUpdateTime = 0;
        this.mLastStateChangeUpdateTime = 0;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public boolean isBluetoothSupported() {
        return this.mLocalBluetoothManager != null;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public void connect(CachedBluetoothDevice cachedBluetoothDevice) {
        if (this.mLocalBluetoothManager != null && cachedBluetoothDevice != null) {
            cachedBluetoothDevice.connect(true);
        }
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public void disconnect(CachedBluetoothDevice cachedBluetoothDevice) {
        if (this.mLocalBluetoothManager != null && cachedBluetoothDevice != null) {
            cachedBluetoothDevice.disconnect();
        }
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public String getConnectedDeviceName() {
        if (this.mConnectedDevices.size() == 1) {
            return this.mConnectedDevices.get(0).getName();
        }
        return null;
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public Collection<CachedBluetoothDevice> getDevices() {
        LocalBluetoothManager localBluetoothManager = this.mLocalBluetoothManager;
        if (localBluetoothManager != null) {
            return localBluetoothManager.getCachedDeviceManager().getCachedDevicesCopy();
        }
        return null;
    }

    private void updateConnected() {
        int connectionState = this.mLocalBluetoothManager.getBluetoothAdapter().getConnectionState();
        this.mConnectedDevices.clear();
        int i = connectionState;
        for (CachedBluetoothDevice cachedBluetoothDevice : getDevices()) {
            int maxConnectionState = cachedBluetoothDevice.getMaxConnectionState();
            if (maxConnectionState > i) {
                i = maxConnectionState;
            }
            if (cachedBluetoothDevice.isConnected()) {
                this.mConnectedDevices.add(cachedBluetoothDevice);
            }
        }
        if (this.mConnectedDevices.isEmpty() && i == 2) {
            i = 0;
            Log.d("BluetoothController", "update state to DISCONNECTED");
        }
        Log.d("BluetoothController", "updateConnected: " + connectionState + " to " + i + ", connection:" + this.mConnectionState + ", empty:" + this.mConnectedDevices.isEmpty());
        if (i != this.mConnectionState) {
            this.mConnectionState = i;
            this.mHandler.sendEmptyMessage(2);
        }
        updateAudioProfile();
    }

    private void updateActive() {
        boolean z = false;
        for (CachedBluetoothDevice cachedBluetoothDevice : getDevices()) {
            boolean z2 = true;
            if (!cachedBluetoothDevice.isActiveDevice(1) && !cachedBluetoothDevice.isActiveDevice(2) && !cachedBluetoothDevice.isActiveDevice(21)) {
                z2 = false;
            }
            z |= z2;
        }
        if (this.mIsActive != z) {
            this.mIsActive = z;
            this.mHandler.sendEmptyMessage(2);
        }
    }

    private void updateAudioProfile() {
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        for (CachedBluetoothDevice cachedBluetoothDevice : getDevices()) {
            for (LocalBluetoothProfile localBluetoothProfile : cachedBluetoothDevice.getProfiles()) {
                int profileId = localBluetoothProfile.getProfileId();
                boolean isConnectedProfile = cachedBluetoothDevice.isConnectedProfile(localBluetoothProfile);
                if (profileId == 1 || profileId == 2 || profileId == 21) {
                    z2 |= isConnectedProfile;
                } else {
                    z3 |= isConnectedProfile;
                }
            }
        }
        if (z2 && !z3) {
            z = true;
        }
        if (z != this.mAudioProfileOnly) {
            this.mAudioProfileOnly = z;
            this.mHandler.sendEmptyMessage(2);
        }
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onBluetoothStateChanged(int i) {
        Log.d("BluetoothController", "BluetoothStateChanged=" + stateToString(i));
        this.mEnabled = i == 12 || i == 11;
        this.mState = i;
        updateConnected();
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onDeviceAdded(CachedBluetoothDevice cachedBluetoothDevice) {
        StringBuilder sb = new StringBuilder();
        sb.append("DeviceAdded=");
        sb.append(cachedBluetoothDevice != null ? cachedBluetoothDevice.getAddress() : "null");
        Log.d("BluetoothController", sb.toString());
        cachedBluetoothDevice.registerCallback(this);
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onDeviceDeleted(CachedBluetoothDevice cachedBluetoothDevice) {
        StringBuilder sb = new StringBuilder();
        sb.append("DeviceDeleted=");
        sb.append(cachedBluetoothDevice != null ? cachedBluetoothDevice.getAddress() : "null");
        Log.d("BluetoothController", sb.toString());
        this.mCachedState.remove(cachedBluetoothDevice);
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("DeviceBondStateChanged=");
        sb.append(cachedBluetoothDevice != null ? cachedBluetoothDevice.getAddress() : "null");
        Log.d("BluetoothController", sb.toString());
        this.mCachedState.remove(cachedBluetoothDevice);
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.settingslib.bluetooth.CachedBluetoothDevice.Callback
    public void onDeviceAttributesChanged() {
        Log.d("BluetoothController", "DeviceAttributesChanged");
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("ConnectionStateChanged=");
        sb.append(cachedBluetoothDevice != null ? cachedBluetoothDevice.getAddress() : "null");
        sb.append(" ");
        sb.append(stateToString(i));
        Log.d("BluetoothController", sb.toString());
        this.mCachedState.remove(cachedBluetoothDevice);
        updateConnected();
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onProfileConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i, int i2) {
        StringBuilder sb = new StringBuilder();
        sb.append("ProfileConnectionStateChanged=");
        sb.append(cachedBluetoothDevice != null ? cachedBluetoothDevice.getAddress() : "null");
        sb.append(" ");
        sb.append(stateToString(i));
        sb.append(" profileId=");
        sb.append(i2);
        Log.d("BluetoothController", sb.toString());
        this.mCachedState.remove(cachedBluetoothDevice);
        updateConnected();
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onActiveDeviceChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("ActiveDeviceChanged=");
        sb.append(cachedBluetoothDevice != null ? cachedBluetoothDevice.getAddress() : "null");
        sb.append(" profileId=");
        sb.append(i);
        Log.d("BluetoothController", sb.toString());
        this.mLastChangedActiveDevice = cachedBluetoothDevice;
        this.mLastChangedActiveBluetoothProfile = i;
        this.mHandler.sendEmptyMessage(5);
        updateActive();
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onAclConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("ACLConnectionStateChanged=");
        sb.append(cachedBluetoothDevice != null ? cachedBluetoothDevice.getAddress() : "null");
        sb.append(" ");
        sb.append(stateToString(i));
        Log.d("BluetoothController", sb.toString());
        this.mCachedState.remove(cachedBluetoothDevice);
        updateConnected();
        this.mHandler.sendEmptyMessage(2);
    }

    private ActuallyCachedState getCachedState(CachedBluetoothDevice cachedBluetoothDevice) {
        ActuallyCachedState actuallyCachedState = this.mCachedState.get(cachedBluetoothDevice);
        if (actuallyCachedState != null) {
            return actuallyCachedState;
        }
        ActuallyCachedState actuallyCachedState2 = new ActuallyCachedState(cachedBluetoothDevice, this.mHandler);
        this.mBgHandler.post(actuallyCachedState2);
        this.mCachedState.put(cachedBluetoothDevice, actuallyCachedState2);
        return actuallyCachedState2;
    }

    @Override // com.android.settingslib.bluetooth.LocalBluetoothProfileManager.ServiceListener
    public void onServiceConnected() {
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    /* access modifiers changed from: private */
    public static class ActuallyCachedState implements Runnable {
        private int mBondState;
        private final WeakReference<CachedBluetoothDevice> mDevice;
        private final Handler mUiHandler;

        private ActuallyCachedState(CachedBluetoothDevice cachedBluetoothDevice, Handler handler) {
            this.mBondState = 10;
            this.mDevice = new WeakReference<>(cachedBluetoothDevice);
            this.mUiHandler = handler;
        }

        @Override // java.lang.Runnable
        public void run() {
            CachedBluetoothDevice cachedBluetoothDevice = this.mDevice.get();
            if (cachedBluetoothDevice != null) {
                this.mBondState = cachedBluetoothDevice.getBondState();
                cachedBluetoothDevice.getMaxConnectionState();
                this.mUiHandler.removeMessages(1);
                this.mUiHandler.sendEmptyMessage(1);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        private final ArrayList<BluetoothController.Callback> mCallbacks = new ArrayList<>();

        public H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 1) {
                if (i == 2) {
                    Log.d("BluetoothController", "into MSG_STATE_CHANGED");
                    if (SystemClock.uptimeMillis() - BluetoothControllerImpl.this.mLastStateChangeUpdateTime < 400) {
                        BluetoothControllerImpl.this.mHandler.removeMessages(2);
                        BluetoothControllerImpl.this.mHandler.sendMessageAtTime(BluetoothControllerImpl.this.mHandler.obtainMessage(2), BluetoothControllerImpl.this.mLastStateChangeUpdateTime + 400);
                        return;
                    }
                    BluetoothControllerImpl.this.mLastStateChangeUpdateTime = SystemClock.uptimeMillis();
                    fireStateChange();
                    if (BluetoothControllerImpl.this.mState == 12 || BluetoothControllerImpl.this.mState == 10 || BluetoothControllerImpl.this.mState == 13) {
                        BluetoothControllerImpl.this.mTransientEnabling = false;
                    }
                } else if (i == 3) {
                    this.mCallbacks.add((BluetoothController.Callback) message.obj);
                } else if (i == 4) {
                    this.mCallbacks.remove((BluetoothController.Callback) message.obj);
                } else if (i == 5) {
                    fireActvieDeviceChange();
                }
            } else if (SystemClock.uptimeMillis() - BluetoothControllerImpl.this.mLastUpdateTime < 400) {
                BluetoothControllerImpl.this.mHandler.removeMessages(1);
                BluetoothControllerImpl.this.mHandler.sendMessageAtTime(BluetoothControllerImpl.this.mHandler.obtainMessage(1), BluetoothControllerImpl.this.mLastUpdateTime + 400);
            } else {
                BluetoothControllerImpl.this.mLastUpdateTime = SystemClock.uptimeMillis();
                firePairedDevicesChanged();
            }
        }

        private void firePairedDevicesChanged() {
            Iterator<BluetoothController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onBluetoothDevicesChanged();
            }
        }

        private void fireStateChange() {
            Iterator<BluetoothController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                fireStateChange(it.next());
            }
        }

        private void fireActvieDeviceChange() {
            Iterator<BluetoothController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onBluetoothActiveDeviceChanged(BluetoothControllerImpl.this.mLastChangedActiveDevice, BluetoothControllerImpl.this.mLastChangedActiveBluetoothProfile);
            }
        }

        private void fireStateChange(BluetoothController.Callback callback) {
            callback.onBluetoothStateChange(BluetoothControllerImpl.this.mEnabled);
        }
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController
    public int getBatteryLevel() {
        int i;
        synchronized (this.mConnectedDevices) {
            if (!this.mConnectedDevices.isEmpty()) {
                CachedBluetoothDevice cachedBluetoothDevice = null;
                CachedBluetoothDevice cachedBluetoothDevice2 = null;
                for (CachedBluetoothDevice cachedBluetoothDevice3 : this.mConnectedDevices) {
                    if (cachedBluetoothDevice3.isActiveDevice(1)) {
                        cachedBluetoothDevice = cachedBluetoothDevice3;
                    }
                    if (cachedBluetoothDevice3.isActiveDevice(2)) {
                        cachedBluetoothDevice2 = cachedBluetoothDevice3;
                    }
                }
                if (cachedBluetoothDevice != null) {
                    i = cachedBluetoothDevice.getBatteryLevel();
                } else if (cachedBluetoothDevice2 != null) {
                    i = cachedBluetoothDevice2.getBatteryLevel();
                }
            }
            i = -1;
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.d("BluetoothController", "getBatteryLevel batteryLevel " + i);
        }
        return i;
    }
}
