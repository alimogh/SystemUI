package com.oneplus.systemui.statusbar.phone;

import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.Log;
import com.android.systemui.C0005R$dimen;
import com.oneplus.util.OpUtils;
public class OpKeyguardClockPositionAlgorithm {
    private int mKeyguardClockY;

    public void opLoadDimens(Resources resources) {
        int dimensionPixelSize = resources.getDimensionPixelSize(C0005R$dimen.fix_op_control_margin_space5) + resources.getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_lock_icon_size) + resources.getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_lock_icon_margin_bottom);
        int i = Resources.getSystem().getDisplayMetrics().widthPixels;
        if (i > 1080) {
            dimensionPixelSize = OpUtils.convertPxByResolutionProportion((float) resources.getDimensionPixelSize(C0005R$dimen.fix_op_control_margin_space5), 1080) + OpUtils.convertPxByResolutionProportion((float) resources.getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_lock_icon_size), 1080) + OpUtils.convertPxByResolutionProportion((float) resources.getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_lock_icon_margin_bottom), 1080);
        }
        this.mKeyguardClockY = dimensionPixelSize;
        Log.d("OpKeyguardClockPositionAlgorithm", "opLoadDimens(), mKeyguardClockY:" + this.mKeyguardClockY + ", opKeyguardClockY:" + dimensionPixelSize + ", margingSpace5: " + resources.getDimensionPixelSize(C0005R$dimen.fix_op_control_margin_space5) + ", lockIconSize: " + resources.getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_lock_icon_size) + ", lockIconMarginBottom: " + resources.getDimensionPixelSize(C0005R$dimen.op_keyguard_clock_info_view_content_lock_icon_margin_bottom) + ", displayWidth:" + i);
    }

    public int opGetMaxClockY() {
        int i = SystemProperties.getInt("debug.opgetmaxclocky.test", 0);
        if (i == 0) {
            return this.mKeyguardClockY;
        }
        Log.d("test", "opGetMaxClockY(), debugTest != 0, debugTest:" + i);
        return i;
    }
}
