package com.android.systemui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import com.android.settingslib.Utils;
public class HardwareBgDrawable extends LayerDrawable {
    private final Drawable[] mLayers;
    private final Paint mPaint;
    private int mPoint;
    private boolean mRotatedBackground;
    private final boolean mRoundTop;

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public int getOpacity() {
        return -1;
    }

    public HardwareBgDrawable(boolean z, boolean z2, Context context) {
        this(z, getLayers(context, z, z2));
    }

    public HardwareBgDrawable(boolean z, Drawable[] drawableArr) {
        super(drawableArr);
        this.mPaint = new Paint();
        if (drawableArr.length == 2) {
            this.mRoundTop = z;
            this.mLayers = drawableArr;
            return;
        }
        throw new IllegalArgumentException("Need 2 layers");
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x003e: APUT  
      (r5v2 android.graphics.drawable.Drawable[])
      (1 ??[boolean, int, float, short, byte, char])
      (wrap: android.graphics.drawable.Drawable : 0x003a: INVOKE  (r6v4 android.graphics.drawable.Drawable) = 
      (wrap: android.graphics.drawable.Drawable : 0x0036: INVOKE  (r6v3 android.graphics.drawable.Drawable) = (r4v0 android.content.Context), (r6v2 int) type: VIRTUAL call: android.content.Context.getDrawable(int):android.graphics.drawable.Drawable)
     type: VIRTUAL call: android.graphics.drawable.Drawable.mutate():android.graphics.drawable.Drawable)
     */
    private static Drawable[] getLayers(Context context, boolean z, boolean z2) {
        Drawable[] drawableArr;
        int i;
        int i2 = z2 ? C0006R$drawable.rounded_bg_full : C0006R$drawable.rounded_bg;
        if (z) {
            drawableArr = new Drawable[]{context.getDrawable(i2).mutate(), context.getDrawable(i2).mutate()};
        } else {
            drawableArr = new Drawable[2];
            drawableArr[0] = context.getDrawable(i2).mutate();
            if (z2) {
                i = C0006R$drawable.rounded_full_bg_bottom;
            } else {
                i = C0006R$drawable.rounded_bg_bottom;
            }
            drawableArr[1] = context.getDrawable(i).mutate();
        }
        drawableArr[1].setTintList(Utils.getColorAttr(context, 16843827));
        return drawableArr;
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (this.mPoint < 0 || this.mRotatedBackground) {
            this.mLayers[0].draw(canvas);
            return;
        }
        Rect bounds = getBounds();
        int i = bounds.top + this.mPoint;
        int i2 = bounds.bottom;
        if (i > i2) {
            i = i2;
        }
        if (this.mRoundTop) {
            this.mLayers[0].setBounds(bounds.left, bounds.top, bounds.right, i);
        } else {
            this.mLayers[1].setBounds(bounds.left, i, bounds.right, bounds.bottom);
        }
        if (this.mRoundTop) {
            this.mLayers[1].draw(canvas);
            this.mLayers[0].draw(canvas);
            return;
        }
        this.mLayers[0].draw(canvas);
        this.mLayers[1].draw(canvas);
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mPaint.setAlpha(i);
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }
}
