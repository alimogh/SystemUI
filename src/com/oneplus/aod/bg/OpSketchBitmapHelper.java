package com.oneplus.aod.bg;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class OpSketchBitmapHelper {
    static int SPREAD_STROKE_WIDTH = 3;

    public OpSketchBitmapHelper(int i) {
        SPREAD_STROKE_WIDTH = SystemProperties.getInt("sys.c.aod.width", SPREAD_STROKE_WIDTH);
        String str = SystemProperties.get("sys.c.aod.length", "");
        String str2 = SystemProperties.get("sys.c.aod.range", "");
        String str3 = SystemProperties.get("sys.c.aod.filter", "");
        String[] split = str.split(",");
        String[] split2 = str2.split(",");
        String[] split3 = str3.split(",");
        if (split.length > 0 && split.length == split2.length && split.length == split3.length) {
            Log.d("OpSketchBitmapHelper", "override value");
            try {
                convertToIntArray(split);
                convertToIntArray(split2);
                convertToIntArray(split3);
            } catch (Exception unused) {
                Log.w("OpSketchBitmapHelper", "exception happaned");
            }
        }
    }

    public static int[] convertToIntArray(String[] strArr) {
        int[] iArr = new int[strArr.length];
        for (int i = 0; i < strArr.length; i++) {
            iArr[i] = Integer.parseInt(strArr[i]);
        }
        return iArr;
    }

    public Bitmap genBurninMask(Point point, int i) {
        Bitmap bitmap;
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (!Thread.currentThread().isInterrupted()) {
            int i2 = i > 0 ? i : 1;
            bitmap = Bitmap.createBitmap(i2, i2, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(-16777216);
            bitmap.setPixel(0, 0, 0);
        } else {
            bitmap = null;
        }
        if (Build.DEBUG_ONEPLUS) {
            Log.d("OpSketchBitmapHelper", "genBurninMask: total cost= " + (SystemClock.elapsedRealtime() - elapsedRealtime) + " ms, size= " + point + ", diameter = " + i);
        }
        return bitmap;
    }

    public static class SketchPoint extends Point {
        private int mDirection = 0;

        private SketchPoint(JSONObject jSONObject) throws JSONException {
            ((Point) this).x = jSONObject.getInt("x");
            ((Point) this).y = jSONObject.getInt("y");
            this.mDirection = jSONObject.getInt("direction");
        }

        public static ArrayList<SketchPoint> parseArray(JSONArray jSONArray) {
            ArrayList<SketchPoint> arrayList = new ArrayList<>();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject optJSONObject = jSONArray.optJSONObject(i);
                if (optJSONObject != null) {
                    try {
                        arrayList.add(new SketchPoint(optJSONObject));
                    } catch (JSONException e) {
                        Log.e("SketchPoint", "parse json object failed. " + i, e);
                    }
                } else {
                    Log.e("SketchPoint", "get json object failed. " + i);
                }
            }
            return arrayList;
        }

        public int getDirection() {
            return this.mDirection;
        }
    }

    public static class SpreadPoint extends Point {
        PointF mSpreadDirection;

        @Override // android.graphics.Point, java.lang.Object
        public String toString() {
            return "[SpreadPoint]: x= " + ((Point) this).x + ", y= " + ((Point) this).y + ", direction= " + this.mSpreadDirection;
        }
    }
}
