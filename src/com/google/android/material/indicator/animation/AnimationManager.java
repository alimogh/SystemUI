package com.google.android.material.indicator.animation;

import com.google.android.material.indicator.animation.controller.AnimationController;
import com.google.android.material.indicator.animation.controller.ValueController;
import com.google.android.material.indicator.draw.data.Indicator;
public class AnimationManager {
    private AnimationController animationController;

    public AnimationManager(Indicator indicator, ValueController.UpdateListener updateListener) {
        this.animationController = new AnimationController(indicator, updateListener);
    }

    public void basic() {
        AnimationController animationController = this.animationController;
        if (animationController != null) {
            animationController.end();
            this.animationController.basic();
        }
    }

    public void interactive(float f) {
        AnimationController animationController = this.animationController;
        if (animationController != null) {
            animationController.interactive(f);
        }
    }

    public void end() {
        AnimationController animationController = this.animationController;
        if (animationController != null) {
            animationController.end();
        }
    }
}
