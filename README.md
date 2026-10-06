<div align="center">

# Implementor

**Генератор реализаций классов и интерфейсов на Java**

По заданному `Class<?>` создаёт исходный код класса-наследника
с суффиксом `Impl`, готовый к компиляции.

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square&logo=openjdk)
![Build](https://github.com/meloy156/jreflect-implementor/actions/workflows/build.yml/badge.svg)
![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)
![Reflection](https://img.shields.io/badge/powered%20by-Reflection%20API-purple?style=flat-square)

</div>

---

## Содержание

- [О проекте](#о-проекте)
- [Демонстрация](#демонстрация)
- [Как это работает](#как-это-работает)
- [Архитектура](#архитектура)
- [Сборка и запуск](#сборка-и-запуск)
- [Технические детали](#технические-детали)
- [Ограничения](#ограничения)
- [Лицензия](#лицензия)

---

## О проекте

**Implementor** — инструмент кодогенерации на Java, который по объекту
`Class<?>` порождает полноценный исходный код класса-реализации. Полученный
класс компилируется без ошибок, не является абстрактным и переопределяет
все абстрактные методы исходного типа.

Инструмент работает **без знания исходного кода** целевого класса — вся
информация извлекается через Reflection API во время выполнения. Это
позволяет генерировать реализации для любых типов, включая загруженные
из внешних JAR, классов стандартной библиотеки и вложенных типов.

### Ключевые возможности

| Возможность | Описание |
|---|---|
| Обход иерархии | Рекурсивный сбор абстрактных методов по цепочке `getSuperclass()` и `getInterfaces()` |
| Дедупликация | Устранение повторяющихся сигнатур через `Map<String, Method>` |
| Вложенные типы | Корректная обработка `Outer.Inner` через `getCanonicalName()` |
| Валидация | Ранний отказ от примитивов, массивов, `enum`, `final` и `private` типов |
| Zero dependencies | Только стандартная библиотека Java |

---

## Демонстрация

### Вход

```bash
java -cp out info.kgeorgiy.java.advanced.implementor.Implementor java.util.List out
```

### Выход — файл `out/java/util/ListImpl.java`

```java
package java.util;

public class ListImpl implements java.util.List {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(java.lang.Object arg0) {
        return false;
    }

    @Override
    public java.lang.Object get(int arg0) {
        return null;
    }

    // ... и так для всех абстрактных методов
}
```

### Компиляция результата

```bash
$ javac out/java/util/ListImpl.java
$ echo $?
0
```

Сгенерированный класс **компилируется без предупреждений** и может быть
использован как обычная реализация интерфейса `List`.

---

## Как это работает

```
   Class<?> token            Путь root
        │                        │
        ▼                        ▼
   ┌──────────────────────────────────┐
   │         Implementor              │
   │                                  │
   │  1. Валидация token              │
   │  2. Сбор абстрактных методов     │  ← Reflection API
   │  3. Генерация текста класса      │
   │  4. Запись файла                 │
   └──────────────────────────────────┘
        │
        ▼
   root/pkg/NameImpl.java   →   javac   →   NameImpl.class
```

**Пайплайн в четыре шага:**

1. **Валидация.** Тип проверяется на принципиальную невозможность
   генерации (примитив, массив, `final`, `private`, `Object`).
2. **Сбор методов.** Рекурсивный обход иерархии с фильтрацией
   неабстрактных, `static`, `private`, `synthetic`, `default`.
3. **Генерация.** Для каждого метода строится сигнатура с
   `@Override` и заглушка, возвращающая значение по умолчанию.
4. **Запись.** Файл кладётся в правильный подкаталог согласно пакету.

---

## Архитектура

```
src/
├── Implementor.java         Оркестратор. Реализует Impler.
│                            Валидация → путь → сборка → запись.
│
├── MethodCollector.java     Обход иерархии типов. Рекурсия по
│                            getSuperclass() и getInterfaces().
│                            Возвращает Map<Signature, Method>.
│
├── MethodGenerator.java     Метод → строка исходника.
│                            Сигнатура, видимость, дефолтный return.
│
└── TypeUtils.java           Общие хелперы: typeName, visibility,
                             signature. Устраняет дублирование
                             между остальными классами.
```

**Разделение ответственности.** `Implementor` не знает, *как*
собираются методы. `MethodCollector` не знает, *куда* пишется файл.
`MethodGenerator` не знает, *откуда* пришёл метод. Каждый класс
решает одну задачу и тестируется независимо.

---

## Сборка и запуск

### Требования

- JDK 17 или выше
- `lib/` с модулем `info.kgeorgiy.java.advanced.implementor`

### Компиляция

```bash
javac -cp "lib/*" -d out src/*.java
```

### Запуск

```bash
# Windows
java -ea -cp "out;lib/*" -p lib -m info.kgeorgiy.java.advanced.implementor interface Implementor

# Linux / macOS
java -ea -cp "out:lib/*" -p lib -m info.kgeorgiy.java.advanced.implementor interface Implementor
```

### Проверка на своём типе

```bash
javac -cp out MyInterface.java
java -cp out MyDir MyInterfaceImpl.java
```

---

## Технические детали

### Почему `Map<String, Method>`, а не `Set<Method>`

`java.lang.reflect.Method` **не переопределяет** `equals` и `hashCode`.
Два вызова `getMethod("f")` создают **два разных объекта**, которые
не равны по `equals`. `HashSet<Method>` в такой ситуации не
дедуплицирует методы — в коллекции окажутся дубликаты одной и той же
сигнатуры. Решение: ключ-строка `"f(int, java.lang.String)"`, которая
сравнивается по содержимому.

### Почему две рекурсии — по классам и по интерфейсам

`getSuperclass()` возвращает **один** родительский класс, а
`getInterfaces()` — **массив** интерфейсов. Абстрактные методы могут
жить в любом из узлов дерева. Обход должен идти по обеим осям
одновременно. Остановка на `Object.class` — иначе соберём
`equals`, `hashCode` и `wait`, которые реализовывать не нужно.

### Почему `getCanonicalName()`, а не `getTypeName()`

Для вложенного класса `Outer$Inner`:

- `getTypeName()` → `com.example.Outer$Inner`
- `getCanonicalName()` → `com.example.Outer.Inner`

В **исходном тексте** Java нужен второй вариант — доллар не является
допустимым разделителем в ссылке на тип. Но `getCanonicalName()`
возвращает `null` для анонимных и локальных классов, поэтому нужен
fallback на `getTypeName()`.

### Приоритет класса над интерфейсом

Порядок обхода — `getSuperclass()` **до** `getInterfaces()`.
Это соответствует правилам разрешения методов в JLS: при коллизии
сигнатур выигрывает версия из класса. Если обойти интерфейсы первыми,
в мапу попадёт метод с интерфейсной сигнатурой, что может привести
к несовпадению `throws`-деклараций и ковариантных возвращаемых типов.

---

## Ограничения

Инструмент **намеренно** отказывается генерировать код в случаях,
когда это невозможно или бессмысленно:

| Случай | Причина |
|---|---|
| Примитивы (`int`, `void`) | Нельзя наследоваться от типа, не являющегося классом |
| Массивы | Нет исходного типа, от которого можно унаследоваться |
| `enum` | Уже `final`, расширение запрещено |
| `final`-классы | Наследование запрещено компилятором |
| `private`-типы | Недоступны за пределами содержащего класса |
| `Object.class` | Особый случай, обработка не требуется |

Во всех этих случаях `implement` бросает `ImplerException` **до**
создания файла.

---

## Лицензия

Распространяется под лицензией **MIT**. См. файл [LICENSE](LICENSE).

---

<div align="center">

*Сделано с использованием `java.lang.reflect` и `java.nio.file`.*

</div>