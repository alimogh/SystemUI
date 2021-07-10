package com.oneplus.battery;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.oneplus.util.OpBatteryUtils;
public class OpBatteryDashChargeView extends ImageView {
    private int mLevel;

    public OpBatteryDashChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpBatteryDashChargeView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public OpBatteryDashChargeView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mLevel = -1;
    }

    public void setIconTint(int i) {
        setImageTintList(ColorStateList.valueOf(i));
    }

    public void updateViews() {
        setImageResource(getImageResId(this.mLevel));
    }

    public void setLevel(int i) {
        this.mLevel = i;
        setImageResource(getImageResId(i));
    }

    private int getImageResId(int i) {
        return OpBatteryUtils.getDashImageResId(i);
    }
}
