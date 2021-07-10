package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Bundle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.C0001R$array;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.StatusBarMobileView;
import com.android.systemui.statusbar.StatusBarWifiView;
import com.android.systemui.statusbar.StatusIconDisplayable;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
import com.android.systemui.util.Utils;
import com.oneplus.networkspeed.StatusBarOPCustView;
import java.util.List;
public interface StatusBarIconController {
    void addIconGroup(IconManager iconManager);

    void removeAllIconsForSlot(String str);

    void removeIconGroup(IconManager iconManager);

    void setExternalIcon(String str);

    void setIcon(String str, int i, CharSequence charSequence);

    void setIcon(String str, StatusBarIcon statusBarIcon);

    void setIconAccessibilityLiveRegion(String str, int i);

    void setIconVisibility(String str, boolean z);

    void setMobileIcons(String str, List<StatusBarSignalPolicy.MobileIconState> list);

    void setOPCustView(String str, int i, boolean z);

    void setSignalIcon(String str, StatusBarSignalPolicy.WifiIconState wifiIconState);

    static ArraySet<String> getIconBlacklist(Context context, String str) {
        String[] strArr;
        ArraySet<String> arraySet = new ArraySet<>();
        if (str == null) {
            strArr = context.getResources().getStringArray(C0001R$array.op_config_statusBarIconBlackList);
            TelephonyManager telephonyManager = TelephonyManager.getDefault();
            if (telephonyManager != null && TextUtils.equals(telephonyManager.getSimOperatorNumeric(SubscriptionManager.getDefaultDataSubscriptionId()), "23410")) {
                Log.d("StatusBarIconController", "O2 UK sim, add volte/vowifi to blacklist by default");
                strArr = context.getResources().getStringArray(C0001R$array.op_o2_uk_config_statusBarIconBlackList);
            }
        } else {
            strArr = str.split(",");
        }
        for (String str2 : strArr) {
            if (!TextUtils.isEmpty(str2)) {
                arraySet.add(str2);
            }
        }
        return arraySet;
    }

