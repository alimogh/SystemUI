package com.oneplus.airplane;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import com.android.systemui.C0015R$string;
public class AirplanePopupActivity extends Activity {
    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getDialog();
    }

    private void getDialog() {
        new AlertDialog.Builder(this).setTitle(getString(C0015R$string.status_bar_airplane)).setMessage(getString(C0015R$string.aiplane_warning_msg)).setIconAttribute(16843605).setPositiveButton(C0015R$string.ok, new DialogInterface.OnClickListener() { // from class: com.oneplus.airplane.AirplanePopupActivity.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                AirplanePopupActivity.this.finish();
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.oneplus.airplane.AirplanePopupActivity.1
            @Override // android.content.DialogInterface.OnCancelListener
            public void onCancel(DialogInterface dialogInterface) {
                AirplanePopupActivity.this.finish();
            }
        }).show();
    }
}
