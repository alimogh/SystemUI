package dagger.internal;

import dagger.Lazy;
public final class InstanceFactory<T> implements Factory<T>, Lazy<T> {
    private final T instance;

    public static <T> Factory<T> create(T t) {
        Preconditions.checkNotNull(t, "instance cannot be null");
        return new InstanceFactory(t);
    }

    private InstanceFactory(T t) {
        this.instance = t;
    }

    @Override // javax.inject.Provider
    public T get() {
        return this.instance;
    }
}
