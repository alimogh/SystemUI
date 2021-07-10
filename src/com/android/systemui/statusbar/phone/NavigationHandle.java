package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.oneplus.systemui.statusbar.phone.OpNavigationHandle;
import com.oneplus.util.OpUtils;
public class NavigationHandle extends OpNavigationHandle implements ButtonInterface {
    public static final boolean DEBUG = Log.isLoggable("NavigationHandle", 3);
    private ValueAnimator mBarColorAnimator;
    protected int mBottom;
    private int mBottomLand;
    private Context mContext;
    private int mCurrentColor;
    private final int mDarkColor;
    private float mDarkIntensity;
    private final int mFullScreenDarkColor;
    private final int mFullScreenLightColor;
    private Handler mHandler;
    private boolean mIsFullScreenColor;
    private boolean mIsVertical;
    private int mLandscapeWidth;
    private final int mLightColor;
    protected final Paint mPaint;
    private int mPortraitWidth;
    private boolean mPreTopAppInFullScreenList;
    protected int mRadius;
    private boolean mRequiresInvalidate;
    private boolean mShouldChangeColor;
    private int mTargetColor;
    private Runnable mUpdateColorTask;

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void abortCurrentGesture() {
    }

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void setDelayTouchFeedback(boolean z) {
    }

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void setImageDrawable(Drawable drawable) {
    }

    public NavigationHandle(Context context) {
        this(context, null);
    }

