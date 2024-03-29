package com.android.systemui.tv;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.INotificationManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.hardware.SensorPrivacyManager;
import android.hardware.display.NightDisplayListener;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UserManager;
import android.os.Vibrator;
import android.service.dreams.IDreamManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Choreographer;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import androidx.slice.Clock;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.CarrierTextController;
import com.android.keyguard.KeyguardClockSwitch;
import com.android.keyguard.KeyguardMessageArea;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardSecurityModel_Factory;
import com.android.keyguard.KeyguardSliceView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitor_Factory;
import com.android.keyguard.clock.ClockManager;
import com.android.keyguard.clock.ClockManager_Factory;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.ActivityIntentHelper_Factory;
import com.android.systemui.ActivityStarterDelegate;
import com.android.systemui.ActivityStarterDelegate_Factory;
import com.android.systemui.BootCompleteCacheImpl;
import com.android.systemui.BootCompleteCacheImpl_Factory;
import com.android.systemui.Dependency;
import com.android.systemui.Dependency_MembersInjector;
import com.android.systemui.ForegroundServiceController;
import com.android.systemui.ForegroundServiceController_Factory;
import com.android.systemui.ForegroundServiceLifetimeExtender_Factory;
import com.android.systemui.ForegroundServiceNotificationListener;
import com.android.systemui.ForegroundServiceNotificationListener_Factory;
import com.android.systemui.ForegroundServicesDialog;
import com.android.systemui.ForegroundServicesDialog_Factory;
import com.android.systemui.ImageWallpaper;
import com.android.systemui.ImageWallpaper_Factory;
import com.android.systemui.InitController;
import com.android.systemui.InitController_Factory;
import com.android.systemui.LatencyTester;
import com.android.systemui.LatencyTester_Factory;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.ScreenDecorations_Factory;
import com.android.systemui.SizeCompatModeActivityController;
import com.android.systemui.SizeCompatModeActivityController_Factory;
import com.android.systemui.SliceBroadcastRelayHandler;
import com.android.systemui.SliceBroadcastRelayHandler_Factory;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIAppComponentFactory;
import com.android.systemui.SystemUIAppComponentFactory_MembersInjector;
import com.android.systemui.SystemUIService;
import com.android.systemui.SystemUIService_Factory;
import com.android.systemui.TransactionPool;
import com.android.systemui.TransactionPool_Factory;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.UiOffloadThread_Factory;
import com.android.systemui.accessibility.SystemActions;
import com.android.systemui.accessibility.SystemActions_Factory;
import com.android.systemui.accessibility.WindowMagnification;
import com.android.systemui.accessibility.WindowMagnification_Factory;
import com.android.systemui.appops.AppOpsControllerImpl;
import com.android.systemui.appops.AppOpsControllerImpl_Factory;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.assist.AssistHandleBehaviorController_Factory;
import com.android.systemui.assist.AssistHandleLikeHomeBehavior_Factory;
import com.android.systemui.assist.AssistHandleOffBehavior_Factory;
import com.android.systemui.assist.AssistHandleReminderExpBehavior_Factory;
import com.android.systemui.assist.AssistLogger;
import com.android.systemui.assist.AssistLogger_Factory;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.assist.AssistManager_Factory;
import com.android.systemui.assist.AssistModule_ProvideAssistHandleBehaviorControllerMapFactory;
import com.android.systemui.assist.AssistModule_ProvideAssistHandleViewControllerFactory;
import com.android.systemui.assist.AssistModule_ProvideAssistUtilsFactory;
import com.android.systemui.assist.AssistModule_ProvideBackgroundHandlerFactory;
import com.android.systemui.assist.AssistModule_ProvideSystemClockFactory;
import com.android.systemui.assist.DeviceConfigHelper;
import com.android.systemui.assist.DeviceConfigHelper_Factory;
import com.android.systemui.assist.PhoneStateMonitor;
import com.android.systemui.assist.PhoneStateMonitor_Factory;
import com.android.systemui.assist.ui.DefaultUiController;
import com.android.systemui.assist.ui.DefaultUiController_Factory;
import com.android.systemui.biometrics.AuthController;
import com.android.systemui.biometrics.AuthController_Factory;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger_Factory;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.bubbles.BubbleData;
import com.android.systemui.bubbles.BubbleDataRepository;
import com.android.systemui.bubbles.BubbleDataRepository_Factory;
import com.android.systemui.bubbles.BubbleData_Factory;
import com.android.systemui.bubbles.BubbleOverflowActivity;
import com.android.systemui.bubbles.BubbleOverflowActivity_Factory;
import com.android.systemui.bubbles.dagger.BubbleModule_NewBubbleControllerFactory;
import com.android.systemui.bubbles.storage.BubblePersistentRepository;
import com.android.systemui.bubbles.storage.BubblePersistentRepository_Factory;
import com.android.systemui.bubbles.storage.BubbleVolatileRepository;
import com.android.systemui.bubbles.storage.BubbleVolatileRepository_Factory;
import com.android.systemui.classifier.FalsingManagerProxy;
import com.android.systemui.classifier.FalsingManagerProxy_Factory;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.colorextraction.SysuiColorExtractor_Factory;
import com.android.systemui.controls.controller.ControlsBindingControllerImpl;
import com.android.systemui.controls.controller.ControlsBindingControllerImpl_Factory;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.controls.controller.ControlsControllerImpl_Factory;
import com.android.systemui.controls.controller.ControlsFavoritePersistenceWrapper;
import com.android.systemui.controls.dagger.ControlsComponent;
import com.android.systemui.controls.dagger.ControlsComponent_Factory;
import com.android.systemui.controls.dagger.ControlsModule_ProvidesControlsFeatureEnabledFactory;
import com.android.systemui.controls.management.ControlsEditingActivity;
import com.android.systemui.controls.management.ControlsEditingActivity_Factory;
import com.android.systemui.controls.management.ControlsFavoritingActivity;
import com.android.systemui.controls.management.ControlsFavoritingActivity_Factory;
import com.android.systemui.controls.management.ControlsListingControllerImpl;
import com.android.systemui.controls.management.ControlsListingControllerImpl_Factory;
import com.android.systemui.controls.management.ControlsProviderSelectorActivity;
import com.android.systemui.controls.management.ControlsProviderSelectorActivity_Factory;
import com.android.systemui.controls.management.ControlsRequestDialog;
import com.android.systemui.controls.management.ControlsRequestDialog_Factory;
import com.android.systemui.controls.ui.ControlActionCoordinatorImpl;
import com.android.systemui.controls.ui.ControlActionCoordinatorImpl_Factory;
import com.android.systemui.controls.ui.ControlsUiControllerImpl;
import com.android.systemui.controls.ui.ControlsUiControllerImpl_Factory;
import com.android.systemui.dagger.ContextComponentHelper;
import com.android.systemui.dagger.ContextComponentResolver;
import com.android.systemui.dagger.ContextComponentResolver_Factory;
import com.android.systemui.dagger.DependencyProvider;
import com.android.systemui.dagger.DependencyProvider_ProvideActivityManagerWrapperFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideAmbientDisplayConfigurationFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideAutoHideControllerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideConfigurationControllerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideDataSaverControllerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideDevicePolicyManagerWrapperFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideDisplayMetricsFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideINotificationManagerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideLeakDetectorFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideLockPatternUtilsFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideMetricsLoggerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideNavigationBarControllerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideNightDisplayListenerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideNotificationMessagingUtilFactory;
import com.android.systemui.dagger.DependencyProvider_ProvidePluginManagerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideSharePreferencesFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideTimeTickHandlerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvideUiEventLoggerFactory;
import com.android.systemui.dagger.DependencyProvider_ProviderLayoutInflaterFactory;
import com.android.systemui.dagger.DependencyProvider_ProvidesBroadcastDispatcherFactory;
import com.android.systemui.dagger.DependencyProvider_ProvidesChoreographerFactory;
import com.android.systemui.dagger.DependencyProvider_ProvidesViewMediatorCallbackFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideAccessibilityManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideActivityManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideAlarmManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideAudioManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideConnectivityManagagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideContentResolverFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideDevicePolicyManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideDisplayIdFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIActivityManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIBatteryStatsFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIDreamManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIPackageManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIStatusBarServiceFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIWallPaperManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideIWindowManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideKeyguardManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideLatencyTrackerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideLauncherAppsFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideLocalBluetoothControllerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideMediaRouter2ManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideNetworkScoreManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideNotificationManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvidePackageManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvidePackageManagerWrapperFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvidePowerManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideResourcesFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideSensorPrivacyManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideShortcutManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideTelecomManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideTelephonyManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideTrustManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideUserManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideVibratorFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideWallpaperManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideWifiManagerFactory;
import com.android.systemui.dagger.SystemServicesModule_ProvideWindowManagerFactory;
import com.android.systemui.dagger.SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory;
import com.android.systemui.dagger.SystemUIDefaultModule_ProvideBatteryControllerFactory;
import com.android.systemui.dagger.SystemUIDefaultModule_ProvideHeadsUpManagerPhoneFactory;
import com.android.systemui.dagger.SystemUIDefaultModule_ProvideLeakReportEmailFactory;
import com.android.systemui.dagger.SystemUIDefaultModule_ProvideRecentsFactory;
import com.android.systemui.dagger.SystemUIModule_ProvideKeyguardLiftControllerFactory;
import com.android.systemui.dagger.SystemUIModule_ProvideSysUiStateFactory;
import com.android.systemui.dock.DockManager;
import com.android.systemui.dock.DockManagerImpl;
import com.android.systemui.dock.DockManagerImpl_Factory;
import com.android.systemui.doze.DozeFactory_Factory;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.doze.DozeLog_Factory;
import com.android.systemui.doze.DozeLogger_Factory;
import com.android.systemui.doze.DozeService;
import com.android.systemui.doze.DozeService_Factory;
import com.android.systemui.dump.DumpHandler_Factory;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.dump.DumpManager_Factory;
import com.android.systemui.dump.LogBufferEulogizer;
import com.android.systemui.dump.LogBufferEulogizer_Factory;
import com.android.systemui.dump.LogBufferFreezer_Factory;
import com.android.systemui.dump.SystemUIAuxiliaryDumpService;
import com.android.systemui.dump.SystemUIAuxiliaryDumpService_Factory;
import com.android.systemui.fragments.FragmentService;
import com.android.systemui.fragments.FragmentService_Factory;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.globalactions.GlobalActionsComponent_Factory;
import com.android.systemui.globalactions.GlobalActionsDialog_Factory;
import com.android.systemui.globalactions.GlobalActionsImpl_Factory;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.keyguard.DismissCallbackRegistry_Factory;
import com.android.systemui.keyguard.KeyguardLifecyclesDispatcher;
import com.android.systemui.keyguard.KeyguardLifecyclesDispatcher_Factory;
import com.android.systemui.keyguard.KeyguardService;
import com.android.systemui.keyguard.KeyguardService_Factory;
import com.android.systemui.keyguard.KeyguardSliceProvider;
import com.android.systemui.keyguard.KeyguardSliceProvider_MembersInjector;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.keyguard.ScreenLifecycle_Factory;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.keyguard.WakefulnessLifecycle_Factory;
import com.android.systemui.keyguard.WorkLockActivity;
import com.android.systemui.keyguard.WorkLockActivity_Factory;
import com.android.systemui.keyguard.dagger.KeyguardModule_NewKeyguardViewMediatorFactory;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogcatEchoTracker;
import com.android.systemui.log.dagger.LogModule_ProvideBroadcastDispatcherLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideDozeLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideLogcatEchoTrackerFactory;
import com.android.systemui.log.dagger.LogModule_ProvideNotifInteractionLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideNotificationSectionLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideNotificationsLogBufferFactory;
import com.android.systemui.log.dagger.LogModule_ProvideQuickSettingsLogBufferFactory;
import com.android.systemui.media.KeyguardMediaController;
import com.android.systemui.media.KeyguardMediaController_Factory;
import com.android.systemui.media.LocalMediaManagerFactory_Factory;
import com.android.systemui.media.MediaCarouselController;
import com.android.systemui.media.MediaCarouselController_Factory;
import com.android.systemui.media.MediaControlPanel_Factory;
import com.android.systemui.media.MediaControllerFactory_Factory;
import com.android.systemui.media.MediaDataCombineLatest;
import com.android.systemui.media.MediaDataCombineLatest_Factory;
import com.android.systemui.media.MediaDataFilter;
import com.android.systemui.media.MediaDataFilter_Factory;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.MediaDataManager_Factory;
import com.android.systemui.media.MediaDeviceManager;
import com.android.systemui.media.MediaDeviceManager_Factory;
import com.android.systemui.media.MediaFeatureFlag_Factory;
import com.android.systemui.media.MediaHierarchyManager;
import com.android.systemui.media.MediaHierarchyManager_Factory;
import com.android.systemui.media.MediaHost;
import com.android.systemui.media.MediaHostStatesManager;
import com.android.systemui.media.MediaHostStatesManager_Factory;
import com.android.systemui.media.MediaHost_Factory;
import com.android.systemui.media.MediaHost_MediaHostStateHolder_Factory;
import com.android.systemui.media.MediaResumeListener;
import com.android.systemui.media.MediaResumeListener_Factory;
import com.android.systemui.media.MediaTimeoutListener;
import com.android.systemui.media.MediaTimeoutListener_Factory;
import com.android.systemui.media.MediaViewController_Factory;
import com.android.systemui.media.SeekBarViewModel_Factory;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.PipAnimationController_Factory;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipBoundsHandler_Factory;
import com.android.systemui.pip.PipSnapAlgorithm_Factory;
import com.android.systemui.pip.PipSurfaceTransactionHelper;
import com.android.systemui.pip.PipSurfaceTransactionHelper_Factory;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.PipTaskOrganizer_Factory;
import com.android.systemui.pip.PipUI;
import com.android.systemui.pip.PipUI_Factory;
import com.android.systemui.pip.tv.PipControlsView;
import com.android.systemui.pip.tv.PipControlsViewController;
import com.android.systemui.pip.tv.PipManager;
import com.android.systemui.pip.tv.PipManager_Factory;
import com.android.systemui.pip.tv.PipMenuActivity;
import com.android.systemui.pip.tv.PipMenuActivity_Factory;
import com.android.systemui.pip.tv.dagger.TvPipComponent;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.PluginDependencyProvider;
import com.android.systemui.plugins.PluginDependencyProvider_Factory;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.power.EnhancedEstimatesImpl;
import com.android.systemui.power.EnhancedEstimatesImpl_Factory;
import com.android.systemui.power.PowerNotificationWarnings;
import com.android.systemui.power.PowerNotificationWarnings_Factory;
import com.android.systemui.power.PowerUI;
import com.android.systemui.power.PowerUI_Factory;
import com.android.systemui.qs.AutoAddTracker_Builder_Factory;
import com.android.systemui.qs.QSContainerImplController;
import com.android.systemui.qs.QSFooterImpl;
import com.android.systemui.qs.QSFragment;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.QSTileHost_Factory;
import com.android.systemui.qs.QuickQSPanel;
import com.android.systemui.qs.QuickStatusBarHeader;
import com.android.systemui.qs.QuickStatusBarHeaderController;
import com.android.systemui.qs.carrier.QSCarrierGroupController$Builder;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.logging.QSLogger_Factory;
import com.android.systemui.qs.tileimpl.QSFactoryImpl;
import com.android.systemui.qs.tileimpl.QSFactoryImpl_Factory;
import com.android.systemui.qs.tiles.AirplaneModeTile_Factory;
import com.android.systemui.qs.tiles.BatterySaverTile_Factory;
import com.android.systemui.qs.tiles.BluetoothTile_Factory;
import com.android.systemui.qs.tiles.CastTile_Factory;
import com.android.systemui.qs.tiles.CellularTile_Factory;
import com.android.systemui.qs.tiles.ColorInversionTile_Factory;
import com.android.systemui.qs.tiles.DataSaverTile_Factory;
import com.android.systemui.qs.tiles.DataSwitchTile_Factory;
import com.android.systemui.qs.tiles.DndTile_Factory;
import com.android.systemui.qs.tiles.FlashlightTile_Factory;
import com.android.systemui.qs.tiles.GameModeTile_Factory;
import com.android.systemui.qs.tiles.HotspotTile_Factory;
import com.android.systemui.qs.tiles.LocationTile_Factory;
import com.android.systemui.qs.tiles.NfcTile_Factory;
import com.android.systemui.qs.tiles.NightDisplayTile_Factory;
import com.android.systemui.qs.tiles.OPDndCarModeTile_Factory;
import com.android.systemui.qs.tiles.OPDndTile_Factory;
import com.android.systemui.qs.tiles.OPReverseChargeTile_Factory;
import com.android.systemui.qs.tiles.OtgTile_Factory;
import com.android.systemui.qs.tiles.ReadModeTile_Factory;
import com.android.systemui.qs.tiles.RotationLockTile_Factory;
import com.android.systemui.qs.tiles.ScreenRecordTile_Factory;
import com.android.systemui.qs.tiles.UiModeNightTile_Factory;
import com.android.systemui.qs.tiles.UserTile_Factory;
import com.android.systemui.qs.tiles.VPNTile_Factory;
import com.android.systemui.qs.tiles.WifiTile_Factory;
import com.android.systemui.qs.tiles.WorkModeTile_Factory;
import com.android.systemui.recents.OverviewProxyRecentsImpl;
import com.android.systemui.recents.OverviewProxyRecentsImpl_Factory;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.OverviewProxyService_Factory;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsImplementation;
import com.android.systemui.recents.RecentsModule_ProvideRecentsImplFactory;
import com.android.systemui.recents.ScreenPinningRequest_Factory;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.screenrecord.RecordingController_Factory;
import com.android.systemui.screenrecord.RecordingService;
import com.android.systemui.screenrecord.RecordingService_Factory;
import com.android.systemui.screenrecord.ScreenRecordDialog;
import com.android.systemui.screenrecord.ScreenRecordDialog_Factory;
import com.android.systemui.screenshot.GlobalScreenshot;
import com.android.systemui.screenshot.GlobalScreenshot_ActionProxyReceiver_Factory;
import com.android.systemui.screenshot.GlobalScreenshot_Factory;
import com.android.systemui.screenshot.ScreenshotNotificationsController_Factory;
import com.android.systemui.screenshot.TakeScreenshotService;
import com.android.systemui.screenshot.TakeScreenshotService_Factory;
import com.android.systemui.settings.BrightnessDialog;
import com.android.systemui.settings.BrightnessDialog_Factory;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.settings.dagger.SettingsModule_ProvideCurrentUserContextTrackerFactory;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.DevicePolicyManagerWrapper;
import com.android.systemui.shared.system.PackageManagerWrapper;
import com.android.systemui.shortcut.ShortcutKeyDispatcher;
import com.android.systemui.shortcut.ShortcutKeyDispatcher_Factory;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.DividerModule_ProvideDividerFactory;
import com.android.systemui.statusbar.ActionClickLogger_Factory;
import com.android.systemui.statusbar.BlurUtils;
import com.android.systemui.statusbar.BlurUtils_Factory;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.FeatureFlags_Factory;
import com.android.systemui.statusbar.FlingAnimationUtils_Builder_Factory;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.KeyguardIndicationController_Factory;
import com.android.systemui.statusbar.MediaArtworkProcessor;
import com.android.systemui.statusbar.MediaArtworkProcessor_Factory;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.NotificationClickNotifier;
import com.android.systemui.statusbar.NotificationClickNotifier_Factory;
import com.android.systemui.statusbar.NotificationInteractionTracker;
import com.android.systemui.statusbar.NotificationInteractionTracker_Factory;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl;
import com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl_Factory;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.NotificationShadeDepthController_Factory;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.PulseExpansionHandler_Factory;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.StatusBarStateControllerImpl;
import com.android.systemui.statusbar.StatusBarStateControllerImpl_Factory;
import com.android.systemui.statusbar.SuperStatusBarViewFactory;
import com.android.systemui.statusbar.SuperStatusBarViewFactory_Factory;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.VibratorHelper_Factory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideCommandQueueFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationListenerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationMediaManagerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationRemoteInputManagerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideNotificationViewHierarchyManagerFactory;
import com.android.systemui.statusbar.dagger.StatusBarDependenciesModule_ProvideSmartReplyControllerFactory;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.ConversationNotificationManager_Factory;
import com.android.systemui.statusbar.notification.ConversationNotificationProcessor_Factory;
import com.android.systemui.statusbar.notification.DynamicChildBindController_Factory;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.DynamicPrivacyController_Factory;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController_Factory;
import com.android.systemui.statusbar.notification.InstantAppNotifier;
import com.android.systemui.statusbar.notification.InstantAppNotifier_Factory;
import com.android.systemui.statusbar.notification.NotificationClickerLogger_Factory;
import com.android.systemui.statusbar.notification.NotificationClicker_Builder_Factory;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationEntryManagerLogger_Factory;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.NotificationFilter_Factory;
import com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager;
import com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager_Factory;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator_Factory;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifCollection_Factory;
import com.android.systemui.statusbar.notification.collection.NotifInflaterImpl;
import com.android.systemui.statusbar.notification.collection.NotifInflaterImpl_Factory;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotifPipeline_Factory;
import com.android.systemui.statusbar.notification.collection.NotifViewBarn;
import com.android.systemui.statusbar.notification.collection.NotifViewBarn_Factory;
import com.android.systemui.statusbar.notification.collection.NotifViewManager;
import com.android.systemui.statusbar.notification.collection.NotifViewManager_Factory;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.NotificationRankingManager_Factory;
import com.android.systemui.statusbar.notification.collection.ShadeListBuilder;
import com.android.systemui.statusbar.notification.collection.ShadeListBuilder_Factory;
import com.android.systemui.statusbar.notification.collection.TargetSdkResolver;
import com.android.systemui.statusbar.notification.collection.TargetSdkResolver_Factory;
import com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescerLogger_Factory;
import com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescer_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.BubbleCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.BubbleCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.ConversationCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.ConversationCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.DeviceProvisionedCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.DeviceProvisionedCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.HeadsUpCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.HideNotifsForOtherUsersCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.KeyguardCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.KeyguardCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.MediaCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.NotifCoordinators;
import com.android.systemui.statusbar.notification.collection.coordinator.NotifCoordinators_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.PreparationCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.PreparationCoordinatorLogger_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.PreparationCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.RankingCoordinator;
import com.android.systemui.statusbar.notification.collection.coordinator.RankingCoordinator_Factory;
import com.android.systemui.statusbar.notification.collection.coordinator.SharedCoordinatorLogger_Factory;
import com.android.systemui.statusbar.notification.collection.inflation.LowPriorityInflationHelper;
import com.android.systemui.statusbar.notification.collection.inflation.LowPriorityInflationHelper_Factory;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl_Factory;
import com.android.systemui.statusbar.notification.collection.init.NotifPipelineInitializer;
import com.android.systemui.statusbar.notification.collection.init.NotifPipelineInitializer_Factory;
import com.android.systemui.statusbar.notification.collection.listbuilder.ShadeListBuilderLogger_Factory;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionLogger_Factory;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider_Factory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideCommonNotifCollectionFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationBlockingHelperManagerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationEntryManagerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationGutsManagerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationLoggerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationPanelLoggerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideNotificationsControllerFactory;
import com.android.systemui.statusbar.notification.dagger.NotificationsModule_ProvideVisualStabilityManagerFactory;
import com.android.systemui.statusbar.notification.icon.IconBuilder_Factory;
import com.android.systemui.statusbar.notification.icon.IconManager_Factory;
import com.android.systemui.statusbar.notification.init.NotificationsController;
import com.android.systemui.statusbar.notification.init.NotificationsControllerImpl;
import com.android.systemui.statusbar.notification.init.NotificationsControllerImpl_Factory;
import com.android.systemui.statusbar.notification.init.NotificationsControllerStub_Factory;
import com.android.systemui.statusbar.notification.interruption.BypassHeadsUpNotifier;
import com.android.systemui.statusbar.notification.interruption.BypassHeadsUpNotifier_Factory;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController_Factory;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder_Factory;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl_Factory;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.logging.NotificationLogger_ExpansionStateLogger_Factory;
import com.android.systemui.statusbar.notification.logging.NotificationPanelLogger;
import com.android.systemui.statusbar.notification.people.NotificationPersonExtractorPluginBoundary;
import com.android.systemui.statusbar.notification.people.NotificationPersonExtractorPluginBoundary_Factory;
import com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl;
import com.android.systemui.statusbar.notification.people.PeopleHubDataSourceImpl_Factory;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapter;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapterImpl;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapterImpl_Factory;
import com.android.systemui.statusbar.notification.people.PeopleHubViewModelFactoryDataSourceImpl;
import com.android.systemui.statusbar.notification.people.PeopleHubViewModelFactoryDataSourceImpl_Factory;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifierImpl;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifierImpl_Factory;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController_Factory;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialogController;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialogController_Factory;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialog_Builder_Factory;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRowController;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRowController_Factory;
import com.android.systemui.statusbar.notification.row.ExpandableOutlineViewController;
import com.android.systemui.statusbar.notification.row.ExpandableOutlineViewController_Factory;
import com.android.systemui.statusbar.notification.row.ExpandableViewController;
import com.android.systemui.statusbar.notification.row.ExpandableViewController_Factory;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.notification.row.NotifBindPipelineInitializer_Factory;
import com.android.systemui.statusbar.notification.row.NotifBindPipelineLogger_Factory;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline_Factory;
import com.android.systemui.statusbar.notification.row.NotifInflationErrorManager;
import com.android.systemui.statusbar.notification.row.NotifInflationErrorManager_Factory;
import com.android.systemui.statusbar.notification.row.NotifRemoteViewCache;
import com.android.systemui.statusbar.notification.row.NotifRemoteViewCacheImpl_Factory;
import com.android.systemui.statusbar.notification.row.NotificationBlockingHelperManager;
import com.android.systemui.statusbar.notification.row.NotificationContentInflater;
import com.android.systemui.statusbar.notification.row.NotificationContentInflater_Factory;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController_Builder_Factory;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import com.android.systemui.statusbar.notification.row.RowContentBindStageLogger_Factory;
import com.android.systemui.statusbar.notification.row.RowContentBindStage_Factory;
import com.android.systemui.statusbar.notification.row.RowInflaterTask_Factory;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory;
import com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent;
import com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController;
import com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController_Factory;
import com.android.systemui.statusbar.notification.stack.NotificationRoundnessManager;
import com.android.systemui.statusbar.notification.stack.NotificationRoundnessManager_Factory;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger_Factory;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsManager;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.AutoHideController;
import com.android.systemui.statusbar.phone.AutoTileManager_Factory;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.BiometricUnlockController_Factory;
import com.android.systemui.statusbar.phone.DarkIconDispatcherImpl;
import com.android.systemui.statusbar.phone.DarkIconDispatcherImpl_Factory;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.DozeParameters_Factory;
import com.android.systemui.statusbar.phone.DozeScrimController;
import com.android.systemui.statusbar.phone.DozeScrimController_Factory;
import com.android.systemui.statusbar.phone.DozeServiceHost;
import com.android.systemui.statusbar.phone.DozeServiceHost_Factory;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.KeyguardBypassController_Factory;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil_Factory;
import com.android.systemui.statusbar.phone.KeyguardEnvironmentImpl;
import com.android.systemui.statusbar.phone.KeyguardEnvironmentImpl_Factory;
import com.android.systemui.statusbar.phone.KeyguardLiftController;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.LightBarController_Factory;
import com.android.systemui.statusbar.phone.LightsOutNotifController;
import com.android.systemui.statusbar.phone.LightsOutNotifController_Factory;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger_Factory;
import com.android.systemui.statusbar.phone.LockscreenLockIconController;
import com.android.systemui.statusbar.phone.LockscreenLockIconController_Factory;
import com.android.systemui.statusbar.phone.LockscreenWallpaper;
import com.android.systemui.statusbar.phone.LockscreenWallpaper_Factory;
import com.android.systemui.statusbar.phone.ManagedProfileControllerImpl;
import com.android.systemui.statusbar.phone.ManagedProfileControllerImpl_Factory;
import com.android.systemui.statusbar.phone.NavigationBarFragment;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.NavigationModeController_Factory;
import com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationGroupManager_Factory;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationPanelViewController_Factory;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController_Factory;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.phone.NotificationShadeWindowViewController;
import com.android.systemui.statusbar.phone.PhoneStatusBarPolicy_Factory;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ScrimController_Factory;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.ShadeControllerImpl;
import com.android.systemui.statusbar.phone.ShadeControllerImpl_Factory;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarIconControllerImpl;
import com.android.systemui.statusbar.phone.StatusBarIconControllerImpl_Factory;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager_Factory;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger_Factory;
import com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter_Builder_Factory;
import com.android.systemui.statusbar.phone.StatusBarRemoteInputCallback;
import com.android.systemui.statusbar.phone.StatusBarRemoteInputCallback_Factory;
import com.android.systemui.statusbar.phone.StatusBarTouchableRegionManager;
import com.android.systemui.statusbar.phone.StatusBarTouchableRegionManager_Factory;
import com.android.systemui.statusbar.phone.StatusBarWindowController;
import com.android.systemui.statusbar.phone.StatusBarWindowController_Factory;
import com.android.systemui.statusbar.phone.dagger.StatusBarComponent;
import com.android.systemui.statusbar.phone.dagger.StatusBarPhoneDependenciesModule_ProvideNotificationGroupAlertTransferHelperFactory;
import com.android.systemui.statusbar.phone.dagger.StatusBarPhoneModule_ProvideStatusBarFactory;
import com.android.systemui.statusbar.phone.dagger.StatusBarViewModule_GetNotificationPanelViewFactory;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.AccessibilityController_Factory;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper_Factory;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl;
import com.android.systemui.statusbar.policy.BluetoothControllerImpl_Factory;
import com.android.systemui.statusbar.policy.CastControllerImpl;
import com.android.systemui.statusbar.policy.CastControllerImpl_Factory;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.DeviceProvisionedControllerImpl;
import com.android.systemui.statusbar.policy.DeviceProvisionedControllerImpl_Factory;
import com.android.systemui.statusbar.policy.ExtensionControllerImpl;
import com.android.systemui.statusbar.policy.ExtensionControllerImpl_Factory;
import com.android.systemui.statusbar.policy.FlashlightControllerImpl;
import com.android.systemui.statusbar.policy.FlashlightControllerImpl_Factory;
import com.android.systemui.statusbar.policy.HotspotControllerImpl;
import com.android.systemui.statusbar.policy.HotspotControllerImpl_Factory;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.KeyguardStateControllerImpl;
import com.android.systemui.statusbar.policy.KeyguardStateControllerImpl_Factory;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.android.systemui.statusbar.policy.LocationControllerImpl_Factory;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.android.systemui.statusbar.policy.NetworkControllerImpl_Factory;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.NextAlarmControllerImpl;
import com.android.systemui.statusbar.policy.NextAlarmControllerImpl_Factory;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler_Factory;
import com.android.systemui.statusbar.policy.RemoteInputUriController;
import com.android.systemui.statusbar.policy.RemoteInputUriController_Factory;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl;
import com.android.systemui.statusbar.policy.RotationLockControllerImpl_Factory;
import com.android.systemui.statusbar.policy.SecurityControllerImpl;
import com.android.systemui.statusbar.policy.SecurityControllerImpl_Factory;
import com.android.systemui.statusbar.policy.SensorPrivacyControllerImpl;
import com.android.systemui.statusbar.policy.SensorPrivacyControllerImpl_Factory;
import com.android.systemui.statusbar.policy.SmartReplyConstants;
import com.android.systemui.statusbar.policy.SmartReplyConstants_Factory;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl_Factory;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.UserSwitcherController_Factory;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.policy.ZenModeControllerImpl;
import com.android.systemui.statusbar.policy.ZenModeControllerImpl_Factory;
import com.android.systemui.statusbar.tv.TvStatusBar;
import com.android.systemui.statusbar.tv.TvStatusBar_Factory;
import com.android.systemui.theme.ThemeOverlayController;
import com.android.systemui.theme.ThemeOverlayController_Factory;
import com.android.systemui.toast.ToastUI;
import com.android.systemui.toast.ToastUI_Factory;
import com.android.systemui.tracing.ProtoTracer;
import com.android.systemui.tracing.ProtoTracer_Factory;
import com.android.systemui.tuner.TunablePadding;
import com.android.systemui.tuner.TunablePadding_TunablePaddingService_Factory;
import com.android.systemui.tuner.TunerActivity;
import com.android.systemui.tuner.TunerActivity_Factory;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerServiceImpl;
import com.android.systemui.tuner.TunerServiceImpl_Factory;
import com.android.systemui.tv.TvSystemUIRootComponent;
import com.android.systemui.usb.UsbDebuggingActivity;
import com.android.systemui.usb.UsbDebuggingActivity_Factory;
import com.android.systemui.usb.UsbDebuggingSecondaryUserActivity;
import com.android.systemui.usb.UsbDebuggingSecondaryUserActivity_Factory;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.DeviceConfigProxy_Factory;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.util.FloatingContentCoordinator_Factory;
import com.android.systemui.util.InjectionInflationController;
import com.android.systemui.util.InjectionInflationController_Factory;
import com.android.systemui.util.InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory;
import com.android.systemui.util.InjectionInflationController_ViewAttributeProvider_ProvideContextFactory;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.util.RingerModeTrackerImpl;
import com.android.systemui.util.RingerModeTrackerImpl_Factory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBackgroundDelayableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBackgroundExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBackgroundRepeatableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBgHandlerFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideBgLooperFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideDelayableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideHandlerFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideLongRunningExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideLongRunningLooperFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainDelayableExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainExecutorFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainHandlerFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideMainLooperFactory;
import com.android.systemui.util.concurrency.ConcurrencyModule_ProvideUiBackgroundExecutorFactory;
import com.android.systemui.util.concurrency.DelayableExecutor;
import com.android.systemui.util.concurrency.RepeatableExecutor;
import com.android.systemui.util.io.Files;
import com.android.systemui.util.io.Files_Factory;
import com.android.systemui.util.leak.GarbageMonitor;
import com.android.systemui.util.leak.GarbageMonitor_Factory;
import com.android.systemui.util.leak.GarbageMonitor_MemoryTile_Factory;
import com.android.systemui.util.leak.GarbageMonitor_Service_Factory;
import com.android.systemui.util.leak.LeakDetector;
import com.android.systemui.util.leak.LeakReporter;
import com.android.systemui.util.leak.LeakReporter_Factory;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.android.systemui.util.sensors.AsyncSensorManager_Factory;
import com.android.systemui.util.sensors.ProximitySensor_Factory;
import com.android.systemui.util.sensors.ProximitySensor_ProximityCheck_Factory;
import com.android.systemui.util.time.DateFormatUtil_Factory;
import com.android.systemui.util.time.SystemClock;
import com.android.systemui.util.time.SystemClockImpl_Factory;
import com.android.systemui.util.wakelock.DelayedWakeLock_Builder_Factory;
import com.android.systemui.util.wakelock.WakeLock_Builder_Factory;
import com.android.systemui.volume.VolumeDialogComponent;
import com.android.systemui.volume.VolumeDialogComponent_Factory;
import com.android.systemui.volume.VolumeDialogControllerImpl;
import com.android.systemui.volume.VolumeDialogControllerImpl_Factory;
import com.android.systemui.volume.VolumeUI;
import com.android.systemui.volume.VolumeUI_Factory;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayController_Factory;
import com.android.systemui.wm.DisplayImeController;
import com.android.systemui.wm.DisplayImeController_Factory;
import com.android.systemui.wm.SystemWindows;
import com.android.systemui.wm.SystemWindows_Factory;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager_Factory;
import com.oneplus.battery.OpChargingAnimationControllerImpl;
import com.oneplus.battery.OpChargingAnimationControllerImpl_Factory;
import com.oneplus.networkspeed.NetworkSpeedControllerImpl;
import com.oneplus.networkspeed.NetworkSpeedControllerImpl_Factory;
import com.oneplus.notification.OpNotificationController;
import com.oneplus.notification.OpNotificationController_Factory;
import com.oneplus.opthreekey.OpThreekeyVolumeGuideControllerImpl;
import com.oneplus.opthreekey.OpThreekeyVolumeGuideControllerImpl_Factory;
import com.oneplus.opzenmode.OpZenModeControllerImpl;
import com.oneplus.opzenmode.OpZenModeControllerImpl_Factory;
import com.oneplus.scene.OpSceneModeObserver;
import com.oneplus.scene.OpSceneModeObserver_Factory;
import com.oneplus.systemui.biometrics.OpBiometricDialogImpl;
import com.oneplus.systemui.biometrics.OpBiometricDialogImpl_Factory;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintControllerImpl;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintControllerImpl_Factory;
import com.oneplus.worklife.OPWLBHelper;
import com.oneplus.worklife.OPWLBHelper_Factory;
import dagger.Lazy;
import dagger.internal.DelegateFactory;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.InstanceFactory;
import dagger.internal.MapProviderFactory;
import dagger.internal.Preconditions;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import javax.inject.Provider;
public final class DaggerTvSystemUIRootComponent implements TvSystemUIRootComponent {
    private static final Provider ABSENT_JDK_OPTIONAL_PROVIDER = InstanceFactory.create(Optional.empty());
    private Provider<AccessibilityController> accessibilityControllerProvider;
    private Provider<AccessibilityManagerWrapper> accessibilityManagerWrapperProvider;
    private ActionClickLogger_Factory actionClickLoggerProvider;
    private GlobalScreenshot_ActionProxyReceiver_Factory actionProxyReceiverProvider;
    private Provider<ActivityIntentHelper> activityIntentHelperProvider;
    private Provider<ActivityStarterDelegate> activityStarterDelegateProvider;
    private AirplaneModeTile_Factory airplaneModeTileProvider;
    private Provider<AppOpsControllerImpl> appOpsControllerImplProvider;
    private Provider<AppOpsCoordinator> appOpsCoordinatorProvider;
    private Provider<AssistHandleBehaviorController> assistHandleBehaviorControllerProvider;
    private Provider assistHandleLikeHomeBehaviorProvider;
    private Provider assistHandleOffBehaviorProvider;
    private Provider assistHandleReminderExpBehaviorProvider;
    private Provider<AssistLogger> assistLoggerProvider;
    private Provider<AssistManager> assistManagerProvider;
    private Provider<AsyncSensorManager> asyncSensorManagerProvider;
    private Provider<AuthController> authControllerProvider;
    private AutoTileManager_Factory autoTileManagerProvider;
    private BatterySaverTile_Factory batterySaverTileProvider;
    private Provider<SystemClock> bindSystemClockProvider;
    private Provider<BiometricUnlockController> biometricUnlockControllerProvider;
    private Provider<BluetoothControllerImpl> bluetoothControllerImplProvider;
    private BluetoothTile_Factory bluetoothTileProvider;
    private Provider<BlurUtils> blurUtilsProvider;
    private Provider<BootCompleteCacheImpl> bootCompleteCacheImplProvider;
    private BrightnessDialog_Factory brightnessDialogProvider;
    private BroadcastDispatcherLogger_Factory broadcastDispatcherLoggerProvider;
    private Provider<BubbleCoordinator> bubbleCoordinatorProvider;
    private Provider<BubbleData> bubbleDataProvider;
    private Provider<BubbleDataRepository> bubbleDataRepositoryProvider;
    private BubbleOverflowActivity_Factory bubbleOverflowActivityProvider;
    private Provider<BubblePersistentRepository> bubblePersistentRepositoryProvider;
    private Provider<BubbleVolatileRepository> bubbleVolatileRepositoryProvider;
    private NotificationClicker_Builder_Factory builderProvider;
    private WakeLock_Builder_Factory builderProvider2;
    private DelayedWakeLock_Builder_Factory builderProvider3;
    private Provider<StatusBarNotificationActivityStarter.Builder> builderProvider4;
    private AutoAddTracker_Builder_Factory builderProvider5;
    private Provider<BypassHeadsUpNotifier> bypassHeadsUpNotifierProvider;
    private Provider<CastControllerImpl> castControllerImplProvider;
    private CastTile_Factory castTileProvider;
    private CellularTile_Factory cellularTileProvider;
    private Provider<ChannelEditorDialogController> channelEditorDialogControllerProvider;
    private Provider<ClockManager> clockManagerProvider;
    private ColorInversionTile_Factory colorInversionTileProvider;
    private Context context;
    private Provider<ContextComponentResolver> contextComponentResolverProvider;
    private Provider<Context> contextProvider;
    private Provider<ControlActionCoordinatorImpl> controlActionCoordinatorImplProvider;
    private Provider<ControlsBindingControllerImpl> controlsBindingControllerImplProvider;
    private Provider<ControlsComponent> controlsComponentProvider;
    private Provider<ControlsControllerImpl> controlsControllerImplProvider;
    private ControlsEditingActivity_Factory controlsEditingActivityProvider;
    private ControlsFavoritingActivity_Factory controlsFavoritingActivityProvider;
    private Provider<ControlsListingControllerImpl> controlsListingControllerImplProvider;
    private ControlsProviderSelectorActivity_Factory controlsProviderSelectorActivityProvider;
    private ControlsRequestDialog_Factory controlsRequestDialogProvider;
    private Provider<ControlsUiControllerImpl> controlsUiControllerImplProvider;
    private Provider<ConversationCoordinator> conversationCoordinatorProvider;
    private Provider<ConversationNotificationManager> conversationNotificationManagerProvider;
    private ConversationNotificationProcessor_Factory conversationNotificationProcessorProvider;
    private Provider<DarkIconDispatcherImpl> darkIconDispatcherImplProvider;
    private DataSaverTile_Factory dataSaverTileProvider;
    private DataSwitchTile_Factory dataSwitchTileProvider;
    private DateFormatUtil_Factory dateFormatUtilProvider;
    private Provider<DefaultUiController> defaultUiControllerProvider;
    private Provider<DeviceConfigHelper> deviceConfigHelperProvider;
    private Provider<DeviceProvisionedControllerImpl> deviceProvisionedControllerImplProvider;
    private Provider<DeviceProvisionedCoordinator> deviceProvisionedCoordinatorProvider;
    private Provider<DismissCallbackRegistry> dismissCallbackRegistryProvider;
    private Provider<DisplayController> displayControllerProvider;
    private Provider<DisplayImeController> displayImeControllerProvider;
    private DndTile_Factory dndTileProvider;
    private Provider<DockManagerImpl> dockManagerImplProvider;
    private DozeFactory_Factory dozeFactoryProvider;
    private Provider<DozeLog> dozeLogProvider;
    private DozeLogger_Factory dozeLoggerProvider;
    private Provider<DozeParameters> dozeParametersProvider;
    private Provider<DozeScrimController> dozeScrimControllerProvider;
    private Provider<DozeServiceHost> dozeServiceHostProvider;
    private DozeService_Factory dozeServiceProvider;
    private DumpHandler_Factory dumpHandlerProvider;
    private Provider<DumpManager> dumpManagerProvider;
    private DynamicChildBindController_Factory dynamicChildBindControllerProvider;
    private Provider<DynamicPrivacyController> dynamicPrivacyControllerProvider;
    private Provider<EnhancedEstimatesImpl> enhancedEstimatesImplProvider;
    private Provider<ExpandableNotificationRowComponent.Builder> expandableNotificationRowComponentBuilderProvider;
    private NotificationLogger_ExpansionStateLogger_Factory expansionStateLoggerProvider;
    private Provider<ExtensionControllerImpl> extensionControllerImplProvider;
    private Provider<FalsingManagerProxy> falsingManagerProxyProvider;
    private Provider<FeatureFlags> featureFlagsProvider;
    private Provider<Files> filesProvider;
    private Provider<FlashlightControllerImpl> flashlightControllerImplProvider;
    private FlashlightTile_Factory flashlightTileProvider;
    private Provider<FloatingContentCoordinator> floatingContentCoordinatorProvider;
    private Provider<ForegroundServiceController> foregroundServiceControllerProvider;
    private Provider<ForegroundServiceDismissalFeatureController> foregroundServiceDismissalFeatureControllerProvider;
    private ForegroundServiceLifetimeExtender_Factory foregroundServiceLifetimeExtenderProvider;
    private Provider<ForegroundServiceNotificationListener> foregroundServiceNotificationListenerProvider;
    private Provider<ForegroundServiceSectionController> foregroundServiceSectionControllerProvider;
    private Provider<FragmentService> fragmentServiceProvider;
    private GameModeTile_Factory gameModeTileProvider;
    private Provider<GarbageMonitor> garbageMonitorProvider;
    private Provider<GlobalActionsComponent> globalActionsComponentProvider;
    private GlobalActionsDialog_Factory globalActionsDialogProvider;
    private GlobalActionsImpl_Factory globalActionsImplProvider;
    private Provider<GlobalScreenshot> globalScreenshotProvider;
    private GroupCoalescerLogger_Factory groupCoalescerLoggerProvider;
    private GroupCoalescer_Factory groupCoalescerProvider;
    private Provider<HeadsUpController> headsUpControllerProvider;
    private Provider<HeadsUpCoordinator> headsUpCoordinatorProvider;
    private Provider<HeadsUpViewBinder> headsUpViewBinderProvider;
    private HideNotifsForOtherUsersCoordinator_Factory hideNotifsForOtherUsersCoordinatorProvider;
    private Provider<HighPriorityProvider> highPriorityProvider;
    private Provider<HotspotControllerImpl> hotspotControllerImplProvider;
    private HotspotTile_Factory hotspotTileProvider;
    private IconBuilder_Factory iconBuilderProvider;
    private IconManager_Factory iconManagerProvider;
    private Provider<InitController> initControllerProvider;
    private Provider<InjectionInflationController> injectionInflationControllerProvider;
    private Provider<InstantAppNotifier> instantAppNotifierProvider;
    private Provider<KeyguardBypassController> keyguardBypassControllerProvider;
    private Provider<KeyguardCoordinator> keyguardCoordinatorProvider;
    private Provider<KeyguardDismissUtil> keyguardDismissUtilProvider;
    private Provider<KeyguardEnvironmentImpl> keyguardEnvironmentImplProvider;
    private Provider<KeyguardIndicationController> keyguardIndicationControllerProvider;
    private Provider<KeyguardLifecyclesDispatcher> keyguardLifecyclesDispatcherProvider;
    private Provider<KeyguardMediaController> keyguardMediaControllerProvider;
    private Provider<KeyguardSecurityModel> keyguardSecurityModelProvider;
    private KeyguardService_Factory keyguardServiceProvider;
    private Provider<KeyguardStateControllerImpl> keyguardStateControllerImplProvider;
    private Provider<KeyguardUpdateMonitor> keyguardUpdateMonitorProvider;
    private Provider<LatencyTester> latencyTesterProvider;
    private Provider<LeakReporter> leakReporterProvider;
    private Provider<LightBarController> lightBarControllerProvider;
    private Provider<LightsOutNotifController> lightsOutNotifControllerProvider;
    private LocalMediaManagerFactory_Factory localMediaManagerFactoryProvider;
    private Provider<LocationControllerImpl> locationControllerImplProvider;
    private LocationTile_Factory locationTileProvider;
    private Provider<LockscreenGestureLogger> lockscreenGestureLoggerProvider;
    private Provider<LockscreenLockIconController> lockscreenLockIconControllerProvider;
    private Provider<LockscreenWallpaper> lockscreenWallpaperProvider;
    private Provider<LogBufferEulogizer> logBufferEulogizerProvider;
    private LogBufferFreezer_Factory logBufferFreezerProvider;
    private Provider<LowPriorityInflationHelper> lowPriorityInflationHelperProvider;
    private Provider<ManagedProfileControllerImpl> managedProfileControllerImplProvider;
    private Provider<Map<Class<?>, Provider<Activity>>> mapOfClassOfAndProviderOfActivityProvider;
    private Provider<Map<Class<?>, Provider<BroadcastReceiver>>> mapOfClassOfAndProviderOfBroadcastReceiverProvider;
    private Provider<Map<Class<?>, Provider<RecentsImplementation>>> mapOfClassOfAndProviderOfRecentsImplementationProvider;
    private Provider<Map<Class<?>, Provider<Service>>> mapOfClassOfAndProviderOfServiceProvider;
    private Provider<Map<Class<?>, Provider<SystemUI>>> mapOfClassOfAndProviderOfSystemUIProvider;
    private Provider<MediaArtworkProcessor> mediaArtworkProcessorProvider;
    private Provider<MediaCarouselController> mediaCarouselControllerProvider;
    private MediaControlPanel_Factory mediaControlPanelProvider;
    private MediaControllerFactory_Factory mediaControllerFactoryProvider;
    private MediaCoordinator_Factory mediaCoordinatorProvider;
    private Provider<MediaDataCombineLatest> mediaDataCombineLatestProvider;
    private Provider<MediaDataFilter> mediaDataFilterProvider;
    private Provider<MediaDataManager> mediaDataManagerProvider;
    private Provider<MediaDeviceManager> mediaDeviceManagerProvider;
    private MediaFeatureFlag_Factory mediaFeatureFlagProvider;
    private Provider<MediaHierarchyManager> mediaHierarchyManagerProvider;
    private MediaHost_Factory mediaHostProvider;
    private Provider<MediaHostStatesManager> mediaHostStatesManagerProvider;
    private Provider<MediaResumeListener> mediaResumeListenerProvider;
    private Provider<MediaTimeoutListener> mediaTimeoutListenerProvider;
    private MediaViewController_Factory mediaViewControllerProvider;
    private GarbageMonitor_MemoryTile_Factory memoryTileProvider;
    private Provider<NavigationModeController> navigationModeControllerProvider;
    private Provider<NetworkControllerImpl> networkControllerImplProvider;
    private Provider<NetworkSpeedControllerImpl> networkSpeedControllerImplProvider;
    private Provider<BubbleController> newBubbleControllerProvider;
    private Provider<KeyguardViewMediator> newKeyguardViewMediatorProvider;
    private Provider<NextAlarmControllerImpl> nextAlarmControllerImplProvider;
    private NfcTile_Factory nfcTileProvider;
    private NightDisplayTile_Factory nightDisplayTileProvider;
    private NotifBindPipelineInitializer_Factory notifBindPipelineInitializerProvider;
    private NotifBindPipelineLogger_Factory notifBindPipelineLoggerProvider;
    private Provider<NotifBindPipeline> notifBindPipelineProvider;
    private NotifCollectionLogger_Factory notifCollectionLoggerProvider;
    private Provider<NotifCollection> notifCollectionProvider;
    private Provider<NotifCoordinators> notifCoordinatorsProvider;
    private Provider<NotifInflaterImpl> notifInflaterImplProvider;
    private Provider<NotifInflationErrorManager> notifInflationErrorManagerProvider;
    private Provider<NotifPipelineInitializer> notifPipelineInitializerProvider;
    private Provider<NotifPipeline> notifPipelineProvider;
    private NotifRemoteViewCacheImpl_Factory notifRemoteViewCacheImplProvider;
    private Provider<NotifViewBarn> notifViewBarnProvider;
    private Provider<NotifViewManager> notifViewManagerProvider;
    private Provider<NotificationClickNotifier> notificationClickNotifierProvider;
    private NotificationClickerLogger_Factory notificationClickerLoggerProvider;
    private Provider<NotificationContentInflater> notificationContentInflaterProvider;
    private NotificationEntryManagerLogger_Factory notificationEntryManagerLoggerProvider;
    private Provider<NotificationFilter> notificationFilterProvider;
    private Provider<NotificationGroupManager> notificationGroupManagerProvider;
    private Provider<NotificationInteractionTracker> notificationInteractionTrackerProvider;
    private Provider<NotificationInterruptStateProviderImpl> notificationInterruptStateProviderImplProvider;
    private Provider<NotificationLockscreenUserManagerImpl> notificationLockscreenUserManagerImplProvider;
    private Provider<NotificationPersonExtractorPluginBoundary> notificationPersonExtractorPluginBoundaryProvider;
    private NotificationRankingManager_Factory notificationRankingManagerProvider;
    private Provider<NotificationRoundnessManager> notificationRoundnessManagerProvider;
    private Provider<NotificationRowBinderImpl> notificationRowBinderImplProvider;
    private Provider<NotificationRowComponent.Builder> notificationRowComponentBuilderProvider;
    private NotificationSectionsFeatureManager_Factory notificationSectionsFeatureManagerProvider;
    private Provider<NotificationSectionsLogger> notificationSectionsLoggerProvider;
    private Provider<NotificationShadeDepthController> notificationShadeDepthControllerProvider;
    private Provider<NotificationShadeWindowController> notificationShadeWindowControllerProvider;
    private Provider<NotificationWakeUpCoordinator> notificationWakeUpCoordinatorProvider;
    private Provider<NotificationsControllerImpl> notificationsControllerImplProvider;
    private NotificationsControllerStub_Factory notificationsControllerStubProvider;
    private OPDndCarModeTile_Factory oPDndCarModeTileProvider;
    private OPDndTile_Factory oPDndTileProvider;
    private OPReverseChargeTile_Factory oPReverseChargeTileProvider;
    private Provider<OPWLBHelper> oPWLBHelperProvider;
    private Provider<OpBiometricDialogImpl> opBiometricDialogImplProvider;
    private Provider<OpBitmojiManager> opBitmojiManagerProvider;
    private Provider<OpChargingAnimationControllerImpl> opChargingAnimationControllerImplProvider;
    private Provider<OpHighlightHintControllerImpl> opHighlightHintControllerImplProvider;
    private Provider<OpNotificationController> opNotificationControllerProvider;
    private Provider<OpSceneModeObserver> opSceneModeObserverProvider;
    private Provider<OpThreekeyVolumeGuideControllerImpl> opThreekeyVolumeGuideControllerImplProvider;
    private Provider<OpZenModeControllerImpl> opZenModeControllerImplProvider;
    private Provider<Optional<ControlsFavoritePersistenceWrapper>> optionalOfControlsFavoritePersistenceWrapperProvider;
    private Provider<Optional<Divider>> optionalOfDividerProvider;
    private Provider<Optional<Lazy<Recents>>> optionalOfLazyOfRecentsProvider;
    private Provider<Optional<Lazy<StatusBar>>> optionalOfLazyOfStatusBarProvider;
    private Provider<Optional<Recents>> optionalOfRecentsProvider;
    private Provider<Optional<StatusBar>> optionalOfStatusBarProvider;
    private OtgTile_Factory otgTileProvider;
    private Provider<OverviewProxyRecentsImpl> overviewProxyRecentsImplProvider;
    private Provider<OverviewProxyService> overviewProxyServiceProvider;
    private Provider<PeopleHubDataSourceImpl> peopleHubDataSourceImplProvider;
    private Provider<PeopleHubViewAdapterImpl> peopleHubViewAdapterImplProvider;
    private Provider<PeopleHubViewModelFactoryDataSourceImpl> peopleHubViewModelFactoryDataSourceImplProvider;
    private Provider<PeopleNotificationIdentifierImpl> peopleNotificationIdentifierImplProvider;
    private Provider<PhoneStateMonitor> phoneStateMonitorProvider;
    private PhoneStatusBarPolicy_Factory phoneStatusBarPolicyProvider;
    private PipAnimationController_Factory pipAnimationControllerProvider;
    private Provider<PipBoundsHandler> pipBoundsHandlerProvider;
    private Provider<PipManager> pipManagerProvider;
    private PipMenuActivity_Factory pipMenuActivityProvider;
    private PipSnapAlgorithm_Factory pipSnapAlgorithmProvider;
    private Provider<PipSurfaceTransactionHelper> pipSurfaceTransactionHelperProvider;
    private Provider<PipTaskOrganizer> pipTaskOrganizerProvider;
    private Provider<PipUI> pipUIProvider;
    private Provider<PluginDependencyProvider> pluginDependencyProvider;
    private Provider<PowerNotificationWarnings> powerNotificationWarningsProvider;
    private Provider<PowerUI> powerUIProvider;
    private PreparationCoordinatorLogger_Factory preparationCoordinatorLoggerProvider;
    private Provider<PreparationCoordinator> preparationCoordinatorProvider;
    private Provider<ProtoTracer> protoTracerProvider;
    private Provider<AccessibilityManager> provideAccessibilityManagerProvider;
    private Provider<ActivityManager> provideActivityManagerProvider;
    private Provider<ActivityManagerWrapper> provideActivityManagerWrapperProvider;
    private Provider<AlarmManager> provideAlarmManagerProvider;
    private Provider<Boolean> provideAllowNotificationLongPressProvider;
    private DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory provideAlwaysOnDisplayPolicyProvider;
    private DependencyProvider_ProvideAmbientDisplayConfigurationFactory provideAmbientDisplayConfigurationProvider;
    private Provider provideAssistHandleBehaviorControllerMapProvider;
    private AssistModule_ProvideAssistHandleViewControllerFactory provideAssistHandleViewControllerProvider;
    private Provider<AssistUtils> provideAssistUtilsProvider;
    private Provider<AudioManager> provideAudioManagerProvider;
    private Provider<AutoHideController> provideAutoHideControllerProvider;
    private Provider<DelayableExecutor> provideBackgroundDelayableExecutorProvider;
    private Provider<Executor> provideBackgroundExecutorProvider;
    private Provider<Handler> provideBackgroundHandlerProvider;
    private Provider<RepeatableExecutor> provideBackgroundRepeatableExecutorProvider;
    private Provider<BatteryController> provideBatteryControllerProvider;
    private ConcurrencyModule_ProvideBgHandlerFactory provideBgHandlerProvider;
    private Provider<Looper> provideBgLooperProvider;
    private Provider<LogBuffer> provideBroadcastDispatcherLogBufferProvider;
    private Provider<CommandQueue> provideCommandQueueProvider;
    private Provider<CommonNotifCollection> provideCommonNotifCollectionProvider;
    private Provider<ConfigurationController> provideConfigurationControllerProvider;
    private Provider<ConnectivityManager> provideConnectivityManagagerProvider;
    private Provider<ContentResolver> provideContentResolverProvider;
    private Provider<CurrentUserContextTracker> provideCurrentUserContextTrackerProvider;
    private Provider<DataSaverController> provideDataSaverControllerProvider;
    private Provider<DelayableExecutor> provideDelayableExecutorProvider;
    private Provider<DevicePolicyManager> provideDevicePolicyManagerProvider;
    private Provider<DevicePolicyManagerWrapper> provideDevicePolicyManagerWrapperProvider;
    private SystemServicesModule_ProvideDisplayIdFactory provideDisplayIdProvider;
    private Provider<DisplayMetrics> provideDisplayMetricsProvider;
    private Provider<Divider> provideDividerProvider;
    private Provider<LogBuffer> provideDozeLogBufferProvider;
    private Provider<Executor> provideExecutorProvider;
    private Provider<HeadsUpManagerPhone> provideHeadsUpManagerPhoneProvider;
    private Provider<IActivityManager> provideIActivityManagerProvider;
    private Provider<IBatteryStats> provideIBatteryStatsProvider;
    private Provider<IDreamManager> provideIDreamManagerProvider;
    private Provider<INotificationManager> provideINotificationManagerProvider;
    private Provider<IPackageManager> provideIPackageManagerProvider;
    private Provider<IStatusBarService> provideIStatusBarServiceProvider;
    private Provider<IWindowManager> provideIWindowManagerProvider;
    private Provider<KeyguardLiftController> provideKeyguardLiftControllerProvider;
    private Provider<KeyguardManager> provideKeyguardManagerProvider;
    private Provider<LatencyTracker> provideLatencyTrackerProvider;
    private Provider<LauncherApps> provideLauncherAppsProvider;
    private Provider<LeakDetector> provideLeakDetectorProvider;
    private Provider<String> provideLeakReportEmailProvider;
    private Provider<LocalBluetoothManager> provideLocalBluetoothControllerProvider;
    private DependencyProvider_ProvideLockPatternUtilsFactory provideLockPatternUtilsProvider;
    private Provider<LogcatEchoTracker> provideLogcatEchoTrackerProvider;
    private Provider<Executor> provideLongRunningExecutorProvider;
    private Provider<Looper> provideLongRunningLooperProvider;
    private Provider<DelayableExecutor> provideMainDelayableExecutorProvider;
    private ConcurrencyModule_ProvideMainExecutorFactory provideMainExecutorProvider;
    private ConcurrencyModule_ProvideMainHandlerFactory provideMainHandlerProvider;
    private SystemServicesModule_ProvideMediaRouter2ManagerFactory provideMediaRouter2ManagerProvider;
    private Provider<MetricsLogger> provideMetricsLoggerProvider;
    private Provider<NavigationBarController> provideNavigationBarControllerProvider;
    private Provider<NetworkScoreManager> provideNetworkScoreManagerProvider;
    private Provider<NightDisplayListener> provideNightDisplayListenerProvider;
    private Provider<LogBuffer> provideNotifInteractionLogBufferProvider;
    private Provider<NotifRemoteViewCache> provideNotifRemoteViewCacheProvider;
    private Provider<NotificationBlockingHelperManager> provideNotificationBlockingHelperManagerProvider;
    private Provider<NotificationEntryManager> provideNotificationEntryManagerProvider;
    private Provider<NotificationGroupAlertTransferHelper> provideNotificationGroupAlertTransferHelperProvider;
    private Provider<NotificationGutsManager> provideNotificationGutsManagerProvider;
    private Provider<NotificationListener> provideNotificationListenerProvider;
    private Provider<NotificationLogger> provideNotificationLoggerProvider;
    private Provider<NotificationManager> provideNotificationManagerProvider;
    private Provider<NotificationMediaManager> provideNotificationMediaManagerProvider;
    private DependencyProvider_ProvideNotificationMessagingUtilFactory provideNotificationMessagingUtilProvider;
    private Provider<NotificationPanelLogger> provideNotificationPanelLoggerProvider;
    private Provider<NotificationRemoteInputManager> provideNotificationRemoteInputManagerProvider;
    private Provider<LogBuffer> provideNotificationSectionLogBufferProvider;
    private Provider<NotificationViewHierarchyManager> provideNotificationViewHierarchyManagerProvider;
    private Provider<NotificationsController> provideNotificationsControllerProvider;
    private Provider<LogBuffer> provideNotificationsLogBufferProvider;
    private Provider<PackageManager> providePackageManagerProvider;
    private Provider<PackageManagerWrapper> providePackageManagerWrapperProvider;
    private Provider<PluginManager> providePluginManagerProvider;
    private Provider<PowerManager> providePowerManagerProvider;
    private Provider<LogBuffer> provideQuickSettingsLogBufferProvider;
    private RecentsModule_ProvideRecentsImplFactory provideRecentsImplProvider;
    private Provider<Recents> provideRecentsProvider;
    private SystemServicesModule_ProvideResourcesFactory provideResourcesProvider;
    private Provider<SensorPrivacyManager> provideSensorPrivacyManagerProvider;
    private DependencyProvider_ProvideSharePreferencesFactory provideSharePreferencesProvider;
    private Provider<ShortcutManager> provideShortcutManagerProvider;
    private Provider<SmartReplyController> provideSmartReplyControllerProvider;
    private Provider<StatusBar> provideStatusBarProvider;
    private Provider<SysUiState> provideSysUiStateProvider;
    private Provider<Clock> provideSystemClockProvider;
    private Provider<TelecomManager> provideTelecomManagerProvider;
    private Provider<TelephonyManager> provideTelephonyManagerProvider;
    private Provider<Handler> provideTimeTickHandlerProvider;
    private Provider<TrustManager> provideTrustManagerProvider;
    private Provider<Executor> provideUiBackgroundExecutorProvider;
    private Provider<UiEventLogger> provideUiEventLoggerProvider;
    private Provider<UserManager> provideUserManagerProvider;
    private Provider<Vibrator> provideVibratorProvider;
    private Provider<VisualStabilityManager> provideVisualStabilityManagerProvider;
    private SystemServicesModule_ProvideWallpaperManagerFactory provideWallpaperManagerProvider;
    private Provider<WifiManager> provideWifiManagerProvider;
    private Provider<WindowManager> provideWindowManagerProvider;
    private Provider<LayoutInflater> providerLayoutInflaterProvider;
    private Provider<BroadcastDispatcher> providesBroadcastDispatcherProvider;
    private Provider<Choreographer> providesChoreographerProvider;
    private Provider<Boolean> providesControlsFeatureEnabledProvider;
    private DependencyProvider_ProvidesViewMediatorCallbackFactory providesViewMediatorCallbackProvider;
    private ProximitySensor_ProximityCheck_Factory proximityCheckProvider;
    private ProximitySensor_Factory proximitySensorProvider;
    private Provider<PulseExpansionHandler> pulseExpansionHandlerProvider;
    private Provider<QSFactoryImpl> qSFactoryImplProvider;
    private QSLogger_Factory qSLoggerProvider;
    private Provider<QSTileHost> qSTileHostProvider;
    private Provider<RankingCoordinator> rankingCoordinatorProvider;
    private ReadModeTile_Factory readModeTileProvider;
    private Provider<RecordingController> recordingControllerProvider;
    private RecordingService_Factory recordingServiceProvider;
    private Provider<RemoteInputQuickSettingsDisabler> remoteInputQuickSettingsDisablerProvider;
    private Provider<RemoteInputUriController> remoteInputUriControllerProvider;
    private Provider<RingerModeTrackerImpl> ringerModeTrackerImplProvider;
    private Provider<RotationLockControllerImpl> rotationLockControllerImplProvider;
    private RotationLockTile_Factory rotationLockTileProvider;
    private RowContentBindStageLogger_Factory rowContentBindStageLoggerProvider;
    private Provider<RowContentBindStage> rowContentBindStageProvider;
    private Provider<ScreenDecorations> screenDecorationsProvider;
    private Provider<ScreenLifecycle> screenLifecycleProvider;
    private ScreenPinningRequest_Factory screenPinningRequestProvider;
    private ScreenRecordDialog_Factory screenRecordDialogProvider;
    private ScreenRecordTile_Factory screenRecordTileProvider;
    private ScreenshotNotificationsController_Factory screenshotNotificationsControllerProvider;
    private Provider<ScrimController> scrimControllerProvider;
    private Provider<SecurityControllerImpl> securityControllerImplProvider;
    private SeekBarViewModel_Factory seekBarViewModelProvider;
    private Provider<SensorPrivacyControllerImpl> sensorPrivacyControllerImplProvider;
    private Provider<GarbageMonitor.Service> serviceProvider;
    private Provider<ShadeControllerImpl> shadeControllerImplProvider;
    private ShadeListBuilderLogger_Factory shadeListBuilderLoggerProvider;
    private Provider<ShadeListBuilder> shadeListBuilderProvider;
    private SharedCoordinatorLogger_Factory sharedCoordinatorLoggerProvider;
    private Provider<ShortcutKeyDispatcher> shortcutKeyDispatcherProvider;
    private Provider<SizeCompatModeActivityController> sizeCompatModeActivityControllerProvider;
    private Provider<SliceBroadcastRelayHandler> sliceBroadcastRelayHandlerProvider;
    private Provider<SmartReplyConstants> smartReplyConstantsProvider;
    private Provider<StatusBarComponent.Builder> statusBarComponentBuilderProvider;
    private Provider<StatusBarIconControllerImpl> statusBarIconControllerImplProvider;
    private Provider<StatusBarKeyguardViewManager> statusBarKeyguardViewManagerProvider;
    private StatusBarNotificationActivityStarterLogger_Factory statusBarNotificationActivityStarterLoggerProvider;
    private Provider<StatusBarRemoteInputCallback> statusBarRemoteInputCallbackProvider;
    private Provider<StatusBarStateControllerImpl> statusBarStateControllerImplProvider;
    private Provider<StatusBarTouchableRegionManager> statusBarTouchableRegionManagerProvider;
    private Provider<StatusBarWindowController> statusBarWindowControllerProvider;
    private Provider<SuperStatusBarViewFactory> superStatusBarViewFactoryProvider;
    private Provider<SystemActions> systemActionsProvider;
    private SystemUIAuxiliaryDumpService_Factory systemUIAuxiliaryDumpServiceProvider;
    private SystemUIService_Factory systemUIServiceProvider;
    private Provider<SystemWindows> systemWindowsProvider;
    private Provider<SysuiColorExtractor> sysuiColorExtractorProvider;
    private TakeScreenshotService_Factory takeScreenshotServiceProvider;
    private Provider<TargetSdkResolver> targetSdkResolverProvider;
    private Provider<ThemeOverlayController> themeOverlayControllerProvider;
    private Provider<ToastUI> toastUIProvider;
    private Provider<TransactionPool> transactionPoolProvider;
    private Provider<TunablePadding.TunablePaddingService> tunablePaddingServiceProvider;
    private Provider<TunerServiceImpl> tunerServiceImplProvider;
    private Provider<TvPipComponent.Builder> tvPipComponentBuilderProvider;
    private Provider<TvStatusBar> tvStatusBarProvider;
    private Provider<TvSystemUIRootComponent> tvSystemUIRootComponentProvider;
    private UiModeNightTile_Factory uiModeNightTileProvider;
    private Provider<UiOffloadThread> uiOffloadThreadProvider;
    private UsbDebuggingActivity_Factory usbDebuggingActivityProvider;
    private UsbDebuggingSecondaryUserActivity_Factory usbDebuggingSecondaryUserActivityProvider;
    private Provider<UserInfoControllerImpl> userInfoControllerImplProvider;
    private Provider<UserSwitcherController> userSwitcherControllerProvider;
    private UserTile_Factory userTileProvider;
    private VPNTile_Factory vPNTileProvider;
    private Provider<VibratorHelper> vibratorHelperProvider;
    private Provider<VolumeDialogComponent> volumeDialogComponentProvider;
    private Provider<VolumeDialogControllerImpl> volumeDialogControllerImplProvider;
    private Provider<VolumeUI> volumeUIProvider;
    private Provider<WakefulnessLifecycle> wakefulnessLifecycleProvider;
    private WifiTile_Factory wifiTileProvider;
    private Provider<WindowMagnification> windowMagnificationProvider;
    private WorkLockActivity_Factory workLockActivityProvider;
    private WorkModeTile_Factory workModeTileProvider;
    private Provider<ZenModeControllerImpl> zenModeControllerImplProvider;

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public void inject(ContentProvider contentProvider) {
    }

