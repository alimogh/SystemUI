package com.android.keyguard;

import android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.C0001R$array;
import com.android.systemui.C0005R$dimen;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.R$styleable;
import com.oneplus.keyguard.OpPasswordTextViewForPin;
import com.oneplus.util.OpUtils;
public class NumPadKey extends ViewGroup {
    private static final String TAG = NumPadKey.class.getSimpleName();
    static String[] sKlondike;
    private int mDigit;
    private final TextView mDigitText;
    private final TextView mKlondikeText;
    private View.OnClickListener mListener;
    private final LockPatternUtils mLockPatternUtils;
    private final PowerManager mPM;
    private PasswordTextView mTextView;
    private OpPasswordTextViewForPin mTextViewForPin;
    private int mTextViewResId;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void userActivity() {
        this.mPM.userActivity(SystemClock.uptimeMillis(), false);
    }

    public NumPadKey(Context context) {
        this(context, null);
    }

    public NumPadKey(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NumPadKey(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, C0011R$layout.keyguard_num_pad_key);
    }

    /* JADX INFO: finally extract failed */
    protected NumPadKey(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i);
        int i3;
        this.mDigit = -1;
        this.mListener = new View.OnClickListener() { // from class: com.android.keyguard.NumPadKey.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (NumPadKey.this.mTextViewForPin == null && NumPadKey.this.mTextView == null && NumPadKey.this.mTextViewResId > 0) {
                    View findViewById = NumPadKey.this.getRootView().findViewById(NumPadKey.this.mTextViewResId);
                    if (findViewById != null && (findViewById instanceof PasswordTextView)) {
                        NumPadKey.this.mTextView = (PasswordTextView) findViewById;
                    } else if (findViewById != null && (findViewById instanceof OpPasswordTextViewForPin)) {
                        NumPadKey.this.mTextViewForPin = (OpPasswordTextViewForPin) findViewById;
                    }
                }
                if (NumPadKey.this.mTextView != null && NumPadKey.this.mTextView.isEnabled()) {
                    NumPadKey.this.mTextView.append(Character.forDigit(NumPadKey.this.mDigit, 10));
                } else if (NumPadKey.this.mTextViewForPin != null && NumPadKey.this.mTextViewForPin.isEnabled()) {
                    NumPadKey.this.mTextViewForPin.append(Character.forDigit(NumPadKey.this.mDigit, 10));
                }
                NumPadKey.this.userActivity();
            }
        };
        setFocusable(true);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.NumPadKey);
        try {
            this.mDigit = obtainStyledAttributes.getInt(R$styleable.NumPadKey_digit, this.mDigit);
            this.mTextViewResId = obtainStyledAttributes.getResourceId(R$styleable.NumPadKey_textView, 0);
            obtainStyledAttributes.recycle();
            setOnClickListener(this.mListener);
            setOnHoverListener(new LiftToActivateListener(context));
            this.mLockPatternUtils = new LockPatternUtils(context);
            this.mPM = (PowerManager) ((ViewGroup) this).mContext.getSystemService("power");
            ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(i2, (ViewGroup) this, true);
            TextView textView = (TextView) findViewById(C0008R$id.digit_text);
            this.mDigitText = textView;
            textView.setText(Integer.toString(this.mDigit));
            this.mKlondikeText = (TextView) findViewById(C0008R$id.klondike_text);
            if (OpUtils.is2KResolution()) {
                this.mDigitText.setTextSize(0, (float) OpUtils.convertPxByResolutionProportion((float) ((ViewGroup) this).mContext.getResources().getDimensionPixelSize(C0005R$dimen.keyguard_styles_widget_textview_numpadkey_textsize), 1080));
            }
            TextView textView2 = this.mKlondikeText;
            if (textView2 != null) {
                textView2.setVisibility(8);
            }
            if (this.mDigit >= 0) {
                if (sKlondike == null) {
                    sKlondike = getResources().getStringArray(C0001R$array.lockscreen_num_pad_klondike);
                }
                String[] strArr = sKlondike;
                if (strArr != null && strArr.length > (i3 = this.mDigit)) {
                    String str = strArr[i3];
                    if (str.length() > 0) {
                        this.mKlondikeText.setText(str);
                    } else {
                        this.mKlondikeText.setVisibility(4);
                    }
                }
            }
            TypedArray obtainStyledAttributes2 = context.obtainStyledAttributes(attributeSet, R.styleable.View);
            if (!obtainStyledAttributes2.hasValueOrEmpty(13)) {
                setBackground(((ViewGroup) this).mContext.getDrawable(C0006R$drawable.ripple_drawable_pin));
            }
            obtainStyledAttributes2.recycle();
            setContentDescription(this.mDigitText.getText().toString());
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            doHapticKeyClick();
        }
        return super.onTouchEvent(motionEvent);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        measureChildren(i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int measuredHeight = this.mDigitText.getMeasuredHeight();
        int measuredHeight2 = this.mKlondikeText.getMeasuredHeight();
        int height = (getHeight() / 2) - (measuredHeight / 2);
        int width = getWidth() / 2;
        int measuredWidth = width - (this.mDigitText.getMeasuredWidth() / 2);
        int i5 = height + measuredHeight;
        TextView textView = this.mDigitText;
        textView.layout(measuredWidth, height, textView.getMeasuredWidth() + measuredWidth, i5);
        int i6 = (int) (((float) i5) - (((float) measuredHeight2) * 0.35f));
        int i7 = measuredHeight2 + i6;
        if (OpUtils.DEBUG_ONEPLUS) {
            String str = TAG;
            Log.i(str, "mDigit:" + this.mDigit + ", getHeight():" + getHeight() + ", getMeasuredHeight():" + getMeasuredHeight() + ", totalHeight:" + measuredHeight + ", top:" + i6 + ", mDigitText.getHeight():" + this.mDigitText.getHeight() + ", mDigitText.getMeasuredHeight():" + this.mDigitText.getMeasuredHeight());
        }
        int measuredWidth2 = width - (this.mKlondikeText.getMeasuredWidth() / 2);
        TextView textView2 = this.mKlondikeText;
        textView2.layout(measuredWidth2, i6, textView2.getMeasuredWidth() + measuredWidth2, i7);
    }

    public void doHapticKeyClick() {
        if (this.mLockPatternUtils.isTactileFeedbackEnabled()) {
            performHapticFeedback(1, 3);
        }
    }
}
