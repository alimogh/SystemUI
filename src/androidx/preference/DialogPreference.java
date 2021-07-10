package androidx.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.util.AttributeSet;
import androidx.core.content.res.TypedArrayUtils;
public abstract class DialogPreference extends Preference {
    private long lastClickTime;
    private Drawable mDialogIcon;
    private int mDialogLayoutResId;
    private CharSequence mDialogMessage;
    private CharSequence mDialogTitle;
    private CharSequence mNegativeButtonText;
    private CharSequence mPositiveButtonText;

    public interface TargetFragment {
        <T extends Preference> T findPreference(CharSequence charSequence);
    }

    public DialogPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.lastClickTime = 0;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.DialogPreference, i, i2);
        String string = TypedArrayUtils.getString(obtainStyledAttributes, R$styleable.DialogPreference_dialogTitle, R$styleable.DialogPreference_android_dialogTitle);
        this.mDialogTitle = string;
        if (string == null) {
            this.mDialogTitle = getTitle();
        }
        this.mDialogMessage = TypedArrayUtils.getString(obtainStyledAttributes, R$styleable.DialogPreference_dialogMessage, R$styleable.DialogPreference_android_dialogMessage);
        this.mDialogIcon = TypedArrayUtils.getDrawable(obtainStyledAttributes, R$styleable.DialogPreference_dialogIcon, R$styleable.DialogPreference_android_dialogIcon);
        this.mPositiveButtonText = TypedArrayUtils.getString(obtainStyledAttributes, R$styleable.DialogPreference_positiveButtonText, R$styleable.DialogPreference_android_positiveButtonText);
        this.mNegativeButtonText = TypedArrayUtils.getString(obtainStyledAttributes, R$styleable.DialogPreference_negativeButtonText, R$styleable.DialogPreference_android_negativeButtonText);
        this.mDialogLayoutResId = TypedArrayUtils.getResourceId(obtainStyledAttributes, R$styleable.DialogPreference_dialogLayout, R$styleable.DialogPreference_android_dialogLayout, 0);
        obtainStyledAttributes.recycle();
    }

    public DialogPreference(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public DialogPreference(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, TypedArrayUtils.getAttr(context, R$attr.dialogPreferenceStyle, 16842897));
    }

    public DialogPreference(Context context) {
        this(context, null);
    }

    public CharSequence getDialogTitle() {
        return this.mDialogTitle;
    }

    public CharSequence getDialogMessage() {
        return this.mDialogMessage;
    }

    public Drawable getDialogIcon() {
        return this.mDialogIcon;
    }

    public CharSequence getPositiveButtonText() {
        return this.mPositiveButtonText;
    }

    public CharSequence getNegativeButtonText() {
        return this.mNegativeButtonText;
    }

    public int getDialogLayoutResource() {
        return this.mDialogLayoutResId;
    }

    /* access modifiers changed from: protected */
    @Override // androidx.preference.Preference
    public void onClick() {
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        if (timeInMillis - this.lastClickTime > 500) {
            this.lastClickTime = timeInMillis;
            getPreferenceManager().showDialog(this);
        }
    }
}
