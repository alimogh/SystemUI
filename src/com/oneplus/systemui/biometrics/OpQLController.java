package com.oneplus.systemui.biometrics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0011R$layout;
import com.oneplus.util.OpUtils;
import com.oneplus.util.VibratorSceneUtils;
public class OpQLController {
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    static final boolean IS_SUPPORT_QL = OpUtils.isSupportQuickLaunch();
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private Context mContext;
    private OpBiometricDialogImpl mDialogImpl;
    private OpFingerprintDialogView mFodDialogView;
    private OpFodFingerTouchValidator mFodFingerTouchValidator;
    private Handler mHandler;
    private boolean mIsEnableQL;
    private QLStateListener mListener;
    private String mQLConfig;
    private OpQLRootView mQLRootView;
    private volatile boolean mQLShowing;
    private Runnable mShowQLView = new Runnable() { // from class: com.oneplus.systemui.biometrics.-$$Lambda$OpQLController$o3HMbj7GAaeqUfGjvIiNwK3IysY
        @Override // java.lang.Runnable
        public final void run() {
            OpQLController.this.lambda$new$1$OpQLController();
        }
    };
    private WindowManager mWindowManager;

    public interface QLStateListener {
        void onQLVisibilityChanged(boolean z);
    }

    public OpQLController(Context context, Handler handler, OpBiometricDialogImpl opBiometricDialogImpl, OpFingerprintDialogView opFingerprintDialogView, OpFodFingerTouchValidator opFodFingerTouchValidator, QLStateListener qLStateListener) {
        this.mContext = context;
        this.mHandler = handler;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mDialogImpl = opBiometricDialogImpl;
        this.mFodDialogView = opFingerprintDialogView;
        this.mFodFingerTouchValidator = opFodFingerTouchValidator;
        this.mListener = qLStateListener;
        if (IS_SUPPORT_QL) {
            new QLContentObserver();
            new QLReceiver();
        }
    }

    public boolean isQLEnabled() {
        return IS_SUPPORT_QL && this.mIsEnableQL && KeyguardUpdateMonitor.getCurrentUser() == 0;
    }

    public boolean shouldShowQL() {
        if (!isQLEnabled()) {
            return false;
        }
        if (this.mQLShowing) {
            if (DEBUG) {
                Log.d("OpQLController", "QL view is already showing");
            }
            return false;
        }
        if (DEBUG) {
            Log.d("OpQLController", "shouldShowQL: isFingerDown: " + this.mFodFingerTouchValidator.isFingerDownOnView());
        }
        if (!this.mFodFingerTouchValidator.isFingerDownOnView()) {
            return false;
        }
        this.mHandler.removeCallbacks(this.mShowQLView);
        this.mHandler.postDelayed(this.mShowQLView, 500);
        if (!DEBUG) {
            return true;
        }
        Log.d("OpQLController", "shouldShowQL: waiting to show...");
        return true;
    }

    public boolean isQLShowing() {
        return this.mQLShowing;
    }

    public void interruptShowingQLView() {
        if (this.mHandler.hasCallbacks(this.mShowQLView)) {
            Log.d("OpQLController", "interrupt showing ql view " + Debug.getCallers(2));
            this.mHandler.removeCallbacks(this.mShowQLView);
        } else if (!this.mDialogImpl.inFingerprintDialogUiThread()) {
            this.mHandler.post(new Runnable() { // from class: com.oneplus.systemui.biometrics.OpQLController.1
                @Override // java.lang.Runnable
                public void run() {
                    OpQLController.this.shouldHideQLView();
                }
            });
        } else {
            shouldHideQLView();
        }
    }

