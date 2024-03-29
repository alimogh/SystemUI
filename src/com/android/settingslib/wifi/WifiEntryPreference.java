package com.android.settingslib.wifi;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.wifi.WifiConfiguration;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.OpFeatures;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.android.settingslib.R$attr;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.R$string;
import com.android.settingslib.Utils;
import com.android.wifitrackerlib.WifiEntry;
public class WifiEntryPreference extends Preference implements WifiEntry.WifiEntryCallback, View.OnClickListener {
    private static final int[] FRICTION_ATTRS = {R$attr.wifi_friction};
    private static final int[] STATE_SECURED = {R$attr.state_encrypted};
    private static final int[] WIFI_CONNECTION_STRENGTH = {R$string.accessibility_no_wifi, R$string.accessibility_wifi_one_bar, R$string.accessibility_wifi_two_bars, R$string.accessibility_wifi_three_bars, R$string.accessibility_wifi_signal_full};
    private CharSequence mContentDescription;
    private final StateListDrawable mFrictionSld;
    private boolean mHe8ssCapableAp;
    private final IconInjector mIconInjector;
    private int mLevel = -1;
    private OnButtonClickListener mOnButtonClickListener;
    private boolean mVhtMax8SpatialStreamsSupport;
    private WifiEntry mWifiEntry;
    private int mWifiStandard;

    /* access modifiers changed from: package-private */
    public static class IconInjector {
        public abstract Drawable getIcon(int i, int i2, boolean z);
    }

    public interface OnButtonClickListener {
        void onButtonClick(WifiEntryPreference wifiEntryPreference);
    }

    WifiEntryPreference(Context context, WifiEntry wifiEntry, IconInjector iconInjector) {
        super(context);
        setLayoutResource(R$layout.preference_access_point);
        setWidgetLayoutResource(R$layout.access_point_friction_widget);
        this.mFrictionSld = getFrictionStateListDrawable();
        this.mWifiEntry = wifiEntry;
        wifiEntry.setListener(this);
        this.mIconInjector = iconInjector;
        refresh();
    }

