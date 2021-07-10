package com.oneplus.volume;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.PathInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.mediarouter.media.MediaRouter;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.volume.MediaRouterWrapper;
import com.android.systemui.volume.VolumeDialogImpl;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpReflectionUtils;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
import com.oneplus.volume.OpOutputChooser;
import java.util.ArrayList;
import java.util.List;
public class OpVolumeDialogImpl {
    protected int mAccentColor = 0;
    protected View.OnClickListener mClickOutputChooser;
    protected TextView mConnectedDevice;
    protected Context mContext;
    protected DeviceInfo mDeviceInfo;
    protected ViewGroup mDialogLower;
    protected ViewGroup mDialogRowContainer;
    protected ViewGroup mDialogRowContainerBottom;
    protected ViewGroup mDialogUpper;
    protected boolean mFirstTimeInitDialog = true;
    protected OpHandler mHandler = new OpHandler();
    protected boolean mIsExpandAnimDone = true;
    protected ViewGroup mODICaptionsView;
    protected int mODIViewHeight = 0;
    protected int mOpBeforeExpandWidth;
    protected boolean mOpForceExpandState = false;
    protected boolean mOpLastforceExpandState = false;
    protected OpOutputChooser mOpOutputChooser;
    protected int mOpafterExpandWidth;
    protected ImageButton mOutputChooser;
    protected View mOutputChooserBackgroundView;
    protected OpOutputChooser.OutputChooserCallback mOutputChooserCallback = new OpOutputChooser.OutputChooserCallback() { // from class: com.oneplus.volume.OpVolumeDialogImpl.1
        @Override // com.oneplus.volume.OpOutputChooser.OutputChooserCallback
        public void onOutputChooserNotifyActiveDeviceChange(int i, int i2, String str, String str2) {
            Log.i("OpVolumeDialogImpl", "recevie OutputChooserCallback, deviceInfoType:" + i + ", iconResId:" + i2 + ", deviceInfoName:" + str + ", deviceInfoAddress:" + str2);
            DeviceInfo deviceInfo = OpVolumeDialogImpl.this.mDeviceInfo;
            deviceInfo.deviceInfoType = i;
            deviceInfo.iconResId = i2;
            StringBuilder sb = new StringBuilder();
            sb.append(str);
            sb.append(" ");
            deviceInfo.deviceInfoName = sb.toString();
            OpVolumeDialogImpl.this.mDeviceInfo.deviceInfoAddress = str2;
        }
    };
    protected OpOutputChooserDialog mOutputChooserDialog;
    protected EditText mOutputChooserExpandEditText;
    protected ImageView mOutputChooserExpandIcon;
    protected TextView mOutputChooserExpandTextView;
    private final Object mOutputChooserLock = new Object();
    protected boolean mPendingInit = true;
    protected List<VolumeRow> mRows = new ArrayList();
    protected int mThemeButtonBg = 0;
    protected int mThemeColorDialogBackground = 0;
    protected int mThemeColorDialogRowContainerBackground = 0;
    protected int mThemeColorIcon = 0;
    protected int mThemeColorMode = 0;
    protected int mThemeColorSeekbarBackgroundDrawable = 0;
    protected int mThemeColorText = 0;

    public static class VolumeRow {
        public ObjectAnimator anim;
        public int animTargetProgress;
        public ColorStateList cachedTint;
        public boolean defaultStream;
        public FrameLayout dndIcon;
        public TextView header;
        public ImageButton icon;
        public int iconMuteRes;
        public int iconRes;
        public int iconState;
        public boolean important;
        public int lastAudibleLevel = 1;
        public int requestedLevel = -1;
        public SeekBar slider;
        public VolumeDialogController.StreamState ss;
        public int stream;
        public int themeColorMode = 2;
        public boolean tracking;
        public long userAttempt;
        public View view;
        public float viewTouchDownY;
        public float viewTouchUpY;
    }

