package com.google.android.material.indicator.animation.controller;

import com.google.android.material.indicator.animation.data.Value;
import com.google.android.material.indicator.animation.type.WormAnimation;
public class ValueController {
    private UpdateListener updateListener;
    private WormAnimation wormAnimation;

    public interface UpdateListener {
        void onValueUpdated(Value value);
    }

    public ValueController(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public WormAnimation worm() {
        if (this.wormAnimation == null) {
            this.wormAnimation = new WormAnimation(this.updateListener);
        }
        return this.wormAnimation;
    }
}
