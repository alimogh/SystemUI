package androidx.lifecycle;
public class ViewModelProvider {
    private final Factory mFactory;
    private final ViewModelStore mViewModelStore;

    public interface Factory {
        <T extends ViewModel> T create(Class<T> cls);
    }

    /* access modifiers changed from: package-private */
    public static class OnRequeryFactory {
        /* access modifiers changed from: package-private */
        public void onRequery(ViewModel viewModel) {
        }

        OnRequeryFactory() {
        }
    }

    /* access modifiers changed from: package-private */
    public static abstract class KeyedFactory extends OnRequeryFactory implements Factory {
        public abstract <T extends ViewModel> T create(String str, Class<T> cls);

        KeyedFactory() {
        }

        @Override // androidx.lifecycle.ViewModelProvider.Factory
        public <T extends ViewModel> T create(Class<T> cls) {
            throw new UnsupportedOperationException("create(String, Class<?>) must be called on implementaions of KeyedFactory");
        }
    }

    public ViewModelProvider(ViewModelStore viewModelStore, Factory factory) {
        this.mFactory = factory;
        this.mViewModelStore = viewModelStore;
    }

    public <T extends ViewModel> T get(Class<T> cls) {
        String canonicalName = cls.getCanonicalName();
        if (canonicalName != null) {
            return (T) get("androidx.lifecycle.ViewModelProvider.DefaultKey:" + canonicalName, cls);
        }
        throw new IllegalArgumentException("Local and anonymous classes can not be ViewModels");
    }

    public <T extends ViewModel> T get(String str, Class<T> cls) {
        T t = (T) this.mViewModelStore.get(str);
        if (cls.isInstance(t)) {
            Factory factory = this.mFactory;
            if (factory instanceof OnRequeryFactory) {
                ((OnRequeryFactory) factory).onRequery(t);
            }
            return t;
        }
        Factory factory2 = this.mFactory;
        T t2 = factory2 instanceof KeyedFactory ? (T) ((KeyedFactory) factory2).create(str, cls) : (T) factory2.create(cls);
        this.mViewModelStore.put(str, t2);
        return t2;
    }
}
