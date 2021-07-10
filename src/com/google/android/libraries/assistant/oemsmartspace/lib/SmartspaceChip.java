package com.google.android.libraries.assistant.oemsmartspace.lib;

import android.app.PendingIntent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
public class SmartspaceChip {
    private final PendingIntent chipIntent;
    private final Drawable icon;
    private final String title;

    public SmartspaceChip(Bundle bundle, Drawable drawable) {
        this.title = bundle.getString("com.google.android.apps.nexuslauncher.extra.SMARTSPACE_CHIP_TITLE_EXTRA");
        this.chipIntent = (PendingIntent) bundle.getParcelable("com.google.android.apps.nexuslauncher.extra.SMARTSPACE_CHIP_PENDING_INTENT_EXTRA");
        this.icon = drawable;
    }

    public static boolean intentHasChip(Bundle bundle) {
        return bundle.getString("com.google.android.apps.nexuslauncher.extra.SMARTSPACE_CHIP_TITLE_EXTRA") != null;
    }

    public PendingIntent getChipIntent() {
        return this.chipIntent;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean hasIcon() {
        return this.icon != null;
    }

    public boolean hasPendingIntent() {
        return this.chipIntent != null;
    }
}
