package androidx.animation;

import android.util.Property;
public abstract class FloatProperty<T> extends Property<T, Float> {
    public abstract void setValue(T t, float f);

    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // android.util.Property
    public /* bridge */ /* synthetic */ void set(Object obj, Float f) {
        set((FloatProperty<T>) obj, f);
    }

    public FloatProperty() {
        super(Float.class, "");
    }

    public final void set(T t, Float f) {
        setValue(t, f.floatValue());
    }
}
