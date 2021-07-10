package com.oneplus.aod.utils.burnin;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.oneplus.aod.controller.IOpClockController;
public class OpBurnInVerticalController extends OpBurnInBaseController {
    private int mMovingDistance;
    private boolean mMovingDown;

    public OpBurnInVerticalController(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.burnin.OpBurnInBaseController
    public void handleSetup(IOpClockController iOpClockController) {
        this.mMovingDistance = iOpClockController != null ? iOpClockController.getMovingDistance() : 0;
        reset();
    }

    @Override // com.oneplus.aod.utils.burnin.IBurnInProtectionController
    public void reset() {
        this.mMovingDown = true;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.burnin.OpBurnInBaseController
    public void handleOnAlarm(View view, ViewGroup viewGroup) {
        Rect rect = new Rect(this.mBound);
        int height = rect.bottom - view.getHeight();
        rect.bottom = height;
        rect.bottom = height - viewGroup.getHeight();
        validateNextAction(rect);
        int moveDown = this.mMovingDown ? moveDown(rect) : moveUp(rect);
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "handleOnAlarm: range= " + rect + ", moveDown= " + this.mMovingDown + ", move from " + getClockViewMarginTop() + " to " + moveDown);
        }
        movePosition(view, moveDown);
    }

    private void movePosition(View view, int i) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.topMargin = i;
        view.setLayoutParams(layoutParams);
    }

    private void validateNextAction(Rect rect) {
        int clockViewMarginTop = getClockViewMarginTop();
        if (this.mMovingDown && clockViewMarginTop >= rect.bottom) {
            this.mMovingDown = false;
            Log.d(this.mTag, "move up!");
        } else if (!this.mMovingDown && clockViewMarginTop <= rect.top) {
            this.mMovingDown = true;
            Log.d(this.mTag, "move down!");
        }
    }

    private int moveDown(Rect rect) {
        int clockViewMarginTop = getClockViewMarginTop();
        int i = this.mMovingDistance;
        int i2 = clockViewMarginTop + i;
        int i3 = rect.bottom;
        return i2 < i3 ? clockViewMarginTop + i : i3;
    }

    private int moveUp(Rect rect) {
        int clockViewMarginTop = getClockViewMarginTop();
        int i = this.mMovingDistance;
        int i2 = clockViewMarginTop - i;
        int i3 = rect.top;
        return i2 > i3 ? clockViewMarginTop - i : i3;
    }
}
