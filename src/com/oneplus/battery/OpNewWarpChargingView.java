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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.R$styleable;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.C0001R$array;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
public class OpNewWarpChargingView extends FrameLayout {
    private static final Interpolator ANIMATION_INTERPILATOR = new PathInterpolator(0.3f, 0.0f, 0.3f, 1.0f);
    static final boolean DEBUG = OpUtils.DEBUG_ONEPLUS;
    private static final Interpolator WARP_ANIMATION_INTERPILATOR = new PathInterpolator(0.4f, 0.0f, 0.3f, 1.0f);
    private static Resources mRes;
    private boolean isAnimationStart;
    private boolean mAssetLoaded;
    private boolean mAssetLoading;
    private boolean mAssetReleasing;
    private Handler mBackgroundHandler;
    private ImageView mBackgroundView;
    private TextView mBatteryLevel;
    private View mBatteryLevelViewContainer;
    private AnimatorSet mChargeAnimation;
    private ValueAnimator mChargingAnimP1;
    private ValueAnimator mChargingAnimP3;
    private int mChargingAnimP3repeatCount;
    ArrayList<Bitmap> mChargingAnimationAssets;
    private OpChargingAnimationController mChargingAnimationController;
    private AnimatorSet mChargingScaleDownAnimSet;
    private AnimatorSet mChargingScaleUpAnimSet;
    private int mChargingType;
    private Context mContext;
    private Handler mHandler;
    private View mInfoView;
    private RelativeLayout mInfoViewContainer;
    private boolean mIsAnimationPlaying;
    private boolean mIsPaddingStartAnimation;
    private boolean mIsWarpAnimRunning;
    private boolean mIsWarpAnimStart;
    private boolean mKeyguardShowing;
    private int mNowPlaying;
    private int mPrevLevel;
    private AnimatorSet mWarpAnimation;
    ArrayList<Bitmap> mWarpAnimationAssets;
    private ImageView mWarpView;

    static /* synthetic */ int access$512(OpNewWarpChargingView opNewWarpChargingView, int i) {
        int i2 = opNewWarpChargingView.mChargingAnimP3repeatCount + i;
        opNewWarpChargingView.mChargingAnimP3repeatCount = i2;
        return i2;
    }

