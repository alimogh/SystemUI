package com.android.systemui.statusbar.phone;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.InsetsState;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import androidx.lifecycle.Lifecycle;
import com.android.internal.accessibility.dialog.AccessibilityButtonChooserActivity;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.util.LatencyTracker;
import com.android.internal.view.AppearanceRegion;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.accessibility.SystemActions;
import com.android.systemui.assist.AssistHandleViewController;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.Recents;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.AutoHideUiElement;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.phone.ContextualButton;
import com.android.systemui.statusbar.phone.NavigationBarTransitions;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.util.LifecycleFragment;
import com.oneplus.onlineconfig.OpSystemUIGestureOnlineConfig;
import com.oneplus.util.OpNavBarUtils;
import com.oneplus.util.OpUtils;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
public class NavigationBarFragment extends LifecycleFragment implements CommandQueue.Callbacks, NavigationModeController.ModeChangedListener, DisplayManager.DisplayListener {
    private static Context sContext;
    private final AccessibilityManager.AccessibilityServicesStateChangeListener mAccessibilityListener;
    private AccessibilityManager mAccessibilityManager;
    private final AccessibilityManagerWrapper mAccessibilityManagerWrapper;
    private int mAppearance;
    private final ContentObserver mAssistContentObserver;
    private AssistHandleViewController mAssistHandlerViewController;
    protected final AssistManager mAssistManager;
    private boolean mAssistantAvailable;
    private final Runnable mAutoDim;
    private AutoHideController mAutoHideController;
    private final AutoHideUiElement mAutoHideUiElement;
    private int mBackupNavBarMode;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final BroadcastReceiver mBroadcastReceiver;
    final Runnable mCheckNavigationBarState;
    private final Runnable mCloseByPassThreshold;
    private final CommandQueue mCommandQueue;
    private ContentResolver mContentResolver;
    private int mCurrentRotation;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private int mDisabledFlags1;
    private int mDisabledFlags2;
    public int mDisplayId;
    private final Divider mDivider;
    private boolean mForceNavBarHandleOpaque;
    private OpSystemUIGestureOnlineConfig mGestureOnlineConfig;
    private final Handler mHandler;
    private boolean mHideNavBar;
    public boolean mHomeBlockedThisTouch;
    private boolean mImeShow;
    private boolean mIsInBrickMode;
    private boolean mIsOnDefaultDisplay;
    private long mLastLockToAppLongPress;
    private int mLayoutDirection;
    private LightBarController mLightBarController;
    private Locale mLocale;
    private final MetricsLogger mMetricsLogger;
    private View.OnClickListener mNavActionListener;
    private int mNavBarMode;
    private NavBarSettingObserver mNavBarSettingObserver;
    private int mNavigationBarColor;
    private int mNavigationBarMode;
    protected NavigationBarView mNavigationBarView = null;
    private int mNavigationBarWindowState;
    private int mNavigationIconHints;
    private final NotificationRemoteInputManager mNotificationRemoteInputManager;
    private final DeviceConfig.OnPropertiesChangedListener mOnPropertiesChangedListener;
    private int mOrientation;
    private QuickswitchOrientedNavHandle mOrientationHandle;
    private ViewTreeObserver.OnGlobalLayoutListener mOrientationHandleGlobalLayoutListener;
    private NavigationBarTransitions.DarkIntensityListener mOrientationHandleIntensityListener;
    private WindowManager.LayoutParams mOrientationParams;
    private final OverviewProxyService.OverviewProxyListener mOverviewProxyListener;
    private OverviewProxyService mOverviewProxyService;
    private final Optional<Recents> mRecentsOptional;
    private final ContextualButton.ContextButtonListener mRotationButtonListener;
    private final Consumer<Integer> mRotationWatcher;
    private boolean mShowOrientedHandleForImmersiveMode;
    private int mStartingQuickSwitchRotation;
    private final Lazy<StatusBar> mStatusBarLazy;
    private final StatusBarStateController mStatusBarStateController;
    private SysUiState mSysUiFlagsContainer;
    private final SystemActions mSystemActions;
    private boolean mTransientShown;
    private UiEventLogger mUiEventLogger;
    private WindowManager mWindowManager;

    private static int barMode(boolean z, int i) {
        if (z) {
            return 1;
        }
        if ((i & 6) == 6) {
            return 3;
        }
        if ((i & 4) != 0) {
            return 6;
        }
        return (i & 2) != 0 ? 4 : 0;
    }

