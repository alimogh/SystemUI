package com.android.settingslib.drawer;

import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import java.util.ArrayList;
public abstract class Tile implements Parcelable {
    public static final Parcelable.Creator<Tile> CREATOR = new Parcelable.Creator<Tile>() { // from class: com.android.settingslib.drawer.Tile.1
        @Override // android.os.Parcelable.Creator
        public Tile createFromParcel(Parcel parcel) {
            boolean readBoolean = parcel.readBoolean();
            parcel.setDataPosition(0);
            return readBoolean ? new ProviderTile(parcel) : new ActivityTile(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Tile[] newArray(int i) {
            return new Tile[i];
        }
    };
    private String mCategory;
    protected ComponentInfo mComponentInfo;
    private final String mComponentName;
    private final String mComponentPackage;
    long mLastUpdateTime;
    private Bundle mMetaData;
    public ArrayList<UserHandle> userHandle = new ArrayList<>();

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    Tile(Parcel parcel) {
        parcel.readBoolean();
        this.mComponentPackage = parcel.readString();
        this.mComponentName = parcel.readString();
        new Intent().setClassName(this.mComponentPackage, this.mComponentName);
        int readInt = parcel.readInt();
        for (int i = 0; i < readInt; i++) {
            this.userHandle.add((UserHandle) UserHandle.CREATOR.createFromParcel(parcel));
        }
        this.mCategory = parcel.readString();
        this.mMetaData = parcel.readBundle();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBoolean(this instanceof ProviderTile);
        parcel.writeString(this.mComponentPackage);
        parcel.writeString(this.mComponentName);
        int size = this.userHandle.size();
        parcel.writeInt(size);
        for (int i2 = 0; i2 < size; i2++) {
            this.userHandle.get(i2).writeToParcel(parcel, i);
        }
        parcel.writeString(this.mCategory);
        parcel.writeBundle(this.mMetaData);
    }

    public int getOrder() {
        if (hasOrder()) {
            return this.mMetaData.getInt("com.android.settings.order");
        }
        return 0;
    }

    public boolean hasOrder() {
        return this.mMetaData.containsKey("com.android.settings.order") && (this.mMetaData.get("com.android.settings.order") instanceof Integer);
    }

    public Bundle getMetaData() {
        return this.mMetaData;
    }

    static {
        $$Lambda$Tile$5_ETnVHzVG6DF0RKPoy76eRIQM r0 = $$Lambda$Tile$5_ETnVHzVG6DF0RKPoy76eRIQM.INSTANCE;
    }

    static /* synthetic */ int lambda$static$0(Tile tile, Tile tile2) {
        return tile2.getOrder() - tile.getOrder();
    }
}