    public static class DarkIconManager extends IconManager {
        private final DarkIconDispatcher mDarkIconDispatcher = ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class));
        private int mIconHPadding = this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.status_bar_icon_padding);

        public DarkIconManager(LinearLayout linearLayout, CommandQueue commandQueue) {
            super(linearLayout, commandQueue);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        public void onIconAdded(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder) {
            StatusIconDisplayable addHolder = addHolder(i, str, z, statusBarIconHolder);
            this.mDarkIconDispatcher.addDarkReceiver(addHolder);
            if (isBTLowBattery(addHolder)) {
                updateBTLowBatteryIcon((StatusBarIconView) addHolder);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        public LinearLayout.LayoutParams onCreateLayoutParams() {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, this.mIconSize);
            int i = this.mIconHPadding;
            layoutParams.setMargins(i, 0, i, 0);
            return layoutParams;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        public void destroy() {
            for (int i = 0; i < this.mGroup.getChildCount(); i++) {
                this.mDarkIconDispatcher.removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mGroup.getChildAt(i));
            }
            this.mGroup.removeAllViews();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        public void onRemoveIcon(int i) {
            this.mDarkIconDispatcher.removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mGroup.getChildAt(i));
            super.onRemoveIcon(i);
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        public void onSetIcon(int i, StatusBarIcon statusBarIcon) {
            super.onSetIcon(i, statusBarIcon);
            this.mDarkIconDispatcher.applyDark((DarkIconDispatcher.DarkReceiver) this.mGroup.getChildAt(i));
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        public DemoStatusIcons createDemoStatusIcons() {
            DemoStatusIcons createDemoStatusIcons = super.createDemoStatusIcons();
            this.mDarkIconDispatcher.addDarkReceiver(createDemoStatusIcons);
            return createDemoStatusIcons;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        public void exitDemoMode() {
            this.mDarkIconDispatcher.removeDarkReceiver(this.mDemoStatusIcons);
            super.exitDemoMode();
        }

        private void updateBTLowBatteryIcon(StatusBarIconView statusBarIconView) {
            int resId = statusBarIconView.getStatusBarIcon().icon.getResId();
            if (isDarkIcon()) {
                if (resId == C0006R$drawable.stat_sys_bt_battery_1) {
                    statusBarIconView.setImageResource(C0006R$drawable.stat_sys_bt_battery_1_dark);
                } else if (resId == C0006R$drawable.stat_sys_bt_battery_2) {
                    statusBarIconView.setImageResource(C0006R$drawable.stat_sys_bt_battery_2_dark);
                }
            } else if (resId == C0006R$drawable.stat_sys_bt_battery_1_dark) {
                statusBarIconView.setImageResource(C0006R$drawable.stat_sys_bt_battery_1);
            } else if (resId == C0006R$drawable.stat_sys_bt_battery_2_dark) {
                statusBarIconView.setImageResource(C0006R$drawable.stat_sys_bt_battery_2);
            }
            statusBarIconView.setImageTintList(null);
        }

        private boolean isDarkIcon() {
            return ((SysuiDarkIconDispatcher) this.mDarkIconDispatcher).getTransitionsController().getCurrentDarkIntensity() == 1.0f;
        }
    }

    public static class TintedIconManager extends IconManager {
        private int mColor;

        private void updateBTLowBatteryIcon(StatusBarIconView statusBarIconView) {
        }

        public TintedIconManager(ViewGroup viewGroup, CommandQueue commandQueue, String str) {
            super(viewGroup, commandQueue, str);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        public void onIconAdded(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder) {
            StatusIconDisplayable addHolder = addHolder(i, str, z, statusBarIconHolder);
            if (isBTLowBattery(addHolder)) {
                updateBTLowBatteryIcon((StatusBarIconView) addHolder);
            } else {
                addHolder.setStaticDrawableColor(this.mColor);
            }
            addHolder.setDecorColor(this.mColor);
        }

        public void setTint(int i) {
            this.mColor = i;
            for (int i2 = 0; i2 < this.mGroup.getChildCount(); i2++) {
                View childAt = this.mGroup.getChildAt(i2);
                if (childAt instanceof StatusIconDisplayable) {
                    StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) childAt;
                    if (isBTLowBattery(statusIconDisplayable)) {
                        updateBTLowBatteryIcon((StatusBarIconView) statusIconDisplayable);
                    } else {
                        statusIconDisplayable.setStaticDrawableColor(this.mColor);
                    }
                    statusIconDisplayable.setDecorColor(this.mColor);
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        public DemoStatusIcons createDemoStatusIcons() {
            DemoStatusIcons createDemoStatusIcons = super.createDemoStatusIcons();
            createDemoStatusIcons.setColor(this.mColor);
            return createDemoStatusIcons;
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        public void onSetIcon(int i, StatusBarIcon statusBarIcon) {
            super.onSetIcon(i, statusBarIcon);
            StatusBarIconView statusBarIconView = (StatusBarIconView) this.mGroup.getChildAt(i);
            if (isBTLowBattery(statusBarIconView)) {
                updateBTLowBatteryIcon(statusBarIconView);
            } else {
                statusBarIconView.setStaticDrawableColor(this.mColor);
            }
        }
    }

    public static class IconManager implements DemoMode {
        protected final Context mContext;
        protected DemoStatusIcons mDemoStatusIcons;
        protected boolean mDemoable;
        protected final ViewGroup mGroup;
        protected int mIconSize;
        private boolean mIsInDemoMode;
        protected boolean mShouldLog;

        public IconManager(ViewGroup viewGroup, CommandQueue commandQueue, String str) {
            this(viewGroup, commandQueue);
        }

        public IconManager(ViewGroup viewGroup, CommandQueue commandQueue) {
            this.mShouldLog = false;
            this.mDemoable = true;
            this.mGroup = viewGroup;
            Context context = viewGroup.getContext();
            this.mContext = context;
            this.mIconSize = context.getResources().getDimensionPixelSize(17105484);
            Utils.DisableStateTracker disableStateTracker = new Utils.DisableStateTracker(0, 2, commandQueue);
            this.mGroup.addOnAttachStateChangeListener(disableStateTracker);
            if (this.mGroup.isAttachedToWindow()) {
                disableStateTracker.onViewAttachedToWindow(this.mGroup);
            }
        }

        public boolean isDemoable() {
            return this.mDemoable;
        }

        public void setShouldLog(boolean z) {
            this.mShouldLog = z;
        }

        public boolean shouldLog() {
            return this.mShouldLog;
        }

        /* access modifiers changed from: protected */
        public void onIconAdded(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder) {
            addHolder(i, str, z, statusBarIconHolder);
        }

        /* access modifiers changed from: protected */
        public StatusIconDisplayable addHolder(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder) {
            int type = statusBarIconHolder.getType();
            if (type == 0) {
                return addIcon(i, str, z, statusBarIconHolder.getIcon());
            }
            if (type == 1) {
                return addSignalIcon(i, str, statusBarIconHolder.getWifiState());
            }
            if (type == 2) {
                return addMobileIcon(i, str, statusBarIconHolder.getMobileState());
            }
            if (type != 3) {
                return null;
            }
            return addText(i, str, statusBarIconHolder.getResourceId(), statusBarIconHolder.isVisible());
        }

        /* access modifiers changed from: protected */
        public StatusBarIconView addIcon(int i, String str, boolean z, StatusBarIcon statusBarIcon) {
            StatusBarIconView onCreateStatusBarIconView = onCreateStatusBarIconView(str, z);
            onCreateStatusBarIconView.set(statusBarIcon);
            this.mGroup.addView(onCreateStatusBarIconView, i, onCreateLayoutParams());
            return onCreateStatusBarIconView;
        }

        /* access modifiers changed from: protected */
        public StatusBarWifiView addSignalIcon(int i, String str, StatusBarSignalPolicy.WifiIconState wifiIconState) {
            StatusBarWifiView onCreateStatusBarWifiView = onCreateStatusBarWifiView(str);
            onCreateStatusBarWifiView.applyWifiState(wifiIconState);
            this.mGroup.addView(onCreateStatusBarWifiView, i, onCreateLayoutParams());
            if (this.mIsInDemoMode) {
                this.mDemoStatusIcons.addDemoWifiView(wifiIconState);
            }
            return onCreateStatusBarWifiView;
        }

        /* access modifiers changed from: protected */
        public StatusBarMobileView addMobileIcon(int i, String str, StatusBarSignalPolicy.MobileIconState mobileIconState) {
            StatusBarMobileView onCreateStatusBarMobileView = onCreateStatusBarMobileView(str);
            onCreateStatusBarMobileView.applyMobileState(mobileIconState);
            this.mGroup.addView(onCreateStatusBarMobileView, i, onCreateLayoutParams());
            if (this.mIsInDemoMode) {
                this.mDemoStatusIcons.addMobileView(mobileIconState);
            }
            return onCreateStatusBarMobileView;
        }

        /* access modifiers changed from: protected */
        public StatusBarOPCustView addText(int i, String str, int i2, boolean z) {
            StatusBarOPCustView onCreateStatusBarText = onCreateStatusBarText(str, i2);
            onCreateStatusBarText.applyVisible(z);
            this.mGroup.addView(onCreateStatusBarText, i, onCreateLayoutParams());
            return onCreateStatusBarText;
        }

        private StatusBarIconView onCreateStatusBarIconView(String str, boolean z) {
            return new StatusBarIconView(this.mContext, str, null, z);
        }

        private StatusBarWifiView onCreateStatusBarWifiView(String str) {
            return StatusBarWifiView.fromContext(this.mContext, str);
        }

        private StatusBarMobileView onCreateStatusBarMobileView(String str) {
            return StatusBarMobileView.fromContext(this.mContext, str);
        }

        private StatusBarOPCustView onCreateStatusBarText(String str, int i) {
            StatusBarOPCustView fromResId = StatusBarOPCustView.fromResId(this.mContext, i);
            fromResId.setSlot(str);
            return fromResId;
        }

        /* access modifiers changed from: protected */
        public LinearLayout.LayoutParams onCreateLayoutParams() {
            return new LinearLayout.LayoutParams(-2, this.mIconSize);
        }

        /* access modifiers changed from: protected */
        public void destroy() {
            this.mGroup.removeAllViews();
        }

        /* access modifiers changed from: protected */
        public void onIconExternal(int i, int i2) {
            ImageView imageView = (ImageView) this.mGroup.getChildAt(i);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            setHeightAndCenter(imageView, i2);
        }

        private void setHeightAndCenter(ImageView imageView, int i) {
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.height = i;
            if (layoutParams instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) layoutParams).gravity = 16;
            }
            imageView.setLayoutParams(layoutParams);
        }

        /* access modifiers changed from: protected */
        public void onRemoveIcon(int i) {
            if (this.mIsInDemoMode) {
                this.mDemoStatusIcons.onRemoveIcon((StatusIconDisplayable) this.mGroup.getChildAt(i));
            }
            this.mGroup.removeViewAt(i);
        }

        public void onSetIcon(int i, StatusBarIcon statusBarIcon) {
            ((StatusBarIconView) this.mGroup.getChildAt(i)).set(statusBarIcon);
        }

        public void onSetIconHolder(int i, StatusBarIconHolder statusBarIconHolder) {
            int type = statusBarIconHolder.getType();
            if (type == 0) {
                onSetIcon(i, statusBarIconHolder.getIcon());
            } else if (type == 1) {
                onSetSignalIcon(i, statusBarIconHolder.getWifiState());
            } else if (type == 2) {
                onSetMobileIcon(i, statusBarIconHolder.getMobileState());
            } else if (type == 3) {
                onSetTextVisible(i, statusBarIconHolder.isVisible());
            }
        }

        public void onSetSignalIcon(int i, StatusBarSignalPolicy.WifiIconState wifiIconState) {
            StatusBarWifiView statusBarWifiView = (StatusBarWifiView) this.mGroup.getChildAt(i);
            if (statusBarWifiView != null) {
                statusBarWifiView.applyWifiState(wifiIconState);
            }
            if (this.mIsInDemoMode) {
                this.mDemoStatusIcons.updateWifiState(wifiIconState);
            }
        }

        public void onSetMobileIcon(int i, StatusBarSignalPolicy.MobileIconState mobileIconState) {
            StatusBarMobileView statusBarMobileView = (StatusBarMobileView) this.mGroup.getChildAt(i);
            if (statusBarMobileView != null) {
                statusBarMobileView.applyMobileState(mobileIconState);
            }
            if (this.mIsInDemoMode) {
                this.mDemoStatusIcons.updateMobileState(mobileIconState);
            }
        }

        public void onSetTextVisible(int i, boolean z) {
            StatusBarOPCustView statusBarOPCustView = (StatusBarOPCustView) this.mGroup.getChildAt(i);
            if (statusBarOPCustView != null) {
                statusBarOPCustView.applyVisible(z);
            }
        }

        @Override // com.android.systemui.DemoMode
        public void dispatchDemoCommand(String str, Bundle bundle) {
            if (this.mDemoable) {
                if (str.equals("exit")) {
                    DemoStatusIcons demoStatusIcons = this.mDemoStatusIcons;
                    if (demoStatusIcons != null) {
                        demoStatusIcons.dispatchDemoCommand(str, bundle);
                        exitDemoMode();
                    }
                    this.mIsInDemoMode = false;
                    return;
                }
                if (this.mDemoStatusIcons == null) {
                    this.mIsInDemoMode = true;
                    this.mDemoStatusIcons = createDemoStatusIcons();
                }
                this.mDemoStatusIcons.dispatchDemoCommand(str, bundle);
            }
        }

        /* access modifiers changed from: protected */
        public void exitDemoMode() {
            this.mDemoStatusIcons.remove();
            this.mDemoStatusIcons = null;
        }

        /* access modifiers changed from: protected */
        public DemoStatusIcons createDemoStatusIcons() {
            return new DemoStatusIcons((LinearLayout) this.mGroup, this.mIconSize);
        }

        /* access modifiers changed from: protected */
        public boolean isBTLowBattery(StatusIconDisplayable statusIconDisplayable) {
            if (!(statusIconDisplayable instanceof StatusBarIconView)) {
                return false;
            }
            StatusBarIconView statusBarIconView = (StatusBarIconView) statusIconDisplayable;
            if (!statusBarIconView.getSlot().equals("bluetooth")) {
                return false;
            }
            int resId = statusBarIconView.getStatusBarIcon().icon.getResId();
            if (resId == C0006R$drawable.stat_sys_bt_battery_1 || resId == C0006R$drawable.stat_sys_bt_battery_1_dark || resId == C0006R$drawable.stat_sys_bt_battery_2 || resId == C0006R$drawable.stat_sys_bt_battery_2_dark) {
                return true;
            }
            return false;
        }

        public void updateIconSize() {
            this.mIconSize = this.mContext.getResources().getDimensionPixelSize(17105484);
            Log.d("IconManager", "updateIconSize: " + this.mIconSize);
        }
    }
}
