# 아이템 49. 매개변수가 유효한지 검사하라
메서드와 생성자 대부분은 입력 매개변수 값에 특정 조건이 있을 것이다.
예를 들어 인덱스 값이 음수이면 안 되고 객체 참조는 null이 아니어야 한다.
이런 제약은 반드시 문서화해야 하며 메서드의 body부분이 실행되기 전에 검사해야 한다.

body가 실행되기 전에 매개변수를 확인한다면 즉각적으로 예외를 던질 수 있다.
하지만 실행 후에는 모호한 예외를 던지며 실패하거나 잘못된 결과를 반환할 수 있다.
더 심각한 경우는 메서드가 문제없이 실행되었으나, 어떤 객체의 상태를 이상하게 바꿔놓는 경우이다.
이러면 어디서 어떻게 잘못되었는지 찾기가 굉장히 어려워진다.

public과 protected 메서드는 매개변수 값이 잘못되었을 때 던지는 예외를 자바독의 `@throws` 태그를 통해 문서화해야 한다.
보통은 IllegalArgumentException, IndexOutOfBoundsException, NullPointerException 중 하나일 확률이 높다.
이를 문서화하는 경우 그 제약을 어겼을 시 발생하는 예외가 무엇인지도 기술해야 한다.
```java
/**
 * Returns a BigInteger whose value is (this mod m). This method
 * differs from the remainder method in that it always returns a
 * non-negative BigInteger.
 *
 * @param m the modulus, which must be positive
 * @return this mod m
 * @throws ArithmeticException if m is less than or equal to 0
*/
public BigInteger mod(BigInteger m) {
    if (m.signum() <= 0)
        throw new ArithmeticException("Modulus <= 0: " + m);
    ... // Do the computation
}
```
이 메서드는 m이 null이면 m.signum()을 호출할 때 NullPointerException을 던진다.
그런데 m이 null일 때 NullPointerException을 던진다는 말이 메서드 설명에 없는 이유는 개별 메서드가 아닌 BigInteger 클래스에 명시되어 있기 때문이다.

클래스 수준 주석은 그 클래스의 모든 public 메서드에 적용되므로 각 메서드에 일일이 기술하는 것보다 훨씬 깔끔하다.
`@Nullable`과 같은 애너테이션을 사용해 알려줄 수 있지만 표준적인 방법은 아니다.

자바 7에 추가된 java.util.Objects.requiredNonNull 메서드는 유연하고 사용하기도 편해서 null 검사를 수동으로 하지 않아도 된다.
원하는 예외 메시지를 지정하고, 입력을 그대로 반환하므로 값을 사용하는 동시에 null 검사를 수행할 수 있다.
```java
this.strategy = Objects.requireNonNull(strategy, "strategy");
```
공개되지 않은 메서드라면 패키지 제작자가 메서드가 호출되는 상황을 통제할 수 있다.
public이 아닌 메서드라면 assert를 사용해 매개변수 유효성을 검증할 수 있다.
```java
private static void sort(long a[], int offset, int length) {
    assert a != null;
    assert offset >= 0 && offset <= a.length;
    assert length >= 0 && length <= a.length - offset;
    ... // Do the computation
}
```
여기서의 중요한 점은 이 assert문들이 조건을 무조건 참이라고 선언한다는 것이다.
assert문은 일반적인 유효성 검사오는 조금 다르게 동작한다.
첫 번째, 실패하면 AssertionError를 던진다.
두 번째, 런타임에 아무런 효과나 성능 저하가 없다.

메서드가 직접 사용하지는 않으나 나중에 사용하기 위해 저장하는 매개변수는 더 신경 써서 검사해야 한다.
이러한 메서드가 잘못된 경우 추적이 어려워 디버깅이 상당히 고통스러울 것이다.

생성자 매개변수의 유효성 검사는 클래스 불변식을 어기는 객체가 만들어지지 않기 위해서 꼭 필요하다.

메서드 body 실행 전에 매개변수의 유효성을 검사해야 한다는 규칙에도 예외는 있다.
유효성 검사 비용이 지나치게 높거나 실용적이지 않을 때, 혹은 계산 과정에서 암묵적으로 검사가 수행될 때다.
Collections.sort(List)처럼 리스트를 정렬하는 메서드를 생각해보자.
리스트 안의 객체들은 모두 Comparable해야 하며, 정렬 과정에서 비교가 이루어진다.
만약 비교가 불가능하다면 비교 시 ClassCaseException을 던질 것이기 때문에 따로 Comparable인지 검사할 필요는 없다.
