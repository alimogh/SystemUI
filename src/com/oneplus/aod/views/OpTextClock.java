package com.oneplus.aod.views;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import androidx.core.content.res.ResourcesCompat;
import com.android.internal.R;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.oneplus.aod.utils.OpAodDimenHelper;
import com.oneplus.aod.utils.OpAodSettings;
import com.oneplus.util.OpUtils;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import libcore.icu.LocaleData;
@RemoteViews.RemoteView
public class OpTextClock extends View implements IOpAodClock {
    private Context mContext;
    private CharSequence mDescFormat;
    private CharSequence mDescFormat12;
    private CharSequence mDescFormat24;
    private int mDigitColorRed;
    private int mDigitColorWhite;
    private float mFontBaseLineY;
    private int mFontFamilyId;
    private int mFontSizeId;
    @ViewDebug.ExportedProperty
    private CharSequence mFormat;
    private CharSequence mFormat12;
    private CharSequence mFormat24;
    @ViewDebug.ExportedProperty
    private boolean mHasSeconds;
    private Paint mHourPaint;
    private Paint mMinPaint;
    private boolean mShowCurrentUserTime;
    private final Runnable mTicker;
    private Calendar mTime;
    private String mTimeZone;

    private static CharSequence abc(CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3) {
        return charSequence == null ? charSequence2 == null ? charSequence3 : charSequence2 : charSequence;
    }

    public OpTextClock(Context context) {
        super(context);
        this.mHourPaint = new Paint();
        this.mMinPaint = new Paint();
        this.mTicker = new Runnable() { // from class: com.oneplus.aod.views.OpTextClock.1
            @Override // java.lang.Runnable
            public void run() {
                OpTextClock.this.onTimeChanged();
                long uptimeMillis = SystemClock.uptimeMillis();
                OpTextClock.this.getHandler().postAtTime(OpTextClock.this.mTicker, uptimeMillis + (1000 - (uptimeMillis % 1000)));
            }
        };
        this.mContext = context;
        init();
    }

