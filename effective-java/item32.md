# 아이템 32. 제네릭과 가변인수를 함께 쓸 때는 신중하라
가변인수(varargs) 메서드와 제네릭은 자바 5 때 함께 추가되었다.
가변인수는 메서드에 넘기는 인수의 개수를 클라이언트가 조절할 수 있게 해주는데, 구현 방식에 허점이 있다.
가변인수 메서드를 호출하면 가변인수를 담기 위한 배열이 자동으로 하나 만들어진다.
내부로 감췄어야 할 이 배열을 클라이언트에 노출하는 문제가 있다.
그래서 varargs 매개변수에 제네릭이나 매개변수화 타입이 포함되면 알기 어려운 컴파일 경고가 발생한다.

[아이템 28](item28.md)에서 실체화 불가 타입은 런타임에는 컴파일타임보다 타입 관련 정보를 적게 담고 있음을 배웠다. 
그리고 거의 모든 제네릭과 매개변수화 타입은 실체화 되지 않는다.
때문에 메서드를 선언할 때 실체화 불가 타입으로 varargs 매개변수를 선언하거나, 가변인수 메서드를 호출할 때 varargs 매개변수가 실체화 불가 타입이라고 판단하면 컴파일러가 다음과 같은 경고를 보여준다.
```text
warning: [unchecked] Possible heap pollution from
    parameterized vararg type List<String>
```
매개변수화 타입의 변수가 타입이 다른 객체를 참조하면 힙 오염이 발생한다.
```java
static void dangerous(List<String>... stringLists) {
    List<Integer> intList = List.of(42);
    Object[] objects = stringLists;
    objects[0] = intList;              // 힙 오염 발생
    String s = stringLists[0].get(0);  // ClassCastException
}
```
마지막 줄에서 컴파일러가 생성한 보이지 않는 형변환이 숨어 있어 ClassCastException이 발생한다.
이처럼 타입 안정성이 깨지니 제네릭 varargs 배열 매개변수에 값을 저장하는 것은 안전하지 않다.

이 코드를 보면서 의문점이 생길 수 있다.
제네릭 배열을 프로그래머가 직접 생성하는 것은 허용되지 않는데, 제네릭 varargs 매개변수를 받는 메서드를 선언할 수 있게 한 이유는 무엇일까?
제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 메서드가 실무에서 매우 유용하기 때문이다.
자바 라이브러리도 이런 메서드를 여럿 제공하는데, `Arrays.asList(T... a)`, `Collections.addAll(Collection<? super T> c, T... elements)`, `EnumSet.of(E first, E... rest)`가 대표적이다.

자바 7에서 `@SafeVarargs` 애너테이션이 추가되어 제네릭 가변인수 메서드 작성자가 클라이언트 측에서 발생하는 경고를 숨길 수 있게 되었다.
이는 메서드 작성자가 그 메서드가 타입 안전함을 보장한다는 것을 의미한다.
메서드가 타입 안전한지는 다음과 같은 기준으로 판별한다.
가변인수 메서드를 호출할 때 varargs 매개변수를 담는 제네릭 배열의 값을 변경하지 않고, 그 배열의 참조가 밖으로 노출되어 신뢰할 수 없는 코드가 배열에 접근하는 일이 발생하지 않는다면 타입 안전하다고 할 수 있다.
쉽게 말해서 varargs 매개변수 배열이 순수하게 인수들을 전달하는 일만 한다면 안전하다.
```java
static <T> T[] toArray(T... args) {
    return args;
}
```
위와 같은 메서드는 제네릭 매개변수의 참조를 노출시켜 안전하지 않다.
이 메서드가 반환하는 배열의 타입은 컴파일타임에 결정되는데, 그 시점에는 컴파일러에 충분한 정보가 없어 타입을 잘못 판단할 수 있다.
따라서 varargs 매개변수 배열을 그대로 반환하면 힙 오염을 이 메서드를 호출한 쪽의 콜스택으로까지 전이하는 결과를 낳을 수 있다.

