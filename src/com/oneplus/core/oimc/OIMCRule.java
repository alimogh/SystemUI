package com.oneplus.core.oimc;

import android.os.Parcel;
import android.os.Parcelable;
public class OIMCRule implements Parcelable {
    public static final Parcelable.Creator<OIMCRule> CREATOR = new Parcelable.Creator<OIMCRule>() { // from class: com.oneplus.core.oimc.OIMCRule.1
        @Override // android.os.Parcelable.Creator
        public OIMCRule createFromParcel(Parcel parcel) {
            return new OIMCRule(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public OIMCRule[] newArray(int i) {
            return new OIMCRule[i];
        }
    };
    public static final OIMCRule RULE_DISABLE_HEADSUPNOTIFICATION = new OIMCRule("HeadsUpNotification", 1, new String[]{"GameMode"}, 0);
    public static final OIMCRule RULE_DISABLE_HEADSUPNOTIFICATION_ZEN = new OIMCRule("HeadsUpNotificationZen", 1, new String[]{"ZenMode"}, 0);
    private int mAction;
    private String[] mDifferenceSwitch;
    private String mFuncName;
    private int mLevel;
    private int mReserved;
    private String[] mTriggerModes;

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public OIMCRule(String str, int i, String[] strArr, int i2) {
        this.mLevel = 50;
        this.mFuncName = str;
        this.mAction = i;
        this.mTriggerModes = strArr;
        this.mReserved = i2;
        this.mLevel = 50;
        this.mDifferenceSwitch = null;
    }

    private OIMCRule(Parcel parcel) {
        this.mLevel = 50;
        readFromParcel(parcel);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mFuncName);
        parcel.writeInt(this.mAction);
        parcel.writeStringArray(this.mTriggerModes);
        parcel.writeInt(this.mReserved);
        parcel.writeInt(this.mLevel);
        parcel.writeStringArray(this.mDifferenceSwitch);
    }

    public void readFromParcel(Parcel parcel) {
        this.mFuncName = parcel.readString();
        this.mAction = parcel.readInt();
        this.mTriggerModes = parcel.readStringArray();
        this.mReserved = parcel.readInt();
        this.mLevel = parcel.readInt();
        this.mDifferenceSwitch = parcel.readStringArray();
    }
}
