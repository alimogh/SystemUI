package com.oneplus.aod.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.shared.system.OpContextWrapper;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.oneplus.aod.OpAodUtils;
import com.oneplus.aod.bg.OpSketchBitmapHelper;
import com.oneplus.plugin.OpLsState;
import com.oneplus.util.OpUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
public class OpCanvasAodHelper {
    private Context mContext;

    public interface OnBitmapHandleDoneListener {
        void onBitmapHandleDone(boolean z, boolean z2, Data data);
    }

    public OpCanvasAodHelper(Context context) {
        this.mContext = context;
    }

    public static boolean isCanvasAodAnimation(Context context, boolean z) {
        boolean z2;
        boolean z3;
        boolean z4;
        boolean isCanvasAodEnabled = isCanvasAodEnabled(context);
        StatusBar phoneStatusBar = OpLsState.getInstance().getPhoneStatusBar();
        if (phoneStatusBar != null) {
            z3 = phoneStatusBar.getAodDisplayViewManager().getAodDisplayViewState() == 1;
            z2 = phoneStatusBar.isOccluded();
            z4 = phoneStatusBar.isKeyguardShowing();
        } else {
            z4 = true;
            z3 = false;
            z2 = false;
        }
        boolean isHomeApp = OpUtils.isHomeApp();
        boolean hasMediaArtwork = ((NotificationMediaManager) Dependency.get(NotificationMediaManager.class)).hasMediaArtwork();
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        String wakingUpReason = keyguardUpdateMonitor.getWakingUpReason();
        boolean hasLockscreenWallpaper = keyguardUpdateMonitor.hasLockscreenWallpaper();
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpCanvasAodHelper", "isCanvasAodAnimation: isCanvasAodEnabled= " + isCanvasAodEnabled + ", isAodMainState= " + z3 + ", isHomeApp= " + isHomeApp + ", isOccluded= " + z2 + ", isKeyguardShowing= " + z4 + ", isMediaWallpaper= " + hasMediaArtwork + ", hasLockScreenWallpaper= " + hasLockscreenWallpaper + ", wakingUpReason= " + wakingUpReason);
        }
        if (isCanvasAodEnabled && z3 && !z2) {
            if (isHomeApp && (z || !z4)) {
                return true;
            }
            if (!z && z4 && !hasLockscreenWallpaper && !hasMediaArtwork && !"com.android.systemui:FailedAttempts".equals(wakingUpReason)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCanvasAodEnabled(Context context) {
        return context.getSharedPreferences(getCanvasAodPrefs(), 0).getBoolean("canvasAODEnabled", false);
    }

    public static String getCanvasAodUri(Context context) {
        return context.getSharedPreferences(getCanvasAodPrefs(), 0).getString("canvasAODUri", null);
    }

    private void recordCurrentClockStyle() {
        int currentAodClockStyle = OpAodUtils.getCurrentAodClockStyle(this.mContext, KeyguardUpdateMonitor.getCurrentUser());
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpCanvasAodHelper", "recordCurrentClockStyle " + currentAodClockStyle);
        }
        this.mContext.getSharedPreferences(getCanvasAodPrefs(), 0).edit().putInt("prev_clock_style", currentAodClockStyle).commit();
    }

    public void resetPrevClockStyle() {
        this.mContext.getSharedPreferences(getCanvasAodPrefs(), 0).edit().putInt("prev_clock_style", -1).commit();
    }

    private int getPrevClockStyle() {
        return this.mContext.getSharedPreferences(getCanvasAodPrefs(), 0).getInt("prev_clock_style", -1);
    }

    public void saveCanvasAodParams(Bundle bundle, final boolean z, final OnBitmapHandleDoneListener onBitmapHandleDoneListener, final Handler handler) {
        final String str;
        final String str2;
        boolean z2;
        boolean z3 = true;
        boolean z4 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "canvas_aod_enabled", 0, KeyguardUpdateMonitor.getCurrentUser()) == 1;
        if (bundle == null) {
            z2 = false;
            str2 = null;
            str = null;
        } else {
            boolean z5 = bundle.getBoolean("canvasAODEnabled", isCanvasAodEnabled(this.mContext));
            str = bundle.getString("canvasAODUri", null);
            str2 = bundle.getString("canvasAODJSONObject", null);
            z2 = z5;
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpCanvasAodHelper", "saveCanvasAodParams: enabled= " + z2 + ", systemUIEnabled= " + z4 + ", contentChange= " + z + ", imageUri= " + Boolean.valueOf(!TextUtils.isEmpty(str)) + ", pointUri= " + Boolean.valueOf(!TextUtils.isEmpty(str2)) + ", userId= " + KeyguardUpdateMonitor.getCurrentUser());
        }
        if (z) {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            int i = z2 ? 1 : 0;
            int i2 = z2 ? 1 : 0;
            int i3 = z2 ? 1 : 0;
            int i4 = z2 ? 1 : 0;
            Settings.Secure.putIntForUser(contentResolver, "canvas_aod_enabled", i, currentUser);
            if (!z2) {
                int prevClockStyle = getPrevClockStyle();
                if (!(prevClockStyle == -1 || prevClockStyle == 0)) {
                    Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "aod_clock_style", prevClockStyle, KeyguardUpdateMonitor.getCurrentUser());
                    if (Build.DEBUG_ONEPLUS) {
                        Log.d("OpCanvasAodHelper", "disable canvas aod change clock style to " + prevClockStyle);
                    }
                }
                resetPrevClockStyle();
            } else if (!z4) {
                recordCurrentClockStyle();
            }
            Log.d("OpCanvasAodHelper", "systemui enable canvas aod ? " + z2);
        } else {
            if (!z2 || !z4) {
                z3 = false;
            }
            z2 = z3;
        }
        this.mContext.getSharedPreferences(getCanvasAodPrefs(), 0).edit().putBoolean("canvasAODEnabled", z2).putString("canvasAODUri", str).putString("canvasAODJSONObject", str2).commit();
        if (!z2) {
            deleteCacheFile();
            if (onBitmapHandleDoneListener != null) {
                onBitmapHandleDoneListener.onBitmapHandleDone(false, z, null);
                return;
            }
            return;
        }
        new Thread() { // from class: com.oneplus.aod.utils.OpCanvasAodHelper.1
            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                final Data data;
                try {
                    data = Data.parse(OpCanvasAodHelper.this.mContext, str, str2);
                } catch (Exception e) {
                    Log.e("OpCanvasAodHelper", "Data parse error", e);
                    data = null;
                }
                Handler handler2 = handler;
                if (handler2 != null) {
                    handler2.post(new Runnable() { // from class: com.oneplus.aod.utils.OpCanvasAodHelper.1.1
                        @Override // java.lang.Runnable
                        public void run() {
                            OnBitmapHandleDoneListener onBitmapHandleDoneListener2 = onBitmapHandleDoneListener;
                            if (onBitmapHandleDoneListener2 != null) {
                                onBitmapHandleDoneListener2.onBitmapHandleDone(data != null, z, data);
                            }
                        }
                    });
                }
            }
        }.start();
    }

    public void loadFromCache(final OnBitmapHandleDoneListener onBitmapHandleDoneListener, final Handler handler) {
        boolean isCanvasAodEnabled = isCanvasAodEnabled(this.mContext);
        String canvasAodUri = getCanvasAodUri(this.mContext);
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpCanvasAodHelper", "loadFromCache: enabledCache= " + isCanvasAodEnabled + ", imageUriCache= " + Boolean.valueOf(!TextUtils.isEmpty(canvasAodUri)) + ", userId= " + KeyguardUpdateMonitor.getCurrentUser());
        }
        if (isCanvasAodEnabled) {
            new Thread() { // from class: com.oneplus.aod.utils.OpCanvasAodHelper.2
                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    final Data data;
                    try {
                        data = Data.loadFromCache(OpCanvasAodHelper.this.mContext);
                    } catch (Exception e) {
                        Log.e("OpCanvasAodHelper", "Data parse error", e);
                        data = null;
                    }
                    Handler handler2 = handler;
                    if (handler2 != null) {
                        handler2.post(new Runnable() { // from class: com.oneplus.aod.utils.OpCanvasAodHelper.2.1
                            @Override // java.lang.Runnable
                            public void run() {
                                OnBitmapHandleDoneListener onBitmapHandleDoneListener2 = onBitmapHandleDoneListener;
                                if (onBitmapHandleDoneListener2 != null) {
                                    onBitmapHandleDoneListener2.onBitmapHandleDone(data != null, false, data);
                                }
                            }
                        });
                    }
                }
            }.start();
        } else if (onBitmapHandleDoneListener != null) {
            onBitmapHandleDoneListener.onBitmapHandleDone(false, false, null);
        }
    }

    private void deleteCacheFile() {
        File dir = this.mContext.getDir("canvasAod", 0);
        File file = new File(dir, getCanvasCacheBitmap());
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception e) {
                Log.w("OpCanvasAodHelper", "deleteBitmapCacheFile failed", e);
            }
        }
        File file2 = new File(dir, getCanvasCacheDetail());
        if (file2.exists()) {
            try {
                file2.delete();
            } catch (Exception e2) {
                Log.w("OpCanvasAodHelper", "deleteDetailCacheFile failed", e2);
            }
        }
    }

    static String getCanvasAodPrefs() {
        return "canvas_aod-" + KeyguardUpdateMonitor.getCurrentUser();
    }

    static String getCanvasCacheBitmap() {
        return "canvas_aod-" + KeyguardUpdateMonitor.getCurrentUser();
    }

    static String getCanvasCacheDetail() {
        return "canvas_aod_detail-" + KeyguardUpdateMonitor.getCurrentUser();
    }

    public static class Data {
        Bitmap mBitmap;
        ArrayList<OpSketchBitmapHelper.SketchPoint> mList = new ArrayList<>();
        float mScale = 1.0f;

        private Data() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:28:0x0068, code lost:
            if (r3 != null) goto L_0x0054;
         */
        /* JADX WARNING: Removed duplicated region for block: B:33:0x0070 A[SYNTHETIC, Splitter:B:33:0x0070] */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x0094  */
        /* JADX WARNING: Removed duplicated region for block: B:57:0x009d A[SYNTHETIC, Splitter:B:57:0x009d] */
        /* JADX WARNING: Removed duplicated region for block: B:64:0x00ac  */
        /* JADX WARNING: Removed duplicated region for block: B:67:0x00bb  */
        /* JADX WARNING: Removed duplicated region for block: B:69:0x00bf  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        static com.oneplus.aod.utils.OpCanvasAodHelper.Data parse(android.content.Context r8, java.lang.String r9, java.lang.String r10) throws java.lang.Exception {
            /*
            // Method dump skipped, instructions count: 207
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.utils.OpCanvasAodHelper.Data.parse(android.content.Context, java.lang.String, java.lang.String):com.oneplus.aod.utils.OpCanvasAodHelper$Data");
        }

        static Data loadFromCache(Context context) throws Exception {
            Data data = new Data();
            Bitmap loadBitmapFromCache = loadBitmapFromCache(context);
            if (loadBitmapFromCache != null) {
                data.mBitmap = loadBitmapFromCache;
                String loadJsonFromCache = loadJsonFromCache(context);
                if (!TextUtils.isEmpty(loadJsonFromCache)) {
                    parseJsonObjet(data, loadJsonFromCache);
                    return data;
                }
                throw new RuntimeException("json string is empty");
            }
            throw new RuntimeException("loadFromCache bitmap is null");
        }

        private static String loadJsonFromCache(Context context) {
            File file = new File(context.getDir("canvasAod", 0), OpCanvasAodHelper.getCanvasCacheDetail());
            if (file.exists()) {
                try {
                    return getJsonFromFile(new FileReader(file));
                } catch (Exception e) {
                    Log.e("OpCanvasAodHelper:Data", "loadJsonFromCache occur error", e);
                }
            } else {
                Log.d("OpCanvasAodHelper:Data", "loadFromCache: json string cache is not exists");
                return null;
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:21:0x0053 A[SYNTHETIC, Splitter:B:21:0x0053] */
        /* JADX WARNING: Removed duplicated region for block: B:26:? A[RETURN, SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static void cacheCanvasAodBitmap(android.content.Context r3, android.graphics.Bitmap r4) {
            /*
                java.lang.String r0 = "OpCanvasAodHelper:Data"
                java.lang.String r1 = "canvasAod"
                r2 = 0
                java.io.File r3 = r3.getDir(r1, r2)
                java.io.File r1 = new java.io.File
                java.lang.String r2 = com.oneplus.aod.utils.OpCanvasAodHelper.getCanvasCacheBitmap()
                r1.<init>(r3, r2)
                r3 = 0
                java.io.FileOutputStream r2 = new java.io.FileOutputStream     // Catch:{ Exception -> 0x0045, all -> 0x0041 }
                r2.<init>(r1)     // Catch:{ Exception -> 0x0045, all -> 0x0041 }
                android.graphics.Bitmap$CompressFormat r3 = android.graphics.Bitmap.CompressFormat.PNG     // Catch:{ Exception -> 0x003f }
                r1 = 100
                r4.compress(r3, r1, r2)     // Catch:{ Exception -> 0x003f }
                boolean r3 = android.os.Build.DEBUG_ONEPLUS     // Catch:{ Exception -> 0x003f }
                if (r3 == 0) goto L_0x0039
                java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x003f }
                r3.<init>()     // Catch:{ Exception -> 0x003f }
                java.lang.String r4 = "cache bitmap success "
                r3.append(r4)     // Catch:{ Exception -> 0x003f }
                java.lang.String r4 = ""
                r3.append(r4)     // Catch:{ Exception -> 0x003f }
                java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x003f }
                android.util.Log.i(r0, r3)     // Catch:{ Exception -> 0x003f }
            L_0x0039:
                r2.close()     // Catch:{ Exception -> 0x0050 }
                goto L_0x0050
            L_0x003d:
                r3 = move-exception
                goto L_0x0051
            L_0x003f:
                r3 = move-exception
                goto L_0x0048
            L_0x0041:
                r4 = move-exception
                r2 = r3
                r3 = r4
                goto L_0x0051
            L_0x0045:
                r4 = move-exception
                r2 = r3
                r3 = r4
            L_0x0048:
                java.lang.String r4 = "cacheCanvasAodBitmap occur error"
                android.util.Log.e(r0, r4, r3)     // Catch:{ all -> 0x003d }
                if (r2 == 0) goto L_0x0050
                goto L_0x0039
            L_0x0050:
                return
            L_0x0051:
                if (r2 == 0) goto L_0x0056
                r2.close()     // Catch:{ Exception -> 0x0056 }
            L_0x0056:
                throw r3
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.utils.OpCanvasAodHelper.Data.cacheCanvasAodBitmap(android.content.Context, android.graphics.Bitmap):void");
        }

        public float getScale() {
            return this.mScale;
        }

        public Bitmap getImage() {
            return this.mBitmap;
        }

        public ArrayList<OpSketchBitmapHelper.SketchPoint> getList() {
            return this.mList;
        }

        private static Bitmap getBitmapFromUri(Context context, String str) {
            if (!TextUtils.isEmpty(str)) {
                Context currentUserContext = OpContextWrapper.getCurrentUserContext(context);
                Bitmap bitmap = null;
                if (currentUserContext != null) {
                    try {
                        bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(currentUserContext.getContentResolver(), Uri.parse(str)));
                        cacheCanvasAodBitmap(context, bitmap);
                        return bitmap;
                    } catch (IOException e) {
                        Log.e("OpCanvasAodHelper:Data", "getBitmapFromUri occur error", e);
                        return bitmap;
                    }
                } else {
                    Log.e("OpCanvasAodHelper:Data", "getBitmapFromUri: context is null!");
                    return null;
                }
            } else {
                Log.e("OpCanvasAodHelper:Data", "getBitmapFromUri uri is empty try to use cache");
                return loadBitmapFromCache(context);
            }
        }

        private static Bitmap loadBitmapFromCache(Context context) {
            return BitmapFactory.decodeFile(new File(context.getDir("canvasAod", 0), OpCanvasAodHelper.getCanvasCacheBitmap()).getPath());
        }

        /* JADX WARNING: Code restructure failed: missing block: B:18:0x002a, code lost:
            if (r1 != null) goto L_0x0019;
         */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0032 A[SYNTHETIC, Splitter:B:24:0x0032] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static java.lang.String getJsonFromFile(java.io.FileReader r4) {
            /*
                r0 = 0
                java.io.BufferedReader r1 = new java.io.BufferedReader     // Catch:{ Exception -> 0x0021, all -> 0x001f }
                r1.<init>(r4)     // Catch:{ Exception -> 0x0021, all -> 0x001f }
                java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x001d }
                r4.<init>()     // Catch:{ Exception -> 0x001d }
            L_0x000b:
                java.lang.String r2 = r1.readLine()     // Catch:{ Exception -> 0x001d }
                if (r2 == 0) goto L_0x0015
                r4.append(r2)     // Catch:{ Exception -> 0x001d }
                goto L_0x000b
            L_0x0015:
                java.lang.String r0 = r4.toString()     // Catch:{ Exception -> 0x001d }
            L_0x0019:
                r1.close()     // Catch:{ Exception -> 0x002d }
                goto L_0x002d
            L_0x001d:
                r4 = move-exception
                goto L_0x0023
            L_0x001f:
                r4 = move-exception
                goto L_0x0030
            L_0x0021:
                r4 = move-exception
                r1 = r0
            L_0x0023:
                java.lang.String r2 = "OpCanvasAodHelper:Data"
                java.lang.String r3 = "getJsonFromFile occur error"
                android.util.Log.e(r2, r3, r4)     // Catch:{ all -> 0x002e }
                if (r1 == 0) goto L_0x002d
                goto L_0x0019
            L_0x002d:
                return r0
            L_0x002e:
                r4 = move-exception
                r0 = r1
            L_0x0030:
                if (r0 == 0) goto L_0x0035
                r0.close()     // Catch:{ Exception -> 0x0035 }
            L_0x0035:
                throw r4
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.utils.OpCanvasAodHelper.Data.getJsonFromFile(java.io.FileReader):java.lang.String");
        }

        private static void parseJsonObjet(Data data, String str) {
            try {
                JSONObject jSONObject = new JSONObject(str);
                data.mScale = (float) jSONObject.optDouble("scale", 1.0d);
                JSONArray optJSONArray = jSONObject.optJSONArray("points");
                if (optJSONArray == null || optJSONArray.length() == 0) {
                    throw new RuntimeException("no points");
                }
                data.mList.addAll(OpSketchBitmapHelper.SketchPoint.parseArray(optJSONArray));
            } catch (Exception e) {
                Log.e("OpCanvasAodHelper:Data", "parse: parse json object occur error", e);
                throw new RuntimeException("parse json failed");
            }
        }
    }
}
