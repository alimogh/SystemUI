package com.android.systemui.statusbar.notification.collection.notifcollection;

import android.service.notification.NotificationListenerService;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: NotifEvent.kt */
public final class RankingUpdatedEvent extends NotifEvent {
    @NotNull
    private final NotificationListenerService.RankingMap rankingMap;

    public boolean equals(@Nullable Object obj) {
        if (this != obj) {
            return (obj instanceof RankingUpdatedEvent) && Intrinsics.areEqual(this.rankingMap, ((RankingUpdatedEvent) obj).rankingMap);
        }
        return true;
    }

    public int hashCode() {
        NotificationListenerService.RankingMap rankingMap = this.rankingMap;
        if (rankingMap != null) {
            return rankingMap.hashCode();
        }
        return 0;
    }

    @NotNull
    public String toString() {
        return "RankingUpdatedEvent(rankingMap=" + this.rankingMap + ")";
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public RankingUpdatedEvent(@NotNull NotificationListenerService.RankingMap rankingMap) {
        super(null);
        Intrinsics.checkParameterIsNotNull(rankingMap, "rankingMap");
        this.rankingMap = rankingMap;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifEvent
    public void dispatchToListener(@NotNull NotifCollectionListener notifCollectionListener) {
        Intrinsics.checkParameterIsNotNull(notifCollectionListener, "listener");
        notifCollectionListener.onRankingUpdate(this.rankingMap);
    }
}
