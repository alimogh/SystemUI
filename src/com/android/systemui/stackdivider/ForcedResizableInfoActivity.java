package com.android.systemui.stackdivider;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.C0000R$anim;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
public class ForcedResizableInfoActivity extends Activity implements View.OnTouchListener {
    private final Runnable mFinishRunnable = new Runnable() { // from class: com.android.systemui.stackdivider.ForcedResizableInfoActivity.1
        @Override // java.lang.Runnable
        public void run() {
            ForcedResizableInfoActivity.this.finish();
        }
    };

    @Override // android.app.Activity
    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        String str;
        super.onCreate(bundle);
        setContentView(C0011R$layout.forced_resizable_activity);
        TextView textView = (TextView) findViewById(16908299);
        int intExtra = getIntent().getIntExtra("extra_forced_resizeable_reason", -1);
        if (intExtra == 1) {
            str = getString(C0015R$string.dock_forced_resizable);
        } else if (intExtra == 2) {
            str = getString(C0015R$string.forced_resizable_secondary_display);
        } else {
            throw new IllegalArgumentException("Unexpected forced resizeable reason: " + intExtra);
        }
        textView.setText(str);
        getWindow().setTitle(str);
        getWindow().getDecorView().setOnTouchListener(this);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onStart() {
        super.onStart();
        getWindow().getDecorView().postDelayed(this.mFinishRunnable, 2500);
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onStop() {
        super.onStop();
        finish();
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        finish();
        return true;
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        finish();
        return true;
    }

    @Override // android.app.Activity
    public void finish() {
        super.finish();
        overridePendingTransition(0, C0000R$anim.forced_resizable_exit);
    }
}
