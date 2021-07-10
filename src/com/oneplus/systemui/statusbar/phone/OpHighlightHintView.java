package com.oneplus.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintController;
import com.oneplus.util.OpUtils;
public class OpHighlightHintView extends FrameLayout implements OpHighlightHintController.OnHighlightHintStateChangeListener, ConfigurationController.ConfigurationListener {
    private static final boolean OP_DEBUG = Build.DEBUG_ONEPLUS;
    AnimatorSet breathingAnimatorSet;
    private int mBackgroundColor;
    Drawable mBackgroundDrawable;
    FrameLayout mChronometerContainer;
    private ViewGroup mContainer;
    int mContentWidth;
    private Context mContext;
    private TextView mHint;
    private ImageView mIconView;
    private int mOrientation;
    private boolean mShowBreathingEffect;
    private boolean mShowHighlightHint;

    public OpHighlightHintView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public OpHighlightHintView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mShowBreathingEffect = false;
        this.mShowHighlightHint = false;
        this.mContentWidth = 0;
        this.mContext = context;
        addView(LayoutInflater.from(context).inflate(C0011R$layout.highlight_hint_view_notch, (ViewGroup) null));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIconView = (ImageView) findViewById(C0008R$id.highlight_hint_notification_icon);
        this.mHint = (TextView) findViewById(C0008R$id.highlight_hint_notification_text);
        this.mChronometerContainer = (FrameLayout) findViewById(C0008R$id.highlight_hint_view_container);
        ViewGroup viewGroup = (ViewGroup) findViewById(C0008R$id.highlight_hint_container);
        this.mContainer = viewGroup;
        if (viewGroup != null) {
            Drawable background = viewGroup.getBackground();
            this.mBackgroundDrawable = background;
            background.mutate();
        }
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
        updateLayout();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        ViewGroup viewGroup = this.mContainer;
        if (viewGroup != null) {
            ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
            int width = getWidth() - getPaddingStart();
            if (layoutParams.width > width) {
                layoutParams.width = width;
                this.mContainer.setLayoutParams(layoutParams);
            }
        }
    }

    private void updateLayout() {
        ViewGroup viewGroup = this.mContainer;
        if (viewGroup != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewGroup.getLayoutParams();
            layoutParams.width = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.highlight_hint_width_notch);
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.highlight_hint_bg_height);
            int dimensionPixelSize2 = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.status_bar_height) - (this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.highlight_hint_bg_vertical_padding) * 2);
            if (dimensionPixelSize > dimensionPixelSize2) {
                dimensionPixelSize = dimensionPixelSize2;
            }
            layoutParams.height = dimensionPixelSize;
            layoutParams.gravity = 17;
            setPaddingRelative(OpUtils.isSupportHolePunchFrontCam() ? 0 : this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.highlight_hint_margin_start), getPaddingTop(), getPaddingTop(), getPaddingEnd());
            this.mContainer.setLayoutParams(layoutParams);
            ImageView imageView = this.mIconView;
            if (imageView != null) {
                imageView.getLayoutParams().height = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.highlight_hint_icon_size_notch);
                this.mIconView.getLayoutParams().width = -1;
                this.mIconView.requestLayout();
            }
            ((GradientDrawable) this.mContainer.getBackground()).setCornerRadius((float) this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.highlight_hint_bg_radius));
        }
    }

    private void updateHint() {
        Notification notification;
        OpHighlightHintController opHighlightHintController = (OpHighlightHintController) Dependency.get(OpHighlightHintController.class);
        if (opHighlightHintController != null && opHighlightHintController.getHighlightHintNotification() != null && (notification = opHighlightHintController.getHighlightHintNotification().getNotification()) != null) {
            try {
                this.mShowBreathingEffect = notification.extras.getBoolean("op_show_breathing");
                int i = notification.extras.getInt("op_breathing_color_start");
                int i2 = notification.extras.getInt("op_breathing_color_end");
                if (OP_DEBUG) {
                    Log.d("HighlightHintView", "updateHint, mShowBreathingEffect " + this.mShowBreathingEffect + " breathingColorStart " + i + " breathingColorEnd " + i2);
                }
                if (this.mShowBreathingEffect) {
                    playBreathingAnimation(i, i2);
                } else {
                    if (this.breathingAnimatorSet != null && this.breathingAnimatorSet.isRunning()) {
                        cancelAnimation();
                    }
                    int backgroundColorOnStatusBar = notification.getBackgroundColorOnStatusBar();
                    this.mBackgroundColor = backgroundColorOnStatusBar;
                    if (this.mBackgroundDrawable != null) {
                        this.mBackgroundDrawable.setColorFilter(backgroundColorOnStatusBar, PorterDuff.Mode.SRC_ATOP);
                    }
                }
                Resources resourcesForApplication = this.mContext.getPackageManager().getResourcesForApplication(opHighlightHintController.getHighlightHintNotification().getPackageName());
                if (notification.getStatusBarIcon() != -1) {
                    this.mIconView.setImageDrawable(resourcesForApplication.getDrawable(notification.getStatusBarIcon(), null));
                    this.mIconView.setVisibility(0);
                } else {
                    this.mIconView.setVisibility(8);
                }
                int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.status_bar_clock_size);
                if (this.mHint != null) {
                    this.mHint.setTextSize(0, (float) dimensionPixelSize);
                    if (notification.getTextOnStatusBar() != -1) {
                        StringBuffer stringBuffer = new StringBuffer(resourcesForApplication.getString(notification.getTextOnStatusBar()));
                        stringBuffer.append(" ");
                        this.mHint.setText(stringBuffer);
                        int maxHighlightHintTextWidth = getMaxHighlightHintTextWidth();
                        if (maxHighlightHintTextWidth > 0) {
                            this.mHint.setMaxWidth(maxHighlightHintTextWidth);
                        }
                        this.mHint.setVisibility(0);
                    } else {
                        this.mHint.setVisibility(8);
                    }
                }
                if (this.mChronometerContainer == null) {
                    return;
                }
                if (notification.ShowChronometerOnStatusBar()) {
                    this.mChronometerContainer.removeAllViews();
                    int color = this.mContext.getResources().getColor(C0004R$color.highlight_hint_view_chronometer_text_color);
                    Chronometer statusBarChronometer = ((Integer) getTag()).intValue() == 1000 ? opHighlightHintController.getStatusBarChronometer() : opHighlightHintController.getKeyguardChronometer();
                    if (statusBarChronometer != null) {
                        if (statusBarChronometer.getParent() != null) {
                            ((ViewGroup) statusBarChronometer.getParent()).removeView(statusBarChronometer);
                        }
                        statusBarChronometer.setTextSize(0, (float) dimensionPixelSize);
                        statusBarChronometer.setTextColor(color);
                        statusBarChronometer.setEllipsize(TextUtils.TruncateAt.END);
                        this.mChronometerContainer.addView(statusBarChronometer);
                    }
                    this.mChronometerContainer.setVisibility(0);
                    return;
                }
                this.mChronometerContainer.setVisibility(8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
            updateLayout();
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        updateLayout();
        updateHint();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).addCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).removeCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    private int getMaxHighlightHintTextWidth() {
        OpHighlightHintController opHighlightHintController = (OpHighlightHintController) Dependency.get(OpHighlightHintController.class);
        int i = 0;
        if (opHighlightHintController == null) {
            return 0;
        }
        Chronometer statusBarChronometer = ((Integer) getTag()).intValue() == 1000 ? opHighlightHintController.getStatusBarChronometer() : opHighlightHintController.getKeyguardChronometer();
        if (statusBarChronometer == null) {
            return 0;
        }
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(0, 0);
        int measuredWidth = statusBarChronometer.getMeasuredWidth();
        if (measuredWidth == 0 || this.mContentWidth != measuredWidth) {
            statusBarChronometer.measure(makeMeasureSpec, makeMeasureSpec2);
            this.mContentWidth = measuredWidth;
        }
        int measuredWidth2 = statusBarChronometer.getMeasuredWidth();
        int measuredWidth3 = this.mIconView.getMeasuredWidth();
        Context context = this.mContext;
        if (!(context == null || context.getResources() == null)) {
            i = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.highlighthint_chronometer_padding);
        }
        int width = ((getWidth() - measuredWidth2) - measuredWidth3) - i;
        return width > 0 ? width : (int) (((double) getWidth()) * 0.3d);
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController.OnHighlightHintStateChangeListener
    public void onHighlightHintStateChange() {
        if (((Integer) getTag()).intValue() == 1000) {
            boolean isHighLightHintShow = ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isHighLightHintShow();
            this.mShowHighlightHint = isHighLightHintShow;
            if (isHighLightHintShow) {
                updateHint();
                setVisibility(0);
                return;
            }
            setVisibility(8);
            cancelAnimation();
        }
    }

    private void cancelAnimation() {
        AnimatorSet animatorSet = this.breathingAnimatorSet;
        if (animatorSet != null) {
            animatorSet.removeAllListeners();
            this.breathingAnimatorSet.cancel();
            this.breathingAnimatorSet = null;
        }
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController.OnHighlightHintStateChangeListener
    public void onHighlightHintInfoChange() {
        updateHint();
    }

    private void playBreathingAnimation(int i, int i2) {
        if (this.breathingAnimatorSet == null) {
            this.breathingAnimatorSet = new AnimatorSet();
        }
        if (!this.breathingAnimatorSet.isRunning()) {
            AnonymousClass1 r0 = new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.systemui.statusbar.phone.OpHighlightHintView.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    OpHighlightHintView.this.mBackgroundColor = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                    OpHighlightHintView opHighlightHintView = OpHighlightHintView.this;
                    opHighlightHintView.mBackgroundDrawable.setColorFilter(opHighlightHintView.mBackgroundColor, PorterDuff.Mode.SRC_ATOP);
                }
            };
            ValueAnimator ofArgb = ValueAnimator.ofArgb(i, i2);
            ofArgb.setDuration(1000L);
            ofArgb.addUpdateListener(r0);
            ofArgb.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
            ValueAnimator ofArgb2 = ValueAnimator.ofArgb(i2, i);
            ofArgb2.setDuration(1000L);
            ofArgb2.addUpdateListener(r0);
            ofArgb2.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            this.breathingAnimatorSet.play(ofArgb2).after(ofArgb);
            this.breathingAnimatorSet.setInterpolator(new LinearInterpolator());
            this.breathingAnimatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.oneplus.systemui.statusbar.phone.OpHighlightHintView.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    if (OpHighlightHintView.this.mShowHighlightHint) {
                        OpHighlightHintView opHighlightHintView = OpHighlightHintView.this;
                        if (opHighlightHintView.breathingAnimatorSet != null && opHighlightHintView.mShowBreathingEffect) {
                            OpHighlightHintView.this.breathingAnimatorSet.start();
                            if (OpHighlightHintView.OP_DEBUG) {
                                Log.d("HighlightHintView", "playBreathingAnimation, onAnimationEnd: Restart breathing animation");
                            }
                        }
                    }
                }
            });
            this.breathingAnimatorSet.start();
            if (OP_DEBUG) {
                Log.d("HighlightHintView", "playBreathingAnimation: Start breathing animation");
            }
        }
    }
}
