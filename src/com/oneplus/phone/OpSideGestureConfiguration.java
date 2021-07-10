package com.oneplus.phone;

import android.content.res.Resources;
import android.graphics.Point;
import android.os.SystemProperties;
import android.view.Display;
public class OpSideGestureConfiguration {
    public static final float PORTRAIT_NON_DETECT_SCALE = (((float) SystemProperties.getInt("persist.portrait_non.detect.scale", 0)) * 0.1f);
    private static int sScreenHeight;
    private static int sScreenWidth;

    static {
        SystemProperties.getInt("persist.gesture_button.corner", 11);
    }

    public OpSideGestureConfiguration(Display display) {
        Point point = new Point();
        display.getRealSize(point);
        sScreenWidth = Math.min(point.x, point.y);
        sScreenHeight = Math.max(point.x, point.y);
    }

    public static int getWindowHeight(int i) {
        return Math.min(getScreenHeight(i), (int) ((((float) getScreenHeight(i)) * (1.0f - PORTRAIT_NON_DETECT_SCALE)) + ((float) getAnimRadius())));
    }

    public static int getWindowWidth() {
        return convertDpToPixel(38.6f);
    }

    public static int getAnimRadius() {
        return convertDpToPixel(104.5f);
    }

    public static int getBezierControlOffset1() {
        return convertDpToPixel(56.0f);
    }

    public static int getBezierControlOffsetSwitch2() {
        return convertDpToPixel(39.0f);
    }

    public static int getAnimSwitchDistance() {
        return convertDpToPixel(38.6f);
    }

    public static int getAnimCancelDistance() {
        return convertDpToPixel(8.8f);
    }

    private static int convertDpToPixel(float f) {
        return (int) (f * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight(int i) {
        if (i == 0 || i == 2) {
            return sScreenHeight;
        }
        if (i == 1 || i == 3) {
            return sScreenWidth;
        }
        return sScreenHeight;
    }

    public static int getScreenWidth(int i) {
        if (i == 0 || i == 2) {
            return sScreenWidth;
        }
        if (i == 1 || i == 3) {
            return sScreenHeight;
        }
        return sScreenWidth;
    }

    public static int boundToRange(int i, int i2, int i3) {
        return Math.max(i2, Math.min(i, i3));
    }

    public static int getIconSize() {
        return convertDpToPixel(13.0f);
    }
}
