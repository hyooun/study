# 아이템 44. 표준 함수형 인터페이스를 사용하라
자바가 람다를 지원하면서 API를 작성하는 방법도 바뀌었다.
예를 들어, 상위 클래스의 기본 메서드를 재정의해 원하는 동작을 구현하는 템플릿 메서드 패턴보다 같은 효과의 함수 객체를 받는 static 팩터리나 생성자를 제공하는 방식으로 변화하고 있다.
함수 객체를 매개변수로 받는 생성자와 메서드를 더 많이 만들고 함수형 매개변수 타입을 올바르게 선택해야 한다.

LinkedHashMap을 생각해보자.
이 클래스의 protected 메서드인 removeEldestEntry를 재정의하면 캐시로 사용할 수 있다.
다음과 같이 재정의하면 맵에 원소가 100개가 될 때까지 커지다가, 그 이상이 되면 새로운 키가 더해질 때마다 가장 오래된 원소를 하나씩 제거한다.
가장 최근 원소 100개가 유지되므로 캐시처럼 사용할 수 있는 것이다.
```java
protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
    return size() > 100;
}
```
동작에는 문제가 없지만 람다를 사용하면 훨씬 효율적이다.
LinkedHashMap이 최근에 구현됐다면 함수 객체를 받는 static 팩터리나 생성자를 제공했을 것이다.
removeEldestEntry는 인스턴스 메서드기 때문에 size()를 호출해 맵 안의 원소 수를 알아낸다.
하지만 생성자에 넘기는 함수 객체는 인스턴스 메서드가 아니다.
따라서 맵은 자기 자신도 함수 객체에 건네줘야 하고, 다음과 같이 함수형 인터페이스를 선언할 수 있다.
```java
@FunctionalInterface interface EldestEntryRemovalFunction<K,V>{
    boolean remove(Map<K,V> map, Map.Entry<K,V> eldest);
}
```
이 인터페이스도 잘 동작하지만, 사용할 이유가 없다.
java.util.function 패키지에서 필요한 용도의 표준 함수형 인터페이스를 찾아서 사용하자.

예를 들어 Predicate 인터페이스는 predicate들을 조합하는 메서드를 제공한다.
앞의 LinkedHashMap의 예시에서 직접 만든 EldestEntryRemovalFunction 대신 표준 인터페이스인 `BiPredicate<Map<K, V>, Map.Entry<K, V>>`를 사용할 수 있다.

java.util.function에는 총 43개의 인터페이스가 있고 이 중 6개만 기억해도 나머지를 유추할 수 있다.
| Interface         | Function Signature      | Example                  |
|-------------------|-------------------------|--------------------------|
| UnaryOperator<T>  | T apply(T t)           | String::toLowerCase      |
| BinaryOperator<T> | T apply(T t1, T t2)    | BigInteger::add          |
| Predicate<T>      | boolean test(T t)      | Collection::isEmpty      |
| Function<T,R>     | R apply(T t)           | Arrays::asList           |
| Supplier<T>       | T get()                | Instant::now             |
| Consumer<T>       | void accept(T t)       | System.out::println      |

Operator 인터페이스는 인수가 1개인 UnaryOperator와 2개인 BinaryOperator로 나뉘며, 반환값과 인수의 타입은 같다.
Predicate 인터페이스는 인수 하나를 받아 boolean을 반환한다.
Function 인터페이스는 인수와 반환 타입이 다르다.
Supplier 인터페이스는 인수를 받지 않고 값을 반환한다.
Consumer 인터페이스는 인수를 하나 받고 반환값은 없는 것을 의미한다.

기본 인터페이스는 primitive 타입인 int, long, double용으로 각각 3개씩 존재한다.
네이밍도 기본 인터페이스 앞에 primitive 타입의 이름을 붙이면 된다.
int를 받는 Predicate는 IntPredicate이고, long을 받아 long을 반환하는 BinaryOperator는 LongBinaryOperator이다(총 15개).
유일하게 Function에서 반환 타입만 매개변수화되어 있다(총 9개).
예를 들어 `LongFunction<int[]>`은 long 인수를 받아 int[]를 반환한다.

