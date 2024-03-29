package com.android.systemui.statusbar.notification.stack;

import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.media.KeyguardMediaController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapter;
import com.android.systemui.statusbar.notification.people.Subscription;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.ConvenienceExtensionsKt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.NoWhenBranchMatchedException;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.collections.Grouping;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: NotificationSectionsManager.kt */
public final class NotificationSectionsManager implements StackScrollAlgorithm.SectionProvider {
    private final ActivityStarter activityStarter;
    @Nullable
    private SectionHeaderView alertingHeaderView;
    private final ConfigurationController configurationController;
    private final NotificationSectionsManager$configurationListener$1 configurationListener = new NotificationSectionsManager$configurationListener$1(this);
    @Nullable
    private SectionHeaderView incomingHeaderView;
    private boolean initialized;
    private final KeyguardMediaController keyguardMediaController;
    private final NotificationSectionsLogger logger;
    @Nullable
    private MediaHeaderView mediaControlsView;
    private View.OnClickListener onClearSilentNotifsClickListener;
    private NotificationStackScrollLayout parent;
    @Nullable
    private PeopleHubView peopleHeaderView;
    private Subscription peopleHubSubscription;
    private boolean peopleHubVisible;
    private final NotificationSectionsFeatureManager sectionsFeatureManager;
    @Nullable
    private SectionHeaderView silentHeaderView;
    private final StatusBarStateController statusBarStateController;

    /* compiled from: NotificationSectionsManager.kt */
    /* access modifiers changed from: private */
    public interface SectionUpdateState<T extends ExpandableView> {
        void adjustViewPosition();

        @Nullable
        Integer getCurrentPosition();

        @Nullable
        Integer getTargetPosition();

        void setCurrentPosition(@Nullable Integer num);

        void setTargetPosition(@Nullable Integer num);
    }

    public final void setHeaderForegroundColor(int i) {
    }

