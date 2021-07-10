package com.oneplus.aod.utils.bitmoji;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.aod.utils.bitmoji.triggers.base.Trigger;
import com.oneplus.systemui.util.OpMdmLogger;
import java.util.HashMap;
import java.util.Map;
import kotlin.Unit;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: MdmLogger.kt */
public final class MdmLogger {
    public static final Companion Companion = new Companion(null);
    private static final boolean DEBUG = SystemProperties.getBoolean("sys.aod.bitmoji.mdm", false);
    @NotNull
    private static final String KEY_USE_PREV = KEY_USE_PREV;
    @NotNull
    private static final String KEY_USE_START = KEY_USE_START;
    @NotNull
    private static final String MDM_ALWAYS_ON_DIALOG = MDM_ALWAYS_ON_DIALOG;
    @NotNull
    private static final String MDM_BITMOJI_PREFS = MDM_BITMOJI_PREFS;
    @NotNull
    private static final String MDM_CANCEL_DOWNLOAD = MDM_CANCEL_DOWNLOAD;
    @NotNull
    private static final String MDM_DOWNLOAD_BUTTON = MDM_DOWNLOAD_BUTTON;
    @NotNull
    private static final String MDM_DOWNLOAD_PROMPT = MDM_DOWNLOAD_PROMPT;
    @NotNull
    private static final String MDM_EVENT_NAME = MDM_EVENT_NAME;
    @NotNull
    private static final String MDM_FIRST_TIME = MDM_FIRST_TIME;
    @NotNull
    private static final String MDM_NETWORK_AFTER = MDM_NETWORK_AFTER;
    @NotNull
    private static final String MDM_NETWORK_FIRST = MDM_NETWORK_FIRST;
    @NotNull
    private static final String MDM_NETWORK_OPTION = MDM_NETWORK_OPTION;
    @NotNull
    private static final String MDM_NOTIFICATION_READY = MDM_NOTIFICATION_READY;
    @NotNull
    private static final String MDM_NOTIFICATION_READY_CLICK = MDM_NOTIFICATION_READY_CLICK;
    @NotNull
    private static final String MDM_SETUP_BUTTON = MDM_SETUP_BUTTON;
    @NotNull
    private static final String MDM_SETUP_PROMPT = MDM_SETUP_PROMPT;
    @NotNull
    private static final String MDM_SHOW_INTRO = MDM_SHOW_INTRO;
    @NotNull
    private static final String MDM_STICKER_LABEL_PREFIX = MDM_STICKER_LABEL_PREFIX;
    @NotNull
    private static final String MDM_UPDATE = MDM_UPDATE;
    @NotNull
    private static final String MDM_UPDATE_HOW = MDM_UPDATE_HOW;
    @NotNull
    private static final String MDM_USE = MDM_USE;
    @NotNull
    private static final String MDM_USE_TIME = MDM_USE_TIME;
    private static final int NO = 0;
    @NotNull
    private static final String TAG = "BitmojiAod.MdmLogger";
    private static final int YES = 1;
    @Nullable
    private static MdmLogger sInstance;
    @NotNull
    private final Context context;
    private final Object mLock;
    private final HashMap<String, Integer> mMap;

