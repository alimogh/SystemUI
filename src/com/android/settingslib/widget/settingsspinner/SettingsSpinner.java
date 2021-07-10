package com.android.settingslib.widget.settingsspinner;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;
import com.android.settingslib.widget.R$drawable;
public class SettingsSpinner extends Spinner {
    public SettingsSpinner(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setBackgroundResource(R$drawable.settings_spinner_background);
    }
}
