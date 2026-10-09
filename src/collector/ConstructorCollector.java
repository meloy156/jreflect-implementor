package collector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Утилитный класс для сбора конструкторов, которые необходимо
 * воспроизвести в генерируемом классе-заглушке.
 */
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


    /**
     * Добавляет в список все неприватные конструкторы указанного класса.
     *
     * @param aClass класс, конструкторы которого нужно собрать
     * @param list список, в который добавляются конструкторы
     */
    private static void collect(Class<?> aClass, List<Constructor<?>> list) {
        for (Constructor<?> con : aClass.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(con.getModifiers())) {
                list.add(con);
            }
        }
    }
}
