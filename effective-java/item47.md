# 아이템 47. 반환 타입으로는 스트림보다 컬렉션이 낫다
스트림은 iteration을 지원하지 않기 때문에 for-each 사용이 불가능하다.
Stream 인터페이스는 Iterable 인터페이스가 정의한 추상 메서드를 모두 포함하고 Iterable이 정의한 방식대로 동작하지만, Iterable을 extend 하지는 않았기 때문이다.
```java
 * <p>Collections and streams, while bearing some superficial similarities,
 * have different goals.  Collections are primarily concerned with the efficient
 * management of, and access to, their elements.  By contrast, streams do not
 * provide a means to directly access or manipulate their elements, and are
 * instead concerned with declaratively describing their source and the
 * computational operations which will be performed in aggregate on that source.
 * However, if the provided stream operations do not offer the desired
 * functionality, the {@link #iterator()} and {@link #spliterator()} operations
 * can be used to perform a controlled traversal.
```
Stream의 iterator 메서드에 메서드 참조를 건네면 사용할 수 있을 것 같다. 
```java
for (ProcessHandle ph : ProcessHandle.allProcesses()::iterator) {
    // Process the process
}
```
하지만 이 코드는 다음과 같은 컴파일 오류가 발생한다.
```text
Test.java:6: error: method reference not expected here
for (ProcessHandle ph : ProcessHandle.allProcesses()::iterator) {
                        ^
```
이를 해결하기 위해서는 메서드 참조를 매개변수화된 Iterable로 적절히 형변환해줘야 한다.
```java
for (ProcessHandle ph : (Iterable<ProcessHandle>)
                        ProcessHandle.allProcesses()::iterator) {
    // Process the process
}   
```
작동은 하지만 실전에서 사용하기엔 난잡하고 직관성이 떨어진다.
다음과 같은 어댑터 메서드를 사용하면 훨씬 깔끔하게 사용할 수 있다.
```java
public static <E> Iterable<E> iterableOf(Stream<E> stream) {
    return stream::iterator;
}
```
어댑터를 사용하면 어떤 Stream도 for-each 문으로 반복할 수 있다.
```java
for (ProcessHandle p : iterableOf(ProcessHandle.allProcesses())) {
    // Process the process
}
```
만약 API가 Iterable만 반환하면 이를 Stream으로 바꿔주는 어댑터도 함께 제공하는 것이 좋다.
```java
public static <E> Stream<E> streamOf(Iterable<E> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false);
}
```

Collection 인터페이스는 Iterable의 하위 타입이고 stream 메서드도 제공하기 때문에 두 가지를 동시에 지원한다.
때문에 원소 시퀀스를 반환하는 공개 API의 반환 타입에는 Collection이나 하위 타입을 사용하는 것이 일반적으로 좋다.

반환할 시퀀스가 크지만 표현을 간결하게 할 수 있다면 전용 컬렉션을 구현하는 방법도 고려해봐야 한다.
주어진 집합의 멱집합(한 집합의 모든 부분집합)을 반환해야 한다면, 원소 개수가 n개일 때 원소의 개수는 2<sup>n</sup>개가 된다.
때문에 표준 컬렉션 구현체에 저장하는 방법은 매우 비효율적이고 AbstractList를 이용해 전용 컬렉션을 구현해서 사용할 수 있다.
```java
public class PowerSet {
    public static final <E> Collection<Set<E>> of(Set<E> s) {
        List<E> src = new ArrayList<>(s);
        if (src.size() > 30)
            throw new IllegalArgumentException("Set too big " + s);

        return new AbstractList<Set<E>>() {
            @Override public int size() {
                return 1 << src.size(); // 2 to the power srcSize
            }

            @Override public boolean contains(Object o) {
                return o instanceof Set && src.containsAll((Set)o);
            }

            @Override public Set<E> get(int index) {
                Set<E> result = new HashSet<>();
                for (int i = 0; index != 0; i++, index >>= 1)
                    if ((index & 1) == 1)
                        result.add(src.get(i));
                return result;
            }
        };
    }
}
```
> 입력 집합의 원소 수가 30을 넘기면 PowerSet.of가 예외를 던진다.
> 이는 Stream이나 Iterable이 아닌 Collection을 반환 타입으로 사용할 때의 단점이다.
> Collection의 size 메서드가 int값을 반환하므로 PowerSet.of가 반환되는 시퀀스의 최대 길이는 Integer.MAX_VALUE 혹은 2<sup>31</sup>-1로 제한된다.

입력 리스트의 모든 부분리스트를 스트림으로 만드는 것도 어렵진 않다.
```java
public class SubLists {
    public static <E> Stream<List<E>> of(List<E> list) {
        return Stream.concat(Stream.of(Collections.emptyList()),
            prefixes(list).flatMap(SubLists::suffixes));
    }

    private static <E> Stream<List<E>> prefixes(List<E> list) {
        return IntStream.rangeClosed(1, list.size())
            .mapToObj(end -> list.subList(0, end));
    }

    private static <E> Stream<List<E>> suffixes(List<E> list) {
        return IntStream.range(0, list.size())
            .mapToObj(start -> list.subList(start, list.size()));
    }
}
```
Stream.concat 메서드는 반환되는 스트림에 빈 리스트를 추가하며, flatMap 메서드는 모든 prefix의 모든 suffix로 구성된 하나의 스트림을 만든다.
prefix들과 suffix들의 스트림은 IntStream.range와 IntStream.rangeClosed가 반환하는 연속된 정수들을 매핑해 만들었다.
이 구현은 아래와 같이 중첩 for문으로 구현한 것과 비슷하다.
```java
for (int start = 0; start < src.size(); start++)
    for (int end = start + 1; end <= src.size(); end++)
        System.out.println(src.subList(start, end));
```
이를 그대로 스트림으로 변환할 수 있다.
앞서의 구현보다 간단해지지만, 가독성이 떨어진다.
```java
public static <E> Stream<List<E>> of(List<E> list) {
    return IntStream.range(0, list.size())
        .mapToObj(start ->
            IntStream.rangeClosed(start + 1, list.size())
                .mapToObj(end -> list.subList(start, end)))
        .flatMap(x -> x);
}
```

정리하자면, 우선 Collection을 반환할 수 있다면 그렇게 하고, 그렇지 않다면 전용 컬렉션을 고려해본다.
컬렉션으로 반환하는 것이 불가능하다면, Stream과 Iterable 중에서 더 합리적인 것을 반환하면 된다.
