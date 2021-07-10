package com.android.systemui.volume;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioSystem;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.VibrationEffect;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settingslib.Utils;
import com.android.settingslib.volume.Util;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.Prefs;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.VolumeDialog;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.util.ProductUtils;
import com.android.systemui.volume.CaptionsToggleImageButton;
import com.oneplus.opthreekey.OpThreekeyVolumeGuideController;
import com.oneplus.scene.OpSceneModeObserver;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
import com.oneplus.volume.OpOutputChooser;
import com.oneplus.volume.OpVolumeDialogImpl;
import java.util.Iterator;
public class VolumeDialogImpl extends OpVolumeDialogImpl implements VolumeDialog, ConfigurationController.ConfigurationListener {
    private static final String TAG = Util.logTag(VolumeDialogImpl.class);
    private final Accessibility mAccessibility = new Accessibility();
    private final AccessibilityManagerWrapper mAccessibilityMgr;
    private int mActiveStream;
    private final ActivityManager mActivityManager;
    private ValueAnimator mAnimVol;
    private boolean mAutomute = true;
    private boolean mConfigChanged;
    private ConfigurableTexts mConfigurableTexts;
    private final VolumeDialogController mController;
    private final VolumeDialogController.Callbacks mControllerCallbackH;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private CustomDialog mDialog;
    private ViewGroup mDialogRowsView;
    private ViewGroup mDialogView;
    private final SparseBooleanArray mDynamic = new SparseBooleanArray();
    private boolean mFromTooltip;
    private final H mHandler = new H();
    private boolean mHasSeenODICaptionsTooltip;
    private boolean mHovering;
    private boolean mIsAnimatingDismiss;
    private boolean mIsCaptionComponentEnabled;
    private boolean mIsOpZenModeOn;
    private final KeyguardManager mKeyguard;
    private boolean mNeedPlayExpandAnim;
    private CaptionsToggleImageButton mODICaptionsIcon;
    private View mODICaptionsTooltipView;
    private ViewStub mODICaptionsTooltipViewStub;
    private boolean mODIDebug;
    private int mPrevActiveStream;
    private ViewGroup mRinger;
    private ImageButton mRingerIcon;
    private SafetyWarningDialog mSafetyWarning;
    private final Object mSafetyWarningLock = new Object();
    private ImageButton mSettingsBackIcon;
    private View mSettingsBackView;
    private ImageButton mSettingsIcon;
    private ImageButton mSettingsOpSettingsIcon;
    private View mSettingsOpSettingsView;
    private View mSettingsView;
    private boolean mShowA11yStream;
    private boolean mShowActiveStreamOnly;
    private boolean mShowing;
    private boolean mSilentMode = true;
    private VolumeDialogController.State mState;
    private int mTargetBottomSettingsBackIconVisible;
    private int mTargetBottomSettingsIconVisible;
    private int mTargetBottomSettingsOpSettingsIconVisible;
    private Window mWindow;
    private FrameLayout mZenIcon;

    static /* synthetic */ void lambda$updateRowsH$19() {
    }

    static /* synthetic */ void lambda$updateRowsH$20() {
    }

    static /* synthetic */ void lambda$updateRowsH$21() {
    }

