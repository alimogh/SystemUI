package androidx.animation;

import android.util.Log;
import android.util.Property;
import androidx.animation.Keyframes;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
public class PropertyValuesHolder implements Cloneable {
    private static final Class<?>[] DOUBLE_VARIANTS;
    private static final Class<?>[] FLOAT_VARIANTS;
    private static final Class<?>[] INTEGER_VARIANTS;
    private static final TypeEvaluator sFloatEvaluator = new FloatEvaluator();
    private static final HashMap<Class<?>, HashMap<String, Method>> sGetterPropertyMap = new HashMap<>();
    private static final TypeEvaluator sIntEvaluator = new IntEvaluator();
    static final HashMap<Class<?>, HashMap<String, Method>> sSetterPropertyMap = new HashMap<>();
    private Object mAnimatedValue;
    private TypeConverter mConverter;
    private TypeEvaluator mEvaluator;
    private Method mGetter;
    Keyframes mKeyframes;
    Property mProperty;
    String mPropertyName;
    Method mSetter;
    final Object[] mTmpValueArray;
    Class<?> mValueType;

    static {
        Class<?> cls = Integer.TYPE;
        FLOAT_VARIANTS = new Class[]{Float.TYPE, Float.class, Double.TYPE, cls, Double.class, Integer.class};
        Class<?> cls2 = Double.TYPE;
        INTEGER_VARIANTS = new Class[]{cls, Integer.class, Float.TYPE, cls2, Float.class, Double.class};
        DOUBLE_VARIANTS = new Class[]{cls2, Double.class, Float.TYPE, Integer.TYPE, Float.class, Integer.class};
    }

    PropertyValuesHolder(String str) {
        this.mSetter = null;
        this.mGetter = null;
        this.mKeyframes = null;
        this.mTmpValueArray = new Object[1];
        this.mPropertyName = str;
    }

    PropertyValuesHolder(Property property) {
        this.mSetter = null;
        this.mGetter = null;
        this.mKeyframes = null;
        this.mTmpValueArray = new Object[1];
        this.mProperty = property;
        if (property != null) {
            this.mPropertyName = property.getName();
        }
    }

    public static PropertyValuesHolder ofFloat(String str, float... fArr) {
        return new FloatPropertyValuesHolder(str, fArr);
    }

    public static PropertyValuesHolder ofFloat(Property<?, Float> property, float... fArr) {
        return new FloatPropertyValuesHolder(property, fArr);
    }

