package com.oneplus.battery;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.R$styleable;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0009R$integer;
import com.android.systemui.C0010R$interpolator;
import com.android.systemui.C0015R$string;
import com.oneplus.util.OpUtils;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
public class OpCBWarpChargingView extends FrameLayout {
    static final boolean DEBUG = OpUtils.DEBUG_ONEPLUS;
    private static Resources mRes;
    private boolean isAnimationStart;
    private ImageView mArrowView;
    private boolean mAssetLoaded;
    private boolean mAssetLoading;
    private boolean mAssetReleasing;
    private Handler mBackgroundHandler;
    private ImageView mBackgroundView;
    private TextView mBatteryLevel;
    private TextView mBatteryLevelBound;
    private TextView mBatteryLevelHundredth;
    private TextView mBatteryLevelHundredthBound;
    private TextView mBatteryLevelPercent;
    private TextView mBatteryLevelPercentBound;
    private View mBatteryLevelViewContainer;
    private AnimatorSet mChargeAnimation;
    private ValueAnimator mChargingAnimP1;
    private AnimatorSet mChargingAnimSet;
    ArrayList<Bitmap> mChargingAnimationAssets;
    private OpChargingAnimationController mChargingAnimationController;
    private OpShadowView mChargingProgressBottomView;
    private OpShadowView mChargingProgressTopView;
    private TextView mChargingState;
    private int mChargingType;
    private Context mContext;
    private Handler mHandler;
    private ValueAnimator mHundredLevelAnim;
    private ValueAnimator mHundredthBoundAnim;
    private View mInfoView;
    private boolean mIsAnimationPlaying;
    private boolean mIsPaddingStartAnimation;
    private boolean mIsWarpAnimRunning;
    private boolean mKeyguardShowing;
    private float mLevelViewTransTarget;
    private int mNowPlaying;
    private int mPrevLevel;
    private long mSWarpDuration;
    private float mSWarpLevel;
    private float mSWarpLevelNext;
    ArrayList<Bitmap> mWarpAnimationAssets;
    private AnimatorSet mWarpChargingAnimSet;
    private int mWarpChargingType;

    static {
        new PathInterpolator(0.3f, 0.0f, 0.3f, 1.0f);
    }

