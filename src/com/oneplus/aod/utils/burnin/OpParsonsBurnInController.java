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
public class OpParsonsBurnInController extends OpBurnInBaseController {
    private int mCenterCount;
    private int mEndCount;
    private int mMovingAlign;
    private int mStartCount;
    private int mStep;

    public OpParsonsBurnInController(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.burnin.OpBurnInBaseController
    public void handleSetup(IOpClockController iOpClockController) {
        reset();
        this.mStartCount = 7;
        this.mCenterCount = 1;
        this.mEndCount = 7;
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "setup: start= " + this.mStartCount + ", center= " + this.mCenterCount + ", end= " + this.mEndCount);
        }
    }

    @Override // com.oneplus.aod.utils.burnin.IBurnInProtectionController
    public void reset() {
        this.mMovingAlign = 1;
        this.mStep = 1;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.burnin.OpBurnInBaseController
    public void handleOnAlarm(View view, ViewGroup viewGroup) {
        Rect rect = new Rect(this.mBound);
        validateNextAction(rect);
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "handleOnAlarm: range= " + rect + ", align= " + logAlignment() + ", step= " + this.mStep);
        }
        movePosition(view, viewGroup, this.mMovingAlign, this.mStep);
    }

    private void movePosition(View view, ViewGroup viewGroup, int i, int i2) {
        IOpAodClock iOpAodClock = (IOpAodClock) view;
        if (iOpAodClock != null) {
            iOpAodClock.alignforBurnIn2(viewGroup, i, i2);
        }
        int childCount = viewGroup.getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = viewGroup.getChildAt(i3);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) childAt.getLayoutParams();
            layoutParams.gravity = i;
            childAt.setLayoutParams(layoutParams);
        }
    }

    private void validateNextAction(Rect rect) {
        int i = this.mMovingAlign;
        if (i == 1) {
            int i2 = this.mStep;
            if (i2 < this.mCenterCount) {
                this.mStep = i2 + 1;
                return;
            }
            Log.d(this.mTag, "align end");
            this.mMovingAlign = 8388613;
            this.mStep = 0;
        } else if (i == 8388611) {
            int i3 = this.mStep;
            if (i3 < this.mStartCount) {
                this.mStep = i3 + 1;
                return;
            }
            Log.d(this.mTag, "align center");
            this.mMovingAlign = 1;
            this.mStep = 0;
        } else if (i == 8388613) {
            int i4 = this.mStep;
            if (i4 < this.mEndCount) {
                this.mStep = i4 + 1;
                return;
            }
            Log.d(this.mTag, "align start");
            this.mMovingAlign = 8388611;
            this.mStep = 0;
        }
    }

    private String logAlignment() {
        int i = this.mMovingAlign;
        if (i == 8388611) {
            return "start";
        }
        if (i == 8388613) {
            return "end";
        }
        return i == 1 ? "center horizontal" : "no match";
    }
}
