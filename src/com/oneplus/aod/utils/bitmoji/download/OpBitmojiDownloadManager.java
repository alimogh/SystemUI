package com.oneplus.aod.utils.bitmoji.download;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.aod.utils.bitmoji.MdmLogger;
import com.oneplus.aod.utils.bitmoji.OpBitmojiHelper;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.OpBitmojiNotificationManager;
import com.oneplus.aod.utils.bitmoji.download.OpBitmojiNetworkTypeObserver;
import com.oneplus.aod.utils.bitmoji.download.item.LocalPack;
import com.oneplus.aod.utils.bitmoji.download.item.Pack;
import com.oneplus.aod.utils.bitmoji.download.item.Sticker;
import com.oneplus.aod.utils.bitmoji.download.task.AvatarDownloadTask;
import com.oneplus.aod.utils.bitmoji.download.task.BaseDownloadTask;
import com.oneplus.aod.utils.bitmoji.download.task.PackDownloadTask;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
public class OpBitmojiDownloadManager implements BaseDownloadTask.OnDownloadDoneListener {
    static final HashMap<String, String> DOWNLOAD_PACK_INFO;
    private H mBgHandler;
    private Context mContext;
    private OpBitmojiDownloadInfo mDownloadInfo;
    private OpBitmojiDownloader mDownloader;
    private HandlerThread mHandlerThread;
    private OpBitmojiNetworkTypeObserver.OnNetworkTypeChangeListener mNetworkCallback = new OpBitmojiNetworkTypeObserver.OnNetworkTypeChangeListener() { // from class: com.oneplus.aod.utils.bitmoji.download.OpBitmojiDownloadManager.2
        @Override // com.oneplus.aod.utils.bitmoji.download.OpBitmojiNetworkTypeObserver.OnNetworkTypeChangeListener
        public void onNetworkTypeChange(int i) {
            boolean isOwnerClockBitmoji = OpBitmojiDownloadManager.this.isOwnerClockBitmoji();
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiDownloadManager", "onNetworkTypeChange: networkType= " + i + ", isOwnerClockBitmoji= " + isOwnerClockBitmoji);
            }
            OpBitmojiHelper.getInstance().shouldNotifyDownloadStatusChange();
            OpBitmojiDownloadManager.this.showStickerUpdateNotificationIfPossible();
            if (!OpBitmojiDownloadManager.this.checkAccessAvaiable()) {
                Log.d("OpBitmojiDownloadManager", "onNetworkTypeChange: access unavailable");
                OpBitmojiDownloadManager.this.stopAllTask();
            } else if (isOwnerClockBitmoji) {
                OpBitmojiDownloadManager.this.startDownloadAll(false, "recover detect by network");
            }
        }
    };
    private OpBitmojiNetworkTypeObserver mNetworkObserver;
    private OpBitmojiNotificationManager.OnDownloadNotificationActionListener mNotificationListener = new OpBitmojiNotificationManager.OnDownloadNotificationActionListener() { // from class: com.oneplus.aod.utils.bitmoji.download.OpBitmojiDownloadManager.1
        @Override // com.oneplus.aod.utils.bitmoji.OpBitmojiNotificationManager.OnDownloadNotificationActionListener
        public void onCancel() {
            OpBitmojiDownloadManager.this.stopAllTask();
        }
    };
    private OpBitmojiNotificationManager mNotificationManager;
    private PowerManager.WakeLock mWakeLock;

    static {
        HashMap<String, String> hashMap = new HashMap<>();
        DOWNLOAD_PACK_INFO = hashMap;
        hashMap.put("mornin", "time");
        DOWNLOAD_PACK_INFO.put("afternoon", "time");
        DOWNLOAD_PACK_INFO.put("evening", "time");
        DOWNLOAD_PACK_INFO.put("night", "time");
        DOWNLOAD_PACK_INFO.put("weekday", "date");
        DOWNLOAD_PACK_INFO.put("weekend", "date");
        DOWNLOAD_PACK_INFO.put("sun", "weather");
        DOWNLOAD_PACK_INFO.put("cloud", "weather");
        DOWNLOAD_PACK_INFO.put("rain", "weather");
        DOWNLOAD_PACK_INFO.put("snow", "weather");
        DOWNLOAD_PACK_INFO.put("tunes", "music");
        DOWNLOAD_PACK_INFO.put("watching", "video");
        DOWNLOAD_PACK_INFO.put("gaming", "gaming");
        DOWNLOAD_PACK_INFO.put("camera", "camera");
        DOWNLOAD_PACK_INFO.put("messaging", "messaging");
        DOWNLOAD_PACK_INFO.put("battery_low", "battery");
        DOWNLOAD_PACK_INFO.put("charging", "battery");
        DOWNLOAD_PACK_INFO.put("tgif", "tgif");
    }

    public OpBitmojiDownloadManager(Context context, OpBitmojiNotificationManager opBitmojiNotificationManager) {
        new Handler(Looper.getMainLooper());
        this.mContext = context;
        this.mDownloadInfo = new OpBitmojiDownloadInfo(context);
        OpBitmojiDownloader opBitmojiDownloader = new OpBitmojiDownloader(context);
        this.mDownloader = opBitmojiDownloader;
        this.mNotificationManager = opBitmojiNotificationManager;
        opBitmojiNotificationManager.setDownloadInfo(opBitmojiDownloader, this.mNotificationListener);
        this.mNetworkObserver = new OpBitmojiNetworkTypeObserver(context, this.mNetworkCallback);
        PowerManager.WakeLock newWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, OpBitmojiDownloadManager.class.getSimpleName() + ":run");
        this.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(false);
        HandlerThread handlerThread = new HandlerThread("OpBitmojiDownloadManager");
        this.mHandlerThread = handlerThread;
        handlerThread.start();
        this.mBgHandler = new H(this.mHandlerThread.getLooper());
    }

    public void prepare() {
        this.mNetworkObserver.prepare();
    }

    @Override // com.oneplus.aod.utils.bitmoji.download.task.BaseDownloadTask.OnDownloadDoneListener
    public void onDownloadSuccess(String str) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiDownloadManager", "onDownloadSuccess: key= " + str);
        }
        this.mNotificationManager.updateDownloadNotification();
        wakeLockRelease();
    }

    @Override // com.oneplus.aod.utils.bitmoji.download.task.BaseDownloadTask.OnDownloadDoneListener
    public void onDownloadFail(String str) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiDownloadManager", "onDownloadFail: key= " + str);
        }
        this.mNotificationManager.updateDownloadNotification();
        wakeLockRelease();
    }

    public int getDownloadStatus() {
        boolean isNetworkTypeWifi = this.mNetworkObserver.isNetworkTypeWifi();
        boolean isDownloadViaMobile = OpBitmojiHelper.isDownloadViaMobile(this.mContext);
        if (hasNewOrUpdateData(true) && !isNetworkTypeWifi && !isDownloadViaMobile) {
            if (!this.mDownloader.hasForceData()) {
                return 2;
            }
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiDownloadManager", "has force download task");
            }
        }
        ArrayList<String> downloadingList = this.mDownloader.getDownloadingList();
        if (downloadingList.size() <= 0) {
            return 0;
        }
        Iterator<String> it = downloadingList.iterator();
        while (it.hasNext()) {
            if (this.mDownloadInfo.isPackNeesUpdate(it.next())) {
                return 3;
            }
        }
        return 1;
    }

    public void startDownloadAll(boolean z, String str) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiDownloadManager", "startDownloadAll: force= " + z + ", reason= " + str);
        }
        if (hasNewOrUpdateData(z)) {
            startDownloadAll(z, false);
        }
    }

    public void startDownloadAll(boolean z, boolean z2) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiDownloadManager", "startDownloadAll: force= " + z + ", contentChange= " + z2);
        }
        if (!checkAccessAvaiable(z)) {
            Log.d("OpBitmojiDownloadManager", "startDownloadAll: access unavailable");
            return;
        }
        stopAllTask();
        wakeLockAcquire();
        this.mBgHandler.sendMessageDelayed(this.mBgHandler.obtainMessage(1, z ? 1 : 0, 0), z2 ? 1000 : 0);
    }

    public void checkUserConfig(boolean z) {
        if (!z && !this.mNetworkObserver.isNetworkTypeMobile()) {
            stopAllTask();
        }
        showStickerUpdateNotificationIfPossible();
    }

    public void startDownloadCertainPackIfPossible(String str) {
        if (isOwnerClockBitmoji()) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiDownloadManager", "startDownloadPackIfPossible: " + str);
            }
            if (!checkAccessAvaiable()) {
                Log.d("OpBitmojiDownloadManager", "startDownloadPackIfPossible: access unavailable");
            } else if (!this.mBgHandler.hasMessages(3, str)) {
                createPackDirIfNeeded(str);
                if (this.mDownloadInfo.isPackNeedsUpdateOrDownload(str)) {
                    logUpdateStickerEvent(false);
                    checkIfPackNeedsToDownload(str, false);
                }
            } else if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiDownloadManager", "startDownloadPackIfPossible: already in download list");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void queryAll(boolean z) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiDownloadManager", "queryAll: start");
        }
        if (!checkAccessAvaiable(z)) {
            Log.d("OpBitmojiDownloadManager", "queryAll: access unavailable");
            return;
        }
        checkIfAvatarNeedsToDownload(z);
        ArrayList<String> downloadPackList = getDownloadPackList(z);
        if (downloadPackList.size() > 0) {
            logUpdateStickerEvent(z);
            Iterator<String> it = downloadPackList.iterator();
            while (it.hasNext()) {
                String next = it.next();
                createPackDirIfNeeded(next);
                checkIfPackNeedsToDownload(next, z);
            }
        }
    }

    private void checkIfAvatarNeedsToDownload(boolean z) {
        if (this.mDownloadInfo.isAvatarNeedsUpdate()) {
            startDownloadAvatar(z);
        } else if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiDownloadManager", "avatar no needs to download");
        }
    }

    private void checkIfPackNeedsToDownload(String str, boolean z) {
        if (this.mDownloadInfo.isPackNeedsUpdateOrDownload(str)) {
            wakeLockAcquire();
            this.mBgHandler.sendMessage(this.mBgHandler.obtainMessage(3, z ? 1 : 0, 0, str));
        } else if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiDownloadManager", "pack " + str + " no needs to download");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkAccessAvaiable() {
        return checkAccessAvaiable(false);
    }

    private boolean checkAccessAvaiable(boolean z) {
        int bitmojiStatus = OpBitmojiHelper.getInstance().getBitmojiStatus();
        if (bitmojiStatus != 1) {
            if (Build.DEBUG_ONEPLUS) {
                Log.e("OpBitmojiDownloadManager", "checkAccessAvaiable: fail! status= " + bitmojiStatus);
            }
            return false;
        } else if (isNetworkAvailable(z)) {
            return true;
        } else {
            Log.d("OpBitmojiDownloadManager", "checkAccessAvaiable: network is not available or fit");
            return false;
        }
    }

    private boolean isNetworkAvailable(boolean z) {
        if (this.mNetworkObserver.isNetworkUnavailable()) {
            Log.d("OpBitmojiDownloadManager", "network unavailable");
            return false;
        } else if (z) {
            return true;
        } else {
            boolean isDownloadViaMobile = OpBitmojiHelper.isDownloadViaMobile(this.mContext);
            if (!this.mNetworkObserver.isNetworkTypeMobile() || isDownloadViaMobile) {
                return true;
            }
            Log.d("OpBitmojiDownloadManager", "user choose wifi-only, but current network type is mobile");
            return false;
        }
    }

    private boolean hasNewOrUpdateData(boolean z) {
        return getDownloadPackList(z).size() > 0 || this.mDownloadInfo.isAvatarNeedsUpdate();
    }

    private ArrayList<String> getDownloadPackList(boolean z) {
        boolean isDownloadViaMobile = OpBitmojiHelper.isDownloadViaMobile(this.mContext);
        boolean isNetworkTypeMobile = this.mNetworkObserver.isNetworkTypeMobile();
        ArrayList<String> arrayList = new ArrayList<>();
        if (!this.mNetworkObserver.isNetworkUnavailable() && (!isNetworkTypeMobile || isDownloadViaMobile || z)) {
            for (Map.Entry<String, String> entry : DOWNLOAD_PACK_INFO.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (z || !isNetworkTypeMobile || "time".equals(value) || "date".equals(value)) {
                    if (this.mDownloadInfo.isPackNeedsUpdateOrDownload(key)) {
                        arrayList.add(key);
                    }
                }
            }
            if (!z && isNetworkTypeMobile) {
                String activePack = ((OpBitmojiManager) Dependency.get(OpBitmojiManager.class)).getActivePack();
                if (!TextUtils.isEmpty(activePack) && !arrayList.contains(activePack) && this.mDownloadInfo.isPackNeedsUpdateOrDownload(activePack)) {
                    arrayList.add(activePack);
                }
            }
        }
        return arrayList;
    }

    public static String getTriggerIdByPackId(String str) {
        return DOWNLOAD_PACK_INFO.get(str);
    }

    public void needsUpdate(boolean z) {
        MdmLogger.getInstance(this.mContext).logStickerNeedsUpdateEvent();
        this.mDownloadInfo.needsUpdate(z);
    }

    private void createPackDirIfNeeded(String str) {
        File packFolder = OpBitmojiHelper.getInstance().getPackFolder(str);
        if (!packFolder.exists() && !packFolder.mkdirs()) {
            Log.e("OpBitmojiDownloadManager", "createPackDirIfNeeded: failed. packId= " + str);
        }
    }

    public void startDownloadAvatar(boolean z) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiDownloadManager", "startDownloadAvatar: force= " + z);
        }
        if (!checkAccessAvaiable(true)) {
            Log.d("OpBitmojiDownloadManager", "startDownloadAvatar: access unavailable");
            return;
        }
        wakeLockAcquire();
        this.mBgHandler.removeMessages(2);
        this.mBgHandler.sendMessage(this.mBgHandler.obtainMessage(2, z ? 1 : 0, 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDownloadAvatar(boolean z) {
        if (!this.mDownloadInfo.isAvatarNeedsUpdate()) {
            onDownloadSuccess("avatar");
            return;
        }
        AvatarDownloadTask avatarDownloadTask = new AvatarDownloadTask(this.mContext, this.mBgHandler, this, this.mDownloadInfo.getAvatar());
        avatarDownloadTask.setForce(z);
        this.mDownloader.enqueue(avatarDownloadTask);
    }

    private boolean validatePack(Pack pack) {
        HashMap<String, Sticker> hashMap;
        String packId = pack.getPackId();
        if ("battery_low".equals(packId) || "charging".equals(packId)) {
            hashMap = ((LocalPack) pack).getLocalStickers();
        } else {
            Cursor query = this.mContext.getContentResolver().query(OpBitmojiHelper.getInstance().getPackUri(packId), null, null, null, null);
            if (query != null) {
                hashMap = new HashMap<>();
                try {
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("OpBitmojiDownloadManager", "validatePack: count from cursor= " + query.getCount());
                    }
                    while (query.moveToNext()) {
                        try {
                            Sticker createFromCursor = Sticker.createFromCursor(packId, query);
                            hashMap.put(createFromCursor.getName(), createFromCursor);
                        } catch (Exception e) {
                            Log.e("OpBitmojiDownloadManager", "validatePack: item error", e);
                        }
                    }
                } finally {
                    query.close();
                }
            } else {
                Log.w("OpBitmojiDownloadManager", "validatePack: cursor is null. packId= " + packId);
                hashMap = null;
            }
        }
        if (hashMap == null) {
            return false;
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiDownloadManager", "validatePack: packId= " + packId + ", sticker count= " + hashMap.size());
        }
        if (hashMap.size() > 0) {
            pack.checkUnmatchStickers(hashMap);
            try {
                pack.writeToFile(this.mContext);
                return true;
            } catch (Exception e2) {
                Log.e("OpBitmojiDownloadManager", "validatePack: write to file error. " + packId, e2);
                return false;
            }
        } else {
            Log.e("OpBitmojiDownloadManager", "validatePack: stickers are empty for pack= " + packId);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDownloadPackData(String str, boolean z) {
        Pack pack = this.mDownloadInfo.getPack(str);
        if (!validatePack(pack)) {
            onDownloadFail(str);
        } else if (pack.isNeedsUpdateOrDownload()) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiDownloadManager", "pack download this time " + str + ", force= " + z);
            }
            PackDownloadTask packDownloadTask = new PackDownloadTask(this.mContext, this.mBgHandler, this, pack);
            packDownloadTask.setForce(z);
            this.mDownloader.enqueue(packDownloadTask);
            this.mNotificationManager.updateDownloadNotification();
        } else {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiDownloadManager", "queryPackData: no needs to download " + str);
            }
            onDownloadSuccess(str);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopAllTask() {
        this.mBgHandler.removeCallbacksAndMessages(null);
        if (this.mDownloader.hasUndoneTask()) {
            wakeLockAcquire();
            this.mBgHandler.sendEmptyMessage(4);
            return;
        }
        wakeLockRelease();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isOwnerClockBitmoji() {
        int currentAodClockStyle = OpAodUtils.getCurrentAodClockStyle(this.mContext, 0);
        if (KeyguardUpdateMonitor.getCurrentUser() == 0 && currentAodClockStyle == 12) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleStopDownloadTask() {
        this.mDownloader.stopAll();
    }

    public void showStickerUpdateNotificationIfPossible() {
        if (isOwnerClockBitmoji()) {
            boolean isDownloadViaMobile = OpBitmojiHelper.isDownloadViaMobile(this.mContext);
            boolean isNetworkTypeMobile = this.mNetworkObserver.isNetworkTypeMobile();
            if (!hasNewOrUpdateData(true) || isDownloadViaMobile || !isNetworkTypeMobile) {
                this.mNotificationManager.removeStickerUpdateNotification();
            } else {
                this.mNotificationManager.updateStickerUpdateNotification();
            }
        }
    }

    private void wakeLockAcquire() {
        if (!this.mWakeLock.isHeld()) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiDownloadManager", "wakeLockAcquire: callers= " + Debug.getCallers(1));
            }
            this.mWakeLock.acquire();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void wakeLockRelease() {
        if (this.mWakeLock.isHeld() && !this.mBgHandler.hasMessagesOrCallbacks()) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiDownloadManager", "wakeLockRelease: callers= " + Debug.getCallers(1));
            }
            this.mWakeLock.release();
        }
    }

    private void logUpdateStickerEvent(boolean z) {
        MdmLogger.getInstance(this.mContext).logUpdateStickerEvent(z ? 3 : this.mNetworkObserver.isNetworkTypeMobile() ? 1 : 2);
    }

    /* access modifiers changed from: private */
    public class H extends Handler {
        public H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            boolean z = false;
            if (i == 1) {
                OpBitmojiDownloadManager opBitmojiDownloadManager = OpBitmojiDownloadManager.this;
                if (message.arg1 == 1) {
                    z = true;
                }
                opBitmojiDownloadManager.queryAll(z);
            } else if (i == 2) {
                OpBitmojiDownloadManager opBitmojiDownloadManager2 = OpBitmojiDownloadManager.this;
                if (message.arg1 == 1) {
                    z = true;
                }
                opBitmojiDownloadManager2.handleDownloadAvatar(z);
            } else if (i == 3) {
                String str = (String) message.obj;
                if (message.arg1 == 1) {
                    z = true;
                }
                OpBitmojiDownloadManager.this.handleDownloadPackData(str, z);
            } else if (i == 4) {
                OpBitmojiDownloadManager.this.handleStopDownloadTask();
            }
            OpBitmojiDownloadManager.this.wakeLockRelease();
        }
    }
}
