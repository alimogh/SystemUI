package com.android.systemui.statusbar.notification.stack;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.statusbar.NotificationRemoveInterceptor;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.DungeonRow;
import com.android.systemui.util.Assert;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ForegroundServiceSectionController.kt */
public final class ForegroundServiceSectionController {
    private final Set<NotificationEntry> entries = new LinkedHashSet();
    private View entriesView;
    @NotNull
    private final NotificationEntryManager entryManager;
    @NotNull
    private final ForegroundServiceDismissalFeatureController featureController;

    public ForegroundServiceSectionController(@NotNull NotificationEntryManager notificationEntryManager, @NotNull ForegroundServiceDismissalFeatureController foregroundServiceDismissalFeatureController) {
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "entryManager");
        Intrinsics.checkParameterIsNotNull(foregroundServiceDismissalFeatureController, "featureController");
        this.entryManager = notificationEntryManager;
        this.featureController = foregroundServiceDismissalFeatureController;
        if (this.featureController.isForegroundServiceDismissalEnabled()) {
            this.entryManager.addNotificationRemoveInterceptor(new NotificationRemoveInterceptor() { // from class: com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController$sam$com_android_systemui_statusbar_NotificationRemoveInterceptor$0
                @Override // com.android.systemui.statusbar.NotificationRemoveInterceptor
                public final /* synthetic */ boolean onNotificationRemoveRequested(@NotNull String str, @Nullable NotificationEntry notificationEntry, int i) {
                    Intrinsics.checkParameterIsNotNull(str, "key");
                    Object invoke = Function3.this.invoke(str, notificationEntry, Integer.valueOf(i));
                    Intrinsics.checkExpressionValueIsNotNull(invoke, "invoke(...)");
                    return ((Boolean) invoke).booleanValue();
                }
            });
            this.entryManager.addNotificationEntryListener(new NotificationEntryListener(this) { // from class: com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController.2
                final /* synthetic */ ForegroundServiceSectionController this$0;

                /* JADX WARN: Incorrect args count in method signature: ()V */
                {
                    this.this$0 = r1;
                }

                @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
                public void onPostEntryUpdated(@NotNull NotificationEntry notificationEntry) {
                    Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
                    if (this.this$0.entries.contains(notificationEntry)) {
                        this.this$0.removeEntry(notificationEntry);
                        this.this$0.addEntry(notificationEntry);
                        this.this$0.update();
                    }
                }
            });
        }
    }

    @NotNull
    public final NotificationEntryManager getEntryManager() {
        return this.entryManager;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final boolean shouldInterceptRemoval(String str, NotificationEntry notificationEntry, int i) {
        Assert.isMainThread();
        boolean z = i == 3;
        boolean z2 = i == 2 || i == 1;
        if (i != 8) {
        }
        boolean z3 = i == 12;
        if (notificationEntry == null) {
            return false;
        }
        if (z2) {
            StatusBarNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
            if (!sbn.isClearable()) {
                if (!hasEntry(notificationEntry)) {
                    addEntry(notificationEntry);
                    update();
                }
                this.entryManager.updateNotifications("FgsSectionController.onNotificationRemoveRequested");
                return true;
            }
        }
        if (z || z3) {
            StatusBarNotification sbn2 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
            if (!sbn2.isClearable()) {
                return true;
            }
        }
        if (hasEntry(notificationEntry)) {
            removeEntry(notificationEntry);
            update();
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void removeEntry(NotificationEntry notificationEntry) {
        Assert.isMainThread();
        this.entries.remove(notificationEntry);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void addEntry(NotificationEntry notificationEntry) {
        Assert.isMainThread();
        this.entries.add(notificationEntry);
    }

    public final boolean hasEntry(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        Assert.isMainThread();
        return this.entries.contains(notificationEntry);
    }

    @NotNull
    public final View createView(@NotNull LayoutInflater layoutInflater) {
        Intrinsics.checkParameterIsNotNull(layoutInflater, "li");
        View inflate = layoutInflater.inflate(C0011R$layout.foreground_service_dungeon, (ViewGroup) null);
        this.entriesView = inflate;
        if (inflate != null) {
            inflate.setVisibility(8);
            View view = this.entriesView;
            if (view != null) {
                return view;
            }
            Intrinsics.throwNpe();
            throw null;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void update() {
        Assert.isMainThread();
        View view = this.entriesView;
        if (view == null) {
            throw new IllegalStateException("ForegroundServiceSectionController is trying to show dismissed fgs notifications without having been initialized!");
        } else if (view != null) {
            View findViewById = view.findViewById(C0008R$id.entry_list);
            if (findViewById != null) {
                LinearLayout linearLayout = (LinearLayout) findViewById;
                linearLayout.removeAllViews();
                for (NotificationEntry notificationEntry : CollectionsKt___CollectionsKt.sortedWith(this.entries, new Comparator<T>() { // from class: com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController$$special$$inlined$sortedBy$1
                    @Override // java.util.Comparator
                    public final int compare(T t, T t2) {
                        NotificationListenerService.Ranking ranking = t.getRanking();
                        Intrinsics.checkExpressionValueIsNotNull(ranking, "it.ranking");
                        Integer valueOf = Integer.valueOf(ranking.getRank());
                        NotificationListenerService.Ranking ranking2 = t2.getRanking();
                        Intrinsics.checkExpressionValueIsNotNull(ranking2, "it.ranking");
                        return ComparisonsKt.compareValues(valueOf, Integer.valueOf(ranking2.getRank()));
                    }
                })) {
                    View inflate = LayoutInflater.from(linearLayout.getContext()).inflate(C0011R$layout.foreground_service_dungeon_row, (ViewGroup) null);
                    if (inflate != null) {
                        DungeonRow dungeonRow = (DungeonRow) inflate;
                        dungeonRow.setEntry(notificationEntry);
                        dungeonRow.setOnClickListener(new View.OnClickListener(dungeonRow, notificationEntry, linearLayout, this) { // from class: com.android.systemui.statusbar.notification.stack.ForegroundServiceSectionController$update$$inlined$apply$lambda$1
                            final /* synthetic */ DungeonRow $child;
                            final /* synthetic */ NotificationEntry $entry;
                            final /* synthetic */ ForegroundServiceSectionController this$0;

                            {
                                this.$child = r1;
                                this.$entry = r2;
                                this.this$0 = r4;
                            }

                            @Override // android.view.View.OnClickListener
                            public final void onClick(View view2) {
                                ForegroundServiceSectionController foregroundServiceSectionController = this.this$0;
                                NotificationEntry entry = this.$child.getEntry();
                                if (entry != null) {
                                    foregroundServiceSectionController.removeEntry(entry);
                                    this.this$0.update();
                                    this.$entry.getRow().unDismiss();
                                    this.$entry.getRow().resetTranslation();
                                    this.this$0.getEntryManager().updateNotifications("ForegroundServiceSectionController.onClick");
                                    return;
                                }
                                Intrinsics.throwNpe();
                                throw null;
                            }
                        });
                        linearLayout.addView(dungeonRow);
                    } else {
                        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.DungeonRow");
                    }
                }
                if (this.entries.isEmpty()) {
                    View view2 = this.entriesView;
                    if (view2 != null) {
                        view2.setVisibility(8);
                        return;
                    }
                    return;
                }
                View view3 = this.entriesView;
                if (view3 != null) {
                    view3.setVisibility(0);
                    return;
                }
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.widget.LinearLayout");
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
