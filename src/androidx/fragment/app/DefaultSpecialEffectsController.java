package androidx.fragment.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import androidx.collection.ArrayMap;
import androidx.core.os.CancellationSignal;
import androidx.core.view.OneShotPreDrawListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewGroupCompat;
import androidx.fragment.app.FragmentAnim;
import androidx.fragment.app.SpecialEffectsController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
class DefaultSpecialEffectsController extends SpecialEffectsController {
    private final HashMap<SpecialEffectsController.Operation, HashSet<CancellationSignal>> mRunningOperations = new HashMap<>();

    DefaultSpecialEffectsController(ViewGroup viewGroup) {
        super(viewGroup);
    }

    private void addCancellationSignal(SpecialEffectsController.Operation operation, CancellationSignal cancellationSignal) {
        if (this.mRunningOperations.get(operation) == null) {
            this.mRunningOperations.put(operation, new HashSet<>());
        }
        this.mRunningOperations.get(operation).add(cancellationSignal);
    }

    /* access modifiers changed from: package-private */
    public void removeCancellationSignal(SpecialEffectsController.Operation operation, CancellationSignal cancellationSignal) {
        HashSet<CancellationSignal> hashSet = this.mRunningOperations.get(operation);
        if (hashSet != null && hashSet.remove(cancellationSignal) && hashSet.isEmpty()) {
            this.mRunningOperations.remove(operation);
            operation.complete();
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelAllSpecialEffects(SpecialEffectsController.Operation operation) {
        HashSet<CancellationSignal> remove = this.mRunningOperations.remove(operation);
        if (remove != null) {
            Iterator<CancellationSignal> it = remove.iterator();
            while (it.hasNext()) {
                it.next().cancel();
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: androidx.fragment.app.DefaultSpecialEffectsController$8  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass8 {
        static final /* synthetic */ int[] $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type;

        static {
            int[] iArr = new int[SpecialEffectsController.Operation.Type.values().length];
            $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type = iArr;
            try {
                iArr[SpecialEffectsController.Operation.Type.HIDE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type[SpecialEffectsController.Operation.Type.REMOVE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type[SpecialEffectsController.Operation.Type.SHOW.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type[SpecialEffectsController.Operation.Type.ADD.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // androidx.fragment.app.SpecialEffectsController
    public void executeOperations(List<SpecialEffectsController.Operation> list, boolean z) {
        SpecialEffectsController.Operation operation = null;
        SpecialEffectsController.Operation operation2 = null;
        for (SpecialEffectsController.Operation operation3 : list) {
            int i = AnonymousClass8.$SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type[operation3.getType().ordinal()];
            if (i == 1 || i == 2) {
                if (operation == null) {
                    operation = operation3;
                }
            } else if (i == 3 || i == 4) {
                operation2 = operation3;
            }
        }
        ArrayList<AnimationInfo> arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        final ArrayList<SpecialEffectsController.Operation> arrayList3 = new ArrayList(list);
        for (final SpecialEffectsController.Operation operation4 : list) {
            CancellationSignal cancellationSignal = new CancellationSignal();
            addCancellationSignal(operation4, cancellationSignal);
            arrayList.add(new AnimationInfo(operation4, cancellationSignal));
            CancellationSignal cancellationSignal2 = new CancellationSignal();
            addCancellationSignal(operation4, cancellationSignal2);
            boolean z2 = false;
            if (z) {
                if (operation4 != operation) {
                    arrayList2.add(new TransitionInfo(operation4, cancellationSignal2, z, z2));
                    operation4.addCompletionListener(new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.1
                        @Override // java.lang.Runnable
                        public void run() {
                            if (arrayList3.contains(operation4)) {
                                arrayList3.remove(operation4);
                                DefaultSpecialEffectsController.this.applyContainerChanges(operation4);
                            }
                        }
                    });
                    operation4.getCancellationSignal().setOnCancelListener(new CancellationSignal.OnCancelListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.2
                        @Override // androidx.core.os.CancellationSignal.OnCancelListener
                        public void onCancel() {
                            DefaultSpecialEffectsController.this.cancelAllSpecialEffects(operation4);
                        }
                    });
                }
            } else if (operation4 != operation2) {
                arrayList2.add(new TransitionInfo(operation4, cancellationSignal2, z, z2));
                operation4.addCompletionListener(new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.1
                    @Override // java.lang.Runnable
                    public void run() {
                        if (arrayList3.contains(operation4)) {
                            arrayList3.remove(operation4);
                            DefaultSpecialEffectsController.this.applyContainerChanges(operation4);
                        }
                    }
                });
                operation4.getCancellationSignal().setOnCancelListener(new CancellationSignal.OnCancelListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.2
                    @Override // androidx.core.os.CancellationSignal.OnCancelListener
                    public void onCancel() {
                        DefaultSpecialEffectsController.this.cancelAllSpecialEffects(operation4);
                    }
                });
            }
            z2 = true;
            arrayList2.add(new TransitionInfo(operation4, cancellationSignal2, z, z2));
            operation4.addCompletionListener(new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.1
                @Override // java.lang.Runnable
                public void run() {
                    if (arrayList3.contains(operation4)) {
                        arrayList3.remove(operation4);
                        DefaultSpecialEffectsController.this.applyContainerChanges(operation4);
                    }
                }
            });
            operation4.getCancellationSignal().setOnCancelListener(new CancellationSignal.OnCancelListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.2
                @Override // androidx.core.os.CancellationSignal.OnCancelListener
                public void onCancel() {
                    DefaultSpecialEffectsController.this.cancelAllSpecialEffects(operation4);
                }
            });
        }
        startTransitions(arrayList2, z, operation, operation2);
        for (AnimationInfo animationInfo : arrayList) {
            startAnimation(animationInfo.getOperation(), animationInfo.getSignal());
        }
        for (SpecialEffectsController.Operation operation5 : arrayList3) {
            applyContainerChanges(operation5);
        }
        arrayList3.clear();
    }

    private void startAnimation(final SpecialEffectsController.Operation operation, final CancellationSignal cancellationSignal) {
        Animation animation;
        final ViewGroup container = getContainer();
        Context context = container.getContext();
        Fragment fragment = operation.getFragment();
        final View view = fragment.mView;
        FragmentAnim.AnimationOrAnimator loadAnimation = FragmentAnim.loadAnimation(context, fragment, operation.getType() == SpecialEffectsController.Operation.Type.ADD || operation.getType() == SpecialEffectsController.Operation.Type.SHOW);
        if (loadAnimation == null) {
            removeCancellationSignal(operation, cancellationSignal);
            return;
        }
        container.startViewTransition(view);
        if (loadAnimation.animation != null) {
            if (operation.getType() == SpecialEffectsController.Operation.Type.ADD || operation.getType() == SpecialEffectsController.Operation.Type.SHOW) {
                animation = new FragmentAnim.EnterViewTransitionAnimation(loadAnimation.animation);
            } else {
                animation = new FragmentAnim.EndViewTransitionAnimation(loadAnimation.animation, container, view);
            }
            animation.setAnimationListener(new Animation.AnimationListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.3
                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationRepeat(Animation animation2) {
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationStart(Animation animation2) {
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationEnd(Animation animation2) {
                    container.post(new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.3.1
                        @Override // java.lang.Runnable
                        public void run() {
                            AnonymousClass3 r0 = AnonymousClass3.this;
                            container.endViewTransition(view);
                            AnonymousClass3 r2 = AnonymousClass3.this;
                            DefaultSpecialEffectsController.this.removeCancellationSignal(operation, cancellationSignal);
                        }
                    });
                }
            });
            view.startAnimation(animation);
        } else {
            loadAnimation.animator.addListener(new AnimatorListenerAdapter() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.4
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    container.endViewTransition(view);
                    DefaultSpecialEffectsController.this.removeCancellationSignal(operation, cancellationSignal);
                }
            });
            loadAnimation.animator.setTarget(view);
            loadAnimation.animator.start();
        }
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener(this) { // from class: androidx.fragment.app.DefaultSpecialEffectsController.5
            @Override // androidx.core.os.CancellationSignal.OnCancelListener
            public void onCancel() {
                view.clearAnimation();
            }
        });
    }

    private void startTransitions(List<TransitionInfo> list, boolean z, SpecialEffectsController.Operation operation, SpecialEffectsController.Operation operation2) {
        Object obj;
        View view;
        Iterator<TransitionInfo> it;
        ArrayList<String> arrayList;
        ArrayList<String> arrayList2;
        V v;
        final View view2;
        SpecialEffectsController.Operation operation3 = operation2;
        final FragmentTransitionImpl fragmentTransitionImpl = null;
        for (TransitionInfo transitionInfo : list) {
            FragmentTransitionImpl handlingImpl = transitionInfo.getHandlingImpl();
            if (fragmentTransitionImpl == null) {
                fragmentTransitionImpl = handlingImpl;
            } else if (!(handlingImpl == null || fragmentTransitionImpl == handlingImpl)) {
                throw new IllegalArgumentException("Mixing framework transitions and AndroidX transitions is not allowed. Fragment " + transitionInfo.getOperation().getFragment() + " returned Transition " + transitionInfo.getTransition() + " which uses a different Transition  type than other Fragments.");
            }
        }
        if (fragmentTransitionImpl == null) {
            for (TransitionInfo transitionInfo2 : list) {
                removeCancellationSignal(transitionInfo2.getOperation(), transitionInfo2.getSignal());
            }
            return;
        }
        View view3 = new View(getContainer().getContext());
        final Rect rect = new Rect();
        ArrayList<View> arrayList3 = new ArrayList<>();
        ArrayList<View> arrayList4 = new ArrayList<>();
        ArrayMap arrayMap = new ArrayMap();
        Iterator<TransitionInfo> it2 = list.iterator();
        View view4 = null;
        boolean z2 = false;
        Object obj2 = null;
        while (it2.hasNext()) {
            TransitionInfo next = it2.next();
            if (!next.hasSharedElementTransition() || operation == null || operation3 == null) {
                it = it2;
                view4 = view4;
            } else {
                obj2 = fragmentTransitionImpl.wrapTransitionInSet(fragmentTransitionImpl.cloneTransition(next.getSharedElementTransition()));
                Fragment fragment = next.getOperation().getFragment();
                if (!z) {
                    arrayList = fragment.getSharedElementSourceNames();
                    arrayList2 = fragment.getSharedElementTargetNames();
                } else {
                    arrayList = fragment.getSharedElementTargetNames();
                    arrayList2 = fragment.getSharedElementSourceNames();
                }
                int size = arrayList.size();
                int i = 0;
                while (i < size) {
                    arrayMap.put(arrayList.get(i), arrayList2.get(i));
                    i++;
                    arrayList = arrayList;
                    size = size;
                    it2 = it2;
                    view4 = view4;
                }
                it = it2;
                ArrayMap arrayMap2 = new ArrayMap();
                findNamedViews(arrayMap2, operation.getFragment().mView);
                arrayMap2.retainAll(arrayList);
                arrayMap.retainAll(arrayMap2.keySet());
                ArrayMap arrayMap3 = new ArrayMap();
                findNamedViews(arrayMap3, operation2.getFragment().mView);
                arrayMap3.retainAll(arrayList2);
                FragmentTransition.retainValues(arrayMap, arrayMap3);
                arrayMap2.retainAll(arrayMap.keySet());
                arrayMap3.retainAll(arrayMap.values());
                if (arrayMap.isEmpty()) {
                    arrayList3.clear();
                    arrayList4.clear();
                    view4 = view4;
                    obj2 = null;
                } else {
                    for (Iterator it3 = arrayMap2.values().iterator(); it3.hasNext(); it3 = it3) {
                        captureTransitioningViews(arrayList3, (View) it3.next());
                    }
                    if (!arrayList.isEmpty()) {
                        v = arrayMap2.get(arrayList.get(0));
                        fragmentTransitionImpl.setEpicenter(obj2, (View) v);
                    } else {
                        v = view4;
                    }
                    for (View view5 : arrayMap3.values()) {
                        captureTransitioningViews(arrayList4, view5);
                    }
                    if (!arrayList2.isEmpty() && (view2 = arrayMap3.get(arrayList2.get(0))) != null) {
                        OneShotPreDrawListener.add(getContainer(), new Runnable(this) { // from class: androidx.fragment.app.DefaultSpecialEffectsController.6
                            @Override // java.lang.Runnable
                            public void run() {
                                fragmentTransitionImpl.getBoundsOnScreen(view2, rect);
                            }
                        });
                        z2 = true;
                    }
                    fragmentTransitionImpl.addTargets(obj2, arrayList3);
                    view4 = v;
                }
            }
            it2 = it;
        }
        View view6 = view4;
        ArrayList arrayList5 = new ArrayList();
        Iterator<TransitionInfo> it4 = list.iterator();
        Object obj3 = null;
        Object obj4 = null;
        while (it4.hasNext()) {
            TransitionInfo next2 = it4.next();
            Object cloneTransition = fragmentTransitionImpl.cloneTransition(next2.getTransition());
            SpecialEffectsController.Operation operation4 = next2.getOperation();
            boolean z3 = obj2 != null && (operation4 == operation || operation4 == operation3);
            if (cloneTransition == null) {
                if (!z3) {
                    removeCancellationSignal(next2.getOperation(), next2.getSignal());
                }
                obj = obj2;
                view = view6;
            } else {
                ArrayList<View> arrayList6 = new ArrayList<>();
                obj = obj2;
                captureTransitioningViews(arrayList6, next2.getOperation().getFragment().mView);
                if (z3) {
                    if (operation4 == operation) {
                        arrayList6.removeAll(arrayList3);
                    } else {
                        arrayList6.removeAll(arrayList4);
                    }
                }
                if (arrayList6.isEmpty()) {
                    fragmentTransitionImpl.addTarget(cloneTransition, view3);
                } else {
                    fragmentTransitionImpl.addTargets(cloneTransition, arrayList6);
                }
                if (next2.getOperation().getType().equals(SpecialEffectsController.Operation.Type.ADD)) {
                    arrayList5.addAll(arrayList6);
                    if (z2) {
                        fragmentTransitionImpl.setEpicenter(cloneTransition, rect);
                    }
                    view = view6;
                } else {
                    view = view6;
                    fragmentTransitionImpl.setEpicenter(cloneTransition, view);
                }
                if (next2.isOverlapAllowed()) {
                    obj3 = fragmentTransitionImpl.mergeTransitionsTogether(obj3, cloneTransition, null);
                } else {
                    obj4 = fragmentTransitionImpl.mergeTransitionsTogether(obj4, cloneTransition, null);
                }
            }
            it4 = it4;
            view6 = view;
            arrayMap = arrayMap;
            obj2 = obj;
            operation3 = operation2;
        }
        Object mergeTransitionsInSequence = fragmentTransitionImpl.mergeTransitionsInSequence(obj3, obj4, obj2);
        for (final TransitionInfo transitionInfo3 : list) {
            if (transitionInfo3.getTransition() != null) {
                fragmentTransitionImpl.setListenerForTransitionEnd(transitionInfo3.getOperation().getFragment(), mergeTransitionsInSequence, transitionInfo3.getSignal(), new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.7
                    @Override // java.lang.Runnable
                    public void run() {
                        DefaultSpecialEffectsController.this.removeCancellationSignal(transitionInfo3.getOperation(), transitionInfo3.getSignal());
                    }
                });
            }
        }
        FragmentTransition.setViewVisibility(arrayList5, 4);
        ArrayList<String> prepareSetNameOverridesReordered = fragmentTransitionImpl.prepareSetNameOverridesReordered(arrayList4);
        fragmentTransitionImpl.beginDelayedTransition(getContainer(), mergeTransitionsInSequence);
        fragmentTransitionImpl.setNameOverridesReordered(getContainer(), arrayList3, arrayList4, prepareSetNameOverridesReordered, arrayMap);
        FragmentTransition.setViewVisibility(arrayList5, 0);
        fragmentTransitionImpl.swapSharedElementTargets(obj2, arrayList3, arrayList4);
    }

    /* access modifiers changed from: package-private */
    public void captureTransitioningViews(ArrayList<View> arrayList, View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (ViewGroupCompat.isTransitionGroup(viewGroup)) {
                arrayList.add(viewGroup);
                return;
            }
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt.getVisibility() == 0) {
                    captureTransitioningViews(arrayList, childAt);
                }
            }
            return;
        }
        arrayList.add(view);
    }

    /* access modifiers changed from: package-private */
    public void findNamedViews(Map<String, View> map, View view) {
        String transitionName = ViewCompat.getTransitionName(view);
        if (transitionName != null) {
            map.put(transitionName, view);
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt.getVisibility() == 0) {
                    findNamedViews(map, childAt);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void applyContainerChanges(SpecialEffectsController.Operation operation) {
        View view = operation.getFragment().mView;
        int i = AnonymousClass8.$SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type[operation.getType().ordinal()];
        if (i == 1) {
            view.setVisibility(8);
        } else if (i == 2) {
            getContainer().removeView(view);
        } else if (i == 3 || i == 4) {
            view.setVisibility(0);
        }
    }

    private static class AnimationInfo {
        private final SpecialEffectsController.Operation mOperation;
        private final CancellationSignal mSignal;

        AnimationInfo(SpecialEffectsController.Operation operation, CancellationSignal cancellationSignal) {
            this.mOperation = operation;
            this.mSignal = cancellationSignal;
        }

        /* access modifiers changed from: package-private */
        public SpecialEffectsController.Operation getOperation() {
            return this.mOperation;
        }

        /* access modifiers changed from: package-private */
        public CancellationSignal getSignal() {
            return this.mSignal;
        }
    }

    /* access modifiers changed from: private */
    public static class TransitionInfo {
        private final SpecialEffectsController.Operation mOperation;
        private final boolean mOverlapAllowed;
        private final Object mSharedElementTransition;
        private final CancellationSignal mSignal;
        private final Object mTransition;

        TransitionInfo(SpecialEffectsController.Operation operation, CancellationSignal cancellationSignal, boolean z, boolean z2) {
            Object obj;
            boolean z3;
            Object obj2;
            this.mOperation = operation;
            this.mSignal = cancellationSignal;
            if (operation.getType() == SpecialEffectsController.Operation.Type.ADD || operation.getType() == SpecialEffectsController.Operation.Type.SHOW) {
                if (z) {
                    obj = operation.getFragment().getReenterTransition();
                } else {
                    obj = operation.getFragment().getEnterTransition();
                }
                this.mTransition = obj;
                if (z) {
                    z3 = operation.getFragment().getAllowEnterTransitionOverlap();
                } else {
                    z3 = operation.getFragment().getAllowReturnTransitionOverlap();
                }
                this.mOverlapAllowed = z3;
            } else {
                if (z) {
                    obj2 = operation.getFragment().getReturnTransition();
                } else {
                    obj2 = operation.getFragment().getExitTransition();
                }
                this.mTransition = obj2;
                this.mOverlapAllowed = true;
            }
            if (!z2) {
                this.mSharedElementTransition = null;
            } else if (z) {
                this.mSharedElementTransition = operation.getFragment().getSharedElementReturnTransition();
            } else {
                this.mSharedElementTransition = operation.getFragment().getSharedElementEnterTransition();
            }
        }

        /* access modifiers changed from: package-private */
        public SpecialEffectsController.Operation getOperation() {
            return this.mOperation;
        }

        /* access modifiers changed from: package-private */
        public CancellationSignal getSignal() {
            return this.mSignal;
        }

        /* access modifiers changed from: package-private */
        public Object getTransition() {
            return this.mTransition;
        }

        /* access modifiers changed from: package-private */
        public boolean isOverlapAllowed() {
            return this.mOverlapAllowed;
        }

        public boolean hasSharedElementTransition() {
            return this.mSharedElementTransition != null;
        }

        public Object getSharedElementTransition() {
            return this.mSharedElementTransition;
        }

        /* access modifiers changed from: package-private */
        public FragmentTransitionImpl getHandlingImpl() {
            FragmentTransitionImpl handlingImpl = getHandlingImpl(this.mTransition);
            FragmentTransitionImpl handlingImpl2 = getHandlingImpl(this.mSharedElementTransition);
            if (handlingImpl == null || handlingImpl2 == null || handlingImpl == handlingImpl2) {
                return handlingImpl != null ? handlingImpl : handlingImpl2;
            }
            throw new IllegalArgumentException("Mixing framework transitions and AndroidX transitions is not allowed. Fragment " + this.mOperation.getFragment() + " returned Transition " + this.mTransition + " which uses a different Transition  type than its shared element transition " + this.mSharedElementTransition);
        }

        private FragmentTransitionImpl getHandlingImpl(Object obj) {
            if (obj == null) {
                return null;
            }
            FragmentTransitionImpl fragmentTransitionImpl = FragmentTransition.PLATFORM_IMPL;
            if (fragmentTransitionImpl != null && fragmentTransitionImpl.canHandle(obj)) {
                return FragmentTransition.PLATFORM_IMPL;
            }
            FragmentTransitionImpl fragmentTransitionImpl2 = FragmentTransition.SUPPORT_IMPL;
            if (fragmentTransitionImpl2 != null && fragmentTransitionImpl2.canHandle(obj)) {
                return FragmentTransition.SUPPORT_IMPL;
            }
            throw new IllegalArgumentException("Transition " + obj + " for fragment " + this.mOperation.getFragment() + " is not a valid framework Transition or AndroidX Transition");
        }
    }
}
