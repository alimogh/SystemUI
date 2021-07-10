package com.android.systemui.statusbar.policy;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class CallbackHandler extends Handler implements NetworkController.EmergencyListener, NetworkController.SignalCallback {
    private final ArrayList<NetworkController.EmergencyListener> mEmergencyListeners = new ArrayList<>();
    private final ArrayList<NetworkController.SignalCallback> mSignalCallbacks = new ArrayList<>();

    public CallbackHandler() {
        super(Looper.getMainLooper());
    }

    @VisibleForTesting
    CallbackHandler(Looper looper) {
        super(looper);
    }

    @Override // android.os.Handler
    public void handleMessage(Message message) {
        switch (message.what) {
            case 0:
                Iterator<NetworkController.EmergencyListener> it = this.mEmergencyListeners.iterator();
                while (it.hasNext()) {
                    it.next().setEmergencyCallsOnly(message.arg1 != 0);
                }
                return;
            case 1:
                Iterator<NetworkController.SignalCallback> it2 = this.mSignalCallbacks.iterator();
                while (it2.hasNext()) {
                    it2.next().setSubs((List) message.obj);
                }
                return;
            case 2:
                Iterator<NetworkController.SignalCallback> it3 = this.mSignalCallbacks.iterator();
                while (it3.hasNext()) {
                    it3.next().setNoSims(message.arg1 != 0, message.arg2 != 0);
                }
                return;
            case 3:
                Iterator<NetworkController.SignalCallback> it4 = this.mSignalCallbacks.iterator();
                while (it4.hasNext()) {
                    it4.next().setEthernetIndicators((NetworkController.IconState) message.obj);
                }
                return;
            case 4:
                Iterator<NetworkController.SignalCallback> it5 = this.mSignalCallbacks.iterator();
                while (it5.hasNext()) {
                    it5.next().setIsAirplaneMode((NetworkController.IconState) message.obj);
                }
                return;
            case 5:
                Iterator<NetworkController.SignalCallback> it6 = this.mSignalCallbacks.iterator();
                while (it6.hasNext()) {
                    it6.next().setMobileDataEnabled(message.arg1 != 0);
                }
                return;
            case 6:
                if (message.arg1 != 0) {
                    this.mEmergencyListeners.add((NetworkController.EmergencyListener) message.obj);
                    return;
                } else {
                    this.mEmergencyListeners.remove((NetworkController.EmergencyListener) message.obj);
                    return;
                }
            case 7:
                if (message.arg1 != 0) {
                    this.mSignalCallbacks.add((NetworkController.SignalCallback) message.obj);
                    return;
                } else {
                    this.mSignalCallbacks.remove((NetworkController.SignalCallback) message.obj);
                    return;
                }
            case 8:
                Iterator<NetworkController.SignalCallback> it7 = this.mSignalCallbacks.iterator();
                while (it7.hasNext()) {
                    it7.next().setLTEStatus((NetworkController.IconState[]) message.obj);
                }
                return;
            case 9:
                Iterator<NetworkController.SignalCallback> it8 = this.mSignalCallbacks.iterator();
                while (it8.hasNext()) {
                    it8.next().setProvision(message.arg1, message.arg2);
                }
                return;
            case 10:
                Iterator<NetworkController.SignalCallback> it9 = this.mSignalCallbacks.iterator();
                while (it9.hasNext()) {
                    it9.next().setVirtualSimstate((int[]) message.obj);
                }
                return;
            case 11:
                Iterator<NetworkController.SignalCallback> it10 = this.mSignalCallbacks.iterator();
                while (it10.hasNext()) {
                    it10.next().setHasAnySimReady(((Boolean) message.obj).booleanValue());
                }
                return;
            case 12:
                Iterator<NetworkController.SignalCallback> it11 = this.mSignalCallbacks.iterator();
                while (it11.hasNext()) {
                    it11.next().setTetherError((Intent) message.obj);
                }
                return;
            default:
                return;
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
        post(new Runnable(z, iconState, iconState2, z2, z3, str, z4, str2) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$CallbackHandler$BL9Oe1XlhjuRCIkE3XITv_5klDM
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ NetworkController.IconState f$2;
            public final /* synthetic */ NetworkController.IconState f$3;
            public final /* synthetic */ boolean f$4;
            public final /* synthetic */ boolean f$5;
            public final /* synthetic */ String f$6;
            public final /* synthetic */ boolean f$7;
            public final /* synthetic */ String f$8;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
                this.f$8 = r9;
            }

            @Override // java.lang.Runnable
            public final void run() {
                CallbackHandler.this.lambda$setWifiIndicators$0$CallbackHandler(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setWifiIndicators$0 */
    public /* synthetic */ void lambda$setWifiIndicators$0$CallbackHandler(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
        Iterator<NetworkController.SignalCallback> it = this.mSignalCallbacks.iterator();
        while (it.hasNext()) {
            it.next().setWifiIndicators(z, iconState, iconState2, z2, z3, str, z4, str2);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataIndicators(final NetworkController.IconState iconState, final NetworkController.IconState iconState2, final int i, final int i2, final boolean z, final boolean z2, final int i3, final int[] iArr, final int[] iArr2, final CharSequence charSequence, final CharSequence charSequence2, final CharSequence charSequence3, final boolean z3, final int i4, final boolean z4, final boolean z5) {
        post(new Runnable() { // from class: com.android.systemui.statusbar.policy.CallbackHandler.1
            @Override // java.lang.Runnable
            public void run() {
                Iterator it = CallbackHandler.this.mSignalCallbacks.iterator();
                while (it.hasNext()) {
                    ((NetworkController.SignalCallback) it.next()).setMobileDataIndicators(iconState, iconState2, i, i2, z, z2, i3, iArr, iArr2, charSequence, charSequence2, charSequence3, z3, i4, z4, z5);
                }
            }
        });
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setSubs(List<SubscriptionInfo> list) {
        obtainMessage(1, list).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setNoSims(boolean z, boolean z2) {
        obtainMessage(2, z ? 1 : 0, z2 ? 1 : 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataEnabled(boolean z) {
        obtainMessage(5, z ? 1 : 0, 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.EmergencyListener
    public void setEmergencyCallsOnly(boolean z) {
        obtainMessage(0, z ? 1 : 0, 0).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setEthernetIndicators(NetworkController.IconState iconState) {
        obtainMessage(3, iconState).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        obtainMessage(4, iconState).sendToTarget();
    }

    public void setListening(NetworkController.SignalCallback signalCallback, boolean z) {
        obtainMessage(7, z ? 1 : 0, 0, signalCallback).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setLTEStatus(NetworkController.IconState[] iconStateArr) {
        obtainMessage(8, iconStateArr).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setProvision(int i, int i2) {
        obtainMessage(9, i, i2).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setVirtualSimstate(int[] iArr) {
        obtainMessage(10, 0, 0, iArr).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setHasAnySimReady(boolean z) {
        obtainMessage(11, 0, 0, Boolean.valueOf(z)).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setTetherError(Intent intent) {
        obtainMessage(12, 0, 0, intent).sendToTarget();
    }
}
