package com.oneplus.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.telecom.TelecomManager;
import android.util.Log;
import android.util.OpFeatures;
import android.view.Display;
import android.view.MotionEvent;
import android.view.RemoteAnimationAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.android.internal.view.AppearanceRegion;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.model.SysUiState;
import com.android.systemui.qs.QuickQSPanel;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.NotificationViewHierarchyManager;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.DozeScrimController;
import com.android.systemui.statusbar.phone.DozeServiceHost;
import com.android.systemui.statusbar.phone.EdgeBackGestureHandler;
import com.android.systemui.statusbar.phone.HeadsUpAppearanceController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.NavigationBarFragment;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ScrimState;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.util.NotificationChannels;
import com.android.systemui.util.ProductUtils;
import com.android.systemui.util.Utils;
import com.oneplus.airplane.AirplanePopupMonitor;
import com.oneplus.anim.OpCameraAnimateController;
import com.oneplus.aod.OpAodDisplayViewManager;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.aod.OpAodWindowManager;
import com.oneplus.aod.utils.OpCanvasAodHelper;
import com.oneplus.battery.OpChargingAnimationController;
import com.oneplus.faceunlock.OpFacelockController;
import com.oneplus.networkspeed.NetworkSpeedController;
import com.oneplus.notification.OpNotificationController;
import com.oneplus.onlineconfig.OpSystemUIGestureOnlineConfig;
import com.oneplus.opzenmode.OpZenModeController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.scene.OpSceneModeObserver;
import com.oneplus.systemui.keyguard.OpKeyguardUnlockCounter;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintController;
import com.oneplus.util.OpBoostUtils;
import com.oneplus.util.OpReflectionUtils;
import com.oneplus.util.OpReverseWirelessChargeUtils;
import com.oneplus.util.OpUtils;
import com.oneplus.util.SystemSetting;
import com.oneplus.util.ThemeColorUtils;
import com.oneplus.util.VibratorSceneUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
public class OpStatusBar extends SystemUI implements OpHighlightHintController.OnHighlightHintStateChangeListener, OpZenModeController.Callback {
    public static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private static BroadcastReceiver sClearDataReceiver = null;
    private static BroadcastReceiver sPkgReceiver = null;
    private boolean isCTSStart = false;
    private int mAccentColor;
    private final Intent mAlexaIntent = new Intent();
    protected OpAodDisplayViewManager mAodDisplayViewManager;
    protected OpAodWindowManager mAodWindowManager;
    protected int mBackDisposition;
    private boolean mBouncerScrimmedBootDone;
    private OpCameraAnimateController mCameraAnim;
    private CarModeScreenshotReceiver mCarModeReceiver;
    private final Runnable mCheckIMENavBarTask = new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpStatusBar$xcu8lQa_Z5-UzFgWs_GWgbGv5bo
        @Override // java.lang.Runnable
        public final void run() {
            OpStatusBar.this.lambda$new$2$OpStatusBar();
        }
    };
    private final Runnable mCheckNavigationBarTask = new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpStatusBar$b4K8-DVbezRw2CdKDCQbZqxi8NU
        @Override // java.lang.Runnable
        public final void run() {
            OpStatusBar.this.lambda$new$0$OpStatusBar();
        }
    };
    protected boolean mCustomDozing;
    private Notification.Builder mDemoCarModeHighlightHint = null;
    private Notification.Builder mDemoHighlightHint = null;
    private int mDisableQs = 0;
    public ContentObserver mDisableQsObserver;
    private int mDoubleTapPowerApp = 0;
    protected ContentObserver mDoubleTapPowerObserver;
    private ContentObserver mFullScreenGestureObserver;
    private OpSystemUIGestureOnlineConfig mGestureOnlineConfig;
    private boolean mGoogleDarkTheme;
    private boolean mHideImeBackAndSwitcher = false;
    protected int mImeDisplayId;
    private WindowManager.LayoutParams mImeNavLp;
    private boolean mImeShow = false;
    protected boolean mImeStateChange = false;
    protected int mImeVisibleState;
    private boolean mIsInMultiWindow = false;
    private boolean mLastExpand = true;
    private long mLastUpdateIMENavBarTime = 0;
    private long mLastUpdateNavBarTime = 0;
    private WindowManager.LayoutParams mNavLp;
    private boolean mNavShowing = true;
    private int mNavType = 0;
    private boolean mNeedShowOTAWizard = false;
    NetworkSpeedController mNetworkSpeedController;
    protected RelativeLayout mOPAodWindow;
    private OpBoostUtils mOpBoostUtils;
    private boolean mOpDozingRequested;
    private OpEdgeBackGestureHandler mOpEdgeBackGestureHandler;
    protected OpFacelockController mOpFacelockController;
    private OpGestureButtonViewController mOpGestureButtonViewController;
    protected OpNotificationController mOpNotificationController;
    private OpOneHandModeController mOpOneHandModeController = null;
    protected OpSceneModeObserver mOpSceneModeObserver;
    protected OpWakingUpScrimController mOpWakingUpScrimController;
    protected int mOrientation;
    private ContentObserver mOtaWizardObserver;
    private final PluginManager mPluginManager = ((PluginManager) Dependency.get(PluginManager.class));
    private PackageManager mPm;
    private boolean mQsDisabled = false;
    protected BrightnessMirrorController mQuickBrightnessMirrorController;
    protected QuickQSPanel mQuickQSPanel;
    private int mRotation;
    protected boolean mShowImeSwitcher;
    private boolean mSpecialTheme;
    protected boolean mStartDozingRequested;
    protected StatusBarCollapseListener mStatusBarCollapseListener;
    private final SysUiState mSysUiFlagContainer = ((SysUiState) Dependency.get(SysUiState.class));
    protected TelecomManager mTelecomManager;
    private int mThemeColor;
    private SystemSetting mThemeSetting;
    protected IBinder mToken;
    protected boolean mWakingUpAnimationStart = false;
    private WindowManager mWindowManager;

    public interface StatusBarCollapseListener {
        void statusBarCollapse();
    }

    private boolean isBrightnessMirrorVisible() {
        return false;
    }

    /* access modifiers changed from: protected */
    public ScrimController.Callback getUnlockScrimCallback() {
        return null;
    }

    public boolean isAppFullScreen() {
        return false;
    }

    public boolean isCameraNotchIgnoreSetting() {
        return false;
    }

    public void onWallpaperChange(Bitmap bitmap) {
    }

    protected OpStatusBar(Context context) {
        super(context);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        ThemeColorUtils.init(this.mContext);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        AnonymousClass1 r0 = new ContentObserver(getHandler()) { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.1
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                boolean z2 = false;
                if (Settings.Global.getInt(((SystemUI) OpStatusBar.this).mContext.getContentResolver(), "oneplus_need_show_ota_wizard", 0) == 1) {
                    z2 = true;
                }
                Log.d("OpStatusBar", "needShowOTAWizard: " + OpStatusBar.this.mNeedShowOTAWizard + " to " + z2);
                if (OpStatusBar.this.mNeedShowOTAWizard != z2) {
                    OpStatusBar.this.mNeedShowOTAWizard = z2;
                    if (OpStatusBar.this.getNotificationPanelView() != null) {
                        OpStatusBar.this.getNotificationPanelView().setShowOTAWizard(z2);
                    }
                }
            }
        };
        this.mOtaWizardObserver = r0;
        r0.onChange(true);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("oneplus_need_show_ota_wizard"), true, this.mOtaWizardObserver, -1);
        this.mAlexaIntent.setAction("amazon.intent.action.ALEXA_LISTEN_PUSHBUTTON");
        this.mAlexaIntent.setPackage("com.amazon.dee.app");
        this.mPm = this.mContext.getPackageManager();
        this.mFullScreenGestureObserver = new ContentObserver(getHandler()) { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.2
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                int i = 0;
                int intForUser = Settings.System.getIntForUser(((SystemUI) OpStatusBar.this).mContext.getContentResolver(), "oneplus_fullscreen_gesture_type", 0, -2);
                Log.d("OpStatusBar", "gesture type " + OpStatusBar.this.mNavType + " to " + intForUser);
                OpStatusBar.this.mNavType = intForUser;
                boolean z2 = Settings.System.getIntForUser(((SystemUI) OpStatusBar.this).mContext.getContentResolver(), "op_gesture_button_side_enabled", 1, -2) != 0;
                if (EdgeBackGestureHandler.sSideGestureEnabled != z2) {
                    Log.d("OpStatusBar", "gesture side to " + z2);
                    EdgeBackGestureHandler.sSideGestureEnabled = z2;
                }
                boolean z3 = Settings.Secure.getIntForUser(((SystemUI) OpStatusBar.this).mContext.getContentResolver(), "nav_icon_hide", 0, -2) != 0;
                Log.d("OpStatusBar", "hideImeBackAndSwitcher=" + OpStatusBar.this.mHideImeBackAndSwitcher + "->" + z3);
                OpStatusBar.this.mHideImeBackAndSwitcher = z3;
                int navBarMode = ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).getNavBarMode();
                if ((OpStatusBar.this.mNavType == 1 || !EdgeBackGestureHandler.sSideGestureEnabled) && QuickStepContract.isGesturalMode(navBarMode)) {
                    i = 1;
                }
                Settings.System.putInt(((SystemUI) OpStatusBar.this).mContext.getContentResolver(), "buttons_show_on_screen_navkeys", i ^ 1);
                OpStatusBar.this.checkNavigationBarState();
            }
        };
        Log.d("OpStatusBar", "FullScreenGestureObserver internal onChange");
        this.mFullScreenGestureObserver.onChange(true);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("oneplus_fullscreen_gesture_type"), true, this.mFullScreenGestureObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("op_gesture_button_side_enabled"), true, this.mFullScreenGestureObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("nav_icon_hide"), true, this.mFullScreenGestureObserver, -1);
        AnonymousClass3 r2 = new ContentObserver(getHandler()) { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.3
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                int intForUser = Settings.Secure.getIntForUser(((SystemUI) OpStatusBar.this).mContext.getContentResolver(), "op_app_double_tap_power_gesture", 0, -2);
                Log.i("OpStatusBar", "DoubleTapPower: app=" + intForUser);
                OpStatusBar.this.mDoubleTapPowerApp = intForUser;
            }
        };
        this.mDoubleTapPowerObserver = r2;
        r2.onChange(true);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("op_app_double_tap_power_gesture"), true, this.mDoubleTapPowerObserver, -1);
        AnonymousClass4 r22 = new ContentObserver(getHandler()) { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.4
            @Override // android.database.ContentObserver
            public void onChange(boolean z) {
                ContentResolver contentResolver = ((SystemUI) OpStatusBar.this).mContext.getContentResolver();
                OpStatusBar.this.getKeyguardUpdateMonitor();
                int intForUser = Settings.Secure.getIntForUser(contentResolver, "oneplus_disable_qs_when_locked", 0, KeyguardUpdateMonitor.getCurrentUser());
                StringBuilder sb = new StringBuilder();
                sb.append("disable qs:");
                sb.append(intForUser);
                sb.append(", ");
                OpStatusBar.this.getKeyguardUpdateMonitor();
                sb.append(KeyguardUpdateMonitor.getCurrentUser());
                Log.d("OpStatusBar", sb.toString());
                OpStatusBar.this.mDisableQs = intForUser;
                OpStatusBar.this.updateQsEnabled();
            }
        };
        this.mDisableQsObserver = r22;
        r22.onChange(true);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("oneplus_disable_qs_when_locked"), true, this.mDisableQsObserver, -1);
        ((OpZenModeController) Dependency.get(OpZenModeController.class)).addCallback(this);
        this.mThemeColor = OpUtils.getThemeColor(this.mContext);
        this.mAccentColor = OpUtils.getThemeAccentColor(this.mContext, C0004R$color.qs_tile_icon);
        ThemeColorUtils.init(this.mContext);
        this.mSpecialTheme = OpUtils.isSpecialTheme(this.mContext);
        this.mGoogleDarkTheme = OpUtils.isGoogleDarkTheme(this.mContext);
        this.mOpSceneModeObserver = (OpSceneModeObserver) Dependency.get(OpSceneModeObserver.class);
        OpUtils.init(this.mContext);
        SystemSetting systemSetting = this.mThemeSetting;
        if (systemSetting != null) {
            systemSetting.setListening(false);
        }
        AnonymousClass5 r23 = new SystemSetting(this.mContext, null, "oem_black_mode", true) { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.5
            /* access modifiers changed from: protected */
            @Override // com.oneplus.util.SystemSetting
            public void handleValueChanged(int i, boolean z) {
                OpStatusBar.this.getHandler().post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.5.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (OpStatusBar.DEBUG) {
                            Log.d("OpStatusBar", "theme setting changed.");
                        }
                        OpStatusBar.this.checkIfThemeChanged();
                    }
                });
            }
        };
        this.mThemeSetting = r23;
        r23.setListening(true);
        this.mOpWakingUpScrimController = new OpWakingUpScrimController(this.mContext);
        initDetectPkgReceiver();
        initClearDataReceiver();
        OpReverseWirelessChargeUtils.init(this.mContext, getHandler());
        if (this.mNavLp == null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2019, 545521768, -3);
            this.mNavLp = layoutParams;
            layoutParams.token = new Binder();
            this.mNavLp.setTitle("NavigationBar" + this.mContext.getDisplayId());
            this.mNavLp.accessibilityTitle = this.mContext.getString(C0015R$string.nav_bar);
            WindowManager.LayoutParams layoutParams2 = this.mNavLp;
            layoutParams2.windowAnimations = 0;
            layoutParams2.privateFlags |= 16777216;
        }
        if (this.mImeNavLp == null) {
            WindowManager.LayoutParams layoutParams3 = new WindowManager.LayoutParams(-1, this.mContext.getResources().getDimensionPixelSize(17105324), 2017, 545522024, -3);
            this.mImeNavLp = layoutParams3;
            layoutParams3.token = new Binder();
            this.mImeNavLp.setTitle("NavigationBar" + this.mContext.getDisplayId());
            this.mImeNavLp.accessibilityTitle = this.mContext.getString(C0015R$string.nav_bar);
            WindowManager.LayoutParams layoutParams4 = this.mImeNavLp;
            layoutParams4.windowAnimations = 0;
            layoutParams4.gravity = 80;
            layoutParams4.privateFlags = 16777216 | layoutParams4.privateFlags;
        }
        this.mTelecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        this.mOpGestureButtonViewController = new OpGestureButtonViewController(this.mContext);
        if (OpFeatures.isSupport(new int[]{221})) {
            this.mOpOneHandModeController = new OpOneHandModeController(this.mContext);
        }
        this.mOpBoostUtils = new OpBoostUtils();
        if (ProductUtils.isUsvMode()) {
            AirplanePopupMonitor.getInstance(this.mContext).init();
        } else {
            Log.d("OpStatusBar", "non usv");
        }
        OpSystemUIGestureOnlineConfig.getInstance().init(this.mContext);
        this.mGestureOnlineConfig = OpSystemUIGestureOnlineConfig.getInstance();
    }

    private void initDetectPkgReceiver() {
        if (sPkgReceiver == null) {
            sPkgReceiver = new BroadcastReceiver() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.6
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    boolean z;
                    try {
                        String action = intent.getAction();
                        context.getContentResolver();
                        Uri data = intent.getData();
                        boolean z2 = true;
                        if (data != null) {
                            String schemeSpecificPart = data.getSchemeSpecificPart();
                            if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                                if ("com.android.compatibility.common.deviceinfo".equals(schemeSpecificPart)) {
                                    OpStatusBar.this.isCTSStart = true;
                                    OpUtils.setCTSAdded(true);
                                    Log.i("OpStatusBar", "isSpecial case start");
                                }
                            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                                if ("com.android.tradefed.utils.wifi".equals(schemeSpecificPart)) {
                                    OpStatusBar.this.isCTSStart = false;
                                    OpUtils.setCTSAdded(false);
                                    Log.i("OpStatusBar", "isSpecial case end");
                                } else if ("com.amazon.dee.app".equals(schemeSpecificPart)) {
                                    boolean booleanExtra = intent.getBooleanExtra("android.intent.extra.REPLACING", false);
                                    if (OpUtils.DEBUG_ONEPLUS) {
                                        Log.i("OpStatusBar", "ALEXA_PKG removed, dbValue:" + Settings.Secure.getIntForUser(((SystemUI) OpStatusBar.this).mContext.getContentResolver(), "op_app_double_tap_power_gesture", 0, -2));
                                    }
                                    Log.i("OpStatusBar", "ALEXA_PKG removed, OpUtils.DoubleTap:" + OpUtils.isSupportDoubleTapAlexa() + ", mDTPApp:" + OpStatusBar.this.mDoubleTapPowerApp + ", isReplacing:" + booleanExtra);
                                    if (OpUtils.isSupportDoubleTapAlexa() && OpStatusBar.this.mDoubleTapPowerApp == 2 && !booleanExtra) {
                                        Settings.Secure.putIntForUser(((SystemUI) OpStatusBar.this).mContext.getContentResolver(), "op_app_double_tap_power_gesture", 0, -2);
                                        if (OpStatusBar.this.mDoubleTapPowerObserver != null) {
                                            OpStatusBar.this.mDoubleTapPowerObserver.onChange(true);
                                        }
                                    }
                                }
                            }
                        }
                        if (OpUtils.isSupportDoubleTapAlexa() && "android.intent.action.PACKAGE_CHANGED".equals(action)) {
                            String[] stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.changed_component_name_list");
                            if (stringArrayExtra != null) {
                                int length = stringArrayExtra.length;
                                int i = 0;
                                while (true) {
                                    if (i >= length) {
                                        break;
                                    } else if ("com.amazon.alexa.handsfree.settings.quicksettings.AlexaQuickSettingService".equals(stringArrayExtra[i])) {
                                        int componentEnabledSetting = OpStatusBar.this.mPm.getComponentEnabledSetting(new ComponentName("com.amazon.dee.app", "com.amazon.alexa.handsfree.settings.quicksettings.AlexaQuickSettingService"));
                                        Log.d("OpStatusBar", "alexa cmp changed: " + componentEnabledSetting);
                                        if (componentEnabledSetting == 1) {
                                            z = true;
                                        }
                                    } else {
                                        i++;
                                    }
                                }
                            }
                            z = false;
                            if (z) {
                                Intent intent2 = new Intent("com.oneplus.systemui.qs.hide_tile");
                                intent2.putExtra("tile", "custom(com.amazon.dee.app/com.amazon.alexa.handsfree.settings.quicksettings.AlexaQuickSettingService)");
                                intent2.putExtra("hide", false);
                                int i2 = 12;
                                if (Settings.System.getIntForUser(((SystemUI) OpStatusBar.this).mContext.getContentResolver(), "qs_less_rows", 0, -2) == 0 && !Utils.useQsMediaPlayer(((SystemUI) OpStatusBar.this).mContext)) {
                                    z2 = false;
                                }
                                if (z2) {
                                    i2 = 8;
                                }
                                if (OpUtils.getSimCount() >= 2) {
                                    i2--;
                                }
                                intent2.putExtra("position", i2);
                                Log.d("OpStatusBar", "add alexa tile, " + i2 + ", less:" + z2);
                                ((SystemUI) OpStatusBar.this).mContext.sendBroadcast(intent2);
                            }
                        }
                    } catch (Exception e) {
                        Log.w("OpStatusBar", "sPkgReceiver error.", e);
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.addDataScheme("package");
            intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
            this.mContext.registerReceiverAsUser(sPkgReceiver, UserHandle.ALL, intentFilter, null, null);
        }
    }

    private void initClearDataReceiver() {
        if (sClearDataReceiver == null) {
            sClearDataReceiver = new BroadcastReceiver() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.7
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    Log.d("OpStatusBar", "get clear data");
                    NotificationChannels.createAll(((SystemUI) OpStatusBar.this).mContext);
                }
            };
            this.mContext.registerReceiverAsUser(sClearDataReceiver, UserHandle.ALL, new IntentFilter("com.oneplus.intent.ACTION_CLEAR_NOTIFICATION_DATA"), null, null);
        }
    }

    public boolean isDoubleTapCamera() {
        return this.mDoubleTapPowerApp == 0;
    }

    public Intent getDoubleTapPowerOpAppIntent(int i) {
        int i2;
        Intent intent;
        Intent intent2 = null;
        if (i == 1 && (i2 = this.mDoubleTapPowerApp) != 0) {
            if (i2 == 1) {
                intent = new Intent("coloros.wallet.intent.action.OPEN");
                intent.setFlags(268468224);
            } else {
                if (i2 == 2 && OpUtils.isPackageInstalledAsUser(this.mContext, "com.amazon.dee.app", 0, KeyguardUpdateMonitor.getCurrentUser()) && OpUtils.isSupportDoubleTapAlexa()) {
                    Log.d("OpStatusBar", "launch alexa");
                    intent = this.mAlexaIntent;
                    if (intent.resolveActivity(this.mPm) == null && this.mPm != null) {
                        Log.i("OpStatusBar", "getLaunchIntent of alexa");
                        intent = this.mPm.getLaunchIntentForPackage("com.amazon.dee.app");
                    }
                    if (intent != null) {
                        intent.putExtra("isAlexa", true);
                    }
                }
                Log.i("OpStatusBar", "DoubleTapPower: getDoubleTapPowerOpAppIntent " + this.mDoubleTapPowerApp + ", " + intent2);
            }
            intent2 = intent;
            Log.i("OpStatusBar", "DoubleTapPower: getDoubleTapPowerOpAppIntent " + this.mDoubleTapPowerApp + ", " + intent2);
        }
        return intent2;
    }

    /* access modifiers changed from: protected */
    public void makeStatusBarView(Context context) {
        this.mNetworkSpeedController = (NetworkSpeedController) Dependency.get(NetworkSpeedController.class);
        getNetworkController().setNetworkSpeedController(this.mNetworkSpeedController);
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
        setUpHighlightHintInfo();
        OpCameraAnimateController opCameraAnimateController = new OpCameraAnimateController(this.mContext);
        this.mCameraAnim = opCameraAnimateController;
        opCameraAnimateController.init();
    }

    public int getNavigationBarHiddenMode() {
        return this.mNavType;
    }

    public boolean isHideImeBackAndSwitcher() {
        return this.mHideImeBackAndSwitcher;
    }

    public void checkNavigationBarState() {
        long uptimeMillis = SystemClock.uptimeMillis();
        long j = uptimeMillis - this.mLastUpdateNavBarTime;
        Log.d("OpStatusBar", "updateNavigationBar: now=" + uptimeMillis + ", last=" + this.mLastUpdateNavBarTime);
        if (getHandler().hasCallbacks(this.mCheckNavigationBarTask)) {
            Log.i("OpStatusBar", "checkNavigationBarState: already scheduled, skip.");
        } else if (j < 1000) {
            getHandler().postAtTime(this.mCheckNavigationBarTask, this.mLastUpdateNavBarTime + 1000);
        } else {
            getHandler().post(this.mCheckNavigationBarTask);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$OpStatusBar() {
        int navBarMode = ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).getNavBarMode();
        ((NavigationBarController) Dependency.get(NavigationBarController.class)).getDefaultNavigationBarFragment();
        View navView = ((NavigationBarController) Dependency.get(NavigationBarController.class)).getNavView();
        boolean z = navView != null && navView.isAttachedToWindow();
        StringBuilder sb = new StringBuilder();
        sb.append("checkNavBarState mode: ");
        sb.append(navBarMode);
        sb.append(", type: ");
        sb.append(this.mNavType);
        sb.append(", showing: ");
        sb.append(this.mNavShowing);
        sb.append(", attached:");
        sb.append(z);
        sb.append(", view:");
        sb.append(navView != null);
        sb.append(", ImeShow: ");
        sb.append(this.mImeShow);
        Log.d("OpStatusBar", sb.toString());
        boolean z2 = this.mNavShowing;
        if ((this.mNavType == 1 || !EdgeBackGestureHandler.sSideGestureEnabled) && QuickStepContract.isGesturalMode(navBarMode)) {
            if (z && this.mNavShowing) {
                Log.d("OpStatusBar", "NavBar remove it");
                this.mWindowManager.removeViewImmediate(navView);
                this.mNavShowing = false;
            }
            enableGestureHandler();
        } else {
            if (navView != null && !navView.isAttachedToWindow() && !this.mNavShowing) {
                Log.d("OpStatusBar", "NavBar add it");
                WindowManager.LayoutParams layoutParams = this.mNavLp;
                if (layoutParams == null) {
                    layoutParams = (WindowManager.LayoutParams) navView.getLayoutParams();
                }
                if (QuickStepContract.isGesturalMode(navBarMode)) {
                    int i = layoutParams.flags & -65;
                    layoutParams.flags = i;
                    layoutParams.flags = i | 16;
                } else {
                    int i2 = layoutParams.flags & -17;
                    layoutParams.flags = i2;
                    layoutParams.flags = i2 | 64;
                }
                this.mWindowManager.addView(navView, layoutParams);
                this.mNavShowing = true;
            } else if (navView != null && navView.getLayoutParams() == this.mImeNavLp) {
                Log.d("OpStatusBar", "NavBar update window type to navigation bar");
                WindowManager.LayoutParams layoutParams2 = this.mNavLp;
                if (layoutParams2 == null) {
                    layoutParams2 = (WindowManager.LayoutParams) navView.getLayoutParams();
                }
                this.mWindowManager.removeViewImmediate(navView);
                this.mWindowManager.addView(navView, layoutParams2);
            }
            disableGestureHandler();
            updateImeWindowStatus();
        }
        if (!(z2 == this.mNavShowing || getStatusBarKeyguardViewManager() == null)) {
            getStatusBarKeyguardViewManager().onHideNavBar(!this.mNavShowing);
        }
        this.mLastUpdateNavBarTime = SystemClock.uptimeMillis();
    }

    public boolean checkGestureStartAssist(Bundle bundle) {
        if (this.mNavShowing) {
            return false;
        }
        getHandler().post(new Runnable(bundle) { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpStatusBar$pxcFa-Vpz_eOL3Jjz1zwym3ILn0
            public final /* synthetic */ Bundle f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ((AssistManager) Dependency.get(AssistManager.class)).startAssist(this.f$0);
            }
        });
        Log.d("OpStatusBar", "startAssist");
        return true;
    }

    public void checkIMENavBarState() {
        long uptimeMillis = SystemClock.uptimeMillis();
        long j = uptimeMillis - this.mLastUpdateIMENavBarTime;
        Log.d("OpStatusBar", "updateIMENavBar: now=" + uptimeMillis + ", last=" + this.mLastUpdateIMENavBarTime);
        if (getHandler().hasCallbacks(this.mCheckIMENavBarTask)) {
            Log.i("OpStatusBar", "checkIMENavBarState: already scheduled, skip.");
        } else if (j < 1000) {
            getHandler().postAtTime(this.mCheckIMENavBarTask, this.mLastUpdateIMENavBarTime + 1000);
        } else {
            getHandler().post(this.mCheckIMENavBarTask);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$OpStatusBar() {
        int navBarMode = ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).getNavBarMode();
        View navView = ((NavigationBarController) Dependency.get(NavigationBarController.class)).getNavView();
        boolean z = navView != null && navView.isAttachedToWindow();
        StringBuilder sb = new StringBuilder();
        sb.append("checkIMENavBarState mode: ");
        sb.append(navBarMode);
        sb.append(", type: ");
        sb.append(this.mNavType);
        sb.append(", showing: ");
        sb.append(this.mNavShowing);
        sb.append(", attached:");
        sb.append(z);
        sb.append(", view:");
        sb.append(navView != null);
        sb.append(", ImeShow: ");
        sb.append(this.mImeShow);
        Log.d("OpStatusBar", sb.toString());
        boolean z2 = this.mNavShowing;
        if ((this.mNavType == 1 || !EdgeBackGestureHandler.sSideGestureEnabled) && QuickStepContract.isGesturalMode(navBarMode)) {
            if (!this.mImeShow) {
                if (z && this.mNavShowing) {
                    Log.d("OpStatusBar", "IME hide - NavBar remove it");
                    this.mWindowManager.removeViewImmediate(navView);
                    this.mNavShowing = false;
                }
                enableGestureHandler();
                updateImeWindowStatus();
            } else if (this.mOrientation == 1) {
                if (navView != null && !navView.isAttachedToWindow() && !this.mNavShowing) {
                    Log.d("OpStatusBar", "IME show - NavBar add it");
                    this.mWindowManager.addView(navView, this.mImeNavLp);
                    this.mNavShowing = true;
                }
                disableGestureHandler();
            } else {
                if (z && this.mNavShowing) {
                    Log.d("OpStatusBar", "IME show in landscape mode - NavBar remove it");
                    this.mWindowManager.removeViewImmediate(navView);
                    this.mNavShowing = false;
                }
                enableGestureHandler();
                updateImeWindowStatus();
            }
        }
        if (!(z2 == this.mNavShowing || getStatusBarKeyguardViewManager() == null)) {
            getStatusBarKeyguardViewManager().onHideNavBar(!this.mNavShowing);
        }
        this.mLastUpdateIMENavBarTime = SystemClock.uptimeMillis();
    }

    public void updateQsEnabled() {
        if (this.mDisableQs != 1 || getStatusBarKeyguardViewManager() == null || !getStatusBarKeyguardViewManager().isShowing() || !isKeyguardSecure()) {
            if (this.mQsDisabled) {
                Log.d("OpStatusBar", "enable QS");
            }
            this.mQsDisabled = false;
            return;
        }
        if (!this.mQsDisabled) {
            Log.d("OpStatusBar", "disable QS");
        }
        this.mQsDisabled = true;
    }

    private boolean isKeyguardSecure() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(StatusBar.class, this, "isKeyguardSecure", new Object[0])).booleanValue();
    }

    public boolean isQsDisabled() {
        return this.mQsDisabled;
    }

    /* access modifiers changed from: protected */
    public void inflateOPAodView(Context context) {
        this.mOPAodWindow = (RelativeLayout) View.inflate(context, C0011R$layout.op_aod_view, null);
    }

    /* access modifiers changed from: protected */
    public void opOnDensityOrFontScaleChanged() {
        OpAodWindowManager opAodWindowManager = this.mAodWindowManager;
        if (opAodWindowManager != null) {
            opAodWindowManager.getUIHandler().post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.8
                @Override // java.lang.Runnable
                public void run() {
                    OpStatusBar opStatusBar = OpStatusBar.this;
                    opStatusBar.inflateOPAodView(((SystemUI) opStatusBar).mContext);
                    OpStatusBar opStatusBar2 = OpStatusBar.this;
                    opStatusBar2.mAodWindowManager.updateView(opStatusBar2.mOPAodWindow);
                    OpStatusBar opStatusBar3 = OpStatusBar.this;
                    opStatusBar3.mAodDisplayViewManager.onDensityOrFontScaleChanged(opStatusBar3.mOPAodWindow);
                }
            });
        }
        BrightnessMirrorController brightnessMirrorController = this.mQuickBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.onDensityOrFontScaleChanged();
        }
    }

    public void onWakingAndUnlocking() {
        OpAodWindowManager opAodWindowManager = this.mAodWindowManager;
        if (opAodWindowManager != null) {
            opAodWindowManager.onWakingAndUnlocking();
            Log.d("OpStatusBar", "onWakingAndUnlocking");
            if (this.mAodWindowManager.isWakingAndUnlockByFP()) {
                checkToStopDozing();
            }
        }
    }

    public void launchHighlightHintAp() {
        Log.d("OpStatusBar", "launchHighlightHintAp");
        StatusBarNotification highlightHintNotification = ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).getHighlightHintNotification();
        Intent intentOnStatusBar = highlightHintNotification != null ? highlightHintNotification.getNotification().getIntentOnStatusBar() : null;
        if (intentOnStatusBar != null) {
            this.mContext.startActivityAsUser(intentOnStatusBar, new UserHandle(UserHandle.CURRENT.getIdentifier()));
        }
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController.OnHighlightHintStateChangeListener
    public void onHighlightHintStateChange() {
        ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).getHighlighColor();
        checkBarModes();
        setUpHighlightHintInfo();
    }

    /* access modifiers changed from: protected */
    public void setUpHighlightHintInfo() {
        if (((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).showOvalLayout() && this.mOrientation == 2 && getPanelController() != null) {
            getPanelController().setUpHighlightHintInfo();
        }
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController.OnHighlightHintStateChangeListener
    public void onHighlightHintInfoChange() {
        ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).getHighlighColor();
        checkBarModes();
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:13:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0022  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void opUpdateDozing() {
        /*
            r3 = this;
            boolean r0 = r3.mOpDozingRequested
            r1 = 0
            if (r0 != 0) goto L_0x0016
            com.android.systemui.statusbar.phone.BiometricUnlockController r0 = r3.getBiometricUnlockController()
            int r0 = r0.getMode()
            r3.getBiometricUnlockController()
            r2 = 2
            if (r0 != r2) goto L_0x0014
            goto L_0x0016
        L_0x0014:
            r0 = r1
            goto L_0x0017
        L_0x0016:
            r0 = 1
        L_0x0017:
            com.android.systemui.statusbar.phone.BiometricUnlockController r2 = r3.getBiometricUnlockController()
            boolean r2 = r2.isWakeAndUnlock()
            if (r2 == 0) goto L_0x0022
            goto L_0x0023
        L_0x0022:
            r1 = r0
        L_0x0023:
            boolean r0 = r3.mCustomDozing
            if (r0 == r1) goto L_0x003b
            r3.mCustomDozing = r1
            com.android.systemui.statusbar.phone.DozeScrimController r0 = r3.getDozeScrimController()
            boolean r1 = r3.mCustomDozing
            r0.setDozing(r1)
            com.android.systemui.keyguard.KeyguardViewMediator r0 = r3.getKeyguardViewMediator()
            boolean r3 = r3.mCustomDozing
            r0.setAodShowing(r3)
        L_0x003b:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.systemui.statusbar.phone.OpStatusBar.opUpdateDozing():void");
    }

    /* access modifiers changed from: protected */
    public void opUpdateResources() {
        if (DEBUG) {
            Log.d("OpStatusBar", "opUpdateResources");
        }
        BrightnessMirrorController brightnessMirrorController = this.mQuickBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.updateResources();
        }
    }

    /* access modifiers changed from: protected */
    public void checkToStartDozing() {
        if (DEBUG) {
            Log.d("OpStatusBar", "checkToStartDozing mStartDozingRequested=" + this.mStartDozingRequested + " mOpDozingRequested=" + this.mOpDozingRequested);
        }
        if (getPowerManager().isInteractive()) {
            Log.d("OpStatusBar", "checkToStartDozing: return, !isGoingToSleep");
        } else if (this.mStartDozingRequested && !this.mOpDozingRequested) {
            this.mOpDozingRequested = true;
            this.mAodWindowManager.startDozing();
            this.mAodDisplayViewManager.startDozing();
            opUpdateDozing();
            updateIsKeyguard();
        }
    }

    public OpAodWindowManager getAodWindowManager() {
        return this.mAodWindowManager;
    }

    /* access modifiers changed from: protected */
    public void checkToStopDozing() {
        if (DEBUG) {
            Log.d("OpStatusBar", "checkToStopDozing mOpDozingRequested=" + this.mOpDozingRequested + ":" + this.isCTSStart);
        }
        if (!getPowerManager().isInteractive()) {
            Log.d("OpStatusBar", "checkToStopDozing: return, !isWakingUpOrAwake");
            return;
        }
        if (this.isCTSStart) {
            if (this.mOpDozingRequested) {
                this.mAodWindowManager.stopDozing();
                this.mAodDisplayViewManager.stopDozing();
                this.mAodDisplayViewManager.resetStatus();
            }
            this.mOpWakingUpScrimController.removeFromWindow(true);
        } else if (this.mOpDozingRequested) {
            boolean isAlwaysOnEnabled = getKeyguardUpdateMonitor().isAlwaysOnEnabled();
            boolean isCanvasAodEnabled = OpCanvasAodHelper.isCanvasAodEnabled(this.mContext);
            if (!isAlwaysOnEnabled || isCanvasAodEnabled) {
                startWakingUpAnimation();
            } else {
                this.mAodWindowManager.stopDozing();
                this.mAodDisplayViewManager.stopDozing();
                this.mAodDisplayViewManager.resetStatus();
            }
        }
        if (this.mOpDozingRequested) {
            this.mOpDozingRequested = false;
            updateDozing();
        }
    }

    public void onFingerprintAuthenticated() {
        if (DEBUG) {
            Log.d("OpStatusBar", "onFingerprintAuthenticated mOpDozingRequested=" + this.mOpDozingRequested + " playAodWakingUpAnimation=" + this.mAodDisplayViewManager.playAodWakingUpAnimation());
        }
        getStatusBarKeyguardViewManager().onFingerprintAuthenticated();
        this.mOpWakingUpScrimController.onFingerprintAuthenticated();
        this.mAodWindowManager.onFingerprintAuthenticated();
        if (this.mOpDozingRequested) {
            if (DEBUG) {
                Log.d("OpStatusBar", "onFingerprintAuthenticated");
            }
            if (this.mAodDisplayViewManager.playAodWakingUpAnimation()) {
                this.mAodDisplayViewManager.onPlayFingerprintUnlockAnimation(true);
            }
            startWakingUpAnimation();
        }
    }

    public void onFacelockUnlocking(final boolean z) {
        getHandler().post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.9
            @Override // java.lang.Runnable
            public void run() {
                if (OpStatusBar.this.getStatusBarKeyguardViewManager() != null) {
                    OpStatusBar.this.getStatusBarKeyguardViewManager().onFacelockUnlocking(z);
                }
            }
        });
    }

    public void opOnStartedGoingToSleep() {
        Log.d("OpStatusBar", "opOnStartedGoingToSleep");
        cancelWakingupScrimAnimation();
        this.mWakingUpAnimationStart = false;
        this.mAodDisplayViewManager.onPlayFingerprintUnlockAnimation(false);
        getKeyguardUpdateMonitor().isAlwaysOnEnabled();
        OpCanvasAodHelper.isCanvasAodEnabled(this.mContext);
        if (!this.isCTSStart) {
            this.mOpWakingUpScrimController.prepare();
        } else {
            Log.w("OpStatusBar", "don't request show wakingUpScrim for PIP window test");
        }
    }

    public void opOnFinishedWakingUp() {
        Log.d("OpStatusBar", "opOnFinishedWakingUp");
        startWakingUpAnimation();
    }

    /* access modifiers changed from: protected */
    public void startWakingUpAnimation() {
        OpFacelockController opFacelockController;
        if (this.mWakingUpAnimationStart) {
            Log.d("OpStatusBar", "don't startWakingUpAnimation since animation started");
            return;
        }
        this.mWakingUpAnimationStart = true;
        boolean playAodWakingUpAnimation = this.mAodDisplayViewManager.playAodWakingUpAnimation();
        boolean isShowing = getStatusBarKeyguardViewManager().isShowing();
        boolean isPreventModeActive = OpLsState.getInstance().getPreventModeCtrl().isPreventModeActive();
        boolean isAnimationStarted = ((OpChargingAnimationController) Dependency.get(OpChargingAnimationController.class)).isAnimationStarted();
        boolean z = !getBouncerShowing() && !getIsOccluded() && isShowing && (opFacelockController = this.mOpFacelockController) != null && !opFacelockController.isScreenOffUnlock() && !getKeyguardUpdateMonitor().isCameraLaunched() && !isPreventModeActive && !isAnimationStarted;
        boolean isFingerprintAlreadyAuthenticated = getKeyguardUpdateMonitor().isFingerprintAlreadyAuthenticated();
        boolean isCanvasAodAnimation = OpCanvasAodHelper.isCanvasAodAnimation(this.mContext, isFingerprintAlreadyAuthenticated);
        if (DEBUG) {
            Log.i("OpStatusBar", "startWakingUpAnimation canPlayNotificationAnimation" + z + " isFingerprintUnlock:" + isFingerprintAlreadyAuthenticated + " bouncerShow:" + getBouncerShowing() + " isShowing:" + isShowing + " getIsOccluded:" + getIsOccluded() + " playAodWakingUpAnimation:" + playAodWakingUpAnimation + " isScreenOffUnlock:" + this.mOpFacelockController.isScreenOffUnlock() + " isCameraLaunched:" + getKeyguardUpdateMonitor().isCameraLaunched() + " isPreventViewShow:" + isPreventModeActive + " isWarpChargingAnimationStart:" + isAnimationStarted + " isCanvasAodAnimation: " + isCanvasAodAnimation);
        }
        cancelWakingupScrimAnimation();
        if (playAodWakingUpAnimation) {
            final AnimatorSet genAodDisappearAnimation = this.mAodWindowManager.genAodDisappearAnimation(isFingerprintAlreadyAuthenticated);
            this.mOpBoostUtils.aquireGPUBoost();
            genAodDisappearAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.10
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    Log.i("OpStatusBar", "AodDisappearAnimation onAnimationStart:");
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    Log.i("OpStatusBar", "AodDisappearAnimation onAnimationCancel:");
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    Log.i("OpStatusBar", "AodDisappearAnimation onAnimationEnd:");
                    OpStatusBar.this.mAodWindowManager.stopDozing();
                    OpStatusBar.this.mAodDisplayViewManager.stopDozing();
                    OpStatusBar.this.mAodDisplayViewManager.resetStatus();
                    OpStatusBar.this.mOpBoostUtils.releaseGPUBoost();
                }
            });
            if (isCanvasAodAnimation) {
                Log.i("OpStatusBar", "canvas aod override waking up scrim animation");
                this.mOpWakingUpScrimController.removeFromWindow(true);
            }
            if (isFingerprintAlreadyAuthenticated || !z) {
                this.mAodWindowManager.getUIHandler().post(new Runnable(this) { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.12
                    @Override // java.lang.Runnable
                    public void run() {
                        genAodDisappearAnimation.start();
                    }
                });
                if (!isCanvasAodAnimation) {
                    this.mOpWakingUpScrimController.startAnimation(false, isFingerprintAlreadyAuthenticated);
                }
                Log.i("OpStatusBar", "mWakingUpAnimation case 2");
                return;
            }
            if (!isCanvasAodAnimation) {
                this.mOpWakingUpScrimController.startAnimation(true, false);
            }
            this.mAodWindowManager.getUIHandler().post(new Runnable(this) { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.11
                @Override // java.lang.Runnable
                public void run() {
                    genAodDisappearAnimation.start();
                }
            });
            Log.i("OpStatusBar", "mWakingUpAnimation case 1");
            return;
        }
        this.mAodWindowManager.stopDozing();
        this.mAodDisplayViewManager.stopDozing();
        this.mAodDisplayViewManager.resetStatus();
        Log.i("OpStatusBar", "stopDozing");
        if (!isShowing || getKeyguardUpdateMonitor().isCameraLaunched()) {
            if (getKeyguardUpdateMonitor().isCameraLaunched()) {
                this.mOpWakingUpScrimController.removeFromWindowForCameraLaunched();
            } else {
                this.mOpWakingUpScrimController.removeFromWindow(true);
            }
            Log.i("OpStatusBar", "mWakingUpAnimation case 4");
            return;
        }
        this.mOpWakingUpScrimController.startAnimation(false, false);
        Log.i("OpStatusBar", "mWakingUpAnimation case 3");
    }

    private void cancelWakingupScrimAnimation() {
        if (this.mAodWindowManager == null || getNotificationPanelView() == null) {
            Log.d("OpStatusBar", " mAodWindowManager or NotificationPanelView is null");
            return;
        }
        Log.i("OpStatusBar", "cancelWakingupScrimAnimation");
        final AnimatorSet lastAodDisappearAnimation = this.mAodWindowManager.getLastAodDisappearAnimation();
        if (lastAodDisappearAnimation != null) {
            this.mAodWindowManager.getUIHandler().post(new Runnable(this) { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.13
                @Override // java.lang.Runnable
                public void run() {
                    lastAodDisappearAnimation.cancel();
                }
            });
        }
    }

    public void onBrickModeChanged(boolean z) {
        if (getHeadsUpAppearanceController() != null) {
            getHeadsUpAppearanceController().updateTopEntry();
        }
        getKeyguardUpdateMonitor().onBrickModeChanged(z);
        if (z) {
            checkToStopDozing();
        } else {
            checkToStartDozing();
        }
    }

    public void onEmptySpaceClick() {
        if ((getNotificationPanelViewController() != null) && (true ^ getNotificationPanelViewController().isTracking())) {
            getNotificationPanelViewController().onEmptySpaceClick(0.0f);
        }
    }

    public boolean notifyCameraLaunching(String str, boolean z) {
        OpFacelockController opFacelockController = this.mOpFacelockController;
        if (opFacelockController != null) {
            return opFacelockController.notifyCameraLaunching(true, str, z);
        }
        return false;
    }

    public OpFacelockController getFacelockController() {
        return this.mOpFacelockController;
    }

    public void startFacelockFailAnimation() {
        getNotificationPanelView();
    }

    public void notifyBarHeightChange(boolean z) {
        KeyguardViewMediator keyguardViewMediator = getKeyguardViewMediator();
        NotificationPanelView notificationPanelView = getNotificationShadeWindowView().getNotificationPanelView();
        if (!(keyguardViewMediator == null || this.mLastExpand == z || notificationPanelView == null)) {
            if (DEBUG) {
                Log.d("OpStatusBar", "BarHeight change to " + z + ", alpha:" + notificationPanelView.getAlpha());
            }
            if (DEBUG && getNotificationShadeWindowController() != null) {
                getNotificationShadeWindowController().debugBarHeight();
            }
            if (notificationPanelView.getAlpha() < 1.0f && !z) {
                keyguardViewMediator.notifyBarHeightChange(z);
            } else if (!z && getScrimController() != null) {
                getScrimController().resetForceHide();
            }
        }
        this.mLastExpand = z;
    }

    public interface OpDozeCallbacks {
        ArrayList<DozeHost.Callback> getCallbacks();

        default void fireThreeKeyChanged(int i) {
            Iterator<DozeHost.Callback> it = getCallbacks().iterator();
            while (it.hasNext()) {
                it.next().onThreeKeyChanged(i);
            }
        }

        default void fireSingleTap() {
            Iterator<DozeHost.Callback> it = getCallbacks().iterator();
            while (it.hasNext()) {
                it.next().onSingleTap();
            }
        }

        default void fireFingerprintPoke() {
            Iterator<DozeHost.Callback> it = getCallbacks().iterator();
            while (it.hasNext()) {
                it.next().onFingerprintPoke();
            }
        }

        default void fireAlwaysOnEnableChanged(boolean z) {
            Iterator<DozeHost.Callback> it = getCallbacks().iterator();
            while (it.hasNext()) {
                it.next().onAlwaysOnEnableChanged(z);
            }
        }
    }

    @Override // com.oneplus.opzenmode.OpZenModeController.Callback
    public void onThreeKeyStatus(final int i) {
        if (OpAodUtils.isMotionAwakeOn() || OpAodUtils.isSingleTapEnabled()) {
            getHandler().post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.14
                @Override // java.lang.Runnable
                public void run() {
                    if (OpStatusBar.this.getDozeServiceHost() != null) {
                        OpStatusBar.this.getDozeServiceHost().fireThreeKeyChanged(i);
                    }
                }
            });
        }
    }

    public void onSingleTap() {
        getHandler().post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.15
            @Override // java.lang.Runnable
            public void run() {
                if (OpStatusBar.this.getDozeServiceHost() != null) {
                    OpStatusBar.this.getDozeServiceHost().fireSingleTap();
                }
            }
        });
    }

    public void onFingerprintPoke() {
        getHandler().post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.16
            @Override // java.lang.Runnable
            public void run() {
                if (OpStatusBar.this.getDozeServiceHost() != null) {
                    OpStatusBar.this.getDozeServiceHost().fireFingerprintPoke();
                }
            }
        });
    }

    public void onAlwaysOnEnableChanged(final boolean z) {
        getHandler().post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.17
            @Override // java.lang.Runnable
            public void run() {
                if (OpStatusBar.this.getDozeServiceHost() != null) {
                    OpStatusBar.this.getDozeServiceHost().fireAlwaysOnEnableChanged(z);
                }
            }
        });
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).dispatchAlwaysOnEnableChanged(z);
    }

    public boolean shouldHideSensitive(NotificationEntry notificationEntry) {
        return ((NotificationViewHierarchyManager) Dependency.get(NotificationViewHierarchyManager.class)).shouldHideSensitive(notificationEntry, KeyguardUpdateMonitor.getCurrentUser());
    }

    /* access modifiers changed from: protected */
    public void opOnOverlayChanged() {
        if (DEBUG) {
            Log.d("OpStatusBar", "opOnOverlayChanged");
        }
        BrightnessMirrorController brightnessMirrorController = this.mQuickBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.onOverlayChanged();
        }
    }

    /* access modifiers changed from: protected */
    public void opOnUiModeChanged() {
        if (DEBUG) {
            Log.d("OpStatusBar", "opOnUiModeChanged");
        }
        BrightnessMirrorController brightnessMirrorController = this.mQuickBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.onUiModeChanged();
        }
        checkIfThemeChanged();
    }

    /* access modifiers changed from: protected */
    public void opOnConfigChanged(final Configuration configuration) {
        if (DEBUG) {
            Log.d("OpStatusBar", "opOnConfigChanged newConfig " + configuration);
        }
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
        }
        setUpHighlightHintInfo();
        checkIfThemeChanged();
        OpAodWindowManager opAodWindowManager = this.mAodWindowManager;
        if (opAodWindowManager != null) {
            opAodWindowManager.getUIHandler().post(new Runnable() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.18
                @Override // java.lang.Runnable
                public void run() {
                    OpStatusBar.this.mAodDisplayViewManager.onConfigChanged(configuration);
                }
            });
        }
        if (this.mOpEdgeBackGestureHandler != null) {
            if (DEBUG) {
                Log.d("OpStatusBar", "OpEdgeBackGestureHandler onConfigurationChanged ");
            }
            this.mOpEdgeBackGestureHandler.onConfigurationChanged(((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRotation());
        }
        if (this.mImeNavLp != null) {
            this.mImeNavLp.height = this.mContext.getResources().getDimensionPixelSize(17105324);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkIfThemeChanged() {
        int themeColor = OpUtils.getThemeColor(this.mContext);
        int themeAccentColor = OpUtils.getThemeAccentColor(this.mContext, C0004R$color.qs_tile_icon);
        boolean isSpecialTheme = OpUtils.isSpecialTheme(this.mContext);
        boolean isGoogleDarkTheme = OpUtils.isGoogleDarkTheme(this.mContext);
        Log.d("OpStatusBar", String.format("mThemeColor=0x%x -> 0x%x, mAccentColor=0x%x -> 0x%x, mSpecialTheme=%b -> %b, mGoogleDarkTheme=%b -> %b", Integer.valueOf(this.mThemeColor), Integer.valueOf(themeColor), Integer.valueOf(this.mAccentColor), Integer.valueOf(themeAccentColor), Boolean.valueOf(this.mSpecialTheme), Boolean.valueOf(isSpecialTheme), Boolean.valueOf(this.mGoogleDarkTheme), Boolean.valueOf(isGoogleDarkTheme)));
        if (this.mSpecialTheme != isSpecialTheme || ((this.mThemeColor == 2 && themeColor == 0) || (this.mThemeColor == 0 && themeColor == 2))) {
            this.mThemeColor = themeColor;
            this.mAccentColor = themeAccentColor;
            this.mSpecialTheme = isSpecialTheme;
            Log.d("OpStatusBar", "checkIfThemeChanged: handle theme change #1");
            ThemeColorUtils.init(this.mContext);
            Log.d("OpStatusBar", "checkIfThemeChanged: handle theme change #2");
            FragmentHostManager.get(getStatusBarWindow()).reloadFragments();
            Log.d("OpStatusBar", "checkIfThemeChanged: handle theme change #3");
            this.mGoogleDarkTheme = isGoogleDarkTheme;
        }
        this.mThemeColor = themeColor;
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (this.mOpFacelockController != null) {
            printWriter.println();
            this.mOpFacelockController.dump(fileDescriptor, printWriter, strArr);
            printWriter.println();
        } else {
            printWriter.println(" OpFacelockController null");
        }
        if (this.mAodDisplayViewManager != null) {
            printWriter.println();
            this.mAodDisplayViewManager.dump(printWriter);
            printWriter.println();
        }
        printWriter.println(" mDisableQs:" + this.mDisableQs);
        printWriter.println(" mNavType:" + this.mNavType);
        printWriter.println(" mDoubleTapPowerApp:" + this.mDoubleTapPowerApp);
        printWriter.println(" supportDouble:" + OpUtils.isSupportDoubleTapAlexa());
        printWriter.println(" notificationBGColor:" + this.mContext.getColor(C0004R$color.notification_material_background_color));
        OpUtils.dump(fileDescriptor, printWriter, strArr);
    }

    /* access modifiers changed from: protected */
    public boolean opVibrateForCameraGesture(Context context, Vibrator vibrator) {
        if (!OpUtils.isSupportLinearVibration()) {
            return false;
        }
        if (!VibratorSceneUtils.isVibratorSceneSupported(context, 1024)) {
            return true;
        }
        VibratorSceneUtils.doVibrateWithSceneIfNeeded(context, vibrator, 1024);
        return true;
    }

    public void removeHeadsUps() {
        HeadsUpManagerPhone headsUpManager = getHeadsUpManager();
        if (headsUpManager != null) {
            headsUpManager.releaseAllImmediately();
        }
        OpNotificationController opNotificationController = this.mOpNotificationController;
        if (opNotificationController != null) {
            opNotificationController.hideSimpleHeadsUps();
        }
    }

    public ActivityLaunchAnimator getActivityLaunchAnimator() {
        return (ActivityLaunchAnimator) OpReflectionUtils.getValue(StatusBar.class, this, "mActivityLaunchAnimator");
    }

    public static ActivityOptions getActivityOptionsInternal(RemoteAnimationAdapter remoteAnimationAdapter) {
        ActivityOptions activityOptions;
        if (remoteAnimationAdapter != null) {
            activityOptions = ActivityOptions.makeRemoteAnimation(remoteAnimationAdapter);
        } else {
            activityOptions = ActivityOptions.makeBasic();
        }
        activityOptions.setLaunchWindowingMode(4);
        return activityOptions;
    }

    /* access modifiers changed from: protected */
    public void opUpdateScrimController() {
        Trace.beginSection("StatusBar#updateScrimController");
        boolean isWakeAndUnlock = getBiometricUnlockController().isWakeAndUnlock();
        ScrimController scrimController = getScrimController();
        scrimController.setExpansionAffectsAlpha(!getBiometricUnlockController().isBiometricUnlock());
        scrimController.setLaunchingAffordanceWithPreview(false);
        if (getBouncerShowing() || ((getBiometricUnlockController().getFaceLockMode() == 8 || getBiometricUnlockController().getMode() == 8) && isKeyguardShowing())) {
            ScrimState scrimState = getStatusBarKeyguardViewManager().bouncerNeedsScrimming() ? ScrimState.BOUNCER_SCRIMMED : ScrimState.BOUNCER;
            if (DEBUG) {
                Log.i("OpStatusBar", "isUserUnlocked:" + getKeyguardUpdateMonitor().isUserUnlocked() + ", mBouncerScrimmedBootDone:" + this.mBouncerScrimmedBootDone + ", state:" + scrimState);
            }
            if (scrimState == ScrimState.BOUNCER_SCRIMMED && !getKeyguardUpdateMonitor().isUserUnlocked() && !this.mBouncerScrimmedBootDone) {
                scrimState = ScrimState.BOUNCER_SCRIMMED_BOOT;
                this.mBouncerScrimmedBootDone = true;
            }
            scrimController.transitionTo(scrimState);
        } else if ((isInLaunchTransition() && !getIsKeyguard()) || isLaunchCameraWhenFinishedWaking()) {
            scrimController.transitionTo(ScrimState.UNLOCKED, getUnlockScrimCallback());
        } else if (isBrightnessMirrorVisible()) {
            scrimController.transitionTo(ScrimState.BRIGHTNESS_MIRROR);
        } else if (isPulsing()) {
            scrimController.transitionTo(ScrimState.PULSING, getDozeScrimController().getScrimCallback());
        } else if (((DozeServiceHost) getDozeServiceHost()).hasPendingScreenOffCallback()) {
            scrimController.transitionTo(ScrimState.OFF, new ScrimController.Callback() { // from class: com.oneplus.systemui.statusbar.phone.OpStatusBar.19
                @Override // com.android.systemui.statusbar.phone.ScrimController.Callback
                public void onFinished() {
                    ((DozeServiceHost) OpStatusBar.this.getDozeServiceHost()).executePendingScreenOffCallback();
                }
            });
        } else if (isDozing() && !isWakeAndUnlock) {
            scrimController.transitionTo(ScrimState.AOD);
        } else if (getIsKeyguard() && !isWakeAndUnlock) {
            scrimController.transitionTo(ScrimState.KEYGUARD);
        } else if (getBubbleController().isStackExpanded()) {
            scrimController.transitionTo(ScrimState.BUBBLE_EXPANDED);
        } else {
            scrimController.transitionTo(ScrimState.UNLOCKED, getUnlockScrimCallback());
        }
        Trace.endSection();
        getNotificationPanelViewController().updateScrimState(scrimController.getState());
    }

    /* access modifiers changed from: protected */
    public void opDispatchDemoCommand(String str, Bundle bundle) {
        if (str.equals("highlight")) {
            bundle.getString("mode").equals("enable");
            showDemoHighLight(bundle.getString("type").equals("show"), bundle.getString("chronometer").equals("start"));
        }
        if (str.equals("carmode_highlight")) {
            bundle.getString("mode").equals("enable");
            showDemooCarModeHighLight(bundle.getString("type").equals("show"), bundle.getString("chronometer").equals("start"));
        }
    }

    /* access modifiers changed from: protected */
    public void showDemoHighLight(boolean z, boolean z2) {
        Log.i("OpStatusBar", " showDemoHighLight show:" + z);
        Context context = this.mContext;
        if (context != null) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            if (z) {
                if (this.mDemoHighlightHint == null) {
                    this.mDemoHighlightHint = getDemoNotificationBuilder();
                }
                this.mDemoHighlightHint.setShowOnStatusBar(true);
                this.mDemoHighlightHint.setUsesChronometerOnStatusBar(true);
                this.mDemoHighlightHint.setChronometerBase(0);
                if (z2) {
                    this.mDemoHighlightHint.setChronometerState(0);
                } else {
                    this.mDemoHighlightHint.setChronometerState(1);
                }
                notificationManager.notify(50, this.mDemoHighlightHint.build());
                Log.i("OpStatusBar", " send demo HighlightHint");
                return;
            }
            this.mDemoHighlightHint = null;
            notificationManager.cancel(50);
            Log.i("OpStatusBar", " cancel demo HighlightHint");
        }
    }

    private Notification.Builder getDemoNotificationBuilder() {
        Notification.Builder builder = new Notification.Builder(this.mContext, NotificationChannels.GENERAL);
        builder.setOngoing(true);
        builder.setPriority(1);
        constructDemoHighLightNotification(builder, C0006R$drawable.ic_add);
        return builder;
    }

    private void constructDemoHighLightNotification(Notification.Builder builder, int i) {
        builder.setSmallIcon(i);
        builder.setIconOnStatusBar(i);
        builder.setPriority(1);
        builder.setPriorityOnStatusBar(50);
        builder.setTextOnStatusBar(C0015R$string.notification_tap_again);
        builder.setBackgroundColorOnStatusBar(-16470538);
        builder.setUsesChronometer(true);
        builder.setUsesChronometerOnStatusBar(true);
        builder.setShowOnStatusBar(true);
    }

    /* access modifiers changed from: protected */
    public void showDemooCarModeHighLight(boolean z, boolean z2) {
        Log.i("OpStatusBar", " showDemooCarModeHighLight show:" + z);
        Context context = this.mContext;
        if (context != null) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            if (z) {
                if (this.mDemoCarModeHighlightHint == null) {
                    this.mDemoCarModeHighlightHint = getCarModeDemoNotificationBuilder();
                }
                this.mDemoCarModeHighlightHint.setShowOnStatusBar(true);
                this.mDemoCarModeHighlightHint.setUsesChronometerOnStatusBar(true);
                this.mDemoCarModeHighlightHint.setChronometerBase(0);
                this.mDemoCarModeHighlightHint.setIntentOnStatusBar(new Intent("com.oneplus.carmode.test"));
                if (z2) {
                    this.mDemoCarModeHighlightHint.setChronometerState(0);
                } else {
                    this.mDemoCarModeHighlightHint.setChronometerState(1);
                }
                if (this.mCarModeReceiver == null) {
                    this.mCarModeReceiver = new CarModeScreenshotReceiver();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("com.oneplus.carmode.test");
                    this.mContext.registerReceiver(this.mCarModeReceiver, intentFilter);
                }
                Notification build = this.mDemoCarModeHighlightHint.build();
                notificationManager.notify(200, build);
                Log.i("OpStatusBar", " send demo carModeHighlightHint intent:" + build.getIntentOnStatusBar());
                return;
            }
            this.mDemoCarModeHighlightHint = null;
            CarModeScreenshotReceiver carModeScreenshotReceiver = this.mCarModeReceiver;
            if (carModeScreenshotReceiver != null) {
                this.mContext.unregisterReceiver(carModeScreenshotReceiver);
                this.mCarModeReceiver = null;
            }
            notificationManager.cancel(200);
            Log.i("OpStatusBar", " cancel demo carModeHighlightHint");
        }
    }

    private Notification.Builder getCarModeDemoNotificationBuilder() {
        Notification.Builder builder = new Notification.Builder(this.mContext, NotificationChannels.GENERAL);
        builder.setOngoing(true);
        builder.setPriority(1);
        constructCarModeDemoHighLightNotification(builder, C0006R$drawable.ic_add);
        return builder;
    }

    private void constructCarModeDemoHighLightNotification(Notification.Builder builder, int i) {
        builder.setSmallIcon(i);
        builder.setIconOnStatusBar(i);
        builder.setPriority(1);
        builder.setPriorityOnStatusBar(200);
        builder.setTextOnStatusBar(C0015R$string.notification_tap_again);
        builder.setBackgroundColorOnStatusBar(-3823);
        builder.setShowOnStatusBar(true);
    }

    public class CarModeScreenshotReceiver extends BroadcastReceiver {
        public CarModeScreenshotReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.i("OpStatusBar", " cancel carMode");
            OpStatusBar.this.showDemooCarModeHighLight(false, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private NotificationPanelView getNotificationPanelView() {
        if (getNotificationShadeWindowView() != null) {
            return getNotificationShadeWindowView().getNotificationPanelView();
        }
        return null;
    }

    public void dispatchNotificationsPanelTouchEvent(MotionEvent motionEvent) {
        if (!getCommandQueue().panelsEnabled()) {
            Log.w("OpStatusBar", "not panelsEnabled");
        } else if (getNotificationPanelViewController() == null || getNotificationPanelViewController().getView() == null) {
            Log.w("OpStatusBar", "getNotificationPanelViewController null");
        } else if (getNotificationShadeWindowController() == null) {
            Log.w("OpStatusBar", "getNotificationShadeWindowController null");
        } else {
            getNotificationPanelViewController().getView().dispatchTouchEvent(motionEvent);
            int action = motionEvent.getAction();
            if (action == 0) {
                getNotificationShadeWindowController().setNotTouchable(true);
                if (DEBUG) {
                    Log.d("OpStatusBar", "setNotTouchable true");
                }
            } else if (action == 1 || action == 3) {
                getNotificationShadeWindowController().setNotTouchable(false);
                if (DEBUG) {
                    Log.d("OpStatusBar", "setNotTouchable false");
                }
            }
        }
    }

    public CommandQueue getCommandQueue() {
        return (CommandQueue) OpReflectionUtils.getValue(StatusBar.class, this, "mCommandQueue");
    }

    public View getOpStatusBarView() {
        return (View) OpReflectionUtils.methodInvokeVoid(StatusBar.class, this, "getStatusBarView", new Object[0]);
    }

    private HeadsUpManagerPhone getHeadsUpManager() {
        return (HeadsUpManagerPhone) OpReflectionUtils.getValue(StatusBar.class, this, "mHeadsUpManager");
    }

    private NotificationPanelViewController getPanelController() {
        return (NotificationPanelViewController) OpReflectionUtils.methodInvokeVoid(StatusBar.class, this, "getPanelController", new Object[0]);
    }

    private void checkBarModes() {
        OpReflectionUtils.methodInvokeVoid(StatusBar.class, this, "checkBarModes", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Handler getHandler() {
        return (Handler) OpReflectionUtils.getValue(StatusBar.class, this, "mHandler");
    }

    private BiometricUnlockController getBiometricUnlockController() {
        return (BiometricUnlockController) OpReflectionUtils.getValue(StatusBar.class, this, "mBiometricUnlockController");
    }

    private KeyguardViewMediator getKeyguardViewMediator() {
        return (KeyguardViewMediator) OpReflectionUtils.getValue(StatusBar.class, this, "mKeyguardViewMediator");
    }

    private ScrimController getScrimController() {
        return (ScrimController) OpReflectionUtils.getValue(StatusBar.class, this, "mScrimController");
    }

    private boolean isDozing() {
        return ((Boolean) OpReflectionUtils.getValue(StatusBar.class, this, "mDozing")).booleanValue();
    }

    public int getSystemIconAreaMaxWidth(int i) {
        PhoneStatusBarView phoneStatusBarView = (PhoneStatusBarView) getOpStatusBarView();
        int i2 = 0;
        if (phoneStatusBarView == null) {
            return 0;
        }
        ViewGroup viewGroup = (ViewGroup) phoneStatusBarView.findViewById(C0008R$id.status_bar_contents);
        if (viewGroup != null) {
            View findViewById = viewGroup.findViewById(C0008R$id.clock);
            View findViewById2 = viewGroup.findViewById(C0008R$id.system_icon_area);
            if (!(findViewById == null || findViewById2 == null)) {
                View findViewById3 = findViewById2.findViewById(C0008R$id.battery);
                int paddingStart = viewGroup.getPaddingStart();
                int paddingEnd = viewGroup.getPaddingEnd();
                boolean isHighLightHintShow = ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isHighLightHintShow();
                phoneStatusBarView.getLayoutDirection();
                int maxDotsForNotificationIconContainer = OpUtils.getMaxDotsForNotificationIconContainer(this.mContext);
                Context context = this.mContext;
                int dimensionPixelSize = (context == null || maxDotsForNotificationIconContainer <= 0) ? 0 : context.getResources().getDimensionPixelSize(C0005R$dimen.status_bar_icon_size) * maxDotsForNotificationIconContainer;
                int width = viewGroup.getWidth();
                int measuredWidth = paddingStart + paddingEnd + findViewById3.getMeasuredWidth() + findViewById.getMeasuredWidth();
                if (isHighLightHintShow) {
                    i2 = phoneStatusBarView.getHighlightHintWidth();
                }
                return width - (((measuredWidth + i2) + dimensionPixelSize) + findViewById2.getPaddingStart());
            }
        }
        return -1;
    }

    public int getMinWidthOfClock() {
        ViewGroup viewGroup;
        View findViewById;
        PhoneStatusBarView phoneStatusBarView = (PhoneStatusBarView) getOpStatusBarView();
        if (phoneStatusBarView == null || (viewGroup = (ViewGroup) phoneStatusBarView.findViewById(C0008R$id.status_bar_contents)) == null || (findViewById = viewGroup.findViewById(C0008R$id.clock)) == null) {
            return 0;
        }
        return findViewById.getMinimumWidth();
    }

    private boolean updateIsKeyguard() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(StatusBar.class, this, "updateIsKeyguard", new Object[0])).booleanValue();
    }

    private void updateDozing() {
        opUpdateDozing();
    }

    private DozeScrimController getDozeScrimController() {
        return (DozeScrimController) OpReflectionUtils.getValue(StatusBar.class, this, "mDozeScrimController");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private KeyguardUpdateMonitor getKeyguardUpdateMonitor() {
        return (KeyguardUpdateMonitor) OpReflectionUtils.getValue(StatusBar.class, this, "mKeyguardUpdateMonitor");
    }

    private HeadsUpAppearanceController getHeadsUpAppearanceController() {
        return (HeadsUpAppearanceController) OpReflectionUtils.getValue(StatusBar.class, this, "mHeadsUpAppearanceController");
    }

    private NetworkController getNetworkController() {
        return (NetworkController) OpReflectionUtils.getValue(StatusBar.class, this, "mNetworkController");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private OpDozeCallbacks getDozeServiceHost() {
        return (OpDozeCallbacks) OpReflectionUtils.getValue(StatusBar.class, this, "mDozeServiceHost");
    }

    private StatusBarWindowView getStatusBarWindow() {
        return (StatusBarWindowView) OpReflectionUtils.getValue(StatusBar.class, this, "mPhoneStatusBarWindow");
    }

    private boolean getIsOccluded() {
        return ((Boolean) OpReflectionUtils.getValue(StatusBar.class, this, "mIsOccluded")).booleanValue();
    }

    private boolean getBouncerShowing() {
        return ((Boolean) OpReflectionUtils.getValue(StatusBar.class, this, "mBouncerShowing")).booleanValue();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private StatusBarKeyguardViewManager getStatusBarKeyguardViewManager() {
        return (StatusBarKeyguardViewManager) OpReflectionUtils.getValue(StatusBar.class, this, "mStatusBarKeyguardViewManager");
    }

    private boolean isKeyguardShowing() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(StatusBar.class, this, "isKeyguardShowing", new Object[0])).booleanValue();
    }

    private boolean isInLaunchTransition() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(StatusBar.class, this, "isInLaunchTransition", new Object[0])).booleanValue();
    }

    private boolean isLaunchCameraWhenFinishedWaking() {
        return ((Boolean) OpReflectionUtils.getValue(StatusBar.class, this, "mLaunchCameraWhenFinishedWaking")).booleanValue();
    }

    private boolean isPulsing() {
        return ((Boolean) OpReflectionUtils.methodInvokeVoid(StatusBar.class, this, "isPulsing", new Object[0])).booleanValue();
    }

    private boolean getIsKeyguard() {
        return ((Boolean) OpReflectionUtils.getValue(StatusBar.class, this, "mIsKeyguard")).booleanValue();
    }

    private BubbleController getBubbleController() {
        return (BubbleController) OpReflectionUtils.getValue(StatusBar.class, this, "mBubbleController");
    }

    public int getDisableFlag() {
        return ((Integer) OpReflectionUtils.getValue(StatusBar.class, this, "mDisabled1")).intValue();
    }

    public int getAppearance() {
        return ((Integer) OpReflectionUtils.getValue(StatusBar.class, this, "mAppearance")).intValue();
    }

    /* access modifiers changed from: protected */
    public void notifyImeWindowVisible(int i, IBinder iBinder, int i2, int i3, boolean z) {
        this.mImeDisplayId = i;
        this.mToken = iBinder;
        this.mImeVisibleState = i2;
        this.mBackDisposition = i3;
        this.mShowImeSwitcher = z;
        this.mImeShow = (i2 & 2) != 0;
        this.mImeStateChange = true;
        if (!getHandler().hasCallbacks(this.mCheckIMENavBarTask) && (getNavigationBarHiddenMode() == 1 || !EdgeBackGestureHandler.sSideGestureEnabled)) {
            Log.d("OpStatusBar", "Check navigation bar state when ime state change");
            checkIMENavBarState();
        }
        if (getNotificationPanelViewController() != null) {
            getNotificationPanelViewController().setIsImeShow(this.mImeShow);
        }
    }

    public void updateImeWindowStatus() {
        Log.d("OpStatusBar", "updateImeWindowStatus ImeStateChange " + this.mImeStateChange + " , imeShow " + this.mImeShow);
        if (this.mImeStateChange) {
            NavigationBarFragment defaultNavigationBarFragment = ((NavigationBarController) Dependency.get(NavigationBarController.class)).getDefaultNavigationBarFragment();
            if (defaultNavigationBarFragment != null) {
                defaultNavigationBarFragment.setImeWindowStatus(this.mImeDisplayId, this.mToken, this.mImeVisibleState, this.mBackDisposition, this.mShowImeSwitcher);
            }
            this.mImeStateChange = false;
        }
    }

    public boolean isImeStateChange() {
        return this.mImeStateChange;
    }

    private void enableGestureHandler() {
        OverviewProxyService overviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        int navBarMode = overviewProxyService.getNavBarMode();
        Display defaultDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        if (this.mOpEdgeBackGestureHandler == null) {
            if (DEBUG) {
                Log.d("OpStatusBar", "OpStatusBar new OpEdgeBackGestureHandler when NavBar hidden");
            }
            this.mOpEdgeBackGestureHandler = new OpEdgeBackGestureHandler(this.mContext, overviewProxyService, this.mSysUiFlagContainer, this.mPluginManager);
            if (((NavigationModeController) Dependency.get(NavigationModeController.class)).getCurrentUserContext() != null) {
                this.mOpEdgeBackGestureHandler.onNavigationModeChanged(navBarMode);
            }
            this.mOpEdgeBackGestureHandler.onNavigationBarHidden();
            this.mOpEdgeBackGestureHandler.onConfigurationChanged(defaultDisplay.getRotation());
            this.mRotation = defaultDisplay.getRotation();
            return;
        }
        if (DEBUG) {
            Log.d("OpStatusBar", "OpEdgeBackGestureHandler update window");
        }
        if (this.mRotation != defaultDisplay.getRotation()) {
            this.mOpEdgeBackGestureHandler.onConfigurationChanged(defaultDisplay.getRotation());
            this.mRotation = defaultDisplay.getRotation();
        }
    }

    private void disableGestureHandler() {
        if (this.mOpEdgeBackGestureHandler != null) {
            if (DEBUG) {
                Log.d("OpStatusBar", "OpStatusBar release OpEdgeBackGestureHandler when NavBar show");
            }
            this.mOpEdgeBackGestureHandler.onNavigationBarShow();
            this.mOpEdgeBackGestureHandler = null;
        }
    }

    private PowerManager getPowerManager() {
        return (PowerManager) OpReflectionUtils.getValue(StatusBar.class, this, "mPowerManager");
    }

    public void startDozing() {
        this.mStartDozingRequested = true;
        if (this.mOpSceneModeObserver.isInBrickMode()) {
            Log.i("OpStatusBar", "Do not start dozing in brick mode");
        } else {
            checkToStartDozing();
        }
    }

    public void stopDozing() {
        this.mStartDozingRequested = false;
        checkToStopDozing();
    }

    public void showBouncer(boolean z) {
        getStatusBarKeyguardViewManager().showBouncer(z);
    }

    public OpGestureButtonViewController getGestureButtonController() {
        return this.mOpGestureButtonViewController;
    }

    public OpOneHandModeController getOneHandModeController() {
        return this.mOpOneHandModeController;
    }

    public OpCameraAnimateController getOpCameraAnimateController() {
        return this.mCameraAnim;
    }

    public boolean bypassPreventMode() {
        if (getNotificationPanelViewController() != null) {
            return getNotificationPanelViewController().bypassPreventMode(this.mContext);
        }
        return false;
    }

    private NotificationShadeWindowView getNotificationShadeWindowView() {
        return (NotificationShadeWindowView) OpReflectionUtils.methodInvokeVoid(StatusBar.class, this, "getNotificationShadeWindowView", new Object[0]);
    }

    private NotificationShadeWindowController getNotificationShadeWindowController() {
        return (NotificationShadeWindowController) OpReflectionUtils.getValue(StatusBar.class, this, "mNotificationShadeWindowController");
    }

    private NotificationPanelViewController getNotificationPanelViewController() {
        return (NotificationPanelViewController) OpReflectionUtils.getValue(StatusBar.class, this, "mNotificationPanelViewController");
    }

    public OpKeyguardUnlockCounter getKeyguardUnlockCounter() {
        if (getKeyguardViewMediator() != null) {
            return getKeyguardViewMediator().getKeyguardUnlockCounter();
        }
        return null;
    }

    public void setmStatusBarCollapseListener(StatusBarCollapseListener statusBarCollapseListener) {
        this.mStatusBarCollapseListener = statusBarCollapseListener;
    }

    public StatusBarCollapseListener getmStatusBarCollapseListener() {
        return this.mStatusBarCollapseListener;
    }

    public OpSystemUIGestureOnlineConfig getGestureOnlineConfig() {
        return this.mGestureOnlineConfig;
    }

    public void shouldForceHideWallpaper(boolean z) {
        if (!z || OpUtils.isHomeApp() || !getStatusBarKeyguardViewManager().isUnlockWithWallpaper()) {
            getNotificationShadeWindowController().forceHideWallpaper(false);
        } else {
            getNotificationShadeWindowController().forceHideWallpaper(true);
        }
    }

    public void opOnSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        if (appearanceRegionArr != null) {
            boolean z2 = true;
            if (appearanceRegionArr.length == 1) {
                z2 = false;
            }
            if (DEBUG && z2 != this.mIsInMultiWindow) {
                Log.d("OpStatusBar", "isInMultiWindow = " + z2);
            }
            this.mIsInMultiWindow = z2;
        }
    }

    public boolean isInMultiWindow() {
        return this.mIsInMultiWindow;
    }
}
