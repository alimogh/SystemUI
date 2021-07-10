package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SubscriptionInfo;
import android.util.ArraySet;
import android.util.Log;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.tuner.TunerService;
import com.oneplus.util.OpUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.codeaurora.internal.IExtTelephony;
public class StatusBarSignalPolicy implements NetworkController.SignalCallback, SecurityController.SecurityControllerCallback, TunerService.Tunable {
    static final boolean OP_DEBUG = Build.DEBUG_ONEPLUS;
    private boolean mActivityEnabled;
    private String mBlackList;
    private boolean mBlockAirplane;
    private boolean mBlockEthernet;
    private boolean[] mBlockLTE;
    private boolean mBlockMobile;
    private boolean mBlockWifi;
    private boolean mCTA;
    private final Context mContext;
    private IExtTelephony mExtTelephony;
    private boolean mForceBlockWifi;
    private final Handler mHandler = Handler.getMain();
    private final StatusBarIconController mIconController;
    private boolean mIsAirplaneMode;
    private List<LTEIconState> mLTEIconStates;
    private ArrayList<MobileIconState> mMobileStates;
    private final NetworkController mNetworkController;
    private int[] mProvisionState;
    private final SecurityController mSecurityController;
    private boolean mShowNoSim;
    private final String mSlotAirplane;
    private final String mSlotEthernet;
    private final String mSlotMobile;
    private final String mSlotVpn;
    private final String mSlotWifi;
    private WifiIconState mWifiIconState;

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataEnabled(boolean z) {
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setNoSims(boolean z, boolean z2) {
    }

    public StatusBarSignalPolicy(Context context, StatusBarIconController statusBarIconController) {
        boolean z = false;
        this.mIsAirplaneMode = false;
        this.mMobileStates = new ArrayList<>();
        this.mWifiIconState = new WifiIconState();
        this.mShowNoSim = true;
        this.mCTA = false;
        this.mLTEIconStates = new ArrayList();
        this.mBlockLTE = new boolean[]{false, false, false, false, false, false};
        this.mBlackList = null;
        this.mProvisionState = new int[]{1, 1};
        this.mContext = context;
        this.mSlotAirplane = context.getString(17041300);
        this.mSlotMobile = this.mContext.getString(17041318);
        this.mSlotWifi = this.mContext.getString(17041334);
        this.mSlotEthernet = this.mContext.getString(17041310);
        this.mSlotVpn = this.mContext.getString(17041333);
        this.mActivityEnabled = this.mContext.getResources().getBoolean(C0003R$bool.config_showActivity);
        boolean hasCtaFeature = OpUtils.hasCtaFeature(context);
        this.mCTA = hasCtaFeature;
        this.mShowNoSim = (this.mShowNoSim || hasCtaFeature) ? true : z;
        initProvistionState();
        this.mIconController = statusBarIconController;
        this.mNetworkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mSecurityController = (SecurityController) Dependency.get(SecurityController.class);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "icon_blacklist");
        this.mNetworkController.addCallback((NetworkController.SignalCallback) this);
        this.mSecurityController.addCallback(this);
    }

