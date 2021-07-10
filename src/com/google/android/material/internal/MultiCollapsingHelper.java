package com.google.android.material.internal;

import android.animation.TimeInterpolator;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.animation.AnimatorUtils;
import androidx.core.math.MathUtils;
import androidx.core.text.TextDirectionHeuristicsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import com.google.android.material.R$color;
import com.google.android.material.R$dimen;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.appbar.CollapsingAppbarLayout;
import com.google.android.material.resources.TextAppearance;
public final class MultiCollapsingHelper {
    private static final Paint DEBUG_DRAW_PAINT = null;
    private static final boolean USE_SCALING_TEXTURE = (Build.VERSION.SDK_INT < 18);
    private ColorStateList collapsedSubtitleColor;
    private ColorStateList collapsedSubtitleShadowColor;
    private float collapsedSubtitleShadowDx;
    private float collapsedSubtitleShadowDy;
    private float collapsedSubtitleShadowRadius;
    private float collapsedSubtitleSize = 15.0f;
    private Typeface collapsedSubtitleTypeface;
    private float collapsedSubtitleX;
    private float collapsedSubtitleY;
    private float currentSubtitleSize;
    private Typeface currentSubtitleTypeface;
    private float currentSubtitleX;
    private float currentSubtitleY;
    private ColorStateList expandedSubtitleColor;
    private ColorStateList expandedSubtitleShadowColor;
    private float expandedSubtitleShadowDx;
    private float expandedSubtitleShadowDy;
    private float expandedSubtitleShadowRadius;
    private float expandedSubtitleSize = 15.0f;
    private Bitmap expandedSubtitleTexture;
    private Typeface expandedSubtitleTypeface;
    private float expandedSubtitleX;
    private float expandedSubtitleY;
    private float lineSpacingExtra = 0.0f;
    private float lineSpacingMultiplier = 1.0f;
    private int mAppbarMarginBottom;
    private int mAppbarMarginLeft;
    private int mAppbarMarginRight;
    private boolean mBoundsChanged;
    private final Rect mCollapsedBounds;
    private float mCollapsedDrawX;
    private float mCollapsedDrawY;
    private ColorStateList mCollapsedTextColor;
    private int mCollapsedTextGravity = 16;
    private float mCollapsedTextSize;
    private ColorStateList mCollapsedTitleShadowColor;
    private float mCollapsedTitleShadowDx;
    private float mCollapsedTitleShadowDy;
    private float mCollapsedTitleShadowRadius;
    private Bitmap mCollapsedTitleTexture;
    private Typeface mCollapsedTypeface;
    private Bitmap mCrossSectionTitleTexture;
    private final RectF mCurrentBounds;
    private float mCurrentDrawX;
    private float mCurrentDrawY;
    private float mCurrentTitleSize;
    private Typeface mCurrentTypeface;
    private Bitmap mDrawBitmap;
    private boolean mDrawLine = true;
    private boolean mDrawTitle;
    private final Rect mExpandedBounds;
    private float mExpandedDrawX;
    private float mExpandedDrawY;
    private float mExpandedFraction;
    private ColorStateList mExpandedTextColor;
    private int mExpandedTextGravity = 16;
    private float mExpandedTextSize;
    private ColorStateList mExpandedTitleShadowColor;
    private float mExpandedTitleShadowDx;
    private float mExpandedTitleShadowDy;
    private float mExpandedTitleShadowRadius;
    private Bitmap mExpandedTitleTexture;
    private Typeface mExpandedTypeface;
    private ImageView mImageView;
    private int mImageViewSize;
    private boolean mInsetSubtitleImage = false;
    private boolean mIsDrawLine;
    private boolean mIsRtl;
    private int mMenuMargin;
    private TimeInterpolator mPositionInterpolator;
    private float mScale;
    private boolean mShowDrawLine = false;
    private int[] mState;
    private TextView mSubTitleView;
    private int mSubtitleAppearance;
    private float mSyncBottomY;
    private boolean mSyncCollapsNull;
    private float mSyncLeftX;
    private CharSequence mSyncText;
    private boolean mSyncTextSecondLine;
    private TextView mSyncTextView;
    private float mSyncTopY;
    private StaticLayout mTextLayout;
    private final TextPaint mTextPaint;
    private TimeInterpolator mTextSizeInterpolator;
    private CharSequence mTextToDraw;
    private CharSequence mTextToDrawCollapsed;
    private Paint mTexturePaint;
    private CharSequence mTitle;
    private int mTitleAppearance;
    private TextView mTitleView;
    private boolean mUseTexture;
    private final CollapsingAppbarLayout mView;
    private int maxLines = 2;
    private CharSequence subtitle;
    private final TextPaint subtitlePaint;
    private float subtitleScale;
    private Paint subtitleTexturePaint;
    private CharSequence subtitleToDraw;
    private ColorStateList syncColorList;
    private final TextPaint syncTextPaint;

    static {
        Paint paint = null;
        if (0 != 0) {
            paint.setAntiAlias(true);
            DEBUG_DRAW_PAINT.setColor(-65281);
        }
    }

    public MultiCollapsingHelper(CollapsingAppbarLayout collapsingAppbarLayout) {
        this.mView = collapsingAppbarLayout;
        this.mTextPaint = new TextPaint(129);
        this.subtitlePaint = new TextPaint(129);
        this.syncTextPaint = new TextPaint(129);
        this.syncColorList = this.mView.getResources().getColorStateList(R$color.oneplus_accent_text_color);
        this.mImageViewSize = this.mView.getResources().getDimensionPixelOffset(R$dimen.op_control_icon_size_indicator);
        this.mCollapsedBounds = new Rect();
        this.mExpandedBounds = new Rect();
        this.mCurrentBounds = new RectF();
    }

    public void setTextSizeInterpolator(TimeInterpolator timeInterpolator) {
        this.mTextSizeInterpolator = timeInterpolator;
        recalculate();
    }

    public void setCollapsedTitleColor(ColorStateList colorStateList) {
        if (this.mCollapsedTextColor != colorStateList) {
            this.mCollapsedTextColor = colorStateList;
            recalculate();
        }
    }

    public void setExpandedTitleColor(ColorStateList colorStateList) {
        if (this.mExpandedTextColor != colorStateList) {
            this.mExpandedTextColor = colorStateList;
            recalculate();
        }
    }

    public void setSyncTextColor(ColorStateList colorStateList) {
        if (this.syncColorList != colorStateList) {
            this.syncColorList = colorStateList;
        }
    }

    public void setExpandedBounds(int i, int i2, int i3, int i4) {
        if (!rectEquals(this.mExpandedBounds, i, i2, i3, i4)) {
            this.mExpandedBounds.set(i, i2, i3, i4);
            this.mBoundsChanged = true;
            onBoundsChanged();
        }
    }

