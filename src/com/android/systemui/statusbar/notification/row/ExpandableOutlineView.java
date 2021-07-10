package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import com.android.settingslib.Utils;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.statusbar.notification.AnimatableProperty;
import com.android.systemui.statusbar.notification.PropertyAnimator;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.oneplus.util.OpUtils;
public abstract class ExpandableOutlineView extends ExpandableView {
    private static final AnimatableProperty BOTTOM_ROUNDNESS = AnimatableProperty.from("bottomRoundness", $$Lambda$ExpandableOutlineView$ZLqiUGCQzNj3P4m8kfbTwbzfyaI.INSTANCE, $$Lambda$RLFq7_ULx7AWbuaAJNsAxNrN1PI.INSTANCE, C0008R$id.bottom_roundess_animator_tag, C0008R$id.bottom_roundess_animator_end_tag, C0008R$id.bottom_roundess_animator_start_tag);
    private static final Path EMPTY_PATH = new Path();
    private static final AnimationProperties ROUNDNESS_PROPERTIES;
    private static final AnimatableProperty TOP_ROUNDNESS = AnimatableProperty.from("topRoundness", $$Lambda$ExpandableOutlineView$lgIjKBD4iaJhFeEZ5izPzOddhds.INSTANCE, $$Lambda$iDWyu_PNFZfGQr9kcCLSWoFYxpI.INSTANCE, C0008R$id.top_roundess_animator_tag, C0008R$id.top_roundess_animator_end_tag, C0008R$id.top_roundess_animator_start_tag);
    private boolean mAlwaysRoundBothCorners;
    private int mBackgroundTop;
    private float mBottomRoundness;
    private final Path mClipPath = new Path();
    private float mCurrentBottomRoundness;
    private float mCurrentTopRoundness;
    private boolean mCustomOutline;
    private float mDistanceToTopRoundness = -1.0f;
    private boolean mIsHeadsUp;
    private boolean mIsNotificationShelf;
    private boolean mIsScrolledToTop;
    private boolean mIsSectionHeader;
    private float mOutlineAlpha = -1.0f;
    protected float mOutlineRadius;
    private final Rect mOutlineRect = new Rect();
    private final ViewOutlineProvider mProvider;
    private boolean mShouldClipNotifToOutline;
    protected boolean mShouldTranslateContents;
    private Paint mStrokePaint = new Paint();
    private float[] mTmpCornerRadii = new float[8];
    private Path mTmpPath = new Path();
    private boolean mTopAmountRounded;
    private float mTopRoundness;

    /* access modifiers changed from: protected */
    public boolean childNeedsClipping(View view) {
        return false;
    }

    public Path getCustomClipPath(View view) {
        return null;
    }

    public boolean topAmountNeedsClipping() {
        return true;
    }

    static {
        AnimationProperties animationProperties = new AnimationProperties();
        animationProperties.setDuration(360);
        ROUNDNESS_PROPERTIES = animationProperties;
    }

    /* access modifiers changed from: protected */
    public Path getClipPath(boolean z) {
        int i;
        int i2;
        int i3;
        int i4;
        float currentBackgroundRadiusTop = this.mAlwaysRoundBothCorners ? this.mOutlineRadius : getCurrentBackgroundRadiusTop();
        if (!this.mCustomOutline) {
            int translation = (!this.mShouldTranslateContents || z) ? 0 : (int) getTranslation();
            int i5 = (int) (this.mExtraWidthForClipping / 2.0f);
            i4 = Math.max(translation, 0) - i5;
            i3 = this.mClipTopAmount + this.mBackgroundTop;
            i2 = getWidth() + i5 + Math.min(translation, 0);
            i = Math.max(this.mMinimumHeightForClipping, Math.max(getActualHeight() - this.mClipBottomAmount, (int) (((float) i3) + currentBackgroundRadiusTop)));
        } else {
            Rect rect = this.mOutlineRect;
            i4 = rect.left;
            i3 = rect.top;
            i2 = rect.right;
            i = rect.bottom;
        }
        int i6 = i - i3;
        if (i6 == 0) {
            return EMPTY_PATH;
        }
        float currentBackgroundRadiusBottom = this.mAlwaysRoundBothCorners ? this.mOutlineRadius : getCurrentBackgroundRadiusBottom();
        float f = currentBackgroundRadiusTop + currentBackgroundRadiusBottom;
        float f2 = (float) i6;
        if (f > f2) {
            float f3 = f - f2;
            float f4 = this.mCurrentTopRoundness;
            float f5 = this.mCurrentBottomRoundness;
            currentBackgroundRadiusTop -= (f3 * f4) / (f4 + f5);
            currentBackgroundRadiusBottom -= (f3 * f5) / (f4 + f5);
        }
        getRoundedRectPath(i4, i3, i2, i, currentBackgroundRadiusTop, currentBackgroundRadiusBottom, this.mTmpPath);
        return this.mTmpPath;
    }

