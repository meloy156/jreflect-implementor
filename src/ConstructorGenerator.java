import java.lang.reflect.Constructor;

public final class ConstructorGenerator {

    private ConstructorGenerator() {}


    /**
     * Генерируется полный конструктор с дефолтным значением для вывода в новый класс
     * @param c конструктор
     * @param implClassName имя самого класса - имя конструктора в новом файле
     * @return все строки конструктора
     */
    public static String generateConstructor(Constructor<?> c, String implClassName) {
        StringBuilder sb = new StringBuilder();
        sb.append("    ").append(TypeUtils.visible(c.getClass())).append(implClassName);
        sb.append('(').append(parametersFromConstructor(c)).append(')');

        Class<?>[] exceptions = c.getExceptionTypes();
        if (exceptions.length > 0) {
            sb.append(" throws ");
            for (int i = 0; i < exceptions.length; ++i) {
                if (i > 0)
                    sb.append(", ");
                sb.append(TypeUtils.typeName(exceptions[i]));
            }
        }

        sb.append(" {\n");
        sb.append("        super(").append(argumentList(c)).append(");\n");
        sb.append("    }\n\n");
        return sb.toString();
    }


    /**
     * Создает список параметров с типами для написания конструктора
     *
     * @param c конструктор
     * @return String - строка для параметра конструткора
     * Constructor(return) {}
     */
    private static String parametersFromConstructor(Constructor<?> c) {
        Class<?>[] types = c.getParameterTypes();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.length; ++i) {
            if (i > 0)
                sb.append(", ");
            sb.append(TypeUtils.typeName(types[i])).append(" arg").append(i);
        }
        return sb.toString();
    }

    /**
     * Создает строку параметров конструктора для super
     * @param c конструктор
     * @return String
     * super(return)...
     */
    private static String argumentList(Constructor<?> c) {
        StringBuilder sb = new StringBuilder();
        int count = c.getParameterCount();
        for (int i = 0; i < count; ++i) {
            if (i > 0)
                sb.append(", ");
            sb.append("arg").append(i);
        }
        return sb.toString();
    }

}
