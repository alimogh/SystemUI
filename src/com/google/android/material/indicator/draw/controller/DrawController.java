package com.google.android.material.indicator.draw.controller;

import android.graphics.Canvas;
import android.view.MotionEvent;
import com.google.android.material.indicator.animation.data.Value;
import com.google.android.material.indicator.draw.data.Indicator;
import com.google.android.material.indicator.draw.drawer.Drawer;
import com.google.android.material.indicator.utils.CoordinatesUtils;
public class DrawController {
    private Drawer drawer;
    private Indicator indicator;
    private ClickListener listener;
    private Value value;

    public interface ClickListener {
        void onIndicatorClicked(int i);
    }

    public DrawController(Indicator indicator) {
        this.indicator = indicator;
        this.drawer = new Drawer(indicator);
    }

    public void updateValue(Value value) {
        this.value = value;
    }

    public void touch(MotionEvent motionEvent) {
        if (motionEvent != null && motionEvent.getAction() == 1) {
            onIndicatorTouched(motionEvent.getX(), motionEvent.getY());
        }
    }

    private void onIndicatorTouched(float f, float f2) {
        int position;
        if (this.listener != null && (position = CoordinatesUtils.getPosition(this.indicator, f, f2)) >= 0) {
            this.listener.onIndicatorClicked(position);
        }
    }

    public void draw(Canvas canvas) {
        int count = this.indicator.getCount();
        for (int i = 0; i < count; i++) {
            drawIndicator(canvas, i, CoordinatesUtils.getXCoordinate(this.indicator, i), CoordinatesUtils.getYCoordinate(this.indicator, i));
        }
    }

    private void drawIndicator(Canvas canvas, int i, int i2, int i3) {
        boolean isInteractiveAnimation = this.indicator.isInteractiveAnimation();
        int selectedPosition = this.indicator.getSelectedPosition();
        int selectingPosition = this.indicator.getSelectingPosition();
        boolean z = true;
        boolean z2 = !isInteractiveAnimation && (i == selectedPosition || i == this.indicator.getLastSelectedPosition());
        if (!isInteractiveAnimation || !(i == selectedPosition || i == selectingPosition)) {
            z = false;
        }
        boolean z3 = z2 | z;
        this.drawer.setup(i, i2, i3);
        Value value = this.value;
        if (value == null || !z3) {
            this.drawer.drawBasic(canvas, z3);
        } else {
            this.drawer.drawWorm(canvas, value);
        }
    }
}
