package com.oneplus.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintController;
import com.oneplus.util.OpUtils;
public class OpCarModeHintView extends FrameLayout implements OpHighlightHintController.OnHighlightHintStateChangeListener, ConfigurationController.ConfigurationListener {
    private static final Interpolator ANIMATION_INTERPILATOR = new PathInterpolator(0.4f, 0.0f, 0.6f, 1.0f);
    public static final boolean SHOW_OVAL_LAYOUT = OpUtils.isSupportCustomStatusBar();
    private static String TAG = "CarModeHintView";
    private Context mContext;
    private TextView mHint;
    private boolean mHintShow;
    private int mOrientation;
    private Animator mShowAnimation;

    private void updateLayout() {
    }

    public OpCarModeHintView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public OpCarModeHintView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mHintShow = false;
        this.mContext = context;
        addView(LayoutInflater.from(context).inflate(SHOW_OVAL_LAYOUT ? C0011R$layout.carmode_highlight_hint_view_notch : C0011R$layout.carmode_highlight_view_without_notch, (ViewGroup) null));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mHint = (TextView) findViewById(C0008R$id.notification_text);
        updateLayout();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
    }

    private void updateHint() {
        Notification notification;
        OpHighlightHintController opHighlightHintController = (OpHighlightHintController) Dependency.get(OpHighlightHintController.class);
        if (opHighlightHintController != null && opHighlightHintController.getCarModeHighlightHintNotification() != null && (notification = opHighlightHintController.getCarModeHighlightHintNotification().getNotification()) != null) {
            int backgroundColorOnStatusBar = notification.getBackgroundColorOnStatusBar();
            try {
                Resources resourcesForApplication = this.mContext.getPackageManager().getResourcesForApplication(opHighlightHintController.getCarModeHighlightHintNotification().getPackageName());
                if (this.mHint != null) {
                    if (notification.getTextOnStatusBar() != -1) {
                        StringBuffer stringBuffer = new StringBuffer(resourcesForApplication.getString(notification.getTextOnStatusBar()));
                        stringBuffer.append(" ");
                        this.mHint.setText(stringBuffer);
                        this.mHint.setVisibility(0);
                    } else {
                        this.mHint.setVisibility(8);
                    }
                    this.mHint.setTextSize(0, (float) getContext().getResources().getDimensionPixelOffset(C0005R$dimen.op_car_mode_highlight_hint_text_size));
                }
                setBackgroundColor(backgroundColorOnStatusBar);
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

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController.OnHighlightHintStateChangeListener
    public void onCarModeHighlightHintStateChange(boolean z) {
        boolean isCarModeHighlightHintSHow = ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isCarModeHighlightHintSHow();
        if (Build.DEBUG_ONEPLUS) {
            String str = TAG;
            Log.i(str, "onCarModeHighlightHintStateChange show:" + isCarModeHighlightHintSHow);
        }
        if (isCarModeHighlightHintSHow) {
            updateHint();
            show(z);
            return;
        }
        hide();
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController.OnHighlightHintStateChangeListener
    public void onCarModeHighlightHintInfoChange() {
        updateHint();
    }

    public void show(boolean z) {
        if (Build.DEBUG_ONEPLUS) {
            String str = TAG;
            Log.i(str, "show:" + z);
        }
        if (!z) {
            setVisibility(0);
        } else if (!this.mHintShow) {
            Animator animator = this.mShowAnimation;
            if (animator != null) {
                animator.cancel();
            } else {
                this.mShowAnimation = getShowAnimation();
            }
            this.mShowAnimation.start();
        }
    }

    public void hide() {
        if (Build.DEBUG_ONEPLUS) {
            Log.i(TAG, "hide");
        }
        if (this.mHintShow) {
            this.mHintShow = false;
            Animator animator = this.mShowAnimation;
            if (animator != null) {
                animator.cancel();
            }
        }
        setVisibility(8);
    }

    private Animator getShowAnimation() {
        new ValueAnimator();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setStartDelay(75);
        ofFloat.setDuration(150L);
        ofFloat.setInterpolator(ANIMATION_INTERPILATOR);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.systemui.statusbar.phone.OpCarModeHintView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OpCarModeHintView.this.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        ofFloat.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.systemui.statusbar.phone.OpCarModeHintView.2
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.i(OpCarModeHintView.TAG, "show");
                }
                OpCarModeHintView.this.mHintShow = true;
                OpCarModeHintView.this.setAlpha(0.0f);
                OpCarModeHintView.this.setVisibility(0);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                OpCarModeHintView.this.setAlpha(1.0f);
            }
        });
        return ofFloat;
    }
}