    public OpTextClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
        this.mContext = context;
    }

    public OpTextClock(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
        this.mContext = context;
    }

    public OpTextClock(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mHourPaint = new Paint();
        this.mMinPaint = new Paint();
        this.mTicker = new Runnable() { // from class: com.oneplus.aod.views.OpTextClock.1
            @Override // java.lang.Runnable
            public void run() {
                OpTextClock.this.onTimeChanged();
                long uptimeMillis = SystemClock.uptimeMillis();
                OpTextClock.this.getHandler().postAtTime(OpTextClock.this.mTicker, uptimeMillis + (1000 - (uptimeMillis % 1000)));
            }
        };
        this.mContext = context;
        setupAttributes(attributeSet);
        setLayoutParams(new FrameLayout.LayoutParams(context, attributeSet));
        init();
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
        this.mShowCurrentUserTime = true;
        createTime(this.mTimeZone);
        chooseFormat(false);
        this.mDigitColorRed = this.mContext.getResources().getColor(C0004R$color.clock_ten_digit_red);
        this.mDigitColorWhite = this.mContext.getResources().getColor(C0004R$color.clock_ten_digit_white);
        this.mHourPaint.setAntiAlias(true);
        this.mMinPaint.setAntiAlias(true);
        this.mMinPaint.setColor(this.mDigitColorWhite);
        updateTypeface();
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void applyLayoutParams(OpAodSettings.OpViewInfo opViewInfo) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        if (!(layoutParams == null || opViewInfo == null)) {
            layoutParams.setMarginStart(opViewInfo.getMarginStart(this.mContext));
            layoutParams.setMarginEnd(opViewInfo.getMarginEnd(this.mContext));
            layoutParams.topMargin = opViewInfo.getMarginTop(this.mContext);
            layoutParams.bottomMargin = opViewInfo.getMarginEnd(this.mContext);
            layoutParams.height = getTextHeight();
        }
        int i = this.mFontSizeId;
        if (i == -1) {
            i = C0005R$dimen.aod_clock_default_font_size;
        }
        float convertDpToFixedPx = (float) OpAodDimenHelper.convertDpToFixedPx(this.mContext, i);
        this.mHourPaint.setTextSize(convertDpToFixedPx);
        this.mMinPaint.setTextSize(convertDpToFixedPx);
    }

    private void createTime(String str) {
        if (str != null) {
            this.mTime = Calendar.getInstance(TimeZone.getTimeZone(str), Locale.ENGLISH);
        } else {
            this.mTime = Calendar.getInstance();
        }
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
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onTimeChanged(Calendar calendar) {
        onTimeChanged();
        invalidate();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTimeChanged() {
        setContentDescription(DateFormat.format(this.mDescFormat, this.mTime));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mTime.setTimeInMillis(System.currentTimeMillis());
        drawClockDefault(canvas);
    }

    private void drawClockDefault(Canvas canvas) {
        Rect rect = new Rect();
        char[] cArr = new char[2];
        String charSequence = DateFormat.format(is24HourModeEnabled() ? "HH" : "hh", this.mTime).toString();
        String charSequence2 = DateFormat.format("mm", this.mTime).toString();
        Paint.FontMetrics fontMetrics = this.mHourPaint.getFontMetrics();
        float[] fArr = new float[2];
        this.mHourPaint.getTextWidths(charSequence, fArr);
        float f = fArr[0];
        int width = (int) (((float) (canvas.getWidth() / 2)) - fArr[0]);
        this.mHourPaint.setTextAlign(Paint.Align.LEFT);
        this.mHourPaint.setColor(charSequence.charAt(0) == '1' ? this.mDigitColorRed : this.mDigitColorWhite);
        cArr[0] = charSequence.charAt(0);
        this.mHourPaint.getTextBounds(String.valueOf(cArr[0]), 0, 1, rect);
        if (OpUtils.isMCLVersion()) {
            this.mFontBaseLineY = (float) this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.clock_view_default_font_base_line1_y);
        } else {
            this.mFontBaseLineY = Math.abs(fontMetrics.ascent) - 24.0f;
        }
        float f2 = (float) width;
        canvas.drawText(cArr[0] + "", f2, this.mFontBaseLineY, this.mHourPaint);
        cArr[1] = charSequence.charAt(1);
        this.mHourPaint.setColor(charSequence.charAt(1) == '1' ? this.mDigitColorRed : this.mDigitColorWhite);
        canvas.drawText(cArr[1] + "", f2 + f, this.mFontBaseLineY, this.mHourPaint);
        if (OpUtils.isMCLVersion()) {
            this.mFontBaseLineY = (float) this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.clock_view_default_font_base_line2_y);
        } else {
            this.mFontBaseLineY = ((Math.abs(fontMetrics.ascent) * 2.0f) + fontMetrics.descent) - 72.0f;
        }
        this.mHourPaint.setTextAlign(Paint.Align.CENTER);
        this.mHourPaint.setColor(this.mDigitColorWhite);
        canvas.drawText(charSequence2, (float) (canvas.getWidth() / 2), this.mFontBaseLineY, this.mHourPaint);
    }

    private void setupAttributes(AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(attributeSet, R.styleable.TextClock, 0, 0);
        this.mFormat12 = obtainStyledAttributes.getText(0);
        this.mFormat24 = obtainStyledAttributes.getText(1);
        this.mTimeZone = obtainStyledAttributes.getString(2);
        obtainStyledAttributes.recycle();
        TypedArray obtainStyledAttributes2 = this.mContext.obtainStyledAttributes(attributeSet, R.styleable.TextAppearance, 0, 0);
        this.mFontSizeId = obtainStyledAttributes2.getResourceId(0, -1);
        this.mFontFamilyId = obtainStyledAttributes2.getResourceId(12, -1);
        obtainStyledAttributes2.recycle();
    }

    private void updateTypeface() {
        Typeface mclTypeface = OpUtils.isMCLVersion() ? OpUtils.getMclTypeface(2) : null;
        if (mclTypeface == null) {
            mclTypeface = Typeface.create(ResourcesCompat.getFont(this.mContext, this.mFontFamilyId), 0);
            mclTypeface.isLikeDefault = false;
        }
        if (mclTypeface == null) {
            mclTypeface = Typeface.create("sans-serif", 1);
        }
        this.mHourPaint.setTypeface(mclTypeface);
        this.mMinPaint.setTypeface(mclTypeface);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = getTextHeight();
        setLayoutParams(layoutParams);
    }

    private int getTextHeight() {
        float f;
        Paint.FontMetrics fontMetrics = this.mHourPaint.getFontMetrics();
        if (OpUtils.isMCLVersion()) {
            f = Math.abs(fontMetrics.top) * 2.0f;
        } else {
            f = ((Math.abs(fontMetrics.ascent) + fontMetrics.descent) - 48.0f) * 2.0f;
        }
        return (int) f;
    }
}
