package com.android.systemui.qs;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
import java.util.function.Consumer;
public class QSContainerImpl extends FrameLayout {
    private static final FloatPropertyCompat<QSContainerImpl> BACKGROUND_BOTTOM = new FloatPropertyCompat<QSContainerImpl>("backgroundBottom") { // from class: com.android.systemui.qs.QSContainerImpl.1
        public float getValue(QSContainerImpl qSContainerImpl) {
            return qSContainerImpl.getBackgroundBottom();
        }

        public void setValue(QSContainerImpl qSContainerImpl, float f) {
            qSContainerImpl.setBackgroundBottom((int) f);
        }
    };
    private static final PhysicsAnimator.SpringConfig BACKGROUND_SPRING = new PhysicsAnimator.SpringConfig(1500.0f, 0.75f);
    private boolean mAnimateBottomOnNextLayout;
    private View mBackground;
    private int mBackgroundBottom = -1;
    private View mBackgroundGradient;
    private int mContentPaddingEnd = -1;
    private int mContentPaddingStart = -1;
    private View mDragHandle;
    private QuickStatusBarHeader mHeader;
    private int mHeightOverride = -1;
    private int mLastUpdateOrientation = -1;
    private QSCustomizer mQSCustomizer;
    private View mQSDetail;
    private QSPanel mQSPanel;
    private View mQSPanelContainer;
    private boolean mQsDisabled;
    private float mQsExpansion;
    private int mSideMargins;
    private final Point mSizePoint = new Point();
    private View mStatusBarBackground;

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void dispatchSetPressed(boolean z) {
    }

    @Override // android.view.View
    public boolean performClick() {
        return true;
    }

