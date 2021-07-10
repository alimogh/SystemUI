package com.google.android.material.chip;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import com.google.android.material.R$attr;
import com.google.android.material.R$string;
import com.google.android.material.R$style;
import com.google.android.material.R$styleable;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.TextAppearance;
import com.google.android.material.resources.TextAppearanceFontCallback;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
public class Chip extends AppCompatCheckBox implements ChipDrawable.Delegate, Shapeable {
    private static final int[] CHECKABLE_STATE_SET = {16842911};
    private static final int DEF_STYLE_RES = R$style.Widget_MaterialComponents_Chip_Action;
    private static final Rect EMPTY_BOUNDS = new Rect();
    private static final int[] SELECTED_STATE = {16842913};
    private ChipDrawable chipDrawable;
    private boolean closeIconFocused;
    private boolean closeIconHovered;
    private boolean closeIconPressed;
    private boolean deferredCheckedValue;
    private boolean ensureMinTouchTargetSize;
    private final TextAppearanceFontCallback fontCallback;
    private InsetDrawable insetBackgroundDrawable;
    private int lastLayoutDirection;
    private int minTouchTargetSize;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListenerInternal;
    private View.OnClickListener onCloseIconClickListener;
    private final Rect rect;
    private final RectF rectF;
    private RippleDrawable ripple;
    private final ChipTouchHelper touchHelper;

