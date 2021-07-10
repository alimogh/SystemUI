package com.android.keyguard;

import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.LockscreenCredential;
import com.android.internal.widget.TextViewInputDisabler;
import com.android.keyguard.KeyguardSecurityViewFlipper;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.assist.ui.DisplayUtils;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.PinnedStackListenerForwarder;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.oneolus.anim.OpFadeAnim;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpUtils;
import java.util.List;
public class KeyguardPasswordView extends KeyguardAbsKeyInputView implements KeyguardSecurityView, TextView.OnEditorActionListener, TextWatcher {
    private static final String DEBUG_SECURITY_ICON_HEIGHT = SystemProperties.get("debug.security.icon.password.height", "");
    private static final boolean IS_CUSTOM_FINGERPRINT = OpUtils.isCustomFingerprint();
    private final int mDisappearYTranslation;
    private View mEcaContainer;
    protected View mEcaView;
    private Interpolator mFastOutLinearInInterpolator;
    private View mFingerprintIcon;
    private final H mHandler;
    private boolean mHasWindowFocus;
    private boolean mHideNavigationBar;
    private boolean mImeVisible;
    InputMethodManager mImm;
    private boolean mIsResume;
    private Interpolator mLinearOutSlowInInterpolator;
    private OpPasswordImeListener mOpPasswordImeListener;
    private int mOriginFingerprintIconHeight;
    private int mOriginPasswordEntryHeight;
    private TextView mPasswordEntry;
    private TextViewInputDisabler mPasswordEntryDisabler;
    private final boolean mShowImeAtScreenOn;
    private View mSwitchImeButton;
    private final KeyguardUpdateMonitorCallback mUpdateCallback;
    private int mUsedScreenWidth;

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return true;
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public KeyguardPasswordView(Context context) {
        this(context, null);
    }

