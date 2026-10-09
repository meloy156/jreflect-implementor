import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.tools.JarImpler;
import util.HeadBuilder;
import util.OverrideResolver;
import validator.MethodValidator;
import validator.TokenValidator;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;


/**
 * Реализация интерфейса {@link Impler}, генерирующая классы-заглушки,
 * реализующие заданный интерфейс или абстрактный класс.
 * <p>
 * Дополнительно класс умеет компилировать сгенерированный класс и упаковывать
 * его в JAR-архив с помощью метода {@link #implementJar(Class, Path)}.
 * </p>
 */
public class Implementor implements JarImpler {

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

            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
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


    /**
     * Генерирует класс-заглушку, компилирует его и упаковывает в JAR.
     *
     * @param aClass интерфейс или абстрактный класс
     * @param jarPath путь к создаваемому JAR-архиву
     * @throws ImplerException если не удалось сгенерировать, скомпилировать или упаковать
     */
    @Override
    public void implementJar(Class<?> aClass, Path jarPath) throws ImplerException {
        Path tempDir;
        // временная папка для java и class
        try {
            tempDir = Files.createTempDirectory("impl");
        } catch (IOException e) {
            throw new ImplerException("Cannot create temp dir", e);
        }

        try {
            // создаем java
            implement(aClass, tempDir);

            // путь и имя java класса
            String packagePath = aClass.getPackageName().replace(".", "/");
            String implSimpleName = aClass.getSimpleName() + "Impl";
            Path javaFile = tempDir.resolve(packagePath).resolve(implSimpleName + ".java");

            // компилируем в class
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new ImplerException("No system Java compiler");
            }
            String classpath = System.getProperty("java.class.path");
            int code = compiler.run(null, null, null,
                    "-encoding", "UTF-8",
                    "-d", tempDir.toString(),
                    "-cp", classpath,
                    javaFile.toString());
            if (code != 0) {
                throw new ImplerException("Compilation failed with code " + code);
            }


            // 5. Упаковываем .class в .jar
            // Внимание: в JAR-е должны быть только .class, без .java
            Files.createDirectories(jarPath.getParent());
            try (JarOutputStream jos = new JarOutputStream(
                    Files.newOutputStream(jarPath))) {
                // манифест не обязателен для "библиотечного" JAR, но можно добавить
                Files.walk(tempDir)
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".class"))
                        .forEach(p -> {
                            String entryName = tempDir.relativize(p).toString()
                                    .replace('\\', '/');
                            try {
                                jos.putNextEntry(new JarEntry(entryName));
                                Files.copy(p, jos);
                                jos.closeEntry();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        } catch (IOException e) {
            throw new ImplerException("IO error", e);
        } finally {
            // 6. Чистим временную папку
            deleteRecursively(tempDir);
        }
    }



    public static void main(String[] args) {
        try {
            if (args.length == 3 && args[0].equals("-jar")) {
                Class<?> clazz = Class.forName(args[1]);
                Path out = Path.of(args[2]);
                new Implementor().implementJar(clazz, out);
            } else if (args.length == 2) {
                Class<?> clazz = Class.forName(args[0]);
                Path out = Path.of(args[1]);
                new Implementor().implement(clazz, out);
            } else {
                System.err.println("Usage:");
                System.err.println("  java -jar Implementor.jar <className> <outputDir>");
                System.err.println("  java -jar Implementor.jar -jar <className> <output.jar>");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }



    private static void deleteRecursively(Path dir) {
        try {
            if (dir != null && Files.exists(dir)) {
                Stream<Path> stream = Files.walk(dir);

                stream.sorted((a, b) -> b.compareTo(a))   // сначала файлы, потом папки
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {
                            }
                        });

                stream.close();
            }
        } catch (IOException ignored) {
        }
    }
}






