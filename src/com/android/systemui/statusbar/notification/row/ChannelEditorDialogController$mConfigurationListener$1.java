package com.android.systemui.statusbar.notification.row;

import android.content.res.Configuration;
import com.android.systemui.statusbar.policy.ConfigurationController;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.Nullable;
/* compiled from: ChannelEditorDialogController.kt */
public final class ChannelEditorDialogController$mConfigurationListener$1 implements ConfigurationController.ConfigurationListener {
    final /* synthetic */ ChannelEditorDialogController this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ChannelEditorDialogController$mConfigurationListener$1(ChannelEditorDialogController channelEditorDialogController) {
        this.this$0 = channelEditorDialogController;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(@Nullable Configuration configuration) {
        if (configuration == null || configuration.orientation != this.this$0.mOrientation) {
            ChannelEditorDialogController channelEditorDialogController = this.this$0;
            Integer valueOf = configuration != null ? Integer.valueOf(configuration.orientation) : null;
            if (valueOf != null) {
                channelEditorDialogController.mOrientation = valueOf.intValue();
                this.this$0.done();
                return;
            }
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
