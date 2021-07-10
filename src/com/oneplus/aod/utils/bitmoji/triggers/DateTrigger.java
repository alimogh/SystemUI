package com.oneplus.aod.utils.bitmoji.triggers;

import android.content.Context;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger;
import java.util.Calendar;
public class DateTrigger extends CategoryTrigger {
    public DateTrigger(Context context, OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String[] getCategories() {
        return new String[]{"weekday", "weekend"};
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002b  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0032 A[RETURN] */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getMdmLabel() {
        /*
            r3 = this;
            java.lang.String r3 = r3.getCurrentCategory()
            int r0 = r3.hashCode()
            r1 = 1226862376(0x49206f28, float:657138.5)
            r2 = 1
            if (r0 == r1) goto L_0x001e
            r1 = 1226863719(0x49207467, float:657222.44)
            if (r0 == r1) goto L_0x0014
            goto L_0x0028
        L_0x0014:
            java.lang.String r0 = "weekend"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0028
            r3 = r2
            goto L_0x0029
        L_0x001e:
            java.lang.String r0 = "weekday"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0028
            r3 = 0
            goto L_0x0029
        L_0x0028:
            r3 = -1
        L_0x0029:
            if (r3 == 0) goto L_0x0032
            if (r3 == r2) goto L_0x002f
            r3 = 0
            return r3
        L_0x002f:
            java.lang.String r3 = "date_weekend"
            return r3
        L_0x0032:
            java.lang.String r3 = "date_weekday"
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.aod.utils.bitmoji.triggers.DateTrigger.getMdmLabel():java.lang.String");
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String getCurrentCategory() {
        int i = Calendar.getInstance().get(7);
        return (i < 2 || i > 6) ? "weekend" : "weekday";
    }
}
