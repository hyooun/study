# 아이템 46. 스트림에서는 부작용 없는 함수를 사용하라
스트림의 핵심은 계산을 일련의 변환(transformation)으로 재구성하는 것이다.
이때 각 변환 단계는 가능한 이전 단계의 결과를 처리하는 순수 함수여야 한다.
이는 오직 입력만이 결과에 영향을 주는 함수를 말하며, 다른 가변 상태를 참조하거나 다른 상태를 변경하지 않는다.
이렇게 하려면 스트림 연산에 사용되는 함수 객체는 부작용(side effect)이 없어야 한다.

```java
Map<String, Long> freq = new HashMap<>();
try (Stream<String> words = new Scanner(file).tokens()) {
    words.forEach(word -> {
        freq.merge(word.toLowerCase(), 1L, Long::sum);
    });
}
```
이 코드는 스트림 코드처럼 보이지만 같은 기능의 반복적 코드보다 길이도 길고 가독성도 떨어져 유지보수하기 어렵다.
종단 연산인 forEach에서 외부 상태(freq 맵)를 수정하는 람다를 실행하면서 문제가 생긴다(멀티스레드 환경에서 문제가 될 수 있음).
이를 올바르게 수정하면 다음과 같다.
```java
Map<String, Long> freq;
try (Stream<String> words = new Scanner(file).tokens()) {
    freq = words
        .collect(groupingBy(String::toLowerCase, counting()));
}
```
이 코드는 collector를 사용하는데, 스트림을 사용하려면 익혀둬야 한다.
java.util.stream.Collectors 클래스는 39개의 메서드가 있다.
collector가 생성하는 객체는 일반적으로 Collection이며, 그래서 이름도 collector다.

collector을 사용하면 스트림의 원소를 컬렉션으로 만들 수 있다.
toList(), toSet(), toCollection(collectionFactory)는 각각 list, set, 프로그래머가 지정한 컬렉션 타입을 반환한다.
다음은 freq에서 가장 흔한 단어 10개를 뽑아내는 스트림 파이프라인이다.
```java
List<String> topTen = freq.keySet().stream()
    .sorted(comparing(freq::get).reversed())
    .limit(10)
    .collect(toList());
```
comparing 메서드는 key 추출 함수를 받는 comparator 생성 메서드다([아이템 14](item14.md)).
그리고 bound 메서드 참조이자, key 추출 함수로 쓰인 `freq::get`은 입력받은 단어(key)를 빈도표에서 추출해 빈도를 확인한다.
그 다음 가장 흔한 단어가 위로 오도록 역순으로 정렬한다.

나머지 36개의 메서드들도 알아보자.
가장 간단한 Map collector는 `toMap(keyMapper, valueMapper)`로, 스트림 원소를 key에 매핑하는 함수와 value에 매핑하는 함수를 인수로 받는다.
```java
private static final Map<String, Operation> stringToEnum =
    Stream.of(values()).collect(
        toMap(Object::toString, e -> e));
```
이런 형태의 간단한 toMap은 스트림의 각 원소가 고유한 key에 매핑되어 있을 때 적합하다.
스트림 원소가 같은 key를 사용한다면 파이프라인이 IllegalStateException을 발생시킨다.

더 복잡한 형태의 toMap이나 groupingBy를 사용하면 이를 해결할 수 있다.
세 번째 인자로 `BinaryOperator<U>` 타입의 merge function을 주면 같은 key를 사용하는 값들이 이 함수를 통해 합쳐진다.
예를 들어 merge function이 곱셈이라면, key가 같은 모든 값들을 곱한 결과를 얻는다.
```java
Map<Artist, Album> topHits = albums.collect(
    toMap(Album::artist, a->a, maxBy(comparing(Album::sales))));
```
이 코드에서 merge function으로 BinaryOperator에서 static import한 maxBy라는 static 팩터리 메서드를 사용했다.
키 추출 함수로는 `Album::sales`를 받아서 앨범 스트림을 맵으로 바꿀 때 Artist와 sales를 기준으로 가장 많은 Album을 짝지어서 Map을 구성한다.

인수가 3개인 toMap은 충돌 시 마지막 값을 취하는(last-write-wins) 수집기를 만들 때도 유용하다.
```java
toMap(keyMapper, valueMapper, (v1, v2) -> v2)
```

네 번째 인수로 맵 팩터리를 받는으면, EnumMap이나 TreeMap처럼 원하는 맵 구현체를 지정할 수 있다.
이러한 toMap에는 다양한 종류가 있고, toConcurrentMap은 병렬 실행된 결과로 ConcurrentHashMap 인스턴스를 생성한다.

groupingBy는 입력으로 classifier를 받고 출력으로는 원소들을 카테고리별로 모아 놓은 맵을 담은 collector를 반환한다.
가장 간단한 형태는 classifier function 하나를 인수로 받아 맵을 반환한다.
아래는 [아이템 45](item45.md)의 Anagram에서 사용한 collector로, alphabetize를 classifier function으로 사용했다.
```java
words.collect(groupingBy(word -> alphabetize(word)))
```
groupingBy가 반환하는 collector가 리스트 외의 값을 갖는 맵을 생성하게 하려면, classifier function과 함께 downstream collector도 명시해야 한다.
downstream collector의 역할은 해당 카테고리의 모든 원소를 담은 스트림으로부터 값을 생성하는 일이다.
이 매개변수에 toSet()을 넘기면 원소들의 List가 아닌 Set을 value로 가지는 Map을 만들어낸다.
toSet() 대신 toCollection(collectionFactory)를 넘기면, 원하는 컬렉션 타입의 value를 갖는 맵을 생성한다.

downstream collector로 counting()을 넘기면 각 카테고리(key)를 원소로 담은 컬렉션이 아닌, 해당 카테고리에 속하는 원소의 개수(value)와 매핑한 맵을 얻는다.
```java
Map<String, Long> freq = words
        .collect(groupingBy(String::toLowerCase, counting()));
```
groupingBy는 맵 팩터리도 지정할 수 있다.
예를 들어 value의 타입이 TreeSet인 TreeMap을 반환하는 collector를 만들 수 있다.

동일한 인자로 groupingByConcurrent 메서드를 사용하면 ConcurrentHashMap을 만들어준다.
많이 사용하진 않지만 groupingBy의 사촌격인 partitioningBy도 있다.
classifier function에 predicate를 받고 key가 Boolean인 맵을 반환한다.

counting 메서드가 반환하는 collector는 downstream collector 전용이다.
Stream의 count 메서드를 사용하면 같은 기능을 수행하므로 collect(counting())처럼 사용할 일은 없다.

Collectors에는 이런 메서드가 15개나 더 있다.
9개는 summing, averaging, summarizing으로 시작하며, 각각 int, long, double 스트림용으로 하나씩 존재한다.
그리고 overload된 reducing 메서드들, filtering, mapping, flatMapping, collectingAndThen 메서드가 있다.

minBy와 maxBy는 인수로 받은 comparator를 사용해 스트림에서 가장 작은 혹은 가장 큰 값을 찾아 반환한다.

joining은 문자열 등의 CharSequence 인스턴스의 스트림에만 적용할 수 있다.
이 중 매개변수가 없는 joining은 단순히 원소들을 연결하는 collector를 반환한다.
인수 하나짜리 joining은 CharSequence 타입의 구분문자(delimiter)를 매개변수로 받아 연결 부위에 이 구분문자를 삽입한다.
인수 3개짜리 joining은 prefix와 suffix도 받아서 문자열을 반환한다.

이러한 Collector들을 잘 익히고 있으면 Stream을 사용할 때 유용하게 활용할 수 있다.
