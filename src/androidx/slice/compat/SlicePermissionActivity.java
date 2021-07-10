package androidx.slice.compat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.BidiFormatter;
import androidx.slice.core.R$id;
import androidx.slice.core.R$layout;
import androidx.slice.core.R$string;
public class SlicePermissionActivity extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
    private String mCallingPkg;
    private AlertDialog mDialog;
    private String mProviderPkg;
    private Uri mUri;

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mUri = (Uri) getIntent().getParcelableExtra("slice_uri");
        this.mCallingPkg = getIntent().getStringExtra("pkg");
        this.mProviderPkg = getIntent().getStringExtra("provider_pkg");
        try {
            PackageManager packageManager = getPackageManager();
            String unicodeWrap = BidiFormatter.getInstance().unicodeWrap(loadSafeLabel(packageManager, packageManager.getApplicationInfo(this.mCallingPkg, 0)).toString());
            String unicodeWrap2 = BidiFormatter.getInstance().unicodeWrap(loadSafeLabel(packageManager, packageManager.getApplicationInfo(this.mProviderPkg, 0)).toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R$string.abc_slice_permission_title, new Object[]{unicodeWrap, unicodeWrap2}));
            builder.setView(R$layout.abc_slice_permission_request);
            builder.setNegativeButton(R$string.abc_slice_permission_deny, this);
            builder.setPositiveButton(R$string.abc_slice_permission_allow, this);
            builder.setOnDismissListener(this);
            AlertDialog show = builder.show();
            this.mDialog = show;
            ((TextView) show.getWindow().getDecorView().findViewById(R$id.text1)).setText(getString(R$string.abc_slice_permission_text_1, new Object[]{unicodeWrap2}));
            ((TextView) this.mDialog.getWindow().getDecorView().findViewById(R$id.text2)).setText(getString(R$string.abc_slice_permission_text_2, new Object[]{unicodeWrap2}));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SlicePermissionActivity", "Couldn't find package", e);
            finish();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0058, code lost:
        r5 = r5.substring(0, r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.CharSequence loadSafeLabel(android.content.pm.PackageManager r6, android.content.pm.ApplicationInfo r7) {
        /*
            r5 = this;
            java.lang.CharSequence r5 = r7.loadLabel(r6)
            java.lang.String r5 = r5.toString()
            android.text.Spanned r5 = android.text.Html.fromHtml(r5)
            java.lang.String r5 = r5.toString()
            int r6 = r5.length()
            r0 = 0
            r1 = r0
        L_0x0016:
            if (r1 >= r6) goto L_0x005c
            int r2 = r5.codePointAt(r1)
            int r3 = java.lang.Character.getType(r2)
            r4 = 13
            if (r3 == r4) goto L_0x0058
            r4 = 15
            if (r3 == r4) goto L_0x0058
            r4 = 14
            if (r3 != r4) goto L_0x002d
            goto L_0x0058
        L_0x002d:
            r4 = 12
            if (r3 != r4) goto L_0x0052
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = r5.substring(r0, r1)
            r3.append(r4)
            java.lang.String r4 = " "
            r3.append(r4)
            int r4 = java.lang.Character.charCount(r2)
            int r4 = r4 + r1
            java.lang.String r5 = r5.substring(r4)
            r3.append(r5)
            java.lang.String r5 = r3.toString()
        L_0x0052:
            int r2 = java.lang.Character.charCount(r2)
            int r1 = r1 + r2
            goto L_0x0016
        L_0x0058:
            java.lang.String r5 = r5.substring(r0, r1)
        L_0x005c:
            java.lang.String r5 = r5.trim()
            boolean r6 = r5.isEmpty()
            if (r6 == 0) goto L_0x0069
            java.lang.String r5 = r7.packageName
            return r5
        L_0x0069:
            android.text.TextPaint r6 = new android.text.TextPaint
            r6.<init>()
            r7 = 1109917696(0x42280000, float:42.0)
            r6.setTextSize(r7)
            r7 = 1140457472(0x43fa0000, float:500.0)
            android.text.TextUtils$TruncateAt r0 = android.text.TextUtils.TruncateAt.END
            java.lang.CharSequence r5 = android.text.TextUtils.ellipsize(r5, r6, r7, r0)
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slice.compat.SlicePermissionActivity.loadSafeLabel(android.content.pm.PackageManager, android.content.pm.ApplicationInfo):java.lang.CharSequence");
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -1) {
            SliceProviderCompat.grantSlicePermission(this, getPackageName(), this.mCallingPkg, this.mUri.buildUpon().path("").build());
        }
        finish();
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        finish();
    }

    @Override // android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            this.mDialog.cancel();
        }
    }
}
