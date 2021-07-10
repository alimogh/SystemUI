package com.android.keyguard;

import android.content.res.ColorStateList;
public interface SecurityMessageDisplay {
    void setMessage(int i);

    default void setMessage(int i, int i2) {
    }

    void setMessage(CharSequence charSequence);

    default void setMessage(CharSequence charSequence, int i) {
    }

    void setNextMessageColor(ColorStateList colorStateList);
}
