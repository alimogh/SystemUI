package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.MathUtils;
import android.view.IRemoteAnimationFinishedCallback;
import android.view.IRemoteAnimationRunner;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationTarget;
import android.view.SyncRtSurfaceTransactionApplier;
import android.view.View;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowViewController;
import java.util.concurrent.Executor;
public class ActivityLaunchAnimator {
    private boolean mAnimationPending;
    private boolean mAnimationRunning;
    private Callback mCallback;
    private final NotificationShadeDepthController mDepthController;
    private boolean mIsLaunchForActivity;
    private final Executor mMainExecutor;
    private final NotificationListContainer mNotificationContainer;
    private final NotificationPanelViewController mNotificationPanel;
    private final NotificationShadeWindowViewController mNotificationShadeWindowViewController;
    private final Runnable mTimeoutRunnable = new Runnable() { // from class: com.android.systemui.statusbar.notification.-$$Lambda$ActivityLaunchAnimator$l5Gj6YM2XO6z1WFQpGTriWePKVk
        @Override // java.lang.Runnable
        public final void run() {
            ActivityLaunchAnimator.this.lambda$new$0$ActivityLaunchAnimator();
        }
    };
    private final float mWindowCornerRadius;

    public interface Callback {
        boolean areLaunchAnimationsEnabled();

        void onExpandAnimationFinished(boolean z);

        void onExpandAnimationTimedOut();

        void onLaunchAnimationCancelled();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$ActivityLaunchAnimator() {
        setAnimationPending(false);
        this.mCallback.onExpandAnimationTimedOut();
    }

    public ActivityLaunchAnimator(NotificationShadeWindowViewController notificationShadeWindowViewController, Callback callback, NotificationPanelViewController notificationPanelViewController, NotificationShadeDepthController notificationShadeDepthController, NotificationListContainer notificationListContainer, Executor executor) {
        this.mNotificationPanel = notificationPanelViewController;
        this.mNotificationContainer = notificationListContainer;
        this.mDepthController = notificationShadeDepthController;
        this.mNotificationShadeWindowViewController = notificationShadeWindowViewController;
        this.mCallback = callback;
        this.mMainExecutor = executor;
        this.mWindowCornerRadius = ScreenDecorationsUtils.getWindowCornerRadius(notificationShadeWindowViewController.getView().getResources());
    }

    public RemoteAnimationAdapter getLaunchAnimation(View view, boolean z) {
        if (!(view instanceof ExpandableNotificationRow) || !this.mCallback.areLaunchAnimationsEnabled() || z) {
            return null;
        }
        return new RemoteAnimationAdapter(new AnimationRunner((ExpandableNotificationRow) view), 400, 250);
    }

    public boolean isAnimationPending() {
        return this.mAnimationPending;
    }

    public void setLaunchResult(int i, boolean z) {
        this.mIsLaunchForActivity = z;
        setAnimationPending((i == 2 || i == 0) && this.mCallback.areLaunchAnimationsEnabled());
    }