    public OpCBWarpChargingView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mBackgroundHandler = new Handler(BackgroundThread.getHandler().getLooper());
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mChargingAnimationAssets = new ArrayList<>();
        this.mAssetLoading = false;
        this.mAssetLoaded = false;
        this.mAssetReleasing = false;
        this.mIsPaddingStartAnimation = false;
        this.mChargingType = 1;
        this.mWarpAnimationAssets = new ArrayList<>();
        this.mIsWarpAnimRunning = false;
        this.mKeyguardShowing = false;
        this.mIsAnimationPlaying = false;
        this.mNowPlaying = -1;
        this.mSWarpLevel = 0.0f;
        this.mSWarpLevelNext = 0.0f;
        this.mSWarpDuration = 0;
        this.mWarpChargingType = 0;
        this.mLevelViewTransTarget = 0.0f;
        this.mContext = context;
        mRes = context.getResources();
    }

    public OpCBWarpChargingView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public OpCBWarpChargingView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public OpCBWarpChargingView(Context context) {
        this(context, null, 0, 0);
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        refreshUI();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mBatteryLevel = (TextView) findViewById(C0008R$id.battery_level);
        this.mChargingState = (TextView) findViewById(C0008R$id.charging_state);
        OpShadowView opShadowView = (OpShadowView) findViewById(C0008R$id.charging_progress_top_view);
        this.mChargingProgressTopView = opShadowView;
        opShadowView.setShadowLayer(20.0f, 0.0f, 0.0f, this.mContext.getResources().getColor(C0004R$color.op_cb_warp_charging_info_color), this.mContext.getResources().getColor(C0004R$color.op_cb_warp_charging_info_shadow_color));
        OpShadowView opShadowView2 = (OpShadowView) findViewById(C0008R$id.charging_progress_bottom_view);
        this.mChargingProgressBottomView = opShadowView2;
        opShadowView2.setShadowLayer(20.0f, 0.0f, 0.0f, this.mContext.getResources().getColor(C0004R$color.op_cb_charging_info_color), this.mContext.getResources().getColor(C0004R$color.op_cb_charging_info_shadow_color));
        this.mArrowView = (ImageView) findViewById(C0008R$id.arrow_view);
        this.mBackgroundView = (ImageView) findViewById(C0008R$id.background_view);
        this.mInfoView = findViewById(C0008R$id.info_view);
        findViewById(C0008R$id.scrim_view);
        this.mBatteryLevelViewContainer = findViewById(C0008R$id.battery_level_container);
        this.mBatteryLevelBound = (TextView) findViewById(C0008R$id.battery_level_bound);
        this.mBatteryLevelHundredthBound = (TextView) findViewById(C0008R$id.battery_level_hundredth_bound);
        this.mBatteryLevelHundredth = (TextView) findViewById(C0008R$id.battery_level_hundredth);
        this.mBatteryLevelPercent = (TextView) findViewById(C0008R$id.battery_level_percent);
        this.mBatteryLevelPercentBound = (TextView) findViewById(C0008R$id.battery_level_percent_bound);
        refreshUI();
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
            refreshUI();
        }
    }

    private void refreshUI() {
        ImageView imageView = this.mBackgroundView;
        if (imageView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
            if (marginLayoutParams != null) {
                marginLayoutParams.width = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_background_view_width));
                marginLayoutParams.height = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_background_view_height));
                marginLayoutParams.topMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_background_view_margin_top));
            }
            this.mBackgroundView.setLayoutParams(marginLayoutParams);
        }
        View view = this.mInfoView;
        if (view != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            if (marginLayoutParams2 != null) {
                marginLayoutParams2.width = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_info_view_width));
                marginLayoutParams2.topMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_info_view_margin_top));
                marginLayoutParams2.leftMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_info_view_margin_left));
            }
            this.mInfoView.setLayoutParams(marginLayoutParams2);
        }
        TextView textView = this.mChargingState;
        if (textView != null) {
            textView.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_text_size_px_body1, 1080));
        }
        OpShadowView opShadowView = this.mChargingProgressTopView;
        if (opShadowView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) opShadowView.getLayoutParams();
            if (marginLayoutParams3 != null) {
                marginLayoutParams3.height = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_view_margin_height));
                marginLayoutParams3.topMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_view_margin_top));
                marginLayoutParams3.leftMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_view_margin));
                marginLayoutParams3.rightMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_view_margin));
            }
            this.mChargingProgressTopView.setLayoutParams(marginLayoutParams3);
        }
        OpShadowView opShadowView2 = this.mChargingProgressBottomView;
        if (opShadowView2 != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams4 = (ViewGroup.MarginLayoutParams) opShadowView2.getLayoutParams();
            if (marginLayoutParams4 != null) {
                marginLayoutParams4.height = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_bottom_view_margin_height));
                marginLayoutParams4.topMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_bottom_view_margin_top));
                marginLayoutParams4.leftMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_view_margin));
                marginLayoutParams4.rightMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_view_margin));
            }
            this.mChargingProgressBottomView.setLayoutParams(marginLayoutParams4);
        }
        ImageView imageView2 = this.mArrowView;
        if (imageView2 != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams5 = (ViewGroup.MarginLayoutParams) imageView2.getLayoutParams();
            if (marginLayoutParams5 != null) {
                marginLayoutParams5.width = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_arrow_view_width));
                marginLayoutParams5.height = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_arrow_view_height));
                marginLayoutParams5.topMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_arrow_view_margin_top));
                marginLayoutParams5.leftMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_view_margin));
                marginLayoutParams5.rightMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_view_margin));
            }
            this.mArrowView.setLayoutParams(marginLayoutParams5);
        }
        View view2 = this.mBatteryLevelViewContainer;
        if (view2 != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams6 = (ViewGroup.MarginLayoutParams) view2.getLayoutParams();
            if (marginLayoutParams6 != null) {
                marginLayoutParams6.topMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_battery_level_view_margin_top));
                marginLayoutParams6.rightMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_view_margin));
            }
            this.mBatteryLevelViewContainer.setLayoutParams(marginLayoutParams6);
        }
        TextView textView2 = this.mBatteryLevel;
        if (textView2 != null) {
            textView2.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_text_size_px_h5, 1080));
        }
        TextView textView3 = this.mBatteryLevelBound;
        if (textView3 != null) {
            textView3.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_text_size_px_h5, 1080));
        }
        TextView textView4 = this.mBatteryLevelHundredth;
        if (textView4 != null) {
            textView4.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_text_size_px_h5, 1080));
        }
        TextView textView5 = this.mBatteryLevelHundredthBound;
        if (textView5 != null) {
            textView5.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_text_size_px_h5, 1080));
        }
        TextView textView6 = this.mBatteryLevelPercent;
        if (textView6 != null) {
            textView6.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_text_size_px_h5, 1080));
        }
        TextView textView7 = this.mBatteryLevelPercentBound;
        if (textView7 != null) {
            textView7.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_text_size_px_h5, 1080));
        }
    }

    public void setChargingAnimationController(OpChargingAnimationController opChargingAnimationController) {
        this.mChargingAnimationController = opChargingAnimationController;
    }

    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        TextView textView;
        if (this.mPrevLevel != i || (textView = this.mBatteryLevel) == null || textView.getText().toString().isEmpty()) {
            this.mPrevLevel = i;
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(i);
            if (this.mBatteryLevel != null) {
                Log.d("OpCBWarpChargingView", "Update level [" + i + "]");
                this.mBatteryLevel.setText(stringBuffer.toString());
            }
            TextView textView2 = this.mBatteryLevelBound;
            if (textView2 != null) {
                textView2.setText(stringBuffer.toString());
            }
            TextView textView3 = this.mBatteryLevelHundredth;
            if (textView3 != null) {
                textView3.setText(".00");
            }
            updateBatteryInfoView(i);
            return;
        }
        Log.d("OpCBWarpChargingView", "level is same as latest one [" + i + "]");
    }

    public void onSWarpBatteryLevelChanged(float f, float f2, long j) {
        this.mSWarpLevel = f;
        this.mSWarpLevelNext = f2;
        this.mSWarpDuration = j;
        String[] split = new BigDecimal((double) f).setScale(2, 4).toString().split("\\.");
        if (this.mBatteryLevel != null) {
            Log.d("OpCBWarpChargingView", "Update Swarp level [" + f + "]");
            this.mBatteryLevel.setText(split[0]);
        }
        TextView textView = this.mBatteryLevelHundredth;
        if (textView != null) {
            textView.setText("." + split[1]);
            this.mLevelViewTransTarget = (float) this.mBatteryLevelHundredth.getWidth();
        }
        updateBatteryInfoView((int) f);
    }

    public void startAnimation(int i) {
        Handler handler;
        if (this.mChargingType != i) {
            this.mAssetLoaded = false;
        }
        if (!this.mAssetLoaded || this.mAssetReleasing) {
            if (DEBUG) {
                Log.i("OpCBWarpChargingView", "startAnimation / else / prepareAsset");
            }
            if (this.mAssetReleasing && (handler = this.mBackgroundHandler) != null) {
                handler.removeCallbacksAndMessages(null);
                this.mAssetLoaded = false;
                this.mAssetReleasing = false;
            }
            this.mIsPaddingStartAnimation = true;
            prepareAsset(i);
            return;
        }
        this.mIsPaddingStartAnimation = false;
        if (!this.isAnimationStart) {
            Log.i("OpCBWarpChargingView", "startAnimation");
            AnimatorSet animatorSet = this.mChargeAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
            } else {
                genWarpAnimation();
                this.mChargeAnimation = getChargeAnimation();
            }
            this.isAnimationStart = true;
            this.mIsAnimationPlaying = true;
            this.mChargeAnimation.start();
        }
    }

    public void stopAnimation() {
        this.mKeyguardShowing = false;
        if (this.mIsWarpAnimRunning) {
            Log.i("OpCBWarpChargingView", "stop warp animation");
            ValueAnimator valueAnimator = this.mHundredthBoundAnim;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
        }
        if (this.isAnimationStart) {
            Log.i("OpCBWarpChargingView", "stop charging animation");
            this.mIsAnimationPlaying = false;
            int i = this.mNowPlaying;
            if (i == 1) {
                this.mChargingAnimSet.cancel();
            } else if (i != 2) {
                this.mChargingAnimP1.cancel();
            } else {
                this.mWarpChargingAnimSet.cancel();
            }
        }
    }

    private AnimatorSet getChargeAnimation() {
        new ValueAnimator();
        ValueAnimator ofInt = ValueAnimator.ofInt(0, 42);
        this.mChargingAnimP1 = ofInt;
        ofInt.setDuration((long) 1376);
        this.mChargingAnimP1.setInterpolator(new LinearInterpolator());
        this.mChargingAnimP1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpCBWarpChargingView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpCBWarpChargingView.this.mBackgroundView.setImageBitmap(OpCBWarpChargingView.this.mChargingAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration((long) 576);
        ofFloat.setStartDelay((long) 800);
        ofFloat.setInterpolator(new LinearInterpolator());
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpCBWarpChargingView.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpCBWarpChargingView.this.mInfoView.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        this.mChargingAnimP1.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpCBWarpChargingView.3
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (OpCBWarpChargingView.DEBUG) {
                    Log.i("OpCBWarpChargingView", "P1 onAnimationStart()");
                }
                OpCBWarpChargingView.this.mNowPlaying = 0;
                OpCBWarpChargingView.this.mInfoView.setAlpha(0.0f);
                OpCBWarpChargingView.this.setVisibility(0);
                OpCBWarpChargingView.this.mBatteryLevelHundredth.setAlpha(0.0f);
                if (OpCBWarpChargingView.this.mChargingAnimationController != null) {
                    OpCBWarpChargingView.this.mChargingAnimationController.animationStart(R$styleable.Constraint_layout_goneMarginTop);
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (!OpCBWarpChargingView.this.mIsAnimationPlaying) {
                    OpCBWarpChargingView.this.animationEnd("chargingAnimP1");
                } else if (OpCBWarpChargingView.this.isSWarp()) {
                    OpCBWarpChargingView.this.mWarpChargingAnimSet.start();
                } else {
                    OpCBWarpChargingView.this.mChargingAnimSet.start();
                }
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(1.0f, 0.0f);
        long j = (long) 1280;
        ofFloat2.setDuration(j);
        ofFloat2.setInterpolator(new LinearInterpolator());
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpCBWarpChargingView.4
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpCBWarpChargingView.this.mInfoView.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        new ValueAnimator();
        ValueAnimator ofInt2 = ValueAnimator.ofInt(43, 145);
        long j2 = (long) 3296;
        ofInt2.setDuration(j2);
        ofInt2.setInterpolator(new LinearInterpolator());
        ofInt2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpCBWarpChargingView.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpCBWarpChargingView.this.mBackgroundView.setImageBitmap(OpCBWarpChargingView.this.mChargingAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        new ValueAnimator();
        ValueAnimator ofInt3 = ValueAnimator.ofInt(146, 186);
        ofInt3.setDuration((long) 1312);
        ofInt3.setInterpolator(new LinearInterpolator());
        ofInt3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpCBWarpChargingView.6
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpCBWarpChargingView.this.mBackgroundView.setImageBitmap(OpCBWarpChargingView.this.mChargingAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        this.mChargingAnimSet = animatorSet;
        animatorSet.play(ofInt3).with(ofFloat2).after(ofInt2);
        this.mChargingAnimSet.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpCBWarpChargingView.7
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpCBWarpChargingView.this.mNowPlaying = 1;
                if (OpCBWarpChargingView.DEBUG) {
                    Log.i("OpCBWarpChargingView", "P2 P3 set onAnimationStart()");
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                OpCBWarpChargingView.this.animationEnd("ANIM_P2_P3");
            }
        });
        new ValueAnimator();
        ValueAnimator ofInt4 = ValueAnimator.ofInt(43, 145);
        ofInt4.setDuration(j2);
        ofInt4.setInterpolator(new LinearInterpolator());
        ofInt4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(42) { // from class: com.oneplus.battery.OpCBWarpChargingView.8
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpCBWarpChargingView.this.mBackgroundView.setImageBitmap(OpCBWarpChargingView.this.mWarpAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue() - (42 + 1)));
                if (!OpCBWarpChargingView.this.mIsWarpAnimRunning && OpCBWarpChargingView.this.mSWarpLevel != 100.0f && ((Integer) valueAnimator.getAnimatedValue()).intValue() > 77) {
                    OpCBWarpChargingView opCBWarpChargingView = OpCBWarpChargingView.this;
                    opCBWarpChargingView.mLevelViewTransTarget = (float) opCBWarpChargingView.mBatteryLevelHundredth.getWidth();
                    OpCBWarpChargingView.this.mHundredthBoundAnim.start();
                    OpCBWarpChargingView.this.mHundredLevelAnim.setDuration(OpCBWarpChargingView.this.mSWarpDuration);
                    OpCBWarpChargingView.this.mHundredLevelAnim.setFloatValues(OpCBWarpChargingView.this.mSWarpLevel, OpCBWarpChargingView.this.mSWarpLevelNext);
                    Log.d("OpCBWarpChargingView", "setSwarpLevelAnimation : startLevel[" + OpCBWarpChargingView.this.mSWarpLevel + "], endLevel[" + OpCBWarpChargingView.this.mSWarpLevelNext + "]");
                    OpCBWarpChargingView.this.mHundredLevelAnim.start();
                }
            }
        });
        new ValueAnimator();
        ValueAnimator ofInt5 = ValueAnimator.ofInt(146, 185);
        ofInt5.setDuration(j);
        ofInt5.setInterpolator(new LinearInterpolator());
        ofInt5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(42) { // from class: com.oneplus.battery.OpCBWarpChargingView.9
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpCBWarpChargingView.this.mBackgroundView.setImageBitmap(OpCBWarpChargingView.this.mWarpAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue() - (42 + 1)));
            }
        });
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mWarpChargingAnimSet = animatorSet2;
        animatorSet2.play(ofInt5).with(ofFloat2).after(ofInt4);
        this.mWarpChargingAnimSet.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpCBWarpChargingView.10
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpCBWarpChargingView.this.mNowPlaying = 2;
                if (OpCBWarpChargingView.DEBUG) {
                    Log.i("OpCBWarpChargingView", "WARP P2 P3 set onAnimationStart()");
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                OpCBWarpChargingView.this.animationEnd("WARP_ANIM_P2_P3");
            }
        });
        AnimatorSet animatorSet3 = new AnimatorSet();
        animatorSet3.playTogether(this.mChargingAnimP1, ofFloat);
        return animatorSet3;
    }

    private void genWarpAnimation() {
        new ValueAnimator();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.6f, 1.0f);
        this.mHundredthBoundAnim = ofFloat;
        ofFloat.setDuration((long) mRes.getInteger(C0009R$integer.op_control_time_325));
        this.mHundredthBoundAnim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, C0010R$interpolator.op_control_interpolator_fast_out_slow_in_standard));
        this.mHundredthBoundAnim.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpCBWarpChargingView.11
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpCBWarpChargingView.this.mIsWarpAnimRunning = true;
                OpCBWarpChargingView.this.mBatteryLevelHundredth.setScaleX(0.0f);
                OpCBWarpChargingView.this.mBatteryLevelHundredth.setScaleY(0.0f);
                OpCBWarpChargingView.this.mBatteryLevelHundredth.setVisibility(0);
            }
        });
        this.mHundredthBoundAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpCBWarpChargingView.12
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float f = (floatValue - 0.6f) / 0.39999998f;
                OpCBWarpChargingView.this.mBatteryLevelHundredthBound.setWidth((int) (OpCBWarpChargingView.this.mLevelViewTransTarget * f));
                OpCBWarpChargingView.this.mBatteryLevelHundredth.setScaleX(floatValue);
                OpCBWarpChargingView.this.mBatteryLevelHundredth.setScaleY(floatValue);
                OpCBWarpChargingView.this.mBatteryLevelHundredth.setAlpha(f);
            }
        });
        ValueAnimator valueAnimator = new ValueAnimator();
        this.mHundredLevelAnim = valueAnimator;
        valueAnimator.setInterpolator(new LinearInterpolator());
        this.mHundredLevelAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpCBWarpChargingView.13
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                String[] split = new BigDecimal((double) ((Float) valueAnimator2.getAnimatedValue()).floatValue()).setScale(2, 4).toString().split("\\.");
                OpCBWarpChargingView.this.mBatteryLevel.setText(split[0]);
                TextView textView = OpCBWarpChargingView.this.mBatteryLevelHundredth;
                textView.setText("." + split[1]);
            }
        });
    }

    public void prepareAsset(int i) {
        if (DEBUG) {
            Log.i("OpCBWarpChargingView", "prepareAsset() / mAssetLoading:" + this.mAssetLoading + " / mAssetLoaded:" + this.mAssetLoaded + " chargingType " + i);
        }
        this.mChargingType = i;
        if (this.mAssetLoading || this.mAssetLoaded) {
            this.mKeyguardShowing = true;
            return;
        }
        this.mAssetLoading = true;
        this.mKeyguardShowing = true;
        AsyncTask.execute(new Runnable() { // from class: com.oneplus.battery.OpCBWarpChargingView.14
            @Override // java.lang.Runnable
            public void run() {
                OpCBWarpChargingView.this.preloadWarpAnimationList();
            }
        });
        AsyncTask.execute(new Runnable() { // from class: com.oneplus.battery.OpCBWarpChargingView.15
            @Override // java.lang.Runnable
            public void run() {
                OpCBWarpChargingView.this.preloadAnimationList();
            }
        });
    }

    public void releaseAsset() {
        if (DEBUG) {
            Log.i("OpCBWarpChargingView", "releaseAsset() / mAssetLoaded:" + this.mAssetLoaded + " / isAnimationStart:" + this.isAnimationStart + " / mAssetReleasing:" + this.mAssetReleasing);
        }
        if (this.mAssetLoaded && !this.isAnimationStart && !this.mAssetReleasing) {
            this.mAssetReleasing = true;
            this.mBackgroundHandler.post(new Runnable() { // from class: com.oneplus.battery.OpCBWarpChargingView.16
                @Override // java.lang.Runnable
                public void run() {
                    OpCBWarpChargingView.this.releaseAnimationList();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void preloadAnimationList() {
        if (DEBUG) {
            Log.i("OpCBWarpChargingView", "preloadAnimationList()");
        }
        long currentTimeMillis = System.currentTimeMillis();
        int[] animationRes = OpChargingAnimationResHelper.getAnimationRes(this.mContext, 3);
        for (int i = 0; i < this.mChargingAnimationAssets.size(); i++) {
            Bitmap bitmap = this.mChargingAnimationAssets.get(i);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        this.mChargingAnimationAssets.clear();
        for (int i2 : animationRes) {
            InputStream openRawResource = getResources().openRawResource(i2);
            this.mChargingAnimationAssets.add(BitmapFactory.decodeStream(openRawResource));
            if (openRawResource != null) {
                try {
                    openRawResource.close();
                } catch (IOException unused) {
                }
            }
        }
        long currentTimeMillis2 = System.currentTimeMillis();
        if (DEBUG) {
            Log.i("OpCBWarpChargingView", "preloadAnimationList: cost Time" + (currentTimeMillis2 - currentTimeMillis) + " mChargingAnimationAssets size:" + this.mChargingAnimationAssets.size());
        }
        this.mHandler.post(new Runnable() { // from class: com.oneplus.battery.OpCBWarpChargingView.17
            @Override // java.lang.Runnable
            public void run() {
                OpCBWarpChargingView.this.mAssetLoading = false;
                OpCBWarpChargingView.this.mAssetLoaded = true;
                Log.i("OpCBWarpChargingView", "preloadAnimationList: pre start anim keyguardShowing " + OpCBWarpChargingView.this.mKeyguardShowing);
                if (!OpCBWarpChargingView.this.mKeyguardShowing) {
                    OpCBWarpChargingView.this.releaseAsset();
                } else if (OpCBWarpChargingView.this.mIsPaddingStartAnimation) {
                    OpCBWarpChargingView opCBWarpChargingView = OpCBWarpChargingView.this;
                    opCBWarpChargingView.startAnimation(opCBWarpChargingView.mChargingType);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void preloadWarpAnimationList() {
        if (DEBUG) {
            Log.i("OpCBWarpChargingView", "preloadWarpAnimationList()");
        }
        long currentTimeMillis = System.currentTimeMillis();
        int[] animationRes = OpChargingAnimationResHelper.getAnimationRes(this.mContext, 4);
        for (int i = 0; i < this.mWarpAnimationAssets.size(); i++) {
            Bitmap bitmap = this.mWarpAnimationAssets.get(i);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        this.mWarpAnimationAssets.clear();
        for (int i2 : animationRes) {
            InputStream openRawResource = getResources().openRawResource(i2);
            this.mWarpAnimationAssets.add(BitmapFactory.decodeStream(openRawResource));
            if (openRawResource != null) {
                try {
                    openRawResource.close();
                } catch (IOException unused) {
                }
            }
        }
        long currentTimeMillis2 = System.currentTimeMillis();
        if (DEBUG) {
            Log.i("OpCBWarpChargingView", "preloadAnimationList: cost Time" + (currentTimeMillis2 - currentTimeMillis) + " mWarpAnimationAssets size:" + this.mWarpAnimationAssets.size());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseAnimationList() {
        if (DEBUG) {
            Log.i("OpCBWarpChargingView", "releaseAnimationList()");
        }
        this.mBackgroundView.setImageBitmap(null);
        for (int i = 0; i < this.mChargingAnimationAssets.size(); i++) {
            Bitmap bitmap = this.mChargingAnimationAssets.get(i);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        this.mChargingAnimationAssets.clear();
        for (int i2 = 0; i2 < this.mWarpAnimationAssets.size(); i2++) {
            Bitmap bitmap2 = this.mWarpAnimationAssets.get(i2);
            if (bitmap2 != null) {
                bitmap2.recycle();
            }
        }
        this.mWarpAnimationAssets.clear();
        this.mHandler.post(new Runnable() { // from class: com.oneplus.battery.OpCBWarpChargingView.18
            @Override // java.lang.Runnable
            public void run() {
                OpCBWarpChargingView.this.mAssetLoaded = false;
                OpCBWarpChargingView.this.mAssetReleasing = false;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void animationEnd(String str) {
        Log.i("OpCBWarpChargingView", "animationEnd from " + str);
        setVisibility(8);
        this.mInfoView.setAlpha(0.0f);
        this.mBackgroundView.setImageBitmap(null);
        int i = this.mPrevLevel;
        this.mPrevLevel = 0;
        onBatteryLevelChanged(i, false, false);
        OpChargingAnimationController opChargingAnimationController = this.mChargingAnimationController;
        if (opChargingAnimationController != null) {
            opChargingAnimationController.animationEnd(R$styleable.Constraint_layout_goneMarginTop);
        }
        this.isAnimationStart = false;
        this.mIsWarpAnimRunning = false;
        this.mWarpChargingType = 0;
        TextView textView = this.mBatteryLevelHundredthBound;
        if (textView != null) {
            textView.setWidth(0);
        }
        releaseAsset();
    }

    public void notifyWarpCharging(int i) {
        Log.d("OpCBWarpChargingView", " Notify Warp Charging : " + i);
        this.mWarpChargingType = i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSWarp() {
        return 3 == SystemProperties.getInt("persist.test.warp", this.mWarpChargingType);
    }

    public void updateBatteryInfoView(int i) {
        int i2;
        int i3;
        String str;
        if (i == 100) {
            i2 = this.mContext.getResources().getColor(C0004R$color.op_cb_charged_info_color);
        } else {
            i2 = this.mContext.getResources().getColor(C0004R$color.op_cb_charging_info_color);
        }
        if (i == 100) {
            i3 = this.mContext.getResources().getColor(C0004R$color.op_cb_charged_info_shadow_color);
        } else {
            i3 = this.mContext.getResources().getColor(C0004R$color.op_cb_charging_info_shadow_color);
        }
        if (i == 100) {
            str = this.mContext.getString(C0015R$string.op_cb_warp_charging_animation_charging_state_charged);
        } else {
            str = this.mContext.getString(C0015R$string.op_cb_warp_charging_animation_charging_state_charging);
        }
        TextView textView = this.mChargingState;
        if (textView != null) {
            textView.setTextColor(i2);
            this.mChargingState.setShadowLayer(4.0f, 2.0f, 2.0f, i3);
            this.mChargingState.setText(str);
        }
        TextView textView2 = this.mBatteryLevel;
        if (textView2 != null) {
            textView2.setTextColor(i2);
            this.mBatteryLevel.setShadowLayer(4.0f, 2.0f, 2.0f, i3);
        }
        TextView textView3 = this.mBatteryLevelHundredth;
        if (textView3 != null) {
            textView3.setTextColor(i2);
            this.mBatteryLevelHundredth.setShadowLayer(4.0f, 2.0f, 2.0f, i3);
        }
        TextView textView4 = this.mBatteryLevelPercent;
        if (textView4 != null) {
            textView4.setTextColor(i2);
            this.mBatteryLevelPercent.setShadowLayer(4.0f, 2.0f, 2.0f, i3);
        }
        int convertDpToFixedPx2 = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_info_view_width)) - (OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_cb_warp_charging_progress_view_margin)) * 2);
        OpShadowView opShadowView = this.mChargingProgressTopView;
        if (opShadowView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) opShadowView.getLayoutParams();
            if (marginLayoutParams != null) {
                marginLayoutParams.width = (convertDpToFixedPx2 * i) / 100;
            }
            this.mChargingProgressTopView.setLayoutParams(marginLayoutParams);
            this.mChargingProgressTopView.requestLayout();
        }
        OpShadowView opShadowView2 = this.mChargingProgressBottomView;
        if (opShadowView2 != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) opShadowView2.getLayoutParams();
            if (marginLayoutParams2 != null) {
                marginLayoutParams2.width = (convertDpToFixedPx2 * i) / 100;
            }
            this.mChargingProgressBottomView.setLayoutParams(marginLayoutParams2);
            this.mChargingProgressBottomView.requestLayout();
        }
    }
}
