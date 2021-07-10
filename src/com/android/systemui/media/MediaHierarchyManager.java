package com.android.systemui.media;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.MathUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.ViewOverlay;
import android.view.ViewRootImpl;
import com.android.systemui.Interpolators;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.animation.UniqueObjectHostView;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: MediaHierarchyManager.kt */
public final class MediaHierarchyManager {
    private boolean animationPending;
    private Rect animationStartBounds = new Rect();
    private ValueAnimator animator;
    private final KeyguardBypassController bypassController;
    private boolean collapsingShadeFromQS;
    private final Context context;
    private int currentAttachmentLocation;
    private Rect currentBounds = new Rect();
    private int desiredLocation;
    private boolean dozeAnimationRunning;
    private boolean fullyAwake;
    private boolean goingToSleep;
    private final KeyguardStateController keyguardStateController;
    private final MediaCarouselController mediaCarouselController;
    private final MediaHost[] mediaHosts;
    private final NotificationLockscreenUserManager notifLockscreenUserManager;
    private int previousLocation;
    private float qsExpansion;
    private ViewGroupOverlay rootOverlay;
    private View rootView;
    private final Runnable startAnimation;
    private final SysuiStatusBarStateController statusBarStateController;
    private int statusbarState = this.statusBarStateController.getState();
    private Rect targetBounds = new Rect();

