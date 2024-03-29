package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.ReverseLinearLayout;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.oneplus.util.OpNavBarUtils;
import com.oneplus.util.OpUtils;
import java.io.PrintWriter;
import java.util.Objects;
public class NavigationBarInflaterView extends FrameLayout implements NavigationModeController.ModeChangedListener {
    private boolean mAlternativeOrder;
    @VisibleForTesting
    SparseArray<ButtonDispatcher> mButtonDispatchers;
    private String mCurrentLayout;
    protected FrameLayout mHorizontal;
    private boolean mIsCustomNavBar = false;
    private boolean mIsVertical;
    protected LayoutInflater mLandscapeInflater;
    private View mLastLandscape;
    private View mLastPortrait;
    protected LayoutInflater mLayoutInflater;
    private int mNavBarMode = 0;
    private OverviewProxyService mOverviewProxyService;
    private int mScreenResolution;
    protected FrameLayout mVertical;

    public NavigationBarInflaterView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        createInflaters();
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        setLayoutDirection(0);
        this.mIsCustomNavBar = OpNavBarUtils.isSupportCustomNavBar();
        this.mNavBarMode = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(this);
        this.mScreenResolution = OpUtils.mScreenResolution;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void createInflaters() {
        this.mLayoutInflater = LayoutInflater.from(((FrameLayout) this).mContext);
        Configuration configuration = new Configuration();
        configuration.setTo(((FrameLayout) this).mContext.getResources().getConfiguration());
        configuration.orientation = 2;
        this.mLandscapeInflater = LayoutInflater.from(((FrameLayout) this).mContext.createConfigurationContext(configuration));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        inflateChildren();
        clearViews();
        inflateLayout(getDefaultLayout());
    }

