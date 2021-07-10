package com.oneplus.aod.utils.burnin;

import android.view.ViewGroup;
import com.oneplus.aod.controller.IOpClockController;
public interface IBurnInProtectionController {
    void moveBackToOriginalPosition(Runnable runnable);

    void onAlarm();

    void recover();

    void release();

    void reset();

    void setup(ViewGroup viewGroup, ViewGroup viewGroup2, IOpClockController iOpClockController);
}
