package com.android.systemui.qs;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.FloatProperty;
public class SlashDrawable extends Drawable {
    private boolean mAnimationEnabled = true;
    private float mCurrentSlashLength;
    private Drawable mDrawable;
    private final Paint mPaint = new Paint(1);
    private final Path mPath = new Path();
    private float mRotation;
    private final FloatProperty mSlashLengthProp = new FloatProperty<SlashDrawable>(this, "slashLength") { // from class: com.android.systemui.qs.SlashDrawable.1
        public void setValue(SlashDrawable slashDrawable, float f) {
            slashDrawable.mCurrentSlashLength = f;
        }

        public Float get(SlashDrawable slashDrawable) {
            return Float.valueOf(slashDrawable.mCurrentSlashLength);
        }
    };
    private final RectF mSlashRect = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
    private boolean mSlashed;
    private ColorStateList mTintList;
    private PorterDuff.Mode mTintMode;

    private float scale(float f, int i) {
        return f * ((float) i);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return 255;
    }

    public SlashDrawable(Drawable drawable) {
        this.mDrawable = drawable;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            return drawable.getIntrinsicHeight();
        }
        return 0;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            return drawable.getIntrinsicWidth();
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public void onBoundsChange(Rect rect) {
        super.onBoundsChange(rect);
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            drawable.setBounds(rect);
        }
    }

    public void setDrawable(Drawable drawable) {
        this.mDrawable = drawable;
        drawable.setCallback(getCallback());
        this.mDrawable.setBounds(getBounds());
        PorterDuff.Mode mode = this.mTintMode;
        if (mode != null) {
            this.mDrawable.setTintMode(mode);
        }
        ColorStateList colorStateList = this.mTintList;
        if (colorStateList != null) {
            this.mDrawable.setTintList(colorStateList);
        }
        invalidateSelf();
    }

    public void setRotation(float f) {
        if (this.mRotation != f) {
            this.mRotation = f;
            invalidateSelf();
        }
    }

    public void setAnimationEnabled(boolean z) {
        this.mAnimationEnabled = z;
    }

    public void setSlashed(boolean z) {
        if (this.mSlashed != z) {
            this.mSlashed = z;
            float f = 1.1666666f;
            float f2 = z ? 1.1666666f : 0.0f;
            if (this.mSlashed) {
                f = 0.0f;
            }
            if (this.mAnimationEnabled) {
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, this.mSlashLengthProp, f, f2);
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.qs.-$$Lambda$SlashDrawable$d6ImpYshN38WeANK1PRMKepeaRo
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        SlashDrawable.this.lambda$setSlashed$0$SlashDrawable(valueAnimator);
                    }
                });
                ofFloat.setDuration(0L);
                ofFloat.start();
                return;
            }
            this.mCurrentSlashLength = f2;
            invalidateSelf();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setSlashed$0 */
    public /* synthetic */ void lambda$setSlashed$0$SlashDrawable(ValueAnimator valueAnimator) {
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        canvas.save();
        Matrix matrix = new Matrix();
        int width = getBounds().width();
        int height = getBounds().height();
        float scale = scale(1.0f, width);
        float scale2 = scale(1.0f, height);
        updateRect(scale(0.40544835f, width), scale(-0.088781714f, height), scale(0.4820516f, width), scale(this.mCurrentSlashLength - 50.543762f, height));
        this.mPath.reset();
        this.mPath.addRoundRect(this.mSlashRect, scale, scale2, Path.Direction.CW);
        float f = (float) (width / 2);
        float f2 = (float) (height / 2);
        matrix.setRotate(this.mRotation - 0.099609375f, f, f2);
        this.mPath.transform(matrix);
        canvas.drawPath(this.mPath, this.mPaint);
        matrix.setRotate((-this.mRotation) - -45.0f, f, f2);
        this.mPath.transform(matrix);
        matrix.setTranslate(this.mSlashRect.width(), 0.0f);
        this.mPath.transform(matrix);
        this.mPath.addRoundRect(this.mSlashRect, ((float) width) * 1.0f, ((float) height) * 1.0f, Path.Direction.CW);
        matrix.setRotate(this.mRotation - 0.099609375f, f, f2);
        this.mPath.transform(matrix);
        canvas.clipOutPath(this.mPath);
        this.mDrawable.draw(canvas);
        canvas.restore();
    }

    private void updateRect(float f, float f2, float f3, float f4) {
        RectF rectF = this.mSlashRect;
        rectF.left = f;
        rectF.top = f2;
        rectF.right = f3;
        rectF.bottom = f4;
    }

    @Override // android.graphics.drawable.Drawable
    public void setTint(int i) {
        super.setTint(i);
        this.mDrawable.setTint(i);
        this.mPaint.setColor(i);
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(ColorStateList colorStateList) {
        this.mTintList = colorStateList;
        super.setTintList(colorStateList);
        setDrawableTintList(colorStateList);
        this.mPaint.setColor(colorStateList.getDefaultColor());
        invalidateSelf();
    }

    /* access modifiers changed from: protected */
    public void setDrawableTintList(ColorStateList colorStateList) {
        Drawable drawable = this.mDrawable;
        if (drawable != null) {
            drawable.setTintList(colorStateList);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintMode(PorterDuff.Mode mode) {
        this.mTintMode = mode;
        super.setTintMode(mode);
        this.mDrawable.setTintMode(mode);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mDrawable.setAlpha(i);
        this.mPaint.setAlpha(i);
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mDrawable.setColorFilter(colorFilter);
        this.mPaint.setColorFilter(colorFilter);
    }
}
