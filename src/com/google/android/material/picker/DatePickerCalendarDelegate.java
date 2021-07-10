package com.google.android.material.picker;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Parcelable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;
import androidx.animation.AnimatorUtils;
import com.google.android.material.R$dimen;
import com.google.android.material.R$id;
import com.google.android.material.R$layout;
import com.google.android.material.R$string;
import com.google.android.material.R$styleable;
import com.google.android.material.picker.DatePicker;
import com.google.android.material.picker.DayPickerView;
import com.google.android.material.picker.YearPickerView;
import com.google.android.material.picker.calendar.OneplusLunarCalendar;
import com.google.android.material.picker.calendar.OnepulsCalendarUtil;
import com.oneplus.common.OPFeaturesUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
/* access modifiers changed from: package-private */
public class DatePickerCalendarDelegate extends DatePicker.AbstractDatePickerDelegate {
    private static final int[] ATTRS_TEXT_COLOR = {16842904};
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private ViewAnimator mAnimator;
    private ViewGroup mContainer;
    private int mCurrentView = -1;
    private DayPickerView mDayPickerView;
    private int mFirstDayOfWeek = 0;
    private TextView mHeaderLunarMonthDay;
    private TextView mHeaderMonthDay;
    private final View mHeaderMonthSelection;
    private int mHeaderMonthSelectionHeight;
    private TextView mHeaderYear;
    private final View mHeaderYearSelection;
    private int mHeaderYearSelectionHeight;
    private final Calendar mMaxDate;
    private final Calendar mMinDate;
    private SimpleDateFormat mMonthDayFormat;
    private final DayPickerView.OnDaySelectedListener mOnDaySelectedListener = new DayPickerView.OnDaySelectedListener() { // from class: com.google.android.material.picker.DatePickerCalendarDelegate.1
        @Override // com.google.android.material.picker.DayPickerView.OnDaySelectedListener
        public void onDaySelected(DayPickerView dayPickerView, Calendar calendar) {
            DatePickerCalendarDelegate.this.mCurrentDate.setTimeInMillis(calendar.getTimeInMillis());
            DatePickerCalendarDelegate.this.onDateChanged(true, true);
        }
    };
    private final View.OnClickListener mOnHeaderClickListener = new View.OnClickListener() { // from class: com.google.android.material.picker.DatePickerCalendarDelegate.3
        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            DatePickerCalendarDelegate.this.tryVibrate();
            if (view.getId() == R$id.date_picker_header_year) {
                DatePickerCalendarDelegate.this.setCurrentView(1);
            } else if (view.getId() == R$id.date_picker_header_date) {
                DatePickerCalendarDelegate.this.setCurrentView(0);
            }
        }
    };
    private final YearPickerView.OnYearSelectedListener mOnYearSelectedListener = new YearPickerView.OnYearSelectedListener() { // from class: com.google.android.material.picker.DatePickerCalendarDelegate.2
        @Override // com.google.android.material.picker.YearPickerView.OnYearSelectedListener
        public void onYearChanged(YearPickerView yearPickerView, int i) {
            int i2 = DatePickerCalendarDelegate.this.mCurrentDate.get(5);
            int daysInMonth = DatePickerCalendarDelegate.getDaysInMonth(DatePickerCalendarDelegate.this.mCurrentDate.get(2), i);
            if (i2 > daysInMonth) {
                DatePickerCalendarDelegate.this.mCurrentDate.set(5, daysInMonth);
            }
            DatePickerCalendarDelegate.this.mCurrentDate.set(1, i);
            DatePickerCalendarDelegate.this.onDateChanged(true, true);
        }
    };
    private String mSelectDay;
    private String mSelectYear;
    private final LinearLayout mSelectionLayout;
    private SimpleDateFormat mYearFormat;
    private YearPickerView mYearPickerView;

    public DatePickerCalendarDelegate(DatePicker datePicker, Context context, AttributeSet attributeSet, int i, int i2) {
        super(datePicker, context);
        Locale locale = this.mCurrentLocale;
        this.mCurrentDate = Calendar.getInstance(locale);
        Calendar.getInstance(locale);
        this.mMinDate = Calendar.getInstance(locale);
        this.mMaxDate = Calendar.getInstance(locale);
        this.mMinDate.set(1900, 0, 1);
        this.mMaxDate.set(2100, 11, 31);
        Resources resources = this.mDelegator.getResources();
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(attributeSet, R$styleable.DatePicker, i, i2);
        ViewGroup viewGroup = (ViewGroup) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(obtainStyledAttributes.getResourceId(R$styleable.DatePicker_internalLayout, R$layout.op_date_picker_material), (ViewGroup) this.mDelegator, false);
        this.mContainer = viewGroup;
        viewGroup.setSaveFromParentEnabled(false);
        this.mDelegator.addView(this.mContainer);
        ViewGroup viewGroup2 = (ViewGroup) this.mContainer.findViewById(R$id.date_picker_header);
        this.mHeaderYear = (TextView) viewGroup2.findViewById(R$id.date_picker_header_year);
        this.mHeaderYearSelection = viewGroup2.findViewById(R$id.date_picker_year_selection);
        this.mSelectionLayout = (LinearLayout) viewGroup2.findViewById(R$id.date_picker_selection_layout);
        this.mHeaderYear.setOnClickListener(this.mOnHeaderClickListener);
        this.mHeaderMonthDay = (TextView) viewGroup2.findViewById(R$id.date_picker_header_date);
        this.mHeaderMonthSelection = viewGroup2.findViewById(R$id.date_picker_month_selection);
        this.mHeaderMonthDay.setOnClickListener(this.mOnHeaderClickListener);
        this.mHeaderLunarMonthDay = (TextView) viewGroup2.findViewById(R$id.date_picker_header_lunar);
        int resourceId = obtainStyledAttributes.getResourceId(R$styleable.DatePicker_android_headerMonthTextAppearance, 0);
        if (resourceId != 0) {
            TypedArray obtainStyledAttributes2 = this.mContext.obtainStyledAttributes(null, ATTRS_TEXT_COLOR, 0, resourceId);
            obtainStyledAttributes2.getColorStateList(0);
            obtainStyledAttributes2.recycle();
        }
        ColorStateList colorStateList = obtainStyledAttributes.getColorStateList(R$styleable.DatePicker_headerTextColor);
        if (colorStateList != null) {
            this.mHeaderYear.setTextColor(colorStateList);
            this.mHeaderMonthDay.setTextColor(colorStateList);
        }
        obtainStyledAttributes.recycle();
        ViewAnimator viewAnimator = (ViewAnimator) this.mContainer.findViewById(R$id.animator);
        this.mAnimator = viewAnimator;
        DayPickerView dayPickerView = (DayPickerView) viewAnimator.findViewById(R$id.date_picker_day_picker);
        this.mDayPickerView = dayPickerView;
        dayPickerView.setFirstDayOfWeek(this.mFirstDayOfWeek);
        this.mDayPickerView.setMinDate(this.mMinDate.getTimeInMillis());
        this.mDayPickerView.setMaxDate(this.mMaxDate.getTimeInMillis());
        this.mDayPickerView.setDate(this.mCurrentDate.getTimeInMillis());
        this.mDayPickerView.setOnDaySelectedListener(this.mOnDaySelectedListener);
        YearPickerView yearPickerView = (YearPickerView) this.mAnimator.findViewById(R$id.date_picker_year_picker);
        this.mYearPickerView = yearPickerView;
        yearPickerView.setRange(this.mMinDate, this.mMaxDate);
        this.mYearPickerView.setYear(this.mCurrentDate.get(1));
        this.mYearPickerView.setOnYearSelectedListener(this.mOnYearSelectedListener);
        this.mSelectDay = resources.getString(R$string.select_day);
        this.mSelectYear = resources.getString(R$string.select_year);
        onLocaleChanged(this.mCurrentLocale);
        setCurrentView(0);
    }

    private void initSelectionParams() {
        if (this.mHeaderYearSelection != null && this.mHeaderMonthSelection != null && this.mHeaderMonthSelectionHeight == 0 && this.mHeaderYearSelectionHeight == 0) {
            Rect rect = new Rect();
            String format = this.mMonthDayFormat.format(this.mCurrentDate.getTime());
            this.mHeaderMonthDay.getPaint().getTextBounds(format, 0, format.length(), rect);
            this.mHeaderMonthSelectionHeight = rect.height();
            Rect rect2 = new Rect();
            String format2 = this.mYearFormat.format(this.mCurrentDate.getTime());
            this.mHeaderYear.getPaint().getTextBounds(format2, 0, format2.length(), rect2);
            this.mHeaderYearSelectionHeight = rect2.height();
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mHeaderYearSelection.getLayoutParams();
            layoutParams.height = this.mHeaderYearSelectionHeight;
            this.mHeaderYearSelection.setLayoutParams(layoutParams);
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mHeaderMonthSelection.getLayoutParams();
            layoutParams2.height = this.mHeaderMonthSelectionHeight;
            int measuredHeight = (this.mHeaderYear.getMeasuredHeight() - this.mHeaderYearSelectionHeight) / 2;
            layoutParams2.topMargin = Math.abs(measuredHeight - this.mContext.getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space2)) + ((this.mHeaderMonthDay.getMeasuredHeight() - this.mHeaderMonthSelectionHeight) / 2);
            this.mHeaderMonthSelection.setLayoutParams(layoutParams2);
            LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mSelectionLayout.getLayoutParams();
            layoutParams3.topMargin = measuredHeight + this.mContext.getResources().getDimensionPixelOffset(R$dimen.op_control_margin_list_top2);
            this.mSelectionLayout.setLayoutParams(layoutParams3);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.google.android.material.picker.DatePicker.AbstractDatePickerDelegate
    public void onLocaleChanged(Locale locale) {
        if (this.mHeaderYear != null) {
            this.mMonthDayFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "EMMMd"), locale);
            this.mYearFormat = new SimpleDateFormat("y", locale);
            onCurrentDateChanged(false);
        }
    }

    private void onCurrentDateChanged(boolean z) {
        if (this.mHeaderYear != null) {
            this.mHeaderYear.setText(this.mYearFormat.format(this.mCurrentDate.getTime()));
            this.mHeaderMonthDay.setText(this.mMonthDayFormat.format(this.mCurrentDate.getTime()));
            updateLunarDate();
            if (z) {
                this.mAnimator.announceForAccessibility(getFormattedCurrentDate());
            }
        }
    }

    private void updateLunarDate() {
        String str = Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry();
        if (str == null || !str.contains("zh")) {
            this.mHeaderLunarMonthDay.setVisibility(8);
            return;
        }
        OneplusLunarCalendar solarToLunar = OnepulsCalendarUtil.solarToLunar(this.mCurrentDate);
        boolean equals = "zh_CN".equals(str);
        TextView textView = this.mHeaderLunarMonthDay;
        StringBuilder sb = new StringBuilder();
        sb.append(equals ? "农历：" : "農曆：");
        sb.append(solarToLunar.getYYMMDD());
        textView.setText(sb.toString());
        this.mHeaderLunarMonthDay.setVisibility(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCurrentView(int i) {
        if (i == 0) {
            this.mDayPickerView.setDate(this.mCurrentDate.getTimeInMillis());
            if (this.mCurrentView != i) {
                this.mHeaderMonthDay.setActivated(true);
                animatorToYearSelection(true);
                this.mHeaderYear.setActivated(false);
                this.mAnimator.setDisplayedChild(0);
                this.mCurrentView = i;
            }
            this.mAnimator.announceForAccessibility(this.mSelectDay);
        } else if (i == 1) {
            changeYearLayoutParams();
            this.mYearPickerView.setYear(this.mCurrentDate.get(1));
            this.mYearPickerView.post(new Runnable() { // from class: com.google.android.material.picker.DatePickerCalendarDelegate.4
                @Override // java.lang.Runnable
                public void run() {
                    DatePickerCalendarDelegate.this.mYearPickerView.requestFocus();
                    DatePickerCalendarDelegate.this.mYearPickerView.clearFocus();
                }
            });
            if (this.mCurrentView != i) {
                this.mHeaderMonthDay.setActivated(false);
                this.mHeaderYear.setActivated(true);
                animatorToYearSelection(false);
                this.mAnimator.setDisplayedChild(1);
                this.mCurrentView = i;
            }
            this.mAnimator.announceForAccessibility(this.mSelectYear);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0021: APUT  (r4v0 float[]), (0 ??[int, short, byte, char]), (r6v0 float) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0054: APUT  (r4v1 float[]), (0 ??[int, short, byte, char]), (r10v1 float) */
    private void animatorToYearSelection(boolean z) {
        float f;
        if (this.mHeaderYearSelectionHeight > 0 && this.mHeaderMonthSelectionHeight > 0) {
            AnimatorSet animatorSet = new AnimatorSet();
            float f2 = ((float) this.mHeaderYearSelectionHeight) / ((float) this.mHeaderMonthSelectionHeight);
            View view = this.mHeaderMonthSelection;
            float[] fArr = new float[2];
            fArr[0] = z ? f2 : 1.0f;
            if (z) {
                f2 = 1.0f;
            }
            fArr[1] = f2;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, "scaleY", fArr);
            View view2 = this.mHeaderMonthSelection;
            float[] fArr2 = new float[1];
            if (z) {
                f = 0.0f;
            } else {
                f = ((float) (this.mHeaderYearSelection.getTop() - this.mHeaderMonthSelection.getTop())) - (((float) Math.abs(this.mHeaderMonthSelectionHeight - this.mHeaderYearSelectionHeight)) / 2.0f);
            }
            fArr2[0] = f;
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view2, "translationY", fArr2);
            animatorSet.setDuration(125L);
            animatorSet.setInterpolator(AnimatorUtils.op_control_interpolator_linear_out_slow_in);
            animatorSet.playTogether(ofFloat, ofFloat2);
            animatorSet.start();
        }
    }

    public void changeYearLayoutParams() {
        if (this.mCurrentView == 1) {
            this.mYearPickerView.setLayoutParams(new FrameLayout.LayoutParams(-1, this.mContext.getResources().getConfiguration().orientation == 2 ? -2 : -1));
        }
        initSelectionParams();
    }

    @Override // com.google.android.material.picker.DatePicker.DatePickerDelegate
    public void updateDate(int i, int i2, int i3) {
        this.mCurrentDate.set(1, i);
        this.mCurrentDate.set(2, i2);
        this.mCurrentDate.set(5, i3);
        onDateChanged(false, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDateChanged(boolean z, boolean z2) {
        int i = this.mCurrentDate.get(1);
        if (z2 && !(this.mOnDateChangedListener == null && this.mAutoFillChangeListener == null)) {
            int i2 = this.mCurrentDate.get(2);
            int i3 = this.mCurrentDate.get(5);
            DatePicker.OnDateChangedListener onDateChangedListener = this.mOnDateChangedListener;
            if (onDateChangedListener != null) {
                onDateChangedListener.onDateChanged(this.mDelegator, i, i2, i3);
            }
            DatePicker.OnDateChangedListener onDateChangedListener2 = this.mAutoFillChangeListener;
            if (onDateChangedListener2 != null) {
                onDateChangedListener2.onDateChanged(this.mDelegator, i, i2, i3);
            }
        }
        this.mDayPickerView.setDate(this.mCurrentDate.getTimeInMillis(), true);
        this.mYearPickerView.setYear(i);
        onCurrentDateChanged(z);
        if (z) {
            tryVibrate();
        }
    }

    @Override // com.google.android.material.picker.DatePicker.DatePickerDelegate
    public void setFirstDayOfWeek(int i) {
        this.mFirstDayOfWeek = i;
        this.mDayPickerView.setFirstDayOfWeek(i);
    }

    @Override // com.google.android.material.picker.DatePicker.DatePickerDelegate
    public void setEnabled(boolean z) {
        this.mContainer.setEnabled(z);
        this.mDayPickerView.setEnabled(z);
        this.mYearPickerView.setEnabled(z);
        this.mHeaderYear.setEnabled(z);
        this.mHeaderMonthDay.setEnabled(z);
    }

    @Override // com.google.android.material.picker.DatePicker.DatePickerDelegate
    public boolean isEnabled() {
        return this.mContainer.isEnabled();
    }

    @Override // com.google.android.material.picker.DatePicker.DatePickerDelegate
    public void onConfigurationChanged(Configuration configuration) {
        setCurrentLocale(configuration.locale);
    }

    @Override // com.google.android.material.picker.DatePicker.DatePickerDelegate
    public Parcelable onSaveInstanceState(Parcelable parcelable) {
        return new DatePicker.AbstractDatePickerDelegate.SavedState(parcelable, this.mCurrentDate.get(1), this.mCurrentDate.get(2), this.mCurrentDate.get(5), this.mMinDate.getTimeInMillis(), this.mMaxDate.getTimeInMillis(), this.mCurrentView, this.mCurrentView == 0 ? this.mDayPickerView.getMostVisiblePosition() : -1, -1);
    }

    @Override // com.google.android.material.picker.DatePicker.DatePickerDelegate
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof DatePicker.AbstractDatePickerDelegate.SavedState) {
            DatePicker.AbstractDatePickerDelegate.SavedState savedState = (DatePicker.AbstractDatePickerDelegate.SavedState) parcelable;
            this.mCurrentDate.set(savedState.getSelectedYear(), savedState.getSelectedMonth(), savedState.getSelectedDay());
            this.mMinDate.setTimeInMillis(savedState.getMinDate());
            this.mMaxDate.setTimeInMillis(savedState.getMaxDate());
            onCurrentDateChanged(false);
            int currentView = savedState.getCurrentView();
            setCurrentView(currentView);
            int listPosition = savedState.getListPosition();
            if (listPosition == -1) {
                return;
            }
            if (currentView == 0) {
                this.mDayPickerView.setPosition(listPosition);
            } else if (currentView == 1) {
                savedState.getListPositionOffset();
            }
        }
    }

    public static int getDaysInMonth(int i, int i2) {
        switch (i) {
            case 0:
            case 2:
            case 4:
            case 6:
            case 7:
            case 9:
            case 11:
                return 31;
            case 1:
                return i2 % 4 == 0 ? 29 : 28;
            case 3:
            case 5:
            case 8:
            case 10:
                return 30;
            default:
                throw new IllegalArgumentException("Invalid Month");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @SuppressLint({"MissingPermission"})
    private void tryVibrate() {
        if (!OPFeaturesUtils.isSupportZVibrate() || Build.VERSION.SDK_INT <= 26) {
            this.mDelegator.performHapticFeedback(5);
            return;
        }
        try {
            Field declaredField = VibrationEffect.class.getDeclaredField("EFFECT_CLICK");
            Method declaredMethod = VibrationEffect.class.getDeclaredMethod("get", Integer.TYPE);
            declaredMethod.setAccessible(true);
            declaredField.setAccessible(true);
            ((Vibrator) this.mContext.getSystemService("vibrator")).vibrate((VibrationEffect) declaredMethod.invoke(null, Integer.valueOf(declaredField.getInt(null))), VIBRATION_ATTRIBUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
