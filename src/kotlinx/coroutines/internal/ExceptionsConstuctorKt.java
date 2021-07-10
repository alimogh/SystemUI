package kotlinx.coroutines.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.comparisons.ComparisonsKt__ComparisonsKt;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CopyableThrowable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/* compiled from: ExceptionsConstuctor.kt */
public final class ExceptionsConstuctorKt {
    private static final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private static final WeakHashMap<Class<? extends Throwable>, Function1<Throwable, Throwable>> exceptionCtors = new WeakHashMap<>();
    private static final int throwableFields = fieldsCountOrDefault(Throwable.class, -1);

    @Nullable
    public static final <E extends Throwable> E tryCopyException(@NotNull E e) {
        Object obj;
        Intrinsics.checkParameterIsNotNull(e, "exception");
        Object obj2 = null;
        if (e instanceof CopyableThrowable) {
            try {
                Result.Companion companion = Result.Companion;
                obj = ((CopyableThrowable) e).createCopy();
                Result.m31constructorimpl(obj);
            } catch (Throwable th) {
                Result.Companion companion2 = Result.Companion;
                obj = ResultKt.createFailure(th);
                Result.m31constructorimpl(obj);
            }
            if (!Result.m35isFailureimpl(obj)) {
                obj2 = obj;
            }
            return (E) ((Throwable) obj2);
        }
        ReentrantReadWriteLock.ReadLock readLock = cacheLock.readLock();
        readLock.lock();
        try {
            Function1<Throwable, Throwable> function1 = exceptionCtors.get(e.getClass());
            if (function1 != null) {
                return (E) function1.invoke(e);
            }
            int i = 0;
            if (throwableFields != fieldsCountOrDefault(e.getClass(), 0)) {
                ReentrantReadWriteLock reentrantReadWriteLock = cacheLock;
                ReentrantReadWriteLock.ReadLock readLock2 = reentrantReadWriteLock.readLock();
                int readHoldCount = reentrantReadWriteLock.getWriteHoldCount() == 0 ? reentrantReadWriteLock.getReadHoldCount() : 0;
                for (int i2 = 0; i2 < readHoldCount; i2++) {
                    readLock2.unlock();
                }
                ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
                writeLock.lock();
                try {
                    exceptionCtors.put(e.getClass(), ExceptionsConstuctorKt$tryCopyException$4$1.INSTANCE);
                    Unit unit = Unit.INSTANCE;
                    return null;
                } finally {
                    while (i < readHoldCount) {
                        readLock2.lock();
                        i++;
                    }
                    writeLock.unlock();
                }
            } else {
                Constructor<?>[] constructors = e.getClass().getConstructors();
                Intrinsics.checkExpressionValueIsNotNull(constructors, "exception.javaClass.constructors");
                Function1<Throwable, Throwable> function12 = null;
                for (Constructor constructor : ArraysKt___ArraysKt.sortedWith(constructors, new Comparator<T>() { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$tryCopyException$$inlined$sortedByDescending$1
                    @Override // java.util.Comparator
                    public final int compare(T t, T t2) {
                        T t3 = t2;
                        Intrinsics.checkExpressionValueIsNotNull(t3, "it");
                        Integer valueOf = Integer.valueOf(t3.getParameterTypes().length);
                        T t4 = t;
                        Intrinsics.checkExpressionValueIsNotNull(t4, "it");
                        return ComparisonsKt__ComparisonsKt.compareValues(valueOf, Integer.valueOf(t4.getParameterTypes().length));
                    }
                })) {
                    Intrinsics.checkExpressionValueIsNotNull(constructor, "constructor");
                    function12 = createConstructor(constructor);
                    if (function12 != null) {
                        break;
                    }
                }
                ReentrantReadWriteLock reentrantReadWriteLock2 = cacheLock;
                ReentrantReadWriteLock.ReadLock readLock3 = reentrantReadWriteLock2.readLock();
                int readHoldCount2 = reentrantReadWriteLock2.getWriteHoldCount() == 0 ? reentrantReadWriteLock2.getReadHoldCount() : 0;
                for (int i3 = 0; i3 < readHoldCount2; i3++) {
                    readLock3.unlock();
                }
                ReentrantReadWriteLock.WriteLock writeLock2 = reentrantReadWriteLock2.writeLock();
                writeLock2.lock();
                try {
                    exceptionCtors.put(e.getClass(), function12 != null ? function12 : ExceptionsConstuctorKt$tryCopyException$5$1.INSTANCE);
                    Unit unit2 = Unit.INSTANCE;
                    if (function12 != null) {
                        return (E) function12.invoke(e);
                    }
                    return null;
                } finally {
                    while (i < readHoldCount2) {
                        readLock3.lock();
                        i++;
                    }
                    writeLock2.unlock();
                }
            }
        } finally {
            readLock.unlock();
        }
    }

