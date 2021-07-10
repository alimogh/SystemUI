package com.oneplus.aod.utils.bitmoji;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.C0016R$style;
import com.oneplus.aod.utils.bitmoji.MdmLogger;
import com.oneplus.util.ThemeColorUtils;
import kotlin.TypeCastException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: OpBitmojiSettingsActivity.kt */
public final class OpBitmojiSettingsActivity extends FragmentActivity {
    /* access modifiers changed from: protected */
    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        SettingsDialogFragment.Companion.newInstance().show(getSupportFragmentManager(), "SettingsDialogFragment");
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onUserLeaveHint() {
        Fragment findFragmentByTag;
        super.onUserLeaveHint();
        if (!isFinishing() && (findFragmentByTag = getSupportFragmentManager().findFragmentByTag("SettingsDialogFragment")) != null) {
            if (findFragmentByTag != null) {
                ((DialogFragment) findFragmentByTag).dismiss();
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type androidx.fragment.app.DialogFragment");
        }
    }

    /* compiled from: OpBitmojiSettingsActivity.kt */
    public static final class SettingsDialogFragment extends DialogFragment implements View.OnClickListener {
        public static final Companion Companion = new Companion(null);
        private static final long STICKER_SIZE = 27791988;
        private boolean mIsFirstApply = true;
        private CheckedTextView mMobileButton;
        private boolean mUseMobile;
        private CheckedTextView mWifiButton;

        /* compiled from: OpBitmojiSettingsActivity.kt */
        public static final class Companion {
            private Companion() {
            }

            public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
                this();
            }

            @NotNull
            public final SettingsDialogFragment newInstance() {
                return new SettingsDialogFragment();
            }
        }

        @Override // androidx.fragment.app.DialogFragment
        @NotNull
        public Dialog onCreateDialog(@Nullable Bundle bundle) {
            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getActivity(), ThemeColorUtils.getCurrentTheme() == 1 ? C0016R$style.oneplus_theme_dialog_dark : C0016R$style.oneplus_theme_dialog_light);
            View inflate = LayoutInflater.from(getActivity()).inflate(C0011R$layout.op_bitmoji_settings_content, (ViewGroup) null);
            TextView textView = (TextView) inflate.findViewById(C0008R$id.bitmoji_settings_msg);
            Formatter.BytesResult formatBytes = Formatter.formatBytes(getResources(), STICKER_SIZE, 0);
            if (textView != null) {
                textView.setText(getString(C0015R$string.op_bitmoji_settings_info, formatBytes.value, formatBytes.units));
            }
            this.mIsFirstApply = OpBitmojiHelper.getInstance().isApplyFirstTime();
            this.mUseMobile = OpBitmojiHelper.isDownloadViaMobile(getContext());
            this.mMobileButton = (CheckedTextView) inflate.findViewById(C0008R$id.mobile);
            this.mWifiButton = (CheckedTextView) inflate.findViewById(C0008R$id.wifiOnly);
            if (!this.mIsFirstApply) {
                CheckedTextView checkedTextView = this.mMobileButton;
                if (checkedTextView != null) {
                    checkedTextView.setChecked(this.mUseMobile);
                }
                CheckedTextView checkedTextView2 = this.mWifiButton;
                if (checkedTextView2 != null) {
                    checkedTextView2.setChecked(!this.mUseMobile);
                }
            }
            CheckedTextView checkedTextView3 = this.mMobileButton;
            if (checkedTextView3 != null) {
                checkedTextView3.setOnClickListener(this);
            }
            CheckedTextView checkedTextView4 = this.mWifiButton;
            if (checkedTextView4 != null) {
                checkedTextView4.setOnClickListener(this);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
            builder.setView(inflate);
            builder.setNegativeButton(17039360, null);
            AlertDialog create = builder.create();
            Intrinsics.checkExpressionValueIsNotNull(create, "builder.create()");
            create.setShowInBottom(true);
            if (this.mIsFirstApply) {
                setCancelable(false);
                create.setCanceledOnTouchOutside(false);
                MdmLogger.Companion companion = MdmLogger.Companion;
                Context context = getContext();
                if (context != null) {
                    Intrinsics.checkExpressionValueIsNotNull(context, "getContext()!!");
                    companion.getInstance(context).logNetworkOptionFirstShownEvent();
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                MdmLogger.Companion companion2 = MdmLogger.Companion;
                Context context2 = getContext();
                if (context2 != null) {
                    Intrinsics.checkExpressionValueIsNotNull(context2, "getContext()!!");
                    companion2.getInstance(context2).logNetworkOptionClickEvent();
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
            return create;
        }

        @Override // androidx.fragment.app.DialogFragment, android.content.DialogInterface.OnDismissListener
        public void onDismiss(@NotNull DialogInterface dialogInterface) {
            Intrinsics.checkParameterIsNotNull(dialogInterface, "dialog");
            super.onDismiss(dialogInterface);
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        }

        @Override // android.view.View.OnClickListener
        public void onClick(@NotNull View view) {
            boolean z;
            Intrinsics.checkParameterIsNotNull(view, "v");
            int id = view.getId();
            int i = 1;
            if (id == C0008R$id.mobile) {
                z = true;
            } else if (id == C0008R$id.wifiOnly) {
                z = false;
            } else {
                return;
            }
            if (this.mUseMobile != z) {
                CheckedTextView checkedTextView = this.mMobileButton;
                if (checkedTextView != null) {
                    checkedTextView.setChecked(z);
                }
                CheckedTextView checkedTextView2 = this.mWifiButton;
                if (checkedTextView2 != null) {
                    checkedTextView2.setChecked(!z);
                }
                OpBitmojiHelper.getInstance().setDownloadViaMobile(z);
                this.mUseMobile = z;
                MdmLogger.Companion companion = MdmLogger.Companion;
                Context context = getContext();
                if (context != null) {
                    Intrinsics.checkExpressionValueIsNotNull(context, "getContext()!!");
                    MdmLogger instance = companion.getInstance(context);
                    if (!this.mUseMobile) {
                        i = 2;
                    }
                    instance.logNetworkOptionChooseEvent(i);
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
            if (this.mIsFirstApply) {
                OpBitmojiHelper.getInstance().firstApply();
                OpBitmojiHelper.getInstance().startDownloading(false);
            }
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.setResult(-1);
            }
            dismiss();
        }
    }
}
