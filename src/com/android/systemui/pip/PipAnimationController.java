package com.android.systemui.pip;

import android.animation.AnimationHandler;
import android.animation.Animator;
import android.animation.RectEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.view.SurfaceControl;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.SfVsyncFrameCallbackProvider;
import com.android.systemui.Interpolators;
import com.android.systemui.pip.PipSurfaceTransactionHelper;
public class PipAnimationController {
    private PipTransitionAnimator mCurrentAnimator;
    private ThreadLocal<AnimationHandler> mSfAnimationHandlerThreadLocal = ThreadLocal.withInitial($$Lambda$PipAnimationController$iXb7MLu8McpFbUwX5eyjXMVFMI.INSTANCE);
    private final PipSurfaceTransactionHelper mSurfaceTransactionHelper;

    public static class PipAnimationCallback {
        public abstract void onPipAnimationCancel(PipTransitionAnimator pipTransitionAnimator);

        public abstract void onPipAnimationEnd(SurfaceControl.Transaction transaction, PipTransitionAnimator pipTransitionAnimator);

        public abstract void onPipAnimationStart(PipTransitionAnimator pipTransitionAnimator);
    }

    public static boolean isInPipDirection(int i) {
        return i == 2;
    }

    public static boolean isOutPipDirection(int i) {
        return i == 3 || i == 4;
    }

    static /* synthetic */ AnimationHandler lambda$new$0() {
        AnimationHandler animationHandler = new AnimationHandler();
        animationHandler.setProvider(new SfVsyncFrameCallbackProvider());
        return animationHandler;
    }

    PipAnimationController(Context context, PipSurfaceTransactionHelper pipSurfaceTransactionHelper) {
        this.mSurfaceTransactionHelper = pipSurfaceTransactionHelper;
    }

    public PipTransitionAnimator getAnimator(SurfaceControl surfaceControl, Rect rect, float f, float f2) {
        PipTransitionAnimator pipTransitionAnimator = this.mCurrentAnimator;
        if (pipTransitionAnimator == null) {
            PipTransitionAnimator<Float> ofAlpha = PipTransitionAnimator.ofAlpha(surfaceControl, rect, f, f2);
            setupPipTransitionAnimator(ofAlpha);
            this.mCurrentAnimator = ofAlpha;
        } else if (pipTransitionAnimator.getAnimationType() != 1 || !this.mCurrentAnimator.isRunning()) {
            this.mCurrentAnimator.cancel();
            PipTransitionAnimator<Float> ofAlpha2 = PipTransitionAnimator.ofAlpha(surfaceControl, rect, f, f2);
            setupPipTransitionAnimator(ofAlpha2);
            this.mCurrentAnimator = ofAlpha2;
        } else {
            this.mCurrentAnimator.updateEndValue(Float.valueOf(f2));
        }
        return this.mCurrentAnimator;
    }

    public PipTransitionAnimator getAnimator(SurfaceControl surfaceControl, Rect rect, Rect rect2, Rect rect3) {
        PipTransitionAnimator pipTransitionAnimator = this.mCurrentAnimator;
        if (pipTransitionAnimator == null) {
            PipTransitionAnimator<Rect> ofBounds = PipTransitionAnimator.ofBounds(surfaceControl, rect, rect2, rect3);
            setupPipTransitionAnimator(ofBounds);
            this.mCurrentAnimator = ofBounds;
        } else if (pipTransitionAnimator.getAnimationType() == 1 && this.mCurrentAnimator.isRunning()) {
            this.mCurrentAnimator.setDestinationBounds(rect2);
        } else if (this.mCurrentAnimator.getAnimationType() != 0 || !this.mCurrentAnimator.isRunning()) {
            this.mCurrentAnimator.cancel();
            PipTransitionAnimator<Rect> ofBounds2 = PipTransitionAnimator.ofBounds(surfaceControl, rect, rect2, rect3);
            setupPipTransitionAnimator(ofBounds2);
            this.mCurrentAnimator = ofBounds2;
        } else {
            this.mCurrentAnimator.setDestinationBounds(rect2);
            this.mCurrentAnimator.updateEndValue(new Rect(rect2));
        }
        return this.mCurrentAnimator;
    }

    public PipTransitionAnimator getCurrentAnimator() {
        return this.mCurrentAnimator;
    }

    private PipTransitionAnimator setupPipTransitionAnimator(PipTransitionAnimator pipTransitionAnimator) {
        pipTransitionAnimator.setSurfaceTransactionHelper(this.mSurfaceTransactionHelper);
        pipTransitionAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        pipTransitionAnimator.setFloatValues(0.0f, 1.0f);
        pipTransitionAnimator.setAnimationHandler(this.mSfAnimationHandlerThreadLocal.get());
        return pipTransitionAnimator;
    }

    public static abstract class PipTransitionAnimator<T> extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private final int mAnimationType;
        protected T mCurrentValue;
        private final Rect mDestinationBounds;
        private T mEndValue;
        private final SurfaceControl mLeash;
        private PipAnimationCallback mPipAnimationCallback;
        protected T mStartValue;
        private PipSurfaceTransactionHelper.SurfaceControlTransactionFactory mSurfaceControlTransactionFactory;
        private PipSurfaceTransactionHelper mSurfaceTransactionHelper;
        private int mTransitionDirection;

