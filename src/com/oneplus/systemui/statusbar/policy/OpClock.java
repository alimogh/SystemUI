package com.oneplus.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.android.systemui.statusbar.policy.Clock;
import com.oneplus.scene.OpSceneModeObserver;
import com.oneplus.systemui.statusbar.phone.OpHighlightHintController;
import com.oneplus.util.OpReflectionUtils;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
public class OpClock extends TextView implements OpHighlightHintController.OnHighlightHintStateChangeListener {
    private static final String TAG = Clock.class.getSimpleName();
    protected boolean mAlwaysVisible;
    protected OpSceneModeObserver mOpSceneModeObserver;

    public OpClock(Context context) {
        this(context, null);
    }

    public OpClock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    /* JADX INFO: finally extract failed */
    public OpClock(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.Clock, 0, 0);
        try {
            this.mAlwaysVisible = obtainStyledAttributes.getBoolean(R$styleable.Clock_alwaysVisible, false);
            obtainStyledAttributes.recycle();
            if (OpUtils.needLargeQSClock(getContext())) {
                this.mAlwaysVisible = false;
            }
            this.mOpSceneModeObserver = (OpSceneModeObserver) Dependency.get(OpSceneModeObserver.class);
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).removeCallback(this);
    }

    /* access modifiers changed from: protected */
    public boolean opShowSeconds() {
        if (!this.mAlwaysVisible && getShowSeconds() && !((OpHighlightHintController) Dependency.get(OpHighlightHintController.class)).isHighLightHintShow()) {
            return true;
        }
        return false;
    }

    @Override // com.oneplus.systemui.statusbar.phone.OpHighlightHintController.OnHighlightHintStateChangeListener
    public void onHighlightHintStateChange() {
        Log.i(TAG, "onHighlightHintStateChange");
        updateShowSeconds();
    }

    /* access modifiers changed from: protected */
    public void setTextWithOpStyle(CharSequence charSequence) {
        StringBuilder sb = new StringBuilder(charSequence);
        int i = 0;
        while (true) {
            if (i >= charSequence.length()) {
                break;
            } else if (sb.charAt(i) == ':') {
                sb = sb.replace(i, i + 1, "᛬");
                break;
            } else {
                i++;
            }
        }
        if (OpUtils.isREDVersion()) {
            setTextColor(ThemeColorUtils.getColor(100));
            setText(sb);
            return;
        }
        SpannableString spannableString = new SpannableString(sb);
        for (int i2 = 0; i2 < 2; i2++) {
            if (sb.charAt(i2) == '1') {
                spannableString.setSpan(new ForegroundColorSpan(ThemeColorUtils.getColor(17)), i2, i2 + 1, 0);
            }
        }
        setTextColor(ThemeColorUtils.getColor(1));
        setLayoutDirection(0);
        setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    private void updateShowSeconds() {
        OpReflectionUtils.methodInvokeVoid(Clock.class, this, "updateShowSeconds", new Object[0]);
    }

    private boolean getShowSeconds() {
        return ((Boolean) OpReflectionUtils.getValue(Clock.class, this, "mShowSeconds")).booleanValue();
    }
}
