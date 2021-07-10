package com.oneplus.aod.views.parsons;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.systemui.C0016R$style;
import com.android.systemui.R$styleable;
import com.oneplus.aod.utils.OpAodDimenHelper;
import com.oneplus.util.OpUtils;
public class OpParsonsUnlockLabel extends TextView {
    private boolean mIsRTL;
    private int mUnderlineHeightId;
    private int mUnlockPaddingId;

    public OpParsonsUnlockLabel(Context context) {
        this(context, null);
    }

    public OpParsonsUnlockLabel(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpParsonsUnlockLabel(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setupAttributes(attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        boolean z = true;
        if (((TextView) this).mContext.getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        this.mIsRTL = z;
    }

    public void updateResource() {
        TypedArray obtainStyledAttributes = ((TextView) this).mContext.obtainStyledAttributes(C0016R$style.op_parsons_unlock_msg2, new int[]{16842901});
        setTextSize(0, (float) OpUtils.convertDpToFixedPx2(obtainStyledAttributes.getDimension(0, 0.0f)));
        obtainStyledAttributes.recycle();
    }

    public void setUnlockMsg(int i, int i2) {
        int convertDpToFixedPx2 = OpAodDimenHelper.convertDpToFixedPx2(((TextView) this).mContext, this.mUnlockPaddingId);
        int dimensionPixelSize = ((TextView) this).mContext.getResources().getDimensionPixelSize(this.mUnderlineHeightId);
        SpannableString spannableString = new SpannableString(((TextView) this).mContext.getText(i));
        Annotation[] annotationArr = (Annotation[]) spannableString.getSpans(0, spannableString.length(), Annotation.class);
        if (annotationArr != null && annotationArr.length == 1) {
            Annotation annotation = annotationArr[0];
            if ("unlockNum".equals(annotation.getValue())) {
                int spanStart = spannableString.getSpanStart(annotation);
                spannableString.setSpan(new UnderlineSpan((spanStart != 0 || this.mIsRTL) ? convertDpToFixedPx2 : 0, convertDpToFixedPx2, dimensionPixelSize), spanStart, spannableString.getSpanEnd(annotation), 33);
            }
        }
        setText(TextUtils.expandTemplate(spannableString, Integer.toString(i2)));
    }

    private void setupAttributes(AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = ((TextView) this).mContext.obtainStyledAttributes(attributeSet, R$styleable.OpParsonsUnlockLabel, 0, 0);
        this.mUnderlineHeightId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsUnlockLabel_underlineHeight, -1);
        this.mUnlockPaddingId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsUnlockLabel_unlockPadding, -1);
        obtainStyledAttributes.recycle();
    }

    private static class UnderlineSpan extends ReplacementSpan {
        private int mLeftPadding;
        private int mLineHeight;
        private int mRightPadding;

        public UnderlineSpan(int i, int i2, int i3) {
            this.mLeftPadding = i;
            this.mRightPadding = i2;
            this.mLineHeight = i3;
        }

        @Override // android.text.style.ReplacementSpan
        public void draw(Canvas canvas, CharSequence charSequence, int i, int i2, float f, int i3, int i4, int i5, Paint paint) {
            float measureText = paint.measureText(charSequence, i, i2);
            float round = (float) Math.round(f + ((float) this.mLeftPadding));
            canvas.drawText(charSequence, i, i2, round, (float) ((int) (((float) (canvas.getHeight() / 2)) - ((paint.descent() + paint.ascent()) / 2.0f))), paint);
            canvas.drawRect(round, (float) (i5 - this.mLineHeight), round + measureText, (float) i5, paint);
        }

        @Override // android.text.style.ReplacementSpan
        public int getSize(Paint paint, CharSequence charSequence, int i, int i2, Paint.FontMetricsInt fontMetricsInt) {
            return Math.round(paint.measureText(charSequence, i, i2) + ((float) this.mLeftPadding) + ((float) this.mRightPadding));
        }
    }
}
