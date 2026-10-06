package util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;


/**
 * Фильтрация методов
 * оставляет только те которые НЕ реализованы в иерархии или в самом классе
 */
public final class OverrideResolver {

    private OverrideResolver() {}

    /**
     * Удаляет методы которые уже реализованы из мапа всех методов
     * @param methodMap мапа всех методов из которой и удаляем
     * @param token основной класс
     */
    public static void removeImplemented(Map<String, Method> methodMap, Class<?> token) {
        methodMap.keySet().removeIf(sig -> isImplemented(token, sig));
    }


    /**
     * Проверка на то реализован ли метод в классе или его родителях
     * @param token сам класс
     * @param signature имя метода/сигнатуры
     * @return true - значит реализован
     *         false - не реализован
     */
    private static boolean isImplemented(Class<?> token, String signature) {
        for (Class<?> current = token; current != null; current = current.getSuperclass()) {
            for (Method m : current.getDeclaredMethods()) {
                if (m.isSynthetic()) continue;
                if (TypeUtils.getFullNameMethod(m).equals(signature)) {
                    return !Modifier.isAbstract(m.getModifiers());
                }
            }
        }
        return false;
    }
}