또한 primitive 타입을 반환하는 변형이 9개 더 있다.
인수와 같은 타입을 반환하는 함수는 UnaryOperator이므로, Function 인터페이스의 변형은 입력과 결과가 항상 다르다.
입력과 결과 타입이 모두 primitive 타입이면 SrcToResultFunction 형식이다.
예를 들어 long을 받아 int를 반환하면 longToIntFunction이 되는 식이다(총 6개).
나머지는 입력이 객체 참조이고 결과가 int, long, double인 변형들로, 입력을 매개변수화하고 ToResultFunction으로 사용한다.
`ToLongFunction<int[]>`은 int[]를 받아 long을 반환한다(총 3개).

기본 함수형 인터페이스 중 3개는 인수를 2개씩 받는 변형이 있다.
`BiPredicate<T, U>`, `BiFunction<T, U, R>`, `BiConsumer<T, U>`이고, 다시 기본 타입을 반환하는 세 변형인 `ToIntBiFunction<T, U>`, `ToLongBiFunction<T, U>`, `ToDoubleBiFunction<T, U>`가 존재한다.
Consumer에도 객체 참조와 primitive 하나씩, 인수 2개를 받는 변형인 `ObjDoubleConsumer<T>`, `ObjIntConsumer<T>`, `ObjLongConsumer<T>`가 존재한다.
이렇게 총 9개의 변형이 더 있다.

BooleanSupplier 인터페이스는 boolean을 반환하는 Supplier의 변형이다.
앞서 소개한 42개에 BooleanSupplier까지 더해 총 43개의 표준 함수형 인터페이스가 존재한다.

표준 함수형 인터페이스는 대부분 primitive 타입만 지원한다.
박싱된 primitive 타입을 사용하는 경우 계산량이 많아질수록 성능이 저하되므로 사용하지 말자.

가능하면 이러한 표준 함수형 인터페이스를 활용하고 다음과 같은 경우에만 직접 구현하는 것을 고려해보자.
> - 자주 쓰이며, 이름 자체가 용도를 명확히 설명해준다.
> - 반드시 따라야 하는 규약이 있다.
> - 유용한 디폴트 메서드를 제공할 수 있다.

위에서 직접 구현한 EldestEntryRemovalFunction 인터페이스에 `@FunctionalInterface`라는 애너테이션을 사용했다.
이는 `@Override`를 사용하는 이유와 비슷한데, 프로그래머의 의도를 명시하는 것으로 크게 세 가지 목적이 있다.
> 1. 해당 클래스의 코드나 문서를 읽는 사람에게 인터페이스가 람다용으로 설계된 것임을 알려준다.
> 2. 해당 인터페이스가 추상 메서드를 오직 하나만 가지고 있어야 컴파일되게 해준다.
> 3. 유지보수 과정에서 누군가 메서드를 추가하지 못하게 막아준다.
때문에 직접 함수형 인터페이스를 구현하는 경우 `@FunctionalInterface` 애너테이션을 추가해야 한다.

함수형 인터페이스를 API에서 사용할 때의 주의점으로 서로 다른 함수형 인터페이스를 같은 위치의 인수로 받는 메서드들을 overloading 해서는 안된다.
ExecutorService의 submit 메서드는 Callable<T>를 받는 과 Runnable을 받는 것을 overloading하고 있다.
```java
    /**
     * Submits a value-returning task for execution and returns a
     * Future representing the pending results of the task. The
     * Future's {@code get} method will return the task's result upon
     * successful completion.
     *
     * <p>
     * If you would like to immediately block waiting
     * for a task, you can use constructions of the form
     * {@code result = exec.submit(aCallable).get();}
     *
     * <p>Note: The {@link Executors} class includes a set of methods
     * that can convert some other common closure-like objects,
     * for example, {@link java.security.PrivilegedAction} to
     * {@link Callable} form so they can be submitted.
     *
     * @param task the task to submit
     * @param <T> the type of the task's result
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task is null
     */
    <T> Future<T> submit(Callable<T> task);

    /**
     * Submits a Runnable task for execution and returns a Future
     * representing that task. The Future's {@code get} method will
     * return {@code null} upon <em>successful</em> completion.
     *
     * @param task the task to submit
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task is null
     */
    Future<?> submit(Runnable task);
```

```java
ExecutorService executor = Executors.newSingleThreadExecutor();

executor.submit(() -> System.out.println("Hello, world!"));
```
위와 같이 람다식이나 메서드 참조를 사용했을 때, 둘 중 어떤 메서드인지 컴파일러가 판단하기 모호해진다.
때문에 서로 다른 함수형 인터페이스를 같은 위치의 인수로 사용하는 overloading은 피해야 한다.
