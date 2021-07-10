package com.oneplus.aod.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontFamily;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineHeightSpan;
import android.text.style.TypefaceSpan;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.R;
import com.android.systemui.C0001R$array;
import com.android.systemui.R$styleable;
import com.oneplus.aod.utils.OpAodDimenHelper;
import com.oneplus.aod.utils.OpAodSettings;
import java.io.IOException;
import java.util.Calendar;
public class OpTypoGraphicClock extends TextView implements IOpAodClock {
    private int[] mColors;
    private int mFontFamily2Id;
    private int mFontSizeId;
    private final String[] mHours;
    private int[] mLineHeights;
    private final String[] mMinutes;
    private int mTextClockStringTemplate;

    public OpTypoGraphicClock(Context context) {
        this(context, null);
    }

    public OpTypoGraphicClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpTypoGraphicClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTextClockStringTemplate = -1;
        this.mFontSizeId = -1;
        this.mFontFamily2Id = -1;
        setupAttributes(attributeSet);
        this.mHours = context.getResources().getStringArray(C0001R$array.type_clock_hours);
        this.mMinutes = context.getResources().getStringArray(C0001R$array.type_clock_minutes);
        setLayoutParams(new FrameLayout.LayoutParams(context, attributeSet));
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void applyLayoutParams(OpAodSettings.OpViewInfo opViewInfo) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        if (layoutParams != null && opViewInfo != null) {
            layoutParams.setMarginStart(opViewInfo.getMarginStart(((TextView) this).mContext));
            layoutParams.setMarginEnd(opViewInfo.getMarginEnd(((TextView) this).mContext));
            layoutParams.topMargin = opViewInfo.getMarginTop(((TextView) this).mContext);
            layoutParams.bottomMargin = opViewInfo.getMarginEnd(((TextView) this).mContext);
            setTextSize(0, (float) opViewInfo.getSize(((TextView) this).mContext, this.mFontSizeId));
        }
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onTimeChanged(Calendar calendar) {
        int[] iArr;
        int i = calendar.get(10) % 12;
        int i2 = calendar.get(12) % 60;
        SpannableString spannableString = new SpannableString(getContext().getText(this.mTextClockStringTemplate));
        Annotation[] annotationArr = (Annotation[]) spannableString.getSpans(0, spannableString.length(), Annotation.class);
        int i3 = 0;
        int i4 = 0;
        for (Annotation annotation : annotationArr) {
            String value = annotation.getValue();
            if ("color".equals(value)) {
                int[] iArr2 = this.mColors;
                if (iArr2 != null && i3 < iArr2.length) {
                    spannableString.setSpan(new ForegroundColorSpan(this.mColors[i3]), spannableString.getSpanStart(annotation), spannableString.getSpanEnd(annotation), 33);
                    i3++;
                }
            } else if ("bold".equals(value)) {
                try {
                    spannableString.setSpan(new TypefaceSpan(new Typeface.CustomFallbackBuilder(new FontFamily.Builder(new Font.Builder(((TextView) this).mContext.getResources(), this.mFontFamily2Id).build()).build()).build()), spannableString.getSpanStart(annotation), spannableString.getSpanEnd(annotation), 33);
                } catch (IOException unused) {
                }
            } else if ("line-height".equals(value) && (iArr = this.mLineHeights) != null && i4 < iArr.length) {
                Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
                spannableString.setSpan(new LineHeightSpan.Standard((int) (((fontMetrics.descent - fontMetrics.ascent) + ((float) OpAodDimenHelper.convertDpToFixedPx(((TextView) this).mContext, this.mLineHeights[i4]))) - (fontMetrics.ascent - fontMetrics.top))), spannableString.getSpanStart(annotation), spannableString.getSpanEnd(annotation), 33);
                i4++;
            }
        }
        setText(TextUtils.expandTemplate(spannableString, this.mHours[i], this.mMinutes[i2]));
    }

    private void setupAttributes(AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = ((TextView) this).mContext.obtainStyledAttributes(attributeSet, R$styleable.OpTypoGraphicClock, 0, 0);
        readColors(obtainStyledAttributes.getResourceId(R$styleable.OpTypoGraphicClock_colors, -1));
        readLineHeights(obtainStyledAttributes.getResourceId(R$styleable.OpTypoGraphicClock_lineHeights, -1));
        this.mTextClockStringTemplate = obtainStyledAttributes.getResourceId(R$styleable.OpTypoGraphicClock_textClockStringTemplate, -1);
        this.mFontFamily2Id = obtainStyledAttributes.getResourceId(R$styleable.OpTypoGraphicClock_textFont2, -1);
        obtainStyledAttributes.recycle();
        TypedArray obtainStyledAttributes2 = ((TextView) this).mContext.obtainStyledAttributes(attributeSet, R.styleable.TextAppearance, 0, 0);
        this.mFontSizeId = obtainStyledAttributes2.getResourceId(0, -1);
        obtainStyledAttributes2.recycle();
    }

    private void readColors(int i) {
        if (i != -1) {
            TypedArray obtainTypedArray = getContext().getResources().obtainTypedArray(i);
            this.mColors = new int[obtainTypedArray.length()];
            for (int i2 = 0; i2 < obtainTypedArray.length(); i2++) {
                this.mColors[i2] = obtainTypedArray.getColor(i2, 0);
            }
            obtainTypedArray.recycle();
        }
    }

    private void readLineHeights(int i) {
        if (i != -1) {
            TypedArray obtainTypedArray = getContext().getResources().obtainTypedArray(i);
            this.mLineHeights = new int[obtainTypedArray.length()];
            for (int i2 = 0; i2 < obtainTypedArray.length(); i2++) {
                this.mLineHeights[i2] = obtainTypedArray.getResourceId(i2, 0);
            }
            obtainTypedArray.recycle();
        }
    }
}
