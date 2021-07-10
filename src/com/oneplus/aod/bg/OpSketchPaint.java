package com.oneplus.aod.bg;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.PathInterpolator;
import com.oneplus.aod.bg.OpSketchBitmapHelper;
import com.oneplus.aod.utils.OpCanvasAodHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
public class OpSketchPaint extends OpBasePaint {
    private static final int BURNIN_DIAMETER = SystemProperties.getInt("sys.sketch.diameter", 2);
    private static final boolean DRAW_TEST = SystemProperties.getBoolean("sys.sketch.test", false);
    private static final int MAX_CIRCLE_RAIDUS = SystemProperties.getInt("sys.sketch.circle.max", 115);
    private static final int MIN_CIRCLE_RAIDUS = SystemProperties.getInt("sys.sketch.circle.min", 15);
    private static final int SPREAD_DURATION = SystemProperties.getInt("sys.sketch.sp.duration", 300);
    private Paint mBgPaint;
    private int mBitmapHeight;
    private int mBitmapWidth;
    private BlackMaskGenerator mBlackMaskGenerator;
    private Paint mBurnInPaint;
    private int mBurnInStep;
    private int mBurnInX;
    private int[] mBurnInXPath1;
    private int[] mBurnInXPath2;
    private int mBurnInY;
    private int[] mBurnInYPath1;
    private int[] mBurnInYPath2;
    private Canvas mCanvas;
    private Paint mContourPaint;
    private ArrayList<OpSketchBitmapHelper.SketchPoint> mContourPoints = new ArrayList<>();
    private float mContourScaleX;
    private float mContourScaleY;
    private float mDeltaX;
    private float mDeltaY;
    private OnGenBurnInMaskDoneListener mListener = new OnGenBurnInMaskDoneListener() { // from class: com.oneplus.aod.bg.OpSketchPaint.4
        @Override // com.oneplus.aod.bg.OpSketchPaint.OnGenBurnInMaskDoneListener
        public void onGenBurnInMaskDone(Bitmap bitmap) {
            BitmapShader bitmapShader;
            Bitmap bitmap2;
            if (!(OpSketchPaint.this.mBurnInPaint.getShader() == null || (bitmap2 = (bitmapShader = (BitmapShader) OpSketchPaint.this.mBurnInPaint.getShader()).mBitmap) == null || bitmap2.isRecycled())) {
                bitmapShader.mBitmap.recycle();
            }
            if (bitmap != null) {
                Shader.TileMode tileMode = Shader.TileMode.REPEAT;
                OpSketchPaint.this.mBurnInPaint.setShader(new BitmapShader(bitmap, tileMode, tileMode));
                return;
            }
            OpSketchPaint.this.mBurnInPaint.setShader(null);
        }
    };
    private Paint[] mMaskPaint;
    private Path[] mMaskPath;
    private boolean mMoveForward;
    private PowerManager mPm;
    private float mScaleFactor;
    private OpSketchBitmapHelper mSketchBitmapHelper = new OpSketchBitmapHelper(MIN_CIRCLE_RAIDUS);
    private Bitmap mSpreadBitmap;
    private float mSpreadScaleX;
    private float mSpreadScaleY;

    public interface OnGenBurnInMaskDoneListener {
        void onGenBurnInMaskDone(Bitmap bitmap);
    }

