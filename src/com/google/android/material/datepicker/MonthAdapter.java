package com.google.android.material.datepicker;

import android.content.Context;
import android.widget.BaseAdapter;
/* access modifiers changed from: package-private */
public class MonthAdapter extends BaseAdapter {
    static final int MAXIMUM_WEEKS = UtcDates.getUtcCalendar().getMaximum(4);
    final CalendarConstraints calendarConstraints;
    CalendarStyle calendarStyle;
    final DateSelector<?> dateSelector;
    final Month month;

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public boolean hasStableIds() {
        return true;
    }

    MonthAdapter(Month month, DateSelector<?> dateSelector, CalendarConstraints calendarConstraints) {
        this.month = month;
        this.dateSelector = dateSelector;
        this.calendarConstraints = calendarConstraints;
    }

    @Override // android.widget.Adapter
    public Long getItem(int i) {
        if (i < this.month.daysFromStartOfWeekToFirstOfMonth() || i > lastPositionInMonth()) {
            return null;
        }
        return Long.valueOf(this.month.getDay(positionToDay(i)));
    }

    @Override // android.widget.Adapter
    public long getItemId(int i) {
        return (long) (i / this.month.daysInWeek);
    }

    @Override // android.widget.Adapter
    public int getCount() {
        return this.month.daysInMonth + firstPositionInMonth();
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x006f A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0070  */
    @Override // android.widget.Adapter
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.widget.TextView getView(int r6, android.view.View r7, android.view.ViewGroup r8) {
        /*
            r5 = this;
            android.content.Context r0 = r8.getContext()
            r5.initializeStyles(r0)
            r0 = r7
            android.widget.TextView r0 = (android.widget.TextView) r0
            r1 = 0
            if (r7 != 0) goto L_0x001e
            android.content.Context r7 = r8.getContext()
            android.view.LayoutInflater r7 = android.view.LayoutInflater.from(r7)
            int r0 = com.google.android.material.R$layout.mtrl_calendar_day
            android.view.View r7 = r7.inflate(r0, r8, r1)
            r0 = r7
            android.widget.TextView r0 = (android.widget.TextView) r0
        L_0x001e:
            int r7 = r5.firstPositionInMonth()
            int r7 = r6 - r7
            r8 = 1
            if (r7 < 0) goto L_0x0061
            com.google.android.material.datepicker.Month r2 = r5.month
            int r3 = r2.daysInMonth
            if (r7 < r3) goto L_0x002e
            goto L_0x0061
        L_0x002e:
            int r7 = r7 + r8
            r0.setTag(r2)
            java.lang.String r2 = java.lang.String.valueOf(r7)
            r0.setText(r2)
            com.google.android.material.datepicker.Month r2 = r5.month
            long r2 = r2.getDay(r7)
            com.google.android.material.datepicker.Month r7 = r5.month
            int r7 = r7.year
            com.google.android.material.datepicker.Month r4 = com.google.android.material.datepicker.Month.today()
            int r4 = r4.year
            if (r7 != r4) goto L_0x0053
            java.lang.String r7 = com.google.android.material.datepicker.DateStrings.getMonthDayOfWeekDay(r2)
            r0.setContentDescription(r7)
            goto L_0x005a
        L_0x0053:
            java.lang.String r7 = com.google.android.material.datepicker.DateStrings.getYearMonthDayOfWeekDay(r2)
            r0.setContentDescription(r7)
        L_0x005a:
            r0.setVisibility(r1)
            r0.setEnabled(r8)
            goto L_0x0069
        L_0x0061:
            r7 = 8
            r0.setVisibility(r7)
            r0.setEnabled(r1)
        L_0x0069:
            java.lang.Long r6 = r5.getItem(r6)
            if (r6 != 0) goto L_0x0070
            return r0
        L_0x0070:
            com.google.android.material.datepicker.CalendarConstraints r7 = r5.calendarConstraints
            com.google.android.material.datepicker.CalendarConstraints$DateValidator r7 = r7.getDateValidator()
            long r2 = r6.longValue()
            boolean r7 = r7.isValid(r2)
            if (r7 == 0) goto L_0x00d5
            r0.setEnabled(r8)
            com.google.android.material.datepicker.DateSelector<?> r7 = r5.dateSelector
            java.util.Collection r7 = r7.getSelectedDays()
            java.util.Iterator r7 = r7.iterator()
        L_0x008d:
            boolean r8 = r7.hasNext()
            if (r8 == 0) goto L_0x00b5
            java.lang.Object r8 = r7.next()
            java.lang.Long r8 = (java.lang.Long) r8
            long r1 = r8.longValue()
            long r3 = r6.longValue()
            long r3 = com.google.android.material.datepicker.UtcDates.canonicalYearMonthDay(r3)
            long r1 = com.google.android.material.datepicker.UtcDates.canonicalYearMonthDay(r1)
            int r8 = (r3 > r1 ? 1 : (r3 == r1 ? 0 : -1))
            if (r8 != 0) goto L_0x008d
            com.google.android.material.datepicker.CalendarStyle r5 = r5.calendarStyle
            com.google.android.material.datepicker.CalendarItemStyle r5 = r5.selectedDay
            r5.styleItem(r0)
            return r0
        L_0x00b5:
            java.util.Calendar r7 = com.google.android.material.datepicker.UtcDates.getTodayCalendar()
            long r7 = r7.getTimeInMillis()
            long r1 = r6.longValue()
            int r6 = (r7 > r1 ? 1 : (r7 == r1 ? 0 : -1))
            if (r6 != 0) goto L_0x00cd
            com.google.android.material.datepicker.CalendarStyle r5 = r5.calendarStyle
            com.google.android.material.datepicker.CalendarItemStyle r5 = r5.todayDay
            r5.styleItem(r0)
            return r0
        L_0x00cd:
            com.google.android.material.datepicker.CalendarStyle r5 = r5.calendarStyle
            com.google.android.material.datepicker.CalendarItemStyle r5 = r5.day
            r5.styleItem(r0)
            return r0
        L_0x00d5:
            r0.setEnabled(r1)
            com.google.android.material.datepicker.CalendarStyle r5 = r5.calendarStyle
            com.google.android.material.datepicker.CalendarItemStyle r5 = r5.invalidDay
            r5.styleItem(r0)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.datepicker.MonthAdapter.getView(int, android.view.View, android.view.ViewGroup):android.widget.TextView");
    }

    private void initializeStyles(Context context) {
        if (this.calendarStyle == null) {
            this.calendarStyle = new CalendarStyle(context);
        }
    }

    /* access modifiers changed from: package-private */
    public int firstPositionInMonth() {
        return this.month.daysFromStartOfWeekToFirstOfMonth();
    }

    /* access modifiers changed from: package-private */
    public int lastPositionInMonth() {
        return (this.month.daysFromStartOfWeekToFirstOfMonth() + this.month.daysInMonth) - 1;
    }

    /* access modifiers changed from: package-private */
    public int positionToDay(int i) {
        return (i - this.month.daysFromStartOfWeekToFirstOfMonth()) + 1;
    }

    /* access modifiers changed from: package-private */
    public int dayToPosition(int i) {
        return firstPositionInMonth() + (i - 1);
    }

    /* access modifiers changed from: package-private */
    public boolean withinMonth(int i) {
        return i >= firstPositionInMonth() && i <= lastPositionInMonth();
    }

    /* access modifiers changed from: package-private */
    public boolean isFirstInRow(int i) {
        return i % this.month.daysInWeek == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isLastInRow(int i) {
        return (i + 1) % this.month.daysInWeek == 0;
    }
}
