package com.oneplus.aod;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.util.OpFeatures;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.oneplus.util.OpUtils;
import java.io.IOException;
import java.io.InputStream;
public class OpAodLightEffectContainer extends FrameLayout {
    private int mAnimateIndex = 0;
    private Bitmap mAnimationBgLeft;
    private Bitmap mAnimationBgRight;
    private Handler mBgHandler;
    private Bitmap[] mBitmapLeft;
    private Bitmap[] mBitmapRight;
    private int mDecodeIndex = 0;
    private final Runnable mFrameRunnable = new Runnable() { // from class: com.oneplus.aod.OpAodLightEffectContainer.1
        @Override // java.lang.Runnable
        public void run() {
            Log.d("OpAodLightEffectContainer", "mFrameRunnable: " + OpAodLightEffectContainer.this.mAnimateIndex);
            try {
                if (OpAodLightEffectContainer.this.mAnimateIndex >= 0 && OpAodLightEffectContainer.this.mBitmapLeft != null && OpAodLightEffectContainer.this.mAnimateIndex < OpAodLightEffectContainer.this.mBitmapLeft.length) {
                    if (OpAodLightEffectContainer.this.mBitmapLeft[OpAodLightEffectContainer.this.mAnimateIndex] == null || !OpAodLightEffectContainer.this.mBitmapLeft[OpAodLightEffectContainer.this.mAnimateIndex].isRecycled()) {
                        OpAodLightEffectContainer.this.mLeftView.setImageBitmap(OpAodLightEffectContainer.this.mBitmapLeft[OpAodLightEffectContainer.this.mAnimateIndex]);
                    } else {
                        Log.d("OpAodLightEffectContainer", "mBitmapLeft is recycled");
                        OpAodLightEffectContainer.access$008(OpAodLightEffectContainer.this);
                        return;
                    }
                }
                if (OpAodLightEffectContainer.this.mAnimateIndex >= 0 && OpAodLightEffectContainer.this.mBitmapRight != null && OpAodLightEffectContainer.this.mAnimateIndex < OpAodLightEffectContainer.this.mBitmapRight.length) {
                    if (OpAodLightEffectContainer.this.mBitmapRight[OpAodLightEffectContainer.this.mAnimateIndex] == null || !OpAodLightEffectContainer.this.mBitmapRight[OpAodLightEffectContainer.this.mAnimateIndex].isRecycled()) {
                        OpAodLightEffectContainer.this.mRightView.setImageBitmap(OpAodLightEffectContainer.this.mBitmapRight[OpAodLightEffectContainer.this.mAnimateIndex]);
                    } else {
                        Log.d("OpAodLightEffectContainer", "mBitmapRight is recycled");
                        OpAodLightEffectContainer.access$008(OpAodLightEffectContainer.this);
                        return;
                    }
                }
                if (OpAodLightEffectContainer.this.mAnimateIndex < 100) {
                    OpAodLightEffectContainer.this.mHandler.postDelayed(OpAodLightEffectContainer.this.mFrameRunnable, (long) OpAodLightEffectContainer.this.mFramesDuration);
                } else {
                    OpAodLightEffectContainer.this.mLeftView.setImageBitmap(null);
                    OpAodLightEffectContainer.this.mRightView.setImageBitmap(null);
                }
            } catch (Exception e) {
                Log.w("OpAodLightEffectContainer", "mFrameRunnable error: " + e.getMessage());
            }
            OpAodLightEffectContainer.access$008(OpAodLightEffectContainer.this);
        }
    };
    private int mFramesDuration = 16;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private ImageView mLeftView;
    private ValueAnimator mLightAnimator;
    private int mLightIndex = 0;
    private ImageView mRightView;

    static /* synthetic */ int access$008(OpAodLightEffectContainer opAodLightEffectContainer) {
        int i = opAodLightEffectContainer.mAnimateIndex;
        opAodLightEffectContainer.mAnimateIndex = i + 1;
        return i;
    }

    public OpAodLightEffectContainer(Context context) {
        super(context);
        initViews();
    }

