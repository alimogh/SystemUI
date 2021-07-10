package com.oneplus.plugin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import java.util.Random;
public class OpRippleView extends View {
    public static int MESSAGE_DELAY = SystemProperties.getInt("ripple.sys.value", 20);
    private int COLOR = Color.parseColor("#888888");
    private float DURATION = 1000.0f;
    private float END_RADIUS_FISRT = 120.0f;
    private float END_RADIUS_SECOND = 150.0f;
    private float START_RADIUS_FIRST = 30.0f;
    private float START_RADIUS_SECOND = 50.0f;
    private int STROKE_WIDTH_FIRST = 4;
    private int STROKE_WIDTH_SECOUND = 2;
    private Handler handler = new Handler() { // from class: com.oneplus.plugin.OpRippleView.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            OpRippleView.this.invalidate();
            if (OpRippleView.this.isStartRipple) {
                if (OpRippleView.this.rippleFirstRadius != 0 || OpRippleView.this.rippleSecendRadius <= 0) {
                    OpRippleView.access$108(OpRippleView.this);
                }
                if (((float) OpRippleView.this.rippleFirstRadius) > OpRippleView.this.DURATION / ((float) OpRippleView.MESSAGE_DELAY)) {
                    OpRippleView.this.rippleFirstRadius = 0;
                }
                OpRippleView.access$208(OpRippleView.this);
                if (((float) OpRippleView.this.rippleSecendRadius) > OpRippleView.this.DURATION / ((float) OpRippleView.MESSAGE_DELAY)) {
                    OpRippleView.this.rippleSecendRadius = 0;
                }
                if (OpRippleView.this.rippleFirstRadius == 0 && OpRippleView.this.rippleSecendRadius == 0) {
                    sendEmptyMessageDelayed(0, 1000);
                } else {
                    sendEmptyMessageDelayed(0, 20);
                }
            }
        }
    };
    private boolean isStartRipple;
    private int mClickCount = 0;
    private float mOffsetFirst;
    private float mOffsetSecond;
    private int mPositionX;
    private int mPositionY;
    private Paint mRipplePaintFirst = new Paint();
    private Paint mRipplePaintSecond = new Paint();
    private int mScreenHeight;
    private int mScreenWidth;
    private int rippleFirstRadius = -5;
    private int rippleSecendRadius = 0;

    static /* synthetic */ int access$108(OpRippleView opRippleView) {
        int i = opRippleView.rippleFirstRadius;
        opRippleView.rippleFirstRadius = i + 1;
        return i;
    }

    static /* synthetic */ int access$208(OpRippleView opRippleView) {
        int i = opRippleView.rippleSecendRadius;
        opRippleView.rippleSecendRadius = i + 1;
        return i;
    }

    public OpRippleView(Context context) {
        super(context);
        init(context);
    }

    public OpRippleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public OpRippleView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void init(Context context) {
        this.mRipplePaintFirst.setAntiAlias(true);
        this.mRipplePaintFirst.setStyle(Paint.Style.STROKE);
        this.mRipplePaintSecond.setAntiAlias(true);
        this.mRipplePaintSecond.setStyle(Paint.Style.STROKE);
        Display defaultDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        this.mScreenWidth = defaultDisplay.getWidth();
        this.mScreenHeight = defaultDisplay.getHeight();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.isStartRipple) {
            int i = this.rippleFirstRadius;
            if (i >= 0) {
                this.mRipplePaintFirst.setAlpha((int) (Math.sin(((((double) MESSAGE_DELAY) * 3.1416d) / ((double) this.DURATION)) * ((double) i)) * 255.0d));
                canvas.drawCircle((float) this.mPositionX, (float) this.mPositionY, this.START_RADIUS_FIRST + (this.mOffsetFirst * ((float) this.rippleFirstRadius)), this.mRipplePaintFirst);
            }
            int i2 = this.rippleSecendRadius;
            if (i2 >= 0) {
                this.mRipplePaintSecond.setAlpha((int) (Math.sin(((((double) MESSAGE_DELAY) * 3.1416d) / ((double) this.DURATION)) * ((double) i2)) * 255.0d));
                canvas.drawCircle((float) this.mPositionX, (float) this.mPositionY, this.START_RADIUS_SECOND + (this.mOffsetSecond * ((float) this.rippleSecendRadius)), this.mRipplePaintSecond);
            }
        }
    }

    public void startRipple() {
        if (MESSAGE_DELAY != 0) {
            generatePosition();
            startRipple(0);
        }
    }

    public void startRipple(int i) {
        this.isStartRipple = true;
        this.handler.sendEmptyMessageDelayed(0, (long) i);
    }

    public void stopRipple() {
        this.isStartRipple = false;
        this.handler.removeMessages(0);
    }

    public void prepare() {
        this.mClickCount = 0;
        generatePosition();
        float f = this.END_RADIUS_FISRT;
        float f2 = this.START_RADIUS_FIRST;
        int i = MESSAGE_DELAY;
        float f3 = this.DURATION;
        this.mOffsetFirst = ((f - f2) * ((float) i)) / f3;
        this.mOffsetSecond = ((this.END_RADIUS_SECOND - f2) * ((float) i)) / f3;
        this.mRipplePaintFirst.setStrokeWidth((float) this.STROKE_WIDTH_FIRST);
        this.mRipplePaintFirst.setColor(this.COLOR);
        this.mRipplePaintSecond.setStrokeWidth((float) this.STROKE_WIDTH_SECOUND);
        this.mRipplePaintSecond.setColor(this.COLOR);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean isValidPosition = isValidPosition(motionEvent);
        Log.d("OpRippleView", "onTouchEvent: isValid = " + isValidPosition);
        if (isValidPosition) {
            int i = this.mClickCount;
            if (i == 3) {
                OpLsState.getInstance().getPreventModeCtrl().stopPreventMode();
            } else {
                this.mClickCount = i + 1;
                stopRipple();
                startRipple();
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    public void generatePosition() {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        Random random = new Random();
        int i7 = this.mScreenWidth;
        float f = this.END_RADIUS_FISRT;
        int i8 = this.mScreenHeight;
        int i9 = this.mClickCount;
        if (i9 == 0) {
            i4 = (int) (((float) (i7 / 2)) - f);
            i = (int) f;
            i2 = (int) (((float) (i8 / 2)) - f);
            i3 = (int) f;
            this.mPositionX = (int) (((float) random.nextInt(i4 - i)) + this.END_RADIUS_FISRT);
            this.mPositionY = (int) (((float) random.nextInt(i2 - i3)) + this.END_RADIUS_FISRT);
        } else {
            if (i9 == 1) {
                i5 = (int) (((float) i7) - f);
                i6 = (int) (((float) (i7 / 2)) + f);
                i2 = (int) (((float) (i8 / 2)) - f);
                i3 = (int) f;
                this.mPositionX = (int) (((float) (random.nextInt(i5 - i6) + (this.mScreenWidth / 2))) + this.END_RADIUS_FISRT);
                this.mPositionY = (int) (((float) random.nextInt(i2 - i3)) + this.END_RADIUS_FISRT);
            } else if (i9 == 2) {
                i5 = (int) (((float) i7) - f);
                i6 = (int) (((float) (i7 / 2)) + f);
                int i10 = (int) (((float) i8) - f);
                i3 = (int) (((float) (i8 / 2)) + f);
                this.mPositionX = (int) (((float) (random.nextInt(i5 - i6) + (this.mScreenWidth / 2))) + this.END_RADIUS_FISRT);
                this.mPositionY = (int) (((float) (random.nextInt(i10 - i3) + (this.mScreenHeight / 2))) + this.END_RADIUS_FISRT);
                i2 = i10;
            } else {
                i4 = (int) (((float) (i7 / 2)) - f);
                i = (int) f;
                int i11 = (int) (((float) i8) - f);
                i3 = (int) (((float) (i8 / 2)) + f);
                this.mPositionX = (int) (((float) random.nextInt(i4 - i)) + this.END_RADIUS_FISRT);
                this.mPositionY = (int) (((float) (random.nextInt(i11 - i3) + (this.mScreenHeight / 2))) + this.END_RADIUS_FISRT);
                i2 = i11;
            }
            i = i6;
            i4 = i5;
        }
        Log.d("OpRippleView", "generatePosition : click = " + this.mClickCount + ", " + i + " < x < " + i4 + ", " + i3 + " < y < " + i2);
        StringBuilder sb = new StringBuilder();
        sb.append("generatePosition: (");
        sb.append(this.mPositionX);
        sb.append(", ");
        sb.append(this.mPositionY);
        sb.append(")");
        Log.d("OpRippleView", sb.toString());
    }

    public boolean isValidPosition(MotionEvent motionEvent) {
        if (motionEvent.getAction() != 0) {
            return false;
        }
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        Log.d("OpRippleView", "isValidPosition: (" + x + ", " + y + ")");
        int i = this.mPositionX;
        float f = this.END_RADIUS_FISRT;
        if (x <= ((float) i) + f && x >= ((float) i) - f) {
            int i2 = this.mPositionY;
            if (y <= ((float) i2) + f && y > ((float) i2) - f) {
                Log.d("OpRippleView", "isValidPosition: true");
                return true;
            }
        }
        return false;
    }
}
