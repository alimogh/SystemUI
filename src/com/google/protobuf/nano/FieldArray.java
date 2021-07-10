package com.google.protobuf.nano;
public final class FieldArray implements Cloneable {
    @Override // java.lang.Object
    public final FieldArray clone() {
        throw null;
    }

    /* access modifiers changed from: package-private */
    public abstract FieldData dataAt(int i);

    /* access modifiers changed from: package-private */
    public abstract int size();
}
