package kotlin.jvm.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import kotlin.Function;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.functions.Function5;
import kotlin.jvm.internal.markers.KMappedMarker;
import kotlin.jvm.internal.markers.KMutableMap;
public class TypeIntrinsics {
    private static <T extends Throwable> T sanitizeStackTrace(T t) {
        Intrinsics.sanitizeStackTrace(t, TypeIntrinsics.class.getName());
        return t;
    }

    public static void throwCce(Object obj, String str) {
        String name = obj == null ? "null" : obj.getClass().getName();
        throwCce(name + " cannot be cast to " + str);
        throw null;
    }

    public static void throwCce(String str) {
        throwCce(new ClassCastException(str));
        throw null;
    }

    public static ClassCastException throwCce(ClassCastException classCastException) {
        sanitizeStackTrace(classCastException);
        throw classCastException;
    }

    public static Iterable asMutableIterable(Object obj) {
        if (!(obj instanceof KMappedMarker)) {
            return castToIterable(obj);
        }
        throwCce(obj, "kotlin.collections.MutableIterable");
        throw null;
    }

    public static Iterable castToIterable(Object obj) {
        try {
            return (Iterable) obj;
        } catch (ClassCastException e) {
            throwCce(e);
            throw null;
        }
    }

    public static Collection asMutableCollection(Object obj) {
        if (!(obj instanceof KMappedMarker)) {
            return castToCollection(obj);
        }
        throwCce(obj, "kotlin.collections.MutableCollection");
        throw null;
    }

    public static Collection castToCollection(Object obj) {
        try {
            return (Collection) obj;
        } catch (ClassCastException e) {
            throwCce(e);
            throw null;
        }
    }

    public static Set asMutableSet(Object obj) {
        if (!(obj instanceof KMappedMarker)) {
            return castToSet(obj);
        }
        throwCce(obj, "kotlin.collections.MutableSet");
        throw null;
    }

    public static Set castToSet(Object obj) {
        try {
            return (Set) obj;
        } catch (ClassCastException e) {
            throwCce(e);
            throw null;
        }
    }

    public static Map asMutableMap(Object obj) {
        if (!(obj instanceof KMappedMarker) || (obj instanceof KMutableMap)) {
            return castToMap(obj);
        }
        throwCce(obj, "kotlin.collections.MutableMap");
        throw null;
    }

    public static Map castToMap(Object obj) {
        try {
            return (Map) obj;
        } catch (ClassCastException e) {
            throwCce(e);
            throw null;
        }
    }

    public static int getFunctionArity(Object obj) {
        if (obj instanceof FunctionBase) {
            return ((FunctionBase) obj).getArity();
        }
        if (obj instanceof Function0) {
            return 0;
        }
        if (obj instanceof Function1) {
            return 1;
        }
        if (obj instanceof Function2) {
            return 2;
        }
        if (obj instanceof Function3) {
            return 3;
        }
        return obj instanceof Function5 ? 5 : -1;
    }

    public static boolean isFunctionOfArity(Object obj, int i) {
        return (obj instanceof Function) && getFunctionArity(obj) == i;
    }

    public static Object beforeCheckcastToFunctionOfArity(Object obj, int i) {
        if (obj == null || isFunctionOfArity(obj, i)) {
            return obj;
        }
        throwCce(obj, "kotlin.jvm.functions.Function" + i);
        throw null;
    }
}
