package com.android.settingslib.wifi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.OpFeatures;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.android.settingslib.R$attr;
import com.android.settingslib.R$dimen;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.R$string;
public class AccessPointPreference extends Preference {
    private static final int[] STATE_METERED = {R$attr.state_metered};
    private static final int[] STATE_SECURED = {R$attr.state_encrypted};
    private static final int[] WIFI_CONNECTION_STRENGTH = {R$string.accessibility_no_wifi, R$string.accessibility_wifi_one_bar, R$string.accessibility_wifi_two_bars, R$string.accessibility_wifi_three_bars, R$string.accessibility_wifi_signal_full};
    private AccessPoint mAccessPoint;
    private Drawable mBadge;
    private final int mBadgePadding;
    private CharSequence mContentDescription;
    private Context mContext;
    private final StateListDrawable mFrictionSld;
    private int mLevel;
    private final Runnable mNotifyChanged;
    private boolean mShowDivider;
    private TextView mTitleView;

    static class IconInjector {
    }

    public static class UserBadgeCache {
    }

    public AccessPointPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mNotifyChanged = new Runnable() { // from class: com.android.settingslib.wifi.AccessPointPreference.1
            @Override // java.lang.Runnable
            public void run() {
                AccessPointPreference.this.notifyChanged();
            }
        };
        this.mFrictionSld = null;
        this.mBadgePadding = 0;
        this.mContext = context;
    }

    AccessPointPreference(AccessPoint accessPoint, Context context, UserBadgeCache userBadgeCache, int i, boolean z, StateListDrawable stateListDrawable, int i2, IconInjector iconInjector) {
        super(context);
        this.mNotifyChanged = new Runnable() { // from class: com.android.settingslib.wifi.AccessPointPreference.1
            @Override // java.lang.Runnable
            public void run() {
                AccessPointPreference.this.notifyChanged();
            }
        };
        setLayoutResource(R$layout.preference_access_point);
        setWidgetLayoutResource(getWidgetLayoutResourceId());
        this.mAccessPoint = accessPoint;
        accessPoint.setTag(this);
        this.mLevel = i2;
        this.mFrictionSld = stateListDrawable;
        this.mBadgePadding = context.getResources().getDimensionPixelSize(R$dimen.wifi_preference_badge_padding);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public int getWidgetLayoutResourceId() {
        return R$layout.access_point_friction_widget;
    }

    @Override // androidx.preference.Preference
    public void onBindViewHolder(PreferenceViewHolder preferenceViewHolder) {
        super.onBindViewHolder(preferenceViewHolder);
        if (this.mAccessPoint != null) {
            Drawable icon = getIcon();
            if (icon != null) {
                icon.setLevel(this.mLevel);
            }
            TextView textView = (TextView) preferenceViewHolder.findViewById(16908310);
            this.mTitleView = textView;
            if (textView != null) {
                textView.setCompoundDrawablesRelativeWithIntrinsicBounds((Drawable) null, (Drawable) null, this.mBadge, (Drawable) null);
                this.mTitleView.setCompoundDrawablePadding(this.mBadgePadding);
            }
            preferenceViewHolder.itemView.setContentDescription(this.mContentDescription);
            bindFrictionImage((ImageView) preferenceViewHolder.findViewById(R$id.friction_icon));
            ImageView imageView = (ImageView) preferenceViewHolder.findViewById(R$id.icon_passpoint);
            int i = 0;
            boolean z = Settings.System.getInt(this.mContext.getContentResolver(), "oneplus_passpoint", 0) == 1;
            if (!OpFeatures.isSupport(new int[]{73}) || !z || this.mAccessPoint.getDetailedState() != NetworkInfo.DetailedState.CONNECTED) {
                imageView.setVisibility(8);
            } else {
                imageView.setVisibility(0);
                WifiConfiguration config = this.mAccessPoint.getConfig();
                if (config == null || !config.isPasspoint()) {
                    imageView.setVisibility(8);
                } else if (config.isHomeProviderNetwork) {
                    imageView.setImageDrawable(this.mContext.getResources().getDrawable(R$drawable.ic_passpoint_r));
                } else {
                    imageView.setImageDrawable(this.mContext.getResources().getDrawable(R$drawable.ic_passpoint_h));
                }
            }
            View findViewById = preferenceViewHolder.findViewById(R$id.two_target_divider);
            if (!shouldShowDivider()) {
                i = 4;
            }
            findViewById.setVisibility(i);
        }
    }

    public boolean shouldShowDivider() {
        return this.mShowDivider;
    }

    private void bindFrictionImage(ImageView imageView) {
        if (imageView != null && this.mFrictionSld != null) {
            if (this.mAccessPoint.getSecurity() != 0 && this.mAccessPoint.getSecurity() != 4) {
                this.mFrictionSld.setState(STATE_SECURED);
            } else if (this.mAccessPoint.isMetered()) {
                this.mFrictionSld.setState(STATE_METERED);
            }
            imageView.setImageDrawable(this.mFrictionSld.getCurrent());
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.preference.Preference
    public void notifyChanged() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            postNotifyChanged();
        } else {
            super.notifyChanged();
        }
    }

    static void setTitle(AccessPointPreference accessPointPreference, AccessPoint accessPoint) {
        accessPointPreference.setTitle(accessPoint.getTitle());
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0056: APUT  (r9v3 java.lang.CharSequence[]), (2 ??[int, float, short, byte, char]), (r8v1 java.lang.String) */
    static CharSequence buildContentDescription(Context context, Preference preference, AccessPoint accessPoint) {
        String str;
        CharSequence title = preference.getTitle();
        CharSequence summary = preference.getSummary();
        if (!TextUtils.isEmpty(summary)) {
            title = TextUtils.concat(title, ",", summary);
        }
        int level = accessPoint.getLevel();
        if (level >= 0) {
            int[] iArr = WIFI_CONNECTION_STRENGTH;
            if (level < iArr.length) {
                title = TextUtils.concat(title, ",", context.getString(iArr[level]));
            }
        }
        CharSequence[] charSequenceArr = new CharSequence[3];
        charSequenceArr[0] = title;
        charSequenceArr[1] = ",";
        if (accessPoint.getSecurity() == 0) {
            str = context.getString(R$string.accessibility_wifi_security_type_none);
        } else {
            str = context.getString(R$string.accessibility_wifi_security_type_secured);
        }
        charSequenceArr[2] = str;
        return TextUtils.concat(charSequenceArr);
    }

    private void postNotifyChanged() {
        TextView textView = this.mTitleView;
        if (textView != null) {
            textView.post(this.mNotifyChanged);
        }
    }
}
