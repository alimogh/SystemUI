package kotlin.collections;

import java.util.Arrays;
import java.util.List;
/* access modifiers changed from: package-private */
public class ArraysUtilJVM {
    static <T> List<T> asList(T[] tArr) {
        return Arrays.asList(tArr);
    }
}
