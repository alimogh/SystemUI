package com.android.systemui.settings;

import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.Log;
import android.util.MathUtils;
import android.view.View;
import android.widget.ImageView;
import com.android.internal.BrightnessSynchronizer;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.display.BrightnessUtils;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.BrightnessController;
import com.android.systemui.settings.ToggleSlider;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
import java.util.Iterator;
public class BrightnessController implements ToggleSlider.Listener {
    private static final Uri BRIGHTNESS_FLOAT_URI = Settings.System.getUriFor("screen_brightness_float");
    private static final Uri BRIGHTNESS_FOR_VR_FLOAT_URI = Settings.System.getUriFor("screen_brightness_for_vr_float");
    private static final Uri BRIGHTNESS_MODE_URI = Settings.System.getUriFor("screen_brightness_mode");
    private static final Uri BRIGHTNESS_URI = Settings.System.getUriFor("screen_brightness");
    private static final boolean DEBUG = Build.DEBUG_ONEPLUS;
    private static long mLastClickTimeMillis = -1;
    private int AUTO_BRIGHTNESS_MINIMUM = 0;
    private long mAnimationDuration = 0;
    private volatile boolean mAutomatic;
    private final boolean mAutomaticAvailable;
    private final Handler mBackgroundHandler;
    private float mBrightness = 0.0f;
    private final BrightnessObserver mBrightnessObserver;
    private ArrayList<BrightnessStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    private final Context mContext;
    private final ToggleSlider mControl;
    private final float mDefaultBacklight;
    private final float mDefaultBacklightForVr;
    private final DisplayManager mDisplayManager;
    private boolean mExternalChange;
    private final Handler mHandler = new Handler() { // from class: com.android.systemui.settings.BrightnessController.7
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            boolean z = true;
            BrightnessController.this.mExternalChange = true;
            boolean z2 = false;
            try {
                int i = message.what;
                if (i == 0) {
                    BrightnessController brightnessController = BrightnessController.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    brightnessController.updateIcon(z);
                } else if (i == 1) {
                    Boolean bool = Boolean.FALSE;
                    if (message.obj != null && (bool instanceof Boolean)) {
                        bool = (Boolean) message.obj;
                    }
                    BrightnessController brightnessController2 = BrightnessController.this;
                    float intBitsToFloat = Float.intBitsToFloat(message.arg1);
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    brightnessController2.updateSlider(intBitsToFloat, z, bool.booleanValue());
                    BrightnessController.this.updateIcon(BrightnessController.this.mAutomatic);
                } else if (i == 2) {
                    ToggleSlider toggleSlider = BrightnessController.this.mControl;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    toggleSlider.setChecked(z);
                } else if (i == 3) {
                    BrightnessController.this.mControl.setOnChangedListener(BrightnessController.this);
                } else if (i == 4) {
                    BrightnessController.this.mControl.setOnChangedListener(null);
                } else if (i != 5) {
                    super.handleMessage(message);
                } else {
                    BrightnessController brightnessController3 = BrightnessController.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    brightnessController3.updateVrMode(z);
                }
            } finally {
                BrightnessController.this.mExternalChange = z2;
            }
        }
    };
    private final ImageView mIcon;
    private volatile boolean mIsVrModeEnabled;
    private final ImageView mLevelIcon;
    private boolean mListening;
    private final float mMaximumBacklight;
    private final float mMaximumBacklightForVr;
    private final float mMinimumBacklight;
    private final float mMinimumBacklightForVr;
    private ImageView mMirrorIcon = null;
    private ImageView mMirrorLevelIcon = null;
    private boolean mNewController = false;
    private ValueAnimator mSliderAnimator;
    private int mSliderMax = 0;
    private int mSliderValue = 0;
    private final Runnable mStartListeningRunnable = new Runnable() { // from class: com.android.systemui.settings.BrightnessController.1
        @Override // java.lang.Runnable
        public void run() {
            if (!BrightnessController.this.mListening) {
                BrightnessController.this.mListening = true;
                if (BrightnessController.this.mVrManager != null) {
                    try {
                        BrightnessController.this.mVrManager.registerListener(BrightnessController.this.mVrStateCallbacks);
                        BrightnessController.this.mIsVrModeEnabled = BrightnessController.this.mVrManager.getVrModeState();
                    } catch (RemoteException e) {
                        Log.e("StatusBar.BrightnessController", "Failed to register VR mode state listener: ", e);
                    }
                }
                BrightnessController.this.mBrightnessObserver.startObserving();
                BrightnessController.this.mUserTracker.startTracking();
                BrightnessController.this.mUpdateModeRunnable.run();
                BrightnessController.this.mUpdateSliderNoAnimRunnable.run();
                BrightnessController.this.mHandler.sendEmptyMessage(3);
            }
        }
    };
    private final Runnable mStopListeningRunnable = new Runnable() { // from class: com.android.systemui.settings.BrightnessController.2
        @Override // java.lang.Runnable
        public void run() {
            if (BrightnessController.this.mListening) {
                BrightnessController.this.mListening = false;
                if (BrightnessController.this.mVrManager != null) {
                    try {
                        BrightnessController.this.mVrManager.unregisterListener(BrightnessController.this.mVrStateCallbacks);
                    } catch (RemoteException e) {
                        Log.e("StatusBar.BrightnessController", "Failed to unregister VR mode state listener: ", e);
                    }
                }
                BrightnessController.this.mBrightnessObserver.stopObserving();
                BrightnessController.this.mUserTracker.stopTracking();
                BrightnessController.this.mHandler.sendEmptyMessage(4);
                if (BrightnessController.DEBUG) {
                    Log.d("StatusBar.BrightnessController", "mStopListeningRunnable mTracking: " + BrightnessController.this.mTracking + ", mAutomatic: " + BrightnessController.this.mAutomatic + ", mNewController: " + BrightnessController.this.mNewController + ", mBrightness: " + BrightnessController.this.mBrightness);
                }
                if (BrightnessController.this.mTracking) {
                    AsyncTask.execute(new Runnable() { // from class: com.android.systemui.settings.-$$Lambda$BrightnessController$2$R7xwW4ZTyw9CTX8R56AsACHdwQs
                        @Override // java.lang.Runnable
                        public final void run() {
                            BrightnessController.AnonymousClass2.this.lambda$run$0$BrightnessController$2();
                        }
                    });
                    BrightnessController.this.mTracking = false;
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$run$0 */
        public /* synthetic */ void lambda$run$0$BrightnessController$2() {
            String str = BrightnessController.this.mIsVrModeEnabled ? "screen_brightness_for_vr_float" : "screen_brightness_float";
            long unused = BrightnessController.mLastClickTimeMillis = SystemClock.uptimeMillis();
            Settings.System.putFloatForUser(BrightnessController.this.mContext.getContentResolver(), str, BrightnessController.this.mBrightness, -2);
        }
    };
    private boolean mTracking = false;
    private final Runnable mUpdateModeRunnable = new Runnable() { // from class: com.android.systemui.settings.BrightnessController.3
        @Override // java.lang.Runnable
        public void run() {
            if (BrightnessController.this.mAutomaticAvailable) {
                int intForUser = Settings.System.getIntForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2);
                BrightnessController.this.mAutomatic = intForUser != 0;
                OpMdmLogger.notifyBrightnessMode(BrightnessController.this.mAutomatic);
                BrightnessController.this.mHandler.obtainMessage(0, BrightnessController.this.mAutomatic ? 1 : 0, 0).sendToTarget();
                return;
            }
            BrightnessController.this.mHandler.obtainMessage(2, 0, 0).sendToTarget();
            BrightnessController.this.mHandler.obtainMessage(0, 0, 0).sendToTarget();
        }
    };
    private final Runnable mUpdateSliderNoAnimRunnable = new Runnable() { // from class: com.android.systemui.settings.BrightnessController.5
        @Override // java.lang.Runnable
        public void run() {
            float f;
            boolean z = BrightnessController.this.mIsVrModeEnabled;
            if (z) {
                f = Settings.System.getFloatForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_for_vr_float", BrightnessController.this.mDefaultBacklightForVr, -2);
            } else {
                f = Settings.System.getFloatForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_float", BrightnessController.this.mDefaultBacklight, -2);
            }
            int floatToIntBits = Float.floatToIntBits(f);
            Log.d("StatusBar.BrightnessController", "UpdateSliderNoAnimTask: valFloat=" + f + ", vAIB=" + floatToIntBits + ", auto=" + BrightnessController.this.mAutomatic + ", inVr=" + z);
            BrightnessController.this.mHandler.obtainMessage(1, floatToIntBits, z ? 1 : 0, Boolean.TRUE).sendToTarget();
        }
    };
    private final Runnable mUpdateSliderRunnable = new Runnable() { // from class: com.android.systemui.settings.BrightnessController.4
        @Override // java.lang.Runnable
        public void run() {
            float f;
            boolean z = BrightnessController.this.mIsVrModeEnabled;
            if (z) {
                f = Settings.System.getFloatForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_for_vr_float", BrightnessController.this.mDefaultBacklightForVr, -2);
            } else {
                f = Settings.System.getFloatForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_float", BrightnessController.this.mDefaultBacklight, -2);
            }
            int floatToIntBits = Float.floatToIntBits(f);
            Log.d("StatusBar.BrightnessController", "UpdateSliderTask: valFloat=" + f + ", vAIB=" + floatToIntBits + ", auto=" + BrightnessController.this.mAutomatic + ", inVr=" + z);
            BrightnessController.this.mHandler.obtainMessage(1, floatToIntBits, z ? 1 : 0).sendToTarget();
        }
    };
    private final CurrentUserTracker mUserTracker;
    private final IVrManager mVrManager;
    private final IVrStateCallbacks mVrStateCallbacks = new IVrStateCallbacks.Stub() { // from class: com.android.systemui.settings.BrightnessController.6
        public void onVrStateChanged(boolean z) {
            BrightnessController.this.mHandler.obtainMessage(5, z ? 1 : 0, 0).sendToTarget();
        }
    };

    public interface BrightnessStateChangeCallback {
        void onBrightnessLevelChanged();
    }

    static /* synthetic */ void lambda$new$0(View view) {
    }

    @Override // com.android.systemui.settings.ToggleSlider.Listener
    public void onInit(ToggleSlider toggleSlider) {
    }

    /* access modifiers changed from: private */
    public class BrightnessObserver extends ContentObserver {
        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            onChange(z, null);
        }

        private long getDuration() {
            long intForUser = (long) Settings.System.getIntForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_animation_duration", 0, -2);
            Log.i("StatusBar.BrightnessController", "animationDuration:" + intForUser);
            return intForUser;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (!z) {
                if (BrightnessController.BRIGHTNESS_MODE_URI.equals(uri)) {
                    BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateModeRunnable);
                    BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
                } else if (BrightnessController.BRIGHTNESS_FLOAT_URI.equals(uri)) {
                    BrightnessController.this.mAnimationDuration = getDuration();
                    if (SystemClock.uptimeMillis() - BrightnessController.mLastClickTimeMillis < 100) {
                        BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderNoAnimRunnable);
                    } else {
                        BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
                    }
                } else if (BrightnessController.BRIGHTNESS_FOR_VR_FLOAT_URI.equals(uri)) {
                    BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
                } else {
                    BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateModeRunnable);
                    if (SystemClock.uptimeMillis() - BrightnessController.mLastClickTimeMillis < 100) {
                        BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderNoAnimRunnable);
                    } else {
                        BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
                    }
                }
                if (OpUtils.isCustomFingerprint()) {
                    ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).notifyBrightnessChange();
                }
                Iterator it = BrightnessController.this.mChangeCallbacks.iterator();
                while (it.hasNext()) {
                    ((BrightnessStateChangeCallback) it.next()).onBrightnessLevelChanged();
                }
            }
        }

        public void startObserving() {
            ContentResolver contentResolver = BrightnessController.this.mContext.getContentResolver();
            contentResolver.unregisterContentObserver(this);
            contentResolver.registerContentObserver(BrightnessController.BRIGHTNESS_MODE_URI, false, this, -1);
            contentResolver.registerContentObserver(BrightnessController.BRIGHTNESS_URI, false, this, -1);
            contentResolver.registerContentObserver(BrightnessController.BRIGHTNESS_FLOAT_URI, false, this, -1);
            contentResolver.registerContentObserver(BrightnessController.BRIGHTNESS_FOR_VR_FLOAT_URI, false, this, -1);
        }

        public void stopObserving() {
            BrightnessController.this.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    public BrightnessController(Context context, ImageView imageView, ImageView imageView2, ToggleSlider toggleSlider, BroadcastDispatcher broadcastDispatcher) {
        this.mContext = context;
        this.mIcon = imageView2;
        this.mLevelIcon = imageView;
        imageView.setOnClickListener($$Lambda$BrightnessController$daXJdO8QhMk8Ivkwgi6j5oHn7Mk.INSTANCE);
        this.mIcon.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.settings.BrightnessController.8
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                BrightnessController.this.onClickAutomaticIcon();
            }
        });
        this.mSliderMax = 65535;
        this.mControl = toggleSlider;
        toggleSlider.setMax(65535);
        this.mBackgroundHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mUserTracker = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.settings.BrightnessController.9
            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateModeRunnable);
                BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
            }
        };
        this.mBrightnessObserver = new BrightnessObserver(this.mHandler);
        PowerManager powerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mMinimumBacklight = powerManager.getBrightnessConstraint(0);
        this.mMaximumBacklight = powerManager.getBrightnessConstraint(1);
        this.mDefaultBacklight = powerManager.getBrightnessConstraint(2);
        this.mMinimumBacklightForVr = powerManager.getBrightnessConstraint(5);
        this.mMaximumBacklightForVr = powerManager.getBrightnessConstraint(6);
        this.mDefaultBacklightForVr = powerManager.getBrightnessConstraint(7);
        this.mAutomaticAvailable = context.getResources().getBoolean(17891369);
        this.mDisplayManager = (DisplayManager) context.getSystemService(DisplayManager.class);
        this.mVrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        this.mNewController = this.mContext.getPackageManager().hasSystemFeature("oem.autobrightctl.animation.support");
        Log.d("StatusBar.BrightnessController", "mNewController=" + this.mNewController);
        this.AUTO_BRIGHTNESS_MINIMUM = this.mContext.getResources().getInteger(17694896);
        Log.d("StatusBar.BrightnessController", "AUTO_BRIGHTNESS_MINIMUM=" + this.AUTO_BRIGHTNESS_MINIMUM);
    }

    public void registerCallbacks() {
        this.mBackgroundHandler.post(this.mStartListeningRunnable);
    }

    public void unregisterCallbacks() {
        this.mBackgroundHandler.post(this.mStopListeningRunnable);
    }

    @Override // com.android.systemui.settings.ToggleSlider.Listener
    public void onChanged(ToggleSlider toggleSlider, boolean z, boolean z2, int i, boolean z3) {
        float f;
        int i2;
        final String str;
        float f2;
        if (DEBUG) {
            Log.d("StatusBar.BrightnessController", "Slider.onChanged value=" + i + ", extChange=" + this.mExternalChange + ", tracking=" + z + ", auto=" + z2);
        }
        this.mSliderValue = i;
        this.mTracking = z;
        updateIcon(this.mAutomatic);
        if (!this.mExternalChange) {
            ValueAnimator valueAnimator = this.mSliderAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            if (this.mIsVrModeEnabled) {
                i2 = 498;
                f = this.mMinimumBacklightForVr;
                f2 = this.mMaximumBacklightForVr;
                str = "screen_brightness_for_vr_float";
            } else {
                i2 = this.mAutomatic ? 219 : 218;
                f = this.mMinimumBacklight;
                f2 = this.mMaximumBacklight;
                str = "screen_brightness_float";
            }
            final float min = MathUtils.min(BrightnessUtils.convertGammaToLinearFloat(i, f, f2), 1.0f);
            this.mBrightness = min;
            if (z3) {
                Context context = this.mContext;
                MetricsLogger.action(context, i2, BrightnessSynchronizer.brightnessFloatToInt(context, min));
            }
            setBrightness(min);
            if (!z) {
                AsyncTask.execute(new Runnable() { // from class: com.android.systemui.settings.BrightnessController.10
                    @Override // java.lang.Runnable
                    public void run() {
                        long unused = BrightnessController.mLastClickTimeMillis = SystemClock.uptimeMillis();
                        Settings.System.putFloatForUser(BrightnessController.this.mContext.getContentResolver(), str, min, -2);
                    }
                });
            }
            Iterator<BrightnessStateChangeCallback> it = this.mChangeCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onBrightnessLevelChanged();
            }
        }
    }

    public void checkRestrictionAndSetEnabled() {
        this.mBackgroundHandler.post(new Runnable() { // from class: com.android.systemui.settings.BrightnessController.11
            @Override // java.lang.Runnable
            public void run() {
                ((ToggleSliderView) BrightnessController.this.mControl).setEnforcedAdmin(RestrictedLockUtilsInternal.checkIfRestrictionEnforced(BrightnessController.this.mContext, "no_config_brightness", BrightnessController.this.mUserTracker.getCurrentUserId()));
            }
        });
    }

    private void setMode(int i) {
        Settings.System.putIntForUser(this.mContext.getContentResolver(), "screen_brightness_mode", i, this.mUserTracker.getCurrentUserId());
    }

    private void setBrightness(float f) {
        if (DEBUG) {
            Log.d("StatusBar.BrightnessController", "setBrightness " + f);
        }
        this.mDisplayManager.setTemporaryBrightness(f);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateIcon(boolean z) {
        updateIconInternal(z, this.mIcon, this.mLevelIcon);
        updateIconInternal(z, this.mMirrorIcon, this.mMirrorLevelIcon);
    }

    private void updateIconInternal(boolean z, ImageView imageView, ImageView imageView2) {
        int i;
        if (imageView != null) {
            if (z) {
                i = C0006R$drawable.ic_qs_brightness_auto_on;
            } else {
                i = C0006R$drawable.ic_qs_brightness_auto_off;
            }
            imageView.setImageResource(i);
        }
        if (imageView2 == null) {
            return;
        }
        if (this.mIsVrModeEnabled) {
            int i2 = this.mSliderValue;
            if (((float) i2) <= this.mMinimumBacklightForVr) {
                imageView2.setImageResource(C0006R$drawable.ic_qs_brightness_low);
            } else if (i2 >= this.mSliderMax - 1) {
                imageView2.setImageResource(C0006R$drawable.ic_qs_brightness_high);
            } else {
                imageView2.setImageResource(C0006R$drawable.ic_qs_brightness_medium);
            }
        } else {
            int i3 = this.mSliderValue;
            if (((float) i3) <= this.mMinimumBacklight) {
                imageView2.setImageResource(C0006R$drawable.ic_qs_brightness_low);
            } else if (i3 >= this.mSliderMax - 1) {
                imageView2.setImageResource(C0006R$drawable.ic_qs_brightness_high);
            } else {
                imageView2.setImageResource(C0006R$drawable.ic_qs_brightness_medium);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVrMode(boolean z) {
        if (this.mIsVrModeEnabled != z) {
            this.mIsVrModeEnabled = z;
            this.mBackgroundHandler.post(this.mUpdateSliderRunnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSlider(float f, boolean z, boolean z2) {
        float f2;
        float f3;
        if (z) {
            f2 = this.mMinimumBacklightForVr;
            f3 = this.mMaximumBacklightForVr;
        } else {
            f2 = this.mMinimumBacklight;
            f3 = this.mMaximumBacklight;
        }
        if (!BrightnessSynchronizer.floatEquals(f, BrightnessUtils.convertGammaToLinearFloat(this.mControl.getValue(), f2, f3))) {
            int convertLinearToGammaFloat = BrightnessUtils.convertLinearToGammaFloat(f, f2, f3);
            this.mSliderValue = convertLinearToGammaFloat;
            animateSliderTo(convertLinearToGammaFloat, z2);
        }
    }

    private void animateSliderTo(int i, boolean z) {
        if (z) {
            Log.d("StatusBar.BrightnessController", "not inited, set to " + i);
            this.mControl.setValue(i);
        }
        ValueAnimator valueAnimator = this.mSliderAnimator;
        if (valueAnimator != null && valueAnimator.isStarted()) {
            Log.d("StatusBar.BrightnessController", "animateSliderTo: cancel anim.");
            this.mSliderAnimator.cancel();
        }
        this.mSliderAnimator = ValueAnimator.ofInt(this.mControl.getValue(), i);
        Log.d("StatusBar.BrightnessController", "animateSliderTo: animating from " + this.mControl.getValue() + " to " + i);
        this.mSliderAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.settings.-$$Lambda$BrightnessController$S-CS_s0jEi0EiTJesxKBGNeWZLE
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                BrightnessController.this.lambda$animateSliderTo$1$BrightnessController(valueAnimator2);
            }
        });
        long abs = (long) ((Math.abs(this.mControl.getValue() - i) * 3000) / 65535);
        if (abs <= 1000) {
            abs = 1000;
        }
        long j = this.mAnimationDuration;
        if (j > 0) {
            abs = j;
        }
        Log.d("StatusBar.BrightnessController", "AMD:" + abs);
        this.mSliderAnimator.setDuration(abs);
        this.mSliderAnimator.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateSliderTo$1 */
    public /* synthetic */ void lambda$animateSliderTo$1$BrightnessController(ValueAnimator valueAnimator) {
        this.mExternalChange = true;
        this.mControl.setValue(((Integer) valueAnimator.getAnimatedValue()).intValue());
        this.mExternalChange = false;
    }

    public void onClickAutomaticIcon() {
        OpMdmLogger.log("quick_bright", "auto", "1");
        setMode(!this.mAutomatic ? 1 : 0);
    }

    public void setMirrorView(View view) {
        this.mMirrorIcon = (ImageView) view.findViewById(C0008R$id.brightness_icon);
        this.mMirrorLevelIcon = (ImageView) view.findViewById(C0008R$id.brightness_level);
    }
}
