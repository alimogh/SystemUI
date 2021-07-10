package com.android.settingslib;

import android.content.Context;
import android.util.AttributeSet;
import androidx.preference.EditTextPreference;
public class CustomEditTextPreferenceCompat extends EditTextPreference {
    public CustomEditTextPreferenceCompat(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}