    public MediaHierarchyManager(@NotNull Context context, @NotNull SysuiStatusBarStateController sysuiStatusBarStateController, @NotNull KeyguardStateController keyguardStateController, @NotNull KeyguardBypassController keyguardBypassController, @NotNull MediaCarouselController mediaCarouselController, @NotNull NotificationLockscreenUserManager notificationLockscreenUserManager, @NotNull WakefulnessLifecycle wakefulnessLifecycle) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(sysuiStatusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(keyguardStateController, "keyguardStateController");
        Intrinsics.checkParameterIsNotNull(keyguardBypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(mediaCarouselController, "mediaCarouselController");
        Intrinsics.checkParameterIsNotNull(notificationLockscreenUserManager, "notifLockscreenUserManager");
        Intrinsics.checkParameterIsNotNull(wakefulnessLifecycle, "wakefulnessLifecycle");
        this.context = context;
        this.statusBarStateController = sysuiStatusBarStateController;
        this.keyguardStateController = keyguardStateController;
        this.bypassController = keyguardBypassController;
        this.mediaCarouselController = mediaCarouselController;
        this.notifLockscreenUserManager = notificationLockscreenUserManager;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(ofFloat, this) { // from class: com.android.systemui.media.MediaHierarchyManager$$special$$inlined$apply$lambda$1
            final /* synthetic */ ValueAnimator $this_apply;
            final /* synthetic */ MediaHierarchyManager this$0;

            {
                this.$this_apply = r1;
                this.this$0 = r2;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.this$0.updateTargetState();
                MediaHierarchyManager mediaHierarchyManager = this.this$0;
                Rect unused = mediaHierarchyManager.interpolateBounds(mediaHierarchyManager.animationStartBounds, this.this$0.targetBounds, this.$this_apply.getAnimatedFraction(), this.this$0.currentBounds);
                MediaHierarchyManager mediaHierarchyManager2 = this.this$0;
                MediaHierarchyManager.applyState$default(mediaHierarchyManager2, mediaHierarchyManager2.currentBounds, false, 2, null);
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter(this) { // from class: com.android.systemui.media.MediaHierarchyManager$$special$$inlined$apply$lambda$2
            private boolean cancelled;
            final /* synthetic */ MediaHierarchyManager this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(@Nullable Animator animator) {
                this.cancelled = true;
                this.this$0.animationPending = false;
                View view = this.this$0.rootView;
                if (view != null) {
                    view.removeCallbacks(this.this$0.startAnimation);
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(@Nullable Animator animator) {
                if (!this.cancelled) {
                    this.this$0.applyTargetStateIfNotAnimating();
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(@Nullable Animator animator) {
                this.cancelled = false;
                this.this$0.animationPending = false;
            }
        });
        this.animator = ofFloat;
        this.mediaHosts = new MediaHost[3];
        this.previousLocation = -1;
        this.desiredLocation = -1;
        this.currentAttachmentLocation = -1;
        this.startAnimation = new Runnable(this) { // from class: com.android.systemui.media.MediaHierarchyManager$startAnimation$1
            final /* synthetic */ MediaHierarchyManager this$0;

            {
                this.this$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.animator.start();
            }
        };
        this.statusBarStateController.addCallback(new StatusBarStateController.StateListener(this) { // from class: com.android.systemui.media.MediaHierarchyManager.1
            final /* synthetic */ MediaHierarchyManager this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStatePreChange(int i, int i2) {
                this.this$0.statusbarState = i2;
                MediaHierarchyManager.updateDesiredLocation$default(this.this$0, false, 1, null);
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                this.this$0.updateTargetState();
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onDozeAmountChanged(float f, float f2) {
                this.this$0.setDozeAnimationRunning((f == 0.0f || f == 1.0f) ? false : true);
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onDozingChanged(boolean z) {
                if (!z) {
                    this.this$0.setDozeAnimationRunning(false);
                } else {
                    MediaHierarchyManager.updateDesiredLocation$default(this.this$0, false, 1, null);
                }
            }
        });
        wakefulnessLifecycle.addObserver(new WakefulnessLifecycle.Observer(this) { // from class: com.android.systemui.media.MediaHierarchyManager.2
            final /* synthetic */ MediaHierarchyManager this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onFinishedGoingToSleep() {
                this.this$0.setGoingToSleep(false);
            }

            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onStartedGoingToSleep() {
                this.this$0.setGoingToSleep(true);
                this.this$0.setFullyAwake(false);
            }

            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onFinishedWakingUp() {
                this.this$0.setGoingToSleep(false);
                this.this$0.setFullyAwake(true);
            }

            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onStartedWakingUp() {
                this.this$0.setGoingToSleep(false);
            }
        });
    }

    private final ViewGroup getMediaFrame() {
        return this.mediaCarouselController.getMediaFrame();
    }

    public final void setQsExpansion(float f) {
        if (this.qsExpansion != f) {
            this.qsExpansion = f;
            updateDesiredLocation$default(this, false, 1, null);
            if (getQSTransformationProgress() >= ((float) 0)) {
                updateTargetState();
                applyTargetStateIfNotAnimating();
            }
        }
    }

    public final void setCollapsingShadeFromQS(boolean z) {
        if (this.collapsingShadeFromQS != z) {
            this.collapsingShadeFromQS = z;
            updateDesiredLocation(true);
        }
    }

    private final boolean getBlockLocationChanges() {
        return this.goingToSleep || this.dozeAnimationRunning;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void setGoingToSleep(boolean z) {
        if (this.goingToSleep != z) {
            this.goingToSleep = z;
            if (!z) {
                updateDesiredLocation$default(this, false, 1, null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void setFullyAwake(boolean z) {
        if (this.fullyAwake != z) {
            this.fullyAwake = z;
            if (z) {
                updateDesiredLocation(true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void setDozeAnimationRunning(boolean z) {
        if (this.dozeAnimationRunning != z) {
            this.dozeAnimationRunning = z;
            if (!z) {
                updateDesiredLocation$default(this, false, 1, null);
            }
        }
    }

    @NotNull
    public final UniqueObjectHostView register(@NotNull MediaHost mediaHost) {
        Intrinsics.checkParameterIsNotNull(mediaHost, "mediaObject");
        UniqueObjectHostView createUniqueObjectHost = createUniqueObjectHost();
        mediaHost.setHostView(createUniqueObjectHost);
        mediaHost.addVisibilityChangeListener(new Function1<Boolean, Unit>(this) { // from class: com.android.systemui.media.MediaHierarchyManager$register$1
            final /* synthetic */ MediaHierarchyManager this$0;

            {
                this.this$0 = r1;
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Unit invoke(Boolean bool) {
                invoke(bool.booleanValue());
                return Unit.INSTANCE;
            }

            public final void invoke(boolean z) {
                this.this$0.updateDesiredLocation(true);
            }
        });
        this.mediaHosts[mediaHost.getLocation()] = mediaHost;
        if (mediaHost.getLocation() == this.desiredLocation) {
            this.desiredLocation = -1;
        }
        if (mediaHost.getLocation() == this.currentAttachmentLocation) {
            this.currentAttachmentLocation = -1;
        }
        updateDesiredLocation$default(this, false, 1, null);
        return createUniqueObjectHost;
    }

    private final UniqueObjectHostView createUniqueObjectHost() {
        UniqueObjectHostView uniqueObjectHostView = new UniqueObjectHostView(this.context);
        uniqueObjectHostView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(this, uniqueObjectHostView) { // from class: com.android.systemui.media.MediaHierarchyManager$createUniqueObjectHost$1
            final /* synthetic */ UniqueObjectHostView $viewHost;
            final /* synthetic */ MediaHierarchyManager this$0;

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(@Nullable View view) {
            }

            {
                this.this$0 = r1;
                this.$viewHost = r2;
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(@Nullable View view) {
                if (this.this$0.rootOverlay == null) {
                    MediaHierarchyManager mediaHierarchyManager = this.this$0;
                    ViewRootImpl viewRootImpl = this.$viewHost.getViewRootImpl();
                    Intrinsics.checkExpressionValueIsNotNull(viewRootImpl, "viewHost.viewRootImpl");
                    mediaHierarchyManager.rootView = viewRootImpl.getView();
                    MediaHierarchyManager mediaHierarchyManager2 = this.this$0;
                    View view2 = mediaHierarchyManager2.rootView;
                    if (view2 != null) {
                        ViewOverlay overlay = view2.getOverlay();
                        if (overlay != null) {
                            mediaHierarchyManager2.rootOverlay = (ViewGroupOverlay) overlay;
                        } else {
                            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroupOverlay");
                        }
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                }
                this.$viewHost.removeOnAttachStateChangeListener(this);
            }
        });
        return uniqueObjectHostView;
    }

    static /* synthetic */ void updateDesiredLocation$default(MediaHierarchyManager mediaHierarchyManager, boolean z, int i, Object obj) {
        if ((i & 1) != 0) {
            z = false;
        }
        mediaHierarchyManager.updateDesiredLocation(z);
    }

    /* access modifiers changed from: private */
    public final void updateDesiredLocation(boolean z) {
        int calculateLocation = calculateLocation();
        int i = this.desiredLocation;
        if (calculateLocation != i) {
            if (i >= 0) {
                this.previousLocation = i;
            }
            boolean z2 = this.desiredLocation == -1;
            this.desiredLocation = calculateLocation;
            boolean z3 = !z && shouldAnimateTransition(calculateLocation, this.previousLocation);
            Pair<Long, Long> animationParams = getAnimationParams(this.previousLocation, calculateLocation);
            this.mediaCarouselController.onDesiredLocationChanged(calculateLocation, getHost(calculateLocation), z3, animationParams.component1().longValue(), animationParams.component2().longValue());
            performTransitionToNewLocation(z2, z3);
        }
    }

    private final void performTransitionToNewLocation(boolean z, boolean z2) {
        View view;
        if (this.previousLocation < 0 || z) {
            cancelAnimationAndApplyDesiredState();
            return;
        }
        MediaHost host = getHost(this.desiredLocation);
        MediaHost host2 = getHost(this.previousLocation);
        if (host == null || host2 == null) {
            cancelAnimationAndApplyDesiredState();
            return;
        }
        updateTargetState();
        if (isCurrentlyInGuidedTransformation()) {
            applyTargetStateIfNotAnimating();
        } else if (z2) {
            this.animator.cancel();
            if (this.currentAttachmentLocation != this.previousLocation || !host2.getHostView().isAttachedToWindow()) {
                this.animationStartBounds.set(this.currentBounds);
            } else {
                this.animationStartBounds.set(host2.getCurrentBounds());
            }
            adjustAnimatorForTransition(this.desiredLocation, this.previousLocation);
            if (!this.animationPending && (view = this.rootView) != null) {
                this.animationPending = true;
                view.postOnAnimation(this.startAnimation);
            }
        } else {
            cancelAnimationAndApplyDesiredState();
        }
    }

    private final boolean shouldAnimateTransition(int i, int i2) {
        if (isCurrentlyInGuidedTransformation()) {
            return false;
        }
        if (i == 1 && i2 == 2 && (this.statusBarStateController.leaveOpenOnKeyguardHide() || this.statusbarState == 2)) {
            return true;
        }
        if (!MediaHierarchyManagerKt.isShownNotFaded(getMediaFrame())) {
            ValueAnimator valueAnimator = this.animator;
            Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "animator");
            if (!valueAnimator.isRunning() && !this.animationPending) {
                return false;
            }
        }
        return true;
    }

    private final void adjustAnimatorForTransition(int i, int i2) {
        Pair<Long, Long> animationParams = getAnimationParams(i2, i);
        long longValue = animationParams.component1().longValue();
        long longValue2 = animationParams.component2().longValue();
        ValueAnimator valueAnimator = this.animator;
        valueAnimator.setDuration(longValue);
        valueAnimator.setStartDelay(longValue2);
    }

    private final Pair<Long, Long> getAnimationParams(int i, int i2) {
        long j;
        int i3;
        long j2 = 0;
        if (i == 2 && i2 == 1) {
            if (this.statusbarState == 0 && this.keyguardStateController.isKeyguardFadingAway()) {
                j2 = this.keyguardStateController.getKeyguardFadingAwayDelay();
            }
            i3 = 448;
        } else if (i == 1 && i2 == 2) {
            i3 = 464;
        } else {
            j = 200;
            return TuplesKt.to(Long.valueOf(j), Long.valueOf(j2));
        }
        j = (long) i3;
        return TuplesKt.to(Long.valueOf(j), Long.valueOf(j2));
    }

    /* access modifiers changed from: private */
    public final void applyTargetStateIfNotAnimating() {
        ValueAnimator valueAnimator = this.animator;
        Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "animator");
        if (!valueAnimator.isRunning()) {
            applyState$default(this, this.targetBounds, false, 2, null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void updateTargetState() {
        Rect currentBounds;
        if (isCurrentlyInGuidedTransformation()) {
            float transformationProgress = getTransformationProgress();
            MediaHost host = getHost(this.desiredLocation);
            if (host != null) {
                MediaHost host2 = getHost(this.previousLocation);
                if (host2 != null) {
                    if (!host.getVisible()) {
                        host = host2;
                    } else if (!host2.getVisible()) {
                        host2 = host;
                    }
                    this.targetBounds = interpolateBounds$default(this, host2.getCurrentBounds(), host.getCurrentBounds(), transformationProgress, null, 8, null);
                    return;
                }
                Intrinsics.throwNpe();
                throw null;
            }
            Intrinsics.throwNpe();
            throw null;
        }
        MediaHost host3 = getHost(this.desiredLocation);
        if (host3 != null && (currentBounds = host3.getCurrentBounds()) != null) {
            this.targetBounds.set(currentBounds);
        }
    }

    static /* synthetic */ Rect interpolateBounds$default(MediaHierarchyManager mediaHierarchyManager, Rect rect, Rect rect2, float f, Rect rect3, int i, Object obj) {
        if ((i & 8) != 0) {
            rect3 = null;
        }
        return mediaHierarchyManager.interpolateBounds(rect, rect2, f, rect3);
    }

    /* access modifiers changed from: private */
    public final Rect interpolateBounds(Rect rect, Rect rect2, float f, Rect rect3) {
        int lerp = (int) MathUtils.lerp((float) rect.left, (float) rect2.left, f);
        int lerp2 = (int) MathUtils.lerp((float) rect.top, (float) rect2.top, f);
        int lerp3 = (int) MathUtils.lerp((float) rect.right, (float) rect2.right, f);
        int lerp4 = (int) MathUtils.lerp((float) rect.bottom, (float) rect2.bottom, f);
        if (rect3 == null) {
            rect3 = new Rect();
        }
        rect3.set(lerp, lerp2, lerp3, lerp4);
        return rect3;
    }

    private final boolean isCurrentlyInGuidedTransformation() {
        return getTransformationProgress() >= ((float) 0);
    }

    private final float getTransformationProgress() {
        float qSTransformationProgress = getQSTransformationProgress();
        if (qSTransformationProgress >= ((float) 0)) {
            return qSTransformationProgress;
        }
        return -1.0f;
    }

    private final float getQSTransformationProgress() {
        MediaHost host = getHost(this.desiredLocation);
        MediaHost host2 = getHost(this.previousLocation);
        if (host == null || host.getLocation() != 0 || host2 == null || host2.getLocation() != 1) {
            return -1.0f;
        }
        if (host2.getVisible() || this.statusbarState != 1) {
            return this.qsExpansion;
        }
        return -1.0f;
    }

    private final MediaHost getHost(int i) {
        if (i < 0) {
            return null;
        }
        return this.mediaHosts[i];
    }

    private final void cancelAnimationAndApplyDesiredState() {
        this.animator.cancel();
        MediaHost host = getHost(this.desiredLocation);
        if (host != null) {
            applyState(host.getCurrentBounds(), true);
        }
    }

    static /* synthetic */ void applyState$default(MediaHierarchyManager mediaHierarchyManager, Rect rect, boolean z, int i, Object obj) {
        if ((i & 2) != 0) {
            z = false;
        }
        mediaHierarchyManager.applyState(rect, z);
    }

    private final void applyState(Rect rect, boolean z) {
        this.currentBounds.set(rect);
        boolean isCurrentlyInGuidedTransformation = isCurrentlyInGuidedTransformation();
        this.mediaCarouselController.setCurrentState(isCurrentlyInGuidedTransformation ? this.previousLocation : -1, this.desiredLocation, isCurrentlyInGuidedTransformation ? getTransformationProgress() : 1.0f, z);
        updateHostAttachment();
        if (this.currentAttachmentLocation == -1000) {
            ViewGroup mediaFrame = getMediaFrame();
            Rect rect2 = this.currentBounds;
            mediaFrame.setLeftTopRightBottom(rect2.left, rect2.top, rect2.right, rect2.bottom);
        }
    }

    private final void updateHostAttachment() {
        int i;
        boolean z = isTransitionRunning() && this.rootOverlay != null;
        if (z) {
            i = -1000;
        } else {
            i = this.desiredLocation;
        }
        if (this.currentAttachmentLocation != i) {
            this.currentAttachmentLocation = i;
            ViewGroup viewGroup = (ViewGroup) getMediaFrame().getParent();
            if (viewGroup != null) {
                viewGroup.removeView(getMediaFrame());
            }
            MediaHost host = getHost(this.desiredLocation);
            if (host != null) {
                UniqueObjectHostView hostView = host.getHostView();
                if (z) {
                    ViewGroupOverlay viewGroupOverlay = this.rootOverlay;
                    if (viewGroupOverlay != null) {
                        viewGroupOverlay.add(getMediaFrame());
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                } else {
                    hostView.addView(getMediaFrame());
                    int paddingLeft = hostView.getPaddingLeft();
                    int paddingTop = hostView.getPaddingTop();
                    getMediaFrame().setLeftTopRightBottom(paddingLeft, paddingTop, this.currentBounds.width() + paddingLeft, this.currentBounds.height() + paddingTop);
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
    }

    private final boolean isTransitionRunning() {
        if (!isCurrentlyInGuidedTransformation() || getTransformationProgress() == 1.0f) {
            ValueAnimator valueAnimator = this.animator;
            Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "animator");
            if (!valueAnimator.isRunning() && !this.animationPending) {
                return false;
            }
        }
        return true;
    }

    private final int calculateLocation() {
        int i;
        MediaHost host;
        int i2;
        if (getBlockLocationChanges()) {
            return this.desiredLocation;
        }
        boolean z = !this.bypassController.getBypassEnabled() && ((i2 = this.statusbarState) == 1 || i2 == 3);
        boolean shouldShowLockscreenNotifications = this.notifLockscreenUserManager.shouldShowLockscreenNotifications();
        if ((this.qsExpansion <= 0.0f || z) && (this.qsExpansion <= 0.4f || !z)) {
            i = (!z || !shouldShowLockscreenNotifications) ? 1 : 2;
        } else {
            i = 0;
        }
        if (i == 2 && (((host = getHost(i)) == null || !host.getVisible()) && !this.statusBarStateController.isDozing())) {
            return 0;
        }
        if (i == 2 && this.desiredLocation == 0 && this.collapsingShadeFromQS) {
            return 0;
        }
        if (i == 2 || this.desiredLocation != 2 || this.fullyAwake) {
            return i;
        }
        return 2;
    }
}