        /* access modifiers changed from: package-private */
        public abstract void applySurfaceControlTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction, float f);

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animator) {
        }

        public void onEndTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction) {
        }

        public void onStartTransaction(SurfaceControl surfaceControl, SurfaceControl.Transaction transaction) {
        }

        private PipTransitionAnimator(SurfaceControl surfaceControl, int i, Rect rect, T t, T t2) {
            Rect rect2 = new Rect();
            this.mDestinationBounds = rect2;
            this.mLeash = surfaceControl;
            this.mAnimationType = i;
            rect2.set(rect);
            this.mStartValue = t;
            this.mEndValue = t2;
            addListener(this);
            addUpdateListener(this);
            this.mSurfaceControlTransactionFactory = $$Lambda$0FLZQAxNoOm85ohJ3bgjkYQDWsU.INSTANCE;
            this.mTransitionDirection = 0;
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
            this.mCurrentValue = this.mStartValue;
            onStartTransaction(this.mLeash, newSurfaceControlTransaction());
            PipAnimationCallback pipAnimationCallback = this.mPipAnimationCallback;
            if (pipAnimationCallback != null) {
                pipAnimationCallback.onPipAnimationStart(this);
            }
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            applySurfaceControlTransaction(this.mLeash, newSurfaceControlTransaction(), valueAnimator.getAnimatedFraction());
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            this.mCurrentValue = this.mEndValue;
            SurfaceControl.Transaction newSurfaceControlTransaction = newSurfaceControlTransaction();
            onEndTransaction(this.mLeash, newSurfaceControlTransaction);
            PipAnimationCallback pipAnimationCallback = this.mPipAnimationCallback;
            if (pipAnimationCallback != null) {
                pipAnimationCallback.onPipAnimationEnd(newSurfaceControlTransaction, this);
            }
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            PipAnimationCallback pipAnimationCallback = this.mPipAnimationCallback;
            if (pipAnimationCallback != null) {
                pipAnimationCallback.onPipAnimationCancel(this);
            }
        }

        public int getAnimationType() {
            return this.mAnimationType;
        }

        public PipTransitionAnimator<T> setPipAnimationCallback(PipAnimationCallback pipAnimationCallback) {
            this.mPipAnimationCallback = pipAnimationCallback;
            return this;
        }

        public int getTransitionDirection() {
            return this.mTransitionDirection;
        }

        public PipTransitionAnimator<T> setTransitionDirection(int i) {
            if (i != 1) {
                this.mTransitionDirection = i;
            }
            return this;
        }

        public T getStartValue() {
            return this.mStartValue;
        }

        public T getEndValue() {
            return this.mEndValue;
        }

        public Rect getDestinationBounds() {
            return this.mDestinationBounds;
        }

        public void setDestinationBounds(Rect rect) {
            this.mDestinationBounds.set(rect);
            if (this.mAnimationType == 1) {
                onStartTransaction(this.mLeash, newSurfaceControlTransaction());
            }
        }

        public void setCurrentValue(T t) {
            this.mCurrentValue = t;
        }

        public boolean shouldApplyCornerRadius() {
            return !PipAnimationController.isOutPipDirection(this.mTransitionDirection);
        }

        public boolean inScaleTransition() {
            if (this.mAnimationType != 0) {
                return false;
            }
            return !PipAnimationController.isInPipDirection(getTransitionDirection());
        }

        public void updateEndValue(T t) {
            this.mEndValue = t;
        }

        public SurfaceControl.Transaction newSurfaceControlTransaction() {
            return this.mSurfaceControlTransactionFactory.getTransaction();
        }

        @VisibleForTesting
        public void setSurfaceControlTransactionFactory(PipSurfaceTransactionHelper.SurfaceControlTransactionFactory surfaceControlTransactionFactory) {
            this.mSurfaceControlTransactionFactory = surfaceControlTransactionFactory;
        }

        public PipSurfaceTransactionHelper getSurfaceTransactionHelper() {
            return this.mSurfaceTransactionHelper;
        }

        public void setSurfaceTransactionHelper(PipSurfaceTransactionHelper pipSurfaceTransactionHelper) {
            this.mSurfaceTransactionHelper = pipSurfaceTransactionHelper;
        }

        static PipTransitionAnimator<Float> ofAlpha(SurfaceControl surfaceControl, Rect rect, float f, float f2) {
            return new PipTransitionAnimator<Float>(surfaceControl, 1, rect, Float.valueOf(f), Float.valueOf(f2)) { // from class: com.android.systemui.pip.PipAnimationController.PipTransitionAnimator.1
                /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.systemui.pip.PipAnimationController$PipTransitionAnimator$1 */
                /* JADX WARN: Multi-variable type inference failed */
                @Override // com.android.systemui.pip.PipAnimationController.PipTransitionAnimator
                public void applySurfaceControlTransaction(SurfaceControl surfaceControl2, SurfaceControl.Transaction transaction, float f3) {
                    float floatValue = (((Float) getStartValue()).floatValue() * (1.0f - f3)) + (((Float) getEndValue()).floatValue() * f3);
                    setCurrentValue(Float.valueOf(floatValue));
                    getSurfaceTransactionHelper().alpha(transaction, surfaceControl2, floatValue);
                    transaction.apply();
                }

                @Override // com.android.systemui.pip.PipAnimationController.PipTransitionAnimator
                public void onStartTransaction(SurfaceControl surfaceControl2, SurfaceControl.Transaction transaction) {
                    PipSurfaceTransactionHelper surfaceTransactionHelper = getSurfaceTransactionHelper();
                    surfaceTransactionHelper.resetScale(transaction, surfaceControl2, getDestinationBounds());
                    surfaceTransactionHelper.crop(transaction, surfaceControl2, getDestinationBounds());
                    surfaceTransactionHelper.round(transaction, surfaceControl2, shouldApplyCornerRadius());
                    transaction.show(surfaceControl2);
                    transaction.apply();
                }

                public void updateEndValue(Float f3) {
                    super.updateEndValue((AnonymousClass1) f3);
                    this.mStartValue = this.mCurrentValue;
                }
            };
        }

        static PipTransitionAnimator<Rect> ofBounds(SurfaceControl surfaceControl, Rect rect, Rect rect2, Rect rect3) {
            final Rect rect4 = new Rect(rect);
            final Rect rect5 = rect3 != null ? new Rect(rect3.left - rect.left, rect3.top - rect.top, rect.right - rect3.right, rect.bottom - rect3.bottom) : null;
            final Rect rect6 = new Rect(0, 0, 0, 0);
            return new PipTransitionAnimator<Rect>(surfaceControl, 0, rect2, new Rect(rect), new Rect(rect2)) { // from class: com.android.systemui.pip.PipAnimationController.PipTransitionAnimator.2
                private final RectEvaluator mInsetsEvaluator = new RectEvaluator(new Rect());
                private final RectEvaluator mRectEvaluator = new RectEvaluator(new Rect());

                /* JADX DEBUG: Multi-variable search result rejected for r9v0, resolved type: com.android.systemui.pip.PipAnimationController$PipTransitionAnimator$2 */
                /* JADX WARN: Multi-variable type inference failed */
                @Override // com.android.systemui.pip.PipAnimationController.PipTransitionAnimator
                public void applySurfaceControlTransaction(SurfaceControl surfaceControl2, SurfaceControl.Transaction transaction, float f) {
                    Rect rect7 = (Rect) getStartValue();
                    Rect rect8 = (Rect) getEndValue();
                    Rect evaluate = this.mRectEvaluator.evaluate(f, rect7, rect8);
                    setCurrentValue(evaluate);
                    if (!inScaleTransition()) {
                        Rect rect9 = rect5;
                        if (rect9 != null) {
                            getSurfaceTransactionHelper().scaleAndCrop(transaction, surfaceControl2, rect4, evaluate, this.mInsetsEvaluator.evaluate(f, rect6, rect9));
                        } else {
                            getSurfaceTransactionHelper().scale(transaction, surfaceControl2, rect7, evaluate);
                        }
                    } else if (PipAnimationController.isOutPipDirection(getTransitionDirection())) {
                        getSurfaceTransactionHelper().scale(transaction, surfaceControl2, rect8, evaluate);
                    } else {
                        getSurfaceTransactionHelper().scale(transaction, surfaceControl2, rect7, evaluate);
                    }
                    transaction.apply();
                }

                @Override // com.android.systemui.pip.PipAnimationController.PipTransitionAnimator
                public void onStartTransaction(SurfaceControl surfaceControl2, SurfaceControl.Transaction transaction) {
                    PipSurfaceTransactionHelper surfaceTransactionHelper = getSurfaceTransactionHelper();
                    surfaceTransactionHelper.alpha(transaction, surfaceControl2, 1.0f);
                    surfaceTransactionHelper.round(transaction, surfaceControl2, shouldApplyCornerRadius());
                    transaction.show(surfaceControl2);
                    transaction.apply();
                }

                @Override // com.android.systemui.pip.PipAnimationController.PipTransitionAnimator
                public void onEndTransaction(SurfaceControl surfaceControl2, SurfaceControl.Transaction transaction) {
                    PipSurfaceTransactionHelper surfaceTransactionHelper = getSurfaceTransactionHelper();
                    surfaceTransactionHelper.resetScale(transaction, surfaceControl2, getDestinationBounds());
                    surfaceTransactionHelper.crop(transaction, surfaceControl2, getDestinationBounds());
                }

                public void updateEndValue(Rect rect7) {
                    T t;
                    super.updateEndValue((AnonymousClass2) rect7);
                    T t2 = this.mStartValue;
                    if (t2 != null && (t = this.mCurrentValue) != null) {
                        t2.set(t);
                    }
                }
            };
        }
    }
}