    private DaggerTvSystemUIRootComponent(Builder builder) {
        initialize(builder);
        initialize2(builder);
        initialize3(builder);
        initialize4(builder);
        initialize5(builder);
    }

    public static TvSystemUIRootComponent.Builder builder() {
        return new Builder();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Handler getMainHandler() {
        return ConcurrencyModule_ProvideMainHandlerFactory.proxyProvideMainHandler(ConcurrencyModule_ProvideMainLooperFactory.proxyProvideMainLooper());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Handler getBackgroundHandler() {
        return ConcurrencyModule_ProvideBgHandlerFactory.proxyProvideBgHandler(this.provideBgLooperProvider.get());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Resources getMainResources() {
        return SystemServicesModule_ProvideResourcesFactory.proxyProvideResources(this.context);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NotificationSectionsFeatureManager getNotificationSectionsFeatureManager() {
        return new NotificationSectionsFeatureManager(new DeviceConfigProxy(), this.context);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private QSLogger getQSLogger() {
        return new QSLogger(this.provideQuickSettingsLogBufferProvider.get());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private MediaHost getMediaHost() {
        return new MediaHost(new MediaHost.MediaHostStateHolder(), this.mediaHierarchyManagerProvider.get(), this.mediaDataFilterProvider.get(), this.mediaHostStatesManagerProvider.get(), this.provideNotificationMediaManagerProvider.get());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Executor getMainExecutor() {
        return ConcurrencyModule_ProvideMainExecutorFactory.proxyProvideMainExecutor(this.context);
    }

    private void initialize(Builder builder) {
        Provider<DumpManager> provider = DoubleCheck.provider(DumpManager_Factory.create());
        this.dumpManagerProvider = provider;
        this.bootCompleteCacheImplProvider = DoubleCheck.provider(BootCompleteCacheImpl_Factory.create(provider));
        this.contextProvider = InstanceFactory.create(builder.context);
        this.provideConfigurationControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideConfigurationControllerFactory.create(builder.dependencyProvider, this.contextProvider));
        Provider<Looper> provider2 = DoubleCheck.provider(ConcurrencyModule_ProvideBgLooperFactory.create());
        this.provideBgLooperProvider = provider2;
        this.provideBackgroundExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideBackgroundExecutorFactory.create(provider2));
        Provider<ContentResolver> provider3 = DoubleCheck.provider(SystemServicesModule_ProvideContentResolverFactory.create(this.contextProvider));
        this.provideContentResolverProvider = provider3;
        Provider<LogcatEchoTracker> provider4 = DoubleCheck.provider(LogModule_ProvideLogcatEchoTrackerFactory.create(provider3, ConcurrencyModule_ProvideMainLooperFactory.create()));
        this.provideLogcatEchoTrackerProvider = provider4;
        Provider<LogBuffer> provider5 = DoubleCheck.provider(LogModule_ProvideBroadcastDispatcherLogBufferFactory.create(provider4, this.dumpManagerProvider));
        this.provideBroadcastDispatcherLogBufferProvider = provider5;
        this.broadcastDispatcherLoggerProvider = BroadcastDispatcherLogger_Factory.create(provider5);
        Provider<BroadcastDispatcher> provider6 = DoubleCheck.provider(DependencyProvider_ProvidesBroadcastDispatcherFactory.create(builder.dependencyProvider, this.contextProvider, this.provideBgLooperProvider, this.provideBackgroundExecutorProvider, this.dumpManagerProvider, this.broadcastDispatcherLoggerProvider));
        this.providesBroadcastDispatcherProvider = provider6;
        this.workLockActivityProvider = WorkLockActivity_Factory.create(provider6);
        this.brightnessDialogProvider = BrightnessDialog_Factory.create(this.providesBroadcastDispatcherProvider);
        this.recordingControllerProvider = DoubleCheck.provider(RecordingController_Factory.create(this.providesBroadcastDispatcherProvider));
        Provider<CurrentUserContextTracker> provider7 = DoubleCheck.provider(SettingsModule_ProvideCurrentUserContextTrackerFactory.create(this.contextProvider, this.providesBroadcastDispatcherProvider));
        this.provideCurrentUserContextTrackerProvider = provider7;
        this.screenRecordDialogProvider = ScreenRecordDialog_Factory.create(this.recordingControllerProvider, provider7);
        this.provideWindowManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideWindowManagerFactory.create(this.contextProvider));
        this.provideIActivityManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideIActivityManagerFactory.create());
        this.provideResourcesProvider = SystemServicesModule_ProvideResourcesFactory.create(this.contextProvider);
        this.provideAmbientDisplayConfigurationProvider = DependencyProvider_ProvideAmbientDisplayConfigurationFactory.create(builder.dependencyProvider, this.contextProvider);
        this.provideAlwaysOnDisplayPolicyProvider = DependencyProvider_ProvideAlwaysOnDisplayPolicyFactory.create(builder.dependencyProvider, this.contextProvider);
        this.providePowerManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvidePowerManagerFactory.create(this.contextProvider));
        this.provideMainHandlerProvider = ConcurrencyModule_ProvideMainHandlerFactory.create(ConcurrencyModule_ProvideMainLooperFactory.create());
        Provider<LeakDetector> provider8 = DoubleCheck.provider(DependencyProvider_ProvideLeakDetectorFactory.create(builder.dependencyProvider));
        this.provideLeakDetectorProvider = provider8;
        Provider<TunerServiceImpl> provider9 = DoubleCheck.provider(TunerServiceImpl_Factory.create(this.contextProvider, this.provideMainHandlerProvider, provider8, this.providesBroadcastDispatcherProvider));
        this.tunerServiceImplProvider = provider9;
        this.dozeParametersProvider = DoubleCheck.provider(DozeParameters_Factory.create(this.provideResourcesProvider, this.provideAmbientDisplayConfigurationProvider, this.provideAlwaysOnDisplayPolicyProvider, this.providePowerManagerProvider, provider9));
        Provider<UiEventLogger> provider10 = DoubleCheck.provider(DependencyProvider_ProvideUiEventLoggerFactory.create());
        this.provideUiEventLoggerProvider = provider10;
        this.statusBarStateControllerImplProvider = DoubleCheck.provider(StatusBarStateControllerImpl_Factory.create(provider10));
        this.providePluginManagerProvider = DoubleCheck.provider(DependencyProvider_ProvidePluginManagerFactory.create(builder.dependencyProvider, this.contextProvider));
        this.provideMainExecutorProvider = ConcurrencyModule_ProvideMainExecutorFactory.create(this.contextProvider);
        this.provideDisplayMetricsProvider = DoubleCheck.provider(DependencyProvider_ProvideDisplayMetricsFactory.create(builder.dependencyProvider, this.contextProvider, this.provideWindowManagerProvider));
        Provider<AsyncSensorManager> provider11 = DoubleCheck.provider(AsyncSensorManager_Factory.create(this.contextProvider, this.providePluginManagerProvider));
        this.asyncSensorManagerProvider = provider11;
        this.proximitySensorProvider = ProximitySensor_Factory.create(this.provideResourcesProvider, provider11);
        this.dockManagerImplProvider = DoubleCheck.provider(DockManagerImpl_Factory.create());
        Provider<AudioManager> provider12 = DoubleCheck.provider(SystemServicesModule_ProvideAudioManagerFactory.create(this.contextProvider));
        this.provideAudioManagerProvider = provider12;
        this.ringerModeTrackerImplProvider = DoubleCheck.provider(RingerModeTrackerImpl_Factory.create(provider12, this.providesBroadcastDispatcherProvider, this.provideBackgroundExecutorProvider));
        this.provideLockPatternUtilsProvider = DependencyProvider_ProvideLockPatternUtilsFactory.create(builder.dependencyProvider, this.contextProvider);
        this.keyguardUpdateMonitorProvider = DoubleCheck.provider(KeyguardUpdateMonitor_Factory.create(this.contextProvider, ConcurrencyModule_ProvideMainLooperFactory.create(), this.providesBroadcastDispatcherProvider, this.dumpManagerProvider, this.ringerModeTrackerImplProvider, this.provideBackgroundExecutorProvider, this.statusBarStateControllerImplProvider, this.provideLockPatternUtilsProvider));
        this.provideUiBackgroundExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideUiBackgroundExecutorFactory.create());
        this.falsingManagerProxyProvider = DoubleCheck.provider(FalsingManagerProxy_Factory.create(this.contextProvider, this.providePluginManagerProvider, this.provideMainExecutorProvider, this.provideDisplayMetricsProvider, this.proximitySensorProvider, DeviceConfigProxy_Factory.create(), this.dockManagerImplProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.provideUiBackgroundExecutorProvider, this.statusBarStateControllerImplProvider));
        this.newKeyguardViewMediatorProvider = new DelegateFactory();
        this.providesViewMediatorCallbackProvider = DependencyProvider_ProvidesViewMediatorCallbackFactory.create(builder.dependencyProvider, this.newKeyguardViewMediatorProvider);
        Provider<DeviceProvisionedControllerImpl> provider13 = DoubleCheck.provider(DeviceProvisionedControllerImpl_Factory.create(this.contextProvider, this.provideMainHandlerProvider, this.providesBroadcastDispatcherProvider));
        this.deviceProvisionedControllerImplProvider = provider13;
        this.navigationModeControllerProvider = DoubleCheck.provider(NavigationModeController_Factory.create(this.contextProvider, provider13, this.provideConfigurationControllerProvider, this.provideUiBackgroundExecutorProvider));
        this.notificationShadeWindowControllerProvider = new DelegateFactory();
        this.keyguardStateControllerImplProvider = DoubleCheck.provider(KeyguardStateControllerImpl_Factory.create(this.contextProvider, this.keyguardUpdateMonitorProvider, this.provideLockPatternUtilsProvider));
        this.featureFlagsProvider = DoubleCheck.provider(FeatureFlags_Factory.create(this.provideBackgroundExecutorProvider));
        Provider<NotificationManager> provider14 = DoubleCheck.provider(SystemServicesModule_ProvideNotificationManagerFactory.create(this.contextProvider));
        this.provideNotificationManagerProvider = provider14;
        this.provideNotificationListenerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationListenerFactory.create(this.contextProvider, provider14, this.provideMainHandlerProvider));
        Provider<LogBuffer> provider15 = DoubleCheck.provider(LogModule_ProvideNotificationsLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideNotificationsLogBufferProvider = provider15;
        this.notificationEntryManagerLoggerProvider = NotificationEntryManagerLogger_Factory.create(provider15);
        Provider<ExtensionControllerImpl> provider16 = DoubleCheck.provider(ExtensionControllerImpl_Factory.create(this.contextProvider, this.provideLeakDetectorProvider, this.providePluginManagerProvider, this.tunerServiceImplProvider, this.provideConfigurationControllerProvider));
        this.extensionControllerImplProvider = provider16;
        this.notificationPersonExtractorPluginBoundaryProvider = DoubleCheck.provider(NotificationPersonExtractorPluginBoundary_Factory.create(provider16));
        DelegateFactory delegateFactory = new DelegateFactory();
        this.notificationGroupManagerProvider = delegateFactory;
        Provider<PeopleNotificationIdentifierImpl> provider17 = DoubleCheck.provider(PeopleNotificationIdentifierImpl_Factory.create(this.notificationPersonExtractorPluginBoundaryProvider, delegateFactory));
        this.peopleNotificationIdentifierImplProvider = provider17;
        Provider<NotificationGroupManager> provider18 = DoubleCheck.provider(NotificationGroupManager_Factory.create(this.statusBarStateControllerImplProvider, provider17));
        this.notificationGroupManagerProvider = provider18;
        ((DelegateFactory) this.notificationGroupManagerProvider).setDelegatedProvider(provider18);
        this.provideNotificationMediaManagerProvider = new DelegateFactory();
        this.provideDevicePolicyManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideDevicePolicyManagerFactory.create(this.contextProvider));
        this.provideUserManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideUserManagerFactory.create(this.contextProvider));
        Provider<IStatusBarService> provider19 = DoubleCheck.provider(SystemServicesModule_ProvideIStatusBarServiceFactory.create());
        this.provideIStatusBarServiceProvider = provider19;
        this.notificationClickNotifierProvider = DoubleCheck.provider(NotificationClickNotifier_Factory.create(provider19, this.provideMainExecutorProvider));
        Provider<KeyguardManager> provider20 = DoubleCheck.provider(SystemServicesModule_ProvideKeyguardManagerFactory.create(this.contextProvider));
        this.provideKeyguardManagerProvider = provider20;
        Provider<NotificationLockscreenUserManagerImpl> provider21 = DoubleCheck.provider(NotificationLockscreenUserManagerImpl_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.provideDevicePolicyManagerProvider, this.provideUserManagerProvider, this.notificationClickNotifierProvider, provider20, this.statusBarStateControllerImplProvider, this.provideMainHandlerProvider, this.deviceProvisionedControllerImplProvider, this.keyguardStateControllerImplProvider));
        this.notificationLockscreenUserManagerImplProvider = provider21;
        Provider<KeyguardBypassController> provider22 = DoubleCheck.provider(KeyguardBypassController_Factory.create(this.contextProvider, this.tunerServiceImplProvider, this.statusBarStateControllerImplProvider, provider21, this.keyguardStateControllerImplProvider, this.dumpManagerProvider));
        this.keyguardBypassControllerProvider = provider22;
        this.provideHeadsUpManagerPhoneProvider = DoubleCheck.provider(SystemUIDefaultModule_ProvideHeadsUpManagerPhoneFactory.create(this.contextProvider, this.statusBarStateControllerImplProvider, provider22, this.notificationGroupManagerProvider, this.provideConfigurationControllerProvider));
        MediaFeatureFlag_Factory create = MediaFeatureFlag_Factory.create(this.contextProvider);
        this.mediaFeatureFlagProvider = create;
        this.notificationFilterProvider = DoubleCheck.provider(NotificationFilter_Factory.create(this.statusBarStateControllerImplProvider, create));
        this.notificationSectionsFeatureManagerProvider = NotificationSectionsFeatureManager_Factory.create(DeviceConfigProxy_Factory.create(), this.contextProvider);
        Provider<HighPriorityProvider> provider23 = DoubleCheck.provider(HighPriorityProvider_Factory.create(this.peopleNotificationIdentifierImplProvider, this.notificationGroupManagerProvider));
        this.highPriorityProvider = provider23;
        this.notificationRankingManagerProvider = NotificationRankingManager_Factory.create(this.provideNotificationMediaManagerProvider, this.notificationGroupManagerProvider, this.provideHeadsUpManagerPhoneProvider, this.notificationFilterProvider, this.notificationEntryManagerLoggerProvider, this.notificationSectionsFeatureManagerProvider, this.peopleNotificationIdentifierImplProvider, provider23);
        this.keyguardEnvironmentImplProvider = DoubleCheck.provider(KeyguardEnvironmentImpl_Factory.create());
        this.provideNotificationMessagingUtilProvider = DependencyProvider_ProvideNotificationMessagingUtilFactory.create(builder.dependencyProvider, this.contextProvider);
        DelegateFactory delegateFactory2 = new DelegateFactory();
        this.provideNotificationEntryManagerProvider = delegateFactory2;
        this.provideSmartReplyControllerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideSmartReplyControllerFactory.create(delegateFactory2, this.provideIStatusBarServiceProvider, this.notificationClickNotifierProvider));
        this.provideStatusBarProvider = new DelegateFactory();
        this.remoteInputUriControllerProvider = DoubleCheck.provider(RemoteInputUriController_Factory.create(this.provideIStatusBarServiceProvider));
        Provider<LogBuffer> provider24 = DoubleCheck.provider(LogModule_ProvideNotifInteractionLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideNotifInteractionLogBufferProvider = provider24;
        this.actionClickLoggerProvider = ActionClickLogger_Factory.create(provider24);
        this.provideNotificationRemoteInputManagerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationRemoteInputManagerFactory.create(this.contextProvider, this.notificationLockscreenUserManagerImplProvider, this.provideSmartReplyControllerProvider, this.provideNotificationEntryManagerProvider, this.provideStatusBarProvider, this.statusBarStateControllerImplProvider, ConcurrencyModule_ProvideHandlerFactory.create(), this.remoteInputUriControllerProvider, this.notificationClickNotifierProvider, this.actionClickLoggerProvider));
        this.bindSystemClockProvider = DoubleCheck.provider(SystemClockImpl_Factory.create());
        this.notifCollectionLoggerProvider = NotifCollectionLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        Provider<Files> provider25 = DoubleCheck.provider(Files_Factory.create());
        this.filesProvider = provider25;
        Provider<LogBufferEulogizer> provider26 = DoubleCheck.provider(LogBufferEulogizer_Factory.create(this.contextProvider, this.dumpManagerProvider, this.bindSystemClockProvider, provider25));
        this.logBufferEulogizerProvider = provider26;
        this.notifCollectionProvider = DoubleCheck.provider(NotifCollection_Factory.create(this.provideIStatusBarServiceProvider, this.bindSystemClockProvider, this.featureFlagsProvider, this.notifCollectionLoggerProvider, provider26, this.dumpManagerProvider));
        this.shadeListBuilderLoggerProvider = ShadeListBuilderLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        Provider<NotificationInteractionTracker> provider27 = DoubleCheck.provider(NotificationInteractionTracker_Factory.create(this.notificationClickNotifierProvider, this.provideNotificationEntryManagerProvider));
        this.notificationInteractionTrackerProvider = provider27;
        Provider<ShadeListBuilder> provider28 = DoubleCheck.provider(ShadeListBuilder_Factory.create(this.bindSystemClockProvider, this.shadeListBuilderLoggerProvider, this.dumpManagerProvider, provider27));
        this.shadeListBuilderProvider = provider28;
        Provider<NotifPipeline> provider29 = DoubleCheck.provider(NotifPipeline_Factory.create(this.notifCollectionProvider, provider28));
        this.notifPipelineProvider = provider29;
        this.provideCommonNotifCollectionProvider = DoubleCheck.provider(NotificationsModule_ProvideCommonNotifCollectionFactory.create(this.featureFlagsProvider, provider29, this.provideNotificationEntryManagerProvider));
        NotifBindPipelineLogger_Factory create2 = NotifBindPipelineLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.notifBindPipelineLoggerProvider = create2;
        this.notifBindPipelineProvider = DoubleCheck.provider(NotifBindPipeline_Factory.create(this.provideCommonNotifCollectionProvider, create2, ConcurrencyModule_ProvideMainLooperFactory.create()));
        NotifRemoteViewCacheImpl_Factory create3 = NotifRemoteViewCacheImpl_Factory.create(this.provideCommonNotifCollectionProvider);
        this.notifRemoteViewCacheImplProvider = create3;
        this.provideNotifRemoteViewCacheProvider = DoubleCheck.provider(create3);
        this.smartReplyConstantsProvider = DoubleCheck.provider(SmartReplyConstants_Factory.create(this.provideMainHandlerProvider, this.contextProvider, DeviceConfigProxy_Factory.create()));
        this.provideLauncherAppsProvider = DoubleCheck.provider(SystemServicesModule_ProvideLauncherAppsFactory.create(this.contextProvider));
        Provider<ConversationNotificationManager> provider30 = DoubleCheck.provider(ConversationNotificationManager_Factory.create(this.provideNotificationEntryManagerProvider, this.notificationGroupManagerProvider, this.contextProvider, this.provideMainHandlerProvider));
        this.conversationNotificationManagerProvider = provider30;
        ConversationNotificationProcessor_Factory create4 = ConversationNotificationProcessor_Factory.create(this.provideLauncherAppsProvider, provider30);
        this.conversationNotificationProcessorProvider = create4;
        this.notificationContentInflaterProvider = DoubleCheck.provider(NotificationContentInflater_Factory.create(this.provideNotifRemoteViewCacheProvider, this.provideNotificationRemoteInputManagerProvider, this.smartReplyConstantsProvider, this.provideSmartReplyControllerProvider, create4, this.provideBackgroundExecutorProvider));
        this.notifInflationErrorManagerProvider = DoubleCheck.provider(NotifInflationErrorManager_Factory.create());
        this.rowContentBindStageLoggerProvider = RowContentBindStageLogger_Factory.create(this.provideNotificationsLogBufferProvider);
    }

    private void initialize2(Builder builder) {
        this.rowContentBindStageProvider = DoubleCheck.provider(RowContentBindStage_Factory.create(this.notificationContentInflaterProvider, this.notifInflationErrorManagerProvider, this.rowContentBindStageLoggerProvider));
        this.provideIDreamManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideIDreamManagerFactory.create());
        this.enhancedEstimatesImplProvider = DoubleCheck.provider(EnhancedEstimatesImpl_Factory.create());
        ConcurrencyModule_ProvideBgHandlerFactory create = ConcurrencyModule_ProvideBgHandlerFactory.create(this.provideBgLooperProvider);
        this.provideBgHandlerProvider = create;
        Provider<BatteryController> provider = DoubleCheck.provider(SystemUIDefaultModule_ProvideBatteryControllerFactory.create(this.contextProvider, this.enhancedEstimatesImplProvider, this.providePowerManagerProvider, this.providesBroadcastDispatcherProvider, this.provideMainHandlerProvider, create));
        this.provideBatteryControllerProvider = provider;
        this.notificationInterruptStateProviderImplProvider = DoubleCheck.provider(NotificationInterruptStateProviderImpl_Factory.create(this.provideContentResolverProvider, this.providePowerManagerProvider, this.provideIDreamManagerProvider, this.provideAmbientDisplayConfigurationProvider, this.notificationFilterProvider, provider, this.statusBarStateControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.provideMainHandlerProvider));
        this.expandableNotificationRowComponentBuilderProvider = new Provider<ExpandableNotificationRowComponent.Builder>() { // from class: com.android.systemui.tv.DaggerTvSystemUIRootComponent.1
            @Override // javax.inject.Provider
            public ExpandableNotificationRowComponent.Builder get() {
                return new ExpandableNotificationRowComponentBuilder();
            }
        };
        IconBuilder_Factory create2 = IconBuilder_Factory.create(this.contextProvider);
        this.iconBuilderProvider = create2;
        this.iconManagerProvider = IconManager_Factory.create(this.provideCommonNotifCollectionProvider, this.provideLauncherAppsProvider, create2);
        this.lowPriorityInflationHelperProvider = DoubleCheck.provider(LowPriorityInflationHelper_Factory.create(this.featureFlagsProvider, this.notificationGroupManagerProvider, this.rowContentBindStageProvider));
        this.notificationRowBinderImplProvider = DoubleCheck.provider(NotificationRowBinderImpl_Factory.create(this.contextProvider, this.provideNotificationMessagingUtilProvider, this.provideNotificationRemoteInputManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.notifBindPipelineProvider, this.rowContentBindStageProvider, this.notificationInterruptStateProviderImplProvider, RowInflaterTask_Factory.create(), this.expandableNotificationRowComponentBuilderProvider, this.iconManagerProvider, this.lowPriorityInflationHelperProvider));
        Provider<ForegroundServiceDismissalFeatureController> provider2 = DoubleCheck.provider(ForegroundServiceDismissalFeatureController_Factory.create(DeviceConfigProxy_Factory.create(), this.contextProvider));
        this.foregroundServiceDismissalFeatureControllerProvider = provider2;
        Provider<NotificationEntryManager> provider3 = DoubleCheck.provider(NotificationsModule_ProvideNotificationEntryManagerFactory.create(this.notificationEntryManagerLoggerProvider, this.notificationGroupManagerProvider, this.notificationRankingManagerProvider, this.keyguardEnvironmentImplProvider, this.featureFlagsProvider, this.notificationRowBinderImplProvider, this.provideNotificationRemoteInputManagerProvider, this.provideLeakDetectorProvider, provider2));
        this.provideNotificationEntryManagerProvider = provider3;
        ((DelegateFactory) this.provideNotificationEntryManagerProvider).setDelegatedProvider(provider3);
        this.targetSdkResolverProvider = DoubleCheck.provider(TargetSdkResolver_Factory.create(this.contextProvider));
        this.provideMainDelayableExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideMainDelayableExecutorFactory.create(ConcurrencyModule_ProvideMainLooperFactory.create()));
        GroupCoalescerLogger_Factory create3 = GroupCoalescerLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.groupCoalescerLoggerProvider = create3;
        this.groupCoalescerProvider = GroupCoalescer_Factory.create(this.provideMainDelayableExecutorProvider, this.bindSystemClockProvider, create3);
        SharedCoordinatorLogger_Factory create4 = SharedCoordinatorLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.sharedCoordinatorLoggerProvider = create4;
        this.hideNotifsForOtherUsersCoordinatorProvider = HideNotifsForOtherUsersCoordinator_Factory.create(this.notificationLockscreenUserManagerImplProvider, create4);
        this.keyguardCoordinatorProvider = DoubleCheck.provider(KeyguardCoordinator_Factory.create(this.contextProvider, ConcurrencyModule_ProvideHandlerFactory.create(), this.keyguardStateControllerImplProvider, this.notificationLockscreenUserManagerImplProvider, this.providesBroadcastDispatcherProvider, this.statusBarStateControllerImplProvider, this.keyguardUpdateMonitorProvider, this.highPriorityProvider));
        this.rankingCoordinatorProvider = DoubleCheck.provider(RankingCoordinator_Factory.create(this.statusBarStateControllerImplProvider));
        Provider<AppOpsControllerImpl> provider4 = DoubleCheck.provider(AppOpsControllerImpl_Factory.create(this.contextProvider, this.provideBgLooperProvider, this.dumpManagerProvider));
        this.appOpsControllerImplProvider = provider4;
        Provider<ForegroundServiceController> provider5 = DoubleCheck.provider(ForegroundServiceController_Factory.create(this.provideNotificationEntryManagerProvider, provider4, this.provideMainHandlerProvider));
        this.foregroundServiceControllerProvider = provider5;
        this.appOpsCoordinatorProvider = DoubleCheck.provider(AppOpsCoordinator_Factory.create(provider5, this.appOpsControllerImplProvider, this.provideMainDelayableExecutorProvider));
        Provider<IPackageManager> provider6 = DoubleCheck.provider(SystemServicesModule_ProvideIPackageManagerFactory.create());
        this.provideIPackageManagerProvider = provider6;
        this.deviceProvisionedCoordinatorProvider = DoubleCheck.provider(DeviceProvisionedCoordinator_Factory.create(this.deviceProvisionedControllerImplProvider, provider6));
        DelegateFactory delegateFactory = new DelegateFactory();
        this.newBubbleControllerProvider = delegateFactory;
        this.bubbleCoordinatorProvider = DoubleCheck.provider(BubbleCoordinator_Factory.create(delegateFactory, this.notifCollectionProvider));
        Provider<HeadsUpViewBinder> provider7 = DoubleCheck.provider(HeadsUpViewBinder_Factory.create(this.provideNotificationMessagingUtilProvider, this.rowContentBindStageProvider));
        this.headsUpViewBinderProvider = provider7;
        this.headsUpCoordinatorProvider = DoubleCheck.provider(HeadsUpCoordinator_Factory.create(this.provideHeadsUpManagerPhoneProvider, provider7, this.notificationInterruptStateProviderImplProvider, this.provideNotificationRemoteInputManagerProvider));
        this.conversationCoordinatorProvider = DoubleCheck.provider(ConversationCoordinator_Factory.create());
        this.preparationCoordinatorLoggerProvider = PreparationCoordinatorLogger_Factory.create(this.provideNotificationsLogBufferProvider);
        this.notifInflaterImplProvider = DoubleCheck.provider(NotifInflaterImpl_Factory.create(this.provideIStatusBarServiceProvider, this.notifCollectionProvider, this.notifInflationErrorManagerProvider, this.notifPipelineProvider));
        Provider<NotifViewBarn> provider8 = DoubleCheck.provider(NotifViewBarn_Factory.create());
        this.notifViewBarnProvider = provider8;
        this.preparationCoordinatorProvider = DoubleCheck.provider(PreparationCoordinator_Factory.create(this.preparationCoordinatorLoggerProvider, this.notifInflaterImplProvider, this.notifInflationErrorManagerProvider, provider8, this.provideIStatusBarServiceProvider));
        MediaCoordinator_Factory create5 = MediaCoordinator_Factory.create(this.mediaFeatureFlagProvider);
        this.mediaCoordinatorProvider = create5;
        this.notifCoordinatorsProvider = DoubleCheck.provider(NotifCoordinators_Factory.create(this.dumpManagerProvider, this.featureFlagsProvider, this.hideNotifsForOtherUsersCoordinatorProvider, this.keyguardCoordinatorProvider, this.rankingCoordinatorProvider, this.appOpsCoordinatorProvider, this.deviceProvisionedCoordinatorProvider, this.bubbleCoordinatorProvider, this.headsUpCoordinatorProvider, this.conversationCoordinatorProvider, this.preparationCoordinatorProvider, create5));
        Provider<VisualStabilityManager> provider9 = DoubleCheck.provider(NotificationsModule_ProvideVisualStabilityManagerFactory.create(this.provideNotificationEntryManagerProvider, ConcurrencyModule_ProvideHandlerFactory.create()));
        this.provideVisualStabilityManagerProvider = provider9;
        Provider<NotifViewManager> provider10 = DoubleCheck.provider(NotifViewManager_Factory.create(this.notifViewBarnProvider, provider9, this.featureFlagsProvider));
        this.notifViewManagerProvider = provider10;
        this.notifPipelineInitializerProvider = DoubleCheck.provider(NotifPipelineInitializer_Factory.create(this.notifPipelineProvider, this.groupCoalescerProvider, this.notifCollectionProvider, this.shadeListBuilderProvider, this.notifCoordinatorsProvider, this.notifInflaterImplProvider, this.dumpManagerProvider, this.featureFlagsProvider, provider10));
        this.notifBindPipelineInitializerProvider = NotifBindPipelineInitializer_Factory.create(this.notifBindPipelineProvider, this.rowContentBindStageProvider);
        this.provideNotificationGroupAlertTransferHelperProvider = DoubleCheck.provider(StatusBarPhoneDependenciesModule_ProvideNotificationGroupAlertTransferHelperFactory.create(this.rowContentBindStageProvider));
        this.headsUpControllerProvider = DoubleCheck.provider(HeadsUpController_Factory.create(this.headsUpViewBinderProvider, this.notificationInterruptStateProviderImplProvider, this.provideHeadsUpManagerPhoneProvider, this.provideNotificationRemoteInputManagerProvider, this.statusBarStateControllerImplProvider, this.provideVisualStabilityManagerProvider, this.provideNotificationListenerProvider));
        NotificationClickerLogger_Factory create6 = NotificationClickerLogger_Factory.create(this.provideNotifInteractionLogBufferProvider);
        this.notificationClickerLoggerProvider = create6;
        NotificationClicker_Builder_Factory create7 = NotificationClicker_Builder_Factory.create(this.newBubbleControllerProvider, create6);
        this.builderProvider = create7;
        this.notificationsControllerImplProvider = DoubleCheck.provider(NotificationsControllerImpl_Factory.create(this.featureFlagsProvider, this.provideNotificationListenerProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, this.targetSdkResolverProvider, this.notifPipelineInitializerProvider, this.notifBindPipelineInitializerProvider, this.deviceProvisionedControllerImplProvider, this.notificationRowBinderImplProvider, this.remoteInputUriControllerProvider, this.notificationGroupManagerProvider, this.provideNotificationGroupAlertTransferHelperProvider, this.provideHeadsUpManagerPhoneProvider, this.headsUpControllerProvider, this.headsUpViewBinderProvider, create7));
        NotificationsControllerStub_Factory create8 = NotificationsControllerStub_Factory.create(this.provideNotificationListenerProvider);
        this.notificationsControllerStubProvider = create8;
        this.provideNotificationsControllerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationsControllerFactory.create(this.contextProvider, this.notificationsControllerImplProvider, create8));
        Provider<ProtoTracer> provider11 = DoubleCheck.provider(ProtoTracer_Factory.create(this.contextProvider, this.dumpManagerProvider));
        this.protoTracerProvider = provider11;
        Provider<CommandQueue> provider12 = DoubleCheck.provider(StatusBarDependenciesModule_ProvideCommandQueueFactory.create(this.contextProvider, provider11));
        this.provideCommandQueueProvider = provider12;
        Provider<DarkIconDispatcherImpl> provider13 = DoubleCheck.provider(DarkIconDispatcherImpl_Factory.create(this.contextProvider, provider12));
        this.darkIconDispatcherImplProvider = provider13;
        this.lightBarControllerProvider = DoubleCheck.provider(LightBarController_Factory.create(this.contextProvider, provider13, this.provideBatteryControllerProvider, this.navigationModeControllerProvider));
        this.provideIWindowManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideIWindowManagerFactory.create());
        this.provideAutoHideControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideAutoHideControllerFactory.create(builder.dependencyProvider, this.contextProvider, this.provideMainHandlerProvider, this.provideIWindowManagerProvider));
        this.statusBarIconControllerImplProvider = DoubleCheck.provider(StatusBarIconControllerImpl_Factory.create(this.contextProvider, this.provideCommandQueueProvider));
        this.notificationWakeUpCoordinatorProvider = DoubleCheck.provider(NotificationWakeUpCoordinator_Factory.create(this.provideHeadsUpManagerPhoneProvider, this.statusBarStateControllerImplProvider, this.keyguardBypassControllerProvider, this.dozeParametersProvider));
        Provider<NotificationRoundnessManager> provider14 = DoubleCheck.provider(NotificationRoundnessManager_Factory.create(this.keyguardBypassControllerProvider, this.notificationSectionsFeatureManagerProvider));
        this.notificationRoundnessManagerProvider = provider14;
        this.pulseExpansionHandlerProvider = DoubleCheck.provider(PulseExpansionHandler_Factory.create(this.contextProvider, this.notificationWakeUpCoordinatorProvider, this.keyguardBypassControllerProvider, this.provideHeadsUpManagerPhoneProvider, provider14, this.statusBarStateControllerImplProvider, this.falsingManagerProxyProvider));
        this.dynamicPrivacyControllerProvider = DoubleCheck.provider(DynamicPrivacyController_Factory.create(this.notificationLockscreenUserManagerImplProvider, this.keyguardStateControllerImplProvider, this.statusBarStateControllerImplProvider));
        this.bypassHeadsUpNotifierProvider = DoubleCheck.provider(BypassHeadsUpNotifier_Factory.create(this.contextProvider, this.keyguardBypassControllerProvider, this.statusBarStateControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.notificationLockscreenUserManagerImplProvider, this.provideNotificationMediaManagerProvider, this.provideNotificationEntryManagerProvider, this.tunerServiceImplProvider));
        this.remoteInputQuickSettingsDisablerProvider = DoubleCheck.provider(RemoteInputQuickSettingsDisabler_Factory.create(this.contextProvider, this.provideConfigurationControllerProvider, this.provideCommandQueueProvider));
        this.provideAccessibilityManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideAccessibilityManagerFactory.create(this.contextProvider));
        this.provideINotificationManagerProvider = DoubleCheck.provider(DependencyProvider_ProvideINotificationManagerFactory.create(builder.dependencyProvider));
        this.provideShortcutManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideShortcutManagerFactory.create(this.contextProvider));
        Provider<ChannelEditorDialogController> provider15 = DoubleCheck.provider(ChannelEditorDialogController_Factory.create(this.contextProvider, this.provideINotificationManagerProvider, ChannelEditorDialog_Builder_Factory.create()));
        this.channelEditorDialogControllerProvider = provider15;
        this.provideNotificationGutsManagerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationGutsManagerFactory.create(this.contextProvider, this.provideVisualStabilityManagerProvider, this.provideStatusBarProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider, this.provideAccessibilityManagerProvider, this.highPriorityProvider, this.provideINotificationManagerProvider, this.provideLauncherAppsProvider, this.provideShortcutManagerProvider, provider15, this.provideCurrentUserContextTrackerProvider, PriorityOnboardingDialogController_Builder_Factory.create(), this.newBubbleControllerProvider, this.provideUiEventLoggerProvider));
        this.expansionStateLoggerProvider = NotificationLogger_ExpansionStateLogger_Factory.create(this.provideUiBackgroundExecutorProvider);
        Provider<NotificationPanelLogger> provider16 = DoubleCheck.provider(NotificationsModule_ProvideNotificationPanelLoggerFactory.create());
        this.provideNotificationPanelLoggerProvider = provider16;
        this.provideNotificationLoggerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationLoggerFactory.create(this.provideNotificationListenerProvider, this.provideUiBackgroundExecutorProvider, this.provideNotificationEntryManagerProvider, this.statusBarStateControllerImplProvider, this.expansionStateLoggerProvider, provider16));
        this.foregroundServiceSectionControllerProvider = DoubleCheck.provider(ForegroundServiceSectionController_Factory.create(this.provideNotificationEntryManagerProvider, this.foregroundServiceDismissalFeatureControllerProvider));
        DynamicChildBindController_Factory create9 = DynamicChildBindController_Factory.create(this.rowContentBindStageProvider);
        this.dynamicChildBindControllerProvider = create9;
        this.provideNotificationViewHierarchyManagerProvider = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationViewHierarchyManagerFactory.create(this.contextProvider, this.provideMainHandlerProvider, this.notificationLockscreenUserManagerImplProvider, this.notificationGroupManagerProvider, this.provideVisualStabilityManagerProvider, this.statusBarStateControllerImplProvider, this.provideNotificationEntryManagerProvider, this.keyguardBypassControllerProvider, this.newBubbleControllerProvider, this.dynamicPrivacyControllerProvider, this.foregroundServiceSectionControllerProvider, create9, this.lowPriorityInflationHelperProvider));
        this.provideMetricsLoggerProvider = DoubleCheck.provider(DependencyProvider_ProvideMetricsLoggerFactory.create(builder.dependencyProvider));
        Provider<Optional<Lazy<StatusBar>>> of = PresentJdkOptionalLazyProvider.of(this.provideStatusBarProvider);
        this.optionalOfLazyOfStatusBarProvider = of;
        Provider<ActivityStarterDelegate> provider17 = DoubleCheck.provider(ActivityStarterDelegate_Factory.create(of));
        this.activityStarterDelegateProvider = provider17;
        this.userSwitcherControllerProvider = DoubleCheck.provider(UserSwitcherController_Factory.create(this.contextProvider, this.keyguardStateControllerImplProvider, this.provideMainHandlerProvider, provider17, this.providesBroadcastDispatcherProvider, this.provideUiEventLoggerProvider));
        this.provideConnectivityManagagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideConnectivityManagagerFactory.create(this.contextProvider));
        this.provideTelephonyManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideTelephonyManagerFactory.create(this.contextProvider));
        this.provideWifiManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideWifiManagerFactory.create(this.contextProvider));
        Provider<NetworkScoreManager> provider18 = DoubleCheck.provider(SystemServicesModule_ProvideNetworkScoreManagerFactory.create(this.contextProvider));
        this.provideNetworkScoreManagerProvider = provider18;
        this.networkControllerImplProvider = DoubleCheck.provider(NetworkControllerImpl_Factory.create(this.contextProvider, this.provideBgLooperProvider, this.deviceProvisionedControllerImplProvider, this.providesBroadcastDispatcherProvider, this.provideConnectivityManagagerProvider, this.provideTelephonyManagerProvider, this.provideWifiManagerProvider, provider18));
        this.sysuiColorExtractorProvider = DoubleCheck.provider(SysuiColorExtractor_Factory.create(this.contextProvider, this.provideConfigurationControllerProvider));
        this.screenLifecycleProvider = DoubleCheck.provider(ScreenLifecycle_Factory.create());
        this.wakefulnessLifecycleProvider = DoubleCheck.provider(WakefulnessLifecycle_Factory.create());
        this.vibratorHelperProvider = DoubleCheck.provider(VibratorHelper_Factory.create(this.contextProvider));
        this.provideNavigationBarControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideNavigationBarControllerFactory.create(builder.dependencyProvider, this.contextProvider, this.provideMainHandlerProvider, this.provideCommandQueueProvider));
        this.provideAssistUtilsProvider = DoubleCheck.provider(AssistModule_ProvideAssistUtilsFactory.create(this.contextProvider));
        this.provideBackgroundHandlerProvider = DoubleCheck.provider(AssistModule_ProvideBackgroundHandlerFactory.create());
        this.provideAssistHandleViewControllerProvider = AssistModule_ProvideAssistHandleViewControllerFactory.create(this.provideNavigationBarControllerProvider);
        this.deviceConfigHelperProvider = DoubleCheck.provider(DeviceConfigHelper_Factory.create());
        this.assistHandleOffBehaviorProvider = DoubleCheck.provider(AssistHandleOffBehavior_Factory.create());
        Provider<SysUiState> provider19 = DoubleCheck.provider(SystemUIModule_ProvideSysUiStateFactory.create());
        this.provideSysUiStateProvider = provider19;
        this.assistHandleLikeHomeBehaviorProvider = DoubleCheck.provider(AssistHandleLikeHomeBehavior_Factory.create(this.statusBarStateControllerImplProvider, this.wakefulnessLifecycleProvider, provider19));
        this.provideSystemClockProvider = DoubleCheck.provider(AssistModule_ProvideSystemClockFactory.create());
        this.provideActivityManagerWrapperProvider = DoubleCheck.provider(DependencyProvider_ProvideActivityManagerWrapperFactory.create(builder.dependencyProvider));
        this.pipSnapAlgorithmProvider = PipSnapAlgorithm_Factory.create(this.contextProvider);
        Provider<DisplayController> provider20 = DoubleCheck.provider(DisplayController_Factory.create(this.contextProvider, this.provideMainHandlerProvider, this.provideIWindowManagerProvider));
        this.displayControllerProvider = provider20;
        this.pipBoundsHandlerProvider = DoubleCheck.provider(PipBoundsHandler_Factory.create(this.contextProvider, this.pipSnapAlgorithmProvider, provider20));
        this.pipSurfaceTransactionHelperProvider = DoubleCheck.provider(PipSurfaceTransactionHelper_Factory.create(this.contextProvider, this.provideConfigurationControllerProvider));
        this.contextComponentResolverProvider = new DelegateFactory();
    }

    private void initialize3(Builder builder) {
        RecentsModule_ProvideRecentsImplFactory create = RecentsModule_ProvideRecentsImplFactory.create(this.contextProvider, this.contextComponentResolverProvider);
        this.provideRecentsImplProvider = create;
        Provider<Recents> provider = DoubleCheck.provider(SystemUIDefaultModule_ProvideRecentsFactory.create(this.contextProvider, create, this.provideCommandQueueProvider));
        this.provideRecentsProvider = provider;
        this.optionalOfLazyOfRecentsProvider = PresentJdkOptionalLazyProvider.of(provider);
        this.systemWindowsProvider = DoubleCheck.provider(SystemWindows_Factory.create(this.contextProvider, this.displayControllerProvider, this.provideIWindowManagerProvider));
        Provider<TransactionPool> provider2 = DoubleCheck.provider(TransactionPool_Factory.create());
        this.transactionPoolProvider = provider2;
        Provider<DisplayImeController> provider3 = DoubleCheck.provider(DisplayImeController_Factory.create(this.systemWindowsProvider, this.displayControllerProvider, this.provideMainHandlerProvider, provider2));
        this.displayImeControllerProvider = provider3;
        this.provideDividerProvider = DoubleCheck.provider(DividerModule_ProvideDividerFactory.create(this.contextProvider, this.optionalOfLazyOfRecentsProvider, this.displayControllerProvider, this.systemWindowsProvider, provider3, this.provideMainHandlerProvider, this.keyguardStateControllerImplProvider, this.transactionPoolProvider));
        PipAnimationController_Factory create2 = PipAnimationController_Factory.create(this.contextProvider, this.pipSurfaceTransactionHelperProvider);
        this.pipAnimationControllerProvider = create2;
        Provider<PipTaskOrganizer> provider4 = DoubleCheck.provider(PipTaskOrganizer_Factory.create(this.contextProvider, this.pipBoundsHandlerProvider, this.pipSurfaceTransactionHelperProvider, this.provideDividerProvider, this.displayControllerProvider, create2));
        this.pipTaskOrganizerProvider = provider4;
        Provider<PipManager> provider5 = DoubleCheck.provider(PipManager_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.pipBoundsHandlerProvider, provider4, this.pipSurfaceTransactionHelperProvider, this.provideDividerProvider));
        this.pipManagerProvider = provider5;
        this.pipUIProvider = DoubleCheck.provider(PipUI_Factory.create(this.contextProvider, this.provideCommandQueueProvider, provider5));
        Provider<Optional<Divider>> of = PresentJdkOptionalInstanceProvider.of(this.provideDividerProvider);
        this.optionalOfDividerProvider = of;
        this.overviewProxyServiceProvider = DoubleCheck.provider(OverviewProxyService_Factory.create(this.contextProvider, this.provideCommandQueueProvider, this.provideNavigationBarControllerProvider, this.navigationModeControllerProvider, this.notificationShadeWindowControllerProvider, this.provideSysUiStateProvider, this.pipUIProvider, of, this.optionalOfLazyOfStatusBarProvider, this.providesBroadcastDispatcherProvider));
        Provider<PackageManagerWrapper> provider6 = DoubleCheck.provider(SystemServicesModule_ProvidePackageManagerWrapperFactory.create());
        this.providePackageManagerWrapperProvider = provider6;
        Provider provider7 = DoubleCheck.provider(AssistHandleReminderExpBehavior_Factory.create(this.provideSystemClockProvider, this.provideBackgroundHandlerProvider, this.deviceConfigHelperProvider, this.statusBarStateControllerImplProvider, this.provideActivityManagerWrapperProvider, this.overviewProxyServiceProvider, this.provideSysUiStateProvider, this.wakefulnessLifecycleProvider, provider6, this.providesBroadcastDispatcherProvider, this.bootCompleteCacheImplProvider));
        this.assistHandleReminderExpBehaviorProvider = provider7;
        Provider provider8 = DoubleCheck.provider(AssistModule_ProvideAssistHandleBehaviorControllerMapFactory.create(this.assistHandleOffBehaviorProvider, this.assistHandleLikeHomeBehaviorProvider, provider7));
        this.provideAssistHandleBehaviorControllerMapProvider = provider8;
        this.assistHandleBehaviorControllerProvider = DoubleCheck.provider(AssistHandleBehaviorController_Factory.create(this.contextProvider, this.provideAssistUtilsProvider, this.provideBackgroundHandlerProvider, this.provideAssistHandleViewControllerProvider, this.deviceConfigHelperProvider, provider8, this.navigationModeControllerProvider, this.provideAccessibilityManagerProvider, this.dumpManagerProvider));
        Provider<PhoneStateMonitor> provider9 = DoubleCheck.provider(PhoneStateMonitor_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.optionalOfLazyOfStatusBarProvider, this.bootCompleteCacheImplProvider));
        this.phoneStateMonitorProvider = provider9;
        Provider<AssistLogger> provider10 = DoubleCheck.provider(AssistLogger_Factory.create(this.contextProvider, this.provideUiEventLoggerProvider, this.provideAssistUtilsProvider, provider9, this.assistHandleBehaviorControllerProvider));
        this.assistLoggerProvider = provider10;
        Provider<DefaultUiController> provider11 = DoubleCheck.provider(DefaultUiController_Factory.create(this.contextProvider, provider10));
        this.defaultUiControllerProvider = provider11;
        this.assistManagerProvider = DoubleCheck.provider(AssistManager_Factory.create(this.deviceProvisionedControllerImplProvider, this.contextProvider, this.provideAssistUtilsProvider, this.assistHandleBehaviorControllerProvider, this.provideCommandQueueProvider, this.phoneStateMonitorProvider, this.overviewProxyServiceProvider, this.provideConfigurationControllerProvider, this.provideSysUiStateProvider, provider11, this.assistLoggerProvider));
        this.lockscreenGestureLoggerProvider = DoubleCheck.provider(LockscreenGestureLogger_Factory.create());
        DelegateFactory delegateFactory = new DelegateFactory();
        this.statusBarKeyguardViewManagerProvider = delegateFactory;
        this.shadeControllerImplProvider = DoubleCheck.provider(ShadeControllerImpl_Factory.create(this.provideCommandQueueProvider, this.statusBarStateControllerImplProvider, this.notificationShadeWindowControllerProvider, delegateFactory, this.provideWindowManagerProvider, this.provideStatusBarProvider, this.assistManagerProvider, this.newBubbleControllerProvider));
        this.accessibilityControllerProvider = DoubleCheck.provider(AccessibilityController_Factory.create(this.contextProvider));
        this.builderProvider2 = WakeLock_Builder_Factory.create(this.contextProvider);
        Provider<IBatteryStats> provider12 = DoubleCheck.provider(SystemServicesModule_ProvideIBatteryStatsFactory.create());
        this.provideIBatteryStatsProvider = provider12;
        Provider<KeyguardIndicationController> provider13 = DoubleCheck.provider(KeyguardIndicationController_Factory.create(this.contextProvider, this.builderProvider2, this.keyguardStateControllerImplProvider, this.statusBarStateControllerImplProvider, this.keyguardUpdateMonitorProvider, this.dockManagerImplProvider, this.providesBroadcastDispatcherProvider, this.provideDevicePolicyManagerProvider, provider12, this.provideUserManagerProvider));
        this.keyguardIndicationControllerProvider = provider13;
        this.lockscreenLockIconControllerProvider = DoubleCheck.provider(LockscreenLockIconController_Factory.create(this.lockscreenGestureLoggerProvider, this.keyguardUpdateMonitorProvider, this.provideLockPatternUtilsProvider, this.shadeControllerImplProvider, this.accessibilityControllerProvider, provider13, this.statusBarStateControllerImplProvider, this.provideConfigurationControllerProvider, this.notificationWakeUpCoordinatorProvider, this.keyguardBypassControllerProvider, this.dockManagerImplProvider, this.keyguardStateControllerImplProvider, this.provideResourcesProvider, this.provideHeadsUpManagerPhoneProvider));
        this.provideAlarmManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideAlarmManagerFactory.create(this.contextProvider));
        this.builderProvider3 = DelayedWakeLock_Builder_Factory.create(this.contextProvider);
        this.blurUtilsProvider = DoubleCheck.provider(BlurUtils_Factory.create(this.provideResourcesProvider, this.dumpManagerProvider));
        this.scrimControllerProvider = DoubleCheck.provider(ScrimController_Factory.create(this.lightBarControllerProvider, this.dozeParametersProvider, this.provideAlarmManagerProvider, this.keyguardStateControllerImplProvider, this.builderProvider3, ConcurrencyModule_ProvideHandlerFactory.create(), this.keyguardUpdateMonitorProvider, this.sysuiColorExtractorProvider, this.dockManagerImplProvider, this.blurUtilsProvider));
        this.provideKeyguardLiftControllerProvider = DoubleCheck.provider(SystemUIModule_ProvideKeyguardLiftControllerFactory.create(this.contextProvider, this.statusBarStateControllerImplProvider, this.asyncSensorManagerProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider));
        SystemServicesModule_ProvideWallpaperManagerFactory create3 = SystemServicesModule_ProvideWallpaperManagerFactory.create(this.contextProvider);
        this.provideWallpaperManagerProvider = create3;
        this.lockscreenWallpaperProvider = DoubleCheck.provider(LockscreenWallpaper_Factory.create(create3, SystemServicesModule_ProvideIWallPaperManagerFactory.create(), this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.provideNotificationMediaManagerProvider, this.provideMainHandlerProvider));
        Provider<LogBuffer> provider14 = DoubleCheck.provider(LogModule_ProvideDozeLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideDozeLogBufferProvider = provider14;
        DozeLogger_Factory create4 = DozeLogger_Factory.create(provider14);
        this.dozeLoggerProvider = create4;
        Provider<DozeLog> provider15 = DoubleCheck.provider(DozeLog_Factory.create(this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, create4));
        this.dozeLogProvider = provider15;
        Provider<DozeScrimController> provider16 = DoubleCheck.provider(DozeScrimController_Factory.create(this.dozeParametersProvider, provider15));
        this.dozeScrimControllerProvider = provider16;
        Provider<BiometricUnlockController> provider17 = DoubleCheck.provider(BiometricUnlockController_Factory.create(this.contextProvider, provider16, this.newKeyguardViewMediatorProvider, this.scrimControllerProvider, this.provideStatusBarProvider, this.shadeControllerImplProvider, this.notificationShadeWindowControllerProvider, this.keyguardStateControllerImplProvider, ConcurrencyModule_ProvideHandlerFactory.create(), this.keyguardUpdateMonitorProvider, this.provideResourcesProvider, this.keyguardBypassControllerProvider, this.dozeParametersProvider, this.provideMetricsLoggerProvider, this.dumpManagerProvider));
        this.biometricUnlockControllerProvider = provider17;
        this.dozeServiceHostProvider = DoubleCheck.provider(DozeServiceHost_Factory.create(this.dozeLogProvider, this.providePowerManagerProvider, this.wakefulnessLifecycleProvider, this.statusBarStateControllerImplProvider, this.deviceProvisionedControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.provideBatteryControllerProvider, this.scrimControllerProvider, provider17, this.newKeyguardViewMediatorProvider, this.assistManagerProvider, this.dozeScrimControllerProvider, this.keyguardUpdateMonitorProvider, this.provideVisualStabilityManagerProvider, this.pulseExpansionHandlerProvider, this.notificationShadeWindowControllerProvider, this.notificationWakeUpCoordinatorProvider, this.lockscreenLockIconControllerProvider));
        this.screenPinningRequestProvider = ScreenPinningRequest_Factory.create(this.contextProvider, this.optionalOfLazyOfStatusBarProvider);
        Provider<VolumeDialogControllerImpl> provider18 = DoubleCheck.provider(VolumeDialogControllerImpl_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.optionalOfLazyOfStatusBarProvider, this.ringerModeTrackerImplProvider));
        this.volumeDialogControllerImplProvider = provider18;
        this.volumeDialogComponentProvider = DoubleCheck.provider(VolumeDialogComponent_Factory.create(this.contextProvider, this.newKeyguardViewMediatorProvider, provider18));
        this.optionalOfRecentsProvider = PresentJdkOptionalInstanceProvider.of(this.provideRecentsProvider);
        this.statusBarComponentBuilderProvider = new Provider<StatusBarComponent.Builder>() { // from class: com.android.systemui.tv.DaggerTvSystemUIRootComponent.2
            @Override // javax.inject.Provider
            public StatusBarComponent.Builder get() {
                return new StatusBarComponentBuilder();
            }
        };
        this.lightsOutNotifControllerProvider = DoubleCheck.provider(LightsOutNotifController_Factory.create(this.provideWindowManagerProvider, this.provideNotificationEntryManagerProvider, this.provideCommandQueueProvider));
        this.statusBarRemoteInputCallbackProvider = DoubleCheck.provider(StatusBarRemoteInputCallback_Factory.create(this.contextProvider, this.notificationGroupManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.keyguardStateControllerImplProvider, this.statusBarStateControllerImplProvider, this.statusBarKeyguardViewManagerProvider, this.activityStarterDelegateProvider, this.shadeControllerImplProvider, this.provideCommandQueueProvider, this.actionClickLoggerProvider));
        this.activityIntentHelperProvider = DoubleCheck.provider(ActivityIntentHelper_Factory.create(this.contextProvider));
        StatusBarNotificationActivityStarterLogger_Factory create5 = StatusBarNotificationActivityStarterLogger_Factory.create(this.provideNotifInteractionLogBufferProvider);
        this.statusBarNotificationActivityStarterLoggerProvider = create5;
        this.builderProvider4 = DoubleCheck.provider(StatusBarNotificationActivityStarter_Builder_Factory.create(this.contextProvider, this.provideCommandQueueProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider, this.provideUiBackgroundExecutorProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, this.notifCollectionProvider, this.provideHeadsUpManagerPhoneProvider, this.activityStarterDelegateProvider, this.notificationClickNotifierProvider, this.statusBarStateControllerImplProvider, this.statusBarKeyguardViewManagerProvider, this.provideKeyguardManagerProvider, this.provideIDreamManagerProvider, this.newBubbleControllerProvider, this.assistManagerProvider, this.provideNotificationRemoteInputManagerProvider, this.notificationGroupManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.shadeControllerImplProvider, this.keyguardStateControllerImplProvider, this.notificationInterruptStateProviderImplProvider, this.provideLockPatternUtilsProvider, this.statusBarRemoteInputCallbackProvider, this.activityIntentHelperProvider, this.featureFlagsProvider, this.provideMetricsLoggerProvider, create5));
        Factory create6 = InstanceFactory.create(this);
        this.tvSystemUIRootComponentProvider = create6;
        this.injectionInflationControllerProvider = DoubleCheck.provider(InjectionInflationController_Factory.create(create6));
        AnonymousClass3 r1 = new Provider<NotificationRowComponent.Builder>() { // from class: com.android.systemui.tv.DaggerTvSystemUIRootComponent.3
            @Override // javax.inject.Provider
            public NotificationRowComponent.Builder get() {
                return new NotificationRowComponentBuilder();
            }
        };
        this.notificationRowComponentBuilderProvider = r1;
        this.superStatusBarViewFactoryProvider = DoubleCheck.provider(SuperStatusBarViewFactory_Factory.create(this.contextProvider, this.injectionInflationControllerProvider, r1, this.lockscreenLockIconControllerProvider));
        this.initControllerProvider = DoubleCheck.provider(InitController_Factory.create());
        this.provideTimeTickHandlerProvider = DoubleCheck.provider(DependencyProvider_ProvideTimeTickHandlerFactory.create(builder.dependencyProvider));
        this.pluginDependencyProvider = DoubleCheck.provider(PluginDependencyProvider_Factory.create(this.providePluginManagerProvider));
        this.keyguardDismissUtilProvider = DoubleCheck.provider(KeyguardDismissUtil_Factory.create());
        this.userInfoControllerImplProvider = DoubleCheck.provider(UserInfoControllerImpl_Factory.create(this.contextProvider));
        this.castControllerImplProvider = DoubleCheck.provider(CastControllerImpl_Factory.create(this.contextProvider));
        this.hotspotControllerImplProvider = DoubleCheck.provider(HotspotControllerImpl_Factory.create(this.contextProvider, this.provideMainHandlerProvider, this.provideBgHandlerProvider));
        this.provideLocalBluetoothControllerProvider = DoubleCheck.provider(SystemServicesModule_ProvideLocalBluetoothControllerFactory.create(this.contextProvider, this.provideBgHandlerProvider));
        this.bluetoothControllerImplProvider = DoubleCheck.provider(BluetoothControllerImpl_Factory.create(this.contextProvider, this.provideBgLooperProvider, ConcurrencyModule_ProvideMainLooperFactory.create(), this.provideLocalBluetoothControllerProvider));
        this.nextAlarmControllerImplProvider = DoubleCheck.provider(NextAlarmControllerImpl_Factory.create(this.contextProvider));
        this.rotationLockControllerImplProvider = DoubleCheck.provider(RotationLockControllerImpl_Factory.create(this.contextProvider));
        this.provideDataSaverControllerProvider = DoubleCheck.provider(DependencyProvider_ProvideDataSaverControllerFactory.create(builder.dependencyProvider, this.networkControllerImplProvider));
        this.zenModeControllerImplProvider = DoubleCheck.provider(ZenModeControllerImpl_Factory.create(this.contextProvider, this.provideMainHandlerProvider, this.providesBroadcastDispatcherProvider));
        this.locationControllerImplProvider = DoubleCheck.provider(LocationControllerImpl_Factory.create(this.contextProvider, ConcurrencyModule_ProvideMainLooperFactory.create(), this.provideBgLooperProvider, this.providesBroadcastDispatcherProvider, this.bootCompleteCacheImplProvider));
        this.sensorPrivacyControllerImplProvider = DoubleCheck.provider(SensorPrivacyControllerImpl_Factory.create(this.contextProvider));
        this.provideTelecomManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideTelecomManagerFactory.create(this.contextProvider));
        this.provideDisplayIdProvider = SystemServicesModule_ProvideDisplayIdFactory.create(this.contextProvider);
        this.provideSharePreferencesProvider = DependencyProvider_ProvideSharePreferencesFactory.create(builder.dependencyProvider, this.contextProvider);
        DateFormatUtil_Factory create7 = DateFormatUtil_Factory.create(this.contextProvider);
        this.dateFormatUtilProvider = create7;
        this.phoneStatusBarPolicyProvider = PhoneStatusBarPolicy_Factory.create(this.contextProvider, this.statusBarIconControllerImplProvider, this.provideCommandQueueProvider, this.providesBroadcastDispatcherProvider, this.provideUiBackgroundExecutorProvider, this.provideResourcesProvider, this.castControllerImplProvider, this.hotspotControllerImplProvider, this.bluetoothControllerImplProvider, this.nextAlarmControllerImplProvider, this.userInfoControllerImplProvider, this.rotationLockControllerImplProvider, this.provideDataSaverControllerProvider, this.zenModeControllerImplProvider, this.deviceProvisionedControllerImplProvider, this.keyguardStateControllerImplProvider, this.locationControllerImplProvider, this.sensorPrivacyControllerImplProvider, this.provideIActivityManagerProvider, this.provideAlarmManagerProvider, this.provideUserManagerProvider, this.recordingControllerProvider, this.provideTelecomManagerProvider, this.provideDisplayIdProvider, this.provideSharePreferencesProvider, create7, this.ringerModeTrackerImplProvider);
        Provider<Choreographer> provider19 = DoubleCheck.provider(DependencyProvider_ProvidesChoreographerFactory.create(builder.dependencyProvider));
        this.providesChoreographerProvider = provider19;
        this.notificationShadeDepthControllerProvider = DoubleCheck.provider(NotificationShadeDepthController_Factory.create(this.statusBarStateControllerImplProvider, this.blurUtilsProvider, this.biometricUnlockControllerProvider, this.keyguardStateControllerImplProvider, provider19, this.provideWallpaperManagerProvider, this.notificationShadeWindowControllerProvider, this.dozeParametersProvider, this.dumpManagerProvider));
        this.dismissCallbackRegistryProvider = DoubleCheck.provider(DismissCallbackRegistry_Factory.create(this.provideUiBackgroundExecutorProvider));
        Provider<StatusBarTouchableRegionManager> provider20 = DoubleCheck.provider(StatusBarTouchableRegionManager_Factory.create(this.contextProvider, this.notificationShadeWindowControllerProvider, this.provideConfigurationControllerProvider, this.provideHeadsUpManagerPhoneProvider));
        this.statusBarTouchableRegionManagerProvider = provider20;
        DelegateFactory delegateFactory2 = (DelegateFactory) this.provideStatusBarProvider;
        Provider<StatusBar> provider21 = DoubleCheck.provider(StatusBarPhoneModule_ProvideStatusBarFactory.create(this.contextProvider, this.provideNotificationsControllerProvider, this.lightBarControllerProvider, this.provideAutoHideControllerProvider, this.keyguardUpdateMonitorProvider, this.statusBarIconControllerImplProvider, this.pulseExpansionHandlerProvider, this.notificationWakeUpCoordinatorProvider, this.keyguardBypassControllerProvider, this.keyguardStateControllerImplProvider, this.provideHeadsUpManagerPhoneProvider, this.dynamicPrivacyControllerProvider, this.bypassHeadsUpNotifierProvider, this.falsingManagerProxyProvider, this.providesBroadcastDispatcherProvider, this.remoteInputQuickSettingsDisablerProvider, this.provideNotificationGutsManagerProvider, this.provideNotificationLoggerProvider, this.notificationInterruptStateProviderImplProvider, this.provideNotificationViewHierarchyManagerProvider, this.newKeyguardViewMediatorProvider, this.provideDisplayMetricsProvider, this.provideMetricsLoggerProvider, this.provideUiBackgroundExecutorProvider, this.provideNotificationMediaManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.provideNotificationRemoteInputManagerProvider, this.userSwitcherControllerProvider, this.networkControllerImplProvider, this.provideBatteryControllerProvider, this.sysuiColorExtractorProvider, this.screenLifecycleProvider, this.wakefulnessLifecycleProvider, this.statusBarStateControllerImplProvider, this.vibratorHelperProvider, this.newBubbleControllerProvider, this.notificationGroupManagerProvider, this.provideVisualStabilityManagerProvider, this.deviceProvisionedControllerImplProvider, this.provideNavigationBarControllerProvider, this.assistManagerProvider, this.provideConfigurationControllerProvider, this.notificationShadeWindowControllerProvider, this.lockscreenLockIconControllerProvider, this.dozeParametersProvider, this.scrimControllerProvider, this.provideKeyguardLiftControllerProvider, this.lockscreenWallpaperProvider, this.biometricUnlockControllerProvider, this.dozeServiceHostProvider, this.providePowerManagerProvider, this.screenPinningRequestProvider, this.dozeScrimControllerProvider, this.volumeDialogComponentProvider, this.provideCommandQueueProvider, this.optionalOfRecentsProvider, this.statusBarComponentBuilderProvider, this.providePluginManagerProvider, this.optionalOfDividerProvider, this.lightsOutNotifControllerProvider, this.builderProvider4, this.shadeControllerImplProvider, this.superStatusBarViewFactoryProvider, this.statusBarKeyguardViewManagerProvider, this.providesViewMediatorCallbackProvider, this.initControllerProvider, this.darkIconDispatcherImplProvider, this.provideTimeTickHandlerProvider, this.pluginDependencyProvider, this.keyguardDismissUtilProvider, this.extensionControllerImplProvider, this.userInfoControllerImplProvider, this.phoneStatusBarPolicyProvider, this.keyguardIndicationControllerProvider, this.notificationShadeDepthControllerProvider, this.dismissCallbackRegistryProvider, provider20));
        this.provideStatusBarProvider = provider21;
        delegateFactory2.setDelegatedProvider(provider21);
        this.mediaArtworkProcessorProvider = DoubleCheck.provider(MediaArtworkProcessor_Factory.create());
        MediaControllerFactory_Factory create8 = MediaControllerFactory_Factory.create(this.contextProvider);
        this.mediaControllerFactoryProvider = create8;
        this.mediaTimeoutListenerProvider = DoubleCheck.provider(MediaTimeoutListener_Factory.create(create8, this.provideMainDelayableExecutorProvider));
        Provider<MediaResumeListener> provider22 = DoubleCheck.provider(MediaResumeListener_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.provideBackgroundExecutorProvider, this.tunerServiceImplProvider));
        this.mediaResumeListenerProvider = provider22;
        this.mediaDataManagerProvider = DoubleCheck.provider(MediaDataManager_Factory.create(this.contextProvider, this.provideBackgroundExecutorProvider, this.provideMainExecutorProvider, this.mediaControllerFactoryProvider, this.dumpManagerProvider, this.providesBroadcastDispatcherProvider, this.mediaTimeoutListenerProvider, provider22));
        Provider<NotificationMediaManager> provider23 = DoubleCheck.provider(StatusBarDependenciesModule_ProvideNotificationMediaManagerFactory.create(this.contextProvider, this.provideStatusBarProvider, this.notificationShadeWindowControllerProvider, this.provideNotificationEntryManagerProvider, this.mediaArtworkProcessorProvider, this.keyguardBypassControllerProvider, this.provideMainDelayableExecutorProvider, DeviceConfigProxy_Factory.create(), this.mediaDataManagerProvider));
        this.provideNotificationMediaManagerProvider = provider23;
        ((DelegateFactory) this.provideNotificationMediaManagerProvider).setDelegatedProvider(provider23);
        Provider<StatusBarKeyguardViewManager> provider24 = DoubleCheck.provider(StatusBarKeyguardViewManager_Factory.create(this.contextProvider, this.providesViewMediatorCallbackProvider, this.provideLockPatternUtilsProvider, this.statusBarStateControllerImplProvider, this.provideConfigurationControllerProvider, this.keyguardUpdateMonitorProvider, this.navigationModeControllerProvider, this.dockManagerImplProvider, this.notificationShadeWindowControllerProvider, this.keyguardStateControllerImplProvider, this.provideNotificationMediaManagerProvider));
        this.statusBarKeyguardViewManagerProvider = provider24;
        ((DelegateFactory) this.statusBarKeyguardViewManagerProvider).setDelegatedProvider(provider24);
        Provider<TrustManager> provider25 = DoubleCheck.provider(SystemServicesModule_ProvideTrustManagerFactory.create(this.contextProvider));
        this.provideTrustManagerProvider = provider25;
        Provider<KeyguardViewMediator> provider26 = DoubleCheck.provider(KeyguardModule_NewKeyguardViewMediatorFactory.create(this.contextProvider, this.falsingManagerProxyProvider, this.provideLockPatternUtilsProvider, this.providesBroadcastDispatcherProvider, this.statusBarKeyguardViewManagerProvider, this.dismissCallbackRegistryProvider, this.keyguardUpdateMonitorProvider, this.dumpManagerProvider, this.providePowerManagerProvider, provider25, this.provideUiBackgroundExecutorProvider, DeviceConfigProxy_Factory.create(), this.navigationModeControllerProvider));
        this.newKeyguardViewMediatorProvider = provider26;
        ((DelegateFactory) this.newKeyguardViewMediatorProvider).setDelegatedProvider(provider26);
        Provider<NotificationShadeWindowController> provider27 = DoubleCheck.provider(NotificationShadeWindowController_Factory.create(this.contextProvider, this.provideWindowManagerProvider, this.provideIActivityManagerProvider, this.dozeParametersProvider, this.statusBarStateControllerImplProvider, this.provideConfigurationControllerProvider, this.newKeyguardViewMediatorProvider, this.keyguardBypassControllerProvider, this.sysuiColorExtractorProvider, this.dumpManagerProvider));
        this.notificationShadeWindowControllerProvider = provider27;
        ((DelegateFactory) this.notificationShadeWindowControllerProvider).setDelegatedProvider(provider27);
        this.bubbleDataProvider = DoubleCheck.provider(BubbleData_Factory.create(this.contextProvider));
        this.floatingContentCoordinatorProvider = DoubleCheck.provider(FloatingContentCoordinator_Factory.create());
        this.bubbleVolatileRepositoryProvider = DoubleCheck.provider(BubbleVolatileRepository_Factory.create(this.provideLauncherAppsProvider));
        Provider<BubblePersistentRepository> provider28 = DoubleCheck.provider(BubblePersistentRepository_Factory.create(this.contextProvider));
        this.bubblePersistentRepositoryProvider = provider28;
        Provider<BubbleDataRepository> provider29 = DoubleCheck.provider(BubbleDataRepository_Factory.create(this.bubbleVolatileRepositoryProvider, provider28, this.provideLauncherAppsProvider));
        this.bubbleDataRepositoryProvider = provider29;
        DelegateFactory delegateFactory3 = (DelegateFactory) this.newBubbleControllerProvider;
        Provider<BubbleController> provider30 = DoubleCheck.provider(BubbleModule_NewBubbleControllerFactory.create(this.contextProvider, this.notificationShadeWindowControllerProvider, this.statusBarStateControllerImplProvider, this.shadeControllerImplProvider, this.bubbleDataProvider, this.provideConfigurationControllerProvider, this.notificationInterruptStateProviderImplProvider, this.zenModeControllerImplProvider, this.notificationLockscreenUserManagerImplProvider, this.notificationGroupManagerProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, this.featureFlagsProvider, this.dumpManagerProvider, this.floatingContentCoordinatorProvider, provider29, this.provideSysUiStateProvider, this.provideINotificationManagerProvider, this.provideIStatusBarServiceProvider, this.provideWindowManagerProvider, this.provideLauncherAppsProvider));
        this.newBubbleControllerProvider = provider30;
        delegateFactory3.setDelegatedProvider(provider30);
        this.bubbleOverflowActivityProvider = BubbleOverflowActivity_Factory.create(this.newBubbleControllerProvider);
        this.usbDebuggingActivityProvider = UsbDebuggingActivity_Factory.create(this.providesBroadcastDispatcherProvider);
        this.usbDebuggingSecondaryUserActivityProvider = UsbDebuggingSecondaryUserActivity_Factory.create(this.providesBroadcastDispatcherProvider);
    }

    private void initialize4(Builder builder) {
        Provider<Executor> provider = DoubleCheck.provider(ConcurrencyModule_ProvideExecutorFactory.create(this.provideBgLooperProvider));
        this.provideExecutorProvider = provider;
        this.controlsListingControllerImplProvider = DoubleCheck.provider(ControlsListingControllerImpl_Factory.create(this.contextProvider, provider));
        this.provideBackgroundDelayableExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideBackgroundDelayableExecutorFactory.create(this.provideBgLooperProvider));
        this.controlsControllerImplProvider = new DelegateFactory();
        this.provideDelayableExecutorProvider = DoubleCheck.provider(ConcurrencyModule_ProvideDelayableExecutorFactory.create(this.provideBgLooperProvider));
        this.globalActionsComponentProvider = new DelegateFactory();
        this.provideVibratorProvider = DoubleCheck.provider(SystemServicesModule_ProvideVibratorFactory.create(this.contextProvider));
        Provider<PackageManager> provider2 = DoubleCheck.provider(SystemServicesModule_ProvidePackageManagerFactory.create(this.contextProvider));
        this.providePackageManagerProvider = provider2;
        this.providesControlsFeatureEnabledProvider = DoubleCheck.provider(ControlsModule_ProvidesControlsFeatureEnabledFactory.create(provider2));
        DelegateFactory delegateFactory = new DelegateFactory();
        this.controlsUiControllerImplProvider = delegateFactory;
        Provider<ControlsComponent> provider3 = DoubleCheck.provider(ControlsComponent_Factory.create(this.providesControlsFeatureEnabledProvider, this.controlsControllerImplProvider, delegateFactory, this.controlsListingControllerImplProvider));
        this.controlsComponentProvider = provider3;
        GlobalActionsDialog_Factory create = GlobalActionsDialog_Factory.create(this.contextProvider, this.globalActionsComponentProvider, this.provideAudioManagerProvider, this.provideIDreamManagerProvider, this.provideDevicePolicyManagerProvider, this.provideLockPatternUtilsProvider, this.providesBroadcastDispatcherProvider, this.provideConnectivityManagagerProvider, this.provideTelephonyManagerProvider, this.provideContentResolverProvider, this.provideVibratorProvider, this.provideResourcesProvider, this.provideConfigurationControllerProvider, this.activityStarterDelegateProvider, this.keyguardStateControllerImplProvider, this.provideUserManagerProvider, this.provideTrustManagerProvider, this.provideIActivityManagerProvider, this.provideTelecomManagerProvider, this.provideMetricsLoggerProvider, this.notificationShadeDepthControllerProvider, this.sysuiColorExtractorProvider, this.provideIStatusBarServiceProvider, this.notificationShadeWindowControllerProvider, this.provideIWindowManagerProvider, this.provideBackgroundExecutorProvider, this.provideUiEventLoggerProvider, this.ringerModeTrackerImplProvider, this.provideSysUiStateProvider, this.provideMainHandlerProvider, provider3, this.provideCurrentUserContextTrackerProvider);
        this.globalActionsDialogProvider = create;
        GlobalActionsImpl_Factory create2 = GlobalActionsImpl_Factory.create(this.contextProvider, this.provideCommandQueueProvider, create, this.blurUtilsProvider);
        this.globalActionsImplProvider = create2;
        Provider<GlobalActionsComponent> provider4 = DoubleCheck.provider(GlobalActionsComponent_Factory.create(this.contextProvider, this.provideCommandQueueProvider, this.extensionControllerImplProvider, create2, this.statusBarKeyguardViewManagerProvider));
        this.globalActionsComponentProvider = provider4;
        ((DelegateFactory) this.globalActionsComponentProvider).setDelegatedProvider(provider4);
        Provider<ControlActionCoordinatorImpl> provider5 = DoubleCheck.provider(ControlActionCoordinatorImpl_Factory.create(this.contextProvider, this.provideDelayableExecutorProvider, this.provideMainDelayableExecutorProvider, this.activityStarterDelegateProvider, this.keyguardStateControllerImplProvider, this.globalActionsComponentProvider));
        this.controlActionCoordinatorImplProvider = provider5;
        Provider<ControlsUiControllerImpl> provider6 = DoubleCheck.provider(ControlsUiControllerImpl_Factory.create(this.controlsControllerImplProvider, this.contextProvider, this.provideMainDelayableExecutorProvider, this.provideBackgroundDelayableExecutorProvider, this.controlsListingControllerImplProvider, this.provideSharePreferencesProvider, provider5, this.activityStarterDelegateProvider, this.shadeControllerImplProvider));
        this.controlsUiControllerImplProvider = provider6;
        ((DelegateFactory) this.controlsUiControllerImplProvider).setDelegatedProvider(provider6);
        this.controlsBindingControllerImplProvider = DoubleCheck.provider(ControlsBindingControllerImpl_Factory.create(this.contextProvider, this.provideBackgroundDelayableExecutorProvider, this.controlsControllerImplProvider));
        Provider<Optional<ControlsFavoritePersistenceWrapper>> absentJdkOptionalProvider = absentJdkOptionalProvider();
        this.optionalOfControlsFavoritePersistenceWrapperProvider = absentJdkOptionalProvider;
        Provider<ControlsControllerImpl> provider7 = DoubleCheck.provider(ControlsControllerImpl_Factory.create(this.contextProvider, this.provideBackgroundDelayableExecutorProvider, this.controlsUiControllerImplProvider, this.controlsBindingControllerImplProvider, this.controlsListingControllerImplProvider, this.providesBroadcastDispatcherProvider, absentJdkOptionalProvider, this.dumpManagerProvider));
        this.controlsControllerImplProvider = provider7;
        ((DelegateFactory) this.controlsControllerImplProvider).setDelegatedProvider(provider7);
        this.controlsProviderSelectorActivityProvider = ControlsProviderSelectorActivity_Factory.create(this.provideMainExecutorProvider, this.provideBackgroundExecutorProvider, this.controlsListingControllerImplProvider, this.controlsControllerImplProvider, this.globalActionsComponentProvider, this.providesBroadcastDispatcherProvider);
        this.controlsFavoritingActivityProvider = ControlsFavoritingActivity_Factory.create(this.provideMainExecutorProvider, this.controlsControllerImplProvider, this.controlsListingControllerImplProvider, this.providesBroadcastDispatcherProvider, this.globalActionsComponentProvider);
        this.controlsEditingActivityProvider = ControlsEditingActivity_Factory.create(this.controlsControllerImplProvider, this.providesBroadcastDispatcherProvider, this.globalActionsComponentProvider);
        this.controlsRequestDialogProvider = ControlsRequestDialog_Factory.create(this.controlsControllerImplProvider, this.providesBroadcastDispatcherProvider, this.controlsListingControllerImplProvider);
        AnonymousClass4 r1 = new Provider<TvPipComponent.Builder>() { // from class: com.android.systemui.tv.DaggerTvSystemUIRootComponent.4
            @Override // javax.inject.Provider
            public TvPipComponent.Builder get() {
                return new TvPipComponentBuilder();
            }
        };
        this.tvPipComponentBuilderProvider = r1;
        this.pipMenuActivityProvider = PipMenuActivity_Factory.create(r1, this.pipManagerProvider);
        MapProviderFactory.Builder builder2 = MapProviderFactory.builder(13);
        builder2.put(TunerActivity.class, TunerActivity_Factory.create());
        builder2.put(ForegroundServicesDialog.class, ForegroundServicesDialog_Factory.create());
        builder2.put(WorkLockActivity.class, this.workLockActivityProvider);
        builder2.put(BrightnessDialog.class, this.brightnessDialogProvider);
        builder2.put(ScreenRecordDialog.class, this.screenRecordDialogProvider);
        builder2.put(BubbleOverflowActivity.class, this.bubbleOverflowActivityProvider);
        builder2.put(UsbDebuggingActivity.class, this.usbDebuggingActivityProvider);
        builder2.put(UsbDebuggingSecondaryUserActivity.class, this.usbDebuggingSecondaryUserActivityProvider);
        builder2.put(ControlsProviderSelectorActivity.class, this.controlsProviderSelectorActivityProvider);
        builder2.put(ControlsFavoritingActivity.class, this.controlsFavoritingActivityProvider);
        builder2.put(ControlsEditingActivity.class, this.controlsEditingActivityProvider);
        builder2.put(ControlsRequestDialog.class, this.controlsRequestDialogProvider);
        builder2.put(PipMenuActivity.class, this.pipMenuActivityProvider);
        this.mapOfClassOfAndProviderOfActivityProvider = builder2.build();
        this.proximityCheckProvider = ProximitySensor_ProximityCheck_Factory.create(this.proximitySensorProvider, this.provideMainDelayableExecutorProvider);
        DozeFactory_Factory create3 = DozeFactory_Factory.create(this.falsingManagerProxyProvider, this.dozeLogProvider, this.dozeParametersProvider, this.provideBatteryControllerProvider, this.asyncSensorManagerProvider, this.provideAlarmManagerProvider, this.wakefulnessLifecycleProvider, this.keyguardUpdateMonitorProvider, this.dockManagerImplProvider, SystemServicesModule_ProvideIWallPaperManagerFactory.create(), this.proximitySensorProvider, this.proximityCheckProvider, this.builderProvider3, this.provideMainHandlerProvider, this.biometricUnlockControllerProvider, this.providesBroadcastDispatcherProvider, this.dozeServiceHostProvider);
        this.dozeFactoryProvider = create3;
        this.dozeServiceProvider = DozeService_Factory.create(create3, this.providePluginManagerProvider);
        Provider<KeyguardLifecyclesDispatcher> provider8 = DoubleCheck.provider(KeyguardLifecyclesDispatcher_Factory.create(this.screenLifecycleProvider, this.wakefulnessLifecycleProvider));
        this.keyguardLifecyclesDispatcherProvider = provider8;
        this.keyguardServiceProvider = KeyguardService_Factory.create(this.newKeyguardViewMediatorProvider, provider8);
        this.dumpHandlerProvider = DumpHandler_Factory.create(this.contextProvider, this.dumpManagerProvider, this.logBufferEulogizerProvider);
        LogBufferFreezer_Factory create4 = LogBufferFreezer_Factory.create(this.dumpManagerProvider, this.provideMainDelayableExecutorProvider);
        this.logBufferFreezerProvider = create4;
        this.systemUIServiceProvider = SystemUIService_Factory.create(this.provideMainHandlerProvider, this.dumpHandlerProvider, this.providesBroadcastDispatcherProvider, create4);
        this.systemUIAuxiliaryDumpServiceProvider = SystemUIAuxiliaryDumpService_Factory.create(this.dumpHandlerProvider);
        ScreenshotNotificationsController_Factory create5 = ScreenshotNotificationsController_Factory.create(this.contextProvider, this.provideWindowManagerProvider);
        this.screenshotNotificationsControllerProvider = create5;
        Provider<GlobalScreenshot> provider9 = DoubleCheck.provider(GlobalScreenshot_Factory.create(this.contextProvider, this.provideResourcesProvider, create5, this.provideUiEventLoggerProvider));
        this.globalScreenshotProvider = provider9;
        this.takeScreenshotServiceProvider = TakeScreenshotService_Factory.create(provider9, this.provideUserManagerProvider, this.provideUiEventLoggerProvider);
        Provider<Looper> provider10 = DoubleCheck.provider(ConcurrencyModule_ProvideLongRunningLooperFactory.create());
        this.provideLongRunningLooperProvider = provider10;
        Provider<Executor> provider11 = DoubleCheck.provider(ConcurrencyModule_ProvideLongRunningExecutorFactory.create(provider10));
        this.provideLongRunningExecutorProvider = provider11;
        this.recordingServiceProvider = RecordingService_Factory.create(this.recordingControllerProvider, provider11, this.provideUiEventLoggerProvider, this.provideNotificationManagerProvider, this.provideCurrentUserContextTrackerProvider);
        MapProviderFactory.Builder builder3 = MapProviderFactory.builder(7);
        builder3.put(DozeService.class, this.dozeServiceProvider);
        builder3.put(ImageWallpaper.class, ImageWallpaper_Factory.create());
        builder3.put(KeyguardService.class, this.keyguardServiceProvider);
        builder3.put(SystemUIService.class, this.systemUIServiceProvider);
        builder3.put(SystemUIAuxiliaryDumpService.class, this.systemUIAuxiliaryDumpServiceProvider);
        builder3.put(TakeScreenshotService.class, this.takeScreenshotServiceProvider);
        builder3.put(RecordingService.class, this.recordingServiceProvider);
        this.mapOfClassOfAndProviderOfServiceProvider = builder3.build();
        this.authControllerProvider = DoubleCheck.provider(AuthController_Factory.create(this.contextProvider, this.provideCommandQueueProvider));
        Provider<String> provider12 = DoubleCheck.provider(SystemUIDefaultModule_ProvideLeakReportEmailFactory.create());
        this.provideLeakReportEmailProvider = provider12;
        Provider<LeakReporter> provider13 = DoubleCheck.provider(LeakReporter_Factory.create(this.contextProvider, this.provideLeakDetectorProvider, provider12));
        this.leakReporterProvider = provider13;
        Provider<GarbageMonitor> provider14 = DoubleCheck.provider(GarbageMonitor_Factory.create(this.contextProvider, this.provideBgLooperProvider, this.provideLeakDetectorProvider, provider13));
        this.garbageMonitorProvider = provider14;
        this.serviceProvider = DoubleCheck.provider(GarbageMonitor_Service_Factory.create(this.contextProvider, provider14));
        this.instantAppNotifierProvider = DoubleCheck.provider(InstantAppNotifier_Factory.create(this.contextProvider, this.provideCommandQueueProvider, this.provideUiBackgroundExecutorProvider, this.provideDividerProvider));
        this.latencyTesterProvider = DoubleCheck.provider(LatencyTester_Factory.create(this.contextProvider, this.biometricUnlockControllerProvider, this.providePowerManagerProvider, this.providesBroadcastDispatcherProvider));
        this.powerUIProvider = DoubleCheck.provider(PowerUI_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.provideCommandQueueProvider, this.provideStatusBarProvider));
        this.screenDecorationsProvider = DoubleCheck.provider(ScreenDecorations_Factory.create(this.contextProvider, this.provideMainHandlerProvider, this.providesBroadcastDispatcherProvider, this.tunerServiceImplProvider));
        this.shortcutKeyDispatcherProvider = DoubleCheck.provider(ShortcutKeyDispatcher_Factory.create(this.contextProvider, this.provideDividerProvider, this.provideRecentsProvider));
        this.sizeCompatModeActivityControllerProvider = DoubleCheck.provider(SizeCompatModeActivityController_Factory.create(this.contextProvider, this.provideActivityManagerWrapperProvider, this.provideCommandQueueProvider));
        this.sliceBroadcastRelayHandlerProvider = DoubleCheck.provider(SliceBroadcastRelayHandler_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider));
        this.systemActionsProvider = DoubleCheck.provider(SystemActions_Factory.create(this.contextProvider));
        this.themeOverlayControllerProvider = DoubleCheck.provider(ThemeOverlayController_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider, this.provideBgHandlerProvider));
        this.toastUIProvider = DoubleCheck.provider(ToastUI_Factory.create(this.contextProvider, this.provideCommandQueueProvider));
        this.tvStatusBarProvider = DoubleCheck.provider(TvStatusBar_Factory.create(this.contextProvider, this.provideCommandQueueProvider, this.assistManagerProvider));
        this.volumeUIProvider = DoubleCheck.provider(VolumeUI_Factory.create(this.contextProvider, this.volumeDialogComponentProvider));
        this.windowMagnificationProvider = DoubleCheck.provider(WindowMagnification_Factory.create(this.contextProvider, this.provideMainHandlerProvider));
        this.opBiometricDialogImplProvider = DoubleCheck.provider(OpBiometricDialogImpl_Factory.create(this.contextProvider, this.provideCommandQueueProvider));
        this.opBitmojiManagerProvider = DoubleCheck.provider(OpBitmojiManager_Factory.create(this.contextProvider));
        MapProviderFactory.Builder builder4 = MapProviderFactory.builder(23);
        builder4.put(AuthController.class, this.authControllerProvider);
        builder4.put(Divider.class, this.provideDividerProvider);
        builder4.put(GarbageMonitor.Service.class, this.serviceProvider);
        builder4.put(GlobalActionsComponent.class, this.globalActionsComponentProvider);
        builder4.put(InstantAppNotifier.class, this.instantAppNotifierProvider);
        builder4.put(KeyguardViewMediator.class, this.newKeyguardViewMediatorProvider);
        builder4.put(LatencyTester.class, this.latencyTesterProvider);
        builder4.put(PipUI.class, this.pipUIProvider);
        builder4.put(PowerUI.class, this.powerUIProvider);
        builder4.put(Recents.class, this.provideRecentsProvider);
        builder4.put(ScreenDecorations.class, this.screenDecorationsProvider);
        builder4.put(ShortcutKeyDispatcher.class, this.shortcutKeyDispatcherProvider);
        builder4.put(SizeCompatModeActivityController.class, this.sizeCompatModeActivityControllerProvider);
        builder4.put(SliceBroadcastRelayHandler.class, this.sliceBroadcastRelayHandlerProvider);
        builder4.put(StatusBar.class, this.provideStatusBarProvider);
        builder4.put(SystemActions.class, this.systemActionsProvider);
        builder4.put(ThemeOverlayController.class, this.themeOverlayControllerProvider);
        builder4.put(ToastUI.class, this.toastUIProvider);
        builder4.put(TvStatusBar.class, this.tvStatusBarProvider);
        builder4.put(VolumeUI.class, this.volumeUIProvider);
        builder4.put(WindowMagnification.class, this.windowMagnificationProvider);
        builder4.put(OpBiometricDialogImpl.class, this.opBiometricDialogImplProvider);
        builder4.put(OpBitmojiManager.class, this.opBitmojiManagerProvider);
        this.mapOfClassOfAndProviderOfSystemUIProvider = builder4.build();
        this.overviewProxyRecentsImplProvider = DoubleCheck.provider(OverviewProxyRecentsImpl_Factory.create(this.optionalOfLazyOfStatusBarProvider, this.optionalOfDividerProvider));
        MapProviderFactory.Builder builder5 = MapProviderFactory.builder(1);
        builder5.put(OverviewProxyRecentsImpl.class, this.overviewProxyRecentsImplProvider);
        this.mapOfClassOfAndProviderOfRecentsImplementationProvider = builder5.build();
        this.actionProxyReceiverProvider = GlobalScreenshot_ActionProxyReceiver_Factory.create(this.optionalOfLazyOfStatusBarProvider);
        MapProviderFactory.Builder builder6 = MapProviderFactory.builder(1);
        builder6.put(GlobalScreenshot.ActionProxyReceiver.class, this.actionProxyReceiverProvider);
        MapProviderFactory build = builder6.build();
        this.mapOfClassOfAndProviderOfBroadcastReceiverProvider = build;
        Provider<ContextComponentResolver> provider15 = DoubleCheck.provider(ContextComponentResolver_Factory.create(this.mapOfClassOfAndProviderOfActivityProvider, this.mapOfClassOfAndProviderOfServiceProvider, this.mapOfClassOfAndProviderOfSystemUIProvider, this.mapOfClassOfAndProviderOfRecentsImplementationProvider, build));
        this.contextComponentResolverProvider = provider15;
        ((DelegateFactory) this.contextComponentResolverProvider).setDelegatedProvider(provider15);
        this.provideAllowNotificationLongPressProvider = DoubleCheck.provider(SystemUIDefaultModule_ProvideAllowNotificationLongPressFactory.create());
        this.flashlightControllerImplProvider = DoubleCheck.provider(FlashlightControllerImpl_Factory.create(this.contextProvider));
        this.provideNightDisplayListenerProvider = DoubleCheck.provider(DependencyProvider_ProvideNightDisplayListenerFactory.create(builder.dependencyProvider, this.contextProvider, this.provideBgHandlerProvider));
        this.managedProfileControllerImplProvider = DoubleCheck.provider(ManagedProfileControllerImpl_Factory.create(this.contextProvider, this.providesBroadcastDispatcherProvider));
        this.securityControllerImplProvider = DoubleCheck.provider(SecurityControllerImpl_Factory.create(this.contextProvider, this.provideBgHandlerProvider, this.providesBroadcastDispatcherProvider, this.provideBackgroundExecutorProvider));
        this.statusBarWindowControllerProvider = DoubleCheck.provider(StatusBarWindowController_Factory.create(this.contextProvider, this.provideWindowManagerProvider, this.superStatusBarViewFactoryProvider, this.provideResourcesProvider));
        this.fragmentServiceProvider = DoubleCheck.provider(FragmentService_Factory.create(this.tvSystemUIRootComponentProvider, this.provideConfigurationControllerProvider));
        this.accessibilityManagerWrapperProvider = DoubleCheck.provider(AccessibilityManagerWrapper_Factory.create(this.contextProvider));
        this.tunablePaddingServiceProvider = DoubleCheck.provider(TunablePadding_TunablePaddingService_Factory.create(this.tunerServiceImplProvider));
        this.uiOffloadThreadProvider = DoubleCheck.provider(UiOffloadThread_Factory.create());
        this.powerNotificationWarningsProvider = DoubleCheck.provider(PowerNotificationWarnings_Factory.create(this.contextProvider, this.activityStarterDelegateProvider));
        this.provideNotificationBlockingHelperManagerProvider = DoubleCheck.provider(NotificationsModule_ProvideNotificationBlockingHelperManagerFactory.create(this.contextProvider, this.provideNotificationGutsManagerProvider, this.provideNotificationEntryManagerProvider, this.provideMetricsLoggerProvider));
        this.provideSensorPrivacyManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideSensorPrivacyManagerFactory.create(this.contextProvider));
        ForegroundServiceLifetimeExtender_Factory create6 = ForegroundServiceLifetimeExtender_Factory.create(this.notificationInteractionTrackerProvider, this.bindSystemClockProvider);
        this.foregroundServiceLifetimeExtenderProvider = create6;
        this.foregroundServiceNotificationListenerProvider = DoubleCheck.provider(ForegroundServiceNotificationListener_Factory.create(this.contextProvider, this.foregroundServiceControllerProvider, this.provideNotificationEntryManagerProvider, this.notifPipelineProvider, create6, this.bindSystemClockProvider));
        this.clockManagerProvider = DoubleCheck.provider(ClockManager_Factory.create(this.contextProvider, this.injectionInflationControllerProvider, this.providePluginManagerProvider, this.sysuiColorExtractorProvider, this.dockManagerImplProvider, this.providesBroadcastDispatcherProvider));
        this.provideDevicePolicyManagerWrapperProvider = DoubleCheck.provider(DependencyProvider_ProvideDevicePolicyManagerWrapperFactory.create(builder.dependencyProvider));
        this.keyguardSecurityModelProvider = DoubleCheck.provider(KeyguardSecurityModel_Factory.create(this.contextProvider));
        this.opZenModeControllerImplProvider = DoubleCheck.provider(OpZenModeControllerImpl_Factory.create(this.contextProvider));
        this.opSceneModeObserverProvider = DoubleCheck.provider(OpSceneModeObserver_Factory.create(this.contextProvider));
        this.opChargingAnimationControllerImplProvider = DoubleCheck.provider(OpChargingAnimationControllerImpl_Factory.create(this.contextProvider));
        this.networkSpeedControllerImplProvider = DoubleCheck.provider(NetworkSpeedControllerImpl_Factory.create(this.contextProvider));
        this.oPWLBHelperProvider = DoubleCheck.provider(OPWLBHelper_Factory.create(this.contextProvider));
        this.opNotificationControllerProvider = DoubleCheck.provider(OpNotificationController_Factory.create(this.contextProvider));
        this.opHighlightHintControllerImplProvider = DoubleCheck.provider(OpHighlightHintControllerImpl_Factory.create());
        this.opThreekeyVolumeGuideControllerImplProvider = DoubleCheck.provider(OpThreekeyVolumeGuideControllerImpl_Factory.create(this.contextProvider));
        DelegateFactory delegateFactory2 = new DelegateFactory();
        this.qSTileHostProvider = delegateFactory2;
        this.wifiTileProvider = WifiTile_Factory.create(delegateFactory2, this.networkControllerImplProvider, this.activityStarterDelegateProvider);
        this.bluetoothTileProvider = BluetoothTile_Factory.create(this.qSTileHostProvider, this.bluetoothControllerImplProvider, this.activityStarterDelegateProvider);
        this.cellularTileProvider = CellularTile_Factory.create(this.qSTileHostProvider, this.networkControllerImplProvider, this.activityStarterDelegateProvider);
        this.dndTileProvider = DndTile_Factory.create(this.qSTileHostProvider, this.zenModeControllerImplProvider, this.activityStarterDelegateProvider, this.providesBroadcastDispatcherProvider, this.provideSharePreferencesProvider);
        this.colorInversionTileProvider = ColorInversionTile_Factory.create(this.qSTileHostProvider);
    }

    private void initialize5(Builder builder) {
        this.airplaneModeTileProvider = AirplaneModeTile_Factory.create(this.qSTileHostProvider, this.activityStarterDelegateProvider, this.providesBroadcastDispatcherProvider);
        this.workModeTileProvider = WorkModeTile_Factory.create(this.qSTileHostProvider, this.managedProfileControllerImplProvider);
        this.rotationLockTileProvider = RotationLockTile_Factory.create(this.qSTileHostProvider, this.rotationLockControllerImplProvider);
        this.flashlightTileProvider = FlashlightTile_Factory.create(this.qSTileHostProvider, this.flashlightControllerImplProvider);
        this.locationTileProvider = LocationTile_Factory.create(this.qSTileHostProvider, this.locationControllerImplProvider, this.keyguardStateControllerImplProvider, this.activityStarterDelegateProvider);
        this.castTileProvider = CastTile_Factory.create(this.qSTileHostProvider, this.castControllerImplProvider, this.keyguardStateControllerImplProvider, this.networkControllerImplProvider, this.activityStarterDelegateProvider);
        this.hotspotTileProvider = HotspotTile_Factory.create(this.qSTileHostProvider, this.hotspotControllerImplProvider, this.provideDataSaverControllerProvider);
        this.userTileProvider = UserTile_Factory.create(this.qSTileHostProvider, this.userSwitcherControllerProvider, this.userInfoControllerImplProvider);
        this.batterySaverTileProvider = BatterySaverTile_Factory.create(this.qSTileHostProvider, this.provideBatteryControllerProvider);
        this.dataSaverTileProvider = DataSaverTile_Factory.create(this.qSTileHostProvider, this.networkControllerImplProvider);
        this.nightDisplayTileProvider = NightDisplayTile_Factory.create(this.qSTileHostProvider, this.locationControllerImplProvider);
        this.nfcTileProvider = NfcTile_Factory.create(this.qSTileHostProvider, this.providesBroadcastDispatcherProvider);
        this.memoryTileProvider = GarbageMonitor_MemoryTile_Factory.create(this.qSTileHostProvider, this.garbageMonitorProvider, this.activityStarterDelegateProvider);
        this.uiModeNightTileProvider = UiModeNightTile_Factory.create(this.qSTileHostProvider, this.provideConfigurationControllerProvider, this.provideBatteryControllerProvider, this.locationControllerImplProvider);
        this.screenRecordTileProvider = ScreenRecordTile_Factory.create(this.qSTileHostProvider, this.recordingControllerProvider, this.keyguardDismissUtilProvider);
        this.readModeTileProvider = ReadModeTile_Factory.create(this.qSTileHostProvider);
        this.gameModeTileProvider = GameModeTile_Factory.create(this.qSTileHostProvider);
        this.oPDndCarModeTileProvider = OPDndCarModeTile_Factory.create(this.qSTileHostProvider);
        this.otgTileProvider = OtgTile_Factory.create(this.qSTileHostProvider);
        this.dataSwitchTileProvider = DataSwitchTile_Factory.create(this.qSTileHostProvider);
        this.vPNTileProvider = VPNTile_Factory.create(this.qSTileHostProvider);
        this.oPDndTileProvider = OPDndTile_Factory.create(this.qSTileHostProvider);
        OPReverseChargeTile_Factory create = OPReverseChargeTile_Factory.create(this.qSTileHostProvider, this.provideBatteryControllerProvider);
        this.oPReverseChargeTileProvider = create;
        this.qSFactoryImplProvider = DoubleCheck.provider(QSFactoryImpl_Factory.create(this.qSTileHostProvider, this.wifiTileProvider, this.bluetoothTileProvider, this.cellularTileProvider, this.dndTileProvider, this.colorInversionTileProvider, this.airplaneModeTileProvider, this.workModeTileProvider, this.rotationLockTileProvider, this.flashlightTileProvider, this.locationTileProvider, this.castTileProvider, this.hotspotTileProvider, this.userTileProvider, this.batterySaverTileProvider, this.dataSaverTileProvider, this.nightDisplayTileProvider, this.nfcTileProvider, this.memoryTileProvider, this.uiModeNightTileProvider, this.screenRecordTileProvider, this.readModeTileProvider, this.gameModeTileProvider, this.oPDndCarModeTileProvider, this.otgTileProvider, this.dataSwitchTileProvider, this.vPNTileProvider, this.oPDndTileProvider, create));
        AutoAddTracker_Builder_Factory create2 = AutoAddTracker_Builder_Factory.create(this.contextProvider);
        this.builderProvider5 = create2;
        this.autoTileManagerProvider = AutoTileManager_Factory.create(this.contextProvider, create2, this.qSTileHostProvider, this.provideBgHandlerProvider, this.hotspotControllerImplProvider, this.provideDataSaverControllerProvider, this.managedProfileControllerImplProvider, this.provideNightDisplayListenerProvider, this.castControllerImplProvider);
        this.optionalOfStatusBarProvider = PresentJdkOptionalInstanceProvider.of(this.provideStatusBarProvider);
        Provider<LogBuffer> provider = DoubleCheck.provider(LogModule_ProvideQuickSettingsLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideQuickSettingsLogBufferProvider = provider;
        QSLogger_Factory create3 = QSLogger_Factory.create(provider);
        this.qSLoggerProvider = create3;
        Provider<QSTileHost> provider2 = DoubleCheck.provider(QSTileHost_Factory.create(this.contextProvider, this.statusBarIconControllerImplProvider, this.qSFactoryImplProvider, this.provideMainHandlerProvider, this.provideBgLooperProvider, this.providePluginManagerProvider, this.tunerServiceImplProvider, this.autoTileManagerProvider, this.dumpManagerProvider, this.providesBroadcastDispatcherProvider, this.optionalOfStatusBarProvider, create3, this.provideUiEventLoggerProvider));
        this.qSTileHostProvider = provider2;
        ((DelegateFactory) this.qSTileHostProvider).setDelegatedProvider(provider2);
        this.context = builder.context;
        Provider<MediaHostStatesManager> provider3 = DoubleCheck.provider(MediaHostStatesManager_Factory.create());
        this.mediaHostStatesManagerProvider = provider3;
        this.mediaViewControllerProvider = MediaViewController_Factory.create(this.contextProvider, this.provideConfigurationControllerProvider, provider3);
        Provider<RepeatableExecutor> provider4 = DoubleCheck.provider(ConcurrencyModule_ProvideBackgroundRepeatableExecutorFactory.create(this.provideBackgroundDelayableExecutorProvider));
        this.provideBackgroundRepeatableExecutorProvider = provider4;
        SeekBarViewModel_Factory create4 = SeekBarViewModel_Factory.create(provider4);
        this.seekBarViewModelProvider = create4;
        this.mediaControlPanelProvider = MediaControlPanel_Factory.create(this.contextProvider, this.provideBackgroundExecutorProvider, this.activityStarterDelegateProvider, this.mediaViewControllerProvider, create4);
        this.localMediaManagerFactoryProvider = LocalMediaManagerFactory_Factory.create(this.contextProvider, this.provideLocalBluetoothControllerProvider);
        SystemServicesModule_ProvideMediaRouter2ManagerFactory create5 = SystemServicesModule_ProvideMediaRouter2ManagerFactory.create(this.contextProvider);
        this.provideMediaRouter2ManagerProvider = create5;
        Provider<MediaDeviceManager> provider5 = DoubleCheck.provider(MediaDeviceManager_Factory.create(this.contextProvider, this.localMediaManagerFactoryProvider, create5, this.provideMainExecutorProvider, this.mediaDataManagerProvider, this.dumpManagerProvider));
        this.mediaDeviceManagerProvider = provider5;
        Provider<MediaDataCombineLatest> provider6 = DoubleCheck.provider(MediaDataCombineLatest_Factory.create(this.mediaDataManagerProvider, provider5));
        this.mediaDataCombineLatestProvider = provider6;
        Provider<MediaDataFilter> provider7 = DoubleCheck.provider(MediaDataFilter_Factory.create(provider6, this.providesBroadcastDispatcherProvider, this.mediaResumeListenerProvider, this.mediaDataManagerProvider, this.notificationLockscreenUserManagerImplProvider, this.provideMainExecutorProvider));
        this.mediaDataFilterProvider = provider7;
        Provider<MediaCarouselController> provider8 = DoubleCheck.provider(MediaCarouselController_Factory.create(this.contextProvider, this.mediaControlPanelProvider, this.provideVisualStabilityManagerProvider, this.mediaHostStatesManagerProvider, this.activityStarterDelegateProvider, this.provideMainDelayableExecutorProvider, provider7, this.provideConfigurationControllerProvider, this.falsingManagerProxyProvider));
        this.mediaCarouselControllerProvider = provider8;
        this.mediaHierarchyManagerProvider = DoubleCheck.provider(MediaHierarchyManager_Factory.create(this.contextProvider, this.statusBarStateControllerImplProvider, this.keyguardStateControllerImplProvider, this.keyguardBypassControllerProvider, provider8, this.notificationLockscreenUserManagerImplProvider, this.wakefulnessLifecycleProvider));
        MediaHost_Factory create6 = MediaHost_Factory.create(MediaHost_MediaHostStateHolder_Factory.create(), this.mediaHierarchyManagerProvider, this.mediaDataFilterProvider, this.mediaHostStatesManagerProvider, this.provideNotificationMediaManagerProvider);
        this.mediaHostProvider = create6;
        this.keyguardMediaControllerProvider = DoubleCheck.provider(KeyguardMediaController_Factory.create(create6, this.keyguardBypassControllerProvider, this.statusBarStateControllerImplProvider, this.notificationLockscreenUserManagerImplProvider, this.provideNotificationMediaManagerProvider));
        Provider<PeopleHubDataSourceImpl> provider9 = DoubleCheck.provider(PeopleHubDataSourceImpl_Factory.create(this.provideNotificationEntryManagerProvider, this.notificationPersonExtractorPluginBoundaryProvider, this.provideUserManagerProvider, this.provideLauncherAppsProvider, this.providePackageManagerProvider, this.contextProvider, this.provideNotificationListenerProvider, this.provideBackgroundExecutorProvider, this.provideMainExecutorProvider, this.notificationLockscreenUserManagerImplProvider, this.peopleNotificationIdentifierImplProvider));
        this.peopleHubDataSourceImplProvider = provider9;
        Provider<PeopleHubViewModelFactoryDataSourceImpl> provider10 = DoubleCheck.provider(PeopleHubViewModelFactoryDataSourceImpl_Factory.create(this.activityStarterDelegateProvider, provider9));
        this.peopleHubViewModelFactoryDataSourceImplProvider = provider10;
        this.peopleHubViewAdapterImplProvider = DoubleCheck.provider(PeopleHubViewAdapterImpl_Factory.create(provider10));
        Provider<LogBuffer> provider11 = DoubleCheck.provider(LogModule_ProvideNotificationSectionLogBufferFactory.create(this.provideLogcatEchoTrackerProvider, this.dumpManagerProvider));
        this.provideNotificationSectionLogBufferProvider = provider11;
        this.notificationSectionsLoggerProvider = DoubleCheck.provider(NotificationSectionsLogger_Factory.create(provider11));
        this.provideLatencyTrackerProvider = DoubleCheck.provider(SystemServicesModule_ProvideLatencyTrackerFactory.create(this.contextProvider));
        this.provideActivityManagerProvider = DoubleCheck.provider(SystemServicesModule_ProvideActivityManagerFactory.create(this.contextProvider));
        this.providerLayoutInflaterProvider = DoubleCheck.provider(DependencyProvider_ProviderLayoutInflaterFactory.create(builder.dependencyProvider, this.contextProvider));
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public BootCompleteCacheImpl provideBootCacheImpl() {
        return this.bootCompleteCacheImplProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public ConfigurationController getConfigurationController() {
        return this.provideConfigurationControllerProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public ContextComponentHelper getContextComponentHelper() {
        return this.contextComponentResolverProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public DumpManager createDumpManager() {
        return this.dumpManagerProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public InitController getInitController() {
        return this.initControllerProvider.get();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public void inject(SystemUIAppComponentFactory systemUIAppComponentFactory) {
        injectSystemUIAppComponentFactory(systemUIAppComponentFactory);
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public void inject(KeyguardSliceProvider keyguardSliceProvider) {
        injectKeyguardSliceProvider(keyguardSliceProvider);
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public Dependency.DependencyInjector createDependency() {
        return new DependencyInjectorImpl();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public FragmentService.FragmentCreator createFragmentCreator() {
        return new FragmentCreatorImpl();
    }

    @Override // com.android.systemui.dagger.SystemUIRootComponent
    public InjectionInflationController.ViewCreator createViewCreator() {
        return new ViewCreatorImpl();
    }

    private SystemUIAppComponentFactory injectSystemUIAppComponentFactory(SystemUIAppComponentFactory systemUIAppComponentFactory) {
        SystemUIAppComponentFactory_MembersInjector.injectMComponentHelper(systemUIAppComponentFactory, this.contextComponentResolverProvider.get());
        return systemUIAppComponentFactory;
    }

    private KeyguardSliceProvider injectKeyguardSliceProvider(KeyguardSliceProvider keyguardSliceProvider) {
        KeyguardSliceProvider_MembersInjector.injectMDozeParameters(keyguardSliceProvider, this.dozeParametersProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMZenModeController(keyguardSliceProvider, this.zenModeControllerImplProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMNextAlarmController(keyguardSliceProvider, this.nextAlarmControllerImplProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMAlarmManager(keyguardSliceProvider, this.provideAlarmManagerProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMContentResolver(keyguardSliceProvider, this.provideContentResolverProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMMediaManager(keyguardSliceProvider, this.provideNotificationMediaManagerProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMStatusBarStateController(keyguardSliceProvider, this.statusBarStateControllerImplProvider.get());
        KeyguardSliceProvider_MembersInjector.injectMKeyguardBypassController(keyguardSliceProvider, this.keyguardBypassControllerProvider.get());
        return keyguardSliceProvider;
    }

    private static <T> Provider<Optional<T>> absentJdkOptionalProvider() {
        return ABSENT_JDK_OPTIONAL_PROVIDER;
    }

    /* access modifiers changed from: private */
    public static final class PresentJdkOptionalLazyProvider<T> implements Provider<Optional<Lazy<T>>> {
        private final Provider<T> delegate;

        private PresentJdkOptionalLazyProvider(Provider<T> provider) {
            Preconditions.checkNotNull(provider);
            this.delegate = provider;
        }

        @Override // javax.inject.Provider
        public Optional<Lazy<T>> get() {
            return Optional.of(DoubleCheck.lazy(this.delegate));
        }

        /* access modifiers changed from: private */
        public static <T> Provider<Optional<Lazy<T>>> of(Provider<T> provider) {
            return new PresentJdkOptionalLazyProvider(provider);
        }
    }

    /* access modifiers changed from: private */
    public static final class PresentJdkOptionalInstanceProvider<T> implements Provider<Optional<T>> {
        private final Provider<T> delegate;

        private PresentJdkOptionalInstanceProvider(Provider<T> provider) {
            Preconditions.checkNotNull(provider);
            this.delegate = provider;
        }

        @Override // javax.inject.Provider
        public Optional<T> get() {
            return Optional.of(this.delegate.get());
        }

        /* access modifiers changed from: private */
        public static <T> Provider<Optional<T>> of(Provider<T> provider) {
            return new PresentJdkOptionalInstanceProvider(provider);
        }
    }

    /* access modifiers changed from: private */
    public static final class Builder implements TvSystemUIRootComponent.Builder {
        private Context context;
        private DependencyProvider dependencyProvider;

        private Builder() {
        }

        @Override // com.android.systemui.tv.TvSystemUIRootComponent.Builder
        public TvSystemUIRootComponent build() {
            if (this.dependencyProvider == null) {
                this.dependencyProvider = new DependencyProvider();
            }
            if (this.context != null) {
                return new DaggerTvSystemUIRootComponent(this);
            }
            throw new IllegalStateException(Context.class.getCanonicalName() + " must be set");
        }

        @Override // com.android.systemui.tv.TvSystemUIRootComponent.Builder
        public Builder context(Context context) {
            Preconditions.checkNotNull(context);
            this.context = context;
            return this;
        }
    }

    private final class DependencyInjectorImpl implements Dependency.DependencyInjector {
        private DependencyInjectorImpl() {
        }

        @Override // com.android.systemui.Dependency.DependencyInjector
        public void createSystemUI(Dependency dependency) {
            injectDependency(dependency);
        }

        private Dependency injectDependency(Dependency dependency) {
            Dependency_MembersInjector.injectMDumpManager(dependency, (DumpManager) DaggerTvSystemUIRootComponent.this.dumpManagerProvider.get());
            Dependency_MembersInjector.injectMActivityStarter(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider));
            Dependency_MembersInjector.injectMBroadcastDispatcher(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.providesBroadcastDispatcherProvider));
            Dependency_MembersInjector.injectMAsyncSensorManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.asyncSensorManagerProvider));
            Dependency_MembersInjector.injectMBluetoothController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.bluetoothControllerImplProvider));
            Dependency_MembersInjector.injectMLocationController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.locationControllerImplProvider));
            Dependency_MembersInjector.injectMRotationLockController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.rotationLockControllerImplProvider));
            Dependency_MembersInjector.injectMNetworkController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.networkControllerImplProvider));
            Dependency_MembersInjector.injectMZenModeController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.zenModeControllerImplProvider));
            Dependency_MembersInjector.injectMHotspotController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.hotspotControllerImplProvider));
            Dependency_MembersInjector.injectMCastController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.castControllerImplProvider));
            Dependency_MembersInjector.injectMFlashlightController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.flashlightControllerImplProvider));
            Dependency_MembersInjector.injectMUserSwitcherController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.userSwitcherControllerProvider));
            Dependency_MembersInjector.injectMUserInfoController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.userInfoControllerImplProvider));
            Dependency_MembersInjector.injectMKeyguardMonitor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.keyguardStateControllerImplProvider));
            Dependency_MembersInjector.injectMKeyguardUpdateMonitor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.keyguardUpdateMonitorProvider));
            Dependency_MembersInjector.injectMBatteryController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideBatteryControllerProvider));
            Dependency_MembersInjector.injectMNightDisplayListener(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNightDisplayListenerProvider));
            Dependency_MembersInjector.injectMManagedProfileController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.managedProfileControllerImplProvider));
            Dependency_MembersInjector.injectMNextAlarmController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.nextAlarmControllerImplProvider));
            Dependency_MembersInjector.injectMDataSaverController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideDataSaverControllerProvider));
            Dependency_MembersInjector.injectMAccessibilityController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.accessibilityControllerProvider));
            Dependency_MembersInjector.injectMDeviceProvisionedController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.deviceProvisionedControllerImplProvider));
            Dependency_MembersInjector.injectMPluginManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.providePluginManagerProvider));
            Dependency_MembersInjector.injectMAssistManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.assistManagerProvider));
            Dependency_MembersInjector.injectMSecurityController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.securityControllerImplProvider));
            Dependency_MembersInjector.injectMLeakDetector(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideLeakDetectorProvider));
            Dependency_MembersInjector.injectMLeakReporter(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.leakReporterProvider));
            Dependency_MembersInjector.injectMGarbageMonitor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.garbageMonitorProvider));
            Dependency_MembersInjector.injectMTunerService(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.tunerServiceImplProvider));
            Dependency_MembersInjector.injectMNotificationShadeWindowController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.notificationShadeWindowControllerProvider));
            Dependency_MembersInjector.injectMTempStatusBarWindowController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.statusBarWindowControllerProvider));
            Dependency_MembersInjector.injectMDarkIconDispatcher(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.darkIconDispatcherImplProvider));
            Dependency_MembersInjector.injectMConfigurationController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideConfigurationControllerProvider));
            Dependency_MembersInjector.injectMStatusBarIconController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.statusBarIconControllerImplProvider));
            Dependency_MembersInjector.injectMScreenLifecycle(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.screenLifecycleProvider));
            Dependency_MembersInjector.injectMWakefulnessLifecycle(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.wakefulnessLifecycleProvider));
            Dependency_MembersInjector.injectMFragmentService(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.fragmentServiceProvider));
            Dependency_MembersInjector.injectMExtensionController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.extensionControllerImplProvider));
            Dependency_MembersInjector.injectMPluginDependencyProvider(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.pluginDependencyProvider));
            Dependency_MembersInjector.injectMLocalBluetoothManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideLocalBluetoothControllerProvider));
            Dependency_MembersInjector.injectMVolumeDialogController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.volumeDialogControllerImplProvider));
            Dependency_MembersInjector.injectMMetricsLogger(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideMetricsLoggerProvider));
            Dependency_MembersInjector.injectMAccessibilityManagerWrapper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.accessibilityManagerWrapperProvider));
            Dependency_MembersInjector.injectMSysuiColorExtractor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.sysuiColorExtractorProvider));
            Dependency_MembersInjector.injectMTunablePaddingService(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.tunablePaddingServiceProvider));
            Dependency_MembersInjector.injectMForegroundServiceController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.foregroundServiceControllerProvider));
            Dependency_MembersInjector.injectMUiOffloadThread(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.uiOffloadThreadProvider));
            Dependency_MembersInjector.injectMWarningsUI(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.powerNotificationWarningsProvider));
            Dependency_MembersInjector.injectMLightBarController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.lightBarControllerProvider));
            Dependency_MembersInjector.injectMIWindowManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideIWindowManagerProvider));
            Dependency_MembersInjector.injectMOverviewProxyService(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.overviewProxyServiceProvider));
            Dependency_MembersInjector.injectMNavBarModeController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.navigationModeControllerProvider));
            Dependency_MembersInjector.injectMEnhancedEstimates(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.enhancedEstimatesImplProvider));
            Dependency_MembersInjector.injectMVibratorHelper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.vibratorHelperProvider));
            Dependency_MembersInjector.injectMIStatusBarService(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideIStatusBarServiceProvider));
            Dependency_MembersInjector.injectMDisplayMetrics(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideDisplayMetricsProvider));
            Dependency_MembersInjector.injectMLockscreenGestureLogger(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.lockscreenGestureLoggerProvider));
            Dependency_MembersInjector.injectMKeyguardEnvironment(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.keyguardEnvironmentImplProvider));
            Dependency_MembersInjector.injectMShadeController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.shadeControllerImplProvider));
            Dependency_MembersInjector.injectMNotificationRemoteInputManagerCallback(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.statusBarRemoteInputCallbackProvider));
            Dependency_MembersInjector.injectMAppOpsController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.appOpsControllerImplProvider));
            Dependency_MembersInjector.injectMNavigationBarController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNavigationBarControllerProvider));
            Dependency_MembersInjector.injectMStatusBarStateController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider));
            Dependency_MembersInjector.injectMNotificationLockscreenUserManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider));
            Dependency_MembersInjector.injectMNotificationGroupAlertTransferHelper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationGroupAlertTransferHelperProvider));
            Dependency_MembersInjector.injectMNotificationGroupManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.notificationGroupManagerProvider));
            Dependency_MembersInjector.injectMVisualStabilityManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideVisualStabilityManagerProvider));
            Dependency_MembersInjector.injectMNotificationGutsManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationGutsManagerProvider));
            Dependency_MembersInjector.injectMNotificationMediaManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationMediaManagerProvider));
            Dependency_MembersInjector.injectMNotificationBlockingHelperManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationBlockingHelperManagerProvider));
            Dependency_MembersInjector.injectMNotificationRemoteInputManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationRemoteInputManagerProvider));
            Dependency_MembersInjector.injectMSmartReplyConstants(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.smartReplyConstantsProvider));
            Dependency_MembersInjector.injectMNotificationListener(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationListenerProvider));
            Dependency_MembersInjector.injectMNotificationLogger(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationLoggerProvider));
            Dependency_MembersInjector.injectMNotificationViewHierarchyManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationViewHierarchyManagerProvider));
            Dependency_MembersInjector.injectMNotificationFilter(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.notificationFilterProvider));
            Dependency_MembersInjector.injectMKeyguardDismissUtil(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.keyguardDismissUtilProvider));
            Dependency_MembersInjector.injectMSmartReplyController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideSmartReplyControllerProvider));
            Dependency_MembersInjector.injectMRemoteInputQuickSettingsDisabler(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.remoteInputQuickSettingsDisablerProvider));
            Dependency_MembersInjector.injectMBubbleController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.newBubbleControllerProvider));
            Dependency_MembersInjector.injectMNotificationEntryManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideNotificationEntryManagerProvider));
            Dependency_MembersInjector.injectMSensorPrivacyManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideSensorPrivacyManagerProvider));
            Dependency_MembersInjector.injectMAutoHideController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideAutoHideControllerProvider));
            Dependency_MembersInjector.injectMForegroundServiceNotificationListener(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.foregroundServiceNotificationListenerProvider));
            Dependency_MembersInjector.injectMBgLooper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideBgLooperProvider));
            Dependency_MembersInjector.injectMBgHandler(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideBgHandlerProvider));
            Dependency_MembersInjector.injectMMainLooper(dependency, DoubleCheck.lazy(ConcurrencyModule_ProvideMainLooperFactory.create()));
            Dependency_MembersInjector.injectMMainHandler(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideMainHandlerProvider));
            Dependency_MembersInjector.injectMTimeTickHandler(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideTimeTickHandlerProvider));
            Dependency_MembersInjector.injectMLeakReportEmail(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideLeakReportEmailProvider));
            Dependency_MembersInjector.injectMMainExecutor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideMainExecutorProvider));
            Dependency_MembersInjector.injectMBackgroundExecutor(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideBackgroundExecutorProvider));
            Dependency_MembersInjector.injectMClockManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.clockManagerProvider));
            Dependency_MembersInjector.injectMActivityManagerWrapper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideActivityManagerWrapperProvider));
            Dependency_MembersInjector.injectMDevicePolicyManagerWrapper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideDevicePolicyManagerWrapperProvider));
            Dependency_MembersInjector.injectMPackageManagerWrapper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.providePackageManagerWrapperProvider));
            Dependency_MembersInjector.injectMSensorPrivacyController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.sensorPrivacyControllerImplProvider));
            Dependency_MembersInjector.injectMDockManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.dockManagerImplProvider));
            Dependency_MembersInjector.injectMINotificationManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideINotificationManagerProvider));
            Dependency_MembersInjector.injectMSysUiStateFlagsContainer(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideSysUiStateProvider));
            Dependency_MembersInjector.injectMAlarmManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideAlarmManagerProvider));
            Dependency_MembersInjector.injectMKeyguardSecurityModel(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.keyguardSecurityModelProvider));
            Dependency_MembersInjector.injectMDozeParameters(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.dozeParametersProvider));
            Dependency_MembersInjector.injectMWallpaperManager(dependency, DoubleCheck.lazy(SystemServicesModule_ProvideIWallPaperManagerFactory.create()));
            Dependency_MembersInjector.injectMCommandQueue(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider));
            Dependency_MembersInjector.injectMRecents(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideRecentsProvider));
            Dependency_MembersInjector.injectMStatusBar(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideStatusBarProvider));
            Dependency_MembersInjector.injectMDisplayController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.displayControllerProvider));
            Dependency_MembersInjector.injectMSystemWindows(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.systemWindowsProvider));
            Dependency_MembersInjector.injectMDisplayImeController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.displayImeControllerProvider));
            Dependency_MembersInjector.injectMRecordingController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.recordingControllerProvider));
            Dependency_MembersInjector.injectMProtoTracer(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.protoTracerProvider));
            Dependency_MembersInjector.injectMDivider(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideDividerProvider));
            Dependency_MembersInjector.injectMOpZenModeController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.opZenModeControllerImplProvider));
            Dependency_MembersInjector.injectMOpBiometricDialogImpl(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.opBiometricDialogImplProvider));
            Dependency_MembersInjector.injectMOpSceneModeObserver(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.opSceneModeObserverProvider));
            Dependency_MembersInjector.injectMOpChargingAnimationController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.opChargingAnimationControllerImplProvider));
            Dependency_MembersInjector.injectMOpNetworkSpeedController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.networkSpeedControllerImplProvider));
            Dependency_MembersInjector.injectMOPWLBHelper(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.oPWLBHelperProvider));
            Dependency_MembersInjector.injectMOpNotificationController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.opNotificationControllerProvider));
            Dependency_MembersInjector.injectMOpHighlightHintController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.opHighlightHintControllerImplProvider));
            Dependency_MembersInjector.injectMOpThreekeyVolumeGuideController(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.opThreekeyVolumeGuideControllerImplProvider));
            Dependency_MembersInjector.injectMOpBitmojiManager(dependency, DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.opBitmojiManagerProvider));
            return dependency;
        }
    }

    private final class FragmentCreatorImpl implements FragmentService.FragmentCreator {
        private FragmentCreatorImpl() {
        }

        private CarrierTextController.Builder getBuilder4() {
            return new CarrierTextController.Builder(DaggerTvSystemUIRootComponent.this.context, DaggerTvSystemUIRootComponent.this.getMainResources());
        }

        private QSCarrierGroupController$Builder getBuilder3() {
            return new QSCarrierGroupController$Builder((ActivityStarter) DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider.get(), DaggerTvSystemUIRootComponent.this.getBackgroundHandler(), ConcurrencyModule_ProvideMainLooperFactory.proxyProvideMainLooper(), (NetworkController) DaggerTvSystemUIRootComponent.this.networkControllerImplProvider.get(), getBuilder4());
        }

        private QuickStatusBarHeaderController.Builder getBuilder2() {
            return new QuickStatusBarHeaderController.Builder(getBuilder3());
        }

        private QSContainerImplController.Builder getBuilder() {
            return new QSContainerImplController.Builder(getBuilder2());
        }

        @Override // com.android.systemui.fragments.FragmentService.FragmentCreator
        public NavigationBarFragment createNavigationBarFragment() {
            return new NavigationBarFragment((AccessibilityManagerWrapper) DaggerTvSystemUIRootComponent.this.accessibilityManagerWrapperProvider.get(), (DeviceProvisionedController) DaggerTvSystemUIRootComponent.this.deviceProvisionedControllerImplProvider.get(), (MetricsLogger) DaggerTvSystemUIRootComponent.this.provideMetricsLoggerProvider.get(), (AssistManager) DaggerTvSystemUIRootComponent.this.assistManagerProvider.get(), (OverviewProxyService) DaggerTvSystemUIRootComponent.this.overviewProxyServiceProvider.get(), (NavigationModeController) DaggerTvSystemUIRootComponent.this.navigationModeControllerProvider.get(), (StatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (SysUiState) DaggerTvSystemUIRootComponent.this.provideSysUiStateProvider.get(), (BroadcastDispatcher) DaggerTvSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get(), (CommandQueue) DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider.get(), (Divider) DaggerTvSystemUIRootComponent.this.provideDividerProvider.get(), Optional.of((Recents) DaggerTvSystemUIRootComponent.this.provideRecentsProvider.get()), DoubleCheck.lazy(DaggerTvSystemUIRootComponent.this.provideStatusBarProvider), (ShadeController) DaggerTvSystemUIRootComponent.this.shadeControllerImplProvider.get(), (NotificationRemoteInputManager) DaggerTvSystemUIRootComponent.this.provideNotificationRemoteInputManagerProvider.get(), (SystemActions) DaggerTvSystemUIRootComponent.this.systemActionsProvider.get(), DaggerTvSystemUIRootComponent.this.getMainHandler(), (UiEventLogger) DaggerTvSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
        }

        @Override // com.android.systemui.fragments.FragmentService.FragmentCreator
        public QSFragment createQSFragment() {
            return new QSFragment((RemoteInputQuickSettingsDisabler) DaggerTvSystemUIRootComponent.this.remoteInputQuickSettingsDisablerProvider.get(), (InjectionInflationController) DaggerTvSystemUIRootComponent.this.injectionInflationControllerProvider.get(), (QSTileHost) DaggerTvSystemUIRootComponent.this.qSTileHostProvider.get(), (StatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (CommandQueue) DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider.get(), getBuilder());
        }
    }

    /* access modifiers changed from: private */
    public final class ViewCreatorImpl implements InjectionInflationController.ViewCreator {
        private ViewCreatorImpl() {
        }

        @Override // com.android.systemui.util.InjectionInflationController.ViewCreator
        public InjectionInflationController.ViewInstanceCreator createInstanceCreator(InjectionInflationController.ViewAttributeProvider viewAttributeProvider) {
            return new ViewInstanceCreatorImpl(viewAttributeProvider);
        }

        private final class ViewInstanceCreatorImpl implements InjectionInflationController.ViewInstanceCreator {
            private InjectionInflationController.ViewAttributeProvider viewAttributeProvider;

            private ViewInstanceCreatorImpl(InjectionInflationController.ViewAttributeProvider viewAttributeProvider) {
                initialize(viewAttributeProvider);
            }

            private NotificationSectionsManager getNotificationSectionsManager() {
                return new NotificationSectionsManager((ActivityStarter) DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (StatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (ConfigurationController) DaggerTvSystemUIRootComponent.this.provideConfigurationControllerProvider.get(), (PeopleHubViewAdapter) DaggerTvSystemUIRootComponent.this.peopleHubViewAdapterImplProvider.get(), (KeyguardMediaController) DaggerTvSystemUIRootComponent.this.keyguardMediaControllerProvider.get(), DaggerTvSystemUIRootComponent.this.getNotificationSectionsFeatureManager(), (NotificationSectionsLogger) DaggerTvSystemUIRootComponent.this.notificationSectionsLoggerProvider.get());
            }

            private TileQueryHelper getTileQueryHelper() {
                return new TileQueryHelper(DaggerTvSystemUIRootComponent.this.context, DaggerTvSystemUIRootComponent.this.getMainExecutor(), (Executor) DaggerTvSystemUIRootComponent.this.provideBackgroundExecutorProvider.get());
            }

            private void initialize(InjectionInflationController.ViewAttributeProvider viewAttributeProvider) {
                Preconditions.checkNotNull(viewAttributeProvider);
                this.viewAttributeProvider = viewAttributeProvider;
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QuickStatusBarHeader createQsHeader() {
                return new QuickStatusBarHeader(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (NextAlarmController) DaggerTvSystemUIRootComponent.this.nextAlarmControllerImplProvider.get(), (ZenModeController) DaggerTvSystemUIRootComponent.this.zenModeControllerImplProvider.get(), (StatusBarIconController) DaggerTvSystemUIRootComponent.this.statusBarIconControllerImplProvider.get(), (ActivityStarter) DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (CommandQueue) DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider.get(), (RingerModeTracker) DaggerTvSystemUIRootComponent.this.ringerModeTrackerImplProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QSFooterImpl createQsFooter() {
                return new QSFooterImpl(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (ActivityStarter) DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (UserInfoController) DaggerTvSystemUIRootComponent.this.userInfoControllerImplProvider.get(), (DeviceProvisionedController) DaggerTvSystemUIRootComponent.this.deviceProvisionedControllerImplProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public NotificationStackScrollLayout createNotificationStackScrollLayout() {
                return new NotificationStackScrollLayout(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), ((Boolean) DaggerTvSystemUIRootComponent.this.provideAllowNotificationLongPressProvider.get()).booleanValue(), (NotificationRoundnessManager) DaggerTvSystemUIRootComponent.this.notificationRoundnessManagerProvider.get(), (DynamicPrivacyController) DaggerTvSystemUIRootComponent.this.dynamicPrivacyControllerProvider.get(), (SysuiStatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (HeadsUpManagerPhone) DaggerTvSystemUIRootComponent.this.provideHeadsUpManagerPhoneProvider.get(), (KeyguardBypassController) DaggerTvSystemUIRootComponent.this.keyguardBypassControllerProvider.get(), (KeyguardMediaController) DaggerTvSystemUIRootComponent.this.keyguardMediaControllerProvider.get(), (FalsingManager) DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider.get(), (NotificationLockscreenUserManager) DaggerTvSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider.get(), (NotificationGutsManager) DaggerTvSystemUIRootComponent.this.provideNotificationGutsManagerProvider.get(), (ZenModeController) DaggerTvSystemUIRootComponent.this.zenModeControllerImplProvider.get(), getNotificationSectionsManager(), (ForegroundServiceSectionController) DaggerTvSystemUIRootComponent.this.foregroundServiceSectionControllerProvider.get(), (ForegroundServiceDismissalFeatureController) DaggerTvSystemUIRootComponent.this.foregroundServiceDismissalFeatureControllerProvider.get(), (FeatureFlags) DaggerTvSystemUIRootComponent.this.featureFlagsProvider.get(), (NotifPipeline) DaggerTvSystemUIRootComponent.this.notifPipelineProvider.get(), (NotificationEntryManager) DaggerTvSystemUIRootComponent.this.provideNotificationEntryManagerProvider.get(), (NotifCollection) DaggerTvSystemUIRootComponent.this.notifCollectionProvider.get(), (UiEventLogger) DaggerTvSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public NotificationShelf creatNotificationShelf() {
                return new NotificationShelf(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (KeyguardBypassController) DaggerTvSystemUIRootComponent.this.keyguardBypassControllerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public KeyguardClockSwitch createKeyguardClockSwitch() {
                return new KeyguardClockSwitch(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (StatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (SysuiColorExtractor) DaggerTvSystemUIRootComponent.this.sysuiColorExtractorProvider.get(), (ClockManager) DaggerTvSystemUIRootComponent.this.clockManagerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public KeyguardSliceView createKeyguardSliceView() {
                return new KeyguardSliceView(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (ActivityStarter) DaggerTvSystemUIRootComponent.this.activityStarterDelegateProvider.get(), (ConfigurationController) DaggerTvSystemUIRootComponent.this.provideConfigurationControllerProvider.get(), (TunerService) DaggerTvSystemUIRootComponent.this.tunerServiceImplProvider.get(), DaggerTvSystemUIRootComponent.this.getMainResources());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public KeyguardMessageArea createKeyguardMessageArea() {
                return new KeyguardMessageArea(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (ConfigurationController) DaggerTvSystemUIRootComponent.this.provideConfigurationControllerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QSPanel createQSPanel() {
                return new QSPanel(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (DumpManager) DaggerTvSystemUIRootComponent.this.dumpManagerProvider.get(), (BroadcastDispatcher) DaggerTvSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get(), DaggerTvSystemUIRootComponent.this.getQSLogger(), DaggerTvSystemUIRootComponent.this.getMediaHost(), (UiEventLogger) DaggerTvSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QuickQSPanel createQuickQSPanel() {
                return new QuickQSPanel(InjectionInflationController_ViewAttributeProvider_ProvideContextFactory.proxyProvideContext(this.viewAttributeProvider), InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (DumpManager) DaggerTvSystemUIRootComponent.this.dumpManagerProvider.get(), (BroadcastDispatcher) DaggerTvSystemUIRootComponent.this.providesBroadcastDispatcherProvider.get(), DaggerTvSystemUIRootComponent.this.getQSLogger(), DaggerTvSystemUIRootComponent.this.getMediaHost(), (UiEventLogger) DaggerTvSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
            }

            @Override // com.android.systemui.util.InjectionInflationController.ViewInstanceCreator
            public QSCustomizer createQSCustomizer() {
                return new QSCustomizer(DaggerTvSystemUIRootComponent.this.context, InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory.proxyProvideAttributeSet(this.viewAttributeProvider), (LightBarController) DaggerTvSystemUIRootComponent.this.lightBarControllerProvider.get(), (KeyguardStateController) DaggerTvSystemUIRootComponent.this.keyguardStateControllerImplProvider.get(), (ScreenLifecycle) DaggerTvSystemUIRootComponent.this.screenLifecycleProvider.get(), getTileQueryHelper(), (UiEventLogger) DaggerTvSystemUIRootComponent.this.provideUiEventLoggerProvider.get());
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ExpandableNotificationRowComponentBuilder implements ExpandableNotificationRowComponent.Builder {
        private ExpandableNotificationRow expandableNotificationRow;
        private NotificationEntry notificationEntry;
        private Runnable onDismissRunnable;
        private ExpandableNotificationRow.OnExpandClickListener onExpandClickListener;
        private RowContentBindStage rowContentBindStage;

        private ExpandableNotificationRowComponentBuilder() {
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponent build() {
            if (this.expandableNotificationRow == null) {
                throw new IllegalStateException(ExpandableNotificationRow.class.getCanonicalName() + " must be set");
            } else if (this.notificationEntry == null) {
                throw new IllegalStateException(NotificationEntry.class.getCanonicalName() + " must be set");
            } else if (this.onDismissRunnable == null) {
                throw new IllegalStateException(Runnable.class.getCanonicalName() + " must be set");
            } else if (this.rowContentBindStage == null) {
                throw new IllegalStateException(RowContentBindStage.class.getCanonicalName() + " must be set");
            } else if (this.onExpandClickListener != null) {
                return new ExpandableNotificationRowComponentImpl(this);
            } else {
                throw new IllegalStateException(ExpandableNotificationRow.OnExpandClickListener.class.getCanonicalName() + " must be set");
            }
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder expandableNotificationRow(ExpandableNotificationRow expandableNotificationRow) {
            Preconditions.checkNotNull(expandableNotificationRow);
            this.expandableNotificationRow = expandableNotificationRow;
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder notificationEntry(NotificationEntry notificationEntry) {
            Preconditions.checkNotNull(notificationEntry);
            this.notificationEntry = notificationEntry;
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder onDismissRunnable(Runnable runnable) {
            Preconditions.checkNotNull(runnable);
            this.onDismissRunnable = runnable;
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder rowContentBindStage(RowContentBindStage rowContentBindStage) {
            Preconditions.checkNotNull(rowContentBindStage);
            this.rowContentBindStage = rowContentBindStage;
            return this;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent.Builder
        public ExpandableNotificationRowComponentBuilder onExpandClickListener(ExpandableNotificationRow.OnExpandClickListener onExpandClickListener) {
            Preconditions.checkNotNull(onExpandClickListener);
            this.onExpandClickListener = onExpandClickListener;
            return this;
        }
    }

    private final class ExpandableNotificationRowComponentImpl implements ExpandableNotificationRowComponent {
        private ActivatableNotificationViewController_Factory activatableNotificationViewControllerProvider;
        private Provider<ExpandableNotificationRowController> expandableNotificationRowControllerProvider;
        private Provider<ExpandableNotificationRow> expandableNotificationRowProvider;
        private ExpandableOutlineViewController_Factory expandableOutlineViewControllerProvider;
        private ExpandableViewController_Factory expandableViewControllerProvider;
        private Provider<NotificationEntry> notificationEntryProvider;
        private Provider<Runnable> onDismissRunnableProvider;
        private Provider<ExpandableNotificationRow.OnExpandClickListener> onExpandClickListenerProvider;
        private ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory provideAppNameProvider;
        private ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory provideNotificationKeyProvider;
        private ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory provideStatusBarNotificationProvider;
        private Provider<RowContentBindStage> rowContentBindStageProvider;

        private ExpandableNotificationRowComponentImpl(ExpandableNotificationRowComponentBuilder expandableNotificationRowComponentBuilder) {
            initialize(expandableNotificationRowComponentBuilder);
        }

        private void initialize(ExpandableNotificationRowComponentBuilder expandableNotificationRowComponentBuilder) {
            Factory create = InstanceFactory.create(expandableNotificationRowComponentBuilder.expandableNotificationRow);
            this.expandableNotificationRowProvider = create;
            ExpandableViewController_Factory create2 = ExpandableViewController_Factory.create(create);
            this.expandableViewControllerProvider = create2;
            ExpandableOutlineViewController_Factory create3 = ExpandableOutlineViewController_Factory.create(this.expandableNotificationRowProvider, create2);
            this.expandableOutlineViewControllerProvider = create3;
            this.activatableNotificationViewControllerProvider = ActivatableNotificationViewController_Factory.create(this.expandableNotificationRowProvider, create3, DaggerTvSystemUIRootComponent.this.provideAccessibilityManagerProvider, DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider);
            Factory create4 = InstanceFactory.create(expandableNotificationRowComponentBuilder.notificationEntry);
            this.notificationEntryProvider = create4;
            this.provideStatusBarNotificationProvider = ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideStatusBarNotificationFactory.create(create4);
            this.provideAppNameProvider = ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideAppNameFactory.create(DaggerTvSystemUIRootComponent.this.contextProvider, this.provideStatusBarNotificationProvider);
            this.provideNotificationKeyProvider = ExpandableNotificationRowComponent_ExpandableNotificationRowModule_ProvideNotificationKeyFactory.create(this.provideStatusBarNotificationProvider);
            this.rowContentBindStageProvider = InstanceFactory.create(expandableNotificationRowComponentBuilder.rowContentBindStage);
            this.onExpandClickListenerProvider = InstanceFactory.create(expandableNotificationRowComponentBuilder.onExpandClickListener);
            this.onDismissRunnableProvider = InstanceFactory.create(expandableNotificationRowComponentBuilder.onDismissRunnable);
            this.expandableNotificationRowControllerProvider = DoubleCheck.provider(ExpandableNotificationRowController_Factory.create(this.expandableNotificationRowProvider, this.activatableNotificationViewControllerProvider, DaggerTvSystemUIRootComponent.this.provideNotificationMediaManagerProvider, DaggerTvSystemUIRootComponent.this.providePluginManagerProvider, DaggerTvSystemUIRootComponent.this.bindSystemClockProvider, this.provideAppNameProvider, this.provideNotificationKeyProvider, DaggerTvSystemUIRootComponent.this.keyguardBypassControllerProvider, DaggerTvSystemUIRootComponent.this.notificationGroupManagerProvider, this.rowContentBindStageProvider, DaggerTvSystemUIRootComponent.this.provideNotificationLoggerProvider, DaggerTvSystemUIRootComponent.this.provideHeadsUpManagerPhoneProvider, this.onExpandClickListenerProvider, DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider, DaggerTvSystemUIRootComponent.this.provideNotificationGutsManagerProvider, DaggerTvSystemUIRootComponent.this.provideAllowNotificationLongPressProvider, this.onDismissRunnableProvider, DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider, DaggerTvSystemUIRootComponent.this.peopleNotificationIdentifierImplProvider));
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent
        public ExpandableNotificationRowController getExpandableNotificationRowController() {
            return this.expandableNotificationRowControllerProvider.get();
        }
    }

    /* access modifiers changed from: private */
    public final class StatusBarComponentBuilder implements StatusBarComponent.Builder {
        private NotificationShadeWindowView statusBarWindowView;

        private StatusBarComponentBuilder() {
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent.Builder
        public StatusBarComponent build() {
            if (this.statusBarWindowView != null) {
                return new StatusBarComponentImpl(this);
            }
            throw new IllegalStateException(NotificationShadeWindowView.class.getCanonicalName() + " must be set");
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent.Builder
        public StatusBarComponentBuilder statusBarWindowView(NotificationShadeWindowView notificationShadeWindowView) {
            Preconditions.checkNotNull(notificationShadeWindowView);
            this.statusBarWindowView = notificationShadeWindowView;
            return this;
        }
    }

    private final class StatusBarComponentImpl implements StatusBarComponent {
        private FlingAnimationUtils_Builder_Factory builderProvider;
        private Provider<NotificationPanelView> getNotificationPanelViewProvider;
        private Provider<NotificationPanelViewController> notificationPanelViewControllerProvider;
        private NotificationShadeWindowView statusBarWindowView;
        private Provider<NotificationShadeWindowView> statusBarWindowViewProvider;

        private StatusBarComponentImpl(StatusBarComponentBuilder statusBarComponentBuilder) {
            initialize(statusBarComponentBuilder);
        }

        private void initialize(StatusBarComponentBuilder statusBarComponentBuilder) {
            this.statusBarWindowView = statusBarComponentBuilder.statusBarWindowView;
            Factory create = InstanceFactory.create(statusBarComponentBuilder.statusBarWindowView);
            this.statusBarWindowViewProvider = create;
            this.getNotificationPanelViewProvider = DoubleCheck.provider(StatusBarViewModule_GetNotificationPanelViewFactory.create(create));
            this.builderProvider = FlingAnimationUtils_Builder_Factory.create(DaggerTvSystemUIRootComponent.this.provideDisplayMetricsProvider);
            this.notificationPanelViewControllerProvider = DoubleCheck.provider(NotificationPanelViewController_Factory.create(this.getNotificationPanelViewProvider, DaggerTvSystemUIRootComponent.this.injectionInflationControllerProvider, DaggerTvSystemUIRootComponent.this.notificationWakeUpCoordinatorProvider, DaggerTvSystemUIRootComponent.this.pulseExpansionHandlerProvider, DaggerTvSystemUIRootComponent.this.dynamicPrivacyControllerProvider, DaggerTvSystemUIRootComponent.this.keyguardBypassControllerProvider, DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider, DaggerTvSystemUIRootComponent.this.shadeControllerImplProvider, DaggerTvSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider, DaggerTvSystemUIRootComponent.this.provideNotificationEntryManagerProvider, DaggerTvSystemUIRootComponent.this.keyguardStateControllerImplProvider, DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider, DaggerTvSystemUIRootComponent.this.dozeLogProvider, DaggerTvSystemUIRootComponent.this.dozeParametersProvider, DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider, DaggerTvSystemUIRootComponent.this.vibratorHelperProvider, DaggerTvSystemUIRootComponent.this.provideLatencyTrackerProvider, DaggerTvSystemUIRootComponent.this.providePowerManagerProvider, DaggerTvSystemUIRootComponent.this.provideAccessibilityManagerProvider, DaggerTvSystemUIRootComponent.this.provideDisplayIdProvider, DaggerTvSystemUIRootComponent.this.keyguardUpdateMonitorProvider, DaggerTvSystemUIRootComponent.this.provideMetricsLoggerProvider, DaggerTvSystemUIRootComponent.this.provideActivityManagerProvider, DaggerTvSystemUIRootComponent.this.zenModeControllerImplProvider, DaggerTvSystemUIRootComponent.this.provideConfigurationControllerProvider, this.builderProvider, DaggerTvSystemUIRootComponent.this.statusBarTouchableRegionManagerProvider, DaggerTvSystemUIRootComponent.this.conversationNotificationManagerProvider, DaggerTvSystemUIRootComponent.this.mediaHierarchyManagerProvider, DaggerTvSystemUIRootComponent.this.biometricUnlockControllerProvider, DaggerTvSystemUIRootComponent.this.statusBarKeyguardViewManagerProvider));
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent
        public NotificationShadeWindowViewController getNotificationShadeWindowViewController() {
            return new NotificationShadeWindowViewController((InjectionInflationController) DaggerTvSystemUIRootComponent.this.injectionInflationControllerProvider.get(), (NotificationWakeUpCoordinator) DaggerTvSystemUIRootComponent.this.notificationWakeUpCoordinatorProvider.get(), (PulseExpansionHandler) DaggerTvSystemUIRootComponent.this.pulseExpansionHandlerProvider.get(), (DynamicPrivacyController) DaggerTvSystemUIRootComponent.this.dynamicPrivacyControllerProvider.get(), (KeyguardBypassController) DaggerTvSystemUIRootComponent.this.keyguardBypassControllerProvider.get(), (FalsingManager) DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider.get(), (PluginManager) DaggerTvSystemUIRootComponent.this.providePluginManagerProvider.get(), (TunerService) DaggerTvSystemUIRootComponent.this.tunerServiceImplProvider.get(), (NotificationLockscreenUserManager) DaggerTvSystemUIRootComponent.this.notificationLockscreenUserManagerImplProvider.get(), (NotificationEntryManager) DaggerTvSystemUIRootComponent.this.provideNotificationEntryManagerProvider.get(), (KeyguardStateController) DaggerTvSystemUIRootComponent.this.keyguardStateControllerImplProvider.get(), (SysuiStatusBarStateController) DaggerTvSystemUIRootComponent.this.statusBarStateControllerImplProvider.get(), (DozeLog) DaggerTvSystemUIRootComponent.this.dozeLogProvider.get(), (DozeParameters) DaggerTvSystemUIRootComponent.this.dozeParametersProvider.get(), (CommandQueue) DaggerTvSystemUIRootComponent.this.provideCommandQueueProvider.get(), (ShadeController) DaggerTvSystemUIRootComponent.this.shadeControllerImplProvider.get(), (DockManager) DaggerTvSystemUIRootComponent.this.dockManagerImplProvider.get(), (NotificationShadeDepthController) DaggerTvSystemUIRootComponent.this.notificationShadeDepthControllerProvider.get(), this.statusBarWindowView, this.notificationPanelViewControllerProvider.get(), (SuperStatusBarViewFactory) DaggerTvSystemUIRootComponent.this.superStatusBarViewFactoryProvider.get());
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent
        public StatusBarWindowController getStatusBarWindowController() {
            return (StatusBarWindowController) DaggerTvSystemUIRootComponent.this.statusBarWindowControllerProvider.get();
        }

        @Override // com.android.systemui.statusbar.phone.dagger.StatusBarComponent
        public NotificationPanelViewController getNotificationPanelViewController() {
            return this.notificationPanelViewControllerProvider.get();
        }
    }

    /* access modifiers changed from: private */
    public final class NotificationRowComponentBuilder implements NotificationRowComponent.Builder {
        private ActivatableNotificationView activatableNotificationView;

        private NotificationRowComponentBuilder() {
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent.Builder
        public NotificationRowComponent build() {
            if (this.activatableNotificationView != null) {
                return new NotificationRowComponentImpl(this);
            }
            throw new IllegalStateException(ActivatableNotificationView.class.getCanonicalName() + " must be set");
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent.Builder
        public NotificationRowComponentBuilder activatableNotificationView(ActivatableNotificationView activatableNotificationView) {
            Preconditions.checkNotNull(activatableNotificationView);
            this.activatableNotificationView = activatableNotificationView;
            return this;
        }
    }

    private final class NotificationRowComponentImpl implements NotificationRowComponent {
        private ActivatableNotificationView activatableNotificationView;

        private NotificationRowComponentImpl(NotificationRowComponentBuilder notificationRowComponentBuilder) {
            initialize(notificationRowComponentBuilder);
        }

        private ExpandableViewController getExpandableViewController() {
            return new ExpandableViewController(this.activatableNotificationView);
        }

        private ExpandableOutlineViewController getExpandableOutlineViewController() {
            return new ExpandableOutlineViewController(this.activatableNotificationView, getExpandableViewController());
        }

        private void initialize(NotificationRowComponentBuilder notificationRowComponentBuilder) {
            this.activatableNotificationView = notificationRowComponentBuilder.activatableNotificationView;
        }

        @Override // com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent
        public ActivatableNotificationViewController getActivatableNotificationViewController() {
            return new ActivatableNotificationViewController(this.activatableNotificationView, getExpandableOutlineViewController(), (AccessibilityManager) DaggerTvSystemUIRootComponent.this.provideAccessibilityManagerProvider.get(), (FalsingManager) DaggerTvSystemUIRootComponent.this.falsingManagerProxyProvider.get());
        }
    }

    /* access modifiers changed from: private */
    public final class TvPipComponentBuilder implements TvPipComponent.Builder {
        private PipControlsView pipControlsView;

        private TvPipComponentBuilder() {
        }

        @Override // com.android.systemui.pip.tv.dagger.TvPipComponent.Builder
        public TvPipComponent build() {
            if (this.pipControlsView != null) {
                return new TvPipComponentImpl(this);
            }
            throw new IllegalStateException(PipControlsView.class.getCanonicalName() + " must be set");
        }

        @Override // com.android.systemui.pip.tv.dagger.TvPipComponent.Builder
        public TvPipComponentBuilder pipControlsView(PipControlsView pipControlsView) {
            Preconditions.checkNotNull(pipControlsView);
            this.pipControlsView = pipControlsView;
            return this;
        }
    }

    private final class TvPipComponentImpl implements TvPipComponent {
        private PipControlsView pipControlsView;

        private TvPipComponentImpl(TvPipComponentBuilder tvPipComponentBuilder) {
            initialize(tvPipComponentBuilder);
        }

        private void initialize(TvPipComponentBuilder tvPipComponentBuilder) {
            this.pipControlsView = tvPipComponentBuilder.pipControlsView;
        }

        @Override // com.android.systemui.pip.tv.dagger.TvPipComponent
        public PipControlsViewController getPipControlsViewController() {
            return new PipControlsViewController(this.pipControlsView, (PipManager) DaggerTvSystemUIRootComponent.this.pipManagerProvider.get(), (LayoutInflater) DaggerTvSystemUIRootComponent.this.providerLayoutInflaterProvider.get(), DaggerTvSystemUIRootComponent.this.getMainHandler());
        }
    }
}