    private int deltaRotation(int i, int i2) {
        int i3 = i2 - i;
        return i3 < 0 ? i3 + 4 : i3;
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayAdded(int i) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onDisplayRemoved(int i) {
    }

    @VisibleForTesting
    public enum NavBarActionEvent implements UiEventLogger.UiEventEnum {
        NAVBAR_ASSIST_LONGPRESS(550);
        
        private final int mId;

        private NavBarActionEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$NavigationBarFragment() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.getRegionSamplingHelper().setByPassThreshold(false);
            NavigationHandle navigationHandle = (NavigationHandle) this.mNavigationBarView.getHomeHandle().getCurrentView();
            if (navigationHandle != null) {
                navigationHandle.triggerChangeColorAnimation();
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$NavigationBarFragment(ContextualButton contextualButton, boolean z) {
        if (z) {
            this.mAutoHideController.touchAutoHide();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$NavigationBarFragment() {
        getBarTransitions().setAutoDim(true);
    }

    public NavigationBarFragment(AccessibilityManagerWrapper accessibilityManagerWrapper, DeviceProvisionedController deviceProvisionedController, MetricsLogger metricsLogger, AssistManager assistManager, OverviewProxyService overviewProxyService, NavigationModeController navigationModeController, StatusBarStateController statusBarStateController, SysUiState sysUiState, BroadcastDispatcher broadcastDispatcher, CommandQueue commandQueue, Divider divider, Optional<Recents> optional, Lazy<StatusBar> lazy, ShadeController shadeController, NotificationRemoteInputManager notificationRemoteInputManager, SystemActions systemActions, Handler handler, UiEventLogger uiEventLogger) {
        boolean z = false;
        this.mNavigationBarWindowState = 0;
        this.mNavigationIconHints = 0;
        this.mNavBarMode = 0;
        this.mStartingQuickSwitchRotation = -1;
        this.mHideNavBar = false;
        this.mNavigationBarColor = 0;
        this.mIsInBrickMode = false;
        this.mBackupNavBarMode = -1;
        this.mImeShow = false;
        this.mCloseByPassThreshold = new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$2a6PQeDykikHzH0rBVD4AZZp14o
            @Override // java.lang.Runnable
            public final void run() {
                NavigationBarFragment.this.lambda$new$0$NavigationBarFragment();
            }
        };
        this.mAutoHideUiElement = new AutoHideUiElement() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.1
            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public boolean isHideNavBar() {
                return false;
            }

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public void synchronizeState() {
                NavigationBarFragment.this.checkNavBarModes();
            }

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public boolean shouldHideOnTouch() {
                return !NavigationBarFragment.this.mNotificationRemoteInputManager.getController().isRemoteInputActive();
            }

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public boolean isVisible() {
                return NavigationBarFragment.this.isTransientShown();
            }

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public void hide() {
                NavigationBarFragment.this.clearTransient();
            }

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public void refreshLayout(int i) {
                NavigationBarFragment.this.refreshLayout(i);
            }
        };
        this.mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.2
            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onConnectionChanged(boolean z2) {
                NavigationBarFragment.this.mNavigationBarView.updateStates();
                NavigationBarFragment.this.updateScreenPinningGestures();
                if (z2) {
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("NavigationBar", "sendAssistantAvailability to launcher when overview proxy connection changed. [Availability] " + NavigationBarFragment.this.mAssistantAvailable);
                    }
                    NavigationBarFragment navigationBarFragment = NavigationBarFragment.this;
                    navigationBarFragment.sendAssistantAvailability(navigationBarFragment.mAssistantAvailable);
                }
            }

            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onQuickSwitchToNewTask(int i) {
                NavigationBarFragment.this.mStartingQuickSwitchRotation = i;
                if (i == -1) {
                    NavigationBarFragment.this.mShowOrientedHandleForImmersiveMode = false;
                }
                NavigationBarFragment.this.orientSecondaryHomeHandle();
            }

            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void startAssistant(Bundle bundle) {
                NavigationBarFragment.this.mAssistManager.startAssist(bundle);
            }

            /* JADX WARNING: Removed duplicated region for block: B:18:0x0083 A[ADDED_TO_REGION] */
            /* JADX WARNING: Removed duplicated region for block: B:27:? A[RETURN, SYNTHETIC] */
            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onNavBarButtonAlphaChanged(float r5, boolean r6) {
                /*
                    r4 = this;
                    com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    int r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$1100(r0)
                    boolean r0 = com.android.systemui.shared.system.QuickStepContract.isSwipeUpMode(r0)
                    r1 = 0
                    r2 = 0
                    if (r0 == 0) goto L_0x0017
                    com.android.systemui.statusbar.phone.NavigationBarFragment r4 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    com.android.systemui.statusbar.phone.NavigationBarView r4 = r4.mNavigationBarView
                    com.android.systemui.statusbar.phone.ButtonDispatcher r4 = r4.getBackButton()
                    goto L_0x0080
                L_0x0017:
                    com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    int r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$1100(r0)
                    boolean r0 = com.android.systemui.shared.system.QuickStepContract.isGesturalMode(r0)
                    if (r0 == 0) goto L_0x007f
                    java.lang.Class<com.android.keyguard.KeyguardUpdateMonitor> r0 = com.android.keyguard.KeyguardUpdateMonitor.class
                    java.lang.Object r0 = com.android.systemui.Dependency.get(r0)
                    com.android.keyguard.KeyguardUpdateMonitor r0 = (com.android.keyguard.KeyguardUpdateMonitor) r0
                    boolean r0 = r0.isKeyguardVisible()
                    if (r0 != 0) goto L_0x0054
                    com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    int r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$1100(r0)
                    boolean r0 = com.android.systemui.shared.system.QuickStepContract.isGesturalMode(r0)
                    if (r0 == 0) goto L_0x0070
                    com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    dagger.Lazy r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$1200(r0)
                    java.lang.Object r0 = r0.get()
                    com.android.systemui.statusbar.phone.StatusBar r0 = (com.android.systemui.statusbar.phone.StatusBar) r0
                    int r0 = r0.getNavigationBarHiddenMode()
                    r3 = 1
                    if (r0 == r3) goto L_0x0054
                    boolean r0 = com.android.systemui.statusbar.phone.EdgeBackGestureHandler.sSideGestureEnabled
                    if (r0 != 0) goto L_0x0070
                L_0x0054:
                    java.lang.StringBuilder r0 = new java.lang.StringBuilder
                    r0.<init>()
                    java.lang.String r3 = "onNavBarButtonAlphaChanged replace alpha from "
                    r0.append(r3)
                    r0.append(r5)
                    java.lang.String r5 = " to 0.f."
                    r0.append(r5)
                    java.lang.String r5 = r0.toString()
                    java.lang.String r0 = "NavigationBar"
                    android.util.Log.d(r0, r5)
                    r5 = r1
                L_0x0070:
                    com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    boolean r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$1300(r0)
                    com.android.systemui.statusbar.phone.NavigationBarFragment r4 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    com.android.systemui.statusbar.phone.NavigationBarView r4 = r4.mNavigationBarView
                    com.android.systemui.statusbar.phone.ButtonDispatcher r4 = r4.getHomeHandle()
                    goto L_0x0081
                L_0x007f:
                    r4 = 0
                L_0x0080:
                    r0 = r2
                L_0x0081:
                    if (r4 == 0) goto L_0x0095
                    if (r0 != 0) goto L_0x008b
                    int r1 = (r5 > r1 ? 1 : (r5 == r1 ? 0 : -1))
                    if (r1 <= 0) goto L_0x008a
                    goto L_0x008b
                L_0x008a:
                    r2 = 4
                L_0x008b:
                    r4.setVisibility(r2)
                    if (r0 == 0) goto L_0x0092
                    r5 = 1065353216(0x3f800000, float:1.0)
                L_0x0092:
                    r4.setAlpha(r5, r6)
                L_0x0095:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavigationBarFragment.AnonymousClass2.onNavBarButtonAlphaChanged(float, boolean):void");
            }

            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onOverviewShown(boolean z2) {
                NavigationBarFragment.this.mNavigationBarView.getRotationButtonController().setSkipOverrideUserLockPrefsOnce();
            }

            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onToggleRecentApps() {
                NavigationBarFragment.this.mNavigationBarView.getRotationButtonController().setSkipOverrideUserLockPrefsOnce();
            }
        };
        this.mOrientationHandleIntensityListener = new NavigationBarTransitions.DarkIntensityListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.3
            @Override // com.android.systemui.statusbar.phone.NavigationBarTransitions.DarkIntensityListener
            public void onDarkIntensity(float f) {
                NavigationBarFragment.this.mOrientationHandle.setDarkIntensity(f);
            }
        };
        this.mRotationButtonListener = new ContextualButton.ContextButtonListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$64cObUVomQJ4mZyswq0vgRauVPI
            @Override // com.android.systemui.statusbar.phone.ContextualButton.ContextButtonListener
            public final void onVisibilityChanged(ContextualButton contextualButton, boolean z2) {
                NavigationBarFragment.this.lambda$new$1$NavigationBarFragment(contextualButton, z2);
            }
        };
        this.mAutoDim = new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$BSA_kMakLk5yiXdgwYZ_AoVEsRo
            @Override // java.lang.Runnable
            public final void run() {
                NavigationBarFragment.this.lambda$new$2$NavigationBarFragment();
            }
        };
        this.mAssistContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.4
            @Override // android.database.ContentObserver
            public void onChange(boolean z2, Uri uri) {
                boolean z3 = NavigationBarFragment.this.mAssistManager.getAssistInfoForUser(-2) != null;
                if (NavigationBarFragment.this.mAssistantAvailable != z3) {
                    NavigationBarFragment.this.sendAssistantAvailability(z3);
                    NavigationBarFragment.this.mAssistantAvailable = z3;
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("NavigationBar", "sendAssistantAvailability to launcher when receive assist state change. [Availability] " + NavigationBarFragment.this.mAssistantAvailable);
                    }
                }
            }
        };
        this.mOnPropertiesChangedListener = new DeviceConfig.OnPropertiesChangedListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.5
            public void onPropertiesChanged(DeviceConfig.Properties properties) {
                if (properties.getKeyset().contains("nav_bar_handle_force_opaque")) {
                    NavigationBarFragment.this.mForceNavBarHandleOpaque = properties.getBoolean("nav_bar_handle_force_opaque", false);
                }
            }
        };
        this.mNavActionListener = new View.OnClickListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (NavigationBarFragment.this.mHideNavBar) {
                    Settings.System.putInt(NavigationBarFragment.this.mContentResolver, "systemui_navigation_bar_hided", 0);
                } else {
                    Settings.System.putInt(NavigationBarFragment.this.mContentResolver, "systemui_navigation_bar_hided", 1);
                    ((StatusBar) NavigationBarFragment.this.mStatusBarLazy.get()).showNavigationBarGuide();
                }
                NavigationBarFragment navigationBarFragment = NavigationBarFragment.this;
                navigationBarFragment.mNavigationBarView.updateNavButtonIcon(navigationBarFragment.mHideNavBar);
                NavigationBarFragment navigationBarFragment2 = NavigationBarFragment.this;
                navigationBarFragment2.mHideNavBar = true ^ navigationBarFragment2.mHideNavBar;
                NavigationBarFragment.this.onHideNavBar(false);
            }
        };
        this.mCheckNavigationBarState = new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.7
            @Override // java.lang.Runnable
            public void run() {
                ((StatusBar) NavigationBarFragment.this.mStatusBarLazy.get()).checkNavigationBarState();
            }
        };
        this.mAccessibilityListener = new AccessibilityManager.AccessibilityServicesStateChangeListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$dxES00kAyC8r2RmY9FwTYgUhoj8
            public final void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager) {
                NavigationBarFragment.this.updateAccessibilityServicesState(accessibilityManager);
            }
        };
        this.mRotationWatcher = new Consumer() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$rPVTsn99rxp2rmSxt8MZ9ZaaU_I
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NavigationBarFragment.this.lambda$new$6$NavigationBarFragment((Integer) obj);
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.8
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.d("NavigationBar", "onReceive:" + action + " start" + this);
                }
                if ("android.intent.action.SCREEN_OFF".equals(action) || "android.intent.action.SCREEN_ON".equals(action)) {
                    NavigationBarFragment.this.notifyNavigationBarScreenOn();
                    NavigationBarFragment.this.mNavigationBarView.onScreenStateChanged("android.intent.action.SCREEN_ON".equals(action));
                }
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    NavigationBarFragment navigationBarFragment = NavigationBarFragment.this;
                    navigationBarFragment.updateAccessibilityServicesState(navigationBarFragment.mAccessibilityManager);
                }
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.d("NavigationBar", "onReceive:" + action + " end");
                }
            }
        };
        this.mAccessibilityManagerWrapper = accessibilityManagerWrapper;
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mStatusBarStateController = statusBarStateController;
        this.mMetricsLogger = metricsLogger;
        this.mAssistManager = assistManager;
        this.mSysUiFlagsContainer = sysUiState;
        this.mStatusBarLazy = lazy;
        this.mNotificationRemoteInputManager = notificationRemoteInputManager;
        this.mAssistantAvailable = assistManager.getAssistInfoForUser(-2) != null ? true : z;
        this.mOverviewProxyService = overviewProxyService;
        this.mNavBarMode = navigationModeController.addListener(this);
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mCommandQueue = commandQueue;
        this.mDivider = divider;
        this.mRecentsOptional = optional;
        this.mSystemActions = systemActions;
        this.mHandler = handler;
        this.mUiEventLogger = uiEventLogger;
    }

    @Override // com.android.systemui.util.LifecycleFragment, android.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mCommandQueue.observe(getLifecycle(), (Lifecycle) this);
        this.mWindowManager = (WindowManager) getContext().getSystemService(WindowManager.class);
        this.mAccessibilityManager = (AccessibilityManager) getContext().getSystemService(AccessibilityManager.class);
        ContentResolver contentResolver = getContext().getContentResolver();
        this.mContentResolver = contentResolver;
        contentResolver.registerContentObserver(Settings.Secure.getUriFor("assistant"), false, this.mAssistContentObserver, -1);
        NavBarSettingObserver navBarSettingObserver = new NavBarSettingObserver(getContext().getMainThreadHandler());
        this.mNavBarSettingObserver = navBarSettingObserver;
        navBarSettingObserver.onChange(true);
        this.mContentResolver.registerContentObserver(Settings.System.getUriFor("op_gesture_button_enabled"), false, this.mNavBarSettingObserver, -1);
        if (bundle != null) {
            this.mDisabledFlags1 = bundle.getInt("disabled_state", 0);
            this.mDisabledFlags2 = bundle.getInt("disabled2_state", 0);
            this.mAppearance = bundle.getInt("appearance", 0);
            this.mTransientShown = bundle.getBoolean("transient_state", false);
            if (Build.DEBUG_ONEPLUS) {
                Log.d("NavigationBar", "NavBar vis get from savedInstance " + Integer.toHexString(this.mAppearance) + ", Status bar vis " + this.mStatusBarLazy.get().getAppearance());
            }
        } else if (this.mStatusBarLazy.get().getAppearance() != 0 && (this.mAppearance == 0 || bundle == null)) {
            this.mAppearance = this.mStatusBarLazy.get().getAppearance();
            Log.d("NavigationBar", "Get mAppearance from status bar " + Integer.toHexString(this.mAppearance));
        }
        this.mAccessibilityManagerWrapper.addCallback(this.mAccessibilityListener);
        this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, false);
        this.mForceNavBarHandleOpaque = DeviceConfig.getBoolean("systemui", "nav_bar_handle_force_opaque", false);
        Handler handler = this.mHandler;
        Objects.requireNonNull(handler);
        DeviceConfig.addOnPropertiesChangedListener("systemui", new Executor(handler) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$LfzJt661qZfn2w-6SYHFbD3aMy0
            public final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.concurrent.Executor
            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        }, this.mOnPropertiesChangedListener);
        onHideNavBar(false);
        this.mOrientation = getContext().getResources().getConfiguration().orientation;
        this.mGestureOnlineConfig = OpSystemUIGestureOnlineConfig.getInstance();
    }

    @Override // com.android.systemui.util.LifecycleFragment, android.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mAccessibilityManagerWrapper.removeCallback(this.mAccessibilityListener);
        this.mContentResolver.unregisterContentObserver(this.mAssistContentObserver);
        DeviceConfig.removeOnPropertiesChangedListener(this.mOnPropertiesChangedListener);
        this.mContentResolver.unregisterContentObserver(this.mNavBarSettingObserver);
        this.mOverviewProxyService.removeCallback(this.mOverviewProxyListener);
        if (getBarTransitions() != null) {
            getBarTransitions().destroy();
        }
    }

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (this.mNavigationBarView == null) {
            return layoutInflater.inflate(C0011R$layout.navigation_bar, viewGroup, false);
        }
        Log.d("NavigationBar", " Return the navigation bar view if it's already created");
        return this.mNavigationBarView;
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mNavigationBarView = (NavigationBarView) view;
        Display display = view.getDisplay();
        if (display != null) {
            int displayId = display.getDisplayId();
            this.mDisplayId = displayId;
            this.mIsOnDefaultDisplay = displayId == 0;
        }
        this.mNavigationBarView.setComponents(this.mStatusBarLazy.get().getPanelController());
        this.mNavigationBarView.setOnVerticalChangedListener(new NavigationBarView.OnVerticalChangedListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$eFJm5m1txtISSi8Cx3m3pc8Nvjw
            @Override // com.android.systemui.statusbar.phone.NavigationBarView.OnVerticalChangedListener
            public final void onVerticalChanged(boolean z) {
                NavigationBarFragment.this.onVerticalChanged(z);
            }
        });
        this.mNavigationBarView.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$X9JO9eLzlFoQkYf8XrZG-l2EMsk
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view2, MotionEvent motionEvent) {
                return NavigationBarFragment.this.onNavigationTouch(view2, motionEvent);
            }
        });
        if (bundle != null) {
            this.mNavigationBarView.getLightTransitionsController().restoreState(bundle);
        }
        this.mNavigationBarView.setNavigationIconHints(this.mNavigationIconHints);
        checkHideNavBarState();
        this.mNavigationBarView.setWindowVisible(isNavBarWindowVisible());
        prepareNavigationBarView();
        checkNavBarModes();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mBroadcastReceiver, intentFilter, Handler.getMain(), UserHandle.ALL);
        notifyNavigationBarScreenOn();
        this.mOverviewProxyService.addCallback(this.mOverviewProxyListener);
        this.mNavigationBarView.setDisabledFlags(this.mDisabledFlags1);
        updateSystemUiStateFlags(-1);
        if (this.mIsOnDefaultDisplay) {
            this.mNavigationBarView.getRotateSuggestionButton().setListener(this.mRotationButtonListener);
            RotationButtonController rotationButtonController = this.mNavigationBarView.getRotationButtonController();
            rotationButtonController.addRotationCallback(this.mRotationWatcher);
            if (display != null && rotationButtonController.isRotationLocked()) {
                rotationButtonController.setRotationLockedAtAngle(display.getRotation());
            }
        } else {
            this.mDisabledFlags2 |= 16;
        }
        setDisabled2Flags(this.mDisabledFlags2);
        if (this.mIsOnDefaultDisplay) {
            this.mAssistHandlerViewController = new AssistHandleViewController(this.mHandler, this.mNavigationBarView);
            getBarTransitions().addDarkIntensityListener(this.mAssistHandlerViewController);
        }
        initSecondaryHomeHandleForRotation();
    }

    @Override // android.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            if (this.mIsOnDefaultDisplay) {
                navigationBarView.getBarTransitions().removeDarkIntensityListener(this.mAssistHandlerViewController);
                this.mAssistHandlerViewController = null;
            }
            this.mNavigationBarView.getBarTransitions().destroy();
            this.mNavigationBarView.getLightTransitionsController().destroy(getContext());
        }
        this.mBroadcastDispatcher.unregisterReceiver(this.mBroadcastReceiver);
        if (this.mOrientationHandle != null) {
            resetSecondaryHandle();
            ((DisplayManager) getContext().getSystemService(DisplayManager.class)).unregisterDisplayListener(this);
            getBarTransitions().removeDarkIntensityListener(this.mOrientationHandleIntensityListener);
            if (this.mOrientationHandle.isAttachedToWindow()) {
                this.mWindowManager.removeView(this.mOrientationHandle);
            }
            this.mOrientationHandle.getViewTreeObserver().removeOnGlobalLayoutListener(this.mOrientationHandleGlobalLayoutListener);
        }
    }

    @Override // android.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("disabled_state", this.mDisabledFlags1);
        bundle.putInt("disabled2_state", this.mDisabledFlags2);
        bundle.putInt("appearance", this.mAppearance);
        bundle.putBoolean("transient_state", this.mTransientShown);
        if (Build.DEBUG_ONEPLUS) {
            Log.d("NavigationBar", "onSaveInstanceState save vis " + Integer.toHexString(this.mAppearance));
        }
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.getLightTransitionsController().saveState(bundle);
        }
    }

    @Override // android.app.Fragment, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (getContext() != null) {
            Locale locale = getContext().getResources().getConfiguration().locale;
            int layoutDirectionFromLocale = TextUtils.getLayoutDirectionFromLocale(locale);
            if (!locale.equals(this.mLocale) || layoutDirectionFromLocale != this.mLayoutDirection) {
                this.mLocale = locale;
                this.mLayoutDirection = layoutDirectionFromLocale;
                refreshLayout(layoutDirectionFromLocale);
            }
            repositionNavigationBar();
            this.mOrientation = configuration.orientation;
            updateNavigationBarTouchableState();
        }
    }

    private void initSecondaryHomeHandleForRotation() {
        if (canShowSecondaryHandle()) {
            ((DisplayManager) getContext().getSystemService(DisplayManager.class)).registerDisplayListener(this, new Handler(Looper.getMainLooper()));
            this.mOrientationHandle = new QuickswitchOrientedNavHandle(getContext());
            getBarTransitions().addDarkIntensityListener(this.mOrientationHandleIntensityListener);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(0, 0, 2024, 545259816, -3);
            this.mOrientationParams = layoutParams;
            layoutParams.setTitle("SecondaryHomeHandle" + getContext().getDisplayId());
            WindowManager.LayoutParams layoutParams2 = this.mOrientationParams;
            layoutParams2.privateFlags = layoutParams2.privateFlags | 64;
            this.mWindowManager.addView(this.mOrientationHandle, layoutParams2);
            this.mOrientationHandle.setVisibility(8);
            this.mOrientationParams.setFitInsetsTypes(0);
            this.mOrientationHandleGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$WBR057y3e1A5lbkY-xhTKGb4TAo
                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public final void onGlobalLayout() {
                    NavigationBarFragment.this.lambda$initSecondaryHomeHandleForRotation$3$NavigationBarFragment();
                }
            };
            this.mOrientationHandle.getViewTreeObserver().addOnGlobalLayoutListener(this.mOrientationHandleGlobalLayoutListener);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initSecondaryHomeHandleForRotation$3 */
    public /* synthetic */ void lambda$initSecondaryHomeHandleForRotation$3$NavigationBarFragment() {
        if (this.mStartingQuickSwitchRotation != -1) {
            RectF computeHomeHandleBounds = this.mOrientationHandle.computeHomeHandleBounds();
            this.mOrientationHandle.mapRectFromViewToScreenCoords(computeHomeHandleBounds, true);
            Rect rect = new Rect();
            computeHomeHandleBounds.roundOut(rect);
            this.mNavigationBarView.setOrientedHandleSamplingRegion(rect);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0090  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void orientSecondaryHomeHandle() {
        /*
            r7 = this;
            boolean r0 = r7.canShowSecondaryHandle()
            if (r0 != 0) goto L_0x0007
            return
        L_0x0007:
            int r0 = r7.mStartingQuickSwitchRotation
            r1 = -1
            if (r0 == r1) goto L_0x00b0
            com.android.systemui.stackdivider.Divider r0 = r7.mDivider
            boolean r0 = r0.isDividerVisible()
            if (r0 == 0) goto L_0x0016
            goto L_0x00b0
        L_0x0016:
            int r0 = r7.mCurrentRotation
            int r2 = r7.mStartingQuickSwitchRotation
            int r0 = r7.deltaRotation(r0, r2)
            int r2 = r7.mStartingQuickSwitchRotation
            if (r2 == r1) goto L_0x0024
            if (r0 != r1) goto L_0x004e
        L_0x0024:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "secondary nav delta rotation: "
            r1.append(r2)
            r1.append(r0)
            java.lang.String r2 = " current: "
            r1.append(r2)
            int r2 = r7.mCurrentRotation
            r1.append(r2)
            java.lang.String r2 = " starting: "
            r1.append(r2)
            int r2 = r7.mStartingQuickSwitchRotation
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "NavigationBar"
            android.util.Log.d(r2, r1)
        L_0x004e:
            android.view.WindowManager r1 = r7.mWindowManager
            android.view.WindowMetrics r1 = r1.getCurrentWindowMetrics()
            android.graphics.Rect r1 = r1.getBounds()
            com.android.systemui.statusbar.phone.QuickswitchOrientedNavHandle r2 = r7.mOrientationHandle
            r2.setDeltaRotation(r0)
            r2 = 3
            r3 = 1
            r4 = 0
            if (r0 == 0) goto L_0x0077
            if (r0 == r3) goto L_0x006c
            r5 = 2
            if (r0 == r5) goto L_0x0077
            if (r0 == r2) goto L_0x006c
            r1 = r4
            r5 = r1
            goto L_0x0089
        L_0x006c:
            int r1 = r1.height()
            com.android.systemui.statusbar.phone.NavigationBarView r5 = r7.mNavigationBarView
            int r5 = r5.getHeight()
            goto L_0x0089
        L_0x0077:
            boolean r5 = r7.mShowOrientedHandleForImmersiveMode
            if (r5 != 0) goto L_0x007f
            r7.resetSecondaryHandle()
            return
        L_0x007f:
            int r5 = r1.width()
            com.android.systemui.statusbar.phone.NavigationBarView r1 = r7.mNavigationBarView
            int r1 = r1.getHeight()
        L_0x0089:
            android.view.WindowManager$LayoutParams r6 = r7.mOrientationParams
            if (r0 != 0) goto L_0x0090
            r2 = 80
            goto L_0x0094
        L_0x0090:
            if (r0 != r3) goto L_0x0093
            goto L_0x0094
        L_0x0093:
            r2 = 5
        L_0x0094:
            r6.gravity = r2
            android.view.WindowManager$LayoutParams r0 = r7.mOrientationParams
            r0.height = r1
            r0.width = r5
            android.view.WindowManager r1 = r7.mWindowManager
            com.android.systemui.statusbar.phone.QuickswitchOrientedNavHandle r2 = r7.mOrientationHandle
            r1.updateViewLayout(r2, r0)
            com.android.systemui.statusbar.phone.NavigationBarView r0 = r7.mNavigationBarView
            r1 = 8
            r0.setVisibility(r1)
            com.android.systemui.statusbar.phone.QuickswitchOrientedNavHandle r7 = r7.mOrientationHandle
            r7.setVisibility(r4)
            goto L_0x00b3
        L_0x00b0:
            r7.resetSecondaryHandle()
        L_0x00b3:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavigationBarFragment.orientSecondaryHomeHandle():void");
    }

    private void resetSecondaryHandle() {
        QuickswitchOrientedNavHandle quickswitchOrientedNavHandle = this.mOrientationHandle;
        if (quickswitchOrientedNavHandle != null) {
            quickswitchOrientedNavHandle.setVisibility(8);
        }
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.setVisibility(0);
            this.mNavigationBarView.setOrientedHandleSamplingRegion(null);
        }
    }

    @Override // android.app.Fragment
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (this.mNavigationBarView != null) {
            printWriter.print("  mNavigationBarWindowState=");
            printWriter.println(StatusBarManager.windowStateToString(this.mNavigationBarWindowState));
            printWriter.print("  mNavigationBarMode=");
            printWriter.println(BarTransitions.modeToString(this.mNavigationBarMode));
            StatusBar.dumpBarTransitions(printWriter, "mNavigationBarView", this.mNavigationBarView.getBarTransitions());
        }
        printWriter.print("  mStartingQuickSwitchRotation=" + this.mStartingQuickSwitchRotation);
        printWriter.print("  mCurrentRotation=" + this.mCurrentRotation);
        printWriter.print("  mNavigationBarView=");
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView == null) {
            printWriter.println("null");
        } else {
            navigationBarView.dump(fileDescriptor, printWriter, strArr);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0042, code lost:
        if (r6 != 3) goto L_0x004c;
     */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004e  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0050  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0056 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0057  */
    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setImeWindowStatus(int r3, android.os.IBinder r4, int r5, int r6, boolean r7) {
        /*
            r2 = this;
            int r4 = r2.mDisplayId
            if (r3 == r4) goto L_0x0005
            return
        L_0x0005:
            r3 = 2
            r4 = r5 & 2
            r5 = 1
            if (r4 == 0) goto L_0x000d
            r4 = r5
            goto L_0x000e
        L_0x000d:
            r4 = 0
        L_0x000e:
            boolean r0 = r2.mImeShow
            if (r0 == r4) goto L_0x0017
            r2.mImeShow = r4
            r2.byPassRegionSamplingHelperThreshold()
        L_0x0017:
            boolean r0 = android.os.Build.DEBUG_ONEPLUS
            if (r0 == 0) goto L_0x0039
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "imeShown "
            r0.append(r1)
            r0.append(r4)
            java.lang.String r1 = ", showImeSwitcher "
            r0.append(r1)
            r0.append(r7)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "NavigationBar"
            android.util.Log.d(r1, r0)
        L_0x0039:
            int r0 = r2.mNavigationIconHints
            if (r6 == 0) goto L_0x0048
            if (r6 == r5) goto L_0x0048
            if (r6 == r3) goto L_0x0048
            r4 = 3
            if (r6 == r4) goto L_0x0045
            goto L_0x004c
        L_0x0045:
            r0 = r0 & -2
            goto L_0x004c
        L_0x0048:
            if (r4 == 0) goto L_0x0045
            r0 = r0 | 1
        L_0x004c:
            if (r7 == 0) goto L_0x0050
            r3 = r3 | r0
            goto L_0x0052
        L_0x0050:
            r3 = r0 & -3
        L_0x0052:
            int r4 = r2.mNavigationIconHints
            if (r3 != r4) goto L_0x0057
            return
        L_0x0057:
            r2.mNavigationIconHints = r3
            com.android.systemui.statusbar.phone.NavigationBarView r4 = r2.mNavigationBarView
            if (r4 == 0) goto L_0x0060
            r4.setNavigationIconHints(r3)
        L_0x0060:
            r2.checkBarModes()
            r2.updateNavigationBarTouchableState()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavigationBarFragment.setImeWindowStatus(int, android.os.IBinder, int, int, boolean):void");
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setWindowState(int i, int i2, int i3) {
        if (i == this.mDisplayId && i2 == 2 && this.mNavigationBarWindowState != i3) {
            this.mNavigationBarWindowState = i3;
            updateSystemUiStateFlags(-1);
            this.mShowOrientedHandleForImmersiveMode = i3 == 2;
            if (!(this.mOrientationHandle == null || this.mStartingQuickSwitchRotation == -1)) {
                orientSecondaryHomeHandle();
            }
            Log.d("NavigationBar", "Navigation bar " + StatusBarManager.windowStateToString(i3));
            NavigationBarView navigationBarView = this.mNavigationBarView;
            if (navigationBarView != null) {
                navigationBarView.setWindowVisible(isNavBarWindowVisible());
            }
            this.mBackupNavBarMode = this.mNavBarMode;
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onRotationProposal(int i, boolean z) {
        int rotation = this.mNavigationBarView.getDisplay().getRotation();
        boolean hasDisable2RotateSuggestionFlag = RotationButtonController.hasDisable2RotateSuggestionFlag(this.mDisabledFlags2);
        RotationButtonController rotationButtonController = this.mNavigationBarView.getRotationButtonController();
        rotationButtonController.getRotationButton();
        if (!hasDisable2RotateSuggestionFlag) {
            rotationButtonController.onRotationProposal(i, rotation, z);
        }
    }

    public void restoreAppearanceAndTransientState() {
        int barMode = barMode(this.mTransientShown, this.mAppearance);
        this.mNavigationBarMode = barMode;
        checkNavBarModes();
        this.mAutoHideController.touchAutoHide();
        if (Build.DEBUG_ONEPLUS) {
            Log.d("NavigationBar", "restoreSystemUiVisibilityState: vis " + Integer.toHexString(this.mAppearance) + " mode " + barMode);
        }
        this.mLightBarController.onNavigationBarAppearanceChanged(this.mAppearance, true, barMode, false);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        NavigationBarView navigationBarView;
        if (i == this.mDisplayId) {
            boolean z2 = false;
            if (this.mAppearance != i2) {
                this.mAppearance = i2;
                if (getView() != null) {
                    boolean updateBarMode = updateBarMode(barMode(this.mTransientShown, i2));
                    if (OpNavBarUtils.isSupportHideNavBar() && (navigationBarView = this.mNavigationBarView) != null) {
                        if ((i2 & 4098) != 0) {
                            navigationBarView.onImmersiveSticky(true);
                        } else {
                            navigationBarView.onImmersiveSticky(false);
                        }
                    }
                    z2 = updateBarMode;
                } else {
                    return;
                }
            }
            if (Build.DEBUG_ONEPLUS) {
                Log.d("NavigationBar", "onSystemBarAppearanceChanged: appearance=" + Integer.toHexString(i2) + ", mode=" + this.mNavigationBarMode);
            }
            this.mLightBarController.onNavigationBarAppearanceChanged(i2, z2, this.mNavigationBarMode, z);
            byPassRegionSamplingHelperThreshold();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 1) && !this.mTransientShown) {
            this.mTransientShown = true;
            handleTransientChanged();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void abortTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 1)) {
            clearTransient();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearTransient() {
        if (this.mTransientShown) {
            this.mTransientShown = false;
            handleTransientChanged();
        }
    }

    private void handleTransientChanged() {
        if (getView() != null) {
            NavigationBarView navigationBarView = this.mNavigationBarView;
            if (navigationBarView != null) {
                navigationBarView.onTransientStateChanged(this.mTransientShown);
            }
            int barMode = barMode(this.mTransientShown, this.mAppearance);
            if (updateBarMode(barMode)) {
                this.mLightBarController.onNavigationBarModeChanged(barMode);
            }
        }
    }

    private boolean updateBarMode(int i) {
        int i2 = this.mNavigationBarMode;
        if (i2 == i) {
            return false;
        }
        if (i2 == 0 || i2 == 6) {
            this.mNavigationBarView.hideRecentsOnboarding();
        }
        this.mNavigationBarMode = i;
        checkNavBarModes();
        this.mAutoHideController.touchAutoHide();
        return true;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        int i4;
        if (i == this.mDisplayId) {
            int i5 = 56623104 & i2;
            if (i5 != this.mDisabledFlags1) {
                this.mDisabledFlags1 = i5;
                NavigationBarView navigationBarView = this.mNavigationBarView;
                if (navigationBarView != null) {
                    navigationBarView.setDisabledFlags(i2);
                }
                updateScreenPinningGestures();
            }
            if (this.mIsOnDefaultDisplay && (i4 = i3 & 16) != this.mDisabledFlags2) {
                this.mDisabledFlags2 = i4;
                setDisabled2Flags(i4);
            }
        }
    }

    private void setDisabled2Flags(int i) {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.getRotationButtonController().onDisable2FlagChanged(i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshLayout(int i) {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.setLayoutDirection(i);
        }
    }

    private boolean shouldDisableNavbarGestures() {
        return !this.mDeviceProvisionedController.isDeviceProvisioned() || (this.mDisabledFlags1 & 33554432) != 0;
    }

    private void repositionNavigationBar() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null && navigationBarView.isAttachedToWindow()) {
            prepareNavigationBarView();
            this.mWindowManager.updateViewLayout((View) this.mNavigationBarView.getParent(), ((View) this.mNavigationBarView.getParent()).getLayoutParams());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateScreenPinningGestures() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            boolean isRecentsButtonVisible = navigationBarView.isRecentsButtonVisible();
            ButtonDispatcher backButton = this.mNavigationBarView.getBackButton();
            if (isRecentsButtonVisible) {
                backButton.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$dtGeJfWz2E4_XAoQgX8peIw4kU8
                    @Override // android.view.View.OnLongClickListener
                    public final boolean onLongClick(View view) {
                        return NavigationBarFragment.this.onLongPressBackRecents(view);
                    }
                });
            } else {
                backButton.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$oZtQ9jE1OTI8AtitIxsN6ETT4sc
                    @Override // android.view.View.OnLongClickListener
                    public final boolean onLongClick(View view) {
                        return NavigationBarFragment.this.onLongPressBackHome(view);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyNavigationBarScreenOn() {
        this.mNavigationBarView.updateNavButtonIcons();
    }

    private void prepareNavigationBarView() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.reorient();
            if (OpNavBarUtils.isSupportHideNavBar()) {
                this.mNavigationBarView.getNavButton().setOnClickListener(this.mNavActionListener);
            }
            ButtonDispatcher recentsButton = this.mNavigationBarView.getRecentsButton();
            recentsButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$0mmLLxBq7RxotphHQB_RtYb4SpQ
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    NavigationBarFragment.this.onRecentsClick(view);
                }
            });
            recentsButton.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$VEqqEZFjg0f3lWOW2BJ66Oo_2aE
                @Override // android.view.View.OnTouchListener
                public final boolean onTouch(View view, MotionEvent motionEvent) {
                    return NavigationBarFragment.this.onRecentsTouch(view, motionEvent);
                }
            });
            recentsButton.setLongClickable(true);
            recentsButton.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$dtGeJfWz2E4_XAoQgX8peIw4kU8
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return NavigationBarFragment.this.onLongPressBackRecents(view);
                }
            });
            this.mNavigationBarView.getBackButton().setLongClickable(true);
            ButtonDispatcher homeButton = this.mNavigationBarView.getHomeButton();
            homeButton.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$y_1OHmWTpLl8uCcO3A0Am620g94
                @Override // android.view.View.OnTouchListener
                public final boolean onTouch(View view, MotionEvent motionEvent) {
                    return NavigationBarFragment.this.onHomeTouch(view, motionEvent);
                }
            });
            homeButton.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$8vcstZEv0YyG7EUTK_UrsNSFXRo
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return NavigationBarFragment.this.onHomeLongClick(view);
                }
            });
            ButtonDispatcher accessibilityButton = this.mNavigationBarView.getAccessibilityButton();
            accessibilityButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$Ylizyb5K7ZQr77j1Ehc8SUjcI6E
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    NavigationBarFragment.this.onAccessibilityClick(view);
                }
            });
            accessibilityButton.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$RtBTLxltRKo37YrTKiaCXCxwRDg
                @Override // android.view.View.OnLongClickListener
                public final boolean onLongClick(View view) {
                    return NavigationBarFragment.this.onAccessibilityLongClick(view);
                }
            });
            updateAccessibilityServicesState(this.mAccessibilityManager);
            updateScreenPinningGestures();
        }
    }

    /* access modifiers changed from: private */
    public boolean onHomeTouch(View view, MotionEvent motionEvent) {
        if (this.mHomeBlockedThisTouch && motionEvent.getActionMasked() != 0) {
            return true;
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mHomeBlockedThisTouch = false;
            TelecomManager telecomManager = (TelecomManager) getContext().getSystemService(TelecomManager.class);
            if (telecomManager != null && telecomManager.isRinging() && this.mStatusBarLazy.get().isKeyguardShowing()) {
                Log.i("NavigationBar", "Ignoring HOME; there's a ringing incoming call. No heads up");
                this.mHomeBlockedThisTouch = true;
                return true;
            }
        } else if (action == 1 || action == 3) {
            this.mStatusBarLazy.get().awakenDreams();
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void onVerticalChanged(boolean z) {
        this.mStatusBarLazy.get().setQsScrimEnabled(!z);
    }

    /* access modifiers changed from: private */
    public boolean onNavigationTouch(View view, MotionEvent motionEvent) {
        this.mAutoHideController.checkUserAutoHide(motionEvent);
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean onHomeLongClick(View view) {
        if (!this.mNavigationBarView.isRecentsButtonVisible() && ActivityManagerWrapper.getInstance().isScreenPinningActive()) {
            return onLongPressBackHome(view);
        }
        if (shouldDisableNavbarGestures()) {
            return false;
        }
        this.mMetricsLogger.action(239);
        this.mUiEventLogger.log(NavBarActionEvent.NAVBAR_ASSIST_LONGPRESS);
        Bundle bundle = new Bundle();
        bundle.putInt("invocation_type", 5);
        this.mAssistManager.startAssist(bundle);
        this.mStatusBarLazy.get().awakenDreams();
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView == null) {
            return true;
        }
        navigationBarView.abortCurrentGesture();
        return true;
    }

    /* access modifiers changed from: private */
    public boolean onRecentsTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction() & 255;
        if (action == 0) {
            this.mCommandQueue.preloadRecentApps();
            return false;
        } else if (action == 3) {
            this.mCommandQueue.cancelPreloadRecentApps();
            return false;
        } else if (action != 1 || view.isPressed()) {
            return false;
        } else {
            this.mCommandQueue.cancelPreloadRecentApps();
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void onRecentsClick(View view) {
        if (LatencyTracker.isEnabled(getContext())) {
            LatencyTracker.getInstance(getContext()).onActionStart(1);
        }
        this.mStatusBarLazy.get().awakenDreams();
        this.mCommandQueue.toggleRecentApps();
    }

    /* access modifiers changed from: private */
    public boolean onLongPressBackHome(View view) {
        return onLongPressNavigationButtons(view, C0008R$id.back, C0008R$id.home);
    }

    /* access modifiers changed from: private */
    public boolean onLongPressBackRecents(View view) {
        return onLongPressNavigationButtons(view, C0008R$id.back, C0008R$id.recent_apps);
    }

    private boolean onLongPressNavigationButtons(View view, int i, int i2) {
        boolean z;
        ButtonDispatcher buttonDispatcher;
        try {
            IActivityTaskManager service = ActivityTaskManager.getService();
            boolean isTouchExplorationEnabled = this.mAccessibilityManager.isTouchExplorationEnabled();
            boolean isInLockTaskMode = service.isInLockTaskMode();
            if (isInLockTaskMode && !isTouchExplorationEnabled) {
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - this.mLastLockToAppLongPress < 200) {
                    service.stopSystemLockTaskMode();
                    this.mNavigationBarView.updateNavButtonIcons();
                    return true;
                }
                if (view.getId() == i) {
                    if (i2 == C0008R$id.recent_apps) {
                        buttonDispatcher = this.mNavigationBarView.getRecentsButton();
                    } else {
                        buttonDispatcher = this.mNavigationBarView.getHomeButton();
                    }
                    if (!buttonDispatcher.getCurrentView().isPressed()) {
                        z = true;
                        this.mLastLockToAppLongPress = currentTimeMillis;
                    }
                }
                z = false;
                this.mLastLockToAppLongPress = currentTimeMillis;
            } else if (view.getId() == i) {
                z = true;
            } else if (isTouchExplorationEnabled && isInLockTaskMode) {
                service.stopSystemLockTaskMode();
                this.mNavigationBarView.updateNavButtonIcons();
                return true;
            } else if (view.getId() != i2) {
                z = false;
            } else if (i2 == C0008R$id.recent_apps) {
                return onLongPressRecents();
            } else {
                return onHomeLongClick(this.mNavigationBarView.getHomeButton().getCurrentView());
            }
            if (z) {
                KeyButtonView keyButtonView = (KeyButtonView) view;
                keyButtonView.sendEvent(0, 128);
                keyButtonView.sendAccessibilityEvent(2);
                return true;
            }
        } catch (RemoteException e) {
            Log.d("NavigationBar", "Unable to reach activity manager", e);
        }
        return false;
    }

    private boolean onLongPressRecents() {
        if (this.mRecentsOptional.isPresent() || !ActivityTaskManager.supportsMultiWindow(getContext()) || !this.mDivider.getView().getSnapAlgorithm().isSplitScreenFeasible() || ActivityManager.isLowRamDeviceStatic() || this.mOverviewProxyService.getProxy() != null) {
            return false;
        }
        return this.mStatusBarLazy.get().toggleSplitScreenMode(271, 286);
    }

    /* access modifiers changed from: private */
    public void onAccessibilityClick(View view) {
        Display display = view.getDisplay();
        this.mAccessibilityManager.notifyAccessibilityButtonClicked(display != null ? display.getDisplayId() : 0);
    }

    /* access modifiers changed from: private */
    public boolean onAccessibilityLongClick(View view) {
        Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
        intent.addFlags(268468224);
        intent.setClassName("android", AccessibilityButtonChooserActivity.class.getName());
        view.getContext().startActivityAsUser(intent, UserHandle.CURRENT);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void updateAccessibilityServicesState(AccessibilityManager accessibilityManager) {
        boolean z = true;
        int a11yButtonState = getA11yButtonState(new boolean[1]);
        boolean z2 = (a11yButtonState & 16) != 0;
        if ((a11yButtonState & 32) == 0) {
            z = false;
        }
        this.mNavigationBarView.setAccessibilityButtonState(z2, z);
        updateSystemUiStateFlags(a11yButtonState);
    }

    public void updateSystemUiStateFlags(int i) {
        if (i < 0) {
            i = getA11yButtonState(null);
        }
        boolean z = false;
        boolean z2 = (i & 16) != 0;
        boolean z3 = (i & 32) != 0;
        boolean z4 = (this.mStatusBarLazy.get().getNavigationBarHiddenMode() == 1 || !EdgeBackGestureHandler.sSideGestureEnabled) && QuickStepContract.isGesturalMode(this.mNavBarMode);
        SysUiState sysUiState = this.mSysUiFlagsContainer;
        sysUiState.setFlag(16, z2);
        sysUiState.setFlag(32, z3);
        if (!isNavBarWindowVisible() && !z4) {
            z = true;
        }
        sysUiState.setFlag(2, z);
        sysUiState.commitUpdate(this.mDisplayId);
        registerAction(z2, 11);
        registerAction(z3, 12);
    }

    private void registerAction(boolean z, int i) {
        if (z) {
            this.mSystemActions.register(i);
        } else {
            this.mSystemActions.unregister(i);
        }
    }

    public int getA11yButtonState(boolean[] zArr) {
        int i;
        List<AccessibilityServiceInfo> enabledAccessibilityServiceList = this.mAccessibilityManager.getEnabledAccessibilityServiceList(-1);
        int i2 = 0;
        int size = this.mAccessibilityManager.getAccessibilityShortcutTargets(0).size();
        int size2 = enabledAccessibilityServiceList.size() - 1;
        boolean z = false;
        while (true) {
            i = 16;
            if (size2 < 0) {
                break;
            }
            int i3 = enabledAccessibilityServiceList.get(size2).feedbackType;
            if (!(i3 == 0 || i3 == 16)) {
                z = true;
            }
            size2--;
        }
        if (zArr != null) {
            zArr[0] = z;
        }
        if (size < 1) {
            i = 0;
        }
        if (size >= 2) {
            i2 = 32;
        }
        return i | i2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAssistantAvailability(boolean z) {
        if (this.mOverviewProxyService.getProxy() != null) {
            try {
                this.mOverviewProxyService.getProxy().onAssistantAvailable(z && QuickStepContract.isGesturalMode(this.mNavBarMode));
            } catch (RemoteException unused) {
                Log.w("NavigationBar", "Unable to send assistant availability data to launcher");
            }
        }
    }

    public void touchAutoDim() {
        getBarTransitions().setAutoDim(false);
        this.mHandler.removeCallbacks(this.mAutoDim);
        int state = this.mStatusBarStateController.getState();
        if (state != 1 && state != 2) {
            this.mHandler.postDelayed(this.mAutoDim, 2250);
        }
    }

    public void setLightBarController(LightBarController lightBarController) {
        this.mLightBarController = lightBarController;
        lightBarController.setNavigationBar(this.mNavigationBarView.getLightTransitionsController());
    }

    public void setAutoHideController(AutoHideController autoHideController) {
        this.mAutoHideController = autoHideController;
        if (autoHideController != null) {
            autoHideController.setNavigationBar(this.mAutoHideUiElement);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTransientShown() {
        return this.mTransientShown;
    }

    private void checkBarModes() {
        if (this.mIsOnDefaultDisplay) {
            this.mStatusBarLazy.get().checkBarModes();
        } else {
            checkNavBarModes();
        }
    }

    public boolean isNavBarWindowVisible() {
        OpUtils.setNavigationBarShowing(this.mNavBarMode, this.mNavigationBarWindowState == 0);
        return this.mNavigationBarWindowState == 0;
    }

    public void checkNavBarModes() {
        int i;
        NavigationBarView navigationBarView;
        boolean z = this.mStatusBarLazy.get().isDeviceInteractive() && this.mNavigationBarWindowState != 2;
        boolean z2 = this.mImeShow && QuickStepContract.isGesturalMode(this.mNavBarMode);
        boolean z3 = z2 && this.mNavigationBarMode == 4 && this.mGestureOnlineConfig.isUseNativeOpaqueColor(OpUtils.getTopPackageName());
        if (Build.DEBUG_ONEPLUS) {
            Log.d("NavigationBar", "checkNavBarModes isImeShowOnGestureMode=" + z2 + ", mNavigationBarMode=" + this.mNavigationBarMode + ", UseNativeOpaqueColor=" + this.mGestureOnlineConfig.isUseNativeOpaqueColor(OpUtils.getTopPackageName()));
        }
        NavigationBarTransitions barTransitions = this.mNavigationBarView.getBarTransitions();
        if (z3) {
            i = 0;
        } else {
            i = this.mNavigationBarMode;
        }
        barTransitions.transitionTo(i, z);
        if (OpNavBarUtils.isSupportCustomNavBar() && !QuickStepContract.isGesturalMode(this.mNavBarMode) && (navigationBarView = this.mNavigationBarView) != null) {
            int i2 = this.mNavigationBarMode;
            if (i2 == 1 || i2 == 2) {
                this.mNavigationBarView.notifyNavBarColorChange(0);
            } else {
                navigationBarView.notifyNavBarColorChange(this.mNavigationBarColor);
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("NavigationBar", "onNavigationModeChanged mode from " + this.mNavBarMode + " to " + i);
        }
        this.mNavBarMode = i;
        updateScreenPinningGestures();
        boolean z = false;
        z = false;
        OpUtils.setNavigationBarShowing(this.mNavBarMode, this.mNavigationBarWindowState == 0);
        if (!canShowSecondaryHandle()) {
            resetSecondaryHandle();
        }
        if (ActivityManagerWrapper.getInstance().getCurrentUserId() != 0) {
            this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$XEk0w8DzgaQF2WOx5moeKvCIr5U
                @Override // java.lang.Runnable
                public final void run() {
                    NavigationBarFragment.this.lambda$onNavigationModeChanged$4$NavigationBarFragment();
                }
            });
        }
        if ((this.mStatusBarLazy.get().getNavigationBarHiddenMode() == 1 || !EdgeBackGestureHandler.sSideGestureEnabled) && QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            z = true;
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.d("NavigationBar", "onNavigationModeChanged navBarHidden " + z);
        }
        Settings.System.putInt(sContext.getContentResolver(), "buttons_show_on_screen_navkeys", !z ? 1 : 0);
        this.mHandler.removeCallbacks(this.mCheckNavigationBarState);
        this.mHandler.postDelayed(this.mCheckNavigationBarState, 1000);
        updateNavigationBarTouchableState();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onNavigationModeChanged$4 */
    public /* synthetic */ void lambda$onNavigationModeChanged$4$NavigationBarFragment() {
        FragmentHostManager.get(this.mNavigationBarView).reloadFragments();
    }

    public void disableAnimationsDuringHide(long j) {
        this.mNavigationBarView.setLayoutTransitionsEnabled(false);
        this.mNavigationBarView.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$NavigationBarFragment$j_WZTHSZ47fEKh7hZbvsbut1wMg
            @Override // java.lang.Runnable
            public final void run() {
                NavigationBarFragment.this.lambda$disableAnimationsDuringHide$5$NavigationBarFragment();
            }
        }, j + 448);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$disableAnimationsDuringHide$5 */
    public /* synthetic */ void lambda$disableAnimationsDuringHide$5$NavigationBarFragment() {
        this.mNavigationBarView.setLayoutTransitionsEnabled(true);
    }

    public AssistHandleViewController getAssistHandlerViewController() {
        return this.mAssistHandlerViewController;
    }

    public void transitionTo(int i, boolean z) {
        getBarTransitions().transitionTo(i, z);
    }

    public NavigationBarTransitions getBarTransitions() {
        return this.mNavigationBarView.getBarTransitions();
    }

    public void finishBarAnimations() {
        this.mNavigationBarView.getBarTransitions().finishAnimations();
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayChanged(int i) {
        int rotation;
        if (canShowSecondaryHandle() && (rotation = getContext().getResources().getConfiguration().windowConfiguration.getRotation()) != this.mCurrentRotation) {
            this.mCurrentRotation = rotation;
            orientSecondaryHomeHandle();
        }
    }

    private boolean canShowSecondaryHandle() {
        return this.mNavBarMode == 2;
    }

    private class NavBarSettingObserver extends ContentObserver {
        public NavBarSettingObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("NavigationBar", "mNavBarSettingObserver onChange");
            }
            if (NavigationBarFragment.this.mContentResolver != null) {
                boolean z2 = true;
                boolean z3 = !(NavigationBarFragment.this.mOverviewProxyService != null ? NavigationBarFragment.this.mOverviewProxyService.shouldShowSwipeUpUI() : false) && QuickStepContract.isLegacyMode(NavigationBarFragment.this.mNavBarMode) && Settings.System.getInt(NavigationBarFragment.this.mContentResolver, "op_gesture_button_enabled", 0) == 1;
                NavigationBarView navigationBarView = NavigationBarFragment.this.mNavigationBarView;
                if (navigationBarView != null) {
                    navigationBarView.updateNavButtonState(z3);
                }
                if (!z3) {
                    Settings.System.putInt(NavigationBarFragment.this.mContentResolver, "systemui_navigation_bar_hided", 0);
                    NavigationBarFragment.this.mHideNavBar = false;
                } else {
                    NavigationBarFragment navigationBarFragment = NavigationBarFragment.this;
                    if (Settings.System.getInt(navigationBarFragment.mContentResolver, "systemui_navigation_bar_hided", 0) != 1) {
                        z2 = false;
                    }
                    navigationBarFragment.mHideNavBar = z2;
                }
                NavigationBarFragment.this.onHideNavBar(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$6 */
    public /* synthetic */ void lambda$new$6$NavigationBarFragment(Integer num) {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null && navigationBarView.needsReorient(num.intValue())) {
            repositionNavigationBar();
        }
    }

    public static View create(Context context, final FragmentHostManager.FragmentListener fragmentListener) {
        sContext = context;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2019, 545521768, -3);
        layoutParams.token = new Binder();
        layoutParams.setTitle("NavigationBar" + context.getDisplayId());
        layoutParams.accessibilityTitle = context.getString(C0015R$string.nav_bar);
        layoutParams.windowAnimations = 0;
        layoutParams.privateFlags = layoutParams.privateFlags | 16777216;
        View inflate = LayoutInflater.from(context).inflate(C0011R$layout.navigation_bar_window, (ViewGroup) null);
        if (inflate == null) {
            return null;
        }
        inflate.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() { // from class: com.android.systemui.statusbar.phone.NavigationBarFragment.9
            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View view) {
                FragmentHostManager fragmentHostManager = FragmentHostManager.get(view);
                fragmentHostManager.getFragmentManager().beginTransaction().replace(C0008R$id.navigation_bar_frame, NavigationBarFragment.this, "NavigationBar").commit();
                fragmentHostManager.addTagListener("NavigationBar", fragmentListener);
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View view) {
                FragmentHostManager.removeAndDestroy(view);
            }
        });
        ((WindowManager) context.getSystemService(WindowManager.class)).addView(inflate, layoutParams);
        return inflate;
    }

    /* access modifiers changed from: package-private */
    public int getNavigationIconHints() {
        return this.mNavigationIconHints;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void notifyNavBarColorChanged(int i, String str, String str2) {
        if (OpNavBarUtils.isSupportCustomNavBar()) {
            this.mNavigationBarColor = i;
            OpUtils.updateTopPackage(sContext, str, str2);
            if (this.mNavigationBarView != null && !QuickStepContract.isGesturalMode(this.mNavBarMode)) {
                this.mNavigationBarView.notifyNavBarColorChange(i);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onHideNavBar(boolean z) {
        boolean z2 = this.mHideNavBar && !z;
        this.mStatusBarLazy.get().onHideNavBar(z2);
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.setHideNavBarOn(z2);
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.d("NavigationBar", "onHideNavBar mHideNavBar=" + this.mHideNavBar + " forceShow=" + z);
        }
    }

    public void onBrickModeChanged(boolean z) {
        this.mIsInBrickMode = z;
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.onBrickModeChanged(z);
        }
        onHideNavBar(z);
    }

    private void checkHideNavBarState() {
        int i = this.mBackupNavBarMode;
        int i2 = this.mNavBarMode;
        if (i != i2 && QuickStepContract.isGesturalMode(i2) && !isNavBarWindowVisible()) {
            Log.d("NavigationBar", "checkHideNavBarState: It's gesture mode. Reset state to showing.");
            this.mNavigationBarWindowState = 0;
        }
    }

    private void updateNavigationBarTouchableState() {
        View navView = ((NavigationBarController) Dependency.get(NavigationBarController.class)).getNavView();
        if (navView != null && navView.isAttachedToWindow() && QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            Log.d("NavigationBar", " Set navigation bar not touchable for gesture mode.");
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) navView.getLayoutParams();
            if (!this.mImeShow || this.mOrientation == 2 || this.mStatusBarLazy.get().isHideImeBackAndSwitcher()) {
                int i = layoutParams.flags & -65;
                layoutParams.flags = i;
                layoutParams.flags = i | 16;
            } else {
                Log.d("NavigationBar", " Enable navigation bar touch when IME showing.");
                int i2 = layoutParams.flags & -17;
                layoutParams.flags = i2;
                layoutParams.flags = i2 | 64;
            }
            WindowManager windowManager = this.mWindowManager;
            if (windowManager != null) {
                windowManager.updateViewLayout(navView, layoutParams);
            }
        } else if (navView != null && navView.isAttachedToWindow()) {
            Log.d("NavigationBar", " Set navigation bar touchable when for 3-button mode");
            WindowManager.LayoutParams layoutParams2 = (WindowManager.LayoutParams) navView.getLayoutParams();
            int i3 = layoutParams2.flags & -17;
            layoutParams2.flags = i3;
            layoutParams2.flags = i3 | 64;
            WindowManager windowManager2 = this.mWindowManager;
            if (windowManager2 != null) {
                windowManager2.updateViewLayout(navView, layoutParams2);
            }
        }
    }

    private void byPassRegionSamplingHelperThreshold() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.getRegionSamplingHelper().setByPassThreshold(true);
            this.mHandler.removeCallbacks(this.mCloseByPassThreshold);
            this.mHandler.postDelayed(this.mCloseByPassThreshold, 2000);
        }
    }
}
