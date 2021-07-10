package com.android.systemui.statusbar.notification.icon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.Person;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.ImageView;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.C0008R$id;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.InflationException;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import java.util.List;
import kotlin.Pair;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: IconManager.kt */
public final class IconManager {
    private final IconManager$entryListener$1 entryListener = new IconManager$entryListener$1(this);
    private final IconBuilder iconBuilder;
    private final LauncherApps launcherApps;
    private final CommonNotifCollection notifCollection;
    private final NotificationEntry.OnSensitivityChangedListener sensitivityListener = new NotificationEntry.OnSensitivityChangedListener(this) { // from class: com.android.systemui.statusbar.notification.icon.IconManager$sensitivityListener$1
        final /* synthetic */ IconManager this$0;

        {
            this.this$0 = r1;
        }

        @Override // com.android.systemui.statusbar.notification.collection.NotificationEntry.OnSensitivityChangedListener
        public final void onSensitivityChanged(@NotNull NotificationEntry notificationEntry) {
            Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
            this.this$0.updateIconsSafe(notificationEntry);
        }
    };

    public IconManager(@NotNull CommonNotifCollection commonNotifCollection, @NotNull LauncherApps launcherApps, @NotNull IconBuilder iconBuilder) {
        Intrinsics.checkParameterIsNotNull(commonNotifCollection, "notifCollection");
        Intrinsics.checkParameterIsNotNull(launcherApps, "launcherApps");
        Intrinsics.checkParameterIsNotNull(iconBuilder, "iconBuilder");
        this.notifCollection = commonNotifCollection;
        this.launcherApps = launcherApps;
        this.iconBuilder = iconBuilder;
    }

    public final void attach() {
        this.notifCollection.addCollectionListener(this.entryListener);
    }

    public final void createIcons(@NotNull NotificationEntry notificationEntry) throws InflationException {
        StatusBarIconView statusBarIconView;
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        StatusBarIconView createIconView = this.iconBuilder.createIconView(notificationEntry);
        createIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        StatusBarIconView createIconView2 = this.iconBuilder.createIconView(notificationEntry);
        createIconView2.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        createIconView2.setOnVisibilityChangedListener(new StatusBarIconView.OnVisibilityChangedListener(notificationEntry) { // from class: com.android.systemui.statusbar.notification.icon.IconManager$createIcons$1
            final /* synthetic */ NotificationEntry $entry;

            {
                this.$entry = r1;
            }

            @Override // com.android.systemui.statusbar.StatusBarIconView.OnVisibilityChangedListener
            public final void onVisibilityChanged(int i) {
                this.$entry.setShelfIconVisible(i == 0);
            }
        });
        createIconView2.setVisibility(4);
        StatusBarIconView createIconView3 = this.iconBuilder.createIconView(notificationEntry);
        createIconView3.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        createIconView3.setIncreasedSize(true);
        StatusBarNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
        Notification notification = sbn.getNotification();
        Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
        if (notification.isMediaNotification()) {
            statusBarIconView = this.iconBuilder.createIconView(notificationEntry);
            statusBarIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            statusBarIconView = null;
        }
        Pair<StatusBarIcon, StatusBarIcon> iconDescriptors = getIconDescriptors(notificationEntry);
        StatusBarIcon component1 = iconDescriptors.component1();
        StatusBarIcon component2 = iconDescriptors.component2();
        try {
            setIcon(notificationEntry, component1, createIconView);
            setIcon(notificationEntry, component2, createIconView2);
            setIcon(notificationEntry, component2, createIconView3);
            if (statusBarIconView != null) {
                setIcon(notificationEntry, component1, statusBarIconView);
            }
            notificationEntry.setIcons(IconPack.buildPack(createIconView, createIconView2, createIconView3, statusBarIconView, notificationEntry.getIcons()));
        } catch (InflationException e) {
            notificationEntry.setIcons(IconPack.buildEmptyPack(notificationEntry.getIcons()));
            throw e;
        }
    }

