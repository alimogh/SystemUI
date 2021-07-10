package com.android.systemui.controls;

import android.content.Context;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Prefs;
import com.android.systemui.recents.TriangleShape;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: TooltipManager.kt */
public final class TooltipManager {
    private final View arrowView;
    private final boolean below;
    @NotNull
    private final ViewGroup layout;
    private final int maxTimesShown;
    private final String preferenceName;
    @NotNull
    private final Function1<Integer, Unit> preferenceStorer;
    private int shown;
    private final TextView textView;

    public TooltipManager(@NotNull Context context, @NotNull String str, int i, boolean z) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(str, "preferenceName");
        this.preferenceName = str;
        this.maxTimesShown = i;
        this.below = z;
        this.shown = Prefs.getInt(context, str, 0);
        View inflate = LayoutInflater.from(context).inflate(C0011R$layout.controls_onboarding, (ViewGroup) null);
        if (inflate != null) {
            this.layout = (ViewGroup) inflate;
            this.preferenceStorer = new Function1<Integer, Unit>(this, context) { // from class: com.android.systemui.controls.TooltipManager$preferenceStorer$1
                final /* synthetic */ Context $context;
                final /* synthetic */ TooltipManager this$0;

                {
                    this.this$0 = r1;
                    this.$context = r2;
                }

                /* Return type fixed from 'java.lang.Object' to match base method */
                /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ Unit invoke(Integer num) {
                    invoke(num.intValue());
                    return Unit.INSTANCE;
                }

                public final void invoke(int i2) {
                    Prefs.putInt(this.$context, TooltipManager.access$getPreferenceName$p(this.this$0), i2);
                }
            };
            this.layout.setAlpha(0.0f);
            this.textView = (TextView) this.layout.requireViewById(C0008R$id.onboarding_text);
            this.layout.requireViewById(C0008R$id.dismiss).setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.controls.TooltipManager$$special$$inlined$apply$lambda$1
                final /* synthetic */ TooltipManager this$0;

                {
                    this.this$0 = r1;
                }

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.this$0.hide(true);
                }
            });
            View requireViewById = this.layout.requireViewById(C0008R$id.arrow);
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(16843829, typedValue, true);
            int color = context.getResources().getColor(typedValue.resourceId, context.getTheme());
            int dimensionPixelSize = context.getResources().getDimensionPixelSize(C0005R$dimen.recents_onboarding_toast_arrow_corner_radius);
            ViewGroup.LayoutParams layoutParams = requireViewById.getLayoutParams();
            ShapeDrawable shapeDrawable = new ShapeDrawable(TriangleShape.create((float) layoutParams.width, (float) layoutParams.height, this.below));
            Paint paint = shapeDrawable.getPaint();
            Intrinsics.checkExpressionValueIsNotNull(paint, "arrowPaint");
            paint.setColor(color);
            paint.setPathEffect(new CornerPathEffect((float) dimensionPixelSize));
            requireViewById.setBackground(shapeDrawable);
            this.arrowView = requireViewById;
            if (!this.below) {
                this.layout.removeView(requireViewById);
                this.layout.addView(this.arrowView);
                View view = this.arrowView;
                Intrinsics.checkExpressionValueIsNotNull(view, "arrowView");
                ViewGroup.LayoutParams layoutParams2 = view.getLayoutParams();
                if (layoutParams2 != null) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams2;
                    marginLayoutParams.bottomMargin = marginLayoutParams.topMargin;
                    marginLayoutParams.topMargin = 0;
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
            }
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ TooltipManager(Context context, String str, int i, boolean z, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, str, (i2 & 4) != 0 ? 2 : i, (i2 & 8) != 0 ? true : z);
    }

    @NotNull
    public final ViewGroup getLayout() {
        return this.layout;
    }

    public final void show(int i, int i2, int i3) {
        if (shouldShow()) {
            this.textView.setText(i);
            int i4 = this.shown + 1;
            this.shown = i4;
            this.preferenceStorer.invoke(Integer.valueOf(i4));
            this.layout.post(new Runnable(this, i2, i3) { // from class: com.android.systemui.controls.TooltipManager$show$1
                final /* synthetic */ int $x;
                final /* synthetic */ int $y;
                final /* synthetic */ TooltipManager this$0;

                {
                    this.this$0 = r1;
                    this.$x = r2;
                    this.$y = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    int[] iArr = new int[2];
                    this.this$0.getLayout().getLocationOnScreen(iArr);
                    int i5 = 0;
                    this.this$0.getLayout().setTranslationX((float) ((this.$x - iArr[0]) - (this.this$0.getLayout().getWidth() / 2)));
                    ViewGroup layout = this.this$0.getLayout();
                    float f = (float) (this.$y - iArr[1]);
                    if (!TooltipManager.access$getBelow$p(this.this$0)) {
                        i5 = this.this$0.getLayout().getHeight();
                    }
                    layout.setTranslationY(f - ((float) i5));
                    if (this.this$0.getLayout().getAlpha() == 0.0f) {
                        this.this$0.getLayout().animate().alpha(1.0f).withLayer().setStartDelay(500).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
                    }
                }
            });
        }
    }

    public final void hide(boolean z) {
        if (this.layout.getAlpha() != 0.0f) {
            this.layout.post(new Runnable(this, z) { // from class: com.android.systemui.controls.TooltipManager$hide$1
                final /* synthetic */ boolean $animate;
                final /* synthetic */ TooltipManager this$0;

                {
                    this.this$0 = r1;
                    this.$animate = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    if (this.$animate) {
                        this.this$0.getLayout().animate().alpha(0.0f).withLayer().setStartDelay(0).setDuration(100).setInterpolator(new AccelerateInterpolator()).start();
                        return;
                    }
                    this.this$0.getLayout().animate().cancel();
                    this.this$0.getLayout().setAlpha(0.0f);
                }
            });
        }
    }

    private final boolean shouldShow() {
        return this.shown < this.maxTimesShown;
    }
}
