package com.oneplus.aod.slice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.util.OpUtils;
public class OpSliceContainer extends LinearLayout {
    private boolean mEllipsize;
    private View mIcon;
    private boolean mIsRTL;
    private int mLinePaddingY;
    private Paint mLinePaint;
    private int mLineWidth;
    private TextView mPrimary;
    private TextView mRemark;
    private TextView mSecondary;
    private int mUserId;

    public OpSliceContainer(Context context) {
        this(context, null);
    }

    public OpSliceContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpSliceContainer(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public OpSliceContainer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mEllipsize = true;
        Paint paint = new Paint();
        this.mLinePaint = paint;
        paint.setColor(-1);
        this.mLinePaint.setAntiAlias(true);
        this.mLineWidth = OpUtils.convertDpToFixedPx(((LinearLayout) this).mContext.getResources().getDimension(C0005R$dimen.op_keyguard_clock_info_view_smart_space_line));
        this.mLinePaddingY = OpUtils.convertDpToFixedPx(((LinearLayout) this).mContext.getResources().getDimension(C0005R$dimen.op_keyguard_clock_info_view_smart_space_line_y_padding));
        setWillNotDraw(false);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIcon = findViewById(C0008R$id.slice_icon);
        this.mPrimary = (TextView) findViewById(C0008R$id.slice_primary);
        this.mRemark = (TextView) findViewById(C0008R$id.slice_remark);
        this.mSecondary = (TextView) findViewById(C0008R$id.slice_secondary);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        boolean z = true;
        if (getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        this.mIsRTL = z;
        updateLayout();
        updateTextSize();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int measuredWidth = this.mIcon.getMeasuredWidth();
        TextPaint paint = this.mPrimary.getPaint();
        float measureText = paint.measureText(this.mPrimary.getText().toString());
        float measureText2 = paint.measureText(this.mRemark.getText().toString());
        if (!OpAodUtils.isDefaultOrRedAodClockStyle(((LinearLayout) this).mContext, this.mUserId)) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mIcon.getLayoutParams();
            float marginStart = ((float) marginLayoutParams.getMarginStart()) + 0.0f + ((float) measuredWidth) + ((float) marginLayoutParams.getMarginEnd());
            ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mPrimary.getLayoutParams();
            float marginStart2 = marginStart + ((float) marginLayoutParams2.getMarginStart()) + measureText + ((float) marginLayoutParams2.getMarginEnd());
            ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) this.mRemark.getLayoutParams();
            updateLayoutParams(((marginStart2 + ((float) marginLayoutParams3.getMarginStart())) + measureText2) + ((float) marginLayoutParams3.getMarginEnd()) > ((float) getMeasuredWidth()));
        }
    }

    @Override // android.widget.LinearLayout, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.mIsRTL ? getWidth() - this.mLineWidth : 0;
        canvas.drawRect((float) width, (float) this.mLinePaddingY, (float) (width + this.mLineWidth), (float) (getHeight() - this.mLinePaddingY), this.mLinePaint);
    }

    private void updateLayoutParams(boolean z) {
        if (this.mEllipsize != z) {
            this.mEllipsize = z;
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mPrimary.getLayoutParams();
            if (z) {
                layoutParams.width = 0;
                layoutParams.weight = 1.0f;
                return;
            }
            layoutParams.width = -2;
            layoutParams.weight = 0.0f;
        }
    }

    private void updateTextSize() {
        float f = getResources().getDisplayMetrics().scaledDensity;
        float convertDpToFixedPx2 = (float) OpUtils.convertDpToFixedPx2(getResources().getDimension(C0005R$dimen.aod_slice_text_size_primary));
        this.mPrimary.setTextSize(0, convertDpToFixedPx2);
        this.mRemark.setTextSize(0, convertDpToFixedPx2);
        this.mSecondary.setTextSize(0, (float) OpUtils.convertDpToFixedPx2(getResources().getDimension(C0005R$dimen.aod_slice_text_size_secondary)));
    }

    private void updateLayout() {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mSecondary.getLayoutParams();
        marginLayoutParams.topMargin = OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.aod_slice_layout_primary_margin_bottom));
        this.mSecondary.setLayoutParams(marginLayoutParams);
        int convertDpToFixedPx = OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.op_control_icon_size_list));
        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mIcon.getLayoutParams();
        marginLayoutParams2.width = convertDpToFixedPx;
        marginLayoutParams2.height = convertDpToFixedPx;
        this.mIcon.setLayoutParams(marginLayoutParams2);
        ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) this.mPrimary.getLayoutParams();
        marginLayoutParams3.setMarginStart(OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.op_control_margin_space1)));
        TextView textView = this.mPrimary;
        textView.setPaddingRelative(textView.getPaddingStart(), this.mPrimary.getPaddingTop(), OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.aod_slice_view_primary_padding_end)), this.mPrimary.getPaddingBottom());
        this.mPrimary.setLayoutParams(marginLayoutParams3);
        ViewGroup.MarginLayoutParams marginLayoutParams4 = (ViewGroup.MarginLayoutParams) this.mRemark.getLayoutParams();
        marginLayoutParams4.setMarginStart(OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.op_control_margin_space1)));
        this.mRemark.setLayoutParams(marginLayoutParams4);
        setPaddingRelative(OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.op_control_margin_space2)) + this.mLineWidth, getPaddingTop(), getPaddingEnd(), getPaddingBottom());
    }

    public void setUserId(int i) {
        this.mUserId = i;
    }
}
