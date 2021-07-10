package com.oneplus.aod;

import android.content.Context;
import android.content.res.TypedArray;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0001R$array;
import com.android.systemui.C0008R$id;
import com.oneplus.aod.OpDateTimeView;
import com.oneplus.aod.controller.IOpClockController;
import com.oneplus.aod.controller.OpBuildInClockController;
import com.oneplus.aod.utils.OpAodBurnInProtectionHelper;
import com.oneplus.aod.utils.OpAodXmlParser;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
public class OpClockViewCtrl {
    private static int mClockStyle = -1;
    private OpAodBurnInProtectionHelper mAodBurnInProtectionHelper;
    private final SparseArray<Integer> mBuildInClockStyleMapping = new SparseArray<>();
    private FrameLayout mClockContainer;
    private IOpClockController mClockController;
    private View mClockView;
    private final Context mContext;
    private final HashMap<String, IOpClockController> mControllerList = new HashMap<>();
    private boolean mDreaming;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final ArrayList<OpClockOnChangeListener> mListeners = new ArrayList<>();
    private ViewGroup mSystemInfoContainer;
    private int mUserId;

    public interface OpClockOnChangeListener {
        void onClockChanged(IOpClockController iOpClockController);
    }

    public OpClockViewCtrl(Context context) {
        this.mContext = context;
        this.mUserId = KeyguardUpdateMonitor.getCurrentUser();
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        initBuildInMapping();
        OpAodBurnInProtectionHelper opAodBurnInProtectionHelper = new OpAodBurnInProtectionHelper(context);
        this.mAodBurnInProtectionHelper = opAodBurnInProtectionHelper;
        addOnChangeListener(opAodBurnInProtectionHelper);
        initControllers();
    }

    public void initViews(ViewGroup viewGroup) {
        this.mClockContainer = (FrameLayout) viewGroup.findViewById(C0008R$id.op_aod_clock_container);
        ViewGroup viewGroup2 = (ViewGroup) viewGroup.findViewById(C0008R$id.op_aod_system_info_container);
        this.mSystemInfoContainer = viewGroup2;
        this.mAodBurnInProtectionHelper.registerViews(this.mClockContainer, viewGroup2);
        updateClockDB(true);
    }

