package validator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import info.kgeorgiy.java.advanced.implementor.ImplerException;


/**
 * Проверяет, что все типы в сигнатурах методов доступны
 * для использования в исходном коде (не private).
 */
public final class MethodValidator {

    private MethodValidator() {}


    /**
     * Проверка валидности параметров и возвращаемого типа у метода
     * @param methodMap мапа всех методов
     * @throws ImplerException если параметр приватный и метод нельзя реализовать
     */
    public static void validateMethodTypes(Map<String, Method> methodMap) throws ImplerException {
        for (Method m : methodMap.values()) {
            for (Class<?> type : m.getParameterTypes()) {
                checkAccessible(type);
            }
            checkAccessible(m.getReturnType());
            for (Class<?> type : m.getExceptionTypes()) {
                checkAccessible(type);
            }
        }
    }


    /**
     * Проверка что тип не приватный
     * @param type класс, передаем либо параметр либо возвращаемый тип методы
     * @throws ImplerException если все таки приватный
     */
    private static void checkAccessible(Class<?> type) throws ImplerException {
        if (type.isPrimitive()) return;

        if (type.isArray()) {
            checkAccessible(type.getComponentType());
            return;
        }

        if (Modifier.isPrivate(type.getModifiers())) {
            throw new ImplerException("Cannot reference private type: " + util.TypeUtils.typeName(type));
        }
    }

}
