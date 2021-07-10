package com.oneplus.aod.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.internal.R;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.R$styleable;
import com.oneplus.aod.utils.OpAodSettings;
import java.text.SimpleDateFormat;
import java.util.Calendar;
public class OpAnalogClock extends FrameLayout implements IOpAodClock {
    private ImageView mBackground;
    private View mDotView;
    private int mHeightId;
    private View mHourView;
    private View mMinView;
    private View mOuterView;
    private View mSecView;
    private int mWidthId;

    public OpAnalogClock(Context context) {
        this(context, null);
    }

    public OpAnalogClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpAnalogClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        initViews(((LayoutInflater) context.getSystemService("layout_inflater")).inflate(getLayoutId(), this));
        setupAttributes(attributeSet);
        setLayoutParams(new FrameLayout.LayoutParams(context, attributeSet));
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void applyLayoutParams(OpAodSettings.OpViewInfo opViewInfo) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        if (layoutParams != null && opViewInfo != null) {
            layoutParams.width = opViewInfo.getSize(((FrameLayout) this).mContext, this.mWidthId);
            layoutParams.height = opViewInfo.getSize(((FrameLayout) this).mContext, this.mHeightId);
            layoutParams.setMarginStart(opViewInfo.getMarginStart(((FrameLayout) this).mContext));
            layoutParams.setMarginEnd(opViewInfo.getMarginEnd(((FrameLayout) this).mContext));
            layoutParams.topMargin = opViewInfo.getMarginTop(((FrameLayout) this).mContext);
            layoutParams.bottomMargin = opViewInfo.getMarginBottom(((FrameLayout) this).mContext);
        }
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void supportSeconds(boolean z) {
        this.mSecView.setVisibility(z ? 0 : 8);
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onTimeChanged(Calendar calendar) {
        String format = new SimpleDateFormat("hh:mm:ss").format(calendar.getTime());
        String[] split = format.toString().split(":");
        int parseInt = Integer.parseInt(split[0]);
        int parseInt2 = Integer.parseInt(split[1]);
        int parseInt3 = Integer.parseInt(split[2]);
        Log.d("OpAnalogClock", "refreshTime: " + ((Object) format) + " hour = " + parseInt + ", min = " + parseInt2 + ", sec = " + parseInt3);
        float f = (float) parseInt2;
        setRotation(((((float) parseInt) * 360.0f) / 12.0f) + ((30.0f * f) / 60.0f), (f * 360.0f) / 60.0f, (((float) parseInt3) * 360.0f) / 60.0f);
    }

    /* access modifiers changed from: protected */
    public int getLayoutId() {
        return C0011R$layout.op_analog_clock;
    }

    /* access modifiers changed from: protected */
    public void initViews(View view) {
        this.mBackground = (ImageView) view.findViewById(C0008R$id.analog_background);
        this.mHourView = view.findViewById(C0008R$id.analog_hour);
        this.mMinView = view.findViewById(C0008R$id.analog_min);
        this.mSecView = view.findViewById(C0008R$id.analog_sec);
        this.mDotView = view.findViewById(C0008R$id.analog_dot);
        this.mOuterView = view.findViewById(C0008R$id.analog_outer);
    }

    /* access modifiers changed from: protected */
    public void setRotation(float f, float f2, float f3) {
        this.mHourView.setRotation(f);
        this.mMinView.setRotation(f2);
        this.mOuterView.setRotation(f2);
        this.mSecView.setRotation(f3);
    }

    /* access modifiers changed from: protected */
    public void setupAttributes(AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = ((FrameLayout) this).mContext.obtainStyledAttributes(attributeSet, R$styleable.OpAnalogClock, 0, 0);
        this.mBackground.setImageResource(obtainStyledAttributes.getResourceId(R$styleable.OpAnalogClock_bg, 0));
        this.mHourView.setBackgroundResource(obtainStyledAttributes.getResourceId(R$styleable.OpAnalogClock_hour, 0));
        this.mMinView.setBackgroundResource(obtainStyledAttributes.getResourceId(R$styleable.OpAnalogClock_minutes, 0));
        this.mSecView.setBackgroundResource(obtainStyledAttributes.getResourceId(R$styleable.OpAnalogClock_sec, 0));
        this.mDotView.setBackgroundResource(obtainStyledAttributes.getResourceId(R$styleable.OpAnalogClock_dot, 0));
        this.mOuterView.setBackgroundResource(obtainStyledAttributes.getResourceId(R$styleable.OpAnalogClock_outer, 0));
        obtainStyledAttributes.recycle();
        TypedArray obtainStyledAttributes2 = ((FrameLayout) this).mContext.obtainStyledAttributes(attributeSet, R.styleable.ViewGroup_MarginLayout, 0, 0);
        this.mWidthId = obtainStyledAttributes2.getResourceId(0, -1);
        this.mHeightId = obtainStyledAttributes2.getResourceId(1, -1);
        obtainStyledAttributes2.recycle();
    }
}
