package com.oneplus.aod.controller;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.C0005R$dimen;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.aod.views.IOpAodClock;
import java.util.Calendar;
public class OpBuildInClockController extends OpClockControllerImpl {
    private Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() { // from class: com.oneplus.aod.controller.OpBuildInClockController.1
        @Override // java.lang.Runnable
        public void run() {
            OpBuildInClockController.this.scheduleNext();
        }
    };
    private boolean mStartSchedule;
    private IOpAodClock mViewImpl;

    public OpBuildInClockController(Context context) {
        super(context);
    }

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewAttachedToWindow(View view) {
        if (this.mViewImpl != null && view != null) {
            this.mViewImpl.applyLayoutParams(this.mAodClockSettings.getClockInfo());
            view.setLayoutParams((FrameLayout.LayoutParams) view.getLayoutParams());
        }
    }

    @Override // com.oneplus.aod.controller.OpClockControllerImpl, com.oneplus.aod.controller.IOpClockController
    public View getClockView() {
        View clockView = super.getClockView();
        if (clockView != null) {
            IOpAodClock iOpAodClock = (IOpAodClock) clockView;
            this.mViewImpl = iOpAodClock;
            iOpAodClock.supportSeconds(this.mAodClockSettings.isSupportSeconds());
            this.mViewImpl.applyLayoutParams(this.mAodClockSettings.getClockInfo());
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) clockView.getLayoutParams();
            if (layoutParams != null) {
                Resources resources = this.mContext.getResources();
                int i = 0;
                if (OpAodUtils.getDeviceTag().equals("17819")) {
                    i = resources.getDimensionPixelSize(C0005R$dimen.date_time_view_17819_additional_marginTop);
                } else if (OpAodUtils.getDeviceTag().equals("17801")) {
                    i = resources.getDimensionPixelSize(C0005R$dimen.date_time_view_17801_additional_marginTop);
                }
                layoutParams.topMargin += i;
                clockView.setLayoutParams(layoutParams);
            }
        } else {
            this.mViewImpl = null;
        }
        return clockView;
    }

    @Override // com.oneplus.aod.controller.OpClockControllerImpl, com.oneplus.aod.controller.IOpClockController
    public void onDestroyView() {
        endSchedule();
        IOpAodClock iOpAodClock = this.mViewImpl;
        if (iOpAodClock != null) {
            iOpAodClock.release();
            this.mViewImpl = null;
        }
        super.onDestroyView();
    }

    @Override // com.oneplus.aod.controller.OpClockControllerImpl, com.oneplus.aod.controller.IOpClockController
    public void onTimeTick() {
        if (this.mView != null && this.mViewImpl != null) {
            super.onTimeTick();
            this.mViewImpl.onTimeChanged(this.mTime);
        }
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void onFodShowOrHideOnAod(boolean z) {
        IOpAodClock iOpAodClock;
        if (this.mView != null && (iOpAodClock = this.mViewImpl) != null) {
            iOpAodClock.onFodShowOrHideOnAod(z);
        }
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void recoverFromBurnInScreen() {
        View view = this.mView;
        if (view != null && this.mViewImpl != null) {
            this.mViewImpl.recoverFromBurnInScreen();
            this.mViewImpl.applyLayoutParams(this.mAodClockSettings.getClockInfo());
            this.mView.setLayoutParams((FrameLayout.LayoutParams) view.getLayoutParams());
        }
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void onFodIndicationVisibilityChanged(boolean z) {
        IOpAodClock iOpAodClock;
        if (this.mView != null && (iOpAodClock = this.mViewImpl) != null) {
            iOpAodClock.onFodIndicationVisibilityChanged(z);
        }
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void onUserTrigger(int i) {
        IOpAodClock iOpAodClock;
        if (this.mView != null && (iOpAodClock = this.mViewImpl) != null) {
            iOpAodClock.onUserTrigger(i);
        }
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void onScreenTurnedOn() {
        if (this.mView != null) {
            IOpAodClock iOpAodClock = this.mViewImpl;
            if (iOpAodClock != null) {
                iOpAodClock.onScreenTurnedOn();
            }
            startSchedule();
        }
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void onScreenTurnedOff() {
        if (this.mView != null) {
            IOpAodClock iOpAodClock = this.mViewImpl;
            if (iOpAodClock != null) {
                iOpAodClock.onScreenTurnedOff();
            }
            endSchedule();
        }
    }

    private void startSchedule() {
        if (!this.mAodClockSettings.isSupportSeconds()) {
            Log.d(getTag(), "not support seconds.");
        } else if (!this.mStartSchedule) {
            this.mStartSchedule = true;
            scheduleNext();
        } else {
            Log.d(getTag(), "already start scheduling...");
        }
    }

    private void endSchedule() {
        this.mHandler.removeCallbacks(this.mRunnable);
        this.mStartSchedule = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleNext() {
        if (!this.mStartSchedule) {
            Log.d(getTag(), "end schedule, do not schedule next");
            return;
        }
        onTimeTick();
        int i = 1000 - Calendar.getInstance().get(14);
        String tag = getTag();
        Log.d(tag, "scheduleNext: " + i);
        this.mHandler.postDelayed(this.mRunnable, (long) i);
    }
}
