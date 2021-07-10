package com.oneplus.opzenmode;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.volume.Util;
import com.google.android.collect.Lists;
import com.oneplus.opzenmode.OpZenModeController;
import com.oneplus.util.SystemSetting;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
public class OpZenModeControllerImpl implements OpZenModeController {
    private static boolean DEBUG = Build.DEBUG_ONEPLUS;
    private final ArrayList<WeakReference<OpZenModeController.Callback>> mCallbacks = Lists.newArrayList();
    private Context mContext;
    private int mCurrentUserId;
    private boolean mDndEnable = false;
    private SystemSetting mDndSettingObserver;
    private final Handler mHandler = new Handler() { // from class: com.oneplus.opzenmode.OpZenModeControllerImpl.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 201) {
                OpZenModeControllerImpl.this.handleThreeKeyUpdate(message.arg1);
            } else if (i == 202) {
                OpZenModeControllerImpl.this.handleDndUpdate(((Boolean) message.obj).booleanValue());
            }
        }
    };
    private int mThreeKeySatus = -1;
    private GlobalSetting mThreeKeySettingObserver;
    private final CurrentUserTracker mUserTracker;
    private GlobalSetting mZenModeSettingObserver;

    public OpZenModeControllerImpl(Context context) {
        Log.i("OpZenModeControllerImpl", "OpZenModeControllerImpl");
        this.mContext = context;
        registerListener();
        this.mCurrentUserId = -2;
        AnonymousClass2 r0 = new CurrentUserTracker((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class)) { // from class: com.oneplus.opzenmode.OpZenModeControllerImpl.2
            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                OpZenModeControllerImpl.this.onUserSwitched(i);
            }
        };
        this.mUserTracker = r0;
        r0.startTracking();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onUserSwitched(int i) {
        if (DEBUG) {
            Log.i("OpZenModeControllerImpl", "onUserSwitched " + i);
        }
        if (this.mCurrentUserId != i) {
            this.mCurrentUserId = i;
            registerListener();
        }
    }

    private void registerListener() {
        AnonymousClass3 r0 = new GlobalSetting(this.mContext, new Handler(), "zen_mode") { // from class: com.oneplus.opzenmode.OpZenModeControllerImpl.3
            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.GlobalSetting
            public void handleValueChanged(int i) {
                OpZenModeControllerImpl.this.onDndUpdate(i != 0);
                Log.i("OpZenModeControllerImpl", " zenMode Chnage:" + i);
            }
        };
        this.mZenModeSettingObserver = r0;
        r0.setListening(true);
        onDndUpdate(this.mZenModeSettingObserver.getValue() != 0);
        AnonymousClass4 r02 = new GlobalSetting(this.mContext, this.mHandler, "three_Key_mode") { // from class: com.oneplus.opzenmode.OpZenModeControllerImpl.4
            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.GlobalSetting
            public void handleValueChanged(int i) {
                OpZenModeControllerImpl.this.onThreeKeyUpdate(i);
            }
        };
        this.mThreeKeySettingObserver = r02;
        r02.setListening(true);
        int threeKeyStatus = Util.getThreeKeyStatus(this.mContext);
        if (threeKeyStatus != this.mThreeKeySatus) {
            this.mThreeKeySatus = threeKeyStatus;
            onThreeKeyUpdate(threeKeyStatus);
        }
        Log.i("OpZenModeControllerImpl", "registerListener mThreeKeySatus:" + this.mThreeKeySatus + " current user:-2");
    }

    @Override // com.oneplus.opzenmode.OpZenModeController
    public void setDndEnable(boolean z) {
        this.mDndSettingObserver.setValue(z ? 1 : 0);
    }

    @Override // com.oneplus.opzenmode.OpZenModeController
    public boolean getDndEnable() {
        return this.mDndEnable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onThreeKeyUpdate(int i) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(201, i, 0));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDndUpdate(boolean z) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(202, Boolean.valueOf(z)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleThreeKeyUpdate(int i) {
        if (i != this.mThreeKeySatus) {
            this.mThreeKeySatus = i;
            Log.i("OpZenModeControllerImpl", " handleThreeKeyUpdate :" + i);
            for (int i2 = 0; i2 < this.mCallbacks.size(); i2++) {
                OpZenModeController.Callback callback = this.mCallbacks.get(i2).get();
                if (callback != null) {
                    callback.onThreeKeyStatus(this.mThreeKeySatus);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDndUpdate(boolean z) {
        if (this.mDndEnable != z) {
            Log.i("OpZenModeControllerImpl", " handleDndUpdate enable:" + z);
            this.mDndEnable = z;
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                OpZenModeController.Callback callback = this.mCallbacks.get(i).get();
                if (callback != null) {
                    callback.onDndChanged(this.mDndEnable);
                }
            }
        }
    }

    public void addCallback(OpZenModeController.Callback callback) {
        synchronized (this.mCallbacks) {
            if (DEBUG) {
                Log.v("OpZenModeControllerImpl", "*** register callback for " + callback);
            }
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                if (this.mCallbacks.get(i).get() == callback) {
                    if (DEBUG) {
                        Log.e("OpZenModeControllerImpl", "Object tried to add another callback", new Exception("Called by"));
                    }
                    return;
                }
            }
            this.mCallbacks.add(new WeakReference<>(callback));
            removeCallback((OpZenModeController.Callback) null);
            sendUpdates(callback);
        }
    }

    public void removeCallback(OpZenModeController.Callback callback) {
        synchronized (this.mCallbacks) {
            if (DEBUG) {
                Log.v("OpZenModeControllerImpl", "*** unregister callback for " + callback);
            }
            for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
                if (this.mCallbacks.get(size).get() == callback) {
                    this.mCallbacks.remove(size);
                }
            }
        }
    }

    private void sendUpdates(OpZenModeController.Callback callback) {
        callback.onDndChanged(this.mDndEnable);
        callback.onThreeKeyStatus(this.mThreeKeySatus);
    }

    @Override // com.oneplus.opzenmode.OpZenModeController
    public int getThreeKeySatus() {
        return this.mThreeKeySatus;
    }
}
