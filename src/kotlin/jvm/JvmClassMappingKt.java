package kotlin.jvm;

import kotlin.TypeCastException;
import kotlin.jvm.internal.ClassBasedDeclarationContainer;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;
/* compiled from: JvmClassMapping.kt */
public final class JvmClassMappingKt {
    @NotNull
    public static final <T> Class<T> getJavaClass(@NotNull KClass<T> kClass) {
        Intrinsics.checkParameterIsNotNull(kClass, "$this$java");
        Class<T> cls = (Class<T>) ((ClassBasedDeclarationContainer) kClass).getJClass();
        if (cls != null) {
            return cls;
        }
        throw new TypeCastException("null cannot be cast to non-null type java.lang.Class<T>");
    }

    @NotNull
    public static final <T> Class<T> getJavaObjectType(@NotNull KClass<T> kClass) {
        Intrinsics.checkParameterIsNotNull(kClass, "$this$javaObjectType");
        Class<T> cls = (Class<T>) ((ClassBasedDeclarationContainer) kClass).getJClass();
        if (cls.isPrimitive()) {
            String name = cls.getName();
            if (name != null) {
                switch (name.hashCode()) {
                    case -1325958191:
                        if (name.equals("double")) {
                            cls = (Class<T>) Double.class;
                            break;
                        }
                        break;
                    case 104431:
                        if (name.equals("int")) {
                            cls = (Class<T>) Integer.class;
                            break;
                        }
                        break;
                    case 3039496:
                        if (name.equals("byte")) {
                            cls = (Class<T>) Byte.class;
                            break;
                        }
                        break;
                    case 3052374:
                        if (name.equals("char")) {
                            cls = (Class<T>) Character.class;
                            break;
                        }
                        break;
                    case 3327612:
                        if (name.equals("long")) {
                            cls = (Class<T>) Long.class;
                            break;
                        }
                        break;
                    case 3625364:
                        if (name.equals("void")) {
                            cls = (Class<T>) Void.class;
                            break;
                        }
                        break;
                    case 64711720:
                        if (name.equals("boolean")) {
                            cls = (Class<T>) Boolean.class;
                            break;
                        }
                        break;
                    case 97526364:
                        if (name.equals("float")) {
                            cls = (Class<T>) Float.class;
                            break;
                        }
                        break;
                    case 109413500:
                        if (name.equals("short")) {
                            cls = (Class<T>) Short.class;
                            break;
                        }
                        break;
                }
            }
            if (cls != null) {
                return cls;
            }
            throw new TypeCastException("null cannot be cast to non-null type java.lang.Class<T>");
        } else if (cls != null) {
            return cls;
        } else {
            throw new TypeCastException("null cannot be cast to non-null type java.lang.Class<T>");
        }
    }

    @NotNull
    public static final <T> KClass<T> getKotlinClass(@NotNull Class<T> cls) {
        Intrinsics.checkParameterIsNotNull(cls, "$this$kotlin");
        return Reflection.getOrCreateKotlinClass(cls);
    }
}
