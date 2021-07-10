package com.oneplus.battery;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.android.settingslib.graph.BatteryMeterDrawableBase;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0006R$drawable;
import com.oneplus.util.OpBatteryUtils;
import com.oneplus.util.OpImageUtils;
public class OpBatteryMeterDrawable extends BatteryMeterDrawableBase {
    private Bitmap mBatterySaveOutline;
    private int mBatteryStyle = 0;
    private Bitmap mChargingOutlineBitmap;
    private Paint mCircleBackPaint;
    private Paint mCircleChargingPaint;
    private Paint mCircleFrontPaint;
    private Paint mCirclePowerSavePaint;
    private final RectF mCircleRect = new RectF();
    private int mCircleSize;
    private int mHeight;
    protected int mIsInvalidCharge = 0;
    private boolean mIsOptimizatedCharge = false;
    private int mLastHeight = 0;
    private int mLastWidth = 0;
    private int mMaskColor;
    private int mMaskColorWithoutAlpha;
    private Drawable mMaskDrawable;
    private Bitmap mMaskOutlineBitmap;
    private Bitmap mOptimizatedChargeOutline;
    private Paint mOptimizatedChargePaint;
    private int mPowerSaveColor;
    private boolean mPowerSaveEnabled;
    private int mWidth;

    public OpBatteryMeterDrawable(Context context, int i) {
        super(context, i);
        Paint paint = new Paint(1);
        this.mCircleBackPaint = paint;
        paint.setColor(i);
        this.mCircleBackPaint.setStrokeCap(Paint.Cap.BUTT);
        this.mCircleBackPaint.setDither(true);
        this.mCircleBackPaint.setStrokeWidth(0.0f);
        this.mCircleBackPaint.setStyle(Paint.Style.STROKE);
        Paint paint2 = new Paint(1);
        this.mCircleFrontPaint = paint2;
        paint2.setStrokeCap(Paint.Cap.BUTT);
        this.mCircleFrontPaint.setDither(true);
        this.mCircleFrontPaint.setStrokeWidth(0.0f);
        this.mCircleFrontPaint.setStyle(Paint.Style.STROKE);
        Paint paint3 = new Paint(1);
        this.mCircleChargingPaint = paint3;
        paint3.setStyle(Paint.Style.FILL);
        Paint paint4 = new Paint(1);
        this.mCirclePowerSavePaint = paint4;
        paint4.setStrokeCap(Paint.Cap.BUTT);
        this.mCirclePowerSavePaint.setDither(true);
        this.mCirclePowerSavePaint.setStyle(Paint.Style.STROKE);
        this.mCircleFrontPaint.setStrokeWidth(0.0f);
        this.mBatterySaveOutline = OpImageUtils.drawableToBitmap(this.mContext.getDrawable(C0006R$drawable.op_ic_battery_saver_outline));
        this.mPowerSaveColor = context.getColor(C0004R$color.battery_power_save_color);
        Paint paint5 = new Paint(1);
        this.mOptimizatedChargePaint = paint5;
        paint5.setColor(-16777216);
        this.mOptimizatedChargePaint.setStrokeCap(Paint.Cap.BUTT);
        this.mOptimizatedChargePaint.setDither(true);
        this.mOptimizatedChargePaint.setStrokeWidth(0.0f);
        this.mOptimizatedChargePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mOptimizatedChargePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        this.mOptimizatedChargeOutline = OpImageUtils.drawableToBitmap(this.mContext.getDrawable(C0006R$drawable.op_optimized_charging_outline));
        this.mChargingOutlineBitmap = OpImageUtils.drawableToBitmap(this.mContext.getDrawable(C0006R$drawable.op_battery_charging_outline));
    }

