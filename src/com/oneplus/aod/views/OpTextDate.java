package com.oneplus.aod.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.internal.R;
import com.android.systemui.C0015R$string;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class OpTextDate extends TextView {
    private String mFormatString;
    private String mLocale;

    public OpTextDate(Context context) {
        this(context, null);
    }

    public OpTextDate(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpTextDate(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setupAttributes(attributeSet);
    }

    public void onTimeChanged() {
        Locale locale;
        String str;
        if (!TextUtils.isEmpty(this.mLocale)) {
            locale = new Locale(this.mLocale);
        } else {
            locale = Locale.getDefault();
        }
        Date date = new Date();
        String str2 = null;
        boolean contains = locale.toString().contains("zh_");
        if (!TextUtils.isEmpty(this.mFormatString)) {
            if ("keyguardStyle".equals(this.mFormatString)) {
                DateFormat instanceForSkeleton = DateFormat.getInstanceForSkeleton(((TextView) this).mContext.getString(C0015R$string.system_ui_aod_date_pattern), locale);
                instanceForSkeleton.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
                str2 = instanceForSkeleton.format(Long.valueOf(date.getTime()));
            } else if (!contains) {
                str2 = new SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, this.mFormatString).toString(), locale).format(Long.valueOf(date.getTime()));
            }
        }
        if (TextUtils.isEmpty(str2)) {
            if (contains) {
                str = android.text.format.DateFormat.getBestDateTimePattern(locale, "MMMMd EEE");
            } else {
                str = android.text.format.DateFormat.getBestDateTimePattern(locale, "EEE, MMM d");
            }
            str2 = new SimpleDateFormat(str.toString(), locale).format(Long.valueOf(date.getTime()));
        }
        setText(str2);
    }

    public void setTextSettings(Typeface typeface, int i) {
        setTypeface(typeface);
        setTextSize(0, (float) i);
    }

    public void setLocale(String str) {
        this.mLocale = str;
    }

    public void setFormatString(String str) {
        this.mFormatString = str;
    }

    private void setupAttributes(AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = ((TextView) this).mContext.obtainStyledAttributes(attributeSet, R.styleable.TextAppearance, 0, 0);
        obtainStyledAttributes.getResourceId(0, -1);
        obtainStyledAttributes.recycle();
    }
}
