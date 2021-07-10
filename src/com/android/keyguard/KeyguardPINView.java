package com.android.keyguard;

import android.animation.AnimatorSet;
import android.content.Context;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardSecurityViewFlipper;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.assist.ui.DisplayUtils;
import com.oneolus.anim.OpFadeAnim;
import com.oneplus.keyguard.OpKeyguardPinBasedInputViewForPin;
import com.oneplus.util.OpUtils;
public class KeyguardPINView extends OpKeyguardPinBasedInputViewForPin {
    private static final String DEBUG_SECURITY_ICON_HEIGHT = SystemProperties.get("debug.security.icon.pin.height", "");
    private final AppearAnimationUtils mAppearAnimationUtils;
    private ViewGroup mContainer;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    private final DisappearAnimationUtils mDisappearAnimationUtilsLocked;
    private int mDisappearYTranslation;
    private View mFingerprintIcon;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private ViewGroup mRow0;
    private ViewGroup mRow1;
    private ViewGroup mRow2;
    private ViewGroup mRow3;
    private int mUsedScreenWidth;
    private View[][] mViews;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public KeyguardPINView(Context context) {
        this(context, null);
    }

    public KeyguardPINView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mAppearAnimationUtils = new AppearAnimationUtils(context);
        this.mDisappearAnimationUtils = new DisappearAnimationUtils(context, 125, 0.6f, 0.45f, AnimationUtils.loadInterpolator(((LinearLayout) this).mContext, 17563663));
        this.mDisappearAnimationUtilsLocked = new DisappearAnimationUtils(context, 187, 0.6f, 0.45f, AnimationUtils.loadInterpolator(((LinearLayout) this).mContext, 17563663));
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(C0005R$dimen.disappear_y_translation);
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.keyguard.OpKeyguardPinBasedInputViewForPin, com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() {
        super.resetState();
        displayDefaultSecurityMessage();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return C0008R$id.pinEntry;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.keyguard.OpKeyguardPinBasedInputViewForPin, com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mContainer = (ViewGroup) findViewById(C0008R$id.container);
        this.mRow0 = (ViewGroup) findViewById(C0008R$id.row0);
        this.mRow1 = (ViewGroup) findViewById(C0008R$id.row1);
        this.mRow2 = (ViewGroup) findViewById(C0008R$id.row2);
        this.mRow3 = (ViewGroup) findViewById(C0008R$id.row3);
        this.mViews = new View[][]{new View[]{null, findViewById(C0008R$id.keyguard_message_area), null}, new View[]{this.mRow0, null, null}, new View[]{findViewById(C0008R$id.key1), findViewById(C0008R$id.key2), findViewById(C0008R$id.key3)}, new View[]{findViewById(C0008R$id.key4), findViewById(C0008R$id.key5), findViewById(C0008R$id.key6)}, new View[]{findViewById(C0008R$id.key7), findViewById(C0008R$id.key8), findViewById(C0008R$id.key9)}, new View[]{findViewById(C0008R$id.deleteOrCancel), findViewById(C0008R$id.key0), findViewById(C0008R$id.key_enter)}, new View[]{null, this.mEcaView, null}};
        View findViewById = findViewById(C0008R$id.cancel_button);
        if (findViewById != null) {
            findViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.-$$Lambda$KeyguardPINView$32q9EwjCzWlJ6lNiw9pw0PSsPxs
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    KeyguardPINView.this.lambda$onFinishInflate$0$KeyguardPINView(view);
                }
            });
        }
        this.mFingerprintIcon = findViewById(C0008R$id.fingerprint_icon);
        displayDefaultSecurityMessage();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$KeyguardPINView(View view) {
        this.mCallback.reset();
        this.mCallback.onCancelClicked();
    }

    private void displayDefaultSecurityMessage() {
        int i = this.mKeyguardUpdateMonitor.isFirstUnlock() ? C0015R$string.kg_first_unlock_instructions : C0015R$string.kg_pin_instructions;
        boolean isFacelockRecognizing = this.mKeyguardUpdateMonitor.isFacelockRecognizing();
        SecurityMessageDisplay securityMessageDisplay = this.mSecurityMessageDisplay;
        if (securityMessageDisplay != null && !isFacelockRecognizing) {
            securityMessageDisplay.setMessage(getMessageWithCount(i));
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getWrongPasswordStringId() {
        int failedUnlockAttempts = KeyguardUpdateMonitor.getInstance(((LinearLayout) this).mContext).getFailedUnlockAttempts(KeyguardUpdateMonitor.getCurrentUser()) % 5;
        if (failedUnlockAttempts == 3) {
            return C0015R$string.kg_wrong_pin_warning;
        }
        if (failedUnlockAttempts == 4) {
            return C0015R$string.kg_wrong_pin_warning_one;
        }
        return C0015R$string.kg_wrong_pin;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        AnimatorSet fadeInOutVisibilityAnimation;
        enableClipping(false);
        setAlpha(1.0f);
        setTranslationY(this.mAppearAnimationUtils.getStartTranslation());
        AppearAnimationUtils.startTranslationYAnimation(this, 0, 500, 0.0f, this.mAppearAnimationUtils.getInterpolator());
        this.mAppearAnimationUtils.startAnimation2d(this.mViews, new Runnable() { // from class: com.android.keyguard.KeyguardPINView.1
            @Override // java.lang.Runnable
            public void run() {
                KeyguardPINView.this.enableClipping(true);
            }
        });
        View view = this.mFingerprintIcon;
        if (view != null && view.getVisibility() == 0 && (fadeInOutVisibilityAnimation = OpFadeAnim.getFadeInOutVisibilityAnimation(this.mFingerprintIcon, 0, null, true)) != null) {
            fadeInOutVisibilityAnimation.start();
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(final Runnable runnable) {
        DisappearAnimationUtils disappearAnimationUtils;
        enableClipping(false);
        setTranslationY(0.0f);
        AppearAnimationUtils.startTranslationYAnimation(this, 0, 500, (float) this.mDisappearYTranslation, this.mDisappearAnimationUtils.getInterpolator());
        if (this.mKeyguardUpdateMonitor.needsSlowUnlockTransition()) {
            disappearAnimationUtils = this.mDisappearAnimationUtilsLocked;
        } else {
            disappearAnimationUtils = this.mDisappearAnimationUtils;
        }
        disappearAnimationUtils.startAnimation2d(this.mViews, new Runnable() { // from class: com.android.keyguard.KeyguardPINView.2
            @Override // java.lang.Runnable
            public void run() {
                KeyguardPINView.this.enableClipping(true);
                Runnable runnable2 = runnable;
                if (runnable2 != null) {
                    runnable2.run();
                }
            }
        });
        if (this.mFingerprintIcon.getVisibility() != 0) {
            return true;
        }
        DisappearAnimationUtils disappearAnimationUtils2 = this.mDisappearAnimationUtils;
        disappearAnimationUtils2.createAnimation(this.mFingerprintIcon, 0L, 200L, 3.0f * (-disappearAnimationUtils2.getStartTranslation()), false, this.mDisappearAnimationUtils.getInterpolator(), (Runnable) null);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enableClipping(boolean z) {
        this.mContainer.setClipToPadding(z);
        this.mContainer.setClipChildren(z);
        this.mRow1.setClipToPadding(z);
        this.mRow2.setClipToPadding(z);
        this.mRow3.setClipToPadding(z);
        setClipChildren(z);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        updateLayoutParamForDisplayWidth();
    }

    private void updateLayoutParamForDisplayWidth() {
        if (!DEBUG_SECURITY_ICON_HEIGHT.isEmpty()) {
            ViewGroup.LayoutParams layoutParams = this.mFingerprintIcon.getLayoutParams();
            layoutParams.height = OpUtils.convertPxByResolutionProportion((float) Integer.valueOf(DEBUG_SECURITY_ICON_HEIGHT).intValue(), 1080);
            this.mFingerprintIcon.setLayoutParams(layoutParams);
        }
        int width = DisplayUtils.getWidth(((LinearLayout) this).mContext);
        if (this.mUsedScreenWidth != width) {
            Log.i("KeyguardPINView", "updateLayoutParamForDisplayWidth, displayWidth:" + width + ", mUsedScreenWidth:" + this.mUsedScreenWidth);
            this.mUsedScreenWidth = width;
            if (width > 1080) {
                KeyguardSecurityViewFlipper.LayoutParams layoutParams2 = (KeyguardSecurityViewFlipper.LayoutParams) getLayoutParams();
                layoutParams2.maxHeight = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_max_height), 1080);
                layoutParams2.maxWidth = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_width), 1080);
                setLayoutParams(layoutParams2);
                ImageView imageView = (ImageView) findViewById(C0008R$id.security_image);
                imageView.getLayoutParams().height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_image_height), 1080);
                imageView.getLayoutParams().width = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_image_width), 1080);
                int convertPxByResolutionProportion = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.fingerprint_icon_padding), 1080);
                this.mFingerprintIcon.setPaddingRelative(convertPxByResolutionProportion, convertPxByResolutionProportion, convertPxByResolutionProportion, convertPxByResolutionProportion);
                ViewGroup.LayoutParams layoutParams3 = this.mFingerprintIcon.getLayoutParams();
                layoutParams3.height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_pin_view_fingerprint_icon_framelayout_height), 1080);
                this.mFingerprintIcon.setLayoutParams(layoutParams3);
                ViewGroup.LayoutParams layoutParams4 = this.mRow0.getLayoutParams();
                layoutParams4.height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_pin_view_alpha_optimized_relativelayout_height), 1080);
                this.mRow0.setLayoutParams(layoutParams4);
                ViewGroup.LayoutParams layoutParams5 = this.mRow1.getLayoutParams();
                layoutParams5.height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_num_pad_key_height), 1080);
                this.mRow1.setLayoutParams(layoutParams5);
                ViewGroup.LayoutParams layoutParams6 = this.mRow2.getLayoutParams();
                layoutParams6.height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_num_pad_key_height), 1080);
                this.mRow2.setLayoutParams(layoutParams6);
                ViewGroup.LayoutParams layoutParams7 = this.mRow3.getLayoutParams();
                layoutParams7.height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_num_pad_key_height), 1080);
                this.mRow3.setLayoutParams(layoutParams7);
                ViewGroup viewGroup = (ViewGroup) findViewById(C0008R$id.row4);
                ViewGroup.LayoutParams layoutParams8 = viewGroup.getLayoutParams();
                layoutParams8.height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_num_pad_key_height), 1080);
                viewGroup.setLayoutParams(layoutParams8);
                View findViewById = findViewById(C0008R$id.pinEntry);
                ViewGroup.LayoutParams layoutParams9 = findViewById.getLayoutParams();
                layoutParams9.width = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_width), 1080);
                if (layoutParams9 instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) findViewById.getLayoutParams()).setMarginEnd(OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_pin_view_password_textview_for_pin_margin_right), 1080));
                }
                View findViewById2 = findViewById(C0008R$id.row1);
                if (findViewById2.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) findViewById2.getLayoutParams();
                    marginLayoutParams.setMarginStart(OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_pin_view_num_pad_margin_start), 1080));
                    marginLayoutParams.setMarginEnd(OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_pin_view_num_pad_margin_end), 1080));
                    findViewById2.setLayoutParams(marginLayoutParams);
                }
                ((TextView) findViewById(C0008R$id.deleteOrCancel)).setAutoSizeTextTypeUniformWithConfiguration(OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_pin_view_delete_cancel_min_textsize), 1080), OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_pin_view_delete_cancel_max_textsize), 1080), OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_pin_view_delete_cancel_step_granularity), 1080), 0);
            }
        }
    }
}