    public NotificationSectionsManager(@NotNull ActivityStarter activityStarter, @NotNull StatusBarStateController statusBarStateController, @NotNull ConfigurationController configurationController, @NotNull PeopleHubViewAdapter peopleHubViewAdapter, @NotNull KeyguardMediaController keyguardMediaController, @NotNull NotificationSectionsFeatureManager notificationSectionsFeatureManager, @NotNull NotificationSectionsLogger notificationSectionsLogger) {
        Intrinsics.checkParameterIsNotNull(activityStarter, "activityStarter");
        Intrinsics.checkParameterIsNotNull(statusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(configurationController, "configurationController");
        Intrinsics.checkParameterIsNotNull(peopleHubViewAdapter, "peopleHubViewAdapter");
        Intrinsics.checkParameterIsNotNull(keyguardMediaController, "keyguardMediaController");
        Intrinsics.checkParameterIsNotNull(notificationSectionsFeatureManager, "sectionsFeatureManager");
        Intrinsics.checkParameterIsNotNull(notificationSectionsLogger, "logger");
        this.activityStarter = activityStarter;
        this.statusBarStateController = statusBarStateController;
        this.configurationController = configurationController;
        this.keyguardMediaController = keyguardMediaController;
        this.sectionsFeatureManager = notificationSectionsFeatureManager;
        this.logger = notificationSectionsLogger;
    }

    public static final /* synthetic */ NotificationStackScrollLayout access$getParent$p(NotificationSectionsManager notificationSectionsManager) {
        NotificationStackScrollLayout notificationStackScrollLayout = notificationSectionsManager.parent;
        if (notificationStackScrollLayout != null) {
            return notificationStackScrollLayout;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    @VisibleForTesting
    @Nullable
    public final SectionHeaderView getSilentHeaderView() {
        return this.silentHeaderView;
    }

    @VisibleForTesting
    @Nullable
    public final SectionHeaderView getAlertingHeaderView() {
        return this.alertingHeaderView;
    }

    @VisibleForTesting
    @Nullable
    public final SectionHeaderView getIncomingHeaderView() {
        return this.incomingHeaderView;
    }

    @VisibleForTesting
    @Nullable
    public final PeopleHubView getPeopleHeaderView() {
        return this.peopleHeaderView;
    }

    @VisibleForTesting
    public final void setPeopleHubVisible(boolean z) {
        this.peopleHubVisible = z;
    }

    @VisibleForTesting
    @Nullable
    public final MediaHeaderView getMediaControlsView() {
        return this.mediaControlsView;
    }

    public final void initialize(@NotNull NotificationStackScrollLayout notificationStackScrollLayout, @NotNull LayoutInflater layoutInflater) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "parent");
        Intrinsics.checkParameterIsNotNull(layoutInflater, "layoutInflater");
        if (!this.initialized) {
            this.initialized = true;
            this.parent = notificationStackScrollLayout;
            reinflateViews(layoutInflater);
            this.configurationController.addCallback(this.configurationListener);
            return;
        }
        throw new IllegalStateException("NotificationSectionsManager already initialized".toString());
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0058  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final <T extends com.android.systemui.statusbar.notification.row.ExpandableView> T reinflateView(T r6, android.view.LayoutInflater r7, int r8) {
        /*
            r5 = this;
            r0 = -1
            r1 = 0
            java.lang.String r2 = "parent"
            if (r6 == 0) goto L_0x0033
            android.view.ViewGroup r3 = r6.getTransientContainer()
            if (r3 == 0) goto L_0x000f
            r3.removeView(r6)
        L_0x000f:
            android.view.ViewParent r3 = r6.getParent()
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r4 = r5.parent
            if (r4 == 0) goto L_0x002f
            if (r3 != r4) goto L_0x0033
            if (r4 == 0) goto L_0x002b
            int r3 = r4.indexOfChild(r6)
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r4 = r5.parent
            if (r4 == 0) goto L_0x0027
            r4.removeView(r6)
            goto L_0x0034
        L_0x0027:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x002b:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x002f:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x0033:
            r3 = r0
        L_0x0034:
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r6 = r5.parent
            if (r6 == 0) goto L_0x0058
            r4 = 0
            android.view.View r6 = r7.inflate(r8, r6, r4)
            if (r6 == 0) goto L_0x0050
            com.android.systemui.statusbar.notification.row.ExpandableView r6 = (com.android.systemui.statusbar.notification.row.ExpandableView) r6
            if (r3 == r0) goto L_0x004f
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r5 = r5.parent
            if (r5 == 0) goto L_0x004b
            r5.addView(r6, r3)
            goto L_0x004f
        L_0x004b:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x004f:
            return r6
        L_0x0050:
            kotlin.TypeCastException r5 = new kotlin.TypeCastException
            java.lang.String r6 = "null cannot be cast to non-null type T"
            r5.<init>(r6)
            throw r5
        L_0x0058:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.reinflateView(com.android.systemui.statusbar.notification.row.ExpandableView, android.view.LayoutInflater, int):com.android.systemui.statusbar.notification.row.ExpandableView");
    }

    @NotNull
    public final NotificationSection[] createSectionsForBuckets() {
        int[] notificationBuckets = this.sectionsFeatureManager.getNotificationBuckets();
        ArrayList arrayList = new ArrayList(notificationBuckets.length);
        for (int i : notificationBuckets) {
            NotificationStackScrollLayout notificationStackScrollLayout = this.parent;
            if (notificationStackScrollLayout != null) {
                arrayList.add(new NotificationSection(notificationStackScrollLayout, i));
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("parent");
                throw null;
            }
        }
        Object[] array = arrayList.toArray(new NotificationSection[0]);
        if (array != null) {
            return (NotificationSection[]) array;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<T>");
    }

    public final void reinflateViews(@NotNull LayoutInflater layoutInflater) {
        Intrinsics.checkParameterIsNotNull(layoutInflater, "layoutInflater");
        SectionHeaderView sectionHeaderView = (SectionHeaderView) reinflateView(this.silentHeaderView, layoutInflater, C0011R$layout.status_bar_notification_section_header);
        sectionHeaderView.setHeaderText(C0015R$string.op_notification_section_header_gentle);
        sectionHeaderView.setOnHeaderClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$1
            final /* synthetic */ NotificationSectionsManager this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.onGentleHeaderClick();
            }
        });
        sectionHeaderView.setOnClearAllClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$2
            final /* synthetic */ NotificationSectionsManager this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                NotificationSectionsManager notificationSectionsManager = this.this$0;
                Intrinsics.checkExpressionValueIsNotNull(view, "it");
                notificationSectionsManager.onClearGentleNotifsClick(view);
            }
        });
        this.silentHeaderView = sectionHeaderView;
        SectionHeaderView sectionHeaderView2 = (SectionHeaderView) reinflateView(this.alertingHeaderView, layoutInflater, C0011R$layout.status_bar_notification_section_header);
        sectionHeaderView2.setHeaderText(C0015R$string.notification_section_header_alerting);
        sectionHeaderView2.setOnHeaderClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$3
            final /* synthetic */ NotificationSectionsManager this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.onGentleHeaderClick();
            }
        });
        this.alertingHeaderView = sectionHeaderView2;
        Subscription subscription = this.peopleHubSubscription;
        if (subscription != null) {
            subscription.unsubscribe();
        }
        this.peopleHubSubscription = null;
        this.peopleHeaderView = (PeopleHubView) reinflateView(this.peopleHeaderView, layoutInflater, C0011R$layout.people_strip);
        SectionHeaderView sectionHeaderView3 = (SectionHeaderView) reinflateView(this.incomingHeaderView, layoutInflater, C0011R$layout.status_bar_notification_section_header);
        sectionHeaderView3.setHeaderText(C0015R$string.notification_section_header_incoming);
        sectionHeaderView3.setOnHeaderClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$4
            final /* synthetic */ NotificationSectionsManager this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.this$0.onGentleHeaderClick();
            }
        });
        this.incomingHeaderView = sectionHeaderView3;
        MediaHeaderView mediaHeaderView = (MediaHeaderView) reinflateView(this.mediaControlsView, layoutInflater, C0011R$layout.keyguard_media_header);
        this.keyguardMediaController.attach(mediaHeaderView);
        this.mediaControlsView = mediaHeaderView;
    }

    @Override // com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm.SectionProvider
    public boolean beginsSection(@NotNull View view, @Nullable View view2) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        return view == this.silentHeaderView || view == this.mediaControlsView || view == this.peopleHeaderView || view == this.alertingHeaderView || view == this.incomingHeaderView || (Intrinsics.areEqual(getBucket(view), getBucket(view2)) ^ true);
    }

    /* access modifiers changed from: private */
    public final Integer getBucket(View view) {
        if (view == this.silentHeaderView) {
            return 6;
        }
        if (view == this.incomingHeaderView) {
            return 2;
        }
        if (view == this.mediaControlsView) {
            return 1;
        }
        if (view == this.peopleHeaderView) {
            return 4;
        }
        if (view == this.alertingHeaderView) {
            return 5;
        }
        if (!(view instanceof ExpandableNotificationRow)) {
            return null;
        }
        NotificationEntry entry = ((ExpandableNotificationRow) view).getEntry();
        Intrinsics.checkExpressionValueIsNotNull(entry, "view.entry");
        return Integer.valueOf(entry.getBucket());
    }

    private final void logShadeChild(int i, View view) {
        if (view == this.incomingHeaderView) {
            this.logger.logIncomingHeader(i);
        } else if (view == this.mediaControlsView) {
            this.logger.logMediaControls(i);
        } else if (view == this.peopleHeaderView) {
            this.logger.logConversationsHeader(i);
        } else if (view == this.alertingHeaderView) {
            this.logger.logAlertingHeader(i);
        } else if (view == this.silentHeaderView) {
            this.logger.logSilentHeader(i);
        } else if (!(view instanceof ExpandableNotificationRow)) {
            this.logger.logOther(i, view.getClass());
        } else {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            boolean isHeadsUp = expandableNotificationRow.isHeadsUp();
            NotificationEntry entry = expandableNotificationRow.getEntry();
            Intrinsics.checkExpressionValueIsNotNull(entry, "child.entry");
            int bucket = entry.getBucket();
            if (bucket == 2) {
                this.logger.logHeadsUp(i, isHeadsUp);
            } else if (bucket == 4) {
                this.logger.logConversation(i, isHeadsUp);
            } else if (bucket == 5) {
                this.logger.logAlerting(i, isHeadsUp);
            } else if (bucket == 6) {
                this.logger.logSilent(i, isHeadsUp);
            }
        }
    }

    private final void logShadeContents() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.parent;
        if (notificationStackScrollLayout != null) {
            int i = 0;
            for (View view : ConvenienceExtensionsKt.getChildren(notificationStackScrollLayout)) {
                int i2 = i + 1;
                if (i >= 0) {
                    logShadeChild(i, view);
                    i = i2;
                } else {
                    CollectionsKt.throwIndexOverflow();
                    throw null;
                }
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    private final boolean isUsingMultipleSections() {
        return this.sectionsFeatureManager.getNumberOfBuckets() > 1;
    }

    @VisibleForTesting
    public final void updateSectionBoundaries() {
        updateSectionBoundaries("test");
    }

    private final <T extends ExpandableView> SectionUpdateState<T> expandableViewHeaderState(T t) {
        return new SectionUpdateState<T>(this, t) { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$expandableViewHeaderState$1
            final /* synthetic */ ExpandableView $header;
            @Nullable
            private Integer currentPosition;
            @Nullable
            private Integer targetPosition;
            final /* synthetic */ NotificationSectionsManager this$0;

            {
                this.this$0 = r1;
                this.$header = r2;
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            @Nullable
            public Integer getCurrentPosition() {
                return this.currentPosition;
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            public void setCurrentPosition(@Nullable Integer num) {
                this.currentPosition = num;
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            @Nullable
            public Integer getTargetPosition() {
                return this.targetPosition;
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            public void setTargetPosition(@Nullable Integer num) {
                this.targetPosition = num;
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState
            public void adjustViewPosition() {
                Integer targetPosition = getTargetPosition();
                Integer currentPosition = getCurrentPosition();
                if (targetPosition == null) {
                    if (currentPosition != null) {
                        NotificationSectionsManager.access$getParent$p(this.this$0).removeView(this.$header);
                    }
                } else if (currentPosition == null) {
                    ViewGroup transientContainer = this.$header.getTransientContainer();
                    if (transientContainer != null) {
                        transientContainer.removeTransientView(this.$header);
                    }
                    this.$header.setTransientContainer(null);
                    NotificationSectionsManager.access$getParent$p(this.this$0).addView(this.$header, targetPosition.intValue());
                } else {
                    NotificationSectionsManager.access$getParent$p(this.this$0).changeViewPosition(this.$header, targetPosition.intValue());
                }
            }
        };
    }

    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0118, code lost:
        if ((r2.getVisibility() == 8) == false) goto L_0x011d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x016f, code lost:
        if (r8.intValue() != r4.getBucket()) goto L_0x0171;
     */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x01a6  */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x01a8  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x01d2  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x01eb  */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x01fe  */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x0266  */
    /* JADX WARNING: Removed duplicated region for block: B:164:0x027c  */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x028f  */
    /* JADX WARNING: Removed duplicated region for block: B:176:0x02a2  */
    /* JADX WARNING: Removed duplicated region for block: B:182:0x02b5  */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x02e7 A[LOOP:2: B:192:0x02e1->B:194:0x02e7, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x0306  */
    /* JADX WARNING: Removed duplicated region for block: B:204:0x031e  */
    /* JADX WARNING: Removed duplicated region for block: B:231:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0145  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x015c  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x017b  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x0184  */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x0191  */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x019a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void updateSectionBoundaries(@org.jetbrains.annotations.NotNull java.lang.String r26) {
        /*
        // Method dump skipped, instructions count: 848
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.updateSectionBoundaries(java.lang.String):void");
    }

    public final void onUiModeChanged() {
        PeopleHubView peopleHubView = this.peopleHeaderView;
        if (peopleHubView != null) {
            peopleHubView.onUiModeChanged();
        }
        SectionHeaderView sectionHeaderView = this.silentHeaderView;
        if (sectionHeaderView != null) {
            sectionHeaderView.onUiModeChanged();
        }
        SectionHeaderView sectionHeaderView2 = this.alertingHeaderView;
        if (sectionHeaderView2 != null) {
            sectionHeaderView2.onUiModeChanged();
        }
        SectionHeaderView sectionHeaderView3 = this.incomingHeaderView;
        if (sectionHeaderView3 != null) {
            sectionHeaderView3.onUiModeChanged();
        }
    }

    /* compiled from: NotificationSectionsManager.kt */
    private static abstract class SectionBounds {
        private SectionBounds() {
        }

        public /* synthetic */ SectionBounds(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        /* compiled from: NotificationSectionsManager.kt */
        public static final class Many extends SectionBounds {
            @NotNull
            private final ExpandableView first;
            @NotNull
            private final ExpandableView last;

            public static /* synthetic */ Many copy$default(Many many, ExpandableView expandableView, ExpandableView expandableView2, int i, Object obj) {
                if ((i & 1) != 0) {
                    expandableView = many.first;
                }
                if ((i & 2) != 0) {
                    expandableView2 = many.last;
                }
                return many.copy(expandableView, expandableView2);
            }

            @NotNull
            public final Many copy(@NotNull ExpandableView expandableView, @NotNull ExpandableView expandableView2) {
                Intrinsics.checkParameterIsNotNull(expandableView, "first");
                Intrinsics.checkParameterIsNotNull(expandableView2, "last");
                return new Many(expandableView, expandableView2);
            }

            public boolean equals(@Nullable Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof Many)) {
                    return false;
                }
                Many many = (Many) obj;
                return Intrinsics.areEqual(this.first, many.first) && Intrinsics.areEqual(this.last, many.last);
            }

            public int hashCode() {
                ExpandableView expandableView = this.first;
                int i = 0;
                int hashCode = (expandableView != null ? expandableView.hashCode() : 0) * 31;
                ExpandableView expandableView2 = this.last;
                if (expandableView2 != null) {
                    i = expandableView2.hashCode();
                }
                return hashCode + i;
            }

            @NotNull
            public String toString() {
                return "Many(first=" + this.first + ", last=" + this.last + ")";
            }

            @NotNull
            public final ExpandableView getFirst() {
                return this.first;
            }

            @NotNull
            public final ExpandableView getLast() {
                return this.last;
            }

            /* JADX INFO: super call moved to the top of the method (can break code semantics) */
            public Many(@NotNull ExpandableView expandableView, @NotNull ExpandableView expandableView2) {
                super(null);
                Intrinsics.checkParameterIsNotNull(expandableView, "first");
                Intrinsics.checkParameterIsNotNull(expandableView2, "last");
                this.first = expandableView;
                this.last = expandableView2;
            }
        }

        /* compiled from: NotificationSectionsManager.kt */
        public static final class One extends SectionBounds {
            @NotNull
            private final ExpandableView lone;

            public boolean equals(@Nullable Object obj) {
                if (this != obj) {
                    return (obj instanceof One) && Intrinsics.areEqual(this.lone, ((One) obj).lone);
                }
                return true;
            }

            public int hashCode() {
                ExpandableView expandableView = this.lone;
                if (expandableView != null) {
                    return expandableView.hashCode();
                }
                return 0;
            }

            @NotNull
            public String toString() {
                return "One(lone=" + this.lone + ")";
            }

            /* JADX INFO: super call moved to the top of the method (can break code semantics) */
            public One(@NotNull ExpandableView expandableView) {
                super(null);
                Intrinsics.checkParameterIsNotNull(expandableView, "lone");
                this.lone = expandableView;
            }

            @NotNull
            public final ExpandableView getLone() {
                return this.lone;
            }
        }

        /* compiled from: NotificationSectionsManager.kt */
        public static final class None extends SectionBounds {
            public static final None INSTANCE = new None();

            private None() {
                super(null);
            }
        }

        @NotNull
        public final SectionBounds addNotif(@NotNull ExpandableView expandableView) {
            Intrinsics.checkParameterIsNotNull(expandableView, "notif");
            if (this instanceof None) {
                return new One(expandableView);
            }
            if (this instanceof One) {
                return new Many(((One) this).getLone(), expandableView);
            }
            if (this instanceof Many) {
                return Many.copy$default((Many) this, null, expandableView, 1, null);
            }
            throw new NoWhenBranchMatchedException();
        }

        public final boolean updateSection(@NotNull NotificationSection notificationSection) {
            Intrinsics.checkParameterIsNotNull(notificationSection, "section");
            if (this instanceof None) {
                return setFirstAndLastVisibleChildren(notificationSection, null, null);
            }
            if (this instanceof One) {
                One one = (One) this;
                return setFirstAndLastVisibleChildren(notificationSection, one.getLone(), one.getLone());
            } else if (this instanceof Many) {
                Many many = (Many) this;
                return setFirstAndLastVisibleChildren(notificationSection, many.getFirst(), many.getLast());
            } else {
                throw new NoWhenBranchMatchedException();
            }
        }

        private final boolean setFirstAndLastVisibleChildren(@NotNull NotificationSection notificationSection, ExpandableView expandableView, ExpandableView expandableView2) {
            return notificationSection.setFirstVisibleChild(expandableView) || notificationSection.setLastVisibleChild(expandableView2);
        }
    }

    public final boolean updateFirstAndLastViewsForAllSections(@NotNull NotificationSection[] notificationSectionArr, @NotNull List<? extends ExpandableView> list) {
        SparseArray sparseArray;
        Intrinsics.checkParameterIsNotNull(notificationSectionArr, "sections");
        Intrinsics.checkParameterIsNotNull(list, "children");
        NotificationSectionsManager$updateFirstAndLastViewsForAllSections$$inlined$groupingBy$1 notificationSectionsManager$updateFirstAndLastViewsForAllSections$$inlined$groupingBy$1 = new Grouping<ExpandableView, Integer>(CollectionsKt___CollectionsKt.asSequence(list), this) { // from class: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$updateFirstAndLastViewsForAllSections$$inlined$groupingBy$1
            final /* synthetic */ Sequence $this_groupingBy;
            final /* synthetic */ NotificationSectionsManager this$0;

            {
                this.$this_groupingBy = r1;
                this.this$0 = r2;
            }

            @Override // kotlin.collections.Grouping
            @NotNull
            public Iterator<ExpandableView> sourceIterator() {
                return this.$this_groupingBy.iterator();
            }

            @Override // kotlin.collections.Grouping
            public Integer keyOf(ExpandableView expandableView) {
                Integer num = this.this$0.getBucket(expandableView);
                if (num != null) {
                    return Integer.valueOf(num.intValue());
                }
                throw new IllegalArgumentException("Cannot find section bucket for view");
            }
        };
        SectionBounds.None none = SectionBounds.None.INSTANCE;
        int length = notificationSectionArr.length;
        if (length < 0) {
            sparseArray = new SparseArray();
        } else {
            sparseArray = new SparseArray(length);
        }
        Iterator<ExpandableView> sourceIterator = notificationSectionsManager$updateFirstAndLastViewsForAllSections$$inlined$groupingBy$1.sourceIterator();
        while (sourceIterator.hasNext()) {
            ExpandableView next = sourceIterator.next();
            int intValue = notificationSectionsManager$updateFirstAndLastViewsForAllSections$$inlined$groupingBy$1.keyOf(next).intValue();
            Object obj = sparseArray.get(intValue);
            if (obj == null) {
                obj = none;
            }
            sparseArray.put(intValue, ((SectionBounds) obj).addNotif(next));
        }
        boolean z = false;
        for (NotificationSection notificationSection : notificationSectionArr) {
            SectionBounds sectionBounds = (SectionBounds) sparseArray.get(notificationSection.getBucket());
            if (sectionBounds == null) {
                sectionBounds = SectionBounds.None.INSTANCE;
            }
            z = sectionBounds.updateSection(notificationSection) || z;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public final void onGentleHeaderClick() {
        this.activityStarter.startActivity(new Intent("android.settings.NOTIFICATION_SETTINGS"), true, true, 536870912);
    }

    /* access modifiers changed from: private */
    public final void onClearGentleNotifsClick(View view) {
        View.OnClickListener onClickListener = this.onClearSilentNotifsClickListener;
        if (onClickListener != null) {
            onClickListener.onClick(view);
        }
    }

    public final void setOnClearSilentNotifsClickListener(@NotNull View.OnClickListener onClickListener) {
        Intrinsics.checkParameterIsNotNull(onClickListener, "listener");
        this.onClearSilentNotifsClickListener = onClickListener;
    }

    public final void hidePeopleRow() {
        this.peopleHubVisible = false;
        updateSectionBoundaries("PeopleHub dismissed");
    }
}
