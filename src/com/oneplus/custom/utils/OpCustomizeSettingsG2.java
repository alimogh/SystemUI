package com.oneplus.custom.utils;

import com.oneplus.custom.utils.OpCustomizeSettings;
public class OpCustomizeSettingsG2 extends OpCustomizeSettings {
    /* access modifiers changed from: protected */
    @Override // com.oneplus.custom.utils.OpCustomizeSettings
    public OpCustomizeSettings.CUSTOM_TYPE getCustomization() {
        OpCustomizeSettings.CUSTOM_TYPE custom_type = OpCustomizeSettings.CUSTOM_TYPE.NONE;
        int custFlagVal = ParamReader.getCustFlagVal();
        if (custFlagVal == 3) {
            return OpCustomizeSettings.CUSTOM_TYPE.AVG;
        }
        switch (custFlagVal) {
            case 6:
                return OpCustomizeSettings.CUSTOM_TYPE.MCL;
            case 7:
                return OpCustomizeSettings.CUSTOM_TYPE.OPR_RETAIL;
            case 8:
                return OpCustomizeSettings.CUSTOM_TYPE.RED;
            case 9:
                return OpCustomizeSettings.CUSTOM_TYPE.C88;
            default:
                return custom_type;
        }
    }
}
