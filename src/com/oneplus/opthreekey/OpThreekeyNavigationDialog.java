package com.oneplus.opthreekey;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0009R$integer;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.opzenmode.OpZenModeController;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
public class OpThreekeyNavigationDialog implements OpZenModeController.Callback, ConfigurationController.ConfigurationListener {
    private static boolean DEBUG = false;
    private static String TAG = "OpThreekeyNavigationDialog";
    private static OpThreekeyNavigationDialog mOpThreekeyNavigationDialog;
    private LayoutInflater layoutInflater;
    private int mAccentColor = 0;
    private final View.OnClickListener mClickThreeKeyNavigationGotItTextButton;
    private final View.OnClickListener mClickThreeKeyNavigationNextTextButton;
    private Context mContext;
    private final H mHandler = new H();
    private LinearLayout[] mInnerVirtualThreekeyView;
    private View mMainView;
    KeyguardUpdateMonitorCallback mMonitorCallback;
    private OpZenModeController mOpZenModeController;
    OrientationEventListener mOrientationListener;
    private int mOrientationType = 0;
    private int mParentStatus = -1;
    private View mSecondView;
    private boolean mShowing = false;
    private int mShowingType = 0;
    private int mThemeBgColor = 0;
    private int mThemeColorMode = 0;
    private int mThemeIconColor = 0;
    private int mThemeTextColor = 0;
    private TextView mThreeKeyNavigationNextGotItButton;
    private TextView mThreeKeyNavigationNextTextButton;
    private ImageView mThreeKeyNavigationTriangle;
    ImageView[] mThreeKeyRowIcon;
    TextView[] mThreeKeyRowText;
    private LinearLayout mThreekeyNavigationFullBlueDialog;
    private int mThreekeyType;
    KeyguardUpdateMonitor mUpdateMonitor;
    private LinearLayout mViewContainer;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private WindowManager mWindowManager;

    static {
        DEBUG = Build.DEBUG_ONEPLUS;
    }

    public static OpThreekeyNavigationDialog getInstance(Context context) {
        synchronized (OpThreekeyNavigationDialog.class) {
            if (mOpThreekeyNavigationDialog == null) {
                Log.v(TAG, "OpThreekeyNavigationDialog getInstance");
                OpThreekeyNavigationDialog opThreekeyNavigationDialog = new OpThreekeyNavigationDialog(context);
                mOpThreekeyNavigationDialog = opThreekeyNavigationDialog;
                opThreekeyNavigationDialog.init();
            }
        }
        return mOpThreekeyNavigationDialog;
    }

