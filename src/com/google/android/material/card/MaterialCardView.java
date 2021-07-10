package com.google.android.material.card;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Checkable;
import android.widget.FrameLayout;
import androidx.appcompat.widget.SmoothCornerUtils;
import androidx.cardview.widget.CardView;
import com.google.android.material.R$attr;
import com.google.android.material.R$style;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
import java.util.List;
public class MaterialCardView extends CardView implements Checkable, Shapeable {
    private static final int[] CHECKABLE_STATE_SET = {16842911};
    private static final int[] CHECKED_STATE_SET = {16842912};
    private static final int DEF_STYLE_RES = R$style.Widget_MaterialComponents_CardView;
    private static final int[] DRAGGED_STATE_SET = {R$attr.state_dragged};
    private final MaterialCardViewHelper cardViewHelper;
    private boolean checked;
    private boolean dragged;
    private boolean hasInitBackground;
    private final boolean isParentCardViewDoneInitializing;
    private int mBackgroundColor;
    private Paint mCardBackgroundMaskPaint;
    ViewOutlineProvider mCardViewOutlineProvider;
    private List<Path> mCornerPathList;
    private float mCornerRadius;
    private PorterDuffXfermode mDuffXferMode;
    private boolean mIsCardSelected;
    private Paint mPaint;
    private RectF mRectF;
    private OnCheckedChangeListener onCheckedChangeListener;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(MaterialCardView materialCardView, boolean z);
    }

    public MaterialCardView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.materialCardViewStyle);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MaterialCardView(android.content.Context r10, android.util.AttributeSet r11, int r12) {
        /*
            r9 = this;
            int r0 = com.google.android.material.card.MaterialCardView.DEF_STYLE_RES
            android.content.Context r10 = com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap(r10, r11, r12, r0)
            r9.<init>(r10, r11, r12)
            r10 = 0
            r9.checked = r10
            r9.dragged = r10
            android.graphics.Paint r1 = new android.graphics.Paint
            r2 = 1
            r1.<init>(r2)
            r9.mPaint = r1
            android.graphics.RectF r1 = new android.graphics.RectF
            r1.<init>()
            r9.mRectF = r1
            android.graphics.PorterDuffXfermode r1 = new android.graphics.PorterDuffXfermode
            android.graphics.PorterDuff$Mode r3 = android.graphics.PorterDuff.Mode.CLEAR
            r1.<init>(r3)
            r9.mDuffXferMode = r1
            com.google.android.material.card.MaterialCardView$1 r1 = new com.google.android.material.card.MaterialCardView$1
            r1.<init>()
            r9.mCardViewOutlineProvider = r1
            r9.isParentCardViewDoneInitializing = r2
            android.content.Context r3 = r9.getContext()
            int[] r5 = com.google.android.material.R$styleable.MaterialCardView
            int r7 = com.google.android.material.card.MaterialCardView.DEF_STYLE_RES
            int[] r8 = new int[r10]
            r4 = r11
            r6 = r12
            android.content.res.TypedArray r1 = com.google.android.material.internal.ThemeEnforcement.obtainStyledAttributes(r3, r4, r5, r6, r7, r8)
            int r2 = com.google.android.material.R$styleable.MaterialCardView_cardBackgroundColorMask
            r1.getColor(r2, r10)
            int r10 = com.google.android.material.R$styleable.MaterialCardView_smoothCornerRadius
            r2 = 21
            int r10 = r1.getDimensionPixelOffset(r10, r2)
            float r10 = (float) r10
            r9.mCornerRadius = r10
            com.google.android.material.card.MaterialCardViewHelper r10 = new com.google.android.material.card.MaterialCardViewHelper
            r10.<init>(r9, r11, r12, r0)
            r9.cardViewHelper = r10
            android.content.res.ColorStateList r11 = super.getCardBackgroundColor()
            r10.setCardBackgroundColor(r11)
            com.google.android.material.card.MaterialCardViewHelper r10 = r9.cardViewHelper
            int r11 = super.getContentPaddingLeft()
            int r12 = super.getContentPaddingTop()
            int r0 = super.getContentPaddingRight()
            int r2 = super.getContentPaddingBottom()
            r10.setUserContentPadding(r11, r12, r0, r2)
            com.google.android.material.card.MaterialCardViewHelper r10 = r9.cardViewHelper
            r10.loadFromAttributes(r1)
            r1.recycle()
            android.view.ViewOutlineProvider r10 = r9.mCardViewOutlineProvider
            r9.setOutlineProvider(r10)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.card.MaterialCardView.<init>(android.content.Context, android.util.AttributeSet, int):void");
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName("androidx.cardview.widget.CardView");
        accessibilityNodeInfo.setCheckable(isCheckable());
        accessibilityNodeInfo.setClickable(isClickable());
        accessibilityNodeInfo.setChecked(isChecked());
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName("androidx.cardview.widget.CardView");
        accessibilityEvent.setChecked(isChecked());
    }

    /* access modifiers changed from: protected */
    @Override // androidx.cardview.widget.CardView, android.widget.FrameLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        this.cardViewHelper.onMeasure(getMeasuredWidth(), getMeasuredHeight());
    }

    /* access modifiers changed from: package-private */
    public float getCardViewRadius() {
        return super.getRadius();
    }

    /* access modifiers changed from: package-private */
    public void setAncestorContentPadding(int i, int i2, int i3, int i4) {
        super.setContentPadding(i, i2, i3, i4);
    }

    public void setCardBackgroundColor(int i) {
        this.cardViewHelper.setCardBackgroundColor(ColorStateList.valueOf(i));
    }

    @Override // android.view.View
    public void setClickable(boolean z) {
        super.setClickable(z);
        this.cardViewHelper.updateClickable();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        MaterialShapeUtils.setParentAbsoluteElevation(this, this.cardViewHelper.getBackground());
    }

    @Override // android.view.View
    public void setBackground(Drawable drawable) {
        setBackgroundDrawable(drawable);
    }

    @Override // android.view.View
    public void setBackgroundDrawable(Drawable drawable) {
        if (this.isParentCardViewDoneInitializing) {
            if (!this.cardViewHelper.isBackgroundOverwritten()) {
                Log.i("MaterialCardView", "Setting a custom background is not supported.");
                this.cardViewHelper.setBackgroundOverwritten(true);
            }
            super.setBackgroundDrawable(drawable);
        }
    }

    /* access modifiers changed from: package-private */
    public void setBackgroundInternal(Drawable drawable) {
        super.setBackgroundDrawable(drawable);
    }

    @Override // android.widget.Checkable
    public boolean isChecked() {
        return this.checked;
    }

    @Override // android.widget.Checkable
    public void setChecked(boolean z) {
        if (this.checked != z) {
            toggle();
        }
    }

    public boolean isDragged() {
        return this.dragged;
    }

    public boolean isCheckable() {
        MaterialCardViewHelper materialCardViewHelper = this.cardViewHelper;
        return materialCardViewHelper != null && materialCardViewHelper.isCheckable();
    }

    @Override // android.widget.Checkable
    public void toggle() {
        if (isCheckable() && isEnabled()) {
            this.checked = !this.checked;
            refreshDrawableState();
            forceRippleRedrawIfNeeded();
            OnCheckedChangeListener onCheckedChangeListener = this.onCheckedChangeListener;
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(this, this.checked);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 3);
        if (isCheckable()) {
            FrameLayout.mergeDrawableStates(onCreateDrawableState, CHECKABLE_STATE_SET);
        }
        if (isChecked()) {
            FrameLayout.mergeDrawableStates(onCreateDrawableState, CHECKED_STATE_SET);
        }
        if (isDragged()) {
            FrameLayout.mergeDrawableStates(onCreateDrawableState, DRAGGED_STATE_SET);
        }
        return onCreateDrawableState;
    }

    @Override // com.google.android.material.shape.Shapeable
    public void setShapeAppearanceModel(ShapeAppearanceModel shapeAppearanceModel) {
        this.cardViewHelper.setShapeAppearanceModel(shapeAppearanceModel);
    }

    private void forceRippleRedrawIfNeeded() {
        if (Build.VERSION.SDK_INT > 26) {
            this.cardViewHelper.forceRippleRedraw();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        this.mRectF.set(0.0f, 0.0f, (float) i, (float) i2);
        this.mCornerPathList = SmoothCornerUtils.calculateBezierCornerPaths(this.mRectF, this.mCornerRadius);
        super.onSizeChanged(i, i2, i3, i4);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean drawChild(Canvas canvas, View view, long j) {
        if (view.getBackground() == null && !this.hasInitBackground) {
            if (getHeight() > view.getHeight()) {
                setCardBackgroundColor(this.mBackgroundColor);
            } else {
                view.setBackgroundColor(this.mBackgroundColor);
            }
            this.hasInitBackground = true;
        }
        return super.drawChild(canvas, view, j);
    }

    @Override // android.view.View, android.view.ViewGroup
    public void dispatchDraw(Canvas canvas) {
        int saveLayer = canvas.saveLayer(this.mRectF, this.mPaint);
        super.dispatchDraw(canvas);
        this.mPaint.setXfermode(this.mDuffXferMode);
        List<Path> list = this.mCornerPathList;
        if (list != null && !list.isEmpty()) {
            for (Path path : this.mCornerPathList) {
                canvas.drawPath(path, this.mPaint);
            }
        }
        this.mPaint.setXfermode(null);
        canvas.restoreToCount(saveLayer);
        if (this.mCardBackgroundMaskPaint != null && this.mIsCardSelected) {
            Log.d("chenhb", "go in mCardBackgroundMaskPaint");
            canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), this.mCardBackgroundMaskPaint);
        }
    }
}