    public void shouldHideQLView() {
        if (this.mQLShowing && this.mQLRootView != null) {
            if (DEBUG) {
                Log.d("OpQLController", "hideQLView");
            }
            this.mHandler.removeCallbacks(this.mShowQLView);
            $$Lambda$OpQLController$2TCG8Dg9bfuOlZpWl6dbwhpYtg4 r0 = new Runnable() { // from class: com.oneplus.systemui.biometrics.-$$Lambda$OpQLController$2TCG8Dg9bfuOlZpWl6dbwhpYtg4
                @Override // java.lang.Runnable
                public final void run() {
                    OpQLController.this.lambda$shouldHideQLView$0$OpQLController();
                }
            };
            if (!this.mQLRootView.isAttachedToWindow()) {
                this.mHandler.postDelayed(r0, 500);
            } else {
                r0.run();
            }
            OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
            if (opFingerprintDialogView != null) {
                opFingerprintDialogView.updateIconVisibility(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$shouldHideQLView$0 */
    public /* synthetic */ void lambda$shouldHideQLView$0$OpQLController() {
        OpQLRootView opQLRootView = this.mQLRootView;
        if (opQLRootView != null) {
            this.mWindowManager.removeViewImmediate(opQLRootView);
            this.mQLRootView.onQLExit();
            this.mQLRootView = null;
        }
        if (DEBUG) {
            Log.d("OpQLController", "mQLShowing set to false");
        }
        this.mQLShowing = false;
        notifyQLViewVisibilityChanged(false);
    }

    public void handleQLTouchEvent(MotionEvent motionEvent) {
        OpQLRootView opQLRootView;
        int action = motionEvent.getAction();
        if (DEBUG && (action == 0 || action == 1)) {
            StringBuilder sb = new StringBuilder();
            sb.append("mQLShowing ");
            sb.append(this.mQLShowing);
            sb.append(" mQLRootView ");
            sb.append(this.mQLRootView);
            sb.append(" attach ");
            OpQLRootView opQLRootView2 = this.mQLRootView;
            sb.append(opQLRootView2 != null ? Boolean.valueOf(opQLRootView2.isAttachedToWindow()) : null);
            Log.d("OpQLController", sb.toString());
        }
        if (isQLEnabled()) {
            if (action == 1 || action == 3) {
                if (DEBUG) {
                    Log.d("OpQLController", "removeCallbacks mShowQLView");
                }
                this.mHandler.removeCallbacks(this.mShowQLView);
            }
            if (this.mQLShowing && (opQLRootView = this.mQLRootView) != null) {
                if (opQLRootView.isAttachedToWindow()) {
                    this.mQLRootView.onTouch(motionEvent);
                }
                if (action == 1 || action == 3) {
                    shouldHideQLView();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$OpQLController() {
        boolean isCurrentGuest = OpUtils.isCurrentGuest(this.mContext);
        if (DEBUG) {
            Log.d("OpQLController", "mShowQLView enable " + isQLEnabled() + " isGuest " + isCurrentGuest);
        }
        if (isQLEnabled() && !isCurrentGuest) {
            int i = this.mContext.getResources().getConfiguration().orientation;
            if (DEBUG) {
                Log.d("OpQLController", "mShowQLView mQLShowing " + this.mQLShowing + " mFingerOnView " + this.mFodFingerTouchValidator.isFingerDownOnView() + " orientation " + i);
            }
            if (!this.mQLShowing && this.mQLRootView == null && 1 == i && this.mFodFingerTouchValidator.isFingerDownOnView()) {
                if (DEBUG) {
                    Log.d("OpQLController", "mQLShowing set to true");
                }
                this.mQLShowing = true;
                vibrate(1023);
                OpFingerprintDialogView opFingerprintDialogView = this.mFodDialogView;
                if (opFingerprintDialogView != null) {
                    opFingerprintDialogView.updateIconVisibility(true);
                }
                OpQLRootView opQLRootView = (OpQLRootView) LayoutInflater.from(this.mContext).inflate(C0011R$layout.ql_root_view, (ViewGroup) null);
                this.mQLRootView = opQLRootView;
                String str = this.mQLConfig;
                if (str != null) {
                    opQLRootView.setQLConfig(str);
                }
                WindowManager windowManager = this.mWindowManager;
                OpQLRootView opQLRootView2 = this.mQLRootView;
                windowManager.addView(opQLRootView2, opQLRootView2.getLayoutParams());
                this.mQLRootView.setOnTouchListener(new View.OnTouchListener() { // from class: com.oneplus.systemui.biometrics.OpQLController.2
                    @Override // android.view.View.OnTouchListener
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        OpQLController.this.shouldHideQLView();
                        return true;
                    }
                });
                this.mDialogImpl.expandTransparentLayout();
                notifyQLViewVisibilityChanged(true);
            }
        }
    }

    private void vibrate(int i) {
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        if (OpUtils.isSupportLinearVibration()) {
            VibratorSceneUtils.doVibrateWithSceneIfNeeded(this.mContext, vibrator, i);
        } else if (OpUtils.isSupportZVibrationMotor()) {
            vibrator.vibrate(VibrationEffect.get(0), VIBRATION_ATTRIBUTES);
        } else {
            vibrator.vibrate(VibrationEffect.get(5), VIBRATION_ATTRIBUTES);
        }
    }

    private void notifyQLViewVisibilityChanged(boolean z) {
        QLStateListener qLStateListener = this.mListener;
        if (qLStateListener != null) {
            qLStateListener.onQLVisibilityChanged(z);
        }
    }

    private final class QLContentObserver extends ContentObserver {
        private final Uri mQLAppsUri = Settings.Secure.getUriFor("op_quick_launch_apps");
        private final Uri mQLEnableUri = Settings.Secure.getUriFor("op_quickpay_enable");

        public QLContentObserver() {
            super(OpQLController.this.mHandler);
            OpQLController.this.mContext.getContentResolver().registerContentObserver(this.mQLEnableUri, true, this, -1);
            OpQLController.this.mContext.getContentResolver().registerContentObserver(this.mQLAppsUri, true, this, -1);
            onChange();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (this.mQLEnableUri.equals(uri)) {
                updateQuickpayEnable();
            } else if (this.mQLAppsUri.equals(uri)) {
                updateQuickLaunchApps();
            }
        }

        public void onChange() {
            updateQuickpayEnable();
            updateQuickLaunchApps();
        }

        private void updateQuickpayEnable() {
            OpQLController opQLController = OpQLController.this;
            boolean z = false;
            if (Settings.Secure.getInt(opQLController.mContext.getContentResolver(), "op_quickpay_enable", 0) == 1) {
                z = true;
            }
            opQLController.mIsEnableQL = z;
            if (OpQLController.DEBUG) {
                Log.d("OpQLController", "op_quickpay_enable " + OpQLController.this.mIsEnableQL);
            }
        }

        private void updateQuickLaunchApps() {
            OpQLController opQLController = OpQLController.this;
            opQLController.mQLConfig = Settings.Secure.getString(opQLController.mContext.getContentResolver(), "op_quick_launch_apps");
            if (SystemProperties.getInt("debug.ql.wx.mini.program", 0) != 0) {
                OpQLController opQLController2 = OpQLController.this;
                opQLController2.mQLConfig = OpQLController.this.mQLConfig + "OpenWxMiniProgram:com.tencent.mm;0,";
            }
            if (OpQLController.DEBUG) {
                Log.d("OpQLController", "op_quick_launch_apps change " + OpQLController.this.mQLConfig);
            }
        }
    }

    private final class QLReceiver extends BroadcastReceiver {
        public QLReceiver() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.PHONE_STATE");
            intentFilter.addAction("com.android.deskclock.ALARM_ALERT");
            OpQLController.this.mContext.registerReceiver(this, intentFilter, null, OpQLController.this.mHandler);
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (OpQLController.this.isQLEnabled()) {
                String action = intent.getAction();
                boolean z = false;
                if ("android.intent.action.PHONE_STATE".equals(action)) {
                    z = TelephonyManager.EXTRA_STATE_RINGING.equals(intent.getStringExtra("state"));
                } else if ("com.android.deskclock.ALARM_ALERT".equals(action)) {
                    z = true;
                }
                if (z) {
                    OpQLController.this.shouldHideQLView();
                }
            }
        }
    }
}
