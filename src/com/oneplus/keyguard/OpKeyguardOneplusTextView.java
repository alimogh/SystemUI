package com.oneplus.keyguard;

import android.content.Context;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0016R$style;
import com.oneplus.aod.OpClockViewCtrl;
import com.oneplus.util.OpUtils;
public class OpKeyguardOneplusTextView extends TextView {
    private boolean mIsAOD;
    private boolean mIsClock;
    private boolean mIsTimeLineTwo;
    private float mShadowDx;
    private float mShadowDy;
    private float mShadowRadius;
    private int mStrokeColor;
    private float mStrokeWidth;

    public OpKeyguardOneplusTextView(Context context) {
        super(context);
    }

    public OpKeyguardOneplusTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mStrokeWidth = context.getResources().getDimension(C0005R$dimen.op_cb_lockscreen_clock_stroke_width);
        this.mStrokeColor = context.getColor(C0004R$color.op_brand_yellow);
        this.mShadowRadius = context.getResources().getFloat(C0005R$dimen.op_cb_lockscreen_clock_shadow_radius);
        this.mShadowDx = context.getResources().getFloat(C0005R$dimen.op_cb_lockscreen_clock_shadow_dx);
        this.mShadowDy = context.getResources().getFloat(C0005R$dimen.op_cb_lockscreen_clock_shadow_dy);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override // android.widget.TextView
    public void setText(CharSequence charSequence, TextView.BufferType bufferType) {
        TextPaint paint = getPaint();
        if (!isREDVersion() || this.mStrokeWidth <= 0.0f) {
            paint.setStyle(Paint.Style.FILL);
            paint.clearShadowLayer();
            if (this.mIsTimeLineTwo) {
                charSequence = getTextWithOneplusColor(charSequence);
            }
            super.setText(charSequence, TextView.BufferType.SPANNABLE);
            return;
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(this.mStrokeWidth);
        setTextColor(this.mStrokeColor);
        setShadowLayer(this.mShadowRadius, this.mShadowDx, this.mShadowDy, this.mStrokeColor);
        super.setText(charSequence, TextView.BufferType.SPANNABLE);
    }

    public void setIsAOD(boolean z) {
        this.mIsAOD = z;
    }

    public void setIsClockTimeLineTwo(boolean z) {
        this.mIsTimeLineTwo = z;
    }

    private boolean isREDVersion() {
        boolean z = OpClockViewCtrl.getClockStyle() == 50;
        if (!OpUtils.isREDVersion() || this.mIsAOD) {
            return this.mIsAOD && z;
        }
        return true;
    }

    private SpannableString getTextWithOneplusColor(CharSequence charSequence) {
        int color = getContext().getResources().getColor(getContext().getThemeResId() == C0016R$style.Theme_SystemUI_Light ? C0004R$color.op_control_accent_color_red_light : C0004R$color.op_control_accent_color_red_dark);
        String charSequence2 = charSequence != null ? charSequence.toString() : "";
        SpannableString spannableString = new SpannableString(charSequence2);
        int indexOf = charSequence2.indexOf("1");
        while (indexOf >= 0 && indexOf < 2 && charSequence2.charAt(indexOf) != ':' && charSequence2.charAt(indexOf) != 8758) {
            Log.i("OpColorTextView", "index:" + indexOf);
            int i = indexOf + 1;
            spannableString.setSpan(new ForegroundColorSpan(color), indexOf, i, 33);
            indexOf = charSequence2.indexOf("1", i);
        }
        return spannableString;
    }

    public void updataClockView(boolean z) {
        this.mIsClock = z;
        updataClockView();
    }

    public void updataClockView() {
        boolean z = true;
        if (TextUtils.getLayoutDirectionFromLocale(getContext().getResources().getConfiguration().locale) != 1) {
            z = false;
        }
        Log.i("OpColorTextView", "mIsClock:" + this.mIsClock + ", isRtl:" + z);
        if (this.mIsClock) {
            setLayoutDirection(0);
            setGravity((z ? 5 : 3) | 16);
        }
    }
}
