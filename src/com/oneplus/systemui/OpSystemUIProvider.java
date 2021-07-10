package com.oneplus.systemui;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.oneplus.aod.utils.bitmoji.MdmLogger;
import com.oneplus.aod.utils.bitmoji.OpBitmojiHelper;
import java.io.File;
import java.io.FileNotFoundException;
public class OpSystemUIProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher;

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return true;
    }

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        sUriMatcher = uriMatcher;
        uriMatcher.addURI("com.oneplus.systemui.ContentProvider", "bitmojiStatus", 1);
        sUriMatcher.addURI("com.oneplus.systemui.ContentProvider", "bitmojiAvatar", 2);
        sUriMatcher.addURI("com.oneplus.systemui.ContentProvider", "bitmojiPackageInstall", 3);
        sUriMatcher.addURI("com.oneplus.systemui.ContentProvider", "bitmojiApplyFirstTime", 4);
        sUriMatcher.addURI("com.oneplus.systemui.ContentProvider", "bitmojiDownloadStatus", 5);
        sUriMatcher.addURI("com.oneplus.systemui.ContentProvider", "bitmojiMDM", 6);
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        int userIdFromUri = ContentProvider.getUserIdFromUri(uri, UserHandle.getCallingUserId());
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpSystemUIProvider", "query: uri= " + uri + ", callingPackage= " + getCallingPackage() + ", userId= " + userIdFromUri);
        }
        checkReadPermission(uri);
        int match = sUriMatcher.match(uri);
        if (match != 1) {
            if (match != 4) {
                if (match != 5 || userIdFromUri != 0) {
                    return null;
                }
                MatrixCursor matrixCursor = new MatrixCursor(new String[]{"status"});
                matrixCursor.newRow().add("status", Integer.valueOf(OpBitmojiHelper.getInstance().getBitmojiDownloadStatus()));
                return matrixCursor;
            } else if (userIdFromUri != 0) {
                return null;
            } else {
                MatrixCursor matrixCursor2 = new MatrixCursor(new String[]{"result"});
                matrixCursor2.newRow().add("result", Integer.valueOf(OpBitmojiHelper.getInstance().isApplyFirstTime() ? 1 : 0));
                return matrixCursor2;
            }
        } else if (userIdFromUri != 0) {
            return null;
        } else {
            MatrixCursor matrixCursor3 = new MatrixCursor(new String[]{"status"});
            matrixCursor3.newRow().add("status", Integer.valueOf(OpBitmojiHelper.getInstance().getOpBitmojiStatus()));
            return matrixCursor3;
        }
    }

    @Override // android.content.ContentProvider
    public Bundle call(String str, String str2, Bundle bundle) {
        checkReadPermission(str);
        if (UserHandle.getCallingUserId() != 0) {
            return null;
        }
        char c = 65535;
        int hashCode = str.hashCode();
        if (hashCode != -1347489304) {
            if (hashCode == 1116850070 && str.equals("bitmojiDownload")) {
                c = 0;
            }
        } else if (str.equals("bitmojiMDM")) {
            c = 1;
        }
        if (c == 0) {
            OpBitmojiHelper.getInstance().startDownloading("force".equals(str2));
            return null;
        } else if (c != 1 || bundle == null) {
            return null;
        } else {
            String string = bundle.getString("mdmLabel");
            String string2 = bundle.getString("mdmValue");
            if (TextUtils.isEmpty(string)) {
                return null;
            }
            MdmLogger.getInstance(getContext()).trackFromSettings(string, string2);
            return null;
        }
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        if (match == 1) {
            return "vnd.android.cursor.item/status";
        }
        if (match != 2) {
            return null;
        }
        return "image/png";
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int userIdFromUri = ContentProvider.getUserIdFromUri(uri, UserHandle.getCallingUserId());
        checkWritePermission(uri);
        if (sUriMatcher.match(uri) != 3 || userIdFromUri != 0) {
            return 0;
        }
        OpBitmojiHelper.getInstance().updateStatusFromStore(contentValues);
        getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }

    @Override // android.content.ContentProvider
    public ParcelFileDescriptor openFile(Uri uri, String str) throws FileNotFoundException {
        int userIdFromUri = ContentProvider.getUserIdFromUri(uri, UserHandle.getCallingUserId());
        checkReadPermission(uri);
        if (sUriMatcher.match(uri) != 2 || userIdFromUri != 0) {
            return null;
        }
        File avatarFile = OpBitmojiHelper.getInstance().getAvatarFile();
        if (avatarFile.exists()) {
            return ParcelFileDescriptor.open(avatarFile, ParcelFileDescriptor.parseMode(str));
        }
        return null;
    }

    private void checkReadPermission(String str) {
        int callingAppId = UserHandle.getCallingAppId();
        if (callingAppId != 0 && callingAppId != 1000) {
            throw new SecurityException("Permission Denial: not support for " + getCallingPackage());
        }
    }

    private void checkWritePermission(Uri uri) {
        checkPermissionInner(uri);
    }

    private void checkReadPermission(Uri uri) {
        checkPermissionInner(uri);
    }

    private void checkPermissionInner(Uri uri) {
        int callingAppId = UserHandle.getCallingAppId();
        if (callingAppId != 0 && callingAppId != 1000) {
            throw new SecurityException("Permission Denial: not support for " + getCallingPackage());
        }
    }

    public static void notifyAvatarUpdate(Context context) {
        context.getContentResolver().notifyChange(getBitmojiAvatarUri(), null);
    }

    public static void notifyDownloadStatusUpdate(Context context) {
        context.getContentResolver().notifyChange(getDownloadStatusUri(), null);
    }

    private static Uri getBitmojiAvatarUri() {
        return new Uri.Builder().scheme("content").authority("com.oneplus.systemui.ContentProvider").appendPath("bitmojiAvatar").build();
    }

    private static Uri getDownloadStatusUri() {
        return new Uri.Builder().scheme("content").authority("com.oneplus.systemui.ContentProvider").appendPath("bitmojiDownloadStatus").build();
    }
}