    public OpSketchPaint() {
        Paint paint = new Paint();
        this.mBgPaint = paint;
        paint.setAntiAlias(true);
        this.mBgPaint.setColor(-16777216);
        this.mBgPaint.setStyle(Paint.Style.FILL);
        Paint paint2 = new Paint();
        this.mContourPaint = paint2;
        paint2.setAntiAlias(true);
        Paint paint3 = new Paint();
        this.mBurnInPaint = paint3;
        paint3.setAntiAlias(true);
        Paint[] paintArr = new Paint[4];
        this.mMaskPaint = paintArr;
        paintArr[0] = new Paint();
        this.mMaskPaint[0].setAntiAlias(false);
        this.mMaskPaint[0].setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        this.mMaskPaint[0].setColor(-16777216);
        this.mMaskPaint[0].setStyle(Paint.Style.FILL);
        this.mMaskPaint[1] = new Paint(this.mMaskPaint[0]);
        this.mMaskPaint[2] = new Paint(this.mMaskPaint[0]);
        this.mMaskPaint[3] = new Paint(this.mMaskPaint[0]);
        BlurMaskFilter blurMaskFilter = new BlurMaskFilter(10.0f, BlurMaskFilter.Blur.NORMAL);
        this.mMaskPaint[0].setMaskFilter(blurMaskFilter);
        this.mMaskPaint[1].setMaskFilter(blurMaskFilter);
        this.mMaskPaint[2].setMaskFilter(blurMaskFilter);
        this.mMaskPaint[3].setMaskFilter(blurMaskFilter);
        Path[] pathArr = new Path[4];
        this.mMaskPath = pathArr;
        pathArr[0] = new Path();
        this.mMaskPath[1] = new Path();
        this.mMaskPath[2] = new Path();
        this.mMaskPath[3] = new Path();
        this.mCanvas = new Canvas();
        this.mScaleFactor = 1.1f;
        this.mContourScaleY = 1.1f;
        this.mContourScaleX = 1.1f;
        float f = 1.1f * 4.0f;
        this.mSpreadScaleY = f;
        this.mSpreadScaleX = f;
        loadBurnInData();
        resetInner();
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.i(str, "properties: MIN_CIRCLE_RAIDUS= " + MIN_CIRCLE_RAIDUS + ", MAX_CIRCLE_RAIDUS= " + MAX_CIRCLE_RAIDUS + ", SPREAD_DURATION= " + SPREAD_DURATION);
        }
    }

    private void loadBurnInData() {
        String str = "0,20,20,-20,-20,40,40,-40,-40,60,60,-60,-60,0";
        String str2 = SystemProperties.get("sys.sketch.burnin.x.1", str);
        String str3 = "0,0,-20,-20,20,20,-40,-40,40,40,-60,-60,60,60";
        String str4 = SystemProperties.get("sys.sketch.burnin.y.1", str3);
        if (str2.length() != 0 && str4.length() != 0 && str2.length() == str4.length()) {
            str = str2;
            str3 = str4;
        } else if (Build.DEBUG_ONEPLUS) {
            Log.d(this.mTag, "use default burnin data");
        }
        this.mBurnInXPath1 = OpSketchBitmapHelper.convertToIntArray(str.split(","));
        this.mBurnInYPath1 = OpSketchBitmapHelper.convertToIntArray(str3.split(","));
        String str5 = "0,30,30,-30,-30,50,50,-50,-50,0";
        String str6 = SystemProperties.get("sys.sketch.burnin.x.2", str5);
        String str7 = "10,10,-30,-30,30,30,-50,-50,50,50";
        String str8 = SystemProperties.get("sys.sketch.burnin.y.2", str7);
        if (str6.length() != 0 && str8.length() != 0 && str6.length() == str8.length()) {
            str5 = str6;
            str7 = str8;
        } else if (Build.DEBUG_ONEPLUS) {
            Log.d(this.mTag, "use default burnin data");
        }
        this.mBurnInXPath2 = OpSketchBitmapHelper.convertToIntArray(str5.split(","));
        this.mBurnInYPath2 = OpSketchBitmapHelper.convertToIntArray(str7.split(","));
        if (Build.DEBUG_ONEPLUS) {
            String str9 = this.mTag;
            Log.d(str9, "burnin path: x1= " + Arrays.toString(this.mBurnInXPath1) + ", y1= " + Arrays.toString(this.mBurnInYPath1) + ", x2= " + Arrays.toString(this.mBurnInXPath2) + ", y2= " + Arrays.toString(this.mBurnInYPath2));
        }
    }

    @Override // com.oneplus.aod.bg.IBgPaint
    public void release() {
        Bitmap bitmap;
        Bitmap bitmap2;
        Log.d(this.mTag, "release");
        stopGeneratorIfPossible();
        this.mContourPoints.clear();
        BitmapShader bitmapShader = (BitmapShader) this.mContourPaint.getShader();
        if (!(bitmapShader == null || (bitmap2 = bitmapShader.mBitmap) == null || bitmap2.isRecycled())) {
            bitmapShader.mBitmap.recycle();
        }
        BitmapShader bitmapShader2 = (BitmapShader) this.mBurnInPaint.getShader();
        if (!(bitmapShader2 == null || (bitmap = bitmapShader2.mBitmap) == null || bitmap.isRecycled())) {
            bitmapShader2.mBitmap.recycle();
        }
        clearSpreadData();
    }

    @Override // com.oneplus.aod.bg.OpBasePaint, com.oneplus.aod.bg.IBgPaint
    public void setup(View view) {
        super.setup(view);
        if (view != null) {
            this.mPm = (PowerManager) view.getContext().getSystemService(PowerManager.class);
        }
    }

    @Override // com.oneplus.aod.bg.OpBasePaint, com.oneplus.aod.bg.IBgPaint
    public void onSizeChanged(int i, int i2) {
        int i3;
        super.onSizeChanged(i, i2);
        float f = this.mScaleFactor;
        int i4 = (int) (((float) this.mWidth) * f);
        int i5 = (int) (((float) this.mHeight) * f);
        int i6 = this.mBitmapWidth;
        if (!(i6 == 0 || (i3 = this.mBitmapHeight) == 0)) {
            this.mContourScaleX = ((float) i4) / (((float) i6) * 1.0f);
            this.mContourScaleY = ((float) i5) / (((float) i3) * 1.0f);
        }
        this.mSpreadScaleX = this.mContourScaleX * 4.0f;
        this.mSpreadScaleY = this.mContourScaleY * 4.0f;
        this.mDeltaX = (float) ((i4 - this.mWidth) / 2);
        this.mDeltaY = (float) ((i5 - this.mHeight) / 2);
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.i(str, "onSizeChanged: w=" + i + ", h= " + i2 + ", mBitmapWidth= " + this.mBitmapWidth + ", mBitmapHeight= " + this.mBitmapHeight + ", mContourScaleX= " + this.mContourScaleX + ", mContourScaleY= " + this.mContourScaleY + ", mDeltaX= " + (-this.mDeltaX) + ", mDeltaY= " + (-this.mDeltaY) + ", mScaleFactor= " + this.mScaleFactor);
        }
    }

    @Override // com.oneplus.aod.bg.IBgPaint
    public void onDetachedFromWindow() {
        clearSpreadData();
    }

    @Override // com.oneplus.aod.bg.OpBasePaint, com.oneplus.aod.bg.IBgPaint
    public void reset() {
        super.reset();
        this.mBgPaint.setColor(-16777216);
        this.mContourPaint.setColor(-16777216);
        for (Paint paint : this.mMaskPaint) {
            paint.setColor(-16777216);
        }
        for (Path path : this.mMaskPath) {
            path.reset();
        }
        resetInner();
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.bg.OpBasePaint
    public void onDraw(Canvas canvas) {
        Trace.beginSection("OpSketchPaint#onDraw");
        if (this.mMaskPaint[0].getColor() != -16777216 && this.mSpreadBitmap != null) {
            canvas.save();
            canvas.translate(-this.mDeltaX, -this.mDeltaY);
            canvas.scale(this.mSpreadScaleX * 2.0f, this.mSpreadScaleY * 2.0f);
            canvas.drawBitmap(this.mSpreadBitmap, 0.0f, 0.0f, (Paint) null);
            canvas.restore();
        } else if (DRAW_TEST) {
            canvas.drawRect(0.0f, 0.0f, (float) this.mWidth, (float) this.mHeight, this.mBgPaint);
            canvas.save();
            canvas.translate(-this.mDeltaX, -this.mDeltaY);
            canvas.scale(this.mSpreadScaleX, this.mSpreadScaleY);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3.0f);
            Iterator<OpSketchBitmapHelper.SketchPoint> it = this.mContourPoints.iterator();
            while (it.hasNext()) {
                OpSketchBitmapHelper.SketchPoint next = it.next();
                int direction = next.getDirection();
                if (direction == 0) {
                    paint.setColor(-65536);
                } else if (direction == 1) {
                    paint.setColor(-256);
                } else if (direction == 2) {
                    paint.setColor(-16776961);
                } else if (direction == 3) {
                    paint.setColor(-16711936);
                }
                canvas.drawPoint((float) ((Point) next).x, (float) ((Point) next).y, paint);
            }
            canvas.restore();
        } else {
            canvas.drawRect(0.0f, 0.0f, (float) this.mWidth, (float) this.mHeight, this.mBgPaint);
        }
        if (this.mContourPaint.getColor() != 0) {
            canvas.save();
            canvas.translate((-this.mDeltaX) + ((float) this.mBurnInX), (-this.mDeltaY) + ((float) this.mBurnInY));
            canvas.scale(this.mContourScaleX, this.mContourScaleY);
            canvas.drawRect(0.0f, 0.0f, (float) this.mBitmapWidth, (float) this.mBitmapHeight, this.mContourPaint);
            canvas.restore();
            if (!(this.mBurnInStep == -1 || this.mBurnInPaint.getShader() == null)) {
                canvas.drawRect(0.0f, 0.0f, (float) this.mWidth, (float) this.mHeight, this.mBurnInPaint);
            }
        }
        Trace.endSection();
    }

    @Override // com.oneplus.aod.bg.IBgPaint
    public ArrayList<Animator> genAodDisappearAnimation() {
        return getSketchAnimator();
    }

    @Override // com.oneplus.aod.bg.IBgPaint
    public void burnInProtect() {
        Log.i(this.mTag, "burnInProtect");
        PowerManager powerManager = this.mPm;
        if (powerManager == null || !powerManager.isInteractive()) {
            makeNextMove();
            this.mView.invalidate();
            return;
        }
        Log.d(this.mTag, "do nothing.");
    }

    @Override // com.oneplus.aod.bg.IBgPaint
    public void recover() {
        Log.i(this.mTag, "recover");
        resetInner();
        this.mView.invalidate();
    }

    @Override // com.oneplus.aod.bg.IBgPaint
    public void userActivityInAlwaysOn() {
        Log.i(this.mTag, "userActivityInAlwaysOn");
        resetInner();
        this.mView.invalidate();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetInner() {
        this.mBurnInStep = -1;
        this.mMoveForward = true;
        this.mBurnInY = 0;
        this.mBurnInX = 0;
    }

    private void makeNextMove() {
        if (this.mMoveForward) {
            int i = this.mBurnInStep;
            if (i == -1) {
                this.mBurnInStep = 1;
            } else if (i + 1 >= this.mBurnInXPath1.length) {
                this.mBurnInStep = this.mBurnInXPath2.length - 1;
                this.mMoveForward = false;
            } else {
                this.mBurnInStep = i + 1;
            }
        } else {
            int i2 = this.mBurnInStep;
            if (i2 - 1 < 0) {
                this.mBurnInStep = 0;
                this.mMoveForward = true;
            } else {
                this.mBurnInStep = i2 - 1;
            }
        }
        if (this.mMoveForward) {
            int[] iArr = this.mBurnInXPath1;
            int i3 = this.mBurnInStep;
            this.mBurnInX = iArr[i3];
            this.mBurnInY = this.mBurnInYPath1[i3];
        } else {
            int[] iArr2 = this.mBurnInXPath2;
            int i4 = this.mBurnInStep;
            this.mBurnInX = iArr2[i4];
            this.mBurnInY = this.mBurnInYPath2[i4];
        }
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "makeNextMove: move forward= " + this.mMoveForward + ", step= " + this.mBurnInStep + ", x= " + this.mBurnInX + ", y= " + this.mBurnInY);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void drawMaskPoint(Canvas canvas, int i) {
        if (!this.mMaskPath[i].isEmpty()) {
            canvas.drawPath(this.mMaskPath[i], this.mMaskPaint[i]);
        }
    }

    public void setupContour(OpCanvasAodHelper.Data data) {
        Bitmap image = data.getImage();
        this.mBitmapWidth = image.getWidth();
        this.mBitmapHeight = image.getHeight();
        Shader.TileMode tileMode = Shader.TileMode.CLAMP;
        this.mContourPaint.setShader(new BitmapShader(image, tileMode, tileMode));
        if (Build.DEBUG_ONEPLUS) {
            String str = this.mTag;
            Log.d(str, "spread bitmap width= " + (this.mBitmapWidth / 8) + ", height= " + (this.mBitmapHeight / 8));
        }
        this.mContourPoints.clear();
        this.mContourPoints.addAll(data.getList());
        this.mScaleFactor = data.getScale();
        stopGeneratorIfPossible();
        Display display = this.mView.getContext().getDisplay();
        Point point = new Point();
        display.getRealSize(point);
        BlackMaskGenerator blackMaskGenerator = new BlackMaskGenerator(this.mListener, point, this.mSketchBitmapHelper);
        this.mBlackMaskGenerator = blackMaskGenerator;
        blackMaskGenerator.start();
    }

    private void clearSpreadData() {
        this.mCanvas.setBitmap(null);
        Bitmap bitmap = this.mSpreadBitmap;
        if (bitmap != null && !bitmap.isRecycled()) {
            this.mSpreadBitmap.recycle();
        }
        this.mSpreadBitmap = null;
    }

    private void stopGeneratorIfPossible() {
        BlackMaskGenerator blackMaskGenerator = this.mBlackMaskGenerator;
        if (blackMaskGenerator != null && blackMaskGenerator.isAlive() && !this.mBlackMaskGenerator.isInterrupted()) {
            Log.d(this.mTag, "stopGenerator");
            this.mBlackMaskGenerator.interrupt();
            this.mBlackMaskGenerator = null;
        }
    }

    private ArrayList<Animator> getSketchAnimator() {
        ValueAnimator ofPropertyValuesHolder = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[0]);
        ofPropertyValuesHolder.setValues(PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f), PropertyValuesHolder.ofInt("radius", MIN_CIRCLE_RAIDUS, MAX_CIRCLE_RAIDUS));
        ofPropertyValuesHolder.setInterpolator(new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f));
        ofPropertyValuesHolder.setDuration((long) SPREAD_DURATION);
        ofPropertyValuesHolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.aod.bg.OpSketchPaint.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = valueAnimator.getAnimatedValue("alpha") != null ? ((Float) valueAnimator.getAnimatedValue("alpha")).floatValue() : 0.0f;
                int intValue = valueAnimator.getAnimatedValue("radius") != null ? ((Integer) valueAnimator.getAnimatedValue("radius")).intValue() : OpSketchPaint.MAX_CIRCLE_RAIDUS;
                OpSketchPaint.this.mBgPaint.setColor(Color.argb(floatValue, 0.0f, 0.0f, 0.0f));
                for (Path path : OpSketchPaint.this.mMaskPath) {
                    path.reset();
                }
                Iterator it = OpSketchPaint.this.mContourPoints.iterator();
                while (it.hasNext()) {
                    OpSketchBitmapHelper.SketchPoint sketchPoint = (OpSketchBitmapHelper.SketchPoint) it.next();
                    OpSketchPaint.this.mMaskPath[sketchPoint.getDirection()].addCircle((float) ((Point) sketchPoint).x, (float) ((Point) sketchPoint).y, (float) intValue, Path.Direction.CW);
                }
                if (OpSketchPaint.this.mSpreadBitmap != null) {
                    Trace.beginSection("OpSketchPaint#genSpreadBitmap");
                    OpSketchPaint.this.mSpreadBitmap.eraseColor(0);
                    OpSketchPaint.this.mCanvas.drawRect(0.0f, 0.0f, (float) OpSketchPaint.this.mCanvas.getWidth(), (float) OpSketchPaint.this.mCanvas.getHeight(), OpSketchPaint.this.mBgPaint);
                    if (OpSketchPaint.this.mContourPoints.size() > 0) {
                        OpSketchPaint.this.mCanvas.save();
                        OpSketchPaint.this.mCanvas.scale(0.5f, 0.5f);
                        OpSketchPaint opSketchPaint = OpSketchPaint.this;
                        opSketchPaint.drawMaskPoint(opSketchPaint.mCanvas, 0);
                        OpSketchPaint opSketchPaint2 = OpSketchPaint.this;
                        opSketchPaint2.drawMaskPoint(opSketchPaint2.mCanvas, 1);
                        OpSketchPaint opSketchPaint3 = OpSketchPaint.this;
                        opSketchPaint3.drawMaskPoint(opSketchPaint3.mCanvas, 2);
                        OpSketchPaint opSketchPaint4 = OpSketchPaint.this;
                        opSketchPaint4.drawMaskPoint(opSketchPaint4.mCanvas, 3);
                        OpSketchPaint.this.mCanvas.restore();
                    }
                    Trace.endSection();
                }
                if (Build.DEBUG_ONEPLUS) {
                    Log.d(OpSketchPaint.this.mTag, "spread update");
                }
                OpSketchPaint.this.mView.invalidate();
            }
        });
        ofPropertyValuesHolder.addListener(new Animator.AnimatorListener() { // from class: com.oneplus.aod.bg.OpSketchPaint.2
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d(OpSketchPaint.this.mTag, "spread start");
                }
                if (OpSketchPaint.this.mBitmapWidth > 0 && OpSketchPaint.this.mBitmapHeight > 0) {
                    OpSketchPaint opSketchPaint = OpSketchPaint.this;
                    opSketchPaint.mSpreadBitmap = Bitmap.createBitmap(opSketchPaint.mBitmapWidth / 8, OpSketchPaint.this.mBitmapHeight / 8, Bitmap.Config.ARGB_8888);
                    OpSketchPaint.this.mCanvas.setBitmap(OpSketchPaint.this.mSpreadBitmap);
                }
                OpSketchPaint.this.resetInner();
                OpSketchPaint.this.mView.invalidate();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (Build.DEBUG_ONEPLUS) {
                    Log.d(OpSketchPaint.this.mTag, "spread end");
                }
            }
        });
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        ofFloat.setInterpolator(new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f));
        ofFloat.setDuration(150L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.aod.bg.OpSketchPaint.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = valueAnimator.getAnimatedValue() != null ? ((Float) valueAnimator.getAnimatedValue()).floatValue() : 0.0f;
                OpSketchPaint.this.mContourPaint.setColor(Color.argb(floatValue, 0.0f, 0.0f, 0.0f));
                for (Paint paint : OpSketchPaint.this.mMaskPaint) {
                    paint.setColor(Color.argb(floatValue, 0.0f, 0.0f, 0.0f));
                }
            }
        });
        ArrayList<Animator> arrayList = new ArrayList<>();
        arrayList.add(ofPropertyValuesHolder);
        arrayList.add(ofFloat);
        return arrayList;
    }

    /* access modifiers changed from: private */
    public static class BlackMaskGenerator extends Thread {
        private OpSketchBitmapHelper mHelper;
        private OnGenBurnInMaskDoneListener mListener;
        private Point mSize;

        public BlackMaskGenerator(OnGenBurnInMaskDoneListener onGenBurnInMaskDoneListener, Point point, OpSketchBitmapHelper opSketchBitmapHelper) {
            this.mListener = onGenBurnInMaskDoneListener;
            this.mSize = point;
            this.mHelper = opSketchBitmapHelper;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Log.d("BlackMaskGenerator", "start");
            Bitmap genBurninMask = this.mHelper.genBurninMask(this.mSize, OpSketchPaint.BURNIN_DIAMETER);
            OnGenBurnInMaskDoneListener onGenBurnInMaskDoneListener = this.mListener;
            if (onGenBurnInMaskDoneListener != null) {
                onGenBurnInMaskDoneListener.onGenBurnInMaskDone(genBurninMask);
                this.mListener = null;
            }
            this.mHelper = null;
            Log.d("BlackMaskGenerator", "end");
        }
    }
}
