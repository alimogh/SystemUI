package com.google.protobuf;

import com.google.protobuf.ArrayDecoders;
import com.google.protobuf.FieldSet;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.LazyField;
import com.google.protobuf.WireFormat;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
final class MessageSetSchema<T> implements Schema<T> {
    private final MessageLite defaultInstance;
    private final ExtensionSchema<?> extensionSchema;
    private final boolean hasExtensions;
    private final UnknownFieldSchema<?, ?> unknownFieldSchema;

    private MessageSetSchema(UnknownFieldSchema<?, ?> unknownFieldSchema, ExtensionSchema<?> extensionSchema, MessageLite messageLite) {
        this.unknownFieldSchema = unknownFieldSchema;
        this.hasExtensions = extensionSchema.hasExtensions(messageLite);
        this.extensionSchema = extensionSchema;
        this.defaultInstance = messageLite;
    }

    static <T> MessageSetSchema<T> newSchema(UnknownFieldSchema<?, ?> unknownFieldSchema, ExtensionSchema<?> extensionSchema, MessageLite messageLite) {
        return new MessageSetSchema<>(unknownFieldSchema, extensionSchema, messageLite);
    }

    @Override // com.google.protobuf.Schema
    public T newInstance() {
        return (T) this.defaultInstance.newBuilderForType().buildPartial();
    }

    @Override // com.google.protobuf.Schema
    public boolean equals(T t, T t2) {
        if (!this.unknownFieldSchema.getFromMessage(t).equals(this.unknownFieldSchema.getFromMessage(t2))) {
            return false;
        }
        if (this.hasExtensions) {
            return this.extensionSchema.getExtensions(t).equals(this.extensionSchema.getExtensions(t2));
        }
        return true;
    }

    @Override // com.google.protobuf.Schema
    public int hashCode(T t) {
        int hashCode = this.unknownFieldSchema.getFromMessage(t).hashCode();
        return this.hasExtensions ? (hashCode * 53) + this.extensionSchema.getExtensions(t).hashCode() : hashCode;
    }

    @Override // com.google.protobuf.Schema
    public void mergeFrom(T t, T t2) {
        SchemaUtil.mergeUnknownFields(this.unknownFieldSchema, t, t2);
        if (this.hasExtensions) {
            SchemaUtil.mergeExtensions(this.extensionSchema, t, t2);
        }
    }

    /* JADX DEBUG: Type inference failed for r0v3. Raw type applied. Possible types: com.google.protobuf.UnknownFieldSchema<?, ?>, com.google.protobuf.UnknownFieldSchema<UT, UB> */
    @Override // com.google.protobuf.Schema
    public void writeTo(T t, Writer writer) throws IOException {
        Iterator<Map.Entry<?, Object>> it = this.extensionSchema.getExtensions(t).iterator();
        while (it.hasNext()) {
            Map.Entry<?, Object> next = it.next();
            FieldSet.FieldDescriptorLite fieldDescriptorLite = (FieldSet.FieldDescriptorLite) next.getKey();
            if (fieldDescriptorLite.getLiteJavaType() != WireFormat.JavaType.MESSAGE || fieldDescriptorLite.isRepeated() || fieldDescriptorLite.isPacked()) {
                throw new IllegalStateException("Found invalid MessageSet item.");
            } else if (next instanceof LazyField.LazyEntry) {
                writer.writeMessageSetItem(fieldDescriptorLite.getNumber(), ((LazyField.LazyEntry) next).getField().toByteString());
            } else {
                writer.writeMessageSetItem(fieldDescriptorLite.getNumber(), next.getValue());
            }
        }
        writeUnknownFieldsHelper(this.unknownFieldSchema, t, writer);
    }

    private <UT, UB> void writeUnknownFieldsHelper(UnknownFieldSchema<UT, UB> unknownFieldSchema, T t, Writer writer) throws IOException {
        unknownFieldSchema.writeAsMessageSetTo(unknownFieldSchema.getFromMessage(t), writer);
    }

