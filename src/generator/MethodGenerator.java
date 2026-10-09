package generator;

import java.lang.reflect.Method;


/**
 * Утилитный класс для генерации исходного кода методов-заглушек.
 */
public final class MethodGenerator {

    private MethodGenerator() {}

    /**
     * Генерирует текст метода-заглушки с аннотацией {@code @Override}
     * и возвратом значения по умолчанию.
     *
     * @param m метод, для которого создаётся заглушка
     * @return исходный код метода
     */
    public static String generateTxtMethodForFile(Method m) {
        return "    @Override\n" +
                "    " +
                util.TypeUtils.visible(m.getClass()) +
                util.TypeUtils.typeName(m.getReturnType()) + " " +
                m.getName() +
                "(" + parametrs(m) + ")"
                + throwsClause(m)  + " {\n" +
                defaultReturn(m) +
                "    }\n\n";
    }


    /**
     /**
     * Формирует список параметров метода с типами и именами.
     *
     * @param m метод
     * @return строка вида {@code Type0 arg0, Type1 arg1}
     */
    private static String parametrs(Method m) {
        Class<?>[] types = m.getParameterTypes();
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) string.append(", ");
            string.append(util.TypeUtils.typeName(types[i])).append(" arg").append(i);
        }
        return string.toString();
    }

    /**
     * Возвращает код возврата значения по умолчанию для указанного
     * возвращаемого типа.
     *
     * @param m метод
     * @return строка с {@code return ...;} или пустая строка для {@code void}
     */
    private static String defaultReturn(Method m) {
        Class<?> ret = m.getReturnType();

        if (ret == void.class) return "";
        if (ret == boolean.class) return "        return false;\n";
        if (ret == char.class) return "        return 0;\n";
        if (ret.isPrimitive()) return "        return 0;\n";

        return "        return null;\n";
    }


    /**
     * Формирует секцию {@code throws} для метода.
     *
     * @param m метод
     * @return строка вида {@code  throws Exception1, Exception2} или пустая строка
     */
    private static String throwsClause(Method m) {
        Class<?>[] exceptions = m.getExceptionTypes();
        if (exceptions.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(" throws ");
        for (int i = 0; i < exceptions.length; ++i) {
            if (i > 0)
                sb.append(", ");
            sb.append(util.TypeUtils.typeName(exceptions[i]));
        }
        return sb.toString();
    }

}
