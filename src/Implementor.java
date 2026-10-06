import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Implementor implements Impler {

    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        validate(aClass);

        // Имена и путь
        String packageName = aClass.getPackageName();
        String simpleName = aClass.getSimpleName();
        String implClassName = simpleName + "Impl";

        // Колекция конструкторов
        List<Constructor<?>> listConstructors = ConstructorCollector.collect(aClass);

        // колекция методов
        Map<String, Method> mapAllMethodsForClass = MethodCollector.collect(aClass);
        removeImplemented(mapAllMethodsForClass, aClass);
        validateMethodTypes(mapAllMethodsForClass);

        // Путь нового файла
        Path file = path.resolve(packageName.replace(".", "/"))
                .resolve(implClassName + ".java");


        try {
            Files.createDirectories(file.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                // пакет
                if (!packageName.isEmpty()) {
                    writer.write("package " + packageName + ";\n\n");
                }

                // загловочная строка
                writer.write(buildHeader(aClass, implClassName));

                // методы по одному
                for (Method m : mapAllMethodsForClass.values()) {
                    writer.write(MethodGenerator.generateTxtMethodForFile(m));
                }

                // конструкторы по 1 если это класс
                for (Constructor<?> c : listConstructors) {
                    writer.write(ConstructorGenerator.generateConstructor(c, implClassName));
                }

                // закрывающиеся скобки
                writer.write("}\n");
            }
        } catch (IOException e) {
            throw new ImplerException("Cannot write " + file, e);
        }
    }


    /**
     * Создание заголовочной строки для нового файла
     * @param aClass сам класс
     * @param implClassName имя класса для нового файла
     * @return строка заголовочного файла
     */
    private static String buildHeader(Class<?> aClass, String implClassName) {
        StringBuilder string = new StringBuilder();
        string.append("public class ");
        string.append(implClassName).append(" ");


        if (aClass.isInterface()) {
            string.append("implements ").append(TypeUtils.typeName(aClass));
        } else {
            string.append("extends ").append(TypeUtils.typeName(aClass));
        }

        string.append(" {\n");
        return string.toString();
    }

    /**
     * Проверка что переданный класс не примитив
     * @param aClass класс
     * @throws ImplerException ошибка имлементра из утилиты
     */
    private static void validate(Class<?> aClass) throws ImplerException {
        if (aClass.isPrimitive()
                || aClass.isArray()
                || aClass.isEnum()
                || aClass.isRecord()
                || aClass == Object.class
                || aClass == Enum.class
                || aClass == Record.class
                || Modifier.isFinal(aClass.getModifiers())
                || Modifier.isPrivate(aClass.getModifiers())) {
            throw new ImplerException("Cannot implement: " + TypeUtils.typeName(aClass));
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
                throw new ImplerException("No accessible constructors: " + TypeUtils.typeName(aClass));
            }
        }
    }


    /**
     * Удаляет методы которые уже реализованы из мапа всех методов
     * @param methodMap мапа всех методов их которой и удаляем
     * @param token основной класс
     */
    private static void removeImplemented(Map<String, Method> methodMap, Class<?> token) {
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


    /**
     * Проверка валидности параметров и возвращаемого типа у метода
     * @param methodMap мапа всех методов
     * @throws ImplerException если параметр приватный и метод нельзя реализовать
     */
    private static void validateMethodTypes(Map<String, Method> methodMap) throws ImplerException {
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
     * Проверка что класс не приватный
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
            throw new ImplerException("Cannot reference private type: " + TypeUtils.typeName(type));
        }
    }


}