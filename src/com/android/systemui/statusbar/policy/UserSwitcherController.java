package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import androidx.appcompat.R$styleable;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.UiEventLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.R$string;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.drawable.UserIcons;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.GuestResumeSessionReceiver;
import com.android.systemui.Prefs;
import com.android.systemui.SystemUISecondaryUserService;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.QSUserSwitcherEvent;
import com.android.systemui.qs.tiles.UserDetailView;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.oneplus.systemui.statusbar.phone.OpSystemUIDialog;
import com.oneplus.util.OpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
public class UserSwitcherController implements Dumpable {
    private final ActivityStarter mActivityStarter;
    private final ArrayList<WeakReference<BaseUserAdapter>> mAdapters = new ArrayList<>();
    private Dialog mAddUserDialog;
    private boolean mAddUsersWhenLocked;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final KeyguardStateController.Callback mCallback = new KeyguardStateController.Callback() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.8
        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onKeyguardShowingChanged() {
            if (!UserSwitcherController.this.mKeyguardStateController.isShowing()) {
                UserSwitcherController userSwitcherController = UserSwitcherController.this;
                userSwitcherController.mHandler.post(new Runnable() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$UserSwitcherController$8$A3r6icx46POmpjCAt7rArXBXF0c
                    @Override // java.lang.Runnable
                    public final void run() {
                        UserSwitcherController.this.notifyAdapters();
                    }
                });
                return;
            }
            UserSwitcherController.this.notifyAdapters();
        }
    };
    protected final Context mContext;
    private Dialog mExitGuestDialog;
    private SparseBooleanArray mForcePictureLoadForUserId = new SparseBooleanArray(2);
    private final GuestResumeSessionReceiver mGuestResumeSessionReceiver = new GuestResumeSessionReceiver();
    protected final Handler mHandler;
    private final KeyguardStateController mKeyguardStateController;
    private int mLastNonGuestUser = 0;
    private boolean mPauseRefreshUsers;
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.3
        private int mCallState;

        @Override // android.telephony.PhoneStateListener
        public void onCallStateChanged(int i, String str) {
            if (this.mCallState != i) {
                this.mCallState = i;
                UserSwitcherController.this.refreshUsers(-10000);
            }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.4
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            int i = -10000;
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                if (UserSwitcherController.this.mExitGuestDialog != null && UserSwitcherController.this.mExitGuestDialog.isShowing()) {
                    UserSwitcherController.this.mExitGuestDialog.cancel();
                    UserSwitcherController.this.mExitGuestDialog = null;
                }
                int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -1);
                UserInfo userInfo = UserSwitcherController.this.mUserManager.getUserInfo(intExtra);
                int size = UserSwitcherController.this.mUsers.size();
                int i2 = 0;
                while (i2 < size) {
                    UserRecord userRecord = (UserRecord) UserSwitcherController.this.mUsers.get(i2);
                    UserInfo userInfo2 = userRecord.info;
                    if (userInfo2 != null) {
                        boolean z2 = userInfo2.id == intExtra;
                        if (userRecord.isCurrent != z2) {
                            UserSwitcherController.this.mUsers.set(i2, userRecord.copyWithIsCurrent(z2));
                        }
                        if (z2 && !userRecord.isGuest) {
                            UserSwitcherController.this.mLastNonGuestUser = userRecord.info.id;
                        }
                        if ((userInfo == null || !userInfo.isAdmin()) && userRecord.isRestricted) {
                            UserSwitcherController.this.mUsers.remove(i2);
                            i2--;
                        }
                    }
                    i2++;
                }
                UserSwitcherController.this.notifyAdapters();
                if (UserSwitcherController.this.mSecondaryUser != -10000) {
                    context.stopServiceAsUser(UserSwitcherController.this.mSecondaryUserServiceIntent, UserHandle.of(UserSwitcherController.this.mSecondaryUser));
                    UserSwitcherController.this.mSecondaryUser = -10000;
                }
                if (!(userInfo == null || userInfo.id == 0)) {
                    context.startServiceAsUser(UserSwitcherController.this.mSecondaryUserServiceIntent, UserHandle.of(userInfo.id));
                    UserSwitcherController.this.mSecondaryUser = userInfo.id;
                }
            } else {
                if ("android.intent.action.USER_INFO_CHANGED".equals(intent.getAction())) {
                    i = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                } else if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction()) && intent.getIntExtra("android.intent.extra.user_handle", -10000) != 0) {
                    return;
                }
                z = false;
            }
            UserSwitcherController.this.refreshUsers(i);
            if (z) {
                UserSwitcherController.this.mUnpauseRefreshUsers.run();
            }
        }
    };
    private boolean mResumeUserOnGuestLogout = true;
    private int mSecondaryUser = -10000;
    private Intent mSecondaryUserServiceIntent;
    private final ContentObserver mSettingsObserver = new ContentObserver(new Handler()) { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.6
        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            UserSwitcherController userSwitcherController = UserSwitcherController.this;
            userSwitcherController.mSimpleUserSwitcher = userSwitcherController.shouldUseSimpleUserSwitcher();
            UserSwitcherController userSwitcherController2 = UserSwitcherController.this;
            boolean z2 = false;
            if (Settings.Global.getInt(userSwitcherController2.mContext.getContentResolver(), "add_users_when_locked", 0) != 0) {
                z2 = true;
            }
            userSwitcherController2.mAddUsersWhenLocked = z2;
            UserSwitcherController.this.refreshUsers(-10000);
        }
    };
    private boolean mSimpleUserSwitcher;
    private final UiEventLogger mUiEventLogger;
    private final Runnable mUnpauseRefreshUsers = new Runnable() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.5
        @Override // java.lang.Runnable
        public void run() {
            UserSwitcherController.this.mHandler.removeCallbacks(this);
            UserSwitcherController.this.mPauseRefreshUsers = false;
            UserSwitcherController.this.refreshUsers(-10000);
        }
    };
    protected final UserManager mUserManager;
    private ArrayList<UserRecord> mUsers = new ArrayList<>();
    public final DetailAdapter userDetailAdapter = new DetailAdapter() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.7
        private final Intent USER_SETTINGS_INTENT = new Intent("android.settings.USER_SETTINGS");

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getMetricsCategory() {
            return R$styleable.AppCompatTheme_windowMinWidthMinor;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Boolean getToggleState() {
            return null;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public void setToggleState(boolean z) {
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public CharSequence getTitle() {
            return UserSwitcherController.this.mContext.getString(C0015R$string.quick_settings_user_title);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            UserDetailView userDetailView;
            if (!(view instanceof UserDetailView)) {
                userDetailView = UserDetailView.inflate(context, viewGroup, false);
                UserSwitcherController userSwitcherController = UserSwitcherController.this;
                userDetailView.createAndSetAdapter(userSwitcherController, userSwitcherController.mUiEventLogger);
            } else {
                userDetailView = (UserDetailView) view;
            }
            userDetailView.refreshAdapter();
            return userDetailView;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Intent getSettingsIntent() {
            return this.USER_SETTINGS_INTENT;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public UiEventLogger.UiEventEnum openDetailEvent() {
            return QSUserSwitcherEvent.QS_USER_DETAIL_OPEN;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public UiEventLogger.UiEventEnum closeDetailEvent() {
            return QSUserSwitcherEvent.QS_USER_DETAIL_CLOSE;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public UiEventLogger.UiEventEnum moreSettingsEvent() {
            return QSUserSwitcherEvent.QS_USER_MORE_SETTINGS;
        }
    };

    public UserSwitcherController(Context context, KeyguardStateController keyguardStateController, Handler handler, ActivityStarter activityStarter, BroadcastDispatcher broadcastDispatcher, UiEventLogger uiEventLogger) {
        this.mContext = context;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mUiEventLogger = uiEventLogger;
        if (!UserManager.isGuestUserEphemeral()) {
            this.mGuestResumeSessionReceiver.register(this.mBroadcastDispatcher);
        }
        this.mKeyguardStateController = keyguardStateController;
        this.mHandler = handler;
        this.mActivityStarter = activityStarter;
        this.mUserManager = UserManager.get(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_INFO_CHANGED");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_STOPPED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mBroadcastDispatcher.registerReceiver(this.mReceiver, intentFilter, null, UserHandle.SYSTEM);
        this.mSimpleUserSwitcher = shouldUseSimpleUserSwitcher();
        this.mSecondaryUserServiceIntent = new Intent(context, SystemUISecondaryUserService.class);
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.SYSTEM, new IntentFilter(), "com.android.systemui.permission.SELF", null);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("lockscreenSimpleUserSwitcher"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("add_users_when_locked"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("allow_user_switching_when_system_user_locked"), true, this.mSettingsObserver);
        this.mSettingsObserver.onChange(false);
        keyguardStateController.addCallback(this.mCallback);
        listenForCallState();
        refreshUsers(-10000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshUsers(int i) {
        UserInfo userInfo;
        if (i != -10000) {
            this.mForcePictureLoadForUserId.put(i, true);
        }
        if (!this.mPauseRefreshUsers) {
            boolean z = this.mForcePictureLoadForUserId.get(-1);
            SparseArray sparseArray = new SparseArray(this.mUsers.size());
            int size = this.mUsers.size();
            for (int i2 = 0; i2 < size; i2++) {
                UserRecord userRecord = this.mUsers.get(i2);
                if (!(userRecord == null || userRecord.picture == null || (userInfo = userRecord.info) == null || z || this.mForcePictureLoadForUserId.get(userInfo.id))) {
                    sparseArray.put(userRecord.info.id, userRecord.picture);
                }
            }
            this.mForcePictureLoadForUserId.clear();
            final boolean z2 = this.mAddUsersWhenLocked;
            new AsyncTask<SparseArray<Bitmap>, Void, ArrayList<UserRecord>>() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.1
                /* access modifiers changed from: protected */
                public ArrayList<UserRecord> doInBackground(SparseArray<Bitmap>... sparseArrayArr) {
                    boolean z3 = false;
                    SparseArray<Bitmap> sparseArray2 = sparseArrayArr[0];
                    List<UserInfo> users = UserSwitcherController.this.mUserManager.getUsers(true);
                    UserRecord userRecord2 = null;
                    if (users == null) {
                        return null;
                    }
                    ArrayList<UserRecord> arrayList = new ArrayList<>(users.size());
                    int currentUser = ActivityManager.getCurrentUser();
                    boolean z4 = UserSwitcherController.this.mUserManager.getUserSwitchability(UserHandle.of(ActivityManager.getCurrentUser())) == 0;
                    UserInfo userInfo2 = null;
                    for (UserInfo userInfo3 : users) {
                        boolean z5 = currentUser == userInfo3.id;
                        UserInfo userInfo4 = z5 ? userInfo3 : userInfo2;
                        boolean z6 = z4 || z5;
                        if (userInfo3.isEnabled()) {
                            if (userInfo3.isGuest()) {
                                userRecord2 = new UserRecord(userInfo3, null, true, z5, false, false, z4);
                            } else if (userInfo3.supportsSwitchToByUser()) {
                                Bitmap bitmap = sparseArray2.get(userInfo3.id);
                                if (bitmap == null && (bitmap = UserSwitcherController.this.mUserManager.getUserIcon(userInfo3.id)) != null) {
                                    int dimensionPixelSize = UserSwitcherController.this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.max_avatar_size);
                                    bitmap = Bitmap.createScaledBitmap(bitmap, dimensionPixelSize, dimensionPixelSize, true);
                                }
                                arrayList.add(new UserRecord(userInfo3, bitmap, false, z5, false, false, z6));
                            }
                        }
                        userInfo2 = userInfo4;
                    }
                    if (arrayList.size() > 1 || userRecord2 != null) {
                        Prefs.putBoolean(UserSwitcherController.this.mContext, "HasSeenMultiUser", true);
                    } else {
                        Prefs.putBoolean(UserSwitcherController.this.mContext, "HasSeenMultiUser", false);
                    }
                    boolean z7 = !UserSwitcherController.this.mUserManager.hasBaseUserRestriction("no_add_user", UserHandle.SYSTEM);
                    boolean z8 = userInfo2 != null && (userInfo2.isAdmin() || userInfo2.id == 0) && z7;
                    boolean z9 = z7 && z2;
                    boolean z10 = (z8 || z9) && userRecord2 == null;
                    if ((z8 || z9) && UserSwitcherController.this.mUserManager.canAddMoreUsers()) {
                        z3 = true;
                    }
                    boolean z11 = !z2;
                    if (userRecord2 != null) {
                        arrayList.add(userRecord2);
                    } else if (z10) {
                        UserRecord userRecord3 = new UserRecord(null, null, true, false, false, z11, z4);
                        UserSwitcherController.this.checkIfAddUserDisallowedByAdminOnly(userRecord3);
                        arrayList.add(userRecord3);
                    }
                    if (z3) {
                        UserRecord userRecord4 = new UserRecord(null, null, false, false, true, z11, z4);
                        UserSwitcherController.this.checkIfAddUserDisallowedByAdminOnly(userRecord4);
                        arrayList.add(userRecord4);
                    }
                    return arrayList;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(ArrayList<UserRecord> arrayList) {
                    if (arrayList != null) {
                        UserSwitcherController.this.mUsers = arrayList;
                        UserSwitcherController.this.notifyAdapters();
                    }
                }
            }.execute(sparseArray);
        }
    }

    private void pauseRefreshUsers() {
        if (!this.mPauseRefreshUsers) {
            this.mHandler.postDelayed(this.mUnpauseRefreshUsers, 3000);
            this.mPauseRefreshUsers = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void notifyAdapters() {
        for (int size = this.mAdapters.size() - 1; size >= 0; size--) {
            BaseUserAdapter baseUserAdapter = this.mAdapters.get(size).get();
            if (baseUserAdapter != null) {
                baseUserAdapter.notifyDataSetChanged();
            } else {
                this.mAdapters.remove(size);
            }
        }
    }

    public boolean isSimpleUserSwitcher() {
        return this.mSimpleUserSwitcher;
    }

    public boolean useFullscreenUserSwitcher() {
        int intValue = ((Integer) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.statusbar.policy.-$$Lambda$UserSwitcherController$gJeQLk7uUPWe8l2LAzLToqB-eJo
            @Override // java.util.function.Supplier
            public final Object get() {
                return UserSwitcherController.this.lambda$useFullscreenUserSwitcher$0$UserSwitcherController();
            }
        })).intValue();
        if (intValue != -1) {
            return intValue != 0;
        }
        return this.mContext.getResources().getBoolean(C0003R$bool.config_enableFullscreenUserSwitcher);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$useFullscreenUserSwitcher$0 */
    public /* synthetic */ Integer lambda$useFullscreenUserSwitcher$0$UserSwitcherController() {
        return Integer.valueOf(Settings.System.getInt(this.mContext.getContentResolver(), "enable_fullscreen_user_switcher", -1));
    }

    public void switchTo(UserRecord userRecord) {
        UserInfo userInfo;
        if (userRecord.isGuest && userRecord.info == null) {
            new Thread() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.2
                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    Log.d("UserSwitcherController", "switchTo:createGuest:START");
                    try {
                        UserInfo createGuest = UserSwitcherController.this.mUserManager.createGuest(UserSwitcherController.this.mContext, UserSwitcherController.this.mContext.getString(R$string.guest_nickname));
                        Log.d("UserSwitcherController", "switchTo:createGuest:END");
                        if (createGuest != null) {
                            UserSwitcherController.this.switchToUserId(createGuest.id);
                        }
                    } catch (UserManager.UserOperationException e) {
                        Log.e("UserSwitcherController", "Couldn't create guest user", e);
                    }
                }
            }.start();
        } else if (userRecord.isAddUser) {
            showAddUserDialog();
        } else {
            int i = userRecord.info.id;
            int currentUser = ActivityManager.getCurrentUser();
            if (currentUser == i) {
                if (userRecord.isGuest) {
                    showExitGuestDialog(i);
                }
            } else if (!UserManager.isGuestUserEphemeral() || (userInfo = this.mUserManager.getUserInfo(currentUser)) == null || !userInfo.isGuest()) {
                switchToUserId(i);
            } else {
                showExitGuestDialog(currentUser, userRecord.resolveId());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void switchToUserId(int i) {
        Log.d("UserSwitcherController", "switchToUserId START");
        try {
            if (OpUtils.isCustomFingerprint()) {
                ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).earlyNotifySwitchingUser();
            }
            pauseRefreshUsers();
            ActivityManager.getService().switchUser(i);
        } catch (RemoteException e) {
            Log.e("UserSwitcherController", "Couldn't switch user.", e);
        }
        Log.d("UserSwitcherController", "switchToUserId END");
    }

    private void showExitGuestDialog(int i) {
        int i2;
        UserInfo userInfo;
        showExitGuestDialog(i, (!this.mResumeUserOnGuestLogout || (i2 = this.mLastNonGuestUser) == 0 || (userInfo = this.mUserManager.getUserInfo(i2)) == null || !userInfo.isEnabled() || !userInfo.supportsSwitchToByUser()) ? 0 : userInfo.id);
    }

    /* access modifiers changed from: protected */
    public void showExitGuestDialog(int i, int i2) {
        Dialog dialog = this.mExitGuestDialog;
        if (dialog != null && dialog.isShowing()) {
            this.mExitGuestDialog.cancel();
        }
        ExitGuestDialog exitGuestDialog = new ExitGuestDialog(this.mContext, i, i2);
        this.mExitGuestDialog = exitGuestDialog;
        exitGuestDialog.show();
    }

    public void showAddUserDialog() {
        Dialog dialog = this.mAddUserDialog;
        if (dialog != null && dialog.isShowing()) {
            this.mAddUserDialog.cancel();
        }
        AddUserDialog addUserDialog = new AddUserDialog(this.mContext);
        this.mAddUserDialog = addUserDialog;
        addUserDialog.show();
    }

    /* access modifiers changed from: protected */
    public void exitGuest(int i, int i2) {
        switchToUserId(i2);
        this.mUserManager.removeUser(i);
    }

    private void listenForCallState() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 32);
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("UserSwitcherController state:");
        printWriter.println("  mLastNonGuestUser=" + this.mLastNonGuestUser);
        printWriter.print("  mUsers.size=");
        printWriter.println(this.mUsers.size());
        for (int i = 0; i < this.mUsers.size(); i++) {
            printWriter.print("    ");
            printWriter.println(this.mUsers.get(i).toString());
        }
        printWriter.println("mSimpleUserSwitcher=" + this.mSimpleUserSwitcher);
    }

    public String getCurrentUserName(Context context) {
        UserRecord userRecord;
        UserInfo userInfo;
        if (this.mUsers.isEmpty() || (userRecord = this.mUsers.get(0)) == null || (userInfo = userRecord.info) == null) {
            return null;
        }
        if (userRecord.isGuest) {
            return context.getString(R$string.guest_nickname);
        }
        return userInfo.name;
    }

    public void onDensityOrFontScaleChanged() {
        refreshUsers(-1);
    }

    @VisibleForTesting
    public void addAdapter(WeakReference<BaseUserAdapter> weakReference) {
        this.mAdapters.add(weakReference);
    }

    @VisibleForTesting
    public ArrayList<UserRecord> getUsers() {
        return this.mUsers;
    }

    public static abstract class BaseUserAdapter extends BaseAdapter {
        final UserSwitcherController mController;
        private final KeyguardStateController mKeyguardStateController;

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return (long) i;
        }

        protected BaseUserAdapter(UserSwitcherController userSwitcherController) {
            this.mController = userSwitcherController;
            this.mKeyguardStateController = userSwitcherController.mKeyguardStateController;
            userSwitcherController.addAdapter(new WeakReference<>(this));
        }

        public int getUserCount() {
            if (!(this.mKeyguardStateController.isShowing() && this.mKeyguardStateController.isMethodSecure() && !this.mKeyguardStateController.canDismissLockScreen())) {
                return this.mController.getUsers().size();
            }
            int size = this.mController.getUsers().size();
            int i = 0;
            for (int i2 = 0; i2 < size; i2++) {
                if (!this.mController.getUsers().get(i2).isGuest) {
                    if (this.mController.getUsers().get(i2).isRestricted) {
                        break;
                    }
                    i++;
                }
            }
            return i;
        }

        @Override // android.widget.Adapter
        public int getCount() {
            int i = 0;
            if (!(this.mKeyguardStateController.isShowing() && this.mKeyguardStateController.isMethodSecure() && !this.mKeyguardStateController.canDismissLockScreen())) {
                return this.mController.getUsers().size();
            }
            int size = this.mController.getUsers().size();
            int i2 = 0;
            while (i < size && !this.mController.getUsers().get(i).isRestricted) {
                i2++;
                i++;
            }
            return i2;
        }

        @Override // android.widget.Adapter
        public UserRecord getItem(int i) {
            return this.mController.getUsers().get(i);
        }

        public void switchTo(UserRecord userRecord) {
            this.mController.switchTo(userRecord);
        }

        public String getName(Context context, UserRecord userRecord) {
            int i;
            if (userRecord.isGuest) {
                if (userRecord.isCurrent) {
                    return context.getString(R$string.guest_exit_guest);
                }
                if (userRecord.info == null) {
                    i = R$string.guest_new_guest;
                } else {
                    i = R$string.guest_nickname;
                }
                return context.getString(i);
            } else if (userRecord.isAddUser) {
                return context.getString(C0015R$string.user_add_user);
            } else {
                return userRecord.info.name;
            }
        }

        protected static ColorFilter getDisabledUserAvatarColorFilter() {
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0.0f);
            return new ColorMatrixColorFilter(colorMatrix);
        }

        protected static Drawable getIconDrawable(Context context, UserRecord userRecord) {
            int i;
            if (userRecord.isAddUser) {
                i = C0006R$drawable.ic_add_circle;
            } else if (userRecord.isGuest) {
                i = C0006R$drawable.ic_avatar_guest_user;
            } else {
                i = C0006R$drawable.ic_avatar_user;
            }
            return context.getDrawable(i);
        }

        public void refresh() {
            this.mController.refreshUsers(-10000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkIfAddUserDisallowedByAdminOnly(UserRecord userRecord) {
        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtilsInternal.checkIfRestrictionEnforced(this.mContext, "no_add_user", ActivityManager.getCurrentUser());
        if (checkIfRestrictionEnforced == null || RestrictedLockUtilsInternal.hasBaseUserRestriction(this.mContext, "no_add_user", ActivityManager.getCurrentUser())) {
            userRecord.isDisabledByAdmin = false;
            userRecord.enforcedAdmin = null;
            return;
        }
        userRecord.isDisabledByAdmin = true;
        userRecord.enforcedAdmin = checkIfRestrictionEnforced;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldUseSimpleUserSwitcher() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "lockscreenSimpleUserSwitcher", this.mContext.getResources().getBoolean(17891461) ? 1 : 0) != 0;
    }

    public void startActivity(Intent intent) {
        this.mActivityStarter.startActivity(intent, true);
    }

    public static final class UserRecord {
        public RestrictedLockUtils.EnforcedAdmin enforcedAdmin;
        public final UserInfo info;
        public final boolean isAddUser;
        public final boolean isCurrent;
        public boolean isDisabledByAdmin;
        public final boolean isGuest;
        public final boolean isRestricted;
        public boolean isStorageInsufficient;
        public boolean isSwitchToEnabled;
        public final Bitmap picture;

        public UserRecord(UserInfo userInfo, Bitmap bitmap, boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
            this.info = userInfo;
            this.picture = bitmap;
            this.isGuest = z;
            this.isCurrent = z2;
            this.isAddUser = z3;
            this.isRestricted = z4;
            this.isSwitchToEnabled = z5;
        }

        public UserRecord copyWithIsCurrent(boolean z) {
            return new UserRecord(this.info, this.picture, this.isGuest, z, this.isAddUser, this.isRestricted, this.isSwitchToEnabled);
        }

        public int resolveId() {
            UserInfo userInfo;
            if (this.isGuest || (userInfo = this.info) == null) {
                return -10000;
            }
            return userInfo.id;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("UserRecord(");
            if (this.info != null) {
                sb.append("name=\"");
                sb.append(this.info.name);
                sb.append("\" id=");
                sb.append(this.info.id);
            } else if (this.isGuest) {
                sb.append("<add guest placeholder>");
            } else if (this.isAddUser) {
                sb.append("<add user placeholder>");
            }
            if (this.isGuest) {
                sb.append(" <isGuest>");
            }
            if (this.isAddUser) {
                sb.append(" <isAddUser>");
            }
            if (this.isCurrent) {
                sb.append(" <isCurrent>");
            }
            if (this.picture != null) {
                sb.append(" <hasPicture>");
            }
            if (this.isRestricted) {
                sb.append(" <isRestricted>");
            }
            if (this.isDisabledByAdmin) {
                sb.append(" <isDisabledByAdmin>");
                sb.append(" enforcedAdmin=");
                sb.append(this.enforcedAdmin);
            }
            if (this.isSwitchToEnabled) {
                sb.append(" <isSwitchToEnabled>");
            }
            sb.append(')');
            return sb.toString();
        }
    }

    /* access modifiers changed from: private */
    public final class ExitGuestDialog extends OpSystemUIDialog implements DialogInterface.OnClickListener {
        private final int mGuestId;
        private final int mTargetId;

        public ExitGuestDialog(Context context, int i, int i2) {
            super(context);
            setTitle(C0015R$string.guest_exit_guest_dialog_title);
            setMessage(context.getString(C0015R$string.guest_exit_guest_dialog_message));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(C0015R$string.guest_exit_guest_dialog_remove), this);
            OpSystemUIDialog.setWindowOnTop(this);
            setCanceledOnTouchOutside(false);
            this.mGuestId = i;
            this.mTargetId = i2;
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == -2) {
                cancel();
                return;
            }
            dismiss();
            UserSwitcherController.this.exitGuest(this.mGuestId, this.mTargetId);
        }
    }

    /* access modifiers changed from: private */
    public final class AddUserDialog extends OpSystemUIDialog implements DialogInterface.OnClickListener {
        public AddUserDialog(Context context) {
            super(context);
            setTitle(R$string.user_add_user_title);
            setMessage(context.getString(R$string.user_add_user_message_long));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(17039370), this);
            OpSystemUIDialog.setWindowOnTop(this);
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == -2) {
                cancel();
                return;
            }
            dismiss();
            if (!ActivityManager.isUserAMonkey()) {
                new Thread() { // from class: com.android.systemui.statusbar.policy.UserSwitcherController.AddUserDialog.1
                    @Override // java.lang.Thread, java.lang.Runnable
                    public void run() {
                        Log.d("UserSwitcherController", "switchTo:createUser:START");
                        UserSwitcherController userSwitcherController = UserSwitcherController.this;
                        UserInfo createUser = userSwitcherController.mUserManager.createUser(userSwitcherController.mContext.getString(C0015R$string.user_new_user_name), 0);
                        Log.d("UserSwitcherController", "switchTo:createUser:END");
                        if (createUser != null) {
                            int i2 = createUser.id;
                            UserSwitcherController.this.mUserManager.setUserIcon(i2, UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(UserSwitcherController.this.mContext.getResources(), i2, false)));
                            UserSwitcherController.this.switchToUserId(i2);
                        }
                    }
                }.start();
            }
        }
    }
}