    public void setFloatValues(float... fArr) {
        this.mValueType = Float.TYPE;
        this.mKeyframes = KeyframeSet.ofFloat(fArr);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x003d: APUT  (r3v0 java.lang.Class<?>[]), (0 ??[int, short, byte, char]), (r7v0 java.lang.Class<?>) */
    private Method getPropertyFunction(Class<?> cls, String str, Class<?> cls2) {
        Class<?>[] clsArr;
        String methodName = getMethodName(str, this.mPropertyName);
        Method method = null;
        if (cls2 == null) {
            try {
                method = cls.getMethod(methodName, null);
            } catch (NoSuchMethodException unused) {
            }
        } else {
            Class<?>[] clsArr2 = new Class[1];
            if (cls2.equals(Float.class)) {
                clsArr = FLOAT_VARIANTS;
            } else if (cls2.equals(Integer.class)) {
                clsArr = INTEGER_VARIANTS;
            } else {
                clsArr = cls2.equals(Double.class) ? DOUBLE_VARIANTS : new Class[]{cls2};
            }
            for (Class<?> cls3 : clsArr) {
                clsArr2[0] = cls3;
                try {
                    Method method2 = cls.getMethod(methodName, clsArr2);
                    if (this.mConverter == null) {
                        this.mValueType = cls3;
                    }
                    return method2;
                } catch (NoSuchMethodException unused2) {
                }
            }
        }
        if (method == null) {
            Log.w("PropertyValuesHolder", "Method " + getMethodName(str, this.mPropertyName) + "() with type " + cls2 + " not found on target class " + cls);
        }
        return method;
    }

    private Method setupSetterOrGetter(Class<?> cls, HashMap<Class<?>, HashMap<String, Method>> hashMap, String str, Class<?> cls2) {
        Method method;
        synchronized (hashMap) {
            HashMap<String, Method> hashMap2 = hashMap.get(cls);
            boolean z = false;
            method = null;
            if (hashMap2 != null && (z = hashMap2.containsKey(this.mPropertyName))) {
                method = hashMap2.get(this.mPropertyName);
            }
            if (!z) {
                method = getPropertyFunction(cls, str, cls2);
                if (hashMap2 == null) {
                    hashMap2 = new HashMap<>();
                    hashMap.put(cls, hashMap2);
                }
                hashMap2.put(this.mPropertyName, method);
            }
        }
        return method;
    }

    /* access modifiers changed from: package-private */
    public void setupSetter(Class<?> cls) {
        TypeConverter typeConverter = this.mConverter;
        this.mSetter = setupSetterOrGetter(cls, sSetterPropertyMap, "set", typeConverter == null ? this.mValueType : typeConverter.getTargetType());
    }

    private void setupGetter(Class<?> cls) {
        this.mGetter = setupSetterOrGetter(cls, sGetterPropertyMap, "get", null);
    }

    /* access modifiers changed from: package-private */
    public void setupSetterAndGetter(Object obj) {
        if (this.mProperty != null) {
            try {
                List keyframes = this.mKeyframes.getKeyframes();
                int size = keyframes == null ? 0 : keyframes.size();
                Object obj2 = null;
                for (int i = 0; i < size; i++) {
                    Keyframe keyframe = (Keyframe) keyframes.get(i);
                    if (!keyframe.hasValue() || keyframe.valueWasSetOnStart()) {
                        if (obj2 == null) {
                            obj2 = convertBack(this.mProperty.get(obj));
                        }
                        keyframe.setValue(obj2);
                        keyframe.setValueWasSetOnStart(true);
                    }
                }
                return;
            } catch (ClassCastException unused) {
                Log.w("PropertyValuesHolder", "No such property (" + this.mProperty.getName() + ") on target object " + obj + ". Trying reflection instead");
                this.mProperty = null;
            }
        }
        if (this.mProperty == null) {
            Class<?> cls = obj.getClass();
            if (this.mSetter == null) {
                setupSetter(cls);
            }
            List keyframes2 = this.mKeyframes.getKeyframes();
            int size2 = keyframes2 == null ? 0 : keyframes2.size();
            for (int i2 = 0; i2 < size2; i2++) {
                Keyframe keyframe2 = (Keyframe) keyframes2.get(i2);
                if (!keyframe2.hasValue() || keyframe2.valueWasSetOnStart()) {
                    if (this.mGetter == null) {
                        setupGetter(cls);
                        if (this.mGetter == null) {
                            return;
                        }
                    }
                    try {
                        keyframe2.setValue(convertBack(this.mGetter.invoke(obj, new Object[0])));
                        keyframe2.setValueWasSetOnStart(true);
                    } catch (InvocationTargetException e) {
                        Log.e("PropertyValuesHolder", e.toString());
                    } catch (IllegalAccessException e2) {
                        Log.e("PropertyValuesHolder", e2.toString());
                    }
                }
            }
        }
    }

    private Object convertBack(Object obj) {
        TypeConverter typeConverter = this.mConverter;
        if (typeConverter == null) {
            return obj;
        }
        if (typeConverter instanceof BidirectionalTypeConverter) {
            return ((BidirectionalTypeConverter) typeConverter).convertBack(obj);
        }
        throw new IllegalArgumentException("Converter " + this.mConverter.getClass().getName() + " must be a BidirectionalTypeConverter");
    }

    @Override // java.lang.Object
    public PropertyValuesHolder clone() {
        try {
            PropertyValuesHolder propertyValuesHolder = (PropertyValuesHolder) super.clone();
            propertyValuesHolder.mPropertyName = this.mPropertyName;
            propertyValuesHolder.mProperty = this.mProperty;
            propertyValuesHolder.mKeyframes = this.mKeyframes.clone();
            propertyValuesHolder.mEvaluator = this.mEvaluator;
            return propertyValuesHolder;
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void setAnimatedValue(Object obj) {
        Property property = this.mProperty;
        if (property != null) {
            property.set(obj, getAnimatedValue());
        }
        if (this.mSetter != null) {
            try {
                this.mTmpValueArray[0] = getAnimatedValue();
                this.mSetter.invoke(obj, this.mTmpValueArray);
            } catch (InvocationTargetException e) {
                Log.e("PropertyValuesHolder", e.toString());
            } catch (IllegalAccessException e2) {
                Log.e("PropertyValuesHolder", e2.toString());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void init() {
        if (this.mEvaluator == null) {
            Class<?> cls = this.mValueType;
            this.mEvaluator = cls == Integer.class ? sIntEvaluator : cls == Float.class ? sFloatEvaluator : null;
        }
        TypeEvaluator typeEvaluator = this.mEvaluator;
        if (typeEvaluator != null) {
            this.mKeyframes.setEvaluator(typeEvaluator);
        }
    }

    /* access modifiers changed from: package-private */
    public void calculateValue(float f) {
        Object value = this.mKeyframes.getValue(f);
        TypeConverter typeConverter = this.mConverter;
        if (typeConverter != null) {
            value = typeConverter.convert(value);
        }
        this.mAnimatedValue = value;
    }

    public void setPropertyName(String str) {
        this.mPropertyName = str;
    }

    public String getPropertyName() {
        return this.mPropertyName;
    }

    /* access modifiers changed from: package-private */
    public Object getAnimatedValue() {
        return this.mAnimatedValue;
    }

    @Override // java.lang.Object
    public String toString() {
        return this.mPropertyName + ": " + this.mKeyframes.toString();
    }

    static String getMethodName(String str, String str2) {
        if (str2 == null || str2.length() == 0) {
            return str;
        }
        char upperCase = Character.toUpperCase(str2.charAt(0));
        String substring = str2.substring(1);
        return str + upperCase + substring;
    }

    /* access modifiers changed from: package-private */
    public static class FloatPropertyValuesHolder extends PropertyValuesHolder {
        float mFloatAnimatedValue;
        Keyframes.FloatKeyframes mFloatKeyframes;
        private FloatProperty mFloatProperty;

        FloatPropertyValuesHolder(String str, float... fArr) {
            super(str);
            setFloatValues(fArr);
        }

        FloatPropertyValuesHolder(Property property, float... fArr) {
            super(property);
            setFloatValues(fArr);
            if (property instanceof FloatProperty) {
                this.mFloatProperty = (FloatProperty) this.mProperty;
            }
        }

        @Override // androidx.animation.PropertyValuesHolder
        public void setFloatValues(float... fArr) {
            PropertyValuesHolder.super.setFloatValues(fArr);
            this.mFloatKeyframes = (Keyframes.FloatKeyframes) this.mKeyframes;
        }

        /* access modifiers changed from: package-private */
        @Override // androidx.animation.PropertyValuesHolder
        public void calculateValue(float f) {
            this.mFloatAnimatedValue = this.mFloatKeyframes.getFloatValue(f);
        }

        /* access modifiers changed from: package-private */
        @Override // androidx.animation.PropertyValuesHolder
        public Object getAnimatedValue() {
            return Float.valueOf(this.mFloatAnimatedValue);
        }

        @Override // androidx.animation.PropertyValuesHolder, java.lang.Object
        public FloatPropertyValuesHolder clone() {
            FloatPropertyValuesHolder floatPropertyValuesHolder = (FloatPropertyValuesHolder) PropertyValuesHolder.super.clone();
            floatPropertyValuesHolder.mFloatKeyframes = (Keyframes.FloatKeyframes) floatPropertyValuesHolder.mKeyframes;
            return floatPropertyValuesHolder;
        }

        /* access modifiers changed from: package-private */
        @Override // androidx.animation.PropertyValuesHolder
        public void setAnimatedValue(Object obj) {
            FloatProperty floatProperty = this.mFloatProperty;
            if (floatProperty != null) {
                floatProperty.setValue(obj, this.mFloatAnimatedValue);
                return;
            }
            Property property = this.mProperty;
            if (property != null) {
                property.set(obj, Float.valueOf(this.mFloatAnimatedValue));
            } else if (this.mSetter != null) {
                try {
                    this.mTmpValueArray[0] = Float.valueOf(this.mFloatAnimatedValue);
                    this.mSetter.invoke(obj, this.mTmpValueArray);
                } catch (InvocationTargetException e) {
                    Log.e("PropertyValuesHolder", e.toString());
                } catch (IllegalAccessException e2) {
                    Log.e("PropertyValuesHolder", e2.toString());
                }
            }
        }
    }
}
