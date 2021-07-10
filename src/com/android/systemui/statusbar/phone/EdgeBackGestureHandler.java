package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.os.Build;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.DeviceConfig;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.OpFeatures;
import android.util.TypedValue;
import android.view.Display;
import android.view.ISystemGestureExclusionListener;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputMonitor;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.policy.GestureNavigationSettingsObserver;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.assist.ui.DisplayUtils;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.NavigationEdgeBackPlugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.shared.tracing.ProtoTraceable;
import com.android.systemui.statusbar.phone.EdgeBackGestureHandler;
import com.android.systemui.tracing.ProtoTracer;
import com.android.systemui.tracing.nano.EdgeBackGestureHandlerProto;
import com.android.systemui.tracing.nano.SystemUiTraceProto;
import com.oneplus.onlineconfig.OpSystemUIGestureOnlineConfig;
import com.oneplus.phone.GesturePointContainer;
import com.oneplus.phone.OpSideGestureConfiguration;
import com.oneplus.phone.OpSideGestureNavView;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.OpSystemUIInjector;
import com.oneplus.systemui.statusbar.phone.OpGestureButtonViewController;
import com.oneplus.systemui.statusbar.phone.OpOneHandModeController;
import com.oneplus.util.OpUtils;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
public class EdgeBackGestureHandler extends CurrentUserTracker implements DisplayManager.DisplayListener, PluginListener<NavigationEdgeBackPlugin>, ProtoTraceable<SystemUiTraceProto>, OpOneHandModeController.OneHandModeStateListener {
    private static final boolean DEBUG_GESTURE = SystemProperties.getBoolean("persist.gesture_button.debug_gesture", false);
    private static final String[] DEFAULT_HOME_CHANGE_ACTIONS = {"android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED", "android.intent.action.BOOT_COMPLETED", "android.intent.action.PACKAGE_ADDED", "android.intent.action.PACKAGE_CHANGED", "android.intent.action.PACKAGE_REMOVED"};
    private static final int GESTURE_KEY_DISTANCE_THRESHOLD = SystemProperties.getInt("persist.gesture_button.dis", 60);
    private static final boolean IS_SUPPORT_CAMERA_NOTCH = OpFeatures.isSupport(new int[]{49});
    private static final int MAX_LONG_PRESS_TIMEOUT = SystemProperties.getInt("gestures.back_timeout", 250);
    private static final float PORTRAIT_LEAVE_ONE_HANDED_SCALE = (((float) SystemProperties.getInt("persist.gesture_button.one.handed.leave.scale", 45)) * 0.01f);
    private static final float SIDE_GESTURE_EDGE_BACK_SCALE = (((float) SystemProperties.getInt("persist.gesture_button.side.back.scale", Resources.getSystem().getInteger(84475923))) * 0.001f);
    private static final float SIDE_GESTURE_EDGE_HORIZONTAL_SCALE = (((float) SystemProperties.getInt("persist.gesture_button.side.hor.scale", 300)) * 0.01f);
    private static final float SIDE_GESTURE_EDGE_MOVE_SCALE = (((float) SystemProperties.getInt("persist.gesture_button.side.move.scale", 45)) * 0.001f);
    private static final float SIDE_GESTURE_EDGE_SCALE = (((float) SystemProperties.getInt("persist.gesture_button.side.edge.scale", Resources.getSystem().getInteger(84475924))) * 0.001f);
    private static int mCameraNotchHeight = 80;
    public static boolean sSideGestureEnabled = false;
    private final ActivityManagerWrapper mActivityManagerWrapper;
    private boolean mAllowGesture = false;
    private boolean mAllowLeaveOneHandedGesture = false;
    private final NavigationEdgeBackPlugin.BackCallback mBackCallback = new NavigationEdgeBackPlugin.BackCallback() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.5
        @Override // com.android.systemui.plugins.NavigationEdgeBackPlugin.BackCallback
        public void triggerBack() {
            EdgeBackGestureHandler.this.sendEvent(0, 4);
            int i = 1;
            EdgeBackGestureHandler.this.sendEvent(1, 4);
            EdgeBackGestureHandler.this.mOverviewProxyService.notifyBackAction(true, (int) EdgeBackGestureHandler.this.mDownPoint.x, (int) EdgeBackGestureHandler.this.mDownPoint.y, false, !EdgeBackGestureHandler.this.mIsOnLeftEdge);
            EdgeBackGestureHandler edgeBackGestureHandler = EdgeBackGestureHandler.this;
            if (edgeBackGestureHandler.mInRejectedExclusion) {
                i = 2;
            }
            edgeBackGestureHandler.logGesture(i);
        }

