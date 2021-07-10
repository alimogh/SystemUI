package com.oneplus.aod.utils.burnin;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.aod.controller.IOpClockController;
import com.oneplus.plugin.OpLsState;
public abstract class OpBurnInBaseController implements IBurnInProtectionController {
    protected Rect mBound;
    protected ViewGroup mClockContainer;
    protected IOpClockController mClockController;
    protected ViewGroup mSystemInfoView;
    protected String mTag = getClass().getSimpleName();

    /* access modifiers changed from: protected */
    public abstract void handleOnAlarm(View view, ViewGroup viewGroup);

    /* access modifiers changed from: protected */
    public abstract void handleSetup(IOpClockController iOpClockController);

    public OpBurnInBaseController(Context context) {
    }

    @Override // com.oneplus.aod.utils.burnin.IBurnInProtectionController
    public void setup(ViewGroup viewGroup, ViewGroup viewGroup2, IOpClockController iOpClockController) {
        this.mClockContainer = viewGroup;
        this.mSystemInfoView = viewGroup2;
        this.mClockController = iOpClockController;
        if (iOpClockController != null) {
            this.mBound = iOpClockController.getBound();
        } else {
            this.mBound = new Rect();
        }
        handleSetup(iOpClockController);
    }

    @Override // com.oneplus.aod.utils.burnin.IBurnInProtectionController
    public void release() {
        Log.d(this.mTag, "release");
        this.mClockContainer = null;
        this.mSystemInfoView = null;
        this.mClockController = null;
    }

    @Override // com.oneplus.aod.utils.burnin.IBurnInProtectionController
    public void onAlarm() {
        Log.d(this.mTag, "onAlarm");
        View clockView = getClockView();
        if (clockView != null) {
            handleOnAlarm(clockView, this.mSystemInfoView);
        } else {
            Log.w(this.mTag, "onAlarm: clockView not exists");
        }
    }

    @Override // com.oneplus.aod.utils.burnin.IBurnInProtectionController
    public void moveBackToOriginalPosition(Runnable runnable) {
        recover();
        if (runnable != null) {
            runnable.run();
        }
    }

    @Override // com.oneplus.aod.utils.burnin.IBurnInProtectionController
    public void recover() {
        reset();
        if (getClockView() != null) {
            IOpClockController iOpClockController = this.mClockController;
            if (iOpClockController != null) {
                iOpClockController.recoverFromBurnInScreen();
            }
        } else {
            Log.w(this.mTag, "recover: clockView not exists");
        }
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        if (phoneStatusBar != null) {
            phoneStatusBar.getAodDisplayViewManager().recoverFromBurnInScreen();
        } else {
            Log.w(this.mTag, "recover: statusbar not exists");
        }
    }

    /* access modifiers changed from: protected */
    public View getClockView() {
        ViewGroup viewGroup = this.mClockContainer;
        if (viewGroup == null || viewGroup.getChildCount() <= 0) {
            return null;
        }
        return this.mClockContainer.getChildAt(0);
    }

    /* access modifiers changed from: protected */
    public int getClockViewMarginTop() {
        View clockView = getClockView();
        if (clockView != null) {
            return ((FrameLayout.LayoutParams) clockView.getLayoutParams()).topMargin;
        }
        return 0;
    }
}
