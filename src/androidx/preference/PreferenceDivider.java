package androidx.preference;

import android.content.Context;
import android.util.AttributeSet;
public class PreferenceDivider extends PreferenceCategory {
    public PreferenceDivider(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initViews(context);
    }

    private void initViews(Context context) {
        setLayoutResource(R$layout.preference_divider);
    }
}
