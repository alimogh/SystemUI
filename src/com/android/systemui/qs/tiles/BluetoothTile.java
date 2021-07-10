package com.android.systemui.qs.tiles;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import androidx.lifecycle.Lifecycle;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.Utils;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.graph.BluetoothDeviceLayerDrawable;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0013R$plurals;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.oneplus.util.OpUtils;
import com.oos.onepluspods.service.aidl.IOnePlusPodDevice;
import com.oos.onepluspods.service.aidl.IOnePlusUpdate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
public class BluetoothTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent BLUETOOTH_SETTINGS = new Intent("android.settings.BLUETOOTH_SETTINGS");
    private final ActivityStarter mActivityStarter;
    private HashMap<String, BatteryInfo> mBatteryInfoMap;
    private boolean mBindingPodsService = false;
    private final BluetoothController.Callback mCallback = new BluetoothController.Callback() { // from class: com.android.systemui.qs.tiles.BluetoothTile.4
        @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
        public void onBluetoothStateChange(boolean z) {
            String str = ((QSTileImpl) BluetoothTile.this).TAG;
            Log.d(str, "** onBluetoothStateChange(): enabled=" + z);
            BluetoothTile.this.refreshState();
            if (BluetoothTile.this.isShowingDetail()) {
                BluetoothTile.this.mDetailAdapter.updateItems();
                BluetoothTile bluetoothTile = BluetoothTile.this;
                bluetoothTile.fireToggleStateChanged(bluetoothTile.mDetailAdapter.getToggleState().booleanValue());
            }
        }

        @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
        public void onBluetoothDevicesChanged() {
            Log.d(((QSTileImpl) BluetoothTile.this).TAG, "** onBluetoothDevicesChanged()");
            BluetoothTile.this.refreshState();
            if (BluetoothTile.this.isShowingDetail()) {
                BluetoothTile.this.mDetailAdapter.updateItems();
            }
        }
    };
    private ServiceConnection mConnecttion = new ServiceConnection() { // from class: com.android.systemui.qs.tiles.BluetoothTile.3
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            String str = ((QSTileImpl) BluetoothTile.this).TAG;
            Log.d(str, "onServiceConnected: " + componentName + ", binder=" + iBinder);
            BluetoothTile.this.mBindingPodsService = false;
            if (iBinder != null) {
                try {
                    BluetoothTile.this.mPodsService = IOnePlusPodDevice.Stub.asInterface(iBinder);
                    BluetoothTile.this.mPodsService.setIOnePlusUpdate(BluetoothTile.this.mStub);
                    if (BluetoothTile.this.mCurrentAddress != null) {
                        BluetoothTile.this.mStub.updateView(BluetoothTile.this.mCurrentAddress, 2);
                    }
                } catch (RemoteException e) {
                    String str2 = ((QSTileImpl) BluetoothTile.this).TAG;
                    Log.d(str2, "Error in setIOnePlusUpdate: " + e);
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            String str = ((QSTileImpl) BluetoothTile.this).TAG;
            Log.d(str, "onServiceDisconnected: " + componentName);
            BluetoothTile.this.mBindingPodsService = false;
            if (BluetoothTile.this.mTWSPodsHandler != null) {
                BluetoothTile.this.mTWSPodsHandler.post(new Runnable() { // from class: com.android.systemui.qs.tiles.-$$Lambda$BluetoothTile$3$27WkAf3YRSotIhmgVcoXpXlo00c
                    @Override // java.lang.Runnable
                    public final void run() {
                        BluetoothTile.AnonymousClass3.this.lambda$onServiceDisconnected$0$BluetoothTile$3();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onServiceDisconnected$0 */
        public /* synthetic */ void lambda$onServiceDisconnected$0$BluetoothTile$3() {
            BluetoothTile.this.mPodsService = null;
        }
    };
    private final BluetoothController mController;
    private String mCurrentAddress;
    private final BluetoothDetailAdapter mDetailAdapter;
    private IOnePlusPodDevice mPodsService;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.BluetoothTile.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice bluetoothDevice;
            String action = intent.getAction();
            Log.d(((QSTileImpl) BluetoothTile.this).TAG, "TWS broadcast onReceive: " + action);
            if ("android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT".equals(action) && (bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE")) != null) {
                String stringExtra = intent.getStringExtra("android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_CMD");
                Object[] objArr = (Object[]) intent.getExtras().get("android.bluetooth.headset.extra.VENDOR_SPECIFIC_HEADSET_EVENT_ARGS");
                if (Build.DEBUG_ONEPLUS) {
                    String str = ((QSTileImpl) BluetoothTile.this).TAG;
                    StringBuilder sb = new StringBuilder();
                    sb.append("TWS broadcast command: ");
                    sb.append(stringExtra);
                    sb.append(", args: ");
                    sb.append(objArr != null ? TextUtils.join("|", objArr) : null);
                    Log.d(str, sb.toString());
                }
                if ("+VDBTY".equals(stringExtra) && objArr != null && objArr.length >= 3) {
                    int i = -1;
                    int i2 = -1;
                    int i3 = -1;
                    for (int i4 = 0; i4 < ((Integer) objArr[0]).intValue(); i4++) {
                        int i5 = i4 * 2;
                        int intValue = ((Integer) objArr[i5 + 1]).intValue();
                        int batteryLevel = BluetoothTile.this.getBatteryLevel(objArr[i5 + 2]);
                        if (intValue == 1) {
                            i = batteryLevel;
                        }
                        if (intValue == 2) {
                            i2 = batteryLevel;
                        }
                        if (intValue == 3) {
                            i3 = batteryLevel;
                        }
                    }
                    Log.d(((QSTileImpl) BluetoothTile.this).TAG, "TWS v2 battery info for device: " + bluetoothDevice.getAddress() + ", leftLevel: " + i + ", rightLevel: " + i2 + ", boxLevel: " + i3);
                    BatteryInfo batteryInfo = BluetoothTile.this.getBatteryInfo(bluetoothDevice.getAddress());
                    if (batteryInfo == null) {
                        return;
                    }
                    if (i >= 0 || i2 >= 0) {
                        batteryInfo.leftBattery = i;
                        batteryInfo.rightBattery = i2;
                        if (BluetoothTile.this.mTWSPodsHandler != null) {
                            BluetoothTile.this.mTWSPodsHandler.post(new Runnable(batteryInfo, bluetoothDevice) { // from class: com.android.systemui.qs.tiles.-$$Lambda$BluetoothTile$2$0030NaulpdlGN87AFb-81nJf79g
                                public final /* synthetic */ BluetoothTile.BatteryInfo f$1;
                                public final /* synthetic */ BluetoothDevice f$2;

                                {
                                    this.f$1 = r2;
                                    this.f$2 = r3;
                                }

                                @Override // java.lang.Runnable
                                public final void run() {
                                    BluetoothTile.AnonymousClass2.this.lambda$onReceive$0$BluetoothTile$2(this.f$1, this.f$2);
                                }
                            });
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onReceive$0 */
        public /* synthetic */ void lambda$onReceive$0$BluetoothTile$2(BatteryInfo batteryInfo, BluetoothDevice bluetoothDevice) {
            try {
                batteryInfo.isOnePlusPods = BluetoothTile.this.mPodsService != null && BluetoothTile.this.mPodsService.isOnePlusPods(bluetoothDevice.getAddress());
            } catch (RemoteException e) {
                String str = ((QSTileImpl) BluetoothTile.this).TAG;
                Log.d(str, "Error calling into oppods service: " + e);
                batteryInfo.isOnePlusPods = false;
            }
            BluetoothTile.this.refreshState();
        }
    };
    private IOnePlusUpdate.Stub mStub = new IOnePlusUpdate.Stub() { // from class: com.android.systemui.qs.tiles.BluetoothTile.1
        @Override // com.oos.onepluspods.service.aidl.IOnePlusUpdate
        public void updateView(String str, int i) {
            if (BluetoothTile.this.mTWSPodsHandler != null) {
                BluetoothTile.this.mTWSPodsHandler.post(new Runnable(str, i) { // from class: com.android.systemui.qs.tiles.-$$Lambda$BluetoothTile$1$GRxIaWvDtxd1o_3yFKy3qShKziE
                    public final /* synthetic */ String f$1;
                    public final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        BluetoothTile.AnonymousClass1.this.lambda$updateView$0$BluetoothTile$1(this.f$1, this.f$2);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: updateViewImpl */
        public void lambda$updateView$0(String str, int i) {
            String str2 = ((QSTileImpl) BluetoothTile.this).TAG;
            Log.d(str2, "IOnePlusUpdate.Stub updateView: address=" + str + ", content=" + i);
            if (i == 2 && BluetoothTile.this.mPodsService != null) {
                try {
                    BatteryInfo batteryInfo = BluetoothTile.this.getBatteryInfo(str);
                    int battaryInfo = BluetoothTile.this.mPodsService.getBattaryInfo(str);
                    if (battaryInfo >= 0 && batteryInfo != null) {
                        batteryInfo.leftBattery = battaryInfo % 1000;
                        batteryInfo.rightBattery = (battaryInfo / 1000) % 1000;
                        batteryInfo.isOnePlusPods = BluetoothTile.this.mPodsService.isOnePlusPods(str);
                    }
                    BluetoothTile.this.refreshState();
                } catch (RemoteException e) {
                    String str3 = ((QSTileImpl) BluetoothTile.this).TAG;
                    Log.d(str3, "Error calling mPodsService: " + e);
                }
            }
        }
    };
    private Handler mTWSPodsHandler;
    private HandlerThread mTWSPodsThread;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 113;
    }

    /* access modifiers changed from: package-private */
    public static class BatteryInfo {
        boolean isOnePlusPods;
        int leftBattery;
        int rightBattery;

        BatteryInfo() {
        }

        public String toString() {
            return "[ isOnePlusPods=" + this.isOnePlusPods + ", leftBattery=" + this.leftBattery + ", rightBattery=" + this.rightBattery + " ]";
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private BatteryInfo getBatteryInfo(String str) {
        if (str == null) {
            return null;
        }
        String trim = str.trim();
        if (!this.mBatteryInfoMap.containsKey(trim)) {
            this.mBatteryInfoMap.put(trim, new BatteryInfo());
        }
        return this.mBatteryInfoMap.get(trim);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getBatteryLevel(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof String) {
            return 1;
        }
        int intValue = ((Integer) obj).intValue();
        int i = intValue > 1000 ? intValue % 1000 : (intValue + 1) * 10;
        String str = this.TAG;
        Log.d(str, "getBatteryLevel value = " + intValue + " level = " + i);
        return i;
    }

    /* access modifiers changed from: private */
    /* renamed from: bindOnePlusPodsService */
    public void lambda$getSecondaryLabel$2(CachedBluetoothDevice cachedBluetoothDevice) {
        String str = this.TAG;
        Log.d(str, "bindOnePlusPodsService(): mPodsService=" + this.mPodsService + ", binding=" + this.mBindingPodsService + ", device=" + cachedBluetoothDevice);
        if (cachedBluetoothDevice != null && this.mPodsService == null && !this.mBindingPodsService) {
            Intent intent = new Intent();
            intent.setClassName("com.oneplus.twspods", "com.oos.onepluspods.service.MultiDeviceCoreService");
            intent.putExtra("address", cachedBluetoothDevice.getAddress());
            intent.putExtra("device", cachedBluetoothDevice.getDevice());
            this.mContext.bindService(intent, this.mConnecttion, 1);
            this.mCurrentAddress = cachedBluetoothDevice.getAddress();
            this.mUiHandler.postDelayed(new Runnable() { // from class: com.android.systemui.qs.tiles.-$$Lambda$BluetoothTile$eqFUzQb3kRYA30JP7ri6MkZ1kqI
                @Override // java.lang.Runnable
                public final void run() {
                    BluetoothTile.this.lambda$bindOnePlusPodsService$0$BluetoothTile();
                }
            }, 2000);
            this.mBindingPodsService = true;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindOnePlusPodsService$0 */
    public /* synthetic */ void lambda$bindOnePlusPodsService$0$BluetoothTile() {
        this.mBindingPodsService = false;
    }

    /* access modifiers changed from: private */
    /* renamed from: unBindOnePlusPodsService */
    public void lambda$handleDestroy$1() {
        try {
            String str = this.TAG;
            Log.d(str, "unBindOnePlusPodsService(): mPodsService=" + this.mPodsService);
            if (this.mPodsService != null) {
                this.mPodsService.setIOnePlusUpdate(null);
                this.mContext.unbindService(this.mConnecttion);
                this.mPodsService = null;
            }
        } catch (RemoteException e) {
            String str2 = this.TAG;
            Log.d(str2, "Error unbindService: " + e);
        }
    }

    public BluetoothTile(QSHost qSHost, BluetoothController bluetoothController, ActivityStarter activityStarter) {
        super(qSHost);
        this.mController = bluetoothController;
        this.mActivityStarter = activityStarter;
        this.mDetailAdapter = (BluetoothDetailAdapter) createDetailAdapter();
        this.mController.observe(getLifecycle(), (Lifecycle) this.mCallback);
        Log.d(this.TAG, "BluetoothTile.<init>: Creating TWS pods thread ...");
        HandlerThread handlerThread = new HandlerThread("tws_pods");
        this.mTWSPodsThread = handlerThread;
        handlerThread.start();
        this.mTWSPodsHandler = new Handler(this.mTWSPodsThread.getLooper());
        Log.d(this.TAG, "BluetoothTile.<init>: Registering for TWS broadcast ...");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        this.mBatteryInfoMap = new HashMap<>();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        Handler handler = this.mTWSPodsHandler;
        if (handler != null) {
            handler.post(new Runnable() { // from class: com.android.systemui.qs.tiles.-$$Lambda$BluetoothTile$0xzTgBmK363_YXVbrz1i8wN7z0M
                @Override // java.lang.Runnable
                public final void run() {
                    BluetoothTile.this.lambda$handleDestroy$1$BluetoothTile();
                }
            });
            Log.d(this.TAG, "handleDestroy(): Unregistered for TWS broadcast");
            this.mContext.unregisterReceiver(this.mReceiver);
            Log.d(this.TAG, "handleDestroy(): Stopping TWS pods thread ...");
            this.mTWSPodsThread.quitSafely();
            this.mTWSPodsHandler = null;
        }
        super.handleDestroy();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        Object obj;
        if (this.mController.isBluetoothTransientEnabling()) {
            Log.d(this.TAG, "transient enabling. skip.");
            return;
        }
        boolean z = ((QSTile.BooleanState) this.mState).value;
        if (z) {
            obj = null;
        } else {
            obj = QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        }
        refreshState(obj);
        if (!z) {
            this.mController.setBluetoothTransientEnabling();
        }
        if (OpUtils.DEBUG_ONEPLUS) {
            String str = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("handleClick, !isEnabled: ");
            sb.append(!z);
            Log.i(str, sb.toString());
        }
        this.mController.setBluetoothEnabled(!z);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.BLUETOOTH_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSecondaryClick() {
        if (!this.mController.canConfigBluetooth()) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.settings.BLUETOOTH_SETTINGS"), 0);
            return;
        }
        showDetail(true);
        if (!((QSTile.BooleanState) this.mState).value) {
            this.mController.setBluetoothEnabled(true);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_bluetooth_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = obj == QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING || this.mController.isBluetoothConnecting() || this.mController.isBluetoothTransientEnabling();
        boolean z2 = z || this.mController.isBluetoothEnabled();
        boolean isBluetoothConnected = this.mController.isBluetoothConnected();
        boolean isBluetoothConnecting = this.mController.isBluetoothConnecting();
        booleanState.isTransient = z || isBluetoothConnecting || this.mController.getBluetoothState() == 11;
        booleanState.dualTarget = true;
        booleanState.value = z2;
        if (booleanState.slash == null) {
            booleanState.slash = new QSTile.SlashState();
        }
        booleanState.slash.isSlashed = !z2;
        booleanState.label = this.mContext.getString(C0015R$string.quick_settings_bluetooth_label);
        booleanState.secondaryLabel = TextUtils.emptyIfNull(getSecondaryLabel(z2, isBluetoothConnecting, isBluetoothConnected, booleanState.isTransient));
        booleanState.contentDescription = booleanState.label;
        booleanState.stateDescription = "";
        if (z2) {
            if (isBluetoothConnected) {
                booleanState.icon = new BluetoothConnectedTileIcon(this);
                if (!TextUtils.isEmpty(this.mController.getConnectedDeviceName())) {
                    booleanState.label = this.mController.getConnectedDeviceName();
                }
                booleanState.stateDescription = this.mContext.getString(C0015R$string.accessibility_bluetooth_name, booleanState.label) + ", " + ((Object) booleanState.secondaryLabel);
            } else if (booleanState.isTransient) {
                booleanState.icon = QSTileImpl.ResourceIcon.get(17302321);
                booleanState.stateDescription = booleanState.secondaryLabel;
            } else {
                booleanState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_bluetooth);
                booleanState.contentDescription = this.mContext.getString(C0015R$string.accessibility_quick_settings_bluetooth);
                booleanState.stateDescription = this.mContext.getString(C0015R$string.accessibility_not_connected);
            }
            booleanState.state = 2;
        } else {
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_bluetooth);
            booleanState.contentDescription = this.mContext.getString(C0015R$string.accessibility_quick_settings_bluetooth);
            booleanState.state = 1;
        }
        booleanState.dualLabelContentDescription = this.mContext.getResources().getString(C0015R$string.accessibility_quick_settings_open_settings, getTileLabel());
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        if (OpUtils.DEBUG_ONEPLUS) {
            String str = this.TAG;
            Log.i(str, "Bt handleUpdateState end, state.state" + booleanState.state + ", enabled:" + z2 + ", connected:" + isBluetoothConnected + ", connecting:" + isBluetoothConnecting + ", transientEnabling:" + z + ", bluetoothState: " + this.mController.getBluetoothState());
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x011b: APUT  
      (r2v7 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x0117: INVOKE  (r10v19 java.lang.String) = (r7v0 double) type: STATIC call: com.android.settingslib.Utils.formatPercentage(double):java.lang.String)
     */
    private String getSecondaryLabel(boolean z, boolean z2, boolean z3, boolean z4) {
        String str = this.TAG;
        Log.d(str, "getSecondaryLabel: enabled=" + z + ", connecting=" + z2 + ", connected=" + z3 + ", transient=" + z4);
        if (z2) {
            return this.mContext.getString(C0015R$string.quick_settings_connecting);
        }
        if (z4) {
            return this.mContext.getString(C0015R$string.quick_settings_bluetooth_secondary_label_transient);
        }
        List<CachedBluetoothDevice> connectedDevices = this.mController.getConnectedDevices();
        if (Build.DEBUG_ONEPLUS) {
            String str2 = this.TAG;
            Log.d(str2, "** getSecondaryLabel: num_connected=" + connectedDevices.size() + ", devices: [ " + TextUtils.join(", ", connectedDevices) + " ], mPodsService=" + this.mPodsService);
        }
        if (z && z3 && !connectedDevices.isEmpty()) {
            if (connectedDevices.size() > 1) {
                return this.mContext.getResources().getQuantityString(C0013R$plurals.quick_settings_hotspot_secondary_label_num_devices, connectedDevices.size(), Integer.valueOf(connectedDevices.size()));
            }
            CachedBluetoothDevice cachedBluetoothDevice = connectedDevices.get(0);
            int batteryLevel = cachedBluetoothDevice.getBatteryLevel();
            if (batteryLevel <= -1) {
                BluetoothClass btClass = cachedBluetoothDevice.getBtClass();
                if (btClass != null) {
                    if (cachedBluetoothDevice.isHearingAidDevice()) {
                        return this.mContext.getString(C0015R$string.quick_settings_bluetooth_secondary_label_hearing_aids);
                    }
                    if (btClass.doesClassMatch(1)) {
                        return this.mContext.getString(C0015R$string.quick_settings_bluetooth_secondary_label_audio);
                    }
                    if (btClass.doesClassMatch(0)) {
                        return this.mContext.getString(C0015R$string.quick_settings_bluetooth_secondary_label_headset);
                    }
                    if (btClass.doesClassMatch(3)) {
                        return this.mContext.getString(C0015R$string.quick_settings_bluetooth_secondary_label_input);
                    }
                }
            } else if (this.mPodsService != null || this.mBindingPodsService) {
                BatteryInfo batteryInfo = getBatteryInfo(cachedBluetoothDevice.getAddress());
                String str3 = this.TAG;
                Log.d(str3, "** getSecondaryLabel: device=" + cachedBluetoothDevice + ", info=" + batteryInfo);
                if (batteryInfo != null && batteryInfo.isOnePlusPods) {
                    int i = batteryInfo.leftBattery;
                    int i2 = batteryInfo.rightBattery;
                    if (i >= 0 || i2 >= 0) {
                        Context context = this.mContext;
                        int i3 = C0015R$string.earphone_support_battery_info;
                        Object[] objArr = new Object[2];
                        double d = Double.NaN;
                        objArr[0] = Utils.formatPercentage(i == -1 ? Double.NaN : ((double) i) / 100.0d);
                        if (i2 != -1) {
                            d = ((double) i2) / 100.0d;
                        }
                        objArr[1] = Utils.formatPercentage(d);
                        return context.getString(i3, objArr);
                    }
                }
                return this.mContext.getString(C0015R$string.quick_settings_bluetooth_secondary_label_battery_level, Utils.formatPercentage(batteryLevel));
            } else {
                Handler handler = this.mTWSPodsHandler;
                if (handler != null) {
                    handler.post(new Runnable(cachedBluetoothDevice) { // from class: com.android.systemui.qs.tiles.-$$Lambda$BluetoothTile$pgLdrpl2l9ui3HKUmyyLqCB83kM
                        public final /* synthetic */ CachedBluetoothDevice f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            BluetoothTile.this.lambda$getSecondaryLabel$2$BluetoothTile(this.f$1);
                        }
                    });
                }
                return null;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0015R$string.accessibility_quick_settings_bluetooth_changed_on);
        }
        return this.mContext.getString(C0015R$string.accessibility_quick_settings_bluetooth_changed_off);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mController.isBluetoothSupported();
    }

    /* access modifiers changed from: protected */
    public DetailAdapter createDetailAdapter() {
        return new BluetoothDetailAdapter();
    }

    /* access modifiers changed from: private */
    public class BluetoothBatteryTileIcon extends QSTile.Icon {
        private int mBatteryLevel;
        private float mIconScale;

        BluetoothBatteryTileIcon(BluetoothTile bluetoothTile, int i, float f) {
            this.mBatteryLevel = i;
            this.mIconScale = f;
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            return BluetoothDeviceLayerDrawable.createLayerDrawable(context, C0006R$drawable.ic_bluetooth_connected, this.mBatteryLevel, this.mIconScale);
        }
    }

    /* access modifiers changed from: private */
    public class BluetoothConnectedTileIcon extends QSTile.Icon {
        BluetoothConnectedTileIcon(BluetoothTile bluetoothTile) {
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            return context.getDrawable(C0006R$drawable.ic_bluetooth_connected);
        }
    }

    /* access modifiers changed from: protected */
    public class BluetoothDetailAdapter implements DetailAdapter, QSDetailItems.Callback {
        private QSDetailItems mItems;

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getMetricsCategory() {
            return 150;
        }

        protected BluetoothDetailAdapter() {
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public CharSequence getTitle() {
            return ((QSTileImpl) BluetoothTile.this).mContext.getString(C0015R$string.quick_settings_bluetooth_label);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.BooleanState) ((QSTileImpl) BluetoothTile.this).mState).value);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public boolean getToggleEnabled() {
            return BluetoothTile.this.mController.getBluetoothState() == 10 || BluetoothTile.this.mController.getBluetoothState() == 12;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Intent getSettingsIntent() {
            return BluetoothTile.BLUETOOTH_SETTINGS;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public void setToggleState(boolean z) {
            MetricsLogger.action(((QSTileImpl) BluetoothTile.this).mContext, 154, z);
            BluetoothTile.this.mController.setBluetoothEnabled(z);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            QSDetailItems convertOrInflate = QSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems = convertOrInflate;
            convertOrInflate.setTagSuffix("Bluetooth");
            this.mItems.setCallback(this);
            updateItems();
            setItemsVisible(((QSTile.BooleanState) ((QSTileImpl) BluetoothTile.this).mState).value);
            return this.mItems;
        }

        public void setItemsVisible(boolean z) {
            QSDetailItems qSDetailItems = this.mItems;
            if (qSDetailItems != null) {
                qSDetailItems.setItemsVisible(z);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateItems() {
            if (this.mItems != null) {
                if (BluetoothTile.this.mController.isBluetoothEnabled()) {
                    this.mItems.setEmptyState(C0006R$drawable.ic_qs_bluetooth_detail_empty, C0015R$string.quick_settings_bluetooth_detail_empty_text);
                } else {
                    this.mItems.setEmptyState(C0006R$drawable.ic_qs_bluetooth_detail_empty, C0015R$string.bt_is_off);
                }
                ArrayList arrayList = new ArrayList();
                Collection<CachedBluetoothDevice> devices = BluetoothTile.this.mController.getDevices();
                if (devices != null) {
                    int i = 0;
                    int i2 = 0;
                    for (CachedBluetoothDevice cachedBluetoothDevice : devices) {
                        if (BluetoothTile.this.mController.getBondState(cachedBluetoothDevice) != 10) {
                            QSDetailItems.Item item = new QSDetailItems.Item();
                            item.iconResId = C0006R$drawable.op_ic_qs_bluetooth;
                            item.line1 = cachedBluetoothDevice.getName();
                            item.tag = cachedBluetoothDevice;
                            int maxConnectionState = cachedBluetoothDevice.getMaxConnectionState();
                            if (maxConnectionState == 2) {
                                item.iconResId = C0006R$drawable.ic_bluetooth_connected;
                                int batteryLevel = cachedBluetoothDevice.getBatteryLevel();
                                if (batteryLevel > -1) {
                                    item.icon = new BluetoothBatteryTileIcon(BluetoothTile.this, batteryLevel, 1.0f);
                                    item.line2 = ((QSTileImpl) BluetoothTile.this).mContext.getString(C0015R$string.quick_settings_connected_battery_level, Utils.formatPercentage(batteryLevel));
                                } else {
                                    item.line2 = ((QSTileImpl) BluetoothTile.this).mContext.getString(C0015R$string.quick_settings_connected);
                                }
                                item.canDisconnect = true;
                                arrayList.add(i, item);
                                i++;
                            } else if (maxConnectionState == 1) {
                                item.iconResId = C0006R$drawable.ic_qs_bluetooth_connecting;
                                item.line2 = ((QSTileImpl) BluetoothTile.this).mContext.getString(C0015R$string.quick_settings_connecting);
                                arrayList.add(i, item);
                            } else {
                                arrayList.add(item);
                            }
                            i2++;
                            if (i2 == 20) {
                                break;
                            }
                        }
                    }
                }
                this.mItems.setItems((QSDetailItems.Item[]) arrayList.toArray(new QSDetailItems.Item[arrayList.size()]));
            }
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemClick(QSDetailItems.Item item) {
            Object obj;
            CachedBluetoothDevice cachedBluetoothDevice;
            if (item != null && (obj = item.tag) != null && (cachedBluetoothDevice = (CachedBluetoothDevice) obj) != null && cachedBluetoothDevice.getMaxConnectionState() == 0) {
                BluetoothTile.this.mController.connect(cachedBluetoothDevice);
            }
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemDisconnect(QSDetailItems.Item item) {
            Object obj;
            CachedBluetoothDevice cachedBluetoothDevice;
            if (item != null && (obj = item.tag) != null && (cachedBluetoothDevice = (CachedBluetoothDevice) obj) != null) {
                BluetoothTile.this.mController.disconnect(cachedBluetoothDevice);
            }
        }
    }
}
