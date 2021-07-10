package com.android.systemui.statusbar.notification.row;

import android.view.View;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.util.time.SystemClock;
import java.util.Objects;
public class ExpandableNotificationRowController {
    private final ActivatableNotificationViewController mActivatableNotificationViewController;
    private final boolean mAllowLongPress;
    private final String mAppName;
    private final SystemClock mClock;
    private final ExpandableNotificationRow.ExpansionLogger mExpansionLogger = new ExpandableNotificationRow.ExpansionLogger() { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$ExpandableNotificationRowController$7PRoCj-f2CPB0eC3liBvfR80zWU
        @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow.ExpansionLogger
        public final void logNotificationExpansion(String str, boolean z, boolean z2) {
            ExpandableNotificationRowController.this.logNotificationExpansion(str, z, z2);
        }
    };
    private final FalsingManager mFalsingManager;
    private final HeadsUpManager mHeadsUpManager;
    private final KeyguardBypassController mKeyguardBypassController;
    private final NotificationMediaManager mMediaManager;
    private final NotificationGroupManager mNotificationGroupManager;
    private final NotificationGutsManager mNotificationGutsManager;
    private final String mNotificationKey;
    private final NotificationLogger mNotificationLogger;
    private final ExpandableNotificationRow.OnAppOpsClickListener mOnAppOpsClickListener;
    private Runnable mOnDismissRunnable;
    private final ExpandableNotificationRow.OnExpandClickListener mOnExpandClickListener;
    private final PeopleNotificationIdentifier mPeopleNotificationIdentifier;
    private final PluginManager mPluginManager;
    private final RowContentBindStage mRowContentBindStage;
    private final StatusBarStateController mStatusBarStateController;
    private final ExpandableNotificationRow mView;

    public ExpandableNotificationRowController(ExpandableNotificationRow expandableNotificationRow, ActivatableNotificationViewController activatableNotificationViewController, NotificationMediaManager notificationMediaManager, PluginManager pluginManager, SystemClock systemClock, String str, String str2, KeyguardBypassController keyguardBypassController, NotificationGroupManager notificationGroupManager, RowContentBindStage rowContentBindStage, NotificationLogger notificationLogger, HeadsUpManager headsUpManager, ExpandableNotificationRow.OnExpandClickListener onExpandClickListener, StatusBarStateController statusBarStateController, NotificationGutsManager notificationGutsManager, boolean z, Runnable runnable, FalsingManager falsingManager, PeopleNotificationIdentifier peopleNotificationIdentifier) {
        this.mView = expandableNotificationRow;
        this.mActivatableNotificationViewController = activatableNotificationViewController;
        this.mMediaManager = notificationMediaManager;
        this.mPluginManager = pluginManager;
        this.mClock = systemClock;
        this.mAppName = str;
        this.mNotificationKey = str2;
        this.mKeyguardBypassController = keyguardBypassController;
        this.mNotificationGroupManager = notificationGroupManager;
        this.mRowContentBindStage = rowContentBindStage;
        this.mNotificationLogger = notificationLogger;
        this.mHeadsUpManager = headsUpManager;
        this.mOnExpandClickListener = onExpandClickListener;
        this.mStatusBarStateController = statusBarStateController;
        this.mNotificationGutsManager = notificationGutsManager;
        this.mOnDismissRunnable = runnable;
        Objects.requireNonNull(notificationGutsManager);
        this.mOnAppOpsClickListener = new ExpandableNotificationRow.OnAppOpsClickListener() { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$oy9pBf4KjrW7ZRpgHkpOCIaDYlg
            @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow.OnAppOpsClickListener
            public final boolean onClick(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
                return NotificationGutsManager.this.openGuts(view, i, i2, menuItem);
            }
        };
        this.mAllowLongPress = z;
        this.mFalsingManager = falsingManager;
        this.mPeopleNotificationIdentifier = peopleNotificationIdentifier;
    }

    public void init() {
        this.mActivatableNotificationViewController.init();
        this.mView.initialize(this.mAppName, this.mNotificationKey, this.mExpansionLogger, this.mKeyguardBypassController, this.mNotificationGroupManager, this.mHeadsUpManager, this.mRowContentBindStage, this.mOnExpandClickListener, this.mMediaManager, this.mOnAppOpsClickListener, this.mFalsingManager, this.mStatusBarStateController, this.mPeopleNotificationIdentifier);
        this.mView.setOnDismissRunnable(this.mOnDismissRunnable);
        this.mView.setDescendantFocusability(393216);
        setLongPressListenerIfNeeded();
        if (NotificationRemoteInputManager.ENABLE_REMOTE_INPUT) {
            this.mView.setDescendantFocusability(131072);
        }
        this.mView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() { // from class: com.android.systemui.statusbar.notification.row.ExpandableNotificationRowController.1
            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewAttachedToWindow(View view) {
                ExpandableNotificationRowController.this.mView.getEntry().setInitializationTime(ExpandableNotificationRowController.this.mClock.elapsedRealtime());
                ExpandableNotificationRowController.this.mPluginManager.addPluginListener((PluginListener) ExpandableNotificationRowController.this.mView, NotificationMenuRowPlugin.class, false);
            }

            @Override // android.view.View.OnAttachStateChangeListener
            public void onViewDetachedFromWindow(View view) {
                ExpandableNotificationRowController.this.mPluginManager.removePluginListener(ExpandableNotificationRowController.this.mView);
            }
        });
    }

    /* access modifiers changed from: private */
    public void logNotificationExpansion(String str, boolean z, boolean z2) {
        this.mNotificationLogger.onExpansionChanged(str, z, z2);
    }

    public void setOnDismissRunnable(Runnable runnable) {
        this.mOnDismissRunnable = runnable;
        this.mView.setOnDismissRunnable(runnable);
    }

    public void setLongPressListenerIfNeeded() {
        if (this.mAllowLongPress) {
            this.mView.setLongPressListener(new ExpandableNotificationRow.LongPressListener() { // from class: com.android.systemui.statusbar.notification.row.-$$Lambda$ExpandableNotificationRowController$6Nlf1kO-1fxOfAdCtUiLbXCFVIg
                @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow.LongPressListener
                public final boolean onLongPress(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
                    return ExpandableNotificationRowController.this.lambda$setLongPressListenerIfNeeded$0$ExpandableNotificationRowController(view, i, i2, menuItem);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setLongPressListenerIfNeeded$0 */
    public /* synthetic */ boolean lambda$setLongPressListenerIfNeeded$0$ExpandableNotificationRowController(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
        if (!this.mView.isSummaryWithChildren()) {
            return this.mNotificationGutsManager.openGuts(view, i, i2, menuItem);
        }
        this.mView.expandNotification();
        return true;
    }
}
