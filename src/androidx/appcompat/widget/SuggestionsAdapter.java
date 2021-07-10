package androidx.appcompat.widget;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import androidx.cursoradapter.widget.ResourceCursorAdapter;
@SuppressLint({"RestrictedAPI"})
class SuggestionsAdapter extends ResourceCursorAdapter implements View.OnClickListener {
    public static String getColumnString(Cursor cursor, String str) {
        return getStringOrNull(cursor, cursor.getColumnIndex(str));
    }

    private static String getStringOrNull(Cursor cursor, int i) {
        if (i == -1) {
            return null;
        }
        try {
            return cursor.getString(i);
        } catch (Exception e) {
            Log.e("SuggestionsAdapter", "unexpected error retrieving valid column from cursor, did the remote process die?", e);
            return null;
        }
    }
}
