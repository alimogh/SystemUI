package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.AlphaOptimizedLinearLayout;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.oneplus.util.OpUtils;
public class HeadsUpStatusBarView extends AlphaOptimizedLinearLayout {
    private int mAbsoluteStartPadding;
    private int mCutOutInset;
    private DisplayCutout mDisplayCutout;
    private Point mDisplaySize;
    private int mEndMargin;
    private boolean mFirstLayout;
    private Rect mIconDrawingRect;
    private View mIconPlaceholder;
    private Rect mLayoutedIconRect;
    private int mMaxWidth;
    private Runnable mOnDrawingRectChangedListener;
    private final NotificationEntry.OnSensitivityChangedListener mOnSensitivityChangedListener;
    private NotificationEntry mShowingEntry;
    private int mSysWinInset;
    private TextView mTextView;
    private int[] mTmpPosition;

    public HeadsUpStatusBarView(Context context) {
        this(context, null);
    }

    public HeadsUpStatusBarView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HeadsUpStatusBarView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public HeadsUpStatusBarView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mLayoutedIconRect = new Rect();
        this.mTmpPosition = new int[2];
        this.mFirstLayout = true;
        this.mIconDrawingRect = new Rect();
        this.mOnSensitivityChangedListener = new NotificationEntry.OnSensitivityChangedListener() { // from class: com.android.systemui.statusbar.-$$Lambda$HeadsUpStatusBarView$3EjbzF6YolguGjurT443cwUG0Vs
            @Override // com.android.systemui.statusbar.notification.collection.NotificationEntry.OnSensitivityChangedListener
            public final void onSensitivityChanged(NotificationEntry notificationEntry) {
                HeadsUpStatusBarView.this.lambda$new$0$HeadsUpStatusBarView(notificationEntry);
            }
        };
        Resources resources = getResources();
        this.mAbsoluteStartPadding = resources.getDimensionPixelSize(C0005R$dimen.notification_side_paddings) + resources.getDimensionPixelSize(C0005R$dimen.heads_up_status_bar_padding_start) + resources.getDimensionPixelSize(17105347);
        if (OpUtils.isSupportHolePunchFrontCam()) {
            this.mAbsoluteStartPadding = OpUtils.getDimensionPixelSize(getResources(), C0005R$dimen.status_bar_padding_start, 1080);
        }
        int dimensionPixelSize = resources.getDimensionPixelSize(17105346);
        this.mEndMargin = dimensionPixelSize;
        setPaddingRelative(this.mAbsoluteStartPadding, 0, dimensionPixelSize, 0);
        updateMaxWidth();
    }

    private void updateMaxWidth() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0005R$dimen.qs_panel_width);
        if (dimensionPixelSize != this.mMaxWidth) {
            this.mMaxWidth = dimensionPixelSize;
            requestLayout();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        if (this.mMaxWidth > 0) {
            i = View.MeasureSpec.makeMeasureSpec(Math.min(View.MeasureSpec.getSize(i), this.mMaxWidth), View.MeasureSpec.getMode(i));
        }
        super.onMeasure(i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateMaxWidth();
    }

    @Override // android.view.View
    public Bundle onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("heads_up_status_bar_view_super_parcelable", super.onSaveInstanceState());
        bundle.putBoolean("first_layout", this.mFirstLayout);
        bundle.putInt("visibility", getVisibility());
        bundle.putFloat("alpha", getAlpha());
        return bundle;
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable == null || !(parcelable instanceof Bundle)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        Bundle bundle = (Bundle) parcelable;
        super.onRestoreInstanceState(bundle.getParcelable("heads_up_status_bar_view_super_parcelable"));
        this.mFirstLayout = bundle.getBoolean("first_layout", true);
        if (bundle.containsKey("visibility")) {
            setVisibility(bundle.getInt("visibility"));
        }
        if (bundle.containsKey("alpha")) {
            setAlpha(bundle.getFloat("alpha"));
        }
    }

    @VisibleForTesting
    public HeadsUpStatusBarView(Context context, View view, TextView textView) {
        this(context);
        this.mIconPlaceholder = view;
        this.mTextView = textView;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIconPlaceholder = findViewById(C0008R$id.icon_placeholder);
        this.mTextView = (TextView) findViewById(C0008R$id.text);
    }

    public void setEntry(NotificationEntry notificationEntry) {
        NotificationEntry notificationEntry2 = this.mShowingEntry;
        if (notificationEntry2 != null) {
            notificationEntry2.removeOnSensitivityChangedListener(this.mOnSensitivityChangedListener);
        }
        this.mShowingEntry = notificationEntry;
        if (notificationEntry != null) {
            CharSequence charSequence = notificationEntry.headsUpStatusBarText;
            if (notificationEntry.isSensitive()) {
                charSequence = notificationEntry.headsUpStatusBarTextPublic;
            }
            this.mTextView.setText(charSequence);
            this.mShowingEntry.addOnSensitivityChangedListener(this.mOnSensitivityChangedListener);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$HeadsUpStatusBarView(NotificationEntry notificationEntry) {
        if (notificationEntry == this.mShowingEntry) {
            setEntry(notificationEntry);
            return;
        }
        throw new IllegalStateException("Got a sensitivity change for " + notificationEntry + " but mShowingEntry is " + this.mShowingEntry);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        post(new Runnable() { // from class: com.android.systemui.statusbar.-$$Lambda$HeadsUpStatusBarView$nzfnElI2xOD0W8y3WE5sPcHmFhw
            @Override // java.lang.Runnable
            public final void run() {
                HeadsUpStatusBarView.this.lambda$onLayout$1$HeadsUpStatusBarView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: updatePosition */
    public void lambda$onLayout$1() {
        this.mIconPlaceholder.getLocationOnScreen(this.mTmpPosition);
        int translationX = (int) (((float) this.mTmpPosition[0]) - getTranslationX());
        int i = this.mTmpPosition[1];
        int width = this.mIconPlaceholder.getWidth() + translationX;
        this.mLayoutedIconRect.set(translationX, i, width, this.mIconPlaceholder.getHeight() + i);
        updateDrawingRect();
        int i2 = this.mAbsoluteStartPadding + this.mSysWinInset + this.mCutOutInset;
        if (this.mDisplayCutout == null && getRootWindowInsets() != null) {
            this.mDisplayCutout = getRootWindowInsets().getDisplayCutout();
        }
        if (getResources().getConfiguration().orientation == 1 && OpUtils.isSupportHolePunchFrontCam() && this.mDisplayCutout != null) {
            Rect rect = new Rect();
            ScreenDecorations.DisplayCutoutView.boundsFromDirection(((LinearLayout) this).mContext, this.mDisplayCutout, 48, rect);
            i2 += rect.right;
        }
        boolean isLayoutRtl = isLayoutRtl();
        int i3 = isLayoutRtl ? this.mDisplaySize.x - width : translationX;
        Log.i("HeadsUpStatusBarView", "1 onLayout:" + i3 + "," + i2 + " left:" + translationX + " mDisplaySize.x:" + this.mDisplaySize.x + " isRtl:" + isLayoutRtl + " right:" + width);
        if (i3 != i2) {
            int paddingStart = (i2 - i3) + getPaddingStart();
            Log.i("", "1 onLayout:" + paddingStart + "," + i3 + "," + getPaddingStart());
            setPaddingRelative(paddingStart, 0, this.mEndMargin, 0);
        }
        if (this.mFirstLayout) {
            setVisibility(8);
            this.mFirstLayout = false;
        }
    }

    public void setPanelTranslation(float f) {
        setTranslationX(f);
        updateDrawingRect();
    }

    private void updateDrawingRect() {
        Runnable runnable;
        Rect rect = this.mIconDrawingRect;
        rect.set(this.mLayoutedIconRect);
        this.mIconDrawingRect.offset((int) getTranslationX(), 0);
        if (((float) rect.left) != ((float) this.mIconDrawingRect.left) && (runnable = this.mOnDrawingRectChangedListener) != null) {
            runnable.run();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean fitSystemWindows(Rect rect) {
        boolean isLayoutRtl = isLayoutRtl();
        this.mSysWinInset = isLayoutRtl ? rect.right : rect.left;
        DisplayCutout displayCutout = getRootWindowInsets().getDisplayCutout();
        this.mDisplayCutout = displayCutout;
        Rect rect2 = new Rect();
        ScreenDecorations.DisplayCutoutView.boundsFromDirection(((LinearLayout) this).mContext, this.mDisplayCutout, 48, rect2);
        if (OpUtils.isSupportHolePunchFrontCam() && rect2.left != 0) {
            getDisplaySize();
            Resources resources = ((LinearLayout) this).mContext.getResources();
            Point point = this.mDisplaySize;
            this.mDisplayCutout = DisplayCutout.fromResourcesRectApproximation(resources, point.x, point.y);
        }
        this.mCutOutInset = displayCutout != null ? isLayoutRtl ? displayCutout.getSafeInsetRight() : displayCutout.getSafeInsetLeft() : 0;
        getDisplaySize();
        if (displayCutout != null && displayCutout.getSafeInsetRight() == 0 && displayCutout.getSafeInsetLeft() == 0) {
            displayCutout.getBoundingRects();
        }
        if (this.mSysWinInset != 0) {
            this.mCutOutInset = 0;
        }
        return super.fitSystemWindows(rect);
    }

    public NotificationEntry getShowingEntry() {
        return this.mShowingEntry;
    }

    public Rect getIconDrawingRect() {
        return this.mIconDrawingRect;
    }

    public void onDarkChanged(Rect rect, float f, int i) {
        this.mTextView.setTextColor(DarkIconDispatcher.getTint(rect, this, i));
    }

    public void setOnDrawingRectChangedListener(Runnable runnable) {
        this.mOnDrawingRectChangedListener = runnable;
    }

    private void getDisplaySize() {
        if (this.mDisplaySize == null) {
            this.mDisplaySize = new Point();
        }
        getDisplay().getRealSize(this.mDisplaySize);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getDisplaySize();
    }
}
