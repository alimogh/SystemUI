package com.oneplus.aod.bg;

import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
public abstract class OpBasePaint implements IBgPaint {
    protected int mHeight;
    protected String mTag = getClass().getSimpleName();
    protected View mView;
    protected int mWidth;

    /* access modifiers changed from: protected */
    public abstract void onDraw(Canvas canvas);

    @Override // com.oneplus.aod.bg.IBgPaint
    public void onSizeChanged(int i, int i2) {
        this.mWidth = i;
        this.mHeight = i2;
    }

    @Override // com.oneplus.aod.bg.IBgPaint
    public void setup(View view) {
        this.mView = view;
    }

    @Override // com.oneplus.aod.bg.IBgPaint
    public void reset() {
        Log.i(this.mTag, "reset");
    }

    @Override // com.oneplus.aod.bg.IBgPaint
    public void draw(Canvas canvas) {
        if (this.mView != null) {
            onDraw(canvas);
        } else {
            Log.e(this.mTag, "draw: mView is null!!!");
        }
    }
}