    public Chip(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R$attr.chipStyle);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Chip(android.content.Context r9, android.util.AttributeSet r10, int r11) {
        /*
            r8 = this;
            int r0 = com.google.android.material.chip.Chip.DEF_STYLE_RES
            android.content.Context r9 = com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap(r9, r10, r11, r0)
            r8.<init>(r9, r10, r11)
            android.graphics.Rect r9 = new android.graphics.Rect
            r9.<init>()
            r8.rect = r9
            android.graphics.RectF r9 = new android.graphics.RectF
            r9.<init>()
            r8.rectF = r9
            com.google.android.material.chip.Chip$1 r9 = new com.google.android.material.chip.Chip$1
            r9.<init>()
            r8.fontCallback = r9
            android.content.Context r9 = r8.getContext()
            r8.validateAttributes(r10)
            com.google.android.material.chip.ChipDrawable r0 = com.google.android.material.chip.ChipDrawable.createFromAttributes(r9, r10, r11, r0)
            r8.initMinTouchTarget(r9, r10, r11)
            r8.setChipDrawable(r0)
            float r1 = androidx.core.view.ViewCompat.getElevation(r8)
            r0.setElevation(r1)
            int[] r3 = com.google.android.material.R$styleable.Chip
            int r5 = com.google.android.material.chip.Chip.DEF_STYLE_RES
            r7 = 0
            int[] r6 = new int[r7]
            r1 = r9
            r2 = r10
            r4 = r11
            android.content.res.TypedArray r10 = com.google.android.material.internal.ThemeEnforcement.obtainStyledAttributes(r1, r2, r3, r4, r5, r6)
            int r11 = android.os.Build.VERSION.SDK_INT
            r1 = 23
            if (r11 >= r1) goto L_0x0053
            int r11 = com.google.android.material.R$styleable.Chip_android_textColor
            android.content.res.ColorStateList r9 = com.google.android.material.resources.MaterialResources.getColorStateList(r9, r10, r11)
            r8.setTextColor(r9)
        L_0x0053:
            int r9 = com.google.android.material.R$styleable.Chip_shapeAppearance
            boolean r9 = r10.hasValue(r9)
            r10.recycle()
            com.google.android.material.chip.Chip$ChipTouchHelper r10 = new com.google.android.material.chip.Chip$ChipTouchHelper
            r10.<init>(r8)
            r8.touchHelper = r10
            r8.updateAccessibilityDelegate()
            if (r9 != 0) goto L_0x006b
            r8.initOutlineProvider()
        L_0x006b:
            boolean r9 = r8.deferredCheckedValue
            r8.setChecked(r9)
            java.lang.CharSequence r9 = r0.getText()
            r8.setText(r9)
            android.text.TextUtils$TruncateAt r9 = r0.getEllipsize()
            r8.setEllipsize(r9)
            r8.setIncludeFontPadding(r7)
            r8.updateTextPaintDrawState()
            com.google.android.material.chip.ChipDrawable r9 = r8.chipDrawable
            boolean r9 = r9.shouldDrawText()
            if (r9 != 0) goto L_0x0093
            r9 = 1
            r8.setLines(r9)
            r8.setHorizontallyScrolling(r9)
        L_0x0093:
            r9 = 8388627(0x800013, float:1.175497E-38)
            r8.setGravity(r9)
            r8.updatePaddingInternal()
            boolean r9 = r8.shouldEnsureMinTouchTargetSize()
            if (r9 == 0) goto L_0x00a7
            int r9 = r8.minTouchTargetSize
            r8.setMinHeight(r9)
        L_0x00a7:
            int r9 = androidx.core.view.ViewCompat.getLayoutDirection(r8)
            r8.lastLayoutDirection = r9
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.chip.Chip.<init>(android.content.Context, android.util.AttributeSet, int):void");
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        MaterialShapeUtils.setParentAbsoluteElevation(this, this.chipDrawable);
    }

    @Override // android.view.View
    public void setElevation(float f) {
        super.setElevation(f);
        ChipDrawable chipDrawable = this.chipDrawable;
        if (chipDrawable != null) {
            chipDrawable.setElevation(f);
        }
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (isCheckable() || isClickable()) {
            accessibilityNodeInfo.setClassName(isCheckable() ? "android.widget.CompoundButton" : "android.widget.Button");
        } else {
            accessibilityNodeInfo.setClassName("android.view.View");
        }
        accessibilityNodeInfo.setCheckable(isCheckable());
        accessibilityNodeInfo.setClickable(isClickable());
        if (getParent() instanceof ChipGroup) {
            ChipGroup chipGroup = (ChipGroup) getParent();
            AccessibilityNodeInfoCompat.wrap(accessibilityNodeInfo).setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(chipGroup.getRowIndex(this), 1, chipGroup.isSingleLine() ? chipGroup.getIndexOfChip(this) : -1, 1, false, isChecked()));
        }
    }

    private void updateAccessibilityDelegate() {
        if (!hasCloseIcon() || !isCloseIconVisible() || this.onCloseIconClickListener == null) {
            ViewCompat.setAccessibilityDelegate(this, null);
        } else {
            ViewCompat.setAccessibilityDelegate(this, this.touchHelper);
        }
    }

    private void initMinTouchTarget(Context context, AttributeSet attributeSet, int i) {
        TypedArray obtainStyledAttributes = ThemeEnforcement.obtainStyledAttributes(context, attributeSet, R$styleable.Chip, i, DEF_STYLE_RES, new int[0]);
        this.ensureMinTouchTargetSize = obtainStyledAttributes.getBoolean(R$styleable.Chip_ensureMinTouchTargetSize, false);
        this.minTouchTargetSize = (int) Math.ceil((double) obtainStyledAttributes.getDimension(R$styleable.Chip_chipMinTouchTargetSize, (float) Math.ceil((double) ViewUtils.dpToPx(getContext(), 48))));
        obtainStyledAttributes.recycle();
    }

    private void updatePaddingInternal() {
        ChipDrawable chipDrawable;
        if (!TextUtils.isEmpty(getText()) && (chipDrawable = this.chipDrawable) != null) {
            int chipEndPadding = (int) (chipDrawable.getChipEndPadding() + this.chipDrawable.getTextEndPadding() + this.chipDrawable.calculateCloseIconWidth());
            int chipStartPadding = (int) (this.chipDrawable.getChipStartPadding() + this.chipDrawable.getTextStartPadding() + this.chipDrawable.calculateChipIconWidth());
            if (this.insetBackgroundDrawable != null) {
                Rect rect = new Rect();
                this.insetBackgroundDrawable.getPadding(rect);
                chipStartPadding += rect.left;
                chipEndPadding += rect.right;
            }
            ViewCompat.setPaddingRelative(this, chipStartPadding, getPaddingTop(), chipEndPadding, getPaddingBottom());
        }
    }

    @Override // android.widget.TextView, android.view.View
    @TargetApi(17)
    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        if (this.lastLayoutDirection != i) {
            this.lastLayoutDirection = i;
            updatePaddingInternal();
        }
    }

    private void validateAttributes(AttributeSet attributeSet) {
        if (attributeSet != null) {
            if (attributeSet.getAttributeValue("http://schemas.android.com/apk/res/android", "background") != null) {
                Log.w("Chip", "Do not set the background; Chip manages its own background drawable.");
            }
            if (attributeSet.getAttributeValue("http://schemas.android.com/apk/res/android", "drawableLeft") != null) {
                throw new UnsupportedOperationException("Please set left drawable using R.attr#chipIcon.");
            } else if (attributeSet.getAttributeValue("http://schemas.android.com/apk/res/android", "drawableStart") != null) {
                throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
            } else if (attributeSet.getAttributeValue("http://schemas.android.com/apk/res/android", "drawableEnd") != null) {
                throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
            } else if (attributeSet.getAttributeValue("http://schemas.android.com/apk/res/android", "drawableRight") != null) {
                throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
            } else if (!attributeSet.getAttributeBooleanValue("http://schemas.android.com/apk/res/android", "singleLine", true) || attributeSet.getAttributeIntValue("http://schemas.android.com/apk/res/android", "lines", 1) != 1 || attributeSet.getAttributeIntValue("http://schemas.android.com/apk/res/android", "minLines", 1) != 1 || attributeSet.getAttributeIntValue("http://schemas.android.com/apk/res/android", "maxLines", 1) != 1) {
                throw new UnsupportedOperationException("Chip does not support multi-line text");
            } else if (attributeSet.getAttributeIntValue("http://schemas.android.com/apk/res/android", "gravity", 8388627) != 8388627) {
                Log.w("Chip", "Chip text must be vertically center and start aligned");
            }
        }
    }

    private void initOutlineProvider() {
        if (Build.VERSION.SDK_INT >= 21) {
            setOutlineProvider(new ViewOutlineProvider() { // from class: com.google.android.material.chip.Chip.2
                @Override // android.view.ViewOutlineProvider
                @TargetApi(21)
                public void getOutline(View view, Outline outline) {
                    if (Chip.this.chipDrawable != null) {
                        Chip.this.chipDrawable.getOutline(outline);
                    } else {
                        outline.setAlpha(0.0f);
                    }
                }
            });
        }
    }

    public void setChipDrawable(ChipDrawable chipDrawable) {
        ChipDrawable chipDrawable2 = this.chipDrawable;
        if (chipDrawable2 != chipDrawable) {
            unapplyChipDrawable(chipDrawable2);
            this.chipDrawable = chipDrawable;
            chipDrawable.setShouldDrawText(false);
            applyChipDrawable(this.chipDrawable);
            ensureAccessibleTouchTarget(this.minTouchTargetSize);
        }
    }

    private void updateBackgroundDrawable() {
        if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
            updateFrameworkRippleBackground();
            return;
        }
        this.chipDrawable.setUseCompatRipple(true);
        ViewCompat.setBackground(this, getBackgroundDrawable());
        updatePaddingInternal();
        ensureChipDrawableHasCallback();
    }

    private void ensureChipDrawableHasCallback() {
        if (getBackgroundDrawable() == this.insetBackgroundDrawable && this.chipDrawable.getCallback() == null) {
            this.chipDrawable.setCallback(this.insetBackgroundDrawable);
        }
    }

    public Drawable getBackgroundDrawable() {
        InsetDrawable insetDrawable = this.insetBackgroundDrawable;
        return insetDrawable == null ? this.chipDrawable : insetDrawable;
    }

    private void updateFrameworkRippleBackground() {
        this.ripple = new RippleDrawable(RippleUtils.sanitizeRippleDrawableColor(this.chipDrawable.getRippleColor()), getBackgroundDrawable(), null);
        this.chipDrawable.setUseCompatRipple(false);
        ViewCompat.setBackground(this, this.ripple);
        updatePaddingInternal();
    }

    private void unapplyChipDrawable(ChipDrawable chipDrawable) {
        if (chipDrawable != null) {
            chipDrawable.setDelegate(null);
        }
    }

    private void applyChipDrawable(ChipDrawable chipDrawable) {
        chipDrawable.setDelegate(this);
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.widget.AppCompatCheckBox, android.widget.TextView, android.widget.CompoundButton, android.view.View
    public int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 2);
        if (isChecked()) {
            CheckBox.mergeDrawableStates(onCreateDrawableState, SELECTED_STATE);
        }
        if (isCheckable()) {
            CheckBox.mergeDrawableStates(onCreateDrawableState, CHECKABLE_STATE_SET);
        }
        return onCreateDrawableState;
    }

    @Override // android.widget.TextView
    public void setGravity(int i) {
        if (i != 8388627) {
            Log.w("Chip", "Chip text must be vertically center and start aligned");
        } else {
            super.setGravity(i);
        }
    }

    @Override // android.view.View
    public void setBackgroundTintList(ColorStateList colorStateList) {
        Log.w("Chip", "Do not set the background tint list; Chip manages its own background drawable.");
    }

    @Override // android.view.View
    public void setBackgroundTintMode(PorterDuff.Mode mode) {
        Log.w("Chip", "Do not set the background tint mode; Chip manages its own background drawable.");
    }

    @Override // android.view.View
    public void setBackgroundColor(int i) {
        Log.w("Chip", "Do not set the background color; Chip manages its own background drawable.");
    }

    @Override // androidx.appcompat.widget.AppCompatCheckBox, android.view.View
    public void setBackgroundResource(int i) {
        Log.w("Chip", "Do not set the background resource; Chip manages its own background drawable.");
    }

    @Override // android.view.View
    public void setBackground(Drawable drawable) {
        if (drawable == getBackgroundDrawable() || drawable == this.ripple) {
            super.setBackground(drawable);
        } else {
            Log.w("Chip", "Do not set the background; Chip manages its own background drawable.");
        }
    }

    @Override // androidx.appcompat.widget.AppCompatCheckBox, android.view.View
    public void setBackgroundDrawable(Drawable drawable) {
        if (drawable == getBackgroundDrawable() || drawable == this.ripple) {
            super.setBackgroundDrawable(drawable);
        } else {
            Log.w("Chip", "Do not set the background drawable; Chip manages its own background drawable.");
        }
    }

    @Override // android.widget.TextView
    public void setCompoundDrawables(Drawable drawable, Drawable drawable2, Drawable drawable3, Drawable drawable4) {
        if (drawable != null) {
            throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
        } else if (drawable3 == null) {
            super.setCompoundDrawables(drawable, drawable2, drawable3, drawable4);
        } else {
            throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
        }
    }

    @Override // android.widget.TextView
    public void setCompoundDrawablesWithIntrinsicBounds(int i, int i2, int i3, int i4) {
        if (i != 0) {
            throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
        } else if (i3 == 0) {
            super.setCompoundDrawablesWithIntrinsicBounds(i, i2, i3, i4);
        } else {
            throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
        }
    }

    @Override // android.widget.TextView
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable drawable, Drawable drawable2, Drawable drawable3, Drawable drawable4) {
        if (drawable != null) {
            throw new UnsupportedOperationException("Please set left drawable using R.attr#chipIcon.");
        } else if (drawable3 == null) {
            super.setCompoundDrawablesWithIntrinsicBounds(drawable, drawable2, drawable3, drawable4);
        } else {
            throw new UnsupportedOperationException("Please set right drawable using R.attr#closeIcon.");
        }
    }

    @Override // android.widget.TextView
    public void setCompoundDrawablesRelative(Drawable drawable, Drawable drawable2, Drawable drawable3, Drawable drawable4) {
        if (drawable != null) {
            throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
        } else if (drawable3 == null) {
            super.setCompoundDrawablesRelative(drawable, drawable2, drawable3, drawable4);
        } else {
            throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
        }
    }

    @Override // android.widget.TextView
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(int i, int i2, int i3, int i4) {
        if (i != 0) {
            throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
        } else if (i3 == 0) {
            super.setCompoundDrawablesRelativeWithIntrinsicBounds(i, i2, i3, i4);
        } else {
            throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
        }
    }

    @Override // android.widget.TextView
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(Drawable drawable, Drawable drawable2, Drawable drawable3, Drawable drawable4) {
        if (drawable != null) {
            throw new UnsupportedOperationException("Please set start drawable using R.attr#chipIcon.");
        } else if (drawable3 == null) {
            super.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, drawable2, drawable3, drawable4);
        } else {
            throw new UnsupportedOperationException("Please set end drawable using R.attr#closeIcon.");
        }
    }

    @Override // android.widget.TextView
    public TextUtils.TruncateAt getEllipsize() {
        ChipDrawable chipDrawable = this.chipDrawable;
        if (chipDrawable != null) {
            return chipDrawable.getEllipsize();
        }
        return null;
    }

    @Override // android.widget.TextView
    public void setEllipsize(TextUtils.TruncateAt truncateAt) {
        if (this.chipDrawable != null) {
            if (truncateAt != TextUtils.TruncateAt.MARQUEE) {
                super.setEllipsize(truncateAt);
                ChipDrawable chipDrawable = this.chipDrawable;
                if (chipDrawable != null) {
                    chipDrawable.setEllipsize(truncateAt);
                    return;
                }
                return;
            }
            throw new UnsupportedOperationException("Text within a chip are not allowed to scroll.");
        }
    }

    @Override // android.widget.TextView
    public void setSingleLine(boolean z) {
        if (z) {
            super.setSingleLine(z);
            return;
        }
        throw new UnsupportedOperationException("Chip does not support multi-line text");
    }

    @Override // android.widget.TextView
    public void setLines(int i) {
        if (i <= 1) {
            super.setLines(i);
            return;
        }
        throw new UnsupportedOperationException("Chip does not support multi-line text");
    }

    @Override // android.widget.TextView
    public void setMinLines(int i) {
        if (i <= 1) {
            super.setMinLines(i);
            return;
        }
        throw new UnsupportedOperationException("Chip does not support multi-line text");
    }

    @Override // android.widget.TextView
    public void setMaxLines(int i) {
        if (i <= 1) {
            super.setMaxLines(i);
            return;
        }
        throw new UnsupportedOperationException("Chip does not support multi-line text");
    }

    @Override // android.widget.TextView
    public void setMaxWidth(int i) {
        super.setMaxWidth(i);
        ChipDrawable chipDrawable = this.chipDrawable;
        if (chipDrawable != null) {
            chipDrawable.setMaxWidth(i);
        }
    }

    @Override // com.google.android.material.chip.ChipDrawable.Delegate
    public void onChipDrawableSizeChange() {
        ensureAccessibleTouchTarget(this.minTouchTargetSize);
        requestLayout();
        if (Build.VERSION.SDK_INT >= 21) {
            invalidateOutline();
        }
    }

    @Override // androidx.appcompat.widget.AppCompatCheckBox, android.widget.CompoundButton, android.widget.Checkable
    public void setChecked(boolean z) {
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
        ChipDrawable chipDrawable = this.chipDrawable;
        if (chipDrawable == null) {
            this.deferredCheckedValue = z;
        } else if (chipDrawable.isCheckable()) {
            boolean isChecked = isChecked();
            super.setChecked(z);
            if (isChecked != z && (onCheckedChangeListener = this.onCheckedChangeListenerInternal) != null) {
                onCheckedChangeListener.onCheckedChanged(this, z);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setOnCheckedChangeListenerInternal(CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListenerInternal = onCheckedChangeListener;
    }

    public boolean performCloseIconClick() {
        boolean z = false;
        playSoundEffect(0);
        View.OnClickListener onClickListener = this.onCloseIconClickListener;
        if (onClickListener != null) {
            onClickListener.onClick(this);
            z = true;
        }
        this.touchHelper.sendEventForVirtualView(1, 1);
        return z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x001e, code lost:
        if (r0 != 3) goto L_0x0040;
     */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0043  */
    @Override // android.widget.TextView, android.view.View
    @android.annotation.SuppressLint({"ClickableViewAccessibility"})
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r6) {
        /*
            r5 = this;
            int r0 = r6.getActionMasked()
            android.graphics.RectF r1 = r5.getCloseIconTouchBounds()
            float r2 = r6.getX()
            float r3 = r6.getY()
            boolean r1 = r1.contains(r2, r3)
            r2 = 0
            r3 = 1
            if (r0 == 0) goto L_0x0039
            if (r0 == r3) goto L_0x002b
            r4 = 2
            if (r0 == r4) goto L_0x0021
            r1 = 3
            if (r0 == r1) goto L_0x0034
            goto L_0x0040
        L_0x0021:
            boolean r0 = r5.closeIconPressed
            if (r0 == 0) goto L_0x0040
            if (r1 != 0) goto L_0x003e
            r5.setCloseIconPressed(r2)
            goto L_0x003e
        L_0x002b:
            boolean r0 = r5.closeIconPressed
            if (r0 == 0) goto L_0x0034
            r5.performCloseIconClick()
            r0 = r3
            goto L_0x0035
        L_0x0034:
            r0 = r2
        L_0x0035:
            r5.setCloseIconPressed(r2)
            goto L_0x0041
        L_0x0039:
            if (r1 == 0) goto L_0x0040
            r5.setCloseIconPressed(r3)
        L_0x003e:
            r0 = r3
            goto L_0x0041
        L_0x0040:
            r0 = r2
        L_0x0041:
            if (r0 != 0) goto L_0x0049
            boolean r5 = super.onTouchEvent(r6)
            if (r5 == 0) goto L_0x004a
        L_0x0049:
            r2 = r3
        L_0x004a:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.material.chip.Chip.onTouchEvent(android.view.MotionEvent):boolean");
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 7) {
            setCloseIconHovered(getCloseIconTouchBounds().contains(motionEvent.getX(), motionEvent.getY()));
        } else if (actionMasked == 10) {
            setCloseIconHovered(false);
        }
        return super.onHoverEvent(motionEvent);
    }

    @SuppressLint({"PrivateApi"})
    private boolean handleAccessibilityExit(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 10) {
            try {
                Field declaredField = ExploreByTouchHelper.class.getDeclaredField("mHoveredVirtualViewId");
                declaredField.setAccessible(true);
                if (((Integer) declaredField.get(this.touchHelper)).intValue() != Integer.MIN_VALUE) {
                    Method declaredMethod = ExploreByTouchHelper.class.getDeclaredMethod("updateHoveredVirtualView", Integer.TYPE);
                    declaredMethod.setAccessible(true);
                    declaredMethod.invoke(this.touchHelper, Integer.MIN_VALUE);
                    return true;
                }
            } catch (NoSuchMethodException e) {
                Log.e("Chip", "Unable to send Accessibility Exit event", e);
            } catch (IllegalAccessException e2) {
                Log.e("Chip", "Unable to send Accessibility Exit event", e2);
            } catch (InvocationTargetException e3) {
                Log.e("Chip", "Unable to send Accessibility Exit event", e3);
            } catch (NoSuchFieldException e4) {
                Log.e("Chip", "Unable to send Accessibility Exit event", e4);
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return handleAccessibilityExit(motionEvent) || this.touchHelper.dispatchHoverEvent(motionEvent) || super.dispatchHoverEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (!this.touchHelper.dispatchKeyEvent(keyEvent) || this.touchHelper.getKeyboardFocusedVirtualViewId() == Integer.MIN_VALUE) {
            return super.dispatchKeyEvent(keyEvent);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onFocusChanged(boolean z, int i, Rect rect) {
        super.onFocusChanged(z, i, rect);
        this.touchHelper.onFocusChanged(z, i, rect);
    }

    @Override // android.widget.TextView, android.view.View
    public void getFocusedRect(Rect rect) {
        if (this.touchHelper.getKeyboardFocusedVirtualViewId() == 1 || this.touchHelper.getAccessibilityFocusedVirtualViewId() == 1) {
            rect.set(getCloseIconTouchBoundsInt());
        } else {
            super.getFocusedRect(rect);
        }
    }

    private void setCloseIconPressed(boolean z) {
        if (this.closeIconPressed != z) {
            this.closeIconPressed = z;
            refreshDrawableState();
        }
    }

    private void setCloseIconHovered(boolean z) {
        if (this.closeIconHovered != z) {
            this.closeIconHovered = z;
            refreshDrawableState();
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.appcompat.widget.AppCompatCheckBox, android.widget.TextView, android.widget.CompoundButton, android.view.View
    public void drawableStateChanged() {
        super.drawableStateChanged();
        ChipDrawable chipDrawable = this.chipDrawable;
        if ((chipDrawable == null || !chipDrawable.isCloseIconStateful()) ? false : this.chipDrawable.setCloseIconState(createCloseIconDrawableState())) {
            invalidate();
        }
    }

    private int[] createCloseIconDrawableState() {
        int i = 0;
        int i2 = isEnabled() ? 1 : 0;
        if (this.closeIconFocused) {
            i2++;
        }
        if (this.closeIconHovered) {
            i2++;
        }
        if (this.closeIconPressed) {
            i2++;
        }
        if (isChecked()) {
            i2++;
        }
        int[] iArr = new int[i2];
        if (isEnabled()) {
            iArr[0] = 16842910;
            i = 1;
        }
        if (this.closeIconFocused) {
            iArr[i] = 16842908;
            i++;
        }
        if (this.closeIconHovered) {
            iArr[i] = 16843623;
            i++;
        }
        if (this.closeIconPressed) {
            iArr[i] = 16842919;
            i++;
        }
        if (isChecked()) {
            iArr[i] = 16842913;
        }
        return iArr;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasCloseIcon() {
        ChipDrawable chipDrawable = this.chipDrawable;
        return (chipDrawable == null || chipDrawable.getCloseIcon() == null) ? false : true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private RectF getCloseIconTouchBounds() {
        this.rectF.setEmpty();
        if (hasCloseIcon()) {
            this.chipDrawable.getCloseIconTouchBounds(this.rectF);
        }
        return this.rectF;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Rect getCloseIconTouchBoundsInt() {
        RectF closeIconTouchBounds = getCloseIconTouchBounds();
        this.rect.set((int) closeIconTouchBounds.left, (int) closeIconTouchBounds.top, (int) closeIconTouchBounds.right, (int) closeIconTouchBounds.bottom);
        return this.rect;
    }

    @Override // android.widget.TextView, android.widget.Button, android.view.View
    @TargetApi(24)
    public PointerIcon onResolvePointerIcon(MotionEvent motionEvent, int i) {
        if (!getCloseIconTouchBounds().contains(motionEvent.getX(), motionEvent.getY()) || !isEnabled()) {
            return null;
        }
        return PointerIcon.getSystemIcon(getContext(), 1002);
    }

    /* access modifiers changed from: private */
    public class ChipTouchHelper extends ExploreByTouchHelper {
        ChipTouchHelper(Chip chip) {
            super(chip);
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public int getVirtualViewAt(float f, float f2) {
            return (!Chip.this.hasCloseIcon() || !Chip.this.getCloseIconTouchBounds().contains(f, f2)) ? 0 : 1;
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void getVisibleVirtualViews(List<Integer> list) {
            list.add(0);
            if (Chip.this.hasCloseIcon() && Chip.this.isCloseIconVisible() && Chip.this.onCloseIconClickListener != null) {
                list.add(1);
            }
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onVirtualViewKeyboardFocusChanged(int i, boolean z) {
            if (i == 1) {
                Chip.this.closeIconFocused = z;
                Chip.this.refreshDrawableState();
            }
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0029: APUT  (r1v1 java.lang.Object[]), (0 ??[int, short, byte, char]), (r0v1 java.lang.CharSequence) */
        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            CharSequence charSequence = "";
            if (i == 1) {
                CharSequence closeIconContentDescription = Chip.this.getCloseIconContentDescription();
                if (closeIconContentDescription != null) {
                    accessibilityNodeInfoCompat.setContentDescription(closeIconContentDescription);
                } else {
                    CharSequence text = Chip.this.getText();
                    Context context = Chip.this.getContext();
                    int i2 = R$string.mtrl_chip_close_icon_content_description;
                    Object[] objArr = new Object[1];
                    if (!TextUtils.isEmpty(text)) {
                        charSequence = text;
                    }
                    objArr[0] = charSequence;
                    accessibilityNodeInfoCompat.setContentDescription(context.getString(i2, objArr).trim());
                }
                accessibilityNodeInfoCompat.setBoundsInParent(Chip.this.getCloseIconTouchBoundsInt());
                accessibilityNodeInfoCompat.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK);
                accessibilityNodeInfoCompat.setEnabled(Chip.this.isEnabled());
                return;
            }
            accessibilityNodeInfoCompat.setContentDescription(charSequence);
            accessibilityNodeInfoCompat.setBoundsInParent(Chip.EMPTY_BOUNDS);
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public void onPopulateNodeForHost(AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            accessibilityNodeInfoCompat.setCheckable(Chip.this.isCheckable());
            accessibilityNodeInfoCompat.setClickable(Chip.this.isClickable());
            if (Chip.this.isCheckable() || Chip.this.isClickable()) {
                accessibilityNodeInfoCompat.setClassName(Chip.this.isCheckable() ? "android.widget.CompoundButton" : "android.widget.Button");
            } else {
                accessibilityNodeInfoCompat.setClassName("android.view.View");
            }
            CharSequence text = Chip.this.getText();
            if (Build.VERSION.SDK_INT >= 23) {
                accessibilityNodeInfoCompat.setText(text);
            } else {
                accessibilityNodeInfoCompat.setContentDescription(text);
            }
        }

        /* access modifiers changed from: protected */
        @Override // androidx.customview.widget.ExploreByTouchHelper
        public boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
            if (i2 != 16) {
                return false;
            }
            if (i == 0) {
                return Chip.this.performClick();
            }
            if (i == 1) {
                return Chip.this.performCloseIconClick();
            }
            return false;
        }
    }

    public float getChipMinHeight() {
        ChipDrawable chipDrawable = this.chipDrawable;
        if (chipDrawable != null) {
            return chipDrawable.getChipMinHeight();
        }
        return 0.0f;
    }

    @Override // com.google.android.material.shape.Shapeable
    public void setShapeAppearanceModel(ShapeAppearanceModel shapeAppearanceModel) {
        this.chipDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    }

    @Override // android.view.View
    public void setLayoutDirection(int i) {
        if (this.chipDrawable != null && Build.VERSION.SDK_INT >= 17) {
            super.setLayoutDirection(i);
        }
    }

    @Override // android.widget.TextView
    public void setText(CharSequence charSequence, TextView.BufferType bufferType) {
        if (this.chipDrawable != null) {
            if (charSequence == null) {
                charSequence = "";
            }
            super.setText(this.chipDrawable.shouldDrawText() ? null : charSequence, bufferType);
            ChipDrawable chipDrawable = this.chipDrawable;
            if (chipDrawable != null) {
                chipDrawable.setText(charSequence);
            }
        }
    }

    @Override // android.widget.TextView
    public void setTextAppearance(Context context, int i) {
        super.setTextAppearance(context, i);
        ChipDrawable chipDrawable = this.chipDrawable;
        if (chipDrawable != null) {
            chipDrawable.setTextAppearanceResource(i);
        }
        updateTextPaintDrawState();
    }

    @Override // android.widget.TextView
    public void setTextAppearance(int i) {
        super.setTextAppearance(i);
        ChipDrawable chipDrawable = this.chipDrawable;
        if (chipDrawable != null) {
            chipDrawable.setTextAppearanceResource(i);
        }
        updateTextPaintDrawState();
    }

    private void updateTextPaintDrawState() {
        TextPaint paint = getPaint();
        ChipDrawable chipDrawable = this.chipDrawable;
        if (chipDrawable != null) {
            paint.drawableState = chipDrawable.getState();
        }
        TextAppearance textAppearance = getTextAppearance();
        if (textAppearance != null) {
            textAppearance.updateDrawState(getContext(), paint, this.fontCallback);
        }
    }

    private TextAppearance getTextAppearance() {
        ChipDrawable chipDrawable = this.chipDrawable;
        if (chipDrawable != null) {
            return chipDrawable.getTextAppearance();
        }
        return null;
    }

    public boolean isCloseIconVisible() {
        ChipDrawable chipDrawable = this.chipDrawable;
        return chipDrawable != null && chipDrawable.isCloseIconVisible();
    }

    public CharSequence getCloseIconContentDescription() {
        ChipDrawable chipDrawable = this.chipDrawable;
        if (chipDrawable != null) {
            return chipDrawable.getCloseIconContentDescription();
        }
        return null;
    }

    public boolean isCheckable() {
        ChipDrawable chipDrawable = this.chipDrawable;
        return chipDrawable != null && chipDrawable.isCheckable();
    }

    public boolean shouldEnsureMinTouchTargetSize() {
        return this.ensureMinTouchTargetSize;
    }

    public boolean ensureAccessibleTouchTarget(int i) {
        this.minTouchTargetSize = i;
        int i2 = 0;
        if (!shouldEnsureMinTouchTargetSize()) {
            if (this.insetBackgroundDrawable != null) {
                removeBackgroundInset();
            } else {
                updateBackgroundDrawable();
            }
            return false;
        }
        int max = Math.max(0, i - this.chipDrawable.getIntrinsicHeight());
        int max2 = Math.max(0, i - this.chipDrawable.getIntrinsicWidth());
        if (max2 > 0 || max > 0) {
            int i3 = max2 > 0 ? max2 / 2 : 0;
            if (max > 0) {
                i2 = max / 2;
            }
            if (this.insetBackgroundDrawable != null) {
                Rect rect = new Rect();
                this.insetBackgroundDrawable.getPadding(rect);
                if (rect.top == i2 && rect.bottom == i2 && rect.left == i3 && rect.right == i3) {
                    updateBackgroundDrawable();
                    return true;
                }
            }
            if (Build.VERSION.SDK_INT >= 16) {
                if (getMinHeight() != i) {
                    setMinHeight(i);
                }
                if (getMinWidth() != i) {
                    setMinWidth(i);
                }
            } else {
                setMinHeight(i);
                setMinWidth(i);
            }
            insetChipBackgroundDrawable(i3, i2, i3, i2);
            updateBackgroundDrawable();
            return true;
        }
        if (this.insetBackgroundDrawable != null) {
            removeBackgroundInset();
        } else {
            updateBackgroundDrawable();
        }
        return false;
    }

    private void removeBackgroundInset() {
        if (this.insetBackgroundDrawable != null) {
            this.insetBackgroundDrawable = null;
            setMinWidth(0);
            setMinHeight((int) getChipMinHeight());
            updateBackgroundDrawable();
        }
    }

    private void insetChipBackgroundDrawable(int i, int i2, int i3, int i4) {
        this.insetBackgroundDrawable = new InsetDrawable((Drawable) this.chipDrawable, i, i2, i3, i4);
    }
}
