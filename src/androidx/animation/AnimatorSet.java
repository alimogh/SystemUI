package androidx.animation;

import android.os.Looper;
import android.util.AndroidRuntimeException;
import android.util.Log;
import androidx.animation.AnimationHandler;
import androidx.animation.Animator;
import androidx.collection.ArrayMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
public final class AnimatorSet extends Animator implements AnimationHandler.AnimationFrameCallback {
    private static final Comparator<AnimationEvent> EVENT_COMPARATOR = new Comparator<AnimationEvent>() { // from class: androidx.animation.AnimatorSet.3
        public int compare(AnimationEvent animationEvent, AnimationEvent animationEvent2) {
            long time = animationEvent.getTime();
            long time2 = animationEvent2.getTime();
            if (time == time2) {
                int i = animationEvent2.mEvent;
                int i2 = animationEvent.mEvent;
                return i + i2 == 1 ? i2 - i : i - i2;
            } else if (time2 == -1) {
                return -1;
            } else {
                if (time != -1 && time - time2 <= 0) {
                    return -1;
                }
                return 1;
            }
        }
    };
    private boolean mChildrenInitialized;
    private ValueAnimator mDelayAnim;
    boolean mDependencyDirty = false;
    private AnimatorListenerAdapter mDummyListener;
    private long mDuration;
    private ArrayList<AnimationEvent> mEvents = new ArrayList<>();
    private long mFirstFrame;
    private Interpolator mInterpolator;
    private int mLastEventId;
    private long mLastFrameTime;
    ArrayMap<Animator, Node> mNodeMap = new ArrayMap<>();
    private ArrayList<Node> mNodes = new ArrayList<>();
    private long mPauseTime;
    private ArrayList<Node> mPlayingSet = new ArrayList<>();
    boolean mReversing;
    private Node mRootNode;
    private SeekState mSeekState;
    private boolean mSelfPulse;
    long mStartDelay = 0;
    private boolean mStarted = false;
    private long mTotalDuration;

