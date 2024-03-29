package com.airbnb.lottie.animation.keyframe;

import android.graphics.Path;
import android.graphics.PointF;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.Keyframe;
public class PathKeyframe extends Keyframe<PointF> {
    private Path path;
    private final Keyframe<PointF> pointKeyFrame;

    public PathKeyframe(LottieComposition lottieComposition, Keyframe<PointF> keyframe) {
        super(lottieComposition, keyframe.startValue, keyframe.endValue, keyframe.interpolator, keyframe.startFrame, keyframe.endFrame);
        this.pointKeyFrame = keyframe;
        createPath();
    }

    public void createPath() {
        PointF pointF;
        PointF pointF2 = this.endValue;
        boolean z = (pointF2 == null || (pointF = this.startValue) == null || !pointF.equals(pointF2.x, pointF2.y)) ? false : true;
        PointF pointF3 = this.endValue;
        if (pointF3 != null && !z) {
            Keyframe<PointF> keyframe = this.pointKeyFrame;
            this.path = Utils.createPath(this.startValue, pointF3, keyframe.pathCp1, keyframe.pathCp2);
        }
    }

    /* access modifiers changed from: package-private */
    public Path getPath() {
        return this.path;
    }
}
