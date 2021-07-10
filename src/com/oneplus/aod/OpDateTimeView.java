package com.oneplus.aod;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.GridLayout;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.oneplus.util.OpUtils;
import java.util.Locale;
public class OpDateTimeView extends GridLayout {
    private int mClockStyle;
    private OpTextDate mDateView;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public OpDateTimeView(Context context) {
        this(context, null, 0);
    }

    public OpDateTimeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpDateTimeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        Log.d("DateTimeView", "onFinishInflate: ");
        OpTextDate opTextDate = (OpTextDate) findViewById(C0008R$id.date_view);
        this.mDateView = opTextDate;
        opTextDate.setShowCurrentUserTime(true);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        int i;
        int i2;
        super.onAttachedToWindow();
        Log.d("DateTimeView", "onAttachedToWindow");
        Resources resources = getResources();
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        int i3 = this.mClockStyle;
        if (i3 == 0) {
            i = resources.getDimensionPixelSize(C0005R$dimen.date_time_view_default_marginTop);
        } else if (i3 == 6 || i3 == 7) {
            i = OpUtils.convertDpToFixedPx(resources.getDimension(C0005R$dimen.date_time_view_analog_marginTop));
        } else if (i3 == 3) {
            i = OpUtils.convertDpToFixedPx(resources.getDimension(C0005R$dimen.aod_clock_digital_margin_top));
        } else if (i3 == 4) {
            i = OpUtils.convertDpToFixedPx(resources.getDimension(C0005R$dimen.aod_clock_typographic_margin_top));
        } else if (i3 == 10 || i3 == 9 || i3 == 8 || i3 == 5) {
            i = OpUtils.convertDpToFixedPx(resources.getDimension(C0005R$dimen.aod_clock_analog_min2_top));
        } else {
            i = i3 == 2 ? OpUtils.convertDpToFixedPx(resources.getDimension(C0005R$dimen.aod_clock_digital2_margin_top)) : 0;
        }
        if (OpAodUtils.getDeviceTag().equals("17819")) {
            i2 = resources.getDimensionPixelSize(C0005R$dimen.date_time_view_17819_additional_marginTop);
        } else if (OpAodUtils.getDeviceTag().equals("17801")) {
            i2 = resources.getDimensionPixelSize(C0005R$dimen.date_time_view_17801_additional_marginTop);
        } else {
            i2 = this.mClockStyle == 0 ? resources.getDimensionPixelSize(C0005R$dimen.date_time_view_additional_marginTop) : 0;
        }
        if (this.mClockStyle == 40) {
            marginLayoutParams.topMargin = OpUtils.convertDpToFixedPx(resources.getDimension(C0005R$dimen.op_aod_clock_analog_my_margin_top));
        } else {
            marginLayoutParams.topMargin = i + i2;
        }
        setLayoutParams(marginLayoutParams);
        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mDateView.getLayoutParams();
        int i4 = this.mClockStyle;
        if (i4 == 3 || i4 == 4 || i4 == 2) {
            marginLayoutParams2.topMargin = 0;
        } else {
            marginLayoutParams2.topMargin = resources.getDimensionPixelSize(C0005R$dimen.date_view_marginTop);
        }
        this.mDateView.setLayoutParams(marginLayoutParams2);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("DateTimeView", "onDetachedFromWindow");
    }

    public static final class Patterns {
        public static String clockView12;
        public static String clockView24;

        static void update(Context context, boolean z, int i) {
            int i2;
            Locale locale = Locale.getDefault();
            Resources resources = context.getResources();
            if (z) {
                i2 = C0015R$string.abbrev_wday_month_day_no_year_alarm;
            } else {
                i2 = C0015R$string.abbrev_wday_month_day_no_year;
            }
            String string = resources.getString(i2);
            String string2 = resources.getString(C0015R$string.clock_12hr_format);
            String string3 = resources.getString(C0015R$string.clock_24hr_format);
            DateFormat.getBestDateTimePattern(locale, string);
            clockView12 = DateFormat.getBestDateTimePattern(locale, string2);
            if (!string2.contains("a")) {
                clockView12 = clockView12.replaceAll("a", "").trim();
            }
            clockView24 = DateFormat.getBestDateTimePattern(locale, string3);
            Log.d("DateTimeView", "updateClockPattern: " + i);
            clockView24 = clockView24.replace(':', (char) 42889);
            clockView12 = clockView12.replace(':', (char) 42889);
            Log.d("DateTimeView", "update clockView12: " + clockView12 + " clockView24:" + clockView24);
        }
    }
}
