# 아이템 26. 로 타입은 사용하지 말라
클래스와 인터페이스 선언에 타입 매개변수(type parameter)가 쓰이면 이를 제네릭 클래스 혹은 제네릭 인터페이스라고 한다.
예를 들어 List 인터페이스는 원소의 타입을 나타내는 타입 매개변수 E를 받는다.
그래서 완전한 이름은 List<E>지만, 짧게 List라고도 자주 쓴다.
제네릭 클래스와 제네릭 인터페이스를 통틀어 제네릭 타입(generic type)이라고 한다.

각각의 제네릭 타입은 일련의 매개변수화 타입(parameterized type)을 정의한다.
먼저 클래스(혹은 인터페이스) 이름이 나오고, 꺾쇠괄호 안에 실제 타입 매개변수들을 나열한다.
List<String>을 예로 들면, 원소의 타입이 String인 리스트를 뜻하는 매개변수화 타입이다.
String이 정규(formal) 타입 매개변수 E에 해당하는 실제(actual) 타입 매개변수다.

제네릭 타입을 하나 정의하면 그에 딸린 로 타입(raw type)도 함께 정의된다.
로 타입은 제네릭 타입에서 타입 매개변수를 전혀 사용하지 않을 때를 말한다.
(List<E>의 로 타입은 List다.)
로 타입은 타입 선언에서 제네릭 타입 정보가 전부 지워진 것처럼 동작하는데, 제네릭을 사용하기 전 코드와 호환하기 위함이다.
제네릭을 지원하기 전에는 컬렉션을 다음과 같이 선언했다.
```java
// Stamp 인스턴스만 취급한다.
private final Collection stamps = ...;
```
이 코드를 사용하면 실수로 Stamp 대신 Coin을 넣어도 아무 오류 없이 컴파일이 실행된다.
```java
// 실수로 동전을 넣는다.
stamp.add(new Coin(...));  // unchecked call 경고를 출력한다.
```
컬렉션에서 동전을 다시 꺼내기 전까지 오류를 알지 못한다.
```java
for (Iterator i = stamp.iterator(); i.hasNext(); ) {
    Stamp stamp = (Stamp) i.next(); // ClassCastException을 던진다.
    stamp.cancel();
}
```
오류는 가능한 빠르게 발견하고 수정하는 것이 이상적이다. (런타임보다 컴파일할 때 발견하는 것이 좋다)
이렇게 코드를 작성하면 런타임에 문제를 겪는 코드와 원인을 제공한 코드가 떨어져 있어 디버깅이 복잡해진다.

```java
private final Collection<Stamp> stamps = ...;
```
이렇게 선언하면 stamps에는 Stamp의 인스턴스만 넣어야 함을 컴파일러가 인지한다.
stamps에 다른 타입의 인스턴스를 넣으려 하면 컴파일 오류가 발생해 오류를 빠르게 수정할 수 있다.

```text
Test.java:9: error incompatible types: Coin cannot be converted to Stamp
    stamps.add(new Coin());
                   ^
```

이러한 이유들 때문에 로 타입(타입 매개변수가 없는 제네릭 타입)을 언어 차원에서 막아 놓지는 않았지만 절대로 사용하면 안 된다.
로 타입을 쓰면 제네릭을 사용할 때 얻는 안정성과 표현력이 모두 사라진다.
로 타입을 지원하는 이유는 제네릭을 지원하기 이전 코드와의 호환성 때문이다.

List<Object>처럼 임의 객체를 허용하는 매개변수화 타입은 괜찮다.
매개변수로 List를 받는 메서드에 List<String>을 넘길 수 있지만, List<Object>를 받는 메서드에는 넘길 수 없다.
즉, List<String>은 로 타입의 하위 타입이지만 List<Object>는 아니다([아이템 28](item28.md)).
```java
public static void main(String[] args) {
    List<String> strings = new ArrayList<>();
    unsafeAdd(strings, Integer.valueOf(42));
    String s = string.get(0); // 컴파일러가 자동으로 형변환 코드를 넣어준다.
}

private static void unsafeAdd(List list, Object o) {
    list.add(o);
}
```
이 코드는 컴파일은 되지만 로 타입인 List를 사용하여 다음과 같은 경고가 발생한다.
```text
Test.java:10: warning: [unchecked] unchecked call to add(E) as a member of the raw type List
    list.add(o);
            ^
```
이 프로그램을 이대로 실행하면 `string.get(0)`의 결과를 형변환하려 할 때 ClassCastException을 던진다.
Integer를 String으로 변환하려 시도했기 때문이다.

