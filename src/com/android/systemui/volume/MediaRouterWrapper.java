package com.android.systemui.volume;

import androidx.mediarouter.media.MediaRouter;
public class MediaRouterWrapper {
    private final MediaRouter mRouter;

    public MediaRouterWrapper(MediaRouter mediaRouter) {
        this.mRouter = mediaRouter;
    }

    public void removeCallback(MediaRouter.Callback callback) {
        this.mRouter.removeCallback(callback);
    }
}
