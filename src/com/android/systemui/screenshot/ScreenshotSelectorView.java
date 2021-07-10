package com.android.systemui.screenshot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
public class ScreenshotSelectorView extends View {
    private final Paint mPaintBackground;
    private final Paint mPaintSelection;
    private Rect mSelectionRect;

    public ScreenshotSelectorView(Context context) {
        this(context, null);
    }

    public ScreenshotSelectorView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Paint paint = new Paint(-16777216);
        this.mPaintBackground = paint;
        paint.setAlpha(160);
        Paint paint2 = new Paint(0);
        this.mPaintSelection = paint2;
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    public Rect getSelectionRect() {
        return this.mSelectionRect;
    }

    public void stopSelection() {
        this.mSelectionRect = null;
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        canvas.drawRect((float) ((View) this).mLeft, (float) ((View) this).mTop, (float) ((View) this).mRight, (float) ((View) this).mBottom, this.mPaintBackground);
        Rect rect = this.mSelectionRect;
        if (rect != null) {
            canvas.drawRect(rect, this.mPaintSelection);
        }
    }
}
