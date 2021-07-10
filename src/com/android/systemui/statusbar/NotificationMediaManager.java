package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Trace;
import android.provider.DeviceConfig;
import android.util.ArraySet;
import android.util.Log;
import android.widget.ImageView;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.LockscreenWallpaper;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.concurrency.DelayableExecutor;
import com.oneplus.util.OpUtils;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
public class NotificationMediaManager implements Dumpable {
    public static final boolean DEBUG_MEDIA = OpUtils.DEBUG_ONEPLUS;
    private static final HashSet<Integer> PAUSED_MEDIA_STATES;
    private BackDropView mBackdrop;
    private ImageView mBackdropBack;
    private ImageView mBackdropFront;
    private BiometricUnlockController mBiometricUnlockController;
    private final SysuiColorExtractor mColorExtractor = ((SysuiColorExtractor) Dependency.get(SysuiColorExtractor.class));
    private final Context mContext;
    private final NotificationEntryManager mEntryManager;
    private boolean mHasMediaArtwork;
    private boolean mHasNotification;
    protected final Runnable mHideBackdropFront = new Runnable() { // from class: com.android.systemui.statusbar.NotificationMediaManager.4
        @Override // java.lang.Runnable
        public void run() {
            if (NotificationMediaManager.DEBUG_MEDIA) {
                Log.v("NotificationMediaManager", "DEBUG_MEDIA: removing fade layer");
            }
            NotificationMediaManager.this.mBackdropFront.setVisibility(4);
            NotificationMediaManager.this.mBackdropFront.animate().cancel();
            NotificationMediaManager.this.mBackdropFront.setImageDrawable(null);
        }
    };
    private final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardStateController mKeyguardStateController = ((KeyguardStateController) Dependency.get(KeyguardStateController.class));
    private LockscreenWallpaper mLockscreenWallpaper;
    private final DelayableExecutor mMainExecutor;
    private final MediaArtworkProcessor mMediaArtworkProcessor;
    private MediaController mMediaController;
    private boolean mMediaHostViewVisible;
    private final MediaController.Callback mMediaListener = new MediaController.Callback() { // from class: com.android.systemui.statusbar.NotificationMediaManager.2
        @Override // android.media.session.MediaController.Callback
        public void onPlaybackStateChanged(PlaybackState playbackState) {
            super.onPlaybackStateChanged(playbackState);
            if (NotificationMediaManager.DEBUG_MEDIA) {
                Log.v("NotificationMediaManager", "DEBUG_MEDIA: onPlaybackStateChanged: " + playbackState);
            }
            if (playbackState != null) {
                if (!NotificationMediaManager.this.isPlaybackActive(playbackState.getState())) {
                    NotificationMediaManager.this.clearCurrentMediaNotification();
                }
                NotificationMediaManager.this.mState = playbackState;
                NotificationMediaManager.this.findAndUpdateMediaNotifications();
            }
        }

        @Override // android.media.session.MediaController.Callback
        public void onMetadataChanged(MediaMetadata mediaMetadata) {
            super.onMetadataChanged(mediaMetadata);
            if (NotificationMediaManager.DEBUG_MEDIA) {
                Log.v("NotificationMediaManager", "DEBUG_MEDIA: onMetadataChanged: " + mediaMetadata);
            }
            NotificationMediaManager.this.mMediaArtworkProcessor.clearCache();
            NotificationMediaManager.this.mMediaMetadata = mediaMetadata;
            NotificationMediaManager.this.dispatchUpdateMediaMetaData(true, true);
        }
    };
    private final ArrayList<MediaListener> mMediaListeners;
    private MediaMetadata mMediaMetadata;
    private String mMediaNotificationKey;
    private final MediaSessionManager mMediaSessionManager;
    private Lazy<NotificationShadeWindowController> mNotificationShadeWindowController;
    protected NotificationPresenter mPresenter;
    private final Set<AsyncTask<?, ?, ?>> mProcessArtworkTasks = new ArraySet();
    private final DeviceConfig.OnPropertiesChangedListener mPropertiesChangedListener = new DeviceConfig.OnPropertiesChangedListener() { // from class: com.android.systemui.statusbar.NotificationMediaManager.1
        public void onPropertiesChanged(DeviceConfig.Properties properties) {
            for (String str : properties.getKeyset()) {
                if ("compact_media_notification_seekbar_enabled".equals(str)) {
                    String string = properties.getString(str, (String) null);
                    if (NotificationMediaManager.DEBUG_MEDIA) {
                        Log.v("NotificationMediaManager", "DEBUG_MEDIA: compact media seekbar flag updated: " + string);
                    }
                    NotificationMediaManager.this.mShowCompactMediaSeekbar = "true".equals(string);
                }
            }
        }
    };
    private ScrimController mScrimController;
    private boolean mShowCompactMediaSeekbar;
    private PlaybackState mState;
    private final Lazy<StatusBar> mStatusBarLazy;
    private final StatusBarStateController mStatusBarStateController = ((StatusBarStateController) Dependency.get(StatusBarStateController.class));

