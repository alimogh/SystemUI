package com.android.systemui.media;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.ResumeMediaBrowser;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Regex;
import kotlin.text.StringsKt__StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: MediaResumeListener.kt */
public final class MediaResumeListener implements MediaDataManager.Listener {
    private final Executor backgroundExecutor;
    private final BroadcastDispatcher broadcastDispatcher;
    private final Context context;
    private int currentUserId = this.context.getUserId();
    private ResumeMediaBrowser mediaBrowser;
    private final MediaResumeListener$mediaBrowserCallback$1 mediaBrowserCallback = new MediaResumeListener$mediaBrowserCallback$1(this);
    private MediaDataManager mediaDataManager;
    private final ConcurrentLinkedQueue<ComponentName> resumeComponents = new ConcurrentLinkedQueue<>();
    private final TunerService tunerService;
    private boolean useMediaResumption;
    private final MediaResumeListener$userChangeReceiver$1 userChangeReceiver = new BroadcastReceiver(this) { // from class: com.android.systemui.media.MediaResumeListener$userChangeReceiver$1
        final /* synthetic */ MediaResumeListener this$0;

        /* JADX WARN: Incorrect args count in method signature: ()V */
        {
            this.this$0 = r1;
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(@NotNull Context context, @NotNull Intent intent) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            Intrinsics.checkParameterIsNotNull(intent, "intent");
            if (Intrinsics.areEqual("android.intent.action.USER_UNLOCKED", intent.getAction())) {
                this.this$0.loadMediaResumptionControls();
            } else if (Intrinsics.areEqual("android.intent.action.USER_SWITCHED", intent.getAction())) {
                this.this$0.currentUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                this.this$0.loadSavedComponents();
            }
        }
    };

