package com.oneplus.systemui.util;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.util.Log;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.util.HashMap;
import java.util.Map;
import net.oneplus.odm.OpDeviceManagerInjector;
public class OpMdmLogger implements ConfigurationController.ConfigurationListener {
    private static final boolean DEBUG = SystemProperties.getBoolean("debug.mdm.systemui", false);
    private static Context mContext;
    private static boolean sAutomatic = false;
    private static String sCurOrien;
    private static String sFpType = null;
    private static Handler sHandler;
    private static HandlerThread sHandlerThread;
    private static boolean sNpvExpanded = false;
    private static HashMap<String, String> sQsEvent = new HashMap<>();
    private static boolean sQsExpanded = false;
    private static boolean sStatusBarPulled = false;
    private static Map<String, String> sTagMap;

    static {
        HashMap hashMap = new HashMap();
        sTagMap = hashMap;
        hashMap.put("Tile.AirplaneModeTile", "quick_airplane");
        sTagMap.put("Tile.BatterySaverTile", "quick_battery");
        sTagMap.put("Tile.BluetoothTile", "quick_bt");
        sTagMap.put("Tile.CastTile", "quick_cast");
        sTagMap.put("Tile.CellularTile", "quick_mobile");
        sTagMap.put("Tile.ColorInversionTile", "quick_invert");
        sTagMap.put("Tile.DataSaverTile", "quick_ds");
        sTagMap.put("Tile.FlashlightTile", "quick_fl");
        sTagMap.put("Tile.GameModeTile", "quick_game");
        sTagMap.put("Tile.HotspotTile", "quick_hot");
        sTagMap.put("Tile.LocationTile", "quick_location");
        sTagMap.put("Tile.NfcTile", "quick_nfc");
        sTagMap.put("Tile.NightDisplayTile", "quick_night");
        sTagMap.put("Tile.ReadModeTile", "quick_read");
        sTagMap.put("Tile.RotationLockTile", "quick_ar");
        sTagMap.put("Tile.VPNTile", "quick_vpn");
        sTagMap.put("Tile.WifiTile", "quick_wifi");
        sTagMap.put("Tile.WorkModeTile", "quick_work");
        sTagMap.put("Tile.OtgTile", "quick_otg");
    }

    public static void init(Context context) {
        mContext = context;
        if (sHandlerThread == null) {
            HandlerThread handlerThread = new HandlerThread("MdmLogger", 10);
            sHandlerThread = handlerThread;
            handlerThread.start();
            sHandler = new Handler(sHandlerThread.getLooper());
        }
        initQsEvent();
        Log.d("MdmLogger", "MdmLogger is initialized");
    }

    private static void initQsEvent() {
        sQsEvent.clear();
        sQsEvent.put("click_tile", "0");
        sQsEvent.put("click_bright", "0");
        sQsEvent.put("click_settings", "0");
        sQsEvent.put("click_notif", "0");
        sQsEvent.put("swipe_notif", "0");
        sCurOrien = getOrientationEvent();
    }

    public static void log(String str, String str2, String str3) {
        log(str, str2, str3, "X9HFK50WT7");
    }

    public static void log(String str, String str2, String str3, String str4) {
        HashMap hashMap = new HashMap();
        hashMap.put(str2, str3);
        log(str, hashMap, str4);
    }

    public static void log(final String str, final Map<String, String> map, String str2) {
        if (DEBUG) {
            Log.d("MdmLogger", "log:tag=" + str + ", data=" + map + ", appId=" + str2);
        }
        "lock_unlock_success".equals(str);
        if (map == null) {
            Log.e("MdmLogger", "data map shouldn't be null");
            return;
        }
        map.put("version", "1.0");
        final HashMap hashMap = new HashMap();
        hashMap.put("appid", str2);
        sHandler.post(new Runnable() { // from class: com.oneplus.systemui.util.OpMdmLogger.1
            @Override // java.lang.Runnable
            public void run() {
                if (str.startsWith("lock_unlock_")) {
                    if (OpMdmLogger.sFpType == null) {
                        String unused = OpMdmLogger.sFpType = ((FingerprintManager) OpMdmLogger.mContext.getSystemService("fingerprint")).getSensorType();
                    }
                    if (!(OpMdmLogger.sFpType == null || OpMdmLogger.sFpType.length() == 0)) {
                        map.put("finger_sensor_type", OpMdmLogger.sFpType);
                    }
                }
                if (OpMdmLogger.DEBUG) {
                    Log.d("MdmLogger", "log: mdmData= " + map);
                }
                OpDeviceManagerInjector.getInstance().preserveAppData(OpMdmLogger.mContext, str, map, hashMap);
            }
        });
    }

    public static void logQsTile(String str, String str2, String str3) {
        String str4 = sTagMap.get(str);
        if (str4 != null) {
            log(str4, str2, str3);
            return;
        }
        Log.e("MdmLogger", "Cannot get tag from tileTag : " + str);
    }

    public static void notifyNpvExpanded(boolean z) {
        if (sNpvExpanded != z) {
            if (z && sStatusBarPulled) {
                sStatusBarPulled = false;
                log("landscape_full_screen", "status_bar", "1");
            }
            handleQsUpdate(z);
            sNpvExpanded = z;
        }
    }

    public static void notifyQsExpanded(boolean z) {
        if (sQsExpanded != z && !sNpvExpanded) {
            handleQsUpdate(z);
            sQsExpanded = z;
        }
    }

    private static boolean isLandscape() {
        return Resources.getSystem().getConfiguration().orientation == 2;
    }

    private static String getOrientationEvent() {
        return isLandscape() ? "landscape_pull" : "portrait_pull";
    }

    private static void handleQsUpdate(boolean z) {
        if (z) {
            log(getOrientationEvent(), "pull_down", "1");
            initQsEvent();
            return;
        }
        reportQsEvents();
    }

    private static void reportQsEvents() {
        for (String str : sQsEvent.keySet()) {
            log(sCurOrien, str, sQsEvent.get(str));
        }
    }

    public static void logQsPanel(String str) {
        if (isLandscape() && !"click_settings".equals(str)) {
            "click_notif".equals(str);
        }
        if (sQsEvent.containsKey(str)) {
            sQsEvent.put(str, "1");
        }
    }

    public static void notifyBrightnessMode(boolean z) {
        sAutomatic = z;
    }

    public static void brightnessSliderClicked() {
        log("quick_bright", "manual", sAutomatic ? "1" : "0");
    }
}
