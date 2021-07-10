package com.oneplus.aod.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import com.android.internal.R;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0008R$id;
import com.android.systemui.R$styleable;
public class OpAodSettings {
    private OpTextViewInfo mBatteryInfo = new OpTextViewInfo(C0005R$dimen.aod_battery_percentage_text_size);
    private OpBurnInSettings mBurnInSettings;
    private OpViewInfo mClockInfo = new OpViewInfo();
    private OpDateViewInfo mDateInfo = new OpDateViewInfo();
    private OpTextViewInfo mNotificationInfo = new OpTextViewInfo(C0005R$dimen.aod_noti_icon_more_text_size);
    private OpTextViewInfo mOwnerInfo = new OpTextViewInfo(C0005R$dimen.aod_owner_view_text_size);
    private OpTextViewInfo mSliceInfo = new OpTextViewInfo();
    private boolean mSupportSeconds = false;
    private OpSystemViewInfo mSystemInfo = new OpSystemViewInfo();
    private View mView;

    private OpAodSettings(Context context) {
        this.mBurnInSettings = new OpBurnInSettings(context);
    }

    private void validate() {
        if (this.mView == null) {
            throw new RuntimeException("ERROR !!!! view can't be null");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00d8, code lost:
        if (r13 != null) goto L_0x00e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00e6, code lost:
        if (r13 != null) goto L_0x00e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00e8, code lost:
        r13.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00eb, code lost:
        return null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00f0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.oneplus.aod.utils.OpAodSettings parse(android.content.Context r12, int r13) {
        /*
        // Method dump skipped, instructions count: 244
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.utils.OpAodSettings.parse(android.content.Context, int):com.oneplus.aod.utils.OpAodSettings");
    }

    public View getView() {
        return this.mView;
    }

    public boolean isSupportSeconds() {
        return this.mSupportSeconds;
    }

    public OpViewInfo getClockInfo() {
        return this.mClockInfo;
    }

    public boolean shouldShowDate() {
        return this.mDateInfo.isEnabled();
    }

    public boolean shouldShowSliceInfo() {
        return this.mSliceInfo.isEnabled();
    }

    public boolean shouldShowBattery() {
        return this.mBatteryInfo.isEnabled();
    }

    public boolean shouldShowNotification() {
        return this.mNotificationInfo.isEnabled();
    }

    public boolean shouldShowOwnerInfo() {
        return this.mOwnerInfo.isEnabled();
    }

    public Rect getBound() {
        return this.mBurnInSettings.getBound();
    }

    public int getMovingDistance(Context context) {
        return this.mBurnInSettings.getMovingDistance(context);
    }

    public String getBurnInHandleClassName() {
        return this.mBurnInSettings.getHandleClass();
    }

    public OpSystemViewInfo getSystemInfo() {
        return this.mSystemInfo;
    }

    public OpDateViewInfo getDateInfo() {
        return this.mDateInfo;
    }

    public OpTextViewInfo getSliceInfo() {
        return this.mSliceInfo;
    }

    public OpTextViewInfo getBatteryInfo() {
        return this.mBatteryInfo;
    }

    public OpTextViewInfo getNotificationInfo() {
        return this.mNotificationInfo;
    }

    public OpTextViewInfo getOwnerInfo() {
        return this.mOwnerInfo;
    }

    public static class OpBurnInSettings {
        int mBottom = -1;
        float mBottomRatio = 0.0f;
        private Context mContext;
        String mHandleClassName;
        int mLeft = -1;
        float mLeftRatio = 0.0f;
        int mMovingDistanceId = C0005R$dimen.aod_moving_distance_default;
        int mRight = -1;
        float mRightRatio = 0.0f;
        private int mScreenHeight;
        private int mScreenWidth;
        int mTop = -1;
        float mTopRatio = 0.0f;

        OpBurnInSettings(Context context) {
            this.mContext = context;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
            int i = displayMetrics.widthPixels;
            int i2 = displayMetrics.heightPixels;
            if (i > i2) {
                this.mScreenWidth = i2;
                this.mScreenHeight = i;
                return;
            }
            this.mScreenHeight = i2;
            this.mScreenWidth = i;
        }

        /* access modifiers changed from: package-private */
        public void parse(Context context, XmlResourceParser xmlResourceParser) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(Xml.asAttributeSet(xmlResourceParser), R$styleable.OpburnInSettings);
            this.mTopRatio = obtainStyledAttributes.getFloat(R$styleable.OpburnInSettings_minTopRatio, this.mTopRatio);
            this.mBottomRatio = obtainStyledAttributes.getFloat(R$styleable.OpburnInSettings_minBottomRatio, this.mBottomRatio);
            this.mLeftRatio = obtainStyledAttributes.getFloat(R$styleable.OpburnInSettings_minLeftRatio, this.mLeftRatio);
            this.mRightRatio = obtainStyledAttributes.getFloat(R$styleable.OpburnInSettings_minRightRatio, this.mRightRatio);
            this.mTop = obtainStyledAttributes.getResourceId(R$styleable.OpburnInSettings_minTop, this.mTop);
            this.mBottom = obtainStyledAttributes.getResourceId(R$styleable.OpburnInSettings_minBottom, this.mBottom);
            this.mLeft = obtainStyledAttributes.getResourceId(R$styleable.OpburnInSettings_minLeft, this.mLeft);
            this.mRight = obtainStyledAttributes.getResourceId(R$styleable.OpburnInSettings_minRight, this.mRight);
            this.mMovingDistanceId = obtainStyledAttributes.getResourceId(R$styleable.OpburnInSettings_movingDistance, this.mMovingDistanceId);
            obtainStyledAttributes.getInt(R$styleable.OpburnInSettings_layoutParamsType, 0);
            this.mHandleClassName = obtainStyledAttributes.getString(R$styleable.OpburnInSettings_handleClass);
            obtainStyledAttributes.recycle();
        }

        public Rect getBound() {
            int i;
            int i2;
            int i3;
            float f = this.mLeftRatio;
            int i4 = 0;
            if (f != 0.0f) {
                i = (int) (((float) this.mScreenWidth) * f);
            } else {
                int i5 = this.mLeft;
                i = i5 != -1 ? OpAodDimenHelper.convertDpToFixedPx2(this.mContext, i5) : 0;
            }
            float f2 = this.mTopRatio;
            if (f2 != 0.0f) {
                i2 = (int) (((float) this.mScreenHeight) * f2);
            } else {
                int i6 = this.mTop;
                i2 = i6 != -1 ? OpAodDimenHelper.convertDpToFixedPx2(this.mContext, i6) : 0;
            }
            float f3 = this.mRightRatio;
            if (f3 != 0.0f) {
                i3 = (int) (((float) this.mScreenWidth) * (1.0f - f3));
            } else {
                int i7 = this.mRight;
                i3 = i7 != -1 ? this.mScreenWidth - OpAodDimenHelper.convertDpToFixedPx2(this.mContext, i7) : 0;
            }
            float f4 = this.mBottomRatio;
            if (f4 != 0.0f) {
                i4 = (int) (((float) this.mScreenHeight) * (1.0f - f4));
            } else {
                int i8 = this.mBottom;
                if (i8 != -1) {
                    i4 = this.mScreenHeight - OpAodDimenHelper.convertDpToFixedPx2(this.mContext, i8);
                }
            }
            return new Rect(i, i2, i3, i4);
        }

        public int getMovingDistance(Context context) {
            return OpAodDimenHelper.convertDpToFixedPx(context, this.mMovingDistanceId);
        }

        public String getHandleClass() {
            return this.mHandleClassName;
        }
    }

    public static class OpDateViewInfo extends OpTextViewInfo {
        String mDateFormat = null;
        String mLocale = null;

        OpDateViewInfo() {
        }

        /* access modifiers changed from: package-private */
        @Override // com.oneplus.aod.utils.OpAodSettings.OpTextViewInfo, com.oneplus.aod.utils.OpAodSettings.OpViewInfo
        public void parse(Context context, XmlResourceParser xmlResourceParser) {
            super.parse(context, xmlResourceParser);
            parseDateView(context, Xml.asAttributeSet(xmlResourceParser));
        }

        public String getDateFormat() {
            return this.mDateFormat;
        }

        public String getLocale() {
            return this.mLocale;
        }

        /* access modifiers changed from: protected */
        public void parseDateView(Context context, AttributeSet attributeSet) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.OpDateView, 0, 0);
            this.mDateFormat = obtainStyledAttributes.getString(R$styleable.OpDateView_dateFormat);
            this.mLocale = obtainStyledAttributes.getString(R$styleable.OpDateView_locale);
            obtainStyledAttributes.recycle();
        }
    }

    public static class OpTextViewInfo extends OpViewInfo {
        boolean mFollowSystemFont;
        int mFontFamily;
        int mTextFontWeight;
        int mTextSizeId;

        OpTextViewInfo() {
            this.mTextFontWeight = -1;
            this.mFontFamily = -1;
            this.mTextSizeId = -1;
            this.mFollowSystemFont = true;
        }

        OpTextViewInfo(int i) {
            this();
            this.mTextSizeId = i;
        }

        /* access modifiers changed from: package-private */
        @Override // com.oneplus.aod.utils.OpAodSettings.OpViewInfo
        public void parse(Context context, XmlResourceParser xmlResourceParser) {
            super.parse(context, xmlResourceParser);
            AttributeSet asAttributeSet = Xml.asAttributeSet(xmlResourceParser);
            parseTextAppearance(context, asAttributeSet);
            parseOpFont(context, asAttributeSet);
        }

        public int getFontFamily() {
            return this.mFontFamily;
        }

        public int getTextSizeId() {
            return this.mTextSizeId;
        }

        public int getTextFontWeight() {
            return this.mTextFontWeight;
        }

        public int getTextSize(Context context) {
            return getSize(context, this.mTextSizeId);
        }

        public boolean isFollowSystemFont() {
            return this.mFollowSystemFont;
        }

        /* access modifiers changed from: protected */
        public void parseTextAppearance(Context context, AttributeSet attributeSet) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.TextAppearance, 0, 0);
            int indexCount = obtainStyledAttributes.getIndexCount();
            for (int i = 0; i < indexCount; i++) {
                int index = obtainStyledAttributes.getIndex(i);
                if (index == 0) {
                    this.mTextSizeId = obtainStyledAttributes.getResourceId(index, -1);
                } else if (index == 12) {
                    this.mFontFamily = obtainStyledAttributes.getResourceId(index, -1);
                } else if (index == 18) {
                    this.mTextFontWeight = obtainStyledAttributes.getInt(index, this.mTextFontWeight);
                }
            }
            obtainStyledAttributes.recycle();
        }

        /* access modifiers changed from: protected */
        public void parseOpFont(Context context, AttributeSet attributeSet) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.OpAodFont);
            this.mFollowSystemFont = obtainStyledAttributes.getBoolean(R$styleable.OpAodFont_followSystemFont, this.mFollowSystemFont);
            obtainStyledAttributes.recycle();
        }
    }

    public static class OpViewInfo {
        boolean mEnabled = true;
        int mGravity = 17;
        int mMarginBottomId = -1;
        int mMarginBottomType;
        int mMarginEndId = -1;
        int mMarginEndType;
        int mMarginLeftId = -1;
        int mMarginLeftType;
        int mMarginRightId = -1;
        int mMarginRightType;
        int mMarginStartId = -1;
        int mMarginStartType;
        int mMarginTopId = -1;
        int mMarginTopType;
        int mSizeType;

        OpViewInfo() {
        }

        /* access modifiers changed from: package-private */
        public void parse(Context context, XmlResourceParser xmlResourceParser) {
            AttributeSet asAttributeSet = Xml.asAttributeSet(xmlResourceParser);
            parseOp(context, asAttributeSet);
            parseMargin(context, asAttributeSet);
            parseGravity(context, asAttributeSet);
        }

        public int getGravity() {
            return this.mGravity;
        }

        public boolean isEnabled() {
            return this.mEnabled;
        }

        public int getMarginLeftId() {
            return this.mMarginLeftId;
        }

        public int getMarginRightId() {
            return this.mMarginRightId;
        }

        public int getSize(Context context, int i) {
            return getSizeInner(context, i, this.mSizeType);
        }

        public int getMarginStart(Context context) {
            return getSizeInner(context, this.mMarginStartId, this.mMarginStartType);
        }

        public int getMarginEnd(Context context) {
            return getSizeInner(context, this.mMarginEndId, this.mMarginEndType);
        }

        public int getMarginLeft(Context context) {
            return getSizeInner(context, this.mMarginLeftId, this.mMarginLeftType);
        }

        public int getMarginRight(Context context) {
            return getSizeInner(context, this.mMarginRightId, this.mMarginRightType);
        }

        public int getMarginTop(Context context) {
            return getSizeInner(context, this.mMarginTopId, this.mMarginTopType);
        }

        public int getMarginBottom(Context context) {
            return getSizeInner(context, this.mMarginBottomId, this.mMarginBottomType);
        }

        /* access modifiers changed from: protected */
        public int getSizeInner(Context context, int i, int i2) {
            if (i == -1) {
                return 0;
            }
            if (i2 == 1) {
                try {
                    return context.getResources().getDimensionPixelSize(i);
                } catch (Exception e) {
                    Log.w("OpAodSettings", "getSizeInner occur exception", e);
                    return 0;
                }
            } else if (i2 == 2) {
                return OpAodDimenHelper.convertDpToFixedPx2(context, i);
            } else {
                return OpAodDimenHelper.convertDpToFixedPx(context, i);
            }
        }

        /* access modifiers changed from: protected */
        public void parseOp(Context context, AttributeSet attributeSet) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.OpAodLayoutInfo);
            this.mEnabled = obtainStyledAttributes.getBoolean(R$styleable.OpAodLayoutInfo_show, true);
            this.mMarginStartType = obtainStyledAttributes.getInt(R$styleable.OpAodLayoutInfo_marginStartType, 0);
            this.mMarginEndType = obtainStyledAttributes.getInt(R$styleable.OpAodLayoutInfo_marginEndType, 0);
            this.mMarginLeftType = obtainStyledAttributes.getInt(R$styleable.OpAodLayoutInfo_marginLeftType, 0);
            this.mMarginRightType = obtainStyledAttributes.getInt(R$styleable.OpAodLayoutInfo_marginRightType, 0);
            this.mMarginTopType = obtainStyledAttributes.getInt(R$styleable.OpAodLayoutInfo_marginTopType, 0);
            this.mMarginBottomType = obtainStyledAttributes.getInt(R$styleable.OpAodLayoutInfo_marginBottomType, 0);
            this.mSizeType = obtainStyledAttributes.getInt(R$styleable.OpAodLayoutInfo_sizeType, 0);
            obtainStyledAttributes.recycle();
        }

        /* access modifiers changed from: protected */
        public void parseMargin(Context context, AttributeSet attributeSet) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ViewGroup_MarginLayout, 0, 0);
            int indexCount = obtainStyledAttributes.getIndexCount();
            for (int i = 0; i < indexCount; i++) {
                int index = obtainStyledAttributes.getIndex(i);
                switch (index) {
                    case 3:
                        this.mMarginLeftId = obtainStyledAttributes.getResourceId(index, -1);
                        break;
                    case 4:
                        this.mMarginTopId = obtainStyledAttributes.getResourceId(index, -1);
                        break;
                    case 5:
                        this.mMarginRightId = obtainStyledAttributes.getResourceId(index, -1);
                        break;
                    case 6:
                        this.mMarginBottomId = obtainStyledAttributes.getResourceId(index, -1);
                        break;
                    case 7:
                        this.mMarginStartId = obtainStyledAttributes.getResourceId(index, -1);
                        break;
                    case 8:
                        this.mMarginEndId = obtainStyledAttributes.getResourceId(index, -1);
                        break;
                }
            }
            obtainStyledAttributes.recycle();
        }

        /* access modifiers changed from: protected */
        public void parseGravity(Context context, AttributeSet attributeSet) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.FrameLayout_Layout, 0, 0);
            int indexCount = obtainStyledAttributes.getIndexCount();
            for (int i = 0; i < indexCount; i++) {
                int index = obtainStyledAttributes.getIndex(i);
                if (index == 0) {
                    this.mGravity = obtainStyledAttributes.getInt(index, 8388611);
                }
            }
            obtainStyledAttributes.recycle();
        }
    }

    public static class OpSystemViewInfo extends OpViewInfo {
        private boolean mAlignParentBottom = false;
        private int mBelowViewId = C0008R$id.op_aod_clock_container;

        OpSystemViewInfo() {
        }

        /* access modifiers changed from: package-private */
        @Override // com.oneplus.aod.utils.OpAodSettings.OpViewInfo
        public void parse(Context context, XmlResourceParser xmlResourceParser) {
            super.parse(context, xmlResourceParser);
            parseRules(context, xmlResourceParser);
        }

        /* access modifiers changed from: protected */
        public void parseRules(Context context, XmlResourceParser xmlResourceParser) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(Xml.asAttributeSet(xmlResourceParser), R.styleable.RelativeLayout_Layout, 0, 0);
            int indexCount = obtainStyledAttributes.getIndexCount();
            for (int i = 0; i < indexCount; i++) {
                int index = obtainStyledAttributes.getIndex(i);
                if (index == 3) {
                    this.mBelowViewId = obtainStyledAttributes.getResourceId(index, this.mBelowViewId);
                } else if (index == 12) {
                    this.mAlignParentBottom = obtainStyledAttributes.getBoolean(index, this.mAlignParentBottom);
                    this.mBelowViewId = 0;
                }
            }
            obtainStyledAttributes.recycle();
        }

        public void applyRules(RelativeLayout.LayoutParams layoutParams) {
            layoutParams.addRule(3, this.mBelowViewId);
            layoutParams.addRule(12, this.mAlignParentBottom ? -1 : 0);
        }
    }
}
