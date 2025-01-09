# 아이템 31. 한정적 와일드카드를 사용해 API 유연성을 높이라
매개변수화 타입은 불공변(invariant)이다.
즉, 서로 다른 타입 Type1과 Type2가 있을 때 List<Type1>은 List<Type2>의 하위 타입도 상위 타입도 아니다.
예를 들어 List<String>은 List<Object>의 하위 타입이 아니라는 말인데, List<Object>는 어떤 객체든 넣을 수 있지만 List<String>은 문자열만 넣을 수 있다.
List<String>은 List<Object>를 대체할 수 없기 때문에 하위 타입이 될 수 없다.(리스코프 치환 원칙에 어긋난다. [아이템 10](item10.md) 참조)

때론 불공변 방식보다 유연한 무언가가 필요하다. [아이템 29](item29.md)의 Stack 클래스를 살펴보자.
Stack의 public API는 다음과 같다.
```java
public class Stack<E> {
    public Stack();
    public void push(E e);
    public E pop();
    public boolean isEmpty();
}
```
여기에 pushAll을 추가해야 한다고 해보자.
```java
public void pushAll(Iterable<E> src) {
    for (E e : src)
        push(e);
}
```
이 메서드는 컴파일되지만 완벽하진 않다.
Iterable src의 원소 타입이 Stack의 원소 타입과 일치하면 잘 작동한다.
하지만 다음과 같이 사용하면 정상적으로 작동하지 않는다.
```java
Stack<Number> numberStack = new Stack<>();
Iterable<Integer> integers = ...;
numberStack.pushAll(integers);
```
Integer가 Number의 하위 타입이기 때문에 잘 동작할 것 같지만, 매개변수화 타입이 불공변이기 때문에 다음과 같은 오류가 발생한다.
```text
StackTest.java:7: error: incompatible types: Iterable<Integer>
cannot be converted to Iterable<Number>
        numberStack.pushAll(integers);
                            ^
```
자바는 이러한 상황에 대처할 수 있는 한정적 와일드카드 타입이라는 특별한 매개변수화 타입을 제공한다.
`Iterable<? extends E>`와 같은 타입으로 작성한다.
```java
public void pushAll(Iterable<? extends E> src) {
    for (E e : src)
        push(e);
}
```
이렇게 수정하면 잘 컴파일 된다. 
비슷하게 popAll도 작성해보자
```java
public void popAll(Collection<E> dst) {
    while (!isEmpty())
        dst.add(pop);
}
```
이번에도 주어진 컬렉션의 원소 타입이 스택의 원소 타입과 일치하면 문제없이 동작한다.
하지만 다음과 같은 코드에서 문제가 발생한다.
```java
Stack<Number> numberStack = new Stack<>();
Collection<Object> objects = ...;
numberStack.popAll(objects);
```
이와 같이 컴파일하면 pushAll을 처음 작성했을 때와 비슷한 오류가 발생한다.
이번에는 값을 넣어야 해서 하위 타입이어야 하는 것이 아닌, 값을 꺼낼 때 상위 타입도 가능하게 하는 코드이므로 `Iterable<? extends E>`가 아닌 `Iterable<? super E>`로 사용하면 된다.
다음과 같이 작성할 수 있다.
```java
public void popAll(Collection<? super E> dst) {
    while (!isEmpty())
        dst.add(pop());
}
```

다음 공식을 외워두면 어떤 와일드카드 타입을 사용해야 하는지 판단하기 편하다.
> PECS : producer-extends, consumer-super

매개변수화 타입 T가 생산자라면 `<? extends T>`를 사용하고, 소비자라면 `<? super T>`를 사용하라.
PECS 공식은 와일드카드 타입을 사용하는 기본 원칙이다.

이 공식을 기반으로 앞 아이템들에서 소개한 메서드와 생성자 선언을 다시 살펴보자.
[아이템 28](item28.md)의 Chooser 생성자는 다음과 같이 선언했다.
```java
public Chooser(Collection<T> choices)
```
이 생성자로 넘겨지는 choices 컬렉션은 T 타입의 값을 생산하기만 하니, T를 확장하는 와일드카드 타입을 사용해 선언해야 한다.
이 공식을 기반으로 수정하면 다음과 같다.
```java
public Chooser(Collection<? extends T> choices)
```
`Chooser<Number>`의 생성자에 `List<Integer>`를 넘기고 싶다고 해보자.
수정 전 생성자로는 컴파일이 안되겠지만, 한정적 와일드카드 타입으로 변경한 후에는 정상적으로 동작한다.

[아이템 30](item30.md)의 union 메서드도 살펴보자.
```java
public static <E> Set<E> union(Set<E> s1, Set<E> s2)
```
s1과 s2 모두 E의 생산자이니 PECS 공식에 따라 다음처럼 선언해야 한다.
```java
public static <E> Set<E> union(Set<? extends E> s1, Set<? extends E> s2)
```
> ### ❗ 주의</br>
> 반환 타입에는 한정적 와일드카드 타입을 사용하면 안 된다. </br>
> 클라이언트 코드에서 어떤 타입인지 정확하게 알 수가 없게 된다.

