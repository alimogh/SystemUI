package com.android.systemui.usb;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.android.systemui.C0008R$id;
import com.android.systemui.C0011R$layout;
import com.android.systemui.C0015R$string;
public class UsbContaminantActivity extends Activity implements View.OnClickListener {
    private TextView mEnableUsb;
    private TextView mGotIt;
    private TextView mLearnMore;
    private TextView mMessage;
    private TextView mTitle;
    private UsbPort mUsbPort;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        Window window = getWindow();
        window.addSystemFlags(524288);
        window.setType(2008);
        requestWindowFeature(1);
        super.onCreate(bundle);
        setContentView(C0011R$layout.contaminant_dialog);
        this.mUsbPort = getIntent().getParcelableExtra("port").getUsbPort((UsbManager) getSystemService(UsbManager.class));
        this.mLearnMore = (TextView) findViewById(C0008R$id.learnMore);
        this.mEnableUsb = (TextView) findViewById(C0008R$id.enableUsb);
        this.mGotIt = (TextView) findViewById(C0008R$id.gotIt);
        this.mTitle = (TextView) findViewById(C0008R$id.title);
        this.mMessage = (TextView) findViewById(C0008R$id.message);
        this.mTitle.setText(getString(C0015R$string.usb_contaminant_title));
        this.mMessage.setText(getString(C0015R$string.usb_contaminant_message));
        this.mEnableUsb.setText(getString(C0015R$string.usb_disable_contaminant_detection));
        this.mGotIt.setText(getString(C0015R$string.got_it));
        this.mLearnMore.setText(getString(C0015R$string.learn_more));
        this.mEnableUsb.setOnClickListener(this);
        this.mGotIt.setOnClickListener(this);
        this.mLearnMore.setOnClickListener(this);
        this.mEnableUsb.setVisibility(8);
        this.mLearnMore.setVisibility(8);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowAttributesChanged(WindowManager.LayoutParams layoutParams) {
        super.onWindowAttributesChanged(layoutParams);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view == this.mEnableUsb) {
            try {
                this.mUsbPort.enableContaminantDetection(false);
                Toast.makeText(this, C0015R$string.usb_port_enabled, 0).show();
            } catch (Exception e) {
                Log.e("UsbContaminantActivity", "Unable to notify Usb service", e);
            }
        } else if (view == this.mLearnMore) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.HelpTrampoline");
            intent.putExtra("android.intent.extra.TEXT", "help_url_usb_contaminant_detected");
            startActivity(intent);
        }
        finish();
    }
}
