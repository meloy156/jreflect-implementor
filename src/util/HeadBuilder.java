package util;

/**
 * Создание заголовочной сторки для файла с классом ImplClassName
 * Содержит наследованные классы/Интерфейсы и кидаемые ошибки
 */
public final class HeadBuilder {

    private HeadBuilder() {}

    /**
     * @param aClass сам класс
     * @param implClassName имя класса для нового файла
     * @return строка заголовочного файла
     */
    public static String buildHeader(Class<?> aClass, String implClassName) {
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
}
