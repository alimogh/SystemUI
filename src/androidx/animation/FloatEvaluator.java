package androidx.animation;
public final class FloatEvaluator implements TypeEvaluator<Float> {
    public Float evaluate(float f, Float f2, Float f3) {
        float floatValue = f2.floatValue();
        return Float.valueOf(floatValue + (f * (f3.floatValue() - floatValue)));
    }
}
