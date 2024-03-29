package com.android.systemui.media;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaRouter2Manager;
import android.media.RoutingSessionInfo;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import com.android.settingslib.media.LocalMediaManager;
import com.android.settingslib.media.MediaDevice;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.MediaDeviceManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: MediaDeviceManager.kt */
public final class MediaDeviceManager implements MediaDataManager.Listener, Dumpable {
    private final Context context;
    private final DumpManager dumpManager;
    private final Map<String, Token> entries = new LinkedHashMap();
    private final Executor fgExecutor;
    private final Set<Listener> listeners = new LinkedHashSet();
    private final LocalMediaManagerFactory localMediaManagerFactory;
    private final MediaDataManager mediaDataManager;
    private final MediaRouter2Manager mr2manager;

    /* compiled from: MediaDeviceManager.kt */
    public interface Listener {
        void onKeyRemoved(@NotNull String str);

        void onMediaDeviceChanged(@NotNull String str, @Nullable MediaDeviceData mediaDeviceData);
    }

    public MediaDeviceManager(@NotNull Context context, @NotNull LocalMediaManagerFactory localMediaManagerFactory, @NotNull MediaRouter2Manager mediaRouter2Manager, @NotNull Executor executor, @NotNull MediaDataManager mediaDataManager, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(localMediaManagerFactory, "localMediaManagerFactory");
        Intrinsics.checkParameterIsNotNull(mediaRouter2Manager, "mr2manager");
        Intrinsics.checkParameterIsNotNull(executor, "fgExecutor");
        Intrinsics.checkParameterIsNotNull(mediaDataManager, "mediaDataManager");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.context = context;
        this.localMediaManagerFactory = localMediaManagerFactory;
        this.mr2manager = mediaRouter2Manager;
        this.fgExecutor = executor;
        this.mediaDataManager = mediaDataManager;
        this.dumpManager = dumpManager;
        this.mediaDataManager.addListener(this);
        DumpManager dumpManager2 = this.dumpManager;
        String name = MediaDeviceManager.class.getName();
        Intrinsics.checkExpressionValueIsNotNull(name, "javaClass.name");
        dumpManager2.registerDumpable(name, this);
    }

    public final boolean addListener(@NotNull Listener listener) {
        Intrinsics.checkParameterIsNotNull(listener, "listener");
        return this.listeners.add(listener);
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
        Token remove;
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(mediaData, "data");
        if (!(str2 == null || !(!Intrinsics.areEqual(str2, str)) || (remove = this.entries.remove(str2)) == null)) {
            remove.stop();
        }
        Token token = this.entries.get(str);
        if (token == null || (!Intrinsics.areEqual(token.getToken(), mediaData.getToken()))) {
            if (token != null) {
                token.stop();
            }
            MediaSession.Token token2 = mediaData.getToken();
            Token token3 = new Token(this, str, token2 != null ? new MediaController(this.context, token2) : null, this.localMediaManagerFactory.create(mediaData.getPackageName()));
            this.entries.put(str, token3);
            token3.start();
        }
    }

