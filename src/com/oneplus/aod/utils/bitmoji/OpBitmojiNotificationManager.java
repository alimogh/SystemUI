package com.oneplus.aod.utils.bitmoji;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.util.NotificationChannels;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.aod.utils.bitmoji.OpBitmojiNotificationManager;
import com.oneplus.aod.utils.bitmoji.download.OpBitmojiDownloader;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: OpBitmojiNotificationManager.kt */
public final class OpBitmojiNotificationManager {
    @NotNull
    private static final String ACTION_CANCEL = "com.oneplus.aod.bitmoji.cancel";
    @NotNull
    private static final String ACTION_READY = "com.oneplus.aod.bitmoji.ready";
    @NotNull
    private static final String ACTION_REDIRECT = "com.oneplus.aod.bitmoji.redirect";
    @NotNull
    private static final String BITMOJI_RECEIVER_ACTION = "com.bitstrips.imoji.provider.action.STATUS_CHANGE";
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final String KEY_NOTIFICATION_TAG = "notificationTag";
    @NotNull
    private static final String KEY_SHOWN_READY_NOTIFICATION = "shown_ready";
    private static final int NOTI_DOWNLOAD_ID = 2;
    private static final int NOTI_READY_ID = 0;
    private static final int NOTI_REVOKE_ID = 1;
    private static final int NOTI_STICKER_UPDATE_ID = 3;
    @NotNull
    private static final String TAG = "OpBitmojiNotificationManager";
    @NotNull
    private final Context context;
    private Intent mCancelIntent;
    private OnDownloadNotificationActionListener mDownloadNotificationListener;
    private OpBitmojiDownloader mDownloader;
    private NotificationManager mNm = ((NotificationManager) this.context.getSystemService(NotificationManager.class));
    private final OpBitmojiNotificationManager$mReceiver$1 mReceiver = new BroadcastReceiver(this) { // from class: com.oneplus.aod.utils.bitmoji.OpBitmojiNotificationManager$mReceiver$1
        final /* synthetic */ OpBitmojiNotificationManager this$0;

        /* JADX WARN: Incorrect args count in method signature: ()V */
        {
            this.this$0 = r1;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(@NotNull Context context, @NotNull Intent intent) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            Intrinsics.checkParameterIsNotNull(intent, "intent");
            String action = intent.getAction();
            if (Intrinsics.areEqual(action, "android.intent.action.LOCALE_CHANGED")) {
                if (this.this$0.findNotificationInStatusBar(OpBitmojiNotificationManager.Companion.getTAG(), OpBitmojiNotificationManager.Companion.getNOTI_READY_ID())) {
                    this.this$0.updateReadyNotification();
                }
                if (this.this$0.findNotificationInStatusBar(OpBitmojiNotificationManager.Companion.getTAG(), OpBitmojiNotificationManager.Companion.getNOTI_REVOKE_ID())) {
                    this.this$0.updateRevokeNotification();
                }
                if (this.this$0.findNotificationInStatusBar(OpBitmojiNotificationManager.Companion.getTAG(), OpBitmojiNotificationManager.Companion.getNOTI_DOWNLOAD_ID())) {
                    this.this$0.updateDownloadNotification();
                }
                if (this.this$0.findNotificationInStatusBar(OpBitmojiNotificationManager.Companion.getTAG(), OpBitmojiNotificationManager.Companion.getNOTI_STICKER_UPDATE_ID())) {
                    this.this$0.updateStickerUpdateNotification();
                }
            } else if (Intrinsics.areEqual(action, OpBitmojiNotificationManager.Companion.getBITMOJI_RECEIVER_ACTION())) {
                String stringExtra = intent.getStringExtra("status");
                if (Build.DEBUG_ONEPLUS) {
                    String tag = OpBitmojiNotificationManager.Companion.getTAG();
                    Log.d(tag, "onReceive: status change= " + stringExtra);
                }
                if (stringExtra != null) {
                    int hashCode = stringExtra.hashCode();
                    if (hashCode != 108386723) {
                        if (hashCode == 211933602 && stringExtra.equals("no_access")) {
                            this.this$0.updateRevokeNotification();
                        }
                    } else if (stringExtra.equals("ready")) {
                        this.this$0.removeNotification(OpBitmojiNotificationManager.Companion.getTAG(), OpBitmojiNotificationManager.Companion.getNOTI_REVOKE_ID());
                        this.this$0.checkNeedToShowReadyNotification();
                        this.this$0.checkIfNeedRecoverDownload();
                    }
                }
            } else if (Intrinsics.areEqual(action, OpBitmojiNotificationManager.Companion.getACTION_CANCEL())) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d(OpBitmojiNotificationManager.Companion.getTAG(), "click cancel");
                }
                NotificationManager notificationManager = this.this$0.mNm;
                if (notificationManager != null) {
                    notificationManager.cancel(OpBitmojiNotificationManager.Companion.getTAG(), OpBitmojiNotificationManager.Companion.getNOTI_DOWNLOAD_ID());
                }
                MdmLogger.Companion.getInstance(context).logCancelDownloadEvent();
                OpBitmojiNotificationManager.OnDownloadNotificationActionListener onDownloadNotificationActionListener = this.this$0.mDownloadNotificationListener;
                if (onDownloadNotificationActionListener != null) {
                    onDownloadNotificationActionListener.onCancel();
                }
            } else if (Intrinsics.areEqual(action, OpBitmojiNotificationManager.Companion.getACTION_READY()) || Intrinsics.areEqual(action, OpBitmojiNotificationManager.Companion.getACTION_REDIRECT())) {
                int intExtra = intent.getIntExtra(OpBitmojiNotificationManager.Companion.getKEY_NOTIFICATION_TAG(), -1);
                Intent intent2 = new Intent();
                intent2.setComponent(ComponentName.unflattenFromString("com.android.settings/.Settings$OPCustomClockSettingsActivity"));
                intent2.setFlags(335544320);
                context.startActivity(intent2);
                if (intExtra == OpBitmojiNotificationManager.Companion.getNOTI_READY_ID()) {
                    MdmLogger.Companion.getInstance(context).logNotificationReadyClickEvent();
                }
            }
        }
    };

    /* compiled from: OpBitmojiNotificationManager.kt */
    public interface OnDownloadNotificationActionListener {
        void onCancel();
    }

    public OpBitmojiNotificationManager(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.context = context;
        Intent intent = new Intent(ACTION_CANCEL);
        intent.setPackage(this.context.getPackageName());
        this.mCancelIntent = intent;
        IntentFilter intentFilter = new IntentFilter("android.intent.action.LOCALE_CHANGED");
        intentFilter.addAction(BITMOJI_RECEIVER_ACTION);
        intentFilter.addAction(ACTION_CANCEL);
        intentFilter.addAction(ACTION_READY);
        intentFilter.addAction(ACTION_REDIRECT);
        this.context.registerReceiverAsUser(this.mReceiver, UserHandle.OWNER, intentFilter, null, null);
    }

    /* compiled from: OpBitmojiNotificationManager.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final String getTAG() {
            return OpBitmojiNotificationManager.TAG;
        }

        @NotNull
        public final String getBITMOJI_RECEIVER_ACTION() {
            return OpBitmojiNotificationManager.BITMOJI_RECEIVER_ACTION;
        }

        @NotNull
        public final String getACTION_CANCEL() {
            return OpBitmojiNotificationManager.ACTION_CANCEL;
        }

        @NotNull
        public final String getACTION_READY() {
            return OpBitmojiNotificationManager.ACTION_READY;
        }

        @NotNull
        public final String getACTION_REDIRECT() {
            return OpBitmojiNotificationManager.ACTION_REDIRECT;
        }

        @NotNull
        public final String getKEY_NOTIFICATION_TAG() {
            return OpBitmojiNotificationManager.KEY_NOTIFICATION_TAG;
        }

        public final int getNOTI_READY_ID() {
            return OpBitmojiNotificationManager.NOTI_READY_ID;
        }

        public final int getNOTI_REVOKE_ID() {
            return OpBitmojiNotificationManager.NOTI_REVOKE_ID;
        }

        public final int getNOTI_DOWNLOAD_ID() {
            return OpBitmojiNotificationManager.NOTI_DOWNLOAD_ID;
        }

        public final int getNOTI_STICKER_UPDATE_ID() {
            return OpBitmojiNotificationManager.NOTI_STICKER_UPDATE_ID;
        }
    }

    public final void setDownloadInfo(@NotNull OpBitmojiDownloader opBitmojiDownloader, @NotNull OnDownloadNotificationActionListener onDownloadNotificationActionListener) {
        Intrinsics.checkParameterIsNotNull(opBitmojiDownloader, "downloader");
        Intrinsics.checkParameterIsNotNull(onDownloadNotificationActionListener, "listener");
        this.mDownloader = opBitmojiDownloader;
        this.mDownloadNotificationListener = onDownloadNotificationActionListener;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0021  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean findNotificationInStatusBar(@org.jetbrains.annotations.NotNull java.lang.String r7, int r8) {
        /*
            r6 = this;
            java.lang.String r0 = "tag"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r7, r0)
            android.app.NotificationManager r6 = r6.mNm
            if (r6 == 0) goto L_0x000e
            android.service.notification.StatusBarNotification[] r6 = r6.getActiveNotifications()
            goto L_0x000f
        L_0x000e:
            r6 = 0
        L_0x000f:
            r0 = 0
            r1 = 1
            if (r6 == 0) goto L_0x001e
            int r2 = r6.length
            if (r2 != 0) goto L_0x0018
            r2 = r1
            goto L_0x0019
        L_0x0018:
            r2 = r0
        L_0x0019:
            if (r2 == 0) goto L_0x001c
            goto L_0x001e
        L_0x001c:
            r2 = r0
            goto L_0x001f
        L_0x001e:
            r2 = r1
        L_0x001f:
            if (r2 != 0) goto L_0x003b
            int r2 = r6.length
            r3 = r0
        L_0x0023:
            if (r3 >= r2) goto L_0x003b
            r4 = r6[r3]
            java.lang.String r5 = r4.getTag()
            boolean r5 = android.text.TextUtils.equals(r7, r5)
            if (r5 == 0) goto L_0x0038
            int r4 = r4.getId()
            if (r4 != r8) goto L_0x0038
            return r1
        L_0x0038:
            int r3 = r3 + 1
            goto L_0x0023
        L_0x003b:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.utils.bitmoji.OpBitmojiNotificationManager.findNotificationInStatusBar(java.lang.String, int):boolean");
    }

    public final void checkNeedToShowReadyNotification() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences("bitmoji_info_prefs", 0);
        if (!sharedPreferences.getBoolean(KEY_SHOWN_READY_NOTIFICATION, false)) {
            updateReadyNotification();
            MdmLogger.Companion.getInstance(this.context).logNotificationReadyShownEvent();
            sharedPreferences.edit().putBoolean(KEY_SHOWN_READY_NOTIFICATION, true).apply();
        }
    }

    public final void clearShowReadyNotificationFlag() {
        this.context.getSharedPreferences("bitmoji_info_prefs", 0).edit().remove(KEY_SHOWN_READY_NOTIFICATION).apply();
    }

    public final void updateDownloadNotification() {
        OpBitmojiDownloader opBitmojiDownloader = this.mDownloader;
        if (opBitmojiDownloader != null) {
            int totalCount = opBitmojiDownloader.getTotalCount();
            if (totalCount == 0) {
                removeNotification(TAG, NOTI_DOWNLOAD_ID);
                if (Build.DEBUG_ONEPLUS) {
                    Log.d(TAG, "download totalCount empty");
                    return;
                }
                return;
            }
            int runningTaskCount = opBitmojiDownloader.getRunningTaskCount();
            if (Build.DEBUG_ONEPLUS) {
                String str = TAG;
                Log.d(str, "updateDownloadNotification: totalCount= " + totalCount + ", runningCount= " + runningTaskCount);
            }
            Notification.Builder onlyAlertOnce = makeNotification(C0015R$string.op_bitmoji_aod_download_stickers_title, -1).setOnlyAlertOnce(true);
            if (totalCount < 0 || runningTaskCount <= 0) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d(TAG, "download completed");
                }
                NotificationManager notificationManager = this.mNm;
                if (notificationManager != null) {
                    notificationManager.cancel(TAG, NOTI_DOWNLOAD_ID);
                    return;
                }
                return;
            }
            onlyAlertOnce.setProgress(totalCount, totalCount - runningTaskCount, false);
            onlyAlertOnce.setOngoing(true);
            onlyAlertOnce.addAction(0, this.context.getString(C0015R$string.cancel), PendingIntent.getBroadcast(this.context, 0, this.mCancelIntent, 134217728));
            NotificationManager notificationManager2 = this.mNm;
            if (notificationManager2 != null) {
                notificationManager2.notify(TAG, NOTI_DOWNLOAD_ID, onlyAlertOnce.build());
            }
        }
    }

    public final void updateStickerUpdateNotification() {
        updateNotificationInner(NOTI_STICKER_UPDATE_ID, C0015R$string.op_bitmoji_sticker_update_title, C0015R$string.op_bitmoji_sticker_update_msg);
    }

    public final void removeStickerUpdateNotification() {
        removeNotification(TAG, NOTI_STICKER_UPDATE_ID);
    }

    /* access modifiers changed from: private */
    public final void updateReadyNotification() {
        updateNotificationInner(NOTI_READY_ID, C0015R$string.op_bitmoji_ready_to_use, C0015R$string.op_bitmoji_ready_to_use_go_check);
    }

    /* access modifiers changed from: private */
    public final void updateRevokeNotification() {
        updateNotificationInner(NOTI_REVOKE_ID, C0015R$string.op_bitmoji_not_working_normally, C0015R$string.op_bitmoji_connect_to_resume);
    }

    /* access modifiers changed from: private */
    public final void removeNotification(String str, int i) {
        NotificationManager notificationManager = this.mNm;
        if (notificationManager != null) {
            notificationManager.cancel(str, i);
        }
    }

    private final void updateNotificationInner(int i, int i2, int i3) {
        Intent intent;
        if (i == NOTI_READY_ID) {
            intent = new Intent(ACTION_READY);
        } else {
            intent = new Intent(ACTION_REDIRECT);
        }
        intent.setPackage(this.context.getPackageName());
        intent.putExtra(KEY_NOTIFICATION_TAG, i);
        Notification.Builder contentIntent = makeNotification(i2, i3).setAutoCancel(true).setContentIntent(PendingIntent.getBroadcast(this.context, 0, intent, 134217728));
        NotificationManager notificationManager = this.mNm;
        if (notificationManager != null) {
            notificationManager.notify(TAG, i, contentIntent.build());
        }
    }

    private final Notification.Builder makeNotification(int i, int i2) {
        Notification.Builder showWhen = new Notification.Builder(this.context, NotificationChannels.HINTS).setContentTitle(this.context.getString(i)).setSmallIcon(C0006R$drawable.ic_bitmoji_noti_icon).setWhen(System.currentTimeMillis()).setShowWhen(true);
        if (i2 != -1) {
            String string = this.context.getString(i2);
            showWhen.setContentText(string);
            showWhen.setStyle(new Notification.BigTextStyle().bigText(string));
        }
        Intrinsics.checkExpressionValueIsNotNull(showWhen, "builder");
        return showWhen;
    }

    /* access modifiers changed from: private */
    public final void checkIfNeedRecoverDownload() {
        if (OpAodUtils.getCurrentAodClockStyle(this.context, 0) == 12) {
            OpBitmojiHelper.getInstance().startDownloading(false, "recover after connect");
        }
    }
}
