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
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.R$styleable;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.C0001R$array;
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
public class OpSWarpChargingView extends FrameLayout {
    private static final Interpolator ANIMATION_INTERPILATOR = new PathInterpolator(0.3f, 0.0f, 0.3f, 1.0f);
    static final boolean DEBUG = OpUtils.DEBUG_ONEPLUS;
    private static Resources mRes;
    private boolean isAnimationStart;
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
    private LinearLayout mBatteryLevelViewContainer;
    private AnimatorSet mChargeAnimation;
    private ValueAnimator mChargingAnimP1;
    private ValueAnimator mChargingAnimP3;
    private ValueAnimator mChargingAnimWarpP3;
    ArrayList<Bitmap> mChargingAnimationAssets;
    private OpChargingAnimationController mChargingAnimationController;
    ArrayList<Bitmap> mChargingAnimationSWarpAssets;
    private AnimatorSet mChargingP4AnimSet;
    private AnimatorSet mChargingScaleUpAnimSet;
    private int mChargingType;
    private Context mContext;
    private Handler mHandler;
    private ValueAnimator mHundredLevelAnim;
    private ValueAnimator mHundredthBoundAnim;
    private ValueAnimator mHundredthTranslateUpAnim;
    private View mInfoView;
    private RelativeLayout mInfoViewContainer;
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
    private AnimatorSet mWarpAnimSet;
    private int mWarpChargingType;
    private TextView mWarpView;
    private ValueAnimator mWarpZoomInAnim;

    static {
        new PathInterpolator(0.4f, 0.0f, 0.3f, 1.0f);
    }

    public OpSWarpChargingView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mBackgroundHandler = new Handler(BackgroundThread.getHandler().getLooper());
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mChargingAnimationAssets = new ArrayList<>();
        this.mChargingAnimationSWarpAssets = new ArrayList<>();
        this.mAssetLoading = false;
        this.mAssetLoaded = false;
        this.mAssetReleasing = false;
        this.mIsPaddingStartAnimation = false;
        this.mChargingType = 0;
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

