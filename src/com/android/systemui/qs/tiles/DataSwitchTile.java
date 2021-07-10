package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.SysUIToast;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkControllerImpl;
import com.oneplus.util.OpUtils;
import com.oneplus.util.SystemSetting;
import java.lang.reflect.Method;
import org.codeaurora.internal.IExtTelephony;
public class DataSwitchTile extends QSTileImpl<QSTile.BooleanState> {
    static final Intent DATA_SWITCH_SETTINGS = new Intent("oneplus.intent.action.SIM_AND_NETWORK_SETTINGS");
    private boolean mCanSwitch = true;
    protected final NetworkController mController;
    private SystemSetting mEsportModeSetting;
    private IExtTelephony mExtTelephony = null;
    private MyCallStateListener mPhoneStateListener;
    private boolean mRegistered = false;
    protected final DataSwitchSignalCallback mSignalCallback = new DataSwitchSignalCallback();
    private int mSimCount = 0;
    BroadcastReceiver mSimReceiver = new BroadcastReceiver() { // from class: com.android.systemui.qs.tiles.DataSwitchTile.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Log.d(((QSTileImpl) DataSwitchTile.this).TAG, "mSimReceiver:onReceive");
            DataSwitchTile.this.refreshState();
        }
    };
    private SubscriptionManager mSubscriptionManager;
    private TelephonyManager mTelephonyManager;
    private boolean mVirtualSimExist = false;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return 2002;
    }

    public DataSwitchTile(QSHost qSHost) {
        super(qSHost);
        this.mSubscriptionManager = SubscriptionManager.from(qSHost.getContext());
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mPhoneStateListener = new MyCallStateListener();
        this.mController = (NetworkController) Dependency.get(NetworkController.class);
        this.mEsportModeSetting = new SystemSetting(this.mContext, null, "esport_mode_enabled", true) { // from class: com.android.systemui.qs.tiles.DataSwitchTile.1
            /* access modifiers changed from: protected */
            @Override // com.oneplus.util.SystemSetting
            public void handleValueChanged(int i, boolean z) {
                String str = ((QSTileImpl) DataSwitchTile.this).TAG;
                Log.d(str, "handleValueChanged: ESPORT_MODE_STATUS=" + i);
                DataSwitchTile.this.refreshState();
            }
        };
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        int simCount = OpUtils.getSimCount();
        String str = this.TAG;
        Log.d(str, "phoneCount: " + simCount);
        return simCount >= 2;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mEsportModeSetting.setListening(z);
        if (z) {
            if (!this.mRegistered) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
                this.mContext.registerReceiver(this.mSimReceiver, intentFilter);
                this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
                this.mController.addCallback((NetworkController.SignalCallback) this.mSignalCallback);
                this.mRegistered = true;
            }
            refreshState();
        } else if (this.mRegistered) {
            this.mContext.unregisterReceiver(this.mSimReceiver);
            this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
            this.mController.removeCallback((NetworkController.SignalCallback) this.mSignalCallback);
            this.mRegistered = false;
        }
    }

    private void updateSimCount() {
        String str = SystemProperties.get("gsm.sim.state");
        Log.d(this.TAG, "DataSwitchTile:updateSimCount:simState=" + str);
        this.mSimCount = 0;
        try {
            String[] split = TextUtils.split(str, ",");
            for (int i = 0; i < split.length; i++) {
                if (!split[i].isEmpty() && !split[i].equalsIgnoreCase("ABSENT")) {
                    if (!split[i].equalsIgnoreCase("NOT_READY")) {
                        this.mSimCount++;
                    }
                }
            }
        } catch (Exception unused) {
            Log.e(this.TAG, "Error to parse sim state");
        }
        Log.d(this.TAG, "DataSwitchTile:updateSimCount:mSimCount=" + this.mSimCount);
    }

    private void setDefaultDataSimIndex(int i) {
        try {
            if (this.mExtTelephony == null) {
                this.mExtTelephony = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
            }
            String str = this.TAG;
            Log.d(str, "oemDdsSwitch:phoneId=" + i);
            Method declaredMethod = this.mExtTelephony.getClass().getDeclaredMethod("oemDdsSwitch", Integer.TYPE);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(this.mExtTelephony, Integer.valueOf(i));
        } catch (Exception e) {
            Log.d(this.TAG, "setDefaultDataSimId", e);
            Log.d(this.TAG, "clear ext telephony service ref");
            this.mExtTelephony = null;
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (this.mEsportModeSetting.getValue() != 1) {
            if (!this.mCanSwitch) {
                String str = this.TAG;
                Log.d(str, "Call state=" + this.mTelephonyManager.getCallState());
            } else if (this.mVirtualSimExist) {
                Log.d(this.TAG, "virtual sim exist. ignore click.");
            } else {
                int i = this.mSimCount;
                if (i == 0) {
                    Log.d(this.TAG, "handleClick:no sim card");
                    Context context = this.mContext;
                    SysUIToast.makeText(context, context.getString(C0015R$string.quick_settings_data_switch_toast_0), 1).show();
                } else if (i == 1) {
                    Log.d(this.TAG, "handleClick:only one sim card");
                    Context context2 = this.mContext;
                    SysUIToast.makeText(context2, context2.getString(C0015R$string.quick_settings_data_switch_toast_1), 1).show();
                } else {
                    AsyncTask.execute(new Runnable() { // from class: com.android.systemui.qs.tiles.-$$Lambda$DataSwitchTile$SMUKxUiuh-wmJK6getmuCjTYAmY
                        @Override // java.lang.Runnable
                        public final void run() {
                            DataSwitchTile.this.lambda$handleClick$0$DataSwitchTile();
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleClick$0 */
    public /* synthetic */ void lambda$handleClick$0$DataSwitchTile() {
        setDefaultDataSimIndex(1 - this.mSubscriptionManager.getDefaultDataPhoneId());
        refreshState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return DATA_SWITCH_SETTINGS;
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_data_switch_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z;
        int i = 1;
        if (obj == null) {
            int defaultDataPhoneId = this.mSubscriptionManager.getDefaultDataPhoneId();
            Log.d(this.TAG, "default data phone id=" + defaultDataPhoneId);
            z = defaultDataPhoneId == 0;
        } else {
            z = ((Boolean) obj).booleanValue();
        }
        updateSimCount();
        int i2 = this.mSimCount;
        if (i2 == 1) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(z ? C0006R$drawable.op_ic_qs_data_switch_1 : C0006R$drawable.op_ic_qs_data_switch_2);
            booleanState.value = false;
        } else if (i2 != 2) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_qs_data_switch_1);
            booleanState.value = false;
        } else {
            booleanState.icon = QSTileImpl.ResourceIcon.get(z ? C0006R$drawable.op_ic_qs_data_switch_1 : C0006R$drawable.op_ic_qs_data_switch_2);
            booleanState.value = true;
        }
        if (this.mEsportModeSetting.getValue() == 1) {
            Log.d(this.TAG, "E-Sport, set to unavailable.");
            booleanState.state = 0;
        } else if (this.mSimCount < 2) {
            booleanState.state = 0;
        } else if (this.mVirtualSimExist) {
            booleanState.state = 0;
            Log.d(this.TAG, "virtual sim exist, set to unavailable.");
        } else if (!this.mCanSwitch) {
            booleanState.state = 0;
            Log.d(this.TAG, "call state isn't idle, set to unavailable.");
        } else {
            if (booleanState.value) {
                i = 2;
            }
            booleanState.state = i;
        }
        if (z) {
            booleanState.contentDescription = this.mContext.getString(C0015R$string.accessibility_quick_settings_data_switch_changed_1);
        } else {
            booleanState.contentDescription = this.mContext.getString(C0015R$string.accessibility_quick_settings_data_switch_changed_2);
        }
        booleanState.label = this.mContext.getString(C0015R$string.quick_settings_data_switch_label);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0015R$string.accessibility_quick_settings_data_switch_changed_1);
        }
        return this.mContext.getString(C0015R$string.accessibility_quick_settings_data_switch_changed_2);
    }

    class MyCallStateListener extends PhoneStateListener {
        MyCallStateListener() {
        }

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int i, String str) {
            DataSwitchTile dataSwitchTile = DataSwitchTile.this;
            dataSwitchTile.mCanSwitch = dataSwitchTile.mTelephonyManager.getCallState() == 0;
            DataSwitchTile.this.refreshState();
        }
    }

    protected final class DataSwitchSignalCallback implements NetworkController.SignalCallback {
        protected DataSwitchSignalCallback() {
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setVirtualSimstate(int[] iArr) {
            boolean z = false;
            if (iArr != null && iArr.length > 0) {
                int length = iArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (iArr[i] != NetworkControllerImpl.SOFTSIM_DISABLE) {
                        z = true;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            Log.d(((QSTileImpl) DataSwitchTile.this).TAG, "virtual sim state change: " + DataSwitchTile.this.mVirtualSimExist + " to " + z);
            DataSwitchTile.this.mVirtualSimExist = z;
            DataSwitchTile.this.refreshState();
        }
    }
}