```java
static <T> T[] pickTwo(T a, T b, T c) {
    switch(ThreadLocalRandom.current().nextInt(3)) {
      case 0: return toArray(a, b);
      case 1: return toArray(a, c);
      case 2: return toArray(b, c);
    }
    throw new AssertionError();  // 도달할 수 없다.
}
```
이 pickTwo 메서드를 다음과 같이 사용한다고 해보자
```java
public static void main(String[] args) {
    String[] attributes = pickTwo("좋은", "빠른", "저렴한");
}
```
이 코드를 본 컴파일러는 toArray에 넘길 T 인스턴스 2개를 담을 varargs 매개변수 배열을 만드는 코드를 생성한다.
이 때 배열은 Object[]인데, pickTwo에 어떤 타입의 객체를 넘기더라도 담을 수 있는 가장 구체적인 타입이기 때문이다.
그리고 toArray 메서드가 돌려준 이 배열이 그대로 pickTwo를 호출한 클라이언트까지 전달된다.
pickTwo의 반환값을 attributes에 저장하러고 할 때, Objcts[]를 String[]으로 형변환하려고 시도하게 되는데, ClassCastException이 발생한다.

이처럼 제네릭 varargs 매개변수 배열에 다른 메서드가 접근하도록 허용하는 것은 안전하지 않다.
두 가지 예외가 있는데 `@SafaVarargs`로 제대로 애노테이트 된 또 다른 varargs 메서드에 넘기는 것과, varargs를 받지 않는 일반 메서드에 넘기는 것이다.
```java
@SafeVarargs
static <T> List<T> flatten(List<? extends T>... lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists)
        result.addAll(list);
    return result;
}
```
안전하지 않은 varargs 메서드는 절대 작성하지 말고, 제네릭이나 매개변수화 타입의 varargs 매개변수를 받는 모든 메서드에 `@SafeVarargs` 애너테이션을 사용하자.
안전한 제네릭 varargs 메서드를 작성하기 위해 다음 두 조건을 만족하는지 항상 확인하자.
- varargs 매개변수 배열에 아무것도 저장하지 않는다.
- 배열(혹은 복제본)을 신뢰할 수 없는 코드에 노출하지 않는다.

또한 `@SafeVarargs` 애너테이션은 재정의할 수 없는 메서드에만 달아야 한다.
재정의한 메서드도 안전할지는 보장할 수 없기 때문이다.
자바 8에서는 static 메서드와 final 인스턴스 메서드에만 붙일 수 있고, 자바 9부터는 private 인스턴스 메서드까지 허용된다.

[아이템 28](item28.md) 내용에 따라 varargs 매개변수를 List 매개변수로 바꿀 수도 있다.
flatten에 이를 적용하면 다음과 같이 변경할 수 있다.
```java
static <T> List<T> flatten(List<List<? extends T>> lists) {
    List<T> result = new ArrayList<>();
    for (List<? extends T> list : lists)
        result.addAll(list);
    return result;
}
```
static 팩터리 메서드인 List.of를 활용하면 다음과 같이 넘길 수 있다.
List.of에도 `@SafeVarargs`가 달려 있기 때문에 가능하다.
```java
audience = flatten(List.of(friends, romans, countrymen));
```
이 방식은 컴파일러가 메서드의 타입 안전성을 검증할 수 있다는 데 장점이 있다.
메서드를 작성할 때 타입 안전한지 직접 판단하고 `@SafeVarargs`를 달지 않아도 된다.
클라이언트 코드가 살짝 길어지고 속도가 조금 느려질 수 있는 단점도 존재한다.

toArray처럼 varargs 메서드를 안전하게 작성하는 게 불가능한 상황에서도 쓸 수 있다.
toArray의 List 버전이 List.of로, 자바 라이브러리 차원에서 제공하니 직접 작성할 필요도 없다.
이 방식을 pickTwo에 적용하면 다음과 같이 변경할 수 있다.
```java
static <T> List<T> pickTwo(T a, T b, T c) {
    switch(ThreadLocalRandom.current().nextInt(3)) {
      case 0: return List.of(a, b);
      case 1: return List.of(a, c);
      case 2: return List.of(b, c);
    }
    throw new AssertionError();  // 도달할 수 없다.
}
```
```java
public static void main(String[] args) {
    List<String> attributes = pickTwo("좋은", "빠른", "저렴한");
}
```
결과 코드는 배열 없이 제네릭만 사용하므로 타입 안전하다.
