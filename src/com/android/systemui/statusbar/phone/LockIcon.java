package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import com.android.internal.graphics.ColorUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0000R$anim;
import com.android.systemui.C0016R$style;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
public class LockIcon extends KeyguardAffordanceView {
    private static final int[][] LOCK_ANIM_RES_IDS = {new int[]{C0000R$anim.lock_to_error, C0000R$anim.lock_unlock, C0000R$anim.lock_lock, C0000R$anim.lock_scanning}, new int[]{C0000R$anim.lock_to_error_circular, C0000R$anim.lock_unlock_circular, C0000R$anim.lock_lock_circular, C0000R$anim.lock_scanning_circular}, new int[]{C0000R$anim.lock_to_error_filled, C0000R$anim.lock_unlock_filled, C0000R$anim.lock_lock_filled, C0000R$anim.lock_scanning_filled}, new int[]{C0000R$anim.lock_to_error_rounded, C0000R$anim.lock_unlock_rounded, C0000R$anim.lock_lock_rounded, C0000R$anim.lock_scanning_rounded}};
    private static final String TAG = LockIcon.class.getSimpleName();
    private float mDozeAmount;
    private final SparseArray<Drawable> mDrawableCache = new SparseArray<>();
    private int mIconColor;

    public LockIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mDrawableCache.clear();
    }

    /* access modifiers changed from: package-private */
    public void setDozeAmount(float f) {
        this.mDozeAmount = f;
        updateDarkTint();
    }

    /* access modifiers changed from: package-private */
    public void onThemeChange(int i) {
        this.mDrawableCache.clear();
        this.mIconColor = i;
        updateDarkTint();
        boolean z = getContext().getThemeResId() == C0016R$style.Theme_SystemUI_Light;
        if (z) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.9f);
        }
        if (OpUtils.DEBUG_ONEPLUS) {
            String str = TAG;
            Log.i(str, "onThemeChange, lightWpTheme:" + z + ", getAlpha():" + getAlpha());
        }
    }

    public void updateDarkTint() {
        int blendARGB = ColorUtils.blendARGB(this.mIconColor, -1, this.mDozeAmount);
        String str = TAG;
        Log.i(str, " updateDarkTint state:" + KeyguardUpdateMonitor.getInstance(((ImageView) this).mContext).isCameraErrorState());
        if (KeyguardUpdateMonitor.getInstance(((ImageView) this).mContext).isCameraErrorState()) {
            blendARGB = Color.parseColor("#FF5236");
        }
        if (OpUtils.isREDVersion()) {
            blendARGB = ThemeColorUtils.getColor(100);
        }
        setImageTintList(ColorStateList.valueOf(blendARGB));
    }
}
