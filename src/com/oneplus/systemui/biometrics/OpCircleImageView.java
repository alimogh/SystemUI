package com.oneplus.systemui.biometrics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import com.android.systemui.C0004R$color;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
public class OpCircleImageView extends ImageView {
    private static final int[][] BRIGHTNESS_ALPHA_ARRAY = {new int[]{0, 255}, new int[]{1, 241}, new int[]{2, 236}, new int[]{4, 235}, new int[]{5, 234}, new int[]{6, 232}, new int[]{10, 228}, new int[]{20, 220}, new int[]{30, 212}, new int[]{45, 204}, new int[]{70, 190}, new int[]{100, 179}, new int[]{150, 166}, new int[]{227, 144}, new int[]{300, 131}, new int[]{400, 112}, new int[]{500, 96}, new int[]{600, 83}, new int[]{800, 60}, new int[]{1023, 34}, new int[]{2000, 131}};
    private Context mContext;
    private int mDefaultBacklight;
    private boolean mHasCustomizedHightlight;
    Paint mPaint;
    Path mPath;
    private int mRadius;
    private int mType;

    private int interpolate(int i, int i2, int i3, int i4, int i5) {
        int i6 = i5 - i4;
        int i7 = i - i2;
        int i8 = ((i6 * 2) * i7) / (i3 - i2);
        int i9 = i8 / 2;
        int i10 = i2 - i3;
        return i4 + i9 + (i8 % 2) + ((i10 == 0 || i6 == 0) ? 0 : (((i7 * 2) * (i - i3)) / i6) / i10);
    }

    private int getDimAlpha() {
        int[][] iArr = BRIGHTNESS_ALPHA_ARRAY;
        int intForUser = Settings.System.getIntForUser(this.mContext.getContentResolver(), "screen_brightness", this.mDefaultBacklight, -2);
        int length = iArr.length;
        Log.d("OpCircleImageView", "brightness = " + intForUser + ", level = " + length);
        int i = 0;
        while (i < length && iArr[i][0] < intForUser) {
            i++;
        }
        if (i == 0) {
            return iArr[0][1];
        }
        if (i == length) {
            return iArr[length - 1][1];
        }
        int i2 = i - 1;
        return interpolate(intForUser, iArr[i2][0], iArr[i][0], iArr[i2][1], iArr[i][1]);
    }

    public OpCircleImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    public OpCircleImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public OpCircleImageView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        this.mContext = context;
        int id = getId();
        boolean z = true;
        if (id == C0008R$id.op_fingerprint_icon_white) {
            this.mType = 3;
        } else if (id == C0008R$id.op_fingerprint_icon_disable) {
            this.mType = 0;
        } else if (id == C0008R$id.op_fingerprint_icon) {
            this.mType = 1;
        }
        Log.d("OpCircleImageView", "init view: " + this.mType);
        if (this.mType == 3) {
            Drawable drawable = this.mContext.getResources().getDrawable(C0006R$drawable.fod_flash_icon);
            if (drawable.getMinimumHeight() == 0) {
                z = false;
            }
            this.mHasCustomizedHightlight = z;
            setBackgroundDrawable(drawable);
            setScaleType(ImageView.ScaleType.FIT_CENTER);
            this.mRadius = this.mContext.getResources().getDimensionPixelOffset(C0005R$dimen.op_biometric_icon_flash_width);
        }
        initPaint();
        this.mDefaultBacklight = ((PowerManager) context.getSystemService(PowerManager.class)).getDefaultScreenBrightnessSetting();
    }

    private void initPaint() {
        new PaintFlagsDrawFilter(0, 3);
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setAntiAlias(true);
        if (this.mType == 3 && !this.mHasCustomizedHightlight) {
            this.mPaint.setColor(this.mContext.getResources().getColor(C0004R$color.fingerprint_highlight_color));
        }
        this.mPaint.setStyle(Paint.Style.FILL);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = this.mType;
        if (i == 3) {
            if (i != 3 || !this.mHasCustomizedHightlight) {
                float measuredHeight = (float) getMeasuredHeight();
                float measuredWidth = (float) getMeasuredWidth();
                if (this.mPath == null) {
                    Path path = new Path();
                    this.mPath = path;
                    float f = measuredWidth / 2.0f;
                    path.addCircle(f, measuredHeight / 2.0f, (float) Math.min((double) f, ((double) measuredHeight) / 2.0d), Path.Direction.CCW);
                    this.mPath.close();
                }
                canvas.drawCircle(measuredWidth / 2.0f, measuredHeight / 2.0f, (float) (this.mRadius / 2), this.mPaint);
            }
        }
    }

    private void updateIconDim() {
        int dimAlpha = getDimAlpha();
        float dimensionPixelSize = (float) this.mContext.getResources().getDimensionPixelSize(C0005R$dimen.op_fingerprint_icon_dim_alpha_precentage);
        float f = ((float) dimAlpha) / 255.0f;
        float f2 = ((float) SystemProperties.getInt("sys.fod.icon.dim", (int) dimensionPixelSize)) / 100.0f;
        float f3 = (-0.8458527f * f) + 1.11275f;
        float f4 = ((float) SystemProperties.getInt("sys.fod.icon.cust", 0)) / 100.0f;
        if (f4 != 0.0f) {
            f3 = f4;
        }
        Log.d("OpCircleImageView", "updateIconDim: " + dimAlpha + ", " + dimensionPixelSize + "alpha = " + f + ", ratio = " + f2 + ", (alpha * ratio):" + (f * f2) + ", converseAlpha:" + f3 + ", custBright:" + f4);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setScale(f3, f3, f3, 1.0f);
        getBackground().setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    }

    @Override // android.widget.ImageView, android.view.View
    public void setVisibility(int i) {
        super.setVisibility(i);
        if (this.mType == 1 && i == 0) {
            updateIconDim();
        }
    }

    public void onBrightnessChange() {
        updateIconDim();
    }

    public void updateLayoutDimension(boolean z) {
        if (this.mType == 3) {
            this.mRadius = this.mContext.getResources().getDimensionPixelOffset(z ? C0005R$dimen.op_biometric_icon_flash_width_2k : C0005R$dimen.op_biometric_icon_flash_width_1080p);
        }
    }
}