    public OpSWarpChargingView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public OpSWarpChargingView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public OpSWarpChargingView(Context context) {
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
        this.mInfoViewContainer = (RelativeLayout) findViewById(C0008R$id.info_view_container);
        Display defaultDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getRealMetrics(displayMetrics);
        int i = displayMetrics.widthPixels;
        int i2 = displayMetrics.heightPixels;
        if (i2 < i) {
            i = i2;
        }
        ViewGroup.LayoutParams layoutParams = this.mInfoViewContainer.getLayoutParams();
        layoutParams.width = i;
        layoutParams.height = i;
        this.mBatteryLevel = (TextView) findViewById(C0008R$id.battery_level);
        this.mWarpView = (TextView) findViewById(C0008R$id.warp_view);
        this.mBackgroundView = (ImageView) findViewById(C0008R$id.background_view);
        this.mInfoView = findViewById(C0008R$id.info_view);
        findViewById(C0008R$id.scrim_view);
        this.mBatteryLevelViewContainer = (LinearLayout) findViewById(C0008R$id.battery_level_container);
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
        RelativeLayout relativeLayout = this.mInfoViewContainer;
        if (relativeLayout != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) relativeLayout.getLayoutParams();
            marginLayoutParams.topMargin = OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_swarp_charging_anim_view_margin_top));
            this.mInfoViewContainer.setLayoutParams(marginLayoutParams);
        }
        TextView textView = this.mBatteryLevelPercent;
        if (textView != null) {
            textView.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_text_size_px_h5, 1080));
            this.mBatteryLevelPercent.setPadding(OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_control_margin_space2)), 0, 0, 0);
        }
        TextView textView2 = this.mBatteryLevelPercentBound;
        if (textView2 != null) {
            textView2.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_text_size_px_h5, 1080));
            this.mBatteryLevelPercentBound.setPadding(OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_control_margin_space2)), 0, 0, 0);
        }
        TextView textView3 = this.mBatteryLevel;
        if (textView3 != null) {
            textView3.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_swarp_charging_anim_info_battery_level_text_size, 1080));
            this.mBatteryLevel.setPadding(0, 0, OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_control_margin_space2)), 0);
        }
        TextView textView4 = this.mBatteryLevelBound;
        if (textView4 != null) {
            textView4.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_swarp_charging_anim_info_battery_level_text_size, 1080));
            this.mBatteryLevelBound.setPadding(0, 0, OpUtils.convertDpToFixedPx2(mRes.getDimension(C0005R$dimen.op_control_margin_space2)), 0);
        }
        TextView textView5 = this.mBatteryLevelHundredth;
        if (textView5 != null) {
            textView5.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_swarp_charging_anim_info_battery_level_text_size, 1080));
        }
        TextView textView6 = this.mBatteryLevelHundredthBound;
        if (textView6 != null) {
            textView6.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_swarp_charging_anim_info_battery_level_text_size, 1080));
        }
        TextView textView7 = this.mWarpView;
        if (textView7 != null) {
            textView7.setTextSize(0, (float) OpUtils.getDimensionPixelSize(mRes, C0005R$dimen.op_swarp_charging_anim_info_warp_text_size, 1080));
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
                Log.d("OpSWarpChargingView", "Update level [" + i + "]");
                this.mBatteryLevel.setText(stringBuffer.toString());
            }
            TextView textView2 = this.mBatteryLevelBound;
            if (textView2 != null) {
                textView2.setText(stringBuffer.toString());
                return;
            }
            return;
        }
        Log.d("OpSWarpChargingView", "level is same as latest one [" + i + "]");
    }

    public void onSWarpBatteryLevelChanged(float f, float f2, long j) {
        this.mSWarpLevel = f;
        this.mSWarpLevelNext = f2;
        this.mSWarpDuration = j;
    }

    public void startAnimation(int i) {
        Handler handler;
        if (this.mChargingType != i) {
            this.mAssetLoaded = false;
        }
        if (!this.mAssetLoaded || this.mAssetReleasing) {
            if (DEBUG) {
                Log.i("OpSWarpChargingView", "startAnimation / else / prepareAsset");
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
            Log.i("OpSWarpChargingView", "startAnimation");
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
            Log.i("OpSWarpChargingView", "stop warp animation");
            AnimatorSet animatorSet = this.mWarpAnimSet;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
        }
        if (this.isAnimationStart) {
            Log.i("OpSWarpChargingView", "stop charging animation");
            this.mIsAnimationPlaying = false;
            int i = this.mNowPlaying;
            if (i == 1) {
                this.mChargingScaleUpAnimSet.cancel();
            } else if (i != 2) {
                if (i != 3) {
                    this.mChargingAnimP1.cancel();
                } else {
                    this.mChargingP4AnimSet.cancel();
                }
            } else if (isSWarp()) {
                this.mChargingAnimWarpP3.cancel();
            } else {
                this.mChargingAnimP3.cancel();
            }
        }
    }

    private AnimatorSet getChargeAnimation() {
        new ValueAnimator();
        final ValueAnimator ofInt = ValueAnimator.ofInt(90, 107);
        ofInt.setInterpolator(new LinearInterpolator());
        ofInt.setDuration((long) 608);
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpSWarpChargingView.this.mBackgroundView.setImageBitmap(OpSWarpChargingView.this.mChargingAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 1.05f);
        ofFloat.setDuration((long) 255);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpSWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpSWarpChargingView.this.mInfoView.setScaleY(floatValue);
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(1.05f, 0.8f);
        long j = (long) 320;
        ofFloat2.setDuration(j);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpSWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpSWarpChargingView.this.mInfoView.setScaleY(floatValue);
                OpSWarpChargingView.this.mInfoView.setAlpha(1.0f - ((1.05f - floatValue) / 0.24999994f));
            }
        });
        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(ANIMATION_INTERPILATOR);
        animatorSet.play(ofFloat).before(ofFloat2);
        new ValueAnimator();
        final ValueAnimator ofInt2 = ValueAnimator.ofInt(106, 117);
        ofInt2.setInterpolator(new LinearInterpolator());
        ofInt2.setDuration((long) 416);
        ofInt2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.4
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpSWarpChargingView.this.mBackgroundView.setImageBitmap(OpSWarpChargingView.this.mChargingAnimationSWarpAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue() - 30));
            }
        });
        new ValueAnimator();
        final ValueAnimator ofFloat3 = ValueAnimator.ofFloat(1.0f, 1.2f);
        ofFloat3.setDuration((long) mRes.getInteger(C0009R$integer.op_control_time_225));
        ofFloat3.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, C0010R$interpolator.op_control_interpolator_fast_out_slow_in_standard));
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpSWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpSWarpChargingView.this.mInfoView.setScaleY(floatValue);
                OpSWarpChargingView.this.mInfoView.setAlpha(1.0f - ((1.0f - floatValue) / -0.1500001f));
            }
        });
        new ValueAnimator();
        ValueAnimator ofInt3 = ValueAnimator.ofInt(30, 89);
        this.mChargingAnimP3 = ofInt3;
        ofInt3.setInterpolator(new LinearInterpolator());
        long j2 = (long) 2464;
        this.mChargingAnimP3.setDuration(j2);
        this.mChargingAnimP3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.6
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpSWarpChargingView.this.mBackgroundView.setImageBitmap(OpSWarpChargingView.this.mChargingAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        this.mChargingAnimP3.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpSWarpChargingView.7
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpSWarpChargingView.this.mNowPlaying = 2;
                if (OpSWarpChargingView.DEBUG) {
                    Log.i("OpSWarpChargingView", "P3 onAnimationStart()");
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                Log.i("OpSWarpChargingView", "P3 onAnimationEnd() mIsAnimationPlaying " + OpSWarpChargingView.this.mIsAnimationPlaying);
                if (OpSWarpChargingView.this.mIsAnimationPlaying) {
                    OpSWarpChargingView.this.mChargingP4AnimSet = new AnimatorSet();
                    OpSWarpChargingView.this.mChargingP4AnimSet.playTogether(ofInt, animatorSet);
                    OpSWarpChargingView.this.mChargingP4AnimSet.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpSWarpChargingView.7.1
                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationCancel(Animator animator2) {
                        }

                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationRepeat(Animator animator2) {
                        }

                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationStart(Animator animator2) {
                            OpSWarpChargingView.this.mNowPlaying = 3;
                            if (OpSWarpChargingView.DEBUG) {
                                Log.i("OpSWarpChargingView", "P4 onAnimationStart()");
                            }
                        }

                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationEnd(Animator animator2) {
                            OpSWarpChargingView.this.animationEnd("chargingScaleDownAnimSet");
                        }
                    });
                    OpSWarpChargingView.this.mChargingP4AnimSet.start();
                } else if (!OpSWarpChargingView.this.mIsAnimationPlaying) {
                    OpSWarpChargingView.this.animationEnd("chargingAnimP3");
                }
            }
        });
        new ValueAnimator();
        ValueAnimator ofInt4 = ValueAnimator.ofInt(30, 105);
        this.mChargingAnimWarpP3 = ofInt4;
        ofInt4.setInterpolator(new LinearInterpolator());
        this.mChargingAnimWarpP3.setDuration(j2);
        this.mChargingAnimWarpP3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.8
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpSWarpChargingView.this.mBackgroundView.setImageBitmap(OpSWarpChargingView.this.mChargingAnimationSWarpAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue() - 30));
                if (OpSWarpChargingView.this.isWarp() && !OpSWarpChargingView.this.mIsWarpAnimRunning) {
                    OpSWarpChargingView.this.mWarpAnimSet = new AnimatorSet();
                    OpSWarpChargingView.this.mWarpAnimSet.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpSWarpChargingView.8.1
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
                            OpSWarpChargingView.this.mIsWarpAnimRunning = true;
                            OpSWarpChargingView.this.mWarpView.setAlpha(0.0f);
                        }
                    });
                    if (OpSWarpChargingView.this.isSWarp()) {
                        OpSWarpChargingView.this.mWarpView.setText(C0015R$string.op_swarp_animation_logo);
                    } else {
                        OpSWarpChargingView.this.mWarpView.setText(C0015R$string.op_warp_animation_logo);
                    }
                    if (OpSWarpChargingView.this.mSWarpLevel == 100.0f || !OpSWarpChargingView.this.isSWarp()) {
                        OpSWarpChargingView.this.mWarpAnimSet.play(OpSWarpChargingView.this.mHundredthTranslateUpAnim).with(OpSWarpChargingView.this.mWarpZoomInAnim);
                    } else {
                        OpSWarpChargingView.this.mHundredLevelAnim.setDuration(OpSWarpChargingView.this.mSWarpDuration);
                        OpSWarpChargingView.this.mHundredLevelAnim.setFloatValues(OpSWarpChargingView.this.mSWarpLevel, OpSWarpChargingView.this.mSWarpLevelNext);
                        Log.d("OpSWarpChargingView", "setSwarpAnimation : startLevel[" + OpSWarpChargingView.this.mSWarpLevel + "], endLevel[" + OpSWarpChargingView.this.mSWarpLevelNext + "]");
                        OpSWarpChargingView.this.mWarpAnimSet.play(OpSWarpChargingView.this.mHundredthTranslateUpAnim).with(OpSWarpChargingView.this.mHundredthBoundAnim).with(OpSWarpChargingView.this.mWarpZoomInAnim).before(OpSWarpChargingView.this.mHundredLevelAnim);
                    }
                    OpSWarpChargingView.this.mWarpAnimSet.start();
                }
            }
        });
        this.mChargingAnimWarpP3.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpSWarpChargingView.9
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpSWarpChargingView.this.mNowPlaying = 2;
                if (OpSWarpChargingView.DEBUG) {
                    Log.i("OpSWarpChargingView", "P3 onAnimationStart()");
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                Log.i("OpSWarpChargingView", "P3 onAnimationEnd() mIsWarpAnimRunning " + OpSWarpChargingView.this.mIsWarpAnimRunning + " mIsAnimationPlaying " + OpSWarpChargingView.this.mIsAnimationPlaying);
                if (OpSWarpChargingView.this.mIsAnimationPlaying) {
                    OpSWarpChargingView.this.mChargingP4AnimSet = new AnimatorSet();
                    OpSWarpChargingView.this.mChargingP4AnimSet.playTogether(ofInt2, ofFloat3);
                    OpSWarpChargingView.this.mChargingP4AnimSet.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpSWarpChargingView.9.1
                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationCancel(Animator animator2) {
                        }

                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationRepeat(Animator animator2) {
                        }

                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationStart(Animator animator2) {
                            OpSWarpChargingView.this.mNowPlaying = 3;
                            if (OpSWarpChargingView.DEBUG) {
                                Log.i("OpSWarpChargingView", "P4 onAnimationStart()");
                            }
                        }

                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationEnd(Animator animator2) {
                            OpSWarpChargingView.this.animationEnd("chargingScaleUpFadeOut");
                        }
                    });
                    OpSWarpChargingView.this.mChargingP4AnimSet.start();
                } else if (!OpSWarpChargingView.this.mIsAnimationPlaying) {
                    OpSWarpChargingView.this.animationEnd("chargingAnimP3");
                }
            }
        });
        new ValueAnimator();
        ValueAnimator ofInt5 = ValueAnimator.ofInt(14, 29);
        ofInt5.setInterpolator(new LinearInterpolator());
        ofInt5.setDuration((long) 544);
        ofInt5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.10
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpSWarpChargingView.this.mBackgroundView.setImageBitmap(OpSWarpChargingView.this.mChargingAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat4 = ValueAnimator.ofFloat(0.24f, 1.1f);
        ofFloat4.setDuration((long) 190);
        ofFloat4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.11
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpSWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpSWarpChargingView.this.mInfoView.setScaleY(floatValue);
                OpSWarpChargingView.this.mInfoView.setAlpha((floatValue - 0.24f) / 0.86f);
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat5 = ValueAnimator.ofFloat(1.1f, 1.0f);
        ofFloat5.setDuration(j);
        ofFloat5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.12
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpSWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpSWarpChargingView.this.mInfoView.setScaleY(floatValue);
            }
        });
        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.setInterpolator(ANIMATION_INTERPILATOR);
        animatorSet2.play(ofFloat4).before(ofFloat5);
        AnimatorSet animatorSet3 = new AnimatorSet();
        this.mChargingScaleUpAnimSet = animatorSet3;
        animatorSet3.playTogether(ofInt5, animatorSet2);
        this.mChargingScaleUpAnimSet.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpSWarpChargingView.13
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpSWarpChargingView.this.mNowPlaying = 1;
                if (OpSWarpChargingView.DEBUG) {
                    Log.i("OpSWarpChargingView", "P2 onAnimationStart()");
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (!OpSWarpChargingView.this.mIsAnimationPlaying) {
                    OpSWarpChargingView.this.animationEnd("chargingAnimP2");
                } else if (OpSWarpChargingView.this.isSWarp()) {
                    OpSWarpChargingView.this.mChargingAnimWarpP3.start();
                } else {
                    OpSWarpChargingView.this.mChargingAnimP3.start();
                }
            }
        });
        new ValueAnimator();
        ValueAnimator ofInt6 = ValueAnimator.ofInt(0, 13);
        this.mChargingAnimP1 = ofInt6;
        ofInt6.setInterpolator(new LinearInterpolator());
        this.mChargingAnimP1.setDuration((long) 448);
        this.mChargingAnimP1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.14
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpSWarpChargingView.this.mBackgroundView.setImageBitmap(OpSWarpChargingView.this.mChargingAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        this.mChargingAnimP1.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpSWarpChargingView.15
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (OpSWarpChargingView.DEBUG) {
                    Log.i("OpSWarpChargingView", "P1 onAnimationStart()");
                }
                OpSWarpChargingView.this.mNowPlaying = 0;
                OpSWarpChargingView.this.mInfoView.setScaleX(0.0f);
                OpSWarpChargingView.this.mInfoView.setScaleY(0.0f);
                OpSWarpChargingView.this.mInfoView.setAlpha(0.0f);
                OpSWarpChargingView.this.setVisibility(0);
                OpSWarpChargingView.this.mWarpView.setVisibility(4);
                OpSWarpChargingView opSWarpChargingView = OpSWarpChargingView.this;
                opSWarpChargingView.mLevelViewTransTarget = (float) opSWarpChargingView.mBatteryLevelHundredth.getWidth();
                OpSWarpChargingView.this.mBatteryLevelHundredth.setAlpha(0.0f);
                OpSWarpChargingView.this.mBatteryLevelViewContainer.setTranslationY((float) ((OpSWarpChargingView.this.mInfoView.getHeight() - OpSWarpChargingView.this.mBatteryLevel.getHeight()) / 2));
                OpSWarpChargingView.this.mHundredthTranslateUpAnim.setFloatValues((float) (((double) (OpSWarpChargingView.this.mInfoView.getHeight() - OpSWarpChargingView.this.mBatteryLevel.getHeight())) / 2.0d), 0.0f);
                if (OpSWarpChargingView.this.mChargingAnimationController != null) {
                    OpSWarpChargingView.this.mChargingAnimationController.animationStart(R$styleable.Constraint_layout_goneMarginTop);
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (OpSWarpChargingView.this.mIsAnimationPlaying) {
                    OpSWarpChargingView.this.mChargingScaleUpAnimSet.start();
                } else {
                    OpSWarpChargingView.this.animationEnd("chargingAnimP1");
                }
            }
        });
        AnimatorSet animatorSet4 = new AnimatorSet();
        animatorSet4.play(this.mChargingAnimP1);
        return animatorSet4;
    }

    private void genWarpAnimation() {
        ValueAnimator valueAnimator = new ValueAnimator();
        this.mHundredthTranslateUpAnim = valueAnimator;
        valueAnimator.setDuration((long) mRes.getInteger(C0009R$integer.op_control_time_325));
        this.mHundredthTranslateUpAnim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, C0010R$interpolator.op_control_interpolator_fast_out_slow_in_standard));
        this.mHundredthTranslateUpAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.16
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                OpSWarpChargingView.this.mBatteryLevelViewContainer.setTranslationY(((Float) valueAnimator2.getAnimatedValue()).floatValue());
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.6f, 1.0f);
        this.mHundredthBoundAnim = ofFloat;
        ofFloat.setStartDelay(100);
        this.mHundredthBoundAnim.setDuration((long) mRes.getInteger(C0009R$integer.op_control_time_325));
        this.mHundredthBoundAnim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, C0010R$interpolator.op_control_interpolator_fast_out_slow_in_standard));
        this.mHundredthBoundAnim.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpSWarpChargingView.17
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
                OpSWarpChargingView.this.mBatteryLevelHundredth.setScaleX(0.0f);
                OpSWarpChargingView.this.mBatteryLevelHundredth.setScaleY(0.0f);
                OpSWarpChargingView.this.mBatteryLevelHundredth.setVisibility(0);
            }
        });
        this.mHundredthBoundAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.18
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                float floatValue = ((Float) valueAnimator2.getAnimatedValue()).floatValue();
                float f = (floatValue - 0.6f) / 0.39999998f;
                OpSWarpChargingView.this.mBatteryLevelHundredthBound.setWidth((int) (OpSWarpChargingView.this.mLevelViewTransTarget * f));
                OpSWarpChargingView.this.mBatteryLevelHundredth.setScaleX(floatValue);
                OpSWarpChargingView.this.mBatteryLevelHundredth.setScaleY(floatValue);
                OpSWarpChargingView.this.mBatteryLevelHundredth.setAlpha(f);
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.6f, 1.0f);
        this.mWarpZoomInAnim = ofFloat2;
        ofFloat2.setStartDelay(200);
        this.mWarpZoomInAnim.setDuration((long) mRes.getInteger(C0009R$integer.op_control_time_325));
        this.mWarpZoomInAnim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, C0010R$interpolator.op_control_interpolator_fast_out_slow_in_standard));
        this.mWarpZoomInAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.19
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                float floatValue = ((Float) valueAnimator2.getAnimatedValue()).floatValue();
                OpSWarpChargingView.this.mWarpView.setScaleX(floatValue);
                OpSWarpChargingView.this.mWarpView.setScaleY(floatValue);
                OpSWarpChargingView.this.mWarpView.setAlpha((floatValue - 0.6f) / 0.39999998f);
            }
        });
        this.mWarpZoomInAnim.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpSWarpChargingView.20
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
                OpSWarpChargingView.this.mWarpView.setVisibility(0);
            }
        });
        ValueAnimator valueAnimator2 = new ValueAnimator();
        this.mHundredLevelAnim = valueAnimator2;
        valueAnimator2.setInterpolator(new LinearInterpolator());
        this.mHundredLevelAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpSWarpChargingView.21
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator3) {
                String[] split = new BigDecimal((double) ((Float) valueAnimator3.getAnimatedValue()).floatValue()).setScale(2, 4).toString().split("\\.");
                OpSWarpChargingView.this.mBatteryLevel.setText(split[0]);
                TextView textView = OpSWarpChargingView.this.mBatteryLevelHundredth;
                textView.setText("." + split[1]);
            }
        });
    }

    public void prepareAsset(int i) {
        if (DEBUG) {
            Log.i("OpSWarpChargingView", "prepareAsset() / mAssetLoading:" + this.mAssetLoading + " / mAssetLoaded:" + this.mAssetLoaded + " chargingType " + i);
        }
        this.mChargingType = i;
        if (this.mAssetLoading || this.mAssetLoaded) {
            this.mKeyguardShowing = true;
            return;
        }
        this.mAssetLoading = true;
        this.mKeyguardShowing = true;
        this.mBackgroundHandler.post(new Runnable() { // from class: com.oneplus.battery.OpSWarpChargingView.22
            @Override // java.lang.Runnable
            public void run() {
                OpSWarpChargingView.this.preloadAnimationList();
            }
        });
    }

    public void releaseAsset() {
        if (DEBUG) {
            Log.i("OpSWarpChargingView", "releaseAsset() / mAssetLoaded:" + this.mAssetLoaded + " / isAnimationStart:" + this.isAnimationStart + " / mAssetReleasing:" + this.mAssetReleasing);
        }
        if (this.mAssetLoaded && !this.isAnimationStart && !this.mAssetReleasing) {
            this.mAssetReleasing = true;
            View view = this.mInfoView;
            if (view != null) {
                view.setBackgroundResource(0);
            }
            this.mBackgroundHandler.post(new Runnable() { // from class: com.oneplus.battery.OpSWarpChargingView.23
                @Override // java.lang.Runnable
                public void run() {
                    OpSWarpChargingView.this.releaseAnimationList();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void preloadAnimationList() {
        TypedArray typedArray;
        if (DEBUG) {
            Log.i("OpSWarpChargingView", "preloadAnimationList()");
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (this.mChargingType == 0) {
            typedArray = mRes.obtainTypedArray(C0001R$array.warpcharge_wired_anim);
        } else {
            typedArray = mRes.obtainTypedArray(C0001R$array.warpcharge_wireless_anim);
        }
        TypedArray obtainTypedArray = mRes.obtainTypedArray(C0001R$array.swarpcharge_wired_anim);
        for (int i = 0; i < this.mChargingAnimationAssets.size(); i++) {
            Bitmap bitmap = this.mChargingAnimationAssets.get(i);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        this.mChargingAnimationAssets.clear();
        for (int i2 = 0; i2 < this.mChargingAnimationSWarpAssets.size(); i2++) {
            Bitmap bitmap2 = this.mChargingAnimationSWarpAssets.get(i2);
            if (bitmap2 != null) {
                bitmap2.recycle();
            }
        }
        this.mChargingAnimationSWarpAssets.clear();
        for (int i3 = 0; i3 < typedArray.length(); i3++) {
            InputStream openRawResource = getResources().openRawResource(typedArray.getResourceId(i3, 0));
            this.mChargingAnimationAssets.add(BitmapFactory.decodeStream(openRawResource));
            if (openRawResource != null) {
                try {
                    openRawResource.close();
                } catch (IOException unused) {
                }
            }
        }
        typedArray.recycle();
        for (int i4 = 0; i4 < obtainTypedArray.length(); i4++) {
            InputStream openRawResource2 = getResources().openRawResource(obtainTypedArray.getResourceId(i4, 0));
            this.mChargingAnimationSWarpAssets.add(BitmapFactory.decodeStream(openRawResource2));
            if (openRawResource2 != null) {
                try {
                    openRawResource2.close();
                } catch (IOException unused2) {
                }
            }
        }
        obtainTypedArray.recycle();
        long currentTimeMillis2 = System.currentTimeMillis();
        if (DEBUG) {
            Log.i("OpSWarpChargingView", "preloadAnimationList: cost Time" + (currentTimeMillis2 - currentTimeMillis) + " mChargingAnimationAssets size:" + this.mChargingAnimationAssets.size());
        }
        this.mHandler.post(new Runnable() { // from class: com.oneplus.battery.OpSWarpChargingView.24
            @Override // java.lang.Runnable
            public void run() {
                OpSWarpChargingView.this.mAssetLoading = false;
                OpSWarpChargingView.this.mAssetLoaded = true;
                Log.i("OpSWarpChargingView", "preloadAnimationList: pre start anim keyguardShowing " + OpSWarpChargingView.this.mKeyguardShowing);
                if (!OpSWarpChargingView.this.mKeyguardShowing) {
                    OpSWarpChargingView.this.releaseAsset();
                } else if (OpSWarpChargingView.this.mIsPaddingStartAnimation) {
                    OpSWarpChargingView opSWarpChargingView = OpSWarpChargingView.this;
                    opSWarpChargingView.startAnimation(opSWarpChargingView.mChargingType);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseAnimationList() {
        if (DEBUG) {
            Log.i("OpSWarpChargingView", "releaseAnimationList()");
        }
        this.mBackgroundView.setImageBitmap(null);
        for (int i = 0; i < this.mChargingAnimationAssets.size(); i++) {
            Bitmap bitmap = this.mChargingAnimationAssets.get(i);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        this.mChargingAnimationAssets.clear();
        for (int i2 = 0; i2 < this.mChargingAnimationSWarpAssets.size(); i2++) {
            Bitmap bitmap2 = this.mChargingAnimationSWarpAssets.get(i2);
            if (bitmap2 != null) {
                bitmap2.recycle();
            }
        }
        this.mChargingAnimationSWarpAssets.clear();
        this.mHandler.post(new Runnable() { // from class: com.oneplus.battery.OpSWarpChargingView.25
            @Override // java.lang.Runnable
            public void run() {
                OpSWarpChargingView.this.mAssetLoaded = false;
                OpSWarpChargingView.this.mAssetReleasing = false;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void animationEnd(String str) {
        Log.i("OpSWarpChargingView", "animationEnd from " + str);
        setVisibility(8);
        this.mInfoView.setScaleX(0.0f);
        this.mInfoView.setScaleY(0.0f);
        this.mInfoView.setAlpha(0.0f);
        this.mBackgroundView.setImageBitmap(null);
        this.mWarpView.setVisibility(4);
        int i = this.mPrevLevel;
        this.mPrevLevel = 0;
        onBatteryLevelChanged(i, false, false);
        OpChargingAnimationController opChargingAnimationController = this.mChargingAnimationController;
        if (opChargingAnimationController != null) {
            opChargingAnimationController.animationEnd(R$styleable.Constraint_layout_goneMarginTop);
        }
        this.isAnimationStart = false;
        this.mWarpChargingType = 0;
        this.mIsWarpAnimRunning = false;
        TextView textView = this.mBatteryLevelHundredthBound;
        if (textView != null) {
            textView.setWidth(0);
        }
        releaseAsset();
    }

    public void notifyWarpCharging(int i) {
        Log.d("OpSWarpChargingView", " Notify Warp Charging : " + i);
        this.mWarpChargingType = i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSWarp() {
        return 3 == SystemProperties.getInt("persist.test.warp", this.mWarpChargingType);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isWarp() {
        if (1 == SystemProperties.getInt("persist.test.warp", this.mWarpChargingType) || 2 == SystemProperties.getInt("persist.test.warp", this.mWarpChargingType) || 3 == SystemProperties.getInt("persist.test.warp", this.mWarpChargingType)) {
            return true;
        }
        return false;
    }
}
