package com.oneplus.aod.utils.burnin;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.oneplus.aod.controller.IOpClockController;
import com.oneplus.aod.views.IOpAodClock;
public class OpBurnInAlignController extends OpBurnInBaseController {
    private int mLastAlign;
    private int mMovingAlign;

    public OpBurnInAlignController(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.burnin.OpBurnInBaseController
    public void handleSetup(IOpClockController iOpClockController) {
        reset();
    }

    @Override // com.oneplus.aod.utils.burnin.IBurnInProtectionController
    public void reset() {
        this.mLastAlign = 1;
        this.mMovingAlign = 1;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.burnin.OpBurnInBaseController
    public void handleOnAlarm(View view, ViewGroup viewGroup) {
        Rect rect = new Rect(this.mBound);
        validateNextAction(rect);
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "handleOnAlarm: range= " + rect + ", move from " + this.mLastAlign + " to " + this.mMovingAlign);
        }
        movePosition(view, viewGroup, this.mMovingAlign);
    }

    private void movePosition(View view, ViewGroup viewGroup, int i) {
        IOpAodClock iOpAodClock = (IOpAodClock) view;
        if (iOpAodClock != null) {
            iOpAodClock.alignforBurnIn(i);
        }
        int childCount = viewGroup.getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = viewGroup.getChildAt(i2);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) childAt.getLayoutParams();
            layoutParams.gravity = i;
            childAt.setLayoutParams(layoutParams);
        }
    }

    private void validateNextAction(Rect rect) {
        int i = this.mMovingAlign;
        int i2 = 8388613;
        if (i != 1) {
            if (i == 8388611) {
                Log.d(this.mTag, "align center");
            } else if (i != 8388613) {
                i2 = 0;
            } else {
                Log.d(this.mTag, "align center");
            }
            i2 = 1;
        } else if (this.mLastAlign == 8388611) {
            Log.d(this.mTag, "align end");
        } else {
            Log.d(this.mTag, "align start");
            i2 = 8388611;
        }
        this.mLastAlign = this.mMovingAlign;
        this.mMovingAlign = i2;
    }
}