수정한 선언을 사용하면 다음과 같은 코드도 잘 컴파일된다.
```java
Set<Integer> integers = Set.of(1, 3, 5);
Set<Double> doubles = Set.of(2.0, 4.0, 6.0);
Set<Number> numbers = union(integers, doubles);
```

자바 8부터는 정상적으로 컴파일되지만, 자바 7까지는 추론 능력이 부족해 다음과 같은 오류 메시지가 나온다.
```text
Union.java:14: error: incompatible types
        Set<Number> numbers = union(integers, doubles);
                                   ^
  required: Set<Number>
  found: Set<INT#1>
  where INT#1,INT#2 are intersection types:
    INT#1 extends Number,Comparable<? extends INT#2>
    INT#2 extends Number,Comparable<?>
```
이는 명시적 타입 인수(explicit type argument)를 사용해 해결이 가능하다.
```java
Set<Number> numbers = Union.<Number>union(integers, doubles);
```

다음은 [아이템 30](item30.md)의 max 메서드를 바꿔보자.
```java
public static <E extends Comparable<E>> E max(List<E> list)
```
입력 매개변수에서는 E 인스넡스를 생산하므로 `<? extends E>`로 수정하자.
타입 매개변수는 기존에 `<E extends Comparable<E>>`로 정의되어 있는데, 이때 `Comparable<E>` 는 E 인스턴스를 소비한다.
Comparable은 항상 소비자이므로 일반적으로 `Comparable<E>`보다는 `Comparable<? extends E>`를 사용하는 편이 낫다.
```java
public static <E extends Comparable<? super E>> E max(List<? extends E> list)
```
다음 리스트는 수정된 max로만 처리가 가능하다.
```java
List<ScheduledFuture<?>> scheduledFutures = ...;
```
수정 전 max는 java.util.concurrent 패키지의 ScheduledFuture가 `Comparable<ScheduledFuture>`를 구현하지 않았기 때문에 오류가 발생한다.
ScheduledFuture는 Delayed의 하위 인터페이스이고, Delayed는 `Comparable<Delayed>`를 확장했다.
ScheduledFuture의 인스턴스는 다른 ScheduledFuture 인스턴스뿐 아니라 Delayed 인스턴스와도 비교할 수 있어 수정 전 max에서 이를 처리하지 못한다.
즉, Comparable이나 Comparator를 직접 구현하지 않고 직접 구현한 다른 타입을 확장한 타입을 지원하기 위해 와일드카드를 사용한다.

다음은 비한정적 타입 매개변수([아이템 30](item30.md))와 비한정적 와일드카드를 사용한 두 가지 선언이다.
```java
public static <E> void swap(List<E> list, int i, int j);
public static void swap(List<?> list, int i, int j);
```
public API라면 두 번째가 더 낫다.
어떤 리스트든 명시한 인덱스의 원소들을 교환해주고 신경 써야 할 타입 매개변수도 없다.
메서드 선언에 타입 매개변수가 한 번만 나오면 와일드카드로 대체하자.
이때 비한정적 타입 매개변수라면 비한정적 와일드카드로 바꾸고, 한정적 타입 매개변수라면 한정적 와일드카드로 바꾸면 된다.

```java
public static void swap(List<?> list, int i, int j) {
    list.set(i, list.set(j, list.get(i)));
}
```
이 코드를 컴파일하면 오류 메시지가 나온다.
```text
Swap.java:5: error: incompatible types: Object cannot be converted to CAP#1
    list.set(i, list.set(j, list.get(i)));
                                    ^
  where CAP#1 is a fresh type-variable:
    CAP#1 extends Object from capture of ?
```
원인은 리스트의 타입이 List<?>인데 List<?>에는 null 외에 어떤 값도 넣을 수 없다는 게 있다.
와일드카드 타입의 실제 타입을 알려주는 private 메서드를 작성하여 해결이 가능하다.
실제 타입을 알아내려면 이 메서드는 제네릭 메서드여야 한다.
```java
public static void swap(List<?> list, int i, int j) {
    swapHelper(list, i, j);
}

// 와일드카드 타입을 실제 타입으로 바꿔주는 private 도우미 메서드
private static <E> void swapHelper(List<E> list, int i, int j) {
    list.set(i, list.set(j, list.get(i)))
}
```
swapHelper 메서드는 리스트가 List<E>임을 알고 있다.
즉, 이 리스트에서 꺼낸 값의 타입은 항상 E이고 List<E>에 넣어도 안전하다는 것을 알고 있다.
이렇게 외부에서 와일드카드를 사용한 선언을 유지하고, 내부에서 제네릭 메서드를 사용하여 구현할 수 있다.
