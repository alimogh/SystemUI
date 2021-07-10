package com.android.keyguard;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.keyguard.CarrierTextController;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.oneplus.systemui.util.OpDataUsageUtils;
import java.util.Locale;
public class CarrierText extends TextView {
    private static CharSequence mSeparator;
    private CarrierTextController.CarrierTextCallback mCarrierTextCallback;
    private CarrierTextController mCarrierTextController;
    private OpDataUsageUtils mOpDataUsageUtils;
    private boolean mShouldMarquee;
    private boolean mShowAirplaneMode;
    private boolean mShowMissingSim;

    public CarrierText(Context context) {
        this(context, null);
    }

    /* JADX INFO: finally extract failed */
    public CarrierText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCarrierTextCallback = new CarrierTextController.CarrierTextCallback() { // from class: com.android.keyguard.CarrierText.1
            @Override // com.android.keyguard.CarrierTextController.CarrierTextCallback
            public void updateCarrierInfo(CarrierTextController.CarrierTextCallbackInfo carrierTextCallbackInfo) {
                if (((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isSimPinSecure()) {
                    CarrierText.this.setText(carrierTextCallbackInfo.carrierText);
                } else {
                    CarrierText.this.mOpDataUsageUtils.setCarrierText(carrierTextCallbackInfo);
                }
            }

            @Override // com.android.keyguard.CarrierTextController.CarrierTextCallback
            public void startedGoingToSleep() {
                CarrierText.this.setSelected(false);
            }

            @Override // com.android.keyguard.CarrierTextController.CarrierTextCallback
            public void finishedWakingUp() {
                CarrierText.this.setSelected(true);
            }
        };
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.CarrierText, 0, 0);
        try {
            boolean z = obtainStyledAttributes.getBoolean(R$styleable.CarrierText_allCaps, false);
            this.mShowAirplaneMode = obtainStyledAttributes.getBoolean(R$styleable.CarrierText_showAirplaneMode, false);
            this.mShowMissingSim = obtainStyledAttributes.getBoolean(R$styleable.CarrierText_showMissingSim, false);
            obtainStyledAttributes.recycle();
            setTransformationMethod(new CarrierTextTransformationMethod(this, ((TextView) this).mContext, z));
            this.mOpDataUsageUtils = new OpDataUsageUtils(((TextView) this).mContext, this);
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        mSeparator = getResources().getString(17040424);
        this.mCarrierTextController = new CarrierTextController(((TextView) this).mContext, mSeparator, this.mShowAirplaneMode, this.mShowMissingSim);
        boolean isDeviceInteractive = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive();
        this.mShouldMarquee = isDeviceInteractive;
        setSelected(isDeviceInteractive);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mCarrierTextController.setListening(this.mCarrierTextCallback);
        this.mOpDataUsageUtils.setListening(true);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mCarrierTextController.setListening(null);
        this.mOpDataUsageUtils.setListening(false);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (i == 0) {
            this.mCarrierTextController.updateCarrierText();
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
            return;
        }
        setEllipsize(TextUtils.TruncateAt.END);
    }

    public void setExpanded(boolean z) {
        this.mOpDataUsageUtils.setExpanded(z);
    }

    private class CarrierTextTransformationMethod extends SingleLineTransformationMethod {
        private final boolean mAllCaps;
        private final Locale mLocale;

        public CarrierTextTransformationMethod(CarrierText carrierText, Context context, boolean z) {
            this.mLocale = context.getResources().getConfiguration().locale;
            this.mAllCaps = z;
        }

        @Override // android.text.method.ReplacementTransformationMethod, android.text.method.TransformationMethod
        public CharSequence getTransformation(CharSequence charSequence, View view) {
            CharSequence transformation = super.getTransformation(charSequence, view);
            return (!this.mAllCaps || transformation == null) ? transformation : transformation.toString().toUpperCase(this.mLocale);
        }
    }
}
