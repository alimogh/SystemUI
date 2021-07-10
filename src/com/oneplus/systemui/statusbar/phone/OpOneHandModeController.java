package com.oneplus.systemui.statusbar.phone;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.recents.OverviewProxyService;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
public class OpOneHandModeController {
    private final Context mContext;
    private boolean mIsOneHandedPerformed = false;
    private int mIsOneHandedSettingEnable = 0;
    private ArrayList<OneHandModeStateListener> mListeners = new ArrayList<>();
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    private ContentObserver mOneHandedGestureObserver = new ContentObserver(this.mMainThreadHandler) { // from class: com.oneplus.systemui.statusbar.phone.OpOneHandModeController.1
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            int intForUser = Settings.System.getIntForUser(OpOneHandModeController.this.mContext.getContentResolver(), "op_one_hand_mode_setting", 0, -2);
            OpOneHandModeController opOneHandModeController = OpOneHandModeController.this;
            opOneHandModeController.mIsOneHandedPerformed = "1".equals(Settings.Global.getStringForUser(opOneHandModeController.mContext.getContentResolver(), "one_hand_mode_status", -1));
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.d("OpOneHandModeController", "Trigger change by self. one handed mode type from " + OpOneHandModeController.this.mIsOneHandedSettingEnable + " to " + intForUser);
            }
            OpOneHandModeController.this.mIsOneHandedSettingEnable = intForUser;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (uri.equals(Settings.System.getUriFor("op_one_hand_mode_setting"))) {
                int intForUser = Settings.System.getIntForUser(OpOneHandModeController.this.mContext.getContentResolver(), "op_one_hand_mode_setting", 0, -2);
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.d("OpOneHandModeController", "Trigger change by observer uri. one handed mode type from " + OpOneHandModeController.this.mIsOneHandedSettingEnable + " to " + intForUser);
                }
                OpOneHandModeController.this.mIsOneHandedSettingEnable = intForUser;
                for (int i = 0; i < OpOneHandModeController.this.mListeners.size(); i++) {
                    ((OneHandModeStateListener) OpOneHandModeController.this.mListeners.get(i)).onOneHandEnableStateChange(OpOneHandModeController.this.isOneHandedSettingEnable());
                }
            }
            if (uri.equals(Settings.Global.getUriFor("one_hand_mode_status"))) {
                boolean equals = "1".equals(Settings.Global.getStringForUser(OpOneHandModeController.this.mContext.getContentResolver(), "one_hand_mode_status", -1));
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.d("OpOneHandModeController", "Trigger chang by observer uri. one handed mode status from " + OpOneHandModeController.this.mIsOneHandedPerformed + " to " + equals);
                }
                OpOneHandModeController.this.mIsOneHandedPerformed = equals;
                for (int i2 = 0; i2 < OpOneHandModeController.this.mListeners.size(); i2++) {
                    ((OneHandModeStateListener) OpOneHandModeController.this.mListeners.get(i2)).onOneHandPerformStateChange(OpOneHandModeController.this.mIsOneHandedPerformed);
                }
                if (!equals) {
                    OpOneHandModeController.this.notifyLeaveOneHandedMode();
                }
            }
        }
    };

    public interface OneHandModeStateListener {
        void onOneHandEnableStateChange(boolean z);

        void onOneHandPerformStateChange(boolean z);
    }

    public OpOneHandModeController(Context context) {
        this.mContext = context;
        OverviewProxyService overviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        if (this.mOneHandedGestureObserver != null) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("op_one_hand_mode_setting"), true, this.mOneHandedGestureObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("one_hand_mode_status"), true, this.mOneHandedGestureObserver, -1);
            this.mOneHandedGestureObserver.onChange(true);
        }
    }

    public void addListener(OneHandModeStateListener oneHandModeStateListener) {
        if (!this.mListeners.contains(oneHandModeStateListener)) {
            this.mListeners.add(oneHandModeStateListener);
        }
        oneHandModeStateListener.onOneHandEnableStateChange(isOneHandedSettingEnable());
        oneHandModeStateListener.onOneHandPerformStateChange(this.mIsOneHandedPerformed);
    }

    public void removeListener(OneHandModeStateListener oneHandModeStateListener) {
        this.mListeners.remove(oneHandModeStateListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isOneHandedSettingEnable() {
        return this.mIsOneHandedSettingEnable == 1;
    }

    public void notifyLeaveOneHandedMode() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpOneHandModeController", "notifyLeaveOneHandedMode");
        }
    }
}