    public NavigationHandle(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaint = new Paint();
        this.mIsVertical = true;
        this.mShouldChangeColor = false;
        this.mPreTopAppInFullScreenList = false;
        this.mIsFullScreenColor = false;
        this.mDarkIntensity = -1.0f;
        this.mContext = context;
        Resources resources = context.getResources();
        this.mRadius = resources.getDimensionPixelSize(C0005R$dimen.navigation_handle_radius);
        this.mBottom = resources.getDimensionPixelSize(17105327);
        this.mBottomLand = resources.getDimensionPixelSize(C0005R$dimen.navigation_handle_bottom);
        this.mPortraitWidth = resources.getDimensionPixelSize(C0005R$dimen.navigation_home_handle_width);
        this.mLandscapeWidth = resources.getDimensionPixelSize(C0005R$dimen.navigation_home_handle_width_land);
        this.mLightColor = resources.getColor(C0004R$color.op_home_handle_light_color);
        this.mDarkColor = resources.getColor(C0004R$color.op_home_handle_dark_color);
        this.mFullScreenLightColor = resources.getColor(C0004R$color.op_home_handle_fullscreen_light_color);
        this.mFullScreenDarkColor = resources.getColor(C0004R$color.op_home_handle_fullscreen_dark_color);
        this.mPaint.setAntiAlias(true);
        setFocusable(false);
        this.mTargetColor = this.mDarkColor;
        this.mHandler = new Handler();
        ValueAnimator valueAnimator = new ValueAnimator();
        this.mBarColorAnimator = valueAnimator;
        valueAnimator.setFloatValues(0.0f, 1.0f);
        this.mBarColorAnimator.setDuration((long) SystemProperties.getInt("persist.homebar.anim.time", 300));
        this.mBarColorAnimator.addListener(new Animator.AnimatorListener() { // from class: com.android.systemui.statusbar.phone.NavigationHandle.1
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
                NavigationHandle.this.mShouldChangeColor = false;
            }
        });
        this.mBarColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.statusbar.phone.NavigationHandle.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                NavigationHandle.this.mPaint.setColor(((Integer) ArgbEvaluator.getInstance().evaluate(((Float) valueAnimator2.getAnimatedValue()).floatValue(), Integer.valueOf(NavigationHandle.this.mCurrentColor), Integer.valueOf(NavigationHandle.this.mTargetColor))).intValue());
                NavigationHandle.this.invalidate();
            }
        });
        this.mUpdateColorTask = new Runnable() { // from class: com.android.systemui.statusbar.phone.NavigationHandle.3
            @Override // java.lang.Runnable
            public void run() {
                boolean z = false;
                if (NavigationHandle.this.mDarkIntensity == 1.0f) {
                    Log.d("NavigationHandle", "Dark home handle");
                    NavigationHandle.this.mCurrentColor = OpUtils.isInFullScreenListApp() ? NavigationHandle.this.mDarkColor : NavigationHandle.this.mFullScreenDarkColor;
                    NavigationHandle.this.mTargetColor = OpUtils.isInFullScreenListApp() ? NavigationHandle.this.mFullScreenDarkColor : NavigationHandle.this.mDarkColor;
                    NavigationHandle navigationHandle = NavigationHandle.this;
                    if (navigationHandle.mTargetColor == NavigationHandle.this.mFullScreenDarkColor) {
                        z = true;
                    }
                    navigationHandle.mIsFullScreenColor = z;
                    NavigationHandle.this.mBarColorAnimator.start();
                } else if (NavigationHandle.this.mDarkIntensity == 0.0f) {
                    Log.d("NavigationHandle", "Light home handle");
                    NavigationHandle.this.mCurrentColor = OpUtils.isInFullScreenListApp() ? NavigationHandle.this.mLightColor : NavigationHandle.this.mFullScreenLightColor;
                    NavigationHandle.this.mTargetColor = OpUtils.isInFullScreenListApp() ? NavigationHandle.this.mFullScreenLightColor : NavigationHandle.this.mLightColor;
                    NavigationHandle navigationHandle2 = NavigationHandle.this;
                    if (navigationHandle2.mTargetColor == NavigationHandle.this.mFullScreenLightColor) {
                        z = true;
                    }
                    navigationHandle2.mIsFullScreenColor = z;
                    NavigationHandle.this.mBarColorAnimator.start();
                } else {
                    Log.d("NavigationHandle", "no action");
                    NavigationHandle.this.mPreTopAppInFullScreenList = !OpUtils.isInFullScreenListApp();
                }
            }
        };
    }

    @Override // android.view.View
    public void setAlpha(float f) {
        super.setAlpha(f);
        if (f > 0.0f && this.mRequiresInvalidate) {
            this.mRequiresInvalidate = false;
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int i = this.mRadius * 3;
        float f = ((float) i) / 2.0f;
        int width = getWidth();
        int i2 = this.mIsVertical ? height - ((this.mBottom + i) / 2) : (height - this.mBottomLand) - i;
        canvas.drawRoundRect(0.0f, (float) i2, (float) width, (float) (i2 + i), f, f, this.mPaint);
    }

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void setVertical(boolean z) {
        updateDisplaySize();
        this.mIsVertical = z;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
        layoutParams.width = this.mIsVertical ? this.mPortraitWidth : this.mLandscapeWidth;
        setLayoutParams(layoutParams);
    }

    @Override // com.android.systemui.statusbar.phone.ButtonInterface
    public void setDarkIntensity(float f) {
        if (DEBUG && Build.DEBUG_ONEPLUS) {
            Log.i("NavigationHandle", " setDarkIntensity:" + f + "," + Debug.getCallers(7));
        }
        if (this.mPreTopAppInFullScreenList != OpUtils.isInFullScreenListApp() && (f == 0.0f || f == 1.0f)) {
            Log.i("NavigationHandle", "mShouldChangeColor PreTopInFull:" + this.mPreTopAppInFullScreenList + " currentIsInFullScreenList:" + OpUtils.isInFullScreenListApp() + " intensity:" + f);
            this.mPreTopAppInFullScreenList = OpUtils.isInFullScreenListApp();
            this.mShouldChangeColor = true;
            triggerChangeColorAnimation();
        }
        int intValue = ((Integer) ArgbEvaluator.getInstance().evaluate(f, Integer.valueOf(this.mIsFullScreenColor ? this.mFullScreenLightColor : this.mLightColor), Integer.valueOf(this.mIsFullScreenColor ? this.mFullScreenDarkColor : this.mDarkColor))).intValue();
        if (this.mPaint.getColor() != intValue || this.mShouldChangeColor) {
            this.mPaint.setColor(intValue);
            if (getVisibility() != 0 || getAlpha() <= 0.0f) {
                this.mRequiresInvalidate = true;
            } else {
                invalidate();
            }
        }
        if (this.mDarkIntensity != f) {
            this.mDarkIntensity = f;
        }
    }

    private void updateDisplaySize() {
        this.mRadius = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.navigation_handle_radius);
        this.mBottom = this.mContext.getResources().getDimensionPixelSize(17105327);
        this.mBottomLand = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.navigation_handle_bottom);
        this.mPortraitWidth = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.navigation_home_handle_width);
        this.mLandscapeWidth = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.navigation_home_handle_width_land);
    }

    public void triggerChangeColorAnimation() {
        Log.d("NavigationHandle", "triggerChangeColorAnimation");
        if ((!this.mIsFullScreenColor || OpUtils.isInFullScreenListApp()) && (this.mIsFullScreenColor || !OpUtils.isInFullScreenListApp())) {
            Log.d("NavigationHandle", "current color is match top app. no need to change color");
            this.mShouldChangeColor = false;
            this.mHandler.removeCallbacks(this.mUpdateColorTask);
            return;
        }
        this.mHandler.removeCallbacks(this.mUpdateColorTask);
        this.mHandler.postDelayed(this.mUpdateColorTask, (long) SystemProperties.getInt("persist.homebar.task.delay", 50));
    }
}