    public void setCollapsedBounds(int i, int i2, int i3, int i4) {
        if (!rectEquals(this.mCollapsedBounds, i, i2, i3, i4)) {
            this.mCollapsedBounds.set(i, i2, i3, i4);
            this.mBoundsChanged = true;
            TextView textView = this.mTitleView;
            if (textView != null) {
                textView.setMaxWidth(((i3 - i) - this.mImageViewSize) + ((this.mExpandedFraction >= 0.5f || "net.oneplus.weather".equals(this.mView.getContext().getPackageName())) ? 0 : this.mMenuMargin));
            }
            TextView textView2 = this.mSubTitleView;
            if (textView2 != null) {
                textView2.setMaxWidth((i3 - i) - this.mImageViewSize);
            }
            onBoundsChanged();
        }
    }

    public void onBoundsChanged() {
        this.mDrawTitle = this.mCollapsedBounds.width() > 0 && this.mCollapsedBounds.height() > 0 && this.mExpandedBounds.width() > 0 && this.mExpandedBounds.height() > 0;
    }

    public void setExpandedTextGravity(int i) {
        if (this.mExpandedTextGravity != i) {
            this.mExpandedTextGravity = i;
            recalculate();
        }
    }

    public void setCollapsedTextGravity(int i) {
        if (this.mCollapsedTextGravity != i) {
            this.mCollapsedTextGravity = i;
            recalculate();
        }
    }

    public void setCollapsedTitleAppearance(int i) {
        TextAppearance textAppearance = new TextAppearance(this.mView.getContext(), i);
        ColorStateList colorStateList = textAppearance.textColor;
        if (colorStateList != null) {
            this.mCollapsedTextColor = colorStateList;
        }
        float f = textAppearance.textSize;
        if (f != 0.0f) {
            this.mCollapsedTextSize = f;
        }
        ColorStateList colorStateList2 = textAppearance.shadowColor;
        if (colorStateList2 != null) {
            this.mCollapsedTitleShadowColor = colorStateList2;
        }
        this.mCollapsedTitleShadowDx = textAppearance.shadowDx;
        this.mCollapsedTitleShadowDy = textAppearance.shadowDy;
        this.mCollapsedTitleShadowRadius = textAppearance.shadowRadius;
        if (Build.VERSION.SDK_INT >= 16) {
            this.mCollapsedTypeface = readFontFamilyTypeface(i);
        }
        recalculate();
    }

    public void setExpandedTitleAppearance(int i) {
        TextAppearance textAppearance = new TextAppearance(this.mView.getContext(), i);
        this.mTitleAppearance = i;
        ColorStateList colorStateList = textAppearance.textColor;
        if (colorStateList != null) {
            this.mExpandedTextColor = colorStateList;
        }
        float f = textAppearance.textSize;
        if (f != 0.0f) {
            this.mExpandedTextSize = f;
        }
        ColorStateList colorStateList2 = textAppearance.shadowColor;
        if (colorStateList2 != null) {
            this.mExpandedTitleShadowColor = colorStateList2;
        }
        this.mExpandedTitleShadowDx = textAppearance.shadowDx;
        this.mExpandedTitleShadowDy = textAppearance.shadowDy;
        this.mExpandedTitleShadowRadius = textAppearance.shadowRadius;
        if (Build.VERSION.SDK_INT >= 16) {
            this.mExpandedTypeface = readFontFamilyTypeface(i);
        }
        recalculate();
    }

