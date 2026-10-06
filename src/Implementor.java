import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import util.HeadBuilder;
import util.OverrideResolver;
import validator.MethodValidator;
import validator.TokenValidator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Implementor implements Impler {

    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        TokenValidator.validate(aClass);

        // Имена и путь
        String packageName = aClass.getPackageName();
        String simpleName = aClass.getSimpleName();
        String implClassName = simpleName + "Impl";

        // Колекция конструкторов
        List<Constructor<?>> listConstructors = collector.ConstructorCollector.collect(aClass);

        // колекция методов
        Map<String, Method> mapAllMethodsForClass = collector.MethodCollector.collect(aClass);
        OverrideResolver.removeImplemented(mapAllMethodsForClass, aClass);
        MethodValidator.validateMethodTypes(mapAllMethodsForClass);

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
                writer.write(HeadBuilder.buildHeader(aClass, implClassName));

                // методы по одному
                for (Method m : mapAllMethodsForClass.values()) {
                    writer.write(generator.MethodGenerator.generateTxtMethodForFile(m));
                }

                // конструкторы по 1 если это класс
                for (Constructor<?> c : listConstructors) {
                    writer.write(generator.ConstructorGenerator.generateConstructor(c, implClassName));
                }

                // закрывающиеся скобки
                writer.write("}\n");
            }
        } catch (IOException e) {
            throw new ImplerException("Cannot write " + file, e);
        }
    }


}