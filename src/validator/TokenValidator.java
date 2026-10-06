package validator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

/**
 * Првоеряет что переданный тип реально можно реалзивать
 * не примитив, не массив, не enum и тд
 */
public final class TokenValidator {

    private TokenValidator() {}

    /**
     * @param aClass класс для проверки
     * @throws ImplerException если нельзя реализовать
     */
    public static void validate(Class<?> aClass) throws ImplerException {
        if (aClass.isPrimitive()
                || aClass.isArray()
                || aClass.isEnum()
                || aClass.isRecord()
                || aClass == Object.class
                || aClass == Enum.class
                || aClass == Record.class
                || Modifier.isFinal(aClass.getModifiers())
                || Modifier.isPrivate(aClass.getModifiers())) {
            throw new ImplerException("Cannot implement: " + util.TypeUtils.typeName(aClass));
        }

        if (!aClass.isInterface()) {
            Constructor<?>[] constructors = aClass.getDeclaredConstructors();
            boolean hasAccessible = false;
            for (Constructor<?> c : constructors) {
                if (!Modifier.isPrivate(c.getModifiers())) {
                    hasAccessible = true;
                    break;
                }
            }
            if (!hasAccessible) {
                throw new ImplerException("No accessible constructors: " + util.TypeUtils.typeName(aClass));
            }
        }
    }
}
