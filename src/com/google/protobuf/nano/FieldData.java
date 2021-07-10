package com.google.protobuf.nano;

import java.io.IOException;
class FieldData implements Cloneable {
    /* access modifiers changed from: package-private */
    public abstract int computeSerializedSize();

    /* access modifiers changed from: package-private */
    public abstract void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException;
}