    @NotNull
    public static final MdmLogger getInstance(@NotNull Context context) {
        return Companion.getInstance(context);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v4, resolved type: java.util.HashMap<java.lang.String, java.lang.Integer> */
    /* JADX WARN: Multi-variable type inference failed */
    private MdmLogger(Context context) {
        this.context = context;
        this.mLock = new Object();
        this.mMap = new HashMap<>();
        Map<String, ?> all = this.context.getSharedPreferences(MDM_BITMOJI_PREFS, 0).getAll();
        Intrinsics.checkExpressionValueIsNotNull(all, "entries");
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!TextUtils.isEmpty(key)) {
                Intrinsics.checkExpressionValueIsNotNull(key, "key");
                if (!skipKey(key) && (value instanceof Integer)) {
                    this.mMap.put(key, value);
                }
            }
        }
    }

    public /* synthetic */ MdmLogger(Context context, DefaultConstructorMarker defaultConstructorMarker) {
        this(context);
    }

    /* compiled from: MdmLogger.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @Nullable
        public final MdmLogger getSInstance() {
            return MdmLogger.sInstance;
        }

        public final void setSInstance(@Nullable MdmLogger mdmLogger) {
            MdmLogger.sInstance = mdmLogger;
        }

        @NotNull
        public final MdmLogger getInstance(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            synchronized (MdmLogger.class) {
                if (MdmLogger.Companion.getSInstance() == null) {
                    MdmLogger.Companion.setSInstance(new MdmLogger(context, null));
                }
                Unit unit = Unit.INSTANCE;
            }
            MdmLogger sInstance = getSInstance();
            if (sInstance != null) {
                return sInstance;
            }
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public final void logNetworkOptionFirstShownEvent() {
        doCounterAndSave(MDM_NETWORK_FIRST);
    }

    public final void logNetworkOptionChooseEvent(int i) {
        log(MDM_NETWORK_OPTION, String.valueOf(i));
    }

    public final void logNetworkOptionClickEvent() {
        doCounterAndSave(MDM_NETWORK_AFTER);
    }

    public final void logStickerNeedsUpdateEvent() {
        doCounterAndSave(MDM_UPDATE);
    }

    public final void logUpdateStickerEvent(int i) {
        doCounterAndSave(MDM_UPDATE_HOW + '@' + i);
    }

    public final void logNotificationReadyShownEvent() {
        doCounterAndSave(MDM_NOTIFICATION_READY);
        doCounterAndSave(MDM_NOTIFICATION_READY_CLICK + '@' + NO);
    }

    public final void logNotificationReadyClickEvent() {
        doCounterAndSave(MDM_NOTIFICATION_READY_CLICK + '@' + YES, MDM_NOTIFICATION_READY_CLICK + '@' + NO);
    }

    public final void logCancelDownloadEvent() {
        doCounterAndSave(MDM_CANCEL_DOWNLOAD);
    }

    public final void logStickerEvent(@NotNull Trigger trigger) {
        Intrinsics.checkParameterIsNotNull(trigger, "trigger");
        String mdmLabel = trigger.getMdmLabel();
        if (!TextUtils.isEmpty(mdmLabel)) {
            doCounterAndSave(MDM_STICKER_LABEL_PREFIX + mdmLabel);
        }
    }

    public final void trackFromSettings(@NotNull String str, @Nullable String str2) {
        Intrinsics.checkParameterIsNotNull(str, "label");
        if (Intrinsics.areEqual(str, MDM_SHOW_INTRO)) {
            doChecked(str);
        } else if (Intrinsics.areEqual(str, MDM_DOWNLOAD_PROMPT) || Intrinsics.areEqual(str, MDM_SETUP_PROMPT)) {
            doCounterAndSave(str);
        } else if (Intrinsics.areEqual(str, MDM_DOWNLOAD_BUTTON) || Intrinsics.areEqual(str, MDM_SETUP_BUTTON) || Intrinsics.areEqual(str, MDM_ALWAYS_ON_DIALOG)) {
            log(str, str2);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x011b  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0125  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void upload() {
        /*
        // Method dump skipped, instructions count: 308
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.utils.bitmoji.MdmLogger.upload():void");
    }

    public final void onOwnerClockChanged() {
        int currentAodClockStyle = OpAodUtils.getCurrentAodClockStyle(this.context, 0);
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(MDM_BITMOJI_PREFS, 0);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        long j = sharedPreferences.getLong(KEY_USE_START, -1);
        if (currentAodClockStyle == 12) {
            if (j == -1) {
                edit.putLong(KEY_USE_START, System.currentTimeMillis());
                edit.commit();
                if (DEBUG) {
                    Log.d(TAG, "trackClockChanged: choose bitmoji");
                }
            } else if (Build.DEBUG_ONEPLUS) {
                Log.d(TAG, "onOwnerClockChanged: switch bitmoji invalid");
            }
        } else if (j != -1) {
            int currentTimeMillis = (int) ((System.currentTimeMillis() - j) / ((long) 86400000));
            if (currentTimeMillis <= 0) {
                currentTimeMillis = 1;
            }
            if (currentTimeMillis > sharedPreferences.getInt(KEY_USE_PREV, -1)) {
                edit.putInt(KEY_USE_PREV, currentTimeMillis);
            }
            if (sharedPreferences.getInt(MDM_FIRST_TIME, -1) == -1) {
                edit.putInt(MDM_FIRST_TIME, currentTimeMillis);
            }
            edit.remove(KEY_USE_START);
            edit.commit();
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "trackClockChanged: consistDay= " + currentTimeMillis);
            }
        } else if (Build.DEBUG_ONEPLUS) {
            Log.d(TAG, "onOwnerClockChanged: switch other invalid");
        }
    }

    private final int get(String str) {
        return this.context.getSharedPreferences(MDM_BITMOJI_PREFS, 0).getInt(str, 0);
    }

    private final void doChecked(String str) {
        this.context.getSharedPreferences(MDM_BITMOJI_PREFS, 0).edit().putInt(str, 1).commit();
    }

    private final void doCounterAndSave(String str) {
        doCounterAndSave(str, null);
    }

    private final void doCounterAndSave(String str, String str2) {
        synchronized (this.mLock) {
            int i = 0;
            SharedPreferences.Editor edit = this.context.getSharedPreferences(MDM_BITMOJI_PREFS, 0).edit();
            Integer num = this.mMap.get(str);
            int intValue = (num instanceof Integer ? num.intValue() : 0) + 1;
            this.mMap.put(str, new Integer(intValue));
            edit.putInt(str, intValue);
            if (str2 != null && this.mMap.containsKey(str2)) {
                Integer num2 = this.mMap.get(str2);
                if (num2 instanceof Integer) {
                    i = num2.intValue();
                }
                if (i - 1 > 0) {
                    int i2 = i - 1;
                    this.mMap.put(str2, new Integer(i2));
                    edit.putInt(str2, i2);
                } else {
                    this.mMap.remove(str2);
                    edit.remove(str2);
                }
            }
            edit.apply();
            if (DEBUG) {
                Log.d(TAG, "event: label= " + str + ", count=" + intValue);
            }
            Unit unit = Unit.INSTANCE;
        }
    }

    private final void log(String str, String str2) {
        if (DEBUG) {
            String str3 = TAG;
            Log.d(str3, "log: label=" + str + ", value=" + str2);
        }
        OpMdmLogger.log(MDM_EVENT_NAME, str, str2);
    }

    private final boolean skipKey(String str) {
        return Intrinsics.areEqual(str, KEY_USE_PREV) || Intrinsics.areEqual(str, KEY_USE_START) || Intrinsics.areEqual(str, MDM_FIRST_TIME) || Intrinsics.areEqual(str, MDM_SHOW_INTRO);
    }
}
