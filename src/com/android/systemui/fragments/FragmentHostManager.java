package com.android.systemui.fragments;

import android.app.Fragment;
import android.app.FragmentController;
import android.app.FragmentHostCallback;
import android.app.FragmentManager;
import android.app.FragmentManagerNonConfig;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import com.android.settingslib.applications.InterestingConfigChanges;
import com.android.systemui.Dependency;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.statusbar.phone.NavigationBarFrame;
import com.android.systemui.util.leak.LeakDetector;
import com.oneplus.util.OpUtils;
import com.oneplus.util.ThemeColorUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
public class FragmentHostManager {
    private static final String TAG = "FragmentHostManager";
    private final InterestingConfigChanges mConfigChanges = new InterestingConfigChanges(-1073732860);
    private final Context mContext;
    private int mCustSmallestWidthDp;
    private FragmentController mFragments;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsDestroyed;
    private boolean mIsSpecialTheme = false;
    private int mLastScreenHeightDp;
    private FragmentManager.FragmentLifecycleCallbacks mLifecycleCallbacks;
    private final HashMap<String, ArrayList<FragmentListener>> mListeners = new HashMap<>();
    private final FragmentService mManager;
    private int mOrientation = -1;
    private final ExtensionFragmentManager mPlugins = new ExtensionFragmentManager();
    private final View mRootView;

    public interface FragmentListener {
        void onFragmentViewCreated(String str, Fragment fragment);

        default void onFragmentViewDestroyed(String str, Fragment fragment) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    FragmentHostManager(FragmentService fragmentService, View view) {
        Context context = view.getContext();
        this.mContext = context;
        this.mManager = fragmentService;
        this.mRootView = view;
        this.mConfigChanges.applyNewConfig(context.getResources());
        this.mIsSpecialTheme = OpUtils.isSpecialTheme(this.mContext);
        createFragmentHost(null);
    }

    private void createFragmentHost(Parcelable parcelable) {
        FragmentController createController = FragmentController.createController(new HostCallbacks());
        this.mFragments = createController;
        createController.attachHost(null);
        this.mLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() { // from class: com.android.systemui.fragments.FragmentHostManager.1
            @Override // android.app.FragmentManager.FragmentLifecycleCallbacks
            public void onFragmentViewCreated(FragmentManager fragmentManager, Fragment fragment, View view, Bundle bundle) {
                FragmentHostManager.this.onFragmentViewCreated(fragment);
            }

            @Override // android.app.FragmentManager.FragmentLifecycleCallbacks
            public void onFragmentViewDestroyed(FragmentManager fragmentManager, Fragment fragment) {
                FragmentHostManager.this.onFragmentViewDestroyed(fragment);
            }

            @Override // android.app.FragmentManager.FragmentLifecycleCallbacks
            public void onFragmentDestroyed(FragmentManager fragmentManager, Fragment fragment) {
                ((LeakDetector) Dependency.get(LeakDetector.class)).trackGarbage(fragment);
            }
        };
        this.mFragments.getFragmentManager().registerFragmentLifecycleCallbacks(this.mLifecycleCallbacks, true);
        if (parcelable != null) {
            this.mFragments.restoreAllState(parcelable, (FragmentManagerNonConfig) null);
        }
        this.mFragments.dispatchCreate();
        this.mFragments.dispatchStart();
        this.mFragments.dispatchResume();
    }

    private Parcelable destroyFragmentHost() {
        this.mFragments.dispatchPause();
        Parcelable saveAllState = this.mFragments.saveAllState();
        this.mFragments.dispatchStop();
        this.mFragments.dispatchDestroy();
        this.mFragments.getFragmentManager().unregisterFragmentLifecycleCallbacks(this.mLifecycleCallbacks);
        return saveAllState;
    }

    public FragmentHostManager addTagListener(String str, FragmentListener fragmentListener) {
        ArrayList<FragmentListener> arrayList = this.mListeners.get(str);
        if (arrayList == null) {
            arrayList = new ArrayList<>();
            this.mListeners.put(str, arrayList);
        }
        arrayList.add(fragmentListener);
        Fragment findFragmentByTag = getFragmentManager().findFragmentByTag(str);
        if (!(findFragmentByTag == null || findFragmentByTag.getView() == null)) {
            fragmentListener.onFragmentViewCreated(str, findFragmentByTag);
        }
        return this;
    }