    public boolean isLaunchForActivity() {
        return this.mIsLaunchForActivity;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAnimationPending(boolean z) {
        this.mAnimationPending = z;
        this.mNotificationShadeWindowViewController.setExpandAnimationPending(z);
        if (z) {
            this.mNotificationShadeWindowViewController.getView().postDelayed(this.mTimeoutRunnable, 500);
        } else {
            this.mNotificationShadeWindowViewController.getView().removeCallbacks(this.mTimeoutRunnable);
        }
    }

    public boolean isAnimationRunning() {
        return this.mAnimationRunning;
    }

    /* access modifiers changed from: package-private */
    public class AnimationRunner extends IRemoteAnimationRunner.Stub {
        private float mCornerRadius;
        private boolean mIsFullScreenLaunch = true;
        private final float mNotificationCornerRadius;
        private final ExpandAnimationParameters mParams;
        private final ExpandableNotificationRow mSourceNotification;
        private final SyncRtSurfaceTransactionApplier mSyncRtTransactionApplier;
        private final Rect mWindowCrop = new Rect();

        public AnimationRunner(ExpandableNotificationRow expandableNotificationRow) {
            this.mSourceNotification = expandableNotificationRow;
            this.mParams = new ExpandAnimationParameters();
            this.mSyncRtTransactionApplier = new SyncRtSurfaceTransactionApplier(this.mSourceNotification);
            this.mNotificationCornerRadius = Math.max(this.mSourceNotification.getCurrentTopRoundness(), this.mSourceNotification.getCurrentBottomRoundness());
        }

        public void onAnimationStart(RemoteAnimationTarget[] remoteAnimationTargetArr, RemoteAnimationTarget[] remoteAnimationTargetArr2, IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) throws RemoteException {
            ActivityLaunchAnimator.this.mMainExecutor.execute(new Runnable(remoteAnimationTargetArr, iRemoteAnimationFinishedCallback) { // from class: com.android.systemui.statusbar.notification.-$$Lambda$ActivityLaunchAnimator$AnimationRunner$sNLXzFzCbt6n0LlixbKU_lp1tVA
                public final /* synthetic */ RemoteAnimationTarget[] f$1;
                public final /* synthetic */ IRemoteAnimationFinishedCallback f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ActivityLaunchAnimator.AnimationRunner.this.lambda$onAnimationStart$0$ActivityLaunchAnimator$AnimationRunner(this.f$1, this.f$2);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onAnimationStart$0 */
        public /* synthetic */ void lambda$onAnimationStart$0$ActivityLaunchAnimator$AnimationRunner(RemoteAnimationTarget[] remoteAnimationTargetArr, final IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) {
            final RemoteAnimationTarget primaryRemoteAnimationTarget = getPrimaryRemoteAnimationTarget(remoteAnimationTargetArr);
            if (primaryRemoteAnimationTarget == null) {
                ActivityLaunchAnimator.this.setAnimationPending(false);
                invokeCallback(iRemoteAnimationFinishedCallback);
                ActivityLaunchAnimator.this.mNotificationPanel.collapse(false, 1.0f);
                return;
            }
            boolean z = true;
            setExpandAnimationRunning(true);
            if (primaryRemoteAnimationTarget.position.y != 0 || primaryRemoteAnimationTarget.sourceContainerBounds.height() < ActivityLaunchAnimator.this.mNotificationPanel.getHeight()) {
                z = false;
            }
            this.mIsFullScreenLaunch = z;
            if (!z) {
                ActivityLaunchAnimator.this.mNotificationPanel.collapseWithDuration(400);
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.mParams.startPosition = this.mSourceNotification.getLocationOnScreen();
            this.mParams.startTranslationZ = this.mSourceNotification.getTranslationZ();
            this.mParams.startClipTopAmount = this.mSourceNotification.getClipTopAmount();
            if (this.mSourceNotification.isChildInGroup()) {
                int clipTopAmount = this.mSourceNotification.getNotificationParent().getClipTopAmount();
                this.mParams.parentStartClipTopAmount = clipTopAmount;
                if (clipTopAmount != 0) {
                    float translationY = ((float) clipTopAmount) - this.mSourceNotification.getTranslationY();
                    if (translationY > 0.0f) {
                        this.mParams.startClipTopAmount = (int) Math.ceil((double) translationY);
                    }
                }
            }
            final int width = primaryRemoteAnimationTarget.sourceContainerBounds.width();
            final int max = Math.max(this.mSourceNotification.getActualHeight() - this.mSourceNotification.getClipBottomAmount(), 0);
            final int width2 = this.mSourceNotification.getWidth();
            ofFloat.setDuration(400L);
            ofFloat.setInterpolator(Interpolators.LINEAR);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.notification.ActivityLaunchAnimator.AnimationRunner.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimationRunner.this.mParams.linearProgress = valueAnimator.getAnimatedFraction();
                    float interpolation = Interpolators.FAST_OUT_SLOW_IN.getInterpolation(AnimationRunner.this.mParams.linearProgress);
                    int lerp = (int) MathUtils.lerp((float) width2, (float) width, interpolation);
                    AnimationRunner.this.mParams.left = (int) (((float) (width - lerp)) / 2.0f);
                    AnimationRunner.this.mParams.right = AnimationRunner.this.mParams.left + lerp;
                    AnimationRunner.this.mParams.top = (int) MathUtils.lerp((float) AnimationRunner.this.mParams.startPosition[1], (float) primaryRemoteAnimationTarget.position.y, interpolation);
                    ExpandAnimationParameters expandAnimationParameters = AnimationRunner.this.mParams;
                    RemoteAnimationTarget remoteAnimationTarget = primaryRemoteAnimationTarget;
                    expandAnimationParameters.bottom = (int) MathUtils.lerp((float) (AnimationRunner.this.mParams.startPosition[1] + max), (float) (remoteAnimationTarget.position.y + remoteAnimationTarget.sourceContainerBounds.bottom), interpolation);
                    AnimationRunner animationRunner = AnimationRunner.this;
                    animationRunner.mCornerRadius = MathUtils.lerp(animationRunner.mNotificationCornerRadius, ActivityLaunchAnimator.this.mWindowCornerRadius, interpolation);
                    AnimationRunner.this.applyParamsToWindow(primaryRemoteAnimationTarget);
                    AnimationRunner animationRunner2 = AnimationRunner.this;
                    animationRunner2.applyParamsToNotification(animationRunner2.mParams);
                    AnimationRunner animationRunner3 = AnimationRunner.this;
                    animationRunner3.applyParamsToNotificationShade(animationRunner3.mParams);
                }
            });
            ofFloat.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.statusbar.notification.ActivityLaunchAnimator.AnimationRunner.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    AnimationRunner.this.setExpandAnimationRunning(false);
                    AnimationRunner.this.invokeCallback(iRemoteAnimationFinishedCallback);
                }
            });
            ofFloat.start();
            ActivityLaunchAnimator.this.setAnimationPending(false);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void invokeCallback(IRemoteAnimationFinishedCallback iRemoteAnimationFinishedCallback) {
            try {
                iRemoteAnimationFinishedCallback.onAnimationFinished();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private RemoteAnimationTarget getPrimaryRemoteAnimationTarget(RemoteAnimationTarget[] remoteAnimationTargetArr) {
            for (RemoteAnimationTarget remoteAnimationTarget : remoteAnimationTargetArr) {
                if (remoteAnimationTarget.mode == 0) {
                    return remoteAnimationTarget;
                }
            }
            return null;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setExpandAnimationRunning(boolean z) {
            ActivityLaunchAnimator.this.mNotificationPanel.setLaunchingNotification(z);
            this.mSourceNotification.setExpandAnimationRunning(z);
            ActivityLaunchAnimator.this.mNotificationShadeWindowViewController.setExpandAnimationRunning(z);
            ActivityLaunchAnimator.this.mNotificationContainer.setExpandingNotification(z ? this.mSourceNotification : null);
            ActivityLaunchAnimator.this.mAnimationRunning = z;
            if (!z) {
                ActivityLaunchAnimator.this.mCallback.onExpandAnimationFinished(this.mIsFullScreenLaunch);
                applyParamsToNotification(null);
                applyParamsToNotificationShade(null);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void applyParamsToNotificationShade(ExpandAnimationParameters expandAnimationParameters) {
            ActivityLaunchAnimator.this.mNotificationContainer.applyExpandAnimationParams(expandAnimationParameters);
            ActivityLaunchAnimator.this.mNotificationPanel.applyExpandAnimationParams(expandAnimationParameters);
            ActivityLaunchAnimator.this.mDepthController.setNotificationLaunchAnimationParams(expandAnimationParameters);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void applyParamsToNotification(ExpandAnimationParameters expandAnimationParameters) {
            this.mSourceNotification.applyExpandAnimationParams(expandAnimationParameters);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void applyParamsToWindow(RemoteAnimationTarget remoteAnimationTarget) {
            Matrix matrix = new Matrix();
            matrix.postTranslate(0.0f, (float) (this.mParams.top - remoteAnimationTarget.position.y));
            Rect rect = this.mWindowCrop;
            ExpandAnimationParameters expandAnimationParameters = this.mParams;
            rect.set(expandAnimationParameters.left, 0, expandAnimationParameters.right, expandAnimationParameters.getHeight());
            this.mSyncRtTransactionApplier.scheduleApply(new SyncRtSurfaceTransactionApplier.SurfaceParams[]{new SyncRtSurfaceTransactionApplier.SurfaceParams.Builder(remoteAnimationTarget.leash).withAlpha(1.0f).withMatrix(matrix).withWindowCrop(this.mWindowCrop).withLayer(remoteAnimationTarget.prefixOrderIndex).withCornerRadius(this.mCornerRadius).withVisibility(true).build()});
        }

        public void onAnimationCancelled() throws RemoteException {
            ActivityLaunchAnimator.this.mMainExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.notification.-$$Lambda$ActivityLaunchAnimator$AnimationRunner$M-3NAwVAMqbtd1nWxQdGu3JgCNY
                @Override // java.lang.Runnable
                public final void run() {
                    ActivityLaunchAnimator.AnimationRunner.this.lambda$onAnimationCancelled$1$ActivityLaunchAnimator$AnimationRunner();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onAnimationCancelled$1 */
        public /* synthetic */ void lambda$onAnimationCancelled$1$ActivityLaunchAnimator$AnimationRunner() {
            ActivityLaunchAnimator.this.setAnimationPending(false);
            ActivityLaunchAnimator.this.mCallback.onLaunchAnimationCancelled();
        }
    }

    public static class ExpandAnimationParameters {
        int bottom;
        int left;
        public float linearProgress;
        int parentStartClipTopAmount;
        int right;
        int startClipTopAmount;
        int[] startPosition;
        float startTranslationZ;
        int top;

        public int getTop() {
            return this.top;
        }

        public int getBottom() {
            return this.bottom;
        }

        public int getWidth() {
            return this.right - this.left;
        }

        public int getHeight() {
            return this.bottom - this.top;
        }

        public int getTopChange() {
            int i = this.startClipTopAmount;
            return Math.min((this.top - this.startPosition[1]) - (((float) i) != 0.0f ? (int) MathUtils.lerp(0.0f, (float) i, Interpolators.FAST_OUT_SLOW_IN.getInterpolation(this.linearProgress)) : 0), 0);
        }

        public float getProgress() {
            return this.linearProgress;
        }

        public float getProgress(long j, long j2) {
            return MathUtils.constrain(((this.linearProgress * 400.0f) - ((float) j)) / ((float) j2), 0.0f, 1.0f);
        }

        public int getStartClipTopAmount() {
            return this.startClipTopAmount;
        }

        public int getParentStartClipTopAmount() {
            return this.parentStartClipTopAmount;
        }

        public float getStartTranslationZ() {
            return this.startTranslationZ;
        }
    }
}
