package com.google.android.material.picker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import com.google.android.material.R$dimen;
import com.google.android.material.R$string;
import com.google.android.material.R$styleable;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.math.MathUtils;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
/* access modifiers changed from: package-private */
public class SimpleMonthView extends View implements View.OnFocusChangeListener {
    private int mActivatedDay;
    private final Calendar mCalendar;
    private int mCellWidth;
    private Context mContext;
    private final NumberFormat mDayFormatter;
    private int mDayHeight;
    private final Paint mDayHighlightPaint;
    private final Paint mDayHighlightSelectorPaint;
    private int mDayOfWeekHeight;
    private final String[] mDayOfWeekLabels;
    private final TextPaint mDayOfWeekPaint;
    private int mDayOfWeekStart;
    private final TextPaint mDayPaint;
    private final Paint mDaySelectorPaint;
    private int mDaySelectorRadius;
    private ColorStateList mDayTextColor;
    private int mDaysInMonth;
    private final int mDesiredCellWidth;
    private final int mDesiredDayHeight;
    private final int mDesiredDayOfWeekHeight;
    private final int mDesiredDaySelectorRadius;
    private final int mDesiredMonthHeight;
    private int mEnabledDayEnd;
    private int mEnabledDayStart;
    private int mHighlightedDay;
    private int mInitColor;
    private boolean mIsTouchHighlighted;
    private final Locale mLocale;
    private int mMonth;
    private int mMonthHeight;
    private final TextPaint mMonthPaint;
    private String mMonthYearLabel;
    private OnDayClickListener mOnDayClickListener;
    private int mPaddedHeight;
    private int mPaddedWidth;
    private int mPreviouslyHighlightedDay;
    private int mToday;
    private final MonthViewTouchHelper mTouchHelper;
    private int mWeekStart;
    private int mYear;

    public interface OnDayClickListener {
        void onDayClick(SimpleMonthView simpleMonthView, Calendar calendar);
    }

    private static boolean isValidDayOfWeek(int i) {
        return i >= 1 && i <= 7;
    }

    private static boolean isValidMonth(int i) {
        return i >= 0 && i <= 11;
    }

    public SimpleMonthView(Context context) {
        this(context, null);
    }

