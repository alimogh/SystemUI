package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import androidx.lifecycle.Lifecycle;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.wifi.AccessPoint;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.AlphaControlledSignalTileView;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.WifiIcons;
import com.oneplus.systemui.statusbar.policy.OpWifiIcons;
import com.oneplus.systemui.util.OpTetherUtils;
import com.oneplus.util.OpUtils;
import java.util.List;
public class WifiTile extends QSTileImpl<QSTile.SignalState> {
    private static final Intent WIFI_SETTINGS = new Intent("android.settings.WIFI_SETTINGS");
    private final ActivityStarter mActivityStarter;
    private long mClickTimeMillis = -1;
    protected final NetworkController mController;
    private final WifiDetailAdapter mDetailAdapter;
    private boolean mExpectDisabled;
    protected final WifiSignalCallback mSignalCallback = new WifiSignalCallback();
    private final QSTile.SignalState mStateBeforeClick = newTileState();
    private boolean mTransientEnabling = false;
    private final NetworkController.AccessPointController mWifiController;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowNoTitle;
    }

    static {
        new Intent("android.settings.panel.action.WIFI");
    }

    public WifiTile(QSHost qSHost, NetworkController networkController, ActivityStarter activityStarter) {
        super(qSHost);
        this.mController = networkController;
        this.mWifiController = networkController.getAccessPointController();
        this.mDetailAdapter = (WifiDetailAdapter) createDetailAdapter();
        this.mActivityStarter = activityStarter;
        this.mController.observe(getLifecycle(), (Lifecycle) this.mSignalCallback);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.SignalState newTileState() {
        return new QSTile.SignalState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public void setDetailListening(boolean z) {
        if (z) {
            this.mWifiController.addAccessPointCallback(this.mDetailAdapter);
        } else {
            this.mWifiController.removeAccessPointCallback(this.mDetailAdapter);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    /* access modifiers changed from: protected */
    public DetailAdapter createDetailAdapter() {
        return new WifiDetailAdapter();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public QSIconView createTileView(Context context) {
        return new AlphaControlledSignalTileView(context);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return WIFI_SETTINGS;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        Object obj = null;
        if (OpUtils.isUST() && OpTetherUtils.isTetheringOpen(this.mContext) && !((QSTile.SignalState) this.mState).value) {
            if (OpTetherUtils.isNeedShowDialog(this.mContext)) {
                OpTetherUtils.showUstAlertDialog(this.mContext, null, false);
            }
            OpTetherUtils.disableTmoTethering(this.mContext);
        }
        ((QSTile.SignalState) this.mState).copyTo(this.mStateBeforeClick);
        boolean z = ((QSTile.SignalState) this.mState).value;
        boolean z2 = !z;
        this.mTransientEnabling = z2;
        if (z2) {
            this.mClickTimeMillis = SystemClock.uptimeMillis();
        }
        if (!z) {
            obj = QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        }
        refreshState(obj);
        this.mController.setWifiEnabled(!z);
        this.mExpectDisabled = z;
        if (z) {
            this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.qs.tiles.-$$Lambda$WifiTile$FBMX-zj483F7uFPAUwutmnquiRU
                @Override // java.lang.Runnable
                public final void run() {
                    WifiTile.this.lambda$handleClick$0$WifiTile();
                }
            }, 0);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleClick$0 */
    public /* synthetic */ void lambda$handleClick$0$WifiTile() {
        if (this.mExpectDisabled) {
            this.mExpectDisabled = false;
            refreshState();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSecondaryClick() {
        if (!this.mWifiController.canConfigWifi()) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.settings.WIFI_SETTINGS"), 0);
            return;
        }
        showDetail(true);
        if (!((QSTile.SignalState) this.mState).value) {
            this.mController.setWifiEnabled(true);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0015R$string.quick_settings_wifi_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.SignalState signalState, Object obj) {
        if (QSTileImpl.DEBUG) {
            String str = this.TAG;
            Log.d(str, "handleUpdateState arg=" + obj);
        }
        CallbackInfo callbackInfo = this.mSignalCallback.mInfo;
        if (this.mExpectDisabled) {
            if (!callbackInfo.enabled) {
                this.mExpectDisabled = false;
            } else if (Build.DEBUG_ONEPLUS) {
                Log.d(this.TAG, "handleUpdateState: skipping update.");
                return;
            } else {
                return;
            }
        }
        boolean z = obj == QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        boolean z2 = callbackInfo.enabled && callbackInfo.wifiSignalIconId > 0 && callbackInfo.ssid != null;
        boolean z3 = callbackInfo.wifiSignalIconId > 0 && callbackInfo.ssid == null;
        if (signalState.value != callbackInfo.enabled) {
            this.mDetailAdapter.setItemsVisible(callbackInfo.enabled);
            fireToggleStateChanged(callbackInfo.enabled);
        }
        if (signalState.slash == null) {
            QSTile.SlashState slashState = new QSTile.SlashState();
            signalState.slash = slashState;
            slashState.rotation = 6.0f;
        }
        signalState.slash.isSlashed = false;
        if (this.mTransientEnabling && SystemClock.uptimeMillis() - this.mClickTimeMillis >= 10000) {
            this.mTransientEnabling = false;
        }
        boolean z4 = callbackInfo.enabled ? false : this.mTransientEnabling;
        this.mTransientEnabling = z4;
        boolean z5 = z | z4;
        boolean z6 = z5 || callbackInfo.isTransient;
        signalState.secondaryLabel = getSecondaryLabel(z6, callbackInfo.statusLabel);
        signalState.state = 2;
        signalState.dualTarget = true;
        signalState.value = z5 || callbackInfo.enabled;
        signalState.activityIn = callbackInfo.enabled && callbackInfo.activityIn;
        signalState.activityOut = callbackInfo.enabled && callbackInfo.activityOut;
        StringBuffer stringBuffer = new StringBuffer();
        StringBuffer stringBuffer2 = new StringBuffer();
        Resources resources = this.mContext.getResources();
        if (Build.DEBUG_ONEPLUS) {
            String str2 = this.TAG;
            Log.d(str2, "handleUpdateState: state.value=" + signalState.value + ", cb.enabled=" + callbackInfo.enabled + ", cb.isTran=" + callbackInfo.isTransient + ", mTrans=" + this.mTransientEnabling + ", wifiConned=" + z2 + ", wifiNotConned=" + z3);
        }
        if (OpUtils.isUSS()) {
            if (z6) {
                signalState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_ic_signal_wifi_transient_animation);
                signalState.label = resources.getString(C0015R$string.quick_settings_wifi_label);
            } else if (!signalState.value) {
                signalState.slash.isSlashed = true;
                signalState.state = 1;
                signalState.icon = QSTileImpl.ResourceIcon.get(OpWifiIcons.OP_QS_WIFI_DISABLED);
                signalState.label = resources.getString(C0015R$string.quick_settings_wifi_label);
            } else if (z2) {
                signalState.icon = QSTileImpl.ResourceIcon.get(callbackInfo.wifiSignalIconId);
                signalState.label = removeDoubleQuotes(callbackInfo.ssid);
            } else if (z3) {
                signalState.icon = QSTileImpl.ResourceIcon.get(WifiIcons.QS_WIFI_NO_NETWORK);
                signalState.label = resources.getString(C0015R$string.quick_settings_wifi_label);
            } else {
                signalState.icon = QSTileImpl.ResourceIcon.get(OpWifiIcons.OP_QS_WIFI_NO_NETWORK);
                signalState.label = resources.getString(C0015R$string.quick_settings_wifi_label);
            }
        } else if (z6) {
            signalState.icon = QSTileImpl.ResourceIcon.get(17302838);
            signalState.label = resources.getString(C0015R$string.quick_settings_wifi_label);
        } else if (!signalState.value) {
            signalState.slash.isSlashed = true;
            signalState.state = 1;
            signalState.icon = QSTileImpl.ResourceIcon.get(WifiIcons.QS_WIFI_DISABLED);
            signalState.label = resources.getString(C0015R$string.quick_settings_wifi_label);
        } else if (z2) {
            signalState.icon = QSTileImpl.ResourceIcon.get(callbackInfo.wifiSignalIconId);
            signalState.label = removeDoubleQuotes(callbackInfo.ssid);
        } else if (z3) {
            signalState.icon = QSTileImpl.ResourceIcon.get(C0006R$drawable.op_q_ic_qs_wifi_disconnected);
            signalState.label = resources.getString(C0015R$string.quick_settings_wifi_label);
        } else {
            signalState.icon = QSTileImpl.ResourceIcon.get(WifiIcons.QS_WIFI_NO_NETWORK);
            signalState.label = resources.getString(C0015R$string.quick_settings_wifi_label);
        }
        if (Build.DEBUG_ONEPLUS) {
            String str3 = this.TAG;
            Log.d(str3, "handleUpdateState: state=" + signalState.state + ", label=" + ((Object) signalState.label) + ", secondaryLabel=" + ((Object) signalState.secondaryLabel) + ", actIn=" + signalState.activityIn + ", actOut=" + signalState.activityOut + ", iconRes=" + signalState.icon);
        }
        stringBuffer.append(this.mContext.getString(C0015R$string.quick_settings_wifi_label));
        stringBuffer.append(",");
        if (signalState.value && z2) {
            stringBuffer2.append(callbackInfo.wifiSignalContentDescription);
            stringBuffer.append(removeDoubleQuotes(callbackInfo.ssid));
            if (!TextUtils.isEmpty(signalState.secondaryLabel)) {
                stringBuffer.append(",");
                stringBuffer.append(signalState.secondaryLabel);
            }
        }
        signalState.stateDescription = stringBuffer2.toString();
        signalState.contentDescription = stringBuffer.toString();
        signalState.dualLabelContentDescription = resources.getString(C0015R$string.accessibility_quick_settings_open_settings, getTileLabel());
        signalState.expandedAccessibilityClassName = Switch.class.getName();
    }

    private CharSequence getSecondaryLabel(boolean z, String str) {
        return z ? this.mContext.getString(C0015R$string.quick_settings_wifi_secondary_label_transient) : str;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean shouldAnnouncementBeDelayed() {
        return this.mStateBeforeClick.value == ((QSTile.SignalState) this.mState).value;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.SignalState) this.mState).value) {
            return this.mContext.getString(C0015R$string.accessibility_quick_settings_wifi_changed_on);
        }
        return this.mContext.getString(C0015R$string.accessibility_quick_settings_wifi_changed_off);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl, com.android.systemui.plugins.qs.QSTile
    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi");
    }

    private static String removeDoubleQuotes(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length <= 1 || str.charAt(0) != '\"') {
            return str;
        }
        int i = length - 1;
        return str.charAt(i) == '\"' ? str.substring(1, i) : str;
    }

    /* access modifiers changed from: protected */
    public static final class CallbackInfo {
        boolean activityIn;
        boolean activityOut;
        boolean connected;
        boolean enabled;
        boolean isTransient;
        String ssid;
        public String statusLabel;
        String wifiSignalContentDescription;
        int wifiSignalIconId;

        protected CallbackInfo() {
        }

        public String toString() {
            return "CallbackInfo[enabled=" + this.enabled + ",connected=" + this.connected + ",wifiSignalIconId=" + this.wifiSignalIconId + ",ssid=" + this.ssid + ",activityIn=" + this.activityIn + ",activityOut=" + this.activityOut + ",wifiSignalContentDescription=" + this.wifiSignalContentDescription + ",isTransient=" + this.isTransient + ']';
        }
    }

    /* access modifiers changed from: protected */
    public final class WifiSignalCallback implements NetworkController.SignalCallback {
        final CallbackInfo mInfo = new CallbackInfo();

        protected WifiSignalCallback() {
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
            if (QSTileImpl.DEBUG) {
                String str3 = ((QSTileImpl) WifiTile.this).TAG;
                Log.d(str3, "onWifiSignalChanged: enabled=" + z + ", isTrans=" + z4 + ", conn=" + iconState2.visible + ", actIn=" + z2 + ", actOut=" + z3 + ", label=" + str2);
            }
            CallbackInfo callbackInfo = this.mInfo;
            callbackInfo.enabled = z;
            callbackInfo.connected = iconState2.visible;
            callbackInfo.wifiSignalIconId = iconState2.icon;
            callbackInfo.ssid = str;
            callbackInfo.activityIn = z2;
            callbackInfo.activityOut = z3;
            callbackInfo.wifiSignalContentDescription = iconState2.contentDescription;
            callbackInfo.isTransient = z4;
            callbackInfo.statusLabel = str2;
            if (WifiTile.this.isShowingDetail()) {
                WifiTile.this.mDetailAdapter.updateItems();
            }
            WifiTile.this.mTransientEnabling = false;
            WifiTile.this.refreshState();
        }
    }

    /* access modifiers changed from: protected */
    public class WifiDetailAdapter implements DetailAdapter, NetworkController.AccessPointController.AccessPointCallback, QSDetailItems.Callback {
        private AccessPoint[] mAccessPoints;
        private QSDetailItems mItems;

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getMetricsCategory() {
            return 152;
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemDisconnect(QSDetailItems.Item item) {
        }

        protected WifiDetailAdapter() {
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public CharSequence getTitle() {
            return ((QSTileImpl) WifiTile.this).mContext.getString(C0015R$string.quick_settings_wifi_label);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Intent getSettingsIntent() {
            return WifiTile.WIFI_SETTINGS;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.SignalState) ((QSTileImpl) WifiTile.this).mState).value);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public void setToggleState(boolean z) {
            if (QSTileImpl.DEBUG) {
                String str = ((QSTileImpl) WifiTile.this).TAG;
                Log.d(str, "setToggleState " + z);
            }
            MetricsLogger.action(((QSTileImpl) WifiTile.this).mContext, 153, z);
            WifiTile.this.mController.setWifiEnabled(z);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            if (QSTileImpl.DEBUG) {
                String str = ((QSTileImpl) WifiTile.this).TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("createDetailView convertView=");
                sb.append(view != null);
                Log.d(str, sb.toString());
            }
            this.mAccessPoints = null;
            QSDetailItems convertOrInflate = QSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems = convertOrInflate;
            convertOrInflate.setTagSuffix("Wifi");
            this.mItems.setCallback(this);
            WifiTile.this.mWifiController.scanForAccessPoints();
            setItemsVisible(((QSTile.SignalState) ((QSTileImpl) WifiTile.this).mState).value);
            return this.mItems;
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController.AccessPointCallback
        public void onAccessPointsChanged(List<AccessPoint> list) {
            this.mAccessPoints = (AccessPoint[]) list.toArray(new AccessPoint[list.size()]);
            filterUnreachableAPs();
            updateItems();
        }

        private void filterUnreachableAPs() {
            int i = 0;
            for (AccessPoint accessPoint : this.mAccessPoints) {
                if (accessPoint.isReachable()) {
                    i++;
                }
            }
            AccessPoint[] accessPointArr = this.mAccessPoints;
            if (i != accessPointArr.length) {
                this.mAccessPoints = new AccessPoint[i];
                int i2 = 0;
                for (AccessPoint accessPoint2 : accessPointArr) {
                    if (accessPoint2.isReachable()) {
                        this.mAccessPoints[i2] = accessPoint2;
                        i2++;
                    }
                }
            }
        }

        @Override // com.android.systemui.statusbar.policy.NetworkController.AccessPointController.AccessPointCallback
        public void onSettingsActivityTriggered(Intent intent) {
            WifiTile.this.mActivityStarter.postStartActivityDismissingKeyguard(intent, 0);
        }

        @Override // com.android.systemui.qs.QSDetailItems.Callback
        public void onDetailItemClick(QSDetailItems.Item item) {
            Object obj;
            if (item != null && (obj = item.tag) != null) {
                AccessPoint accessPoint = (AccessPoint) obj;
                if (!accessPoint.isActive() && WifiTile.this.mWifiController.connect(accessPoint)) {
                    ((QSTileImpl) WifiTile.this).mHost.collapsePanels();
                }
                WifiTile.this.showDetail(false);
            }
        }

        public void setItemsVisible(boolean z) {
            QSDetailItems qSDetailItems = this.mItems;
            if (qSDetailItems != null) {
                qSDetailItems.setItemsVisible(z);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        /* JADX WARNING: Removed duplicated region for block: B:13:0x002c  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x004b  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void updateItems() {
            /*
                r6 = this;
                com.android.systemui.qs.QSDetailItems r0 = r6.mItems
                if (r0 != 0) goto L_0x0005
                return
            L_0x0005:
                com.android.settingslib.wifi.AccessPoint[] r0 = r6.mAccessPoints
                r1 = 0
                if (r0 == 0) goto L_0x000d
                int r0 = r0.length
                if (r0 > 0) goto L_0x0017
            L_0x000d:
                com.android.systemui.qs.tiles.WifiTile r0 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.qs.tiles.WifiTile$WifiSignalCallback r2 = r0.mSignalCallback
                com.android.systemui.qs.tiles.WifiTile$CallbackInfo r2 = r2.mInfo
                boolean r2 = r2.enabled
                if (r2 != 0) goto L_0x001d
            L_0x0017:
                com.android.systemui.qs.tiles.WifiTile r0 = com.android.systemui.qs.tiles.WifiTile.this
                r0.fireScanStateChanged(r1)
                goto L_0x0021
            L_0x001d:
                r2 = 1
                r0.fireScanStateChanged(r2)
            L_0x0021:
                com.android.systemui.qs.tiles.WifiTile r0 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.qs.tiles.WifiTile$WifiSignalCallback r0 = r0.mSignalCallback
                com.android.systemui.qs.tiles.WifiTile$CallbackInfo r0 = r0.mInfo
                boolean r0 = r0.enabled
                r2 = 0
                if (r0 != 0) goto L_0x004b
                boolean r0 = com.oneplus.util.OpUtils.isUSS()
                if (r0 == 0) goto L_0x003c
                com.android.systemui.qs.QSDetailItems r0 = r6.mItems
                int r1 = com.oneplus.systemui.statusbar.policy.OpWifiIcons.OP_QS_WIFI_NO_NETWORK
                int r3 = com.android.systemui.C0015R$string.wifi_is_off
                r0.setEmptyState(r1, r3)
                goto L_0x0045
            L_0x003c:
                com.android.systemui.qs.QSDetailItems r0 = r6.mItems
                int r1 = com.android.systemui.statusbar.policy.WifiIcons.QS_WIFI_NO_NETWORK
                int r3 = com.android.systemui.C0015R$string.wifi_is_off
                r0.setEmptyState(r1, r3)
            L_0x0045:
                com.android.systemui.qs.QSDetailItems r6 = r6.mItems
                r6.setItems(r2)
                return
            L_0x004b:
                boolean r0 = com.oneplus.util.OpUtils.isUSS()
                if (r0 == 0) goto L_0x005b
                com.android.systemui.qs.QSDetailItems r0 = r6.mItems
                int r3 = com.oneplus.systemui.statusbar.policy.OpWifiIcons.OP_QS_WIFI_NO_NETWORK
                int r4 = com.android.systemui.C0015R$string.quick_settings_wifi_detail_empty_text
                r0.setEmptyState(r3, r4)
                goto L_0x0064
            L_0x005b:
                com.android.systemui.qs.QSDetailItems r0 = r6.mItems
                int r3 = com.android.systemui.statusbar.policy.WifiIcons.QS_WIFI_NO_NETWORK
                int r4 = com.android.systemui.C0015R$string.quick_settings_wifi_detail_empty_text
                r0.setEmptyState(r3, r4)
            L_0x0064:
                com.android.settingslib.wifi.AccessPoint[] r0 = r6.mAccessPoints
                if (r0 == 0) goto L_0x00ab
                int r0 = r0.length
                com.android.systemui.qs.QSDetailItems$Item[] r0 = new com.android.systemui.qs.QSDetailItems.Item[r0]
            L_0x006b:
                com.android.settingslib.wifi.AccessPoint[] r3 = r6.mAccessPoints
                int r4 = r3.length
                if (r1 >= r4) goto L_0x00aa
                r3 = r3[r1]
                com.android.systemui.qs.QSDetailItems$Item r4 = new com.android.systemui.qs.QSDetailItems$Item
                r4.<init>()
                r4.tag = r3
                com.android.systemui.qs.tiles.WifiTile r5 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.statusbar.policy.NetworkController$AccessPointController r5 = com.android.systemui.qs.tiles.WifiTile.access$1400(r5)
                int r5 = r5.getIcon(r3)
                r4.iconResId = r5
                java.lang.CharSequence r5 = r3.getSsid()
                r4.line1 = r5
                boolean r5 = r3.isActive()
                if (r5 == 0) goto L_0x0096
                java.lang.String r5 = r3.getSummary()
                goto L_0x0097
            L_0x0096:
                r5 = r2
            L_0x0097:
                r4.line2 = r5
                int r3 = r3.getSecurity()
                if (r3 == 0) goto L_0x00a2
                int r3 = com.android.systemui.C0006R$drawable.qs_ic_wifi_lock
                goto L_0x00a3
            L_0x00a2:
                r3 = -1
            L_0x00a3:
                r4.icon2 = r3
                r0[r1] = r4
                int r1 = r1 + 1
                goto L_0x006b
            L_0x00aa:
                r2 = r0
            L_0x00ab:
                com.android.systemui.qs.QSDetailItems r6 = r6.mItems
                r6.setItems(r2)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.tiles.WifiTile.WifiDetailAdapter.updateItems():void");
        }
    }
}
