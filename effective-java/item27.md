# 아이템 27. 비검사 경고를 제거하라
제네릭을 사용하면 비검사 형변환 경고, 비검사 메서드 호출 경고, 비검사 매개변수화 가변인수 타입 경고, 비검사 변환 경고 등 다양한 컴파일러 경고를 보게 된다.
다음과 같이 코드를 잘못 작성했다고 해보자.
```java
Set<Lark> exaltation = new HashSet();
```
그러면 컴파일러에서 다음과 같은 경고를 보여준다(javac 명령줄 인수에 -Xlint:uncheck 옵션을 추가해야 한다).
```text
Venery.java:4: warning [unchecked] unchecked conversion
        Set<Lark> exaltation = new HashSet();
                               ^
  required: Set<Lark>
  found:    HashSet
```
컴파일러가 알려준 대로 수정하면 경고가 사라진다.
자바 7부터 지원하는 다이아몬드 연산자(<>)를 사용해도 해결된다.
```java
Set<Lark> exaltation = new HashSet<>();
```
이렇게 작성하면 컴파일러가 올바른 실제 타입 매개변수를 추론해준다.

이러한 비검사 경고를 제거하면 타입 안정성이 보장된다.
런타임에 `ClassCastException`이 발생할 일이 없고, 의도한 대로 잘 동작할 것이다.
경고를 제거할 수는 없지만 안전하다고 확신할 수 있다면 `@SuppressWarnings("unchecked")` 애너테이션을 달아 경고를 숨기자.
단, 검증하지 않고 경고를 숨기면 런타임 시에 `ClassCastException`이 발생할 수 있으니 주의하자.

`@SuppressWarnings` 애너테이션은 개별 지역변수 선언부터 클래스 전체까지 어떤 선언에도 달 수 있다.
이는 가능한 좁은 범위에 적용해야 한다.
자칫하면 심각한 경고를 놓칠 수 있으니 클래스 전체에 적용해서는 안 된다.

ArrayList의 toArray 메서드를 예로 생각해보자.
```java
public <T> T[] toArray(T[] a) {
    if (a.length < size)
        return (T[]) Arrays.copyOf(elements, size, a.getClass());
    System.arraycopy(elements, 0, a, 0, size);
    if (a.length > size)
        a[size] = null;
    return a;
}
```
ArrayList를 컴파일하면 toArray에서 다음과 같은 경고가 발생한다.
```text
ArrayList.java:305: warning: [unchecked] unchecked cast
        return (T[]) Arrays.copyOf(elements, size, a.getClass());

  required: T[]
  found:    Object[]
```
애너테이션은 선언에만 달 수 있기 때문에 return 문에는 `@SuppressWarnings`를 다는 게 불가능하다.
메서드 전체에 달게 되면 필요 이상으로 넓어지니 반환값을 담을 지역변수를 하나 선언하고 그 변수에 애너테이션을 달아주자.
```java
public <T> T[] toArray(T[] a) {
    if (a.length < size) {
        // 생성한 배열과 매개변수로 받은 배열의 타입이 모두 T[]로 같으므로 올바른 형변환이다.
        @SuppressWarnings("unchecked") T[] result =
            (T[]) Arrays.copyOf(elements, size, a.getClass());
        return result;
    }
    System.arraycopy(elements, 0, a, 0, size);
    if (a.length > size)
        a[size] = null;
    return a;
}
```
이 코드는 경고 없이 깔끔하게 컴파일되고 비검사 경고를 숨기는 범위도 최소화했다.
`@SuppressWarnings("unchecked")` 애너테이션을 사용하면 그 경고를 무시해도 안전한 이유를 항상 주석으로 남겨야 한다.
