import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


public final class ConstructorCollector {

    private ConstructorCollector() {}

    /**
     * Создание листа со всеми не приватными конструкторами класса
     *
     * @param aClass данный класс
     * @return listConstructor - массив конструкторов
     */
    public static List<Constructor<?>> collect(Class<?> aClass) {
        if (aClass.isInterface())  {
            return new ArrayList<>();
        }
        List<Constructor<?>> listConstructor = new ArrayList<>();
        collect(aClass, listConstructor);
        return listConstructor;
    }


    private static void collect(Class<?> aClass, List<Constructor<?>> list) {
        for (Constructor<?> con : aClass.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(con.getModifiers())) {
                list.add(con);
            }
        }
    }
}
