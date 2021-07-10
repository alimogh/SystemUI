package com.oneplus.aod.controller;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0016R$style;
import com.oneplus.aod.OpAodBatteryStatusView;
import com.oneplus.aod.OpAodNotificationIconAreaController;
import com.oneplus.aod.utils.OpAodDimenHelper;
import com.oneplus.aod.utils.OpAodSettings;
import com.oneplus.aod.views.OpTextDate;
import com.oneplus.util.OpUtils;
import java.util.Calendar;
import java.util.TimeZone;
public abstract class OpClockControllerImpl implements IOpClockController, View.OnAttachStateChangeListener {
    protected static final int FONT_DATE_SIZE = C0005R$dimen.aod_date_view_font_size;
    protected static final int FONT_STYLE_BATTERY = C0016R$style.battery_percentage;
    protected static final int FONT_STYLE_NOTIFICATION = C0016R$style.notification_icon_more;
    protected static final int FONT_STYLE_OWNERINFO = C0016R$style.aod_owner_info;
    protected OpAodSettings mAodClockSettings;
    protected Context mContext;
    protected Calendar mTime;
    protected TimeZone mTimeZone;
    protected View mView;

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewDetachedFromWindow(View view) {
    }

    public OpClockControllerImpl(Context context) {
        this.mContext = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        TimeZone timeZone = TimeZone.getDefault();
        this.mTimeZone = timeZone;
        this.mTime = Calendar.getInstance(timeZone);
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public View getClockView() {
        if (this.mView == null) {
            View view = this.mAodClockSettings.getView();
            this.mView = view;
            view.addOnAttachStateChangeListener(this);
        }
        return this.mView;
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public View getCurrentView() {
        return this.mView;
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void onDestroyView() {
        View view = this.mView;
        if (view != null) {
            view.removeOnAttachStateChangeListener(this);
            this.mView = null;
        }
        this.mAodClockSettings = null;
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void updateSettings(int i) {
        this.mAodClockSettings = OpAodSettings.parse(this.mContext, i);
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public boolean shouldShowDate() {
        return this.mAodClockSettings.shouldShowDate();
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public boolean shouldShowBattery() {
        return this.mAodClockSettings.shouldShowBattery();
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public boolean shouldShowNotification() {
        return this.mAodClockSettings.shouldShowNotification();
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public boolean shouldShowOwnerInfo() {
        return this.mAodClockSettings.shouldShowOwnerInfo();
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public boolean shouldShowSliceInfo() {
        return this.mAodClockSettings.shouldShowSliceInfo();
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public Rect getBound() {
        return this.mAodClockSettings.getBound();
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public int getMovingDistance() {
        return this.mAodClockSettings.getMovingDistance(this.mContext);
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public String getBurnInHandleClassName() {
        return this.mAodClockSettings.getBurnInHandleClassName();
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void applySystemInfoViewMargin(RelativeLayout.LayoutParams layoutParams) {
        OpAodSettings.OpSystemViewInfo systemInfo = this.mAodClockSettings.getSystemInfo();
        systemInfo.applyRules(layoutParams);
        if (systemInfo.getMarginLeftId() == -1 && systemInfo.getMarginRightId() == -1) {
            layoutParams.setMarginsRelative(systemInfo.getMarginStart(this.mContext), systemInfo.getMarginTop(this.mContext), systemInfo.getMarginEnd(this.mContext), systemInfo.getMarginBottom(this.mContext));
            return;
        }
        layoutParams.setMarginStart(Integer.MIN_VALUE);
        layoutParams.setMarginEnd(Integer.MIN_VALUE);
        layoutParams.setMargins(systemInfo.getMarginLeft(this.mContext), systemInfo.getMarginTop(this.mContext), systemInfo.getMarginRight(this.mContext), systemInfo.getMarginBottom(this.mContext));
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void applyDateInfoViewMargin(LinearLayout.LayoutParams layoutParams) {
        applySystemInfoViewMargin(layoutParams, this.mAodClockSettings.getDateInfo());
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void applySliceInfoViewMargin(LinearLayout.LayoutParams layoutParams) {
        applySystemInfoViewMargin(layoutParams, this.mAodClockSettings.getSliceInfo());
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void applyBatteryInfoViewMargin(LinearLayout.LayoutParams layoutParams) {
        applySystemInfoViewMargin(layoutParams, this.mAodClockSettings.getBatteryInfo());
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void applyNotificationInfoViewMargin(LinearLayout.LayoutParams layoutParams) {
        applySystemInfoViewMargin(layoutParams, this.mAodClockSettings.getNotificationInfo());
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void applyOwnerInfoViewMargin(LinearLayout.LayoutParams layoutParams) {
        applySystemInfoViewMargin(layoutParams, this.mAodClockSettings.getOwnerInfo());
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void applyDateInfoTextSettings(OpTextDate opTextDate) {
        int i;
        if (opTextDate != null) {
            OpAodSettings.OpDateViewInfo dateInfo = this.mAodClockSettings.getDateInfo();
            if (dateInfo.isEnabled()) {
                Typeface typeface = getTypeface(dateInfo, Typeface.create("sans-serif-medium", 0));
                if (dateInfo.getTextSizeId() != -1) {
                    i = dateInfo.getTextSize(this.mContext);
                } else {
                    i = OpAodDimenHelper.convertDpToFixedPx(this.mContext, FONT_DATE_SIZE);
                }
                opTextDate.setTextSettings(typeface, i);
                opTextDate.setLocale(dateInfo.getLocale());
                opTextDate.setFormatString(dateInfo.getDateFormat());
            }
        }
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void applyBatteryInfoTextSettings(OpAodBatteryStatusView opAodBatteryStatusView) {
        if (opAodBatteryStatusView != null) {
            OpAodSettings.OpTextViewInfo batteryInfo = this.mAodClockSettings.getBatteryInfo();
            if (batteryInfo.isEnabled()) {
                opAodBatteryStatusView.setTextSettings(FONT_STYLE_BATTERY, getTypeface(batteryInfo), batteryInfo.getTextSize(this.mContext));
            }
        }
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void applyNotificationInfoTextSettings(OpAodNotificationIconAreaController opAodNotificationIconAreaController) {
        if (opAodNotificationIconAreaController != null) {
            OpAodSettings.OpTextViewInfo notificationInfo = this.mAodClockSettings.getNotificationInfo();
            if (notificationInfo.isEnabled()) {
                opAodNotificationIconAreaController.setTextSettings(FONT_STYLE_NOTIFICATION, getTypeface(notificationInfo), notificationInfo.getTextSize(this.mContext));
            }
        }
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void applyOwnerInfoTextSettings(TextView textView) {
        if (textView != null) {
            OpAodSettings.OpTextViewInfo ownerInfo = this.mAodClockSettings.getOwnerInfo();
            if (ownerInfo.isEnabled()) {
                textView.setTextAppearance(FONT_STYLE_OWNERINFO);
                Typeface typeface = getTypeface(ownerInfo);
                if (typeface != null) {
                    textView.setTypeface(typeface);
                }
                int textSize = ownerInfo.getTextSize(this.mContext);
                if (textSize != 0) {
                    textView.setTextSize(0, (float) textSize);
                }
            }
        }
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void onTimeTick() {
        this.mTime.setTimeInMillis(System.currentTimeMillis());
    }

    @Override // com.oneplus.aod.controller.IOpClockController
    public void onTimeZoneChanged(TimeZone timeZone) {
        this.mTimeZone = timeZone;
        this.mTime.setTimeZone(timeZone);
        onTimeTick();
    }

    /* access modifiers changed from: protected */
    public void applySystemInfoViewMargin(LinearLayout.LayoutParams layoutParams, OpAodSettings.OpViewInfo opViewInfo) {
        if (opViewInfo.getMarginLeftId() == -1 && opViewInfo.getMarginRightId() == -1) {
            layoutParams.setMarginsRelative(opViewInfo.getMarginStart(this.mContext), opViewInfo.getMarginTop(this.mContext), opViewInfo.getMarginEnd(this.mContext), opViewInfo.getMarginBottom(this.mContext));
        } else {
            layoutParams.setMarginStart(Integer.MIN_VALUE);
            layoutParams.setMarginEnd(Integer.MIN_VALUE);
            layoutParams.setMargins(opViewInfo.getMarginLeft(this.mContext), opViewInfo.getMarginTop(this.mContext), opViewInfo.getMarginRight(this.mContext), opViewInfo.getMarginBottom(this.mContext));
        }
        layoutParams.gravity = opViewInfo.getGravity();
    }

    /* access modifiers changed from: protected */
    public String getTag() {
        return getClass().getSimpleName();
    }

    /* access modifiers changed from: protected */
    public Typeface getTypeface(OpAodSettings.OpTextViewInfo opTextViewInfo) {
        return getTypeface(opTextViewInfo, null);
    }

    /* access modifiers changed from: protected */
    public Typeface getTypeface(OpAodSettings.OpTextViewInfo opTextViewInfo, Typeface typeface) {
        Typeface typeface2;
        if (opTextViewInfo.getFontFamily() == -1) {
            typeface2 = OpUtils.isMCLVersion() ? OpUtils.getMclTypeface(3) : null;
        } else if (opTextViewInfo.isFollowSystemFont()) {
            typeface2 = ResourcesCompat.getFont(this.mContext, opTextViewInfo.getFontFamily());
            int textFontWeight = opTextViewInfo.getTextFontWeight();
            if (textFontWeight != -1) {
                typeface2 = Typeface.create(typeface2, textFontWeight, false);
            }
        } else {
            typeface2 = Typeface.create(ResourcesCompat.getFont(this.mContext, opTextViewInfo.getFontFamily()), 0);
        }
        return typeface2 == null ? typeface : typeface2;
    }
}
