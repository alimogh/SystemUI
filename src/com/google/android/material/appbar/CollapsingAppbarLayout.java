package com.google.android.material.appbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.math.MathUtils;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.R$attr;
import com.google.android.material.R$dimen;
import com.google.android.material.R$id;
import com.google.android.material.R$style;
import com.google.android.material.R$styleable;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.emptyview.EmptyPageView;
import com.google.android.material.internal.MultiCollapsingHelper;
import com.google.android.material.internal.ThemeEnforcement;
import java.math.BigInteger;
public class CollapsingAppbarLayout extends FrameLayout {
    private static final int DEFAYLT_RES_STYLE = R$style.Widget_Design_CollapsingAppbarLayout;
    private Appbar appbar;
    final MultiCollapsingHelper collapsingTextHelper;
    private boolean collapsingTitleEnabled;
    private Drawable contentScrim;
    int currentOffset;
    private boolean drawCollapsingTitle;
    private View dummyView;
    private int expandedMarginBottom;
    private int expandedMarginEnd;
    private int expandedMarginStart;
    private int expandedMarginTop;
    private boolean isOverFling;
    WindowInsetsCompat lastInsets;
    private final int mAppbarHeight;
    private boolean mCollapsed;
    private ColorStateList mCollapsedSubtitleTextColor;
    private ColorStateList mCollapsedTitleTextColor;
    private boolean mDisableCollapsed;
    private EmptyPageView mEmptyPageView;
    private ColorStateList mExpandSubtitleTextColor;
    private ColorStateList mExpandedTitleTextColor;
    private float mFraction;
    private ImageView mImageView;
    private boolean mPrepareDraw;
    private boolean mStillDisScroll;
    private OnSubTitleClickListener mSubTitleClickListener;
    private TextView mSubtitleView;
    private CharSequence mSyncText;
    private TextView mSyncTextView;
    private TextView mTextView;
    private int menuMargin;
    private Toolbar menuToolbar;
    private int minHeight;
    private OnFractionChangeListener onCollapsedSyncListener;
    private OnFractionChangeListener onFractionChangeListener;
    private AppBarLayout.OnOffsetChangedListener onOffsetChangedListener;
    private boolean refreshTitlebar;
    private int scrimAlpha;
    private long scrimAnimationDuration;
    private ValueAnimator scrimAnimator;
    private int scrimVisibleHeightTrigger;
    private boolean scrimsAreShown;
    Drawable statusBarScrim;
    private CharSequence syncSubtitile;
    private View titlebarDirectChild;
    private final Rect tmpRect;

    public interface OnFractionChangeListener {
        void onFractionChanged(float f);
    }

    public interface OnSubTitleClickListener {
        void onClick();
    }

