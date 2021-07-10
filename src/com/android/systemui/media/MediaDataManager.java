package com.android.systemui.media;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Icon;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import androidx.palette.graphics.Palette;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.statusbar.notification.MediaNotificationProcessor;
import com.android.systemui.util.Assert;
import com.android.systemui.util.Utils;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__CollectionsJVMKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: MediaDataManager.kt */
public final class MediaDataManager implements Dumpable {
    private final MediaDataManager$appChangeReceiver$1 appChangeReceiver;
    private final Executor backgroundExecutor;
    private final BroadcastDispatcher broadcastDispatcher;
    private final Context context;
    private final Executor foregroundExecutor;
    private final Set<Listener> listeners;
    private final MediaControllerFactory mediaControllerFactory;
    private final LinkedHashMap<String, MediaData> mediaEntries;
    private boolean useMediaResumption;
    private final boolean useQsMediaPlayer;

    /* compiled from: MediaDataManager.kt */
    public interface Listener {

        /* compiled from: MediaDataManager.kt */
        public static final class DefaultImpls {
            public static void onMediaDataRemoved(Listener listener, @NotNull String str) {
                Intrinsics.checkParameterIsNotNull(str, "key");
            }
        }

        void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData);

        void onMediaDataRemoved(@NotNull String str);
    }

    public MediaDataManager(@NotNull Context context, @NotNull Executor executor, @NotNull Executor executor2, @NotNull MediaControllerFactory mediaControllerFactory, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull DumpManager dumpManager, @NotNull MediaTimeoutListener mediaTimeoutListener, @NotNull MediaResumeListener mediaResumeListener, boolean z, boolean z2) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(executor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(executor2, "foregroundExecutor");
        Intrinsics.checkParameterIsNotNull(mediaControllerFactory, "mediaControllerFactory");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(mediaTimeoutListener, "mediaTimeoutListener");
        Intrinsics.checkParameterIsNotNull(mediaResumeListener, "mediaResumeListener");
        this.context = context;
        this.backgroundExecutor = executor;
        this.foregroundExecutor = executor2;
        this.mediaControllerFactory = mediaControllerFactory;
        this.broadcastDispatcher = broadcastDispatcher;
        this.useMediaResumption = z;
        this.useQsMediaPlayer = z2;
        this.listeners = new LinkedHashSet();
        this.mediaEntries = new LinkedHashMap<>();
        this.appChangeReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.media.MediaDataManager$appChangeReceiver$1
            final /* synthetic */ MediaDataManager this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // android.content.BroadcastReceiver
            public void onReceive(@NotNull Context context2, @NotNull Intent intent) {
                String[] stringArrayExtra;
                String encodedSchemeSpecificPart;
                Intrinsics.checkParameterIsNotNull(context2, "context");
                Intrinsics.checkParameterIsNotNull(intent, "intent");
                String action = intent.getAction();
                if (action != null) {
                    int hashCode = action.hashCode();
                    if (hashCode != -1001645458) {
                        if (hashCode != -757780528) {
                            if (hashCode != 525384130 || !action.equals("android.intent.action.PACKAGE_REMOVED")) {
                                return;
                            }
                        } else if (!action.equals("android.intent.action.PACKAGE_RESTARTED")) {
                            return;
                        }
                        Uri data = intent.getData();
                        if (!(data == null || (encodedSchemeSpecificPart = data.getEncodedSchemeSpecificPart()) == null)) {
                            this.this$0.removeAllForPackage(encodedSchemeSpecificPart);
                        }
                    } else if (action.equals("android.intent.action.PACKAGES_SUSPENDED") && (stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.changed_package_list")) != null) {
                        for (String str : stringArrayExtra) {
                            MediaDataManager mediaDataManager = this.this$0;
                            Intrinsics.checkExpressionValueIsNotNull(str, "it");
                            mediaDataManager.removeAllForPackage(str);
                        }
                    }
                }
            }
        };
        dumpManager.registerDumpable("MediaDataManager", this);
        mediaTimeoutListener.setTimeoutCallback(new Function2<String, Boolean, Unit>(this) { // from class: com.android.systemui.media.MediaDataManager.1
            final /* synthetic */ MediaDataManager this$0;

            {
                this.this$0 = r1;
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
            @Override // kotlin.jvm.functions.Function2
            public /* bridge */ /* synthetic */ Unit invoke(String str, Boolean bool) {
                invoke(str, bool.booleanValue());
                return Unit.INSTANCE;
            }

            public final void invoke(@NotNull String str, boolean z3) {
                Intrinsics.checkParameterIsNotNull(str, "token");
                this.this$0.setTimedOut$packages__apps__OPSystemUI__android_common__OPSystemUI_core(str, z3);
            }
        });
        addListener(mediaTimeoutListener);
        mediaResumeListener.setManager(this);
        addListener(mediaResumeListener);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGES_SUSPENDED");
        BroadcastDispatcher broadcastDispatcher2 = this.broadcastDispatcher;
        MediaDataManager$appChangeReceiver$1 mediaDataManager$appChangeReceiver$1 = this.appChangeReceiver;
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        broadcastDispatcher2.registerReceiver(mediaDataManager$appChangeReceiver$1, intentFilter, null, userHandle);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter2.addAction("android.intent.action.PACKAGE_RESTARTED");
        intentFilter2.addDataScheme("package");
        this.context.registerReceiver(this.appChangeReceiver, intentFilter2);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public MediaDataManager(@NotNull Context context, @NotNull Executor executor, @NotNull Executor executor2, @NotNull MediaControllerFactory mediaControllerFactory, @NotNull DumpManager dumpManager, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull MediaTimeoutListener mediaTimeoutListener, @NotNull MediaResumeListener mediaResumeListener) {
        this(context, executor, executor2, mediaControllerFactory, broadcastDispatcher, dumpManager, mediaTimeoutListener, mediaResumeListener, Utils.useMediaResumption(context), Utils.useQsMediaPlayer(context));
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(executor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(executor2, "foregroundExecutor");
        Intrinsics.checkParameterIsNotNull(mediaControllerFactory, "mediaControllerFactory");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(mediaTimeoutListener, "mediaTimeoutListener");
        Intrinsics.checkParameterIsNotNull(mediaResumeListener, "mediaResumeListener");
    }

    public final void onNotificationAdded(@NotNull String str, @NotNull StatusBarNotification statusBarNotification) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        if (!this.useQsMediaPlayer || !MediaDataManagerKt.isMediaNotification(statusBarNotification)) {
            onNotificationRemoved(str);
            return;
        }
        Assert.isMainThread();
        String packageName = statusBarNotification.getPackageName();
        Intrinsics.checkExpressionValueIsNotNull(packageName, "sbn.packageName");
        String findExistingEntry = findExistingEntry(str, packageName);
        if (findExistingEntry == null) {
            MediaData access$getLOADING$p = MediaDataManagerKt.access$getLOADING$p();
            String packageName2 = statusBarNotification.getPackageName();
            Intrinsics.checkExpressionValueIsNotNull(packageName2, "sbn.packageName");
            this.mediaEntries.put(str, MediaData.copy$default(access$getLOADING$p, 0, false, 0, null, null, null, null, null, null, null, packageName2, null, null, null, false, null, false, null, false, 523263, null));
        } else if (!Intrinsics.areEqual(findExistingEntry, str)) {
            MediaData remove = this.mediaEntries.remove(findExistingEntry);
            if (remove != null) {
                Intrinsics.checkExpressionValueIsNotNull(remove, "mediaEntries.remove(oldKey)!!");
                this.mediaEntries.put(str, remove);
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        loadMediaData(str, statusBarNotification, findExistingEntry);
    }

    /* access modifiers changed from: private */
    public final void removeAllForPackage(String str) {
        Assert.isMainThread();
        Set<Listener> set = CollectionsKt___CollectionsKt.toSet(this.listeners);
        LinkedHashMap<String, MediaData> linkedHashMap = this.mediaEntries;
        LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        for (Map.Entry<String, MediaData> entry : linkedHashMap.entrySet()) {
            if (Intrinsics.areEqual(entry.getValue().getPackageName(), str)) {
                linkedHashMap2.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry entry2 : linkedHashMap2.entrySet()) {
            this.mediaEntries.remove(entry2.getKey());
            for (Listener listener : set) {
                listener.onMediaDataRemoved((String) entry2.getKey());
            }
        }
    }

    public final void setResumeAction(@NotNull String str, @Nullable Runnable runnable) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        MediaData mediaData = this.mediaEntries.get(str);
        if (mediaData != null) {
            mediaData.setResumeAction(runnable);
            mediaData.setHasCheckedForResume(true);
        }
    }

    public final void addResumptionControls(int i, @NotNull MediaDescription mediaDescription, @NotNull Runnable runnable, @NotNull MediaSession.Token token, @NotNull String str, @NotNull PendingIntent pendingIntent, @NotNull String str2) {
        Intrinsics.checkParameterIsNotNull(mediaDescription, "desc");
        Intrinsics.checkParameterIsNotNull(runnable, "action");
        Intrinsics.checkParameterIsNotNull(token, "token");
        Intrinsics.checkParameterIsNotNull(str, "appName");
        Intrinsics.checkParameterIsNotNull(pendingIntent, "appIntent");
        Intrinsics.checkParameterIsNotNull(str2, "packageName");
        if (!this.mediaEntries.containsKey(str2)) {
            this.mediaEntries.put(str2, MediaData.copy$default(MediaDataManagerKt.access$getLOADING$p(), 0, false, 0, null, null, null, null, null, null, null, str2, null, null, null, false, runnable, false, null, true, 228351, null));
        }
        this.backgroundExecutor.execute(new Runnable(this, i, mediaDescription, runnable, token, str, pendingIntent, str2) { // from class: com.android.systemui.media.MediaDataManager$addResumptionControls$1
            final /* synthetic */ Runnable $action;
            final /* synthetic */ PendingIntent $appIntent;
            final /* synthetic */ String $appName;
            final /* synthetic */ MediaDescription $desc;
            final /* synthetic */ String $packageName;
            final /* synthetic */ MediaSession.Token $token;
            final /* synthetic */ int $userId;
            final /* synthetic */ MediaDataManager this$0;

            {
                this.this$0 = r1;
                this.$userId = r2;
                this.$desc = r3;
                this.$action = r4;
                this.$token = r5;
                this.$appName = r6;
                this.$appIntent = r7;
                this.$packageName = r8;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.loadMediaDataInBgForResumption(this.$userId, this.$desc, this.$action, this.$token, this.$appName, this.$appIntent, this.$packageName);
            }
        });
    }

    private final String findExistingEntry(String str, String str2) {
        if (this.mediaEntries.containsKey(str)) {
            return str;
        }
        if (this.mediaEntries.containsKey(str2)) {
            return str2;
        }
        return null;
    }

    private final void loadMediaData(String str, StatusBarNotification statusBarNotification, String str2) {
        this.backgroundExecutor.execute(new Runnable(this, str, statusBarNotification, str2) { // from class: com.android.systemui.media.MediaDataManager$loadMediaData$1
            final /* synthetic */ String $key;
            final /* synthetic */ String $oldKey;
            final /* synthetic */ StatusBarNotification $sbn;
            final /* synthetic */ MediaDataManager this$0;

            {
                this.this$0 = r1;
                this.$key = r2;
                this.$sbn = r3;
                this.$oldKey = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.loadMediaDataInBg(this.$key, this.$sbn, this.$oldKey);
            }
        });
    }

    public final boolean addListener(@NotNull Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        return this.listeners.add(listener);
    }

    public final void setTimedOut$packages__apps__OPSystemUI__android_common__OPSystemUI_core(@NotNull String str, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "token");
        MediaData mediaData = this.mediaEntries.get(str);
        if (mediaData != null && mediaData.getActive() != (!z)) {
            mediaData.setActive(!z);
            Intrinsics.checkExpressionValueIsNotNull(mediaData, "it");
            onMediaDataLoaded(str, str, mediaData);
        }
    }

    /* access modifiers changed from: private */
    public final void loadMediaDataInBgForResumption(int i, MediaDescription mediaDescription, Runnable runnable, MediaSession.Token token, String str, PendingIntent pendingIntent, String str2) {
        if (TextUtils.isEmpty(mediaDescription.getTitle())) {
            Log.e("MediaDataManager", "Description incomplete");
            this.mediaEntries.remove(str2);
            return;
        }
        Log.d("MediaDataManager", "adding track for " + i + " from browser: " + mediaDescription);
        Bitmap iconBitmap = mediaDescription.getIconBitmap();
        Icon icon = null;
        if (iconBitmap == null && mediaDescription.getIconUri() != null) {
            Uri iconUri = mediaDescription.getIconUri();
            if (iconUri != null) {
                iconBitmap = loadBitmapFromUri(iconUri);
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        if (iconBitmap != null) {
            icon = Icon.createWithBitmap(iconBitmap);
        }
        this.foregroundExecutor.execute(new Runnable(this, str2, i, iconBitmap != null ? computeBackgroundColor(iconBitmap) : -12303292, str, mediaDescription, icon, getResumeMediaAction(runnable), token, pendingIntent, runnable) { // from class: com.android.systemui.media.MediaDataManager$loadMediaDataInBgForResumption$1
            final /* synthetic */ PendingIntent $appIntent;
            final /* synthetic */ String $appName;
            final /* synthetic */ Icon $artworkIcon;
            final /* synthetic */ int $bgColor;
            final /* synthetic */ MediaDescription $desc;
            final /* synthetic */ MediaAction $mediaAction;
            final /* synthetic */ String $packageName;
            final /* synthetic */ Runnable $resumeAction;
            final /* synthetic */ MediaSession.Token $token;
            final /* synthetic */ int $userId;
            final /* synthetic */ MediaDataManager this$0;

            {
                this.this$0 = r1;
                this.$packageName = r2;
                this.$userId = r3;
                this.$bgColor = r4;
                this.$appName = r5;
                this.$desc = r6;
                this.$artworkIcon = r7;
                this.$mediaAction = r8;
                this.$token = r9;
                this.$appIntent = r10;
                this.$resumeAction = r11;
            }

            @Override // java.lang.Runnable
            public final void run() {
                MediaDataManager mediaDataManager = this.this$0;
                String str3 = this.$packageName;
                int i2 = this.$userId;
                int i3 = this.$bgColor;
                String str4 = this.$appName;
                CharSequence subtitle = this.$desc.getSubtitle();
                CharSequence title = this.$desc.getTitle();
                Icon icon2 = this.$artworkIcon;
                List list = CollectionsKt__CollectionsJVMKt.listOf(this.$mediaAction);
                List list2 = CollectionsKt__CollectionsJVMKt.listOf(0);
                String str5 = this.$packageName;
                mediaDataManager.onMediaDataLoaded(str3, null, new MediaData(i2, true, i3, str4, null, subtitle, title, icon2, list, list2, str5, this.$token, this.$appIntent, null, false, this.$resumeAction, true, str5, true));
            }
        });
    }

    /* JADX WARN: Type inference failed for: r3v10, types: [T, java.lang.String] */
    /* JADX WARN: Type inference failed for: r0v6, types: [T, java.lang.String] */
    /* JADX WARN: Type inference failed for: r0v20, types: [java.lang.CharSequence, T] */
    /* JADX WARN: Type inference failed for: r3v23, types: [java.lang.CharSequence, T] */
    /* JADX WARN: Type inference failed for: r3v25, types: [T, java.lang.String] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Unknown variable types count: 5 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void loadMediaDataInBg(java.lang.String r21, android.service.notification.StatusBarNotification r22, java.lang.String r23) {
        /*
        // Method dump skipped, instructions count: 424
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.MediaDataManager.loadMediaDataInBg(java.lang.String, android.service.notification.StatusBarNotification, java.lang.String):void");
    }

    private final Bitmap loadBitmapFromUri(MediaMetadata mediaMetadata) {
        String[] access$getART_URIS$p = MediaDataManagerKt.access$getART_URIS$p();
        for (String str : access$getART_URIS$p) {
            String string = mediaMetadata.getString(str);
            if (!TextUtils.isEmpty(string)) {
                Uri parse = Uri.parse(string);
                Intrinsics.checkExpressionValueIsNotNull(parse, "Uri.parse(uriString)");
                Bitmap loadBitmapFromUri = loadBitmapFromUri(parse);
                if (loadBitmapFromUri != null) {
                    Log.d("MediaDataManager", "loaded art from " + str);
                    return loadBitmapFromUri;
                }
            }
        }
        return null;
    }

    private final Bitmap loadBitmapFromUri(Uri uri) {
        if (uri.getScheme() == null) {
            return null;
        }
        if (!uri.getScheme().equals("content") && !uri.getScheme().equals("android.resource") && !uri.getScheme().equals("file")) {
            return null;
        }
        try {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.context.getContentResolver(), uri), MediaDataManager$loadBitmapFromUri$1.INSTANCE);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private final int computeBackgroundColor(Bitmap bitmap) {
        int i;
        if (bitmap != null) {
            Palette generate = MediaNotificationProcessor.generateArtworkPaletteBuilder(bitmap).generate();
            Intrinsics.checkExpressionValueIsNotNull(generate, "MediaNotificationProcess…              .generate()");
            Palette.Swatch findBackgroundSwatch = MediaNotificationProcessor.findBackgroundSwatch(generate);
            Intrinsics.checkExpressionValueIsNotNull(findBackgroundSwatch, "swatch");
            i = findBackgroundSwatch.getRgb();
        } else {
            i = -1;
        }
        float[] fArr = {0.0f, 0.0f, 0.0f};
        ColorUtils.colorToHSL(i, fArr);
        float f = fArr[2];
        if (f < 0.05f || f > 0.95f) {
            fArr[1] = 0.0f;
        }
        fArr[1] = fArr[1] * 0.8f;
        fArr[2] = 0.25f;
        return ColorUtils.HSLToColor(fArr);
    }

    private final MediaAction getResumeMediaAction(Runnable runnable) {
        return new MediaAction(this.context.getDrawable(C0006R$drawable.lb_ic_play), runnable, this.context.getString(C0015R$string.controls_media_resume));
    }

    public final void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(mediaData, "data");
        Assert.isMainThread();
        if (this.mediaEntries.containsKey(str)) {
            this.mediaEntries.put(str, mediaData);
            for (Listener listener : CollectionsKt___CollectionsKt.toSet(this.listeners)) {
                listener.onMediaDataLoaded(str, str2, mediaData);
            }
        }
    }

    public final void onNotificationRemoved(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Assert.isMainThread();
        MediaData remove = this.mediaEntries.remove(str);
        if (this.useMediaResumption) {
            String str2 = null;
            if ((remove != null ? remove.getResumeAction() : null) != null) {
                Log.d("MediaDataManager", "Not removing " + str + " because resumable");
                Runnable resumeAction = remove.getResumeAction();
                if (resumeAction != null) {
                    boolean z = false;
                    MediaData copy$default = MediaData.copy$default(remove, 0, false, 0, null, null, null, null, null, CollectionsKt__CollectionsJVMKt.listOf(getResumeMediaAction(resumeAction)), CollectionsKt__CollectionsJVMKt.listOf(0), null, null, null, null, false, null, true, null, false, 439551, null);
                    if (remove != null) {
                        str2 = remove.getPackageName();
                    }
                    if (this.mediaEntries.put(str2, copy$default) == null) {
                        z = true;
                    }
                    Set<Listener> set = CollectionsKt___CollectionsKt.toSet(this.listeners);
                    if (z) {
                        for (Listener listener : set) {
                            listener.onMediaDataLoaded(str2, str, copy$default);
                        }
                        return;
                    }
                    for (Listener listener2 : set) {
                        listener2.onMediaDataRemoved(str);
                    }
                    for (Listener listener3 : set) {
                        listener3.onMediaDataLoaded(str2, str2, copy$default);
                    }
                    return;
                }
                Intrinsics.throwNpe();
                throw null;
            }
        }
        if (remove != null) {
            for (Listener listener4 : CollectionsKt___CollectionsKt.toSet(this.listeners)) {
                listener4.onMediaDataRemoved(str);
            }
        }
    }

    public final void setMediaResumptionEnabled(boolean z) {
        if (this.useMediaResumption != z) {
            this.useMediaResumption = z;
            if (!z) {
                Set<Listener> set = CollectionsKt___CollectionsKt.toSet(this.listeners);
                LinkedHashMap<String, MediaData> linkedHashMap = this.mediaEntries;
                LinkedHashMap linkedHashMap2 = new LinkedHashMap();
                for (Map.Entry<String, MediaData> entry : linkedHashMap.entrySet()) {
                    if (!entry.getValue().getActive()) {
                        linkedHashMap2.put(entry.getKey(), entry.getValue());
                    }
                }
                for (Map.Entry entry2 : linkedHashMap2.entrySet()) {
                    this.mediaEntries.remove(entry2.getKey());
                    for (Listener listener : set) {
                        listener.onMediaDataRemoved((String) entry2.getKey());
                    }
                }
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        printWriter.println("listeners: " + this.listeners);
        printWriter.println("mediaEntries: " + this.mediaEntries);
        printWriter.println("useMediaResumption: " + this.useMediaResumption);
    }
}
