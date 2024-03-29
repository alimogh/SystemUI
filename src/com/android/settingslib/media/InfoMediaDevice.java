package com.android.settingslib.media;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaRoute2Info;
import android.media.MediaRouter2Manager;
import com.android.settingslib.R$drawable;
import com.android.settingslib.bluetooth.BluetoothUtils;
import java.util.List;
public class InfoMediaDevice extends MediaDevice {
    @Override // com.android.settingslib.media.MediaDevice
    public boolean isConnected() {
        return true;
    }

    InfoMediaDevice(Context context, MediaRouter2Manager mediaRouter2Manager, MediaRoute2Info mediaRoute2Info, String str) {
        super(context, mediaRouter2Manager, mediaRoute2Info, str);
        initDeviceRecord();
    }

    @Override // com.android.settingslib.media.MediaDevice
    public String getName() {
        return this.mRouteInfo.getName().toString();
    }

    @Override // com.android.settingslib.media.MediaDevice
    public Drawable getIcon() {
        Drawable iconWithoutBackground = getIconWithoutBackground();
        setColorFilter(iconWithoutBackground);
        return BluetoothUtils.buildAdvancedDrawable(this.mContext, iconWithoutBackground);
    }

    @Override // com.android.settingslib.media.MediaDevice
    public Drawable getIconWithoutBackground() {
        return this.mContext.getDrawable(getDrawableResIdByFeature());
    }

    /* access modifiers changed from: package-private */
    public int getDrawableResId() {
        int type = this.mRouteInfo.getType();
        if (type == 1001) {
            return R$drawable.ic_media_display_device;
        }
        if (type != 2000) {
            return R$drawable.ic_media_speaker_device;
        }
        return R$drawable.ic_media_group_device;
    }

    /* access modifiers changed from: package-private */
    public int getDrawableResIdByFeature() {
        List<String> features = this.mRouteInfo.getFeatures();
        if (features.contains("android.media.route.feature.REMOTE_GROUP_PLAYBACK")) {
            return R$drawable.ic_media_group_device;
        }
        if (features.contains("android.media.route.feature.REMOTE_VIDEO_PLAYBACK")) {
            return R$drawable.ic_media_display_device;
        }
        return R$drawable.ic_media_speaker_device;
    }

    @Override // com.android.settingslib.media.MediaDevice
    public String getId() {
        return MediaDeviceUtils.getId(this.mRouteInfo);
    }
}
