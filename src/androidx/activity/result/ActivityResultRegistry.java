package androidx.activity.result;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
public abstract class ActivityResultRegistry {
    private final transient Map<String, CallbackAndContract<?>> mKeyToCallback = new HashMap();
    private final Map<String, Integer> mKeyToRc = new HashMap();
    private final AtomicInteger mNextRc = new AtomicInteger(65535);
    private final Bundle mPendingResults = new Bundle();
    private final Map<Integer, String> mRcToKey = new HashMap();

    public final <I, O> ActivityResultLauncher<I> register(final String str, LifecycleOwner lifecycleOwner, final ActivityResultContract<I, O> activityResultContract, final ActivityResultCallback<O> activityResultCallback) {
        int registerKey = registerKey(str);
        this.mKeyToCallback.put(str, new CallbackAndContract<>(activityResultCallback, activityResultContract));
        Lifecycle lifecycle = lifecycleOwner.getLifecycle();
        final ActivityResult activityResult = (ActivityResult) this.mPendingResults.getParcelable(str);
        if (activityResult != null) {
            this.mPendingResults.remove(str);
            if (lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                activityResultCallback.onActivityResult(activityResultContract.parseResult(activityResult.getResultCode(), activityResult.getData()));
            } else {
                lifecycle.addObserver(new LifecycleEventObserver(this) { // from class: androidx.activity.result.ActivityResultRegistry.1
                    @Override // androidx.lifecycle.LifecycleEventObserver
                    public void onStateChanged(LifecycleOwner lifecycleOwner2, Lifecycle.Event event) {
                        if (Lifecycle.Event.ON_START.equals(event)) {
                            activityResultCallback.onActivityResult(activityResultContract.parseResult(activityResult.getResultCode(), activityResult.getData()));
                        }
                    }
                });
            }
        }
        lifecycle.addObserver(new LifecycleEventObserver() { // from class: androidx.activity.result.ActivityResultRegistry.2
            @Override // androidx.lifecycle.LifecycleEventObserver
            public void onStateChanged(LifecycleOwner lifecycleOwner2, Lifecycle.Event event) {
                if (Lifecycle.Event.ON_DESTROY.equals(event)) {
                    ActivityResultRegistry.this.unregister(str);
                }
            }
        });
        return new ActivityResultLauncher<I>(registerKey, activityResultContract, str) { // from class: androidx.activity.result.ActivityResultRegistry.3
            final /* synthetic */ String val$key;

            {
                this.val$key = r4;
            }

            @Override // androidx.activity.result.ActivityResultLauncher
            public void unregister() {
                ActivityResultRegistry.this.unregister(this.val$key);
            }
        };
    }

    public final <I, O> ActivityResultLauncher<I> register(String str, ActivityResultContract<I, O> activityResultContract, ActivityResultCallback<O> activityResultCallback) {
        int registerKey = registerKey(str);
        this.mKeyToCallback.put(str, new CallbackAndContract<>(activityResultCallback, activityResultContract));
        ActivityResult activityResult = (ActivityResult) this.mPendingResults.getParcelable(str);
        if (activityResult != null) {
            this.mPendingResults.remove(str);
            activityResultCallback.onActivityResult(activityResultContract.parseResult(activityResult.getResultCode(), activityResult.getData()));
        }
        return new ActivityResultLauncher<I>(registerKey, activityResultContract, str) { // from class: androidx.activity.result.ActivityResultRegistry.4
            final /* synthetic */ String val$key;

            {
                this.val$key = r4;
            }

            @Override // androidx.activity.result.ActivityResultLauncher
            public void unregister() {
                ActivityResultRegistry.this.unregister(this.val$key);
            }
        };
    }

    /* access modifiers changed from: package-private */
    public final void unregister(String str) {
        Integer remove = this.mKeyToRc.remove(str);
        if (remove != null) {
            this.mRcToKey.remove(remove);
        }
        this.mKeyToCallback.remove(str);
        if (this.mPendingResults.containsKey(str)) {
            Log.w("ActivityResultRegistry", "Dropping pending result for request " + str + ": " + this.mPendingResults.getParcelable(str));
            this.mPendingResults.remove(str);
        }
    }

    public final void onSaveInstanceState(Bundle bundle) {
        bundle.putIntegerArrayList("KEY_COMPONENT_ACTIVITY_REGISTERED_RCS", new ArrayList<>(this.mRcToKey.keySet()));
        bundle.putStringArrayList("KEY_COMPONENT_ACTIVITY_REGISTERED_KEYS", new ArrayList<>(this.mRcToKey.values()));
        bundle.putBundle("KEY_COMPONENT_ACTIVITY_PENDING_RESULT", this.mPendingResults);
    }

    public final void onRestoreInstanceState(Bundle bundle) {
        if (bundle != null) {
            ArrayList<Integer> integerArrayList = bundle.getIntegerArrayList("KEY_COMPONENT_ACTIVITY_REGISTERED_RCS");
            ArrayList<String> stringArrayList = bundle.getStringArrayList("KEY_COMPONENT_ACTIVITY_REGISTERED_KEYS");
            if (!(stringArrayList == null || integerArrayList == null)) {
                int size = stringArrayList.size();
                for (int i = 0; i < size; i++) {
                    bindRcKey(integerArrayList.get(i).intValue(), stringArrayList.get(i));
                }
                this.mNextRc.set(size);
                this.mPendingResults.putAll(bundle.getBundle("KEY_COMPONENT_ACTIVITY_PENDING_RESULT"));
            }
        }
    }

    public final boolean dispatchResult(int i, int i2, Intent intent) {
        String str = this.mRcToKey.get(Integer.valueOf(i));
        if (str == null) {
            return false;
        }
        doDispatch(str, i2, intent, this.mKeyToCallback.get(str));
        return true;
    }

    private <O> void doDispatch(String str, int i, Intent intent, CallbackAndContract<O> callbackAndContract) {
        ActivityResultCallback<O> activityResultCallback;
        if (callbackAndContract == null || (activityResultCallback = callbackAndContract.mCallback) == null) {
            this.mPendingResults.putParcelable(str, new ActivityResult(i, intent));
        } else {
            activityResultCallback.onActivityResult(callbackAndContract.mContract.parseResult(i, intent));
        }
    }

    private int registerKey(String str) {
        Integer num = this.mKeyToRc.get(str);
        if (num != null) {
            return num.intValue();
        }
        int andIncrement = this.mNextRc.getAndIncrement();
        bindRcKey(andIncrement, str);
        return andIncrement;
    }

    private void bindRcKey(int i, String str) {
        this.mRcToKey.put(Integer.valueOf(i), str);
        this.mKeyToRc.put(str, Integer.valueOf(i));
    }

    /* access modifiers changed from: private */
    public static class CallbackAndContract<O> {
        final ActivityResultCallback<O> mCallback;
        final ActivityResultContract<?, O> mContract;

        CallbackAndContract(ActivityResultCallback<O> activityResultCallback, ActivityResultContract<?, O> activityResultContract) {
            this.mCallback = activityResultCallback;
            this.mContract = activityResultContract;
        }
    }
}
