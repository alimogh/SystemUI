package com.android.systemui.controls.ui;

import android.app.ActivityView;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.widget.ImageView;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0016R$style;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: DetailDialog.kt */
public final class DetailDialog extends Dialog {
    @NotNull
    private String TAG = "DetailDialog";
    @NotNull
    private ActivityView activityView = new ActivityView(getContext(), (AttributeSet) null, 0, false);
    @NotNull
    private final ControlViewHolder cvh;
    @NotNull
    private final Intent intent;
    @NotNull
    private final ActivityView.StateCallback stateCallback = new ActivityView.StateCallback(this) { // from class: com.android.systemui.controls.ui.DetailDialog$stateCallback$1
        final /* synthetic */ DetailDialog this$0;

        public void onActivityViewDestroyed(@NotNull ActivityView activityView) {
            Intrinsics.checkParameterIsNotNull(activityView, "view");
        }

        /* JADX WARN: Incorrect args count in method signature: ()V */
        {
            this.this$0 = r1;
        }

        public void onActivityViewReady(@NotNull ActivityView activityView) {
            Intrinsics.checkParameterIsNotNull(activityView, "view");
            Intent intent = new Intent(this.this$0.getIntent());
            intent.putExtra("controls.DISPLAY_IN_PANEL", true);
            intent.addFlags(524288);
            intent.addFlags(134217728);
            activityView.startActivity(intent);
        }

        public void onTaskRemovalStarted(int i) {
            this.this$0.dismiss();
        }
    };

    @NotNull
    public final Intent getIntent() {
        return this.intent;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DetailDialog(@NotNull ControlViewHolder controlViewHolder, @NotNull Intent intent) {
        super(controlViewHolder.getContext(), C0016R$style.Theme_SystemUI_Dialog_Control_DetailPanel);
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        this.cvh = controlViewHolder;
        this.intent = intent;
        getWindow().setType(2020);
        setContentView(C0011R$layout.controls_detail_dialog);
        ((ViewGroup) requireViewById(C0008R$id.controls_activity_view)).addView(this.activityView);
        ((ImageView) requireViewById(C0008R$id.control_detail_close)).setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.controls.ui.DetailDialog$$special$$inlined$apply$lambda$1
            final /* synthetic */ DetailDialog this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(@NotNull View view) {
                Intrinsics.checkParameterIsNotNull(view, "<anonymous parameter 0>");
                this.this$0.dismiss();
            }
        });
        ImageView imageView = (ImageView) requireViewById(C0008R$id.control_detail_open_in_app);
        imageView.setOnClickListener(new View.OnClickListener(imageView, this) { // from class: com.android.systemui.controls.ui.DetailDialog$$special$$inlined$apply$lambda$2
            final /* synthetic */ ImageView $this_apply;
            final /* synthetic */ DetailDialog this$0;

            {
                this.$this_apply = r1;
                this.this$0 = r2;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(@NotNull View view) {
                Intrinsics.checkParameterIsNotNull(view, "v");
                try {
                    this.this$0.dismiss();
                    this.$this_apply.getContext().sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
                    this.this$0.getIntent().addFlags(268435456);
                    view.getContext().startActivity(this.this$0.getIntent());
                } catch (Exception e) {
                    String tag = this.this$0.getTAG();
                    Log.d(tag, "open_in_app Exception " + e.toString());
                }
            }
        });
        getWindow().getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener(this) { // from class: com.android.systemui.controls.ui.DetailDialog.4
            final /* synthetic */ DetailDialog this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(@NotNull View view, @NotNull WindowInsets windowInsets) {
                Intrinsics.checkParameterIsNotNull(view, "<anonymous parameter 0>");
                Intrinsics.checkParameterIsNotNull(windowInsets, "insets");
                ActivityView activityView = this.this$0.getActivityView();
                activityView.setPadding(activityView.getPaddingLeft(), activityView.getPaddingTop(), activityView.getPaddingRight(), windowInsets.getInsets(WindowInsets.Type.systemBars()).bottom);
                return WindowInsets.CONSUMED;
            }
        });
        ViewGroup viewGroup = (ViewGroup) requireViewById(C0008R$id.control_detail_root);
        int i = Settings.Secure.getInt(this.cvh.getContext().getContentResolver(), "systemui.controls_panel_top_offset", this.cvh.getContext().getResources().getDimensionPixelSize(C0005R$dimen.controls_activity_view_top_offset));
        ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
        if (layoutParams != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            marginLayoutParams.topMargin = i;
            viewGroup.setLayoutParams(marginLayoutParams);
            viewGroup.setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.controls.ui.DetailDialog$$special$$inlined$apply$lambda$3
                final /* synthetic */ DetailDialog this$0;

                {
                    this.this$0 = r1;
                }

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.this$0.dismiss();
                }
            });
            ViewParent parent = viewGroup.getParent();
            if (parent != null) {
                ((View) parent).setOnClickListener(new View.OnClickListener(this) { // from class: com.android.systemui.controls.ui.DetailDialog$$special$$inlined$apply$lambda$4
                    final /* synthetic */ DetailDialog this$0;

                    {
                        this.this$0 = r1;
                    }

                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        this.this$0.dismiss();
                    }
                });
                if (ScreenDecorationsUtils.supportsRoundedCornersOnWindows(getContext().getResources())) {
                    Context context = getContext();
                    Intrinsics.checkExpressionValueIsNotNull(context, "context");
                    this.activityView.setCornerRadius((float) context.getResources().getDimensionPixelSize(C0005R$dimen.controls_activity_view_corner_radius));
                    return;
                }
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.view.View");
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
    }

    @NotNull
    public final String getTAG() {
        return this.TAG;
    }

    @NotNull
    public final ActivityView getActivityView() {
        return this.activityView;
    }

    @Override // android.app.Dialog
    public void show() {
        this.activityView.setCallback(this.stateCallback);
        super.show();
    }

    @Override // android.app.Dialog, android.content.DialogInterface
    public void dismiss() {
        if (isShowing()) {
            this.activityView.release();
            super.dismiss();
        }
    }
}