    @Override // com.google.protobuf.Schema
    public void mergeFrom(T t, byte[] bArr, int i, int i2, ArrayDecoders.Registers registers) throws IOException {
        T t2 = t;
        UnknownFieldSetLite unknownFieldSetLite = t2.unknownFields;
        if (unknownFieldSetLite == UnknownFieldSetLite.getDefaultInstance()) {
            unknownFieldSetLite = UnknownFieldSetLite.newInstance();
            t2.unknownFields = unknownFieldSetLite;
        }
        FieldSet<GeneratedMessageLite.ExtensionDescriptor> ensureExtensionsAreMutable = t.ensureExtensionsAreMutable();
        GeneratedMessageLite.GeneratedExtension generatedExtension = null;
        while (i < i2) {
            int decodeVarint32 = ArrayDecoders.decodeVarint32(bArr, i, registers);
            int i3 = registers.int1;
            if (i3 == WireFormat.MESSAGE_SET_ITEM_TAG) {
                int i4 = 0;
                ByteString byteString = null;
                while (decodeVarint32 < i2) {
                    decodeVarint32 = ArrayDecoders.decodeVarint32(bArr, decodeVarint32, registers);
                    int i5 = registers.int1;
                    int tagFieldNumber = WireFormat.getTagFieldNumber(i5);
                    int tagWireType = WireFormat.getTagWireType(i5);
                    if (tagFieldNumber != 2) {
                        if (tagFieldNumber == 3) {
                            if (generatedExtension != null) {
                                decodeVarint32 = ArrayDecoders.decodeMessageField(Protobuf.getInstance().schemaFor((Class) generatedExtension.getMessageDefaultInstance().getClass()), bArr, decodeVarint32, i2, registers);
                                ensureExtensionsAreMutable.setField(generatedExtension.descriptor, registers.object1);
                            } else if (tagWireType == 2) {
                                decodeVarint32 = ArrayDecoders.decodeBytes(bArr, decodeVarint32, registers);
                                byteString = (ByteString) registers.object1;
                            }
                        }
                    } else if (tagWireType == 0) {
                        decodeVarint32 = ArrayDecoders.decodeVarint32(bArr, decodeVarint32, registers);
                        i4 = registers.int1;
                        generatedExtension = (GeneratedMessageLite.GeneratedExtension) this.extensionSchema.findExtensionByNumber(registers.extensionRegistry, this.defaultInstance, i4);
                    }
                    if (i5 == WireFormat.MESSAGE_SET_ITEM_END_TAG) {
                        break;
                    }
                    decodeVarint32 = ArrayDecoders.skipField(i5, bArr, decodeVarint32, i2, registers);
                }
                if (byteString != null) {
                    unknownFieldSetLite.storeField(WireFormat.makeTag(i4, 2), byteString);
                }
                i = decodeVarint32;
            } else if (WireFormat.getTagWireType(i3) == 2) {
                GeneratedMessageLite.GeneratedExtension generatedExtension2 = (GeneratedMessageLite.GeneratedExtension) this.extensionSchema.findExtensionByNumber(registers.extensionRegistry, this.defaultInstance, WireFormat.getTagFieldNumber(i3));
                if (generatedExtension2 != null) {
                    i = ArrayDecoders.decodeMessageField(Protobuf.getInstance().schemaFor((Class) generatedExtension2.getMessageDefaultInstance().getClass()), bArr, decodeVarint32, i2, registers);
                    ensureExtensionsAreMutable.setField(generatedExtension2.descriptor, registers.object1);
                } else {
                    i = ArrayDecoders.decodeUnknownField(i3, bArr, decodeVarint32, i2, unknownFieldSetLite, registers);
                }
                generatedExtension = generatedExtension2;
            } else {
                i = ArrayDecoders.skipField(i3, bArr, decodeVarint32, i2, registers);
            }
        }
        if (i != i2) {
            throw InvalidProtocolBufferException.parseFailure();
        }
    }

    /* JADX DEBUG: Type inference failed for r1v0. Raw type applied. Possible types: com.google.protobuf.UnknownFieldSchema<?, ?>, com.google.protobuf.UnknownFieldSchema<UT, UB> */
    /* JADX DEBUG: Type inference failed for r2v0. Raw type applied. Possible types: com.google.protobuf.ExtensionSchema<?>, com.google.protobuf.ExtensionSchema<ET extends com.google.protobuf.FieldSet$FieldDescriptorLite<ET>> */
    @Override // com.google.protobuf.Schema
    public void mergeFrom(T t, Reader reader, ExtensionRegistryLite extensionRegistryLite) throws IOException {
        mergeFromHelper(this.unknownFieldSchema, this.extensionSchema, t, reader, extensionRegistryLite);
    }

