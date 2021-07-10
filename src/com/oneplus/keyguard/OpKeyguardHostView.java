package com.oneplus.keyguard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardSecurityContainer;
import com.android.systemui.Dependency;
import com.android.systemui.recents.OverviewProxyService;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpReflectionUtils;
public class OpKeyguardHostView extends FrameLayout {
    protected View mKeyguardSecurityNavigationSpace;
    private boolean mNavigationbarHide = false;

    public OpKeyguardHostView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        OverviewProxyService overviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
    }

    public void showMessage(CharSequence charSequence, ColorStateList colorStateList, int i) {
        if (getSecurityContainer() != null) {
            getSecurityContainer().showMessage(charSequence, colorStateList, i);
        }
    }

    private KeyguardSecurityContainer getSecurityContainer() {
        return (KeyguardSecurityContainer) OpReflectionUtils.getValue(KeyguardHostView.class, this, "mSecurityContainer");
    }

    /* access modifiers changed from: protected */
    public void updateNavigationSpace() {
        onHideNavBar(this.mNavigationbarHide);
    }

    public void onHideNavBar(boolean z) {
        this.mNavigationbarHide = z;
        if (this.mKeyguardSecurityNavigationSpace != null) {
            Log.i("OpKeyguardHostView", "onHideNavBar:" + z + ", " + isGestureNavigationBarMode());
            if (z || isGestureNavigationBarMode()) {
                int navigationSpaceHeight = getNavigationSpaceHeight();
                this.mKeyguardSecurityNavigationSpace.setVisibility(0);
                ViewGroup.LayoutParams layoutParams = this.mKeyguardSecurityNavigationSpace.getLayoutParams();
                layoutParams.height = navigationSpaceHeight;
                this.mKeyguardSecurityNavigationSpace.setLayoutParams(layoutParams);
                Log.i("OpKeyguardHostView", "onHideNavBar setVisibility VISIBLE, height:" + navigationSpaceHeight);
                return;
            }
            Log.i("OpKeyguardHostView", "onHideNavBar setVisibility GONE:");
            this.mKeyguardSecurityNavigationSpace.setVisibility(8);
        }
    }

    private boolean isGestureNavigationBarMode() {
        return Settings.Secure.getInt(getContext().getContentResolver(), "navigation_mode", 0) != 0;
    }

    private int getNavigationSpaceHeight() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(17105324);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(17105327);
        Log.i("OpKeyguardHostView", "getNavigationSpaceHeight:" + dimensionPixelSize + ", " + dimensionPixelSize2);
        return this.mNavigationbarHide ? dimensionPixelSize : dimensionPixelSize - dimensionPixelSize2;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean fitSystemWindows(Rect rect) {
        if (!OpLsState.getInstance().getPhoneStatusBar().isAppFullScreen()) {
            return super.fitSystemWindows(rect);
        }
        setPaddingRelative(getPaddingStart(), 0, getPaddingEnd(), 0);
        Log.d("OpKeyguardHostView", "fitSystemWindows: isAppFullScreen.");
        return false;
    }

    public void resetFlipperY() {
        if (getSecurityContainer() != null) {
            getSecurityContainer().resetFlipperY();
        }
    }
}
