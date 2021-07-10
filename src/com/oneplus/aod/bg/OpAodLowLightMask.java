package com.oneplus.aod.bg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
public class OpAodLowLightMask extends View {
    private MaskGenerator mMaskGenerator;
    private Point mSize;

    public OpAodLowLightMask(Context context) {
        this(context, null);
    }

    public OpAodLowLightMask(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpAodLowLightMask(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Display display = ((View) this).mContext.getDisplay();
        Point point = new Point();
        this.mSize = point;
        display.getRealSize(point);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (view == this) {
            Log.d("OpAodLowLightMask", "onVisibilityChanged: " + i);
        }
    }

    public void createMask() {
        stopThreadIfNeeded();
        MaskGenerator maskGenerator = new MaskGenerator(this, this.mSize);
        this.mMaskGenerator = maskGenerator;
        maskGenerator.start();
    }

    public void release() {
        stopThreadIfNeeded();
        setBackground(null);
    }

    private void stopThreadIfNeeded() {
        MaskGenerator maskGenerator = this.mMaskGenerator;
        if (maskGenerator != null && maskGenerator.isAlive() && !this.mMaskGenerator.isInterrupted()) {
            Log.d("OpAodLowLightMask", "stopGenerator");
            this.mMaskGenerator.interrupt();
            this.mMaskGenerator = null;
        }
    }

    /* access modifiers changed from: private */
    public static class MaskGenerator extends Thread {
        private OpAodLowLightMask mMaskView;

        public MaskGenerator(OpAodLowLightMask opAodLowLightMask, Point point) {
            this.mMaskView = opAodLowLightMask;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            final Bitmap createBitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
            createBitmap.eraseColor(-16777216);
            createBitmap.setPixel(0, 0, 0);
            createBitmap.setPixel(1, 1, 0);
            if (Build.DEBUG_ONEPLUS) {
                Log.d("MaskGenerator", "MaskGenerator: total cost= " + (SystemClock.elapsedRealtime() - elapsedRealtime) + " ms");
            }
            this.mMaskView.post(new Runnable() { // from class: com.oneplus.aod.bg.OpAodLowLightMask.MaskGenerator.1
                @Override // java.lang.Runnable
                public void run() {
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(MaskGenerator.this.mMaskView.getResources(), createBitmap);
                    Shader.TileMode tileMode = Shader.TileMode.REPEAT;
                    bitmapDrawable.setTileModeXY(tileMode, tileMode);
                    MaskGenerator.this.mMaskView.setBackground(bitmapDrawable);
                }
            });
        }
    }
}