    @Override // com.android.systemui.media.MediaDataManager.Listener
    public void onMediaDataRemoved(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Token remove = this.entries.remove(str);
        if (remove != null) {
            remove.stop();
        }
        if (remove != null) {
            for (Listener listener : this.listeners) {
                listener.onKeyRemoved(str);
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        printWriter.println("MediaDeviceManager state:");
        this.entries.forEach(new BiConsumer<String, Token>(printWriter, this, fileDescriptor, printWriter, strArr) { // from class: com.android.systemui.media.MediaDeviceManager$dump$$inlined$with$lambda$1
            final /* synthetic */ String[] $args$inlined;
            final /* synthetic */ FileDescriptor $fd$inlined;
            final /* synthetic */ PrintWriter $pw$inlined;
            final /* synthetic */ PrintWriter $this_with;

            {
                this.$this_with = r1;
                this.$fd$inlined = r3;
                this.$pw$inlined = r4;
                this.$args$inlined = r5;
            }

            public final void accept(@NotNull String str, @NotNull MediaDeviceManager.Token token) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                Intrinsics.checkParameterIsNotNull(token, "entry");
                PrintWriter printWriter2 = this.$this_with;
                printWriter2.println("  key=" + str);
                token.dump(this.$fd$inlined, this.$pw$inlined, this.$args$inlined);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void processDevice(String str, MediaDevice mediaDevice) {
        boolean z = mediaDevice != null;
        String str2 = null;
        Drawable iconWithoutBackground = mediaDevice != null ? mediaDevice.getIconWithoutBackground() : null;
        if (mediaDevice != null) {
            str2 = mediaDevice.getName();
        }
        MediaDeviceData mediaDeviceData = new MediaDeviceData(z, iconWithoutBackground, str2);
        for (Listener listener : this.listeners) {
            listener.onMediaDeviceChanged(str, mediaDeviceData);
        }
    }

    /* compiled from: MediaDeviceManager.kt */
    /* access modifiers changed from: private */
    public final class Token implements LocalMediaManager.DeviceCallback {
        @Nullable
        private final MediaController controller;
        private MediaDevice current;
        @NotNull
        private final String key;
        @NotNull
        private final LocalMediaManager localMediaManager;
        private boolean started;
        final /* synthetic */ MediaDeviceManager this$0;

        public Token(@NotNull MediaDeviceManager mediaDeviceManager, @Nullable String str, @NotNull MediaController mediaController, LocalMediaManager localMediaManager) {
            Intrinsics.checkParameterIsNotNull(str, "key");
            Intrinsics.checkParameterIsNotNull(localMediaManager, "localMediaManager");
            this.this$0 = mediaDeviceManager;
            this.key = str;
            this.controller = mediaController;
            this.localMediaManager = localMediaManager;
        }

        @Nullable
        public final MediaSession.Token getToken() {
            MediaController mediaController = this.controller;
            if (mediaController != null) {
                return mediaController.getSessionToken();
            }
            return null;
        }

        private final void setCurrent(MediaDevice mediaDevice) {
            if (!this.started || (!Intrinsics.areEqual(mediaDevice, this.current))) {
                this.current = mediaDevice;
                this.this$0.processDevice(this.key, mediaDevice);
            }
        }

        public final void start() {
            this.localMediaManager.registerCallback(this);
            this.localMediaManager.startScan();
            updateCurrent();
            this.started = true;
        }

        public final void stop() {
            this.started = false;
            this.localMediaManager.stopScan();
            this.localMediaManager.unregisterCallback(this);
        }

        public final void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
            MediaController.PlaybackInfo playbackInfo;
            Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
            Intrinsics.checkParameterIsNotNull(printWriter, "pw");
            Intrinsics.checkParameterIsNotNull(strArr, "args");
            MediaController mediaController = this.controller;
            Integer num = null;
            RoutingSessionInfo routingSessionForMediaController = mediaController != null ? this.this$0.mr2manager.getRoutingSessionForMediaController(mediaController) : null;
            StringBuilder sb = new StringBuilder();
            sb.append("    current device is ");
            MediaDevice mediaDevice = this.current;
            sb.append(mediaDevice != null ? mediaDevice.getName() : null);
            printWriter.println(sb.toString());
            MediaController mediaController2 = this.controller;
            if (!(mediaController2 == null || (playbackInfo = mediaController2.getPlaybackInfo()) == null)) {
                num = Integer.valueOf(playbackInfo.getPlaybackType());
            }
            printWriter.println("    PlaybackType=" + num + " (1 for local, 2 for remote)");
            StringBuilder sb2 = new StringBuilder();
            sb2.append("    route=");
            sb2.append(routingSessionForMediaController);
            printWriter.println(sb2.toString());
        }

        @Override // com.android.settingslib.media.LocalMediaManager.DeviceCallback
        public void onDeviceListUpdate(@Nullable List<? extends MediaDevice> list) {
            this.this$0.fgExecutor.execute(new MediaDeviceManager$Token$onDeviceListUpdate$1(this));
        }

        @Override // com.android.settingslib.media.LocalMediaManager.DeviceCallback
        public void onSelectedDeviceStateChanged(@NotNull MediaDevice mediaDevice, int i) {
            Intrinsics.checkParameterIsNotNull(mediaDevice, "device");
            this.this$0.fgExecutor.execute(new MediaDeviceManager$Token$onSelectedDeviceStateChanged$1(this));
        }

        /* access modifiers changed from: private */
        public final void updateCurrent() {
            MediaDevice currentConnectedDevice = this.localMediaManager.getCurrentConnectedDevice();
            MediaController mediaController = this.controller;
            if (mediaController != null) {
                if (this.this$0.mr2manager.getRoutingSessionForMediaController(mediaController) == null) {
                    currentConnectedDevice = null;
                }
                setCurrent(currentConnectedDevice);
                return;
            }
            setCurrent(currentConnectedDevice);
        }
    }
}
