package com.android.systemui.statusbar.phone;

import android.content.res.Resources;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Debug;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.MathUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0009R$integer;
import com.android.systemui.doze.AlwaysOnDisplayPolicy;
import com.android.systemui.tuner.TunerService;
import com.oneplus.aod.OpAodUtils;
public class DozeParameters implements TunerService.Tunable, com.android.systemui.plugins.statusbar.DozeParameters {
    public static final boolean FORCE_BLANKING = SystemProperties.getBoolean("debug.force_blanking", false);
    public static final boolean FORCE_NO_BLANKING = SystemProperties.getBoolean("debug.force_no_blanking", false);
    private final AlwaysOnDisplayPolicy mAlwaysOnPolicy;
    private final AmbientDisplayConfiguration mAmbientDisplayConfiguration;
    private boolean mControlScreenOffAnimation;
    private boolean mDozeAlwaysOn;
    private final PowerManager mPowerManager;
    private final Resources mResources;
    private int mUserId = -1;

    public boolean getDozeSuspendDisplayStateSupported() {
        return true;
    }

    protected DozeParameters(Resources resources, AmbientDisplayConfiguration ambientDisplayConfiguration, AlwaysOnDisplayPolicy alwaysOnDisplayPolicy, PowerManager powerManager, TunerService tunerService) {
        this.mResources = resources;
        this.mAmbientDisplayConfiguration = ambientDisplayConfiguration;
        this.mAlwaysOnPolicy = alwaysOnDisplayPolicy;
        this.mControlScreenOffAnimation = !getDisplayNeedsBlanking();
        this.mPowerManager = powerManager;
        this.mUserId = KeyguardUpdateMonitor.getCurrentUser();
        Log.d("DozeParameters", "DozeParameters init:" + hashCode());
        this.mPowerManager.setDozeAfterScreenOff(this.mControlScreenOffAnimation ^ true);
        if (tunerService != null) {
            tunerService.addTunable(this, "doze_always_on", "accessibility_display_inversion_enabled");
        }
    }

    public boolean getDisplayStateSupported() {
        return getBoolean("doze.display.supported", C0003R$bool.doze_display_state_supported);
    }

    public float getScreenBrightnessDoze() {
        return ((float) this.mResources.getInteger(17694897)) / 255.0f;
    }

    public int getPulseVisibleDuration(int i) {
        if (OpAodUtils.isAlwaysOnEnabled()) {
            return Integer.MAX_VALUE;
        }
        if (i == 10) {
            return getInt("op.doze.three.key.pusle.duration.visible", C0009R$integer.op_doze_three_key_pulse_duration_visible);
        }
        if (i == 3) {
            return getInt("op.doze.pick.up.pusle.duration.visible", C0009R$integer.op_doze_pick_up_pulse_duration_visible);
        }
        if (i == 12) {
            return getInt("op.doze.single.tap.pusle.duration.visible", C0009R$integer.op_doze_single_tap_pulse_duration_visible);
        }
        if (i == 13) {
            return getInt("op.doze.fingerprint.poke.pusle.duration.visible", C0009R$integer.op_doze_fingerprint_poke_pulse_duration_visible);
        }
        return getInt("doze.pulse.duration.visible", C0009R$integer.op_doze_pulse_duration_visible);
    }

    public boolean getPulseOnSigMotion() {
        return getBoolean("doze.pulse.sigmotion", C0003R$bool.doze_pulse_on_significant_motion);
    }

    public boolean getProxCheckBeforePulse() {
        return getBoolean("doze.pulse.proxcheck", C0003R$bool.doze_proximity_check_before_pulse);
    }

    public int getPickupVibrationThreshold() {
        return getInt("doze.pickup.vibration.threshold", C0009R$integer.doze_pickup_vibration_threshold);
    }

    public long getWallpaperAodDuration() {
        if (shouldControlScreenOff()) {
            return 2500;
        }
        return this.mAlwaysOnPolicy.wallpaperVisibilityDuration;
    }

    public long getWallpaperFadeOutDuration() {
        return this.mAlwaysOnPolicy.wallpaperFadeOutDuration;
    }

    public boolean getAlwaysOn() {
        return this.mDozeAlwaysOn;
    }

    public boolean getDisplayNeedsBlanking() {
        return FORCE_BLANKING || (!FORCE_NO_BLANKING && this.mResources.getBoolean(17891415));
    }

    @Override // com.android.systemui.plugins.statusbar.DozeParameters
    public boolean shouldControlScreenOff() {
        return this.mControlScreenOffAnimation;
    }

    public void setControlScreenOffAnimation(boolean z) {
        Log.d("DozeParameters", "setControlScreenOffAnimation, controlScreenOffAnimation:" + z + ", stack:" + Debug.getCallers(10));
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (this.mControlScreenOffAnimation != z || currentUser != this.mUserId) {
            this.mUserId = currentUser;
            this.mControlScreenOffAnimation = z;
            this.mPowerManager.setDozeAfterScreenOff(!z);
        }
    }

    private boolean getBoolean(String str, int i) {
        return SystemProperties.getBoolean(str, this.mResources.getBoolean(i));
    }

    private int getInt(String str, int i) {
        return MathUtils.constrain(SystemProperties.getInt(str, this.mResources.getInteger(i)), 0, 60000);
    }

    public boolean doubleTapReportsTouchCoordinates() {
        return this.mResources.getBoolean(C0003R$bool.doze_double_tap_reports_touch_coordinates);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        this.mDozeAlwaysOn = this.mAmbientDisplayConfiguration.alwaysOnEnabled(-2);
    }

    public AlwaysOnDisplayPolicy getPolicy() {
        return this.mAlwaysOnPolicy;
    }
}