    public OpThreekeyNavigationDialog(Context context) {
        UserHandle.myUserId();
        this.mClickThreeKeyNavigationNextTextButton = new View.OnClickListener() { // from class: com.oneplus.opthreekey.OpThreekeyNavigationDialog.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Log.i(OpThreekeyNavigationDialog.TAG, "onClickNextButton");
                OpThreekeyNavigationDialog.this.change();
            }
        };
        this.mClickThreeKeyNavigationGotItTextButton = new View.OnClickListener() { // from class: com.oneplus.opthreekey.OpThreekeyNavigationDialog.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                OpThreekeyNavigationDialog opThreekeyNavigationDialog = OpThreekeyNavigationDialog.this;
                opThreekeyNavigationDialog.dismiss(opThreekeyNavigationDialog.mViewContainer);
                OpThreekeyNavigationDialog.this.mShowingType = 0;
                OpThreekeyNavigationDialog.this.setFinished("op_threekey_navigation_completed", 1);
            }
        };
        this.mMonitorCallback = new KeyguardUpdateMonitorCallback(this) { // from class: com.oneplus.opthreekey.OpThreekeyNavigationDialog.4
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitching(int i) {
                String str = OpThreekeyNavigationDialog.TAG;
                Log.i(str, "onUserSwitching / userId:" + i);
                synchronized (this) {
                }
            }
        };
        this.mContext = context;
        this.mOpZenModeController = (OpZenModeController) Dependency.get(OpZenModeController.class);
        this.mOrientationListener = new OrientationEventListener(this.mContext, 3) { // from class: com.oneplus.opthreekey.OpThreekeyNavigationDialog.1
            @Override // android.view.OrientationEventListener
            public void onOrientationChanged(int i) {
                int rotation;
                Display realDisplay = DisplayManagerGlobal.getInstance().getRealDisplay(0);
                if (realDisplay != null && (rotation = realDisplay.getRotation()) != OpThreekeyNavigationDialog.this.mOrientationType) {
                    String str = OpThreekeyNavigationDialog.TAG;
                    Log.v(str, "OrientType to " + rotation);
                    OpThreekeyNavigationDialog.this.mOrientationType = rotation;
                    if (OpThreekeyNavigationDialog.this.mShowingType != 0) {
                        OpThreekeyNavigationDialog.this.mHandler.obtainMessage(7).sendToTarget();
                    }
                }
            }
        };
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
    }

    private void init() {
        this.mThreekeyType = this.mContext.getResources().getInteger(C0009R$integer.oneplus_config_threekey_type);
        String str = TAG;
        Log.v(str, "init / mThreekeyType:" + this.mThreekeyType);
        if (this.mThreekeyType != 0) {
            inflateView();
            int i = this.mContext.getResources().getConfiguration().densityDpi;
            this.mOpZenModeController.addCallback(this);
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
            KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
            this.mUpdateMonitor = keyguardUpdateMonitor;
            keyguardUpdateMonitor.registerCallback(this.mMonitorCallback);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void inflateView() {
        if (DEBUG) {
            String str = TAG;
            Log.i(str, "inflateView mOrientationType == " + this.mOrientationType);
        }
        LayoutInflater layoutInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.layoutInflater = layoutInflater;
        this.mMainView = layoutInflater.inflate(C0011R$layout.op_threekey_navigation_dialog_first, (ViewGroup) null);
        this.mSecondView = this.layoutInflater.inflate(C0011R$layout.op_threekey_navigation_dialog_second, (ViewGroup) null);
        this.mViewContainer = new LinearLayout(this.mContext);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, 0, 0, 1003, 16777512, -3);
        this.mWindowLayoutParams = layoutParams;
        layoutParams.type = 2038;
        layoutParams.format = -3;
        layoutParams.setTitle("OpThreekeyNavigationDialog");
        this.mWindowLayoutParams.layoutInDisplayCutoutMode = 3;
    }

    private void initDialog() {
        if (DEBUG) {
            Log.i(TAG, "initDialog / mMainView:" + this.mMainView + " / mSecondView:" + this.mSecondView);
        }
        View view = this.mMainView;
        if (view != null && this.mSecondView != null) {
            this.mThreekeyNavigationFullBlueDialog = (LinearLayout) view.findViewById(C0008R$id.threekey_navigation_full_blue_dialog);
            TextView textView = (TextView) this.mMainView.findViewById(C0008R$id.threekey_navigation_next_text);
            this.mThreeKeyNavigationNextTextButton = textView;
            textView.setOnClickListener(this.mClickThreeKeyNavigationNextTextButton);
            this.mThreeKeyNavigationTriangle = (ImageView) this.mMainView.findViewById(C0008R$id.threekey_navigation_triangle_icon);
            TextView textView2 = (TextView) this.mSecondView.findViewById(C0008R$id.threekey_navigation_gotit_text);
            this.mThreeKeyNavigationNextGotItButton = textView2;
            textView2.setOnClickListener(this.mClickThreeKeyNavigationGotItTextButton);
            Resources resources = this.mContext.getResources();
            LinearLayout[] linearLayoutArr = new LinearLayout[3];
            this.mInnerVirtualThreekeyView = linearLayoutArr;
            linearLayoutArr[0] = (LinearLayout) this.mMainView.findViewById(C0008R$id.threekey_vurtual_silent);
            this.mInnerVirtualThreekeyView[1] = (LinearLayout) this.mMainView.findViewById(C0008R$id.threekey_vurtual_vibrate);
            this.mInnerVirtualThreekeyView[2] = (LinearLayout) this.mMainView.findViewById(C0008R$id.threekey_vurtual_ring);
            this.mInnerVirtualThreekeyView[0].findViewById(C0008R$id.threekey_layout).setBackgroundDrawable(resources.getDrawable(C0006R$drawable.dialog_threekey_up_bg));
            this.mInnerVirtualThreekeyView[1].findViewById(C0008R$id.threekey_layout).setBackgroundDrawable(resources.getDrawable(C0006R$drawable.dialog_threekey_middle_bg));
            this.mInnerVirtualThreekeyView[2].findViewById(C0008R$id.threekey_layout).setBackgroundDrawable(resources.getDrawable(C0006R$drawable.dialog_threekey_down_bg));
            this.mThreeKeyRowIcon = new ImageView[3];
            this.mThreeKeyRowText = new TextView[3];
            for (int i = 0; i < 3; i++) {
                this.mThreeKeyRowIcon[i] = (ImageView) this.mInnerVirtualThreekeyView[i].findViewById(C0008R$id.threekey_icon);
                this.mThreeKeyRowText[i] = (TextView) this.mInnerVirtualThreekeyView[i].findViewById(C0008R$id.threekey_text);
            }
            this.mThreeKeyRowIcon[0].setImageResource(C0006R$drawable.op_ic_silence);
            this.mThreeKeyRowIcon[1].setImageResource(C0006R$drawable.op_ic_vibrate);
            this.mThreeKeyRowIcon[2].setImageResource(C0006R$drawable.op_ic_ring);
            this.mThreeKeyRowText[0].setText(resources.getString(C0015R$string.volume_footer_slient));
            this.mThreeKeyRowText[1].setText(resources.getString(C0015R$string.volume_vibrate));
            this.mThreeKeyRowText[2].setText(resources.getString(C0015R$string.volume_footer_ring));
            updateTheme(true);
            int i2 = this.mOrientationType;
            if (i2 == 1) {
                for (int i3 = 0; i3 < 3; i3++) {
                    this.mInnerVirtualThreekeyView[i3].setVisibility(8);
                }
                this.mInnerVirtualThreekeyView[this.mParentStatus - 1].setVisibility(0);
                this.mInnerVirtualThreekeyView[this.mParentStatus - 1].findViewById(C0008R$id.threekey_layout).setBackgroundDrawable(resources.getDrawable(C0006R$drawable.dialog_threekey_middle_bg));
            } else if (i2 != 2) {
                if (i2 != 3) {
                    int dimensionPixelSize = resources.getDimensionPixelSize(C0005R$dimen.three_key_up_dialog_position);
                    int dimensionPixelSize2 = resources.getDimensionPixelSize(C0005R$dimen.three_key_middle_dialog_position);
                    int dimensionPixelSize3 = resources.getDimensionPixelSize(C0005R$dimen.three_key_down_dialog_position);
                    int dimensionPixelSize4 = resources.getDimensionPixelSize(C0005R$dimen.op_threekey_dialog_padding);
                    if (dimensionPixelSize == dimensionPixelSize2 && dimensionPixelSize2 == dimensionPixelSize3) {
                        this.mInnerVirtualThreekeyView[(this.mParentStatus - 1) % 3].setVisibility(0);
                        this.mInnerVirtualThreekeyView[((this.mParentStatus - 1) + 1) % 3].setVisibility(8);
                        this.mInnerVirtualThreekeyView[((this.mParentStatus - 1) + 2) % 3].setVisibility(8);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
                        layoutParams.setMargins(0, resources.getDimensionPixelSize(C0005R$dimen.op_threekey_dialog_triangle_short) - dimensionPixelSize4, 0, 0);
                        this.mThreeKeyNavigationTriangle.setLayoutParams(layoutParams);
                    } else {
                        setAlpha(this.mInnerVirtualThreekeyView[(this.mParentStatus - 1) % 3].findViewById(C0008R$id.threekey_layout), 1000);
                        this.mInnerVirtualThreekeyView[((this.mParentStatus - 1) + 1) % 3].findViewById(C0008R$id.threekey_layout).setAlpha(0.3f);
                        this.mInnerVirtualThreekeyView[((this.mParentStatus - 1) + 2) % 3].findViewById(C0008R$id.threekey_layout).setAlpha(0.3f);
                        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-2, -2);
                        layoutParams2.setMargins(0, resources.getDimensionPixelSize(C0005R$dimen.op_threekey_dialog_triangle_long) - (dimensionPixelSize4 * 2), 0, 0);
                        this.mThreeKeyNavigationTriangle.setLayoutParams(layoutParams2);
                    }
                    int dimensionPixelSize5 = resources.getDimensionPixelSize(17105481);
                    LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mThreekeyNavigationFullBlueDialog.getLayoutParams();
                    layoutParams3.topMargin = dimensionPixelSize5;
                    this.mThreekeyNavigationFullBlueDialog.setLayoutParams(layoutParams3);
                    FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(-2, -2);
                    int i4 = -dimensionPixelSize4;
                    layoutParams4.setMargins(i4, (dimensionPixelSize - dimensionPixelSize4) + dimensionPixelSize5, 0, 0);
                    this.mInnerVirtualThreekeyView[0].setLayoutParams(layoutParams4);
                    FrameLayout.LayoutParams layoutParams5 = new FrameLayout.LayoutParams(-2, -2);
                    layoutParams5.setMargins(i4, (dimensionPixelSize2 - dimensionPixelSize4) + dimensionPixelSize5, 0, 0);
                    this.mInnerVirtualThreekeyView[1].setLayoutParams(layoutParams5);
                    FrameLayout.LayoutParams layoutParams6 = new FrameLayout.LayoutParams(-2, -2);
                    layoutParams6.setMargins(i4, (dimensionPixelSize3 - dimensionPixelSize4) + dimensionPixelSize5, 0, 0);
                    this.mInnerVirtualThreekeyView[2].setLayoutParams(layoutParams6);
                    return;
                }
                for (int i5 = 0; i5 < 3; i5++) {
                    this.mInnerVirtualThreekeyView[i5].setVisibility(8);
                }
                this.mInnerVirtualThreekeyView[this.mParentStatus - 1].setVisibility(0);
                this.mInnerVirtualThreekeyView[this.mParentStatus - 1].findViewById(C0008R$id.threekey_layout).setBackgroundDrawable(resources.getDrawable(C0006R$drawable.dialog_threekey_middle_bg));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void show() {
        int rotation;
        String str = TAG;
        Log.i(str, "show mShowingType:" + this.mShowingType);
        Display defaultDisplay = this.mWindowManager.getDefaultDisplay();
        if (!(defaultDisplay == null || (rotation = defaultDisplay.getRotation()) == this.mOrientationType)) {
            this.mOrientationType = rotation;
        }
        int i = this.mShowingType;
        if (i == 1 || i == 0) {
            int i2 = C0011R$layout.op_threekey_navigation_dialog_first;
            int i3 = this.mOrientationType;
            if (i3 == 1) {
                i2 = C0011R$layout.op_threekey_navigation_dialog_first_left_land;
            } else if (i3 == 3) {
                i2 = C0011R$layout.op_threekey_navigation_dialog_first_right_land;
            }
            View inflate = this.layoutInflater.inflate(i2, (ViewGroup) null);
            this.mMainView = inflate;
            this.mShowing = true;
            this.mHandler.obtainMessage(1, inflate).sendToTarget();
        } else if (i == 2) {
            View inflate2 = this.layoutInflater.inflate(C0011R$layout.op_threekey_navigation_dialog_second, (ViewGroup) null);
            this.mSecondView = inflate2;
            this.mShowing = true;
            this.mHandler.obtainMessage(1, inflate2).sendToTarget();
        }
        registerOrientationListener(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismiss(View view) {
        this.mHandler.obtainMessage(2, view).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void change() {
        this.mHandler.obtainMessage(4).sendToTarget();
    }

    private void registerOrientationListener(boolean z) {
        String str = TAG;
        Log.v(str, "registerOrientationListener:" + z);
        OrientationEventListener orientationEventListener = this.mOrientationListener;
        if (orientationEventListener != null) {
            if (!orientationEventListener.canDetectOrientation() || !z) {
                Log.v(TAG, "Cannot detect orientation");
                this.mOrientationListener.disable();
                return;
            }
            Log.v(TAG, "Can detect orientation");
            this.mOrientationListener.enable();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0061, code lost:
        if (r0 != false) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0064, code lost:
        if (r0 != false) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0067, code lost:
        if (r0 != false) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x006c, code lost:
        if (r0 != false) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0070, code lost:
        r4 = 83;
     */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:37:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateThreekeyLayout() {
        /*
            r8 = this;
            android.content.Context r0 = r8.mContext
            if (r0 == 0) goto L_0x0086
            android.view.WindowManager$LayoutParams r1 = r8.mWindowLayoutParams
            if (r1 != 0) goto L_0x000a
            goto L_0x0086
        L_0x000a:
            android.content.res.Resources r0 = r0.getResources()
            if (r0 != 0) goto L_0x0011
            return
        L_0x0011:
            boolean r0 = r8.mShowing
            if (r0 != 0) goto L_0x0016
            return
        L_0x0016:
            android.view.WindowManager$LayoutParams r0 = r8.mWindowLayoutParams
            int r0 = r0.gravity
            boolean r0 = com.oneplus.opthreekey.OpThreekeyNavigationDialog.DEBUG
            if (r0 == 0) goto L_0x0036
            java.lang.String r0 = com.oneplus.opthreekey.OpThreekeyNavigationDialog.TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "updateThreekeyLayout() / mShowingType == "
            r1.append(r2)
            int r2 = r8.mShowingType
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
        L_0x0036:
            int r0 = r8.mShowingType
            r1 = 2
            if (r0 != r1) goto L_0x0049
            r0 = 49
            android.view.WindowManager$LayoutParams r1 = r8.mWindowLayoutParams
            r1.gravity = r0
            android.view.WindowManager r0 = r8.mWindowManager
            android.widget.LinearLayout r8 = r8.mViewContainer
            r0.updateViewLayout(r8, r1)
            return
        L_0x0049:
            r0 = 0
            int r2 = r8.mThreekeyType
            r3 = 1
            if (r2 != 0) goto L_0x0050
            r0 = r3
        L_0x0050:
            int r2 = r8.mOrientationType
            r4 = 53
            r5 = 85
            r6 = 51
            r7 = 83
            if (r2 == r3) goto L_0x006c
            if (r2 == r1) goto L_0x0067
            r1 = 3
            if (r2 == r1) goto L_0x0064
            if (r0 == 0) goto L_0x006e
            goto L_0x0071
        L_0x0064:
            if (r0 == 0) goto L_0x0071
            goto L_0x006a
        L_0x0067:
            if (r0 == 0) goto L_0x006a
            goto L_0x0070
        L_0x006a:
            r4 = r5
            goto L_0x0071
        L_0x006c:
            if (r0 == 0) goto L_0x0070
        L_0x006e:
            r4 = r6
            goto L_0x0071
        L_0x0070:
            r4 = r7
        L_0x0071:
            android.view.WindowManager$LayoutParams r0 = r8.mWindowLayoutParams
            r0.gravity = r4
            android.widget.LinearLayout r0 = r8.mViewContainer
            android.view.ViewParent r0 = r0.getParent()
            if (r0 == 0) goto L_0x0086
            android.view.WindowManager r0 = r8.mWindowManager
            android.widget.LinearLayout r1 = r8.mViewContainer
            android.view.WindowManager$LayoutParams r8 = r8.mWindowLayoutParams
            r0.updateViewLayout(r1, r8)
        L_0x0086:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.opthreekey.OpThreekeyNavigationDialog.updateThreekeyLayout():void");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showH(View view) {
        String str = TAG;
        Log.d(str, "showH view.getParent():" + view.getParent() + ", type:" + this.mShowingType);
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(4);
        this.mHandler.removeMessages(5);
        if (view.getParent() == null) {
            this.mWindowManager.addView(this.mViewContainer, this.mWindowLayoutParams);
            this.mViewContainer.addView(view);
            checkShowPage();
        }
        updateThreekeyLayout();
        initDialog();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissH(View view) {
        LinearLayout linearLayout;
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(4);
        this.mHandler.removeMessages(5);
        if (view != null && view.getParent() != null && view == this.mViewContainer) {
            this.mWindowManager.removeView(view);
        } else if (view != null && view.getParent() != null && (linearLayout = this.mViewContainer) != null) {
            linearLayout.removeView(view);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeH() {
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(4);
        this.mHandler.removeMessages(5);
        if (this.mMainView.getParent() != null && this.mSecondView.getParent() == null) {
            this.mViewContainer.addView(this.mSecondView);
            this.mViewContainer.removeView(this.mMainView);
            checkShowPage();
            updateThreekeyLayout();
            initDialog();
        }
    }

    private int checkShowPage() {
        boolean z = this.mMainView.getParent() != null;
        boolean z2 = this.mSecondView.getParent() != null;
        if (z && z2) {
            Log.d(TAG, "mShowingType Invalid");
        } else if (z && !z2) {
            this.mShowingType = 1;
        } else if (!z && z2) {
            this.mShowingType = 2;
        } else if (!z && !z2) {
            this.mShowingType = 0;
        }
        String str = TAG;
        Log.d(str, "checkShowPage mShowingType:" + this.mShowingType);
        return this.mShowingType;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stateChange(int i) {
        if (DEBUG) {
            String str = TAG;
            Log.d(str, "stateChange: " + i + ", parent: " + this.mParentStatus);
        }
        if (i != this.mParentStatus) {
            this.mParentStatus = i;
            initDialog();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void rotateH() {
        if (DEBUG) {
            String str = TAG;
            Log.i(str, " rotateH /mOrientationType == " + this.mOrientationType);
        }
        if (this.mShowing) {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(4);
            this.mHandler.removeMessages(5);
            updateThreekeyLayout();
            ViewParent parent = this.mMainView.getParent();
            int i = this.mOrientationType;
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (parent != null) {
                            this.mViewContainer.removeView(this.mMainView);
                            View inflate = this.layoutInflater.inflate(C0011R$layout.op_threekey_navigation_dialog_first, (ViewGroup) null);
                            this.mMainView = inflate;
                            this.mViewContainer.addView(inflate);
                        }
                    } else if (this.mShowingType == 1 && parent != null) {
                        this.mViewContainer.removeView(this.mMainView);
                        View inflate2 = this.layoutInflater.inflate(C0011R$layout.op_threekey_navigation_dialog_first_right_land, (ViewGroup) null);
                        this.mMainView = inflate2;
                        this.mViewContainer.addView(inflate2);
                    }
                }
            } else if (this.mShowingType == 1 && parent != null) {
                this.mViewContainer.removeView(this.mMainView);
                View inflate3 = this.layoutInflater.inflate(C0011R$layout.op_threekey_navigation_dialog_first_left_land, (ViewGroup) null);
                this.mMainView = inflate3;
                this.mViewContainer.addView(inflate3);
            }
            initDialog();
        }
    }

    private void setAlpha(View view, int i) {
        H h = this.mHandler;
        h.sendMessageDelayed(h.obtainMessage(8, view), (long) i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAlphaH(View view) {
        if (!this.mHandler.hasMessages(8)) {
            view.setAlpha(1.0f);
        }
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    OpThreekeyNavigationDialog.this.showH((View) message.obj);
                    return;
                case 2:
                    OpThreekeyNavigationDialog.this.dismissH((View) message.obj);
                    return;
                case 3:
                    OpThreekeyNavigationDialog.this.stateChange(message.arg1);
                    return;
                case 4:
                    if (OpThreekeyNavigationDialog.this.mShowing) {
                        OpThreekeyNavigationDialog.this.changeH();
                        return;
                    }
                    return;
                case 5:
                    if (OpThreekeyNavigationDialog.this.mShowing) {
                        OpThreekeyNavigationDialog.this.rotateH();
                        return;
                    }
                    return;
                case 6:
                    OpThreekeyNavigationDialog.this.updateTheme(((Boolean) message.obj).booleanValue());
                    return;
                case 7:
                    OpThreekeyNavigationDialog opThreekeyNavigationDialog = OpThreekeyNavigationDialog.this;
                    opThreekeyNavigationDialog.dismissH(opThreekeyNavigationDialog.mMainView);
                    OpThreekeyNavigationDialog opThreekeyNavigationDialog2 = OpThreekeyNavigationDialog.this;
                    opThreekeyNavigationDialog2.dismissH(opThreekeyNavigationDialog2.mSecondView);
                    OpThreekeyNavigationDialog opThreekeyNavigationDialog3 = OpThreekeyNavigationDialog.this;
                    opThreekeyNavigationDialog3.dismissH(opThreekeyNavigationDialog3.mViewContainer);
                    OpThreekeyNavigationDialog.this.mShowing = false;
                    OpThreekeyNavigationDialog.this.inflateView();
                    if (OpThreekeyNavigationDialog.this.mShowingType != 0) {
                        OpThreekeyNavigationDialog.this.show();
                        return;
                    }
                    return;
                case 8:
                    OpThreekeyNavigationDialog.this.setAlphaH((View) message.obj);
                    return;
                default:
                    return;
            }
        }
    }

    @Override // com.oneplus.opzenmode.OpZenModeController.Callback
    public void onThreeKeyStatus(int i) {
        boolean z = true;
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "op_threekey_navigation_completed", 0, KeyguardUpdateMonitor.getCurrentUser()) != 1) {
            z = false;
        }
        Log.i(TAG, "onThreeKeyStatus() / completed:" + z + " / userId:" + KeyguardUpdateMonitor.getCurrentUser() + " / UserHandle.myUserId():" + UserHandle.myUserId() + ", threekey:" + i + ", mShowing:" + this.mShowing + ", mParentStatus:" + this.mParentStatus);
        if (!z && mOpThreekeyNavigationDialog != null && KeyguardUpdateMonitor.getCurrentUser() == 0) {
            this.mHandler.obtainMessage(3, i, 0).sendToTarget();
            if (this.mParentStatus != -1 && !this.mShowing) {
                show();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setFinished(String str, int i) {
        String str2 = TAG;
        Log.i(str2, "setFinished / UserHandle.myUserId():" + UserHandle.myUserId() + " mUpdateMonitor.getCurrentUser():" + KeyguardUpdateMonitor.getCurrentUser());
        Settings.System.putIntForUser(this.mContext.getContentResolver(), str, i, KeyguardUpdateMonitor.getCurrentUser());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTheme(boolean z) {
        if (this.mShowing) {
            int themeColor = OpUtils.getThemeColor(this.mContext);
            int color = ThemeColorUtils.getColor(100);
            boolean z2 = (this.mThemeColorMode == themeColor && this.mAccentColor == color) ? false : true;
            String str = TAG;
            Log.i(str, "updateTheme change:" + z2 + " force:" + z + " theme:" + themeColor + " accentColor:" + String.format("0x%08X", Integer.valueOf(color)) + " mThemeColorMode:" + this.mThemeColorMode + " mAccentColor:" + String.format("0x%08X", Integer.valueOf(this.mAccentColor)));
            if (z2 || z) {
                this.mThemeColorMode = themeColor;
                this.mAccentColor = color;
                applyTheme();
            }
        }
    }

    private void applyTheme() {
        Resources resources = this.mContext.getResources();
        if (this.mShowing) {
            if (this.mThemeColorMode != 1) {
                this.mThemeIconColor = this.mAccentColor;
                this.mThemeTextColor = resources.getColor(C0004R$color.oneplus_contorl_text_color_primary_light);
                this.mThemeBgColor = resources.getColor(C0004R$color.oneplus_contorl_bg_color_steppers_light);
            } else {
                this.mThemeIconColor = this.mAccentColor;
                this.mThemeTextColor = resources.getColor(C0004R$color.oneplus_contorl_text_color_primary_dark);
                this.mThemeBgColor = resources.getColor(C0004R$color.oneplus_contorl_bg_color_steppers_dark);
            }
            this.mInnerVirtualThreekeyView[0].findViewById(C0008R$id.threekey_layout).setBackgroundTintList(ColorStateList.valueOf(this.mThemeBgColor));
            this.mInnerVirtualThreekeyView[1].findViewById(C0008R$id.threekey_layout).setBackgroundTintList(ColorStateList.valueOf(this.mThemeBgColor));
            this.mInnerVirtualThreekeyView[2].findViewById(C0008R$id.threekey_layout).setBackgroundTintList(ColorStateList.valueOf(this.mThemeBgColor));
            this.mThreeKeyRowText[0].setTextColor(this.mThemeTextColor);
            this.mThreeKeyRowText[1].setTextColor(this.mThemeTextColor);
            this.mThreeKeyRowText[2].setTextColor(this.mThemeTextColor);
            this.mThreeKeyRowIcon[0].setColorFilter(this.mThemeIconColor);
            this.mThreeKeyRowIcon[1].setColorFilter(this.mThemeIconColor);
            this.mThreeKeyRowIcon[2].setColorFilter(this.mThemeIconColor);
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        if (configuration != null) {
            String str = TAG;
            Log.i(str, "onConfigChanged, mShowingType:" + this.mShowingType + " / newConfig.toString():" + configuration.toString());
        }
        this.mHandler.obtainMessage(7).sendToTarget();
    }
}
