package com.android.systemui.statusbar.notification.stack;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
public class SectionHeaderView extends ActivatableNotificationView {
    private ImageView mClearAllButton;
    private ViewGroup mContents;
    private View.OnClickListener mLabelClickListener = null;
    private Integer mLabelTextId;
    private TextView mLabelView;
    private View.OnClickListener mOnClearClickListener = null;
    private final RectF mTmpRect = new RectF();

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean needsClippingToShelf() {
        return true;
    }

    public SectionHeaderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setIsSectionHeader(true);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView, android.view.View
    public void onFinishInflate() {
        this.mContents = (ViewGroup) requireViewById(C0008R$id.content);
        bindContents();
        super.onFinishInflate();
        setVisibility(0);
        onUiModeChanged();
    }

    /* access modifiers changed from: package-private */
    public void onUiModeChanged() {
        updateBackgroundColors();
        this.mLabelView.setTextColor(getContext().getColor(C0004R$color.op_notification_info_primary_color));
        this.mClearAllButton.setImageResource(C0006R$drawable.op_status_bar_notification_section_header_clear_btn);
        Configuration configuration = getResources().getConfiguration();
        if (configuration != null) {
            boolean z = (configuration.uiMode & 48) == 32;
            Log.d("SectionHeaderView", "opOnUiModeChanged, current theme is in night mode: " + z);
            if (z) {
                this.mClearAllButton.setColorFilter(getResources().getColor(C0004R$color.oneplus_contorl_icon_color_accent_active_dark));
            } else {
                this.mClearAllButton.setColorFilter(getResources().getColor(C0004R$color.oneplus_contorl_icon_color_accent_active_light));
            }
        }
    }

    private void bindContents() {
        this.mLabelView = (TextView) requireViewById(C0008R$id.header_label);
        ImageView imageView = (ImageView) requireViewById(C0008R$id.btn_clear_all);
        this.mClearAllButton = imageView;
        View.OnClickListener onClickListener = this.mOnClearClickListener;
        if (onClickListener != null) {
            imageView.setOnClickListener(onClickListener);
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
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public View getContentView() {
        return this.mContents;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public boolean disallowSingleClick(MotionEvent motionEvent) {
        this.mTmpRect.set((float) this.mClearAllButton.getLeft(), (float) this.mClearAllButton.getTop(), (float) (this.mClearAllButton.getLeft() + this.mClearAllButton.getWidth()), (float) (this.mClearAllButton.getTop() + this.mClearAllButton.getHeight()));
        return this.mTmpRect.contains(motionEvent.getX(), motionEvent.getY());
    }

    /* access modifiers changed from: package-private */
    public void setAreThereDismissableGentleNotifs(boolean z) {
        this.mClearAllButton.setVisibility(z ? 0 : 8);
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return super.onInterceptTouchEvent(motionEvent);
    }

    /* access modifiers changed from: package-private */
    public void setOnHeaderClickListener(View.OnClickListener onClickListener) {
        this.mLabelClickListener = onClickListener;
        this.mLabelView.setOnClickListener(onClickListener);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void applyContentTransformation(float f, float f2) {
        super.applyContentTransformation(f, f2);
        this.mLabelView.setAlpha(f);
        this.mLabelView.setTranslationY(f2);
        this.mClearAllButton.setAlpha(f);
        this.mClearAllButton.setTranslationY(f2);
    }

    /* access modifiers changed from: package-private */
    public void setOnClearAllClickListener(View.OnClickListener onClickListener) {
        this.mOnClearClickListener = onClickListener;
        this.mClearAllButton.setOnClickListener(onClickListener);
    }

    /* access modifiers changed from: package-private */
    public void setHeaderText(int i) {
        this.mLabelTextId = Integer.valueOf(i);
        this.mLabelView.setText(i);
    }
}
