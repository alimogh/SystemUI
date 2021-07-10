package androidx.animation;

import java.util.List;
/* access modifiers changed from: package-private */
public interface Keyframes<T> extends Cloneable {

    public interface FloatKeyframes extends Keyframes<Float> {
        float getFloatValue(float f);
    }

    @Override // java.lang.Object
    Keyframes clone();

    List<Keyframe<T>> getKeyframes();

    T getValue(float f);

    void setEvaluator(TypeEvaluator<T> typeEvaluator);
}
