package com.android.systemui.media;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Outline;
import android.util.MathUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import androidx.core.view.GestureDetectorCompat;
import com.android.settingslib.Utils;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.qs.PageIndicator;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.concurrency.DelayableExecutor;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: MediaCarouselScrollHandler.kt */
public final class MediaCarouselScrollHandler {
    private static final MediaCarouselScrollHandler$Companion$CONTENT_TRANSLATION$1 CONTENT_TRANSLATION = new MediaCarouselScrollHandler$Companion$CONTENT_TRANSLATION$1("contentTranslation");
    private int activeMediaIndex;
    private int carouselHeight;
    private int carouselWidth;
    private float contentTranslation;
    private int cornerRadius;
    private final Function0<Unit> dismissCallback;
    private final FalsingManager falsingManager;
    private boolean falsingProtectionNeeded;
    private final GestureDetectorCompat gestureDetector = new GestureDetectorCompat(this.scrollView.getContext(), this.gestureListener);
    private final MediaCarouselScrollHandler$gestureListener$1 gestureListener = new GestureDetector.SimpleOnGestureListener(this) { // from class: com.android.systemui.media.MediaCarouselScrollHandler$gestureListener$1
        final /* synthetic */ MediaCarouselScrollHandler this$0;

        /* JADX WARN: Incorrect args count in method signature: ()V */
        {
            this.this$0 = r1;
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onFling(@Nullable MotionEvent motionEvent, @Nullable MotionEvent motionEvent2, float f, float f2) {
            return this.this$0.onFling(f, f2);
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onScroll(@Nullable MotionEvent motionEvent, @Nullable MotionEvent motionEvent2, float f, float f2) {
            MediaCarouselScrollHandler mediaCarouselScrollHandler = this.this$0;
            if (motionEvent == null) {
                Intrinsics.throwNpe();
                throw null;
            } else if (motionEvent2 != null) {
                return mediaCarouselScrollHandler.onScroll(motionEvent, motionEvent2, f);
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onDown(@Nullable MotionEvent motionEvent) {
            if (!this.this$0.getFalsingProtectionNeeded()) {
                return false;
            }
            this.this$0.falsingManager.onNotificationStartDismissing();
            return false;
        }
    };
    private final DelayableExecutor mainExecutor;
    private ViewGroup mediaContent;
    private final PageIndicator pageIndicator;
    private int playerWidthPlusPadding;
    private final MediaCarouselScrollHandler$scrollChangedListener$1 scrollChangedListener = new View.OnScrollChangeListener(this) { // from class: com.android.systemui.media.MediaCarouselScrollHandler$scrollChangedListener$1
        final /* synthetic */ MediaCarouselScrollHandler this$0;

        /* JADX WARN: Incorrect args count in method signature: ()V */
        {
            this.this$0 = r1;
        }

        @Override // android.view.View.OnScrollChangeListener
        public void onScrollChange(@Nullable View view, int i, int i2, int i3, int i4) {
            if (this.this$0.getPlayerWidthPlusPadding() != 0) {
                int relativeScrollX = this.this$0.scrollView.getRelativeScrollX();
                MediaCarouselScrollHandler mediaCarouselScrollHandler = this.this$0;
                mediaCarouselScrollHandler.onMediaScrollingChanged(relativeScrollX / mediaCarouselScrollHandler.getPlayerWidthPlusPadding(), relativeScrollX % this.this$0.getPlayerWidthPlusPadding());
            }
        }
    };
    private int scrollIntoCurrentMedia;
    private final MediaScrollView scrollView;
    private View settingsButton;
    private boolean showsSettingsButton;
    private final MediaCarouselScrollHandler$touchListener$1 touchListener = new Gefingerpoken(this) { // from class: com.android.systemui.media.MediaCarouselScrollHandler$touchListener$1
        final /* synthetic */ MediaCarouselScrollHandler this$0;

        /* JADX WARN: Incorrect args count in method signature: ()V */
        {
            this.this$0 = r1;
        }

        @Override // com.android.systemui.Gefingerpoken
        public boolean onTouchEvent(@Nullable MotionEvent motionEvent) {
            MediaCarouselScrollHandler mediaCarouselScrollHandler = this.this$0;
            if (motionEvent != null) {
                return mediaCarouselScrollHandler.onTouch(motionEvent);
            }
            Intrinsics.throwNpe();
            throw null;
        }

        @Override // com.android.systemui.Gefingerpoken
        public boolean onInterceptTouchEvent(@Nullable MotionEvent motionEvent) {
            MediaCarouselScrollHandler mediaCarouselScrollHandler = this.this$0;
            if (motionEvent != null) {
                return mediaCarouselScrollHandler.onInterceptTouch(motionEvent);
            }
            Intrinsics.throwNpe();
            throw null;
        }
    };
    private Function0<Unit> translationChangedListener;

    public MediaCarouselScrollHandler(@NotNull MediaScrollView mediaScrollView, @NotNull PageIndicator pageIndicator, @NotNull DelayableExecutor delayableExecutor, @NotNull Function0<Unit> function0, @NotNull Function0<Unit> function02, @NotNull FalsingManager falsingManager) {
        Intrinsics.checkParameterIsNotNull(mediaScrollView, "scrollView");
        Intrinsics.checkParameterIsNotNull(pageIndicator, "pageIndicator");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "mainExecutor");
        Intrinsics.checkParameterIsNotNull(function0, "dismissCallback");
        Intrinsics.checkParameterIsNotNull(function02, "translationChangedListener");
        Intrinsics.checkParameterIsNotNull(falsingManager, "falsingManager");
        this.scrollView = mediaScrollView;
        this.pageIndicator = pageIndicator;
        this.mainExecutor = delayableExecutor;
        this.dismissCallback = function0;
        this.translationChangedListener = function02;
        this.falsingManager = falsingManager;
        this.scrollView.setTouchListener(this.touchListener);
        this.scrollView.setOverScrollMode(2);
        this.mediaContent = this.scrollView.getContentContainer();
        this.scrollView.setOnScrollChangeListener(this.scrollChangedListener);
        this.scrollView.setOutlineProvider(new ViewOutlineProvider(this) { // from class: com.android.systemui.media.MediaCarouselScrollHandler.1
            final /* synthetic */ MediaCarouselScrollHandler this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.view.ViewOutlineProvider
            public void getOutline(@Nullable View view, @Nullable Outline outline) {
                if (outline != null) {
                    outline.setRoundRect(0, 0, this.this$0.carouselWidth, this.this$0.carouselHeight, (float) this.this$0.cornerRadius);
                }
            }
        });
    }

    public final boolean isRtl() {
        return this.scrollView.isLayoutRtl();
    }

    public final boolean getFalsingProtectionNeeded() {
        return this.falsingProtectionNeeded;
    }

    public final void setFalsingProtectionNeeded(boolean z) {
        this.falsingProtectionNeeded = z;
    }

    public final float getContentTranslation() {
        return this.contentTranslation;
    }

    private final void setContentTranslation(float f) {
        this.contentTranslation = f;
        this.mediaContent.setTranslationX(f);
        updateSettingsPresentation();
        this.translationChangedListener.invoke();
        updateClipToOutline();
    }

    public final int getPlayerWidthPlusPadding() {
        return this.playerWidthPlusPadding;
    }

    public final void setPlayerWidthPlusPadding(int i) {
        this.playerWidthPlusPadding = i;
        int i2 = this.activeMediaIndex * i;
        int i3 = this.scrollIntoCurrentMedia;
        this.scrollView.setRelativeScrollX(i3 > i ? i2 + (i - (i3 - i)) : i2 + i3);
    }

    public final void setShowsSettingsButton(boolean z) {
        this.showsSettingsButton = z;
    }

    public final void onSettingsButtonUpdated(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "button");
        this.settingsButton = view;
        if (view != null) {
            Resources resources = view.getResources();
            View view2 = this.settingsButton;
            if (view2 != null) {
                this.cornerRadius = resources.getDimensionPixelSize(Utils.getThemeAttr(view2.getContext(), 16844145));
                updateSettingsPresentation();
                this.scrollView.invalidateOutline();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
        throw null;
    }

    private final void updateSettingsPresentation() {
        int i = 4;
        if (this.showsSettingsButton) {
            float map = MathUtils.map(0.0f, (float) getMaxTranslation(), 0.0f, 1.0f, Math.abs(this.contentTranslation));
            float f = 1.0f - map;
            View view = this.settingsButton;
            if (view != null) {
                float f2 = ((float) (-view.getWidth())) * f * 0.3f;
                if (isRtl()) {
                    if (this.contentTranslation > ((float) 0)) {
                        float width = ((float) this.scrollView.getWidth()) - f2;
                        View view2 = this.settingsButton;
                        if (view2 != null) {
                            f2 = -(width - ((float) view2.getWidth()));
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                            throw null;
                        }
                    } else {
                        f2 = -f2;
                    }
                } else if (this.contentTranslation <= ((float) 0)) {
                    float width2 = ((float) this.scrollView.getWidth()) - f2;
                    View view3 = this.settingsButton;
                    if (view3 != null) {
                        f2 = width2 - ((float) view3.getWidth());
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                        throw null;
                    }
                }
                float f3 = f * ((float) 50);
                View view4 = this.settingsButton;
                if (view4 != null) {
                    view4.setRotation(f3 * (-Math.signum(this.contentTranslation)));
                    float saturate = MathUtils.saturate(MathUtils.map(0.5f, 1.0f, 0.0f, 1.0f, map));
                    View view5 = this.settingsButton;
                    if (view5 != null) {
                        view5.setAlpha(saturate);
                        View view6 = this.settingsButton;
                        if (view6 != null) {
                            if (saturate != 0.0f) {
                                i = 0;
                            }
                            view6.setVisibility(i);
                            View view7 = this.settingsButton;
                            if (view7 != null) {
                                view7.setTranslationX(f2);
                                View view8 = this.settingsButton;
                                if (view8 != null) {
                                    int height = this.scrollView.getHeight();
                                    View view9 = this.settingsButton;
                                    if (view9 != null) {
                                        view8.setTranslationY(((float) (height - view9.getHeight())) / 2.0f);
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                                        throw null;
                                    }
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                                    throw null;
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                throw null;
            }
        } else {
            View view10 = this.settingsButton;
            if (view10 != null) {
                view10.setVisibility(4);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                throw null;
            }
        }
    }

    private final boolean onTouch(MotionEvent motionEvent) {
        float f;
        boolean z = true;
        boolean z2 = motionEvent.getAction() == 1;
        if (z2 && this.falsingProtectionNeeded) {
            this.falsingManager.onNotificationStopDismissing();
        }
        if (!this.gestureDetector.onTouchEvent(motionEvent)) {
            if (z2 || motionEvent.getAction() == 3) {
                int relativeScrollX = this.scrollView.getRelativeScrollX();
                int i = this.playerWidthPlusPadding;
                int i2 = relativeScrollX % i;
                int i3 = i2 > i / 2 ? i - i2 : i2 * -1;
                if (i3 != 0) {
                    this.mainExecutor.execute(new Runnable(this, i3) { // from class: com.android.systemui.media.MediaCarouselScrollHandler$onTouch$1
                        final /* synthetic */ int $scrollXAmount;
                        final /* synthetic */ MediaCarouselScrollHandler this$0;

                        {
                            this.this$0 = r1;
                            this.$scrollXAmount = r2;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            MediaScrollView mediaScrollView = this.this$0.scrollView;
                            boolean isRtl = this.this$0.isRtl();
                            int i4 = this.$scrollXAmount;
                            if (isRtl) {
                                i4 = -i4;
                            }
                            mediaScrollView.smoothScrollBy(i4, 0);
                        }
                    });
                }
                float contentTranslation = this.scrollView.getContentTranslation();
                if (contentTranslation != 0.0f) {
                    if (Math.abs(contentTranslation) >= ((float) (getMaxTranslation() / 2)) && !isFalseTouch()) {
                        z = false;
                    }
                    if (z) {
                        f = 0.0f;
                    } else {
                        f = ((float) getMaxTranslation()) * Math.signum(contentTranslation);
                        if (!this.showsSettingsButton) {
                            this.mainExecutor.executeDelayed(new Runnable(this) { // from class: com.android.systemui.media.MediaCarouselScrollHandler$onTouch$2
                                final /* synthetic */ MediaCarouselScrollHandler this$0;

                                {
                                    this.this$0 = r1;
                                }

                                @Override // java.lang.Runnable
                                public final void run() {
                                    this.this$0.dismissCallback.invoke();
                                }
                            }, 100);
                        }
                    }
                    PhysicsAnimator instance = PhysicsAnimator.Companion.getInstance(this);
                    instance.spring(CONTENT_TRANSLATION, f, 0.0f, MediaCarouselScrollHandlerKt.translationConfig);
                    instance.start();
                    this.scrollView.setAnimationTargetX(f);
                }
            }
            return false;
        } else if (!z2) {
            return false;
        } else {
            this.scrollView.cancelCurrentScroll();
            return true;
        }
    }

    private final boolean isFalseTouch() {
        return this.falsingProtectionNeeded && this.falsingManager.isFalseTouch();
    }

    private final int getMaxTranslation() {
        if (!this.showsSettingsButton) {
            return this.playerWidthPlusPadding;
        }
        View view = this.settingsButton;
        if (view != null) {
            return view.getWidth();
        }
        Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
        throw null;
    }

    private final boolean onInterceptTouch(MotionEvent motionEvent) {
        return this.gestureDetector.onTouchEvent(motionEvent);
    }

    public final boolean onScroll(@NotNull MotionEvent motionEvent, @NotNull MotionEvent motionEvent2, float f) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "down");
        Intrinsics.checkParameterIsNotNull(motionEvent2, "lastMotion");
        float x = motionEvent2.getX() - motionEvent.getX();
        float contentTranslation = this.scrollView.getContentTranslation();
        int i = (contentTranslation > 0.0f ? 1 : (contentTranslation == 0.0f ? 0 : -1));
        if (i == 0 && this.scrollView.canScrollHorizontally((int) (-x))) {
            return false;
        }
        float f2 = contentTranslation - f;
        float abs = Math.abs(f2);
        if (abs > ((float) getMaxTranslation()) && Math.signum(f) != Math.signum(contentTranslation)) {
            f2 = Math.abs(contentTranslation) > ((float) getMaxTranslation()) ? contentTranslation - (f * 0.2f) : Math.signum(f2) * (((float) getMaxTranslation()) + ((abs - ((float) getMaxTranslation())) * 0.2f));
        }
        if (!(Math.signum(f2) == Math.signum(contentTranslation) || i == 0 || !this.scrollView.canScrollHorizontally(-((int) f2)))) {
            f2 = 0.0f;
        }
        PhysicsAnimator instance = PhysicsAnimator.Companion.getInstance(this);
        if (instance.isRunning()) {
            instance.spring(CONTENT_TRANSLATION, f2, 0.0f, MediaCarouselScrollHandlerKt.translationConfig);
            instance.start();
        } else {
            setContentTranslation(f2);
        }
        this.scrollView.setAnimationTargetX(f2);
        return true;
    }

    private final boolean onFling(float f, float f2) {
        float f3 = f * f;
        double d = (double) f2;
        if (((double) f3) < 0.5d * d * d || f3 < ((float) 1000000)) {
            return false;
        }
        float contentTranslation = this.scrollView.getContentTranslation();
        float f4 = 0.0f;
        if (contentTranslation != 0.0f) {
            if (Math.signum(f) == Math.signum(contentTranslation) && !isFalseTouch()) {
                f4 = ((float) getMaxTranslation()) * Math.signum(contentTranslation);
                if (!this.showsSettingsButton) {
                    this.mainExecutor.executeDelayed(new Runnable(this) { // from class: com.android.systemui.media.MediaCarouselScrollHandler$onFling$1
                        final /* synthetic */ MediaCarouselScrollHandler this$0;

                        {
                            this.this$0 = r1;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            this.this$0.dismissCallback.invoke();
                        }
                    }, 100);
                }
            }
            PhysicsAnimator instance = PhysicsAnimator.Companion.getInstance(this);
            instance.spring(CONTENT_TRANSLATION, f4, f, MediaCarouselScrollHandlerKt.translationConfig);
            instance.start();
            this.scrollView.setAnimationTargetX(f4);
        } else {
            int relativeScrollX = this.scrollView.getRelativeScrollX();
            int i = this.playerWidthPlusPadding;
            int i2 = i > 0 ? relativeScrollX / i : 0;
            if (!isRtl() ? f < ((float) 0) : f > ((float) 0)) {
                i2++;
            }
            this.mainExecutor.execute(new Runnable(this, this.mediaContent.getChildAt(Math.min(this.mediaContent.getChildCount() - 1, Math.max(0, i2)))) { // from class: com.android.systemui.media.MediaCarouselScrollHandler$onFling$2
                final /* synthetic */ View $view;
                final /* synthetic */ MediaCarouselScrollHandler this$0;

                {
                    this.this$0 = r1;
                    this.$view = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    MediaScrollView mediaScrollView = this.this$0.scrollView;
                    View view = this.$view;
                    Intrinsics.checkExpressionValueIsNotNull(view, "view");
                    mediaScrollView.smoothScrollTo(view.getLeft(), this.this$0.scrollView.getScrollY());
                }
            });
        }
        return true;
    }

    public static /* synthetic */ void resetTranslation$default(MediaCarouselScrollHandler mediaCarouselScrollHandler, boolean z, int i, Object obj) {
        if ((i & 1) != 0) {
            z = false;
        }
        mediaCarouselScrollHandler.resetTranslation(z);
    }

    public final void resetTranslation(boolean z) {
        if (this.scrollView.getContentTranslation() == 0.0f) {
            return;
        }
        if (z) {
            PhysicsAnimator instance = PhysicsAnimator.Companion.getInstance(this);
            instance.spring(CONTENT_TRANSLATION, 0.0f, MediaCarouselScrollHandlerKt.translationConfig);
            instance.start();
            this.scrollView.setAnimationTargetX(0.0f);
            return;
        }
        PhysicsAnimator.Companion.getInstance(this).cancel();
        setContentTranslation(0.0f);
    }

    private final void updateClipToOutline() {
        this.scrollView.setClipToOutline((this.contentTranslation == 0.0f && this.scrollIntoCurrentMedia == 0) ? false : true);
    }

    private final void onMediaScrollingChanged(int i, int i2) {
        boolean z = false;
        boolean z2 = this.scrollIntoCurrentMedia != 0;
        this.scrollIntoCurrentMedia = i2;
        if (i2 != 0) {
            z = true;
        }
        if (!(i == this.activeMediaIndex && z2 == z)) {
            this.activeMediaIndex = i;
            updatePlayerVisibilities();
        }
        float f = (float) this.activeMediaIndex;
        int i3 = this.playerWidthPlusPadding;
        float f2 = f + (i3 > 0 ? ((float) i2) / ((float) i3) : 0.0f);
        if (isRtl()) {
            f2 = (((float) this.mediaContent.getChildCount()) - f2) - ((float) 1);
        }
        this.pageIndicator.setLocation(f2);
        updateClipToOutline();
    }

    public final void onPlayersChanged() {
        updatePlayerVisibilities();
        updateMediaPaddings();
    }

    private final void updateMediaPaddings() {
        Context context = this.scrollView.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "scrollView.context");
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(C0005R$dimen.qs_media_padding);
        int childCount = this.mediaContent.getChildCount();
        int i = 0;
        while (i < childCount) {
            View childAt = this.mediaContent.getChildAt(i);
            int i2 = i == childCount + -1 ? 0 : dimensionPixelSize;
            Intrinsics.checkExpressionValueIsNotNull(childAt, "mediaView");
            ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
            if (layoutParams != null) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                if (marginLayoutParams.getMarginEnd() != i2) {
                    marginLayoutParams.setMarginEnd(i2);
                    childAt.setLayoutParams(marginLayoutParams);
                }
                i++;
            } else {
                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
            }
        }
    }

    private final void updatePlayerVisibilities() {
        boolean z = this.scrollIntoCurrentMedia != 0;
        int childCount = this.mediaContent.getChildCount();
        int i = 0;
        while (i < childCount) {
            View childAt = this.mediaContent.getChildAt(i);
            int i2 = this.activeMediaIndex;
            boolean z2 = i == i2 || (i == i2 + 1 && z);
            Intrinsics.checkExpressionValueIsNotNull(childAt, "view");
            childAt.setVisibility(z2 ? 0 : 4);
            i++;
        }
    }

    public final void onPrePlayerRemoved(@NotNull MediaControlPanel mediaControlPanel) {
        Intrinsics.checkParameterIsNotNull(mediaControlPanel, "removed");
        ViewGroup viewGroup = this.mediaContent;
        PlayerViewHolder view = mediaControlPanel.getView();
        boolean z = true;
        boolean z2 = viewGroup.indexOfChild(view != null ? view.getPlayer() : null) <= this.activeMediaIndex;
        if (z2) {
            this.activeMediaIndex = Math.max(0, this.activeMediaIndex - 1);
        }
        if (!isRtl()) {
            z = z2;
        } else if (z2) {
            z = false;
        }
        if (z) {
            MediaScrollView mediaScrollView = this.scrollView;
            mediaScrollView.setScrollX(Math.max(mediaScrollView.getScrollX() - this.playerWidthPlusPadding, 0));
        }
    }

    public final void setCarouselBounds(int i, int i2) {
        int i3 = this.carouselHeight;
        if (i2 != i3 || i != i3) {
            this.carouselWidth = i;
            this.carouselHeight = i2;
            this.scrollView.invalidateOutline();
        }
    }

    public final void scrollToStart() {
        this.scrollView.setRelativeScrollX(0);
    }
}
