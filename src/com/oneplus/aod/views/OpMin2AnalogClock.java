package com.oneplus.aod.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.R;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.R$styleable;
import com.oneplus.aod.utils.OpAodDimenHelper;
import com.oneplus.aod.utils.OpAodSettings;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
public class OpMin2AnalogClock extends OpAnalogClock {
    private int mDateMarginLeftId;
    private int mDateTextColor;
    private int mDateTextSizeId;
    private TextView mDateView;

    public OpMin2AnalogClock(Context context) {
        this(context, null);
    }

    public OpMin2AnalogClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpMin2AnalogClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDateView.setTextColor(this.mDateTextColor);
    }

    @Override // com.oneplus.aod.views.OpAnalogClock, com.oneplus.aod.views.IOpAodClock
    public void applyLayoutParams(OpAodSettings.OpViewInfo opViewInfo) {
        super.applyLayoutParams(opViewInfo);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mDateView.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.leftMargin = OpAodDimenHelper.convertDpToFixedPx(((FrameLayout) this).mContext, this.mDateMarginLeftId);
            this.mDateView.setLayoutParams(layoutParams);
            this.mDateView.setTextSize(0, (float) OpAodDimenHelper.convertDpToFixedPx(((FrameLayout) this).mContext, this.mDateTextSizeId));
        }
    }

    @Override // com.oneplus.aod.views.OpAnalogClock, com.oneplus.aod.views.IOpAodClock
    public void onTimeChanged(Calendar calendar) {
        String str;
        super.onTimeChanged(calendar);
        Locale locale = Locale.getDefault();
        String locale2 = locale.toString();
        if (locale2.startsWith("zh_") || locale2.startsWith("ko_") || locale2.startsWith("ja_")) {
            str = DateFormat.getBestDateTimePattern(locale, "MMMMd");
        } else {
            str = DateFormat.getBestDateTimePattern(locale, "MMM d");
        }
        this.mDateView.setText(new SimpleDateFormat(str.toString(), locale).format(new Date()));
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.views.OpAnalogClock
    public void initViews(View view) {
        super.initViews(view);
        this.mDateView = (TextView) view.findViewById(C0008R$id.analog_dateView);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.views.OpAnalogClock
    public int getLayoutId() {
        return C0011R$layout.op_min2_analog_clock;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.views.OpAnalogClock
    public void setupAttributes(AttributeSet attributeSet) {
        super.setupAttributes(attributeSet);
        TypedArray obtainStyledAttributes = ((FrameLayout) this).mContext.obtainStyledAttributes(attributeSet, R$styleable.OpMin2AnalogClock, 0, 0);
        this.mDateMarginLeftId = obtainStyledAttributes.getResourceId(R$styleable.OpMin2AnalogClock_dateMarginLeft, 0);
        obtainStyledAttributes.recycle();
        TypedArray obtainStyledAttributes2 = ((FrameLayout) this).mContext.obtainStyledAttributes(attributeSet, R.styleable.TextAppearance, 0, 0);
        this.mDateTextSizeId = obtainStyledAttributes2.getResourceId(0, -1);
        this.mDateTextColor = obtainStyledAttributes2.getColor(3, -1);
        obtainStyledAttributes2.recycle();
    }
}
