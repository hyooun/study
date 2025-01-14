# 아이템 36. 비트 필드 대신 EnumSet을 사용하라
열거한 값들이 주로 단독이 아닌 집합으로 사용될 경우, 예전에는 각 상수에 서로 다른 2의 거듭제곱 값을 할당한 정수 열거 패턴([아이템 34](item34.md))을 사용했었다.
```java
public class Text {
    public static final int STYLE_BOLD          = 1 << 0;  // 1
    public static final int STYLE_ITALIC        = 1 << 1;  // 2
    public static final int STYLE_UNDERLINE     = 2 << 2;  // 4
    public static final int STYLE_STRIKETHROUGH = 1 << 3;  // 8

    // 매개변수 styles는 0개 이상의 STYLE_ 상수를 비트별 OR한 값이다.
    public void applyStyles(int styles) { ... }
}
```
다음과 같은 식으로 비트별 OR을 사용해 여러 상수를 하나의 집합으로 모을 수 있으며, 이렇게 만들어진 집합을 비트 필드(bit field)라 한다.
```java
text.applyStyles(STYLE_BOLD | STYLE_ITALIC);
```
이를 사용하면 비트별 연산으로 합집합과 교집합 같은 집합 연산을 효율적으로 수행할 수 있지만, 정수 열거 상수의 단점을 그대로 가지고 있다.
또한 비트 필드 값이 그대로 출력되면 단순한 정수 열거 상수를 출력할 때보다 해석하기도 어렵다.

이를 보완한 java.util 패키지의 EnumSet 클래스는 열거 타입 상수의 값으로 구성된 집합을 효과적으로 표현한다.
Set 인터페이스를 구현하며, 타입 안전하고, 다른 Set 구현체와도 함께 사용할 수 있다.
EnumSet 내부는 비트 벡터로 구현되어 원소가 총 64개 이하라면 EnumSet 전체를 long 변수 하나로 표현하여 비트 필드와 비슷한 성능을 보여준다.

removeAll이나 retainAll 같은 대량 작업은 비트를 효율적으로 처리할 수 있는 산술 연산으로 구현되었다.
앞의 예를 EnumSet을 사용하도록 수정하면 다음과 같다.
```java
public class Text {
    public enum Style { BOLD, ITALIC, UNDERLINE, STRIKETHROUGH }

    // 어떤 Set을 넘겨도 상관 없으나 EnumSet이 가장 좋다.
    public void applyStyles(Set<Style> styles) { ... }
}
```
이를 사용하는 코드는 다음과 같다.
```java
text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC));
```
applyStyles 메서드에서 `EnumSet<Style>`이 아닌 `Set<Style>`을 받은 이유는 혹시나 클라이언트가 다른 Set 구현체를 넘기더라도 처리할 수 있도록 인터페이스로 받는 것이 더 유연하기 때문이다([아이템 64](item64.md)).
