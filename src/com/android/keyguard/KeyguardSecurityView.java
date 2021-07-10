package com.android.keyguard;

import android.content.res.ColorStateList;
import com.android.internal.widget.LockPatternUtils;
public interface KeyguardSecurityView {
    CharSequence getTitle();

    boolean isCheckingPassword();

    boolean needsInput();

    default void onHidden() {
    }

    void onPause();

    void onResume(int i);

    default void onStartingToHide() {
    }

    void reset();

    void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback);

    void setLockPatternUtils(LockPatternUtils lockPatternUtils);

    void showMessage(CharSequence charSequence, ColorStateList colorStateList);

    default void showMessage(CharSequence charSequence, ColorStateList colorStateList, int i) {
    }

    void showPromptReason(int i);

    void startAppearAnimation();

    boolean startDisappearAnimation(Runnable runnable);
}
