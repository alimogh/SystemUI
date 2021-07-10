package com.oneplus.aod.views.parsons;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.systemui.R$styleable;
import com.oneplus.aod.utils.OpAodDimenHelper;
import com.oneplus.systemui.biometrics.OpFodViewSettings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
public class OpParsonsBar extends View {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");
    private int mBarDistanceBottom;
    private int mBarDistanceBottomId;
    private int mBarDistanceTop;
    private int mBarDistanceTopId;
    private int mBarHeight;
    private int mBarMarginBottom;
    private int mBarMarginBottomId;
    private int mBarWidth;
    private int mBarWidthId;
    private int mBorder;
    private int mBorderId;
    private Paint mClearPaint;
    private int[] mColors;
    private int mGap;
    private int mGapId;
    private View mOverlayView;
    private Paint mPaint;
    private float[] mPositions;
    private String mStartTime;
    private OpUnlockDataHelper mUnlockDataHelper;
    private int mUnlockMarginBottom;
    private int mUnlockMarginBottomId;
    private int mUnlockMarginTop;
    private int mUnlockMarginTopId;

    public OpParsonsBar(Context context) {
        this(context, null);
    }

    public OpParsonsBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpParsonsBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setupAttributes(attributeSet);
        this.mUnlockDataHelper = new OpUnlockDataHelper(context, this);
        updateResource();
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setAntiAlias(true);
        Paint paint2 = new Paint();
        this.mClearPaint = paint2;
        paint2.setColor(0);
        this.mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        setLayerType(2, null);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mUnlockDataHelper.startListen();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mUnlockDataHelper.stopListen();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (z) {
            updateShader();
        }
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        int i;
        float f = (float) (this.mBorder + this.mGap);
        this.mPaint.setStyle(Paint.Style.FILL);
        int i2 = this.mBarWidth;
        canvas.drawRoundRect(f, f, ((float) i2) - f, ((float) this.mBarHeight) - f, (float) i2, (float) i2, this.mPaint);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mOverlayView.getLayoutParams();
        if (layoutParams != null) {
            i = layoutParams.topMargin;
        } else {
            i = this.mOverlayView.getTop();
        }
        int top = i - getTop();
        this.mUnlockDataHelper.clearUnlockRecord(canvas, (int) f, this.mBarDistanceTop, (int) (((float) this.mBarWidth) - f), top, this.mClearPaint);
        float f2 = ((float) this.mBorder) / 2.0f;
        this.mPaint.setStyle(Paint.Style.STROKE);
        int i3 = this.mBarWidth;
        canvas.drawRoundRect(f2, f2, ((float) i3) - f2, ((float) this.mBarHeight) - f2, (float) i3, (float) i3, this.mPaint);
        canvas.drawRect(0.0f, (float) top, (float) this.mBarWidth, (float) (top + this.mOverlayView.getHeight()), this.mClearPaint);
    }

    /* access modifiers changed from: package-private */
    public void setupAttributes(AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = ((View) this).mContext.obtainStyledAttributes(attributeSet, R$styleable.OpParsonsClock, 0, 0);
        readColors(obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_colors, -1));
        readPositions(obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_positions, -1));
        this.mBarWidthId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_barWidth, -1);
        this.mBorderId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_border, -1);
        this.mGapId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_gap, -1);
        this.mBarDistanceTopId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_barDistanceTop, -1);
        this.mBarDistanceBottomId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_barDistanceBottom, -1);
        this.mStartTime = obtainStyledAttributes.getString(R$styleable.OpParsonsClock_barStartTime);
        this.mUnlockMarginTopId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_clockMarginTop1, -1);
        this.mUnlockMarginBottomId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_unlockMarginBottom, -1);
        this.mBarMarginBottomId = obtainStyledAttributes.getResourceId(R$styleable.OpParsonsClock_clockMarginBottom1, -1);
        obtainStyledAttributes.recycle();
    }

    /* access modifiers changed from: package-private */
    public void updateResource() {
        this.mUnlockDataHelper.updateResources();
        int i = this.mBarWidthId;
        if (i != -1) {
            this.mBarWidth = OpAodDimenHelper.convertDpToFixedPx2(((View) this).mContext, i);
        }
        int i2 = this.mBorderId;
        if (i2 != -1) {
            this.mBorder = OpAodDimenHelper.convertDpToFixedPx2(((View) this).mContext, i2);
        }
        int i3 = this.mGapId;
        if (i3 != -1) {
            this.mGap = OpAodDimenHelper.convertDpToFixedPx2(((View) this).mContext, i3);
        }
        int i4 = this.mBarDistanceTopId;
        if (i4 != -1) {
            this.mBarDistanceTop = OpAodDimenHelper.convertDpToFixedPx2(((View) this).mContext, i4);
        }
        int i5 = this.mBarDistanceBottomId;
        if (i5 != -1) {
            this.mBarDistanceBottom = OpAodDimenHelper.convertDpToFixedPx2(((View) this).mContext, i5);
        }
        int i6 = this.mUnlockMarginTopId;
        if (i6 != -1) {
            this.mUnlockMarginTop = OpAodDimenHelper.convertDpToFixedPx2(((View) this).mContext, i6);
        }
        int i7 = this.mUnlockMarginBottomId;
        if (i7 != -1) {
            this.mUnlockMarginBottom = OpAodDimenHelper.convertDpToFixedPx2(((View) this).mContext, i7);
        }
        int i8 = this.mBarMarginBottomId;
        if (i8 != -1) {
            this.mBarMarginBottom = OpAodDimenHelper.convertDpToFixedPx2(((View) this).mContext, i8);
        }
    }

    /* access modifiers changed from: package-private */
    public void setOverlayView(View view) {
        this.mOverlayView = view;
    }

    /* access modifiers changed from: package-private */
    public View getOverlayView() {
        return this.mOverlayView;
    }

    /* access modifiers changed from: package-private */
    public void setUnlocksMsg(OpParsonsUnlockLabel opParsonsUnlockLabel) {
        this.mUnlockDataHelper.setUnlocksMsg(opParsonsUnlockLabel);
    }

    /* access modifiers changed from: package-private */
    public void onTimeChanged(Date date) {
        int convertToSeconds = convertToSeconds(this.mStartTime);
        int i = convertToSeconds / 3600;
        int i2 = (convertToSeconds - (i * 3600)) / 60;
        int convertToSeconds2 = convertToSeconds(date);
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        if (convertToSeconds2 - convertToSeconds >= 0) {
            instance.set(11, i);
            instance.set(12, i2);
            instance.set(13, 0);
        } else {
            instance.add(5, -1);
            instance.set(11, i);
            instance.set(12, i2);
            instance.set(13, 0);
        }
        long time = date.getTime() - instance.getTimeInMillis();
        long elapsedRealtime = SystemClock.elapsedRealtime();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mOverlayView.getLayoutParams();
        if (!(layoutParams == null || this.mOverlayView.getHeight() == 0)) {
            int i3 = this.mBarDistanceTop;
            layoutParams.topMargin = getTop() + i3 + ((int) (((float) TimeUnit.MILLISECONDS.toSeconds(time)) * (((float) (((this.mBarHeight - this.mBarDistanceBottom) - this.mOverlayView.getHeight()) - i3)) / 86400.0f)));
            this.mOverlayView.setLayoutParams(layoutParams);
        }
        this.mUnlockDataHelper.onTimeChanged(elapsedRealtime, time);
    }

    /* access modifiers changed from: package-private */
    public int getBarWidth() {
        return this.mBarWidth;
    }

    /* access modifiers changed from: package-private */
    public void calculateBarHeight(View view) {
        this.mBarHeight = ((OpFodViewSettings.getFodHighlightY(((View) this).mContext) + ((OpFodViewSettings.getFodHighlightSize(((View) this).mContext) - OpFodViewSettings.getFodIconSize(((View) this).mContext)) / 2)) - this.mBarMarginBottom) - ((this.mUnlockMarginTop + view.getHeight()) + this.mUnlockMarginBottom);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = this.mBarHeight;
            setLayoutParams(layoutParams);
        }
    }

    /* access modifiers changed from: package-private */
    public int getBarHeight() {
        return this.mBarHeight;
    }

    private void updateShader() {
        this.mPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) this.mBarHeight, this.mColors, this.mPositions, Shader.TileMode.CLAMP));
        this.mPaint.setStrokeWidth((float) this.mBorder);
    }

    private void readColors(int i) {
        if (i != -1) {
            TypedArray obtainTypedArray = ((View) this).mContext.getResources().obtainTypedArray(i);
            this.mColors = new int[obtainTypedArray.length()];
            for (int i2 = 0; i2 < obtainTypedArray.length(); i2++) {
                this.mColors[i2] = obtainTypedArray.getColor(i2, 0);
            }
            obtainTypedArray.recycle();
        }
    }

    private void readPositions(int i) {
        if (i != -1) {
            TypedArray obtainTypedArray = ((View) this).mContext.getResources().obtainTypedArray(i);
            this.mPositions = new float[obtainTypedArray.length()];
            for (int i2 = 0; i2 < obtainTypedArray.length(); i2++) {
                this.mPositions[i2] = obtainTypedArray.getFloat(i2, 0.0f);
            }
            obtainTypedArray.recycle();
        }
    }

    private int convertToSeconds(Date date) {
        return (date.getHours() * 3600) + (date.getMinutes() * 60) + date.getSeconds();
    }

    private int convertToSeconds(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        try {
            Date parse = DATE_FORMAT.parse(str);
            return (parse.getHours() * 3600) + (parse.getMinutes() * 60);
        } catch (ParseException e) {
            Log.e("OpParsonsBar", "convertToSeconds occur parse exception", e);
            return 0;
        }
    }
}
