package com.oneplus.networkspeed;

import com.android.systemui.statusbar.policy.CallbackController;
import java.util.BitSet;
public interface NetworkSpeedController extends CallbackController<INetworkSpeedStateCallBack> {

    public interface INetworkSpeedStateCallBack {
        default void onSpeedChange(String str) {
        }

        default void onSpeedShow(boolean z) {
        }
    }

    void updateConnectivity(BitSet bitSet, BitSet bitSet2);
}