    private <UT, UB, ET extends FieldSet.FieldDescriptorLite<ET>> void mergeFromHelper(UnknownFieldSchema<UT, UB> unknownFieldSchema, ExtensionSchema<ET> extensionSchema, T t, Reader reader, ExtensionRegistryLite extensionRegistryLite) throws IOException {
        UB builderFromMessage = unknownFieldSchema.getBuilderFromMessage(t);
        FieldSet<ET> mutableExtensions = extensionSchema.getMutableExtensions(t);
        do {
            try {
                if (reader.getFieldNumber() == Integer.MAX_VALUE) {
                    unknownFieldSchema.setBuilderToMessage(t, builderFromMessage);
                    return;
                }
            } finally {
                unknownFieldSchema.setBuilderToMessage(t, builderFromMessage);
            }
        } while (parseMessageSetItemOrUnknownField(reader, extensionRegistryLite, extensionSchema, mutableExtensions, unknownFieldSchema, builderFromMessage));
    }

    @Override // com.google.protobuf.Schema
    public void makeImmutable(T t) {
        this.unknownFieldSchema.makeImmutable(t);
        this.extensionSchema.makeImmutable(t);
    }

    private <UT, UB, ET extends FieldSet.FieldDescriptorLite<ET>> boolean parseMessageSetItemOrUnknownField(Reader reader, ExtensionRegistryLite extensionRegistryLite, ExtensionSchema<ET> extensionSchema, FieldSet<ET> fieldSet, UnknownFieldSchema<UT, UB> unknownFieldSchema, UB ub) throws IOException {
        int tag = reader.getTag();
        if (tag == WireFormat.MESSAGE_SET_ITEM_TAG) {
            int i = 0;
            Object obj = null;
            ByteString byteString = null;
            while (reader.getFieldNumber() != Integer.MAX_VALUE) {
                int tag2 = reader.getTag();
                if (tag2 == WireFormat.MESSAGE_SET_TYPE_ID_TAG) {
                    i = reader.readUInt32();
                    obj = extensionSchema.findExtensionByNumber(extensionRegistryLite, this.defaultInstance, i);
                } else if (tag2 == WireFormat.MESSAGE_SET_MESSAGE_TAG) {
                    if (obj != null) {
                        extensionSchema.parseLengthPrefixedMessageSetItem(reader, obj, extensionRegistryLite, fieldSet);
                    } else {
                        byteString = reader.readBytes();
                    }
                } else if (!reader.skipField()) {
                    break;
                }
            }
            if (reader.getTag() == WireFormat.MESSAGE_SET_ITEM_END_TAG) {
                if (byteString != null) {
                    if (obj != null) {
                        extensionSchema.parseMessageSetItem(byteString, obj, extensionRegistryLite, fieldSet);
                    } else {
                        unknownFieldSchema.addLengthDelimited(ub, i, byteString);
                    }
                }
                return true;
            }
            throw InvalidProtocolBufferException.invalidEndTag();
        } else if (WireFormat.getTagWireType(tag) != 2) {
            return reader.skipField();
        } else {
            Object findExtensionByNumber = extensionSchema.findExtensionByNumber(extensionRegistryLite, this.defaultInstance, WireFormat.getTagFieldNumber(tag));
            if (findExtensionByNumber == null) {
                return unknownFieldSchema.mergeOneFieldFrom(ub, reader);
            }
            extensionSchema.parseLengthPrefixedMessageSetItem(reader, findExtensionByNumber, extensionRegistryLite, fieldSet);
            return true;
        }
    }

    @Override // com.google.protobuf.Schema
    public final boolean isInitialized(T t) {
        return this.extensionSchema.getExtensions(t).isInitialized();
    }

    /* JADX DEBUG: Type inference failed for r0v0. Raw type applied. Possible types: com.google.protobuf.UnknownFieldSchema<?, ?>, com.google.protobuf.UnknownFieldSchema<UT, UB> */
    @Override // com.google.protobuf.Schema
    public int getSerializedSize(T t) {
        int unknownFieldsSerializedSize = getUnknownFieldsSerializedSize(this.unknownFieldSchema, t) + 0;
        return this.hasExtensions ? unknownFieldsSerializedSize + this.extensionSchema.getExtensions(t).getMessageSetSerializedSize() : unknownFieldsSerializedSize;
    }

    private <UT, UB> int getUnknownFieldsSerializedSize(UnknownFieldSchema<UT, UB> unknownFieldSchema, T t) {
        return unknownFieldSchema.getSerializedSizeAsMessageSet(unknownFieldSchema.getFromMessage(t));
    }
}