    public OpAodLightEffectContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public OpAodLightEffectContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public OpAodLightEffectContainer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        initViews();
    }

    private void initViews() {
        this.mLeftView = (ImageView) findViewById(C0008R$id.notification_animation_left);
        this.mRightView = (ImageView) findViewById(C0008R$id.notification_animation_right);
        relayoutViews();
    }

    private void relayoutViews() {
        int i = this.mLightIndex;
        boolean z = i == 10 || i == 20;
        int dimensionPixelSize = ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(z ? C0005R$dimen.op_aod_light_effect_my_width : C0005R$dimen.op_aod_light_effect_width);
        this.mLeftView.getLayoutParams().width = dimensionPixelSize;
        this.mRightView.getLayoutParams().width = dimensionPixelSize;
        if (z) {
            setScaleY(1.0f);
            setAlpha(1.0f);
            this.mLeftView.setScaleType(ImageView.ScaleType.FIT_XY);
            this.mRightView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else if (!OpFeatures.isSupport(new int[]{324})) {
            this.mLeftView.setScaleType(ImageView.ScaleType.FIT_START);
            this.mRightView.setScaleType(ImageView.ScaleType.FIT_END);
        }
    }

    public void setLightIndex(int i) {
        if (this.mLightIndex == i) {
            return;
        }
        if (OpUtils.isMCLVersion() || i != 10) {
            int i2 = this.mLightIndex;
            if (i2 == 10) {
                this.mFramesDuration = 16;
            } else if (i2 == 20) {
                this.mFramesDuration = 40;
            }
            boolean z = false;
            int i3 = SystemProperties.getInt("sys.debug.notify.light.frames", 0);
            if (i3 != 0) {
                this.mFramesDuration = i3;
            }
            Log.d("OpAodLightEffectContainer", "setLightIndex, mFramesDuration: " + this.mFramesDuration);
            int i4 = this.mLightIndex;
            if (i4 == 10 || i == 10 || i4 == 20 || i == 20) {
                z = true;
            }
            this.mLightIndex = i;
            if (z) {
                relayoutViews();
                return;
            }
            return;
        }
        Log.d("OpAodLightEffectContainer", "Set horizon light failed. Invalid index: " + i);
    }

    public void resetNotificationAnimView() {
        int i = this.mLightIndex;
        if (!(i == 10 || i == 20)) {
            setScaleY(0.0f);
            setAlpha(0.0f);
        }
        Log.d("OpAodLightEffectContainer", "resetNotificationAnimView");
        this.mRightView.setImageBitmap(null);
        this.mLeftView.setImageBitmap(null);
        Bitmap bitmap = this.mAnimationBgRight;
        if (bitmap != null) {
            bitmap.recycle();
            this.mAnimationBgRight = null;
        }
        Bitmap bitmap2 = this.mAnimationBgLeft;
        if (bitmap2 != null) {
            bitmap2.recycle();
            this.mAnimationBgLeft = null;
        }
        Handler handler = this.mBgHandler;
        if (handler != null) {
            handler.removeMessages(256);
            this.mBgHandler.removeMessages(272);
            this.mBgHandler = null;
        }
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null) {
            handlerThread.getLooper().quit();
            this.mHandlerThread = null;
        }
        Handler handler2 = this.mHandler;
        if (handler2 != null) {
            handler2.removeCallbacks(this.mFrameRunnable);
            this.mHandler = null;
        }
        this.mDecodeIndex = 0;
        this.mAnimateIndex = 0;
        Bitmap[] bitmapArr = this.mBitmapLeft;
        if (bitmapArr != null) {
            for (Bitmap bitmap3 : bitmapArr) {
                if (bitmap3 != null) {
                    bitmap3.recycle();
                }
            }
        }
        Bitmap[] bitmapArr2 = this.mBitmapRight;
        if (bitmapArr2 != null) {
            for (Bitmap bitmap4 : bitmapArr2) {
                if (bitmap4 != null) {
                    bitmap4.recycle();
                }
            }
        }
    }

    public void showLight() {
        int i = this.mLightIndex;
        if (i == 10 || i == 20) {
            prepareResources();
            int i2 = this.mAnimateIndex;
            if (i2 <= 0 || i2 >= 100) {
                this.mHandler.removeCallbacks(this.mFrameRunnable);
                this.mHandler.postDelayed(this.mFrameRunnable, 350);
                return;
            }
            return;
        }
        setAlpha(1.0f);
        ValueAnimator valueAnimator = this.mLightAnimator;
        if (valueAnimator == null || (valueAnimator != null && !valueAnimator.isRunning())) {
            loadResources();
            animateNotification();
        }
    }

    private void prepareResources() {
        Log.d("OpAodLightEffectContainer", "prepareResources");
        if (this.mBitmapLeft == null) {
            this.mBitmapLeft = new Bitmap[100];
        }
        if (this.mBitmapRight == null) {
            this.mBitmapRight = new Bitmap[100];
        }
        if (this.mHandler == null) {
            this.mHandler = new Handler();
        }
        startHandlerThread();
    }

    private void startHandlerThread() {
        if (this.mHandlerThread == null) {
            HandlerThread handlerThread = new HandlerThread("HandlerThread");
            this.mHandlerThread = handlerThread;
            handlerThread.start();
            AnonymousClass2 r0 = new Handler(this.mHandlerThread.getLooper()) { // from class: com.oneplus.aod.OpAodLightEffectContainer.2
                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    super.handleMessage(message);
                    int i = message.what;
                    if (i != 256) {
                        if (i == 272) {
                            OpAodLightEffectContainer.this.decodeCyBitmap();
                        }
                    } else if (OpAodLightEffectContainer.this.mDecodeIndex < 100 && OpAodLightEffectContainer.this.mBgHandler != null) {
                        OpAodLightEffectContainer.this.mBgHandler.sendEmptyMessage(256);
                        OpAodLightEffectContainer.this.decodeBitmap();
                    }
                }
            };
            this.mBgHandler = r0;
            int i = this.mLightIndex;
            if (i == 10) {
                r0.sendEmptyMessage(256);
            } else if (i == 20) {
                r0.sendEmptyMessage(272);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void decodeCyBitmap() {
        if (this.mBitmapLeft == null || this.mBitmapRight == null) {
            Log.d("OpAodLightEffectContainer", "Failed to decodeCyBitmap. mBitmapLeft=" + this.mBitmapLeft + " mBitmapRight=" + this.mBitmapRight);
            return;
        }
        Resources resources = ((FrameLayout) this).mContext.getResources();
        InputStream inputStream = null;
        for (int i = 1; i < 100; i++) {
            String str = "aod_light_cy_l_" + String.format("%02d", Integer.valueOf(i));
            String str2 = "aod_light_cy_r_" + String.format("%02d", Integer.valueOf(i));
            int identifier = resources.getIdentifier(str, "drawable", ((FrameLayout) this).mContext.getPackageName());
            int identifier2 = resources.getIdentifier(str2, "drawable", ((FrameLayout) this).mContext.getPackageName());
            Log.d("OpAodLightEffectContainer", "name_l:" + str + ", name_r:" + str2 + ", id_l:" + identifier + ", id_r:" + identifier2);
            if (identifier == 0 || identifier2 == 0) {
                Log.d("OpAodLightEffectContainer", "id_l or id_r 0, end");
                return;
            }
            Bitmap decodeStream = BitmapFactory.decodeStream(resources.openRawResource(identifier));
            if (decodeStream == null || !decodeStream.isRecycled()) {
                if (decodeStream == null) {
                    Log.d("OpAodLightEffectContainer", "decode bitmap_l null");
                }
                this.mBitmapLeft[i] = decodeStream;
                inputStream = resources.openRawResource(identifier2);
                Bitmap decodeStream2 = BitmapFactory.decodeStream(inputStream);
                if (decodeStream2 == null || !decodeStream2.isRecycled()) {
                    if (decodeStream2 == null) {
                        Log.d("OpAodLightEffectContainer", "decode bitmap_r null");
                    }
                    this.mBitmapRight[i] = decodeStream2;
                } else {
                    Log.w("OpAodLightEffectContainer", "decodeBitmapR but recycled");
                    return;
                }
            } else {
                Log.w("OpAodLightEffectContainer", "decodeBitmapL but recycled");
                return;
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException unused) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void decodeBitmap() {
        if (this.mBitmapLeft == null || this.mBitmapRight == null) {
            Log.d("OpAodLightEffectContainer", "Failed to decodeBitmap. mBitmapLeft=" + this.mBitmapLeft + " mBitmapRight=" + this.mBitmapRight);
            return;
        }
        int i = this.mDecodeIndex;
        Resources resources = ((FrameLayout) this).mContext.getResources();
        Bitmap decodeResource = BitmapFactory.decodeResource(resources, resources.getIdentifier("aod_light_my_" + String.format("%02d", Integer.valueOf(i)), "drawable", ((FrameLayout) this).mContext.getPackageName()));
        if (decodeResource == null || !decodeResource.isRecycled()) {
            if (decodeResource == null) {
                Log.d("OpAodLightEffectContainer", "decode bitmap null");
            }
            this.mBitmapRight[i] = decodeResource;
            this.mBitmapLeft[i] = flip(decodeResource);
            this.mDecodeIndex++;
            return;
        }
        Log.w("OpAodLightEffectContainer", "decodeBitmap but recycled");
        this.mDecodeIndex++;
    }

    private void loadResources() {
        this.mAnimationBgRight = BitmapFactory.decodeResource(((FrameLayout) this).mContext.getResources(), C0006R$drawable.aod_notification_light_right);
        Bitmap decodeResource = BitmapFactory.decodeResource(((FrameLayout) this).mContext.getResources(), C0006R$drawable.aod_notification_light_left);
        this.mAnimationBgLeft = decodeResource;
        this.mLeftView.setImageBitmap(decodeResource);
        this.mRightView.setImageBitmap(this.mAnimationBgRight);
    }

    private Bitmap flip(Bitmap bitmap) {
        if (bitmap != null) {
            try {
                Matrix matrix = new Matrix();
                matrix.preScale(-1.0f, 1.0f);
                if (!bitmap.isRecycled()) {
                    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                }
                Log.w("OpAodLightEffectContainer", "src is recycled");
                return null;
            } catch (RuntimeException e) {
                Log.w("OpAodLightEffectContainer", "flip error: " + e.getMessage());
            }
        }
        return null;
    }

    private void animateNotification() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 2.0f);
        this.mLightAnimator = ofFloat;
        ofFloat.setDuration(2000L);
        this.mLightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.oneplus.aod.OpAodLightEffectContainer.3
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                OpAodLightEffectContainer.this.setScaleY(floatValue);
                Log.d("OpAodLightEffectContainer", "progress=" + floatValue);
                float f = 1.0f;
                if (floatValue <= 0.3f) {
                    f = floatValue / 0.3f;
                } else if (floatValue >= 1.0f) {
                    f = 2.0f - floatValue;
                }
                OpAodLightEffectContainer.this.setAlpha(f);
            }
        });
        this.mLightAnimator.start();
    }
}
