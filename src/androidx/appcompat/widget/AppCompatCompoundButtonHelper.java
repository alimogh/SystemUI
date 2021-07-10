package androidx.appcompat.widget;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.CompoundButton;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.CompoundButtonCompat;
/* access modifiers changed from: package-private */
public class AppCompatCompoundButtonHelper {
    private ColorStateList mButtonTintList = null;
    private PorterDuff.Mode mButtonTintMode = null;
    private boolean mHasButtonTint = false;
    private boolean mHasButtonTintMode = false;
    private boolean mSkipNextApply;
    private final CompoundButton mView;

    AppCompatCompoundButtonHelper(CompoundButton compoundButton) {
        this.mView = compoundButton;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0070  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0083  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void loadFromAttributes(android.util.AttributeSet r12, int r13) {
        /*
            r11 = this;
            android.widget.CompoundButton r0 = r11.mView
            android.content.Context r0 = r0.getContext()
            int[] r1 = androidx.appcompat.R$styleable.CompoundButton
            r2 = 0
            androidx.appcompat.widget.TintTypedArray r0 = androidx.appcompat.widget.TintTypedArray.obtainStyledAttributes(r0, r12, r1, r13, r2)
            int r1 = android.os.Build.VERSION.SDK_INT
            r3 = 29
            if (r1 < r3) goto L_0x0025
            android.widget.CompoundButton r4 = r11.mView
            android.content.Context r5 = r4.getContext()
            int[] r6 = androidx.appcompat.R$styleable.CompoundButton
            android.content.res.TypedArray r8 = r0.getWrappedTypeArray()
            r10 = 0
            r7 = r12
            r9 = r13
            r4.saveAttributeDataForStyleable(r5, r6, r7, r8, r9, r10)
        L_0x0025:
            int r12 = androidx.appcompat.R$styleable.CompoundButton_buttonCompat     // Catch:{ all -> 0x0098 }
            boolean r12 = r0.hasValue(r12)     // Catch:{ all -> 0x0098 }
            if (r12 == 0) goto L_0x0046
            int r12 = androidx.appcompat.R$styleable.CompoundButton_buttonCompat     // Catch:{ all -> 0x0098 }
            int r12 = r0.getResourceId(r12, r2)     // Catch:{ all -> 0x0098 }
            if (r12 == 0) goto L_0x0046
            android.widget.CompoundButton r13 = r11.mView     // Catch:{ NotFoundException -> 0x0046 }
            android.widget.CompoundButton r1 = r11.mView     // Catch:{ NotFoundException -> 0x0046 }
            android.content.Context r1 = r1.getContext()     // Catch:{ NotFoundException -> 0x0046 }
            android.graphics.drawable.Drawable r12 = androidx.appcompat.content.res.AppCompatResources.getDrawable(r1, r12)     // Catch:{ NotFoundException -> 0x0046 }
            r13.setButtonDrawable(r12)     // Catch:{ NotFoundException -> 0x0046 }
            r12 = 1
            goto L_0x0047
        L_0x0046:
            r12 = r2
        L_0x0047:
            if (r12 != 0) goto L_0x0068
            int r12 = androidx.appcompat.R$styleable.CompoundButton_android_button
            boolean r12 = r0.hasValue(r12)
            if (r12 == 0) goto L_0x0068
            int r12 = androidx.appcompat.R$styleable.CompoundButton_android_button
            int r12 = r0.getResourceId(r12, r2)
            if (r12 == 0) goto L_0x0068
            android.widget.CompoundButton r13 = r11.mView
            android.widget.CompoundButton r1 = r11.mView
            android.content.Context r1 = r1.getContext()
            android.graphics.drawable.Drawable r12 = androidx.appcompat.content.res.AppCompatResources.getDrawable(r1, r12)
            r13.setButtonDrawable(r12)
        L_0x0068:
            int r12 = androidx.appcompat.R$styleable.CompoundButton_buttonTint
            boolean r12 = r0.hasValue(r12)
            if (r12 == 0) goto L_0x007b
            android.widget.CompoundButton r12 = r11.mView
            int r13 = androidx.appcompat.R$styleable.CompoundButton_buttonTint
            android.content.res.ColorStateList r13 = r0.getColorStateList(r13)
            androidx.core.widget.CompoundButtonCompat.setButtonTintList(r12, r13)
        L_0x007b:
            int r12 = androidx.appcompat.R$styleable.CompoundButton_buttonTintMode
            boolean r12 = r0.hasValue(r12)
            if (r12 == 0) goto L_0x0094
            android.widget.CompoundButton r11 = r11.mView
            int r12 = androidx.appcompat.R$styleable.CompoundButton_buttonTintMode
            r13 = -1
            int r12 = r0.getInt(r12, r13)
            r13 = 0
            android.graphics.PorterDuff$Mode r12 = androidx.appcompat.widget.DrawableUtils.parseTintMode(r12, r13)
            androidx.core.widget.CompoundButtonCompat.setButtonTintMode(r11, r12)
        L_0x0094:
            r0.recycle()
            return
        L_0x0098:
            r11 = move-exception
            r0.recycle()
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.appcompat.widget.AppCompatCompoundButtonHelper.loadFromAttributes(android.util.AttributeSet, int):void");
    }

    /* access modifiers changed from: package-private */
    public void setSupportButtonTintList(ColorStateList colorStateList) {
        this.mButtonTintList = colorStateList;
        this.mHasButtonTint = true;
        applyButtonTint();
    }

    /* access modifiers changed from: package-private */
    public ColorStateList getSupportButtonTintList() {
        return this.mButtonTintList;
    }

    /* access modifiers changed from: package-private */
    public void setSupportButtonTintMode(PorterDuff.Mode mode) {
        this.mButtonTintMode = mode;
        this.mHasButtonTintMode = true;
        applyButtonTint();
    }

    /* access modifiers changed from: package-private */
    public void onSetButtonDrawable() {
        if (this.mSkipNextApply) {
            this.mSkipNextApply = false;
            return;
        }
        this.mSkipNextApply = true;
        applyButtonTint();
    }

    /* access modifiers changed from: package-private */
    public void applyButtonTint() {
        Drawable buttonDrawable = CompoundButtonCompat.getButtonDrawable(this.mView);
        if (buttonDrawable == null) {
            return;
        }
        if (this.mHasButtonTint || this.mHasButtonTintMode) {
            Drawable mutate = DrawableCompat.wrap(buttonDrawable).mutate();
            if (this.mHasButtonTint) {
                DrawableCompat.setTintList(mutate, this.mButtonTintList);
            }
            if (this.mHasButtonTintMode) {
                DrawableCompat.setTintMode(mutate, this.mButtonTintMode);
            }
            if (mutate.isStateful()) {
                mutate.setState(this.mView.getDrawableState());
            }
            this.mView.setButtonDrawable(mutate);
        }
    }

    /* access modifiers changed from: package-private */
    public int getCompoundPaddingLeft(int i) {
        Drawable buttonDrawable;
        return (Build.VERSION.SDK_INT >= 17 || (buttonDrawable = CompoundButtonCompat.getButtonDrawable(this.mView)) == null) ? i : i + buttonDrawable.getIntrinsicWidth();
    }
}
