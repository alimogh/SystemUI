package com.android.systemui.doze;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.hardware.display.AmbientDisplayConfiguration;
import android.net.Uri;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.OpFeatures;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.doze.DozeSensors;
import com.android.systemui.plugins.SensorManagerPlugin;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.util.sensors.AsyncSensorManager;
import com.android.systemui.util.sensors.ProximitySensor;
import com.android.systemui.util.wakelock.WakeLock;
import com.oneplus.aod.OpAodUtils;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.function.Consumer;
public class DozeSensors {
    private static final boolean DEBUG = DozeService.DEBUG;
    private static final UiEventLogger UI_EVENT_LOGGER = new UiEventLoggerImpl();
    private final Callback mCallback;
    private final AmbientDisplayConfiguration mConfig;
    private final Context mContext;
    private CustomProximityCheck mCustomProximityCheck;
    private long mDebounceFrom;
    private final Handler mHandler = new Handler();
    private LightSensor mLightSensor;
    private boolean mListening;
    private MotionCheck mMotionCheck;
    private boolean mPaused;
    private PickupCheck mPickUpCheck;
    private final Consumer<Boolean> mProxCallback;
    private int mProximityResult = 0;
    private final ProximitySensor mProximitySensor;
    private final ContentResolver mResolver;
    private final AsyncSensorManager mSensorManager;
    protected TriggerSensor[] mSensors;
    private boolean mSettingRegistered;
    private final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) { // from class: com.android.systemui.doze.DozeSensors.2
        public void onChange(boolean z, Collection<Uri> collection, int i, int i2) {
            if (i2 == ActivityManager.getCurrentUser()) {
                for (TriggerSensor triggerSensor : DozeSensors.this.mSensors) {
                    triggerSensor.updateListening();
                }
            }
        }
    };
    private final WakeLock mWakeLock;

    public interface Callback {
        void onSensorPulse(int i, float f, float f2, float[] fArr);
    }

    public enum DozeSensorsUiEvent implements UiEventLogger.UiEventEnum {
        ACTION_AMBIENT_GESTURE_PICKUP(459);
        
        private final int mId;

        private DozeSensorsUiEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00f4: APUT  
      (r11v0 com.android.systemui.doze.DozeSensors$TriggerSensor[])
      (5 ??[int, float, short, byte, char])
      (wrap: com.android.systemui.doze.DozeSensors$PluginSensor : 0x00f0: CONSTRUCTOR  (r9v6 com.android.systemui.doze.DozeSensors$PluginSensor) = 
      (r18v0 'this' com.android.systemui.doze.DozeSensors A[IMMUTABLE_TYPE, THIS])
      (r2v5 com.android.systemui.plugins.SensorManagerPlugin$Sensor)
      ("doze_wake_display_gesture")
      (r4v3 boolean)
      (7 int)
      false
      false
      (r27v0 com.android.systemui.doze.DozeLog)
     call: com.android.systemui.doze.DozeSensors.PluginSensor.<init>(com.android.systemui.doze.DozeSensors, com.android.systemui.plugins.SensorManagerPlugin$Sensor, java.lang.String, boolean, int, boolean, boolean, com.android.systemui.doze.DozeLog):void type: CONSTRUCTOR)
     */
    public DozeSensors(Context context, AlarmManager alarmManager, AsyncSensorManager asyncSensorManager, DozeParameters dozeParameters, AmbientDisplayConfiguration ambientDisplayConfiguration, WakeLock wakeLock, Callback callback, Consumer<Boolean> consumer, DozeLog dozeLog, ProximitySensor proximitySensor, DozeHost dozeHost) {
        this.mContext = context;
        this.mSensorManager = asyncSensorManager;
        this.mConfig = ambientDisplayConfiguration;
        this.mWakeLock = wakeLock;
        this.mProxCallback = consumer;
        this.mResolver = context.getContentResolver();
        this.mCallback = callback;
        this.mProximitySensor = proximitySensor;
        boolean alwaysOnEnabled = this.mConfig.alwaysOnEnabled(-2);
        TriggerSensor[] triggerSensorArr = new TriggerSensor[7];
        triggerSensorArr[0] = new TriggerSensor(this, this.mSensorManager.getDefaultSensor(17), null, dozeParameters.getPulseOnSigMotion(), 2, false, false, dozeLog);
        triggerSensorArr[1] = new TriggerSensor(this.mSensorManager.getDefaultSensor(25), "doze_pulse_on_pick_up", true, ambientDisplayConfiguration.dozePickupSensorAvailable(), 3, false, false, false, dozeLog);
        triggerSensorArr[2] = new TriggerSensor(this, findSensorWithType(ambientDisplayConfiguration.doubleTapSensorType()), "doze_pulse_on_double_tap", true, 4, dozeParameters.doubleTapReportsTouchCoordinates(), true, dozeLog);
        triggerSensorArr[3] = new TriggerSensor(this, findSensorWithType(ambientDisplayConfiguration.tapSensorType()), "doze_tap_gesture", true, 9, false, true, dozeLog);
        triggerSensorArr[4] = new TriggerSensor(this, findSensorWithType(ambientDisplayConfiguration.longPressSensorType()), "doze_pulse_on_long_press", false, true, 5, true, true, dozeLog);
        triggerSensorArr[5] = new PluginSensor(this, new SensorManagerPlugin.Sensor(2), "doze_wake_display_gesture", this.mConfig.wakeScreenGestureAvailable() && alwaysOnEnabled, 7, false, false, dozeLog);
        triggerSensorArr[6] = new PluginSensor(new SensorManagerPlugin.Sensor(1), "doze_wake_screen_gesture", this.mConfig.wakeScreenGestureAvailable(), 8, false, false, this.mConfig.getWakeLockScreenDebounce(), dozeLog);
        this.mSensors = triggerSensorArr;
        boolean isPulsingBlocked = dozeHost.isPulsingBlocked();
        Log.i("DozeSensors", "isPulsingBlocked:" + isPulsingBlocked);
        if (!OpFeatures.isSupport(new int[]{60}) || isPulsingBlocked) {
            this.mPickUpCheck = new PickupCheck();
        } else {
            this.mMotionCheck = new MotionCheck(true, 3);
            this.mCustomProximityCheck = new CustomProximityCheck() { // from class: com.android.systemui.doze.DozeSensors.1
                @Override // com.android.systemui.doze.DozeSensors.CustomProximityCheck
                public void onProximityResult(int i) {
                    Log.d("DozeSensors", "onProximityResult: " + i);
                    int i2 = DozeSensors.this.mProximityResult;
                    DozeSensors.this.mProximityResult = i;
                    if (i2 == 1 && i == 2 && DozeSensors.this.mMotionCheck.getCurrentState() == 1) {
                        DozeSensors.this.mMotionCheck.resetCurrentState();
                        Log.d("DozeSensors", "pulse from pocket");
                        DozeSensors.this.mCallback.onSensorPulse(3, -1.0f, -1.0f, null);
                    }
                }
            };
            this.mLightSensor = new LightSensor();
        }
        setProxListening(false);
        this.mProximitySensor.register(new ProximitySensor.ProximitySensorListener() { // from class: com.android.systemui.doze.-$$Lambda$DozeSensors$eWcsfaBj95QArTbTaV_jJjjsPh4
            @Override // com.android.systemui.util.sensors.ProximitySensor.ProximitySensorListener
            public final void onSensorEvent(ProximitySensor.ProximityEvent proximityEvent) {
                DozeSensors.this.lambda$new$0$DozeSensors(proximityEvent);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$DozeSensors(ProximitySensor.ProximityEvent proximityEvent) {
        if (proximityEvent != null) {
            this.mProxCallback.accept(Boolean.valueOf(!proximityEvent.getNear()));
        }
    }

    public int getCustomProximityResult() {
        return this.mProximityResult;
    }

    public void destroy() {
        for (TriggerSensor triggerSensor : this.mSensors) {
            triggerSensor.setListening(false);
        }
        this.mProximitySensor.pause();
    }

    public void requestTemporaryDisable() {
        this.mDebounceFrom = SystemClock.uptimeMillis();
    }

    private Sensor findSensorWithType(String str) {
        return findSensorWithType(this.mSensorManager, str);
    }

    static Sensor findSensorWithType(SensorManager sensorManager, String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        for (Sensor sensor : sensorManager.getSensorList(-1)) {
            if (str.equals(sensor.getStringType())) {
                return sensor;
            }
        }
        return null;
    }

    public void setListening(boolean z) {
        StringBuilder sb = new StringBuilder();
        sb.append("setListening: mListening=");
        sb.append(this.mListening);
        sb.append(", listen= ");
        sb.append(z);
        sb.append(", callers= ");
        boolean z2 = true;
        sb.append(Debug.getCallers(1));
        Log.d("DozeSensors", sb.toString());
        if (!OpAodUtils.isMotionAwakeOn() || !z) {
            z2 = false;
        }
        Log.d("DozeSensors", "setListening: adjustListen= " + z2);
        MotionCheck motionCheck = this.mMotionCheck;
        if (!(motionCheck == null || motionCheck.isListened() == z2)) {
            this.mMotionCheck.setListening(z2);
        }
        PickupCheck pickupCheck = this.mPickUpCheck;
        if (!(pickupCheck == null || pickupCheck.isListened() == z2)) {
            this.mPickUpCheck.setListening(z2);
        }
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(this.mContext);
        LightSensor lightSensor = this.mLightSensor;
        if (!(lightSensor == null || lightSensor.isListened() == z)) {
            if (instance == null || !instance.isAlwaysOnEnabled()) {
                this.mLightSensor.setListening(false);
            } else {
                this.mLightSensor.setListening(z);
            }
        }
        if (this.mListening != z) {
            this.mListening = z;
            updateListening();
        }
    }

    public void setPaused(boolean z) {
        if (this.mPaused != z) {
            this.mPaused = z;
            updateListening();
        }
    }

    public void updateListening() {
        boolean z = false;
        for (TriggerSensor triggerSensor : this.mSensors) {
            triggerSensor.setListening(this.mListening);
            if (this.mListening) {
                z = true;
            }
        }
        if (DEBUG) {
            Log.i("DozeSensors", "updateListening: mPaused = " + this.mPaused + ", callstack = " + Debug.getCallers(3));
        }
        MotionCheck motionCheck = this.mMotionCheck;
        if (motionCheck != null && motionCheck.isListened()) {
            this.mMotionCheck.setListening(!this.mPaused);
        }
        if (!z) {
            this.mResolver.unregisterContentObserver(this.mSettingsObserver);
        } else if (!this.mSettingRegistered) {
            for (TriggerSensor triggerSensor2 : this.mSensors) {
                triggerSensor2.registerSettingsObserver(this.mSettingsObserver);
            }
        }
        this.mSettingRegistered = z;
    }

    public void setLightSensorListening(boolean z) {
        LightSensor lightSensor = this.mLightSensor;
        if (lightSensor != null) {
            lightSensor.setListening(z);
        }
    }

    public void setTouchscreenSensorsListening(boolean z) {
        TriggerSensor[] triggerSensorArr = this.mSensors;
        for (TriggerSensor triggerSensor : triggerSensorArr) {
            if (triggerSensor.mRequiresTouchscreen) {
                triggerSensor.setListening(z);
            }
        }
    }

    public void onUserSwitched() {
        for (TriggerSensor triggerSensor : this.mSensors) {
            triggerSensor.updateListening();
        }
    }

    public void setCustomProxListening(boolean z) {
        CustomProximityCheck customProximityCheck;
        if (shouldListenProximity() && (customProximityCheck = this.mCustomProximityCheck) != null) {
            customProximityCheck.setListening(z);
        }
    }

    private boolean shouldListenProximity() {
        return !OpFeatures.isSupport(new int[]{91}) || !OpFeatures.isSupport(new int[]{60});
    }

    public void setProxListening(boolean z) {
        if (shouldListenProximity()) {
            if (this.mProximitySensor.isRegistered() && z) {
                this.mProximitySensor.alertListeners();
            } else if (z) {
                this.mProximitySensor.resume();
            } else {
                this.mProximitySensor.pause();
            }
        }
    }

    public void dump(PrintWriter printWriter) {
        TriggerSensor[] triggerSensorArr = this.mSensors;
        for (TriggerSensor triggerSensor : triggerSensorArr) {
            printWriter.println("  Sensor: " + triggerSensor.toString());
        }
        printWriter.println("  ProxSensor: " + this.mProximitySensor.toString());
    }

    public Boolean isProximityCurrentlyNear() {
        return this.mProximitySensor.isNear();
    }

    public void resetMotionValue() {
        MotionCheck motionCheck = this.mMotionCheck;
        if (motionCheck != null) {
            motionCheck.resetCurrentState();
        }
    }

    public void onDozeTimeChanged() {
        LightSensor lightSensor = this.mLightSensor;
        if (lightSensor != null) {
            lightSensor.receiveTimeChanged();
        }
    }

    /* access modifiers changed from: private */
    public class MotionCheck implements SensorEventListener, Runnable {
        private boolean mConfigured;
        private int mCurrentState;
        private boolean mFinished = false;
        private boolean mProximityChecking;
        private int mPulseReason;
        private boolean mRegistered;
        private int mSensorType = 33171028;

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        @Override // java.lang.Runnable
        public void run() {
        }

        public MotionCheck(boolean z, int i) {
            Log.d("DozeSensors", "choose sensor: TYPE_MOTION");
            this.mConfigured = z;
            this.mPulseReason = i;
        }

        public void check() {
            if (!this.mFinished && !this.mRegistered && this.mConfigured) {
                Sensor defaultSensor = DozeSensors.this.mSensorManager.getDefaultSensor(this.mSensorType);
                if (defaultSensor == null) {
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensor.MotionCheck", "No sensor found");
                    }
                    finishWithResult(0);
                    return;
                }
                Log.d("DozeSensor.MotionCheck", "sensor registered " + hashCode());
                defaultSensor.getMaximumRange();
                DozeSensors.this.mSensorManager.registerListener(this, defaultSensor, 3, 0, DozeSensors.this.mHandler);
                DozeSensors.this.mHandler.postDelayed(this, 500);
                this.mRegistered = true;
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            Log.d("DozeSensor.MotionCheck", "onSensorChanged: proximity checking = " + this.mProximityChecking);
            if (!this.mProximityChecking) {
                float[] fArr = sensorEvent.values;
                if (fArr.length == 0) {
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensor.MotionCheck", "Event has no values!");
                    }
                    finishWithResult(0);
                } else if (fArr[0] == 1.0f) {
                    finishWithResult(1);
                } else if (((double) fArr[0]) == 2.0d) {
                    finishWithResult(2);
                } else if (fArr[0] == 0.0f) {
                    finishWithResult(3);
                    DozeSensors.this.mHandler.removeCallbacks(this);
                } else if (fArr[0] == -1.0f) {
                    finishWithResult(4);
                }
                Log.d("DozeSensor.MotionCheck", "onSensorChanged: value = " + sensorEvent.values[0]);
            }
        }

        public boolean isListened() {
            return this.mRegistered;
        }

        private void finishWithResult(int i) {
            if (this.mRegistered) {
                if (this.mCurrentState != 0 && i == 1) {
                    DozeSensors.this.mCallback.onSensorPulse(this.mPulseReason, -1.0f, -1.0f, null);
                } else if (this.mCurrentState == 0 || i != 2) {
                    int i2 = this.mCurrentState;
                    if ((i2 == 2 || i2 == 1) && i == 4) {
                        DozeSensors.this.mCallback.onSensorPulse(-1, -1.0f, -1.0f, null);
                        i = 3;
                    }
                } else {
                    DozeSensors.this.mCallback.onSensorPulse(this.mPulseReason, -1.0f, -1.0f, null);
                }
                this.mCurrentState = i;
            }
        }

        public void setListening(boolean z) {
            if (z) {
                check();
            } else {
                release();
            }
        }

        public int getCurrentState() {
            return this.mCurrentState;
        }

        public void resetCurrentState() {
            this.mCurrentState = 3;
        }

        private void release() {
            if (this.mRegistered && DozeSensors.this.mSensorManager != null) {
                Log.d("DozeSensor.MotionCheck", "Unregister Motion Sensor " + hashCode());
                DozeSensors.this.mSensorManager.unregisterListener(this);
                this.mRegistered = false;
            }
        }
    }

    private class PickupCheck implements SensorEventListener, Runnable {
        private int mCurrentState;
        private boolean mFinished = false;
        private float mMaxRange;
        private boolean mProximityChecking;
        private boolean mRegistered;
        private int mSensorType = 33171026;

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        @Override // java.lang.Runnable
        public void run() {
        }

        public PickupCheck() {
            Log.d("DozeSensors", "choose sensor: TYPE_PICK_UP");
        }

        public void check() {
            if (!this.mFinished && !this.mRegistered) {
                Sensor defaultSensor = DozeSensors.this.mSensorManager.getDefaultSensor(this.mSensorType);
                if (defaultSensor == null) {
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors.PickupCheck", "No sensor found");
                    }
                    finishWithResult(0);
                    return;
                }
                Log.d("DozeSensors.PickupCheck", "sensor registered");
                this.mMaxRange = defaultSensor.getMaximumRange();
                DozeSensors.this.mSensorManager.registerListener(this, defaultSensor, 3, 0, DozeSensors.this.mHandler);
                DozeSensors.this.mHandler.postDelayed(this, 500);
                this.mRegistered = true;
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            Log.i("DozeSensors", "onSensorChanged = " + sensorEvent);
            if (!this.mProximityChecking) {
                boolean z = false;
                if (sensorEvent.values.length == 0) {
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors.PickupCheck", "Event has no values!");
                    }
                    finishWithResult(0);
                    return;
                }
                if (DozeSensors.DEBUG) {
                    Log.d("DozeSensors.PickupCheck", "Event: value=" + sensorEvent.values[0] + " max=" + this.mMaxRange);
                }
                int i = 1;
                if (sensorEvent.values[0] == 1.0f) {
                    z = true;
                }
                if (!z) {
                    i = 2;
                }
                finishWithResult(i);
            }
        }

        private void finishWithResult(int i) {
            if (this.mRegistered) {
                if (!OpAodUtils.isAlwaysOnEnabled()) {
                    if (this.mCurrentState != 0 && i == 1) {
                        DozeSensors.this.mCallback.onSensorPulse(3, -1.0f, -1.0f, null);
                    } else if (i == 2) {
                        DozeSensors.this.mCallback.onSensorPulse(-1, -1.0f, -1.0f, null);
                    }
                }
                this.mCurrentState = i;
            }
        }

        public boolean isListened() {
            return this.mRegistered;
        }

        public void setListening(boolean z) {
            if (z) {
                check();
            } else {
                release();
            }
        }

        private void release() {
            if (this.mRegistered && DozeSensors.this.mSensorManager != null) {
                Log.d("DozeSensors.PickupCheck", "Unregister P Sensor");
                DozeSensors.this.mSensorManager.unregisterListener(this);
                this.mRegistered = false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public abstract class CustomProximityCheck implements SensorEventListener, Runnable {
        private boolean mFinished;
        private float mMaxRange;
        private boolean mRegistered;

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public abstract void onProximityResult(int i);

        CustomProximityCheck() {
        }

        public void check() {
            if (!this.mFinished && !this.mRegistered) {
                Sensor defaultSensor = DozeSensors.this.mSensorManager.getDefaultSensor(33171025);
                if (defaultSensor == null) {
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensor.CustomProximityCheck", "No sensor found");
                    }
                    finishWithResult(0);
                    return;
                }
                Log.d("DozeSensor.CustomProximityCheck", "register pocket " + hashCode());
                this.mMaxRange = defaultSensor.getMaximumRange();
                DozeSensors.this.mSensorManager.registerListener(this, defaultSensor, 3, 0, DozeSensors.this.mHandler);
                DozeSensors.this.mHandler.postDelayed(this, 500);
                this.mRegistered = true;
            }
        }

        public void setListening(boolean z) {
            if (z) {
                check();
            } else {
                release();
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            boolean z = false;
            if (sensorEvent.values.length == 0) {
                if (DozeSensors.DEBUG) {
                    Log.d("DozeSensor.CustomProximityCheck", "Event has no values!");
                }
                finishWithResult(0);
                return;
            }
            if (DozeSensors.DEBUG) {
                Log.d("DozeSensor.CustomProximityCheck", "Event: value=" + sensorEvent.values[0] + " max=" + this.mMaxRange);
            }
            int i = 1;
            if (sensorEvent.values[0] == 1.0f) {
                z = true;
            }
            if (!z) {
                i = 2;
            }
            finishWithResult(i);
            KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(DozeSensors.this.mContext);
            if (instance != null && instance.isAlwaysOnEnabled()) {
                DozeSensors.this.mHandler.post(new Runnable(z) { // from class: com.android.systemui.doze.-$$Lambda$DozeSensors$CustomProximityCheck$9pIc7EqJWnwMe91o9UTr1WLuvZE
                    public final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        DozeSensors.CustomProximityCheck.this.lambda$onSensorChanged$0$DozeSensors$CustomProximityCheck(this.f$1);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSensorChanged$0 */
        public /* synthetic */ void lambda$onSensorChanged$0$DozeSensors$CustomProximityCheck(boolean z) {
            DozeSensors.this.mProxCallback.accept(Boolean.valueOf(!z));
        }

        @Override // java.lang.Runnable
        public void run() {
            if (DozeSensors.DEBUG) {
                Log.d("DozeSensor.CustomProximityCheck", "No event received before timeout");
            }
            finishWithResult(0);
        }

        private void finishWithResult(int i) {
            if (this.mRegistered) {
                DozeSensors.this.mHandler.removeCallbacks(this);
            }
            onProximityResult(i);
        }

        private void release() {
            if (this.mRegistered && DozeSensors.this.mSensorManager != null) {
                Log.d("DozeSensor.CustomProximityCheck", "Unregister pocket Sensor " + hashCode());
                DozeSensors.this.mSensorManager.unregisterListener(this);
                this.mRegistered = false;
            }
        }
    }

    private class LightSensor implements SensorEventListener {
        private final boolean LIGHT_SENSOR_ENABLED;
        boolean mIsIgnoredFirstChanged;
        boolean mIsLowLightEnv;
        private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
        boolean mRegistered;
        boolean mRegisteredSensor;
        private int mSensorType;

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public LightSensor() {
            boolean z = true;
            this.LIGHT_SENSOR_ENABLED = (!SystemProperties.getBoolean("debug.aod_low_light_detect.enabled", true) || !OpAodUtils.isSupportAlwaysOn()) ? false : z;
            this.mRegisteredSensor = false;
            this.mIsLowLightEnv = false;
            this.mIsIgnoredFirstChanged = false;
            this.mSensorType = 5;
            this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(DozeSensors.this.mContext);
        }

        public void setListening(boolean z) {
            if (this.LIGHT_SENSOR_ENABLED) {
                if (DozeSensors.DEBUG) {
                    Log.i("DozeSensor.LightSensor", "setListening = " + z);
                }
                if (z) {
                    check();
                } else {
                    release();
                }
            } else {
                Log.i("DozeSensor.LightSensor", "disable light sensor");
            }
        }

        public void check() {
            if (DozeSensors.DEBUG) {
                Log.i("DozeSensor.LightSensor", "isRegistered = " + this.mRegistered + ", sensor registed = " + this.mRegisteredSensor);
            }
            if (!this.mRegistered) {
                if (DozeSensors.this.mSensorManager.getDefaultSensor(this.mSensorType) != null) {
                    this.mRegistered = true;
                } else if (DozeSensors.DEBUG) {
                    Log.d("DozeSensor.LightSensor", "No sensor found");
                }
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            boolean z = this.mIsLowLightEnv;
            boolean z2 = sensorEvent.values[0] <= 20.0f;
            if (DozeSensors.DEBUG) {
                Log.i("DozeSensor.LightSensor", "Light values= " + sensorEvent.values[0] + ", pre= " + z + ", cur= " + z2);
            }
            if (z2 != z) {
                this.mIsLowLightEnv = z2;
                KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(DozeSensors.this.mContext);
                if (instance != null) {
                    instance.notifyEnvironmentLightChanged(this.mIsLowLightEnv);
                } else {
                    Log.e("DozeSensor.LightSensor", "can't get monitor object");
                }
            }
            DozeSensors.this.mSensorManager.unregisterListener(this);
            this.mRegisteredSensor = false;
        }

        private void release() {
            if (this.mRegistered) {
                this.mRegistered = false;
                this.mIsLowLightEnv = false;
                if (DozeSensors.this.mSensorManager != null && this.mRegisteredSensor) {
                    DozeSensors.this.mSensorManager.unregisterListener(this);
                }
                this.mIsIgnoredFirstChanged = false;
                this.mRegisteredSensor = false;
            }
        }

        public boolean isListened() {
            return this.mRegistered;
        }

        public void receiveTimeChanged() {
            boolean isDeviceInteractive = this.mKeyguardUpdateMonitor.isDeviceInteractive();
            boolean isScreenOn = this.mKeyguardUpdateMonitor.isScreenOn();
            if (DozeSensors.DEBUG) {
                Log.i("DozeSensor.LightSensor", "onTimeChanged: registeredSensor= " + this.mRegisteredSensor + ", interactive= " + isDeviceInteractive + ", screenOn= " + isScreenOn + ", ignoreFirstChanged= " + this.mIsIgnoredFirstChanged);
            }
            if (!isDeviceInteractive && isScreenOn) {
                if (!this.mIsIgnoredFirstChanged) {
                    this.mIsIgnoredFirstChanged = true;
                    return;
                }
                this.mRegisteredSensor = true;
                DozeSensors.this.mSensorManager.registerListener(this, DozeSensors.this.mSensorManager.getDefaultSensor(this.mSensorType), 3, 0, DozeSensors.this.mHandler);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class TriggerSensor extends TriggerEventListener {
        final boolean mConfigured;
        protected boolean mDisabled;
        protected final DozeLog mDozeLog;
        protected boolean mIgnoresSetting;
        final int mPulseReason;
        protected boolean mRegistered;
        private final boolean mReportsTouchCoordinates;
        protected boolean mRequested;
        private final boolean mRequiresTouchscreen;
        final Sensor mSensor;
        private final String mSetting;
        private final boolean mSettingDefault;

        public TriggerSensor(DozeSensors dozeSensors, Sensor sensor, String str, boolean z, int i, boolean z2, boolean z3, DozeLog dozeLog) {
            this(dozeSensors, sensor, str, true, z, i, z2, z3, dozeLog);
        }

        public TriggerSensor(DozeSensors dozeSensors, Sensor sensor, String str, boolean z, boolean z2, int i, boolean z3, boolean z4, DozeLog dozeLog) {
            this(sensor, str, z, z2, i, z3, z4, false, dozeLog);
        }

        private TriggerSensor(Sensor sensor, String str, boolean z, boolean z2, int i, boolean z3, boolean z4, boolean z5, DozeLog dozeLog) {
            this.mSensor = sensor;
            this.mSetting = str;
            this.mSettingDefault = z;
            this.mConfigured = z2;
            this.mPulseReason = i;
            this.mReportsTouchCoordinates = z3;
            this.mRequiresTouchscreen = z4;
            this.mIgnoresSetting = z5;
            this.mDozeLog = dozeLog;
        }

        public void setListening(boolean z) {
            if (this.mRequested != z) {
                this.mRequested = z;
                updateListening();
            }
        }

        public void updateListening() {
            if (this.mConfigured && this.mSensor != null) {
                if (this.mRequested && !this.mDisabled && ((enabledBySetting() || this.mIgnoresSetting) && !this.mRegistered)) {
                    this.mRegistered = DozeSensors.this.mSensorManager.requestTriggerSensor(this, this.mSensor);
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors", "requestTriggerSensor " + this.mRegistered);
                    }
                } else if (this.mRegistered) {
                    boolean cancelTriggerSensor = DozeSensors.this.mSensorManager.cancelTriggerSensor(this, this.mSensor);
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors", "cancelTriggerSensor " + cancelTriggerSensor);
                    }
                    this.mRegistered = false;
                }
            }
        }

        /* access modifiers changed from: protected */
        public boolean enabledBySetting() {
            if (!DozeSensors.this.mConfig.enabled(-2)) {
                return false;
            }
            if (TextUtils.isEmpty(this.mSetting)) {
                return true;
            }
            if (Settings.Secure.getIntForUser(DozeSensors.this.mResolver, this.mSetting, this.mSettingDefault ? 1 : 0, -2) != 0) {
                return true;
            }
            return false;
        }

        @Override // java.lang.Object
        public String toString() {
            return "{mRegistered=" + this.mRegistered + ", mRequested=" + this.mRequested + ", mDisabled=" + this.mDisabled + ", mConfigured=" + this.mConfigured + ", mIgnoresSetting=" + this.mIgnoresSetting + ", mSensor=" + this.mSensor + "}";
        }

        @Override // android.hardware.TriggerEventListener
        public void onTrigger(TriggerEvent triggerEvent) {
            this.mDozeLog.traceSensor(this.mPulseReason);
            DozeSensors.this.mHandler.post(DozeSensors.this.mWakeLock.wrap(new Runnable(triggerEvent) { // from class: com.android.systemui.doze.-$$Lambda$DozeSensors$TriggerSensor$O2XJN2HKJ96bSF_1qNx6jPK-eFk
                public final /* synthetic */ TriggerEvent f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    DozeSensors.TriggerSensor.this.lambda$onTrigger$0$DozeSensors$TriggerSensor(this.f$1);
                }
            }));
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x006c  */
        /* JADX WARNING: Removed duplicated region for block: B:18:? A[RETURN, SYNTHETIC] */
        /* renamed from: lambda$onTrigger$0 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public /* synthetic */ void lambda$onTrigger$0$DozeSensors$TriggerSensor(android.hardware.TriggerEvent r6) {
            /*
                r5 = this;
                boolean r0 = com.android.systemui.doze.DozeSensors.access$600()
                if (r0 == 0) goto L_0x0020
                java.lang.StringBuilder r0 = new java.lang.StringBuilder
                r0.<init>()
                java.lang.String r1 = "onTrigger: "
                r0.append(r1)
                java.lang.String r1 = r5.triggerEventToString(r6)
                r0.append(r1)
                java.lang.String r0 = r0.toString()
                java.lang.String r1 = "DozeSensors"
                android.util.Log.d(r1, r0)
            L_0x0020:
                android.hardware.Sensor r0 = r5.mSensor
                r1 = 0
                if (r0 == 0) goto L_0x0046
                int r0 = r0.getType()
                r2 = 25
                if (r0 != r2) goto L_0x0046
                float[] r0 = r6.values
                r0 = r0[r1]
                int r0 = (int) r0
                com.android.systemui.doze.DozeSensors r2 = com.android.systemui.doze.DozeSensors.this
                android.content.Context r2 = com.android.systemui.doze.DozeSensors.access$800(r2)
                r3 = 411(0x19b, float:5.76E-43)
                com.android.internal.logging.MetricsLogger.action(r2, r3, r0)
                com.android.internal.logging.UiEventLogger r0 = com.android.systemui.doze.DozeSensors.access$1400()
                com.android.systemui.doze.DozeSensors$DozeSensorsUiEvent r2 = com.android.systemui.doze.DozeSensors.DozeSensorsUiEvent.ACTION_AMBIENT_GESTURE_PICKUP
                r0.log(r2)
            L_0x0046:
                r5.mRegistered = r1
                boolean r0 = r5.mReportsTouchCoordinates
                r2 = -1082130432(0xffffffffbf800000, float:-1.0)
                if (r0 == 0) goto L_0x005a
                float[] r0 = r6.values
                int r3 = r0.length
                r4 = 2
                if (r3 < r4) goto L_0x005a
                r2 = r0[r1]
                r1 = 1
                r0 = r0[r1]
                goto L_0x005b
            L_0x005a:
                r0 = r2
            L_0x005b:
                com.android.systemui.doze.DozeSensors r1 = com.android.systemui.doze.DozeSensors.this
                com.android.systemui.doze.DozeSensors$Callback r1 = com.android.systemui.doze.DozeSensors.access$300(r1)
                int r3 = r5.mPulseReason
                float[] r6 = r6.values
                r1.onSensorPulse(r3, r2, r0, r6)
                boolean r6 = r5.mRegistered
                if (r6 != 0) goto L_0x006f
                r5.updateListening()
            L_0x006f:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.doze.DozeSensors.TriggerSensor.lambda$onTrigger$0$DozeSensors$TriggerSensor(android.hardware.TriggerEvent):void");
        }

        public void registerSettingsObserver(ContentObserver contentObserver) {
            if (this.mConfigured && !TextUtils.isEmpty(this.mSetting)) {
                DozeSensors.this.mResolver.registerContentObserver(Settings.Secure.getUriFor(this.mSetting), false, DozeSensors.this.mSettingsObserver, -1);
            }
        }

        /* access modifiers changed from: protected */
        public String triggerEventToString(TriggerEvent triggerEvent) {
            if (triggerEvent == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder("SensorEvent[");
            sb.append(triggerEvent.timestamp);
            sb.append(',');
            sb.append(triggerEvent.sensor.getName());
            if (triggerEvent.values != null) {
                for (int i = 0; i < triggerEvent.values.length; i++) {
                    sb.append(',');
                    sb.append(triggerEvent.values[i]);
                }
            }
            sb.append(']');
            return sb.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public class PluginSensor extends TriggerSensor implements SensorManagerPlugin.SensorEventListener {
        private long mDebounce;
        final SensorManagerPlugin.Sensor mPluginSensor;

        PluginSensor(DozeSensors dozeSensors, SensorManagerPlugin.Sensor sensor, String str, boolean z, int i, boolean z2, boolean z3, DozeLog dozeLog) {
            this(sensor, str, z, i, z2, z3, 0, dozeLog);
        }

        PluginSensor(SensorManagerPlugin.Sensor sensor, String str, boolean z, int i, boolean z2, boolean z3, long j, DozeLog dozeLog) {
            super(DozeSensors.this, null, str, z, i, z2, z3, dozeLog);
            this.mPluginSensor = sensor;
            this.mDebounce = j;
        }

        @Override // com.android.systemui.doze.DozeSensors.TriggerSensor
        public void updateListening() {
            if (this.mConfigured) {
                AsyncSensorManager asyncSensorManager = DozeSensors.this.mSensorManager;
                if (this.mRequested && !this.mDisabled && ((enabledBySetting() || this.mIgnoresSetting) && !this.mRegistered)) {
                    asyncSensorManager.registerPluginListener(this.mPluginSensor, this);
                    this.mRegistered = true;
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors", "registerPluginListener");
                    }
                } else if (this.mRegistered) {
                    asyncSensorManager.unregisterPluginListener(this.mPluginSensor, this);
                    this.mRegistered = false;
                    if (DozeSensors.DEBUG) {
                        Log.d("DozeSensors", "unregisterPluginListener");
                    }
                }
            }
        }

        @Override // com.android.systemui.doze.DozeSensors.TriggerSensor, java.lang.Object
        public String toString() {
            return "{mRegistered=" + this.mRegistered + ", mRequested=" + this.mRequested + ", mDisabled=" + this.mDisabled + ", mConfigured=" + this.mConfigured + ", mIgnoresSetting=" + this.mIgnoresSetting + ", mSensor=" + this.mPluginSensor + "}";
        }

        private String triggerEventToString(SensorManagerPlugin.SensorEvent sensorEvent) {
            if (sensorEvent == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder("PluginTriggerEvent[");
            sb.append(sensorEvent.getSensor());
            sb.append(',');
            sb.append(sensorEvent.getVendorType());
            if (sensorEvent.getValues() != null) {
                for (int i = 0; i < sensorEvent.getValues().length; i++) {
                    sb.append(',');
                    sb.append(sensorEvent.getValues()[i]);
                }
            }
            sb.append(']');
            return sb.toString();
        }

        @Override // com.android.systemui.plugins.SensorManagerPlugin.SensorEventListener
        public void onSensorChanged(SensorManagerPlugin.SensorEvent sensorEvent) {
            this.mDozeLog.traceSensor(this.mPulseReason);
            DozeSensors.this.mHandler.post(DozeSensors.this.mWakeLock.wrap(new Runnable(sensorEvent) { // from class: com.android.systemui.doze.-$$Lambda$DozeSensors$PluginSensor$EFDqlQhDL6RwEmmtbTd8M88V_8Y
                public final /* synthetic */ SensorManagerPlugin.SensorEvent f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    DozeSensors.PluginSensor.this.lambda$onSensorChanged$0$DozeSensors$PluginSensor(this.f$1);
                }
            }));
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSensorChanged$0 */
        public /* synthetic */ void lambda$onSensorChanged$0$DozeSensors$PluginSensor(SensorManagerPlugin.SensorEvent sensorEvent) {
            if (SystemClock.uptimeMillis() < DozeSensors.this.mDebounceFrom + this.mDebounce) {
                Log.d("DozeSensors", "onSensorEvent dropped: " + triggerEventToString(sensorEvent));
                return;
            }
            if (DozeSensors.DEBUG) {
                Log.d("DozeSensors", "onSensorEvent: " + triggerEventToString(sensorEvent));
            }
            DozeSensors.this.mCallback.onSensorPulse(this.mPulseReason, -1.0f, -1.0f, sensorEvent.getValues());
        }
    }
}
