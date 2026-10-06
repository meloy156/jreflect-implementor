import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;


public final class MethodCollector {

    private MethodCollector() {}

    /**
     * СОбирает все абстрактные методы которые должен реализовать класс
     * @param token класс методы которые нужны
     * @return result мапа со всеми методами без повторов
     */
    public static Map<String, Method> collect(Class<?> token) {
        Map<String, Method> result = new LinkedHashMap<>();
        collect(token, result);
        return result;
    }


    private static void collect(Class<?> aClass, Map<String, Method> result) {
        if (aClass == null || aClass == Object.class) return;

        for (Method metod : aClass.getDeclaredMethods()) {
            int mods = metod.getModifiers();
            if (!Modifier.isAbstract(mods)) continue;
            if (Modifier.isStatic(mods)) continue;
            if (Modifier.isPrivate(mods)) continue;
            if (metod.isSynthetic()) continue;
            if (metod.isDefault()) continue;

            result.putIfAbsent(TypeUtils.getFullNameMethod(metod), metod);
        }

        collect(aClass.getSuperclass(), result);

        for (Class<?> item: aClass.getInterfaces()) {
            collect(item, result);
        }
    }


}
