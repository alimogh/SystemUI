package com.oneplus.aod;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.C0005R$dimen;
import com.oneplus.util.OpBatteryUtils;
import com.oneplus.util.OpUtils;
public class OpAodBatteryDashChargeView extends ImageView {
    public OpAodBatteryDashChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpAodBatteryDashChargeView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public OpAodBatteryDashChargeView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        marginLayoutParams.width = OpUtils.convertDpToFixedPx(((ImageView) this).mContext.getResources().getDimension(C0005R$dimen.aod_battery_dash_icon_width));
        marginLayoutParams.height = OpUtils.convertDpToFixedPx(((ImageView) this).mContext.getResources().getDimension(C0005R$dimen.aod_battery_dash_icon_height));
        marginLayoutParams.setMarginStart(OpUtils.convertDpToFixedPx(((ImageView) this).mContext.getResources().getDimension(C0005R$dimen.aod_battery_icon_margin_start)));
        setLayoutParams(marginLayoutParams);
    }

    public void setLevel(int i) {
        setImageResource(getImageResId(i));
    }

    private int getImageResId(int i) {
        return OpBatteryUtils.getDashImageResId(i);
    }
}