    public final void updateIcons(@NotNull NotificationEntry notificationEntry) throws InflationException {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        IconPack icons = notificationEntry.getIcons();
        Intrinsics.checkExpressionValueIsNotNull(icons, "entry.icons");
        if (icons.getAreIconsAvailable()) {
            IconPack icons2 = notificationEntry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons2, "entry.icons");
            icons2.setSmallIconDescriptor(null);
            IconPack icons3 = notificationEntry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons3, "entry.icons");
            icons3.setPeopleAvatarDescriptor(null);
            Pair<StatusBarIcon, StatusBarIcon> iconDescriptors = getIconDescriptors(notificationEntry);
            StatusBarIcon component1 = iconDescriptors.component1();
            StatusBarIcon component2 = iconDescriptors.component2();
            IconPack icons4 = notificationEntry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons4, "entry.icons");
            StatusBarIconView statusBarIcon = icons4.getStatusBarIcon();
            if (statusBarIcon != null) {
                Intrinsics.checkExpressionValueIsNotNull(statusBarIcon, "it");
                statusBarIcon.setNotification(notificationEntry.getSbn());
                setIcon(notificationEntry, component1, statusBarIcon);
            }
            IconPack icons5 = notificationEntry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons5, "entry.icons");
            StatusBarIconView shelfIcon = icons5.getShelfIcon();
            if (shelfIcon != null) {
                Intrinsics.checkExpressionValueIsNotNull(shelfIcon, "it");
                shelfIcon.setNotification(notificationEntry.getSbn());
                setIcon(notificationEntry, component1, shelfIcon);
            }
            IconPack icons6 = notificationEntry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons6, "entry.icons");
            StatusBarIconView aodIcon = icons6.getAodIcon();
            if (aodIcon != null) {
                Intrinsics.checkExpressionValueIsNotNull(aodIcon, "it");
                aodIcon.setNotification(notificationEntry.getSbn());
                setIcon(notificationEntry, component2, aodIcon);
            }
            IconPack icons7 = notificationEntry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons7, "entry.icons");
            StatusBarIconView centeredIcon = icons7.getCenteredIcon();
            if (centeredIcon != null) {
                Intrinsics.checkExpressionValueIsNotNull(centeredIcon, "it");
                centeredIcon.setNotification(notificationEntry.getSbn());
                setIcon(notificationEntry, component2, centeredIcon);
            }
        }
    }

    /* access modifiers changed from: private */
    public final void updateIconsSafe(NotificationEntry notificationEntry) {
        try {
            updateIcons(notificationEntry);
        } catch (InflationException e) {
            Log.e("IconManager", "Unable to update icon", e);
        }
    }

    private final Pair<StatusBarIcon, StatusBarIcon> getIconDescriptors(NotificationEntry notificationEntry) throws InflationException {
        StatusBarIcon iconDescriptor = getIconDescriptor(notificationEntry, false);
        return new Pair<>(iconDescriptor, notificationEntry.isSensitive() ? getIconDescriptor(notificationEntry, true) : iconDescriptor);
    }

    private final StatusBarIcon getIconDescriptor(NotificationEntry notificationEntry, boolean z) throws InflationException {
        Icon icon;
        StatusBarNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
        Notification notification = sbn.getNotification();
        boolean z2 = isImportantConversation(notificationEntry) && !z;
        IconPack icons = notificationEntry.getIcons();
        Intrinsics.checkExpressionValueIsNotNull(icons, "entry.icons");
        StatusBarIcon peopleAvatarDescriptor = icons.getPeopleAvatarDescriptor();
        IconPack icons2 = notificationEntry.getIcons();
        Intrinsics.checkExpressionValueIsNotNull(icons2, "entry.icons");
        StatusBarIcon smallIconDescriptor = icons2.getSmallIconDescriptor();
        if (z2 && peopleAvatarDescriptor != null) {
            return peopleAvatarDescriptor;
        }
        if (!z2 && smallIconDescriptor != null) {
            return smallIconDescriptor;
        }
        if (z2) {
            icon = createPeopleAvatar(notificationEntry);
        } else {
            Intrinsics.checkExpressionValueIsNotNull(notification, "n");
            icon = notification.getSmallIcon();
        }
        if (icon != null) {
            StatusBarNotification sbn2 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
            UserHandle user = sbn2.getUser();
            StatusBarNotification sbn3 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn3, "entry.sbn");
            String packageName = sbn3.getPackageName();
            int i = notification.iconLevel;
            int i2 = notification.number;
            IconBuilder iconBuilder = this.iconBuilder;
            Intrinsics.checkExpressionValueIsNotNull(notification, "n");
            StatusBarIcon statusBarIcon = new StatusBarIcon(user, packageName, icon, i, i2, iconBuilder.getIconContentDescription(notification));
            if (isImportantConversation(notificationEntry)) {
                if (z2) {
                    IconPack icons3 = notificationEntry.getIcons();
                    Intrinsics.checkExpressionValueIsNotNull(icons3, "entry.icons");
                    icons3.setPeopleAvatarDescriptor(statusBarIcon);
                } else {
                    IconPack icons4 = notificationEntry.getIcons();
                    Intrinsics.checkExpressionValueIsNotNull(icons4, "entry.icons");
                    icons4.setSmallIconDescriptor(statusBarIcon);
                }
            }
            return statusBarIcon;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("No icon in notification from ");
        StatusBarNotification sbn4 = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn4, "entry.sbn");
        sb.append(sbn4.getPackageName());
        throw new InflationException(sb.toString());
    }

    private final void setIcon(NotificationEntry notificationEntry, StatusBarIcon statusBarIcon, StatusBarIconView statusBarIconView) throws InflationException {
        statusBarIconView.setShowsConversation(showsConversation(notificationEntry, statusBarIconView, statusBarIcon));
        statusBarIconView.setTag(C0008R$id.icon_is_pre_L, Boolean.valueOf(notificationEntry.targetSdk < 21));
        if (!statusBarIconView.set(statusBarIcon)) {
            throw new InflationException("Couldn't create icon " + statusBarIcon);
        }
    }

    private final Icon createPeopleAvatar(NotificationEntry notificationEntry) throws InflationException {
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "entry.ranking");
        ShortcutInfo shortcutInfo = ranking.getShortcutInfo();
        Icon shortcutIcon = shortcutInfo != null ? this.launcherApps.getShortcutIcon(shortcutInfo) : null;
        if (shortcutIcon == null) {
            StatusBarNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
            Bundle bundle = sbn.getNotification().extras;
            Intrinsics.checkExpressionValueIsNotNull(bundle, "entry.sbn.notification.extras");
            List<Notification.MessagingStyle.Message> messagesFromBundleArray = Notification.MessagingStyle.Message.getMessagesFromBundleArray(bundle.getParcelableArray("android.messages"));
            Person person = (Person) bundle.getParcelable("android.messagingUser");
            Intrinsics.checkExpressionValueIsNotNull(messagesFromBundleArray, "messages");
            int size = messagesFromBundleArray.size();
            while (true) {
                size--;
                if (size < 0) {
                    break;
                }
                Notification.MessagingStyle.Message message = messagesFromBundleArray.get(size);
                Intrinsics.checkExpressionValueIsNotNull(message, "message");
                Person senderPerson = message.getSenderPerson();
                if (senderPerson != null && senderPerson != person) {
                    Person senderPerson2 = message.getSenderPerson();
                    if (senderPerson2 != null) {
                        shortcutIcon = senderPerson2.getIcon();
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                }
            }
        }
        if (shortcutIcon == null) {
            StatusBarNotification sbn2 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
            shortcutIcon = sbn2.getNotification().getLargeIcon();
        }
        if (shortcutIcon == null) {
            StatusBarNotification sbn3 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn3, "entry.sbn");
            Notification notification = sbn3.getNotification();
            Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
            shortcutIcon = notification.getSmallIcon();
        }
        if (shortcutIcon != null) {
            return shortcutIcon;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("No icon in notification from ");
        StatusBarNotification sbn4 = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn4, "entry.sbn");
        sb.append(sbn4.getPackageName());
        throw new InflationException(sb.toString());
    }

    private final boolean showsConversation(NotificationEntry notificationEntry, StatusBarIconView statusBarIconView, StatusBarIcon statusBarIcon) {
        boolean z;
        IconPack icons = notificationEntry.getIcons();
        Intrinsics.checkExpressionValueIsNotNull(icons, "entry.icons");
        if (statusBarIconView != icons.getShelfIcon()) {
            IconPack icons2 = notificationEntry.getIcons();
            Intrinsics.checkExpressionValueIsNotNull(icons2, "entry.icons");
            if (statusBarIconView != icons2.getAodIcon()) {
                z = false;
                Icon icon = statusBarIcon.icon;
                StatusBarNotification sbn = notificationEntry.getSbn();
                Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
                Notification notification = sbn.getNotification();
                Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
                boolean equals = icon.equals(notification.getSmallIcon());
                if (!isImportantConversation(notificationEntry) && !equals) {
                    return !z || !notificationEntry.isSensitive();
                }
            }
        }
        z = true;
        Icon icon = statusBarIcon.icon;
        StatusBarNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
        Notification notification = sbn.getNotification();
        Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
        boolean equals = icon.equals(notification.getSmallIcon());
        return !isImportantConversation(notificationEntry) ? false : false;
    }

    /* access modifiers changed from: private */
    public final boolean isImportantConversation(NotificationEntry notificationEntry) {
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "entry.ranking");
        if (ranking.getChannel() != null) {
            NotificationListenerService.Ranking ranking2 = notificationEntry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking2, "entry.ranking");
            NotificationChannel channel = ranking2.getChannel();
            Intrinsics.checkExpressionValueIsNotNull(channel, "entry.ranking.channel");
            if (channel.isImportantConversation()) {
                return true;
            }
        }
        return false;
    }
}
