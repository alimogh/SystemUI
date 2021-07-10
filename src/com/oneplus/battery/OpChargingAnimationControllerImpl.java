package com.oneplus.battery;

import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.settingslib.Utils;
import com.android.systemui.C0002R$attr;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0014R$raw;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.battery.OpChargingAnimationController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
import java.util.function.Consumer;
public class OpChargingAnimationControllerImpl implements OpChargingAnimationController, BatteryController.BatteryStateChangeCallback, ConfigurationController.ConfigurationListener {
    private static boolean mPreventModeNoBackground = false;
    private String TAG = "OpChargingAnimationControllerImpl";
    private boolean isKeyguardShowing = false;
    private boolean mAnimationStarted;
    private AudioManager mAudioManager;
    private BatteryController mBatteryController;
    private OpBatteryStatus mBatteryStatus;
    private boolean mBouncerShow = false;
    private int mCacheFastChargeType = -1;
    private boolean mCacheWirelessWarpChargeType;
    private final ArrayList<OpChargingAnimationController.ChargingStateChangeCallback> mCallbacks = new ArrayList<>();
    private SoundPool mChargingSound;
    private int mChargingSoundId;
    private ConfigurationController mConfigurationController;
    private Context mContext;
    protected boolean mDockOnWireless = true;
    private ContentObserver mDockOnWirelessObserver = null;
    private Handler mHandler = new Handler(Looper.getMainLooper()) { // from class: com.oneplus.battery.OpChargingAnimationControllerImpl.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1000) {
                OpChargingAnimationControllerImpl.this.mHandler.removeMessages(1000);
                if (OpChargingAnimationControllerImpl.this.mOpWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpWarpChargingView.releaseAsset();
                }
                Log.i(OpChargingAnimationControllerImpl.this.TAG, "releaseAsset");
            }
        }
    };
    private boolean mKeyguardOn = false;
    private KeyguardStateCallback mKeyguardStateCallback;
    private int mOldChargingType = -1;
    private boolean mOldPluggedInAndCharging = false;
    private OpCBWarpChargingView mOpCBWarpChargingView;
    private OpNewWarpChargingView mOpNewWarpChargingView;
    private OpSWarpChargingView mOpSWarpChargingView;
    private OpWarpChargingView mOpWarpChargingView;
    private boolean mPluggedButNotUsb = false;
    private boolean mPreventViewShow = false;
    private final Runnable mRefreshBatteryInfo = new Runnable() { // from class: com.oneplus.battery.-$$Lambda$OpChargingAnimationControllerImpl$Rd40fvDNGOg-j7g-6AL7TtU1fGI
        @Override // java.lang.Runnable
        public final void run() {
            OpChargingAnimationControllerImpl.this.lambda$new$2$OpChargingAnimationControllerImpl();
        }
    };
    private ScreenLifecycle mScreenLifecycle;
    private final ScreenLifecycle.Observer mScreenObserver = new ScreenLifecycle.Observer() { // from class: com.oneplus.battery.OpChargingAnimationControllerImpl.3
        @Override // com.android.systemui.keyguard.ScreenLifecycle.Observer
        public void onScreenTurnedOn() {
            Log.i(OpChargingAnimationControllerImpl.this.TAG, "onScreenTurnedOn onRefreshBatteryInfo");
            OpChargingAnimationControllerImpl.this.mHandler.removeCallbacks(OpChargingAnimationControllerImpl.this.mRefreshBatteryInfo);
            OpChargingAnimationControllerImpl.this.mHandler.postDelayed(OpChargingAnimationControllerImpl.this.mRefreshBatteryInfo, 1000);
        }

        @Override // com.android.systemui.keyguard.ScreenLifecycle.Observer
        public void onScreenTurnedOff() {
            Log.i(OpChargingAnimationControllerImpl.this.TAG, "onScreenTurnedOff");
            if (((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isAlwaysOnEnabled()) {
                Log.d(OpChargingAnimationControllerImpl.this.TAG, "onScreenTurnedOff: return because of always-on enabled");
            } else if (OpUtils.isSupportREDCharging()) {
                if (OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView.stopAnimation();
                }
            } else if (OpUtils.SUPPORT_CHARGING_ANIM_V2) {
                if (OpChargingAnimationControllerImpl.this.mOpSWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpSWarpChargingView.stopAnimation();
                }
            } else if (OpUtils.SUPPORT_CHARGING_ANIM_V1) {
                if (OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView.stopAnimation();
                }
            } else if (OpChargingAnimationControllerImpl.this.mOpWarpChargingView != null) {
                OpChargingAnimationControllerImpl.this.mOpWarpChargingView.stopAnimation();
            }
        }
    };
    private boolean mShouldPlayChargeSound;
    private int mSmallestWidth;
    private KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.battery.OpChargingAnimationControllerImpl.2
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            String str = OpChargingAnimationControllerImpl.this.TAG;
            Log.i(str, "onKeyguardVisibilityChanged:" + z);
            OpChargingAnimationControllerImpl.this.isKeyguardShowing = z;
            if (OpChargingAnimationControllerImpl.this.isKeyguardShowing) {
                return;
            }
            if (OpUtils.isSupportREDCharging()) {
                if (OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView != null) {
                    String str2 = OpChargingAnimationControllerImpl.this.TAG;
                    Log.i(str2, "stop animation when keyguard not showing.  keyguardShowing:" + OpChargingAnimationControllerImpl.this.isKeyguardShowing);
                    OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView.stopAnimation();
                }
            } else if (OpUtils.SUPPORT_CHARGING_ANIM_V2) {
                if (OpChargingAnimationControllerImpl.this.mOpSWarpChargingView != null) {
                    String str3 = OpChargingAnimationControllerImpl.this.TAG;
                    Log.i(str3, "stop animation when keyguard not showing.  keyguardShowing:" + OpChargingAnimationControllerImpl.this.isKeyguardShowing);
                    OpChargingAnimationControllerImpl.this.mOpSWarpChargingView.stopAnimation();
                }
            } else if (OpUtils.SUPPORT_CHARGING_ANIM_V1) {
                if (OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView != null) {
                    String str4 = OpChargingAnimationControllerImpl.this.TAG;
                    Log.i(str4, "stop animation when keyguard not showing.  keyguardShowing:" + OpChargingAnimationControllerImpl.this.isKeyguardShowing);
                    OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView.stopAnimation();
                }
            } else if (OpChargingAnimationControllerImpl.this.mOpWarpChargingView != null) {
                OpChargingAnimationControllerImpl.this.mOpWarpChargingView.stopAnimation();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedGoingToSleep(int i) {
            Log.i(OpChargingAnimationControllerImpl.this.TAG, "onStartedGoingToSleep");
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFinishedGoingToSleep(int i) {
            Log.i(OpChargingAnimationControllerImpl.this.TAG, "onFinishedGoingToSleep");
            if (((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isAlwaysOnEnabled()) {
                if (OpChargingAnimationControllerImpl.this.mOpWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpWarpChargingView.stopAnimation();
                }
                if (OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView.stopAnimation();
                }
                if (OpChargingAnimationControllerImpl.this.mOpSWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpSWarpChargingView.stopAnimation();
                }
                if (OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView.stopAnimation();
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardBouncerChanged(boolean z) {
            OpChargingAnimationControllerImpl.this.mBouncerShow = z;
            String str = OpChargingAnimationControllerImpl.this.TAG;
            Log.i(str, "onKeyguardBouncerChanged:" + OpChargingAnimationControllerImpl.this.mBouncerShow);
            if (!z) {
                return;
            }
            if (OpUtils.isSupportREDCharging()) {
                if (OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView.stopAnimation();
                }
            } else if (OpUtils.SUPPORT_CHARGING_ANIM_V2) {
                if (OpChargingAnimationControllerImpl.this.mOpSWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpSWarpChargingView.stopAnimation();
                }
            } else if (OpUtils.SUPPORT_CHARGING_ANIM_V1) {
                if (OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView.stopAnimation();
                }
            } else if (OpChargingAnimationControllerImpl.this.mOpWarpChargingView != null) {
                OpChargingAnimationControllerImpl.this.mOpWarpChargingView.stopAnimation();
            }
        }

        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onPreventModeChanged(boolean z) {
            OpChargingAnimationControllerImpl.this.mPreventViewShow = z;
            if (!z) {
                return;
            }
            if (OpUtils.isSupportREDCharging()) {
                if (OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView.stopAnimation();
                }
            } else if (OpUtils.SUPPORT_CHARGING_ANIM_V2) {
                if (OpChargingAnimationControllerImpl.this.mOpSWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpSWarpChargingView.stopAnimation();
                }
            } else if (OpUtils.SUPPORT_CHARGING_ANIM_V1) {
                if (OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView.stopAnimation();
                }
            } else if (OpChargingAnimationControllerImpl.this.mOpWarpChargingView != null) {
                OpChargingAnimationControllerImpl.this.mOpWarpChargingView.stopAnimation();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onRefreshBatteryInfo(OpBatteryStatus opBatteryStatus) {
            OpChargingAnimationControllerImpl.this.mBatteryStatus = opBatteryStatus;
            OpChargingAnimationControllerImpl opChargingAnimationControllerImpl = OpChargingAnimationControllerImpl.this;
            int i = opBatteryStatus.plugged;
            int i2 = 0;
            opChargingAnimationControllerImpl.mPluggedButNotUsb = i == 1 || i == 4;
            Log.i(OpChargingAnimationControllerImpl.this.TAG, "onRefreshBatteryInfo / mPluggedButNotUsb:" + OpChargingAnimationControllerImpl.this.mPluggedButNotUsb + " / status:" + opBatteryStatus + " / chargingType: " + opBatteryStatus.plugged + " / oldChargingType:" + OpChargingAnimationControllerImpl.this.mOldChargingType);
            int i3 = opBatteryStatus.status;
            boolean z = i3 == 2 || i3 == 5;
            boolean z2 = opBatteryStatus.isPluggedIn() && z;
            boolean z3 = OpChargingAnimationControllerImpl.this.mOldChargingType != opBatteryStatus.plugged;
            boolean unused = OpChargingAnimationControllerImpl.this.mShouldPlayChargeSound;
            OpChargingAnimationControllerImpl.this.mShouldPlayChargeSound = (opBatteryStatus.isPluggedInWired() || (opBatteryStatus.wirelessCharging && OpChargingAnimationControllerImpl.this.mDockOnWireless)) && (OpChargingAnimationControllerImpl.this.mShouldPlayChargeSound || z);
            boolean z4 = OpChargingAnimationControllerImpl.this.mScreenLifecycle.getScreenState() == 2;
            Log.i(OpChargingAnimationControllerImpl.this.TAG, "onRefreshBatteryInfo / mOldPluggedInAndCharging:" + OpChargingAnimationControllerImpl.this.mOldPluggedInAndCharging + " / isPluggedInAndCharging:" + z2 + " / isKeyguardShowing " + OpChargingAnimationControllerImpl.this.isKeyguardShowing + " / isScreenTurnedOn " + z4 + " / isChargingTypeChange " + z3);
            if ((OpChargingAnimationControllerImpl.this.mOldPluggedInAndCharging != z2 || z3) && z2) {
                if (!OpChargingAnimationControllerImpl.this.isKeyguardShowing) {
                    Log.i(OpChargingAnimationControllerImpl.this.TAG, "Only play charging sound when keyguard not showing.");
                    OpChargingAnimationControllerImpl.this.playChargingSound();
                    OpChargingAnimationControllerImpl.this.mOldPluggedInAndCharging = z2;
                    OpChargingAnimationControllerImpl.this.mOldChargingType = opBatteryStatus.plugged;
                } else if (OpChargingAnimationControllerImpl.this.mOpWarpChargingView != null && OpChargingAnimationControllerImpl.this.mKeyguardOn && OpChargingAnimationControllerImpl.this.mPluggedButNotUsb) {
                    OpChargingAnimationControllerImpl.this.prepareAnimationResource();
                } else if ((OpChargingAnimationControllerImpl.this.mOldPluggedInAndCharging != z2 || z3) && z2 && z4) {
                    OpChargingAnimationControllerImpl.this.mOldPluggedInAndCharging = z2;
                    OpChargingAnimationControllerImpl.this.mOldChargingType = opBatteryStatus.plugged;
                    if (z3) {
                        if (OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView != null) {
                            OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView.stopAnimation();
                        } else if (OpChargingAnimationControllerImpl.this.mOpSWarpChargingView != null) {
                            OpChargingAnimationControllerImpl.this.mOpSWarpChargingView.stopAnimation();
                        } else if (OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView != null) {
                            OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView.stopAnimation();
                        }
                    }
                    if (OpChargingAnimationControllerImpl.this.mBouncerShow || OpChargingAnimationControllerImpl.this.mPreventViewShow) {
                        OpChargingAnimationControllerImpl.this.playChargingSound();
                        return;
                    }
                    Log.i(OpChargingAnimationControllerImpl.this.TAG, "startAnimation");
                    if (OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView != null) {
                        OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView.startAnimation(1);
                    } else if (OpChargingAnimationControllerImpl.this.mOpSWarpChargingView != null) {
                        OpSWarpChargingView opSWarpChargingView = OpChargingAnimationControllerImpl.this.mOpSWarpChargingView;
                        int i4 = opBatteryStatus.plugged;
                        if (!(i4 == 1 || i4 == 2)) {
                            i2 = 1;
                        }
                        opSWarpChargingView.startAnimation(i2);
                    } else if (OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView != null) {
                        OpNewWarpChargingView opNewWarpChargingView = OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView;
                        int i5 = opBatteryStatus.plugged;
                        if (!(i5 == 1 || i5 == 2)) {
                            i2 = 1;
                        }
                        opNewWarpChargingView.startAnimation(i2);
                    }
                }
            } else if (OpChargingAnimationControllerImpl.this.mOldPluggedInAndCharging != z2 && !z2 && !opBatteryStatus.isPluggedIn()) {
                Log.i(OpChargingAnimationControllerImpl.this.TAG, "stopAnimation by unplug the charging");
                if (OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView.stopAnimation();
                } else if (OpChargingAnimationControllerImpl.this.mOpSWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpSWarpChargingView.stopAnimation();
                } else if (OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView.stopAnimation();
                } else if (OpChargingAnimationControllerImpl.this.mOpWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpWarpChargingView.stopAnimation();
                }
                OpChargingAnimationControllerImpl.this.resetChargingState();
            }
        }
    };
    private boolean mWarpFastCharging = false;
    private boolean mWirelessWarpCharging = false;
    private FrameLayout mWrapChargingLayout;

    public OpChargingAnimationControllerImpl(Context context) {
        this.mContext = context;
        this.mScreenLifecycle = (ScreenLifecycle) Dependency.get(ScreenLifecycle.class);
        Log.i(this.TAG, "OpChargingAnimationControllerImpl init");
        this.mBatteryController = (BatteryController) Dependency.get(BatteryController.class);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        SoundPool soundPool = new SoundPool(1, 1, 0);
        this.mChargingSound = soundPool;
        this.mChargingSoundId = soundPool.load(this.mContext, C0014R$raw.charging, 1);
        if (OpUtils.SUPPORT_WARP_CHARGING) {
            registerReceiver();
        }
        ConfigurationController configurationController = (ConfigurationController) Dependency.get(ConfigurationController.class);
        this.mConfigurationController = configurationController;
        configurationController.addCallback(this);
        this.mKeyguardStateCallback = new KeyguardStateCallback();
        registerDockOnWirelessObserver(true);
    }

    public void registerReceiver() {
        this.mBatteryController.addCallback(this);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateCallback);
        this.mScreenLifecycle.addObserver(this.mScreenObserver);
    }

    @Override // com.oneplus.battery.OpChargingAnimationController
    public void init(KeyguardViewMediator keyguardViewMediator) {
        Log.i(this.TAG, "init");
        if (Build.DEBUG_ONEPLUS) {
            String str = this.TAG;
            Log.i(str, "Support V1 charging animation:" + OpUtils.SUPPORT_CHARGING_ANIM_V1 + " Support V2 charging animation:" + OpUtils.SUPPORT_CHARGING_ANIM_V2);
        }
        if (OpUtils.isSupportREDCharging()) {
            genOpCBWarpChargingView();
        } else if (OpUtils.SUPPORT_CHARGING_ANIM_V2) {
            genOpSWarpChargingView();
        } else if (OpUtils.SUPPORT_CHARGING_ANIM_V1) {
            genOpNewWarpChargingView();
        } else {
            genOpWarpChargingView();
        }
        initOPWarpCharging();
        keyguardViewMediator.addStateMonitorCallback(this.mKeyguardStateCallback);
    }

    public void addCallback(OpChargingAnimationController.ChargingStateChangeCallback chargingStateChangeCallback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.add(chargingStateChangeCallback);
        }
    }

    public void removeCallback(OpChargingAnimationController.ChargingStateChangeCallback chargingStateChangeCallback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(chargingStateChangeCallback);
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        OpWarpChargingView opWarpChargingView = this.mOpWarpChargingView;
        if (opWarpChargingView != null) {
            opWarpChargingView.onBatteryLevelChanged(i, z, z2);
        }
        OpNewWarpChargingView opNewWarpChargingView = this.mOpNewWarpChargingView;
        if (opNewWarpChargingView != null) {
            opNewWarpChargingView.onBatteryLevelChanged(i, z, z2);
        }
        OpSWarpChargingView opSWarpChargingView = this.mOpSWarpChargingView;
        if (opSWarpChargingView != null) {
            opSWarpChargingView.onBatteryLevelChanged(i, z, z2);
        }
        OpCBWarpChargingView opCBWarpChargingView = this.mOpCBWarpChargingView;
        if (opCBWarpChargingView != null) {
            opCBWarpChargingView.onBatteryLevelChanged(i, z, z2);
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onSWarpBatteryLevelChanged(float f, float f2, long j) {
        OpSWarpChargingView opSWarpChargingView = this.mOpSWarpChargingView;
        if (opSWarpChargingView != null) {
            opSWarpChargingView.onSWarpBatteryLevelChanged(f, f2, j);
        }
        OpCBWarpChargingView opCBWarpChargingView = this.mOpCBWarpChargingView;
        if (opCBWarpChargingView != null) {
            opCBWarpChargingView.onSWarpBatteryLevelChanged(f, f2, j);
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onFastChargeChanged(int i) {
        OpNewWarpChargingView opNewWarpChargingView;
        OpSWarpChargingView opSWarpChargingView;
        OpCBWarpChargingView opCBWarpChargingView;
        boolean isWarpCharging = this.mBatteryController.isWarpCharging(i);
        if (isWarpCharging && isWarpCharging != this.mWarpFastCharging) {
            boolean z = this.mScreenLifecycle.getScreenState() == 2;
            if (this.isKeyguardShowing && z && !this.mBouncerShow && !this.mPreventViewShow) {
                if (!isAnimationStarted()) {
                    String str = this.TAG;
                    Log.i(str, "CacheFastChargeType:" + i);
                    this.mCacheFastChargeType = i;
                    return;
                }
                updateScrim();
                if (!OpUtils.isSupportREDCharging() || (opCBWarpChargingView = this.mOpCBWarpChargingView) == null) {
                    if (!OpUtils.SUPPORT_CHARGING_ANIM_V2 || (opSWarpChargingView = this.mOpSWarpChargingView) == null) {
                        if (!OpUtils.SUPPORT_CHARGING_ANIM_V1 || (opNewWarpChargingView = this.mOpNewWarpChargingView) == null) {
                            this.mOpWarpChargingView.startAnimation();
                        } else {
                            opNewWarpChargingView.notifyWarpCharging();
                        }
                    } else if (i == 4) {
                        opSWarpChargingView.notifyWarpCharging(3);
                    } else {
                        opSWarpChargingView.notifyWarpCharging(1);
                    }
                } else if (i == 4) {
                    opCBWarpChargingView.notifyWarpCharging(3);
                } else {
                    opCBWarpChargingView.notifyWarpCharging(1);
                }
                this.mWarpFastCharging = isWarpCharging;
            }
        } else if (!isWarpCharging && isWarpCharging != this.mWarpFastCharging) {
            this.mWarpFastCharging = isWarpCharging;
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onWirelessWarpChargeChanged(boolean z) {
        if (z && z != this.mWirelessWarpCharging) {
            boolean z2 = this.mScreenLifecycle.getScreenState() == 2;
            if (this.isKeyguardShowing && z2 && !this.mBouncerShow && !this.mPreventViewShow) {
                if (!isAnimationStarted()) {
                    String str = this.TAG;
                    Log.i(str, "mCacheWirelessWarpCharging:" + z);
                    this.mCacheWirelessWarpChargeType = z;
                    return;
                }
                updateScrim();
                OpCBWarpChargingView opCBWarpChargingView = this.mOpCBWarpChargingView;
                if (opCBWarpChargingView != null) {
                    opCBWarpChargingView.notifyWarpCharging(2);
                } else {
                    OpSWarpChargingView opSWarpChargingView = this.mOpSWarpChargingView;
                    if (opSWarpChargingView != null) {
                        opSWarpChargingView.notifyWarpCharging(2);
                    } else {
                        this.mOpNewWarpChargingView.notifyWarpCharging();
                    }
                }
                this.mWirelessWarpCharging = z;
            }
        } else if (!z && z != this.mWirelessWarpCharging) {
            this.mWirelessWarpCharging = z;
        }
    }

    private void updateScrim() {
        OpWarpChargingView opWarpChargingView;
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        if (this.mContext == null || phoneStatusBar == null) {
            Log.d(this.TAG, "can't updateScrim");
            return;
        }
        String str = this.TAG;
        Log.d(str, "updateScrim, 0");
        if (!OpUtils.SUPPORT_CHARGING_ANIM_V1 && (opWarpChargingView = this.mOpWarpChargingView) != null) {
            opWarpChargingView.updaetScrimColor(0);
            this.mOpWarpChargingView.updateColors(Utils.getColorAttrDefaultColor(this.mContext, C0002R$attr.wallpaperTextColor));
        }
    }

    @Override // com.oneplus.battery.OpChargingAnimationController
    public void animationStart(int i) {
        this.mAnimationStarted = true;
        playChargingSound();
        if (this.mCacheFastChargeType != -1) {
            Log.d(this.TAG, "Notify fast charge changed by cache");
            onFastChargeChanged(this.mCacheFastChargeType);
            this.mCacheFastChargeType = -1;
        }
        if (this.mCacheWirelessWarpChargeType) {
            Log.i(this.TAG, "Notify wireless charge charging by cache");
            onWirelessWarpChargeChanged(this.mCacheWirelessWarpChargeType);
            this.mCacheWirelessWarpChargeType = false;
        }
        Log.d(this.TAG, "animationStart");
        OpUtils.safeForeach(this.mCallbacks, new Consumer(i) { // from class: com.oneplus.battery.-$$Lambda$OpChargingAnimationControllerImpl$mohiFvFBhh3_g_WyopM6xYNqSWY
            public final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((OpChargingAnimationController.ChargingStateChangeCallback) obj).onWarpCharingAnimationStart(this.f$0);
            }
        });
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        mPreventModeNoBackground = true;
        phoneStatusBar.setPanelViewAlpha(0.0f, true, -1);
        phoneStatusBar.userActivity();
    }

    @Override // com.oneplus.battery.OpChargingAnimationController
    public void animationEnd(int i) {
        this.mAnimationStarted = false;
        Log.d(this.TAG, "animationEnd");
        OpUtils.safeForeach(this.mCallbacks, new Consumer(i) { // from class: com.oneplus.battery.-$$Lambda$OpChargingAnimationControllerImpl$5vtklR-Tu8EsuL7GG0Pgx7MKs0Q
            public final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((OpChargingAnimationController.ChargingStateChangeCallback) obj).onWarpCharingAnimationEnd(this.f$0);
            }
        });
        if (mPreventModeNoBackground && !this.mPreventViewShow) {
            OpLsState.getInstance().getPhoneStatusBar().setPanelViewAlpha(1.0f, true, -1);
            mPreventModeNoBackground = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void prepareAnimationResource() {
        OpWarpChargingView opWarpChargingView = this.mOpWarpChargingView;
        if (opWarpChargingView != null) {
            opWarpChargingView.prepareAsset();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseAnimationResource() {
        Message message = new Message();
        message.what = 1000;
        this.mHandler.removeMessages(1000);
        this.mHandler.sendMessageDelayed(message, 1000);
    }

    private OpWarpChargingView genOpWarpChargingView() {
        Log.i(this.TAG, "genOpWarpChargingView");
        if (this.mOpWarpChargingView != null) {
            if (Build.DEBUG_ONEPLUS) {
                String str = this.TAG;
                Log.i(str, " mOpWarpChargingView != null / mOpWarpChargingView.getParent():" + this.mOpWarpChargingView.getParent());
            }
            ((ViewGroup) this.mOpWarpChargingView.getParent()).removeView(this.mOpWarpChargingView);
        }
        OpWarpChargingView opWarpChargingView = (OpWarpChargingView) LayoutInflater.from(this.mContext).inflate(C0011R$layout.op_warp_charging_animation_view, (ViewGroup) null);
        this.mOpWarpChargingView = opWarpChargingView;
        return opWarpChargingView;
    }

    private OpNewWarpChargingView genOpNewWarpChargingView() {
        Log.i(this.TAG, "genOpNewWarpChargingView");
        if (this.mOpNewWarpChargingView != null) {
            if (Build.DEBUG_ONEPLUS) {
                String str = this.TAG;
                Log.i(str, " mOpNewWarpChargingView != null / mOpNewWarpChargingView.getParent():" + this.mOpNewWarpChargingView.getParent());
            }
            ((ViewGroup) this.mOpNewWarpChargingView.getParent()).removeView(this.mOpNewWarpChargingView);
        }
        OpNewWarpChargingView opNewWarpChargingView = (OpNewWarpChargingView) LayoutInflater.from(this.mContext).inflate(C0011R$layout.op_warp_charging_animation_view_new, (ViewGroup) null);
        this.mOpNewWarpChargingView = opNewWarpChargingView;
        return opNewWarpChargingView;
    }

    private OpSWarpChargingView genOpSWarpChargingView() {
        Log.i(this.TAG, "genOpSWarpChargingView");
        if (this.mOpSWarpChargingView != null) {
            if (Build.DEBUG_ONEPLUS) {
                String str = this.TAG;
                Log.i(str, " mOpSWarpChargingView != null / mOpSWarpChargingView.getParent():" + this.mOpSWarpChargingView.getParent());
            }
            ((ViewGroup) this.mOpSWarpChargingView.getParent()).removeView(this.mOpSWarpChargingView);
        }
        OpSWarpChargingView opSWarpChargingView = (OpSWarpChargingView) LayoutInflater.from(this.mContext).inflate(C0011R$layout.op_swarp_charging_animation_view, (ViewGroup) null);
        this.mOpSWarpChargingView = opSWarpChargingView;
        return opSWarpChargingView;
    }

    private OpCBWarpChargingView genOpCBWarpChargingView() {
        Log.i(this.TAG, "genOpCBWarpChargingView");
        if (this.mOpCBWarpChargingView != null) {
            if (Build.DEBUG_ONEPLUS) {
                String str = this.TAG;
                Log.i(str, " mOpCBWarpChargingView != null / mOpCBWarpChargingView.getParent():" + this.mOpCBWarpChargingView.getParent());
            }
            ((ViewGroup) this.mOpCBWarpChargingView.getParent()).removeView(this.mOpCBWarpChargingView);
        }
        OpCBWarpChargingView opCBWarpChargingView = (OpCBWarpChargingView) LayoutInflater.from(this.mContext).inflate(C0011R$layout.op_cb_warp_charging_animation_view, (ViewGroup) null);
        this.mOpCBWarpChargingView = opCBWarpChargingView;
        return opCBWarpChargingView;
    }

    private void initOPWarpCharging() {
        this.mWrapChargingLayout = (FrameLayout) OpLsState.getInstance().getContainer().findViewById(C0008R$id.wrap_charging_layout);
        if (OpUtils.isSupportREDCharging()) {
            this.mWrapChargingLayout.addView(this.mOpCBWarpChargingView);
            this.mOpCBWarpChargingView.setChargingAnimationController(this);
        } else if (OpUtils.SUPPORT_CHARGING_ANIM_V2) {
            this.mWrapChargingLayout.addView(this.mOpSWarpChargingView);
            this.mOpSWarpChargingView.setChargingAnimationController(this);
        } else if (OpUtils.SUPPORT_CHARGING_ANIM_V1) {
            this.mWrapChargingLayout.addView(this.mOpNewWarpChargingView);
            this.mOpNewWarpChargingView.setChargingAnimationController(this);
        } else {
            this.mWrapChargingLayout.addView(this.mOpWarpChargingView);
            this.mOpWarpChargingView.setChargingAnimationController(this);
        }
        OpLsState.getInstance().getPhoneStatusBar();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetChargingState() {
        this.mCacheFastChargeType = -1;
        this.mCacheWirelessWarpChargeType = false;
        this.mOldPluggedInAndCharging = false;
        this.mOldChargingType = -1;
    }

    private class KeyguardStateCallback extends IKeyguardStateCallback.Stub {
        public void onFingerprintStateChange(boolean z, int i, int i2, int i3) {
        }

        public void onHasLockscreenWallpaperChanged(boolean z) {
        }

        public void onInputRestrictedStateChanged(boolean z) {
        }

        public void onPocketModeActiveChanged(boolean z) {
        }

        public void onSimSecureStateChanged(boolean z) {
        }

        public void onTrustedChanged(boolean z) {
        }

        private KeyguardStateCallback() {
        }

        public void onShowingStateChanged(boolean z) {
            String str = OpChargingAnimationControllerImpl.this.TAG;
            Log.d(str, "onShowingStateChanged " + z);
            OpChargingAnimationControllerImpl.this.mKeyguardOn = z;
            if (OpChargingAnimationControllerImpl.this.mKeyguardOn && OpChargingAnimationControllerImpl.this.mPluggedButNotUsb) {
                OpChargingAnimationControllerImpl.this.prepareAnimationResource();
            } else if (!OpChargingAnimationControllerImpl.this.mKeyguardOn) {
                if (OpUtils.isSupportREDCharging()) {
                    if (OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView != null) {
                        OpChargingAnimationControllerImpl.this.mOpCBWarpChargingView.stopAnimation();
                    }
                } else if (OpUtils.SUPPORT_CHARGING_ANIM_V2) {
                    if (OpChargingAnimationControllerImpl.this.mOpSWarpChargingView != null) {
                        OpChargingAnimationControllerImpl.this.mOpSWarpChargingView.stopAnimation();
                    }
                } else if (OpUtils.SUPPORT_CHARGING_ANIM_V1) {
                    if (OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView != null) {
                        OpChargingAnimationControllerImpl.this.mOpNewWarpChargingView.stopAnimation();
                    }
                } else if (OpChargingAnimationControllerImpl.this.mOpWarpChargingView != null) {
                    OpChargingAnimationControllerImpl.this.mOpWarpChargingView.stopAnimation();
                }
                OpChargingAnimationControllerImpl.this.releaseAnimationResource();
            }
        }

        public void onDisabledStateChanged(boolean z) {
            String str = OpChargingAnimationControllerImpl.this.TAG;
            Log.d(str, "onDisabledStateChanged" + z);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$OpChargingAnimationControllerImpl() {
        this.mUpdateCallback.onRefreshBatteryInfo(this.mBatteryStatus);
    }

    @Override // com.oneplus.battery.OpChargingAnimationController
    public boolean isAnimationStarted() {
        return this.mAnimationStarted;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        String str = this.TAG;
        Log.d(str, "onConfigChanged / newConfig:" + configuration);
        if (this.mSmallestWidth != configuration.smallestScreenWidthDp) {
            if (OpUtils.isSupportREDCharging()) {
                genOpCBWarpChargingView();
            } else if (OpUtils.SUPPORT_CHARGING_ANIM_V2) {
                genOpSWarpChargingView();
            } else if (OpUtils.SUPPORT_CHARGING_ANIM_V1) {
                genOpNewWarpChargingView();
            } else {
                genOpWarpChargingView();
            }
            initOPWarpCharging();
            this.mSmallestWidth = configuration.smallestScreenWidthDp;
        }
    }

    @Override // com.oneplus.battery.OpChargingAnimationController
    public void disPatchTouchEvent(MotionEvent motionEvent) {
        OpSWarpChargingView opSWarpChargingView = this.mOpSWarpChargingView;
        if (opSWarpChargingView != null) {
            opSWarpChargingView.stopAnimation();
        }
        OpCBWarpChargingView opCBWarpChargingView = this.mOpCBWarpChargingView;
        if (opCBWarpChargingView != null) {
            opCBWarpChargingView.stopAnimation();
        }
        OpNewWarpChargingView opNewWarpChargingView = this.mOpNewWarpChargingView;
        if (opNewWarpChargingView != null) {
            opNewWarpChargingView.stopAnimation();
        }
    }

    public void playChargingSound() {
        SoundPool soundPool;
        boolean isStreamMute = this.mAudioManager.isStreamMute(2);
        String str = this.TAG;
        Log.d(str, "play charging sound. mute: " + isStreamMute);
        if (!isStreamMute && (soundPool = this.mChargingSound) != null) {
            int play = soundPool.play(this.mChargingSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
            String str2 = this.TAG;
            Log.d(str2, "play charging sound result " + play);
        }
    }

    /* access modifiers changed from: protected */
    public void registerDockOnWirelessObserver(boolean z) {
        String str = this.TAG;
        Log.d(str, "registerDockOnWirelessObserver regsiter = " + z);
        if (z) {
            if (this.mDockOnWirelessObserver == null) {
                this.mDockOnWirelessObserver = new ContentObserver(this.mHandler) { // from class: com.oneplus.battery.OpChargingAnimationControllerImpl.4
                    @Override // android.database.ContentObserver
                    public void onChange(boolean z2) {
                        super.onChange(z2);
                        OpChargingAnimationControllerImpl opChargingAnimationControllerImpl = OpChargingAnimationControllerImpl.this;
                        opChargingAnimationControllerImpl.mDockOnWireless = opChargingAnimationControllerImpl.isDockOnWireless();
                        String str2 = OpChargingAnimationControllerImpl.this.TAG;
                        Log.d(str2, "mDockOnWireless state = " + OpChargingAnimationControllerImpl.this.mDockOnWireless);
                    }
                };
            }
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("doced_on_wireless_charger"), false, this.mDockOnWirelessObserver);
        } else if (this.mDockOnWirelessObserver != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mDockOnWirelessObserver);
            this.mDockOnWirelessObserver = null;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isDockOnWireless() {
        return Settings.System.getInt(this.mContext.getContentResolver(), "doced_on_wireless_charger", 1) != 0;
    }
}
