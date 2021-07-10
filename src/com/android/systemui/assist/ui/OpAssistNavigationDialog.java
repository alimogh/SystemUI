package com.android.systemui.assist.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0014R$raw;
import com.android.systemui.C0015R$string;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.oneplus.util.OpUtils;
public class OpAssistNavigationDialog implements ConfigurationController.ConfigurationListener {
    private static LayoutInflater layoutInflater;
    private static LottieAnimationView mAnimationViewAssist;
    private static LottieAnimationView mAnimationViewPow;
    private static TextView mContentTextView;
    private static Context mContext;
    private static int mCurrentPage = 0;
    private static boolean mIsShowing = false;
    private static Button mLeftButton;
    private static View mMainView;
    private static OpAssistNavigationDialog mOpAssistNavigationDialog;
    private static Button mRightButton;
    private static WindowManager.LayoutParams mWindowLayoutParams;
    private static WindowManager mWindowManager;

    static /* synthetic */ int access$308() {
        int i = mCurrentPage;
        mCurrentPage = i + 1;
        return i;
    }

    static {
        boolean z = Build.DEBUG_ONEPLUS;
    }

    private OpAssistNavigationDialog(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService("window");
    }

    public static OpAssistNavigationDialog getInstance(Context context) {
        OpAssistNavigationDialog opAssistNavigationDialog;
        synchronized (OpAssistNavigationDialog.class) {
            if (mOpAssistNavigationDialog == null) {
                Log.v("OpAssistNavDialog", "OpAssistNavigationDialog getInstance");
                mOpAssistNavigationDialog = new OpAssistNavigationDialog(context);
                mCurrentPage = 0;
            }
            opAssistNavigationDialog = mOpAssistNavigationDialog;
        }
        return opAssistNavigationDialog;
    }

    public static void showDialog() {
        if (!mIsShowing) {
            if (mCurrentPage == 2) {
                mCurrentPage = 0;
            }
            inflateView();
            updateDialog(mCurrentPage);
            mWindowManager.addView(mMainView, mWindowLayoutParams);
            mIsShowing = true;
        }
    }

    /* access modifiers changed from: private */
    public static void updateDialog(int i) {
        if (i == 0) {
            mContentTextView.setText(C0015R$string.long_press_power_navigation_power_content);
            mLeftButton.setVisibility(8);
            mRightButton.setText(C0015R$string.oneplus_threekey_navigation_next);
            mAnimationViewAssist.setVisibility(8);
            mAnimationViewPow.setVisibility(0);
            if (mAnimationViewAssist.isAnimating()) {
                mAnimationViewAssist.cancelAnimation();
            }
            mAnimationViewPow.playAnimation();
        } else if (i == 1) {
            mContentTextView.setText(C0015R$string.long_press_power_navigation_assist_content);
            if (OpUtils.isPrimaryOwnerMode(mContext)) {
                mLeftButton.setVisibility(0);
                mLeftButton.setSelected(true);
            } else {
                mLeftButton.setVisibility(8);
            }
            mRightButton.setText(C0015R$string.nav_bar_guide_positive);
            mAnimationViewAssist.setVisibility(0);
            mAnimationViewPow.setVisibility(8);
            if (mAnimationViewPow.isAnimating()) {
                mAnimationViewPow.cancelAnimation();
            }
            mAnimationViewAssist.playAnimation();
        }
    }

    public static void dismissDialog() {
        WindowManager windowManager;
        View view = mMainView;
        if (view != null && (windowManager = mWindowManager) != null && mIsShowing) {
            mIsShowing = false;
            windowManager.removeViewImmediate(view);
        }
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    private static void inflateView() {
        layoutInflater = (LayoutInflater) mContext.getSystemService("layout_inflater");
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 0, 0, 2008, 16777504, -3);
        mWindowLayoutParams = layoutParams;
        layoutParams.type = 2008;
        layoutParams.format = -3;
        layoutParams.screenOrientation = 1;
        layoutParams.setTitle("OpAssistNavigationDialog");
        mWindowLayoutParams.layoutInDisplayCutoutMode = 3;
        View inflate = layoutInflater.inflate(C0011R$layout.op_assist_navigation_dialog, (ViewGroup) null);
        mMainView = inflate;
        inflate.setSystemUiVisibility(5122);
        mMainView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() { // from class: com.android.systemui.assist.ui.OpAssistNavigationDialog.1
            @Override // android.view.View.OnSystemUiVisibilityChangeListener
            public void onSystemUiVisibilityChange(int i) {
                if (i == 0 || i == 2) {
                    if (OpAssistNavigationDialog.mWindowLayoutParams != null) {
                        OpAssistNavigationDialog.mWindowManager.updateViewLayout(OpAssistNavigationDialog.mMainView, OpAssistNavigationDialog.mWindowLayoutParams);
                    }
                    if (OpAssistNavigationDialog.mMainView != null) {
                        OpAssistNavigationDialog.mMainView.setSystemUiVisibility(5122);
                        OpAssistNavigationDialog.mMainView.requestLayout();
                    }
                }
            }
        });
        View view = mMainView;
        if (view != null) {
            view.findViewById(C0008R$id.animation_view_container).getLayoutParams().height = OpUtils.convertDpToFixedPx(mContext.getResources().getDimension(C0005R$dimen.op_assist_navigation_anim_height));
            mContentTextView = (TextView) mMainView.findViewById(C0008R$id.content);
            mLeftButton = (Button) mMainView.findViewById(C0008R$id.left_button);
            mRightButton = (Button) mMainView.findViewById(C0008R$id.right_button);
            LottieAnimationView lottieAnimationView = (LottieAnimationView) mMainView.findViewById(C0008R$id.animation_view_pow);
            mAnimationViewPow = lottieAnimationView;
            lottieAnimationView.setAnimation(C0014R$raw.pow_nav);
            LottieAnimationView lottieAnimationView2 = (LottieAnimationView) mMainView.findViewById(C0008R$id.animation_view_assist);
            mAnimationViewAssist = lottieAnimationView2;
            lottieAnimationView2.setAnimation(C0014R$raw.assist_nav);
            mRightButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.assist.ui.OpAssistNavigationDialog.2
                @Override // android.view.View.OnClickListener
                public void onClick(View view2) {
                    OpAssistNavigationDialog.access$308();
                    if (OpAssistNavigationDialog.mCurrentPage < 2) {
                        OpAssistNavigationDialog.updateDialog(OpAssistNavigationDialog.mCurrentPage);
                    } else {
                        OpAssistNavigationDialog.dismissDialog();
                    }
                }
            });
            mLeftButton.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.assist.ui.OpAssistNavigationDialog.3
                @Override // android.view.View.OnClickListener
                public void onClick(View view2) {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.SubSettings"));
                    intent.putExtra(":settings:show_fragment", "com.oneplus.settings.OPLongPressPowerButtonSettings");
                    intent.addFlags(268435456);
                    OpAssistNavigationDialog.mContext.startActivity(intent);
                    int unused = OpAssistNavigationDialog.mCurrentPage = 2;
                    OpAssistNavigationDialog.dismissDialog();
                }
            });
        }
    }
}
