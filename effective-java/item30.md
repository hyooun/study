# 아이템 30. 이왕이면 제네릭 메서드로 만들라
클래스와 마찬가지로, 메서드도 제네릭으로 만들 수 있다.
매개변수화 타입을 받는 정적 유틸리티 메서드는 보통 제네릭이다.
Collections의 알고리즘 메서드(binarySearch, sort 등)는 모드 제네릭이다.
```java
public static Set union(Set s1, Set s2) {
    Set result = new HashSet(s1);
    result.addAll(s2);
    return result;
```
이 메서드는 컴파일은 되지만, 다음과 같은 경고가 발생한다.
```text
Union.java:5: warning: [unchecked] unchecked call to
HashSet(Collection<? extends E>) as a member of raw type HashSet
        Set result = new HashSet(s1);
                     ^
Union.java:6: warning: [unchecked] unchecked call to
addAll(Collection<? extends E>) as a member of raw type Set
        result.addAll(s2);
                     ^
```
경고를 없애려면 메서드 선언에서의 세 집합(입력 2개, 반환 1개)의 원소 타입을 타입 매개변수로 명시하고, 메서드 안에서도 이 타입 매개변수만 사용하게 수정하게면 된다.
타입 매개변수들을 선언하는 타입 매개변수 목록은 메서드 제한자와 반환 타입 사이에 온다.
다음 코드에서 타입 매개변수 목록은 <E>이고 반환 타입은 Set<E>이다.
```java
public static <E> Set<E> union(Set<E> s1, Set<E> s2) {
    Set<E> result = new HashSet<>(s1);
    result.addAll(s2);
    return result;
}
```
이는 한정적 와일드카드 타입([아이템 31](item31.md))을 사용하여 더 유연하게 개선할수도 있다.

때때로 불변 객체를 여러 타입으로 활용할 수 있게 만들어야 할 때가 있다.
제네릭은 런타임에 타입 정보가 소거([아이템 28](item28.md))되므로 하나의 객체를 어떤 타입으로든 매개변수화할 수 있다.
이렇게 하려면 요청한 타입 매개변수에 맞게 매번 그 객체의 타입을 바꿔주는 static 팩터리를 만들어야 한다.
이 패턴을 제네릭 싱글턴 팩터리라 하며, Collections.reverseOrder 같은 함수 객체([아이템 42](item42.md))나 Collections.emptySet 같은 컬렉션용으로 사용한다.

항등함수(identity function)를 담은 클래스를 만들고 싶다고 해보자.
자바 라이브러리의 Function.identity를 사용하면 되지만([아이템 59](item59.md)), 직접 한번 작성해보자.
```java
private static UnaryOperator<Obejct> IDENTITY_FN = (t) -> t;

@SuppressWarnings("unchecked")
public static <T> UnaryOperator<T> identityFunction() {
    return (UnaryOperator<T>) IDENTITY_FN;
}
```
IDENTITY_FN을 `UnaryOperator<T>`로 형변환하면 비검사 형변환 경고가 발생한다.
T가 어떤 타입이든 `UnaryOperator<Object>`는 `UnaryOperator<T>`가 아니기 때문이다.
하지만 항등함수란 입력 값을 수정 없이 그대로 반환하는 함수이므로 T가 어떤 타입이든 `UnaryOperator<T>`를 사용해도 안전하다.
이를 알고 있으니 `@SuppressWarnings`로 경고를 숨겨도 된다.
> 💡 UnaryOperator는 자바에서 제공하는 함수형 인터페이스로 람다 표현식으로 구현할 수 있다.
> apply 메서드를 사용하면 인자에 대한 람다식의 결과가 나온다.

```java
public static void main(String[] args) {
    String[] strings = { "삼베", "대마", "나일론" };
    UnaryOperator<String> sameString = identityFunction();
    for (String s : strings)
        System.out.println(sameString.apply(s));

    Number[] numbers = { 1, 2.0, 3L };
    UnaryOperator<Number> sameNumber = identityFunction();
    for (Number n : numbers)
        System.out.println(sameNumber.apply(n));
}
```
위와 같이 다양한 타입을 사용해도 오류가 발생하지 않는다.
자기 자신이 들어간 표현식을 사용하여 타입 매개변수의 허용 범위를 한정할 수 있다.
재귀적 타입 한정(recursive type bound)라는 개념으로, 주로 Comparable 인터페이스([아이템 14](item14.md))와 함께 쓰인다.
```java
public interface Comparable<T> {
    int compareTo(T o);
}
```
여기서 타입 매개변수 T는 `Comparable<T>`를 구현한 타입이 비교할 수 있는 원소의 타입을 정의한다.
거의 모든 타입은 자신과 같은 타입의 원소와만 비교할 수 있기 때문에 String은 `Comparable<String>`을 구현하고 Integer는 `Comparable<Integer>`를 구현하는 식이다.
Comparable을 구현한 원소의 컬렉션을 입력받는 메서드들은 주로 그 원소들을 정렬 혹은 검색하거나, 최솟값이나 최댓값을 구하는 식으로 사용된다.
이 기능을 수행하려면 컬렉션에 담긴 모든 원소가 상호 비교될 수 있어야 한다.

메서드의 구현은 다음과 같다.
타입 한정인 `<E extends Comparable<E>>`는 모든 타입 E는 자신과 비교할 수 있다는 의미이다.
```java
public static <E extends Comparable<E>> E max(Collection<E> c) {
    if (c.isEmpty())
        throw new IllegalArgumentException("컬렉션이 비어 있습니다.");

    E result = null;
    for (E e : c)
        if (result == null || e.compareTo(result) > 0)
            result = Objects.requireNonNull(e);

    return result;
}
```