    private void inflateChildren() {
        int i;
        int i2;
        removeAllViews();
        this.mHorizontal = (FrameLayout) this.mLayoutInflater.inflate(C0011R$layout.navigation_layout, (ViewGroup) this, false);
        if (this.mIsCustomNavBar) {
            Resources resources = ((FrameLayout) this).mContext.getResources();
            if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
                i2 = C0005R$dimen.op_gesture_navigation_side_padding;
            } else {
                i2 = C0005R$dimen.op_rounded_corner_content_padding;
            }
            i = resources.getDimensionPixelSize(i2);
        } else {
            i = 0;
        }
        ((ViewGroup.MarginLayoutParams) this.mHorizontal.getLayoutParams()).setMarginsRelative(i, 0, i, 0);
        addView(this.mHorizontal);
        FrameLayout frameLayout = (FrameLayout) this.mLayoutInflater.inflate(C0011R$layout.navigation_layout_vertical, (ViewGroup) this, false);
        this.mVertical = frameLayout;
        ((ViewGroup.MarginLayoutParams) frameLayout.getLayoutParams()).setMarginsRelative(0, i, 0, i);
        addView(this.mVertical);
        updateAlternativeOrder();
    }

    /* access modifiers changed from: protected */
    public String getDefaultLayout() {
        int i;
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            i = C0015R$string.config_navBarLayoutHandle;
        } else {
            i = OpNavBarUtils.getNavBarLayout(getContext(), this.mOverviewProxyService.shouldShowSwipeUpUI());
        }
        return getContext().getString(i);
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mNavBarMode = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(this);
        onLikelyDefaultLayoutChange();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        ((NavigationModeController) Dependency.get(NavigationModeController.class)).removeListener(this);
        super.onDetachedFromWindow();
    }

    public void onLikelyDefaultLayoutChange() {
        String defaultLayout = getDefaultLayout();
        if (!Objects.equals(this.mCurrentLayout, defaultLayout)) {
            updateChildrenMargin();
            clearViews();
            inflateLayout(defaultLayout);
        }
    }

    public void setButtonDispatchers(SparseArray<ButtonDispatcher> sparseArray) {
        this.mButtonDispatchers = sparseArray;
        for (int i = 0; i < sparseArray.size(); i++) {
            initiallyFill(sparseArray.valueAt(i));
        }
    }

    /* access modifiers changed from: package-private */
    public void updateButtonDispatchersCurrentView() {
        if (this.mButtonDispatchers != null) {
            FrameLayout frameLayout = this.mIsVertical ? this.mVertical : this.mHorizontal;
            for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
                this.mButtonDispatchers.valueAt(i).setCurrentView(frameLayout);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setVertical(boolean z) {
        if (z != this.mIsVertical) {
            this.mIsVertical = z;
        }
    }

    /* access modifiers changed from: package-private */
    public void setAlternativeOrder(boolean z) {
        if (z != this.mAlternativeOrder) {
            this.mAlternativeOrder = z;
            updateAlternativeOrder();
        }
    }

    private void updateAlternativeOrder() {
        updateAlternativeOrder(this.mHorizontal.findViewById(C0008R$id.ends_group));
        updateAlternativeOrder(this.mHorizontal.findViewById(C0008R$id.center_group));
        updateAlternativeOrder(this.mVertical.findViewById(C0008R$id.ends_group));
        updateAlternativeOrder(this.mVertical.findViewById(C0008R$id.center_group));
    }

    private void updateAlternativeOrder(View view) {
        if (view instanceof ReverseLinearLayout) {
            ((ReverseLinearLayout) view).setAlternativeOrder(this.mAlternativeOrder);
        }
    }

    private void initiallyFill(ButtonDispatcher buttonDispatcher) {
        addAll(buttonDispatcher, (ViewGroup) this.mHorizontal.findViewById(C0008R$id.ends_group));
        addAll(buttonDispatcher, (ViewGroup) this.mHorizontal.findViewById(C0008R$id.center_group));
        addAll(buttonDispatcher, (ViewGroup) this.mVertical.findViewById(C0008R$id.ends_group));
        addAll(buttonDispatcher, (ViewGroup) this.mVertical.findViewById(C0008R$id.center_group));
    }

    private void addAll(ButtonDispatcher buttonDispatcher, ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i).getId() == buttonDispatcher.getId()) {
                buttonDispatcher.addView(viewGroup.getChildAt(i));
            }
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                addAll(buttonDispatcher, (ViewGroup) viewGroup.getChildAt(i));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void inflateLayout(String str) {
        this.mCurrentLayout = str;
        if (str == null) {
            str = getDefaultLayout();
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.d("NavBarInflater", "inflateLayout newLayout: " + str);
        }
        String[] split = str.split(";", 3);
        if (split.length != 3) {
            Log.d("NavBarInflater", "Invalid layout.");
            split = getDefaultLayout().split(";", 3);
        }
        String[] split2 = split[0].split(",");
        String[] split3 = split[1].split(",");
        String[] split4 = split[2].split(",");
        inflateButtons(split2, (ViewGroup) this.mHorizontal.findViewById(C0008R$id.ends_group), false, true);
        inflateButtons(split2, (ViewGroup) this.mVertical.findViewById(C0008R$id.ends_group), true, true);
        inflateButtons(split3, (ViewGroup) this.mHorizontal.findViewById(C0008R$id.center_group), false, false);
        inflateButtons(split3, (ViewGroup) this.mVertical.findViewById(C0008R$id.center_group), true, false);
        addGravitySpacer((LinearLayout) this.mHorizontal.findViewById(C0008R$id.ends_group));
        addGravitySpacer((LinearLayout) this.mVertical.findViewById(C0008R$id.ends_group));
        inflateButtons(split4, (ViewGroup) this.mHorizontal.findViewById(C0008R$id.ends_group), false, false);
        inflateButtons(split4, (ViewGroup) this.mVertical.findViewById(C0008R$id.ends_group), true, false);
        if (this.mIsCustomNavBar && !QuickStepContract.isGesturalMode(this.mNavBarMode) && (getParent() instanceof NavigationBarView)) {
            ((NavigationBarView) getParent()).refreshButtonColor();
        }
        updateButtonDispatchersCurrentView();
    }

    private void addGravitySpacer(LinearLayout linearLayout) {
        linearLayout.addView(new Space(((FrameLayout) this).mContext), new LinearLayout.LayoutParams(0, 0, 1.0f));
    }

    private void inflateButtons(String[] strArr, ViewGroup viewGroup, boolean z, boolean z2) {
        for (String str : strArr) {
            inflateButton(str, viewGroup, z, z2);
        }
    }

    /* access modifiers changed from: protected */
    public View inflateButton(String str, ViewGroup viewGroup, boolean z, boolean z2) {
        View createView = createView(str, viewGroup, z ? this.mLandscapeInflater : this.mLayoutInflater);
        if (createView == null) {
            return null;
        }
        View applySize = applySize(createView, str, z, z2);
        viewGroup.addView(applySize);
        addToDispatchers(applySize);
        View view = z ? this.mLastLandscape : this.mLastPortrait;
        View childAt = applySize instanceof ReverseLinearLayout.ReverseRelativeLayout ? ((ReverseLinearLayout.ReverseRelativeLayout) applySize).getChildAt(0) : applySize;
        if (view != null) {
            childAt.setAccessibilityTraversalAfter(view.getId());
        }
        if (z) {
            this.mLastLandscape = childAt;
        } else {
            this.mLastPortrait = childAt;
        }
        return applySize;
    }

    private View applySize(View view, String str, boolean z, boolean z2) {
        String extractSize = extractSize(str);
        if (extractSize == null) {
            return view;
        }
        if (extractSize.contains("W") || extractSize.contains("A")) {
            ReverseLinearLayout.ReverseRelativeLayout reverseRelativeLayout = new ReverseLinearLayout.ReverseRelativeLayout(((FrameLayout) this).mContext);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(view.getLayoutParams());
            int i = z ? z2 ? 48 : 80 : z2 ? 8388611 : 8388613;
            if (extractSize.endsWith("WC")) {
                i = 17;
            } else if (extractSize.endsWith("C")) {
                i = 16;
            }
            reverseRelativeLayout.setDefaultGravity(i);
            reverseRelativeLayout.setGravity(i);
            reverseRelativeLayout.addView(view, layoutParams);
            if (extractSize.contains("W")) {
                reverseRelativeLayout.setLayoutParams(new LinearLayout.LayoutParams(0, -1, Float.parseFloat(extractSize.substring(0, extractSize.indexOf("W")))));
            } else {
                reverseRelativeLayout.setLayoutParams(new LinearLayout.LayoutParams((int) convertDpToPx(((FrameLayout) this).mContext, Float.parseFloat(extractSize.substring(0, extractSize.indexOf("A")))), -1));
            }
            reverseRelativeLayout.setClipChildren(false);
            reverseRelativeLayout.setClipToPadding(false);
            return reverseRelativeLayout;
        }
        float parseFloat = Float.parseFloat(extractSize);
        ViewGroup.LayoutParams layoutParams2 = view.getLayoutParams();
        layoutParams2.width = (int) (((float) layoutParams2.width) * parseFloat);
        return view;
    }

    private View createView(String str, ViewGroup viewGroup, LayoutInflater layoutInflater) {
        View view;
        View view2;
        View view3;
        View view4;
        String extractButton = extractButton(str);
        if ("left".equals(extractButton)) {
            extractButton = extractButton("space");
        } else if ("right".equals(extractButton)) {
            extractButton = extractButton("menu_ime");
        }
        if ("home".equals(extractButton)) {
            if (this.mIsCustomNavBar) {
                view4 = layoutInflater.inflate(C0011R$layout.home2, viewGroup, false);
            } else {
                view4 = layoutInflater.inflate(C0011R$layout.home, viewGroup, false);
            }
            if (!this.mIsCustomNavBar) {
                return view4;
            }
            view4.getLayoutParams().width = OpUtils.convertPxByResolutionProportion(((FrameLayout) this).mContext.getResources().getDimension(C0005R$dimen.op_navigation_key_width), 1080);
            int convertPxByResolutionProportion = OpUtils.convertPxByResolutionProportion(((FrameLayout) this).mContext.getResources().getDimension(C0005R$dimen.op_navigation_key_padding), 1080);
            view4.setPadding(convertPxByResolutionProportion, 0, convertPxByResolutionProportion, 0);
            return view4;
        } else if ("back".equals(extractButton)) {
            if (this.mIsCustomNavBar) {
                view3 = layoutInflater.inflate(C0011R$layout.back2, viewGroup, false);
            } else {
                view3 = layoutInflater.inflate(C0011R$layout.back, viewGroup, false);
            }
            if (!this.mIsCustomNavBar) {
                return view3;
            }
            view3.getLayoutParams().width = OpUtils.convertPxByResolutionProportion(((FrameLayout) this).mContext.getResources().getDimension(C0005R$dimen.op_navigation_key_width), 1080);
            return view3;
        } else if ("recent".equals(extractButton)) {
            if (this.mIsCustomNavBar) {
                view2 = layoutInflater.inflate(C0011R$layout.recent_apps2, viewGroup, false);
            } else {
                view2 = layoutInflater.inflate(C0011R$layout.recent_apps, viewGroup, false);
            }
            if (!this.mIsCustomNavBar) {
                return view2;
            }
            view2.getLayoutParams().width = OpUtils.convertPxByResolutionProportion(((FrameLayout) this).mContext.getResources().getDimension(C0005R$dimen.op_navigation_key_width), 1080);
            return view2;
        } else if ("menu_ime".equals(extractButton)) {
            if (this.mIsCustomNavBar) {
                view = layoutInflater.inflate(C0011R$layout.menu_ime2, viewGroup, false);
            } else {
                view = layoutInflater.inflate(C0011R$layout.menu_ime, viewGroup, false);
            }
            if (!this.mIsCustomNavBar) {
                return view;
            }
            view.getLayoutParams().width = OpUtils.convertPxByResolutionProportion(((FrameLayout) this).mContext.getResources().getDimension(C0005R$dimen.op_navigation_extra_key_width), 1080);
            return view;
        } else if ("space".equals(extractButton)) {
            View inflate = layoutInflater.inflate(C0011R$layout.nav_key_space, viewGroup, false);
            if (!this.mIsCustomNavBar) {
                return inflate;
            }
            inflate.getLayoutParams().width = OpUtils.convertPxByResolutionProportion(((FrameLayout) this).mContext.getResources().getDimension(C0005R$dimen.op_navigation_side_padding), 1080);
            return inflate;
        } else if ("clipboard".equals(extractButton)) {
            return layoutInflater.inflate(C0011R$layout.clipboard, viewGroup, false);
        } else {
            if ("contextual".equals(extractButton)) {
                return layoutInflater.inflate(C0011R$layout.contextual, viewGroup, false);
            }
            if ("home_handle".equals(extractButton)) {
                return layoutInflater.inflate(C0011R$layout.home_handle, viewGroup, false);
            }
            if ("ime_switcher".equals(extractButton)) {
                return layoutInflater.inflate(C0011R$layout.ime_switcher, viewGroup, false);
            }
            if ("nav".equals(extractButton)) {
                View inflate2 = layoutInflater.inflate(C0011R$layout.nav, viewGroup, false);
                if (!this.mIsCustomNavBar) {
                    return inflate2;
                }
                inflate2.getLayoutParams().width = OpUtils.convertPxByResolutionProportion(((FrameLayout) this).mContext.getResources().getDimension(C0005R$dimen.op_navigation_extra_key_width), 1080);
                return inflate2;
            } else if (!extractButton.startsWith("key")) {
                return null;
            } else {
                String extractImage = extractImage(extractButton);
                int extractKeycode = extractKeycode(extractButton);
                View inflate3 = layoutInflater.inflate(C0011R$layout.custom_key, viewGroup, false);
                KeyButtonView keyButtonView = (KeyButtonView) inflate3;
                keyButtonView.setCode(extractKeycode);
                if (extractImage != null) {
                    if (extractImage.contains(":")) {
                        keyButtonView.loadAsync(Icon.createWithContentUri(extractImage));
                    } else if (extractImage.contains("/")) {
                        int indexOf = extractImage.indexOf(47);
                        keyButtonView.loadAsync(Icon.createWithResource(extractImage.substring(0, indexOf), Integer.parseInt(extractImage.substring(indexOf + 1))));
                    }
                }
                return inflate3;
            }
        }
    }

    public static String extractImage(String str) {
        if (!str.contains(":")) {
            return null;
        }
        return str.substring(str.indexOf(":") + 1, str.indexOf(")"));
    }

    public static int extractKeycode(String str) {
        if (!str.contains("(")) {
            return 1;
        }
        return Integer.parseInt(str.substring(str.indexOf("(") + 1, str.indexOf(":")));
    }

    public static String extractSize(String str) {
        if (!str.contains("[")) {
            return null;
        }
        return str.substring(str.indexOf("[") + 1, str.indexOf("]"));
    }

    public static String extractButton(String str) {
        if (!str.contains("[")) {
            return str;
        }
        return str.substring(0, str.indexOf("["));
    }

    private void addToDispatchers(View view) {
        SparseArray<ButtonDispatcher> sparseArray = this.mButtonDispatchers;
        if (sparseArray != null) {
            int indexOfKey = sparseArray.indexOfKey(view.getId());
            if (indexOfKey >= 0) {
                this.mButtonDispatchers.valueAt(indexOfKey).addView(view);
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    addToDispatchers(viewGroup.getChildAt(i));
                }
            }
        }
    }

    private void clearViews() {
        if (this.mButtonDispatchers != null) {
            for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
                this.mButtonDispatchers.valueAt(i).clear();
            }
        }
        clearAllChildren((ViewGroup) this.mHorizontal.findViewById(C0008R$id.nav_buttons));
        clearAllChildren((ViewGroup) this.mVertical.findViewById(C0008R$id.nav_buttons));
    }

    private void clearAllChildren(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            ((ViewGroup) viewGroup.getChildAt(i)).removeAllViews();
        }
    }

    private static float convertDpToPx(Context context, float f) {
        return f * context.getResources().getDisplayMetrics().density;
    }

    private void updateChildrenMargin() {
        int i;
        int i2;
        if (this.mIsCustomNavBar) {
            Resources resources = ((FrameLayout) this).mContext.getResources();
            if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
                i2 = C0005R$dimen.op_gesture_navigation_side_padding;
            } else {
                i2 = C0005R$dimen.op_rounded_corner_content_padding;
            }
            i = resources.getDimensionPixelSize(i2);
        } else {
            i = 0;
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mHorizontal.getLayoutParams();
        marginLayoutParams.setMarginsRelative(i, 0, i, 0);
        updateViewLayout(this.mHorizontal, marginLayoutParams);
        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mVertical.getLayoutParams();
        marginLayoutParams2.setMarginsRelative(0, i, 0, i);
        updateViewLayout(this.mVertical, marginLayoutParams2);
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("NavigationBarInflaterView {");
        printWriter.println("      mCurrentLayout: " + this.mCurrentLayout);
        printWriter.println("    }");
    }

    public void updateCurrentView() {
        int i = OpUtils.mScreenResolution;
        if (i != this.mScreenResolution) {
            this.mScreenResolution = i;
            updateChildrenMargin();
            clearViews();
            inflateLayout(getDefaultLayout());
        }
    }
}
