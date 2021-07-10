package com.oneplus.aod;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.C0005R$dimen;
import com.oneplus.aod.utils.OpAodDimenHelper;
public class OpIconMerger extends LinearLayout {
    private int mIconHPadding;
    private int mIconSize;
    private View mMoreView;

    public OpIconMerger(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        reloadDimens();
    }

    private void reloadDimens() {
        this.mIconSize = OpAodDimenHelper.convertDpToFixedPx(((LinearLayout) this).mContext, C0005R$dimen.aod_notification_icon_fixed_size);
        this.mIconHPadding = OpAodDimenHelper.convertDpToFixedPx(((LinearLayout) this).mContext, C0005R$dimen.aod_notification_icon_fixed_padding);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        reloadDimens();
    }

    private int getFullIconWidth() {
        return this.mIconSize + this.mIconHPadding;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        checkOverflow(i3 - i);
    }

    private void checkOverflow(int i) {
        if (this.mMoreView != null) {
            int childCount = getChildCount();
            final boolean z = false;
            int i2 = 0;
            for (int i3 = 0; i3 < childCount; i3++) {
                if (getChildAt(i3).getVisibility() != 8) {
                    i2++;
                }
            }
            boolean z2 = this.mMoreView.getVisibility() == 0;
            if (i2 * getFullIconWidth() > i) {
                z = true;
            }
            if (z != z2) {
                post(new Runnable() { // from class: com.oneplus.aod.OpIconMerger.1
                    @Override // java.lang.Runnable
                    public void run() {
                        OpIconMerger.this.mMoreView.setVisibility(z ? 0 : 8);
                    }
                });
            }
        }
    }
}