    private void initProvistionState() {
        Log.i("StatusBarSignalPolicy", "init provision");
        int i = !OpUtils.isReallyHasOneSim() ? -1 : 1;
        int i2 = 0;
        while (true) {
            int[] iArr = this.mProvisionState;
            if (i2 < iArr.length) {
                iArr[i2] = getSlotProvisionStatus(i2, i);
                i2++;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateVpn() {
        boolean isVpnEnabled = this.mSecurityController.isVpnEnabled();
        this.mIconController.setIcon(this.mSlotVpn, currentVpnIconId(this.mSecurityController.isVpnBranded()), this.mContext.getResources().getString(C0015R$string.accessibility_vpn_on));
        this.mIconController.setIconVisibility(this.mSlotVpn, isVpnEnabled);
    }

    private int currentVpnIconId(boolean z) {
        return z ? C0006R$drawable.stat_sys_branded_vpn : C0006R$drawable.stat_sys_vpn_ic;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController.SecurityControllerCallback
    public void onStateChanged() {
        this.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.phone.-$$Lambda$StatusBarSignalPolicy$UsBELiDs0GJjQ8hYeagcWJmxhFc
            @Override // java.lang.Runnable
            public final void run() {
                StatusBarSignalPolicy.this.updateVpn();
            }
        });
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            if (OP_DEBUG) {
                Log.i("StatusBarSignalPolicy", "onTuningChanged / newValue:" + str2 + " / mBlackList:" + this.mBlackList);
            }
            this.mBlackList = str2;
            ArraySet<String> iconBlacklist = StatusBarIconController.getIconBlacklist(this.mContext, str2);
            boolean contains = iconBlacklist.contains(this.mSlotAirplane);
            boolean contains2 = iconBlacklist.contains(this.mSlotMobile);
            boolean contains3 = iconBlacklist.contains(this.mSlotWifi);
            boolean contains4 = iconBlacklist.contains(this.mSlotEthernet);
            boolean contains5 = iconBlacklist.contains("volte");
            boolean contains6 = iconBlacklist.contains("vowifi");
            if (contains == this.mBlockAirplane && contains2 == this.mBlockMobile && contains4 == this.mBlockEthernet && contains3 == this.mBlockWifi) {
                boolean[] zArr = this.mBlockLTE;
                if (contains5 == zArr[0] && contains6 == zArr[2]) {
                    return;
                }
            }
            this.mBlockAirplane = contains;
            this.mBlockMobile = contains2;
            this.mBlockEthernet = contains4;
            this.mBlockWifi = contains3 || this.mForceBlockWifi;
            boolean[] zArr2 = this.mBlockLTE;
            zArr2[0] = contains5;
            zArr2[2] = contains6;
            this.mNetworkController.removeCallback((NetworkController.SignalCallback) this);
            this.mNetworkController.addCallback((NetworkController.SignalCallback) this);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
        boolean z5 = true;
        boolean z6 = iconState.visible && !this.mBlockWifi;
        boolean z7 = z2 && this.mActivityEnabled && z6;
        boolean z8 = z3 && this.mActivityEnabled && z6;
        WifiIconState copy = this.mWifiIconState.copy();
        copy.visible = z6;
        copy.resId = iconState.icon;
        copy.activityIn = z7;
        copy.activityOut = z8;
        copy.slot = this.mSlotWifi;
        copy.airplaneSpacerVisible = this.mIsAirplaneMode;
        copy.contentDescription = iconState.contentDescription;
        MobileIconState firstMobileState = getFirstMobileState();
        if (firstMobileState == null || (firstMobileState.typeId == 0 && firstMobileState.stackedDataTypeId == 0 && firstMobileState.stackedVoiceTypeId == 0 && !firstMobileState.showNoSim)) {
            z5 = false;
        }
        copy.signalSpacerVisible = z5;
        updateWifiIconWithState(copy);
        this.mWifiIconState = copy;
    }

    private void updateShowWifiSignalSpacer(WifiIconState wifiIconState) {
        MobileIconState firstMobileState = getFirstMobileState();
        wifiIconState.signalSpacerVisible = (firstMobileState == null || (firstMobileState.typeId == 0 && firstMobileState.stackedDataTypeId == 0 && firstMobileState.stackedVoiceTypeId == 0 && !firstMobileState.showNoSim)) ? false : true;
    }

    private void updateWifiIconWithState(WifiIconState wifiIconState) {
        if (!wifiIconState.visible || wifiIconState.resId <= 0) {
            this.mIconController.setIconVisibility(this.mSlotWifi, false);
            return;
        }
        this.mIconController.setSignalIcon(this.mSlotWifi, wifiIconState);
        this.mIconController.setIconVisibility(this.mSlotWifi, true);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, int[] iArr, int[] iArr2, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4, boolean z5) {
        MobileIconState state = getState(i4);
        if (state == null) {
            Log.i("StatusBarSignalPolicy", "setMobileDataIndicators / state == null");
            return;
        }
        int i5 = state.typeId;
        boolean z6 = i != i5 && (i == 0 || i5 == 0);
        state.visible = iconState.visible && !this.mBlockMobile;
        state.strengthId = iconState.icon;
        state.typeId = i;
        state.contentDescription = iconState.contentDescription;
        state.typeContentDescription = charSequence;
        state.roaming = z4;
        state.activityIn = z && this.mActivityEnabled;
        state.activityOut = z2 && this.mActivityEnabled;
        state.stackedDataStrengthId = iArr[0];
        state.stackedVoiceStrengthId = iArr2[0];
        state.stackedDataTypeId = iArr[1];
        state.stackedVoiceTypeId = iArr2[1];
        state.dataConnected = z5;
        this.mIconController.setMobileIcons(this.mSlotMobile, MobileIconState.copyStates(this.mMobileStates));
        if (z6) {
            WifiIconState copy = this.mWifiIconState.copy();
            updateShowWifiSignalSpacer(copy);
            if (!Objects.equals(copy, this.mWifiIconState)) {
                updateWifiIconWithState(copy);
                this.mWifiIconState = copy;
            }
        }
    }

    private MobileIconState getState(int i) {
        Iterator<MobileIconState> it = this.mMobileStates.iterator();
        while (it.hasNext()) {
            MobileIconState next = it.next();
            if (next.subId == i) {
                return next;
            }
        }
        Log.e("StatusBarSignalPolicy", "Unexpected subscription " + i);
        return null;
    }

    private MobileIconState getFirstMobileState() {
        if (this.mMobileStates.size() > 0) {
            return this.mMobileStates.get(0);
        }
        return null;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setSubs(List<SubscriptionInfo> list) {
        if (!hasCorrectSubs(list)) {
            Log.i("StatusBarSignalPolicy", "setSubs s");
            initLTEIcon();
            this.mIconController.removeAllIconsForSlot(this.mSlotMobile);
            this.mMobileStates.clear();
            int size = list.size();
            int simCount = OpUtils.getSimCount();
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < size; i++) {
                int simSlotIndex = list.get(i).getSimSlotIndex();
                MobileIconState mobileIconState = new MobileIconState(list.get(i).getSubscriptionId());
                mobileIconState.phoneId = simSlotIndex;
                arrayList.add(mobileIconState);
                if (simSlotIndex >= simCount) {
                    simCount = simSlotIndex + 1;
                }
            }
            MobileIconState[] mobileIconStateArr = new MobileIconState[simCount];
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                mobileIconStateArr[((MobileIconState) arrayList.get(i2)).phoneId] = (MobileIconState) arrayList.get(i2);
            }
            if (this.mShowNoSim) {
                for (int i3 = simCount - 1; i3 >= 0; i3--) {
                    if (size == 0 || (this.mCTA && mobileIconStateArr[i3] == null)) {
                        MobileIconState mobileIconState2 = new MobileIconState((0 - i3) - 1);
                        mobileIconState2.phoneId = i3;
                        this.mMobileStates.add(mobileIconState2);
                        mobileIconState2.visible = !this.mIsAirplaneMode && !this.mBlockMobile;
                    } else if (mobileIconStateArr[i3] != null) {
                        this.mMobileStates.add(mobileIconStateArr[i3]);
                    }
                }
            }
            this.mIconController.setMobileIcons(this.mSlotMobile, MobileIconState.copyStates(this.mMobileStates));
            dumpMobileStates();
            Log.i("StatusBarSignalPolicy", "setSubs e");
        }
    }

    private void dumpMobileStates() {
        for (int i = 0; i < this.mMobileStates.size(); i++) {
            Log.i("StatusBarSignalPolicy", " setSubs log:" + this.mMobileStates.get(i).toString());
        }
    }

    private boolean hasCorrectSubs(List<SubscriptionInfo> list) {
        int size = list.size();
        if (size != this.mMobileStates.size() || list.size() == 0) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (this.mMobileStates.get(i).subId != list.get(i).getSubscriptionId()) {
                return false;
            }
            if (this.mMobileStates.get(i).subId == list.get(i).getSubscriptionId() && this.mMobileStates.get(i).phoneId != list.get(i).getSimSlotIndex()) {
                Log.i("StatusBarSignalPolicy", "hasCorrectSubs SubId:" + this.mMobileStates.get(i).subId + " change from:" + this.mMobileStates.get(i).phoneId + " to:" + list.get(i).getSimSlotIndex());
                return false;
            }
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setEthernetIndicators(NetworkController.IconState iconState) {
        if (iconState.visible) {
            boolean z = this.mBlockEthernet;
        }
        int i = iconState.icon;
        String str = iconState.contentDescription;
        if (i > 0) {
            this.mIconController.setIcon(this.mSlotEthernet, i, str);
            this.mIconController.setIconVisibility(this.mSlotEthernet, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotEthernet, false);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        boolean z = iconState.visible && !this.mBlockAirplane;
        this.mIsAirplaneMode = z;
        int i = iconState.icon;
        String str = iconState.contentDescription;
        if (!z || i <= 0) {
            this.mIconController.setIconVisibility(this.mSlotAirplane, false);
        } else {
            this.mIconController.setIcon(this.mSlotAirplane, i, str);
            this.mIconController.setIconVisibility(this.mSlotAirplane, true);
        }
        Iterator<MobileIconState> it = this.mMobileStates.iterator();
        while (it.hasNext()) {
            MobileIconState next = it.next();
            if (next.subId < 0) {
                next.visible = !this.mIsAirplaneMode && !this.mBlockMobile;
            }
        }
        this.mIconController.setMobileIcons(this.mSlotMobile, MobileIconState.copyStates(this.mMobileStates));
    }

    /* access modifiers changed from: private */
    public static abstract class SignalIconState {
        public boolean activityIn;
        public boolean activityOut;
        public String contentDescription;
        public String slot;
        public boolean visible;

        private SignalIconState() {
        }

        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SignalIconState signalIconState = (SignalIconState) obj;
            if (this.visible == signalIconState.visible && this.activityOut == signalIconState.activityOut && this.activityIn == signalIconState.activityIn && Objects.equals(this.contentDescription, signalIconState.contentDescription) && Objects.equals(this.slot, signalIconState.slot)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(Boolean.valueOf(this.visible), Boolean.valueOf(this.activityOut), this.slot);
        }

        /* access modifiers changed from: protected */
        public void copyTo(SignalIconState signalIconState) {
            signalIconState.visible = this.visible;
            signalIconState.activityIn = this.activityIn;
            signalIconState.activityOut = this.activityOut;
            signalIconState.slot = this.slot;
            signalIconState.contentDescription = this.contentDescription;
        }
    }

    public static class WifiIconState extends SignalIconState {
        public boolean airplaneSpacerVisible;
        public int resId;
        public boolean signalSpacerVisible;

        public WifiIconState() {
            super();
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public boolean equals(Object obj) {
            if (obj == null || WifiIconState.class != obj.getClass() || !super.equals(obj)) {
                return false;
            }
            WifiIconState wifiIconState = (WifiIconState) obj;
            if (this.resId == wifiIconState.resId && this.airplaneSpacerVisible == wifiIconState.airplaneSpacerVisible && this.signalSpacerVisible == wifiIconState.signalSpacerVisible) {
                return true;
            }
            return false;
        }

        public void copyTo(WifiIconState wifiIconState) {
            super.copyTo((SignalIconState) wifiIconState);
            wifiIconState.resId = this.resId;
            wifiIconState.airplaneSpacerVisible = this.airplaneSpacerVisible;
            wifiIconState.signalSpacerVisible = this.signalSpacerVisible;
        }

        public WifiIconState copy() {
            WifiIconState wifiIconState = new WifiIconState();
            copyTo(wifiIconState);
            return wifiIconState;
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public int hashCode() {
            return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.resId), Boolean.valueOf(this.airplaneSpacerVisible), Boolean.valueOf(this.signalSpacerVisible));
        }

        public String toString() {
            return "WifiIconState(resId=" + this.resId + ", visible=" + this.visible + ")";
        }
    }

    public static class MobileIconState extends SignalIconState {
        public boolean dataConnected;
        public boolean needsLeadingPadding;
        public int phoneId;
        public boolean roaming;
        public boolean showNoSim;
        public int stackedDataStrengthId;
        public int stackedDataTypeId;
        public int stackedVoiceStrengthId;
        public int stackedVoiceTypeId;
        public int strengthId;
        public int subId;
        public CharSequence typeContentDescription;
        public int typeId;
        public int volteId;

        private MobileIconState(int i) {
            super();
            this.stackedDataStrengthId = 0;
            this.stackedVoiceStrengthId = 0;
            this.stackedDataTypeId = 0;
            this.stackedVoiceTypeId = 0;
            this.dataConnected = false;
            this.phoneId = 0;
            this.subId = i;
            if (i < 0) {
                this.showNoSim = true;
                this.visible = true;
            }
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public boolean equals(Object obj) {
            if (obj == null || MobileIconState.class != obj.getClass() || !super.equals(obj)) {
                return false;
            }
            MobileIconState mobileIconState = (MobileIconState) obj;
            if (this.subId == mobileIconState.subId && this.strengthId == mobileIconState.strengthId && this.typeId == mobileIconState.typeId && this.roaming == mobileIconState.roaming && this.stackedDataTypeId == mobileIconState.stackedDataTypeId && this.stackedVoiceTypeId == mobileIconState.stackedVoiceTypeId && this.stackedDataStrengthId == mobileIconState.stackedDataStrengthId && this.stackedVoiceStrengthId == mobileIconState.stackedVoiceStrengthId && this.dataConnected == mobileIconState.dataConnected && this.showNoSim == mobileIconState.showNoSim && this.phoneId == mobileIconState.phoneId && this.needsLeadingPadding == mobileIconState.needsLeadingPadding && Objects.equals(this.typeContentDescription, mobileIconState.typeContentDescription) && this.volteId == mobileIconState.volteId) {
                return true;
            }
            return false;
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public int hashCode() {
            return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.subId), Integer.valueOf(this.strengthId), Integer.valueOf(this.typeId), Boolean.valueOf(this.roaming), Boolean.valueOf(this.needsLeadingPadding), this.typeContentDescription);
        }

        public MobileIconState copy() {
            MobileIconState mobileIconState = new MobileIconState(this.subId);
            copyTo(mobileIconState);
            return mobileIconState;
        }

        public void copyTo(MobileIconState mobileIconState) {
            super.copyTo((SignalIconState) mobileIconState);
            mobileIconState.subId = this.subId;
            mobileIconState.strengthId = this.strengthId;
            mobileIconState.typeId = this.typeId;
            mobileIconState.roaming = this.roaming;
            mobileIconState.needsLeadingPadding = this.needsLeadingPadding;
            mobileIconState.typeContentDescription = this.typeContentDescription;
            mobileIconState.volteId = this.volteId;
            mobileIconState.stackedDataStrengthId = this.stackedDataStrengthId;
            mobileIconState.stackedVoiceStrengthId = this.stackedVoiceStrengthId;
            mobileIconState.stackedDataTypeId = this.stackedDataTypeId;
            mobileIconState.stackedVoiceTypeId = this.stackedVoiceTypeId;
            mobileIconState.dataConnected = this.dataConnected;
            mobileIconState.showNoSim = this.showNoSim;
            mobileIconState.phoneId = this.phoneId;
        }

        /* access modifiers changed from: private */
        public static List<MobileIconState> copyStates(List<MobileIconState> list) {
            ArrayList arrayList = new ArrayList();
            for (MobileIconState mobileIconState : list) {
                MobileIconState mobileIconState2 = new MobileIconState(mobileIconState.subId);
                mobileIconState.copyTo(mobileIconState2);
                arrayList.add(mobileIconState2);
            }
            return arrayList;
        }

        public String toString() {
            return "MobileIconState(subId=" + this.subId + ", strengthId=" + this.strengthId + ", roaming=" + this.roaming + ", typeId=" + this.typeId + ", volteId=" + this.volteId + ", visible=" + this.visible + ")";
        }

        public boolean isAbsent() {
            return this.subId < 0;
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setLTEStatus(NetworkController.IconState[] iconStateArr) {
        if (OP_DEBUG) {
            Log.i("StatusBarSignalPolicy", "setLTEStatus / mBlackList:" + this.mBlackList);
        }
        String str = this.mBlackList;
        if (str == null) {
            ArraySet<String> iconBlacklist = StatusBarIconController.getIconBlacklist(this.mContext, str);
            boolean contains = iconBlacklist.contains("volte");
            boolean contains2 = iconBlacklist.contains("vowifi");
            boolean[] zArr = this.mBlockLTE;
            zArr[0] = contains;
            zArr[2] = contains2;
        }
        for (int i = 0; i < this.mLTEIconStates.size(); i++) {
            LTEIconState lTEIconState = this.mLTEIconStates.get(i);
            int i2 = lTEIconState.imsIndex;
            boolean z = iconStateArr[i2].visible && !this.mBlockLTE[i2] && iconStateArr[i2].icon > 0;
            lTEIconState.visible = z;
            if (OP_DEBUG && !z) {
                Log.i("StatusBarSignalPolicy", "setLTEStatus / status[imsIndex].visible:" + iconStateArr[i2].visible + " / mBlockLTE[imsIndex]:" + this.mBlockLTE[i2] + " / status[imsIndex].icon:" + iconStateArr[i2].icon + " / imsIndex:" + i2);
            }
            if (lTEIconState.visible) {
                int i3 = iconStateArr[i2].icon;
                lTEIconState.resId = i3;
                String str2 = iconStateArr[i2].contentDescription;
                lTEIconState.contentDescription = str2;
                this.mIconController.setIcon(lTEIconState.slot, i3, str2);
                this.mIconController.setIconVisibility(lTEIconState.slot, true);
            } else {
                this.mIconController.setIconVisibility(lTEIconState.slot, false);
            }
        }
        if (OP_DEBUG) {
            Log.i("StatusBarSignalPolicy", " setLTEStatus:" + this.mLTEIconStates.toString());
        }
    }

    public static class LTEIconState {
        public String contentDescription;
        public int imsIndex = 0;
        public int resId;
        public String slot;
        public boolean visible;

        public LTEIconState(String str) {
            this.slot = str;
        }

        public boolean equals(Object obj) {
            if (obj == null || LTEIconState.class != obj.getClass() || !super.equals(obj)) {
                return false;
            }
            LTEIconState lTEIconState = (LTEIconState) obj;
            if (this.resId == lTEIconState.resId && this.slot == lTEIconState.slot && this.contentDescription == lTEIconState.contentDescription) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.resId), this.slot, this.contentDescription);
        }

        public String toString() {
            return "LTEIconState(slot=" + this.slot + ", visible=" + this.visible + ", resId=" + this.resId + ", imsIndex:" + this.imsIndex + ", contentDescription" + this.contentDescription + ")";
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setProvision(int i, int i2) {
        int[] iArr = this.mProvisionState;
        if (i < iArr.length) {
            iArr[i] = i2;
            MobileIconState stateByPhoneId = getStateByPhoneId(i);
            if (stateByPhoneId != null) {
                stateByPhoneId.showNoSim = isDataDisable(i) || stateByPhoneId.isAbsent();
                Log.i("StatusBarSignalPolicy", "setProvision slotId:" + i + " provision:" + i2 + " state.isAbsent():" + stateByPhoneId.isAbsent() + " state.showNoSim:" + stateByPhoneId.showNoSim);
                this.mIconController.setMobileIcons(this.mSlotMobile, MobileIconState.copyStates(this.mMobileStates));
            }
        }
    }

    private boolean isDataDisable(int i) {
        int[] iArr = this.mProvisionState;
        if (i < iArr.length && iArr[i] != 1) {
            return true;
        }
        return false;
    }

    private MobileIconState getStateByPhoneId(int i) {
        Iterator<MobileIconState> it = this.mMobileStates.iterator();
        while (it.hasNext()) {
            MobileIconState next = it.next();
            if (next.phoneId == i) {
                return next;
            }
        }
        Log.e("StatusBarSignalPolicy", "Unexpected slotId " + i);
        return null;
    }

    private void initLTEIcon() {
        this.mIconController.removeAllIconsForSlot("volte");
        this.mIconController.removeAllIconsForSlot("vowifi");
        this.mLTEIconStates.clear();
        LTEIconState lTEIconState = new LTEIconState("volte");
        lTEIconState.resId = C0006R$drawable.stat_sys_volte;
        lTEIconState.imsIndex = 0;
        LTEIconState lTEIconState2 = new LTEIconState("vowifi");
        lTEIconState2.resId = C0006R$drawable.stat_sys_vowifi;
        lTEIconState2.imsIndex = 2;
        this.mLTEIconStates.add(lTEIconState);
        this.mLTEIconStates.add(lTEIconState2);
        this.mIconController.setIcon("vowifi", C0006R$drawable.stat_sys_vowifi, null);
        this.mIconController.setIcon("volte", C0006R$drawable.stat_sys_volte, null);
        this.mIconController.setIconVisibility("volte", false);
        this.mIconController.setIconVisibility("vowifi", false);
    }

    private int getSlotProvisionStatus(int i, int i2) {
        if (this.mExtTelephony == null) {
            this.mExtTelephony = IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
        }
        try {
            i2 = this.mExtTelephony.getCurrentUiccCardProvisioningStatus(i);
        } catch (RemoteException e) {
            this.mExtTelephony = null;
            Log.e("StatusBarSignalPolicy", "Failed to get pref, slotId: " + i + " Exception: " + e);
        } catch (NullPointerException e2) {
            this.mExtTelephony = null;
            Log.e("StatusBarSignalPolicy", "Failed to get pref, slotId: " + i + " Exception: " + e2);
        }
        Log.d("StatusBarSignalPolicy", "getSlotProvisionStatus slotId: " + i + ", status = " + i2);
        return i2;
    }
}
