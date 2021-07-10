package com.oneplus.aod;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0016R$style;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpUtils;
public class OpSingleNotificationView extends LinearLayout {
    private Handler mHandler;
    private TextView mHeader;
    private LinearLayout mHeaderContainer;
    private ImageView mIcon;
    private LinearLayout mNotificationViewCustom;
    private LinearLayout mNotificationViewDefault;
    private TextView mSmallText;
    private TextView mTitle;

    private void initHandler() {
        StatusBar phoneStatusBar;
        OpAodWindowManager aodWindowManager;
        if (this.mHandler == null && (phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar()) != null && (aodWindowManager = phoneStatusBar.getAodWindowManager()) != null && aodWindowManager.getUIHandler() != null) {
            this.mHandler = new Handler(aodWindowManager.getUIHandler().getLooper()) { // from class: com.oneplus.aod.OpSingleNotificationView.1
                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    if (message.what == 1) {
                        OpSingleNotificationView.this.handleUpdateViewInternal((NotificationEntry) message.obj);
                    }
                }
            };
        }
    }

    public OpSingleNotificationView(Context context) {
        super(context);
    }

    public OpSingleNotificationView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
        ((LinearLayout) this).mContext = context;
    }

    public OpSingleNotificationView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
        ((LinearLayout) this).mContext = context;
    }

    public OpSingleNotificationView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        ((LinearLayout) this).mContext = context;
        initHandler();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIcon = (ImageView) findViewById(C0008R$id.single_notification_icon);
        this.mHeader = (TextView) findViewById(C0008R$id.single_notification_header);
        this.mHeaderContainer = (LinearLayout) findViewById(C0008R$id.header_container);
        this.mTitle = (TextView) findViewById(C0008R$id.single_notification_title);
        this.mSmallText = (TextView) findViewById(C0008R$id.single_notification_smallText);
        this.mNotificationViewDefault = (LinearLayout) findViewById(C0008R$id.notification_default);
        this.mNotificationViewCustom = (LinearLayout) findViewById(C0008R$id.notificaiton_custom);
        adjustNotificationMargin();
    }

    private void adjustNotificationMargin() {
        if (this.mNotificationViewDefault != null) {
            int convertDpToFixedPx = OpUtils.convertDpToFixedPx(getResources().getDimension(C0005R$dimen.single_notification_horizontal_margin));
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mNotificationViewDefault.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.setMarginStart(convertDpToFixedPx);
                layoutParams.setMarginEnd(convertDpToFixedPx);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        int i = 1;
        if (((LinearLayout) this).mContext.getResources().getConfiguration().getLayoutDirection() != 1) {
            i = 0;
        }
        ViewGroup.LayoutParams layoutParams = this.mIcon.getLayoutParams();
        layoutParams.width = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.single_notification_icon_width);
        layoutParams.height = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.single_notification_icon_height);
        this.mIcon.setLayoutParams(layoutParams);
        this.mHeaderContainer.setTextDirection(i);
        this.mHeader.setTextAppearance(C0016R$style.single_notification_header);
        int i2 = 4;
        this.mTitle.setTextDirection(i != 0 ? 4 : 3);
        this.mTitle.setTextAppearance(C0016R$style.single_notification_title);
        TextView textView = this.mSmallText;
        if (i == 0) {
            i2 = 3;
        }
        textView.setTextDirection(i2);
        this.mSmallText.setTextAppearance(C0016R$style.single_notification_smallText);
    }

    public void onNotificationHeadsUp(NotificationEntry notificationEntry) {
        notificationEntry.getSbn();
        updateViewInternal(notificationEntry);
    }

    private void updateViewInternal(NotificationEntry notificationEntry) {
        initHandler();
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.removeMessages(1);
            Handler handler2 = this.mHandler;
            handler2.sendMessage(handler2.obtainMessage(1, notificationEntry));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUpdateViewInternal(NotificationEntry notificationEntry) {
        int i;
        TextView textView;
        Log.d("SingleNotificationView", "updateViewInternal");
        this.mIcon.setImageDrawable(null);
        StatusBarNotification sbn = notificationEntry.getSbn();
        Bundle bundle = sbn.getNotification().extras;
        CharSequence charSequence = bundle.getCharSequence("android.title");
        CharSequence charSequence2 = bundle.getCharSequence("android.text");
        CharSequence[] charSequenceArray = bundle.getCharSequenceArray("android.textLines");
        if (!(charSequenceArray == null || charSequenceArray.length == 0)) {
            charSequence2 = charSequenceArray[charSequenceArray.length - 1];
        }
        Icon smallIcon = sbn.getNotification().getSmallIcon();
        showCustomNotification(false, null);
        try {
            showCustomNotification(false, null);
        } catch (Exception e) {
            Log.e("SingleNotificationView", "Exception e = " + e.toString());
        }
        int i2 = sbn.getNotification().color;
        if (i2 == 0) {
            i = ((LinearLayout) this).mContext.getResources().getColor(17170443);
        } else {
            i = ContrastColorUtil.changeColorLightness(i2, 25);
        }
        boolean shouldHideSensitive = OpLsState.getInstance().getPhoneStatusBar().shouldHideSensitive(notificationEntry);
        StringBuilder sb = new StringBuilder();
        sb.append("updateViewInternal: custom=");
        sb.append(false);
        sb.append(", hideSensitivie=");
        sb.append(shouldHideSensitive);
        sb.append(", isLock=");
        sb.append(notificationEntry.getRow().isUserLocked());
        sb.append(", color=0x");
        sb.append(Integer.toHexString(i2));
        sb.append(", headerColor=0x");
        sb.append(Integer.toHexString(i));
        sb.append(", titleVis = ");
        TextView textView2 = this.mTitle;
        Object obj = "null";
        sb.append(textView2 != null ? Integer.valueOf(textView2.getVisibility()) : obj);
        sb.append(", smallTextVis = ");
        TextView textView3 = this.mSmallText;
        if (textView3 != null) {
            obj = Integer.valueOf(textView3.getVisibility());
        }
        sb.append(obj);
        Log.d("SingleNotificationView", sb.toString());
        if (this.mHeader != null) {
            String resolveAppName = resolveAppName(sbn);
            if (resolveAppName != null) {
                this.mHeader.setText(resolveAppName);
                this.mHeader.setTextColor(i);
            }
        } else {
            Log.w("SingleNotificationView", sbn.getKey() + " mHeader is null");
        }
        if (this.mIcon == null || smallIcon == null) {
            Log.w("SingleNotificationView", sbn.getKey() + " mIcon and icon is null");
        } else {
            Drawable loadDrawable = smallIcon.loadDrawable(((LinearLayout) this).mContext);
            if (loadDrawable == null) {
                Log.d("SingleNotificationView", "drawable = null");
                return;
            }
            Drawable newDrawable = loadDrawable.getConstantState().newDrawable();
            this.mIcon.setColorFilter((ColorFilter) null);
            if (i2 != 0) {
                this.mIcon.setColorFilter(i);
            }
            if (smallIcon != null) {
                this.mIcon.setImageDrawable(newDrawable);
            } else {
                Log.d("SingleNotificationView", "private layout icon null");
            }
        }
        TextView textView4 = this.mSmallText;
        if (textView4 == null || (textView = this.mTitle) == null) {
            Log.w("SingleNotificationView", "Title = " + this.mTitle + " or SmallText = " + this.mSmallText + " is null");
        } else if (shouldHideSensitive) {
            textView4.setText(((LinearLayout) this).mContext.getResources().getQuantityString(84738048, 1, 1));
            if (TextUtils.isEmpty(this.mSmallText.getText())) {
                Log.d("SingleNotificationView", "small text content is empty");
            }
            this.mSmallText.setVisibility(0);
            this.mTitle.setText("");
            this.mTitle.setVisibility(8);
        } else {
            textView.setText("");
            this.mSmallText.setText("");
            if (charSequence != null) {
                this.mTitle.setVisibility(0);
                this.mTitle.setText(charSequence.toString());
            } else {
                this.mTitle.setVisibility(8);
            }
            if (charSequence2 != null) {
                this.mSmallText.setVisibility(0);
                this.mSmallText.setText(charSequence2.toString());
                if (TextUtils.isEmpty(charSequence2)) {
                    Log.d("SingleNotificationView", "small text is null or empty");
                    return;
                }
                return;
            }
            this.mSmallText.setVisibility(8);
            Log.d("SingleNotificationView", "small text is null");
        }
    }

    private void showCustomNotification(boolean z, View view) {
        this.mNotificationViewCustom.removeAllViews();
        if (!z) {
            this.mNotificationViewCustom.setVisibility(8);
            this.mNotificationViewDefault.setVisibility(0);
            return;
        }
        this.mNotificationViewCustom.addView(view);
        this.mNotificationViewCustom.setVisibility(0);
        this.mNotificationViewDefault.setVisibility(8);
    }

    private String resolveAppName(StatusBarNotification statusBarNotification) {
        Notification notification = statusBarNotification.getNotification();
        try {
            return Notification.Builder.recoverBuilder(((LinearLayout) this).mContext, notification).loadHeaderAppName();
        } catch (RuntimeException e) {
            Log.e("SingleNotificationView", "Unable to recover builder", e);
            Parcelable parcelable = notification.extras.getParcelable("android.appInfo");
            if (parcelable instanceof ApplicationInfo) {
                return String.valueOf(((ApplicationInfo) parcelable).loadLabel(((LinearLayout) this).mContext.getPackageManager()));
            }
            return null;
        }
    }

    public void updateRTL(int i) {
        int i2 = 1;
        if (i != 1) {
            i2 = 0;
        }
        this.mHeaderContainer.setLayoutDirection(i2);
        invalidate();
    }
}
