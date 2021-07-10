package com.oneplus.aod.utils.bitmoji;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.Dependency;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.aod.utils.bitmoji.download.OpBitmojiDownloadManager;
import com.oneplus.systemui.OpSystemUIProvider;
import com.oneplus.util.OpUtils;
import java.io.File;
import org.json.JSONException;
import org.json.JSONObject;
public class OpBitmojiHelper {
    private static OpBitmojiHelper sInstance;
    private Context mContext;
    private OpBitmojiDownloadManager mDownloadManager;
    private int mDownloadStatus;
    private boolean mInitialized;
    private OpBitmojiNotificationManager mNotificationManager;
    private OpBitmojiObserver mObserver;
    private OpBitmojiReceiver mReceiver = new OpBitmojiReceiver();
    private boolean mRegistered;

    private OpBitmojiHelper(Context context) {
        this.mContext = context;
        this.mNotificationManager = new OpBitmojiNotificationManager(context);
        this.mDownloadManager = new OpBitmojiDownloadManager(context, this.mNotificationManager);
        this.mObserver = new OpBitmojiObserver(context, this.mDownloadManager);
    }

    public static void init(Context context) {
        synchronized (OpBitmojiHelper.class) {
            if (sInstance == null) {
                OpBitmojiHelper opBitmojiHelper = new OpBitmojiHelper(context);
                sInstance = opBitmojiHelper;
                opBitmojiHelper.init();
                return;
            }
            Log.w("OpBitmojiHelper", "already init.");
        }
    }

    public static OpBitmojiHelper getInstance() {
        OpBitmojiHelper opBitmojiHelper = sInstance;
        if (opBitmojiHelper != null) {
            return opBitmojiHelper;
        }
        Log.e("OpBitmojiHelper", "not initial yet, call init before.");
        throw new RuntimeException("not initial yet, call init before.");
    }

    public static boolean isBitmojiAodEnabled() {
        return !OpUtils.isHydrogen();
    }

    private void init() {
        synchronized (this) {
            if (!this.mInitialized) {
                this.mDownloadManager.prepare();
                this.mDownloadStatus = this.mDownloadManager.getDownloadStatus();
                checkVersion();
                this.mInitialized = true;
            }
        }
    }

