package com.oneplus.volume;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
public class OpOutputChooser {
    private static boolean DEBUG = Build.DEBUG_ONEPLUS;
    private AudioManager mAudioManager;
    private BluetoothCallbackHandler mBluetoothCallbackHandler;
    protected final List<BluetoothDevice> mConnectedDevices = new ArrayList();
    private final Context mContext;
    private String mHeadSetString = null;
    private LocalBluetoothManager mLocalBluetoothManager;
    private OutputChooserCallback mOutputChooserCallback;
    protected LocalBluetoothProfileManager mProfileManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.oneplus.volume.OpOutputChooser.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (OpOutputChooser.DEBUG) {
                Log.i("OpOutputChooser", "mReceiver, intent.getAction():" + intent.getAction());
            }
            if (!"android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                if (intent.getAction().equals("android.intent.action.HEADSET_PLUG")) {
                    boolean z = true;
                    boolean z2 = intent.getIntExtra("state", 0) != 0;
                    if (intent.getIntExtra("microphone", 0) == 0) {
                        z = false;
                    }
                    if (OpOutputChooser.DEBUG) {
                        Log.d("OpOutputChooser", "receive ACTION_USBHEADSET_PLUG, connected:" + z2 + ", hasMic" + z);
                    }
                    OpOutputChooser.this.updateItems(false);
                } else if (intent.getAction().equals("android.media.STREAM_DEVICES_CHANGED_ACTION")) {
                    OpOutputChooser.this.updateItems(false);
                }
            }
        }
    };
    private String mSpeakerString = null;

    /* renamed from: com.oneplus.volume.OpOutputChooser$1  reason: invalid class name */
    class AnonymousClass1 {
    }

    public static class OutputChooserCallback {
        public abstract void onOutputChooserNotifyActiveDeviceChange(int i, int i2, String str, String str2);
    }

    public void addCallback(OutputChooserCallback outputChooserCallback) {
        Log.i("OpOutputChooser", "OutputChooserCallback, addCallback");
        this.mOutputChooserCallback = outputChooserCallback;
        findActiveDevice(3);
    }

    public void removeCallback() {
        this.mOutputChooserCallback = null;
    }

    public OpOutputChooser(Context context) {
        Log.i("OpOutputChooser", "new OpOutputChooser");
        this.mContext = context;
        this.mHeadSetString = context.getResources().getString(C0015R$string.quick_settings_footer_audio_headset);
        this.mSpeakerString = this.mContext.getResources().getString(C0015R$string.quick_settings_footer_audio_speaker);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        FutureTask futureTask = new FutureTask(new Callable() { // from class: com.oneplus.volume.-$$Lambda$OpOutputChooser$cO6gGwOhaEUTIDCM4wwaNtdbqS4
            @Override // java.util.concurrent.Callable
            public final Object call() {
                return OpOutputChooser.this.lambda$new$0$OpOutputChooser();
            }
        });
        try {
            futureTask.run();
            this.mLocalBluetoothManager = (LocalBluetoothManager) futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.w("OpOutputChooser", "Error getting LocalBluetoothManager.", e);
        }
        LocalBluetoothManager localBluetoothManager = this.mLocalBluetoothManager;
        if (localBluetoothManager == null) {
            Log.e("OpOutputChooser", "Bluetooth is not supported on this device");
        } else {
            this.mProfileManager = localBluetoothManager.getProfileManager();
        }
        LocalBluetoothManager localBluetoothManager2 = this.mLocalBluetoothManager;
        if (localBluetoothManager2 != null) {
            localBluetoothManager2.setForegroundActivity(this.mContext);
            this.mBluetoothCallbackHandler = new BluetoothCallbackHandler(this, null);
            this.mLocalBluetoothManager.getEventManager().registerCallback(this.mBluetoothCallbackHandler);
            IntentFilter intentFilter = new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            intentFilter.addAction("android.intent.action.HEADSET_PLUG");
            intentFilter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ LocalBluetoothManager lambda$new$0$OpOutputChooser() throws Exception {
        return getLocalBtManager(this.mContext);
    }

    public void destory() {
        Log.i("OpOutputChooser", "destory OpOutputChooser");
        this.mLocalBluetoothManager.getEventManager().unregisterCallback(this.mBluetoothCallbackHandler);
        this.mLocalBluetoothManager.setForegroundActivity(null);
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public static LocalBluetoothManager getLocalBtManager(Context context) {
        return (LocalBluetoothManager) Dependency.get(LocalBluetoothManager.class);
    }

    private final class BluetoothCallbackHandler implements BluetoothCallback {
        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onAudioModeChanged() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onBluetoothStateChanged(int i) {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onDeviceBondStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onScanningStateChanged(boolean z) {
        }

        private BluetoothCallbackHandler() {
        }

        /* synthetic */ BluetoothCallbackHandler(OpOutputChooser opOutputChooser, AnonymousClass1 r2) {
            this();
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onDeviceAdded(CachedBluetoothDevice cachedBluetoothDevice) {
            if (OpOutputChooser.DEBUG) {
                Log.d("OpOutputChooser", "BluetoothCallbackHandler onDeviceAdded");
            }
            OpOutputChooser.this.updateItems(false);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onDeviceDeleted(CachedBluetoothDevice cachedBluetoothDevice) {
            if (OpOutputChooser.DEBUG) {
                Log.d("OpOutputChooser", "BluetoothCallbackHandler onDeviceDeleted");
            }
            OpOutputChooser.this.updateItems(false);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
            if (OpOutputChooser.DEBUG) {
                Log.d("OpOutputChooser", "BluetoothCallbackHandler onConnectionStateChanged, cachedDevice:" + cachedBluetoothDevice);
            }
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onActiveDeviceChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
            if (OpOutputChooser.DEBUG) {
                Log.d("OpOutputChooser", "BluetoothCallbackHandler onActiveDeviceChanged, activeDevice:" + cachedBluetoothDevice);
            }
            OpOutputChooser.this.updateItems(false);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0120  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0134  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0139  */
    /* JADX WARNING: Removed duplicated region for block: B:59:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void findActiveDevice(int r9) {
        /*
        // Method dump skipped, instructions count: 321
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.volume.OpOutputChooser.findActiveDevice(int):void");
    }

    /* access modifiers changed from: protected */
    public boolean isStreamFromOutputDevice(int i, int i2) {
        return (this.mAudioManager.getDevicesForStream(i) & i2) != 0;
    }

    public boolean isStreamFromOutputDevice(int i, Set<Integer> set) {
        for (Integer num : set) {
            if (isStreamFromOutputDevice(i, num.intValue())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateItems(boolean z) {
        findActiveDevice(3);
    }

    private int getIconResId(int i) {
        if (i == 0) {
            return C0006R$drawable.ic_output_chooser_phone;
        }
        if (i == 1) {
            return C0006R$drawable.ic_output_chooser_headset;
        }
        if (i != 2) {
            return 0;
        }
        return C0006R$drawable.ic_qs_bluetooth_connected;
    }
}
