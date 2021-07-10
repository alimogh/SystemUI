package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import com.android.systemui.C0008R$id;
import com.google.android.material.listview.ListItemView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: ChannelEditorListView.kt */
public final class AppControlView extends ListItemView {
    @NotNull
    public TextView channelName;
    @NotNull

    /* renamed from: switch  reason: not valid java name */
    public Switch f0switch;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AppControlView(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "c");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    @NotNull
    public final TextView getChannelName() {
        TextView textView = this.channelName;
        if (textView != null) {
            return textView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("channelName");
        throw null;
    }

    @NotNull
    public final Switch getSwitch() {
        Switch r0 = this.f0switch;
        if (r0 != null) {
            return r0;
        }
        Intrinsics.throwUninitializedPropertyAccessException("switch");
        throw null;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        View findViewById = findViewById(C0008R$id.list_title);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.list_title)");
        this.channelName = (TextView) findViewById;
        Switch r0 = new Switch(getContext());
        this.f0switch = r0;
        if (r0 != null) {
            addCustomView(r0);
            setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.statusbar.notification.row.AppControlView$onFinishInflate$1
                final /* synthetic */ AppControlView this$0;

                {
                    this.this$0 = r1;
                }

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.this$0.getSwitch().toggle();
                }
            });
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("switch");
        throw null;
    }
}
