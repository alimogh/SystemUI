package com.oneplus.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.tileimpl.QSTileImpl;
public class OpLottieUtils {
    private static final String TAG = "OpLottieUtils";
    private ImageView mBg;
    private boolean mClickedForAnim;
    private Context mContext;
    private QSIconView mIcon;
    private boolean mIsAnimating;
    private LottieAnimationView mLottieAnimView;
    private LottieDrawable mLottieDrawable;

    public OpLottieUtils(Context context, ImageView imageView, QSIconView qSIconView) {
        this.mContext = context;
        this.mBg = imageView;
        this.mIcon = qSIconView;
    }

    public boolean performClick() {
        if (this.mIsAnimating) {
            return true;
        }
        this.mClickedForAnim = true;
        return false;
    }

    public boolean applyLottieAnimIfNeeded(FrameLayout frameLayout, QSTile.State state, boolean z) {
        if (this.mIsAnimating || !isNeedLottie(state)) {
            return false;
        }
        this.mIsAnimating = true;
        if (this.mLottieAnimView == null) {
            LottieAnimationView lottieAnimationView = new LottieAnimationView(this.mContext);
            this.mLottieAnimView = lottieAnimationView;
            lottieAnimationView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LottieDrawable createLottieDrawable = createLottieDrawable(state, z);
            this.mLottieDrawable = createLottieDrawable;
            this.mLottieAnimView.setImageDrawable(createLottieDrawable);
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.qs_tile_background_size);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
            layoutParams.gravity = 17;
            frameLayout.addView(this.mLottieAnimView, 1, layoutParams);
        }
        LottieComposition.Factory.fromAssetFileName(this.mContext, getLottieAnimFile(state), new OnCompositionLoadedListener() { // from class: com.oneplus.util.OpLottieUtils.1
            @Override // com.airbnb.lottie.OnCompositionLoadedListener
            public void onCompositionLoaded(LottieComposition lottieComposition) {
                OpLottieUtils.this.mLottieDrawable.setComposition(lottieComposition);
                OpLottieUtils.this.mLottieDrawable.playAnimation();
            }
        });
        return true;
    }

    public boolean isNeedLottie(QSTile.State state) {
        if (state == null || TextUtils.isEmpty(state.lottiePrefix) || !this.mClickedForAnim || !isCurStateNeedLottie(state) || !isCurShapeNeedLottie(state)) {
            return false;
        }
        return true;
    }

    private LottieDrawable createLottieDrawable(final QSTile.State state, final boolean z) {
        LottieDrawable lottieDrawable = new LottieDrawable();
        lottieDrawable.removeAllAnimatorListeners();
        lottieDrawable.addAnimatorUpdateListener(new ValueAnimator.AnimatorUpdateListener(lottieDrawable) { // from class: com.oneplus.util.-$$Lambda$OpLottieUtils$KLHs-jaR4W6vEh09AMB5gG5I_xY
            public final /* synthetic */ LottieDrawable f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpLottieUtils.this.lambda$createLottieDrawable$0$OpLottieUtils(this.f$1, valueAnimator);
            }
        });
        lottieDrawable.addAnimatorListener(new Animator.AnimatorListener() { // from class: com.oneplus.util.OpLottieUtils.2
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpLottieUtils.this.mLottieAnimView.setVisibility(0);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                hideLottieView();
                OpLottieUtils.this.setBgAnimator(state).start();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                hideLottieView();
                OpLottieUtils.this.onFinish(state);
            }

            private void hideLottieView() {
                OpLottieUtils.this.mLottieAnimView.setVisibility(8);
                OpLottieUtils.this.mIcon.setIcon(state, z);
            }
        });
        return lottieDrawable;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createLottieDrawable$0 */
    public /* synthetic */ void lambda$createLottieDrawable$0$OpLottieUtils(LottieDrawable lottieDrawable, ValueAnimator valueAnimator) {
        ImageView imageView = this.mBg;
        if (imageView != null && !imageView.isShown() && lottieDrawable.isAnimating()) {
            lottieDrawable.cancelAnimation();
            lottieDrawable.setProgress(1.0f);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ValueAnimator setBgAnimator(final QSTile.State state) {
        ValueAnimator duration = ValueAnimator.ofArgb(getCurrentBgColor(), QSTileImpl.getCircleColorForState(state.state)).setDuration(0L);
        duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.util.-$$Lambda$OpLottieUtils$pyRyLRZa5U9o76ho7ClFEwqqfI8
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpLottieUtils.this.lambda$setBgAnimator$1$OpLottieUtils(valueAnimator);
            }
        });
        duration.addListener(new AnimatorListenerAdapter() { // from class: com.oneplus.util.OpLottieUtils.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                OpLottieUtils.this.onFinish(state);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                OpLottieUtils.this.onFinish(state);
            }
        });
        return duration;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setBgAnimator$1 */
    public /* synthetic */ void lambda$setBgAnimator$1$OpLottieUtils(ValueAnimator valueAnimator) {
        this.mBg.setImageTintList(ColorStateList.valueOf(((Integer) valueAnimator.getAnimatedValue()).intValue()));
    }

    private int getCurrentBgColor() {
        ImageView imageView = this.mBg;
        if (imageView == null) {
            Log.d(TAG, "getCurrentBgColor: mBg is null.");
            return 0;
        }
        ColorStateList imageTintList = imageView.getImageTintList();
        if (imageTintList != null) {
            return imageTintList.getDefaultColor();
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onFinish(QSTile.State state) {
        ImageView imageView = this.mBg;
        if (!(imageView == null || state == null)) {
            imageView.setImageTintList(ColorStateList.valueOf(QSTileImpl.getCircleColorForState(state.state)));
        }
        this.mClickedForAnim = false;
        this.mIsAnimating = false;
    }

    private String getLottieAnimFile(QSTile.State state) {
        if (!isNeedLottie(state)) {
            return null;
        }
        boolean z = ThemeColorUtils.getCurrentTheme() == 2;
        StringBuilder sb = new StringBuilder(state.lottiePrefix);
        sb.append("_");
        sb.append(getCurrentShapeString(this.mContext));
        sb.append("_");
        int i = state.state;
        if (i == 0) {
            sb.append("unavailable");
        } else if (i != 2) {
            sb.append("inactive");
        } else {
            sb.append("active");
        }
        if (z) {
            sb.append("_");
            sb.append("android");
        }
        sb.append(".json");
        return sb.toString();
    }

    private boolean isCurStateNeedLottie(QSTile.State state) {
        if (state == null) {
            return false;
        }
        int i = state.state;
        if (i != 0) {
            if (i != 2) {
                if ((state.lottieSupport & 32) != 0) {
                    return true;
                }
            } else if ((state.lottieSupport & 16) != 0) {
                return true;
            }
        } else if ((state.lottieSupport & 64) != 0) {
            return true;
        }
        return false;
    }

    private boolean isCurShapeNeedLottie(QSTile.State state) {
        if (state == null) {
            return false;
        }
        int currentShape = getCurrentShape(this.mContext);
        if (currentShape != 2) {
            if (currentShape != 3) {
                if (currentShape != 4) {
                    if ((state.lottieSupport & 1) != 0) {
                        return true;
                    }
                } else if ((state.lottieSupport & 8) != 0) {
                    return true;
                }
            } else if ((state.lottieSupport & 4) != 0) {
                return true;
            }
        } else if ((state.lottieSupport & 2) != 0) {
            return true;
        }
        return false;
    }

    private static int getCurrentShape(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "oneplus_shape", 1);
    }

    private static String getCurrentShapeString(Context context) {
        int currentShape = getCurrentShape(context);
        if (currentShape == 2) {
            return "roundedrect";
        }
        if (currentShape != 3) {
            return currentShape != 4 ? "circle" : "squircle";
        }
        return "teardrop";
    }
}
