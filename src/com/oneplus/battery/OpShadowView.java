package com.oneplus.battery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;
public class OpShadowView extends ImageView {
    private Paint mShadowPaint;

    public OpShadowView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(new RectF(0.0f, 0.0f, (float) getWidth(), (float) getHeight()), 0.0f, 0.0f, this.mShadowPaint);
    }

    public void setShadowLayer(float f, float f2, float f3, int i, int i2) {
        Paint paint = new Paint();
        this.mShadowPaint = paint;
        paint.setColor(i);
        this.mShadowPaint.setShadowLayer(f, f2, f3, i2);
        invalidate();
    }
}
