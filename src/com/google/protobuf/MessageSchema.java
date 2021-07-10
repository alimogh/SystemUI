package com.google.protobuf;

import androidx.constraintlayout.widget.R$styleable;
import com.google.protobuf.ArrayDecoders;
import com.google.protobuf.ByteString;
import com.google.protobuf.FieldSet;
import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MapEntryLite;
import com.google.protobuf.WireFormat;
import com.google.protobuf.Writer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import sun.misc.Unsafe;
/* access modifiers changed from: package-private */
public final class MessageSchema<T> implements Schema<T> {
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();
    private final int[] buffer;
    private final int checkInitializedCount;
    private final MessageLite defaultInstance;
    private final ExtensionSchema<?> extensionSchema;
    private final boolean hasExtensions;
    private final int[] intArray;
    private final ListFieldSchema listFieldSchema;
    private final boolean lite;
    private final MapFieldSchema mapFieldSchema;
    private final int maxFieldNumber;
    private final int minFieldNumber;
    private final NewInstanceSchema newInstanceSchema;
    private final Object[] objects;
    private final boolean proto3;
    private final int repeatedFieldOffsetStart;
    private final UnknownFieldSchema<?, ?> unknownFieldSchema;
    private final boolean useCachedSizeField;

    private static boolean isEnforceUtf8(int i) {
        return (i & 536870912) != 0;
    }

    private static boolean isRequired(int i) {
        return (i & 268435456) != 0;
    }

    private static long offset(int i) {
        return (long) (i & 1048575);
    }

    private static int type(int i) {
        return (i & 267386880) >>> 20;
    }

    private MessageSchema(int[] iArr, Object[] objArr, int i, int i2, MessageLite messageLite, boolean z, boolean z2, int[] iArr2, int i3, int i4, NewInstanceSchema newInstanceSchema, ListFieldSchema listFieldSchema, UnknownFieldSchema<?, ?> unknownFieldSchema, ExtensionSchema<?> extensionSchema, MapFieldSchema mapFieldSchema) {
        this.buffer = iArr;
        this.objects = objArr;
        this.minFieldNumber = i;
        this.maxFieldNumber = i2;
        this.lite = messageLite instanceof GeneratedMessageLite;
        this.proto3 = z;
        this.hasExtensions = extensionSchema != null && extensionSchema.hasExtensions(messageLite);
        this.useCachedSizeField = z2;
        this.intArray = iArr2;
        this.checkInitializedCount = i3;
        this.repeatedFieldOffsetStart = i4;
        this.newInstanceSchema = newInstanceSchema;
        this.listFieldSchema = listFieldSchema;
        this.unknownFieldSchema = unknownFieldSchema;
        this.extensionSchema = extensionSchema;
        this.defaultInstance = messageLite;
        this.mapFieldSchema = mapFieldSchema;
    }

    static <T> MessageSchema<T> newSchema(Class<T> cls, MessageInfo messageInfo, NewInstanceSchema newInstanceSchema, ListFieldSchema listFieldSchema, UnknownFieldSchema<?, ?> unknownFieldSchema, ExtensionSchema<?> extensionSchema, MapFieldSchema mapFieldSchema) {
        if (messageInfo instanceof RawMessageInfo) {
            return newSchemaForRawMessageInfo((RawMessageInfo) messageInfo, newInstanceSchema, listFieldSchema, unknownFieldSchema, extensionSchema, mapFieldSchema);
        }
        return newSchemaForMessageInfo((StructuralMessageInfo) messageInfo, newInstanceSchema, listFieldSchema, unknownFieldSchema, extensionSchema, mapFieldSchema);
    }