    public void getRoundedRectPath(int i, int i2, int i3, int i4, float f, float f2, Path path) {
        path.reset();
        float[] fArr = this.mTmpCornerRadii;
        fArr[0] = f;
        fArr[1] = f;
        fArr[2] = f;
        fArr[3] = f;
        fArr[4] = f2;
        fArr[5] = f2;
        fArr[6] = f2;
        fArr[7] = f2;
        path.addRoundRect((float) i, (float) i2, (float) i3, (float) i4, fArr, Path.Direction.CW);
    }

    public ExpandableOutlineView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        AnonymousClass1 r1 = new ViewOutlineProvider() { // from class: com.android.systemui.statusbar.notification.row.ExpandableOutlineView.1
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                if (ExpandableOutlineView.this.mCustomOutline || ExpandableOutlineView.this.mCurrentTopRoundness != 0.0f || ExpandableOutlineView.this.mCurrentBottomRoundness != 0.0f || ExpandableOutlineView.this.mAlwaysRoundBothCorners || ExpandableOutlineView.this.mTopAmountRounded) {
                    Path clipPath = ExpandableOutlineView.this.getClipPath(false);
                    if (clipPath != null) {
                        outline.setPath(clipPath);
                    }
                } else {
                    ExpandableOutlineView expandableOutlineView = ExpandableOutlineView.this;
                    int translation = expandableOutlineView.mShouldTranslateContents ? (int) expandableOutlineView.getTranslation() : 0;
                    int max = Math.max(translation, 0);
                    ExpandableOutlineView expandableOutlineView2 = ExpandableOutlineView.this;
                    int i = expandableOutlineView2.mClipTopAmount + expandableOutlineView2.mBackgroundTop;
                    outline.setRect(max, i, ExpandableOutlineView.this.getWidth() + Math.min(translation, 0), Math.max(ExpandableOutlineView.this.getActualHeight() - ExpandableOutlineView.this.mClipBottomAmount, i));
                }
                outline.setAlpha(ExpandableOutlineView.this.mOutlineAlpha);
            }
        };
        this.mProvider = r1;
        setOutlineProvider(r1);
        initDimens();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean drawChild(Canvas canvas, View view, long j) {
        Path path;
        canvas.save();
        if (!this.mTopAmountRounded || !topAmountNeedsClipping()) {
            path = null;
        } else {
            int i = (int) ((-this.mExtraWidthForClipping) / 2.0f);
            int i2 = (int) (((float) this.mClipTopAmount) - this.mDistanceToTopRoundness);
            getRoundedRectPath(i, i2, getWidth() + ((int) (this.mExtraWidthForClipping + ((float) i))), (int) Math.max((float) this.mMinimumHeightForClipping, Math.max((float) (getActualHeight() - this.mClipBottomAmount), ((float) i2) + this.mOutlineRadius)), this.mOutlineRadius, 0.0f, this.mClipPath);
            path = this.mClipPath;
        }
        boolean z = false;
        if (childNeedsClipping(view)) {
            Path customClipPath = getCustomClipPath(view);
            if (customClipPath == null) {
                customClipPath = getClipPath(false);
            }
            if (customClipPath != null) {
                if (path != null) {
                    customClipPath.op(path, Path.Op.INTERSECT);
                }
                canvas.clipPath(customClipPath);
                z = true;
                int i3 = getResources().getConfiguration().orientation;
                if (OpUtils.isREDVersion() && this.mIsHeadsUp && !this.mIsScrolledToTop && i3 == 2) {
                    canvas.drawPath(customClipPath, this.mStrokePaint);
                }
            }
        }
        if (!z && path != null) {
            canvas.clipPath(path);
        }
        boolean drawChild = super.drawChild(canvas, view, j);
        canvas.restore();
        return drawChild;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setExtraWidthForClipping(float f) {
        super.setExtraWidthForClipping(f);
        invalidate();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setMinimumHeightForClipping(int i) {
        super.setMinimumHeightForClipping(i);
        invalidate();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setDistanceToTopRoundness(float f) {
        super.setDistanceToTopRoundness(f);
        if (f != this.mDistanceToTopRoundness) {
            this.mTopAmountRounded = f >= 0.0f;
            this.mDistanceToTopRoundness = f;
            applyRoundness();
        }
    }

    public void setIsHeadsUp(boolean z) {
        boolean z2 = this.mIsHeadsUp;
        if (!z2 && z2 != z) {
            this.mIsScrolledToTop = false;
        }
        this.mIsHeadsUp = z;
    }

    public void onScrolledToTop() {
        this.mIsScrolledToTop = true;
    }

    /* access modifiers changed from: protected */
    public boolean isClippingNeeded() {
        return this.mAlwaysRoundBothCorners || this.mCustomOutline || getTranslation() != 0.0f || this.mShouldClipNotifToOutline;
    }

    private void initDimens() {
        Resources resources = getResources();
        this.mShouldTranslateContents = resources.getBoolean(C0003R$bool.config_translateNotificationContentsOnSwipe);
        this.mOutlineRadius = resources.getDimension(C0005R$dimen.notification_shadow_radius);
        boolean z = resources.getBoolean(C0003R$bool.config_clipNotificationsToOutline);
        this.mAlwaysRoundBothCorners = z;
        if (!z) {
            this.mOutlineRadius = (float) resources.getDimensionPixelSize(Utils.getThemeAttr(((FrameLayout) this).mContext, 16844145));
            Log.i("ExpandableOutlineView", "initDimens mOutlineRadius:" + this.mOutlineRadius);
        }
        this.mShouldClipNotifToOutline = ((FrameLayout) this).mContext.getResources().getBoolean(C0003R$bool.op_config_shouldClipNotifToOutline);
        setClipToOutline(this.mAlwaysRoundBothCorners);
        this.mStrokePaint.setAntiAlias(true);
        this.mStrokePaint.setStyle(Paint.Style.STROKE);
        this.mStrokePaint.setStrokeWidth(getResources().getDimension(C0005R$dimen.op_cb_notification_panel_stroke_width));
        this.mStrokePaint.setColor(((FrameLayout) this).mContext.getColor(C0004R$color.op_turquoise));
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean setTopRoundness(float f, boolean z) {
        if (this.mTopRoundness == f) {
            return false;
        }
        this.mTopRoundness = f;
        PropertyAnimator.setProperty(this, TOP_ROUNDNESS, f, ROUNDNESS_PROPERTIES, z);
        return true;
    }

    /* access modifiers changed from: protected */
    public void applyRoundness() {
        invalidateOutline();
        invalidate();
    }

    public float getCurrentBackgroundRadiusTop() {
        float f;
        float f2;
        if (!this.mTopAmountRounded) {
            if (this.mIsNotificationShelf) {
                int i = this.mBackgroundTop;
                float f3 = this.mOutlineRadius;
                if (((float) i) + f3 <= 0.0f) {
                    return 0.0f;
                }
                f2 = ((float) i) + f3;
                f = this.mCurrentTopRoundness;
            } else {
                f2 = this.mCurrentTopRoundness;
                f = this.mOutlineRadius;
            }
            return f2 * f;
        } else if (this.mIsSectionHeader) {
            return this.mOutlineRadius;
        } else {
            return 0.85f * (this.mOutlineRadius - this.mDistanceToTopRoundness);
        }
    }

    public float getCurrentTopRoundness() {
        return this.mCurrentTopRoundness;
    }

    public float getCurrentBottomRoundness() {
        return this.mCurrentBottomRoundness;
    }

    /* access modifiers changed from: protected */
    public float getCurrentBackgroundRadiusBottom() {
        return this.mCurrentBottomRoundness * this.mOutlineRadius;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean setBottomRoundness(float f, boolean z) {
        if (this.mBottomRoundness == f) {
            return false;
        }
        this.mBottomRoundness = f;
        PropertyAnimator.setProperty(this, BOTTOM_ROUNDNESS, f, ROUNDNESS_PROPERTIES, z);
        return true;
    }

    /* access modifiers changed from: protected */
    public void setBackgroundTop(int i) {
        if (this.mBackgroundTop != i) {
            this.mBackgroundTop = i;
            invalidateOutline();
            if (this.mIsNotificationShelf) {
                invalidate();
            }
        }
    }

    /* access modifiers changed from: private */
    public void setTopRoundnessInternal(float f) {
        this.mCurrentTopRoundness = f;
        applyRoundness();
    }

    /* access modifiers changed from: private */
    public void setBottomRoundnessInternal(float f) {
        this.mCurrentBottomRoundness = f;
        applyRoundness();
    }

    public void onDensityOrFontScaleChanged() {
        initDimens();
        applyRoundness();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setActualHeight(int i, boolean z) {
        int actualHeight = getActualHeight();
        super.setActualHeight(i, z);
        if (actualHeight != i) {
            applyRoundness();
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setClipTopAmount(int i) {
        int clipTopAmount = getClipTopAmount();
        super.setClipTopAmount(i);
        if (clipTopAmount != i) {
            applyRoundness();
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void setClipBottomAmount(int i) {
        int clipBottomAmount = getClipBottomAmount();
        super.setClipBottomAmount(i);
        if (clipBottomAmount != i) {
            applyRoundness();
        }
    }

    /* access modifiers changed from: protected */
    public void setOutlineAlpha(float f) {
        if (f != this.mOutlineAlpha) {
            this.mOutlineAlpha = f;
            applyRoundness();
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public float getOutlineAlpha() {
        return this.mOutlineAlpha;
    }

    /* access modifiers changed from: protected */
    public void setOutlineRect(RectF rectF) {
        if (rectF != null) {
            setOutlineRect(rectF.left, rectF.top, rectF.right, rectF.bottom);
            return;
        }
        this.mCustomOutline = false;
        applyRoundness();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public int getOutlineTranslation() {
        return this.mCustomOutline ? this.mOutlineRect.left : (int) getTranslation();
    }

    public void updateOutline() {
        if (!this.mCustomOutline) {
            setOutlineProvider(needsOutline() ? this.mProvider : null);
        }
    }

    /* access modifiers changed from: protected */
    public boolean needsOutline() {
        if (isChildInGroup()) {
            return isGroupExpanded() && !isGroupExpansionChanging();
        }
        if (isSummaryWithChildren()) {
            return !isGroupExpanded() || isGroupExpansionChanging();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void setOutlineRect(float f, float f2, float f3, float f4) {
        this.mCustomOutline = true;
        this.mOutlineRect.set((int) f, (int) f2, (int) f3, (int) f4);
        Rect rect = this.mOutlineRect;
        rect.bottom = (int) Math.max(f2, (float) rect.bottom);
        Rect rect2 = this.mOutlineRect;
        rect2.right = (int) Math.max(f, (float) rect2.right);
        applyRoundness();
    }

    public void setIsSectionHeader(boolean z) {
        this.mIsSectionHeader = z;
    }

    public void setIsNotificationShelf(boolean z) {
        this.mIsNotificationShelf = z;
    }
}
