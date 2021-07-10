package com.android.systemui.statusbar.notification.row;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.systemui.C0008R$id;
import com.google.android.material.listview.ListItemView;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ChannelEditorListView.kt */
public final class ChannelRow extends ListItemView {
    @Nullable
    private NotificationChannel channel;
    private TextView channelDescription;
    private TextView channelName;
    @NotNull
    public ChannelEditorDialogController controller;
    private final int highlightColor = Utils.getColorAttrDefaultColor(getContext(), 16843820);

    /* renamed from: switch  reason: not valid java name */
    private Switch f1switch;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ChannelRow(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "c");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    public static final /* synthetic */ Switch access$getSwitch$p(ChannelRow channelRow) {
        Switch r0 = channelRow.f1switch;
        if (r0 != null) {
            return r0;
        }
        Intrinsics.throwUninitializedPropertyAccessException("switch");
        throw null;
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

    @Nullable
    public final NotificationChannel getChannel() {
        return this.channel;
    }

    public final void setChannel(@Nullable NotificationChannel notificationChannel) {
        this.channel = notificationChannel;
        updateImportance();
        updateViews();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        View findViewById = findViewById(C0008R$id.list_title);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.list_title)");
        this.channelName = (TextView) findViewById;
        View findViewById2 = findViewById(C0008R$id.list_summary);
        Intrinsics.checkExpressionValueIsNotNull(findViewById2, "findViewById(R.id.list_summary)");
        this.channelDescription = (TextView) findViewById2;
        View findViewById3 = findViewById(C0008R$id.list_widget_frame);
        Intrinsics.checkExpressionValueIsNotNull(findViewById3, "findViewById(R.id.list_widget_frame)");
        ViewGroup viewGroup = (ViewGroup) findViewById3;
        Switch r0 = new Switch(getContext());
        this.f1switch = r0;
        if (r0 != null) {
            addCustomView(r0);
            Switch r02 = this.f1switch;
            if (r02 != null) {
                r02.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(this) { // from class: com.android.systemui.statusbar.notification.row.ChannelRow$onFinishInflate$1
                    final /* synthetic */ ChannelRow this$0;

                    {
                        this.this$0 = r1;
                    }

                    @Override // android.widget.CompoundButton.OnCheckedChangeListener
                    public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                        NotificationChannel channel = this.this$0.getChannel();
                        if (channel != null) {
                            this.this$0.getController().proposeEditForChannel(channel, z ? channel.getImportance() : 0);
                        }
                    }
                });
                setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.notification.row.ChannelRow$onFinishInflate$2
                    final /* synthetic */ ChannelRow this$0;

                    {
                        this.this$0 = r1;
                    }

                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        ChannelRow.access$getSwitch$p(this.this$0).toggle();
                    }
                });
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("switch");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("switch");
        throw null;
    }

    public final void playHighlight() {
        ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), 0, Integer.valueOf(this.highlightColor));
        Intrinsics.checkExpressionValueIsNotNull(ofObject, "fadeInLoop");
        ofObject.setDuration(200L);
        ofObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.statusbar.notification.row.ChannelRow$playHighlight$1
            final /* synthetic */ ChannelRow this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                ChannelRow channelRow = this.this$0;
                Intrinsics.checkExpressionValueIsNotNull(valueAnimator, "animator");
                Object animatedValue = valueAnimator.getAnimatedValue();
                if (animatedValue != null) {
                    channelRow.setBackgroundColor(((Integer) animatedValue).intValue());
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
            }
        });
        ofObject.setRepeatMode(2);
        ofObject.setRepeatCount(5);
        ofObject.start();
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x006a  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0075  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void updateViews() {
        /*
            r6 = this;
            android.app.NotificationChannel r0 = r6.channel
            if (r0 == 0) goto L_0x0086
            android.widget.TextView r1 = r6.channelName
            r2 = 0
            if (r1 == 0) goto L_0x0080
            java.lang.CharSequence r3 = r0.getName()
            if (r3 == 0) goto L_0x0010
            goto L_0x0012
        L_0x0010:
            java.lang.String r3 = ""
        L_0x0012:
            r1.setText(r3)
            java.lang.String r1 = r0.getGroup()
            java.lang.String r3 = "channelDescription"
            if (r1 == 0) goto L_0x0037
            android.widget.TextView r4 = r6.channelDescription
            if (r4 == 0) goto L_0x0033
            com.android.systemui.statusbar.notification.row.ChannelEditorDialogController r5 = r6.controller
            if (r5 == 0) goto L_0x002d
            java.lang.CharSequence r1 = r5.groupNameForId(r1)
            r4.setText(r1)
            goto L_0x0037
        L_0x002d:
            java.lang.String r6 = "controller"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r6)
            throw r2
        L_0x0033:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r3)
            throw r2
        L_0x0037:
            java.lang.String r1 = r0.getGroup()
            r4 = 0
            if (r1 == 0) goto L_0x005d
            android.widget.TextView r1 = r6.channelDescription
            if (r1 == 0) goto L_0x0059
            java.lang.CharSequence r1 = r1.getText()
            boolean r1 = android.text.TextUtils.isEmpty(r1)
            if (r1 == 0) goto L_0x004d
            goto L_0x005d
        L_0x004d:
            android.widget.TextView r1 = r6.channelDescription
            if (r1 == 0) goto L_0x0055
            r1.setVisibility(r4)
            goto L_0x0066
        L_0x0055:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r3)
            throw r2
        L_0x0059:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r3)
            throw r2
        L_0x005d:
            android.widget.TextView r1 = r6.channelDescription
            if (r1 == 0) goto L_0x007c
            r3 = 8
            r1.setVisibility(r3)
        L_0x0066:
            android.widget.Switch r6 = r6.f1switch
            if (r6 == 0) goto L_0x0075
            int r0 = r0.getImportance()
            if (r0 == 0) goto L_0x0071
            r4 = 1
        L_0x0071:
            r6.setChecked(r4)
            return
        L_0x0075:
            java.lang.String r6 = "switch"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r6)
            throw r2
        L_0x007c:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r3)
            throw r2
        L_0x0080:
            java.lang.String r6 = "channelName"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r6)
            throw r2
        L_0x0086:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.row.ChannelRow.updateViews():void");
    }

    private final void updateImportance() {
        NotificationChannel notificationChannel = this.channel;
        if ((notificationChannel != null ? notificationChannel.getImportance() : 0) == -1000) {
        }
    }
}
