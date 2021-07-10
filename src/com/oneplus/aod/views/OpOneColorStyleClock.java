package com.oneplus.aod.views;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.R;
import com.android.systemui.R$styleable;
import com.oneplus.aod.OpDateTimeView;
import com.oneplus.aod.utils.OpAodSettings;
import com.oneplus.util.OpUtils;
import java.util.Calendar;
public class OpOneColorStyleClock extends TextView implements IOpAodClock {
    private int mFontSizeId;
    private CharSequence mFormat12;
    private CharSequence mFormat24;
    private int mMclFontSizeId;
    private int mSpecialColor;

    public OpOneColorStyleClock(Context context) {
        this(context, null);
    }

    public OpOneColorStyleClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpOneColorStyleClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setupAttributes(attributeSet);
        setLayoutParams(new FrameLayout.LayoutParams(context, attributeSet));
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mFormat12 = OpDateTimeView.Patterns.clockView12;
        this.mFormat24 = OpDateTimeView.Patterns.clockView24;
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void applyLayoutParams(OpAodSettings.OpViewInfo opViewInfo) {
        int i;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        if (layoutParams != null && opViewInfo != null) {
            layoutParams.setMarginStart(opViewInfo.getMarginStart(((TextView) this).mContext));
            layoutParams.setMarginEnd(opViewInfo.getMarginEnd(((TextView) this).mContext));
            layoutParams.topMargin = opViewInfo.getMarginTop(((TextView) this).mContext);
            layoutParams.bottomMargin = opViewInfo.getMarginEnd(((TextView) this).mContext);
            int i2 = this.mFontSizeId;
            if (OpUtils.isMCLVersion() && (i = this.mMclFontSizeId) != -1) {
                i2 = i;
            }
            setTextSize(0, (float) opViewInfo.getSize(((TextView) this).mContext, i2));
        }
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onTimeChanged(Calendar calendar) {
        String charSequence = DateFormat.format(is24HourModeEnabled() ? this.mFormat24 : this.mFormat12, calendar.getTime()).toString();
        SpannableString spannableString = new SpannableString(charSequence);
        for (int i = 0; i < 2; i++) {
            if ('1' == charSequence.charAt(i)) {
                spannableString.setSpan(new ForegroundColorSpan(this.mSpecialColor), i, i + 1, 33);
            }
        }
        setText(TextUtils.expandTemplate(spannableString, new CharSequence[0]));
    }

    private boolean is24HourModeEnabled() {
        return DateFormat.is24HourFormat(((TextView) this).mContext, ActivityManager.getCurrentUser());
    }

    private void setupAttributes(AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = ((TextView) this).mContext.obtainStyledAttributes(attributeSet, R$styleable.OpOneColorStyleClock, 0, 0);
        this.mSpecialColor = obtainStyledAttributes.getColor(R$styleable.OpOneColorStyleClock_specialColor, 0);
        this.mMclFontSizeId = obtainStyledAttributes.getResourceId(R$styleable.OpOneColorStyleClock_mclTextSize, -1);
        int i = obtainStyledAttributes.getInt(R$styleable.OpOneColorStyleClock_mclFontStyle, 3);
        obtainStyledAttributes.recycle();
        TypedArray obtainStyledAttributes2 = ((TextView) this).mContext.obtainStyledAttributes(attributeSet, R.styleable.TextClock, 0, 0);
        this.mFormat12 = obtainStyledAttributes2.getText(0);
        this.mFormat24 = obtainStyledAttributes2.getText(1);
        obtainStyledAttributes2.recycle();
        TypedArray obtainStyledAttributes3 = ((TextView) this).mContext.obtainStyledAttributes(attributeSet, R.styleable.TextAppearance, 0, 0);
        this.mFontSizeId = obtainStyledAttributes3.getResourceId(0, -1);
        obtainStyledAttributes3.recycle();
        if (OpUtils.isMCLVersion()) {
            setTypeface(OpUtils.getMclTypeface(i));
        }
    }
}
