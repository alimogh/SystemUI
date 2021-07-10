package com.oneplus.battery;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.R$styleable;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.C0001R$array;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.oneplus.util.OpUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
public class OpWarpChargingView extends FrameLayout {
    private static final Interpolator ANIMATION_INTERPILATOR = new PathInterpolator(0.4f, 0.0f, 0.6f, 1.0f);
    static final boolean DEBUG = OpUtils.DEBUG_ONEPLUS;
    private static Resources mRes;
    private boolean isAnimationStart;
    private boolean mAssetLoaded;
    private boolean mAssetLoading;
    private boolean mAssetReleasing;
    private Handler mBackgroundHandler;
    private ImageView mBackgroundView;
    private TextView mBatteryLevel;
    private AnimatorSet mChargeAnimation;
    private OpChargingAnimationController mChargingAnimationController;
    private Context mContext;
    private Handler mHandler;
    private View mInfoView;
    private boolean mIsPaddingStartAnimation;
    private int mPrevLevel;
    private View mScrim;
    ArrayList<Bitmap> mStartAnimationAssets1;
    private ImageView mWrapview;

    private void refresh() {
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
    }

    public OpWarpChargingView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mBackgroundHandler = new Handler(BackgroundThread.getHandler().getLooper());
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mStartAnimationAssets1 = new ArrayList<>();
        this.mAssetLoading = false;
        this.mAssetLoaded = false;
        this.mAssetReleasing = false;
        this.mIsPaddingStartAnimation = false;
        this.mContext = context;
        mRes = context.getResources();
    }

    public OpWarpChargingView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public OpWarpChargingView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public OpWarpChargingView(Context context) {
        this(context, null, 0, 0);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mBatteryLevel = (TextView) findViewById(C0008R$id.battery_level);
        this.mWrapview = (ImageView) findViewById(C0008R$id.wrap_view);
        this.mBackgroundView = (ImageView) findViewById(C0008R$id.background_view);
        this.mInfoView = findViewById(C0008R$id.info_view);
        this.mScrim = findViewById(C0008R$id.scrim_view);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (i == 0) {
            refresh();
        }
    }

    public void setChargingAnimationController(OpChargingAnimationController opChargingAnimationController) {
        this.mChargingAnimationController = opChargingAnimationController;
    }

    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        if (this.mPrevLevel != i) {
            this.mPrevLevel = i;
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(i);
            stringBuffer.append("%");
            TextView textView = this.mBatteryLevel;
            if (textView != null) {
                textView.setText(stringBuffer.toString());
            }
        }
    }

    public void startAnimation() {
        Handler handler;
        if (!this.mAssetLoaded || this.mAssetReleasing) {
            if (DEBUG) {
                Log.i("OpWarpChargingView", "startAnimation / else / prepareAsset");
            }
            if (this.mAssetReleasing && (handler = this.mBackgroundHandler) != null) {
                handler.removeCallbacksAndMessages(null);
            }
            this.mIsPaddingStartAnimation = true;
            prepareAsset();
            return;
        }
        this.mIsPaddingStartAnimation = false;
        if (!this.isAnimationStart) {
            Log.i("OpWarpChargingView", "startAnimation");
            this.isAnimationStart = true;
            AnimatorSet animatorSet = this.mChargeAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
            } else {
                this.mChargeAnimation = getWarpFastChargeAnimation();
            }
            this.mChargeAnimation.start();
        }
    }

    public void stopAnimation() {
        if (this.isAnimationStart) {
            Log.i("OpWarpChargingView", "stopAnimation");
            AnimatorSet animatorSet = this.mChargeAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
        }
    }

    private AnimatorSet getWarpFastChargeAnimation() {
        new ValueAnimator();
        ValueAnimator ofInt = ValueAnimator.ofInt(0, this.mStartAnimationAssets1.size() - 1);
        ofInt.setDuration((long) 432);
        ofInt.setInterpolator(ANIMATION_INTERPILATOR);
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpWarpChargingView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpWarpChargingView.this.mBackgroundView.setImageBitmap(OpWarpChargingView.this.mStartAnimationAssets1.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        ofInt.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpWarpChargingView.2
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                OpWarpChargingView.this.mBackgroundView.setImageBitmap(null);
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.05f);
        ofFloat.setDuration((long) 144);
        ofFloat.setInterpolator(ANIMATION_INTERPILATOR);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpWarpChargingView.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpWarpChargingView.this.mInfoView.setScaleY(floatValue);
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(1.05f, 1.0f);
        ofFloat2.setDuration((long) 560);
        ofFloat2.setInterpolator(ANIMATION_INTERPILATOR);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpWarpChargingView.4
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpWarpChargingView.this.mInfoView.setScaleY(floatValue);
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(1.0f, 0.0f);
        ofFloat3.setStartDelay(2000);
        ofFloat3.setDuration((long) 255);
        ofFloat3.setInterpolator(ANIMATION_INTERPILATOR);
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpWarpChargingView.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpWarpChargingView.this.mInfoView.setScaleY(floatValue);
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpWarpChargingView.6
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (OpWarpChargingView.DEBUG) {
                    Log.i("OpWarpChargingView", "onAnimationStart()");
                }
                OpWarpChargingView.this.mInfoView.setScaleX(0.0f);
                OpWarpChargingView.this.mInfoView.setScaleY(0.0f);
                OpWarpChargingView.this.setVisibility(0);
                if (OpWarpChargingView.this.mChargingAnimationController != null) {
                    OpWarpChargingView.this.mChargingAnimationController.animationStart(R$styleable.Constraint_layout_goneMarginTop);
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (OpWarpChargingView.DEBUG) {
                    Log.i("OpWarpChargingView", "onAnimationEnd()");
                }
                OpWarpChargingView.this.setVisibility(8);
                OpWarpChargingView.this.mInfoView.setScaleX(0.0f);
                OpWarpChargingView.this.mInfoView.setScaleY(0.0f);
                OpWarpChargingView.this.mBackgroundView.setImageBitmap(null);
                if (OpWarpChargingView.this.mChargingAnimationController != null) {
                    OpWarpChargingView.this.mChargingAnimationController.animationEnd(R$styleable.Constraint_layout_goneMarginTop);
                }
                OpWarpChargingView.this.isAnimationStart = false;
                OpWarpChargingView.this.releaseAsset();
            }
        });
        animatorSet.play(ofInt).before(ofFloat);
        animatorSet.play(ofFloat2).after(ofFloat);
        animatorSet.play(ofFloat3).after(ofFloat2);
        return animatorSet;
    }

    public void prepareAsset() {
        if (DEBUG) {
            Log.i("OpWarpChargingView", "prepareAsset() / mAssetLoading:" + this.mAssetLoading + " / mAssetLoaded:" + this.mAssetLoaded);
        }
        if (!this.mAssetLoading && !this.mAssetLoaded) {
            this.mAssetLoading = true;
            View view = this.mInfoView;
            if (view != null) {
                view.setBackgroundResource(C0006R$drawable.fast_charging_background);
            }
            this.mBackgroundHandler.post(new Runnable() { // from class: com.oneplus.battery.OpWarpChargingView.7
                @Override // java.lang.Runnable
                public void run() {
                    OpWarpChargingView.this.preloadAnimationList();
                }
            });
        }
    }

    public void releaseAsset() {
        if (DEBUG) {
            Log.i("OpWarpChargingView", "releaseAsset() / mAssetLoaded:" + this.mAssetLoaded + " / isAnimationStart:" + this.isAnimationStart + " / mAssetReleasing:" + this.mAssetReleasing);
        }
        if (this.mAssetLoaded && !this.isAnimationStart && !this.mAssetReleasing) {
            this.mAssetReleasing = true;
            View view = this.mInfoView;
            if (view != null) {
                view.setBackgroundResource(0);
            }
            this.mBackgroundHandler.post(new Runnable() { // from class: com.oneplus.battery.OpWarpChargingView.8
                @Override // java.lang.Runnable
                public void run() {
                    OpWarpChargingView.this.releaseAnimationList();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void preloadAnimationList() {
        if (DEBUG) {
            Log.i("OpWarpChargingView", "preloadAnimationList()");
        }
        long currentTimeMillis = System.currentTimeMillis();
        TypedArray obtainTypedArray = mRes.obtainTypedArray(C0001R$array.fast_charging_start_animation1);
        for (int i = 0; i < this.mStartAnimationAssets1.size(); i++) {
            Bitmap bitmap = this.mStartAnimationAssets1.get(i);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        this.mStartAnimationAssets1.clear();
        for (int i2 = 0; i2 < obtainTypedArray.length(); i2++) {
            InputStream openRawResource = getResources().openRawResource(obtainTypedArray.getResourceId(i2, 0));
            this.mStartAnimationAssets1.add(BitmapFactory.decodeStream(openRawResource));
            if (openRawResource != null) {
                try {
                    openRawResource.close();
                } catch (IOException unused) {
                }
            }
        }
        obtainTypedArray.recycle();
        long currentTimeMillis2 = System.currentTimeMillis();
        if (DEBUG) {
            Log.i("OpWarpChargingView", "preloadAnimationList: cost Time" + (currentTimeMillis2 - currentTimeMillis) + " mStartAnimationAssets1 size:" + this.mStartAnimationAssets1.size());
        }
        this.mHandler.post(new Runnable() { // from class: com.oneplus.battery.OpWarpChargingView.9
            @Override // java.lang.Runnable
            public void run() {
                OpWarpChargingView.this.mAssetLoading = false;
                OpWarpChargingView.this.mAssetLoaded = true;
                if (OpWarpChargingView.this.mIsPaddingStartAnimation) {
                    OpWarpChargingView.this.startAnimation();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseAnimationList() {
        if (DEBUG) {
            Log.i("OpWarpChargingView", "releaseAnimationList()");
        }
        this.mBackgroundView.setImageBitmap(null);
        for (int i = 0; i < this.mStartAnimationAssets1.size(); i++) {
            Bitmap bitmap = this.mStartAnimationAssets1.get(i);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        this.mStartAnimationAssets1.clear();
        this.mHandler.post(new Runnable() { // from class: com.oneplus.battery.OpWarpChargingView.10
            @Override // java.lang.Runnable
            public void run() {
                OpWarpChargingView.this.mAssetLoaded = false;
                OpWarpChargingView.this.mAssetReleasing = false;
            }
        });
    }

    public void updaetScrimColor(int i) {
        View view = this.mScrim;
        if (view != null) {
            view.setBackgroundColor(i);
        }
    }

    public void updateColors(int i) {
        TextView textView = this.mBatteryLevel;
        if (textView != null) {
            textView.setTextColor(i);
        }
        ImageView imageView = this.mWrapview;
        if (imageView != null) {
            imageView.setColorFilter(i);
        }
    }
}