    public void startPackageListening() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.OWNER, intentFilter, null, null);
    }

    public void registerBitmojiObserver() {
        if (!this.mRegistered) {
            try {
                this.mContext.getContentResolver().registerContentObserver(getSelfieUri(), true, this.mObserver);
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("OpBitmojiHelper", "registerBitmojiObserver: success");
                }
                this.mRegistered = true;
            } catch (Exception e) {
                Log.e("OpBitmojiHelper", "registerBitmojiObserver: occur error", e);
            }
        } else if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiHelper", "registerBitmojiObserver: already registered");
        }
    }

    public void unregisterBitmojiObserver() {
        if (this.mRegistered) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
            this.mRegistered = false;
        }
    }

    public void startDownloading(boolean z) {
        startDownloading(z, "start downloading");
    }

    public void startDownloading(boolean z, String str) {
        this.mDownloadManager.startDownloadAll(z, str);
    }

    public void startDownloadCertainPackIfPossible(String str) {
        this.mDownloadManager.startDownloadCertainPackIfPossible(str);
    }

    public void updateStatusFromStore(ContentValues contentValues) {
        if (contentValues == null || contentValues.isEmpty()) {
            Log.w("OpBitmojiHelper", "updateStatusFromStore values null or empty");
            return;
        }
        String asString = contentValues.getAsString("action");
        boolean booleanValue = contentValues.containsKey("success") ? contentValues.getAsBoolean("success").booleanValue() : false;
        if (!"com.android.launcher.action.ACTION_PACKAGE_DEQUEUED".equals(asString) || !booleanValue) {
            SharedPreferences sharedPreferences = this.mContext.getSharedPreferences("bitmoji_info_prefs", 0);
            JSONObject jSONObject = new JSONObject();
            try {
                jSONObject.put("action", asString);
                if (contentValues.containsKey("success")) {
                    jSONObject.put("success", booleanValue);
                }
                if (contentValues.containsKey("timestemp")) {
                    jSONObject.put("timestemp", contentValues.getAsLong("timestemp"));
                }
            } catch (JSONException e) {
                Log.e("OpBitmojiHelper", "updateStatusFromStore occur error", e);
            }
            sharedPreferences.edit().putString("status_from_store", jSONObject.toString()).apply();
            return;
        }
        clearStatusFromStore();
    }

    public void clearStatusFromStore() {
        this.mContext.getSharedPreferences("bitmoji_info_prefs", 0).edit().remove("status_from_store").apply();
    }

    public int getStatusFromStore() {
        String string = this.mContext.getSharedPreferences("bitmoji_info_prefs", 0).getString("status_from_store", null);
        if (!TextUtils.isEmpty(string)) {
            try {
                JSONObject jSONObject = new JSONObject(string);
                long j = jSONObject.getLong("timestemp");
                long elapsedRealtime = SystemClock.elapsedRealtime();
                if (elapsedRealtime >= j && elapsedRealtime - j <= 300000) {
                    String string2 = jSONObject.getString("action");
                    boolean optBoolean = jSONObject.optBoolean("success");
                    char c = 65535;
                    switch (string2.hashCode()) {
                        case -1349147213:
                            if (string2.equals("com.android.launcher.action.ACTION_PACKAGE_INSTALLING")) {
                                c = 2;
                                break;
                            }
                            break;
                        case -544688768:
                            if (string2.equals("com.android.launcher.action.ACTION_PACKAGE_DEQUEUED")) {
                                c = 3;
                                break;
                            }
                            break;
                        case 595720104:
                            if (string2.equals("com.android.launcher.action.ACTION_PACKAGE_ENQUEUED")) {
                                c = 0;
                                break;
                            }
                            break;
                        case 2065788526:
                            if (string2.equals("com.android.launcher.action.ACTION_PACKAGE_DOWNLOADING")) {
                                c = 1;
                                break;
                            }
                            break;
                    }
                    if (c == 0) {
                        return 1;
                    }
                    if (c == 1) {
                        return 2;
                    }
                    if (c == 2) {
                        return 3;
                    }
                    if (c == 3) {
                        return optBoolean ? 5 : 4;
                    }
                }
            } catch (JSONException e) {
                Log.e("OpBitmojiHelper", "getStatusFromStore occur error", e);
            }
        }
        return 0;
    }

    public boolean isApplyFirstTime() {
        return this.mContext.getSharedPreferences("bitmoji_info_prefs", 0).getBoolean("apply_first_time", true);
    }

    public void firstApply() {
        this.mContext.getSharedPreferences("bitmoji_info_prefs", 0).edit().putBoolean("apply_first_time", false).apply();
    }

    public static boolean isDownloadViaMobile(Context context) {
        return context.getSharedPreferences("bitmoji_info_prefs", 0).getBoolean("download_via_mobile", true);
    }

    public void setDownloadViaMobile(boolean z) {
        this.mContext.getSharedPreferences("bitmoji_info_prefs", 0).edit().putBoolean("download_via_mobile", z).commit();
        shouldNotifyDownloadStatusChange();
        this.mDownloadManager.checkUserConfig(z);
    }

    public boolean isAvatarSet() {
        return this.mContext.getSharedPreferences("bitmoji_info_prefs", 0).getBoolean("avatar_set", false);
    }

    public void avatarSet(boolean z) {
        this.mContext.getSharedPreferences("bitmoji_info_prefs", 0).edit().putBoolean("avatar_set", z).commit();
    }

    public String[] getImagesPathByPackId(String str) {
        File[] listFiles;
        File file = new File(getRootFolder(), str);
        if (!file.exists() || (listFiles = file.listFiles()) == null || listFiles.length <= 0) {
            return null;
        }
        String[] strArr = new String[listFiles.length];
        for (int i = 0; i < listFiles.length; i++) {
            strArr[i] = listFiles[i].toString();
        }
        return strArr;
    }

    public Uri getStatusUri() {
        return getUriInner("status");
    }

    public Uri getSelfieUri() {
        return getUriInner("me");
    }

    public Uri getPackUri(String str) {
        return getUriInner("pack", str);
    }

    public File getRootFolder() {
        return this.mContext.getDir("bitmojiAod", 0);
    }

    public File getPackFolder(String str) {
        return new File(getRootFolder(), str);
    }

    public File getImagePath(String str, Uri uri) {
        return getImagePath(str, uri.getLastPathSegment());
    }

    public File getImagePath(String str, String str2) {
        return new File(getPackFolder(str), str2);
    }

    public File getAvatarFile() {
        return new File(getRootFolder(), "avatar");
    }

    public int getBitmojiStatus() {
        int i;
        Cursor query = this.mContext.getContentResolver().query(getStatusUri(), null, null, null);
        if (query == null) {
            return 0;
        }
        try {
            if (query.moveToNext()) {
                String string = query.getString(query.getColumnIndexOrThrow("status"));
                if ("no_access".equals(string)) {
                    i = 3;
                } else if ("no_avatar".equals(string)) {
                    i = 2;
                } else if ("ready".equals(string)) {
                    i = 1;
                }
                return i;
            }
            query.close();
            return 0;
        } finally {
            query.close();
        }
    }

    public int getOpBitmojiStatus() {
        int bitmojiStatus = getBitmojiStatus();
        if (bitmojiStatus == 1) {
            return bitmojiStatus;
        }
        if (bitmojiStatus == 3 && !isAvatarSet()) {
            Log.d("OpBitmojiHelper", "status is no access but avatar is not set yet.");
            bitmojiStatus = 2;
        }
        int statusFromStore = getStatusFromStore();
        if (statusFromStore != 0 && Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiHelper", "getStatusFromStore: " + statusFromStore);
        }
        if (statusFromStore == 1 || statusFromStore == 2) {
            return 4;
        }
        if (statusFromStore != 3) {
            return bitmojiStatus;
        }
        return 5;
    }

    public int getBitmojiDownloadStatus() {
        return this.mDownloadStatus;
    }

    public void shouldNotifyDownloadStatusChange() {
        int downloadStatus = this.mDownloadManager.getDownloadStatus();
        if (this.mDownloadStatus != downloadStatus) {
            this.mDownloadStatus = downloadStatus;
            OpSystemUIProvider.notifyDownloadStatusUpdate(this.mContext);
        }
    }

    private Uri getUriInner(String... strArr) {
        Uri.Builder authority = new Uri.Builder().scheme("content").authority("com.bitstrips.imoji.provider");
        for (String str : strArr) {
            authority.appendPath(str);
        }
        return authority.build();
    }

    public Uri getStickerUriByName(String str) {
        return getUriInner("me", "sticker", str);
    }

    public OpBitmojiNotificationManager getNotificationManager() {
        return this.mNotificationManager;
    }

    private void checkVersion() {
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences("bitmoji_info_prefs", 0);
        int i = sharedPreferences.getInt("version", 0);
        if (i != 2) {
            if (i != 0) {
                if (i > 2) {
                    onDowngrade(i, 2);
                } else {
                    onUpgrade(i, 2);
                }
            }
            sharedPreferences.edit().putInt("version", 2).apply();
        }
        Log.d("OpBitmojiHelper", "version: 2");
    }

    private void onUpgrade(int i, int i2) {
        Log.d("OpBitmojiHelper", "onUpgrade: oldVersion= " + i + ", currentVersion= " + i2);
        if (i == 1 && getAvatarFile().exists()) {
            avatarSet(true);
        }
        if (i != i2) {
            this.mDownloadManager.showStickerUpdateNotificationIfPossible();
        }
    }

    private void onDowngrade(int i, int i2) {
        Log.d("OpBitmojiHelper", "onDowngrade: oldVersion= " + i + ", currentVersion= " + i2);
    }

    public static class OpBitmojiReceiver extends BroadcastReceiver {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Uri data = intent.getData();
            if (data != null && "com.bitstrips.imoji".equals(data.getSchemeSpecificPart())) {
                String action = intent.getAction();
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("OpBitmojiReceiver", "onReceive: action= " + action);
                }
                if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                    OpBitmojiHelper.getInstance().clearStatusFromStore();
                    OpBitmojiHelper.getInstance().registerBitmojiObserver();
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    OpBitmojiHelper.getInstance().avatarSet(false);
                    OpBitmojiHelper.getInstance().clearStatusFromStore();
                    OpBitmojiHelper.getInstance().getNotificationManager().clearShowReadyNotificationFlag();
                    OpBitmojiHelper.getInstance().unregisterBitmojiObserver();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class OpBitmojiObserver extends ContentObserver {
        private Context mContext;
        private OpBitmojiDownloadManager mDownloadManager;

        public OpBitmojiObserver(Context context, OpBitmojiDownloadManager opBitmojiDownloadManager) {
            super(new Handler((Looper) Dependency.get(Dependency.BG_LOOPER)));
            this.mContext = context;
            this.mDownloadManager = opBitmojiDownloadManager;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            if (OpBitmojiHelper.getInstance().getSelfieUri().equals(uri)) {
                int bitmojiStatus = OpBitmojiHelper.getInstance().getBitmojiStatus();
                int currentAodClockStyle = OpAodUtils.getCurrentAodClockStyle(this.mContext, -2);
                Log.d("OpBitmojiObserver", "onChange: selfie, status= " + bitmojiStatus + ", clockStyle= " + currentAodClockStyle);
                this.mDownloadManager.needsUpdate(OpBitmojiHelper.getInstance().isApplyFirstTime());
                if (currentAodClockStyle != 12) {
                    if (bitmojiStatus == 1) {
                        this.mDownloadManager.startDownloadAvatar(false);
                    }
                    OpBitmojiHelper.getInstance().getNotificationManager().checkNeedToShowReadyNotification();
                } else if (bitmojiStatus == 1) {
                    this.mDownloadManager.startDownloadAll(false, true);
                }
                if (!OpBitmojiHelper.getInstance().isAvatarSet() && bitmojiStatus == 1) {
                    OpBitmojiHelper.getInstance().avatarSet(true);
                }
                this.mDownloadManager.showStickerUpdateNotificationIfPossible();
                OpBitmojiHelper.getInstance().shouldNotifyDownloadStatusChange();
            }
        }
    }
}