    public CollapsingAppbarLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.collapsingAppbarStyle);
    }

    public CollapsingAppbarLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Drawable drawable;
        this.refreshTitlebar = true;
        this.tmpRect = new Rect();
        this.scrimVisibleHeightTrigger = -1;
        this.mPrepareDraw = true;
        this.mCollapsed = false;
        this.mDisableCollapsed = false;
        this.mFraction = 1.0f;
        MultiCollapsingHelper multiCollapsingHelper = new MultiCollapsingHelper(this);
        this.collapsingTextHelper = multiCollapsingHelper;
        multiCollapsingHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
        TypedArray obtainStyledAttributes = ThemeEnforcement.obtainStyledAttributes(context, attributeSet, R$styleable.CollapsingAppbarLayout, i, DEFAYLT_RES_STYLE, new int[0]);
        this.collapsingTextHelper.setExpandedTextGravity(obtainStyledAttributes.getInt(R$styleable.CollapsingAppbarLayout_expandedTitleGravity, 8388691));
        this.collapsingTextHelper.setCollapsedTextGravity(obtainStyledAttributes.getInt(R$styleable.CollapsingAppbarLayout_collapsedTitleGravity, 8388627));
        this.collapsingTextHelper.setDrawLine(obtainStyledAttributes.getBoolean(R$styleable.CollapsingAppbarLayout_drawLineEnabled, true));
        int dimensionPixelSize = obtainStyledAttributes.getDimensionPixelSize(R$styleable.CollapsingAppbarLayout_expandedTitleMargin, 0);
        this.expandedMarginBottom = dimensionPixelSize;
        this.expandedMarginEnd = dimensionPixelSize;
        this.expandedMarginTop = dimensionPixelSize;
        this.expandedMarginStart = dimensionPixelSize;
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_android_background)) {
            setBackground(obtainStyledAttributes.getDrawable(R$styleable.CollapsingAppbarLayout_android_background));
        }
        this.mAppbarHeight = obtainStyledAttributes.getDimensionPixelSize(R$styleable.CollapsingAppbarLayout_collapsedAppbarHeight, 0);
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_expandedTitleMarginStart)) {
            this.expandedMarginStart = obtainStyledAttributes.getDimensionPixelSize(R$styleable.CollapsingAppbarLayout_expandedTitleMarginStart, 0);
        }
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_expandedTitleMarginEnd)) {
            this.expandedMarginEnd = obtainStyledAttributes.getDimensionPixelSize(R$styleable.CollapsingAppbarLayout_expandedTitleMarginEnd, 0);
        }
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_expandedTitleMarginTop)) {
            this.expandedMarginTop = obtainStyledAttributes.getDimensionPixelSize(R$styleable.CollapsingAppbarLayout_expandedTitleMarginTop, 0);
        }
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_expandedTitleMarginBottom)) {
            this.expandedMarginBottom = obtainStyledAttributes.getDimensionPixelSize(R$styleable.CollapsingAppbarLayout_expandedTitleMarginBottom, 0);
        }
        this.collapsingTitleEnabled = obtainStyledAttributes.getBoolean(R$styleable.CollapsingAppbarLayout_titleEnabled, true);
        this.mDisableCollapsed = obtainStyledAttributes.getBoolean(R$styleable.CollapsingAppbarLayout_disableCollapsed, false);
        isInsetSubtitleImage(obtainStyledAttributes.getBoolean(R$styleable.CollapsingAppbarLayout_insetSubtitleIconFlag, false));
        setTitle(obtainStyledAttributes.getText(R$styleable.CollapsingAppbarLayout_title));
        setSubtitle(obtainStyledAttributes.getText(R$styleable.CollapsingAppbarLayout_subtitle));
        this.collapsingTextHelper.setExpandedTitleAppearance(R$style.TextAppearance_Design_MultiCollapsingTitle_ExpandedTitle);
        this.collapsingTextHelper.setCollapsedTitleAppearance(R$style.TextAppearance_AppCompat_Widget_ActionBar_Title);
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_expandedTitleTextAppearance)) {
            this.collapsingTextHelper.setExpandedTitleAppearance(obtainStyledAttributes.getResourceId(R$styleable.CollapsingAppbarLayout_expandedTitleTextAppearance, 0));
        }
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_collapsedTitleTextAppearance)) {
            this.collapsingTextHelper.setCollapsedTitleAppearance(obtainStyledAttributes.getResourceId(R$styleable.CollapsingAppbarLayout_collapsedTitleTextAppearance, 0));
        }
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_expandedTitleTextColor)) {
            ColorStateList colorStateList = obtainStyledAttributes.getColorStateList(R$styleable.CollapsingAppbarLayout_expandedTitleTextColor);
            this.mExpandedTitleTextColor = colorStateList;
            this.collapsingTextHelper.setExpandedTitleColor(colorStateList);
        }
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_collapsedTitleTextColor)) {
            ColorStateList colorStateList2 = obtainStyledAttributes.getColorStateList(R$styleable.CollapsingAppbarLayout_collapsedTitleTextColor);
            this.mCollapsedTitleTextColor = colorStateList2;
            this.collapsingTextHelper.setCollapsedTitleColor(colorStateList2);
        }
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_expandedSubtitleTextColor)) {
            ColorStateList colorStateList3 = obtainStyledAttributes.getColorStateList(R$styleable.CollapsingAppbarLayout_expandedSubtitleTextColor);
            this.mExpandSubtitleTextColor = colorStateList3;
            this.collapsingTextHelper.setExpandedSubtitleColor(colorStateList3);
        }
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_collapsedSubtitleTextColor)) {
            ColorStateList colorStateList4 = obtainStyledAttributes.getColorStateList(R$styleable.CollapsingAppbarLayout_collapsedSubtitleTextColor);
            this.mCollapsedSubtitleTextColor = colorStateList4;
            this.collapsingTextHelper.setCollapsedSubtitleColor(colorStateList4);
        }
        this.collapsingTextHelper.setExpandedSubtitleAppearance(R$style.TextAppearance_Design_MultiCollapsingTitle_ExpandedSubtitle);
        this.collapsingTextHelper.setCollapsedSubtitleAppearance(R$style.TextAppearance_AppCompat_Widget_ActionBar_Subtitle);
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_expandedSubtitleTextAppearance)) {
            this.collapsingTextHelper.setExpandedSubtitleAppearance(obtainStyledAttributes.getResourceId(R$styleable.CollapsingAppbarLayout_expandedSubtitleTextAppearance, 0));
        }
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_collapsedSubtitleTextAppearance)) {
            this.collapsingTextHelper.setCollapsedSubtitleAppearance(obtainStyledAttributes.getResourceId(R$styleable.CollapsingAppbarLayout_collapsedSubtitleTextAppearance, 0));
        }
        if (obtainStyledAttributes.hasValue(R$styleable.CollapsingAppbarLayout_subtitleIcon) && (drawable = getResources().getDrawable(obtainStyledAttributes.getResourceId(R$styleable.CollapsingAppbarLayout_subtitleIcon, 0))) != null) {
            setSubtitleImage(drawableToSubtitleBitmap(drawable));
        }
        this.scrimVisibleHeightTrigger = obtainStyledAttributes.getDimensionPixelSize(R$styleable.CollapsingAppbarLayout_scrimVisibleHeightTrigger, -1);
        this.scrimAnimationDuration = (long) obtainStyledAttributes.getInt(R$styleable.CollapsingAppbarLayout_scrimAnimationDuration, 425);
        setContentScrim(obtainStyledAttributes.getDrawable(R$styleable.CollapsingAppbarLayout_contentScrim));
        setStatusBarScrim(obtainStyledAttributes.getDrawable(R$styleable.CollapsingAppbarLayout_statusBarScrim));
        int dimensionPixelOffset = obtainStyledAttributes.getDimensionPixelOffset(R$styleable.CollapsingAppbarLayout_android_minHeight, -1);
        this.minHeight = dimensionPixelOffset;
        if (dimensionPixelOffset > 0) {
            setMinimumHeight(dimensionPixelOffset);
        }
        if (this.mDisableCollapsed) {
            setMinimumHeight(this.mAppbarHeight);
        }
        obtainStyledAttributes.recycle();
        setWillNotDraw(false);
        ViewCompat.setOnApplyWindowInsetsListener(this, new OnApplyWindowInsetsListener() { // from class: com.google.android.material.appbar.CollapsingAppbarLayout.1
            @Override // androidx.core.view.OnApplyWindowInsetsListener
            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
                return CollapsingAppbarLayout.this.onWindowInsetChanged(windowInsetsCompat);
            }
        });
    }

    public Bitmap drawableToSubtitleBitmap(Drawable drawable) {
        int dimensionPixelOffset = getResources().getDimensionPixelOffset(R$dimen.op_control_icon_size_indicator);
        int dimensionPixelOffset2 = getResources().getDimensionPixelOffset(R$dimen.op_control_icon_size_indicator);
        Bitmap createBitmap = Bitmap.createBitmap(dimensionPixelOffset, dimensionPixelOffset2, drawable.getOpacity() != -1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(createBitmap);
        drawable.setBounds(0, 0, dimensionPixelOffset, dimensionPixelOffset2);
        drawable.draw(canvas);
        return createBitmap;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        if (parent instanceof AppBarLayout) {
            ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows((View) parent));
            if (this.onOffsetChangedListener == null) {
                this.onOffsetChangedListener = new OffsetUpdateListener();
            }
            ((AppBarLayout) parent).addOnOffsetChangedListener(this.onOffsetChangedListener);
            ViewCompat.requestApplyInsets(this);
            if (this.mTextView == null) {
                this.mTextView = new TextView(getContext());
                LayoutParams layoutParams = new LayoutParams(-2, -2);
                layoutParams.collapseMode = 1;
                this.mTextView.setLayoutParams(layoutParams);
            }
            if (this.mSubtitleView == null) {
                this.mSubtitleView = new TextView(getContext());
                LayoutParams layoutParams2 = new LayoutParams(-2, -2);
                layoutParams2.collapseMode = 1;
                this.mSubtitleView.setLayoutParams(layoutParams2);
            }
            if (this.mSyncTextView == null) {
                TextView textView = new TextView(getContext());
                this.mSyncTextView = textView;
                textView.setOnClickListener(new View.OnClickListener() { // from class: com.google.android.material.appbar.CollapsingAppbarLayout.2
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        if (CollapsingAppbarLayout.this.mSubTitleClickListener != null) {
                            CollapsingAppbarLayout.this.mSubTitleClickListener.onClick();
                        }
                    }
                });
                LayoutParams layoutParams3 = new LayoutParams(-2, -2);
                layoutParams3.collapseMode = 1;
                this.mSyncTextView.setLayoutParams(layoutParams3);
                addView(this.mSyncTextView);
                this.collapsingTextHelper.setSyncTextView(this.mSyncTextView);
            }
            addView(this.mTextView);
            addView(this.mSubtitleView);
            this.collapsingTextHelper.setTitleView(this.mTextView);
            this.collapsingTextHelper.setSubTitleView(this.mSubtitleView);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        ViewParent parent = getParent();
        AppBarLayout.OnOffsetChangedListener onOffsetChangedListener = this.onOffsetChangedListener;
        if (onOffsetChangedListener != null && (parent instanceof AppBarLayout)) {
            ((AppBarLayout) parent).removeOnOffsetChangedListener(onOffsetChangedListener);
        }
        if (this.onFractionChangeListener != null) {
            this.onFractionChangeListener = null;
        }
        if (this.mEmptyPageView != null) {
            this.mEmptyPageView = null;
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: package-private */
    public WindowInsetsCompat onWindowInsetChanged(WindowInsetsCompat windowInsetsCompat) {
        WindowInsetsCompat windowInsetsCompat2 = ViewCompat.getFitsSystemWindows(this) ? windowInsetsCompat : null;
        if (!ObjectsCompat.equals(this.lastInsets, windowInsetsCompat2)) {
            this.lastInsets = windowInsetsCompat2;
            requestLayout();
        }
        return windowInsetsCompat.consumeSystemWindowInsets();
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        Drawable drawable;
        super.draw(canvas);
        if (this.mFraction == this.collapsingTextHelper.getExpansionFraction()) {
            float f = this.mFraction;
            if (f == 1.0f) {
                if (f == 1.0f && this.mTextView.getTextSize() == this.collapsingTextHelper.getTitleViewCollapsedTextSize() && this.collapsingTextHelper.getIsDrawLineInit()) {
                    if (this.collapsingTitleEnabled && this.drawCollapsingTitle && this.mPrepareDraw) {
                        this.collapsingTextHelper.drawLine(canvas);
                        return;
                    }
                    return;
                }
            } else {
                return;
            }
        }
        ensureTitlebar();
        if (this.appbar == null && (drawable = this.contentScrim) != null && this.scrimAlpha > 0 && !this.mDisableCollapsed) {
            drawable.mutate().setAlpha(this.scrimAlpha);
            this.contentScrim.draw(canvas);
        }
        if (this.collapsingTitleEnabled && this.drawCollapsingTitle && this.mPrepareDraw) {
            if (this.mDisableCollapsed) {
                this.collapsingTextHelper.setExpansionFraction(1.0f);
            }
            this.collapsingTextHelper.draw(canvas);
        }
        if (this.statusBarScrim != null && this.scrimAlpha > 0) {
            WindowInsetsCompat windowInsetsCompat = this.lastInsets;
            int systemWindowInsetTop = windowInsetsCompat != null ? windowInsetsCompat.getSystemWindowInsetTop() : 0;
            if (systemWindowInsetTop > 0) {
                this.statusBarScrim.setBounds(0, -this.currentOffset, getWidth(), systemWindowInsetTop - this.currentOffset);
                this.statusBarScrim.mutate().setAlpha(this.scrimAlpha);
                this.statusBarScrim.draw(canvas);
            }
        }
        this.mFraction = this.collapsingTextHelper.getExpansionFraction();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean drawChild(Canvas canvas, View view, long j) {
        boolean z;
        if (this.contentScrim == null || this.scrimAlpha <= 0 || !isTitlebarChild(view)) {
            z = false;
        } else {
            this.contentScrim.mutate().setAlpha(this.scrimAlpha);
            this.contentScrim.draw(canvas);
            z = true;
        }
        return super.drawChild(canvas, view, j) || z;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        Drawable drawable = this.contentScrim;
        if (drawable != null) {
            drawable.setBounds(0, 0, i, i2);
        }
    }

    private void ensureTitlebar() {
        Appbar appbar;
        if (this.refreshTitlebar) {
            Toolbar toolbar = null;
            this.appbar = null;
            this.titlebarDirectChild = null;
            if (0 == 0) {
                int childCount = getChildCount();
                int i = 0;
                while (true) {
                    if (i >= childCount) {
                        appbar = null;
                        break;
                    }
                    View childAt = getChildAt(i);
                    if (childAt instanceof Appbar) {
                        appbar = (Appbar) childAt;
                        appbar.limitDividerShown(true);
                        break;
                    }
                    i++;
                }
                this.appbar = appbar;
            }
            if (this.menuToolbar == null) {
                int childCount2 = getChildCount();
                int i2 = 0;
                while (true) {
                    if (i2 >= childCount2) {
                        break;
                    }
                    View childAt2 = getChildAt(i2);
                    if (childAt2 instanceof Toolbar) {
                        toolbar = (Toolbar) childAt2;
                        break;
                    }
                    i2++;
                }
                this.menuToolbar = toolbar;
            }
            updateDummyView();
            this.refreshTitlebar = false;
        }
    }

    private boolean isTitlebarChild(View view) {
        View view2 = this.titlebarDirectChild;
        if (view2 == null || view2 == this) {
            if (view == this.appbar) {
                return true;
            }
        } else if (view == view2) {
            return true;
        }
        return false;
    }

    private void updateDummyView() {
        View view;
        if (!this.collapsingTitleEnabled && (view = this.dummyView) != null) {
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(this.dummyView);
            }
        }
        if (this.collapsingTitleEnabled && this.appbar != null) {
            if (this.dummyView == null) {
                this.dummyView = new View(getContext());
            }
            if (this.dummyView.getParent() == null) {
                this.appbar.addView(this.dummyView, -1, -1);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int i, int i2) {
        int i3 = this.minHeight;
        if (i3 > 0) {
            if (this.mDisableCollapsed) {
                i2 = View.MeasureSpec.makeMeasureSpec(this.mAppbarHeight, 1073741824);
            } else {
                i2 = View.MeasureSpec.makeMeasureSpec(i3, 1073741824);
            }
        }
        ensureTitlebar();
        super.onMeasure(i, i2);
        int mode = View.MeasureSpec.getMode(i2);
        WindowInsetsCompat windowInsetsCompat = this.lastInsets;
        int systemWindowInsetTop = windowInsetsCompat != null ? windowInsetsCompat.getSystemWindowInsetTop() : 0;
        if (mode == 0 && systemWindowInsetTop > 0) {
            super.onMeasure(i, View.MeasureSpec.makeMeasureSpec(getMeasuredHeight() + systemWindowInsetTop, 1073741824));
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:100:0x0237  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x01d4  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x01d7  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x01e8  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01fc  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x021a  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x021d  */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onLayout(boolean r10, int r11, int r12, int r13, int r14) {
        /*
        // Method dump skipped, instructions count: 760
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.appbar.CollapsingAppbarLayout.onLayout(boolean, int, int, int, int):void");
    }

    private static int getHeightWithMargins(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof ViewGroup.MarginLayoutParams)) {
            return view.getHeight();
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
        return view.getHeight() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;
    }

    static ViewOffsetHelper getViewOffsetHelper(View view) {
        ViewOffsetHelper viewOffsetHelper = (ViewOffsetHelper) view.getTag(R$id.view_offset_helper);
        if (viewOffsetHelper != null) {
            return viewOffsetHelper;
        }
        ViewOffsetHelper viewOffsetHelper2 = new ViewOffsetHelper(view);
        view.setTag(R$id.view_offset_helper, viewOffsetHelper2);
        return viewOffsetHelper2;
    }

    public void setTitle(CharSequence charSequence) {
        this.collapsingTextHelper.setTitle(charSequence);
        updateContentDescriptionFromTitle();
    }

    public CharSequence getTitle() {
        if (this.collapsingTitleEnabled) {
            return this.collapsingTextHelper.getTitle();
        }
        return null;
    }

    public void isInsetSubtitleImage(boolean z) {
        this.collapsingTextHelper.setInsetImage(z);
    }

    public void setSubtitleImage(Bitmap bitmap) {
        if (this.mImageView == null) {
            this.mImageView = new ImageView(getContext());
            int dimensionPixelOffset = getResources().getDimensionPixelOffset(R$dimen.op_control_icon_size_indicator);
            LayoutParams layoutParams = new LayoutParams(dimensionPixelOffset, dimensionPixelOffset);
            layoutParams.collapseMode = 1;
            this.mImageView.setLayoutParams(layoutParams);
            addView(this.mImageView, layoutParams);
        }
        ImageView imageView = this.mImageView;
        if (imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
        this.collapsingTextHelper.setImageDrawable(bitmap, this.mImageView);
    }

    public void setSubtitle(CharSequence charSequence) {
        this.collapsingTextHelper.setSubtitle(charSequence);
    }

    public void setScrimsShown(boolean z) {
        setScrimsShown(z, ViewCompat.isLaidOut(this) && !isInEditMode());
    }

    public void setScrimsShown(boolean z, boolean z2) {
        if (this.scrimsAreShown != z) {
            int i = 255;
            if (z2) {
                if (!z) {
                    i = 0;
                }
                animateScrim(i);
            } else {
                if (!z) {
                    i = 0;
                }
                setScrimAlpha(i);
            }
            this.scrimsAreShown = z;
        }
    }

    private void animateScrim(int i) {
        ensureTitlebar();
        ValueAnimator valueAnimator = this.scrimAnimator;
        if (valueAnimator == null) {
            ValueAnimator valueAnimator2 = new ValueAnimator();
            this.scrimAnimator = valueAnimator2;
            valueAnimator2.setDuration(this.scrimAnimationDuration);
            this.scrimAnimator.setInterpolator(i > this.scrimAlpha ? AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR : AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
            this.scrimAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.google.android.material.appbar.CollapsingAppbarLayout.3
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator3) {
                    CollapsingAppbarLayout.this.setScrimAlpha(((Integer) valueAnimator3.getAnimatedValue()).intValue());
                }
            });
        } else if (valueAnimator.isRunning()) {
            this.scrimAnimator.cancel();
        }
        this.scrimAnimator.setIntValues(this.scrimAlpha, i);
        this.scrimAnimator.start();
    }

    /* access modifiers changed from: package-private */
    public void setScrimAlpha(int i) {
        Appbar appbar;
        if (i != this.scrimAlpha) {
            if (!(this.contentScrim == null || (appbar = this.appbar) == null)) {
                ViewCompat.postInvalidateOnAnimation(appbar);
            }
            this.scrimAlpha = i;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setContentScrim(Drawable drawable) {
        Drawable drawable2 = this.contentScrim;
        if (drawable2 != drawable) {
            Drawable drawable3 = null;
            if (drawable2 != null) {
                drawable2.setCallback(null);
            }
            if (drawable != null) {
                drawable3 = drawable.mutate();
            }
            this.contentScrim = drawable3;
            if (drawable3 != null) {
                drawable3.setBounds(0, 0, getWidth(), getHeight());
                this.contentScrim.setCallback(this);
                this.contentScrim.setAlpha(this.scrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setStatusBarScrim(Drawable drawable) {
        Drawable drawable2 = this.statusBarScrim;
        if (drawable2 != drawable) {
            Drawable drawable3 = null;
            if (drawable2 != null) {
                drawable2.setCallback(null);
            }
            if (drawable != null) {
                drawable3 = drawable.mutate();
            }
            this.statusBarScrim = drawable3;
            if (drawable3 != null) {
                if (drawable3.isStateful()) {
                    this.statusBarScrim.setState(getDrawableState());
                }
                DrawableCompat.setLayoutDirection(this.statusBarScrim, ViewCompat.getLayoutDirection(this));
                this.statusBarScrim.setVisible(getVisibility() == 0, false);
                this.statusBarScrim.setCallback(this);
                this.statusBarScrim.setAlpha(this.scrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void drawableStateChanged() {
        super.drawableStateChanged();
        int[] drawableState = getDrawableState();
        Drawable drawable = this.statusBarScrim;
        boolean z = false;
        if (drawable != null && drawable.isStateful()) {
            z = false | drawable.setState(drawableState);
        }
        Drawable drawable2 = this.contentScrim;
        if (drawable2 != null && drawable2.isStateful()) {
            z |= drawable2.setState(drawableState);
        }
        MultiCollapsingHelper multiCollapsingHelper = this.collapsingTextHelper;
        if (multiCollapsingHelper != null) {
            z |= multiCollapsingHelper.setState(drawableState);
        }
        if (z) {
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean verifyDrawable(Drawable drawable) {
        return super.verifyDrawable(drawable) || drawable == this.contentScrim || drawable == this.statusBarScrim;
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        super.setVisibility(i);
        boolean z = i == 0;
        Drawable drawable = this.statusBarScrim;
        if (!(drawable == null || drawable.isVisible() == z)) {
            this.statusBarScrim.setVisible(z, false);
        }
        Drawable drawable2 = this.contentScrim;
        if (drawable2 != null && drawable2.isVisible() != z) {
            this.contentScrim.setVisible(z, false);
        }
    }

    public void setExpandedTitleAlpha(int i) {
        int defaultColor = this.collapsingTextHelper.getSyncColor().getDefaultColor();
        String hexString = Integer.toHexString((new BigInteger(Integer.toHexString(defaultColor).substring(0, 2), 16).intValue() * i) / 255);
        String substring = Integer.toHexString(defaultColor).substring(2);
        BigInteger bigInteger = new BigInteger(hexString + substring, 16);
        int defaultColor2 = this.collapsingTextHelper.getExpandedTextColor().getDefaultColor();
        String hexString2 = Integer.toHexString((new BigInteger(Integer.toHexString(defaultColor2).substring(0, 2), 16).intValue() * i) / 255);
        String substring2 = Integer.toHexString(defaultColor2).substring(2);
        BigInteger bigInteger2 = new BigInteger(hexString2 + substring2, 16);
        int defaultColor3 = this.collapsingTextHelper.getExpandedSubtitleColor().getDefaultColor();
        String hexString3 = Integer.toHexString((new BigInteger(Integer.toHexString(defaultColor3).substring(0, 2), 16).intValue() * i) / 255);
        String substring3 = Integer.toHexString(defaultColor3).substring(2);
        BigInteger bigInteger3 = new BigInteger(hexString3 + substring3, 16);
        this.collapsingTextHelper.setSyncTextColor(new ColorStateList(new int[][]{new int[]{16842910}, new int[0]}, new int[]{bigInteger.intValue(), defaultColor}));
        setExpandedTitleTextColor(new ColorStateList(new int[][]{new int[]{16842910}, new int[0]}, new int[]{bigInteger2.intValue(), defaultColor2}));
        setExpandedSubtitleTextColor(new ColorStateList(new int[][]{new int[]{16842910}, new int[0]}, new int[]{bigInteger3.intValue(), defaultColor3}));
        setCollapsedTitleTextColor(new ColorStateList(new int[][]{new int[]{16842910}, new int[0]}, new int[]{bigInteger2.intValue(), defaultColor2}));
        setCollapsedSubtitleTextColor(new ColorStateList(new int[][]{new int[]{16842910}, new int[0]}, new int[]{bigInteger3.intValue(), defaultColor3}));
        TextView textView = this.mTextView;
        if (textView != null) {
            textView.setAlpha(((float) i) / 255.0f);
        }
        ImageView imageView = this.mImageView;
        if (imageView != null) {
            imageView.setAlpha(((float) i) / 255.0f);
        }
        TextView textView2 = this.mSubtitleView;
        if (textView2 != null) {
            textView2.setAlpha(((float) i) / 255.0f);
        }
        TextView textView3 = this.mSyncTextView;
        if (textView3 != null) {
            textView3.setAlpha(((float) i) / 255.0f);
        }
    }

    public void setCollapsedTitleTextColor(ColorStateList colorStateList) {
        this.collapsingTextHelper.setCollapsedTitleColor(colorStateList);
        TextView textView = this.mTextView;
        if (textView != null) {
            textView.setTextColor(colorStateList);
        }
    }

    public void setCollapsedSubtitleTextColor(ColorStateList colorStateList) {
        this.collapsingTextHelper.setCollapsedSubtitleColor(colorStateList);
        TextView textView = this.mSubtitleView;
        if (textView != null) {
            textView.setTextColor(colorStateList);
        }
    }

    public boolean isExistSynsText() {
        return this.collapsingTextHelper.isExistSyncCloud();
    }

    public void setExpandedTitleTextColor(ColorStateList colorStateList) {
        this.collapsingTextHelper.setExpandedTitleColor(colorStateList);
        TextView textView = this.mTextView;
        if (textView != null) {
            textView.setTextColor(colorStateList);
        }
    }

    public void setExpandedSubtitleTextColor(ColorStateList colorStateList) {
        this.collapsingTextHelper.setExpandedSubtitleColor(colorStateList);
        TextView textView = this.mSubtitleView;
        if (textView != null) {
            textView.setTextColor(colorStateList);
        }
    }

    public void setPrepareDraw(boolean z) {
        this.mPrepareDraw = z;
    }

    public boolean isDisableScroll() {
        return this.mCollapsed;
    }

    public boolean isDisableCollapsed() {
        return this.mDisableCollapsed;
    }

    public int getScrimVisibleHeightTrigger() {
        int i = this.scrimVisibleHeightTrigger;
        if (i >= 0) {
            return i;
        }
        WindowInsetsCompat windowInsetsCompat = this.lastInsets;
        int systemWindowInsetTop = windowInsetsCompat != null ? windowInsetsCompat.getSystemWindowInsetTop() : 0;
        int minimumHeight = ViewCompat.getMinimumHeight(this);
        if (minimumHeight > 0) {
            return Math.min((minimumHeight * 2) + systemWindowInsetTop, getHeight());
        }
        return getHeight() / 3;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -1);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    public static class LayoutParams extends CollapsingToolbarLayout.LayoutParams {
        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public LayoutParams(int i, int i2) {
            super(i, i2);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }
    }

    /* access modifiers changed from: package-private */
    public final void updateScrimVisibility() {
        if (this.contentScrim != null || this.statusBarScrim != null) {
            setScrimsShown(getHeight() + this.currentOffset < getScrimVisibleHeightTrigger());
        }
    }

    /* access modifiers changed from: package-private */
    public final int getMaxOffsetForPinChild(View view) {
        return ((getHeight() - getViewOffsetHelper(view).getLayoutTop()) - view.getHeight()) - ((FrameLayout.LayoutParams) ((LayoutParams) view.getLayoutParams())).bottomMargin;
    }

    private void updateContentDescriptionFromTitle() {
        setContentDescription(getTitle());
    }

    private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
        OffsetUpdateListener() {
        }

        @Override // com.google.android.material.appbar.AppBarLayout.BaseOnOffsetChangedListener
        public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
            if (!(CollapsingAppbarLayout.this.mDisableCollapsed || CollapsingAppbarLayout.this.mCollapsed)) {
                CollapsingAppbarLayout collapsingAppbarLayout = CollapsingAppbarLayout.this;
                collapsingAppbarLayout.currentOffset = i;
                WindowInsetsCompat windowInsetsCompat = collapsingAppbarLayout.lastInsets;
                int systemWindowInsetTop = windowInsetsCompat != null ? windowInsetsCompat.getSystemWindowInsetTop() : 0;
                int childCount = CollapsingAppbarLayout.this.getChildCount();
                for (int i2 = 0; i2 < childCount; i2++) {
                    View childAt = CollapsingAppbarLayout.this.getChildAt(i2);
                    LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                    ViewOffsetHelper viewOffsetHelper = CollapsingAppbarLayout.getViewOffsetHelper(childAt);
                    int i3 = layoutParams.collapseMode;
                    if (i3 == 1) {
                        viewOffsetHelper.setTopAndBottomOffset(MathUtils.clamp(-i, 0, CollapsingAppbarLayout.this.getMaxOffsetForPinChild(childAt)));
                    } else if (i3 == 2) {
                        viewOffsetHelper.setTopAndBottomOffset(Math.round(((float) (-i)) * layoutParams.parallaxMult));
                    }
                }
                CollapsingAppbarLayout.this.updateScrimVisibility();
                CollapsingAppbarLayout collapsingAppbarLayout2 = CollapsingAppbarLayout.this;
                if (collapsingAppbarLayout2.statusBarScrim != null && systemWindowInsetTop > 0) {
                    ViewCompat.postInvalidateOnAnimation(collapsingAppbarLayout2);
                }
                int height = (CollapsingAppbarLayout.this.getHeight() - ViewCompat.getMinimumHeight(CollapsingAppbarLayout.this)) - systemWindowInsetTop;
                float f = (float) height;
                if (((float) Math.abs(i)) / f == 1.0f) {
                    CollapsingAppbarLayout.this.expandedMarginBottom = 0;
                }
                float abs = ((float) Math.abs(i)) / f;
                if (CollapsingAppbarLayout.this.mEmptyPageView != null) {
                    CollapsingAppbarLayout.this.mEmptyPageView.setEmptyPadding(CollapsingAppbarLayout.this.mEmptyPageView.getPaddingLeft(), (int) Math.abs(((float) CollapsingAppbarLayout.this.getResources().getDimensionPixelOffset(R$dimen.control_empty_image_margin_top)) * abs), CollapsingAppbarLayout.this.mEmptyPageView.getPaddingRight(), CollapsingAppbarLayout.this.mEmptyPageView.getEmptyPaddingBottom() - (CollapsingAppbarLayout.this.mEmptyPageView.getEmptyPaddingBottom() > height / 2 ? (int) Math.abs(((float) CollapsingAppbarLayout.this.getResources().getDimensionPixelOffset(R$dimen.control_empty_image_margin_top)) * abs) : 0));
                }
                if (!CollapsingAppbarLayout.this.isOverFling) {
                    CollapsingAppbarLayout.this.collapsingTextHelper.setExpansionFraction(abs);
                }
                if ((abs > 0.8f && abs < 1.0f && !CollapsingAppbarLayout.this.mStillDisScroll) || abs < 0.2f) {
                    CollapsingAppbarLayout.this.isOverFling = false;
                }
                if (CollapsingAppbarLayout.this.onFractionChangeListener != null) {
                    CollapsingAppbarLayout.this.onFractionChangeListener.onFractionChanged(abs);
                }
                if (CollapsingAppbarLayout.this.onCollapsedSyncListener != null) {
                    CollapsingAppbarLayout.this.onCollapsedSyncListener.onFractionChanged(abs);
                }
            }
        }
    }
}
