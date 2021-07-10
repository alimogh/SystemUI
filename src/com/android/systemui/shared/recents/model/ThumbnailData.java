package com.android.systemui.shared.recents.model;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
public class ThumbnailData {
    private static Method sMethodGetScale;
    private static Method sMethodIsReducedResolution;
    public Bitmap thumbnail;

    public ThumbnailData(ActivityManager.TaskSnapshot taskSnapshot) {
        boolean z;
        try {
            this.thumbnail = Bitmap.wrapHardwareBuffer(taskSnapshot.getSnapshot(), taskSnapshot.getColorSpace());
        } catch (IllegalArgumentException e) {
            Log.w("ThumbnailData", e.getMessage());
        }
        new Rect(taskSnapshot.getContentInsets());
        taskSnapshot.getOrientation();
        try {
            taskSnapshot.getRotation();
        } catch (NoSuchMethodError unused) {
        }
        boolean z2 = true;
        try {
            taskSnapshot.isLowResolution();
            z = false;
        } catch (NoSuchMethodError unused2) {
            z = true;
        }
        if (z) {
            try {
                if (sMethodIsReducedResolution == null) {
                    sMethodIsReducedResolution = ActivityManager.TaskSnapshot.class.getMethod("isReducedResolution", new Class[0]);
                }
                ((Boolean) sMethodIsReducedResolution.invoke(taskSnapshot, new Object[0])).booleanValue();
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException unused3) {
            }
        }
        try {
            this.thumbnail.getWidth();
            int i = taskSnapshot.getTaskSize().x;
            z2 = false;
        } catch (NoSuchMethodError unused4) {
        }
        if (z2) {
            try {
                if (sMethodGetScale == null) {
                    sMethodGetScale = ActivityManager.TaskSnapshot.class.getMethod("getScale", new Class[0]);
                }
                ((Float) sMethodGetScale.invoke(taskSnapshot, new Object[0])).floatValue();
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException unused5) {
            }
        }
        taskSnapshot.isRealSnapshot();
        taskSnapshot.isTranslucent();
        taskSnapshot.getWindowingMode();
        taskSnapshot.getSystemUiVisibility();
        try {
            taskSnapshot.getId();
        } catch (NoSuchMethodError unused6) {
        }
    }
}