    /* JADX WARNING: Removed duplicated region for block: B:121:0x0277  */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x027a  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x0292  */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0295  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x033c  */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x0391  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static <T> com.google.protobuf.MessageSchema<T> newSchemaForRawMessageInfo(com.google.protobuf.RawMessageInfo r36, com.google.protobuf.NewInstanceSchema r37, com.google.protobuf.ListFieldSchema r38, com.google.protobuf.UnknownFieldSchema<?, ?> r39, com.google.protobuf.ExtensionSchema<?> r40, com.google.protobuf.MapFieldSchema r41) {
        /*
        // Method dump skipped, instructions count: 1041
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.MessageSchema.newSchemaForRawMessageInfo(com.google.protobuf.RawMessageInfo, com.google.protobuf.NewInstanceSchema, com.google.protobuf.ListFieldSchema, com.google.protobuf.UnknownFieldSchema, com.google.protobuf.ExtensionSchema, com.google.protobuf.MapFieldSchema):com.google.protobuf.MessageSchema");
    }

    private static Field reflectField(Class<?> cls, String str) {
        try {
            return cls.getDeclaredField(str);
        } catch (NoSuchFieldException unused) {
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field field : declaredFields) {
                if (str.equals(field.getName())) {
                    return field;
                }
            }
            throw new RuntimeException("Field " + str + " for " + cls.getName() + " not found. Known fields are " + Arrays.toString(declaredFields));
        }
    }

    static <T> MessageSchema<T> newSchemaForMessageInfo(StructuralMessageInfo structuralMessageInfo, NewInstanceSchema newInstanceSchema, ListFieldSchema listFieldSchema, UnknownFieldSchema<?, ?> unknownFieldSchema, ExtensionSchema<?> extensionSchema, MapFieldSchema mapFieldSchema) {
        int i;
        int i2;
        int i3;
        int[] iArr = EMPTY_INT_ARRAY;
        boolean z = structuralMessageInfo.getSyntax() == ProtoSyntax.PROTO3;
        FieldInfo[] fields = structuralMessageInfo.getFields();
        if (fields.length == 0) {
            i2 = 0;
            i = 0;
        } else {
            i2 = fields[0].getFieldNumber();
            i = fields[fields.length - 1].getFieldNumber();
        }
        int length = fields.length;
        int[] iArr2 = new int[(length * 3)];
        Object[] objArr = new Object[(length * 2)];
        int i4 = 0;
        int i5 = 0;
        for (FieldInfo fieldInfo : fields) {
            if (fieldInfo.getType() == FieldType.MAP) {
                i4++;
            } else if (fieldInfo.getType().id() >= 18 && fieldInfo.getType().id() <= 49) {
                i5++;
            }
        }
        int[] iArr3 = null;
        int[] iArr4 = i4 > 0 ? new int[i4] : null;
        if (i5 > 0) {
            iArr3 = new int[i5];
        }
        int[] checkInitialized = structuralMessageInfo.getCheckInitialized();
        if (checkInitialized == null) {
            checkInitialized = iArr;
        }
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        int i10 = 0;
        while (i6 < fields.length) {
            FieldInfo fieldInfo2 = fields[i6];
            int fieldNumber = fieldInfo2.getFieldNumber();
            storeFieldData(fieldInfo2, iArr2, i7, z, objArr);
            if (i8 < checkInitialized.length && checkInitialized[i8] == fieldNumber) {
                checkInitialized[i8] = i7;
                i8++;
            }
            if (fieldInfo2.getType() == FieldType.MAP) {
                iArr4[i9] = i7;
                i9++;
            } else if (fieldInfo2.getType().id() >= 18 && fieldInfo2.getType().id() <= 49) {
                i3 = i8;
                iArr3[i10] = (int) UnsafeUtil.objectFieldOffset(fieldInfo2.getField());
                i10++;
                i6++;
                i7 += 3;
                i8 = i3;
                iArr = iArr;
            }
            i3 = i8;
            i6++;
            i7 += 3;
            i8 = i3;
            iArr = iArr;
        }
        if (iArr4 == null) {
            iArr4 = iArr;
        }
        int[] iArr5 = iArr3 == null ? iArr : iArr3;
        int[] iArr6 = new int[(checkInitialized.length + iArr4.length + iArr5.length)];
        System.arraycopy(checkInitialized, 0, iArr6, 0, checkInitialized.length);
        System.arraycopy(iArr4, 0, iArr6, checkInitialized.length, iArr4.length);
        System.arraycopy(iArr5, 0, iArr6, checkInitialized.length + iArr4.length, iArr5.length);
        return new MessageSchema<>(iArr2, objArr, i2, i, structuralMessageInfo.getDefaultInstance(), z, true, iArr6, checkInitialized.length, checkInitialized.length + iArr4.length, newInstanceSchema, listFieldSchema, unknownFieldSchema, extensionSchema, mapFieldSchema);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0081  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x008b  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00c5  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void storeFieldData(com.google.protobuf.FieldInfo r8, int[] r9, int r10, boolean r11, java.lang.Object[] r12) {
        /*
            com.google.protobuf.OneofInfo r0 = r8.getOneof()
            r1 = 0
            if (r0 == 0) goto L_0x0027
            com.google.protobuf.FieldType r11 = r8.getType()
            int r11 = r11.id()
            int r11 = r11 + 51
            java.lang.reflect.Field r2 = r0.getValueField()
            long r2 = com.google.protobuf.UnsafeUtil.objectFieldOffset(r2)
            int r2 = (int) r2
            java.lang.reflect.Field r0 = r0.getCaseField()
            long r3 = com.google.protobuf.UnsafeUtil.objectFieldOffset(r0)
            int r0 = (int) r3
        L_0x0023:
            r3 = r2
            r2 = r0
            r0 = r1
            goto L_0x0073
        L_0x0027:
            com.google.protobuf.FieldType r0 = r8.getType()
            java.lang.reflect.Field r2 = r8.getField()
            long r2 = com.google.protobuf.UnsafeUtil.objectFieldOffset(r2)
            int r2 = (int) r2
            int r3 = r0.id()
            if (r11 != 0) goto L_0x005d
            boolean r11 = r0.isList()
            if (r11 != 0) goto L_0x005d
            boolean r11 = r0.isMap()
            if (r11 != 0) goto L_0x005d
            java.lang.reflect.Field r11 = r8.getPresenceField()
            long r4 = com.google.protobuf.UnsafeUtil.objectFieldOffset(r11)
            int r0 = (int) r4
            int r11 = r8.getPresenceMask()
            int r11 = java.lang.Integer.numberOfTrailingZeros(r11)
            r7 = r0
            r0 = r11
            r11 = r3
            r3 = r2
            r2 = r7
            goto L_0x0073
        L_0x005d:
            java.lang.reflect.Field r11 = r8.getCachedSizeField()
            if (r11 != 0) goto L_0x0068
            r0 = r1
            r11 = r3
            r3 = r2
            r2 = r0
            goto L_0x0073
        L_0x0068:
            java.lang.reflect.Field r11 = r8.getCachedSizeField()
            long r4 = com.google.protobuf.UnsafeUtil.objectFieldOffset(r11)
            int r0 = (int) r4
            r11 = r3
            goto L_0x0023
        L_0x0073:
            int r4 = r8.getFieldNumber()
            r9[r10] = r4
            int r4 = r10 + 1
            boolean r5 = r8.isEnforceUtf8()
            if (r5 == 0) goto L_0x0084
            r5 = 536870912(0x20000000, float:1.0842022E-19)
            goto L_0x0085
        L_0x0084:
            r5 = r1
        L_0x0085:
            boolean r6 = r8.isRequired()
            if (r6 == 0) goto L_0x008d
            r1 = 268435456(0x10000000, float:2.5243549E-29)
        L_0x008d:
            r1 = r1 | r5
            int r11 = r11 << 20
            r11 = r11 | r1
            r11 = r11 | r3
            r9[r4] = r11
            int r11 = r10 + 2
            int r0 = r0 << 20
            r0 = r0 | r2
            r9[r11] = r0
            java.lang.Class r9 = r8.getMessageFieldClass()
            java.lang.Object r11 = r8.getMapDefaultEntry()
            if (r11 == 0) goto L_0x00c5
            int r10 = r10 / 3
            int r10 = r10 * 2
            java.lang.Object r11 = r8.getMapDefaultEntry()
            r12[r10] = r11
            if (r9 == 0) goto L_0x00b6
            int r10 = r10 + 1
            r12[r10] = r9
            goto L_0x00e2
        L_0x00b6:
            com.google.protobuf.Internal$EnumVerifier r9 = r8.getEnumVerifier()
            if (r9 == 0) goto L_0x00e2
            int r10 = r10 + 1
            com.google.protobuf.Internal$EnumVerifier r8 = r8.getEnumVerifier()
            r12[r10] = r8
            goto L_0x00e2
        L_0x00c5:
            if (r9 == 0) goto L_0x00d0
            int r10 = r10 / 3
            int r10 = r10 * 2
            int r10 = r10 + 1
            r12[r10] = r9
            goto L_0x00e2
        L_0x00d0:
            com.google.protobuf.Internal$EnumVerifier r9 = r8.getEnumVerifier()
            if (r9 == 0) goto L_0x00e2
            int r10 = r10 / 3
            int r10 = r10 * 2
            int r10 = r10 + 1
            com.google.protobuf.Internal$EnumVerifier r8 = r8.getEnumVerifier()
            r12[r10] = r8
        L_0x00e2:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.MessageSchema.storeFieldData(com.google.protobuf.FieldInfo, int[], int, boolean, java.lang.Object[]):void");
    }

    @Override // com.google.protobuf.Schema
    public T newInstance() {
        return (T) this.newInstanceSchema.newInstance(this.defaultInstance);
    }

    @Override // com.google.protobuf.Schema
    public boolean equals(T t, T t2) {
        int length = this.buffer.length;
        for (int i = 0; i < length; i += 3) {
            if (!equals(t, t2, i)) {
                return false;
            }
        }
        if (!this.unknownFieldSchema.getFromMessage(t).equals(this.unknownFieldSchema.getFromMessage(t2))) {
            return false;
        }
        if (this.hasExtensions) {
            return this.extensionSchema.getExtensions(t).equals(this.extensionSchema.getExtensions(t2));
        }
        return true;
    }

    private boolean equals(T t, T t2, int i) {
        int typeAndOffsetAt = typeAndOffsetAt(i);
        long offset = offset(typeAndOffsetAt);
        switch (type(typeAndOffsetAt)) {
            case 0:
                return arePresentForEquals(t, t2, i) && Double.doubleToLongBits(UnsafeUtil.getDouble(t, offset)) == Double.doubleToLongBits(UnsafeUtil.getDouble(t2, offset));
            case 1:
                return arePresentForEquals(t, t2, i) && Float.floatToIntBits(UnsafeUtil.getFloat(t, offset)) == Float.floatToIntBits(UnsafeUtil.getFloat(t2, offset));
            case 2:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getLong(t, offset) == UnsafeUtil.getLong(t2, offset);
            case 3:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getLong(t, offset) == UnsafeUtil.getLong(t2, offset);
            case 4:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getInt(t, offset) == UnsafeUtil.getInt(t2, offset);
            case 5:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getLong(t, offset) == UnsafeUtil.getLong(t2, offset);
            case 6:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getInt(t, offset) == UnsafeUtil.getInt(t2, offset);
            case 7:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getBoolean(t, offset) == UnsafeUtil.getBoolean(t2, offset);
            case 8:
                return arePresentForEquals(t, t2, i) && SchemaUtil.safeEquals(UnsafeUtil.getObject(t, offset), UnsafeUtil.getObject(t2, offset));
            case 9:
                return arePresentForEquals(t, t2, i) && SchemaUtil.safeEquals(UnsafeUtil.getObject(t, offset), UnsafeUtil.getObject(t2, offset));
            case 10:
                return arePresentForEquals(t, t2, i) && SchemaUtil.safeEquals(UnsafeUtil.getObject(t, offset), UnsafeUtil.getObject(t2, offset));
            case 11:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getInt(t, offset) == UnsafeUtil.getInt(t2, offset);
            case 12:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getInt(t, offset) == UnsafeUtil.getInt(t2, offset);
            case 13:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getInt(t, offset) == UnsafeUtil.getInt(t2, offset);
            case 14:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getLong(t, offset) == UnsafeUtil.getLong(t2, offset);
            case 15:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getInt(t, offset) == UnsafeUtil.getInt(t2, offset);
            case 16:
                return arePresentForEquals(t, t2, i) && UnsafeUtil.getLong(t, offset) == UnsafeUtil.getLong(t2, offset);
            case 17:
                return arePresentForEquals(t, t2, i) && SchemaUtil.safeEquals(UnsafeUtil.getObject(t, offset), UnsafeUtil.getObject(t2, offset));
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
                return SchemaUtil.safeEquals(UnsafeUtil.getObject(t, offset), UnsafeUtil.getObject(t2, offset));
            case 50:
                return SchemaUtil.safeEquals(UnsafeUtil.getObject(t, offset), UnsafeUtil.getObject(t2, offset));
            case 51:
            case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_begin /* 52 */:
            case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_end /* 53 */:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68:
                return isOneofCaseEqual(t, t2, i) && SchemaUtil.safeEquals(UnsafeUtil.getObject(t, offset), UnsafeUtil.getObject(t2, offset));
            default:
                return true;
        }
    }

    @Override // com.google.protobuf.Schema
    public int hashCode(T t) {
        int i;
        int i2;
        int length = this.buffer.length;
        int i3 = 0;
        for (int i4 = 0; i4 < length; i4 += 3) {
            int typeAndOffsetAt = typeAndOffsetAt(i4);
            int numberAt = numberAt(i4);
            long offset = offset(typeAndOffsetAt);
            int i5 = 37;
            switch (type(typeAndOffsetAt)) {
                case 0:
                    i2 = i3 * 53;
                    i = Internal.hashLong(Double.doubleToLongBits(UnsafeUtil.getDouble(t, offset)));
                    i3 = i2 + i;
                    break;
                case 1:
                    i2 = i3 * 53;
                    i = Float.floatToIntBits(UnsafeUtil.getFloat(t, offset));
                    i3 = i2 + i;
                    break;
                case 2:
                    i2 = i3 * 53;
                    i = Internal.hashLong(UnsafeUtil.getLong(t, offset));
                    i3 = i2 + i;
                    break;
                case 3:
                    i2 = i3 * 53;
                    i = Internal.hashLong(UnsafeUtil.getLong(t, offset));
                    i3 = i2 + i;
                    break;
                case 4:
                    i2 = i3 * 53;
                    i = UnsafeUtil.getInt(t, offset);
                    i3 = i2 + i;
                    break;
                case 5:
                    i2 = i3 * 53;
                    i = Internal.hashLong(UnsafeUtil.getLong(t, offset));
                    i3 = i2 + i;
                    break;
                case 6:
                    i2 = i3 * 53;
                    i = UnsafeUtil.getInt(t, offset);
                    i3 = i2 + i;
                    break;
                case 7:
                    i2 = i3 * 53;
                    i = Internal.hashBoolean(UnsafeUtil.getBoolean(t, offset));
                    i3 = i2 + i;
                    break;
                case 8:
                    i2 = i3 * 53;
                    i = ((String) UnsafeUtil.getObject(t, offset)).hashCode();
                    i3 = i2 + i;
                    break;
                case 9:
                    Object object = UnsafeUtil.getObject(t, offset);
                    if (object != null) {
                        i5 = object.hashCode();
                    }
                    i3 = (i3 * 53) + i5;
                    break;
                case 10:
                    i2 = i3 * 53;
                    i = UnsafeUtil.getObject(t, offset).hashCode();
                    i3 = i2 + i;
                    break;
                case 11:
                    i2 = i3 * 53;
                    i = UnsafeUtil.getInt(t, offset);
                    i3 = i2 + i;
                    break;
                case 12:
                    i2 = i3 * 53;
                    i = UnsafeUtil.getInt(t, offset);
                    i3 = i2 + i;
                    break;
                case 13:
                    i2 = i3 * 53;
                    i = UnsafeUtil.getInt(t, offset);
                    i3 = i2 + i;
                    break;
                case 14:
                    i2 = i3 * 53;
                    i = Internal.hashLong(UnsafeUtil.getLong(t, offset));
                    i3 = i2 + i;
                    break;
                case 15:
                    i2 = i3 * 53;
                    i = UnsafeUtil.getInt(t, offset);
                    i3 = i2 + i;
                    break;
                case 16:
                    i2 = i3 * 53;
                    i = Internal.hashLong(UnsafeUtil.getLong(t, offset));
                    i3 = i2 + i;
                    break;
                case 17:
                    Object object2 = UnsafeUtil.getObject(t, offset);
                    if (object2 != null) {
                        i5 = object2.hashCode();
                    }
                    i3 = (i3 * 53) + i5;
                    break;
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 42:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 49:
                    i2 = i3 * 53;
                    i = UnsafeUtil.getObject(t, offset).hashCode();
                    i3 = i2 + i;
                    break;
                case 50:
                    i2 = i3 * 53;
                    i = UnsafeUtil.getObject(t, offset).hashCode();
                    i3 = i2 + i;
                    break;
                case 51:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = Internal.hashLong(Double.doubleToLongBits(oneofDoubleAt(t, offset)));
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_begin /* 52 */:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = Float.floatToIntBits(oneofFloatAt(t, offset));
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_end /* 53 */:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = Internal.hashLong(oneofLongAt(t, offset));
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 54:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = Internal.hashLong(oneofLongAt(t, offset));
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 55:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = oneofIntAt(t, offset);
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 56:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = Internal.hashLong(oneofLongAt(t, offset));
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 57:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = oneofIntAt(t, offset);
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 58:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = Internal.hashBoolean(oneofBooleanAt(t, offset));
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 59:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = ((String) UnsafeUtil.getObject(t, offset)).hashCode();
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 60:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = UnsafeUtil.getObject(t, offset).hashCode();
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 61:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = UnsafeUtil.getObject(t, offset).hashCode();
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 62:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = oneofIntAt(t, offset);
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 63:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = oneofIntAt(t, offset);
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 64:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = oneofIntAt(t, offset);
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 65:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = Internal.hashLong(oneofLongAt(t, offset));
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 66:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = oneofIntAt(t, offset);
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 67:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = Internal.hashLong(oneofLongAt(t, offset));
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
                case 68:
                    if (isOneofPresent(t, numberAt, i4)) {
                        i2 = i3 * 53;
                        i = UnsafeUtil.getObject(t, offset).hashCode();
                        i3 = i2 + i;
                        break;
                    } else {
                        break;
                    }
            }
        }
        int hashCode = (i3 * 53) + this.unknownFieldSchema.getFromMessage(t).hashCode();
        return this.hasExtensions ? (hashCode * 53) + this.extensionSchema.getExtensions(t).hashCode() : hashCode;
    }

    @Override // com.google.protobuf.Schema
    public void mergeFrom(T t, T t2) {
        if (t2 != null) {
            for (int i = 0; i < this.buffer.length; i += 3) {
                mergeSingleField(t, t2, i);
            }
            if (!this.proto3) {
                SchemaUtil.mergeUnknownFields(this.unknownFieldSchema, t, t2);
                if (this.hasExtensions) {
                    SchemaUtil.mergeExtensions(this.extensionSchema, t, t2);
                    return;
                }
                return;
            }
            return;
        }
        throw null;
    }

    private void mergeSingleField(T t, T t2, int i) {
        int typeAndOffsetAt = typeAndOffsetAt(i);
        long offset = offset(typeAndOffsetAt);
        int numberAt = numberAt(i);
        switch (type(typeAndOffsetAt)) {
            case 0:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putDouble(t, offset, UnsafeUtil.getDouble(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 1:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putFloat(t, offset, UnsafeUtil.getFloat(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 2:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putLong(t, offset, UnsafeUtil.getLong(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 3:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putLong(t, offset, UnsafeUtil.getLong(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 4:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putInt(t, offset, UnsafeUtil.getInt(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 5:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putLong(t, offset, UnsafeUtil.getLong(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 6:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putInt(t, offset, UnsafeUtil.getInt(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 7:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putBoolean(t, offset, UnsafeUtil.getBoolean(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 8:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putObject(t, offset, UnsafeUtil.getObject(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 9:
                mergeMessage(t, t2, i);
                return;
            case 10:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putObject(t, offset, UnsafeUtil.getObject(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 11:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putInt(t, offset, UnsafeUtil.getInt(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 12:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putInt(t, offset, UnsafeUtil.getInt(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 13:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putInt(t, offset, UnsafeUtil.getInt(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 14:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putLong(t, offset, UnsafeUtil.getLong(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 15:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putInt(t, offset, UnsafeUtil.getInt(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 16:
                if (isFieldPresent(t2, i)) {
                    UnsafeUtil.putLong(t, offset, UnsafeUtil.getLong(t2, offset));
                    setFieldPresent(t, i);
                    return;
                }
                return;
            case 17:
                mergeMessage(t, t2, i);
                return;
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
                this.listFieldSchema.mergeListsAt(t, t2, offset);
                return;
            case 50:
                SchemaUtil.mergeMap(this.mapFieldSchema, t, t2, offset);
                return;
            case 51:
            case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_begin /* 52 */:
            case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_end /* 53 */:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
                if (isOneofPresent(t2, numberAt, i)) {
                    UnsafeUtil.putObject(t, offset, UnsafeUtil.getObject(t2, offset));
                    setOneofPresent(t, numberAt, i);
                    return;
                }
                return;
            case 60:
                mergeOneofMessage(t, t2, i);
                return;
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
                if (isOneofPresent(t2, numberAt, i)) {
                    UnsafeUtil.putObject(t, offset, UnsafeUtil.getObject(t2, offset));
                    setOneofPresent(t, numberAt, i);
                    return;
                }
                return;
            case 68:
                mergeOneofMessage(t, t2, i);
                return;
            default:
                return;
        }
    }

    private void mergeMessage(T t, T t2, int i) {
        long offset = offset(typeAndOffsetAt(i));
        if (isFieldPresent(t2, i)) {
            Object object = UnsafeUtil.getObject(t, offset);
            Object object2 = UnsafeUtil.getObject(t2, offset);
            if (object != null && object2 != null) {
                UnsafeUtil.putObject(t, offset, Internal.mergeMessage(object, object2));
                setFieldPresent(t, i);
            } else if (object2 != null) {
                UnsafeUtil.putObject(t, offset, object2);
                setFieldPresent(t, i);
            }
        }
    }

    private void mergeOneofMessage(T t, T t2, int i) {
        int typeAndOffsetAt = typeAndOffsetAt(i);
        int numberAt = numberAt(i);
        long offset = offset(typeAndOffsetAt);
        if (isOneofPresent(t2, numberAt, i)) {
            Object object = UnsafeUtil.getObject(t, offset);
            Object object2 = UnsafeUtil.getObject(t2, offset);
            if (object != null && object2 != null) {
                UnsafeUtil.putObject(t, offset, Internal.mergeMessage(object, object2));
                setOneofPresent(t, numberAt, i);
            } else if (object2 != null) {
                UnsafeUtil.putObject(t, offset, object2);
                setOneofPresent(t, numberAt, i);
            }
        }
    }

    @Override // com.google.protobuf.Schema
    public int getSerializedSize(T t) {
        return this.proto3 ? getSerializedSizeProto3(t) : getSerializedSizeProto2(t);
    }

    /* JADX DEBUG: Type inference failed for r2v1. Raw type applied. Possible types: com.google.protobuf.UnknownFieldSchema<?, ?>, com.google.protobuf.UnknownFieldSchema<UT, UB> */
    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private int getSerializedSizeProto2(T t) {
        int i;
        int i2;
        int i3;
        int computeBoolSize;
        int i4;
        boolean z;
        int i5;
        int i6;
        int i7;
        int i8;
        Unsafe unsafe = UNSAFE;
        int i9 = -1;
        int i10 = 0;
        int i11 = 0;
        int i12 = 0;
        while (i10 < this.buffer.length) {
            int typeAndOffsetAt = typeAndOffsetAt(i10);
            int numberAt = numberAt(i10);
            int type = type(typeAndOffsetAt);
            if (type <= 17) {
                i2 = this.buffer[i10 + 2];
                int i13 = 1048575 & i2;
                int i14 = 1 << (i2 >>> 20);
                if (i13 != i9) {
                    i12 = unsafe.getInt(t, (long) i13);
                    i9 = i13;
                }
                i = i14;
            } else {
                i2 = (!this.useCachedSizeField || type < FieldType.DOUBLE_LIST_PACKED.id() || type > FieldType.SINT64_LIST_PACKED.id()) ? 0 : this.buffer[i10 + 2] & 1048575;
                i = 0;
            }
            long offset = offset(typeAndOffsetAt);
            switch (type) {
                case 0:
                    if ((i12 & i) == 0) {
                        break;
                    } else {
                        i3 = CodedOutputStream.computeDoubleSize(numberAt, 0.0d);
                        i11 += i3;
                        break;
                    }
                case 1:
                    if ((i12 & i) == 0) {
                        break;
                    } else {
                        i3 = CodedOutputStream.computeFloatSize(numberAt, 0.0f);
                        i11 += i3;
                        break;
                    }
                case 2:
                    if ((i12 & i) == 0) {
                        break;
                    } else {
                        i3 = CodedOutputStream.computeInt64Size(numberAt, unsafe.getLong(t, offset));
                        i11 += i3;
                        break;
                    }
                case 3:
                    if ((i12 & i) == 0) {
                        break;
                    } else {
                        i3 = CodedOutputStream.computeUInt64Size(numberAt, unsafe.getLong(t, offset));
                        i11 += i3;
                        break;
                    }
                case 4:
                    if ((i12 & i) == 0) {
                        break;
                    } else {
                        i3 = CodedOutputStream.computeInt32Size(numberAt, unsafe.getInt(t, offset));
                        i11 += i3;
                        break;
                    }
                case 5:
                    if ((i12 & i) == 0) {
                        break;
                    } else {
                        i3 = CodedOutputStream.computeFixed64Size(numberAt, 0);
                        i11 += i3;
                        break;
                    }
                case 6:
                    if ((i12 & i) != 0) {
                        i3 = CodedOutputStream.computeFixed32Size(numberAt, 0);
                        i11 += i3;
                        break;
                    }
                    break;
                case 7:
                    if ((i12 & i) != 0) {
                        computeBoolSize = CodedOutputStream.computeBoolSize(numberAt, true);
                        i11 += computeBoolSize;
                    }
                    break;
                case 8:
                    if ((i12 & i) != 0) {
                        Object object = unsafe.getObject(t, offset);
                        computeBoolSize = object instanceof ByteString ? CodedOutputStream.computeBytesSize(numberAt, (ByteString) object) : CodedOutputStream.computeStringSize(numberAt, (String) object);
                        i11 += computeBoolSize;
                    }
                    break;
                case 9:
                    if ((i12 & i) != 0) {
                        computeBoolSize = SchemaUtil.computeSizeMessage(numberAt, unsafe.getObject(t, offset), getMessageFieldSchema(i10));
                        i11 += computeBoolSize;
                    }
                    break;
                case 10:
                    if ((i12 & i) != 0) {
                        computeBoolSize = CodedOutputStream.computeBytesSize(numberAt, (ByteString) unsafe.getObject(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 11:
                    if ((i12 & i) != 0) {
                        computeBoolSize = CodedOutputStream.computeUInt32Size(numberAt, unsafe.getInt(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 12:
                    if ((i12 & i) != 0) {
                        computeBoolSize = CodedOutputStream.computeEnumSize(numberAt, unsafe.getInt(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 13:
                    if ((i12 & i) != 0) {
                        i4 = CodedOutputStream.computeSFixed32Size(numberAt, 0);
                        i11 += i4;
                    }
                    break;
                case 14:
                    if ((i12 & i) != 0) {
                        computeBoolSize = CodedOutputStream.computeSFixed64Size(numberAt, 0);
                        i11 += computeBoolSize;
                    }
                    break;
                case 15:
                    if ((i12 & i) != 0) {
                        computeBoolSize = CodedOutputStream.computeSInt32Size(numberAt, unsafe.getInt(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 16:
                    if ((i12 & i) != 0) {
                        computeBoolSize = CodedOutputStream.computeSInt64Size(numberAt, unsafe.getLong(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 17:
                    if ((i12 & i) != 0) {
                        computeBoolSize = CodedOutputStream.computeGroupSize(numberAt, (MessageLite) unsafe.getObject(t, offset), getMessageFieldSchema(i10));
                        i11 += computeBoolSize;
                    }
                    break;
                case 18:
                    computeBoolSize = SchemaUtil.computeSizeFixed64List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += computeBoolSize;
                    break;
                case 19:
                    z = false;
                    i5 = SchemaUtil.computeSizeFixed32List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 20:
                    z = false;
                    i5 = SchemaUtil.computeSizeInt64List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 21:
                    z = false;
                    i5 = SchemaUtil.computeSizeUInt64List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 22:
                    z = false;
                    i5 = SchemaUtil.computeSizeInt32List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 23:
                    z = false;
                    i5 = SchemaUtil.computeSizeFixed64List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 24:
                    z = false;
                    i5 = SchemaUtil.computeSizeFixed32List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 25:
                    z = false;
                    i5 = SchemaUtil.computeSizeBoolList(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 26:
                    computeBoolSize = SchemaUtil.computeSizeStringList(numberAt, (List) unsafe.getObject(t, offset));
                    i11 += computeBoolSize;
                    break;
                case 27:
                    computeBoolSize = SchemaUtil.computeSizeMessageList(numberAt, (List) unsafe.getObject(t, offset), getMessageFieldSchema(i10));
                    i11 += computeBoolSize;
                    break;
                case 28:
                    computeBoolSize = SchemaUtil.computeSizeByteStringList(numberAt, (List) unsafe.getObject(t, offset));
                    i11 += computeBoolSize;
                    break;
                case 29:
                    computeBoolSize = SchemaUtil.computeSizeUInt32List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += computeBoolSize;
                    break;
                case 30:
                    z = false;
                    i5 = SchemaUtil.computeSizeEnumList(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 31:
                    z = false;
                    i5 = SchemaUtil.computeSizeFixed32List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 32:
                    z = false;
                    i5 = SchemaUtil.computeSizeFixed64List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 33:
                    z = false;
                    i5 = SchemaUtil.computeSizeSInt32List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 34:
                    z = false;
                    i5 = SchemaUtil.computeSizeSInt64List(numberAt, (List) unsafe.getObject(t, offset), false);
                    i11 += i5;
                    break;
                case 35:
                    i8 = SchemaUtil.computeSizeFixed64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 36:
                    i8 = SchemaUtil.computeSizeFixed32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 37:
                    i8 = SchemaUtil.computeSizeInt64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 38:
                    i8 = SchemaUtil.computeSizeUInt64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 39:
                    i8 = SchemaUtil.computeSizeInt32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 40:
                    i8 = SchemaUtil.computeSizeFixed64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 41:
                    i8 = SchemaUtil.computeSizeFixed32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 42:
                    i8 = SchemaUtil.computeSizeBoolListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 43:
                    i8 = SchemaUtil.computeSizeUInt32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 44:
                    i8 = SchemaUtil.computeSizeEnumListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 45:
                    i8 = SchemaUtil.computeSizeFixed32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 46:
                    i8 = SchemaUtil.computeSizeFixed64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 47:
                    i8 = SchemaUtil.computeSizeSInt32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 48:
                    i8 = SchemaUtil.computeSizeSInt64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i8 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i2, i8);
                        }
                        i7 = CodedOutputStream.computeTagSize(numberAt);
                        i6 = CodedOutputStream.computeUInt32SizeNoTag(i8);
                        i4 = i7 + i6 + i8;
                        i11 += i4;
                    }
                    break;
                case 49:
                    computeBoolSize = SchemaUtil.computeSizeGroupList(numberAt, (List) unsafe.getObject(t, offset), getMessageFieldSchema(i10));
                    i11 += computeBoolSize;
                    break;
                case 50:
                    computeBoolSize = this.mapFieldSchema.getSerializedSize(numberAt, unsafe.getObject(t, offset), getMapFieldDefaultEntry(i10));
                    i11 += computeBoolSize;
                    break;
                case 51:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeDoubleSize(numberAt, 0.0d);
                        i11 += computeBoolSize;
                    }
                    break;
                case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_begin /* 52 */:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeFloatSize(numberAt, 0.0f);
                        i11 += computeBoolSize;
                    }
                    break;
                case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_end /* 53 */:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeInt64Size(numberAt, oneofLongAt(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 54:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeUInt64Size(numberAt, oneofLongAt(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 55:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeInt32Size(numberAt, oneofIntAt(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 56:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeFixed64Size(numberAt, 0);
                        i11 += computeBoolSize;
                    }
                    break;
                case 57:
                    if (isOneofPresent(t, numberAt, i10)) {
                        i4 = CodedOutputStream.computeFixed32Size(numberAt, 0);
                        i11 += i4;
                    }
                    break;
                case 58:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeBoolSize(numberAt, true);
                        i11 += computeBoolSize;
                    }
                    break;
                case 59:
                    if (isOneofPresent(t, numberAt, i10)) {
                        Object object2 = unsafe.getObject(t, offset);
                        computeBoolSize = object2 instanceof ByteString ? CodedOutputStream.computeBytesSize(numberAt, (ByteString) object2) : CodedOutputStream.computeStringSize(numberAt, (String) object2);
                        i11 += computeBoolSize;
                    }
                    break;
                case 60:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = SchemaUtil.computeSizeMessage(numberAt, unsafe.getObject(t, offset), getMessageFieldSchema(i10));
                        i11 += computeBoolSize;
                    }
                    break;
                case 61:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeBytesSize(numberAt, (ByteString) unsafe.getObject(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 62:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeUInt32Size(numberAt, oneofIntAt(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 63:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeEnumSize(numberAt, oneofIntAt(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 64:
                    if (isOneofPresent(t, numberAt, i10)) {
                        i4 = CodedOutputStream.computeSFixed32Size(numberAt, 0);
                        i11 += i4;
                    }
                    break;
                case 65:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeSFixed64Size(numberAt, 0);
                        i11 += computeBoolSize;
                    }
                    break;
                case 66:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeSInt32Size(numberAt, oneofIntAt(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 67:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeSInt64Size(numberAt, oneofLongAt(t, offset));
                        i11 += computeBoolSize;
                    }
                    break;
                case 68:
                    if (isOneofPresent(t, numberAt, i10)) {
                        computeBoolSize = CodedOutputStream.computeGroupSize(numberAt, (MessageLite) unsafe.getObject(t, offset), getMessageFieldSchema(i10));
                        i11 += computeBoolSize;
                    }
                    break;
            }
            i10 += 3;
            i9 = i9;
        }
        int unknownFieldsSerializedSize = i11 + getUnknownFieldsSerializedSize(this.unknownFieldSchema, t);
        return this.hasExtensions ? unknownFieldsSerializedSize + this.extensionSchema.getExtensions(t).getSerializedSize() : unknownFieldsSerializedSize;
    }

    /* JADX DEBUG: Type inference failed for r2v1. Raw type applied. Possible types: com.google.protobuf.UnknownFieldSchema<?, ?>, com.google.protobuf.UnknownFieldSchema<UT, UB> */
    private int getSerializedSizeProto3(T t) {
        int computeDoubleSize;
        int i;
        int i2;
        int i3;
        Unsafe unsafe = UNSAFE;
        int i4 = 0;
        for (int i5 = 0; i5 < this.buffer.length; i5 += 3) {
            int typeAndOffsetAt = typeAndOffsetAt(i5);
            int type = type(typeAndOffsetAt);
            int numberAt = numberAt(i5);
            long offset = offset(typeAndOffsetAt);
            int i6 = (type < FieldType.DOUBLE_LIST_PACKED.id() || type > FieldType.SINT64_LIST_PACKED.id()) ? 0 : this.buffer[i5 + 2] & 1048575;
            switch (type) {
                case 0:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeDoubleSize(numberAt, 0.0d);
                        break;
                    } else {
                        continue;
                    }
                case 1:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeFloatSize(numberAt, 0.0f);
                        break;
                    } else {
                        continue;
                    }
                case 2:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeInt64Size(numberAt, UnsafeUtil.getLong(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 3:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeUInt64Size(numberAt, UnsafeUtil.getLong(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 4:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeInt32Size(numberAt, UnsafeUtil.getInt(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 5:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeFixed64Size(numberAt, 0);
                        break;
                    } else {
                        continue;
                    }
                case 6:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeFixed32Size(numberAt, 0);
                        break;
                    } else {
                        continue;
                    }
                case 7:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeBoolSize(numberAt, true);
                        break;
                    } else {
                        continue;
                    }
                case 8:
                    if (isFieldPresent(t, i5)) {
                        Object object = UnsafeUtil.getObject(t, offset);
                        if (object instanceof ByteString) {
                            computeDoubleSize = CodedOutputStream.computeBytesSize(numberAt, (ByteString) object);
                            break;
                        } else {
                            computeDoubleSize = CodedOutputStream.computeStringSize(numberAt, (String) object);
                            break;
                        }
                    } else {
                        continue;
                    }
                case 9:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = SchemaUtil.computeSizeMessage(numberAt, UnsafeUtil.getObject(t, offset), getMessageFieldSchema(i5));
                        break;
                    } else {
                        continue;
                    }
                case 10:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeBytesSize(numberAt, (ByteString) UnsafeUtil.getObject(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 11:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeUInt32Size(numberAt, UnsafeUtil.getInt(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 12:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeEnumSize(numberAt, UnsafeUtil.getInt(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 13:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeSFixed32Size(numberAt, 0);
                        break;
                    } else {
                        continue;
                    }
                case 14:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeSFixed64Size(numberAt, 0);
                        break;
                    } else {
                        continue;
                    }
                case 15:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeSInt32Size(numberAt, UnsafeUtil.getInt(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 16:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeSInt64Size(numberAt, UnsafeUtil.getLong(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 17:
                    if (isFieldPresent(t, i5)) {
                        computeDoubleSize = CodedOutputStream.computeGroupSize(numberAt, (MessageLite) UnsafeUtil.getObject(t, offset), getMessageFieldSchema(i5));
                        break;
                    } else {
                        continue;
                    }
                case 18:
                    computeDoubleSize = SchemaUtil.computeSizeFixed64List(numberAt, listAt(t, offset), false);
                    break;
                case 19:
                    computeDoubleSize = SchemaUtil.computeSizeFixed32List(numberAt, listAt(t, offset), false);
                    break;
                case 20:
                    computeDoubleSize = SchemaUtil.computeSizeInt64List(numberAt, listAt(t, offset), false);
                    break;
                case 21:
                    computeDoubleSize = SchemaUtil.computeSizeUInt64List(numberAt, listAt(t, offset), false);
                    break;
                case 22:
                    computeDoubleSize = SchemaUtil.computeSizeInt32List(numberAt, listAt(t, offset), false);
                    break;
                case 23:
                    computeDoubleSize = SchemaUtil.computeSizeFixed64List(numberAt, listAt(t, offset), false);
                    break;
                case 24:
                    computeDoubleSize = SchemaUtil.computeSizeFixed32List(numberAt, listAt(t, offset), false);
                    break;
                case 25:
                    computeDoubleSize = SchemaUtil.computeSizeBoolList(numberAt, listAt(t, offset), false);
                    break;
                case 26:
                    computeDoubleSize = SchemaUtil.computeSizeStringList(numberAt, listAt(t, offset));
                    break;
                case 27:
                    computeDoubleSize = SchemaUtil.computeSizeMessageList(numberAt, listAt(t, offset), getMessageFieldSchema(i5));
                    break;
                case 28:
                    computeDoubleSize = SchemaUtil.computeSizeByteStringList(numberAt, listAt(t, offset));
                    break;
                case 29:
                    computeDoubleSize = SchemaUtil.computeSizeUInt32List(numberAt, listAt(t, offset), false);
                    break;
                case 30:
                    computeDoubleSize = SchemaUtil.computeSizeEnumList(numberAt, listAt(t, offset), false);
                    break;
                case 31:
                    computeDoubleSize = SchemaUtil.computeSizeFixed32List(numberAt, listAt(t, offset), false);
                    break;
                case 32:
                    computeDoubleSize = SchemaUtil.computeSizeFixed64List(numberAt, listAt(t, offset), false);
                    break;
                case 33:
                    computeDoubleSize = SchemaUtil.computeSizeSInt32List(numberAt, listAt(t, offset), false);
                    break;
                case 34:
                    computeDoubleSize = SchemaUtil.computeSizeSInt64List(numberAt, listAt(t, offset), false);
                    break;
                case 35:
                    i2 = SchemaUtil.computeSizeFixed64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 36:
                    i2 = SchemaUtil.computeSizeFixed32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 37:
                    i2 = SchemaUtil.computeSizeInt64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 38:
                    i2 = SchemaUtil.computeSizeUInt64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 39:
                    i2 = SchemaUtil.computeSizeInt32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 40:
                    i2 = SchemaUtil.computeSizeFixed64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 41:
                    i2 = SchemaUtil.computeSizeFixed32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 42:
                    i2 = SchemaUtil.computeSizeBoolListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 43:
                    i2 = SchemaUtil.computeSizeUInt32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 44:
                    i2 = SchemaUtil.computeSizeEnumListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 45:
                    i2 = SchemaUtil.computeSizeFixed32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 46:
                    i2 = SchemaUtil.computeSizeFixed64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 47:
                    i2 = SchemaUtil.computeSizeSInt32ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 48:
                    i2 = SchemaUtil.computeSizeSInt64ListNoTag((List) unsafe.getObject(t, offset));
                    if (i2 > 0) {
                        if (this.useCachedSizeField) {
                            unsafe.putInt(t, (long) i6, i2);
                        }
                        i3 = CodedOutputStream.computeTagSize(numberAt);
                        i = CodedOutputStream.computeUInt32SizeNoTag(i2);
                        computeDoubleSize = i3 + i + i2;
                        break;
                    } else {
                        continue;
                    }
                case 49:
                    computeDoubleSize = SchemaUtil.computeSizeGroupList(numberAt, listAt(t, offset), getMessageFieldSchema(i5));
                    break;
                case 50:
                    computeDoubleSize = this.mapFieldSchema.getSerializedSize(numberAt, UnsafeUtil.getObject(t, offset), getMapFieldDefaultEntry(i5));
                    break;
                case 51:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeDoubleSize(numberAt, 0.0d);
                        break;
                    } else {
                        continue;
                    }
                case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_begin /* 52 */:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeFloatSize(numberAt, 0.0f);
                        break;
                    } else {
                        continue;
                    }
                case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_end /* 53 */:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeInt64Size(numberAt, oneofLongAt(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 54:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeUInt64Size(numberAt, oneofLongAt(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 55:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeInt32Size(numberAt, oneofIntAt(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 56:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeFixed64Size(numberAt, 0);
                        break;
                    } else {
                        continue;
                    }
                case 57:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeFixed32Size(numberAt, 0);
                        break;
                    } else {
                        continue;
                    }
                case 58:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeBoolSize(numberAt, true);
                        break;
                    } else {
                        continue;
                    }
                case 59:
                    if (isOneofPresent(t, numberAt, i5)) {
                        Object object2 = UnsafeUtil.getObject(t, offset);
                        if (object2 instanceof ByteString) {
                            computeDoubleSize = CodedOutputStream.computeBytesSize(numberAt, (ByteString) object2);
                            break;
                        } else {
                            computeDoubleSize = CodedOutputStream.computeStringSize(numberAt, (String) object2);
                            break;
                        }
                    } else {
                        continue;
                    }
                case 60:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = SchemaUtil.computeSizeMessage(numberAt, UnsafeUtil.getObject(t, offset), getMessageFieldSchema(i5));
                        break;
                    } else {
                        continue;
                    }
                case 61:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeBytesSize(numberAt, (ByteString) UnsafeUtil.getObject(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 62:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeUInt32Size(numberAt, oneofIntAt(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 63:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeEnumSize(numberAt, oneofIntAt(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 64:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeSFixed32Size(numberAt, 0);
                        break;
                    } else {
                        continue;
                    }
                case 65:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeSFixed64Size(numberAt, 0);
                        break;
                    } else {
                        continue;
                    }
                case 66:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeSInt32Size(numberAt, oneofIntAt(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 67:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeSInt64Size(numberAt, oneofLongAt(t, offset));
                        break;
                    } else {
                        continue;
                    }
                case 68:
                    if (isOneofPresent(t, numberAt, i5)) {
                        computeDoubleSize = CodedOutputStream.computeGroupSize(numberAt, (MessageLite) UnsafeUtil.getObject(t, offset), getMessageFieldSchema(i5));
                        break;
                    } else {
                        continue;
                    }
                default:
            }
            i4 += computeDoubleSize;
        }
        return i4 + getUnknownFieldsSerializedSize(this.unknownFieldSchema, t);
    }

    private <UT, UB> int getUnknownFieldsSerializedSize(UnknownFieldSchema<UT, UB> unknownFieldSchema, T t) {
        return unknownFieldSchema.getSerializedSize(unknownFieldSchema.getFromMessage(t));
    }

    private static List<?> listAt(Object obj, long j) {
        return (List) UnsafeUtil.getObject(obj, j);
    }

    @Override // com.google.protobuf.Schema
    public void writeTo(T t, Writer writer) throws IOException {
        writer.fieldOrder();
        Writer.FieldOrder fieldOrder = Writer.FieldOrder.DESCENDING;
        if (this.proto3) {
            writeFieldsInAscendingOrderProto3(t, writer);
        } else {
            writeFieldsInAscendingOrderProto2(t, writer);
        }
    }

    /* JADX DEBUG: Type inference failed for r3v2. Raw type applied. Possible types: com.google.protobuf.UnknownFieldSchema<?, ?>, com.google.protobuf.UnknownFieldSchema<UT, UB> */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x049e  */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x002d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeFieldsInAscendingOrderProto2(T r18, com.google.protobuf.Writer r19) throws java.io.IOException {
        /*
        // Method dump skipped, instructions count: 1352
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.MessageSchema.writeFieldsInAscendingOrderProto2(java.lang.Object, com.google.protobuf.Writer):void");
    }

    /* JADX DEBUG: Type inference failed for r0v2. Raw type applied. Possible types: com.google.protobuf.UnknownFieldSchema<?, ?>, com.google.protobuf.UnknownFieldSchema<UT, UB> */
    /* JADX WARNING: Removed duplicated region for block: B:161:0x0588  */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x0025  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeFieldsInAscendingOrderProto3(T r13, com.google.protobuf.Writer r14) throws java.io.IOException {
        /*
        // Method dump skipped, instructions count: 1584
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.MessageSchema.writeFieldsInAscendingOrderProto3(java.lang.Object, com.google.protobuf.Writer):void");
    }

    private <K, V> void writeMapHelper(Writer writer, int i, Object obj, int i2) throws IOException {
        if (obj != null) {
            writer.writeMap(i, this.mapFieldSchema.forMapMetadata(getMapFieldDefaultEntry(i2)), this.mapFieldSchema.forMapData(obj));
        }
    }

    private <UT, UB> void writeUnknownInMessageTo(UnknownFieldSchema<UT, UB> unknownFieldSchema, T t, Writer writer) throws IOException {
        unknownFieldSchema.writeTo(unknownFieldSchema.getFromMessage(t), writer);
    }

    /* JADX DEBUG: Type inference failed for r1v0. Raw type applied. Possible types: com.google.protobuf.UnknownFieldSchema<?, ?>, com.google.protobuf.UnknownFieldSchema<UT, UB> */
    /* JADX DEBUG: Type inference failed for r2v0. Raw type applied. Possible types: com.google.protobuf.ExtensionSchema<?>, com.google.protobuf.ExtensionSchema<ET extends com.google.protobuf.FieldSet$FieldDescriptorLite<ET>> */
    @Override // com.google.protobuf.Schema
    public void mergeFrom(T t, Reader reader, ExtensionRegistryLite extensionRegistryLite) throws IOException {
        if (extensionRegistryLite != null) {
            mergeFromHelper(this.unknownFieldSchema, this.extensionSchema, t, reader, extensionRegistryLite);
            return;
        }
        throw null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r17v0, resolved type: com.google.protobuf.UnknownFieldSchema<UT, UB> */
    /* JADX WARN: Multi-variable type inference failed */
    private <UT, UB, ET extends FieldSet.FieldDescriptorLite<ET>> void mergeFromHelper(UnknownFieldSchema<UT, UB> unknownFieldSchema, ExtensionSchema<ET> extensionSchema, T t, Reader reader, ExtensionRegistryLite extensionRegistryLite) throws IOException {
        Object obj = null;
        FieldSet<ET> fieldSet = null;
        while (true) {
            try {
                int fieldNumber = reader.getFieldNumber();
                int positionForFieldNumber = positionForFieldNumber(fieldNumber);
                if (positionForFieldNumber >= 0) {
                    int typeAndOffsetAt = typeAndOffsetAt(positionForFieldNumber);
                    try {
                        switch (type(typeAndOffsetAt)) {
                            case 0:
                                UnsafeUtil.putDouble(t, offset(typeAndOffsetAt), reader.readDouble());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 1:
                                UnsafeUtil.putFloat(t, offset(typeAndOffsetAt), reader.readFloat());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 2:
                                UnsafeUtil.putLong(t, offset(typeAndOffsetAt), reader.readInt64());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 3:
                                UnsafeUtil.putLong(t, offset(typeAndOffsetAt), reader.readUInt64());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 4:
                                UnsafeUtil.putInt(t, offset(typeAndOffsetAt), reader.readInt32());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 5:
                                UnsafeUtil.putLong(t, offset(typeAndOffsetAt), reader.readFixed64());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 6:
                                UnsafeUtil.putInt(t, offset(typeAndOffsetAt), reader.readFixed32());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 7:
                                UnsafeUtil.putBoolean(t, offset(typeAndOffsetAt), reader.readBool());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 8:
                                readString(t, typeAndOffsetAt, reader);
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 9:
                                if (!isFieldPresent(t, positionForFieldNumber)) {
                                    UnsafeUtil.putObject(t, offset(typeAndOffsetAt), reader.readMessageBySchemaWithCheck(getMessageFieldSchema(positionForFieldNumber), extensionRegistryLite));
                                    setFieldPresent(t, positionForFieldNumber);
                                    break;
                                } else {
                                    UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Internal.mergeMessage(UnsafeUtil.getObject(t, offset(typeAndOffsetAt)), reader.readMessageBySchemaWithCheck(getMessageFieldSchema(positionForFieldNumber), extensionRegistryLite)));
                                    continue;
                                }
                            case 10:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), reader.readBytes());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 11:
                                UnsafeUtil.putInt(t, offset(typeAndOffsetAt), reader.readUInt32());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 12:
                                int readEnum = reader.readEnum();
                                Internal.EnumVerifier enumFieldVerifier = getEnumFieldVerifier(positionForFieldNumber);
                                if (enumFieldVerifier != null) {
                                    if (!enumFieldVerifier.isInRange(readEnum)) {
                                        obj = SchemaUtil.storeUnknownEnum(fieldNumber, readEnum, obj, unknownFieldSchema);
                                        continue;
                                    }
                                }
                                UnsafeUtil.putInt(t, offset(typeAndOffsetAt), readEnum);
                                setFieldPresent(t, positionForFieldNumber);
                                break;
                            case 13:
                                UnsafeUtil.putInt(t, offset(typeAndOffsetAt), reader.readSFixed32());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 14:
                                UnsafeUtil.putLong(t, offset(typeAndOffsetAt), reader.readSFixed64());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 15:
                                UnsafeUtil.putInt(t, offset(typeAndOffsetAt), reader.readSInt32());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 16:
                                UnsafeUtil.putLong(t, offset(typeAndOffsetAt), reader.readSInt64());
                                setFieldPresent(t, positionForFieldNumber);
                                continue;
                            case 17:
                                if (!isFieldPresent(t, positionForFieldNumber)) {
                                    UnsafeUtil.putObject(t, offset(typeAndOffsetAt), reader.readGroupBySchemaWithCheck(getMessageFieldSchema(positionForFieldNumber), extensionRegistryLite));
                                    setFieldPresent(t, positionForFieldNumber);
                                    break;
                                } else {
                                    UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Internal.mergeMessage(UnsafeUtil.getObject(t, offset(typeAndOffsetAt)), reader.readGroupBySchemaWithCheck(getMessageFieldSchema(positionForFieldNumber), extensionRegistryLite)));
                                    continue;
                                }
                            case 18:
                                reader.readDoubleList(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 19:
                                reader.readFloatList(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 20:
                                reader.readInt64List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 21:
                                reader.readUInt64List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 22:
                                reader.readInt32List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 23:
                                reader.readFixed64List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 24:
                                reader.readFixed32List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 25:
                                reader.readBoolList(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 26:
                                readStringList(t, typeAndOffsetAt, reader);
                                continue;
                            case 27:
                                readMessageList(t, typeAndOffsetAt, reader, getMessageFieldSchema(positionForFieldNumber), extensionRegistryLite);
                                continue;
                            case 28:
                                reader.readBytesList(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 29:
                                reader.readUInt32List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 30:
                                List<Integer> mutableListAt = this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt));
                                reader.readEnumList(mutableListAt);
                                obj = SchemaUtil.filterUnknownEnumList(fieldNumber, mutableListAt, getEnumFieldVerifier(positionForFieldNumber), obj, unknownFieldSchema);
                                continue;
                            case 31:
                                reader.readSFixed32List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 32:
                                reader.readSFixed64List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 33:
                                reader.readSInt32List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 34:
                                reader.readSInt64List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 35:
                                reader.readDoubleList(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 36:
                                reader.readFloatList(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 37:
                                reader.readInt64List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 38:
                                reader.readUInt64List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 39:
                                reader.readInt32List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 40:
                                reader.readFixed64List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 41:
                                reader.readFixed32List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 42:
                                reader.readBoolList(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 43:
                                reader.readUInt32List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 44:
                                List<Integer> mutableListAt2 = this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt));
                                reader.readEnumList(mutableListAt2);
                                obj = SchemaUtil.filterUnknownEnumList(fieldNumber, mutableListAt2, getEnumFieldVerifier(positionForFieldNumber), obj, unknownFieldSchema);
                                continue;
                            case 45:
                                reader.readSFixed32List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 46:
                                reader.readSFixed64List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 47:
                                reader.readSInt32List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 48:
                                reader.readSInt64List(this.listFieldSchema.mutableListAt(t, offset(typeAndOffsetAt)));
                                continue;
                            case 49:
                                readGroupList(t, offset(typeAndOffsetAt), reader, getMessageFieldSchema(positionForFieldNumber), extensionRegistryLite);
                                continue;
                            case 50:
                                mergeMap(t, positionForFieldNumber, getMapFieldDefaultEntry(positionForFieldNumber), extensionRegistryLite, reader);
                                continue;
                            case 51:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Double.valueOf(reader.readDouble()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_begin /* 52 */:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Float.valueOf(reader.readFloat()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_end /* 53 */:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Long.valueOf(reader.readInt64()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 54:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Long.valueOf(reader.readUInt64()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 55:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Integer.valueOf(reader.readInt32()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 56:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Long.valueOf(reader.readFixed64()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 57:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Integer.valueOf(reader.readFixed32()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 58:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Boolean.valueOf(reader.readBool()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 59:
                                readString(t, typeAndOffsetAt, reader);
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 60:
                                if (isOneofPresent(t, fieldNumber, positionForFieldNumber)) {
                                    UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Internal.mergeMessage(UnsafeUtil.getObject(t, offset(typeAndOffsetAt)), reader.readMessageBySchemaWithCheck(getMessageFieldSchema(positionForFieldNumber), extensionRegistryLite)));
                                } else {
                                    UnsafeUtil.putObject(t, offset(typeAndOffsetAt), reader.readMessageBySchemaWithCheck(getMessageFieldSchema(positionForFieldNumber), extensionRegistryLite));
                                    setFieldPresent(t, positionForFieldNumber);
                                }
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 61:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), reader.readBytes());
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 62:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Integer.valueOf(reader.readUInt32()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 63:
                                int readEnum2 = reader.readEnum();
                                Internal.EnumVerifier enumFieldVerifier2 = getEnumFieldVerifier(positionForFieldNumber);
                                if (enumFieldVerifier2 != null) {
                                    if (!enumFieldVerifier2.isInRange(readEnum2)) {
                                        obj = SchemaUtil.storeUnknownEnum(fieldNumber, readEnum2, obj, unknownFieldSchema);
                                        continue;
                                    }
                                }
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Integer.valueOf(readEnum2));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                break;
                            case 64:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Integer.valueOf(reader.readSFixed32()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 65:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Long.valueOf(reader.readSFixed64()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 66:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Integer.valueOf(reader.readSInt32()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 67:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), Long.valueOf(reader.readSInt64()));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            case 68:
                                UnsafeUtil.putObject(t, offset(typeAndOffsetAt), reader.readGroupBySchemaWithCheck(getMessageFieldSchema(positionForFieldNumber), extensionRegistryLite));
                                setOneofPresent(t, fieldNumber, positionForFieldNumber);
                                continue;
                            default:
                                if (obj == null) {
                                    obj = unknownFieldSchema.newBuilder();
                                }
                                if (unknownFieldSchema.mergeOneFieldFrom(obj, reader)) {
                                    continue;
                                } else if (obj != null) {
                                    return;
                                } else {
                                    return;
                                }
                        }
                    } catch (InvalidProtocolBufferException.InvalidWireTypeException unused) {
                        if (!unknownFieldSchema.shouldDiscardUnknownFields(reader)) {
                            if (obj == null) {
                                obj = unknownFieldSchema.getBuilderFromMessage(t);
                            }
                            if (!unknownFieldSchema.mergeOneFieldFrom(obj, reader)) {
                                for (int i = this.checkInitializedCount; i < this.repeatedFieldOffsetStart; i++) {
                                    obj = filterMapUnknownEnumValues(t, this.intArray[i], obj, unknownFieldSchema);
                                }
                                if (obj != null) {
                                    unknownFieldSchema.setBuilderToMessage(t, obj);
                                    return;
                                }
                                return;
                            }
                        } else if (!reader.skipField()) {
                            for (int i2 = this.checkInitializedCount; i2 < this.repeatedFieldOffsetStart; i2++) {
                                obj = filterMapUnknownEnumValues(t, this.intArray[i2], obj, unknownFieldSchema);
                            }
                            if (obj != null) {
                                unknownFieldSchema.setBuilderToMessage(t, obj);
                                return;
                            }
                            return;
                        }
                    }
                } else if (fieldNumber == Integer.MAX_VALUE) {
                    for (int i3 = this.checkInitializedCount; i3 < this.repeatedFieldOffsetStart; i3++) {
                        obj = filterMapUnknownEnumValues(t, this.intArray[i3], obj, unknownFieldSchema);
                    }
                    if (obj != null) {
                        unknownFieldSchema.setBuilderToMessage(t, obj);
                        return;
                    }
                    return;
                } else {
                    Object findExtensionByNumber = !this.hasExtensions ? null : extensionSchema.findExtensionByNumber(extensionRegistryLite, this.defaultInstance, fieldNumber);
                    if (findExtensionByNumber != null) {
                        if (fieldSet == null) {
                            fieldSet = extensionSchema.getMutableExtensions(t);
                        }
                        obj = extensionSchema.parseExtension(reader, findExtensionByNumber, extensionRegistryLite, fieldSet, obj, unknownFieldSchema);
                    } else if (!unknownFieldSchema.shouldDiscardUnknownFields(reader)) {
                        if (obj == null) {
                            obj = unknownFieldSchema.getBuilderFromMessage(t);
                        }
                        if (unknownFieldSchema.mergeOneFieldFrom(obj, reader)) {
                        }
                    } else if (reader.skipField()) {
                    }
                }
            } finally {
                for (int i4 = this.checkInitializedCount; i4 < this.repeatedFieldOffsetStart; i4++) {
                    obj = filterMapUnknownEnumValues(t, this.intArray[i4], obj, unknownFieldSchema);
                }
                if (obj != null) {
                    unknownFieldSchema.setBuilderToMessage(t, obj);
                }
            }
        }
        for (int i5 = this.checkInitializedCount; i5 < this.repeatedFieldOffsetStart; i5++) {
            obj = filterMapUnknownEnumValues(t, this.intArray[i5], obj, unknownFieldSchema);
        }
        if (obj != null) {
            unknownFieldSchema.setBuilderToMessage(t, obj);
        }
    }

    static UnknownFieldSetLite getMutableUnknownFields(Object obj) {
        GeneratedMessageLite generatedMessageLite = (GeneratedMessageLite) obj;
        UnknownFieldSetLite unknownFieldSetLite = generatedMessageLite.unknownFields;
        if (unknownFieldSetLite != UnknownFieldSetLite.getDefaultInstance()) {
            return unknownFieldSetLite;
        }
        UnknownFieldSetLite newInstance = UnknownFieldSetLite.newInstance();
        generatedMessageLite.unknownFields = newInstance;
        return newInstance;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.google.protobuf.MessageSchema$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$google$protobuf$WireFormat$FieldType;

        static {
            int[] iArr = new int[WireFormat.FieldType.values().length];
            $SwitchMap$com$google$protobuf$WireFormat$FieldType = iArr;
            try {
                iArr[WireFormat.FieldType.BOOL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.BYTES.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.DOUBLE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.FIXED32.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SFIXED32.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.FIXED64.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SFIXED64.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.FLOAT.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.ENUM.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.INT32.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.UINT32.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.INT64.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.UINT64.ordinal()] = 13;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.MESSAGE.ordinal()] = 14;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SINT32.ordinal()] = 15;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.SINT64.ordinal()] = 16;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$com$google$protobuf$WireFormat$FieldType[WireFormat.FieldType.STRING.ordinal()] = 17;
            } catch (NoSuchFieldError unused17) {
            }
        }
    }

    private int decodeMapEntryValue(byte[] bArr, int i, int i2, WireFormat.FieldType fieldType, Class<?> cls, ArrayDecoders.Registers registers) throws IOException {
        switch (AnonymousClass1.$SwitchMap$com$google$protobuf$WireFormat$FieldType[fieldType.ordinal()]) {
            case 1:
                int decodeVarint64 = ArrayDecoders.decodeVarint64(bArr, i, registers);
                registers.object1 = Boolean.valueOf(registers.long1 != 0);
                return decodeVarint64;
            case 2:
                return ArrayDecoders.decodeBytes(bArr, i, registers);
            case 3:
                registers.object1 = Double.valueOf(ArrayDecoders.decodeDouble(bArr, i));
                return i + 8;
            case 4:
            case 5:
                registers.object1 = Integer.valueOf(ArrayDecoders.decodeFixed32(bArr, i));
                return i + 4;
            case 6:
            case 7:
                registers.object1 = Long.valueOf(ArrayDecoders.decodeFixed64(bArr, i));
                return i + 8;
            case 8:
                registers.object1 = Float.valueOf(ArrayDecoders.decodeFloat(bArr, i));
                return i + 4;
            case 9:
            case 10:
            case 11:
                int decodeVarint32 = ArrayDecoders.decodeVarint32(bArr, i, registers);
                registers.object1 = Integer.valueOf(registers.int1);
                return decodeVarint32;
            case 12:
            case 13:
                int decodeVarint642 = ArrayDecoders.decodeVarint64(bArr, i, registers);
                registers.object1 = Long.valueOf(registers.long1);
                return decodeVarint642;
            case 14:
                return ArrayDecoders.decodeMessageField(Protobuf.getInstance().schemaFor((Class) cls), bArr, i, i2, registers);
            case 15:
                int decodeVarint322 = ArrayDecoders.decodeVarint32(bArr, i, registers);
                registers.object1 = Integer.valueOf(CodedInputStream.decodeZigZag32(registers.int1));
                return decodeVarint322;
            case 16:
                int decodeVarint643 = ArrayDecoders.decodeVarint64(bArr, i, registers);
                registers.object1 = Long.valueOf(CodedInputStream.decodeZigZag64(registers.long1));
                return decodeVarint643;
            case 17:
                return ArrayDecoders.decodeStringRequireUtf8(bArr, i, registers);
            default:
                throw new RuntimeException("unsupported field type.");
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:36:0x001d */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:37:0x001d */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r19v0, types: [java.util.Map, java.util.Map<K, V>] */
    /* JADX WARN: Type inference failed for: r13v1, types: [java.lang.Object] */
    /* JADX WARN: Type inference failed for: r12v1, types: [java.lang.Object] */
    /* JADX WARN: Type inference failed for: r13v2 */
    /* JADX WARN: Type inference failed for: r12v2 */
    /* JADX WARN: Type inference failed for: r13v3, types: [java.lang.Object] */
    /* JADX WARN: Type inference failed for: r12v3, types: [java.lang.Object] */
    /* JADX WARN: Type inference failed for: r1v10, types: [int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private <K, V> int decodeMapEntry(byte[] r15, int r16, int r17, com.google.protobuf.MapEntryLite.Metadata<K, V> r18, java.util.Map<K, V> r19, com.google.protobuf.ArrayDecoders.Registers r20) throws java.io.IOException {
        /*
            r14 = this;
            r7 = r15
            r8 = r17
            r9 = r18
            r0 = r16
            r10 = r20
            int r0 = com.google.protobuf.ArrayDecoders.decodeVarint32(r15, r0, r10)
            int r1 = r10.int1
            if (r1 < 0) goto L_0x0081
            int r2 = r8 - r0
            if (r1 > r2) goto L_0x0081
            int r11 = r0 + r1
            K r1 = r9.defaultKey
            V r2 = r9.defaultValue
            r12 = r1
            r13 = r2
        L_0x001d:
            if (r0 >= r11) goto L_0x0074
            int r1 = r0 + 1
            byte r0 = r7[r0]
            if (r0 >= 0) goto L_0x002e
            int r0 = com.google.protobuf.ArrayDecoders.decodeVarint32(r0, r15, r1, r10)
            int r1 = r10.int1
            r2 = r0
            r0 = r1
            goto L_0x002f
        L_0x002e:
            r2 = r1
        L_0x002f:
            int r1 = r0 >>> 3
            r3 = r0 & 7
            r4 = 1
            if (r1 == r4) goto L_0x0057
            r4 = 2
            if (r1 == r4) goto L_0x003a
            goto L_0x006f
        L_0x003a:
            com.google.protobuf.WireFormat$FieldType r1 = r9.valueType
            int r1 = r1.getWireType()
            if (r3 != r1) goto L_0x006f
            com.google.protobuf.WireFormat$FieldType r4 = r9.valueType
            V r0 = r9.defaultValue
            java.lang.Class r5 = r0.getClass()
            r0 = r14
            r1 = r15
            r3 = r17
            r6 = r20
            int r0 = r0.decodeMapEntryValue(r1, r2, r3, r4, r5, r6)
            java.lang.Object r13 = r10.object1
            goto L_0x001d
        L_0x0057:
            com.google.protobuf.WireFormat$FieldType r1 = r9.keyType
            int r1 = r1.getWireType()
            if (r3 != r1) goto L_0x006f
            com.google.protobuf.WireFormat$FieldType r4 = r9.keyType
            r5 = 0
            r0 = r14
            r1 = r15
            r3 = r17
            r6 = r20
            int r0 = r0.decodeMapEntryValue(r1, r2, r3, r4, r5, r6)
            java.lang.Object r12 = r10.object1
            goto L_0x001d
        L_0x006f:
            int r0 = com.google.protobuf.ArrayDecoders.skipField(r0, r15, r2, r8, r10)
            goto L_0x001d
        L_0x0074:
            if (r0 != r11) goto L_0x007c
            r0 = r19
            r0.put(r12, r13)
            return r11
        L_0x007c:
            com.google.protobuf.InvalidProtocolBufferException r0 = com.google.protobuf.InvalidProtocolBufferException.parseFailure()
            throw r0
        L_0x0081:
            com.google.protobuf.InvalidProtocolBufferException r0 = com.google.protobuf.InvalidProtocolBufferException.truncatedMessage()
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.MessageSchema.decodeMapEntry(byte[], int, int, com.google.protobuf.MapEntryLite$Metadata, java.util.Map, com.google.protobuf.ArrayDecoders$Registers):int");
    }

    private int parseRepeatedField(T t, byte[] bArr, int i, int i2, int i3, int i4, int i5, int i6, long j, int i7, long j2, ArrayDecoders.Registers registers) throws IOException {
        int i8;
        Internal.ProtobufList protobufList = (Internal.ProtobufList) UNSAFE.getObject(t, j2);
        if (!protobufList.isModifiable()) {
            int size = protobufList.size();
            protobufList = protobufList.mutableCopyWithCapacity(size == 0 ? 10 : size * 2);
            UNSAFE.putObject(t, j2, protobufList);
        }
        switch (i7) {
            case 18:
            case 35:
                if (i5 == 2) {
                    return ArrayDecoders.decodePackedDoubleList(bArr, i, protobufList, registers);
                }
                if (i5 == 1) {
                    return ArrayDecoders.decodeDoubleList(i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 19:
            case 36:
                if (i5 == 2) {
                    return ArrayDecoders.decodePackedFloatList(bArr, i, protobufList, registers);
                }
                if (i5 == 5) {
                    return ArrayDecoders.decodeFloatList(i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 20:
            case 21:
            case 37:
            case 38:
                if (i5 == 2) {
                    return ArrayDecoders.decodePackedVarint64List(bArr, i, protobufList, registers);
                }
                if (i5 == 0) {
                    return ArrayDecoders.decodeVarint64List(i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 22:
            case 29:
            case 39:
            case 43:
                if (i5 == 2) {
                    return ArrayDecoders.decodePackedVarint32List(bArr, i, protobufList, registers);
                }
                if (i5 == 0) {
                    return ArrayDecoders.decodeVarint32List(i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 23:
            case 32:
            case 40:
            case 46:
                if (i5 == 2) {
                    return ArrayDecoders.decodePackedFixed64List(bArr, i, protobufList, registers);
                }
                if (i5 == 1) {
                    return ArrayDecoders.decodeFixed64List(i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 24:
            case 31:
            case 41:
            case 45:
                if (i5 == 2) {
                    return ArrayDecoders.decodePackedFixed32List(bArr, i, protobufList, registers);
                }
                if (i5 == 5) {
                    return ArrayDecoders.decodeFixed32List(i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 25:
            case 42:
                if (i5 == 2) {
                    return ArrayDecoders.decodePackedBoolList(bArr, i, protobufList, registers);
                }
                if (i5 == 0) {
                    return ArrayDecoders.decodeBoolList(i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 26:
                if (i5 == 2) {
                    if ((j & 536870912) == 0) {
                        return ArrayDecoders.decodeStringList(i3, bArr, i, i2, protobufList, registers);
                    }
                    return ArrayDecoders.decodeStringListRequireUtf8(i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 27:
                if (i5 == 2) {
                    return ArrayDecoders.decodeMessageList(getMessageFieldSchema(i6), i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 28:
                if (i5 == 2) {
                    return ArrayDecoders.decodeBytesList(i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 30:
            case 44:
                if (i5 == 2) {
                    i8 = ArrayDecoders.decodePackedVarint32List(bArr, i, protobufList, registers);
                } else if (i5 == 0) {
                    i8 = ArrayDecoders.decodeVarint32List(i3, bArr, i, i2, protobufList, registers);
                }
                T t2 = t;
                UnknownFieldSetLite unknownFieldSetLite = t2.unknownFields;
                if (unknownFieldSetLite == UnknownFieldSetLite.getDefaultInstance()) {
                    unknownFieldSetLite = null;
                }
                UnknownFieldSetLite unknownFieldSetLite2 = (UnknownFieldSetLite) SchemaUtil.filterUnknownEnumList(i4, (List<Integer>) protobufList, getEnumFieldVerifier(i6), unknownFieldSetLite, (UnknownFieldSchema<UT, UnknownFieldSetLite>) this.unknownFieldSchema);
                if (unknownFieldSetLite2 != null) {
                    t2.unknownFields = unknownFieldSetLite2;
                }
                return i8;
            case 33:
            case 47:
                if (i5 == 2) {
                    return ArrayDecoders.decodePackedSInt32List(bArr, i, protobufList, registers);
                }
                if (i5 == 0) {
                    return ArrayDecoders.decodeSInt32List(i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 34:
            case 48:
                if (i5 == 2) {
                    return ArrayDecoders.decodePackedSInt64List(bArr, i, protobufList, registers);
                }
                if (i5 == 0) {
                    return ArrayDecoders.decodeSInt64List(i3, bArr, i, i2, protobufList, registers);
                }
                break;
            case 49:
                if (i5 == 3) {
                    return ArrayDecoders.decodeGroupList(getMessageFieldSchema(i6), i3, bArr, i, i2, protobufList, registers);
                }
                break;
        }
        return i;
    }

    private <K, V> int parseMapField(T t, byte[] bArr, int i, int i2, int i3, long j, ArrayDecoders.Registers registers) throws IOException {
        Unsafe unsafe = UNSAFE;
        Object mapFieldDefaultEntry = getMapFieldDefaultEntry(i3);
        Object object = unsafe.getObject(t, j);
        if (this.mapFieldSchema.isImmutable(object)) {
            Object newMapField = this.mapFieldSchema.newMapField(mapFieldDefaultEntry);
            this.mapFieldSchema.mergeFrom(newMapField, object);
            unsafe.putObject(t, j, newMapField);
            object = newMapField;
        }
        return decodeMapEntry(bArr, i, i2, this.mapFieldSchema.forMapMetadata(mapFieldDefaultEntry), this.mapFieldSchema.forMutableMapData(object), registers);
    }

    private int parseOneofField(T t, byte[] bArr, int i, int i2, int i3, int i4, int i5, int i6, int i7, long j, int i8, ArrayDecoders.Registers registers) throws IOException {
        Unsafe unsafe = UNSAFE;
        long j2 = (long) (this.buffer[i8 + 2] & 1048575);
        switch (i7) {
            case 51:
                if (i5 == 1) {
                    unsafe.putObject(t, j, Double.valueOf(ArrayDecoders.decodeDouble(bArr, i)));
                    int i9 = i + 8;
                    unsafe.putInt(t, j2, i4);
                    return i9;
                }
                break;
            case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_begin /* 52 */:
                if (i5 == 5) {
                    unsafe.putObject(t, j, Float.valueOf(ArrayDecoders.decodeFloat(bArr, i)));
                    int i10 = i + 4;
                    unsafe.putInt(t, j2, i4);
                    return i10;
                }
                break;
            case R$styleable.ConstraintLayout_Layout_layout_constraintGuide_end /* 53 */:
            case 54:
                if (i5 == 0) {
                    int decodeVarint64 = ArrayDecoders.decodeVarint64(bArr, i, registers);
                    unsafe.putObject(t, j, Long.valueOf(registers.long1));
                    unsafe.putInt(t, j2, i4);
                    return decodeVarint64;
                }
                break;
            case 55:
            case 62:
                if (i5 == 0) {
                    int decodeVarint32 = ArrayDecoders.decodeVarint32(bArr, i, registers);
                    unsafe.putObject(t, j, Integer.valueOf(registers.int1));
                    unsafe.putInt(t, j2, i4);
                    return decodeVarint32;
                }
                break;
            case 56:
            case 65:
                if (i5 == 1) {
                    unsafe.putObject(t, j, Long.valueOf(ArrayDecoders.decodeFixed64(bArr, i)));
                    int i11 = i + 8;
                    unsafe.putInt(t, j2, i4);
                    return i11;
                }
                break;
            case 57:
            case 64:
                if (i5 == 5) {
                    unsafe.putObject(t, j, Integer.valueOf(ArrayDecoders.decodeFixed32(bArr, i)));
                    int i12 = i + 4;
                    unsafe.putInt(t, j2, i4);
                    return i12;
                }
                break;
            case 58:
                if (i5 == 0) {
                    int decodeVarint642 = ArrayDecoders.decodeVarint64(bArr, i, registers);
                    unsafe.putObject(t, j, Boolean.valueOf(registers.long1 != 0));
                    unsafe.putInt(t, j2, i4);
                    return decodeVarint642;
                }
                break;
            case 59:
                if (i5 == 2) {
                    int decodeVarint322 = ArrayDecoders.decodeVarint32(bArr, i, registers);
                    int i13 = registers.int1;
                    if (i13 == 0) {
                        unsafe.putObject(t, j, "");
                    } else if ((i6 & 536870912) == 0 || Utf8.isValidUtf8(bArr, decodeVarint322, decodeVarint322 + i13)) {
                        unsafe.putObject(t, j, new String(bArr, decodeVarint322, i13, Internal.UTF_8));
                        decodeVarint322 += i13;
                    } else {
                        throw InvalidProtocolBufferException.invalidUtf8();
                    }
                    unsafe.putInt(t, j2, i4);
                    return decodeVarint322;
                }
                break;
            case 60:
                if (i5 == 2) {
                    int decodeMessageField = ArrayDecoders.decodeMessageField(getMessageFieldSchema(i8), bArr, i, i2, registers);
                    Object object = unsafe.getInt(t, j2) == i4 ? unsafe.getObject(t, j) : null;
                    if (object == null) {
                        unsafe.putObject(t, j, registers.object1);
                    } else {
                        unsafe.putObject(t, j, Internal.mergeMessage(object, registers.object1));
                    }
                    unsafe.putInt(t, j2, i4);
                    return decodeMessageField;
                }
                break;
            case 61:
                if (i5 == 2) {
                    int decodeBytes = ArrayDecoders.decodeBytes(bArr, i, registers);
                    unsafe.putObject(t, j, registers.object1);
                    unsafe.putInt(t, j2, i4);
                    return decodeBytes;
                }
                break;
            case 63:
                if (i5 == 0) {
                    int decodeVarint323 = ArrayDecoders.decodeVarint32(bArr, i, registers);
                    int i14 = registers.int1;
                    Internal.EnumVerifier enumFieldVerifier = getEnumFieldVerifier(i8);
                    if (enumFieldVerifier == null || enumFieldVerifier.isInRange(i14)) {
                        unsafe.putObject(t, j, Integer.valueOf(i14));
                        unsafe.putInt(t, j2, i4);
                    } else {
                        getMutableUnknownFields(t).storeField(i3, Long.valueOf((long) i14));
                    }
                    return decodeVarint323;
                }
                break;
            case 66:
                if (i5 == 0) {
                    int decodeVarint324 = ArrayDecoders.decodeVarint32(bArr, i, registers);
                    unsafe.putObject(t, j, Integer.valueOf(CodedInputStream.decodeZigZag32(registers.int1)));
                    unsafe.putInt(t, j2, i4);
                    return decodeVarint324;
                }
                break;
            case 67:
                if (i5 == 0) {
                    int decodeVarint643 = ArrayDecoders.decodeVarint64(bArr, i, registers);
                    unsafe.putObject(t, j, Long.valueOf(CodedInputStream.decodeZigZag64(registers.long1)));
                    unsafe.putInt(t, j2, i4);
                    return decodeVarint643;
                }
                break;
            case 68:
                if (i5 == 3) {
                    int decodeGroupField = ArrayDecoders.decodeGroupField(getMessageFieldSchema(i8), bArr, i, i2, (i3 & -8) | 4, registers);
                    Object object2 = unsafe.getInt(t, j2) == i4 ? unsafe.getObject(t, j) : null;
                    if (object2 == null) {
                        unsafe.putObject(t, j, registers.object1);
                    } else {
                        unsafe.putObject(t, j, Internal.mergeMessage(object2, registers.object1));
                    }
                    unsafe.putInt(t, j2, i4);
                    return decodeGroupField;
                }
                break;
        }
        return i;
    }

    private Schema getMessageFieldSchema(int i) {
        int i2 = (i / 3) * 2;
        Schema schema = (Schema) this.objects[i2];
        if (schema != null) {
            return schema;
        }
        Schema<T> schemaFor = Protobuf.getInstance().schemaFor((Class) ((Class) this.objects[i2 + 1]));
        this.objects[i2] = schemaFor;
        return schemaFor;
    }

    private Object getMapFieldDefaultEntry(int i) {
        return this.objects[(i / 3) * 2];
    }

    private Internal.EnumVerifier getEnumFieldVerifier(int i) {
        return (Internal.EnumVerifier) this.objects[((i / 3) * 2) + 1];
    }

    /* JADX DEBUG: Type inference failed for r6v3. Raw type applied. Possible types: com.google.protobuf.UnknownFieldSchema<?, ?>, com.google.protobuf.UnknownFieldSchema<UT, UB> */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x0359, code lost:
        if (r0 != r4) goto L_0x035b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x03a2, code lost:
        if (r0 != r15) goto L_0x035b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:127:0x03c5, code lost:
        if (r0 != r15) goto L_0x035b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x03c8, code lost:
        r2 = r0;
        r8 = r18;
        r0 = r35;
     */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x03cf A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x03e4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int parseProto2Message(T r31, byte[] r32, int r33, int r34, int r35, com.google.protobuf.ArrayDecoders.Registers r36) throws java.io.IOException {
        /*
        // Method dump skipped, instructions count: 1172
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.MessageSchema.parseProto2Message(java.lang.Object, byte[], int, int, int, com.google.protobuf.ArrayDecoders$Registers):int");
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v12, types: [int] */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01de, code lost:
        if (r0 != r8) goto L_0x023f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x020c, code lost:
        if (r0 != r15) goto L_0x023f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x022b, code lost:
        if (r0 != r15) goto L_0x023f;
     */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int parseProto3Message(T r28, byte[] r29, int r30, int r31, com.google.protobuf.ArrayDecoders.Registers r32) throws java.io.IOException {
        /*
        // Method dump skipped, instructions count: 642
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.protobuf.MessageSchema.parseProto3Message(java.lang.Object, byte[], int, int, com.google.protobuf.ArrayDecoders$Registers):int");
    }

    @Override // com.google.protobuf.Schema
    public void mergeFrom(T t, byte[] bArr, int i, int i2, ArrayDecoders.Registers registers) throws IOException {
        if (this.proto3) {
            parseProto3Message(t, bArr, i, i2, registers);
        } else {
            parseProto2Message(t, bArr, i, i2, 0, registers);
        }
    }

    @Override // com.google.protobuf.Schema
    public void makeImmutable(T t) {
        int i;
        int i2 = this.checkInitializedCount;
        while (true) {
            i = this.repeatedFieldOffsetStart;
            if (i2 >= i) {
                break;
            }
            long offset = offset(typeAndOffsetAt(this.intArray[i2]));
            Object object = UnsafeUtil.getObject(t, offset);
            if (object != null) {
                UnsafeUtil.putObject(t, offset, this.mapFieldSchema.toImmutable(object));
            }
            i2++;
        }
        int length = this.intArray.length;
        while (i < length) {
            this.listFieldSchema.makeImmutableListAt(t, (long) this.intArray[i]);
            i++;
        }
        this.unknownFieldSchema.makeImmutable(t);
        if (this.hasExtensions) {
            this.extensionSchema.makeImmutable(t);
        }
    }

    private final <K, V> void mergeMap(Object obj, int i, Object obj2, ExtensionRegistryLite extensionRegistryLite, Reader reader) throws IOException {
        long offset = offset(typeAndOffsetAt(i));
        Object object = UnsafeUtil.getObject(obj, offset);
        if (object == null) {
            object = this.mapFieldSchema.newMapField(obj2);
            UnsafeUtil.putObject(obj, offset, object);
        } else if (this.mapFieldSchema.isImmutable(object)) {
            Object newMapField = this.mapFieldSchema.newMapField(obj2);
            this.mapFieldSchema.mergeFrom(newMapField, object);
            UnsafeUtil.putObject(obj, offset, newMapField);
            object = newMapField;
        }
        reader.readMap(this.mapFieldSchema.forMutableMapData(object), this.mapFieldSchema.forMapMetadata(obj2), extensionRegistryLite);
    }

    /* JADX DEBUG: Type inference failed for r3v0. Raw type applied. Possible types: java.util.Map<?, ?>, java.util.Map<K, V> */
    private final <UT, UB> UB filterMapUnknownEnumValues(Object obj, int i, UB ub, UnknownFieldSchema<UT, UB> unknownFieldSchema) {
        Internal.EnumVerifier enumFieldVerifier;
        int numberAt = numberAt(i);
        Object object = UnsafeUtil.getObject(obj, offset(typeAndOffsetAt(i)));
        return (object == null || (enumFieldVerifier = getEnumFieldVerifier(i)) == null) ? ub : (UB) filterUnknownEnumMap(i, numberAt, this.mapFieldSchema.forMutableMapData(object), enumFieldVerifier, ub, unknownFieldSchema);
    }

    private final <K, V, UT, UB> UB filterUnknownEnumMap(int i, int i2, Map<K, V> map, Internal.EnumVerifier enumVerifier, UB ub, UnknownFieldSchema<UT, UB> unknownFieldSchema) {
        MapEntryLite.Metadata<?, ?> forMapMetadata = this.mapFieldSchema.forMapMetadata(getMapFieldDefaultEntry(i));
        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, V> next = it.next();
            if (!enumVerifier.isInRange(next.getValue().intValue())) {
                if (ub == null) {
                    ub = unknownFieldSchema.newBuilder();
                }
                ByteString.CodedBuilder newCodedBuilder = ByteString.newCodedBuilder(MapEntryLite.computeSerializedSize(forMapMetadata, next.getKey(), next.getValue()));
                try {
                    MapEntryLite.writeTo(newCodedBuilder.getCodedOutput(), forMapMetadata, next.getKey(), next.getValue());
                    unknownFieldSchema.addLengthDelimited(ub, i2, newCodedBuilder.build());
                    it.remove();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return ub;
    }

    @Override // com.google.protobuf.Schema
    public final boolean isInitialized(T t) {
        int i;
        int i2 = -1;
        int i3 = 0;
        for (int i4 = 0; i4 < this.checkInitializedCount; i4++) {
            int i5 = this.intArray[i4];
            int numberAt = numberAt(i5);
            int typeAndOffsetAt = typeAndOffsetAt(i5);
            if (!this.proto3) {
                int i6 = this.buffer[i5 + 2];
                int i7 = 1048575 & i6;
                i = 1 << (i6 >>> 20);
                if (i7 != i2) {
                    i3 = UNSAFE.getInt(t, (long) i7);
                    i2 = i7;
                }
            } else {
                i = 0;
            }
            if (isRequired(typeAndOffsetAt) && !isFieldPresent(t, i5, i3, i)) {
                return false;
            }
            int type = type(typeAndOffsetAt);
            if (type != 9 && type != 17) {
                if (type != 27) {
                    if (type == 60 || type == 68) {
                        if (isOneofPresent(t, numberAt, i5) && !isInitialized(t, typeAndOffsetAt, getMessageFieldSchema(i5))) {
                            return false;
                        }
                    } else if (type != 49) {
                        if (type == 50 && !isMapInitialized(t, typeAndOffsetAt, i5)) {
                            return false;
                        }
                    }
                }
                if (!isListInitialized(t, typeAndOffsetAt, i5)) {
                    return false;
                }
            } else if (isFieldPresent(t, i5, i3, i) && !isInitialized(t, typeAndOffsetAt, getMessageFieldSchema(i5))) {
                return false;
            }
        }
        return !this.hasExtensions || this.extensionSchema.getExtensions(t).isInitialized();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: com.google.protobuf.Schema */
    /* JADX WARN: Multi-variable type inference failed */
    private static boolean isInitialized(Object obj, int i, Schema schema) {
        return schema.isInitialized(UnsafeUtil.getObject(obj, offset(i)));
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v1, resolved type: com.google.protobuf.Schema */
    /* JADX WARN: Multi-variable type inference failed */
    private <N> boolean isListInitialized(Object obj, int i, int i2) {
        List list = (List) UnsafeUtil.getObject(obj, offset(i));
        if (list.isEmpty()) {
            return true;
        }
        Schema messageFieldSchema = getMessageFieldSchema(i2);
        for (int i3 = 0; i3 < list.size(); i3++) {
            if (!messageFieldSchema.isInitialized(list.get(i3))) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v5 */
    /* JADX WARN: Type inference failed for: r3v7, types: [com.google.protobuf.Schema] */
    /* JADX WARN: Type inference failed for: r3v11 */
    private boolean isMapInitialized(T t, int i, int i2) {
        Map<?, ?> forMapData = this.mapFieldSchema.forMapData(UnsafeUtil.getObject(t, offset(i)));
        if (forMapData.isEmpty()) {
            return true;
        }
        if (this.mapFieldSchema.forMapMetadata(getMapFieldDefaultEntry(i2)).valueType.getJavaType() != WireFormat.JavaType.MESSAGE) {
            return true;
        }
        Schema<T> schema = 0;
        for (Object obj : forMapData.values()) {
            if (schema == null) {
                schema = Protobuf.getInstance().schemaFor((Class) obj.getClass());
            }
            boolean isInitialized = schema.isInitialized(obj);
            schema = schema;
            if (!isInitialized) {
                return false;
            }
        }
        return true;
    }

    private void writeString(int i, Object obj, Writer writer) throws IOException {
        if (obj instanceof String) {
            writer.writeString(i, (String) obj);
        } else {
            writer.writeBytes(i, (ByteString) obj);
        }
    }

    private void readString(Object obj, int i, Reader reader) throws IOException {
        if (isEnforceUtf8(i)) {
            UnsafeUtil.putObject(obj, offset(i), reader.readStringRequireUtf8());
        } else if (this.lite) {
            UnsafeUtil.putObject(obj, offset(i), reader.readString());
        } else {
            UnsafeUtil.putObject(obj, offset(i), reader.readBytes());
        }
    }

    private void readStringList(Object obj, int i, Reader reader) throws IOException {
        if (isEnforceUtf8(i)) {
            reader.readStringListRequireUtf8(this.listFieldSchema.mutableListAt(obj, offset(i)));
        } else {
            reader.readStringList(this.listFieldSchema.mutableListAt(obj, offset(i)));
        }
    }

    private <E> void readMessageList(Object obj, int i, Reader reader, Schema<E> schema, ExtensionRegistryLite extensionRegistryLite) throws IOException {
        reader.readMessageList(this.listFieldSchema.mutableListAt(obj, offset(i)), schema, extensionRegistryLite);
    }

    private <E> void readGroupList(Object obj, long j, Reader reader, Schema<E> schema, ExtensionRegistryLite extensionRegistryLite) throws IOException {
        reader.readGroupList(this.listFieldSchema.mutableListAt(obj, j), schema, extensionRegistryLite);
    }

    private int numberAt(int i) {
        return this.buffer[i];
    }

    private int typeAndOffsetAt(int i) {
        return this.buffer[i + 1];
    }

    private int presenceMaskAndOffsetAt(int i) {
        return this.buffer[i + 2];
    }

    private static <T> double doubleAt(T t, long j) {
        return UnsafeUtil.getDouble(t, j);
    }

    private static <T> float floatAt(T t, long j) {
        return UnsafeUtil.getFloat(t, j);
    }

    private static <T> int intAt(T t, long j) {
        return UnsafeUtil.getInt(t, j);
    }

    private static <T> long longAt(T t, long j) {
        return UnsafeUtil.getLong(t, j);
    }

    private static <T> boolean booleanAt(T t, long j) {
        return UnsafeUtil.getBoolean(t, j);
    }

    private static <T> double oneofDoubleAt(T t, long j) {
        return ((Double) UnsafeUtil.getObject(t, j)).doubleValue();
    }

    private static <T> float oneofFloatAt(T t, long j) {
        return ((Float) UnsafeUtil.getObject(t, j)).floatValue();
    }

    private static <T> int oneofIntAt(T t, long j) {
        return ((Integer) UnsafeUtil.getObject(t, j)).intValue();
    }

    private static <T> long oneofLongAt(T t, long j) {
        return ((Long) UnsafeUtil.getObject(t, j)).longValue();
    }

    private static <T> boolean oneofBooleanAt(T t, long j) {
        return ((Boolean) UnsafeUtil.getObject(t, j)).booleanValue();
    }

    private boolean arePresentForEquals(T t, T t2, int i) {
        return isFieldPresent(t, i) == isFieldPresent(t2, i);
    }

    private boolean isFieldPresent(T t, int i, int i2, int i3) {
        if (this.proto3) {
            return isFieldPresent(t, i);
        }
        return (i2 & i3) != 0;
    }

    private boolean isFieldPresent(T t, int i) {
        if (this.proto3) {
            int typeAndOffsetAt = typeAndOffsetAt(i);
            long offset = offset(typeAndOffsetAt);
            switch (type(typeAndOffsetAt)) {
                case 0:
                    return UnsafeUtil.getDouble(t, offset) != 0.0d;
                case 1:
                    return UnsafeUtil.getFloat(t, offset) != 0.0f;
                case 2:
                    return UnsafeUtil.getLong(t, offset) != 0;
                case 3:
                    return UnsafeUtil.getLong(t, offset) != 0;
                case 4:
                    return UnsafeUtil.getInt(t, offset) != 0;
                case 5:
                    return UnsafeUtil.getLong(t, offset) != 0;
                case 6:
                    return UnsafeUtil.getInt(t, offset) != 0;
                case 7:
                    return UnsafeUtil.getBoolean(t, offset);
                case 8:
                    Object object = UnsafeUtil.getObject(t, offset);
                    if (object instanceof String) {
                        return !((String) object).isEmpty();
                    }
                    if (object instanceof ByteString) {
                        return !ByteString.EMPTY.equals(object);
                    }
                    throw new IllegalArgumentException();
                case 9:
                    return UnsafeUtil.getObject(t, offset) != null;
                case 10:
                    return !ByteString.EMPTY.equals(UnsafeUtil.getObject(t, offset));
                case 11:
                    return UnsafeUtil.getInt(t, offset) != 0;
                case 12:
                    return UnsafeUtil.getInt(t, offset) != 0;
                case 13:
                    return UnsafeUtil.getInt(t, offset) != 0;
                case 14:
                    return UnsafeUtil.getLong(t, offset) != 0;
                case 15:
                    return UnsafeUtil.getInt(t, offset) != 0;
                case 16:
                    return UnsafeUtil.getLong(t, offset) != 0;
                case 17:
                    return UnsafeUtil.getObject(t, offset) != null;
                default:
                    throw new IllegalArgumentException();
            }
        } else {
            int presenceMaskAndOffsetAt = presenceMaskAndOffsetAt(i);
            return (UnsafeUtil.getInt(t, (long) (presenceMaskAndOffsetAt & 1048575)) & (1 << (presenceMaskAndOffsetAt >>> 20))) != 0;
        }
    }

    private void setFieldPresent(T t, int i) {
        if (!this.proto3) {
            int presenceMaskAndOffsetAt = presenceMaskAndOffsetAt(i);
            long j = (long) (presenceMaskAndOffsetAt & 1048575);
            UnsafeUtil.putInt(t, j, UnsafeUtil.getInt(t, j) | (1 << (presenceMaskAndOffsetAt >>> 20)));
        }
    }

    private boolean isOneofPresent(T t, int i, int i2) {
        return UnsafeUtil.getInt(t, (long) (presenceMaskAndOffsetAt(i2) & 1048575)) == i;
    }

    private boolean isOneofCaseEqual(T t, T t2, int i) {
        long presenceMaskAndOffsetAt = (long) (presenceMaskAndOffsetAt(i) & 1048575);
        return UnsafeUtil.getInt(t, presenceMaskAndOffsetAt) == UnsafeUtil.getInt(t2, presenceMaskAndOffsetAt);
    }

    private void setOneofPresent(T t, int i, int i2) {
        UnsafeUtil.putInt(t, (long) (presenceMaskAndOffsetAt(i2) & 1048575), i);
    }

    private int positionForFieldNumber(int i) {
        if (i < this.minFieldNumber || i > this.maxFieldNumber) {
            return -1;
        }
        return slowPositionForFieldNumber(i, 0);
    }

    private int positionForFieldNumber(int i, int i2) {
        if (i < this.minFieldNumber || i > this.maxFieldNumber) {
            return -1;
        }
        return slowPositionForFieldNumber(i, i2);
    }

    private int slowPositionForFieldNumber(int i, int i2) {
        int length = (this.buffer.length / 3) - 1;
        while (i2 <= length) {
            int i3 = (length + i2) >>> 1;
            int i4 = i3 * 3;
            int numberAt = numberAt(i4);
            if (i == numberAt) {
                return i4;
            }
            if (i < numberAt) {
                length = i3 - 1;
            } else {
                i2 = i3 + 1;
            }
        }
        return -1;
    }
}
