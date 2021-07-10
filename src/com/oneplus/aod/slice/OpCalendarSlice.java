package com.oneplus.aod.slice;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0013R$plurals;
import com.oneplus.aod.slice.OpSliceManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
public class OpCalendarSlice extends OpSlice {
    private static final String[] EVENT_PROJECTION = {"_id", "title", "allDay", "dtstart", "eventLocation"};
    private Calendar mCalendar = null;
    private ContentResolver mContentResolver;
    private Context mContext;
    private CalendarEvent mEvent = null;
    private Uri mEventUri;
    private Uri mReminderUri;

    public OpCalendarSlice(Context context, OpSliceManager.Callback callback) {
        super(callback);
        this.mIcon = C0006R$drawable.op_aod_slice_calendar;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.mCalendar = Calendar.getInstance();
        this.mEventUri = CalendarContract.Events.CONTENT_URI;
        this.mReminderUri = CalendarContract.Reminders.CONTENT_URI;
    }

    private void updateEvent() {
        getSoonestEvent();
        CalendarEvent calendarEvent = this.mEvent;
        if (calendarEvent != null) {
            this.mPrimary = calendarEvent.mTitle;
            this.mSecondary = calendarEvent.mLocation;
            int eventIntervalInMinutes = calendarEvent.getEventIntervalInMinutes();
            this.mRemark = this.mContext.getResources().getQuantityString(C0013R$plurals.smart_aod_calendar_remain_time, eventIntervalInMinutes, Integer.valueOf(eventIntervalInMinutes));
            if (OpSlice.DEBUG) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                this.mCalendar.setTimeInMillis(this.mEvent.mDateStartTimeInMillis);
                String format = simpleDateFormat.format(this.mCalendar.getTime());
                this.mCalendar.setTimeInMillis(this.mEvent.mReminderTimeInMillis);
                String format2 = simpleDateFormat.format(this.mCalendar.getTime());
                String str = this.mTag;
                Log.i(str, "Event time = " + format + ", reminderTime = " + format2 + ", title = " + this.mPrimary + ", location = " + this.mSecondary);
            }
            if (this.mEvent.getEventIntervalInMinutes() <= 0 || this.mEvent.getEventIntervalInMinutes() > 45) {
                setActive(false);
                return;
            }
            setActive(true);
            updateUI();
            return;
        }
        setActive(false);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.slice.OpSlice
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (z) {
            updateEvent();
        }
    }

    @Override // com.oneplus.aod.slice.OpSlice
    public void handleTimeChanged() {
        super.handleTimeChanged();
        updateEvent();
    }

    private void getSoonestEvent() {
        int i;
        Cursor cursor = null;
        this.mEvent = null;
        try {
            cursor = this.mContentResolver.query(this.mEventUri, EVENT_PROJECTION, "((hasAlarm = 1) AND (dtstart >= ?) AND (deleted != 1))", new String[]{String.valueOf(Calendar.getInstance().getTimeInMillis())}, "dtstart LIMIT 1");
            if (!(cursor == null || cursor.getCount() == 0)) {
                if (cursor.moveToNext()) {
                    i = cursor.getInt(cursor.getColumnIndex("_id"));
                    this.mEvent = new CalendarEvent(this, i, cursor.getString(cursor.getColumnIndex("title")), cursor.getLong(cursor.getColumnIndex("dtstart")), cursor.getString(cursor.getColumnIndex("eventLocation")));
                } else {
                    i = -1;
                }
                if (i != -1) {
                    getReminder(i);
                }
            }
            if (cursor == null) {
                return;
            }
        } catch (Exception e) {
            Log.w(this.mTag, "getSoonestEvent occur exception", e);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
    }

    private void getReminder(int i) {
        String[] strArr = {String.valueOf(i)};
        Cursor cursor = null;
        try {
            cursor = this.mContentResolver.query(this.mReminderUri, null, "(event_id = ?)", strArr, "_id");
            if (!(cursor == null || cursor.getCount() == 0)) {
                int i2 = cursor.moveToNext() ? cursor.getInt(cursor.getColumnIndex("minutes")) : -1;
                if (!(i2 == -1 || this.mEvent == null)) {
                    this.mEvent.setReminderInMinutes(i2);
                }
            }
            if (cursor == null) {
                return;
            }
        } catch (Exception e) {
            Log.w(this.mTag, "getReminder occur exception", e);
            if (0 == 0) {
                return;
            }
        } catch (Throwable th) {
            if (0 != 0) {
                cursor.close();
            }
            throw th;
        }
        cursor.close();
    }

    /* access modifiers changed from: private */
    public class CalendarEvent {
        public long mDateStartTimeInMillis;
        public String mLocation;
        public long mReminderTimeInMillis;
        public String mTitle;

        public CalendarEvent(OpCalendarSlice opCalendarSlice, int i, String str, long j, String str2) {
            this.mTitle = str;
            this.mDateStartTimeInMillis = j;
            this.mLocation = str2;
        }

        public void setReminderInMinutes(int i) {
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(this.mDateStartTimeInMillis);
            instance.add(12, 0 - i);
            this.mReminderTimeInMillis = instance.getTimeInMillis();
        }

        public int getEventIntervalInMinutes() {
            long timeInMillis = this.mDateStartTimeInMillis - Calendar.getInstance().getTimeInMillis();
            if (timeInMillis <= 0) {
                return 0;
            }
            return ((int) (timeInMillis / 60000)) + 1;
        }
    }
}
