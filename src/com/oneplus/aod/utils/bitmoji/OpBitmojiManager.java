package com.oneplus.aod.utils.bitmoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.oneplus.aod.utils.bitmoji.triggers.BatteryTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.CameraTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.DateTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.GameTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.MusicTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.PhoneTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.TGIFTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.TimeTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.VideoTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.WeatherTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.DelayTrigger;
import com.oneplus.aod.utils.bitmoji.triggers.base.Trigger;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
public class OpBitmojiManager extends SystemUI {
    private static final String[] CASUAL_TRIGGERS_ID = {"time", "date", "weather"};
    private static final String[] USER_TRIGGERS_ID = {"battery", "gaming", "music", "video", "camera", "messaging", "tgif"};
    private Trigger mCurrentTrigger;
    private Handler mHandler;
    private String mLastPackId;
    private ArrayList<OnTriggerChangedListener> mListeners = new ArrayList<>();
    private MdmLogger mMdmLogger;
    private HashMap<String, Trigger> mTriggers = new HashMap<>();
    private final KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() { // from class: com.oneplus.aod.utils.bitmoji.OpBitmojiManager.1
        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onUserUnlocked() {
            if (OpBitmojiManager.this.mHandler != null) {
                OpBitmojiManager.this.mHandler.post(new Runnable() { // from class: com.oneplus.aod.utils.bitmoji.-$$Lambda$OpBitmojiManager$1$MU0tnckoq4d-yBEtL4wGrDLu-_g
                    @Override // java.lang.Runnable
                    public final void run() {
                        OpBitmojiManager.this.onUserUnlocked();
                    }
                });
            } else {
                Log.e("OpBitmojiManager", "onUserUnlocked: handler is null");
            }
        }
    };
    private KeyguardUpdateMonitor mUpdateMonitor;
    private PowerManager.WakeLock mWakeLock;

    public interface OnTriggerChangedListener {
        void onTriggerChanged();
    }

