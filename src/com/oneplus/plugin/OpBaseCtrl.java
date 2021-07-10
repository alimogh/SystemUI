package com.oneplus.plugin;

import android.content.Context;
import android.graphics.Bitmap;
public abstract class OpBaseCtrl {
    protected Context mContext;
    private boolean mStarted = false;

    public interface ControlCallback {
    }

    public abstract void onFinishedGoingToSleep(int i);

    public abstract void onKeyguardBouncerChanged(boolean z);

    public abstract void onKeyguardVisibilityChanged(boolean z);

    public void onScreenTurnedOff() {
    }

    public abstract void onScreenTurnedOn();

    public abstract void onStartCtrl();

    public void onStartedGoingToSleep(int i) {
    }

    public abstract void onStartedWakingUp();

    public void onWallpaperChange(Bitmap bitmap) {
    }

    public void setCallback(ControlCallback controlCallback) {
    }

    public void startCtrl() {
        this.mStarted = true;
        onStartCtrl();
    }

    public boolean isEnable() {
        return this.mStarted;
    }

    public void init(Context context) {
        this.mContext = context;
    }
}
