package com.oneplus.aod.bg;

import android.animation.Animator;
import android.graphics.Canvas;
import android.view.View;
import java.util.ArrayList;
public interface IBgPaint {
    default void burnInProtect() {
    }

    void draw(Canvas canvas);

    ArrayList<Animator> genAodDisappearAnimation();

    default void onAttachedToWindow() {
    }

    default void onDetachedFromWindow() {
    }

    void onSizeChanged(int i, int i2);

    default void recover() {
    }

    void release();

    void reset();

    void setup(View view);

    default void userActivityInAlwaysOn() {
    }
}
