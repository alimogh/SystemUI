package androidx.animation;
public abstract class BidirectionalTypeConverter<T, V> extends TypeConverter<T, V> {
    public abstract T convertBack(V v);
}
