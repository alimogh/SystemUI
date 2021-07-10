package com.android.systemui.statusbar.notification.people;

import android.app.NotificationChannel;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
/* compiled from: PeopleNotificationIdentifier.kt */
public final class PeopleNotificationIdentifierImpl implements PeopleNotificationIdentifier {
    private final NotificationGroupManager groupManager;
    private final NotificationPersonExtractor personExtractor;

    public PeopleNotificationIdentifierImpl(@NotNull NotificationPersonExtractor notificationPersonExtractor, @NotNull NotificationGroupManager notificationGroupManager) {
        Intrinsics.checkParameterIsNotNull(notificationPersonExtractor, "personExtractor");
        Intrinsics.checkParameterIsNotNull(notificationGroupManager, "groupManager");
        this.personExtractor = notificationPersonExtractor;
        this.groupManager = notificationGroupManager;
    }

    @Override // com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier
    public int getPeopleNotificationType(@NotNull StatusBarNotification statusBarNotification, @NotNull NotificationListenerService.Ranking ranking) {
        int upperBound;
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        Intrinsics.checkParameterIsNotNull(ranking, "ranking");
        int personTypeInfo = getPersonTypeInfo(ranking);
        if (personTypeInfo == 3 || (upperBound = upperBound(personTypeInfo, extractPersonTypeInfo(statusBarNotification))) == 3) {
            return 3;
        }
        return upperBound(upperBound, getPeopleTypeOfSummary(statusBarNotification));
    }

    @Override // com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier
    public int compareTo(int i, int i2) {
        return Intrinsics.compare(i2, i);
    }

    private final int upperBound(int i, int i2) {
        return Math.max(i, i2);
    }

    private final int getPersonTypeInfo(@NotNull NotificationListenerService.Ranking ranking) {
        if (!ranking.isConversation()) {
            return 0;
        }
        if (ranking.getShortcutInfo() == null) {
            return 1;
        }
        NotificationChannel channel = ranking.getChannel();
        return (channel == null || !channel.isImportantConversation()) ? 2 : 3;
    }

    private final int extractPersonTypeInfo(StatusBarNotification statusBarNotification) {
        return this.personExtractor.isPersonNotification(statusBarNotification) ? 1 : 0;
    }

    private final int getPeopleTypeOfSummary(StatusBarNotification statusBarNotification) {
        Sequence sequence;
        Sequence sequence2;
        int i = 0;
        if (!this.groupManager.isSummaryOfGroup(statusBarNotification)) {
            return 0;
        }
        ArrayList<NotificationEntry> children = this.groupManager.getChildren(statusBarNotification);
        if (!(children == null || (sequence = CollectionsKt___CollectionsKt.asSequence(children)) == null || (sequence2 = SequencesKt___SequencesKt.map(sequence, new Function1<NotificationEntry, Integer>(this) { // from class: com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifierImpl$getPeopleTypeOfSummary$childTypes$1
            final /* synthetic */ PeopleNotificationIdentifierImpl this$0;

            {
                this.this$0 = r1;
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
            @Override // kotlin.jvm.functions.Function1
            public /* bridge */ /* synthetic */ Integer invoke(NotificationEntry notificationEntry) {
                return Integer.valueOf(invoke(notificationEntry));
            }

            public final int invoke(NotificationEntry notificationEntry) {
                PeopleNotificationIdentifierImpl peopleNotificationIdentifierImpl = this.this$0;
                Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "it");
                StatusBarNotification sbn = notificationEntry.getSbn();
                Intrinsics.checkExpressionValueIsNotNull(sbn, "it.sbn");
                NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
                Intrinsics.checkExpressionValueIsNotNull(ranking, "it.ranking");
                return peopleNotificationIdentifierImpl.getPeopleNotificationType(sbn, ranking);
            }
        })) == null)) {
            Iterator it = sequence2.iterator();
            while (it.hasNext() && (i = upperBound(i, ((Number) it.next()).intValue())) != 3) {
            }
        }
        return i;
    }
}
