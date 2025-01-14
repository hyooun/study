# 아이템 37. ordinal 인덱싱 대신 EnumMap을 사용하라
Plant를 간단히 나타낸 다음과 같은 클래스가 있다.
```java
class Plant {
    enum LifeCycle { ANNUAL, PERENNIAL, BIENNIAL }

    final String name;
    final LifeCycle lifeCycle;

    Plant(String name, LifeCycle lifeCycle) {
        this.name = name;
        this.lifeCycle = lifeCycle;
    }

    @Override public String toString() {
        return name;
    }
}
```
배열이나 리스트에서 원소를 꺼낼 때 ordinal 메서드([아이템 35](item35.md))로 인덱스를 얻는 코드가 있다고 해보자.
```java
Set<Plant>[] plantsByLifeCycle =
    (Set<Plant>[]) new Set[Plant.LifeCycle.values().length];
for (int i = 0; i < plantsByLifeCycle.length; i++)
    plantsByLifeCycle[i] = new HashSet<>();

for (Plant t : garden)
    plantsByLifeCycle[p.lifeCycle.ordinal()].add(p);

// 결과 출력
for (int i = 0; i < plantsByLifeCycle.length; i++) {
    System.out.printf("%s: %s%n",
        Plant.LifeCycle.values()[i], plantsByLifeCycle[i]);
}
```
동작은 하지만 배열은 제네릭과 호환되지 않으니([아이템 28](item28.md)) 비검사 형변환을 수행해야 하고 깔끔하게 컴파일되지 않는다.
배열은 각 인덱스의 의미를 모르니 출력 형식도 직접 정해야 한다.
또한 정수가 범위 내에 있는지 직접 검증해야 하며, 잘못된 값을 사용하면 예상과 달리 동작하거나 ArrayIndexOutOfBoundsException을 던질 것이다.

여기서 배열은 실질적으로 열거 타입 상수를 값으로 매핑하는 역할이다.
때문에 Map을 사용할 수 있고 열거 타입을 키로 사용하도록 설계한 EnumMap을 사용하면 된다.
```java
Map<Plant.LifeCycle, Set<Plant>> plantsByLifeCycle =
    new EnumMap<>(Plant.LifeCycle.class);
for (Plant.LifeCycle lc : Plant.LifeCycle.values())
    plantsByLifeCycle.put(lc, new HashSet<>());
for (Plant p : garden)
    plantsByLifeCycle.get(p.lifeCycle).add(p);
System.out.println(plantsByLifeCycle);
```
짧고 명료해졌으며 안전하고 성능도 비슷하다.
안전하지 않은 형변환을 사용하지 않고, 맵의 키인 열거 타입이 출력용 문자열을 제공해 직접 수정하지 않아도 된다.
배열 인덱스를 계산하는 과정에서 오류가 날 가능성도 없다.

여기서 EnumMap의 생성자가 받는 키 타입의 Class 객체는 한정적 타입 토큰으로, 런타임 제네릭 타입 정보를 제공한다([아이템 33](item33.md)).
스트림([아이템 45](item45.md))을 사용해 맵을 관리하면 코드를 더 간결하게 만들 수 있다.
다음은 앞 예시와 비슷하게 동작하는 단순한 스트림 기반 코드다.
```java
System.out.println(Arrays.stream(garden)
        .collect(groupingBy(p -> p.lifeCycle)));
```
이 코드는 EnumMap이 아닌 고유한 Map 구현체를 사용하여 EnumMap의 공간과 성능 이점은 사라진다.
매개변수 3개를 가지는 Collectors.groupingBy 메서드는 mapFactory 매개변수에 원하는 맵 구현체를 명시해 호출할 수 있다.
```java
System.out.println(Arrays.stream(garden)
        .collect(groupingBy(p -> p.lifeCycle,
            () -> new EnumMap<>(LifeCycle.class), toSet())));
```

