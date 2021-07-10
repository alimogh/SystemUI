package com.oneplus.util;

import android.util.BoostFramework;
public class OpBoostUtils {
    static final int[] BOOST_GPU = {1115734016, 587};
    private BoostFramework mBoostFramework;

    public OpBoostUtils() {
        this.mBoostFramework = null;
        this.mBoostFramework = new BoostFramework();
    }

    public void aquireGPUBoost() {
        BoostFramework boostFramework = this.mBoostFramework;
        if (boostFramework != null) {
            boostFramework.perfLockAcquire(0, BOOST_GPU);
        }
    }

    public void releaseGPUBoost() {
        BoostFramework boostFramework = this.mBoostFramework;
        if (boostFramework != null) {
            boostFramework.perfLockRelease();
        }
    }
}
