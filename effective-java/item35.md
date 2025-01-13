# 아이템 35. ordinal 메서드 대신 인스턴스 필드를 사용하라
대부분의 열거 타입 상수는 하나의 정수값에 대응되고, 해당 상수가 몇 번째 위치인지를 반환하는 ordinal 메서드를 제공한다.
```java
public enum Ensemble {
    SOLO, DUET, TRIO, QUARTET, QUINTET,
    SEXTET, SEPTET, OCTET, NONET, DECTET;

    public int numberOfMusicians() { return ordinal() + 1; }
}
```
이러한 방법은 동작은 하지만 유지보수하기 불편하다.
상수 선언 순서를 바꾸는 순간 numberOfMusicians가 오작동하며, 이미 사용 중인 정수와 값이 같은 상수는 추가할 방법이 없다.
예를 들어 8중주(OCTET) 상수가 이미 있으니 똑같이 8명이 연주하는 복 4중주(DOUBLE QUARTET)는 추가할 수 없다.

또한, 값을 중간에 비워둘 수도 없다.
중간에 없는 값은 건너뛰고 추가하려면 dummy 상수를 같이 추가해줘야 하는데, 코드가 지저분해지고 실용성이 떨어진다.

열거 타입 상수에 연결된 값은 ordinal 메서드로 얻지 말고 인스턴스 필드에 저장하자.
```java
public enum Ensemble {
    SOLO(1), DUET(2), TRIO(3), QUARTET(4), QUINTET(5),
    SEXTET(6), SEPTET(7), OCTET(8), DOUBLE_QUARTET(8),
    NONET(9), DECTET(10), TRIPLE_QUARTET(12);

    private final int numberOfMusicians;
    Ensemble(int size) { this.numberOfMusicians = size; }
    public int numberOfMusicians() { return numberOfMusicians; }
}
```
Enum에서 ordinal을 살펴보면 다음과 같이 설명하고 있다.
```java
    /**
     * Returns the ordinal of this enumeration constant (its position
     * in its enum declaration, where the initial constant is assigned
     * an ordinal of zero).
     *
     * Most programmers will have no use for this method.  It is
     * designed for use by sophisticated enum-based data structures, such
     * as {@link java.util.EnumSet} and {@link java.util.EnumMap}.
     *
     * @return the ordinal of this enumeration constant
     */
    public final int ordinal() {
        return ordinal;
    }
```
"대부분의 프로그래머는 이 메서드를 쓸 일이 없다. 이 메서드는 EnumSet이나 EnumMap같이 정교한 enum-based 자료 구조를 위해 설계되었다."라고 적혀 있다.
이러한 목적이 아니라면 사용하지 말자.