    @Override // com.android.settingslib.graph.BatteryMeterDrawableBase, android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mBatteryStyle == 1 ? super.getIntrinsicHeight() : super.getIntrinsicWidth();
    }

    private void postInvalidate(int i) {
        scheduleSelf(new Runnable() { // from class: com.oneplus.battery.-$$Lambda$S7Wld1_rpLukBj6_kbvV_X28zVM
            @Override // java.lang.Runnable
            public final void run() {
                OpBatteryMeterDrawable.this.invalidateSelf();
            }
        }, (long) i);
    }

    public void onBatteryStyleChanged(int i) {
        if (this.mBatteryStyle != i) {
            this.mBatteryStyle = i;
            updateViews();
        }
    }

    @Override // com.android.settingslib.graph.BatteryMeterDrawableBase
    public void onOptimizatedStatusChange(boolean z) {
        if (this.mIsOptimizatedCharge != z) {
            super.onOptimizatedStatusChange(z);
            this.mIsOptimizatedCharge = z;
            String str = BatteryMeterDrawableBase.TAG;
            Log.i(str, "onOptimizatedStatusChange isOptimizatedCharge:" + z);
            updateViews();
        }
    }

    @Override // com.android.settingslib.graph.BatteryMeterDrawableBase, android.graphics.drawable.Drawable
    public void setBounds(int i, int i2, int i3, int i4) {
        super.setBounds(i, i2, i3, i4);
        int i5 = i4 - i2;
        this.mHeight = i5;
        int i6 = i3 - i;
        this.mWidth = i6;
        if (this.mLastHeight != i5 || this.mLastWidth != i6) {
            this.mLastHeight = this.mHeight;
            this.mLastWidth = this.mWidth;
            postInvalidate(20);
        }
    }

    public void setColors(int i, int i2, int i3) {
        this.mMaskColor = i;
        this.mMaskColorWithoutAlpha = Color.rgb(Color.red(i), Color.green(this.mMaskColor), Color.blue(this.mMaskColor));
        Drawable drawable = this.mMaskDrawable;
        if (drawable != null) {
            drawable.setTintMode(PorterDuff.Mode.SRC_ATOP);
            this.mMaskDrawable.setTintList(ColorStateList.valueOf(this.mMaskColorWithoutAlpha));
        }
        this.mCirclePowerSavePaint.setColor(this.mMaskColor);
        if (this.mBatteryStyle == 0) {
            i2 = 0;
        }
        if (this.mPowerSaveEnabled) {
            int i4 = this.mBatteryStyle;
            if (i4 == 0) {
                i = this.mPowerSaveColor;
            } else if (i4 == 1) {
                i = this.mPowerSaveColor;
            }
        }
        setColors(i, i2);
    }

    @Override // com.android.settingslib.graph.BatteryMeterDrawableBase
    public void setColors(int i, int i2) {
        this.mCircleBackPaint.setColor(i2);
        this.mCircleFrontPaint.setColor(i);
        this.mCircleChargingPaint.setColor(i);
        super.setColors(i, i2);
    }

    @Override // com.android.settingslib.graph.BatteryMeterDrawableBase, android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (getBatteryLevel() != -1) {
            updateViews();
            int i = this.mBatteryStyle;
            if (i == 1) {
                drawCircle(canvas, this.mWidth, this.mHeight);
            } else if (i != 2) {
                RectF rectF = new RectF(0.0f, 0.0f, (float) this.mWidth, (float) this.mHeight);
                drawBattery(rectF, canvas);
                Bitmap bitmap = this.mMaskOutlineBitmap;
                if (bitmap != null && !bitmap.isRecycled()) {
                    canvas.drawBitmap(this.mMaskOutlineBitmap, (Rect) null, rectF, this.mOptimizatedChargePaint);
                }
                Drawable drawable = this.mMaskDrawable;
                if (drawable != null) {
                    drawable.setAlpha(Color.alpha(this.mMaskColor));
                    this.mMaskDrawable.setBounds(0, 0, this.mWidth, this.mHeight);
                    this.mMaskDrawable.draw(canvas);
                }
            }
        }
    }

    private void drawBattery(RectF rectF, Canvas canvas) {
        Paint paint = new Paint(3);
        paint.setAntiAlias(true);
        int batteryLevel = getBatteryLevel();
        Bitmap drawableToBitmap = OpImageUtils.drawableToBitmap(this.mContext.getDrawable(OpBatteryUtils.getBatteryMaskInsideResId(batteryLevel)));
        Bitmap drawableToBitmap2 = OpImageUtils.drawableToBitmap(this.mContext.getDrawable(C0006R$drawable.op_battery_mask));
        if (drawableToBitmap != null && drawableToBitmap2 != null) {
            Bitmap createBitmap = Bitmap.createBitmap((int) rectF.right, (int) rectF.bottom, Bitmap.Config.ARGB_8888);
            Canvas canvas2 = new Canvas(createBitmap);
            int rgb = Color.rgb(Color.red(this.mMaskColor), Color.green(this.mMaskColor), Color.blue(this.mMaskColor));
            int batteryColorForLevel = batteryColorForLevel(batteryLevel);
            paint.setColorFilter(new PorterDuffColorFilter(Color.rgb(Color.red(batteryColorForLevel), Color.green(batteryColorForLevel), Color.blue(batteryColorForLevel)), PorterDuff.Mode.SRC_ATOP));
            canvas2.drawBitmap(drawableToBitmap, (Rect) null, rectF, paint);
            paint.setColorFilter(new PorterDuffColorFilter(rgb, PorterDuff.Mode.SRC_ATOP));
            canvas2.drawBitmap(drawableToBitmap2, (Rect) null, rectF, paint);
            paint.setColorFilter(null);
            paint.setAlpha(Color.alpha(this.mMaskColor));
            canvas.drawBitmap(createBitmap, (Rect) null, rectF, paint);
            if (!drawableToBitmap.isRecycled()) {
                drawableToBitmap.recycle();
            }
            if (!drawableToBitmap2.isRecycled()) {
                drawableToBitmap2.recycle();
            }
            if (createBitmap != null && !createBitmap.isRecycled()) {
                createBitmap.recycle();
            }
        }
    }

    private void drawCircle(Canvas canvas, int i, int i2) {
        initCircleSize(i, i2);
        int batteryLevel = getBatteryLevel();
        boolean charging = getCharging();
        this.mCircleFrontPaint.setColor((charging || this.mPowerSaveEnabled) ? getChargeColor() : getColorForLevel(batteryLevel));
        float f = ((float) batteryLevel) * 3.6f;
        canvas.drawArc(this.mCircleRect, 270.0f, f - 360.0f, false, this.mCircleBackPaint);
        canvas.drawArc(this.mCircleRect, 270.0f, f, false, this.mCircleFrontPaint);
        RectF rectF = this.mCircleRect;
        float f2 = (rectF.right - rectF.left) / 4.0f;
        canvas.save();
        if (charging) {
            this.mCircleChargingPaint.setColor(getChargeColor());
            if (isOptimizatedCharge() || isInvalidCharge()) {
                Drawable drawable = this.mMaskDrawable;
                if (drawable != null) {
                    drawable.setBounds(0, 0, this.mWidth, this.mHeight);
                    this.mMaskDrawable.draw(canvas);
                }
            } else {
                canvas.drawCircle(this.mCircleRect.centerX(), this.mCircleRect.centerY(), f2, this.mCircleChargingPaint);
            }
        } else if (this.mPowerSaveEnabled) {
            float f3 = (float) i;
            float f4 = f3 * 0.34f;
            float strokeWidth = this.mCirclePowerSavePaint.getStrokeWidth();
            float f5 = (float) i2;
            float f6 = f5 / 2.0f;
            canvas.drawLine(f4, f6, f3 - f4, f6, this.mCirclePowerSavePaint);
            float f7 = (float) (i / 2);
            float f8 = (float) (i2 / 2);
            float f9 = strokeWidth / 2.0f;
            canvas.drawLine(f7, f4, f7, f8 - f9, this.mCirclePowerSavePaint);
            canvas.drawLine(f7, f8 + f9, f7, f5 - f4, this.mCirclePowerSavePaint);
        }
        canvas.restore();
    }

    private void initCircleSize(int i, int i2) {
        int max = Math.max(i, i2);
        this.mCircleSize = max;
        float f = ((float) max) / 6.5f;
        this.mCircleBackPaint.setStrokeWidth(f);
        this.mCircleFrontPaint.setStrokeWidth(f);
        this.mCirclePowerSavePaint.setStrokeWidth(0.5f * f);
        float f2 = f / 2.0f;
        int i3 = this.mCircleSize;
        this.mCircleRect.set(f2, f2, ((float) i3) - f2, ((float) i3) - f2);
    }

    private void setMaskDrawable(Drawable drawable) {
        setMaskDrawable(drawable, this.mMaskColorWithoutAlpha, PorterDuff.Mode.SRC_ATOP);
    }

    private void setMaskDrawable(Drawable drawable, int i, PorterDuff.Mode mode) {
        this.mMaskDrawable = drawable;
        if (drawable != null) {
            drawable.setTintMode(mode);
            this.mMaskDrawable.setTintList(ColorStateList.valueOf(i));
        }
    }

    public void setPowerSaveEnabled(boolean z) {
        if (this.mPowerSaveEnabled != z) {
            super.setPowerSave(z);
            this.mPowerSaveEnabled = z;
            updateViews();
        }
    }

    @Override // com.android.settingslib.graph.BatteryMeterDrawableBase
    public void setIsInvalidCharge(int i) {
        if (this.mIsInvalidCharge != i) {
            super.setIsInvalidCharge(i);
            this.mIsInvalidCharge = i;
            String str = BatteryMeterDrawableBase.TAG;
            Log.i(str, "setIsInvalidCharge isInvalidCharge:" + i);
            updateViews();
        }
    }

    private void updateViews() {
        if (isInvalidCharge()) {
            int i = this.mBatteryStyle;
            if (i == 0) {
                setMaskDrawable(this.mContext.getDrawable(C0006R$drawable.op_invalid_charge));
                this.mMaskOutlineBitmap = OpImageUtils.drawableToBitmap(this.mContext.getDrawable(C0006R$drawable.op_invalid_charge_outline));
            } else if (i == 1) {
                setMaskDrawable(this.mContext.getDrawable(C0006R$drawable.op_circle_invalid_charge));
                this.mMaskOutlineBitmap = null;
            }
        } else if (this.mPowerSaveEnabled) {
            if (this.mBatteryStyle == 0) {
                setMaskDrawable(this.mContext.getDrawable(C0006R$drawable.op_ic_battery_saver));
                this.mMaskOutlineBitmap = this.mBatterySaveOutline;
            }
        } else if (isOptimizatedCharge()) {
            int i2 = this.mBatteryStyle;
            if (i2 == 0) {
                setMaskDrawable(this.mContext.getDrawable(C0006R$drawable.op_optimized_charging));
                this.mMaskOutlineBitmap = this.mOptimizatedChargeOutline;
            } else if (i2 == 1) {
                setMaskDrawable(this.mContext.getDrawable(C0006R$drawable.op_circle_optimized_charging));
                this.mMaskOutlineBitmap = null;
            }
        } else if (this.mBatteryStyle != 0) {
            setMaskDrawable(null);
            this.mMaskOutlineBitmap = null;
        } else if (getCharging()) {
            setMaskDrawable(this.mContext.getDrawable(C0006R$drawable.op_battery_charging_mask));
            this.mMaskOutlineBitmap = this.mChargingOutlineBitmap;
        } else {
            setMaskDrawable(null);
            this.mMaskOutlineBitmap = null;
        }
    }

    @Override // com.android.settingslib.graph.BatteryMeterDrawableBase
    public void setCharging(boolean z) {
        super.setCharging(z);
        updateViews();
    }

    private boolean isOptimizatedCharge() {
        return getCharging() && this.mIsOptimizatedCharge;
    }

    private boolean isInvalidCharge() {
        return getCharging() && this.mIsInvalidCharge != 0;
    }
}
