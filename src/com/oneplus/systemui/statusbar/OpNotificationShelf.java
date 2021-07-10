package com.oneplus.systemui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.oneplus.util.OpUtils;
public class OpNotificationShelf extends ActivatableNotificationView {
    protected boolean mHasItemsInStableShelf;
    protected boolean mInteractive;
    protected int mStatusBarState;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public View getContentView() {
        return null;
    }

    static {
        boolean z = OpUtils.DEBUG_ONEPLUS;
    }

    public OpNotificationShelf(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setIsNotificationShelf(true);
    }

    /* access modifiers changed from: protected */
    public void updateInteractivenessInternal() {
        int i = 1;
        boolean z = this.mStatusBarState == 1 && this.mHasItemsInStableShelf;
        setClickable(this.mHasItemsInStableShelf);
        setFocusable(z);
        this.mInteractive = z;
        if (!z) {
            i = 4;
        }
        setImportantForAccessibility(i);
    }
}
