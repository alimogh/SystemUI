package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintController;
import com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector;
public class OpKeyguardStatusBarView extends RelativeLayout implements OpHighlightHintController.OnHighlightHintStateChangeListener, OpScreenBurnInProtector.OnBurnInPreventListener {
    private TextView mCarrierLabel;
    private View mHighlightHintView;
    private View mKeyguardHeader;
    private View mSystemIcons;

    public OpKeyguardStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mSystemIcons = findViewById(C0008R$id.statusIcons);
        View findViewById = findViewById(C0008R$id.highlighthintview);
        this.mHighlightHintView = findViewById;
        findViewById.setTag(1001);
        this.mCarrierLabel = (TextView) findViewById(C0008R$id.keyguard_carrier_text);
        this.mKeyguardHeader = findViewById(C0008R$id.keyguard_header);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).addCallback(this);
        OpScreenBurnInProtector.getInstance().registerListener(((RelativeLayout) this).mContext, this);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).removeCallback(this);
        OpScreenBurnInProtector.getInstance().unregisterListener(this);
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController.OnHighlightHintStateChangeListener
    public void onHighlightHintStateChange() {
        OpHighlightHintController opHighlightHintController = (OpHighlightHintController) Dependency.get(OpHighlightHintController.class);
        boolean isHighLightHintShow = opHighlightHintController.isHighLightHintShow();
        int i = 8;
        if (opHighlightHintController.showOvalLayout()) {
            this.mCarrierLabel.setVisibility(isHighLightHintShow ? 8 : 0);
            View view = this.mHighlightHintView;
            if (isHighLightHintShow) {
                i = 0;
            }
            view.setVisibility(i);
        } else if (isHighLightHintShow) {
            this.mSystemIcons.setVisibility(8);
            this.mCarrierLabel.setVisibility(8);
            this.mHighlightHintView.setVisibility(0);
        } else {
            this.mSystemIcons.setVisibility(0);
            this.mCarrierLabel.setVisibility(0);
            this.mHighlightHintView.setVisibility(8);
        }
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpScreenBurnInProtector.OnBurnInPreventListener
    public void onBurnInPreventTrigger(int i) {
        View view = this.mKeyguardHeader;
        if (view != null) {
            view.setTranslationX((float) i);
        }
    }
}
