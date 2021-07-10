package com.oneplus.aod;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.C0006R$drawable;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0015R$string;
public class OpAodThreeKeyStatusView extends LinearLayout {
    public static int MODE_NONE = 0;
    public static int MODE_SILENCE = 1;
    public static int MODE_VIBRATE = 2;
    private String TAG = "AodThreeKeyStatusView";
    private ImageView mIcon;
    private TextView mTextView;

    public OpAodThreeKeyStatusView(Context context) {
        super(context);
    }

    public OpAodThreeKeyStatusView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public OpAodThreeKeyStatusView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public OpAodThreeKeyStatusView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIcon = (ImageView) findViewById(C0008R$id.three_key_icon);
        this.mTextView = (TextView) findViewById(C0008R$id.three_key_text);
    }

    public void onThreeKeyChanged(int i) {
        String str = this.TAG;
        Log.d(str, "mode = " + i);
        if (i != MODE_NONE) {
            ImageView imageView = this.mIcon;
            if (imageView != null) {
                if (i == MODE_SILENCE) {
                    imageView.setImageResource(C0006R$drawable.aod_stat_sys_three_key_silent);
                } else if (i == MODE_VIBRATE) {
                    imageView.setImageResource(C0006R$drawable.aod_stat_sys_ringer_vibrate);
                } else {
                    imageView.setImageResource(C0006R$drawable.aod_stat_sys_three_key_normal);
                }
            }
            TextView textView = this.mTextView;
            if (textView == null) {
                return;
            }
            if (i == MODE_SILENCE) {
                textView.setText(C0015R$string.volume_footer_slient);
            } else if (i == MODE_VIBRATE) {
                textView.setText(C0015R$string.volume_vibrate);
            } else {
                textView.setText(C0015R$string.volume_footer_ring);
            }
        }
    }
}
