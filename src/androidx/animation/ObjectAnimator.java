package androidx.animation;

import android.util.Property;
import androidx.animation.AnimationHandler;
import java.lang.ref.WeakReference;
public final class ObjectAnimator extends ValueAnimator {
    private boolean mAutoCancel = false;
    private Property mProperty;
    private String mPropertyName;
    private WeakReference<Object> mTarget;

    public void setPropertyName(String str) {
        PropertyValuesHolder[] propertyValuesHolderArr = this.mValues;
        if (propertyValuesHolderArr != null) {
            PropertyValuesHolder propertyValuesHolder = propertyValuesHolderArr[0];
            String propertyName = propertyValuesHolder.getPropertyName();
            propertyValuesHolder.setPropertyName(str);
            this.mValuesMap.remove(propertyName);
            this.mValuesMap.put(str, propertyValuesHolder);
        }
        this.mPropertyName = str;
        this.mInitialized = false;
    }

    public String getPropertyName() {
        String str = this.mPropertyName;
        String str2 = null;
        if (str != null) {
            return str;
        }
        Property property = this.mProperty;
        if (property != null) {
            return property.getName();
        }
        PropertyValuesHolder[] propertyValuesHolderArr = this.mValues;
        if (propertyValuesHolderArr != null && propertyValuesHolderArr.length > 0) {
            for (int i = 0; i < this.mValues.length; i++) {
                str2 = (i == 0 ? "" : str2 + ",") + this.mValues[i].getPropertyName();
            }
        }
        return str2;
    }

    @Override // androidx.animation.ValueAnimator
    public String getNameForTrace() {
        String str = this.mAnimTraceName;
        if (str != null) {
            return str;
        }
        return "animator:" + getPropertyName();
    }

    public ObjectAnimator() {
    }

    private ObjectAnimator(Object obj, String str) {
        setTarget(obj);
        setPropertyName(str);
    }

    public static ObjectAnimator ofFloat(Object obj, String str, float... fArr) {
        ObjectAnimator objectAnimator = new ObjectAnimator(obj, str);
        objectAnimator.setFloatValues(fArr);
        return objectAnimator;
    }

    @Override // androidx.animation.ValueAnimator
    public void setFloatValues(float... fArr) {
        PropertyValuesHolder[] propertyValuesHolderArr = this.mValues;
        if (propertyValuesHolderArr == null || propertyValuesHolderArr.length == 0) {
            Property property = this.mProperty;
            if (property != null) {
                setValues(PropertyValuesHolder.ofFloat(property, fArr));
            } else {
                setValues(PropertyValuesHolder.ofFloat(this.mPropertyName, fArr));
            }
        } else {
            super.setFloatValues(fArr);
        }
    }

    private boolean hasSameTargetAndProperties(Animator animator) {
        if (animator instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) animator;
            PropertyValuesHolder[] values = objectAnimator.getValues();
            if (objectAnimator.getTarget() == getTarget() && this.mValues.length == values.length) {
                int i = 0;
                while (true) {
                    PropertyValuesHolder[] propertyValuesHolderArr = this.mValues;
                    if (i >= propertyValuesHolderArr.length) {
                        return true;
                    }
                    PropertyValuesHolder propertyValuesHolder = propertyValuesHolderArr[i];
                    PropertyValuesHolder propertyValuesHolder2 = values[i];
                    if (propertyValuesHolder.getPropertyName() == null || !propertyValuesHolder.getPropertyName().equals(propertyValuesHolder2.getPropertyName())) {
                        break;
                    }
                    i++;
                }
                return false;
            }
        }
        return false;
    }

    @Override // androidx.animation.ValueAnimator, androidx.animation.Animator
    public void start() {
        AnimationHandler.getInstance().autoCancelBasedOn(this);
        super.start();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldAutoCancel(AnimationHandler.AnimationFrameCallback animationFrameCallback) {
        if (animationFrameCallback != null && (animationFrameCallback instanceof ObjectAnimator)) {
            ObjectAnimator objectAnimator = (ObjectAnimator) animationFrameCallback;
            if (objectAnimator.mAutoCancel && hasSameTargetAndProperties(objectAnimator)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.animation.ValueAnimator
    public void initAnimation() {
        if (!this.mInitialized) {
            Object target = getTarget();
            if (target != null) {
                int length = this.mValues.length;
                for (int i = 0; i < length; i++) {
                    this.mValues[i].setupSetterAndGetter(target);
                }
            }
            super.initAnimation();
        }
    }

    @Override // androidx.animation.ValueAnimator, androidx.animation.Animator
    public ObjectAnimator setDuration(long j) {
        super.setDuration(j);
        return this;
    }

    public Object getTarget() {
        WeakReference<Object> weakReference = this.mTarget;
        if (weakReference == null) {
            return null;
        }
        return weakReference.get();
    }

    public void setTarget(Object obj) {
        WeakReference<Object> weakReference;
        if (getTarget() != obj) {
            if (isStarted()) {
                cancel();
            }
            if (obj == null) {
                weakReference = null;
            } else {
                weakReference = new WeakReference<>(obj);
            }
            this.mTarget = weakReference;
            this.mInitialized = false;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.animation.ValueAnimator
    public void animateValue(float f) {
        Object target = getTarget();
        if (this.mTarget == null || target != null) {
            super.animateValue(f);
            int length = this.mValues.length;
            for (int i = 0; i < length; i++) {
                this.mValues[i].setAnimatedValue(target);
            }
            return;
        }
        cancel();
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.animation.ValueAnimator, androidx.animation.Animator
    public boolean isInitialized() {
        return this.mInitialized;
    }

    @Override // androidx.animation.ValueAnimator, androidx.animation.Animator, java.lang.Object
    public ObjectAnimator clone() {
        return (ObjectAnimator) super.clone();
    }

    @Override // androidx.animation.ValueAnimator, java.lang.Object
    public String toString() {
        String str = "ObjectAnimator@" + Integer.toHexString(hashCode()) + ", target " + getTarget();
        if (this.mValues != null) {
            for (int i = 0; i < this.mValues.length; i++) {
                str = str + "\n    " + this.mValues[i].toString();
            }
        }
        return str;
    }
}
