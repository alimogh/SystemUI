package com.oneplus.keyguard;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
import java.util.Locale;
public class OpEmergencyPanel extends FrameLayout {
    private OpEmergencyBubble mBubble;
    private PanelCallback mCallback;
    private Context mContext;
    private LinearLayout mEmergencyIndicatorLayout;
    private TextView mHintText;
    private Locale mLocale;
    private OpDragPanelController mPanelController;

    public static class PanelCallback {
        public abstract void onBubbleTouched();

        public abstract void onDrop();

        public void onTimeout() {
        }
    }

    public OpEmergencyPanel(Context context) {
        this(context, null);
    }

    public OpEmergencyPanel(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OpEmergencyPanel(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mLocale = null;
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mEmergencyIndicatorLayout = (LinearLayout) findViewById(C0008R$id.indator_layout);
        this.mBubble = (OpEmergencyBubble) findViewById(C0008R$id.bubble);
        this.mHintText = (TextView) this.mEmergencyIndicatorLayout.findViewById(C0008R$id.hint);
        this.mPanelController = new OpDragPanelController(this.mContext, this, this.mBubble, this.mEmergencyIndicatorLayout);
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return super.dispatchTouchEvent(motionEvent);
    }

    public void onStart() {
        OpDragPanelController opDragPanelController = this.mPanelController;
        if (opDragPanelController != null) {
            opDragPanelController.onStart();
        }
    }

    public void onStop() {
        OpDragPanelController opDragPanelController = this.mPanelController;
        if (opDragPanelController != null) {
            opDragPanelController.onStop();
        }
    }

    public void onTimeout() {
        PanelCallback panelCallback = this.mCallback;
        if (panelCallback != null) {
            panelCallback.onTimeout();
        }
    }

    public void onBubbleTouched() {
        PanelCallback panelCallback = this.mCallback;
        if (panelCallback != null) {
            panelCallback.onBubbleTouched();
        }
    }

    public void addCallback(PanelCallback panelCallback) {
        this.mCallback = panelCallback;
    }

    public void removeCallback() {
        this.mCallback = null;
    }

    public void onDrop() {
        PanelCallback panelCallback = this.mCallback;
        if (panelCallback != null) {
            panelCallback.onDrop();
            if (Build.DEBUG_ONEPLUS) {
                Log.i("OpEmergencyPanel", "onDrop");
            }
        }
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        refreshLocale();
    }

    private void refreshLocale() {
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        if (locale != null && !locale.equals(this.mLocale)) {
            this.mLocale = locale;
            if (this.mHintText != null) {
                this.mHintText.setText(this.mContext.getResources().getString(C0015R$string.op_emergency_indicator_hint_text));
            }
        }
    }
}
