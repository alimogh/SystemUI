package com.oneplus.volume;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telecom.TelecomManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.ArrayUtils;
import com.android.settingslib.Utils;
import com.android.settingslib.bluetooth.A2dpProfile;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.BluetoothUtils;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.HeadsetProfile;
import com.android.settingslib.bluetooth.HearingAidProfile;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.volume.MediaRouterWrapper;
import com.android.systemui.volume.SystemUIInterpolators$LogAccelerateInterpolator;
import com.oneplus.util.OpUtils;
import com.oneplus.volume.OpOutputChooserLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
public class OpOutputChooserDialog extends Dialog implements DialogInterface.OnDismissListener, OpOutputChooserLayout.Callback {
    private static boolean DEBUG = Build.DEBUG_ONEPLUS;
    private AudioManager mAudioManager;
    private int mBgDrawable = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothCallbackHandler mBluetoothCallbackHandler;
    private final BluetoothController mBluetoothController;
    private BluetoothHeadset mBluetoothHeadset;
    private final BluetoothProfile.ServiceListener mBluetoothProfileServiceListener = new BluetoothProfile.ServiceListener() { // from class: com.oneplus.volume.OpOutputChooserDialog.1
        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceConnected(int i, BluetoothProfile bluetoothProfile) {
            Log.i("OpOutputChooserDialog", "BluetoothProfile.ServiceListener / onServiceConnected / profile:" + i + "  / proxy: " + bluetoothProfile);
            synchronized (this) {
                if (i == 1) {
                    OpOutputChooserDialog.this.mBluetoothHeadset = (BluetoothHeadset) bluetoothProfile;
                    Log.i("OpOutputChooserDialog", "- Got BluetoothHeadset: " + OpOutputChooserDialog.this.mBluetoothHeadset);
                } else {
                    Log.w("OpOutputChooserDialog", "Connected to non-headset bluetooth service. Not changing bluetooth headset.");
                }
            }
        }

        @Override // android.bluetooth.BluetoothProfile.ServiceListener
        public void onServiceDisconnected(int i) {
            Log.i("OpOutputChooserDialog", "BluetoothProfile.ServiceListener / onServiceDisconnected / profile:" + i);
            synchronized (this) {
                OpOutputChooserDialog.this.mBluetoothHeadset = null;
                Log.i("OpOutputChooserDialog", "Lost BluetoothHeadset service. Removing all tracked devices.");
            }
        }
    };
    private final BluetoothController.Callback mCallback = new BluetoothController.Callback() { // from class: com.oneplus.volume.OpOutputChooserDialog.3
        @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
        public void onBluetoothStateChange(boolean z) {
            OpOutputChooserDialog.this.updateItems(false);
        }

        @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
        public void onBluetoothDevicesChanged() {
            OpOutputChooserDialog.this.updateItems(false);
        }
    };
    private Runnable mCheckActiveDeviceRunnable = new Runnable() { // from class: com.oneplus.volume.OpOutputChooserDialog.8
        @Override // java.lang.Runnable
        public void run() {
            Log.i("OpOutputChooserDialog", " CheckActiveDevice again");
            OpOutputChooserDialog.this.mPreSelectDevice = null;
            OpOutputChooserDialog.this.updateItems(false);
        }
    };
    protected final List<BluetoothDevice> mConnectedDevices = new ArrayList();
    private final Context mContext;
    private final VolumeDialogController mController;
    private final VolumeDialogController.Callbacks mControllerCallbackH = new VolumeDialogController.Callbacks() { // from class: com.oneplus.volume.OpOutputChooserDialog.5
        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onAccessibilityModeChanged(Boolean bool) {
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onCaptionComponentStateChanged(Boolean bool, Boolean bool2) {
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onDismissRequested(int i) {
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onLayoutDirectionChanged(int i) {
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSafetyWarning(int i) {
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSilentHint() {
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowVibrateHint() {
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onStateChanged(VolumeDialogController.State state) {
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowRequested(int i) {
            OpOutputChooserDialog.this.dismiss();
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onScreenOff() {
            OpOutputChooserDialog.this.dismiss();
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onConfigurationChanged() {
            OpOutputChooserDialog.this.dismiss();
        }
    };
    private CachedBluetoothDevice mCurrentActiveDevice = null;
    private int mEmytyIconColor = 0;
    private final Handler mHandler = new Handler() { // from class: com.oneplus.volume.OpOutputChooserDialog.4
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                OpOutputChooserDialog.this.updateItems(((Boolean) message.obj).booleanValue());
            } else if (i == 2) {
                CachedBluetoothDevice cachedBluetoothDevice = (CachedBluetoothDevice) message.obj;
                OpOutputChooserDialog.this.setActiveBluetoothDevice(cachedBluetoothDevice);
                OpOutputChooserDialog.this.mPaddingActiveDevice = null;
                Log.i("OpOutputChooserDialog", "active the select device:" + cachedBluetoothDevice.getName());
            } else if (i == 3) {
                OpOutputChooserDialog.this.onDetailItemClick((OpOutputChooserLayout.Item) message.obj);
            }
        }
    };
    private String mHeadSetString = null;
    private int mIconColor = 0;
    private boolean mIsInCall;
    private long mLastDetailItemClickTime = 0;
    private long mLastUpdateTime;
    private LocalBluetoothManager mLocalBluetoothManager;
    private CachedBluetoothDevice mPaddingActiveDevice = null;
    private CachedBluetoothDevice mPreSelectDevice = null;
    private int mPrimaryTextColor = 0;
    protected LocalBluetoothProfileManager mProfileManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.oneplus.volume.OpOutputChooserDialog.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                if (OpOutputChooserDialog.DEBUG) {
                    Log.d("OpOutputChooserDialog", "Received ACTION_CLOSE_SYSTEM_DIALOGS");
                }
                OpOutputChooserDialog.this.cancel();
                OpOutputChooserDialog.this.cleanUp();
            } else if (intent.getAction().equals("android.intent.action.HEADSET_PLUG") || intent.getAction().equals("android.media.STREAM_DEVICES_CHANGED_ACTION")) {
                if (OpOutputChooserDialog.DEBUG) {
                    Log.d("OpOutputChooserDialog", "Received ACTION_HEADSET_PLUG");
                }
                OpOutputChooserDialog.this.updateItems(false);
            }
        }
    };
    private final MediaRouterWrapper mRouter;
    private final MediaRouterCallback mRouterCallback;
    private int mSecondaryTextColor = 0;
    private String mSpeakerString = null;
    private TelecomManager mTelecomManager;
    private OpOutputChooserLayout mView;
    private WifiManager mWifiManager;
    private WindowManager mWindowManager;

    private int getDefaultIndext() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public abstract void cleanUp();

    public OpOutputChooserDialog(Context context, MediaRouterWrapper mediaRouterWrapper) {
        super(context, C0016R$style.qs_theme);
        this.mContext = context;
        this.mBluetoothController = (BluetoothController) Dependency.get(BluetoothController.class);
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        TelecomManager telecomManager = (TelecomManager) context.getSystemService("telecom");
        this.mTelecomManager = telecomManager;
        this.mIsInCall = telecomManager.isInCall();
        this.mRouter = mediaRouterWrapper;
        this.mRouterCallback = new MediaRouterCallback();
        MediaRouteSelector.Builder builder = new MediaRouteSelector.Builder();
        builder.addControlCategory("android.media.intent.category.REMOTE_PLAYBACK");
        builder.build();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction("android.media.STREAM_DEVICES_CHANGED_ACTION");
        context.registerReceiver(this.mReceiver, intentFilter);
        this.mController = (VolumeDialogController) Dependency.get(VolumeDialogController.class);
        Window window = getWindow();
        window.requestFeature(1);
        window.setBackgroundDrawable(new ColorDrawable(0));
        window.clearFlags(65538);
        window.addFlags(17563944);
        window.setType(2020);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity = isLandscape() ? 21 : 19;
        window.setAttributes(attributes);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Dialog
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0011R$layout.output_chooser);
        setCanceledOnTouchOutside(true);
        setOnDismissListener(new DialogInterface.OnDismissListener() { // from class: com.oneplus.volume.-$$Lambda$BW-pj7NNgwJ6BtmLIoCptW843qk
            @Override // android.content.DialogInterface.OnDismissListener
            public final void onDismiss(DialogInterface dialogInterface) {
                OpOutputChooserDialog.this.onDismiss(dialogInterface);
            }
        });
        OpOutputChooserLayout opOutputChooserLayout = (OpOutputChooserLayout) findViewById(C0008R$id.output_chooser);
        this.mView = opOutputChooserLayout;
        opOutputChooserLayout.setCallback(this);
        updateTile();
        this.mContext.getDrawable(C0006R$drawable.ic_cast);
        this.mContext.getDrawable(C0006R$drawable.ic_tv);
        this.mContext.getDrawable(C0006R$drawable.ic_speaker);
        this.mContext.getDrawable(C0006R$drawable.ic_speaker_group);
        this.mHeadSetString = this.mContext.getResources().getString(C0015R$string.quick_settings_footer_audio_headset);
        this.mSpeakerString = this.mContext.getResources().getString(C0015R$string.quick_settings_footer_audio_speaker);
        this.mPreSelectDevice = null;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        FutureTask futureTask = new FutureTask(new Callable() { // from class: com.oneplus.volume.-$$Lambda$OpOutputChooserDialog$00NnWdkxXzsJW09jWG2oOcRbI_w
            @Override // java.util.concurrent.Callable
            public final Object call() {
                return OpOutputChooserDialog.this.lambda$onCreate$0$OpOutputChooserDialog();
            }
        });
        try {
            futureTask.run();
            this.mLocalBluetoothManager = (LocalBluetoothManager) futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.w("OpOutputChooserDialog", "Error getting LocalBluetoothManager.", e);
        }
        LocalBluetoothManager localBluetoothManager = this.mLocalBluetoothManager;
        if (localBluetoothManager == null) {
            Log.e("OpOutputChooserDialog", "Bluetooth is not supported on this device");
        } else {
            this.mProfileManager = localBluetoothManager.getProfileManager();
        }
        LocalBluetoothManager localBluetoothManager2 = this.mLocalBluetoothManager;
        if (localBluetoothManager2 != null) {
            localBluetoothManager2.setForegroundActivity(this.mContext);
            boolean z = !this.mWifiManager.isWifiEnabled();
            boolean z2 = !this.mBluetoothController.isBluetoothEnabled();
            if (z && z2) {
                this.mView.setEmptyState(getDisabledServicesMessage(z, z2));
            }
            this.mView.postDelayed(new Runnable() { // from class: com.oneplus.volume.-$$Lambda$OpOutputChooserDialog$dHDUhUBifSGxhUBYrqKcIMESrsE
                @Override // java.lang.Runnable
                public final void run() {
                    OpOutputChooserDialog.this.lambda$onCreate$1$OpOutputChooserDialog();
                }
            }, 5000);
            this.mBluetoothCallbackHandler = new BluetoothCallbackHandler();
            BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
            this.mBluetoothAdapter = defaultAdapter;
            if (defaultAdapter != null && defaultAdapter.getState() == 12) {
                this.mBluetoothAdapter.getProfileProxy(this.mContext, this.mBluetoothProfileServiceListener, 1);
            }
            this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$0 */
    public /* synthetic */ LocalBluetoothManager lambda$onCreate$0$OpOutputChooserDialog() throws Exception {
        return getLocalBtManager(this.mContext);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$1 */
    public /* synthetic */ void lambda$onCreate$1$OpOutputChooserDialog() {
        updateItems(true);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Dialog
    public void onStart() {
        super.onStart();
    }

    @Override // android.app.Dialog, android.view.Window.Callback
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mBluetoothController.addCallback(this.mCallback);
        this.mController.addCallback(this.mControllerCallbackH, this.mHandler);
        this.mLocalBluetoothManager.getEventManager().registerCallback(this.mBluetoothCallbackHandler);
    }

    @Override // android.app.Dialog, android.view.Window.Callback
    public void onDetachedFromWindow() {
        this.mRouter.removeCallback(this.mRouterCallback);
        this.mController.removeCallback(this.mControllerCallbackH);
        this.mBluetoothController.removeCallback(this.mCallback);
        this.mLocalBluetoothManager.getEventManager().unregisterCallback(this.mBluetoothCallbackHandler);
        this.mLocalBluetoothManager.setForegroundActivity(null);
        super.onDetachedFromWindow();
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        this.mContext.unregisterReceiver(this.mReceiver);
        cleanUp();
    }

    private void updateTile() {
        if (this.mIsInCall) {
            this.mView.setTitle(C0015R$string.output_calls_title);
        } else {
            this.mView.setTitle(C0015R$string.output_title);
        }
    }

    private void updateDialogLayout() {
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_output_chooser_dialog_panel_width);
        int dimensionPixelSize2 = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_output_chooser_dialog_panel_height_with_back_key);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int min = (int) (((float) Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels)) * 0.8f);
        ViewGroup.LayoutParams layoutParams = this.mView.getLayoutParams();
        if (isLandscape()) {
            layoutParams.height = Math.min(min, dimensionPixelSize2);
            layoutParams.width = dimensionPixelSize;
        } else {
            layoutParams.height = dimensionPixelSize2;
            layoutParams.width = Math.min(min, dimensionPixelSize);
        }
        this.mView.setLayoutParams(layoutParams);
    }

    @Override // android.app.Dialog
    public void show() {
        super.show();
        updateDialogLayout();
        ((MetricsLogger) Dependency.get(MetricsLogger.class)).visible(1295);
        this.mView.setTranslationX(getAnimTranslation());
        this.mView.setAlpha(0.0f);
        this.mView.animate().alpha(1.0f).translationX(0.0f).setDuration(300).setInterpolator(new SystemUIInterpolators$LogAccelerateInterpolator()).withEndAction(new Runnable() { // from class: com.oneplus.volume.-$$Lambda$OpOutputChooserDialog$S97eXojaikl-c1M20DRtR0WfIo4
            @Override // java.lang.Runnable
            public final void run() {
                OpOutputChooserDialog.this.lambda$show$2$OpOutputChooserDialog();
            }
        }).start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$show$2 */
    public /* synthetic */ void lambda$show$2$OpOutputChooserDialog() {
        getWindow().getDecorView().requestAccessibilityFocus();
    }

    @Override // android.app.Dialog, android.content.DialogInterface
    public void dismiss() {
        ((MetricsLogger) Dependency.get(MetricsLogger.class)).hidden(1295);
        this.mView.setTranslationX(0.0f);
        this.mView.setAlpha(1.0f);
        this.mView.animate().alpha(0.0f).translationX(getAnimTranslation()).setDuration(300).withEndAction(new Runnable() { // from class: com.oneplus.volume.-$$Lambda$OpOutputChooserDialog$uuC2AGIFmlmGmnd3aCbL124xDjw
            @Override // java.lang.Runnable
            public final void run() {
                OpOutputChooserDialog.this.lambda$dismiss$3$OpOutputChooserDialog();
            }
        }).setInterpolator(new SystemUIInterpolators$LogAccelerateInterpolator()).start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$dismiss$3 */
    public /* synthetic */ void lambda$dismiss$3$OpOutputChooserDialog() {
        super.dismiss();
    }

    private float getAnimTranslation() {
        float dimension = getContext().getResources().getDimension(C0005R$dimen.op_output_chooser_dialog_panel_width) / 2.0f;
        return isLandscape() ? dimension : -dimension;
    }

    @Override // com.oneplus.volume.OpOutputChooserLayout.Callback
    public void onDetailItemClick(OpOutputChooserLayout.Item item) {
        if (item == null || item.tag == null) {
            Log.i("OpOutputChooserDialog", "onDetailItemClick / item == null || item.tag == null");
        } else if (SystemClock.uptimeMillis() - this.mLastDetailItemClickTime < 500) {
            this.mHandler.removeMessages(3);
            Handler handler = this.mHandler;
            handler.sendMessageAtTime(handler.obtainMessage(3, 0, 0, item), this.mLastDetailItemClickTime + 500);
        } else {
            this.mLastDetailItemClickTime = SystemClock.uptimeMillis();
            Log.i("OpOutputChooserDialog", "onDetailItemClick:" + item.deviceType + " tag:" + item.tag);
            this.mPaddingActiveDevice = null;
            int i = item.deviceType;
            if (i == OpOutputChooserLayout.Item.DEVICE_TYPE_BT) {
                CachedBluetoothDevice cachedBluetoothDevice = (CachedBluetoothDevice) item.tag;
                if (cachedBluetoothDevice.getMaxConnectionState() == 0) {
                    ((MetricsLogger) Dependency.get(MetricsLogger.class)).action(1296);
                    this.mPaddingActiveDevice = cachedBluetoothDevice;
                    this.mBluetoothController.connect(cachedBluetoothDevice);
                } else {
                    this.mPreSelectDevice = cachedBluetoothDevice;
                    setActiveBluetoothDevice(cachedBluetoothDevice);
                }
            } else if (i == OpOutputChooserLayout.Item.DEVICE_TYPE_MEDIA_ROUTER) {
                MediaRouter.RouteInfo routeInfo = (MediaRouter.RouteInfo) item.tag;
                if (routeInfo.isEnabled()) {
                    ((MetricsLogger) Dependency.get(MetricsLogger.class)).action(1296);
                    routeInfo.select();
                }
            } else if (i == OpOutputChooserLayout.Item.DEVICE_TYPE_PHONE || i == OpOutputChooserLayout.Item.DEVICE_TYPE_HEADSET) {
                setActiveBluetoothDevice(null);
                this.mPreSelectDevice = null;
            }
            Log.d("OpOutputChooserDialog", "onDetailItemClick mPreSelectDevice:" + this.mPreSelectDevice + " mPaddingActiveDevice:" + this.mPaddingActiveDevice);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateItems(boolean z) {
        if (SystemClock.uptimeMillis() - this.mLastUpdateTime < 300) {
            this.mHandler.removeMessages(1);
            Handler handler = this.mHandler;
            handler.sendMessageAtTime(handler.obtainMessage(1, Boolean.valueOf(z)), this.mLastUpdateTime + 300);
            return;
        }
        this.mLastUpdateTime = SystemClock.uptimeMillis();
        if (this.mView != null) {
            ArrayList arrayList = new ArrayList();
            boolean z2 = !this.mWifiManager.isWifiEnabled();
            boolean isBluetoothEnabled = true ^ this.mBluetoothController.isBluetoothEnabled();
            if (!isBluetoothEnabled) {
                addBluetoothDevices(arrayList);
            }
            addPhoneDevices(arrayList);
            arrayList.sort(ItemComparator.sInstance);
            setSelecter(arrayList);
            if (arrayList.size() == 0 && z) {
                String string = this.mContext.getString(C0015R$string.output_none_found);
                if (z2 || isBluetoothEnabled) {
                    string = getDisabledServicesMessage(z2, isBluetoothEnabled);
                }
                this.mView.setEmptyState(string);
            }
            this.mView.setItems((OpOutputChooserLayout.Item[]) arrayList.toArray(new OpOutputChooserLayout.Item[arrayList.size()]));
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0026: APUT  (r2v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r3v1 java.lang.String) */
    private String getDisabledServicesMessage(boolean z, boolean z2) {
        String str;
        Context context = this.mContext;
        int i = C0015R$string.output_none_found_service_off;
        Object[] objArr = new Object[1];
        if (z && z2) {
            str = context.getString(C0015R$string.output_service_bt_wifi);
        } else if (z) {
            str = this.mContext.getString(C0015R$string.output_service_wifi);
        } else {
            str = this.mContext.getString(C0015R$string.output_service_bt);
        }
        objArr[0] = str;
        return context.getString(i, objArr);
    }

    private void addBluetoothDevices(List<OpOutputChooserLayout.Item> list) {
        int majorDeviceClass;
        this.mConnectedDevices.clear();
        Collection<CachedBluetoothDevice> devices = getDevices();
        if (devices != null) {
            int i = 0;
            int i2 = 0;
            for (CachedBluetoothDevice cachedBluetoothDevice : devices) {
                if (!(this.mBluetoothController.getBondState(cachedBluetoothDevice) == 10 || cachedBluetoothDevice == null)) {
                    if (!(cachedBluetoothDevice.getBtClass() == null || (majorDeviceClass = cachedBluetoothDevice.getBtClass().getMajorDeviceClass()) == 1024 || majorDeviceClass == 7936)) {
                        if (majorDeviceClass == 1792 && cachedBluetoothDevice.getDevice() != null) {
                            if (!ArrayUtils.contains(cachedBluetoothDevice.getDevice().getUuids(), BluetoothUuid.HFP) && !ArrayUtils.contains(cachedBluetoothDevice.getDevice().getUuids(), BluetoothUuid.A2DP_SINK)) {
                            }
                        }
                    }
                    OpOutputChooserLayout.Item item = new OpOutputChooserLayout.Item();
                    item.iconResId = C0006R$drawable.ic_qs_bluetooth_on;
                    item.line1 = cachedBluetoothDevice.getName();
                    item.tag = cachedBluetoothDevice;
                    item.deviceType = OpOutputChooserLayout.Item.DEVICE_TYPE_BT;
                    int maxConnectionState = cachedBluetoothDevice.getMaxConnectionState();
                    if (maxConnectionState == 2) {
                        item.iconResId = C0006R$drawable.ic_qs_bluetooth_connected;
                        int batteryLevel = cachedBluetoothDevice.getBatteryLevel();
                        if (batteryLevel != -1) {
                            item.icon = (Drawable) BluetoothUtils.getBtClassDrawableWithDescription(getContext(), cachedBluetoothDevice).first;
                            item.line2 = this.mContext.getString(C0015R$string.quick_settings_connected_battery_level, Utils.formatPercentage(batteryLevel));
                        } else {
                            item.line2 = this.mContext.getString(C0015R$string.quick_settings_connected);
                        }
                        item.canDisconnect = true;
                        list.add(i, item);
                        this.mConnectedDevices.add(cachedBluetoothDevice.getDevice());
                        if (this.mPaddingActiveDevice == cachedBluetoothDevice) {
                            Log.i("OpOutputChooserDialog", "The active device:" + cachedBluetoothDevice.getName());
                            Message message = new Message();
                            message.what = 2;
                            message.obj = this.mPaddingActiveDevice;
                            this.mHandler.sendMessage(message);
                        }
                        i++;
                    } else if (maxConnectionState == 1) {
                        item.iconResId = C0006R$drawable.ic_qs_bluetooth_connecting;
                        item.line2 = this.mContext.getString(C0015R$string.quick_settings_connecting);
                        list.add(i, item);
                    } else {
                        list.add(item);
                    }
                    i2++;
                    if (i2 == 10) {
                        return;
                    }
                }
            }
        }
    }

    private void addPhoneDevices(List<OpOutputChooserLayout.Item> list) {
        OpOutputChooserLayout.Item item = new OpOutputChooserLayout.Item();
        if (this.mAudioManager.isWiredHeadsetOn()) {
            item.line1 = this.mHeadSetString;
            item.iconResId = C0006R$drawable.ic_output_chooser_headset;
            item.deviceType = OpOutputChooserLayout.Item.DEVICE_TYPE_HEADSET;
        } else {
            item.line1 = this.mSpeakerString;
            item.iconResId = C0006R$drawable.ic_output_chooser_phone;
            item.deviceType = OpOutputChooserLayout.Item.DEVICE_TYPE_PHONE;
        }
        item.tag = Integer.valueOf(item.deviceType);
        item.canDisconnect = true;
        list.add(item);
    }

    private void setSelecter(List<OpOutputChooserLayout.Item> list) {
        if (list.size() > 0) {
            int defaultIndext = getDefaultIndext();
            BluetoothDevice findActiveDevice = findActiveDevice(3);
            int size = list.size();
            int i = -1;
            int i2 = -1;
            for (int i3 = 1; i3 < size; i3++) {
                if (list.get(i3).tag instanceof CachedBluetoothDevice) {
                    BluetoothDevice device = ((CachedBluetoothDevice) list.get(i3).tag).getDevice();
                    if (device.equals(findActiveDevice)) {
                        i = i3;
                    }
                    CachedBluetoothDevice cachedBluetoothDevice = this.mPreSelectDevice;
                    if (cachedBluetoothDevice != null && cachedBluetoothDevice.getDevice().equals(device)) {
                        i2 = i3;
                    }
                }
            }
            if (i != -1) {
                defaultIndext = i;
            } else if (i2 != -1) {
                defaultIndext = i2;
            }
            if (defaultIndext < list.size()) {
                Log.d("OpOutputChooserDialog", "activeDevice = " + findActiveDevice + " mPreSelectDevice:" + this.mPreSelectDevice + " selectedDeviceIndex:" + defaultIndext + " cehck:" + list.get(defaultIndext).tag);
                list.get(defaultIndext).selected = true;
                if (list.get(defaultIndext).tag instanceof CachedBluetoothDevice) {
                    this.mCurrentActiveDevice = (CachedBluetoothDevice) list.get(defaultIndext).tag;
                } else {
                    this.mCurrentActiveDevice = null;
                }
            }
        }
    }

    private final class MediaRouterCallback extends MediaRouter.Callback {
        private MediaRouterCallback() {
        }

        @Override // androidx.mediarouter.media.MediaRouter.Callback
        public void onRouteAdded(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            OpOutputChooserDialog.this.updateItems(false);
        }

        @Override // androidx.mediarouter.media.MediaRouter.Callback
        public void onRouteRemoved(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            OpOutputChooserDialog.this.updateItems(false);
        }

        @Override // androidx.mediarouter.media.MediaRouter.Callback
        public void onRouteChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            OpOutputChooserDialog.this.updateItems(false);
        }

        @Override // androidx.mediarouter.media.MediaRouter.Callback
        public void onRouteSelected(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            OpOutputChooserDialog.this.updateItems(false);
        }
    }

    /* access modifiers changed from: package-private */
    public static final class ItemComparator implements Comparator<OpOutputChooserLayout.Item> {
        public static final ItemComparator sInstance = new ItemComparator();

        ItemComparator() {
        }

        public int compare(OpOutputChooserLayout.Item item, OpOutputChooserLayout.Item item2) {
            boolean z = item.canDisconnect;
            boolean z2 = item2.canDisconnect;
            if (z != z2) {
                return Boolean.compare(z2, z);
            }
            int i = item.deviceType;
            int i2 = item2.deviceType;
            if (i != i2) {
                return Integer.compare(i, i2);
            }
            return item.line1.toString().compareToIgnoreCase(item2.line1.toString());
        }
    }

    public void setActiveBluetoothDevice(CachedBluetoothDevice cachedBluetoothDevice) {
        if (this.mProfileManager != null && this.mCurrentActiveDevice != cachedBluetoothDevice) {
            this.mCurrentActiveDevice = cachedBluetoothDevice;
            Log.i("OpOutputChooserDialog", "setActiveBluetoothDevice:" + cachedBluetoothDevice);
            if (cachedBluetoothDevice != null) {
                cachedBluetoothDevice.setActive();
            } else {
                A2dpProfile a2dpProfile = this.mProfileManager.getA2dpProfile();
                HeadsetProfile headsetProfile = this.mProfileManager.getHeadsetProfile();
                HearingAidProfile hearingAidProfile = this.mProfileManager.getHearingAidProfile();
                if (a2dpProfile != null) {
                    a2dpProfile.setActiveDevice(null);
                }
                if (headsetProfile != null) {
                    headsetProfile.setActiveDevice(null);
                }
                if (hearingAidProfile != null) {
                    hearingAidProfile.setActiveDevice(null);
                }
            }
            if (this.mBluetoothHeadset != null) {
                Log.i("OpOutputChooserDialog", "mBluetoothHeadset:" + this.mBluetoothHeadset + " / mBluetoothHeadset.isAudioOn():" + this.mBluetoothHeadset.isAudioOn());
                if (!this.mBluetoothHeadset.isAudioOn()) {
                    this.mBluetoothHeadset.connectAudio();
                }
            }
        }
    }

    public static LocalBluetoothManager getLocalBtManager(Context context) {
        return (LocalBluetoothManager) Dependency.get(LocalBluetoothManager.class);
    }

    /* access modifiers changed from: protected */
    public BluetoothDevice findActiveDevice(int i) {
        if (!(i == 3 || i == 0)) {
            return null;
        }
        if (isStreamFromOutputDevice(3, AudioSystem.DEVICE_OUT_ALL_A2DP_SET)) {
            Log.i("OpOutputChooserDialog", "StreamFrom output DEVICE_OUT_ALL_A2DP_SET:" + this.mProfileManager.getA2dpProfile().getActiveDevice() + " type:3");
            return this.mProfileManager.getA2dpProfile().getActiveDevice();
        } else if (isStreamFromOutputDevice(0, AudioSystem.DEVICE_OUT_ALL_SCO_SET)) {
            Log.i("OpOutputChooserDialog", "StreamFrom output DEVICE_OUT_ALL_SCO_SET:" + this.mProfileManager.getHeadsetProfile().getActiveDevice() + " type:0");
            return this.mProfileManager.getHeadsetProfile().getActiveDevice();
        } else {
            if (isStreamFromOutputDevice(i, 134217728)) {
                for (BluetoothDevice bluetoothDevice : this.mProfileManager.getHearingAidProfile().getActiveDevices()) {
                    if (bluetoothDevice != null && this.mConnectedDevices.contains(bluetoothDevice)) {
                        Log.i("OpOutputChooserDialog", "StreamFrom output DEVICE_OUT_HEARING_AID:" + bluetoothDevice + " type:" + i);
                        return bluetoothDevice;
                    }
                }
            }
            Log.i("OpOutputChooserDialog", "no active device");
            return null;
        }
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

    public void setTheme(int i) {
        int i2;
        boolean isREDVersion = OpUtils.isREDVersion();
        Resources resources = this.mContext.getResources();
        if (i != 1) {
            this.mIconColor = resources.getColor(C0004R$color.oneplus_contorl_icon_color_accent_active_light);
            this.mEmytyIconColor = resources.getColor(C0004R$color.oneplus_contorl_icon_color_active_light);
            this.mPrimaryTextColor = resources.getColor(C0004R$color.oneplus_contorl_text_color_primary_light);
            this.mSecondaryTextColor = resources.getColor(C0004R$color.oneplus_contorl_text_color_secondary_light);
            this.mBgDrawable = C0006R$drawable.volume_dialog_bg_light;
        } else {
            this.mIconColor = resources.getColor(isREDVersion ? C0004R$color.op_turquoise : C0004R$color.oneplus_contorl_icon_color_accent_active_dark);
            this.mEmytyIconColor = resources.getColor(C0004R$color.oneplus_contorl_icon_color_active_dark);
            this.mPrimaryTextColor = resources.getColor(C0004R$color.oneplus_contorl_text_color_primary_dark);
            this.mSecondaryTextColor = resources.getColor(C0004R$color.oneplus_contorl_text_color_secondary_dark);
            if (isREDVersion) {
                i2 = C0006R$drawable.volume_dialog_bg_red_dark;
            } else {
                i2 = C0006R$drawable.volume_dialog_bg_dark;
            }
            this.mBgDrawable = i2;
        }
        this.mView.setBackgroundDrawable(this.mContext.getResources().getDrawable(this.mBgDrawable));
        this.mView.setTitleColor(this.mSecondaryTextColor);
        this.mView.setEmptyIconColor(this.mEmytyIconColor);
        this.mView.setEmptyTextColor(this.mSecondaryTextColor);
        this.mView.setBackKeyColor(this.mIconColor);
        updateItems(false);
    }

    @Override // com.oneplus.volume.OpOutputChooserLayout.Callback
    public int getPrimaryTextColor() {
        return this.mPrimaryTextColor;
    }

    @Override // com.oneplus.volume.OpOutputChooserLayout.Callback
    public int getSecondaryTextColor() {
        return this.mSecondaryTextColor;
    }

    @Override // com.oneplus.volume.OpOutputChooserLayout.Callback
    public int getIconColor() {
        return this.mIconColor;
    }

    private final class BluetoothCallbackHandler implements BluetoothCallback {
        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onAudioModeChanged() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onBluetoothStateChanged(int i) {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onDeviceBondStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onScanningStateChanged(boolean z) {
        }

        private BluetoothCallbackHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onDeviceAdded(CachedBluetoothDevice cachedBluetoothDevice) {
            OpOutputChooserDialog.this.updateItems(false);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onDeviceDeleted(CachedBluetoothDevice cachedBluetoothDevice) {
            OpOutputChooserDialog.this.updateItems(false);
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onActiveDeviceChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
            Log.i("OpOutputChooserDialog", " onActiveDeviceChanged:" + cachedBluetoothDevice);
            OpOutputChooserDialog.this.mHandler.removeCallbacks(OpOutputChooserDialog.this.mCheckActiveDeviceRunnable);
            if (cachedBluetoothDevice == OpOutputChooserDialog.this.mPreSelectDevice) {
                OpOutputChooserDialog.this.mHandler.postDelayed(OpOutputChooserDialog.this.mCheckActiveDeviceRunnable, 3000);
            } else {
                OpOutputChooserDialog.this.mPreSelectDevice = null;
            }
            OpOutputChooserDialog.this.updateItems(false);
        }
    }

    private boolean isLandscape() {
        return this.mContext.getResources().getConfiguration().orientation == 2;
    }

    public Collection<CachedBluetoothDevice> getDevices() {
        LocalBluetoothManager localBluetoothManager = this.mLocalBluetoothManager;
        if (localBluetoothManager != null) {
            return localBluetoothManager.getCachedDeviceManager().getCachedDevicesCopy();
        }
        return null;
    }

    @Override // com.oneplus.volume.OpOutputChooserLayout.Callback
    public void backToVolumeDialog() {
        dismiss();
        VolumeDialogController volumeDialogController = this.mController;
        if (volumeDialogController != null) {
            volumeDialogController.showVolumeDialog(4);
        }
    }
}
