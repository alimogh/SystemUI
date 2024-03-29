package com.android.systemui;

import android.app.AlarmManager;
import android.app.INotificationManager;
import android.app.IWallpaperManager;
import android.hardware.SensorPrivacyManager;
import android.hardware.display.NightDisplayListener;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.view.IWindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.Preconditions;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.clock.ClockManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.dock.DockManager;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.fragments.FragmentService;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.PluginDependencyProvider;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.power.EnhancedEstimates;
import com.android.systemui.power.PowerUI;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.Recents;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.DevicePolicyManagerWrapper;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.row.NotificationBlockingHelperManager;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.phone.AutoHideController;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarWindowController;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.SensorPrivacyController;
import com.android.systemui.statusbar.policy.SmartReplyConstants;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tracing.ProtoTracer;
import com.android.systemui.tuner.TunablePadding;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.leak.GarbageMonitor;
import com.android.systemui.util.leak.LeakDetector;
import com.android.systemui.util.leak.LeakReporter;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayImeController;
import com.android.systemui.wm.SystemWindows;
import com.oneplus.aod.utils.bitmoji.OpBitmojiHelper;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.battery.OpChargingAnimationController;
import com.oneplus.networkspeed.NetworkSpeedController;
import com.oneplus.notification.OpNotificationController;
import com.oneplus.opthreekey.OpThreekeyVolumeGuideController;
import com.oneplus.opzenmode.OpZenModeController;
import com.oneplus.scene.OpSceneModeObserver;
import com.oneplus.systemui.biometrics.OpBiometricDialogImpl;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintController;
import com.oneplus.util.OpUtils;
import com.oneplus.worklife.OPWLBHelper;
import dagger.Lazy;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
public class Dependency {
    public static final DependencyKey<Executor> BACKGROUND_EXECUTOR = new DependencyKey<>("background_executor");
    public static final DependencyKey<Looper> BG_LOOPER = new DependencyKey<>("background_looper");
    public static final DependencyKey<String> LEAK_REPORT_EMAIL = new DependencyKey<>("leak_report_email");
    public static final DependencyKey<Executor> MAIN_EXECUTOR = new DependencyKey<>("main_executor");
    public static final DependencyKey<Handler> MAIN_HANDLER = new DependencyKey<>("main_handler");
    public static final DependencyKey<Looper> MAIN_LOOPER = new DependencyKey<>("main_looper");
    public static final DependencyKey<Handler> TIME_TICK_HANDLER = new DependencyKey<>("time_tick_handler");
    private static Dependency sDependency;
    Lazy<AccessibilityController> mAccessibilityController;
    Lazy<AccessibilityManagerWrapper> mAccessibilityManagerWrapper;
    Lazy<ActivityManagerWrapper> mActivityManagerWrapper;
    Lazy<ActivityStarter> mActivityStarter;
    Lazy<AlarmManager> mAlarmManager;
    Lazy<AppOpsController> mAppOpsController;
    Lazy<AssistManager> mAssistManager;
    Lazy<AsyncSensorManager> mAsyncSensorManager;
    Lazy<AutoHideController> mAutoHideController;
    Lazy<Executor> mBackgroundExecutor;
    Lazy<BatteryController> mBatteryController;
    Lazy<Handler> mBgHandler;
    Lazy<Looper> mBgLooper;
    Lazy<BluetoothController> mBluetoothController;
    Lazy<BroadcastDispatcher> mBroadcastDispatcher;
    Lazy<BubbleController> mBubbleController;
    Lazy<CastController> mCastController;
    Lazy<ClockManager> mClockManager;
    Lazy<CommandQueue> mCommandQueue;
    Lazy<ConfigurationController> mConfigurationController;
    Lazy<DarkIconDispatcher> mDarkIconDispatcher;
    Lazy<DataSaverController> mDataSaverController;
    private final ArrayMap<Object, Object> mDependencies = new ArrayMap<>();
    Lazy<DevicePolicyManagerWrapper> mDevicePolicyManagerWrapper;
    Lazy<DeviceProvisionedController> mDeviceProvisionedController;
    Lazy<DisplayController> mDisplayController;
    Lazy<DisplayImeController> mDisplayImeController;
    Lazy<DisplayMetrics> mDisplayMetrics;
    Lazy<Divider> mDivider;
    Lazy<DockManager> mDockManager;
    Lazy<DozeParameters> mDozeParameters;
    DumpManager mDumpManager;
    Lazy<EnhancedEstimates> mEnhancedEstimates;
    Lazy<ExtensionController> mExtensionController;
    Lazy<FlashlightController> mFlashlightController;
    Lazy<ForegroundServiceController> mForegroundServiceController;
    Lazy<ForegroundServiceNotificationListener> mForegroundServiceNotificationListener;
    Lazy<FragmentService> mFragmentService;
    Lazy<GarbageMonitor> mGarbageMonitor;
    Lazy<HotspotController> mHotspotController;
    Lazy<INotificationManager> mINotificationManager;
    Lazy<IStatusBarService> mIStatusBarService;
    Lazy<IWindowManager> mIWindowManager;
    Lazy<KeyguardDismissUtil> mKeyguardDismissUtil;
    Lazy<NotificationEntryManager.KeyguardEnvironment> mKeyguardEnvironment;
    Lazy<KeyguardStateController> mKeyguardMonitor;
    Lazy<KeyguardSecurityModel> mKeyguardSecurityModel;
    Lazy<KeyguardUpdateMonitor> mKeyguardUpdateMonitor;
    Lazy<LeakDetector> mLeakDetector;
    Lazy<String> mLeakReportEmail;
    Lazy<LeakReporter> mLeakReporter;
    Lazy<LightBarController> mLightBarController;
    Lazy<LocalBluetoothManager> mLocalBluetoothManager;
    Lazy<LocationController> mLocationController;
    Lazy<LockscreenGestureLogger> mLockscreenGestureLogger;
    Lazy<Executor> mMainExecutor;
    Lazy<Handler> mMainHandler;
    Lazy<Looper> mMainLooper;
    Lazy<ManagedProfileController> mManagedProfileController;
    Lazy<MetricsLogger> mMetricsLogger;
    Lazy<NavigationModeController> mNavBarModeController;
    Lazy<NavigationBarController> mNavigationBarController;
    Lazy<NetworkController> mNetworkController;
    Lazy<NextAlarmController> mNextAlarmController;
    Lazy<NightDisplayListener> mNightDisplayListener;
    Lazy<NotificationBlockingHelperManager> mNotificationBlockingHelperManager;
    Lazy<NotificationEntryManager> mNotificationEntryManager;
    Lazy<NotificationFilter> mNotificationFilter;
    Lazy<NotificationGroupAlertTransferHelper> mNotificationGroupAlertTransferHelper;
    Lazy<NotificationGroupManager> mNotificationGroupManager;
    Lazy<NotificationGutsManager> mNotificationGutsManager;
    Lazy<NotificationListener> mNotificationListener;
    Lazy<NotificationLockscreenUserManager> mNotificationLockscreenUserManager;
    Lazy<NotificationLogger> mNotificationLogger;
    Lazy<NotificationMediaManager> mNotificationMediaManager;
    Lazy<NotificationRemoteInputManager> mNotificationRemoteInputManager;
    Lazy<NotificationRemoteInputManager.Callback> mNotificationRemoteInputManagerCallback;
    Lazy<NotificationShadeWindowController> mNotificationShadeWindowController;
    Lazy<NotificationViewHierarchyManager> mNotificationViewHierarchyManager;
    Lazy<OPWLBHelper> mOPWLBHelper;
    Lazy<OpBiometricDialogImpl> mOpBiometricDialogImpl;
    Lazy<OpBitmojiManager> mOpBitmojiManager;
    Lazy<OpChargingAnimationController> mOpChargingAnimationController;
    Lazy<OpHighlightHintController> mOpHighlightHintController;
    Lazy<NetworkSpeedController> mOpNetworkSpeedController;
    Lazy<OpNotificationController> mOpNotificationController;
    Lazy<OpSceneModeObserver> mOpSceneModeObserver;
    Lazy<OpThreekeyVolumeGuideController> mOpThreekeyVolumeGuideController;
    Lazy<OpZenModeController> mOpZenModeController;
    Lazy<OverviewProxyService> mOverviewProxyService;
    Lazy<PackageManagerWrapper> mPackageManagerWrapper;
    Lazy<PluginDependencyProvider> mPluginDependencyProvider;
    Lazy<PluginManager> mPluginManager;
    Lazy<ProtoTracer> mProtoTracer;
    private final ArrayMap<Object, LazyDependencyCreator> mProviders = new ArrayMap<>();
    Lazy<Recents> mRecents;
    Lazy<RecordingController> mRecordingController;
    Lazy<RemoteInputQuickSettingsDisabler> mRemoteInputQuickSettingsDisabler;
    Lazy<RotationLockController> mRotationLockController;
    Lazy<ScreenLifecycle> mScreenLifecycle;
    Lazy<SecurityController> mSecurityController;
    Lazy<SensorPrivacyController> mSensorPrivacyController;
    Lazy<SensorPrivacyManager> mSensorPrivacyManager;
    Lazy<ShadeController> mShadeController;
    Lazy<SmartReplyConstants> mSmartReplyConstants;
    Lazy<SmartReplyController> mSmartReplyController;
    Lazy<StatusBar> mStatusBar;
    Lazy<StatusBarIconController> mStatusBarIconController;
    Lazy<StatusBarStateController> mStatusBarStateController;
    Lazy<SysUiState> mSysUiStateFlagsContainer;
    Lazy<SystemWindows> mSystemWindows;
    Lazy<SysuiColorExtractor> mSysuiColorExtractor;
    Lazy<StatusBarWindowController> mTempStatusBarWindowController;
    Lazy<Handler> mTimeTickHandler;
    Lazy<TunablePadding.TunablePaddingService> mTunablePaddingService;
    Lazy<TunerService> mTunerService;
    Lazy<UiOffloadThread> mUiOffloadThread;
    Lazy<UserInfoController> mUserInfoController;
    Lazy<UserSwitcherController> mUserSwitcherController;
    Lazy<VibratorHelper> mVibratorHelper;
    Lazy<VisualStabilityManager> mVisualStabilityManager;
    Lazy<VolumeDialogController> mVolumeDialogController;
    Lazy<WakefulnessLifecycle> mWakefulnessLifecycle;
    Lazy<IWallpaperManager> mWallpaperManager;
    Lazy<PowerUI.WarningsUI> mWarningsUI;
    Lazy<ZenModeController> mZenModeController;

