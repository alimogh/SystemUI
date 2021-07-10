package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import com.oneplus.util.OpUtils;
public class FooterView extends StackScrollerDecorView {
    private final int mClearAllTopPadding;
    private FooterViewButton mDismissButton;
    private FooterViewButton mManageButton;
    private boolean mShowHistory;

    public FooterView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mClearAllTopPadding = context.getResources().getDimensionPixelSize(C0005R$dimen.clear_all_padding_top);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public View findContentView() {
        return findViewById(C0008R$id.content);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public View findSecondaryView() {
        return findViewById(C0008R$id.dismiss_text);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mDismissButton = (FooterViewButton) findSecondaryView();
        this.mManageButton = (FooterViewButton) findViewById(C0008R$id.manage_text);
    }

    public void setTextColor(int i) {
        if (OpUtils.isREDVersion()) {
            i = getContext().getColor(C0004R$color.op_turquoise);
        }
        this.mManageButton.setTextColor(i);
        this.mDismissButton.setTextColor(i);
    }

    public void setManageButtonClickListener(View.OnClickListener onClickListener) {
        this.mManageButton.setOnClickListener(onClickListener);
    }

    public void setDismissButtonClickListener(View.OnClickListener onClickListener) {
        this.mDismissButton.setOnClickListener(onClickListener);
    }

    public boolean isOnEmptySpace(float f, float f2) {
        return f < this.mContent.getX() || f > this.mContent.getX() + ((float) this.mContent.getWidth()) || f2 < this.mContent.getY() || f2 > this.mContent.getY() + ((float) this.mContent.getHeight());
    }

    public void showHistory(boolean z) {
        this.mShowHistory = z;
        if (z) {
            this.mManageButton.setText(C0015R$string.manage_notifications_history_text);
            this.mManageButton.setContentDescription(((FrameLayout) this).mContext.getString(C0015R$string.manage_notifications_history_text));
            return;
        }
        this.mManageButton.setText(C0015R$string.manage_notifications_text);
        this.mManageButton.setContentDescription(((FrameLayout) this).mContext.getString(C0015R$string.manage_notifications_text));
    }

    public boolean isHistoryShown() {
        return this.mShowHistory;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableView, android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mDismissButton.setText(C0015R$string.clear_all_notifications_text);
        this.mDismissButton.setContentDescription(((FrameLayout) this).mContext.getString(C0015R$string.accessibility_clear_all));
        showHistory(this.mShowHistory);
        post(new Runnable() { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$FooterView$41XRRTwqAWxRJ4ZxHuVwRz_46Cw
            @Override // java.lang.Runnable
            public final void run() {
                FooterView.this.lambda$onConfigurationChanged$0$FooterView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onConfigurationChanged$0 */
    public /* synthetic */ void lambda$onConfigurationChanged$0$FooterView() {
        if (this.mManageButton.getWidth() + this.mDismissButton.getWidth() > (getWidth() * 80) / 100) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("FooterView", "onConfigurationChanged: parent-width=" + getWidth() + ", dismiss-btn-width=" + this.mDismissButton.getWidth() + ", manage-btn-width=" + this.mManageButton.getWidth());
            }
            compactFooterButtons((getWidth() * 40) / 100);
        }
    }

    private void compactFooterButtons(int i) {
        this.mManageButton.setMaxLines(1);
        this.mManageButton.setMaxWidth(i);
        this.mManageButton.setEllipsize(TextUtils.TruncateAt.END);
        this.mDismissButton.setMaxLines(1);
        this.mDismissButton.setMaxWidth(i);
        this.mDismissButton.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public ExpandableViewState createExpandableViewState() {
        return new FooterViewState();
    }

    public class FooterViewState extends ExpandableViewState {
        public FooterViewState() {
        }

        @Override // com.android.systemui.statusbar.notification.stack.ExpandableViewState, com.android.systemui.statusbar.notification.stack.ViewState
        public void applyToView(View view) {
            super.applyToView(view);
            if (view instanceof FooterView) {
                FooterView footerView = (FooterView) view;
                boolean z = true;
                if (!(this.clipTopAmount < FooterView.this.mClearAllTopPadding) || !footerView.isVisible()) {
                    z = false;
                }
                footerView.setContentVisible(z);
            }
        }
    }
}
