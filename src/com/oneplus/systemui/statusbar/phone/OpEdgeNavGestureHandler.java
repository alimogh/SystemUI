package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.ISystemGestureExclusionListener;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputMonitor;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManagerGlobal;
import com.android.systemui.model.SysUiState;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.NavigationHandle;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.statusbar.phone.OpEdgeNavGestureHandler;
import com.oneplus.systemui.statusbar.phone.OpOneHandModeController;
import com.oneplus.systemui.statusbar.policy.OpHomeButton;
import java.util.concurrent.Executor;
public class OpEdgeNavGestureHandler implements DisplayManager.DisplayListener, OpOneHandModeController.OneHandModeStateListener {
    private static boolean mIsOneHandedEnable = false;
    private boolean mAllowNavGesture = false;
    private StatusBar mBar;
    private int mBottomGestureHeight;
    private final Context mContext;
    private final int mDisplayId;
    private final Point mDisplaySize = new Point();
    private final Region mExcludeRegion = new Region();
    private ISystemGestureExclusionListener mGestureExclusionListener = new ISystemGestureExclusionListener.Stub() { // from class: com.oneplus.systemui.statusbar.phone.OpEdgeNavGestureHandler.1
        public void onSystemGestureExclusionChanged(int i, Region region, Region region2) {
            if (i == OpEdgeNavGestureHandler.this.mDisplayId) {
                OpEdgeNavGestureHandler.this.mMainExecutor.execute(new Runnable(region) { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpEdgeNavGestureHandler$1$9Ann_QTbgpTliOGHxLahg53IcD4
                    public final /* synthetic */ Region f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        OpEdgeNavGestureHandler.AnonymousClass1.this.lambda$onSystemGestureExclusionChanged$0$OpEdgeNavGestureHandler$1(this.f$1);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSystemGestureExclusionChanged$0 */
        public /* synthetic */ void lambda$onSystemGestureExclusionChanged$0$OpEdgeNavGestureHandler$1(Region region) {
            OpEdgeNavGestureHandler.this.mExcludeRegion.set(region);
        }
    };
    private View mHome;
    private InputEventReceiver mInputEventReceiver;
    private InputMonitor mInputMonitor;
    private boolean mIsAttached;
    private boolean mIsEnabled;
    private boolean mIsGesturalModeEnabled;
    private boolean mIsNavBarShownTransiently;
    private final Executor mMainExecutor;
    private int mNavBarFrameHeight;
    private int mNavEdgeWidth;
    private int mNavEdgeWidthLand;
    private OpOneHandModeController mOneHandModeController;
    private int mRotation = 0;
    private int mSysUiFlags;

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayAdded(int i) {
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayRemoved(int i) {
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpOneHandModeController.OneHandModeStateListener
    public void onOneHandPerformStateChange(boolean z) {
    }

    public OpEdgeNavGestureHandler(Context context, OverviewProxyService overviewProxyService, SysUiState sysUiState) {
        Resources resources = context.getResources();
        this.mContext = context;
        this.mDisplayId = context.getDisplayId();
        this.mMainExecutor = context.getMainExecutor();
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        this.mBar = phoneStatusBar;
        this.mOneHandModeController = phoneStatusBar.getOneHandModeController();
        updateCurrentUserResources(resources);
        sysUiState.addCallback(new SysUiState.SysUiStateCallback() { // from class: com.oneplus.systemui.statusbar.phone.-$$Lambda$OpEdgeNavGestureHandler$1O3ShNNo6cPSc_sep9lRivTsiJM
            @Override // com.android.systemui.model.SysUiState.SysUiStateCallback
            public final void onSystemUiStateChanged(int i) {
                OpEdgeNavGestureHandler.this.lambda$new$0$OpEdgeNavGestureHandler(i);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$OpEdgeNavGestureHandler(int i) {
        this.mSysUiFlags = i;
    }

    public void updateCurrentUserResources(Resources resources) {
        this.mNavEdgeWidth = resources.getDimensionPixelSize(84279737);
        this.mNavEdgeWidthLand = resources.getDimensionPixelSize(84279736);
        this.mBottomGestureHeight = resources.getDimensionPixelSize(17105326);
        this.mNavBarFrameHeight = resources.getDimensionPixelSize(17105324);
    }

    public void onNavBarAttached() {
        Log.d("OpEdgeNavGestureHandler", "onNavBarAttached");
        this.mIsAttached = true;
        OpOneHandModeController opOneHandModeController = this.mOneHandModeController;
        if (opOneHandModeController != null) {
            opOneHandModeController.addListener(this);
        }
        updateIsEnabled();
    }

    public void onNavBarDetached() {
        Log.d("OpEdgeNavGestureHandler", "onNavBarDetached");
        this.mIsAttached = false;
        OpOneHandModeController opOneHandModeController = this.mOneHandModeController;
        if (opOneHandModeController != null) {
            opOneHandModeController.removeListener(this);
        }
        updateIsEnabled();
    }

    public void onNavigationModeChanged(int i, Context context) {
        this.mIsGesturalModeEnabled = QuickStepContract.isGesturalMode(i);
        updateIsEnabled();
        updateCurrentUserResources(context.getResources());
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
        Log.d("OpEdgeNavGestureHandler", "updateIsEnabled: GestureModeEnable: " + this.mIsGesturalModeEnabled + ", OneHandEnable: " + mIsOneHandedEnable + ", Attach: " + this.mIsAttached + ", LastTimeEnableState: " + this.mIsEnabled);
        boolean z = this.mIsAttached && (this.mIsGesturalModeEnabled || mIsOneHandedEnable);
        if (z == this.mIsEnabled) {
            Log.d("OpEdgeNavGestureHandler", "Already enable");
            return;
        }
        this.mIsEnabled = z;
        disposeInputChannel();
        if (!this.mIsEnabled) {
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).unregisterDisplayListener(this);
            try {
                if (((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(this.mDisplayId) != null) {
                    WindowManagerGlobal.getWindowManagerService().unregisterSystemGestureExclusionListener(this.mGestureExclusionListener, this.mDisplayId);
                } else {
                    Log.d("OpEdgeNavGestureHandler", "It is not unregister system gesture exclusion listener, because display is null or display already removed.");
                }
            } catch (RemoteException e) {
                Log.e("OpEdgeNavGestureHandler", "Failed to unregister window manager callbacks", e);
            }
        } else {
            updateDisplaySize();
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).registerDisplayListener(this, this.mContext.getMainThreadHandler());
            try {
                if (((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(this.mDisplayId) != null) {
                    WindowManagerGlobal.getWindowManagerService().registerSystemGestureExclusionListener(this.mGestureExclusionListener, this.mDisplayId);
                } else {
                    Log.d("OpEdgeNavGestureHandler", "It is not register system gesture exclusion listener, because display is null or display already removed.");
                }
            } catch (RemoteException e2) {
                Log.e("OpEdgeNavGestureHandler", "Failed to register window manager callbacks", e2);
            }
            this.mInputMonitor = InputManager.getInstance().monitorGestureInput("edge-nav-swipe", this.mDisplayId);
            this.mInputEventReceiver = new SysUiInputEventReceiver(this.mInputMonitor.getInputChannel(), Looper.getMainLooper());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onInputEvent(InputEvent inputEvent) {
        if ((inputEvent instanceof MotionEvent) && this.mHome != null) {
            onMotionEvent((MotionEvent) inputEvent);
        }
    }

    private boolean isWithinNavTouchRegion(int i, int i2) {
        int i3 = mIsOneHandedEnable ? this.mNavBarFrameHeight : this.mBottomGestureHeight;
        Point point = this.mDisplaySize;
        if (i2 < point.y - i3) {
            Log.i("OpEdgeNavGestureHandler", " mAllowNavGesture touch point is height than NavBarHeight.");
            return false;
        } else if (i > point.x - getNavEdgeWidth(this.mRotation) || i < getNavEdgeWidth(this.mRotation)) {
            Log.i("OpEdgeNavGestureHandler", " mAllowNavGesture touch point is out of range");
            return false;
        } else if (this.mIsNavBarShownTransiently) {
            return true;
        } else {
            return !this.mExcludeRegion.contains(i, i2);
        }
    }

    private void onMotionEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        boolean z = true;
        if (actionMasked == 0) {
            if (QuickStepContract.isBackGestureDisabled(this.mSysUiFlags) || !isWithinNavTouchRegion((int) motionEvent.getX(), (int) motionEvent.getY())) {
                z = false;
            }
            this.mAllowNavGesture = z;
            if (z && this.mHome != null) {
                Log.d("OpEdgeNavGestureHandler", "AllowNavGesture actionDown");
                View view = this.mHome;
                if (view instanceof NavigationHandle) {
                    ((NavigationHandle) view).handleTouch(motionEvent);
                } else if (mIsOneHandedEnable) {
                    int i = this.mRotation;
                    if (i == 0 || i == 2) {
                        ((OpHomeButton) this.mHome).handleTouch(motionEvent);
                    }
                }
            }
        } else if (this.mAllowNavGesture && this.mHome != null) {
            if (actionMasked == 1) {
                Log.d("OpEdgeNavGestureHandler", "AllowNavGesture actionUp");
            }
            View view2 = this.mHome;
            if (view2 instanceof NavigationHandle) {
                ((NavigationHandle) view2).handleTouch(motionEvent);
            } else if (mIsOneHandedEnable) {
                int i2 = this.mRotation;
                if (i2 == 0 || i2 == 2) {
                    ((OpHomeButton) this.mHome).handleTouch(motionEvent);
                }
            }
        }
    }

    @Override // android.hardware.display.DisplayManager.DisplayListener
    public void onDisplayChanged(int i) {
        if (i == this.mDisplayId) {
            updateDisplaySize();
        }
    }

    private void updateDisplaySize() {
        if (((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(this.mDisplayId) == null) {
            Log.d("OpEdgeNavGestureHandler", "It's not update display size, because display is null or display already removed.");
        } else {
            ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(this.mDisplayId).getRealSize(this.mDisplaySize);
        }
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpOneHandModeController.OneHandModeStateListener
    public void onOneHandEnableStateChange(boolean z) {
        mIsOneHandedEnable = z;
        updateIsEnabled();
    }

    public static boolean isOneHandedEnable() {
        return mIsOneHandedEnable;
    }

    /* access modifiers changed from: package-private */
    public class SysUiInputEventReceiver extends InputEventReceiver {
        SysUiInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent inputEvent) {
            OpEdgeNavGestureHandler.this.onInputEvent(inputEvent);
            finishInputEvent(inputEvent, true);
        }
    }

    public void onConfigurationChanged(int i) {
        this.mRotation = i;
        if (this.mIsEnabled) {
            updateDisplaySize();
        }
    }

    public void setHomeButton(View view) {
        this.mHome = view;
    }

    private int getNavEdgeWidth(int i) {
        if (i == 0 || i == 2) {
            return this.mNavEdgeWidth;
        }
        if (i == 1 || i == 3) {
            return this.mNavEdgeWidthLand;
        }
        return this.mNavEdgeWidth;
    }
}
