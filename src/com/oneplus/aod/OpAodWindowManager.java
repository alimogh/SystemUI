package com.oneplus.aod;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.PathInterpolator;
import android.widget.RelativeLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.StatusBarWindowController;
import com.oneplus.aod.bg.OpAodCanvas;
import com.oneplus.aod.utils.OpCanvasAodHelper;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
public class OpAodWindowManager {
    private OpAodCanvas mAodBg;
    private View mAodContainer;
    private RelativeLayout mAodWindowView;
    private Context mContext;
    AnimatorSet mDisppearAnimation;
    private boolean mDozing;
    private Handler mHandler;
    private boolean mIsLowLightEnvironment = false;
    private boolean mIsWakeAndUnlock;
    private boolean mIsWindowRemoved;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private OpLsState mLSState;
    private BroadcastReceiver mMdmReadyReceiver = new BroadcastReceiver() { // from class: com.oneplus.aod.OpAodWindowManager.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals("com.oneplus.intent.action.mdm_provider_ready")) {
                Log.i("AodWindowManager", "Receive MDM provider ready");
                OpAodWindowManager.this.reportMDMEvent();
            }
        }
    };
    private final Runnable mRemoveWindow = new Runnable() { // from class: com.oneplus.aod.OpAodWindowManager.4
        @Override // java.lang.Runnable
        public void run() {
            OpAodWindowManager.this.removeAodWindow();
        }
    };
    private SettingObserver mSettingsOberver = new SettingObserver();
    private HandlerThread mUIHandlerThread;
    private BiometricUnlockController mUnlockController;
    private final KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.aod.OpAodWindowManager.3
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitching(int i) {
            OpAodWindowManager.this.mAodBg.onUserSwitching(i);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(int i) {
            OpAodUtils.updateDozeSettings(OpAodWindowManager.this.mContext, i);
            OpAodWindowManager.this.mAodBg.onUserSwitchComplete(i);
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedWakingUp() {
            super.onStartedWakingUp();
            if (OpAodWindowManager.this.mUnlockController != null) {
                OpAodWindowManager opAodWindowManager = OpAodWindowManager.this;
                opAodWindowManager.mIsWakeAndUnlock = opAodWindowManager.mUnlockController.isWakeAndUnlock();
                if (!OpAodWindowManager.this.mIsWakeAndUnlock) {
                    OpAodWindowManager opAodWindowManager2 = OpAodWindowManager.this;
                    opAodWindowManager2.mWakingUpReason = opAodWindowManager2.mKeyguardUpdateMonitor.getWakingUpReason();
                    Log.d("AodWindowManager", "onStartedWakingUp");
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserUnlocked() {
            super.onUserUnlocked();
            OpAodWindowManager.this.mAodBg.onUserUnlocked();
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onEnvironmentLightChanged(boolean z) {
            super.onEnvironmentLightChanged(z);
            OpAodWindowManager.this.mIsLowLightEnvironment = z;
        }
    };
    private String mWakingUpReason = null;
    private WindowManager mWm;

    public void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) { // from class: com.oneplus.aod.OpAodWindowManager.1
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                if (i == 2) {
                    Log.d("AodWindowManager", "start handleStartDozing; mDozing = " + OpAodWindowManager.this.mDozing);
                    OpAodWindowManager.this.handleStartDozing();
                } else if (i == 3) {
                    Log.d("AodWindowManager", "start handleStopDozing; mDozing = " + OpAodWindowManager.this.mDozing);
                    OpAodWindowManager.this.handleStopDozing();
                } else if (i == 4) {
                    OpAodWindowManager.this.handleFingerprintAuthenticated();
                }
            }
        };
    }

    public OpAodWindowManager(Context context) {
        this.mContext = context;
        this.mWm = (WindowManager) context.getSystemService("window");
        IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
        this.mLSState = OpLsState.getInstance();
        this.mSettingsOberver.observe();
        StatusBarWindowController statusBarWindowController = (StatusBarWindowController) Dependency.get(StatusBarWindowController.class);
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this.mUpdateCallback);
        OpAodUtils.init(context, KeyguardUpdateMonitor.getCurrentUser());
        HandlerThread handlerThread = new HandlerThread("AODUIThread", -8);
        this.mUIHandlerThread = handlerThread;
        handlerThread.start();
        initHandler(this.mUIHandlerThread.getLooper());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.oneplus.intent.action.mdm_provider_ready");
        this.mContext.registerReceiver(this.mMdmReadyReceiver, intentFilter);
    }

    public void updateView(RelativeLayout relativeLayout) {
        this.mAodWindowView = relativeLayout;
        this.mAodContainer = relativeLayout.findViewById(C0008R$id.op_aod_container);
        this.mAodBg = (OpAodCanvas) relativeLayout.findViewById(C0008R$id.op_aod_bg);
    }

    public Handler getUIHandler() {
        return this.mHandler;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportMDMEvent() {
        String str;
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        ContentResolver contentResolver = this.mContext.getContentResolver();
        boolean z = false;
        boolean z2 = OpAodUtils.isMotionAwakeOn() || OpAodUtils.isSingleTapEnabled();
        boolean z3 = 1 == Settings.System.getIntForUser(contentResolver, "aod_smart_display_cur_state", 1, currentUser);
        String str2 = "1";
        if (z2) {
            boolean z4 = 1 == Settings.System.getIntForUser(contentResolver, "aod_smart_display_music_info_enabled", 1, currentUser);
            if (1 == Settings.System.getIntForUser(contentResolver, "aod_smart_display_calendar_enabled", 1, currentUser)) {
                z = true;
            }
            OpMdmLogger.log("Smart_AOD", "switch", z3 ? "2" : "0", "X9HFK50WT7");
            if (z4) {
                str = str2;
            } else {
                str = "0";
            }
            OpMdmLogger.log("Smart_AOD", "Media", str, "X9HFK50WT7");
            if (!z) {
                str2 = "0";
            }
            OpMdmLogger.log("Smart_AOD", "Calendar", str2, "X9HFK50WT7");
            return;
        }
        OpMdmLogger.log("Smart_AOD", "switch", str2, "X9HFK50WT7");
    }

    public void onWakingAndUnlocking() {
        this.mWakingUpReason = this.mKeyguardUpdateMonitor.getWakingUpReason();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStartDozing() {
        if (!this.mDozing) {
            Log.d("AodWindowManager", "handleStartDozing: " + this.mDozing);
            if (this.mUnlockController == null) {
                this.mUnlockController = this.mLSState.getBiometricUnlockController();
            }
            this.mDozing = true;
            this.mIsWakeAndUnlock = false;
            this.mWakingUpReason = null;
            this.mAodWindowView.setAlpha(1.0f);
            this.mAodContainer.setTranslationY(0.0f);
            this.mAodContainer.setAlpha(1.0f);
            this.mAodBg.reset();
            if (this.mAodWindowView.isAttachedToWindow()) {
                this.mHandler.removeCallbacks(this.mRemoveWindow);
                Log.d("AodWindowManager", "mAodView has already been added to window, do not add it again.");
            } else {
                this.mWm.addView(this.mAodWindowView, getAodViewLayoutParams());
            }
            this.mIsWindowRemoved = false;
            this.mAodWindowView.setSystemUiVisibility(this.mAodWindowView.getSystemUiVisibility() | 1792);
            this.mAodWindowView.setVisibility(0);
            this.mAodWindowView.getWindowInsetsController().hide(WindowInsets.Type.navigationBars());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStopDozing() {
        if (this.mDozing) {
            int i = 0;
            this.mDozing = false;
            this.mAodBg.stopDozing();
            if (shouldRemoveAodImmediately()) {
                Log.d("AodWindowManager", "handleStopDozing: remove window immediately");
                removeAodWindow();
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("handleStopDozing: mIsWakeAndUnlock = ");
            sb.append(this.mIsWakeAndUnlock);
            sb.append(", hasLockWallpaper = ");
            sb.append(!OpLsState.getInstance().getStatusBarKeyguardViewManager().isShowingLiveWallpaper(true));
            Log.d("AodWindowManager", sb.toString());
            float f = 0.0f;
            if (Build.DEBUG_ONEPLUS) {
                Log.d("AodWindowManager", "handleStopDozing mWakingUpReason " + this.mWakingUpReason);
            }
            if (OpUtils.isCustomFingerprint()) {
                String str = this.mWakingUpReason;
                if (str != null) {
                    if (!str.contains("FINGERPRINT") && this.mWakingUpReason.equals("com.android.systemui:FailedAttempts")) {
                        i = 90;
                    }
                    if (this.mWakingUpReason.equals("com.android.systemui:FailedAttempts")) {
                        f = 1.0f;
                    }
                }
            } else {
                String str2 = this.mWakingUpReason;
                if (str2 != null && str2.contains("FINGERPRINT_WALLPAPER")) {
                    i = 100;
                }
            }
            this.mAodWindowView.setAlpha(f);
            Log.d("AodWindowManager", "aod remove window delay:" + i);
            if (i > 0) {
                this.mHandler.postDelayed(this.mRemoveWindow, (long) i);
            } else {
                removeAodWindow();
            }
        }
    }

    public void onFingerprintAuthenticated() {
        this.mHandler.sendEmptyMessage(4);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFingerprintAuthenticated() {
        RelativeLayout relativeLayout = this.mAodWindowView;
        if (relativeLayout != null && relativeLayout.getWindowInsetsController() != null) {
            this.mAodWindowView.getWindowInsetsController().show(WindowInsets.Type.navigationBars());
        }
    }

    public void startDozing() {
        Log.d("AodWindowManager", "startDozing");
        this.mHandler.sendEmptyMessage(2);
    }

    public void stopDozing() {
        Log.d("AodWindowManager", "stopDozing");
        this.mHandler.sendEmptyMessage(3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeAodWindow() {
        KeyguardUpdateMonitor keyguardUpdateMonitor;
        if (!this.mIsWindowRemoved) {
            this.mIsLowLightEnvironment = false;
            RelativeLayout relativeLayout = this.mAodWindowView;
            if (!(relativeLayout == null || relativeLayout.getWindowInsetsController() == null || (keyguardUpdateMonitor = this.mKeyguardUpdateMonitor) == null || !keyguardUpdateMonitor.isFingerprintAlreadyAuthenticated())) {
                this.mAodWindowView.getWindowInsetsController().show(WindowInsets.Type.navigationBars());
            }
            Log.d("AodWindowManager", "aod remove window");
            RelativeLayout relativeLayout2 = this.mAodWindowView;
            if (relativeLayout2 != null && relativeLayout2.isAttachedToWindow()) {
                this.mWm.removeViewImmediate(this.mAodWindowView);
            }
            this.mIsWindowRemoved = true;
        }
    }

    private WindowManager.LayoutParams getAodViewLayoutParams() {
        int i;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = 2303;
        if (OpUtils.isCtsInputmethodservice() || OpUtils.isCTSAdded()) {
            Log.d("AodWindowManager", "no focus flag");
            i = 16778504;
        } else {
            i = 16778496;
        }
        layoutParams.privateFlags = 16;
        layoutParams.layoutInDisplayCutoutMode = 3;
        if (Build.VERSION.SDK_INT >= 27) {
            layoutParams.privateFlags = 16 | 2097152;
        }
        layoutParams.flags = i;
        layoutParams.format = -2;
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = 17;
        layoutParams.screenOrientation = 1;
        layoutParams.setTitle("OpAodQ");
        layoutParams.softInputMode = 3;
        return layoutParams;
    }

    /* access modifiers changed from: private */
    public final class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(new Handler());
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver contentResolver = OpAodWindowManager.this.mContext.getContentResolver();
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("notification_wake_enabled"), false, OpAodWindowManager.this.mSettingsOberver, -1);
            contentResolver.registerContentObserver(Settings.Secure.getUriFor("aod_display_mode"), false, OpAodWindowManager.this.mSettingsOberver, -1);
            contentResolver.registerContentObserver(Settings.System.getUriFor("prox_wake_enabled"), false, OpAodWindowManager.this.mSettingsOberver, -1);
            contentResolver.registerContentObserver(Settings.System.getUriFor("oem_acc_blackscreen_gestrue_enable"), false, OpAodWindowManager.this.mSettingsOberver, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            if (Settings.Secure.getUriFor("notification_wake_enabled").equals(uri)) {
                OpAodUtils.updateNotificationWakeState(OpAodWindowManager.this.mContext, currentUser);
            } else if (Settings.Secure.getUriFor("aod_display_mode").equals(uri)) {
                OpAodUtils.updateAlwaysOnState(OpAodWindowManager.this.mContext, currentUser);
            } else if (Settings.System.getUriFor("prox_wake_enabled").equals(uri)) {
                OpAodUtils.updateMotionAwakeState(OpAodWindowManager.this.mContext, currentUser);
            } else if (Settings.System.getUriFor("oem_acc_blackscreen_gestrue_enable").equals(uri)) {
                OpAodUtils.updateSingleTapAwakeState(OpAodWindowManager.this.mContext, currentUser);
            }
        }
    }

    public AnimatorSet genAodDisappearAnimation(boolean z) {
        ArrayList<Animator> genAodDisappearAnimation;
        final boolean isCanvasAodAnimation = OpCanvasAodHelper.isCanvasAodAnimation(this.mContext, z);
        this.mDisppearAnimation = new AnimatorSet();
        ArrayList arrayList = new ArrayList();
        PathInterpolator pathInterpolator = new PathInterpolator(0.4f, 0.0f, 0.3f, 1.0f);
        new ValueAnimator();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, -100.0f);
        ofFloat.setInterpolator(pathInterpolator);
        ofFloat.setDuration(375L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.aod.OpAodWindowManager.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpAodWindowManager.this.mAodContainer.setTranslationY(floatValue);
                if (!isCanvasAodAnimation) {
                    OpAodWindowManager.this.mAodBg.setTranslationY(floatValue);
                }
            }
        });
        arrayList.add(ofFloat);
        new ValueAnimator();
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(1.0f, 0.0f);
        ofFloat2.setDuration(z ? (long) SystemProperties.getInt("debug.aod.disappear.animation", 225) : 225);
        ofFloat2.setInterpolator(pathInterpolator);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.aod.OpAodWindowManager.6
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpAodWindowManager.this.mAodContainer.setAlpha(floatValue);
                if (!isCanvasAodAnimation) {
                    OpAodWindowManager.this.mAodBg.setAlpha(floatValue);
                }
            }
        });
        arrayList.add(ofFloat2);
        if (isCanvasAodAnimation && (genAodDisappearAnimation = this.mAodBg.genAodDisappearAnimation()) != null && genAodDisappearAnimation.size() > 0) {
            arrayList.addAll(genAodDisappearAnimation);
        }
        this.mDisppearAnimation.playTogether(arrayList);
        this.mDisppearAnimation.addListener(makeHardwareLayerListener(this.mAodContainer));
        return this.mDisppearAnimation;
    }

    private final Animator.AnimatorListener makeHardwareLayerListener(final View view) {
        return new AnimatorListenerAdapter(this) { // from class: com.oneplus.aod.OpAodWindowManager.7
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (view.isAttachedToWindow()) {
                    view.setLayerType(2, null);
                    view.buildLayer();
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (view.isAttachedToWindow()) {
                    view.setLayerType(0, null);
                }
            }
        };
    }

    public AnimatorSet getLastAodDisappearAnimation() {
        return this.mDisppearAnimation;
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public boolean isWakingAndUnlockByFP() {
        String str = this.mWakingUpReason;
        return str != null && str.contains("FINGERPRINT");
    }

    private boolean shouldRemoveAodImmediately() {
        if (!this.mKeyguardUpdateMonitor.isAlwaysOnEnabled() || OpCanvasAodHelper.isCanvasAodEnabled(this.mContext)) {
            return this.mKeyguardUpdateMonitor.isAlwaysOnEnabled() && OpCanvasAodHelper.isCanvasAodEnabled(this.mContext) && this.mIsLowLightEnvironment;
        }
        return true;
    }
}
