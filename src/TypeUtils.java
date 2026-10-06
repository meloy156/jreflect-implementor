import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import  java.util.stream.Collectors;



public final class TypeUtils {

    private TypeUtils() {}

    /**
     * Имя типа для использования в исходнике
     * @param c сам тип имя которого нужно
     * @return правильно готовое имя
     */
    public static String typeName(Class<?> c) {
        String canonical = c.getCanonicalName();
        return canonical != null ? canonical : c.getTypeName();
    }


    /**
     * Модификатор видимости метода
     * @param m сам метод
     * @return public/protected/''
     */
    public static String visible(Method m) {
        int mod = m.getModifiers();
        if (Modifier.isPublic(mod))
            return "public ";
        if (Modifier.isProtected(mod))
            return "protected ";
        return "";
    }

    /**
     * Полное имя метода со всеми параметрами для дедупликации
     * @param method сам метод
     * @return строка метод(параметры)
     */
    public static String getFullNameMethod(Method method) {

        String param = Arrays.stream(method.getParameterTypes())
                .map(Class::getTypeName)
                .collect(Collectors.joining(", ", "(", ")"));

        return method.getName() + param;
    }
}