    public void removeTagListener(String str, FragmentListener fragmentListener) {
        ArrayList<FragmentListener> arrayList = this.mListeners.get(str);
        if (arrayList != null && arrayList.remove(fragmentListener) && arrayList.size() == 0) {
            this.mListeners.remove(str);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onFragmentViewCreated(Fragment fragment) {
        String tag = fragment.getTag();
        ArrayList<FragmentListener> arrayList = this.mListeners.get(tag);
        if (arrayList != null) {
            arrayList.forEach(new Consumer(tag, fragment) { // from class: com.android.systemui.fragments.-$$Lambda$FragmentHostManager$OsWXqtcfRJZBAvEEeN8CG6EN5T4
                public final /* synthetic */ String f$0;
                public final /* synthetic */ Fragment f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((FragmentHostManager.FragmentListener) obj).onFragmentViewCreated(this.f$0, this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onFragmentViewDestroyed(Fragment fragment) {
        String tag = fragment.getTag();
        ArrayList<FragmentListener> arrayList = this.mListeners.get(tag);
        if (arrayList != null) {
            arrayList.forEach(new Consumer(tag, fragment) { // from class: com.android.systemui.fragments.-$$Lambda$FragmentHostManager$AcJHY99nHc-JEzu3q8ny-wMOZ4E
                public final /* synthetic */ String f$0;
                public final /* synthetic */ Fragment f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((FragmentHostManager.FragmentListener) obj).onFragmentViewDestroyed(this.f$0, this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        boolean z = this.mRootView instanceof NavigationBarFrame;
        boolean z2 = false;
        boolean z3 = true;
        boolean z4 = configuration.orientation != 2;
        boolean z5 = this.mCustSmallestWidthDp != configuration.smallestScreenWidthDp;
        boolean z6 = this.mLastScreenHeightDp != configuration.screenHeightDp;
        boolean isCTSAdded = OpUtils.isCTSAdded();
        if (z4 && !isCTSAdded && (!z ? z6 : z5)) {
            z2 = true;
        }
        boolean isSpecialTheme = OpUtils.isSpecialTheme(this.mContext);
        if (this.mIsSpecialTheme != isSpecialTheme) {
            Log.d(TAG, "special theme changed. " + this.mIsSpecialTheme + "->" + isSpecialTheme);
            this.mIsSpecialTheme = isSpecialTheme;
        } else {
            z3 = z2;
        }
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2 && !z && !isCTSAdded) {
            this.mOrientation = i2;
        }
        if (this.mConfigChanges.applyNewConfig(this.mContext.getResources()) || z3) {
            this.mCustSmallestWidthDp = configuration.smallestScreenWidthDp;
            this.mLastScreenHeightDp = configuration.screenHeightDp;
            Log.d(TAG, "onConfigChange: calling reload fragments.");
            reloadFragments();
            return;
        }
        this.mFragments.dispatchConfigurationChanged(configuration);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private <T extends View> T findViewById(int i) {
        return (T) this.mRootView.findViewById(i);
    }

    public FragmentManager getFragmentManager() {
        return this.mFragments.getFragmentManager();
    }

    /* access modifiers changed from: package-private */
    public ExtensionFragmentManager getExtensionManager() {
        return this.mPlugins;
    }

    /* access modifiers changed from: package-private */
    public void destroy() {
        this.mIsDestroyed = true;
        this.mFragments.dispatchDestroy();
    }

    public <T> T create(Class<T> cls) {
        return (T) this.mPlugins.instantiate(this.mContext, cls.getName(), null);
    }

    public static FragmentHostManager get(View view) {
        try {
            return ((FragmentService) Dependency.get(FragmentService.class)).getFragmentHostManager(view);
        } catch (ClassCastException e) {
            throw e;
        }
    }

    public static void removeAndDestroy(View view) {
        ((FragmentService) Dependency.get(FragmentService.class)).removeAndDestroy(view);
    }

    public void reloadFragments() {
        boolean z = this.mRootView instanceof NavigationBarFrame;
        if (!this.mIsDestroyed || !z) {
            ThemeColorUtils.init(this.mContext);
            createFragmentHost(destroyFragmentHost());
            return;
        }
        Log.w(TAG, "mFragments is isDestroyed, skip recreate FragmentController");
    }

    /* access modifiers changed from: package-private */
    public class HostCallbacks extends FragmentHostCallback<FragmentHostManager> {
        @Override // android.app.FragmentHostCallback
        public void onAttachFragment(Fragment fragment) {
        }

        @Override // android.app.FragmentHostCallback
        public int onGetWindowAnimations() {
            return 0;
        }

        @Override // android.app.FragmentHostCallback, android.app.FragmentContainer
        public boolean onHasView() {
            return true;
        }

        @Override // android.app.FragmentHostCallback
        public boolean onHasWindowAnimations() {
            return false;
        }

        @Override // android.app.FragmentHostCallback
        public boolean onShouldSaveFragmentState(Fragment fragment) {
            return true;
        }

        @Override // android.app.FragmentHostCallback
        public boolean onUseFragmentManagerInflaterFactory() {
            return true;
        }

        public HostCallbacks() {
            super(FragmentHostManager.this.mContext, FragmentHostManager.this.mHandler, 0);
        }

        @Override // android.app.FragmentHostCallback
        public FragmentHostManager onGetHost() {
            return FragmentHostManager.this;
        }

        @Override // android.app.FragmentHostCallback
        public void onDump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            FragmentHostManager.this.dump(str, fileDescriptor, printWriter, strArr);
        }

        public Fragment instantiate(Context context, String str, Bundle bundle) {
            return FragmentHostManager.this.mPlugins.instantiate(context, str, bundle);
        }

        @Override // android.app.FragmentHostCallback
        public LayoutInflater onGetLayoutInflater() {
            return LayoutInflater.from(FragmentHostManager.this.mContext);
        }

        @Override // android.app.FragmentHostCallback, android.app.FragmentContainer
        public <T extends View> T onFindViewById(int i) {
            return (T) FragmentHostManager.this.findViewById(i);
        }
    }

    /* access modifiers changed from: package-private */
    public class ExtensionFragmentManager {
        private final ArrayMap<String, Context> mExtensionLookup = new ArrayMap<>();

        ExtensionFragmentManager() {
        }

        public void setCurrentExtension(int i, String str, String str2, String str3, Context context) {
            if (str2 != null) {
                this.mExtensionLookup.remove(str2);
            }
            this.mExtensionLookup.put(str3, context);
            FragmentHostManager.this.getFragmentManager().beginTransaction().replace(i, instantiate(context, str3, null), str).commit();
            FragmentHostManager.this.reloadFragments();
        }

        /* access modifiers changed from: package-private */
        public Fragment instantiate(Context context, String str, Bundle bundle) {
            Context context2 = this.mExtensionLookup.get(str);
            if (context2 == null) {
                return instantiateWithInjections(context, str, bundle);
            }
            Fragment instantiateWithInjections = instantiateWithInjections(context2, str, bundle);
            if (instantiateWithInjections instanceof Plugin) {
                ((Plugin) instantiateWithInjections).onCreate(FragmentHostManager.this.mContext, context2);
            }
            return instantiateWithInjections;
        }

        private Fragment instantiateWithInjections(Context context, String str, Bundle bundle) {
            Method method = FragmentHostManager.this.mManager.getInjectionMap().get(str);
            if (method == null) {
                return Fragment.instantiate(context, str, bundle);
            }
            try {
                Fragment fragment = (Fragment) method.invoke(FragmentHostManager.this.mManager.getFragmentCreator(), new Object[0]);
                if (bundle != null) {
                    bundle.setClassLoader(fragment.getClass().getClassLoader());
                    fragment.setArguments(bundle);
                }
                return fragment;
            } catch (IllegalAccessException e) {
                throw new Fragment.InstantiationException("Unable to instantiate " + str, e);
            } catch (InvocationTargetException e2) {
                throw new Fragment.InstantiationException("Unable to instantiate " + str, e2);
            }
        }
    }
}
