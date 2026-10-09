package collector;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Утилитный класс для сбора абстрактных методов, которые должны быть
 * реализованы в генерируемом классе.
 */
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


    /**
     * Рекурсивно обходит иерархию классов и интерфейсов и добавляет
     * абстрактные методы в карту.
     *
     * @param aClass текущий класс или интерфейс
     * @param result карта, в которую добавляются методы
     */
    private static void collect(Class<?> aClass, Map<String, Method> result) {
        if (aClass == null || aClass == Object.class) return;

        for (Method metod : aClass.getDeclaredMethods()) {
            int mods = metod.getModifiers();
            if (!Modifier.isAbstract(mods)) continue;
            if (Modifier.isStatic(mods)) continue;
            if (Modifier.isPrivate(mods)) continue;
            if (metod.isSynthetic()) continue;
            if (metod.isDefault()) continue;

            result.putIfAbsent(util.TypeUtils.getFullNameMethod(metod), metod);
        }

        collect(aClass.getSuperclass(), result);

        for (Class<?> item: aClass.getInterfaces()) {
            collect(item, result);
        }
    }


}
