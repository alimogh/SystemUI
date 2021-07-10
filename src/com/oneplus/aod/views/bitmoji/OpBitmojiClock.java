package com.oneplus.aod.views.bitmoji;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0016R$style;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.oneplus.aod.utils.OpAodDimenHelper;
import com.oneplus.aod.utils.OpAodSettings;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.views.IOpAodClock;
import com.oneplus.util.OpUtils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
public class OpBitmojiClock extends FrameLayout implements IOpAodClock, OpBitmojiManager.OnTriggerChangedListener {
    private static final boolean DEBUG_TEST = SystemProperties.getBoolean("sys.aod.bitmoji.support", false);
    private ImageView mBitmojiIcon;
    private int mBitmojiIconHeightId;
    private int mBitmojiIconWidthId;
    private OpBitmojiManager mBitmojiManager;
    private String mCurrentPackId;
    private int mDateMarginTopId;
    private TextView mDateView;
    private Paint mPaint;
    private TextView mTimeView;

    public OpBitmojiClock(Context context) {
        this(context, null);
    }

    public OpBitmojiClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpBitmojiClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        View inflate = FrameLayout.inflate(context, C0011R$layout.op_aod_bitmoji_clock, null);
        this.mBitmojiIcon = (ImageView) inflate.findViewById(C0008R$id.bitmoji_icon);
        this.mDateView = (TextView) inflate.findViewById(C0008R$id.date);
        this.mTimeView = (TextView) inflate.findViewById(C0008R$id.time);
        setupAttributes(attributeSet);
        addView(inflate, new FrameLayout.LayoutParams(-1, -2));
        setLayoutParams(new FrameLayout.LayoutParams(context, attributeSet));
        try {
            this.mBitmojiManager = (OpBitmojiManager) Dependency.get(OpBitmojiManager.class);
        } catch (IllegalArgumentException e) {
            Log.w("OpBitmojiClock", "bitmojiManager error", e);
        }
        if (DEBUG_TEST) {
            Paint paint = new Paint();
            this.mPaint = paint;
            paint.setColor(-16711936);
            this.mPaint.setTextSize(60.0f);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        OpBitmojiManager opBitmojiManager = this.mBitmojiManager;
        if (opBitmojiManager != null) {
            opBitmojiManager.addListener(this);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        OpBitmojiManager opBitmojiManager = this.mBitmojiManager;
        if (opBitmojiManager != null) {
            opBitmojiManager.removeListener(this);
        }
    }

    @Override // android.view.View, android.view.ViewGroup
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (DEBUG_TEST && !TextUtils.isEmpty(this.mCurrentPackId)) {
            canvas.drawText(this.mCurrentPackId, 0.0f, 100.0f, this.mPaint);
        }
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void applyLayoutParams(OpAodSettings.OpViewInfo opViewInfo) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        if (!(layoutParams == null || opViewInfo == null)) {
            layoutParams.setMarginStart(opViewInfo.getMarginStart(((FrameLayout) this).mContext));
            layoutParams.setMarginEnd(opViewInfo.getMarginEnd(((FrameLayout) this).mContext));
            layoutParams.topMargin = opViewInfo.getMarginTop(((FrameLayout) this).mContext);
            layoutParams.bottomMargin = opViewInfo.getMarginBottom(((FrameLayout) this).mContext);
        }
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mBitmojiIcon.getLayoutParams();
        if (layoutParams2 != null) {
            layoutParams2.width = OpAodDimenHelper.convertDpToFixedPx(((FrameLayout) this).mContext, this.mBitmojiIconWidthId);
            layoutParams2.height = OpAodDimenHelper.convertDpToFixedPx(((FrameLayout) this).mContext, this.mBitmojiIconHeightId);
        }
        LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mDateView.getLayoutParams();
        if (layoutParams3 != null) {
            layoutParams3.topMargin = OpAodDimenHelper.convertDpToFixedPx(((FrameLayout) this).mContext, this.mDateMarginTopId);
        }
        updateTextSize(this.mDateView, C0016R$style.op_text_style_h6);
        updateTextSize(this.mTimeView, C0016R$style.op_text_style_h2);
    }

    @Override // com.oneplus.aod.views.IOpAodClock
    public void onTimeChanged(Calendar calendar) {
        String str;
        SimpleDateFormat simpleDateFormat;
        Locale locale = Locale.getDefault();
        String locale2 = locale.toString();
        if (locale2.startsWith("zh_") || locale2.startsWith("ko_") || locale2.startsWith("ja_")) {
            str = DateFormat.getBestDateTimePattern(locale, "MMMMd EEEE");
        } else {
            str = DateFormat.getBestDateTimePattern(locale, "EEEE, MMMM d");
        }
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(str.toString(), locale);
        Date time = calendar.getTime();
        this.mDateView.setText(simpleDateFormat2.format(time).toString());
        if (is24HourModeEnabled()) {
            simpleDateFormat = new SimpleDateFormat("HH'꞉'mm");
        } else {
            simpleDateFormat = new SimpleDateFormat("h'꞉'mm");
        }
        this.mTimeView.setText(simpleDateFormat.format(time).toString());
        OpBitmojiManager opBitmojiManager = this.mBitmojiManager;
        if (opBitmojiManager != null) {
            updateData(opBitmojiManager.getAodImage());
        }
    }

    @Override // com.oneplus.aod.utils.bitmoji.OpBitmojiManager.OnTriggerChangedListener
    public void onTriggerChanged() {
        OpBitmojiManager opBitmojiManager = this.mBitmojiManager;
        if (opBitmojiManager != null) {
            updateData(opBitmojiManager.getAodImage());
            if (DEBUG_TEST) {
                invalidate();
            }
        }
    }

    private void updateData(Object[] objArr) {
        if (objArr != null && objArr.length >= 2) {
            this.mCurrentPackId = (String) objArr[0];
            this.mBitmojiIcon.setImageDrawable((Drawable) objArr[1]);
        }
    }

    private void setupAttributes(AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = ((FrameLayout) this).mContext.obtainStyledAttributes(attributeSet, R$styleable.OpBitmojiClock, 0, 0);
        this.mBitmojiIconWidthId = obtainStyledAttributes.getResourceId(R$styleable.OpBitmojiClock_bitmojiIconWidth, -1);
        this.mBitmojiIconHeightId = obtainStyledAttributes.getResourceId(R$styleable.OpBitmojiClock_bitmojiIconHeight, -1);
        this.mDateMarginTopId = obtainStyledAttributes.getResourceId(R$styleable.OpBitmojiClock_dateMarginTop, -1);
        obtainStyledAttributes.recycle();
    }

    private void updateTextSize(TextView textView, int i) {
        TypedArray obtainStyledAttributes = ((FrameLayout) this).mContext.obtainStyledAttributes(i, new int[]{16842901});
        textView.setTextSize(0, (float) OpUtils.convertDpToFixedPx2(obtainStyledAttributes.getDimension(0, 0.0f)));
        obtainStyledAttributes.recycle();
    }

    private boolean is24HourModeEnabled() {
        return DateFormat.is24HourFormat(((FrameLayout) this).mContext, ActivityManager.getCurrentUser());
    }
}
