package com.oneplus.aod.utils.bitmoji;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
public class OpBitmojiConnectedEntry extends Activity {
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (checkStatusAndFinish()) {
            finish();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1) {
            if (i2 == -1) {
                Log.d("OpBitmojiConnectedEntry", "connected!");
            }
            finish();
        }
    }

    private boolean checkStatusAndFinish() {
        int bitmojiStatus = OpBitmojiHelper.getInstance().getBitmojiStatus();
        if (bitmojiStatus == 3) {
            Log.d("OpBitmojiConnectedEntry", "not require access yet!");
            startActivityForResult(new Intent("android.intent.action.VIEW").setData(Uri.parse("imoji://content-provider/connected-apps")), 1);
            return false;
        }
        Log.d("OpBitmojiConnectedEntry", "not no_access. status= " + bitmojiStatus);
        return true;
    }
}