    public QSContainerImpl(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mQSPanel = (QSPanel) findViewById(C0008R$id.quick_settings_panel);
        this.mQSPanelContainer = findViewById(C0008R$id.expanded_qs_scroll_view);
        this.mQSDetail = findViewById(C0008R$id.qs_detail);
        this.mHeader = (QuickStatusBarHeader) findViewById(C0008R$id.header);
        this.mQSCustomizer = (QSCustomizer) findViewById(C0008R$id.qs_customize);
        this.mDragHandle = findViewById(C0008R$id.qs_drag_handle_view);
        this.mBackground = findViewById(C0008R$id.quick_settings_background);
        this.mStatusBarBackground = findViewById(C0008R$id.quick_settings_status_bar_background);
        this.mBackgroundGradient = findViewById(C0008R$id.quick_settings_gradient_view);
        updateResources();
        this.mHeader.getHeaderQsPanel().setMediaVisibilityChangedListener(new Consumer() { // from class: com.android.systemui.qs.-$$Lambda$QSContainerImpl$SmgcCxPvK9MpCttxm75WvXCaB3s
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                QSContainerImpl.this.lambda$onFinishInflate$0$QSContainerImpl((Boolean) obj);
            }
        });
        this.mQSPanel.setMediaVisibilityChangedListener(new Consumer() { // from class: com.android.systemui.qs.-$$Lambda$QSContainerImpl$671EqL2XSP9H1_W3SpTM-CiE58Y
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                QSContainerImpl.this.lambda$onFinishInflate$1$QSContainerImpl((Boolean) obj);
            }
        });
        setImportantForAccessibility(2);
        updateThemeColor();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$QSContainerImpl(Boolean bool) {
        if (this.mHeader.getHeaderQsPanel().isShown()) {
            this.mAnimateBottomOnNextLayout = true;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$1 */
    public /* synthetic */ void lambda$onFinishInflate$1$QSContainerImpl(Boolean bool) {
        if (this.mQSPanel.isShown()) {
            this.mAnimateBottomOnNextLayout = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setBackgroundBottom(int i) {
        this.mBackgroundBottom = i;
        this.mBackground.setBottom(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getBackgroundBottom() {
        int i = this.mBackgroundBottom;
        return i == -1 ? (float) this.mBackground.getBottom() : (float) i;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setBackgroundGradientVisibility(configuration);
        updateResources();
        this.mSizePoint.set(0, 0);
        updateThemeColor();
    }

    private void updateThemeColor() {
        if (OpUtils.isREDVersion()) {
            int top = this.mBackground.getTop();
            this.mBackground.setBackgroundResource(C0006R$drawable.op_qs_red_all);
            this.mBackground.setTop(top);
            this.mBackground.setBottom(this.mBackgroundBottom);
            View findViewById = findViewById(C0008R$id.op_qs_drag_handle);
            if (findViewById != null) {
                findViewById.setBackgroundTintList(ColorStateList.valueOf(getContext().getColor(C0004R$color.op_turquoise)));
                return;
            }
            return;
        }
        this.mBackground.setBackgroundTintList(ColorStateList.valueOf(ThemeColorUtils.getColor(9)));
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int i, int i2) {
        Configuration configuration = getResources().getConfiguration();
        boolean z = configuration.smallestScreenWidthDp >= 600 || configuration.orientation != 2;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mQSPanelContainer.getLayoutParams();
        int displayHeight = ((getDisplayHeight() - marginLayoutParams.topMargin) - marginLayoutParams.bottomMargin) - getPaddingBottom();
        if (z) {
            displayHeight -= getResources().getDimensionPixelSize(C0005R$dimen.navigation_bar_height);
        }
        int i3 = ((FrameLayout) this).mPaddingLeft + ((FrameLayout) this).mPaddingRight + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
        this.mQSPanelContainer.measure(FrameLayout.getChildMeasureSpec(i, i3, marginLayoutParams.width), View.MeasureSpec.makeMeasureSpec(displayHeight, Integer.MIN_VALUE));
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(this.mQSPanelContainer.getMeasuredWidth() + i3, 1073741824), View.MeasureSpec.makeMeasureSpec(marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + this.mQSPanelContainer.getMeasuredHeight() + getPaddingBottom(), 1073741824));
        this.mQSCustomizer.measure(i, View.MeasureSpec.makeMeasureSpec(getDisplayHeight(), 1073741824));
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateExpansion(this.mAnimateBottomOnNextLayout);
        this.mAnimateBottomOnNextLayout = false;
    }

    public void disable(int i, int i2, boolean z) {
        boolean z2 = true;
        int i3 = 0;
        if ((i2 & 1) == 0) {
            z2 = false;
        }
        if (z2 != this.mQsDisabled) {
            this.mQsDisabled = z2;
            setBackgroundGradientVisibility(getResources().getConfiguration());
            View view = this.mBackground;
            if (this.mQsDisabled) {
                i3 = 8;
            }
            view.setVisibility(i3);
        }
    }

    public void updateResources(Configuration configuration) {
        int i = this.mLastUpdateOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mLastUpdateOrientation = i2;
            updateResources();
        }
    }

    private void updateResources() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mQSPanelContainer.getLayoutParams();
        layoutParams.topMargin = ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(17105432);
        this.mQSPanelContainer.setLayoutParams(layoutParams);
        this.mSideMargins = getResources().getDimensionPixelSize(C0005R$dimen.op_notification_side_paddings);
        this.mContentPaddingStart = 0;
        this.mContentPaddingEnd = 0;
        updatePaddingsAndMargins();
        setBackgroundGradientVisibility(getResources().getConfiguration());
    }

    public void setHeightOverride(int i) {
        this.mHeightOverride = i;
        updateExpansion();
    }

    public void updateExpansion() {
        updateExpansion(false);
    }

    public void updateExpansion(boolean z) {
        int calculateContainerHeight = calculateContainerHeight();
        setBottom(getTop() + calculateContainerHeight);
        this.mQSDetail.setBottom(getTop() + calculateContainerHeight);
        View view = this.mDragHandle;
        view.setTranslationY((float) (calculateContainerHeight - view.getHeight()));
        View findViewById = findViewById(C0008R$id.qs_footer);
        findViewById.setTranslationY((float) (calculateContainerHeight - findViewById.getHeight()));
        this.mBackground.setTop(this.mQSPanelContainer.getTop());
        updateBackgroundBottom(calculateContainerHeight, z);
    }

    private void updateBackgroundBottom(int i, boolean z) {
        FloatPropertyCompat<QSContainerImpl> floatPropertyCompat = BACKGROUND_BOTTOM;
        PhysicsAnimator instance = PhysicsAnimator.getInstance(this);
        if (instance.isPropertyAnimating(floatPropertyCompat) || z) {
            floatPropertyCompat.setValue(this, floatPropertyCompat.getValue(this));
            instance.spring(floatPropertyCompat, (float) i, BACKGROUND_SPRING);
            instance.start();
            return;
        }
        floatPropertyCompat.setValue(this, (float) i);
    }

    /* access modifiers changed from: protected */
    public int calculateContainerHeight() {
        int i = this.mHeightOverride;
        if (i == -1) {
            i = getMeasuredHeight();
        }
        if (this.mQSCustomizer.isCustomizing()) {
            return this.mQSCustomizer.getHeight();
        }
        return this.mHeader.getHeight() + Math.round(this.mQsExpansion * ((float) (i - this.mHeader.getHeight())));
    }

    private void setBackgroundGradientVisibility(Configuration configuration) {
        int i = 4;
        if (configuration.orientation == 2) {
            this.mBackgroundGradient.setVisibility(4);
            this.mStatusBarBackground.setVisibility(4);
            return;
        }
        View view = this.mBackgroundGradient;
        if (!this.mQsDisabled) {
            i = 0;
        }
        view.setVisibility(i);
        this.mStatusBarBackground.setVisibility(0);
    }

    public void setExpansion(float f) {
        this.mQsExpansion = f;
        this.mDragHandle.setAlpha(1.0f - f);
        updateExpansion();
    }

    private void updatePaddingsAndMargins() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (!(childAt == this.mStatusBarBackground || childAt == this.mBackgroundGradient || childAt == this.mQSCustomizer)) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) childAt.getLayoutParams();
                int i2 = this.mSideMargins;
                layoutParams.rightMargin = i2;
                layoutParams.leftMargin = i2;
                if (childAt == this.mQSPanelContainer) {
                    this.mQSPanel.setContentMargins(this.mContentPaddingStart, this.mContentPaddingEnd);
                } else {
                    QuickStatusBarHeader quickStatusBarHeader = this.mHeader;
                    if (childAt == quickStatusBarHeader) {
                        quickStatusBarHeader.setContentMargins(this.mContentPaddingStart, this.mContentPaddingEnd);
                    } else {
                        childAt.setPaddingRelative(this.mContentPaddingStart, childAt.getPaddingTop(), this.mContentPaddingEnd, childAt.getPaddingBottom());
                    }
                }
            }
        }
    }

    private int getDisplayHeight() {
        if (this.mSizePoint.y == 0) {
            getDisplay().getRealSize(this.mSizePoint);
        }
        return this.mSizePoint.y;
    }
}
