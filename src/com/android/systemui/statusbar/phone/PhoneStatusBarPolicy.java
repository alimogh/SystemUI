package com.android.systemui.statusbar.phone;

import android.app.ActivityTaskManager;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.notification.ZenModeConfig;
import android.telecom.TelecomManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.OpFeatures;
import androidx.lifecycle.Observer;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.PhoneStatusBarPolicy;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SensorPrivacyController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.ProductUtils;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.util.time.DateFormatUtil;
import com.oneplus.systemui.statusbar.phone.OpPhoneStatusBarPolicy;
import com.oneplus.worklife.OPWLBHelper;
import java.util.Locale;
import java.util.concurrent.Executor;
public class PhoneStatusBarPolicy extends OpPhoneStatusBarPolicy implements BluetoothController.Callback, CommandQueue.Callbacks, RotationLockController.RotationLockControllerCallback, DataSaverController.Listener, ZenModeController.Callback, DeviceProvisionedController.DeviceProvisionedListener, KeyguardStateController.Callback, LocationController.LocationChangeCallback, RecordingController.RecordingStateChangeCallback, OPWLBHelper.IStatusBarIconChangeListener {
    private static final boolean DEBUG = Log.isLoggable("PhoneStatusBarPolicy", 3);
    public static final int LOCATION_STATUS_ICON_ID = C0006R$drawable.op_perm_group_location;
    protected static final Handler mHandler = new Handler();
    private final AlarmManager mAlarmManager;
    private BluetoothController mBluetooth;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final CastController mCast;
    private final CastController.Callback mCastCallback = new CastController.Callback() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.3
        @Override // com.android.systemui.statusbar.policy.CastController.Callback
        public void onCastDevicesChanged() {
            PhoneStatusBarPolicy.this.updateCast();
        }
    };
    private final CommandQueue mCommandQueue;
    private boolean mCurrentUserSetup;
    private final DataSaverController mDataSaver;
    private final DateFormatUtil mDateFormatUtil;
    private final int mDisplayId;
    private final HotspotController mHotspot;
    private final HotspotController.Callback mHotspotCallback = new HotspotController.Callback() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.2
        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotChanged(boolean z, int i) {
            PhoneStatusBarPolicy.this.updateHotspotIcon(0, i);
            PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotHotspot, z);
        }

        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotChanged(boolean z, int i, int i2) {
            PhoneStatusBarPolicy.this.updateHotspotIcon(i2, i);
            PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotHotspot, z);
        }
    };
    private final IActivityManager mIActivityManager;
    private final StatusBarIconController mIconController;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.6
        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            switch (action.hashCode()) {
                case -1676458352:
                    if (action.equals("android.intent.action.HEADSET_PLUG")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1238404651:
                    if (action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -864107122:
                    if (action.equals("android.intent.action.MANAGED_PROFILE_AVAILABLE")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -229777127:
                    if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1051344550:
                    if (action.equals("android.telecom.action.CURRENT_TTY_MODE_CHANGED")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1051477093:
                    if (action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                intent.getBooleanExtra("rebroadcastOnUnlock", false);
            } else if (c == 1) {
                PhoneStatusBarPolicy.this.updateTTY(intent.getIntExtra("android.telecom.extra.CURRENT_TTY_MODE", 0));
            } else if (c == 2 || c == 3 || c == 4) {
                PhoneStatusBarPolicy.this.updateManagedProfile();
            } else if (c == 5) {
                PhoneStatusBarPolicy.this.updateHeadsetPlug(intent);
            }
        }
    };
    private final KeyguardStateController mKeyguardStateController;
    private final LocationController mLocationController;
    private boolean mManagedProfileIconVisible = false;
    private AlarmManager.AlarmClockInfo mNextAlarm;
    private final NextAlarmController.NextAlarmChangeCallback mNextAlarmCallback = new NextAlarmController.NextAlarmChangeCallback() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.4
        @Override // com.android.systemui.statusbar.policy.NextAlarmController.NextAlarmChangeCallback
        public void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo) {
            PhoneStatusBarPolicy.this.mNextAlarm = alarmClockInfo;
            PhoneStatusBarPolicy.this.updateAlarm();
        }
    };
    private final NextAlarmController mNextAlarmController;
    private final DeviceProvisionedController mProvisionedController;
    private final RecordingController mRecordingController;
    private Runnable mRemoveCastIconRunnable = new Runnable() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.7
        @Override // java.lang.Runnable
        public void run() {
            if (PhoneStatusBarPolicy.DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateCast: hiding icon NOW");
            }
            PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotCast, false);
        }
    };
    private final Resources mResources;
    private final RingerModeTracker mRingerModeTracker;
    private final RotationLockController mRotationLockController;
    private final SensorPrivacyController mSensorPrivacyController;
    private final SensorPrivacyController.OnSensorPrivacyChangedListener mSensorPrivacyListener = new SensorPrivacyController.OnSensorPrivacyChangedListener() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.5
        @Override // com.android.systemui.statusbar.policy.SensorPrivacyController.OnSensorPrivacyChangedListener
        public void onSensorPrivacyChanged(boolean z) {
            PhoneStatusBarPolicy.mHandler.post(new Runnable(z) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$5$UApHxsPG0BIvDnX5FCFYX6op1Fs
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    PhoneStatusBarPolicy.AnonymousClass5.this.lambda$onSensorPrivacyChanged$0$PhoneStatusBarPolicy$5(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSensorPrivacyChanged$0 */
        public /* synthetic */ void lambda$onSensorPrivacyChanged$0$PhoneStatusBarPolicy$5(boolean z) {
            PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotSensorsOff, z);
        }
    };
    private final String mSlotAlarmClock;
    private final String mSlotCast;
    private final String mSlotDataSaver;
    private final String mSlotHeadset;
    private final String mSlotHotspot;
    private final String mSlotLocation;
    private final String mSlotManagedProfile;
    private final String mSlotRotate;
    private final String mSlotScreenRecord;
    private final String mSlotSensorsOff;
    private final String mSlotTty;
    private final String mSlotVolume;
    private String mSlotWLB;
    private final String mSlotZen;
    private final TelecomManager mTelecomManager;
    private final Executor mUiBgExecutor;
    private final UserInfoController mUserInfoController;
    private final UserManager mUserManager;
    private final SynchronousUserSwitchObserver mUserSwitchListener = new SynchronousUserSwitchObserver() { // from class: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.1
        /* access modifiers changed from: private */
        /* renamed from: lambda$onUserSwitching$0 */
        public /* synthetic */ void lambda$onUserSwitching$0$PhoneStatusBarPolicy$1() {
            PhoneStatusBarPolicy.this.mUserInfoController.reloadUserInfo();
        }

        public void onUserSwitching(int i) throws RemoteException {
            PhoneStatusBarPolicy.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$1$4_BI5ieR2ylfAj9z5SwNfbqaqk4
                @Override // java.lang.Runnable
                public final void run() {
                    PhoneStatusBarPolicy.AnonymousClass1.this.lambda$onUserSwitching$0$PhoneStatusBarPolicy$1();
                }
            });
        }

        public void onUserSwitchComplete(int i) throws RemoteException {
            PhoneStatusBarPolicy.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$1$lONTSmykfPe64DIHRuLayVCRwlI
                @Override // java.lang.Runnable
                public final void run() {
                    PhoneStatusBarPolicy.AnonymousClass1.this.lambda$onUserSwitchComplete$1$PhoneStatusBarPolicy$1();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onUserSwitchComplete$1 */
        public /* synthetic */ void lambda$onUserSwitchComplete$1$PhoneStatusBarPolicy$1() {
            PhoneStatusBarPolicy.this.updateAlarm();
            PhoneStatusBarPolicy.this.updateManagedProfile();
        }
    };
    private boolean mVolumeVisible;
    private final ZenModeController mZenController;
    private boolean mZenVisible;

    public PhoneStatusBarPolicy(Context context, StatusBarIconController statusBarIconController, CommandQueue commandQueue, BroadcastDispatcher broadcastDispatcher, Executor executor, Resources resources, CastController castController, HotspotController hotspotController, BluetoothController bluetoothController, NextAlarmController nextAlarmController, UserInfoController userInfoController, RotationLockController rotationLockController, DataSaverController dataSaverController, ZenModeController zenModeController, DeviceProvisionedController deviceProvisionedController, KeyguardStateController keyguardStateController, LocationController locationController, SensorPrivacyController sensorPrivacyController, IActivityManager iActivityManager, AlarmManager alarmManager, UserManager userManager, RecordingController recordingController, TelecomManager telecomManager, int i, SharedPreferences sharedPreferences, DateFormatUtil dateFormatUtil, RingerModeTracker ringerModeTracker) {
        super(context, statusBarIconController, sharedPreferences);
        this.mIconController = statusBarIconController;
        this.mCommandQueue = commandQueue;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mResources = resources;
        this.mCast = castController;
        this.mHotspot = hotspotController;
        this.mBluetooth = bluetoothController;
        this.mNextAlarmController = nextAlarmController;
        this.mAlarmManager = alarmManager;
        this.mUserInfoController = userInfoController;
        this.mIActivityManager = iActivityManager;
        this.mUserManager = userManager;
        this.mRotationLockController = rotationLockController;
        this.mDataSaver = dataSaverController;
        this.mZenController = zenModeController;
        this.mProvisionedController = deviceProvisionedController;
        this.mKeyguardStateController = keyguardStateController;
        this.mLocationController = locationController;
        this.mSensorPrivacyController = sensorPrivacyController;
        this.mRecordingController = recordingController;
        this.mUiBgExecutor = executor;
        this.mTelecomManager = telecomManager;
        this.mRingerModeTracker = ringerModeTracker;
        this.mSlotCast = resources.getString(17041305);
        this.mSlotHotspot = resources.getString(17041313);
        resources.getString(17041303);
        this.mSlotTty = resources.getString(17041330);
        this.mSlotZen = resources.getString(17041336);
        this.mSlotVolume = resources.getString(17041332);
        this.mSlotAlarmClock = resources.getString(17041301);
        this.mSlotManagedProfile = resources.getString(17041316);
        this.mSlotRotate = resources.getString(17041323);
        this.mSlotHeadset = resources.getString(17041312);
        this.mSlotDataSaver = resources.getString(17041309);
        this.mSlotLocation = resources.getString(17041315);
        this.mSlotSensorsOff = resources.getString(17041326);
        this.mSlotScreenRecord = resources.getString(17041324);
        this.mDisplayId = i;
        this.mDateFormatUtil = dateFormatUtil;
        this.mSlotWLB = resources.getString(17041335);
    }

    public void init() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.telecom.action.CURRENT_TTY_MODE_CHANGED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mIntentReceiver, intentFilter, mHandler, UserHandle.ALL);
        $$Lambda$PhoneStatusBarPolicy$KdIGXKMKGALjz1ooREPEW2VYtAY r0 = new Observer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$KdIGXKMKGALjz1ooREPEW2VYtAY
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Object obj) {
                PhoneStatusBarPolicy.this.lambda$init$0$PhoneStatusBarPolicy((Integer) obj);
            }
        };
        this.mRingerModeTracker.getRingerMode().observeForever(r0);
        this.mRingerModeTracker.getRingerModeInternal().observeForever(r0);
        try {
            this.mIActivityManager.registerUserSwitchObserver(this.mUserSwitchListener, "PhoneStatusBarPolicy");
        } catch (RemoteException unused) {
        }
        updateTTY();
        updateBluetooth();
        this.mIconController.setIcon(this.mSlotAlarmClock, C0006R$drawable.stat_sys_alarm, null);
        this.mIconController.setIconVisibility(this.mSlotAlarmClock, false);
        this.mIconController.setIcon(this.mSlotZen, C0006R$drawable.stat_sys_dnd, null);
        this.mIconController.setIconVisibility(this.mSlotZen, false);
        this.mIconController.setIcon(this.mSlotVolume, C0006R$drawable.stat_sys_ringer_vibrate, null);
        this.mIconController.setIconVisibility(this.mSlotVolume, false);
        updateVolumeZen();
        this.mIconController.setIcon(this.mSlotCast, C0006R$drawable.stat_sys_cast, null);
        this.mIconController.setIconVisibility(this.mSlotCast, false);
        this.mIconController.setIcon(this.mSlotManagedProfile, C0006R$drawable.stat_sys_managed_profile_status, this.mResources.getString(C0015R$string.accessibility_managed_profile));
        this.mIconController.setIconVisibility(this.mSlotManagedProfile, this.mManagedProfileIconVisible);
        this.mIconController.setIcon(this.mSlotDataSaver, C0006R$drawable.stat_sys_data_saver, this.mResources.getString(C0015R$string.accessibility_data_saver_on));
        this.mIconController.setIconVisibility(this.mSlotDataSaver, false);
        this.mIconController.setIcon(this.mSlotLocation, LOCATION_STATUS_ICON_ID, this.mResources.getString(C0015R$string.accessibility_location_active));
        this.mIconController.setIconVisibility(this.mSlotLocation, false);
        this.mIconController.setIcon(this.mSlotSensorsOff, C0006R$drawable.stat_sys_sensors_off, this.mResources.getString(C0015R$string.accessibility_sensors_off_active));
        this.mIconController.setIconVisibility(this.mSlotSensorsOff, this.mSensorPrivacyController.isSensorPrivacyEnabled());
        this.mIconController.setIcon(this.mSlotScreenRecord, C0006R$drawable.stat_sys_screen_record, null);
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, false);
        this.mRotationLockController.addCallback(this);
        this.mBluetooth.addCallback(this);
        this.mProvisionedController.addCallback(this);
        this.mZenController.addCallback(this);
        this.mCast.addCallback(this.mCastCallback);
        this.mHotspot.addCallback(this.mHotspotCallback);
        this.mNextAlarmController.addCallback(this.mNextAlarmCallback);
        this.mDataSaver.addCallback(this);
        this.mKeyguardStateController.addCallback(this);
        this.mSensorPrivacyController.addCallback(this.mSensorPrivacyListener);
        this.mLocationController.addCallback(this);
        this.mRecordingController.addCallback((RecordingController.RecordingStateChangeCallback) this);
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        OPWLBHelper oPWLBHelper = (OPWLBHelper) Dependency.get(OPWLBHelper.class);
        oPWLBHelper.checkAndIncludeWLBTile();
        oPWLBHelper.registerStatusBarObserver(this);
        this.mIconController.setIcon(this.mSlotWLB, C0006R$drawable.stat_sys_wlb_mode, null);
        this.mIconController.setIconVisibility(this.mSlotWLB, oPWLBHelper.isWLBEnabled());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$init$0 */
    public /* synthetic */ void lambda$init$0$PhoneStatusBarPolicy(Integer num) {
        mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$Xfz5Yb18bNwaH_MFXZNINIQ7qpE
            @Override // java.lang.Runnable
            public final void run() {
                PhoneStatusBarPolicy.this.updateVolumeZen();
            }
        });
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onZenChanged(int i) {
        updateVolumeZen();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onConfigChanged(ZenModeConfig zenModeConfig) {
        updateVolumeZen();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAlarm() {
        int i;
        AlarmManager.AlarmClockInfo nextAlarmClock = this.mAlarmManager.getNextAlarmClock(-2);
        boolean z = true;
        boolean z2 = nextAlarmClock != null && nextAlarmClock.getTriggerTime() > 0;
        boolean z3 = this.mZenController.getZen() == 2;
        StatusBarIconController statusBarIconController = this.mIconController;
        String str = this.mSlotAlarmClock;
        if (z3) {
            i = C0006R$drawable.stat_sys_alarm_dim;
        } else {
            i = C0006R$drawable.stat_sys_alarm;
        }
        statusBarIconController.setIcon(str, i, buildAlarmContentDescription());
        StatusBarIconController statusBarIconController2 = this.mIconController;
        String str2 = this.mSlotAlarmClock;
        if (!this.mCurrentUserSetup || !z2) {
            z = false;
        }
        statusBarIconController2.setIconVisibility(str2, z);
    }

    private String buildAlarmContentDescription() {
        if (this.mNextAlarm == null) {
            return this.mResources.getString(C0015R$string.status_bar_alarm);
        }
        return this.mResources.getString(C0015R$string.accessibility_quick_settings_alarm, DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), this.mDateFormatUtil.is24HourFormat() ? "EHm" : "Ehma"), this.mNextAlarm.getTriggerTime()).toString());
    }

    /* access modifiers changed from: private */
    public final void updateVolumeZen() {
        OpPhoneStatusBarPolicy.OpUpdateVolumeZenObject updateVolumeZen = super.updateVolumeZen(this.mZenController.getZen());
        boolean z = updateVolumeZen.zenVisible;
        int i = updateVolumeZen.zenIconId;
        String str = updateVolumeZen.zenDescription;
        boolean z2 = updateVolumeZen.volumeVisible;
        int i2 = updateVolumeZen.volumeIconId;
        String str2 = updateVolumeZen.volumeDescription;
        if (z) {
            this.mIconController.setIcon(this.mSlotZen, i, str);
        }
        if (z != this.mZenVisible) {
            this.mIconController.setIconVisibility(this.mSlotZen, z);
            this.mZenVisible = z;
        }
        if (z2) {
            this.mIconController.setIcon(this.mSlotVolume, i2, str2);
        }
        if (z2 != this.mVolumeVisible) {
            this.mIconController.setIconVisibility(this.mSlotVolume, z2);
            this.mVolumeVisible = z2;
        }
        updateAlarm();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
    public void onBluetoothDevicesChanged() {
        updateBluetooth();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
    public void onBluetoothStateChange(boolean z) {
        updateBluetooth();
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
    public void onBluetoothActiveDeviceChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        updateBluetooth();
    }

    private final void updateBluetooth() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("PhoneStatusBarPolicy", "updateBluetooth");
        }
        super.OpUpdateBluetooth();
    }

    private final void updateTTY() {
        TelecomManager telecomManager = this.mTelecomManager;
        if (telecomManager == null) {
            updateTTY(0);
        } else {
            updateTTY(telecomManager.getCurrentTtyMode());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void updateTTY(int i) {
        boolean z = i != 0;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: enabled: " + z);
        }
        if (z) {
            if (DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY on");
            }
            this.mIconController.setIcon(this.mSlotTty, C0006R$drawable.stat_sys_tty_mode, this.mResources.getString(C0015R$string.accessibility_tty_enabled));
            this.mIconController.setIconVisibility(this.mSlotTty, true);
            return;
        }
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY off");
        }
        this.mIconController.setIconVisibility(this.mSlotTty, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x001e, code lost:
        r0 = true;
     */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0020 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:3:0x0011  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateCast() {
        /*
            r6 = this;
            com.android.systemui.statusbar.policy.CastController r0 = r6.mCast
            java.util.List r0 = r0.getCastDevices()
            java.util.Iterator r0 = r0.iterator()
        L_0x000a:
            boolean r1 = r0.hasNext()
            r2 = 1
            if (r1 == 0) goto L_0x0020
            java.lang.Object r1 = r0.next()
            com.android.systemui.statusbar.policy.CastController$CastDevice r1 = (com.android.systemui.statusbar.policy.CastController.CastDevice) r1
            int r1 = r1.state
            if (r1 == r2) goto L_0x001e
            r3 = 2
            if (r1 != r3) goto L_0x000a
        L_0x001e:
            r0 = r2
            goto L_0x0021
        L_0x0020:
            r0 = 0
        L_0x0021:
            boolean r1 = com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.DEBUG
            java.lang.String r3 = "PhoneStatusBarPolicy"
            if (r1 == 0) goto L_0x003c
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r4 = "updateCast: isCasting: "
            r1.append(r4)
            r1.append(r0)
            java.lang.String r1 = r1.toString()
            android.util.Log.v(r3, r1)
        L_0x003c:
            android.os.Handler r1 = com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.mHandler
            java.lang.Runnable r4 = r6.mRemoveCastIconRunnable
            r1.removeCallbacks(r4)
            if (r0 == 0) goto L_0x0066
            com.android.systemui.screenrecord.RecordingController r0 = r6.mRecordingController
            boolean r0 = r0.isRecording()
            if (r0 != 0) goto L_0x0066
            com.android.systemui.statusbar.phone.StatusBarIconController r0 = r6.mIconController
            java.lang.String r1 = r6.mSlotCast
            int r3 = com.android.systemui.C0006R$drawable.stat_sys_cast
            android.content.res.Resources r4 = r6.mResources
            int r5 = com.android.systemui.C0015R$string.accessibility_casting
            java.lang.String r4 = r4.getString(r5)
            r0.setIcon(r1, r3, r4)
            com.android.systemui.statusbar.phone.StatusBarIconController r0 = r6.mIconController
            java.lang.String r6 = r6.mSlotCast
            r0.setIconVisibility(r6, r2)
            goto L_0x0079
        L_0x0066:
            boolean r0 = com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.DEBUG
            if (r0 == 0) goto L_0x0070
            java.lang.String r0 = "updateCast: hiding icon in 3 sec..."
            android.util.Log.v(r3, r0)
        L_0x0070:
            android.os.Handler r0 = com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.mHandler
            java.lang.Runnable r6 = r6.mRemoveCastIconRunnable
            r1 = 3000(0xbb8, double:1.482E-320)
            r0.postDelayed(r6, r1)
        L_0x0079:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.updateCast():void");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateManagedProfile() {
        this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$w1c9js_99pYYTi-INgjnhsQltlU
            @Override // java.lang.Runnable
            public final void run() {
                PhoneStatusBarPolicy.this.lambda$updateManagedProfile$2$PhoneStatusBarPolicy();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateManagedProfile$2 */
    public /* synthetic */ void lambda$updateManagedProfile$2$PhoneStatusBarPolicy() {
        try {
            int lastResumedActivityUserId = ActivityTaskManager.getService().getLastResumedActivityUserId();
            mHandler.post(new Runnable(this.mUserManager.isManagedProfile(lastResumedActivityUserId), lastResumedActivityUserId) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$k5LzGn5vL350weYgt3sHE3wykZE
                public final /* synthetic */ boolean f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    PhoneStatusBarPolicy.this.lambda$updateManagedProfile$1$PhoneStatusBarPolicy(this.f$1, this.f$2);
                }
            });
        } catch (RemoteException e) {
            Log.w("PhoneStatusBarPolicy", "updateManagedProfile: ", e);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateManagedProfile$1 */
    public /* synthetic */ void lambda$updateManagedProfile$1$PhoneStatusBarPolicy(boolean z, int i) {
        boolean z2;
        if (!z || i == 999 || (this.mKeyguardStateController.isShowing() && !this.mKeyguardStateController.isOccluded())) {
            z2 = false;
        } else {
            z2 = true;
            this.mIconController.setIcon(this.mSlotManagedProfile, C0006R$drawable.stat_sys_managed_profile_status, this.mResources.getString(C0015R$string.accessibility_managed_profile));
        }
        if (this.mManagedProfileIconVisible != z2) {
            this.mIconController.setIconVisibility(this.mSlotManagedProfile, z2);
            this.mManagedProfileIconVisible = z2;
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void appTransitionStarting(int i, long j, long j2, boolean z) {
        if (this.mDisplayId == i) {
            updateManagedProfile();
        }
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onKeyguardShowingChanged() {
        updateManagedProfile();
    }

    @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
    public void onUserSetupChanged() {
        DeviceProvisionedController deviceProvisionedController = this.mProvisionedController;
        boolean isUserSetup = deviceProvisionedController.isUserSetup(deviceProvisionedController.getCurrentUser());
        if (this.mCurrentUserSetup != isUserSetup) {
            this.mCurrentUserSetup = isUserSetup;
            updateAlarm();
        }
    }

    @Override // com.android.systemui.statusbar.policy.RotationLockController.RotationLockControllerCallback
    public void onRotationLockStateChanged(boolean z, boolean z2) {
        boolean isCurrentOrientationLockPortrait = RotationLockTile.isCurrentOrientationLockPortrait(this.mRotationLockController, this.mResources);
        if (z) {
            if (isCurrentOrientationLockPortrait) {
                this.mIconController.setIcon(this.mSlotRotate, C0006R$drawable.stat_sys_rotate_portrait, this.mResources.getString(C0015R$string.accessibility_rotation_lock_on_portrait));
            } else {
                this.mIconController.setIcon(this.mSlotRotate, C0006R$drawable.stat_sys_rotate_landscape, this.mResources.getString(C0015R$string.accessibility_rotation_lock_on_landscape));
            }
            this.mIconController.setIconVisibility(this.mSlotRotate, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotRotate, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHeadsetPlug(Intent intent) {
        int i;
        int i2;
        boolean z = intent.getIntExtra("state", 0) != 0;
        boolean z2 = intent.getIntExtra("microphone", 0) != 0;
        Log.d("PhoneStatusBarPolicy", "receive ACTION_USBHEADSET_PLUG, connected:" + z + ", hasMic" + z2);
        if (z) {
            Resources resources = this.mResources;
            if (z2) {
                i = C0015R$string.accessibility_status_bar_headset;
            } else {
                i = C0015R$string.accessibility_status_bar_headphones;
            }
            String string = resources.getString(i);
            StatusBarIconController statusBarIconController = this.mIconController;
            String str = this.mSlotHeadset;
            if (z2) {
                i2 = C0006R$drawable.stat_sys_headset_mic;
            } else {
                i2 = C0006R$drawable.stat_sys_headset;
            }
            statusBarIconController.setIcon(str, i2, string);
            this.mIconController.setIconVisibility(this.mSlotHeadset, true);
            sendHeadsetNotify();
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotHeadset, false);
        cancelHeadsetNotify();
    }

    @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
    public void onDataSaverChanged(boolean z) {
        this.mIconController.setIconVisibility(this.mSlotDataSaver, z);
    }

    @Override // com.android.systemui.statusbar.policy.LocationController.LocationChangeCallback
    public void onLocationActiveChanged(boolean z) {
        updateLocation();
        Log.d("PhoneStatusBarPolicy", "onLocationActiveChanged, update status of location service: " + z);
    }

    private void updateLocation() {
        if (this.mLocationController.isLocationActive()) {
            this.mIconController.setIconVisibility(this.mSlotLocation, true);
        } else {
            this.mIconController.setIconVisibility(this.mSlotLocation, false);
        }
    }

    @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
    public void onCountdown(long j) {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: countdown " + j);
        }
        int floorDiv = (int) Math.floorDiv(j + 500, 1000);
        int i = C0006R$drawable.stat_sys_screen_record;
        String num = Integer.toString(floorDiv);
        if (floorDiv == 1) {
            i = C0006R$drawable.stat_sys_screen_record_1;
        } else if (floorDiv == 2) {
            i = C0006R$drawable.stat_sys_screen_record_2;
        } else if (floorDiv == 3) {
            i = C0006R$drawable.stat_sys_screen_record_3;
        }
        this.mIconController.setIcon(this.mSlotScreenRecord, i, num);
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, true);
        this.mIconController.setIconAccessibilityLiveRegion(this.mSlotScreenRecord, 2);
    }

    @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
    public void onCountdownEnd() {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: hiding icon during countdown");
        }
        mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$PHI8Z1W8Z96hbvbATx2ZJj0XaZQ
            @Override // java.lang.Runnable
            public final void run() {
                PhoneStatusBarPolicy.this.lambda$onCountdownEnd$3$PhoneStatusBarPolicy();
            }
        });
        mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$7mFArDgpnanqDUji3ufC10IThZA
            @Override // java.lang.Runnable
            public final void run() {
                PhoneStatusBarPolicy.this.lambda$onCountdownEnd$4$PhoneStatusBarPolicy();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCountdownEnd$3 */
    public /* synthetic */ void lambda$onCountdownEnd$3$PhoneStatusBarPolicy() {
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCountdownEnd$4 */
    public /* synthetic */ void lambda$onCountdownEnd$4$PhoneStatusBarPolicy() {
        this.mIconController.setIconAccessibilityLiveRegion(this.mSlotScreenRecord, 0);
    }

    @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
    public void onRecordingStart() {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: showing icon");
        }
        this.mIconController.setIcon(this.mSlotScreenRecord, C0006R$drawable.stat_sys_screen_record, this.mResources.getString(C0015R$string.screenrecord_ongoing_screen_only));
        mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$7Gelwg8Ag7IxnHM8BkaPNl8uo3o
            @Override // java.lang.Runnable
            public final void run() {
                PhoneStatusBarPolicy.this.lambda$onRecordingStart$5$PhoneStatusBarPolicy();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onRecordingStart$5 */
    public /* synthetic */ void lambda$onRecordingStart$5$PhoneStatusBarPolicy() {
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, true);
    }

    @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
    public void onRecordingEnd() {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: hiding icon");
        }
        mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$PhoneStatusBarPolicy$lQt3Uj0Z6eUPln3nxKWH-1F6RLQ
            @Override // java.lang.Runnable
            public final void run() {
                PhoneStatusBarPolicy.this.lambda$onRecordingEnd$6$PhoneStatusBarPolicy();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onRecordingEnd$6 */
    public /* synthetic */ void lambda$onRecordingEnd$6$PhoneStatusBarPolicy() {
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHotspotIcon(int i, int i2) {
        int i3;
        Log.d("PhoneStatusBarPolicy", "updateHotspotIcon standard:" + i + " numDevices:" + i2);
        if (ProductUtils.isUsvMode()) {
            switch (i2) {
                case 1:
                    i3 = C0006R$drawable.stat_sys_hotspot_1;
                    break;
                case 2:
                    i3 = C0006R$drawable.stat_sys_hotspot_2;
                    break;
                case 3:
                    i3 = C0006R$drawable.stat_sys_hotspot_3;
                    break;
                case 4:
                    i3 = C0006R$drawable.stat_sys_hotspot_4;
                    break;
                case 5:
                    i3 = C0006R$drawable.stat_sys_hotspot_5;
                    break;
                case 6:
                    i3 = C0006R$drawable.stat_sys_hotspot_6;
                    break;
                case 7:
                    i3 = C0006R$drawable.stat_sys_hotspot_7;
                    break;
                case 8:
                    i3 = C0006R$drawable.stat_sys_hotspot_8;
                    break;
                case 9:
                    i3 = C0006R$drawable.stat_sys_hotspot_9;
                    break;
                case 10:
                    i3 = C0006R$drawable.stat_sys_hotspot_10;
                    break;
                default:
                    i3 = C0006R$drawable.stat_sys_hotspot;
                    break;
            }
            this.mIconController.setIcon(this.mSlotHotspot, i3, this.mResources.getString(C0015R$string.accessibility_status_bar_hotspot));
        } else if (!OpFeatures.isSupport(new int[]{197})) {
            this.mIconController.setIcon(this.mSlotHotspot, C0006R$drawable.stat_sys_hotspot, this.mResources.getString(C0015R$string.accessibility_status_bar_hotspot));
            Log.d("PhoneStatusBarPolicy", "use default hotspot icon");
        } else if (i == 6) {
            this.mIconController.setIcon(this.mSlotHotspot, C0006R$drawable.stat_sys_wifi_6_hotspot, this.mResources.getString(C0015R$string.accessibility_status_bar_hotspot));
        } else if (i == 5) {
            this.mIconController.setIcon(this.mSlotHotspot, C0006R$drawable.stat_sys_wifi_5_hotspot, this.mResources.getString(C0015R$string.accessibility_status_bar_hotspot));
        } else if (i == 4) {
            this.mIconController.setIcon(this.mSlotHotspot, C0006R$drawable.stat_sys_wifi_4_hotspot, this.mResources.getString(C0015R$string.accessibility_status_bar_hotspot));
        } else {
            this.mIconController.setIcon(this.mSlotHotspot, C0006R$drawable.stat_sys_hotspot, this.mResources.getString(C0015R$string.accessibility_status_bar_hotspot));
        }
    }

    @Override // com.oneplus.worklife.OPWLBHelper.IStatusBarIconChangeListener
    public void onHideWLBStatusBarIcon() {
        this.mIconController.setIconVisibility(this.mSlotWLB, false);
    }

    @Override // com.oneplus.worklife.OPWLBHelper.IStatusBarIconChangeListener
    public void onShowWLBStatusBarIcon() {
        this.mIconController.setIconVisibility(this.mSlotWLB, true);
    }
}
