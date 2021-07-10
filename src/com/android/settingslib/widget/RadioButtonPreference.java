package com.android.settingslib.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;
public class RadioButtonPreference extends CheckBoxPreference {
    private View mAppendix;
    private int mAppendixVisibility = -1;
    private OnClickListener mListener = null;

    public interface OnClickListener {
        void onRadioButtonClicked(RadioButtonPreference radioButtonPreference);
    }

    public RadioButtonPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    @Override // androidx.preference.TwoStatePreference, androidx.preference.Preference
    public void onClick() {
        OnClickListener onClickListener = this.mListener;
        if (onClickListener != null) {
            onClickListener.onRadioButtonClicked(this);
        }
    }

    @Override // androidx.preference.CheckBoxPreference, androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        int i;
        super.onBindViewHolder(preferenceViewHolder);
        View findViewById = preferenceViewHolder.findViewById(R$id.summary_container);
        if (findViewById != null) {
            findViewById.setVisibility(TextUtils.isEmpty(getSummary()) ? 8 : 0);
            View findViewById2 = preferenceViewHolder.findViewById(R$id.appendix);
            this.mAppendix = findViewById2;
            if (!(findViewById2 == null || (i = this.mAppendixVisibility) == -1)) {
                findViewById2.setVisibility(i);
            }
        }
        TextView textView = (TextView) preferenceViewHolder.findViewById(16908310);
        if (textView != null) {
            textView.setSingleLine(false);
            textView.setMaxLines(3);
        }
    }

    private void init() {
        setWidgetLayoutResource(R$layout.preference_widget_radiobutton);
        setLayoutResource(R$layout.preference_radio);
        setIconSpaceReserved(false);
    }
}
