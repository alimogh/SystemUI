package com.oneplus.aod;

import android.content.Context;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.oneplus.util.OpUtils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
public class OpAnalogClock extends FrameLayout {
    private int mClockSize;
    private TextView mDateView;
    private Handler mHandler = new Handler();
    private View mHour;
    private View mMin;
    private View mOuter;
    private final Runnable mRunnable = new Runnable() { // from class: com.oneplus.aod.OpAnalogClock.1
        @Override // java.lang.Runnable
        public void run() {
            OpAnalogClock.this.scheduleNext();
        }
    };
    private View mSec;
    private boolean mStartSchedule;
    private int mStyle;

    public OpAnalogClock(Context context) {
        super(context);
    }

    public OpAnalogClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public OpAnalogClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mHour = findViewById(C0008R$id.analog_hour);
        this.mMin = findViewById(C0008R$id.analog_min);
        ImageView imageView = (ImageView) findViewById(C0008R$id.analog_background);
        this.mOuter = findViewById(C0008R$id.analog_outer);
        findViewById(C0008R$id.analog_dot);
        this.mSec = findViewById(C0008R$id.analog_sec);
        this.mDateView = (TextView) findViewById(C0008R$id.analog_date_view);
        loadDimensions();
        updateLayout();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshTime();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        endSchedule();
        super.onDetachedFromWindow();
    }

    public void refreshTime() {
        SimpleDateFormat simpleDateFormat;
        String format = new SimpleDateFormat("hh:mm:ss").format(new Date());
        String[] split = format.toString().split(":");
        int parseInt = Integer.parseInt(split[0]);
        int parseInt2 = Integer.parseInt(split[1]);
        int parseInt3 = Integer.parseInt(split[2]);
        Log.d("OpAnalogClock", "refreshTime: " + ((Object) format) + " hour = " + parseInt + ", min = " + parseInt2 + ", sec = " + parseInt3);
        float f = (float) parseInt2;
        float f2 = ((((float) parseInt) * 360.0f) / 12.0f) + ((30.0f * f) / 60.0f);
        float f3 = (f * 360.0f) / 60.0f;
        View view = this.mHour;
        view.setRotation(view.getRotation());
        View view2 = this.mMin;
        view2.setRotation(view2.getRotation());
        View view3 = this.mOuter;
        view3.setRotation(view3.getRotation());
        View view4 = this.mSec;
        view4.setRotation(view4.getRotation());
        this.mHour.setRotation(f2);
        this.mMin.setRotation(f3);
        this.mOuter.setRotation(f3);
        this.mSec.setRotation((((float) parseInt3) * 360.0f) / 60.0f);
        if (this.mDateView.getVisibility() == 0) {
            Locale locale = Locale.getDefault();
            if (locale.toString().contains("zh_")) {
                simpleDateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "MMMMd").toString(), locale);
            } else {
                simpleDateFormat = new SimpleDateFormat("MMM d");
            }
            this.mDateView.setText(simpleDateFormat.format(new Date()));
        }
    }

    private void updateLayout() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int i = this.mClockSize;
        layoutParams.width = i;
        layoutParams.height = i;
    }

    private void loadDimensions() {
        int i = this.mStyle;
        if (i == 1) {
            this.mClockSize = OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.op_aod_clock_analog_my_size));
        } else if (i == 0 || i == 2) {
            this.mClockSize = OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.clock_analog_size));
        } else if (i == 6) {
            this.mClockSize = OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.aod_clock_analog_numeral_size));
        } else {
            this.mClockSize = OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.aod_clock_analog_min2_size));
        }
    }

    public void endSchedule() {
        this.mHandler.removeCallbacks(this.mRunnable);
        this.mStartSchedule = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleNext() {
        if (!this.mStartSchedule) {
            Log.d("OpAnalogClock", "end schedule, do not schedule next");
            return;
        }
        refreshTime();
        int i = 1000 - Calendar.getInstance().get(14);
        Log.d("OpAnalogClock", "scheduleNext: " + i);
        this.mHandler.postDelayed(this.mRunnable, (long) i);
    }
}
