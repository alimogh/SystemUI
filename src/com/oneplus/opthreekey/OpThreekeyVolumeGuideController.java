package com.oneplus.opthreekey;

import com.android.systemui.statusbar.policy.CallbackController;
public interface OpThreekeyVolumeGuideController extends CallbackController<IOpThreekeyVolumeGuideControllerCallBack> {

    public interface IOpThreekeyVolumeGuideControllerCallBack {
        default void onThreekeyVolumeGuideUiStateChanged(int i) {
        }
    }

    public interface UserActivityListener {
    }

    void init(int i, UserActivityListener userActivityListener);

    int isNeedToShowGuideUi(int i, boolean z);
}