    @Override // androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        Drawable icon = getIcon();
        if (icon != null) {
            icon.setLevel(this.mLevel);
        }
        preferenceViewHolder.itemView.setContentDescription(this.mContentDescription);
        preferenceViewHolder.findViewById(R$id.two_target_divider).setVisibility(4);
        ImageButton imageButton = (ImageButton) preferenceViewHolder.findViewById(R$id.icon_button);
        ImageView imageView = (ImageView) preferenceViewHolder.findViewById(R$id.friction_icon);
        if (this.mWifiEntry.getHelpUriString() == null || this.mWifiEntry.getConnectedState() != 0) {
            imageButton.setVisibility(8);
            if (imageView != null) {
                imageView.setVisibility(0);
                bindFrictionImage(imageView);
            }
        } else {
            Drawable drawable = getDrawable(R$drawable.ic_help);
            drawable.setTintList(Utils.getColorAttr(getContext(), 16843817));
            imageButton.setImageDrawable(drawable);
            imageButton.setVisibility(0);
            imageButton.setOnClickListener(this);
            imageButton.setContentDescription(getContext().getText(R$string.help_label));
            if (imageView != null) {
                imageView.setVisibility(8);
            }
        }
        ImageView imageView2 = (ImageView) preferenceViewHolder.findViewById(R$id.icon_passpoint);
        if (imageView2 != null && getContext() != null && this.mWifiEntry != null) {
            imageView2.setVisibility(0);
            boolean z = Settings.System.getInt(getContext().getContentResolver(), "oneplus_passpoint", 0) == 1;
            Log.d("WifiEntryPreference", "onBindViewHolder isPasspointEnabled = " + z);
            if (!OpFeatures.isSupport(new int[]{73}) || !z || this.mWifiEntry.getConnectedState() != 2) {
                imageView2.setVisibility(8);
                return;
            }
            Log.d("WifiEntryPreference", "onBindViewHolder isTmo mode and connectedState is connected");
            imageView2.setVisibility(0);
            WifiConfiguration wifiConfiguration = this.mWifiEntry.getWifiConfiguration();
            if (wifiConfiguration == null || !wifiConfiguration.isPasspoint()) {
                imageView2.setVisibility(8);
            } else if (wifiConfiguration.isHomeProviderNetwork) {
                Log.d("WifiEntryPreference", "onBindViewHolder config.isPasspoint is true, set R icon");
                imageView2.setImageDrawable(getDrawable(R$drawable.ic_passpoint_r));
            } else {
                Log.d("WifiEntryPreference", "onBindViewHolder config.isPasspoint is true, set H icon");
                imageView2.setImageDrawable(getDrawable(R$drawable.ic_passpoint_h));
            }
        }
    }

    public void refresh() {
        setTitle(this.mWifiEntry.getTitle());
        int level = this.mWifiEntry.getLevel();
        int wifiStandard = this.mWifiEntry.getWifiStandard();
        boolean isVhtMax8SpatialStreamsSupported = this.mWifiEntry.isVhtMax8SpatialStreamsSupported();
        boolean isHe8ssCapableAp = this.mWifiEntry.isHe8ssCapableAp();
        if (!(level == this.mLevel && wifiStandard == this.mWifiStandard && isHe8ssCapableAp == this.mHe8ssCapableAp && isVhtMax8SpatialStreamsSupported == this.mVhtMax8SpatialStreamsSupport)) {
            this.mLevel = level;
            this.mWifiStandard = wifiStandard;
            this.mHe8ssCapableAp = isHe8ssCapableAp;
            this.mVhtMax8SpatialStreamsSupport = isVhtMax8SpatialStreamsSupported;
            updateIcon(level, wifiStandard, isHe8ssCapableAp && isVhtMax8SpatialStreamsSupported);
            notifyChanged();
        }
        String summary = this.mWifiEntry.getSummary(false);
        if (this.mWifiEntry.isPskSaeTransitionMode()) {
            summary = "WPA3(SAE Transition Mode) " + summary;
        } else if (this.mWifiEntry.isOweTransitionMode()) {
            summary = "WPA3(OWE Transition Mode) " + summary;
        } else if (this.mWifiEntry.getSecurity() == 5) {
            summary = "WPA3(SAE) " + summary;
        } else if (this.mWifiEntry.getSecurity() == 4) {
            summary = "WPA3(OWE) " + summary;
        }
        setSummary(summary);
        this.mContentDescription = buildContentDescription();
    }

    @Override // com.android.wifitrackerlib.WifiEntry.WifiEntryCallback
    public void onUpdated() {
        refresh();
    }

    private void updateIcon(int i, int i2, boolean z) {
        if (i == -1) {
            setIcon((Drawable) null);
            return;
        }
        Drawable icon = this.mIconInjector.getIcon(i, i2, z);
        if (icon != null) {
            icon.setTintList(Utils.getColorAttr(getContext(), 16843817));
            setIcon(icon);
            return;
        }
        setIcon((Drawable) null);
    }

    private StateListDrawable getFrictionStateListDrawable() {
        TypedArray typedArray;
        try {
            typedArray = getContext().getTheme().obtainStyledAttributes(FRICTION_ATTRS);
        } catch (Resources.NotFoundException unused) {
            typedArray = null;
        }
        if (typedArray != null) {
            return (StateListDrawable) typedArray.getDrawable(0);
        }
        return null;
    }

    private void bindFrictionImage(ImageView imageView) {
        if (imageView != null && this.mFrictionSld != null) {
            if (!(this.mWifiEntry.getSecurity() == 0 || this.mWifiEntry.getSecurity() == 4)) {
                this.mFrictionSld.setState(STATE_SECURED);
            }
            imageView.setImageDrawable(this.mFrictionSld.getCurrent());
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x005e: APUT  (r2v3 java.lang.CharSequence[]), (2 ??[int, float, short, byte, char]), (r10v3 java.lang.String) */
    /* access modifiers changed from: package-private */
    public CharSequence buildContentDescription() {
        String str;
        Context context = getContext();
        CharSequence title = getTitle();
        CharSequence summary = getSummary();
        if (!TextUtils.isEmpty(summary)) {
            title = TextUtils.concat(title, ",", summary);
        }
        int level = this.mWifiEntry.getLevel();
        if (level >= 0) {
            int[] iArr = WIFI_CONNECTION_STRENGTH;
            if (level < iArr.length) {
                title = TextUtils.concat(title, ",", context.getString(iArr[level]));
            }
        }
        CharSequence[] charSequenceArr = new CharSequence[3];
        charSequenceArr[0] = title;
        charSequenceArr[1] = ",";
        if (this.mWifiEntry.getSecurity() == 0) {
            str = context.getString(R$string.accessibility_wifi_security_type_none);
        } else {
            str = context.getString(R$string.accessibility_wifi_security_type_secured);
        }
        charSequenceArr[2] = str;
        return TextUtils.concat(charSequenceArr);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        OnButtonClickListener onButtonClickListener;
        if (view.getId() == R$id.icon_button && (onButtonClickListener = this.mOnButtonClickListener) != null) {
            onButtonClickListener.onButtonClick(this);
        }
    }

    private Drawable getDrawable(int i) {
        try {
            return getContext().getDrawable(i);
        } catch (Resources.NotFoundException unused) {
            return null;
        }
    }
}