두 열거 타입 값들을 매핑하기 위해 ordinal을 두 번 사용하는 2차원 배열을 사용하는 코드가 있다고 해보자.
```java
public enum Phase {
    SOLID, LIQUID, GAS;

    public enum Transition {
        MELT, FREEZE, BOIL, CONDENSE, SUBLIME, DEPOSIT;

        // 행은 from의 ordinal을, 열은 to의 ordinal을 인덱스로 쓴다.
        private static final Transition[][] TRANSITIONS = {
            { null, MELT, SUBLIME },
            { FREEZE, null, BOIL },
            { DEPOSIT, CONDENSE, null}
        };

        // 한 상태에서 다른 상태로의 전이를 반환한다.
        public static Transition from(Phase from, Phase to) {
            return TRANSITIONS[from.ordinal()][to.ordinal()];
        }
    }
}
```
앞선 garden과 마찬가지로 컴파일러는 ordinal과 배열 인덱스의 관계를 알 수 없다.
즉, Phase나 Phase.Transition 열거 타입을 수정하면서 TRANSITIONS를 수정하지 않거나 잘못 수정하면 런타임 에러가 발생할 것이다.

이러한 경우엔 EnumMap을 사용하는 편이 훨씬 낫다.
```java
public enum Phase {
    SOLID, LIQUID, GAS;

    public enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),
        BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),
        SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID);

        private final Phase from;
        private final Phase to;

        Transition(Phase from, Phase to) {
            this.from = from;
            this.to = to;
        }

        // Initialize the phase transition map
        private static final Map<Phase, Map<Phase, Transition>>
          m = Stream.of(values()).collect(groupingBy(t -> t.from,
            () -> new EnumMap<>(Phase.class),
            toMap(t -> t.to, t -> t,
                (x, y) -> y, () -> new EnumMap<>(Phase.class))));

        public static Transition from(Phase from, Phase to) {
            return m.get(from).get(to);
        }
    }
}
```
이 맵의 타입인 `Map<Phase, Map<Phase, Transition>>`은 source phase에서 'destination phase 에서 transition으로의 맵'으로의 맵을 의미한다.
이러한 중첩 맵을 초기화하기 위해 java.util.stream.Collector 2개를 사용했다.
첫 collector groupingBy에서는 source phase를 기준으로 묶고, 두 번째 collector인 toMap에서는 destination phase를 transition에 대응시키는 EnumMap을 생성한다.

두 번째 collector에서의 `((x, y) -> y))`는 실제로 사용되지 않는데, toMap의 메서드 시그니처는 다음과 같다.
```java
toMap(keyMapper, valueMapper, mergeFunction, mapFactory)
```
여기서 `((x, y) -> y))`는 Map을 만들다가 키 충돌이 발생한 경우 값을 병합하는 방법에 대한 정의인데, 이 코드에서는 키 충돌이 발생하지 않기 때문에 실제로는 사용되지 않는 것이다.

새로운 상태 PLASMA를 추가해보자.
배열로 만든 코드를 수정하려면 새로운 상수를 Phase에 1개, Phase.Transition에 2개를 추가하고, 원소가 9개짜리인 배열들의 배열을 원소 16개짜리로 변경해야 한다.
만약 바꾸면서 실수한다면 런타임 에러가 발생할 것이다.
EnumMap 버전에서는 Phase 목록에 PLASMA를 추가하고, Transition에 IONIZE(GAS, PLASMA)와 DEIONIZE(PLASMA, GAS)만 추가하면 된다.
```java
public enum Phase {
    SOLID, LIQUID, GAS, PLASMA;

    public enum Transition {
        MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID),
        BOIL(LIQUID, GAS), CONDENSE(GAS, LIQUID),
        SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID),
        IONIZE(GAS, PLASMA), DEIONIZE(PLASMA, GAS);

        ... // 나머지 코드는 그대로다.
}
```
