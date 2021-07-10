package com.oneplus.systemui.biometrics;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.systemui.C0001R$array;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.systemui.biometrics.OpQLAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class OpQLHelper {
    final ArrayList<OpQLAdapter.ActionInfo> mAppMap = new ArrayList<>();
    private Context mContext;
    private LauncherApps mLauncherApps;
    private PackageManager mPackageManager = null;
    private ArrayList<OpQLAdapter.OPQuickPayConfig> mPaymentApps = new ArrayList<>();
    private ArrayList<String> mPaymentAppsName;
    private ArrayList<String> mWxMiniProgramAppsName;

    public OpQLHelper(Context context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mWxMiniProgramAppsName = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(C0001R$array.op_wx_mini_program_strings)));
        this.mPaymentAppsName = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(C0001R$array.zzz_op_quick_pay_switch_strings)));
        loadPaymentApps();
    }

    public ArrayList<OpQLAdapter.ActionInfo> getQLApps() {
        return this.mAppMap;
    }

    public void parseQLConfig(String str) {
        String[] split;
        if (Build.DEBUG_ONEPLUS) {
            Log.d("QuickLaunch.QLHelper", "parseQLConfig config " + str);
        }
        if (str != null) {
            for (String str2 : str.split(",")) {
                OpQLAdapter.ActionInfo parseSettingData = parseSettingData(str2);
                if (parseSettingData != null) {
                    this.mAppMap.add(0, parseSettingData);
                }
            }
        }
    }

    private OpQLAdapter.ActionInfo parseSettingData(String str) {
        String str2;
        if (str == null) {
            return null;
        }
        OpQLAdapter.ActionInfo actionInfo = new OpQLAdapter.ActionInfo();
        int indexOf = str.indexOf(":");
        if (indexOf < 0) {
            actionInfo.setActionName(str);
        } else {
            String[] strArr = new String[4];
            strArr[0] = str.substring(0, indexOf);
            String[] split = str.substring(indexOf + 1).split(";", 3);
            System.arraycopy(split, 0, strArr, 1, split.length);
            actionInfo.setActionName(strArr[0]);
            actionInfo.setPackage(strArr[1]);
            if (!isPackageAvailable(actionInfo.mPackageName)) {
                return null;
            }
            if ("OpenApp".equals(strArr[0])) {
                actionInfo.setUid(strArr[2]);
                actionInfo.mAppIcon = getApplicationIcon(actionInfo.mPackageName, actionInfo.mUid);
                actionInfo.mLabel = getAppLabel(actionInfo.mPackageName);
            } else if ("OpenShortcut".equals(strArr[0])) {
                actionInfo.setShortcutId(strArr[2]);
                actionInfo.setUid(strArr[3]);
                List<ShortcutInfo> loadShortCuts = loadShortCuts(actionInfo.mPackageName);
                for (int i = 0; i < loadShortCuts.size(); i++) {
                    ShortcutInfo shortcutInfo = loadShortCuts.get(i);
                    if (shortcutInfo.getId().equals(actionInfo.mShortcutId)) {
                        shortcutInfo.getIconResourceId();
                        Drawable shortcutIconDrawable = this.mLauncherApps.getShortcutIconDrawable(shortcutInfo, 0);
                        actionInfo.mAppIcon = shortcutIconDrawable;
                        if (shortcutIconDrawable == null) {
                            actionInfo.mAppIcon = getApplicationIcon(actionInfo.mPackageName, actionInfo.mUid);
                        }
                        CharSequence longLabel = shortcutInfo.getLongLabel();
                        if (TextUtils.isEmpty(longLabel)) {
                            longLabel = shortcutInfo.getShortLabel();
                        }
                        if (TextUtils.isEmpty(longLabel)) {
                            longLabel = shortcutInfo.getId();
                        }
                        actionInfo.mLabel = longLabel.toString();
                    }
                }
            } else if ("OpenQuickPay".equals(strArr[0])) {
                if (isPackageAvailable(actionInfo.mPackageName)) {
                    int parseInt = Integer.parseInt(strArr[2]);
                    actionInfo.mPaymentWhich = parseInt;
                    if (parseInt < this.mPaymentApps.size()) {
                        OpQLAdapter.OPQuickPayConfig oPQuickPayConfig = this.mPaymentApps.get(actionInfo.mPaymentWhich);
                        actionInfo.mQuickPayConfig = oPQuickPayConfig;
                        actionInfo.mAppIcon = oPQuickPayConfig.appIcon;
                    }
                    boolean equals = this.mContext.getResources().getConfiguration().locale.getLanguage().equals("zh");
                    int i2 = actionInfo.mPaymentWhich;
                    if (i2 == 4) {
                        str2 = this.mPaymentAppsName.get(i2);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(getAppLabel(actionInfo.mPackageName));
                        sb.append((equals || actionInfo.mPaymentWhich == 4) ? "" : " ");
                        sb.append(this.mPaymentAppsName.get(actionInfo.mPaymentWhich));
                        str2 = sb.toString();
                    }
                    actionInfo.mLabel = str2;
                }
            } else if ("OpenWxMiniProgram".equals(strArr[0])) {
                int parseInt2 = Integer.parseInt(strArr[2]);
                if (isPackageAvailable(actionInfo.mPackageName)) {
                    actionInfo.mWxMiniProgramWhich = parseInt2;
                    actionInfo.mAppIcon = getWxMiniProgramApplicationIcon(parseInt2);
                    actionInfo.mLabel = this.mWxMiniProgramAppsName.get(parseInt2);
                }
            }
        }
        return actionInfo;
    }

    private List<ShortcutInfo> loadShortCuts(String str) {
        if (this.mLauncherApps == null) {
            this.mLauncherApps = (LauncherApps) this.mContext.getSystemService("launcherapps");
        }
        if (this.mLauncherApps == null) {
            return null;
        }
        LauncherApps.ShortcutQuery shortcutQuery = new LauncherApps.ShortcutQuery();
        shortcutQuery.setQueryFlags(27);
        shortcutQuery.setPackage(str);
        return this.mLauncherApps.getShortcuts(shortcutQuery, Process.myUserHandle());
    }

    private boolean isPackageAvailable(String str) {
        try {
            ApplicationInfo applicationInfo = this.mPackageManager.getApplicationInfo(str, 0);
            if (applicationInfo != null && applicationInfo.enabled) {
                return true;
            }
        } catch (Exception e) {
            Log.w("QuickLaunch.QLHelper", "Exception e = " + e.toString());
        }
        Log.w("QuickLaunch.QLHelper", "QuickPay: " + str + " is not available.");
        return false;
    }

    private Drawable getApplicationIcon(String str, int i) {
        ApplicationInfo applicationInfo;
        Resources resources;
        int i2;
        if (i >= 0) {
            try {
                ApplicationInfo applicationInfo2 = this.mPackageManager.getApplicationInfo(str, 128);
                this.mContext.getResources();
                return this.mPackageManager.getUserBadgedIcon(this.mPackageManager.getApplicationIcon(applicationInfo2), UserHandle.getUserHandleForUid(i));
            } catch (PackageManager.NameNotFoundException unused) {
                Log.e("QuickLaunch.QLHelper", "Package [" + str + "] name not found");
                return null;
            }
        } else {
            try {
                applicationInfo = this.mPackageManager.getApplicationInfo(str, 0);
            } catch (Exception e) {
                Log.w("QuickLaunch.QLHelper", "Exception e = " + e.toString());
                applicationInfo = null;
            }
            try {
                resources = this.mPackageManager.getResourcesForApplication(str);
            } catch (PackageManager.NameNotFoundException unused2) {
                resources = null;
            }
            if (!(resources == null || applicationInfo == null || (i2 = applicationInfo.icon) == 0)) {
                try {
                    return getDrawable(resources, i2);
                } catch (Exception e2) {
                    Log.w("QuickLaunch.QLHelper", "Exception e = " + e2.toString());
                }
            }
            return null;
        }
    }

    private String getAppLabel(String str) {
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = this.mPackageManager.getApplicationInfo(str, 128);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("QuickLaunch.QLHelper", "Exception e = " + e.toString());
            applicationInfo = null;
        }
        return (String) (applicationInfo != null ? this.mPackageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    private void loadPaymentApps() {
        ArrayList arrayList = new ArrayList(Arrays.asList(this.mContext.getResources().getStringArray(84017211)));
        for (int i = 0; i < arrayList.size(); i++) {
            String[] split = ((String) arrayList.get(i)).split(";");
            if (split.length >= 4) {
                OpQLAdapter.OPQuickPayConfig oPQuickPayConfig = new OpQLAdapter.OPQuickPayConfig();
                oPQuickPayConfig.index = i;
                oPQuickPayConfig.packageName = split[0];
                oPQuickPayConfig.switchName = this.mPaymentAppsName.get(i);
                if (!split[1].equals("sdk")) {
                    if (split[1].contains("://")) {
                        oPQuickPayConfig.urlScheme = split[1];
                    } else {
                        oPQuickPayConfig.className = split[1];
                        oPQuickPayConfig.targetClassName = oPQuickPayConfig.packageName + "/" + oPQuickPayConfig.className;
                    }
                }
                if ("default".equals(split[2])) {
                    oPQuickPayConfig.isDefault = true;
                }
                if (!"class".equals(split[3])) {
                    oPQuickPayConfig.targetClassName = split[3];
                }
                Drawable paymentApplicationIcon = getPaymentApplicationIcon(i);
                if (paymentApplicationIcon != null) {
                    int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(84279509);
                    paymentApplicationIcon.setBounds(0, 0, dimensionPixelSize, dimensionPixelSize);
                    oPQuickPayConfig.appIcon = paymentApplicationIcon;
                } else {
                    oPQuickPayConfig.appIcon = getApplicationIcon(oPQuickPayConfig.packageName, -1);
                }
                this.mPaymentApps.add(oPQuickPayConfig);
            }
        }
    }

    public boolean startApp(String str, ActivityOptions activityOptions, int i) {
        Intent launchIntentForPackage = this.mPackageManager.getLaunchIntentForPackage(str);
        if (launchIntentForPackage != null) {
            this.mContext.startActivityAsUser(launchIntentForPackage, activityOptions.toBundle(), new UserHandle(UserHandle.getUserId(i)));
            return true;
        }
        Log.e("QuickLaunch.QLHelper", "start app " + str + " failed because intent is null");
        return false;
    }

    public void startQuickPay(int i, int i2) {
        boolean z;
        if (i == 2) {
            z = startShortcut("com.eg.android.AlipayGphone", "1002", i2, false);
        } else {
            z = i == 3 ? startShortcut("com.eg.android.AlipayGphone", "1001", i2, false) : false;
        }
        if (!z) {
            try {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("QuickLaunch.QLHelper", "QuickPay: startQuickPay which=" + i);
                }
                boolean isPackageAvailable = isPackageAvailable(this.mPaymentApps.get(i).packageName);
                if (!isPackageAvailable) {
                    int i3 = 0;
                    while (true) {
                        if (i3 >= this.mPaymentApps.size()) {
                            break;
                        }
                        if (i != i3) {
                            OpQLAdapter.OPQuickPayConfig oPQuickPayConfig = this.mPaymentApps.get(i3);
                            if (oPQuickPayConfig.isDefault && isPackageAvailable(oPQuickPayConfig.packageName)) {
                                if (Build.DEBUG_ONEPLUS) {
                                    Log.i("QuickLaunch.QLHelper", "QuickPay: startQuickPay new which=" + i3);
                                }
                                isPackageAvailable = true;
                                i = i3;
                            }
                        }
                        i3++;
                    }
                }
                if (isPackageAvailable) {
                    OpQLAdapter.OPQuickPayConfig oPQuickPayConfig2 = this.mPaymentApps.get(i);
                    if (oPQuickPayConfig2.className != null) {
                        Intent intent = new Intent();
                        Log.d("QuickLaunch.QLHelper", "next.packageName " + oPQuickPayConfig2.packageName + " next.className " + oPQuickPayConfig2.className);
                        intent.setClassName(oPQuickPayConfig2.packageName, oPQuickPayConfig2.className);
                        intent.addFlags(268468224);
                        this.mContext.startActivityAsUser(intent, UserHandle.SYSTEM);
                    } else if (oPQuickPayConfig2.urlScheme != null) {
                        Log.d("QuickLaunch.QLHelper", "next.urlScheme " + oPQuickPayConfig2.urlScheme);
                        Intent intent2 = new Intent("android.intent.action.VIEW", Uri.parse(oPQuickPayConfig2.urlScheme));
                        intent2.addFlags(335544320);
                        this.mContext.startActivityAsUser(intent2, UserHandle.SYSTEM);
                    }
                } else {
                    Toast.makeText(this.mContext, C0015R$string.zzz_op_quick_pay_no_install_message, 0).show();
                    Log.w("QuickLaunch.QLHelper", "QuickPay: startQuickPay have no installed app.");
                }
            } catch (Exception e) {
                Log.w("QuickLaunch.QLHelper", "QuickPay: startQuickPay failed " + e);
            }
        }
    }

    public void startWxMiniProgram(int i) {
        StatusBar statusBar = (StatusBar) Dependency.get(StatusBar.class);
        if (statusBar != null) {
            statusBar.toggleWxBus();
        }
    }

    public boolean startShortcut(String str, String str2, int i, boolean z) {
        if (this.mLauncherApps == null) {
            this.mLauncherApps = (LauncherApps) this.mContext.getSystemService("launcherapps");
        }
        LauncherApps launcherApps = this.mLauncherApps;
        if (launcherApps != null) {
            try {
                launcherApps.startShortcut(str, str2, null, null, new UserHandle(UserHandle.getUserId(i)));
                return true;
            } catch (ActivityNotFoundException | IllegalStateException | SecurityException unused) {
                Log.e("QuickLaunch.QLHelper", "start shortcut failed");
                return false;
            }
        } else {
            Log.e("QuickLaunch.QLHelper", "shortcut service is null");
            return false;
        }
    }

    private Drawable getPaymentApplicationIcon(int i) {
        if (i == 0) {
            return getDrawable(null, C0006R$drawable.ic_wechat_qrcode);
        }
        if (i == 1) {
            return getDrawable(null, C0006R$drawable.ic_wechat_scanning);
        }
        if (i == 2) {
            return getDrawable(null, C0006R$drawable.ic_alipay_qrcode);
        }
        if (i != 3) {
            return null;
        }
        return getDrawable(null, C0006R$drawable.ic_alipay_scanning);
    }

    private Drawable getWxMiniProgramApplicationIcon(int i) {
        if (i != 0) {
            return null;
        }
        return getDrawable(null, C0006R$drawable.ic_wechat_mini_program_bus);
    }

    private Drawable getDrawable(Resources resources, int i) {
        if (resources != null) {
            return resources.getDrawableForDensity(i, 640);
        }
        return this.mContext.getResources().getDrawableForDensity(i, 640);
    }

    public void resolveQuickPayConfigFromJSON(JSONArray jSONArray) {
        if (jSONArray != null) {
            for (int i = 0; i < jSONArray.length(); i++) {
                try {
                    JSONObject jSONObject = jSONArray.getJSONObject(i);
                    if (jSONObject.getString("name").equals("op_quick_pay_wechat_qrcode_config")) {
                        updateQuickPayIfNeed(0, jSONObject);
                    } else if (jSONObject.getString("name").equals("op_quick_pay_wechat_scanning_config")) {
                        updateQuickPayIfNeed(1, jSONObject);
                    } else if (jSONObject.getString("name").equals("op_quick_pay_alipay_qrcode_config")) {
                        updateQuickPayIfNeed(2, jSONObject);
                    } else if (jSONObject.getString("name").equals("op_quick_pay_alipay_scanning_config")) {
                        updateQuickPayIfNeed(3, jSONObject);
                    } else if (jSONObject.getString("name").equals("op_quick_pay_paytm_config")) {
                        updateQuickPayIfNeed(4, jSONObject);
                    }
                } catch (JSONException e) {
                    Log.e("QuickLaunch.QLHelper", "[OnlineConfig] QuickPayConfigUpdater, error message:" + e.getMessage());
                    return;
                } catch (Exception e2) {
                    Log.e("QuickLaunch.QLHelper", "[OnlineConfig] QuickPayConfigUpdater, error message:" + e2.getMessage());
                    return;
                }
            }
            Log.v("QuickLaunch.QLHelper", "[OnlineConfig] QuickPayConfigUpdater updated complete");
        }
    }

    /* access modifiers changed from: package-private */
    public void updateQuickPayIfNeed(int i, JSONObject jSONObject) throws JSONException {
        JSONArray jSONArray = jSONObject.getJSONArray("value");
        for (int i2 = 0; i2 < jSONArray.length(); i2++) {
            String string = jSONArray.getString(i2);
            String[] split = string.split(";");
            if (split.length >= 5 && this.mPaymentApps.size() >= 5 && isNewConfig(this.mPaymentApps.get(i).packageName, split[4])) {
                OpQLAdapter.OPQuickPayConfig oPQuickPayConfig = new OpQLAdapter.OPQuickPayConfig();
                oPQuickPayConfig.packageName = split[0];
                if (!split[1].equals("sdk")) {
                    if (split[1].contains("://")) {
                        oPQuickPayConfig.urlScheme = split[1];
                    } else {
                        oPQuickPayConfig.className = split[1];
                        oPQuickPayConfig.targetClassName = oPQuickPayConfig.packageName + "/" + oPQuickPayConfig.className;
                    }
                }
                if ("default".equals(split[2])) {
                    oPQuickPayConfig.isDefault = true;
                }
                if (!"class".equals(split[3])) {
                    oPQuickPayConfig.targetClassName = split[3];
                }
                this.mPaymentApps.set(i, oPQuickPayConfig);
                Log.v("QuickLaunch.QLHelper", "QuickPay: update " + i + " to " + string);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isNewConfig(String str, String str2) {
        if (isPackageAvailable(str) && str2 != "") {
            try {
                PackageInfo packageInfo = this.mPackageManager.getPackageInfo(str, 0);
                if (packageInfo != null) {
                    String[] split = packageInfo.versionName.split("\\.");
                    String[] split2 = str2.split("\\.");
                    int max = Math.max(split.length, split2.length);
                    int i = 0;
                    while (i < max) {
                        int parseInt = i < split.length ? Integer.parseInt(split[i]) : 0;
                        int parseInt2 = i < split2.length ? Integer.parseInt(split2[i]) : 0;
                        if (parseInt < parseInt2) {
                            return true;
                        }
                        if (parseInt > parseInt2) {
                            return false;
                        }
                        i++;
                    }
                }
            } catch (Exception e) {
                Log.w("QuickLaunch.QLHelper", "Exception e = " + e.toString());
            }
        }
        return false;
    }
}