반면 로 타입인 List를 매개변수화 타입인 List<Object>로 바꾼 다음 다시 컴파일하면, 다음과 같은 오류 메시지가 출력되며 컴파일이 되지 않는다.
```text
Text.java:5: error: incompatible types: List<String> cannot be converted to List<Object>
    unsafeAdd(strings, Integer.valueOf(42));
        ^
```

다음과 같이 2개의 Set을 받아 공통 원소를 반환하는 메서드를 작성한다고 해보자.
```java
static int numElementsInCommon(Set s1, Set s2) {
    int result = 0;
    for (Object o1 : s1)
        if (s2.contains(o1))
            result++;
    return result;
}
```
이 메서드는 동작은 하지만 로 타입을 사용해 안전하지 않다.
`비한정적 와일드카드 타입(unbounded wildcard type)`을 대신 사용하는 게 좋다.
제네릭 타입을 쓰고 싶지만 실제 타입 매개변수가 무엇인지 신경쓰고 싶지 않다면 ❔를 사용하자.
어떤 타입이라도 담을 수 있는 범용적인 매개변수화 타입이다.
```java
static int numElementsInCommon(Set<?> s1, Set<?> s2) { ... }
```
로 타입 컬렉션에는 아무 원소나 넣을 수 있어 타입 불변식을 훼손하기 쉽다.
반면 Collection<?>에는 null을 제외한 어떤 원소도 넣을 수 없다.
다른 원소를 넣으려고 하면 다음과 같은 컴파일 에러가 발생한다.
```text
WildCard.java:13: error: imcompatible types: String cannot be converted to CAP#1
    c.add("verboten");
          ^
  where CAP#1 is a fresh type-variable:
    Cap#1 extends Object from capture of ?
```

### 로 타입을 사용하는 예외
class 리터럴에는 로 타입으로 써야 한다.
자바 명세는 class 리터럴에 매개변수화 타입을 사용하지 못하게 했다(배열과 기본 타입은 허용한다).
List.class, String[].class, int.class는 허용하고 List<String>.class나 List<?>.class는 허용하지 않는다.

런타임에는 제네릭 타입 정보가 지워지므로 instanceof 연산자는 비한정적 와일드카드 타입 외에 매개변수화 타입에는 적용할 수 없다.
제네릭은 다음과 같이 instanceof를 사용해야 한다.
```java
if (o instanceof Set) {       // 로 타입
    Set<?> s = (Set<?>) o;    // 와일드카드 타입
    ...
}
```

## 📝 용어 정리
| 한글 용어             | 영문 용어                     | 예                         | 아이템                      |
|-----------------------|------------------------------|---------------------------|---------------------------|
| 매개변수화 타입       | parameterized type           | `List<String>`            | [아이템 26](item26.md)    |
| 실제 타입 매개변수     | actual type parameter        | `String` (in `List<String>`) | [아이템 26](item26.md)    |
| 제네릭 타입           | generic type                | `List<E>`                 | [아이템 26](item26.md), [아이템 29](item29.md) |
| 정규 타입 매개변수     | formal type parameter       | `E` (in `List<E>`)        | [아이템 26](item26.md)    |
| 비한정적 와일드카드 타입 | unbounded wildcard type      | `List<?>`                 | [아이템 26](item26.md)    |
| 로 타입              | raw type                    | `List`                    | [아이템 26](item26.md)    |
| 한정적 타입 매개변수 | bounded type parameter        | `<E extends Number>`  | [아이템 29](item29.md)    |
| 재귀적 타입 한정 | recursive type bound  | `<T extends Comparable<T>>`  | [아이템 30](item30.md)    |
| 한정적 와일드카드 타입 | bounded wildcard type        | `List<? extends Number>`  | [아이템 31](item31.md)    |
| 제네릭 메서드 | generic method  | `static <E> List<E> asList(E[] a)`  | [아이템 30](item30.md)    |
| 타입 토큰 | type token        | `String.class`  | [아이템 33](item33.md)    |
