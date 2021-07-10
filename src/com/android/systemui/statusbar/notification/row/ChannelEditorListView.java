package com.android.systemui.statusbar.notification.row;

import android.app.NotificationChannel;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.util.Assert;
import java.util.ArrayList;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ChannelEditorListView.kt */
public final class ChannelEditorListView extends LinearLayout {
    private AppControlView appControlRow;
    @Nullable
    private Drawable appIcon;
    @Nullable
    private ImageView appIconView;
    @Nullable
    private String appName;
    private final List<ChannelRow> channelRows = new ArrayList();
    @NotNull
    private List<NotificationChannel> channels = new ArrayList();
    @NotNull
    public ChannelEditorDialogController controller;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ChannelEditorListView(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "c");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    @NotNull
    public final ChannelEditorDialogController getController() {
        ChannelEditorDialogController channelEditorDialogController = this.controller;
        if (channelEditorDialogController != null) {
            return channelEditorDialogController;
        }
        Intrinsics.throwUninitializedPropertyAccessException("controller");
        throw null;
    }

    public final void setController(@NotNull ChannelEditorDialogController channelEditorDialogController) {
        Intrinsics.checkParameterIsNotNull(channelEditorDialogController, "<set-?>");
        this.controller = channelEditorDialogController;
    }

    public final void setAppIcon(@Nullable Drawable drawable) {
        this.appIcon = drawable;
    }

    public final void setAppName(@Nullable String str) {
        this.appName = str;
    }

    public final void setChannels(@NotNull List<NotificationChannel> list) {
        Intrinsics.checkParameterIsNotNull(list, "newValue");
        this.channels = list;
        updateRows();
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        View findViewById = findViewById(C0008R$id.app_control);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.app_control)");
        this.appControlRow = (AppControlView) findViewById;
        this.appIconView = (ImageView) findViewById(C0008R$id.notification_icon);
    }

    public final void highlightChannel(@NotNull NotificationChannel notificationChannel) {
        Intrinsics.checkParameterIsNotNull(notificationChannel, "channel");
        Assert.isMainThread();
        for (ChannelRow channelRow : this.channelRows) {
            if (Intrinsics.areEqual(channelRow.getChannel(), notificationChannel)) {
                channelRow.playHighlight();
            }
        }
    }

    /* access modifiers changed from: public */
    private final void updateRows() {
        ChannelEditorDialogController channelEditorDialogController = this.controller;
        if (channelEditorDialogController != null) {
            boolean areAppNotificationsEnabled = channelEditorDialogController.areAppNotificationsEnabled();
            AutoTransition autoTransition = new AutoTransition();
            autoTransition.setDuration(200L);
            autoTransition.addListener((Transition.TransitionListener) new Transition.TransitionListener(this) { // from class: com.android.systemui.statusbar.notification.row.ChannelEditorListView$updateRows$1
                final /* synthetic */ ChannelEditorListView this$0;

                @Override // android.transition.Transition.TransitionListener
                public void onTransitionCancel(@Nullable Transition transition) {
                }

                @Override // android.transition.Transition.TransitionListener
                public void onTransitionPause(@Nullable Transition transition) {
                }

                @Override // android.transition.Transition.TransitionListener
                public void onTransitionResume(@Nullable Transition transition) {
                }

                @Override // android.transition.Transition.TransitionListener
                public void onTransitionStart(@Nullable Transition transition) {
                }

                /* JADX WARN: Incorrect args count in method signature: ()V */
                {
                    this.this$0 = r1;
                }

                @Override // android.transition.Transition.TransitionListener
                public void onTransitionEnd(@Nullable Transition transition) {
                    this.this$0.notifySubtreeAccessibilityStateChangedIfNeeded();
                }
            });
            TransitionManager.beginDelayedTransition(this, autoTransition);
            for (ChannelRow channelRow : this.channelRows) {
                removeView(channelRow);
            }
            this.channelRows.clear();
            updateAppControlRow(areAppNotificationsEnabled);
            if (areAppNotificationsEnabled) {
                LayoutInflater from = LayoutInflater.from(getContext());
                for (NotificationChannel notificationChannel : this.channels) {
                    Intrinsics.checkExpressionValueIsNotNull(from, "inflater");
                    addChannelRow(notificationChannel, from);
                }
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("controller");
        throw null;
    }

    private final void addChannelRow(NotificationChannel notificationChannel, LayoutInflater layoutInflater) {
        View inflate = layoutInflater.inflate(C0011R$layout.op_notif_half_shelf_row, (ViewGroup) null);
        if (inflate != null) {
            ChannelRow channelRow = (ChannelRow) inflate;
            ChannelEditorDialogController channelEditorDialogController = this.controller;
            if (channelEditorDialogController != null) {
                channelRow.setController(channelEditorDialogController);
                channelRow.setChannel(notificationChannel);
                this.channelRows.add(channelRow);
                addView(channelRow);
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("controller");
            throw null;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.ChannelRow");
    }

    private final void updateAppControlRow(boolean z) {
        ImageView imageView = this.appIconView;
        if (imageView != null) {
            imageView.setImageDrawable(this.appIcon);
        }
        AppControlView appControlView = this.appControlRow;
        if (appControlView != null) {
            TextView channelName = appControlView.getChannelName();
            Context context = getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "context");
            channelName.setText(context.getResources().getString(C0015R$string.notification_channel_dialog_title, this.appName));
            AppControlView appControlView2 = this.appControlRow;
            if (appControlView2 != null) {
                appControlView2.getSwitch().setChecked(z);
                AppControlView appControlView3 = this.appControlRow;
                if (appControlView3 != null) {
                    appControlView3.getSwitch().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(this) { // from class: com.android.systemui.statusbar.notification.row.ChannelEditorListView$updateAppControlRow$1
                        final /* synthetic */ ChannelEditorListView this$0;

                        {
                            this.this$0 = r1;
                        }

                        @Override // android.widget.CompoundButton.OnCheckedChangeListener
                        public final void onCheckedChanged(CompoundButton compoundButton, boolean z2) {
                            this.this$0.getController().proposeSetAppNotificationsEnabled(z2);
                            ChannelEditorListView.access$updateRows(this.this$0);
                        }
                    });
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("appControlRow");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("appControlRow");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("appControlRow");
            throw null;
        }
    }
}
