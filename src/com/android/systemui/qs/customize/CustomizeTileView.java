package com.android.systemui.qs.customize;

import android.content.Context;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.tileimpl.QSTileView;
public class CustomizeTileView extends QSTileView {
    private boolean mShowAppLabel;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileBaseView
    public boolean animationsEnabled() {
        return false;
    }

    @Override // android.view.View
    public boolean isLongClickable() {
        return false;
    }

    public CustomizeTileView(Context context, QSIconView qSIconView) {
        super(context, qSIconView);
        this.mRipple = null;
    }

    public void setShowAppLabel(boolean z) {
        this.mShowAppLabel = z;
        this.mSecondLine.setVisibility(z ? 0 : 8);
        this.mLabel.setSingleLine(z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileView, com.android.systemui.qs.tileimpl.QSTileBaseView
    public void handleStateChanged(QSTile.State state) {
        super.handleStateChanged(state);
        this.mSecondLine.setVisibility(this.mShowAppLabel ? 0 : 8);
    }
}
