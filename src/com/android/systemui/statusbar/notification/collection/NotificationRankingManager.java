package com.android.systemui.statusbar.notification.collection;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.notification.NotificationEntryManagerLogger;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import kotlin.reflect.KProperty;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: NotificationRankingManager.kt */
public class NotificationRankingManager {
    static final /* synthetic */ KProperty[] $$delegatedProperties;
    private final NotificationGroupManager groupManager;
    private final HeadsUpManager headsUpManager;
    private final HighPriorityProvider highPriorityProvider;
    private final NotificationEntryManagerLogger logger;
    private final Lazy mediaManager$delegate = LazyKt.lazy(new Function0<NotificationMediaManager>(this) { // from class: com.android.systemui.statusbar.notification.collection.NotificationRankingManager$mediaManager$2
        final /* synthetic */ NotificationRankingManager this$0;

        {
            this.this$0 = r1;
        }

        @Override // kotlin.jvm.functions.Function0
        public final NotificationMediaManager invoke() {
            return (NotificationMediaManager) this.this$0.mediaManagerLazy.get();
        }
    });
    private final dagger.Lazy<NotificationMediaManager> mediaManagerLazy;
    private final NotificationFilter notifFilter;
    private final PeopleNotificationIdentifier peopleNotificationIdentifier;
    private final Comparator<NotificationEntry> rankingComparator = new Comparator<NotificationEntry>(this) { // from class: com.android.systemui.statusbar.notification.collection.NotificationRankingManager$rankingComparator$1
        final /* synthetic */ NotificationRankingManager this$0;

        {
            this.this$0 = r1;
        }

        public final int compare(NotificationEntry notificationEntry, NotificationEntry notificationEntry2) {
            Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "a");
            StatusBarNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "a.sbn");
            Intrinsics.checkExpressionValueIsNotNull(notificationEntry2, "b");
            StatusBarNotification sbn2 = notificationEntry2.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn2, "b.sbn");
            NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking, "a.ranking");
            int rank = ranking.getRank();
            NotificationListenerService.Ranking ranking2 = notificationEntry2.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking2, "b.ranking");
            int rank2 = ranking2.getRank();
            boolean access$isColorizedForegroundService = NotificationRankingManagerKt.access$isColorizedForegroundService(notificationEntry);
            boolean access$isColorizedForegroundService2 = NotificationRankingManagerKt.access$isColorizedForegroundService(notificationEntry2);
            int i = this.this$0.getPeopleNotificationType(notificationEntry);
            int i2 = this.this$0.getPeopleNotificationType(notificationEntry2);
            boolean z = this.this$0.isImportantMedia(notificationEntry);
            boolean z2 = this.this$0.isImportantMedia(notificationEntry2);
            boolean access$isSystemMax = NotificationRankingManagerKt.access$isSystemMax(notificationEntry);
            boolean access$isSystemMax2 = NotificationRankingManagerKt.access$isSystemMax(notificationEntry2);
            boolean isRowHeadsUp = notificationEntry.isRowHeadsUp();
            boolean isRowHeadsUp2 = notificationEntry2.isRowHeadsUp();
            boolean z3 = this.this$0.isHighPriority(notificationEntry);
            boolean z4 = this.this$0.isHighPriority(notificationEntry2);
            boolean z5 = notificationEntry.mIsGamingModeNotification;
            boolean z6 = notificationEntry2.mIsGamingModeNotification;
            if (isRowHeadsUp != isRowHeadsUp2) {
                if (!isRowHeadsUp) {
                    return 1;
                }
            } else if (isRowHeadsUp) {
                return this.this$0.headsUpManager.compare(notificationEntry, notificationEntry2);
            } else {
                if (!z5) {
                    if (z6) {
                        return 1;
                    }
                    if (access$isColorizedForegroundService != access$isColorizedForegroundService2) {
                        if (!access$isColorizedForegroundService) {
                            return 1;
                        }
                    } else if ((this.this$0.getUsePeopleFiltering()) && i != i2) {
                        return this.this$0.peopleNotificationIdentifier.compareTo(i, i2);
                    } else {
                        if (z != z2) {
                            if (!z) {
                                return 1;
                            }
                        } else if (access$isSystemMax != access$isSystemMax2) {
                            if (!access$isSystemMax) {
                                return 1;
                            }
                        } else if (z3 != z4) {
                            return Intrinsics.compare(z3 ? 1 : 0, z4 ? 1 : 0) * -1;
                        } else {
                            if (rank != rank2) {
                                return rank - rank2;
                            }
                            return (sbn2.getNotification().when > sbn.getNotification().when ? 1 : (sbn2.getNotification().when == sbn.getNotification().when ? 0 : -1));
                        }
                    }
                }
            }
            return -1;
        }
    };
    @Nullable
    private NotificationListenerService.RankingMap rankingMap;
    private final NotificationSectionsFeatureManager sectionsFeatureManager;

    static {
        PropertyReference1Impl propertyReference1Impl = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(NotificationRankingManager.class), "mediaManager", "getMediaManager()Lcom/android/systemui/statusbar/NotificationMediaManager;");
        Reflection.property1(propertyReference1Impl);
        $$delegatedProperties = new KProperty[]{propertyReference1Impl};
    }

    private final NotificationMediaManager getMediaManager() {
        Lazy lazy = this.mediaManager$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (NotificationMediaManager) lazy.getValue();
    }

    public NotificationRankingManager(@NotNull dagger.Lazy<NotificationMediaManager> lazy, @NotNull NotificationGroupManager notificationGroupManager, @NotNull HeadsUpManager headsUpManager, @NotNull NotificationFilter notificationFilter, @NotNull NotificationEntryManagerLogger notificationEntryManagerLogger, @NotNull NotificationSectionsFeatureManager notificationSectionsFeatureManager, @NotNull PeopleNotificationIdentifier peopleNotificationIdentifier, @NotNull HighPriorityProvider highPriorityProvider) {
        Intrinsics.checkParameterIsNotNull(lazy, "mediaManagerLazy");
        Intrinsics.checkParameterIsNotNull(notificationGroupManager, "groupManager");
        Intrinsics.checkParameterIsNotNull(headsUpManager, "headsUpManager");
        Intrinsics.checkParameterIsNotNull(notificationFilter, "notifFilter");
        Intrinsics.checkParameterIsNotNull(notificationEntryManagerLogger, "logger");
        Intrinsics.checkParameterIsNotNull(notificationSectionsFeatureManager, "sectionsFeatureManager");
        Intrinsics.checkParameterIsNotNull(peopleNotificationIdentifier, "peopleNotificationIdentifier");
        Intrinsics.checkParameterIsNotNull(highPriorityProvider, "highPriorityProvider");
        this.mediaManagerLazy = lazy;
        this.groupManager = notificationGroupManager;
        this.headsUpManager = headsUpManager;
        this.notifFilter = notificationFilter;
        this.logger = notificationEntryManagerLogger;
        this.sectionsFeatureManager = notificationSectionsFeatureManager;
        this.peopleNotificationIdentifier = peopleNotificationIdentifier;
        this.highPriorityProvider = highPriorityProvider;
    }

    @Nullable
    public final NotificationListenerService.RankingMap getRankingMap() {
        return this.rankingMap;
    }

    /* access modifiers changed from: private */
    public final boolean getUsePeopleFiltering() {
        return this.sectionsFeatureManager.isFilteringEnabled();
    }

    @NotNull
    public final List<NotificationEntry> updateRanking(@Nullable NotificationListenerService.RankingMap rankingMap, @NotNull Collection<NotificationEntry> collection, @NotNull String str) {
        List<NotificationEntry> filterAndSortLocked;
        Intrinsics.checkParameterIsNotNull(collection, "entries");
        Intrinsics.checkParameterIsNotNull(str, "reason");
        if (rankingMap != null) {
            this.rankingMap = rankingMap;
            updateRankingForEntries(collection);
        }
        synchronized (this) {
            filterAndSortLocked = filterAndSortLocked(collection, str);
        }
        return filterAndSortLocked;
    }

    private final List<NotificationEntry> filterAndSortLocked(Collection<NotificationEntry> collection, String str) {
        this.logger.logFilterAndSort(str);
        List<NotificationEntry> list = SequencesKt___SequencesKt.toList(SequencesKt___SequencesKt.sortedWith(SequencesKt___SequencesKt.filterNot(CollectionsKt___CollectionsKt.asSequence(collection), new Function1<NotificationEntry, Boolean>(this) { // from class: com.android.systemui.statusbar.notification.collection.NotificationRankingManager$filterAndSortLocked$filtered$1
            @Override // kotlin.jvm.internal.CallableReference
            public final String getName() {
                return "filter";
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final KDeclarationContainer getOwner() {
                return Reflection.getOrCreateKotlinClass(NotificationRankingManager.class);
            }

            @Override // kotlin.jvm.internal.CallableReference
            public final String getSignature() {
                return "filter(Lcom/android/systemui/statusbar/notification/collection/NotificationEntry;)Z";
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Boolean invoke(NotificationEntry notificationEntry) {
                return Boolean.valueOf(invoke(notificationEntry));
            }

            public final boolean invoke(@NotNull NotificationEntry notificationEntry) {
                Intrinsics.checkParameterIsNotNull(notificationEntry, "p1");
                return ((NotificationRankingManager) this.receiver).filter(notificationEntry);
            }
        }), this.rankingComparator));
        for (NotificationEntry notificationEntry : collection) {
            notificationEntry.setBucket(getBucketForEntry(notificationEntry));
        }
        return list;
    }

    /* access modifiers changed from: private */
    public final boolean filter(NotificationEntry notificationEntry) {
        boolean shouldFilterOut = this.notifFilter.shouldFilterOut(notificationEntry);
        if (shouldFilterOut) {
            notificationEntry.resetInitializationTime();
        }
        return shouldFilterOut;
    }

    private final int getBucketForEntry(NotificationEntry notificationEntry) {
        boolean isRowHeadsUp = notificationEntry.isRowHeadsUp();
        boolean isImportantMedia = isImportantMedia(notificationEntry);
        boolean access$isSystemMax = NotificationRankingManagerKt.access$isSystemMax(notificationEntry);
        if (NotificationRankingManagerKt.access$isColorizedForegroundService(notificationEntry)) {
            return 3;
        }
        if (!getUsePeopleFiltering() || !isConversation(notificationEntry)) {
            return (isRowHeadsUp || isImportantMedia || access$isSystemMax || isHighPriority(notificationEntry)) ? 5 : 6;
        }
        return 4;
    }

    private final void updateRankingForEntries(Iterable<NotificationEntry> iterable) {
        NotificationListenerService.RankingMap rankingMap = this.rankingMap;
        if (rankingMap != null) {
            synchronized (iterable) {
                for (NotificationEntry notificationEntry : iterable) {
                    NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
                    if (rankingMap.getRanking(notificationEntry.getKey(), ranking)) {
                        notificationEntry.setRanking(ranking);
                        String overrideGroupKey = ranking.getOverrideGroupKey();
                        StatusBarNotification sbn = notificationEntry.getSbn();
                        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
                        if (!Objects.equals(sbn.getOverrideGroupKey(), overrideGroupKey)) {
                            StatusBarNotification sbn2 = notificationEntry.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
                            String groupKey = sbn2.getGroupKey();
                            StatusBarNotification sbn3 = notificationEntry.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn3, "entry.sbn");
                            boolean isGroup = sbn3.isGroup();
                            StatusBarNotification sbn4 = notificationEntry.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn4, "entry.sbn");
                            Notification notification = sbn4.getNotification();
                            Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
                            boolean isGroupSummary = notification.isGroupSummary();
                            StatusBarNotification sbn5 = notificationEntry.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn5, "entry.sbn");
                            sbn5.setOverrideGroupKey(overrideGroupKey);
                            this.groupManager.onEntryUpdated(notificationEntry, groupKey, isGroup, isGroupSummary);
                        }
                    }
                }
                Unit unit = Unit.INSTANCE;
            }
        }
    }

    /* access modifiers changed from: private */
    public final boolean isImportantMedia(@NotNull NotificationEntry notificationEntry) {
        String key = notificationEntry.getKey();
        NotificationMediaManager mediaManager = getMediaManager();
        Intrinsics.checkExpressionValueIsNotNull(mediaManager, "mediaManager");
        if (Intrinsics.areEqual(key, mediaManager.getMediaNotificationKey())) {
            NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking, "ranking");
            if (ranking.getImportance() > 1) {
                return true;
            }
        }
        return false;
    }

    private final boolean isConversation(@NotNull NotificationEntry notificationEntry) {
        return getPeopleNotificationType(notificationEntry) != 0;
    }

    /* access modifiers changed from: private */
    public final int getPeopleNotificationType(@NotNull NotificationEntry notificationEntry) {
        PeopleNotificationIdentifier peopleNotificationIdentifier = this.peopleNotificationIdentifier;
        StatusBarNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "sbn");
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "ranking");
        return peopleNotificationIdentifier.getPeopleNotificationType(sbn, ranking);
    }

    /* access modifiers changed from: private */
    public final boolean isHighPriority(@NotNull NotificationEntry notificationEntry) {
        return this.highPriorityProvider.isHighPriority(notificationEntry);
    }
}
