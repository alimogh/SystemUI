package com.android.settingslib.inputmethod;

import android.content.Context;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import java.util.ArrayList;
public class InputMethodSettingValuesWrapper {
    private static final String TAG = "InputMethodSettingValuesWrapper";
    private static volatile InputMethodSettingValuesWrapper sInstance;
    private final InputMethodManager mImm;
    private final ArrayList<InputMethodInfo> mMethodList = new ArrayList<>();

    public static InputMethodSettingValuesWrapper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TAG) {
                if (sInstance == null) {
                    sInstance = new InputMethodSettingValuesWrapper(context);
                }
            }
        }
        return sInstance;
    }

    private InputMethodSettingValuesWrapper(Context context) {
        context.getContentResolver();
        this.mImm = (InputMethodManager) context.getSystemService(InputMethodManager.class);
        refreshAllInputMethodAndSubtypes();
    }

    public void refreshAllInputMethodAndSubtypes() {
        this.mMethodList.clear();
        this.mMethodList.addAll(this.mImm.getInputMethodList());
    }
}
