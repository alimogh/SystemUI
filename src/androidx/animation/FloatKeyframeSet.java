package androidx.animation;

import androidx.animation.Keyframe;
import androidx.animation.Keyframes;
import java.util.List;
/* access modifiers changed from: package-private */
public class FloatKeyframeSet extends KeyframeSet<Float> implements Keyframes.FloatKeyframes {
    FloatKeyframeSet(Keyframe.FloatKeyframe... floatKeyframeArr) {
        super(floatKeyframeArr);
    }

    @Override // androidx.animation.Keyframes
    public Float getValue(float f) {
        return Float.valueOf(getFloatValue(f));
    }

    @Override // androidx.animation.Keyframes, java.lang.Object
    public FloatKeyframeSet clone() {
        List<Keyframe<T>> list = this.mKeyframes;
        int size = list.size();
        Keyframe.FloatKeyframe[] floatKeyframeArr = new Keyframe.FloatKeyframe[size];
        for (int i = 0; i < size; i++) {
            floatKeyframeArr[i] = (Keyframe.FloatKeyframe) ((Keyframe) list.get(i)).clone();
        }
        return new FloatKeyframeSet(floatKeyframeArr);
    }

    @Override // androidx.animation.Keyframes.FloatKeyframes
    public float getFloatValue(float f) {
        if (f <= 0.0f) {
            Keyframe.FloatKeyframe floatKeyframe = (Keyframe.FloatKeyframe) this.mKeyframes.get(0);
            Keyframe.FloatKeyframe floatKeyframe2 = (Keyframe.FloatKeyframe) this.mKeyframes.get(1);
            float floatValue = floatKeyframe.getFloatValue();
            float floatValue2 = floatKeyframe2.getFloatValue();
            float fraction = floatKeyframe.getFraction();
            float fraction2 = floatKeyframe2.getFraction();
            Interpolator interpolator = floatKeyframe2.getInterpolator();
            if (interpolator != null) {
                f = interpolator.getInterpolation(f);
            }
            float f2 = (f - fraction) / (fraction2 - fraction);
            TypeEvaluator<T> typeEvaluator = this.mEvaluator;
            if (typeEvaluator == 0) {
                return floatValue + (f2 * (floatValue2 - floatValue));
            }
            return ((Float) typeEvaluator.evaluate(f2, Float.valueOf(floatValue), Float.valueOf(floatValue2))).floatValue();
        } else if (f >= 1.0f) {
            Keyframe.FloatKeyframe floatKeyframe3 = (Keyframe.FloatKeyframe) this.mKeyframes.get(this.mNumKeyframes - 2);
            Keyframe.FloatKeyframe floatKeyframe4 = (Keyframe.FloatKeyframe) this.mKeyframes.get(this.mNumKeyframes - 1);
            float floatValue3 = floatKeyframe3.getFloatValue();
            float floatValue4 = floatKeyframe4.getFloatValue();
            float fraction3 = floatKeyframe3.getFraction();
            float fraction4 = floatKeyframe4.getFraction();
            Interpolator interpolator2 = floatKeyframe4.getInterpolator();
            if (interpolator2 != null) {
                f = interpolator2.getInterpolation(f);
            }
            float f3 = (f - fraction3) / (fraction4 - fraction3);
            TypeEvaluator<T> typeEvaluator2 = this.mEvaluator;
            if (typeEvaluator2 == 0) {
                return floatValue3 + (f3 * (floatValue4 - floatValue3));
            }
            return ((Float) typeEvaluator2.evaluate(f3, Float.valueOf(floatValue3), Float.valueOf(floatValue4))).floatValue();
        } else {
            Keyframe.FloatKeyframe floatKeyframe5 = (Keyframe.FloatKeyframe) this.mKeyframes.get(0);
            int i = 1;
            while (true) {
                int i2 = this.mNumKeyframes;
                if (i >= i2) {
                    return ((Float) ((Keyframe) this.mKeyframes.get(i2 - 1)).getValue()).floatValue();
                }
                Keyframe.FloatKeyframe floatKeyframe6 = (Keyframe.FloatKeyframe) this.mKeyframes.get(i);
                if (f < floatKeyframe6.getFraction()) {
                    Interpolator interpolator3 = floatKeyframe6.getInterpolator();
                    float fraction5 = (f - floatKeyframe5.getFraction()) / (floatKeyframe6.getFraction() - floatKeyframe5.getFraction());
                    float floatValue5 = floatKeyframe5.getFloatValue();
                    float floatValue6 = floatKeyframe6.getFloatValue();
                    if (interpolator3 != null) {
                        fraction5 = interpolator3.getInterpolation(fraction5);
                    }
                    TypeEvaluator<T> typeEvaluator3 = this.mEvaluator;
                    if (typeEvaluator3 == 0) {
                        return floatValue5 + (fraction5 * (floatValue6 - floatValue5));
                    }
                    return ((Float) typeEvaluator3.evaluate(fraction5, Float.valueOf(floatValue5), Float.valueOf(floatValue6))).floatValue();
                }
                i++;
                floatKeyframe5 = floatKeyframe6;
            }
        }
    }
}
