package com.oneplus.keyguard;

import android.content.res.ColorStateList;
public class OpBounerMessageAreaInfo {
    private ColorStateList mColorStateList;
    private String mDisplayMessage;
    private int mType = 0;

    public String getDisplayMessage() {
        return this.mDisplayMessage;
    }

    public int getType() {
        return this.mType;
    }

    public void setDisplayMessage(String str) {
        this.mDisplayMessage = str;
    }

    public void setType(int i) {
        this.mType = i;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mDisplayMessage:");
        sb.append(this.mDisplayMessage);
        sb.append(" mColorStateList:");
        ColorStateList colorStateList = this.mColorStateList;
        sb.append(colorStateList != null ? colorStateList.toString() : "null");
        sb.append(" mType:");
        sb.append(this.mType);
        return sb.toString();
    }
}
