package com.oneplus.systemui.biometrics;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.fingerprint.IFingerprintClientActiveCallback;
import android.hardware.fingerprint.IFingerprintService;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.IStatusBarService;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.shared.system.PinnedStackListenerForwarder;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.aod.utils.OpCanvasAodHelper;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.biometrics.OpFodHelper;
import com.oneplus.systemui.biometrics.OpQLController;
public class OpBiometricDialogImpl extends SystemUI implements CommandQueue.Callbacks, ConfigurationController.ConfigurationListener, OpFodHelper.OnFingerprintStateChangeListener {
    protected static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private static final int FINGER_SET_DIM_DELAY = SystemProperties.getInt("debug.fingerprint_set_dim_delay", 0);
    public static final boolean SHOW_TRANSPARENT_ICON_VIEW = SystemProperties.getBoolean("debug.show_transparent_icon_view", false);
    private boolean mAuthenticatedSuccess;
    private final CommandQueue mCommandQueue;
    private boolean mDialogShowingRequest = false;
    private final IFingerprintClientActiveCallback mFingerprintClientActiveCallback = new IFingerprintClientActiveCallback.Stub() { // from class: com.oneplus.systemui.biometrics.OpBiometricDialogImpl.1
        public void onClientActiveChanged(boolean z) {
        }

        public void onClientActiveChangedWithPkg(boolean z, String str) {
            StringBuilder sb = new StringBuilder();
            sb.append("onClientActiveChanged, ");
            sb.append(z);
            sb.append(", pkg = ");
            sb.append(str);
            sb.append(", lastOwnerString = ");
            sb.append(OpFodHelper.getInstance().getLastOwner());
            sb.append(", null:");
            sb.append(OpBiometricDialogImpl.this.mFodDialogView == null);
            Log.d("OpBiometricDialogImpl", sb.toString());
        }

        public void onFingerprintEventCallback(int i, int i2, int i3, int i4) {
            IStatusBarService asInterface;
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBiometricDialogImpl", "onFingerprintEventCallback, " + i + ", " + i2);
            }
            if (i == 6 && i2 == 12) {
                OpBiometricDialogImpl.this.mHandler.postDelayed(new Runnable() { // from class: com.oneplus.systemui.biometrics.OpBiometricDialogImpl.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        OpFingerprintDialogView opFingerprintDialogView = OpBiometricDialogImpl.this.mFodDialogView;
                        if (opFingerprintDialogView != null) {
                            opFingerprintDialogView.setDimForFingerprintAcquired();
                        }
                    }
                }, (long) OpBiometricDialogImpl.FINGER_SET_DIM_DELAY);
            }
            if (i == 6 && i2 == 1 && (asInterface = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"))) != null) {
                try {
                    asInterface.onFingerprintAcquired(i, i2);
                } catch (RemoteException e) {
                    Log.e("OpBiometricDialogImpl", "onFingerprintEventCallback occur error", e);
                }
            }
            KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
            if (keyguardUpdateMonitor != null && !keyguardUpdateMonitor.isDeviceInteractive() && i == 1006) {
                keyguardUpdateMonitor.notifyFpAcquiredInfo(i2);
            }
        }
    };
    protected OpFingerprintDialogView mFodDialogView;
    protected OpFodFingerTouchValidator mFodFingerTouchValidator;
    protected OpFodWindowManager mFodWindowManager;
    private FingerprintUIHandler mHandler;
    private boolean mIsFaceUnlocked = false;
    private PowerManager mPowerManager;
    protected OpQLController mQLController;
    private final OpQLController.QLStateListener mQLStateListener = new OpQLController.QLStateListener() { // from class: com.oneplus.systemui.biometrics.OpBiometricDialogImpl.6
        @Override // com.oneplus.systemui.biometrics.OpQLController.QLStateListener
        public void onQLVisibilityChanged(boolean z) {
            if (!z) {
                OpBiometricDialogImpl.this.mFodFingerTouchValidator.reset();
                OpBiometricDialogImpl.this.collapseTransparentLayout();
                if (!OpBiometricDialogImpl.this.isDialogShowing()) {
                    OpBiometricDialogImpl.this.shouldRemoveTransparentIconView();
                }
            }
        }
    };
    private ScreenLifecycle mScreenLifecycle;
    final ScreenLifecycle.Observer mScreenObserver = new ScreenLifecycle.Observer() { // from class: com.oneplus.systemui.biometrics.OpBiometricDialogImpl.2
        @Override // com.android.systemui.keyguard.ScreenLifecycle.Observer
        public void onScreenTurnedOff() {
            OpBiometricDialogImpl.this.mFodFingerTouchValidator.reset();
            OpBiometricDialogImpl.this.cancelQLShowing();
        }
    };
    private OpFingerprintBlockTouchView mTransparentIconView;

    /* access modifiers changed from: private */
    public class FingerprintUIHandler extends Handler {
        public FingerprintUIHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    OpBiometricDialogImpl.this.opHandleShowDialog((SomeArgs) message.obj);
                    return;
                case 2:
                    OpBiometricDialogImpl.this.opHandleFingerprintAuthenticatedSuccess();
                    return;
                case 3:
                    OpBiometricDialogImpl.this.opHandleHideFodDialog((SomeArgs) message.obj);
                    return;
                case 4:
                default:
                    Log.w("OpBiometricDialogImpl", "Unknown message: " + message.what);
                    return;
                case 5:
                    OpBiometricDialogImpl.this.opHandleFingerprintError(message.arg1);
                    return;
                case 6:
                    OpBiometricDialogImpl.this.handleFingerprintAcquire(message.arg1, message.arg2);
                    return;
                case 7:
                    OpBiometricDialogImpl.this.handleFingerprintEnroll(message.arg1);
                    return;
                case 8:
                    OpBiometricDialogImpl.this.handleFingerprintAuthenticatedFail();
                    return;
                case 9:
                    OpBiometricDialogImpl.this.handleUpdateTransparentIconLayoutParams(((Boolean) message.obj).booleanValue());
                    return;
                case 10:
                    OpBiometricDialogImpl.this.handleUpdateTransparentIconVisibility(message.arg1);
                    return;
                case 11:
                    OpBiometricDialogImpl.this.mFodDialogView.onUiModeChanged();
                    return;
            }
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showFodDialog(Bundle bundle, String str) {
        if (DEBUG) {
            Log.d("OpBiometricDialogImpl", "showFodDialog: reason= " + str);
        }
        if (!this.mDialogShowingRequest) {
            Log.d("OpBiometricDialogImpl", "showFodDialog: !mDialogShowingRequest");
            this.mHandler.removeMessages(6);
            this.mHandler.removeMessages(7);
        }
        this.mHandler.removeMessages(8);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(5);
        this.mHandler.removeMessages(3);
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = bundle;
        obtain.arg2 = str;
        this.mHandler.removeMessages(1);
        this.mHandler.obtainMessage(1, obtain).sendToTarget();
    }

    public void hideFodDialogInner(String str) {
        Log.d("OpBiometricDialogImpl", "hideFodDialogInner: reason= " + str);
        hideFodDialog(null, str);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void hideFodDialog(Bundle bundle, String str) {
        if (DEBUG) {
            Log.d("OpBiometricDialogImpl", "hideFodDialog: reason= " + str);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(3);
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = bundle;
        obtain.arg2 = str;
        this.mHandler.obtainMessage(3, obtain).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onFingerprintAuthenticatedSuccess() {
        if (DEBUG) {
            Log.d("OpBiometricDialogImpl", "onFingerprintAuthenticatedSuccess");
        }
        boolean isDozing = OpLsState.getInstance().getPhoneStatusBar().getAodWindowManager().isDozing();
        boolean isInteractive = this.mPowerManager.isInteractive();
        if (DEBUG) {
            Log.d("OpBiometricDialogImpl", "opHandleHideFodDialog: aodAppear= " + isDozing + ", isInteractive= " + isInteractive);
        }
        if (isDozing && !isInteractive && shouldRemoveAodFirst()) {
            OpLsState.getInstance().getPhoneStatusBar().getAodWindowManager().stopDozing();
        }
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onFingerprintError(int i) {
        if (DEBUG) {
            Log.d("OpBiometricDialogImpl", "onFingerprintError: errorCode= " + i);
        }
        this.mHandler.obtainMessage(5, i, 0).sendToTarget();
    }

    public OpBiometricDialogImpl(Context context, CommandQueue commandQueue) {
        super(context);
        this.mCommandQueue = commandQueue;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        Log.d("OpBiometricDialogImpl", "start");
        PackageManager packageManager = this.mContext.getPackageManager();
        OpFodHelper.init(this.mContext);
        OpFodHelper.getInstance().addFingerprintStateChangeListener(this);
        HandlerThread handlerThread = new HandlerThread("FingerprintDialogUI", -8);
        handlerThread.start();
        this.mHandler = new FingerprintUIHandler(handlerThread.getLooper());
        if (packageManager.hasSystemFeature("android.hardware.fingerprint")) {
            this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
            WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
            ScreenLifecycle screenLifecycle = (ScreenLifecycle) Dependency.get(ScreenLifecycle.class);
            this.mScreenLifecycle = screenLifecycle;
            screenLifecycle.addObserver(this.mScreenObserver);
        }
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        IFingerprintService asInterface = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));
        if (asInterface != null) {
            try {
                asInterface.addClientActiveCallback(this.mFingerprintClientActiveCallback);
            } catch (RemoteException e) {
                Log.e("OpBiometricDialogImpl", "addClientActiveCallback: ", e);
            }
        }
        try {
            WindowManagerWrapper.getInstance().addPinnedStackListener(new OpFodImeListener());
        } catch (RemoteException e2) {
            Log.e("OpBiometricDialogImpl", "addPinnedStackListener: ", e2);
        }
        this.mFodDialogView = new OpFingerprintDialogView(this.mContext, this);
        this.mFodWindowManager = new OpFodWindowManager(this.mContext, this, this.mFodDialogView);
        this.mFodFingerTouchValidator = new OpFodFingerTouchValidator(this.mContext);
        OpFingerprintBlockTouchView opFingerprintBlockTouchView = (OpFingerprintBlockTouchView) LayoutInflater.from(this.mContext).inflate(C0011R$layout.op_fingerprint_icon, (ViewGroup) null);
        this.mTransparentIconView = opFingerprintBlockTouchView;
        opFingerprintBlockTouchView.setOnTouchListener(new View.OnTouchListener() { // from class: com.oneplus.systemui.biometrics.OpBiometricDialogImpl.3
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (OpBiometricDialogImpl.this.mFodDialogView == null) {
                    Log.d("OpBiometricDialogImpl", "mTransparentIconView onTouch mFodDialogView doesn't init yet");
                    return false;
                }
                int action = motionEvent.getAction();
                if (OpBiometricDialogImpl.DEBUG && (action == 1 || action == 0 || action == 3)) {
                    String str = null;
                    if (action == 1) {
                        str = "finger up";
                    } else if (action == 0) {
                        str = "finger down";
                    } else if (action == 3) {
                        str = "finger cancel";
                    }
                    Log.d("OpBiometricDialogImpl", "onTouchTransparent: " + str + ", mDialogShowing = " + OpBiometricDialogImpl.this.isDialogShowing() + ", " + OpBiometricDialogImpl.this.mTransparentIconView.toString() + ", isPendingHideDialog = " + OpBiometricDialogImpl.this.mFodDialogView.isPendingHideDialog() + ", isDreaming = " + ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDreaming() + ", getAodDisplayViewState():" + OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager().getAodDisplayViewState() + ", authenticated = " + OpBiometricDialogImpl.this.mAuthenticatedSuccess + ", pressState = " + OpBiometricDialogImpl.this.mFodFingerTouchValidator.toString());
                }
                if (OpBiometricDialogImpl.this.mAuthenticatedSuccess && action == 0 && OpLsState.getInstance().getPhoneStatusBar().getAodDisplayViewManager().getAodDisplayViewState() == 0) {
                    return false;
                }
                if (OpBiometricDialogImpl.this.mQLController.isQLShowing()) {
                    OpBiometricDialogImpl.this.mQLController.handleQLTouchEvent(motionEvent);
                    return true;
                }
                boolean validateFingerAction = OpBiometricDialogImpl.this.mFodFingerTouchValidator.validateFingerAction(action);
                if (OpBiometricDialogImpl.DEBUG && (action == 1 || action == 0)) {
                    Log.d("OpBiometricDialogImpl", "onTouchTransparent: validate= " + validateFingerAction + ", pressState= " + OpBiometricDialogImpl.this.mFodFingerTouchValidator.toString());
                }
                if (validateFingerAction) {
                    if (!OpBiometricDialogImpl.this.mFodFingerTouchValidator.isFingerDown()) {
                        OpBiometricDialogImpl.this.handleFingerprintPressUp();
                    } else if (OpBiometricDialogImpl.this.isDialogShowing() && !OpBiometricDialogImpl.this.mAuthenticatedSuccess) {
                        Log.d("OpBiometricDialogImpl", "onTouchTransparent: touch on view before authenticated success");
                        OpBiometricDialogImpl.this.mFodDialogView.doFingerprintPressDown(0);
                    } else if (OpBiometricDialogImpl.this.mAuthenticatedSuccess) {
                        Log.d("OpBiometricDialogImpl", "onTouchTransparent: touch on view after authenticated success");
                        OpBiometricDialogImpl.this.shouldShowQL();
                    }
                }
                return true;
            }
        });
        this.mTransparentIconView.setDialog(this);
        if (SHOW_TRANSPARENT_ICON_VIEW) {
            this.mTransparentIconView.setBackgroundColor(-65536);
            this.mTransparentIconView.setAlpha(0.3f);
        }
        this.mQLController = new OpQLController(this.mContext, this.mHandler, this, this.mFodDialogView, this.mFodFingerTouchValidator, this.mQLStateListener);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    @Override // com.oneplus.systemui.biometrics.OpFodHelper.OnFingerprintStateChangeListener
    public void onFingerprintStateChanged() {
        if (OpFodHelper.getInstance().isFingerprintSuspended() && OpFodHelper.getInstance().isDoingEnroll()) {
            this.mFodFingerTouchValidator.reset();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean inFingerprintDialogUiThread() {
        return "FingerprintDialogUI".equals(Thread.currentThread().getName());
    }

    /* access modifiers changed from: package-private */
    public Looper getMainLooper() {
        return this.mHandler.getLooper();
    }

    public void forceShowFodDialog(Bundle bundle) {
        Log.d("OpBiometricDialogImpl", "forceShowFodDialog callers: " + Debug.getCallers(3));
        this.mHandler.removeMessages(8);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(5);
        this.mHandler.removeMessages(3);
        this.mHandler.removeMessages(1);
        SomeArgs obtain = SomeArgs.obtain();
        obtain.arg1 = bundle;
        obtain.arg2 = "force show";
        this.mHandler.obtainMessage(1, obtain).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onFingerprintAcquired(int i, int i2) {
        if (DEBUG) {
            Log.d("OpBiometricDialogImpl", "onFingerprintAcquired: acquireInfo = " + i + ", vendorCode = " + i2);
        }
        getHandler().obtainMessage(6, i, i2).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onFingerprintEnrollResult(int i) {
        if (DEBUG) {
            Log.d("OpBiometricDialogImpl", "onFingerprintEnrollResult: remaining= " + i);
        }
        getHandler().obtainMessage(7, i, 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onFingerprintAuthenticatedFail() {
        if (DEBUG) {
            Log.d("OpBiometricDialogImpl", "onFingerprintAuthenticatedFail");
        }
        getHandler().obtainMessage(8).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void opHandleShowDialog(SomeArgs someArgs) {
        if (this.mFodDialogView == null) {
            Log.d("OpBiometricDialogImpl", "opHandleShowDialog mFodDialogView doesn't init yet");
            return;
        }
        Bundle bundle = (Bundle) someArgs.arg1;
        String string = bundle.getString("key_fingerprint_package_name", "");
        boolean z = bundle.getBoolean("key_resume");
        Log.d("OpBiometricDialogImpl", "opHandleShowDialog authenticatedPkg: " + string + ", reason: " + ((String) someArgs.arg2) + ", resume: " + z);
        if (TextUtils.isEmpty(string)) {
            Log.d("OpBiometricDialogImpl", "opHandleShowDialog: ownerString empty return");
            return;
        }
        this.mIsFaceUnlocked = false;
        this.mAuthenticatedSuccess = false;
        OpFodHelper.getInstance().updateOwner(bundle);
        if (this.mFodDialogView.isAnimatingAway()) {
            Log.d("OpBiometricDialogImpl", "opHandleShowDialog: Dialog is doing animating away, force remove first.");
            this.mFodDialogView.forceRemove();
        } else if (this.mDialogShowingRequest) {
            Log.w("OpBiometricDialogImpl", "opHandleShowDialog: Dialog already showing. , really added ? " + isDialogShowing());
            this.mFodDialogView.updateIconVisibility(false);
            return;
        }
        this.mDialogShowingRequest = true;
        this.mFodDialogView.addToWindow();
        this.mTransparentIconView.addToWindow();
    }

    /* access modifiers changed from: protected */
    public void opHandleFingerprintAuthenticatedSuccess() {
        Log.d("OpBiometricDialogImpl", "opHandleFingerprintAuthenticatedSuccess");
        this.mAuthenticatedSuccess = true;
        this.mFodDialogView.notifyFingerprintAuthenticated();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void opHandleFingerprintError(int i) {
        Log.d("OpBiometricDialogImpl", "opHandleFingerprintError: errorCode= " + i);
        OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
        if (opFingerprintDialogView != null) {
            opFingerprintDialogView.onFpEventCancel();
        }
        if (i == 9) {
            Log.d("OpBiometricDialogImpl", "opHandleFingerprintError: in lockout state.");
            OpFodHelper.getInstance().changeState(OpFodHelper.FingerprintState.LOCKOUT);
        } else if (i == 5) {
            OpFodHelper.getInstance().changeState(OpFodHelper.FingerprintState.STOP);
        }
        if (i != 5) {
            collapseTransparentLayout();
            shouldRemoveTransparentIconView();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void opHandleHideFodDialog(SomeArgs someArgs) {
        Bundle bundle = (Bundle) someArgs.arg1;
        String str = (String) someArgs.arg2;
        boolean z = bundle != null ? bundle.getBoolean("key_suspend", false) : false;
        Log.d("OpBiometricDialogImpl", "opHandleHideFodDialog: do hide dialog. reason: " + str + ", suspend: " + z);
        if (!this.mDialogShowingRequest) {
            Log.w("OpBiometricDialogImpl", "opHandleHideFodDialog: Dialog already dismissed.");
            return;
        }
        this.mFodDialogView.onFpEventCancel();
        boolean isDozing = OpLsState.getInstance().getPhoneStatusBar().getAodWindowManager().isDozing();
        boolean isInteractive = this.mPowerManager.isInteractive();
        if (DEBUG) {
            Log.d("OpBiometricDialogImpl", "opHandleHideFodDialog: aodAppear= " + isDozing + ", isInteractive= " + isInteractive);
        }
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        if ((!keyguardUpdateMonitor.isKeyguardDone() || (isDozing && !isInteractive)) && !keyguardUpdateMonitor.isFingerprintAlreadyAuthenticated() && keyguardUpdateMonitor.isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser()) && !keyguardUpdateMonitor.isSwitchingUser() && ((!OpLsState.getInstance().getStatusBarKeyguardViewManager().isOccluded() && !OpLsState.getInstance().getPhoneStatusBar().isInLaunchTransition()) || OpLsState.getInstance().getPhoneStatusBar().isBouncerShowing())) {
            Log.d("OpBiometricDialogImpl", "opHandleHideFodDialog: don't hide window since keyguard is showing");
        } else if (this.mFodDialogView.isPendingHideDialog()) {
            Log.d("OpBiometricDialogImpl", "opHandleHideFodDialog: don't hide window since pending hide dialog until animation end");
        } else if (z) {
            Log.d("OpBiometricDialogImpl", "opHandleHideFodDialog: suspend return");
            OpFodHelper.getInstance().updateOwner(bundle);
        } else {
            this.mDialogShowingRequest = false;
            OpFodHelper.getInstance().updateOwner(null);
            this.mFodDialogView.startDismiss(this.mAuthenticatedSuccess);
            Log.d("OpBiometricDialogImpl:removeTransparentIconView", "opHandleHideFodDialog: removeTransparentIconView , isRequestShowing= " + this.mTransparentIconView.isRequestShowing() + ", mAuthenticatedSuccess= " + this.mAuthenticatedSuccess + ", " + this.mFodFingerTouchValidator.toString());
            if (this.mTransparentIconView.isRequestShowing() && !this.mFodFingerTouchValidator.isFingerDown()) {
                collapseTransparentLayout();
                shouldRemoveTransparentIconView();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (configuration.orientation == 2) {
            boolean isQLShowing = this.mQLController.isQLShowing();
            Log.d("OpBiometricDialogImpl", "onConfigurationChanged: landscape , " + this.mTransparentIconView.toString() + ", mQLShowing = " + isQLShowing + ", pressState = " + this.mFodFingerTouchValidator.toString() + ", authenticated= " + this.mAuthenticatedSuccess + ", faceUnlocked= " + this.mIsFaceUnlocked);
            if (isQLShowing) {
                cancelQLShowing();
            }
            if (this.mFodFingerTouchValidator.isFingerDown() && this.mTransparentIconView.isRequestShowing()) {
                this.mFodFingerTouchValidator.reset();
                this.mIsFaceUnlocked = false;
                collapseTransparentLayout();
                if (!isDialogShowing()) {
                    this.mHandler.post(new Runnable() { // from class: com.oneplus.systemui.biometrics.OpBiometricDialogImpl.4
                        @Override // java.lang.Runnable
                        public void run() {
                            OpBiometricDialogImpl.this.shouldRemoveTransparentIconView();
                        }
                    });
                }
            }
        }
    }

    public void expandTransparentLayout() {
        updateTransparentIconLayoutParams(true);
    }

    public void collapseTransparentLayout() {
        updateTransparentIconLayoutParams(false);
    }

    private void updateTransparentIconLayoutParams(boolean z) {
        getHandler().removeMessages(9);
        Message obtainMessage = getHandler().obtainMessage(9);
        obtainMessage.obj = Boolean.valueOf(z);
        getHandler().sendMessage(obtainMessage);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateTransparentIconLayoutParams(boolean z) {
        if (this.mQLController.isQLShowing()) {
            z = true;
        }
        this.mFodWindowManager.handleUpdateTransparentIconLayoutParams(z);
    }

    public void updateTransparentIconVisibility(int i) {
        getHandler().removeMessages(10);
        Message obtainMessage = getHandler().obtainMessage(10);
        obtainMessage.arg1 = i;
        getHandler().sendMessage(obtainMessage);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateTransparentIconVisibility(int i) {
        boolean isFingerDown = this.mFodFingerTouchValidator.isFingerDown();
        boolean isFingerprintSuspended = OpFodHelper.getInstance().isFingerprintSuspended();
        boolean isDoingEnroll = OpFodHelper.getInstance().isDoingEnroll();
        Log.d("OpBiometricDialogImpl", "handleUpdateTransparentIconVisibility: pressState= " + this.mFodFingerTouchValidator.toString() + ", isEnrollClient= " + isDoingEnroll + ", isFpSuspended= " + isFingerprintSuspended);
        if (i != 8 || !isFingerDown || (isDoingEnroll && !isFingerprintSuspended)) {
            Log.d("OpBiometricDialogImpl", "handleUpdateTransparentIconVisibility: visibility= " + i);
            if (i == 8) {
                Log.d("OpBiometricDialogImpl", "handleUpdateTransparentIconVisibility: collapse transparent icon layout while visibility set to gone");
                collapseTransparentLayout();
            }
            this.mTransparentIconView.setVisibility(i);
            return;
        }
        Log.d("OpBiometricDialogImpl", "handleUpdateTransparentIconVisibility: finger down do not hide it");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintAcquire(int i, int i2) {
        boolean isDeviceInteractive = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive();
        boolean isPendingHideDialog = this.mFodDialogView.isPendingHideDialog();
        Log.d("OpBiometricDialogImpl", "handleFingerprintAcquire: acquireInfo = " + i + ", vendorCode = " + i2 + ", interactive = " + isDeviceInteractive + ", " + this.mTransparentIconView.toString() + ", dialogShowing = " + isDialogShowing() + ", pendingHideDialog = " + isPendingHideDialog + ", pressState = " + this.mFodFingerTouchValidator.toString());
        if (i != 0 && i != 6) {
            this.mFodDialogView.onFpEventCancel();
        } else if (i == 6) {
            if (i2 == 0) {
                this.mFodDialogView.removePressTimeOutMessage();
            }
            if (!this.mQLController.isQLShowing()) {
                boolean validateFingerAction = this.mFodFingerTouchValidator.validateFingerAction(i, i2);
                Log.d("OpBiometricDialogImpl", "handleFingerprintAcquire: validate= " + validateFingerAction + ", pressState:= " + this.mFodFingerTouchValidator.toString());
                if (!validateFingerAction) {
                    return;
                }
                if (this.mFodFingerTouchValidator.isFingerDown()) {
                    this.mFodDialogView.doFingerprintPressDown(1);
                } else {
                    handleFingerprintPressUp();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintPressUp() {
        boolean isEmptyClient = OpFodHelper.getInstance().isEmptyClient();
        Log.d("OpBiometricDialogImpl", "handleFingerprintPressUp: isEmptyClient= " + isEmptyClient + ", callers= " + Debug.getCallers(2));
        collapseTransparentLayout();
        if (isEmptyClient) {
            Log.d("OpBiometricDialogImpl", "handleFingerprintPressUp: finger press up and client is empty.");
            shouldRemoveTransparentIconView();
        }
        if (!this.mFodDialogView.isPendingHideDialog()) {
            this.mFodDialogView.doFingerprintPressUp();
        }
        cancelQLShowing();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintEnroll(int i) {
        OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
        if (opFingerprintDialogView == null) {
            Log.d("OpBiometricDialogImpl", "handleFingerprintEnroll mFodDialogView doesn't init yet");
            return;
        }
        opFingerprintDialogView.onFpEventCancel();
        boolean isFingerDownOnSensor = this.mFodFingerTouchValidator.isFingerDownOnSensor();
        Log.d("OpBiometricDialogImpl", "handleFingerprintEnroll: remaining= " + i + ", pressState= " + this.mFodFingerTouchValidator.toString());
        if (i == 0 && isFingerDownOnSensor) {
            this.mFodFingerTouchValidator.resetTouchFromSensor();
            handleFingerprintPressUp();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintAuthenticatedFail() {
        OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
        if (opFingerprintDialogView == null) {
            Log.d("OpBiometricDialogImpl", "handleFingerprintAuthenticatedFail mFodDialogView doesn't init yet");
            return;
        }
        opFingerprintDialogView.onFpEventCancel();
        collapseTransparentLayout();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        if (this.mFodWindowManager != null) {
            this.mHandler.post(new Runnable() { // from class: com.oneplus.systemui.biometrics.OpBiometricDialogImpl.5
                @Override // java.lang.Runnable
                public void run() {
                    OpBiometricDialogImpl.this.mFodWindowManager.onDensityOrFontScaleChanged();
                }
            });
        }
    }

    public void onFingerprintDialogDismissDone() {
        Log.d("OpBiometricDialogImpl", "onFingerprintDialogDismissDone: " + this.mTransparentIconView.toString() + ", pressState = " + this.mFodFingerTouchValidator.toString() + ", authenticated= " + this.mAuthenticatedSuccess + ", faceUnlocked= " + this.mIsFaceUnlocked);
        shouldShowQL();
        this.mIsFaceUnlocked = false;
    }

    public boolean isAuthenticateSuccess() {
        return this.mAuthenticatedSuccess;
    }

    private boolean isQLEnabled() {
        return this.mQLController.isQLEnabled();
    }

    public void onFaceUnlocked() {
        this.mIsFaceUnlocked = true;
        if (DEBUG) {
            Log.d("OpBiometricDialogImpl", "onFaceUnlocked mIsEnableQL " + isQLEnabled() + this.mTransparentIconView.toString());
        }
        cancelQLShowing();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelQLShowing() {
        if (isQLEnabled()) {
            this.mQLController.interruptShowingQLView();
        }
    }

    private void removeTransparentIconView() {
        OpFingerprintBlockTouchView opFingerprintBlockTouchView = this.mTransparentIconView;
        if (opFingerprintBlockTouchView != null) {
            opFingerprintBlockTouchView.removeFromWindow();
        }
    }

    private Handler getHandler() {
        return this.mHandler;
    }

    /* access modifiers changed from: package-private */
    public boolean isDialogShowing() {
        OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
        return opFingerprintDialogView != null && opFingerprintDialogView.isDialogShowing();
    }

    /* access modifiers changed from: package-private */
    public boolean isDialogShowingRequest() {
        return this.mDialogShowingRequest;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() {
        OpFodWindowManager opFodWindowManager = this.mFodWindowManager;
        if (opFodWindowManager != null) {
            opFodWindowManager.onOverlayChanged();
            if (this.mTransparentIconView.isRequestShowing()) {
                updateTransparentIconLayoutParams(this.mFodWindowManager.isTransparentViewExpanded());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isFingerDown() {
        return this.mFodFingerTouchValidator.isFingerDown();
    }

    /* access modifiers changed from: package-private */
    public boolean isFaceUnlocked() {
        return this.mIsFaceUnlocked;
    }

    /* access modifiers changed from: package-private */
    public boolean isFodHighlighted() {
        return this.mFodDialogView.isFodHighlighted();
    }

    public void hideFodImmediately() {
        boolean isBiometricPromptReadyToShow = OpFodHelper.getInstance().isBiometricPromptReadyToShow();
        Log.d("OpBiometricDialogImpl", "hideFodImmediately: shouldForceHideByBiometric= " + isBiometricPromptReadyToShow);
        if (isBiometricPromptReadyToShow) {
            this.mFodDialogView.updateIconVisibility(true);
        }
    }

    public void onBiometricPromptReady(int i) {
        if (OpFodHelper.getInstance().updateBiometricPromptReady(i)) {
            this.mFodDialogView.updateIconVisibility(false);
        }
    }

    public static class OpFingerprintBlockTouchView extends AddRemoveRequestingView {
        public OpFingerprintBlockTouchView(Context context) {
            this(context, null);
        }

        public OpFingerprintBlockTouchView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        /* access modifiers changed from: protected */
        @Override // com.oneplus.systemui.biometrics.AddRemoveRequestingView
        public void addAlready() {
            this.mDialog.collapseTransparentLayout();
        }

        @Override // com.oneplus.systemui.biometrics.AddRemoveRequestingView, android.view.View, java.lang.Object
        public String toString() {
            return String.format("([%s]: mShowingRequest: %b, isAttachedToWindow: %b)", "OpFingerprintBlockTouchView", Boolean.valueOf(isRequestShowing()), Boolean.valueOf(isAttachedToWindow())).toString();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldShowQL() {
        Log.d("OpBiometricDialogImpl", "shouldShowQL: mAuthenticatedSuccess= " + this.mAuthenticatedSuccess + ", isKeyguardUnlocked= " + OpFodHelper.getInstance().isKeyguardUnlocked() + ", isFaceUnlocked= " + this.mIsFaceUnlocked);
        if (!this.mAuthenticatedSuccess || !OpFodHelper.getInstance().isKeyguardUnlocked() || this.mIsFaceUnlocked) {
            return false;
        }
        this.mQLController.shouldShowQL();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldRemoveTransparentIconView() {
        boolean isFingerDown = this.mFodFingerTouchValidator.isFingerDown();
        Log.d("OpBiometricDialogImpl:removeTransparentIconView", "shouldRemoveTransparentIconView: isFingerDownOnView= " + isFingerDown + ", callers= " + Debug.getCallers(2));
        if (isFingerDown) {
            return false;
        }
        removeTransparentIconView();
        return true;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        Log.d("OpBiometricDialogImpl", "onUiModeChanged");
        this.mHandler.removeMessages(11);
        this.mHandler.sendEmptyMessage(11);
    }

    private class OpFodImeListener extends PinnedStackListenerForwarder.PinnedStackListener {
        private OpFodImeListener(OpBiometricDialogImpl opBiometricDialogImpl) {
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onImeVisibilityChanged(boolean z, int i) {
            if (OpBiometricDialogImpl.DEBUG) {
                Log.d("OpBiometricDialogImpl", "onImeVisibilityChanged: imeVisible= " + z + ", imeHeight= " + i);
            }
            ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).onImeShow(z);
        }
    }

    private boolean shouldRemoveAodFirst() {
        boolean z = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isAlwaysOnEnabled() && !OpCanvasAodHelper.isCanvasAodEnabled(this.mContext);
        if (DEBUG) {
            Log.i("OpBiometricDialogImpl", "shouldRemoveAodFirst= " + z);
        }
        return z;
    }
}
