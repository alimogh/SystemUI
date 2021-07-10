package com.oneplus.plugin;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SystemSensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.util.OpFeatures;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.android.internal.view.IInputMethodManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.LockscreenWallpaper;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.scene.OpSceneModeObserver;
import com.oneplus.util.OpUtils;
public class OpPreventModeCtrl extends OpBaseCtrl implements ConfigurationController.ConfigurationListener {
    private static final boolean IS_SUPPORT_POCKET_SWITCH = OpFeatures.isSupport(new int[]{116});
    private static final boolean IS_SUPPORT_UNDERSCREEN_SENSOR = OpFeatures.isSupport(new int[]{199});
    private static boolean mPreventModeActive = false;
    private static boolean mPreventModeNoBackground = false;
    private static boolean mSensorEnabled = false;
    private ValueAnimator mAlphaAnimator;
    ImageView mBackground;
    private Drawable mDrawable;
    private Handler mHandler;
    private int mKeyLockMode;
    private boolean mKeyguardIsShowing = false;
    private boolean mKeyguardIsVisible = false;
    private int mNearThreshold = 0;
    private Object mObject = new Object();
    private OpSceneModeObserver mOpSceneModeObserver;
    OpPreventModeView mPMView;
    private Sensor mSensor;
    private SensorEventListener mSensorListener;
    private SensorManager mSensorManager;

    @Override // com.oneplus.plugin.OpBaseCtrl
    public void onKeyguardBouncerChanged(boolean z) {
    }

    @Override // com.oneplus.plugin.OpBaseCtrl
    public void onScreenTurnedOn() {
    }

