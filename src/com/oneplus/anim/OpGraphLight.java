package com.oneplus.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SystemSensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.OnCompositionLoadedListener;
import com.android.systemui.C0001R$array;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
public class OpGraphLight implements DarkIconDispatcher.DarkReceiver {
    private static int ALPHA_MODE_HIGH = 255;
    private static int ALPHA_MODE_LOW = 100;
    private static final boolean CIRCLE_FRONT_CAMERA_ANIM = OpUtils.isSupportHolePunchFrontCam();
    private static float LUX_THRESHOLD_HIGH = 100.0f;
    private static float LUX_THRESHOLD_MIDDLE = 20.0f;
    private int m2kOr1080p;
    private int mAlphaMode = ALPHA_MODE_HIGH;
    private int mAnimateImageHeight = 70;
    private ImageView mAnimateImageView;
    private int mAnimateImageWidth = 507;
    private final Context mContext;
    private boolean mDarkMode;
    private int mFrontCameraPosistion;
    private int mFrontCameraPosistionY;
    private final Handler mHandler;
    private Runnable mHideRunnable = new Runnable() { // from class: com.oneplus.anim.OpGraphLight.3
        @Override // java.lang.Runnable
        public void run() {
            OpGraphLight.this.mHandler.removeCallbacks(OpGraphLight.this.mHideRunnable);
            if ((OpGraphLight.this.mAnimateImageView instanceof AnimateLottieView) && OpGraphLight.this.mAnimateImageView != null) {
                ((AnimateLottieView) OpGraphLight.this.mAnimateImageView).release(false);
            }
        }
    };
    private final LightBarController mLightBarController;
    private boolean mLightSensorRegistered = false;
    private OrientationEventListener mOrientationListener;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener = new SensorEventListener() { // from class: com.oneplus.anim.OpGraphLight.4
        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent sensorEvent) {
            int i;
            if (sensorEvent.sensor.getType() == 5) {
                float f = sensorEvent.values[0];
                if (f <= OpGraphLight.LUX_THRESHOLD_MIDDLE) {
                    i = OpGraphLight.ALPHA_MODE_LOW;
                } else if (f <= OpGraphLight.LUX_THRESHOLD_MIDDLE || f > OpGraphLight.LUX_THRESHOLD_HIGH) {
                    i = OpGraphLight.ALPHA_MODE_HIGH;
                } else {
                    i = ((int) (((f - OpGraphLight.LUX_THRESHOLD_MIDDLE) / (OpGraphLight.LUX_THRESHOLD_HIGH - OpGraphLight.LUX_THRESHOLD_MIDDLE)) * ((float) (OpGraphLight.ALPHA_MODE_HIGH - OpGraphLight.ALPHA_MODE_LOW)))) + OpGraphLight.ALPHA_MODE_LOW;
                }
                float abs = ((float) Math.abs(i - OpGraphLight.this.mAlphaMode)) / ((float) OpGraphLight.this.mAlphaMode);
                if (OpGraphLight.this.mAlphaMode != i && ((double) abs) > 0.1d) {
                    Log.d("OpGraphLight", "onSensorChanged mode " + i + " lux " + f);
                    OpGraphLight.this.mAlphaMode = i;
                    if (OpGraphLight.this.mAnimateImageView != null && (OpGraphLight.this.mAnimateImageView instanceof AnimateLottieView) && OpGraphLight.this.isFrontCameraAnimOn() && !OpGraphLight.this.mSupportDarkMode) {
                        ((AnimateLottieView) OpGraphLight.this.mAnimateImageView).setAlpha(OpGraphLight.this.mAlphaMode);
                    }
                }
            }
        }
    };
    private SensorManager mSensorManager;
    private Runnable mShowRunnable = new Runnable() { // from class: com.oneplus.anim.OpGraphLight.2
        @Override // java.lang.Runnable
        public void run() {
            OpGraphLight.this.mHandler.removeCallbacks(OpGraphLight.this.mShowRunnable);
            OpGraphLight.this.show(false);
        }
    };
    private StatusBar mStatusBar;
    private boolean mSupportDarkMode;
    private boolean mViewAdded;
    private LinearLayout mViewContainer;
    private final WindowManager mWm;

    public OpGraphLight(WindowManager windowManager, Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mWm = windowManager;
        if (context != null) {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KeyguardManager.class);
            this.mSupportDarkMode = this.mContext.getResources().getBoolean(C0003R$bool.config_front_camera_animation_support_dark_theme);
        }
        this.mOrientationListener = new OrientationEventListener(this.mContext, 2) { // from class: com.oneplus.anim.OpGraphLight.1
            @Override // android.view.OrientationEventListener
            public void onOrientationChanged(int i) {
                if (i != -1 && OpGraphLight.this.mAnimateImageView != null && (OpGraphLight.this.mAnimateImageView instanceof AnimateLottieView)) {
                    ((AnimateLottieView) OpGraphLight.this.mAnimateImageView).checkOrientationType();
                }
            }
        };
        this.mStatusBar = (StatusBar) Dependency.get(StatusBar.class);
        this.mLightBarController = (LightBarController) Dependency.get(LightBarController.class);
    }

    public void onConfigChanged(Configuration configuration) {
        ImageView imageView = this.mAnimateImageView;
        if (imageView != null && (imageView instanceof AnimateLottieView)) {
            ((AnimateLottieView) imageView).checkOrientationType();
        }
    }

    public void postShow() {
        this.mHandler.removeCallbacks(this.mShowRunnable);
        this.mHandler.removeCallbacks(this.mHideRunnable);
        this.mHandler.postDelayed(this.mShowRunnable, 50);
    }

    public void stop() {
        if (CIRCLE_FRONT_CAMERA_ANIM) {
            disableLightSensor();
            this.mHandler.removeCallbacks(this.mHideRunnable);
            this.mHandler.postDelayed(this.mHideRunnable, 150);
        }
    }

    public void forceStop() {
        if (CIRCLE_FRONT_CAMERA_ANIM) {
            ImageView imageView = this.mAnimateImageView;
            if ((imageView instanceof AnimateLottieView) && imageView != null) {
                ((AnimateLottieView) imageView).release(true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0298  */
    /* JADX WARNING: Removed duplicated region for block: B:71:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void show(boolean r21) {
        /*
        // Method dump skipped, instructions count: 792
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.anim.OpGraphLight.show(boolean):void");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hide() {
        if (this.mViewAdded) {
            ImageView imageView = this.mAnimateImageView;
            if (!(imageView == null || imageView.getParent() == null)) {
                Log.i("OpGraphLight", "hide, mViewContainer.remove view");
                this.mViewContainer.removeView(this.mAnimateImageView);
                ImageView imageView2 = this.mAnimateImageView;
                if (imageView2 instanceof AnimateLottieView) {
                    ((AnimateLottieView) imageView2).destroyLottieDrawable();
                }
            }
            this.mAnimateImageView = null;
            if (this.mViewContainer.getParent() != null) {
                Log.i("OpGraphLight", "hide, mWm.remove container view");
                this.mWm.removeView(this.mViewContainer);
            }
            this.mOrientationListener.disable();
            this.mViewContainer = null;
            ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this);
            this.mViewAdded = false;
        }
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i) {
        boolean z = false;
        boolean z2 = f == 1.0f;
        LightBarController lightBarController = this.mLightBarController;
        boolean z3 = lightBarController != null && lightBarController.getDockedLight();
        LightBarController lightBarController2 = this.mLightBarController;
        boolean z4 = lightBarController2 != null && lightBarController2.getHasDockedStack();
        ImageView imageView = this.mAnimateImageView;
        if (imageView != null && (imageView instanceof AnimateLottieView)) {
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.i("OpGraphLight", "onDarkChanged, area:" + rect + ", darkIntensity:" + f + ", tint:" + i + ", isDark:" + z2 + ", isInArea:" + DarkIconDispatcher.isInArea(rect, this.mAnimateImageView));
            }
            if ((rect != null && DarkIconDispatcher.isInArea(rect, this.mAnimateImageView)) || z4) {
                if (!z4) {
                    z = z2;
                } else if (z2 && z3) {
                    z = true;
                }
                OpUtils.notifyStatusBarIconsDark(z);
                if (this.mDarkMode != z) {
                    this.mDarkMode = z;
                    if (isFrontCameraAnimOn() && this.mSupportDarkMode) {
                        ((AnimateLottieView) this.mAnimateImageView).release(true);
                        show(true);
                    }
                }
            }
        }
    }

    private class FrontCameraAnimateBaseImageView extends ImageView {
        protected TypedArray mAnimationArray;
        protected AnimatorSet mAnimator;
        protected ViewGroup.LayoutParams mLp;
        protected int mOrientationType = getOrientation();
        protected ArrayList mStartAnimationAssets1 = new ArrayList();

        public FrontCameraAnimateBaseImageView(Context context) {
            super(context);
            setScaleType(ImageView.ScaleType.FIT_XY);
        }

        /* access modifiers changed from: protected */
        public int getOrientation() {
            Display defaultDisplay = OpGraphLight.this.mWm.getDefaultDisplay();
            if (defaultDisplay == null) {
                return 0;
            }
            return defaultDisplay.getRotation();
        }

        /* access modifiers changed from: protected */
        public boolean checkOrientationType() {
            int orientation = getOrientation();
            Log.v("OpGraphLight", "checkOrientationType / rotation:" + orientation);
            if (this.mOrientationType == orientation) {
                return true;
            }
            Log.v("OpGraphLight", "detect checkOrientationType() / rotation:" + orientation + " / mOrientationType:" + this.mOrientationType);
            this.mOrientationType = orientation;
            this.mAnimator.cancel();
            relaseAnimationList();
            OpGraphLight.this.hide();
            OpGraphLight.this.postShow();
            return false;
        }

        /* access modifiers changed from: protected */
        public void startAnimation(int i) {
            this.mAnimator.cancel();
            setAnimationList();
            Log.i("OpGraphLight", "startAnimation");
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            this.mLp = layoutParams;
            layoutParams.width = i;
            setLayoutParams(layoutParams);
            setImageResource(((Integer) this.mStartAnimationAssets1.get(0)).intValue());
            this.mAnimator.start();
        }

        /* access modifiers changed from: protected */
        public void setAnimationList() {
            Log.i("OpGraphLight", "setAnimationList (clear & add)");
            this.mStartAnimationAssets1.clear();
            for (int i = 0; i < this.mAnimationArray.length(); i++) {
                this.mStartAnimationAssets1.add(Integer.valueOf(this.mAnimationArray.getResourceId(i, 0)));
            }
        }

        /* access modifiers changed from: protected */
        public void relaseAnimationList() {
            Log.i("OpGraphLight", "relaseAnimationList");
            setImageDrawable(null);
            this.mAnimationArray.recycle();
            this.mStartAnimationAssets1.clear();
        }
    }

    /* access modifiers changed from: protected */
    public class AnimateImageView extends FrontCameraAnimateBaseImageView {
        private final ValueAnimator mAlphaInAnimator;
        private final ValueAnimator mAlphaInAnimatorDisappear;

        public AnimateImageView(Context context) {
            super(context);
            this.mAnimationArray = ((ImageView) this).mContext.getResources().obtainTypedArray(C0001R$array.op_light_start_animation);
            ValueAnimator duration = ValueAnimator.ofInt(0, 225).setDuration(225L);
            this.mAlphaInAnimator = duration;
            duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(OpGraphLight.this) { // from class: com.oneplus.anim.OpGraphLight.AnimateImageView.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                    AnimateImageView.this.checkOrientationType();
                    if (intValue <= 225) {
                        AnimateImageView animateImageView = AnimateImageView.this;
                        animateImageView.mLp = animateImageView.getLayoutParams();
                        AnimateImageView animateImageView2 = AnimateImageView.this;
                        animateImageView2.mLp.width = (int) ((((float) OpGraphLight.this.mAnimateImageWidth) * 0.5f) + (((float) OpGraphLight.this.mAnimateImageWidth) * 0.5f * (((float) intValue) / 225.0f)));
                        AnimateImageView animateImageView3 = AnimateImageView.this;
                        animateImageView3.setLayoutParams(animateImageView3.mLp);
                    }
                    if (intValue <= 150) {
                        AnimateImageView.this.setImageAlpha((int) ((((float) intValue) * 255.0f) / 150.0f));
                    }
                }
            });
            this.mAlphaInAnimator.setInterpolator(new PathInterpolator(0.0f, 0.0f, 0.2f, 1.0f));
            ValueAnimator duration2 = ValueAnimator.ofInt(0, 150).setDuration(150L);
            this.mAlphaInAnimatorDisappear = duration2;
            duration2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(OpGraphLight.this) { // from class: com.oneplus.anim.OpGraphLight.AnimateImageView.2
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int intValue = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                    if (intValue <= 150) {
                        AnimateImageView animateImageView = AnimateImageView.this;
                        animateImageView.mLp = animateImageView.getLayoutParams();
                        AnimateImageView animateImageView2 = AnimateImageView.this;
                        animateImageView2.mLp.width = (int) (((float) OpGraphLight.this.mAnimateImageWidth) * (1.0f - (((float) intValue) / 150.0f)));
                        AnimateImageView animateImageView3 = AnimateImageView.this;
                        animateImageView3.setLayoutParams(animateImageView3.mLp);
                    }
                    if (intValue >= 50) {
                        AnimateImageView.this.setImageAlpha((int) ((1.0f - (((float) (intValue - 50)) / 100.0f)) * 255.0f));
                    }
                }
            });
            this.mAlphaInAnimatorDisappear.setInterpolator(new PathInterpolator(0.8f, 0.0f, 1.0f, 1.0f));
            this.mAlphaInAnimatorDisappear.setStartDelay(225);
            AnimatorSet animatorSet = new AnimatorSet();
            this.mAnimator = animatorSet;
            animatorSet.play(this.mAlphaInAnimator).before(this.mAlphaInAnimatorDisappear);
            this.mAnimator.addListener(new AnimatorListenerAdapter(OpGraphLight.this) { // from class: com.oneplus.anim.OpGraphLight.AnimateImageView.3
                boolean mCancelled;

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    this.mCancelled = false;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    this.mCancelled = true;
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (!this.mCancelled) {
                        Log.i("OpGraphLight", "onAnimationEnd & !mCancelled");
                        AnimateImageView.this.relaseAnimationList();
                        OpGraphLight.this.hide();
                    }
                }
            });
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.ImageView, android.view.View
        public void onAttachedToWindow() {
            startAnimation((int) (((float) OpGraphLight.this.mAnimateImageWidth) * 0.5f));
        }
    }

    /* access modifiers changed from: private */
    public class AnimateLottieView extends LottieAnimationView {
        private Context mLottieContext;
        private LottieDrawable mLottieDrawable;
        private int mOrientationType;
        private boolean mPendingRelease = false;

        public AnimateLottieView(Context context) {
            super(context);
            this.mLottieContext = context;
            setScaleType(ImageView.ScaleType.FIT_CENTER);
            LottieDrawable createLottieDrawable = createLottieDrawable();
            this.mLottieDrawable = createLottieDrawable;
            setImageDrawable(createLottieDrawable);
            this.mOrientationType = getOrientation();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void loadAndPlay(final boolean z) {
            LottieComposition.Factory.fromAssetFileName(this.mLottieContext, getLottieFrontCamAnimFile(), new OnCompositionLoadedListener() { // from class: com.oneplus.anim.OpGraphLight.AnimateLottieView.1
                @Override // com.airbnb.lottie.OnCompositionLoadedListener
                public void onCompositionLoaded(LottieComposition lottieComposition) {
                    if (AnimateLottieView.this.mLottieDrawable != null) {
                        AnimateLottieView.this.mLottieDrawable.setComposition(lottieComposition);
                        AnimateLottieView animateLottieView = AnimateLottieView.this;
                        animateLottieView.mOrientationType = animateLottieView.getOrientation();
                        int i = SystemProperties.getInt("debug.frontcamera.alpha", 999);
                        if (i != 999) {
                            AnimateLottieView.this.setAlpha(i);
                        } else {
                            AnimateLottieView animateLottieView2 = AnimateLottieView.this;
                            animateLottieView2.setAlpha(OpGraphLight.this.mAlphaMode);
                        }
                        Log.v("OpGraphLight", "loadAndPlay" + z + " Alpha " + AnimateLottieView.this.mLottieDrawable.getAlpha());
                        AnimateLottieView.this.mLottieDrawable.playAnimation();
                        if (z) {
                            AnimateLottieView.this.mLottieDrawable.setProgress(1.0f);
                        }
                    }
                }
            });
        }

        @Override // android.widget.ImageView
        public void setAlpha(int i) {
            LottieDrawable lottieDrawable = this.mLottieDrawable;
            if (lottieDrawable != null) {
                lottieDrawable.setAlpha(OpGraphLight.this.mAlphaMode);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getOrientation() {
            Display defaultDisplay = OpGraphLight.this.mWm.getDefaultDisplay();
            if (defaultDisplay == null) {
                return 0;
            }
            return defaultDisplay.getRotation();
        }

        public boolean checkOrientationType() {
            int orientation = getOrientation();
            if (this.mOrientationType == orientation) {
                return true;
            }
            Log.v("OpGraphLight", "detect checkOrientationType() / rotation:" + orientation + " / mOrientationType:" + this.mOrientationType + " mViewAdded " + OpGraphLight.this.mViewAdded);
            this.mOrientationType = orientation;
            if (!OpGraphLight.this.mViewAdded) {
                return false;
            }
            release(true);
            OpGraphLight.this.show(true);
            return false;
        }

        private LottieDrawable createLottieDrawable() {
            LottieDrawable lottieDrawable = new LottieDrawable();
            lottieDrawable.removeAllAnimatorListeners();
            lottieDrawable.addAnimatorListener(new Animator.AnimatorListener() { // from class: com.oneplus.anim.OpGraphLight.AnimateLottieView.2
                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    Log.d("OpGraphLight", "LottieStart.");
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    boolean isFrontCameraAnimOn = OpGraphLight.this.isFrontCameraAnimOn();
                    Log.d("OpGraphLight", "LottieEnd " + AnimateLottieView.this.mPendingRelease + " isAnimOn " + isFrontCameraAnimOn);
                    if (AnimateLottieView.this.mPendingRelease || !isFrontCameraAnimOn) {
                        AnimateLottieView.this.mPendingRelease = false;
                        AnimateLottieView.this.release(false);
                    }
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                    Log.d("OpGraphLight", "LottieCancel.");
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animator) {
                    Log.d("OpGraphLight", "LottieRepeat.");
                }
            });
            return lottieDrawable;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void release(boolean z) {
            LottieDrawable lottieDrawable = this.mLottieDrawable;
            if (lottieDrawable != null) {
                if (!lottieDrawable.isAnimating() || z) {
                    destroyLottieDrawable();
                    OpGraphLight.this.hide();
                    return;
                }
                this.mPendingRelease = true;
            }
        }

        public void destroyLottieDrawable() {
            Log.d("OpGraphLight", "destroyLottieDrawable.");
            LottieDrawable lottieDrawable = this.mLottieDrawable;
            if (lottieDrawable != null) {
                lottieDrawable.removeAllAnimatorListeners();
                setImageDrawable(null);
                this.mLottieDrawable.clearComposition();
                this.mLottieDrawable = null;
            }
        }

        private String getLottieFrontCamAnimFile() {
            StringBuilder sb;
            String string = ((ImageView) this).mContext.getResources().getString(C0015R$string.op_front_camera_animation_file_prefix);
            if (!TextUtils.isEmpty(string)) {
                sb = new StringBuilder(string);
            } else {
                sb = new StringBuilder(((ImageView) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_front_camera_animation_default_resource_resolution) == 1440 ? "120_" : "100_");
            }
            if (!OpGraphLight.this.mSupportDarkMode) {
                sb.append(".json");
            } else if (OpGraphLight.this.mDarkMode) {
                sb.append("dark.json");
            } else {
                sb.append("light.json");
            }
            Log.d("OpGraphLight", sb.toString());
            return sb.toString();
        }
    }

    public boolean isFrontCameraAnimOn() {
        StatusBar statusBar = this.mStatusBar;
        return (statusBar == null || statusBar.getOpCameraAnimateController() == null || !this.mStatusBar.getOpCameraAnimateController().isFrontCameraAnimOn()) ? false : true;
    }

    private void enableLightSensor() {
        if (this.mSensorManager == null) {
            this.mSensorManager = new SystemSensorManager(this.mContext, Looper.getMainLooper());
        }
        if (SystemProperties.getBoolean("debug.frontcamera.lightsensor.enable", true)) {
            LUX_THRESHOLD_MIDDLE = (float) SystemProperties.getInt("debug.frontcamera.lux.threshold.middle", 20);
            LUX_THRESHOLD_HIGH = (float) SystemProperties.getInt("debug.frontcamera.lux.threshold.high", 100);
            ALPHA_MODE_HIGH = SystemProperties.getInt("debug.frontcamera.alpha.mode.high", 255);
            SystemProperties.getInt("debug.frontcamera.alpha.mode.middle", 150);
            ALPHA_MODE_LOW = SystemProperties.getInt("debug.frontcamera.alpha.low", 100);
            if (!this.mLightSensorRegistered) {
                Sensor defaultSensor = this.mSensorManager.getDefaultSensor(5);
                this.mSensor = defaultSensor;
                this.mSensorManager.registerListener(this.mSensorEventListener, defaultSensor, 3);
                this.mLightSensorRegistered = true;
            }
        }
    }

    private void disableLightSensor() {
        if (this.mSensorManager == null) {
            this.mSensorManager = new SystemSensorManager(this.mContext, Looper.getMainLooper());
        }
        if (this.mLightSensorRegistered) {
            this.mSensorManager.unregisterListener(this.mSensorEventListener);
            this.mLightSensorRegistered = false;
        }
    }
}
