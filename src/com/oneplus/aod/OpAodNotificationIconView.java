package com.oneplus.aod;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.C0003R$bool;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0015R$string;
import com.android.systemui.statusbar.AnimatedImageView;
import java.text.NumberFormat;
public class OpAodNotificationIconView extends AnimatedImageView {
    private boolean mAlwaysScaleIcon;
    private final boolean mBlocked;
    private int mDensity;
    private StatusBarIcon mIcon;
    private Notification mNotification;
    private Drawable mNumberBackground;
    private Paint mNumberPain;
    private String mNumberText;
    private int mNumberX;
    private int mNumberY;
    @ViewDebug.ExportedProperty
    private String mSlot;

    private void updateIconScale() {
    }

    public OpAodNotificationIconView(Context context, String str, Notification notification) {
        this(context, str, notification, false);
    }

    public OpAodNotificationIconView(Context context, String str, Notification notification, boolean z) {
        super(context);
        this.mBlocked = z;
        this.mSlot = str;
        Paint paint = new Paint();
        this.mNumberPain = paint;
        paint.setTextAlign(Paint.Align.CENTER);
        this.mNumberPain.setColor(context.getColor(C0006R$drawable.notification_number_text_color));
        this.mNumberPain.setAntiAlias(true);
        setNotification(notification);
        maybeUpdateIconScale();
        setScaleType(ImageView.ScaleType.FIT_CENTER);
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
    }

    private void maybeUpdateIconScale() {
        if (this.mNotification != null || this.mAlwaysScaleIcon) {
            updateIconScale();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = configuration.densityDpi;
        if (i != this.mDensity) {
            this.mDensity = i;
            maybeUpdateIconScale();
            updateDrawable();
        }
    }

    public void setNotification(Notification notification) {
        this.mNotification = notification;
        setContentDescription(notification);
    }

    public OpAodNotificationIconView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mBlocked = false;
        this.mAlwaysScaleIcon = true;
        updateIconScale();
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
    }

    public boolean equalIcons(Icon icon, Icon icon2) {
        if (icon == icon2) {
            return true;
        }
        if (icon.getType() != icon2.getType()) {
            return false;
        }
        int type = icon.getType();
        if (type == 2) {
            return icon.getResPackage().equals(icon2.getResPackage()) && icon.getResId() == icon2.getResId();
        }
        if (type != 4) {
            return false;
        }
        return icon.getUriString().equals(icon2.getUriString());
    }

    public boolean set(StatusBarIcon statusBarIcon) {
        StatusBarIcon statusBarIcon2 = this.mIcon;
        int i = 0;
        boolean z = statusBarIcon2 != null && equalIcons(statusBarIcon2.icon, statusBarIcon.icon);
        boolean z2 = z && this.mIcon.iconLevel == statusBarIcon.iconLevel;
        StatusBarIcon statusBarIcon3 = this.mIcon;
        boolean z3 = statusBarIcon3 != null && statusBarIcon3.visible == statusBarIcon.visible;
        StatusBarIcon statusBarIcon4 = this.mIcon;
        boolean z4 = statusBarIcon4 != null && statusBarIcon4.number == statusBarIcon.number;
        this.mIcon = statusBarIcon.clone();
        setContentDescription(statusBarIcon.contentDescription);
        if (!z && !updateDrawable(false)) {
            return false;
        }
        if (!z2) {
            setImageLevel(statusBarIcon.iconLevel);
        }
        if (!z4) {
            if (statusBarIcon.number <= 0 || !getContext().getResources().getBoolean(C0003R$bool.config_statusBarShowNumber)) {
                this.mNumberBackground = null;
                this.mNumberText = null;
            } else {
                if (this.mNumberBackground == null) {
                    this.mNumberBackground = getContext().getResources().getDrawable(C0006R$drawable.ic_notification_overlay);
                }
                placeNumber();
            }
            invalidate();
        }
        if (!z3) {
            if (Build.DEBUG_ONEPLUS) {
                Log.i("StatusBarIconView", "set mSlot:" + this.mSlot + " icon.visible:" + statusBarIcon.visible + " mBlocked:" + this.mBlocked);
            }
            if (!statusBarIcon.visible || this.mBlocked) {
                i = 8;
            }
            setVisibility(i);
        }
        return true;
    }

    public void updateDrawable() {
        updateDrawable(true);
    }

    private boolean updateDrawable(boolean z) {
        StatusBarIcon statusBarIcon = this.mIcon;
        if (statusBarIcon == null) {
            Log.w("StatusBarIconView", "No icon" + this.mSlot);
            return false;
        }
        Drawable icon = getIcon(statusBarIcon);
        if (icon == null) {
            Log.w("StatusBarIconView", "No icon for slot " + this.mSlot);
            return false;
        }
        if (z) {
            setImageDrawable(null);
        }
        setImageDrawable(icon);
        return true;
    }

