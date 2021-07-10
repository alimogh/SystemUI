package kotlin.jvm.internal;

import kotlin.reflect.KClass;
import kotlin.reflect.KDeclarationContainer;
import kotlin.reflect.KFunction;
import kotlin.reflect.KProperty0;
import kotlin.reflect.KProperty1;
public class Reflection {
    private static final ReflectionFactory factory;

    static {
        ReflectionFactory reflectionFactory = null;
        try {
            reflectionFactory = (ReflectionFactory) Class.forName("kotlin.reflect.jvm.internal.ReflectionFactoryImpl").newInstance();
        } catch (ClassCastException | ClassNotFoundException | IllegalAccessException | InstantiationException unused) {
        }
        if (reflectionFactory == null) {
            reflectionFactory = new ReflectionFactory();
        }
        factory = reflectionFactory;
    }

    public static KDeclarationContainer getOrCreateKotlinPackage(Class cls, String str) {
        return factory.getOrCreateKotlinPackage(cls, str);
    }

    public static KClass getOrCreateKotlinClass(Class cls) {
        return factory.getOrCreateKotlinClass(cls);
    }

    public static String renderLambdaToString(Lambda lambda) {
        return factory.renderLambdaToString(lambda);
    }

    public static String renderLambdaToString(FunctionBase functionBase) {
        return factory.renderLambdaToString(functionBase);
    }

    public static KFunction function(FunctionReference functionReference) {
        factory.function(functionReference);
        return functionReference;
    }

    public static KProperty0 property0(PropertyReference0 propertyReference0) {
        factory.property0(propertyReference0);
        return propertyReference0;
    }

    public static KProperty1 property1(PropertyReference1 propertyReference1) {
        factory.property1(propertyReference1);
        return propertyReference1;
    }
}
