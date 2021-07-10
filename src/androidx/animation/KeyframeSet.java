package androidx.animation;

import android.util.Log;
import androidx.animation.Keyframe;
import java.util.Arrays;
import java.util.List;
/* access modifiers changed from: package-private */
public class KeyframeSet<T> implements Keyframes<T> {
    TypeEvaluator<T> mEvaluator;
    List<Keyframe<T>> mKeyframes;
    Keyframe<T> mLastKeyframe;
    int mNumKeyframes;

    @SafeVarargs
    KeyframeSet(Keyframe<T>... keyframeArr) {
        this.mNumKeyframes = keyframeArr.length;
        this.mKeyframes = Arrays.asList(keyframeArr);
        Keyframe<T> keyframe = keyframeArr[0];
        Keyframe<T> keyframe2 = keyframeArr[this.mNumKeyframes - 1];
        this.mLastKeyframe = keyframe2;
        keyframe2.getInterpolator();
    }

    @Override // androidx.animation.Keyframes
    public List<Keyframe<T>> getKeyframes() {
        return this.mKeyframes;
    }

    static KeyframeSet ofFloat(float... fArr) {
        int length = fArr.length;
        Keyframe.FloatKeyframe[] floatKeyframeArr = new Keyframe.FloatKeyframe[Math.max(length, 2)];
        boolean z = false;
        if (length == 1) {
            floatKeyframeArr[0] = Keyframe.ofFloat(0.0f);
            floatKeyframeArr[1] = Keyframe.ofFloat(1.0f, fArr[0]);
            if (Float.isNaN(fArr[0])) {
                z = true;
            }
        } else {
            floatKeyframeArr[0] = Keyframe.ofFloat(0.0f, fArr[0]);
            for (int i = 1; i < length; i++) {
                floatKeyframeArr[i] = Keyframe.ofFloat(((float) i) / ((float) (length - 1)), fArr[i]);
                if (Float.isNaN(fArr[i])) {
                    z = true;
                }
            }
        }
        if (z) {
            Log.w("Animator", "Bad value (NaN) in float animator");
        }
        return new FloatKeyframeSet(floatKeyframeArr);
    }

    @Override // androidx.animation.Keyframes
    public void setEvaluator(TypeEvaluator<T> typeEvaluator) {
        this.mEvaluator = typeEvaluator;
    }

    @Override // java.lang.Object
    public String toString() {
        String str = " ";
        for (int i = 0; i < this.mNumKeyframes; i++) {
            str = str + ((Object) this.mKeyframes.get(i).getValue()) + "  ";
        }
        return str;
    }
}