        @Override // com.android.systemui.plugins.NavigationEdgeBackPlugin.BackCallback
        public void cancelBack() {
            EdgeBackGestureHandler.this.logGesture(4);
            EdgeBackGestureHandler.this.mOverviewProxyService.notifyBackAction(false, (int) EdgeBackGestureHandler.this.mDownPoint.x, (int) EdgeBackGestureHandler.this.mDownPoint.y, false, !EdgeBackGestureHandler.this.mIsOnLeftEdge);
        }
    };
    private StatusBar mBar;
    private float mBottomGestureHeight;
    private final Context mContext;
    private final BroadcastReceiver mDefaultHomeBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.7
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            EdgeBackGestureHandler.this.updateTopPackage();
        }
    };
    private final IntentFilter mDefaultHomeIntentFilter;
    private boolean mDisabledForQuickstep;
    private Display mDisplay;
    private final int mDisplayId;
    private final Point mDisplaySize = new Point();
    private final PointF mDownPoint = new PointF();
    private OpSideGestureNavView mEdgePanel;
    private WindowManager.LayoutParams mEdgePanelLp;
    private int mEdgeSwipeStartThreshold = ((int) (SIDE_GESTURE_EDGE_SCALE * 1080.0f));
    private int mEdgeWidthLeft;
    private int mEdgeWidthRight;
    private final PointF mEndPoint = new PointF();
    private final Region mExcludeRegion = new Region();
    private int mGameToolBoxRegionHeight;
    private final List<ComponentName> mGestureBlockingActivities = new ArrayList();
    private boolean mGestureBlockingActivityRunning;
    private ISystemGestureExclusionListener mGestureExclusionListener = new ISystemGestureExclusionListener.Stub() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.1
        public void onSystemGestureExclusionChanged(int i, Region region, Region region2) {
            if (i == EdgeBackGestureHandler.this.mDisplayId) {
                EdgeBackGestureHandler.this.mMainExecutor.execute(new Runnable(region, region2) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$EdgeBackGestureHandler$1$gxj4RNtkm_JZXkSr9gvVxA9V4Ew
                    public final /* synthetic */ Region f$1;
                    public final /* synthetic */ Region f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        EdgeBackGestureHandler.AnonymousClass1.this.lambda$onSystemGestureExclusionChanged$0$EdgeBackGestureHandler$1(this.f$1, this.f$2);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSystemGestureExclusionChanged$0 */
        public /* synthetic */ void lambda$onSystemGestureExclusionChanged$0$EdgeBackGestureHandler$1(Region region, Region region2) {
            EdgeBackGestureHandler.this.mExcludeRegion.set(region);
            Region region3 = EdgeBackGestureHandler.this.mUnrestrictedExcludeRegion;
            if (region2 != null) {
                region = region2;
            }
            region3.set(region);
        }
    };
    private final GestureNavigationSettingsObserver mGestureNavigationSettingsObserver;
    private OpSystemUIGestureOnlineConfig mGestureOnlineConfig;
    private boolean mInRejectedExclusion = false;
    private InputEventReceiver mInputEventReceiver;
    private InputMonitor mInputMonitor;
    private boolean mIsAttached;
    private boolean mIsBackGestureAllowed;
    private boolean mIsEnabled;
    private boolean mIsGesturalModeEnabled;
    protected boolean mIsHidden = false;
    private boolean mIsIgnoreCameraNotch = false;
    private boolean mIsNavBarShownTransiently;
    private boolean mIsOnLeftEdge;
    private boolean mIsOneHandedPerformed = false;
    private boolean mIsOneHandedSettingEnable = false;
    private int mLeftInset;
    private boolean mLogGesture = false;
    private final int mLongPressTimeout;
    private final Executor mMainExecutor;
    private final OverviewProxyService.OverviewProxyOneHandedListener mOneHandListener = new OverviewProxyService.OverviewProxyOneHandedListener() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.4
        @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyOneHandedListener
        public void leaveOneHand() {
            Log.d("EdgeBackGestureHandler", "Launcher request leave one hand mode. isOneHandedSettingEnable " + EdgeBackGestureHandler.this.mIsOneHandedSettingEnable + " mIsOneHandedPerformed " + EdgeBackGestureHandler.this.mIsOneHandedPerformed);
            if (EdgeBackGestureHandler.this.mIsOneHandedSettingEnable && EdgeBackGestureHandler.this.mIsOneHandedPerformed && OpSystemUIInjector.requestExitOneHandMode()) {
                EdgeBackGestureHandler.this.mIsOneHandedPerformed = false;
                EdgeBackGestureHandler.this.notifyLeaveOneHandedMode();
            }
        }
    };
    private OpGestureButtonViewController mOpGestureButtonViewController;
    private OpOneHandModeController mOpOneHandModeController;
    private final TaskStackChangeListener mOpTaskStackChangeListener = new TaskStackChangeListener() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.6
        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChanged() {
            super.onTaskStackChanged();
            EdgeBackGestureHandler.this.updateTopPackage();
        }
    };
    private final OverviewProxyService mOverviewProxyService;
    private final PluginManager mPluginManager;
    private OverviewProxyService.OverviewProxyListener mQuickSwitchListener = new OverviewProxyService.OverviewProxyListener() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.2
        @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
        public void onQuickSwitchToNewTask(int i) {
            EdgeBackGestureHandler.this.mStartingQuickstepRotation = i;
            EdgeBackGestureHandler.this.updateDisabledForQuickstep();
        }
    };
    private boolean mReceiverRegister = false;
    private int mRightInset;
    private int mRotation;
    private int mRunningTaskId;
    private int mScreenHeight = -1;
    private int mScreenWidth = -1;
    private int mSideGestureKeyAnimThreshold = ((int) (SIDE_GESTURE_EDGE_MOVE_SCALE * 1080.0f));
    private int mSideGestureKeyDistanceThreshold = ((int) (SIDE_GESTURE_EDGE_BACK_SCALE * 1080.0f));
    private int mStartingQuickstepRotation = -1;
    private final Runnable mStateChangeCallback;
    private int mSwipeStartThreshold = 50;
    private int mSysUiFlags;
    private TaskStackChangeListener mTaskStackListener = new TaskStackChangeListener() { // from class: com.android.systemui.statusbar.phone.EdgeBackGestureHandler.3
        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChanged() {
            EdgeBackGestureHandler edgeBackGestureHandler = EdgeBackGestureHandler.this;
            edgeBackGestureHandler.mGestureBlockingActivityRunning = edgeBackGestureHandler.isGestureBlockingActivityRunning();
        }
    };
    private boolean mThresholdCrossed = false;
    private String mTopClassName = null;
    private String mTopPackageName = null;
    private final Region mUnrestrictedExcludeRegion = new Region();
    private final WindowManager mWm;

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayAdded(int i) {
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayRemoved(int i) {
    }

    public void onPluginConnected(NavigationEdgeBackPlugin navigationEdgeBackPlugin, Context context) {
    }

    public void onPluginDisconnected(NavigationEdgeBackPlugin navigationEdgeBackPlugin) {
    }

    public EdgeBackGestureHandler(Context context, OverviewProxyService overviewProxyService, SysUiState sysUiState, PluginManager pluginManager, Runnable runnable) {
        super((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class));
        this.mContext = context;
        this.mDisplayId = context.getDisplayId();
        this.mMainExecutor = context.getMainExecutor();
        this.mOverviewProxyService = overviewProxyService;
        this.mPluginManager = pluginManager;
        this.mStateChangeCallback = runnable;
        ComponentName unflattenFromString = ComponentName.unflattenFromString(context.getString(17039953));
        if (unflattenFromString != null) {
            String packageName = unflattenFromString.getPackageName();
            try {
                Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(packageName);
                int identifier = resourcesForApplication.getIdentifier("gesture_blocking_activities", "array", packageName);
                if (identifier == 0) {
                    Log.e("EdgeBackGestureHandler", "No resource found for gesture-blocking activities");
                } else {
                    for (String str : resourcesForApplication.getStringArray(identifier)) {
                        this.mGestureBlockingActivities.add(ComponentName.unflattenFromString(str));
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("EdgeBackGestureHandler", "Failed to add gesture blocking activities", e);
            }
        }
        ((ProtoTracer) Dependency.get(ProtoTracer.class)).add(this);
        this.mLongPressTimeout = Math.min(MAX_LONG_PRESS_TIMEOUT, ViewConfiguration.getLongPressTimeout());
        this.mGestureNavigationSettingsObserver = new GestureNavigationSettingsObserver(this.mContext.getMainThreadHandler(), this.mContext, new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$EdgeBackGestureHandler$_LuW15YEeoCQlkaEsBj7DgfSfSI
            @Override // java.lang.Runnable
            public final void run() {
                EdgeBackGestureHandler.this.onNavigationSettingsChanged();
            }
        });
        updateCurrentUserResources();
        sysUiState.addCallback(new SysUiState.SysUiStateCallback() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$EdgeBackGestureHandler$0xBzEFuOIJ4-3m0YZa3952VKAW8
            @Override // com.android.systemui.model.SysUiState.SysUiStateCallback
            public final void onSystemUiStateChanged(int i) {
                EdgeBackGestureHandler.this.lambda$new$0$EdgeBackGestureHandler(i);
            }
        });
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        this.mBar = phoneStatusBar;
        this.mOpGestureButtonViewController = phoneStatusBar.getGestureButtonController();
        WindowManager windowManager = (WindowManager) context.getSystemService(WindowManager.class);
        this.mWm = windowManager;
        int i = this.mScreenWidth;
        this.mSideGestureKeyAnimThreshold = (int) (((float) i) * SIDE_GESTURE_EDGE_MOVE_SCALE);
        this.mSideGestureKeyDistanceThreshold = (int) (((float) i) * SIDE_GESTURE_EDGE_BACK_SCALE);
        this.mDisplay = windowManager.getDefaultDisplay();
        new OpSideGestureConfiguration(this.mDisplay);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mDisplay.getRealMetrics(displayMetrics);
        this.mScreenHeight = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
        this.mScreenWidth = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        mCameraNotchHeight = this.mContext.getResources().getDimensionPixelSize(17105481);
        if (IS_SUPPORT_CAMERA_NOTCH) {
            this.mIsIgnoreCameraNotch = true;
        }
        if (this.mContext.getDisplay() != null) {
            int rotation = this.mDisplay.getRotation();
            if (rotation == 0 || rotation == 2) {
                this.mGameToolBoxRegionHeight = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_game_mode_toolbox_region_width_port);
            } else {
                this.mGameToolBoxRegionHeight = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_game_mode_toolbox_region_width_land);
            }
        } else {
            this.mGameToolBoxRegionHeight = DisplayUtils.convertDpToPx(48.0f, this.mContext);
        }
        this.mOverviewProxyService.addOneHandListener(this.mOneHandListener);
        this.mActivityManagerWrapper = ActivityManagerWrapper.getInstance();
        this.mDefaultHomeIntentFilter = new IntentFilter();
        for (String str2 : DEFAULT_HOME_CHANGE_ACTIONS) {
            this.mDefaultHomeIntentFilter.addAction(str2);
        }
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d("EdgeBackGestureHandler", "is Support one hand feature " + OpFeatures.isSupport(new int[]{221}));
        }
        if (OpFeatures.isSupport(new int[]{221})) {
            this.mOpOneHandModeController = this.mBar.getOneHandModeController();
        }
        this.mGestureOnlineConfig = this.mBar.getGestureOnlineConfig();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$EdgeBackGestureHandler(int i) {
        this.mSysUiFlags = i;
    }

    public void updateCurrentUserResources() {
        Resources resources = ((NavigationModeController) Dependency.get(NavigationModeController.class)).getCurrentUserContext().getResources();
        this.mEdgeWidthLeft = this.mGestureNavigationSettingsObserver.getLeftSensitivity(resources);
        this.mEdgeWidthRight = this.mGestureNavigationSettingsObserver.getRightSensitivity(resources);
        this.mIsBackGestureAllowed = !this.mGestureNavigationSettingsObserver.areNavigationButtonForcedVisible() || this.mIsGesturalModeEnabled;
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        this.mBottomGestureHeight = TypedValue.applyDimension(1, DeviceConfig.getFloat("systemui", "back_gesture_bottom_height", resources.getDimension(17105326) / displayMetrics.density), displayMetrics);
        DeviceConfig.getFloat("systemui", "back_gesture_slop_multiplier", 0.75f);
        ViewConfiguration.get(this.mContext).getScaledTouchSlop();
    }

    /* access modifiers changed from: private */
    public void onNavigationSettingsChanged() {
        boolean isHandlingGestures = isHandlingGestures();
        updateCurrentUserResources();
        if (isHandlingGestures != isHandlingGestures()) {
            this.mStateChangeCallback.run();
        }
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        updateIsEnabled();
        updateCurrentUserResources();
    }

    public void onNavBarAttached() {
        Log.d("EdgeBackGestureHandler", "onNavBarAttached");
        this.mIsAttached = true;
        this.mOverviewProxyService.addCallback(this.mQuickSwitchListener);
        updateIsEnabled();
        startTracking();
        this.mActivityManagerWrapper.registerTaskStackListener(this.mOpTaskStackChangeListener);
        this.mContext.registerReceiver(this.mDefaultHomeBroadcastReceiver, this.mDefaultHomeIntentFilter);
        this.mReceiverRegister = true;
        OpOneHandModeController opOneHandModeController = this.mOpOneHandModeController;
        if (opOneHandModeController != null) {
            opOneHandModeController.addListener(this);
        }
    }

    public void onNavBarDetached() {
        Log.d("EdgeBackGestureHandler", "onNavBarDetached");
        this.mIsAttached = false;
        this.mOverviewProxyService.removeCallback(this.mQuickSwitchListener);
        updateIsEnabled();
        stopTracking();
        this.mActivityManagerWrapper.unregisterTaskStackListener(this.mOpTaskStackChangeListener);
        if (this.mReceiverRegister) {
            this.mContext.unregisterReceiver(this.mDefaultHomeBroadcastReceiver);
            this.mReceiverRegister = false;
        }
        OpOneHandModeController opOneHandModeController = this.mOpOneHandModeController;
        if (opOneHandModeController != null) {
            opOneHandModeController.removeListener(this);
        }
        this.mIsOneHandedSettingEnable = false;
    }

    public void onNavigationModeChanged(int i) {
        this.mIsGesturalModeEnabled = QuickStepContract.isGesturalMode(i);
        updateIsEnabled();
        updateCurrentUserResources();
    }

    public void onNavBarTransientStateChanged(boolean z) {
        this.mIsNavBarShownTransiently = z;
    }

    private void disposeInputChannel() {
        InputEventReceiver inputEventReceiver = this.mInputEventReceiver;
        if (inputEventReceiver != null) {
            inputEventReceiver.dispose();
            this.mInputEventReceiver = null;
        }
        InputMonitor inputMonitor = this.mInputMonitor;
        if (inputMonitor != null) {
            inputMonitor.dispose();
            this.mInputMonitor = null;
        }
    }

    private void updateIsEnabled() {
        Log.d("EdgeBackGestureHandler", "updateIsEnabled: " + this.mIsGesturalModeEnabled + ", Attach: " + this.mIsAttached + ", enable: " + this.mIsEnabled + ", isHidden: " + this.mIsHidden + ", OneHandMode:" + this.mIsOneHandedSettingEnable);
        boolean z = true;
        boolean z2 = (this.mIsAttached || this.mIsHidden) && (this.mIsGesturalModeEnabled || this.mIsOneHandedSettingEnable);
        if (z2 != this.mIsEnabled) {
            this.mIsEnabled = z2;
            disposeInputChannel();
            OpSideGestureNavView opSideGestureNavView = this.mEdgePanel;
            if (opSideGestureNavView != null) {
                this.mWm.removeView(opSideGestureNavView);
                this.mEdgePanel = null;
            }
            if (!this.mIsEnabled) {
                this.mGestureNavigationSettingsObserver.unregister();
                ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).unregisterDisplayListener(this);
                this.mPluginManager.removePluginListener(this);
                ActivityManagerWrapper.getInstance().unregisterTaskStackListener(this.mTaskStackListener);
                try {
                    if (((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(this.mDisplayId) != null) {
                        WindowManagerGlobal.getWindowManagerService().unregisterSystemGestureExclusionListener(this.mGestureExclusionListener, this.mDisplayId);
                    } else {
                        Log.d("EdgeBackGestureHandler", "It is not unregister system gesture exclusion listener, because display is null or display already removed.");
                    }
                } catch (RemoteException | IllegalArgumentException e) {
                    Log.e("EdgeBackGestureHandler", "Failed to unregister window manager callbacks", e);
                }
                OpGestureButtonViewController opGestureButtonViewController = this.mOpGestureButtonViewController;
                if (!this.mIsEnabled && !this.mIsOneHandedSettingEnable) {
                    z = false;
                }
                opGestureButtonViewController.updateRegion(z);
                return;
            }
            this.mGestureNavigationSettingsObserver.register();
            updateDisplaySize();
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).registerDisplayListener(this, this.mContext.getMainThreadHandler());
            ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
            try {
                if (((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(this.mDisplayId) != null) {
                    WindowManagerGlobal.getWindowManagerService().registerSystemGestureExclusionListener(this.mGestureExclusionListener, this.mDisplayId);
                } else {
                    Log.d("EdgeBackGestureHandler", "It is not register system gesture exclusion listener, because display is null or display already removed.");
                }
            } catch (RemoteException | IllegalArgumentException e2) {
                Log.e("EdgeBackGestureHandler", "Failed to register window manager callbacks", e2);
            }
            this.mInputMonitor = InputManager.getInstance().monitorGestureInput("edge-swipe", this.mDisplayId);
            this.mInputEventReceiver = new SysUiInputEventReceiver(this.mInputMonitor.getInputChannel(), Looper.getMainLooper());
            this.mEdgePanel = new OpSideGestureNavView(this.mContext, 0, 0);
            WindowManager.LayoutParams createLayoutParams = createLayoutParams();
            this.mEdgePanelLp = createLayoutParams;
            this.mEdgePanel.setLayoutParams(createLayoutParams);
            this.mWm.addView(this.mEdgePanel, this.mEdgePanelLp);
            this.mPluginManager.addPluginListener((PluginListener) this, NavigationEdgeBackPlugin.class, false);
        }
    }

    public boolean isHandlingGestures() {
        return this.mIsEnabled && this.mIsBackGestureAllowed;
    }

    private WindowManager.LayoutParams createLayoutParams() {
        this.mContext.getResources();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(OpSideGestureConfiguration.getWindowWidth(), OpSideGestureConfiguration.getWindowHeight(0), 2024, 8388904, -3);
        if (OpUtils.isSupportHolePunchFrontCam()) {
            layoutParams.layoutInDisplayCutoutMode = 3;
        }
        layoutParams.setTitle("EdgeBackGestureHandler" + this.mContext.getDisplayId());
        layoutParams.accessibilityTitle = this.mContext.getString(C0015R$string.nav_bar_edge_panel);
        layoutParams.windowAnimations = 0;
        layoutParams.setFitInsetsTypes(0);
        return layoutParams;
    }

    private boolean isPortrait() {
        int i = this.mRotation;
        return i == 0 || i == 2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onInputEvent(InputEvent inputEvent) {
        if (inputEvent instanceof MotionEvent) {
            onMotionEvent((MotionEvent) inputEvent);
        }
    }

    private boolean isWithinTouchRegion(int i, int i2) {
        int i3;
        if (!this.mIsGesturalModeEnabled) {
            return false;
        }
        if (!sSideGestureEnabled) {
            if (isPortrait()) {
                if (i2 < this.mScreenHeight - this.mSwipeStartThreshold) {
                    return false;
                }
                int i4 = this.mScreenWidth;
                if (i < i4 / 3 || i > (i4 * 2) / 3) {
                    this.mLogGesture = true;
                    return true;
                }
            } else if ((this.mRotation == 1 && i < this.mScreenHeight - this.mSwipeStartThreshold) || (this.mRotation == 3 && i > this.mSwipeStartThreshold)) {
                return false;
            } else {
                int i5 = this.mScreenWidth;
                if (i2 < i5 / 3 || i2 > (i5 * 2) / 3) {
                    this.mLogGesture = true;
                    return true;
                }
            }
            return false;
        }
        OpSideGestureNavView opSideGestureNavView = this.mEdgePanel;
        if (opSideGestureNavView == null || opSideGestureNavView.isExitAnimFinished()) {
            int adjuestEdgeThreshold = adjuestEdgeThreshold(i, i2, this.mRotation);
            if (OpUtils.DEBUG_ONEPLUS && this.mTopClassName != null) {
                Log.d("EdgeBackGestureHandler", "topClassName " + this.mTopClassName);
            }
            if (this.mGestureOnlineConfig.isInPhotoEditorList(OpUtils.getTopClassName()) && i > (i3 = adjuestEdgeThreshold / 2) && i < this.mDisplaySize.x - i3) {
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE][Back] special ignore region [0~" + i3 + "],[" + (this.mDisplaySize.x - i3) + "~" + this.mDisplaySize.y + "]");
                }
                return false;
            } else if (i > adjuestEdgeThreshold && i < this.mDisplaySize.x - adjuestEdgeThreshold) {
                if (DEBUG_GESTURE) {
                    Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE][Back] ignore region [0~" + adjuestEdgeThreshold + "],[" + (this.mDisplaySize.x - adjuestEdgeThreshold) + "~" + this.mDisplaySize.y + "]");
                }
                return false;
            } else if (((float) i2) >= ((float) this.mDisplaySize.y) - this.mBottomGestureHeight || !isYInTouchRegion(i2)) {
                if (DEBUG_GESTURE) {
                    Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE][Back] touch on navigation bar area [" + (((float) this.mDisplaySize.y) - this.mBottomGestureHeight) + " ~ " + this.mDisplaySize.y + "]");
                }
                return false;
            } else {
                boolean z = i <= this.mEdgeWidthLeft + this.mLeftInset || i >= (this.mDisplaySize.x - this.mEdgeWidthRight) - this.mRightInset;
                if (this.mIsNavBarShownTransiently) {
                    this.mLogGesture = true;
                    return z;
                } else if (isGameToolBoxRegion(i2)) {
                    if (OpUtils.DEBUG_ONEPLUS) {
                        Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE][Back] isInGameToolBoxRegion!");
                    }
                    return false;
                } else {
                    boolean contains = this.mExcludeRegion.contains(i, i2);
                    if (OpUtils.DEBUG_ONEPLUS) {
                        Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE][Back] isInExcludedRegion " + contains);
                    }
                    if (contains) {
                        if (z) {
                            this.mOverviewProxyService.notifyBackAction(false, -1, -1, false, !this.mIsOnLeftEdge);
                            PointF pointF = this.mEndPoint;
                            pointF.x = -1.0f;
                            pointF.y = -1.0f;
                            this.mLogGesture = true;
                            logGesture(3);
                        }
                        return false;
                    }
                    this.mInRejectedExclusion = this.mUnrestrictedExcludeRegion.contains(i, i2);
                    this.mLogGesture = true;
                    return z;
                }
            }
        } else {
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE][Back] Exit anim not finished");
            }
            return false;
        }
    }

    private boolean isGameToolBoxRegion(int i) {
        return OpUtils.gameToolboxEnable(this.mContext) && i < this.mGameToolBoxRegionHeight;
    }

    private boolean isYInTouchRegion(int i) {
        return ((float) i) > ((float) getScreenHeight(this.mRotation)) * OpSideGestureConfiguration.PORTRAIT_NON_DETECT_SCALE;
    }

    private boolean isYInLeaveOneHandedTouchRegion(int i) {
        return ((float) i) < ((float) getScreenHeight(this.mRotation)) * PORTRAIT_LEAVE_ONE_HANDED_SCALE;
    }

    private void cancelGesture(MotionEvent motionEvent) {
        this.mAllowGesture = false;
        this.mLogGesture = false;
        this.mInRejectedExclusion = false;
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        obtain.setAction(3);
        OpSideGestureNavView opSideGestureNavView = this.mEdgePanel;
        if (opSideGestureNavView != null) {
            opSideGestureNavView.handleTouch(obtain);
        }
        this.mBackCallback.cancelBack();
        obtain.recycle();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logGesture(int i) {
        if (this.mLogGesture) {
            this.mLogGesture = false;
            int i2 = (int) this.mDownPoint.y;
            int i3 = this.mIsOnLeftEdge ? 1 : 2;
            PointF pointF = this.mDownPoint;
            int i4 = (int) pointF.y;
            PointF pointF2 = this.mEndPoint;
            SysUiStatsLog.write(224, i, i2, i3, (int) pointF.x, i4, (int) pointF2.x, (int) pointF2.y, this.mEdgeWidthLeft + this.mLeftInset, this.mDisplaySize.x - (this.mEdgeWidthRight + this.mRightInset));
        }
    }

    private void onMotionEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        boolean z = true;
        if (actionMasked == 0) {
            this.mIsOnLeftEdge = motionEvent.getX() <= ((float) adjuestEdgeThreshold((int) motionEvent.getX(), (int) motionEvent.getY(), this.mRotation));
            this.mLogGesture = false;
            this.mInRejectedExclusion = false;
            boolean isQsDetailShowing = OpLsState.getInstance().getPhoneStatusBar().mNotificationPanelViewController.isQsDetailShowing();
            this.mAllowGesture = !this.mDisabledForQuickstep && this.mIsBackGestureAllowed && !this.mGestureBlockingActivityRunning && (!QuickStepContract.isBackGestureDisabled(this.mSysUiFlags) || isNonBlockHiddenNavBar() || isQsDetailShowing) && isWithinTouchRegion((int) motionEvent.getX(), (int) motionEvent.getY());
            if (OpUtils.DEBUG_ONEPLUS) {
                StringBuilder sb = new StringBuilder();
                sb.append("mAllowGesture: ");
                sb.append(!QuickStepContract.isBackGestureDisabled(this.mSysUiFlags));
                sb.append(", ");
                sb.append(isQsDetailShowing);
                Log.d("EdgeBackGestureHandler", sb.toString());
            }
            if (this.mAllowGesture) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("EdgeBackGestureHandler", "AllowGesture down");
                }
                if (this.mEdgePanel != null) {
                    this.mEdgePanelLp.gravity = this.mIsOnLeftEdge ? 83 : 85;
                    this.mEdgePanel.setIsLeftPanel(this.mIsOnLeftEdge);
                    this.mEdgePanel.handleTouch(motionEvent);
                    this.mWm.updateViewLayout(this.mEdgePanel, this.mEdgePanelLp);
                }
            }
            if (this.mLogGesture) {
                this.mDownPoint.set(motionEvent.getX(), motionEvent.getY());
                this.mEndPoint.set(-1.0f, -1.0f);
                this.mThresholdCrossed = false;
            }
            if (!this.mIsOneHandedSettingEnable || !isYInLeaveOneHandedTouchRegion((int) motionEvent.getY())) {
                z = false;
            }
            this.mAllowLeaveOneHandedGesture = z;
            if (DEBUG_GESTURE) {
                Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE] Motion: " + actionMasked);
                Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE] x,y : [" + motionEvent.getX() + "," + motionEvent.getY() + "]");
                Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE][Back] mIsOnLeftEdge: " + this.mIsOnLeftEdge + ", x:[" + motionEvent.getX() + "]");
                StringBuilder sb2 = new StringBuilder();
                sb2.append("[DEBUG_GESTURE][OneHand] AllowLeave: ");
                sb2.append(this.mAllowLeaveOneHandedGesture);
                Log.d("EdgeBackGestureHandler", sb2.toString());
            }
        } else if (this.mAllowGesture || this.mLogGesture) {
            if (!this.mThresholdCrossed) {
                this.mEndPoint.x = (float) ((int) motionEvent.getX());
                this.mEndPoint.y = (float) ((int) motionEvent.getY());
                if (actionMasked == 5) {
                    if (this.mAllowGesture) {
                        logGesture(6);
                        cancelGesture(motionEvent);
                    }
                    this.mLogGesture = false;
                    return;
                } else if (actionMasked == 2) {
                    if (DEBUG_GESTURE && this.mTopClassName != null) {
                        Log.d("EdgeBackGestureHandler", "topClassName " + this.mTopClassName);
                    }
                    if (this.mGestureOnlineConfig.isInPhotoEditorList(OpUtils.getTopClassName()) && motionEvent.getEventTime() - motionEvent.getDownTime() > 80) {
                        if (OpUtils.DEBUG_ONEPLUS) {
                            Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE][Back] special LongPressTimeOut [80]");
                        }
                        if (this.mAllowGesture) {
                            cancelGesture(motionEvent);
                        }
                        this.mLogGesture = false;
                        return;
                    } else if (motionEvent.getEventTime() - motionEvent.getDownTime() > ((long) this.mLongPressTimeout)) {
                        if (OpUtils.DEBUG_ONEPLUS) {
                            Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE] LongPressTimeOut: [" + this.mLongPressTimeout + "]");
                            StringBuilder sb3 = new StringBuilder();
                            sb3.append("[DEBUG_GESTURE] time: ");
                            sb3.append(motionEvent.getEventTime() - motionEvent.getDownTime());
                            Log.d("EdgeBackGestureHandler", sb3.toString());
                        }
                        if (this.mAllowGesture) {
                            logGesture(7);
                            cancelGesture(motionEvent);
                        }
                        this.mLogGesture = false;
                        return;
                    } else {
                        float abs = Math.abs(motionEvent.getX() - this.mDownPoint.x);
                        float abs2 = Math.abs(motionEvent.getY() - this.mDownPoint.y);
                        if (sSideGestureEnabled || !this.mAllowGesture) {
                            int i = this.mSideGestureKeyAnimThreshold;
                            if (abs > ((float) i)) {
                                if (this.mAllowGesture) {
                                    if (OpUtils.DEBUG_ONEPLUS) {
                                        Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE] Trigger Back gesture animation");
                                    }
                                    this.mThresholdCrossed = true;
                                    this.mInputMonitor.pilferPointers();
                                    this.mEdgePanel.onDownEvent();
                                }
                            } else if (abs2 > ((float) i) * SIDE_GESTURE_EDGE_HORIZONTAL_SCALE) {
                                if (this.mAllowGesture) {
                                    Log.d("EdgeBackGestureHandler", "Swipe too skew " + abs2);
                                    cancelGesture(motionEvent);
                                }
                                this.mLogGesture = false;
                                return;
                            }
                        } else {
                            if (((!isPortrait() ? abs <= ((float) GESTURE_KEY_DISTANCE_THRESHOLD) : abs2 <= ((float) GESTURE_KEY_DISTANCE_THRESHOLD)) ? null : 1) != null) {
                                Log.i("EdgeBackGestureHandler", "onMotionEvent sendBackKey");
                                sendEvent(0, 4);
                                sendEvent(1, 4);
                                this.mOpGestureButtonViewController.onBackAction(this.mDownPoint.x);
                                cancelGesture(motionEvent);
                                return;
                            }
                            return;
                        }
                    }
                }
            }
            if (this.mAllowGesture) {
                this.mEdgePanel.handleTouch(motionEvent);
                boolean isYInTouchRegion = isYInTouchRegion((int) motionEvent.getY());
                int rotation = this.mDisplay.getRotation();
                float abs3 = Math.abs(motionEvent.getX() - this.mDownPoint.x);
                int i2 = abs3 > ((float) this.mSideGestureKeyDistanceThreshold) ? 2 : 1;
                this.mEdgePanel.onUpdateGestureView(new GesturePointContainer(new PointF(motionEvent.getX(), motionEvent.getY()), (actionMasked == 2 && !isYInTouchRegion && i2 == 2) ? 4 : i2, !this.mIsOnLeftEdge ? 1 : 0, rotation, this.mScreenHeight, this.mScreenWidth));
                boolean z2 = actionMasked == 1;
                boolean z3 = z2 && abs3 > ((float) this.mSideGestureKeyDistanceThreshold) && isYInTouchRegion;
                if (z2) {
                    this.mEdgePanel.onGestureFinished(new GesturePointContainer(new PointF(motionEvent.getX(), motionEvent.getY()), 2, !this.mIsOnLeftEdge ? 1 : 0, rotation, this.mScreenHeight, this.mScreenWidth));
                } else if (actionMasked == 3 || actionMasked == 5) {
                    this.mEdgePanel.onGestureFinished(new GesturePointContainer(new PointF(motionEvent.getX(), motionEvent.getY()), 2, !this.mIsOnLeftEdge ? 1 : 0, rotation, this.mScreenHeight, this.mScreenWidth));
                    this.mEdgePanel.onUpEvent();
                    cancelGesture(motionEvent);
                }
                if (OpUtils.DEBUG_ONEPLUS && z2 && abs3 > ((float) (this.mSideGestureKeyDistanceThreshold / 2)) && !z3) {
                    Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE][Back] performBackKey: " + z3);
                    Log.d("EdgeBackGestureHandler", "[DEBUG_GESTURE][Back] back move distance: [" + abs3 + ", need big than target [" + this.mSideGestureKeyDistanceThreshold + "]");
                }
                if (z2 && z3) {
                    this.mBackCallback.triggerBack();
                    this.mAllowLeaveOneHandedGesture = false;
                }
            }
            if (this.mAllowLeaveOneHandedGesture && actionMasked == 1 && isYInLeaveOneHandedTouchRegion((int) motionEvent.getY()) && this.mIsOneHandedPerformed && isPortrait() && OpSystemUIInjector.requestExitOneHandMode()) {
                Log.d("EdgeBackGestureHandler", "Touch leave region to leave One Handed.");
                this.mIsOneHandedPerformed = false;
                notifyLeaveOneHandedMode();
            }
        }
        ((ProtoTracer) Dependency.get(ProtoTracer.class)).update();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDisabledForQuickstep() {
        int rotation = this.mContext.getResources().getConfiguration().windowConfiguration.getRotation();
        int i = this.mStartingQuickstepRotation;
        this.mDisabledForQuickstep = i > -1 && i != rotation;
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayChanged(int i) {
        if (this.mStartingQuickstepRotation > -1) {
            updateDisabledForQuickstep();
        }
        if (i == this.mDisplayId) {
            updateDisplaySize();
            this.mRotation = this.mDisplay.getRotation();
        }
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpOneHandModeController.OneHandModeStateListener
    public void onOneHandPerformStateChange(boolean z) {
        this.mIsOneHandedPerformed = z;
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpOneHandModeController.OneHandModeStateListener
    public void onOneHandEnableStateChange(boolean z) {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.d("EdgeBackGestureHandler", "One hand enable state changed from " + this.mIsOneHandedSettingEnable + " to " + z);
        }
        this.mIsOneHandedSettingEnable = z;
        updateIsEnabled();
    }

    private void updateDisplaySize() {
        if (((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(this.mDisplayId) == null) {
            Log.d("EdgeBackGestureHandler", "It's not update display size, because display is null or display already removed.");
            return;
        }
        ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(this.mDisplayId).getRealSize(this.mDisplaySize);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display defaultDisplay = this.mWm.getDefaultDisplay();
        this.mDisplay = defaultDisplay;
        defaultDisplay.getRealMetrics(displayMetrics);
        this.mScreenHeight = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
        int min = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        this.mScreenWidth = min;
        this.mEdgeSwipeStartThreshold = (int) (((float) min) * SIDE_GESTURE_EDGE_SCALE);
        this.mSideGestureKeyAnimThreshold = (int) (((float) min) * SIDE_GESTURE_EDGE_MOVE_SCALE);
        this.mSideGestureKeyDistanceThreshold = (int) (((float) min) * SIDE_GESTURE_EDGE_BACK_SCALE);
        new OpSideGestureConfiguration(this.mDisplay);
        this.mOpGestureButtonViewController.updateDisplaySize();
        this.mOpGestureButtonViewController.updateRegion(this.mIsEnabled || this.mIsOneHandedSettingEnable);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendEvent(int i, int i2) {
        long uptimeMillis = SystemClock.uptimeMillis();
        KeyEvent keyEvent = new KeyEvent(uptimeMillis, uptimeMillis, i, i2, 0, 0, -1, 0, 72, 257);
        int expandedDisplayId = ((BubbleController) Dependency.get(BubbleController.class)).getExpandedDisplayId(this.mContext);
        if (i2 == 4 && expandedDisplayId != -1) {
            keyEvent.setDisplayId(expandedDisplayId);
        }
        InputManager.getInstance().injectInputEvent(keyEvent, 0);
    }

    public void setInsets(int i, int i2) {
        this.mLeftInset = i;
        this.mRightInset = i2;
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("EdgeBackGestureHandler:");
        printWriter.println("  mIsEnabled=" + this.mIsEnabled);
        printWriter.println("  mIsBackGestureAllowed=" + this.mIsBackGestureAllowed);
        printWriter.println("  mAllowGesture=" + this.mAllowGesture);
        printWriter.println("  mDisabledForQuickstep=" + this.mDisabledForQuickstep);
        printWriter.println("  mStartingQuickstepRotation=" + this.mStartingQuickstepRotation);
        printWriter.println("  mInRejectedExclusion" + this.mInRejectedExclusion);
        printWriter.println("  mExcludeRegion=" + this.mExcludeRegion);
        printWriter.println("  mUnrestrictedExcludeRegion=" + this.mUnrestrictedExcludeRegion);
        printWriter.println("  mIsAttached=" + this.mIsAttached);
        printWriter.println("  mEdgeWidthLeft=" + this.mEdgeWidthLeft);
        printWriter.println("  mEdgeWidthRight=" + this.mEdgeWidthRight);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isGestureBlockingActivityRunning() {
        ComponentName componentName;
        ActivityManager.RunningTaskInfo runningTask = ActivityManagerWrapper.getInstance().getRunningTask();
        if (runningTask == null) {
            componentName = null;
        } else {
            componentName = runningTask.topActivity;
        }
        return componentName != null && this.mGestureBlockingActivities.contains(componentName);
    }

    public void writeToProto(SystemUiTraceProto systemUiTraceProto) {
        if (systemUiTraceProto.edgeBackGestureHandler == null) {
            systemUiTraceProto.edgeBackGestureHandler = new EdgeBackGestureHandlerProto();
        }
        systemUiTraceProto.edgeBackGestureHandler.allowGesture = this.mAllowGesture;
    }

    /* access modifiers changed from: package-private */
    public class SysUiInputEventReceiver extends InputEventReceiver {
        SysUiInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent inputEvent) {
            EdgeBackGestureHandler.this.onInputEvent(inputEvent);
            finishInputEvent(inputEvent, true);
        }
    }

    public void onConfigurationChanged(int i) {
        if (this.mIsEnabled) {
            this.mRotation = i;
            updateDisplaySize();
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.mEdgePanel.getLayoutParams();
            layoutParams.height = OpSideGestureConfiguration.getWindowHeight(i);
            layoutParams.width = OpSideGestureConfiguration.getWindowWidth();
            this.mWm.updateViewLayout(this.mEdgePanel, layoutParams);
            if (Build.DEBUG_ONEPLUS) {
                Log.d("EdgeBackGestureHandler", "Edge onConfigurationChanged rotation:" + i + ", height:" + layoutParams.height + ", width:" + layoutParams.width);
            }
            this.mEdgePanel.onConfigChanged(i);
        }
    }

    private int getScreenHeight(int i) {
        if (i == 0 || i == 2) {
            return this.mScreenHeight;
        }
        if (i == 1 || i == 3) {
            return this.mScreenWidth;
        }
        return this.mScreenHeight;
    }

    /* access modifiers changed from: package-private */
    public int adjuestEdgeThreshold(int i, int i2, int i3) {
        int i4 = this.mEdgeSwipeStartThreshold;
        if (!IS_SUPPORT_CAMERA_NOTCH || OpUtils.isSupportHolePunchFrontCam()) {
            return i4;
        }
        boolean z = i < this.mScreenHeight / 2;
        new Region();
        if ((i3 != 1 || !z) && (i3 != 3 || z)) {
            return (int) (((float) this.mScreenWidth) * SIDE_GESTURE_EDGE_SCALE);
        }
        return this.mIsIgnoreCameraNotch ? mCameraNotchHeight + this.mEdgeSwipeStartThreshold : i4;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTopPackage() {
        ComponentName componentName;
        ActivityManager.RunningTaskInfo runningTask = this.mActivityManagerWrapper.getRunningTask();
        if (runningTask != null) {
            if (this.mRunningTaskId != runningTask.taskId && (componentName = runningTask.topActivity) != null && componentName.getPackageName() != null) {
                this.mRunningTaskId = runningTask.taskId;
                this.mTopPackageName = runningTask.topActivity.getPackageName();
                this.mTopClassName = runningTask.topActivity.getClassName();
                OpUtils.updateTopPackage(this.mContext, runningTask.topActivity.getPackageName(), this.mTopClassName);
            } else {
                return;
            }
        }
        Log.d("EdgeBackGestureHandler", "updateTopPackage isHomeApp " + OpUtils.isHomeApp());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyLeaveOneHandedMode() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("EdgeBackGestureHandler", "notifyLeaveOneHandedMode");
        }
        if (this.mOverviewProxyService.getSysUIProxy() != null) {
            try {
                this.mOverviewProxyService.getSysUIProxy().notifyGestureEnded(50);
            } catch (RemoteException e) {
                Log.w("EdgeBackGestureHandler", " notifyGestureEnded , " + e);
            }
        }
    }

    private boolean isNonBlockHiddenNavBar() {
        if (this.mTopPackageName == null) {
            return false;
        }
        if (DEBUG_GESTURE) {
            StringBuilder sb = new StringBuilder();
            sb.append("isNonBlockHiddenNavBar package name ");
            sb.append(this.mTopPackageName);
            sb.append(" navbar hidden ");
            sb.append((this.mSysUiFlags & 2) != 0);
            Log.d("EdgeBackGestureHandler", sb.toString());
        }
        if ((this.mSysUiFlags & 2) == 0 || !this.mGestureOnlineConfig.isInNonBlockBackGestureList(this.mTopPackageName)) {
            return false;
        }
        return true;
    }
}
