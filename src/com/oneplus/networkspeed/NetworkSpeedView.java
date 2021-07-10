package com.oneplus.networkspeed;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.C0008R$id;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.oneplus.networkspeed.NetworkSpeedController;
public class NetworkSpeedView extends LinearLayout implements NetworkSpeedController.INetworkSpeedStateCallBack {
    private Context mContext;
    private boolean mIsVisible;
    private String mLastTextDown;
    private String mLastTextUp;
    private NetworkSpeedController mNetworkSpeedController;
    private ScreenLifecycle mScreenLifecycle;
    private final ScreenLifecycle.Observer mScreenObserver;
    private String mTextDown;
    private String mTextUp;
    private TextView mTextViewDown;
    private TextView mTextViewUp;

    public NetworkSpeedView(Context context) {
        this(context, null);
    }

    public NetworkSpeedView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NetworkSpeedView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mTextUp = "";
        this.mTextDown = "";
        this.mLastTextUp = "";
        this.mLastTextDown = "";
        this.mIsVisible = false;
        this.mScreenObserver = new ScreenLifecycle.Observer() { // from class: com.oneplus.networkspeed.NetworkSpeedView.1
            @Override // com.android.systemui.keyguard.ScreenLifecycle.Observer
            public void onScreenTurnedOff() {
            }

            @Override // com.android.systemui.keyguard.ScreenLifecycle.Observer
            public void onScreenTurnedOn() {
                NetworkSpeedView.this.updateText();
            }
        };
        this.mNetworkSpeedController = (NetworkSpeedController) Dependency.get(NetworkSpeedController.class);
        this.mScreenLifecycle = (ScreenLifecycle) Dependency.get(ScreenLifecycle.class);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mTextViewUp = (TextView) findViewById(C0008R$id.speed_word_up);
        this.mTextViewDown = (TextView) findViewById(C0008R$id.speed_word_down);
        Log.i("NetworkSpeedView", "onFinishInflate");
        this.mContext.getResources().getConfiguration();
        refreshTextView();
    }

    @Override // com.oneplus.networkspeed.NetworkSpeedController.INetworkSpeedStateCallBack
    public void onSpeedChange(String str) {
        String[] split = str.split(":");
        if (split.length == 2) {
            this.mTextUp = split[0];
            this.mTextDown = split[1];
            updateText();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerReceiver();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterReceiver();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        if (Build.DEBUG_ONEPLUS) {
            Log.i("NetworkSpeedView", " onConfigurationChanged:" + configuration);
        }
    }

    public void registerReceiver() {
        this.mNetworkSpeedController.addCallback(this);
        this.mScreenLifecycle.addObserver(this.mScreenObserver);
    }

    public void unregisterReceiver() {
        this.mNetworkSpeedController.removeCallback(this);
        this.mScreenLifecycle.removeObserver(this.mScreenObserver);
    }

    @Override // android.view.View
    public void setVisibility(int i) {
        super.setVisibility(i);
        boolean z = i == 0;
        if (Build.DEBUG_ONEPLUS) {
            Log.i("NetworkSpeedView", " setVisibility:" + i);
        }
        if (this.mIsVisible != z) {
            this.mIsVisible = z;
            updateText();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateText() {
        TextView textView;
        boolean z = this.mScreenLifecycle.getScreenState() == 2;
        String str = this.mLastTextUp;
        if (!(str == null || this.mLastTextDown == null || ((str.equals(this.mTextUp) && this.mLastTextDown.equals(this.mTextDown)) || !Build.DEBUG_ONEPLUS))) {
            Log.i("NetworkSpeedView", " updateText:" + this.mTextUp + " " + this.mTextDown + " mIsVisible:" + this.mIsVisible + " mScreenOn:" + z);
        }
        if (this.mIsVisible && z && (textView = this.mTextViewUp) != null && this.mTextViewDown != null) {
            textView.setText(this.mTextUp);
            this.mTextViewDown.setText(this.mTextDown);
            this.mLastTextUp = this.mTextUp;
            this.mLastTextDown = this.mTextDown;
        }
    }

    public void setTextColor(int i) {
        TextView textView = this.mTextViewUp;
        if (textView != null && this.mTextViewDown != null) {
            textView.setTextColor(i);
            this.mTextViewDown.setTextColor(i);
        }
    }

    private void refreshTextView() {
        TextView textView = this.mTextViewUp;
        if (textView != null && this.mTextViewDown != null) {
            textView.setLetterSpacing(-0.05f);
            this.mTextViewDown.setLetterSpacing(0.05f);
        }
    }
}