    public OpNewWarpChargingView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mBackgroundHandler = new Handler(BackgroundThread.getHandler().getLooper());
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mChargingAnimationAssets = new ArrayList<>();
        this.mAssetLoading = false;
        this.mAssetLoaded = false;
        this.mAssetReleasing = false;
        this.mIsPaddingStartAnimation = false;
        this.mChargingType = 0;
        this.mWarpAnimationAssets = new ArrayList<>();
        this.mIsWarpAnimStart = false;
        this.mIsWarpAnimRunning = false;
        this.mKeyguardShowing = false;
        this.mIsAnimationPlaying = false;
        this.mNowPlaying = -1;
        this.mChargingAnimP3repeatCount = 0;
        this.mContext = context;
        mRes = context.getResources();
    }

    public OpNewWarpChargingView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public OpNewWarpChargingView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public OpNewWarpChargingView(Context context) {
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
        this.mWarpView = (ImageView) findViewById(C0008R$id.warp_view);
        this.mBackgroundView = (ImageView) findViewById(C0008R$id.background_view);
        this.mInfoView = findViewById(C0008R$id.info_view);
        findViewById(C0008R$id.scrim_view);
        this.mBatteryLevelViewContainer = findViewById(C0008R$id.battery_level_container);
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
        View view = this.mInfoView;
        if (view != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            if (marginLayoutParams != null) {
                marginLayoutParams.height = OpUtils.convertDpToFixedPx(mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_anim_info_view_height));
            }
            this.mInfoView.setLayoutParams(marginLayoutParams);
        }
        View view2 = this.mBatteryLevelViewContainer;
        if (view2 != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) view2.getLayoutParams();
            if (marginLayoutParams2 != null) {
                marginLayoutParams2.height = OpUtils.convertDpToFixedPx(mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_anim_info_battery_level_container_height));
            }
            this.mBatteryLevelViewContainer.setLayoutParams(marginLayoutParams2);
        }
        TextView textView = this.mBatteryLevel;
        if (textView != null) {
            textView.setTextSize(0, (float) OpUtils.convertDpToFixedPx(mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_anim_info_battery_level_text_size)));
            this.mBatteryLevel.setPadding(OpUtils.convertDpToFixedPx(mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_anim_info_battery_level_text_padding_left)), 0, 0, 0);
            ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) this.mBatteryLevel.getLayoutParams();
            if (marginLayoutParams3 != null) {
                marginLayoutParams3.height = OpUtils.convertDpToFixedPx(mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_anim_info_battery_level_height));
            }
            this.mBatteryLevel.setLayoutParams(marginLayoutParams3);
        }
        ImageView imageView = this.mWarpView;
        if (imageView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams4 = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
            if (marginLayoutParams4 != null) {
                marginLayoutParams4.width = OpUtils.convertDpToFixedPx(mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_text_image_width));
                marginLayoutParams4.height = OpUtils.convertDpToFixedPx(mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_text_image_height));
                marginLayoutParams4.topMargin = OpUtils.convertDpToFixedPx(mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_anim_info_warp_view_margin_top));
            }
            this.mWarpView.setLayoutParams(marginLayoutParams4);
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
            stringBuffer.append("%");
            TextView textView2 = this.mBatteryLevel;
            if (textView2 != null) {
                textView2.setText(stringBuffer.toString());
            }
        }
    }

    public void startAnimation(int i) {
        Handler handler;
        if (this.mChargingType != i) {
            this.mAssetLoaded = false;
        }
        if (!this.mAssetLoaded || this.mAssetReleasing) {
            if (DEBUG) {
                Log.i("OpNewWarpChargingView", "startAnimation / else / prepareAsset");
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
            Log.i("OpNewWarpChargingView", "startAnimation");
            AnimatorSet animatorSet = this.mChargeAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
            } else {
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
            Log.i("OpNewWarpChargingView", "stop warp animation");
            AnimatorSet animatorSet = this.mWarpAnimation;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
        }
        if (this.isAnimationStart) {
            Log.i("OpNewWarpChargingView", "stop charging animation");
            this.mIsAnimationPlaying = false;
            int i = this.mNowPlaying;
            if (i == 1) {
                this.mChargingScaleUpAnimSet.cancel();
            } else if (i == 2) {
                this.mChargingAnimP3.cancel();
            } else if (i != 3) {
                this.mChargingAnimP1.cancel();
            } else {
                this.mChargingScaleDownAnimSet.cancel();
            }
        }
    }

    private AnimatorSet getChargeAnimation() {
        new ValueAnimator();
        ValueAnimator ofInt = ValueAnimator.ofInt(90, 107);
        ofInt.setInterpolator(new LinearInterpolator());
        ofInt.setDuration((long) 608);
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpNewWarpChargingView.this.mBackgroundView.setImageBitmap(OpNewWarpChargingView.this.mChargingAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 1.05f);
        ofFloat.setDuration((long) 255);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpNewWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpNewWarpChargingView.this.mInfoView.setScaleY(floatValue);
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(1.05f, 0.8f);
        long j = (long) 320;
        ofFloat2.setDuration(j);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpNewWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpNewWarpChargingView.this.mInfoView.setScaleY(floatValue);
                OpNewWarpChargingView.this.mInfoView.setAlpha(1.0f - ((1.05f - floatValue) / 0.24999994f));
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(ANIMATION_INTERPILATOR);
        animatorSet.play(ofFloat).before(ofFloat2);
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mChargingScaleDownAnimSet = animatorSet2;
        animatorSet2.playTogether(ofInt, animatorSet);
        this.mChargingScaleDownAnimSet.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.4
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpNewWarpChargingView.this.mNowPlaying = 3;
                if (OpNewWarpChargingView.DEBUG) {
                    Log.i("OpNewWarpChargingView", "P4 onAnimationStart()");
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                OpNewWarpChargingView.this.animationEnd("chargingScaleDownAnimSet");
            }
        });
        new ValueAnimator();
        ValueAnimator ofInt2 = ValueAnimator.ofInt(30, 89);
        this.mChargingAnimP3 = ofInt2;
        ofInt2.setInterpolator(new LinearInterpolator());
        this.mChargingAnimP3.setDuration((long) 1952);
        this.mChargingAnimP3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.5
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpNewWarpChargingView.this.mBackgroundView.setImageBitmap(OpNewWarpChargingView.this.mChargingAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
                boolean z = (OpNewWarpChargingView.this.mChargingAnimP3.getTotalDuration() * 3) - (OpNewWarpChargingView.this.mChargingAnimP3.getCurrentPlayTime() + (OpNewWarpChargingView.this.mChargingAnimP3.getTotalDuration() * ((long) OpNewWarpChargingView.this.mChargingAnimP3repeatCount))) > ((long) (OpNewWarpChargingView.this.mWarpAnimationAssets.size() * 32));
                if (OpNewWarpChargingView.DEBUG && !z && !OpNewWarpChargingView.this.mIsWarpAnimRunning) {
                    OpNewWarpChargingView.this.mIsWarpAnimRunning = true;
                    Log.i("OpNewWarpChargingView", "Not enough time to play the warp animation.");
                }
                if (OpNewWarpChargingView.this.mIsWarpAnimStart && z) {
                    if (OpNewWarpChargingView.this.mWarpAnimation == null) {
                        OpNewWarpChargingView opNewWarpChargingView = OpNewWarpChargingView.this;
                        opNewWarpChargingView.mWarpAnimation = opNewWarpChargingView.getWarpAnimation();
                    }
                    if (!OpNewWarpChargingView.this.mIsWarpAnimRunning && OpNewWarpChargingView.this.mChargingAnimP3.isStarted()) {
                        OpNewWarpChargingView.this.mWarpAnimation.cancel();
                        OpNewWarpChargingView.this.mWarpAnimation.start();
                    }
                }
            }
        });
        this.mChargingAnimP3.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.6
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                StatusBar phoneStatusBar;
                OpNewWarpChargingView.this.mNowPlaying = 2;
                if (OpNewWarpChargingView.DEBUG) {
                    Log.i("OpNewWarpChargingView", "P3 onAnimationStart() repeatCount " + OpNewWarpChargingView.this.mChargingAnimP3repeatCount);
                }
                if (OpNewWarpChargingView.this.mChargingAnimP3repeatCount == 2 && (phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar()) != null) {
                    phoneStatusBar.userActivity();
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                Log.i("OpNewWarpChargingView", "P3 onAnimationEnd() repeatCount " + OpNewWarpChargingView.this.mChargingAnimP3repeatCount + " mIsWarpAnimRunning " + OpNewWarpChargingView.this.mIsWarpAnimRunning + " mIsAnimationPlaying " + OpNewWarpChargingView.this.mIsAnimationPlaying);
                if (OpNewWarpChargingView.this.mIsAnimationPlaying && ((OpNewWarpChargingView.this.mChargingAnimP3repeatCount < 2 && !OpNewWarpChargingView.this.mIsWarpAnimRunning) || (OpNewWarpChargingView.this.mChargingAnimP3repeatCount < 2 && OpNewWarpChargingView.this.mWarpAnimation != null && OpNewWarpChargingView.this.mWarpAnimation.isRunning()))) {
                    OpNewWarpChargingView.access$512(OpNewWarpChargingView.this, 1);
                    OpNewWarpChargingView.this.mChargingAnimP3.start();
                } else if (OpNewWarpChargingView.this.mIsAnimationPlaying) {
                    OpNewWarpChargingView.this.mChargingScaleDownAnimSet.start();
                } else if (!OpNewWarpChargingView.this.mIsAnimationPlaying) {
                    OpNewWarpChargingView.this.animationEnd("chargingAnimP3");
                }
            }
        });
        new ValueAnimator();
        ValueAnimator ofInt3 = ValueAnimator.ofInt(14, 29);
        ofInt3.setInterpolator(new LinearInterpolator());
        ofInt3.setDuration((long) 544);
        ofInt3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.7
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpNewWarpChargingView.this.mBackgroundView.setImageBitmap(OpNewWarpChargingView.this.mChargingAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(0.24f, 1.1f);
        ofFloat3.setDuration((long) 190);
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.8
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpNewWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpNewWarpChargingView.this.mInfoView.setScaleY(floatValue);
                OpNewWarpChargingView.this.mInfoView.setAlpha((floatValue - 0.24f) / 0.86f);
            }
        });
        new ValueAnimator();
        ValueAnimator ofFloat4 = ValueAnimator.ofFloat(1.1f, 1.0f);
        ofFloat4.setDuration(j);
        ofFloat4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.9
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpNewWarpChargingView.this.mInfoView.setScaleX(floatValue);
                OpNewWarpChargingView.this.mInfoView.setScaleY(floatValue);
            }
        });
        AnimatorSet animatorSet3 = new AnimatorSet();
        animatorSet3.setInterpolator(ANIMATION_INTERPILATOR);
        animatorSet3.play(ofFloat3).before(ofFloat4);
        AnimatorSet animatorSet4 = new AnimatorSet();
        this.mChargingScaleUpAnimSet = animatorSet4;
        animatorSet4.playTogether(ofInt3, animatorSet3);
        this.mChargingScaleUpAnimSet.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.10
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpNewWarpChargingView.this.mNowPlaying = 1;
                if (OpNewWarpChargingView.DEBUG) {
                    Log.i("OpNewWarpChargingView", "P2 onAnimationStart()");
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (OpNewWarpChargingView.this.mIsAnimationPlaying) {
                    OpNewWarpChargingView.this.mChargingAnimP3.start();
                } else {
                    OpNewWarpChargingView.this.animationEnd("chargingAnimP2");
                }
            }
        });
        new ValueAnimator();
        ValueAnimator ofInt4 = ValueAnimator.ofInt(0, 13);
        this.mChargingAnimP1 = ofInt4;
        ofInt4.setInterpolator(new LinearInterpolator());
        this.mChargingAnimP1.setDuration((long) 448);
        this.mChargingAnimP1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.11
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpNewWarpChargingView.this.mBackgroundView.setImageBitmap(OpNewWarpChargingView.this.mChargingAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        this.mChargingAnimP1.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.12
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (OpNewWarpChargingView.DEBUG) {
                    Log.i("OpNewWarpChargingView", "P1 onAnimationStart()");
                }
                OpNewWarpChargingView.this.mNowPlaying = 0;
                OpNewWarpChargingView.this.mInfoView.setScaleX(0.0f);
                OpNewWarpChargingView.this.mInfoView.setScaleY(0.0f);
                OpNewWarpChargingView.this.mInfoView.setAlpha(0.0f);
                OpNewWarpChargingView.this.setVisibility(0);
                float convertDpToFixedPx = (float) ((OpUtils.convertDpToFixedPx(OpNewWarpChargingView.mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_anim_info_view_height)) - OpUtils.convertDpToFixedPx(OpNewWarpChargingView.mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_anim_info_battery_level_height))) / 2);
                OpNewWarpChargingView.this.mBatteryLevel.setTranslationY(convertDpToFixedPx);
                OpNewWarpChargingView.this.mWarpView.setTranslationY(convertDpToFixedPx);
                OpNewWarpChargingView.this.mWarpView.setVisibility(4);
                if (OpNewWarpChargingView.this.mChargingAnimationController != null) {
                    OpNewWarpChargingView.this.mChargingAnimationController.animationStart(R$styleable.Constraint_layout_goneMarginTop);
                }
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (OpNewWarpChargingView.this.mIsAnimationPlaying) {
                    OpNewWarpChargingView.this.mChargingScaleUpAnimSet.start();
                } else {
                    OpNewWarpChargingView.this.animationEnd("chargingAnimP1");
                }
            }
        });
        AnimatorSet animatorSet5 = new AnimatorSet();
        animatorSet5.play(this.mChargingAnimP1);
        return animatorSet5;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AnimatorSet getWarpAnimation() {
        new ValueAnimator();
        ValueAnimator ofInt = ValueAnimator.ofInt(0, this.mWarpAnimationAssets.size() - 1);
        ofInt.setInterpolator(new LinearInterpolator());
        ofInt.setDuration((long) (this.mWarpAnimationAssets.size() * 32));
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.13
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpNewWarpChargingView.this.mWarpView.setImageBitmap(OpNewWarpChargingView.this.mWarpAnimationAssets.get(((Integer) valueAnimator.getAnimatedValue()).intValue()));
            }
        });
        ofInt.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.14
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
                OpNewWarpChargingView.this.mWarpView.setVisibility(0);
            }
        });
        int convertDpToFixedPx = OpUtils.convertDpToFixedPx(mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_anim_info_view_height));
        int convertDpToFixedPx2 = OpUtils.convertDpToFixedPx(mRes.getDimension(C0005R$dimen.op_warp_charging_wireless_anim_info_battery_level_container_height));
        final float translationY = this.mBatteryLevel.getTranslationY();
        final float translationY2 = this.mWarpView.getTranslationY();
        new ValueAnimator();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, translationY - ((float) ((convertDpToFixedPx - convertDpToFixedPx2) / 2)));
        ofFloat.setInterpolator(WARP_ANIMATION_INTERPILATOR);
        ofFloat.setDuration(425L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.15
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpNewWarpChargingView.this.mBatteryLevel.setTranslationY(translationY - floatValue);
                OpNewWarpChargingView.this.mWarpView.setTranslationY(translationY2 - floatValue);
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ofFloat, ofInt);
        animatorSet.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.battery.OpNewWarpChargingView.16
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OpNewWarpChargingView.this.mIsWarpAnimRunning = true;
                Log.d("OpNewWarpChargingView", "warp animation start");
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                Log.d("OpNewWarpChargingView", "warp animation end");
            }
        });
        return animatorSet;
    }

    public void prepareAsset(int i) {
        if (DEBUG) {
            Log.i("OpNewWarpChargingView", "prepareAsset() / mAssetLoading:" + this.mAssetLoading + " / mAssetLoaded:" + this.mAssetLoaded + " chargingType " + i);
        }
        this.mChargingType = i;
        if (this.mAssetLoading || this.mAssetLoaded) {
            this.mKeyguardShowing = true;
            return;
        }
        this.mAssetLoading = true;
        this.mKeyguardShowing = true;
        this.mBackgroundHandler.post(new Runnable() { // from class: com.oneplus.battery.OpNewWarpChargingView.17
            @Override // java.lang.Runnable
            public void run() {
                OpNewWarpChargingView.this.preloadAnimationList();
            }
        });
    }

    public void releaseAsset() {
        if (DEBUG) {
            Log.i("OpNewWarpChargingView", "releaseAsset() / mAssetLoaded:" + this.mAssetLoaded + " / isAnimationStart:" + this.isAnimationStart + " / mAssetReleasing:" + this.mAssetReleasing);
        }
        if (this.mAssetLoaded && !this.isAnimationStart && !this.mAssetReleasing) {
            this.mAssetReleasing = true;
            View view = this.mInfoView;
            if (view != null) {
                view.setBackgroundResource(0);
            }
            this.mBackgroundHandler.post(new Runnable() { // from class: com.oneplus.battery.OpNewWarpChargingView.18
                @Override // java.lang.Runnable
                public void run() {
                    OpNewWarpChargingView.this.releaseAnimationList();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void preloadAnimationList() {
        TypedArray typedArray;
        if (DEBUG) {
            Log.i("OpNewWarpChargingView", "preloadAnimationList()");
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (this.mChargingType == 0) {
            typedArray = mRes.obtainTypedArray(C0001R$array.warpcharge_wired_anim);
        } else {
            typedArray = mRes.obtainTypedArray(C0001R$array.warpcharge_wireless_anim);
        }
        TypedArray obtainTypedArray = mRes.obtainTypedArray(C0001R$array.warp_anim);
        for (int i = 0; i < this.mChargingAnimationAssets.size(); i++) {
            Bitmap bitmap = this.mChargingAnimationAssets.get(i);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        for (int i2 = 0; i2 < this.mWarpAnimationAssets.size(); i2++) {
            Bitmap bitmap2 = this.mWarpAnimationAssets.get(i2);
            if (bitmap2 != null) {
                bitmap2.recycle();
            }
        }
        this.mChargingAnimationAssets.clear();
        this.mWarpAnimationAssets.clear();
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
        for (int i4 = 0; i4 < obtainTypedArray.length(); i4++) {
            InputStream openRawResource2 = getResources().openRawResource(obtainTypedArray.getResourceId(i4, 0));
            this.mWarpAnimationAssets.add(BitmapFactory.decodeStream(openRawResource2));
            if (openRawResource2 != null) {
                try {
                    openRawResource2.close();
                } catch (IOException unused2) {
                }
            }
        }
        typedArray.recycle();
        obtainTypedArray.recycle();
        long currentTimeMillis2 = System.currentTimeMillis();
        if (DEBUG) {
            Log.i("OpNewWarpChargingView", "preloadAnimationList: cost Time" + (currentTimeMillis2 - currentTimeMillis) + " mChargingAnimationAssets size:" + this.mChargingAnimationAssets.size() + " mWarpAnimationAssets size:" + this.mWarpAnimationAssets.size());
        }
        this.mHandler.post(new Runnable() { // from class: com.oneplus.battery.OpNewWarpChargingView.19
            @Override // java.lang.Runnable
            public void run() {
                OpNewWarpChargingView.this.mAssetLoading = false;
                OpNewWarpChargingView.this.mAssetLoaded = true;
                Log.i("OpNewWarpChargingView", "preloadAnimationList: pre start anim keyguardShowing " + OpNewWarpChargingView.this.mKeyguardShowing);
                if (!OpNewWarpChargingView.this.mKeyguardShowing) {
                    OpNewWarpChargingView.this.releaseAsset();
                } else if (OpNewWarpChargingView.this.mIsPaddingStartAnimation) {
                    OpNewWarpChargingView opNewWarpChargingView = OpNewWarpChargingView.this;
                    opNewWarpChargingView.startAnimation(opNewWarpChargingView.mChargingType);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseAnimationList() {
        if (DEBUG) {
            Log.i("OpNewWarpChargingView", "releaseAnimationList()");
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
        this.mHandler.post(new Runnable() { // from class: com.oneplus.battery.OpNewWarpChargingView.20
            @Override // java.lang.Runnable
            public void run() {
                OpNewWarpChargingView.this.mAssetLoaded = false;
                OpNewWarpChargingView.this.mAssetReleasing = false;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void animationEnd(String str) {
        Log.i("OpNewWarpChargingView", "animationEnd from " + str);
        setVisibility(8);
        this.mInfoView.setScaleX(0.0f);
        this.mInfoView.setScaleY(0.0f);
        this.mInfoView.setAlpha(0.0f);
        this.mBackgroundView.setImageBitmap(null);
        this.mWarpView.setImageBitmap(null);
        OpChargingAnimationController opChargingAnimationController = this.mChargingAnimationController;
        if (opChargingAnimationController != null) {
            opChargingAnimationController.animationEnd(R$styleable.Constraint_layout_goneMarginTop);
        }
        this.isAnimationStart = false;
        this.mIsWarpAnimStart = false;
        this.mIsWarpAnimRunning = false;
        this.mChargingAnimP3repeatCount = 0;
        releaseAsset();
    }

    public void notifyWarpCharging() {
        if (!this.mIsWarpAnimStart) {
            Log.d("OpNewWarpChargingView", " notifyWarpCharging");
            this.mIsWarpAnimStart = true;
        }
    }
}
