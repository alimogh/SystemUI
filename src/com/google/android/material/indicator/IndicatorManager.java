package com.google.android.material.indicator;

import com.google.android.material.indicator.animation.AnimationManager;
import com.google.android.material.indicator.animation.controller.ValueController;
import com.google.android.material.indicator.animation.data.Value;
import com.google.android.material.indicator.draw.DrawManager;
import com.google.android.material.indicator.draw.data.Indicator;
public class IndicatorManager implements ValueController.UpdateListener {
    private AnimationManager animationManager;
    private DrawManager drawManager;
    private Listener listener;

    interface Listener {
        void onIndicatorUpdated();
    }

    IndicatorManager(Listener listener) {
        this.listener = listener;
        DrawManager drawManager = new DrawManager();
        this.drawManager = drawManager;
        this.animationManager = new AnimationManager(drawManager.indicator(), this);
    }

    public AnimationManager animate() {
        return this.animationManager;
    }

    public Indicator indicator() {
        return this.drawManager.indicator();
    }

    public DrawManager drawer() {
        return this.drawManager;
    }

    @Override // com.google.android.material.indicator.animation.controller.ValueController.UpdateListener
    public void onValueUpdated(Value value) {
        this.drawManager.updateValue(value);
        Listener listener = this.listener;
        if (listener != null) {
            listener.onIndicatorUpdated();
        }
    }
}
