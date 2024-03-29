package com.android.systemui.media;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.View;
import androidx.annotation.Keep;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R$styleable;
import java.util.ArrayList;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParser;
/* compiled from: IlluminationDrawable.kt */
@Keep
public final class IlluminationDrawable extends Drawable {
    private ValueAnimator backgroundAnimation;
    private int backgroundColor;
    private float cornerRadius;
    private float highlight;
    private int highlightColor;
    private final ArrayList<LightSourceDrawable> lightSources = new ArrayList<>();
    private Paint paint = new Paint();
    private int[] themeAttrs;
    private float[] tmpHsl = {0.0f, 0.0f, 0.0f};

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -2;
    }

    /* access modifiers changed from: private */
    public final void setBackgroundColor(int i) {
        if (i != this.backgroundColor) {
            this.backgroundColor = i;
            animateBackground();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(@NotNull Canvas canvas) {
        Intrinsics.checkParameterIsNotNull(canvas, "canvas");
        float f = this.cornerRadius;
        canvas.drawRoundRect(0.0f, 0.0f, (float) getBounds().width(), (float) getBounds().height(), f, f, this.paint);
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(@NotNull Outline outline) {
        Intrinsics.checkParameterIsNotNull(outline, "outline");
        outline.setRoundRect(getBounds(), this.cornerRadius);
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(@NotNull Resources resources, @NotNull XmlPullParser xmlPullParser, @NotNull AttributeSet attributeSet, @Nullable Resources.Theme theme) {
        Intrinsics.checkParameterIsNotNull(resources, "r");
        Intrinsics.checkParameterIsNotNull(xmlPullParser, "parser");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
        TypedArray obtainAttributes = Drawable.obtainAttributes(resources, theme, attributeSet, R$styleable.IlluminationDrawable);
        this.themeAttrs = obtainAttributes.extractThemeAttrs();
        Intrinsics.checkExpressionValueIsNotNull(obtainAttributes, "a");
        updateStateFromTypedArray(obtainAttributes);
        obtainAttributes.recycle();
    }

    private final void updateStateFromTypedArray(TypedArray typedArray) {
        if (typedArray.hasValue(R$styleable.IlluminationDrawable_cornerRadius)) {
            this.cornerRadius = typedArray.getDimension(R$styleable.IlluminationDrawable_cornerRadius, this.cornerRadius);
        }
        if (typedArray.hasValue(R$styleable.IlluminationDrawable_highlight)) {
            this.highlight = ((float) typedArray.getInteger(R$styleable.IlluminationDrawable_highlight, 0)) / 100.0f;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0007, code lost:
        if (r0.length <= 0) goto L_0x000f;
     */
    @Override // android.graphics.drawable.Drawable
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canApplyTheme() {
        /*
            r1 = this;
            int[] r0 = r1.themeAttrs
            if (r0 == 0) goto L_0x000f
            if (r0 == 0) goto L_0x000a
            int r0 = r0.length
            if (r0 > 0) goto L_0x0015
            goto L_0x000f
        L_0x000a:
            kotlin.jvm.internal.Intrinsics.throwNpe()
            r1 = 0
            throw r1
        L_0x000f:
            boolean r1 = super.canApplyTheme()
            if (r1 == 0) goto L_0x0017
        L_0x0015:
            r1 = 1
            goto L_0x0018
        L_0x0017:
            r1 = 0
        L_0x0018:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.IlluminationDrawable.canApplyTheme():boolean");
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(@NotNull Resources.Theme theme) {
        Intrinsics.checkParameterIsNotNull(theme, "t");
        super.applyTheme(theme);
        int[] iArr = this.themeAttrs;
        if (iArr != null) {
            TypedArray resolveAttributes = theme.resolveAttributes(iArr, R$styleable.IlluminationDrawable);
            Intrinsics.checkExpressionValueIsNotNull(resolveAttributes, "a");
            updateStateFromTypedArray(resolveAttributes);
            resolveAttributes.recycle();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        throw new UnsupportedOperationException("Color filters are not supported");
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        throw new UnsupportedOperationException("Alpha is not supported");
    }

    private final void animateBackground() {
        ColorUtils.colorToHSL(this.backgroundColor, this.tmpHsl);
        float[] fArr = this.tmpHsl;
        float f = fArr[2];
        float f2 = this.highlight;
        fArr[2] = MathUtils.constrain(f < 1.0f - f2 ? f + f2 : f - f2, 0.0f, 1.0f);
        int color = this.paint.getColor();
        int i = this.highlightColor;
        int HSLToColor = ColorUtils.HSLToColor(this.tmpHsl);
        ValueAnimator valueAnimator = this.backgroundAnimation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(370L);
        ofFloat.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this, color, i, HSLToColor) { // from class: com.android.systemui.media.IlluminationDrawable$animateBackground$$inlined$apply$lambda$1
            final /* synthetic */ int $finalHighlight$inlined;
            final /* synthetic */ int $initialBackground$inlined;
            final /* synthetic */ int $initialHighlight$inlined;
            final /* synthetic */ IlluminationDrawable this$0;

            {
                this.this$0 = r1;
                this.$initialBackground$inlined = r2;
                this.$initialHighlight$inlined = r3;
                this.$finalHighlight$inlined = r4;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                Intrinsics.checkExpressionValueIsNotNull(valueAnimator2, "it");
                Object animatedValue = valueAnimator2.getAnimatedValue();
                if (animatedValue != null) {
                    float floatValue = ((Float) animatedValue).floatValue();
                    IlluminationDrawable.access$getPaint$p(this.this$0).setColor(ColorUtils.blendARGB(this.$initialBackground$inlined, IlluminationDrawable.access$getBackgroundColor$p(this.this$0), floatValue));
                    IlluminationDrawable.access$setHighlightColor$p(this.this$0, ColorUtils.blendARGB(this.$initialHighlight$inlined, this.$finalHighlight$inlined, floatValue));
                    for (LightSourceDrawable lightSourceDrawable : IlluminationDrawable.access$getLightSources$p(this.this$0)) {
                        lightSourceDrawable.setHighlightColor(IlluminationDrawable.access$getHighlightColor$p(this.this$0));
                    }
                    this.this$0.invalidateSelf();
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Float");
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter(this, color, i, HSLToColor) { // from class: com.android.systemui.media.IlluminationDrawable$animateBackground$$inlined$apply$lambda$2
            final /* synthetic */ IlluminationDrawable this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(@Nullable Animator animator) {
                IlluminationDrawable.access$setBackgroundAnimation$p(this.this$0, null);
            }
        });
        ofFloat.start();
        this.backgroundAnimation = ofFloat;
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(@Nullable ColorStateList colorStateList) {
        super.setTintList(colorStateList);
        if (colorStateList != null) {
            setBackgroundColor(colorStateList.getDefaultColor());
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public final void registerLightSource(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "lightSource");
        if (view.getBackground() instanceof LightSourceDrawable) {
            ArrayList<LightSourceDrawable> arrayList = this.lightSources;
            Drawable background = view.getBackground();
            if (background != null) {
                arrayList.add((LightSourceDrawable) background);
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.media.LightSourceDrawable");
        } else if (view.getForeground() instanceof LightSourceDrawable) {
            ArrayList<LightSourceDrawable> arrayList2 = this.lightSources;
            Drawable foreground = view.getForeground();
            if (foreground != null) {
                arrayList2.add((LightSourceDrawable) foreground);
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.media.LightSourceDrawable");
        }
    }
}
