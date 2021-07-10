package com.oneplus.aod.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.oneplus.aod.OpClockViewCtrl;
import com.oneplus.aod.controller.IOpClockController;
import com.oneplus.aod.utils.burnin.IBurnInProtectionController;
import com.oneplus.aod.utils.burnin.OpBurnInAlignController;
import com.oneplus.aod.utils.burnin.OpBurnInVerticalController;
import com.oneplus.aod.utils.burnin.OpParsonsBurnInController;
import java.util.HashMap;
public class OpAodBurnInProtectionHelper implements OpClockViewCtrl.OpClockOnChangeListener, View.OnAttachStateChangeListener {
    private IBurnInProtectionController mBurnInController;
    private HashMap<String, IBurnInProtectionController> mBurnInProtections = new HashMap<>();
    private ViewGroup mClockContainer;
    private Context mContext;
    private long mCurrentTime;
    private ViewGroup mSystemInfoView;
    private KeyguardUpdateMonitor mUpdateMonitor;

    public OpAodBurnInProtectionHelper(Context context) {
        this.mContext = context;
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(AlarmManager.class);
        initControllers();
    }

    private void initControllers() {
        this.mBurnInProtections.put(OpBurnInVerticalController.class.getName(), new OpBurnInVerticalController(this.mContext));
        this.mBurnInProtections.put(OpBurnInAlignController.class.getName(), new OpBurnInAlignController(this.mContext));
        this.mBurnInProtections.put(OpParsonsBurnInController.class.getName(), new OpParsonsBurnInController(this.mContext));
    }

    public void onTimeChanged() {
        if (getClockView() != null && this.mUpdateMonitor.isAlwaysOnEnabled()) {
            long elapsedRealtime = SystemClock.elapsedRealtime() / 1000;
            int moveDelay = getMoveDelay() * 59;
            if (Build.DEBUG_ONEPLUS) {
                Log.i("OpAodBurnInProtectionHelper", "onTimeChanged: mCurrentTime= " + this.mCurrentTime + ", delta = " + (elapsedRealtime - this.mCurrentTime) + ", threshold= " + moveDelay);
            }
            if (elapsedRealtime - this.mCurrentTime > ((long) moveDelay)) {
                onAlarm();
                this.mCurrentTime = elapsedRealtime;
            }
        }
    }

    public void registerViews(ViewGroup viewGroup, ViewGroup viewGroup2) {
        this.mClockContainer = viewGroup;
        this.mSystemInfoView = viewGroup2;
    }

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewAttachedToWindow(View view) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpAodBurnInProtectionHelper", "onViewAttachedToWindow");
        }
        IBurnInProtectionController iBurnInProtectionController = this.mBurnInController;
        if (iBurnInProtectionController != null) {
            iBurnInProtectionController.reset();
        }
        resetAlarm();
    }

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewDetachedFromWindow(View view) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpAodBurnInProtectionHelper", "onViewDetachedFromWindow");
        }
        resetAlarm();
    }

    @Override // com.oneplus.aod.OpClockViewCtrl.OpClockOnChangeListener
    public void onClockChanged(IOpClockController iOpClockController) {
        IBurnInProtectionController iBurnInProtectionController = this.mBurnInController;
        if (iBurnInProtectionController != null) {
            iBurnInProtectionController.release();
            this.mBurnInController = null;
        }
        if (iOpClockController != null) {
            String burnInHandleClassName = iOpClockController.getBurnInHandleClassName();
            if (!TextUtils.isEmpty(burnInHandleClassName)) {
                this.mBurnInController = this.mBurnInProtections.get(burnInHandleClassName);
                if (Build.DEBUG_ONEPLUS) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("onClockChanged: burnin handle class= ");
                    sb.append(burnInHandleClassName);
                    sb.append(", exists= ");
                    sb.append(this.mBurnInController != null);
                    Log.d("OpAodBurnInProtectionHelper", sb.toString());
                }
                IBurnInProtectionController iBurnInProtectionController2 = this.mBurnInController;
                if (iBurnInProtectionController2 != null) {
                    iBurnInProtectionController2.setup(this.mClockContainer, this.mSystemInfoView, iOpClockController);
                }
            }
        }
    }

    private void onAlarm() {
        if (getClockView() != null) {
            IBurnInProtectionController iBurnInProtectionController = this.mBurnInController;
            if (iBurnInProtectionController != null) {
                iBurnInProtectionController.onAlarm();
            } else {
                Log.w("OpAodBurnInProtectionHelper", "onAlarm: controller not exists");
            }
        }
    }

    public void moveBackToOriginalPosition(Runnable runnable) {
        Log.d("OpAodBurnInProtectionHelper", "moveBackToOriginalPosition");
        resetAlarm();
        IBurnInProtectionController iBurnInProtectionController = this.mBurnInController;
        if (iBurnInProtectionController != null) {
            iBurnInProtectionController.moveBackToOriginalPosition(runnable);
        } else {
            Log.w("OpAodBurnInProtectionHelper", "moveBackToOriginalPosition: controller not exists");
        }
    }

    public void recover() {
        Log.d("OpAodBurnInProtectionHelper", "recover");
        resetAlarm();
        IBurnInProtectionController iBurnInProtectionController = this.mBurnInController;
        if (iBurnInProtectionController != null) {
            iBurnInProtectionController.recover();
        } else {
            Log.w("OpAodBurnInProtectionHelper", "recover: controller not exists");
        }
    }

    private View getClockView() {
        ViewGroup viewGroup = this.mClockContainer;
        if (viewGroup == null || viewGroup.getChildCount() <= 0) {
            return null;
        }
        return this.mClockContainer.getChildAt(0);
    }

    public static int getMoveDelay() {
        int i = SystemProperties.getInt("sys.aod.move_delay", 0);
        if (i == 0) {
            return 1;
        }
        Log.d("OpAodBurnInProtectionHelper", "getMoveDelay: override to " + i + " minute");
        return i;
    }

    private void resetAlarm() {
        this.mCurrentTime = SystemClock.elapsedRealtime() / 1000;
    }
}