    private Drawable getIcon(StatusBarIcon statusBarIcon) {
        return getIcon(getContext(), statusBarIcon);
    }

    public static Drawable getIcon(Context context, StatusBarIcon statusBarIcon) {
        int identifier = statusBarIcon.user.getIdentifier();
        if (identifier == -1) {
            identifier = 0;
        }
        Drawable loadDrawableAsUser = statusBarIcon.icon.loadDrawableAsUser(context, identifier);
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(C0005R$dimen.status_bar_icon_scale_factor, typedValue, true);
        float f = typedValue.getFloat();
        if (f == 1.0f) {
            return loadDrawableAsUser;
        }
        return new OpScalingDrawableWrapper(loadDrawableAsUser, f);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        Notification notification = this.mNotification;
        if (notification != null) {
            accessibilityEvent.setParcelableData(notification);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (this.mNumberBackground != null) {
            placeNumber();
        }
    }

    @Override // android.widget.ImageView, android.view.View
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateDrawable();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable drawable = this.mNumberBackground;
        if (drawable != null) {
            drawable.draw(canvas);
            canvas.drawText(this.mNumberText, (float) this.mNumberX, (float) this.mNumberY, this.mNumberPain);
        }
    }

    /* access modifiers changed from: protected */
    public void debug(int i) {
        super.debug(i);
        Log.d("View", ImageView.debugIndent(i) + "slot=" + this.mSlot);
        Log.d("View", ImageView.debugIndent(i) + "icon=" + this.mIcon);
    }

    /* access modifiers changed from: package-private */
    public void placeNumber() {
        String str;
        if (this.mIcon.number > getContext().getResources().getInteger(17694723)) {
            str = getContext().getResources().getString(17039383);
        } else {
            str = NumberFormat.getIntegerInstance().format((long) this.mIcon.number);
        }
        this.mNumberText = str;
        int width = getWidth();
        int height = getHeight();
        Rect rect = new Rect();
        this.mNumberPain.getTextBounds(str, 0, str.length(), rect);
        int i = rect.right - rect.left;
        int i2 = rect.bottom - rect.top;
        this.mNumberBackground.getPadding(rect);
        int i3 = rect.left + i + rect.right;
        if (i3 < this.mNumberBackground.getMinimumWidth()) {
            i3 = this.mNumberBackground.getMinimumWidth();
        }
        int i4 = rect.right;
        this.mNumberX = (width - i4) - (((i3 - i4) - rect.left) / 2);
        int i5 = rect.top + i2 + rect.bottom;
        if (i5 < this.mNumberBackground.getMinimumWidth()) {
            i5 = this.mNumberBackground.getMinimumWidth();
        }
        int i6 = rect.bottom;
        this.mNumberY = (height - i6) - ((((i5 - rect.top) - i2) - i6) / 2);
        this.mNumberBackground.setBounds(width - i3, height - i5, width, height);
    }

    private void setContentDescription(Notification notification) {
        if (notification != null) {
            String contentDescForNotification = contentDescForNotification(((ImageView) this).mContext, notification);
            if (!TextUtils.isEmpty(contentDescForNotification)) {
                setContentDescription(contentDescForNotification);
            }
        }
    }

    @Override // android.view.View, java.lang.Object
    public String toString() {
        return "StatusBarIconView(slot=" + this.mSlot + " icon=" + this.mIcon + " notification=" + this.mNotification + ")";
    }

    public static String contentDescForNotification(Context context, Notification notification) {
        CharSequence charSequence;
        CharSequence charSequence2 = "";
        try {
            charSequence = Notification.Builder.recoverBuilder(context, notification).loadHeaderAppName();
        } catch (RuntimeException e) {
            Log.e("StatusBarIconView", "Unable to recover builder", e);
            Parcelable parcelable = notification.extras.getParcelable("android.appInfo");
            charSequence = parcelable instanceof ApplicationInfo ? String.valueOf(((ApplicationInfo) parcelable).loadLabel(context.getPackageManager())) : charSequence2;
        }
        CharSequence charSequence3 = notification.extras.getCharSequence("android.title");
        CharSequence charSequence4 = notification.tickerText;
        if (!TextUtils.isEmpty(charSequence4)) {
            charSequence2 = charSequence4;
        } else if (!TextUtils.isEmpty(charSequence3)) {
            charSequence2 = charSequence3;
        }
        return context.getString(C0015R$string.accessibility_desc_notification_icon, charSequence, charSequence2);
    }
}
