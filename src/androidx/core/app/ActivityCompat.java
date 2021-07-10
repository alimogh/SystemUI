package androidx.core.app;

import android.app.Activity;
import android.os.Build;
import androidx.core.content.ContextCompat;
public class ActivityCompat extends ContextCompat {
    public static void finishAffinity(Activity activity) {
        if (Build.VERSION.SDK_INT >= 16) {
            activity.finishAffinity();
        } else {
            activity.finish();
        }
    }

    public static void recreate(Activity activity) {
        if (Build.VERSION.SDK_INT >= 28) {
            activity.recreate();
        } else if (!ActivityRecreator.recreate(activity)) {
            activity.recreate();
        }
    }
}
