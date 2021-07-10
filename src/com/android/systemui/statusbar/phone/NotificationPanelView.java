package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import com.oneplus.systemui.statusbar.phone.OpNotificationPanelView;
public class NotificationPanelView extends OpNotificationPanelView {
    private final Paint mAlphaPaint = new Paint();
    private int mCurrentPanelAlpha;
    private boolean mDozing;
    private RtlChangeListener mRtlChangeListener;

    /* access modifiers changed from: package-private */
    public interface RtlChangeListener {
        void onRtlPropertielsChanged(int i);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public NotificationPanelView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setWillNotDraw(true);
        this.mAlphaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        setBackgroundColor(0);
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        RtlChangeListener rtlChangeListener = this.mRtlChangeListener;
        if (rtlChangeListener != null) {
            rtlChangeListener.onRtlPropertielsChanged(i);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mCurrentPanelAlpha != 255) {
            canvas.drawRect(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), this.mAlphaPaint);
        }
    }

    /* access modifiers changed from: package-private */
    public float getCurrentPanelAlpha() {
        return (float) this.mCurrentPanelAlpha;
    }

    /* access modifiers changed from: package-private */
    public void setPanelAlphaInternal(float f) {
        int i = (int) f;
        this.mCurrentPanelAlpha = i;
        this.mAlphaPaint.setARGB(i, 255, 255, 255);
        invalidate();
    }

    public void setDozing(boolean z) {
        this.mDozing = z;
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return !this.mDozing;
    }

    /* access modifiers changed from: package-private */
    public void setRtlChangeListener(RtlChangeListener rtlChangeListener) {
        this.mRtlChangeListener = rtlChangeListener;
    }
}
