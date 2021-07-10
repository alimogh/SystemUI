package com.oneplus.aod.utils.bitmoji.triggers.base;

import android.content.Context;
import android.text.TextUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.oneplus.aod.utils.bitmoji.OpBitmojiManager;
import java.io.FileDescriptor;
import java.util.Arrays;
import java.util.HashMap;
public abstract class CategoryTrigger extends Trigger {
    protected HashMap<String, String[]> mCategories = new HashMap<>();
    protected HashMap<String, Integer> mUsedImageCount = new HashMap<>();

    /* access modifiers changed from: protected */
    public abstract String[] getCategories();

    /* access modifiers changed from: protected */
    public abstract String getCurrentCategory();

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public int getPriority() {
        return 1;
    }

    public CategoryTrigger(Context context, OpBitmojiManager opBitmojiManager) {
        super(context, opBitmojiManager);
        syncImagePack();
    }

    public void syncImagePack() {
        String[] categories = getCategories();
        this.mCategories.clear();
        this.mUsedImageCount.clear();
        for (String str : categories) {
            this.mCategories.put(str, getHelper().getImagesPathByPackId(str));
            this.mUsedImageCount.put(str, 0);
        }
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void onImagePackUpdate(String str) {
        String[] categories = getCategories();
        if (categories != null && Arrays.asList(categories).contains(str)) {
            this.mCategories.put(str, getHelper().getImagesPathByPackId(str));
            this.mUsedImageCount.put(str, 0);
        }
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public String getCurrentPackId() {
        return getCurrentCategory();
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void dumpDetail(FileDescriptor fileDescriptor, IndentingPrintWriter indentingPrintWriter, String[] strArr) {
        indentingPrintWriter.println("category=" + getCurrentCategory());
    }

    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public String[] getCurrentImageArray() {
        return getListByCategory(getCurrentCategory());
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public int getUsedImageCount() {
        String currentCategory = getCurrentCategory();
        if (!TextUtils.isEmpty(currentCategory)) {
            return this.mUsedImageCount.get(currentCategory).intValue();
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.oneplus.aod.utils.bitmoji.triggers.base.Trigger
    public void upateUsedImageCount(int i) {
        String currentCategory = getCurrentCategory();
        if (!TextUtils.isEmpty(currentCategory)) {
            int intValue = this.mUsedImageCount.get(currentCategory).intValue() + 1;
            if (intValue >= i) {
                intValue = 0;
            }
            this.mUsedImageCount.put(currentCategory, Integer.valueOf(intValue));
        }
    }

    /* access modifiers changed from: protected */
    public String[] getListByCategory(String str) {
        return this.mCategories.get(str);
    }
}
