package com.android.keyguard;

import com.android.keyguard.KeyguardSecurityModel;
import com.oneplus.keyguard.OpEmergencyPanel;
public interface KeyguardSecurityCallback {
    void dismiss(boolean z, int i);

    void dismiss(boolean z, int i, boolean z2);

    KeyguardSecurityModel.SecurityMode getCurrentSecurityMode();

    OpEmergencyPanel getEmergencyPanel();

    void hideSecurityIcon();

    default void onCancelClicked() {
    }

    void onUserInput();

    void reportMDMEvent(String str, String str2, String str3);

    void reportUnlockAttempt(int i, boolean z, int i2);

    void reset();

    void tryToStartFaceLockFromBouncer();

    void userActivity();
}
