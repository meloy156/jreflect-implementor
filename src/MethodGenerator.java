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
        String string = "    @Override\n" +
                "    " +
                TypeUtils.visible(m) +
                TypeUtils.typeName(m.getReturnType()) + " " +
                m.getName() +
                "(" + parametrs(m) + ")" + " {\n" +
                defaultReturn(m) +
                "    }\n\n";
        return string;
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
            string.append(TypeUtils.typeName(types[i])).append(" arg").append(i);
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
}
