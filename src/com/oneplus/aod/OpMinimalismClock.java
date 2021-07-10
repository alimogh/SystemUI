package com.oneplus.aod;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import java.text.SimpleDateFormat;
import java.util.Date;
public class OpMinimalismClock extends RelativeLayout {
    private String TAG = "OpMinimalismClock";
    private ImageView mHour;
    private ImageView mMin;

    public OpMinimalismClock(Context context) {
        super(context);
        init();
    }

    public OpMinimalismClock(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public OpMinimalismClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {
        Log.d(this.TAG, "init");
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mHour = (ImageView) findViewById(C0008R$id.minimalism_hour);
        this.mMin = (ImageView) findViewById(C0008R$id.minimalism_min);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.i(this.TAG, "onAttachedToWindow");
        refreshTime();
    }

    public void refreshTime() {
        String[] split = new SimpleDateFormat("hh:mm").format(new Date()).toString().split(":");
        int parseInt = Integer.parseInt(split[0]);
        int parseInt2 = Integer.parseInt(split[1]);
        this.mHour.setImageResource(C0006R$drawable.minimalism_hour);
        this.mMin.setImageResource(C0006R$drawable.minimalism_min);
        float f = (float) parseInt2;
        this.mHour.setRotation(((((float) parseInt) * 360.0f) / 12.0f) + ((30.0f * f) / 60.0f));
        this.mMin.setRotation((f * 360.0f) / 60.0f);
    }
}