    private static final Function1<Throwable, Throwable> createConstructor(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        int length = parameterTypes.length;
        if (length == 0) {
            return new Function1<Throwable, Throwable>(constructor) { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$createConstructor$$inlined$safeCtor$4
                final /* synthetic */ Constructor $constructor$inlined;

                {
                    this.$constructor$inlined = r1;
                }

                @Nullable
                public final Throwable invoke(@NotNull Throwable th) {
                    Throwable th2;
                    Intrinsics.checkParameterIsNotNull(th, "e");
                    try {
                        Result.Companion companion = Result.Companion;
                        Object newInstance = this.$constructor$inlined.newInstance(new Object[0]);
                        if (newInstance != null) {
                            Throwable th3 = (Throwable) newInstance;
                            th3.initCause(th);
                            Result.m31constructorimpl(th3);
                            th2 = th3;
                            boolean r2 = Result.m35isFailureimpl(th2);
                            Object obj = th2;
                            if (r2) {
                                obj = null;
                            }
                            return (Throwable) obj;
                        }
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.Throwable");
                    } catch (Throwable th4) {
                        Result.Companion companion2 = Result.Companion;
                        Object createFailure = ResultKt.createFailure(th4);
                        Result.m31constructorimpl(createFailure);
                        th2 = createFailure;
                    }
                }
            };
        }
        if (length == 1) {
            Class<?> cls = parameterTypes[0];
            if (Intrinsics.areEqual(cls, Throwable.class)) {
                return new Function1<Throwable, Throwable>(constructor) { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$createConstructor$$inlined$safeCtor$2
                    final /* synthetic */ Constructor $constructor$inlined;

                    {
                        this.$constructor$inlined = r1;
                    }

                    @Nullable
                    public final Throwable invoke(@NotNull Throwable th) {
                        Object obj;
                        Intrinsics.checkParameterIsNotNull(th, "e");
                        try {
                            Result.Companion companion = Result.Companion;
                            Object newInstance = this.$constructor$inlined.newInstance(th);
                            if (newInstance != null) {
                                obj = (Throwable) newInstance;
                                Result.m31constructorimpl(obj);
                                if (Result.m35isFailureimpl(obj)) {
                                    obj = null;
                                }
                                return (Throwable) obj;
                            }
                            throw new TypeCastException("null cannot be cast to non-null type kotlin.Throwable");
                        } catch (Throwable th2) {
                            Result.Companion companion2 = Result.Companion;
                            obj = ResultKt.createFailure(th2);
                            Result.m31constructorimpl(obj);
                        }
                    }
                };
            }
            if (Intrinsics.areEqual(cls, String.class)) {
                return new Function1<Throwable, Throwable>(constructor) { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$createConstructor$$inlined$safeCtor$3
                    final /* synthetic */ Constructor $constructor$inlined;

                    {
                        this.$constructor$inlined = r1;
                    }

                    @Nullable
                    public final Throwable invoke(@NotNull Throwable th) {
                        Throwable th2;
                        Intrinsics.checkParameterIsNotNull(th, "e");
                        try {
                            Result.Companion companion = Result.Companion;
                            Object newInstance = this.$constructor$inlined.newInstance(th.getMessage());
                            if (newInstance != null) {
                                Throwable th3 = (Throwable) newInstance;
                                th3.initCause(th);
                                Result.m31constructorimpl(th3);
                                th2 = th3;
                                boolean r4 = Result.m35isFailureimpl(th2);
                                Object obj = th2;
                                if (r4) {
                                    obj = null;
                                }
                                return (Throwable) obj;
                            }
                            throw new TypeCastException("null cannot be cast to non-null type kotlin.Throwable");
                        } catch (Throwable th4) {
                            Result.Companion companion2 = Result.Companion;
                            Object createFailure = ResultKt.createFailure(th4);
                            Result.m31constructorimpl(createFailure);
                            th2 = createFailure;
                        }
                    }
                };
            }
            return null;
        } else if (length == 2 && Intrinsics.areEqual(parameterTypes[0], String.class) && Intrinsics.areEqual(parameterTypes[1], Throwable.class)) {
            return new Function1<Throwable, Throwable>(constructor) { // from class: kotlinx.coroutines.internal.ExceptionsConstuctorKt$createConstructor$$inlined$safeCtor$1
                final /* synthetic */ Constructor $constructor$inlined;

                {
                    this.$constructor$inlined = r1;
                }

                @Nullable
                public final Throwable invoke(@NotNull Throwable th) {
                    Object obj;
                    Intrinsics.checkParameterIsNotNull(th, "e");
                    try {
                        Result.Companion companion = Result.Companion;
                        Object newInstance = this.$constructor$inlined.newInstance(th.getMessage(), th);
                        if (newInstance != null) {
                            obj = (Throwable) newInstance;
                            Result.m31constructorimpl(obj);
                            if (Result.m35isFailureimpl(obj)) {
                                obj = null;
                            }
                            return (Throwable) obj;
                        }
                        throw new TypeCastException("null cannot be cast to non-null type kotlin.Throwable");
                    } catch (Throwable th2) {
                        Result.Companion companion2 = Result.Companion;
                        obj = ResultKt.createFailure(th2);
                        Result.m31constructorimpl(obj);
                    }
                }
            };
        } else {
            return null;
        }
    }

    private static final int fieldsCountOrDefault(@NotNull Class<?> cls, int i) {
        Integer num;
        JvmClassMappingKt.getKotlinClass(cls);
        try {
            Result.Companion companion = Result.Companion;
            num = Integer.valueOf(fieldsCount$default(cls, 0, 1, null));
            Result.m31constructorimpl(num);
        } catch (Throwable th) {
            Result.Companion companion2 = Result.Companion;
            num = ResultKt.createFailure(th);
            Result.m31constructorimpl(num);
        }
        Integer valueOf = Integer.valueOf(i);
        if (Result.m35isFailureimpl(num)) {
            num = valueOf;
        }
        return ((Number) num).intValue();
    }

    static /* synthetic */ int fieldsCount$default(Class cls, int i, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            i = 0;
        }
        return fieldsCount(cls, i);
    }

    private static final int fieldsCount(@NotNull Class<?> cls, int i) {
        do {
            Field[] declaredFields = cls.getDeclaredFields();
            Intrinsics.checkExpressionValueIsNotNull(declaredFields, "declaredFields");
            int i2 = 0;
            for (Field field : declaredFields) {
                Intrinsics.checkExpressionValueIsNotNull(field, "it");
                if (!Modifier.isStatic(field.getModifiers())) {
                    i2++;
                }
            }
            i += i2;
            cls = cls.getSuperclass();
        } while (cls != null);
        return i;
    }
}