    public interface MediaListener {
        default void onPrimaryMetadataOrStateChanged(MediaMetadata mediaMetadata, int i) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPlaybackActive(int i) {
        return (i == 1 || i == 7 || i == 0) ? false : true;
    }

    static {
        HashSet<Integer> hashSet = new HashSet<>();
        PAUSED_MEDIA_STATES = hashSet;
        hashSet.add(0);
        PAUSED_MEDIA_STATES.add(1);
        PAUSED_MEDIA_STATES.add(2);
        PAUSED_MEDIA_STATES.add(7);
        PAUSED_MEDIA_STATES.add(8);
    }

    public NotificationMediaManager(Context context, Lazy<StatusBar> lazy, Lazy<NotificationShadeWindowController> lazy2, NotificationEntryManager notificationEntryManager, MediaArtworkProcessor mediaArtworkProcessor, KeyguardBypassController keyguardBypassController, DelayableExecutor delayableExecutor, DeviceConfigProxy deviceConfigProxy, final MediaDataManager mediaDataManager) {
        this.mContext = context;
        this.mMediaArtworkProcessor = mediaArtworkProcessor;
        this.mKeyguardBypassController = keyguardBypassController;
        this.mMediaListeners = new ArrayList<>();
        this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
        this.mStatusBarLazy = lazy;
        this.mNotificationShadeWindowController = lazy2;
        this.mEntryManager = notificationEntryManager;
        this.mMainExecutor = delayableExecutor;
        notificationEntryManager.addNotificationEntryListener(new NotificationEntryListener() { // from class: com.android.systemui.statusbar.NotificationMediaManager.3
            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPendingEntryAdded(NotificationEntry notificationEntry) {
                mediaDataManager.onNotificationAdded(notificationEntry.getKey(), notificationEntry.getSbn());
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPreEntryUpdated(NotificationEntry notificationEntry) {
                mediaDataManager.onNotificationAdded(notificationEntry.getKey(), notificationEntry.getSbn());
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryInflated(NotificationEntry notificationEntry) {
                NotificationMediaManager.this.findAndUpdateMediaNotifications();
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryReinflated(NotificationEntry notificationEntry) {
                NotificationMediaManager.this.findAndUpdateMediaNotifications();
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
                NotificationMediaManager.this.onNotificationRemoved(notificationEntry.getKey());
                mediaDataManager.onNotificationRemoved(notificationEntry.getKey());
            }
        });
        this.mShowCompactMediaSeekbar = "true".equals(DeviceConfig.getProperty("systemui", "compact_media_notification_seekbar_enabled"));
        deviceConfigProxy.addOnPropertiesChangedListener("systemui", this.mContext.getMainExecutor(), this.mPropertiesChangedListener);
    }

    public static boolean isPlayingState(int i) {
        return !PAUSED_MEDIA_STATES.contains(Integer.valueOf(i));
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter) {
        this.mPresenter = notificationPresenter;
    }

    public void onNotificationRemoved(String str) {
        if (str.equals(this.mMediaNotificationKey)) {
            clearCurrentMediaNotification();
            dispatchUpdateMediaMetaData(true, true);
        }
    }

    public String getMediaNotificationKey() {
        return this.mMediaNotificationKey;
    }

    public MediaMetadata getMediaMetadata() {
        return this.mMediaMetadata;
    }

    public boolean hasMediaArtwork() {
        return this.mHasMediaArtwork;
    }

    public boolean getShowCompactMediaSeekbar() {
        return this.mShowCompactMediaSeekbar;
    }

    public Icon getMediaIcon() {
        if (this.mMediaNotificationKey == null) {
            return null;
        }
        synchronized (this.mEntryManager) {
            NotificationEntry activeNotificationUnfiltered = this.mEntryManager.getActiveNotificationUnfiltered(this.mMediaNotificationKey);
            if (activeNotificationUnfiltered != null) {
                if (activeNotificationUnfiltered.getIcons().getShelfIcon() != null) {
                    return activeNotificationUnfiltered.getIcons().getShelfIcon().getSourceIcon();
                }
            }
            return null;
        }
    }

    public void addCallback(MediaListener mediaListener) {
        this.mMediaListeners.add(mediaListener);
        mediaListener.onPrimaryMetadataOrStateChanged(this.mMediaMetadata, getMediaControllerPlaybackState(this.mMediaController));
    }

    public void removeCallback(MediaListener mediaListener) {
        this.mMediaListeners.remove(mediaListener);
    }

    public void findAndUpdateMediaNotifications() {
        NotificationEntry notificationEntry;
        MediaController mediaController;
        boolean z;
        MediaSession.Token token;
        synchronized (this.mEntryManager) {
            Collection<NotificationEntry> allNotifs = this.mEntryManager.getAllNotifs();
            Iterator<NotificationEntry> it = allNotifs.iterator();
            while (true) {
                if (!it.hasNext()) {
                    notificationEntry = null;
                    mediaController = null;
                    break;
                }
                notificationEntry = it.next();
                if (notificationEntry.isMediaNotification() && (token = (MediaSession.Token) notificationEntry.getSbn().getNotification().extras.getParcelable("android.mediaSession")) != null) {
                    mediaController = new MediaController(this.mContext, token);
                    if (3 == getMediaControllerPlaybackState(mediaController)) {
                        if (DEBUG_MEDIA) {
                            Log.v("NotificationMediaManager", "DEBUG_MEDIA: found mediastyle controller matching " + notificationEntry.getSbn().getKey());
                        }
                    }
                }
            }
            if (notificationEntry == null && this.mMediaSessionManager != null) {
                for (MediaController mediaController2 : this.mMediaSessionManager.getActiveSessionsForUser(null, -1)) {
                    if (DEBUG_MEDIA && mediaController2 != null) {
                        Log.d("NotificationMediaManager", "DEBUG_MEDIA: packageName=" + mediaController2.getPackageName() + " playbackState=" + getMediaControllerPlaybackState(mediaController2));
                    }
                    if (3 == getMediaControllerPlaybackState(mediaController2) || 8 == getMediaControllerPlaybackState(mediaController2)) {
                        String packageName = mediaController2.getPackageName();
                        Iterator<NotificationEntry> it2 = allNotifs.iterator();
                        while (true) {
                            if (!it2.hasNext()) {
                                break;
                            }
                            NotificationEntry next = it2.next();
                            if (next.getSbn().getPackageName().equals(packageName)) {
                                if (DEBUG_MEDIA) {
                                    Log.v("NotificationMediaManager", "DEBUG_MEDIA: found controller matching " + next.getSbn().getKey());
                                }
                                mediaController = mediaController2;
                                notificationEntry = next;
                            }
                        }
                    }
                }
            }
            if (mediaController == null || sameSessions(this.mMediaController, mediaController)) {
                z = false;
            } else {
                clearCurrentMediaNotificationSession();
                this.mMediaController = mediaController;
                mediaController.registerCallback(this.mMediaListener);
                this.mMediaMetadata = this.mMediaController.getMetadata();
                if (DEBUG_MEDIA) {
                    Log.v("NotificationMediaManager", "DEBUG_MEDIA: insert listener, found new controller: " + this.mMediaController + ", receive metadata: " + this.mMediaMetadata);
                }
                z = true;
            }
            if (notificationEntry != null && !notificationEntry.getSbn().getKey().equals(this.mMediaNotificationKey)) {
                this.mMediaNotificationKey = notificationEntry.getSbn().getKey();
                if (DEBUG_MEDIA) {
                    Log.v("NotificationMediaManager", "DEBUG_MEDIA: Found new media notification: key=" + this.mMediaNotificationKey);
                }
            }
        }
        if (z) {
            this.mEntryManager.updateNotifications("NotificationMediaManager - metaDataChanged");
            MediaController mediaController3 = this.mMediaController;
            if (mediaController3 != null) {
                this.mState = mediaController3.getPlaybackState();
                if (DEBUG_MEDIA) {
                    Log.d("NotificationMediaManager", "metaDataChanged: mState = " + this.mState.getState());
                }
            }
        }
        dispatchUpdateMediaMetaData(z, true);
    }

    public void clearCurrentMediaNotification() {
        this.mMediaNotificationKey = null;
        clearCurrentMediaNotificationSession();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchUpdateMediaMetaData(boolean z, boolean z2) {
        NotificationPresenter notificationPresenter = this.mPresenter;
        if (notificationPresenter != null) {
            notificationPresenter.updateMediaMetaData(z, z2);
        }
        int mediaControllerPlaybackState = getMediaControllerPlaybackState(this.mMediaController);
        ArrayList arrayList = new ArrayList(this.mMediaListeners);
        for (int i = 0; i < arrayList.size(); i++) {
            ((MediaListener) arrayList.get(i)).onPrimaryMetadataOrStateChanged(this.mMediaMetadata, mediaControllerPlaybackState);
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("    mMediaSessionManager=");
        printWriter.println(this.mMediaSessionManager);
        printWriter.print("    mMediaNotificationKey=");
        printWriter.println(this.mMediaNotificationKey);
        printWriter.print("    mMediaController=");
        printWriter.print(this.mMediaController);
        if (this.mMediaController != null) {
            printWriter.print(" state=" + this.mMediaController.getPlaybackState());
        }
        printWriter.println();
        printWriter.print("    mMediaMetadata=");
        printWriter.print(this.mMediaMetadata);
        if (this.mMediaMetadata != null) {
            printWriter.print(" title=" + ((Object) this.mMediaMetadata.getText("android.media.metadata.TITLE")));
        }
        printWriter.println();
    }

    private boolean sameSessions(MediaController mediaController, MediaController mediaController2) {
        if (mediaController == mediaController2) {
            return true;
        }
        if (mediaController == null) {
            return false;
        }
        return mediaController.controlsSameSession(mediaController2);
    }

    private int getMediaControllerPlaybackState(MediaController mediaController) {
        PlaybackState playbackState;
        if (mediaController == null || (playbackState = mediaController.getPlaybackState()) == null) {
            return 0;
        }
        return playbackState.getState();
    }

    private void clearCurrentMediaNotificationSession() {
        this.mMediaArtworkProcessor.clearCache();
        this.mMediaMetadata = null;
        if (this.mMediaController != null) {
            if (DEBUG_MEDIA) {
                Log.v("NotificationMediaManager", "DEBUG_MEDIA: Disconnecting from old controller: " + this.mMediaController.getPackageName());
            }
            this.mMediaController.unregisterCallback(this.mMediaListener);
        }
        this.mMediaController = null;
        this.mState = null;
        this.mHasNotification = false;
        this.mMediaHostViewVisible = false;
    }

    public void updateMediaMetaData(boolean z, boolean z2) {
        Bitmap bitmap;
        Trace.beginSection("StatusBar#updateMediaMetaData");
        if (this.mBackdrop == null) {
            Trace.endSection();
            return;
        }
        BiometricUnlockController biometricUnlockController = this.mBiometricUnlockController;
        boolean z3 = biometricUnlockController != null && biometricUnlockController.isWakeAndUnlock();
        boolean isFacelockUnlocking = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFacelockUnlocking();
        if (this.mKeyguardStateController.isLaunchTransitionFadingAway() || z3 || isFacelockUnlocking) {
            if (DEBUG_MEDIA) {
                Log.i("NotificationMediaManager", "wakeAndUnlock:" + z3 + "isFacelockUnlocking:" + isFacelockUnlocking);
            }
            this.mBackdrop.setVisibility(4);
            Trace.endSection();
            return;
        }
        MediaMetadata mediaMetadata = getMediaMetadata();
        if (DEBUG_MEDIA) {
            Log.v("NotificationMediaManager", "DEBUG_MEDIA: updating album art for notification " + getMediaNotificationKey() + " metadata=" + mediaMetadata + " metaDataChanged=" + z + " state=" + this.mStatusBarStateController.getState());
        }
        if (mediaMetadata == null || this.mKeyguardBypassController.getBypassEnabled()) {
            bitmap = null;
        } else {
            if (DEBUG_MEDIA) {
                Log.d("NotificationMediaManager", "Try to get artworkBitmap from METADATA_KEY_ART");
            }
            bitmap = mediaMetadata.getBitmap("android.media.metadata.ART");
            if (bitmap == null) {
                if (DEBUG_MEDIA) {
                    Log.d("NotificationMediaManager", "Try to get artworkBitmap from METADATA_KEY_ALBUM_ART");
                }
                bitmap = mediaMetadata.getBitmap("android.media.metadata.ALBUM_ART");
            }
        }
        if (z) {
            for (AsyncTask<?, ?, ?> asyncTask : this.mProcessArtworkTasks) {
                asyncTask.cancel(true);
            }
            this.mProcessArtworkTasks.clear();
        }
        if (bitmap != null) {
            this.mProcessArtworkTasks.add(new ProcessArtworkTask(this, z, z2).execute(bitmap));
        } else {
            if (DEBUG_MEDIA) {
                Log.d("NotificationMediaManager", "artworkBitmap is null, remove music album art");
            }
            finishUpdateMediaMetaData(z, z2, null);
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:114:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0085  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0087  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0115  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x013f  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0174  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void finishUpdateMediaMetaData(boolean r13, boolean r14, android.graphics.Bitmap r15) {
        /*
        // Method dump skipped, instructions count: 702
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.NotificationMediaManager.finishUpdateMediaMetaData(boolean, boolean, android.graphics.Bitmap):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$finishUpdateMediaMetaData$0 */
    public /* synthetic */ void lambda$finishUpdateMediaMetaData$0$NotificationMediaManager() {
        this.mBackdrop.setVisibility(8);
        this.mBackdropFront.animate().cancel();
        this.mBackdropBack.setImageDrawable(null);
        this.mMainExecutor.execute(this.mHideBackdropFront);
    }

    public void setup(BackDropView backDropView, ImageView imageView, ImageView imageView2, ScrimController scrimController, LockscreenWallpaper lockscreenWallpaper) {
        this.mBackdrop = backDropView;
        this.mBackdropFront = imageView;
        this.mBackdropBack = imageView2;
        this.mScrimController = scrimController;
        this.mLockscreenWallpaper = lockscreenWallpaper;
    }

    public void setBiometricUnlockController(BiometricUnlockController biometricUnlockController) {
        this.mBiometricUnlockController = biometricUnlockController;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Bitmap processArtwork(Bitmap bitmap) {
        return this.mMediaArtworkProcessor.processArtwork(this.mContext, bitmap);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeTask(AsyncTask<?, ?, ?> asyncTask) {
        this.mProcessArtworkTasks.remove(asyncTask);
    }

    /* access modifiers changed from: private */
    public static final class ProcessArtworkTask extends AsyncTask<Bitmap, Void, Bitmap> {
        private final boolean mAllowEnterAnimation;
        private final WeakReference<NotificationMediaManager> mManagerRef;
        private final boolean mMetaDataChanged;

        ProcessArtworkTask(NotificationMediaManager notificationMediaManager, boolean z, boolean z2) {
            this.mManagerRef = new WeakReference<>(notificationMediaManager);
            this.mMetaDataChanged = z;
            this.mAllowEnterAnimation = z2;
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(Bitmap... bitmapArr) {
            NotificationMediaManager notificationMediaManager = this.mManagerRef.get();
            if (notificationMediaManager == null || bitmapArr.length == 0 || isCancelled()) {
                return null;
            }
            return notificationMediaManager.processArtwork(bitmapArr[0]);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            NotificationMediaManager notificationMediaManager = this.mManagerRef.get();
            if (notificationMediaManager != null && !isCancelled()) {
                notificationMediaManager.removeTask(this);
                notificationMediaManager.finishUpdateMediaMetaData(this.mMetaDataChanged, this.mAllowEnterAnimation, bitmap);
            }
        }

        /* access modifiers changed from: protected */
        public void onCancelled(Bitmap bitmap) {
            if (bitmap != null) {
                bitmap.recycle();
            }
            NotificationMediaManager notificationMediaManager = this.mManagerRef.get();
            if (notificationMediaManager != null) {
                notificationMediaManager.removeTask(this);
            }
        }
    }

    public void onMediaHeaderViewChanged(int i) {
        if (DEBUG_MEDIA) {
            Log.d("NotificationMediaManager", "MediaHeaderView new visibility: " + i + ", has keyguard music notification: " + this.mHasNotification);
        }
        if (i == 8 && this.mHasNotification) {
            this.mHasNotification = false;
            PlaybackState playbackState = this.mState;
            if (playbackState != null && playbackState.getState() == 2 && this.mStatusBarStateController.getState() == 1) {
                finishUpdateMediaMetaData(true, true, null);
            }
        } else if (i == 0 && !this.mHasNotification) {
            this.mHasNotification = true;
        }
    }

    public void onMediaHostVisibilityChanged(int i) {
        if (DEBUG_MEDIA) {
            Log.d("NotificationMediaManager", "MediaHostView - new visibility: " + i + ", media host visible: " + this.mMediaHostViewVisible);
        }
        if (i == 8 && this.mMediaHostViewVisible) {
            this.mMediaHostViewVisible = false;
            PlaybackState playbackState = this.mState;
            if (playbackState != null && playbackState.getState() == 2) {
                finishUpdateMediaMetaData(true, true, null);
            }
        } else if (i == 0 && !this.mMediaHostViewVisible) {
            this.mMediaHostViewVisible = true;
        }
    }
}
