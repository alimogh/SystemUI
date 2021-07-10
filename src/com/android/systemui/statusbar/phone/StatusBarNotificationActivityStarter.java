package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.ContactsContract;
import android.service.dreams.IDreamManager;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.Dependency;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationClickNotifier;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.DismissedByUserStats;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.policy.HeadsUpUtil;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.oneplus.notification.OpNotificationController;
import com.oneplus.plugin.OpLsState;
import com.oneplus.scene.OpSceneModeObserver;
import com.oneplus.systemui.util.OpMdmLogger;
import dagger.Lazy;
import java.util.Objects;
import java.util.concurrent.Executor;
public class StatusBarNotificationActivityStarter implements NotificationActivityStarter {
    private final ActivityIntentHelper mActivityIntentHelper;
    private final ActivityLaunchAnimator mActivityLaunchAnimator;
    private final ActivityStarter mActivityStarter;
    private final Lazy<AssistManager> mAssistManagerLazy;
    private final Handler mBackgroundHandler;
    private final BubbleController mBubbleController;
    private final NotificationClickNotifier mClickNotifier;
    private final CommandQueue mCommandQueue;
    private final Context mContext;
    private final IDreamManager mDreamManager;
    private final NotificationEntryManager mEntryManager;
    private final FeatureFlags mFeatureFlags;
    private final NotificationGroupManager mGroupManager;
    private final HeadsUpManagerPhone mHeadsUpManager;
    private boolean mIsCollapsingToShowActivityOverLockscreen;
    private boolean mIsContactsChanged;
    private final KeyguardManager mKeyguardManager;
    private final KeyguardStateController mKeyguardStateController;
    private final LockPatternUtils mLockPatternUtils;
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    private final StatusBarNotificationActivityStarterLogger mLogger;
    private final Handler mMainThreadHandler;
    private final MetricsLogger mMetricsLogger;
    private final NotifCollection mNotifCollection;
    private final NotifPipeline mNotifPipeline;
    private final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
    private final NotificationPanelViewController mNotificationPanel;
    private ContentObserver mObserver;
    private final OpNotificationController mOpNotificationController;
    private final NotificationPresenter mPresenter;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private final ShadeController mShadeController;
    private final StatusBar mStatusBar;
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final StatusBarRemoteInputCallback mStatusBarRemoteInputCallback;
    private final StatusBarStateController mStatusBarStateController;
    private final Executor mUiBgExecutor;

    static {
        boolean z = Build.DEBUG_ONEPLUS;
    }

