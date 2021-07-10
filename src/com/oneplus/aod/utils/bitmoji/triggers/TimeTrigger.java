package com.oneplus.aod.utils.bitmoji.triggers;

import android.content.Context;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger;
import java.util.Date;
public class TimeTrigger extends CategoryTrigger {
    public TimeTrigger(Context context, OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String[] getCategories() {
        return new String[]{"mornin", "afternoon", "evening", "night"};
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public String getMdmLabel() {
        char c;
        String currentCategory = getCurrentCategory();
        switch (currentCategory.hashCode()) {
            case -1376511864:
                if (currentCategory.equals("evening")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1068373757:
                if (currentCategory.equals("mornin")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 104817688:
                if (currentCategory.equals("night")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1020028732:
                if (currentCategory.equals("afternoon")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            return "time_morning";
        }
        if (c == 1) {
            return "time_afternoon";
        }
        if (c == 2) {
            return "time_evening";
        }
        if (c != 3) {
            return null;
        }
        return "time_night";
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.CategoryTrigger
    public String getCurrentCategory() {
        int hours = new Date().getHours();
        if (hours >= 6 && hours < 12) {
            return "mornin";
        }
        if (hours < 12 || hours >= 16) {
            return (hours < 16 || hours >= 22) ? "night" : "evening";
        }
        return "afternoon";
    }
}
