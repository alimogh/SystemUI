package androidx.animation;
public abstract class TypeConverter<T, V> {
    private final Class<V> mToClass;

    public abstract V convert(T t);

    /* access modifiers changed from: package-private */
    public Class<V> getTargetType() {
        return this.mToClass;
    }
}
