package com.oneplus.keyguard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.assist.ui.DisplayUtils;
import com.oneplus.util.OpUtils;
public class OpEmergencyBubble extends FrameLayout {
    public static int ACTIVE_CIRCLE_COLOR = -1376216;
    public static int ACTIVE_TEXT_COLOR = -1;
    public static int DEFAULT_CIRCLE_COLOR = -1;
    private int mCircleAlpha;
    private int mCircleColor;
    private float mCircleRadius;
    private Context mContext;
    private Paint mPaint;
    private TextView mText;
    private int mTextColor;

    public OpEmergencyBubble(Context context) {
        this(context, null);
    }

    public OpEmergencyBubble(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpEmergencyBubble(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCircleColor = DEFAULT_CIRCLE_COLOR;
        this.mCircleAlpha = 255;
        this.mTextColor = -1376216;
        this.mContext = context;
        init(context);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mText = (TextView) findViewById(C0008R$id.sos);
    }

    private void init(Context context) {
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setAntiAlias(true);
        if (DisplayUtils.getWidth(this.mContext) > 1080) {
            this.mCircleRadius = (float) (OpUtils.convertPxByResolutionProportion((float) this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_bubble_diameter), 1080) / 2);
        } else {
            this.mCircleRadius = (float) (this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_emergency_bubble_diameter) / 2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        drawWhiteCircle(canvas);
        drawBackgroundCircle(canvas);
        drawText();
        super.onDraw(canvas);
    }

    private void drawText() {
        TextView textView = this.mText;
        if (textView != null) {
            textView.setTextColor(this.mTextColor);
        }
    }

    private void drawWhiteCircle(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(DEFAULT_CIRCLE_COLOR);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        float f = this.mCircleRadius;
        canvas.drawCircle(f, f, f, paint);
    }

    private void drawBackgroundCircle(Canvas canvas) {
        this.mPaint.setColor(this.mCircleColor);
        this.mPaint.setAlpha(this.mCircleAlpha);
        this.mPaint.setStyle(Paint.Style.FILL);
        float f = this.mCircleRadius;
        canvas.drawCircle(f, f, f, this.mPaint);
    }

    public void drawView(int i, int i2, int i3, int i4, int i5) {
        this.mCircleColor = i;
        this.mCircleAlpha = i2;
        this.mTextColor = i4;
        invalidate();
    }

    public void reset() {
        this.mCircleColor = DEFAULT_CIRCLE_COLOR;
        this.mCircleAlpha = 255;
        this.mTextColor = -1376216;
        invalidate();
    }
}