    private StatusBarNotificationActivityStarter(Context context, CommandQueue commandQueue, Handler handler, Handler handler2, Executor executor, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, NotifCollection notifCollection, HeadsUpManagerPhone headsUpManagerPhone, ActivityStarter activityStarter, NotificationClickNotifier notificationClickNotifier, StatusBarStateController statusBarStateController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, KeyguardManager keyguardManager, IDreamManager iDreamManager, BubbleController bubbleController, Lazy<AssistManager> lazy, NotificationRemoteInputManager notificationRemoteInputManager, NotificationGroupManager notificationGroupManager, NotificationLockscreenUserManager notificationLockscreenUserManager, ShadeController shadeController, KeyguardStateController keyguardStateController, NotificationInterruptStateProvider notificationInterruptStateProvider, LockPatternUtils lockPatternUtils, StatusBarRemoteInputCallback statusBarRemoteInputCallback, ActivityIntentHelper activityIntentHelper, FeatureFlags featureFlags, MetricsLogger metricsLogger, StatusBarNotificationActivityStarterLogger statusBarNotificationActivityStarterLogger, StatusBar statusBar, NotificationPresenter notificationPresenter, NotificationPanelViewController notificationPanelViewController, ActivityLaunchAnimator activityLaunchAnimator) {
        this.mOpNotificationController = (OpNotificationController) Dependency.get(OpNotificationController.class);
        this.mContext = context;
        this.mCommandQueue = commandQueue;
        this.mMainThreadHandler = handler;
        this.mBackgroundHandler = handler2;
        this.mUiBgExecutor = executor;
        this.mEntryManager = notificationEntryManager;
        this.mNotifPipeline = notifPipeline;
        this.mNotifCollection = notifCollection;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mActivityStarter = activityStarter;
        this.mClickNotifier = notificationClickNotifier;
        this.mStatusBarStateController = statusBarStateController;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        this.mKeyguardManager = keyguardManager;
        this.mDreamManager = iDreamManager;
        this.mBubbleController = bubbleController;
        this.mAssistManagerLazy = lazy;
        this.mRemoteInputManager = notificationRemoteInputManager;
        this.mGroupManager = notificationGroupManager;
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mShadeController = shadeController;
        this.mKeyguardStateController = keyguardStateController;
        this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
        this.mLockPatternUtils = lockPatternUtils;
        this.mStatusBarRemoteInputCallback = statusBarRemoteInputCallback;
        this.mActivityIntentHelper = activityIntentHelper;
        this.mFeatureFlags = featureFlags;
        this.mMetricsLogger = metricsLogger;
        this.mLogger = statusBarNotificationActivityStarterLogger;
        this.mStatusBar = statusBar;
        this.mPresenter = notificationPresenter;
        this.mNotificationPanel = notificationPanelViewController;
        this.mActivityLaunchAnimator = activityLaunchAnimator;
        if (!featureFlags.isNewNotifPipelineRenderingEnabled()) {
            this.mEntryManager.addNotificationEntryListener(new NotificationEntryListener() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.1
                @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
                public void onPendingEntryAdded(final NotificationEntry notificationEntry) {
                    boolean z = true;
                    boolean z2 = ((ZenModeController) Dependency.get(ZenModeController.class)).getZen() != 0;
                    PendingIntent pendingIntent = notificationEntry.getSbn().getNotification().fullScreenIntent;
                    String str = null;
                    if (!(pendingIntent == null || pendingIntent.getIntent() == null || pendingIntent.getIntent().getComponent() == null)) {
                        str = pendingIntent.getIntent().getComponent().getShortClassName();
                    }
                    if (!(str != null && str.contains("InCallActivity")) || !z2 || !StatusBarNotificationActivityStarter.this.mIsContactsChanged) {
                        z = false;
                    }
                    if (z) {
                        StatusBarNotificationActivityStarter.this.mMainThreadHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                StatusBarNotificationActivityStarter.this.handleFullScreenIntent(notificationEntry);
                            }
                        }, 1100);
                        StatusBarNotificationActivityStarter.this.mIsContactsChanged = false;
                        return;
                    }
                    StatusBarNotificationActivityStarter.this.handleFullScreenIntent(notificationEntry);
                }
            });
        } else {
            this.mNotifPipeline.addCollectionListener(new NotifCollectionListener() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.2
                @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
                public void onEntryAdded(NotificationEntry notificationEntry) {
                    StatusBarNotificationActivityStarter.this.handleFullScreenIntent(notificationEntry);
                }
            });
        }
        this.mObserver = new ContentObserver(this.mBackgroundHandler) { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.3
            @Override // android.database.ContentObserver
            public void onChange(boolean z, Uri uri, int i) {
                StatusBarNotificationActivityStarter.this.mIsContactsChanged = true;
            }
        };
        this.mContext.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, this.mObserver, -1);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
    public void onNotificationClicked(StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow) {
        PendingIntent pendingIntent;
        this.mLogger.logStartingActivityFromClick(statusBarNotification.getKey());
        OpMdmLogger.logQsPanel("click_notif");
        RemoteInputController controller = this.mRemoteInputManager.getController();
        if (!controller.isRemoteInputActive(expandableNotificationRow.getEntry()) || TextUtils.isEmpty(expandableNotificationRow.getActiveRemoteInputText())) {
            Notification notification = statusBarNotification.getNotification();
            PendingIntent pendingIntent2 = notification.contentIntent;
            if (pendingIntent2 != null) {
                pendingIntent = pendingIntent2;
            } else {
                pendingIntent = notification.fullScreenIntent;
            }
            boolean isBubble = expandableNotificationRow.getEntry().isBubble();
            if (pendingIntent != null || isBubble) {
                boolean z = pendingIntent != null && pendingIntent.isActivity() && !isBubble;
                boolean z2 = z && this.mActivityIntentHelper.wouldLaunchResolverActivity(pendingIntent.getIntent(), this.mLockscreenUserManager.getCurrentUserId());
                boolean isOccluded = this.mStatusBar.isOccluded();
                boolean z3 = (this.mKeyguardStateController.isShowing() && pendingIntent != null && this.mActivityIntentHelper.wouldShowOverLockscreen(pendingIntent.getIntent(), this.mLockscreenUserManager.getCurrentUserId())) || ((OpSceneModeObserver) Dependency.get(OpSceneModeObserver.class)).isInBrickMode();
                $$Lambda$StatusBarNotificationActivityStarter$Pyeef5xkti2nTtS5zKZgWAnZicA r12 = new ActivityStarter.OnDismissAction(statusBarNotification, expandableNotificationRow, controller, pendingIntent, z, isOccluded, z3) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$Pyeef5xkti2nTtS5zKZgWAnZicA
                    public final /* synthetic */ StatusBarNotification f$1;
                    public final /* synthetic */ ExpandableNotificationRow f$2;
                    public final /* synthetic */ RemoteInputController f$3;
                    public final /* synthetic */ PendingIntent f$4;
                    public final /* synthetic */ boolean f$5;
                    public final /* synthetic */ boolean f$6;
                    public final /* synthetic */ boolean f$7;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                        this.f$5 = r6;
                        this.f$6 = r7;
                        this.f$7 = r8;
                    }

                    @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
                    public final boolean onDismiss() {
                        return StatusBarNotificationActivityStarter.this.lambda$onNotificationClicked$0$StatusBarNotificationActivityStarter(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
                    }
                };
                if (z3) {
                    this.mIsCollapsingToShowActivityOverLockscreen = true;
                    r12.onDismiss();
                    return;
                }
                this.mActivityStarter.dismissKeyguardThenExecute(r12, null, z2);
                return;
            }
            this.mLogger.logNonClickableNotification(statusBarNotification.getKey());
            return;
        }
        controller.closeRemoteInputs();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x002b, code lost:
        if (shouldAutoCancel(r0.getSbn()) != false) goto L_0x002f;
     */
    /* renamed from: handleNotificationClickAfterKeyguardDismissed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean lambda$onNotificationClicked$0(android.service.notification.StatusBarNotification r12, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r13, com.android.systemui.statusbar.RemoteInputController r14, android.app.PendingIntent r15, boolean r16, boolean r17, boolean r18) {
        /*
            r11 = this;
            r9 = r11
            r2 = r12
            com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger r0 = r9.mLogger
            java.lang.String r1 = r12.getKey()
            r0.logHandleClickAfterKeyguardDismissed(r1)
            r3 = r13
            r11.removeHUN(r13)
            boolean r0 = shouldAutoCancel(r12)
            if (r0 == 0) goto L_0x002e
            com.android.systemui.statusbar.phone.NotificationGroupManager r0 = r9.mGroupManager
            boolean r0 = r0.isOnlyChildInGroup(r12)
            if (r0 == 0) goto L_0x002e
            com.android.systemui.statusbar.phone.NotificationGroupManager r0 = r9.mGroupManager
            com.android.systemui.statusbar.notification.collection.NotificationEntry r0 = r0.getLogicalGroupSummary(r12)
            android.service.notification.StatusBarNotification r1 = r0.getSbn()
            boolean r1 = shouldAutoCancel(r1)
            if (r1 == 0) goto L_0x002e
            goto L_0x002f
        L_0x002e:
            r0 = 0
        L_0x002f:
            r8 = r0
            com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$7mfSGy2G6exE-3cGRoA3iww8GIU r10 = new com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$7mfSGy2G6exE-3cGRoA3iww8GIU
            r0 = r10
            r1 = r11
            r2 = r12
            r3 = r13
            r4 = r14
            r5 = r15
            r6 = r16
            r7 = r17
            r0.<init>(r2, r3, r4, r5, r6, r7, r8)
            r0 = 1
            if (r18 == 0) goto L_0x004d
            com.android.systemui.statusbar.phone.ShadeController r1 = r9.mShadeController
            r1.addPostCollapseAction(r10)
            com.android.systemui.statusbar.phone.ShadeController r1 = r9.mShadeController
            r1.collapsePanel(r0)
            goto L_0x006d
        L_0x004d:
            com.android.systemui.statusbar.policy.KeyguardStateController r1 = r9.mKeyguardStateController
            boolean r1 = r1.isShowing()
            if (r1 == 0) goto L_0x0068
            com.android.systemui.statusbar.phone.StatusBar r1 = r9.mStatusBar
            boolean r1 = r1.isOccluded()
            if (r1 == 0) goto L_0x0068
            com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager r1 = r9.mStatusBarKeyguardViewManager
            r1.addAfterKeyguardGoneRunnable(r10)
            com.android.systemui.statusbar.phone.ShadeController r1 = r9.mShadeController
            r1.collapsePanel()
            goto L_0x006d
        L_0x0068:
            android.os.Handler r1 = r9.mBackgroundHandler
            r1.postAtFrontOfQueue(r10)
        L_0x006d:
            com.android.systemui.statusbar.phone.NotificationPanelViewController r1 = r9.mNotificationPanel
            boolean r1 = r1.isFullyCollapsed()
            r0 = r0 ^ r1
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.lambda$onNotificationClicked$0(android.service.notification.StatusBarNotification, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow, com.android.systemui.statusbar.RemoteInputController, android.app.PendingIntent, boolean, boolean, boolean):boolean");
    }

    /* access modifiers changed from: private */
    /* renamed from: handleNotificationClickAfterPanelCollapsed */
    public void lambda$handleNotificationClickAfterKeyguardDismissed$1(StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow, RemoteInputController remoteInputController, PendingIntent pendingIntent, boolean z, boolean z2, NotificationEntry notificationEntry) {
        this.mLogger.logHandleClickAfterPanelCollapsed(statusBarNotification.getKey());
        String key = statusBarNotification.getKey();
        try {
            ActivityManager.getService().resumeAppSwitches();
        } catch (RemoteException unused) {
        }
        if (z) {
            int identifier = pendingIntent.getCreatorUserHandle().getIdentifier();
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(identifier) && this.mKeyguardManager.isDeviceLocked(identifier) && this.mStatusBarRemoteInputCallback.startWorkChallengeIfNecessary(identifier, pendingIntent.getIntentSender(), key)) {
                collapseOnMainThread();
                return;
            }
        }
        NotificationEntry entry = expandableNotificationRow.getEntry();
        CharSequence charSequence = !TextUtils.isEmpty(entry.remoteInputText) ? entry.remoteInputText : null;
        Intent putExtra = (TextUtils.isEmpty(charSequence) || remoteInputController.isSpinning(key)) ? null : new Intent().putExtra("android.remoteInputDraft", charSequence.toString());
        boolean canBubble = entry.canBubble();
        if (canBubble) {
            this.mLogger.logExpandingBubble(key);
            expandBubbleStackOnMainThread(entry);
        } else {
            startNotificationIntent(pendingIntent, putExtra, entry, expandableNotificationRow, z2, z);
        }
        if (z || canBubble) {
            this.mAssistManagerLazy.get().hideAssist();
        }
        if (shouldCollapse()) {
            collapseOnMainThread();
        }
        this.mClickNotifier.onNotificationClick(key, NotificationVisibility.obtain(key, entry.getRanking().getRank(), getVisibleNotificationsCount(), true, NotificationLogger.getNotificationLocation(entry)));
        if (!canBubble) {
            if (notificationEntry != null) {
                removeNotification(notificationEntry);
            }
            if (shouldAutoCancel(statusBarNotification) || this.mRemoteInputManager.isNotificationKeptForRemoteInputHistory(key)) {
                removeNotification(expandableNotificationRow.getEntry());
            }
        }
        this.mIsCollapsingToShowActivityOverLockscreen = false;
    }

    private void expandBubbleStackOnMainThread(NotificationEntry notificationEntry) {
        if (Looper.getMainLooper().isCurrentThread()) {
            this.mBubbleController.expandStackAndSelectBubble(notificationEntry);
        } else {
            this.mMainThreadHandler.post(new Runnable(notificationEntry) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$_h_OdrtdsD1DAoz8Z6fGvw_e1JY
                public final /* synthetic */ NotificationEntry f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    StatusBarNotificationActivityStarter.this.lambda$expandBubbleStackOnMainThread$2$StatusBarNotificationActivityStarter(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$expandBubbleStackOnMainThread$2 */
    public /* synthetic */ void lambda$expandBubbleStackOnMainThread$2$StatusBarNotificationActivityStarter(NotificationEntry notificationEntry) {
        this.mBubbleController.expandStackAndSelectBubble(notificationEntry);
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0096  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startNotificationIntent(android.app.PendingIntent r10, android.content.Intent r11, com.android.systemui.statusbar.notification.collection.NotificationEntry r12, android.view.View r13, boolean r14, boolean r15) {
        /*
            r9 = this;
            com.android.systemui.statusbar.notification.ActivityLaunchAnimator r0 = r9.mActivityLaunchAnimator
            android.view.RemoteAnimationAdapter r13 = r0.getLaunchAnimation(r13, r14)
            r14 = 1
            int[] r0 = new int[r14]
            r1 = 176(0xb0, float:2.47E-43)
            r2 = 0
            r0[r2] = r1
            boolean r0 = android.util.OpFeatures.isSupport(r0)
            if (r0 == 0) goto L_0x0029
            com.android.systemui.statusbar.phone.ShadeController r12 = r9.mShadeController
            com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$l8oyMTDrKqyJOCogjUaq0Do3Ofw r13 = new com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$l8oyMTDrKqyJOCogjUaq0Do3Ofw
            r13.<init>(r11, r10)
            r12.addPostCollapseAction(r13)
            android.os.Handler r10 = r9.mMainThreadHandler
            com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$7XPsAjbNZ7TS9R5Cs6f8w6LzZYA r11 = new com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$7XPsAjbNZ7TS9R5Cs6f8w6LzZYA
            r11.<init>()
            r10.post(r11)
            return
        L_0x0029:
            com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger r0 = r9.mLogger
            java.lang.String r12 = r12.getKey()
            r0.logStartNotificationIntent(r12, r10)
            if (r13 == 0) goto L_0x003f
            android.app.IActivityTaskManager r12 = android.app.ActivityTaskManager.getService()
            java.lang.String r0 = r10.getCreatorPackage()
            r12.registerRemoteAnimationForNextActivityStart(r0, r13)
        L_0x003f:
            com.oneplus.notification.OpNotificationController r12 = r9.mOpNotificationController
            int r12 = r12.getCallState()
            java.lang.String r0 = "NotifActivityStarter"
            if (r12 == 0) goto L_0x008d
            com.android.systemui.statusbar.policy.KeyguardStateController r12 = r9.mKeyguardStateController     // Catch:{ CanceledException | RemoteException -> 0x00d5 }
            boolean r12 = r12.isShowing()     // Catch:{ CanceledException | RemoteException -> 0x00d5 }
            if (r12 == 0) goto L_0x008d
            java.lang.String r12 = ""
            android.content.Intent r1 = r10.getIntent()     // Catch:{ Exception -> 0x005f }
            android.content.ComponentName r1 = r1.getComponent()     // Catch:{ Exception -> 0x005f }
            java.lang.String r12 = r1.getShortClassName()     // Catch:{ Exception -> 0x005f }
        L_0x005f:
            if (r12 == 0) goto L_0x008d
            java.lang.String r1 = "InCallActivity"
            boolean r1 = r12.contains(r1)
            if (r1 == 0) goto L_0x008d
            android.content.Context r1 = r9.mContext
            android.content.ContentResolver r1 = r1.getContentResolver()
            java.lang.String r3 = "driving_mode_state"
            int r1 = android.provider.Settings.Secure.getInt(r1, r3, r2)
            if (r1 != r14) goto L_0x008d
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "skip cmp: "
            r1.append(r2)
            r1.append(r12)
            java.lang.String r12 = r1.toString()
            android.util.Log.d(r0, r12)
            goto L_0x008e
        L_0x008d:
            r14 = r2
        L_0x008e:
            if (r14 == 0) goto L_0x0096
            java.lang.String r10 = "not handle this intent in driving mode"
            android.util.Log.d(r0, r10)
            return
        L_0x0096:
            android.content.Context r2 = r9.mContext
            r3 = 0
            r5 = 0
            r6 = 0
            r7 = 0
            android.os.Bundle r8 = com.android.systemui.statusbar.phone.StatusBar.getActivityOptions(r13)
            r1 = r10
            r4 = r11
            int r11 = r1.sendAndReturnResult(r2, r3, r4, r5, r6, r7, r8)
            java.lang.StringBuilder r12 = new java.lang.StringBuilder
            r12.<init>()
            java.lang.String r13 = "intent sendAndReturnResult: "
            r12.append(r13)
            android.content.Intent r10 = r10.getIntent()
            android.content.ComponentName r10 = r10.getComponent()
            r12.append(r10)
            java.lang.String r10 = ", "
            r12.append(r10)
            r12.append(r11)
            java.lang.String r10 = r12.toString()
            android.util.Log.d(r0, r10)
            android.os.Handler r10 = r9.mMainThreadHandler
            com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$tRM6Dood5QZ-T_3WjygjcsNLtEk r12 = new com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$tRM6Dood5QZ-T_3WjygjcsNLtEk
            r12.<init>(r11, r15)
            r10.post(r12)
            goto L_0x00db
        L_0x00d5:
            r10 = move-exception
            com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger r9 = r9.mLogger
            r9.logSendingIntentFailed(r10)
        L_0x00db:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.startNotificationIntent(android.app.PendingIntent, android.content.Intent, com.android.systemui.statusbar.notification.collection.NotificationEntry, android.view.View, boolean, boolean):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationIntent$3 */
    public /* synthetic */ void lambda$startNotificationIntent$3$StatusBarNotificationActivityStarter(Intent intent, PendingIntent pendingIntent) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("android:activity.packageName", "OP_EXTRA_REMOTE_INPUT_DRAFT");
            pendingIntent.sendAndReturnResult(this.mContext, 0, intent, null, null, null, bundle);
            Log.d("NotifActivityStarter", "intent sendAndReturnResult: " + pendingIntent.getIntent().getComponent());
        } catch (PendingIntent.CanceledException e) {
            Log.e("NotifActivityStarter", "intent.sendAndReturnResult: ERROR!");
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationIntent$4 */
    public /* synthetic */ void lambda$startNotificationIntent$4$StatusBarNotificationActivityStarter() {
        this.mActivityLaunchAnimator.setLaunchResult(0, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationIntent$5 */
    public /* synthetic */ void lambda$startNotificationIntent$5$StatusBarNotificationActivityStarter(int i, boolean z) {
        this.mActivityLaunchAnimator.setLaunchResult(i, z);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
    public void startNotificationGutsIntent(Intent intent, int i, ExpandableNotificationRow expandableNotificationRow) {
        this.mActivityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction(intent, expandableNotificationRow, i) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$bVQyLqeL8s2vEyL7avL868-QZN8
            public final /* synthetic */ Intent f$1;
            public final /* synthetic */ ExpandableNotificationRow f$2;
            public final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return StatusBarNotificationActivityStarter.this.lambda$startNotificationGutsIntent$9$StatusBarNotificationActivityStarter(this.f$1, this.f$2, this.f$3);
            }
        }, null, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationGutsIntent$9 */
    public /* synthetic */ boolean lambda$startNotificationGutsIntent$9$StatusBarNotificationActivityStarter(Intent intent, ExpandableNotificationRow expandableNotificationRow, int i) {
        AsyncTask.execute(new Runnable(intent, expandableNotificationRow, i) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$nW3uLQs8PHeofwxU6XMCWtI0QxY
            public final /* synthetic */ Intent f$1;
            public final /* synthetic */ ExpandableNotificationRow f$2;
            public final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                StatusBarNotificationActivityStarter.this.lambda$startNotificationGutsIntent$8$StatusBarNotificationActivityStarter(this.f$1, this.f$2, this.f$3);
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationGutsIntent$8 */
    public /* synthetic */ void lambda$startNotificationGutsIntent$8$StatusBarNotificationActivityStarter(Intent intent, ExpandableNotificationRow expandableNotificationRow, int i) {
        this.mMainThreadHandler.post(new Runnable(TaskStackBuilder.create(this.mContext).addNextIntentWithParentStack(intent).startActivities(StatusBar.getActivityOptions(this.mActivityLaunchAnimator.getLaunchAnimation(expandableNotificationRow, this.mStatusBar.isOccluded())), new UserHandle(UserHandle.getUserId(i))), expandableNotificationRow) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$7HkSe7jqxWVChUP0aMlWFfntdY8
            public final /* synthetic */ int f$1;
            public final /* synthetic */ ExpandableNotificationRow f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                StatusBarNotificationActivityStarter.this.lambda$startNotificationGutsIntent$6$StatusBarNotificationActivityStarter(this.f$1, this.f$2);
            }
        });
        if (shouldCollapse()) {
            this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$UA-wtsgLQkEy4zfImYylcYfB4bE
                @Override // java.lang.Runnable
                public final void run() {
                    StatusBarNotificationActivityStarter.this.lambda$startNotificationGutsIntent$7$StatusBarNotificationActivityStarter();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationGutsIntent$6 */
    public /* synthetic */ void lambda$startNotificationGutsIntent$6$StatusBarNotificationActivityStarter(int i, ExpandableNotificationRow expandableNotificationRow) {
        this.mActivityLaunchAnimator.setLaunchResult(i, true);
        removeHUN(expandableNotificationRow);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationGutsIntent$7 */
    public /* synthetic */ void lambda$startNotificationGutsIntent$7$StatusBarNotificationActivityStarter() {
        this.mCommandQueue.animateCollapsePanels(2, true);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
    public void startHistoryIntent(boolean z) {
        this.mActivityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction(z) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$HiHCQQS5efymKkG0o3cpzPgw1uI
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return StatusBarNotificationActivityStarter.this.lambda$startHistoryIntent$12$StatusBarNotificationActivityStarter(this.f$1);
            }
        }, null, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startHistoryIntent$12 */
    public /* synthetic */ boolean lambda$startHistoryIntent$12$StatusBarNotificationActivityStarter(boolean z) {
        AsyncTask.execute(new Runnable(z) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$y2bTxp8eAeRvYhxP1cB4MkmoOg4
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                StatusBarNotificationActivityStarter.this.lambda$startHistoryIntent$11$StatusBarNotificationActivityStarter(this.f$1);
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startHistoryIntent$11 */
    public /* synthetic */ void lambda$startHistoryIntent$11$StatusBarNotificationActivityStarter(boolean z) {
        Intent intent;
        if (z) {
            intent = new Intent("android.settings.NOTIFICATION_HISTORY");
        } else {
            intent = new Intent("android.settings.NOTIFICATION_SETTINGS");
        }
        TaskStackBuilder addNextIntent = TaskStackBuilder.create(this.mContext).addNextIntent(new Intent("android.settings.NOTIFICATION_SETTINGS"));
        if (z) {
            addNextIntent.addNextIntent(intent);
        }
        addNextIntent.startActivities(null, UserHandle.CURRENT);
        if (shouldCollapse()) {
            this.mMainThreadHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$nTNnwH2zGH-PS_xWdyKt9YWDeMo
                @Override // java.lang.Runnable
                public final void run() {
                    StatusBarNotificationActivityStarter.this.lambda$startHistoryIntent$10$StatusBarNotificationActivityStarter();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startHistoryIntent$10 */
    public /* synthetic */ void lambda$startHistoryIntent$10$StatusBarNotificationActivityStarter() {
        this.mCommandQueue.animateCollapsePanels(2, true);
    }

    private void removeHUN(ExpandableNotificationRow expandableNotificationRow) {
        String key = expandableNotificationRow.getEntry().getSbn().getKey();
        HeadsUpManagerPhone headsUpManagerPhone = this.mHeadsUpManager;
        if (headsUpManagerPhone != null && headsUpManagerPhone.isAlerting(key)) {
            if (this.mPresenter.isPresenterFullyCollapsed()) {
                HeadsUpUtil.setIsClickedHeadsUpNotification(expandableNotificationRow, true);
            }
            this.mHeadsUpManager.removeNotification(key, true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFullScreenIntent(final NotificationEntry notificationEntry) {
        if (this.mNotificationInterruptStateProvider.shouldLaunchFullScreenIntentWhenAdded(notificationEntry) && !this.mOpNotificationController.shouldSuppressFullScreenIntent(notificationEntry)) {
            if (shouldSuppressFullScreenIntent(notificationEntry)) {
                this.mLogger.logFullScreenIntentSuppressedByDnD(notificationEntry.getKey());
            } else if (notificationEntry.getImportance() < 4) {
                this.mLogger.logFullScreenIntentNotImportantEnough(notificationEntry.getKey());
            } else {
                boolean z = false;
                try {
                    z = this.mDreamManager.isDreaming();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (!OpLsState.getInstance().getPhoneStatusBar().isDozingCustom()) {
                    this.mUiBgExecutor.execute(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$I08Xq1WjobdAZJsjYxZCmuS-jX0
                        @Override // java.lang.Runnable
                        public final void run() {
                            StatusBarNotificationActivityStarter.this.lambda$handleFullScreenIntent$13$StatusBarNotificationActivityStarter();
                        }
                    });
                }
                if (z) {
                    this.mMainThreadHandler.postDelayed(new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.4
                        @Override // java.lang.Runnable
                        public void run() {
                            StatusBarNotificationActivityStarter.this.sendFullScreenIntentInternal(notificationEntry);
                        }
                    }, 300);
                } else {
                    sendFullScreenIntentInternal(notificationEntry);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleFullScreenIntent$13 */
    public /* synthetic */ void lambda$handleFullScreenIntent$13$StatusBarNotificationActivityStarter() {
        try {
            this.mDreamManager.awaken();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendFullScreenIntentInternal(NotificationEntry notificationEntry) {
        PendingIntent pendingIntent = notificationEntry.getSbn().getNotification().fullScreenIntent;
        if (pendingIntent == null) {
            Log.e("NotifActivityStarter", "sendFullScreenIntent error: " + notificationEntry.getSbn().getNotification());
            return;
        }
        this.mLogger.logSendingFullScreenIntent(notificationEntry.getKey(), pendingIntent);
        EventLog.writeEvent(36002, notificationEntry.getKey());
        try {
            pendingIntent.send();
            notificationEntry.notifyFullScreenIntentLaunched();
            this.mEntryManager.cacheNotification(notificationEntry.getKey(), notificationEntry);
            this.mMetricsLogger.count("note_fullscreen", 1);
        } catch (PendingIntent.CanceledException unused) {
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
    public boolean isCollapsingToShowActivityOverLockscreen() {
        return this.mIsCollapsingToShowActivityOverLockscreen;
    }

    private static boolean shouldAutoCancel(StatusBarNotification statusBarNotification) {
        int i = statusBarNotification.getNotification().flags;
        return (i & 16) == 16 && (i & 64) == 0;
    }

    private void collapseOnMainThread() {
        if (Looper.getMainLooper().isCurrentThread()) {
            this.mShadeController.collapsePanel();
            return;
        }
        Handler handler = this.mMainThreadHandler;
        ShadeController shadeController = this.mShadeController;
        Objects.requireNonNull(shadeController);
        handler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$XDmf1V0qHGBRkx-V63RRNIpOXuQ
            @Override // java.lang.Runnable
            public final void run() {
                ShadeController.this.collapsePanel();
            }
        });
    }

    private boolean shouldCollapse() {
        return this.mStatusBarStateController.getState() != 0 || !this.mActivityLaunchAnimator.isAnimationPending();
    }

    private boolean shouldSuppressFullScreenIntent(NotificationEntry notificationEntry) {
        if (this.mPresenter.isDeviceInVrMode()) {
            return true;
        }
        return notificationEntry.shouldSuppressFullScreenIntent();
    }

    private void removeNotification(NotificationEntry notificationEntry) {
        this.mMainThreadHandler.post(new Runnable(notificationEntry) { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$2LS2Q1aB3MxofHyV1vp1ZRFNvds
            public final /* synthetic */ NotificationEntry f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                StatusBarNotificationActivityStarter.this.lambda$removeNotification$14$StatusBarNotificationActivityStarter(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$removeNotification$14 */
    public /* synthetic */ void lambda$removeNotification$14$StatusBarNotificationActivityStarter(NotificationEntry notificationEntry) {
        Runnable createRemoveRunnable = createRemoveRunnable(notificationEntry);
        if (this.mPresenter.isCollapsing()) {
            this.mShadeController.addPostCollapseAction(createRemoveRunnable);
        } else {
            createRemoveRunnable.run();
        }
    }

    private int getVisibleNotificationsCount() {
        if (this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            return this.mNotifPipeline.getShadeListCount();
        }
        return this.mEntryManager.getActiveNotificationsCount();
    }

    private Runnable createRemoveRunnable(final NotificationEntry notificationEntry) {
        return this.mFeatureFlags.isNewNotifPipelineRenderingEnabled() ? new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.5
            @Override // java.lang.Runnable
            public void run() {
                int i;
                if (StatusBarNotificationActivityStarter.this.mHeadsUpManager.isAlerting(notificationEntry.getKey())) {
                    i = 1;
                } else {
                    i = StatusBarNotificationActivityStarter.this.mNotificationPanel.hasPulsingNotifications() ? 2 : 3;
                }
                NotifCollection notifCollection = StatusBarNotificationActivityStarter.this.mNotifCollection;
                NotificationEntry notificationEntry2 = notificationEntry;
                notifCollection.dismissNotification(notificationEntry2, new DismissedByUserStats(i, 1, NotificationVisibility.obtain(notificationEntry2.getKey(), notificationEntry.getRanking().getRank(), StatusBarNotificationActivityStarter.this.mNotifPipeline.getShadeListCount(), true, NotificationLogger.getNotificationLocation(notificationEntry))));
            }
        } : new Runnable() { // from class: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.6
            @Override // java.lang.Runnable
            public void run() {
                StatusBarNotificationActivityStarter.this.mEntryManager.performRemoveNotification(notificationEntry.getSbn(), 1);
            }
        };
    }

    public static class Builder {
        private final ActivityIntentHelper mActivityIntentHelper;
        private ActivityLaunchAnimator mActivityLaunchAnimator;
        private final ActivityStarter mActivityStarter;
        private final Lazy<AssistManager> mAssistManagerLazy;
        private final Handler mBackgroundHandler;
        private final BubbleController mBubbleController;
        private final NotificationClickNotifier mClickNotifier;
        private final CommandQueue mCommandQueue;
        private final Context mContext;
        private final IDreamManager mDreamManager;
        private final NotificationEntryManager mEntryManager;
        private final FeatureFlags mFeatureFlags;
        private final NotificationGroupManager mGroupManager;
        private final HeadsUpManagerPhone mHeadsUpManager;
        private final KeyguardManager mKeyguardManager;
        private final KeyguardStateController mKeyguardStateController;
        private final LockPatternUtils mLockPatternUtils;
        private final NotificationLockscreenUserManager mLockscreenUserManager;
        private final StatusBarNotificationActivityStarterLogger mLogger;
        private final Handler mMainThreadHandler;
        private final MetricsLogger mMetricsLogger;
        private final NotifCollection mNotifCollection;
        private final NotifPipeline mNotifPipeline;
        private final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
        private NotificationPanelViewController mNotificationPanelViewController;
        private NotificationPresenter mNotificationPresenter;
        private final StatusBarRemoteInputCallback mRemoteInputCallback;
        private final NotificationRemoteInputManager mRemoteInputManager;
        private final ShadeController mShadeController;
        private StatusBar mStatusBar;
        private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
        private final StatusBarStateController mStatusBarStateController;
        private final Executor mUiBgExecutor;

        public Builder(Context context, CommandQueue commandQueue, Handler handler, Handler handler2, Executor executor, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, NotifCollection notifCollection, HeadsUpManagerPhone headsUpManagerPhone, ActivityStarter activityStarter, NotificationClickNotifier notificationClickNotifier, StatusBarStateController statusBarStateController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, KeyguardManager keyguardManager, IDreamManager iDreamManager, BubbleController bubbleController, Lazy<AssistManager> lazy, NotificationRemoteInputManager notificationRemoteInputManager, NotificationGroupManager notificationGroupManager, NotificationLockscreenUserManager notificationLockscreenUserManager, ShadeController shadeController, KeyguardStateController keyguardStateController, NotificationInterruptStateProvider notificationInterruptStateProvider, LockPatternUtils lockPatternUtils, StatusBarRemoteInputCallback statusBarRemoteInputCallback, ActivityIntentHelper activityIntentHelper, FeatureFlags featureFlags, MetricsLogger metricsLogger, StatusBarNotificationActivityStarterLogger statusBarNotificationActivityStarterLogger) {
            this.mContext = context;
            this.mCommandQueue = commandQueue;
            this.mMainThreadHandler = handler;
            this.mBackgroundHandler = handler2;
            this.mUiBgExecutor = executor;
            this.mEntryManager = notificationEntryManager;
            this.mNotifPipeline = notifPipeline;
            this.mNotifCollection = notifCollection;
            this.mHeadsUpManager = headsUpManagerPhone;
            this.mActivityStarter = activityStarter;
            this.mClickNotifier = notificationClickNotifier;
            this.mStatusBarStateController = statusBarStateController;
            this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
            this.mKeyguardManager = keyguardManager;
            this.mDreamManager = iDreamManager;
            this.mBubbleController = bubbleController;
            this.mAssistManagerLazy = lazy;
            this.mRemoteInputManager = notificationRemoteInputManager;
            this.mGroupManager = notificationGroupManager;
            this.mLockscreenUserManager = notificationLockscreenUserManager;
            this.mShadeController = shadeController;
            this.mKeyguardStateController = keyguardStateController;
            this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
            this.mLockPatternUtils = lockPatternUtils;
            this.mRemoteInputCallback = statusBarRemoteInputCallback;
            this.mActivityIntentHelper = activityIntentHelper;
            this.mFeatureFlags = featureFlags;
            this.mMetricsLogger = metricsLogger;
            this.mLogger = statusBarNotificationActivityStarterLogger;
        }

        public Builder setStatusBar(StatusBar statusBar) {
            this.mStatusBar = statusBar;
            return this;
        }

        public Builder setNotificationPresenter(NotificationPresenter notificationPresenter) {
            this.mNotificationPresenter = notificationPresenter;
            return this;
        }

        public Builder setActivityLaunchAnimator(ActivityLaunchAnimator activityLaunchAnimator) {
            this.mActivityLaunchAnimator = activityLaunchAnimator;
            return this;
        }

        public Builder setNotificationPanelViewController(NotificationPanelViewController notificationPanelViewController) {
            this.mNotificationPanelViewController = notificationPanelViewController;
            return this;
        }

        public StatusBarNotificationActivityStarter build() {
            return new StatusBarNotificationActivityStarter(this.mContext, this.mCommandQueue, this.mMainThreadHandler, this.mBackgroundHandler, this.mUiBgExecutor, this.mEntryManager, this.mNotifPipeline, this.mNotifCollection, this.mHeadsUpManager, this.mActivityStarter, this.mClickNotifier, this.mStatusBarStateController, this.mStatusBarKeyguardViewManager, this.mKeyguardManager, this.mDreamManager, this.mBubbleController, this.mAssistManagerLazy, this.mRemoteInputManager, this.mGroupManager, this.mLockscreenUserManager, this.mShadeController, this.mKeyguardStateController, this.mNotificationInterruptStateProvider, this.mLockPatternUtils, this.mRemoteInputCallback, this.mActivityIntentHelper, this.mFeatureFlags, this.mMetricsLogger, this.mLogger, this.mStatusBar, this.mNotificationPresenter, this.mNotificationPanelViewController, this.mActivityLaunchAnimator);
        }
    }
}