    public MediaResumeListener(@NotNull Context context, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull Executor executor, @NotNull TunerService tunerService) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(executor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(tunerService, "tunerService");
        this.context = context;
        this.broadcastDispatcher = broadcastDispatcher;
        this.backgroundExecutor = executor;
        this.tunerService = tunerService;
        this.useMediaResumption = Utils.useMediaResumption(context);
        if (this.useMediaResumption) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_UNLOCKED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            BroadcastDispatcher broadcastDispatcher2 = this.broadcastDispatcher;
            MediaResumeListener$userChangeReceiver$1 mediaResumeListener$userChangeReceiver$1 = this.userChangeReceiver;
            UserHandle userHandle = UserHandle.ALL;
            Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
            broadcastDispatcher2.registerReceiver(mediaResumeListener$userChangeReceiver$1, intentFilter, null, userHandle);
            loadSavedComponents();
        }
    }

    public static final /* synthetic */ MediaDataManager access$getMediaDataManager$p(MediaResumeListener mediaResumeListener) {
        MediaDataManager mediaDataManager = mediaResumeListener.mediaDataManager;
        if (mediaDataManager != null) {
            return mediaDataManager;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mediaDataManager");
        throw null;
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataRemoved(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        MediaDataManager.Listener.DefaultImpls.onMediaDataRemoved(this, str);
    }

    public final void setManager(@NotNull MediaDataManager mediaDataManager) {
        Intrinsics.checkParameterIsNotNull(mediaDataManager, "manager");
        this.mediaDataManager = mediaDataManager;
        this.tunerService.addTunable(new TunerService.Tunable(this) { // from class: com.android.systemui.media.MediaResumeListener$setManager$1
            final /* synthetic */ MediaResumeListener this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.tuner.TunerService.Tunable
            public void onTuningChanged(@Nullable String str, @Nullable String str2) {
                MediaResumeListener mediaResumeListener = this.this$0;
                mediaResumeListener.useMediaResumption = Utils.useMediaResumption(mediaResumeListener.context);
                MediaResumeListener.access$getMediaDataManager$p(this.this$0).setMediaResumptionEnabled(this.this$0.useMediaResumption);
            }
        }, "qs_media_resumption");
    }

    public final boolean isResumptionEnabled() {
        return this.useMediaResumption;
    }

    /* access modifiers changed from: private */
    public final void loadSavedComponents() {
        List<String> split;
        boolean z;
        this.resumeComponents.clear();
        List<String> list = null;
        String string = this.context.getSharedPreferences("media_control_prefs", 0).getString("browser_components_" + this.currentUserId, null);
        if (string != null && (split = new Regex(":").split(string, 0)) != null) {
            if (!split.isEmpty()) {
                ListIterator<String> listIterator = split.listIterator(split.size());
                while (true) {
                    if (!listIterator.hasPrevious()) {
                        break;
                    }
                    if (listIterator.previous().length() == 0) {
                        z = true;
                        continue;
                    } else {
                        z = false;
                        continue;
                    }
                    if (!z) {
                        list = CollectionsKt___CollectionsKt.take(split, listIterator.nextIndex() + 1);
                        break;
                    }
                }
            }
            list = CollectionsKt__CollectionsKt.emptyList();
        }
        if (list != null) {
            for (String str : list) {
                List list2 = StringsKt__StringsKt.split$default(str, new String[]{"/"}, false, 0, 6, null);
                this.resumeComponents.add(new ComponentName((String) list2.get(0), (String) list2.get(1)));
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("loaded resume components ");
        Object[] array = this.resumeComponents.toArray();
        Intrinsics.checkExpressionValueIsNotNull(array, "resumeComponents.toArray()");
        String arrays = Arrays.toString(array);
        Intrinsics.checkExpressionValueIsNotNull(arrays, "java.util.Arrays.toString(this)");
        sb.append(arrays);
        Log.d("MediaResumeListener", sb.toString());
    }

    /* access modifiers changed from: private */
    public final void loadMediaResumptionControls() {
        if (this.useMediaResumption) {
            for (ComponentName componentName : this.resumeComponents) {
                new ResumeMediaBrowser(this.context, this.mediaBrowserCallback, componentName).findRecentMedia();
            }
        }
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
        ArrayList arrayList;
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(mediaData, "data");
        if (this.useMediaResumption) {
            ResumeMediaBrowser resumeMediaBrowser = this.mediaBrowser;
            if (resumeMediaBrowser != null) {
                resumeMediaBrowser.disconnect();
            }
            if (mediaData.getResumeAction() == null && !mediaData.getHasCheckedForResume()) {
                Log.d("MediaResumeListener", "Checking for service component for " + mediaData.getPackageName());
                List<ResolveInfo> queryIntentServices = this.context.getPackageManager().queryIntentServices(new Intent("android.media.browse.MediaBrowserService"), 0);
                if (queryIntentServices != null) {
                    arrayList = new ArrayList();
                    for (Object obj : queryIntentServices) {
                        if (Intrinsics.areEqual(((ResolveInfo) obj).serviceInfo.packageName, mediaData.getPackageName())) {
                            arrayList.add(obj);
                        }
                    }
                } else {
                    arrayList = null;
                }
                if (arrayList == null || arrayList.size() <= 0) {
                    MediaDataManager mediaDataManager = this.mediaDataManager;
                    if (mediaDataManager != null) {
                        mediaDataManager.setResumeAction(str, null);
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("mediaDataManager");
                        throw null;
                    }
                } else {
                    this.backgroundExecutor.execute(new Runnable(this, str, arrayList) { // from class: com.android.systemui.media.MediaResumeListener$onMediaDataLoaded$1
                        final /* synthetic */ List $inf;
                        final /* synthetic */ String $key;
                        final /* synthetic */ MediaResumeListener this$0;

                        {
                            this.this$0 = r1;
                            this.$key = r2;
                            this.$inf = r3;
                        }

                        @Override // java.lang.Runnable
                        public final void run() {
                            MediaResumeListener mediaResumeListener = this.this$0;
                            String str3 = this.$key;
                            List list = this.$inf;
                            if (list != null) {
                                Object obj2 = list.get(0);
                                Intrinsics.checkExpressionValueIsNotNull(obj2, "inf!!.get(0)");
                                ComponentInfo componentInfo = ((ResolveInfo) obj2).getComponentInfo();
                                Intrinsics.checkExpressionValueIsNotNull(componentInfo, "inf!!.get(0).componentInfo");
                                ComponentName componentName = componentInfo.getComponentName();
                                Intrinsics.checkExpressionValueIsNotNull(componentName, "inf!!.get(0).componentInfo.componentName");
                                mediaResumeListener.tryUpdateResumptionList(str3, componentName);
                                return;
                            }
                            Intrinsics.throwNpe();
                            throw null;
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final void tryUpdateResumptionList(String str, ComponentName componentName) {
        Log.d("MediaResumeListener", "Testing if we can connect to " + componentName);
        ResumeMediaBrowser resumeMediaBrowser = this.mediaBrowser;
        if (resumeMediaBrowser != null) {
            resumeMediaBrowser.disconnect();
        }
        ResumeMediaBrowser resumeMediaBrowser2 = new ResumeMediaBrowser(this.context, new ResumeMediaBrowser.Callback(this, componentName, str) { // from class: com.android.systemui.media.MediaResumeListener$tryUpdateResumptionList$1
            final /* synthetic */ ComponentName $componentName;
            final /* synthetic */ String $key;
            final /* synthetic */ MediaResumeListener this$0;

            {
                this.this$0 = r1;
                this.$componentName = r2;
                this.$key = r3;
            }

            @Override // com.android.systemui.media.ResumeMediaBrowser.Callback
            public void onConnected() {
                Log.d("MediaResumeListener", "yes we can resume with " + this.$componentName);
                MediaResumeListener.access$getMediaDataManager$p(this.this$0).setResumeAction(this.$key, this.this$0.getResumeAction(this.$componentName));
                this.this$0.updateResumptionList(this.$componentName);
                ResumeMediaBrowser resumeMediaBrowser3 = this.this$0.mediaBrowser;
                if (resumeMediaBrowser3 != null) {
                    resumeMediaBrowser3.disconnect();
                }
                this.this$0.mediaBrowser = null;
            }

            @Override // com.android.systemui.media.ResumeMediaBrowser.Callback
            public void onError() {
                Log.e("MediaResumeListener", "Cannot resume with " + this.$componentName);
                MediaResumeListener.access$getMediaDataManager$p(this.this$0).setResumeAction(this.$key, null);
                ResumeMediaBrowser resumeMediaBrowser3 = this.this$0.mediaBrowser;
                if (resumeMediaBrowser3 != null) {
                    resumeMediaBrowser3.disconnect();
                }
                this.this$0.mediaBrowser = null;
            }
        }, componentName);
        this.mediaBrowser = resumeMediaBrowser2;
        if (resumeMediaBrowser2 != null) {
            resumeMediaBrowser2.testConnection();
        }
    }

    /* access modifiers changed from: private */
    public final void updateResumptionList(ComponentName componentName) {
        this.resumeComponents.remove(componentName);
        this.resumeComponents.add(componentName);
        if (this.resumeComponents.size() > 5) {
            this.resumeComponents.remove();
        }
        StringBuilder sb = new StringBuilder();
        for (ComponentName componentName2 : this.resumeComponents) {
            sb.append(componentName2.flattenToString());
            sb.append(":");
        }
        SharedPreferences.Editor edit = this.context.getSharedPreferences("media_control_prefs", 0).edit();
        edit.putString("browser_components_" + this.currentUserId, sb.toString()).apply();
    }

    /* access modifiers changed from: private */
    public final Runnable getResumeAction(ComponentName componentName) {
        return new MediaResumeListener$getResumeAction$1(this, componentName);
    }
}