    /* access modifiers changed from: protected */
    public class DeviceInfo {
        public String deviceInfoAddress;
        public String deviceInfoName = "";
        public int deviceInfoType;
        public int iconResId = 0;

        public DeviceInfo(OpVolumeDialogImpl opVolumeDialogImpl) {
        }
    }

    protected class OpHandler extends Handler {
        protected OpHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what != 1) {
                Log.w("OpVolumeDialogImpl", "Unknown message: " + message.what);
                return;
            }
            OpVolumeDialogImpl.this.setDialogWidthH(message.arg1);
        }
    }

    /* access modifiers changed from: protected */
    public void setOpOutputChooserGravityNeedBeforeAnimStart(boolean z) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mOutputChooser.getLayoutParams();
        if (z) {
            layoutParams.gravity = 3;
            this.mOutputChooser.setLayoutParams(layoutParams);
            return;
        }
        layoutParams.gravity = 17;
        this.mOutputChooser.setLayoutParams(layoutParams);
    }

    /* access modifiers changed from: protected */
    public void setOpOutputChooserVisible(boolean z) {
        setOpOutputChooserVisible(z, false);
    }

    /* access modifiers changed from: protected */
    public void setOpOutputChooserVisible(boolean z, boolean z2) {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("OpVolumeDialogImpl", "setOpOutputChooserVisible:" + z + ", mOpForceExpandState:" + this.mOpForceExpandState);
        }
        FrameLayout frameLayout = (FrameLayout) getDialogView().findViewById(C0008R$id.output_chooser_background_container);
        LinearLayout linearLayout = (LinearLayout) getDialogView().findViewById(C0008R$id.output_active_device_container);
        if (z) {
            this.mOutputChooserExpandIcon.setImageResource(this.mDeviceInfo.iconResId);
            this.mOutputChooserExpandEditText.setText(this.mDeviceInfo.deviceInfoName);
            this.mOutputChooserExpandTextView.setText(this.mDeviceInfo.deviceInfoName);
            linearLayout.setVisibility(0);
            if (z2) {
                this.mOutputChooserExpandIcon.setVisibility(0);
                this.mOutputChooser.setVisibility(8);
                this.mOutputChooserExpandEditText.setVisibility(0);
            } else {
                setViewVisibleGoneFadeInOutAndScaleAnim(this.mOutputChooserExpandIcon, true);
                setViewVisibleGoneFadeInOutAndScaleAnim(this.mOutputChooser, false);
                setViewVisibleGoneFadeInOutAnim(this.mOutputChooserExpandEditText, true, linearLayout);
            }
            this.mOutputChooserExpandTextView.setSelected(true);
        } else if (z2) {
            this.mOutputChooserExpandIcon.setVisibility(8);
            this.mOutputChooser.setVisibility(0);
            this.mOutputChooserExpandTextView.setVisibility(8);
            linearLayout.setVisibility(8);
        } else {
            setViewVisibleGoneFadeInOutAndScaleAnim(this.mOutputChooserExpandIcon, false);
            setViewVisibleGoneFadeInOutAndScaleAnim(this.mOutputChooser, true);
            setViewVisibleGoneFadeInOutAnim(this.mOutputChooserExpandTextView, false, linearLayout);
        }
    }

    private void setViewVisibleGoneFadeInOutAndScaleAnim(View view, boolean z) {
        float f = 0.5f;
        if (z) {
            view.setAlpha(0.0f);
            view.setScaleX(0.5f);
            view.setScaleY(0.5f);
            view.setVisibility(0);
        }
        ViewPropertyAnimator scaleX = view.animate().alpha(z ? 1.0f : 0.0f).scaleX(z ? 1.0f : 0.5f);
        if (z) {
            f = 1.0f;
        }
        scaleX.scaleY(f).setDuration(275).setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f)).withEndAction(new Runnable(z, view) { // from class: com.oneplus.volume.-$$Lambda$OpVolumeDialogImpl$f7qEr0d70p_O061iK0Mg-UuLhHg
            public final /* synthetic */ boolean f$0;
            public final /* synthetic */ View f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                OpVolumeDialogImpl.lambda$setViewVisibleGoneFadeInOutAndScaleAnim$0(this.f$0, this.f$1);
            }
        }).setStartDelay(0);
    }

    static /* synthetic */ void lambda$setViewVisibleGoneFadeInOutAndScaleAnim$0(boolean z, View view) {
        if (!z) {
            view.setVisibility(8);
        }
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
        view.setAlpha(1.0f);
    }

    private void setViewVisibleGoneFadeInOutAnim(View view, boolean z, LinearLayout linearLayout) {
        if (z) {
            view.setAlpha(0.0f);
            view.setVisibility(0);
        }
        view.animate().alpha(z ? 1.0f : 0.0f).setDuration(275).setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f)).withEndAction(new Runnable(z, view, linearLayout) { // from class: com.oneplus.volume.-$$Lambda$OpVolumeDialogImpl$TA3PITzbby9aaH8iUXUPvx1U6QE
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ View f$2;
            public final /* synthetic */ LinearLayout f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                OpVolumeDialogImpl.this.lambda$setViewVisibleGoneFadeInOutAnim$1$OpVolumeDialogImpl(this.f$1, this.f$2, this.f$3);
            }
        }).setStartDelay(0);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setViewVisibleGoneFadeInOutAnim$1 */
    public /* synthetic */ void lambda$setViewVisibleGoneFadeInOutAnim$1$OpVolumeDialogImpl(boolean z, View view, LinearLayout linearLayout) {
        if (!z) {
            view.setVisibility(8);
            linearLayout.setVisibility(8);
            changeEditTextAndTextViewForMarquee(false);
        } else {
            changeEditTextAndTextViewForMarquee(true);
        }
        view.setAlpha(1.0f);
    }

    /* access modifiers changed from: protected */
    public void changeEditTextAndTextViewForMarquee(boolean z) {
        if (z) {
            this.mOutputChooserExpandEditText.setVisibility(8);
            this.mOutputChooserExpandTextView.setVisibility(0);
            return;
        }
        this.mOutputChooserExpandEditText.setVisibility(0);
        this.mOutputChooserExpandTextView.setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public void setDialogWidth(int i) {
        this.mHandler.removeMessages(1);
        OpHandler opHandler = this.mHandler;
        opHandler.sendMessage(opHandler.obtainMessage(1, i, 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDialogWidthH(int i) {
        FrameLayout frameLayout = (FrameLayout) getDialog().findViewById(C0008R$id.volume_dialog_container);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) frameLayout.getLayoutParams();
        layoutParams.gravity = isLandscape() ? 21 : 19;
        layoutParams.width = i;
        frameLayout.setLayoutParams(layoutParams);
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) getDialogView().getLayoutParams();
        layoutParams2.gravity = isLandscape() ? 21 : 19;
        getDialogView().setLayoutParams(layoutParams2);
        WindowManager.LayoutParams attributes = getDialog().getWindow().getAttributes();
        attributes.width = i;
        if (isLandscape()) {
            attributes.gravity = 21;
        } else {
            attributes.gravity = 19;
        }
        getDialog().getWindow().setAttributes(attributes);
    }

    private boolean isLandscape() {
        return this.mContext.getResources().getConfiguration().orientation == 2;
    }

    /* access modifiers changed from: protected */
    public void loadOpDimens() {
        Resources resources = this.mContext.getResources();
        int dimensionPixelSize = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_panel_transparent_padding, 1080);
        int dimensionPixelSize2 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_panel_width, 1080);
        int size = this.mRows.size();
        int i = dimensionPixelSize * 2;
        this.mOpBeforeExpandWidth = (this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.oneplus_contorl_layout_margin_left1) * 2) + i + dimensionPixelSize2;
        this.mOpafterExpandWidth = (this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.oneplus_contorl_layout_margin_left1) * 2) + i + (size * dimensionPixelSize2);
    }

    /* access modifiers changed from: protected */
    public void updateDialogLayout() {
        Context context = this.mContext;
        if (context != null && OpUtils.isSupportResolutionSwitch(context)) {
            Resources resources = this.mContext.getResources();
            int dimensionPixelSize = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_panel_width, 1080);
            int dimensionPixelSize2 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_panel_transparent_padding, 1080);
            int dimensionPixelSize3 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_outer_size, 1080);
            int dimensionPixelSize4 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_elevation, 1080);
            int dimensionPixelSize5 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_output_chooser_padding, 1080);
            int dimensionPixelSize6 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_output_chooser_padding2, 1080);
            int dimensionPixelSize7 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_image_icon_width, 1080);
            int dimensionPixelSize8 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_output_chooser_expand_text_size, 1080);
            OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_settings_margin, 1080);
            OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_settings_container_height, 1080);
            getDialogView().setMinimumWidth(dimensionPixelSize);
            getDialogView().findViewById(C0008R$id.main).setMinimumWidth(dimensionPixelSize);
            this.mDialogRowContainer.setPadding(dimensionPixelSize2, OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_content_padding_top, 1080), dimensionPixelSize2, 0);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mOutputChooserBackgroundView.getLayoutParams();
            marginLayoutParams.height = dimensionPixelSize3;
            marginLayoutParams.leftMargin = dimensionPixelSize4;
            marginLayoutParams.rightMargin = dimensionPixelSize4;
            this.mOutputChooserBackgroundView.setLayoutParams(marginLayoutParams);
            ViewGroup.LayoutParams layoutParams = this.mOutputChooser.getLayoutParams();
            layoutParams.width = dimensionPixelSize7;
            layoutParams.height = dimensionPixelSize3;
            this.mOutputChooser.setPadding(0, dimensionPixelSize5, 0, dimensionPixelSize5);
            this.mOutputChooser.setLayoutParams(layoutParams);
            ViewGroup.LayoutParams layoutParams2 = this.mOutputChooserExpandIcon.getLayoutParams();
            layoutParams2.width = dimensionPixelSize7;
            layoutParams2.height = dimensionPixelSize3;
            this.mOutputChooserExpandIcon.setPadding(0, dimensionPixelSize6, 0, dimensionPixelSize6);
            this.mOutputChooserExpandIcon.setLayoutParams(layoutParams2);
            float f = (float) dimensionPixelSize8;
            this.mOutputChooserExpandEditText.setTextSize(0, f);
            this.mOutputChooserExpandTextView.setTextSize(0, f);
            this.mConnectedDevice.setTextSize(0, (float) OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_volume_row_connected_device_text_size, 1080));
            updateVolumeRowLayout(resources);
            updateButtonLayout(getSettingsView(), getSettingsIcon(), resources);
            updateButtonLayout(getSettingsBackView(), getSettingsBackIcon(), resources);
            updateButtonLayout(getSettingsOpSettingsView(), getSettingsOpSettingsIcon(), resources);
            this.mODICaptionsView.getLayoutParams().height = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_odi_captions_bottom_height, 1080);
            getDialogView().invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void updateButtonLayout(View view, ImageButton imageButton, Resources resources) {
        int dimensionPixelSize = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_settings_container_height, 1080);
        int dimensionPixelSize2 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_panel_transparent_padding, 1080);
        int dimensionPixelSize3 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_panel_width, 1080);
        int dimensionPixelSize4 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_settings_margin, 1080);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        marginLayoutParams.height = dimensionPixelSize;
        marginLayoutParams.setMargins(dimensionPixelSize2, 0, dimensionPixelSize2, 0);
        view.setLayoutParams(marginLayoutParams);
        ViewGroup.LayoutParams layoutParams = imageButton.getLayoutParams();
        layoutParams.height = dimensionPixelSize3;
        layoutParams.width = dimensionPixelSize3;
        imageButton.setPadding(dimensionPixelSize4, 0, dimensionPixelSize4, 0);
        imageButton.setLayoutParams(layoutParams);
    }

    /* access modifiers changed from: protected */
    public void updateVolumeRowLayout(Resources resources) {
        int dimensionPixelSize = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_panel_width, 1080);
        int dimensionPixelSize2 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_slider_width, 1080);
        int dimensionPixelSize3 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_slider_height, 1080);
        int dimensionPixelSize4 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_slider_margin_bottom, 1080);
        int dimensionPixelSize5 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_icon_size, 1080);
        int dimensionPixelSize6 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_row_margin_bottom, 1080);
        for (VolumeRow volumeRow : this.mRows) {
            ViewGroup.LayoutParams layoutParams = volumeRow.view.getLayoutParams();
            layoutParams.width = dimensionPixelSize;
            volumeRow.view.setLayoutParams(layoutParams);
            View findViewById = volumeRow.view.findViewById(C0008R$id.volume_row_slider);
            ViewGroup.LayoutParams layoutParams2 = findViewById.getLayoutParams();
            layoutParams2.width = dimensionPixelSize3;
            layoutParams2.height = dimensionPixelSize2;
            findViewById.setLayoutParams(layoutParams2);
            FrameLayout frameLayout = (FrameLayout) volumeRow.view.findViewById(C0008R$id.volume_row_slider_frame);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) frameLayout.getLayoutParams();
            marginLayoutParams.height = dimensionPixelSize3;
            marginLayoutParams.width = dimensionPixelSize2;
            marginLayoutParams.setMargins(0, 0, 0, dimensionPixelSize4);
            frameLayout.setLayoutParams(marginLayoutParams);
            ImageButton imageButton = (ImageButton) volumeRow.view.findViewById(C0008R$id.volume_row_icon);
            ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) imageButton.getLayoutParams();
            marginLayoutParams2.height = dimensionPixelSize5;
            marginLayoutParams2.width = dimensionPixelSize5;
            marginLayoutParams2.setMargins(0, 0, 0, dimensionPixelSize6);
            imageButton.setLayoutParams(marginLayoutParams2);
            volumeRow.view.invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void updateODIRelatedLayout() {
        ViewGroup viewGroup;
        Resources resources = this.mContext.getResources();
        int dimensionPixelSize = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_icon_size, 1080);
        int dimensionPixelSize2 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_odi_captions_margin, 1080);
        int dimensionPixelSize3 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_settings_container_height, 1080);
        int dimensionPixelSize4 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_row_margin_bottom, 1080);
        int dimensionPixelSize5 = OpUtils.getDimensionPixelSize(resources, C0005R$dimen.op_volume_dialog_slider_height, 1080);
        int i = (dimensionPixelSize2 * 2) + dimensionPixelSize;
        int dimensionPixelSize6 = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.oneplus_contorl_layout_margin_left1);
        if (isLandscape()) {
            int i2 = this.mContext.getResources().getConfiguration().smallestScreenWidthDp;
            int dimensionPixelSize7 = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_volume_dialog_odi_captions_margin_land);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getDialogView().getLayoutParams();
            if (i2 <= 411) {
                float f = (((float) i2) / 411.0f) * 0.5f;
                dimensionPixelSize7 = (int) (((float) dimensionPixelSize7) * f);
                Log.d("OpVolumeDialogImpl", "mODICaptionsView swdp:" + i2 + " resizeRate:" + f);
                getDialogView().setPadding(dimensionPixelSize6, (int) (((float) dimensionPixelSize6) * f), (int) (((double) dimensionPixelSize6) * 1.5d), dimensionPixelSize6);
            } else {
                getDialogView().setPadding(dimensionPixelSize6, dimensionPixelSize6, (int) (((double) dimensionPixelSize6) * 1.5d), dimensionPixelSize6);
            }
            dimensionPixelSize4 = dimensionPixelSize7;
            dimensionPixelSize3 = dimensionPixelSize + (dimensionPixelSize4 * 2);
            dimensionPixelSize5 = (int) (((float) dimensionPixelSize5) * (((float) i2) / 411.0f) * 0.93f);
            i = dimensionPixelSize3;
        }
        for (VolumeRow volumeRow : this.mRows) {
            View findViewById = volumeRow.view.findViewById(C0008R$id.volume_row_slider);
            ViewGroup.LayoutParams layoutParams2 = findViewById.getLayoutParams();
            layoutParams2.width = dimensionPixelSize5;
            findViewById.setLayoutParams(layoutParams2);
            FrameLayout frameLayout = (FrameLayout) volumeRow.view.findViewById(C0008R$id.volume_row_slider_frame);
            ViewGroup.LayoutParams layoutParams3 = frameLayout.getLayoutParams();
            layoutParams3.height = dimensionPixelSize5;
            frameLayout.setLayoutParams(layoutParams3);
            frameLayout.invalidate();
        }
        if (this.mODICaptionsView != null && (viewGroup = this.mDialogUpper) != null && !this.mOpForceExpandState && this.mIsExpandAnimDone) {
            viewGroup.measure(0, 0);
            int measuredWidth = this.mDialogUpper.getMeasuredWidth();
            if (i > measuredWidth) {
                Log.d("OpVolumeDialogImpl", "mODICaptionsView odiHeight:" + i + " max:" + measuredWidth);
                i = measuredWidth;
            }
            this.mODIViewHeight = i;
            this.mODICaptionsView.getLayoutParams().height = i;
            this.mDialogLower.invalidate();
        }
        if (getSettingsView() != null) {
            getSettingsView().getLayoutParams().height = dimensionPixelSize3;
        }
        if (getSettingsBackView() != null) {
            getSettingsBackView().getLayoutParams().height = dimensionPixelSize3;
        }
        if (getSettingsOpSettingsView() != null) {
            getSettingsOpSettingsView().getLayoutParams().height = dimensionPixelSize3;
        }
        int size = this.mRows.size();
        for (int i3 = 0; i3 < size; i3++) {
            ImageButton imageButton = this.mRows.get(i3).icon;
            if (imageButton != null) {
                ((ViewGroup.MarginLayoutParams) imageButton.getLayoutParams()).bottomMargin = dimensionPixelSize4;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void showOutputChooserH() {
        synchronized (this.mOutputChooserLock) {
            if (this.mOutputChooserDialog == null) {
                if (this.mOutputChooserDialog == null) {
                    generateOutputChooserH();
                }
                this.mOutputChooserDialog.show();
                this.mOutputChooserDialog.setTheme(this.mThemeColorMode);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void generateOutputChooserH() {
        Context context = this.mContext;
        this.mOutputChooserDialog = new OpOutputChooserDialog(context, new MediaRouterWrapper(MediaRouter.getInstance(context))) { // from class: com.oneplus.volume.OpVolumeDialogImpl.2
            /* access modifiers changed from: protected */
            @Override // com.oneplus.volume.OpOutputChooserDialog
            public void cleanUp() {
                synchronized (OpVolumeDialogImpl.this.mOutputChooserLock) {
                    OpVolumeDialogImpl.this.mOutputChooserDialog = null;
                }
            }
        };
    }

    /* access modifiers changed from: protected */
    public void setExpandFeautureDismissState() {
        this.mOpLastforceExpandState = false;
        this.mOpForceExpandState = false;
        setOpOutputChooserVisible(false, true);
        initSettingsH();
        OpOutputChooser opOutputChooser = this.mOpOutputChooser;
        if (opOutputChooser != null) {
            this.mFirstTimeInitDialog = true;
            opOutputChooser.removeCallback();
            this.mOpOutputChooser.destory();
            this.mOpOutputChooser = null;
        }
    }

    private boolean isAccentColorChanged(int i, boolean z) {
        int color = ThemeColorUtils.getColor(100);
        if (this.mAccentColor == color) {
            return false;
        }
        this.mAccentColor = color;
        return true;
    }

    /* access modifiers changed from: protected */
    public void applyColorTheme(boolean z) {
        int themeColor = OpUtils.getThemeColor(this.mContext);
        boolean isAccentColorChanged = isAccentColorChanged(themeColor, z);
        if (this.mThemeColorMode != themeColor || isAccentColorChanged || z) {
            this.mThemeColorMode = themeColor;
            if (themeColor == 0) {
                applyWhiteTheme();
            } else if (themeColor != 1) {
                applyWhiteTheme();
            } else {
                applyBlackTheme();
            }
            applyColors();
        }
    }

    /* access modifiers changed from: protected */
    public void applyWhiteTheme() {
        Resources resources = this.mContext.getResources();
        this.mThemeColorDialogBackground = C0006R$drawable.volume_dialog_bg_light;
        this.mThemeColorDialogRowContainerBackground = C0006R$drawable.volume_dialog_row_container_bg;
        this.mThemeColorText = resources.getColor(C0004R$color.oneplus_contorl_text_color_secondary_light);
        this.mThemeColorIcon = resources.getColor(C0004R$color.op_volume_icon_color);
        this.mThemeColorSeekbarBackgroundDrawable = C0006R$drawable.volume_dialog_progress;
        this.mThemeButtonBg = resources.getColor(C0004R$color.op_volume_outputchooser_bg_color);
    }

    /* access modifiers changed from: protected */
    public void applyBlackTheme() {
        Resources resources = this.mContext.getResources();
        this.mThemeColorDialogBackground = C0006R$drawable.volume_dialog_bg_dark;
        this.mThemeColorDialogRowContainerBackground = C0006R$drawable.volume_dialog_row_container_bg;
        this.mThemeColorText = resources.getColor(C0004R$color.oneplus_contorl_text_color_secondary_dark);
        this.mThemeColorIcon = resources.getColor(C0004R$color.op_volume_icon_color);
        this.mThemeColorSeekbarBackgroundDrawable = C0006R$drawable.volume_dialog_progress;
        this.mThemeButtonBg = resources.getColor(C0004R$color.op_volume_outputchooser_bg_color);
        if (OpUtils.isREDVersion()) {
            this.mThemeColorDialogBackground = C0006R$drawable.volume_dialog_bg_red_dark;
            this.mThemeColorDialogRowContainerBackground = 0;
            this.mThemeColorIcon = resources.getColor(C0004R$color.op_turquoise);
        }
    }

    private void applyColors() {
        this.mDialogUpper.setBackgroundDrawable(getCornerGradientDrawable(this.mThemeColorDialogBackground));
        this.mDialogLower.setBackgroundDrawable(getCornerGradientDrawable(this.mThemeColorDialogBackground));
        this.mDialogRowContainer.setBackgroundDrawable(getCornerGradientDrawable(this.mThemeColorDialogRowContainerBackground));
        this.mODICaptionsView.setBackgroundDrawable(getCornerGradientDrawable(this.mThemeColorDialogRowContainerBackground));
        getSettingsIcon().setColorFilter(this.mThemeColorIcon);
        getSettingsBackIcon().setColorFilter(this.mThemeColorIcon);
        getSettingsOpSettingsIcon().setColorFilter(this.mThemeColorIcon);
        getODICaptionsIcon().setColorFilter(this.mThemeColorIcon);
        this.mConnectedDevice.setTextColor(this.mThemeColorText);
        this.mOutputChooser.setColorFilter(this.mAccentColor);
        this.mOutputChooser.setBackgroundTintList(ColorStateList.valueOf(this.mThemeButtonBg));
        this.mOutputChooserBackgroundView.setBackgroundTintList(ColorStateList.valueOf(this.mThemeButtonBg));
        this.mOutputChooserExpandIcon.setColorFilter(this.mThemeColorIcon);
        this.mOutputChooserExpandEditText.setTextColor(this.mThemeColorIcon);
        this.mOutputChooserExpandTextView.setTextColor(this.mThemeColorIcon);
        for (VolumeRow volumeRow : this.mRows) {
            updateVolumeRowTintH(volumeRow, true, true);
        }
    }

    private GradientDrawable getCornerGradientDrawable(int i) {
        try {
            GradientDrawable gradientDrawable = (GradientDrawable) ((LayerDrawable) this.mContext.getResources().getDrawable(i)).getDrawable(0);
            gradientDrawable.setCornerRadii(getVolCornerRadii(this.mContext));
            return gradientDrawable;
        } catch (Exception e) {
            Log.d("OpVolumeDialogImpl", "getCornerGradientDrawable Exception " + e.toString());
            return null;
        }
    }

    private float[] getVolCornerRadii(Context context) {
        float dimensionPixelSize = (float) context.getResources().getDimensionPixelSize(C0005R$dimen.shape_corner_radius);
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i("OpVolumeDialogImpl", "shape_corner_radius:" + dimensionPixelSize);
        }
        return new float[]{dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize};
    }

    private ViewGroup getDialogView() {
        return (ViewGroup) OpReflectionUtils.getValue(VolumeDialogImpl.class, this, "mDialogView");
    }

    /* access modifiers changed from: protected */
    public boolean isStatusBarShowing() {
        if ((OpLsState.getInstance().getPhoneStatusBar() != null ? OpLsState.getInstance().getPhoneStatusBar().getStatusBarWindowState() : 0) == 0) {
            return true;
        }
        Log.d("OpVolumeDialogImpl", "adjust to 1500");
        return false;
    }

    private VolumeDialogImpl.CustomDialog getDialog() {
        return (VolumeDialogImpl.CustomDialog) OpReflectionUtils.getValue(VolumeDialogImpl.class, this, "mDialog");
    }

    private void updateVolumeRowTintH(VolumeRow volumeRow, boolean z, boolean z2) {
        Class cls = Boolean.TYPE;
        OpReflectionUtils.methodInvokeWithArgs(this, OpReflectionUtils.getMethodWithParams(VolumeDialogImpl.class, "updateVolumeRowTintH", VolumeRow.class, cls, cls), volumeRow, Boolean.valueOf(z), Boolean.valueOf(z2));
    }

    private ImageButton getSettingsIcon() {
        return (ImageButton) OpReflectionUtils.getValue(VolumeDialogImpl.class, this, "mSettingsIcon");
    }

    private ImageButton getSettingsBackIcon() {
        return (ImageButton) OpReflectionUtils.getValue(VolumeDialogImpl.class, this, "mSettingsBackIcon");
    }

    private ImageButton getSettingsOpSettingsIcon() {
        return (ImageButton) OpReflectionUtils.getValue(VolumeDialogImpl.class, this, "mSettingsOpSettingsIcon");
    }

    private View getSettingsView() {
        return (View) OpReflectionUtils.getValue(VolumeDialogImpl.class, this, "mSettingsView");
    }

    private View getSettingsBackView() {
        return (View) OpReflectionUtils.getValue(VolumeDialogImpl.class, this, "mSettingsBackView");
    }

    private View getSettingsOpSettingsView() {
        return (View) OpReflectionUtils.getValue(VolumeDialogImpl.class, this, "mSettingsOpSettingsView");
    }

    private ImageButton getODICaptionsIcon() {
        return (ImageButton) OpReflectionUtils.getValue(VolumeDialogImpl.class, this, "mODICaptionsIcon");
    }

    private void initSettingsH() {
        OpReflectionUtils.methodInvokeVoid(VolumeDialogImpl.class, this, "initSettingsH", new Object[0]);
    }
}