    public VolumeDialogImpl(Context context) {
        boolean z = false;
        this.mHovering = false;
        this.mConfigChanged = false;
        this.mIsAnimatingDismiss = false;
        this.mODICaptionsTooltipView = null;
        this.mTargetBottomSettingsIconVisible = 0;
        this.mTargetBottomSettingsBackIconVisible = 8;
        this.mTargetBottomSettingsOpSettingsIconVisible = 8;
        this.mNeedPlayExpandAnim = false;
        this.mControllerCallbackH = new VolumeDialogController.Callbacks() { // from class: com.android.systemui.volume.VolumeDialogImpl.9
            @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
            public void onShowRequested(int i) {
                try {
                    VolumeDialogImpl.this.showH(i);
                } catch (RuntimeException e) {
                    String str = VolumeDialogImpl.TAG;
                    Log.e(str, "RuntimeException, " + e.getMessage());
                }
            }

            @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
            public void onDismissRequested(int i) {
                VolumeDialogImpl.this.dismissH(i);
            }

            @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
            public void onScreenOff() {
                VolumeDialogImpl.this.dismissH(4);
            }

            @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
            public void onStateChanged(VolumeDialogController.State state) {
                VolumeDialogImpl.this.onStateChangedH(state);
            }

            @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
            public void onLayoutDirectionChanged(int i) {
                VolumeDialogImpl.this.mDialogView.setLayoutDirection(i);
            }

            @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
            public void onConfigurationChanged() {
                VolumeDialogImpl.this.updateDialogLayout();
                if (((OpVolumeDialogImpl) VolumeDialogImpl.this).mODICaptionsView != null && ((OpVolumeDialogImpl) VolumeDialogImpl.this).mODICaptionsView.getVisibility() == 0) {
                    VolumeDialogImpl.this.updateODIRelatedLayout();
                }
                VolumeDialogImpl.this.loadOpDimens();
                if (OpUtils.DEBUG_ONEPLUS) {
                    String str = VolumeDialogImpl.TAG;
                    Log.i(str, "Vol onConfigurationChanged, mOpBeforeExpandWidth:" + ((OpVolumeDialogImpl) VolumeDialogImpl.this).mOpBeforeExpandWidth + ", mOpafterExpandWidth:" + ((OpVolumeDialogImpl) VolumeDialogImpl.this).mOpafterExpandWidth);
                }
                VolumeDialogImpl.this.setExpandFeautureDismissState();
                VolumeDialogImpl.this.mDialog.dismiss();
                VolumeDialogImpl.this.mHandler.removeMessages(2);
                VolumeDialogImpl.this.mConfigChanged = true;
                ((OpVolumeDialogImpl) VolumeDialogImpl.this).mPendingInit = true;
            }

            @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
            public void onShowVibrateHint() {
                if (VolumeDialogImpl.this.mSilentMode) {
                    VolumeDialogImpl.this.mController.setRingerMode(0, false);
                }
            }

            @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
            public void onShowSilentHint() {
                if (VolumeDialogImpl.this.mSilentMode) {
                    VolumeDialogImpl.this.mController.setRingerMode(2, false);
                }
            }

            @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
            public void onShowSafetyWarning(int i) {
                VolumeDialogImpl.this.showSafetyWarningH(i);
            }

            @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
            public void onAccessibilityModeChanged(Boolean bool) {
                VolumeDialogImpl.this.mShowA11yStream = bool == null ? false : bool.booleanValue();
                OpVolumeDialogImpl.VolumeRow activeRow = VolumeDialogImpl.this.getActiveRow();
                if (VolumeDialogImpl.this.mShowA11yStream || 10 != activeRow.stream) {
                    VolumeDialogImpl.this.updateRowsH(activeRow);
                } else {
                    VolumeDialogImpl.this.dismissH(7);
                }
            }

            @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
            public void onCaptionComponentStateChanged(Boolean bool, Boolean bool2) {
                VolumeDialogImpl.this.mIsCaptionComponentEnabled = bool.booleanValue();
                VolumeDialogImpl.this.mFromTooltip = bool2.booleanValue();
                if (OpUtils.DEBUG_ONEPLUS) {
                    String str = VolumeDialogImpl.TAG;
                    Log.i(str, "onCaptionComponentStateChanged, mIsCaptionComponentEnabled:" + VolumeDialogImpl.this.mIsCaptionComponentEnabled + ", fromTooltip:" + bool2);
                }
                VolumeDialogImpl.this.updateODICaptionsH(bool.booleanValue(), bool2.booleanValue());
            }
        };
        this.mContext = new ContextThemeWrapper(context, C0016R$style.qs_theme);
        this.mController = (VolumeDialogController) Dependency.get(VolumeDialogController.class);
        this.mKeyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mAccessibilityMgr = (AccessibilityManagerWrapper) Dependency.get(AccessibilityManagerWrapper.class);
        this.mDeviceProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);
        this.mShowActiveStreamOnly = showActiveStreamOnly();
        boolean equals = "1".equals(SystemProperties.get("systemui.odi.debug"));
        this.mODIDebug = equals;
        this.mHasSeenODICaptionsTooltip = !equals ? Prefs.getBoolean(context, "HasSeenODICaptionsTooltip", false) : z;
        this.mClickOutputChooser = new View.OnClickListener() { // from class: com.android.systemui.volume.VolumeDialogImpl.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                VolumeDialogImpl.this.dismissH(8);
                VolumeDialogImpl.this.showOutputChooserH();
            }
        };
        loadOpDimens();
        if (OpUtils.DEBUG_ONEPLUS) {
            String str = TAG;
            Log.i(str, "Vol Controller, mOpBeforeExpandWidth:" + this.mOpBeforeExpandWidth + ", mOpafterExpandWidth:" + this.mOpafterExpandWidth);
        }
        this.mDeviceInfo = new OpVolumeDialogImpl.DeviceInfo(this);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        this.mContext.getTheme().applyStyle(this.mContext.getThemeResId(), true);
    }

    @Override // com.android.systemui.plugins.VolumeDialog
    public void init(int i, VolumeDialog.Callback callback) {
        initDialog();
        this.mAccessibility.init();
        this.mController.addCallback(this.mControllerCallbackH, this.mHandler);
        this.mController.getState();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    @Override // com.android.systemui.plugins.VolumeDialog
    public void destroy() {
        this.mController.removeCallback(this.mControllerCallbackH);
        this.mHandler.removeCallbacksAndMessages(null);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    private void initDialog() {
        this.mDialog = new CustomDialog(this.mContext);
        this.mConfigurableTexts = new ConfigurableTexts(this.mContext);
        this.mHovering = false;
        this.mShowing = false;
        Window window = this.mDialog.getWindow();
        this.mWindow = window;
        window.requestFeature(1);
        this.mWindow.setBackgroundDrawable(new ColorDrawable(0));
        this.mWindow.clearFlags(65538);
        this.mWindow.addFlags(17563688);
        this.mWindow.setType(2020);
        this.mWindow.setWindowAnimations(16973828);
        WindowManager.LayoutParams attributes = this.mWindow.getAttributes();
        attributes.format = -3;
        attributes.setTitle(VolumeDialogImpl.class.getSimpleName());
        attributes.gravity = isLandscape() ? 21 : 19;
        attributes.windowAnimations = -1;
        this.mWindow.setAttributes(attributes);
        this.mWindow.setLayout(-2, -2);
        try {
            this.mDialog.setContentView(C0011R$layout.op_volume_dialog);
            this.mDialogView = (ViewGroup) this.mDialog.findViewById(C0008R$id.volume_dialog);
            this.mDialogUpper = (ViewGroup) this.mDialog.findViewById(C0008R$id.volume_dialog_upper);
            this.mDialogLower = (ViewGroup) this.mDialog.findViewById(C0008R$id.volume_dialog_lower);
            this.mDialogView.setAlpha(0.0f);
            this.mDialog.setCanceledOnTouchOutside(true);
            this.mDialog.setOnShowListener(new DialogInterface.OnShowListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$8BZhTIdOE2rPYfFa5HbcUDCtXeM
                @Override // android.content.DialogInterface.OnShowListener
                public final void onShow(DialogInterface dialogInterface) {
                    VolumeDialogImpl.this.lambda$initDialog$1$VolumeDialogImpl(dialogInterface);
                }
            });
            this.mDialogView.setOnHoverListener(new View.OnHoverListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$T52d0W13mYvykk6ORgbytqfZsps
                @Override // android.view.View.OnHoverListener
                public final boolean onHover(View view, MotionEvent motionEvent) {
                    return VolumeDialogImpl.this.lambda$initDialog$2$VolumeDialogImpl(view, motionEvent);
                }
            });
            this.mDialogView.setOnTouchListener(new View.OnTouchListener(this) { // from class: com.android.systemui.volume.VolumeDialogImpl.2
                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (!OpUtils.DEBUG_ONEPLUS) {
                        return true;
                    }
                    Log.i(VolumeDialogImpl.TAG, "DialogView, onTouch");
                    return true;
                }
            });
            this.mDialogRowContainer = (ViewGroup) this.mDialog.findViewById(C0008R$id.volume_dialog_row_container);
            this.mDialogRowContainerBottom = (ViewGroup) this.mDialog.findViewById(C0008R$id.volume_dialog_row_container_bottom);
            this.mDialogRowsView = (ViewGroup) this.mDialog.findViewById(C0008R$id.volume_dialog_rows);
            ViewGroup viewGroup = (ViewGroup) this.mDialog.findViewById(C0008R$id.ringer);
            this.mRinger = viewGroup;
            if (viewGroup != null) {
                this.mRingerIcon = (ImageButton) viewGroup.findViewById(C0008R$id.ringer_icon);
                this.mZenIcon = (FrameLayout) this.mRinger.findViewById(C0008R$id.dnd_icon);
            }
            ViewGroup viewGroup2 = (ViewGroup) this.mDialog.findViewById(C0008R$id.odi_captions);
            this.mODICaptionsView = viewGroup2;
            if (viewGroup2 != null) {
                this.mODICaptionsIcon = (CaptionsToggleImageButton) viewGroup2.findViewById(C0008R$id.odi_captions_icon);
            }
            ViewStub viewStub = (ViewStub) this.mDialog.findViewById(C0008R$id.odi_captions_tooltip_stub);
            this.mODICaptionsTooltipViewStub = viewStub;
            if (this.mHasSeenODICaptionsTooltip && viewStub != null) {
                this.mDialogView.removeView(viewStub);
                this.mODICaptionsTooltipViewStub = null;
            }
            if (this.mODICaptionsView != null) {
                if (OpUtils.DEBUG_ONEPLUS) {
                    String str = TAG;
                    Log.i(str, "initDialog, mODICaptionsView:" + this.mODICaptionsView.getVisibility() + ", mIsCaptionComponentEnabled:" + this.mIsCaptionComponentEnabled + ", mIsOpZenModeOn:" + this.mIsOpZenModeOn);
                }
                updateODICaptionsH(this.mIsCaptionComponentEnabled, false);
            }
            this.mSettingsView = this.mDialog.findViewById(C0008R$id.settings_container);
            this.mSettingsIcon = (ImageButton) this.mDialog.findViewById(C0008R$id.settings);
            ImageButton imageButton = (ImageButton) this.mDialogView.findViewById(C0008R$id.output_chooser);
            this.mOutputChooser = imageButton;
            imageButton.setOnClickListener(this.mClickOutputChooser);
            this.mConnectedDevice = (TextView) this.mDialogView.findViewById(C0008R$id.volume_row_connected_device);
            this.mSettingsBackView = this.mDialog.findViewById(C0008R$id.settings_back_container);
            this.mSettingsBackIcon = (ImageButton) this.mDialog.findViewById(C0008R$id.settings_back);
            this.mSettingsOpSettingsView = this.mDialog.findViewById(C0008R$id.settings_opsettings_container);
            this.mSettingsOpSettingsIcon = (ImageButton) this.mDialog.findViewById(C0008R$id.settings_opsettings);
            this.mOutputChooserExpandIcon = (ImageView) this.mDialogView.findViewById(C0008R$id.output_active_device_icon);
            this.mOutputChooserExpandEditText = (EditText) this.mDialogView.findViewById(C0008R$id.output_active_device_name);
            this.mOutputChooserExpandTextView = (TextView) this.mDialogView.findViewById(C0008R$id.output_active_device_name_marquee);
            final ViewGroup viewGroup3 = (ViewGroup) this.mDialog.findViewById(C0008R$id.volume_dialog_container);
            viewGroup3.setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.volume.VolumeDialogImpl.3
                @Override // android.view.View.OnTouchListener
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (OpUtils.DEBUG_ONEPLUS) {
                        Log.i(VolumeDialogImpl.TAG, "volume_dialog_container, onTouch");
                    }
                    if (!VolumeDialogImpl.this.isLandscape()) {
                        int i = ((OpVolumeDialogImpl) VolumeDialogImpl.this).mOpForceExpandState ? ((OpVolumeDialogImpl) VolumeDialogImpl.this).mOpafterExpandWidth : ((OpVolumeDialogImpl) VolumeDialogImpl.this).mOpBeforeExpandWidth;
                        String str2 = VolumeDialogImpl.TAG;
                        Log.i(str2, "volume_dialog_container, width:" + i);
                        if (((int) motionEvent.getX()) > i) {
                            VolumeDialogImpl.this.dismissH(1);
                        }
                    }
                    if (VolumeDialogImpl.this.isLandscape() && ((int) motionEvent.getX()) < viewGroup3.getLayoutParams().width - ((OpVolumeDialogImpl) VolumeDialogImpl.this).mOpBeforeExpandWidth) {
                        VolumeDialogImpl.this.dismissH(1);
                    }
                    return true;
                }
            });
            View findViewById = this.mDialog.findViewById(C0008R$id.output_chooser_background_container);
            this.mOutputChooserBackgroundView = findViewById;
            findViewById.setOnClickListener(this.mClickOutputChooser);
            if (this.mRows.isEmpty()) {
                if (!AudioSystem.isSingleVolume(this.mContext)) {
                    int i = C0006R$drawable.ic_volume_accessibility;
                    addRow(10, i, i, true, false);
                }
                addRow(3, C0006R$drawable.ic_volume_media, C0006R$drawable.ic_volume_media_mute, true, true);
                if (!AudioSystem.isSingleVolume(this.mContext)) {
                    addRow(2, C0006R$drawable.ic_volume_ringer, C0006R$drawable.ic_volume_ringer_mute, true, false);
                    addRow(4, C0006R$drawable.op_ic_volume_alarm, C0006R$drawable.ic_volume_alarm_mute, true, false);
                    if (ProductUtils.isUsvMode()) {
                        int i2 = C0006R$drawable.ic_local_phone_24_lib;
                        addRow(0, i2, i2, true, false);
                    } else {
                        addRow(0, 17302790, 17302790, false, false);
                    }
                    int i3 = C0006R$drawable.ic_volume_bt_sco;
                    addRow(6, i3, i3, false, false);
                    addRow(1, C0006R$drawable.ic_volume_system, C0006R$drawable.ic_volume_system_mute, false, false);
                }
            } else {
                addExistingRows();
            }
            updateRowsH(getActiveRow());
            initRingerH();
            initSettingsH();
            initODICaptionsH();
            this.mAccentColor = ThemeColorUtils.getColor(100);
            this.mController.getState();
            applyColorTheme(true);
            updateDialogLayout();
            ViewGroup viewGroup4 = this.mODICaptionsView;
            if (viewGroup4 != null && viewGroup4.getVisibility() == 0) {
                updateODIRelatedLayout();
            }
        } catch (Exception e) {
            String str2 = TAG;
            Log.d(str2, "setContentView Exception: " + e.toString());
            initDialog();
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initDialog$1 */
    private /* synthetic */ void lambda$initDialog$1$VolumeDialogImpl(DialogInterface dialogInterface) {
        if (isLandscape()) {
            ViewGroup viewGroup = this.mDialogView;
            viewGroup.setTranslationX(((float) viewGroup.getWidth()) / 2.0f);
        } else {
            ViewGroup viewGroup2 = this.mDialogView;
            viewGroup2.setTranslationX(((float) (-viewGroup2.getWidth())) / 2.0f);
        }
        this.mDialogView.setAlpha(0.0f);
        this.mDialogView.animate().alpha(1.0f).translationX(0.0f).setDuration(300).setInterpolator(new SystemUIInterpolators$LogDecelerateInterpolator()).withEndAction(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$vBH_Cy2LsLvfluWDg0W4IzJ1dm8
            @Override // java.lang.Runnable
            public final void run() {
                VolumeDialogImpl.this.lambda$initDialog$0$VolumeDialogImpl();
            }
        }).start();
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initDialog$0 */
    private /* synthetic */ void lambda$initDialog$0$VolumeDialogImpl() {
        ImageButton imageButton;
        if (!Prefs.getBoolean(this.mContext, "TouchedRingerToggle", false) && (imageButton = this.mRingerIcon) != null) {
            imageButton.postOnAnimationDelayed(getSinglePressFor(imageButton), 1500);
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initDialog$2 */
    private /* synthetic */ boolean lambda$initDialog$2$VolumeDialogImpl(View view, MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        this.mHovering = actionMasked == 9 || actionMasked == 7;
        rescheduleTimeoutH();
        return true;
    }

    private int getAlphaAttr(int i) {
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(new int[]{i});
        float f = obtainStyledAttributes.getFloat(0, 0.0f);
        obtainStyledAttributes.recycle();
        return (int) (f * 255.0f);
    }

    private boolean isLandscape() {
        return this.mContext.getResources().getConfiguration().orientation == 2;
    }

    public void setStreamImportant(int i, boolean z) {
        this.mHandler.obtainMessage(5, i, z ? 1 : 0).sendToTarget();
    }

    public void setAutomute(boolean z) {
        if (this.mAutomute != z) {
            this.mAutomute = z;
            this.mHandler.sendEmptyMessage(4);
        }
    }

    public void setSilentMode(boolean z) {
        if (this.mSilentMode != z) {
            this.mSilentMode = z;
            this.mHandler.sendEmptyMessage(4);
        }
    }

    private void addRow(int i, int i2, int i3, boolean z, boolean z2) {
        addRow(i, i2, i3, z, z2, false);
    }

    private void addRow(int i, int i2, int i3, boolean z, boolean z2, boolean z3) {
        if (D.BUG) {
            String str = TAG;
            Slog.d(str, "Adding row for stream " + i);
        }
        OpVolumeDialogImpl.VolumeRow volumeRow = new OpVolumeDialogImpl.VolumeRow();
        initRow(volumeRow, i, i2, i3, z, z2);
        this.mDialogRowsView.addView(volumeRow.view);
        this.mRows.add(volumeRow);
    }

    private void addExistingRows() {
        int size = this.mRows.size();
        for (int i = 0; i < size; i++) {
            OpVolumeDialogImpl.VolumeRow volumeRow = this.mRows.get(i);
            initRow(volumeRow, volumeRow.stream, volumeRow.iconRes, volumeRow.iconMuteRes, volumeRow.important, volumeRow.defaultStream);
            this.mDialogRowsView.addView(volumeRow.view);
            updateVolumeRowH(volumeRow);
        }
    }

    private OpVolumeDialogImpl.VolumeRow getActiveRow() {
        for (OpVolumeDialogImpl.VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == this.mActiveStream) {
                return volumeRow;
            }
        }
        for (OpVolumeDialogImpl.VolumeRow volumeRow2 : this.mRows) {
            if (volumeRow2.stream == 3) {
                return volumeRow2;
            }
        }
        return this.mRows.get(0);
    }

    private OpVolumeDialogImpl.VolumeRow findRow(int i) {
        for (OpVolumeDialogImpl.VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == i) {
                return volumeRow;
            }
        }
        return null;
    }

    public static int getImpliedLevel(SeekBar seekBar, int i) {
        int max = seekBar.getMax();
        int i2 = max / 100;
        int i3 = i2 - 1;
        if (i == 0) {
            return 0;
        }
        return i == max ? i2 : ((int) ((((float) i) / ((float) max)) * ((float) i3))) + 1;
    }

    @SuppressLint({"InflateParams"})
    private void initRow(OpVolumeDialogImpl.VolumeRow volumeRow, int i, int i2, int i3, boolean z, boolean z2) {
        volumeRow.stream = i;
        volumeRow.iconRes = i2;
        volumeRow.iconMuteRes = i3;
        volumeRow.important = z;
        volumeRow.defaultStream = z2;
        View inflate = this.mDialog.getLayoutInflater().inflate(C0011R$layout.op_volume_dialog_row, (ViewGroup) null);
        volumeRow.view = inflate;
        inflate.setId(volumeRow.stream);
        volumeRow.view.setTag(volumeRow);
        TextView textView = (TextView) volumeRow.view.findViewById(C0008R$id.volume_row_header);
        volumeRow.header = textView;
        textView.setId(volumeRow.stream * 20);
        if (i == 10) {
            volumeRow.header.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        }
        volumeRow.dndIcon = (FrameLayout) volumeRow.view.findViewById(C0008R$id.dnd_icon);
        SeekBar seekBar = (SeekBar) volumeRow.view.findViewById(C0008R$id.volume_row_slider);
        volumeRow.slider = seekBar;
        seekBar.setOnSeekBarChangeListener(new VolumeSeekBarChangeListener(volumeRow));
        volumeRow.anim = null;
        ImageButton imageButton = (ImageButton) volumeRow.view.findViewById(C0008R$id.volume_row_icon);
        volumeRow.icon = imageButton;
        imageButton.setImageResource(i2);
        if (volumeRow.stream != 10) {
            volumeRow.icon.setOnClickListener(new View.OnClickListener(volumeRow, i) { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$faxejaGfFb01-SDF9TessAqe10c
                public final /* synthetic */ OpVolumeDialogImpl.VolumeRow f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    VolumeDialogImpl.this.lambda$initRow$3$VolumeDialogImpl(this.f$1, this.f$2, view);
                }
            });
        } else {
            volumeRow.icon.setImportantForAccessibility(2);
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initRow$3 */
    private /* synthetic */ void lambda$initRow$3$VolumeDialogImpl(OpVolumeDialogImpl.VolumeRow volumeRow, int i, View view) {
        boolean z = false;
        Events.writeEvent(7, Integer.valueOf(volumeRow.stream), Integer.valueOf(volumeRow.iconState));
        if (Build.DEBUG_ONEPLUS) {
            Log.i(TAG, "initRow setOnClickListener row.stream:" + volumeRow.stream + ", row.iconState:" + volumeRow.iconState + ", row.ss.level:" + volumeRow.ss.level + ", row.ss.levelMin:" + volumeRow.ss.levelMin + ", row.lastAudibleLevel:" + volumeRow.lastAudibleLevel);
        }
        this.mController.setActiveStream(volumeRow.stream);
        if (volumeRow.stream != 2) {
            VolumeDialogController.StreamState streamState = volumeRow.ss;
            if (streamState.level == streamState.levelMin) {
                z = true;
            }
            this.mController.setStreamVolume(i, z ? volumeRow.lastAudibleLevel : volumeRow.ss.levelMin);
        } else if (volumeRow.ss.level == 1) {
            this.mController.setStreamVolume(i, volumeRow.lastAudibleLevel);
        } else {
            this.mController.setStreamVolume(i, 1);
        }
        volumeRow.userAttempt = 0;
    }

    public void initSettingsH() {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i(TAG, "initSettingsH");
        }
        ViewGroup viewGroup = this.mDialogRowContainerBottom;
        int i = 0;
        if (viewGroup != null) {
            viewGroup.setVisibility(this.mIsOpZenModeOn ? 8 : 0);
        }
        long j = 0;
        long j2 = 275;
        float f = 0.5f;
        float f2 = 0.0f;
        if (this.mSettingsView != null) {
            int i2 = (!this.mDeviceProvisionedController.isCurrentUserSetup() || this.mActivityManager.getLockTaskModeState() != 0 || this.mOpForceExpandState) ? 8 : 0;
            if (OpUtils.DEBUG_ONEPLUS) {
                Log.i(TAG, "initSettingsH, targetBottomSettingsIconVisible: " + i2 + ", mTargetBottomSettingsIconVisible:" + this.mTargetBottomSettingsIconVisible);
            }
            if (this.mTargetBottomSettingsIconVisible != i2) {
                this.mTargetBottomSettingsIconVisible = i2;
                if (i2 == 0) {
                    this.mSettingsView.setVisibility(0);
                }
                this.mSettingsView.animate().alpha(i2 == 8 ? 0.0f : 1.0f).scaleX(i2 == 8 ? 0.5f : 1.0f).scaleY(i2 == 8 ? 0.5f : 1.0f).setDuration(275).setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f)).withEndAction(new Runnable(i2) { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$tYqu_SzICRK6KaTw5NCx1axBwhY
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        VolumeDialogImpl.this.lambda$initSettingsH$4$VolumeDialogImpl(this.f$1);
                    }
                }).setStartDelay(0);
            } else {
                this.mSettingsView.setVisibility(i2);
                this.mSettingsView.setAlpha(i2 == 8 ? 0.0f : 1.0f);
            }
        }
        ImageButton imageButton = this.mSettingsIcon;
        if (imageButton != null) {
            imageButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$yhKa93yBvDMghPsrninv77nitIM
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    VolumeDialogImpl.this.lambda$initSettingsH$5$VolumeDialogImpl(view);
                }
            });
        }
        if (this.mSettingsBackView != null) {
            int i3 = this.mOpForceExpandState ? 0 : 8;
            if (this.mTargetBottomSettingsBackIconVisible != i3) {
                this.mTargetBottomSettingsBackIconVisible = i3;
                if (i3 == 0) {
                    this.mSettingsBackView.setVisibility(0);
                }
                this.mSettingsBackView.animate().alpha(i3 == 8 ? 0.0f : 1.0f).scaleX(i3 == 8 ? 0.5f : 1.0f).scaleY(i3 == 8 ? 0.5f : 1.0f).setDuration(275).setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f)).withEndAction(new Runnable(i3) { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$s2Cuv-jqDV5qd2d-dbjuR_YugHQ
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        VolumeDialogImpl.this.lambda$initSettingsH$6$VolumeDialogImpl(this.f$1);
                    }
                }).setStartDelay(0);
            } else {
                this.mSettingsBackView.setVisibility(i3);
                this.mSettingsBackView.setAlpha(i3 == 8 ? 0.0f : 1.0f);
            }
        }
        if (this.mSettingsBackIcon != null) {
            if (isLandscape()) {
                this.mSettingsBackIcon.setRotation(180.0f);
            } else {
                this.mSettingsBackIcon.setRotation(0.0f);
            }
            this.mSettingsBackIcon.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$_tPh_IeO5Q_gkFTTSsZazreKzHw
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    VolumeDialogImpl.this.lambda$initSettingsH$7$VolumeDialogImpl(view);
                }
            });
        }
        if (this.mSettingsOpSettingsView != null) {
            if (!this.mOpForceExpandState) {
                i = 8;
            }
            if (this.mTargetBottomSettingsOpSettingsIconVisible != i) {
                this.mTargetBottomSettingsOpSettingsIconVisible = i;
                ViewPropertyAnimator scaleX = this.mSettingsOpSettingsView.animate().alpha(i == 8 ? 0.0f : 1.0f).scaleX(i == 8 ? 0.5f : 1.0f);
                if (i != 8) {
                    f = 1.0f;
                }
                ViewPropertyAnimator scaleY = scaleX.scaleY(f);
                if (!this.mOpForceExpandState) {
                    j2 = 120;
                }
                ViewPropertyAnimator withEndAction = scaleY.setDuration(j2).setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f)).withStartAction(new Runnable(i) { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$3q1KL3g4O5A3gVZ4JD2dByVrkNI
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        VolumeDialogImpl.this.lambda$initSettingsH$8$VolumeDialogImpl(this.f$1);
                    }
                }).withEndAction(new Runnable(i) { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$3i1zvcqG_4mrqN_GeZ7K60UeisA
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        VolumeDialogImpl.this.lambda$initSettingsH$9$VolumeDialogImpl(this.f$1);
                    }
                });
                if (this.mOpForceExpandState) {
                    j = 91;
                }
                withEndAction.setStartDelay(j);
            } else {
                this.mSettingsOpSettingsView.setVisibility(i);
                View view = this.mSettingsOpSettingsView;
                if (i != 8) {
                    f2 = 1.0f;
                }
                view.setAlpha(f2);
            }
        }
        ImageButton imageButton2 = this.mSettingsOpSettingsIcon;
        if (imageButton2 != null) {
            imageButton2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$vFGDLS9dLMW9Hn6QccZRosNtBGU
                @Override // android.view.View.OnClickListener
                public final void onClick(View view2) {
                    VolumeDialogImpl.this.lambda$initSettingsH$10$VolumeDialogImpl(view2);
                }
            });
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initSettingsH$4 */
    private /* synthetic */ void lambda$initSettingsH$4$VolumeDialogImpl(int i) {
        this.mSettingsView.setVisibility(i);
        this.mSettingsView.setAlpha(i == 8 ? 0.0f : 1.0f);
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initSettingsH$5 */
    private /* synthetic */ void lambda$initSettingsH$5$VolumeDialogImpl(View view) {
        Events.writeEvent(8, new Object[0]);
        if (Build.DEBUG_ONEPLUS) {
            Log.i(TAG, "mSettingsIcon click");
        }
        if ((this.mAnimVol == null || this.mIsExpandAnimDone) && !this.mOpForceExpandState) {
            hideCaptionsTooltipImmediately();
            this.mOpLastforceExpandState = this.mOpForceExpandState;
            this.mOpForceExpandState = true;
            this.mNeedPlayExpandAnim = true;
            loadOpDimens();
            setDialogWidth(this.mOpafterExpandWidth);
            updateRowsH(getActiveRow());
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initSettingsH$6 */
    private /* synthetic */ void lambda$initSettingsH$6$VolumeDialogImpl(int i) {
        this.mSettingsBackView.setVisibility(i);
        this.mSettingsBackView.setAlpha(i == 8 ? 0.0f : 1.0f);
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initSettingsH$7 */
    private /* synthetic */ void lambda$initSettingsH$7$VolumeDialogImpl(View view) {
        boolean z;
        if (Build.DEBUG_ONEPLUS) {
            Log.i(TAG, "mSettingsBackIcon click");
        }
        if ((this.mAnimVol == null || this.mIsExpandAnimDone) && (z = this.mOpForceExpandState)) {
            this.mOpLastforceExpandState = z;
            this.mOpForceExpandState = false;
            this.mNeedPlayExpandAnim = true;
            setDialogWidth(this.mOpafterExpandWidth);
            updateRowsH(getActiveRow());
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initSettingsH$8 */
    private /* synthetic */ void lambda$initSettingsH$8$VolumeDialogImpl(int i) {
        if (i == 0) {
            this.mSettingsOpSettingsView.setVisibility(0);
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initSettingsH$9 */
    private /* synthetic */ void lambda$initSettingsH$9$VolumeDialogImpl(int i) {
        this.mSettingsOpSettingsView.setVisibility(i);
        this.mSettingsOpSettingsView.setAlpha(i == 8 ? 0.0f : 1.0f);
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initSettingsH$10 */
    private /* synthetic */ void lambda$initSettingsH$10$VolumeDialogImpl(View view) {
        if (Build.DEBUG_ONEPLUS) {
            Log.i(TAG, "mSettingsOpSettingsIcon click");
        }
        if ((this.mAnimVol == null || this.mIsExpandAnimDone) && this.mOpForceExpandState) {
            Intent intent = new Intent("android.settings.SOUND_SETTINGS");
            dismissH(5);
            ((ActivityStarter) Dependency.get(ActivityStarter.class)).startActivity(intent, true);
        }
    }

    public void initRingerH() {
        ImageButton imageButton = this.mRingerIcon;
        if (imageButton != null) {
            imageButton.setAccessibilityLiveRegion(1);
            this.mRingerIcon.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$wxx-1NamOLWvH0efv0Fvn1ZXt8A
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    VolumeDialogImpl.this.lambda$initRingerH$11$VolumeDialogImpl(view);
                }
            });
        }
        updateRingerH();
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initRingerH$11 */
    private /* synthetic */ void lambda$initRingerH$11$VolumeDialogImpl(View view) {
        Prefs.putBoolean(this.mContext, "TouchedRingerToggle", true);
        int i = 2;
        VolumeDialogController.StreamState streamState = this.mState.states.get(2);
        if (streamState != null) {
            boolean hasVibrator = this.mController.hasVibrator();
            int i2 = this.mState.ringerModeInternal;
            if (i2 == 2) {
                if (hasVibrator) {
                    i = 1;
                    Events.writeEvent(18, Integer.valueOf(i));
                    incrementManualToggleCount();
                    updateRingerH();
                    provideTouchFeedbackH(i);
                    this.mController.setRingerMode(i, false);
                    maybeShowToastH(i);
                }
            } else if (i2 != 1) {
                if (streamState.level == 0) {
                    this.mController.setStreamVolume(2, 1);
                }
                Events.writeEvent(18, Integer.valueOf(i));
                incrementManualToggleCount();
                updateRingerH();
                provideTouchFeedbackH(i);
                this.mController.setRingerMode(i, false);
                maybeShowToastH(i);
            }
            i = 0;
            Events.writeEvent(18, Integer.valueOf(i));
            incrementManualToggleCount();
            updateRingerH();
            provideTouchFeedbackH(i);
            this.mController.setRingerMode(i, false);
            maybeShowToastH(i);
        }
    }

    private void initODICaptionsH() {
        CaptionsToggleImageButton captionsToggleImageButton = this.mODICaptionsIcon;
        if (captionsToggleImageButton != null) {
            captionsToggleImageButton.setOnConfirmedTapListener(new CaptionsToggleImageButton.ConfirmedTapListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$9fzv42wROG9GvTVjJNs8RbTVwQs
                @Override // com.android.systemui.volume.CaptionsToggleImageButton.ConfirmedTapListener
                public final void onConfirmedTap() {
                    VolumeDialogImpl.this.lambda$initODICaptionsH$12$VolumeDialogImpl();
                }
            }, this.mHandler);
        }
        this.mController.getCaptionsComponentState(false);
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$initODICaptionsH$12 */
    private /* synthetic */ void lambda$initODICaptionsH$12$VolumeDialogImpl() {
        onCaptionIconClicked();
        Events.writeEvent(21, new Object[0]);
    }

    private void checkODICaptionsTooltip(boolean z) {
        if (!this.mHasSeenODICaptionsTooltip && !z && this.mODICaptionsTooltipViewStub != null) {
            this.mController.getCaptionsComponentState(true);
        } else if (this.mHasSeenODICaptionsTooltip && z && this.mODICaptionsTooltipView != null) {
            hideCaptionsTooltip();
        }
    }

    public void showCaptionsTooltip() {
        ViewStub viewStub;
        if (!this.mHasSeenODICaptionsTooltip && (viewStub = this.mODICaptionsTooltipViewStub) != null) {
            View inflate = viewStub.inflate();
            this.mODICaptionsTooltipView = inflate;
            inflate.findViewById(C0008R$id.dismiss).setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$XF6c34Qrx7dMvHKAz_Q6TKl64lY
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    VolumeDialogImpl.this.lambda$showCaptionsTooltip$13$VolumeDialogImpl(view);
                }
            });
            this.mODICaptionsTooltipViewStub = null;
            rescheduleTimeoutH();
        }
        if (this.mODICaptionsTooltipView != null) {
            if (isLandscape()) {
                setDialogWidth((int) (((double) this.mOpafterExpandWidth) * 2.5d));
            }
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mODICaptionsTooltipView.getLayoutParams();
            int measuredWidth = (int) (((double) this.mDialogLower.getMeasuredWidth()) * 1.4d);
            int i = (int) (((double) this.mODIViewHeight) * 0.3d);
            if (isLandscape()) {
                layoutParams.gravity = 85;
                layoutParams.setMargins(0, 0, measuredWidth, i);
            } else {
                layoutParams.gravity = 83;
                layoutParams.setMargins(measuredWidth, 0, 0, i);
            }
            this.mODICaptionsTooltipView.setLayoutParams(layoutParams);
            this.mODICaptionsTooltipView.setAlpha(0.0f);
            this.mODICaptionsTooltipView.animate().alpha(1.0f).setStartDelay(300).withEndAction(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$VfqQNQdyxat1ut-LHF0mfB9W3dg
                @Override // java.lang.Runnable
                public final void run() {
                    VolumeDialogImpl.this.lambda$showCaptionsTooltip$14$VolumeDialogImpl();
                }
            }).start();
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$showCaptionsTooltip$13 */
    private /* synthetic */ void lambda$showCaptionsTooltip$13$VolumeDialogImpl(View view) {
        hideCaptionsTooltip();
        Events.writeEvent(22, new Object[0]);
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$showCaptionsTooltip$14 */
    private /* synthetic */ void lambda$showCaptionsTooltip$14$VolumeDialogImpl() {
        if (D.BUG) {
            Log.d(TAG, "tool:checkODICaptionsTooltip() putBoolean true");
        }
        if (!this.mODIDebug) {
            Prefs.putBoolean(this.mContext, "HasSeenODICaptionsTooltip", true);
            this.mHasSeenODICaptionsTooltip = true;
        }
        CaptionsToggleImageButton captionsToggleImageButton = this.mODICaptionsIcon;
        if (captionsToggleImageButton != null) {
            captionsToggleImageButton.postOnAnimation(getSinglePressFor(captionsToggleImageButton));
        }
    }

    private void hideCaptionsTooltip() {
        View view = this.mODICaptionsTooltipView;
        if (view != null && view.getVisibility() == 0) {
            if (isLandscape()) {
                setDialogWidth(this.mOpafterExpandWidth);
            }
            this.mODICaptionsTooltipView.animate().cancel();
            this.mODICaptionsTooltipView.setAlpha(1.0f);
            this.mODICaptionsTooltipView.animate().alpha(0.0f).setStartDelay(0).setDuration(250).withEndAction(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$c8tgC19HinFoEv5JhfgLli1ykrw
                @Override // java.lang.Runnable
                public final void run() {
                    VolumeDialogImpl.this.lambda$hideCaptionsTooltip$15$VolumeDialogImpl();
                }
            }).start();
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$hideCaptionsTooltip$15 */
    private /* synthetic */ void lambda$hideCaptionsTooltip$15$VolumeDialogImpl() {
        View view = this.mODICaptionsTooltipView;
        if (view != null) {
            view.setVisibility(8);
        }
    }

    private void hideCaptionsTooltipImmediately() {
        View view = this.mODICaptionsTooltipView;
        if (view != null && view.getVisibility() == 0) {
            if (isLandscape()) {
                setDialogWidth(this.mOpafterExpandWidth);
            }
            this.mODICaptionsTooltipView.setVisibility(8);
        }
    }

    public void tryToRemoveCaptionsTooltip() {
        if (this.mHasSeenODICaptionsTooltip && this.mODICaptionsTooltipView != null) {
            ((ViewGroup) this.mDialog.findViewById(C0008R$id.volume_dialog_container)).removeView(this.mODICaptionsTooltipView);
            this.mODICaptionsTooltipView = null;
        }
    }

    private void updateODICaptionsH(boolean z, boolean z2) {
        if (this.mODICaptionsView != null) {
            if (z) {
                updateODIRelatedLayout();
            }
            String str = TAG;
            Log.i(str, "updateODICaptionsH, isServiceComponentEnabled:" + z + ", mIsOpZenModeOn:" + this.mIsOpZenModeOn);
            this.mODICaptionsView.setVisibility((!z || this.mIsOpZenModeOn) ? 8 : 0);
        }
        if (z) {
            updateCaptionsIcon();
            if (z2) {
                showCaptionsTooltip();
            }
        }
    }

    private void updateCaptionsIcon() {
        boolean areCaptionsEnabled = this.mController.areCaptionsEnabled();
        if (this.mODICaptionsIcon.getCaptionsEnabled() != areCaptionsEnabled) {
            this.mHandler.post(this.mODICaptionsIcon.setCaptionsEnabled(areCaptionsEnabled));
        }
        boolean isCaptionStreamOptedOut = this.mController.isCaptionStreamOptedOut();
        if (this.mODICaptionsIcon.getOptedOut() != isCaptionStreamOptedOut) {
            this.mHandler.post(new Runnable(isCaptionStreamOptedOut) { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$DIKiMyXI1bMyTRDvEPHkZewFa_o
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    VolumeDialogImpl.this.lambda$updateCaptionsIcon$16$VolumeDialogImpl(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$updateCaptionsIcon$16 */
    private /* synthetic */ void lambda$updateCaptionsIcon$16$VolumeDialogImpl(boolean z) {
        this.mODICaptionsIcon.setOptedOut(z);
    }

    private void onCaptionIconClicked() {
        this.mController.setCaptionsEnabled(!this.mController.areCaptionsEnabled());
        updateCaptionsIcon();
    }

    private void incrementManualToggleCount() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Secure.putInt(contentResolver, "manual_ringer_toggle_count", Settings.Secure.getInt(contentResolver, "manual_ringer_toggle_count", 0) + 1);
    }

    private void provideTouchFeedbackH(int i) {
        VibrationEffect vibrationEffect;
        if (i == 0) {
            vibrationEffect = VibrationEffect.get(0);
        } else if (i != 2) {
            vibrationEffect = VibrationEffect.get(1);
        } else {
            this.mController.scheduleTouchFeedback();
            vibrationEffect = null;
        }
        if (vibrationEffect != null) {
            this.mController.vibrate(vibrationEffect);
        }
    }

    private void maybeShowToastH(int i) {
        int i2 = Prefs.getInt(this.mContext, "RingerGuidanceCount", 0);
        if (i2 <= 12) {
            String str = null;
            if (i == 0) {
                str = this.mContext.getString(17041434);
            } else if (i != 2) {
                str = this.mContext.getString(17041435);
            } else {
                VolumeDialogController.StreamState streamState = this.mState.states.get(2);
                if (streamState != null) {
                    str = this.mContext.getString(C0015R$string.volume_dialog_ringer_guidance_ring, Utils.formatPercentage((long) streamState.level, (long) streamState.levelMax));
                }
            }
            Toast.makeText(this.mContext, str, 0).show();
            Prefs.putInt(this.mContext, "RingerGuidanceCount", i2 + 1);
        }
    }

    private void showH(int i) {
        boolean z;
        if (Build.DEBUG_ONEPLUS) {
            Log.d(TAG, "showH r=" + Events.SHOW_REASONS[i] + ", pending=" + this.mPendingInit);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        rescheduleTimeoutH();
        if (this.mConfigChanged) {
            initDialog();
            this.mConfigurableTexts.update();
            this.mConfigChanged = false;
            z = true;
        } else {
            z = false;
        }
        if (this.mDialog != null && this.mPendingInit) {
            if (!z) {
                initDialog();
                z = true;
            }
            this.mPendingInit = false;
        }
        if (i == 4 && !z) {
            initDialog();
        }
        initSettingsH();
        this.mShowing = true;
        applyColorTheme(false);
        this.mIsAnimatingDismiss = false;
        this.mDialog.show();
        Events.writeEvent(0, Integer.valueOf(i), Boolean.valueOf(this.mKeyguard.isKeyguardLocked()));
        this.mController.notifyVisible(true);
        this.mController.getCaptionsComponentState(false);
        checkODICaptionsTooltip(false);
        if (this.mOpOutputChooser == null) {
            OpOutputChooser opOutputChooser = new OpOutputChooser(this.mContext);
            this.mOpOutputChooser = opOutputChooser;
            opOutputChooser.addCallback(this.mOutputChooserCallback);
        }
        if (isLandscape()) {
            setDialogWidth(this.mOpafterExpandWidth);
        } else {
            setDialogWidth(-2);
        }
        this.mFirstTimeInitDialog = false;
    }

    public void rescheduleTimeoutH() {
        this.mHandler.removeMessages(2);
        int computeTimeoutH = computeTimeoutH();
        H h = this.mHandler;
        h.sendMessageDelayed(h.obtainMessage(2, 3, 0), (long) computeTimeoutH);
        if (Build.DEBUG_ONEPLUS || D.BUG) {
            String str = TAG;
            Log.d(str, "rescheduleTimeout " + computeTimeoutH + " " + Debug.getCaller());
        }
        this.mController.userActivity();
    }

    private int computeTimeoutH() {
        if (this.mHovering) {
            return this.mAccessibilityMgr.getRecommendedTimeoutMillis(16000, 4);
        }
        if (this.mSafetyWarning != null) {
            return this.mAccessibilityMgr.getRecommendedTimeoutMillis(5000, 6);
        }
        if (!this.mHasSeenODICaptionsTooltip && this.mODICaptionsTooltipView != null) {
            return this.mAccessibilityMgr.getRecommendedTimeoutMillis(5000, 6);
        }
        if (!isStatusBarShowing()) {
            return 1500;
        }
        return this.mAccessibilityMgr.getRecommendedTimeoutMillis(3000, 4);
    }

    private void setDismissDialog() {
        this.mIsAnimatingDismiss = false;
    }

    public void dismissH(int i) {
        if (D.BUG || OpUtils.DEBUG_ONEPLUS) {
            String str = TAG;
            Log.d(str, "mDialog.dismiss() reason: " + Events.DISMISS_REASONS[i] + " from: " + Debug.getCaller() + " mIsAnimatingDismiss=" + this.mIsAnimatingDismiss);
        }
        if (!this.mIsAnimatingDismiss || !isLandscape() || isStatusBarShowing()) {
            this.mIsAnimatingDismiss = true;
            this.mDialogView.animate().cancel();
            if (this.mShowing) {
                this.mShowing = false;
                Events.writeEvent(1, Integer.valueOf(i));
            }
            this.mDialogView.setTranslationX(0.0f);
            this.mDialogView.setAlpha(1.0f);
            ViewPropertyAnimator withEndAction = this.mDialogView.animate().alpha(0.0f).translationX((float) ((isLandscape() ? this.mDialogView.getWidth() : -this.mDialogView.getWidth()) / 2)).setDuration(250).setInterpolator(new SystemUIInterpolators$LogAccelerateInterpolator()).withEndAction(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$a7-wxVi216tvuQimiidzElZQq3M
                @Override // java.lang.Runnable
                public final void run() {
                    VolumeDialogImpl.this.lambda$dismissH$18$VolumeDialogImpl();
                }
            });
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(8);
            H h = this.mHandler;
            h.sendMessageDelayed(h.obtainMessage(8), 1000);
            withEndAction.start();
            checkODICaptionsTooltip(true);
            synchronized (this.mSafetyWarningLock) {
                if (this.mSafetyWarning != null) {
                    if (D.BUG) {
                        Log.d(TAG, "SafetyWarning dismissed");
                    }
                    this.mSafetyWarning.dismiss();
                }
            }
        }
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$dismissH$18 */
    private /* synthetic */ void lambda$dismissH$18$VolumeDialogImpl() {
        this.mHandler.postDelayed(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$IE-RbQpvR2pa3pWKthxPITNgmkU
            @Override // java.lang.Runnable
            public final void run() {
                VolumeDialogImpl.this.lambda$dismissH$17$VolumeDialogImpl();
            }
        }, 50);
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$dismissH$17 */
    private /* synthetic */ void lambda$dismissH$17$VolumeDialogImpl() {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i(TAG, "dismissH withEndAction");
        }
        setExpandFeautureDismissState();
        this.mDialog.dismiss();
        this.mHandler.removeMessages(8);
        this.mController.notifyVisible(false);
        tryToRemoveCaptionsTooltip();
        this.mIsAnimatingDismiss = false;
    }

    private boolean showActiveStreamOnly() {
        return this.mContext.getPackageManager().hasSystemFeature("android.software.leanback") || this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.television");
    }

    private boolean shouldBeVisibleH(OpVolumeDialogImpl.VolumeRow volumeRow, OpVolumeDialogImpl.VolumeRow volumeRow2) {
        if (volumeRow.stream == volumeRow2.stream) {
            return true;
        }
        if (this.mShowActiveStreamOnly) {
            return false;
        }
        int i = volumeRow.stream;
        if (i == 10) {
            return this.mShowA11yStream;
        }
        if (volumeRow2.stream == 10 && i == this.mPrevActiveStream) {
            return true;
        }
        if (!volumeRow.defaultStream) {
            return false;
        }
        int i2 = volumeRow2.stream;
        return i2 == 2 || i2 == 4 || i2 == 0 || i2 == 10 || this.mDynamic.get(i2);
    }

    private void updateRowsH(OpVolumeDialogImpl.VolumeRow volumeRow) {
        boolean z;
        boolean z2;
        boolean z3;
        if (D.BUG) {
            Log.d(TAG, "updateRowsH");
        }
        if (!this.mShowing) {
            trimObsoleteH();
        }
        if (OpUtils.DEBUG_ONEPLUS) {
            String str = TAG;
            Log.i(str, "updateRowsH, mOpForceExpandState:" + this.mOpForceExpandState + ", mOpLastforceExpandState:" + this.mOpLastforceExpandState + ", mNeedPlayExpandAnim:" + this.mNeedPlayExpandAnim + ", mFirstTimeInitDialog:" + this.mFirstTimeInitDialog + ", activeRow:" + volumeRow.stream);
        }
        int dimensionPixelSize = OpUtils.getDimensionPixelSize(this.mContext.getResources(), C0005R$dimen.op_volume_dialog_panel_width, 1080);
        boolean z4 = this.mOpForceExpandState;
        long j = 30;
        int i = 2;
        if (z4) {
            for (final OpVolumeDialogImpl.VolumeRow volumeRow2 : this.mRows) {
                int i2 = volumeRow2.stream;
                if (i2 == i || i2 == 4 || i2 == 3 || (i2 == 0 && ProductUtils.isUsvMode())) {
                    if (!this.mNeedPlayExpandAnim || this.mFirstTimeInitDialog) {
                        Util.setVisOrGone(volumeRow2.view, true);
                    } else {
                        int i3 = volumeRow2.stream;
                        if (i3 == i) {
                            ((FrameLayout) volumeRow2.view.findViewById(C0008R$id.volume_row_slider_frame)).setOnTouchListener(new View.OnTouchListener() { // from class: com.android.systemui.volume.VolumeDialogImpl.4
                                @Override // android.view.View.OnTouchListener
                                public boolean onTouch(View view, MotionEvent motionEvent) {
                                    if (motionEvent.getActionMasked() == 0) {
                                        volumeRow2.viewTouchDownY = motionEvent.getY();
                                    } else if (motionEvent.getActionMasked() == 1) {
                                        volumeRow2.viewTouchUpY = motionEvent.getY();
                                        OpVolumeDialogImpl.VolumeRow volumeRow3 = volumeRow2;
                                        if (volumeRow3.viewTouchUpY - volumeRow3.viewTouchDownY < 0.0f) {
                                            int i4 = 2;
                                            if (!(VolumeDialogImpl.this.mState.ringerModeInternal == 0 || VolumeDialogImpl.this.mState.ringerModeInternal == 1)) {
                                                i4 = -1;
                                            }
                                            if (i4 != -1) {
                                                ((OpThreekeyVolumeGuideController) Dependency.get(OpThreekeyVolumeGuideController.class)).isNeedToShowGuideUi(i4, true);
                                            }
                                        }
                                    }
                                    if (OpUtils.DEBUG_ONEPLUS) {
                                        Log.i(VolumeDialogImpl.TAG, "sliderFrameLayout, onTouch, event.getActionMasked():" + motionEvent.getActionMasked() + ", event:" + motionEvent.getX() + " / " + motionEvent.getY() + ", row.viewTouchDownY:" + volumeRow2.viewTouchDownY + ", row.viewTouchUpY:" + volumeRow2.viewTouchUpY);
                                    }
                                    return true;
                                }
                            });
                            volumeRow2.view.setAlpha(0.0f);
                            Util.setVisOrGone(volumeRow2.view, true);
                            if (ProductUtils.isUsvMode()) {
                                volumeRow2.view.setTranslationX((float) ((-dimensionPixelSize) / 4));
                            } else {
                                volumeRow2.view.setTranslationX((float) ((-dimensionPixelSize) / 3));
                            }
                            volumeRow2.view.animate().alpha(1.0f).translationX(0.0f).setDuration(275).setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f)).withEndAction($$Lambda$VolumeDialogImpl$rNQvPZ4v1mumin0xPzfWijGPmaI.INSTANCE).setStartDelay(j);
                        } else if (i3 == 4) {
                            volumeRow2.view.setAlpha(0.0f);
                            Util.setVisOrGone(volumeRow2.view, true);
                            if (ProductUtils.isUsvMode()) {
                                volumeRow2.view.setTranslationX((float) ((-dimensionPixelSize) / 4));
                            } else {
                                volumeRow2.view.setTranslationX((float) ((-dimensionPixelSize) / 3));
                            }
                            volumeRow2.view.animate().alpha(1.0f).translationX(0.0f).setDuration(275).setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f)).withEndAction($$Lambda$VolumeDialogImpl$m26Kv58I4TUt4m4TU2mNQQsVCVc.INSTANCE).setStartDelay(60);
                        } else if (i3 != 0 || !ProductUtils.isUsvMode()) {
                            Util.setVisOrGone(volumeRow2.view, true);
                        } else {
                            volumeRow2.view.setAlpha(0.0f);
                            Util.setVisOrGone(volumeRow2.view, true);
                            volumeRow2.view.setTranslationX((float) ((-dimensionPixelSize) / 4));
                            volumeRow2.view.animate().alpha(1.0f).translationX(0.0f).setDuration(275).setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f)).withEndAction($$Lambda$VolumeDialogImpl$CGNjjawdA5MPOVXXrPSL0CwlAlM.INSTANCE).setStartDelay(90);
                        }
                    }
                    if (volumeRow2.view.isShown()) {
                        updateVolumeRowTintH(volumeRow2, true, true);
                    }
                }
                j = 30;
                i = 2;
            }
            if (this.mNeedPlayExpandAnim) {
                opExpandAnim(true);
                if (this.mIsExpandAnimDone) {
                    if (OpUtils.DEBUG_ONEPLUS) {
                        String str2 = TAG;
                        Log.i(str2, "mAnimVol.start, " + this.mOpForceExpandState);
                    }
                    setOpOutputChooserGravityNeedBeforeAnimStart(true);
                    setOpOutputChooserVisible(true);
                    this.mAnimVol.start();
                }
            }
            this.mNeedPlayExpandAnim = false;
        } else if (!z4) {
            if (this.mNeedPlayExpandAnim) {
                for (OpVolumeDialogImpl.VolumeRow volumeRow3 : this.mRows) {
                    int i4 = volumeRow3.stream;
                    if (i4 == 2) {
                        volumeRow3.view.animate().alpha(0.0f).translationX((float) ((-dimensionPixelSize) / 5)).setDuration(175).setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f)).withEndAction(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$MpNJ24ayoduzmAFeDFV4eMtPu04
                            @Override // java.lang.Runnable
                            public final void run() {
                                VolumeDialogImpl.lambda$updateRowsH$22(OpVolumeDialogImpl.VolumeRow.this);
                            }
                        }).setStartDelay(60);
                    } else if (i4 == 4) {
                        volumeRow3.view.animate().alpha(0.0f).translationX((float) ((-dimensionPixelSize) / 5)).setDuration(175).setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f)).withEndAction(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$uTzBYZgj9yTHnMEMjuHMHE4EwAI
                            @Override // java.lang.Runnable
                            public final void run() {
                                VolumeDialogImpl.lambda$updateRowsH$23(OpVolumeDialogImpl.VolumeRow.this);
                            }
                        }).setStartDelay(30);
                    } else if (i4 == 0 && ProductUtils.isUsvMode()) {
                        volumeRow3.view.animate().alpha(0.0f).translationX((float) ((-dimensionPixelSize) / 5)).setDuration(175).setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f)).withEndAction(new Runnable() { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$5x76B-GMRRmIBDZfWPK8_p6CN4k
                            @Override // java.lang.Runnable
                            public final void run() {
                                VolumeDialogImpl.lambda$updateRowsH$24(OpVolumeDialogImpl.VolumeRow.this);
                            }
                        }).setStartDelay(30);
                    }
                }
                if (this.mNeedPlayExpandAnim) {
                    z3 = false;
                    opExpandAnim(false);
                    if (this.mIsExpandAnimDone) {
                        this.mAnimVol.start();
                    }
                } else {
                    z3 = false;
                }
                this.mNeedPlayExpandAnim = z3;
            } else {
                Iterator<OpVolumeDialogImpl.VolumeRow> it = this.mRows.iterator();
                while (it.hasNext()) {
                    OpVolumeDialogImpl.VolumeRow next = it.next();
                    if (volumeRow == null) {
                        z2 = next.stream == 3;
                        z = z2;
                    } else {
                        z = next == volumeRow;
                        z2 = shouldBeVisibleH(next, volumeRow);
                    }
                    Util.setVisOrGone(next.view, z2);
                    if (next.stream != 3 && z) {
                        for (OpVolumeDialogImpl.VolumeRow volumeRow4 : this.mRows) {
                            if (volumeRow4.stream == 3) {
                                Util.setVisOrGone(volumeRow4.view, false);
                            }
                        }
                    }
                    if (next.view.isShown()) {
                        updateVolumeRowTintH(next, true);
                    }
                }
            }
        }
        initSettingsH();
    }

    static /* synthetic */ void lambda$updateRowsH$22(OpVolumeDialogImpl.VolumeRow volumeRow) {
        volumeRow.view.setAlpha(1.0f);
        volumeRow.view.setTranslationX(0.0f);
        Util.setVisOrGone(volumeRow.view, false);
    }

    static /* synthetic */ void lambda$updateRowsH$23(OpVolumeDialogImpl.VolumeRow volumeRow) {
        volumeRow.view.setAlpha(1.0f);
        volumeRow.view.setTranslationX(0.0f);
        Util.setVisOrGone(volumeRow.view, false);
    }

    static /* synthetic */ void lambda$updateRowsH$24(OpVolumeDialogImpl.VolumeRow volumeRow) {
        volumeRow.view.setAlpha(1.0f);
        volumeRow.view.setTranslationX(0.0f);
        Util.setVisOrGone(volumeRow.view, false);
    }

    private void opExpandAnim(boolean z) {
        final int i;
        final int i2;
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i(TAG, "init mAnimVol init");
        }
        this.mAnimVol = ValueAnimator.ofFloat(0.0f, 1.0f);
        final ViewGroup viewGroup = (ViewGroup) this.mDialog.findViewById(C0008R$id.volume_dialog_container);
        final ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
        if (z) {
            i = this.mOpBeforeExpandWidth;
            i2 = this.mOpafterExpandWidth;
        } else {
            i = this.mOpafterExpandWidth;
            i2 = this.mOpBeforeExpandWidth;
        }
        this.mAnimVol.setDuration(275L);
        this.mAnimVol.setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.4f, 1.0f));
        this.mAnimVol.addListener(new AnimatorListenerAdapter() { // from class: com.android.systemui.volume.VolumeDialogImpl.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (OpUtils.DEBUG_ONEPLUS) {
                    Log.i(VolumeDialogImpl.TAG, "opExpandAnim onAnimationStart");
                }
                ((OpVolumeDialogImpl) VolumeDialogImpl.this).mIsExpandAnimDone = false;
                ViewGroup.LayoutParams layoutParams2 = layoutParams;
                layoutParams2.width = i;
                viewGroup.setLayoutParams(layoutParams2);
                viewGroup.requestLayout();
                if (!((OpVolumeDialogImpl) VolumeDialogImpl.this).mOpForceExpandState) {
                    VolumeDialogImpl.this.setOpOutputChooserVisible(false);
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (OpUtils.DEBUG_ONEPLUS) {
                    String str = VolumeDialogImpl.TAG;
                    Log.i(str, "mAnimVol onAnimationEnd, isRunning:" + VolumeDialogImpl.this.mAnimVol.isRunning());
                }
                ViewGroup.LayoutParams layoutParams2 = layoutParams;
                layoutParams2.width = i2;
                viewGroup.setLayoutParams(layoutParams2);
                viewGroup.requestLayout();
                if (!((OpVolumeDialogImpl) VolumeDialogImpl.this).mOpForceExpandState) {
                    VolumeDialogImpl.this.setOpOutputChooserGravityNeedBeforeAnimStart(false);
                }
                if (!VolumeDialogImpl.this.isLandscape()) {
                    VolumeDialogImpl.this.setDialogWidth(-2);
                }
                ((OpVolumeDialogImpl) VolumeDialogImpl.this).mIsExpandAnimDone = true;
                VolumeDialogImpl volumeDialogImpl = VolumeDialogImpl.this;
                volumeDialogImpl.updateRowsH(volumeDialogImpl.getActiveRow());
                if (OpUtils.DEBUG_ONEPLUS) {
                    String str2 = VolumeDialogImpl.TAG;
                    Log.i(str2, "mAnimVol onAnimationEnd 2 mIsExpandAnimDone" + ((OpVolumeDialogImpl) VolumeDialogImpl.this).mIsExpandAnimDone + ",  width:" + VolumeDialogImpl.this.mDialog.getWindow().getAttributes().width);
                }
            }
        });
        this.mAnimVol.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this) { // from class: com.android.systemui.volume.VolumeDialogImpl.6
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                int i3 = i2;
                int i4 = i;
                float f = (((float) (i3 - i4)) * floatValue) + ((float) i4);
                if (OpUtils.DEBUG_ONEPLUS) {
                    String str = VolumeDialogImpl.TAG;
                    Log.i(str, "onAnimationUpdate, t = " + floatValue + ", scaleT:" + f + ", lp.width:" + layoutParams.width);
                }
                if (Math.abs(f - ((float) layoutParams.width)) >= 10.0f) {
                    ViewGroup.LayoutParams layoutParams2 = layoutParams;
                    layoutParams2.width = (int) f;
                    viewGroup.setLayoutParams(layoutParams2);
                    viewGroup.requestLayout();
                }
            }
        });
    }

    public void updateRingerH() {
        VolumeDialogController.State state;
        VolumeDialogController.StreamState streamState;
        if (this.mRinger != null && (state = this.mState) != null && (streamState = state.states.get(2)) != null) {
            VolumeDialogController.State state2 = this.mState;
            int i = state2.zenMode;
            boolean z = false;
            boolean z2 = i == 3 || i == 2 || (i == 1 && state2.disallowRinger);
            enableRingerViewsH(!z2);
            int i2 = this.mState.ringerModeInternal;
            if (i2 == 0) {
                this.mRingerIcon.setImageResource(C0006R$drawable.ic_volume_ringer_mute);
                this.mRingerIcon.setTag(2);
                addAccessibilityDescription(this.mRingerIcon, 0, this.mContext.getString(C0015R$string.volume_ringer_hint_unmute));
            } else if (i2 != 1) {
                if ((this.mAutomute && streamState.level == 0) || streamState.muted) {
                    z = true;
                }
                if (z2 || !z) {
                    this.mRingerIcon.setImageResource(C0006R$drawable.ic_volume_ringer);
                    if (this.mController.hasVibrator()) {
                        addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(C0015R$string.volume_ringer_hint_vibrate));
                    } else {
                        addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(C0015R$string.volume_ringer_hint_mute));
                    }
                    this.mRingerIcon.setTag(1);
                    return;
                }
                this.mRingerIcon.setImageResource(C0006R$drawable.ic_volume_ringer_mute);
                addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(C0015R$string.volume_ringer_hint_unmute));
                this.mRingerIcon.setTag(2);
            } else {
                this.mRingerIcon.setImageResource(C0006R$drawable.ic_volume_ringer_vibrate);
                addAccessibilityDescription(this.mRingerIcon, 1, this.mContext.getString(C0015R$string.volume_ringer_hint_mute));
                this.mRingerIcon.setTag(3);
            }
        }
    }

    private void addAccessibilityDescription(View view, int i, final String str) {
        int i2;
        if (i == 0) {
            i2 = C0015R$string.volume_ringer_status_silent;
        } else if (i != 1) {
            i2 = C0015R$string.volume_ringer_status_normal;
        } else {
            i2 = C0015R$string.volume_ringer_status_vibrate;
        }
        view.setContentDescription(this.mContext.getString(i2));
        view.setAccessibilityDelegate(new View.AccessibilityDelegate(this) { // from class: com.android.systemui.volume.VolumeDialogImpl.7
            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View view2, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view2, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, str));
            }
        });
    }

    private void enableRingerViewsH(boolean z) {
        ImageButton imageButton = this.mRingerIcon;
        if (imageButton != null) {
            imageButton.setEnabled(z);
        }
        FrameLayout frameLayout = this.mZenIcon;
        if (frameLayout != null) {
            frameLayout.setVisibility(z ? 8 : 0);
        }
    }

    private void trimObsoleteH() {
        if (D.BUG) {
            Log.d(TAG, "trimObsoleteH");
        }
        for (int size = this.mRows.size() - 1; size >= 0; size--) {
            OpVolumeDialogImpl.VolumeRow volumeRow = this.mRows.get(size);
            VolumeDialogController.StreamState streamState = volumeRow.ss;
            if (streamState != null && streamState.dynamic && !this.mDynamic.get(volumeRow.stream)) {
                this.mRows.remove(size);
                this.mDialogRowsView.removeView(volumeRow.view);
                this.mConfigurableTexts.remove(volumeRow.header);
            }
        }
    }

    public void onStateChangedH(VolumeDialogController.State state) {
        int i;
        this.mIsOpZenModeOn = ((OpSceneModeObserver) Dependency.get(OpSceneModeObserver.class)).isInBrickMode();
        if (Build.DEBUG_ONEPLUS) {
            String str = TAG;
            Log.d(str, "onStateChangedH() state: " + state.toString() + ", mIsOpZenModeOn:" + this.mIsOpZenModeOn);
        }
        if (D.BUG) {
            String str2 = TAG;
            Log.d(str2, "onStateChangedH() state: " + state.toString());
        }
        VolumeDialogController.State state2 = this.mState;
        if (!(state2 == null || state == null || state2.ringerModeInternal == (i = state.ringerModeInternal) || i != 1)) {
            this.mController.vibrate(VibrationEffect.get(5));
        }
        this.mState = state;
        this.mDynamic.clear();
        for (int i2 = 0; i2 < state.states.size(); i2++) {
            int keyAt = state.states.keyAt(i2);
            if (state.states.valueAt(i2).dynamic) {
                String str3 = TAG;
                Log.i(str3, " onStateChangedH stream:" + keyAt);
                this.mDynamic.put(keyAt, true);
                if (findRow(keyAt) == null) {
                    addRow(keyAt, C0006R$drawable.ic_volume_remote, C0006R$drawable.ic_volume_remote_mute, true, false, true);
                    updateDialogLayout();
                }
            }
        }
        int i3 = this.mActiveStream;
        int i4 = state.activeStream;
        if (i3 != i4) {
            this.mPrevActiveStream = i3;
            this.mActiveStream = i4;
            updateRowsH(getActiveRow());
            if (this.mShowing) {
                rescheduleTimeoutH();
            }
        }
        for (OpVolumeDialogImpl.VolumeRow volumeRow : this.mRows) {
            updateVolumeRowH(volumeRow);
        }
        updateRingerH();
        updateODICaptionsH(this.mIsCaptionComponentEnabled, this.mFromTooltip);
        this.mWindow.setTitle(composeWindowTitle());
    }

    public CharSequence composeWindowTitle() {
        return this.mContext.getString(C0015R$string.volume_dialog_title, getStreamLabelH(getActiveRow().ss));
    }

    /* JADX DEBUG: Multi-variable search result rejected for r9v14, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r9v15, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r9v16, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r9v17, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r9v18, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    private void updateVolumeRowH(OpVolumeDialogImpl.VolumeRow volumeRow) {
        VolumeDialogController.StreamState streamState;
        boolean z;
        int i;
        boolean z2;
        int i2;
        boolean z3;
        int i3;
        int i4;
        int i5;
        if (D.BUG) {
            Log.i(TAG, "updateVolumeRowH s=" + volumeRow.stream);
        }
        VolumeDialogController.State state = this.mState;
        if (state != null && (streamState = state.states.get(volumeRow.stream)) != null) {
            volumeRow.ss = streamState;
            int i6 = streamState.level;
            int i7 = 2;
            if (i6 <= 0 || volumeRow.stream == 2) {
                int i8 = streamState.level;
                if (i8 > 1 && volumeRow.stream == 2) {
                    volumeRow.lastAudibleLevel = i8;
                    if (Build.DEBUG_ONEPLUS) {
                        Log.i(TAG, "updateVolumeRowH, lastAudibleLevel = ss.level:" + streamState.level);
                    }
                }
            } else {
                volumeRow.lastAudibleLevel = i6;
            }
            if (streamState.level == volumeRow.requestedLevel) {
                volumeRow.requestedLevel = -1;
            }
            boolean z4 = volumeRow.stream == 10;
            boolean z5 = volumeRow.stream == 2;
            boolean z6 = volumeRow.stream == 1;
            boolean z7 = volumeRow.stream == 4;
            boolean z8 = volumeRow.stream == 3;
            int i9 = volumeRow.stream;
            boolean z9 = z5 && this.mState.ringerModeInternal == 1;
            boolean z10 = z5 && this.mState.ringerModeInternal == 0;
            boolean z11 = this.mState.zenMode == 1;
            boolean z12 = this.mState.zenMode == 3;
            boolean z13 = this.mState.zenMode == 2;
            if (!z12 ? !z13 ? !z11 || ((!z7 || !this.mState.disallowAlarms) && ((!z8 || !this.mState.disallowMedia) && ((!z5 || !this.mState.disallowRinger) && (!z6 || !this.mState.disallowSystem)))) : !z5 && !z6 && !z7 && !z8 : !z5 && !z6) {
                z = false;
            } else {
                z = true;
            }
            int i10 = streamState.levelMax * 100;
            if (i10 != volumeRow.slider.getMax()) {
                volumeRow.slider.setMax(i10);
            }
            int i11 = streamState.levelMin * 100;
            if (!(i11 == volumeRow.slider.getMin() || volumeRow.stream == 0)) {
                volumeRow.slider.setMin(i11);
            }
            if (Build.DEBUG_ONEPLUS) {
                Log.d(TAG, "updateVolumeRowH s=" + volumeRow.stream + " min:" + streamState.levelMin + ", ss.muted:" + streamState.muted);
            }
            Util.setText(volumeRow.header, getStreamLabelH(streamState));
            volumeRow.slider.setContentDescription(volumeRow.header.getText());
            this.mConfigurableTexts.add(volumeRow.header, streamState.name);
            boolean z14 = (this.mAutomute || streamState.muteSupported) && !z;
            volumeRow.icon.setEnabled(z14);
            volumeRow.icon.setAlpha(z14 ? 1.0f : 0.5f);
            if (z9) {
                i = C0006R$drawable.ic_volume_ringer_vibrate;
            } else if (z10 || z) {
                i = volumeRow.iconMuteRes;
            } else if (streamState.routedToBluetooth) {
                i = isStreamMuted(streamState) ? C0006R$drawable.ic_volume_media_bt_mute : C0006R$drawable.ic_volume_media_bt;
            } else {
                i = isStreamMuted(streamState) ? volumeRow.iconMuteRes : volumeRow.iconRes;
            }
            volumeRow.icon.setImageResource(i);
            if (i == C0006R$drawable.ic_volume_ringer_vibrate) {
                i7 = 3;
            } else if (!(i == C0006R$drawable.ic_volume_media_bt_mute || i == volumeRow.iconMuteRes)) {
                i7 = (i == C0006R$drawable.ic_volume_media_bt || i == volumeRow.iconRes) ? 1 : 0;
            }
            volumeRow.iconState = i7;
            if (z14) {
                if (z5) {
                    if (z9) {
                        volumeRow.icon.setContentDescription(this.mContext.getString(C0015R$string.volume_stream_content_description_unmute, getStreamLabelH(streamState)));
                    } else if (this.mController.hasVibrator()) {
                        ImageButton imageButton = volumeRow.icon;
                        Context context = this.mContext;
                        if (this.mShowA11yStream) {
                            i5 = C0015R$string.volume_stream_content_description_vibrate_a11y;
                        } else {
                            i5 = C0015R$string.volume_stream_content_description_vibrate;
                        }
                        imageButton.setContentDescription(context.getString(i5, getStreamLabelH(streamState)));
                    } else {
                        ImageButton imageButton2 = volumeRow.icon;
                        Context context2 = this.mContext;
                        if (this.mShowA11yStream) {
                            i4 = C0015R$string.volume_stream_content_description_mute_a11y;
                        } else {
                            i4 = C0015R$string.volume_stream_content_description_mute;
                        }
                        imageButton2.setContentDescription(context2.getString(i4, getStreamLabelH(streamState)));
                    }
                } else if (z4) {
                    volumeRow.icon.setContentDescription(getStreamLabelH(streamState));
                } else if (streamState.muted || (this.mAutomute && streamState.level == 0)) {
                    z2 = 0;
                    volumeRow.icon.setContentDescription(this.mContext.getString(C0015R$string.volume_stream_content_description_unmute, getStreamLabelH(streamState)));
                } else {
                    ImageButton imageButton3 = volumeRow.icon;
                    Context context3 = this.mContext;
                    if (this.mShowA11yStream) {
                        i3 = C0015R$string.volume_stream_content_description_mute_a11y;
                    } else {
                        i3 = C0015R$string.volume_stream_content_description_mute;
                    }
                    z2 = 0;
                    imageButton3.setContentDescription(context3.getString(i3, getStreamLabelH(streamState)));
                }
                z2 = 0;
            } else {
                z2 = 0;
                volumeRow.icon.setContentDescription(getStreamLabelH(streamState));
            }
            if (z) {
                volumeRow.tracking = z2;
            }
            boolean z15 = !z;
            if (!volumeRow.ss.muted || z5 || z) {
                i2 = volumeRow.ss.level;
            } else {
                i2 = z2;
            }
            if (!z5 || (!z9 && !z10)) {
                z3 = z15;
            } else {
                int i12 = z2 ? 1 : 0;
                Object[] objArr = z2 ? 1 : 0;
                Object[] objArr2 = z2 ? 1 : 0;
                Object[] objArr3 = z2 ? 1 : 0;
                Object[] objArr4 = z2 ? 1 : 0;
                i2 = i12;
                z3 = i2;
            }
            updateVolumeRowSliderH(volumeRow, z3, i2);
        }
    }

    private boolean isStreamMuted(VolumeDialogController.StreamState streamState) {
        return (this.mAutomute && streamState.level == 0) || streamState.muted;
    }

    private void updateVolumeRowTintH(OpVolumeDialogImpl.VolumeRow volumeRow, boolean z) {
        updateVolumeRowTintH(volumeRow, z, false);
    }

    private void updateVolumeRowTintH(OpVolumeDialogImpl.VolumeRow volumeRow, boolean z, boolean z2) {
        if (z) {
            volumeRow.slider.requestFocus();
        }
        ColorStateList valueOf = ColorStateList.valueOf(this.mAccentColor);
        if (z) {
            Color.alpha(valueOf.getDefaultColor());
        } else {
            getAlphaAttr(16844115);
        }
        if (valueOf != volumeRow.cachedTint || this.mThemeColorMode != volumeRow.themeColorMode || z2) {
            volumeRow.slider.setProgressTintList(valueOf);
            volumeRow.slider.setThumbTintList(valueOf);
            volumeRow.cachedTint = valueOf;
            volumeRow.slider.setProgressDrawable(this.mContext.getResources().getDrawable(this.mThemeColorSeekbarBackgroundDrawable));
            volumeRow.themeColorMode = this.mThemeColorMode;
            volumeRow.icon.setColorFilter(this.mThemeColorIcon);
        }
    }

    private void updateVolumeRowSliderH(OpVolumeDialogImpl.VolumeRow volumeRow, boolean z, int i) {
        volumeRow.slider.setEnabled(z);
        updateVolumeRowTintH(volumeRow, true);
        if (!volumeRow.tracking) {
            int progress = volumeRow.slider.getProgress();
            int impliedLevel = getImpliedLevel(volumeRow.slider, progress);
            boolean z2 = volumeRow.view.getVisibility() == 0;
            boolean z3 = SystemClock.uptimeMillis() - volumeRow.userAttempt < 1000;
            this.mHandler.removeMessages(3, volumeRow);
            if (this.mShowing && z2 && z3) {
                if (D.BUG) {
                    Log.d(TAG, "inGracePeriod");
                }
                if (Build.DEBUG_ONEPLUS) {
                    String str = TAG;
                    Log.d(str, "updateVolumeRowSliderH s=" + volumeRow.stream + " inGracePeriod");
                }
                H h = this.mHandler;
                h.sendMessageAtTime(h.obtainMessage(3, volumeRow), volumeRow.userAttempt + 1000);
            } else if (i != impliedLevel || !this.mShowing || !z2) {
                int i2 = i * 100;
                if (Build.DEBUG_ONEPLUS) {
                    String str2 = TAG;
                    Log.d(str2, "updateVolumeRowSliderH s=" + volumeRow.stream + " progress: " + progress + " newProgress: " + i2 + " enable: " + z);
                }
                if (progress == i2) {
                    return;
                }
                if (!this.mShowing || !z2) {
                    ObjectAnimator objectAnimator = volumeRow.anim;
                    if (objectAnimator != null) {
                        objectAnimator.cancel();
                    }
                    volumeRow.slider.setProgress(i2, true);
                    return;
                }
                ObjectAnimator objectAnimator2 = volumeRow.anim;
                if (objectAnimator2 == null || !objectAnimator2.isRunning() || volumeRow.animTargetProgress != i2) {
                    ObjectAnimator objectAnimator3 = volumeRow.anim;
                    if (objectAnimator3 == null) {
                        ObjectAnimator ofInt = ObjectAnimator.ofInt(volumeRow.slider, "progress", progress, i2);
                        volumeRow.anim = ofInt;
                        ofInt.setInterpolator(new DecelerateInterpolator());
                    } else {
                        objectAnimator3.cancel();
                        volumeRow.anim.setIntValues(progress, i2);
                    }
                    volumeRow.animTargetProgress = i2;
                    volumeRow.anim.setDuration(80L);
                    volumeRow.anim.start();
                } else if (Build.DEBUG_ONEPLUS) {
                    Log.d(TAG, "updateVolumeRowSliderH already animating to the target progress");
                }
            } else if (Build.DEBUG_ONEPLUS) {
                String str3 = TAG;
                Log.d(str3, "updateVolumeRowSliderH s=" + volumeRow.stream + " vlevel: " + i + " level: " + impliedLevel);
            }
        } else if (Build.DEBUG_ONEPLUS) {
            String str4 = TAG;
            Log.d(str4, "updateVolumeRowSliderH s=" + volumeRow.stream + " is tracking");
        }
    }

    private void recheckH(OpVolumeDialogImpl.VolumeRow volumeRow) {
        if (volumeRow == null) {
            if (D.BUG) {
                Log.d(TAG, "recheckH ALL");
            }
            trimObsoleteH();
            for (OpVolumeDialogImpl.VolumeRow volumeRow2 : this.mRows) {
                updateVolumeRowH(volumeRow2);
            }
            return;
        }
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "recheckH " + volumeRow.stream);
        }
        updateVolumeRowH(volumeRow);
    }

    private void setStreamImportantH(int i, boolean z) {
        for (OpVolumeDialogImpl.VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == i) {
                String str = TAG;
                Log.i(str, " setStreamImportantH stream:" + volumeRow.stream + " important:" + z);
                volumeRow.important = z;
                return;
            }
        }
    }

    private void showSafetyWarningH(int i) {
        String str = TAG;
        Log.i(str, "showSafetyWarningH flags:" + i + " mShowing:" + this.mShowing);
        if ((i & 1025) != 0 || this.mShowing) {
            synchronized (this.mSafetyWarningLock) {
                if (this.mSafetyWarning == null) {
                    AnonymousClass8 r0 = new SafetyWarningDialog(this.mContext, this.mController.getAudioManager()) { // from class: com.android.systemui.volume.VolumeDialogImpl.8
                        @Override // com.android.systemui.volume.SafetyWarningDialog
                        public void cleanUp() {
                            synchronized (VolumeDialogImpl.this.mSafetyWarningLock) {
                                VolumeDialogImpl.this.mSafetyWarning = null;
                            }
                            VolumeDialogImpl.this.recheckH(null);
                        }
                    };
                    this.mSafetyWarning = r0;
                    r0.show();
                } else {
                    return;
                }
            }
            recheckH(null);
        }
        rescheduleTimeoutH();
    }

    private String getStreamLabelH(VolumeDialogController.StreamState streamState) {
        if (streamState == null) {
            return "";
        }
        String str = streamState.remoteLabel;
        if (str != null) {
            return str;
        }
        try {
            return this.mContext.getResources().getString(streamState.name);
        } catch (Resources.NotFoundException unused) {
            String str2 = TAG;
            Slog.e(str2, "Can't find translation for stream " + streamState);
            return "";
        }
    }

    private Runnable getSinglePressFor(ImageButton imageButton) {
        return new Runnable(imageButton) { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$LT0IPBZdP1jDIMwVCAclfWfxQwU
            public final /* synthetic */ ImageButton f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                VolumeDialogImpl.this.lambda$getSinglePressFor$25$VolumeDialogImpl(this.f$1);
            }
        };
    }

    /* access modifiers changed from: public */
    /* renamed from: lambda$getSinglePressFor$25 */
    private /* synthetic */ void lambda$getSinglePressFor$25$VolumeDialogImpl(ImageButton imageButton) {
        if (imageButton != null) {
            imageButton.setPressed(true);
            imageButton.postOnAnimationDelayed(getSingleUnpressFor(imageButton), 200);
        }
    }

    private Runnable getSingleUnpressFor(ImageButton imageButton) {
        return new Runnable(imageButton) { // from class: com.android.systemui.volume.-$$Lambda$VolumeDialogImpl$J07ySb9s3Y3zCMbX45St4b7bh8A
            public final /* synthetic */ ImageButton f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                VolumeDialogImpl.lambda$getSingleUnpressFor$26(this.f$0);
            }
        };
    }

    static /* synthetic */ void lambda$getSingleUnpressFor$26(ImageButton imageButton) {
        if (imageButton != null) {
            imageButton.setPressed(false);
        }
    }

    public final class H extends Handler {
        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public H() {
            super(Looper.getMainLooper());
            VolumeDialogImpl.this = r1;
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    VolumeDialogImpl.this.showH(message.arg1);
                    return;
                case 2:
                    VolumeDialogImpl.this.dismissH(message.arg1);
                    return;
                case 3:
                    VolumeDialogImpl.this.recheckH((OpVolumeDialogImpl.VolumeRow) message.obj);
                    return;
                case 4:
                    VolumeDialogImpl.this.recheckH(null);
                    return;
                case 5:
                    VolumeDialogImpl.this.setStreamImportantH(message.arg1, message.arg2 != 0);
                    return;
                case 6:
                    VolumeDialogImpl.this.rescheduleTimeoutH();
                    return;
                case 7:
                    VolumeDialogImpl volumeDialogImpl = VolumeDialogImpl.this;
                    volumeDialogImpl.onStateChangedH(volumeDialogImpl.mState);
                    return;
                case 8:
                    VolumeDialogImpl.this.setDismissDialog();
                    return;
                default:
                    return;
            }
        }
    }

    public final class CustomDialog extends Dialog implements DialogInterface {
        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public CustomDialog(Context context) {
            super(context, C0016R$style.qs_theme);
            VolumeDialogImpl.this = r1;
        }

        @Override // android.app.Dialog, android.view.Window.Callback
        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            VolumeDialogImpl.this.rescheduleTimeoutH();
            return super.dispatchTouchEvent(motionEvent);
        }

        @Override // android.app.Dialog
        public void onStart() {
            super.setCanceledOnTouchOutside(true);
            super.onStart();
        }

        @Override // android.app.Dialog
        public void onStop() {
            super.onStop();
            VolumeDialogImpl.this.mHandler.sendEmptyMessage(4);
        }

        @Override // android.app.Dialog
        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (OpUtils.DEBUG_ONEPLUS) {
                String str = VolumeDialogImpl.TAG;
                Log.i(str, "CustomDialog onTouchEvent, event.getAction():" + motionEvent.getAction() + ", mShowing:" + VolumeDialogImpl.this.mShowing);
            }
            if (!VolumeDialogImpl.this.mShowing) {
                return false;
            }
            VolumeDialogImpl.this.dismissH(1);
            return true;
        }
    }

    public final class VolumeSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private int mRecordNearlyProgress;
        private int mRecordOverZeroLimitTimes;
        private int mRecordProgressWhenStartTouch;
        private final OpVolumeDialogImpl.VolumeRow mRow;

        private VolumeSeekBarChangeListener(OpVolumeDialogImpl.VolumeRow volumeRow) {
            VolumeDialogImpl.this = r1;
            this.mRecordProgressWhenStartTouch = -1;
            this.mRecordNearlyProgress = -1;
            this.mRecordOverZeroLimitTimes = 0;
            this.mRow = volumeRow;
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            int i2;
            if (this.mRow.ss != null) {
                if (D.BUG) {
                    Log.d(VolumeDialogImpl.TAG, AudioSystem.streamToString(this.mRow.stream) + " onProgressChanged " + i + " fromUser=" + z);
                }
                int i3 = 100;
                if (Build.DEBUG_ONEPLUS) {
                    Log.d(VolumeDialogImpl.TAG, "mRow.stream:" + this.mRow.stream + " / progress:" + i + " / mRingerModeMinLevel * 100:100 / mRow.ss.levelMin:" + this.mRow.ss.levelMin + " / mRecordProgressWhenStartTouch:" + this.mRecordProgressWhenStartTouch + " / mRecordNearlyProgress:" + this.mRecordNearlyProgress + " / mRecordOverZeroLimitTimes:" + this.mRecordOverZeroLimitTimes);
                }
                if (z) {
                    int i4 = this.mRow.ss.levelMin;
                    if (i4 > 0 && i < (i2 = i4 * 100)) {
                        seekBar.setProgress(i2);
                        i = i2;
                    }
                    if (this.mRow.stream != 2 || i >= 100) {
                        i3 = i;
                    } else {
                        if (i == 0 && this.mRecordNearlyProgress == 100) {
                            this.mRecordOverZeroLimitTimes++;
                        }
                        if (this.mRecordOverZeroLimitTimes > 2 && this.mRecordProgressWhenStartTouch == 100) {
                            ((OpThreekeyVolumeGuideController) Dependency.get(OpThreekeyVolumeGuideController.class)).isNeedToShowGuideUi(3, true);
                        }
                        seekBar.setProgress(100);
                    }
                    this.mRecordNearlyProgress = i3;
                    int impliedLevel = VolumeDialogImpl.getImpliedLevel(seekBar, i3);
                    VolumeDialogController.StreamState streamState = this.mRow.ss;
                    if (streamState.level != impliedLevel || (streamState.muted && impliedLevel > 0)) {
                        this.mRow.userAttempt = SystemClock.uptimeMillis();
                        if (this.mRow.requestedLevel != impliedLevel) {
                            VolumeDialogImpl.this.mController.setActiveStream(this.mRow.stream);
                            VolumeDialogImpl.this.mController.setStreamVolume(this.mRow.stream, impliedLevel);
                            OpVolumeDialogImpl.VolumeRow volumeRow = this.mRow;
                            volumeRow.requestedLevel = impliedLevel;
                            Events.writeEvent(9, Integer.valueOf(volumeRow.stream), Integer.valueOf(impliedLevel));
                        }
                    }
                }
            }
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (D.BUG) {
                String str = VolumeDialogImpl.TAG;
                Log.d(str, "onStartTrackingTouch " + this.mRow.stream + ", seekBar.getProgress:" + seekBar.getProgress());
            }
            VolumeDialogImpl.this.mController.setActiveStream(this.mRow.stream);
            this.mRecordProgressWhenStartTouch = seekBar.getProgress();
            this.mRecordNearlyProgress = seekBar.getProgress();
            this.mRecordOverZeroLimitTimes = 0;
            this.mRow.tracking = true;
        }

        @Override // android.widget.SeekBar.OnSeekBarChangeListener
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (D.BUG) {
                String str = VolumeDialogImpl.TAG;
                Log.d(str, "onStopTrackingTouch " + this.mRow.stream);
            }
            OpVolumeDialogImpl.VolumeRow volumeRow = this.mRow;
            volumeRow.tracking = false;
            volumeRow.userAttempt = SystemClock.uptimeMillis();
            int impliedLevel = VolumeDialogImpl.getImpliedLevel(seekBar, seekBar.getProgress());
            Events.writeEvent(16, Integer.valueOf(this.mRow.stream), Integer.valueOf(impliedLevel));
            if (this.mRow.ss.level != impliedLevel) {
                VolumeDialogImpl.this.mHandler.sendMessageDelayed(VolumeDialogImpl.this.mHandler.obtainMessage(3, this.mRow), 1000);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class Accessibility extends View.AccessibilityDelegate {
        private Accessibility() {
            VolumeDialogImpl.this = r1;
        }

        public void init() {
            VolumeDialogImpl.this.mDialogView.setAccessibilityDelegate(this);
        }

        @Override // android.view.View.AccessibilityDelegate
        public boolean dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.getText().add(VolumeDialogImpl.this.composeWindowTitle());
            return true;
        }

        @Override // android.view.View.AccessibilityDelegate
        public boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent) {
            VolumeDialogImpl.this.rescheduleTimeoutH();
            return super.onRequestSendAccessibilityEvent(viewGroup, view, accessibilityEvent);
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() {
        if (OpUtils.DEBUG_ONEPLUS) {
            Log.i(TAG, "onOverlayChanged be trigger in vol");
        }
        applyColorTheme(true);
    }
}
