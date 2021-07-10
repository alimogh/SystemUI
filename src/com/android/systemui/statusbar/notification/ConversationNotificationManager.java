package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.widget.ConversationLayout;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationContentView;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.collections.MapsKt__MapsKt;
import kotlin.collections.MapsKt___MapsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt__SequencesKt;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ConversationNotifications.kt */
public final class ConversationNotificationManager {
    private final Context context;
    private final Handler mainHandler;
    private boolean notifPanelCollapsed = true;
    private final NotificationEntryManager notificationEntryManager;
    private final NotificationGroupManager notificationGroupManager;
    private final ConcurrentHashMap<String, ConversationState> states = new ConcurrentHashMap<>();

    public ConversationNotificationManager(@NotNull NotificationEntryManager notificationEntryManager, @NotNull NotificationGroupManager notificationGroupManager, @NotNull Context context, @NotNull Handler handler) {
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "notificationEntryManager");
        Intrinsics.checkParameterIsNotNull(notificationGroupManager, "notificationGroupManager");
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(handler, "mainHandler");
        this.notificationEntryManager = notificationEntryManager;
        this.notificationGroupManager = notificationGroupManager;
        this.context = context;
        this.mainHandler = handler;
        this.notificationEntryManager.addNotificationEntryListener(new NotificationEntryListener(this) { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager.1
            final /* synthetic */ ConversationNotificationManager this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onNotificationRankingUpdated(@NotNull NotificationListenerService.RankingMap rankingMap) {
                Sequence<ConversationLayout> sequence;
                NotificationContentView[] layouts;
                Sequence sequence2;
                Sequence sequence3;
                Intrinsics.checkParameterIsNotNull(rankingMap, "rankingMap");
                ConversationNotificationManager$1$onNotificationRankingUpdated$1 conversationNotificationManager$1$onNotificationRankingUpdated$1 = ConversationNotificationManager$1$onNotificationRankingUpdated$1.INSTANCE;
                NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
                Set keySet = this.this$0.states.keySet();
                Intrinsics.checkExpressionValueIsNotNull(keySet, "states.keys");
                for (NotificationEntry notificationEntry : SequencesKt___SequencesKt.mapNotNull(CollectionsKt___CollectionsKt.asSequence(keySet), new ConversationNotificationManager$1$onNotificationRankingUpdated$activeConversationEntries$1(this))) {
                    StatusBarNotification sbn = notificationEntry.getSbn();
                    Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
                    if (rankingMap.getRanking(sbn.getKey(), ranking) && ranking.isConversation()) {
                        NotificationChannel channel = ranking.getChannel();
                        Intrinsics.checkExpressionValueIsNotNull(channel, "ranking.channel");
                        boolean isImportantConversation = channel.isImportantConversation();
                        ExpandableNotificationRow row = notificationEntry.getRow();
                        if (row == null || (layouts = row.getLayouts()) == null || (sequence2 = ArraysKt___ArraysKt.asSequence(layouts)) == null || (sequence3 = SequencesKt___SequencesKt.flatMap(sequence2, ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$1.INSTANCE)) == null || (sequence = SequencesKt___SequencesKt.mapNotNull(sequence3, ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$2.INSTANCE)) == null) {
                            sequence = SequencesKt__SequencesKt.emptySequence();
                        }
                        boolean z = false;
                        for (ConversationLayout conversationLayout : sequence) {
                            if (isImportantConversation != conversationLayout.isImportantConversation()) {
                                z = true;
                                if (!isImportantConversation || !notificationEntry.isMarkedForUserTriggeredMovement()) {
                                    conversationLayout.setIsImportantConversation(isImportantConversation);
                                } else {
                                    this.this$0.mainHandler.postDelayed(new ConversationNotificationManager$1$onNotificationRankingUpdated$2(conversationLayout, isImportantConversation), (long) 960);
                                }
                            }
                        }
                        if (z) {
                            this.this$0.notificationGroupManager.updateIsolation(notificationEntry);
                        }
                    }
                }
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryInflated(@NotNull NotificationEntry notificationEntry) {
                Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
                NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
                Intrinsics.checkExpressionValueIsNotNull(ranking, "entry.ranking");
                if (ranking.isConversation()) {
                    ConversationNotificationManager$1$onEntryInflated$1 conversationNotificationManager$1$onEntryInflated$1 = new ConversationNotificationManager$1$onEntryInflated$1(this, notificationEntry);
                    ExpandableNotificationRow row = notificationEntry.getRow();
                    if (row != null) {
                        row.setOnExpansionChangedListener(new ConversationNotificationManager$1$onEntryInflated$2(notificationEntry, conversationNotificationManager$1$onEntryInflated$1));
                    }
                    ExpandableNotificationRow row2 = notificationEntry.getRow();
                    boolean z = true;
                    if (row2 == null || !row2.isExpanded()) {
                        z = false;
                    }
                    conversationNotificationManager$1$onEntryInflated$1.invoke(z);
                }
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryReinflated(@NotNull NotificationEntry notificationEntry) {
                Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
                onEntryInflated(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryRemoved(@NotNull NotificationEntry notificationEntry, @Nullable NotificationVisibility notificationVisibility, boolean z, int i) {
                Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
                this.this$0.removeTrackedEntry(notificationEntry);
            }
        });
    }

    public final int getUnreadCount(@NotNull NotificationEntry notificationEntry, @NotNull Notification.Builder builder) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        Intrinsics.checkParameterIsNotNull(builder, "recoveredBuilder");
        ConversationState compute = this.states.compute(notificationEntry.getKey(), new BiFunction<String, ConversationState, ConversationState>(this, builder, notificationEntry) { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager$getUnreadCount$1
            final /* synthetic */ NotificationEntry $entry;
            final /* synthetic */ Notification.Builder $recoveredBuilder;
            final /* synthetic */ ConversationNotificationManager this$0;

            {
                this.this$0 = r1;
                this.$recoveredBuilder = r2;
                this.$entry = r3;
            }

            @NotNull
            public final ConversationNotificationManager.ConversationState apply(@NotNull String str, @Nullable ConversationNotificationManager.ConversationState conversationState) {
                Intrinsics.checkParameterIsNotNull(str, "<anonymous parameter 0>");
                int i = 1;
                if (conversationState != null) {
                    i = Notification.areStyledNotificationsVisiblyDifferent(Notification.Builder.recoverBuilder(this.this$0.context, conversationState.getNotification()), this.$recoveredBuilder) ? conversationState.getUnreadCount() + 1 : conversationState.getUnreadCount();
                }
                StatusBarNotification sbn = this.$entry.getSbn();
                Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
                Notification notification = sbn.getNotification();
                Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
                return new ConversationNotificationManager.ConversationState(i, notification);
            }
        });
        if (compute != null) {
            return compute.getUnreadCount();
        }
        Intrinsics.throwNpe();
        throw null;
    }

    public final void onNotificationPanelExpandStateChanged(boolean z) {
        this.notifPanelCollapsed = z;
        if (!z) {
            Map map = MapsKt__MapsKt.toMap(SequencesKt___SequencesKt.mapNotNull(MapsKt___MapsKt.asSequence(this.states), new Function1<Map.Entry<? extends String, ? extends ConversationState>, Pair<? extends String, ? extends NotificationEntry>>(this) { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager$onNotificationPanelExpandStateChanged$expanded$1
                final /* synthetic */ ConversationNotificationManager this$0;

                {
                    this.this$0 = r1;
                }

                /* Return type fixed from 'java.lang.Object' to match base method */
                /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
                @Override // kotlin.jvm.functions.Function1
                public /* bridge */ /* synthetic */ Pair<? extends String, ? extends NotificationEntry> invoke(Map.Entry<? extends String, ? extends ConversationNotificationManager.ConversationState> entry) {
                    return invoke((Map.Entry<String, ConversationNotificationManager.ConversationState>) entry);
                }

                @Nullable
                public final Pair<String, NotificationEntry> invoke(@NotNull Map.Entry<String, ConversationNotificationManager.ConversationState> entry) {
                    ExpandableNotificationRow row;
                    Intrinsics.checkParameterIsNotNull(entry, "<name for destructuring parameter 0>");
                    String key = entry.getKey();
                    NotificationEntry activeNotificationUnfiltered = this.this$0.notificationEntryManager.getActiveNotificationUnfiltered(key);
                    if (activeNotificationUnfiltered == null || (row = activeNotificationUnfiltered.getRow()) == null || !row.isExpanded()) {
                        return null;
                    }
                    return TuplesKt.to(key, activeNotificationUnfiltered);
                }
            }));
            this.states.replaceAll(new BiFunction<String, ConversationState, ConversationState>(map) { // from class: com.android.systemui.statusbar.notification.ConversationNotificationManager$onNotificationPanelExpandStateChanged$1
                final /* synthetic */ Map $expanded;

                {
                    this.$expanded = r1;
                }

                @NotNull
                public final ConversationNotificationManager.ConversationState apply(@NotNull String str, @NotNull ConversationNotificationManager.ConversationState conversationState) {
                    Intrinsics.checkParameterIsNotNull(str, "key");
                    Intrinsics.checkParameterIsNotNull(conversationState, "state");
                    return this.$expanded.containsKey(str) ? ConversationNotificationManager.ConversationState.copy$default(conversationState, 0, null, 2, null) : conversationState;
                }
            });
            for (ExpandableNotificationRow expandableNotificationRow : SequencesKt___SequencesKt.mapNotNull(CollectionsKt___CollectionsKt.asSequence(map.values()), ConversationNotificationManager$onNotificationPanelExpandStateChanged$2.INSTANCE)) {
                resetBadgeUi(expandableNotificationRow);
            }
        }
    }

    /* access modifiers changed from: private */
    public final void resetCount(String str) {
        this.states.compute(str, ConversationNotificationManager$resetCount$1.INSTANCE);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void removeTrackedEntry(NotificationEntry notificationEntry) {
        this.states.remove(notificationEntry.getKey());
    }

    /* access modifiers changed from: private */
    public final void resetBadgeUi(ExpandableNotificationRow expandableNotificationRow) {
        Sequence sequence;
        NotificationContentView[] layouts = expandableNotificationRow.getLayouts();
        if (layouts == null || (sequence = ArraysKt___ArraysKt.asSequence(layouts)) == null) {
            sequence = SequencesKt__SequencesKt.emptySequence();
        }
        for (ConversationLayout conversationLayout : SequencesKt___SequencesKt.mapNotNull(SequencesKt___SequencesKt.flatMap(sequence, ConversationNotificationManager$resetBadgeUi$1.INSTANCE), ConversationNotificationManager$resetBadgeUi$2.INSTANCE)) {
            conversationLayout.setUnreadCount(0);
        }
    }

    /* compiled from: ConversationNotifications.kt */
    /* access modifiers changed from: private */
    public static final class ConversationState {
        @NotNull
        private final Notification notification;
        private final int unreadCount;

        public static /* synthetic */ ConversationState copy$default(ConversationState conversationState, int i, Notification notification, int i2, Object obj) {
            if ((i2 & 1) != 0) {
                i = conversationState.unreadCount;
            }
            if ((i2 & 2) != 0) {
                notification = conversationState.notification;
            }
            return conversationState.copy(i, notification);
        }

        @NotNull
        public final ConversationState copy(int i, @NotNull Notification notification) {
            Intrinsics.checkParameterIsNotNull(notification, "notification");
            return new ConversationState(i, notification);
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ConversationState)) {
                return false;
            }
            ConversationState conversationState = (ConversationState) obj;
            return this.unreadCount == conversationState.unreadCount && Intrinsics.areEqual(this.notification, conversationState.notification);
        }

        public int hashCode() {
            int hashCode = Integer.hashCode(this.unreadCount) * 31;
            Notification notification = this.notification;
            return hashCode + (notification != null ? notification.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "ConversationState(unreadCount=" + this.unreadCount + ", notification=" + this.notification + ")";
        }

        public ConversationState(int i, @NotNull Notification notification) {
            Intrinsics.checkParameterIsNotNull(notification, "notification");
            this.unreadCount = i;
            this.notification = notification;
        }

        @NotNull
        public final Notification getNotification() {
            return this.notification;
        }

        public final int getUnreadCount() {
            return this.unreadCount;
        }
    }
}