    @Override // com.oneplus.plugin.OpBaseCtrl
    public void onStartCtrl() {
        Log.d("OpPreventModeCtrl", "onStartCtrl");
        OpLsState.getInstance().getStatusBarKeyguardViewManager();
        this.mPMView = (OpPreventModeView) OpLsState.getInstance().getContainer().findViewById(C0008R$id.prevent_mode_view);
        this.mBackground = (ImageView) OpLsState.getInstance().getContainer().findViewById(C0008R$id.pevent_mode_background);
        this.mPMView.init();
        this.mHandler = new SensorHandler();
        SystemSensorManager systemSensorManager = new SystemSensorManager(this.mContext, this.mHandler.getLooper());
        this.mSensorManager = systemSensorManager;
        this.mSensor = systemSensorManager.getDefaultSensor(IS_SUPPORT_UNDERSCREEN_SENSOR ? 33171025 : 8, true);
        this.mSensorListener = IS_SUPPORT_UNDERSCREEN_SENSOR ? new PocketSensorListener() : new ProximitorySensorListener();
        this.mNearThreshold = IS_SUPPORT_UNDERSCREEN_SENSOR ? 1 : 0;
        this.mOpSceneModeObserver = (OpSceneModeObserver) Dependency.get(OpSceneModeObserver.class);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    @Override // com.oneplus.plugin.OpBaseCtrl
    public void onStartedWakingUp() {
        if (this.mHandler != null && isPreventModeEnabled()) {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            this.mHandler.sendEmptyMessage(1);
        }
    }

    @Override // com.oneplus.plugin.OpBaseCtrl
    public void onFinishedGoingToSleep(int i) {
        disableSensor();
    }

    @Override // com.oneplus.plugin.OpBaseCtrl
    public void onKeyguardVisibilityChanged(boolean z) {
        this.mKeyguardIsVisible = z;
    }

    public void setKeyguardShowing(boolean z) {
        Log.d("OpPreventModeCtrl", "setKeyguardShowing, " + z);
        if (!(this.mKeyguardIsShowing == z || OpLsState.getInstance().getPhoneStatusBar() == null)) {
            Bitmap lockscreenWallpaper = OpLsState.getInstance().getPhoneStatusBar().getLockscreenWallpaper();
            if (z) {
                OpPreventModeView opPreventModeView = this.mPMView;
                if (opPreventModeView != null) {
                    opPreventModeView.create();
                }
            } else if (this.mPMView != null) {
                disableSensor();
                this.mPMView.clear();
            }
            if (!z || lockscreenWallpaper == null) {
                this.mDrawable = null;
                this.mBackground.setImageDrawable(null);
            } else {
                this.mDrawable = new LockscreenWallpaper.WallpaperDrawable(this.mContext.getResources(), lockscreenWallpaper);
            }
        }
        this.mKeyguardIsShowing = z;
    }

    public void disPatchTouchEvent(MotionEvent motionEvent) {
        OpPreventModeView opPreventModeView = this.mPMView;
        if (opPreventModeView != null) {
            opPreventModeView.dispatchTouchEvent(motionEvent);
        }
    }

    private boolean isPreventModeEnabled() {
        if (IS_SUPPORT_POCKET_SWITCH && OpUtils.isPreventModeEnabled(this.mContext) && this.mKeyguardIsShowing) {
            return true;
        }
        return false;
    }

    private class ProximitorySensorListener implements SensorEventListener {
        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        private ProximitorySensorListener() {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (OpPreventModeCtrl.this.mObject) {
                boolean z = false;
                if (sensorEvent.values.length == 0) {
                    Log.d("OpPreventModeCtrl", "Proximity: Event has no values!");
                    finishWithResult(0);
                } else {
                    Log.d("OpPreventModeCtrl", "Proximity: Event: value=" + sensorEvent.values[0] + " max=" + OpPreventModeCtrl.this.mSensor.getMaximumRange() + ", threshold=" + OpPreventModeCtrl.this.mNearThreshold);
                    int i = 1;
                    if (sensorEvent.values[0] == ((float) OpPreventModeCtrl.this.mNearThreshold)) {
                        z = true;
                    }
                    if (!z) {
                        i = 2;
                    }
                    finishWithResult(i);
                }
            }
        }

        private void finishWithResult(int i) {
            Log.d("OpPreventModeCtrl", "finishWithResult: result = " + i);
            if (i == 1) {
                OpPreventModeCtrl.this.startRootAnimation();
            } else if (i == 2 && OpPreventModeCtrl.mPreventModeActive) {
                OpPreventModeView opPreventModeView = OpPreventModeCtrl.this.mPMView;
                if (opPreventModeView != null) {
                    opPreventModeView.setVisibility(8);
                }
                OpPreventModeCtrl.this.stopPreventMode();
            } else if (i == 0) {
                OpPreventModeCtrl.this.stopPreventMode();
            }
        }
    }

    /* access modifiers changed from: private */
    public class PocketSensorListener implements SensorEventListener {
        protected int mCurrentResult;

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        private PocketSensorListener() {
            this.mCurrentResult = -1;
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            synchronized (OpPreventModeCtrl.this.mObject) {
                if (sensorEvent.values.length == 0) {
                    Log.d("OpPreventModeCtrl", "Pocket: Event has no values!");
                    finishWithResult(-1);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Pocket: Event: value=");
                    int i = 0;
                    sb.append(sensorEvent.values[0]);
                    sb.append(" max=");
                    sb.append(OpPreventModeCtrl.this.mSensor.getMaximumRange());
                    sb.append(", threshold=");
                    sb.append(OpPreventModeCtrl.this.mNearThreshold);
                    Log.d("OpPreventModeCtrl", sb.toString());
                    if (sensorEvent.values[0] == ((float) OpPreventModeCtrl.this.mNearThreshold)) {
                        i = 1;
                    }
                    finishWithResult(i);
                }
            }
        }

        private void finishWithResult(int i) {
            Log.d("OpPreventModeCtrl", "finishWithResult: result = " + i + ", current = " + this.mCurrentResult);
            if (i == 1 && this.mCurrentResult == -1) {
                OpPreventModeCtrl.this.startRootAnimation();
            } else if (i == 0) {
                OpPreventModeCtrl.this.mHandler.removeMessages(4);
                if (OpPreventModeCtrl.mPreventModeActive) {
                    OpPreventModeView opPreventModeView = OpPreventModeCtrl.this.mPMView;
                    if (opPreventModeView != null) {
                        opPreventModeView.setVisibility(8);
                    }
                    OpPreventModeCtrl.this.stopPreventMode();
                }
            } else if (i == -1) {
                OpPreventModeCtrl.this.stopPreventMode();
            } else if (i == 1 && this.mCurrentResult == 0) {
                OpPreventModeCtrl.this.mHandler.sendEmptyMessageDelayed(4, 2000);
            }
            this.mCurrentResult = i;
        }

        public void resetState() {
            this.mCurrentResult = -1;
        }
    }

    private class SensorHandler extends Handler {
        private SensorHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                synchronized (OpPreventModeCtrl.this.mObject) {
                    OpPreventModeCtrl.this.enableSensorInternal();
                }
            } else if (i != 4) {
                synchronized (OpPreventModeCtrl.this.mObject) {
                    OpPreventModeCtrl.this.disableSensorInternal();
                    OpPreventModeCtrl.this.stopPreventMode();
                }
            } else {
                OpPreventModeCtrl.this.startRootAnimation();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableSensorInternal() {
        if (!mSensorEnabled) {
            StringBuilder sb = new StringBuilder();
            sb.append("enableSensor, ");
            sb.append(this.mSensor);
            sb.append(", type: ");
            sb.append(IS_SUPPORT_UNDERSCREEN_SENSOR ? "TYPE_POCKET" : "TYPE_PROXIMITY");
            Log.d("OpPreventModeCtrl", sb.toString());
            Drawable drawable = this.mDrawable;
            if (drawable != null) {
                this.mBackground.setImageDrawable(drawable);
            }
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                this.mSensorManager.registerListener(this.mSensorListener, this.mSensor, 3);
                mSensorEnabled = true;
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
    }

    public void disableSensor() {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.removeMessages(1);
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(4);
            this.mHandler.sendEmptyMessage(2);
        }
    }

    public void stopPreventMode() {
        if (mPreventModeActive) {
            Log.d("OpPreventModeCtrl", "stopPreventMode");
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                if (this.mPMView != null) {
                    this.mPMView.setVisibility(8);
                }
                this.mBackground.setImageDrawable(null);
                if (mPreventModeNoBackground && OpLsState.getInstance().getPhoneStatusBar() != null) {
                    OpLsState.getInstance().getPhoneStatusBar().setPanelViewAlpha(1.0f, true, -1);
                    Log.d("OpPreventModeCtrl", "panel alpha to 1");
                }
                mPreventModeNoBackground = false;
                mPreventModeActive = false;
                if (OpLsState.getInstance().getPhoneStatusBar() != null) {
                    OpLsState.getInstance().getPhoneStatusBar().notifyPreventModeChange(false);
                }
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableSensorInternal() {
        if (mSensorEnabled) {
            Log.d("OpPreventModeCtrl", "disableSensor, " + this.mKeyLockMode);
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                this.mSensorManager.unregisterListener(this.mSensorListener);
                if (IS_SUPPORT_UNDERSCREEN_SENSOR) {
                    ((PocketSensorListener) this.mSensorListener).resetState();
                }
                mSensorEnabled = false;
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startRootAnimation() {
        if (!mPreventModeActive && this.mKeyguardIsShowing && !bypassPreventMode()) {
            StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
            this.mKeyLockMode = Settings.System.getInt(this.mContext.getContentResolver(), "oem_acc_key_lock_mode", -1);
            hideSoftInput();
            Log.d("OpPreventModeCtrl", "startRootAnimation, " + this.mKeyLockMode + ", " + this.mBackground.getDrawable());
            mPreventModeActive = true;
            if (this.mBackground.getDrawable() == null && phoneStatusBar != null) {
                phoneStatusBar.setPanelViewAlpha(0.0f, true, -1);
                mPreventModeNoBackground = true;
                Log.d("OpPreventModeCtrl", "panel alpha to 0");
            }
            if (phoneStatusBar != null) {
                if (phoneStatusBar.getFacelockController() != null) {
                    phoneStatusBar.getFacelockController().stopFacelockLightMode();
                }
                phoneStatusBar.notifyPreventModeChange(true);
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.mAlphaAnimator = ofFloat;
            ofFloat.setDuration(0L);
            this.mAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.plugin.OpPreventModeCtrl.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    OpPreventModeView opPreventModeView = OpPreventModeCtrl.this.mPMView;
                    if (opPreventModeView != null) {
                        opPreventModeView.setAlpha(floatValue);
                    }
                }
            });
            this.mAlphaAnimator.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.plugin.OpPreventModeCtrl.2
                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    if (OpPreventModeCtrl.this.mDrawable != null) {
                        OpPreventModeCtrl opPreventModeCtrl = OpPreventModeCtrl.this;
                        opPreventModeCtrl.mBackground.setImageDrawable(opPreventModeCtrl.mDrawable);
                    }
                    OpPreventModeCtrl.this.mPMView.setVisibility(0);
                }
            });
            this.mAlphaAnimator.start();
        }
    }

    public boolean isPreventModeActive() {
        return mPreventModeActive;
    }

    public boolean isPreventModeNoBackground() {
        return mPreventModeNoBackground;
    }

    private void hideSoftInput() {
        try {
            IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method")).hideSoftInputForLongshot(0, (ResultReceiver) null);
        } catch (Exception e) {
            Log.w("OpPreventModeCtrl", "hide ime failed, ", e);
        }
    }

    private boolean bypassPreventMode() {
        if (((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isSimPinSecure()) {
            return true;
        }
        if (this.mKeyguardIsVisible) {
            return false;
        }
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        if (phoneStatusBar != null && phoneStatusBar.bypassPreventMode()) {
            return true;
        }
        OpSceneModeObserver opSceneModeObserver = this.mOpSceneModeObserver;
        if (opSceneModeObserver == null || !opSceneModeObserver.isInBrickMode()) {
            return false;
        }
        return true;
    }

    public void onPanelExpandedChange(boolean z) {
        if (mPreventModeActive) {
            Log.d("OpPreventModeCtrl", "onPanelExpandedChange expand:" + z + " mPreventModeActive:" + mPreventModeActive + " visible:" + this.mKeyguardIsVisible);
        }
        if (mPreventModeActive && this.mPMView != null) {
            if (!this.mKeyguardIsVisible || z) {
                this.mPMView.setAlpha(z ? 1.0f : 0.0f);
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        OpPreventModeView opPreventModeView = this.mPMView;
        if (opPreventModeView != null) {
            opPreventModeView.init();
        }
    }
}
