package com.android.systemui.qs;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyEventLogger;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserManager;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.SecurityController;
import com.oneplus.systemui.qs.OpQSWidgetAdapter;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
public class QSSecurityFooter implements View.OnClickListener, DialogInterface.OnClickListener {
    private final ActivityStarter mActivityStarter;
    private final Callback mCallback = new Callback();
    private final Context mContext;
    private AlertDialog mDialog;
    private final ImageView mFooterIcon;
    private int mFooterIconId;
    private final TextView mFooterText;
    private CharSequence mFooterTextContent = null;
    protected H mHandler;
    private QSTileHost mHost;
    private boolean mIsExpanding = false;
    private boolean mIsVisible;
    private final Handler mMainHandler;
    private boolean mNeedToRefresh = false;
    private final View mRootView;
    private final SecurityController mSecurityController;
    private final UserManager mUm;
    private final Runnable mUpdateDisplayState = new Runnable() { // from class: com.android.systemui.qs.QSSecurityFooter.2
        @Override // java.lang.Runnable
        public void run() {
            if (QSSecurityFooter.this.mIsExpanding) {
                Log.d("QSSecurityFooter", "update during panel expanding, skip.");
                QSSecurityFooter.this.mNeedToRefresh = true;
                return;
            }
            if (QSSecurityFooter.this.mFooterTextContent != null) {
                QSSecurityFooter.this.mFooterText.setText(QSSecurityFooter.this.mFooterTextContent);
            }
            QSSecurityFooter.this.mRootView.setVisibility(!QSSecurityFooter.this.mIsVisible ? 8 : 0);
        }
    };
    private final Runnable mUpdateIcon = new Runnable() { // from class: com.android.systemui.qs.QSSecurityFooter.1
        @Override // java.lang.Runnable
        public void run() {
            QSSecurityFooter.this.mFooterIcon.setImageResource(QSSecurityFooter.this.mFooterIconId);
        }
    };

    public void onConfigurationChanged() {
    }

    public void setOpQSWidgetAdapter(OpQSWidgetAdapter opQSWidgetAdapter) {
    }

    static {
        Log.isLoggable("QSSecurityFooter", 3);
    }

