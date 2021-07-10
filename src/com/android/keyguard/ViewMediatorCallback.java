package com.android.keyguard;
public interface ViewMediatorCallback {
    CharSequence consumeCustomMessage();

    int getBouncerPromptReason();

    boolean isScreenOn();

    void keyguardDone(boolean z, int i);

    void keyguardDoneDrawing();

    void keyguardDonePending(boolean z, int i);

    void keyguardGone();

    void onBouncerVisiblityChanged(boolean z);

    void onCancelClicked();

    void playTrustedSound();

    void readyForKeyguardDone();

    void reportMDMEvent(String str, String str2, String str3);

    void resetKeyguard();

    void setNeedsInput(boolean z);

    void startPowerKeyLaunchCamera();

    void tryToStartFaceLockFromBouncer();

    void userActivity();
}
