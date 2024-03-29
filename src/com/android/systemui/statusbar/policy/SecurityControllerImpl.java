package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.net.ConnectivityManager;
import android.net.IConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.systemui.C0015R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.SecurityController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executor;
public class SecurityControllerImpl extends CurrentUserTracker implements SecurityController {
    private static final boolean DEBUG = Log.isLoggable("SecurityController", 3);
    private static final NetworkRequest REQUEST = new NetworkRequest.Builder().removeCapability(15).removeCapability(13).removeCapability(14).setUids(null).build();
    private final Executor mBgExecutor;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.policy.SecurityControllerImpl.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int intExtra;
            if ("android.security.action.TRUST_STORE_CHANGED".equals(intent.getAction())) {
                SecurityControllerImpl.this.refreshCACerts(getSendingUserId());
            } else if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction()) && (intExtra = intent.getIntExtra("android.intent.extra.user_handle", -10000)) != -10000) {
                SecurityControllerImpl.this.refreshCACerts(intExtra);
            }
        }
    };
    @GuardedBy({"mCallbacks"})
    private final ArrayList<SecurityController.SecurityControllerCallback> mCallbacks = new ArrayList<>();
    private final ConnectivityManager mConnectivityManager;
    private final IConnectivityManager mConnectivityManagerService;
    private final Context mContext;
    private int mCurrentUserId;
    private SparseArray<VpnConfig> mCurrentVpns = new SparseArray<>();
    private final DevicePolicyManager mDevicePolicyManager;
    private ArrayMap<Integer, Boolean> mHasCACerts = new ArrayMap<>();
    private final ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() { // from class: com.android.systemui.statusbar.policy.SecurityControllerImpl.1
        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            if (SecurityControllerImpl.DEBUG) {
                Log.d("SecurityController", "onAvailable " + network.netId);
            }
            SecurityControllerImpl.this.updateState();
            SecurityControllerImpl.this.fireCallbacks();
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            if (SecurityControllerImpl.DEBUG) {
                Log.d("SecurityController", "onLost " + network.netId);
            }
            SecurityControllerImpl.this.updateState();
            SecurityControllerImpl.this.fireCallbacks();
        }
    };
    private final PackageManager mPackageManager;
    private final UserManager mUserManager;
    private int mVpnUserId;

    public SecurityControllerImpl(Context context, Handler handler, BroadcastDispatcher broadcastDispatcher, Executor executor) {
        super(broadcastDispatcher);
        this.mContext = context;
        this.mDevicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mConnectivityManagerService = IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity"));
        this.mPackageManager = context.getPackageManager();
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mBgExecutor = executor;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.security.action.TRUST_STORE_CHANGED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        broadcastDispatcher.registerReceiverWithHandler(this.mBroadcastReceiver, intentFilter, handler, UserHandle.ALL);
        this.mConnectivityManager.registerNetworkCallback(REQUEST, this.mNetworkCallback);
        onUserSwitched(ActivityManager.getCurrentUser());
        startTracking();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("SecurityController state:");
        printWriter.print("  mCurrentVpns={");
        for (int i = 0; i < this.mCurrentVpns.size(); i++) {
            if (i > 0) {
                printWriter.print(", ");
            }
            printWriter.print(this.mCurrentVpns.keyAt(i));
            printWriter.print('=');
            printWriter.print(this.mCurrentVpns.valueAt(i).user);
        }
        printWriter.println("}");
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isDeviceManaged() {
        return this.mDevicePolicyManager.isDeviceManaged();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public CharSequence getDeviceOwnerOrganizationName() {
        return this.mDevicePolicyManager.getDeviceOwnerOrganizationName();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public CharSequence getWorkProfileOrganizationName() {
        int workProfileUserId = getWorkProfileUserId(this.mCurrentUserId);
        if (workProfileUserId == -10000) {
            return null;
        }
        return this.mDevicePolicyManager.getOrganizationNameForUser(workProfileUserId);
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public String getPrimaryVpnName() {
        VpnConfig vpnConfig = this.mCurrentVpns.get(this.mVpnUserId);
        if (vpnConfig != null) {
            return getNameForVpnConfig(vpnConfig, new UserHandle(this.mVpnUserId));
        }
        return null;
    }

    private int getWorkProfileUserId(int i) {
        for (UserInfo userInfo : this.mUserManager.getProfiles(i)) {
            if (userInfo.isManagedProfile()) {
                return userInfo.id;
            }
        }
        return -10000;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean hasWorkProfile() {
        return getWorkProfileUserId(this.mCurrentUserId) != -10000;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isProfileOwnerOfOrganizationOwnedDevice() {
        return this.mDevicePolicyManager.isOrganizationOwnedDeviceWithManagedProfile();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public String getWorkProfileVpnName() {
        VpnConfig vpnConfig;
        int workProfileUserId = getWorkProfileUserId(this.mVpnUserId);
        if (workProfileUserId == -10000 || (vpnConfig = this.mCurrentVpns.get(workProfileUserId)) == null) {
            return null;
        }
        return getNameForVpnConfig(vpnConfig, UserHandle.of(workProfileUserId));
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isNetworkLoggingEnabled() {
        return this.mDevicePolicyManager.isNetworkLoggingEnabled(null);
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isVpnEnabled() {
        for (int i : this.mUserManager.getProfileIdsWithDisabled(this.mVpnUserId)) {
            if (this.mCurrentVpns.get(i) != null) {
                return true;
            }
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean isVpnBranded() {
        String packageNameForVpnConfig;
        VpnConfig vpnConfig = this.mCurrentVpns.get(this.mVpnUserId);
        if (vpnConfig == null || (packageNameForVpnConfig = getPackageNameForVpnConfig(vpnConfig)) == null) {
            return false;
        }
        return isVpnPackageBranded(packageNameForVpnConfig);
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean hasCACertInCurrentUser() {
        Boolean bool = this.mHasCACerts.get(Integer.valueOf(this.mCurrentUserId));
        return bool != null && bool.booleanValue();
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController
    public boolean hasCACertInWorkProfile() {
        Boolean bool;
        int workProfileUserId = getWorkProfileUserId(this.mCurrentUserId);
        if (workProfileUserId == -10000 || (bool = this.mHasCACerts.get(Integer.valueOf(workProfileUserId))) == null || !bool.booleanValue()) {
            return false;
        }
        return true;
    }

    public void removeCallback(SecurityController.SecurityControllerCallback securityControllerCallback) {
        synchronized (this.mCallbacks) {
            if (securityControllerCallback != null) {
                if (DEBUG) {
                    Log.d("SecurityController", "removeCallback " + securityControllerCallback);
                }
                this.mCallbacks.remove(securityControllerCallback);
            }
        }
    }

    public void addCallback(SecurityController.SecurityControllerCallback securityControllerCallback) {
        synchronized (this.mCallbacks) {
            if (securityControllerCallback != null) {
                if (!this.mCallbacks.contains(securityControllerCallback)) {
                    if (DEBUG) {
                        Log.d("SecurityController", "addCallback " + securityControllerCallback);
                    }
                    this.mCallbacks.add(securityControllerCallback);
                }
            }
        }
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        this.mCurrentUserId = i;
        UserInfo userInfo = this.mUserManager.getUserInfo(i);
        if (userInfo.isRestricted()) {
            this.mVpnUserId = userInfo.restrictedProfileParentId;
        } else {
            this.mVpnUserId = this.mCurrentUserId;
        }
        fireCallbacks();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshCACerts(int i) {
        this.mBgExecutor.execute(new Runnable(i) { // from class: com.android.systemui.statusbar.policy.-$$Lambda$SecurityControllerImpl$gIe4Ly5u4oeRcLYZFLgXwmhKZ40
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                SecurityControllerImpl.this.lambda$refreshCACerts$0$SecurityControllerImpl(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0066, code lost:
        r3 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0067, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x007a, code lost:
        android.util.Log.d("SecurityController", "Refreshing CA Certs " + r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0090, code lost:
        r0 = r7.mHasCACerts;
        r1 = r3.first;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0066 A[ExcHandler: RemoteException | AssertionError | InterruptedException (e java.lang.Throwable), Splitter:B:24:0x0060] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A[RETURN, SYNTHETIC] */
    /* renamed from: lambda$refreshCACerts$0 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$refreshCACerts$0$SecurityControllerImpl(int r8) {
        /*
            r7 = this;
            java.lang.String r0 = "Refreshing CA Certs "
            java.lang.String r1 = "SecurityController"
            r2 = 0
            android.content.Context r3 = r7.mContext     // Catch:{ all -> 0x0064 }
            android.os.UserHandle r4 = android.os.UserHandle.of(r8)     // Catch:{ all -> 0x0064 }
            android.security.KeyChain$KeyChainConnection r3 = android.security.KeyChain.bindAsUser(r3, r4)     // Catch:{ all -> 0x0064 }
            android.security.IKeyChainService r4 = r3.getService()     // Catch:{ all -> 0x0058 }
            android.content.pm.StringParceledListSlice r4 = r4.getUserCaAliases()     // Catch:{ all -> 0x0058 }
            java.util.List r4 = r4.getList()     // Catch:{ all -> 0x0058 }
            boolean r4 = r4.isEmpty()     // Catch:{ all -> 0x0058 }
            if (r4 != 0) goto L_0x0023
            r4 = 1
            goto L_0x0024
        L_0x0023:
            r4 = 0
        L_0x0024:
            android.util.Pair r5 = new android.util.Pair     // Catch:{ all -> 0x0058 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x0058 }
            java.lang.Boolean r4 = java.lang.Boolean.valueOf(r4)     // Catch:{ all -> 0x0058 }
            r5.<init>(r6, r4)     // Catch:{ all -> 0x0058 }
            if (r3 == 0) goto L_0x0039
            r3.close()     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0037 }
            goto L_0x0039
        L_0x0037:
            r3 = move-exception
            goto L_0x0068
        L_0x0039:
            boolean r8 = com.android.systemui.statusbar.policy.SecurityControllerImpl.DEBUG
            if (r8 == 0) goto L_0x004f
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r0)
            r8.append(r5)
            java.lang.String r8 = r8.toString()
            android.util.Log.d(r1, r8)
        L_0x004f:
            java.lang.Object r8 = r5.second
            if (r8 == 0) goto L_0x009e
            android.util.ArrayMap<java.lang.Integer, java.lang.Boolean> r0 = r7.mHasCACerts
            java.lang.Object r1 = r5.first
            goto L_0x0094
        L_0x0058:
            r4 = move-exception
            if (r3 == 0) goto L_0x0063
            r3.close()     // Catch:{ all -> 0x005f }
            goto L_0x0063
        L_0x005f:
            r3 = move-exception
            r4.addSuppressed(r3)     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0066, RemoteException | AssertionError | InterruptedException -> 0x0066 }
        L_0x0063:
            throw r4     // Catch:{ RemoteException | AssertionError | InterruptedException -> 0x0066, RemoteException | AssertionError | InterruptedException -> 0x0066 }
        L_0x0064:
            r8 = move-exception
            goto L_0x00a1
        L_0x0066:
            r3 = move-exception
            r5 = r2
        L_0x0068:
            java.lang.String r4 = "failed to get CA certs"
            android.util.Log.i(r1, r4, r3)     // Catch:{ all -> 0x009f }
            android.util.Pair r3 = new android.util.Pair     // Catch:{ all -> 0x009f }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x009f }
            r3.<init>(r8, r2)     // Catch:{ all -> 0x009f }
            boolean r8 = com.android.systemui.statusbar.policy.SecurityControllerImpl.DEBUG
            if (r8 == 0) goto L_0x008c
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r0)
            r8.append(r3)
            java.lang.String r8 = r8.toString()
            android.util.Log.d(r1, r8)
        L_0x008c:
            java.lang.Object r8 = r3.second
            if (r8 == 0) goto L_0x009e
            android.util.ArrayMap<java.lang.Integer, java.lang.Boolean> r0 = r7.mHasCACerts
            java.lang.Object r1 = r3.first
        L_0x0094:
            java.lang.Integer r1 = (java.lang.Integer) r1
            java.lang.Boolean r8 = (java.lang.Boolean) r8
            r0.put(r1, r8)
            r7.fireCallbacks()
        L_0x009e:
            return
        L_0x009f:
            r8 = move-exception
            r2 = r5
        L_0x00a1:
            boolean r3 = com.android.systemui.statusbar.policy.SecurityControllerImpl.DEBUG
            if (r3 == 0) goto L_0x00b7
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r0)
            r3.append(r2)
            java.lang.String r0 = r3.toString()
            android.util.Log.d(r1, r0)
        L_0x00b7:
            if (r2 == 0) goto L_0x00cb
            java.lang.Object r0 = r2.second
            if (r0 == 0) goto L_0x00cb
            android.util.ArrayMap<java.lang.Integer, java.lang.Boolean> r1 = r7.mHasCACerts
            java.lang.Object r2 = r2.first
            java.lang.Integer r2 = (java.lang.Integer) r2
            java.lang.Boolean r0 = (java.lang.Boolean) r0
            r1.put(r2, r0)
            r7.fireCallbacks()
        L_0x00cb:
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.SecurityControllerImpl.lambda$refreshCACerts$0$SecurityControllerImpl(int):void");
    }

    private String getNameForVpnConfig(VpnConfig vpnConfig, UserHandle userHandle) {
        if (vpnConfig.legacy) {
            return this.mContext.getString(C0015R$string.legacy_vpn_name);
        }
        String str = vpnConfig.user;
        try {
            return VpnConfig.getVpnLabel(this.mContext.createPackageContextAsUser(this.mContext.getPackageName(), 0, userHandle), str).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SecurityController", "Package " + str + " is not present", e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireCallbacks() {
        synchronized (this.mCallbacks) {
            Iterator<SecurityController.SecurityControllerCallback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onStateChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateState() {
        SparseArray<VpnConfig> sparseArray = new SparseArray<>();
        try {
            for (UserInfo userInfo : this.mUserManager.getUsers()) {
                VpnConfig vpnConfig = this.mConnectivityManagerService.getVpnConfig(userInfo.id);
                if (vpnConfig != null) {
                    if (vpnConfig.legacy) {
                        LegacyVpnInfo legacyVpnInfo = this.mConnectivityManagerService.getLegacyVpnInfo(userInfo.id);
                        if (legacyVpnInfo != null) {
                            if (legacyVpnInfo.state != 3) {
                            }
                        }
                    }
                    sparseArray.put(userInfo.id, vpnConfig);
                }
            }
            this.mCurrentVpns = sparseArray;
        } catch (RemoteException e) {
            Log.e("SecurityController", "Unable to list active VPNs", e);
        }
    }

    private String getPackageNameForVpnConfig(VpnConfig vpnConfig) {
        if (vpnConfig.legacy) {
            return null;
        }
        return vpnConfig.user;
    }

    private boolean isVpnPackageBranded(String str) {
        try {
            ApplicationInfo applicationInfo = this.mPackageManager.getApplicationInfo(str, 128);
            if (!(applicationInfo == null || applicationInfo.metaData == null)) {
                if (applicationInfo.isSystemApp()) {
                    return applicationInfo.metaData.getBoolean("com.android.systemui.IS_BRANDED", false);
                }
            }
        } catch (PackageManager.NameNotFoundException unused) {
        }
        return false;
    }
}