    public interface DependencyInjector {
        void createSystemUI(Dependency dependency);
    }

    /* access modifiers changed from: private */
    public interface LazyDependencyCreator<T> {
        T createDependency();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean autoRegisterModulesForDump() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void start() {
        ArrayMap<Object, LazyDependencyCreator> arrayMap = this.mProviders;
        DependencyKey<Handler> dependencyKey = TIME_TICK_HANDLER;
        Lazy<Handler> lazy = this.mTimeTickHandler;
        Objects.requireNonNull(lazy);
        arrayMap.put(dependencyKey, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap2 = this.mProviders;
        DependencyKey<Looper> dependencyKey2 = BG_LOOPER;
        Lazy<Looper> lazy2 = this.mBgLooper;
        Objects.requireNonNull(lazy2);
        arrayMap2.put(dependencyKey2, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap3 = this.mProviders;
        DependencyKey<Looper> dependencyKey3 = MAIN_LOOPER;
        Lazy<Looper> lazy3 = this.mMainLooper;
        Objects.requireNonNull(lazy3);
        arrayMap3.put(dependencyKey3, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap4 = this.mProviders;
        DependencyKey<Handler> dependencyKey4 = MAIN_HANDLER;
        Lazy<Handler> lazy4 = this.mMainHandler;
        Objects.requireNonNull(lazy4);
        arrayMap4.put(dependencyKey4, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap5 = this.mProviders;
        DependencyKey<Executor> dependencyKey5 = MAIN_EXECUTOR;
        Lazy<Executor> lazy5 = this.mMainExecutor;
        Objects.requireNonNull(lazy5);
        arrayMap5.put(dependencyKey5, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap6 = this.mProviders;
        DependencyKey<Executor> dependencyKey6 = BACKGROUND_EXECUTOR;
        Lazy<Executor> lazy6 = this.mBackgroundExecutor;
        Objects.requireNonNull(lazy6);
        arrayMap6.put(dependencyKey6, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap7 = this.mProviders;
        Lazy<ActivityStarter> lazy7 = this.mActivityStarter;
        Objects.requireNonNull(lazy7);
        arrayMap7.put(ActivityStarter.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap8 = this.mProviders;
        Lazy<BroadcastDispatcher> lazy8 = this.mBroadcastDispatcher;
        Objects.requireNonNull(lazy8);
        arrayMap8.put(BroadcastDispatcher.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap9 = this.mProviders;
        Lazy<AsyncSensorManager> lazy9 = this.mAsyncSensorManager;
        Objects.requireNonNull(lazy9);
        arrayMap9.put(AsyncSensorManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap10 = this.mProviders;
        Lazy<BluetoothController> lazy10 = this.mBluetoothController;
        Objects.requireNonNull(lazy10);
        arrayMap10.put(BluetoothController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap11 = this.mProviders;
        Lazy<SensorPrivacyManager> lazy11 = this.mSensorPrivacyManager;
        Objects.requireNonNull(lazy11);
        arrayMap11.put(SensorPrivacyManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap12 = this.mProviders;
        Lazy<LocationController> lazy12 = this.mLocationController;
        Objects.requireNonNull(lazy12);
        arrayMap12.put(LocationController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap13 = this.mProviders;
        Lazy<RotationLockController> lazy13 = this.mRotationLockController;
        Objects.requireNonNull(lazy13);
        arrayMap13.put(RotationLockController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap14 = this.mProviders;
        Lazy<NetworkController> lazy14 = this.mNetworkController;
        Objects.requireNonNull(lazy14);
        arrayMap14.put(NetworkController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap15 = this.mProviders;
        Lazy<ZenModeController> lazy15 = this.mZenModeController;
        Objects.requireNonNull(lazy15);
        arrayMap15.put(ZenModeController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap16 = this.mProviders;
        Lazy<HotspotController> lazy16 = this.mHotspotController;
        Objects.requireNonNull(lazy16);
        arrayMap16.put(HotspotController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap17 = this.mProviders;
        Lazy<CastController> lazy17 = this.mCastController;
        Objects.requireNonNull(lazy17);
        arrayMap17.put(CastController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap18 = this.mProviders;
        Lazy<FlashlightController> lazy18 = this.mFlashlightController;
        Objects.requireNonNull(lazy18);
        arrayMap18.put(FlashlightController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap19 = this.mProviders;
        Lazy<KeyguardStateController> lazy19 = this.mKeyguardMonitor;
        Objects.requireNonNull(lazy19);
        arrayMap19.put(KeyguardStateController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap20 = this.mProviders;
        Lazy<KeyguardUpdateMonitor> lazy20 = this.mKeyguardUpdateMonitor;
        Objects.requireNonNull(lazy20);
        arrayMap20.put(KeyguardUpdateMonitor.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap21 = this.mProviders;
        Lazy<UserSwitcherController> lazy21 = this.mUserSwitcherController;
        Objects.requireNonNull(lazy21);
        arrayMap21.put(UserSwitcherController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap22 = this.mProviders;
        Lazy<UserInfoController> lazy22 = this.mUserInfoController;
        Objects.requireNonNull(lazy22);
        arrayMap22.put(UserInfoController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap23 = this.mProviders;
        Lazy<BatteryController> lazy23 = this.mBatteryController;
        Objects.requireNonNull(lazy23);
        arrayMap23.put(BatteryController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap24 = this.mProviders;
        Lazy<NightDisplayListener> lazy24 = this.mNightDisplayListener;
        Objects.requireNonNull(lazy24);
        arrayMap24.put(NightDisplayListener.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap25 = this.mProviders;
        Lazy<ManagedProfileController> lazy25 = this.mManagedProfileController;
        Objects.requireNonNull(lazy25);
        arrayMap25.put(ManagedProfileController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap26 = this.mProviders;
        Lazy<NextAlarmController> lazy26 = this.mNextAlarmController;
        Objects.requireNonNull(lazy26);
        arrayMap26.put(NextAlarmController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap27 = this.mProviders;
        Lazy<DataSaverController> lazy27 = this.mDataSaverController;
        Objects.requireNonNull(lazy27);
        arrayMap27.put(DataSaverController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap28 = this.mProviders;
        Lazy<AccessibilityController> lazy28 = this.mAccessibilityController;
        Objects.requireNonNull(lazy28);
        arrayMap28.put(AccessibilityController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap29 = this.mProviders;
        Lazy<DeviceProvisionedController> lazy29 = this.mDeviceProvisionedController;
        Objects.requireNonNull(lazy29);
        arrayMap29.put(DeviceProvisionedController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap30 = this.mProviders;
        Lazy<PluginManager> lazy30 = this.mPluginManager;
        Objects.requireNonNull(lazy30);
        arrayMap30.put(PluginManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap31 = this.mProviders;
        Lazy<AssistManager> lazy31 = this.mAssistManager;
        Objects.requireNonNull(lazy31);
        arrayMap31.put(AssistManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap32 = this.mProviders;
        Lazy<SecurityController> lazy32 = this.mSecurityController;
        Objects.requireNonNull(lazy32);
        arrayMap32.put(SecurityController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap33 = this.mProviders;
        Lazy<LeakDetector> lazy33 = this.mLeakDetector;
        Objects.requireNonNull(lazy33);
        arrayMap33.put(LeakDetector.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap34 = this.mProviders;
        DependencyKey<String> dependencyKey7 = LEAK_REPORT_EMAIL;
        Lazy<String> lazy34 = this.mLeakReportEmail;
        Objects.requireNonNull(lazy34);
        arrayMap34.put(dependencyKey7, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap35 = this.mProviders;
        Lazy<LeakReporter> lazy35 = this.mLeakReporter;
        Objects.requireNonNull(lazy35);
        arrayMap35.put(LeakReporter.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap36 = this.mProviders;
        Lazy<GarbageMonitor> lazy36 = this.mGarbageMonitor;
        Objects.requireNonNull(lazy36);
        arrayMap36.put(GarbageMonitor.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap37 = this.mProviders;
        Lazy<TunerService> lazy37 = this.mTunerService;
        Objects.requireNonNull(lazy37);
        arrayMap37.put(TunerService.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap38 = this.mProviders;
        Lazy<NotificationShadeWindowController> lazy38 = this.mNotificationShadeWindowController;
        Objects.requireNonNull(lazy38);
        arrayMap38.put(NotificationShadeWindowController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap39 = this.mProviders;
        Lazy<StatusBarWindowController> lazy39 = this.mTempStatusBarWindowController;
        Objects.requireNonNull(lazy39);
        arrayMap39.put(StatusBarWindowController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap40 = this.mProviders;
        Lazy<DarkIconDispatcher> lazy40 = this.mDarkIconDispatcher;
        Objects.requireNonNull(lazy40);
        arrayMap40.put(DarkIconDispatcher.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap41 = this.mProviders;
        Lazy<ConfigurationController> lazy41 = this.mConfigurationController;
        Objects.requireNonNull(lazy41);
        arrayMap41.put(ConfigurationController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap42 = this.mProviders;
        Lazy<StatusBarIconController> lazy42 = this.mStatusBarIconController;
        Objects.requireNonNull(lazy42);
        arrayMap42.put(StatusBarIconController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap43 = this.mProviders;
        Lazy<ScreenLifecycle> lazy43 = this.mScreenLifecycle;
        Objects.requireNonNull(lazy43);
        arrayMap43.put(ScreenLifecycle.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap44 = this.mProviders;
        Lazy<WakefulnessLifecycle> lazy44 = this.mWakefulnessLifecycle;
        Objects.requireNonNull(lazy44);
        arrayMap44.put(WakefulnessLifecycle.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap45 = this.mProviders;
        Lazy<FragmentService> lazy45 = this.mFragmentService;
        Objects.requireNonNull(lazy45);
        arrayMap45.put(FragmentService.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap46 = this.mProviders;
        Lazy<ExtensionController> lazy46 = this.mExtensionController;
        Objects.requireNonNull(lazy46);
        arrayMap46.put(ExtensionController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap47 = this.mProviders;
        Lazy<PluginDependencyProvider> lazy47 = this.mPluginDependencyProvider;
        Objects.requireNonNull(lazy47);
        arrayMap47.put(PluginDependencyProvider.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap48 = this.mProviders;
        Lazy<LocalBluetoothManager> lazy48 = this.mLocalBluetoothManager;
        Objects.requireNonNull(lazy48);
        arrayMap48.put(LocalBluetoothManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap49 = this.mProviders;
        Lazy<VolumeDialogController> lazy49 = this.mVolumeDialogController;
        Objects.requireNonNull(lazy49);
        arrayMap49.put(VolumeDialogController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap50 = this.mProviders;
        Lazy<MetricsLogger> lazy50 = this.mMetricsLogger;
        Objects.requireNonNull(lazy50);
        arrayMap50.put(MetricsLogger.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap51 = this.mProviders;
        Lazy<AccessibilityManagerWrapper> lazy51 = this.mAccessibilityManagerWrapper;
        Objects.requireNonNull(lazy51);
        arrayMap51.put(AccessibilityManagerWrapper.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap52 = this.mProviders;
        Lazy<SysuiColorExtractor> lazy52 = this.mSysuiColorExtractor;
        Objects.requireNonNull(lazy52);
        arrayMap52.put(SysuiColorExtractor.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap53 = this.mProviders;
        Lazy<TunablePadding.TunablePaddingService> lazy53 = this.mTunablePaddingService;
        Objects.requireNonNull(lazy53);
        arrayMap53.put(TunablePadding.TunablePaddingService.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap54 = this.mProviders;
        Lazy<ForegroundServiceController> lazy54 = this.mForegroundServiceController;
        Objects.requireNonNull(lazy54);
        arrayMap54.put(ForegroundServiceController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap55 = this.mProviders;
        Lazy<UiOffloadThread> lazy55 = this.mUiOffloadThread;
        Objects.requireNonNull(lazy55);
        arrayMap55.put(UiOffloadThread.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap56 = this.mProviders;
        Lazy<PowerUI.WarningsUI> lazy56 = this.mWarningsUI;
        Objects.requireNonNull(lazy56);
        arrayMap56.put(PowerUI.WarningsUI.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap57 = this.mProviders;
        Lazy<LightBarController> lazy57 = this.mLightBarController;
        Objects.requireNonNull(lazy57);
        arrayMap57.put(LightBarController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap58 = this.mProviders;
        Lazy<IWindowManager> lazy58 = this.mIWindowManager;
        Objects.requireNonNull(lazy58);
        arrayMap58.put(IWindowManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap59 = this.mProviders;
        Lazy<OverviewProxyService> lazy59 = this.mOverviewProxyService;
        Objects.requireNonNull(lazy59);
        arrayMap59.put(OverviewProxyService.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap60 = this.mProviders;
        Lazy<NavigationModeController> lazy60 = this.mNavBarModeController;
        Objects.requireNonNull(lazy60);
        arrayMap60.put(NavigationModeController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap61 = this.mProviders;
        Lazy<EnhancedEstimates> lazy61 = this.mEnhancedEstimates;
        Objects.requireNonNull(lazy61);
        arrayMap61.put(EnhancedEstimates.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap62 = this.mProviders;
        Lazy<VibratorHelper> lazy62 = this.mVibratorHelper;
        Objects.requireNonNull(lazy62);
        arrayMap62.put(VibratorHelper.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap63 = this.mProviders;
        Lazy<IStatusBarService> lazy63 = this.mIStatusBarService;
        Objects.requireNonNull(lazy63);
        arrayMap63.put(IStatusBarService.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap64 = this.mProviders;
        Lazy<DisplayMetrics> lazy64 = this.mDisplayMetrics;
        Objects.requireNonNull(lazy64);
        arrayMap64.put(DisplayMetrics.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap65 = this.mProviders;
        Lazy<LockscreenGestureLogger> lazy65 = this.mLockscreenGestureLogger;
        Objects.requireNonNull(lazy65);
        arrayMap65.put(LockscreenGestureLogger.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap66 = this.mProviders;
        Lazy<NotificationEntryManager.KeyguardEnvironment> lazy66 = this.mKeyguardEnvironment;
        Objects.requireNonNull(lazy66);
        arrayMap66.put(NotificationEntryManager.KeyguardEnvironment.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap67 = this.mProviders;
        Lazy<ShadeController> lazy67 = this.mShadeController;
        Objects.requireNonNull(lazy67);
        arrayMap67.put(ShadeController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap68 = this.mProviders;
        Lazy<NotificationRemoteInputManager.Callback> lazy68 = this.mNotificationRemoteInputManagerCallback;
        Objects.requireNonNull(lazy68);
        arrayMap68.put(NotificationRemoteInputManager.Callback.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap69 = this.mProviders;
        Lazy<AppOpsController> lazy69 = this.mAppOpsController;
        Objects.requireNonNull(lazy69);
        arrayMap69.put(AppOpsController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap70 = this.mProviders;
        Lazy<NavigationBarController> lazy70 = this.mNavigationBarController;
        Objects.requireNonNull(lazy70);
        arrayMap70.put(NavigationBarController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap71 = this.mProviders;
        Lazy<StatusBarStateController> lazy71 = this.mStatusBarStateController;
        Objects.requireNonNull(lazy71);
        arrayMap71.put(StatusBarStateController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap72 = this.mProviders;
        Lazy<NotificationLockscreenUserManager> lazy72 = this.mNotificationLockscreenUserManager;
        Objects.requireNonNull(lazy72);
        arrayMap72.put(NotificationLockscreenUserManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap73 = this.mProviders;
        Lazy<VisualStabilityManager> lazy73 = this.mVisualStabilityManager;
        Objects.requireNonNull(lazy73);
        arrayMap73.put(VisualStabilityManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap74 = this.mProviders;
        Lazy<NotificationGroupManager> lazy74 = this.mNotificationGroupManager;
        Objects.requireNonNull(lazy74);
        arrayMap74.put(NotificationGroupManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap75 = this.mProviders;
        Lazy<NotificationGroupAlertTransferHelper> lazy75 = this.mNotificationGroupAlertTransferHelper;
        Objects.requireNonNull(lazy75);
        arrayMap75.put(NotificationGroupAlertTransferHelper.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap76 = this.mProviders;
        Lazy<NotificationMediaManager> lazy76 = this.mNotificationMediaManager;
        Objects.requireNonNull(lazy76);
        arrayMap76.put(NotificationMediaManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap77 = this.mProviders;
        Lazy<NotificationGutsManager> lazy77 = this.mNotificationGutsManager;
        Objects.requireNonNull(lazy77);
        arrayMap77.put(NotificationGutsManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap78 = this.mProviders;
        Lazy<NotificationBlockingHelperManager> lazy78 = this.mNotificationBlockingHelperManager;
        Objects.requireNonNull(lazy78);
        arrayMap78.put(NotificationBlockingHelperManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap79 = this.mProviders;
        Lazy<NotificationRemoteInputManager> lazy79 = this.mNotificationRemoteInputManager;
        Objects.requireNonNull(lazy79);
        arrayMap79.put(NotificationRemoteInputManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap80 = this.mProviders;
        Lazy<SmartReplyConstants> lazy80 = this.mSmartReplyConstants;
        Objects.requireNonNull(lazy80);
        arrayMap80.put(SmartReplyConstants.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap81 = this.mProviders;
        Lazy<NotificationListener> lazy81 = this.mNotificationListener;
        Objects.requireNonNull(lazy81);
        arrayMap81.put(NotificationListener.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap82 = this.mProviders;
        Lazy<NotificationLogger> lazy82 = this.mNotificationLogger;
        Objects.requireNonNull(lazy82);
        arrayMap82.put(NotificationLogger.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap83 = this.mProviders;
        Lazy<NotificationViewHierarchyManager> lazy83 = this.mNotificationViewHierarchyManager;
        Objects.requireNonNull(lazy83);
        arrayMap83.put(NotificationViewHierarchyManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap84 = this.mProviders;
        Lazy<NotificationFilter> lazy84 = this.mNotificationFilter;
        Objects.requireNonNull(lazy84);
        arrayMap84.put(NotificationFilter.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap85 = this.mProviders;
        Lazy<KeyguardDismissUtil> lazy85 = this.mKeyguardDismissUtil;
        Objects.requireNonNull(lazy85);
        arrayMap85.put(KeyguardDismissUtil.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap86 = this.mProviders;
        Lazy<SmartReplyController> lazy86 = this.mSmartReplyController;
        Objects.requireNonNull(lazy86);
        arrayMap86.put(SmartReplyController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap87 = this.mProviders;
        Lazy<RemoteInputQuickSettingsDisabler> lazy87 = this.mRemoteInputQuickSettingsDisabler;
        Objects.requireNonNull(lazy87);
        arrayMap87.put(RemoteInputQuickSettingsDisabler.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap88 = this.mProviders;
        Lazy<BubbleController> lazy88 = this.mBubbleController;
        Objects.requireNonNull(lazy88);
        arrayMap88.put(BubbleController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap89 = this.mProviders;
        Lazy<NotificationEntryManager> lazy89 = this.mNotificationEntryManager;
        Objects.requireNonNull(lazy89);
        arrayMap89.put(NotificationEntryManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap90 = this.mProviders;
        Lazy<ForegroundServiceNotificationListener> lazy90 = this.mForegroundServiceNotificationListener;
        Objects.requireNonNull(lazy90);
        arrayMap90.put(ForegroundServiceNotificationListener.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap91 = this.mProviders;
        Lazy<ClockManager> lazy91 = this.mClockManager;
        Objects.requireNonNull(lazy91);
        arrayMap91.put(ClockManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap92 = this.mProviders;
        Lazy<ActivityManagerWrapper> lazy92 = this.mActivityManagerWrapper;
        Objects.requireNonNull(lazy92);
        arrayMap92.put(ActivityManagerWrapper.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap93 = this.mProviders;
        Lazy<DevicePolicyManagerWrapper> lazy93 = this.mDevicePolicyManagerWrapper;
        Objects.requireNonNull(lazy93);
        arrayMap93.put(DevicePolicyManagerWrapper.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap94 = this.mProviders;
        Lazy<PackageManagerWrapper> lazy94 = this.mPackageManagerWrapper;
        Objects.requireNonNull(lazy94);
        arrayMap94.put(PackageManagerWrapper.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap95 = this.mProviders;
        Lazy<SensorPrivacyController> lazy95 = this.mSensorPrivacyController;
        Objects.requireNonNull(lazy95);
        arrayMap95.put(SensorPrivacyController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap96 = this.mProviders;
        Lazy<DockManager> lazy96 = this.mDockManager;
        Objects.requireNonNull(lazy96);
        arrayMap96.put(DockManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap97 = this.mProviders;
        Lazy<INotificationManager> lazy97 = this.mINotificationManager;
        Objects.requireNonNull(lazy97);
        arrayMap97.put(INotificationManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap98 = this.mProviders;
        Lazy<SysUiState> lazy98 = this.mSysUiStateFlagsContainer;
        Objects.requireNonNull(lazy98);
        arrayMap98.put(SysUiState.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap99 = this.mProviders;
        Lazy<AlarmManager> lazy99 = this.mAlarmManager;
        Objects.requireNonNull(lazy99);
        arrayMap99.put(AlarmManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap100 = this.mProviders;
        Lazy<KeyguardSecurityModel> lazy100 = this.mKeyguardSecurityModel;
        Objects.requireNonNull(lazy100);
        arrayMap100.put(KeyguardSecurityModel.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap101 = this.mProviders;
        Lazy<DozeParameters> lazy101 = this.mDozeParameters;
        Objects.requireNonNull(lazy101);
        arrayMap101.put(DozeParameters.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap102 = this.mProviders;
        Lazy<IWallpaperManager> lazy102 = this.mWallpaperManager;
        Objects.requireNonNull(lazy102);
        arrayMap102.put(IWallpaperManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap103 = this.mProviders;
        Lazy<CommandQueue> lazy103 = this.mCommandQueue;
        Objects.requireNonNull(lazy103);
        arrayMap103.put(CommandQueue.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap104 = this.mProviders;
        Lazy<Recents> lazy104 = this.mRecents;
        Objects.requireNonNull(lazy104);
        arrayMap104.put(Recents.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap105 = this.mProviders;
        Lazy<StatusBar> lazy105 = this.mStatusBar;
        Objects.requireNonNull(lazy105);
        arrayMap105.put(StatusBar.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap106 = this.mProviders;
        Lazy<DisplayController> lazy106 = this.mDisplayController;
        Objects.requireNonNull(lazy106);
        arrayMap106.put(DisplayController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap107 = this.mProviders;
        Lazy<SystemWindows> lazy107 = this.mSystemWindows;
        Objects.requireNonNull(lazy107);
        arrayMap107.put(SystemWindows.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap108 = this.mProviders;
        Lazy<DisplayImeController> lazy108 = this.mDisplayImeController;
        Objects.requireNonNull(lazy108);
        arrayMap108.put(DisplayImeController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap109 = this.mProviders;
        Lazy<ProtoTracer> lazy109 = this.mProtoTracer;
        Objects.requireNonNull(lazy109);
        arrayMap109.put(ProtoTracer.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap110 = this.mProviders;
        Lazy<AutoHideController> lazy110 = this.mAutoHideController;
        Objects.requireNonNull(lazy110);
        arrayMap110.put(AutoHideController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap111 = this.mProviders;
        Lazy<RecordingController> lazy111 = this.mRecordingController;
        Objects.requireNonNull(lazy111);
        arrayMap111.put(RecordingController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap112 = this.mProviders;
        Lazy<Divider> lazy112 = this.mDivider;
        Objects.requireNonNull(lazy112);
        arrayMap112.put(Divider.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap113 = this.mProviders;
        Lazy<OpBiometricDialogImpl> lazy113 = this.mOpBiometricDialogImpl;
        Objects.requireNonNull(lazy113);
        arrayMap113.put(OpBiometricDialogImpl.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        if (OpUtils.isSupportSOCThreekey()) {
            ArrayMap<Object, LazyDependencyCreator> arrayMap114 = this.mProviders;
            Lazy<OpZenModeController> lazy114 = this.mOpZenModeController;
            Objects.requireNonNull(lazy114);
            arrayMap114.put(OpZenModeController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
                @Override // com.android.systemui.Dependency.LazyDependencyCreator
                public final Object createDependency() {
                    return Lazy.this.get();
                }
            });
        }
        ArrayMap<Object, LazyDependencyCreator> arrayMap115 = this.mProviders;
        Lazy<OpChargingAnimationController> lazy115 = this.mOpChargingAnimationController;
        Objects.requireNonNull(lazy115);
        arrayMap115.put(OpChargingAnimationController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap116 = this.mProviders;
        Lazy<NetworkSpeedController> lazy116 = this.mOpNetworkSpeedController;
        Objects.requireNonNull(lazy116);
        arrayMap116.put(NetworkSpeedController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap117 = this.mProviders;
        Lazy<OpNotificationController> lazy117 = this.mOpNotificationController;
        Objects.requireNonNull(lazy117);
        arrayMap117.put(OpNotificationController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap118 = this.mProviders;
        Lazy<OPWLBHelper> lazy118 = this.mOPWLBHelper;
        Objects.requireNonNull(lazy118);
        arrayMap118.put(OPWLBHelper.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap119 = this.mProviders;
        Lazy<OpSceneModeObserver> lazy119 = this.mOpSceneModeObserver;
        Objects.requireNonNull(lazy119);
        arrayMap119.put(OpSceneModeObserver.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap120 = this.mProviders;
        Lazy<OpHighlightHintController> lazy120 = this.mOpHighlightHintController;
        Objects.requireNonNull(lazy120);
        arrayMap120.put(OpHighlightHintController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        ArrayMap<Object, LazyDependencyCreator> arrayMap121 = this.mProviders;
        Lazy<OpThreekeyVolumeGuideController> lazy121 = this.mOpThreekeyVolumeGuideController;
        Objects.requireNonNull(lazy121);
        arrayMap121.put(OpThreekeyVolumeGuideController.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
            @Override // com.android.systemui.Dependency.LazyDependencyCreator
            public final Object createDependency() {
                return Lazy.this.get();
            }
        });
        if (OpBitmojiHelper.isBitmojiAodEnabled()) {
            ArrayMap<Object, LazyDependencyCreator> arrayMap122 = this.mProviders;
            Lazy<OpBitmojiManager> lazy122 = this.mOpBitmojiManager;
            Objects.requireNonNull(lazy122);
            arrayMap122.put(OpBitmojiManager.class, new LazyDependencyCreator() { // from class: com.android.systemui.-$$Lambda$Vs-MsjQwuYhfrxzUr7AqZvcfoH4
                @Override // com.android.systemui.Dependency.LazyDependencyCreator
                public final Object createDependency() {
                    return Lazy.this.get();
                }
            });
        }
        sDependency = this;
    }

    /* access modifiers changed from: protected */
    public final <T> T getDependency(Class<T> cls) {
        return (T) getDependencyInner(cls);
    }

    /* access modifiers changed from: protected */
    public final <T> T getDependency(DependencyKey<T> dependencyKey) {
        return (T) getDependencyInner(dependencyKey);
    }

    private synchronized <T> T getDependencyInner(Object obj) {
        T t;
        t = (T) this.mDependencies.get(obj);
        if (t == null) {
            t = (T) createDependency(obj);
            this.mDependencies.put(obj, t);
            if (autoRegisterModulesForDump() && (t instanceof Dumpable)) {
                this.mDumpManager.registerDumpable(t.getClass().getName(), t);
            }
        }
        return t;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public <T> T createDependency(Object obj) {
        Preconditions.checkArgument((obj instanceof DependencyKey) || (obj instanceof Class));
        LazyDependencyCreator lazyDependencyCreator = this.mProviders.get(obj);
        if (lazyDependencyCreator != null) {
            return (T) lazyDependencyCreator.createDependency();
        }
        throw new IllegalArgumentException("Unsupported dependency " + obj + ". " + this.mProviders.size() + " providers known.");
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.util.function.Consumer<T> */
    /* JADX WARN: Multi-variable type inference failed */
    private <T> void destroyDependency(Class<T> cls, Consumer<T> consumer) {
        Object remove = this.mDependencies.remove(cls);
        if (remove instanceof Dumpable) {
            this.mDumpManager.unregisterDumpable(remove.getClass().getName());
        }
        if (remove != null && consumer != 0) {
            consumer.accept(remove);
        }
    }

    public static <T> void destroy(Class<T> cls, Consumer<T> consumer) {
        Dependency dependency = sDependency;
        if (dependency != null) {
            dependency.destroyDependency(cls, consumer);
        }
    }

    @Deprecated
    public static <T> T get(Class<T> cls) {
        Dependency dependency = sDependency;
        if (dependency == null) {
            return null;
        }
        return (T) dependency.getDependency(cls);
    }

    @Deprecated
    public static <T> T get(DependencyKey<T> dependencyKey) {
        Dependency dependency = sDependency;
        if (dependency == null) {
            return null;
        }
        return (T) dependency.getDependency(dependencyKey);
    }

    public static final class DependencyKey<V> {
        private final String mDisplayName;

        public DependencyKey(String str) {
            this.mDisplayName = str;
        }

        public String toString() {
            return this.mDisplayName;
        }
    }
}