    public QSSecurityFooter(QSPanel qSPanel, Context context) {
        View inflate = LayoutInflater.from(context).inflate(C0011R$layout.quick_settings_footer, (ViewGroup) qSPanel, false);
        this.mRootView = inflate;
        inflate.setOnClickListener(this);
        this.mRootView.setVisibility(8);
        this.mFooterText = (TextView) this.mRootView.findViewById(C0008R$id.footer_text);
        this.mFooterIcon = (ImageView) this.mRootView.findViewById(C0008R$id.footer_icon);
        this.mFooterIconId = C0006R$drawable.ic_info_outline;
        this.mContext = context;
        this.mMainHandler = new Handler(Looper.myLooper());
        this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        this.mSecurityController = (SecurityController) Dependency.get(SecurityController.class);
        this.mHandler = new H((Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mUm = (UserManager) this.mContext.getSystemService("user");
    }

    public void setHostEnvironment(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
    }

    public void setListening(boolean z) {
        if (z) {
            this.mSecurityController.addCallback(this.mCallback);
            refreshState();
            return;
        }
        this.mSecurityController.removeCallback(this.mCallback);
    }

    public View getView() {
        return this.mRootView;
    }

    public boolean hasFooter() {
        return this.mRootView.getVisibility() != 8;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (hasFooter()) {
            this.mHandler.sendEmptyMessage(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleClick() {
        showDeviceMonitoringDialog();
        DevicePolicyEventLogger.createEvent(57).write();
    }

    public void showDeviceMonitoringDialog() {
        this.mHost.collapsePanels();
        createDialog();
    }

    public void refreshState() {
        this.mHandler.sendEmptyMessage(1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRefreshState() {
        boolean isDeviceManaged = this.mSecurityController.isDeviceManaged();
        UserInfo userInfo = this.mUm.getUserInfo(ActivityManager.getCurrentUser());
        boolean z = true;
        boolean z2 = UserManager.isDeviceInDemoMode(this.mContext) && userInfo != null && userInfo.isDemo();
        boolean hasWorkProfile = this.mSecurityController.hasWorkProfile();
        boolean hasCACertInCurrentUser = this.mSecurityController.hasCACertInCurrentUser();
        boolean hasCACertInWorkProfile = this.mSecurityController.hasCACertInWorkProfile();
        boolean isNetworkLoggingEnabled = this.mSecurityController.isNetworkLoggingEnabled();
        String primaryVpnName = this.mSecurityController.getPrimaryVpnName();
        String workProfileVpnName = this.mSecurityController.getWorkProfileVpnName();
        CharSequence deviceOwnerOrganizationName = this.mSecurityController.getDeviceOwnerOrganizationName();
        CharSequence workProfileOrganizationName = this.mSecurityController.getWorkProfileOrganizationName();
        boolean isProfileOwnerOfOrganizationOwnedDevice = this.mSecurityController.isProfileOwnerOfOrganizationOwnedDevice();
        if ((!isDeviceManaged || z2) && !hasCACertInCurrentUser && !hasCACertInWorkProfile && primaryVpnName == null && workProfileVpnName == null && !isProfileOwnerOfOrganizationOwnedDevice) {
            z = false;
        }
        this.mIsVisible = z;
        if (this.mContext.getResources().getConfiguration().orientation == 2) {
            this.mIsVisible = false;
        }
        this.mFooterTextContent = getFooterText(isDeviceManaged, hasWorkProfile, hasCACertInCurrentUser, hasCACertInWorkProfile, isNetworkLoggingEnabled, primaryVpnName, workProfileVpnName, deviceOwnerOrganizationName, workProfileOrganizationName, isProfileOwnerOfOrganizationOwnedDevice);
        int i = C0006R$drawable.ic_info_outline;
        if (!(primaryVpnName == null && workProfileVpnName == null)) {
            if (this.mSecurityController.isVpnBranded()) {
                i = C0006R$drawable.stat_sys_branded_vpn;
            } else {
                i = C0006R$drawable.op_ic_qs_vpn;
            }
        }
        if (this.mFooterIconId != i) {
            this.mFooterIconId = i;
            this.mMainHandler.post(this.mUpdateIcon);
        }
        this.mMainHandler.removeCallbacks(this.mUpdateDisplayState);
        this.mMainHandler.postDelayed(this.mUpdateDisplayState, 500);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00cb: APUT  (r4v4 java.lang.Object[]), (0 ??[int, short, byte, char]), (r8v3 java.lang.String) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00df: APUT  (r4v3 java.lang.Object[]), (1 ??[boolean, int, float, short, byte, char]), (r8v1 java.lang.String) */
    /* access modifiers changed from: protected */
    public CharSequence getFooterText(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, String str, String str2, CharSequence charSequence, CharSequence charSequence2, boolean z6) {
        if (!z) {
            if (z4) {
                if (charSequence2 == null) {
                    return this.mContext.getString(C0015R$string.quick_settings_disclosure_managed_profile_monitoring);
                }
                return this.mContext.getString(C0015R$string.quick_settings_disclosure_named_managed_profile_monitoring, charSequence2);
            } else if (z3) {
                return this.mContext.getString(C0015R$string.quick_settings_disclosure_monitoring);
            } else {
                if (str != null && str2 != null) {
                    return this.mContext.getString(C0015R$string.quick_settings_disclosure_vpns);
                }
                if (str2 != null) {
                    return this.mContext.getString(C0015R$string.quick_settings_disclosure_managed_profile_named_vpn, str2);
                }
                if (str != null) {
                    if (z2) {
                        return this.mContext.getString(C0015R$string.quick_settings_disclosure_personal_profile_named_vpn, str);
                    }
                    return this.mContext.getString(C0015R$string.quick_settings_disclosure_named_vpn, str);
                } else if (!z6) {
                    return null;
                } else {
                    if (charSequence2 == null) {
                        return this.mContext.getString(C0015R$string.quick_settings_disclosure_management);
                    }
                    return this.mContext.getString(C0015R$string.quick_settings_disclosure_named_management, charSequence2);
                }
            }
        } else if (z3 || z4 || z5) {
            if (charSequence == null) {
                return this.mContext.getString(C0015R$string.quick_settings_disclosure_management_monitoring);
            }
            return this.mContext.getString(C0015R$string.quick_settings_disclosure_named_management_monitoring, charSequence);
        } else if (str == null || str2 == null) {
            if (str == null && str2 == null) {
                if (charSequence == null) {
                    return this.mContext.getString(C0015R$string.quick_settings_disclosure_management);
                }
                return this.mContext.getString(C0015R$string.quick_settings_disclosure_named_management, charSequence);
            } else if (charSequence == null) {
                Context context = this.mContext;
                int i = C0015R$string.quick_settings_disclosure_management_named_vpn;
                Object[] objArr = new Object[1];
                if (str == null) {
                    str = str2;
                }
                objArr[0] = str;
                return context.getString(i, objArr);
            } else {
                Context context2 = this.mContext;
                int i2 = C0015R$string.quick_settings_disclosure_named_management_named_vpn;
                Object[] objArr2 = new Object[2];
                objArr2[0] = charSequence;
                if (str == null) {
                    str = str2;
                }
                objArr2[1] = str;
                return context2.getString(i2, objArr2);
            }
        } else if (charSequence == null) {
            return this.mContext.getString(C0015R$string.quick_settings_disclosure_management_vpns);
        } else {
            return this.mContext.getString(C0015R$string.quick_settings_disclosure_named_management_vpns, charSequence);
        }
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -2) {
            Intent intent = new Intent("android.settings.ENTERPRISE_PRIVACY_SETTINGS");
            this.mDialog.dismiss();
            this.mActivityStarter.postStartActivityDismissingKeyguard(intent, 0);
        }
    }

    private void createDialog() {
        boolean isDeviceManaged = this.mSecurityController.isDeviceManaged();
        boolean isProfileOwnerOfOrganizationOwnedDevice = this.mSecurityController.isProfileOwnerOfOrganizationOwnedDevice();
        boolean hasWorkProfile = this.mSecurityController.hasWorkProfile();
        CharSequence deviceOwnerOrganizationName = this.mSecurityController.getDeviceOwnerOrganizationName();
        CharSequence workProfileOrganizationName = this.mSecurityController.getWorkProfileOrganizationName();
        boolean hasCACertInCurrentUser = this.mSecurityController.hasCACertInCurrentUser();
        boolean hasCACertInWorkProfile = this.mSecurityController.hasCACertInWorkProfile();
        boolean isNetworkLoggingEnabled = this.mSecurityController.isNetworkLoggingEnabled();
        String primaryVpnName = this.mSecurityController.getPrimaryVpnName();
        String workProfileVpnName = this.mSecurityController.getWorkProfileVpnName();
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        this.mDialog = systemUIDialog;
        systemUIDialog.requestWindowFeature(1);
        boolean z = false;
        View inflate = LayoutInflater.from(new ContextThemeWrapper(this.mContext, ThemeColorUtils.getCurrentTheme() == 1 ? C0016R$style.oneplus_theme_dialog_dark : C0016R$style.oneplus_theme_dialog_light)).inflate(C0011R$layout.quick_settings_footer_dialog, (ViewGroup) null, false);
        this.mDialog.setView(inflate);
        this.mDialog.setButton(-1, getPositiveButton(), this);
        CharSequence managementMessage = getManagementMessage(isDeviceManaged, deviceOwnerOrganizationName, isProfileOwnerOfOrganizationOwnedDevice, workProfileOrganizationName);
        if (managementMessage == null) {
            inflate.findViewById(C0008R$id.device_management_disclosures).setVisibility(8);
        } else {
            inflate.findViewById(C0008R$id.device_management_disclosures).setVisibility(0);
            ((TextView) inflate.findViewById(C0008R$id.device_management_warning)).setText(managementMessage);
            if (!isProfileOwnerOfOrganizationOwnedDevice) {
                this.mDialog.setButton(-2, getSettingsButton(), this);
            }
        }
        CharSequence caCertsMessage = getCaCertsMessage(isDeviceManaged, hasCACertInCurrentUser, hasCACertInWorkProfile);
        if (caCertsMessage == null) {
            inflate.findViewById(C0008R$id.ca_certs_disclosures).setVisibility(8);
        } else {
            inflate.findViewById(C0008R$id.ca_certs_disclosures).setVisibility(0);
            TextView textView = (TextView) inflate.findViewById(C0008R$id.ca_certs_warning);
            textView.setText(caCertsMessage);
            textView.setMovementMethod(new LinkMovementMethod());
        }
        CharSequence networkLoggingMessage = getNetworkLoggingMessage(isNetworkLoggingEnabled);
        if (networkLoggingMessage == null) {
            inflate.findViewById(C0008R$id.network_logging_disclosures).setVisibility(8);
        } else {
            inflate.findViewById(C0008R$id.network_logging_disclosures).setVisibility(0);
            ((TextView) inflate.findViewById(C0008R$id.network_logging_warning)).setText(networkLoggingMessage);
        }
        CharSequence vpnMessage = getVpnMessage(isDeviceManaged, hasWorkProfile, primaryVpnName, workProfileVpnName);
        if (vpnMessage == null) {
            inflate.findViewById(C0008R$id.vpn_disclosures).setVisibility(8);
        } else {
            inflate.findViewById(C0008R$id.vpn_disclosures).setVisibility(0);
            TextView textView2 = (TextView) inflate.findViewById(C0008R$id.vpn_warning);
            textView2.setText(vpnMessage);
            textView2.setMovementMethod(new LinkMovementMethod());
        }
        boolean z2 = managementMessage != null;
        boolean z3 = caCertsMessage != null;
        boolean z4 = networkLoggingMessage != null;
        if (vpnMessage != null) {
            z = true;
        }
        configSubtitleVisibility(z2, z3, z4, z, inflate);
        this.mDialog.show();
        this.mDialog.getWindow().setLayout(-1, -2);
    }

    /* access modifiers changed from: protected */
    public void configSubtitleVisibility(boolean z, boolean z2, boolean z3, boolean z4, View view) {
        if (!z) {
            int i = 0;
            if (z2) {
                i = 1;
            }
            if (z3) {
                i++;
            }
            if (z4) {
                i++;
            }
            if (i == 1) {
                if (z2) {
                    view.findViewById(C0008R$id.ca_certs_subtitle).setVisibility(8);
                }
                if (z3) {
                    view.findViewById(C0008R$id.network_logging_subtitle).setVisibility(8);
                }
                if (z4) {
                    view.findViewById(C0008R$id.vpn_subtitle).setVisibility(8);
                }
            }
        }
    }

    private String getSettingsButton() {
        return this.mContext.getString(C0015R$string.monitoring_button_view_policies);
    }

    private String getPositiveButton() {
        return this.mContext.getString(C0015R$string.ok);
    }

    /* access modifiers changed from: protected */
    public CharSequence getManagementMessage(boolean z, CharSequence charSequence, boolean z2, CharSequence charSequence2) {
        if (!z && !z2) {
            return null;
        }
        if (z && charSequence != null) {
            return this.mContext.getString(C0015R$string.monitoring_description_named_management, charSequence);
        }
        if (!z2 || charSequence2 == null) {
            return this.mContext.getString(C0015R$string.monitoring_description_management);
        }
        return this.mContext.getString(C0015R$string.monitoring_description_named_management, charSequence2);
    }

    /* access modifiers changed from: protected */
    public CharSequence getCaCertsMessage(boolean z, boolean z2, boolean z3) {
        if (!z2 && !z3) {
            return null;
        }
        if (z) {
            return this.mContext.getString(C0015R$string.monitoring_description_management_ca_certificate);
        }
        if (z3) {
            return this.mContext.getString(C0015R$string.monitoring_description_managed_profile_ca_certificate);
        }
        return this.mContext.getString(C0015R$string.monitoring_description_ca_certificate);
    }

    /* access modifiers changed from: protected */
    public CharSequence getNetworkLoggingMessage(boolean z) {
        if (!z) {
            return null;
        }
        return this.mContext.getString(C0015R$string.monitoring_description_management_network_logging);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0030: APUT  (r1v2 java.lang.Object[]), (0 ??[int, short, byte, char]), (r7v2 java.lang.String) */
    /* access modifiers changed from: protected */
    public CharSequence getVpnMessage(boolean z, boolean z2, String str, String str2) {
        if (str == null && str2 == null) {
            return null;
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if (z) {
            if (str == null || str2 == null) {
                Context context = this.mContext;
                int i = C0015R$string.monitoring_description_named_vpn;
                Object[] objArr = new Object[1];
                if (str == null) {
                    str = str2;
                }
                objArr[0] = str;
                spannableStringBuilder.append((CharSequence) context.getString(i, objArr));
            } else {
                spannableStringBuilder.append((CharSequence) this.mContext.getString(C0015R$string.monitoring_description_two_named_vpns, str, str2));
            }
        } else if (str != null && str2 != null) {
            spannableStringBuilder.append((CharSequence) this.mContext.getString(C0015R$string.monitoring_description_two_named_vpns, str, str2));
        } else if (str2 != null) {
            spannableStringBuilder.append((CharSequence) this.mContext.getString(C0015R$string.monitoring_description_managed_profile_named_vpn, str2));
        } else if (z2) {
            spannableStringBuilder.append((CharSequence) this.mContext.getString(C0015R$string.monitoring_description_personal_profile_named_vpn, str));
        } else {
            spannableStringBuilder.append((CharSequence) this.mContext.getString(C0015R$string.monitoring_description_named_vpn, str));
        }
        spannableStringBuilder.append((CharSequence) this.mContext.getString(C0015R$string.monitoring_description_vpn_settings_separator));
        spannableStringBuilder.append(this.mContext.getString(C0015R$string.monitoring_description_vpn_settings), new VpnSpan(), 0);
        return spannableStringBuilder;
    }

    /* access modifiers changed from: private */
    public class Callback implements SecurityController.SecurityControllerCallback {
        private Callback() {
        }

        @Override // com.android.systemui.statusbar.policy.SecurityController.SecurityControllerCallback
        public void onStateChanged() {
            QSSecurityFooter.this.refreshState();
        }
    }

    /* access modifiers changed from: private */
    public class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            try {
                if (message.what == 1) {
                    QSSecurityFooter.this.handleRefreshState();
                } else if (message.what == 0) {
                    QSSecurityFooter.this.handleClick();
                }
            } catch (Throwable th) {
                String str = "Error in " + ((String) null);
                Log.w("QSSecurityFooter", str, th);
                QSSecurityFooter.this.mHost.warn(str, th);
            }
        }
    }

    /* access modifiers changed from: protected */
    public class VpnSpan extends ClickableSpan {
        @Override // java.lang.Object
        public int hashCode() {
            return 314159257;
        }

        protected VpnSpan() {
        }

        @Override // android.text.style.ClickableSpan
        public void onClick(View view) {
            Intent intent = new Intent("android.settings.VPN_SETTINGS");
            QSSecurityFooter.this.mDialog.dismiss();
            QSSecurityFooter.this.mActivityStarter.postStartActivityDismissingKeyguard(intent, 0);
        }

        @Override // java.lang.Object
        public boolean equals(Object obj) {
            return obj instanceof VpnSpan;
        }
    }

    public void updateThemeColor() {
        int color = ThemeColorUtils.getColor(9);
        int color2 = ThemeColorUtils.getColor(1);
        int color3 = ThemeColorUtils.getColor(10);
        this.mRootView.setBackgroundColor(color);
        if (OpUtils.isREDVersion()) {
            this.mRootView.setBackgroundResource(C0006R$drawable.op_qs_red_middle);
        }
        this.mFooterText.setTextColor(color2);
        this.mFooterIcon.setColorFilter(color3);
    }

    public void setIsExpanding(boolean z) {
        if (this.mIsExpanding != z) {
            this.mIsExpanding = z;
            if (!z && this.mNeedToRefresh) {
                Log.d("QSSecurityFooter", "expand done, update vis");
                this.mNeedToRefresh = false;
                this.mMainHandler.removeCallbacks(this.mUpdateDisplayState);
                this.mMainHandler.post(this.mUpdateDisplayState);
            }
        }
    }
}