    public SimpleMonthView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 16843612);
    }

    public SimpleMonthView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public SimpleMonthView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mMonthPaint = new TextPaint();
        this.mDayOfWeekPaint = new TextPaint();
        this.mDayPaint = new TextPaint();
        this.mDaySelectorPaint = new Paint();
        this.mDayHighlightPaint = new Paint();
        this.mDayHighlightSelectorPaint = new Paint();
        this.mDayOfWeekLabels = new String[7];
        this.mActivatedDay = -1;
        this.mToday = -1;
        this.mWeekStart = 1;
        this.mEnabledDayStart = 1;
        this.mEnabledDayEnd = 31;
        this.mHighlightedDay = -1;
        this.mPreviouslyHighlightedDay = -1;
        this.mIsTouchHighlighted = false;
        this.mContext = context;
        Resources resources = context.getResources();
        this.mDesiredMonthHeight = resources.getDimensionPixelSize(R$dimen.date_picker_month_height);
        this.mDesiredDayOfWeekHeight = resources.getDimensionPixelSize(R$dimen.date_picker_day_of_week_height);
        this.mDesiredDayHeight = resources.getDimensionPixelSize(R$dimen.date_picker_day_height);
        this.mDesiredCellWidth = resources.getDimensionPixelSize(R$dimen.date_picker_day_width);
        this.mDesiredDaySelectorRadius = resources.getDimensionPixelSize(R$dimen.date_picker_day_selector_radius);
        MonthViewTouchHelper monthViewTouchHelper = new MonthViewTouchHelper(this);
        this.mTouchHelper = monthViewTouchHelper;
        ViewCompat.setAccessibilityDelegate(this, monthViewTouchHelper);
        setImportantForAccessibility(1);
        Locale locale = resources.getConfiguration().locale;
        this.mLocale = locale;
        this.mCalendar = Calendar.getInstance(locale);
        this.mDayFormatter = NumberFormat.getIntegerInstance(this.mLocale);
        updateMonthYearLabel();
        updateDayOfWeekLabels();
        initPaints(resources);
    }

    private void updateMonthYearLabel() {
        this.mMonthYearLabel = new SimpleDateFormat(DateFormat.getBestDateTimePattern(this.mLocale, "MMMMy"), this.mLocale).format(this.mCalendar.getTime());
    }

    private void updateDayOfWeekLabels() {
        ArrayList arrayList = new ArrayList();
        for (int i = 1; i < 8; i++) {
            arrayList.add(DateUtils.getDayOfWeekString(i, 50));
        }
        for (int i2 = 0; i2 < 7; i2++) {
            this.mDayOfWeekLabels[i2] = (String) arrayList.get(((this.mWeekStart + i2) - 1) % 7);
        }
    }

    @SuppressLint({"WrongConstant"})
    private ColorStateList applyTextAppearance(Paint paint, int i) {
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(null, R$styleable.TextAppearance, 0, i);
        String string = obtainStyledAttributes.getString(R$styleable.TextAppearance_android_fontFamily);
        if (string != null) {
            paint.setTypeface(Typeface.create(string, 0));
        }
        paint.setTextSize((float) obtainStyledAttributes.getDimensionPixelSize(R$styleable.TextAppearance_android_textSize, (int) paint.getTextSize()));
        ColorStateList colorStateList = obtainStyledAttributes.getColorStateList(R$styleable.TextAppearance_android_textColor);
        if (colorStateList != null) {
            paint.setColor(colorStateList.getColorForState(View.ENABLED_STATE_SET, 0));
        }
        obtainStyledAttributes.recycle();
        return colorStateList;
    }

    public int getMonthHeight() {
        return this.mMonthHeight;
    }

    public int getCellWidth() {
        return this.mCellWidth;
    }

    public void setMonthTextAppearance(int i) {
        applyTextAppearance(this.mMonthPaint, i);
        this.mInitColor = this.mMonthPaint.getColor();
        invalidate();
    }

    public void setDayOfWeekTextAppearance(int i) {
        applyTextAppearance(this.mDayOfWeekPaint, i);
        invalidate();
    }

    public void setDayTextAppearance(int i) {
        ColorStateList applyTextAppearance = applyTextAppearance(this.mDayPaint, i);
        if (applyTextAppearance != null) {
            this.mDayTextColor = applyTextAppearance;
        }
        invalidate();
    }

    @SuppressLint({"WrongConstant"})
    private void initPaints(Resources resources) {
        String string = resources.getString(R$string.date_picker_month_typeface);
        String string2 = resources.getString(R$string.date_picker_day_of_week_typeface);
        String string3 = resources.getString(R$string.date_picker_day_typeface);
        int dimensionPixelSize = resources.getDimensionPixelSize(R$dimen.date_picker_month_text_size);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(R$dimen.date_picker_day_of_week_text_size);
        int dimensionPixelSize3 = resources.getDimensionPixelSize(R$dimen.date_picker_day_text_size);
        this.mMonthPaint.setAntiAlias(true);
        this.mMonthPaint.setTextSize((float) dimensionPixelSize);
        this.mMonthPaint.setTypeface(Typeface.create(string, 0));
        this.mMonthPaint.setTextAlign(Paint.Align.CENTER);
        this.mMonthPaint.setStyle(Paint.Style.FILL);
        this.mDayOfWeekPaint.setAntiAlias(true);
        this.mDayOfWeekPaint.setTextSize((float) dimensionPixelSize2);
        this.mDayOfWeekPaint.setTypeface(Typeface.create(string2, 0));
        this.mDayOfWeekPaint.setTextAlign(Paint.Align.CENTER);
        this.mDayOfWeekPaint.setStyle(Paint.Style.FILL);
        this.mDaySelectorPaint.setAntiAlias(true);
        this.mDaySelectorPaint.setStyle(Paint.Style.FILL);
        this.mDayHighlightPaint.setAntiAlias(true);
        this.mDayHighlightPaint.setStyle(Paint.Style.FILL);
        this.mDayHighlightSelectorPaint.setAntiAlias(true);
        this.mDayHighlightSelectorPaint.setStyle(Paint.Style.FILL);
        this.mDayPaint.setAntiAlias(true);
        this.mDayPaint.setTextSize((float) dimensionPixelSize3);
        this.mDayPaint.setTypeface(Typeface.create(string3, 0));
        this.mDayPaint.setTextAlign(Paint.Align.CENTER);
        this.mDayPaint.setStyle(Paint.Style.FILL);
    }

    public void setMonthTextAlpha(int i) {
        int i2 = this.mInitColor;
        if (Integer.toHexString(i2).length() > 2) {
            String hexString = Integer.toHexString((i * new BigInteger(Integer.toHexString(i2).substring(0, 2), 16).intValue()) / 255);
            String substring = Integer.toHexString(i2).substring(2);
            this.mMonthPaint.setColor(new ColorStateList(new int[][]{new int[]{16842910}, new int[0]}, new int[]{new BigInteger(hexString + substring, 16).intValue(), i2}).getColorForState(View.ENABLED_STATE_SET, 0));
            invalidate();
        }
    }

    /* access modifiers changed from: package-private */
    public void setMonthTextColor(ColorStateList colorStateList) {
        this.mMonthPaint.setColor(colorStateList.getColorForState(View.ENABLED_STATE_SET, 0));
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setDayOfWeekTextColor(ColorStateList colorStateList) {
        this.mDayOfWeekPaint.setColor(colorStateList.getColorForState(View.ENABLED_STATE_SET, 0));
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setDayTextColor(ColorStateList colorStateList) {
        this.mDayTextColor = colorStateList;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setDaySelectorColor(ColorStateList colorStateList) {
        int colorForState = colorStateList.getColorForState(ViewUtils.getViewState(40), 0);
        this.mDaySelectorPaint.setColor(colorForState);
        this.mDayHighlightSelectorPaint.setColor(colorForState);
        this.mDayHighlightSelectorPaint.setAlpha(176);
        this.mDayHighlightSelectorPaint.setFakeBoldText(true);
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setDayHighlightColor(ColorStateList colorStateList) {
        this.mDayHighlightPaint.setColor(colorStateList.getColorForState(ViewUtils.getViewState(24), 0));
        invalidate();
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        this.mOnDayClickListener = onDayClickListener;
    }

    @Override // android.view.View
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return this.mTouchHelper.dispatchHoverEvent(motionEvent) || super.dispatchHoverEvent(motionEvent);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x001c, code lost:
        if (r6 != 3) goto L_0x0047;
     */
    @Override // android.view.View
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r6) {
        /*
            r5 = this;
            float r0 = r6.getX()
            r1 = 1056964608(0x3f000000, float:0.5)
            float r0 = r0 + r1
            int r0 = (int) r0
            float r2 = r6.getY()
            float r2 = r2 + r1
            int r1 = (int) r2
            int r6 = r6.getAction()
            r2 = 0
            r3 = 1
            if (r6 == 0) goto L_0x002f
            if (r6 == r3) goto L_0x001f
            r4 = 2
            if (r6 == r4) goto L_0x002f
            r0 = 3
            if (r6 == r0) goto L_0x0026
            goto L_0x0047
        L_0x001f:
            int r6 = r5.getDayAtLocation(r0, r1)
            r5.onDayClicked(r6)
        L_0x0026:
            r6 = -1
            r5.mHighlightedDay = r6
            r5.mIsTouchHighlighted = r2
            r5.invalidate()
            goto L_0x0047
        L_0x002f:
            int r0 = r5.getDayAtLocation(r0, r1)
            r5.mIsTouchHighlighted = r3
            int r1 = r5.mHighlightedDay
            if (r1 == r0) goto L_0x0040
            r5.mHighlightedDay = r0
            r5.mPreviouslyHighlightedDay = r0
            r5.invalidate()
        L_0x0040:
            if (r6 != 0) goto L_0x0047
            r5 = -100
            if (r0 != r5) goto L_0x0047
            return r2
        L_0x0047:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.picker.SimpleMonthView.onTouchEvent(android.view.MotionEvent):boolean");
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        int i2;
        int keyCode = keyEvent.getKeyCode();
        boolean z = false;
        if (keyCode != 61) {
            if (keyCode != 66) {
                switch (keyCode) {
                    case 19:
                        if (keyEvent.hasNoModifiers()) {
                            ensureFocusedDay();
                            int i3 = this.mHighlightedDay;
                            if (i3 > 7) {
                                this.mHighlightedDay = i3 - 7;
                                z = true;
                                break;
                            }
                        }
                        break;
                    case 20:
                        if (keyEvent.hasNoModifiers()) {
                            ensureFocusedDay();
                            int i4 = this.mHighlightedDay;
                            if (i4 <= this.mDaysInMonth - 7) {
                                this.mHighlightedDay = i4 + 7;
                                z = true;
                                break;
                            }
                        }
                        break;
                    case 21:
                        if (keyEvent.hasNoModifiers()) {
                            z = moveOneDay(ViewUtils.isLayoutRtl(this));
                            break;
                        }
                        break;
                    case 22:
                        if (keyEvent.hasNoModifiers()) {
                            z = moveOneDay(!ViewUtils.isLayoutRtl(this));
                            break;
                        }
                        break;
                }
            }
            int i5 = this.mHighlightedDay;
            if (i5 != -1) {
                onDayClicked(i5);
                return true;
            }
        } else {
            if (keyEvent.hasNoModifiers()) {
                i2 = 2;
            } else {
                i2 = keyEvent.hasModifiers(1) ? 1 : 0;
            }
            if (i2 != 0) {
                ViewParent parent = getParent();
                View view = this;
                do {
                    view = view.focusSearch(i2);
                    if (view == null || view == this) {
                        break;
                    }
                } while (view.getParent() == parent);
                if (view != null) {
                    view.requestFocus();
                    return true;
                }
            }
        }
        if (!z) {
            return super.onKeyDown(i, keyEvent);
        }
        invalidate();
        return true;
    }

    private boolean moveOneDay(boolean z) {
        int i;
        int i2;
        ensureFocusedDay();
        if (z) {
            if (!isLastDayOfWeek(this.mHighlightedDay) && (i2 = this.mHighlightedDay) < this.mDaysInMonth) {
                this.mHighlightedDay = i2 + 1;
                return true;
            }
        } else if (!isFirstDayOfWeek(this.mHighlightedDay) && (i = this.mHighlightedDay) > 1) {
            this.mHighlightedDay = i - 1;
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFocusChanged(boolean z, int i, Rect rect) {
        if (z) {
            int findDayOffset = findDayOffset();
            int i2 = 1;
            if (i == 17) {
                this.mHighlightedDay = Math.min(this.mDaysInMonth, ((findClosestRow(rect) + 1) * 7) - findDayOffset);
            } else if (i == 33) {
                int findClosestColumn = findClosestColumn(rect);
                int i3 = this.mDaysInMonth;
                int i4 = (findClosestColumn - findDayOffset) + (((findDayOffset + i3) / 7) * 7) + 1;
                if (i4 > i3) {
                    i4 -= 7;
                }
                this.mHighlightedDay = i4;
            } else if (i == 66) {
                int findClosestRow = findClosestRow(rect);
                if (findClosestRow != 0) {
                    i2 = 1 + ((findClosestRow * 7) - findDayOffset);
                }
                this.mHighlightedDay = i2;
            } else if (i == 130) {
                int findClosestColumn2 = (findClosestColumn(rect) - findDayOffset) + 1;
                if (findClosestColumn2 < 1) {
                    findClosestColumn2 += 7;
                }
                this.mHighlightedDay = findClosestColumn2;
            }
            ensureFocusedDay();
            invalidate();
        }
        super.onFocusChanged(z, i, rect);
    }

    private int findClosestRow(Rect rect) {
        if (rect == null) {
            return 3;
        }
        int centerY = rect.centerY();
        TextPaint textPaint = this.mDayPaint;
        int i = this.mMonthHeight + this.mDayOfWeekHeight;
        int i2 = this.mDayHeight;
        int round = Math.round(((float) ((int) (((float) centerY) - (((float) (i + (i2 / 2))) - ((textPaint.ascent() + textPaint.descent()) / 2.0f))))) / ((float) i2));
        int findDayOffset = findDayOffset() + this.mDaysInMonth;
        return MathUtils.constrain(round, 0, (findDayOffset / 7) - (findDayOffset % 7 == 0 ? 1 : 0));
    }

    private int findClosestColumn(Rect rect) {
        if (rect == null) {
            return 3;
        }
        int centerX = rect.centerX() - getPaddingLeft();
        int i = this.mCellWidth;
        if (i == 0) {
            return 3;
        }
        int constrain = MathUtils.constrain(centerX / i, 0, 6);
        return ViewUtils.isLayoutRtl(this) ? (7 - constrain) - 1 : constrain;
    }

    @Override // android.view.View
    public void getFocusedRect(Rect rect) {
        int i = this.mHighlightedDay;
        if (i > 0) {
            getBoundsForDay(i, rect);
        } else {
            super.getFocusedRect(rect);
        }
    }

    private void ensureFocusedDay() {
        if (this.mHighlightedDay == -1) {
            int i = this.mPreviouslyHighlightedDay;
            if (i != -1) {
                this.mHighlightedDay = i;
                return;
            }
            int i2 = this.mActivatedDay;
            if (i2 != -1) {
                this.mHighlightedDay = i2;
            } else {
                this.mHighlightedDay = 1;
            }
        }
    }

    private boolean isFirstDayOfWeek(int i) {
        if (((findDayOffset() + i) - 1) % 7 == 0) {
            return true;
        }
        return false;
    }

    private boolean isLastDayOfWeek(int i) {
        return (findDayOffset() + i) % 7 == 0;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        canvas.translate((float) paddingLeft, (float) paddingTop);
        drawMonth(canvas);
        drawDaysOfWeek(canvas);
        drawDays(canvas);
        canvas.translate((float) (-paddingLeft), (float) (-paddingTop));
    }

    private void drawMonth(Canvas canvas) {
        float ascent = this.mMonthPaint.ascent() + this.mMonthPaint.descent();
        canvas.drawText(this.mMonthYearLabel, ((float) this.mPaddedWidth) / 2.0f, (((float) this.mMonthHeight) - ascent) / 2.0f, this.mMonthPaint);
    }

    public String getMonthYearLabel() {
        return this.mMonthYearLabel;
    }

    private void drawDaysOfWeek(Canvas canvas) {
        TextPaint textPaint = this.mDayOfWeekPaint;
        int i = this.mMonthHeight;
        int i2 = this.mDayOfWeekHeight;
        int i3 = this.mCellWidth;
        float ascent = (textPaint.ascent() + textPaint.descent()) / 2.0f;
        int i4 = i + (i2 / 2);
        for (int i5 = 0; i5 < 7; i5++) {
            int i6 = (i3 * i5) + (i3 / 2);
            if (ViewUtils.isLayoutRtl(this)) {
                i6 = this.mPaddedWidth - i6;
            }
            canvas.drawText(this.mDayOfWeekLabels[i5], (float) i6, ((float) i4) - ascent, textPaint);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00a2  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00a4  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00ea  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0104  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void drawDays(android.graphics.Canvas r20) {
        /*
        // Method dump skipped, instructions count: 407
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.picker.SimpleMonthView.drawDays(android.graphics.Canvas):void");
    }

    private int findEndDayOffset() {
        int daysInMonth = 42 - ((getDaysInMonth(this.mMonth, this.mYear) + this.mDayOfWeekStart) - 1);
        if (daysInMonth > 7) {
            return Math.abs(daysInMonth - 14);
        }
        return Math.abs(daysInMonth - 7);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDayEnabled(int i) {
        return i >= this.mEnabledDayStart && i <= this.mEnabledDayEnd;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidDayOfMonth(int i) {
        return i >= 1 && i <= this.mDaysInMonth;
    }

    public void setSelectedDay(int i) {
        this.mActivatedDay = i;
        this.mTouchHelper.invalidateRoot();
        invalidate();
    }

    public void setFirstDayOfWeek(int i) {
        if (isValidDayOfWeek(i)) {
            this.mWeekStart = i;
        } else {
            this.mWeekStart = this.mCalendar.getFirstDayOfWeek();
        }
        updateDayOfWeekLabels();
        this.mTouchHelper.invalidateRoot();
        invalidate();
    }

    /* access modifiers changed from: package-private */
    public void setMonthParams(int i, int i2, int i3, int i4, int i5, int i6) {
        this.mActivatedDay = i;
        if (isValidMonth(i2)) {
            this.mMonth = i2;
        }
        this.mYear = i3;
        this.mCalendar.set(2, this.mMonth);
        this.mCalendar.set(1, this.mYear);
        this.mCalendar.set(5, 1);
        this.mDayOfWeekStart = this.mCalendar.get(7);
        if (isValidDayOfWeek(i4)) {
            this.mWeekStart = i4;
        } else {
            this.mWeekStart = this.mCalendar.getFirstDayOfWeek();
        }
        Calendar instance = Calendar.getInstance();
        this.mToday = -1;
        this.mDaysInMonth = getDaysInMonth(this.mMonth, this.mYear);
        int i7 = 0;
        while (true) {
            int i8 = this.mDaysInMonth;
            if (i7 < i8) {
                i7++;
                if (sameDay(i7, instance)) {
                    this.mToday = i7;
                }
            } else {
                int constrain = MathUtils.constrain(i5, 1, i8);
                this.mEnabledDayStart = constrain;
                this.mEnabledDayEnd = MathUtils.constrain(i6, constrain, this.mDaysInMonth);
                updateMonthYearLabel();
                updateDayOfWeekLabels();
                this.mTouchHelper.invalidateRoot();
                invalidate();
                return;
            }
        }
    }

    private static int getDaysInMonth(int i, int i2) {
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

    private boolean sameDay(int i, Calendar calendar) {
        if (this.mYear == calendar.get(1) && this.mMonth == calendar.get(2) && i == calendar.get(5)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        setMeasuredDimension(View.resolveSize((this.mDesiredCellWidth * 7) + getPaddingStart() + getPaddingEnd(), i), View.resolveSize((this.mDesiredDayHeight * 6) + this.mDesiredDayOfWeekHeight + this.mDesiredMonthHeight + getPaddingTop() + getPaddingBottom(), i2));
    }

    @Override // android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        requestLayout();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (z) {
            int i5 = i3 - i;
            int i6 = i4 - i2;
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();
            int i7 = (i5 - paddingRight) - paddingLeft;
            int i8 = (i6 - paddingBottom) - paddingTop;
            if (i7 != this.mPaddedWidth && i8 != this.mPaddedHeight && i7 >= 0 && i8 >= 0) {
                this.mPaddedWidth = i7;
                this.mPaddedHeight = i8;
                float measuredHeight = ((float) i8) / ((float) ((getMeasuredHeight() - paddingTop) - paddingBottom));
                int i9 = this.mPaddedWidth / 7;
                this.mMonthHeight = (int) (((float) this.mDesiredMonthHeight) * measuredHeight);
                this.mDayOfWeekHeight = (int) (((float) this.mDesiredDayOfWeekHeight) * measuredHeight);
                this.mDayHeight = (int) (((float) this.mDesiredDayHeight) * measuredHeight);
                this.mCellWidth = i9;
                int i10 = i9 / 2;
                Math.min(paddingLeft, paddingRight);
                int i11 = this.mDayHeight / 2;
                this.mDaySelectorRadius = this.mDesiredDaySelectorRadius;
                this.mTouchHelper.invalidateRoot();
            }
        }
    }

    private int findDayOffset() {
        int i = this.mDayOfWeekStart;
        int i2 = this.mWeekStart;
        int i3 = i - i2;
        return i < i2 ? i3 + 7 : i3;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDayAtLocation(int i, int i2) {
        int i3;
        int paddingTop;
        int paddingLeft = i - getPaddingLeft();
        if (paddingLeft < 0 || paddingLeft >= this.mPaddedWidth || (paddingTop = i2 - getPaddingTop()) < (i3 = this.mMonthHeight + this.mDayOfWeekHeight) || paddingTop >= this.mPaddedHeight) {
            return -100;
        }
        if (ViewUtils.isLayoutRtl(this)) {
            paddingLeft = this.mPaddedWidth - paddingLeft;
        }
        return ((((paddingLeft * 7) / this.mPaddedWidth) + (((paddingTop - i3) / this.mDayHeight) * 7)) + 1) - findDayOffset();
    }

    public boolean getBoundsForDay(int i, Rect rect) {
        int i2;
        if (!isValidDayOfMonth(i)) {
            return false;
        }
        int findDayOffset = (i - 1) + findDayOffset();
        int i3 = findDayOffset % 7;
        int i4 = this.mCellWidth;
        if (ViewUtils.isLayoutRtl(this)) {
            i2 = (getWidth() - getPaddingRight()) - ((i3 + 1) * i4);
        } else {
            i2 = getPaddingLeft() + (i3 * i4);
        }
        int i5 = this.mDayHeight;
        int paddingTop = getPaddingTop() + this.mMonthHeight + this.mDayOfWeekHeight + ((findDayOffset / 7) * i5);
        rect.set(i2, paddingTop, i4 + i2, i5 + paddingTop);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean onDayClicked(int i) {
        if (this.mOnDayClickListener != null) {
            Calendar instance = Calendar.getInstance();
            if (i <= 0) {
                int i2 = this.mMonth;
                if (i2 > 0) {
                    int i3 = this.mYear;
                    instance.set(i3, i2 - 1, getDaysInMonth(i2 - 1, i3) + i);
                } else {
                    int i4 = this.mYear;
                    instance.set(i4 - 1, 0, getDaysInMonth(0, i4 - 1) + i);
                }
            } else if (i > getDaysInMonth(this.mMonth, this.mYear)) {
                int i5 = this.mMonth;
                if (i5 < 11) {
                    int i6 = this.mYear;
                    instance.set(i6, i5 + 1, i - getDaysInMonth(i5, i6));
                } else {
                    int i7 = this.mYear;
                    instance.set(i7 + 1, 0, i - getDaysInMonth(i5, i7));
                }
            } else {
                instance.set(this.mYear, this.mMonth, i);
            }
            if (instance.get(1) <= 1900 || instance.get(1) > 2049) {
                return false;
            }
            this.mOnDayClickListener.onDayClick(this, instance);
        }
        this.mTouchHelper.sendEventForVirtualView(i, 1);
        return true;
    }

    @Override // android.view.View
    public PointerIcon onResolvePointerIcon(MotionEvent motionEvent, int i) {
        if (!isEnabled()) {
            return null;
        }
        if (getDayAtLocation((int) (motionEvent.getX() + 0.5f), (int) (motionEvent.getY() + 0.5f)) < 0 || Build.VERSION.SDK_INT < 24) {
            return super.onResolvePointerIcon(motionEvent, i);
        }
        return PointerIcon.getSystemIcon(getContext(), 1002);
    }

    @Override // android.view.View.OnFocusChangeListener
    public void onFocusChange(View view, boolean z) {
        if (!z && !this.mIsTouchHighlighted) {
            this.mPreviouslyHighlightedDay = this.mHighlightedDay;
            this.mHighlightedDay = -1;
            invalidate();
        }
    }

    /* access modifiers changed from: private */
    public class MonthViewTouchHelper extends ExploreByTouchHelper {
        private final Calendar mTempCalendar = Calendar.getInstance();
        private final Rect mTempRect = new Rect();

        public MonthViewTouchHelper(View view) {
            super(view);
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public int getVirtualViewAt(float f, float f2) {
            int dayAtLocation = SimpleMonthView.this.getDayAtLocation((int) (f + 0.5f), (int) (f2 + 0.5f));
            if (dayAtLocation != -1) {
                return dayAtLocation;
            }
            return Integer.MIN_VALUE;
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void getVisibleVirtualViews(List<Integer> list) {
            for (int i = 1; i <= SimpleMonthView.this.mDaysInMonth; i++) {
                list.add(Integer.valueOf(i));
            }
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onPopulateEventForVirtualView(int i, AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.setContentDescription(getDayDescription(i));
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            if (!SimpleMonthView.this.getBoundsForDay(i, this.mTempRect)) {
                this.mTempRect.setEmpty();
                accessibilityNodeInfoCompat.setContentDescription("");
                accessibilityNodeInfoCompat.setBoundsInParent(this.mTempRect);
                accessibilityNodeInfoCompat.setVisibleToUser(false);
                return;
            }
            accessibilityNodeInfoCompat.setText(getDayText(i));
            accessibilityNodeInfoCompat.setContentDescription(getDayDescription(i));
            accessibilityNodeInfoCompat.setBoundsInParent(this.mTempRect);
            boolean isDayEnabled = SimpleMonthView.this.isDayEnabled(i);
            if (isDayEnabled) {
                accessibilityNodeInfoCompat.addAction(16);
            }
            accessibilityNodeInfoCompat.setEnabled(isDayEnabled);
            if (i == SimpleMonthView.this.mActivatedDay) {
                accessibilityNodeInfoCompat.setChecked(true);
            }
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
            if (i2 != 16) {
                return false;
            }
            return SimpleMonthView.this.onDayClicked(i);
        }

        private CharSequence getDayDescription(int i) {
            if (!SimpleMonthView.this.isValidDayOfMonth(i)) {
                return "";
            }
            this.mTempCalendar.set(SimpleMonthView.this.mYear, SimpleMonthView.this.mMonth, i);
            return DateFormat.format("dd MMMM yyyy", this.mTempCalendar.getTimeInMillis());
        }

        private CharSequence getDayText(int i) {
            if (SimpleMonthView.this.isValidDayOfMonth(i)) {
                return SimpleMonthView.this.mDayFormatter.format((long) i);
            }
            return null;
        }
    }
}
