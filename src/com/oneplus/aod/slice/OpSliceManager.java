package com.oneplus.aod.slice;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.keyguard.KeyguardAssistantView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.aod.OpClockViewCtrl;
import com.oneplus.aod.controller.IOpClockController;
import com.oneplus.keyguard.OpKeyguardClockInfoView;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
public class OpSliceManager implements OpClockViewCtrl.OpClockOnChangeListener {
    private static final int[] PRIORITY = {1, 2, 3};
    private boolean mAllowShowSensitiveData = true;
    private boolean mCalendarEnabled = false;
    private IOpClockController mClockController;
    private Context mContext;
    private final H mHandler = new H((Looper) Dependency.get(Dependency.BG_LOOPER));
    private ImageView mIcon;
    private KeyguardAssistantView mKeyguardAssistantView;
    public KeyguardAssistantView.Callback mKeyguardAssistantViewCallback = new KeyguardAssistantView.Callback() { // from class: com.oneplus.aod.slice.OpSliceManager.1
        @Override // com.android.keyguard.KeyguardAssistantView.Callback
        public void onCardShownChanged(boolean z) {
            Log.i("OpSliceManager", "receive onCardShownChanged value:" + z);
            OpSliceManager.this.refresh(true);
        }
    };
    private boolean mListening = false;
    private boolean mMusicInfoEnabled = false;
    private OpKeyguardClockInfoView mOpKeyguardClockInfoView;
    private TextView mPrimary;
    private TextView mRemark;
    private TextView mSecondary;
    private SettingsObserver mSettingsObserver;
    private final BroadcastReceiver mSleepStateReceiver = new BroadcastReceiver() { // from class: com.oneplus.aod.slice.OpSliceManager.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Bundle extras;
            Log.d("OpSliceManager", "onReceive: " + intent);
            String action = intent.getAction();
            if (("net.oneplus.powercontroller.intent.SLEEP_CHANGED".equals(action) || "com.android.systemui.intent.SLEEP_CHANGED".equals(action)) && (extras = intent.getExtras()) != null && extras.getInt("state") == 7788) {
                String localDateTime = LocalDateTime.now().toString();
                OpSliceManager.this.mContext.getSharedPreferences(OpSliceManager.this.mContext.getPackageName(), 0).edit().putString("pref_name_sleep_end", localDateTime).apply();
                OpSliceManager.this.mContext.getSharedPreferences(OpSliceManager.this.mContext.getPackageName(), 0).edit().putString("pref_name_initiative_pulse", "").apply();
                Log.d("OpSliceManager", "save sleep end time: " + localDateTime + " and clear user initiative pulse time");
                OpSlice opSlice = (OpSlice) OpSliceManager.this.mSlices.get(3);
                if (opSlice != null && opSlice.isEnabled()) {
                    ((OpWeatherSlice) opSlice).refreshState();
                }
            }
        }
    };
    private final LinkedHashMap<Integer, OpSlice> mSlices = new LinkedHashMap<>();
    private boolean mSmartDisplayCurState = false;
    private boolean mSmartDisplayEnabled = false;
    private Handler mUiHandler;
    private int mUserId;
    private OpSliceContainer mViewContainer;
    private boolean mViewInit;

    private String getSliceName(int i) {
        return i == 1 ? "calendar" : i == 2 ? "music" : i == 3 ? "weather" : "none";
    }

    public OpSliceManager(Context context, Handler handler) {
        this.mContext = context;
        this.mUserId = KeyguardUpdateMonitor.getCurrentUser();
        this.mUiHandler = handler;
        Callback callback = new Callback(this, this);
        this.mSlices.put(2, new OpMusicSlice(this.mContext, callback));
        this.mSlices.put(3, new OpWeatherSlice(this.mContext, callback));
        this.mSlices.put(1, new OpCalendarSlice(this.mContext, callback));
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
    }

    public void initViews(View view) {
        Log.i("OpSliceManager", "initViews, stack:" + Debug.getCallers(7));
        this.mViewContainer = (OpSliceContainer) view;
        this.mIcon = (ImageView) view.findViewById(C0008R$id.slice_icon);
        this.mPrimary = (TextView) view.findViewById(C0008R$id.slice_primary);
        this.mRemark = (TextView) view.findViewById(C0008R$id.slice_remark);
        this.mSecondary = (TextView) view.findViewById(C0008R$id.slice_secondary);
        this.mViewContainer.setUserId(this.mUserId);
        if (!OpAodUtils.isDefaultOrRedAodClockStyle(this.mContext, this.mUserId)) {
            initAssitantView();
        } else {
            clearAssistantViewIfPossible();
        }
        if (!this.mViewInit) {
            this.mSettingsObserver.observe();
            IntentFilter intentFilter = new IntentFilter("net.oneplus.powercontroller.intent.SLEEP_CHANGED");
            intentFilter.addAction("com.android.systemui.intent.SLEEP_CHANGED");
            this.mContext.registerReceiverAsUser(this.mSleepStateReceiver, UserHandle.ALL, intentFilter, null, null);
            this.mViewInit = true;
        }
    }

    private void updateAssistantView() {
        Log.i("OpSliceManager", "updateAssistantView, stack:" + Debug.getCallers(1));
        if (this.mKeyguardAssistantView == null) {
            Log.i("OpSliceManager", "updateAssistantView mKeyguardAssistantView == null, no launch slicerVersionSmartSapce");
        } else {
            this.mUiHandler.post(new Runnable() { // from class: com.oneplus.aod.slice.-$$Lambda$OpSliceManager$qANlgc44vucdC7lXl8SxHFtf5Hg
                @Override // java.lang.Runnable
                public final void run() {
                    OpSliceManager.this.lambda$updateAssistantView$0$OpSliceManager();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateAssistantView$0 */
    public /* synthetic */ void lambda$updateAssistantView$0$OpSliceManager() {
        if (!this.mKeyguardAssistantView.hasHeader() || !isKeyguardAssistantViewActiveInSlice()) {
            this.mKeyguardAssistantView.setVisibility(8);
        } else {
            this.mKeyguardAssistantView.setVisibility(0);
        }
        this.mKeyguardAssistantView.setHideSensitiveData(!this.mAllowShowSensitiveData);
    }

    public void setListening(boolean z) {
        Log.d("OpSliceManager", "setListening, mUserId:" + this.mUserId + ", mSmartDisplayEnabled:" + this.mSmartDisplayEnabled + ", listening:" + z + ", mListening:" + this.mListening + ", mOpKeyguardClockInfoView:" + this.mOpKeyguardClockInfoView + ", callers= " + Debug.getCallers(3));
        if (this.mUserId != 0) {
            Log.i("OpSliceManager", "Do not active slices since current user is " + this.mUserId);
        } else if (!this.mSmartDisplayEnabled) {
            Log.i("OpSliceManager", "Do not active slices since smart aod is disabled");
        } else if (this.mListening != z) {
            this.mListening = z;
            this.mSlices.values().forEach(new Consumer(z) { // from class: com.oneplus.aod.slice.-$$Lambda$OpSliceManager$jKMwOIyYHukafoZWADnNkJ6OnYU
                public final /* synthetic */ boolean f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((OpSlice) obj).setListening(this.f$0);
                }
            });
            this.mHandler.sendEmptyMessage(1);
        }
    }

    private void clearAssistantViewIfPossible() {
        KeyguardAssistantView keyguardAssistantView = this.mKeyguardAssistantView;
        if (keyguardAssistantView != null) {
            keyguardAssistantView.removeCallback(this.mKeyguardAssistantViewCallback);
            this.mKeyguardAssistantView.release();
            this.mKeyguardAssistantView = null;
        }
    }

    private void initAssitantView() {
        Log.i("OpSliceManager", "initAssitantView, stack:, callers= " + Debug.getCallers(7));
        clearAssistantViewIfPossible();
        if (!OpAodUtils.isDefaultOrRedAodClockStyle(this.mContext, this.mUserId) && !OpAodUtils.isAodNoneClockStyle(this.mContext, this.mUserId)) {
            KeyguardAssistantView keyguardAssistantView = new KeyguardAssistantView(this.mViewContainer, this.mContext, this.mUiHandler);
            this.mKeyguardAssistantView = keyguardAssistantView;
            if (keyguardAssistantView != null) {
                keyguardAssistantView.addCallback(this.mKeyguardAssistantViewCallback);
                this.mKeyguardAssistantView.inflateIndicatorContainer();
                this.mKeyguardAssistantView.setHideSensitiveData(!this.mAllowShowSensitiveData);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateEnabled() {
        for (Integer num : this.mSlices.keySet()) {
            int intValue = num.intValue();
            boolean z = false;
            boolean z2 = this.mSmartDisplayEnabled && this.mSmartDisplayCurState;
            if (this.mUserId == 0 && ((intValue != 2 || this.mMusicInfoEnabled) && (intValue != 1 || this.mCalendarEnabled))) {
                z = z2;
            }
            this.mSlices.get(Integer.valueOf(intValue)).setEnabled(z);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refresh(boolean z) {
        if (z) {
            this.mUiHandler.postAtFrontOfQueue(new Runnable() { // from class: com.oneplus.aod.slice.-$$Lambda$OpSliceManager$CxwC4BVgWsvWi0aWldF3eHFjfUY
                @Override // java.lang.Runnable
                public final void run() {
                    OpSliceManager.this.lambda$refresh$2$OpSliceManager();
                }
            });
        } else {
            this.mUiHandler.post(new Runnable() { // from class: com.oneplus.aod.slice.-$$Lambda$OpSliceManager$bu2Far_RdiKeyiAbaIXdgVh9rUU
                @Override // java.lang.Runnable
                public final void run() {
                    OpSliceManager.this.lambda$refresh$3$OpSliceManager();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: refreshInternal */
    public void lambda$refresh$3() {
        OpKeyguardClockInfoView opKeyguardClockInfoView;
        OpKeyguardClockInfoView opKeyguardClockInfoView2;
        if (!this.mViewInit) {
            Log.w("OpSliceManager", "view has not init yet.");
            return;
        }
        int activeSlice = getActiveSlice();
        OpSlice opSlice = this.mSlices.get(Integer.valueOf(activeSlice));
        Log.i("OpSliceManager", "slice count: " + this.mSlices.size() + ", refresh to " + getSliceName(activeSlice));
        if (isKeyguardAssistantViewActiveInSlice()) {
            this.mIcon.setVisibility(8);
            this.mPrimary.setVisibility(8);
            this.mSecondary.setVisibility(8);
            this.mRemark.setVisibility(8);
            this.mViewContainer.setVisibility(shouldSliceContainerBeVisible());
        } else if (opSlice != null && !OpAodUtils.isDefaultOrRedAodClockStyle(this.mContext, this.mUserId)) {
            this.mIcon.setVisibility(0);
            this.mPrimary.setVisibility(0);
            this.mIcon.setImageResource(opSlice.getIcon());
            this.mPrimary.setText(opSlice.getPrimaryString());
            String remark = opSlice.getRemark();
            if (remark == null || remark.trim().length() == 0) {
                this.mRemark.setVisibility(8);
            } else {
                this.mRemark.setText(remark);
                this.mRemark.setVisibility(0);
            }
            String secondaryString = opSlice.getSecondaryString();
            if (secondaryString == null || secondaryString.trim().length() == 0) {
                this.mSecondary.setVisibility(8);
            } else {
                this.mSecondary.setText(opSlice.getSecondaryString());
                this.mSecondary.setVisibility(0);
            }
            this.mViewContainer.setVisibility(shouldSliceContainerBeVisible());
        } else if (opSlice == null || !OpAodUtils.isDefaultOrRedAodClockStyle(this.mContext, this.mUserId)) {
            this.mIcon.setVisibility(8);
            this.mPrimary.setVisibility(8);
            this.mSecondary.setVisibility(8);
            this.mRemark.setVisibility(8);
            this.mViewContainer.setVisibility(8);
        } else {
            this.mIcon.setVisibility(8);
            this.mPrimary.setVisibility(8);
            this.mSecondary.setVisibility(8);
            this.mRemark.setVisibility(8);
            this.mViewContainer.setVisibility(8);
            if (OpAodUtils.isDefaultOrRedAodClockStyle(this.mContext, this.mUserId) && (opKeyguardClockInfoView2 = this.mOpKeyguardClockInfoView) != null) {
                opKeyguardClockInfoView2.updateSliceView(true, opSlice.getIcon(), opSlice.getPrimaryString(), opSlice.getSecondaryString(), opSlice.getRemark());
            }
        }
        if (opSlice == null && OpAodUtils.isDefaultOrRedAodClockStyle(this.mContext, this.mUserId) && (opKeyguardClockInfoView = this.mOpKeyguardClockInfoView) != null) {
            opKeyguardClockInfoView.updateSliceView(false, 0, "", "", "");
        }
        updateAssistantView();
    }

    public void onTimeChanged() {
        if (this.mListening) {
            for (Integer num : this.mSlices.keySet()) {
                OpSlice opSlice = this.mSlices.get(Integer.valueOf(num.intValue()));
                if (opSlice != null && opSlice.isEnabled()) {
                    opSlice.onTimeChanged();
                }
            }
        }
    }

    public void onInitiativePulse() {
        OpSlice opSlice = this.mSlices.get(3);
        if (opSlice != null && opSlice.isEnabled()) {
            ((OpWeatherSlice) opSlice).onUserActive();
        }
    }

    private boolean isKeyguardAssistantViewActiveInSlice() {
        KeyguardAssistantView keyguardAssistantView;
        return !OpAodUtils.isDefaultOrRedAodClockStyle(this.mContext, this.mUserId) && (keyguardAssistantView = this.mKeyguardAssistantView) != null && keyguardAssistantView.hasHeader();
    }

    private int getActiveSlice() {
        int[] iArr = PRIORITY;
        if (isKeyguardAssistantViewActiveInSlice()) {
            Log.i("OpSliceManager", "getActiveSlice return TYPE_NONE, mAllowShowSensitiveData:" + this.mAllowShowSensitiveData);
            return 0;
        }
        int length = iArr.length;
        for (int i = 0; i < length; i++) {
            OpSlice opSlice = this.mSlices.get(Integer.valueOf(iArr[i]));
            StringBuilder sb = new StringBuilder();
            sb.append("setSlice: ");
            sb.append(getSliceName(iArr[i]));
            sb.append(" priority: ");
            sb.append(i);
            sb.append(opSlice == null ? " slice is null" : " isActive=" + opSlice.isActive() + " isEnabled=" + opSlice.isEnabled());
            Log.i("OpSliceManager", sb.toString());
            if (opSlice != null && opSlice.isActive() && opSlice.isEnabled()) {
                return iArr[i];
            }
        }
        return 0;
    }

    public void onUserSwitchComplete(int i) {
        this.mUserId = i;
        this.mViewContainer.setUserId(i);
        updateEnabled();
    }

    /* access modifiers changed from: private */
    public final class SettingsObserver extends ContentObserver {
        private final Uri uriCalendarEnabled = Settings.System.getUriFor("aod_smart_display_calendar_enabled");
        private final Uri uriLockScreenAllowPrivateNotifications = Settings.Secure.getUriFor("lock_screen_allow_private_notifications");
        private final Uri uriMusicInfoEnabled = Settings.System.getUriFor("aod_smart_display_music_info_enabled");
        private final Uri uriSmartDisplayCurState = Settings.System.getUriFor("aod_smart_display_cur_state");
        private final Uri uriSmartDisplayEnabled = Settings.System.getUriFor("aod_smart_display_enabled");

        SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver contentResolver = OpSliceManager.this.mContext.getContentResolver();
            contentResolver.registerContentObserver(this.uriSmartDisplayEnabled, false, this, -1);
            contentResolver.registerContentObserver(this.uriSmartDisplayCurState, false, this, -1);
            contentResolver.registerContentObserver(this.uriMusicInfoEnabled, false, this, -1);
            contentResolver.registerContentObserver(this.uriCalendarEnabled, false, this, -1);
            contentResolver.registerContentObserver(this.uriLockScreenAllowPrivateNotifications, false, this, -1);
            update(null);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            ContentResolver contentResolver = OpSliceManager.this.mContext.getContentResolver();
            boolean z = false;
            if (uri == null || this.uriSmartDisplayEnabled.equals(uri)) {
                OpSliceManager.this.mSmartDisplayEnabled = 1 == Settings.System.getIntForUser(contentResolver, "aod_smart_display_enabled", 1, -2);
            }
            if (uri == null || this.uriSmartDisplayCurState.equals(uri)) {
                OpSliceManager.this.mSmartDisplayCurState = 1 == Settings.System.getIntForUser(contentResolver, "aod_smart_display_cur_state", 1, -2);
            }
            if (uri == null || this.uriMusicInfoEnabled.equals(uri)) {
                OpSliceManager.this.mMusicInfoEnabled = 1 == Settings.System.getIntForUser(contentResolver, "aod_smart_display_music_info_enabled", 1, -2);
            }
            if (uri == null || this.uriCalendarEnabled.equals(uri)) {
                OpSliceManager.this.mCalendarEnabled = 1 == Settings.System.getIntForUser(contentResolver, "aod_smart_display_calendar_enabled", 1, -2);
            }
            if (uri == null || this.uriLockScreenAllowPrivateNotifications.equals(uri)) {
                OpSliceManager opSliceManager = OpSliceManager.this;
                if (1 == Settings.Secure.getIntForUser(opSliceManager.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 0, -2)) {
                    z = true;
                }
                opSliceManager.mAllowShowSensitiveData = z;
            }
            OpSliceManager.this.updateEnabled();
            Log.d("OpSliceManager", "update uri=" + uri + " mSmartDisplayEnabled=" + OpSliceManager.this.mSmartDisplayEnabled + " mSmartDisplayCurState = " + OpSliceManager.this.mSmartDisplayCurState + " mMusicInfoEnabled=" + OpSliceManager.this.mMusicInfoEnabled + " mCalendarEnabled=" + OpSliceManager.this.mCalendarEnabled + " mAllowShowSensitiveData=" + OpSliceManager.this.mAllowShowSensitiveData);
        }
    }

    /* access modifiers changed from: protected */
    public final class H extends Handler {
        protected H(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                OpSliceManager.this.refresh(false);
            }
        }
    }

    public class Callback {
        private OpSliceManager mSliceManager;

        public Callback(OpSliceManager opSliceManager, OpSliceManager opSliceManager2) {
            this.mSliceManager = opSliceManager2;
        }

        public void updateUI() {
            this.mSliceManager.refresh(false);
        }
    }

    public void dump(PrintWriter printWriter) {
        this.mSlices.values().forEach(new Consumer(printWriter) { // from class: com.oneplus.aod.slice.-$$Lambda$OpSliceManager$EMJ3LU0vgSjo3IF642krw44r6k4
            public final /* synthetic */ PrintWriter f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((OpSlice) obj).dump(this.f$0);
            }
        });
    }

    @Override // com.oneplus.aod.OpClockViewCtrl.OpClockOnChangeListener
    public void onClockChanged(IOpClockController iOpClockController) {
        Log.i("OpSliceManager", "receive onClockChanged");
        this.mClockController = iOpClockController;
        if (!OpAodUtils.isDefaultOrRedAodClockStyle(this.mContext, this.mUserId)) {
            OpKeyguardClockInfoView opKeyguardClockInfoView = this.mOpKeyguardClockInfoView;
            if (opKeyguardClockInfoView != null) {
                opKeyguardClockInfoView.updateSliceView(false, 0, "", "", "");
                this.mOpKeyguardClockInfoView = null;
            }
            if (this.mKeyguardAssistantView == null) {
                initAssitantView();
            }
        } else {
            clearAssistantViewIfPossible();
            if (iOpClockController != null) {
                this.mOpKeyguardClockInfoView = (OpKeyguardClockInfoView) iOpClockController.getCurrentView();
            }
        }
        refresh(false);
    }

    private int shouldSliceContainerBeVisible() {
        IOpClockController iOpClockController = this.mClockController;
        return (iOpClockController == null || !iOpClockController.shouldShowSliceInfo()) ? 8 : 0;
    }
}
