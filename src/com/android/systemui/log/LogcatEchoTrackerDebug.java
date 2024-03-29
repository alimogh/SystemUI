package com.android.systemui.log;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.constraintlayout.widget.R$styleable;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.TypeCastException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
/* compiled from: LogcatEchoTrackerDebug.kt */
public final class LogcatEchoTrackerDebug implements LogcatEchoTracker {
    public static final Factory Factory = new Factory(null);
    private final Map<String, LogLevel> cachedBufferLevels;
    private final Map<String, LogLevel> cachedTagLevels;
    private final ContentResolver contentResolver;

    @NotNull
    public static final LogcatEchoTrackerDebug create(@NotNull ContentResolver contentResolver, @NotNull Looper looper) {
        return Factory.create(contentResolver, looper);
    }

    private LogcatEchoTrackerDebug(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        this.cachedBufferLevels = new LinkedHashMap();
        this.cachedTagLevels = new LinkedHashMap();
    }

    public /* synthetic */ LogcatEchoTrackerDebug(ContentResolver contentResolver, DefaultConstructorMarker defaultConstructorMarker) {
        this(contentResolver);
    }

    /* compiled from: LogcatEchoTrackerDebug.kt */
    public static final class Factory {
        private Factory() {
        }

        public /* synthetic */ Factory(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final LogcatEchoTrackerDebug create(@NotNull ContentResolver contentResolver, @NotNull Looper looper) {
            Intrinsics.checkParameterIsNotNull(contentResolver, "contentResolver");
            Intrinsics.checkParameterIsNotNull(looper, "mainLooper");
            LogcatEchoTrackerDebug logcatEchoTrackerDebug = new LogcatEchoTrackerDebug(contentResolver, null);
            logcatEchoTrackerDebug.attach(looper);
            return logcatEchoTrackerDebug;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void attach(Looper looper) {
        this.contentResolver.registerContentObserver(Settings.Global.getUriFor("systemui/buffer"), true, new ContentObserver(this, looper, new Handler(looper)) { // from class: com.android.systemui.log.LogcatEchoTrackerDebug$attach$1
            final /* synthetic */ LogcatEchoTrackerDebug this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z, @NotNull Uri uri) {
                Intrinsics.checkParameterIsNotNull(uri, "uri");
                super.onChange(z, uri);
                LogcatEchoTrackerDebug.access$getCachedBufferLevels$p(this.this$0).clear();
            }
        });
        this.contentResolver.registerContentObserver(Settings.Global.getUriFor("systemui/tag"), true, new ContentObserver(this, looper, new Handler(looper)) { // from class: com.android.systemui.log.LogcatEchoTrackerDebug$attach$2
            final /* synthetic */ LogcatEchoTrackerDebug this$0;

            {
                this.this$0 = r1;
            }

            @Override // android.database.ContentObserver
            public void onChange(boolean z, @NotNull Uri uri) {
                Intrinsics.checkParameterIsNotNull(uri, "uri");
                super.onChange(z, uri);
                LogcatEchoTrackerDebug.access$getCachedTagLevels$p(this.this$0).clear();
            }
        });
    }

    @Override // com.android.systemui.log.LogcatEchoTracker
    public synchronized boolean isBufferLoggable(@NotNull String str, @NotNull LogLevel logLevel) {
        Intrinsics.checkParameterIsNotNull(str, "bufferName");
        Intrinsics.checkParameterIsNotNull(logLevel, "level");
        return logLevel.ordinal() >= getLogLevel(str, "systemui/buffer", this.cachedBufferLevels).ordinal();
    }

    @Override // com.android.systemui.log.LogcatEchoTracker
    public synchronized boolean isTagLoggable(@NotNull String str, @NotNull LogLevel logLevel) {
        Intrinsics.checkParameterIsNotNull(str, "tagName");
        Intrinsics.checkParameterIsNotNull(logLevel, "level");
        return logLevel.compareTo(getLogLevel(str, "systemui/tag", this.cachedTagLevels)) >= 0;
    }

    private final LogLevel getLogLevel(String str, String str2, Map<String, LogLevel> map) {
        LogLevel logLevel = map.get(str);
        if (logLevel != null) {
            return logLevel;
        }
        LogLevel readSetting = readSetting(str2 + '/' + str);
        map.put(str, readSetting);
        return readSetting;
    }

    private final LogLevel readSetting(String str) {
        try {
            return parseProp(Settings.Global.getString(this.contentResolver, str));
        } catch (Settings.SettingNotFoundException unused) {
            return LogcatEchoTrackerDebugKt.DEFAULT_LEVEL;
        }
    }

    private final LogLevel parseProp(String str) {
        String str2;
        if (str == null) {
            str2 = null;
        } else if (str != null) {
            str2 = str.toLowerCase();
            Intrinsics.checkExpressionValueIsNotNull(str2, "(this as java.lang.String).toLowerCase()");
        } else {
            throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
        }
        if (str2 != null) {
            switch (str2.hashCode()) {
                case -1408208058:
                    if (str2.equals("assert")) {
                        return LogLevel.WTF;
                    }
                    break;
                case R$styleable.Constraint_layout_goneMarginLeft /* 100 */:
                    if (str2.equals("d")) {
                        return LogLevel.DEBUG;
                    }
                    break;
                case R$styleable.Constraint_layout_goneMarginRight /* 101 */:
                    if (str2.equals("e")) {
                        return LogLevel.ERROR;
                    }
                    break;
                case R$styleable.Constraint_pathMotionArc /* 105 */:
                    if (str2.equals("i")) {
                        return LogLevel.INFO;
                    }
                    break;
                case androidx.appcompat.R$styleable.AppCompatTheme_windowActionBarOverlay /* 118 */:
                    if (str2.equals("v")) {
                        return LogLevel.VERBOSE;
                    }
                    break;
                case androidx.appcompat.R$styleable.AppCompatTheme_windowActionModeOverlay /* 119 */:
                    if (str2.equals("w")) {
                        return LogLevel.WARNING;
                    }
                    break;
                case 118057:
                    if (str2.equals("wtf")) {
                        return LogLevel.WTF;
                    }
                    break;
                case 3237038:
                    if (str2.equals("info")) {
                        return LogLevel.INFO;
                    }
                    break;
                case 3641990:
                    if (str2.equals("warn")) {
                        return LogLevel.WARNING;
                    }
                    break;
                case 95458899:
                    if (str2.equals("debug")) {
                        return LogLevel.DEBUG;
                    }
                    break;
                case 96784904:
                    if (str2.equals("error")) {
                        return LogLevel.ERROR;
                    }
                    break;
                case 351107458:
                    if (str2.equals("verbose")) {
                        return LogLevel.VERBOSE;
                    }
                    break;
                case 1124446108:
                    if (str2.equals("warning")) {
                        return LogLevel.WARNING;
                    }
                    break;
            }
        }
        return LogcatEchoTrackerDebugKt.DEFAULT_LEVEL;
    }
}
