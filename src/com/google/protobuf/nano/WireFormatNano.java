package com.google.protobuf.nano;
public final class WireFormatNano {
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];

    static int makeTag(int i, int i2) {
        return (i << 3) | i2;
    }
}