    private Typeface readFontFamilyTypeface(int i) {
        TypedArray obtainStyledAttributes = this.mView.getContext().obtainStyledAttributes(i, new int[]{16843692});
        try {
            String string = obtainStyledAttributes.getString(0);
            if (string != null) {
                return Typeface.create(string, 0);
            }
            obtainStyledAttributes.recycle();
            return null;
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    public void setExpansionFraction(float f) {
        float clamp = MathUtils.clamp(f, 0.0f, 1.0f);
        if (clamp != this.mExpandedFraction) {
            this.mExpandedFraction = clamp;
            calculateCurrentOffsets();
        }
    }

    public final boolean setState(int[] iArr) {
        this.mState = iArr;
        if (!isStateful()) {
            return false;
        }
        recalculate();
        return true;
    }

    /* access modifiers changed from: package-private */
    public final boolean isStateful() {
        ColorStateList colorStateList;
        ColorStateList colorStateList2 = this.mCollapsedTextColor;
        return (colorStateList2 != null && colorStateList2.isStateful()) || ((colorStateList = this.mExpandedTextColor) != null && colorStateList.isStateful());
    }

    public float getExpansionFraction() {
        return this.mExpandedFraction;
    }

    public void calculateCurrentOffsets() {
        calculateOffsets(this.mExpandedFraction);
    }

    private void calculateOffsets(float f) {
        interpolateBounds(f);
        this.mCurrentDrawX = lerp(this.mExpandedDrawX, this.mCollapsedDrawX, f, this.mPositionInterpolator);
        this.mCurrentDrawY = lerp(this.mExpandedDrawY, this.mCollapsedDrawY, f, AnimationUtils.LINEAR_INTERPOLATOR);
        this.currentSubtitleX = lerp(this.expandedSubtitleX, this.collapsedSubtitleX, f, this.mPositionInterpolator);
        this.currentSubtitleY = lerp(this.expandedSubtitleY, this.collapsedSubtitleY, f, this.mPositionInterpolator);
        setInterpolatedTextSize(lerp(this.mExpandedTextSize, this.mCollapsedTextSize, f, this.mTextSizeInterpolator));
        setInterpolatedSubtitleSize(lerp(this.expandedSubtitleSize, this.collapsedSubtitleSize, f, this.mTextSizeInterpolator));
        setCollapsedTextBlend(1.0f - lerp(0.0f, 1.0f, 1.0f - f, AnimatorUtils.op_control_interpolator_linear_out_slow_in));
        setExpandedTextBlend(lerp(1.0f, 0.0f, f, AnimatorUtils.op_control_interpolator_linear_out_slow_in));
        if (this.mCollapsedTextColor != this.mExpandedTextColor) {
            this.mTextPaint.setColor(blendColors(getCurrentExpandedTextColor(), getCurrentCollapsedTextColor(), f));
        } else {
            this.mTextPaint.setColor(getCurrentCollapsedTextColor());
        }
        this.mTextPaint.setShadowLayer(lerp(this.mExpandedTitleShadowRadius, this.mCollapsedTitleShadowRadius, f, null), lerp(this.mExpandedTitleShadowDx, this.mCollapsedTitleShadowDx, f, null), lerp(this.mExpandedTitleShadowDy, this.mCollapsedTitleShadowDy, f, null), blendColors(getCurrentColor(this.mExpandedTitleShadowColor), getCurrentColor(this.mCollapsedTitleShadowColor), f));
        if (this.collapsedSubtitleColor != this.expandedSubtitleColor) {
            this.subtitlePaint.setColor(blendColors(getCurrentExpandedSubtitleColor(), getCurrentCollapsedSubtitleColor(), f));
            this.syncTextPaint.setColor(getCurrentSyncTextColor());
        } else {
            this.subtitlePaint.setColor(getCurrentCollapsedSubtitleColor());
        }
        this.subtitlePaint.setShadowLayer(lerp(this.expandedSubtitleShadowRadius, this.collapsedSubtitleShadowRadius, f, null), lerp(this.expandedSubtitleShadowDx, this.collapsedSubtitleShadowDx, f, null), lerp(this.expandedSubtitleShadowDy, this.collapsedSubtitleShadowDy, f, null), blendColors(getCurrentColor(this.expandedSubtitleShadowColor), getCurrentColor(this.collapsedSubtitleShadowColor), f));
        ViewCompat.postInvalidateOnAnimation(this.mView);
    }

    private void setInterpolatedSubtitleSize(float f) {
        calculateUsingSubtitleSize(f);
        boolean z = USE_SCALING_TEXTURE && this.subtitleScale != 1.0f;
        this.mUseTexture = z;
        if (z) {
            ensureExpandedSubtitleTexture();
        }
        ViewCompat.postInvalidateOnAnimation(this.mView);
    }

    private void ensureExpandedSubtitleTexture() {
        if (this.expandedSubtitleTexture == null && !this.mExpandedBounds.isEmpty() && !TextUtils.isEmpty(this.subtitleToDraw)) {
            calculateOffsets(0.0f);
            TextPaint textPaint = this.subtitlePaint;
            CharSequence charSequence = this.subtitleToDraw;
            int round = Math.round(textPaint.measureText(charSequence, 0, charSequence.length()));
            int round2 = Math.round(this.subtitlePaint.descent() - this.subtitlePaint.ascent());
            if (round > 0 && round2 >= 0) {
                this.expandedSubtitleTexture = Bitmap.createBitmap(round, round2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(this.expandedSubtitleTexture);
                CharSequence charSequence2 = this.subtitleToDraw;
                canvas.drawText(charSequence2, 0, charSequence2.length(), 0.0f, ((float) round2) - this.subtitlePaint.descent(), this.subtitlePaint);
                if (this.subtitleTexturePaint == null) {
                    this.subtitleTexturePaint = new Paint(3);
                }
            }
        }
    }

    private int getCurrentExpandedSubtitleColor() {
        return getCurrentColor(this.expandedSubtitleColor);
    }

    private int getCurrentSyncTextColor() {
        return getCurrentColor(this.syncColorList);
    }

    public int getCurrentCollapsedSubtitleColor() {
        return getCurrentColor(this.collapsedSubtitleColor);
    }

    public void setCollapsedSubtitleColor(ColorStateList colorStateList) {
        if (this.collapsedSubtitleColor != colorStateList) {
            this.collapsedSubtitleColor = colorStateList;
            recalculate();
        }
    }

    public void setExpandedSubtitleColor(ColorStateList colorStateList) {
        if (this.expandedSubtitleColor != colorStateList) {
            this.expandedSubtitleColor = colorStateList;
            recalculate();
        }
    }

    private void calculateUsingSubtitleSize(float f) {
        float f2;
        int i;
        boolean z;
        if (this.subtitle != null) {
            float width = (float) this.mCollapsedBounds.width();
            float width2 = (float) this.mExpandedBounds.width();
            boolean z2 = true;
            int i2 = 0;
            if (isClose(f, this.collapsedSubtitleSize)) {
                f2 = this.collapsedSubtitleSize;
                this.subtitleScale = 1.0f;
                Typeface typeface = this.currentSubtitleTypeface;
                Typeface typeface2 = this.collapsedSubtitleTypeface;
                if (typeface != typeface2) {
                    this.currentSubtitleTypeface = typeface2;
                }
            } else {
                float f3 = this.expandedSubtitleSize;
                Typeface typeface3 = this.currentSubtitleTypeface;
                Typeface typeface4 = this.expandedSubtitleTypeface;
                if (typeface3 != typeface4) {
                    this.currentSubtitleTypeface = typeface4;
                    z = true;
                } else {
                    z = false;
                }
                if (isClose(f, this.expandedSubtitleSize)) {
                    this.subtitleScale = 1.0f;
                } else {
                    this.subtitleScale = f / this.expandedSubtitleSize;
                }
                float f4 = this.collapsedSubtitleSize / this.expandedSubtitleSize;
                width = width2 * f4 > width ? Math.min(width / f4, width2) : width2;
                f2 = f3;
            }
            if (width > 0.0f) {
                if (this.currentSubtitleSize == f2) {
                    boolean z3 = this.mBoundsChanged;
                }
                this.currentSubtitleSize = f2;
                this.mBoundsChanged = false;
            }
            this.subtitlePaint.setTextSize(this.currentSubtitleSize);
            this.subtitlePaint.setTypeface(this.currentSubtitleTypeface);
            this.subtitlePaint.setLinearText(this.subtitleScale != 1.0f);
            this.syncTextPaint.setTextSize(this.currentSubtitleSize);
            this.syncTextPaint.setColor(this.mView.getResources().getColor(R$color.oneplus_accent_color));
            this.syncTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
            TextPaint textPaint = this.syncTextPaint;
            if (this.subtitleScale == 1.0f) {
                z2 = false;
            }
            textPaint.setLinearText(z2);
            CharSequence charSequence = this.subtitle;
            TextPaint textPaint2 = this.subtitlePaint;
            if (this.mExpandedFraction <= 0.4f || (i = this.mMenuMargin) <= 0) {
                i = 0;
            }
            float f5 = width - ((float) i);
            if (this.mInsetSubtitleImage) {
                i2 = this.mImageViewSize;
            }
            CharSequence ellipsize = TextUtils.ellipsize(charSequence, textPaint2, f5 - ((float) i2), TextUtils.TruncateAt.END);
            if (!TextUtils.equals(ellipsize, this.subtitleToDraw)) {
                this.subtitleToDraw = ellipsize;
                this.mIsRtl = calculateIsRtl(ellipsize);
            }
        }
    }

    private int getCurrentColor(ColorStateList colorStateList) {
        if (colorStateList == null) {
            return 0;
        }
        int[] iArr = this.mState;
        if (iArr != null) {
            return colorStateList.getColorForState(iArr, 0);
        }
        return colorStateList.getDefaultColor();
    }

    public int getCurrentExpandedTextColor() {
        int[] iArr = this.mState;
        if (iArr != null) {
            return this.mExpandedTextColor.getColorForState(iArr, 0);
        }
        return this.mExpandedTextColor.getDefaultColor();
    }

    private int getCurrentCollapsedTextColor() {
        int[] iArr = this.mState;
        if (iArr != null) {
            return this.mCollapsedTextColor.getColorForState(iArr, 0);
        }
        return this.mCollapsedTextColor.getDefaultColor();
    }

    private void calculateBaseOffsets() {
        float f;
        float f2 = this.mCurrentTitleSize;
        float f3 = this.currentSubtitleSize;
        boolean isEmpty = TextUtils.isEmpty(this.subtitle);
        calculateUsingTextSize(this.mCollapsedTextSize);
        calculateUsingSubtitleSize(this.collapsedSubtitleSize);
        CharSequence charSequence = this.mTextToDraw;
        this.mTextToDrawCollapsed = charSequence;
        float measureText = charSequence != null ? this.mTextPaint.measureText(charSequence, 0, charSequence.length()) : 0.0f;
        CharSequence charSequence2 = this.subtitleToDraw;
        float measureText2 = charSequence2 != null ? this.subtitlePaint.measureText(charSequence2, 0, charSequence2.length()) : 0.0f;
        int absoluteGravity = GravityCompat.getAbsoluteGravity(this.mCollapsedTextGravity, (!this.mIsRtl || "net.oneplus.weather".equals(this.mView.getContext().getPackageName())) ? 0 : 1);
        StaticLayout staticLayout = this.mTextLayout;
        float height = staticLayout != null ? (float) staticLayout.getHeight() : 0.0f;
        float descent = this.subtitlePaint.descent() - this.subtitlePaint.ascent();
        if (isEmpty) {
            resizeTitleOnlyCollapsedY(absoluteGravity, height);
        } else {
            float height2 = (((float) this.mCollapsedBounds.height()) - (descent + height)) / 3.0f;
            float f4 = (float) this.mCollapsedBounds.top;
            if (height2 > ((float) this.mView.getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space2)) + 2.2f) {
                f = height2;
            } else {
                f = (float) this.mView.getResources().getDimensionPixelOffset(R$dimen.op_app_bar_margin_top);
            }
            this.mCollapsedDrawY = ((f4 + f) - this.mTextPaint.ascent()) + this.subtitlePaint.ascent();
            this.collapsedSubtitleY = (((((float) this.mCollapsedBounds.top) + (height2 * 2.0f)) + height) - this.subtitlePaint.ascent()) - ((float) this.mView.getResources().getDimensionPixelOffset(R$dimen.op_app_bar_collapsed_top_subtitle_margin));
            if (this.mExpandedBounds.top < 100) {
                this.mCollapsedDrawY += this.mTextPaint.ascent() / 4.0f;
            }
            if (this.mSyncCollapsNull) {
                resizeTitleOnlyCollapsedY(absoluteGravity, height);
            }
        }
        int dimensionPixelOffset = this.mView.getContext().getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space4);
        int i = absoluteGravity & 8388615;
        if (i == 1) {
            this.mCollapsedDrawX = ((float) this.mCollapsedBounds.centerX()) - (measureText / 2.0f);
            this.collapsedSubtitleX = (((float) this.mCollapsedBounds.centerX()) - (measureText2 / 2.0f)) + ((float) (this.mInsetSubtitleImage ? dimensionPixelOffset : 0));
        } else if (i != 5) {
            int i2 = this.mCollapsedBounds.left;
            this.mCollapsedDrawX = (float) i2;
            this.collapsedSubtitleX = (float) (i2 + (this.mInsetSubtitleImage ? dimensionPixelOffset : 0));
        } else {
            int i3 = this.mCollapsedBounds.right;
            this.mCollapsedDrawX = ((float) i3) - measureText;
            this.collapsedSubtitleX = (((float) i3) - measureText2) + ((float) (this.mInsetSubtitleImage ? dimensionPixelOffset : 0));
        }
        calculateUsingTextSize(this.mExpandedTextSize);
        calculateUsingSubtitleSize(this.expandedSubtitleSize);
        StaticLayout staticLayout2 = this.mTextLayout;
        float lineWidth = staticLayout2 != null ? staticLayout2.getLineWidth(0) : 0.0f;
        CharSequence charSequence3 = this.subtitleToDraw;
        float measureText3 = charSequence3 != null ? this.subtitlePaint.measureText(charSequence3, 0, charSequence3.length()) : 0.0f;
        StaticLayout staticLayout3 = this.mTextLayout;
        if (staticLayout3 != null) {
            staticLayout3.getLineLeft(0);
        }
        int absoluteGravity2 = GravityCompat.getAbsoluteGravity(this.mExpandedTextGravity, (!this.mIsRtl || "net.oneplus.weather".equals(this.mView.getContext().getPackageName())) ? 0 : 1);
        StaticLayout staticLayout4 = this.mTextLayout;
        float height3 = staticLayout4 != null ? (float) staticLayout4.getHeight() : 0.0f;
        float descent2 = this.subtitlePaint.descent() - this.subtitlePaint.ascent();
        float f5 = height3 / 2.0f;
        float descent3 = f5 - this.mTextPaint.descent();
        if (isEmpty) {
            int i4 = absoluteGravity2 & 112;
            if (i4 == 48) {
                this.mExpandedDrawY = (float) this.mExpandedBounds.top;
            } else if (i4 != 80) {
                this.mExpandedDrawY = ((float) this.mExpandedBounds.centerY()) - f5;
            } else {
                this.mExpandedDrawY = ((float) this.mExpandedBounds.bottom) - height3;
            }
        } else {
            int i5 = absoluteGravity2 & 112;
            if (i5 == 48) {
                float f6 = (float) this.mExpandedBounds.top;
                this.mExpandedDrawY = f6;
                this.expandedSubtitleY = f6 + descent2 + height3;
            } else if (i5 != 80) {
                float centerY = ((float) this.mExpandedBounds.centerY()) - f5;
                this.mExpandedDrawY = centerY;
                this.expandedSubtitleY = centerY + descent2 + descent3;
            } else {
                int i6 = this.mExpandedBounds.bottom;
                this.mExpandedDrawY = (((float) i6) - descent2) - height3;
                this.expandedSubtitleY = ((float) i6) - height3;
            }
        }
        int i7 = absoluteGravity2 & 8388615;
        if (i7 == 1) {
            this.mExpandedDrawX = ((float) this.mExpandedBounds.centerX()) - (lineWidth / 2.0f);
            this.expandedSubtitleX = (((float) this.mExpandedBounds.centerX()) - (measureText3 / 2.0f)) + ((float) (this.mInsetSubtitleImage ? dimensionPixelOffset : 0));
        } else if (i7 != 5) {
            int i8 = this.mExpandedBounds.left;
            this.mExpandedDrawX = (float) i8;
            this.expandedSubtitleX = (float) (i8 + (this.mInsetSubtitleImage ? dimensionPixelOffset : 0));
        } else {
            int i9 = this.mExpandedBounds.right;
            this.mExpandedDrawX = ((float) i9) - lineWidth;
            this.expandedSubtitleX = (((float) i9) - measureText3) + ((float) (this.mInsetSubtitleImage ? dimensionPixelOffset : 0));
        }
        clearTexture();
        setInterpolatedTextSize(f2);
        setInterpolatedSubtitleSize(f3);
    }

    private void resizeTitleOnlyCollapsedY(int i, float f) {
        int i2 = i & 112;
        if (i2 == 48) {
            this.mCollapsedDrawY = (float) this.mCollapsedBounds.top;
        } else if (i2 != 80) {
            this.mCollapsedDrawY = ((float) this.mCollapsedBounds.centerY()) - ((f / 2.0f) + ViewUtils.dpToPx(this.mView.getContext(), 3));
        } else {
            this.mCollapsedDrawY = ((float) this.mCollapsedBounds.bottom) - f;
        }
    }

    private void interpolateBounds(float f) {
        this.mCurrentBounds.left = lerp((float) this.mExpandedBounds.left, (float) this.mCollapsedBounds.left, f, this.mPositionInterpolator);
        this.mCurrentBounds.top = lerp(this.mExpandedDrawY, this.mCollapsedDrawY, f, this.mPositionInterpolator);
        this.mCurrentBounds.right = lerp((float) this.mExpandedBounds.right, (float) this.mCollapsedBounds.right, f, this.mPositionInterpolator);
        this.mCurrentBounds.bottom = lerp((float) this.mExpandedBounds.bottom, (float) this.mCollapsedBounds.bottom, f, this.mPositionInterpolator);
    }

    public void draw(Canvas canvas) {
        float f;
        int save = canvas.save();
        if (this.mTextToDraw != null && this.mDrawTitle) {
            float f2 = this.currentSubtitleX;
            float f3 = this.currentSubtitleY;
            Paint.FontMetricsInt fontMetricsInt = this.subtitlePaint.getFontMetricsInt();
            int i = fontMetricsInt.top;
            int i2 = fontMetricsInt.ascent;
            int i3 = fontMetricsInt.bottom;
            int i4 = fontMetricsInt.descent;
            this.subtitlePaint.ascent();
            this.mView.getResources().getDimensionPixelOffset(R$dimen.op_control_margin_list_top2);
            boolean z = this.mUseTexture && this.mExpandedTitleTexture != null;
            this.mTextPaint.setTextSize(this.mCurrentTitleSize);
            if (z) {
                f = 0.0f;
            } else {
                this.mTextPaint.ascent();
                f = this.subtitlePaint.ascent() * this.subtitleScale;
            }
            if (z) {
                f3 += f;
            }
            drawLine(canvas);
            int save2 = canvas.save();
            if (!TextUtils.isEmpty(this.subtitle)) {
                float f4 = this.subtitleScale;
                if (f4 != 1.0f) {
                    canvas.scale(f4, f4, f2, f3);
                }
                canvas.restoreToCount(save2);
            }
            if (updateTitlePosition()) {
                return;
            }
        }
        canvas.restoreToCount(save);
    }

    public void drawLine(Canvas canvas) {
        int save = canvas.save();
        if ((this.mExpandedFraction != 1.0f || !this.mDrawLine) && !this.mShowDrawLine) {
            this.mIsDrawLine = false;
            return;
        }
        this.subtitlePaint.setStrokeWidth((float) this.mView.getResources().getDimensionPixelSize(R$dimen.op_control_divider_height_standard));
        this.subtitlePaint.setColor(this.mView.getResources().getColor(R$color.op_control_divider_color_default));
        canvas.drawLine((float) this.mAppbarMarginLeft, (float) (this.mView.getMeasuredHeight() - this.mAppbarMarginBottom), (float) ((this.mView.getMeasuredWidth() - this.mAppbarMarginLeft) - this.mAppbarMarginRight), (float) (this.mView.getMeasuredHeight() - this.mAppbarMarginBottom), this.subtitlePaint);
        this.subtitlePaint.setColor(getCurrentCollapsedSubtitleColor());
        canvas.restoreToCount(save);
        this.mIsDrawLine = true;
    }

    private boolean updateTitlePosition() {
        float f;
        float f2;
        float f3;
        float f4;
        float f5;
        boolean z = ViewCompat.getLayoutDirection(this.mView) == 1;
        if (this.mTitleView != null) {
            if (this.mExpandedFraction >= 0.5f || "net.oneplus.weather".equals(this.mView.getContext().getPackageName())) {
                TextView textView = this.mTitleView;
                Rect rect = this.mCollapsedBounds;
                textView.setMaxWidth((rect.right - rect.left) - this.mImageViewSize);
            } else {
                TextView textView2 = this.mTitleView;
                Rect rect2 = this.mCollapsedBounds;
                textView2.setMaxWidth(((rect2.right - rect2.left) - this.mImageViewSize) + this.mMenuMargin);
            }
            if (TextUtils.isEmpty(this.mTitleView.getText())) {
                this.mTitleView.setText(this.mTextToDraw);
            }
            if (z) {
                float measureText = this.mTextPaint.measureText(String.valueOf(this.mTextToDraw));
                if (measureText > ((float) this.mView.getResources().getDisplayMetrics().widthPixels)) {
                    this.mTitleView.setMaxWidth(this.mView.getResources().getDisplayMetrics().widthPixels - (this.mCollapsedBounds.left * 2));
                    this.mTitleView.setTranslationX((float) (-this.mExpandedBounds.left));
                } else if (measureText > ((float) (this.mView.getResources().getDisplayMetrics().widthPixels * 2))) {
                    this.mTitleView.setTranslationX(0.0f);
                } else {
                    this.mTitleView.setTranslationX((float) (-this.mExpandedBounds.left));
                }
            } else {
                this.mTitleView.setTranslationX(this.mCollapsedDrawX);
            }
            if (this.mExpandedFraction == 1.0f) {
                this.mTitleView.setMaxLines(1);
            } else {
                this.mTitleView.setMaxLines(2);
            }
            TextView textView3 = this.mTitleView;
            float f6 = this.mExpandedTextSize;
            textView3.setTextSize(0, f6 - ((f6 - this.mCollapsedTextSize) * this.mExpandedFraction));
            this.mTextPaint.setTextSize(lerp(this.mExpandedTextSize, this.mCollapsedTextSize, this.mExpandedFraction, this.mTextSizeInterpolator));
            if (this.mSyncCollapsNull) {
                this.mTitleView.setTranslationY(((float) this.mExpandedBounds.top) + (((float) this.mView.getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space2)) * this.mExpandedFraction));
            } else {
                TextView textView4 = this.mTitleView;
                float f7 = (float) this.mExpandedBounds.top;
                if (TextUtils.isEmpty(this.subtitle)) {
                    f5 = (float) this.mView.getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space2);
                    f4 = this.mExpandedFraction;
                } else {
                    f5 = (float) (-this.mView.getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space1));
                    f4 = this.mExpandedFraction;
                }
                textView4.setTranslationY(f7 + (f5 * f4));
            }
        }
        TextView textView5 = this.mSubTitleView;
        if (!(textView5 == null || this.mTitleView == null)) {
            if (TextUtils.isEmpty(textView5.getText())) {
                this.mSubTitleView.setText(this.subtitle);
            }
            TextView textView6 = this.mSubTitleView;
            if (z) {
                f = (float) ((-this.mExpandedBounds.left) - (this.mInsetSubtitleImage ? this.mImageViewSize + 16 : 0));
            } else {
                f = this.mCollapsedDrawX + ((float) (this.mInsetSubtitleImage ? this.mImageViewSize + 16 : 0));
            }
            textView6.setTranslationX(f);
            if (this.mSyncTextView != null) {
                Rect rect3 = new Rect();
                if (TextUtils.isEmpty(this.mSyncText)) {
                    this.mSyncTextView.setText((CharSequence) null);
                    return true;
                }
                TextPaint textPaint = this.syncTextPaint;
                CharSequence charSequence = this.mSyncText;
                textPaint.getTextBounds(charSequence, 0, charSequence.length(), rect3);
                float measureText2 = this.syncTextPaint.measureText(String.valueOf(this.mSyncText));
                float measureText3 = (ViewCompat.getLayoutDirection(this.mView) == 1 ? (float) (-this.mExpandedBounds.left) : this.mCollapsedDrawX) + ((float) (this.mInsetSubtitleImage ? this.mImageViewSize + 16 : 0)) + this.subtitlePaint.measureText(String.valueOf(this.subtitleToDraw)) + 24.0f;
                if (z) {
                    f2 = Math.max((((((float) this.mExpandedBounds.right) - this.subtitlePaint.measureText(String.valueOf(this.subtitleToDraw))) - measureText2) - 48.0f) - ((float) (this.mInsetSubtitleImage ? this.mImageViewSize + 16 : 0)), 0.0f);
                } else {
                    f2 = measureText3;
                }
                this.mSyncLeftX = f2;
                float f8 = this.expandedSubtitleY;
                int i = this.mExpandedBounds.top;
                float abs = (((float) i) + f8) - (Math.abs((f8 + ((float) i)) - 330.0f) * this.mExpandedFraction);
                this.mSyncTopY = abs;
                this.mSyncBottomY = abs + ((float) rect3.height());
                if (measureText3 + measureText2 < ((float) this.mView.getResources().getDisplayMetrics().widthPixels)) {
                    if (TextUtils.isEmpty(this.mSyncTextView.getText())) {
                        this.mSyncTextView.setText(this.mSyncText);
                    }
                    this.mSyncTextSecondLine = false;
                    TextView textView7 = this.mSyncTextView;
                    if (z) {
                        f3 = ((-measureText3) - 48.0f) - ((float) (this.mInsetSubtitleImage ? this.mImageViewSize + 16 : 0));
                    } else {
                        f3 = this.mSyncLeftX;
                    }
                    textView7.setTranslationX(f3);
                } else {
                    this.mSyncLeftX = z ? Math.max(((float) this.mExpandedBounds.right) - measureText2, 0.0f) : this.currentSubtitleX;
                    float height = this.mSyncBottomY + ((float) rect3.height());
                    this.mSyncTopY = height;
                    this.mSyncBottomY = height + ((float) rect3.height());
                    if (TextUtils.isEmpty(this.mSyncTextView.getText())) {
                        this.mSyncTextView.setText(this.mSyncText);
                    }
                    this.mSyncTextSecondLine = true;
                    this.mSyncTextView.setTranslationX(z ? (float) (-this.mExpandedBounds.left) : this.mCollapsedDrawX);
                }
            }
        }
        return false;
    }

    public void setAppbarMargin(int i, int i2, int i3, int i4) {
        this.mAppbarMarginLeft = i;
        this.mAppbarMarginRight = i2;
        this.mAppbarMarginBottom = i4;
    }

    private boolean calculateIsRtl(CharSequence charSequence) {
        boolean z = true;
        if (ViewCompat.getLayoutDirection(this.mView) != 1) {
            z = false;
        }
        return (z ? TextDirectionHeuristicsCompat.FIRSTSTRONG_RTL : TextDirectionHeuristicsCompat.FIRSTSTRONG_LTR).isRtl(charSequence, 0, charSequence.length());
    }

    public float getTitleViewCollapsedTextSize() {
        return this.mCollapsedTextSize;
    }

    public boolean getIsDrawLineInit() {
        return this.mIsDrawLine;
    }

    private void setInterpolatedTextSize(float f) {
        calculateUsingTextSize(f);
        boolean z = USE_SCALING_TEXTURE && this.mScale != 1.0f;
        this.mUseTexture = z;
        if (z) {
            ensureExpandedTexture();
            ensureCollapsedTexture();
            ensureCrossSectionTexture();
        }
        ViewCompat.postInvalidateOnAnimation(this.mView);
    }

    private void setCollapsedTextBlend(float f) {
        ViewCompat.postInvalidateOnAnimation(this.mView);
    }

    private void setExpandedTextBlend(float f) {
        ViewCompat.postInvalidateOnAnimation(this.mView);
    }

    public void setDrawLine(boolean z) {
        this.mDrawLine = z;
    }

    private boolean areTypefacesDifferent(Typeface typeface, Typeface typeface2) {
        return (typeface != null && !typeface.equals(typeface2)) || (typeface == null && typeface2 != null);
    }

    private void calculateUsingTextSize(float f) {
        boolean z;
        int i;
        float f2;
        Layout.Alignment alignment;
        CharSequence charSequence;
        CharSequence charSequence2;
        CharSequence charSequence3;
        boolean z2;
        CharSequence charSequence4 = this.mTitle;
        if (charSequence4 != null && !TextUtils.isEmpty(charSequence4)) {
            float width = (float) this.mCollapsedBounds.width();
            float width2 = (float) this.mExpandedBounds.width();
            if (isClose(f, this.mCollapsedTextSize)) {
                f2 = this.mCollapsedTextSize;
                this.mScale = 1.0f;
                if (areTypefacesDifferent(this.mCurrentTypeface, this.mCollapsedTypeface)) {
                    this.mCurrentTypeface = this.mCollapsedTypeface;
                    z2 = true;
                } else {
                    z2 = false;
                }
                z = z2;
                width2 = width;
                i = 1;
            } else {
                float f3 = this.mExpandedTextSize;
                if (areTypefacesDifferent(this.mCurrentTypeface, this.mExpandedTypeface)) {
                    this.mCurrentTypeface = this.mExpandedTypeface;
                    z = true;
                } else {
                    z = false;
                }
                if (isClose(f, this.mExpandedTextSize)) {
                    this.mScale = 1.0f;
                } else {
                    this.mScale = f / this.mExpandedTextSize;
                }
                int i2 = (((this.mCollapsedTextSize / this.mExpandedTextSize) * width2) > width ? 1 : (((this.mCollapsedTextSize / this.mExpandedTextSize) * width2) == width ? 0 : -1));
                i = this.maxLines;
                f2 = f3;
            }
            float f4 = 0.0f;
            int i3 = (width2 > 0.0f ? 1 : (width2 == 0.0f ? 0 : -1));
            if (i3 > 0) {
                z = this.mCurrentTitleSize != f2 || this.mBoundsChanged || z;
                this.mCurrentTitleSize = f2;
                this.mBoundsChanged = false;
            }
            if (this.mTextToDraw == null || z) {
                this.mTextPaint.setTextSize(lerp(this.mExpandedTextSize, this.mCollapsedTextSize, this.mExpandedFraction, this.mTextSizeInterpolator));
                this.mTextPaint.setTypeface(this.mCurrentTypeface);
                if (this.mSubTitleView != null) {
                    int round = Math.round(this.subtitlePaint.descent() - this.subtitlePaint.ascent());
                    float measuredHeight = ((float) ((this.mExpandedBounds.top + this.mTitleView.getMeasuredHeight()) + this.mView.getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space1))) - (((float) this.mView.getResources().getDimensionPixelOffset(R$dimen.op_control_margin_space2)) * this.mExpandedFraction);
                    this.mSubTitleView.setTranslationY(measuredHeight);
                    if (this.mInsetSubtitleImage) {
                        this.mSubTitleView.setTranslationX(ViewCompat.getLayoutDirection(this.mView) == 1 ? (float) (((-this.mExpandedBounds.left) - this.mImageViewSize) - 16) : this.mCollapsedDrawX + ((float) this.mImageViewSize) + 16.0f);
                        this.mImageView.setTranslationY(((float) Math.max(round - this.mImageViewSize, 0)) + measuredHeight + ((float) ("net.oneplus.weather".equals(this.mView.getContext().getPackageName()) ? 2 : 0)));
                        if (this.mDrawBitmap != null && this.mImageView.getDrawable() == null) {
                            this.mImageView.setImageBitmap(this.mDrawBitmap);
                        }
                    }
                    TextView textView = this.mSyncTextView;
                    if (textView != null) {
                        if (this.mSyncTextSecondLine) {
                            f4 = (this.subtitlePaint.descent() - this.subtitlePaint.ascent()) + ViewUtils.dpToPx(this.mView.getContext(), 6);
                        }
                        textView.setTranslationY(measuredHeight + f4);
                    }
                }
                if (i3 > 0 && this.mTitle != null) {
                    StaticLayout staticLayout = new StaticLayout(this.mTitle, this.mTextPaint, (int) width2, Layout.Alignment.ALIGN_NORMAL, this.lineSpacingMultiplier, this.lineSpacingExtra, false);
                    if (staticLayout.getLineCount() > i) {
                        int i4 = i - 1;
                        if (i4 > 0) {
                            charSequence2 = this.mTitle.subSequence(0, staticLayout.getLineEnd(i4 - 1));
                        } else {
                            charSequence2 = "";
                        }
                        CharSequence subSequence = this.mTitle.subSequence(staticLayout.getLineStart(i4), staticLayout.getLineEnd(i4));
                        if (subSequence.charAt(subSequence.length() - 1) == ' ') {
                            charSequence3 = subSequence.subSequence(subSequence.length() - 1, subSequence.length());
                            subSequence = subSequence.subSequence(0, subSequence.length() - 1);
                        } else {
                            charSequence3 = "";
                        }
                        charSequence = TextUtils.concat(charSequence2, TextUtils.ellipsize(TextUtils.concat(subSequence, "…", charSequence3), this.mTextPaint, width2, TextUtils.TruncateAt.END));
                    } else {
                        charSequence = this.mTitle;
                    }
                    if (!TextUtils.equals(charSequence, this.mTextToDraw)) {
                        this.mTextToDraw = charSequence;
                        this.mIsRtl = calculateIsRtl(charSequence);
                    }
                }
                int i5 = this.mExpandedTextGravity & 8388615;
                if (i5 == 1) {
                    alignment = Layout.Alignment.ALIGN_CENTER;
                } else if (i5 == 5 || i5 == 8388613) {
                    alignment = Layout.Alignment.ALIGN_OPPOSITE;
                } else {
                    alignment = Layout.Alignment.ALIGN_NORMAL;
                }
                if (this.mTextToDraw == null) {
                    this.mTextToDraw = "";
                }
                if (i3 > 0) {
                    this.mTextLayout = new StaticLayout(this.mTextToDraw, this.mTextPaint, (int) width2, alignment, this.lineSpacingMultiplier, this.lineSpacingExtra, false);
                }
            }
        }
    }

    private void ensureExpandedTexture() {
        if (this.mExpandedTitleTexture == null && !this.mExpandedBounds.isEmpty() && !TextUtils.isEmpty(this.mTextToDraw)) {
            calculateOffsets(0.0f);
            int width = this.mTextLayout.getWidth();
            int height = this.mTextLayout.getHeight();
            if (width > 0 && height > 0) {
                this.mExpandedTitleTexture = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                this.mTextLayout.draw(new Canvas(this.mExpandedTitleTexture));
                if (this.mTexturePaint == null) {
                    this.mTexturePaint = new Paint(3);
                }
            }
        }
    }

    private void ensureCollapsedTexture() {
        if (this.mCollapsedTitleTexture == null && !this.mCollapsedBounds.isEmpty() && !TextUtils.isEmpty(this.mTextToDraw)) {
            calculateOffsets(0.0f);
            TextPaint textPaint = this.mTextPaint;
            CharSequence charSequence = this.mTextToDraw;
            int round = Math.round(textPaint.measureText(charSequence, 0, charSequence.length()));
            int round2 = Math.round(this.mTextPaint.descent() - this.mTextPaint.ascent());
            if (round > 0 || round2 > 0) {
                this.mCollapsedTitleTexture = Bitmap.createBitmap(round, round2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(this.mCollapsedTitleTexture);
                CharSequence charSequence2 = this.mTextToDrawCollapsed;
                canvas.drawText(charSequence2, 0, charSequence2.length(), 0.0f, (-this.mTextPaint.ascent()) / this.mScale, this.mTextPaint);
                if (this.mTexturePaint == null) {
                    this.mTexturePaint = new Paint(3);
                }
            }
        }
    }

    private void ensureCrossSectionTexture() {
        if (this.mCrossSectionTitleTexture == null && !this.mCollapsedBounds.isEmpty() && !TextUtils.isEmpty(this.mTextToDraw)) {
            calculateOffsets(0.0f);
            int round = Math.round(this.mTextPaint.measureText(this.mTextToDraw, this.mTextLayout.getLineStart(0), this.mTextLayout.getLineEnd(0)));
            int round2 = Math.round(this.mTextPaint.descent() - this.mTextPaint.ascent());
            if (round > 0 || round2 > 0) {
                this.mCrossSectionTitleTexture = Bitmap.createBitmap(round, round2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(this.mCrossSectionTitleTexture);
                String trim = this.mTextToDrawCollapsed.toString().trim();
                if (trim.endsWith("…")) {
                    trim = trim.substring(0, trim.length() - 1);
                }
                canvas.drawText(trim, 0, this.mTextLayout.getLineEnd(0) <= trim.length() ? this.mTextLayout.getLineEnd(0) : trim.length(), 0.0f, (-this.mTextPaint.ascent()) / this.mScale, (Paint) this.mTextPaint);
                if (this.mTexturePaint == null) {
                    this.mTexturePaint = new Paint(3);
                }
            }
        }
    }

    public void recalculate() {
        if (this.mView.getHeight() > 0 && this.mView.getWidth() > 0) {
            calculateBaseOffsets();
            calculateCurrentOffsets();
            updateImageViewPosition();
        }
    }

    public void updateImageViewPosition() {
        ImageView imageView = this.mImageView;
        if (imageView != null) {
            imageView.setTranslationX(ViewCompat.getLayoutDirection(this.mView) == 1 ? (float) (-this.mExpandedBounds.left) : this.mCollapsedDrawX);
        }
    }

    public void setTitle(CharSequence charSequence) {
        if (charSequence == null || !charSequence.equals(this.mTitle)) {
            this.mTitle = charSequence;
            TextView textView = this.mTitleView;
            if (textView != null) {
                textView.setText(charSequence);
            }
            this.mTextToDraw = null;
            clearTexture();
            recalculate();
            updateTitlePosition();
        }
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public ColorStateList getExpandedSubtitleColor() {
        return this.expandedSubtitleColor;
    }

    public ColorStateList getSyncColor() {
        return this.syncColorList;
    }

    public void setExpandedSubtitleAppearance(int i) {
        TextAppearance textAppearance = new TextAppearance(this.mView.getContext(), i);
        this.mSubtitleAppearance = i;
        ColorStateList colorStateList = textAppearance.textColor;
        if (colorStateList != null) {
            this.expandedSubtitleColor = colorStateList;
        }
        float f = textAppearance.textSize;
        if (f != 0.0f) {
            this.expandedSubtitleSize = f;
        }
        ColorStateList colorStateList2 = textAppearance.shadowColor;
        if (colorStateList2 != null) {
            this.expandedSubtitleShadowColor = colorStateList2;
        }
        this.expandedSubtitleShadowDx = textAppearance.shadowDx;
        this.expandedSubtitleShadowDy = textAppearance.shadowDy;
        this.expandedSubtitleShadowRadius = textAppearance.shadowRadius;
        if (Build.VERSION.SDK_INT >= 16) {
            this.expandedSubtitleTypeface = readFontFamilyTypeface(i);
        }
        recalculate();
    }

    public void setCollapsedSubtitleAppearance(int i) {
        TextAppearance textAppearance = new TextAppearance(this.mView.getContext(), i);
        ColorStateList colorStateList = textAppearance.textColor;
        if (colorStateList != null) {
            this.collapsedSubtitleColor = colorStateList;
        }
        float f = textAppearance.textSize;
        if (f != 0.0f) {
            this.collapsedSubtitleSize = f;
        }
        ColorStateList colorStateList2 = textAppearance.shadowColor;
        if (colorStateList2 != null) {
            this.collapsedSubtitleShadowColor = colorStateList2;
        }
        this.collapsedSubtitleShadowDx = textAppearance.shadowDx;
        this.collapsedSubtitleShadowDy = textAppearance.shadowDy;
        this.collapsedSubtitleShadowRadius = textAppearance.shadowRadius;
        if (Build.VERSION.SDK_INT >= 16) {
            this.collapsedSubtitleTypeface = readFontFamilyTypeface(i);
        }
        recalculate();
    }

    public void setSubtitle(CharSequence charSequence) {
        TextView textView;
        TextView textView2;
        float f;
        if (charSequence == null || !charSequence.equals(this.subtitle)) {
            this.subtitle = charSequence;
            if (!TextUtils.isEmpty(charSequence) && (textView2 = this.mSubTitleView) != null) {
                textView2.setText(charSequence);
                this.subtitleToDraw = charSequence;
                TextView textView3 = this.mSubTitleView;
                int i = 0;
                if (ViewCompat.getLayoutDirection(this.mView) == 1) {
                    int i2 = -this.mExpandedBounds.left;
                    if (this.mInsetSubtitleImage) {
                        i = this.mImageViewSize + 16;
                    }
                    f = (float) (i2 - i);
                } else {
                    float f2 = this.mCollapsedDrawX;
                    if (this.mInsetSubtitleImage) {
                        i = this.mImageViewSize + 16;
                    }
                    f = f2 + ((float) i);
                }
                textView3.setTranslationX(f);
            } else if (TextUtils.isEmpty(charSequence) && (textView = this.mSubTitleView) != null) {
                textView.setText((CharSequence) null);
                this.subtitleToDraw = null;
            }
            clearTexture();
            recalculate();
            updateTitlePosition();
        }
    }

    private void clearTexture() {
        Bitmap bitmap = this.mExpandedTitleTexture;
        if (bitmap != null) {
            bitmap.recycle();
            this.mExpandedTitleTexture = null;
        }
        Bitmap bitmap2 = this.mCollapsedTitleTexture;
        if (bitmap2 != null) {
            bitmap2.recycle();
            this.mCollapsedTitleTexture = null;
        }
        Bitmap bitmap3 = this.mCrossSectionTitleTexture;
        if (bitmap3 != null) {
            bitmap3.recycle();
            this.mCrossSectionTitleTexture = null;
        }
        Bitmap bitmap4 = this.expandedSubtitleTexture;
        if (bitmap4 != null) {
            bitmap4.recycle();
            this.expandedSubtitleTexture = null;
        }
    }

    private static boolean isClose(float f, float f2) {
        return Math.abs(f - f2) < 0.001f;
    }

    public ColorStateList getExpandedTextColor() {
        return this.mExpandedTextColor;
    }

    private static int blendColors(int i, int i2, float f) {
        float f2 = 1.0f - f;
        return Color.argb((int) ((((float) Color.alpha(i)) * f2) + (((float) Color.alpha(i2)) * f)), (int) ((((float) Color.red(i)) * f2) + (((float) Color.red(i2)) * f)), (int) ((((float) Color.green(i)) * f2) + (((float) Color.green(i2)) * f)), (int) ((((float) Color.blue(i)) * f2) + (((float) Color.blue(i2)) * f)));
    }

    private static float lerp(float f, float f2, float f3, TimeInterpolator timeInterpolator) {
        if (timeInterpolator != null) {
            f3 = timeInterpolator.getInterpolation(f3);
        }
        return AnimationUtils.lerp(f, f2, f3);
    }

    private static boolean rectEquals(Rect rect, int i, int i2, int i3, int i4) {
        return rect.left == i && rect.top == i2 && rect.right == i3 && rect.bottom == i4;
    }

    public void setInsetImage(boolean z) {
        this.mInsetSubtitleImage = z;
    }

    public void setImageDrawable(Bitmap bitmap, ImageView imageView) {
        if (imageView == null) {
            this.mInsetSubtitleImage = false;
        } else {
            this.mInsetSubtitleImage = true;
        }
        this.mDrawBitmap = bitmap;
        this.mImageView = imageView;
        recalculate();
    }

    public void setTitleView(TextView textView) {
        this.mTitleView = textView;
        textView.setTextColor(this.mExpandedTextColor);
        this.mTitleView.setMaxLines(this.maxLines);
        this.mTitleView.setEllipsize(TextUtils.TruncateAt.END);
        this.mTitleView.setTextAppearance(this.mTitleAppearance);
        recalculate();
    }

    public void setSubTitleView(TextView textView) {
        this.mSubTitleView = textView;
        textView.setMaxLines(1);
        this.mSubTitleView.setEllipsize(TextUtils.TruncateAt.END);
        this.mSubTitleView.setTextColor(this.expandedSubtitleColor);
        this.mSubTitleView.setTextAppearance(this.mSubtitleAppearance);
        this.mSubTitleView.setTextSize(0, this.expandedSubtitleSize);
        recalculate();
    }

    public void setSyncTextView(TextView textView) {
        this.mSyncTextView = textView;
        textView.setMaxLines(1);
        this.mSyncTextView.setEllipsize(TextUtils.TruncateAt.END);
        this.mSyncTextView.setTextColor(this.syncColorList);
        this.mSyncTextView.setTextAppearance(this.mSubtitleAppearance);
        this.mSyncTextView.setTextSize(0, this.expandedSubtitleSize);
        this.mSyncTextView.getPaint().setFakeBoldText(true);
    }

    public boolean isExistSyncCloud() {
        return !TextUtils.isEmpty(this.mSyncText);
    }

    public void setMenuMargin(int i) {
        this.mMenuMargin = i;
    }
}