    public void updateClockDB(boolean z) {
        int intForUser = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "aod_clock_style", 0, this.mUserId);
        if (OpUtils.isMCLVersion() || intForUser != 40) {
            updateClockDB(intForUser, z);
            return;
        }
        Log.d("ClockViewCtrl", "Set clock style failed. Invalid clock style: " + intForUser);
    }

    public void updateClockDB(int i, boolean z) {
        if (z || mClockStyle != i) {
            mClockStyle = i;
            if (this.mClockController != null) {
                this.mClockContainer.removeAllViews();
                View view = this.mClockView;
                if (view != null) {
                    view.removeOnAttachStateChangeListener(this.mAodBurnInProtectionHelper);
                    this.mClockView = null;
                }
                this.mClockController.onDestroyView();
            }
            IOpClockController chooseController = chooseController();
            this.mClockController = chooseController;
            if (chooseController != null) {
                View clockView = chooseController.getClockView();
                this.mClockView = clockView;
                if (clockView != null) {
                    clockView.addOnAttachStateChangeListener(this.mAodBurnInProtectionHelper);
                    this.mClockContainer.addView(this.mClockView);
                }
            }
            dispatchClockChanged();
            OpDateTimeView.Patterns.update(this.mContext, false, mClockStyle);
            Log.d("ClockViewCtrl", "updateClock: style = " + mClockStyle + ", user = " + this.mUserId);
            if (this.mDreaming) {
                refreshTime();
            }
        }
    }

    public void startDozing() {
        refreshTime();
    }

    public void onTimeChanged() {
        refreshTime();
        this.mAodBurnInProtectionHelper.onTimeChanged();
    }

    public void onTimeZoneChanged(TimeZone timeZone) {
        IOpClockController iOpClockController = this.mClockController;
        if (iOpClockController != null) {
            iOpClockController.onTimeZoneChanged(timeZone);
        }
    }

    public void onUserSwitchComplete(int i) {
        this.mUserId = i;
        updateClockDB(true);
    }

    public void onDreamingStateChanged(boolean z) {
        this.mDreaming = z;
    }

    public void onScreenTurningOn() {
        if (this.mDreaming) {
            refreshTime();
        }
    }

    public void onScreenTurnedOn() {
        if (this.mDreaming) {
            refreshTime();
            IOpClockController iOpClockController = this.mClockController;
            if (iOpClockController != null) {
                iOpClockController.onScreenTurnedOn();
            }
            userActivityInAlwaysOn("screen turned on");
        }
    }

    public void onScreenTurnedOff() {
        if (this.mDreaming) {
            IOpClockController iOpClockController = this.mClockController;
            if (iOpClockController != null) {
                iOpClockController.onScreenTurnedOff();
            }
            if (this.mKeyguardUpdateMonitor.isAlwaysOnEnabled()) {
                this.mAodBurnInProtectionHelper.recover();
            }
        }
    }

    private void refreshTime() {
        IOpClockController iOpClockController = this.mClockController;
        if (iOpClockController != null) {
            iOpClockController.onTimeTick();
        }
    }

    private void initControllers() {
        this.mControllerList.put(OpBuildInClockController.class.getName(), new OpBuildInClockController(this.mContext));
    }

    private IOpClockController chooseController() {
        Integer num = this.mBuildInClockStyleMapping.get(mClockStyle);
        if (num == null) {
            Log.w("ClockViewCtrl", "chooseController: no matching from mapping");
            return null;
        }
        String controllerName = OpAodXmlParser.getControllerName(this.mContext, num.intValue());
        if (TextUtils.isEmpty(controllerName)) {
            Log.w("ClockViewCtrl", "chooseController: no matching from xml");
            return null;
        }
        IOpClockController iOpClockController = this.mControllerList.get(controllerName);
        if (iOpClockController != null) {
            iOpClockController.updateSettings(num.intValue());
        } else {
            Log.w("ClockViewCtrl", "chooseController: unsupport controller = " + controllerName);
        }
        return iOpClockController;
    }

    public IOpClockController getController() {
        return this.mClockController;
    }

    public static int getClockStyle() {
        return mClockStyle;
    }

    public void addOnChangeListener(OpClockOnChangeListener opClockOnChangeListener) {
        if (opClockOnChangeListener != null) {
            if (!this.mListeners.contains(opClockOnChangeListener)) {
                this.mListeners.add(opClockOnChangeListener);
            }
            opClockOnChangeListener.onClockChanged(this.mClockController);
        }
    }

    public void removeOnChangeListener(OpClockOnChangeListener opClockOnChangeListener) {
        this.mListeners.remove(opClockOnChangeListener);
    }

    private void dispatchClockChanged() {
        Iterator<OpClockOnChangeListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onClockChanged(this.mClockController);
        }
    }

    private void initBuildInMapping() {
        TypedArray obtainTypedArray = this.mContext.getResources().obtainTypedArray(C0001R$array.op_aod_clock_style_mapping);
        int length = obtainTypedArray.length();
        this.mBuildInClockStyleMapping.clear();
        for (int i = 0; i < length; i += 2) {
            int i2 = i + 1;
            if (i2 < length) {
                int i3 = obtainTypedArray.getInt(i, -1);
                int resourceId = obtainTypedArray.getResourceId(i2, -1);
                if (i3 == -1 || resourceId == -1) {
                    Log.w("ClockViewCtrl", "build in mapping (" + i + ") key or value is NO_ID");
                } else {
                    this.mBuildInClockStyleMapping.put(i3, Integer.valueOf(resourceId));
                }
            } else {
                Log.w("ClockViewCtrl", "build in mapping (" + i + ") has no xml resource");
            }
        }
        obtainTypedArray.recycle();
    }

    public void userActivityInAlwaysOn(final String str) {
        if (this.mKeyguardUpdateMonitor.isAlwaysOnEnabled()) {
            this.mAodBurnInProtectionHelper.moveBackToOriginalPosition(new Runnable() { // from class: com.oneplus.aod.OpClockViewCtrl.1
                @Override // java.lang.Runnable
                public void run() {
                    OpClockViewCtrl.this.mKeyguardUpdateMonitor.showFodAndCountdownToHide(str);
                }
            });
        }
    }

    public void onUserTrigger(int i) {
        IOpClockController iOpClockController = this.mClockController;
        if (iOpClockController != null) {
            iOpClockController.onUserTrigger(i);
        }
    }

    public void onFodShowOrHideOnAod(boolean z) {
        IOpClockController iOpClockController = this.mClockController;
        if (iOpClockController != null) {
            iOpClockController.onFodShowOrHideOnAod(z);
        }
    }

    public void onFodIndicationVisibilityChanged(boolean z) {
        IOpClockController iOpClockController = this.mClockController;
        if (iOpClockController != null) {
            iOpClockController.onFodIndicationVisibilityChanged(z);
        }
    }

    public void recover() {
        if (this.mDreaming) {
            this.mAodBurnInProtectionHelper.recover();
        }
    }
}
