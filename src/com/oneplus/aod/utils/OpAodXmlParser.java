package com.oneplus.aod.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.SparseArray;
import com.oneplus.aod.utils.OpAodSettings;
public class OpAodXmlParser {
    private static final SparseArray<String> CONTROLLER_MAPPING_CACHE = new SparseArray<>();

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0050, code lost:
        if (r3 != null) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0060, code lost:
        if (r3 != null) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0062, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0065, code lost:
        return null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x006a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getControllerName(android.content.Context r3, int r4) {
        /*
            android.util.SparseArray<java.lang.String> r0 = com.oneplus.aod.utils.OpAodXmlParser.CONTROLLER_MAPPING_CACHE
            int r0 = r0.indexOfKey(r4)
            if (r0 < 0) goto L_0x0011
            android.util.SparseArray<java.lang.String> r3 = com.oneplus.aod.utils.OpAodXmlParser.CONTROLLER_MAPPING_CACHE
            java.lang.Object r3 = r3.get(r4)
            java.lang.String r3 = (java.lang.String) r3
            return r3
        L_0x0011:
            r0 = 0
            android.content.res.Resources r3 = r3.getResources()     // Catch:{ Exception -> 0x0057, all -> 0x0055 }
            android.content.res.XmlResourceParser r3 = r3.getXml(r4)     // Catch:{ Exception -> 0x0057, all -> 0x0055 }
            r3.next()     // Catch:{ Exception -> 0x0053 }
        L_0x001d:
            int r1 = r3.next()     // Catch:{ Exception -> 0x0053 }
            r2 = 3
            if (r1 == r2) goto L_0x0050
            int r1 = r3.getEventType()     // Catch:{ Exception -> 0x0053 }
            r2 = 2
            if (r1 == r2) goto L_0x002c
            goto L_0x001d
        L_0x002c:
            java.lang.String r1 = r3.getName()     // Catch:{ Exception -> 0x0053 }
            java.lang.String r2 = "controller"
            boolean r1 = r2.equals(r1)     // Catch:{ Exception -> 0x0053 }
            if (r1 == 0) goto L_0x001d
            r3.next()     // Catch:{ Exception -> 0x0053 }
            java.lang.String r1 = r3.getText()     // Catch:{ Exception -> 0x0053 }
            boolean r2 = android.text.TextUtils.isEmpty(r1)     // Catch:{ Exception -> 0x0053 }
            if (r2 != 0) goto L_0x001d
            android.util.SparseArray<java.lang.String> r2 = com.oneplus.aod.utils.OpAodXmlParser.CONTROLLER_MAPPING_CACHE     // Catch:{ Exception -> 0x0053 }
            r2.put(r4, r1)     // Catch:{ Exception -> 0x0053 }
            if (r3 == 0) goto L_0x004f
            r3.close()
        L_0x004f:
            return r1
        L_0x0050:
            if (r3 == 0) goto L_0x0065
            goto L_0x0062
        L_0x0053:
            r4 = move-exception
            goto L_0x0059
        L_0x0055:
            r4 = move-exception
            goto L_0x0068
        L_0x0057:
            r4 = move-exception
            r3 = r0
        L_0x0059:
            java.lang.String r1 = "OpAodXmlParser"
            java.lang.String r2 = "getControllerName occur exception"
            android.util.Log.e(r1, r2, r4)     // Catch:{ all -> 0x0066 }
            if (r3 == 0) goto L_0x0065
        L_0x0062:
            r3.close()
        L_0x0065:
            return r0
        L_0x0066:
            r4 = move-exception
            r0 = r3
        L_0x0068:
            if (r0 == 0) goto L_0x006d
            r0.close()
        L_0x006d:
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.utils.OpAodXmlParser.getControllerName(android.content.Context, int):java.lang.String");
    }

    static void parseSystemInfoArea(Context context, XmlResourceParser xmlResourceParser, OpAodSettings.OpSystemViewInfo opSystemViewInfo, OpAodSettings.OpViewInfo... opViewInfoArr) {
        try {
            int depth = xmlResourceParser.getDepth();
            while (true) {
                int next = xmlResourceParser.next();
                if (next == 1) {
                    return;
                }
                if (next == 3 && xmlResourceParser.getDepth() <= depth) {
                    return;
                }
                if (next != 3) {
                    if (next != 4) {
                        String name = xmlResourceParser.getName();
                        if ("systemInfoContainer".equals(name)) {
                            opSystemViewInfo.parse(context, xmlResourceParser);
                        } else if ("dateInfo".equals(name)) {
                            ((OpAodSettings.OpDateViewInfo) opViewInfoArr[0]).parse(context, xmlResourceParser);
                        } else if ("sliceInfo".equals(name)) {
                            ((OpAodSettings.OpTextViewInfo) opViewInfoArr[1]).parse(context, xmlResourceParser);
                        } else if ("batteryInfo".equals(name)) {
                            ((OpAodSettings.OpTextViewInfo) opViewInfoArr[2]).parse(context, xmlResourceParser);
                        } else if ("notificationInfo".equals(name)) {
                            ((OpAodSettings.OpTextViewInfo) opViewInfoArr[3]).parse(context, xmlResourceParser);
                        } else if ("ownerInfo".equals(name)) {
                            ((OpAodSettings.OpTextViewInfo) opViewInfoArr[4]).parse(context, xmlResourceParser);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("OpAodXmlParser", "parseSystemInfoArea occur error", e);
        }
    }
}
