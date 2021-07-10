package com.oneplus.systemui.statusbar.phone;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.PanelBar;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;
import com.android.systemui.statusbar.phone.StatusIconContainer;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintController;
import com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector;
import com.oneplus.util.OpReflectionUtils;
import com.oneplus.util.OpUtils;
public class OpPhoneStatusBarView extends PanelBar implements OpHighlightHintController.OnHighlightHintStateChangeListener, OpScreenBurnInProtector.OnBurnInPreventListener {
    private View mHighlightHintView;
    private View mNotifications;
    private View mStatusBarContentLeft;
    private View mStatusBarLeftSide;
    private StatusIconContainer mStatusIconContainer;
    private ViewGroup mStatusbarContent;
    private View mSystemIcons;

    @Override // com.android.systemui.statusbar.phone.PanelBar
    public void panelScrimMinFractionChanged(float f) {
    }

    public OpPhoneStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.systemui.statusbar.phone.PanelBar, android.view.View
    public void onFinishInflate() {
        View view;
        super.onFinishInflate();
        this.mSystemIcons = findViewById(C0008R$id.statusIcons);
        this.mNotifications = findViewById(C0008R$id.notification_icon_area);
        this.mStatusBarContentLeft = findViewById(C0008R$id.status_bar_contents_left);
        this.mStatusbarContent = (ViewGroup) findViewById(C0008R$id.status_bar_contents);
        this.mStatusBarLeftSide = findViewById(C0008R$id.status_bar_left_side);
        StatusIconContainer statusIconContainer = (StatusIconContainer) findViewById(C0008R$id.statusIcons);
        this.mStatusIconContainer = statusIconContainer;
        statusIconContainer.setOpTag("status_icon_container");
        if (!OpUtils.isSupportCustomStatusBar() || OpUtils.isSupportHolePunchFrontCam() || OpUtils.isCutoutHide(getContext())) {
            View view2 = this.mStatusBarLeftSide;
            if (view2 != null) {
                View findViewById = view2.findViewById(C0008R$id.clock);
                if (findViewById.getParent() != null) {
                    ((ViewGroup) findViewById.getParent()).removeView(findViewById);
                }
                View findViewById2 = this.mStatusBarLeftSide.findViewById(C0008R$id.highlighthintview);
                if (findViewById2.getParent() != null) {
                    ((ViewGroup) findViewById2.getParent()).removeView(findViewById2);
                }
                if (OpUtils.isSupportHolePunchFrontCam()) {
                    View findViewById3 = this.mStatusbarContent.findViewById(C0008R$id.highlighthintview_right);
                    if (findViewById3.getParent() != null) {
                        ((ViewGroup) findViewById3.getParent()).removeView(findViewById3);
                    }
                    this.mHighlightHintView = findViewById(C0008R$id.highlighthintview_left);
                } else {
                    View findViewById4 = this.mStatusbarContent.findViewById(C0008R$id.highlighthintview_left);
                    if (findViewById4.getParent() != null) {
                        ((ViewGroup) findViewById4.getParent()).removeView(findViewById4);
                    }
                    this.mHighlightHintView = findViewById(C0008R$id.highlighthintview_right);
                }
            }
        } else {
            View view3 = this.mStatusBarContentLeft;
            view3.setPaddingRelative(view3.getPaddingStart(), this.mStatusBarContentLeft.getPaddingTop(), 0, this.mStatusBarContentLeft.getPaddingBottom());
            ViewGroup viewGroup = this.mStatusbarContent;
            if (viewGroup != null) {
                View findViewById5 = viewGroup.findViewById(C0008R$id.clock);
                if (findViewById5.getParent() != null) {
                    ((ViewGroup) findViewById5.getParent()).removeView(findViewById5);
                }
                View findViewById6 = this.mStatusbarContent.findViewById(C0008R$id.highlighthintview_left);
                if (findViewById6.getParent() != null) {
                    ((ViewGroup) findViewById6.getParent()).removeView(findViewById6);
                }
                View findViewById7 = this.mStatusbarContent.findViewById(C0008R$id.highlighthintview_right);
                if (findViewById7.getParent() != null) {
                    ((ViewGroup) findViewById7.getParent()).removeView(findViewById7);
                }
                this.mHighlightHintView = findViewById(C0008R$id.highlighthintview);
            }
        }
        if (!((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).showOvalLayout() && (view = this.mStatusBarContentLeft) != null) {
            View findViewById8 = view.findViewById(C0008R$id.highlighthintview);
            if (findViewById8.getParent() != null) {
                ((ViewGroup) findViewById8.getParent()).removeView(findViewById8);
            }
        }
        View view4 = this.mHighlightHintView;
        if (view4 != null) {
            view4.setTag(1000);
        }
        OpReflectionUtils.setValue(PhoneStatusBarView.class, this, "mAppOps", (AppOpsManager) getContext().getSystemService("appops"));
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController.OnHighlightHintStateChangeListener
    public void onHighlightHintStateChange() {
        OpHighlightHintController opHighlightHintController = (OpHighlightHintController) Dependency.get(OpHighlightHintController.class);
        boolean isHighLightHintShow = opHighlightHintController.isHighLightHintShow();
        int i = 8;
        if (opHighlightHintController.showOvalLayout()) {
            this.mSystemIcons.requestLayout();
            View view = this.mNotifications;
            if (!isHighLightHintShow) {
                i = 0;
            }
            view.setVisibility(i);
        } else if (isHighLightHintShow) {
            this.mStatusBarContentLeft.setVisibility(8);
            this.mSystemIcons.setVisibility(4);
        } else {
            this.mStatusBarContentLeft.setVisibility(0);
            this.mSystemIcons.setVisibility(0);
        }
    }

    public int getHighlightHintWidth() {
        View view;
        if (!((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isHighLightHintShow() || (view = this.mHighlightHintView) == null) {
            return 0;
        }
        return view.getWidth();
    }

    /* access modifiers changed from: protected */
    public void updateStatusBarPadding() {
        int i;
        boolean isHolePunchCutoutHide = OpUtils.isHolePunchCutoutHide(getContext());
        boolean z = true;
        if (!OpUtils.isSupportHolePunchFrontCam() || getContext().getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        if (isHolePunchCutoutHide) {
            i = 0;
        } else {
            i = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.op_status_bar_cust_padding_top, 1080);
        }
        setPadding(getPaddingLeft(), i, getPaddingRight(), 0);
        int dimensionPixelSize = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.status_bar_padding_start, 1080);
        int dimensionPixelSize2 = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.status_bar_padding_end, 1080);
        if (isHolePunchCutoutHide) {
            dimensionPixelSize = dimensionPixelSize2;
        }
        if (this.mStatusbarContent != null) {
            int i2 = z ? dimensionPixelSize2 : dimensionPixelSize;
            if (z) {
                dimensionPixelSize2 = dimensionPixelSize;
            }
            ViewGroup viewGroup = this.mStatusbarContent;
            viewGroup.setPaddingRelative(i2, viewGroup.getPaddingTop(), dimensionPixelSize2, 0);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateStatusBarPadding();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).addCallback(this);
        OpScreenBurnInProtector.getInstance().registerListener(getContext(), this);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).removeCallback(this);
        OpScreenBurnInProtector.getInstance().unregisterListener(this);
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector.OnBurnInPreventListener
    public void onBurnInPreventTrigger(int i) {
        ViewGroup viewGroup = this.mStatusbarContent;
        if (viewGroup != null) {
            viewGroup.setTranslationX((float) i);
        }
    }
}
