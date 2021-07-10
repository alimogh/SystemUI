package com.oneplus.plugin;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.oneplus.util.OpUtils;
public class OpPreventModeView extends RelativeLayout {
    private Context mContext;
    private LinearLayout mInnerView;
    private ImageView mPhone;
    private Resources mResources;
    private OpRippleView mRippleView;
    private TextView mTag;
    private TextView mTag2;
    private TextView mTagNum1;
    private TextView mTagNum2;
    private TextView mTitle;
    private TextView mTitleCancel;

    public OpPreventModeView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mContext = context;
        this.mResources = context.getResources();
    }

    public OpPreventModeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContext = context;
        this.mResources = context.getResources();
    }

    public OpPreventModeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mResources = context.getResources();
    }

    public OpPreventModeView(Context context) {
        super(context);
        this.mContext = context;
        this.mResources = context.getResources();
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        this.mTitle.setText(C0015R$string.prevent_view_title);
        this.mTitleCancel.setText(C0015R$string.prevent_view_title_cancel);
        this.mTag.setText(C0015R$string.prevent_view_top_tag_cancel);
        this.mTag2.setText(C0015R$string.prevent_view_top_tag_cancel2);
    }

    public void init() {
        this.mInnerView = (LinearLayout) findViewById(C0008R$id.prevent_mode_inner_view);
        this.mTitle = (TextView) findViewById(C0008R$id.prevent_view_title);
        this.mPhone = (ImageView) findViewById(C0008R$id.prevent_mode_phone);
        this.mTitleCancel = (TextView) findViewById(C0008R$id.prevent_view_title_cancel);
        this.mTag = (TextView) findViewById(C0008R$id.prevent_view_top_tag_cancel);
        this.mTag2 = (TextView) findViewById(C0008R$id.prevent_view_top_tag_cancel2);
        this.mTagNum1 = (TextView) findViewById(C0008R$id.prevent_view_top_tag_number1);
        this.mTagNum2 = (TextView) findViewById(C0008R$id.prevent_view_top_tag_number2);
        this.mRippleView = (OpRippleView) findViewById(C0008R$id.rippleview_first);
        new Configuration(this.mContext.getResources().getConfiguration());
        findViewById(C0008R$id.scrim_view);
        this.mPhone.setImageResource(C0006R$drawable.prevent_mode_img);
        updateLayout();
    }

    public void create() {
        this.mInnerView = (LinearLayout) findViewById(C0008R$id.prevent_mode_inner_view);
        this.mRippleView = (OpRippleView) findViewById(C0008R$id.rippleview_first);
        findViewById(C0008R$id.scrim_view);
        updateLayout();
        this.mTitle.setText(C0015R$string.prevent_view_title);
        this.mTitleCancel.setText(C0015R$string.prevent_view_title_cancel);
        this.mTag.setText(C0015R$string.prevent_view_top_tag_cancel);
        this.mTag2.setText(C0015R$string.prevent_view_top_tag_cancel2);
    }

    public void clear() {
        this.mInnerView = null;
        this.mRippleView = null;
    }

    private void updateLayout() {
        float f = this.mResources.getDisplayMetrics().scaledDensity;
        LinearLayout linearLayout = this.mInnerView;
        if (linearLayout != null) {
            ((RelativeLayout.LayoutParams) linearLayout.getLayoutParams()).topMargin = OpUtils.convertDpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_inner_view_margin_top));
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mTitle.getLayoutParams();
        int convertDpToFixedPx = OpUtils.convertDpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_title_margin_horizontal));
        layoutParams.setMarginStart(convertDpToFixedPx);
        layoutParams.setMarginEnd(convertDpToFixedPx);
        this.mTitle.setTextSize(0, (float) OpUtils.convertSpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_title_size), f));
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mPhone.getLayoutParams();
        layoutParams2.topMargin = OpUtils.convertDpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_img_margin_top));
        layoutParams2.width = OpUtils.convertDpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_img_width));
        layoutParams2.height = OpUtils.convertDpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_img_height));
        LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mTitleCancel.getLayoutParams();
        layoutParams3.topMargin = OpUtils.convertDpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_title_cancel_margin_top));
        int convertDpToFixedPx2 = OpUtils.convertDpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_cancel_margin_horizontal));
        layoutParams3.setMarginStart(convertDpToFixedPx2);
        layoutParams3.setMarginEnd(convertDpToFixedPx2);
        this.mTitleCancel.setTextSize(0, (float) OpUtils.convertSpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_title_cancel_size), f));
        int convertSpToFixedPx = OpUtils.convertSpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_top_tag_size), f);
        LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) ((LinearLayout) findViewById(C0008R$id.prevent_view_top_tag_view1)).getLayoutParams();
        layoutParams4.topMargin = OpUtils.convertDpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_top_tag_view1_margin_top));
        int convertDpToFixedPx3 = OpUtils.convertDpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_cancel_margin_horizontal));
        layoutParams4.setMarginStart(convertDpToFixedPx3);
        layoutParams4.setMarginEnd(convertDpToFixedPx3);
        float f2 = (float) convertSpToFixedPx;
        this.mTag.setTextSize(0, f2);
        this.mTagNum1.setTextSize(0, f2);
        LinearLayout.LayoutParams layoutParams5 = (LinearLayout.LayoutParams) ((LinearLayout) findViewById(C0008R$id.prevent_view_top_tag_view2)).getLayoutParams();
        layoutParams5.topMargin = OpUtils.convertDpToFixedPx(this.mResources.getDimension(C0005R$dimen.prevent_view_top_tag_view2_margin_top));
        layoutParams5.setMarginStart(convertDpToFixedPx3);
        layoutParams5.setMarginEnd(convertDpToFixedPx3);
        this.mTag2.setTextSize(0, f2);
        this.mTagNum2.setTextSize(0, f2);
    }

    private void playRippleAniamor() {
        OpRippleView opRippleView = this.mRippleView;
        if (opRippleView != null) {
            opRippleView.prepare();
            this.mRippleView.startRipple();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View view, int i) {
        OpRippleView opRippleView;
        super.onVisibilityChanged(view, i);
        if (this.mTitleCancel != null && this.mTitle != null && this.mTag != null && view == this) {
            if (i == 0 || (opRippleView = this.mRippleView) == null) {
                playRippleAniamor();
            } else {
                opRippleView.stopRipple();
            }
        }
    }
}
