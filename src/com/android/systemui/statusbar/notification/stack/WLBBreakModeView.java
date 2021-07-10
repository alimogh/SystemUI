package com.android.systemui.statusbar.notification.stack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.systemui.C0008R$id;
import com.android.systemui.statusbar.notification.row.StackScrollerDecorView;
public class WLBBreakModeView extends StackScrollerDecorView {
    private ViewGroup mContents;
    private TextView mEnableBreakText;
    private View.OnClickListener mLabelClickListener = null;
    private Integer mLabelTextId;
    private TextView mLabelView;
    private View.OnClickListener mOnEnableBreakClickListener = null;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public View findSecondaryView() {
        return null;
    }

    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView, com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean isTransparent() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView, com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean needsClippingToShelf() {
        return true;
    }

    public WLBBreakModeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView, android.view.View
    public void onFinishInflate() {
        this.mContents = (ViewGroup) requireViewById(C0008R$id.content);
        bindContents();
        super.onFinishInflate();
        setVisible(false, false);
    }

    private void bindContents() {
        this.mLabelView = (TextView) requireViewById(C0008R$id.wlb_break_mode_label);
        TextView textView = (TextView) requireViewById(C0008R$id.wlb_enable_break_text);
        this.mEnableBreakText = textView;
        View.OnClickListener onClickListener = this.mOnEnableBreakClickListener;
        if (onClickListener != null) {
            textView.setOnClickListener(onClickListener);
        }
        View.OnClickListener onClickListener2 = this.mLabelClickListener;
        if (onClickListener2 != null) {
            this.mLabelView.setOnClickListener(onClickListener2);
        }
        Integer num = this.mLabelTextId;
        if (num != null) {
            this.mLabelView.setText(num.intValue());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public View findContentView() {
        return this.mContents;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return super.onInterceptTouchEvent(motionEvent);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void applyContentTransformation(float f, float f2) {
        super.applyContentTransformation(f, f2);
        this.mLabelView.setAlpha(f);
        this.mLabelView.setTranslationY(f2);
        this.mEnableBreakText.setAlpha(f);
        this.mEnableBreakText.setTranslationY(f2);
    }
}
