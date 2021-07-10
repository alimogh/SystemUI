package com.oneplus.battery;

import android.view.MotionEvent;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.statusbar.policy.CallbackController;
public interface OpChargingAnimationController extends CallbackController<ChargingStateChangeCallback> {

    public interface ChargingStateChangeCallback {
        default void onWarpCharingAnimationEnd(int i) {
        }

        default void onWarpCharingAnimationStart(int i) {
        }
    }

    void animationEnd(int i);

    void animationStart(int i);

    void disPatchTouchEvent(MotionEvent motionEvent);

    void init(KeyguardViewMediator keyguardViewMediator);

    boolean isAnimationStarted();
}
