package com.oneplus.notification;

import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.util.OpFeatures;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.app.IAppOpsActiveCallback;
import com.android.internal.app.IAppOpsService;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.settingslib.Utils;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.android.scene.OnePlusSceneCallBlockManagerInjector;
import com.oneplus.core.oimc.OIMCRule;
import com.oneplus.core.oimc.OIMCServiceManager;
import com.oneplus.plugin.OpLsState;
import com.oneplus.systemui.statusbar.phone.OpStatusBar;
import com.oneplus.systemui.util.OpMdmLogger;
import com.oneplus.util.OpUtils;
import com.oneplus.util.notification.SimpleHeadsUpController;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public class OpNotificationController implements ConfigurationController.ConfigurationListener {
    private static final Uri DRIVING_MODE_STATE_URI = Settings.Secure.getUriFor("driving_mode_state");
    private static final Uri ESPORTS_MODE_ENABLED = Settings.System.getUriFor("esport_mode_enabled");
    private static final Uri ESPORT_MODE_DO_NOT_DISTURB_ENABLED = Settings.System.getUriFor("key_do_not_disturb_enabled");
    private static final List<String> FORCE_INVERTION_LIST = Arrays.asList("com.nearme.gamecenter");
    private static final Uri GAME_MODE_3RD_PARTY_CALLS_UID_URI = Settings.System.getUriFor("game_mode_notifications_3rd_calls_uid");
    private static final Uri GAME_MODE_BLOCK_HEADS_UP_URI = Settings.System.getUriFor("game_mode_block_notification");
    private static final List<String> ICON_COLORIZE_LIST = Arrays.asList("com.blueline.signalcheck", "com.yuedong.sport");
    private static final List<String> LIFETIME_EXTENSION_LIST = Arrays.asList("com.oneplus.dialer", "com.google.android.dialer", "com.android.dialer", "com.whatsapp", "com.netease.cloudmusic");
    private static final boolean OP_DEBUG = Build.DEBUG_ONEPLUS;
    private static final Uri OP_OIMC_FUNC_DISABLE_HEADSUP_BRICK_URI = Settings.Global.getUriFor("op_oimc_func_disable_headsup_breath");
    private static final Uri OP_OIMC_FUNC_DISABLE_HEADSUP_URI = Settings.Global.getUriFor("op_oimc_func_disable_headsup");
    private static final Uri OP_QUICKREPLY_IM_LIST_URI = Settings.System.getUriFor("op_quickreply_im_list");
    private static final List<String> PRIORITY_LIST_BRICK_MODE = Arrays.asList("com.oneplus.dialer", "com.google.android.dialer", "com.android.incallui", "com.android.dialer");
    private static final List<String> PRIORITY_LIST_DRIVING_MODE = Arrays.asList("com.oneplus.dialer", "com.google.android.dialer", "com.oneplus.deskclock", "com.android.dialer");
    private static final List<String> PRIORITY_LIST_GAME_MODE = Arrays.asList("com.oneplus.dialer", "com.google.android.dialer", "com.android.incallui", "com.oneplus.deskclock", "com.android.dialer");
    private static final int[] PRIVACY_ALERT_OPS = {26, 27};
    private static final List<String> SYSTEM_APP_LIST = Arrays.asList("com.oneplus.soundrecorder");
    private IAppOpsActiveCallback mAppOpsActiveCallback;
    private IAppOpsService mAppOpsService;
    private boolean mBlockedByBrick = false;
    private boolean mBlockedByDriving = false;
    private boolean mBlockedByGame = false;
    private final CommandQueue mCommandQueue;
    private ConfigurationController mConfigurationController;
    private final Context mContext;
    private boolean mDockedStackExists = false;
    private boolean mESportModeDndOn = false;
    private NotificationEntryManager mEntryManager;
    private boolean mEsportsModeOn = false;
    private String mGameMode3rdPartyCallsUid = "-1";
    private int mGameModeNotifyType = 0;
    private boolean mIsFreeForm = false;
    private boolean mIsKeyguardShowing;
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    private KeyguardUpdateMonitorCallback mMonitorCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.notification.OpNotificationController.2
        @Override // com.oneplus.keyguard.OpKeyguardUpdateMonitorCallback
        public void onSystemReady() {
            Log.d("OpNotificationController", "onSystemReady to register provider and OIMC");
            OpNotificationController.this.mSettingsObserver = new SettingsObserver(new Handler());
            OpNotificationController.this.mSettingsObserver.observe();
            OpNotificationController.this.mOimcObserver = new OimcObserver(new Handler());
            OpNotificationController.this.mOimcObserver.observe();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            OpNotificationController.this.mIsKeyguardShowing = z;
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onPhoneStateChanged(int i) {
            OpNotificationController.this.mPhoneState = i;
        }
    };
    private NotificationManager mNoMan;
    private final OIMCServiceManager mOIMCServiceManager;
    private OimcObserver mOimcObserver;
    private int mOrientation = 0;
    private PackageManager mPackageManager;
    private int mPhoneState;
    private HashMap<String, Integer> mPrivacyAlertList = new HashMap<>();
    private HashMap<String, Integer> mPrivacyGroupCount = new HashMap<>();
    private List<String> mQuickReplyList = Arrays.asList(new String[0]);
    private SettingsObserver mSettingsObserver;
    private SimpleHeadsUpController mSimpleHeadsUpController;
    private String mTopActivity;

    public void setBubbleController(BubbleController bubbleController) {
    }

    public OpNotificationController(Context context) {
        this.mContext = context;
        this.mCommandQueue = (CommandQueue) Dependency.get(CommandQueue.class);
        ConfigurationController configurationController = (ConfigurationController) Dependency.get(ConfigurationController.class);
        this.mConfigurationController = configurationController;
        configurationController.addCallback(this);
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mMonitorCallback);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mMonitorCallback);
        this.mLockscreenUserManager = (NotificationLockscreenUserManager) Dependency.get(NotificationLockscreenUserManager.class);
        this.mOIMCServiceManager = new OIMCServiceManager();
        this.mSimpleHeadsUpController = new SimpleHeadsUpController(this.mContext);
        this.mAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        AnonymousClass1 r6 = new IAppOpsActiveCallback.Stub() { // from class: com.oneplus.notification.OpNotificationController.1
            public void opActiveChanged(int i, int i2, String str, boolean z) {
                Log.d("OpNotificationController", "opActiveChanged, op: " + i + ", uid: " + i2 + ", packageName: " + str + ", active: " + z);
                if (str != null) {
                    OpNotificationController.this.preparePrivacyAlertNotification(i, str, z);
                    OpNotificationController.this.cancelPrivacyAlertIfNeeded();
                }
            }
        };
        this.mAppOpsActiveCallback = r6;
        try {
            this.mAppOpsService.startWatchingActive(PRIVACY_ALERT_OPS, r6);
        } catch (RemoteException e) {
            Log.d("OpNotificationController", "AppOpsService: startWatchingMode fail: " + e.toString());
        }
        this.mNoMan = (NotificationManager) this.mContext.getSystemService("notification");
        this.mPackageManager = this.mContext.getPackageManager();
        NotificationChannel notificationChannel = new NotificationChannel("privacy_alert", this.mContext.getString(C0015R$string.privacy_alert_application_name), 3);
        notificationChannel.enableVibration(false);
        notificationChannel.setSound(null, null);
        notificationChannel.enableLights(false);
        this.mNoMan.createNotificationChannel(notificationChannel);
        this.mPrivacyGroupCount.put("PrivacyAlertGroupCamera", 0);
        this.mPrivacyGroupCount.put("PrivacyAlertGroupMicrophone", 0);
    }

    public int getCallState() {
        return this.mPhoneState;
    }

    public void setIsInSplitScreen(boolean z) {
        this.mDockedStackExists = z;
    }

    public NotificationEntryManager getEntryManager() {
        if (this.mEntryManager == null) {
            this.mEntryManager = (NotificationEntryManager) Dependency.get(NotificationEntryManager.class);
        }
        return this.mEntryManager;
    }

    public int canHeadsUp(StatusBarNotification statusBarNotification) {
        String key = statusBarNotification.getKey();
        if (blockedByBrickMode(statusBarNotification.getPackageName())) {
            if (!OP_DEBUG) {
                return 1;
            }
            Log.d("OpNotificationController", "No heads up: blocked by brick mode: " + key);
            return 1;
        } else if (blockedByDrivingMode(statusBarNotification)) {
            if (!OP_DEBUG) {
                return 2;
            }
            Log.d("OpNotificationController", "No heads up: blocked by driving mode: " + key);
            return 2;
        } else if (blockedByEsportsMode(statusBarNotification)) {
            if (!OP_DEBUG) {
                return 3;
            }
            Log.d("OpNotificationController", "No heads up: blocked by esports mode: " + key);
            return 3;
        } else if (blockedByGameMode3rdPartyCall(statusBarNotification)) {
            if (!OP_DEBUG) {
                return 5;
            }
            Log.d("OpNotificationController", "No heads up: blocked by game mode 3rd party calling, key: " + key + " uid: " + this.mGameMode3rdPartyCallsUid);
            return 5;
        } else if (blockedByGameMode(statusBarNotification)) {
            if (!OP_DEBUG) {
                return 4;
            }
            Log.d("OpNotificationController", "No heads up: blocked by game mode: " + key);
            return 4;
        } else if (!blockedByReadingMode()) {
            return -1;
        } else {
            if (!OP_DEBUG) {
                return 6;
            }
            Log.d("OpNotificationController", "No heads up: blocked by reading mode: " + key);
            return 6;
        }
    }

    public boolean shouldSuppressFullScreenIntent(NotificationEntry notificationEntry) {
        if (blockedByBrickMode(notificationEntry.getSbn().getPackageName())) {
            if (OP_DEBUG) {
                Log.d("OpNotificationController", "No Fullscreen intent: suppressed by brick mode: " + notificationEntry.getKey());
            }
            return true;
        } else if (blockedByDrivingMode(notificationEntry.getSbn())) {
            if (OP_DEBUG) {
                Log.d("OpNotificationController", "No Fullscreen intent: suppressed by driving mode: " + notificationEntry.getKey());
            }
            return true;
        } else if (blockedByEsportsMode(notificationEntry.getSbn())) {
            if (OP_DEBUG) {
                Log.d("OpNotificationController", "No Fullscreen intent: suppressed by esports mode: " + notificationEntry.getKey());
            }
            return true;
        } else if (!blockedByGameMode(notificationEntry.getSbn())) {
            return false;
        } else {
            if (OP_DEBUG) {
                Log.d("OpNotificationController", "No Fullscreen intent: suppressed by game mode: " + notificationEntry.getKey());
            }
            return true;
        }
    }

    public boolean forceInversion(String str) {
        return FORCE_INVERTION_LIST.contains(str);
    }

    private boolean blockedByBrickMode(String str) {
        return this.mBlockedByBrick && !PRIORITY_LIST_BRICK_MODE.contains(str);
    }

    private boolean blockedByDrivingMode(StatusBarNotification statusBarNotification) {
        String packageName = statusBarNotification.getPackageName();
        if (!this.mBlockedByDriving) {
            return false;
        }
        if (PRIORITY_LIST_DRIVING_MODE.contains(packageName) && this.mIsKeyguardShowing) {
            return false;
        }
        Bundle bundle = statusBarNotification.getNotification().extras;
        if (bundle != null) {
            return !bundle.getBoolean("oneplus.shouldPeekInCarMode", false);
        }
        return true;
    }

    private boolean blockedByEsportsMode(StatusBarNotification statusBarNotification) {
        return OpFeatures.isSupport(new int[]{282}) ? this.mEsportsModeOn && this.mESportModeDndOn && OnePlusSceneCallBlockManagerInjector.isNotificationMutedByESport(statusBarNotification) : this.mEsportsModeOn && OnePlusSceneCallBlockManagerInjector.isNotificationMutedByESport(statusBarNotification);
    }

    private boolean blockedByGameMode(StatusBarNotification statusBarNotification) {
        if (PRIORITY_LIST_GAME_MODE.contains(statusBarNotification.getPackageName()) || "call".equals(statusBarNotification.getNotification().category)) {
            return false;
        }
        if (OP_DEBUG) {
            Log.d("OpNotificationController", "mBlockedByGame: " + this.mBlockedByGame + " type: " + this.mGameModeNotifyType);
        }
        if (!this.mBlockedByGame || this.mGameModeNotifyType == 0) {
            return false;
        }
        Bundle bundle = statusBarNotification.getNotification().extras;
        if (bundle != null) {
            return !bundle.getBoolean("oneplus.shouldPeekInGameMode", false);
        }
        return true;
    }

    private boolean blockedByGameMode3rdPartyCall(StatusBarNotification statusBarNotification) {
        String str = this.mGameMode3rdPartyCallsUid;
        return str != null && !"-1".equals(str) && !PRIORITY_LIST_GAME_MODE.contains(statusBarNotification.getPackageName());
    }

    private boolean blockedByReadingMode() {
        int intForUser = Settings.System.getIntForUser(this.mContext.getContentResolver(), "reading_mode_status", 0, -2);
        return (intForUser == 1 || intForUser == 2) && Settings.System.getIntForUser(this.mContext.getContentResolver(), "reading_mode_block_notification", 0, -2) == 1;
    }

    public boolean shouldColorizeIcon(String str) {
        return ICON_COLORIZE_LIST.contains(str);
    }

    public boolean shouldForceRemoveEntry(String str) {
        return LIFETIME_EXTENSION_LIST.contains(str);
    }

    public boolean isPanelDisabledInBrickMode() {
        return this.mBlockedByBrick && !this.mCommandQueue.panelsEnabled();
    }

    public void maybeShowSimpleHeadsUp(int i, StatusBarNotification statusBarNotification) {
        int i2;
        String packageName = statusBarNotification.getPackageName();
        if (i == 4 && this.mBlockedByGame && this.mGameModeNotifyType == 2) {
            if (OP_DEBUG) {
                Log.d("OpNotificationController", "Show simple heads-up: game mode: " + packageName);
            }
            i2 = 0;
        } else {
            i2 = -1;
        }
        showSimpleHeadsUp(i2, statusBarNotification);
    }

    private void showSimpleHeadsUp(int i, StatusBarNotification statusBarNotification) {
        String key = statusBarNotification.getKey();
        if (i != -1) {
            this.mSimpleHeadsUpController.show(statusBarNotification, this.mLockscreenUserManager.isSecure() && this.mEntryManager.isLocked(key), 1);
        } else if (this.mSimpleHeadsUpController.getCurrentKey() != null && this.mSimpleHeadsUpController.getCurrentKey().equals(key)) {
            hideSimpleHeadsUps();
        }
    }

    public void hideSimpleHeadsUps() {
        SimpleHeadsUpController simpleHeadsUpController = this.mSimpleHeadsUpController;
        if (simpleHeadsUpController != null) {
            simpleHeadsUpController.hide();
        }
    }

    public void snoozeHeadsUp(Notification notification) {
        PendingIntent swipeUpHeadsUpIntent = notification.getSwipeUpHeadsUpIntent();
        if (swipeUpHeadsUpIntent != null) {
            try {
                if (OP_DEBUG) {
                    Log.d("OpNotificationController", "snooze " + notification + " send pending intent " + swipeUpHeadsUpIntent);
                }
                swipeUpHeadsUpIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    public View getQuickReplyView(StatusBarNotification statusBarNotification) {
        View inflate = LayoutInflater.from(this.mContext).inflate(C0011R$layout.op_quick_reply_notification, (ViewGroup) null);
        Notification notification = statusBarNotification.getNotification();
        ImageView imageView = (ImageView) inflate.findViewById(C0008R$id.app_icon);
        if (imageView != null) {
            imageView.setImageBitmap(getAppIcon(statusBarNotification));
        }
        TextView textView = (TextView) inflate.findViewById(C0008R$id.title);
        if (textView != null) {
            textView.setText(notification.extras.getCharSequence("android.title"));
            textView.setTextColor(Utils.getColorAttr(this.mContext, 16842806));
        }
        TextView textView2 = (TextView) inflate.findViewById(C0008R$id.text);
        if (textView2 != null) {
            textView2.setText(notification.extras.getCharSequence("android.text"));
            textView2.setTextColor(Utils.getColorAttr(this.mContext, 16842808));
        }
        View findViewById = inflate.findViewById(C0008R$id.notification_content);
        if (findViewById != null) {
            findViewById.setOnClickListener(new View.OnClickListener(statusBarNotification) { // from class: com.oneplus.notification.-$$Lambda$OpNotificationController$jDTWB-lrFm2kPlAyY55_hDtaE7g
                public final /* synthetic */ StatusBarNotification f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    OpNotificationController.this.lambda$getQuickReplyView$0$OpNotificationController(this.f$1, view);
                }
            });
        }
        View findViewById2 = inflate.findViewById(C0008R$id.btn_reply);
        if (findViewById2 != null) {
            findViewById2.setOnClickListener(new View.OnClickListener(statusBarNotification) { // from class: com.oneplus.notification.-$$Lambda$OpNotificationController$e06XjAOoiFk5LvjE_IIxg3Asym0
                public final /* synthetic */ StatusBarNotification f$1;

                {
                    this.f$1 = r2;
                }

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    OpNotificationController.this.lambda$getQuickReplyView$1$OpNotificationController(this.f$1, view);
                }
            });
        }
        return inflate;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getQuickReplyView$0 */
    public /* synthetic */ void lambda$getQuickReplyView$0$OpNotificationController(StatusBarNotification statusBarNotification, View view) {
        OpMdmLogger.log("landscape_quick_reply", "hun_action", "2", "YLTI9SVG4L");
        sentIntent(statusBarNotification, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getQuickReplyView$1 */
    public /* synthetic */ void lambda$getQuickReplyView$1$OpNotificationController(StatusBarNotification statusBarNotification, View view) {
        if (OpLsState.getInstance().getPhoneStatusBar().getPresenter().isPresenterFullyCollapsed()) {
            OpMdmLogger.log("landscape_quick_reply", "hun_action", "1", "YLTI9SVG4L");
        } else {
            OpMdmLogger.log("landscape_quick_reply", "nd_action", "1", "YLTI9SVG4L");
        }
        sentIntent(statusBarNotification, true);
    }

    private void sentIntent(StatusBarNotification statusBarNotification, boolean z) {
        Log.d("OpNotificationController", "launch pkg: " + statusBarNotification.getPackageName() + " userId: " + statusBarNotification.getUserId() + " in freeform mode: " + z);
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        Notification notification = statusBarNotification.getNotification();
        PendingIntent pendingIntent = notification.contentIntent;
        if (pendingIntent == null) {
            pendingIntent = notification.fullScreenIntent;
        }
        if (pendingIntent != null) {
            try {
                ActivityOptions activityOptionsInternal = OpStatusBar.getActivityOptionsInternal(null);
                if (z) {
                    activityOptionsInternal.setLaunchWindowingMode(5);
                }
                Bundle bundle = activityOptionsInternal.toBundle();
                if (OpFeatures.isSupport(new int[]{176})) {
                    bundle.putString("android:activity.packageName", "OP_EXTRA_REMOTE_INPUT_DRAFT");
                }
                int sendAndReturnResult = pendingIntent.sendAndReturnResult(this.mContext, 0, null, null, null, null, bundle);
                ActivityLaunchAnimator activityLaunchAnimator = phoneStatusBar.getActivityLaunchAnimator();
                if (activityLaunchAnimator != null) {
                    activityLaunchAnimator.setLaunchResult(sendAndReturnResult, true);
                }
                if (phoneStatusBar.getPresenter().isPresenterFullyCollapsed()) {
                    phoneStatusBar.removeHeadsUps();
                }
            } catch (PendingIntent.CanceledException e) {
                Log.w("OpNotificationController", "Sending PendingIntent failed: " + e);
            }
        }
    }

    private Bitmap getAppIcon(StatusBarNotification statusBarNotification) {
        Drawable drawable;
        String packageName = statusBarNotification.getPackageName();
        try {
            drawable = this.mContext.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("OpNotificationController", "Quick Reply: Get package fail, " + e.toString());
            drawable = statusBarNotification.getNotification().getSmallIcon().loadDrawable(this.mContext);
        }
        if (drawable != null) {
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            Bitmap createBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, drawable.getOpacity() != -1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(createBitmap);
            drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
            drawable.draw(canvas);
            return createBitmap;
        }
        Log.d("OpNotificationController", "Quick Reply: Cannot resolve application icon, pkg: " + packageName);
        return null;
    }

    public boolean supportQuickReply(String str) {
        return !this.mDockedStackExists && isQuickReplyApp(str) && this.mOrientation == 2;
    }

    public boolean isFreeFormActive() {
        return this.mIsFreeForm;
    }

    public boolean isQuickReplyApp(String str) {
        return this.mQuickReplyList.contains(str);
    }

    public void setIsFreeForm(boolean z) {
        if (!OpUtils.QUICK_REPLY_BUBBLE) {
            this.mIsFreeForm = z;
        }
    }

    private void setShowQuickReply(NotificationEntry notificationEntry) {
        String packageName = notificationEntry.getSbn().getPackageName();
        boolean z = true;
        boolean z2 = (notificationEntry.getSbn().getNotification().flags & 64) != 0;
        String str = this.mTopActivity;
        boolean z3 = (str == null || packageName == null || !packageName.equals(str)) ? false : true;
        if (!supportQuickReply(packageName) || this.mIsFreeForm || z3 || z2 || notificationEntry.getRow().isContentHidden()) {
            z = false;
        }
        notificationEntry.getRow().setShowQuickReply(z);
    }

    public void setTopActivity(String str) {
        this.mTopActivity = str;
    }

    public void setQuickReplyFlags() {
        if (getEntryManager() != null) {
            Iterator<NotificationEntry> it = getEntryManager().getSortedAndFiltered().iterator();
            while (it.hasNext()) {
                NotificationEntry next = it.next();
                next.getRow().reinflateQuickReply(isQuickReplyApp(next.getSbn().getPackageName()));
                setShowQuickReply(next);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void preparePrivacyAlertNotification(int i, String str, boolean z) {
        String str2;
        String str3;
        String str4;
        int i2;
        String str5;
        int i3;
        String appName = getAppName(str);
        if (appName == null) {
            if (OP_DEBUG) {
                Log.d("OpNotificationController", "appName is null, use packageName (" + str + ") instead");
            }
            appName = str;
        }
        int i4 = 2;
        boolean z2 = false;
        if (i == 26) {
            str5 = this.mContext.getString(C0015R$string.privacy_type_camera);
            int i5 = C0006R$drawable.privacy_alert_icon_camera;
            String string = this.mContext.getString(C0015R$string.privacy_alert_content_title, appName, str5);
            i2 = i5;
            str3 = this.mContext.getString(C0015R$string.privacy_alert_content_text);
            i4 = 1;
            str2 = "PrivacyAlertGroupCamera";
            str4 = string;
        } else if (i == 27) {
            str5 = this.mContext.getString(C0015R$string.privacy_type_microphone);
            int i6 = C0006R$drawable.privacy_alert_icon_microphone;
            String string2 = this.mContext.getString(C0015R$string.privacy_alert_content_title, appName, str5);
            str3 = this.mContext.getString(C0015R$string.privacy_alert_content_text);
            str2 = "PrivacyAlertGroupMicrophone";
            str4 = string2;
            i2 = i6;
        } else if (OP_DEBUG) {
            Log.d("OpNotificationController", "Wrong appOp value: " + i + ", cancel notification");
            return;
        } else {
            return;
        }
        if (!isSystemApp(str) && !SYSTEM_APP_LIST.contains(str)) {
            int intValue = this.mPrivacyGroupCount.get(str2).intValue();
            int intValue2 = this.mPrivacyAlertList.containsKey(str) ? this.mPrivacyAlertList.get(str).intValue() : 0;
            if ((intValue2 & i4) == 1) {
                z2 = true;
            }
            if (!z || z2) {
                i3 = intValue2 & (~i4);
                if (intValue > 0) {
                    this.mPrivacyGroupCount.put(str2, Integer.valueOf(intValue - 1));
                }
            } else {
                i3 = intValue2 | i4;
                sendPrivacyAlertNotification(str, i4, str2, i2, str4, str3);
                this.mPrivacyGroupCount.put(str2, Integer.valueOf(intValue + 1));
            }
            this.mPrivacyAlertList.put(str, Integer.valueOf(i3));
        } else if (OP_DEBUG) {
            Log.d("OpNotificationController", "preparePrivacyAlertNotification, stop preparing for system application, packageName: " + str + ", privacyName: " + str5 + ", privacyType: " + i4);
        }
    }

    private void sendPrivacyAlertNotification(String str, int i, String str2, int i2, String str3, String str4) {
        if (OP_DEBUG) {
            Log.d("OpNotificationController", "sendPrivacyAlertNotification, packageName: " + str + ", privacyType: " + i + ", group: " + str2);
        }
        if (this.mPrivacyGroupCount.get(str2).intValue() == 0) {
            Notification build = new Notification.Builder(this.mContext, "privacy_alert").setDefaults(-1).setGroup(str2).setGroupSummary(true).setSmallIcon(i2).setOngoing(true).setVisibility(1).setContentIntent(PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS").setFlags(268435456).putExtra("android.provider.extra.CHANNEL_ID", "privacy_alert"), 134217728)).build();
            build.extras.putString("android.substName", this.mContext.getString(C0015R$string.privacy_alert_application_name));
            this.mNoMan.notifyAsUser("PrivacyAlertSummary", i, build, UserHandle.ALL);
        }
        Notification build2 = new Notification.Builder(this.mContext, "privacy_alert").setDefaults(-1).setGroup(str2).setSmallIcon(i2).setContentTitle(str3).setContentText(str4).setOngoing(true).setVisibility(1).setContentIntent(PendingIntent.getActivity(this.mContext, 0, new Intent("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", str, null)).setFlags(268435456).putExtra("android.provider.extra.CHANNEL_ID", "privacy_alert"), 134217728)).build();
        build2.extras.putString("android.substName", this.mContext.getString(C0015R$string.privacy_alert_application_name));
        this.mNoMan.notifyAsUser(str, i, build2, UserHandle.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelPrivacyAlertIfNeeded() {
        if (OP_DEBUG) {
            Log.d("OpNotificationController", "cancelPrivacyAlertIfNeeded, size of mPrivacyAlertList: " + this.mPrivacyAlertList.size() + ", mPrivacyGroupCount(Camera): " + this.mPrivacyGroupCount.get("PrivacyAlertGroupCamera") + ", mPrivacyGroupCount(Microphone): " + this.mPrivacyGroupCount.get("PrivacyAlertGroupMicrophone"));
        }
        Iterator<Map.Entry<String, Integer>> it = this.mPrivacyAlertList.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> next = it.next();
            if (next != null) {
                String key = next.getKey();
                int intValue = next.getValue().intValue();
                if ((intValue & 1) == 0) {
                    cancelPrivacyAlert(key, 1);
                }
                if ((intValue & 2) == 0) {
                    cancelPrivacyAlert(key, 2);
                }
                if (intValue == 0 && this.mPrivacyAlertList.containsKey(key)) {
                    it.remove();
                }
            }
        }
        if (this.mPrivacyGroupCount.get("PrivacyAlertGroupCamera").intValue() == 0) {
            cancelPrivacyAlert("PrivacyAlertSummary", 1);
        }
        if (this.mPrivacyGroupCount.get("PrivacyAlertGroupMicrophone").intValue() == 0) {
            cancelPrivacyAlert("PrivacyAlertSummary", 2);
        }
    }

    private void cancelPrivacyAlert(String str, int i) {
        if (OP_DEBUG) {
            Log.d("OpNotificationController", "cancelPrivacyAlert, tag: " + str + ", privacyType: " + i);
        }
        this.mNoMan.cancelAsUser(str, i, UserHandle.ALL);
    }

    private boolean isSystemApp(String str) {
        try {
            return this.mPackageManager.getPackageInfo(str, 64).applicationInfo.isSystemApp();
        } catch (PackageManager.NameNotFoundException unused) {
            Log.e("OpNotificationController", "cacheIsSystemNotification: Could not find package info");
            return false;
        }
    }

    private String getAppName(String str) {
        try {
            String charSequence = this.mPackageManager.getApplicationLabel(this.mPackageManager.getApplicationInfo(str, 0)).toString();
            return charSequence != null ? charSequence : "";
        } catch (PackageManager.NameNotFoundException unused) {
            Log.e("OpNotificationController", "Failed to find app name for " + str);
            return null;
        }
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver contentResolver = OpNotificationController.this.mContext.getContentResolver();
            contentResolver.registerContentObserver(OpNotificationController.OP_QUICKREPLY_IM_LIST_URI, false, this, -1);
            contentResolver.registerContentObserver(OpNotificationController.DRIVING_MODE_STATE_URI, false, this, -1);
            contentResolver.registerContentObserver(OpNotificationController.ESPORTS_MODE_ENABLED, false, this, -1);
            if (OpFeatures.isSupport(new int[]{282})) {
                contentResolver.registerContentObserver(OpNotificationController.ESPORT_MODE_DO_NOT_DISTURB_ENABLED, false, this, -1);
            }
            contentResolver.registerContentObserver(OpNotificationController.GAME_MODE_BLOCK_HEADS_UP_URI, false, this, -1);
            contentResolver.registerContentObserver(OpNotificationController.GAME_MODE_3RD_PARTY_CALLS_UID_URI, false, this, -1);
            update(null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            ContentResolver contentResolver = OpNotificationController.this.mContext.getContentResolver();
            boolean z = true;
            if (uri == null || OpNotificationController.DRIVING_MODE_STATE_URI.equals(uri)) {
                OpNotificationController.this.mBlockedByDriving = Settings.Secure.getIntForUser(contentResolver, "driving_mode_state", 0, -2) != 0;
            }
            if (uri == null || OpNotificationController.ESPORTS_MODE_ENABLED.equals(uri)) {
                OpNotificationController.this.mEsportsModeOn = Settings.System.getIntForUser(contentResolver, "esport_mode_enabled", 0, -2) == 1;
            }
            if (OpFeatures.isSupport(new int[]{282}) && (uri == null || OpNotificationController.ESPORT_MODE_DO_NOT_DISTURB_ENABLED.equals(uri))) {
                OpNotificationController opNotificationController = OpNotificationController.this;
                if (Settings.System.getIntForUser(contentResolver, "key_do_not_disturb_enabled", 0, -2) != 1) {
                    z = false;
                }
                opNotificationController.mESportModeDndOn = z;
            }
            if (uri == null || OpNotificationController.GAME_MODE_BLOCK_HEADS_UP_URI.equals(uri)) {
                OpNotificationController.this.mGameModeNotifyType = Settings.System.getIntForUser(contentResolver, "game_mode_block_notification", 0, -2);
                if (OpNotificationController.this.mGameModeNotifyType != 0) {
                    OpNotificationController.this.mOIMCServiceManager.addFuncRuleGlobal(OIMCRule.RULE_DISABLE_HEADSUPNOTIFICATION);
                } else {
                    OpNotificationController.this.mOIMCServiceManager.removeFuncRuleGlobal(OIMCRule.RULE_DISABLE_HEADSUPNOTIFICATION);
                }
            }
            if (uri == null || OpNotificationController.GAME_MODE_3RD_PARTY_CALLS_UID_URI.equals(uri)) {
                OpNotificationController.this.mGameMode3rdPartyCallsUid = Settings.System.getStringForUser(contentResolver, "game_mode_notifications_3rd_calls_uid", -2);
                if (OpNotificationController.this.mGameMode3rdPartyCallsUid != null && !"-1".equals(OpNotificationController.this.mGameMode3rdPartyCallsUid)) {
                    OpLsState.getInstance().getPhoneStatusBar().removeHeadsUps();
                    if (OpNotificationController.OP_DEBUG) {
                        Log.d("OpNotificationController", "removeHeadsUps for 3rd party app calling uid: " + OpNotificationController.this.mGameMode3rdPartyCallsUid);
                    }
                }
            }
            if (uri == null || OpNotificationController.OP_QUICKREPLY_IM_LIST_URI.equals(uri)) {
                String stringForUser = Settings.System.getStringForUser(contentResolver, "op_quickreply_im_list", -2);
                if (stringForUser != null && !stringForUser.isEmpty()) {
                    OpNotificationController.this.mQuickReplyList = Arrays.asList(stringForUser.split(";"));
                    OpNotificationController.this.setQuickReplyFlags();
                }
                if (OpNotificationController.OP_DEBUG) {
                    Log.d("OpNotificationController", "list= " + stringForUser);
                }
            }
            if (uri == null) {
                OpNotificationController.this.mOIMCServiceManager.addFuncRuleGlobal(OIMCRule.RULE_DISABLE_HEADSUPNOTIFICATION_ZEN);
            }
            if (OpNotificationController.OP_DEBUG) {
                Log.d("OpNotificationController", "update uri: " + uri + " mBlockedByDriving: " + OpNotificationController.this.mBlockedByDriving + " mEsportsModeOn: " + OpNotificationController.this.mEsportsModeOn + " mESportModeDndOn: " + OpNotificationController.this.mESportModeDndOn + " mGameModeNotifyType: " + OpNotificationController.this.mGameModeNotifyType + " mGameMode3rdPartyCallsUid: " + OpNotificationController.this.mGameMode3rdPartyCallsUid);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class OimcObserver extends ContentObserver {
        OimcObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver contentResolver = OpNotificationController.this.mContext.getContentResolver();
            contentResolver.registerContentObserver(OpNotificationController.OP_OIMC_FUNC_DISABLE_HEADSUP_URI, false, this, -1);
            contentResolver.registerContentObserver(OpNotificationController.OP_OIMC_FUNC_DISABLE_HEADSUP_BRICK_URI, false, this, -1);
            update(null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            boolean z = false;
            if (uri == null || OpNotificationController.OP_OIMC_FUNC_DISABLE_HEADSUP_BRICK_URI.equals(uri)) {
                OpNotificationController.this.mBlockedByBrick = OpNotificationController.this.mOIMCServiceManager.getRemoteFuncStatus("HeadsUpNotificationZen") == 1;
            }
            if (uri == null || OpNotificationController.OP_OIMC_FUNC_DISABLE_HEADSUP_URI.equals(uri)) {
                int remoteFuncStatus = OpNotificationController.this.mOIMCServiceManager.getRemoteFuncStatus("HeadsUpNotification");
                OpNotificationController opNotificationController = OpNotificationController.this;
                if (remoteFuncStatus == 1) {
                    z = true;
                }
                opNotificationController.mBlockedByGame = z;
            }
            if (OpNotificationController.OP_DEBUG) {
                Log.d("OpNotificationController", "OIMC update uri: " + uri + " mBlockedByBrick: " + OpNotificationController.this.mBlockedByBrick + " mBlockedByGame: " + OpNotificationController.this.mBlockedByGame);
            }
        }
    }

    public void updateNotificationRule() {
        SettingsObserver settingsObserver = this.mSettingsObserver;
        if (settingsObserver != null) {
            settingsObserver.update(null);
        }
        OimcObserver oimcObserver = this.mOimcObserver;
        if (oimcObserver != null) {
            oimcObserver.update(null);
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
            hideSimpleHeadsUps();
            setQuickReplyFlags();
        }
    }
}
