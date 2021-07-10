package com.android.systemui.qs.tileimpl;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.oneplus.util.ThemeColorUtils;
import java.util.Objects;
public class QSTileView extends QSTileBaseView {
    protected int mColor;
    private View mExpandIndicator;
    private View mExpandSpace;
    protected TextView mLabel;
    private ViewGroup mLabelContainer;
    private ImageView mPadLock;
    protected TextView mSecondLine;
    private int mState;

    public QSTileView(Context context, QSIconView qSIconView) {
        this(context, qSIconView, false);
    }

    public QSTileView(Context context, QSIconView qSIconView, boolean z) {
        super(context, qSIconView, z);
        this.mColor = 0;
        setClipChildren(false);
        setClipToPadding(false);
        setClickable(true);
        setId(View.generateViewId());
        createLabel();
        setOrientation(1);
        setGravity(49);
        Utils.getColorAttr(getContext(), 16842806);
        Utils.getColorAttr(getContext(), 16842808);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (configuration.orientation == 2) {
            FontSizeUtils.updateFontSize(this.mLabel, C0005R$dimen.op_qs_tile_text_size);
            FontSizeUtils.updateFontSize(this.mSecondLine, C0005R$dimen.op_qs_tile_text_size);
            return;
        }
        FontSizeUtils.updateFontSize(this.mLabel, C0005R$dimen.qs_tile_text_size);
        FontSizeUtils.updateFontSize(this.mSecondLine, C0005R$dimen.qs_tile_text_size);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileBaseView, com.android.systemui.plugins.qs.QSTileView
    public int getDetailY() {
        return getTop() + this.mLabelContainer.getTop() + (this.mLabelContainer.getHeight() / 2);
    }

    /* access modifiers changed from: protected */
    public void createLabel() {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext()).inflate(C0011R$layout.qs_tile_label, (ViewGroup) this, false);
        this.mLabelContainer = viewGroup;
        viewGroup.setClipChildren(false);
        this.mLabelContainer.setClipToPadding(false);
        this.mLabel = (TextView) this.mLabelContainer.findViewById(C0008R$id.tile_label);
        this.mPadLock = (ImageView) this.mLabelContainer.findViewById(C0008R$id.restricted_padlock);
        this.mLabelContainer.findViewById(C0008R$id.underline);
        this.mExpandIndicator = this.mLabelContainer.findViewById(C0008R$id.expand_indicator);
        this.mExpandSpace = this.mLabelContainer.findViewById(C0008R$id.expand_space);
        this.mSecondLine = (TextView) this.mLabelContainer.findViewById(C0008R$id.app_label);
        addView(this.mLabelContainer);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.mLabel.getLineCount() > 2 || (!TextUtils.isEmpty(this.mSecondLine.getText()) && this.mSecondLine.getLineHeight() > this.mSecondLine.getHeight())) {
            this.mLabel.setSingleLine();
            super.onMeasure(i, i2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileBaseView
    public void handleStateChanged(QSTile.State state) {
        super.handleStateChanged(state);
        if (!Objects.equals(this.mLabel.getText(), state.label) || this.mState != state.state) {
            this.mLabel.setTextColor(QSTileImpl.getOPColorForState(state.state));
            this.mState = state.state;
            this.mLabel.setText(state.label);
        }
        int i = 0;
        if (!Objects.equals(this.mSecondLine.getText(), state.secondaryLabel)) {
            this.mSecondLine.setText(state.secondaryLabel);
            this.mSecondLine.setVisibility(TextUtils.isEmpty(state.secondaryLabel) ? 8 : 0);
        }
        this.mExpandIndicator.setVisibility(8);
        this.mExpandSpace.setVisibility(8);
        this.mLabelContainer.setContentDescription(null);
        if (this.mLabelContainer.isClickable()) {
            this.mLabelContainer.setClickable(false);
            this.mLabelContainer.setLongClickable(false);
            this.mLabelContainer.setBackground(null);
        }
        this.mLabel.setEnabled(!state.disabledByPolicy);
        ImageView imageView = this.mPadLock;
        if (!state.disabledByPolicy) {
            i = 8;
        }
        imageView.setVisibility(i);
        updateThemeColor(state);
    }

    private void updateThemeColor(QSTile.State state) {
        int color = ThemeColorUtils.getColor(1);
        this.mColor = color;
        this.mLabel.setTextColor(color);
        this.mSecondLine.setTextColor(ThemeColorUtils.getColor(2));
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileBaseView
    public void init(View.OnClickListener onClickListener, View.OnClickListener onClickListener2, View.OnLongClickListener onLongClickListener) {
        super.init(onClickListener, onClickListener2, onLongClickListener);
        this.mLabelContainer.setOnClickListener(onClickListener2);
        this.mLabelContainer.setOnLongClickListener(onLongClickListener);
        this.mLabelContainer.setClickable(false);
        this.mLabelContainer.setLongClickable(false);
    }
}
