package generator;

import java.lang.reflect.Method;


public final class MethodGenerator {

    private MethodGenerator() {}

    /**
     * Генерирует текст готовой заглушки для метода-заглушка
     * @param m метод который нужен
     * @return 4 строки
     * 1) @Override
     * 2) заголовочная метода
     * 3) return дефолт
     * 4) }
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
     * Для generateTxtMethodForFile
     * правильные параметры для метода
     * @param m метод
     * @return строка с его параметрами
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
     * Для generateTxtMethodForFile
     * выводит дефолтное значение для метода
     * @param m метод
     * @return defaultReturn
     */
    private static String defaultReturn(Method m) {
        Class<?> ret = m.getReturnType();

        if (ret == void.class) return "";
        if (ret == boolean.class) return "        return false;\n";
        if (ret == char.class) return "        return 0;\n";
        if (ret.isPrimitive()) return "        return 0;\n";

        return "        return null;\n";
    }


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