    public KeyguardPasswordView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mHideNavigationBar = false;
        OverviewProxyService overviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        this.mIsResume = false;
        this.mUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.android.keyguard.KeyguardPasswordView.6
            @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
            public void onInsetsChanged() {
                Log.d("KeyguardPasswordView", "onInsetsChanged");
                KeyguardPasswordView.this.updateEmergencyButtonPositionY(50);
            }
        };
        this.mHandler = new H();
        this.mShowImeAtScreenOn = context.getResources().getBoolean(C0003R$bool.kg_show_ime_at_screen_on);
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(C0005R$dimen.disappear_y_translation);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(context, 17563663);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() {
        this.mPasswordEntry.setTextOperationUser(UserHandle.of(KeyguardUpdateMonitor.getCurrentUser()));
        displayDefaultSecurityMessage();
        boolean isEnabled = this.mPasswordEntry.isEnabled();
        Log.i("KeyguardPasswordView", " resetState wasDisabled:" + isEnabled + ", " + isNeedToPendingIME() + ", mResumed:" + this.mResumed + ", mPasswordEntry.isVisibleToUser():" + this.mPasswordEntry.isVisibleToUser());
        setPasswordEntryEnabled(true);
        setPasswordEntryInputEnabled(true);
        if (this.mResumed && this.mPasswordEntry.isVisibleToUser()) {
            if (isNeedToPendingIME()) {
                KeyguardUpdateMonitor.getInstance(getContext()).updateBiometricListeningState();
                if (!isShown() && this.mHideNavigationBar) {
                    this.mImm.hideSoftInputFromWindow(getWindowToken(), 0);
                    Log.d("KeyguardPasswordView", "reset to hide IME when not shown");
                }
            } else if (!isShown() && this.mHideNavigationBar) {
                this.mImm.hideSoftInputFromWindow(getWindowToken(), 0);
                Log.d("KeyguardPasswordView", "reset to hide IME when not shown");
            } else if (isEnabled) {
                requestShowIME();
            }
        }
    }

    private void displayDefaultSecurityMessage() {
        int i = KeyguardUpdateMonitor.getInstance(getContext()).isFirstUnlock() ? C0015R$string.kg_first_unlock_instructions : C0015R$string.kg_password_instructions;
        boolean isFacelockRecognizing = KeyguardUpdateMonitor.getInstance(getContext()).isFacelockRecognizing();
        SecurityMessageDisplay securityMessageDisplay = this.mSecurityMessageDisplay;
        if (securityMessageDisplay != null && !isFacelockRecognizing) {
            securityMessageDisplay.setMessage(getMessageWithCount(i));
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return C0008R$id.passwordEntry;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onResume(final int i) {
        super.onResume(i);
        this.mIsResume = true;
        post(new Runnable() { // from class: com.android.keyguard.KeyguardPasswordView.1
            @Override // java.lang.Runnable
            public void run() {
                if (KeyguardPasswordView.this.mPasswordEntry != null) {
                    KeyguardPasswordView keyguardPasswordView = KeyguardPasswordView.this;
                    keyguardPasswordView.mOriginPasswordEntryHeight = keyguardPasswordView.mPasswordEntry.getHeight();
                }
                if (KeyguardPasswordView.this.mFingerprintIcon != null) {
                    KeyguardPasswordView keyguardPasswordView2 = KeyguardPasswordView.this;
                    keyguardPasswordView2.mOriginFingerprintIconHeight = keyguardPasswordView2.mFingerprintIcon.getHeight();
                }
                if (!(!OpUtils.DEBUG_ONEPLUS || KeyguardPasswordView.this.mPasswordEntry == null || KeyguardPasswordView.this.mFingerprintIcon == null)) {
                    Log.i("KeyguardPasswordView", "onResume, mPwdEntry.getHeight():" + KeyguardPasswordView.this.mPasswordEntry.getHeight() + ", mPwdEntry.getMeasuredHeight():" + KeyguardPasswordView.this.mPasswordEntry.getMeasuredHeight() + ", mFIcon.getHeight():" + KeyguardPasswordView.this.mFingerprintIcon.getHeight() + ", mFIcon.getMeasuredHeight():" + KeyguardPasswordView.this.mFingerprintIcon.getMeasuredHeight() + ", getHeight():" + KeyguardPasswordView.this.getHeight());
                }
                if (KeyguardPasswordView.this.isShown() && KeyguardPasswordView.this.mPasswordEntry.isEnabled()) {
                    KeyguardPasswordView.this.mPasswordEntry.requestFocus();
                    Log.d("KeyguardPasswordView", " onResume reason:" + i);
                    if (i == 1 && !KeyguardPasswordView.this.mShowImeAtScreenOn) {
                        return;
                    }
                    if (!KeyguardPasswordView.this.isNeedToPendingIME()) {
                        KeyguardPasswordView.this.requestShowIME();
                    } else {
                        KeyguardUpdateMonitor.getInstance(KeyguardPasswordView.this.getContext()).updateBiometricListeningState();
                    }
                }
            }
        });
        TextView textView = this.mPasswordEntry;
        if (textView != null && this.mFingerprintIcon != null) {
            textView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.keyguard.KeyguardPasswordView.2
                @Override // android.view.View.OnLayoutChangeListener
                public void onLayoutChange(View view, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
                    if (KeyguardPasswordView.this.mHasWindowFocus) {
                        KeyguardPasswordView.this.mPasswordEntry.removeOnLayoutChangeListener(this);
                        if (OpUtils.DEBUG_ONEPLUS) {
                            Log.i("KeyguardPasswordView", "onLayoutChange, mPwdEntry.getHeight():" + KeyguardPasswordView.this.mPasswordEntry.getHeight() + ", mOriginPasswordEntryHeight:" + KeyguardPasswordView.this.mOriginPasswordEntryHeight + ", mFIcon.getHeight():" + KeyguardPasswordView.this.mFingerprintIcon.getHeight() + ", mOriginFingerprintIconHeight:" + KeyguardPasswordView.this.mOriginFingerprintIconHeight);
                        }
                        if (KeyguardPasswordView.this.mPasswordEntry.getHeight() < KeyguardPasswordView.this.mOriginPasswordEntryHeight) {
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) KeyguardPasswordView.this.mFingerprintIcon.getLayoutParams();
                            layoutParams.height = KeyguardPasswordView.this.mOriginFingerprintIconHeight - (KeyguardPasswordView.this.mOriginPasswordEntryHeight - KeyguardPasswordView.this.mPasswordEntry.getHeight());
                            KeyguardPasswordView.this.mFingerprintIcon.setLayoutParams(layoutParams);
                        }
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestShowIME() {
        boolean z = !KeyguardUpdateMonitor.getInstance(getContext()).isUserUnlocked();
        boolean z2 = false;
        if (OpLsState.getInstance().getPreventModeCtrl() != null) {
            z2 = OpLsState.getInstance().getPreventModeCtrl().isPreventModeActive();
        }
        Log.i("KeyguardPasswordView", "requestShowIME: mIsResume:" + this.mIsResume + " isShown:" + isShown() + " hideNavigationBar:" + this.mHideNavigationBar + " isPreventModeActive:" + z2 + " input Enable:" + this.mPasswordEntry.isEnabled());
        if (this.mIsResume && this.mPasswordEntry.isEnabled()) {
            if (!z2 || !z) {
                Log.i("KeyguardPasswordView", "request IME show");
                this.mImm.showSoftInput(this.mPasswordEntry, 1);
                return;
            }
            Log.d("KeyguardPasswordView", "not show IME when prevent mode");
        }
    }

    @Override // android.view.View
    public void onWindowFocusChanged(boolean z) {
        TextView textView;
        super.onWindowFocusChanged(z);
        Log.i("KeyguardPasswordView", "onWindowFocusChanged: hasWindowFocus:" + z);
        this.mHasWindowFocus = z;
        if (isNeedToPendingIME()) {
            KeyguardUpdateMonitor.getInstance(getContext()).updateBiometricListeningState();
        } else if (z && (textView = this.mPasswordEntry) != null && textView.isEnabled()) {
            requestShowIME();
        }
    }

    @Override // android.view.View
    public void onScreenStateChanged(int i) {
        super.onScreenStateChanged(i);
        Log.i("KeyguardPasswordView", "onScreenStateChanged: screenState:" + i);
        if (isNeedToPendingIME()) {
            KeyguardUpdateMonitor.getInstance(getContext()).updateBiometricListeningState();
        } else if (i == 1) {
            requestShowIME();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPromptReasonStringRes(int i) {
        if (i == 0) {
            return 0;
        }
        if (i == 1) {
            return C0015R$string.kg_prompt_reason_restart_password;
        }
        if (i == 2) {
            return C0015R$string.kg_prompt_reason_timeout_password;
        }
        if (i == 3) {
            return C0015R$string.kg_prompt_reason_device_admin;
        }
        if (i == 4) {
            return C0015R$string.kg_prompt_reason_user_request;
        }
        if (i != 6) {
            return C0015R$string.kg_prompt_reason_timeout_password;
        }
        return C0015R$string.kg_prompt_reason_prepare_for_update_password;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        super.onPause();
        this.mIsResume = false;
        this.mImm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onStartingToHide() {
        this.mImm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSwitchImeButton() {
        boolean z = this.mSwitchImeButton.getVisibility() == 0;
        boolean hasMultipleEnabledIMEsOrSubtypes = hasMultipleEnabledIMEsOrSubtypes(this.mImm, false);
        if (z != hasMultipleEnabledIMEsOrSubtypes) {
            this.mSwitchImeButton.setVisibility(hasMultipleEnabledIMEsOrSubtypes ? 0 : 8);
        }
        if (this.mSwitchImeButton.getVisibility() != 0) {
            ViewGroup.LayoutParams layoutParams = this.mPasswordEntry.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) layoutParams).setMarginStart(0);
                this.mPasswordEntry.setLayoutParams(layoutParams);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mImm = (InputMethodManager) getContext().getSystemService("input_method");
        TextView textView = (TextView) findViewById(getPasswordTextViewId());
        this.mPasswordEntry = textView;
        textView.setTextOperationUser(UserHandle.of(KeyguardUpdateMonitor.getCurrentUser()));
        this.mPasswordEntryDisabler = new TextViewInputDisabler(this.mPasswordEntry);
        this.mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
        this.mPasswordEntry.setInputType(129);
        this.mPasswordEntry.setOnEditorActionListener(this);
        this.mPasswordEntry.addTextChangedListener(this);
        this.mPasswordEntry.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.KeyguardPasswordView.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                KeyguardPasswordView.this.mCallback.userActivity();
            }
        });
        this.mPasswordEntry.setSelected(true);
        View findViewById = findViewById(C0008R$id.switch_ime_button);
        this.mSwitchImeButton = findViewById;
        findViewById.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.KeyguardPasswordView.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                KeyguardPasswordView.this.mCallback.userActivity();
                KeyguardPasswordView keyguardPasswordView = KeyguardPasswordView.this;
                keyguardPasswordView.mImm.showInputMethodPickerFromSystem(false, keyguardPasswordView.getContext().getDisplayId());
            }
        });
        View findViewById2 = findViewById(C0008R$id.cancel_button);
        if (findViewById2 != null) {
            findViewById2.setOnClickListener(new View.OnClickListener() { // from class: com.android.keyguard.-$$Lambda$KeyguardPasswordView$o6rdkANQuxgpLXMWWI2lzhbd_0k
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    KeyguardPasswordView.this.lambda$onFinishInflate$0$KeyguardPasswordView(view);
                }
            });
        }
        updateSwitchImeButton();
        postDelayed(new Runnable() { // from class: com.android.keyguard.KeyguardPasswordView.5
            @Override // java.lang.Runnable
            public void run() {
                KeyguardPasswordView.this.updateSwitchImeButton();
            }
        }, 500);
        displayDefaultSecurityMessage();
        this.mFingerprintIcon = findViewById(C0008R$id.fingerprint_icon);
        this.mEcaView = findViewById(C0008R$id.keyguard_selector_fade_container);
        this.mEcaContainer = findViewById(C0008R$id.keyguard_eca_emergency_container);
        this.mOpPasswordImeListener = new OpPasswordImeListener();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$KeyguardPasswordView(View view) {
        this.mCallback.reset();
        this.mCallback.onCancelClicked();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateCallback);
        try {
            WindowManagerWrapper.getInstance().addPinnedStackListener(this.mOpPasswordImeListener);
        } catch (RemoteException e) {
            Log.e("KeyguardPasswordView", "addPinnedStackListener: ", e);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateCallback);
        WindowManagerWrapper.getInstance().removePinnedStackListener(this.mOpPasswordImeListener);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean onRequestFocusInDescendants(int i, Rect rect) {
        return this.mPasswordEntry.requestFocus(i, rect);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void resetPasswordText(boolean z, boolean z2) {
        this.mPasswordEntry.setText("");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public LockscreenCredential getEnteredCredential() {
        return LockscreenCredential.createPasswordOrNone(this.mPasswordEntry.getText());
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void setPasswordEntryEnabled(boolean z) {
        this.mPasswordEntry.setEnabled(z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void setPasswordEntryInputEnabled(boolean z) {
        this.mPasswordEntryDisabler.setInputEnabled(z);
    }

    private boolean hasMultipleEnabledIMEsOrSubtypes(InputMethodManager inputMethodManager, boolean z) {
        int i = 0;
        for (InputMethodInfo inputMethodInfo : inputMethodManager.getEnabledInputMethodListAsUser(KeyguardUpdateMonitor.getCurrentUser())) {
            if (i > 1) {
                return true;
            }
            List<InputMethodSubtype> enabledInputMethodSubtypeList = inputMethodManager.getEnabledInputMethodSubtypeList(inputMethodInfo, true);
            if (!enabledInputMethodSubtypeList.isEmpty()) {
                int i2 = 0;
                for (InputMethodSubtype inputMethodSubtype : enabledInputMethodSubtypeList) {
                    if (inputMethodSubtype.isAuxiliary()) {
                        i2++;
                    }
                }
                if (enabledInputMethodSubtypeList.size() - i2 <= 0) {
                    if (z && i2 > 1) {
                    }
                }
            }
            i++;
        }
        return i > 1 || inputMethodManager.getEnabledInputMethodSubtypeList(null, false).size() > 1;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getWrongPasswordStringId() {
        int failedUnlockAttempts = KeyguardUpdateMonitor.getInstance(getContext()).getFailedUnlockAttempts(KeyguardUpdateMonitor.getCurrentUser()) % 5;
        if (failedUnlockAttempts == 3) {
            return C0015R$string.kg_wrong_pin_warning;
        }
        if (failedUnlockAttempts == 4) {
            return C0015R$string.kg_wrong_pin_warning_one;
        }
        return C0015R$string.kg_wrong_password;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        AnimatorSet fadeInOutVisibilityAnimation;
        setAlpha(0.0f);
        setTranslationY(0.0f);
        animate().alpha(1.0f).withLayer().setDuration(300).setInterpolator(this.mLinearOutSlowInInterpolator);
        View view = this.mFingerprintIcon;
        if (view != null && view.getVisibility() == 0 && (fadeInOutVisibilityAnimation = OpFadeAnim.getFadeInOutVisibilityAnimation(this.mFingerprintIcon, 0, null, true)) != null) {
            fadeInOutVisibilityAnimation.start();
        }
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        animate().alpha(0.0f).translationY((float) this.mDisappearYTranslation).setInterpolator(this.mFastOutLinearInInterpolator).setDuration(100).withEndAction(runnable);
        return true;
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        KeyguardSecurityCallback keyguardSecurityCallback = this.mCallback;
        if (keyguardSecurityCallback != null) {
            keyguardSecurityCallback.userActivity();
        }
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable editable) {
        if (!TextUtils.isEmpty(editable)) {
            onUserInput();
        }
    }

    @Override // android.widget.TextView.OnEditorActionListener
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        boolean z = keyEvent == null && (i == 0 || i == 6 || i == 5);
        boolean z2 = keyEvent != null && KeyEvent.isConfirmKey(keyEvent.getKeyCode()) && keyEvent.getAction() == 0;
        if (!z && !z2) {
            return false;
        }
        verifyPasswordAndUnlock();
        return true;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public CharSequence getTitle() {
        return getContext().getString(17040371);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedToPendingIME() {
        if (!IS_CUSTOM_FINGERPRINT) {
            return false;
        }
        return KeyguardUpdateMonitor.getInstance(getContext()).isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser());
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardSecurityView
    public boolean isCheckingPassword() {
        return super.isCheckingPassword();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        updateLayoutParamForDisplayWidth();
        updateEmergencyButtonPositionY();
    }

    private void updateLayoutParamForDisplayWidth() {
        if (!DEBUG_SECURITY_ICON_HEIGHT.isEmpty()) {
            ViewGroup.LayoutParams layoutParams = this.mFingerprintIcon.getLayoutParams();
            layoutParams.height = OpUtils.convertPxByResolutionProportion((float) Integer.valueOf(DEBUG_SECURITY_ICON_HEIGHT).intValue(), 1080);
            this.mFingerprintIcon.setLayoutParams(layoutParams);
        }
        int width = DisplayUtils.getWidth(((LinearLayout) this).mContext);
        if (this.mUsedScreenWidth != width) {
            Log.i("KeyguardPasswordView", "updateLayoutParamForDisplayWidth, displayWidth:" + width + ", mUsedScreenWidth:" + this.mUsedScreenWidth);
            this.mUsedScreenWidth = width;
            if (width > 1080) {
                KeyguardSecurityViewFlipper.LayoutParams layoutParams2 = (KeyguardSecurityViewFlipper.LayoutParams) getLayoutParams();
                layoutParams2.maxHeight = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_max_height), 1080);
                layoutParams2.maxWidth = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_width), 1080);
                ((FrameLayout.LayoutParams) layoutParams2).height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_max_height), 1080);
                setLayoutParams(layoutParams2);
                int convertPxByResolutionProportion = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.fingerprint_icon_padding), 1080);
                this.mFingerprintIcon.setPaddingRelative(convertPxByResolutionProportion, convertPxByResolutionProportion, convertPxByResolutionProportion, convertPxByResolutionProportion);
                ViewGroup.LayoutParams layoutParams3 = this.mFingerprintIcon.getLayoutParams();
                layoutParams3.height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_password_view_fingerprint_icon_framelayout_height), 1080);
                this.mFingerprintIcon.setLayoutParams(layoutParams3);
                ImageView imageView = (ImageView) findViewById(C0008R$id.security_image);
                imageView.getLayoutParams().height = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_image_height), 1080);
                imageView.getLayoutParams().width = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_security_image_width), 1080);
                View findViewById = findViewById(C0008R$id.keyguard_password_view_password_entry_field);
                ViewGroup.LayoutParams layoutParams4 = findViewById.getLayoutParams();
                layoutParams4.width = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_password_view_password_entry_field_width), 1080);
                findViewById.setLayoutParams(layoutParams4);
                int convertPxByResolutionProportion2 = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_password_view_switch_ime_button_padding), 1080);
                this.mSwitchImeButton.setPaddingRelative(convertPxByResolutionProportion2, convertPxByResolutionProportion2, convertPxByResolutionProportion2, convertPxByResolutionProportion2);
                if (this.mSwitchImeButton.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mSwitchImeButton.getLayoutParams();
                    marginLayoutParams.bottomMargin = OpUtils.convertPxByResolutionProportion((float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_password_view_switch_ime_button_marginBottom), 1080);
                    this.mSwitchImeButton.setLayoutParams(marginLayoutParams);
                }
            }
        }
    }

    public boolean isInputMethodPickerShown() {
        InputMethodManager inputMethodManager = this.mImm;
        return inputMethodManager != null && inputMethodManager.isInputMethodPickerShown();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateEmergencyButtonPositionY() {
        int dimensionPixelSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(OpUtils.is2KResolution() ? C0005R$dimen.op_biometric_icon_normal_location_y_2k : C0005R$dimen.op_biometric_icon_normal_location_y_1080p);
        boolean z = dimensionPixelSize > 2800;
        View findViewById = findViewById(C0008R$id.keyguard_password_view_password_entry_field);
        int findParentRecursivelyTop = findParentRecursivelyTop(findViewById, C0008R$id.keyguard_host_view, 0) + findViewById.getHeight();
        if (OpUtils.isCutoutHide(((LinearLayout) this).mContext)) {
            findParentRecursivelyTop += ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(17105481);
        }
        View findViewById2 = findViewById(C0008R$id.keyguard_emergency_container_top_space);
        int convertDpToFixedPx = (z ? 165 : 130) + OpUtils.convertDpToFixedPx(((LinearLayout) this).mContext.getResources().getDimension(C0005R$dimen.op_emergency_bubble_drop_distance));
        if (this.mImeVisible) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mEcaContainer.getLayoutParams();
            layoutParams.topMargin = 0;
            this.mEcaContainer.setLayoutParams(layoutParams);
            Log.i("KeyguardPasswordView", "set params.topMargin zero:" + layoutParams.topMargin);
            findViewById2.setVisibility(4);
            return;
        }
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mEcaContainer.getLayoutParams();
        layoutParams2.topMargin = ((dimensionPixelSize - findParentRecursivelyTop) - convertDpToFixedPx) - this.mEcaView.getHeight();
        this.mEcaContainer.setLayoutParams(layoutParams2);
        Log.i("KeyguardPasswordView", "set params.topMargin:" + layoutParams2.topMargin);
        findViewById2.setVisibility(8);
    }

    private int findParentRecursivelyTop(View view, int i, int i2) {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("KeyguardPasswordView", "findParentRecursively, view.getTop():" + view.getTop() + ", before add:" + i2);
        }
        int top = i2 + view.getTop();
        if (view.getId() == i) {
            return top;
        }
        View view2 = (View) view.getParent();
        if (view2 == null) {
            return 0;
        }
        return findParentRecursivelyTop(view2, i, top);
    }

    private class OpPasswordImeListener extends PinnedStackListenerForwarder.PinnedStackListener {
        private OpPasswordImeListener() {
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onImeVisibilityChanged(boolean z, int i) {
            Log.d("KeyguardPasswordView", "onImeVisibilityChanged: imeVisible= " + z + ", imeHeight= " + i + ", mImeVisible:" + KeyguardPasswordView.this.mImeVisible);
            KeyguardPasswordView.this.mImeVisible = z;
            if (KeyguardPasswordView.this.isNeedToPendingIME()) {
                KeyguardPasswordView.this.mEcaContainer.setVisibility(4);
                KeyguardPasswordView.this.updateEmergencyButtonPositionY(50);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateEmergencyButtonPositionY(int i) {
        this.mHandler.removeMessages(1);
        H h = this.mHandler;
        h.sendMessageDelayed(h.obtainMessage(1), (long) i);
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                Log.d("KeyguardPasswordView", "H.UPDATE_EMERGENCY_BUTTON");
                KeyguardPasswordView.this.updateEmergencyButtonPositionY();
                KeyguardPasswordView.this.mEcaContainer.setVisibility(0);
            }
        }
    }
}
