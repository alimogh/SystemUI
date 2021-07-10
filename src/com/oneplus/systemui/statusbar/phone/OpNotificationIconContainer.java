package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.statusbar.AlphaOptimizedFrameLayout;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpUtils;
public class OpNotificationIconContainer extends AlphaOptimizedFrameLayout {
    private Context mContext;
    protected int mIconPadding;
    protected boolean mRemoveWithoutAnimation;

    public OpNotificationIconContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public void initDimensInternal() {
        this.mIconPadding = getResources().getDimensionPixelSize(C0005R$dimen.status_bar_notification_icon_padding);
    }

    /* access modifiers changed from: protected */
    public int setOverflowWidth(int i, int i2, int i3) {
        if (getMaxDots() > 0) {
            return i + ((getMaxDots() - 1) * (i2 + i3));
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getMaxDots() {
        return OpUtils.getMaxDotsForNotificationIconContainer(this.mContext);
    }

    /* access modifiers changed from: protected */
    public boolean onKeyguard() {
        StatusBarKeyguardViewManager statusBarKeyguardViewManager = OpLsState.getInstance().getStatusBarKeyguardViewManager();
        return statusBarKeyguardViewManager.isShowing() && !statusBarKeyguardViewManager.isBouncerShowing();
    }
}
