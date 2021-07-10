package com.android.systemui.statusbar.notification.row;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.android.systemui.Interpolators;
import com.android.systemui.Prefs;
import com.android.systemui.statusbar.notification.row.NotificationConversationInfo;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: PriorityOnboardingDialogController.kt */
public final class PriorityOnboardingDialogController {
    private final long IMPORTANCE_ANIM_DELAY = 150;
    private final long IMPORTANCE_ANIM_GROW_DURATION = 250;
    private final long IMPORTANCE_ANIM_SHRINK_DELAY = 25;
    private final long IMPORTANCE_ANIM_SHRINK_DURATION = 200;
    private final Interpolator OVERSHOOT = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.4f);
    @NotNull
    private final Drawable badge;
    @NotNull
    private final Context context;
    private Dialog dialog;
    @NotNull
    private final Drawable icon;
    private final boolean ignoresDnd;
    private final NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener;
    private final boolean showsAsBubble;
    @NotNull
    private final View view;
    private final int wmFlags = -2130444288;

    public PriorityOnboardingDialogController(@NotNull View view, @NotNull Context context, boolean z, boolean z2, @NotNull Drawable drawable, @NotNull NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener, @NotNull Drawable drawable2) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(drawable, "icon");
        Intrinsics.checkParameterIsNotNull(onConversationSettingsClickListener, "onConversationSettingsClickListener");
        Intrinsics.checkParameterIsNotNull(drawable2, "badge");
        this.view = view;
        this.context = context;
        this.ignoresDnd = z;
        this.showsAsBubble = z2;
        this.icon = drawable;
        this.onConversationSettingsClickListener = onConversationSettingsClickListener;
        this.badge = drawable2;
    }

    public final void init() {
        initDialog();
    }

    public final void show() {
        Dialog dialog = this.dialog;
        if (dialog != null) {
            dialog.show();
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    public final void done() {
        Prefs.putBoolean(this.context, "HasUserSeenPriorityOnboarding", true);
        Dialog dialog = this.dialog;
        if (dialog != null) {
            dialog.dismiss();
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    public final void settings() {
        Prefs.putBoolean(this.context, "HasUserSeenPriorityOnboarding", true);
        Dialog dialog = this.dialog;
        if (dialog != null) {
            dialog.dismiss();
            NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener = this.onConversationSettingsClickListener;
            if (onConversationSettingsClickListener != null) {
                onConversationSettingsClickListener.onClick();
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("dialog");
        throw null;
    }

    /* compiled from: PriorityOnboardingDialogController.kt */
    public static final class Builder {
        private Drawable badge;
        private Context context;
        private Drawable icon;
        private boolean ignoresDnd;
        private NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener;
        private boolean showAsBubble;
        private View view;

        @NotNull
        public final Builder setView(@NotNull View view) {
            Intrinsics.checkParameterIsNotNull(view, "v");
            this.view = view;
            return this;
        }

        @NotNull
        public final Builder setContext(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "c");
            this.context = context;
            return this;
        }

        @NotNull
        public final Builder setIgnoresDnd(boolean z) {
            this.ignoresDnd = z;
            return this;
        }

        @NotNull
        public final Builder setShowsAsBubble(boolean z) {
            this.showAsBubble = z;
            return this;
        }

        @NotNull
        public final Builder setIcon(@NotNull Drawable drawable) {
            Intrinsics.checkParameterIsNotNull(drawable, "draw");
            this.icon = drawable;
            return this;
        }

        @NotNull
        public final Builder setBadge(@NotNull Drawable drawable) {
            Intrinsics.checkParameterIsNotNull(drawable, "badge");
            this.badge = drawable;
            return this;
        }

        @NotNull
        public final Builder setOnSettingsClick(@NotNull NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener) {
            Intrinsics.checkParameterIsNotNull(onConversationSettingsClickListener, "onClick");
            this.onConversationSettingsClickListener = onConversationSettingsClickListener;
            return this;
        }

        @NotNull
        public final PriorityOnboardingDialogController build() {
            View view = this.view;
            if (view != null) {
                Context context = this.context;
                if (context != null) {
                    boolean z = this.ignoresDnd;
                    boolean z2 = this.showAsBubble;
                    Drawable drawable = this.icon;
                    if (drawable != null) {
                        NotificationConversationInfo.OnConversationSettingsClickListener onConversationSettingsClickListener = this.onConversationSettingsClickListener;
                        if (onConversationSettingsClickListener != null) {
                            Drawable drawable2 = this.badge;
                            if (drawable2 != null) {
                                return new PriorityOnboardingDialogController(view, context, z, z2, drawable, onConversationSettingsClickListener, drawable2);
                            }
                            Intrinsics.throwUninitializedPropertyAccessException("badge");
                            throw null;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("onConversationSettingsClickListener");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("icon");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("context");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("view");
            throw null;
        }
    }

    private final void initDialog() {
        Dialog dialog = new Dialog(this.context);
        this.dialog = dialog;
        if (dialog.getWindow() != null) {
            Dialog dialog2 = this.dialog;
            if (dialog2 != null) {
                Window window = dialog2.getWindow();
                if (window != null) {
                    window.requestFeature(1);
                }
                Dialog dialog3 = this.dialog;
                if (dialog3 != null) {
                    dialog3.setTitle(" ");
                    Dialog dialog4 = this.dialog;
                    if (dialog4 != null) {
                        dialog4.setContentView(this.view);
                        dialog4.setCanceledOnTouchOutside(true);
                        TextView textView = (TextView) dialog4.findViewById(C0008R$id.done_button);
                        if (textView != null) {
                            textView.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController$initDialog$$inlined$apply$lambda$1
                                final /* synthetic */ PriorityOnboardingDialogController this$0;

                                {
                                    this.this$0 = r1;
                                }

                                @Override // android.view.View.OnClickListener
                                public final void onClick(View view) {
                                    this.this$0.done();
                                }
                            });
                        }
                        TextView textView2 = (TextView) dialog4.findViewById(C0008R$id.settings_button);
                        if (textView2 != null) {
                            textView2.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController$initDialog$$inlined$apply$lambda$2
                                final /* synthetic */ PriorityOnboardingDialogController this$0;

                                {
                                    this.this$0 = r1;
                                }

                                @Override // android.view.View.OnClickListener
                                public final void onClick(View view) {
                                    this.this$0.settings();
                                }
                            });
                        }
                        ImageView imageView = (ImageView) dialog4.findViewById(C0008R$id.conversation_icon);
                        if (imageView != null) {
                            imageView.setImageDrawable(this.icon);
                        }
                        ImageView imageView2 = (ImageView) dialog4.findViewById(C0008R$id.icon);
                        if (imageView2 != null) {
                            imageView2.setImageDrawable(this.badge);
                        }
                        ImageView imageView3 = (ImageView) dialog4.findViewById(C0008R$id.conversation_icon_badge_ring);
                        ImageView imageView4 = (ImageView) dialog4.findViewById(C0008R$id.conversation_icon_badge_bg);
                        Intrinsics.checkExpressionValueIsNotNull(imageView3, "mImportanceRingView");
                        Drawable drawable = imageView3.getDrawable();
                        if (drawable != null) {
                            GradientDrawable gradientDrawable = (GradientDrawable) drawable;
                            gradientDrawable.mutate();
                            Intrinsics.checkExpressionValueIsNotNull(imageView4, "conversationIconBadgeBg");
                            Drawable drawable2 = imageView4.getDrawable();
                            if (drawable2 != null) {
                                GradientDrawable gradientDrawable2 = (GradientDrawable) drawable2;
                                gradientDrawable2.mutate();
                                int color = dialog4.getContext().getResources().getColor(17170731);
                                Context context = dialog4.getContext();
                                Intrinsics.checkExpressionValueIsNotNull(context, "context");
                                int dimensionPixelSize = context.getResources().getDimensionPixelSize(17105231);
                                Context context2 = dialog4.getContext();
                                Intrinsics.checkExpressionValueIsNotNull(context2, "context");
                                int dimensionPixelSize2 = context2.getResources().getDimensionPixelSize(17105229);
                                Context context3 = dialog4.getContext();
                                Intrinsics.checkExpressionValueIsNotNull(context3, "context");
                                int dimensionPixelSize3 = context3.getResources().getDimensionPixelSize(17105230) - (dimensionPixelSize * 2);
                                Context context4 = dialog4.getContext();
                                Intrinsics.checkExpressionValueIsNotNull(context4, "context");
                                int dimensionPixelSize4 = context4.getResources().getDimensionPixelSize(17105117);
                                PriorityOnboardingDialogController$initDialog$1$animatorUpdateListener$1 priorityOnboardingDialogController$initDialog$1$animatorUpdateListener$1 = new ValueAnimator.AnimatorUpdateListener(gradientDrawable, color, dimensionPixelSize3, imageView3) { // from class: com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController$initDialog$1$animatorUpdateListener$1
                                    final /* synthetic */ int $baseSize;
                                    final /* synthetic */ ImageView $mImportanceRingView;
                                    final /* synthetic */ GradientDrawable $ring;
                                    final /* synthetic */ int $ringColor;

                                    {
                                        this.$ring = r1;
                                        this.$ringColor = r2;
                                        this.$baseSize = r3;
                                        this.$mImportanceRingView = r4;
                                    }

                                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "animation");
                                        Object animatedValue = valueAnimator.getAnimatedValue();
                                        if (animatedValue != null) {
                                            int intValue = ((Integer) animatedValue).intValue();
                                            this.$ring.setStroke(intValue, this.$ringColor);
                                            int i = this.$baseSize + (intValue * 2);
                                            this.$ring.setSize(i, i);
                                            this.$mImportanceRingView.invalidate();
                                            return;
                                        }
                                        throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
                                    }
                                };
                                ValueAnimator ofInt = ValueAnimator.ofInt(0, dimensionPixelSize2);
                                Intrinsics.checkExpressionValueIsNotNull(ofInt, "ValueAnimator.ofInt(0, largeThickness)");
                                ofInt.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
                                ofInt.setDuration(this.IMPORTANCE_ANIM_GROW_DURATION);
                                ofInt.addUpdateListener(priorityOnboardingDialogController$initDialog$1$animatorUpdateListener$1);
                                ValueAnimator ofInt2 = ValueAnimator.ofInt(dimensionPixelSize2, dimensionPixelSize);
                                Intrinsics.checkExpressionValueIsNotNull(ofInt2, "ValueAnimator.ofInt(larg…kness, standardThickness)");
                                ofInt2.setDuration(this.IMPORTANCE_ANIM_SHRINK_DURATION);
                                ofInt2.setStartDelay(this.IMPORTANCE_ANIM_SHRINK_DELAY);
                                ofInt2.setInterpolator(this.OVERSHOOT);
                                ofInt2.addUpdateListener(priorityOnboardingDialogController$initDialog$1$animatorUpdateListener$1);
                                ofInt2.addListener(new AnimatorListenerAdapter(gradientDrawable2, dimensionPixelSize3, imageView4, dimensionPixelSize4) { // from class: com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController$initDialog$1$3
                                    final /* synthetic */ int $baseSize;
                                    final /* synthetic */ GradientDrawable $bg;
                                    final /* synthetic */ int $bgSize;
                                    final /* synthetic */ ImageView $conversationIconBadgeBg;

                                    {
                                        this.$bg = r1;
                                        this.$baseSize = r2;
                                        this.$conversationIconBadgeBg = r3;
                                        this.$bgSize = r4;
                                    }

                                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                                    public void onAnimationStart(@Nullable Animator animator) {
                                        GradientDrawable gradientDrawable3 = this.$bg;
                                        int i = this.$baseSize;
                                        gradientDrawable3.setSize(i, i);
                                        this.$conversationIconBadgeBg.invalidate();
                                    }

                                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                                    public void onAnimationEnd(@Nullable Animator animator) {
                                        GradientDrawable gradientDrawable3 = this.$bg;
                                        int i = this.$bgSize;
                                        gradientDrawable3.setSize(i, i);
                                        this.$conversationIconBadgeBg.invalidate();
                                    }
                                });
                                AnimatorSet animatorSet = new AnimatorSet();
                                animatorSet.setStartDelay(this.IMPORTANCE_ANIM_DELAY);
                                animatorSet.playSequentially(ofInt, ofInt2);
                                Dialog dialog5 = this.dialog;
                                if (dialog5 != null) {
                                    int dimensionPixelSize5 = dialog5.getContext().getResources().getDimensionPixelSize(C0005R$dimen.conversation_onboarding_bullet_gap_width);
                                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                                    spannableStringBuilder.append(dialog4.getContext().getText(C0015R$string.priority_onboarding_show_at_top_text), new BulletSpan(dimensionPixelSize5), 0);
                                    spannableStringBuilder.append((CharSequence) System.lineSeparator());
                                    spannableStringBuilder.append(dialog4.getContext().getText(C0015R$string.priority_onboarding_show_avatar_text), new BulletSpan(dimensionPixelSize5), 0);
                                    if (this.showsAsBubble) {
                                        spannableStringBuilder.append((CharSequence) System.lineSeparator());
                                        spannableStringBuilder.append(dialog4.getContext().getText(C0015R$string.priority_onboarding_appear_as_bubble_text), new BulletSpan(dimensionPixelSize5), 0);
                                    }
                                    if (this.ignoresDnd) {
                                        spannableStringBuilder.append((CharSequence) System.lineSeparator());
                                        spannableStringBuilder.append(dialog4.getContext().getText(C0015R$string.priority_onboarding_ignores_dnd_text), new BulletSpan(dimensionPixelSize5), 0);
                                    }
                                    ((TextView) dialog4.findViewById(C0008R$id.behaviors)).setText(spannableStringBuilder);
                                    Window window2 = dialog4.getWindow();
                                    if (window2 != null) {
                                        window2.setBackgroundDrawable(new ColorDrawable(0));
                                        window2.addFlags(this.wmFlags);
                                        window2.setType(2017);
                                        window2.setWindowAnimations(16973910);
                                        WindowManager.LayoutParams attributes = window2.getAttributes();
                                        attributes.format = -3;
                                        attributes.setTitle(PriorityOnboardingDialogController.class.getSimpleName());
                                        attributes.gravity = 81;
                                        WindowManager.LayoutParams attributes2 = window2.getAttributes();
                                        Intrinsics.checkExpressionValueIsNotNull(attributes2, "attributes");
                                        attributes.setFitInsetsTypes(attributes2.getFitInsetsTypes() & (~WindowInsets.Type.statusBars()));
                                        attributes.width = -1;
                                        attributes.height = -2;
                                        window2.setAttributes(attributes);
                                    }
                                    animatorSet.start();
                                    return;
                                }
                                Intrinsics.throwUninitializedPropertyAccessException("dialog");
                                throw null;
                            }
                            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.GradientDrawable");
                        }
                        throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.GradientDrawable");
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("dialog");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("dialog");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("dialog");
            throw null;
        }
        throw new IllegalStateException("Need a window for the onboarding dialog to show");
    }
}
