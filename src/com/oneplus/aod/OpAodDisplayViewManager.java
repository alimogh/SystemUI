package com.oneplus.aod;

import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Point;
import android.hardware.biometrics.BiometricSourceType;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.oneplus.aod.OpAodDisplayViewManager;
import com.oneplus.aod.bg.OpAodCanvas;
import com.oneplus.aod.slice.OpSliceManager;
import com.oneplus.aod.utils.OpCanvasAodHelper;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.util.OpUtils;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.TimeZone;
public class OpAodDisplayViewManager implements OnHeadsUpChangedListener {
    private static final float AOD_SCRIM_ALPHA_VALUE = ((((float) SystemProperties.getInt("debug.aod_scrim_alpha_value", 300)) * 1.0f) / 1000.0f);
    private OpAodCanvas mAodCanvas;
    private OpAodMain mAodMainView;
    private OpBitmojiManager mBitmojiManager;
    private OpClockViewCtrl mClockViewCtrl;
    private ViewGroup mContainer;
    private Context mContext;
    private final DisplayMetrics mDisplayMetrics = ((DisplayMetrics) Dependency.get(DisplayMetrics.class));
    private DozeHost mDozeHost;
    private Handler mHandler;
    private DozeHost.Callback mHostCallback = new DozeHost.Callback() { // from class: com.oneplus.aod.OpAodDisplayViewManager.2
        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onFingerprintPoke() {
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onPowerSaveChanged(boolean z) {
            Log.d("AodDisplayViewManager", "onPowerSaveChanged");
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onThreeKeyChanged(final int i) {
            Log.d("AodDisplayViewManager", "onThreeKeyChanged: " + i);
            OpAodDisplayViewManager.this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.2.1
                @Override // java.lang.Runnable
                public void run() {
                    OpAodDisplayViewManager.this.mThreeKeyView.onThreeKeyChanged(i);
                }
            });
        }

        @Override // com.android.systemui.doze.DozeHost.Callback
        public void onSingleTap() {
            Log.d("AodDisplayViewManager", "onSingleTap");
        }
    };
    private OpFpAodIndicationText mIndication;
    public boolean mIsPlayFingerprintUnlockAnimation;
    private boolean mIsPress = false;
    private boolean mIsScreenTurnedOff;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private int mLayoutDir;
    private OpAodLightEffectContainer mLightEffectContainer;
    private OpAodNotificationIconAreaController mNotificationIconCtrl;
    private PowerManager mPowerManager;
    private Point mRealDisplaySize = new Point();
    private Runnable mResetIndicationRunnable = new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.1
        @Override // java.lang.Runnable
        public void run() {
            if (OpAodDisplayViewManager.this.mIndication != null) {
                OpAodDisplayViewManager.this.mIndication.resetState();
            }
        }
    };
    private boolean mScreenTurnedOn;
    private View mScrimView;
    private SettingObserver mSettingObserver;
    private boolean mShouldPlayLightEffect = true;
    private OpSingleNotificationView mSingleNotificationView;
    private OpSliceManager mSliceManager;
    private int mStatus = 1;
    private StatusBar mStatusbar;
    private OpAodThreeKeyStatusView mThreeKeyView;
    private final KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.aod.OpAodDisplayViewManager.4
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTimeChanged() {
            if (!OpAodDisplayViewManager.this.mViewInit) {
                Log.w("AodDisplayViewManager", "onTimeChanged: view not init yet.");
            } else {
                OpAodDisplayViewManager.this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.4.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (OpAodDisplayViewManager.this.mBitmojiManager != null) {
                            OpAodDisplayViewManager.this.mBitmojiManager.refresh();
                        }
                        OpAodDisplayViewManager.this.mClockViewCtrl.onTimeChanged();
                        OpAodDisplayViewManager.this.mSliceManager.onTimeChanged();
                        OpAodDisplayViewManager.this.mAodMainView.onTimeChanged();
                        if (OpCanvasAodHelper.isCanvasAodEnabled(OpAodDisplayViewManager.this.mContext) && OpAodDisplayViewManager.this.mKeyguardUpdateMonitor.isAlwaysOnEnabled()) {
                            OpAodDisplayViewManager.this.mAodCanvas.onTimeChanged();
                        }
                    }
                });
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onTimeZoneChanged(final TimeZone timeZone) {
            if (!OpAodDisplayViewManager.this.mViewInit) {
                Log.w("AodDisplayViewManager", "onTimeZoneChanged: view not init yet.");
            } else {
                OpAodDisplayViewManager.this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.4.2
                    @Override // java.lang.Runnable
                    public void run() {
                        OpAodDisplayViewManager.this.mClockViewCtrl.onTimeZoneChanged(timeZone);
                    }
                });
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserSwitchComplete(final int i) {
            OpAodDisplayViewManager.this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.4.3
                @Override // java.lang.Runnable
                public void run() {
                    OpAodDisplayViewManager.this.mSliceManager.onUserSwitchComplete(i);
                    OpAodDisplayViewManager.this.mClockViewCtrl.onUserSwitchComplete(i);
                    OpAodDisplayViewManager.this.mAodMainView.onUserSwitchComplete(i);
                    OpAodDisplayViewManager.this.mNotificationIconCtrl.onUserSwitchComplete(i);
                }
            });
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserInfoChanged(final int i) {
            super.onUserInfoChanged(i);
            OpAodDisplayViewManager.this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.4.4
                @Override // java.lang.Runnable
                public void run() {
                    OpAodDisplayViewManager.this.mAodMainView.onUserInfoChanged(i);
                }
            });
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onDreamingStateChanged(boolean z) {
            super.onDreamingStateChanged(z);
            OpAodDisplayViewManager.this.mClockViewCtrl.onDreamingStateChanged(z);
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onScreenTurningOn() {
            super.onScreenTurningOn();
            OpAodDisplayViewManager.this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.4.5
                @Override // java.lang.Runnable
                public void run() {
                    if (!OpAodDisplayViewManager.this.mPowerManager.isInteractive() && OpAodDisplayViewManager.this.mBitmojiManager != null) {
                        OpAodDisplayViewManager.this.mBitmojiManager.refresh();
                    }
                    OpAodDisplayViewManager.this.mClockViewCtrl.onScreenTurningOn();
                }
            });
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOn() {
            super.onScreenTurnedOn();
            OpAodDisplayViewManager.this.mScreenTurnedOn = true;
            if (OpAodDisplayViewManager.this.mKeyguardUpdateMonitor.isAlwaysOnEnabled()) {
                OpAodDisplayViewManager.this.updateView();
            }
            OpAodDisplayViewManager.this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.4.6
                @Override // java.lang.Runnable
                public void run() {
                    OpAodDisplayViewManager.this.mClockViewCtrl.onScreenTurnedOn();
                }
            });
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onScreenTurnedOff() {
            super.onScreenTurnedOff();
            OpAodDisplayViewManager.this.mScreenTurnedOn = false;
            if (!OpAodDisplayViewManager.this.mIsScreenTurnedOff) {
                Log.d("AodDisplayViewManager", "updateView in screen turned off");
                OpAodDisplayViewManager.this.mIsScreenTurnedOff = true;
                OpAodDisplayViewManager.this.updateView();
            }
            if (OpAodUtils.isCustomFingerprint()) {
                OpAodDisplayViewManager.this.mIndication.resetState();
            }
            OpAodDisplayViewManager.this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.4.7
                @Override // java.lang.Runnable
                public void run() {
                    OpAodDisplayViewManager.this.mClockViewCtrl.onScreenTurnedOff();
                    if (OpCanvasAodHelper.isCanvasAodEnabled(OpAodDisplayViewManager.this.mContext) && OpAodDisplayViewManager.this.mKeyguardUpdateMonitor.isAlwaysOnEnabled()) {
                        OpAodDisplayViewManager.this.mAodCanvas.recover();
                    }
                }
            });
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFinishedGoingToSleep(int i) {
            super.onFinishedGoingToSleep(i);
            if (OpAodUtils.isCustomFingerprint()) {
                OpAodDisplayViewManager.this.mIndication.updateFPIndicationText(false, null);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) {
            super.onBiometricHelp(i, str, biometricSourceType);
            if (OpAodUtils.isCustomFingerprint() && biometricSourceType == BiometricSourceType.FINGERPRINT) {
                OpAodDisplayViewManager.this.mIndication.updateFPIndicationText(true, str);
                OpAodDisplayViewManager.this.userActivityInAlwaysOn("fingerprint help");
            }
            if (OpAodDisplayViewManager.this.mKeyguardUpdateMonitor.isAlwaysOnEnabled()) {
                OpAodDisplayViewManager.this.mHandler.removeCallbacks(OpAodDisplayViewManager.this.mResetIndicationRunnable);
                OpAodDisplayViewManager.this.mHandler.postDelayed(OpAodDisplayViewManager.this.mResetIndicationRunnable, (long) ((DozeParameters) Dependency.get(DozeParameters.class)).getPulseVisibleDuration(13));
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) {
            super.onBiometricError(i, str, biometricSourceType);
            if (OpAodUtils.isCustomFingerprint() && biometricSourceType == BiometricSourceType.FINGERPRINT) {
                OpAodDisplayViewManager.this.mIndication.resetState();
            }
            if (OpAodDisplayViewManager.this.mKeyguardUpdateMonitor.isAlwaysOnEnabled()) {
                OpAodDisplayViewManager.this.mHandler.removeCallbacks(OpAodDisplayViewManager.this.mResetIndicationRunnable);
                OpAodDisplayViewManager.this.mHandler.postDelayed(OpAodDisplayViewManager.this.mResetIndicationRunnable, (long) ((DozeParameters) Dependency.get(DozeParameters.class)).getPulseVisibleDuration(13));
                OpAodDisplayViewManager.this.userActivityInAlwaysOn("fingerprint error");
            }
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onFingerprintAcquired(int i) {
            super.onFingerprintAcquired(i);
            if (OpAodUtils.isCustomFingerprint() && i == 6) {
                OpAodDisplayViewManager.this.mIndication.resetState();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedGoingToSleep(int i) {
            if (OpAodDisplayViewManager.this.mScreenTurnedOn) {
                OpAodDisplayViewManager.this.mIsScreenTurnedOff = false;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedWakingUp() {
            OpAodDisplayViewManager.this.mIsScreenTurnedOff = false;
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onAlwaysOnEnableChanged(boolean z) {
            if (!OpAodDisplayViewManager.this.mViewInit) {
                Log.w("AodDisplayViewManager", "onAlwaysOnEnableChanged: view not init yet.");
            } else if (!z) {
                OpAodDisplayViewManager.this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.4.8
                    @Override // java.lang.Runnable
                    public void run() {
                        if (OpCanvasAodHelper.isCanvasAodEnabled(OpAodDisplayViewManager.this.mContext)) {
                            OpAodDisplayViewManager.this.mAodCanvas.recover();
                        }
                        OpAodDisplayViewManager.this.mClockViewCtrl.recover();
                    }
                });
            }
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onEnvironmentLightChanged(boolean z) {
            OpAodDisplayViewManager.this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.-$$Lambda$OpAodDisplayViewManager$4$BggSDkm-mfbo83_VKT1pTdKL1ac
                @Override // java.lang.Runnable
                public final void run() {
                    OpAodDisplayViewManager.AnonymousClass4.this.lambda$onEnvironmentLightChanged$0$OpAodDisplayViewManager$4();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onEnvironmentLightChanged$0 */
        public /* synthetic */ void lambda$onEnvironmentLightChanged$0$OpAodDisplayViewManager$4() {
            OpAodDisplayViewManager.this.handleEnvironmentLightChanged();
        }
    };
    private boolean mViewInit;

    private String getStateString(int i) {
        return i != 0 ? i != 1 ? i != 2 ? i != 3 ? "unknown" : "threekey" : "notification" : "main" : "none";
    }

    static {
        SystemProperties.getInt("sys.c.aod.move_delay", 60000);
    }

    public OpAodDisplayViewManager(Context context, DozeHost dozeHost, StatusBar statusBar, HeadsUpManager headsUpManager) {
        this.mContext = context;
        this.mDozeHost = dozeHost;
        this.mStatusbar = statusBar;
        this.mHandler = statusBar.getAodWindowManager().getUIHandler();
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mClockViewCtrl = new OpClockViewCtrl(context);
        this.mSliceManager = new OpSliceManager(this.mContext, this.mHandler);
        initBitmojiManager();
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this.mUpdateCallback);
        this.mSettingObserver = new SettingObserver(this.mHandler);
        this.mNotificationIconCtrl = new OpAodNotificationIconAreaController(context, this.mClockViewCtrl);
        headsUpManager.addListener(this);
    }

    private void initBitmojiManager() {
        try {
            OpBitmojiManager opBitmojiManager = (OpBitmojiManager) Dependency.get(OpBitmojiManager.class);
            this.mBitmojiManager = opBitmojiManager;
            if (opBitmojiManager != null) {
                opBitmojiManager.setHandler(this.mHandler);
            }
        } catch (IllegalArgumentException e) {
            Log.w("AodDisplayViewManager", "initBitmojiManager occur error", e);
        }
    }

    public void updateView(ViewGroup viewGroup) {
        initViews(viewGroup);
        this.mClockViewCtrl.removeOnChangeListener(this.mSliceManager);
        this.mSliceManager.initViews(viewGroup.findViewById(C0008R$id.slice_info_container));
        this.mClockViewCtrl.addOnChangeListener(this.mSliceManager);
        this.mNotificationIconCtrl.initViews(this.mAodMainView);
        this.mClockViewCtrl.initViews(viewGroup);
        if (!this.mViewInit) {
            this.mDozeHost.addCallback(this.mHostCallback);
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("aod_clock_style"), false, this.mSettingObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("aod_display_text"), false, this.mSettingObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("op_custom_horizon_light_animation_style"), false, this.mSettingObserver, -1);
            this.mViewInit = true;
        }
    }

    private void initViews(ViewGroup viewGroup) {
        OpAodCanvas opAodCanvas = this.mAodCanvas;
        if (opAodCanvas != null) {
            opAodCanvas.release();
        }
        this.mContainer = (ViewGroup) viewGroup.findViewById(C0008R$id.op_aod_container);
        OpAodCanvas opAodCanvas2 = (OpAodCanvas) viewGroup.findViewById(C0008R$id.op_aod_bg);
        this.mAodCanvas = opAodCanvas2;
        opAodCanvas2.setHandler(this.mHandler);
        this.mAodCanvas.setAodMask(viewGroup.findViewById(C0008R$id.op_aod_lowlight_mask));
        this.mClockViewCtrl.removeOnChangeListener(this.mAodMainView);
        OpAodMain opAodMain = (OpAodMain) viewGroup.findViewById(C0008R$id.op_aod_view);
        this.mAodMainView = opAodMain;
        this.mClockViewCtrl.addOnChangeListener(opAodMain);
        this.mSingleNotificationView = (OpSingleNotificationView) viewGroup.findViewById(C0008R$id.single_notification_view);
        this.mLayoutDir = this.mContext.getResources().getConfiguration().getLayoutDirection();
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("AodDisplayViewManager", "initViews mLayoutDir:" + this.mLayoutDir);
        }
        this.mThreeKeyView = (OpAodThreeKeyStatusView) viewGroup.findViewById(C0008R$id.three_key_view);
        this.mScrimView = viewGroup.findViewById(C0008R$id.aod_scrim);
        OpAodLightEffectContainer opAodLightEffectContainer = (OpAodLightEffectContainer) viewGroup.findViewById(C0008R$id.notification_animation_view);
        this.mLightEffectContainer = opAodLightEffectContainer;
        opAodLightEffectContainer.setLightIndex(Settings.System.getIntForUser(this.mContext.getContentResolver(), "op_custom_horizon_light_animation_style", 0, 0));
        OpFpAodIndicationText opFpAodIndicationText = (OpFpAodIndicationText) viewGroup.findViewById(C0008R$id.op_aod_fp_indication_text);
        this.mIndication = opFpAodIndicationText;
        opFpAodIndicationText.init(this, this.mHandler);
        updateIndication();
    }

    public void onDensityOrFontScaleChanged(ViewGroup viewGroup) {
        updateView(viewGroup);
        updateIndicationTextSize(0, this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.oneplus_contorl_text_size_body1));
    }

    private void updateIndication() {
        FrameLayout.LayoutParams layoutParams;
        if (OpUtils.isCustomFingerprint() && (layoutParams = (FrameLayout.LayoutParams) this.mIndication.getLayoutParams()) != null) {
            Display defaultDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            defaultDisplay.getMetrics(this.mDisplayMetrics);
            defaultDisplay.getRealSize(this.mRealDisplaySize);
            int i = this.mRealDisplaySize.y;
            boolean is2KResolution = OpUtils.is2KResolution();
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(is2KResolution ? C0005R$dimen.op_biometric_icon_normal_location_y_2k : C0005R$dimen.op_biometric_icon_normal_location_y_1080p);
            if (OpUtils.isCutoutHide(this.mContext)) {
                dimensionPixelSize -= OpUtils.getCutoutPathdataHeight(this.mContext);
            }
            layoutParams.bottomMargin = (i - dimensionPixelSize) + this.mContext.getResources().getDimensionPixelSize(is2KResolution ? C0005R$dimen.op_keyguard_indication_padding_bottom_2k : C0005R$dimen.op_keyguard_indication_padding_bottom_1080p);
            this.mIndication.setLayoutParams(layoutParams);
        }
    }

    public void onConfigChanged(Configuration configuration) {
        int layoutDirection = configuration.getLayoutDirection();
        if (this.mLayoutDir != layoutDirection) {
            if (this.mViewInit) {
                this.mAodMainView.updateRTL(layoutDirection);
                this.mSingleNotificationView.updateRTL(layoutDirection);
            }
            this.mLayoutDir = layoutDirection;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x006c  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:36:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateForPulseReason(int r9) {
        /*
            r8 = this;
            com.android.systemui.statusbar.phone.StatusBar r0 = r8.mStatusbar
            boolean r0 = r0.isDozingCustom()
            java.lang.String r1 = "AodDisplayViewManager"
            if (r0 != 0) goto L_0x0010
            java.lang.String r8 = "setState: don't set view if not dozing"
            android.util.Log.d(r1, r8)
            return
        L_0x0010:
            android.os.PowerManager r0 = r8.mPowerManager
            boolean r0 = r0.isInteractive()
            if (r0 == 0) goto L_0x001e
            java.lang.String r8 = "setState: don't set view if waking up"
            android.util.Log.d(r1, r8)
            return
        L_0x001e:
            r0 = 0
            r2 = -1
            r3 = 10
            r4 = 12
            r5 = 3
            r6 = 1
            if (r9 == r2) goto L_0x003f
            r2 = 11
            if (r9 != r2) goto L_0x002d
            goto L_0x003f
        L_0x002d:
            if (r9 == r5) goto L_0x0041
            if (r9 != r4) goto L_0x0032
            goto L_0x0041
        L_0x0032:
            if (r9 != r6) goto L_0x0036
            r0 = 2
            goto L_0x0042
        L_0x0036:
            if (r9 != r3) goto L_0x003a
            r0 = r5
            goto L_0x0042
        L_0x003a:
            r2 = 13
            if (r9 != r2) goto L_0x0042
            return
        L_0x003f:
            r8.mShouldPlayLightEffect = r6
        L_0x0041:
            r0 = r6
        L_0x0042:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r7 = "setState="
            r2.append(r7)
            java.lang.String r7 = r8.getStateString(r0)
            r2.append(r7)
            java.lang.String r7 = ", from="
            r2.append(r7)
            int r7 = r8.mStatus
            java.lang.String r7 = r8.getStateString(r7)
            r2.append(r7)
            java.lang.String r2 = r2.toString()
            android.util.Log.d(r1, r2)
            int r1 = r8.mStatus
            if (r1 == r0) goto L_0x0078
            r8.mStatus = r0
            if (r0 != r6) goto L_0x0075
            com.oneplus.aod.slice.OpSliceManager r0 = r8.mSliceManager
            r0.onInitiativePulse()
        L_0x0075:
            r8.updateView()
        L_0x0078:
            com.android.keyguard.KeyguardUpdateMonitor r0 = r8.mKeyguardUpdateMonitor
            boolean r0 = r0.isAlwaysOnEnabled()
            if (r0 == 0) goto L_0x00a3
            if (r9 == r5) goto L_0x0086
            if (r9 == r4) goto L_0x0086
            if (r9 != r3) goto L_0x00a3
        L_0x0086:
            r8.onUserTrigger(r9)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "pulse reason: "
            r0.append(r1)
            r0.append(r9)
            java.lang.String r9 = r0.toString()
            android.content.Context r0 = r8.mContext
            boolean r0 = com.oneplus.aod.utils.OpCanvasAodHelper.isCanvasAodEnabled(r0)
            r8.userActivityInAlwaysOn(r9, r0)
        L_0x00a3:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.OpAodDisplayViewManager.updateForPulseReason(int):void");
    }

    public void resetStatus() {
        Log.d("AodDisplayViewManager", "resetStatus");
        if (!this.mKeyguardUpdateMonitor.isAlwaysOnEnabled()) {
            this.mStatus = 0;
        }
        this.mIsPress = false;
        updateView();
        this.mShouldPlayLightEffect = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateView() {
        if (this.mViewInit) {
            this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.3
                @Override // java.lang.Runnable
                public void run() {
                    OpAodDisplayViewManager.this.handleUpdateView();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateView() {
        Log.d("AodDisplayViewManager", "updateView: state = " + getStateString(this.mStatus) + " mIsPlayFingerprintUnlockAnimation:" + this.mIsPlayFingerprintUnlockAnimation + " mIsPress:" + this.mIsPress + " mIsScreenTurnedOff:" + this.mIsScreenTurnedOff + " mScreenTurnedOn: " + this.mScreenTurnedOn);
        if (!this.mIsScreenTurnedOff || (this.mKeyguardUpdateMonitor.isAlwaysOnEnabled() && !this.mScreenTurnedOn)) {
            Log.d("AodDisplayViewManager", "screen is not turned off yet.");
            this.mScrimView.setAlpha(1.0f);
            this.mAodCanvas.setVisibility(4);
            this.mAodMainView.setVisibility(4);
            this.mSingleNotificationView.setVisibility(4);
            this.mThreeKeyView.setVisibility(4);
        } else if (this.mStatus == 0) {
            this.mScrimView.setAlpha(1.0f);
            this.mAodCanvas.setVisibility(0);
            this.mAodMainView.setVisibility(0);
            this.mSingleNotificationView.setVisibility(4);
            this.mThreeKeyView.setVisibility(4);
            this.mLightEffectContainer.resetNotificationAnimView();
        } else {
            this.mContainer.setVisibility(0);
            setScrimAlphaDependOnEnvironmentLight();
            int i = this.mStatus;
            if (i == 1) {
                if (!OpAodUtils.isAlwaysOnEnabled() || (OpAodUtils.isAlwaysOnEnabled() && OpAodUtils.isAlwaysOnEnabledWithTimer())) {
                    this.mAodCanvas.setVisibility(0);
                    this.mAodMainView.setVisibility(0);
                } else {
                    this.mAodCanvas.setVisibility(4);
                    this.mAodMainView.setVisibility(4);
                }
                this.mSingleNotificationView.setVisibility(4);
                this.mThreeKeyView.setVisibility(4);
            } else if (i == 2) {
                this.mAodCanvas.setVisibility(4);
                this.mAodMainView.setVisibility(4);
                this.mSingleNotificationView.setVisibility(0);
                this.mThreeKeyView.setVisibility(4);
            } else if (i == 3) {
                this.mAodCanvas.setVisibility(4);
                this.mAodMainView.setVisibility(4);
                this.mSingleNotificationView.setVisibility(4);
                this.mThreeKeyView.setVisibility(0);
            }
            if (!OpAodUtils.isNotificationLightEnabled()) {
                return;
            }
            if (this.mStatus != 2 || !this.mShouldPlayLightEffect) {
                this.mLightEffectContainer.resetNotificationAnimView();
                return;
            }
            this.mLightEffectContainer.showLight();
            this.mShouldPlayLightEffect = false;
        }
    }

    public void onFingerPressed(boolean z) {
        this.mIsPress = z;
        updateView();
    }

    public void onPlayFingerprintUnlockAnimation(boolean z) {
        Log.d("AodDisplayViewManager", "onPlayFingerprintUnlockAnimation");
        this.mIsPlayFingerprintUnlockAnimation = z;
        updateView();
    }

    public OpAodNotificationIconAreaController getAodNotificationIconCtrl() {
        return this.mNotificationIconCtrl;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEnvironmentLightChanged() {
        this.mAodMainView.setAlphaDependOnEnvironmentLight();
        if ((this.mAodMainView.getVisibility() != 4 || this.mSingleNotificationView.getVisibility() != 4 || this.mThreeKeyView.getVisibility() != 4) && this.mStatus != 0) {
            setScrimAlphaDependOnEnvironmentLight();
        }
    }

    private void setScrimAlphaDependOnEnvironmentLight() {
        this.mScrimView.setAlpha(this.mKeyguardUpdateMonitor.isLowLightEnv() ? AOD_SCRIM_ALPHA_VALUE : 0.0f);
    }

    private boolean isAodMode() {
        return this.mKeyguardUpdateMonitor.isScreenOn() && !this.mPowerManager.isInteractive();
    }

    public void handleUserUnlocked() {
        this.mSliceManager.onTimeChanged();
    }

    public void startDozing() {
        this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.5
            @Override // java.lang.Runnable
            public void run() {
                if (OpAodDisplayViewManager.this.mBitmojiManager != null) {
                    OpAodDisplayViewManager.this.mBitmojiManager.refresh();
                }
                OpAodDisplayViewManager.this.mClockViewCtrl.startDozing();
                OpAodDisplayViewManager.this.mSliceManager.setListening(true);
            }
        });
    }

    public void stopDozing() {
        this.mSliceManager.setListening(false);
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
        if (this.mStatusbar.isDozingCustom()) {
            this.mSingleNotificationView.onNotificationHeadsUp(notificationEntry);
        }
    }

    /* access modifiers changed from: private */
    public final class SettingObserver extends ContentObserver {
        public SettingObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri, int i) {
            super.onChange(z, uri, i);
            if (uri.equals(Settings.Secure.getUriFor("aod_clock_style"))) {
                OpAodUtils.checkAodStyle(OpAodDisplayViewManager.this.mContext, i);
                OpAodDisplayViewManager.this.mClockViewCtrl.updateClockDB(true);
                int intForUser = Settings.Secure.getIntForUser(OpAodDisplayViewManager.this.mContext.getContentResolver(), "aod_clock_style", 0, -2);
                if (i == 0 && OpAodDisplayViewManager.this.mBitmojiManager != null) {
                    OpAodDisplayViewManager.this.mBitmojiManager.onOwnerClockChanged();
                }
                if (OpCanvasAodHelper.isCanvasAodEnabled(OpAodDisplayViewManager.this.mContext) && intForUser != 0 && KeyguardUpdateMonitor.getCurrentUser() == 0) {
                    OpAodDisplayViewManager.this.mAodCanvas.disable();
                }
            } else if (uri.equals(Settings.Secure.getUriFor("aod_display_text"))) {
                OpAodDisplayViewManager.this.mAodMainView.updateDisplayTextDB();
            } else if (uri.equals(Settings.System.getUriFor("op_custom_horizon_light_animation_style"))) {
                OpAodDisplayViewManager.this.mLightEffectContainer.setLightIndex(Settings.System.getIntForUser(OpAodDisplayViewManager.this.mContext.getContentResolver(), "op_custom_horizon_light_animation_style", 0, -2));
            }
        }

        public void onChange(boolean z, Collection<Uri> collection, int i, int i2) {
            onChange(z, collection, i2);
        }
    }

    public boolean playAodWakingUpAnimation() {
        boolean isAlwaysOnEnabled = this.mKeyguardUpdateMonitor.isAlwaysOnEnabled();
        boolean isCanvasAodEnabled = OpCanvasAodHelper.isCanvasAodEnabled(this.mContext);
        Log.i("AodDisplayViewManager", "playAodWakingUpAnimation: isAlwaysOnEnabled: " + isAlwaysOnEnabled + ", isCanvasAodEnabled: " + isCanvasAodEnabled);
        return this.mStatus != 0 && this.mKeyguardUpdateMonitor.isScreenOn() && ((isAlwaysOnEnabled && isCanvasAodEnabled) || !isAlwaysOnEnabled);
    }

    public void dump(PrintWriter printWriter) {
        printWriter.print("aod scrim alpha= " + this.mScrimView.getAlpha());
        this.mAodMainView.dump(printWriter);
        this.mSliceManager.dump(printWriter);
    }

    public void updateIndicationTextSize(final int i, final int i2) {
        this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.6
            @Override // java.lang.Runnable
            public void run() {
                if (OpAodUtils.isCustomFingerprint()) {
                    OpAodDisplayViewManager.this.mIndication.setTextSize(i, (float) i2);
                }
            }
        });
    }

    public void onFodShowOrHideOnAod(final boolean z) {
        this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.7
            @Override // java.lang.Runnable
            public void run() {
                OpAodDisplayViewManager.this.mClockViewCtrl.onFodShowOrHideOnAod(z);
            }
        });
        this.mIndication.showOrHide(z);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void userActivityInAlwaysOn(String str) {
        userActivityInAlwaysOn(str, false);
    }

    private void userActivityInAlwaysOn(final String str, final boolean z) {
        if (isAodMode() && this.mKeyguardUpdateMonitor.isAlwaysOnEnabled()) {
            this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.8
                @Override // java.lang.Runnable
                public void run() {
                    OpAodDisplayViewManager.this.mClockViewCtrl.userActivityInAlwaysOn(str);
                    if (z) {
                        OpAodDisplayViewManager.this.mAodCanvas.userActivityInAlwaysOn(str);
                    }
                }
            });
        }
    }

    private void onUserTrigger(final int i) {
        Log.i("AodDisplayViewManager", "onUserTrigger: reason= " + i);
        if (i == 3 || i == 12) {
            this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.9
                @Override // java.lang.Runnable
                public void run() {
                    OpAodDisplayViewManager.this.mClockViewCtrl.onUserTrigger(i);
                }
            });
        }
    }

    public void recoverFromBurnInScreen() {
        this.mAodMainView.updateLayout();
    }

    public void quickHideNotificationBeforeScreenOff(final DozeHost.PulseCallback pulseCallback, final Handler handler) {
        Log.d("AodDisplayViewManager", "quickHideNotificationBeforeScreenOff");
        this.mHandler.postAtFrontOfQueue(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.10
            @Override // java.lang.Runnable
            public void run() {
                if (OpAodDisplayViewManager.this.mSingleNotificationView != null) {
                    Log.d("AodDisplayViewManager", "hide notification before screen off");
                    if (!OpAodDisplayViewManager.this.mKeyguardUpdateMonitor.isAlwaysOnEnabled()) {
                        OpAodDisplayViewManager.this.mStatus = 0;
                    }
                    OpAodDisplayViewManager.this.mIsPress = false;
                    OpAodDisplayViewManager.this.handleUpdateView();
                    OpAodDisplayViewManager.this.mShouldPlayLightEffect = true;
                }
                if (pulseCallback != null) {
                    Log.d("AodDisplayViewManager", "call onPulseFinished");
                    Handler handler2 = handler;
                    if (handler2 != null) {
                        handler2.postAtFrontOfQueue(new Runnable() { // from class: com.oneplus.aod.OpAodDisplayViewManager.10.1
                            @Override // java.lang.Runnable
                            public void run() {
                                pulseCallback.onPulseFinished();
                            }
                        });
                    }
                }
            }
        });
    }

    public int getAodDisplayViewState() {
        return this.mStatus;
    }

    public boolean hasHintText() {
        return this.mIndication.getVisibility() == 0;
    }

    public void onFodIndicationVisibilityChanged(boolean z) {
        Log.i("AodDisplayViewManager", "onFodIndicationVisibilityChanged " + z);
        if (!this.mPowerManager.isInteractive()) {
            this.mKeyguardUpdateMonitor.updateFodIconVisibility();
            this.mClockViewCtrl.onFodIndicationVisibilityChanged(z);
        }
    }
}
