package com.oneplus.keyguard;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.util.OpUtils;
public class OpSimpleLineView extends LinearLayout implements ConfigurationController.ConfigurationListener {
    private Context mContext;
    private boolean mIsRTL;
    private int mLinePaddingStart;
    private int mLinePaddingStartId;
    private int mLinePaddingY;
    private Paint mLinePaint;
    private int mLineWidth;

    public OpSimpleLineView(Context context) {
        super(context);
    }

    /* JADX INFO: finally extract failed */
    public OpSimpleLineView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Log.i("OpSimpleLineView", "OpSimpleLineView constructor");
        this.mContext = context;
        int convertDpToFixedPx = OpUtils.convertDpToFixedPx(context.getResources().getDimension(C0005R$dimen.op_keyguard_clock_info_view_content_draw_smart_space_line_padding));
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.OpSimpleLineView, 0, 0);
        try {
            this.mLinePaddingStartId = obtainStyledAttributes.getResourceId(R$styleable.OpSimpleLineView_linePaddingStart, -1);
            obtainStyledAttributes.recycle();
            this.mLinePaddingStart = convertDpToFixedPx(this.mContext, this.mLinePaddingStartId, convertDpToFixedPx);
            Log.i("OpSimpleLineView", "mLinePaddingStart:" + this.mLinePaddingStart + ", mLinePaddingStartId:" + this.mLinePaddingStartId);
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        Paint paint = new Paint();
        this.mLinePaint = paint;
        paint.setColor(-3355444);
        boolean z = true;
        this.mLinePaint.setAntiAlias(true);
        this.mLineWidth = OpUtils.convertDpToFixedPx(this.mContext.getResources().getDimension(C0005R$dimen.op_keyguard_clock_info_view_smart_space_line));
        this.mLinePaddingY = OpUtils.convertDpToFixedPx(this.mContext.getResources().getDimension(C0005R$dimen.op_keyguard_clock_info_view_smart_space_line_y_padding));
        setWillNotDraw(false);
        if (getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        this.mIsRTL = z;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        boolean z = true;
        if (getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        this.mIsRTL = z;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
    }

    @Override // android.widget.LinearLayout, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.mIsRTL ? (getWidth() - this.mLineWidth) - this.mLinePaddingStart : this.mLinePaddingStart;
        canvas.drawRect((float) width, (float) this.mLinePaddingY, (float) (width + this.mLineWidth), (float) (getHeight() - this.mLinePaddingY), this.mLinePaint);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        boolean z = true;
        if (getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        this.mIsRTL = z;
        Log.i("OpSimpleLineView", "onConfigChanged, mIsRTL:" + this.mIsRTL + ", newConfig:" + configuration);
        invalidate();
    }

    public static int convertDpToFixedPx(Context context, int i, int i2) {
        return i != -1 ? OpUtils.convertDpToFixedPx(context.getResources().getDimension(i)) : i2;
    }
}
