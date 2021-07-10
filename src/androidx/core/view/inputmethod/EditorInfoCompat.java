package androidx.core.view.inputmethod;

import android.os.Build;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
public final class EditorInfoCompat {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static String[] getContentMimeTypes(EditorInfo editorInfo) {
        String[] strArr = EMPTY_STRING_ARRAY;
        if (Build.VERSION.SDK_INT >= 25) {
            String[] strArr2 = editorInfo.contentMimeTypes;
            return strArr2 != null ? strArr2 : strArr;
        }
        Bundle bundle = editorInfo.extras;
        if (bundle == null) {
            return strArr;
        }
        String[] stringArray = bundle.getStringArray("androidx.core.view.inputmethod.EditorInfoCompat.CONTENT_MIME_TYPES");
        if (stringArray == null) {
            stringArray = editorInfo.extras.getStringArray("android.support.v13.view.inputmethod.EditorInfoCompat.CONTENT_MIME_TYPES");
        }
        return stringArray != null ? stringArray : strArr;
    }
}
