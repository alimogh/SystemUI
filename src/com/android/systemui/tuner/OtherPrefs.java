package com.android.systemui.tuner;

import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import com.android.systemui.C0017R$xml;
public class OtherPrefs extends PreferenceFragment {
    @Override // androidx.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        addPreferencesFromResource(C0017R$xml.other_settings);
    }
}