    public AnimatorSet() {
        ValueAnimator duration = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(0L);
        this.mDelayAnim = duration;
        this.mRootNode = new Node(duration);
        this.mDuration = -1;
        this.mInterpolator = null;
        this.mTotalDuration = 0;
        this.mLastFrameTime = -1;
        this.mFirstFrame = -1;
        this.mLastEventId = -1;
        this.mReversing = false;
        this.mSelfPulse = true;
        this.mSeekState = new SeekState();
        this.mChildrenInitialized = false;
        this.mPauseTime = -1;
        this.mDummyListener = new AnimatorListenerAdapter() { // from class: androidx.animation.AnimatorSet.1
            @Override // androidx.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (AnimatorSet.this.mNodeMap.get(animator) != null) {
                    AnimatorSet.this.mNodeMap.get(animator).mEnded = true;
                    return;
                }
                throw new AndroidRuntimeException("Error: animation ended is not in the node map");
            }
        };
        this.mNodeMap.put(this.mDelayAnim, this.mRootNode);
        this.mNodes.add(this.mRootNode);
    }

    public void playTogether(Animator... animatorArr) {
        if (animatorArr != null) {
            Builder play = play(animatorArr[0]);
            for (int i = 1; i < animatorArr.length; i++) {
                play.with(animatorArr[i]);
            }
        }
    }

    public ArrayList<Animator> getChildAnimations() {
        ArrayList<Animator> arrayList = new ArrayList<>();
        int size = this.mNodes.size();
        for (int i = 0; i < size; i++) {
            Node node = this.mNodes.get(i);
            if (node != this.mRootNode) {
                arrayList.add(node.mAnimation);
            }
        }
        return arrayList;
    }

    @Override // androidx.animation.Animator
    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public Builder play(Animator animator) {
        return new Builder(animator);
    }

    @Override // androidx.animation.Animator
    public void cancel() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        } else if (isStarted()) {
            ArrayList<Animator.AnimatorListener> arrayList = this.mListeners;
            if (arrayList != null) {
                ArrayList arrayList2 = (ArrayList) arrayList.clone();
                int size = arrayList2.size();
                for (int i = 0; i < size; i++) {
                    ((Animator.AnimatorListener) arrayList2.get(i)).onAnimationCancel(this);
                }
            }
            ArrayList arrayList3 = new ArrayList(this.mPlayingSet);
            int size2 = arrayList3.size();
            for (int i2 = 0; i2 < size2; i2++) {
                ((Node) arrayList3.get(i2)).mAnimation.cancel();
            }
            this.mPlayingSet.clear();
            endAnimation();
        }
    }

    @Override // androidx.animation.Animator
    public void end() {
        if (Looper.myLooper() != null) {
            if (isStarted()) {
                if (this.mReversing) {
                    int i = this.mLastEventId;
                    if (i == -1) {
                        i = this.mEvents.size();
                    }
                    this.mLastEventId = i;
                    while (true) {
                        int i2 = this.mLastEventId;
                        if (i2 <= 0) {
                            break;
                        }
                        int i3 = i2 - 1;
                        this.mLastEventId = i3;
                        AnimationEvent animationEvent = this.mEvents.get(i3);
                        Animator animator = animationEvent.mNode.mAnimation;
                        if (!this.mNodeMap.get(animator).mEnded) {
                            int i4 = animationEvent.mEvent;
                            if (i4 == 2) {
                                animator.reverse();
                            } else if (i4 == 1 && animator.isStarted()) {
                                animator.end();
                            }
                        }
                    }
                } else {
                    while (this.mLastEventId < this.mEvents.size() - 1) {
                        int i5 = this.mLastEventId + 1;
                        this.mLastEventId = i5;
                        AnimationEvent animationEvent2 = this.mEvents.get(i5);
                        Animator animator2 = animationEvent2.mNode.mAnimation;
                        if (!this.mNodeMap.get(animator2).mEnded) {
                            int i6 = animationEvent2.mEvent;
                            if (i6 == 0) {
                                animator2.start();
                            } else if (i6 == 2 && animator2.isStarted()) {
                                animator2.end();
                            }
                        }
                    }
                }
                this.mPlayingSet.clear();
            }
            endAnimation();
            return;
        }
        throw new AndroidRuntimeException("Animators may only be run on Looper threads");
    }

    @Override // androidx.animation.Animator
    public boolean isRunning() {
        if (this.mStartDelay == 0) {
            return this.mStarted;
        }
        return this.mLastFrameTime > 0;
    }

    @Override // androidx.animation.Animator
    public boolean isStarted() {
        return this.mStarted;
    }

    @Override // androidx.animation.Animator
    public long getStartDelay() {
        return this.mStartDelay;
    }

    @Override // androidx.animation.Animator
    public long getDuration() {
        return this.mDuration;
    }

    @Override // androidx.animation.Animator
    public AnimatorSet setDuration(long j) {
        if (j >= 0) {
            this.mDependencyDirty = true;
            this.mDuration = j;
            return this;
        }
        throw new IllegalArgumentException("duration must be a value of zero or greater");
    }

    @Override // androidx.animation.Animator
    public void start() {
        start(false, true);
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.animation.Animator
    public void startWithoutPulsing(boolean z) {
        start(z, false);
    }

    private void initAnimation() {
        if (this.mInterpolator != null) {
            for (int i = 0; i < this.mNodes.size(); i++) {
                this.mNodes.get(i).mAnimation.setInterpolator(this.mInterpolator);
            }
        }
        updateAnimatorsDuration();
        createDependencyGraph();
    }

    private void start(boolean z, boolean z2) {
        if (Looper.myLooper() != null) {
            this.mStarted = true;
            this.mSelfPulse = z2;
            this.mPaused = false;
            this.mPauseTime = -1;
            int size = this.mNodes.size();
            for (int i = 0; i < size; i++) {
                this.mNodes.get(i).mEnded = false;
            }
            initAnimation();
            if (!z || canReverse()) {
                this.mReversing = z;
                boolean isEmptySet = isEmptySet(this);
                if (!isEmptySet) {
                    startAnimation();
                }
                ArrayList<Animator.AnimatorListener> arrayList = this.mListeners;
                if (arrayList != null) {
                    ArrayList arrayList2 = (ArrayList) arrayList.clone();
                    int size2 = arrayList2.size();
                    for (int i2 = 0; i2 < size2; i2++) {
                        ((Animator.AnimatorListener) arrayList2.get(i2)).onAnimationStart(this, z);
                    }
                }
                if (isEmptySet) {
                    end();
                    return;
                }
                return;
            }
            throw new UnsupportedOperationException("Cannot reverse infinite AnimatorSet");
        }
        throw new AndroidRuntimeException("Animators may only be run on Looper threads");
    }

    private static boolean isEmptySet(AnimatorSet animatorSet) {
        if (animatorSet.getStartDelay() > 0) {
            return false;
        }
        for (int i = 0; i < animatorSet.getChildAnimations().size(); i++) {
            Animator animator = animatorSet.getChildAnimations().get(i);
            if (!((animator instanceof AnimatorSet) && isEmptySet((AnimatorSet) animator))) {
                return false;
            }
        }
        return true;
    }

    private void updateAnimatorsDuration() {
        if (this.mDuration >= 0) {
            int size = this.mNodes.size();
            for (int i = 0; i < size; i++) {
                this.mNodes.get(i).mAnimation.setDuration(this.mDuration);
            }
        }
        this.mDelayAnim.setDuration(this.mStartDelay);
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.animation.Animator
    public void skipToEndValue(boolean z) {
        if (isInitialized()) {
            initAnimation();
            if (z) {
                for (int size = this.mEvents.size() - 1; size >= 0; size--) {
                    if (this.mEvents.get(size).mEvent == 1) {
                        this.mEvents.get(size).mNode.mAnimation.skipToEndValue(true);
                    }
                }
                return;
            }
            for (int i = 0; i < this.mEvents.size(); i++) {
                if (this.mEvents.get(i).mEvent == 2) {
                    this.mEvents.get(i).mNode.mAnimation.skipToEndValue(false);
                }
            }
            return;
        }
        throw new UnsupportedOperationException("Children must be initialized.");
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.animation.Animator
    public boolean isInitialized() {
        boolean z = true;
        if (this.mChildrenInitialized) {
            return true;
        }
        int i = 0;
        while (true) {
            if (i >= this.mNodes.size()) {
                break;
            } else if (!this.mNodes.get(i).mAnimation.isInitialized()) {
                z = false;
                break;
            } else {
                i++;
            }
        }
        this.mChildrenInitialized = z;
        return z;
    }

    private void initChildren() {
        if (!isInitialized()) {
            this.mChildrenInitialized = true;
            skipToEndValue(false);
        }
    }

    @Override // androidx.animation.AnimationHandler.AnimationFrameCallback
    public boolean doAnimationFrame(long j) {
        float durationScale = ValueAnimator.getDurationScale();
        if (durationScale == 0.0f) {
            end();
            return true;
        }
        if (this.mFirstFrame < 0) {
            this.mFirstFrame = j;
        }
        if (this.mPaused) {
            if (this.mPauseTime == -1) {
                this.mPauseTime = j;
            }
            removeAnimationCallback();
            return false;
        }
        long j2 = this.mPauseTime;
        if (j2 > 0) {
            this.mFirstFrame += j - j2;
            this.mPauseTime = -1;
        }
        if (this.mSeekState.isActive()) {
            this.mSeekState.updateSeekDirection(this.mReversing);
            if (this.mReversing) {
                this.mFirstFrame = j - ((long) (((float) this.mSeekState.getPlayTime()) * durationScale));
            } else {
                this.mFirstFrame = j - ((long) (((float) (this.mSeekState.getPlayTime() + this.mStartDelay)) * durationScale));
            }
            this.mSeekState.reset();
        }
        if (!this.mReversing && j < this.mFirstFrame + ((long) (((float) this.mStartDelay) * durationScale))) {
            return false;
        }
        long j3 = (long) (((float) (j - this.mFirstFrame)) / durationScale);
        this.mLastFrameTime = j;
        int findLatestEventIdForTime = findLatestEventIdForTime(j3);
        handleAnimationEvents(this.mLastEventId, findLatestEventIdForTime, j3);
        this.mLastEventId = findLatestEventIdForTime;
        for (int i = 0; i < this.mPlayingSet.size(); i++) {
            Node node = this.mPlayingSet.get(i);
            if (!node.mEnded) {
                pulseFrame(node, getPlayTimeForNode(j3, node));
            }
        }
        for (int size = this.mPlayingSet.size() - 1; size >= 0; size--) {
            if (this.mPlayingSet.get(size).mEnded) {
                this.mPlayingSet.remove(size);
            }
        }
        boolean z = !this.mReversing ? !(!this.mPlayingSet.isEmpty() || this.mLastEventId != this.mEvents.size() - 1) : !(!(this.mPlayingSet.size() == 1 && this.mPlayingSet.get(0) == this.mRootNode) && (!this.mPlayingSet.isEmpty() || this.mLastEventId >= 3));
        notifyUpdateListeners();
        if (!z) {
            return false;
        }
        endAnimation();
        return true;
    }

    private void notifyUpdateListeners() {
        if (this.mUpdateListeners != null) {
            for (int i = 0; i < this.mUpdateListeners.size(); i++) {
                this.mUpdateListeners.get(i).onAnimationUpdate(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.animation.Animator
    public boolean pulseAnimationFrame(long j) {
        return doAnimationFrame(j);
    }

    private void handleAnimationEvents(int i, int i2, long j) {
        if (this.mReversing) {
            if (i == -1) {
                i = this.mEvents.size();
            }
            for (int i3 = i - 1; i3 >= i2; i3--) {
                AnimationEvent animationEvent = this.mEvents.get(i3);
                Node node = animationEvent.mNode;
                int i4 = animationEvent.mEvent;
                if (i4 == 2) {
                    if (node.mAnimation.isStarted()) {
                        node.mAnimation.cancel();
                    }
                    node.mEnded = false;
                    this.mPlayingSet.add(animationEvent.mNode);
                    node.mAnimation.startWithoutPulsing(true);
                    pulseFrame(node, 0);
                } else if (i4 == 1 && !node.mEnded) {
                    pulseFrame(node, getPlayTimeForNode(j, node));
                }
            }
            return;
        }
        for (int i5 = i + 1; i5 <= i2; i5++) {
            AnimationEvent animationEvent2 = this.mEvents.get(i5);
            Node node2 = animationEvent2.mNode;
            int i6 = animationEvent2.mEvent;
            if (i6 == 0) {
                this.mPlayingSet.add(node2);
                if (node2.mAnimation.isStarted()) {
                    node2.mAnimation.cancel();
                }
                node2.mEnded = false;
                node2.mAnimation.startWithoutPulsing(false);
                pulseFrame(node2, 0);
            } else if (i6 == 2 && !node2.mEnded) {
                pulseFrame(node2, getPlayTimeForNode(j, node2));
            }
        }
    }

    private void pulseFrame(Node node, long j) {
        if (!node.mEnded) {
            float durationScale = ValueAnimator.getDurationScale();
            if (durationScale == 0.0f) {
                durationScale = 1.0f;
            }
            node.mEnded = node.mAnimation.pulseAnimationFrame((long) (((float) j) * durationScale));
        }
    }

    private long getPlayTimeForNode(long j, Node node) {
        return getPlayTimeForNode(j, node, this.mReversing);
    }

    private long getPlayTimeForNode(long j, Node node, boolean z) {
        if (!z) {
            return j - node.mStartTime;
        }
        return node.mEndTime - (getTotalDuration() - j);
    }

    private void startAnimation() {
        addDummyListener();
        long j = 0;
        if (this.mSeekState.getPlayTimeNormalized() == 0 && this.mReversing) {
            this.mSeekState.reset();
        }
        if (isInitialized()) {
            skipToEndValue(!this.mReversing);
        } else if (this.mReversing) {
            initChildren();
            skipToEndValue(!this.mReversing);
        } else {
            for (int size = this.mEvents.size() - 1; size >= 0; size--) {
                if (this.mEvents.get(size).mEvent == 1) {
                    Animator animator = this.mEvents.get(size).mNode.mAnimation;
                    if (animator.isInitialized()) {
                        animator.skipToEndValue(true);
                    }
                }
            }
        }
        if (this.mReversing || this.mStartDelay == 0 || this.mSeekState.isActive()) {
            if (this.mSeekState.isActive()) {
                this.mSeekState.updateSeekDirection(this.mReversing);
                j = this.mSeekState.getPlayTime();
            }
            int findLatestEventIdForTime = findLatestEventIdForTime(j);
            handleAnimationEvents(-1, findLatestEventIdForTime, j);
            for (int size2 = this.mPlayingSet.size() - 1; size2 >= 0; size2--) {
                if (this.mPlayingSet.get(size2).mEnded) {
                    this.mPlayingSet.remove(size2);
                }
            }
            this.mLastEventId = findLatestEventIdForTime;
        }
        if (this.mSelfPulse) {
            Animator.addAnimationCallback(this);
        }
    }

    private void addDummyListener() {
        for (int i = 1; i < this.mNodes.size(); i++) {
            this.mNodes.get(i).mAnimation.addListener(this.mDummyListener);
        }
    }

    private void removeDummyListener() {
        for (int i = 1; i < this.mNodes.size(); i++) {
            this.mNodes.get(i).mAnimation.removeListener(this.mDummyListener);
        }
    }

    private int findLatestEventIdForTime(long j) {
        int size = this.mEvents.size();
        int i = this.mLastEventId;
        if (this.mReversing) {
            long totalDuration = getTotalDuration() - j;
            int i2 = this.mLastEventId;
            if (i2 != -1) {
                size = i2;
            }
            this.mLastEventId = size;
            for (int i3 = size - 1; i3 >= 0; i3--) {
                if (this.mEvents.get(i3).getTime() >= totalDuration) {
                    i = i3;
                }
            }
        } else {
            for (int i4 = i + 1; i4 < size; i4++) {
                AnimationEvent animationEvent = this.mEvents.get(i4);
                if (animationEvent.getTime() != -1 && animationEvent.getTime() <= j) {
                    i = i4;
                }
            }
        }
        return i;
    }

    private void endAnimation() {
        this.mStarted = false;
        this.mLastFrameTime = -1;
        this.mFirstFrame = -1;
        this.mLastEventId = -1;
        this.mPaused = false;
        this.mPauseTime = -1;
        this.mSeekState.reset();
        this.mPlayingSet.clear();
        removeAnimationCallback();
        ArrayList<Animator.AnimatorListener> arrayList = this.mListeners;
        if (arrayList != null) {
            ArrayList arrayList2 = (ArrayList) arrayList.clone();
            int size = arrayList2.size();
            for (int i = 0; i < size; i++) {
                ((Animator.AnimatorListener) arrayList2.get(i)).onAnimationEnd(this, this.mReversing);
            }
        }
        removeDummyListener();
        this.mSelfPulse = true;
        this.mReversing = false;
    }

    private void removeAnimationCallback() {
        if (this.mSelfPulse) {
            AnimationHandler.getInstance().removeCallback(this);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r9v0, resolved type: java.util.ArrayList<androidx.animation.AnimatorSet$Node> */
    /* JADX DEBUG: Multi-variable search result rejected for r9v1, resolved type: java.util.ArrayList<androidx.animation.AnimatorSet$Node> */
    /* JADX DEBUG: Multi-variable search result rejected for r9v2, resolved type: java.util.ArrayList<androidx.animation.AnimatorSet$Node> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // androidx.animation.Animator, java.lang.Object
    public AnimatorSet clone() {
        Node node;
        final AnimatorSet animatorSet = (AnimatorSet) super.clone();
        int size = this.mNodes.size();
        animatorSet.mStarted = false;
        animatorSet.mLastFrameTime = -1;
        animatorSet.mFirstFrame = -1;
        animatorSet.mLastEventId = -1;
        animatorSet.mPaused = false;
        animatorSet.mPauseTime = -1;
        animatorSet.mSeekState = new SeekState();
        animatorSet.mSelfPulse = true;
        animatorSet.mPlayingSet = new ArrayList<>();
        animatorSet.mNodeMap = new ArrayMap<>();
        animatorSet.mNodes = new ArrayList<>(size);
        animatorSet.mEvents = new ArrayList<>();
        animatorSet.mDummyListener = new AnimatorListenerAdapter(this) { // from class: androidx.animation.AnimatorSet.2
            @Override // androidx.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (animatorSet.mNodeMap.get(animator) != null) {
                    animatorSet.mNodeMap.get(animator).mEnded = true;
                    return;
                }
                throw new AndroidRuntimeException("Error: animation ended is not in the node map");
            }
        };
        animatorSet.mReversing = false;
        animatorSet.mDependencyDirty = true;
        HashMap hashMap = new HashMap(size);
        for (int i = 0; i < size; i++) {
            Node node2 = this.mNodes.get(i);
            Node clone = node2.clone();
            clone.mAnimation.removeListener(this.mDummyListener);
            hashMap.put(node2, clone);
            animatorSet.mNodes.add(clone);
            animatorSet.mNodeMap.put(clone.mAnimation, clone);
        }
        Node node3 = (Node) hashMap.get(this.mRootNode);
        animatorSet.mRootNode = node3;
        animatorSet.mDelayAnim = (ValueAnimator) node3.mAnimation;
        for (int i2 = 0; i2 < size; i2++) {
            Node node4 = this.mNodes.get(i2);
            Node node5 = (Node) hashMap.get(node4);
            Node node6 = node4.mLatestParent;
            if (node6 == null) {
                node = null;
            } else {
                node = (Node) hashMap.get(node6);
            }
            node5.mLatestParent = node;
            ArrayList<Node> arrayList = node4.mChildNodes;
            int size2 = arrayList == null ? 0 : arrayList.size();
            for (int i3 = 0; i3 < size2; i3++) {
                node5.mChildNodes.set(i3, hashMap.get(node4.mChildNodes.get(i3)));
            }
            ArrayList<Node> arrayList2 = node4.mSiblings;
            int size3 = arrayList2 == null ? 0 : arrayList2.size();
            for (int i4 = 0; i4 < size3; i4++) {
                node5.mSiblings.set(i4, hashMap.get(node4.mSiblings.get(i4)));
            }
            ArrayList<Node> arrayList3 = node4.mParents;
            int size4 = arrayList3 == null ? 0 : arrayList3.size();
            for (int i5 = 0; i5 < size4; i5++) {
                node5.mParents.set(i5, hashMap.get(node4.mParents.get(i5)));
            }
        }
        return animatorSet;
    }

    public boolean canReverse() {
        return getTotalDuration() != -1;
    }

    @Override // androidx.animation.Animator
    public void reverse() {
        start(true, true);
    }

    @Override // java.lang.Object
    public String toString() {
        String str = "AnimatorSet@" + Integer.toHexString(hashCode()) + "{";
        for (int i = 0; i < this.mNodes.size(); i++) {
            str = str + "\n    " + this.mNodes.get(i).mAnimation.toString();
        }
        return str + "\n}";
    }

    private void createDependencyGraph() {
        boolean z;
        if (!this.mDependencyDirty) {
            int i = 0;
            while (true) {
                if (i >= this.mNodes.size()) {
                    z = false;
                    break;
                }
                if (this.mNodes.get(i).mTotalDuration != this.mNodes.get(i).mAnimation.getTotalDuration()) {
                    z = true;
                    break;
                }
                i++;
            }
            if (!z) {
                return;
            }
        }
        this.mDependencyDirty = false;
        int size = this.mNodes.size();
        for (int i2 = 0; i2 < size; i2++) {
            this.mNodes.get(i2).mParentsAdded = false;
        }
        for (int i3 = 0; i3 < size; i3++) {
            Node node = this.mNodes.get(i3);
            if (!node.mParentsAdded) {
                node.mParentsAdded = true;
                ArrayList<Node> arrayList = node.mSiblings;
                if (arrayList != null) {
                    findSiblings(node, arrayList);
                    node.mSiblings.remove(node);
                    int size2 = node.mSiblings.size();
                    for (int i4 = 0; i4 < size2; i4++) {
                        node.addParents(node.mSiblings.get(i4).mParents);
                    }
                    for (int i5 = 0; i5 < size2; i5++) {
                        Node node2 = node.mSiblings.get(i5);
                        node2.addParents(node.mParents);
                        node2.mParentsAdded = true;
                    }
                }
            }
        }
        for (int i6 = 0; i6 < size; i6++) {
            Node node3 = this.mNodes.get(i6);
            Node node4 = this.mRootNode;
            if (node3 != node4 && node3.mParents == null) {
                node3.addParent(node4);
            }
        }
        ArrayList<Node> arrayList2 = new ArrayList<>(this.mNodes.size());
        Node node5 = this.mRootNode;
        node5.mStartTime = 0;
        node5.mEndTime = this.mDelayAnim.getDuration();
        updatePlayTime(this.mRootNode, arrayList2);
        sortAnimationEvents();
        ArrayList<AnimationEvent> arrayList3 = this.mEvents;
        this.mTotalDuration = arrayList3.get(arrayList3.size() - 1).getTime();
    }

    private void sortAnimationEvents() {
        boolean z;
        this.mEvents.clear();
        for (int i = 1; i < this.mNodes.size(); i++) {
            Node node = this.mNodes.get(i);
            this.mEvents.add(new AnimationEvent(node, 0));
            this.mEvents.add(new AnimationEvent(node, 1));
            this.mEvents.add(new AnimationEvent(node, 2));
        }
        Collections.sort(this.mEvents, EVENT_COMPARATOR);
        int size = this.mEvents.size();
        int i2 = 0;
        while (i2 < size) {
            AnimationEvent animationEvent = this.mEvents.get(i2);
            if (animationEvent.mEvent == 2) {
                Node node2 = animationEvent.mNode;
                long j = node2.mStartTime;
                long j2 = node2.mEndTime;
                if (j == j2) {
                    z = true;
                } else if (j2 == j + node2.mAnimation.getStartDelay()) {
                    z = false;
                }
                int i3 = i2 + 1;
                int i4 = size;
                int i5 = i4;
                for (int i6 = i3; i6 < size && (i4 >= size || i5 >= size); i6++) {
                    if (this.mEvents.get(i6).mNode == animationEvent.mNode) {
                        if (this.mEvents.get(i6).mEvent == 0) {
                            i4 = i6;
                        } else if (this.mEvents.get(i6).mEvent == 1) {
                            i5 = i6;
                        }
                    }
                }
                if (z && i4 == this.mEvents.size()) {
                    throw new UnsupportedOperationException("Something went wrong, no start isfound after stop for an animation that has the same start and endtime.");
                } else if (i5 != this.mEvents.size()) {
                    if (z) {
                        this.mEvents.add(i2, this.mEvents.remove(i4));
                        i2 = i3;
                    }
                    this.mEvents.add(i2, this.mEvents.remove(i5));
                    i2 += 2;
                } else {
                    throw new UnsupportedOperationException("Something went wrong, no startdelay end is found after stop for an animation");
                }
            }
            i2++;
        }
        if (this.mEvents.isEmpty() || this.mEvents.get(0).mEvent == 0) {
            this.mEvents.add(0, new AnimationEvent(this.mRootNode, 0));
            this.mEvents.add(1, new AnimationEvent(this.mRootNode, 1));
            this.mEvents.add(2, new AnimationEvent(this.mRootNode, 2));
            ArrayList<AnimationEvent> arrayList = this.mEvents;
            if (arrayList.get(arrayList.size() - 1).mEvent != 0) {
                ArrayList<AnimationEvent> arrayList2 = this.mEvents;
                if (arrayList2.get(arrayList2.size() - 1).mEvent != 1) {
                    return;
                }
            }
            throw new UnsupportedOperationException("Something went wrong, the last event is not an end event");
        }
        throw new UnsupportedOperationException("Sorting went bad, the start event should always be at index 0");
    }

    private void updatePlayTime(Node node, ArrayList<Node> arrayList) {
        int i = 0;
        if (node.mChildNodes != null) {
            arrayList.add(node);
            int size = node.mChildNodes.size();
            while (i < size) {
                Node node2 = node.mChildNodes.get(i);
                node2.mTotalDuration = node2.mAnimation.getTotalDuration();
                int indexOf = arrayList.indexOf(node2);
                if (indexOf >= 0) {
                    while (indexOf < arrayList.size()) {
                        arrayList.get(indexOf).mLatestParent = null;
                        arrayList.get(indexOf).mStartTime = -1;
                        arrayList.get(indexOf).mEndTime = -1;
                        indexOf++;
                    }
                    node2.mStartTime = -1;
                    node2.mEndTime = -1;
                    node2.mLatestParent = null;
                    Log.w("AnimatorSet", "Cycle found in AnimatorSet: " + this);
                } else {
                    long j = node2.mStartTime;
                    if (j != -1) {
                        long j2 = node.mEndTime;
                        if (j2 == -1) {
                            node2.mLatestParent = node;
                            node2.mStartTime = -1;
                            node2.mEndTime = -1;
                        } else {
                            if (j2 >= j) {
                                node2.mLatestParent = node;
                                node2.mStartTime = j2;
                            }
                            long j3 = node2.mTotalDuration;
                            node2.mEndTime = j3 == -1 ? -1 : node2.mStartTime + j3;
                        }
                    }
                    updatePlayTime(node2, arrayList);
                }
                i++;
            }
            arrayList.remove(node);
        } else if (node == this.mRootNode) {
            while (i < this.mNodes.size()) {
                Node node3 = this.mNodes.get(i);
                if (node3 != this.mRootNode) {
                    node3.mStartTime = -1;
                    node3.mEndTime = -1;
                }
                i++;
            }
        }
    }

    private void findSiblings(Node node, ArrayList<Node> arrayList) {
        if (!arrayList.contains(node)) {
            arrayList.add(node);
            if (node.mSiblings != null) {
                for (int i = 0; i < node.mSiblings.size(); i++) {
                    findSiblings(node.mSiblings.get(i), arrayList);
                }
            }
        }
    }

    @Override // androidx.animation.Animator
    public long getTotalDuration() {
        updateAnimatorsDuration();
        createDependencyGraph();
        return this.mTotalDuration;
    }

    /* access modifiers changed from: package-private */
    public Node getNodeForAnimation(Animator animator) {
        Node node = this.mNodeMap.get(animator);
        if (node != null) {
            return node;
        }
        Node node2 = new Node(animator);
        this.mNodeMap.put(animator, node2);
        this.mNodes.add(node2);
        return node2;
    }

    /* access modifiers changed from: private */
    public static class Node implements Cloneable {
        Animator mAnimation;
        ArrayList<Node> mChildNodes = null;
        long mEndTime = 0;
        boolean mEnded = false;
        Node mLatestParent = null;
        ArrayList<Node> mParents;
        boolean mParentsAdded = false;
        ArrayList<Node> mSiblings;
        long mStartTime = 0;
        long mTotalDuration = 0;

        Node(Animator animator) {
            this.mAnimation = animator;
        }

        @Override // java.lang.Object
        public Node clone() {
            try {
                Node node = (Node) super.clone();
                node.mAnimation = this.mAnimation.clone();
                if (this.mChildNodes != null) {
                    node.mChildNodes = new ArrayList<>(this.mChildNodes);
                }
                if (this.mSiblings != null) {
                    node.mSiblings = new ArrayList<>(this.mSiblings);
                }
                if (this.mParents != null) {
                    node.mParents = new ArrayList<>(this.mParents);
                }
                node.mEnded = false;
                return node;
            } catch (CloneNotSupportedException unused) {
                throw new AssertionError();
            }
        }

        /* access modifiers changed from: package-private */
        public void addChild(Node node) {
            if (this.mChildNodes == null) {
                this.mChildNodes = new ArrayList<>();
            }
            if (!this.mChildNodes.contains(node)) {
                this.mChildNodes.add(node);
                node.addParent(this);
            }
        }

        public void addSibling(Node node) {
            if (this.mSiblings == null) {
                this.mSiblings = new ArrayList<>();
            }
            if (!this.mSiblings.contains(node)) {
                this.mSiblings.add(node);
                node.addSibling(this);
            }
        }

        public void addParent(Node node) {
            if (this.mParents == null) {
                this.mParents = new ArrayList<>();
            }
            if (!this.mParents.contains(node)) {
                this.mParents.add(node);
                node.addChild(this);
            }
        }

        public void addParents(ArrayList<Node> arrayList) {
            if (arrayList != null) {
                int size = arrayList.size();
                for (int i = 0; i < size; i++) {
                    addParent(arrayList.get(i));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class AnimationEvent {
        final int mEvent;
        final Node mNode;

        AnimationEvent(Node node, int i) {
            this.mNode = node;
            this.mEvent = i;
        }

        /* access modifiers changed from: package-private */
        public long getTime() {
            int i = this.mEvent;
            if (i == 0) {
                return this.mNode.mStartTime;
            }
            if (i != 1) {
                return this.mNode.mEndTime;
            }
            Node node = this.mNode;
            long j = node.mStartTime;
            if (j == -1) {
                return -1;
            }
            return node.mAnimation.getStartDelay() + j;
        }

        public String toString() {
            int i = this.mEvent;
            String str = i == 0 ? "start" : i == 1 ? "delay ended" : "end";
            return str + " " + this.mNode.mAnimation.toString();
        }
    }

    /* access modifiers changed from: package-private */
    public class SeekState {
        private long mPlayTime = -1;
        private boolean mSeekingInReverse = false;

        SeekState() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.mPlayTime = -1;
            this.mSeekingInReverse = false;
        }

        /* access modifiers changed from: package-private */
        public void updateSeekDirection(boolean z) {
            if (z && AnimatorSet.this.getTotalDuration() == -1) {
                throw new UnsupportedOperationException("Error: Cannot reverse infinite animator set");
            } else if (this.mPlayTime >= 0 && z != this.mSeekingInReverse) {
                this.mPlayTime = (AnimatorSet.this.getTotalDuration() - AnimatorSet.this.mStartDelay) - this.mPlayTime;
                this.mSeekingInReverse = z;
            }
        }

        /* access modifiers changed from: package-private */
        public long getPlayTime() {
            return this.mPlayTime;
        }

        /* access modifiers changed from: package-private */
        public long getPlayTimeNormalized() {
            AnimatorSet animatorSet = AnimatorSet.this;
            if (animatorSet.mReversing) {
                return (animatorSet.getTotalDuration() - AnimatorSet.this.mStartDelay) - this.mPlayTime;
            }
            return this.mPlayTime;
        }

        /* access modifiers changed from: package-private */
        public boolean isActive() {
            return this.mPlayTime != -1;
        }
    }

    public class Builder {
        private Node mCurrentNode;

        Builder(Animator animator) {
            AnimatorSet.this.mDependencyDirty = true;
            this.mCurrentNode = AnimatorSet.this.getNodeForAnimation(animator);
        }

        public Builder with(Animator animator) {
            this.mCurrentNode.addSibling(AnimatorSet.this.getNodeForAnimation(animator));
            return this;
        }
    }
}