    public OpBitmojiManager(Context context) {
        super(context);
        OpBitmojiHelper.init(context);
        MdmLogger instance = MdmLogger.getInstance(context);
        this.mMdmLogger = instance;
        instance.onOwnerClockChanged();
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        PowerManager.WakeLock newWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "Trigger:check");
        this.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(false);
        this.mTriggers.put("time", new TimeTrigger(this.mContext, this));
        this.mTriggers.put("date", new DateTrigger(this.mContext, this));
        this.mTriggers.put("weather", new WeatherTrigger(this.mContext, this));
        this.mTriggers.put("music", new MusicTrigger(this.mContext, this));
        this.mTriggers.put("video", new VideoTrigger(this.mContext, this));
        this.mTriggers.put("gaming", new GameTrigger(this.mContext, this));
        this.mTriggers.put("camera", new CameraTrigger(this.mContext, this));
        this.mTriggers.put("messaging", new PhoneTrigger(this.mContext, this));
        this.mTriggers.put("battery", new BatteryTrigger(this.mContext, this));
        this.mTriggers.put("tgif", new TGIFTrigger(this.mContext, this));
        computeCurrentTrigger(null);
        onSenerioChangedIfPossible();
        for (Trigger trigger : this.mTriggers.values()) {
            trigger.init();
        }
        this.mUpdateMonitor.registerCallback(this.mUpdateCallback);
        OpBitmojiHelper.getInstance().startPackageListening();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onBootCompleted() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiManager", "onBootCompleted");
        }
        OpBitmojiHelper.getInstance().clearStatusFromStore();
        OpBitmojiHelper.getInstance().registerBitmojiObserver();
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public KeyguardUpdateMonitor getKeyguardUpdateMonitor() {
        return this.mUpdateMonitor;
    }

    public PowerManager.WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public void addListener(OnTriggerChangedListener onTriggerChangedListener) {
        synchronized (this.mListeners) {
            if (!this.mListeners.contains(onTriggerChangedListener)) {
                this.mListeners.add(onTriggerChangedListener);
            }
        }
    }

    public void removeListener(OnTriggerChangedListener onTriggerChangedListener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(onTriggerChangedListener);
        }
    }

    public Object[] getAodImage() {
        Object[] objArr = new Object[2];
        objArr[0] = getActivePack();
        Trigger trigger = this.mCurrentTrigger;
        if (trigger != null) {
            Drawable createFromPath = Drawable.createFromPath(trigger.getImagePath());
            if (createFromPath != null) {
                objArr[1] = createFromPath;
            } else {
                Log.w("OpBitmojiManager", "getAodImage: drawable is null");
            }
        } else {
            Log.w("OpBitmojiManager", "getAodImage: currentTrigger is null");
        }
        if (objArr[1] == null) {
            objArr[1] = this.mContext.getDrawable(C0006R$drawable.op_bitmoji_default);
        }
        return objArr;
    }

    public String getActivePack() {
        Trigger trigger = this.mCurrentTrigger;
        if (trigger != null) {
            return trigger.getCurrentPackId();
        }
        return null;
    }

    public void refresh() {
        Trigger trigger = this.mCurrentTrigger;
        if (trigger != null && trigger.complete()) {
            Trigger trigger2 = this.mCurrentTrigger;
            if (trigger2 instanceof TimeTrigger) {
                this.mCurrentTrigger = this.mTriggers.get("date");
            } else if ((trigger2 instanceof DateTrigger) && this.mTriggers.get("weather").isActive()) {
                this.mCurrentTrigger = this.mTriggers.get("weather");
            } else if (!this.mCurrentTrigger.isActive() || this.mCurrentTrigger.getPriority() <= 1) {
                Trigger trigger3 = this.mCurrentTrigger;
                if (!(trigger3 instanceof DelayTrigger) || !((DelayTrigger) trigger3).enableDelay()) {
                    this.mCurrentTrigger = this.mTriggers.get("time");
                } else {
                    onTriggerChanged(((DelayTrigger) this.mCurrentTrigger).getTriggerId(), false);
                }
            } else if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiManager", "refresh: currentTrigger priority is bigger than low, can not automatically switch to other triggers");
            }
            this.mCurrentTrigger.prepare();
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiManager", "refresh: complete! switch to= " + this.mCurrentTrigger.getClass().getSimpleName());
            }
        }
        onSenerioChangedIfPossible();
    }

    public void onImagePackUpdate(String str, String str2) {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiManager", "onImagePackUpdate: triggerId= " + str + ", packId= " + str2);
        }
        this.mHandler.post(new Runnable(str, str2) { // from class: com.oneplus.aod.utils.bitmoji.-$$Lambda$OpBitmojiManager$A4JjA6S5KQ72hPUIfPUSYu0OJXM
            public final /* synthetic */ String f$1;
            public final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                OpBitmojiManager.this.lambda$onImagePackUpdate$0$OpBitmojiManager(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onImagePackUpdate$0 */
    public /* synthetic */ void lambda$onImagePackUpdate$0$OpBitmojiManager(String str, String str2) {
        if (this.mTriggers.get(str) != null) {
            this.mTriggers.get(str).onImagePackUpdate(str2);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a9  */
    /* JADX WARNING: Removed duplicated region for block: B:78:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onTriggerChanged(java.lang.String r6, boolean r7) {
        /*
        // Method dump skipped, instructions count: 257
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.utils.bitmoji.OpBitmojiManager.onTriggerChanged(java.lang.String, boolean):void");
    }

    private void onSenerioChangedIfPossible() {
        String activePack = getActivePack();
        if (TextUtils.isEmpty(activePack)) {
            Log.i("OpBitmojiManager", "onSenerioChangedIfPossible: currentActivePackId is null");
        } else if (!TextUtils.equals(this.mLastPackId, activePack)) {
            if (Build.DEBUG_ONEPLUS) {
                Log.d("OpBitmojiManager", "onSenerioChangedIfPossible: last= " + this.mLastPackId + ", current = " + activePack);
            }
            this.mLastPackId = activePack;
            OpBitmojiHelper.getInstance().startDownloadCertainPackIfPossible(activePack);
            this.mMdmLogger.logStickerEvent(this.mCurrentTrigger);
        }
    }

    private boolean canKickOut(Trigger trigger, Trigger trigger2) {
        if (!(trigger instanceof MusicTrigger) || !(trigger2 instanceof VideoTrigger)) {
            return true;
        }
        return !((VideoTrigger) trigger2).isActiveInner();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void onUserUnlocked() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiManager", "onUserUnlocked");
        }
        OpBitmojiHelper.getInstance().registerBitmojiObserver();
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        if (strArr == null || strArr.length < 2 || !OpBitmojiManager.class.getName().equals(strArr[0])) {
            str = null;
        } else {
            str = strArr[1];
            if (strArr.length >= 3 && this.mTriggers.get(str) != null) {
                this.mTriggers.get(str).dynamicConfig(strArr);
            }
        }
        printWriter.println("OpBitmojiManager:");
        printWriter.println("version: 2");
        printWriter.println("CurrentTrigger:");
        Trigger trigger = this.mCurrentTrigger;
        if (trigger != null) {
            trigger.dump(fileDescriptor, printWriter, strArr);
        }
        if (this.mTriggers.get(str) != null) {
            printWriter.println("-----------------------------------------------");
            printWriter.println("trigger: " + str);
            this.mTriggers.get(str).dump(fileDescriptor, printWriter, strArr);
            printWriter.println("-----------------------------------------------");
            return;
        }
        printWriter.println();
        printWriter.println("PRINT ALL TRIGGERS:");
        printWriter.println("-----------------------------------------------");
        for (Map.Entry<String, Trigger> entry : this.mTriggers.entrySet()) {
            entry.getValue().dump(fileDescriptor, printWriter, strArr);
            printWriter.println("-----------------------------------------------");
        }
    }

    private void computeCurrentTrigger(Trigger trigger) {
        String[] strArr = CASUAL_TRIGGERS_ID;
        for (String str : USER_TRIGGERS_ID) {
            Trigger trigger2 = this.mTriggers.get(str);
            if (trigger2 != null && trigger2.isActive() && trigger2 != trigger) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d("OpBitmojiManager", "some user trigger event still active");
                }
                this.mCurrentTrigger = trigger2;
                return;
            }
        }
        Date date = new Date();
        int hours = (date.getHours() * 60 * 60 * 1000) + (date.getMinutes() * 60 * 1000) + (date.getSeconds() * 1000);
        int i = hours / Trigger.IMAGE_PER_TIME;
        int length = strArr.length;
        if (!this.mTriggers.get("weather").isActive()) {
            length--;
        }
        int i2 = (i / Trigger.IMAGES_PER_SET) % length;
        Trigger trigger3 = this.mTriggers.get(strArr[i2]);
        this.mCurrentTrigger = trigger3;
        trigger3.prepare();
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiManager", "computeCurrentTrigger: current= " + hours + ", playCount= " + i + ", casualListSize= " + length + ", triggerIndex= " + i2);
        }
    }

    public void onOwnerClockChanged() {
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpBitmojiManager", "onOwnerClockChanged");
        }
        this.mMdmLogger.onOwnerClockChanged();
    }

    public void uploadMdm() {
        this.mMdmLogger.upload();
    }
}
