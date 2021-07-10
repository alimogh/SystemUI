package com.oneplus.systemui.biometrics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;
public class OpFingerprintAnimationView extends ImageView {
    private AnimationDrawable mBackground;
    private OpFingerprintAnimationCtrl mFingerprintAnimationCtrl;
    private Handler mHandler = new Handler() { // from class: com.oneplus.systemui.biometrics.OpFingerprintAnimationView.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            int i = message.what;
            if (i == 1) {
                OpFingerprintAnimationView.this.stopAnimation();
            } else if (i == 2 && OpFingerprintAnimationView.this.mFingerprintAnimationCtrl != null) {
                OpFingerprintAnimationView.this.mFingerprintAnimationCtrl.playAnimation(message.arg1);
            }
        }
    };

    public OpFingerprintAnimationView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    public OpFingerprintAnimationView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public OpFingerprintAnimationView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        AnimationDrawable animationDrawable = (AnimationDrawable) getBackground();
        this.mBackground = animationDrawable;
        if (animationDrawable != null) {
            animationDrawable.getNumberOfFrames();
            this.mBackground.getDuration(0);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void stopAnimation() {
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        AnimationDrawable animationDrawable = this.mBackground;
        if (animationDrawable != null) {
            animationDrawable.stop();
        }
        setVisibility(4);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}
