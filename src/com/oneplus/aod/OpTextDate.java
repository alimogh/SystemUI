package com.oneplus.aod;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewDebug;
import android.widget.RemoteViews;
import com.android.internal.R;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.oneplus.util.OpUtils;
import java.util.Calendar;
import java.util.TimeZone;
import libcore.icu.LocaleData;
@RemoteViews.RemoteView
public class OpTextDate extends View {
    private float mDateFontBaseLineY;
    private Paint mDatePaint;
    private CharSequence mDescFormat;
    private CharSequence mDescFormat12;
    private CharSequence mDescFormat24;
    @ViewDebug.ExportedProperty
    private CharSequence mFormat;
    private CharSequence mFormat12;
    private CharSequence mFormat24;
    private String mFormatString;
    @ViewDebug.ExportedProperty
    private boolean mHasSeconds;
    private String mLocale;
    private boolean mShowCurrentUserTime;
    private final Runnable mTicker;
    private Calendar mTime;
    private String mTimeZone;
    private Typeface mTypeface;

    private static CharSequence abc(CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3) {
        return charSequence == null ? charSequence2 == null ? charSequence3 : charSequence2 : charSequence;
    }

    public OpTextDate(Context context) {
        super(context);
        this.mDatePaint = new Paint();
        this.mTicker = new Runnable() { // from class: com.oneplus.aod.OpTextDate.1
            @Override // java.lang.Runnable
            public void run() {
                OpTextDate.this.onTimeChanged();
                long uptimeMillis = SystemClock.uptimeMillis();
                OpTextDate.this.getHandler().postAtTime(OpTextDate.this.mTicker, uptimeMillis + (1000 - (uptimeMillis % 1000)));
            }
        };
        init();
    }

    public OpTextDate(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpTextDate(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    /* JADX INFO: finally extract failed */
    public OpTextDate(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDatePaint = new Paint();
        this.mTicker = new Runnable() { // from class: com.oneplus.aod.OpTextDate.1
            @Override // java.lang.Runnable
            public void run() {
                OpTextDate.this.onTimeChanged();
                long uptimeMillis = SystemClock.uptimeMillis();
                OpTextDate.this.getHandler().postAtTime(OpTextDate.this.mTicker, uptimeMillis + (1000 - (uptimeMillis % 1000)));
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.TextClock, i, i2);
        try {
            this.mFormat12 = obtainStyledAttributes.getText(0);
            this.mFormat24 = obtainStyledAttributes.getText(1);
            this.mTimeZone = obtainStyledAttributes.getString(2);
            obtainStyledAttributes.recycle();
            init();
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    private void init() {
        if (this.mFormat12 == null || this.mFormat24 == null) {
            LocaleData localeData = LocaleData.get(getContext().getResources().getConfiguration().locale);
            if (this.mFormat12 == null) {
                this.mFormat12 = localeData.timeFormat_hm;
            }
            if (this.mFormat24 == null) {
                this.mFormat24 = localeData.timeFormat_Hm;
            }
        }
        reloadDimen();
        createTime(this.mTimeZone);
        chooseFormat(false);
        this.mDatePaint.setAntiAlias(true);
        this.mDatePaint.setLetterSpacing(Float.parseFloat("0.025"));
        this.mDatePaint.setColor(getResources().getColor(C0004R$color.date_view_white));
        this.mDatePaint.setTextAlign(Paint.Align.CENTER);
    }

    private void createTime(String str) {
        if (str != null) {
            this.mTime = Calendar.getInstance(TimeZone.getTimeZone(str));
        } else {
            this.mTime = Calendar.getInstance();
        }
    }

    public void setShowCurrentUserTime(boolean z) {
        this.mShowCurrentUserTime = z;
        chooseFormat();
        onTimeChanged();
    }

    public boolean is24HourModeEnabled() {
        if (this.mShowCurrentUserTime) {
            return DateFormat.is24HourFormat(getContext(), ActivityManager.getCurrentUser());
        }
        return DateFormat.is24HourFormat(getContext());
    }

    @RemotableViewMethod
    public void setTimeZone(String str) {
        this.mTimeZone = str;
        createTime(str);
        onTimeChanged();
    }

    private void chooseFormat() {
        chooseFormat(true);
    }

    private void chooseFormat(boolean z) {
        boolean is24HourModeEnabled = is24HourModeEnabled();
        LocaleData localeData = LocaleData.get(getContext().getResources().getConfiguration().locale);
        if (is24HourModeEnabled) {
            CharSequence abc = abc(this.mFormat24, this.mFormat12, localeData.timeFormat_Hm);
            this.mFormat = abc;
            this.mDescFormat = abc(this.mDescFormat24, this.mDescFormat12, abc);
        } else {
            CharSequence abc2 = abc(this.mFormat12, this.mFormat24, localeData.timeFormat_hm);
            this.mFormat = abc2;
            this.mDescFormat = abc(this.mDescFormat12, this.mDescFormat24, abc2);
        }
        boolean z2 = this.mHasSeconds;
        boolean hasSeconds = DateFormat.hasSeconds(this.mFormat);
        this.mHasSeconds = hasSeconds;
        if (z && z2 != hasSeconds) {
            if (z2) {
                getHandler().removeCallbacks(this.mTicker);
            } else {
                this.mTicker.run();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setTimeZone(TimeZone.getDefault().getID());
        this.mDatePaint.setTypeface(this.mTypeface);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTimeChanged() {
        setContentDescription(DateFormat.format(this.mDescFormat, this.mTime));
        invalidate();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        this.mTime.setTimeInMillis(System.currentTimeMillis());
        drawText(canvas);
        super.onDraw(canvas);
    }

    private void reloadDimen() {
        this.mDateFontBaseLineY = getResources().getDimension(C0005R$dimen.date_view_font_base_line_y);
        getResources().getDimensionPixelSize(C0005R$dimen.date_view_default_marginTop);
        getResources().getDimensionPixelSize(C0005R$dimen.date_view_analog_marginTop);
        OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.date_view_analog_mcl_marginTop));
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0082  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0118  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x011f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void drawText(android.graphics.Canvas r8) {
        /*
        // Method dump skipped, instructions count: 297
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.OpTextDate.drawText(android.graphics.Canvas):void");
    }
}
