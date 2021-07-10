package dagger.internal;

import dagger.Lazy;
import javax.inject.Provider;
public final class DoubleCheck<T> implements Provider<T>, Lazy<T> {
    private static final Object UNINITIALIZED = new Object();
    private volatile Object instance = UNINITIALIZED;
    private volatile Provider<T> provider;

    private DoubleCheck(Provider<T> provider) {
        this.provider = provider;
    }

    @Override // javax.inject.Provider
    public T get() {
        Object obj = UNINITIALIZED;
        T t = (T) this.instance;
        if (t == obj) {
            synchronized (this) {
                t = this.instance;
                if (t == obj) {
                    T t2 = this.provider.get();
                    reentrantCheck(this.instance, t2);
                    this.instance = t2;
                    this.provider = null;
                    t = t2;
                }
            }
        }
        return t;
    }

    public static Object reentrantCheck(Object obj, Object obj2) {
        if (!(obj != UNINITIALIZED) || obj == obj2) {
            return obj2;
        }
        throw new IllegalStateException("Scoped provider was invoked recursively returning different results: " + obj + " & " + obj2 + ". This is likely due to a circular dependency.");
    }

    public static <P extends Provider<T>, T> Provider<T> provider(P p) {
        Preconditions.checkNotNull(p);
        if (p instanceof DoubleCheck) {
            return p;
        }
        return new DoubleCheck(p);
    }

    public static <P extends Provider<T>, T> Lazy<T> lazy(P p) {
        if (p instanceof Lazy) {
            return (Lazy) p;
        }
        Preconditions.checkNotNull(p);
        return new DoubleCheck(p);
    }
}
