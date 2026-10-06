import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Implementor implements Impler {

    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        validate(aClass);

        // Для простого
        if (!aClass.isInterface()) {
            throw new ImplerException("Only interfaces supported");
        }

        // Имена и путь
        String packageName = aClass.getPackageName();
        String simpleName = aClass.getSimpleName();
        String implClassName = simpleName + "Impl";

        // колекция методов
        Map<String, Method> mapAllMethodsForClass = MethodCollector.collect(aClass);

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
                writer.write(buildTypeDeclaration(aClass, implClassName));

                // методы по одному
                for (Method m : mapAllMethodsForClass.values()) {
                    writer.write(MethodGenerator.generateTxtMethodForFile(m));
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
    private static String buildTypeDeclaration(Class<?> aClass, String implClassName) {
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
                || Modifier.isFinal(aClass.getModifiers())
                || Modifier.isPrivate(aClass.getModifiers())
                || aClass == Object.class) {
            throw new ImplerException("Cannot implement: " + TypeUtils.typeName(aClass));
        }
    }
}