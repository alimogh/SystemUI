package com.oneplus.systemui.biometrics;

import android.content.Context;
import com.android.systemui.C0005R$dimen;
import com.oneplus.util.OpUtils;
public class OpFodViewSettings {
    public static int getFodAnimViewY(Context context) {
        int i;
        if (OpUtils.isSupportCustomFingerprintType2()) {
            i = C0005R$dimen.op_biometric_animation_view_ss_y;
        } else if (!OpUtils.isSupportResolutionSwitch(context)) {
            i = C0005R$dimen.op_biometric_animation_view_y;
        } else if (OpUtils.is2KResolution()) {
            i = C0005R$dimen.op_biometric_animation_view_y_2k;
        } else {
            i = C0005R$dimen.op_biometric_animation_view_y_1080p;
        }
        return context.getResources().getDimensionPixelSize(i);
    }

    public static int getFodAnimViewHeight(Context context) {
        int i;
        if (!OpUtils.isSupportResolutionSwitch(context)) {
            i = C0005R$dimen.fp_animation_height;
        } else if (OpUtils.is2KResolution()) {
            i = C0005R$dimen.fp_animation_height_2k;
        } else {
            i = C0005R$dimen.fp_animation_height_1080p;
        }
        return context.getResources().getDimensionPixelSize(i);
    }

    public static int getFodHighlightY(Context context) {
        int i;
        if (OpUtils.isSupportCustomFingerprintType2()) {
            i = C0005R$dimen.op_biometric_icon_flash_ss_location_y;
        } else if (!OpUtils.isSupportResolutionSwitch(context)) {
            i = C0005R$dimen.op_biometric_icon_flash_location_y;
        } else if (OpUtils.is2KResolution()) {
            i = C0005R$dimen.op_biometric_icon_flash_location_y_2k;
        } else {
            i = C0005R$dimen.op_biometric_icon_flash_location_y_1080p;
        }
        return context.getResources().getDimensionPixelSize(i);
    }

    public static int getFodHighlightSize(Context context) {
        int i;
        if (OpUtils.isSupportCustomFingerprintType2()) {
            i = C0005R$dimen.op_biometric_icon_flash_ss_width;
        } else if (!OpUtils.isSupportResolutionSwitch(context)) {
            i = C0005R$dimen.op_biometric_icon_flash_width;
        } else if (OpUtils.is2KResolution()) {
            i = C0005R$dimen.op_biometric_icon_flash_width_2k;
        } else {
            i = C0005R$dimen.op_biometric_icon_flash_width_1080p;
        }
        return context.getResources().getDimensionPixelSize(i);
    }

    public static int getFodIconSize(Context context) {
        int i;
        if (OpUtils.is2KResolution()) {
            i = C0005R$dimen.op_biometric_icon_normal_width_2k;
        } else {
            i = C0005R$dimen.op_biometric_icon_normal_width_1080p;
        }
        return context.getResources().getDimensionPixelSize(i);
    }
}
