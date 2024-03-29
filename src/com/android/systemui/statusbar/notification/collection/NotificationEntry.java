package com.android.systemui.statusbar.notification.collection;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.Person;
import android.app.RemoteInputHistoryItem;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.widget.Chronometer;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.statusbar.InflationTask;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifPromoter;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifDismissInterceptor;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender;
import com.android.systemui.statusbar.notification.icon.IconPack;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRowController;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public final class NotificationEntry extends ListEntry {
    public EditedSuggestionInfo editedSuggestionInfo;
    private boolean hasSentReply;
    public CharSequence headsUpStatusBarText;
    public CharSequence headsUpStatusBarTextPublic;
    private long initializationTime;
    private boolean interruption;
    public Chronometer keyguardChronometer;
    private long lastFullScreenIntentLaunchTime;
    public long lastRemoteInputSent;
    public final ArraySet<Integer> mActiveAppOps;
    private boolean mAllowFgsDismissal;
    private boolean mAutoHeadsUp;
    private Notification.BubbleMetadata mBubbleMetadata;
    private int mBucket;
    private int mCachedContrastColor;
    private int mCachedContrastColorIsFor;
    int mCancellationReason;
    private long mCreationTime;
    private Throwable mDebugThrowable;
    final List<NotifDismissInterceptor> mDismissInterceptors;
    private DismissState mDismissState;
    private IconPack mIcons;
    public boolean mIsGamingModeNotification;
    private boolean mIsMarkedForUserTriggeredMovement;
    public Boolean mIsSystemNotification;
    private final String mKey;
    final List<NotifLifetimeExtender> mLifetimeExtenders;
    private List<OnSensitivityChangedListener> mOnSensitivityChangedListeners;
    private boolean mPulseSupressed;
    private NotificationListenerService.Ranking mRanking;
    private ExpandableNotificationRowController mRowController;
    private InflationTask mRunningTask;
    private StatusBarNotification mSbn;
    private boolean mSensitive;
    private boolean mShelfIconVisible;
    public String remoteInputMimeType;
    public CharSequence remoteInputText;
    public CharSequence remoteInputTextWhenReset;
    public Uri remoteInputUri;
    private ExpandableNotificationRow row;
    public Chronometer statusBarChronometer;
    public int targetSdk;

    public enum DismissState {
        NOT_DISMISSED,
        DISMISSED,
        PARENT_DISMISSED
    }

    public interface OnSensitivityChangedListener {
        void onSensitivityChanged(NotificationEntry notificationEntry);
    }

    @Override // com.android.systemui.statusbar.notification.collection.ListEntry
    public NotificationEntry getRepresentativeEntry() {
        return this;
    }

    public NotificationEntry(StatusBarNotification statusBarNotification, NotificationListenerService.Ranking ranking, long j) {
        this(statusBarNotification, ranking, false, j);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public NotificationEntry(android.service.notification.StatusBarNotification r5, android.service.notification.NotificationListenerService.Ranking r6, boolean r7, long r8) {
        /*
            r4 = this;
            java.util.Objects.requireNonNull(r5)
            r0 = r5
            android.service.notification.StatusBarNotification r0 = (android.service.notification.StatusBarNotification) r0
            java.lang.String r0 = r0.getKey()
            java.util.Objects.requireNonNull(r0)
            java.lang.String r0 = (java.lang.String) r0
            r4.<init>(r0)
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r4.mLifetimeExtenders = r0
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r4.mDismissInterceptors = r0
            r0 = -1
            r4.mCancellationReason = r0
            com.android.systemui.statusbar.notification.collection.NotificationEntry$DismissState r0 = com.android.systemui.statusbar.notification.collection.NotificationEntry.DismissState.NOT_DISMISSED
            r4.mDismissState = r0
            r0 = 0
            com.android.systemui.statusbar.notification.icon.IconPack r1 = com.android.systemui.statusbar.notification.icon.IconPack.buildEmptyPack(r0)
            r4.mIcons = r1
            r1 = -2000(0xfffffffffffff830, double:NaN)
            r4.lastFullScreenIntentLaunchTime = r1
            r3 = 1
            r4.mCachedContrastColor = r3
            r4.mCachedContrastColorIsFor = r3
            r4.mRunningTask = r0
            r4.lastRemoteInputSent = r1
            android.util.ArraySet r1 = new android.util.ArraySet
            r2 = 3
            r1.<init>(r2)
            r4.mActiveAppOps = r1
            r1 = -1
            r4.initializationTime = r1
            r4.mSensitive = r3
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>()
            r4.mOnSensitivityChangedListeners = r1
            r1 = 5
            r4.mBucket = r1
            r4.statusBarChronometer = r0
            r4.keyguardChronometer = r0
            java.util.Objects.requireNonNull(r6)
            r4.mCreationTime = r8
            java.lang.String r8 = r5.getKey()
            r4.mKey = r8
            r4.setSbn(r5)
            r4.setRanking(r6)
            r4.mAllowFgsDismissal = r7
            java.lang.String r6 = "scene_modes_game"
            android.app.Notification r7 = r5.getNotification()     // Catch:{ Exception -> 0x0088 }
            java.lang.String r7 = r7.getChannelId()     // Catch:{ Exception -> 0x0088 }
            boolean r6 = r6.equals(r7)     // Catch:{ Exception -> 0x0088 }
            r4.mIsGamingModeNotification = r6     // Catch:{ Exception -> 0x0088 }
            java.lang.String r7 = "android"
            java.lang.String r5 = r5.getPackageName()     // Catch:{ Exception -> 0x0088 }
            boolean r5 = r7.equals(r5)     // Catch:{ Exception -> 0x0088 }
            r5 = r5 & r6
            r4.mIsGamingModeNotification = r5     // Catch:{ Exception -> 0x0088 }
            goto L_0x0093
        L_0x0088:
            r4 = move-exception
            java.lang.String r5 = "NotificationData"
            java.lang.String r6 = "exception while getting channel ID"
            android.util.Log.e(r5, r6)
            r4.printStackTrace()
        L_0x0093:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.collection.NotificationEntry.<init>(android.service.notification.StatusBarNotification, android.service.notification.NotificationListenerService$Ranking, boolean, long):void");
    }

    @Override // com.android.systemui.statusbar.notification.collection.ListEntry
    public String getKey() {
        return this.mKey;
    }

    public StatusBarNotification getSbn() {
        return this.mSbn;
    }

    public void setSbn(StatusBarNotification statusBarNotification) {
        Objects.requireNonNull(statusBarNotification);
        Objects.requireNonNull(statusBarNotification.getKey());
        if (statusBarNotification.getKey().equals(this.mKey)) {
            this.mSbn = statusBarNotification;
            this.mBubbleMetadata = statusBarNotification.getNotification().getBubbleMetadata();
            return;
        }
        throw new IllegalArgumentException("New key " + statusBarNotification.getKey() + " doesn't match existing key " + this.mKey);
    }

    public NotificationListenerService.Ranking getRanking() {
        return this.mRanking;
    }

    public long getCreationTime() {
        return this.mCreationTime;
    }

    public void setRanking(NotificationListenerService.Ranking ranking) {
        Objects.requireNonNull(ranking);
        Objects.requireNonNull(ranking.getKey());
        if (ranking.getKey().equals(this.mKey)) {
            this.mRanking = ranking;
            return;
        }
        throw new IllegalArgumentException("New key " + ranking.getKey() + " doesn't match existing key " + this.mKey);
    }

    public DismissState getDismissState() {
        return this.mDismissState;
    }

    /* access modifiers changed from: package-private */
    public void setDismissState(DismissState dismissState) {
        Objects.requireNonNull(dismissState);
        this.mDismissState = dismissState;
    }

    public NotifFilter getExcludingFilter() {
        return getAttachState().getExcludingFilter();
    }

    public NotifPromoter getNotifPromoter() {
        return getAttachState().getPromoter();
    }

    public NotificationChannel getChannel() {
        return this.mRanking.getChannel();
    }

    public long getLastAudiblyAlertedMs() {
        return this.mRanking.getLastAudiblyAlertedMillis();
    }

    public boolean isAmbient() {
        return this.mRanking.isAmbient();
    }

    public int getImportance() {
        if (!isMediaNotification() || getChannel() == null || getChannel().hasUserSetImportance()) {
            return this.mRanking.getImportance();
        }
        return Math.max(this.mRanking.getImportance(), 3);
    }

    public List<SnoozeCriterion> getSnoozeCriteria() {
        return this.mRanking.getSnoozeCriteria();
    }

    public int getUserSentiment() {
        return this.mRanking.getUserSentiment();
    }

    public int getSuppressedVisualEffects() {
        return this.mRanking.getSuppressedVisualEffects();
    }

    public boolean canBubble() {
        return this.mRanking.canBubble();
    }

    public List<Notification.Action> getSmartActions() {
        return this.mRanking.getSmartActions();
    }

    public List<CharSequence> getSmartReplies() {
        return this.mRanking.getSmartReplies();
    }

    public IconPack getIcons() {
        return this.mIcons;
    }

    public void setIcons(IconPack iconPack) {
        this.mIcons = iconPack;
    }

    public void setInterruption() {
        this.interruption = true;
    }

    public boolean hasInterrupted() {
        return this.interruption;
    }

    public boolean isBubble() {
        return (this.mSbn.getNotification().flags & 4096) != 0;
    }

    public Notification.BubbleMetadata getBubbleMetadata() {
        return this.mBubbleMetadata;
    }

    public boolean setFlagBubble(boolean z) {
        boolean isBubble = isBubble();
        if (!z) {
            this.mSbn.getNotification().flags &= -4097;
        } else if (this.mBubbleMetadata != null && canBubble()) {
            this.mSbn.getNotification().flags |= 4096;
        }
        return isBubble != isBubble();
    }

    public int getBucket() {
        return this.mBucket;
    }

    public void setBucket(int i) {
        this.mBucket = i;
    }

    public ExpandableNotificationRow getRow() {
        return this.row;
    }

    public void setRow(ExpandableNotificationRow expandableNotificationRow) {
        this.row = expandableNotificationRow;
        updateShelfIconVisibility();
    }

    public ExpandableNotificationRowController getRowController() {
        return this.mRowController;
    }

    public void setRowController(ExpandableNotificationRowController expandableNotificationRowController) {
        this.mRowController = expandableNotificationRowController;
    }

    public List<NotificationEntry> getAttachedNotifChildren() {
        List<ExpandableNotificationRow> attachedChildren;
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow == null || (attachedChildren = expandableNotificationRow.getAttachedChildren()) == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (ExpandableNotificationRow expandableNotificationRow2 : attachedChildren) {
            arrayList.add(expandableNotificationRow2.getEntry());
        }
        return arrayList;
    }

    public void notifyFullScreenIntentLaunched() {
        setInterruption();
        this.lastFullScreenIntentLaunchTime = SystemClock.elapsedRealtime();
    }

    public boolean hasJustLaunchedFullScreenIntent() {
        return SystemClock.elapsedRealtime() < this.lastFullScreenIntentLaunchTime + 2000;
    }

    public boolean hasJustSentRemoteInput() {
        return SystemClock.elapsedRealtime() < this.lastRemoteInputSent + 500;
    }

    public boolean hasFinishedInitialization() {
        return this.initializationTime != -1 && SystemClock.elapsedRealtime() > this.initializationTime + 400;
    }

    public int getContrastedColor(Context context, boolean z, int i) {
        int i2;
        int i3 = z ? 0 : this.mSbn.getNotification().color;
        if (this.mCachedContrastColorIsFor == i3 && (i2 = this.mCachedContrastColor) != 1) {
            return i2;
        }
        int resolveContrastColor = ContrastColorUtil.resolveContrastColor(context, i3, i);
        this.mCachedContrastColorIsFor = i3;
        this.mCachedContrastColor = resolveContrastColor;
        return resolveContrastColor;
    }

    public void abortTask() {
        InflationTask inflationTask = this.mRunningTask;
        if (inflationTask != null) {
            inflationTask.abort();
            this.mRunningTask = null;
        }
    }

    public void setInflationTask(InflationTask inflationTask) {
        abortTask();
        this.mRunningTask = inflationTask;
    }

    public void onInflationTaskFinished() {
        this.mRunningTask = null;
    }

    @VisibleForTesting
    public InflationTask getRunningTask() {
        return this.mRunningTask;
    }

    public void setDebugThrowable(Throwable th) {
        this.mDebugThrowable = th;
    }

    public Throwable getDebugThrowable() {
        return this.mDebugThrowable;
    }

    public void onRemoteInputInserted() {
        this.lastRemoteInputSent = -2000;
        this.remoteInputTextWhenReset = null;
    }

    public void setHasSentReply() {
        this.hasSentReply = true;
    }

    public boolean isLastMessageFromReply() {
        Notification.MessagingStyle.Message message;
        if (!this.hasSentReply) {
            return false;
        }
        Bundle bundle = this.mSbn.getNotification().extras;
        RemoteInputHistoryItem[] parcelableArray = bundle.getParcelableArray("android.remoteInputHistoryItems");
        if (!ArrayUtils.isEmpty(parcelableArray instanceof RemoteInputHistoryItem[] ? parcelableArray : null)) {
            return true;
        }
        List<Notification.MessagingStyle.Message> messagesFromBundleArray = Notification.MessagingStyle.Message.getMessagesFromBundleArray(bundle.getParcelableArray("android.messages"));
        if (messagesFromBundleArray == null || messagesFromBundleArray.isEmpty() || (message = messagesFromBundleArray.get(messagesFromBundleArray.size() - 1)) == null) {
            return false;
        }
        Person senderPerson = message.getSenderPerson();
        if (senderPerson == null) {
            return true;
        }
        return Objects.equals((Person) bundle.getParcelable("android.messagingUser"), senderPerson);
    }

    public void resetInitializationTime() {
        this.initializationTime = -1;
    }

    public void setInitializationTime(long j) {
        if (this.initializationTime == -1) {
            this.initializationTime = j;
        }
    }

    public void sendAccessibilityEvent(int i) {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.sendAccessibilityEvent(i);
        }
    }

    public boolean isMediaNotification() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow == null) {
            return false;
        }
        return expandableNotificationRow.isMediaRow();
    }

    public boolean isTopLevelChild() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow != null && expandableNotificationRow.isTopLevelChild();
    }

    public void resetUserExpansion() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.resetUserExpansion();
        }
    }

    public boolean rowExists() {
        return this.row != null;
    }

    public boolean isRowDismissed() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow != null && expandableNotificationRow.isDismissed();
    }

    public boolean isRowRemoved() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow != null && expandableNotificationRow.isRemoved();
    }

    public boolean isRemoved() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow == null || expandableNotificationRow.isRemoved();
    }

    public boolean isRowPinned() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow != null && expandableNotificationRow.isPinned();
    }

    public boolean isPinnedAndExpanded() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow != null && expandableNotificationRow.isPinnedAndExpanded();
    }

    public void setRowPinned(boolean z) {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setPinned(z);
        }
    }

    public boolean isRowHeadsUp() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow != null && expandableNotificationRow.isHeadsUp();
    }

    public boolean showingPulsing() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow != null && expandableNotificationRow.showingPulsing();
    }

    public void setHeadsUp(boolean z) {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setHeadsUp(z);
        }
    }

    public void setHeadsUpAnimatingAway(boolean z) {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setHeadsUpAnimatingAway(z);
        }
    }

    public void setAutoHeadsUp(boolean z) {
        this.mAutoHeadsUp = z;
    }

    public boolean isAutoHeadsUp() {
        return this.mAutoHeadsUp;
    }

    public boolean mustStayOnScreen() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow != null && expandableNotificationRow.mustStayOnScreen();
    }

    public void setHeadsUpIsVisible() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setHeadsUpIsVisible();
        }
    }

    public ExpandableNotificationRow getHeadsUpAnimationView() {
        return this.row;
    }

    public void setUserLocked(boolean z) {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setUserLocked(z);
        }
    }

    public void setUserExpanded(boolean z, boolean z2) {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setUserExpanded(z, z2);
        }
    }

    public void setGroupExpansionChanging(boolean z) {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setGroupExpansionChanging(z);
        }
    }

    public void notifyHeightChanged(boolean z) {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.notifyHeightChanged(z);
        }
    }

    public void closeRemoteInput() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.closeRemoteInput();
        }
    }

    public boolean areChildrenExpanded() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow != null && expandableNotificationRow.areChildrenExpanded();
    }

    public boolean isGroupNotFullyVisible() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow == null || expandableNotificationRow.isGroupNotFullyVisible();
    }

    public NotificationGuts getGuts() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            return expandableNotificationRow.getGuts();
        }
        return null;
    }

    public void removeRow() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setRemoved();
        }
    }

    public boolean isSummaryWithChildren() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow != null && expandableNotificationRow.isSummaryWithChildren();
    }

    public void setKeepInParent(boolean z) {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setKeepInParent(z);
        }
    }

    public void onDensityOrFontScaleChanged() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.onDensityOrFontScaleChanged();
        }
    }

    public boolean areGutsExposed() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return (expandableNotificationRow == null || expandableNotificationRow.getGuts() == null || !this.row.getGuts().isExposed()) ? false : true;
    }

    public boolean isChildInGroup() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        return expandableNotificationRow != null && expandableNotificationRow.isChildInGroup();
    }

    public boolean isClearable() {
        if (!isDismissable()) {
            return false;
        }
        List<NotificationEntry> attachedNotifChildren = getAttachedNotifChildren();
        if (attachedNotifChildren == null || attachedNotifChildren.size() <= 0) {
            return true;
        }
        for (int i = 0; i < attachedNotifChildren.size(); i++) {
            if (!attachedNotifChildren.get(i).isDismissable()) {
                return false;
            }
        }
        return true;
    }

    private boolean isDismissable() {
        boolean z = (this.mSbn.getNotification().flags & 2) != 0;
        boolean z2 = (this.mSbn.getNotification().flags & 32) != 0;
        boolean z3 = (this.mSbn.getNotification().flags & 64) != 0;
        if (this.mAllowFgsDismissal) {
            return !z2 || z || z3;
        }
        return this.mSbn.isClearable();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isExemptFromDndVisualSuppression() {
        if (isNotificationBlockedByPolicy(this.mSbn.getNotification())) {
            return false;
        }
        if ((this.mSbn.getNotification().flags & 64) != 0 || this.mSbn.getNotification().isMediaNotification()) {
            return true;
        }
        Boolean bool = this.mIsSystemNotification;
        if (bool == null || !bool.booleanValue()) {
            return false;
        }
        return true;
    }

    private boolean shouldSuppressVisualEffect(int i) {
        if (!isExemptFromDndVisualSuppression() && (getSuppressedVisualEffects() & i) != 0) {
            return true;
        }
        return false;
    }

    public boolean shouldSuppressFullScreenIntent() {
        return shouldSuppressVisualEffect(4);
    }

    public boolean shouldSuppressPeek() {
        return shouldSuppressVisualEffect(16);
    }

    public boolean shouldSuppressStatusBar() {
        return shouldSuppressVisualEffect(32);
    }

    public boolean shouldSuppressAmbient() {
        return shouldSuppressVisualEffect(128);
    }

    public boolean shouldSuppressNotificationList() {
        return shouldSuppressVisualEffect(256);
    }

    public boolean shouldSuppressNotificationDot() {
        return shouldSuppressVisualEffect(64);
    }

    private static boolean isNotificationBlockedByPolicy(Notification notification) {
        return isCategory("call", notification) || isCategory("msg", notification) || isCategory("alarm", notification) || isCategory("event", notification) || isCategory("reminder", notification);
    }

    private static boolean isCategory(String str, Notification notification) {
        return Objects.equals(notification.category, str);
    }

    public void setSensitive(boolean z, boolean z2) {
        getRow().setSensitive(z, z2);
        if (z != this.mSensitive) {
            this.mSensitive = z;
            for (int i = 0; i < this.mOnSensitivityChangedListeners.size(); i++) {
                this.mOnSensitivityChangedListeners.get(i).onSensitivityChanged(this);
            }
        }
    }

    public boolean isSensitive() {
        return this.mSensitive;
    }

    public void addOnSensitivityChangedListener(OnSensitivityChangedListener onSensitivityChangedListener) {
        this.mOnSensitivityChangedListeners.add(onSensitivityChangedListener);
    }

    public void removeOnSensitivityChangedListener(OnSensitivityChangedListener onSensitivityChangedListener) {
        this.mOnSensitivityChangedListeners.remove(onSensitivityChangedListener);
    }

    public boolean isPulseSuppressed() {
        return this.mPulseSupressed;
    }

    public void setPulseSuppressed(boolean z) {
        this.mPulseSupressed = z;
    }

    public boolean isMarkedForUserTriggeredMovement() {
        return this.mIsMarkedForUserTriggeredMovement;
    }

    public void setShelfIconVisible(boolean z) {
        this.mShelfIconVisible = z;
        updateShelfIconVisibility();
    }

    private void updateShelfIconVisibility() {
        ExpandableNotificationRow expandableNotificationRow = this.row;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setShelfIconVisible(this.mShelfIconVisible);
        }
    }

    public void markForUserTriggeredMovement(boolean z) {
        this.mIsMarkedForUserTriggeredMovement = z;
    }

    public static class EditedSuggestionInfo {
        public final int index;
        public final CharSequence originalText;

        public EditedSuggestionInfo(CharSequence charSequence, int i) {
            this.originalText = charSequence;
            this.index = i;
        }
    }
}
