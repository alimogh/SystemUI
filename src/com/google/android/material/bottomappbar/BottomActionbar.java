package com.google.android.material.bottomappbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.customview.view.AbsSavedState;
import com.google.android.material.R$attr;
import com.google.android.material.R$color;
import com.google.android.material.R$dimen;
import com.google.android.material.R$style;
import com.google.android.material.animation.TransformationCallback;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.oneplus.common.NavigationButtonUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class BottomActionbar extends Toolbar implements CoordinatorLayout.AttachedBehavior {
    private static final int DEF_STYLE_RES = R$style.Widget_MaterialComponents_BottomAppBar;
    private int animatingModeChangeCounter;
    private ArrayList<AnimationListener> animationListeners;
    private Behavior behavior;
    private int bottomInset;
    private int fabAlignmentMode;
    AnimatorListenerAdapter fabAnimationListener;
    private boolean fabAttached;
    private final int fabOffsetEndMode;
    TransformationCallback<FloatingActionButton> fabTransformationCallback;
    private boolean hideOnScroll;
    private int leftInset;
    private View mDividerLine;
    private boolean mLimitDivider;
    private final MaterialShapeDrawable materialShapeDrawable;
    private Animator menuAnimator;
    private Animator modeAnimator;
    private final boolean paddingBottomSystemWindowInsets;
    private final boolean paddingLeftSystemWindowInsets;
    private final boolean paddingRightSystemWindowInsets;
    private int rightInset;

    /* access modifiers changed from: package-private */
    public interface AnimationListener {
        void onAnimationEnd(BottomActionbar bottomActionbar);

        void onAnimationStart(BottomActionbar bottomActionbar);
    }

    @Override // androidx.appcompat.widget.Toolbar
    public void setSubtitle(CharSequence charSequence) {
    }

    @Override // androidx.appcompat.widget.Toolbar
    public void setTitle(CharSequence charSequence) {
    }

    public BottomActionbar(Context context) {
        this(context, null, 0);
    }

    public BottomActionbar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.bottomAppBarStyle);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public BottomActionbar(android.content.Context r10, android.util.AttributeSet r11, int r12) {
        /*
            r9 = this;
            int r0 = com.google.android.material.bottomappbar.BottomActionbar.DEF_STYLE_RES
            android.content.Context r10 = com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap(r10, r11, r12, r0)
            r1 = 1
            r9.<init>(r10, r11, r12, r1)
            r10 = 0
            r9.mLimitDivider = r10
            com.google.android.material.shape.MaterialShapeDrawable r2 = new com.google.android.material.shape.MaterialShapeDrawable
            r2.<init>()
            r9.materialShapeDrawable = r2
            r9.animatingModeChangeCounter = r10
            r9.fabAttached = r1
            com.google.android.material.bottomappbar.BottomActionbar$1 r1 = new com.google.android.material.bottomappbar.BottomActionbar$1
            r1.<init>()
            r9.fabAnimationListener = r1
            com.google.android.material.bottomappbar.BottomActionbar$2 r1 = new com.google.android.material.bottomappbar.BottomActionbar$2
            r1.<init>()
            r9.fabTransformationCallback = r1
            android.content.Context r1 = r9.getContext()
            int[] r4 = com.google.android.material.R$styleable.BottomAppBar
            int r6 = com.google.android.material.bottomappbar.BottomActionbar.DEF_STYLE_RES
            int[] r7 = new int[r10]
            r2 = r1
            r3 = r11
            r5 = r12
            android.content.res.TypedArray r2 = com.google.android.material.internal.ThemeEnforcement.obtainStyledAttributes(r2, r3, r4, r5, r6, r7)
            int r3 = com.google.android.material.R$styleable.BottomAppBar_backgroundTint
            android.content.res.ColorStateList r3 = com.google.android.material.resources.MaterialResources.getColorStateList(r1, r2, r3)
            int r4 = com.google.android.material.R$styleable.BottomAppBar_elevation
            int r4 = r2.getDimensionPixelSize(r4, r10)
            int r5 = com.google.android.material.R$styleable.BottomAppBar_fabCradleMargin
            int r5 = r2.getDimensionPixelOffset(r5, r10)
            float r5 = (float) r5
            int r6 = com.google.android.material.R$styleable.BottomAppBar_fabCradleRoundedCornerRadius
            int r6 = r2.getDimensionPixelOffset(r6, r10)
            float r6 = (float) r6
            int r7 = com.google.android.material.R$styleable.BottomAppBar_fabCradleVerticalOffset
            int r7 = r2.getDimensionPixelOffset(r7, r10)
            float r7 = (float) r7
            int r8 = com.google.android.material.R$styleable.BottomAppBar_fabAlignmentMode
            int r8 = r2.getInt(r8, r10)
            r9.fabAlignmentMode = r8
            int r8 = com.google.android.material.R$styleable.BottomAppBar_fabAnimationMode
            r2.getInt(r8, r10)
            int r8 = com.google.android.material.R$styleable.BottomAppBar_hideOnScroll
            boolean r8 = r2.getBoolean(r8, r10)
            r9.hideOnScroll = r8
            int r8 = com.google.android.material.R$styleable.BottomAppBar_paddingBottomSystemWindowInsets
            boolean r8 = r2.getBoolean(r8, r10)
            r9.paddingBottomSystemWindowInsets = r8
            int r8 = com.google.android.material.R$styleable.BottomAppBar_paddingLeftSystemWindowInsets
            boolean r8 = r2.getBoolean(r8, r10)
            r9.paddingLeftSystemWindowInsets = r8
            int r8 = com.google.android.material.R$styleable.BottomAppBar_paddingRightSystemWindowInsets
            boolean r10 = r2.getBoolean(r8, r10)
            r9.paddingRightSystemWindowInsets = r10
            r2.recycle()
            android.content.res.Resources r10 = r9.getResources()
            int r2 = com.google.android.material.R$dimen.mtrl_bottomappbar_fabOffsetEndMode
            int r10 = r10.getDimensionPixelOffset(r2)
            r9.fabOffsetEndMode = r10
            com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment r10 = new com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment
            r10.<init>(r5, r6, r7)
            com.google.android.material.shape.ShapeAppearanceModel$Builder r2 = com.google.android.material.shape.ShapeAppearanceModel.builder()
            r2.setTopEdge(r10)
            com.google.android.material.shape.ShapeAppearanceModel r10 = r2.build()
            com.google.android.material.shape.MaterialShapeDrawable r2 = r9.materialShapeDrawable
            r2.setShapeAppearanceModel(r10)
            com.google.android.material.shape.MaterialShapeDrawable r10 = r9.materialShapeDrawable
            r2 = 2
            r10.setShadowCompatibilityMode(r2)
            com.google.android.material.shape.MaterialShapeDrawable r10 = r9.materialShapeDrawable
            android.graphics.Paint$Style r2 = android.graphics.Paint.Style.FILL
            r10.setPaintStyle(r2)
            com.google.android.material.shape.MaterialShapeDrawable r10 = r9.materialShapeDrawable
            r10.initializeElevationOverlay(r1)
            float r10 = (float) r4
            r9.setElevation(r10)
            com.google.android.material.shape.MaterialShapeDrawable r10 = r9.materialShapeDrawable
            androidx.core.graphics.drawable.DrawableCompat.setTintList(r10, r3)
            com.google.android.material.shape.MaterialShapeDrawable r10 = r9.materialShapeDrawable
            androidx.core.view.ViewCompat.setBackground(r9, r10)
            com.google.android.material.bottomappbar.BottomActionbar$3 r10 = new com.google.android.material.bottomappbar.BottomActionbar$3
            r10.<init>()
            com.google.android.material.internal.ViewUtils.doOnApplyWindowInsets(r9, r11, r12, r0, r10)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.bottomappbar.BottomActionbar.<init>(android.content.Context, android.util.AttributeSet, int):void");
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!this.mLimitDivider) {
            addDividerLine();
        }
    }

    private void addDividerLine() {
        if (this.mDividerLine == null) {
            this.mDividerLine = new View(getContext());
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, getResources().getDimensionPixelSize(R$dimen.op_control_divider_height_standard));
            layoutParams.gravity = 48;
            this.mDividerLine.setLayoutParams(layoutParams);
            this.mDividerLine.setBackgroundColor(getResources().getColor(R$color.op_control_divider_color_default));
            addView(this.mDividerLine);
        }
    }

    public boolean getHideOnScroll() {
        return this.hideOnScroll;
    }

    @Override // android.view.View
    public void setElevation(float f) {
        this.materialShapeDrawable.setElevation(f);
        getBehavior().setAdditionalHiddenOffsetY(this, this.materialShapeDrawable.getShadowRadius() - this.materialShapeDrawable.getShadowOffsetY());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchAnimationStart() {
        ArrayList<AnimationListener> arrayList;
        int i = this.animatingModeChangeCounter;
        this.animatingModeChangeCounter = i + 1;
        if (i == 0 && (arrayList = this.animationListeners) != null) {
            Iterator<AnimationListener> it = arrayList.iterator();
            while (it.hasNext()) {
                it.next().onAnimationStart(this);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchAnimationEnd() {
        ArrayList<AnimationListener> arrayList;
        int i = this.animatingModeChangeCounter - 1;
        this.animatingModeChangeCounter = i;
        if (i == 0 && (arrayList = this.animationListeners) != null) {
            Iterator<AnimationListener> it = arrayList.iterator();
            while (it.hasNext()) {
                it.next().onAnimationEnd(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setFabDiameter(int i) {
        float f = (float) i;
        if (f == getTopEdgeTreatment().getFabDiameter()) {
            return false;
        }
        getTopEdgeTreatment().setFabDiameter(f);
        this.materialShapeDrawable.invalidateSelf();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private FloatingActionButton findDependentFab() {
        View findDependentView = findDependentView();
        if (findDependentView instanceof FloatingActionButton) {
            return (FloatingActionButton) findDependentView;
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x001e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.view.View findDependentView() {
        /*
            r3 = this;
            android.view.ViewParent r0 = r3.getParent()
            boolean r0 = r0 instanceof androidx.coordinatorlayout.widget.CoordinatorLayout
            r1 = 0
            if (r0 != 0) goto L_0x000a
            return r1
        L_0x000a:
            android.view.ViewParent r0 = r3.getParent()
            androidx.coordinatorlayout.widget.CoordinatorLayout r0 = (androidx.coordinatorlayout.widget.CoordinatorLayout) r0
            java.util.List r3 = r0.getDependents(r3)
            java.util.Iterator r3 = r3.iterator()
        L_0x0018:
            boolean r0 = r3.hasNext()
            if (r0 == 0) goto L_0x0031
            java.lang.Object r0 = r3.next()
            android.view.View r0 = (android.view.View) r0
            boolean r2 = r0 instanceof com.google.android.material.floatingactionbutton.FloatingActionButton
            if (r2 != 0) goto L_0x0030
            boolean r2 = r0 instanceof com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
            if (r2 != 0) goto L_0x0030
            boolean r2 = r0 instanceof android.widget.Button
            if (r2 == 0) goto L_0x0018
        L_0x0030:
            return r0
        L_0x0031:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.bottomappbar.BottomActionbar.findDependentView():android.view.View");
    }

    private boolean isFabVisibleOrWillBeShown() {
        FloatingActionButton findDependentFab = findDependentFab();
        return findDependentFab != null && findDependentFab.isOrWillBeShown();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeAnimateMenuView(int i, boolean z) {
        if (ViewCompat.isLaidOut(this)) {
            Animator animator = this.menuAnimator;
            if (animator != null) {
                animator.cancel();
            }
            ArrayList arrayList = new ArrayList();
            if (!isFabVisibleOrWillBeShown()) {
                i = 0;
                z = false;
            }
            createMenuViewTranslationAnimation(i, z, arrayList);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(arrayList);
            this.menuAnimator = animatorSet;
            animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.google.android.material.bottomappbar.BottomActionbar.6
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator2) {
                    BottomActionbar.this.dispatchAnimationStart();
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator2) {
                    BottomActionbar.this.dispatchAnimationEnd();
                    BottomActionbar.this.menuAnimator = null;
                }
            });
            this.menuAnimator.start();
        }
    }

    private void createMenuViewTranslationAnimation(final int i, final boolean z, List<Animator> list) {
        final ActionMenuView actionMenuView = getActionMenuView();
        if (actionMenuView != null) {
            Animator ofFloat = ObjectAnimator.ofFloat(actionMenuView, "alpha", 1.0f);
            if (Math.abs(actionMenuView.getTranslationX() - ((float) getActionMenuViewTranslationX(actionMenuView, i, z))) > 1.0f) {
                ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(actionMenuView, "alpha", 0.0f);
                ofFloat2.addListener(new AnimatorListenerAdapter() { // from class: com.google.android.material.bottomappbar.BottomActionbar.7
                    public boolean cancelled;

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationCancel(Animator animator) {
                        this.cancelled = true;
                    }

                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationEnd(Animator animator) {
                        if (!this.cancelled) {
                            BottomActionbar.this.translateActionMenuView(actionMenuView, i, z);
                        }
                    }
                });
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.setDuration(150L);
                animatorSet.playSequentially(ofFloat2, ofFloat);
                list.add(animatorSet);
            } else if (actionMenuView.getAlpha() < 1.0f) {
                list.add(ofFloat);
            }
        }
    }

    private float getFabTranslationY() {
        return -getTopEdgeTreatment().getCradleVerticalOffset();
    }

    private float getFabTranslationX(int i) {
        boolean isLayoutRtl = ViewUtils.isLayoutRtl(this);
        int i2 = 1;
        if (i != 1) {
            return 0.0f;
        }
        int measuredWidth = (getMeasuredWidth() / 2) - (this.fabOffsetEndMode + (isLayoutRtl ? this.leftInset : this.rightInset));
        if (isLayoutRtl) {
            i2 = -1;
        }
        return (float) (measuredWidth * i2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getFabTranslationX() {
        return getFabTranslationX(this.fabAlignmentMode);
    }

    private ActionMenuView getActionMenuView() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof ActionMenuView) {
                return (ActionMenuView) childAt;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void translateActionMenuView(ActionMenuView actionMenuView, int i, boolean z) {
        actionMenuView.setTranslationX((float) getActionMenuViewTranslationX(actionMenuView, i, z));
    }

    /* access modifiers changed from: protected */
    public int getActionMenuViewTranslationX(ActionMenuView actionMenuView, int i, boolean z) {
        if (i != 1 || !z) {
            return 0;
        }
        boolean isLayoutRtl = ViewUtils.isLayoutRtl(this);
        int measuredWidth = isLayoutRtl ? getMeasuredWidth() : 0;
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            View childAt = getChildAt(i2);
            if ((childAt.getLayoutParams() instanceof Toolbar.LayoutParams) && (((Toolbar.LayoutParams) childAt.getLayoutParams()).gravity & 8388615) == 8388611) {
                if (isLayoutRtl) {
                    measuredWidth = Math.min(measuredWidth, childAt.getLeft());
                } else {
                    measuredWidth = Math.max(measuredWidth, childAt.getRight());
                }
            }
        }
        return measuredWidth - ((isLayoutRtl ? actionMenuView.getRight() : actionMenuView.getLeft()) + (isLayoutRtl ? this.rightInset : -this.leftInset));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelAnimations() {
        Animator animator = this.menuAnimator;
        if (animator != null) {
            animator.cancel();
        }
        Animator animator2 = this.modeAnimator;
        if (animator2 != null) {
            animator2.cancel();
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.widget.Toolbar, android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        int width = getWidth();
        if (z) {
            cancelAnimations();
            setCutoutState();
        }
        View view = this.mDividerLine;
        if (view != null) {
            view.layout(0, 0, width, 2);
        }
        setActionMenuViewPosition();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private BottomAppBarTopEdgeTreatment getTopEdgeTreatment() {
        return (BottomAppBarTopEdgeTreatment) this.materialShapeDrawable.getShapeAppearanceModel().getTopEdge();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCutoutState() {
        int i;
        Resources resources;
        getTopEdgeTreatment().setHorizontalOffset(getFabTranslationX());
        View findDependentView = findDependentView();
        this.materialShapeDrawable.setInterpolation((!this.fabAttached || !isFabVisibleOrWillBeShown()) ? 0.0f : 1.0f);
        if (findDependentView != null) {
            findDependentView.setTranslationY(getFabTranslationY());
            if (!(findDependentView instanceof Button)) {
                findDependentView.setTranslationX(getFabTranslationX());
            } else if (((float) findDependentView.getMeasuredWidth()) > getFabTranslationX()) {
                if (getContext().getResources().getDisplayMetrics().densityDpi >= 520) {
                    resources = getContext().getResources();
                    i = R$dimen.max_bottomappbar_width;
                } else {
                    resources = getContext().getResources();
                    i = R$dimen.max_bottomappbar_width_normal;
                }
                int dimensionPixelOffset = resources.getDimensionPixelOffset(i);
                Log.d("chenhb", "maxwidth = " + dimensionPixelOffset);
                findDependentView.setTranslationX((float) (((getMeasuredWidth() - getResources().getDimensionPixelOffset(R$dimen.op_control_margin_screen_left3)) - Math.min(findDependentView.getMeasuredWidth(), dimensionPixelOffset)) - ((getMeasuredWidth() / 2) - (Math.min(findDependentView.getMeasuredWidth(), dimensionPixelOffset) / 2))));
            } else {
                findDependentView.setTranslationX(getFabTranslationX());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setActionMenuViewPosition() {
        ActionMenuView actionMenuView = getActionMenuView();
        if (actionMenuView != null) {
            actionMenuView.setAlpha(1.0f);
            translateActionMenuView(actionMenuView, this.fabAlignmentMode, this.fabAttached);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addFabAnimationListeners(FloatingActionButton floatingActionButton) {
        floatingActionButton.addOnHideAnimationListener(this.fabAnimationListener);
        floatingActionButton.addOnShowAnimationListener(new AnimatorListenerAdapter() { // from class: com.google.android.material.bottomappbar.BottomActionbar.8
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                BottomActionbar.this.fabAnimationListener.onAnimationStart(animator);
                FloatingActionButton findDependentFab = BottomActionbar.this.findDependentFab();
                if (findDependentFab != null) {
                    findDependentFab.setTranslationX(BottomActionbar.this.getFabTranslationX());
                }
            }
        });
        floatingActionButton.addTransformationCallback(this.fabTransformationCallback);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getBottomInset() {
        return this.bottomInset;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getRightInset() {
        return this.rightInset;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getLeftInset() {
        return this.leftInset;
    }

    @Override // androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
    public Behavior getBehavior() {
        if (this.behavior == null) {
            this.behavior = new Behavior(getContext());
        }
        return this.behavior;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        MaterialShapeUtils.setParentAbsoluteElevation(this, this.materialShapeDrawable);
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).setClipChildren(false);
        }
    }

    public static class Behavior extends HideBottomViewOnScrollBehavior<BottomActionbar> {
        private final View.OnLayoutChangeListener buttonLayoutListener = new View.OnLayoutChangeListener() { // from class: com.google.android.material.bottomappbar.BottomActionbar.Behavior.2
            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                int i9;
                Resources resources;
                BottomActionbar bottomActionbar = (BottomActionbar) Behavior.this.viewRef.get();
                if (bottomActionbar == null || !(view instanceof Button)) {
                    view.removeOnLayoutChangeListener(this);
                    return;
                }
                Button button = (Button) view;
                int i10 = Behavior.this.mContext.getResources().getDisplayMetrics().densityDpi;
                if (i10 >= 520) {
                    resources = Behavior.this.mContext.getResources();
                    i9 = R$dimen.max_bottomappbar_width;
                } else {
                    resources = Behavior.this.mContext.getResources();
                    i9 = R$dimen.max_bottomappbar_width_normal;
                }
                int dimensionPixelOffset = resources.getDimensionPixelOffset(i9);
                Log.d("chenhb", "maxwidth = " + dimensionPixelOffset + ",densityDpi = " + i10);
                button.setMaxWidth(dimensionPixelOffset);
                button.setMaxLines(2);
                button.setEllipsize(TextUtils.TruncateAt.END);
                Behavior.this.fabContentRect.set(0, 0, button.getMeasuredWidth(), button.getMeasuredHeight());
                int height = Behavior.this.fabContentRect.height();
                bottomActionbar.setFabDiameter(0);
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
                if (Behavior.this.originalBottomMargin == 0) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = bottomActionbar.getBottomInset() + (bottomActionbar.getResources().getDimensionPixelOffset(NavigationButtonUtils.is3ButtonNavigationBar(Behavior.this.mContext) ? R$dimen.op_control_margin_screen_bottom2 : R$dimen.op_control_margin_screen_bottom3) - ((button.getMeasuredHeight() - height) / 2));
                    ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin = bottomActionbar.getLeftInset();
                    ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = bottomActionbar.getRightInset();
                    if (ViewUtils.isLayoutRtl(button)) {
                        ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin += bottomActionbar.fabOffsetEndMode;
                    } else {
                        ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin += bottomActionbar.fabOffsetEndMode;
                    }
                }
            }
        };
        private final Rect fabContentRect = new Rect();
        private final View.OnLayoutChangeListener fabLayoutListener = new View.OnLayoutChangeListener() { // from class: com.google.android.material.bottomappbar.BottomActionbar.Behavior.1
            @Override // android.view.View.OnLayoutChangeListener
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                BottomActionbar bottomActionbar = (BottomActionbar) Behavior.this.viewRef.get();
                if (bottomActionbar == null || !(view instanceof FloatingActionButton)) {
                    view.removeOnLayoutChangeListener(this);
                    return;
                }
                FloatingActionButton floatingActionButton = (FloatingActionButton) view;
                floatingActionButton.getMeasuredContentRect(Behavior.this.fabContentRect);
                int height = Behavior.this.fabContentRect.height();
                bottomActionbar.setFabDiameter(0);
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
                if (Behavior.this.originalBottomMargin == 0) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = bottomActionbar.getBottomInset() + (bottomActionbar.getResources().getDimensionPixelOffset(NavigationButtonUtils.is3ButtonNavigationBar(Behavior.this.mContext) ? R$dimen.op_control_margin_screen_bottom1 : R$dimen.op_control_margin_screen_bottom3) - ((floatingActionButton.getMeasuredHeight() - height) / 2));
                    ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin = bottomActionbar.getLeftInset();
                    ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = bottomActionbar.getRightInset();
                    if (ViewUtils.isLayoutRtl(floatingActionButton)) {
                        ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin += bottomActionbar.fabOffsetEndMode;
                    } else {
                        ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin += bottomActionbar.fabOffsetEndMode;
                    }
                }
            }
        };
        private Context mContext;
        private int originalBottomMargin;
        private WeakReference<BottomActionbar> viewRef;

        public Behavior(Context context) {
            this.mContext = context;
        }

        public Behavior(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mContext = context;
        }

        public boolean onLayoutChild(CoordinatorLayout coordinatorLayout, BottomActionbar bottomActionbar, int i) {
            this.viewRef = new WeakReference<>(bottomActionbar);
            View findDependentView = bottomActionbar.findDependentView();
            if (findDependentView != null && !ViewCompat.isLaidOut(findDependentView)) {
                CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) findDependentView.getLayoutParams();
                layoutParams.anchorGravity = 17;
                this.originalBottomMargin = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
                if (findDependentView instanceof FloatingActionButton) {
                    FloatingActionButton floatingActionButton = (FloatingActionButton) findDependentView;
                    floatingActionButton.addOnLayoutChangeListener(this.fabLayoutListener);
                    bottomActionbar.addFabAnimationListeners(floatingActionButton);
                } else if (findDependentView instanceof Button) {
                    ((Button) findDependentView).addOnLayoutChangeListener(this.buttonLayoutListener);
                }
                bottomActionbar.setCutoutState();
            }
            coordinatorLayout.onLayoutChild(bottomActionbar, i);
            return super.onLayoutChild(coordinatorLayout, (CoordinatorLayout) bottomActionbar, i);
        }

        public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, BottomActionbar bottomActionbar, View view, View view2, int i, int i2) {
            return bottomActionbar.getHideOnScroll() && super.onStartNestedScroll(coordinatorLayout, bottomActionbar, view, view2, i, i2);
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.widget.Toolbar, android.view.View
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.fabAlignmentMode = this.fabAlignmentMode;
        savedState.fabAttached = this.fabAttached;
        return savedState;
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.widget.Toolbar, android.view.View
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.fabAlignmentMode = savedState.fabAlignmentMode;
        this.fabAttached = savedState.fabAttached;
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends AbsSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.ClassLoaderCreator<SavedState>() { // from class: com.google.android.material.bottomappbar.BottomActionbar.SavedState.1
            @Override // android.os.Parcelable.ClassLoaderCreator
            public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SavedState(parcel, classLoader);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel, null);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        int fabAlignmentMode;
        boolean fabAttached;

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            this.fabAlignmentMode = parcel.readInt();
            this.fabAttached = parcel.readInt() != 0;
        }

        @Override // androidx.customview.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.fabAlignmentMode);
            parcel.writeInt(this.fabAttached ? 1 : 0);
        }
    }
}
