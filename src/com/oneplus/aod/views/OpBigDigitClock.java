package com.oneplus.aod.views;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.R;
import com.android.systemui.R$styleable;
import com.oneplus.aod.utils.OpAodDimenHelper;
import com.oneplus.aod.utils.OpAodSettings;
import java.util.Calendar;
public class OpBigDigitClock extends TextView implements IOpAodClock {
    private int mFontSizeId;
    private CharSequence mFormat12;
    private CharSequence mFormat24;
    private int mGradientEndColor;
    private int mGradientStartColor;
    private int mGradientStyle;
    private int mSubBottomId;

    public OpBigDigitClock(Context context) {
        this(context, null);
    }

    public OpBigDigitClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpBigDigitClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mGradientStyle = 0;
        this.mGradientStartColor = -1;
        this.mGradientEndColor = -1;
        this.mFontSizeId = -1;
        this.mSubBottomId = -1;
        setupAttributes(attributeSet);
        overrideBySystemProperties();
        setLayoutParams(new FrameLayout.LayoutParams(context, attributeSet));
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        super.onLayout(z, i, i2, i3, i4);
        if (z) {
            int i6 = this.mGradientStyle;
            int i7 = 0;
            if (i6 == 0) {
                i5 = getHeight();
            } else if (i6 == 1) {
                i7 = getWidth();
                i5 = 0;
            } else {
                i5 = 0;
            }
            getPaint().setShader(new LinearGradient(0.0f, 0.0f, (float) i7, (float) i5, this.mGradientStartColor, this.mGradientEndColor, Shader.TileMode.CLAMP));
        }
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void applyLayoutParams(OpAodSettings.OpViewInfo opViewInfo) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        if (layoutParams != null) {
            Paint.FontMetrics fontMetrics = getPaint().getFontMetrics();
            float f = fontMetrics.descent - fontMetrics.ascent;
            int i = this.mSubBottomId;
            if (i != -1) {
                layoutParams.height = (int) (f - ((float) OpAodDimenHelper.convertDpToFixedPx(((TextView) this).mContext, i)));
            }
            if (opViewInfo != null) {
                setTextSize(0, (float) opViewInfo.getSize(((TextView) this).mContext, this.mFontSizeId));
                layoutParams.setMarginStart(opViewInfo.getMarginStart(((TextView) this).mContext));
                layoutParams.setMarginEnd(opViewInfo.getMarginEnd(((TextView) this).mContext));
                layoutParams.topMargin = opViewInfo.getMarginTop(((TextView) this).mContext);
                layoutParams.bottomMargin = opViewInfo.getMarginEnd(((TextView) this).mContext);
            }
        }
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onTimeChanged(Calendar calendar) {
        setText(DateFormat.format(DateFormat.is24HourFormat(getContext(), ActivityManager.getCurrentUser()) ? this.mFormat24 : this.mFormat12, calendar.getTime()).toString());
    }

    private void setupAttributes(AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = ((TextView) this).mContext.obtainStyledAttributes(attributeSet, R$styleable.OpBigDigitClock, 0, 0);
        this.mGradientStyle = obtainStyledAttributes.getInt(R$styleable.OpBigDigitClock_gradientStyle, this.mGradientStyle);
        this.mGradientStartColor = obtainStyledAttributes.getColor(R$styleable.OpBigDigitClock_gradientStartColor, this.mGradientStartColor);
        this.mGradientEndColor = obtainStyledAttributes.getColor(R$styleable.OpBigDigitClock_gradientEndColor, this.mGradientEndColor);
        this.mSubBottomId = obtainStyledAttributes.getResourceId(R$styleable.OpBigDigitClock_subBottom, this.mSubBottomId);
        obtainStyledAttributes.recycle();
        TypedArray obtainStyledAttributes2 = ((TextView) this).mContext.obtainStyledAttributes(attributeSet, R.styleable.TextAppearance, 0, 0);
        this.mFontSizeId = obtainStyledAttributes2.getResourceId(0, -1);
        obtainStyledAttributes2.recycle();
        TypedArray obtainStyledAttributes3 = ((TextView) this).mContext.obtainStyledAttributes(attributeSet, R.styleable.TextClock, 0, 0);
        this.mFormat12 = obtainStyledAttributes3.getText(0);
        this.mFormat24 = obtainStyledAttributes3.getText(1);
        obtainStyledAttributes3.recycle();
    }

    private void overrideBySystemProperties() {
        String[] split;
        String str = SystemProperties.get("sys.aod.gradient.color", "");
        if (!TextUtils.isEmpty(str) && (split = str.trim().split(",")) != null && split.length > 1) {
            try {
                this.mGradientStartColor = Color.parseColor("#" + split[0]);
                this.mGradientEndColor = Color.parseColor("#" + split[1]);
            } catch (IllegalArgumentException e) {
                Log.e("OpBigDigitClock", "parseColor occur exception", e);
            }
        }
    }
}
